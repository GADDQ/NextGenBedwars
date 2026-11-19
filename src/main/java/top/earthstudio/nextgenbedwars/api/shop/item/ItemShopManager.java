package top.earthstudio.nextgenbedwars.api.shop.item;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public interface ItemShopManager extends IGameSubSystem {
    IGuiPage cacheAndOpenFor(Player player);
    List<Category> getTemplateCategories();
    Map<Material, Component> getResourceNames();

    boolean canAfford(Player player, ShopItem shopItem);
    boolean isMaxLevel(Player player, ShopItem shopItem);
    int getTargetLevel(Player player, ShopItem shopItem);
    boolean buyItem(Player player, ShopItem shopItem);
    boolean buyItem(Player player, ShopItem shopItem, int times);
    int getBatchTimes(ShopItem shopItem, boolean fullStack);

    @Override
    default void update() {}
}