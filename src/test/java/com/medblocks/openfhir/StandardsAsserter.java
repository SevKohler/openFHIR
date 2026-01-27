package com.medblocks.openfhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.google.gson.*;
import com.nedap.archie.rm.composition.Composition;
import org.ehrbase.openehr.sdk.serialisation.jsonencoding.CanonicalJson;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONObject;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StandardsAsserter {

    private static final Gson GSON = new Gson();

    public void assertComposition(Composition composition, String expectedClasspathJson, OPERATIONALTEMPLATE operationalTemplate) {
        JSONObject actual = new JSONObject(new CanonicalJson().marshal(composition));
        JSONObject expected = loadJsonObject(expectedClasspathJson);
        JSONAssert.assertEquals(expected, actual, true);
        OptCompositionValidator.assertValid(operationalTemplate, composition);
    }

    public void assertBundle(Bundle bundle) {
        // Create an NPM Package Support module and load one package in from
        // the classpath
        FhirContext ctx = FhirContext.forR4();
        NpmPackageValidationSupport npmPackageSupport = new NpmPackageValidationSupport(ctx);
        try {
            npmPackageSupport.loadPackageFromClasspath("kds/de.medizininformatikinitiative.kerndatensatz.diagnose-2025.0.1.tgz");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create a support chain including the NPM Package Support
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(
                npmPackageSupport,
                new DefaultProfileValidationSupport(ctx),
                new CommonCodeSystemsTerminologyService(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                new SnapshotGeneratingValidationSupport(ctx));

        // Create a validator. Note that for good performance you can create as many validator objects
        // as you like, but you should reuse the same validation support object in all of the,.
        FhirValidator validator = ctx.newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupportChain);
        validator.registerValidatorModule(instanceValidator);


        // Perform the validation
        ValidationResult outcome = validator.validateWithResult(bundle);
    }

    public void assertBundle(Bundle bundle, String expectedClasspathJson) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        JSONObject actual = new JSONObject(parser.encodeResourceToString(bundle));
        JSONObject expected = loadJsonObject(expectedClasspathJson);
        JSONAssert.assertEquals(expected, actual, true);
    }

    private JSONObject loadJsonObject(String classpathLocation) {
        InputStream is = getClass().getResourceAsStream(classpathLocation);
        try {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file", e);
        }
    }

}
