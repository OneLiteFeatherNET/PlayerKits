package net.onelitefeather.playerkits.command.parser;

import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;

public final class KitCommandParser {

    private final PlayerKitService playerKitService;

    public KitCommandParser(@NotNull PlayerKitService playerKitService) {
        this.playerKitService = playerKitService;
    }

    @Parser(name = "playerKit", suggestions = "playerKits")
    public @NotNull PlayerKit parsePlayerKit(CommandContext<CommandSender> commandSender, @NotNull Queue<String> input) {

        var name = input.remove();
        var playerKit = this.playerKitService.getPlayerKit(name);

        if (playerKit == null) {
            playerKit = new PlayerKit();
            playerKit.setName(name);
        }

        return playerKit;
    }

    @NotNull
    @Suggestions("playerKits")
    public List<String> getKitNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        List<String> names = this.playerKitService.getKits().stream().map(PlayerKit::getName).toList();
        return names.stream().filter(string -> StringUtil.startsWithIgnoreCase(string, input)).toList();
    }
}
