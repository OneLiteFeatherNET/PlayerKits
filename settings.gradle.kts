rootProject.name = "PlayerKits"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}


dependencyResolutionManagement {
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public")
        mavenCentral()
    }


    versionCatalogs {
        create("libs") {
            version("publishdata", "1.4.0")
            version("shadow", "9.2.2")
            version("pluginYml", "0.6.0")
            version("runPaper", "3.0.2")


            version("hibernate", "7.2.6.Final")
            version("jaxbRuntime", "4.0.6")
            version("postgresql", "42.7.8")

            //Paper
            library("paper", "io.papermc.paper", "paper-api").version("1.21.1-R0.1-SNAPSHOT")
            library("adventureBukkit", "net.kyori", "adventure-platform-bukkit").version("4.4.1")

            //Cloud command framework
            library("cloudPaper", "org.incendo", "cloud-paper").version("2.0.0-SNAPSHOT")
            library("cloudAnnotations", "org.incendo", "cloud-annotations").version("2.0.0")
            library("cloudExtras", "org.incendo", "cloud-minecraft-extras").version("2.0.0-SNAPSHOT")
            library("commodore", "me.lucko", "commodore").version("2.2")

            //Caching
            library("caffeine", "com.github.ben-manes.caffeine", "caffeine").version("3.2.3")

            // Database
            library("hibernateCore", "org.hibernate", "hibernate-core").versionRef("hibernate")
            library("hibernateHikariCP", "org.hibernate", "hibernate-hikaricp").versionRef("hibernate")
            library("jaxbRuntime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxbRuntime")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")

            //Testing
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").version("6.0.3")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()

            bundle("hibernate", listOf("hibernateCore", "hibernateHikariCP"))

            plugin("shadow", "com.gradleup.shadow").versionRef("shadow")
            plugin("publishdata", "de.chojo.publishdata").versionRef("publishdata")
            plugin("pluginYml", "net.minecrell.plugin-yml.paper").versionRef("pluginYml")
            plugin("runPaper", "xyz.jpenilla.run-paper").versionRef("runPaper")
        }
    }
}