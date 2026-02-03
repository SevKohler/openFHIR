package com.medblocks.openfhir.tofhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.model.api.annotation.Child;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.lang.reflect.Field;
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
        Class<?> lastContainerClass = null;
        String lastContainerSegment = null;

        for (String rawPart : splitPath(fhirPath)) {
            String part = cleanSegment(rawPart);
            if (part.isBlank()) {
                continue;
            }
            if (RESOLVE.equals(part)) {
                Optional<Class<? extends IBaseResource>> target = resolveReferenceTargetType(lastContainerClass, lastContainerSegment);
                if (target.isEmpty()) {
                    return Optional.empty();
                }
                current = newResourceInstance(target.get().getSimpleName());
                continue;
            }
            Class<?> containerClass = current.getClass();
            Optional<IBase> next = resolveNext(terser, current, part);

            if (next.isEmpty()) {
                return Optional.empty();
            }
            current = next.get();
            lastContainerClass = containerClass;
            lastContainerSegment = part;
        }

        return Optional.of(current.getClass());
    }

    private static String[] splitPath(String fhirPath) {
        return fhirPath.split("\\.");
    }

    private static String cleanSegment(String rawPart) {
        String noIndexes = rawPart.replaceAll("\\[.*?\\]", "");
        int whereIndex = noIndexes.indexOf("where(");
        String noWhere = whereIndex >= 0 ? noIndexes.substring(0, whereIndex) : noIndexes;
        String trimmed = noWhere.trim();
        if (trimmed.startsWith("as(") && trimmed.endsWith(")")) {
            return "";
        }
        return noWhere;
    }

    private static Optional<Class<? extends IBaseResource>> resolveReferenceTargetType(Class<?> containerClass,
                                                                                       String segmentName) {
        if (containerClass == null || segmentName == null || segmentName.isBlank()) {
            return Optional.empty();
        }
        Field field = findChildField(containerClass, segmentName);
        if (field == null) {
            return Optional.empty();
        }
        Child child = field.getAnnotation(Child.class);
        if (child == null) {
            return Optional.empty();
        }
        Class<? extends IBase>[] types = child.type();
        if (types == null || types.length == 0) {
            return Optional.empty();
        }
        for (Class<? extends IBase> type : types) {
            if (IBaseResource.class.isAssignableFrom(type)) {
                @SuppressWarnings("unchecked")
                Class<? extends IBaseResource> resourceType = (Class<? extends IBaseResource>) type;
                return Optional.of(resourceType);
            }
        }
        return Optional.empty();
    }

    private static Field findChildField(Class<?> containerClass, String segmentName) {
        Class<?> current = containerClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Child child = field.getAnnotation(Child.class);
                if (child == null) {
                    continue;
                }
                String childName = child.name();
                if (segmentName.equals(childName) || segmentName.equals(field.getName())) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static final String RESOLVE = "resolve()";

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

        int resolveIndex = p.lastIndexOf(RESOLVE);
        if (resolveIndex >= 0) {
            int start = resolveIndex + RESOLVE.length();
            if (start < p.length() && p.charAt(start) == '.') {
                start++;
            }
            return p.substring(start);
        }

        return p;
    }
}
