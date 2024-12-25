package net.onelitefeather.playerkits.listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record InventoryListener(@NotNull PlayerKitsPlugin plugin,
                                @NotNull PlayerKitService playerKitService) implements Listener {

    private static final Material PLACEHOLDER_ITEM = Material.BLACK_STAINED_GLASS_PANE;

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

        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTopInventory().equals(this.playerKitService.getKitInventory())) {

            event.setCancelled(true);

            var clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            if (clickedInventory.equals(this.playerKitService.getKitInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem == null) return;
                if (currentItem.getType() == PLACEHOLDER_ITEM) return;
                performAction(player, currentItem, event.getClick());
            }
        }
    }

    @NotNull
    private Component getDisplayName(@NotNull ItemStack itemStack) {

        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack.displayName();

        var displayName = itemMeta.displayName();
        return displayName != null ? displayName : itemStack.displayName();
    }

    private void performAction(Player player, ItemStack itemStack, ClickType clickType) {

        var playerKit = this.playerKitService.getPlayerKit(itemStack.getType());
        if (playerKit == null) {
            player.sendMessage(Component.translatable("kit.not-found")
                    .arguments(this.plugin.getPluginPrefix(), getDisplayName(itemStack)));
            return;
        }

        handleInventoryClick(player, playerKit, clickType);
    }

    private void handleInventoryClick(@NotNull Player player, @NotNull PlayerKit playerKit, ClickType clickType) {

        if (clickType.isLeftClick()) {
            this.playerKitService.handleGrantKit(player, player, playerKit);
            player.closeInventory();
        }

        if (clickType.isRightClick()) {
            this.playerKitService.previewKit(player, playerKit);
        }
    }
}
