package top.earthstudio.nextgenbedwars.core.respawn.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.respawn.RespawnManager;

public class RespawnListener implements Listener {
    private final RespawnManager respawnManager;
    private final GameInstance gameInstance;

    private int respawnTick = 100; // TODO: configurable

    public RespawnListener(RespawnManager respawnManager, GameInstance gameInstance) {
        this.respawnManager = respawnManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPLayerFallInVoid(EntityDamageEvent event) { // TODO: Send event with uncancelled property
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntityType() != EntityType.PLAYER)
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.VOID)
            return;

        Player player = (Player) event.getEntity();

        event.setCancelled(true);
        respawnManager.setRespawn(player, respawnTick);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld() != gameInstance.getWorld())
            return;

        event.setCancelled(true);
        event.deathMessage(null);

        respawnManager.setRespawn(player, respawnTick);
    }
}
