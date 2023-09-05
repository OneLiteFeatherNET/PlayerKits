package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.ClaimedKit;
import net.onelitefeather.playerkits.kit.KitClaimResult;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

        List<ClaimedKit> claimedKits = new ArrayList<>();
        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
            var query = session.createQuery("SELECT ck FROM ClaimedKit ck WHERE ck.claimedBy = :claimedBy", ClaimedKit.class);
            query.setParameter(CLAIMED_BY_PARAMETER, claimedBy.toString());
            claimedKits.addAll(query.list());
        } catch (HibernateException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not found claimed kits for player %s".formatted(claimedBy.toString()), e);
        }

        return claimedKits;
    }

    @Nullable
    public ClaimedKit getClaimedKit(@NotNull String kitName, @NotNull UUID claimedBy) {

        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
            var query = session.createQuery("SELECT ck FROM ClaimedKit ck WHERE ck.kitName = :kitName AND ck.claimedBy = :claimedBy", ClaimedKit.class);
            query.setParameter(KIT_NAME_PARAMETER, kitName);
            query.setParameter(CLAIMED_BY_PARAMETER, claimedBy.toString());
            return query.uniqueResult();
        } catch (HibernateException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not found a claimed kit called %s".formatted(kitName), e);
        }

        return null;
    }

    @Nullable
    public ClaimedKit getClaimedKit(@NotNull String kitName) {

        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
            var query = session.createQuery("SELECT ck FROM ClaimedKit ck WHERE ck.kitName = :kitName", ClaimedKit.class);
            query.setParameter(KIT_NAME_PARAMETER, kitName);
            return query.uniqueResult();
        } catch (HibernateException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not found a claimed kit called %s".formatted(kitName), e);
        }

        return null;
    }

    @NotNull
    public KitClaimResult canClaim(@NotNull UUID claimedBy, @NotNull String kitName) {

        var claimedKit = getClaimedKit(kitName, claimedBy);
        var playerKit = plugin.getPlayerKitService().getPlayerKit(kitName);
        if (playerKit == null) return KitClaimResult.UNKNOWN_KIT;

        var offlinePlayer = plugin.getServer().getOfflinePlayer(claimedBy);

        if (claimedKit == null) return KitClaimResult.SUCCESS;
        if (offlinePlayer.hasPlayedBefore() && playerKit.isFirstJoin() && claimedKit.getFirstJoin())
            return KitClaimResult.ALREADY_CLAIMED;

        if (playerKit.isOneTime() && claimedKit.getOneTime()) return KitClaimResult.ALREADY_CLAIMED;
        return System.currentTimeMillis() > claimedKit.getCooldown() ? KitClaimResult.SUCCESS : KitClaimResult.COOLDOWN_NOT_EXPIRED;
    }

    public boolean claimKit(@NotNull String kitName,
                            @NotNull UUID claimedBy,
                            @NotNull Boolean firstJoin,
                            @NotNull Boolean oneTime,
                            @NotNull Long claimedAt,
                            @NotNull Long cooldown) {

        ClaimedKit claimedKit = getClaimedKit(kitName, claimedBy);
        if (claimedKit != null) return !firstJoin || !oneTime || cooldown.equals(IGNORE_COOLDOWN);

        Transaction transaction = null;
        try (Session session = this.plugin.getDatabaseService().getSessionFactory().openSession()) {
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
