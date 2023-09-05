package net.onelitefeather.playerkits.kit.setup;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum KitSetupStep {

    ONE_TIME(1, "One Time", false),
    PRICE(2, "Price", 0.0D),
    VISIBLE(3, "Visible", true),
    FIRST_JOIN(4, "First Join", false),
    DISPLAY_ITEM(5, "Display Item", Material.IRON_SWORD),
    COOLDOWN_TIME(6, "Cooldown Time", -1L),
    DISPLAY_NAME(7, "Display Name", "Kit");

    private final Integer id;
    private final String name;
    private final Object defaultValue;

    private static final KitSetupStep[] VALUES = values();

    KitSetupStep(@NotNull Integer id, @NotNull String name, @NotNull Object defaultValue) {
        this.id = id;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Nullable
    public static KitSetupStep getById(@NotNull Integer id) {

        KitSetupStep step = null;

        for (int i = 0; i < VALUES.length && step == null; i++) {
            var current = VALUES[i];
            if (current.getId().equals(id)) {
                step = current;
            }
        }

        return step;
    }

    @NotNull
    public Object getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public Integer getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getType() {
        return this.defaultValue.getClass().getSimpleName().toUpperCase();
    }
}
