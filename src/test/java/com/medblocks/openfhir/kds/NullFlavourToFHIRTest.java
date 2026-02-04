package com.medblocks.openfhir.kds;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Assert;
import org.junit.Test;

public class NullFlavourToFHIRTest extends KdsTest {

    private static final String MODEL_MAPPINGS = "/kds_new/";

    private static final String LABORBERICHT_CONTEXT =
            "/kds_new/projects/org.highmed/KDS/laborbericht/KDS_laborbericht.context.yaml";
    private static final String LABORBERICHT_OPT = "/kds/laborbericht/KDS_Laborbericht.opt";
    private static final String LABORBERICHT_FLAT = "/kds/laborbericht/toOpenEHR/output/KDS_Laborbericht.flat.json";

    private static final String LABORAUFTRAG_CONTEXT =
            "/kds_new/projects/org.highmed/KDS/laborauftrag/KDS_laborauftrag.context.yaml";
    private static final String LABORAUFTRAG_OPT = "/kds/laborauftrag/KDS_Laborauftrag.opt";
    private static final String LABORAUFTRAG_FLAT = "/kds/laborauftrag/toOpenEHR/output/KDS_Laborauftrag.flat.json";

    private static final String DAR_URL = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";

    @Override
    @SneakyThrows
    protected void prepareState() {
        initState(LABORBERICHT_CONTEXT, LABORBERICHT_OPT);
    }

    @SneakyThrows
    private void initState(String contextPath, String optPath) {
        context = getContext(contextPath);
        operationaltemplateSerialized = IOUtils.toString(this.getClass().getResourceAsStream(optPath));
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }

    @SneakyThrows
    @Test
    public void mapsNullFlavourToObservationDataAbsentReason() {
        JsonObject flat = JsonParser.parseString(getFile(LABORBERICHT_FLAT)).getAsJsonObject();
        String quantityPath = "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/quantity_value";
        flat.remove(quantityPath + "|magnitude");
        flat.remove(quantityPath + "|unit");
        flat.addProperty("laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/_null_flavour|code", "253");
        flat.addProperty("laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/_null_flavour|value", "unknown");
        flat.addProperty("laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/messwert:0/_null_flavour|terminology", "openehr");
        final org.openehr.schemas.v1.OPERATIONALTEMPLATE template = operationaltemplate;
        final Bundle bundle = openEhrToFhir.compositionToFhir(
                context,
                new FlatJsonUnmarshaller().unmarshal(flat.toString(), new OPTParser(template).parse()),
                template);
        final DiagnosticReport diagnosticReport = (DiagnosticReport) bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(res -> res instanceof DiagnosticReport)
                .findFirst()
                .orElseThrow();
        final Observation observation = (Observation) diagnosticReport.getResultFirstRep().getResource();

        Assert.assertTrue(observation.hasDataAbsentReason());
        Assert.assertEquals("unknown", observation.getDataAbsentReason().getCodingFirstRep().getCode());
    }

    @SneakyThrows
    @Test
    public void mapsNullFlavourToObservationCodeDataAbsentReasonExtension() {
        JsonObject flat = JsonParser.parseString(getFile(LABORBERICHT_FLAT)).getAsJsonObject();
        String analytePath = "laborbericht/laborbefund/any_event:0/pro_laboranalyt:0/analyte_name";
        flat.remove(analytePath + "|code");
        flat.remove(analytePath + "|value");
        flat.remove(analytePath + "|terminology");
        flat.addProperty(analytePath + "/_null_flavour|code", "253");
        flat.addProperty(analytePath + "/_null_flavour|value", "unknown");
        flat.addProperty(analytePath + "/_null_flavour|terminology", "openehr");

        final org.openehr.schemas.v1.OPERATIONALTEMPLATE template = operationaltemplate;
        final Bundle bundle = openEhrToFhir.compositionToFhir(
                context,
                new FlatJsonUnmarshaller().unmarshal(flat.toString(), new OPTParser(template).parse()),
                template);
        final DiagnosticReport diagnosticReport = (DiagnosticReport) bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(res -> res instanceof DiagnosticReport)
                .findFirst()
                .orElseThrow();
        final Observation observation = (Observation) diagnosticReport.getResultFirstRep().getResource();

        Extension darExt = observation.getCode().getExtensionByUrl(DAR_URL);
        Assert.assertNotNull(darExt);
        Assert.assertEquals("unknown", darExt.getValueAsPrimitive().getValueAsString());
    }

    @SneakyThrows
    @Test
    public void mapsNullFlavourToServiceRequestIdentifierDataAbsentReasonExtension() {
        initState(LABORAUFTRAG_CONTEXT, LABORAUFTRAG_OPT);
        JsonObject flat = JsonParser.parseString(getFile(LABORAUFTRAG_FLAT)).getAsJsonObject();
        String idPath = "leistungsanforderung/laborleistung/auftrags-id_des_anfordernden_einsendenden_systems_plac";
        flat.remove(idPath + "|id");
        flat.addProperty(idPath + "/_null_flavour|code", "253");
        flat.addProperty(idPath + "/_null_flavour|value", "unknown");
        flat.addProperty(idPath + "/_null_flavour|terminology", "openehr");

        final Bundle bundle = openEhrToFhir.compositionToFhir(
                context,
                new FlatJsonUnmarshaller().unmarshal(flat.toString(), new OPTParser(operationaltemplate).parse()),
                operationaltemplate);
        final ServiceRequest serviceRequest = (ServiceRequest) bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(res -> res instanceof ServiceRequest)
                .findFirst()
                .orElseThrow();

        Extension darExt = serviceRequest.getIdentifierFirstRep().getExtensionByUrl(DAR_URL);
        Assert.assertNotNull(darExt);
        Assert.assertEquals("unknown", darExt.getValueAsPrimitive().getValueAsString());
    }
}
