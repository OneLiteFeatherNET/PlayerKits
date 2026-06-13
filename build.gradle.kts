plugins {
    java
    jacoco
    alias(libs.plugins.shadow)
    alias(libs.plugins.publishdata)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.pluginYml)
    `maven-publish`
}

group = "net.onelitefeather"
version = "1.0.0"


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


dependencies {
// Paper
    compileOnly(libs.paper)

    //Adventure (Bukkit)
    implementation(libs.adventureBukkit)

    //Cloud command framework
    implementation(libs.cloudPaper)
    implementation(libs.cloudAnnotations)
    implementation(libs.cloudExtras)
    implementation(libs.commodore)

    //Caching
    implementation(libs.caffeine)

    // Database
    implementation(libs.bundles.hibernate)
    implementation(libs.jaxbRuntime)
    implementation(libs.postgresql)

    // Testing
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
}

tasks {

    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xmx4G", "-Dcom.mojang.eula.agree=true")
    }

    test {
        useJUnitPlatform()
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

}

publishData {
    addBuildData()
    val projectId: String by project
    val gitlabUrl: String by project
    useGitlabReposForProject(projectId, gitlabUrl)
    publishTask("shadowJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        // configure the publication as defined previously.
        publishData.configurePublication(this)
        version = publishData.getVersion(false)
    }

    repositories {
        maven {
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }


            name = "Gitlab"
            // Get the detected repository from the publishing data
            url = uri(publishData.getRepository())
        }
    }
}

paper {
    main = "${rootProject.group}.playerkits.PlayerKitsPlugin"
    apiVersion = "1.20"
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
