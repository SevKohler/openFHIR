package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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

    public OpenEhrToFhirHelper.DataWithIndex range(List<String> joinedValues,
                                                   JsonObject valueHolder,
                                                   Integer lastIndex,
                                                   String path) {
        Quantity low = parseIntervalQuantity(joinedValues, valueHolder, "lower");
        Quantity high = parseIntervalQuantity(joinedValues, valueHolder, "upper");

        if (low == null && high == null) {
            return null;
        }

        Range range = new Range();
        if (low != null) {
            range.setLow(low);
        }
        if (high != null) {
            range.setHigh(high);
        }
        return new OpenEhrToFhirHelper.DataWithIndex(range, lastIndex, path);
    }

    private Quantity parseIntervalQuantity(List<String> joinedValues, JsonObject valueHolder, String side) {
        String magnitudePath = find(joinedValues, side + "|magnitude");
        String unitPath = find(joinedValues, side + "|unit");
        String codePath = find(joinedValues, side + "|code");
        String valuePath = find(joinedValues, side + "|value");

        Quantity q = new Quantity();
        boolean populated = false;

        if (magnitudePath != null) {
            Object n = fhirValueReaders.number(fhirValueReaders.get(valueHolder, magnitudePath));
            if (n instanceof Long l) {
                q.setValue(l);
                populated = true;
            }
            if (n instanceof Double d) {
                q.setValue(d);
                populated = true;
            }
        }
        if (unitPath != null) {
            String unit = fhirValueReaders.get(valueHolder, unitPath);
            if (StringUtils.isNotBlank(unit)) {
                q.setUnit(unit);
                populated = true;
            }
        }
        if (valuePath != null) {
            String unit = fhirValueReaders.get(valueHolder, valuePath);
            if (StringUtils.isNotBlank(unit)) {
                q.setUnit(unit);
                populated = true;
            }
        }
        if (codePath != null) {
            String code = fhirValueReaders.get(valueHolder, codePath);
            if (StringUtils.isNotBlank(code)) {
                q.setCode(code);
                populated = true;
            }
        }

        return populated ? q : null;
    }

    private String find(final List<String> joinedValues, final String suffix) {
        if (joinedValues == null) {
            return null;
        }
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }
}
