package top.earthstudio.nextgenbedwars.core.prop.builtin.compass;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.compass.CompassManager;

public class CompassProp extends IProp { // ATTENTION: This was built by LLM.
    public CompassProp() {
        super("compass");
        propTexture = Material.COMPASS;
        cooldownTick = 20;
    }

    @Override
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) {
        CompassManager compassManager = gameInstance.get(CompassManager.class);
        CompassManager.NearestTarget result = compassManager.getNearestTarget(player);

        if (result == null) {
            player.sendMessage(Component.translatable("nextgenbedwars.prop.tracker_compass_no_target"));
            return true;
        }

        Location targetLoc = result.target().getLocation();
        propItem.editMeta(CompassMeta.class, meta -> {
            meta.setLodestone(targetLoc);
            meta.setLodestoneTracked(false);
        });

        player.sendMessage(Component.translatable(
                "nextgenbedwars.prop.tracker_compass_target",
                Component.text(String.format("%.1f", result.distance()))
        ));
        setCooldownFor(player);
        return true;
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, BlockFace blockFace, ItemStack propItem) {
        if ((block.getState() instanceof Container) || block.getState().getType() == Material.ENDER_CHEST)
            return false;
        return super.onUseOnBlock(gameInstance, player, block, blockFace, propItem);
    }
}
