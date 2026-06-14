plugins {
    java
    jacoco
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.pluginYml)
    `maven-publish`
}

group = "net.onelitefeather"
version = (version as String).substringBefore('#').trim()

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
publishing {
    publications.create<MavenPublication>("maven") {
        artifact(project.tasks.getByName("shadowJar"))
        version = rootProject.version as String
        artifactId = "PlayerKits"
        groupId = rootProject.group as String
        pom {
            name = "PlayerKits"
            description = "A simple Kits plugin to give players certain items."
            url = "https://github.com/OneLiteFeatherNET/playerkits"

            developers {
                developer {
                    id = "theShadowsDust"
                    name = "theShadowsDust"
                    email = "theShadowDust@onelitefeather.net"
                }
                developer {
                    id = "themeinerlp"
                    name = "Phillipp Glanz"
                    email = "p.glanz@madfix.me"
                }
            }
            scm {
                connection = "scm:git:git://github.com:OneLiteFeatherNET/PlayerKits.git"
                developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/PlayerKits.git"
                url = "https://github.com/OneLiteFeatherNET/PlayerKits"
            }
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }

            name = "OneLiteFeatherRepository"
            val releasesRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            val snapshotsRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            url = if (version.toString().contains("SNAPSHOT") || version.toString().contains("BETA") || version.toString().contains("ALPHA")) snapshotsRepoUrl else releasesRepoUrl
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
