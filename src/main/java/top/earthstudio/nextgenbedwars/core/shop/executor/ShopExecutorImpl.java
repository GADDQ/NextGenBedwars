package top.earthstudio.nextgenbedwars.core.shop.executor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopBuyAction;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;

import java.util.Map;

public class ShopExecutorImpl implements ShopExecutor {
    public Map<String, ShopBuyAction> actions;

    public ShopExecutorImpl(GameInstance gameInstance) {
        actions = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void register(String id, ShopBuyAction shopBuyAction) {
        if (actions.putIfAbsent(id, shopBuyAction) != null)
            throw new IllegalStateException("executor id already been used!");
    }

    @Override
    public void unregister(String id) {
        actions.remove(id);
    }

    @Override
    public ShopBuyAction get(String id) {
        return actions.get(id);
    }

    @Override
    public void shutdown() {
        actions.clear();
        actions = null;
    }
}
