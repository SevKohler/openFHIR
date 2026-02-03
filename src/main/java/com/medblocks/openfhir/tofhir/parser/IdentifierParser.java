package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;

import java.net.URI;
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
        String issuerPath = find(joinedValues, "|issuer");
        String resolvedIssuerPath = StringUtils.isEmpty(issuerPath) ? (path + "/identifier_value|issuer") : issuerPath;
        String system = normalizeIdentifierSystem(fhirValueReaders.get(valueHolder, resolvedIssuerPath));
        identifier.setValue(fhirValueReaders.get(valueHolder, resolvedIdPath));
        identifier.setSystem(system);

        return new OpenEhrToFhirHelper.DataWithIndex(identifier, lastIndex, path);
    }

    private String find(List<String> joinedValues, String suffix) {
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }

    private String normalizeIdentifierSystem(String system) {
        if (StringUtils.isBlank(system)) {
            return system;
        }
        if (system.startsWith("http://openehr.org/identifier")) {
            return system;
        }
        if (isUri(system)) {
            return system;
        }
        String trimmed = system;
        return "http://openehr.org/identifier/" + trimmed;
    }

    private boolean isUri(String value) {
        try {
            return new URI(value).isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }
}
