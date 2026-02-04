package com.medblocks.openfhir.toopenehr;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medblocks.openfhir.OpenEhrRmWorker;
import com.medblocks.openfhir.TestOpenFhirMappingContext;
import com.medblocks.openfhir.fc.FhirConnectConst;
import com.medblocks.openfhir.util.FhirConnectModelMerger;
import com.medblocks.openfhir.util.OpenEhrCachedUtils;
import com.medblocks.openfhir.util.OpenEhrConditionEvaluator;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import org.ehrbase.openehr.sdk.serialisation.flatencoding.std.umarshal.FlatJsonUnmarshaller;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class  NullFlavourToOpenEhrTest {

    final FhirPathR4 fhirPathR4 = new FhirPathR4(FhirContext.forR4());
    final OpenFhirStringUtils openFhirStringUtils = new OpenFhirStringUtils();
    final FhirConnectModelMerger fhirConnectModelMerger = new FhirConnectModelMerger();
    final TestOpenFhirMappingContext repo = new TestOpenFhirMappingContext(
            fhirPathR4,
            openFhirStringUtils,
            fhirConnectModelMerger);
    FhirToOpenEhr fhirToOpenEhr;

    @Before
    public void init() {
        fhirToOpenEhr = new FhirToOpenEhr(fhirPathR4,
                                          new OpenFhirStringUtils(),
                                          new FlatJsonUnmarshaller(),
                                          new Gson(),
                                          new OpenEhrRmWorker(openFhirStringUtils, new OpenFhirMapperUtils()),
                                          openFhirStringUtils,
                                          repo,
                                          new OpenEhrCachedUtils(null),
                                          new OpenFhirMapperUtils(),
                                          new OpenEhrPopulator(new OpenFhirMapperUtils()),
                                          new OpenEhrConditionEvaluator(openFhirStringUtils));
    }

    @Test
    public void mapsObservationDataAbsentReasonToNullFlavour() {
        FhirToOpenEhrHelper helper = FhirToOpenEhrHelper.builder()
                .fhirPath("value")
                .openEhrPath("test/value")
                .openEhrType(FhirConnectConst.DV_CODED_TEXT)
                .archetype("test")
                .build();
        JsonObject flat = new JsonObject();
        Observation observation = new Observation();
        observation.setDataAbsentReason(new org.hl7.fhir.r4.model.CodeableConcept()
                .addCoding(new Coding("http://terminology.hl7.org/CodeSystem/data-absent-reason",
                                      "unknown",
                                      "Unknown")));

        boolean handled = fhirToOpenEhr.addDataPoints(helper, flat, observation);

        Assert.assertTrue(handled);
        Assert.assertEquals("unknown", flat.get("test/value/_null_flavour|value").getAsString());
        Assert.assertEquals("253", flat.get("test/value/_null_flavour|code").getAsString());
        Assert.assertEquals("openehr", flat.get("test/value/_null_flavour|terminology").getAsString());
    }

    @Test
    public void mapsElementDataAbsentReasonExtensionToNullFlavour() {
        FhirToOpenEhrHelper helper = FhirToOpenEhrHelper.builder()
                .fhirPath("code.coding.code")
                .openEhrPath("test/code")
                .openEhrType(FhirConnectConst.DV_CODED_TEXT)
                .archetype("test")
                .build();
        JsonObject flat = new JsonObject();
        Observation observation = new Observation();
        Coding coding = observation.getCode().addCoding();
        coding.setSystem("http://loinc.org");
        coding.getCodeElement().addExtension(OpenEhrPopulator.DATA_ABSENT_REASON_URL, new CodeType("unknown"));

        boolean handled = fhirToOpenEhr.addDataPoints(helper, flat, observation);

        Assert.assertTrue(handled);
        Assert.assertEquals("unknown", flat.get("test/code/_null_flavour|value").getAsString());
    }

    @Test
    public void mapsIdentifierValueDataAbsentReasonExtensionToNullFlavour() {
        FhirToOpenEhrHelper helper = FhirToOpenEhrHelper.builder()
                .fhirPath("identifier.value")
                .openEhrPath("test/identifier")
                .openEhrType(FhirConnectConst.DV_IDENTIFIER)
                .archetype("test")
                .build();
        JsonObject flat = new JsonObject();
        ServiceRequest serviceRequest = new ServiceRequest();
        Identifier identifier = serviceRequest.addIdentifier();
        identifier.getValueElement().addExtension(OpenEhrPopulator.DATA_ABSENT_REASON_URL, new CodeType("unknown"));

        boolean handled = fhirToOpenEhr.addDataPoints(helper, flat, serviceRequest);

        Assert.assertTrue(handled);
        Assert.assertEquals("unknown", flat.get("test/identifier/_null_flavour|value").getAsString());
    }

    @Test
    public void mapsNullFlavourForArbitraryLaborXPath() {
        FhirToOpenEhrHelper helper = FhirToOpenEhrHelper.builder()
                .fhirPath("valueQuantity")
                .openEhrPath("laborX/any_event:0/messwert:0/quantity_value")
                .openEhrType(FhirConnectConst.DV_QUANTITY)
                .archetype("test")
                .build();
        JsonObject flat = new JsonObject();
        Observation observation = new Observation();
        observation.setDataAbsentReason(new org.hl7.fhir.r4.model.CodeableConcept()
                .addCoding(new Coding("http://terminology.hl7.org/CodeSystem/data-absent-reason",
                                      "unknown",
                                      "Unknown")));

        boolean handled = fhirToOpenEhr.addDataPoints(helper, flat, observation);

        Assert.assertTrue(handled);
        Assert.assertTrue(flat.has("laborX/any_event:0/messwert:0/_null_flavour|value"));
        Assert.assertFalse(flat.has("laborX/any_event:0/messwert:0/quantity_value/_null_flavour|value"));
    }
}
