package com.medblocks.openfhir.kds.fall;

import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FallToFHIRTest extends KdsTest {

    // mappings / config
    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT_MAPPING =
            "/kds_new/projects/org.highmed/KDS/fall/KDS_fall_einfach.context.yaml";
    final String OPT =
            "/kds/fall/KDS_Fall_einfach.opt";

    // ===== INPUT =====
    final String FALL_EINFACH_COMPOSITION =
            "/kds/fall/toOpenEHR/output/KDS_Fall_einfach.Bundle.json";

    final String OPENEHR_COMPOSITION_1 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-encounter-1.json";
    final String OPENEHR_COMPOSITION_2 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-encounter-2.json";
    final String OPENEHR_COMPOSITION_3 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-encounter-1.json";
    final String OPENEHR_COMPOSITION_4 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-encounter-1.json";
    final String OPENEHR_COMPOSITION_5 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-encounter-1.json";
    final String OPENEHR_COMPOSITION_6 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-encounter-1.json";
    final String OPENEHR_COMPOSITION_7 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-encounter-1.json";
    final String OPENEHR_COMPOSITION_8 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-encounter-1.json";
    final String OPENEHR_COMPOSITION_9 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-encounter-1.json";
    final String OPENEHR_COMPOSITION_10 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-encounter-1.json";
    final String OPENEHR_COMPOSITION_11 =
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-encounter-1.json";

    // ===== OUTPUT =====
    final String FALL_EINFACH =
            "/kds/fall/toFHIR/output/KDS_fall_flat.json";
    final String FHIR_ENCOUNTER_1 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-1-encounter-1.json";
    final String FHIR_ENCOUNTER_2 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-1-encounter-2.json";
    final String FHIR_ENCOUNTER_3 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-2-encounter-1.json";
    final String FHIR_ENCOUNTER_4 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-3-encounter-1.json";
    final String FHIR_ENCOUNTER_5 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-4-encounter-1.json";
    final String FHIR_ENCOUNTER_6 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-5-encounter-1.json";
    final String FHIR_ENCOUNTER_7 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-6-encounter-1.json";
    final String FHIR_ENCOUNTER_8 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-7-encounter-1.json";
    final String FHIR_ENCOUNTER_9 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-8-encounter-1.json";
    final String FHIR_ENCOUNTER_10 =
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-9-encounter-1.json";
    final String FHIR_ENCOUNTER_11 =
            "/kds/fall/toOpenEHR/output/Encounter-mii-exa-test-data-patient-10-encounter-1.json";

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
    public void assertToFHIRBundle(){
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_1), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_1);
    }


    @SneakyThrows
    @Test
    @Ignore // Needs major file clean up
    public void assertToFHIRBundleWhole(){
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_1), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FALL_EINFACH);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR1(){
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_1), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_1);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR2(){
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_2), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_2);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR3(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_3), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_3);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR4(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_4), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_4);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR5(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_5), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_5);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR6(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_6), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_6);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR7(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_7), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_7);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR8(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_8), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_8);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR9(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_9), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_9);
    }

    @SneakyThrows
    @Test
    public void assertToFHIR10(){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITION_10), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTER_10);
    }

    @Test
    public void toFhir() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(
                getFile(FALL_EINFACH_COMPOSITION), new OPTParser(operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allEncounters = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof Encounter).collect(Collectors.toList());
        Assert.assertEquals(1, allEncounters.size());

        final Encounter encounter = (Encounter) allEncounters.get(0).getResource();

        // falltyp
        Assert.assertEquals("42", encounter.getTypeFirstRep().getCodingFirstRep().getCode());
        Assert.assertEquals("No example for termínology 'http://fhir.de/ValueSet/kontaktebene-de?subset=' available",
                encounter.getTypeFirstRep().getCodingFirstRep().getDisplay());
        Assert.assertEquals("http://fhir.de/ValueSet/kontaktebene-de?subset=",
                encounter.getTypeFirstRep().getCodingFirstRep().getSystem());

        // fallklasse
        Assert.assertEquals("43", encounter.getClass_().getCode());
        Assert.assertEquals("No example for termínology 'http://fhir.de/ValueSet/EncounterClassDE?subset=' available",
                encounter.getClass_().getDisplay());
        Assert.assertEquals("http://fhir.de/ValueSet/EncounterClassDE?subset=", encounter.getClass_().getSystem());

        // fallart
        Assert.assertEquals("42", encounter.getStatusElement().getValueAsString());

        // fallId
        Assert.assertEquals("FallId-Id", encounter.getIdentifier().stream()
                .filter(id -> id.getType().getCodingFirstRep().getCode().equals("VN"))
                .map(id -> id.getValue())
                .findFirst().orElse(null));

        // serviceProvider
        final Organization serviceProvider = (Organization) encounter.getServiceProvider().getResource();
        Assert.assertEquals("Org Name", serviceProvider.getName());
        Assert.assertEquals("Org Id", serviceProvider.getIdentifierFirstRep().getValue());

        // aufnahmegrundExtension
        final List<Extension> aufnahmegrundExtension = encounter.getExtensionsByUrl(
                "http://fhir.de/StructureDefinition/Aufnahmegrund");
        Assert.assertEquals(1, aufnahmegrundExtension.size());
        Assert.assertEquals(3, aufnahmegrundExtension.get(0).getExtension().size());

        final Extension firstAndSecond = aufnahmegrundExtension.get(0).getExtensionByUrl("ErsteUndZweiteStelle");
        final Extension third = aufnahmegrundExtension.get(0).getExtensionByUrl("DritteStelle");
        final Extension fourth = aufnahmegrundExtension.get(0).getExtensionByUrl("VierteStelle");
        Assert.assertEquals("12", ((Coding) firstAndSecond.getValue()).getCode());
        Assert.assertEquals("3", ((Coding) third.getValue()).getCode());
        Assert.assertEquals("4", ((Coding) fourth.getValue()).getCode());

        final CodeableConcept admitSource = encounter.getHospitalization().getAdmitSource();
        Assert.assertEquals("admitSource", admitSource.getCodingFirstRep().getCode());

        final Extension entlassungsgrundExtension = encounter.getHospitalization().getDischargeDisposition()
                .getExtensionByUrl("http://fhir.de/StructureDefinition/Entlassungsgrund");
        Assert.assertEquals("outcome", ((Coding) entlassungsgrundExtension.getValue()).getCode());

        final Encounter.DiagnosisComponent diagnosisComponent = encounter.getDiagnosisFirstRep();
        final Condition condition = (Condition) diagnosisComponent.getCondition().getResource();
        Assert.assertEquals("diagnos coding", condition.getCode().getCodingFirstRep().getCode());

        Assert.assertEquals(2, diagnosisComponent.getUse().getCoding().size());
        Assert.assertEquals("type", diagnosisComponent.getUse().getCoding().stream()
                .filter(cod -> cod.getSystem().equals("http://fhir.de/CodeSystem/dki-diagnosetyp"))
                .map(Coding::getCode)
                .findFirst().orElse(null));
        Assert.assertEquals("subtype", diagnosisComponent.getUse().getCoding().stream()
                .filter(cod -> cod.getSystem().equals("http://fhir.de/CodeSystem/dki-diagnosesubtyp"))
                .map(Coding::getCode)
                .findFirst().orElse(null));

        //   - name: "period"
        Assert.assertEquals("2020-02-03T04:05:06+01:00", encounter.getPeriod().getStartElement().getValueAsString());
        Assert.assertEquals("2022-02-03T04:05:06+01:00", encounter.getPeriod().getEndElement().getValueAsString());

        final List<Encounter.EncounterLocationComponent> locations = encounter.getLocation();
        Assert.assertEquals(3, locations.size());

        final List<Identifier> roomLocationIds = locations.stream()
                .filter(loc -> "ro".equals(loc.getPhysicalType().getCodingFirstRep().getCode()))
                .map(el -> el.getLocation().getIdentifier())
                .toList();
        final List<Identifier> bedLocationIds = locations.stream()
                .filter(loc -> "bd".equals(loc.getPhysicalType().getCodingFirstRep().getCode()))
                .map(el -> el.getLocation().getIdentifier())
                .toList();
        final List<Identifier> wardLocationIds = locations.stream()
                .filter(loc -> "wa".equals(loc.getPhysicalType().getCodingFirstRep().getCode()))
                .map(el -> el.getLocation().getIdentifier())
                .toList();

        Assert.assertTrue(locations.stream().allMatch(loc -> loc.getPhysicalType().getCodingFirstRep().getSystem()
                .equals("http://terminology.hl7.org/CodeSystem/location-physical-type")));

        Assert.assertEquals(1, roomLocationIds.size());
        Assert.assertEquals(1, bedLocationIds.size());
        Assert.assertEquals(1, wardLocationIds.size());

        Assert.assertEquals("zimmer-1", roomLocationIds.get(0).getValue());
        Assert.assertEquals("bett-1", bedLocationIds.get(0).getValue());
        Assert.assertEquals("station-1", wardLocationIds.get(0).getValue());
    }


}
