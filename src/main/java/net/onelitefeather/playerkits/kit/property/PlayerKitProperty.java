package net.onelitefeather.playerkits.kit.property;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerKitProperty<T> {

    public static final PlayerKitProperty<String> UNKNOWN = new PlayerKitProperty<>(Type.UNKNOWN, "unknown");
    public static final PlayerKitProperty<Boolean> ONE_TIME = new PlayerKitProperty<>(Type.ONE_TIME, false);
    public static final PlayerKitProperty<Boolean> FIRST_JOIN = new PlayerKitProperty<>(Type.FIRST_JOIN, false);
    public static final PlayerKitProperty<Boolean> VISIBLE = new PlayerKitProperty<>(Type.VISIBLE, true);
    public static final PlayerKitProperty<Double> COOLDOWN_TIME = new PlayerKitProperty<>(Type.COOLDOWN_TIME, -1D);
    public static final PlayerKitProperty<TimeUnit> COOLDOWN_TIME_UNIT = new PlayerKitProperty<>(Type.COOLDOWN_TIME_UNIT, TimeUnit.SECONDS);
    public static final PlayerKitProperty<Double> PRICE = new PlayerKitProperty<>(Type.PRICE, 0.0D);
    public static final PlayerKitProperty<String> DISPLAY_NAME = new PlayerKitProperty<>(Type.DISPLAY_NAME, "");
    public static final List<PlayerKitProperty<?>> DEFAULTS = List.of(ONE_TIME, FIRST_JOIN, COOLDOWN_TIME, COOLDOWN_TIME_UNIT, PRICE, VISIBLE, DISPLAY_NAME);

    @SuppressWarnings("java:S1452")
    @Nullable
    public static PlayerKitProperty<?> getDefaultProperty(@NotNull Type type) {

        PlayerKitProperty<?> kitProperty = null;

        for (int i = 0; i < DEFAULTS.size() && kitProperty == null; i++) {
            var current = DEFAULTS.get(i);
            if (current.getType() == type) {
                kitProperty = current;
            }
        }

        return kitProperty;
    }

    @Expose
    private Type type;

    @Expose
    private T value;

    public PlayerKitProperty(@NotNull Type type, @NotNull T value) {
        this.type = type;
        this.value = value;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public void setType(@NotNull Type type) {
        this.type = type;
    }

    @NotNull
    public T getValue() {
        return value;
    }

    public void setValue(@NotNull T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PlayerKitPropertyEntity{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }

    public enum Type {
        UNKNOWN,
        ONE_TIME,
        FIRST_JOIN,
        VISIBLE,
        COOLDOWN_TIME,
        COOLDOWN_TIME_UNIT,
        PRICE,
        DISPLAY_NAME;
    }
}
