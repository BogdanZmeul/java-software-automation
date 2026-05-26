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
        String content = Files.readString(javaFile.toPath());

        Pattern typePattern = Pattern.compile("(class|interface|enum|record)\\s+(\\w+)");
        Matcher typeMatcher = typePattern.matcher(content);

        if (typeMatcher.find()) {
            String typeKeyword = typeMatcher.group(1);
            String typeName = typeMatcher.group(2);

            String capitalizedKeyword = typeKeyword.substring(0, 1).toUpperCase() + typeKeyword.substring(1);

            writer.write(capitalizedKeyword + ": " + typeName);
            writer.newLine();

            Pattern fieldPattern = Pattern.compile("(public|private|protected)\\s+(.*?)\\s+(\\w+)\\s*(=|;)");
            Matcher fieldMatcher = fieldPattern.matcher(content);

            if (fieldMatcher.find()) {
                writer.write("Fields:");
                writer.newLine();
                do {
                    String accessModifier = fieldMatcher.group(1);
                    String typeAndExtras = fieldMatcher.group(2);
                    String fieldName = fieldMatcher.group(3);

                    writer.write("\t" + accessModifier + " " + typeAndExtras + " " + fieldName);
                    writer.newLine();
                } while (fieldMatcher.find());
            } else {
                writer.write("Fields: absent");
                writer.newLine();
            }

            Pattern constructorPattern = Pattern.compile("(public|private|protected)\\s+(" + typeName + ")\\s*(\\(.*?\\))");
            Matcher constructorMatcher = constructorPattern.matcher(content);

            if (constructorMatcher.find()) {
                writer.write("Constructors:");
                writer.newLine();
                do {
                    String accessModifier = constructorMatcher.group(1);
                    String constructorName = constructorMatcher.group(2);
                    String parameters = constructorMatcher.group(3);

                    writer.write("\t" + accessModifier + " " + constructorName + parameters);
                    writer.newLine();
                } while (constructorMatcher.find());
            } else {
                writer.write("Constructors: absent");
                writer.newLine();
            }

            Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+(.*?)\\s+(\\w+)\\s*(\\(.*?\\))");
            Matcher methodMatcher = methodPattern.matcher(content);

            if (methodMatcher.find()) {
                writer.write("Methods:");
                writer.newLine();
                do {
                    String accessModifier = methodMatcher.group(1);
                    String typeAndExtras = methodMatcher.group(2);
                    String methodName = methodMatcher.group(3);
                    String parameters = methodMatcher.group(4);

                    writer.write("\t" + accessModifier + " " + typeAndExtras + " " + methodName + parameters);
                    writer.newLine();
                } while (methodMatcher.find());
            } else {
                writer.write("Methods: absent");
                writer.newLine();
            }

            writer.newLine();
        }
    }
}