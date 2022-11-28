package net.onelitefeather.playerkits.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.specifier.Quoted;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperty;
import net.onelitefeather.playerkits.kit.setup.PlayerKitSetup;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class KitSetupCommand {

    private final PlayerKitsPlugin plugin;
    private static final TimeUnit[] TIME_UNITS = TimeUnit.values();

    public KitSetupCommand(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("playerkits create setup <name>")
    @CommandPermission("playerkits.command.setup")
    public void executeSetupCreateCommand(@NotNull CommandSender commandSender, @NotNull @Argument(value = "name") String name) {

        var setup = plugin.getPlayerKitSetupService().getSetup(name);
        if (setup != null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    this.plugin.i18n().getMessage("commands.playerkit.setup.already-in-setup", this.plugin.i18n().getPrefix())));
            return;
        }

        plugin.getPlayerKitSetupService().addSetup(new PlayerKitSetup(name));
        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                this.plugin.i18n().getMessage("commands.playerkit.setup.help", this.plugin.i18n().getPrefix(), name)));
    }

    @CommandMethod("playerkits setup <name> setitems")
    @CommandPermission("playerkits.command.setup")
    public void setItemsCommand(@NotNull Player commandSender,
                                @Argument(value = "name", suggestions = "setupNames") String name) {

        var setup = plugin.getPlayerKitSetupService().getSetup(name);
        if (isSetupAble(commandSender, setup)) return;
        assert setup != null;

        setup.setContent(commandSender.getInventory().getContents());
        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                this.plugin.i18n().getMessage("commands.playerkit.setup.items-set", this.plugin.i18n().getPrefix(), name)));

        handleSetupFinish(commandSender, setup);
    }

    @CommandMethod("playerkits setup finish <name>")
    @CommandPermission("playerkits.command.setup")
    public void executeSetupFinish(@NotNull CommandSender commandSender,
                                   @NotNull @Argument(value = "name", suggestions = "setupNames") String name) {

        var setup = plugin.getPlayerKitSetupService().getSetup(name);
        if(setup == null) {

            return;
        }

        handleSetupFinish(commandSender, setup);
    }

    @CommandMethod("playerkits setup <name> <property> <value>")
    @CommandPermission("playerkits.command.setup")
    public void executeCommand(@NotNull CommandSender commandSender,
                               @NotNull @Argument(value = "name", suggestions = "setupNames") String name,
                               @NotNull @Argument(value = "property", parserName = "kit_properties") PlayerKitProperty<?> property,
                               @NotNull @Argument(value = "value", suggestions = "kit_properties_values") @Quoted String value) {

        var setup = plugin.getPlayerKitSetupService().getSetup(name);
        var valueIsNumber = StringUtils.isNumeric(value);

        if (isSetupAble(commandSender, setup)) return;
        assert setup != null;
        PlayerKitProperty<?> kitProperty = null;
        var type = property.getType();

        switch (type) {
            case PRICE ->
                    kitProperty = setup.addPropertyValue(new PlayerKitProperty<>(type, valueIsNumber ? Double.parseDouble(value) : 0.0D));

            case VISIBLE, FIRST_JOIN, ONE_TIME ->
                    kitProperty = setup.addPropertyValue(new PlayerKitProperty<>(type, Boolean.parseBoolean(value)));
            case DISPLAY_NAME -> kitProperty = setup.addPropertyValue(new PlayerKitProperty<>(type, value));
            case COOLDOWN_TIME ->
                    kitProperty = setup.addPropertyValue(new PlayerKitProperty<>(type, valueIsNumber ? Long.parseLong(value) : -1L));
            case COOLDOWN_TIME_UNIT ->
                    kitProperty = setup.addPropertyValue(new PlayerKitProperty<>(type, TimeUnit.valueOf(value.toUpperCase())));
            default ->
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                            this.plugin.i18n().getMessage("commands.playerkit.setup.type-not-supported", this.plugin.i18n().getPrefix())));
        }

        if (kitProperty != null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    this.plugin.i18n().getMessage("commands.playerkit.setup.property-set",
                            this.plugin.i18n().getPrefix(), type.name().toLowerCase(), kitProperty.getValue().toString())));
        }

        handleSetupFinish(commandSender, setup);
    }

    private boolean isSetupAble(@NotNull CommandSender commandSender, @Nullable PlayerKitSetup setup) {

        if (setup == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    this.plugin.i18n().getMessage("commands.playerkit.setup.no-setup-found", this.plugin.i18n().getPrefix())));
            return true;
        }

        return false;
    }

    private void handleSetupFinish(CommandSender commandSender, PlayerKitSetup setup) {
        if (setup.isDone()) {
            var playerKit = setup.createKit();
            this.plugin.getPlayerKitSetupService().removeSetup(setup, playerKit);

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(
                    this.plugin.i18n().getMessage("commands.playerkit.create.success",
                            this.plugin.i18n().getPrefix(), playerKit.getName())));
        }
    }

    @Suggestions("setupNames")
    public List<String> getSetupNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        return this.plugin.getPlayerKitSetupService().getPlayerKitSetupList().stream().map(PlayerKitSetup::getName).toList();
    }

    @SuppressWarnings("java:S1452")
    @Parser(name = "kit_properties", suggestions = "kit_properties_names")
    public @NotNull PlayerKitProperty<?> parsePlayerKit(CommandContext<CommandSender> commandSender, @NotNull Queue<String> input) {
        var name = input.remove();

        var defaults = PlayerKitProperty.DEFAULTS;
        PlayerKitProperty<?> property = null;
        for (int i = 0; i < defaults.size() && property == null; i++) {
            PlayerKitProperty<?> kitProperty = defaults.get(i);
            if (name.equalsIgnoreCase(kitProperty.getType().name())) {
                property = kitProperty;
            }
        }

        return property != null ? property : PlayerKitProperty.UNKNOWN;
    }

    @NotNull
    @Suggestions("kit_properties_values")
    public List<String> getPropertyValues(@NotNull CommandContext<CommandSender> context, @NotNull String input) {

        if (!context.contains("property")) return Collections.emptyList();
        PlayerKitProperty<?> property = context.get("property");
        if (property.getValue() instanceof Boolean) {
            return List.of("false", "true");
        }

        if (property.getValue() instanceof TimeUnit) {

            List<String> list = new ArrayList<>();
            for (TimeUnit timeUnit : TIME_UNITS) {
                list.add(timeUnit.name().toLowerCase());
            }

            return list;
        }
        return Collections.emptyList();
    }


    @NotNull
    @Suggestions("kit_properties_names")
    public List<String> getPropertyNames(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        return PlayerKitProperty.DEFAULTS.stream().map(property -> property.getType().name()).toList();
    }
}
