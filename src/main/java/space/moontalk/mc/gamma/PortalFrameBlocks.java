package space.moontalk.mc.gamma;

import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import org.jetbrains.annotations.NotNull;

import lombok.val;

import static space.moontalk.mc.gamma.BlockUtility.*;

public class PortalFrameBlocks extends FrameBlocks {
    public static @NotNull Set<Material> FRAME_MATERIALS = Set.of(
        Material.IRON_BLOCK,
        Material.EMERALD_BLOCK,
        Material.GOLD_BLOCK,
        Material.DIAMOND_BLOCK,
        Material.NETHERITE_BLOCK,
        Material.COPPER_BLOCK,
        Material.EXPOSED_COPPER,
        Material.WEATHERED_COPPER,
        Material.OXIDIZED_COPPER,
        Material.WAXED_COPPER_BLOCK,
        Material.WAXED_EXPOSED_COPPER,
        Material.WAXED_WEATHERED_COPPER,
        Material.WAXED_OXIDIZED_COPPER,
        Material.CUT_COPPER,
        Material.EXPOSED_CUT_COPPER,
        Material.WEATHERED_CUT_COPPER,
        Material.OXIDIZED_CUT_COPPER,
        Material.WAXED_CUT_COPPER,
        Material.WAXED_EXPOSED_CUT_COPPER,
        Material.WAXED_WEATHERED_CUT_COPPER,
        Material.WAXED_OXIDIZED_CUT_COPPER
    );

    public static @NotNull PortalFrameBlocks fromFrame(@NotNull Block source, int maxBlocks) {
        if (!FRAME_MATERIALS.contains(source.getType()))
            throw new IllegalArgumentException("not a frame block");

        for (val axis : Axis.values()) {
            try {
                return fromPortal(incAxis(source, axis), maxBlocks);
            } catch (Exception exception) {}

            try {
                return fromPortal(decAxis(source, axis), maxBlocks);
            } catch (Exception exception) {}
        }

        throw new IllegalArgumentException("not a frame");
    }

    public static @NotNull PortalFrameBlocks fromAir(@NotNull Block source, int maxBlocks) {
        return new PortalFrameBlocks(source, maxBlocks, Material.AIR);
    }

    public static @NotNull PortalFrameBlocks fromPortal(@NotNull Block source, int maxBlocks) {
        return new PortalFrameBlocks(source, maxBlocks, Material.NETHER_PORTAL);
    }

    protected PortalFrameBlocks(@NotNull Block source, int maxBlocks, @NotNull Material innerMaterial) {
        super(source, maxBlocks, FRAME_MATERIALS, innerMaterial);
    }

    public void ignite() {
        fill(Material.NETHER_PORTAL);
    }

    public @NotNull Portal toPortal() {
        return new Portal(getId(), getExit());
    }

    public @NotNull PortalId getId() {
        val center = getCenter();

        return new PortalId(
            center.getWorld().getUID(), 
            center.getBlockX(), 
            center.getBlockY(), 
            center.getBlockZ()
        );
    }

    public @NotNull Location getExit() {
        val center = getCenter();
        val world  = center.getWorld();

        for (double x = center.getX(), y = center.getY(), z = center.getZ(); y >= world.getMinHeight(); --y) {
            val block = world.getBlockAt((int) x, (int) y, (int) z);

            if (FRAME_MATERIALS.contains(block.getType())) {
                if (axis == Axis.X)
                    z += .5;
                else if (axis == Axis.Z)
                    x += .5;

                return new Location(world, x, y + 1, z);
            }
        }

        return center;
    }
}
