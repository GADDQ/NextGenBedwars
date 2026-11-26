package top.earthstudio.nextgenbedwars.api.shop.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ItemShopShowModifier {
    ItemStack modify(Player player, ShopItem shopItem, int targetLevel, ItemStack showItem);
}