package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import java.util.List;
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

    public OpenEhrToFhirHelper.DataWithIndex interval(List<String> joinedValues,
                                                      JsonObject valueHolder,
                                                      Integer lastIndex,
                                                      String path) {
        String lowerPath = find(joinedValues, "lower|value");
        String upperPath = find(joinedValues, "upper|value");
        Period period = new Period();
        boolean populated = false;
        if (lowerPath != null) {
            period.setStart(fhirValueReaders.date(fhirValueReaders.get(valueHolder, lowerPath)));
            populated = period.getStart() != null;
        }
        if (upperPath != null) {
            period.setEnd(fhirValueReaders.date(fhirValueReaders.get(valueHolder, upperPath)));
            populated = populated || period.getEnd() != null;
        }
        if (!populated) {
            return null;
        }
        return new OpenEhrToFhirHelper.DataWithIndex(period, lastIndex, path);
    }

    private String find(final List<String> joinedValues, final String suffix) {
        if (joinedValues == null) {
            return null;
        }
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }
}
