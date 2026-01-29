package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.hl7.fhir.r4.model.Attachment;

import java.nio.charset.StandardCharsets;

public class MediaParser {

    private final FhirValueReaders r;

    public MediaParser(FhirValueReaders readers) {
        this.r = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex attachment(JsonObject valueHolder, Integer lastIndex, String path) {
        String base = r.basePath(path);

        Attachment att = new Attachment();
        att.setContentType(r.get(valueHolder, base + "|mediatype"));

        String size = r.get(valueHolder, base + "|size");
        if (size != null) att.setSize(Integer.parseInt(size));

        att.setUrl(r.get(valueHolder, base + "|url"));

        String dataBytes = r.get(valueHolder, base + "|data");
        att.setData(dataBytes == null ? null : dataBytes.getBytes(StandardCharsets.UTF_8));

        return new OpenEhrToFhirHelper.DataWithIndex(att, lastIndex, base);
    }
}
