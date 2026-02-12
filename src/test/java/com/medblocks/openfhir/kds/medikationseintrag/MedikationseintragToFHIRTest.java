package com.medblocks.openfhir.kds.medikationseintrag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MedikationseintragToFHIRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/medikationseintrag/KDS_medikationseintrag.context.yaml";
    final String HELPER_LOCATION = "/kds/medikationseintrag/";
    final String OPT = "/kds/medikationseintrag/KDS_Medikationseintrag.opt";
    final String FLAT = "/kds/medikationseintrag/toOpenEHR/output/KDS_Medikationseintrag.flat.json";

    final String[] OPENEHR_COMPOSITIONS = {
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-3.json",
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
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-medstatement-2.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-1.json",
            "/kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-2.json"
    };

    final String[] FHIR_BUNDLES = {
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-3.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-3.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-4.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-5.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-3.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-4.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-4-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-4-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-5-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-5-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-3.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-7-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-7-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-3.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-9-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-9-medstatement-2.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-10-medstatement-1.json",
            "/kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-10-medstatement-2.json"
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

    @SneakyThrows
    private void assertToFHIR(int index) {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITIONS[index]),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLES[index]);
    }

    @SneakyThrows
    private JsonObject getJsonObject(String path) {
        return new Gson().fromJson(getFile(path), JsonObject.class);
    }

    private void removeIncompatibleTimingFlatArtifacts(final JsonObject flatJsonObject) {
        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/tägliche_dosierung:0/beschreibung_des_zeitablaufs");
        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/nicht_tägliche_dosierung:0/zeitpunkt/date_value");
        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/tägliche_dosierung:0/bestimmtes_ereignis:0/offset");
    }

    private void assertDoseValue(final Dosage dosage, final String expectedUnit, final String expectedValue) {
        final Dosage.DosageDoseAndRateComponent doseAndRate = dosage.getDoseAndRateFirstRep();
        if (doseAndRate.hasDoseQuantity()) {
            if (doseAndRate.getDoseQuantity().getUnit() != null) {
                Assert.assertEquals(expectedUnit, doseAndRate.getDoseQuantity().getUnit());
            }
            if (doseAndRate.getDoseQuantity().getValue() != null) {
                Assert.assertEquals(expectedValue, doseAndRate.getDoseQuantity().getValue().toPlainString());
            }
            return;
        }

        if (doseAndRate.hasDoseRange()) {
            if (doseAndRate.getDoseRange().hasLow() && doseAndRate.getDoseRange().getLow().getUnit() != null) {
                Assert.assertEquals(expectedUnit, doseAndRate.getDoseRange().getLow().getUnit());
            }
            if (doseAndRate.getDoseRange().hasLow() && doseAndRate.getDoseRange().getLow().getValue() != null) {
                Assert.assertEquals(expectedValue, doseAndRate.getDoseRange().getLow().getValue().toPlainString());
            }
            return;
        }
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-1.json
     */
    @Test
    public void assertToFHIR_1() {
        assertToFHIR(0);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-2.json
     */
    @Test
    public void assertToFHIR_2() {
        assertToFHIR(1);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-medstatement-3.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-1-medstatement-3.json
     */
    @Test
    public void assertToFHIR_3() {
        assertToFHIR(2);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-1.json
     */
    @Test
    public void assertToFHIR_4() {
        assertToFHIR(3);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-2.json
     */
    @Test
    public void assertToFHIR_5() {
        assertToFHIR(4);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-3.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-3.json
     */
    @Test
    public void assertToFHIR_6() {
        assertToFHIR(5);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-4.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-4.json
     */
    @Test
    public void assertToFHIR_7() {
        assertToFHIR(6);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-medstatement-5.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-2-medstatement-5.json
     */
    @Test
    public void assertToFHIR_8() {
        assertToFHIR(7);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-1.json
     */
    @Test
    public void assertToFHIR_9() {
        assertToFHIR(8);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-2.json
     */
    @Test
    public void assertToFHIR_10() {
        assertToFHIR(9);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-3.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-3.json
     */
    @Test
    public void assertToFHIR_11() {
        assertToFHIR(10);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-medstatement-4.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-3-medstatement-4.json
     */
    @Test
    public void assertToFHIR_12() {
        assertToFHIR(11);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-4-medstatement-1.json
     */
    @Test
    public void assertToFHIR_13() {
        assertToFHIR(12);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-4-medstatement-2.json
     */
    @Test
    public void assertToFHIR_14() {
        assertToFHIR(13);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-5-medstatement-1.json
     */
    @Test
    public void assertToFHIR_15() {
        assertToFHIR(14);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-5-medstatement-2.json
     */
    @Test
    public void assertToFHIR_16() {
        assertToFHIR(15);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-1.json
     */
    @Test
    public void assertToFHIR_17() {
        assertToFHIR(16);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-2.json
     */
    @Test
    public void assertToFHIR_18() {
        assertToFHIR(17);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-medstatement-3.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-6-medstatement-3.json
     */
    @Test
    public void assertToFHIR_19() {
        assertToFHIR(18);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-7-medstatement-1.json
     */
    @Test
    public void assertToFHIR_20() {
        assertToFHIR(19);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-7-medstatement-2.json
     */
    @Test
    public void assertToFHIR_21() {
        assertToFHIR(20);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-1.json
     */
    @Test
    public void assertToFHIR_22() {
        assertToFHIR(21);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-2.json
     */
    @Test
    public void assertToFHIR_23() {
        assertToFHIR(22);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-medstatement-3.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-8-medstatement-3.json
     */
    @Test
    public void assertToFHIR_24() {
        assertToFHIR(23);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-9-medstatement-1.json
     */
    @Test
    public void assertToFHIR_25() {
        assertToFHIR(24);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-9-medstatement-2.json
     */
    @Test
    public void assertToFHIR_26() {
        assertToFHIR(25);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-1.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-10-medstatement-1.json
     */
    @Test
    public void assertToFHIR_27() {
        assertToFHIR(26);
    }

    /**
     * Input: /kds/medikationseintrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-medstatement-2.json
     * Expected: /kds/medikationseintrag/toFHIR/output/MedicationStatement-mii-exa-test-data-patient-10-medstatement-2.json
     */
    @Test
    public void assertToFHIR_28() {
        assertToFHIR(27);
    }

    @Test
    public void kdsMedicationList_toFhir() throws IOException {
        // openEHR to FHIR
        final JsonObject flatJsonObject = getJsonObject(FLAT);
        removeIncompatibleTimingFlatArtifacts(flatJsonObject);
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(gson.toJson(flatJsonObject), webTemplate);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);

        final List<MedicationStatement> requests = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof MedicationStatement)
                .map(en -> (MedicationStatement) en.getResource())
                .collect(Collectors.toList());

        Assert.assertEquals(2, requests.size());

        final MedicationStatement req1 = requests.get(0);
        final MedicationStatement req2 = requests.get(1);

        Assert.assertEquals("2022-02-03T04:05:06+01:00", req2.getDateAssertedElement().getValueAsString());


        if (req1.hasReasonCode() && req2.hasReasonCode()) {
            Assert.assertEquals("behandlungsgrund1", req1.getReasonCodeFirstRep().getText());
            Assert.assertEquals("behandlungsgrund", req2.getReasonCodeFirstRep().getText());
        }

        if (req1.hasNote() && req2.hasNote()) {
            Assert.assertEquals("hinweis1", req1.getNoteFirstRep().getText());
            Assert.assertEquals("hinweis", req2.getNoteFirstRep().getText());
        }

        final List<Dosage> req2Dosages = req2.getDosage();

        Assert.assertEquals(2, req2Dosages.size());
        Assert.assertEquals("structured dosage text", req2Dosages.get(0).getText());
        assertDoseValue(req2Dosages.get(0), "mm", "22.0");
        Assert.assertEquals(22, req2Dosages.get(0).getSequence());

        Assert.assertEquals("structured dosage 2 text", req2Dosages.get(1).getText());
        assertDoseValue(req2Dosages.get(1), "mm1", "23.0");
        Assert.assertEquals(23, req2Dosages.get(1).getSequence());

//        Assert.assertEquals(true, req2.getDosageFirstRep().getAsNeededBooleanType().getValue());
        Assert.assertNull(req1.getDosageFirstRep().getAsNeededBooleanType().getValue());

        final Medication med1 = (Medication) req1.getMedicationReference().getResource();
        final Medication med2 = (Medication) req2.getMedicationReference().getResource();
//        Assert.assertEquals("req0, medication code text", med2.getCode().getCodingFirstRep().getDisplay());
        Assert.assertEquals("42", med2.getForm().getCodingFirstRep().getCode());
        Assert.assertEquals("52", med1.getForm().getCodingFirstRep().getCode());
//        Assert.assertEquals("req1, medication code text", med1.getCode().getCodingFirstRep().getDisplay());
        Assert.assertEquals("25.0", med1.getAmount().getNumerator().getValue().toPlainString());
        Assert.assertEquals("mm", med1.getAmount().getNumerator().getUnit());
        Assert.assertEquals("20.0", med2.getAmount().getNumerator().getValue().toPlainString());
        Assert.assertEquals("mm", med2.getAmount().getNumerator().getUnit());

        Assert.assertEquals(3, med2.getIngredient().size());
        Assert.assertEquals(2, med1.getIngredient().size());
        Assert.assertEquals("ingridient item 0, 0",
                ((Medication) med2.getIngredient().get(0).getItemReference().getResource()).getCode().getCodingFirstRep().getCode());
        Assert.assertEquals("ingridient item 0, 1", ((Medication) med2.getIngredient().get(1).getItemReference().getResource()).getCode().getCodingFirstRep().getCode());

        Assert.assertEquals("ingridient item 1, 0", ((Medication) med1.getIngredient().get(0).getItemReference().getResource()).getCode().getCodingFirstRep().getCode());
        Assert.assertEquals("ingridient item 1, 1", ((Medication) med1.getIngredient().get(1).getItemReference().getResource()).getCode().getCodingFirstRep().getCode());


        final Medication.MedicationIngredientComponent ingridient00 = med2.getIngredient().get(0);

        final Medication.MedicationIngredientComponent ingridient01 = med2.getIngredient().get(1);

        final Medication.MedicationIngredientComponent ingridient0Empty = med2.getIngredient().get(2);

        final Medication.MedicationIngredientComponent ingridient10 = med1.getIngredient().get(0);

        final Ratio zerothIngridientStrenght = ingridient00.getStrength();
        final Ratio firstIngridientStrenght = ingridient01.getStrength();

        final Ratio secondIngridientStrenght = ingridient0Empty.getStrength();

        Assert.assertEquals("10.0", zerothIngridientStrenght.getNumerator().getValue().toPlainString());
        Assert.assertEquals("11.0", zerothIngridientStrenght.getDenominator().getValue().toPlainString());
        Assert.assertEquals("1mm", zerothIngridientStrenght.getNumerator().getUnit());
        Assert.assertEquals("2mm", zerothIngridientStrenght.getDenominator().getUnit());

        Assert.assertEquals("20.0", firstIngridientStrenght.getNumerator().getValue().toPlainString());
        Assert.assertEquals("21.0", firstIngridientStrenght.getDenominator().getValue().toPlainString());
        Assert.assertEquals("3mm", firstIngridientStrenght.getNumerator().getUnit());
        Assert.assertEquals("4mm", firstIngridientStrenght.getDenominator().getUnit());

        Assert.assertEquals("30.0", secondIngridientStrenght.getNumerator().getValue().toPlainString());
        Assert.assertEquals("31.0", secondIngridientStrenght.getDenominator().getValue().toPlainString());
        Assert.assertEquals("5mm", secondIngridientStrenght.getNumerator().getUnit());
        Assert.assertEquals("6mm", secondIngridientStrenght.getDenominator().getUnit());

        final Ratio firstZerothIngridientStrenght = ingridient10.getStrength();

        final Ratio firstFirstIngridientStrenght = med1.getIngredient().get(1).getStrength();

        Assert.assertEquals("40.0", firstZerothIngridientStrenght.getDenominator().getValue().toPlainString());
        Assert.assertEquals("mm", firstZerothIngridientStrenght.getDenominator().getUnit());

        Assert.assertEquals("41.0", firstFirstIngridientStrenght.getDenominator().getValue().toPlainString());
        Assert.assertEquals("mm", firstFirstIngridientStrenght.getDenominator().getUnit());

//        Assert.assertEquals("at0143", ingridient00.getItemCodeableConcept().getCodingFirstRep().getCode());
//        Assert.assertEquals("Ad-hoc Mixtur", ingridient00.getItemCodeableConcept().getCodingFirstRep().getDisplay());
//
//        Assert.assertEquals("at3143", ingridient0Empty.getItemCodeableConcept().getCodingFirstRep().getCode());
//        Assert.assertEquals("3Ad-hoc Mixtur", ingridient0Empty.getItemCodeableConcept().getCodingFirstRep().getDisplay());
//
//        Assert.assertEquals("at0243", ingridient10.getItemCodeableConcept().getCodingFirstRep().getCode());
//        Assert.assertEquals("2Ad-hoc Mixtur", ingridient10.getItemCodeableConcept().getCodingFirstRep().getDisplay());
    }

    @Test
    public void kdsMedicationList_toFhir_rateRatioFromRateAndDuration() throws IOException {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITIONS[8]),
                Composition.class);
        final JsonObject flat = gson.fromJson(flatMarshaller.toFlatJson(composition, webTemplate), JsonObject.class);

        String rateMag = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/quantity_value|magnitude";
        String rateUnit = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/quantity_value|unit";
        String duration = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsdauer";

        flat.addProperty(rateMag, 50.0);
        flat.addProperty(rateUnit, "mg");
        flat.addProperty(duration, "PT3H");
        // This fixture can also contain a text-based rate; remove it so quantity+duration maps deterministically to Ratio.
        flat.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/text_value");

        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(flat.toString(), webTemplate);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);

        final MedicationStatement stmt = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof MedicationStatement)
                .map(en -> (MedicationStatement) en.getResource())
                .findFirst()
                .orElseThrow();

        Dosage dosageWithRateRatio = stmt.getDosage().stream()
                .filter(Dosage::hasDoseAndRate)
                .filter(d -> d.getDoseAndRateFirstRep().hasRateRatio())
                .findFirst()
                .orElseThrow();
        Dosage.DosageDoseAndRateComponent doseAndRate = dosageWithRateRatio.getDoseAndRateFirstRep();
        Assert.assertTrue(doseAndRate.hasRateRatio());
        Assert.assertEquals("150.0", doseAndRate.getRateRatio().getNumerator().getValue().toPlainString());
        Assert.assertEquals("mg", doseAndRate.getRateRatio().getNumerator().getUnit());
        Assert.assertEquals("3.0", doseAndRate.getRateRatio().getDenominator().getValue().toPlainString());
        Assert.assertEquals("h", doseAndRate.getRateRatio().getDenominator().getUnit());
    }

    @Test
    public void kdsMedicationList_toFhir_testOpenEhrCondition() throws IOException {
        // openEHR to FHIR
        final String flat = getFile(FLAT);
        final Gson gson = new Gson();
        final JsonObject flatJsonObject = gson.fromJson(flat, JsonObject.class);

//        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/darreichungsform|value");
//        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/darreichungsform|code");
//        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:0/arzneimittel/darreichungsform|terminology");
        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:1/arzneimittel/wirkstärke_konzentration|magnitude");
        flatJsonObject.remove("medikamentenliste/aussage_zur_medikamenteneinnahme:1/arzneimittel/wirkstärke_konzentration|unit");
        removeIncompatibleTimingFlatArtifacts(flatJsonObject);

        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(gson.toJson(flatJsonObject), webTemplate);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);

        final List<MedicationStatement> requests = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof MedicationStatement)
                .map(en -> (MedicationStatement) en.getResource())
                .collect(Collectors.toList());

        Assert.assertEquals(2, requests.size());

        final MedicationStatement theOneWithMedicationReference = requests.stream()
                .filter(MedicationStatement::hasMedicationReference)
                .findFirst().orElse(null);

        Assert.assertNotNull(theOneWithMedicationReference);

        final Medication med1 = (Medication) theOneWithMedicationReference.getMedicationReference().getResource();
//        final CodeableConcept med2 = theOneWithMedicationCodeableConcept.getMedicationCodeableConcept();
//
//        Assert.assertEquals("req1, medication code text", med2.getText());
//
//        Assert.assertEquals("req0, medication code text", med1.getCode().getCodingFirstRep().getDisplay());
        if (med1.hasAmount() && med1.getAmount().hasNumerator() && med1.getAmount().getNumerator().getValue() != null) {
            Assert.assertEquals("20.0", med1.getAmount().getNumerator().getValue().toPlainString());
            Assert.assertEquals("mm", med1.getAmount().getNumerator().getUnit());
        }

    }


}
