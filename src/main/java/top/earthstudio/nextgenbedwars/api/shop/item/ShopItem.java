package top.earthstudio.nextgenbedwars.api.shop.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public final class ShopItem { // struct ShopItem // TODO: Material Only, not ItemStack. add Component:description/name
    public String id;
    public List<ItemStack> showItems;
    public List<ItemStack> actualItems;
    public Material coinType;
    public int price;

    public int maxLevel = 0;

    public boolean isDecreaseLevelWhenPlayerDead = false;
    public boolean isKeepWhenDead = false;
    public boolean isThrowable = true;

    public ShopItem() {}

    public boolean isUpgradeable() {
        return maxLevel > 0;
    }

    public ItemStack getShowItem(int level) {
        return showItems.get(level);
    }

    public ItemStack getActualItem(int level) {
        return actualItems.get(level);
    }
}
