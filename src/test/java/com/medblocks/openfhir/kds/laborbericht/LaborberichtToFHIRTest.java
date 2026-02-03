package com.medblocks.openfhir.kds.laborbericht;

import static org.junit.Assert.assertEquals;

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
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.Assert;
import org.junit.Test;

public class LaborberichtToFHIRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/laborbericht/KDS_laborbericht.context.yaml";
    final String HELPER_LOCATION = "/kds/laborbericht/";
    final String OPT = "/kds/laborbericht/KDS_Laborbericht.opt";
    final String FLAT = "/kds/laborbericht/toOpenEHR/output/KDS_Laborbericht.flat.json";
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

    final String FHIR_BUNDLE_1 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-1-labreport-1.json";
    final String FHIR_BUNDLE_2 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-2-labreport-1.json";
    final String FHIR_BUNDLE_3 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-3-labreport-1.json";
    final String FHIR_BUNDLE_4 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-4-labreport-1.json";
    final String FHIR_BUNDLE_5 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-5-labreport-1.json";
    final String FHIR_BUNDLE_6 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-6-labreport-1.json";
    final String FHIR_BUNDLE_7 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-7-labreport-1.json";
    final String FHIR_BUNDLE_8 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-8-labreport-1.json";
    final String FHIR_BUNDLE_9 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-9-labreport-1.json";
    final String FHIR_BUNDLE_10 = "/kds/laborbericht/toOpenEHR/input/DiagnosticReport-mii-exa-test-data-patient-10-labreport-1.json";

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
    @Test
    public void assertToFHIR1() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_1),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_1);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR2() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_2),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_2);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR3() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_3),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_3);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR4() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_4),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_4);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR5() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_5),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_5);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR6() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_6),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_6);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR7() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_7),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_7);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR8() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_8),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_8);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR9() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_9),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_9);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR10() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_10),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_10);
    }

    @Test
    public void toFhir() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(getFile(FLAT),
                                                                                     new OPTParser(
                                                                                             operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allDiagnosticReports = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof DiagnosticReport).collect(Collectors.toList());
        assertEquals(1, allDiagnosticReports.size());

        final DiagnosticReport diagnosticReport = (DiagnosticReport) allDiagnosticReports.get(0).getResource();

        // - name: "Category"
        assertEquals(1, diagnosticReport.getCategory().size());
        assertEquals("LOINC", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getSystem());
        assertEquals("26436-6", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getCode());
        assertEquals("laboratory", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getDisplay());

        //  - name: "Status"
        assertEquals("at0107", diagnosticReport.getStatusElement().getValueAsString());

        //  - name: "Conclusion"
        assertEquals("Normal blood count", diagnosticReport.getConclusion());

        // - name: "issued"
        assertEquals("2022-02-03T04:05:06.000+01:00", diagnosticReport.getIssuedElement().getValueAsString());

        // - name: "berichtId"
        assertEquals(1, diagnosticReport.getIdentifierFirstRep().getType().getCoding().size());
        assertEquals("FILL", diagnosticReport.getIdentifierFirstRep().getType().getCodingFirstRep().getCode());
        assertEquals("http://terminology.hl7.org/CodeSystem/v2-0203",
                     diagnosticReport.getIdentifierFirstRep().getType().getCodingFirstRep().getSystem());
        assertEquals("bericht_id", diagnosticReport.getIdentifierFirstRep().getValue());

        // Assert Specimen - name: "specimen"
        Specimen specimen = (Specimen) diagnosticReport.getSpecimenFirstRep().getResource();

        // - name: "identifier"
        assertEquals("SP-987654", specimen.getIdentifierFirstRep().getValue());

        //  - name: "collector"
        assertEquals("collectorId", specimen.getCollection().getCollector().getIdentifier().getValue());

        //  - name: "specimen type"
        assertEquals("probenartcode", specimen.getType().getCodingFirstRep().getCode());
        assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0487' available",
                specimen.getType().getText());

        // - name: "type"
        Assert.assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0487' available",
                specimen.getType().getText());
        Assert.assertEquals("probenartcode", specimen.getType().getCodingFirstRep().getCode());

        //   - name: "note"
        assertEquals("Sample collected in the morning.", specimen.getNoteFirstRep().getText());

        // - name: "descriptionOfSpecimen"
        Assert.assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0493' available",
                specimen.getConditionFirstRep().getText());
        Assert.assertEquals("conditionCode", specimen.getConditionFirstRep().getCodingFirstRep().getCode());

        // - name: "identifierOfSpecimen"
        assertEquals("identifierOfSpecimen", specimen.getAccessionIdentifier().getValue());

        // - name: "dateReceived"
        assertEquals("2022-02-03T04:05:06+01:00", specimen.getReceivedTimeElement().getValueAsString());

        // specimen - name: "status"
        assertEquals("available", specimen.getStatusElement().getValueAsString());

        // basedOn, identifierInReference
        assertEquals("identifikation_der_laboranforderung",
                     diagnosticReport.getBasedOnFirstRep().getIdentifier().getValue());

        // Assert Observation  - name: "result"
        Observation observation = (Observation) diagnosticReport.getResultFirstRep().getResource();

        // - name: "issued"
        Assert.assertEquals("2022-02-03T04:05:06.000+01:00", observation.getIssuedElement().getValueAsString());

        //     - name: "analyteMeasurement"
        assertEquals(7.4, observation.getValueQuantity().getValue().doubleValue(), 0);
        assertEquals("mm", observation.getValueQuantity().getUnit());

        // laboryte name
        assertEquals("718-7", observation.getCode().getCodingFirstRep().getCode());
        assertEquals(
                "//fhir.hl7.org/ValueSet/$expand?url=http://hl7.org/fhir/uv/ips/ValueSet/results-laboratory-observations-uv-ips",
                observation.getCode().getCodingFirstRep().getSystem());
        assertEquals("Hemoglobin [Mass/volume] in Blood", observation.getCode().getText());

        // - name: "interpretation"
        assertEquals("142", observation.getInterpretationFirstRep().getCodingFirstRep().getCode());
        assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://hl7.org/fhir/ValueSet/observation-interpretation' available",
                observation.getInterpretationFirstRep().getText());
    }

    @Test
    public void toFhir_multiples() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(
                getFile("/kds/laborbericht/toOpenEHR/output/KDS_Laborbericht_multiples.flat.json"),
                new OPTParser(operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allDiagnosticReports = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof DiagnosticReport).collect(Collectors.toList());
        assertEquals(1, allDiagnosticReports.size());

        final DiagnosticReport diagnosticReport = (DiagnosticReport) allDiagnosticReports.get(0).getResource();

        Assert.assertEquals(2, diagnosticReport.getSpecimen().size());
        Assert.assertEquals(2, diagnosticReport.getResult().size());

        // - name: "Category"
        assertEquals(1, diagnosticReport.getCategory().size());
        assertEquals("LOINC", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getSystem());
        assertEquals("26436-6", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getCode());
        assertEquals("laboratory", diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getDisplay());

        //  - name: "Status"
        assertEquals("at0107", diagnosticReport.getStatusElement().getValueAsString());

        //  - name: "Conclusion"
        assertEquals("Normal blood count", diagnosticReport.getConclusion());

        // - name: "issued"
        assertEquals("2022-02-03T04:05:06.000+01:00", diagnosticReport.getIssuedElement().getValueAsString());

        // - name: "berichtId"
        assertEquals(1, diagnosticReport.getIdentifierFirstRep().getType().getCoding().size());
        assertEquals("FILL", diagnosticReport.getIdentifierFirstRep().getType().getCodingFirstRep().getCode());
        assertEquals("http://terminology.hl7.org/CodeSystem/v2-0203",
                     diagnosticReport.getIdentifierFirstRep().getType().getCodingFirstRep().getSystem());
        assertEquals("bericht_id", diagnosticReport.getIdentifierFirstRep().getValue());

        // Assert Specimen - name: "specimen"
        Specimen specimen = (Specimen) diagnosticReport.getSpecimenFirstRep().getResource();

        // - name: "identifier"
        assertEquals("SP-987654", specimen.getIdentifierFirstRep().getValue());

        //  - name: "collector"
        assertEquals("collectorId", specimen.getCollection().getCollector().getIdentifier().getValue());

        //  - name: "specimen type"
        assertEquals("probenartcode", specimen.getType().getCodingFirstRep().getCode());
        assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0487' available",
                specimen.getType().getText());

        // - name: "type"
        Assert.assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0487' available",
                specimen.getType().getText());
        Assert.assertEquals("probenartcode", specimen.getType().getCodingFirstRep().getCode());

        //   - name: "note"
        assertEquals("Sample collected in the morning.", specimen.getNoteFirstRep().getText());

        // - name: "descriptionOfSpecimen"
        Assert.assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://terminology.hl7.org/ValueSet/v2-0493' available",
                specimen.getConditionFirstRep().getText());
        Assert.assertEquals("conditionCode", specimen.getConditionFirstRep().getCodingFirstRep().getCode());

        // - name: "identifierOfSpecimen"
        assertEquals("identifierOfSpecimen", specimen.getAccessionIdentifier().getValue());

        // - name: "dateReceived"
        assertEquals("2022-02-03T04:05:06+01:00", specimen.getReceivedTimeElement().getValueAsString());

        // specimen - name: "status"
        assertEquals("available", specimen.getStatusElement().getValueAsString());

        Specimen specimen1 = (Specimen) diagnosticReport.getSpecimen().get(1).getResource();

        // - name: "identifier"
        assertEquals("1_SP-987654", specimen1.getIdentifierFirstRep().getValue());

        //  - name: "collector"
        assertEquals("1_collectorId", specimen1.getCollection().getCollector().getIdentifier().getValue());

        //  - name: "specimen type"
        assertEquals("1_probenartcode", specimen1.getType().getCodingFirstRep().getCode());

        // - name: "type"
        Assert.assertEquals("1_probenartcode", specimen1.getType().getCodingFirstRep().getCode());

        //   - name: "note"
        assertEquals("1_Sample collected in the morning.", specimen1.getNoteFirstRep().getText());

        // - name: "descriptionOfSpecimen"
        Assert.assertEquals("1_conditionCode", specimen1.getConditionFirstRep().getCodingFirstRep().getCode());

        // - name: "identifierOfSpecimen"
        assertEquals("1_identifierOfSpecimen", specimen1.getAccessionIdentifier().getValue());

        // - name: "dateReceived"
        assertEquals("3022-02-03T04:05:06+01:00", specimen1.getReceivedTimeElement().getValueAsString());

        // specimen - name: "status"
        assertEquals("unsatisfactory", specimen1.getStatusElement().getValueAsString());

        // basedOn, identifierInReference
        assertEquals("identifikation_der_laboranforderung",
                     diagnosticReport.getBasedOnFirstRep().getIdentifier().getValue());

        // Assert Observation  - name: "result"
        Observation observation = (Observation) diagnosticReport.getResultFirstRep().getResource();

        // - name: "issued"
        Assert.assertEquals("2022-02-03T04:05:06.000+01:00", observation.getIssuedElement().getValueAsString());

        //     - name: "analyteMeasurement"
        assertEquals(7.4, observation.getValueQuantity().getValue().doubleValue(), 0);
        assertEquals("mm", observation.getValueQuantity().getUnit());

        // laboryte name
        assertEquals("718-7", observation.getCode().getCodingFirstRep().getCode());
        assertEquals(
                "//fhir.hl7.org/ValueSet/$expand?url=http://hl7.org/fhir/uv/ips/ValueSet/results-laboratory-observations-uv-ips",
                observation.getCode().getCodingFirstRep().getSystem());
        assertEquals("Hemoglobin [Mass/volume] in Blood", observation.getCode().getText());

        // - name: "interpretation"
        assertEquals("142", observation.getInterpretationFirstRep().getCodingFirstRep().getCode());
        assertEquals(
                "No example for termínology '//fhir.hl7.org/ValueSet/$expand?url=http://hl7.org/fhir/ValueSet/observation-interpretation' available",
                observation.getInterpretationFirstRep().getText());

        Observation observation1 = (Observation) diagnosticReport.getResult().get(1).getResource();

        // - name: "issued"
        Assert.assertEquals("3022-02-03T04:05:06.000+01:00", observation1.getIssuedElement().getValueAsString());

        //     - name: "analyteMeasurement"
        assertEquals(8.4, observation1.getValueQuantity().getValue().doubleValue(), 0);
        assertEquals("1_mm", observation1.getValueQuantity().getUnit());

        // laboryte name
        assertEquals("1_718-7", observation1.getCode().getCodingFirstRep().getCode());
        assertEquals("1_Hemoglobin [Mass/volume] in Blood", observation1.getCode().getText());

        // - name: "interpretation"
        assertEquals("1_142", observation1.getInterpretationFirstRep().getCodingFirstRep().getCode());
    }


}
