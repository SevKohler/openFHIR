package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.medblocks.openfhir.fc.FhirConnectConst.*;

public class ValueToFHIRParser {

    private final OpenFhirStringUtils openFhirStringUtils;
    private final FhirDatatypeResolver datatypeResolver;

    private final TemporalParser temporalParser;
    private final QuantityParser quantityParser;
    private final CodedParser codedParser;
    private final MediaParser mediaParser;
    private final TextParser textParser;
    private final IdentifierParser identifierParser;

    public ValueToFHIRParser(OpenFhirStringUtils openFhirStringUtils,
                             OpenFhirMapperUtils mapperUtils) {
        this.openFhirStringUtils = Objects.requireNonNull(openFhirStringUtils);

        this.datatypeResolver = new FhirDatatypeResolver(); // wrapper around static if you want
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);

        this.temporalParser = new TemporalParser(readers);
        this.quantityParser = new QuantityParser(readers);
        this.codedParser = new CodedParser(readers);
        this.mediaParser = new MediaParser(readers);
        this.textParser = new TextParser(readers);
        this.identifierParser = new IdentifierParser(readers);
    }

    public OpenEhrToFhirHelper.DataWithIndex parse(List<String> joinedValues,
                                                   String targetType,
                                                   JsonObject valueHolder,
                                                   boolean canBeNull,
                                                   String resourceType,
                                                   String fhirPath) {
        if (joinedValues == null || joinedValues.isEmpty()) return null;

        Optional<Class<?>> fhirAttrType = datatypeResolver.resolve(resourceType, fhirPath);

        String path = joinedValues.get(0);
        Integer lastIndex = openFhirStringUtils.getLastIndex(path);

        return switch (targetType) {
            case DV_DATE_TIME, "DATETIME" -> temporalParser.dateTime(valueHolder, lastIndex, path);
            case DV_TIME, "TIME" -> temporalParser.time(valueHolder, lastIndex, path);
            case DV_BOOL, "BOOL" -> temporalParser.bool(valueHolder, lastIndex, path);
            case DV_DATE, "DATE" -> temporalParser.date(valueHolder, lastIndex, path);

            case DV_PROPORTION, "PROPORTION" -> quantityParser.proportion(joinedValues, valueHolder, lastIndex, path);
            case DV_QUANTITY, "QUANTITY" -> quantityParser.quantity(joinedValues, valueHolder, lastIndex, path);
            case DV_COUNT -> quantityParser.count(valueHolder, lastIndex, path);

            case FhirConnectConst.DV_CODED_TEXT, FhirConnectConst.DV_ORDINAL, "CODEABLECONCEPT" ->
                    codedParser.codeableConcept(joinedValues, valueHolder, lastIndex, path, fhirAttrType);

            case CODE_PHRASE, "CODING" -> codedParser.coding(joinedValues, valueHolder, lastIndex, path);

            case DV_MULTIMEDIA, "MEDIA" -> mediaParser.attachment(valueHolder, lastIndex, path);

            case FhirConnectConst.DV_TEXT, "STRING", "TEXT" -> textParser.string(valueHolder, lastIndex, path, canBeNull);

            case FhirConnectConst.DV_IDENTIFIER, "IDENTIFIER" -> identifierParser.identifier(joinedValues, valueHolder, lastIndex, path);

            default -> textParser.string(valueHolder, lastIndex, path, canBeNull);
        };
    }
}
