package com.medblocks.openfhir.kds.laborauftrag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medblocks.openfhir.kds.KdsBidirectionalTest;
import com.medblocks.openfhir.kds.KdsTest;
import com.nedap.archie.rm.composition.Composition;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class LaborauftragToFHIRTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/laborauftrag/KDS_laborauftrag.context.yaml";
    final String HELPER_LOCATION = "/kds/laborauftrag/";
    final String OPT = "KDS_Laborauftrag.opt";
    final String FLAT = "KDS_Laborauftrag.flat.json";

    final String BUNDLE = "KDS_Laborauftrag_bundle.json";

    @SneakyThrows
    @Override
    public void prepareState() {
        context = getContext(CONTEXT);
        operationaltemplateSerialized = IOUtils.toString(this.getClass().getResourceAsStream(HELPER_LOCATION + OPT));
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }


    @Test
    public void toFhir() {
        final Composition compositionFromFlat = new FlatJsonUnmarshaller().unmarshal(getFile(HELPER_LOCATION + FLAT),
                                                                                     new OPTParser(
                                                                                             operationaltemplate).parse());
        final Bundle bundle = openEhrToFhir.compositionToFhir(context, compositionFromFlat, operationaltemplate);
        final List<Bundle.BundleEntryComponent> allServiceRequests = bundle.getEntry().stream()
                .filter(en -> en.getResource() instanceof ServiceRequest).collect(Collectors.toList());
        assertEquals(1, allServiceRequests.size());

        final ServiceRequest serviceRequest = (ServiceRequest) allServiceRequests.get(0).getResource();

        assertServiceRequest(serviceRequest);
    }


}
