plugins {
    id("java")
    `java-library`
    id("org.liquibase.gradle") version "2.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.4.0.2513"
    jacoco
}

group = "net.onelitefeather"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = uri("https://papermc.io/repo/repository/maven-public/"))
    maven(url = uri("https://maven.enginehub.org/repo/"))
    maven(url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"))
    maven(url = uri("https://oss.sonatype.org/content/groups/public/"))
    maven(url = uri("https://libraries.minecraft.net"))
    maven(url = uri("https://repo.cloudnetservice.eu/repository/releases/"))
    maven(url = uri("https://repo.dmulloy2.net/repository/public/"))
    maven(url = uri("https://jitpack.io"))
}

dependencies {

    // Paper
    compileOnly(libs.paper)

    implementation("org.hibernate:hibernate-core:6.1.5.Final")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.1.5.Final")

    // Commands
    implementation(libs.bundles.cloud)
    implementation(libs.commodore) {
        isTransitive = false
    }

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
        minecraftVersion("1.19.2")
    }

    build {
        dependsOn(shadowJar)
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
    getByName<org.sonarqube.gradle.SonarQubeTask>("sonarqube") {
        dependsOn(rootProject.tasks.test)
    }
}

bukkit {
    if (System.getenv().containsKey("CI")) {
        version = "${rootProject.version}+${System.getenv("CI_COMMIT_SHORT_SHA")}"
    }
    main = "${rootProject.group}.playerkits.PlayerKitsPlugin"
    apiVersion = "1.19"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("UniqueGame", "OneLiteFeather")

}

sonarqube {
    properties {
        property("sonar.projectKey", "onelitefeather_projects_player-kits_AYUcL9fiZDfNdlYcbA_J=")
        property("sonar.qualitygate.wait", true)
    }
}

liquibase {
    activities {
        create("diffMain") {
            (this.arguments as MutableMap<String, String>).apply {
                this["changeLogFile"] = "src/main/resources/db/changelog/db.changelog-diff.xml"
                this["url"] = "jdbc:mariadb://localhost:3307/playerkits"
                this["username"] = "root"
                this["password"] = "%Schueler90"

                this["referenceUrl"] = "jdbc:mariadb://localhost:3307/playerkitsdiff"
                this["referenceUsername"] = "root"
                this["referencePassword"] = "%Schueler90"

            }
        }
    }
}
