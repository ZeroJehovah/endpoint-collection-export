package com.personal.brunohelper.service;

import com.intellij.psi.PsiType;
import com.personal.brunohelper.model.ControllerExportModel;
import com.personal.brunohelper.model.EndpointExportModel;
import com.personal.brunohelper.model.EndpointParameterModel;
import com.personal.brunohelper.model.ParameterSource;
import com.personal.brunohelper.model.RequestBodyModel;
import com.personal.brunohelper.openapi.PsiTypeSchemaResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class BrunoCollectionWriter {

    private static final String COLLECTION_FILE = "opencollection.yml";
    private static final String MARKER_FILE = ".bruno-helper.yml";
    private static final String GITIGNORE_FILE = ".gitignore";
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}/]+)}");
    private static final Map<String, Integer> HTTP_METHOD_ORDER = Map.of(
            "GET", 1,
            "POST", 2,
            "PUT", 3,
            "PATCH", 4,
            "DELETE", 5,
            "OPTIONS", 6,
            "HEAD", 7
    );

    public GenerationResult writeCollection(ControllerExportModel model, Path collectionDirectory) throws IOException {
        return writePreparedCollection(prepareCollection(model, collectionDirectory));
    }

    public PreparedCollection prepareCollection(ControllerExportModel model, Path collectionDirectory) {
        String collectionName = BrunoExportOptions.deriveCollectionName(model.getControllerName());
        return new PreparedCollection(
                collectionName,
                model.getControllerName(),
                collectionDirectory,
                buildRequestFiles(model)
        );
    }

    public GenerationResult writePreparedCollection(PreparedCollection preparedCollection) throws IOException {
        prepareCollectionDirectory(preparedCollection.collectionDirectory());
        Files.createDirectories(preparedCollection.collectionDirectory());

        writeFile(preparedCollection.collectionDirectory().resolve(COLLECTION_FILE), renderCollectionFile(preparedCollection.collectionName()));
        writeFile(preparedCollection.collectionDirectory().resolve(MARKER_FILE), renderMarkerFile(preparedCollection.controllerName()));
        writeFile(preparedCollection.collectionDirectory().resolve(GITIGNORE_FILE), renderGitIgnoreFile());

        int sequence = 1;
        for (RequestFile requestFile : preparedCollection.requestFiles()) {
            String fileName = String.format(Locale.ROOT, "%03d-%s.yml", sequence, requestFile.fileSlug());
            writeFile(preparedCollection.collectionDirectory().resolve(fileName), renderRequestFile(requestFile, sequence));
            sequence++;
        }

        return new GenerationResult(preparedCollection.collectionName(), preparedCollection.collectionDirectory(), sequence - 1);
    }

    private void prepareCollectionDirectory(Path collectionDirectory) throws IOException {
        if (!Files.exists(collectionDirectory)) {
            return;
        }
        if (Files.exists(collectionDirectory.resolve(MARKER_FILE)) || looksLikeBrunoCollection(collectionDirectory)) {
            deleteRecursively(collectionDirectory);
            return;
        }
        try (Stream<Path> children = Files.list(collectionDirectory)) {
            if (children.findAny().isEmpty()) {
                return;
            }
        }
        throw new IOException("目标目录已存在且不是 Bruno Helper 生成的 Collection: " + collectionDirectory);
    }

    private boolean looksLikeBrunoCollection(Path collectionDirectory) throws IOException {
        if (Files.exists(collectionDirectory.resolve(COLLECTION_FILE))) {
            return true;
        }
        try (Stream<Path> children = Files.list(collectionDirectory)) {
            return children.anyMatch(path -> {
                String fileName = path.getFileName().toString();
                return fileName.endsWith(".bru");
            });
        }
    }

    private void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private List<RequestFile> buildRequestFiles(ControllerExportModel model) {
        List<RequestFile> requestFiles = new ArrayList<>();
        for (EndpointExportModel endpoint : model.getEndpoints()) {
            List<String> methods = endpoint.getHttpMethods().stream()
                    .sorted(Comparator.comparingInt(method -> HTTP_METHOD_ORDER.getOrDefault(method, Integer.MAX_VALUE)))
                    .toList();
            for (String path : endpoint.getPaths()) {
                for (String method : methods) {
                    requestFiles.add(buildRequestFile(endpoint, method, path));
                }
            }
        }
        return requestFiles;
    }

    private RequestFile buildRequestFile(EndpointExportModel endpoint, String method, String rawPath) {
        PsiTypeSchemaResolver resolver = new PsiTypeSchemaResolver();
        String path = normalizeRequestPath(rawPath);
        String requestName = buildRequestName(endpoint, method, path);

        boolean multipart = endpoint.getParameters().stream().anyMatch(this::isMultipartField);
        boolean formEncoded = !multipart
                && endpoint.getRequestBody() == null
                && endpoint.getParameters().stream().anyMatch(this::isFormObjectParameter);

        List<ParamEntry> params = new ArrayList<>();
        List<HeaderEntry> headers = new ArrayList<>();

        for (EndpointParameterModel parameter : endpoint.getParameters()) {
            if (parameter.getSource() == ParameterSource.PATH_VARIABLE) {
                params.add(new ParamEntry(parameter.getName(), parameterValue(parameter, resolver), "path"));
                continue;
            }
            if (parameter.getSource() == ParameterSource.REQUEST_HEADER) {
                headers.add(new HeaderEntry(parameter.getName(), parameterValue(parameter, resolver)));
                continue;
            }
            if (multipart && isMultipartField(parameter)) {
                continue;
            }
            if (formEncoded && isFormObjectParameter(parameter)) {
                continue;
            }
            if (parameter.getSource() == ParameterSource.MODEL_ATTRIBUTE || parameter.getSource() == ParameterSource.IMPLICIT_MODEL) {
                for (PsiTypeSchemaResolver.PropertyDescriptor property : resolver.expandObjectProperties(parameter.getType())) {
                    params.add(new ParamEntry(property.name(), scalarValue(property.schema(), resolver.buildComponents()), "query"));
                }
                continue;
            }
            if (parameter.getSource() == ParameterSource.REQUEST_BODY) {
                continue;
            }
            params.add(new ParamEntry(parameter.getName(), parameterValue(parameter, resolver), "query"));
        }

        BodyEntry body = buildBody(endpoint, resolver, multipart, formEncoded);
        if (body != null && body.type().equals("json")) {
            addHeaderIfAbsent(headers, "Content-Type", "application/json");
        }
        if (body != null && body.type().equals("form-urlencoded")) {
            addHeaderIfAbsent(headers, "Content-Type", "application/x-www-form-urlencoded");
        }
        if (body != null && body.type().equals("text")) {
            addHeaderIfAbsent(headers, "Content-Type", "text/plain");
        }
        if (body != null && body.type().equals("xml")) {
            addHeaderIfAbsent(headers, "Content-Type", "application/xml");
        }

        return new RequestFile(
                requestName,
                buildFileSlug(method, path, endpoint.getOperationId()),
                method,
                "{{baseUrl}}" + path,
                params,
                headers,
                body,
                buildDocs(endpoint)
        );
    }

    private String buildRequestName(EndpointExportModel endpoint, String method, String path) {
        String summary = endpoint.getSummary();
        if (summary != null && !summary.isBlank()) {
            return summary;
        }
        return method + " " + path;
    }

    private String buildFileSlug(String method, String path, String operationId) {
        String basis = method + "-" + path;
        if (operationId != null && !operationId.isBlank()) {
            basis = basis + "-" + operationId;
        }
        return BrunoExportOptions.sanitizeFileSystemName(basis);
    }

    private String buildDocs(EndpointExportModel endpoint) {
        if (!endpoint.getDescription().isBlank()) {
            return endpoint.getDescription();
        }
        return endpoint.getSummary();
    }

    private BodyEntry buildBody(
            EndpointExportModel endpoint,
            PsiTypeSchemaResolver resolver,
            boolean multipart,
            boolean formEncoded
    ) {
        RequestBodyModel explicitBody = endpoint.getRequestBody();
        if (explicitBody != null) {
            String bodyType = resolveBodyType(explicitBody.getContentType());
            if ("json".equals(bodyType)) {
                Object example = exampleValue(explicitBody.getType(), resolver);
                return new BodyEntry("json", Json.pretty(example == null ? Map.of() : example), List.of());
            }
            return new BodyEntry(bodyType, "", List.of());
        }

        if (multipart) {
            List<FormEntry> formEntries = new ArrayList<>();
            for (EndpointParameterModel parameter : endpoint.getParameters()) {
                if (!isMultipartField(parameter)) {
                    continue;
                }
                if (parameter.getSource() == ParameterSource.MODEL_ATTRIBUTE || parameter.getSource() == ParameterSource.IMPLICIT_MODEL) {
                    for (PsiTypeSchemaResolver.PropertyDescriptor property : resolver.expandObjectProperties(parameter.getType())) {
                        formEntries.add(new FormEntry(property.name(), scalarValue(property.schema(), resolver.buildComponents())));
                    }
                } else {
                    formEntries.add(new FormEntry(parameter.getName(), parameterValue(parameter, resolver)));
                }
            }
            return formEntries.isEmpty() ? null : new BodyEntry("multipart-form", "", formEntries);
        }

        if (formEncoded) {
            List<FormEntry> formEntries = new ArrayList<>();
            for (EndpointParameterModel parameter : endpoint.getParameters()) {
                if (!isFormObjectParameter(parameter)) {
                    continue;
                }
                for (PsiTypeSchemaResolver.PropertyDescriptor property : resolver.expandObjectProperties(parameter.getType())) {
                    formEntries.add(new FormEntry(property.name(), scalarValue(property.schema(), resolver.buildComponents())));
                }
            }
            return formEntries.isEmpty() ? null : new BodyEntry("form-urlencoded", "", formEntries);
        }

        return null;
    }

    private Object exampleValue(PsiType type, PsiTypeSchemaResolver resolver) {
        Schema<?> schema = resolver.resolveSchema(type);
        Components components = resolver.buildComponents();
        Map<String, Schema> schemas = components == null || components.getSchemas() == null
                ? Map.of()
                : components.getSchemas();
        return exampleValue(schema, schemas, Set.of());
    }

    private Object exampleValue(Schema<?> schema, Map<String, Schema> components, Set<String> visitingRefs) {
        if (schema == null) {
            return null;
        }
        if (schema.get$ref() != null) {
            String reference = schema.get$ref();
            String componentName = reference.substring(reference.lastIndexOf('/') + 1);
            if (visitingRefs.contains(componentName)) {
                return Map.of();
            }
            Schema<?> componentSchema = components.get(componentName);
            if (componentSchema == null) {
                return Map.of();
            }
            java.util.LinkedHashSet<String> nextRefs = new java.util.LinkedHashSet<>(visitingRefs);
            nextRefs.add(componentName);
            return exampleValue(componentSchema, components, nextRefs);
        }
        if (schema instanceof ArraySchema arraySchema) {
            Object itemValue = exampleValue(arraySchema.getItems(), components, visitingRefs);
            return itemValue == null ? List.of() : List.of(itemValue);
        }
        if (schema instanceof MapSchema mapSchema) {
            Object value = exampleValue((Schema<?>) mapSchema.getAdditionalProperties(), components, visitingRefs);
            return Map.of("key", value == null ? "" : value);
        }
        if (schema instanceof BinarySchema) {
            return "<binary>";
        }
        if (schema instanceof BooleanSchema) {
            return Boolean.TRUE;
        }
        if (schema instanceof IntegerSchema) {
            return 0;
        }
        if (schema instanceof NumberSchema) {
            return 0.0;
        }
        if (schema instanceof ObjectSchema || schema.getProperties() != null) {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Schema> properties = schema.getProperties();
            if (properties != null) {
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    Object value = exampleValue(entry.getValue(), components, visitingRefs);
                    result.put(entry.getKey(), value == null ? "" : value);
                }
            }
            return result;
        }
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return String.valueOf(schema.getEnum().get(0));
        }
        return "";
    }

    private String scalarValue(Schema<?> schema, Components components) {
        Map<String, Schema> componentMap = components == null || components.getSchemas() == null
                ? Map.of()
                : components.getSchemas();
        Object value = exampleValue(schema, componentMap, Set.of());
        return value == null ? "" : String.valueOf(value);
    }

    private String parameterValue(EndpointParameterModel parameter, PsiTypeSchemaResolver resolver) {
        if (parameter.getDefaultValue() != null && !parameter.getDefaultValue().isBlank()) {
            return parameter.getDefaultValue();
        }
        return scalarValue(resolver.resolveSchema(parameter.getType()), resolver.buildComponents());
    }

    private boolean isMultipartField(EndpointParameterModel parameter) {
        return parameter.getSource() == ParameterSource.REQUEST_PART
                || parameter.getSource() == ParameterSource.REQUEST_PARAM
                || parameter.getSource() == ParameterSource.IMPLICIT_SIMPLE
                || parameter.getSource() == ParameterSource.MODEL_ATTRIBUTE
                || parameter.getSource() == ParameterSource.IMPLICIT_MODEL;
    }

    private boolean isFormObjectParameter(EndpointParameterModel parameter) {
        return parameter.getSource() == ParameterSource.MODEL_ATTRIBUTE
                || parameter.getSource() == ParameterSource.IMPLICIT_MODEL;
    }

    private void addHeaderIfAbsent(List<HeaderEntry> headers, String name, String value) {
        boolean exists = headers.stream().anyMatch(header -> header.name().equalsIgnoreCase(name));
        if (!exists) {
            headers.add(new HeaderEntry(name, value));
        }
    }

    private String resolveBodyType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "json";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        if (normalized.contains("json")) {
            return "json";
        }
        if (normalized.contains("xml")) {
            return "xml";
        }
        if (normalized.contains("x-www-form-urlencoded")) {
            return "form-urlencoded";
        }
        if (normalized.contains("multipart/form-data")) {
            return "multipart-form";
        }
        return "text";
    }

    private String normalizeRequestPath(String rawPath) {
        String normalized = rawPath == null || rawPath.isBlank() ? "/" : rawPath;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(normalized);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ":" + matcher.group(1));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private String renderCollectionFile(String collectionName) {
        StringBuilder builder = new StringBuilder();
        builder.append("opencollection: 1.0.0\n\n");
        builder.append("info:\n");
        builder.append("  name: ").append(yamlString(collectionName)).append('\n');
        builder.append("bundled: false\n");
        builder.append("extensions:\n");
        builder.append("  bruno:\n");
        builder.append("    ignore:\n");
        builder.append("      - node_modules\n");
        builder.append("      - .git\n");
        return builder.toString();
    }

    private String renderMarkerFile(String controllerName) {
        return "generatedBy: Bruno Helper\ncontroller: " + yamlString(controllerName) + "\n";
    }

    private String renderGitIgnoreFile() {
        return "node_modules\n.git\n";
    }

    private String renderRequestFile(RequestFile requestFile, int sequence) {
        StringBuilder builder = new StringBuilder();
        builder.append("info:\n");
        builder.append("  name: ").append(yamlString(requestFile.name())).append('\n');
        builder.append("  type: http\n");
        builder.append("  seq: ").append(sequence).append("\n\n");

        builder.append("http:\n");
        builder.append("  method: ").append(requestFile.method()).append('\n');
        builder.append("  url: ").append(yamlString(requestFile.url())).append('\n');

        if (!requestFile.params().isEmpty()) {
            builder.append("  params:\n");
            for (ParamEntry param : requestFile.params()) {
                builder.append("    - name: ").append(yamlString(param.name())).append('\n');
                builder.append("      value: ").append(yamlString(param.value())).append('\n');
                builder.append("      type: ").append(yamlString(param.type())).append('\n');
            }
        }

        if (!requestFile.headers().isEmpty()) {
            builder.append("  headers:\n");
            for (HeaderEntry header : requestFile.headers()) {
                builder.append("    - name: ").append(yamlString(header.name())).append('\n');
                builder.append("      value: ").append(yamlString(header.value())).append('\n');
            }
        }

        if (requestFile.body() != null) {
            builder.append("  body:\n");
            builder.append("    type: ").append(yamlString(requestFile.body().type())).append('\n');
            if ("json".equals(requestFile.body().type())) {
                builder.append("    data: |-\n");
                appendIndentedBlock(builder, requestFile.body().rawData(), 6);
            } else if (!requestFile.body().formData().isEmpty()) {
                builder.append("    data:\n");
                for (FormEntry formEntry : requestFile.body().formData()) {
                    builder.append("      - name: ").append(yamlString(formEntry.name())).append('\n');
                    builder.append("        value: ").append(yamlString(formEntry.value())).append('\n');
                }
            } else if (!requestFile.body().rawData().isBlank()) {
                builder.append("    data: |-\n");
                appendIndentedBlock(builder, requestFile.body().rawData(), 6);
            }
        }

        builder.append("\nsettings:\n");
        builder.append("  encodeUrl: true\n");
        builder.append("  timeout: 0\n");
        builder.append("  followRedirects: true\n");
        builder.append("  maxRedirects: 5\n");

        if (requestFile.docs() != null && !requestFile.docs().isBlank()) {
            builder.append("\ndocs: |-\n");
            appendIndentedBlock(builder, requestFile.docs(), 2);
        }

        return builder.toString();
    }

    private void appendIndentedBlock(StringBuilder builder, String text, int indent) {
        String prefix = " ".repeat(indent);
        String normalized = text == null ? "" : text.replace("\r\n", "\n");
        for (String line : normalized.split("\n", -1)) {
            builder.append(prefix).append(line).append('\n');
        }
    }

    private String yamlString(String value) {
        String normalized = value == null ? "" : value;
        return "\"" + normalized
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    public record GenerationResult(String collectionName, Path collectionDirectory, int requestCount) {
    }

    record PreparedCollection(
            String collectionName,
            String controllerName,
            Path collectionDirectory,
            List<RequestFile> requestFiles
    ) {
    }

    private record RequestFile(
            String name,
            String fileSlug,
            String method,
            String url,
            List<ParamEntry> params,
            List<HeaderEntry> headers,
            BodyEntry body,
            String docs
    ) {
    }

    private record ParamEntry(String name, String value, String type) {
    }

    private record HeaderEntry(String name, String value) {
    }

    private record FormEntry(String name, String value) {
    }

    private record BodyEntry(String type, String rawData, List<FormEntry> formData) {
    }
}
