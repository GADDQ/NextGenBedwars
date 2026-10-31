package top.earthstudio.nextgenbedwars.core.shop;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;

import top.earthstudio.nextgenbedwars.core.shop.listener.ShopEntityProtectListener;
import top.earthstudio.nextgenbedwars.core.shop.listener.ShopInteractListener;
import top.earthstudio.nextgenbedwars.core.shop.listener.ShopItemThrowListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManagerImpl implements ShopManager {
    private Map<UUID, ShopEntity> shops;
    private World world;

    private ShopInteractListener shopInteractListener;
    private ShopItemThrowListener shopItemThrowListener;
    private ShopEntityProtectListener shopEntityProtectListener;

    private GameInstance gameInstance;

    private ShopConfig shopConfig;

    public ShopManagerImpl(GameInstance gameInstance) {
        shops = new Object2ObjectOpenHashMap<>();
        world = gameInstance.getWorld();
        this.gameInstance = gameInstance;

        if (!gameInstance.isEditMode()) {
            shopInteractListener = new ShopInteractListener(this, gameInstance);
            shopItemThrowListener = new ShopItemThrowListener(gameInstance);
            shopEntityProtectListener = new ShopEntityProtectListener(this, gameInstance);

            Bukkit.getPluginManager().registerEvents(shopInteractListener, BedwarsAPI.getInstance().plugin());
            Bukkit.getPluginManager().registerEvents(shopItemThrowListener, BedwarsAPI.getInstance().plugin());
            Bukkit.getPluginManager().registerEvents(shopEntityProtectListener, BedwarsAPI.getInstance().plugin());
        }

        shopConfig = new ShopConfig(gameInstance);
        shopConfig.getValues().forEach(this::add);
    }

    @Override
    public UUID add(Shop shop) {
        shop.location.setWorld(world);
        shop.shopPageProvider = shopConfig.providerFor(shop.shopType);

        ShopEntity shopEntity = new ShopEntity(shop);
        UUID uuid = shopEntity.getUuid();
        shop.uuid = uuid;
        shops.put(uuid, shopEntity);

        return uuid;
    }

    @Override
    public Shop get(UUID uuid) {
        return shops.get(uuid).shop;
    }

    @Override
    public List<Shop> getAll() {
        List<Shop> shopList =  new ObjectArrayList<>();
        shops.values().forEach(shopEntity -> shopList.add(shopEntity.shop));
        return shopList;
    }

    @Override
    public boolean contains(UUID uuid) {
        return shops.get(uuid) != null;
    }

    @Override
    public IGuiPage getShopGui(UUID uuid, Player player) {
        return shops.get(uuid).getGui(player);
    }

    @Override
    public void remove(UUID uuid) {
        shops.remove(uuid).destroy();
    }

    @Override
    public void save() {
        shopConfig.save(getAll());
    }

    @Override
    public void shutdown() {
        shopConfig.release();
        shopConfig = null;

        world = null;

        if (!gameInstance.isEditMode()) {
            HandlerList.unregisterAll(shopItemThrowListener);
            HandlerList.unregisterAll(shopInteractListener);
            HandlerList.unregisterAll(shopEntityProtectListener);
            shopItemThrowListener = null;
            shopInteractListener = null;
            shopEntityProtectListener = null;
        }

        gameInstance = null;

        shops.forEach((uuid, shopEntity) -> shopEntity.destroy());
        shops.clear();
        shops = null;
    }
}
