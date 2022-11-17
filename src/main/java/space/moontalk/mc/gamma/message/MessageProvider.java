package space.moontalk.mc.gamma.message;

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import space.moontalk.mc.gamma.Portal;

public interface MessageProvider {
    default @NotNull String makePortalNotFoundMessage(@NotNull String name) {
        return String.format("§cPortal \"%s\" not found.", name); 
    }

    default @NotNull String makeNoPortalsMessage() {
        return "§cThere is no portals";
    }

    default @NotNull String makePortalsFixedMessage() {
        return "§aAll portals has been fixed.";
    }

    default @NotNull String makePortalFixedMessage(@NotNull String name) {
        return String.format("§aPortal \"%s\" has been fixed.", name);
    }

    default @NotNull String makePortalsRemovedMessage() {
        return "§aAll portals has been removed";
    }

    default @NotNull String makePortalRemovedMessage(@NotNull String name) {
        return String.format("§aPortal \"%s\" has been removed.", name);
    }

    default @NotNull String makePortalListHeaderMessage(@NotNull Map<String, Set<Portal>> portals) {
        return String.format("Portals (%d):", portals.size());
    }

    default @NotNull String makePortalListItemMessage(@NotNull String name, @NotNull Set<Portal> portals) {
        return String.format("- \"%s\" (%s);", name, portals.size() == 1 ? "one" : "pair");
    }

    default @NotNull String makeOnePortalLeftMessage(@NotNull String name) {
        return String.format("§eOne portal tagged %s left.", name);
    }

    default @NotNull String makeNoPortalsLeftMessage(@NotNull String name) {
        return String.format("§eNo more portals tagged %s left.", name);
    }

    default @NotNull String makeAddSignMessage() {
        return "§ePortal is currently inactive.\nAdd descriptor sign to activate it.";
    }

    default @NotNull String makeTooManySignsMessage() {
        return "§cPortal has to many descriptor signs.\nIt must have only one.";
    }
    
    default @NotNull String makeAlreadyExistsMessage(@NotNull String name) {
        return String.format("§cPortal pair tagged with %s already exists.", name);
    }

    default @NotNull String makeActivatedMessage() {
        return "§aPortal is active and successfully linked.";
    }

    default @NotNull String makeAddSecondMessage(@NotNull String name) {
        return String.format("§ePortal is currently inactive.\nCreate second portal tagged %s to activate it.", name);
    }

    default @NotNull String makeMissingPermissionMessage() {
        return "§cYou have no permission to create a portal.";
    }
}
