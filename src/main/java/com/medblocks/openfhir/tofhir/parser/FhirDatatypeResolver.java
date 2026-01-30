package com.medblocks.openfhir.tofhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.FhirTerser;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;
import java.util.Optional;

public final class FhirDatatypeResolver {

    private static final FhirContext CTX = FhirContext.forR4(); // reuse

    public FhirDatatypeResolver() {}

    public static Optional<Class<?>> resolve(String resourceType, String fhirPath) {
        if (!isValidInput(resourceType, fhirPath)) {
            return Optional.empty();
        }

        String normalized = normalizePath(resourceType, fhirPath);
        return resolveNormalized(resourceType, normalized);
    }

    private static Optional<Class<?>> resolveNormalized(String resourceType, String normalizedPath) {
        try {
            IBaseResource resource = newResourceInstance(resourceType);

            if (normalizedPath.isBlank()) {
                return Optional.of(resource.getClass());
            }

            return resolveFromInstance(resource, normalizedPath);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static boolean isValidInput(String resourceType, String fhirPath) {
        return isNotBlank(resourceType) && fhirPath != null;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static IBaseResource newResourceInstance(String resourceType) {
        return (IBaseResource) CTX.getResourceDefinition(resourceType).newInstance();
    }

    private static Optional<Class<?>> resolveFromInstance(IBaseResource resource, String fhirPath) {
        FhirTerser terser = CTX.newTerser();
        IBase current = resource;

        for (String rawPart : splitPath(fhirPath)) {
            String part = cleanSegment(rawPart);
            Optional<IBase> next = resolveNext(terser, current, part);

            if (next.isEmpty()) {
                return Optional.empty();
            }
            current = next.get();
        }

        return Optional.of(current.getClass());
    }

    private static String[] splitPath(String fhirPath) {
        return fhirPath.split("\\.");
    }

    private static String cleanSegment(String rawPart) {
        String noIndexes = rawPart.replaceAll("\\[.*?\\]", "");
        int whereIndex = noIndexes.indexOf("where(");
        return whereIndex >= 0 ? noIndexes.substring(0, whereIndex) : noIndexes;
    }

    private static Optional<IBase> resolveNext(FhirTerser terser, IBase current, String part) {
        if (part == null || part.isBlank()) {
            return Optional.empty();
        }

        Optional<IBase> existing = getFirstValueSafe(terser, current, part);
        return existing.isPresent() ? existing : createAndGetFirstValueSafe(terser, current, part);
    }

    private static Optional<IBase> getFirstValueSafe(FhirTerser terser, IBase current, String part) {
        try {
            return first(terser.getValues(current, part));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private static Optional<IBase> createAndGetFirstValueSafe(FhirTerser terser, IBase current, String part) {
        try {
            terser.addElement(current, part);
            return first(terser.getValues(current, part));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private static Optional<IBase> first(List<IBase> values) {
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    private static String normalizePath(String resourceType, String fhirPath) {
        String p = fhirPath == null ? "" : fhirPath.trim();
        if (p.isEmpty()) {
            return "";
        }

        String prefix = resourceType + ".";
        if (p.startsWith(prefix)) {
            return p.substring(prefix.length());
        }

        if (p.equals(resourceType)) {
            return "";
        }

        return p;
    }
}
