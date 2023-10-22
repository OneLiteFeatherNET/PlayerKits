rootProject.name = "PlayerKits"
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            //Paper
            library("paper", "io.papermc.paper", "paper-api").version("1.20.1-R0.1-SNAPSHOT")

            //Cloud command framework
            library("cloudPaper", "cloud.commandframework", "cloud-paper").version("1.8.2")
            library("cloudAnnotations", "cloud.commandframework", "cloud-annotations").version("1.8.2")
            library("cloudExtras", "cloud.commandframework", "cloud-minecraft-extras").version("1.8.2")
            library("commodore", "me.lucko", "commodore").version("2.2")

            //Caching
            library("caffeine", "com.github.ben-manes.caffeine", "caffeine").version("3.1.1")

            // Database
            library("hibernateCore", "org.hibernate", "hibernate-core").version("6.1.5.Final")
            library("mariadb", "org.mariadb.jdbc", "mariadb-java-client").version("3.0.6")
            library("hibernateHikariCP", "org.hibernate.orm", "hibernate-hikaricp").version("6.1.5.Final")

            //Testing
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").version("5.9.0")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
        }
    }
}