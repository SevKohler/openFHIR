package com.medblocks.openfhir.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.fc.schema.model.Condition;
import com.medblocks.openfhir.fc.schema.model.Mapping;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OpenEhrConditionEvaluator {

    private OpenFhirStringUtils openFhirStringUtils;

    @Autowired
    public OpenEhrConditionEvaluator(final OpenFhirStringUtils openFhirStringUtils) {
        this.openFhirStringUtils = openFhirStringUtils;
    }

    /**
     * If openehrCondition restricts a mapping only to a certain criteria (i.e. something has to be empty for a
     * mapping to be executed), this is done here. Returns true if a mapping can be executed (condition passes)
     * or false if it mustn't be.
     *
     * @param mapping mapping to be checked
     * @param jsonObject json object holding Composition in a flat path format
     * @return true if a mapping has to be executed or false if not
     */
    public boolean checkOpenEhrCondition(final Mapping mapping, final JsonObject jsonObject,
                                         final String mainOpenEhrPath) {
        if (mapping.getOpenehrCondition() == null) {
            return true;
        }
        final String operator = mapping.getOpenehrCondition().getOperator();
        switch (operator) {
            case FhirConnectConst.CONDITION_OPERATOR_EMPTY -> {
                return checkEmptyCondition(mapping.getOpenehrCondition(), jsonObject, mainOpenEhrPath);
            }
            case FhirConnectConst.CONDITION_OPERATOR_NOT_EMPTY -> {
                return true;
            }
        }
        return true;
    }

    public boolean checkOpenEhrCondition(final Condition condition,
                                         final JsonObject jsonObject,
                                         final String mainOpenEhrPath) {
        if (condition == null) {
            return true;
        }
        final String operator = condition.getOperator();
        switch (operator) {
            case FhirConnectConst.CONDITION_OPERATOR_EMPTY -> {
                return checkEmptyCondition(condition, jsonObject, mainOpenEhrPath);
            }
            case FhirConnectConst.CONDITION_OPERATOR_NOT_EMPTY -> {
                return true;
            }
        }
        return true;
    }

    public boolean checkEmptyCondition(final Condition openEhrCondition,
                                       final JsonObject jsonObject,
                                       final String mainOpenEhrPath) {
        final String openEhrPath = resolveConditionOpenEhrPath(openEhrCondition, mainOpenEhrPath);
        final List<String> fullOpenEhrPaths = buildConditionPaths(openEhrPath, getTargetAttributes(openEhrCondition));
        for (final String fullOpenEhrPath : fullOpenEhrPaths) {
            final List<String> matchingEntries = openFhirStringUtils.getAllEntriesThatMatchIgnoringPipe(
                    fullOpenEhrPath,
                    jsonObject);
            if (!matchingEntries.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkNotEmptyCondition(final Condition openEhrCondition,
                                          final JsonObject jsonObject,
                                          final String mainOpenEhrPath) {
        final String openEhrPath = resolveConditionOpenEhrPath(openEhrCondition, mainOpenEhrPath);
        final List<String> fullOpenEhrPaths = buildConditionPaths(openEhrPath, getTargetAttributes(openEhrCondition));
        for (final String fullOpenEhrPath : fullOpenEhrPaths) {
            final List<String> matchingEntries = openFhirStringUtils.getAllEntriesThatMatchIgnoringPipe(
                    fullOpenEhrPath,
                    jsonObject);
            if (!matchingEntries.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String resolveConditionOpenEhrPath(final Condition openEhrCondition,
                                               final String mainOpenEhrPath) {
        final String targetRoot = openEhrCondition == null ? null : openEhrCondition.getTargetRoot();
        if (StringUtils.isBlank(targetRoot)) {
            return StringUtils.defaultString(mainOpenEhrPath);
        }
        if (StringUtils.isBlank(mainOpenEhrPath)) {
            return targetRoot.replace(FhirConnectConst.REFERENCE + "/", "");
        }
        return openFhirStringUtils.fixOpenEhrPath(targetRoot, mainOpenEhrPath);
    }

    private List<String> getTargetAttributes(final Condition condition) {
        if (condition.getTargetAttributes() != null && !condition.getTargetAttributes().isEmpty()) {
            return condition.getTargetAttributes();
        }
        if (condition.getTargetAttribute() != null) {
            return Collections.singletonList(condition.getTargetAttribute());
        }
        return Collections.emptyList();
    }

    private List<String> buildConditionPaths(final String baseOpenEhrPath,
                                             final List<String> targetAttributes) {
        if (targetAttributes == null || targetAttributes.isEmpty()) {
            return Collections.singletonList(baseOpenEhrPath);
        }
        final List<String> fullPaths = new ArrayList<>();
        for (final String targetAttribute : targetAttributes) {
            fullPaths.add(StringUtils.isBlank(targetAttribute)
                    ? baseOpenEhrPath
                    : String.format("%s/%s", baseOpenEhrPath, targetAttribute));
        }
        return fullPaths;
    }

    private JsonObject handleOneOfOperatorSplit(final Condition openEhrCondition,
                                                final List<String> extractedValueKeys,
                                                final JsonObject fullFlatPath) {
        if (extractedValueKeys.isEmpty()) {
            // no such flat path even exists, so let's just consider all entries?
            return fullFlatPath;
        }
        final String targetAttribute = openEhrCondition.getTargetAttribute() == null ? openEhrCondition.getTargetAttributes().get(0) : openEhrCondition.getTargetAttribute();
        final JsonObject modifiedJsonObject = new JsonObject();
        final Set<String> notAddingForThisCondition = new HashSet<>();
        for (final String extractedValueKey : extractedValueKeys) {
            final String preparedTargetAttribute = openFhirStringUtils.prepareOpenEhrSyntax(
                    targetAttribute,
                    "");
            final String openEhrKey = preparedTargetAttribute == null ? extractedValueKey : String.format("%s/%s", extractedValueKey, preparedTargetAttribute);
            final JsonPrimitive extractedValueJson = fullFlatPath.getAsJsonPrimitive(openEhrKey);
            final String extractedValue = extractedValueJson == null ? "" : extractedValueJson.getAsString();

            if (openEhrCondition.getCriteria().contains(extractedValue)) {
                fullFlatPath.entrySet().forEach((entry) -> {
                    if (entry.getKey().startsWith(extractedValueKey)) {
                        modifiedJsonObject.add(entry.getKey(), entry.getValue());
                    }
                });
                continue;
            }

            log.info(
                    "Flat path {} evaluated to {}, condition.criteria requires it to be {}, therefore excluding all {} from mapping.",
                    openEhrKey, extractedValue, openEhrCondition.getCriteria(), extractedValueKey);
            notAddingForThisCondition.add(extractedValueKey);

            fullFlatPath.entrySet().forEach((entry) -> {
//                if (!entry.getKey().startsWith(extractedValueKey)) {
//                    modifiedJsonObject.add(entry.getKey(), entry.getValue());
//                }
                if (notAddingForThisCondition.stream().noneMatch(extractedValueKey::startsWith)) {
                    modifiedJsonObject.add(entry.getKey(), entry.getValue());
                }
            });
        }
        return modifiedJsonObject;
    }

    private JsonObject handleEmptyOperatorSplit(final Condition openEhrCondition,
                                                final List<String> extractedValueKeys,
                                                final JsonObject fullFlatPath) {
        if (extractedValueKeys.isEmpty()) {
            // no such flat path even exists, so let's just consider all entries?
            return fullFlatPath;
        }
        final JsonObject modifiedJsonObject = new JsonObject();
        for (final String extractedValueKey : extractedValueKeys) {
            final List<String> preparedAttributes = getTargetAttributes(openEhrCondition).isEmpty()
                    ? Collections.singletonList("")
                    : getTargetAttributes(openEhrCondition);
            for (final String targetAttribute : preparedAttributes) {
                final String preparedTargetAttribute = StringUtils.isBlank(targetAttribute) ? null :
                        openFhirStringUtils.prepareOpenEhrSyntax(targetAttribute, "");
                final String openEhrKey = preparedTargetAttribute == null
                        ? extractedValueKey
                        : String.format("%s/%s", extractedValueKey, preparedTargetAttribute);
                final List<String> matchingEntries = openFhirStringUtils.getAllEntriesThatMatchIgnoringPipe(openEhrKey,
                                                                                                            fullFlatPath);

                if (!matchingEntries.isEmpty()) {
                    log.info(
                            "Flat path {} didn't evaluate to empty, as per condition, therefore excluding all {} from mapping.",
                            openEhrKey, extractedValueKey);
                    continue;
                }


                fullFlatPath.entrySet().forEach((entry) -> {
                    if (entry.getKey().startsWith(extractedValueKey)) {
                        modifiedJsonObject.add(entry.getKey(), entry.getValue());
                    }
                });
            }

        }
        return modifiedJsonObject;
    }

    private JsonObject handleNotEmptyOperatorSplit(final Condition openEhrCondition,
                                                   final List<String> extractedValueKeys,
                                                   final JsonObject fullFlatPath) {
        if (extractedValueKeys.isEmpty()) {
            return fullFlatPath;
        }
        final JsonObject modifiedJsonObject = new JsonObject();
        for (final String extractedValueKey : extractedValueKeys) {
            boolean hasMatchingEntries = false;
            final List<String> preparedAttributes = getTargetAttributes(openEhrCondition).isEmpty()
                    ? Collections.singletonList("")
                    : getTargetAttributes(openEhrCondition);
            for (final String targetAttribute : preparedAttributes) {
                final String preparedTargetAttribute = StringUtils.isBlank(targetAttribute) ? null :
                        openFhirStringUtils.prepareOpenEhrSyntax(targetAttribute, "");
                final String openEhrKey = preparedTargetAttribute == null
                        ? extractedValueKey
                        : String.format("%s/%s", extractedValueKey, preparedTargetAttribute);
                final List<String> matchingEntries = openFhirStringUtils.getAllEntriesThatMatchIgnoringPipe(openEhrKey,
                                                                                                            fullFlatPath);
                if (!matchingEntries.isEmpty()) {
                    hasMatchingEntries = true;
                    break;
                }
            }
            if (!hasMatchingEntries) {
                continue;
            }
            fullFlatPath.entrySet().forEach((entry) -> {
                if (entry.getKey().startsWith(extractedValueKey)) {
                    modifiedJsonObject.add(entry.getKey(), entry.getValue());
                }
            });
        }
        return modifiedJsonObject;
    }


    /**
     * If a mapping has openehrCondition, then the whole JsonObject representing flatPath Composition needs to be split
     * in a way so that iteration of tha mapping only extracts from the relevant part of the JsonObject
     *
     * @return a split JsonObject if openEhrCondition is not null, otherwise the original fullFlatPath
     */
    public JsonObject splitByOpenEhrCondition(final JsonObject fullFlatPath, final Condition openEhrCondition,
                                              final String firstFlatPath) {
        if (openEhrCondition == null) {
            return fullFlatPath;
        }

        final List<String> narrowingCriteria = narrowingCriteria(openEhrCondition, firstFlatPath, fullFlatPath);

        switch (openEhrCondition.getOperator()) {
            case FhirConnectConst.CONDITION_OPERATOR_ONE_OF -> {
                return handleOneOfOperatorSplit(openEhrCondition,
                                                narrowingCriteria,
                                                fullFlatPath);
            }
            case FhirConnectConst.CONDITION_OPERATOR_EMPTY -> {
                return handleEmptyOperatorSplit(openEhrCondition, narrowingCriteria, fullFlatPath);
            }
            case FhirConnectConst.CONDITION_OPERATOR_NOT_EMPTY -> {
                return handleNotEmptyOperatorSplit(openEhrCondition, narrowingCriteria, fullFlatPath);
            }
        }
        return fullFlatPath;
    }

    public List<String> narrowingCriteria(final Condition openEhrCondition, final String firstFlatPath,
                                           final JsonObject fullFlatPath) {
        final String openEhrPath = openFhirStringUtils.prepareOpenEhrSyntax(openEhrCondition.getTargetRoot(),
                                                                            firstFlatPath);

        final String withRegex = openFhirStringUtils.addRegexPatternToSimplifiedFlatFormat(openEhrPath);
        return openFhirStringUtils.getAllEntriesThatMatch(withRegex, fullFlatPath).stream().distinct().collect(
                Collectors.toList());

    }

}
