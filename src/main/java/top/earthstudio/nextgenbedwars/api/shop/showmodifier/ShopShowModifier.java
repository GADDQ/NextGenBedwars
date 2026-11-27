package top.earthstudio.nextgenbedwars.api.shop.showmodifier;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.shop.item.ItemShopShowModifier;

@SuppressWarnings("unused")
public interface ShopShowModifier extends IGameSubSystem {
    void register(String id, ItemShopShowModifier modifier);
    void unregister(String id);
    ItemShopShowModifier get(String id);

    @Override
    default void update() {}
}