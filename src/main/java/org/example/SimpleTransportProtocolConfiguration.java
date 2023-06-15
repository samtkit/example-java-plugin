package org.example;

import org.jetbrains.annotations.NotNull;
import tools.samt.api.plugin.TransportConfiguration;
import tools.samt.api.types.ServiceOperation;

import java.util.HashMap;

/**
 * Fictitious transport protocol that uses a simple configuration format.
 */
public record SimpleTransportProtocolConfiguration(
        HashMap<ServiceOperation, String> pathByOperation) implements TransportConfiguration {
    public static final String name = "STP";

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
