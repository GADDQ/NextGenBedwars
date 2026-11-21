package top.earthstudio.nextgenbedwars.core.prop.builtin.platform;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.PlatformManager;

public class PlatformProp extends IProp {
    private static final Vector VECTOR_ZERO = new Vector(0, 0, 0);
    public Location centerLocation;

    public PlatformProp() {
        super("platform");
        cooldownTick = 200;
    }

    @Override
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) {
        Block block = player.getWorld().getBlockAt(player.getLocation().toBlockLocation().add(0, -1, 0));
        centerLocation = block.getLocation();

        if (player.getVelocity().getY() > -0.25) {
            if (block.isCollidable()) {
                player.sendMessage(Component.translatable("nextgenbedwars.prop.platform_invalid_use"));
                return true;
            }
        }

        if (block.isCollidable()) {
            player.setVelocity(VECTOR_ZERO);
            player.setFallDistance(0);
            player.teleport(player.getLocation().add(0, 1, 0));
            centerLocation = block.getLocation().add(0, 1, 0);
        }

        gameInstance.get(PlatformManager.class).use(this);

        setCooldownFor(player);
        propItem.setAmount(propItem.getAmount() - 1);
        return true;
    }
}
