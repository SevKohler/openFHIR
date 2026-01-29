package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.hl7.fhir.r4.model.*;

public class TemporalParser {

    private final FhirValueReaders fhirValueReaders;

    public TemporalParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex dateTime(JsonObject valueHolder, Integer lastIndex, String path) {
        String base = fhirValueReaders.basePath(path);
        DateTimeType dt = new DateTimeType();
        dt.setValue(fhirValueReaders.date(fhirValueReaders.get(valueHolder, base)));
        return new OpenEhrToFhirHelper.DataWithIndex(dt, lastIndex, base);
    }

    public OpenEhrToFhirHelper.DataWithIndex time(JsonObject valueHolder, Integer lastIndex, String path) {
        String base = fhirValueReaders.basePath(path);
        TimeType t = new TimeType();
        t.setValue(fhirValueReaders.get(valueHolder, base));
        return new OpenEhrToFhirHelper.DataWithIndex(t, lastIndex, base);
    }

    public OpenEhrToFhirHelper.DataWithIndex bool(JsonObject valueHolder, Integer lastIndex, String path) {
        String base = fhirValueReaders.basePath(path);
        BooleanType b = new BooleanType();
        b.setValue(Boolean.valueOf(fhirValueReaders.get(valueHolder, base)));
        return new OpenEhrToFhirHelper.DataWithIndex(b, lastIndex, base);
    }

    public OpenEhrToFhirHelper.DataWithIndex date(JsonObject valueHolder, Integer lastIndex, String path) {
        String base = fhirValueReaders.basePath(path);
        DateType d = new DateType();
        d.setValue(fhirValueReaders.date(fhirValueReaders.get(valueHolder, base)));
        return new OpenEhrToFhirHelper.DataWithIndex(d, lastIndex, base);
    }
}
