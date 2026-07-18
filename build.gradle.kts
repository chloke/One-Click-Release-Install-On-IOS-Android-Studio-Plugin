plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "io.github.chloke.oneclickios"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1.4")
    }

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
        }
    }

    pluginVerification {
        ides {
            providers.gradleProperty("localIdePath").orNull?.let(::local)
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.compilerArgs.add("-Xlint:deprecation")
    }

    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "9.1.0"
        distributionType = Wrapper.DistributionType.BIN
    }
}
