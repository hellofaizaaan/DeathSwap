plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.mohammadfaizan"
version = "1.1.6"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    implementation(kotlin("stdlib"))
}

val targetJavaVersion = 25
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    runServer {
        // Minecraft version for the test server spun up by `./gradlew runServer`.
        minecraftVersion("26.2")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()

        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        }
    }
}
