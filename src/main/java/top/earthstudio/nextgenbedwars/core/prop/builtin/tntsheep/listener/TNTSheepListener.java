package top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep.TNTSheepManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

public class TNTSheepListener implements Listener { // ATTENTION: This was built by LLM. TODO: Spectator exclude
    private final TNTSheepManager tntSheepManager;
    private final GameInstance gameInstance;

    public TNTSheepListener(TNTSheepManager tntSheepManager, GameInstance gameInstance) {
        this.tntSheepManager = tntSheepManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (tntSheepManager.isManagedSheep(event.getEntity())) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause != EntityDamageEvent.DamageCause.VOID
                    && cause != EntityDamageEvent.DamageCause.FALL
                    && cause != EntityDamageEvent.DamageCause.CUSTOM) {
                event.setDamage(0.0);
            }
            return;
        }

        Team explodingTeam = tntSheepManager.getExplodingTeam();
        if (explodingTeam != null && event.getEntity() instanceof Player victim) {
            if (victim.getGameMode() == GameMode.SPECTATOR) {
                event.setCancelled(true);
                return;
            }
            Team victimTeam = gameInstance.get(TeamManager.class).get(victim);
            if (explodingTeam.equals(victimTeam)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (tntSheepManager.isManagedSheep(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSheepDeath(EntityDeathEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (!tntSheepManager.isManagedSheep(event.getEntity()))
            return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        tntSheepManager.removeBySheep(event.getEntity().getUniqueId());
    }
}