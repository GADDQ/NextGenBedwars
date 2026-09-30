package top.earthstudio.nextgenbedwars.api.shop;

import top.earthstudio.nextgenbedwars.api.game.IManager;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;

import java.util.UUID;

public interface ShopManager extends IManager<Shop> {
    boolean contains(UUID uuid);

    GuiPage getShopGui(UUID uuid);
}
