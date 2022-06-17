package net.onelitefeather.playerkits.kit.cooldown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.onelitefeather.playerkits.PlayerKitsPlugin;
import net.onelitefeather.playerkits.kit.PlayerKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PlayerKitCooldownManager {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final static String FILE_NAME = "kitsCooldown.json";
    private final PlayerKitsPlugin plugin;
    private final List<PlayerKitCooldown> playerKitCooldowns;
    private final File file;

    public PlayerKitCooldownManager(@NotNull PlayerKitsPlugin plugin) {
        this.plugin = plugin;
        this.playerKitCooldowns = new ArrayList<>();

        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!this.file.exists()) {
            try {
                Files.createFile(this.file.toPath());
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not create File", e);
            }
        }

        load(this.playerKitCooldowns::addAll);
    }

    /**
     * @return a List of all {@link PlayerKitCooldown}
     */
    public List<PlayerKitCooldown> getPlayerKitCooldowns() {
        return playerKitCooldowns;
    }

    /**
     * Load all {@link PlayerKitCooldown} from the file
     *
     * @param consumer the consumer
     */
    public void load(@NotNull Consumer<List<PlayerKitCooldown>> consumer) {

        if (this.file.exists()) {

            List<PlayerKitCooldown> kitCooldowns = new ArrayList<>();

            try (BufferedReader bufferedReader = Files.newBufferedReader(this.file.toPath())) {

                PlayerKitCooldown[] data = GSON.fromJson(bufferedReader, PlayerKitCooldown[].class);
                if (data != null && data.length > 0) {
                    kitCooldowns.addAll(List.of(data));
                }

            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Cannot load cooldowns!", e);
            }

            consumer.accept(kitCooldowns);
        }
    }

    /**
     * Add a {@link PlayerKitCooldown}
     *
     * @param playerKitCooldown the cooldown
     */
    public void addCooldown(@NotNull PlayerKitCooldown playerKitCooldown) {

        this.playerKitCooldowns.add(playerKitCooldown);
        if (!this.file.exists()) return;
        updateKitCooldowns();
    }

    /**
     * Remove a {@link PlayerKitCooldown}
     *
     * @param playerId the playerId
     * @param kitId    the kitId
     */
    public void removeCooldown(@NotNull UUID playerId, long kitId) {

        PlayerKitCooldown playerKitCooldown = getPlayerKitCooldown(playerId, kitId);
        if (playerKitCooldown == null) return;
        if (!playerKitCooldown.expired()) return;

        this.playerKitCooldowns.remove(playerKitCooldown);

        if (!this.file.exists()) return;
        updateKitCooldowns();
    }

    /**
     * @param playerId the player who claimed the {@link PlayerKit}
     * @param kitId    the id of the {@link PlayerKit}
     * @return the {@link PlayerKit} by the given playerId and kitId
     */
    @Nullable
    public PlayerKitCooldown getPlayerKitCooldown(@NotNull UUID playerId, long kitId) {

        PlayerKitCooldown playerKitCooldown = null;
        List<PlayerKitCooldown> kitCooldowns = this.getPlayerKitCooldowns();

        for (int i = 0; i < kitCooldowns.size() && playerKitCooldown == null; i++) {
            PlayerKitCooldown kitCooldown = kitCooldowns.get(i);
            if (kitCooldown.playerId().equals(playerId) && kitCooldown.kitId() == kitId) {
                playerKitCooldown = kitCooldown;
            }
        }

        return playerKitCooldown;
    }

    /**
     * @param kitId the id of the {@link PlayerKit}
     * @return the {@link PlayerKit} by the given kitId
     */
    @Nullable
    public PlayerKitCooldown getPlayerKitCooldown(long kitId) {

        PlayerKitCooldown playerKitCooldown = null;

        List<PlayerKitCooldown> kitCooldowns = this.getPlayerKitCooldowns();
        for (int i = 0; i < kitCooldowns.size() && playerKitCooldown == null; i++) {
            PlayerKitCooldown kitCooldown = kitCooldowns.get(i);
            if (kitCooldown.kitId() == kitId) {
                playerKitCooldown = kitCooldown;
            }
        }

        return playerKitCooldown;
    }

    /**
     * Update the {@link PlayerKitCooldown} file.
     */
    private void updateKitCooldowns() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.file.toPath())) {
            bufferedWriter.write(GSON.toJson(this.playerKitCooldowns));
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not update kit cooldowns", e);
        }
    }
}
