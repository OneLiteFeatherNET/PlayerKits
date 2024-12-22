package net.onelitefeather.playerkits.service;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.command.KitCommand;
import net.onelitefeather.playerkits.command.mapper.BukkitSenderMapper;
import net.onelitefeather.playerkits.command.parser.KitCommandParser;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperCommandService {

    private final PlayerKitsPlugin plugin;
    private final PaperCommandManager<CommandSender> paperCommandManager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final MinecraftHelp<CommandSender> minecraftHelp;
    private final BukkitAudiences bukkitAudiences;

    public PaperCommandService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.bukkitAudiences = BukkitAudiences.create(plugin);
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
        annotationParser.parse(new KitCommandParser(this.plugin.getPlayerKitService()));
        annotationParser.parse(new KitCommand(this.plugin, this.plugin.getPlayerKitService()));
    }

    @NotNull
    private MinecraftHelp<CommandSender> buildHelpSystem() {
        return MinecraftHelp.<CommandSender>builder()
                .commandManager(this.paperCommandManager)
                .audienceProvider(this.bukkitAudiences::sender)
                .commandPrefix("/kit help")
                .colors(MinecraftHelp.helpColors(
                        NamedTextColor.YELLOW,
                        NamedTextColor.GOLD,
                        NamedTextColor.YELLOW,
                        NamedTextColor.GRAY,
                        NamedTextColor.GOLD))
                .build();
    }

    @NotNull
    private AnnotationParser<CommandSender> buildAnnotationParser() {
        return new AnnotationParser<>(paperCommandManager, CommandSender.class);
    }

    @Nullable
    private PaperCommandManager<CommandSender> buildCommandSystem() {

        try {
            return PaperCommandManager.builder(new BukkitSenderMapper())
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(this.plugin);
        } catch (final Exception e) {
            this.plugin.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return null;
        }
    }
}
