package net.onelitefeather.playerkits.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.item.ContainerItem;
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
public final class MessagesManager {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final ResourceBundle messages;
    private final List<Component> kitItemDescription;

    public MessagesManager(@NotNull PlayerKitsPlugin playerKitsPlugin) {
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
    public Component getMessageComponent(@NotNull String key, @Nullable Object... parameters) {
        return MiniMessage.miniMessage().deserialize(getMessage(key, parameters));
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

    /**
     * @return the lore for the {@link ContainerItem#toItemStack()}
     */
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
        Date date = new Date(millis);
        String outputTimeFormat = this.timeFormat.format(date);
        return DateUtils.isSameDay(new Date(), date) ? outputTimeFormat : outputTimeFormat + dateFormat.format(date);
    }
}
