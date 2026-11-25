package top.earthstudio.nextgenbedwars.api.shop.executor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

@FunctionalInterface
public interface ShopBuyAction {
    boolean execute(Player player, ShopItem shopItem, ItemStack giveItem);
}
