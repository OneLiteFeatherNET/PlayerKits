package net.onelitefeather.playerkits.kit.setup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.kit.PlayerKit;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperties;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class PlayerKitSetup {

    private final String kitName;
    private KitSetupStep currentStep;
    private final Map<Integer, Object> values;

    public PlayerKitSetup(@NotNull String kitName) {
        this.kitName = kitName;
        this.values = new HashMap<>();
    }

    public String getKitName() {
        return kitName;
    }

    @NotNull
    public KitSetupStep getCurrentStep() {
        return currentStep;
    }

    public void setDone(@NotNull Player player, @NotNull KitSetupStep step, @NotNull Object value, @Nullable KitSetupStep nextStep) {
        this.values.putIfAbsent(step.getId(), value);
        if (nextStep != null) {
            setCurrentStep(player, nextStep);
        }
    }

    public void setCurrentStep(@NotNull Player player, @NotNull KitSetupStep currentStep) {
        this.currentStep = currentStep;
        player.sendMessage(Component.translatable("kit.setup.current-step")
                .arguments(TranslationArgument.numeric(currentStep.getId()),
                        Component.text(currentStep.getName()),
                        Component.text(currentStep.getType()),
                        Component.text(currentStep.getDefaultValue().toString())));
    }

    public void previousStep(@NotNull Player player) {
        if(this.currentStep.getId() == 1) return;
        var step = KitSetupStep.getById(this.currentStep.getId() - 1);
        if (step == null) {
            step = currentStep;
        }

        setCurrentStep(player, step);
    }

    @NotNull
    public PlayerKit createKit(@Nullable ItemStack @NotNull[] items) {

        var kit = new PlayerKit();

        kit.setName(kitName);

        var displayNameStep = KitSetupStep.DISPLAY_NAME;
        var oneTimeStep = KitSetupStep.ONE_TIME;
        var firstJoinStep = KitSetupStep.FIRST_JOIN;
        var visibleStep = KitSetupStep.VISIBLE;
        var cooldownTimeStep = KitSetupStep.COOLDOWN_TIME;
        var priceStep = KitSetupStep.PRICE;
        var displayItemStep = KitSetupStep.DISPLAY_ITEM;

        kit.setDisplayName((String) this.values.getOrDefault(displayNameStep.getId(), kitName));
        kit.setContents(ItemStack.serializeItemsAsBytes(items));
        var properties = new PlayerKitProperties();
        properties.setOneTime((Boolean) this.values.getOrDefault(oneTimeStep.getId(), oneTimeStep.getDefaultValue()));
        properties.setFirstJoin((Boolean) this.values.getOrDefault(firstJoinStep.getId(), firstJoinStep.getDefaultValue()));
        properties.setVisible((Boolean) this.values.getOrDefault(visibleStep.getId(), visibleStep.getDefaultValue()));
        properties.setCooldownTime((Long) this.values.getOrDefault(cooldownTimeStep.getId(), cooldownTimeStep.getDefaultValue()));
        properties.setPrice((Double) this.values.getOrDefault(priceStep.getId(), priceStep.getDefaultValue()));
        properties.setDisplayItem((Material) this.values.getOrDefault(displayItemStep.getId(), displayItemStep.getDefaultValue()));
        kit.setProperties(properties);
        return kit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerKitSetup that)) return false;

        if (!kitName.equals(that.kitName)) return false;
        if (currentStep != that.currentStep) return false;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        int result = kitName.hashCode();
        result = 31 * result + (currentStep != null ? currentStep.hashCode() : 0);
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlayerKitSetup{" +
                "kitName='" + kitName + '\'' +
                ", currentStep=" + currentStep +
                ", values=" + values +
                '}';
    }
}
