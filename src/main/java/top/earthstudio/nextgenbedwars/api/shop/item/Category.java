package top.earthstudio.nextgenbedwars.api.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.inventory.ItemStack;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.stream.Collectors;

@ConfigSerializable
public final class Category {
    public ItemStack categoryIcon;
    public List<ShopItem> items;

    public Category() {}

    public Category copy() {
        Category copy = new Category();
        copy.categoryIcon = this.categoryIcon != null ? this.categoryIcon.clone() : null;
        if (this.items != null) {
            copy.items = this.items.stream()
                    .map(ShopItem::copy)
                    .collect(Collectors.toCollection(ObjectArrayList::new));
        }
        return copy;
    }
}
