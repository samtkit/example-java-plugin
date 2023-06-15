# SAMT Example Java Plugin

This repository contains a sample SAMT plugin written in Java.
you should read the accompanying [tutorial](https://github.com/samtkit/core/wiki/Authoring-Generators) to understand how to create your own plugin.

## Sample Generator - PlantUML

This plugin contains a very basic [PlantUML](https://plantuml.com/) generator to show how to use the SAMT API.
It is not feature complete in any way, but it should be enough to get you started.

## Sample Transport - STP

This plugin contains a custom parser for the fictional Simple Transport Protocol (STP).
The following is a sample configuration file for the STP transport:

```
transport STP {
    paths: {
    ^^^^^ Top-level field name

        FooService: {
        ^^^^^^^^^^ Service name

            fooOperation: "Whatever",
            ^^^^^^^^^^^^ Operation name
        },

        ...
    }
}
```
