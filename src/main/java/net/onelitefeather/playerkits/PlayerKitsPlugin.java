package net.onelitefeather.playerkits;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.playerkits.commands.KitCommand;
import net.onelitefeather.playerkits.kit.PlayerKitManager;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldown;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldownManager;
import net.onelitefeather.playerkits.language.MessagesManager;
import net.onelitefeather.playerkits.listener.InventoryListener;
import net.onelitefeather.playerkits.listener.PlayerConnectionListener;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.tool.schema.Action;

import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerKitsPlugin extends JavaPlugin {

    private MinecraftHelp<CommandSender> minecraftHelp;
    private PlayerKitManager playerKitManager;
    private PlayerKitCooldownManager playerKitCooldownManager;
    private ItemRegistry itemRegistry;
    private MessagesManager messagesManager;
    private SessionFactory sessionFactory;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        if (!isDebugEnabled()) {
            applyMigrations();
        }
        buildSessionFactory();

        PluginManager pluginManager = getServer().getPluginManager();

        this.messagesManager = new MessagesManager(this);
        this.itemRegistry = new ItemRegistry(this);

        this.playerKitManager = new PlayerKitManager(this);
        this.playerKitCooldownManager = new PlayerKitCooldownManager(this);

        buildCommandSystem();

        pluginManager.registerEvents(new InventoryListener(this, this.playerKitManager, this.playerKitCooldownManager), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this, this.playerKitManager), this);
    }

    private boolean isDebugEnabled() {
        return this.getSLF4JLogger().isDebugEnabled();
    }

    @Override
    public void onDisable() {
        if (this.sessionFactory != null && !this.sessionFactory.isClosed()) {
            this.sessionFactory.close();
        }
    }

    public PlayerKitCooldownManager getCooldownManager() {
        return playerKitCooldownManager;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public PlayerKitManager getPlayerKitManager() {
        return playerKitManager;
    }

    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return minecraftHelp;
    }

    private void buildCommandSystem() {
        final PaperCommandManager<CommandSender> bukkitCommandManager;

        //Commands
        AnnotationParser<CommandSender> annotationParser;
        try {
            bukkitCommandManager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
            if (bukkitCommandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
                bukkitCommandManager.registerBrigadier();
            }

            if (bukkitCommandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                bukkitCommandManager.registerAsynchronousCompletions();
            }

            final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
            annotationParser = new AnnotationParser<>(bukkitCommandManager, CommandSender.class, commandMetaFunction);
            this.minecraftHelp = MinecraftHelp.createNative("/playerkits help", bukkitCommandManager);
        } catch (final Exception e) {
            this.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(NamedTextColor.DARK_PURPLE, NamedTextColor.LIGHT_PURPLE, NamedTextColor.WHITE, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY));
        annotationParser.parse(new KitCommand(this, this.playerKitManager));
    }

    public List<String> getSpecialPlayers() {
        return this.getConfig().getStringList("special-players");
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
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

    private void applyMigrations() {
        DatabaseConnection database;
        try {
            database = DatabaseFactory.getInstance().openConnection(
                    getConfig().getString("database.jdbcUrl"),
                    getConfig().getString("database.username"),
                    getConfig().getString("database.password"),
                    null,
                    new ClassLoaderResourceAccessor(getClassLoader())
            );
        } catch (DatabaseException e) {
            getLogger().log(Level.SEVERE, "Something went wrong at open database connection", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (database != null) {
            try {
                Logger.getLogger("liquibase").setParent(getLogger());
                Liquibase liquibase = new Liquibase("db/changelog/db.changelog-diff.xml", new ClassLoaderResourceAccessor(getClassLoader()), database);
                liquibase.validate();
                liquibase.update((String) null);
            } catch (LiquibaseException e) {
                getLogger().log(Level.SEVERE, "Something went wrong at apply database changes", e);
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }

    }


    private void buildSessionFactory() {

        var configuration = new Configuration();
        var properties = new Properties();
        properties.put(AvailableSettings.URL, getConfig().getString("database.jdbcUrl"));
        properties.put(AvailableSettings.DRIVER, getConfig().getString("database.driver"));
        properties.put(AvailableSettings.USER, getConfig().getString("database.username"));
        properties.put(AvailableSettings.PASS, getConfig().getString("database.password"));
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, ImplicitNamingStrategyLegacyJpaImpl.class);

        properties.put(AvailableSettings.CONNECTION_PROVIDER, HikariCPConnectionProvider.class);
        properties.put(AvailableSettings.DIALECT, new MariaDBDialect());

        if (isDebugEnabled()) {
            properties.put(AvailableSettings.HBM2DDL_AUTO, Action.CREATE_DROP);
        }

        configuration.setProperties(properties);
        configuration.addAnnotatedClass(PlayerKitCooldown.class);

        var registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        this.sessionFactory = configuration.buildSessionFactory(registry);
    }
}
