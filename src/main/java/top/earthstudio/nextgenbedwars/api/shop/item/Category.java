package top.earthstudio.nextgenbedwars.api.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class Category {
    public final ItemStack categoryIcon;
    public final List<ShopItem> items;

    public Category(ItemStack categoryIcon) {
        this.categoryIcon = categoryIcon;
        this.items = new ObjectArrayList<>();
    }
}
