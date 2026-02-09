package com.medblocks.openfhir.customMappings;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.TimeType;
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
                "timingToDaily_NonDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(600.0, flat.get("dose|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose|unit").getAsString());

        Assert.assertEquals(600.0, flat.get("rate|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg/h", flat.get("rate|unit").getAsString());

        Assert.assertEquals("08:00:00", flat.get("timing/zeitpunkt").getAsString());
        Assert.assertEquals(3.0, flat.get("timing/frequenz|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("1/d", flat.get("timing/frequenz|unit").getAsString());
        Assert.assertFalse("periode should be omitted for daily timing", flat.has("timing/periode|value"));
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
                "timingToDaily_NonDaily", "timing", timing, FhirConnectConst.DV_TEXT, flat, populator, mapperUtils, stringUtils));
        Assert.assertTrue(mappings.applyFhirToOpenEhrMapping(
                "dosageDurationToAdministrationDuration", "duration", durationRepeat, FhirConnectConst.DV_DURATION, flat, populator, mapperUtils, stringUtils));

        Assert.assertEquals(200.0, flat.get("dose/lower|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/lower|unit").getAsString());
        Assert.assertEquals(300.0, flat.get("dose/upper|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/upper|unit").getAsString());

        Assert.assertEquals(100.0, flat.get("rate|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg/h", flat.get("rate|unit").getAsString());

        Assert.assertEquals("08:00:00", flat.get("timing/zeitpunkt").getAsString());
        Assert.assertEquals(3.0, flat.get("timing/frequenz|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("1/h", flat.get("timing/frequenz|unit").getAsString());
        Assert.assertEquals("PT2H", flat.get("timing/periode/lower|value").getAsString());
        Assert.assertEquals("PT3H", flat.get("timing/periode/upper|value").getAsString());

        Assert.assertEquals("PT1H-PT3H", flat.get("duration").getAsString());
    }
}
