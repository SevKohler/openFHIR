package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;

import java.util.List;

public class IdentifierParser {

    private final FhirValueReaders fhirValueReaders;

    public IdentifierParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex identifier(List<String> joinedValues,
                                                        JsonObject valueHolder,
                                                        Integer lastIndex,
                                                        String path) {
        // try to find an explicit "|id" path from joinedValues
        String idPath = find(joinedValues, "|id");

        Identifier identifier = new Identifier();
        String resolvedIdPath = StringUtils.isEmpty(idPath) ? (path + "/identifier_value|id") : idPath;
        identifier.setValue(fhirValueReaders.get(valueHolder, resolvedIdPath));

        return new OpenEhrToFhirHelper.DataWithIndex(identifier, lastIndex, path);
    }

    private String find(List<String> joinedValues, String suffix) {
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }
}
