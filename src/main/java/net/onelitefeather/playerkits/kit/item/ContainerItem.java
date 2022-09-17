package net.onelitefeather.playerkits.kit.item;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ContainerItem {

    private static final ItemFlag[] ITEM_FLAGS = ItemFlag.values();

    @Expose
    private final Material material;

    @Expose
    private final String displayName;

    private ItemStack itemStack;

    public ContainerItem(@NotNull Material material, @NotNull String displayName) {
        this.material = material;
        this.displayName = displayName;
    }

    /**
     * @return the {@link ItemStack}
     */
    @NotNull
    public ItemStack toItemStack() {

        if (this.itemStack == null) {
            this.itemStack = new ItemStack(material);
            this.itemStack.addItemFlags(ITEM_FLAGS);

            ItemMeta itemMeta = this.itemStack.getItemMeta();
            itemMeta.displayName(MiniMessage.miniMessage().deserialize(displayName));
            this.itemStack.setItemMeta(itemMeta);
        }

        return this.itemStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerItem that)) return false;

        if (material != that.material) return false;
        return Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + (itemStack != null ? itemStack.hashCode() : 0);
        return result;
    }

    /**
     * @return the material of the item
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * @return the displayName of the item
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public static class ContainerItemBuilder {

        private Material material;
        private String displayName;

        public ContainerItemBuilder withMaterial(@NotNull Material material) {
            this.material = material;
            return this;
        }

        public ContainerItemBuilder withDisplayName(@NotNull String displayName) {
            this.displayName = displayName;
            return this;
        }

        @NotNull
        public ContainerItem build() {
            return new ContainerItem(this.material, this.displayName);
        }

    }
}
