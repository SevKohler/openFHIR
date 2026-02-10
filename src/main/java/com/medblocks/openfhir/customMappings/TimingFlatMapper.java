package com.medblocks.openfhir.customMappings;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.parser.FhirValueReaders;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

final class TimingFlatMapper {

    private TimingFlatMapper() {}

    enum FrequencyFormat {
        DIRECT,
        QUANTITY_VALUE,
        INTERVAL_QUANTITY_VALUE
    }

    enum DurationFormat {
        DIRECT,
        DURATION_VALUE
    }

    static FrequencyFormat detectFrequencyFormat(JsonObject flat, String basePath) {
        if (flat == null || StringUtils.isBlank(basePath)) {
            return FrequencyFormat.DIRECT;
        }
        if (flat.has(basePath + "/interval<dv_quantity>_value|magnitude")
                || flat.has(basePath + "/interval<dv_quantity>_value/lower|magnitude")
                || flat.has(basePath + "/interval<dv_quantity>_value/upper|magnitude")) {
            return FrequencyFormat.INTERVAL_QUANTITY_VALUE;
        }
        if (flat.has(basePath + "/quantity_value|magnitude")
                || flat.has(basePath + "/quantity_value/lower|magnitude")
                || flat.has(basePath + "/quantity_value/upper|magnitude")) {
            return FrequencyFormat.QUANTITY_VALUE;
        }
        return FrequencyFormat.DIRECT;
    }

    static DurationFormat detectDurationFormat(JsonObject flat, String basePath) {
        if (flat == null || StringUtils.isBlank(basePath)) {
            return DurationFormat.DIRECT;
        }
        if (flat.has(basePath + "/duration_value|value")
                || flat.has(basePath + "/duration_value/lower|value")
                || flat.has(basePath + "/duration_value/upper|value")) {
            return DurationFormat.DURATION_VALUE;
        }
        return DurationFormat.DIRECT;
    }

    static void writeFrequency(JsonObject flat, String basePath, Double frequency, Double frequencyMax, String unit) {
        if (flat == null || StringUtils.isBlank(basePath) || frequency == null) {
            return;
        }
        FrequencyFormat format = detectFrequencyFormat(flat, basePath);
        if (format == FrequencyFormat.INTERVAL_QUANTITY_VALUE || format == FrequencyFormat.QUANTITY_VALUE) {
            String prefix = format == FrequencyFormat.INTERVAL_QUANTITY_VALUE
                    ? basePath + "/interval<dv_quantity>_value"
                    : basePath + "/quantity_value";
            if (frequencyMax != null && !frequencyMax.equals(frequency)) {
                set(flat, prefix + "/lower|magnitude", frequency);
                set(flat, prefix + "/upper|magnitude", frequencyMax);
                if (StringUtils.isNotBlank(unit)) {
                    set(flat, prefix + "/lower|unit", unit);
                    set(flat, prefix + "/upper|unit", unit);
                }
            } else {
                set(flat, prefix + "|magnitude", frequency);
                if (StringUtils.isNotBlank(unit)) {
                    set(flat, prefix + "|unit", unit);
                }
            }
            return;
        }
        set(flat, basePath + "|magnitude", frequency);
        if (StringUtils.isNotBlank(unit)) {
            set(flat, basePath + "|unit", unit);
        }
    }

    static void writePeriodDuration(JsonObject flat, String basePath, String duration, String durationMax) {
        if (flat == null || StringUtils.isBlank(basePath) || StringUtils.isBlank(duration)) {
            return;
        }
        DurationFormat format = detectDurationFormat(flat, basePath);
        if (StringUtils.isNotBlank(durationMax) && !durationMax.equals(duration)) {
            if (format == DurationFormat.DURATION_VALUE) {
                set(flat, basePath + "/duration_value/lower|value", duration);
                set(flat, basePath + "/duration_value/upper|value", durationMax);
            } else {
                set(flat, basePath + "/lower|value", duration);
                set(flat, basePath + "/upper|value", durationMax);
            }
            return;
        }
        if (format == DurationFormat.DURATION_VALUE) {
            set(flat, basePath + "/duration_value|value", duration);
        } else {
            set(flat, basePath + "|value", duration);
        }
    }

    static FrequencyValue readFrequency(JsonObject valueHolder, List<String> joinedValues, FhirValueReaders readers) {
        if (valueHolder == null || joinedValues == null || readers == null) {
            return null;
        }
        String lowerMag = find(joinedValues, "interval<dv_quantity>_value/lower|magnitude");
        if (lowerMag == null) lowerMag = find(joinedValues, "lower|magnitude");
        String upperMag = find(joinedValues, "interval<dv_quantity>_value/upper|magnitude");
        if (upperMag == null) upperMag = find(joinedValues, "upper|magnitude");
        String mag = find(joinedValues, "interval<dv_quantity>_value|magnitude");
        if (mag == null) mag = find(joinedValues, "magnitude");
        String unit = find(joinedValues, "interval<dv_quantity>_value/lower|unit");
        if (unit == null) unit = find(joinedValues, "upper|unit");
        if (unit == null) unit = find(joinedValues, "unit");

        Double lower = null;
        Double upper = null;
        Double single = null;

        if (lowerMag != null) {
            Object n = readers.number(readers.get(valueHolder, lowerMag));
            if (n instanceof Number num) {
                lower = num.doubleValue();
            }
        }
        if (upperMag != null) {
            Object n = readers.number(readers.get(valueHolder, upperMag));
            if (n instanceof Number num) {
                upper = num.doubleValue();
            }
        }
        if (mag != null) {
            Object n = readers.number(readers.get(valueHolder, mag));
            if (n instanceof Number num) {
                single = num.doubleValue();
            }
        }
        String unitVal = unit != null ? readers.get(valueHolder, unit) : null;

        if (lower == null && upper == null && single == null) {
            return null;
        }
        return new FrequencyValue(single != null ? single : lower, upper, unitVal);
    }

    static DurationValue readDuration(JsonObject valueHolder, List<String> joinedValues, FhirValueReaders readers) {
        if (valueHolder == null || joinedValues == null || readers == null) {
            return null;
        }
        String lowerVal = find(joinedValues, "lower|value");
        String upperVal = find(joinedValues, "upper|value");
        String value = find(joinedValues, "value");

        String lower = lowerVal != null ? readers.get(valueHolder, lowerVal) : null;
        String upper = upperVal != null ? readers.get(valueHolder, upperVal) : null;
        String single = value != null ? readers.get(valueHolder, value) : null;

        if (StringUtils.isBlank(lower) && StringUtils.isBlank(upper) && StringUtils.isBlank(single)) {
            return null;
        }
        String start = StringUtils.isNotBlank(single) ? single : lower;
        return new DurationValue(start, upper);
    }

    private static String find(List<String> joinedValues, String suffix) {
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }

    private static void set(JsonObject flat, String path, Object value) {
        if (flat == null || StringUtils.isBlank(path) || value == null) {
            return;
        }
        if (value instanceof Number n) {
            flat.addProperty(path, n);
        } else {
            flat.addProperty(path, value.toString());
        }
    }

    static final class FrequencyValue {
        final Double frequency;
        final Double frequencyMax;
        final String unit;

        FrequencyValue(Double frequency, Double frequencyMax, String unit) {
            this.frequency = frequency;
            this.frequencyMax = frequencyMax;
            this.unit = unit;
        }
    }

    static final class DurationValue {
        final String value;
        final String valueMax;

        DurationValue(String value, String valueMax) {
            this.value = value;
            this.valueMax = valueMax;
        }
    }
}
