package com.medblocks.openfhir.customMappings;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import java.util.List;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Assert;
import org.junit.Test;

public class DosageCustomMappingsTest {

    private final DosageCustomMappings mappings = new DosageCustomMappings();
    private final OpenFhirMapperUtils mapperUtils = new OpenFhirMapperUtils();
    private final OpenFhirStringUtils stringUtils = new OpenFhirStringUtils();
    private final OpenEhrPopulator populator = new OpenEhrPopulator(mapperUtils);

    @Test
    public void example1_mapsDoseRateAndTimingDaily() {
        JsonObject flat = new JsonObject();

        Quantity dose = new Quantity().setValue(600).setUnit("mg").setSystem("http://unitsofmeasure.org").setCode("mg");
        Ratio rate = new Ratio()
                .setNumerator(new Quantity().setValue(600).setUnit("mg").setSystem("http://unitsofmeasure.org").setCode("mg"))
                .setDenominator(new Quantity().setValue(1).setUnit("h").setSystem("http://unitsofmeasure.org").setCode("h"));

        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        repeat.setFrequency(3);
        repeat.setPeriod(1);
        repeat.setPeriodUnit(Timing.UnitsOfTime.D);
        repeat.addTimeOfDay("08:00:00");
        timing.setRepeat(repeat);

        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "dosageQuantityToRange", "dose", dose, FhirConnectConst.DV_QUANTITY, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "ratio_to_dv_quantity", "rate", rate, FhirConnectConst.DV_QUANTITY, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "timingToDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(600.0, flat.get("dose|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose|unit").getAsString());

        Assert.assertEquals(600.0, flat.get("rate|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg/h", flat.get("rate|unit").getAsString());

        Assert.assertEquals("08:00:00", flat.get("timing/zeitpunkt").getAsString());
        Assert.assertEquals(3.0, flat.get("timing/frequenz/quantity_value|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("1/d", flat.get("timing/frequenz/quantity_value|unit").getAsString());
        Assert.assertEquals(1.0, flat.get("timing/periode|day").getAsDouble(), 0.0001);
        Assert.assertFalse("daily period should not use duration value format", flat.has("timing/periode/duration_value|value"));
    }

    @Test
    public void example3_mapsDoseRangeRateTimingAndDuration() {
        JsonObject flat = new JsonObject();

        Range doseRange = new Range()
                .setLow(new Quantity().setValue(200).setUnit("mg").setSystem("http://unitsofmeasure.org").setCode("mg"))
                .setHigh(new Quantity().setValue(300).setUnit("mg").setSystem("http://unitsofmeasure.org").setCode("mg"));

        Ratio rate = new Ratio()
                .setNumerator(new Quantity().setValue(100).setUnit("mg").setSystem("http://unitsofmeasure.org").setCode("mg"))
                .setDenominator(new Quantity().setValue(1).setUnit("h").setSystem("http://unitsofmeasure.org").setCode("h"));

        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        repeat.setFrequency(3);
        repeat.setFrequencyMax(4);
        repeat.setPeriod(2);
        repeat.setPeriodMax(3);
        repeat.setPeriodUnit(Timing.UnitsOfTime.H);
        repeat.addTimeOfDay("08:00:00");
        timing.setRepeat(repeat);

        Timing.TimingRepeatComponent durationRepeat = new Timing.TimingRepeatComponent();
        durationRepeat.setDuration(1);
        durationRepeat.setDurationMax(3);
        durationRepeat.setDurationUnit(Timing.UnitsOfTime.H);

        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "dosageQuantityToRange", "dose", doseRange, FhirConnectConst.DV_INTERVAL, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "ratio_to_dv_quantity", "rate", rate, FhirConnectConst.DV_QUANTITY, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "timingToDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "dosageDurationToAdministrationDuration", "duration", durationRepeat, FhirConnectConst.DV_DURATION, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(200.0, flat.get("dose/lower|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/lower|unit").getAsString());
        Assert.assertEquals(300.0, flat.get("dose/upper|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/upper|unit").getAsString());

        Assert.assertEquals(100.0, flat.get("rate|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg/h", flat.get("rate|unit").getAsString());

        Assert.assertEquals("08:00:00", flat.get("timing/zeitpunkt").getAsString());
        Assert.assertEquals(3.0, flat.get("timing/frequenz/interval<dv_quantity>_value/lower|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals(4.0, flat.get("timing/frequenz/interval<dv_quantity>_value/upper|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("1/h", flat.get("timing/frequenz/interval<dv_quantity>_value/lower|unit").getAsString());
        Assert.assertEquals("1/h", flat.get("timing/frequenz/interval<dv_quantity>_value/upper|unit").getAsString());
        Assert.assertEquals(2.0, flat.get("timing/periode/lower|hour").getAsDouble(), 0.0001);
        Assert.assertEquals(3.0, flat.get("timing/periode/upper|hour").getAsDouble(), 0.0001);

        Assert.assertEquals("PT1H-PT3H", flat.get("duration").getAsString());
    }

    @Test
    public void mapsTimingNonDailyPeriodAsDurationValue() {
        JsonObject flat = new JsonObject();

        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        repeat.setPeriod(2);
        repeat.setPeriodMax(3);
        repeat.setPeriodUnit(Timing.UnitsOfTime.WK);
        timing.setRepeat(repeat);

        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "timingNonDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(2.0, flat.get("timing/periode/lower|week").getAsDouble(), 0.0001);
        Assert.assertEquals(3.0, flat.get("timing/periode/upper|week").getAsDouble(), 0.0001);
        Assert.assertFalse("non-daily period should not use daily component format", flat.has("timing/periode|day"));
    }

    @Test
    public void fhirToOpenEhr_timingFrequencyMaxUsesIntervalDvQuantity() {
        JsonObject flat = new JsonObject();
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        repeat.setFrequency(3);
        repeat.setFrequencyMax(3);
        repeat.setPeriod(1);
        repeat.setPeriodUnit(Timing.UnitsOfTime.D);
        timing.setRepeat(repeat);

        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "timingToDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(3.0, flat.get("timing/frequenz/interval<dv_quantity>_value/lower|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals(3.0, flat.get("timing/frequenz/interval<dv_quantity>_value/upper|magnitude").getAsDouble(), 0.0001);
        Assert.assertFalse(flat.has("timing/frequenz/quantity_value|magnitude"));
    }

    @Test
    public void toFhirTiming_readsFrequencyIntervalAsFrequencyAndMax() {
        JsonObject valueHolder = new JsonObject();
        valueHolder.addProperty("timing/frequenz/interval<dv_quantity>_value/lower|magnitude", 3.0);
        valueHolder.addProperty("timing/frequenz/interval<dv_quantity>_value/upper|magnitude", 4.0);
        valueHolder.addProperty("timing/frequenz/interval<dv_quantity>_value/lower|unit", "1/d");

        OpenEhrToFhirHelper.DataWithIndex mapped = mappings.applyOpenEhrToFhirMapping(
                "timingToDaily",
                List.of(
                        "timing/frequenz/interval<dv_quantity>_value/lower|magnitude",
                        "timing/frequenz/interval<dv_quantity>_value/upper|magnitude",
                        "timing/frequenz/interval<dv_quantity>_value/lower|unit"),
                valueHolder,
                0,
                "timing",
                "Dosage",
                "timing",
                stringUtils,
                mapperUtils);

        Assert.assertNotNull(mapped);
        Assert.assertTrue(mapped.getData() instanceof Timing);
        Timing timing = (Timing) mapped.getData();
        Assert.assertTrue(timing.hasRepeat());
        Assert.assertEquals(3, timing.getRepeat().getFrequency());
        Assert.assertEquals(4, timing.getRepeat().getFrequencyMax());
    }

    @Test
    public void toFhirTiming_readsSingleFrequencyWithoutMax() {
        JsonObject valueHolder = new JsonObject();
        valueHolder.addProperty("timing/frequenz/quantity_value|magnitude", 3.0);
        valueHolder.addProperty("timing/frequenz/quantity_value|unit", "1/d");

        OpenEhrToFhirHelper.DataWithIndex mapped = mappings.applyOpenEhrToFhirMapping(
                "timingToDaily",
                List.of(
                        "timing/frequenz/quantity_value|magnitude",
                        "timing/frequenz/quantity_value|unit"),
                valueHolder,
                0,
                "timing",
                "Dosage",
                "timing",
                stringUtils,
                mapperUtils);

        Assert.assertNotNull(mapped);
        Assert.assertTrue(mapped.getData() instanceof Timing);
        Timing timing = (Timing) mapped.getData();
        Assert.assertTrue(timing.hasRepeat());
        Assert.assertEquals(3, timing.getRepeat().getFrequency());
        Assert.assertFalse(timing.getRepeat().hasFrequencyMax());
    }

    @Test
    public void fhirToOpenEhr_rangeToText_serializesReadableAndParsable() {
        JsonObject flat = new JsonObject();
        Range rateRange = new Range()
                .setLow(new Quantity().setValue(150).setUnit("mL/h").setSystem("http://unitsofmeasure.org").setCode("mL/h"))
                .setHigh(new Quantity().setValue(300).setUnit("mL/h").setSystem("http://unitsofmeasure.org").setCode("mL/h"));

        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "rangeToText", "rate_text", rateRange, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));
        Assert.assertEquals("150-300 mL/h", flat.get("rate_text").getAsString());
    }

    @Test
    public void openEhrToFhir_rangeToText_parsesBackToRange() {
        JsonObject valueHolder = new JsonObject();
        valueHolder.addProperty("rate_text", "150-300 mL/h");

        OpenEhrToFhirHelper.DataWithIndex mapped = mappings.applyOpenEhrToFhirMapping(
                "rangeToText",
                List.of("rate_text"),
                valueHolder,
                0,
                "rate_text",
                "Dosage",
                "doseAndRate.rate.as(Range)",
                stringUtils,
                mapperUtils);

        Assert.assertNotNull(mapped);
        Assert.assertTrue(mapped.getData() instanceof Range);
        Range range = (Range) mapped.getData();
        Assert.assertEquals("150.0", range.getLow().getValue().toPlainString());
        Assert.assertEquals("300.0", range.getHigh().getValue().toPlainString());
        Assert.assertEquals("mL/h", range.getLow().getUnit());
        Assert.assertEquals("mL/h", range.getHigh().getUnit());
        Assert.assertEquals("http://unitsofmeasure.org", range.getLow().getSystem());
        Assert.assertEquals("http://unitsofmeasure.org", range.getHigh().getSystem());
    }

}
