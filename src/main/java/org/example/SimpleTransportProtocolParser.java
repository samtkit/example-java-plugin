package org.example;

import org.jetbrains.annotations.NotNull;
import tools.samt.api.plugin.TransportConfiguration;
import tools.samt.api.plugin.TransportConfigurationParser;
import tools.samt.api.plugin.TransportConfigurationParserParams;
import tools.samt.api.types.OnewayOperation;
import tools.samt.api.types.ServiceOperation;

import java.util.HashMap;

public class SimpleTransportProtocolParser implements TransportConfigurationParser {
    private final SimpleTransportProtocolConfiguration emptyConfig = new SimpleTransportProtocolConfiguration(new HashMap<>());

    @NotNull
    @Override
    public String getTransportName() {
        return SimpleTransportProtocolConfiguration.name;
    }

    @NotNull
    @Override
    public TransportConfiguration parse(@NotNull TransportConfigurationParserParams params) {
        var config = params.getConfig();
        if (config == null) {
            return emptyConfig;
        }

        var pathsConfig = config.getFieldOrNull("paths");
        if (pathsConfig == null) {
            return emptyConfig;
        }

        var configs = new HashMap<ServiceOperation, String>();
        pathsConfig.getAsObject().getFields().forEach((serviceKey, operations) -> {
            final var service = serviceKey.getAsServiceName();
            operations.getAsObject().getFields().forEach((operationKey, path) -> {
                var operation = operationKey.asOperationName(service);
                if (operation instanceof OnewayOperation) {
                    params.reportError("Oneway operations are not supported", operationKey);
                }

                // A more complex config structure could be supported here.
                configs.put(operation, path.getAsValue().getAsString());
            });
        });

        return new SimpleTransportProtocolConfiguration(configs);
    }
}
