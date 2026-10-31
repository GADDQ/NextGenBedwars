package top.earthstudio.nextgenbedwars.core.world.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import top.earthstudio.nextgenbedwars.api.util.BlockUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

public final class WorldProtectListener implements Listener {
    // TODO: i18n
    private final WorldProtector worldProtector;
    private final World world;

    public WorldProtectListener(WorldProtector worldProtector, World world) {
        this.worldProtector = worldProtector;
        this.world = world;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getWorld() == world) {
            if (!Tag.AIR.isTagged(event.getBlockReplacedState().getType()) && Tag.REPLACEABLE.isTagged(event.getBlockReplacedState().getType())) {
                worldProtector.removeBlockFromWorldGroup(BlockUtil.asLong(event.getBlockPlaced()));
                return;
            }

            if (worldProtector.contains(BlockUtil.asLong(event.getBlockPlaced()))) {
                event.getPlayer().sendMessage(Component.text("你不允许在这里放置方块！").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 2. 方块破坏：床放行，玩家放置的方块允许破坏，地图原方块拦截
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getWorld() == world) {
            if (!event.getBlock().getType().isCollidable()) {
                if (!worldProtector.contains(BlockUtil.asLong(event.getBlock())))
                    return;

                event.setDropItems(false);
                worldProtector.removeBlockFromWorldGroup(BlockUtil.asLong(event.getBlock()));
                return;
            }

            if (worldProtector.contains(BlockUtil.asLong(event.getBlock()))) {
                if (Tag.BEDS.isTagged(event.getBlock().getType()))
                    return;

                event.getPlayer().sendMessage(Component.text("你只能破坏玩家放置的方块！").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 3. 实体爆炸（TNT、火球）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() == world) {
            event.blockList().removeIf(block ->
                    worldProtector.contains(BlockUtil.asLong(block)) || Tag.BEDS.isTagged(block.getType())
            );
        }
    }

    /**
     * 4. 方块爆炸
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getWorld() == world) {
            event.blockList().removeIf(block ->
                    worldProtector.contains(BlockUtil.asLong(block)) || Tag.BEDS.isTagged(block.getType())
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLiquidFlow(BlockFromToEvent event) {
        if (event.getToBlock().getWorld() == world) {
            Block toBlock = event.getToBlock();

            if (!toBlock.getType().isAir() && !toBlock.isLiquid() && !toBlock.getType().isCollidable()) {
                worldProtector.removeBlockFromWorldGroup(BlockUtil.asLong(toBlock));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.getBlock().getWorld() == world) {
            if (worldProtector.contains(BlockUtil.asLong(event.getBlock()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getWorld() == world) {
            if (worldProtector.contains(BlockUtil.asLong(event.getBlock()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlock().getWorld() == world) {
            for (Block block : event.getBlocks()) {
                if (worldProtector.contains(BlockUtil.asLong(block)) || Tag.BEDS.isTagged(block.getType())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlock().getWorld() == world) {
            for (Block block : event.getBlocks()) {
                if (worldProtector.contains(BlockUtil.asLong(block)) || Tag.BEDS.isTagged(block.getType())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}