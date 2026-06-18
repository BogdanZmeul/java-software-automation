plugins {
    id("java")
    id("pmd")
    `maven-publish`
    id("info.solidsoft.pitest") version "1.19.0"
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
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
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

pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(setOf("TransferService"))
    targetTests.set(setOf("TransferServiceTest"))
    threads.set(8)
    outputFormats.set(setOf("HTML"))
}