package net.onelitefeather.playerkits.kit.property;

import jakarta.persistence.*;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Entity
@Table(name = "kit_properties")
public class PlayerKitProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = null;

    @Column
    private Boolean oneTime = false;

    @Column
    private Boolean firstJoin = false;

    @Column
    private Boolean visible = false;

    @Column
    private Long cooldownTime = null;

    @Column
    private Double price = null;

    @Enumerated(EnumType.STRING)
    private Material displayItem = null;

    @NotNull
    public Long getId() {
        return id;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public Boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(Boolean oneTime) {
        this.oneTime = oneTime;
    }

    public Boolean isFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(Boolean firstJoin) {
        this.firstJoin = firstJoin;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @NotNull
    public Long getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(@NotNull Long cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    @NotNull
    public Double getPrice() {
        return price;
    }

    public void setPrice(@NotNull Double price) {
        this.price = price;
    }

    @NotNull
    public Material getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(@NotNull Material displayItem) {
        this.displayItem = displayItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerKitProperties that)) return false;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(oneTime, that.oneTime)) return false;
        if (!Objects.equals(firstJoin, that.firstJoin)) return false;
        if (!Objects.equals(visible, that.visible)) return false;
        if (!Objects.equals(cooldownTime, that.cooldownTime)) return false;
        if (!Objects.equals(price, that.price)) return false;
        return displayItem == that.displayItem;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (oneTime != null ? oneTime.hashCode() : 0);
        result = 31 * result + (firstJoin != null ? firstJoin.hashCode() : 0);
        result = 31 * result + (visible != null ? visible.hashCode() : 0);
        result = 31 * result + (cooldownTime != null ? cooldownTime.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (displayItem != null ? displayItem.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayerKitProperties{" +
                "id=" + id +
                ", oneTime=" + oneTime +
                ", firstJoin=" + firstJoin +
                ", visible=" + visible +
                ", cooldownTime=" + cooldownTime +
                ", price=" + price +
                ", displayItem=" + displayItem +
                '}';
    }
}
