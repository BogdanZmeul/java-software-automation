import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mojo(name = "collect")
public class CodeCollector extends AbstractMojo {

    @Parameter(property = "codeCollector.sourceDirectory", defaultValue = "${project.basedir}/src")
    private String sourceDirectory;

    @Parameter(property = "codeCollector.outputFile", defaultValue = "${project.build.directory}/source-code.zip")
    private String outputFile;

    @Override
    public void execute() throws MojoExecutionException {
        File sourceFolder = new File(sourceDirectory);
        File zipFile = new File(outputFile);

        if (!sourceFolder.exists()) {
            throw new MojoExecutionException("Source folder does not exist: " + sourceFolder.getAbsolutePath());
        }

        try {
            zipFile.getParentFile().mkdirs();

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                addFolderToZip(sourceFolder, sourceFolder, zipOutputStream);
            }

            getLog().info("Created source code archive: " + zipFile.getAbsolutePath());
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to create source code archive", exception);
        }
    }

    private void addFolderToZip(File rootFolder, File currentFolder, ZipOutputStream zipOutputStream) throws IOException {
        File[] files = currentFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToZip(rootFolder, file, zipOutputStream);
            } else {
                addFileToZip(rootFolder, file, zipOutputStream);
            }
        }
    }

    private void addFileToZip(File rootFolder, File file, ZipOutputStream zipOutputStream) throws IOException {
        String rootPath = rootFolder.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        String fileNameInZip = filePath.substring(rootPath.length() + 1).replace("\\", "/");

        zipOutputStream.putNextEntry(new ZipEntry(fileNameInZip));
        Files.copy(file.toPath(), zipOutputStream);
        zipOutputStream.closeEntry();
    }
}