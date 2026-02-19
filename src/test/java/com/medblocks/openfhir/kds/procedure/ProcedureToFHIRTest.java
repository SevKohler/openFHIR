package com.medblocks.openfhir.kds.procedure;

import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.composition.Composition;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.Assert;
import org.junit.Test;

public class ProcedureToFHIRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/procedure/procedure.context.yaml";
    final String HELPER_LOCATION = "/kds/procedure/";
    final String OPT = "/kds/procedure/KDS_Prozedur.opt";

    final String[] OPENEHR_COMPOSITIONS = {
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-3.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-3.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-2.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-3.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-prozedur-1.json",
            "/kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-prozedur-2.json"
    };

    final String[] FHIR_BUNDLES = {
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-1-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-1-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-10-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-10-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-3.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-3.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-4-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-4-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-5-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-5-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-6-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-6-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-7-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-7-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-2.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-3.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-9-prozedur-1.json",
            "/kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-9-prozedur-2.json"    };

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

    /**
     * Input: /kds/procedure/toOpenEHR/output/KDS_Prozedur.flat.json
     * Expected: Mapped Procedure fields in resulting Bundle
     */
    @Test
    public void assertToFHIRLegacyDetailed() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(getFile("/kds/procedure/toOpenEHR/output/KDS_Prozedur.flat.json"),
                new OPTParser(operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allProcedures = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof Procedure).collect(Collectors.toList());
        Assert.assertEquals(1, allProcedures.size());

        final Procedure theProcedure = (Procedure) allProcedures.get(0).getResource();

        Assert.assertEquals("2022-02-03T04:05:06+01:00",
                theProcedure.getPerformedDateTimeType().getValueAsString());
        Assert.assertEquals("80146002", theProcedure.getCode().getCodingFirstRep().getCode());
        Assert.assertEquals("//fhir.hl7.org/ValueSet/$expand?url=http://fhir.de/ValueSet/bfarm/ops",
                theProcedure.getCode().getCodingFirstRep().getSystem());
        Assert.assertEquals("Procedure completed successfully with no complications.",
                theProcedure.getNoteFirstRep().getText());
        Assert.assertEquals("Diagnostic procedure", theProcedure.getCategory().getText());
        Assert.assertEquals("103693007", theProcedure.getCategory().getCodingFirstRep().getCode());
        Assert.assertEquals("Abdomen", theProcedure.getBodySite().get(0).getText());
        Assert.assertEquals("818981001", theProcedure.getBodySite().get(0).getCodingFirstRep().getCode());

        final Extension durchuhrungsabsicht = theProcedure.getExtensionByUrl(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-prozedur/StructureDefinition/Durchfuehrungsabsicht");
        Assert.assertNotNull(durchuhrungsabsicht);
        Assert.assertEquals("durchführungsabsicht", ((Coding) durchuhrungsabsicht.getValue()).getCode());
        Assert.assertEquals("valuedurchführungsabsicht", ((Coding) durchuhrungsabsicht.getValue()).getDisplay());
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-1-prozedur-1.json
     */
    @Test
    public void assertToFHIR_1() {
        assertToFHIR(0);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-1-prozedur-2.json
     */
    @Test
    public void assertToFHIR_2() {
        assertToFHIR(1);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-10-prozedur-1.json
     */
    @Test
    public void assertToFHIR_3() {
        assertToFHIR(2);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-10-prozedur-2.json
     */
    @Test
    public void assertToFHIR_4() {
        assertToFHIR(3);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-1.json
     */
    @Test
    public void assertToFHIR_5() {
        assertToFHIR(4);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-2.json
     */
    @Test
    public void assertToFHIR_6() {
        assertToFHIR(5);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-prozedur-3.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-2-prozedur-3.json
     */
    @Test
    public void assertToFHIR_7() {
        assertToFHIR(6);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-1.json
     */
    @Test
    public void assertToFHIR_8() {
        assertToFHIR(7);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-2.json
     */
    @Test
    public void assertToFHIR_9() {
        assertToFHIR(8);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-prozedur-3.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-3-prozedur-3.json
     */
    @Test
    public void assertToFHIR_10() {
        assertToFHIR(9);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-4-prozedur-1.json
     */
    @Test
    public void assertToFHIR_11() {
        assertToFHIR(10);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-4-prozedur-2.json
     */
    @Test
    public void assertToFHIR_12() {
        assertToFHIR(11);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-5-prozedur-1.json
     */
    @Test
    public void assertToFHIR_13() {
        assertToFHIR(12);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-5-prozedur-2.json
     */
    @Test
    public void assertToFHIR_14() {
        assertToFHIR(13);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-6-prozedur-1.json
     */
    @Test
    public void assertToFHIR_15() {
        assertToFHIR(14);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-6-prozedur-2.json
     */
    @Test
    public void assertToFHIR_16() {
        assertToFHIR(15);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-7-prozedur-1.json
     */
    @Test
    public void assertToFHIR_17() {
        assertToFHIR(16);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-7-prozedur-2.json
     */
    @Test
    public void assertToFHIR_18() {
        assertToFHIR(17);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-1.json
     */
    @Test
    public void assertToFHIR_19() {
        assertToFHIR(18);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-2.json
     */
    @Test
    public void assertToFHIR_20() {
        assertToFHIR(19);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-prozedur-3.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-8-prozedur-3.json
     */
    @Test
    public void assertToFHIR_21() {
        assertToFHIR(20);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-prozedur-1.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-9-prozedur-1.json
     */
    @Test
    public void assertToFHIR_22() {
        assertToFHIR(21);
    }

    /**
     * Input: /kds/procedure/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-prozedur-2.json
     * Expected: /kds/procedure/toFHIR/output/Procedure-mii-exa-test-data-patient-9-prozedur-2.json
     */
    @Test
    public void assertToFHIR_23() {
        assertToFHIR(22);
    }

}
