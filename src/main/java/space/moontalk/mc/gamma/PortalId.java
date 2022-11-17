package space.moontalk.mc.gamma;

import java.io.Serializable;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public record PortalId(@NotNull UUID worldUUID, int centerX, int centerY, int centerZ, int blockCount) implements Serializable {
    public PortalId {
        if (blockCount <= 0)
            throw new IllegalArgumentException("blockCount has to be positive");
    }
}
