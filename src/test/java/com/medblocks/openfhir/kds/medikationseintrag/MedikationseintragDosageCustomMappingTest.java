package com.medblocks.openfhir.kds.medikationseintrag;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.kds.KdsTest;
import lombok.SneakyThrows;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.junit.Assert;
import org.junit.Test;

public class MedikationseintragDosageCustomMappingTest extends KdsTest {

    final String MODEL_MAPPINGS = "/kds_new/";
    final String CONTEXT = "/kds_new/projects/org.highmed/KDS/medikationseintrag/KDS_medikationseintrag.context.yaml";
    final String OPT = "/kds/medikationseintrag/KDS_Medikationseintrag.opt";
    final String INPUT = "/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-dosage-custom-mapping.json";

    @SneakyThrows
    @Override
    public void prepareState() {
        context = getContext(CONTEXT);
        operationaltemplateSerialized = getFile(OPT);
        operationaltemplate = getOperationalTemplate();
        repo.initRepository(context, operationaltemplate, getClass().getResource(MODEL_MAPPINGS).getFile());
        webTemplate = new OPTParser(operationaltemplate).parse();
    }

    @Test
    public void mapsDosageQuantityAndRateRatioWithCustomMapping() {
        JsonObject flat = fhirToOpenEhr.fhirToFlatJsonObject(context, getTestBundle(INPUT), operationaltemplate);

        String doseMag = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/dosis/quantity_value|magnitude";
        String doseUnit = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/dosis/quantity_value|unit";
        String rateMag = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/quantity_value|magnitude";
        String rateUnit = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/quantity_value|unit";
        String durationValue = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsdauer|value";
        String duration = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsdauer";

        Assert.assertTrue("dose magnitude missing", flat.has(doseMag));
        Assert.assertTrue("dose unit missing", flat.has(doseUnit));
        Assert.assertEquals(1500.0, flat.get(doseMag).getAsDouble(), 0.0001);
        Assert.assertEquals("mg", flat.get(doseUnit).getAsString());

        Assert.assertFalse("rate magnitude should not be mapped for unsupported unit", flat.has(rateMag));
        Assert.assertFalse("rate unit should not be mapped for unsupported unit", flat.has(rateUnit));

        Assert.assertTrue("duration missing", flat.has(durationValue) || flat.has(duration));
        String actualDuration = flat.has(durationValue)
                ? flat.get(durationValue).getAsString()
                : flat.get(duration).getAsString();
        Assert.assertEquals("PT30M", actualDuration);
    }

    @Test
    public void mapsRateRangeToTextValueNotQuantityValue() {
        JsonObject flat = fhirToOpenEhr.fhirToFlatJsonObject(
                context,
                getTestBundle("/kds/medikationseintrag/toOpenEHR/input/MedicationStatement-mii-exa-test-data-patient-2-medstatement-4.json"),
                operationaltemplate);

        String rateText = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/text_value";
        String rateQuantityValue = "medikamentenliste/aussage_zur_medikamenteneinnahme:0/dosierung:0/verabreichungsrate/quantity_value";

        Assert.assertTrue("rate range should map to text_value", flat.has(rateText));
        Assert.assertEquals("150-300 ml/h", flat.get(rateText).getAsString());
        Assert.assertFalse("rate range should not map to quantity_value", flat.has(rateQuantityValue));
    }
}
