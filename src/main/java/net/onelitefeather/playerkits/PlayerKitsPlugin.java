package net.onelitefeather.playerkits;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.playerkits.listener.InventoryListener;
import net.onelitefeather.playerkits.listener.PlayerConnectionListener;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.service.*;
import net.onelitefeather.playerkits.util.LynxWrapper;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlayerKitsPlugin extends JavaPlugin {

    private static final List<Locale> SUPPORTED_LOCALS = List.of(Locale.US);

    private PlayerKitService playerKitService;
    private ItemRegistry itemRegistry;

    private PlayerKitSetupService kitSetupService;
    private PaperCommandService paperCommandService;
    private ClaimedKitService claimedKitService;
    private DatabaseService databaseService;

    private LynxWrapper lynxWrapper;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        //Save new default values
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.databaseService = new DatabaseService(
                getConfig().getString("database.jdbcUrl", ""),
                getConfig().getString("database.driver", ""),
                getConfig().getString("database.username", ""),
                getConfig().getString("database.password", ""),
                getConfig().getString("database.dialect", ""));

        PluginManager pluginManager = getServer().getPluginManager();

        this.itemRegistry = new ItemRegistry(this);

        this.claimedKitService = new ClaimedKitService(this);
        this.playerKitService = new PlayerKitService(this);
        this.kitSetupService = new PlayerKitSetupService(this);

        this.paperCommandService = new PaperCommandService(this);
        this.paperCommandService.registerCommands();

        var registry = TranslationRegistry.create(Key.key("playerkits", "localization"));
        SUPPORTED_LOCALS.forEach(locale -> {
            var bundle = ResourceBundle.getBundle("playerkits", locale, UTF8ResourceBundleControl.get());
            registry.registerAll(locale, bundle, false);
        });

        registry.defaultLocale(SUPPORTED_LOCALS.get(0));
        this.lynxWrapper = new LynxWrapper(registry);
        GlobalTranslator.translator().addSource(lynxWrapper);

        pluginManager.registerEvents(new InventoryListener(this, this.playerKitService), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this, this.playerKitService), this);
        pluginManager.registerEvents(this.kitSetupService, this);
    }

    @Override
    public void onDisable() {
        if (!this.databaseService.getSessionFactory().isClosed()) {
            this.databaseService.getSessionFactory().close();
        }

        GlobalTranslator.translator().removeSource(lynxWrapper);
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
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    @NotNull
    public PlayerKitService getPlayerKitService() {
        return playerKitService;
    }

    @NotNull
    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    @NotNull
    public PlayerKitSetupService getKitSetupService() {
        return kitSetupService;
    }

    public String getPluginPrefix() {
        return "<lang:prefix>";
    }
}
