package net.onelitefeather.playerkits.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.PlayerKitManager;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public record InventoryListener(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitManager playerKitManager,
                                @NotNull PlayerKitCooldownManager cooldownManager) implements Listener {

    @EventHandler
    private void handleClick(InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player player) {
            if (event.getSlotType() != InventoryType.SlotType.CONTAINER) return;

            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            if (clickedInventory.equals(this.playerKitManager.getKitPreviewInventory())) {
                event.setCancelled(true);

                if (currentItem.isSimilar(this.plugin.getItemRegistry().getItem(currentItem))) {
                    player.openInventory(this.playerKitManager.getKitInventory());
                    return;
                }
            }

            if (clickedInventory.equals(this.plugin.getPlayerKitManager().getKitInventory())) {

                event.setCancelled(true);

                ItemMeta itemMeta = currentItem.getItemMeta();
                if (itemMeta == null) return;

                Component displayNameComp = itemMeta.displayName();
                if (displayNameComp == null) return;

                PlayerKit playerKit = this.playerKitManager.getPlayerKit(currentItem);
                if (playerKit == null) {
                    player.sendMessage(this.plugin.getMessagesManager().getMessageComponent("kit.not-found", PlainTextComponentSerializer.plainText().serialize(displayNameComp)));
                    return;
                }

                ClickType clickType = event.getClick();
                if (clickType.isLeftClick()) {
                    this.playerKitManager.handleGrantKit(player, player, playerKit, false);
                    player.closeInventory();
                }

                if (clickType.isRightClick()) {
                    this.playerKitManager.previewKit(player, playerKit);
                }
            }
        }
    }
}
