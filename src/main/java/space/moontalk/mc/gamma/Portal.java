package space.moontalk.mc.gamma;

import java.io.Serializable;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@Getter
@AllArgsConstructor
public class Portal implements Serializable {
    private final @NotNull PortalId id;
    private final @NotNull Location exit;
    private final @NotNull Axis     axis;

    public void teleport(@NotNull Portal from, @NotNull Entity entity) {
        val location = entity.getLocation();
        val exit     = this.exit.clone();

        if (axis != from.axis)
            entity.setVelocity(entity.getVelocity().rotateAroundY(Math.PI / 2));

        exit.setPitch(location.getPitch());
        exit.setYaw(location.getYaw());

        val height = entity.getHeight();

        if (height < 2)
            exit.setY(exit.getY() + 2 - height);

        entity.teleport(exit);
        entity.setPortalCooldown(20);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == null)
            return false;

        if (!getClass().equals(object.getClass()))
            return false;

        val portal = (Portal) object;

        return id.equals(portal.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
