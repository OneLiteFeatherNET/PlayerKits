package net.onelitefeather.playerkits.command;

import net.kyori.adventure.text.Component;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.NotNull;

public record KitCommand(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitService playerKitService) {

    @Command("kit create <name>")
    @Permission("playerkits.command.kit.create")
    @CommandDescription("Create a new kit")
    public void createKitCommand(@NotNull Player player, @Argument("name") @Greedy String name) {
        if (this.playerKitService.existsPlayerKit(name)) {
            player.sendMessage(Component.translatable("kit.already.exist").arguments(this.plugin.getPluginPrefix(), Component.text(name)));
            return;
        }

        this.plugin.getKitSetupService().addSetup(player, name);
        player.sendMessage(Component.translatable("kit.setup.help.cancel").arguments(this.plugin.getPluginPrefix()));
        player.sendMessage(Component.translatable("kit.setup.help.previous-step").arguments(this.plugin.getPluginPrefix()));
    }

    @Command("kit help [query]")
    @Permission("playerkits.command.help")
    @CommandDescription("Shows the help menu")
    private void helpCommand(CommandSender sender, final @Argument("query") @Greedy String query) {
        this.plugin.getPaperCommandService().getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @Command("kits")
    @Permission("playerkits.command.kits")
    @CommandDescription("Open the kits overview")
    public void execute(@NotNull Player player) {
        player.openInventory(this.playerKitService.getKitInventory());
    }

    @Command("kit give <player> <kit>")
    @Permission("playerkits.command.give")
    @CommandDescription("Give a kit to a player")
    public void grantPlayerKit(CommandSender commandSender,
                               @Argument(value = "player") Player player,
                               @Argument(value = "kit", parserName = "playerKit") PlayerKit playerKit) {

        if (notExists(commandSender, playerKit)) return;
        this.playerKitService.kitGrantSuccess(commandSender, player, playerKit);
    }

    @Command("kit delete <name>")
    @Permission("playerkits.command.delete")
    @CommandDescription("Delete a Kit")
    public void deleteKitCommand(@NotNull CommandSender commandSender,
                                 @NotNull @Argument(value = "name", parserName = "playerKit") PlayerKit playerKit) {

        if (notExists(commandSender, playerKit)) return;
        if (this.playerKitService.deleteKit(playerKit)) {

            commandSender.sendMessage(Component.translatable("commands.playerkit.delete.success")
                    .arguments(this.plugin.getPluginPrefix(), Component.text(playerKit.getName())));

        } else {
            commandSender.sendMessage(Component.translatable("commands.playerkit.delete.failure")
                    .arguments(this.plugin.getPluginPrefix(), Component.text(playerKit.getName())));
        }
    }

    private boolean notExists(CommandSender commandSender, PlayerKit playerKit) {

        if (!this.playerKitService.existsPlayerKit(playerKit.getName())) {
            commandSender.sendMessage(Component.translatable("kit.not-found")
                    .arguments(this.plugin.getPluginPrefix(), Component.text(playerKit.getName())));
            return true;
        }

        return false;
    }
}
