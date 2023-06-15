package org.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.samt.api.plugin.CodegenFile;
import tools.samt.api.plugin.Generator;
import tools.samt.api.plugin.GeneratorParams;
import tools.samt.api.types.*;

import java.util.List;

public class PlantUMLGenerator implements Generator {

    @NotNull
    @Override
    public String getName() {
        return "plantuml";
    }

    @NotNull
    private String getPlantUMLName(@NotNull UserType userType) {
        return getPlantUMLName(userType.getQualifiedName());
    }

    @NotNull
    @Override
    public List<CodegenFile> generate(@NotNull GeneratorParams generatorParams) {
        // Allow users to specify the type of diagram to generate.
        var type = generatorParams.getOptions().getOrDefault("type", "class");

        var result = "";
        if (type.equals("class")) {
            result = generateClassDiagram(generatorParams);
        } else if (type.equals("component")) {
            result = generateComponentDiagram(generatorParams);
        } else {
            generatorParams.reportError("Unknown type: " + type);
        }

        // Users can override the output file name.
        var fileName = generatorParams.getOptions().getOrDefault("file", "diagram.puml");

        return List.of(new CodegenFile(fileName, result));
    }

    @NotNull
    private String generateClassDiagram(@NotNull GeneratorParams generatorParams) {
        var sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("hide empty members\n");

        // Example of how to iterate through all types and quickly generate some code.
        for (SamtPackage samtPackage : generatorParams.getPackages()) {
            for (RecordType record : samtPackage.getRecords()) {
                sb.append("class ").append(record.getName()).append(" {\n");
                for (var field : record.getFields()) {
                    sb.append("  +").append(field.getName()).append(": ").append(getPlantUMLType(field.getType())).append("\n");
                }
                sb.append("}\n");
            }
            for (EnumType enumType : samtPackage.getEnums()) {
                sb.append("enum ").append(enumType.getName()).append(" {\n");
                for (var value : enumType.getValues()) {
                    sb.append("  ").append(value).append("\n");
                }
                sb.append("}\n");
            }
            for (ServiceType serviceType : samtPackage.getServices()) {
                sb.append("interface ").append(serviceType.getName()).append(" {\n");
                for (var operation : serviceType.getOperations()) {
                    sb.append("  +").append(operation.getName()).append("(");
                    var first = true;
                    for (var parameter : operation.getParameters()) {
                        if (!first) {
                            sb.append(", ");
                        }
                        first = false;
                        sb.append(parameter.getName()).append(": ").append(getPlantUMLType(parameter.getType()));
                    }
                    sb.append(")");
                    if (operation instanceof RequestResponseOperation requestResponseOperation) {
                        sb.append(": ").append(getPlantUMLType(requestResponseOperation.getReturnType()));
                    }
                    sb.append("\n");
                }
                sb.append("}\n");
            }
        }
        sb.append("@enduml\n");
        return sb.toString();
    }

    @NotNull
    private String generateComponentDiagram(@NotNull GeneratorParams generatorParams) {
        var sb = new StringBuilder();
        sb.append("@startuml\n");

        generatorParams.getPackages().forEach((samtPackage) -> {
            if (!samtPackage.getConsumers().isEmpty() || !samtPackage.getProviders().isEmpty()) {
                var packageName = samtPackage.getQualifiedName();
                sb.append("package \"").append(packageName).append("\" as ").append(getPlantUMLName(packageName)).append(" {\n");
                for (ProviderType provider : samtPackage.getProviders()) {
                    sb.append("  [").append(provider.getName()).append("]").append(" as ").append(getPlantUMLName(provider)).append("\n");
                }
                sb.append("}\n");
            }
        });

        generatorParams.getPackages().forEach((samtPackage) -> {
            if (!samtPackage.getConsumers().isEmpty() || !samtPackage.getProviders().isEmpty()) {
                var packageName = samtPackage.getQualifiedName();
                for (ConsumerType consumer : samtPackage.getConsumers()) {
                    var provider = getPlantUMLName(consumer.getProvider());
                    var transport = consumer.getProvider().getTransport().getName();
                    sb.append(getPlantUMLName(packageName)).append(" --> ").append(provider).append(" : ").append(transport).append("\n");
                }
            }
        });
        sb.append("@enduml\n");
        return sb.toString();
    }

    /**
     * Gets a corresponding PlantUML type string.
     */
    @NotNull
    private String getPlantUMLType(@Nullable TypeReference typeReference) {
        if (typeReference == null) {
            return "Void";
        }
        var type = typeReference.getType();
        if (type instanceof AliasType aliasType) {
            // Use the runtime type for aliases, we don't want to show the alias name.
            return getPlantUMLType(aliasType.getRuntimeType());
        }
        if (type instanceof UserType userType) {
            return userType.getName();
        }
        if (type instanceof IntType) {
            return "Int";
        }
        if (type instanceof LongType) {
            return "Long";
        }
        if (type instanceof FloatType) {
            return "Float";
        }
        if (type instanceof DoubleType) {
            return "Double";
        }
        if (type instanceof DecimalType) {
            return "Decimal";
        }
        if (type instanceof BooleanType) {
            return "Boolean";
        }
        if (type instanceof StringType) {
            return "String";
        }
        if (type instanceof BytesType) {
            return "Bytes";
        }
        if (type instanceof DateType) {
            return "Date";
        }
        if (type instanceof DateTimeType) {
            return "DateTime";
        }
        if (type instanceof DurationType) {
            return "Duration";
        }
        if (type instanceof ListType listType) {
            return "List<" + getPlantUMLType(listType.getElementType()) + ">";
        }
        if (type instanceof MapType mapType) {
            return "Map<" + getPlantUMLType(mapType.getKeyType()) + ", " + getPlantUMLType(mapType.getValueType()) + ">";
        }
        return "UNKNOWN";
    }

    /**
     * PlantUML does not like certain characters in their identifiers, so we need to replace them.
     */
    @NotNull
    private String getPlantUMLName(@NotNull String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
