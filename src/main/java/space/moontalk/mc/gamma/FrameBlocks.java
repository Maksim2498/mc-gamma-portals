package space.moontalk.mc.gamma;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.val;

import static space.moontalk.mc.gamma.BlockUtility.*;

@Getter
public class FrameBlocks {
    protected final @NotNull Set<Block> innerBlocks;
    protected final @NotNull Set<Block> frameBlocks;
    protected final @NotNull Axis       axis;

    public FrameBlocks(@NotNull Block source, int maxBlocks, @NotNull Set<Material> frameMatrials) {
        this(source, maxBlocks, frameMatrials, Material.AIR);
    }

    public FrameBlocks(@NotNull Block source, int maxBlocks, @NotNull Set<Material> frameMatrials, @NotNull Material innerMaterial) {
        this(source, maxBlocks, frameMatrials, Set.of(innerMaterial));
    }

    public FrameBlocks(
        @NotNull Block         source,
                 int           maxBlocks,
        @NotNull Set<Material> frameMatrials,
        @NotNull Set<Material> innerMaterials) {
        if (maxBlocks < 0)
            throw new IllegalArgumentException("maxBlocks has to be positive");

        innerBlocks = new HashSet<>();
        frameBlocks = new HashSet<>();

        for (val axis : Set.of(Axis.X, Axis.Z)) 
            if (tryInit(source, maxBlocks, axis, frameMatrials, innerMaterials)) {
                if (innerBlocks.isEmpty()) {
                    frameBlocks.clear();
                    continue;
                }

                this.axis = axis;

                return;
            }

        throw new RuntimeException("not a frame");
    }

    private boolean tryInit(
        @NotNull Block         source,
                 int           maxBlocks,
        @NotNull Axis          axis,
        @NotNull Set<Material> frameMatrials,
        @NotNull Set<Material> innerMaterials) {
        if (innerBlocks.size() >= maxBlocks) {
            innerBlocks.clear();
            frameBlocks.clear();
            return false;
        }

        val type = source.getType();

        if (frameMatrials.contains(type)) {
            frameBlocks.add(source);
            return true;
        }

        if (innerMaterials.contains(type)) {
            if (!innerBlocks.add(source))
                return true;

            return tryInit(incAxis(source, axis), maxBlocks, axis, frameMatrials, innerMaterials)
                && tryInit(decAxis(source, axis), maxBlocks, axis, frameMatrials, innerMaterials)
                && tryInit(   incY(source),       maxBlocks, axis, frameMatrials, innerMaterials)
                && tryInit(   decY(source),       maxBlocks, axis, frameMatrials, innerMaterials);
        }

        innerBlocks.clear();
        frameBlocks.clear();

        return false;
    }

    protected FrameBlocks(@NotNull Set<Block> innerBlocks, @NotNull Set<Block> frameBlocks, @NotNull Axis axis) {
        this.innerBlocks = innerBlocks;
        this.frameBlocks = frameBlocks;
        this.axis        = axis;
    }

    public void fill(@NotNull Material material) {
        for (val block : getInnerBlocks()) {
            block.setType(material);

            if (block.getBlockData() instanceof Orientable orientable) {
                orientable.setAxis(getAxis());
                block.setBlockData(orientable);
            }
        }
    }

    public @NotNull Location getCenter() {
        val frameBlocks = getFrameBlocks();
        val world       = frameBlocks.stream().findAny().get().getWorld(); 

        double x = 0;
        double y = 0;
        double z = 0;

        for (val block : innerBlocks) {
            x += block.getX();
            y += block.getY();
            z += block.getZ();
        }

        x /= innerBlocks.size();
        y /= innerBlocks.size();
        z /= innerBlocks.size();

        return new Location(world, x, y, z);
    }

    public @NotNull Set<Sign> findSigns() {
        val signs = new HashSet<Sign>();

        for (val block : getFrameBlocks()) 
            for (val axis : Axis.values()) {
                if (incAxis(block, axis).getState() instanceof Sign sign)
                    signs.add(sign);

                if (decAxis(block, axis).getState() instanceof Sign sign)
                    signs.add(sign);
            }

        return signs;
    }

    public @NotNull Set<Sign> findDescriptorSigns() {
        return findSigns().stream().filter(s -> isDescriptorSign(s)).collect(Collectors.toSet());
    }
}
