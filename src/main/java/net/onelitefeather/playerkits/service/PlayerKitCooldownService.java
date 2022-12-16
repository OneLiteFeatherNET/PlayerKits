package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldown;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @deprecated Use {@link ClaimedKitService} instead.
 */
@Deprecated(forRemoval = true)
public final class PlayerKitCooldownService {

    public static final long NO_COOLDOWN = -1;
    private final PlayerKitsPlugin plugin;
    private final List<PlayerKitCooldown> playerKitCooldowns;

    public PlayerKitCooldownService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.playerKitCooldowns = new ArrayList<>();
        load(this.playerKitCooldowns::addAll);
    }

    /**
     * @return a List of all {@link PlayerKitCooldown}
     */
    public List<PlayerKitCooldown> getPlayerKitCooldowns() {
        return playerKitCooldowns;
    }

    /**
     * Load all {@link PlayerKitCooldown} from the file
     *
     * @param consumer the consumer
     */
    public void load(@NotNull Consumer<List<PlayerKitCooldown>> consumer) {
        List<PlayerKitCooldown> kitCooldowns = new ArrayList<>();

        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
            session.beginTransaction();
            var query = session.createQuery("SELECT kd FROM PlayerKitCooldown kd", PlayerKitCooldown.class);
            kitCooldowns.addAll(query.list());
        } catch (HibernateException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not load kit cooldowns.", e);
        }

        consumer.accept(kitCooldowns);
    }

    /**
     * Add a {@link PlayerKitCooldown}
     *
     * @param playerKitCooldown the cooldown
     */
    public void createKitCooldown(@NotNull PlayerKitCooldown playerKitCooldown) {

        if (!exists(playerKitCooldown)) {
            this.playerKitCooldowns.add(playerKitCooldown);
            try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(playerKitCooldown);
                session.getTransaction().commit();
            } catch (HibernateException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not save kit cooldown", e);
            }
        }
    }

    public boolean exists(PlayerKitCooldown playerKitCooldown) {
        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
            var kitCooldown = session.createQuery("SELECT kdc FROM PlayerKitCooldown kdc WHERE kdc.playerId = :playerId AND kdc.kitName = :name", PlayerKitCooldown.class);
            kitCooldown.setMaxResults(1);
            kitCooldown.setParameter("playerId", playerKitCooldown.getPlayerId().toString());
            kitCooldown.setParameter("name", playerKitCooldown.getKitName());
            return kitCooldown.uniqueResult() != null;
        } catch (HibernateException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Something went wrong!", e);
            return false;
        }
    }

    /**
     * Remove a {@link PlayerKitCooldown}
     *
     * @param playerId the playerId
     * @param name the name
     */
    public void removeCooldown(@NotNull UUID playerId, String name) {

        PlayerKitCooldown playerKitCooldown = getPlayerKitCooldown(playerId, name);
        if (playerKitCooldown == null) return;
        if (!playerKitCooldown.expired()) return;

        if (exists(playerKitCooldown)) {
            try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
                session.beginTransaction();
                session.remove(playerKitCooldown);
                session.getTransaction().commit();
                this.playerKitCooldowns.remove(playerKitCooldown);
            } catch (HibernateException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not remove the KitCooldown from the database.", e);
            }
        }
    }

    /**
     * @param playerId the player who claimed the {@link PlayerKit}
     * @param name the name of the {@link PlayerKit}
     * @return the {@link PlayerKitCooldown} by the given playerId and kitId
     */
    @Nullable
    public PlayerKitCooldown getPlayerKitCooldown(@NotNull UUID playerId, String name) {

        PlayerKitCooldown playerKitCooldown = null;
        List<PlayerKitCooldown> kitCooldowns = this.getPlayerKitCooldowns();

        for (int i = 0; i < kitCooldowns.size() && playerKitCooldown == null; i++) {
            PlayerKitCooldown kitCooldown = kitCooldowns.get(i);
            if (kitCooldown.getPlayerId().equals(playerId) && kitCooldown.getKitName().equals(name)) {
                playerKitCooldown = kitCooldown;
            }
        }

        return playerKitCooldown;
    }

    /**
     * @param name the name of the {@link PlayerKit}
     * @return the {@link PlayerKitCooldown} by the given kitId
     */
    @Nullable
    public PlayerKitCooldown getPlayerKitCooldown(String name) {

        PlayerKitCooldown playerKitCooldown = null;

        List<PlayerKitCooldown> kitCooldowns = this.getPlayerKitCooldowns();
        for (int i = 0; i < kitCooldowns.size() && playerKitCooldown == null; i++) {
            PlayerKitCooldown kitCooldown = kitCooldowns.get(i);
            if (kitCooldown.getKitName().equalsIgnoreCase(name)) {
                playerKitCooldown = kitCooldown;
            }
        }

        return playerKitCooldown;
    }

    public boolean isCooldownExpired(@Nullable PlayerKitCooldown kitCooldown) {
        return kitCooldown == null || kitCooldown.expired();
    }
}
