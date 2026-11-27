package top.earthstudio.nextgenbedwars.core.prop.builtin.trap.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;

import net.kyori.adventure.text.Component;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.trap.TrapManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import java.util.Iterator;
import java.util.List;

public class TrapListener implements Listener {
    static private final PotionEffect BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 100, 3, false);
    static private final PotionEffect WEAKNESS = new PotionEffect(PotionEffectType.WEAKNESS, 100, 3, false);
    static private final PotionEffect SLOWNESS = new PotionEffect(PotionEffectType.SLOWNESS, 100, 3, false);

    private final TrapManager trapManager;
    private final GameInstance gameInstance;

    public TrapListener(TrapManager trapManager, GameInstance gameInstance) {
        this.trapManager = trapManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTripwireStep(EntityInsideBlockEvent event) { // TODO: Spectator exclude
        if (!(event.getEntity() instanceof Player victim))
            return;

        if (victim.getWorld() != gameInstance.getWorld())
            return;

        if (victim.getGameMode() == GameMode.SPECTATOR)
            return;

        Block block = event.getBlock();
        if (block.getType() != Material.TRIPWIRE)
            return;

        if (!trapManager.contains(block))
            return;

        Team trapperTeam = trapManager.getTeam(block);
        Team victimTeam = gameInstance.get(TeamManager.class).get(victim);

        if (victimTeam == null || trapperTeam == victimTeam) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        destroyTrap(block);

        victim.addPotionEffect(WEAKNESS);
        victim.addPotionEffect(BLINDNESS);
        victim.addPotionEffect(SLOWNESS);
        victim.sendMessage(Component.translatable("nextgenbedwars.prop.trap_trigger"));

        trapperTeam.players.forEach(player -> player.sendMessage(Component.translatable("nextgenbedwars.prop.trap_trigger_trapper")));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getWorld() != gameInstance.getWorld())
            return;

        if (trapManager.contains(block)) {
            event.setDropItems(false);
            trapManager.remove(block);
            return;
        }

        destroyTrapAbove(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() != gameInstance.getWorld())
            return;

        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getWorld() != gameInstance.getWorld())
            return;

        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        if (piston.getWorld() != gameInstance.getWorld())
            return;

        destroyTrap(piston.getRelative(event.getDirection()));
        handlePistonMove(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block piston = event.getBlock();
        if (piston.getWorld() != gameInstance.getWorld())
            return;

        handlePistonMove(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLiquidFlow(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        if (toBlock.getWorld() != gameInstance.getWorld())
            return;

        destroyTrap(toBlock);
    }

    private void handleExplosion(List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (trapManager.contains(block)) {
                iterator.remove();
                destroyTrap(block);
                continue;
            }
            destroyTrapAbove(block);
        }
    }

    private void handlePistonMove(List<Block> blocks, BlockFace direction) {
        for (Block block : blocks) {
            destroyTrap(block);
            destroyTrap(block.getRelative(direction));
            destroyTrapAbove(block);
        }
    }

    private void destroyTrapAbove(Block supportBlock) {
        destroyTrap(supportBlock.getRelative(BlockFace.UP));
    }

    private void destroyTrap(Block block) {
        if (!trapManager.contains(block))
            return;

        block.setType(Material.AIR, false);
        trapManager.remove(block);
    }
}
