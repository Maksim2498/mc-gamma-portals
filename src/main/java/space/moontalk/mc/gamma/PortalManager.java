package space.moontalk.mc.gamma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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

import space.moontalk.mc.gamma.message.MessageProvider;

import static space.moontalk.mc.gamma.BlockUtility.*;

public class PortalManager implements Listener {
    private @NotNull Map<String, Set<Portal>> portals = new HashMap<>();
    private          boolean                  changed = true;

    @Getter
    private final @NotNull JavaPlugin plugin;

    @Getter
    private final int maxBlocks;

    @Getter
    private final @NotNull MessageProvider messageProvider;

    public PortalManager(@NotNull JavaPlugin plugin) throws IOException, ClassNotFoundException {
        this(plugin, new MessageProvider() {});
    }

    public PortalManager(@NotNull JavaPlugin plugin, @NotNull MessageProvider messageProvider) throws IOException, ClassNotFoundException {
        this.plugin          = plugin;
        this.messageProvider = messageProvider;
        this.maxBlocks       = readMaxBlocks();

        register();
        read();
    }

    public @NotNull Map<String, Set<Portal>> getPortals() {
        val portals = new HashMap<String, Set<Portal>>();
        
        for (val entry : this.portals.entrySet())
            portals.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));

        return Collections.unmodifiableMap(portals);
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

    public void removePortals() {
        for (val name : portals.keySet())
            destroyPortal(name);

        portals.clear();
    }

    public boolean removePortal(@NotNull String name) {
        val fullName = fullName(name);
        val result   = destroyPortal(fullName);

        if (result)
            this.portals.remove(fullName);

        return result;
    }

    private boolean destroyPortal(@NotNull String name) {
        val portals = this.portals.get(name);

        if (portals == null || portals.isEmpty())
            return false;

        for (val portal : portals) {
            val source = decY(portal.getExit().getBlock());

            try {
                val frameBlocks = PortalFrameBlocks.fromFrame(source, maxBlocks);

                if (!frameBlocks.isLit())
                    continue;

                val signs    = frameBlocks.findDescriptorSigns();
                val sign     = signs.stream().findAny().get();
                val signName = getDescriptorSignName(sign);

                if (!signName.equals(name))
                    continue;

                for (val block : frameBlocks.getInnerBlocks()) 
                    block.breakNaturally();
            } catch (Exception exception) {}
        }

        return true;
    }

    public void fixPortals() {
        for (val name : portals.keySet())
            fixPortal(name);
    }

    public boolean fixPortal(@NotNull String name) {
        val fullName = fullName(name);
        val portals  = this.portals.get(fullName);

        if (portals == null)
            return false;

        val toAdd = new HashSet<Portal>();

        portals.removeIf(portal -> {
            val source = decY(portal.getExit().getBlock());

            try {
                val frameBlocks = PortalFrameBlocks.fromFrame(source, maxBlocks);
                val signs       = frameBlocks.findDescriptorSigns();
                
                return switch (signs.size()) {
                    case 0 -> true;

                    case 1 -> {
                        val sign     = signs.stream().findAny().get();
                        val signName = getDescriptorSignName(sign);

                        if(!fullName.equals(signName))
                            yield true;

                        toAdd.add(frameBlocks.toPortal());

                        yield true;
                    }

                    default -> {
                        for (val sign : signs) 
                            sign.getBlock().breakNaturally();

                        yield true;
                    }
                };
            } catch (Exception exception) {
                return true;
            }
        });

        for (val portal : toAdd)
            portals.add(portal);

        if (portals.isEmpty())
            this.portals.remove(fullName);

        return true;
    }

    public boolean noPortals() {
        return portals.isEmpty();
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

        val file = getFile();

        if (file.length() > 0) {
            val fileStream = new FileInputStream(file);
            val stream     = new BukkitObjectInputStream(fileStream);
        
            portals = (Map<String, Set<Portal>>) stream.readObject();

            stream.close();
        }

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
    public void onBlockDestroy(@NotNull BlockDestroyEvent event) {
        tryRemovePortal(event.getBlock());
    }

    @EventHandler
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        for (val block : event.blockList())
            if (tryRemovePortal(block))
                return;
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        val player = event.getPlayer();
        val block  = event.getBlock();

        tryRemovePortal(block, player);
    }

    private boolean tryRemovePortal(@NotNull Block block) {
        return tryRemovePortal(block, null);
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

        if (player != null) 
            player.sendMessage(portals.size() == 1 ? messageProvider.makeNoPortalsLeftMessage(name)
                                                   : messageProvider.makeOnePortalLeftMessage(name)); 

        val portal = frameBlocks.toPortal();

        portals.remove(portal);

        if (portals.isEmpty())
            this.portals.remove(name);

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
                entity.sendMessage(messageProvider.makeAddSignMessage());
                return;
            }

            val sign    = signs.stream().findAny().get();
            val name    = getDescriptorSignName(sign);
            val portals = this.portals.computeIfAbsent(name, k -> new HashSet<>());

            switch (portals.size()) {
                case 1 -> entity.sendMessage(messageProvider.makeAddSecondMessage(name));
                case 2 -> {
                    val sourcePortal = frameBlocks.toPortal();
                    val targetPortal = getPortalPair(portals, sourcePortal);

                    targetPortal.teleport(sourcePortal, entity);
                }
            }
        } catch (Exception exception) {}
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        val item = event.getItem();

        if (item == null || item.getType() != Material.ENDER_EYE)
            return;

        val block = event.getClickedBlock();

        if (block == null)
            return;

        if (block.getType() == Material.NETHER_PORTAL) {
            event.setCancelled(true);
            return;
        }

        if (!PortalFrameBlocks.FRAME_MATERIALS.contains(block.getType()))
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
        val signs = frameBlocks.findDescriptorSigns();

        if (!player.hasPermission("gammaPortals.action.createPortal")) {
            player.sendMessage(messageProvider.makeMissingPermissionMessage()); 

            for (val sign : signs)
                sign.getBlock().breakNaturally();

            return;
        }

        val message = switch (signs.size()) {
            case 0 -> messageProvider.makeAddSignMessage();

            case 1 -> {
                val sign    = signs.stream().findAny().get();
                val name    = getDescriptorSignName(sign);
                val portals = this.portals.computeIfAbsent(name, k -> new HashSet<>());

                yield switch (portals.size()) {
                    case 0  -> {
                        portals.add(frameBlocks.toPortal()); 
                        changed = true;
                        yield messageProvider.makeAddSecondMessage(name);
                    }

                    case 1  -> {
                        portals.add(frameBlocks.toPortal()); 
                        changed = true;
                        yield messageProvider.makeActivatedMessage();
                    }

                    default -> { 
                        sign.getBlock().breakNaturally();
                        yield messageProvider.makeAlreadyExistsMessage(name);
                    }
                };
            }

            default -> {
                for (val sign : signs) 
                    sign.getBlock().breakNaturally();

                yield messageProvider.makeTooManySignsMessage();
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

    private @NotNull String fullName(@NotNull String name) {
        return String.format("[%s]", name);
    }
}
