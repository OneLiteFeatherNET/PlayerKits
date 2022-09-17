package net.onelitefeather.playerkits.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Quoted;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.PlayerKitManager;
import net.onelitefeather.playerkits.kit.item.ContainerItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public record KitCommand(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitManager playerKitManager) {

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
        player.openInventory(this.playerKitManager.getKitInventory());
    }

    @SuppressWarnings("java:S107")
    @CommandMethod("playerkit create <name> <display> <inventoryIcon> <price> <visible> <cooldownTimeUnit> <cooldown>")
    @CommandPermission("playerkits.command.create")
    @CommandDescription("Create a new Kit")
    public void createKitCommand(@NotNull Player player,
                                 @NotNull @Argument(value = "name") String name,
                                 @NotNull @Argument(value = "display") @Quoted String display,
                                 @NotNull @Argument(value = "inventoryIcon") Material inventoryIcon,
                                 @NotNull @Argument(value = "price") Double price,
                                 @NotNull @Argument(value = "visible") Boolean visible,
                                 @NotNull @Argument(value = "cooldownTimeUnit") TimeUnit timeUnit,
                                 @NotNull @Argument(value = "cooldown") Long cooldownTime) {

        PlayerKit playerKit = new PlayerKit.PlayerKitBuilder(this.playerKitManager.getLastKitId() + 1, name)
                .withContainerItem(new ContainerItem.ContainerItemBuilder().withMaterial(inventoryIcon).withDisplayName(display).build())
                .withPrice(price)
                .content(player.getInventory().getContents())
                .visible(visible)
                .cooldownTimeUnit(timeUnit)
                .cooldownTime(cooldownTime)
                .build();

        if (this.playerKitManager.createPlayerKit(playerKit)) {
            player.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.create.success", name));
        } else {
            player.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.create.failure", name));
        }
    }

    @CommandMethod("playerkit give <player> <kit>")
    @CommandPermission("playerkits.command.give")
    public void grantPlayerKit(CommandSender commandSender, @Argument(value = "player") Player player, @Argument(value = "kit", parserName = "playerKit") PlayerKit playerKit) {
        this.playerKitManager.handleGrantKit(commandSender, player, playerKit, true);
    }

    @CommandMethod("playerkit delete <name>")
    @CommandPermission("playerkits.command.delete")
    @CommandDescription("Delete a Kit")
    public void deleteKitCommand(@NotNull CommandSender commandSender,
                                 @NotNull @Argument(value = "name", parserName = "playerKit") PlayerKit playerKit) {

        String name = playerKit.getName();
        if (this.playerKitManager.deleteKit(playerKit)) {
            commandSender.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.delete.success", name));
        } else {
            commandSender.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.delete.failure", name));
        }
    }

    @Parser(name = "playerKit", suggestions = "playerKits")
    public @NotNull PlayerKit parsePlayerKit(CommandContext<CommandSender> commandSender, @NotNull Queue<String> input) {

        var name = input.remove();
        var playerKit = this.playerKitManager.getPlayerKit(name);

        if(playerKit == null) {
            playerKit = new PlayerKit(99, name, new ContainerItem(Material.AIR, "dummy"), "", 0, false, TimeUnit.DAYS, -1);
        }

        return playerKit;
    }

    @NotNull
    @Suggestions("playerKits")
    public List<String> getKitNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        return this.playerKitManager.getPlayerKitNames();
    }
}
