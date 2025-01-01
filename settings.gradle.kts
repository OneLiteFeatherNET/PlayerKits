rootProject.name = "PlayerKits"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}


dependencyResolutionManagement {
    if (System.getenv("CI") != null) {
        repositoriesMode = RepositoriesMode.PREFER_SETTINGS
        repositories {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            maven("https://repo.htl-md.schule/repository/Gitlab-Runner/")
            maven {
                val groupdId = 28 // Gitlab Group
                val ciApiv4Url = System.getenv("CI_API_V4_URL")
                url = uri("$ciApiv4Url/groups/$groupdId/-/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class.java) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    } else {
        repositories {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            maven("https://repo.papermc.io/repository/maven-public")
            mavenCentral()
            maven {
                val groupdId = 28 // Gitlab Group
                url = uri("https://gitlab.onelitefeather.dev/api/v4/groups/$groupdId/-/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class.java) {
                    name =  "Private-Token"
                    value = providers.gradleProperty("gitLabPrivateToken").get()
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }


    versionCatalogs {
        create("libs") {
            version("publishdata", "1.4.0")
            version("shadow", "8.3.0")
            version("pluginYml", "0.6.0")
            version("runPaper", "2.3.1")


            version("hibernate", "6.6.0.Final")
            version("jaxbRuntime", "4.0.2")
            version("postgresql", "42.7.5")

            //Paper
            library("paper", "io.papermc.paper", "paper-api").version("1.21.1-R0.1-SNAPSHOT")
            library("adventureBukkit", "net.kyori", "adventure-platform-bukkit").version("4.3.4")

            //Cloud command framework
            library("cloudPaper", "org.incendo", "cloud-paper").version("2.0.0-beta.10")
            library("cloudAnnotations", "org.incendo", "cloud-annotations").version("2.0.0")
            library("cloudExtras", "org.incendo", "cloud-minecraft-extras").version("2.0.0-beta.10")
            library("commodore", "me.lucko", "commodore").version("2.2")

            //Caching
            library("caffeine", "com.github.ben-manes.caffeine", "caffeine").version("3.2.0")

            // Database
            library("hibernateCore", "org.hibernate", "hibernate-core").versionRef("hibernate")
            library("hibernateHikariCP", "org.hibernate", "hibernate-hikaricp").versionRef("hibernate")
            library("jaxbRuntime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxbRuntime")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")

            //Testing
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").version("5.11.4")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()

            bundle("hibernate", listOf("hibernateCore", "hibernateHikariCP"))

            plugin("shadow", "com.gradleup.shadow").versionRef("shadow")
            plugin("publishdata", "de.chojo.publishdata").versionRef("publishdata")
            plugin("pluginYml", "net.minecrell.plugin-yml.paper").versionRef("pluginYml")
            plugin("runPaper", "xyz.jpenilla.run-paper").versionRef("runPaper")
        }
    }
}