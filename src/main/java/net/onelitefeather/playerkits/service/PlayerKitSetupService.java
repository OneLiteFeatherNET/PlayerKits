package net.onelitefeather.playerkits.service;

import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.setup.PlayerKitSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PlayerKitSetupService {

    private final PlayerKitsPlugin plugin;

    private final List<PlayerKitSetup> playerKitSetupList;

    public PlayerKitSetupService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.playerKitSetupList = new ArrayList<>();
    }

    @NotNull
    public List<PlayerKitSetup> getPlayerKitSetupList() {
        return playerKitSetupList;
    }

    public void addSetup(@NotNull PlayerKitSetup playerKitSetup) {
        if (!playerKitSetupList.contains(playerKitSetup)) {
            this.playerKitSetupList.add(playerKitSetup);
        }
    }

    @Nullable
    public PlayerKitSetup getSetup(@NotNull String name) {

        PlayerKitSetup playerKitSetup = null;

        for (int i = 0; i < this.playerKitSetupList.size() && playerKitSetup == null; i++) {
            var current = this.playerKitSetupList.get(i);
            if (current.getName().equalsIgnoreCase(name)) {
                playerKitSetup = current;
            }
        }

        return playerKitSetup;
    }

    public void removeSetup(@NotNull PlayerKitSetup setup, @NotNull PlayerKit playerKit) {
        this.playerKitSetupList.remove(setup);
        this.plugin.getPlayerKitService().createPlayerKit(playerKit);
    }
}
