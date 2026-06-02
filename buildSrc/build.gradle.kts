plugins {
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("reportGenerator") {
            id = "report-generator"
            implementationClass = "ReportGenerator"
        }
    }
}
