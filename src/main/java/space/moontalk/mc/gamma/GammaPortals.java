package space.moontalk.mc.gamma;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.val;

@Getter
public class GammaPortals extends JavaPlugin {
    private @Nullable PortalManager portalManager;
    private           int           autoSavePeriod;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        autoSavePeriod = readAutoSavePeriod();

        try {
            portalManager = new PortalManager(this);
        } catch (Exception exception) {
            onIOException(exception);
        }

        registerAutoSaver();
    }

    private int readAutoSavePeriod() {
        val config         = getConfig();
        val autoSavePeriod = config.getInt("auto-save");

        if (autoSavePeriod < 0)
            throw new RuntimeException("auto-save has to be positive");

        return 20 * autoSavePeriod;
    }

    private void registerAutoSaver() {
        val server    = getServer();
        val scheduler = server.getScheduler();

        scheduler.scheduleSyncRepeatingTask(this, () -> {
            try {
                portalManager.save();
            } catch (Exception exception) {
                onIOException(exception);
            }
        }, autoSavePeriod, autoSavePeriod);
    }

    private void onIOException(@NotNull Exception exception) {
        val logger  = getLogger();
        val message = String.format("Failed to read/write portal data: %s\nDisabling plugin...", exception.getMessage());

        logger.log(Level.WARNING, message);

        val server  = getServer();
        val manager = server.getPluginManager();

        manager.disablePlugin(this);
    }

    @Override
    public void onDisable() {
        try {
            portalManager.save();
        } catch (Exception exception) {
            onDisableIOException(exception);
        }
    }

    private void onDisableIOException(@NotNull Exception exception) {
        val logger  = getLogger();
        val message = String.format("failed to save portal data: %s", exception.getMessage());

        logger.log(Level.WARNING, message);
    }
}
