package top.earthstudio.nextgenbedwars.core.world.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

public class WorldProtectListener implements Listener {
    // TODO: i18n
    private final WorldProtector worldProtector;
    private final World world;

    public WorldProtectListener(WorldProtector worldProtector, World world) {
        this.worldProtector = worldProtector;
        this.world = world;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getWorld() == world) {
            if (worldProtector.contains(BlockPosUtil.asLong(event.getBlockPlaced()))) {
                event.getPlayer().sendMessage(Component.text("你不允许在这里放置方块！").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 2. 方块破坏：床放行，玩家放置的方块允许破坏，地图原方块拦截
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) { // TODO: 装饰性无碰撞体积方块放行并从保护中删除, 并设置不掉落物品
        if (event.getBlock().getWorld() == world) {
            if (worldProtector.contains(BlockPosUtil.asLong(event.getBlock()))) {
                if (org.bukkit.Tag.BEDS.isTagged(event.getBlock().getType()))
                    return;

                event.getPlayer().sendMessage(Component.text("你只能破坏玩家放置的方块！").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 3. 实体爆炸（TNT、火球）
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() == world) {
            event.blockList().removeIf(block ->
                    worldProtector.contains(BlockPosUtil.asLong(block))
            );
        }
    }

    /**
     * 4. 方块爆炸
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getWorld() == world) {
            event.blockList().removeIf(block ->
                    worldProtector.contains(BlockPosUtil.asLong(block))
            );
        }
    }
}