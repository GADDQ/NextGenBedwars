package top.earthstudio.nextgenbedwars.api.shop.showmodifier;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

@SuppressWarnings("unused")
public interface ShopShowModifier extends IGameSubSystem {
    void register(String id, ShopShowModify modifier);
    void unregister(String id);
    ShopShowModify get(String id);

    @Override
    default void update() {}
}