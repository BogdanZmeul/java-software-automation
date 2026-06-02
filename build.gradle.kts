plugins {
    id("java")
    id("report-generator")
}

group = "org.example"
version = "1.0-SNAPSHOT"

tasks.register("analyzeReport") {
    group = "report"
    description = "Reads the generated report and counts the number of Java types using separate regexes."

    dependsOn("generateCodeReport")

    doLast {
        val reportFile = file("build/reports/code-report.txt")

        if (reportFile.exists()) {
            val content = reportFile.readText()

            val classCount = Regex("(?i)class:").findAll(content).count()
            val interfaceCount = Regex("(?i)interface:").findAll(content).count()
            val enumCount = Regex("(?i)enum:").findAll(content).count()
            val recordCount = Regex("(?i)record:").findAll(content).count()

            val totalCount = classCount + interfaceCount + enumCount + recordCount

            println("Report Analysis Complete")
            println("Classes: $classCount")
            println("Interfaces: $interfaceCount")
            println("Enums: $enumCount")
            println("Records: $recordCount")
            println("Total Java types found: $totalCount")
        } else {
            println("Error: The report file does not exist!")
        }
    }
}
