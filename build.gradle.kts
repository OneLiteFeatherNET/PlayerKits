plugins {
    id("java")
    `java-library`
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.gradleup.shadow") version "9.0.0-beta4"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.sonarqube") version "4.0.0.2929"
    jacoco
}

val baseVersion = "1.0.0"
group = "net.onelitefeather"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    // Paper
    compileOnly(libs.paper)

    //Cloud command framework
    implementation(libs.cloudPaper)
    implementation(libs.cloudAnnotations)
    implementation(libs.cloudExtras)
    implementation(libs.commodore)

    //Caching
    implementation(libs.caffeine)

    // Database
    implementation(libs.hibernateCore)
    implementation(libs.mariadb)
    implementation(libs.hibernateHikariCP)


    // Testing
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.1")
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