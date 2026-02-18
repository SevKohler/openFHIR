package com.medblocks.openfhir.kds.person;

import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class PersonToFHIRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/person/person.context.yaml";
    final String OPT = "/kds/person/KDS_Person.opt";
    final String OPENEHR_FLAT = "/kds/person/KDS_Person.flat.json";

    final String[] OPENEHR_COMPOSITIONS = {
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-1.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-2.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-3.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-4.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-5.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-6.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-7.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-8.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-9.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-10.json",
            "/kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-11.json"
    };

    final String[] FHIR_BUNDLES = {
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-1.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-2.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-3.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-4.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-5.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-6.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-7.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-8.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-9.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-10.json",
            "/kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-11.json"
    };

    @SneakyThrows
    @Override
    protected void prepareState() {
        context = getContext(CONTEXT);
        operationaltemplateSerialized = IOUtils.toString(this.getClass().getResourceAsStream(OPT));
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }

    @SneakyThrows
    private void assertToFHIR(final int index) {
        final Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITIONS[index]),
                Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_BUNDLES[index]);
    }

    /**
     * Input: /kds/person/KDS_Person.flat.json
     * Expected: legacy assertions from former PersonTest.kdsPerson_toFhir
     */
    @Test
    public void assertToFHIRLegacyDetailed() {
        final Bundle legacyInput = getTestBundle("/kds/person/toOpenEHR/input/kds_person_bundle.json");
        final Composition compositionFromFlat =
                fhirToOpenEhr.fhirToCompositionRm(context, legacyInput, operationaltemplate);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);

        final List<Patient> patients = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof Patient)
                .map(en -> (Patient) en.getResource())
                .collect(Collectors.toList());

        final List<Condition> conditions = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof Condition)
                .map(en -> (Condition) en.getResource())
                .collect(Collectors.toList());

        final List<Observation> observations = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof Observation)
                .map(en -> (Observation) en.getResource())
                .collect(Collectors.toList());

        Assert.assertEquals(1, patients.size());
        final Patient thePatient = patients.get(0);

        Assert.assertTrue(thePatient.getAddress().size() >= 1);
        final Address strasseAddress = thePatient.getAddress().get(0);
        final List<StringType> strasseLines = strasseAddress.getLine();
        if (!strasseLines.isEmpty() && strasseLines.get(0).getExtension().size() >= 3) {
            Assert.assertEquals("123 Main St", ((StringType) strasseLines.get(0)
                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName")
                    .getValue()).getValue());
            Assert.assertEquals("Apt 4B", ((StringType) strasseLines.get(0)
                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber")
                    .getValue()).getValue());
            Assert.assertEquals("Wohnung 3", ((StringType) strasseLines.get(0)
                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-additionalLocator")
                    .getValue()).getValue());
        }

        if (thePatient.getAddress().size() > 1) {
            final Address postfachAddress = thePatient.getAddress().get(1);
            final List<StringType> postfachLines = postfachAddress.getLine();
            Assert.assertEquals(0, postfachLines.size());
            Assert.assertEquals("Berlin", ((StringType) postfachAddress.getCityElement().getExtensionFirstRep()
                    .getValue()).getValue());
            Assert.assertEquals("Kreuzberg", ((StringType) postfachAddress.getExtensionFirstRep().getValue()).getValue());
            Assert.assertEquals("postal", postfachAddress.getTypeElement().getValueAsString());
            Assert.assertEquals("10997", postfachAddress.getPostalCode());
            Assert.assertEquals(" DE", postfachAddress.getCountry());
            Assert.assertEquals("Berlin", postfachAddress.getState());
        }

        if (thePatient.getExtensionByUrl("extension_url_to_be_defined") != null) {
            Assert.assertEquals("D", ((Coding) thePatient.getExtensionByUrl("extension_url_to_be_defined")
                    .getValue()).getCode());
            Assert.assertEquals("divers", ((Coding) thePatient.getExtensionByUrl("extension_url_to_be_defined")
                    .getValue()).getDisplay());
            Assert.assertEquals("http://fhir.de/ValueSet/gender-other-de",
                    ((Coding) thePatient.getExtensionByUrl("extension_url_to_be_defined")
                            .getValue()).getSystem());
        }
        Assert.assertEquals("male", thePatient.getGenderElement().getValueAsString());
        final List<HumanName> names = thePatient.getName();
        Assert.assertTrue(names.size() >= 1);
        final List<HumanName> maidenNames = names.stream()
                .filter(name -> "maiden".equals(name.getUseElement().getCode())).collect(Collectors.toList());
        final List<HumanName> officialNames = names.stream()
                .filter(name -> "official".equals(name.getUseElement().getCode())).collect(Collectors.toList());
        if (!officialNames.isEmpty()) {
            final HumanName officialName = officialNames.get(0);
            if (officialName.getFamilyElement()
                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name") != null) {
                Assert.assertEquals("John", ((StringType) officialName.getFamilyElement()
                        .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
                        .getValue()).getValue());
            }
        }

        if (!maidenNames.isEmpty()) {
            final HumanName maidenName = maidenNames.get(0);
            if (maidenName.getFamilyElement()
                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name") != null) {
                Assert.assertEquals("Smith", ((StringType) maidenName.getFamilyElement()
                        .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
                        .getValue()).getValue());
            }
        }

        Assert.assertTrue(thePatient.getIdentifier().size() >= 1);
        Assert.assertNotNull(thePatient.getBirthDate());

        if (!conditions.isEmpty()) {
            Assert.assertFalse(conditions.get(0).getCode().getCoding().isEmpty());
            Assert.assertNotNull(conditions.get(0).getCode().getCodingFirstRep().getCode());
        }

        if (!thePatient.getContact().isEmpty()) {
            final Patient.ContactComponent zerothContact = thePatient.getContact().get(0);
            Assert.assertNotNull(zerothContact.getName());
        }

        if (!observations.isEmpty()) {
            Assert.assertNotNull(observations.get(0).getStatusElement().getCode());
        }
    }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-1.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-1.json
     */
    @Test
    public void assertToFHIR_1() { assertToFHIR(0); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-2.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-2.json
     */
    @Test
    public void assertToFHIR_2() { assertToFHIR(1); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-3.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-3.json
     */
    @Test
    public void assertToFHIR_3() { assertToFHIR(2); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-4.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-4.json
     */
    @Test
    public void assertToFHIR_4() { assertToFHIR(3); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-5.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-5.json
     */
    @Test
    public void assertToFHIR_5() { assertToFHIR(4); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-6.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-6.json
     */
    @Test
    public void assertToFHIR_6() { assertToFHIR(5); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-7.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-7.json
     */
    @Test
    public void assertToFHIR_7() { assertToFHIR(6); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-8.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-8.json
     */
    @Test
    public void assertToFHIR_8() { assertToFHIR(7); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-9.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-9.json
     */
    @Test
    public void assertToFHIR_9() { assertToFHIR(8); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-10.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-10.json
     */
    @Test
    public void assertToFHIR_10() { assertToFHIR(9); }

    /**
     * Input: /kds/person/toOpenEHR/output/Composition-mii-exa-test-data-patient-11.json
     * Expected: /kds/person/toFHIR/output/Patient-mii-exa-test-data-patient-11.json
     */
    @Test
    public void assertToFHIR_11() { assertToFHIR(10); }
}
