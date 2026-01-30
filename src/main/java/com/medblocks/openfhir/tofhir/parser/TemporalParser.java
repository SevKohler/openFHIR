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
        DateTimeType dt = new DateTimeType();
        dt.setValue(fhirValueReaders.date(fhirValueReaders.get(valueHolder, path)));
        return new OpenEhrToFhirHelper.DataWithIndex(dt, lastIndex, path);
    }

    public OpenEhrToFhirHelper.DataWithIndex time(JsonObject valueHolder, Integer lastIndex, String path) {
        TimeType t = new TimeType();
        t.setValue(fhirValueReaders.get(valueHolder, path));
        return new OpenEhrToFhirHelper.DataWithIndex(t, lastIndex, path);
    }

    public OpenEhrToFhirHelper.DataWithIndex bool(JsonObject valueHolder, Integer lastIndex, String path) {
        BooleanType b = new BooleanType();
        b.setValue(Boolean.valueOf(fhirValueReaders.get(valueHolder, path)));
        return new OpenEhrToFhirHelper.DataWithIndex(b, lastIndex, path);
    }

    public OpenEhrToFhirHelper.DataWithIndex date(JsonObject valueHolder, Integer lastIndex, String path) {
        DateType d = new DateType();
        d.setValue(fhirValueReaders.date(fhirValueReaders.get(valueHolder, path)));
        return new OpenEhrToFhirHelper.DataWithIndex(d, lastIndex, path);
    }
}
