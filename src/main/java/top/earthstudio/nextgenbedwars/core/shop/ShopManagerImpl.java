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

    private GameInstance gameInstance;

    public ShopConfig shopConfig; // TODO: public DEBUG ONLY

    public ShopManagerImpl(GameInstance gameInstance) {
        shops = new Object2ObjectOpenHashMap<>();
        world = gameInstance.getWorld();
        this.gameInstance = gameInstance;

        shopInteractListener = new ShopInteractListener(this, gameInstance);
        shopItemThrowListener = new ShopItemThrowListener(gameInstance);

        Bukkit.getPluginManager().registerEvents(shopInteractListener, BedwarsAPI.getInstance().plugin());
        Bukkit.getPluginManager().registerEvents(shopItemThrowListener, BedwarsAPI.getInstance().plugin());

        shopConfig = new ShopConfig(gameInstance);
        shopConfig.getValues().forEach(this::add);
    }

    @Override
    public UUID add(Shop shop) {
        UUID uuid = UUID.randomUUID();
        shop.location.setWorld(world);
        shop.uuid = uuid;
        shop.shopPageProvider = shopConfig.providerFor(shop.shopType);

        ShopEntity shopEntity = new ShopEntity(shop);
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

        gameInstance = null;

        world = null;

        HandlerList.unregisterAll(shopItemThrowListener);
        HandlerList.unregisterAll(shopInteractListener);
        shopItemThrowListener = null;
        shopInteractListener = null;

        shops.forEach((uuid, shopEntity) -> shopEntity.destroy());
        shops.clear();
        shops = null;
    }
}
