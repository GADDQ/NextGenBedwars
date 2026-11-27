package top.earthstudio.nextgenbedwars.core.shop.modifier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.showmodifier.ShopShowModify;
import top.earthstudio.nextgenbedwars.api.shop.showmodifier.ShopShowModifier;

import java.util.Map;

public class ShopShowModifierImpl implements ShopShowModifier {
    public Map<String, ShopShowModify> modifiers;

    public ShopShowModifierImpl(GameInstance gameInstance) {
        modifiers = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void register(String id, ShopShowModify modifier) {
        if (modifiers.putIfAbsent(id, modifier) != null)
            throw new IllegalStateException("modifier id already been used!");
    }

    @Override
    public void unregister(String id) {
        modifiers.remove(id);
    }

    @Override
    public ShopShowModify get(String id) {
        return modifiers.get(id);
    }

    @Override
    public void shutdown() {
        modifiers.clear();
        modifiers = null;
    }
}