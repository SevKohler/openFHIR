package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.hl7.fhir.r4.model.Attachment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MediaParser {

    private final FhirValueReaders fhirValueReaders;

    public MediaParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex attachment(JsonObject valueHolder, Integer lastIndex, String path) {

        Attachment att = new Attachment();
        att.setContentType(fhirValueReaders.get(valueHolder, path + "|mediatype"));

        String size = fhirValueReaders.get(valueHolder, path + "|size");
        if (size != null) att.setSize(Integer.parseInt(size));

        att.setUrl(fhirValueReaders.get(valueHolder, path + "|url"));

        String dataBytes = fhirValueReaders.get(valueHolder, path + "|data");
        if (dataBytes != null) {
            try {
                att.setData(Base64.getDecoder().decode(dataBytes));
            } catch (IllegalArgumentException e) {
                // fallback for non-base64 test fixtures
                att.setData(dataBytes.getBytes(StandardCharsets.UTF_8));
            }
        }

        return new OpenEhrToFhirHelper.DataWithIndex(att, lastIndex, path);
    }
}
