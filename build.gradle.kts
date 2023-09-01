plugins {
    id("java")
    `java-library`
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("org.sonarqube") version "4.0.0.2929"
    jacoco
}

val baseVersion = "1.0.0"
group = "net.onelitefeather"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    //Cloud command framework
    implementation("cloud.commandframework", "cloud-paper", "1.8.2")
    implementation("cloud.commandframework", "cloud-annotations", "1.8.2")
    implementation("cloud.commandframework", "cloud-minecraft-extras", "1.8.2")
    implementation("me.lucko:commodore:2.2") {
        isTransitive = false
    }

    //Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")

    // Database
    implementation("org.hibernate:hibernate-core:6.1.5.Final")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.1.5.Final")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.20.1")
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }

    jacocoTestReport {
        dependsOn(rootProject.tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    getByName<org.sonarqube.gradle.SonarTask>("sonar") {
        dependsOn(rootProject.tasks.test)
    }
}

paper {

    if (System.getenv().containsKey("CI")) {
        version = "${rootProject.version}+${System.getenv("CI_COMMIT_SHORT_SHA")}"
    }

    name = rootProject.name
    main = "${rootProject.group}.playerkits.PlayerKitsPlugin"
    apiVersion = "1.20"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    author = "theShadowsDust"
    authors = listOf("OneLiteFeather")

    //Paper
    hasOpenClassloader = false
    generateLibrariesJson = false
    foliaSupported = true

    serverDependencies {
        register("Vault") {
            required = false
        }
    }
}

version = if (System.getenv().containsKey("CI")) {
    val releaseOrSnapshot = if (System.getenv("CI_COMMIT_BRANCH").equals("main", true)) {
        ""
    } else if(System.getenv("CI_COMMIT_BRANCH").equals("test", true)) {
        "-PREVIEW"
    } else {
        "-SNAPSHOT"
    }
    "$baseVersion$releaseOrSnapshot+${System.getenv("CI_COMMIT_SHORT_SHA")}"
} else {
    "$baseVersion-SNAPSHOT"
}


sonarqube {
    properties {
        property("sonar.projectKey", "onelitefeather_projects_player-kits_AYUcL9fiZDfNdlYcbA_J")
        property("sonar.qualitygate.wait", true)
    }
}