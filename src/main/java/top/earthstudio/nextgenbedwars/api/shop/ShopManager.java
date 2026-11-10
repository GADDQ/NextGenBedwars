package top.earthstudio.nextgenbedwars.api.shop;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IManager;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface ShopManager extends IManager<Shop> {
    boolean contains(UUID uuid);
    void addProvider(String id, Function<Player, ? extends IGuiPage> provider);
    void removeProvider(String id);
    IGuiPage getShopGui(UUID uuid, Player player);
}
