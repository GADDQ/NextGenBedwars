package top.earthstudio.nextgenbedwars.core.game.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;

public class GameNonPlayingActionListener implements Listener {
    private final GameInstance gameInstance;

    public GameNonPlayingActionListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPLayerFallInVoid(EntityDamageEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntityType() != EntityType.PLAYER)
            return;

        if (gameInstance.getGameState() == GameState.PLAYING)
            return;

        Player player = (Player) event.getEntity();

        event.setCancelled(true);

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID)
            player.teleport(gameInstance.getGame().waitingLobbyLocation);
    }
}
