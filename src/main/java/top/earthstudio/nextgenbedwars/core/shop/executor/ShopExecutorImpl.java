package top.earthstudio.nextgenbedwars.core.shop.executor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecute;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;

import java.util.Map;

public class ShopExecutorImpl implements ShopExecutor {
    public Map<String, ShopExecute> actions;

    public ShopExecutorImpl(GameInstance gameInstance) {
        actions = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void register(String id, ShopExecute shopExecute) {
        if (actions.putIfAbsent(id, shopExecute) != null)
            throw new IllegalStateException("executor id already been used!");
    }

    @Override
    public void unregister(String id) {
        actions.remove(id);
    }

    @Override
    public ShopExecute get(String id) {
        return actions.get(id);
    }

    @Override
    public void shutdown() {
        actions.clear();
        actions = null;
    }
}
