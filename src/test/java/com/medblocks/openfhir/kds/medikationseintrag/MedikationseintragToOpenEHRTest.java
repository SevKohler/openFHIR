package com.medblocks.openfhir.kds.medikationseintrag;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.JsonObject;
import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.Test;

public class MedikationseintragToOpenEHRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/medikationseintrag/KDS_medikationseintrag.context.yaml";
    final String HELPER_LOCATION = "/kds/medikationseintrag/";
    final String OPT = "/kds/medikationseintrag/KDS_Medikationseintrag.opt";
    final String BUNDLE = "KDS_Medikationseintrag_v1-Fhir-Bundle-input.json";

    final String[] FHIR_INPUTS = {
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-1-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-1-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-1-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-10-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-10-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-4.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-5.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-3-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-3-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-3-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-3-medstatement-4.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-4-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-4-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-5-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-5-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-6-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-6-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-6-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-7-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-7-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-8-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-8-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-8-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-9-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-9-medstatement-2.json"
    };

    final String[] OPENEHR_OUTPUTS = {
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-4.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-5.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-4.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-3.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-medstatement-2.json"
    };

    @SneakyThrows
    @Override
    public void prepareState() {
        context = getContext(CONTEXT);
        operationaltemplateSerialized = IOUtils.toString(this.getClass().getResourceAsStream(OPT));
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }


    private void assertToOpenEHR(int index) {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_INPUTS[index]), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_OUTPUTS[index], operationaltemplate);
    }

    @Test
    public void assertToOpenEHR_1() {
        assertToOpenEHR(0);
    }

    @Test
    public void assertToOpenEHR_2() {
        assertToOpenEHR(1);
    }

    @Test
    public void assertToOpenEHR_3() {
        assertToOpenEHR(2);
    }

    @Test
    public void assertToOpenEHR_4() {
        assertToOpenEHR(3);
    }

    @Test
    public void assertToOpenEHR_5() {
        assertToOpenEHR(4);
    }

    @Test
    public void assertToOpenEHR_6() {
        assertToOpenEHR(5);
    }

    @Test
    public void assertToOpenEHR_7() {
        assertToOpenEHR(6);
    }

    @Test
    public void assertToOpenEHR_8() {
        assertToOpenEHR(7);
    }

    @Test
    public void assertToOpenEHR_9() {
        assertToOpenEHR(8);
    }

    @Test
    public void assertToOpenEHR_10() {
        assertToOpenEHR(9);
    }

    @Test
    public void assertToOpenEHR_11() {
        assertToOpenEHR(10);
    }

    @Test
    public void assertToOpenEHR_12() {
        assertToOpenEHR(11);
    }

    @Test
    public void assertToOpenEHR_13() {
        assertToOpenEHR(12);
    }

    @Test
    public void assertToOpenEHR_14() {
        assertToOpenEHR(13);
    }

    @Test
    public void assertToOpenEHR_15() {
        assertToOpenEHR(14);
    }

    @Test
    public void assertToOpenEHR_16() {
        assertToOpenEHR(15);
    }

    @Test
    public void assertToOpenEHR_17() {
        assertToOpenEHR(16);
    }

    @Test
    public void assertToOpenEHR_18() {
        assertToOpenEHR(17);
    }

    @Test
    public void assertToOpenEHR_19() {
        assertToOpenEHR(18);
    }

    @Test
    public void assertToOpenEHR_20() {
        assertToOpenEHR(19);
    }

    @Test
    public void assertToOpenEHR_21() {
        assertToOpenEHR(20);
    }

    @Test
    public void assertToOpenEHR_22() {
        assertToOpenEHR(21);
    }

    @Test
    public void assertToOpenEHR_23() {
        assertToOpenEHR(22);
    }

    @Test
    public void assertToOpenEHR_24() {
        assertToOpenEHR(23);
    }

    @Test
    public void assertToOpenEHR_25() {
        assertToOpenEHR(24);
    }

    @Test
    public void assertToOpenEHR_26() {
        assertToOpenEHR(25);
    }

    @Test
    public void assertToOpenEHR_27() {
        assertToOpenEHR(26);
    }

    @Test
    public void assertToOpenEHR_28() {
        assertToOpenEHR(27);
    }



    public JsonObject toOpenEhr() {
        final Bundle testBundle = FhirContext.forR4().newJsonParser().parseResource(Bundle.class, getClass().getResourceAsStream(HELPER_LOCATION + BUNDLE));

        final JsonObject jsonObject = fhirToOpenEhr.fhirToFlatJsonObject(context, testBundle, operationaltemplate);

        Assert.assertEquals("Take 1 tablet every 6 hours as needed for pain", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/dosierung_freitext").getAsString());
        Assert.assertEquals("500.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/dosis/quantity_value|magnitude").getAsString());
        Assert.assertEquals("mg", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/dosis/quantity_value|unit").getAsString());
        Assert.assertEquals("at0143", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/arzneimittel-name|code").getAsString());
        Assert.assertEquals("local", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/arzneimittel-name|terminology").getAsString());
        Assert.assertEquals("Paracetamol 500mg tablet", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/arzneimittel-name|value").getAsString());
        Assert.assertEquals("385055001", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/darreichungsform|code").getAsString());
        Assert.assertEquals("http://snomed.info/sct", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/darreichungsform|terminology").getAsString());
        Assert.assertEquals("Paracetamol", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/bestandteil").getAsString());
        Assert.assertEquals("at0143", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/wirkstofftyp|code").getAsString());
        Assert.assertEquals("local", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/wirkstofftyp|terminology").getAsString());
        Assert.assertEquals("500.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/bestandteil-menge/zähler|magnitude").getAsString());
        Assert.assertEquals("mg", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/bestandteil-menge/zähler|unit").getAsString());
        Assert.assertEquals("1.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/bestandteil-menge/nenner|magnitude").getAsString());
        Assert.assertEquals("tablet", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:0/bestandteil-menge/nenner|unit").getAsString());
        Assert.assertEquals("11Paracetamol", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/bestandteil").getAsString());
        Assert.assertEquals("at0143", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/wirkstofftyp|code").getAsString());
        Assert.assertEquals("local", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/wirkstofftyp|terminology").getAsString());
        Assert.assertEquals("1500.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/bestandteil-menge/zähler|magnitude").getAsString());
        Assert.assertEquals("mg", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/bestandteil-menge/zähler|unit").getAsString());
        Assert.assertEquals("11.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/bestandteil-menge/nenner|magnitude").getAsString());
        Assert.assertEquals("tablet", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:1/bestandteil-menge/nenner|unit").getAsString());
        Assert.assertEquals("at0143", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:2/wirkstofftyp|code").getAsString());
        Assert.assertEquals("local", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/bestandteil:2/wirkstofftyp|terminology").getAsString());
        Assert.assertEquals("Take 1 capsule daily", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/dosierung:0/dosierung_freitext").getAsString());
        Assert.assertEquals("5.0", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/dosierung:0/dosis/quantity_value|magnitude").getAsString());
        Assert.assertEquals("mg", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/dosierung:0/dosis/quantity_value|unit").getAsString());
        Assert.assertEquals("local", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/arzneimittel/arzneimittel-name|terminology").getAsString());
        Assert.assertEquals("C09AA05", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/arzneimittel/arzneimittel-name|code").getAsString());
        Assert.assertEquals("Ramipril 5mg capsule", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:1/arzneimittel/arzneimittel-name|value").getAsString());

        Assert.assertEquals("High cholesterol", jsonObject.getAsJsonPrimitive("medikamentenliste/aussage_zur_medikamenteneinnahme:0/behandlungsgrund:0").getAsString());

        return jsonObject;
    }

}
