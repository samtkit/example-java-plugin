import org.example.PlantUMLGenerator;
import org.example.SimpleTransportProtocolParser;
import tools.samt.api.plugin.CodegenFile;
import tools.samt.codegen.Codegen;
import tools.samt.common.DiagnosticController;
import tools.samt.common.FilesKt;
import tools.samt.common.SamtGeneratorConfiguration;
import tools.samt.config.SamtConfigurationParser;
import tools.samt.lexer.Lexer;
import tools.samt.parser.Parser;
import tools.samt.semantic.SemanticModel;

import java.io.StringReader;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        // Parse saml.yaml from the test resources
        var config = SamtConfigurationParser.parseConfiguration(Path.of("src/test/resources/samt.yaml"));
        var sourceDirectory = config.getSource().toUri();

        // This will hold all diagnostic messages (errors, warnings, etc.)
        var diagnosticController = new DiagnosticController(sourceDirectory);

        // Parse all .samt files in the test resources
        var sourceFiles = FilesKt.readSamtSource(FilesKt.collectSamtFiles(sourceDirectory), diagnosticController);

        // Parse all files and build a semantic model
        var fileNodes = sourceFiles.stream().map(sourceFile -> {
            var diagnostics = diagnosticController.getOrCreateContext(sourceFile);
            var tokens = Lexer.scan(new StringReader(sourceFile.getContent()), diagnostics);
            return Parser.parse(sourceFile, tokens, diagnostics);
        }).toList();
        var semanticModel = SemanticModel.build(fileNodes, diagnosticController);

        // Register custom transport protocol parser and generator
        Codegen.registerTransportParser(new SimpleTransportProtocolParser());
        Codegen.registerGenerator(new PlantUMLGenerator());

        if (diagnosticController.hasErrors()) {
            // Nicely printing all errors isn't easy here, it's best to use a debugger to analyze the diagnosticController
            System.out.println("Has errors!");
            return;
        }

        // Run all configured generators
        for (SamtGeneratorConfiguration generatorConfiguration : config.getGenerators()) {
            // Print all generated files
            for (CodegenFile codegenFile : Codegen.generate(semanticModel, generatorConfiguration, diagnosticController)) {
                System.out.println(codegenFile.getFilepath());
                System.out.println("-".repeat(codegenFile.getFilepath().length()));
                System.out.println(codegenFile.getSource());
            }
        }
    }
}
