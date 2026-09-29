package top.earthstudio.nextgenbedwars.api.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ShopItem { // struct ShopItem // TODO: Material Only, not ItemStack. add Component:description
    public ItemStack showItem;
    public ItemStack actualItem = null;
    public Material coinType;
    public int price;

    public ShopItem(ItemStack showItem, Material coinType, int price) {
        this.showItem = showItem;
        this.coinType = coinType;
        this.price = price;
    }

    public ShopItem(ItemStack showItem, Material coinType, int price, ItemStack actualItem) {
        this(showItem, coinType, price);
        this.actualItem = actualItem;
    }
}
