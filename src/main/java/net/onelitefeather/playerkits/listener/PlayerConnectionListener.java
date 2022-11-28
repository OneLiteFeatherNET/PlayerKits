package net.onelitefeather.playerkits.listener;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public record PlayerConnectionListener(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitService kitManager) implements Listener {

    @EventHandler
    private void handlePlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if (this.plugin.isSpecialPlayer(player)) {
            this.kitManager.grantSpecialKit(player);
        }
    }
}
