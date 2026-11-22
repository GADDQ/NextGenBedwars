package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;

public class WarpPowderProp extends IProp {
    public int countdownTick = 100;
    public ItemStack propItem;

    public WarpPowderProp() {
        super("warp_powder");
    }

    @Override
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) { // TODO: particle animation and texture change and sound
        this.propItem = propItem;

        if (propItem.getPersistentDataContainer().has(PdcKeys.prop.warp_powder_active)) {
            gameInstance.get(WarpPowderManager.class).remove(player);
            player.sendMessage(Component.text("cancel")); // TODO: i18n
            return true;
        }

        player.sendMessage(Component.translatable("nextgenbedwars.prop.warp_powder_use", Component.text((float) countdownTick / 20)));
        propItem.editPersistentDataContainer(pdc -> pdc.set(PdcKeys.prop.warp_powder_active, PersistentDataType.BOOLEAN, true));
        gameInstance.get(WarpPowderManager.class).use(player, this);
        return true;
    }

    @Override
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, Location interactionLocation, ItemStack propItem) {
        if (interactionLocation.getBlock().getState() instanceof Container)
            return false;
        return super.onUseOnBlock(gameInstance, player, block, interactionLocation, propItem);
    }
}
