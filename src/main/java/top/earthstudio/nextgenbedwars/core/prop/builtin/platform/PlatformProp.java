package top.earthstudio.nextgenbedwars.core.prop.builtin.platform;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.platform.PlatformManager;

public class PlatformProp extends IProp {
    private static final Vector VECTOR_ZERO = new Vector(0, 0, 0);
    public Location centerLocation;

    public PlatformProp() {
        super("platform");
        cooldownTick = 400;
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

        player.setVelocity(VECTOR_ZERO);
        player.setFallDistance(0);

        if (block.isCollidable()) {
            player.teleport(player.getLocation().add(0, 1, 0));
            centerLocation = block.getLocation().add(0, 1, 0);
        }

        gameInstance.get(PlatformManager.class).use(this);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_FALL, 2, 0.7f);

        setCooldownFor(player);
        propItem.setAmount(propItem.getAmount() - 1);
        return true;
    }
}
