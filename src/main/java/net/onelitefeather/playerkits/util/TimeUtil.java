package net.onelitefeather.playerkits.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.onelitefeather.playerkits.service.ClaimedKitService;
import org.jetbrains.annotations.NotNull;

public final class TimeUtil {

    private static final String VALID_TIME_PATTERN = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

    private TimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param end the end time
     * @return A Human read-able timestamp.
     */
    @NotNull
    public static Component getRemainingTime(long end) {

        long diff = ceilDiv(end - System.currentTimeMillis(), 1000L);
        if (diff > 60 * 60 * 24) {
            return Component.translatable("remaining-time-days")
                    .arguments(TranslationArgument.numeric(diff / 60 / 60 / 24),
                            TranslationArgument.numeric(diff / 60 / 60 % 24),
                            TranslationArgument.numeric(diff / 60 % 60),
                            TranslationArgument.numeric(diff % 60));
        } else if (diff > 60 * 60) {
            return Component.translatable("remaining-time-hours")
                    .arguments(TranslationArgument.numeric(diff / 60 / 60),
                            TranslationArgument.numeric(diff / 60 % 60),
                            TranslationArgument.numeric(diff % 60));
        } else if (diff > 60) {
            return Component.translatable("remaining-time-minutes")
                    .arguments(TranslationArgument.numeric(diff / 60),
                            TranslationArgument.numeric(diff % 60));
        } else {
            return Component.translatable("remaining-time-seconds").arguments(TranslationArgument.numeric(diff));
        }
    }

    public static long ceilDiv(long x, long y) {
        return -Math.floorDiv(-x, y);
    }

    /**
     * Convert a Time String to the amount of milliseconds.
     * Credits: <a href="http://stackoverflow.com/a/8270824">...</a>
     *
     * @param s the time string
     * @return the amount of milliseconds equivalent to the given string
     */
    public static long toMilliSec(@NotNull String s) {
        if (s.equalsIgnoreCase(ClaimedKitService.IGNORE_COOLDOWN.toString())) return ClaimedKitService.IGNORE_COOLDOWN;
        String[] sl = s.toLowerCase().split(VALID_TIME_PATTERN);
        long i = Long.parseLong(sl[0]);
        return switch (sl[1]) {
            case "s" -> i * 1000;
            case "m" -> i * 1000 * 60;
            case "h" -> i * 1000 * 60 * 60;
            case "d" -> i * 1000 * 60 * 60 * 24;
            case "w" -> i * 1000 * 60 * 60 * 24 * 7;
            case "mo" -> i * 1000 * 60 * 60 * 24 * 30;
            default -> ClaimedKitService.IGNORE_COOLDOWN;
        };
    }
}
