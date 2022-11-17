package space.moontalk.mc.gamma;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.val;

import space.moontalk.mc.gamma.command.*;

@Getter
public class GammaPortals extends JavaPlugin {
    private @Nullable EmptyCompleter        emptyCompleter = new EmptyCompleter();
    private @Nullable FixPortalExecutor     fixPortalExecutor;
    private @Nullable FixPortalsExecutor    fixPortalsExecutor;
    private @Nullable ListPortalsExecutor   listPortalsExecutor;
    private @Nullable OnePortalCompleter    onePortalCompleter;
    private @Nullable RemovePortalExecutor  removePortalExecutor;
    private @Nullable RemovePortalsExecutor removePortalsExecutor;
    private @Nullable PortalManager         portalManager;
    private           int                   autoSavePeriod;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        autoSavePeriod = readAutoSavePeriod();

        try {
            portalManager = new PortalManager(this);
        } catch (Exception exception) {
            onIOException(exception);
            return;
        }

        registerAutoSaver();
        setupCommands();
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

    private void setupCommands() {
        onePortalCompleter = new OnePortalCompleter(portalManager);

        setupFixPortalCommand();
        setupFixPortalsCommand();
        setupListPortalsCommand();
        setupRemovePortalCommand();
        setupRemovePortalsCommand();
    }

    private void setupFixPortalCommand() {
        fixPortalExecutor = new FixPortalExecutor(portalManager);

        val command = Objects.requireNonNull(getCommand("fixPortal"));

        command.setExecutor(fixPortalExecutor);
        command.setTabCompleter(onePortalCompleter);
    }

    private void setupFixPortalsCommand() {
        fixPortalsExecutor = new FixPortalsExecutor(portalManager);
        
        val command = Objects.requireNonNull(getCommand("fixPortals"));

        command.setExecutor(fixPortalsExecutor);
        command.setTabCompleter(emptyCompleter);
    }

    private void setupListPortalsCommand() {
        listPortalsExecutor = new ListPortalsExecutor(portalManager);

        val command = Objects.requireNonNull(getCommand("listPortals"));

        command.setExecutor(listPortalsExecutor);
        command.setTabCompleter(emptyCompleter);
    }

    private void setupRemovePortalCommand() {
        removePortalExecutor = new RemovePortalExecutor(portalManager);

        val command = Objects.requireNonNull(getCommand("removePortal"));

        command.setExecutor(removePortalExecutor);
        command.setTabCompleter(onePortalCompleter);
    }

    private void setupRemovePortalsCommand() {
        removePortalsExecutor = new RemovePortalsExecutor(portalManager);

        val command = Objects.requireNonNull(getCommand("removePortals"));

        command.setExecutor(removePortalsExecutor);
        command.setTabCompleter(emptyCompleter);
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
