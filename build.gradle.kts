plugins {
    id("java")
    id("pmd")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    reports {
        junitXml.required.set(false)
        html.required.set(true)
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.16.0"
    ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
}