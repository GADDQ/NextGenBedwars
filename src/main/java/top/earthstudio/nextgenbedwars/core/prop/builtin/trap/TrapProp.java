package top.earthstudio.nextgenbedwars.core.prop.builtin.trap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.trap.TrapManager;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

public class TrapProp extends IProp {
    public TrapProp() {
        super("trap");
        propTexture = Material.STRING;
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, BlockFace blockFace, ItemStack propItem) {
        Block target = block.getRelative(blockFace);
        gameInstance.get(TrapManager.class).add(target, gameInstance.get(TeamManager.class).get(player));
        return false;
    }
}
