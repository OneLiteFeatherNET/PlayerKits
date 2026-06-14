package net.onelitefeather.playerkits.kit;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "claimed_kits")
public class ClaimedKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(36)")
    private String claimedBy;

    @Column
    private Long kitId;

    @Column
    private Boolean firstJoin;

    @Column
    private Boolean oneTime;

    @Column
    private Long claimedAt;

    @Column
    private Long cooldown;

    public ClaimedKit() {
    }

    public ClaimedKit(@Nullable Long id,
                      @NotNull String claimedBy,
                      @NotNull Long kitId,
                      @NotNull Boolean firstJoin,
                      @NotNull Boolean oneTime,
                      @NotNull Long claimedAt,
                      @NotNull Long cooldown) {
        this.id = id;
        this.claimedBy = claimedBy;
        this.kitId = kitId;
        this.firstJoin = firstJoin;
        this.oneTime = oneTime;
        this.claimedAt = claimedAt;
        this.cooldown = cooldown;
    }

    @NotNull
    public Long getId() {
        return id;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    @NotNull
    public UUID getClaimedUniqueId() {
        return UUID.fromString(this.claimedBy);
    }

    public void setClaimedByUniqueId(@NotNull UUID claimedUniqueId) {
        this.claimedBy = claimedUniqueId.toString();
    }

    @NotNull
    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(@NotNull String claimedBy) {
        this.claimedBy = claimedBy;
    }

    @NotNull
    public Long getKitId() {
        return kitId;
    }

    public void setKitId(@NotNull Long kitId) {
        this.kitId = kitId;
    }

    @NotNull
    public Boolean getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(@NotNull Boolean firstJoin) {
        this.firstJoin = firstJoin;
    }

    @NotNull
    public Boolean getOneTime() {
        return oneTime;
    }

    public void setOneTime(@NotNull Boolean oneTime) {
        this.oneTime = oneTime;
    }

    @NotNull
    public Long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(@NotNull Long claimedAt) {
        this.claimedAt = claimedAt;
    }


    @NotNull
    public Long getCooldown() {
        return cooldown;
    }

    public void setCooldown(@NotNull Long cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public String toString() {
        return "ClaimedKit{" +
                "id=" + id +
                ", claimedBy='" + claimedBy + '\'' +
                ", kitName='" + kitId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimedKit that)) return false;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(claimedBy, that.claimedBy)) return false;
        if (!Objects.equals(kitId, that.kitId)) return false;
        if (!Objects.equals(firstJoin, that.firstJoin)) return false;
        if (!Objects.equals(oneTime, that.oneTime)) return false;
        if (!Objects.equals(claimedAt, that.claimedAt)) return false;
        return Objects.equals(cooldown, that.cooldown);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (claimedBy != null ? claimedBy.hashCode() : 0);
        result = 31 * result + (kitId != null ? kitId.hashCode() : 0);
        result = 31 * result + (firstJoin != null ? firstJoin.hashCode() : 0);
        result = 31 * result + (oneTime != null ? oneTime.hashCode() : 0);
        result = 31 * result + (claimedAt != null ? claimedAt.hashCode() : 0);
        result = 31 * result + (cooldown != null ? cooldown.hashCode() : 0);
        return result;
    }
}
