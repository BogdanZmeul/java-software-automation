plugins {
    id("java")
    id("pmd")
    `maven-publish`
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

    exclude("suiteTests/**")

    reports {
        junitXml.required.set(false)
        html.required.set(true)
    }
}

tasks.register<Test>("runFastSuite") {
    description = "Runs only the fast validation test suite."
    group = "verification"

    useJUnitPlatform()

    val testSourceSet = sourceSets.test.get()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath

    include("suiteTests/FastValidationSuite.class")

    reports {
        junitXml.required.set(false)
        html.required.set(true)
    }
}

tasks.register<Test>("runSlowSuite") {
    description = "Runs only the slow validation test suite."
    group = "verification"

    useJUnitPlatform()

    val testSourceSet = sourceSets.test.get()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath

    include("suiteTests/SlowValidationSuite.class")

    reports {
        junitXml.required.set(false)
        html.required.set(true)
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.16.0"
    ruleSets = listOf("category/java/errorprone.xml")
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY")}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}