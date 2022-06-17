package net.onelitefeather.playerkits.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
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
import java.util.concurrent.TimeUnit;

public record KitCommand(@NotNull PlayerKitsPlugin plugin, @NotNull PlayerKitManager playerKitManager) {

    @CommandMethod("kits")
    @CommandDescription("Open the kits overview")
    @CommandPermission("playerkits.command.kits")
    public void execute(@NotNull Player player) {
        player.openInventory(this.playerKitManager.getKitInventory());
    }

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

    @CommandMethod("playerkit delete <name>")
    @CommandPermission("playerkits.command.delete")
    @CommandDescription("Delete a Kit")
    public void deleteKitCommand(@NotNull CommandSender commandSender,
                                 @NotNull @Argument(value = "name", suggestions = "kitNames") String name) {
        if (this.playerKitManager.deleteKit(name)) {
            commandSender.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.delete.success", name));
        } else {
            commandSender.sendMessage(this.plugin.getMessagesManager().getMessageComponent("commands.playerkit.delete.failure", name));
        }
    }

    @NotNull
    @Suggestions("kitNames")
    public List<String> getKitNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        return this.playerKitManager.getPlayerKitNames();
    }
}
