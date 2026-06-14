package net.onelitefeather.playerkits;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.onelitefeather.playerkits.listener.InventoryListener;
import net.onelitefeather.playerkits.listener.PlayerConnectionListener;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import net.onelitefeather.playerkits.service.ClaimedKitService;
import net.onelitefeather.playerkits.service.DatabaseService;
import net.onelitefeather.playerkits.service.PaperCommandService;
import net.onelitefeather.playerkits.service.PlayerKitService;
import net.onelitefeather.playerkits.service.PlayerKitSetupService;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlayerKitsPlugin extends JavaPlugin {

    private static final List<Locale> SUPPORTED_LOCALS = List.of(Locale.US, Locale.GERMANY);

    private PlayerKitService playerKitService;
    private ItemRegistry itemRegistry;

    private PlayerKitSetupService kitSetupService;
    private PaperCommandService paperCommandService;
    private ClaimedKitService claimedKitService;
    private DatabaseService databaseService;

    private MiniMessageTranslationStore translationStore;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        //Save new default values
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.databaseService = new DatabaseService(this);

        PluginManager pluginManager = getServer().getPluginManager();

        this.itemRegistry = new ItemRegistry(this);

        this.claimedKitService = new ClaimedKitService(this);

        this.playerKitService = new PlayerKitService(this);
        this.playerKitService.init();

        this.kitSetupService = new PlayerKitSetupService(this);

        this.paperCommandService = new PaperCommandService(this);
        this.paperCommandService.registerCommands();

        this.translationStore = MiniMessageTranslationStore.create(Key.key("playerkits", "translations"));
        this.translationStore.defaultLocale(SUPPORTED_LOCALS.getFirst());
        GlobalTranslator.translator().addSource(this.translationStore);

        SUPPORTED_LOCALS.forEach(locale -> this.translationStore.registerAll(
                locale,
                ResourceBundle.getBundle("playerkits", locale),
                false));

        pluginManager.registerEvents(new InventoryListener(this, this.playerKitService), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this, this.playerKitService), this);
        pluginManager.registerEvents(this.kitSetupService, this);
    }

    @Override
    public void onDisable() {
        if (!this.databaseService.getSessionFactory().map(SessionFactory::isClosed).orElse(true)) {
            this.databaseService.getSessionFactory().ifPresent(SessionFactory::close);
        }

        if (this.translationStore != null) GlobalTranslator.translator().removeSource(translationStore);
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

    public Component getPluginPrefix() {
        return Component.translatable("prefix");
    }
}
