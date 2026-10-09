package top.earthstudio.nextgenbedwars.api.shop;

import top.earthstudio.nextgenbedwars.api.game.IManager;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;

import java.util.UUID;

public interface ShopManager extends IManager<Shop> {
    boolean contains(UUID uuid);

    ChestGuiPage getShopGui(UUID uuid);
}
