package top.earthstudio.nextgenbedwars.api.prop.builtin.compass;

import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.UUID;

@SuppressWarnings("unused")
public interface CompassManager extends IGameSubSystem { // ATTENTION: This was built by LLM.
    record NearestTarget(Player target, double distance) {}

    NearestTarget getNearestTarget(Player player);
    void refresh(Player player, ItemStack compassItem);
    void clear(UUID playerUuid);
}