package net.onelitefeather.playerkits.kit;

import jakarta.persistence.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.playerkits.kit.property.PlayerKitProperties;
import net.onelitefeather.playerkits.util.InventoryUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "kits")
public final class PlayerKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "kit_content", columnDefinition = "longblob")
    private byte[] contents;

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

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public byte[] getContents() {
        return contents;
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
