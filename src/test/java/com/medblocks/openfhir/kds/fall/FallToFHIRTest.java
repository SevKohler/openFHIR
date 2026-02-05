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

    final String[] OPENEHR_COMPOSITIONS = {
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-1-encounter-2.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-2-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-3-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-4-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-5-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-6-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-7-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-8-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-9-encounter-1.json",
            "/kds/fall/toOpenEHR/output/Composition-mii-exa-test-data-patient-10-encounter-1.json"
    };

    // ===== OUTPUT =====
    final String BUNDLE_EINFACH =
            "/kds/fall/toFHIR/output/KDS_Fall_einfach.flat.json";
    final String[] FHIR_ENCOUNTERS = {
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-1-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-1-encounter-2.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-2-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-3-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-4-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-5-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-6-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-7-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-8-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-9-encounter-1.json",
            "/kds/fall/toFHIR/output/Encounter-mii-exa-test-data-patient-10-encounter-1.json"
    };

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
    private void assertToFHIR(int index){
        Composition composition = JacksonUtil.getObjectMapper()
                .readValue(getFile(OPENEHR_COMPOSITIONS[index]), Composition.class);
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, composition, operationaltemplate);
        standardsAsserter.assertBundle(bundle, FHIR_ENCOUNTERS[index]);
    }

    @Test
    public void assertToFHIR1(){
        assertToFHIR(0);
    }

    @Test
    public void assertToFHIR2(){
        assertToFHIR(1);
    }

    @Test
    public void assertToFHIR3(){
        assertToFHIR(2);
    }

    @Test
    public void assertToFHIR4(){
        assertToFHIR(3);
    }

    @Test
    public void assertToFHIR5(){
        assertToFHIR(4);
    }

    @Test
    public void assertToFHIR6(){
        assertToFHIR(5);
    }

    @Test
    public void assertToFHIR7(){
        assertToFHIR(6);
    }

    @Test
    public void assertToFHIR8(){
        assertToFHIR(7);
    }

    @Test
    public void assertToFHIR9(){
        assertToFHIR(8);
    }

    @Test
    public void assertToFHIR10(){
        assertToFHIR(9);
    }

    @Test
    public void assertToFHIR11(){
        assertToFHIR(10);
    }

}
