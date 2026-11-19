package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ItemShopConfig extends IConfig<List<Category>> {
    private final Map<String, ShopItem> itemCache;

    public ItemShopConfig(GameInstance gameInstance) {
        super(gameInstance, "item_shop.yml", true, "itemShop");
        this.itemCache = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public List<Category> load() {
        itemCache.clear();
        var categories = new ObjectArrayList<>(super.load());
        for (Category category : categories) {
            if (category.items != null) {
                for (ShopItem item : category.items) {
                    if (item.id == null)
                        throw new IllegalStateException("ShopItem's ID cannot be null!");

                    itemCache.put(item.id, item);
                }
            }
        }
        return categories;
    }

    public ShopItem getItem(String id) {
        return itemCache.get(id);
    }
}