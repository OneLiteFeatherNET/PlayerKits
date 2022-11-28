package net.onelitefeather.playerkits.kit.cooldown;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
@Table
public final class PlayerKitCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "VARCHAR(36)")
    private String playerId;

    @Column
    private long cooldown;

    @Column
    private String kitName;

    public PlayerKitCooldown() {
    }

    public PlayerKitCooldown(@NotNull UUID playerId, long cooldown, String kitName) {
        this.playerId = playerId.toString();
        this.cooldown = cooldown;
        this.kitName = kitName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public UUID getPlayerId() {
        return UUID.fromString(this.playerId);
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId.toString();
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    /**
     * @return true if the cooldown expires.
     */
    public boolean expired() {
        return System.currentTimeMillis() >= this.cooldown;
    }

    @Override
    public String toString() {
        return "PlayerKitCooldown{" +
                "playerId=" + playerId +
                ", cooldown=" + cooldown +
                ", kitName=" + kitName +
                '}';
    }

    public static final class Builder {

        private final String kitName;
        private long cooldown;
        private UUID playerId;

        public Builder(String kitName) {
            this.kitName = kitName;
        }

        public Builder cooldown(long time) {
            this.cooldown = time;
            return this;
        }

        public Builder playerId(@NotNull UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        @NotNull
        public PlayerKitCooldown build() {
            return new PlayerKitCooldown(playerId, cooldown, kitName);
        }


    }
}
