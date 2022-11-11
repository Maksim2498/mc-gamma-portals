package space.moontalk.mc.gamma;

import java.util.List;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.val;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public interface BlockUtility {
    static @NotNull String DESCRIPTOR_REGEX = "\\s*\\[[-\\p{Punct}\\w\\s]+\\]\\s*";

    static @NotNull Block getSignBlock(@NotNull Block signBlock) {
        if (signBlock.getState() instanceof Sign sign)
            return getSignBlock(sign);

        throw new IllegalArgumentException("not a sign");
    }

    static @NotNull Block getSignBlock(@NotNull Sign sign) {
        val block = sign.getBlock();

        if (sign.getBlockData() instanceof Directional directional) 
            return tryGetOppositeFaceNeighbour(block, directional.getFacing()); 

        return decY(block);
    }

    static @Nullable Block findNeighbourOfType(@NotNull Block block, @NotNull Material type) {
        for (val axis : Axis.values()) {
            val incBlock = incAxis(block, axis);

            if (incBlock.getType() == type)
                return incBlock;

            val decBlock = decAxis(block, axis);

            if (decBlock.getType() == type)
                return decBlock;
        }

        return null;
    }

    static boolean isDescriptorSign(@NotNull Block block) {
        if (block.getState() instanceof Sign sign)
            return isDescriptorSign(sign);

        return false;
    }

    static boolean isDescriptorSign(@NotNull Sign sign) {
        return isDescriptorSignLines(sign.lines());
    }

    static boolean isDescriptorSignLines(@NotNull List<Component> lines) {
        int matchCount = 0;

        for (val line : lines) 
            if (line instanceof TextComponent textLine) 
                if (textLine.content().matches(DESCRIPTOR_REGEX))
                    ++matchCount;

        return matchCount == 1;
    }

    public static @Nullable String getDescriptorSignName(@NotNull Sign sign) {
        String match = null;

        for (val line : sign.lines()) 
            if (line instanceof TextComponent textLine) {
                val text = textLine.content();

                if (text.matches(DESCRIPTOR_REGEX)) {
                    if (match != null)
                        return null;

                    match = text.replaceAll("\\s+", "");
                }
            }

        return match;
    }

    static @Nullable Block tryGetOppositeFaceNeighbour(@NotNull Block block, @NotNull BlockFace face) {
        return switch (face) {
            case UP    -> decY(block);
            case DOWN  -> incY(block);
            case SOUTH -> decZ(block);
            case NORTH -> incZ(block);
            case EAST  -> decX(block);
            case WEST  -> incX(block);
            default    -> null;
        };
    }

    static @Nullable Block tryGetFaceNeighbour(@NotNull Block block, @NotNull BlockFace face) {
        return switch (face) {
            case UP    -> incY(block);
            case DOWN  -> decY(block);
            case SOUTH -> incZ(block);
            case NORTH -> decZ(block);
            case EAST  -> incX(block);
            case WEST  -> decX(block);
            default    -> null;
        };
    }

    static @NotNull Block decAxis(@NotNull Block block, @NotNull Axis axis) {
        return switch (axis) {
            case X -> decX(block);
            case Y -> decY(block);
            case Z -> decZ(block);
        };
    }

    static @NotNull Block decX(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX() - 1, block.getY(), block.getZ());
    }

    static @NotNull Block decY(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
    }

    static @NotNull Block decZ(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() - 1);
    }

    static @NotNull Block incAxis(@NotNull Block block, @NotNull Axis axis) {
        return switch (axis) {
            case X -> incX(block);
            case Y -> incY(block);
            case Z -> incZ(block);
        };
    }

    static @NotNull Block incX(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX() + 1, block.getY(), block.getZ());
    }

    static @NotNull Block incY(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
    }

    static @NotNull Block incZ(@NotNull Block block) {
        return block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() + 1);
    }
}
