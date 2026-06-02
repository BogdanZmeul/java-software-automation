import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateCodeReportTask extends DefaultTask {

    @TaskAction
    public void generateReport() throws IOException {
        File sourceFolder = new File(getProject().getProjectDir(), "src/main/java");
        File buildDir = getProject().getLayout().getBuildDirectory().get().getAsFile();
        File reportFile = new File(buildDir, "reports/code-report.txt");

        if (!sourceFolder.exists()) {
            System.out.println("Source folder does not exist: " + sourceFolder.getAbsolutePath());
            return;
        }

        reportFile.getParentFile().mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(reportFile.toPath(), StandardCharsets.UTF_8)) {
            int analyzedFiles = scanFolder(sourceFolder, writer);

            if (analyzedFiles == 0) {
                writer.write("No Java files found in: " + sourceFolder.getAbsolutePath());
                writer.newLine();
            }
        }

        System.out.println("Successfully generated code report at: " + reportFile.getAbsolutePath());
    }

    private int scanFolder(File folder, BufferedWriter writer) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return 0;

        int analyzedFiles = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                analyzedFiles += scanFolder(file, writer);
            } else if (file.getName().endsWith(".java")) {
                analyzeJavaFile(file, writer);
                analyzedFiles++;
            }
        }
        return analyzedFiles;
    }

    private void analyzeJavaFile(File javaFile, BufferedWriter writer) throws IOException {
        String content = Files.readString(javaFile.toPath());

        Pattern typePattern = Pattern.compile("(class|interface|enum|record)\\s+(\\w+)");
        Matcher typeMatcher = typePattern.matcher(content);

        if (!typeMatcher.find()) return;

        String typeKeyword = typeMatcher.group(1);
        String typeName = typeMatcher.group(2);
        String capitalizedKeyword = typeKeyword.substring(0, 1).toUpperCase() + typeKeyword.substring(1);

        writer.write(capitalizedKeyword + ": " + typeName);
        writer.newLine();

        writeFields(content, writer);
        writeConstructors(content, typeName, writer);
        writeMethods(content, writer);
        writer.newLine();
    }

    private void writeFields(String content, BufferedWriter writer) throws IOException {
        Pattern fieldPattern = Pattern.compile("(public|private|protected)\\s+(.*?)\\s+(\\w+)\\s*(=|;)");
        Matcher fieldMatcher = fieldPattern.matcher(content);

        if (!fieldMatcher.find()) {
            writer.write("Fields: absent");
            writer.newLine();
            return;
        }

        writer.write("Fields:");
        writer.newLine();
        do {
            writer.write("\t" + fieldMatcher.group(1) + " " + fieldMatcher.group(2) + " " + fieldMatcher.group(3));
            writer.newLine();
        } while (fieldMatcher.find());
    }

    private void writeConstructors(String content, String typeName, BufferedWriter writer) throws IOException {
        Pattern constructorPattern = Pattern.compile("(public|private|protected)\\s+(" + typeName + ")\\s*(\\(.*?\\))");
        Matcher constructorMatcher = constructorPattern.matcher(content);

        if (!constructorMatcher.find()) {
            writer.write("Constructors: absent");
            writer.newLine();
            return;
        }

        writer.write("Constructors:");
        writer.newLine();
        do {
            writer.write("\t" + constructorMatcher.group(1) + " " + constructorMatcher.group(2) + constructorMatcher.group(3));
            writer.newLine();
        } while (constructorMatcher.find());
    }

    private void writeMethods(String content, BufferedWriter writer) throws IOException {
        Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+(.*?)\\s+(\\w+)\\s*(\\(.*?\\))");
        Matcher methodMatcher = methodPattern.matcher(content);

        if (!methodMatcher.find()) {
            writer.write("Methods: absent");
            writer.newLine();
            return;
        }

        writer.write("Methods:");
        writer.newLine();
        do {
            writer.write("\t" + methodMatcher.group(1) + " " + methodMatcher.group(2) + " " + methodMatcher.group(3) + methodMatcher.group(4));
            writer.newLine();
        } while (methodMatcher.find());
    }
}