package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StringType;

public class TextParser {

    private final FhirValueReaders fhirValueReaders;

    public TextParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex string(JsonObject valueHolder,
                                                    Integer lastIndex,
                                                    String path,
                                                    boolean canBeNull) {

        String v = fhirValueReaders.get(valueHolder, path);
        if (StringUtils.isNotEmpty(v)) {
            return new OpenEhrToFhirHelper.DataWithIndex(new StringType(v), lastIndex, path);
        }
        if (canBeNull) return null;
        return new OpenEhrToFhirHelper.DataWithIndex(new StringType(), lastIndex, path);
    }
}
