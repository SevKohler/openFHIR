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
    // TODO operates currently on the Flat paths this makes the mappers only usable for the KDS mappigns, there need to be some resolving to canonicalpaths back and forth implemented
    private static final Set<String> CODES = Set.of(
            "timingToDaily",
            "timingNonDaily",
            "dosageQuantityToRange",
            "ratio_to_dv_quantity",
            "ratio_to_dosage",
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
            case "dosageQuantityToRange" -> toOpenEhrDosageQuantityToRange(openEhrPath, fhirValue, flat, populator);
            case "ratio_to_dv_quantity" -> toOpenEhrRatioToDvQuantity(openEhrPath, fhirValue, flat, populator);
            case "ratio_to_dosage" -> toOpenEhrRatioToDosage(openEhrPath, fhirValue, flat, populator);
            case "timingToDaily" -> toOpenEhrTimingDaily(openEhrPath, fhirValue, flat, populator);
            case "timingNonDaily" -> toOpenEhrTimingNonDaily(openEhrPath, fhirValue, flat, populator);
            case "dosageDurationToAdministrationDuration" -> toOpenEhrDurationToAdministrationDuration(openEhrPath, fhirValue, flat, populator);
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
            case "ratio_to_dosage" -> toFhirRatioDosage(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            case "timingToDaily" -> toFhirTimingDaily(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            case "timingNonDaily" -> toFhirTimingNonDaily(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            case "dosageDurationToAdministrationDuration" -> toFhirTimingRepeat(joinedValues, valueHolder, lastIndex, path, mapperUtils);
            default -> null;
        };
    }

    private boolean toOpenEhrDosageQuantityToRange(final String openEhrPath,
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

    private boolean toOpenEhrRatioToDvQuantity(final String openEhrPath,
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
        setUcumSystemIfPresent(q);

        populator.setFhirPathValue(openEhrPath, q, FhirConnectConst.DV_QUANTITY, flat);
        return true;
    }

    private boolean toOpenEhrRatioToDosage(final String openEhrPath,
                                           final Base fhirValue,
                                           final JsonObject flat,
                                           final OpenEhrPopulator populator) {
        if (!(fhirValue instanceof Ratio ratio)) {
            return false;
        }
        Quantity numerator = ratio.getNumerator();
        Quantity denominator = ratio.getDenominator();
        if (numerator == null || denominator == null || numerator.getValue() == null || denominator.getValue() == null) {
            return false;
        }
        Double denomValue = denominator.getValue().doubleValue();
        if (denomValue == 0d) {
            return false;
        }
        String duration = durationStringFromQuantity(denominator);
        if (StringUtils.isBlank(duration)) {
            return false;
        }

        double rate = numerator.getValue().doubleValue() / denomValue;
        Quantity rateQuantity = new Quantity();
        rateQuantity.setValue(rate);
        if (StringUtils.isNotBlank(numerator.getUnit())) {
            String unit = numerator.getUnit() + "/" + denominator.getUnit();
            rateQuantity.setUnit(unit);
        }
        if (StringUtils.isNotBlank(numerator.getCode())) {
            rateQuantity.setCode(numerator.getCode());
        }
        setUcumSystemIfPresent(rateQuantity);
        String ratePath = appendFlatChild(openEhrPath, "verabreichungsrate/quantity_value");
        populator.setFhirPathValue(ratePath, rateQuantity, FhirConnectConst.DV_QUANTITY, flat);

        String durationPath = appendFlatChild(openEhrPath, "verabreichungsdauer");
        populator.setFhirPathValue(durationPath, new StringType(duration), FhirConnectConst.DV_DURATION, flat);
        return true;
    }

    private String appendFlatChild(final String basePath, final String child) {
        if (StringUtils.isBlank(basePath) || StringUtils.isBlank(child)) {
            return basePath;
        }
        String base = basePath;
        if (base.startsWith(FhirConnectConst.OPENEHR_ARCHETYPE_FC)) {
            base = base.substring(FhirConnectConst.OPENEHR_ARCHETYPE_FC.length());
            if (base.startsWith("/")) {
                base = base.substring(1);
            }
        }
        if (base.endsWith("/" + child) || base.contains("/" + child + "/") || base.endsWith(child)) {
            return base;
        }
        return base + "/" + child;
    }

    private boolean toOpenEhrTimingDaily(final String openEhrPath,
                                         final Base fhirValue,
                                         final JsonObject flat,
                                         final OpenEhrPopulator populator) {
        TimingApplyContext ctx = buildTimingApplyContext(fhirValue, true);
        if (ctx == null) {
            return false;
        }

        applyTimingShared(openEhrPath, ctx, flat, populator, true);

        // Frequency -> /frequenz (DV_QUANTITY)
        if (ctx.repeat.hasFrequency() || ctx.repeat.hasFrequencyMax()) {
            Integer freq = ctx.repeat.hasFrequency() ? ctx.repeat.getFrequency() : null;
            Integer freqMax = ctx.repeat.hasFrequencyMax() ? ctx.repeat.getFrequencyMax() : null;
            if (freq != null || freqMax != null) {
                Double frequency = freq != null ? freq.doubleValue() : (freqMax != null ? freqMax.doubleValue() : null);
                Double frequencyMax = freqMax != null ? freqMax.doubleValue() : null;
                String unit = toFrequencyUnit(ctx.periodUnit);
                TimingFlatMapper.writeFrequency(flat, openEhrPath + "/frequenz", frequency, frequencyMax, unit);
            }
        }

        return true;
    }

    private boolean toOpenEhrTimingNonDaily(final String openEhrPath,
                                            final Base fhirValue,
                                            final JsonObject flat,
                                            final OpenEhrPopulator populator) {
        TimingApplyContext ctx = buildTimingApplyContext(fhirValue, false);
        if (ctx == null) {
            return false;
        }
        applyTimingShared(openEhrPath, ctx, flat, populator, false);
        return true;
    }

    private static final class TimingApplyContext {
        private final Timing.TimingRepeatComponent repeat;
        private final Timing.UnitsOfTime periodUnit;
        private final Double period;
        private final Double periodMax;

        private TimingApplyContext(Timing.TimingRepeatComponent repeat,
                                   Timing.UnitsOfTime periodUnit,
                                   Double period,
                                   Double periodMax) {
            this.repeat = repeat;
            this.periodUnit = periodUnit;
            this.period = period;
            this.periodMax = periodMax;
        }
    }

    private TimingApplyContext buildTimingApplyContext(final Base fhirValue, final boolean daily) {
        if (!(fhirValue instanceof Timing timing)) {
            return null;
        }
        Timing.TimingRepeatComponent repeat = timing.getRepeat();
        if (repeat == null) {
            return null;
        }
        Timing.UnitsOfTime periodUnit = extractUnitsOfTime(repeat.getPeriodUnit());
        if (periodUnit == null) {
            return null;
        }
        String unitCode = periodUnit.toCode();
        if (daily && !isDailyPeriodUnit(unitCode)) {
            return null;
        }
        if (!daily && !isNonDailyPeriodUnit(unitCode)) {
            return null;
        }
        Double period = toDouble(repeat.getPeriod());
        Double periodMax = toDouble(repeat.getPeriodMax());
        return new TimingApplyContext(repeat, periodUnit, period, periodMax);
    }

    private void applyTimingShared(final String openEhrPath,
                                   final TimingApplyContext ctx,
                                   final JsonObject flat,
                                   final OpenEhrPopulator populator,
                                   final boolean dailyPeriodFormat) {
        // Specific time (timeOfDay[0]) -> /zeitpunkt (DV_TIME)
        if (ctx.repeat.hasTimeOfDay() && !ctx.repeat.getTimeOfDay().isEmpty()) {
            TimeType time = ctx.repeat.getTimeOfDay().get(0);
            if (time != null && StringUtils.isNotBlank(time.getValueAsString())) {
                populator.setFhirPathValue(openEhrPath + "/zeitpunkt", new TimeType(time.getValueAsString()),
                        FhirConnectConst.DV_TIME, flat);
            }
        }

        // Interval/period -> /periode (DV_DURATION)
        if (ctx.repeat.hasPeriod() && ctx.repeat.hasPeriodUnit()) {
            if (dailyPeriodFormat) {
                TimingFlatMapper.writeDailyPeriod(
                        flat,
                        openEhrPath + "/periode",
                        ctx.period,
                        ctx.periodMax,
                        ctx.periodUnit.toCode());
            } else {
                TimingFlatMapper.writeNonDailyPeriod(
                        flat,
                        openEhrPath + "/periode",
                        ctx.period,
                        ctx.periodMax,
                        ctx.periodUnit.toCode());
            }
        }
    }

    private boolean toOpenEhrDurationToAdministrationDuration(final String openEhrPath,
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
        numerator.setSystem("http://unitsofmeasure.org");
        Quantity denominator = new Quantity();
        denominator.setValue(1);
        denominator.setUnit(parts.length > 1 ? parts[1] : null);
        denominator.setSystem("http://unitsofmeasure.org");

        Ratio ratio = new Ratio();
        ratio.setNumerator(numerator);
        ratio.setDenominator(denominator);
        return new OpenEhrToFhirHelper.DataWithIndex(ratio, lastIndex == null ? -1 : lastIndex, path);
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirRatioDosage(final List<String> joinedValues,
                                                                final JsonObject valueHolder,
                                                                final Integer lastIndex,
                                                                final String path,
                                                                final OpenFhirMapperUtils mapperUtils) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);

        Quantity rateQuantity = readRateQuantity(readers, valueHolder, joinedValues);
        if (rateQuantity == null || rateQuantity.getValue() == null) {
            return null;
        }


        String durationPath = findInValueHolder(valueHolder, "verabreichungsdauer|value");
        if (durationPath == null) durationPath = findInValueHolder(valueHolder, "verabreichungsdauer/duration_value|value");
        if (durationPath == null) durationPath = findInValueHolder(valueHolder, "verabreichungsdauer");
        String duration = durationPath != null ? readers.get(valueHolder, durationPath) : null;
        if (StringUtils.isBlank(duration)) {
            // fallback to existing ratio parsing if duration is missing
            return toFhirRatio(joinedValues, valueHolder, lastIndex, path, mapperUtils);
        }
        DurationParts parts = parseIsoDuration(duration);
        if (parts == null || parts.value == null || parts.unit == null) {
            return toFhirRatio(joinedValues, valueHolder, lastIndex, path, mapperUtils);
        }

        Quantity numerator = new Quantity();
        numerator.setValue(rateQuantity.getValue().doubleValue() * parts.value);
        numerator.setUnit(rateQuantity.getUnit());
        numerator.setCode(rateQuantity.getCode());
        numerator.setSystem("http://unitsofmeasure.org");

        Quantity denominator = new Quantity();
        String denomUnit = unitToCode(parts.unit);
        denominator.setValue(parts.value);
        denominator.setUnit(denomUnit);
        denominator.setCode(denomUnit);
        denominator.setSystem("http://unitsofmeasure.org");
        Ratio ratio = new Ratio();
        ratio.setNumerator(numerator);
        ratio.setDenominator(denominator);
        return new OpenEhrToFhirHelper.DataWithIndex(ratio, lastIndex == null ? -1 : lastIndex, path);
    }

    private Quantity readRateQuantity(final FhirValueReaders readers,
                                      final JsonObject valueHolder,
                                      final List<String> joinedValues) {
        String magPath = findInValueHolder(valueHolder, "verabreichungsrate/quantity_value|magnitude");
        if (magPath == null) magPath = findInValueHolder(valueHolder, "verabreichungsrate|magnitude");
        String unitPath = findInValueHolder(valueHolder, "verabreichungsrate/quantity_value|unit");
        if (unitPath == null) unitPath = findInValueHolder(valueHolder, "verabreichungsrate|unit");
        if (magPath != null) {
            Object number = readers.number(readers.get(valueHolder, magPath));
            if (number instanceof Number num) {
                Quantity quantity = new Quantity();
                quantity.setValue(num.doubleValue());
                if (unitPath != null) {
                    String unit = readers.get(valueHolder, unitPath);
                    quantity.setUnit(unit);
                    quantity.setCode(unit);
                }
                setUcumSystemIfPresent(quantity);
                return quantity;
            }
        }
        return null;
    }

    // resolveCanonicalPath now lives in CustomMapping

    private String durationStringFromQuantity(final Quantity quantity) {
        if (quantity == null || quantity.getValue() == null) {
            return null;
        }
        String unit = StringUtils.isNotBlank(quantity.getCode()) ? quantity.getCode() : quantity.getUnit();
        if (StringUtils.isBlank(unit)) {
            return null;
        }
        Timing.UnitsOfTime u = unitFromString(unit);
        if (u == null) {
            return null;
        }
        return durationString(quantity.getValue().doubleValue(), u);
    }

    private String unitToCode(final Timing.UnitsOfTime unit) {
        if (unit == null) {
            return null;
        }
        return switch (unit) {
            case S -> "s";
            case MIN -> "min";
            case H -> "h";
            case D -> "d";
            case WK -> "wk";
            case MO -> "mo";
            case A -> "a";
            default -> null;
        };
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirTimingDaily(final List<String> joinedValues,
                                                                final JsonObject valueHolder,
                                                                final Integer lastIndex,
                                                                final String path,
                                                                final OpenFhirMapperUtils mapperUtils) {
        return toFhirTiming(joinedValues, valueHolder, lastIndex, path, mapperUtils, true, false);
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirTimingNonDaily(final List<String> joinedValues,
                                                                   final JsonObject valueHolder,
                                                                   final Integer lastIndex,
                                                                   final String path,
                                                                   final OpenFhirMapperUtils mapperUtils) {
        return toFhirTiming(joinedValues, valueHolder, lastIndex, path, mapperUtils, true, false);
    }

    private OpenEhrToFhirHelper.DataWithIndex toFhirTiming(final List<String> joinedValues,
                                                           final JsonObject valueHolder,
                                                           final Integer lastIndex,
                                                           final String path,
                                                           final OpenFhirMapperUtils mapperUtils,
                                                           final boolean includeZeitpunkt,
                                                           final boolean inferPeriodFromFrequency) {
        FhirValueReaders readers = new FhirValueReaders(mapperUtils);
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();

        if (includeZeitpunkt) {
            String timePath = find(joinedValues, "zeitpunkt");
            if (timePath != null) {
                String time = readers.get(valueHolder, timePath);
                if (StringUtils.isNotBlank(time)) {
                    repeat.addTimeOfDay(time);
                }
            }
        }

        TimingFlatMapper.FrequencyValue freq = TimingFlatMapper.readFrequency(valueHolder, joinedValues, readers);
        if (freq != null && freq.frequency != null) {
            setFrequency(repeat, freq.frequency, freq.frequencyMax);
            if (inferPeriodFromFrequency) {
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

        if (populated) {
            setUcumSystemIfPresent(q);
            return q;
        }
        return null;
    }

    private void setUcumSystemIfPresent(Quantity quantity) {
        if (quantity == null) {
            return;
        }
        if (quantity.hasSystem()) {
            return;
        }
        if (StringUtils.isNotBlank(quantity.getCode()) || StringUtils.isNotBlank(quantity.getUnit())) {
            quantity.setSystem("http://unitsofmeasure.org");
        }
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

    private String findInValueHolder(JsonObject valueHolder, String suffix) {
        if (valueHolder == null || valueHolder.keySet() == null) return null;
        return valueHolder.keySet().stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
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


    private Timing.UnitsOfTime unitFromCode(String code) {
        if (code == null) return null;
        return switch (code) {
            case "d" -> Timing.UnitsOfTime.D;
            case "h" -> Timing.UnitsOfTime.H;
            case "min" -> Timing.UnitsOfTime.MIN;
            case "s" -> Timing.UnitsOfTime.S;
            case "wk", "w", "week", "weeks" -> Timing.UnitsOfTime.WK;
            case "mo", "mon", "month", "months" -> Timing.UnitsOfTime.MO;
            case "a", "y", "yr", "year", "years" -> Timing.UnitsOfTime.A;
            case "day", "days" -> Timing.UnitsOfTime.D;
            case "hour", "hours" -> Timing.UnitsOfTime.H;
            case "minute", "minutes" -> Timing.UnitsOfTime.MIN;
            case "second", "seconds" -> Timing.UnitsOfTime.S;
            default -> null;
        };
    }

    private Timing.UnitsOfTime unitFromString(String unit) {
        if (StringUtils.isBlank(unit)) return null;
        String normalized = unit.toLowerCase(Locale.ROOT).trim();
        return unitFromCode(normalized);
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
        boolean timeBased = trimmed.startsWith("PT");
        if (trimmed.startsWith("PT")) {
            val = trimmed.substring(2, trimmed.length() - 1);
        } else {
            val = trimmed.substring(1, trimmed.length() - 1);
        }
        Double value = parseDouble(val);
        Timing.UnitsOfTime unit = unitFromIso(unitChar, timeBased);
        if (value == null || unit == null) {
            return null;
        }
        return new DurationParts(value, unit);
    }

    private Timing.UnitsOfTime unitFromIso(char unit, boolean timeBased) {
        if (timeBased) {
            return switch (unit) {
                case 'H' -> Timing.UnitsOfTime.H;
                case 'M' -> Timing.UnitsOfTime.MIN;
                case 'S' -> Timing.UnitsOfTime.S;
                default -> null;
            };
        }
        return switch (unit) {
            case 'D' -> Timing.UnitsOfTime.D;
            case 'W' -> Timing.UnitsOfTime.WK;
            case 'M' -> Timing.UnitsOfTime.MO;
            case 'Y' -> Timing.UnitsOfTime.A;
            default -> null;
        };
    }

    private boolean isDailyPeriodUnit(String code) {
        if (code == null) return false;
        return switch (code) {
            case "d", "h", "min", "s" -> true;
            default -> false;
        };
    }

    private boolean isNonDailyPeriodUnit(String code) {
        if (code == null) return false;
        return switch (code) {
            case "d", "wk", "mo", "a" -> true;
            default -> false;
        };
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
