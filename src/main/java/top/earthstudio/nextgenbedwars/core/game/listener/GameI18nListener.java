package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.translation.GlobalTranslator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.language.ComponentDataType;

@SuppressWarnings("DataFlowIssue")
public class GameI18nListener implements Listener {
    private final GameInstance gameInstance;

    public GameI18nListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPick(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        var pdc = event.getItem().getItemStack().getPersistentDataContainer();

        if (player.getWorld() != gameInstance.getWorld())
            return;

        if (!pdc.has(PdcKeys.i18n.translatable))
            return;

        event.getItem().getItemStack().editMeta(itemMeta ->
                itemMeta.customName(GlobalTranslator.render(
                        pdc.get(PdcKeys.i18n.translatable, ComponentDataType.COMPONENT),
                        player.locale()
                ))
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLocaleChanged(PlayerLocaleChangeEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld() != gameInstance.getWorld())
            return;

        Inventory inventory = event.getPlayer().getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null)
                continue;

            var pdc = item.getPersistentDataContainer();

            if (!pdc.has(PdcKeys.i18n.translatable))
                continue;

            item.editMeta(itemMeta ->
                    itemMeta.customName(GlobalTranslator.render(
                            pdc.get(PdcKeys.i18n.translatable, ComponentDataType.COMPONENT),
                            event.locale()
                    ))
            );
        }
    }
}
