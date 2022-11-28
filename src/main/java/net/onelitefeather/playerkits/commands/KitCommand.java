package net.onelitefeather.playerkits.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.service.PlayerKitService;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperty;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;

public record KitCommand(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitService playerKitService) {

    private static final String DUMMY_ITEMS = InventoryUtil.serializeInventoryToString(new ItemStack[]{new ItemStack(Material.STONE)});

    @CommandDescription("Shows the help menu")
    @CommandMethod("playerkits help [query]")
    @CommandPermission("orion.command.help")
    private void helpCommand(CommandSender sender, final @Argument("query") @Greedy String query) {
        this.plugin.getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandMethod("kits")
    @CommandDescription("Open the kits overview")
    @CommandPermission("playerkits.command.kits")
    public void execute(@NotNull Player player) {
        player.openInventory(this.playerKitService.getKitInventory());
    }

    @CommandMethod("playerkit give <player> <kit>")
    @CommandPermission("playerkits.command.give")
    public void grantPlayerKit(CommandSender commandSender, @Argument(value = "player") Player player, @Argument(value = "kit", parserName = "playerKit") PlayerKit playerKit) {

        if (!this.playerKitService.existsPlayerKit(playerKit.getName())) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.plugin.i18n().getMessage("kit.not-found",
                    this.plugin.i18n().getPrefix(), playerKit.getName())));
            return;
        }

        this.playerKitService.handleGrantKit(commandSender, player, playerKit, true);
    }

    @CommandMethod("playerkit delete <name>")
    @CommandPermission("playerkits.command.delete")
    @CommandDescription("Delete a Kit")
    public void deleteKitCommand(@NotNull CommandSender commandSender,
                                 @NotNull @Argument(value = "name", parserName = "playerKit") PlayerKit playerKit) {

        String name = playerKit.getName();
        if (this.playerKitService.deleteKit(playerKit)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    this.plugin.i18n().getMessage("commands.playerkit.delete.success", this.plugin.i18n().getPrefix(), name)));
        } else {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.plugin.i18n().getMessage("commands.playerkit.delete.failure", this.plugin.i18n().getPrefix(), name)));
        }
    }

    @Parser(name = "playerKit", suggestions = "playerKits")
    public @NotNull PlayerKit parsePlayerKit(CommandContext<CommandSender> commandSender, @NotNull Queue<String> input) {

        var name = input.remove();
        var playerKit = this.playerKitService.getPlayerKit(name);

        if (playerKit == null) {
            playerKit = new PlayerKit(name, DUMMY_ITEMS, PlayerKitProperty.DEFAULTS);
        }

        return playerKit;
    }

    @NotNull
    @Suggestions("playerKits")
    public List<String> getKitNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        return this.playerKitService.getPlayerKitNames();
    }
}
