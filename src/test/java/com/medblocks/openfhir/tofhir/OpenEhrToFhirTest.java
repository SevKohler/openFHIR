package com.medblocks.openfhir.tofhir;

import ca.uhn.fhir.context.FhirContext;
import com.medblocks.openfhir.tofhir.OpenEhrToFhir.FindingOuterMost;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Assert;
import org.junit.Test;

public class OpenEhrToFhirTest {

    @Test
    public void handleWhereInFhirPath() {
        final CodeableConcept lastObject = new CodeableConcept();
        lastObject.addCoding(new Coding("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "abc", null));
        lastObject.addCoding(new Coding("http://terminology.hl7.org/CodeSystem/icd-o-3", "123", null));
        lastObject.addCoding(new Coding("http://fhir.de/CodeSystem/bfarm/alpha-id", "4565", null));
        lastObject.addCoding(new Coding("http://www.orpha.net", "789", null));
        lastObject.addCoding(new Coding("http://snomed.info/sct", "135", null));

        final OpenEhrToFhir openEhrToFhir = new OpenEhrToFhir(null, null, null, null, null, null, null, null, null,
                                                              null, new FhirPathR4(FhirContext.forR4()), null, null,
                                                              null);
        final FindingOuterMost findingOuterMost = new FindingOuterMost(lastObject,
                                                                       ".coding.where(system.toString().lower() = 'http://fhir.de/codesystem/bfarm/icd-10-gm')");
        openEhrToFhir.handleWhereInFhirPath(findingOuterMost);
        Assert.assertEquals("http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            ((Coding) findingOuterMost.getLastObject()).getSystem());

    }
}