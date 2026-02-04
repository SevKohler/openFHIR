package com.medblocks.openfhir.kds.laborbericht;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.Test;

public class LaborberichtToOpenEHRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/laborbericht/KDS_laborbericht.context.yaml";
    final String HELPER_LOCATION = "/kds/laborbericht/";
    final String OPT = "/kds/laborbericht/KDS_Laborbericht.opt";
    final String BUNDLE = "/kds/laborbericht/toOpenEHR/input/KDS_Laborbericht_bundle.json";
    final String FHIR_DIAGNOSTIC_REPORT_1 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-1-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_2 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-2-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_3 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-3-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_4 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-4-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_5 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-5-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_6 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-6-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_7 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-7-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_8 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-8-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_9 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-9-labreport-1.json";
    final String FHIR_DIAGNOSTIC_REPORT_10 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-10-labreport-1.json";

    final String OPENEHR_COMPOSITION_1 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-labreport-1.json";
    final String OPENEHR_COMPOSITION_2 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-labreport-1.json";
    final String OPENEHR_COMPOSITION_3 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-labreport-1.json";
    final String OPENEHR_COMPOSITION_4 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-labreport-1.json";
    final String OPENEHR_COMPOSITION_5 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-labreport-1.json";
    final String OPENEHR_COMPOSITION_6 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-labreport-1.json";
    final String OPENEHR_COMPOSITION_7 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-labreport-1.json";
    final String OPENEHR_COMPOSITION_8 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-labreport-1.json";
    final String OPENEHR_COMPOSITION_9 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-labreport-1.json";
    final String OPENEHR_COMPOSITION_10 = "/kds/laborbericht/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-labreport-1.json";

    @SneakyThrows
    @Override
    public void prepareState() {
        context = getContext(CONTEXT);
        operationaltemplateSerialized = IOUtils.toString(this.getClass().getResourceAsStream(OPT));
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }


    @Test
    public void assertToOpenEHR1() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_1), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_1, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR2() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_2), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_2, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR3() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_3), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_3, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR4() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_4), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_4, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR5() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_5), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_5, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR6() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_6), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_6, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR7() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_7), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_7, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR8() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_8), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_8, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR9() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_9), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_9, operationaltemplate);
    }

    @Test
    public void assertToOpenEHR10() {
        final Composition composition =
                fhirToOpenEhr.fhirToCompositionRm(context, getTestBundle(FHIR_DIAGNOSTIC_REPORT_10), operationaltemplate);
        standardsAsserter.assertComposition(composition, OPENEHR_COMPOSITION_10, operationaltemplate);
    }

    @Test
    public void toOpenEhr() {
        final Bundle testBundle = getTestBundle(BUNDLE);
        final JsonObject jsonObject = fhirToOpenEhr.fhirToFlatJsonObject(context, testBundle, operationaltemplate);

        Assert.assertEquals("registered", jsonObject.getAsJsonPrimitive("laborbericht/context/status|code").getAsString());
        Assert.assertEquals("Normal blood count",
                            jsonObject.getAsJsonPrimitive("laborbericht/laborbefund/any_event:0/conclusion").getAsString());
        Assert.assertEquals("2022-02-03T05:05:06",
                            jsonObject.getAsJsonPrimitive("laborbericht/laborbefund/any_event:0/time").getAsString());
        Assert.assertEquals("SP-987654", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/probenmaterial:0/external_identifier/identifier_value|id").getAsString());
        Assert.assertEquals("2024-08-24T11:00:00", jsonObject.getAsJsonPrimitive(
                        "laborbericht/laborbefund/any_event:0/probenmaterial:0/collection_date_time/date_time_value")
                .getAsString());
        Assert.assertEquals("1234567", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/probenmaterial:0/specimen_collector_identifier|id").getAsString());
        Assert.assertEquals("122555007",
                            jsonObject.getAsJsonPrimitive("laborbericht/laborbefund/any_event:0/probenmaterial:0/specimen_type|code")
                                    .getAsString());
        Assert.assertEquals("http://snomed.info/sct", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/probenmaterial:0/specimen_type|terminology").getAsString());
        Assert.assertEquals("Venous blood specimen",
                            jsonObject.getAsJsonPrimitive("laborbericht/laborbefund/any_event:0/probenmaterial:0/specimen_type|value")
                                    .getAsString());
        Assert.assertEquals("Sample collected in the morning.",
                            jsonObject.getAsJsonPrimitive("laborbericht/laborbefund/any_event:0/probenmaterial:0/comment")
                                    .getAsString());
        Assert.assertEquals("2022-02-03T05:05:06", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/probenmaterial:0/date_time_received").getAsString());
        Assert.assertEquals("at0062", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/probenmaterial:0/adequacy_for_testing|code").getAsString());
        Assert.assertEquals("2022-02-03T05:05:06", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/result_status_time").getAsString());
        Assert.assertEquals("7.4", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/quantity_value|magnitude").getAsString());
        Assert.assertEquals("g/dL", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/quantity_value|unit").getAsString());
        Assert.assertEquals("718-7", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/analyte_name|code").getAsString());
        Assert.assertEquals("http://loinc.org", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/analyte_name|terminology").getAsString());
        Assert.assertEquals("Hemoglobin [Mass/volume] in Blood", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/analyte_name|value").getAsString());
        Assert.assertEquals("H", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/interpretation|code").getAsString());
        Assert.assertEquals("http://hl7.org/fhir/ValueSet/observation-interpretation", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/interpretation|terminology").getAsString());
        Assert.assertEquals("Interpretation description", jsonObject.getAsJsonPrimitive(
                "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/interpretation|value").getAsString());
        Assert.assertEquals("FILL-12345",
                            jsonObject.getAsJsonPrimitive("laborbericht/context/id").getAsString());
    }

}
