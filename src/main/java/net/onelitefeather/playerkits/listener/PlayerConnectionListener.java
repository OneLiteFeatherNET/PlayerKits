package net.onelitefeather.playerkits.listener;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public record PlayerConnectionListener(@NotNull PlayerKitsPlugin plugin,
                                       @NotNull PlayerKitService kitService) implements Listener {

    @EventHandler
    private void handlePlayerJoin(PlayerJoinEvent event) {
        this.kitService.giveFirstJoinKits(event.getPlayer());
    }
}
