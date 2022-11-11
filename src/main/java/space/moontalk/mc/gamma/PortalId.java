package space.moontalk.mc.gamma;

import java.io.Serializable;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public record PortalId(@NotNull UUID worldUUID, int centerX, int centerY, int centerZ) implements Serializable {

}
