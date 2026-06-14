package net.onelitefeather.playerkits.util;

import net.kyori.adventure.text.Component;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class InventoryUtil {

    private InventoryUtil() {
        throw new IllegalStateException("Utility class");
    }

    @NotNull
    public static ItemStack createItem(@NotNull Material material,
                                       @NotNull Component displayName,
                                       @Nullable List<Component> lore) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.displayName(displayName);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * @param inventory the inventory
     * @param playerKit the player kit
     * @return true if the inventory has enough space for the player kit
     */
    @NotNull
    public static Boolean hasInventorySpace(@NotNull Inventory inventory, @NotNull PlayerKit playerKit) {
        return getInventoryFreeSpace(inventory, playerKit) != -1;
    }

    /**
     * @param inventory the inventory
     * @param playerKit the player kit
     * @return the free slot count of the inventory or -1 if the inventory is full
     */
    @NotNull
    public static Integer getInventoryFreeSpace(@NotNull Inventory inventory, @NotNull PlayerKit playerKit) {
        var kitContents = ItemStack.deserializeItemsFromBytes(playerKit.getContents());
//        var kitContents = InventoryUtil.deserializeInventoryFromString(playerKit.getItems());
        int freeSpace = (int) Arrays.stream(inventory.getStorageContents()).filter(Objects::isNull).count();
        return freeSpace <= kitContents.length ? -1 : freeSpace;
    }

    @Deprecated(forRemoval = true, since = "1.0.0")
    public static @NotNull ItemStack[] deserializeInventory(@NotNull InputStream inputStream) {
        ItemStack[] itemStacks = new ItemStack[0];
        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            itemStacks = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                itemStacks[i] = (ItemStack) dataInput.readObject();
            }
            return itemStacks;
        } catch (IOException | ClassNotFoundException e) {
            JavaPlugin.getPlugin(PlayerKitsPlugin.class).getLogger().log(Level.SEVERE, "Unable to load item stacks.", e);
        }
        return itemStacks;
    }
}
