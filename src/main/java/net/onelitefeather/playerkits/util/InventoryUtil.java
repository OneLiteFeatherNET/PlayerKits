package net.onelitefeather.playerkits.util;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class InventoryUtil {

    private InventoryUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void serializeInventory(@Nullable ItemStack[] inventory, @NotNull OutputStream outputStream) {
        if(inventory == null) return;
        try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(inventory.length);
            for (ItemStack itemStack : inventory) {
                dataOutput.writeObject(itemStack);
            }
        } catch (IOException e) {
            JavaPlugin.getPlugin(PlayerKitsPlugin.class).getLogger().log(Level.SEVERE, "Unable to save item stacks.", e);
        }
    }

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

    public static @NotNull String serializeInventoryToString(@Nullable ItemStack @NotNull[] items) {
        String data = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            serializeInventory(items, outputStream);
            data = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            JavaPlugin.getPlugin(PlayerKitsPlugin.class).getLogger().log(Level.SEVERE, "Unable to save item stacks.", e);
        }
        return data;
    }

    @NotNull
    public static ItemStack[] deserializeInventoryFromString(@NotNull String data) {
        ItemStack[] items = new ItemStack[0];
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data))) {
            items = deserializeInventory(inputStream);
        } catch (IOException e) {
            JavaPlugin.getPlugin(PlayerKitsPlugin.class).getLogger().log(Level.SEVERE, "Unable to load item stacks.", e);
        }
        return items;
    }
    @NotNull
    public static ItemStack[] getContents(@Nullable ItemStack @NotNull [] itemStacks) {

        List<ItemStack> list = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                list.add(itemStack);
            }
        }

        return list.toArray(new ItemStack[0]);
    }
}
