package top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep.TNTSheepManager;

public class TNTSheepProp extends IProp { // ATTENTION: This was built by LLM.
    public TNTSheepProp() {
        super("tnt_sheep");
        propTexture = Material.SHEEP_SPAWN_EGG;
    }

    @Override
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) {
        Location spawnLoc = player.getLocation().getBlock()
                .getRelative(player.getFacing())
                .getLocation()
                .add(0.5, 0.0, 0.5);
        return trySpawn(gameInstance, player, propItem, spawnLoc);
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, BlockFace blockFace, ItemStack propItem) {
        if ((block.getState() instanceof Container) || block.getState().getType() == Material.ENDER_CHEST)
            return false;

        Block above = block.getRelative(blockFace);
        Location spawnLoc = above.getType().isAir()
                ? above.getLocation().add(0.5, 0.0, 0.5)
                : player.getLocation().getBlock().getRelative(player.getFacing()).getLocation().add(0.5, 0.0, 0.5);

        return trySpawn(gameInstance, player, propItem, spawnLoc);
    }

    private boolean trySpawn(GameInstance gameInstance, Player player, ItemStack propItem, Location spawnLoc) {
        TNTSheepManager manager = gameInstance.get(TNTSheepManager.class);
        if (!manager.spawn(player, spawnLoc)) {
            player.sendMessage(Component.translatable("nextgenbedwars.prop.tnt_sheep_no_target"));
            return true;
        }

        propItem.setAmount(propItem.getAmount() - 1);
        return true;
    }
}