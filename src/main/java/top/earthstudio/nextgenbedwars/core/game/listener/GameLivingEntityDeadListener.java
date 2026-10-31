package top.earthstudio.nextgenbedwars.core.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class GameLivingEntityDeadListener implements Listener {
    private final GameInstance gameInstance;

    public GameLivingEntityDeadListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNonPlayerLivingEntityDead(EntityDeathEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity() instanceof Player)
            return;

        event.setDroppedExp(0);
        event.getDrops().clear();
    }
}
