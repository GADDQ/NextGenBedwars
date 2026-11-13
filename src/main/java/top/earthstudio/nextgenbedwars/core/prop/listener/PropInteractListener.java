package top.earthstudio.nextgenbedwars.core.prop.listener;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.PropManager;

import java.util.UUID;

public class PropInteractListener implements Listener {
    private final PropManager propManager;
    private final GameInstance gameInstance;

    private final Object2IntOpenHashMap<UUID> lastEntityInteractTick = new Object2IntOpenHashMap<>();

    public PropInteractListener(PropManager propManager, GameInstance gameInstance) {
        this.propManager = propManager;
        this.gameInstance = gameInstance;
        this.lastEntityInteractTick.defaultReturnValue(-1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (lastEntityInteractTick.getInt(event.getPlayer().getUniqueId()) == Bukkit.getCurrentTick()) {
            event.setCancelled(true);
            return;
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND)
            if (!event.getPlayer().getInventory().getItemInMainHand().isEmpty())
                return;

        if (event.useItemInHand() == org.bukkit.event.Event.Result.DENY)
            return;

        if (event.getItem() == null)
            return;

        var pdc = event.getItem().getItemMeta().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);

        boolean noCancelled = true;

        switch (event.getAction())  {
            case LEFT_CLICK_AIR -> noCancelled = propManager.get(id).onClick(gameInstance, event.getPlayer(), event.getItem());
            case RIGHT_CLICK_AIR -> noCancelled = propManager.get(id).onUse(gameInstance, event.getPlayer(), event.getItem());
            case LEFT_CLICK_BLOCK -> noCancelled = propManager.get(id).onClickOnBlock(gameInstance, event.getPlayer(), event.getClickedBlock(), event.getItem());
            case RIGHT_CLICK_BLOCK -> noCancelled = propManager.get(id).onUseOnBlock(gameInstance, event.getPlayer(), event.getClickedBlock(), event.getItem());
            default -> {}
        }

        if (!noCancelled)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (event.getRightClicked().getType() != EntityType.PLAYER)
            return;

        if (event.getHand() == EquipmentSlot.OFF_HAND)
            if (!event.getPlayer().getInventory().getItemInMainHand().isEmpty())
                return;

        ItemStack prop = event.getPlayer().getInventory().getItem(event.getHand());
        if (prop.isEmpty())
            return;

        var pdc = prop.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);

        if (!propManager.get(id).onUseOnPlayer(gameInstance, event.getPlayer(), (Player) event.getRightClicked(), prop))
            event.setCancelled(true);

        lastEntityInteractTick.put(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttacked(PrePlayerAttackEntityEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (event.getAttacked().getType() != EntityType.PLAYER)
            return;

        ItemStack prop;

        if (event.getPlayer().getInventory().getItemInMainHand().isEmpty())
            prop = event.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND);
        else prop = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

        if (prop.isEmpty())
            return;

        var pdc = prop.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);

        if (!propManager.get(id).onAttack(gameInstance, event.getPlayer(), (Player) event.getAttacked(), prop))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPick(PlayerAttemptPickupItemEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        ItemStack prop = event.getItem().getItemStack();

        var pdc = prop.getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);

        if (!propManager.get(id).onPick(gameInstance, event.getPlayer(), prop))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        var pdc = event.getItemDrop().getItemStack().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);
        if (!propManager.get(id).onDrop(gameInstance, event.getPlayer(), event.getItemDrop()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        var pdc = event.getItem().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);
        if (!propManager.get(id).onConsume(gameInstance, event.getPlayer(), event.getPlayer().getInventory().getItem(event.getHand())))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLoadCrossbow(EntityLoadCrossbowEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        var pdc = event.getCrossbow().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);
        if (!propManager.get(id).onCrossbowLoad(gameInstance, player, player.getInventory().getItem(event.getHand())))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerShootBow(EntityShootBowEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        assert event.getBow() != null;
        var pdc = event.getBow().getPersistentDataContainer();
        if (!pdc.has(PdcKeys.prop.id))
            return;

        String id = pdc.get(PdcKeys.prop.id, PersistentDataType.STRING);

        var resultPair = propManager.get(id).onBowShoot(gameInstance, player, player.getInventory().getItem(event.getHand()));

        if (!resultPair.left())
            event.setCancelled(true);
        else if (resultPair.right() != null)
            event.setProjectile(resultPair.right());
    }
}
