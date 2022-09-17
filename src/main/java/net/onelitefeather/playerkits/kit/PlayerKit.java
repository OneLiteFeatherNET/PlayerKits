package net.onelitefeather.playerkits.kit;

import com.google.gson.annotations.Expose;
import net.onelitefeather.playerkits.kit.item.ContainerItem;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class PlayerKit {

    @Expose
    private final long id;

    @Expose
    private final long cooldownTime;

    @Expose
    private final String items;

    @Expose
    private final String name;

    @Expose
    private final ContainerItem containerItem;
    @Expose
    private final TimeUnit cooldownTimeUnit;

    @Expose
    private final double price;

    @Expose
    private final boolean visible;

    private ItemStack[] content;

    @SuppressWarnings("java:S107")
    public PlayerKit(long id,
                     @NotNull String name,
                     @NotNull ContainerItem containerItem,
                     @NotNull String items,
                     double price,
                     boolean visible,
                     @NotNull TimeUnit cooldownTimeUnit,
                     long cooldownTime) {
        this.id = id;
        this.items = items;
        this.name = name;
        this.containerItem = containerItem;
        this.price = price;
        this.visible = visible;
        this.cooldownTimeUnit = cooldownTimeUnit;
        this.cooldownTime = cooldownTime;
        this.content = InventoryUtil.deserializeInventoryFromString(items);
    }

    public long getId() {
        return id;
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
    public ContainerItem getContainerItem() {
        return containerItem;
    }

    public double getPrice() {
        return price;
    }

    public boolean isVisible() {
        return visible;
    }

    public long getCooldownTime() {
        return cooldownTime;
    }

    @NotNull
    public TimeUnit getCooldownTimeUnit() {
        return cooldownTimeUnit;
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
        if (!(o instanceof PlayerKit playerKit)) return false;

        if (id != playerKit.id) return false;
        if (cooldownTime != playerKit.cooldownTime) return false;
        if (Double.compare(playerKit.price, price) != 0) return false;
        if (visible != playerKit.visible) return false;
        if (!Objects.equals(items, playerKit.items)) return false;
        if (!Objects.equals(name, playerKit.name)) return false;
        if (!Objects.equals(containerItem, playerKit.containerItem))
            return false;

        return cooldownTimeUnit == playerKit.cooldownTimeUnit;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (cooldownTime ^ (cooldownTime >>> 32));
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (containerItem != null ? containerItem.hashCode() : 0);
        result = 31 * result + (cooldownTimeUnit != null ? cooldownTimeUnit.hashCode() : 0);
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (visible ? 1 : 0);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @NotNull
    public String getDisplayName() {
        return this.getContainerItem().getDisplayName();
    }

    public static final class PlayerKitBuilder {

        private final long id;
        private final String name;

        private String items;
        private ContainerItem containerItem;
        private TimeUnit cooldownTimeUnit;
        private long cooldownTime;

        private double price;

        private boolean visible;

        public PlayerKitBuilder(long id, @NotNull String name) {
            this.id = id;
            this.name = name;
        }

        public PlayerKitBuilder withContainerItem(@NotNull ContainerItem containerItem) {
            this.containerItem = containerItem;
            return this;
        }

        public PlayerKitBuilder content(@Nullable ItemStack @NotNull [] stacks) {
            this.items = InventoryUtil.serializeInventoryToString(stacks);
            return this;
        }

        public PlayerKitBuilder withPrice(double price) {
            this.price = price;
            return this;
        }

        public PlayerKitBuilder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public PlayerKitBuilder cooldownTimeUnit(@NotNull TimeUnit cooldownTimeUnit) {
            this.cooldownTimeUnit = cooldownTimeUnit;
            return this;
        }

        public PlayerKitBuilder cooldownTime(long cooldownTime) {
            this.cooldownTime = cooldownTime;
            return this;
        }

        public PlayerKit build() {
            return new PlayerKit(this.id, this.name, this.containerItem, this.items, this.price, this.visible, this.cooldownTimeUnit, this.cooldownTime);
        }
    }

}
