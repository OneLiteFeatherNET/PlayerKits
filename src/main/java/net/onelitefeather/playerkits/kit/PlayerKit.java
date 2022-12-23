package net.onelitefeather.playerkits.kit;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperty;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class PlayerKit {

    @Expose
    private final String items;

    @Expose
    private final String name;

    @Expose
    private final List<PlayerKitProperty<?>> kitPropertyList;

    private ItemStack[] content;

    private ItemStack guiItem;

    @SuppressWarnings("java:S107")
    public PlayerKit(@NotNull String name,
                     @NotNull String items,
                     @NotNull List<PlayerKitProperty<?>> kitPropertyList) {
        this.items = items;
        this.name = name;
        this.kitPropertyList = kitPropertyList;
        this.content = InventoryUtil.deserializeInventoryFromString(items);
        this.guiItem = new ItemStack(Material.STONE);
    }

    @NotNull
    public ItemStack getGuiItem() {
        return guiItem;
    }

    public ItemStack setGuiItem(List<Component> lore) {
        var itemStack = content[0].clone().asOne();
        var itemMeta = itemStack.getItemMeta();
        var displayName = getPropertyValue(PlayerKitProperty.DISPLAY_NAME, String.class);
        itemMeta.displayName(MiniMessage.miniMessage().deserialize(displayName));
        itemMeta.lore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        this.guiItem = itemStack;
        return itemStack;
    }

    @NotNull
    public String getItems() {
        return items;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public <T> T getPropertyValue(@NotNull PlayerKitProperty<T> property, Class<T> target) {
        T value = property.getValue();
        for (int i = 0; i < this.kitPropertyList.size() && value.equals(property.getValue()); i++) {
            PlayerKitProperty<?> kitProperty = this.kitPropertyList.get(i);
            if (kitProperty.getType() == property.getType()) {
                value = target.cast(kitProperty.getValue());
            }
        }

        return value;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.valueOf(getPropertyValue(PlayerKitProperty.COOLDOWN_TIME_UNIT, String.class).toUpperCase());
    }

    public boolean isFirstJoin() {
        return getPropertyValue(PlayerKitProperty.FIRST_JOIN, Boolean.class);
    }

    public boolean isOneTime() {
        return getPropertyValue(PlayerKitProperty.ONE_TIME, Boolean.class);
    }

    @NotNull
    public String getDisplayName() {
        return getPropertyValue(PlayerKitProperty.DISPLAY_NAME, String.class);
    }

    /**
     * The Type of the PlayerKitProperty can be everything needs suppressing
     * @return a list of all player kit properties
     */
    @SuppressWarnings("java:S1452")
    @NotNull
    public List<PlayerKitProperty<?>> getKitPropertyList() {
        return kitPropertyList;
    }

    public boolean isVisible() {
        return getPropertyValue(PlayerKitProperty.VISIBLE, Boolean.class);
    }

    public long getCooldownTime() {
        return getPropertyValue(PlayerKitProperty.COOLDOWN_TIME, Double.class).longValue();
    }

    public void setContent(@Nullable ItemStack @NotNull [] content) {
        this.content = content;
    }

    public @Nullable ItemStack @NotNull [] getContent() {

        if (this.content == null) {
            this.content = InventoryUtil.deserializeInventoryFromString(this.items);
        }
        return this.content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerKit playerKit = (PlayerKit) o;
        return Objects.equals(items, playerKit.items) && Objects.equals(name, playerKit.name) && Objects.equals(kitPropertyList, playerKit.kitPropertyList) && Arrays.equals(content, playerKit.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(items, name, kitPropertyList);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}
