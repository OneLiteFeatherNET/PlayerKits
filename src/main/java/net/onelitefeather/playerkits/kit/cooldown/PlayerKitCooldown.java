package net.onelitefeather.playerkits.kit.cooldown;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public final class PlayerKitCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "VARCHAR(36)")
    private String playerId;

    @Column
    private long cooldown;

    @Column
    private long kitId;

    public PlayerKitCooldown() {
    }

    public PlayerKitCooldown(@NotNull UUID playerId, long cooldown, long kitId) {
        this.playerId = playerId.toString();
        this.cooldown = cooldown;
        this.kitId = kitId;
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

    public long getKitId() {
        return kitId;
    }

    public void setKitId(long kitId) {
        this.kitId = kitId;
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
                ", kitId=" + kitId +
                '}';
    }

    public static final class Builder {

        private final long kitId;
        private long cooldown;
        private UUID playerId;

        public Builder(long kitId) {
            this.kitId = kitId;
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
            return new PlayerKitCooldown(playerId, cooldown, kitId);
        }


    }
}
