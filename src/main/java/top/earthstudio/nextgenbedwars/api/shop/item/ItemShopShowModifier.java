package top.earthstudio.nextgenbedwars.api.shop.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

@FunctionalInterface
public interface ItemShopShowModifier {
    ItemStack modify(GameInstance gameInstance, Player player, ShopItem shopItem, int targetLevel, ItemStack showItem);
}