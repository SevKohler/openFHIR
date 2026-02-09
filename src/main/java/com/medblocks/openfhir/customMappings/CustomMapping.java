package com.medblocks.openfhir.customMappings;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import com.medblocks.openfhir.util.OpenEhrPopulator;
import com.medblocks.openfhir.util.OpenFhirMapperUtils;
import com.medblocks.openfhir.util.OpenFhirStringUtils;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.r4.model.Base;

/**
 * Base class for in-code custom mappings referenced by mappingCode in model mappings.
 * Implementations should live in com.medblocks.openfhir.customMappings.
 */
public abstract class CustomMapping {

    /**
     * Mapping codes supported by this custom mapping.
     */
    public abstract Set<String> mappingCodes();

    /**
     * Apply a custom mapping from FHIR to openEHR.
     *
     * @return true if the mapping was applied.
     */
    public boolean applyFhirToOpenEhrMapping(final String mappingCode,
                                             final String openEhrPath,
                                             final Base fhirValue,
                                             final String openEhrType,
                                             final JsonObject flat,
                                             final OpenEhrPopulator populator,
                                             final OpenFhirMapperUtils mapperUtils,
                                             final OpenFhirStringUtils stringUtils) {
        return false;
    }

    /**
     * Apply a custom mapping from openEHR to FHIR.
     *
     * @return a DataWithIndex result, or null if not applicable.
     */
    public OpenEhrToFhirHelper.DataWithIndex applyOpenEhrToFhirMapping(final String mappingCode,
                                                                       final List<String> joinedValues,
                                                                       final JsonObject valueHolder,
                                                                       final Integer lastIndex,
                                                                       final String path,
                                                                       final String resourceType,
                                                                       final String fhirPath,
                                                                       final OpenFhirStringUtils stringUtils,
                                                                       final OpenFhirMapperUtils mapperUtils) {
        return null;
    }
}
