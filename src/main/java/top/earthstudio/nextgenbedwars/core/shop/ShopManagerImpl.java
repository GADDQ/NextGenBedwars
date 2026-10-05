package top.earthstudio.nextgenbedwars.core.shop;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.GuiOpener;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;

import top.earthstudio.nextgenbedwars.core.shop.listener.ShopInteractListener;

import java.util.Map;
import java.util.UUID;

public class ShopManagerImpl implements ShopManager {
    private Map<UUID, ShopEntity> shops;
    private ShopInteractListener shopInteractListener;
    private World world;

    public ShopManagerImpl(GameInstance gameInstance) {
        shops = new Object2ObjectOpenHashMap<>();
        shopInteractListener = new ShopInteractListener(this, gameInstance);
        world = gameInstance.getWorld();
        Bukkit.getPluginManager().registerEvents(shopInteractListener, BedwarsAPI.getInstance().getPlugin());
    }

    @Override
    public UUID add(Shop shop) {
        shop.location.setWorld(world);
        ShopEntity shopEntity = new ShopEntity(shop);
        shops.put(shopEntity.getUuid(), shopEntity);
        return shopEntity.getUuid();
    }

    @Override
    public Shop get(UUID uuid) {
        return shops.get(uuid).shop;
    }

    @Override
    public boolean contains(UUID uuid) {
        return shops.get(uuid) != null;
    }

    @Override
    public GuiPage getShopGui(UUID uuid) {
        return shops.get(uuid).getGui();
    }

    @Override
    public void remove(UUID uuid) {
        shops.remove(uuid).destroy();
    }

    @Override
    public void shutdown() {
        world = null;

        HandlerList.unregisterAll(shopInteractListener);
        shopInteractListener = null;

        shops.forEach((uuid, shopEntity) -> shopEntity.destroy());
        shops.clear();
        shops = null;
    }
}
