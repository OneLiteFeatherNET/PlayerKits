package net.onelitefeather.playerkits.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import org.apache.commons.lang.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("java:S2885")
public final class I18nLocaleService {

    private static final Date NOW_DATE = new Date();
    private static final Date FUTURE_DATE = new Date();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final ResourceBundle messages;
    private final List<Component> kitItemDescription;

    public I18nLocaleService(@NotNull PlayerKitsPlugin playerKitsPlugin) {
        this.messages = ResourceBundle.getBundle("playerkits", new UTF8ResourceBundleControl());

        this.kitItemDescription = new ArrayList<>();
        for (String description : playerKitsPlugin.getConfig().getStringList("gui.item-description")) {
            this.kitItemDescription.add(MiniMessage.miniMessage().deserialize(description));
        }
    }

    /**
     * Translate old color codes to the MiniMessage format.
     *
     * @param text the text to translate
     * @return the translated {@link Component}
     */
    @NotNull
    public Component translateLegacyColorCodes(@NotNull String text) {
        return MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(text)));
    }

    /**
     * @return the resource bundle
     */
    @NotNull
    public ResourceBundle getMessages() {
        return messages;
    }

    /**
     * @param key        the key in the {@link ResourceBundle}
     * @param parameters the placeholders to replace
     * @return the message
     */
    @NotNull
    public String getMessage(@NotNull String key, @Nullable Object... parameters) {
        return this.messages.containsKey(key) ? MessageFormat.format(this.messages.getString(key), parameters) : String.format("N/A (%s)", key);
    }

    @NotNull
    public String getPrefix() {
        return this.messages.getString("prefix");
    }

    @NotNull
    public List<Component> getKitItemDescription() {
        return kitItemDescription;
    }

    /**
     * @param millis the milliseconds
     * @return A Human read-able timestamp.
     */
    @NotNull
    public String formatMillis(long millis) {
        FUTURE_DATE.setTime(millis);
        String outputTimeFormat = this.timeFormat.format(FUTURE_DATE);
        NOW_DATE.setTime(System.currentTimeMillis());
        return DateUtils.isSameDay(NOW_DATE, FUTURE_DATE) ?
                getMessage("time-format", outputTimeFormat) :
                getMessage("datetime-format", outputTimeFormat, dateFormat.format(FUTURE_DATE));
    }
}
