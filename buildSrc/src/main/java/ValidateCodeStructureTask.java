import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class ValidateCodeStructureTask extends DefaultTask {
    @TaskAction
    public void validateStructure() {
        File sourceFolder = getProject().file("src/main/java");

        if (!sourceFolder.exists()) {
            throw new IllegalStateException("Source folder does not exist: " + sourceFolder.getAbsolutePath());
        }

        if (!containsJavaFiles(sourceFolder)) {
            System.out.println("Source folder does not contain Java files yet!");
            return;
        }

        System.out.println("Java source structure is valid!");
    }

    private boolean containsJavaFiles(File folder) {
        File[] files = folder.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (containsJavaFiles(file)) {
                    return true;
                }
            }

            if (file.getName().endsWith(".java")) {
                return true;
            }
        }

        return false;
    }
}
