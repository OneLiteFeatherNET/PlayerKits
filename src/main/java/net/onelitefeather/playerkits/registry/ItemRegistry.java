package net.onelitefeather.playerkits.registry;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemRegistry {

    public static final String OPEN_LAST_INVENTORY = "openLastInventory";
    private final PlayerKitsPlugin plugin;
    private final Map<String, ItemStack> items;

    public ItemRegistry(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        build();
    }

    @NotNull
    public Map<String, ItemStack> getItems() {
        return items;
    }

    @Nullable
    public ItemStack getItem(@NotNull ItemStack origin) {

        ItemStack itemStack = null;
        List<ItemStack> itemStacks = new ArrayList<>(this.getItems().values());
        for (int i = 0; i < itemStacks.size() && itemStack == null; i++) {
            ItemStack stack = itemStacks.get(i);
            if (stack.isSimilar(origin)) {
                itemStack = stack;
            }
        }

        return itemStack;
    }

    @Nullable
    public ItemStack getItem(@NotNull String name) {
        return this.items.get(name);
    }

    public void build() {
        ItemStack openLastInventoryItem = new ItemStack(Material.RED_DYE);
        ItemMeta itemMeta = openLastInventoryItem.getItemMeta();
        itemMeta.displayName(MiniMessage.miniMessage().deserialize(this.plugin.getMessagesManager().getMessage("items.open-last-inventory")));
        openLastInventoryItem.setItemMeta(itemMeta);
        this.items.put(OPEN_LAST_INVENTORY, openLastInventoryItem);
    }
}
