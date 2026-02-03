package com.medblocks.openfhir.tofhir.parser;

import org.hl7.fhir.r4.model.Identifier;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class FhirDatatypeResolverTest {

    @Test
    public void resolve_referenceCast_identifier() {
        Optional<Class<?>> resolved = FhirDatatypeResolver.resolve(
                "ServiceRequest",
                "ServiceRequest.encounter.as(Reference).identifier");
        Assert.assertTrue(resolved.isPresent());
        Assert.assertEquals(Identifier.class, resolved.get());
    }

    @Test
    public void resolve_referenceCast_identifier_noPrefix() {
        Optional<Class<?>> resolved = FhirDatatypeResolver.resolve(
                "ServiceRequest",
                "encounter.as(Reference).identifier");
        Assert.assertTrue(resolved.isPresent());
        Assert.assertEquals(Identifier.class, resolved.get());
    }

    @Test
    public void resolve_referenceResolve_identifier() {
        Optional<Class<?>> resolved = FhirDatatypeResolver.resolve(
                "Encounter",
                "ServiceRequest.encounter.resolve().identifier");
        Assert.assertTrue(resolved.isPresent());
        Assert.assertEquals(Identifier.class, resolved.get());
    }
}
