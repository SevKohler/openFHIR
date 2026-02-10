package com.medblocks.openfhir.util;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OpenEhrPopulatorTest {

    private OpenEhrPopulator populator;
    private JsonObject flat;

    @Before
    public void setUp() {
        populator = new OpenEhrPopulator(new OpenFhirMapperUtils());
        flat = new JsonObject();
    }

    @Test
    public void contextStartTimeRetainsEarliestValue() {
        JsonObject localFlat = new JsonObject();
        String path = "test_template/context/start_time";

        populator.addToConstructingFlat(path, "2024-05-05T10:00:00", localFlat);
        populator.addToConstructingFlat(path, "2024-05-05T12:00:00", localFlat);
        populator.addToConstructingFlat(path, "2024-05-05T09:00:00", localFlat);

        Assert.assertEquals("2024-05-05T09:00:00", localFlat.get(path).getAsString());
    }

    @Test
    public void contextEndTimeRetainsLatestValue() {
        JsonObject localFlat = new JsonObject();
        String path = "test_template/context/_end_time";

        populator.addToConstructingFlat(path, "2024-05-05T10:00:00", localFlat);
        populator.addToConstructingFlat(path, "2024-05-05T09:00:00", localFlat);
        populator.addToConstructingFlat(path, "2024-05-05T11:00:00", localFlat);

        Assert.assertEquals("2024-05-05T11:00:00", localFlat.get(path).getAsString());
    }

    @Test
    public void setNullFlavourForDataAbsentReasonCoding() {
        Coding coding = new Coding(
                "http://terminology.hl7.org/CodeSystem/data-absent-reason",
                "unknown",
                "Unknown");

        boolean handled = populator.setNullFlavourForDataAbsentReason("test/value/null_flavour", coding, flat);

        Assert.assertTrue(handled);
        Assert.assertEquals("unknown", flat.get("test/value/null_flavour|value").getAsString());
        Assert.assertEquals("253", flat.get("test/value/null_flavour|code").getAsString());
        Assert.assertEquals("openehr", flat.get("test/value/null_flavour|terminology").getAsString());
    }

    @Test
    public void setFhirPathValueHandlesNullFlavourPath() {
        Coding coding = new Coding(
                "http://terminology.hl7.org/CodeSystem/data-absent-reason",
                "asked-declined",
                "Asked Declined");

        populator.setFhirPathValue("test/value/null_flavour", coding, FhirConnectConst.CODE_PHRASE, flat);

        Assert.assertEquals("masked", flat.get("test/value/null_flavour|value").getAsString());
        Assert.assertEquals("272", flat.get("test/value/null_flavour|code").getAsString());
        Assert.assertEquals("openehr", flat.get("test/value/null_flavour|terminology").getAsString());
    }

    @Test
    public void periodMapsToDvInterval() {
        Period period = new Period();
        period.setStartElement(new org.hl7.fhir.r4.model.DateTimeType("2024-02-15T08:00:00+01:00"));
        period.setEndElement(new org.hl7.fhir.r4.model.DateTimeType("2024-02-20T17:30:00+01:00"));

        populator.setFhirPathValue("specimen/collection_time", period, FhirConnectConst.DV_INTERVAL, flat);

        Assert.assertTrue(flat.get("specimen/collection_time/lower|value").getAsString()
                .startsWith("2024-02-15T08:00:00"));
        Assert.assertTrue(flat.get("specimen/collection_time/upper|value").getAsString()
                .startsWith("2024-02-20T17:30:00"));
    }

    @Test
    public void rangeMapsToDvIntervalWithDvQuantity() {
        Range range = new Range();
        Quantity low = new Quantity();
        low.setValue(1.5);
        low.setUnit("mg");
        Quantity high = new Quantity();
        high.setValue(3.0);
        high.setUnit("mg");
        range.setLow(low);
        range.setHigh(high);

        populator.setFhirPathValue("dose/amount", range, FhirConnectConst.DV_INTERVAL, flat);


        Assert.assertEquals(1.5, flat.get("dose/amount/lower|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/amount/lower|unit").getAsString());
        Assert.assertEquals(3.0, flat.get("dose/amount/upper|magnitude").getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get("dose/amount/upper|unit").getAsString());

    }
}
