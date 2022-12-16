package net.onelitefeather.playerkits.util;

import net.onelitefeather.playerkits.kit.PlayerKit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class TimeUtil {

    public static final long NO_COOLDOWN = -1;

    private TimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static long getCooldownTime(@NotNull PlayerKit playerKit) {
        return getCooldownTime(playerKit.getTimeUnit(), playerKit.getCooldownTime());
    }

    public static long getCooldownTime(@NotNull TimeUnit timeUnit, long time) {
        if (time == NO_COOLDOWN) return time;
        return System.currentTimeMillis() + switch (timeUnit) {
            case DAYS -> (1000 * 60 * 60 * 24) * time;
            case HOURS -> (1000 * 60 * 60) * time;
            case MINUTES -> (1000 * 60) * time;
            case SECONDS -> 1000 * time;
            default ->
                    throw new IllegalStateException("The TimeUnit " + timeUnit.name().toLowerCase() + " is not allowed here");
        };
    }
}
