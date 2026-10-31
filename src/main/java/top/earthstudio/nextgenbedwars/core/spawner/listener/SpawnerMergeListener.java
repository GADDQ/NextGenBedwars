package top.earthstudio.nextgenbedwars.core.spawner.listener;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class SpawnerMergeListener implements Listener {
    private final GameInstance gameInstance;

    public SpawnerMergeListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMergeEvent(ItemMergeEvent event) {
        Item entity = event.getEntity();
        Item target = event.getTarget();

        if (entity.getWorld() != gameInstance.getWorld() || target.getWorld() != gameInstance.getWorld())
            return;

        PersistentDataContainer entityPdc = entity.getPersistentDataContainer();
        PersistentDataContainer targetPdc = target.getPersistentDataContainer();

        if (!(entityPdc.has(PdcKeys.spawner.drop) == targetPdc.has(PdcKeys.spawner.drop))) {
            event.setCancelled(true);
            return;
        }

        if (entityPdc.has(PdcKeys.spawner.preventMerge) || targetPdc.has(PdcKeys.spawner.preventMerge))
            event.setCancelled(true);
    }
}
