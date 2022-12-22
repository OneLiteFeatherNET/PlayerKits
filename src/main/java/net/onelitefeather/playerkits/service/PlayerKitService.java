package net.onelitefeather.playerkits.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperty;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.util.InventoryUtil;
import net.onelitefeather.playerkits.util.TimeUtil;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PlayerKitService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
    private static final int[] BORDERS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44, 9, 18, 27, 17, 26, 35};
    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private static final String FILE_NAME = "playerKits.json";

    private final PlayerKitsPlugin plugin;
    private final List<PlayerKit> playerKitList;
    private final File playerKitsFile;
    private final Inventory kitPreviewInventory;
    private final Inventory kitInventory;

    public PlayerKitService(@NotNull PlayerKitsPlugin plugin) {

        this.plugin = plugin;
        this.playerKitList = new ArrayList<>();
        this.playerKitsFile = new File(plugin.getDataFolder(), FILE_NAME);

        if (!this.playerKitsFile.exists()) {
            try {
                Files.createFile(this.playerKitsFile.toPath());
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not create file", e);
            }
        }

        this.kitInventory = this.plugin.getServer().createInventory(null, 45,
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        this.plugin.getConfig().getString("gui.title", "Kit Overview")));

        this.kitPreviewInventory = plugin.getServer().createInventory(null, 45,
                LegacyComponentSerializer.legacySection().deserialize(
                        this.plugin.getConfig().getString("gui.preview-title", "Kit Preview")));

        for (int border : BORDERS) {
            this.kitInventory.setItem(border, BORDER_ITEM);
        }

        load();
    }

    private void load() {
        loadPlayerKits(playerKits -> {
            for (PlayerKit playerKit : playerKits) {
                if (!playerKit.getPropertyValue(PlayerKitProperty.VISIBLE, Boolean.class)) continue;
                playerKit.setContent(InventoryUtil.deserializeInventoryFromString(playerKit.getItems()));
                this.kitInventory.addItem(playerKit.setGuiItem(this.plugin.i18n().getKitItemDescription()));
            }

            this.playerKitList.addAll(playerKits);
        });
    }

    @NotNull
    public Inventory getKitInventory() {
        return kitInventory;
    }

    @NotNull
    public List<PlayerKit> getPlayerKitList() {
        return playerKitList;
    }

    @NotNull
    public Inventory getKitPreviewInventory() {
        return kitPreviewInventory;
    }

    public void previewKit(@NotNull Player player, @NotNull PlayerKit playerKit) {

        this.kitPreviewInventory.setContents(playerKit.getContent());
        this.kitPreviewInventory.setItem(this.kitPreviewInventory.getSize() - 1
                , this.plugin.getItemRegistry().getItem(ItemRegistry.OPEN_LAST_INVENTORY));

        player.openInventory(this.getKitPreviewInventory());
    }

    public void loadPlayerKits(@NotNull Consumer<List<PlayerKit>> consumer) {

        if (!this.playerKitsFile.exists()) return;
        List<PlayerKit> playerKits = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(this.playerKitsFile.toPath())) {

            var data = GSON.fromJson(bufferedReader, PlayerKit[].class);
            if (data != null && data.length > 0) {
                playerKits.addAll(List.of(data));
            }

        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not load playerkits", e);
        }

        consumer.accept(playerKits);
    }

    public boolean deleteKit(@NotNull PlayerKit playerKit) {

        try (FileWriter fileWriter = new FileWriter(this.playerKitsFile)) {
            this.kitInventory.remove(playerKit.getGuiItem());
            this.playerKitList.remove(playerKit);
            fileWriter.write(GSON.toJson(this.playerKitList));
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not delete the playerkit.", e);
            return false;
        }

        return true;
    }

    public boolean existsPlayerKit(@NotNull String name) {
        return getPlayerKit(name) != null;
    }

    public boolean createPlayerKit(@NotNull PlayerKit playerKit) {
        if (existsPlayerKit(playerKit.getName())) return false;

        if (playerKit.getPropertyValue(PlayerKitProperty.VISIBLE, Boolean.class)) {
            this.kitInventory.addItem(playerKit.setGuiItem(this.plugin.i18n().getKitItemDescription()));
        }

        this.playerKitList.add(playerKit);
        updatePlayerKits();

        return true;
    }

    @SuppressWarnings("java:S1874")
    public void handleGrantKit(@NotNull CommandSender commandSender, @NotNull Player target,
                               @NotNull PlayerKit playerKit, boolean ignoreCooldown) {

        var claimedKit = this.plugin.getClaimedKitService().getClaimedKit(playerKit.getName());
        var claimResult = this.plugin.getClaimedKitService().canClaim(target.getUniqueId(), playerKit.getName());

        var targetDisplayName = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(target.displayName())));

        switch (claimResult) {
            case UNKNOWN_KIT -> sendMessage(commandSender, target, "kit.not-found", plugin.i18n().getPrefix(), playerKit.getName());
            case ALREADY_CLAIMED -> sendMessage(commandSender, target, "kit.grant.already-claimed", this.plugin.i18n().getPrefix());
            case SUCCESS -> {

                if (!InventoryUtil.hasInventorySpace(target.getInventory(), playerKit)) {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.plugin.i18n()
                            .getMessage("inventory.not-enough-space",
                                    this.plugin.i18n().getPrefix(),
                                    targetDisplayName)));
                    return;
                }

                if (claimKit(target, playerKit)) {

                    for (ItemStack itemStack : InventoryUtil.getContents(playerKit.getContent())) {
                        target.getInventory().addItem(itemStack);
                    }

                    if(commandSender instanceof Player player && !commandSender.equals(target)) {
                        sendMessage(commandSender, player, "kit.grant.other.success", this.plugin.i18n().getPrefix(), playerKit.getName(), targetDisplayName);
                        return;
                    }

                    sendMessage(target, target, "kit.grant.success", this.plugin.i18n().getPrefix(), playerKit.getName());
                }
            }

            case COOLDOWN_NOT_EXPIRED -> {
                if (claimedKit != null) {
                    sendMessage(commandSender, target, "cooldown-expires-at", this.plugin.i18n().getPrefix(),
                            playerKit.getName(),
                            this.plugin.i18n().formatMillis(claimedKit.getCooldown()),
                            targetDisplayName);
                }
            }

            default -> throw new NotImplementedException("Not implemented yet!");
        }
    }

    private void sendMessage(@NotNull CommandSender commandSender, @NotNull Player target, @NotNull String key, Object... args) {
        var message = MiniMessage.miniMessage().deserialize(this.plugin.i18n().getMessage(key, args));
        if (!commandSender.equals(target)) commandSender.sendMessage(message);
        target.sendMessage(message);
    }

    @Nullable
    public PlayerKit getFirstJoinKit() {

        PlayerKit playerKit = null;
        List<PlayerKit> kitList = this.playerKitList;

        for (int i = 0; i < kitList.size() && playerKit == null; i++) {
            var kit = kitList.get(i);
            if (kit.isFirstJoin()) {
                playerKit = kit;
            }
        }

        return playerKit;
    }

    @Nullable
    public PlayerKit getPlayerKit(@NotNull String name) {

        PlayerKit playerKit = null;
        List<PlayerKit> kitList = this.playerKitList;

        for (int i = 0; i < kitList.size() && playerKit == null; i++) {
            var kit = kitList.get(i);
            if (kit.getName().equalsIgnoreCase(name)) {
                playerKit = kit;
            }
        }

        return playerKit;
    }

    @Nullable
    public PlayerKit getPlayerKit(@NotNull ItemStack itemStack) {

        PlayerKit playerKit = null;
        List<PlayerKit> kitList = this.playerKitList;

        for (int i = 0; i < kitList.size() && playerKit == null; i++) {
            var kit = kitList.get(i);
            if (kit.getGuiItem().isSimilar(itemStack)) {
                playerKit = kit;
            }
        }

        return playerKit;
    }

    @NotNull
    public List<String> getPlayerKitNames() {

        List<String> kitNames = new ArrayList<>();
        for (PlayerKit playerKit : this.playerKitList) {
            kitNames.add(playerKit.getName());
        }

        return kitNames;
    }

    private void updatePlayerKits() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.playerKitsFile.toPath())) {
            bufferedWriter.write(GSON.toJson(this.playerKitList));
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not update the playerkits.", e);
        }
    }

    private boolean claimKit(@NotNull Player player, @NotNull PlayerKit playerKit) {
        return this.plugin.getClaimedKitService().claimKit(
                playerKit.getName(),
                player.getUniqueId(),
                playerKit.isFirstJoin(),
                playerKit.isOneTime(),
                System.currentTimeMillis(),
                TimeUtil.getCooldownTime(playerKit));
    }
}
