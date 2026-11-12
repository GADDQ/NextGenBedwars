package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ItemShopConfig extends IConfig<List<Category>> {
    private final GameInstance gameInstance;
    private final List<Category> categories;
    private final Map<String, ShopItem> itemCache;

    public ItemShopConfig(GameInstance gameInstance) {
        super(gameInstance, "item_shop.yml", true, "itemShop");
        this.gameInstance = gameInstance;
        this.categories = new ObjectArrayList<>();
        this.itemCache = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public List<Category> load() {
        categories.clear();
        itemCache.clear();
        categories.addAll(super.load());
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

    public List<Category> getCategories() {
        return categories;
    }

    public ShopItem getItem(String id) {
        return itemCache.get(id);
    }

    public Function<Player, ? extends IGuiPage> getProvider() { // TODO: i18n
        return player -> new ItemShopPage(gameInstance, player, Component.text("物品商店").color(NamedTextColor.GOLD), categories);
    }
}