package top.earthstudio.nextgenbedwars.api.shop.showmodifier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

@FunctionalInterface
public interface ShopShowModify {
    ItemStack modify(GameInstance gameInstance, Player player, ShopItem shopItem, int targetLevel, ItemStack showItem);
}