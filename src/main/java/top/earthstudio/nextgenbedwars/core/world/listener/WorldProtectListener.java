package top.earthstudio.nextgenbedwars.core.world.listener;

import net.kyori.adventure.text.Component;

import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import top.earthstudio.nextgenbedwars.api.util.BlockUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

public final class WorldProtectListener implements Listener {
    private final WorldProtector worldProtector;
    private final World world;

    public WorldProtectListener(WorldProtector worldProtector, World world) {
        this.worldProtector = worldProtector;
        this.world = world;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getWorld() == world) {
            if (
                    (
                            !Tag.AIR.isTagged(event.getBlockReplacedState().getType()) &&
                            Tag.REPLACEABLE.isTagged(event.getBlockReplacedState().getType())
                    ) ||
                            event.getBlockReplacedState().getBlock().isLiquid()
            ) {
                worldProtector.removeBlockFromWorldGroup(BlockUtil.asLong(event.getBlockPlaced()));
                return;
            }

            if (worldProtector.contains(BlockUtil.asLong(event.getBlockPlaced()))) {
                event.getPlayer().sendMessage(Component.translatable("nextgenbedwars.world_protect.forbidden_place"));
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

                event.getPlayer().sendMessage(Component.translatable("nextgenbedwars.world_protect.forbidden_break"));
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

            if (!toBlock.getType().isCollidable()) {
                if (Tag.AIR.isTagged(toBlock.getType()) && worldProtector.contains(BlockUtil.asLong(toBlock))) {
                    event.setCancelled(true);
                    return;
                }
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
            if (worldProtector.contains(BlockUtil.asLong(event.getBlock().getRelative(event.getDirection())))) {
                event.setCancelled(true);
                return;
            }

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

    // ---------- Advance Liquid Detect ----------
    // TODO: This need to be configurable because it's not the standard function from Bedwars

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getBlock().getWorld() != world)
            return;

        if (event.getBlock().getBlockData() instanceof Directional directional) {
            Block targetBlock = event.getBlock().getRelative(directional.getFacing());
            if (worldProtector.contains(BlockUtil.asLong(targetBlock)))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBlock().getWorld() != world)
            return;

        if (worldProtector.contains(BlockUtil.asLong(event.getBlock()))) {
            event.getPlayer().sendMessage(Component.translatable("nextgenbedwars.world_protect.forbidden_place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (event.getBlock().getWorld() != world)
            return;

        if (worldProtector.contains(BlockUtil.asLong(event.getBlock())))
            if (event.getBlock().isLiquid())
                worldProtector.removeBlockFromWorldGroup(BlockUtil.asLong(event.getBlock()));
    }
}