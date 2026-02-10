package com.medblocks.openfhir.customMappings;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import com.medblocks.openfhir.tofhir.parser.FhirValueReaders;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;

@Slf4j
public class DosageCustomMappings extends CustomMapping {

    private static final Set<String> CODES = Set.of(
            "timingToDaily_NonDaily",
            "dosageQuantityToRange",
            "ratio_to_dv_quantity",
            "dosageDurationToAdministrationDuration"
    );

    @Override
    public Set<String> mappingCodes() {
        return CODES;
    }

    @Override
    public boolean applyFhirToOpenEhrMapping(final String mappingCode,
                                             final String openEhrPath,
                                             final Base fhirValue,
                                             final String openEhrType,
                                             final JsonObject flat,
                                             final OpenEhrPopulator populator,
                                             final OpenFhirMapperUtils mapperUtils,
                                             final OpenFhirStringUtils stringUtils) {
        if (StringUtils.isBlank(openEhrPath) || fhirValue == null) {
            return false;
        }
        if (FhirConnectConst.OPENEHR_TYPE_NONE.equals(openEhrType)) {
            return false;
        }

        return switch (mappingCode) {
            case "dosageQuantityToRange" -> applyDosageQuantityToRange(openEhrPath, fhirValue, flat, populator);
            case "ratio_to_dv_quantity" -> applyRatioToDvQuantity(openEhrPath, fhirValue, flat, populator);
            case "timingToDaily_NonDaily" -> applyTimingToDaily(openEhrPath, fhirValue, flat, populator);
            case "dosageDurationToAdministrationDuration" -> applyDurationToAdministration(openEhrPath, fhirValue, flat, populator);
            default -> false;
        };
    }

    @Override
    public OpenEhrToFhirHelper.DataWithIndex applyOpenEhrToFhirMapping(final String mappingCode,
                                                                       final List<String> joinedValues,
                                                                       final JsonObject valueHolder,
                                                                       final Integer lastIndex,
                                                                       final String path,
                                                                       final String resourceType,
                                                                       final String fhirPath,
                                                                       final OpenFhirStringUtils stringUtils,
                                                                       final OpenFhirMapperUtils mapperUtils) {
        if (StringUtils.isBlank(path) || joinedValues == null || joinedValues.isEmpty()) {
            return null;
        }
        return switch (mappingCode) {
            case "dosageQuantityToRange" -> toFhirDose(joinedValues, valueHolder, lastIndex, path, fhirPath, mapperUtils);
            case "ratio_to_dv_quantity" -> toFhirRatio(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            case "timingToDaily_NonDaily" -> toFhirTiming(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            case "dosageDurationToAdministrationDuration" -> toFhirTimingRepeat(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            default -> null;
        };
    }

    private boolean applyDosageQuantityToRange(final String openEhrPath,
                                               final Base fhirValue,
                                               final JsonObject flat,
                                               final OpenEhrPopulator populator) {
        if (fhirValue instanceof Range range) {
            // If we previously mapped a single quantity, clear it to avoid conflicts.
            flat.remove(openEhrPath + "|magnitude");
            flat.remove(openEhrPath + "|unit");
            flat.remove(openEhrPath + "|code");
            flat.remove(openEhrPath + "|value");
            String intervalPath = toIntervalOfQuantityPath(openEhrPath);
            populator.setFhirPathValue(intervalPath, range, FhirConnectConst.DV_INTERVAL, flat);
            return true;
        }
        if (fhirValue instanceof Quantity quantity) {
            // If a range was already mapped, keep the range and skip the quantity.
            if (flat.has(openEhrPath + "/lower|magnitude") || flat.has(openEhrPath + "/upper|magnitude")) {
                return true;
            }
            populator.setFhirPathValue(openEhrPath, quantity, FhirConnectConst.DV_QUANTITY, flat);
            return true;
        }
        return false;
    }

    private String toIntervalOfQuantityPath(final String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith("/interval<dv_quantity>_value")) {
            return path;
        }
        if (path.endsWith("/quantity_value")) {
            String base = path.substring(0, path.length() - "/quantity_value".length());
            return base + "/interval<dv_quantity>_value";
        }
        return path;
    }

    private boolean applyRatioToDvQuantity(final String openEhrPath,
                                           final Base fhirValue,
                                           final JsonObject flat,
                                           final OpenEhrPopulator populator) {
        if (!(fhirValue instanceof Ratio ratio)) {
            return false;
        }
        Quantity numerator = ratio.getNumerator();
        Quantity denominator = ratio.getDenominator();
        if (numerator == null || denominator == null || numerator.getValue() == null) {
            return false;
        }
        Quantity q = new Quantity();
        // Keep numerator value, merge units as "numUnit/denUnit"
        q.setValue(numerator.getValue());
        String unit = buildUnit(numerator.getUnit(), denominator.getUnit());
        if (StringUtils.isNotBlank(unit)) {
            q.setUnit(unit);
        }
        String code = buildUnit(numerator.getCode(), denominator.getCode());
        if (StringUtils.isNotBlank(code)) {
            q.setCode(code);
        }

        populator.setFhirPathValue(openEhrPath, q, FhirConnectConst.DV_QUANTITY, flat);
        return true;
    }

    private boolean applyTimingToDaily(final String openEhrPath,
                                       final Base fhirValue,
                                       final JsonObject flat,
                                       final OpenEhrPopulator populator) {
        if (!(fhirValue instanceof Timing timing)) {
            return false;
        }
        Timing.TimingRepeatComponent repeat = timing.getRepeat();
        if (repeat == null) {
            return false;
        }
        Timing.UnitsOfTime periodUnit = extractUnitsOfTime(repeat.getPeriodUnit());
        if (periodUnit == null) {
            return false;
        }
        if (!isAllowedPeriodUnit(periodUnit.toCode())) {
            return false;
        }
        // For daily mapping, period must be 1 when periodUnit is day.
        Double period = toDouble(repeat.getPeriod());
        Double periodMax = toDouble(repeat.getPeriodMax());
        if (periodUnit == Timing.UnitsOfTime.D && period != null && period != 1d) {
            return false;
        }

        // Specific time (timeOfDay[0]) -> /zeitpunkt (DV_TIME)
        if (repeat.hasTimeOfDay() && !repeat.getTimeOfDay().isEmpty()) {
            TimeType time = repeat.getTimeOfDay().get(0);
            if (time != null && StringUtils.isNotBlank(time.getValueAsString())) {
                populator.setFhirPathValue(openEhrPath + "/zeitpunkt", new TimeType(time.getValueAsString()),
                        FhirConnectConst.DV_TIME, flat);
            }
        }

        // Frequency -> /frequenz (DV_QUANTITY)
        if (repeat.hasFrequency() || repeat.hasFrequencyMax()) {
            Integer freq = repeat.getFrequency();
            Integer freqMax = repeat.getFrequencyMax();
            if (freq != null || freqMax != null) {
                Double frequency = freq != null ? freq.doubleValue() : (freqMax != null ? freqMax.doubleValue() : null);
                Double frequencyMax = (freqMax != null && !freqMax.equals(freq)) ? freqMax.doubleValue() : null;
                String unit = toFrequencyUnit(periodUnit);
                TimingFlatMapper.writeFrequency(flat, openEhrPath + "/frequenz", frequency, frequencyMax, unit);
            }
        }

        // Interval/period -> /periode (DV_DURATION)
        if (repeat.hasPeriod() && repeat.hasPeriodUnit()) {
            // Skip interval when it's the daily default (period=1 day)
            if (!(periodUnit == Timing.UnitsOfTime.D && period != null && period == 1d && (periodMax == null || periodMax == 1d))) {
                String duration = durationString(period, periodUnit);
                String durationMax = periodMax != null ? durationString(periodMax, periodUnit) : null;
                if (StringUtils.isNotBlank(duration)) {
                    TimingFlatMapper.writePeriodDuration(flat, openEhrPath + "/periode", duration, durationMax);
                }
            }
        }

        return true;
    }

    private boolean applyDurationToAdministration(final String openEhrPath,
                                                  final Base fhirValue,
                                                  final JsonObject flat,
                                                  final OpenEhrPopulator populator) {
        if (!(fhirValue instanceof Timing.TimingRepeatComponent repeat)) {
            return false;
        }
        if (!repeat.hasDuration() || !repeat.hasDurationUnit()) {
            return false;
        }
        Double duration = toDouble(repeat.getDuration());
        Double durationMax = toDouble(repeat.getDurationMax());
        Timing.UnitsOfTime durationUnit = extractUnitsOfTime(repeat.getDurationUnit());
        String durationStr = buildDurationFromPeriod(duration, durationMax, durationUnit);
        if (StringUtils.isBlank(durationStr)) {
            return false;
        }
        populator.setFhirPathValue(openEhrPath, new StringType(durationStr),
                FhirConnectConst.DV_DURATION, flat);
        return true;
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirDose(final List<String> joinedValues,
                                                         final JsonObject valueHolder,
                                                         final Integer lastIndex,
                                                         final String path,
                                                         final String fhirPath,
                                                         final OpenFhirMapperUtils mapperUtils) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);
        boolean wantsRange = fhirPath != null && fhirPath.contains("as(Range)");

        String lowerMagPath = find(joinedValues, "lower|magnitude");
        String upperMagPath = find(joinedValues, "upper|magnitude");
        boolean hasRange = lowerMagPath != null || upperMagPath != null;

        if (wantsRange || hasRange) {
            Range range = new Range();
            Quantity low = readQuantityForSide(readers, valueHolder, joinedValues, "lower");
            Quantity high = readQuantityForSide(readers, valueHolder, joinedValues, "upper");
            if (low != null) range.setLow(low);
            if (high != null) range.setHigh(high);
            if (range.getLow() == null && range.getHigh() == null) {
                return null;
            }
            return new OpenEhrToFhirHelper.DataWithIndex(range, lastIndex == null ? -1 : lastIndex, path);
        }

        // Quantity
        Quantity q = readQuantityForSide(readers, valueHolder, joinedValues, "");
        if (q == null) {
            return null;
        }
        return new OpenEhrToFhirHelper.DataWithIndex(q, lastIndex == null ? -1 : lastIndex, path);
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirRatio(final List<String> joinedValues,
                                                          final JsonObject valueHolder,
                                                          final Integer lastIndex,
                                                          final String path,
                                                          final OpenFhirMapperUtils mapperUtils) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);
        String magnitudePath = find(joinedValues, "magnitude");
        if (magnitudePath == null) {
            return null;
        }
        Object n = readers.number(readers.get(valueHolder, magnitudePath));
        if (!(n instanceof Number)) {
            return null;
        }
        String unitPath = find(joinedValues, "unit");
        String unit = unitPath != null ? readers.get(valueHolder, unitPath) : null;
        if (StringUtils.isBlank(unit) || !unit.contains("/")) {
            return null;
        }
        String[] parts = unit.split("/", 2);
        Quantity numerator = new Quantity();
        numerator.setValue(((Number) n).doubleValue());
        numerator.setUnit(parts[0]);
        Quantity denominator = new Quantity();
        denominator.setValue(1);
        denominator.setUnit(parts.length > 1 ? parts[1] : null);

        Ratio ratio = new Ratio();
        ratio.setNumerator(numerator);
        ratio.setDenominator(denominator);
        return new OpenEhrToFhirHelper.DataWithIndex(ratio, lastIndex == null ? -1 : lastIndex, path);
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirTiming(final List<String> joinedValues,
                                                           final JsonObject valueHolder,
                                                           final Integer lastIndex,
                                                           final String path,
                                                           final OpenFhirMapperUtils mapperUtils) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();

        String timePath = find(joinedValues, "zeitpunkt");
        if (timePath != null) {
            String time = readers.get(valueHolder, timePath);
            if (StringUtils.isNotBlank(time)) {
                repeat.addTimeOfDay(time);
            }
        }

        TimingFlatMapper.FrequencyValue freq = TimingFlatMapper.readFrequency(valueHolder, joinedValues, readers);
        if (freq != null && freq.frequency != null) {
            setFrequency(repeat, freq.frequency, freq.frequencyMax);
            Timing.UnitsOfTime unit = periodUnitFromFrequency(freq.unit);
            if (unit != null) {
                if (!repeat.hasPeriodUnit()) {
                    repeat.setPeriodUnit(unit);
                }
                if (!repeat.hasPeriod()) {
                    repeat.setPeriod(1d);
                }
            }
        }

        TimingFlatMapper.DurationValue duration = TimingFlatMapper.readDuration(valueHolder, joinedValues, readers);
        if (duration != null && StringUtils.isNotBlank(duration.value)) {
            DurationParts start = parseIsoDuration(duration.value);
            if (start != null) {
                repeat.setPeriod(start.value);
                repeat.setPeriodUnit(start.unit);
            }
            if (StringUtils.isNotBlank(duration.valueMax)) {
                DurationParts end = parseIsoDuration(duration.valueMax);
                if (end != null && start != null && end.unit == start.unit && !end.value.equals(start.value)) {
                    repeat.setPeriodMax(end.value);
                }
            }
        }

        if (!repeat.isEmpty()) {
            timing.setRepeat(repeat);
            return new OpenEhrToFhirHelper.DataWithIndex(timing, lastIndex == null ? -1 : lastIndex, path);
        }
        return null;
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirTimingRepeat(final List<String> joinedValues,
                                                                 final JsonObject valueHolder,
                                                                 final Integer lastIndex,
                                                                 final String path,
                                                                 final OpenFhirMapperUtils mapperUtils) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);
        String durationPath = find(joinedValues, "duration");
        if (durationPath == null) {
            durationPath = find(joinedValues, "value");
        }
        if (durationPath == null) {
            durationPath = find(joinedValues, "magnitude");
        }
        if (durationPath == null) {
            durationPath = find(joinedValues, "periode");
        }
        if (durationPath == null) {
            return null;
        }

        String duration = readers.get(valueHolder, durationPath);
        if (StringUtils.isBlank(duration)) {
            return null;
        }
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        parseDurationValue(duration, repeat);
        return repeat.isEmpty() ? null : new OpenEhrToFhirHelper.DataWithIndex(repeat, lastIndex == null ? -1 : lastIndex, path);
    }

    private Quantity readQuantityForSide(FhirValueReaders readers, JsonObject valueHolder,
                                         List<String> joinedValues, String side) {
        String prefix = StringUtils.isBlank(side) ? "" : side + "|";
        String magnitudePath = find(joinedValues, prefix + "magnitude");
        String unitPath = find(joinedValues, prefix + "unit");
        String codePath = find(joinedValues, prefix + "code");
        String valuePath = find(joinedValues, prefix + "value");

        Quantity q = new Quantity();
        boolean populated = false;

        if (magnitudePath != null) {
            Object n = readers.number(readers.get(valueHolder, magnitudePath));
            if (n instanceof Number) {
                q.setValue(((Number) n).doubleValue());
                populated = true;
            }
        }
        if (unitPath != null) {
            String unit = readers.get(valueHolder, unitPath);
            if (StringUtils.isNotBlank(unit)) {
                q.setUnit(unit);
                populated = true;
            }
        }
        if (valuePath != null) {
            String unit = readers.get(valueHolder, valuePath);
            if (StringUtils.isNotBlank(unit)) {
                q.setUnit(unit);
                populated = true;
            }
        }
        if (codePath != null) {
            String code = readers.get(valueHolder, codePath);
            if (StringUtils.isNotBlank(code)) {
                q.setCode(code);
                populated = true;
            }
        }

        return populated ? q : null;
    }

    private String buildUnit(String numeratorUnit, String denominatorUnit) {
        if (StringUtils.isBlank(numeratorUnit)) {
            return null;
        }
        if (StringUtils.isBlank(denominatorUnit)) {
            return numeratorUnit;
        }
        return numeratorUnit + "/" + denominatorUnit;
    }

    private String toFrequencyUnit(Timing.UnitsOfTime unit) {
        if (unit == null) return null;
        return switch (unit.toCode()) {
            case "d" -> "1/d";
            case "h" -> "1/h";
            case "min" -> "1/min";
            case "s" -> "1/s";
            default -> null;
        };
    }

    private String buildDurationFromPeriod(Double period, Double periodMax, Timing.UnitsOfTime unit) {
        if (period == null || unit == null) {
            return null;
        }
        String base = durationString(period, unit);
        if (StringUtils.isBlank(base)) {
            return null;
        }
        if (periodMax != null && periodMax > 0 && !periodMax.equals(period)) {
            String max = durationString(periodMax, unit);
            if (StringUtils.isNotBlank(max)) {
                return base + "-" + max;
            }
        }
        return base;
    }

    private String durationString(Double value, Timing.UnitsOfTime unit) {
        if (value == null || unit == null) {
            return null;
        }
        String code = unit.toCode();
        if (!isAllowedPeriodUnit(code)) {
            log.debug("Unsupported periodUnit {}", code);
            return null;
        }
        String v = stripTrailingZeros(value);
        return switch (code) {
            case "d" -> "P" + v + "D";
            case "h" -> "PT" + v + "H";
            case "min" -> "PT" + v + "M";
            case "s" -> "PT" + v + "S";
            default -> null;
        };
    }

    private boolean isAllowedPeriodUnit(String code) {
        if (code == null) return false;
        return switch (code) {
            case "d", "h", "min", "s" -> true;
            default -> false;
        };
    }

    private String stripTrailingZeros(Double value) {
        if (value == null) return null;
        String s = String.format(Locale.ROOT, "%s", value);
        if (s.contains(".")) {
            s = s.replaceAll("\\.?0+$", "");
        }
        return s;
    }

    private String find(List<String> joinedValues, String suffix) {
        if (joinedValues == null) return null;
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }

    private void setFrequency(Timing.TimingRepeatComponent repeat, Double frequency, Double frequencyMax) {
        if (frequency == null) return;
        int freqVal = (int) Math.round(frequency);
        repeat.setFrequency(freqVal);
        if (frequencyMax != null && !frequencyMax.equals(frequency)) {
            int maxVal = (int) Math.round(frequencyMax);
            repeat.setFrequencyMax(maxVal);
        }
    }

    private void parseDurationValue(String text, Timing.TimingRepeatComponent repeat) {
        if (StringUtils.isBlank(text)) return;
        String trimmed = text.trim();
        if (!trimmed.startsWith("P")) {
            return;
        }
        DurationParts start = parseIsoDuration(trimmed);
        if (start == null) {
            return;
        }
        repeat.setDuration(start.value);
        repeat.setDurationUnit(start.unit);
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-", 2);
            DurationParts end = parseIsoDuration(parts[1]);
            if (end != null && end.unit == start.unit && !end.value.equals(start.value)) {
                repeat.setDurationMax(end.value);
            }
        }
    }


    private Timing.UnitsOfTime unitFromIso(char unit) {
        return switch (unit) {
            case 'D' -> Timing.UnitsOfTime.D;
            case 'H' -> Timing.UnitsOfTime.H;
            case 'M' -> Timing.UnitsOfTime.MIN;
            case 'S' -> Timing.UnitsOfTime.S;
            default -> null;
        };
    }

    private Timing.UnitsOfTime unitFromCode(String code) {
        if (code == null) return null;
        return switch (code) {
            case "d" -> Timing.UnitsOfTime.D;
            case "h" -> Timing.UnitsOfTime.H;
            case "min" -> Timing.UnitsOfTime.MIN;
            case "s" -> Timing.UnitsOfTime.S;
            case "day", "days" -> Timing.UnitsOfTime.D;
            case "hour", "hours" -> Timing.UnitsOfTime.H;
            case "minute", "minutes" -> Timing.UnitsOfTime.MIN;
            case "second", "seconds" -> Timing.UnitsOfTime.S;
            default -> null;
        };
    }

    private Timing.UnitsOfTime periodUnitFromFrequency(String unit) {
        if (StringUtils.isBlank(unit)) return null;
        String normalized = unit.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("1/")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return unitFromCode(normalized);
    }

    private Timing.UnitsOfTime extractUnitsOfTime(Object unitObj) {
        if (unitObj == null) {
            return null;
        }
        if (unitObj instanceof Timing.UnitsOfTime u) {
            return u;
        }
        if (unitObj instanceof org.hl7.fhir.r4.model.Enumeration<?> e) {
            Object v = e.getValue();
            if (v instanceof Timing.UnitsOfTime u) {
                return u;
            }
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private DurationParts parseIsoDuration(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.contains("-")) {
            trimmed = trimmed.split("-", 2)[0];
        }
        if (!trimmed.startsWith("P")) {
            return null;
        }
        String val;
        char unitChar = trimmed.charAt(trimmed.length() - 1);
        if (trimmed.startsWith("PT")) {
            val = trimmed.substring(2, trimmed.length() - 1);
        } else {
            val = trimmed.substring(1, trimmed.length() - 1);
        }
        Double value = parseDouble(val);
        Timing.UnitsOfTime unit = unitFromIso(unitChar);
        if (value == null || unit == null) {
            return null;
        }
        return new DurationParts(value, unit);
    }

    private static final class DurationParts {
        private final Double value;
        private final Timing.UnitsOfTime unit;

        private DurationParts(Double value, Timing.UnitsOfTime unit) {
            this.value = value;
            this.unit = unit;
        }
    }
}
