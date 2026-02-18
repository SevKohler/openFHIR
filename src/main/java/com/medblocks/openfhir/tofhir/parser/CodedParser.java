package com.medblocks.openfhir.tofhir.parser;

import com.google.gson.JsonObject;
import com.medblocks.openfhir.tofhir.OpenEhrToFhirHelper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

import java.util.List;
import java.util.Optional;

public class CodedParser {

    private final FhirValueReaders fhirValueReaders;

    public CodedParser(FhirValueReaders readers) {
        this.fhirValueReaders = readers;
    }

    public OpenEhrToFhirHelper.DataWithIndex codeableConcept(List<String> joinedValues,
                                                             JsonObject valueHolder,
                                                             Integer lastIndex,
                                                             String path,
                                                             Optional<Class<?>> fhirAttrType) {
        String base = fhirValueReaders.basePath(path);

        if (fhirAttrType.isPresent() && fhirAttrType.get().equals(Identifier.class)) {
            Identifier id = new Identifier();
            id.setSystem(fhirValueReaders.cleanVersionFromSystem(fhirValueReaders.get(valueHolder, base, "terminology", find(joinedValues, "terminology"))));
            id.setValue(fhirValueReaders.get(valueHolder, base, "code", find(joinedValues, "code")));
            return new OpenEhrToFhirHelper.DataWithIndex(id, lastIndex, base);
        }

        CodeableConcept codeableConcept = new CodeableConcept();

        String fallbackValue = find(joinedValues, "value");
        String fallbackCode = find(joinedValues, "code");
        String fallbackTerminology = find(joinedValues, "terminology");
        String fallbackOrdinal = find(joinedValues, "ordinal");

        String text = fhirValueReaders.get(valueHolder, base, "value", fallbackValue);
        String code = fhirValueReaders.get(valueHolder, base, "code", fallbackCode);
        String systemRaw = fhirValueReaders.get(valueHolder, base, "terminology", fallbackTerminology);
        String system = fhirValueReaders.cleanVersionFromSystem(systemRaw);
        String version = fhirValueReaders.version(systemRaw);
        String ordinal = fhirValueReaders.get(valueHolder, base, "ordinal", fallbackOrdinal);

        codeableConcept.setText(text);

        if (code != null || system != null) {
            Coding coding = new Coding(system, code, text);
            if (version != null) coding.setVersion(version);
            codeableConcept.addCoding(coding);
        }

        if (ordinal != null) codeableConcept.setText(ordinal);

        addMappings(valueHolder, base, codeableConcept);
        return new OpenEhrToFhirHelper.DataWithIndex(codeableConcept, lastIndex, base);
    }

    public OpenEhrToFhirHelper.DataWithIndex coding(List<String> joinedValues,
                                                    JsonObject valueHolder,
                                                    Integer lastIndex,
                                                    String path) {
        String base = fhirValueReaders.basePath(path);

        String fallbackValue = find(joinedValues, "value");
        String fallbackCode = find(joinedValues, "code");
        String fallbackTerminology = find(joinedValues, "terminology");

        String display = fhirValueReaders.get(valueHolder, base, "value", fallbackValue);
        String code = fhirValueReaders.get(valueHolder, base, "code", fallbackCode);
        String systemRaw = fhirValueReaders.get(valueHolder, base, "terminology", fallbackTerminology);
        String system = fhirValueReaders.cleanVersionFromSystem(systemRaw);
        String version = fhirValueReaders.version(systemRaw);

        // Avoid emitting redundant display values when openEHR text equals the code.
        if (display != null && code != null && display.equals(code)) {
            display = null;
        }

        Coding coding = new Coding(system, code, display);
        if (version != null) coding.setVersion(version);

        return new OpenEhrToFhirHelper.DataWithIndex(coding, lastIndex, base);
    }

    private void addMappings(JsonObject valueHolder, String basePath, CodeableConcept cc) {
        for (int i = 0; ; i++) {
            String prefix = basePath + "/_mapping:" + i;
            if (!fhirValueReaders.mappingExists(valueHolder, prefix)) break;

            String system = fhirValueReaders.cleanVersionFromSystem(fhirValueReaders.get(valueHolder, prefix + "/target|terminology"));
            String code = fhirValueReaders.get(valueHolder, prefix + "/target|code");
            String display = fhirValueReaders.get(valueHolder, prefix + "/target|preferred_term");

            if (system != null || code != null) cc.addCoding(new Coding(system, code, display));
        }
    }

    private String find(List<String> joinedValues, String suffix) {
        return joinedValues.stream().filter(s -> s.endsWith(suffix)).findFirst().orElse(null);
    }
}
