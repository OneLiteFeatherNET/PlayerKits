package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.util.ThreadHelper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Optional;

public class DatabaseService implements ThreadHelper {

    private SessionFactory sessionFactory;

    public DatabaseService(PlayerKitsPlugin plugin) {
        syncThreadForServiceLoader(() -> this.sessionFactory = new Configuration().configure().configure(
                plugin.getDataFolder().toPath().resolve("hibernate.cfg.xml").toFile()).buildSessionFactory());
    }

    public Optional<SessionFactory> getSessionFactory() {
        return Optional.ofNullable(this.sessionFactory);
    }
}
