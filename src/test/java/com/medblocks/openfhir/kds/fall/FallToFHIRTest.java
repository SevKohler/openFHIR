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

    final String FALL_EINFACH = "/kds/fall/toOpenEHR/output/Composition-KDS_Fall_einfach.Bundle.json";

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
    final String BUNDLE_EINFACH =
            "/kds/fall/toFHIR/output/KDS_Fall_einfach.flat.json";
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
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(FALL_EINFACH), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, BUNDLE_EINFACH);
    }


    @SneakyThrows
    @Test
    public void assertToFHIR1(){
        Composition composition = JacksonUtil.getObjectMapper().readValue(getFile(OPENEHR_COMPOSITION_1), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
//        final String dumpPath = System.getProperty("dumpBundle");
//        if (dumpPath != null && !dumpPath.isBlank()) {
//            java.nio.file.Files.writeString(java.nio.file.Path.of(dumpPath), jsonParser.encodeResourceToString(bundle));
//        }
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
    @Ignore // TODO since diagnosis bug
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
    @Ignore // TODO since diagnosis bug
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

}
