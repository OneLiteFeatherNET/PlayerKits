package net.onelitefeather.playerkits.service;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.command.KitCommand;
import net.onelitefeather.playerkits.command.parser.KitCommandParser;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperCommandService {

    private final PlayerKitsPlugin plugin;
    private final PaperCommandManager<CommandSourceStack> paperCommandManager;
    private final AnnotationParser<CommandSourceStack> annotationParser;
    private final MinecraftHelp<CommandSourceStack> minecraftHelp;

    public PaperCommandService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.paperCommandManager = buildCommandSystem();
        this.annotationParser = buildAnnotationParser();
        this.minecraftHelp = buildHelpSystem();
    }

    @NotNull
    public PaperCommandManager<CommandSourceStack> getPaperCommandManager() {
        return paperCommandManager;
    }

    @NotNull
    public AnnotationParser<CommandSourceStack> getAnnotationParser() {
        return annotationParser;
    }

    @NotNull
    public MinecraftHelp<CommandSourceStack> getMinecraftHelp() {
        return minecraftHelp;
    }

    public void registerCommands() {
        annotationParser.parse(new KitCommandParser(this.plugin.getPlayerKitService()));
        annotationParser.parse(new KitCommand(this.plugin, this.plugin.getPlayerKitService()));
    }

    @NotNull
    private MinecraftHelp<CommandSourceStack> buildHelpSystem() {
        AudienceProvider<CommandSourceStack> audienceProvider = CommandSourceStack::getSender;
        return MinecraftHelp.<CommandSourceStack>builder()
                .commandManager(paperCommandManager)
                .audienceProvider(audienceProvider)
                .commandPrefix("/hit help")
                .colors(MinecraftHelp.helpColors(
                        NamedTextColor.YELLOW,
                        NamedTextColor.GOLD,
                        NamedTextColor.YELLOW,
                        NamedTextColor.GRAY,
                        NamedTextColor.GOLD
                )).build();
    }

    @NotNull
    private AnnotationParser<CommandSourceStack> buildAnnotationParser() {

        return new AnnotationParser<>(paperCommandManager, CommandSourceStack.class);
    }

    @Nullable
    private PaperCommandManager<CommandSourceStack> buildCommandSystem() {

        try {
            PaperCommandManager<CommandSourceStack> commandManager = PaperCommandManager.builder()
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(this.plugin);
            return commandManager;
        } catch (final Exception e) {
            this.plugin.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return null;
        }
    }
}
