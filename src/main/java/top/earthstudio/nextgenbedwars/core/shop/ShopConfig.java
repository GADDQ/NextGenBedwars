package top.earthstudio.nextgenbedwars.core.shop;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import top.earthstudio.nextgenbedwars.core.shop.item.ItemShopPage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShopConfig {
    private final Config shopsConfig;
    private final Config itemShopConfig;   // 全局 + 局部 overlay

    public final List<Shop> shopList;

    public ShopConfig(GameInstance gameInstance) { // TODO: 1. italic, bold, etc. format process; 2. auto item_lore: how much? what coin?
        File gameFolder = GameFolder.of(gameInstance.getGame());

        Config globalItemShop = new Config("item_shop.yml");
        this.itemShopConfig = new Config(gameFolder, "item_shop.yml", globalItemShop);
        this.shopsConfig    = new Config(gameFolder, "shops.yml");
        this.shopList       = new ObjectArrayList<>();

        loadFromConfig(gameInstance.getWorld());
    }

    // ================= 加载 =================

    private void loadFromConfig(World world) {
        Object raw = shopsConfig.read("shops");
        if (raw == null) return;

        if (!(raw instanceof List<?> list))
            throw new IllegalStateException("'shops' must be a list in shops.yml");

        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map))
                throw new IllegalStateException("shops[" + i + "] must be a map");
            shopList.add(buildShop(map, i, world));
        }
    }

    private Shop buildShop(Map<?, ?> map, int index, World world) {
        String tag = "shops[" + index + "]";

        String type       = requireString(map, "type", tag);
        EntityType entity = parseEntityType(map.get("entityType"), tag);
        Location location = parseLocation(map, tag, world);
        Supplier<GuiPage> provider = providerFor(type, tag);

        // TODO i18n: title/subTitle 应由 i18n 系统按 type 提供
        Component title    = Component.text(type);
        Component subTitle = Component.empty();

        return new Shop(title, subTitle, entity, location, provider);
    }

    // ================= 类型分发 =================

    private Supplier<GuiPage> providerFor(String type, String tag) {
        return switch (type) {
            case "item_shop" -> {
                Component guiTitle = Component.text(readItemShopGuiTitle());
                List<Category> categories = loadItemShopCategories();
                yield () -> new ItemShopPage(guiTitle, categories);
            }
            default -> throw new IllegalStateException(
                    tag + " unknown shop type '" + type + "'");
        };
    }

    private String readItemShopGuiTitle() {
        Object raw = itemShopConfig.read("title");
        if (raw == null)
            throw new IllegalStateException("item_shop.yml missing 'title'");
        if (!(raw instanceof String s))
            throw new IllegalStateException("item_shop.yml 'title' must be a string");
        return s;
    }

    private List<Category> loadItemShopCategories() {
        Object raw = itemShopConfig.read("categories");
        if (raw == null)
            throw new IllegalStateException("item_shop.yml missing 'categories'");
        if (!(raw instanceof List<?> list) || list.isEmpty())
            throw new IllegalStateException("item_shop.yml 'categories' must be a non-empty list");

        List<Category> categories = new ObjectArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map))
                throw new IllegalStateException("item_shop.yml categories[" + i + "] must be a map");
            categories.add(buildCategory(map, i));
        }
        return categories;
    }

    private Category buildCategory(Map<?, ?> map, int index) {
        String tag = "item_shop.yml categories[" + index + "]";

        ItemStack icon = parseItemStack(map.get("icon"), tag, "icon");
        Category category = new Category(icon);

        Object rawItems = map.get("items");
        if (rawItems == null)
            throw new IllegalStateException(tag + " missing 'items'");
        if (!(rawItems instanceof List<?> list))
            throw new IllegalStateException(tag + " 'items' must be a list");

        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> itemMap))
                throw new IllegalStateException(tag + " items[" + i + "] must be a map");
            category.items.add(buildShopItem(itemMap, tag + " items[" + i + "]"));
        }
        return category;
    }

    private ShopItem buildShopItem(Map<?, ?> map, String tag) {
        ItemStack show = parseItemStack(map.get("show"), tag, "show");
        Material coin  = parseMaterial(map.get("coin"), tag, "coin");
        int price      = requireInt(map, "price", tag);

        if (map.containsKey("actual")) {
            ItemStack actual = parseItemStack(map.get("actual"), tag, "actual");
            return new ShopItem(show, coin, price, actual);
        }
        return new ShopItem(show, coin, price);
    }

    // ================= 字段解析 =================

    @SuppressWarnings("unchecked")
    private static ItemStack parseItemStack(Object raw, String tag, String field) {
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException(tag + " field '" + field + "' must be a map");

        Material material = parseMaterial(map.get("material"), tag, field + ".material");
        int amount = map.containsKey("amount")
                ? ((Number) map.get("amount")).intValue()
                : 1;

        ItemStack item = new ItemStack(material, amount);

        String name = (String) map.get("name");
        List<String> lore = (List<String>) map.get("lore");

        if (name != null || lore != null) {
            item.editMeta(meta -> {
                if (name != null) meta.displayName(Component.text(name));
                if (lore != null) meta.lore(lore.stream().map(Component::text).toList());
            });
        }
        return item;
    }

    private static Material parseMaterial(Object raw, String tag, String field) {
        if (raw == null)
            throw new IllegalStateException(tag + " missing field '" + field + "'");
        if (!(raw instanceof String s))
            throw new IllegalStateException(tag + " field '" + field + "' must be a string");
        Material m = Material.matchMaterial(s);
        if (m == null)
            throw new IllegalStateException(tag + " unknown material '" + s + "'");
        return m;
    }

    private static EntityType parseEntityType(Object raw, String tag) {
        if (raw == null)
            throw new IllegalStateException(tag + " missing 'entityType'");
        if (!(raw instanceof String s))
            throw new IllegalStateException(tag + " 'entityType' must be a string");
        try {
            return EntityType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(tag + " unknown entityType '" + s + "'");
        }
    }

    private static Location parseLocation(Map<?, ?> map, String tag, World world) {
        float yaw = map.containsKey("yaw")
                ? ((Number) map.get("yaw")).floatValue()
                : 0f;

        return new Location(
                world,
                requireCoord(map, "x", tag),
                requireCoord(map, "y", tag),
                requireCoord(map, "z", tag),
                yaw,
                0f      // pitch 固定 0
        );
    }

    private static double requireCoord(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " missing '" + key + "'");
        if (!(raw instanceof Number n))
            throw new IllegalStateException(tag + " '" + key + "' must be a number");
        return n.doubleValue();
    }

    private static String requireString(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(raw instanceof String s))
            throw new IllegalStateException(tag + " field '" + key + "' must be a string");
        return s;
    }

    private static int requireInt(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(raw instanceof Number n))
            throw new IllegalStateException(tag + " field '" + key + "' must be a number");
        return n.intValue();
    }

    // ================= 生命周期 =================

    public void release() {
        itemShopConfig.release();
        shopsConfig.release();
    }
}