package top.earthstudio.nextgenbedwars.core.game.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;

import java.util.Collection;

public final class GamePlayerBreakBlockListener implements Listener {
    private final GameInstance gameInstance;
    private final ItemStack silkTouchTool;

    public GamePlayerBreakBlockListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;

        silkTouchTool = new ItemStack(Material.NETHERITE_PICKAXE);
        silkTouchTool.editMeta(itemMeta -> {
            itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            itemMeta.setUnbreakable(true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBreakBlock(BlockBreakEvent event) { // TODO: config to allow disabled silk_touch
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        event.setExpToDrop(0);

        Collection<ItemStack> drops = event.getBlock().getDrops(silkTouchTool);

        event.setDropItems(false);
        for (ItemStack drop : drops) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop);
        }
    }
}
