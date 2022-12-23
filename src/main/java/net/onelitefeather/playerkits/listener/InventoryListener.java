package net.onelitefeather.playerkits.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import net.onelitefeather.playerkits.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record InventoryListener(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitService playerKitService) implements Listener {

    @EventHandler
    public void handlePreviewInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;

        if (event.getView().getTopInventory().equals(this.playerKitService.getKitPreviewInventory())) {
            event.setCancelled(true);

            if (currentItem.isSimilar(this.plugin.getItemRegistry().getItem(currentItem))) {
                player.openInventory(this.playerKitService.getKitInventory());
            }
        }
    }

    @EventHandler
    private void handleClick(InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player player && event.getView().getTopInventory().equals(this.playerKitService.getKitInventory())) {

            event.setCancelled(true);

            var clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            if (clickedInventory.equals(this.playerKitService.getKitInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem == null) return;

                getPlayerKit(currentItem, playerKit -> {
                    if (playerKit != null) {
                        handleInventoryClick(player, playerKit, event.getClick());
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                this.plugin.i18n().getMessage("kit.not-found", this.plugin.i18n().getPrefix(),
                                        PlainTextComponentSerializer.plainText().serialize(getDisplayName(currentItem)))));
                    }
                });
            }
        }
    }

    private void getPlayerKit(@NotNull ItemStack currentItem, Consumer<PlayerKit> consumer) {
        consumer.accept(this.playerKitService.getPlayerKit(currentItem));
    }

    @NotNull
    private Component getDisplayName(@NotNull ItemStack itemStack) {

        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack.displayName();

        var displayName = itemMeta.displayName();
        return displayName != null ? displayName : itemStack.displayName();
    }

    private void handleInventoryClick(@NotNull Player player, @NotNull PlayerKit playerKit, ClickType clickType) {

        if (clickType.isLeftClick()) {
            this.playerKitService.handleGrantKit(player, player, playerKit, playerKit.getCooldownTime() == TimeUtil.NO_COOLDOWN);
            player.closeInventory();
        }

        if (clickType.isRightClick()) {
            this.playerKitService.previewKit(player, playerKit);
        }
    }
}
