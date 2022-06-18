package net.onelitefeather.playerkits;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.playerkits.commands.KitCommand;
import net.onelitefeather.playerkits.kit.PlayerKitManager;
import net.onelitefeather.playerkits.kit.cooldown.PlayerKitCooldownManager;
import net.onelitefeather.playerkits.language.MessagesManager;
import net.onelitefeather.playerkits.listener.InventoryListener;
import net.onelitefeather.playerkits.listener.PlayerConnectionListener;
import net.onelitefeather.playerkits.registry.ItemRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Function;

public class PlayerKitsPlugin extends JavaPlugin {

    private MinecraftHelp<CommandSender> minecraftHelp;
    private PlayerKitManager playerKitManager;
    private PlayerKitCooldownManager playerKitCooldownManager;
    private ItemRegistry itemRegistry;
    private MessagesManager messagesManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        PluginManager pluginManager = getServer().getPluginManager();

        this.messagesManager = new MessagesManager(this);
        this.itemRegistry = new ItemRegistry(this);


        this.playerKitManager = new PlayerKitManager(this);
        this.playerKitCooldownManager = new PlayerKitCooldownManager(this);

        buildCommandSystem();
        pluginManager.registerEvents(new InventoryListener(this, this.playerKitManager, this.playerKitCooldownManager), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this, this.playerKitManager), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
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
