package net.onelitefeather.playerkits.command.parser;

import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class KitCommandParser {

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

        List<String> names = new ArrayList<>();
        var kits = this.playerKitService.getKits();
        for(PlayerKit kit : kits) {
            names.add(kit.getName());
        }

        return StringUtil.copyPartialMatches(input, names, new ArrayList<>(names.size()));
    }
}
