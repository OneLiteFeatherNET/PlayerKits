package net.onelitefeather.playerkits.service;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.setup.KitSetupStep;
import net.onelitefeather.playerkits.kit.setup.PlayerKitSetup;
import net.onelitefeather.playerkits.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerKitSetupService implements Listener {

    private final PlayerKitsPlugin plugin;
    private final Map<Player, PlayerKitSetup> setupMap;

    public PlayerKitSetupService(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.setupMap = new HashMap<>();
    }

    @EventHandler
    public void handleChat(AsyncChatEvent event) {

        var player = event.getPlayer();
        if (!this.setupMap.containsKey(player)) return;

        var setup = this.setupMap.get(player);
        var text = PlainTextComponentSerializer.plainText().serialize(event.message());

        event.setCancelled(true);

        if (text.equalsIgnoreCase("cancel")) {
            this.setupMap.remove(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<lang:kit.setup.cancelled:'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), setup.getKitName())));
            return;
        } else if (text.equalsIgnoreCase("prev")) {
            setup.previousStep(player);
            return;
        }

        Object value = null;
        String stepName = setup.getCurrentStep().getName();

        switch (setup.getCurrentStep()) {
            case ONE_TIME -> {
                value = Boolean.parseBoolean(text);
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.ONE_TIME, value, KitSetupStep.PRICE);
            }

            case PRICE -> {
                value = StringUtils.isNumeric(text) ? Double.parseDouble(text) : 0.0D;
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.PRICE, value, KitSetupStep.VISIBLE);
            }

            case VISIBLE -> {
                value = Boolean.parseBoolean(text);
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.VISIBLE, value, KitSetupStep.FIRST_JOIN);
            }

            case FIRST_JOIN -> {
                value = Boolean.parseBoolean(text);
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.FIRST_JOIN, value, KitSetupStep.DISPLAY_ITEM);
            }

            case DISPLAY_ITEM -> {
                var material = Material.matchMaterial(text.toUpperCase());
                value = material != null ? material : Material.GRASS_BLOCK;
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.DISPLAY_ITEM, value, KitSetupStep.COOLDOWN_TIME);
            }

            case COOLDOWN_TIME -> {
                value = TimeUtil.toMilliSec(text);
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.COOLDOWN_TIME, value, KitSetupStep.DISPLAY_NAME);
            }

            case DISPLAY_NAME -> {
                value = text;
                stepName = setup.getCurrentStep().getName();
                sendSetupFeedBack(player, value, stepName);
                setup.setDone(player, KitSetupStep.DISPLAY_NAME, value, null);

                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<lang:commands.playerkit.create.success:'%s':'%s'>".formatted(
                                this.plugin.getPluginPrefix(), setup.getKitName())));

                this.plugin.getPlayerKitService().createKit(setup.createKit(player.getInventory().getContents()));
                this.setupMap.remove(player);
            }
        }
    }

    private void sendSetupFeedBack(Player player, Object value, String stepName) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<lang:kit.setup.value-set:'%s':'%s':'%s'>".formatted(this.plugin.getPluginPrefix(), stepName, value)));
    }

    public void addSetup(@NotNull Player player, @NotNull String name) {
        var setup = new PlayerKitSetup(name.toLowerCase());
        setup.setCurrentStep(player, KitSetupStep.ONE_TIME);
        this.setupMap.put(player, setup);
    }
}
