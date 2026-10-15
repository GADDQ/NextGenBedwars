package top.earthstudio.nextgenbedwars.api.shop;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IManager;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import java.util.UUID;

public interface ShopManager extends IManager<Shop> {
    boolean contains(UUID uuid);

    IGuiPage getShopGui(UUID uuid, Player player);
}
