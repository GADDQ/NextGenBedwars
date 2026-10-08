package top.earthstudio.nextgenbedwars.core.shop;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;
import top.earthstudio.nextgenbedwars.api.util.ConfigUtil;

import top.earthstudio.nextgenbedwars.core.shop.item.ItemShopPage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShopConfig implements IConfig<Shop> {
    private final Config shopsConfig;
    private final Config itemShopConfig;
    private final List<Shop> shopList;

    public ShopConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());

        Config globalItemShop = new Config("item_shop.yml");
        this.itemShopConfig = new Config(gameFolder, "item_shop.yml", globalItemShop);
        this.shopsConfig = new Config(gameFolder, "shops.yml");
        this.shopList = new ObjectArrayList<>();

        loadFromConfig();
    }

    private void loadFromConfig() {
        Object raw = shopsConfig.read("shops");
        if (raw == null) return;

        if (!(raw instanceof List<?> list)) {
            throw new IllegalStateException("'shops' must be a list in shops.yml");
        }

        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map)) {
                throw new IllegalStateException("shops[" + i + "] must be a map");
            }
            shopList.add(buildShop(map, i));
        }
    }

    private Shop buildShop(Map<?, ?> map, int index) {
        String tag = "shops[" + index + "]";

        String type = ConfigUtil.parseString(map, "type", tag);
        EntityType entity = ConfigUtil.parseEntityType(map.get("entityType"), tag);

        Location location = ConfigUtil.parseLocation(map, tag);
        Supplier<GuiPage> provider = providerFor(type, tag);

        Component title = map.containsKey("title") ? ConfigUtil.parseComponent(map.get("title"), tag + ".title") : Component.text(type);
        Component subTitle = map.containsKey("subtitle") ? ConfigUtil.parseComponent(map.get("subtitle"), tag + ".subtitle") : Component.empty();

        return new Shop(title, subTitle, entity, location, provider);
    }

    private Supplier<GuiPage> providerFor(String type, String tag) {
        return switch (type) {
            case "item_shop" -> {
                Component guiTitle = ConfigUtil.parseComponent(readItemShopGuiTitle(), "item_shop.yml.title");
                List<Category> categories = loadItemShopCategories();
                yield () -> new ItemShopPage(guiTitle, categories);
            }
            default -> throw new IllegalStateException(tag + " unknown shop type '" + type + "'");
        };
    }

    private String readItemShopGuiTitle() {
        Object raw = itemShopConfig.read("title");
        if (raw == null) throw new IllegalStateException("item_shop.yml missing 'title'");
        return raw.toString();
    }

    private List<Category> loadItemShopCategories() {
        Object raw = itemShopConfig.read("categories");
        if (raw == null) throw new IllegalStateException("item_shop.yml missing 'categories'");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalStateException("item_shop.yml 'categories' must be a non-empty list");
        }

        List<Category> categories = new ObjectArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map)) {
                throw new IllegalStateException("item_shop.yml categories[" + i + "] must be a map");
            }
            categories.add(buildCategory(map, i));
        }
        return categories;
    }

    private Category buildCategory(Map<?, ?> map, int index) {
        String tag = "item_shop.yml categories[" + index + "]";
        ItemStack icon = ConfigUtil.parseItemStack(map.get("icon"), tag, "icon");
        Category category = new Category(icon);

        Object rawItems = map.get("items");
        if (rawItems == null) throw new IllegalStateException(tag + " missing 'items'");
        if (!(rawItems instanceof List<?> list)) throw new IllegalStateException(tag + " 'items' must be a list");

        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> itemMap)) {
                throw new IllegalStateException(tag + " items[" + i + "] must be a map");
            }
            category.items.add(buildShopItem(itemMap, tag + " items[" + i + "]"));
        }
        return category;
    }

    private ShopItem buildShopItem(Map<?, ?> map, String tag) {
        ItemStack show = ConfigUtil.parseItemStack(map.get("show"), tag, "show");
        Material coin = ConfigUtil.parseMaterial(map.get("coin"), tag + ".coin");
        int price = ConfigUtil.parseInt(map, "price", tag);

        if (map.containsKey("actual")) {
            ItemStack actual = ConfigUtil.parseItemStack(map.get("actual"), tag, "actual");
            return new ShopItem(show, coin, price, actual);
        }
        return new ShopItem(show, coin, price);
    }

    @Override
    public List<Shop> getValues() {
        return shopList;
    }

    @Override
    public void release() {
        itemShopConfig.release();
        shopsConfig.release();
    }
}