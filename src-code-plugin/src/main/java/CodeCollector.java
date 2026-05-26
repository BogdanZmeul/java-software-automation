import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@Mojo(name = "collect")
public class CodeCollector extends AbstractMojo {

    @Parameter(property = "codeCollector.sourceDirectory", defaultValue = "${project.basedir}/src")
    private String sourceDirectory;

    @Parameter(property = "codeCollector.outputFile", defaultValue = "${project.build.directory}/source-code.jar")
    private String outputFile;

    @Override
    public void execute() throws MojoExecutionException {
        File sourceFolder = new File(sourceDirectory);
        File jarFile = new File(outputFile);

        if (!sourceFolder.exists()) {
            throw new MojoExecutionException("Source folder does not exist: " + sourceFolder.getAbsolutePath());
        }

        try {
            jarFile.getParentFile().mkdirs();

            try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile))) {
                addFolderToJar(sourceFolder, sourceFolder, jarOutputStream);
            }

            getLog().info("Created source code archive: " + jarFile.getAbsolutePath());
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to create source code archive", exception);
        }
    }

    private void addFolderToJar(File rootFolder, File currentFolder, JarOutputStream jarOutputStream) throws IOException {
        File[] files = currentFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToJar(rootFolder, file, jarOutputStream);
            } else {
                addFileToJar(rootFolder, file, jarOutputStream);
            }
        }
    }

    private void addFileToJar(File rootFolder, File file, JarOutputStream jarOutputStream) throws IOException {
        String rootPath = rootFolder.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        String fileNameInJar = filePath.substring(rootPath.length() + 1).replace("\\", "/");

        jarOutputStream.putNextEntry(new JarEntry(fileNameInJar));
        Files.copy(file.toPath(), jarOutputStream);
        jarOutputStream.closeEntry();
    }
}