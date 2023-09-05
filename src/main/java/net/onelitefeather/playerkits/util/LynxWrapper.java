package net.onelitefeather.playerkits.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LynxWrapper implements Translator {

    private final TranslationRegistry translator;

    public LynxWrapper(@NotNull TranslationRegistry translator) {
        this.translator = translator;
    }

    @Override
    public @NotNull Key name() {
        return this.translator.name();
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return this.translator.translate(key, locale);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {

        var miniMessageResult = this.translate(component.key(), locale);
        if (miniMessageResult == null) return null;
        var values = new String[component.args().size()];

        for (int i = 0; i < values.length; i++) {
            values[i] = MiniMessage.miniMessage().serialize(GlobalTranslator.render(component.args().get(i), locale));
        }

        var resultComponent = MiniMessage.miniMessage().deserialize(miniMessageResult.format(values));
        List<Component> children = new ArrayList<>();
        for (Component child : resultComponent.children()) {
            children.add(GlobalTranslator.render(child, locale));
        }

        return GlobalTranslator.render(resultComponent, locale).children(children);
    }
}
