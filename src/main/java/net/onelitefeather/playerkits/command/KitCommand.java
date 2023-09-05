package net.onelitefeather.playerkits.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record KitCommand(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitService playerKitService) {

    @CommandMethod("kit create <name>")
    @CommandPermission("playerkits.command.kit.create")
    @CommandDescription("Create a new kit")
    public void createKitCommand(Player player, @Argument("name") String name) {

        if (this.playerKitService.existsPlayerKit(name)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:kit.already.exist:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), name)));
            return;
        }

        this.plugin.getKitSetupService().addSetup(player, name);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:kit.setup.help.cancel:'%s'>".formatted(this.plugin.getPluginPrefix())));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:kit.setup.help.previous-step:'%s'>".formatted(this.plugin.getPluginPrefix())));
    }

    @CommandDescription("Shows the help menu")
    @CommandMethod("kit help [query]")
    @CommandPermission("playerkits.command.help")
    private void helpCommand(CommandSender sender, final @Argument("query") @Greedy String query) {
        this.plugin.getPaperCommandService().getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandMethod("kits")
    @CommandDescription("Open the kits overview")
    @CommandPermission("playerkits.command.kits")
    public void execute(@NotNull Player player) {
        player.openInventory(this.playerKitService.getKitInventory());
    }

    @CommandMethod("kit give <player> <kit>")
    @CommandPermission("playerkits.command.give")
    public void grantPlayerKit(CommandSender commandSender,
                               @Argument(value = "player") Player player,
                               @Argument(value = "kit", parserName = "playerKit") PlayerKit playerKit) {

        if (notExists(commandSender, playerKit)) return;
        this.playerKitService.handleGrantKit(commandSender, player, playerKit);
    }

    @CommandMethod("kit delete <name>")
    @CommandPermission("playerkits.command.delete")
    @CommandDescription("Delete a Kit")
    public void deleteKitCommand(@NotNull CommandSender commandSender,
                                 @NotNull @Argument(value = "name", parserName = "playerKit") PlayerKit playerKit) {

        if (notExists(commandSender, playerKit)) return;
        if (this.playerKitService.deleteKit(playerKit)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<lang:commands.playerkit.delete.success:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), playerKit.getName())));
        } else {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<lang:commands.playerkit.delete.failure:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), playerKit.getName())));
        }
    }

    private boolean notExists(CommandSender commandSender, PlayerKit playerKit) {

        if (!this.playerKitService.existsPlayerKit(playerKit.getName())) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<lang:kit.not-found:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), playerKit.getName())));
            return true;
        }

        return false;
    }
}
