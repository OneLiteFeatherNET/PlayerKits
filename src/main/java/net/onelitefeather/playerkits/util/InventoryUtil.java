package net.onelitefeather.playerkits.util;

import net.kyori.adventure.text.Component;
import net.onelitefeather.playerkits.kit.PlayerKit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        var items = Arrays.stream(kitContents).map(ItemStack::getType).filter(material -> !material.isAir()).map(Enum::toString).toList();

        int freeSpace = (int) Arrays.stream(inventory.getStorageContents()).filter(Objects::isNull).count();
        return freeSpace < items.size() ? -1 : freeSpace;
    }
}
