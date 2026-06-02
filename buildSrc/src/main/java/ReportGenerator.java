import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ReportGenerator implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("validateCodeStructure", ValidateCodeStructureTask.class, task -> {
            task.setGroup("report");
            task.setDescription("Validates if Java source files exist in the project.");
        });

        project.getTasks().register("generateCodeReport", GenerateCodeReportTask.class, task -> {
            task.setGroup("report");
            task.setDescription("Generates a report of Java class structures.");
            task.dependsOn("validateCodeStructure");
        });
    }
}