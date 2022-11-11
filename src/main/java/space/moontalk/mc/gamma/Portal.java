package space.moontalk.mc.gamma;

import java.io.Serializable;

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

    public void teleport(@NotNull Entity entity) {
        val location = entity.getLocation();
        val exit     = this.exit.clone();

        exit.setPitch(location.getPitch());
        exit.setYaw(location.getYaw());

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
