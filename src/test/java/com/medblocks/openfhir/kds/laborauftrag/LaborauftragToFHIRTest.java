package com.medblocks.openfhir.kds.laborauftrag;

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

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LaborauftragToFHIRTest extends KdsTest {


    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT_MAPPING =
            "/kds_new/projects/org.highmed/KDS/laborauftrag/KDS_laborauftrag.context.yaml";
    final String OPT =
            "/kds/laborauftrag/KDS_Laborauftrag.opt";

    final String FLAT = "/kds/laborauftrag/toOpenEHR/output/KDS_Laborauftrag.flat.json";
    final String OPENEHR_COMPOSITION_1 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-labrequest-1.json";
    final String OPENEHR_COMPOSITION_2 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-labrequest-1.json";
    final String OPENEHR_COMPOSITION_3 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-labrequest-1.json";
    final String OPENEHR_COMPOSITION_4 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-labrequest-1.json";
    final String OPENEHR_COMPOSITION_5 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-labrequest-1.json";
    final String OPENEHR_COMPOSITION_6 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-labrequest-1.json";
    final String OPENEHR_COMPOSITION_7 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-labrequest-1.json";
    final String OPENEHR_COMPOSITION_8 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-labrequest-1.json";
    final String OPENEHR_COMPOSITION_9 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-labrequest-1.json";
    final String OPENEHR_COMPOSITION_10 = "/kds/laborauftrag/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-labrequest-1.json";

    final String FHIR_BUNDLE_1 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-1-labrequest-1.json";
    final String FHIR_BUNDLE_2 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-2-labrequest-1.json";
    final String FHIR_BUNDLE_3 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-3-labrequest-1.json";
    final String FHIR_BUNDLE_4 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-4-labrequest-1.json";
    final String FHIR_BUNDLE_5 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-5-labrequest-1.json";
    final String FHIR_BUNDLE_6 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-6-labrequest-1.json";
    final String FHIR_BUNDLE_7 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-7-labrequest-1.json";
    final String FHIR_BUNDLE_8 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-8-labrequest-1.json";
    final String FHIR_BUNDLE_9 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-9-labrequest-1.json";
    final String FHIR_BUNDLE_10 = "/kds/laborauftrag/toFHIR/output/ServiceRequest-mii-exa-test-data-patient-10-labrequest-1.json";

    @SneakyThrows
    @Override
    public void prepareState() {
        context = getContext(CONTEXT_MAPPING);
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
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_1);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR2() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_2),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_2);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR3() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_3),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_3);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR4() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_4),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_4);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR5() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_5),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_5);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR6() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_6),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_6);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR7() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_7),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_7);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR8() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_8),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_8);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR9() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_9),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_9);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR10() {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_10),
                Composition.class);
        final org.hl7.fhir.r4.model.Bundle bundle =
                openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLE_10);
    }

    @Test
    public void toFhir() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(getFile(FLAT),
                new OPTParser(
                        operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allServiceRequests = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof ServiceRequest).collect(Collectors.toList());
        assertEquals(1, allServiceRequests.size());

        final ServiceRequest serviceRequest = (ServiceRequest) allServiceRequests.get(0).getResource();

        assertServiceRequest(serviceRequest);
    }

    private void assertServiceRequest(final ServiceRequest serviceRequest) {

        //  - name: "identifier"
        Assert.assertEquals("Medical record identifier", serviceRequest.getIdentifierFirstRep().getValue());

        //  - name: "status"
        Assert.assertEquals("completed", serviceRequest.getStatusElement().getValueAsString());

        //  - name: "code"
        Assert.assertEquals("2345-7", serviceRequest.getCode().getCodingFirstRep().getCode());
        Assert.assertEquals("Blood Glucose Test", serviceRequest.getCode().getText());

        //  - name: "category"
        Assert.assertEquals("laboratory", serviceRequest.getCategoryFirstRep().getCodingFirstRep().getCode());

        //  - name: "intent"
        Assert.assertEquals("order", serviceRequest.getIntentElement().getValueAsString());

        //  - name: "note"
        Assert.assertEquals("Sample collected in the morning.", serviceRequest.getNoteFirstRep().getText());

        //  - name: "organisation"
        //  - name: "org name"
        //  - name: "org id"
        final Organization org = (Organization) serviceRequest.getRequester().getResource();
        Assert.assertEquals("Einsender name", org.getName());
        Assert.assertEquals("Example Hospital", org.getIdentifierFirstRep().getValue());

        //  - name: "specimen"
        final List<Reference> specimenReferences = serviceRequest.getSpecimen();
        Assert.assertEquals(2, specimenReferences.size());
        final List<Specimen> specimens = specimenReferences.stream().map(spec -> (Specimen) spec.getResource())
                .toList();

        //  - name: "specimen identifier"
        //  - name: "specimen collection date time"
        //  - name: "specimen collector"
        final Specimen specimen1 = specimens.get(0);
        Assert.assertEquals("spec1", specimen1.getAccessionIdentifier().getValue());
//        Assert.assertEquals("2022-02-03T04:05:06+01:00",
//                            specimen1.getCollection().getCollectedPeriod().getStartElement().getValueAsString());
        Assert.assertEquals("probenehmers_id1", specimen1.getCollection().getCollector().getIdentifier().getValue());

        final Specimen specimen2 = specimens.get(1);
        Assert.assertEquals("spec2", specimen2.getAccessionIdentifier().getValue());
//        Assert.assertEquals("3022-02-03T04:05:06+01:00",
//                            specimen2.getCollection().getCollectedDateTimeType().getValueAsString());
        Assert.assertEquals("probenehmers_id2", specimen2.getCollection().getCollector().getIdentifier().getValue());

    }

}
