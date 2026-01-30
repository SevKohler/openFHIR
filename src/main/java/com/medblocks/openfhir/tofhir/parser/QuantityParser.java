package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Quantity;

import java.util.List;

public class QuantityParser {

    private final FhirValueReaders fhirValueReaders;

    public QuantityParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex count(JsonObject valueHolder, Integer lastIndex, String path) {
        return new OpenEhrToFhirHelper.DataWithIndex(new IntegerType(fhirValueReaders.get(valueHolder, path)), lastIndex, path);
    }

    public OpenEhrToFhirHelper.DataWithIndex proportion(List<String> joinedValues,
                                                        JsonObject valueHolder,
                                                        Integer lastIndex,
                                                        String path) {

        String numeratorPath = path + "|numerator";
        String denominatorPath = path + "|denominator";

        Quantity q = new Quantity();

        String denom = fhirValueReaders.get(valueHolder, denominatorPath);
        if ("100.0".equals(denom)) {
            q.setCode("%");
            q.setUnit("percent");
            q.setSystem("http://unitsofmeasure.org");
        }

        Object numVal = fhirValueReaders.number(fhirValueReaders.get(valueHolder, numeratorPath));
        if (numVal instanceof Long l) q.setValue(l);
        if (numVal instanceof Double d) q.setValue(d);

        return new OpenEhrToFhirHelper.DataWithIndex(q, lastIndex, path);
    }

    public OpenEhrToFhirHelper.DataWithIndex quantity(List<String> joinedValues,
                                                      JsonObject valueHolder,
                                                      Integer lastIndex,
                                                      String path) {

        String magnitudePath = find(joinedValues, "|magnitude");
        String unitPath = find(joinedValues, "|unit");
        String codePath = find(joinedValues, "|code");
        String valuePath = find(joinedValues, "|value");
        String ordinalPath = find(joinedValues, "|ordinal");

        Quantity q = new Quantity();

        setQuantityValue(valueHolder, q, magnitudePath, ordinalPath);

        if (unitPath != null) q.setUnit(fhirValueReaders.get(valueHolder, unitPath));
        if (valuePath != null) q.setUnit(fhirValueReaders.get(valueHolder, valuePath)); // preserves your existing behavior
        if (codePath != null) q.setCode(fhirValueReaders.get(valueHolder, codePath));

        // fallback if no extra fields are present
        if (magnitudePath == null && ordinalPath == null && unitPath == null && valuePath == null && codePath == null) {
            Object n = fhirValueReaders.number(fhirValueReaders.get(valueHolder, path));
            if (n instanceof Long l) q.setValue(l);
            if (n instanceof Double d) q.setValue(d);
        }

        return new OpenEhrToFhirHelper.DataWithIndex(q, lastIndex, path);
    }

    private void setQuantityValue(JsonObject valueHolder, Quantity q, String magnitudePath, String ordinalPath) {
        if (magnitudePath != null) {
            Object n = fhirValueReaders.number(fhirValueReaders.get(valueHolder, magnitudePath));
            if (n instanceof Long l) q.setValue(l);
            if (n instanceof Double d) q.setValue(d);
            return;
        }
        if (ordinalPath != null) {
            Object n = fhirValueReaders.number(fhirValueReaders.get(valueHolder, ordinalPath));
            if (n instanceof Long l) q.setValue(l);
            if (n instanceof Double d) q.setValue(d);
        }
    }

    private String find(List<String> joinedValues, String suffix) {
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }
}
