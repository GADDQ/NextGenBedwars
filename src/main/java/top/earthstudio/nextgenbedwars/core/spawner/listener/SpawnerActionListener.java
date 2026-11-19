package top.earthstudio.nextgenbedwars.core.spawner.listener;

import net.kyori.adventure.translation.GlobalTranslator;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.persistence.PersistentDataContainer;

import org.bukkit.persistence.PersistentDataType;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;

import java.util.Map;

public final class SpawnerActionListener implements Listener {
    private final GameInstance gameInstance;
    private final Map<String, SpawnerType> spawnerTypes;

    public SpawnerActionListener(GameInstance gameInstance, Map<String, SpawnerType> spawnerTypes) {
        this.gameInstance = gameInstance;
        this.spawnerTypes = spawnerTypes;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPick(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        var pdc = event.getItem().getItemStack().getPersistentDataContainer();

        if (player.getWorld() != gameInstance.getWorld())
            return;

        if (!pdc.has(PdcKeys.spawner.spawnerType))
            return;

        event.getItem().getItemStack().editMeta(itemMeta ->
                itemMeta.customName(GlobalTranslator.render(
                        spawnerTypes.get(pdc.get(PdcKeys.spawner.spawnerType, PersistentDataType.STRING)).resourceName,
                        player.locale()
                ))
        );
    }
}
