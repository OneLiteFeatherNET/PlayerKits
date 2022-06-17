package net.onelitefeather.playerkits.util;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class TimeUtil {

    private static final Calendar TO_CALENDAR = Calendar.getInstance();
    private static final Calendar FROM_CALENDAR = Calendar.getInstance();

    public static long getCooldownTime(@NotNull TimeUnit timeUnit, long time) {
        return System.currentTimeMillis() + switch (timeUnit) {
            case DAYS -> (1000 * 60 * 60 * 24) * time;
            case HOURS -> (1000 * 60 * 60) * time;
            case MINUTES -> (1000 * 60) * time;
            case SECONDS -> 1000 * time;
            default ->
                    throw new IllegalStateException("The TimeUnit " + timeUnit.name().toLowerCase() + " is not allowed here");
        };
    }

    public static boolean isSameDay(@NotNull Date start, @NotNull Date end) {
        TO_CALENDAR.setTime(end);
        FROM_CALENDAR.setTime(start);
        return TO_CALENDAR.get(Calendar.MONTH) == FROM_CALENDAR.get(Calendar.MONTH) && TO_CALENDAR.get(Calendar.DAY_OF_MONTH) == FROM_CALENDAR.get(Calendar.DAY_OF_MONTH);
    }
}
