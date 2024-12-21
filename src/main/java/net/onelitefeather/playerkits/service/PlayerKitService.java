package net.onelitefeather.playerkits.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.util.InventoryUtil;
import net.onelitefeather.playerkits.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class PlayerKitService {

    private static final int[] BORDERS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44, 9, 18, 27, 17, 26, 35};
    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

    private final LoadingCache<String, PlayerKit> kitCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(this::getPlayerKit);

    private final PlayerKitsPlugin plugin;
    private Inventory kitPreviewInventory;
    private Inventory kitInventory;
    private final List<Component> kitItemDescription;

    public PlayerKitService(@NotNull PlayerKitsPlugin plugin) {

        this.plugin = plugin;
        buildInventories();
        loadKits();
        this.kitItemDescription = new ArrayList<>();
        for (String description : plugin.getConfig().getStringList("gui.item-description")) {
            this.kitItemDescription.add(MiniMessage.miniMessage().deserialize(description));
        }
    }

    public boolean deleteKit(@NotNull PlayerKit playerKit) {

        if (!existsPlayerKit(playerKit.getName())) return false;
        Transaction transaction = null;

        var sessionFactory = this.plugin.getDatabaseService().getSessionFactory();
        if (sessionFactory.isEmpty()) return false;

        try (Session session = sessionFactory.get().openSession()) {
            transaction = session.beginTransaction();
            session.remove(playerKit.getProperties());
            session.remove(playerKit);
            transaction.commit();
            removeKitFromInventory(playerKit);
            return true;
        } catch (HibernateException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            this.plugin.getLogger().log(Level.SEVERE, "Cannot delete kit %s".formatted(playerKit.getName()), e);
            return false;
        }
    }

    public boolean existsPlayerKit(@NotNull String name) {
        return getPlayerKit(name) != null;
    }

    public void createKit(@NotNull PlayerKit playerKit) {

        if (existsPlayerKit(playerKit.getName())) return;

        Transaction transaction = null;
        var sessionFactory = this.plugin.getDatabaseService().getSessionFactory();
        if (sessionFactory.isEmpty()) return;

        try (Session session = sessionFactory.get().openSession()) {
            transaction = session.beginTransaction();

            session.persist(playerKit.getProperties());
            session.persist(playerKit);

            transaction.commit();
            addKitToInventory(playerKit);

        } catch (HibernateException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            this.plugin.getLogger().log(Level.SEVERE, "Cannot create kit %s".formatted(playerKit.getName()), e);
        }
    }

    @Nullable
    public PlayerKit getFirstJoinKit() {
        return getPlayerKit(this.plugin.getConfig().getString("first-join-kit", "firstjoin"));
    }

    @NotNull
    public List<PlayerKit> getKits() {
        return this.plugin.getDatabaseService().getSessionFactory().map(SessionFactory::openSession).map(session -> {
            var query = session.createQuery("SELECT pt FROM PlayerKit pt JOIN FETCH pt.properties p", PlayerKit.class);
            return query.list();
        }).orElse(Collections.emptyList());
    }

    @Nullable
    public PlayerKit getPlayerKit(@NotNull String name) {
        return this.plugin.getDatabaseService().getSessionFactory().map(SessionFactory::openSession).map(session -> {
            var query = session.createQuery("SELECT pt FROM PlayerKit pt JOIN FETCH pt.properties p WHERE pt.name = :name", PlayerKit.class);
            query.setParameter("name", name);

            var result = query.uniqueResult();
            if (result != null) {
                this.kitCache.put(name, result);
            }

            return result;
        }).orElse(null);
    }

    @Nullable
    public PlayerKit getPlayerKit(@NotNull Material material) {

        return this.plugin.getDatabaseService().getSessionFactory().map(SessionFactory::openSession).map(session -> {
            var query = session.createQuery("SELECT pt FROM PlayerKit pt JOIN FETCH pt.properties p WHERE p.displayItem = :displayItem", PlayerKit.class);
            query.setParameter("displayItem", material);

            var result = query.uniqueResult();
            if (result != null) {
                this.kitCache.put(result.getName(), result);
            }

            return result;
        }).orElse(null);
    }


    public void handleGrantKit(@NotNull CommandSender commandSender, @NotNull Player target, @NotNull PlayerKit playerKit) {

        var claimedKit = this.plugin.getClaimedKitService().getClaimedKit(playerKit.getName(), target.getUniqueId());
        var claimResult = this.plugin.getClaimedKitService().canClaim(target.getUniqueId(), playerKit.getName());

        switch (claimResult) {

            case UNKNOWN_KIT ->
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<lang:kit.not-found:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), playerKit.getName())));

            case ALREADY_CLAIMED -> {

                if (!commandSender.equals(target)) {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<lang:kit.grant.player-has-already-claimed:'%s':'%s'>".formatted(
                                    this.plugin.getPluginPrefix(), target.displayName())));
                }

                target.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<lang:kit.grant.already-claimed:'%s'>".formatted(this.plugin.getPluginPrefix())));
            }
            case SUCCESS -> kitGrantSuccess(commandSender, target, playerKit);

            case COOLDOWN_NOT_EXPIRED -> {
                if(claimedKit.isEmpty()) return;
                if (!commandSender.equals(target)) {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<lang:cooldown-expires-at.other:'%s':'%s':'%s':'%s'>".formatted(
                            this.plugin.getPluginPrefix(),
                            playerKit.getName(),
                            TimeUtil.getRemainingTime(claimedKit.get().getCooldown()),
                            target.displayName())));
                }

                target.sendMessage(MiniMessage.miniMessage().deserialize("<lang:cooldown-expires-at.self:'%s':'%s':'%s'>".formatted(
                        this.plugin.getPluginPrefix(),
                        playerKit.getName(),
                        TimeUtil.getRemainingTime(claimedKit.get().getCooldown()))));
            }

            default ->
                    throw new IllegalStateException("The claim result %s is not allowed here!".formatted(claimResult.toString()));
        }
    }

    public void previewKit(@NotNull Player player, @NotNull PlayerKit playerKit) {

        this.kitPreviewInventory.setContents(InventoryUtil.deserializeInventoryFromString(playerKit.getItems()));
        this.kitPreviewInventory.setItem(this.kitPreviewInventory.getSize() - 1
                , this.plugin.getItemRegistry().getItem(ItemRegistry.OPEN_LAST_INVENTORY));

        player.openInventory(this.getKitPreviewInventory());
    }

    public void updatePlayerKit(@NotNull PlayerKit playerKit) {
        Transaction transaction = null;
        var sessionFactory = this.plugin.getDatabaseService().getSessionFactory();
        if (sessionFactory.isEmpty()) return;

        try (Session session = sessionFactory.get().openSession()) {
            transaction = session.beginTransaction();
            session.merge(playerKit.getProperties());
            session.merge(playerKit);
            transaction.commit();
        } catch (HibernateException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            this.plugin.getLogger().log(Level.SEVERE, "Cannot update kit %s".formatted(playerKit.getName()), e);
        }
    }

    @NotNull
    public Inventory getKitInventory() {
        return kitInventory;
    }

    @NotNull
    public Inventory getKitPreviewInventory() {
        return kitPreviewInventory;
    }

    private void removeKitFromInventory(@NotNull PlayerKit playerKit) {
        this.kitInventory.remove(playerKit.getProperties().getDisplayItem());
    }

    private void addKitToInventory(@NotNull PlayerKit playerKit) {

        if (!playerKit.isVisible()) return;
        var slot = this.kitInventory.firstEmpty();
        if (slot == -1L) return;

        this.kitInventory.setItem(slot,
                InventoryUtil.createItem(
                        playerKit.getProperties().getDisplayItem(),
                        MiniMessage.miniMessage().deserialize(playerKit.getDisplayName()), this.kitItemDescription));
    }

    private void loadKits() {
        var kits = getKits();
        for (PlayerKit kit : kits) {
            addKitToInventory(kit);
        }
    }

    public void kitGrantSuccess(@NotNull CommandSender commandSender,
                                 @NotNull Player target,
                                 @NotNull PlayerKit playerKit) {

        if (!InventoryUtil.hasInventorySpace(target.getInventory(), playerKit)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<lang:inventory.not-enough-space:'%s':'%s'>".formatted(
                            this.plugin.getPluginPrefix(), target.displayName())));
            return;
        }

        if (claimKit(target, playerKit)) {

            for (ItemStack itemStack : InventoryUtil.getContents(playerKit.getItems())) {
                target.getInventory().addItem(itemStack);
            }

            if (commandSender instanceof Player player && !commandSender.equals(target)) {

                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<lang:kit.grant.other.success:'%s':'%s':'%s'".formatted(
                                this.plugin.getPluginPrefix(),
                                playerKit.getDisplayName(),
                                player.displayName())));

                return;
            }

            target.sendMessage(MiniMessage.miniMessage().deserialize("<lang:kit.grant.success:'%s':'%s'>".
                    formatted(this.plugin.getPluginPrefix(), playerKit.getDisplayName())));
        }
    }

    private void buildInventories() {
        this.kitInventory = this.plugin.getServer().createInventory(null, 45,
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        this.plugin.getConfig().getString("gui.title", "Kit Overview")));

        this.kitPreviewInventory = plugin.getServer().createInventory(null, 45,
                LegacyComponentSerializer.legacySection().deserialize(
                        this.plugin.getConfig().getString("gui.preview-title", "Kit Preview")));

        for (int border : BORDERS) {
            this.kitInventory.setItem(border, BORDER_ITEM);
        }
    }

    private boolean claimKit(@NotNull Player player, @NotNull PlayerKit playerKit) {
        return this.plugin.getClaimedKitService().claimKit(
                playerKit.getName(),
                player.getUniqueId(),
                playerKit.isFirstJoin(),
                playerKit.isOneTime(),
                System.currentTimeMillis(),
                playerKit.getCooldownTime());
    }
}
