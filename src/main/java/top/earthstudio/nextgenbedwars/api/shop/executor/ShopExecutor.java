package top.earthstudio.nextgenbedwars.api.shop.executor;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

@SuppressWarnings("unused")
public interface ShopExecutor extends IGameSubSystem {
    void register(String id, ShopBuyAction shopBuyAction);
    void unregister(String id);
    ShopBuyAction get(String id);

    @Override
    default void update() {}
}
