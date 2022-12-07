package net.onelitefeather.playerkits.kit.setup;

import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperty;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerKitSetup {

    private String name;
    private final List<PlayerKitProperty<?>> kitPropertyList;

    private ItemStack[] content;

    public PlayerKitSetup(@NotNull String name) {
        this.name = name;
        this.content = null;
        this.kitPropertyList = new ArrayList<>();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }


    @SuppressWarnings("java:S1452")
    @Nullable
    public PlayerKitProperty<?> getKitProperty(@NotNull PlayerKitProperty.Type type) {

        PlayerKitProperty<?> kitProperty = null;
        for (int i = 0; i < this.kitPropertyList.size() && kitProperty == null; i++) {
            var current = this.kitPropertyList.get(i);
            if (current.getType() == type) {
                kitProperty = current;
            }
        }

        return kitProperty;
    }

    public <T> PlayerKitProperty<T> addPropertyValue(@NotNull PlayerKitProperty<T> property) {

        var kitProperty = getKitProperty(property.getType());
        if (kitProperty != null) {
            this.kitPropertyList.remove(kitProperty);
        }

        this.kitPropertyList.add(property);
        return property;
    }

    @Nullable
    public ItemStack[] getContent() {
        return content;
    }

    public void setContent(@Nullable ItemStack @NotNull [] content) {
        this.content = content;
    }

    public boolean isDone() {
        return this.kitPropertyList.size() == PlayerKitProperty.DEFAULTS.size() && content != null;
    }

    @NotNull
    public PlayerKit createKit() {
        if (!isDone()) throw new IllegalStateException("The Setup for Kit %s is not done!".formatted(this.name));
        return new PlayerKit(name, InventoryUtil.serializeInventoryToString(this.content), this.kitPropertyList);
    }
}
