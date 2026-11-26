package top.earthstudio.nextgenbedwars.core.prop.builtin.magnetshoe.listener;

import io.papermc.paper.event.entity.EntityKnockbackEvent;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.Objects;
import java.util.Random;

public class MagnetShoesListener implements Listener, IGameSubSystem {
    static private final Vector VECTOR_ZERO = new Vector(0, 0, 0);

    private final GameInstance gameInstance;
    private final Random random;

    public MagnetShoesListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.random = new Random();
    }

    @Override
    public void onReady() {
        Bukkit.getPluginManager().registerEvents(this, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void update() {}

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerKnockback(EntityKnockbackEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        ItemStack shoe = player.getInventory().getBoots();

        if (shoe == null)
            return;

        var pdc = shoe.getPersistentDataContainer();
        if (!Objects.equals(pdc.get(new NamespacedKey("nextgenbedwars", "prop_id"), PersistentDataType.STRING), "magnet_shoes"))
            return;

        int num = random.nextInt(100) + 1;
        if (num <= 75)
            event.setKnockback(VECTOR_ZERO);
    }
}
