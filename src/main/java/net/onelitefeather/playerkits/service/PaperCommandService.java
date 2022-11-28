package net.onelitefeather.playerkits.service;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.commands.KitCommand;
import net.onelitefeather.playerkits.commands.KitSetupCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PaperCommandService {

    private final PlayerKitsPlugin plugin;
    private final PaperCommandManager<CommandSender> paperCommandManager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final MinecraftHelp<CommandSender> minecraftHelp;

    public PaperCommandService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.paperCommandManager = buildCommandSystem();
        this.annotationParser = buildAnnotationParser();
        this.minecraftHelp = buildHelpSystem();
    }

    @NotNull
    public PaperCommandManager<CommandSender> getPaperCommandManager() {
        return paperCommandManager;
    }

    @NotNull
    public AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }

    @NotNull
    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return minecraftHelp;
    }

    public void registerCommands() {
        annotationParser.parse(new KitCommand(this.plugin, this.plugin.getPlayerKitService()));
        annotationParser.parse(new KitSetupCommand(this.plugin));
    }

    @NotNull
    private MinecraftHelp<CommandSender> buildHelpSystem() {
        var help = MinecraftHelp.createNative("/playerkits help", paperCommandManager);
        help.setHelpColors(MinecraftHelp.HelpColors.of(
                NamedTextColor.YELLOW,
                NamedTextColor.GOLD,
                NamedTextColor.YELLOW,
                NamedTextColor.GRAY,
                NamedTextColor.GOLD));
        return help;
    }

    @NotNull
    private AnnotationParser<CommandSender> buildAnnotationParser() {
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
        return new AnnotationParser<>(paperCommandManager, CommandSender.class, commandMetaFunction);
    }

    @Nullable
    private PaperCommandManager<CommandSender> buildCommandSystem() {

        try {
            PaperCommandManager<CommandSender> commandManager = new PaperCommandManager<>(this.plugin,
                    CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());

            if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                commandManager.registerBrigadier();
            }

            if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                commandManager.registerAsynchronousCompletions();
            }

            return commandManager;
        } catch (final Exception e) {
            this.plugin.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return null;
        }
    }
}
