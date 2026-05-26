package report.generation.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "generate-report")
public class ReportGenerator extends AbstractMojo {

    @Parameter(property = "reportGenerator.sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private String sourceDirectory;

    @Parameter(property = "reportGenerator.outputFile", defaultValue = "${project.build.directory}/code-report.txt")
    private String outputFile;

    @Override
    public void execute() throws MojoExecutionException {
        File sourceFolder = new File(sourceDirectory);
        File reportFile = new File(outputFile);

        if (!sourceFolder.exists()) {
            throw new MojoExecutionException("Source folder does not exist: " + sourceFolder.getAbsolutePath());
        }

        try {
            reportFile.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile, StandardCharsets.UTF_8))) {
                scanFolder(sourceFolder, writer);
            }

            getLog().info("Successfully generated code report at: " + reportFile.getAbsolutePath());
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to generate code report.", exception);
        }
    }

    private void scanFolder(File folder, BufferedWriter writer) throws IOException {
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanFolder(file, writer);
            } else if (file.getName().endsWith(".java")) {
                analyzeJavaFile(file, writer);
            }
        }
    }

    private void analyzeJavaFile(File javaFile, BufferedWriter writer) throws IOException {
        String content = new String(Files.readAllBytes(javaFile.toPath()), StandardCharsets.UTF_8);

        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(content);

        if (classMatcher.find()) {
            String className = classMatcher.group(1);
            writer.write("\tClass: " + className);
            writer.newLine();

            writer.write("\tFields:");
            writer.newLine();
            Pattern fieldPattern = Pattern.compile("(public|private|protected)\\s+\\S+\\s+(\\w+)\\s*;");
            Matcher fieldMatcher = fieldPattern.matcher(content);

            while (fieldMatcher.find()) {
                String accessModifier = fieldMatcher.group(1);
                String fieldName = fieldMatcher.group(2);

                writer.write("\t\t" + accessModifier + " " + fieldName);
                writer.newLine();
            }

            writer.write("\tMethods:");
            writer.newLine();
            Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+\\S+\\s+(\\w+)\\s*(\\(.*?\\))");
            Matcher methodMatcher = methodPattern.matcher(content);

            while (methodMatcher.find()) {
                String accessModifier = methodMatcher.group(1);
                String methodName = methodMatcher.group(2);
                String parameters = methodMatcher.group(3);

                writer.write("\t\t" + accessModifier + " " + methodName + parameters);
                writer.newLine();
            }

            writer.newLine();
        }
    }
}