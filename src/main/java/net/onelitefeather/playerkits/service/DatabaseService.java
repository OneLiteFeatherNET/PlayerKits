package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.kit.ClaimedKit;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.tool.schema.Action;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class DatabaseService {

    private final String jdbcUrl;
    private final String driver;
    private final String username;
    private final String password;

    private final String dialect;

    private final SessionFactory sessionFactory;

    public DatabaseService(@NotNull String jdbcUrl,
                           @NotNull String driver,
                           @NotNull String username,
                           @NotNull String password,
                           @NotNull String dialect) {
        this.jdbcUrl = jdbcUrl;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.dialect = dialect;
        this.sessionFactory = buildSessionFactory();
    }

    private SessionFactory buildSessionFactory() {

        var configuration = new Configuration();
        var properties = new Properties();
        properties.put(AvailableSettings.URL, this.jdbcUrl);
        properties.put(AvailableSettings.DRIVER, this.driver);
        properties.put(AvailableSettings.USER, this.username);
        properties.put(AvailableSettings.PASS, this.password);
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, ImplicitNamingStrategyLegacyJpaImpl.class);

        properties.put(AvailableSettings.CONNECTION_PROVIDER, HikariCPConnectionProvider.class);
        properties.put(AvailableSettings.DIALECT, this.dialect);

        properties.put(AvailableSettings.HBM2DDL_AUTO, Action.UPDATE.name().toLowerCase());

        configuration.setProperties(properties);
        configuration.addAnnotatedClass(ClaimedKit.class);
        configuration.addAnnotatedClass(PlayerKit.class);
        configuration.addAnnotatedClass(PlayerKitProperties.class);

        var registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(registry);
    }

    @NotNull
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
