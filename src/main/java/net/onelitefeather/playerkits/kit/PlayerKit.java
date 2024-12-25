package net.onelitefeather.playerkits.kit;

import jakarta.persistence.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperties;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "kits")
public final class PlayerKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String items;

    @Column
    private String name;

    @Column(columnDefinition = "TEXT")
    private String displayName;

    @OneToOne
    private PlayerKitProperties properties;

    @NotNull
    public Long getId() {
        return id;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    @NotNull
    public String getItems() {
        return items;
    }

    public void setItems(@NotNull String items) {
        this.items = items;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public Component displayName() {
        return MiniMessage.miniMessage().deserialize(getDisplayName());
    }

    @NotNull
    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public void setProperties(@NotNull PlayerKitProperties properties) {
        this.properties = properties;
    }

    @NotNull
    public PlayerKitProperties getProperties() {
        return properties;
    }

    public boolean isFirstJoin() {
        return this.properties.isFirstJoin();
    }

    public boolean isOneTime() {
        return this.properties.isOneTime();
    }

    public boolean isVisible() {
        return this.properties.isVisible();
    }

    public Long getCooldownTime() {
        return this.properties.getCooldownTime();
    }
}
