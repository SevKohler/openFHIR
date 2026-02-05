package com.medblocks.openfhir;

import com.medblocks.openfhir.fc.schema.model.Condition;
import com.medblocks.openfhir.fc.schema.model.Mapping;
import java.lang.reflect.Method;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ConditionOperatorSupportTest extends GenericTest {

    @Override
    protected void prepareState() {
        // no-op
    }

    @Test
    public void unsupportedFhirConditionOperatorToFhirIsRejected() throws Exception {
        Condition condition = new Condition().withOperator("typo");
        Mapping mapping = new Mapping().withName("test-mapping");

        boolean supported = invokeConditionSupport(openEhrToFhir, "isSupportedFhirConditionForToFhir",
                condition, mapping);

        assertFalse(supported);
    }

    @Test
    public void unsupportedOpenEhrConditionOperatorToFhirIsRejected() throws Exception {
        Condition condition = new Condition().withOperator("typo");
        Mapping mapping = new Mapping().withName("test-mapping");

        boolean supported = invokeConditionSupport(openEhrToFhir, "isSupportedOpenEhrConditionForToFhir",
                condition, mapping);

        assertFalse(supported);
    }

    @Test
    public void unsupportedFhirConditionOperatorToOpenEhrIsRejected() throws Exception {
        Condition condition = new Condition().withOperator("typo");
        Mapping mapping = new Mapping().withName("test-mapping");

        boolean supported = invokeConditionSupport(fhirToOpenEhr, "isSupportedFhirConditionForToOpenEhr",
                condition, mapping);

        assertFalse(supported);
    }

    @Test
    public void unsupportedOpenEhrConditionOperatorToOpenEhrIsRejected() throws Exception {
        Condition condition = new Condition().withOperator("typo");
        Mapping mapping = new Mapping().withName("test-mapping");

        boolean supported = invokeConditionSupport(fhirToOpenEhr, "isSupportedOpenEhrConditionForToOpenEhr",
                condition, mapping);

        assertFalse(supported);
    }

    private boolean invokeConditionSupport(final Object target,
                                           final String methodName,
                                           final Condition condition,
                                           final Mapping mapping) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, Condition.class, Mapping.class);
        method.setAccessible(true);
        return (boolean) method.invoke(target, condition, mapping);
    }
}
