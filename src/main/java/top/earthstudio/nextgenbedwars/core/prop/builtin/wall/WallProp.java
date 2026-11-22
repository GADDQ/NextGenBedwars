package top.earthstudio.nextgenbedwars.core.prop.builtin.wall;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.util.BlockUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

public class WallProp extends IProp {
    public WallProp() {
        super("wall");
        cooldownTick = 400;
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, Location interactionLocation, ItemStack propItem) {
        WorldProtector worldProtector = gameInstance.get(WorldProtector.class);
        Block centerBottom = interactionLocation.getBlock();

        switch (player.getFacing()) {
            case EAST:
            case WEST:
                for (int vertical = 0; vertical < 5; vertical++) {
                    for (int horizontal = -4; horizontal <= 4; horizontal++) {
                        Block target = centerBottom.getRelative(0, vertical, horizontal);
                        if (!target.isCollidable())
                            if (!worldProtector.contains(BlockUtil.asLong(target)))
                                target.setType(Material.BRICKS);
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                for (int vertical = 0; vertical < 5; vertical++) {
                    for (int horizontal = -4; horizontal <= 4; horizontal++) {
                        Block target = centerBottom.getRelative(horizontal, vertical, 0);
                        if (!target.isCollidable())
                            if (!worldProtector.contains(BlockUtil.asLong(target)))
                                target.setType(Material.BRICKS);
                    }
                }
                break;
        }

        setCooldownFor(player);
        player.playSound(player, Sound.BLOCK_ANVIL_LAND, 2, 0.5f);
        propItem.setAmount(propItem.getAmount() - 1);

        return true;
    }
}
