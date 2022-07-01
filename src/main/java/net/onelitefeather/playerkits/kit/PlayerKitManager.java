package net.onelitefeather.playerkits.kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldown;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldownManager;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.util.InventoryUtil;
import net.onelitefeather.playerkits.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PlayerKitManager {

    public static final String DRACONIA_KIT_NAME = "draconia_kit";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
    private static final int[] BORDERS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44, 9, 18, 27, 17, 26, 35};
    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private static final String FILE_NAME = "playerKits.json";

    private final PlayerKitsPlugin plugin;
    private final List<PlayerKit> playerKitList;
    private final File playerKitsFile;
    private final Map<PlayerKit, ItemStack> displayItems;
    private final Inventory kitPreviewInventory, kitInventory;

    public PlayerKitManager(@NotNull PlayerKitsPlugin plugin) {

        this.plugin = plugin;
        this.playerKitList = new ArrayList<>();
        this.displayItems = new HashMap<>();
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

        loadPlayerKits(playerKits -> {

            for (PlayerKit playerKit : playerKits) {
                if (!playerKit.isVisible()) continue;
                ItemStack displayItem = buildDisplayItem(playerKit);
                this.displayItems.put(playerKit, displayItem);
                this.kitInventory.addItem(displayItem);
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
    public Map<PlayerKit, ItemStack> getDisplayItems() {
        return displayItems;
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

            var itemStack = this.displayItems.get(playerKit);
            if (itemStack != null) {
                this.displayItems.remove(playerKit);
                this.kitInventory.remove(itemStack);
            }

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

        var displayItem = buildDisplayItem(playerKit);

        if (playerKit.isVisible()) {
            this.getDisplayItems().put(playerKit, displayItem);
            this.kitInventory.addItem(displayItem);
        }

        this.playerKitList.add(playerKit);
        updatePlayerKits();

        return true;
    }

    @NotNull
    public KitGrantResult grantKit(@NotNull Player player, @NotNull PlayerKit playerKit, boolean ignoreCooldown) {

        if (!this.plugin.getCooldownManager()
                .isCooldownExpired(this.plugin.getCooldownManager()
                        .getPlayerKitCooldown(player.getUniqueId(), playerKit.getId())) && !ignoreCooldown) {
            return KitGrantResult.COOLDOWN_NOT_EXPIRED;
        }

        var kitContents = InventoryUtil.getContents(playerKit.getContent());
        PlayerInventory inventory = player.getInventory();

        int freeSpace = 0;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack != null && itemStack.getAmount() == itemStack.getMaxStackSize()) continue;
            freeSpace++;
        }

        if (freeSpace < kitContents.length) {
            return KitGrantResult.NOT_ENOUGH_SPACE;
        }

        return KitGrantResult.SUCCESS;
    }

    public void handleGrantKit(@NotNull CommandSender commandSender, @NotNull Player target,
                               @NotNull PlayerKit playerKit, boolean ignoreCooldown) {

        var kitCooldown = this.plugin.getCooldownManager()
                .getPlayerKitCooldown(target.getUniqueId(), playerKit.getId());

        var result = grantKit(target, playerKit, ignoreCooldown);

        if (this.plugin.getCooldownManager().isCooldownExpired(kitCooldown)) {
            this.plugin.getCooldownManager().removeCooldown(target.getUniqueId(), playerKit.getId());
        }

        var displayName = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(target.displayName())));

        switch (result) {

            case NOT_ENOUGH_SPACE ->
                    commandSender.sendMessage(this.plugin.getMessagesManager()
                            .getMessageComponent("inventory.not-enough-space", displayName));

            case SUCCESS -> {

                for (ItemStack itemStack : InventoryUtil.getContents(playerKit.getContent())) {
                    target.getInventory().addItem(itemStack);
                }

                if (playerKit.getCooldownTime() != PlayerKitCooldownManager.NO_COOLDOWN && !ignoreCooldown) {
                    kitCooldown = new PlayerKitCooldown.Builder(playerKit.getId())
                            .cooldown(TimeUtil.getCooldownTime(playerKit.getCooldownTimeUnit(),
                                    playerKit.getCooldownTime()))
                            .playerId(target.getUniqueId()).build();
                    this.plugin.getCooldownManager().createKitCooldown(kitCooldown);
                }

                if (!commandSender.equals(target)) {
                    commandSender.sendMessage(this.plugin.getMessagesManager()
                            .getMessageComponent("kit.grant.other.success", playerKit.getName(), displayName));
                }

                target.sendMessage(this.plugin.getMessagesManager().getMessageComponent("kit.grant.success",
                        playerKit.getName()));
            }

            case COOLDOWN_NOT_EXPIRED -> {
                if (kitCooldown != null) {
                    commandSender.sendMessage(this.plugin.getMessagesManager()
                            .getMessageComponent("cooldown-expires-at", playerKit.getName(),
                            this.plugin.getMessagesManager().formatMillis(kitCooldown.getCooldown()), displayName));
                }
            }
        }

    }

    @Nullable
    public ItemStack getDisplayItem(@NotNull PlayerKit playerKit) {
        return this.displayItems.get(playerKit);
    }

    public void grantSpecialKit(@NotNull Player player) {
        if (!this.plugin.isSpecialPlayer(player)) return;

        var playerKit = getPlayerKit(DRACONIA_KIT_NAME);
        if (playerKit != null) {
            if (grantKit(player, playerKit, true) == KitGrantResult.SUCCESS) {
                this.plugin.removeSpecialPlayer(player);
                player.sendMessage(this.plugin.getMessagesManager().getMessageComponent("kit.grant.special",
                        LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName())));
            }
        }
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
            var displayItem = getDisplayItem(kit);

            if (displayItem == null) continue;
            if (displayItem.isSimilar(itemStack)) {
                playerKit = kit;
            }
        }

        return playerKit;

    }

    public long getLastKitId() {
        List<PlayerKit> kits = this.getPlayerKitList();
        if (kits.isEmpty()) return 0;
        return kits.get(kits.size() - 1).getId();
    }

    @NotNull
    public List<String> getPlayerKitNames() {

        List<String> kitNames = new ArrayList<>();
        for (PlayerKit playerKit : this.playerKitList) {
            kitNames.add(playerKit.getName());
        }

        return kitNames;
    }

    @NotNull
    private ItemStack buildDisplayItem(PlayerKit playerKit) {
        var displayItem = playerKit.getContainerItem().toItemStack();
        displayItem.lore(this.plugin.getMessagesManager().getKitItemDescription());
        return displayItem;
    }

    private void updatePlayerKits() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.playerKitsFile.toPath())) {
            bufferedWriter.write(GSON.toJson(this.playerKitList));
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not update the playerkits.", e);
        }
    }
}
