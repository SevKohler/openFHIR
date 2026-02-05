package com.medblocks.openfhir.toFHIRParser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.tofhir.parser.ValueToFHIRParser;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Date;

import static com.medblocks.openfhir.fc.FhirConnectConst.*;
import static org.junit.jupiter.api.Assertions.*;

class ValueToFHIRParserTest {

    private ValueToFHIRParser parser;

    @BeforeEach
    void setUp() {
        // Minimal stubs (no Spring, no Mockito needed)
        OpenFhirStringUtils stringUtils = new OpenFhirStringUtilsStub();
        OpenFhirMapperUtils mapperUtils = new OpenFhirMapperUtilsStub();

        parser = new ValueToFHIRParser(stringUtils, mapperUtils);
    }

    // ---------- DV_DATE_TIME ----------

    @Test
    void parse_dateTime_createsDateTimeType() {
        JsonObject json = new JsonObject();
        json.addProperty("some|datetime", "2020-01-02T03:04:05Z");

        var out = parser.parse(
                List.of("some|datetime"),
                DV_DATE_TIME,
                json,
                false,
                "Observation",
                "Observation.effectiveDateTime"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof DateTimeType);
        DateTimeType dt = (DateTimeType) out.getData();
        assertEquals(Date.from(Instant.parse("2020-01-02T03:04:05Z")), dt.getValue());
    }

    // ---------- DV_DATE ----------

    @Test
    void parse_date_createsDateType() {
        JsonObject json = new JsonObject();
        json.addProperty("some|date", "2020-01-02");

        var out = parser.parse(
                List.of("some|date"),
                DV_DATE,
                json,
                false,
                "Observation",
                "Observation.effectiveDate"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof DateType);
        DateType dt = (DateType) out.getData();
        // Using DateType's string value check is easiest
        assertEquals("2020-01-02", dt.getValueAsString());
    }

    // ---------- DV_INTERVAL ----------

    @Test
    void parse_interval_createsPeriod() {
        JsonObject json = new JsonObject();
        json.addProperty("iv/lower|value", "2024-02-15T08:00:00+01:00");
        json.addProperty("iv/upper|value", "2024-02-20T17:30:00+01:00");

        var out = parser.parse(
                List.of("iv/lower|value", "iv/upper|value"),
                DV_INTERVAL,
                json,
                false,
                "Observation",
                "Observation.effectivePeriod"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof Period);
        Period period = (Period) out.getData();
        assertEquals("2024-02-15T08:00:00+01:00", period.getStartElement().getValueAsString());
        assertEquals("2024-02-20T17:30:00+01:00", period.getEndElement().getValueAsString());
    }

    // ---------- DV_TIME ----------

    @Test
    void parse_time_createsTimeType() {
        JsonObject json = new JsonObject();
        json.addProperty("some|time", "13:37:00");

        var out = parser.parse(
                List.of("some|time"),
                DV_TIME,
                json,
                false,
                "Observation",
                "Observation.effectiveTime"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof TimeType);
        assertEquals("13:37:00", ((TimeType) out.getData()).getValue());
    }

    // ---------- DV_BOOL ----------

    @Test
    void parse_bool_createsBooleanType() {
        JsonObject json = new JsonObject();
        json.addProperty("some|bool", "true");

        var out = parser.parse(
                List.of("some|bool"),
                DV_BOOL,
                json,
                false,
                "Observation",
                "Observation.valueBoolean"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof BooleanType);
        assertTrue(((BooleanType) out.getData()).booleanValue());
    }

    // ---------- DV_COUNT ----------

    @Test
    void parse_count_createsIntegerType() {
        JsonObject json = new JsonObject();
        json.addProperty("some|count", "42");

        var out = parser.parse(
                List.of("some|count"),
                DV_COUNT,
                json,
                false,
                "Observation",
                "Observation.valueInteger"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof IntegerType);
        assertEquals(42, ((IntegerType) out.getData()).getValue().intValue());
    }

    // ---------- DV_PROPORTION ----------

    @Test
    void parse_proportion_setsPercentCodeWhenDenominator100() {
        JsonObject json = new JsonObject();
        json.addProperty("p|numerator", "25");
        json.addProperty("p|denominator", "100.0");

        var out = parser.parse(
                List.of("p"),   // base path
                DV_PROPORTION,
                json,
                false,
                "Observation",
                "Observation.valueQuantity"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof Quantity);

        Quantity q = (Quantity) out.getData();
        assertEquals(25, q.getValue().intValue());
        assertEquals("%", q.getCode());
        assertEquals("percent", q.getUnit());
        assertEquals("http://unitsofmeasure.org", q.getSystem());
    }

    // ---------- DV_QUANTITY ----------

    @Test
    void parse_quantity_usesMagnitudeUnitAndCode() {
        JsonObject json = new JsonObject();
        json.addProperty("q|magnitude", "12.5");
        json.addProperty("q|unit", "mmHg");
        json.addProperty("q|code", "mm[Hg]");

        var out = parser.parse(
                List.of("q|magnitude", "q|unit", "q|code"), // joinedValues
                DV_QUANTITY,
                json,
                false,
                "Observation",
                "Observation.valueQuantity"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof Quantity);

        Quantity q = (Quantity) out.getData();
        assertEquals(12.5, q.getValue().doubleValue(), 0.0001);
        assertEquals("mmHg", q.getUnit());
        assertEquals("mm[Hg]", q.getCode());
    }

    // ---------- DV_MULTIMEDIA ----------

    @Test
    void parse_media_createsAttachment() {
        JsonObject json = new JsonObject();
        json.addProperty("m|mediatype", "image/png");
        json.addProperty("m|size", "3");
        json.addProperty("m|url", "http://example.test/a.png");
        json.addProperty("m|data", "abc");

        var out = parser.parse(
                List.of("m"),
                DV_MULTIMEDIA,
                json,
                false,
                "DocumentReference",
                "DocumentReference.content.attachment"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof Attachment);

        Attachment att = (Attachment) out.getData();
        assertEquals("image/png", att.getContentType());
        assertEquals(3, att.getSize());
        assertEquals("http://example.test/a.png", att.getUrl());
        assertArrayEquals(java.util.Base64.getDecoder().decode("abc"), att.getData());
    }

    // ---------- DV_TEXT / STRING ----------

    @Test
    void parse_string_returnsNullWhenEmptyAndCanBeNullTrue() {
        JsonObject json = new JsonObject();
        // no value for "s"

        var out = parser.parse(
                List.of("s"),
                FhirConnectConst.DV_TEXT,
                json,
                true,
                "Patient",
                "Patient.name.text"
        );

        assertNull(out);
    }

    @Test
    void parse_string_returnsEmptyStringTypeWhenEmptyAndCanBeNullFalse() {
        JsonObject json = new JsonObject();
        // no value for "s"

        var out = parser.parse(
                List.of("s"),
                FhirConnectConst.DV_TEXT,
                json,
                false,
                "Patient",
                "Patient.name.text"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof StringType);
        assertNull(((StringType) out.getData()).getValue()); // empty StringType
    }

    // ---------- CODING (CODE_PHRASE / CODING) ----------

    @Test
    void parse_coding_stripsVersionFromSystem_and_setsVersion() {
        JsonObject json = new JsonObject();
        json.addProperty("c|terminology", "http://loinc.org (2.73)");
        json.addProperty("c|code", "1234-5");
        json.addProperty("c|value", "Some display");

        var out = parser.parse(
                List.of("c|terminology", "c|code", "c|value"),
                CODE_PHRASE,
                json,
                false,
                "Observation",
                "Observation.code.coding"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof Coding);

        Coding coding = (Coding) out.getData();
        assertEquals("http://loinc.org", coding.getSystem());
        assertEquals("2.73", coding.getVersion());
        assertEquals("1234-5", coding.getCode());
        assertEquals("Some display", coding.getDisplay());
    }

    // ---------- CODEABLECONCEPT ----------

    @Test
    void parse_codeableConcept_setsPrimaryCoding_andAddsMappings() {
        JsonObject json = new JsonObject();

        // base coded term
        json.addProperty("cc|value", "Primary Display");
        json.addProperty("cc|code", "A");
        json.addProperty("cc|terminology", "http://snomed.info/sct (2023-09)");

        // mapping 0
        json.addProperty("cc/_mapping:0/target|terminology", "http://loinc.org (2.73)");
        json.addProperty("cc/_mapping:0/target|code", "1234-5");
        json.addProperty("cc/_mapping:0/target|preferred_term", "Mapped Display");

        var out = parser.parse(
                List.of("cc"),
                "CODEABLECONCEPT",
                json,
                false,
                "Observation",
                "Observation.code"
        );

        assertNotNull(out);
        assertTrue(out.getData() instanceof CodeableConcept);

        CodeableConcept cc = (CodeableConcept) out.getData();
        assertEquals("Primary Display", cc.getText());
        assertTrue(cc.getCoding().size() >= 2);

        // primary coding
        Coding primary = cc.getCodingFirstRep();
        assertEquals("http://snomed.info/sct", primary.getSystem());
        assertEquals("2023-09", primary.getVersion());
        assertEquals("A", primary.getCode());
        assertEquals("Primary Display", primary.getDisplay());

        // mapped coding exists
        boolean foundMapped = cc.getCoding().stream().anyMatch(c ->
                "http://loinc.org".equals(c.getSystem())
                        && "1234-5".equals(c.getCode())
                        && "Mapped Display".equals(c.getDisplay())
        );
        assertTrue(foundMapped, "Expected mapping coding to be added");
    }

    // ---------------------------------------------------------------------
    // Minimal stubs so tests compile and run in isolation
    // ---------------------------------------------------------------------

    /**
     * Only method used by parser: getLastIndex(path).
     * We return a constant so assertions don’t depend on your index logic here.
     */
    private static class OpenFhirStringUtilsStub extends OpenFhirStringUtils {
        @Override
        public Integer getLastIndex(String path) {
            return 0;
        }
    }

    /**
     * Only method used by parser: stringToDate(...)
     */
    private static class OpenFhirMapperUtilsStub extends OpenFhirMapperUtils {
        @Override
        public Date stringToDate(String s) {
            if (s == null) return null;

            // supports "YYYY-MM-DD" and ISO instant
            if (s.length() == 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
                // DateType accepts "YYYY-MM-DD" via setValueAsString, but parser calls stringToDate for DV_DATE too.
                // We'll parse as midnight UTC for testing.
                return Date.from(Instant.parse(s + "T00:00:00Z"));
            }
            return Date.from(Instant.parse(s));
        }
    }
}
