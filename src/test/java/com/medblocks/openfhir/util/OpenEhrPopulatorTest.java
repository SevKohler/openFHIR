package com.medblocks.openfhir.util;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import org.hl7.fhir.r4.model.Period;
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

        Assert.assertEquals("DV_INTERVAL", flat.get("specimen/collection_time|_type").getAsString());
        Assert.assertEquals("DV_DATE_TIME", flat.get("specimen/collection_time/lower|_type").getAsString());
        Assert.assertTrue(flat.get("specimen/collection_time/lower|value").getAsString()
                .startsWith("2024-02-15T08:00:00"));
        Assert.assertEquals("DV_DATE_TIME", flat.get("specimen/collection_time/upper|_type").getAsString());
        Assert.assertTrue(flat.get("specimen/collection_time/upper|value").getAsString()
                .startsWith("2024-02-20T17:30:00"));
        Assert.assertEquals("true", flat.get("specimen/collection_time/lower_included").getAsString());
        Assert.assertEquals("true", flat.get("specimen/collection_time/upper_included").getAsString());
    }
}
