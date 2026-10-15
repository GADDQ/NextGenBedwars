package top.earthstudio.nextgenbedwars.api.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.inventory.ItemStack;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public final class Category {
    public ItemStack categoryIcon;
    public List<ShopItem> items;

    public Category() {}
}
