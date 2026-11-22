package top.earthstudio.nextgenbedwars.core.prop.builtin.platform.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.platform.PlatformManager;

import java.util.List;

public class PlatformListener implements Listener {
    private final PlatformManager platformManager;
    private final GameInstance gameInstance;

    public PlatformListener(PlatformManager platformManager, GameInstance gameInstance) {
        this.platformManager = platformManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (platformManager.contains(event.getBlock())) {
            event.setDropItems(false);
            platformManager.remove(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() != gameInstance.getWorld())
            return;

        handleExplode(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getWorld() != gameInstance.getWorld())
            return;

        handleExplode(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlock().getWorld() != gameInstance.getWorld())
            return;

        for (Block block : event.getBlocks()) {
            if (platformManager.contains(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlock().getWorld() != gameInstance.getWorld())
            return;

        for (Block block : event.getBlocks()) {
            if (platformManager.contains(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void handleExplode(List<Block> blocks) {
        var it = blocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (platformManager.contains(block)) {
                block.setType(Material.AIR);
                it.remove();
            }
            platformManager.remove(block);
        }
    }
}
