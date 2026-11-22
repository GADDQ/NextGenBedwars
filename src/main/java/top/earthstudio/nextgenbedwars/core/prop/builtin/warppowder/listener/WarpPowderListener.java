package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.listener;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;

public class WarpPowderListener implements Listener {
    private final WarpPowderManager warpPowderManager;
    private final GameInstance gameInstance;

    public WarpPowderListener(WarpPowderManager warpPowderManager, GameInstance gameInstance) {
        this.warpPowderManager = warpPowderManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (!event.hasChangedPosition())
            return;

        Player player = event.getPlayer();
        if (warpPowderManager.contains(player)) {
            warpPowderManager.remove(player);
            player.sendMessage(Component.translatable("nextgenbedwars.prop.warp_powder_failed"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (warpPowderManager.contains(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getWorld() != gameInstance.getWorld())
            return;

        if (event.getCurrentItem() == null)
            return;

        Player player = (Player) event.getWhoClicked();

        if (!warpPowderManager.contains(player))
            return;

        if (event.getCurrentItem().getPersistentDataContainer().has(PdcKeys.prop.warp_powder_active))
            event.setCancelled(true);
    }
}
