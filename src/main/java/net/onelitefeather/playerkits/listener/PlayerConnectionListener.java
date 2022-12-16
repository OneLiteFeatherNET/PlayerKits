package net.onelitefeather.playerkits.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.KitClaimResult;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public record PlayerConnectionListener(@NotNull PlayerKitsPlugin plugin,
                                       @NotNull PlayerKitService kitService) implements Listener {

    @EventHandler
    private void handlePlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        var playerKit = this.kitService.getPlayerKit(PlayerKitService.DRACONIA_KIT_NAME);
        if (playerKit != null) {
            if(!this.plugin.isSpecialPlayer(player)) return;
            this.plugin.removeSpecialPlayer(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.plugin.i18n().getMessage("kit.grant.special",
                    this.plugin.i18n().getPrefix(),
                    LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName()))));
        } else {
            var kit = this.kitService.getFirstJoinKit();
            if (kit != null && this.plugin.getClaimedKitService().canClaim(player.getUniqueId(), kit.getName()) == KitClaimResult.SUCCESS) {
                this.kitService.handleGrantKit(player, player, kit, true);
            }
        }
    }
}
