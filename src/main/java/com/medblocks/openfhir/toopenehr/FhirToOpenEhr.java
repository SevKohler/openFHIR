package com.medblocks.openfhir.toopenehr;

import static com.medblocks.openfhir.fc.FhirConnectConst.FHIR_ROOT_FC;
import static com.medblocks.openfhir.fc.FhirConnectConst.OPENEHR_TYPE_NONE;
import static com.medblocks.openfhir.util.OpenFhirStringUtils.RECURRING_SYNTAX;
import static com.medblocks.openfhir.util.OpenFhirStringUtils.RECURRING_SYNTAX_ESCAPED;
import static com.medblocks.openfhir.util.OpenFhirStringUtils.RESOLVE;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medblocks.openfhir.OpenEhrRmWorker;
import com.medblocks.openfhir.OpenFhirMappingContext;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.fc.OpenFhirFhirConfig;
import com.medblocks.openfhir.fc.OpenFhirFhirConnectModelMapper;
import com.medblocks.openfhir.fc.schema.context.FhirConnectContext;
import com.medblocks.openfhir.fc.schema.model.Condition;
import com.medblocks.openfhir.fc.schema.model.Mapping;
import com.medblocks.openfhir.fc.schema.model.With;
import com.medblocks.openfhir.util.OpenEhrCachedUtils;
import com.medblocks.openfhir.util.OpenEhrConditionEvaluator;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.quantity.datetime.DvDateTime;
import com.nedap.archie.rm.generic.PartySelf;
import com.nedap.archie.rm.support.identification.TerminologyId;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.pf4j.PluginManager;
import com.medblocks.openfhir.plugin.api.FormatConverter;
import com.medblocks.openfhir.util.SpringContext;

@Slf4j
@Component
public class FhirToOpenEhr {


    final private FhirPathR4 fhirPathR4;
    final private OpenFhirStringUtils stringUtils;
    final private FlatJsonUnmarshaller flatJsonUnmarshaller;
    final private Gson gson;
    final private OpenEhrRmWorker openEhrRmWorker;
    final private OpenFhirStringUtils openFhirStringUtils;
    final private OpenFhirMappingContext openFhirTemplateRepo;
    final private OpenEhrCachedUtils openEhrApplicationScopedUtils;
    final private OpenFhirMapperUtils openFhirMapperUtils;
    final private OpenEhrPopulator openEhrPopulator;
    final private OpenEhrConditionEvaluator openEhrConditionEvaluator;

    @Autowired
    public FhirToOpenEhr(final FhirPathR4 fhirPathR4,
                         final OpenFhirStringUtils stringUtils,
                         final FlatJsonUnmarshaller flatJsonUnmarshaller,
                         final Gson gson,
                         final OpenEhrRmWorker openEhrRmWorker,
                         final OpenFhirStringUtils openFhirStringUtils,
                         final OpenFhirMappingContext openFhirTemplateRepo,
                         final OpenEhrCachedUtils openEhrApplicationScopedUtils,
                         final OpenFhirMapperUtils openFhirMapperUtils,
                         final OpenEhrPopulator openEhrPopulator,
                         final OpenEhrConditionEvaluator openEhrConditionEvaluator) {
        this.fhirPathR4 = fhirPathR4;
        this.stringUtils = stringUtils;
        this.flatJsonUnmarshaller = flatJsonUnmarshaller;
        this.gson = gson;
        this.openEhrRmWorker = openEhrRmWorker;
        this.openFhirStringUtils = openFhirStringUtils;
        this.openFhirTemplateRepo = openFhirTemplateRepo;
        this.openEhrApplicationScopedUtils = openEhrApplicationScopedUtils;
        this.openFhirMapperUtils = openFhirMapperUtils;
        this.openEhrPopulator = openEhrPopulator;
        this.openEhrConditionEvaluator = openEhrConditionEvaluator;
    }

    /**
     * Main method that takes care of mapping from FHIR to openEHR. Mapping is always done to a flat path that can
     * later on be converted to a canonical JSON format.
     *
     * @param context fhir connect context mapper
     * @param resource that needs to be mapped to openEHR, can be a Bundle or a single Resource
     * @param operationaltemplate that is linked to the context mapper
     * @return JsonObject representing a flat path structure/format of the mapped openEHR Composition
     */
    public JsonObject fhirToFlatJsonObject(final FhirConnectContext context, final Resource resource,
                                           final OPERATIONALTEMPLATE operationaltemplate) {
//        final boolean bundle = ResourceType.Bundle.name().equals(context.getFhir().getResourceType()); todo: is this always true? with new context mappings there's no more fhir type
        final boolean bundle = true;

        // mapping is always done on a Bundle, therefore if the incoming Resource is not a Bundle, we first wrap it to one
        final Bundle toRunEngineOn = prepareBundle(resource);

        final WebTemplate webTemplate = openEhrApplicationScopedUtils.parseWebTemplate(operationaltemplate);
        final String templateId = OpenFhirMappingContext.normalizeTemplateId(
                context.getContext().getTemplate().getId());

        // helper objects for mapping to openEHR, where 'helpers' are regular ones constructed as part of the
        // mapping and 'coverHelpers' are those that don't directly reference the FHIR Resource but another one
        // for example a Condition that is part of the Patient's death reason
        final List<FhirToOpenEhrHelper> helpers = new ArrayList<>();
        final List<FhirToOpenEhrHelper> coverHelpers = new ArrayList<>();

        // create helpers
        createHelpers(templateId, toRunEngineOn, null, helpers, coverHelpers, bundle, context.getContext().getStart());

        // join all helpers together
        helpers.addAll(coverHelpers);

        openFhirStringUtils.fixEscapedDotsInOpenEhrPaths(helpers);

        // modify flat path with correct openEHR path containing occurrences and proper types
        openEhrRmWorker.fixFlatWithOccurrences(helpers, webTemplate);

        // do the actual mapping (evaluate fhir paths and create json flat structure from it, based on helpers)
        return resolveFhirPaths(helpers, toRunEngineOn);
    }

    /**
     * If resource is not already a bundle, it will wrap it to a Bundle
     * additional business logic can be done here to make sure references between resources within a Bundle
     * are properly done and prepared for the mapping engine
     */
    private Bundle prepareBundle(final Resource resource) {
        final Bundle toRunEngineOn;
        if (!(resource instanceof Bundle)) {
            toRunEngineOn = new Bundle();
            toRunEngineOn.addEntry(new Bundle.BundleEntryComponent().setResource(resource));
        } else {
            toRunEngineOn = (Bundle) resource;

            // fix referenced resources?
        }
        return toRunEngineOn;
    }

    /**
     * Mapping to a canonical format of a Composition. This method invokes the fhirToFlatJsonObject and then serializes
     * flat json path output to a canonical format.
     *
     * @param context fhir connect context being used for the mappings
     * @param resource FHIR Resource that's being mapped to openEHR
     * @param operationaltemplate openEHR template being referenced by this context mapper
     * @return Composition that's been created based on the FHIR input and fhir connect mappers
     */
    public Composition fhirToCompositionRm(final FhirConnectContext context, final Resource resource,
                                           final OPERATIONALTEMPLATE operationaltemplate) {
        final WebTemplate webTemplate = openEhrApplicationScopedUtils.parseWebTemplate(operationaltemplate);

        // invoke the actual mapping logic
        final JsonObject flattenedWithValues = fhirToFlatJsonObject(context, resource, operationaltemplate);

        // unmarshall flat path to a canonical json format
        final Composition composition = flatJsonUnmarshaller.unmarshal(gson.toJson(flattenedWithValues), webTemplate);

        enrichComposition(composition);

        return composition;
    }

    /**
     * Method that adds all required metadata to a Composition, but only if this was not already set as part
     * of the mapping logic itself.
     *
     * @param composition enriched with metadata that wasn't mapped
     */
    public void enrichComposition(final Composition composition) {
        // default values; set if not already set by mappings
        if (composition.getLanguage() == null) {
            composition.setLanguage(new CodePhrase(new TerminologyId("ISO_639-1"), "en"));
        }
        if (composition.getTerritory() == null) {
            composition.setTerritory(new CodePhrase(new TerminologyId("ISO_3166-1"), "DE"));
        }
        if (composition.getComposer() == null) {
            composition.setComposer(new PartySelf());
        }
        if (composition.getContext() != null && composition.getContext().getStartTime() == null) {
            composition.getContext().setStartTime(new DvDateTime(getUpdatedDateTime().toString()));
        }
    }

    private static LocalDateTime getUpdatedDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return utcDateTime.toLocalDateTime();
    }

    /**
     * Given FhirToOpenEhrHelpers, this method creates the actual json flat structure based on them. It evaluates
     * fhir paths from the incoming Resource and sets them in a JsonObject with the right openEHR flat path.
     *
     * @param helpers used to do the mapping
     * @param resource to be mapped to openEHR
     * @return JsonObject representing a flat path structure/format of the openEHR Composition
     */
    private JsonObject resolveFhirPaths(final List<FhirToOpenEhrHelper> helpers, final Resource resource) {
        final JsonObject finalFlat = new JsonObject();

        final Map<String, List<FhirToOpenEhrHelper>> byMainArtifact = mapperByMainArtifact(helpers);
        for (Map.Entry<String, List<FhirToOpenEhrHelper>> artifactMapper : byMainArtifact.entrySet()) {
            final List<FhirToOpenEhrHelper> artifactHelpers = artifactMapper.getValue();

            // distinct by limiting criteria to avoid duplicated mappings
            artifactHelpers.stream().map(FhirToOpenEhrHelper::getLimitingCriteria).distinct().forEach(lim -> {

                if (resource instanceof Bundle) {
                    handleBundleExtraction((Bundle) resource, lim, artifactHelpers, finalFlat);
                } else {
                    for (FhirToOpenEhrHelper fhirToOpenEhrHelper : artifactHelpers) {
                        final Condition openEhrTypeCondition = fhirToOpenEhrHelper.getTypeCondition();
                        if (openEhrTypeCondition != null
                                && openEhrTypeCondition.getCriteria().equals(fhirToOpenEhrHelper.getOpenEhrType())) {
                            continue;
                        }

                        final List<Base> result = fhirPathR4.evaluate(resource, fhirToOpenEhrHelper.getFhirPath(),
                                Base.class);

                        handleOccurrenceResults(fhirToOpenEhrHelper.getOpenEhrPath(),
                                fhirToOpenEhrHelper.getOpenEhrType(), result, finalFlat);
                    }

                }
            });

        }

        return finalFlat;
    }

    /**
     * Resolve fhir paths from a Bundle
     */
    private void handleBundleExtraction(final Bundle resource, final String lim,
                                        final List<FhirToOpenEhrHelper> artifactHelpers, final JsonObject finalFlat) {
        // apply limiting factor
        final List<Base> relevantResources = fhirPathR4.evaluate(resource, lim, Base.class);

        if (relevantResources.isEmpty()) {
            log.warn("No relevant resources found for {}", lim);
        } else {
            log.info("Evaluation of {} returned {} entries that will be used for mapping.", lim,
                    relevantResources.size());
        }

        String mainMultiple = null;

        int i = 0;
        for (final Base relevantResource : relevantResources) {
            boolean somethingWasAdded = false;
            for (final FhirToOpenEhrHelper fhirToOpenEhrHelper : artifactHelpers) {
                final Condition openEhrTypeCondition = fhirToOpenEhrHelper.getTypeCondition();
                if (openEhrTypeCondition != null
                        && openEhrTypeCondition.getCriteria().equals(fhirToOpenEhrHelper.getOpenEhrType())) {
                    continue;
                }

                final FhirToOpenEhrHelper cloned = fhirToOpenEhrHelper.doClone();

                // If mappingCode is lost in cloning, set it explicitly
                if (fhirToOpenEhrHelper.getMappingCode() != null && cloned.getMappingCode() == null) {
                    cloned.setMappingCode(fhirToOpenEhrHelper.getMappingCode());
                }


                if (fhirToOpenEhrHelper.getMultiple() && (mainMultiple == null || fhirToOpenEhrHelper.getOpenEhrPath()
                        .startsWith(mainMultiple))) {

                    final String openEhrPath = fhirToOpenEhrHelper.getOpenEhrPath();
                    mainMultiple =
                            ignoreMultipleFlag(openEhrPath) ? null : openEhrPath.split(RECURRING_SYNTAX_ESCAPED)[0];
                    cloned.setOpenEhrPath(
                            fhirToOpenEhrHelper.getOpenEhrPath().replaceFirst(RECURRING_SYNTAX_ESCAPED, ":" + i));

                    fixAllChildrenRecurringElements(cloned,
                            cloned.getOpenEhrPath());
                }
                int previousFinalFlatSize = finalFlat.size();
                addDataPoints(cloned, finalFlat, relevantResource);

                somethingWasAdded = somethingWasAdded || previousFinalFlatSize < finalFlat.size();
            }
            if (somethingWasAdded) {
                i++;
            } else {
                log.warn(
                        "Even though a Resource matched criteria, nothing was added to the openEHR composition from it: {}",
                        relevantResource.getIdBase());
            }
        }
    }

    private boolean ignoreMultipleFlag(final String openEhrPath) {
        return openEhrPath.contains("context")
                || openEhrPath.contains("other_participations")
                || openEhrPath.contains("provider");
    }

    /**
     * Joins all helpers of the same archetype
     *
     * @return a Map where key is the artifactId and value is a list of all corresponding Helpers
     */
    private Map<String, List<FhirToOpenEhrHelper>> mapperByMainArtifact(final List<FhirToOpenEhrHelper> flattened) {
        return flattened.stream().collect(Collectors.groupingBy(FhirToOpenEhrHelper::getArchetype));
    }

    JsonObject handleOccurrenceResults(final String openEhrPath, final String openEhrType,
                                       final List<Base> fhirPathResults, final JsonObject finalFlat) {
        if (fhirPathResults == null || fhirPathResults.isEmpty()) {
            return finalFlat;
        }
        final boolean noMoreRecurringOptions = !openEhrPath.contains(RECURRING_SYNTAX);
        final String openEhrWithAllReplacedToZeroth = openEhrPath.replaceAll(RECURRING_SYNTAX_ESCAPED, ":0");
        if (fhirPathResults.size() == 1) {
            // it's a single find, so replace all those multiple-occurrences with zeroth index
            openEhrPopulator.setFhirPathValue(openEhrWithAllReplacedToZeroth, fhirPathResults.get(0), openEhrType,
                    finalFlat);
        } else {
            // many results, set all but the last one to :0.. the last one index according to amount of results in fhirPathResults
            if (noMoreRecurringOptions) {
                log.warn(
                        "Found more than one result, yet there's no more recurring options! Only adding the first result to openEhr flat.");
                openEhrPopulator.setFhirPathValue(openEhrWithAllReplacedToZeroth, fhirPathResults.get(0), openEhrType,
                        finalFlat);
            } else {
                for (int i = 0; i < fhirPathResults.size(); i++) {
                    final Base fhirPathResult = fhirPathResults.get(i);
                    final String finalOpenEhrPath = openFhirStringUtils.replaceLastIndexOf(
                            openEhrWithAllReplacedToZeroth, ":0", ":" + i);

                    openEhrPopulator.setFhirPathValue(finalOpenEhrPath, fhirPathResult, openEhrType, finalFlat);
                }
            }
        }
        return null;
    }

    private boolean handleDataAbsentReasonWhenNoResult(final FhirToOpenEhrHelper helper,
                                                       final JsonObject flatComposition,
                                                       final Base toResolveOn) {
        if (helper == null || flatComposition == null || toResolveOn == null) {
            return false;
        }
        final String nullFlavourPath = deriveNullFlavourPath(helper.getOpenEhrPath());
        if (StringUtils.isBlank(nullFlavourPath)) {
            return false;
        }
        final List<Base> dataAbsentReasons = resolveDataAbsentReasonValuesForMissingPath(
                toResolveOn, helper.getFhirPath());
        if (dataAbsentReasons.isEmpty()) {
            return false;
        }
        for (Base reason : dataAbsentReasons) {
            if (openEhrPopulator.setNullFlavourForDataAbsentReason(nullFlavourPath, reason, flatComposition)) {
                return true;
            }
        }
        return false;
    }

    private String deriveNullFlavourPath(final String openEhrPath) {
        if (StringUtils.isBlank(openEhrPath)) {
            return null;
        }
        String basePath = openEhrPath;
        final int pipeIndex = basePath.indexOf('|');
        if (pipeIndex >= 0) {
            basePath = basePath.substring(0, pipeIndex);
        }
        if (basePath.contains(RECURRING_SYNTAX)) {
            basePath = basePath.replace(RECURRING_SYNTAX, ":0");
        }
        basePath = stripTerminalValueNode(basePath);
        if (basePath.endsWith("_null_flavour")) {
            return basePath;
        }
        if (basePath.endsWith("/")) {
            return basePath + "_null_flavour";
        }
        return basePath + "/_null_flavour";
    }

    private String stripTerminalValueNode(final String basePath) {
        if (StringUtils.isBlank(basePath)) {
            return basePath;
        }
        final int lastSlash = basePath.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == basePath.length() - 1) {
            return basePath;
        }
        final String lastSegment = basePath.substring(lastSlash + 1);
        // openEHR flat value nodes like quantity_value/date_time_value/identifier_value
        // are not ELEMENT paths and can't carry null_flavour directly.
        if (lastSegment.endsWith("_value")) {
            return basePath.substring(0, lastSlash);
        }
        return basePath;
    }

    private List<Base> resolveDataAbsentReasonValuesForMissingPath(final Base rootElement, final String originalFhirPath) {
        if (rootElement == null || StringUtils.isBlank(originalFhirPath)) {
            return Collections.emptyList();
        }
        final String normalizedPath = normalizeFhirPathForDataAbsentReason(rootElement, originalFhirPath);
        if (StringUtils.isBlank(normalizedPath)) {
            return Collections.emptyList();
        }
        final List<String> parts = openFhirStringUtils.splitFhirPathTopLevel(normalizedPath).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (parts.isEmpty()) {
            return Collections.emptyList();
        }

        final int valueIndex = findValueSegmentIndex(parts);
        if (valueIndex >= 0) {
            final String containerPath = valueIndex == 0 ? "" : String.join(".", parts.subList(0, valueIndex));
            final List<Base> containers = evaluateContainers(rootElement, containerPath);
            for (Base container : containers) {
                final List<Base> values = resolveDataAbsentReasonValuesFromElement(container);
                if (values != null && !values.isEmpty()) {
                    return values;
                }
            }
        }

        // For non-value mappings, only honor DAR extension on the exact missing target element.
        return resolveDataAbsentReasonExtensionOnExactPath(rootElement, parts);
    }

    private int findValueSegmentIndex(final List<String> parts) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            final String segment = parts.get(i);
            if (isCastSegment(segment) || RESOLVE.equals(segment)) {
                continue;
            }
            if (isValueLikeSegment(segment)) {
                return i;
            }
            return -1;
        }
        return -1;
    }

    private boolean isValueLikeSegment(final String segment) {
        return "value".equals(segment) || "value[x]".equals(segment) || segment.startsWith("value");
    }

    private boolean isCastSegment(final String segment) {
        return segment != null && segment.startsWith("as(") && segment.endsWith(")");
    }

    private List<Base> evaluateContainers(final Base rootElement, final String containerPath) {
        try {
            if (StringUtils.isBlank(containerPath)) {
                return Collections.singletonList(rootElement);
            }
            final List<Base> evaluated = fhirPathR4.evaluate(rootElement, containerPath, Base.class);
            return evaluated == null ? Collections.emptyList() : evaluated;
        } catch (Exception e) {
            log.debug("Unable to evaluate data absent reason container path '{}' on {}: {}",
                    containerPath, rootElement.getClass(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Base> resolveDataAbsentReasonExtensionOnExactPath(final Base rootElement, final List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return Collections.emptyList();
        }
        final String leaf = parts.get(parts.size() - 1);
        if (StringUtils.isBlank(leaf) || isCastSegment(leaf) || RESOLVE.equals(leaf)) {
            return Collections.emptyList();
        }
        final String parentPath = parts.size() == 1 ? "" : String.join(".", parts.subList(0, parts.size() - 1));
        final List<Base> containers = evaluateContainers(rootElement, parentPath);
        if (containers.isEmpty()) {
            return Collections.emptyList();
        }
        for (Base container : containers) {
            try {
                final String extensionPath =
                        leaf + ".extension('" + OpenEhrPopulator.DATA_ABSENT_REASON_URL + "').value";
                final List<Base> extensionValues = fhirPathR4.evaluate(container, extensionPath, Base.class);
                if (extensionValues != null && !extensionValues.isEmpty()) {
                    return extensionValues;
                }
            } catch (Exception e) {
                log.debug("Unable to evaluate data absent reason extension for path '{}' on element {}: {}",
                        leaf, container.getClass(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    private String normalizeFhirPathForDataAbsentReason(final Base rootElement, final String originalFhirPath) {
        String path = originalFhirPath;
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        path = openFhirStringUtils.fixFhirPathCasting(path);
        final List<String> parts = openFhirStringUtils.splitFhirPathTopLevel(path);
        if (!parts.isEmpty() && rootElement.fhirType().equals(parts.get(0))) {
            path = String.join(".", parts.subList(1, parts.size()));
        }
        return path;
    }

    private List<Base> resolveDataAbsentReasonValuesFromElement(final Base element) {
        if (element == null) {
            return Collections.emptyList();
        }
        try {
            final List<Base> extensionValues = fhirPathR4.evaluate(element,
                    "extension('" + OpenEhrPopulator.DATA_ABSENT_REASON_URL + "').value",
                    Base.class);
            if (extensionValues != null && !extensionValues.isEmpty()) {
                return extensionValues;
            }
        } catch (Exception e) {
            log.debug("Unable to evaluate data absent reason extension on element of type {}: {}",
                    element.getClass(), e.getMessage());
        }
        try {
            final List<Base> propertyValues = fhirPathR4.evaluate(element, "dataAbsentReason", Base.class);
            if (propertyValues != null && !propertyValues.isEmpty()) {
                return propertyValues;
            }
        } catch (Exception e) {
            log.debug("Unable to evaluate dataAbsentReason property on element of type {}: {}",
                    element.getClass(), e.getMessage());
        }
        return Collections.emptyList();
    }


    /**
     * Iterates over Helpers (and inner helpers) and evaluates FHIR path throughout the recursion while adding
     * fhir path evaluation results to the flat json structure
     *
     * @param helper helper that is being evaluated at each iteration of the recursion
     * @param flatComposition json object representing composition in a flat format that is being created
     *         throughout
     *         the recursion
     * @param toResolveOn FHIR object where we're evaluating fhir path on
     */
    boolean addDataPoints(final FhirToOpenEhrHelper helper, final JsonObject flatComposition, final Base toResolveOn) {
        List<Base> results;
        final String fhirPath = helper.getFhirPath();
        final List<Base> resolveContainers;
        final String resolveContainerPath;

        // Regular processing for non-mappingCode cases
        if (StringUtils.isEmpty(fhirPath) || FHIR_ROOT_FC.equals(fhirPath)) {
            // just take the one roResolveOn
            log.debug("Taking Base itself as fhirPath is {}", fhirPath);
            results = Collections.singletonList(toResolveOn);
        } else {
            final String fhirPathToEvaluateOn = openFhirStringUtils.fixFhirPathCasting(
                    fhirPath.startsWith(".") ? fhirPath.substring(1) : fhirPath);
            if (StringUtils.isEmpty(fhirPathToEvaluateOn) && fhirPath.startsWith("..")) {
                results = Collections.singletonList(toResolveOn);
            } else {
                results = fhirPathR4.evaluate(toResolveOn,
                        fhirPathToEvaluateOn,
                        Base.class);
            }
            if (fhirPath.endsWith(RESOLVE) && results.isEmpty()) {
                final List<Base> reference = fhirPathR4.evaluate(toResolveOn,
                        fhirPath.replace("." + RESOLVE, ""),
                        Base.class);
                if (!reference.isEmpty()) {
                    results = reference.stream()
                            .filter(ref -> ref instanceof Reference)
                            .map(ref -> (Base) ((Reference) ref).getResource())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
            }
        }
        if (fhirPath != null && fhirPath.contains(RESOLVE)) {
            final String withoutResolve = fhirPath.replace("." + RESOLVE, "");
            final List<String> parts = openFhirStringUtils.splitFhirPathTopLevel(withoutResolve);
            if (parts.size() > 1) {
                resolveContainerPath = String.join(".", parts.subList(0, parts.size() - 1));
                String containerPath = resolveContainerPath;
                if (containerPath.startsWith(".")) {
                    containerPath = containerPath.substring(1);
                }
                if (StringUtils.isEmpty(containerPath)) {
                    resolveContainers = Collections.singletonList(toResolveOn);
                } else {
                    resolveContainers = fhirPathR4.evaluate(toResolveOn, containerPath, Base.class);
                }
            } else {
                resolveContainerPath = null;
                resolveContainers = null;
            }
        } else {
            resolveContainerPath = null;
            resolveContainers = null;
        }
        final boolean missingResults = results == null || results.isEmpty() || resultsRepresentMissingPrimitiveValues(results);
        if (missingResults) {
            final boolean handledNullFlavour = handleDataAbsentReasonWhenNoResult(helper, flatComposition,
                    toResolveOn);
            if (handledNullFlavour) {
                return true;
            }
            log.warn("No results found for FHIRPath {}, evaluating on type: {}", fhirPath,
                    toResolveOn.getClass());
            return false;
        }

        for (int i = 0; i < results.size(); i++) {
            Base result = results.get(i);
            final boolean noMoreRecurringOptions = !helper.getOpenEhrPath().contains(RECURRING_SYNTAX);
            boolean evaluated = true;
            final String thePath = noMoreRecurringOptions ? helper.getOpenEhrPath()
                    : openFhirStringUtils.replaceLastIndexOf(helper.getOpenEhrPath(), RECURRING_SYNTAX, ":" + i);
            log.debug("Setting value taken with fhirPath {} from object type {}", fhirPath,
                    toResolveOn.getClass());

            if (!openEhrConditionEvaluator.checkOpenEhrCondition(helper.getOpenEhrCondition(),
                    flatComposition,
                    helper.getMainOpenEhrPath())) {
                continue;
            }

            // Now check the conditions explicitly as separate steps to see which one's evaluating true
            boolean isHardcodingCondition = StringUtils.isNotEmpty(helper.getHardcodingValue());
            boolean isMappingCodeCondition = helper.getMappingCode() != null;


            // Original logic but with debug outputs
            if (isHardcodingCondition) {
                System.out.println("Entering hardcoding branch");
                log.debug("Hardcoding value {} to path: {}", helper.getHardcodingValue(), thePath);
                // is it ok we use string type here? could it be something else? probably it could be..
                openEhrPopulator.setFhirPathValue(thePath, new StringType(helper.getHardcodingValue()),
                        helper.getOpenEhrType(), flatComposition);
            }
            else if (isMappingCodeCondition) {
                log.info("Using mapping code: {}", helper.getMappingCode());

                try {
                    // Get the plugin manager
                    PluginManager pluginManager = SpringContext.getBean(PluginManager.class);

                    // Get all FormatConverter extensions
                    List<FormatConverter> converters = pluginManager.getExtensions(FormatConverter.class);

                    if (converters.isEmpty()) {
                        log.warn("No FormatConverter extensions found for mapping code: {}", helper.getMappingCode());
                    } else {
                        // Use the first converter for now
                        FormatConverter converter = converters.get(0);

                        // Apply the mapping
                        boolean success = converter.applyFhirToOpenEhrMapping(
                                helper.getMappingCode(),
                                thePath,
                                result,
                                helper.getOpenEhrType(),
                                flatComposition
                        );

                        if (!success) {
                            log.warn("Mapping failed for code: {}", helper.getMappingCode());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error applying mapping: {}", e.getMessage(), e);
                }
            }

            else {
                final boolean handledEventTime = applyEventTypeMappingIfNeeded(helper, result, thePath,
                                                                              flatComposition);
                if (!handledEventTime) {
                    openEhrPopulator.setFhirPathValue(thePath, result, helper.getOpenEhrType(), flatComposition);
                }
            }


            if (helper.getFhirToOpenEhrHelpers() != null) {
                // iterate over inner elements
                for (FhirToOpenEhrHelper fhirToOpenEhrHelper : helper.getFhirToOpenEhrHelpers()) {
                    final Condition openEhrTypeCondition = fhirToOpenEhrHelper.getTypeCondition();
                    if (openEhrTypeCondition != null
                            && !openEhrTypeCondition.getCriteria().equals(fhirToOpenEhrHelper.getOpenEhrType())) {
                        continue;
                    }

                    FhirToOpenEhrHelper copy = fhirToOpenEhrHelper.doClone();

                    // Make sure mappingCode is properly copied
                    copy.setMappingCode(fhirToOpenEhrHelper.getMappingCode());

                    if (copy.getOpenEhrPath().startsWith(helper.getOpenEhrPath())) {
                        final String newOne = copy.getOpenEhrPath().replace(helper.getOpenEhrPath(), thePath);

                        fixAllChildrenRecurringElements(copy, newOne);
                        final String childFhirPath = copy.getFhirPath();
                        if (childFhirPath != null && childFhirPath.startsWith("..")) {
                            if (resolveContainers != null && i < resolveContainers.size()) {
                                String relative = childFhirPath.substring(2);
                                if (relative.startsWith(".")) {
                                    relative = relative.substring(1);
                                }
                                copy.setFhirPath(relative);
                                evaluated = addDataPoints(copy, flatComposition, resolveContainers.get(i));
                            } else {
                                String parentForRelative = helper.getFhirPath();
                                if (parentForRelative != null) {
                                    parentForRelative = parentForRelative.replace("." + OpenFhirStringUtils.RESOLVE + ".",
                                                    ".")
                                            .replace("." + OpenFhirStringUtils.RESOLVE, "");
                                }
                                copy.setFhirPath(openFhirStringUtils.resolveRelativeFhirPath(parentForRelative,
                                        childFhirPath));
                                evaluated = addDataPoints(copy, flatComposition, toResolveOn);
                            }
                        } else {
                            evaluated = addDataPoints(copy, flatComposition, result);
                        }
                    } else {
                        final String childFhirPath = copy.getFhirPath();
                        if (childFhirPath != null && childFhirPath.startsWith("..")) {
                            if (resolveContainers != null && i < resolveContainers.size()) {
                                String relative = childFhirPath.substring(2);
                                if (relative.startsWith(".")) {
                                    relative = relative.substring(1);
                                }
                                copy.setFhirPath(relative);
                                evaluated = addDataPoints(copy, flatComposition, resolveContainers.get(i));
                            } else {
                                String parentForRelative = helper.getFhirPath();
                                if (parentForRelative != null) {
                                    parentForRelative = parentForRelative.replace("." + OpenFhirStringUtils.RESOLVE + ".",
                                                    ".")
                                            .replace("." + OpenFhirStringUtils.RESOLVE, "");
                                }
                                copy.setFhirPath(openFhirStringUtils.resolveRelativeFhirPath(parentForRelative,
                                        childFhirPath));
                                evaluated = addDataPoints(copy, flatComposition, toResolveOn);
                            }
                        } else {
                            evaluated = addDataPoints(copy, flatComposition, result);
                        }
                    }
                }
            }

            // if it was actually evaluated, meaning the proper instance of "result" was taken, then stop this if no mor
            // recurring options; else try next ones
            if (evaluated && noMoreRecurringOptions) {
                break;
            }

        }

        return true;
    }

    private boolean resultsRepresentMissingPrimitiveValues(final List<Base> results) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        return results.stream().allMatch(result -> {
            if (!(result instanceof PrimitiveType<?> primitiveType)) {
                return false;
            }
            return StringUtils.isBlank(primitiveType.getValueAsString());
        });
    }

    /**
     * Adds proper recurring index to all child elements if parent is the recurring one
     */
    void fixAllChildrenRecurringElements(final FhirToOpenEhrHelper helper, final String newOne) {
        final boolean hasParentRecurring = stringUtils.childHasParentRecurring(helper.getOpenEhrPath(), newOne);
        if (hasParentRecurring) {
            final String replaced = stringUtils.replacePattern(helper.getOpenEhrPath(), newOne);
            helper.setOpenEhrPath(replaced);
        }
        if (helper.getFhirToOpenEhrHelpers() == null) {
            return;
        }
        for (FhirToOpenEhrHelper fhirToOpenEhrHelper : helper.getFhirToOpenEhrHelpers()) {
            fixAllChildrenRecurringElements(fhirToOpenEhrHelper, newOne);
        }
    }

    private boolean applyEventTypeMappingIfNeeded(final FhirToOpenEhrHelper helper, final Base result,
                                                  final String openEhrPath,
                                                  final JsonObject flatComposition) {
        if (result == null || openEhrPath == null) {
            return false;
        }
        final String openEhrType = helper == null ? null : helper.getOpenEhrType();
        if (!isEventRmType(openEhrType)) {
            return false;
        }
        if (result instanceof Period) {
            final Period period = (Period) result;
            final Date start = period.getStart();
            final Date end = period.getEnd();
            final Date time = start != null ? start : end;
            if (time != null) {
                openEhrPopulator.setFhirPathValue(openEhrPath + "/time", new DateTimeType(time),
                        FhirConnectConst.DV_DATE_TIME, flatComposition);
            }
            openEhrPopulator.setFhirPathValue(openEhrPath + "/math_function",
                    new Coding("openehr", "640", "actual"),
                    FhirConnectConst.DV_CODED_TEXT, flatComposition);
            if (start != null && end != null) {
                final java.time.Duration duration = java.time.Duration.between(
                        start.toInstant(), end.toInstant());
                if (!duration.isNegative() && !duration.isZero()) {
                    openEhrPopulator.setFhirPathValue(openEhrPath + "/width",
                            new StringType(duration.toString()), FhirConnectConst.DV_DURATION, flatComposition);
                }
            }
            return true;
        } else if (result instanceof DateTimeType
                || result instanceof InstantType
                || result instanceof DateType) {
            openEhrPopulator.setFhirPathValue(openEhrPath + "/time", result,
                    FhirConnectConst.DV_DATE_TIME, flatComposition);
            return true;
        } else if (result instanceof TimeType) {
            openEhrPopulator.setFhirPathValue(openEhrPath + "/time", result,
                    FhirConnectConst.DV_TIME, flatComposition);
            return true;
        }
        return false;
    }

    private boolean isEventRmType(final String rmType) {
        return "EVENT".equals(rmType) || "POINT_EVENT".equals(rmType) || "INTERVAL_EVENT".equals(rmType);
    }


    /**
     * Creates helpers for each individual model mapper defined for the incoming FHIR Resource
     */
    void createHelpers(final String templateId, final Resource resource, final Condition parentCondition,
                       final List<FhirToOpenEhrHelper> helpers, final List<FhirToOpenEhrHelper> coverHelpers,
                       final boolean bundle, final String startingArchetype) {
        ((Bundle) resource).getEntry().forEach(entry -> {
            final List<OpenFhirFhirConnectModelMapper> mapperForResources = openFhirTemplateRepo.getMapperForResource(
                    entry.getResource());
            if (mapperForResources == null || mapperForResources.isEmpty()) {
                return;
            }
            for (OpenFhirFhirConnectModelMapper mapperForResource : mapperForResources) {
                final String mainArchetype = mapperForResource.getOpenEhrConfig().getArchetype();
                String mainArchetypePath;
                if (!mainArchetype.contains("CLUSTER")) {
                    mainArchetypePath = templateId + "/content[" + mainArchetype + "]";
                } else {
                    mainArchetypePath = templateId;
                }
                createHelpers(mainArchetype, mapperForResource, templateId, mainArchetypePath,
                        mapperForResource.getMappings(),
                        parentCondition, helpers, coverHelpers, bundle,
                        mapperForResource.getFhirConfig().getMultiple(), false);
            }
        });

//        final List<OpenFhirFhirConnectModelMapper> mappers = openFhirTemplateRepo.getMapperForArchetype(
//                templateId, startingArchetype);
//        if (mappers == null || mappers.isEmpty()) {
//            return;
//        }
//        for (OpenFhirFhirConnectModelMapper mapperForResource : mappers) {
//            final String mainArchetype = mapperForResource.getOpenEhrConfig().getArchetype();
//            createHelpers(mainArchetype, mapperForResource, templateId, templateId, mapperForResource.getMappings(),
//                          parentCondition, helpers, coverHelpers, bundle,
//                          mapperForResource.getFhirConfig().getMultiple(), false);
//        }
    }

    /**
     * Creates a list of helpers for FHIR to openEHR mappings based on fhir connect model mappers. While creating
     * helpers, it modifies FHIR path and openEHR paths according to fhir connect rules (slot mapping, followed by
     * mapping,
     * reference, ..) and makes sure all child mappings are added to helpers in inner structure (FhirToOpenEhrHelper
     * has a reference to more related FhirToOpenEhrHelpers).
     *
     * @param mainArtifact main archetype this helper is being created for
     * @param fhirConnectMapper fhir connect mapper
     * @param templateId openehr template id
     * @param mainOpenEhrPath main open ehr path (first item in a flat path structure), usually a template ID,
     *         but not necessarily - if we have slot mappings or followed by mappings, this would be the
     *         base flat path being used for context for further child mappings
     * @param mappings mappings being handled in this iteration of the recursion
     * @param parentCondition condition of a parent's element
     * @param helpers a list of helpers being created
     * @param coverHelpers a list of cover helpers being created
     * @param bundle whether a fhir context mapper is expecting a Bundle
     * @param multiple if a specific model mapper should create multiple Resources instead of a single one
     */
    public void createHelpers(final String mainArtifact,
                              final OpenFhirFhirConnectModelMapper fhirConnectMapper,
                              final String templateId,
                              final String mainOpenEhrPath,
                              final List<Mapping> mappings,
                              final Condition parentCondition,
                              final List<FhirToOpenEhrHelper> helpers,
                              final List<FhirToOpenEhrHelper> coverHelpers,
                              final boolean bundle,
                              // todo: remove this if it turns out it's always true with the new contexts mappings
                              final boolean multiple,
                              final boolean possibleRecursion) {
        if (mappings == null) {
            return;
        }
        for (final Mapping mapping : mappings) {

            final With with = mapping.getWith();
            if (with == null || (with.getOpenehr() == null && StringUtils.isNotEmpty(with.getValue()))) {
                // this is hardcoding to FHIR, nothing to do here which is mapping to openEHR
                continue;
            }
            if (mapping.getUnidirectional() != null && FhirConnectConst.UNIDIRECTIONAL_TOFHIR.equals(
                    mapping.getUnidirectional())) {
                // this is unidirectional mapping toFhir only, ignore
                continue;
            }

            if (!isSupportedFhirConditionForToOpenEhr(mapping.getFhirCondition(), mapping)
                    || !isSupportedOpenEhrConditionForToOpenEhr(mapping.getOpenehrCondition(), mapping)) {
                continue;
            }

            // Add null check for with.getOpenehr()
            if (with.getOpenehr() == null) {
                log.warn("Skipping mapping with null openEHR path for FHIR path: {}", with.getFhir());
                continue;
            }

            final FhirToOpenEhrHelper initialHelper = createHelper(mainArtifact, fhirConnectMapper, bundle);
            if (with.getOpenehr().startsWith(FhirConnectConst.OPENEHR_CONTEXT_FC)) {
                continue;
            }

            if (mapping.getOpenehrCondition() != null
                    && FhirConnectConst.CONDITION_OPERATOR_TYPE.equals(mapping.getOpenehrCondition().getOperator())) {
                initialHelper.setTypeCondition(mapping.getOpenehrCondition());
            } else if (mapping.getOpenehrCondition() != null) {
                initialHelper.setOpenEhrCondition(mapping.getOpenehrCondition());
            }

            hardcodingToOpenEhr(mapping, fhirConnectMapper, initialHelper);

            final Condition condition = parentCondition != null ? parentCondition : mapping.getFhirCondition();

            final String fhirPath = openFhirStringUtils.getFhirPathWithConditions(with.getFhir(),
                    condition,
                    fhirConnectMapper.getFhirConfig()
                            .getResource(),
                    null);

            // because it references Resources not directly tied to a Resource itself, i.e. Condition as a cause of death for a Patient
            final boolean needsToBeAddedToParentHelpers = StringUtils.isNotEmpty(fhirPath)
                    && Character.isUpperCase(fhirPath.charAt(0))
                    && !fhirPath.startsWith(fhirConnectMapper.getFhirConfig().getResource());


            if (with.getOpenehr().contains(FhirConnectConst.REFERENCE) && mapping.getReference() != null) {
                createReferenceMapping(mapping, fhirPath, mainArtifact, fhirConnectMapper, templateId, mainOpenEhrPath,
                        parentCondition, helpers, coverHelpers, bundle, multiple, possibleRecursion);
            } else {
                final String openehr = createMainMapping(mapping, fhirConnectMapper, initialHelper, mainOpenEhrPath,
                        fhirPath, multiple,
                        needsToBeAddedToParentHelpers, helpers, coverHelpers,
                        templateId);

                // inner helpers are those that follow a parent one (when mapping is followedBy or slot archetype)
                final List<FhirToOpenEhrHelper> innerHelpers = new ArrayList<>();
                if (mapping.getFollowedBy() != null) {
                    createFollowedByMappings(initialHelper, mapping, openehr, mainOpenEhrPath, fhirPath, multiple,
                            innerHelpers,
                            fhirConnectMapper, mainArtifact, templateId, coverHelpers, bundle,
                            needsToBeAddedToParentHelpers,
                            helpers, possibleRecursion);
                }
                if (mapping.getSlotArchetype() != null) {
                    createSlotMappings(initialHelper, mapping, openehr, mainOpenEhrPath, fhirPath, multiple,
                            innerHelpers,
                            fhirConnectMapper, mainArtifact, templateId, coverHelpers, bundle,
                            needsToBeAddedToParentHelpers,
                            helpers,
                            possibleRecursion);
                }
            }
        }
    }

    private void createFollowedByMappings(final Mapping mapping, final String openehr, final String openEhrPath) {
        for (final Mapping followedByMapping : mapping.getFollowedBy().getMappings()) {
            final With with = followedByMapping.getWith();
            if (with == null || with.getOpenehr() == null && StringUtils.isNotEmpty(with.getValue())) {
                // this is hardcoding to FHIR, nothing to do here which is mapping to openEHR
                continue;
            }
            if (!with.getOpenehr().startsWith(FhirConnectConst.OPENEHR_ARCHETYPE_FC) && !with.getOpenehr()
                    .startsWith(FhirConnectConst.OPENEHR_COMPOSITION_FC)) {
                final String followedByOpenEhrPath = with.getOpenehr();
                final String delimeter = followedByOpenEhrPath.startsWith("|") ? "" : "/";
                with.setOpenehr(
                        openehr.replace(FhirConnectConst.REFERENCE + "/", "") + delimeter + followedByOpenEhrPath
                                .replace(FhirConnectConst.OPENEHR_ARCHETYPE_FC + ".", "")
                                .replace(FhirConnectConst.OPENEHR_ARCHETYPE_FC, ""));
            } else {
                if (with.getOpenehr().equals(FhirConnectConst.OPENEHR_ROOT_FC)) {
                    with.setOpenehr(openehr);
                } else if (with.getOpenehr().equals(FhirConnectConst.OPENEHR_ARCHETYPE_FC)) {
                    with.setOpenehr(openEhrPath);
                }else {
                    // if you prefixed it with $archetype, it means you know what you're setting yourself
                    with.setOpenehr(with.getOpenehr()
                            .replace(FhirConnectConst.REFERENCE + ".", "")
                            .replace(FhirConnectConst.OPENEHR_ARCHETYPE_FC + ".",
                                    openEhrPath + ".")
                            .replace(FhirConnectConst.OPENEHR_ARCHETYPE_FC,
                                    openEhrPath));
                }
            }

        }
    }

    private void createFollowedByMappings(final FhirToOpenEhrHelper initialHelper, final Mapping mapping,
                                          final String openehr, final String mainOpenEhrPath, final String fhirPath,
                                          final boolean multiple, final List<FhirToOpenEhrHelper> innerHelpers,
                                          final OpenFhirFhirConnectModelMapper fhirConnectMapper,
                                          final String mainArtifact, final String templateId,
                                          final List<FhirToOpenEhrHelper> coverHelpers, final boolean bundle,
                                          final boolean needsToBeAddedToParentHelpers,
                                          final List<FhirToOpenEhrHelper> helpers,
                                          final boolean possibleRecursion) {
        final List<Mapping> followedByMappings = mapping.getFollowedBy().getMappings();

        createFollowedByMappings(mapping, openehr, mainOpenEhrPath);

        initialHelper.setOpenEhrPath(openFhirStringUtils.fixOpenEhrPath(openehr, mainOpenEhrPath));
        initialHelper.setFhirPath(openFhirStringUtils.fixFhirPath(fhirPath));

        initialHelper.setMultiple(multiple);

        initialHelper.setFhirToOpenEhrHelpers(innerHelpers);

        fixLimitingCriteriaForInnerCreatedResources(fhirConnectMapper.getFhirConfig().getResource(), initialHelper);

        // recursive call to create helpers for the followedByMappings
        createHelpers(mainArtifact,
                fhirConnectMapper,
                templateId,
                mainOpenEhrPath,
                followedByMappings,
                null,
                innerHelpers,
                coverHelpers,
                bundle,
                multiple,
                possibleRecursion);

        if (needsToBeAddedToParentHelpers) {
            coverHelpers.add(initialHelper);
        } else {
            if (!helpers.contains(initialHelper)) {
                helpers.add(initialHelper);
            }
        }
    }

    private void createSlotMappings(final FhirToOpenEhrHelper initialHelper, final Mapping mapping,
                                    final String openehr, final String mainOpenEhrPath, final String fhirPath,
                                    final boolean multiple, final List<FhirToOpenEhrHelper> innerHelpers,
                                    final OpenFhirFhirConnectModelMapper fhirConnectMapper, final String mainArtifact,
                                    final String templateId,
                                    final List<FhirToOpenEhrHelper> coverHelpers, final boolean bundle,
                                    final boolean needsToBeAddedToParentHelpers,
                                    final List<FhirToOpenEhrHelper> helpers, final boolean breakRecursion) {
        final List<OpenFhirFhirConnectModelMapper> slotArchetypeMapperss = openFhirTemplateRepo.getMapperForArchetype(
                templateId, mapping.getSlotArchetype());
        if (slotArchetypeMapperss == null) {
            log.warn("Couldn't find referenced slot archetype mapper {}. Referenced in {}. Aborting mapping.", mapping.getSlotArchetype(),
                    mapping.getName());
            return;
        }

        for (OpenFhirFhirConnectModelMapper slotArchetypeMappers : slotArchetypeMapperss) {
            boolean possibleRecursion = slotArchetypeMappers.getName().equals(fhirConnectMapper.getName());
            if (breakRecursion) {
                log.warn("Breaking possible infinite recursion with mapping: {}", slotArchetypeMappers.getName());
                break;
            }

            final String openEhrFixed = openehr.replace("/" + FhirConnectConst.REFERENCE, "");

            openFhirMapperUtils.prepareForwardingSlotArchetypeMapperNoFhirPrefix(slotArchetypeMappers,
                    fhirConnectMapper, fhirPath,
                    openEhrFixed);

            initialHelper.setFhirToOpenEhrHelpers(innerHelpers);

            initialHelper.setOpenEhrPath(openFhirStringUtils.fixOpenEhrPath(openehr, mainOpenEhrPath));
            initialHelper.setFhirPath(openFhirStringUtils.fixFhirPath(fhirPath));

            fixLimitingCriteriaForInnerCreatedResources(fhirConnectMapper.getFhirConfig().getResource(), initialHelper);

            // recursive call to create helpers for the slot archetype mappings
            createHelpers(mainArtifact,
                    slotArchetypeMappers,
                    templateId, // templateId
                    openEhrFixed, // templateId
                    slotArchetypeMappers.getMappings(),
                    null,
                    innerHelpers,
                    coverHelpers,
                    bundle,
                    multiple,
                    possibleRecursion);
            if (needsToBeAddedToParentHelpers) {
                coverHelpers.add(initialHelper);
            } else {
                if (!helpers.contains(initialHelper)) {
                    helpers.add(initialHelper);
                }
            }
        }
    }

    private void hardcodingToOpenEhr(final Mapping mapping, final OpenFhirFhirConnectModelMapper fhirConnectMapper,
                                     final FhirToOpenEhrHelper initialHelper) {
        if (mapping.getWith().getFhir() == null) {
            // is hardcoding
            if (mapping.getFhirCondition() != null) {
                mapping.getWith().setFhir(FHIR_ROOT_FC + mapping.getFhirCondition().getTargetRoot());
            } else {
                mapping.getWith().setFhir(FHIR_ROOT_FC);
            }
            initialHelper.setHardcodingValue(mapping.getWith().getValue());
        }
    }

    private String createMainMapping(final Mapping mapping, final OpenFhirFhirConnectModelMapper fhirConnectMapper,
                                     final FhirToOpenEhrHelper initialHelper,
                                     final String mainOpenEhrPath, final String fhirPath, final boolean multiple,
                                     final boolean needsToBeAddedToParentHelpers,
                                     final List<FhirToOpenEhrHelper> helpers,
                                     final List<FhirToOpenEhrHelper> coverHelpers,
                                     final String templateId) {
        String openehr = stringUtils.prepareOpenEhrSyntax(mapping.getWith().getOpenehr(), mainOpenEhrPath);
        if (mapping.getWith().getType() == null) {
            // when type is not explicitly defined in the fhir connect model mapper, we assume a string
            if (openFhirStringUtils.endsWithOpenEhrType(openehr) != null) {
                openehr = stringUtils.replaceLastIndexOf(openehr, "/", "|");
            }
        } else {
            initialHelper.setOpenEhrType(mapping.getWith().getType());
        }

        if (!OPENEHR_TYPE_NONE.equals(mapping.getWith().getType())) {
            initialHelper.setOpenEhrPath(
                    openehr.replace(FhirConnectConst.OPENEHR_ARCHETYPE_FC + "/", mainOpenEhrPath + "/"));
            initialHelper.setOpenEhrPath(
                    openehr.replace(FhirConnectConst.OPENEHR_COMPOSITION_FC, templateId));
            initialHelper.setMainOpenEhrPath(mainOpenEhrPath);
            final String replacedFhirRoot = fhirPath.replace("." + FHIR_ROOT_FC, "")
                    .replace(FHIR_ROOT_FC, "");
            initialHelper.setFhirPath(replacedFhirRoot);
            if (mapping.getMappingCode() != null) {
                initialHelper.setMappingCode(mapping.getMappingCode());
            }
            initialHelper.setMultiple(multiple);
            fixLimitingCriteriaForInnerCreatedResources(fhirConnectMapper.getFhirConfig().getResource(), initialHelper);
            if (needsToBeAddedToParentHelpers) {
                coverHelpers.add(initialHelper);
            } else {
                helpers.add(initialHelper);
            }
        }
        return openehr;
    }

    private void createReferenceMapping(final Mapping mapping, final String fhirPath, final String mainArtifact,
                                        final OpenFhirFhirConnectModelMapper fhirConnectMapper,
                                        final String templateId, final String mainOpenEhrPath,
                                        final Condition parentCondition,
                                        final List<FhirToOpenEhrHelper> helpers,
                                        final List<FhirToOpenEhrHelper> coverHelpers,
                                        final boolean bundle, final boolean multiple, final boolean possibleRecursion) {
        // a reference mapping; prepare 'reference' mappings
        final List<Mapping> referencedMapping = mapping.getReference().getMappings();
        openFhirMapperUtils.prepareReferencedMappings(fhirPath, mapping.getWith().getOpenehr(), referencedMapping,
                mainOpenEhrPath);

        // recursively call createHelpers after reference mappings have been prepared
        createHelpers(mainArtifact, fhirConnectMapper, templateId, mainOpenEhrPath, referencedMapping, parentCondition,
                helpers, coverHelpers, bundle, multiple, possibleRecursion);
    }

    private boolean isSupportedFhirConditionForToOpenEhr(final Condition condition, final Mapping mapping) {
        if (condition == null || condition.getOperator() == null) {
            return true;
        }
        final String operator = condition.getOperator();
        final boolean supported = FhirConnectConst.CONDITION_OPERATOR_ONE_OF.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_NOT_OF.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_EMPTY.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_NOT_EMPTY.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_TYPE.equals(operator);
        if (!supported) {
            log.warn("Unsupported FHIR condition operator '{}' for mapping '{}', skipping mapping.",
                    operator, mapping == null ? null : mapping.getName());
        }
        return supported;
    }

    private boolean isSupportedOpenEhrConditionForToOpenEhr(final Condition condition, final Mapping mapping) {
        if (condition == null || condition.getOperator() == null) {
            return true;
        }
        final String operator = condition.getOperator();
        final boolean supported = FhirConnectConst.CONDITION_OPERATOR_EMPTY.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_NOT_EMPTY.equals(operator)
                || FhirConnectConst.CONDITION_OPERATOR_TYPE.equals(operator);
        if (!supported) {
            log.warn("Unsupported openEHR condition operator '{}' for mapping '{}', skipping mapping.",
                    operator, mapping == null ? null : mapping.getName());
        }
        return supported;
    }

    /**
     * InnerCreatedResources are those that are not directly being mapped by a model mapper but indirectly because
     * the main Resource is pointing to them.
     * <p>
     * When fhirPath doesn't start with the main target Resource, limitingCriteria needs to be adjusted as well.
     * For example, when you're mapping Condition that's referenced by the main Resource (Patient), limiting criteria
     * is pointing to a patient but would have to be pointing to a Condition
     *
     * @param targetResource main Resource being mapped
     * @param helperToFix helper where limiting criteria needs to be adjusted
     */
    private void fixLimitingCriteriaForInnerCreatedResources(final String targetResource,
                                                             final FhirToOpenEhrHelper helperToFix) {
        final String fhirPath = helperToFix.getFhirPath();
        if (StringUtils.isEmpty(fhirPath)) {
            return;
        }
        if (!fhirPath.startsWith(targetResource) && Character.isUpperCase(fhirPath.charAt(0))) {
            helperToFix.setLimitingCriteria(
                    helperToFix.getLimitingCriteria().replace(targetResource, fhirPath.split("\\.")[0]));
        }
    }

    /**
     * Creates base FhirToOpenEhrHelper POJO that's later on adjusted in the further processing of these helpers. This
     * method
     * only instantiates it and adds generic things to it, such as 'archetype' and 'limitingCriteria' that's
     * constructed
     * based on the header condition of a model mapper.
     *
     * @param mainArtifact main artifact being mapped
     * @param mapperForResource fhir connect model mapper
     * @param bundle if we're mapping to a Bundle (if FhirConnect.context.resourceType is Bundle)
     * @return instantiated FhirToOpenEhrHelper
     */
    private FhirToOpenEhrHelper createHelper(final String mainArtifact,
                                             final OpenFhirFhirConnectModelMapper mapperForResource,
                                             final boolean bundle) {
        final OpenFhirFhirConfig fhirConfig = mapperForResource.getFhirConfig();

        return FhirToOpenEhrHelper.builder()
                .archetype(mainArtifact)
                .limitingCriteria(getLimitingCriteria(fhirConfig, bundle))
                .build();
    }

    /**
     * Creates limiting criteria based on the FhirConnect FhirConfig Condition element.
     */
    private String getLimitingCriteria(final OpenFhirFhirConfig fhirConfig, final boolean bundle) {
        if (fhirConfig == null) {
            return null;
        }
        final String limitingCriteria;
        if (fhirConfig.getCondition() != null) {
            final String existingFhirPath = stringUtils.amendFhirPath(FhirConnectConst.FHIR_RESOURCE_FC,
                    fhirConfig.getCondition(),
                    fhirConfig.getResource());
            if (bundle && existingFhirPath.startsWith(fhirConfig.getResource())) {
                final String withoutResourceType = existingFhirPath.replace(fhirConfig.getResource() + ".", "");
                limitingCriteria = String.format("Bundle.entry.resource.ofType(%s).where(%s)", fhirConfig.getResource(),
                        withoutResourceType);
            } else {
                limitingCriteria = stringUtils.amendFhirPath(FhirConnectConst.FHIR_RESOURCE_FC,
                        fhirConfig.getCondition(), fhirConfig.getResource());
            }
        } else {
            limitingCriteria = String.format("Bundle.entry.resource.ofType(%s)", fhirConfig.getResource());
        }
        return limitingCriteria;
    }
}
