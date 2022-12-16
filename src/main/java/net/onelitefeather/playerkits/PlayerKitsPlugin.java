package net.onelitefeather.playerkits;

import net.onelitefeather.playerkits.listener.InventoryListener;
import net.onelitefeather.playerkits.listener.PlayerConnectionListener;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.service.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerKitsPlugin extends JavaPlugin {

    private PlayerKitService playerKitService;
    private ItemRegistry itemRegistry;
    private I18nLocaleService i18nLocaleService;
    private PlayerKitSetupService playerKitSetupService;

    private PaperCommandService paperCommandService;

    private ClaimedKitService claimedKitService;
    private DatabaseService databaseService;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.databaseService = new DatabaseService(
                getConfig().getString("database.jdbcUrl", ""),
                getConfig().getString("database.driver", ""),
                getConfig().getString("database.username", ""),
                getConfig().getString("database.password", ""),
                getConfig().getString("database.dialect", ""));

        PluginManager pluginManager = getServer().getPluginManager();

        this.i18nLocaleService = new I18nLocaleService(this);
        this.itemRegistry = new ItemRegistry(this);

        this.claimedKitService = new ClaimedKitService(this);
        this.playerKitSetupService = new PlayerKitSetupService(this);
        this.playerKitService = new PlayerKitService(this);

        this.paperCommandService = new PaperCommandService(this);
        this.paperCommandService.registerCommands();

        pluginManager.registerEvents(new InventoryListener(this, this.playerKitService), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this, this.playerKitService), this);
    }

    @Override
    public void onDisable() {
        if (!this.databaseService.getSessionFactory().isClosed()) {
            this.databaseService.getSessionFactory().close();
        }
    }

    @NotNull
    public ClaimedKitService getClaimedKitService() {
        return claimedKitService;
    }

    @NotNull
    public PaperCommandService getPaperCommandService() {
        return paperCommandService;
    }

    @NotNull
    public PlayerKitSetupService getPlayerKitSetupService() {
        return playerKitSetupService;
    }

    @NotNull
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    @NotNull
    public I18nLocaleService i18n() {
        return i18nLocaleService;
    }

    @NotNull
    public PlayerKitService getPlayerKitService() {
        return playerKitService;
    }

    @NotNull
    public DatabaseService getDatabaseService() {
        return databaseService;
    }


    public List<String> getSpecialPlayers() {
        return this.getConfig().getStringList("special-players");
    }

    public boolean isSpecialPlayer(Player player) {
        return getSpecialPlayers().contains(player.getUniqueId().toString());
    }

    public void removeSpecialPlayer(Player player) {
        List<String> players = getSpecialPlayers();
        players.remove(player.getUniqueId().toString());
        this.getConfig().set("special-players", !players.isEmpty() ? players : null);
        saveConfig();
    }
}
