package top.earthstudio.nextgenbedwars.api.shop.executor;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

@SuppressWarnings("unused")
public interface ShopExecutor extends IGameSubSystem {
    void register(String id, ShopExecute shopExecute);
    void unregister(String id);
    ShopExecute get(String id);

    @Override
    default void update() {}
}
