package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.ClaimedKit;
import net.onelitefeather.playerkits.kit.KitClaimResult;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class ClaimedKitService {

    public static final Long IGNORE_COOLDOWN = -1L;
    private static final String KIT_NAME_PARAMETER = "kitName";
    private static final String CLAIMED_BY_PARAMETER = "claimedBy";
    private final PlayerKitsPlugin plugin;

    public ClaimedKitService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public List<ClaimedKit> getClaimedKits(@NotNull UUID claimedBy) {
        return this.plugin.getDatabaseService().getSessionFactory().map(SessionFactory::openSession).map(session -> {
            var query = session.createQuery("SELECT ck FROM ClaimedKit ck WHERE ck.claimedBy = :claimedBy", ClaimedKit.class);
            query.setParameter(CLAIMED_BY_PARAMETER, claimedBy.toString());
            return query.list();
        }).orElseGet(ArrayList::new);
    }

    public Optional<ClaimedKit> getClaimedKit(@NotNull String kitName, @NotNull UUID claimedBy) {
        return this.plugin.getDatabaseService().getSessionFactory().map(SessionFactory::openSession).map(session -> {
            var query = session.createQuery("SELECT ck FROM ClaimedKit ck WHERE ck.kitName = :kitName AND ck.claimedBy = :claimedBy", ClaimedKit.class);
            query.setParameter(KIT_NAME_PARAMETER, kitName);
            query.setParameter(CLAIMED_BY_PARAMETER, claimedBy.toString());
            return query.uniqueResult();
        });
    }

    @NotNull
    public KitClaimResult canClaim(@NotNull UUID claimedBy, @NotNull String kitName) {

        var claimedKit = getClaimedKit(kitName, claimedBy);
        var playerKit = plugin.getPlayerKitService().getPlayerKit(kitName);
        if (playerKit == null) return KitClaimResult.UNKNOWN_KIT;

        var offlinePlayer = plugin.getServer().getOfflinePlayer(claimedBy);

        if (claimedKit.isEmpty()) return KitClaimResult.SUCCESS;
        if (offlinePlayer.hasPlayedBefore() && playerKit.isFirstJoin() && claimedKit.get().getFirstJoin())
            return KitClaimResult.ALREADY_CLAIMED;

        if (playerKit.isOneTime() && claimedKit.get().getOneTime()) return KitClaimResult.ALREADY_CLAIMED;
        return System.currentTimeMillis() > claimedKit.get().getCooldown() ? KitClaimResult.SUCCESS : KitClaimResult.COOLDOWN_NOT_EXPIRED;
    }

    public boolean claimKit(@NotNull String kitName,
                            @NotNull UUID claimedBy,
                            @NotNull Boolean firstJoin,
                            @NotNull Boolean oneTime,
                            @NotNull Long claimedAt,
                            @NotNull Long cooldown) {

        var claimedKit = getClaimedKit(kitName, claimedBy);
        if (claimedKit.isPresent()) return !firstJoin || !oneTime || cooldown.equals(IGNORE_COOLDOWN);

        Transaction transaction = null;

        var sessionFactory = this.plugin.getDatabaseService().getSessionFactory();
        if (sessionFactory.isEmpty()) return false;

        try (Session session = sessionFactory.get().openSession()) {
            transaction = session.beginTransaction();

            ClaimedKit kit = new ClaimedKit();
            kit.setKitName(kitName);
            kit.setClaimedByUniqueId(claimedBy);
            kit.setClaimedAt(claimedAt);
            kit.setFirstJoin(firstJoin);
            kit.setCooldown(cooldown);
            kit.setOneTime(oneTime);

            session.persist(kit);
            transaction.commit();

            return true;
        } catch (HibernateException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            this.plugin.getLogger().log(Level.SEVERE, "Cannot save claimed kit called %s".formatted(kitName), e);
            return false;
        }
    }
}
