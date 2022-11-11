package space.moontalk.mc.gamma;

import static space.moontalk.mc.gamma.BlockUtility.findNeighbourOfType;
import static space.moontalk.mc.gamma.BlockUtility.getDescriptorSignName;
import static space.moontalk.mc.gamma.BlockUtility.getSignBlock;
import static space.moontalk.mc.gamma.BlockUtility.isDescriptorSign;
import static space.moontalk.mc.gamma.BlockUtility.isDescriptorSignLines;
import static space.moontalk.mc.gamma.BlockUtility.tryGetFaceNeighbour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;

import lombok.Getter;
import lombok.val;

public class PortalManager implements Listener {
    private @NotNull Map<String, Set<Portal>> portals = new HashMap<>();
    private          boolean                  changed = true;

    @Getter
    private final @NotNull JavaPlugin plugin;

    @Getter
    private final int maxBlocks;

    private static @NotNull String NO_MORE_LEFT_MESSAGE   = "§eNo more portals tagged %s left.";
    private static @NotNull String ONE_LEFT_MESSAGE       = "§eOne portal tagged %s left.";
    private static @NotNull String ADD_SIGN_MESSAGE       = "§ePortal is currently inactive.\nAdd descriptor sign to activate it.";
    private static @NotNull String TOO_MANY_SINGS_MESSAGE = "§cPortal has to many descriptor signs.\nIt must have only one.";
    private static @NotNull String ALREADY_EXISTS_MESSAGE = "§cPortal pair tagged with %s already exists.";
    private static @NotNull String ACTIVATED_MESSAGE      = "§aPortal is active and successfully linked.";
    private static @NotNull String NEED_SECOND_MESSAGE    = "§ePortal is currently inactive.\nCreate second portal tagged %s to activate it.";

    public PortalManager(@NotNull JavaPlugin plugin) throws IOException, ClassNotFoundException {
        this.plugin    = plugin;
        this.maxBlocks = readMaxBlocks();

        register();
        read();
    }

    private void register() {
        val server  = plugin.getServer();  
        val manager = server.getPluginManager();
        manager.registerEvents(this, plugin);
    }

    private int readMaxBlocks() {
        val config    = plugin.getConfig();
        val maxBlocks = config.getInt("max-blocks");

        if (maxBlocks < 0)
            throw new RuntimeException("max-blocks config property has to be positive");

        return maxBlocks;
    }

    public void save() throws IOException {
        if (!changed)
            return;

        val fileStream = new FileOutputStream(getFile());
        val stream     = new BukkitObjectOutputStream(fileStream);

        stream.writeObject(portals);

        changed = false;
    }

    @SuppressWarnings("unchecked")
    public void read() throws IOException, ClassNotFoundException {
        if (!changed)
            return;

        val fileStream = new FileInputStream(getFile());
        val stream     = new BukkitObjectInputStream(fileStream);

        portals = (Map<String, Set<Portal>>) stream.readObject();

        changed = false;
    }

    public @NotNull File getFile() throws IOException {
        val file = new File (plugin.getDataFolder(), "portals");
        file.createNewFile();
        return file;
    }

    @EventHandler
    public void onSignChange(@NotNull SignChangeEvent event) {
        val block  = event.getBlock();
        val player = event.getPlayer();

        if (isDescriptorSign(block)) {
            val source = getSignBlock(block);
            tryRemovePortal(source, player);
        }

        if (isDescriptorSignLines(event.lines())) {
            val server    = plugin.getServer();
            val scheduler = server.getScheduler();

            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                try {
                    val source      = getSignBlock(block);
                    val frameBlocks = PortalFrameBlocks.fromFrame(source, maxBlocks);

                    createPortal(frameBlocks, player);
                } catch (Exception exception) {}
            }, 1);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(@NotNull BlockPistonRetractEvent event) {
        for (val block : event.getBlocks())
            if (tryRemovePortal(block, null))
                return;
    }

    @EventHandler
    public void onBlockPistonExtend(@NotNull BlockPistonExtendEvent event) {
        for (val block : event.getBlocks())
            if (tryRemovePortal(block, null))
                return;
    }

    @EventHandler
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        for (val block : event.blockList())
            if (tryRemovePortal(block, null))
                return;
    }

    @EventHandler
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        for (val block : event.blockList()) 
            if (tryRemovePortal(block, null))
                return;
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        val player = event.getPlayer();
        val block  = event.getBlock();

        tryRemovePortal(block, player);
    }

    private boolean tryRemovePortal(@NotNull Block block, @Nullable Player player) {
        val type = block.getType();

        try {
            if (PortalFrameBlocks.FRAME_MATERIALS.contains(type)) {
                val frameBlocks = PortalFrameBlocks.fromFrame(block, maxBlocks);
                removePortal(frameBlocks, player);
                return true;
            } else if (type == Material.NETHER_PORTAL) {
                val frameBlocks = PortalFrameBlocks.fromPortal(block, maxBlocks);
                removePortal(frameBlocks, player);
                return true;
            } else if (block.getState() instanceof Sign sign && isDescriptorSign(sign)) {
                val source      = getSignBlock(sign);
                val frameBlocks = PortalFrameBlocks.fromFrame(source, maxBlocks);
                removePortal(frameBlocks, player);
                return true;
            }
        } catch (Exception exception) {}

        return false;
    }

    private void removePortal(@NotNull PortalFrameBlocks frameBlocks, @Nullable Player player) {
        val signs = frameBlocks.findDescriptorSigns();

        if (signs.isEmpty())
            return;

        val sign    = signs.stream().findAny().get();
        val name    = getDescriptorSignName(sign);
        val portals = this.portals.computeIfAbsent(name, k -> new HashSet<>());

        if (player != null) {
            val message = portals.size() == 1
                        ? String.format(NO_MORE_LEFT_MESSAGE, name)
                        : String.format(ONE_LEFT_MESSAGE,     name);
                        
            player.sendMessage(message); 
        }

        val portal = frameBlocks.toPortal();

        portals.remove(portal);
        changed = true;
    }

    @EventHandler
    public void onEntityPortalReady(@NotNull EntityPortalReadyEvent event) {
        val entity     = event.getEntity();
        val block      = entity.getLocation().getBlock();
        val portalBock = findNeighbourOfType(block, Material.NETHER_PORTAL);

        if (portalBock == null)
            return;

        try {
            val frameBlocks = PortalFrameBlocks.fromPortal(portalBock, maxBlocks);

            event.setCancelled(true);

            val signs = frameBlocks.findDescriptorSigns();

            if (signs.isEmpty()) {
                entity.sendMessage(ADD_SIGN_MESSAGE);
                return;
            }

            val sign    = signs.stream().findAny().get();
            val name    = getDescriptorSignName(sign);
            val portals = this.portals.computeIfAbsent(name, k -> new HashSet<>());

            switch (portals.size()) {
                case 1 -> entity.sendMessage(String.format(NEED_SECOND_MESSAGE, name));
                case 2 -> {
                    val sourcePortal = frameBlocks.toPortal();
                    val targetPortal = getPortalPair(portals, sourcePortal);

                    targetPortal.teleport(entity);
                }
            }
        } catch (Exception exception) {}
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        val block = event.getClickedBlock();

        if (block == null || !PortalFrameBlocks.FRAME_MATERIALS.contains(block.getType()))
            return;

        val item = event.getItem();

        if (item == null || item.getType() != Material.ENDER_EYE)
            return;

        event.setCancelled(true);

        val face   = event.getBlockFace();
        val source = tryGetFaceNeighbour(block, face);

        if (source == null)
            return;

        try {
            val frameBlocks = PortalFrameBlocks.fromAir(source, maxBlocks);
            val player      = event.getPlayer();

            createPortal(frameBlocks, player, item);
        } catch (Exception exception) {}
    }

    private void createPortal(@NotNull PortalFrameBlocks frameBlocks, @NotNull Player player, @NotNull ItemStack item) {
        frameBlocks.ignite();

        if (player.getGameMode() != GameMode.CREATIVE)
            item.subtract();

        createPortal(frameBlocks, player);
    }

    private void createPortal(@NotNull PortalFrameBlocks frameBlocks, @NotNull Player player) {
        val signs   = frameBlocks.findDescriptorSigns();
        val message = switch (signs.size()) {
            case 0 -> ADD_SIGN_MESSAGE;

            case 1 -> {
                val sign    = signs.stream().findAny().get();
                val name    = getDescriptorSignName(sign);
                val portals = this.portals.computeIfAbsent(name, k -> new HashSet<>());

                yield switch (portals.size()) {
                    case 0  -> {
                        portals.add(frameBlocks.toPortal()); 
                        changed = true;
                        yield String.format(NEED_SECOND_MESSAGE, name);
                    }

                    case 1  -> {
                        portals.add(frameBlocks.toPortal()); 
                        changed = true;
                        yield ACTIVATED_MESSAGE;
                    }

                    default -> { 
                        sign.getBlock().breakNaturally();
                        yield String.format(ALREADY_EXISTS_MESSAGE, name); 
                    }
                };
            }

            default -> {
                for (val sign : signs) 
                    sign.getBlock().breakNaturally();

                yield TOO_MANY_SINGS_MESSAGE;
            }
        };

        player.sendMessage(message);
    }

    private @NotNull Portal getPortalPair(@NotNull Set<Portal> portals, @NotNull Portal portal) {
        assert portals.size() == 2;

        for (val second : portals)
            if (!second.equals(portal))
                return second;

        assert false;
        return null;
    }
}
