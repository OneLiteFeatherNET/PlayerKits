rootProject.name = "PlayerKits"
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            version("hibernate", "6.6.0.Final")
            version("jaxbRuntime", "4.0.2")
            version("postgresql", "42.7.3")

            //Paper
            library("paper", "io.papermc.paper", "paper-api").version("1.21.1-R0.1-SNAPSHOT")

            //Cloud command framework
            library("cloudPaper", "org.incendo", "cloud-paper").version("2.0.0-beta.10")
            library("cloudAnnotations", "org.incendo", "cloud-annotations").version("2.0.0")
            library("cloudExtras", "org.incendo", "cloud-minecraft-extras").version("2.0.0-beta.10")
            library("commodore", "me.lucko", "commodore").version("2.2")

            //Caching
            library("caffeine", "com.github.ben-manes.caffeine", "caffeine").version("3.1.1")

            // Database
            library("hibernateCore", "org.hibernate", "hibernate-core").versionRef("hibernate")
            library("hibernateHikariCP", "org.hibernate", "hibernate-hikaricp").versionRef("hibernate")
            library("jaxbRuntime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxbRuntime")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")

            //Testing
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").version("5.9.0")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()

            bundle("hibernate", listOf("hibernateCore", "hibernateHikariCP"))
        }
    }
}