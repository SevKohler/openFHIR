package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StringType;

public class TextParser {

    private final FhirValueReaders r;

    public TextParser(FhirValueReaders readers) {
        this.r = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex string(JsonObject valueHolder,
                                                    Integer lastIndex,
                                                    String path,
                                                    boolean canBeNull) {
        String base = r.basePath(path);

        String v = r.get(valueHolder, base);
        if (StringUtils.isNotEmpty(v)) {
            return new OpenEhrToFhirHelper.DataWithIndex(new StringType(v), lastIndex, base);
        }
        if (canBeNull) return null;
        return new OpenEhrToFhirHelper.DataWithIndex(new StringType(), lastIndex, base);
    }
}
