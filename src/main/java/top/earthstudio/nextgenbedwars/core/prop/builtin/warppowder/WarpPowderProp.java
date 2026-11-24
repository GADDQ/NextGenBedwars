package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;

public class WarpPowderProp extends IProp {
    public int usingTick = 100;
    public ItemStack propItem;

    public WarpPowderProp() { // TODO: sound and more particle animation
        super("warp_powder");
        propTexture = Material.GUNPOWDER;
        cooldownTick = 100;
    }

    @Override
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) { // TODO: particle animation and texture change and sound
        this.propItem = propItem;

        if (propItem.getPersistentDataContainer().has(PdcKeys.prop.warp_powder_active)) {
            gameInstance.get(WarpPowderManager.class).remove(player);
            player.sendMessage(Component.translatable("nextgenbedwars.prop.warp_powder_cancel"));
            return true;
        }

        if (gameInstance.get(WarpPowderManager.class).use(player, this)) {
            player.sendMessage(Component.translatable("nextgenbedwars.prop.warp_powder_use", Component.text((float) usingTick / 20)));
            propItem.editPersistentDataContainer(pdc -> pdc.set(PdcKeys.prop.warp_powder_active, PersistentDataType.BOOLEAN, true));
        } else player.sendMessage(Component.translatable("nextgenbedwars.prop.warp_powder_already_using"));

        return true;
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, BlockFace blockFace, ItemStack propItem) {
        if ((block.getState() instanceof Container) || block.getState().getType() == Material.ENDER_CHEST)
            return false;
        return super.onUseOnBlock(gameInstance, player, block, blockFace, propItem);
    }

    @Override
    public void setCooldownFor(Player player) {
        super.setCooldownFor(player);
    }
}
