plugins {
    id("java")
    `java-library`
    checkstyle
    alias(libs.plugins.pluginYmlBukkit)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
}

group = "net.onelitefeather"
version = "1.0.0-SNAPSHOT"

val cloudNetVersion = "3.4.4-RELEASE"

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


    // Commands
    implementation(libs.bundles.cloud)
    implementation(libs.commodore) {
        isTransitive = false
    }
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
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
        minecraftVersion("1.18.2")
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}

bukkit {
    main = "${rootProject.group}.playerkits.PlayerKitsPlugin"
    apiVersion = "1.18"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("UniqueGame", "OneLiteFeather")

}