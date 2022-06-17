package net.onelitefeather.playerkits.kit.cooldown;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerKitCooldown(@NotNull UUID playerId, long cooldown, long kitId) {

    /**
     *
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
