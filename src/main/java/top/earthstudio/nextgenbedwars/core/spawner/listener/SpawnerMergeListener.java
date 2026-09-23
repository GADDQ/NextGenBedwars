package top.earthstudio.nextgenbedwars.core.spawner.listener;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import top.earthstudio.nextgenbedwars.api.PdcKeys;

public class SpawnerMergeListener implements Listener {
    @EventHandler
    public void onItemMergeEvent(ItemMergeEvent event) {
        // TODO: abstract tags class/enum
        Item entity = event.getEntity();
        Item target = event.getTarget();

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
