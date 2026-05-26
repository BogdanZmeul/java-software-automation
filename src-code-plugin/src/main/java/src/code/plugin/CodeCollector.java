package src.code.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Mojo(name = "collect")
public class CodeCollector extends AbstractMojo {
    @Parameter(property = "codeCollector.sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private String sourceDirectory;

    @Parameter(property = "codeCollector.outputFile", defaultValue = "${project.build.directory}/collected-sources.java")
    private String outputFile;

    @Override
    public void execute() throws MojoExecutionException {
        Path sourceDirectoryPath = Path.of(sourceDirectory);
        Path outputFilePath = Path.of(outputFile);

        if (Files.notExists(sourceDirectoryPath)) {
            getLog().error("Source directory does not exist: " + sourceDirectoryPath);
            return;
        }

        try {
            Path outputDirectory = outputFilePath.getParent();
            if (outputDirectory != null) {
                Files.createDirectories(outputDirectory);
            }
            List<Path> javaFiles = findJavaFiles(sourceDirectoryPath);
            writeCollectedSources(javaFiles, outputFilePath);

            getLog().info("Collected " + javaFiles.size() + " Java source file(s) into " + outputFilePath);
        } catch (IOException | UncheckedIOException exception) {
            throw new MojoExecutionException("Failed to collect Java source files", exception);
        }
    }

    private List<Path> findJavaFiles(Path sourceDirectoryPath) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        collectJavaFiles(sourceDirectoryPath, javaFiles);
        Collections.sort(javaFiles);
        return javaFiles;
    }

    private void collectJavaFiles(Path directory, List<Path> javaFiles) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            for (Path file : files) {
                if (Files.isDirectory(file)) {
                    collectJavaFiles(file, javaFiles);
                } else if (Files.isRegularFile(file) && file.toString().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }

    private void writeCollectedSources(List<Path> javaFiles, Path outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile(), StandardCharsets.UTF_8))) {
            for (Path javaFile : javaFiles) {
                writer.write("//Filename: " + javaFile);
                writer.newLine();

                try (BufferedReader reader = new BufferedReader(new FileReader(javaFile.toFile(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                writer.newLine();
            }
        }
    }
}
