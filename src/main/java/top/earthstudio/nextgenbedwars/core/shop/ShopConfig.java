package top.earthstudio.nextgenbedwars.core.shop;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.ConfigurateHelper;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import top.earthstudio.nextgenbedwars.core.shop.item.ItemShopPage;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class ShopConfig implements IConfig<Shop> {
    private final GameInstance gameInstance;
    private final File shopsFile;
    private final YamlConfigurationLoader shopsLoader;
    private final File gameFolder;
    private final List<Shop> shopList;

    public ShopConfig(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.gameFolder = GameFolder.of(gameInstance.getGame());
        this.shopsFile = new File(gameFolder, "shops.yml");
        this.shopsLoader = ConfigurateHelper.loader(shopsFile);
        this.shopList = new ObjectArrayList<>();

        loadFromConfig();
    }

    private void loadFromConfig() {
        if (!shopsFile.exists()) return;

        try {
            CommentedConfigurationNode root = shopsLoader.load();
            List<Shop> loaded = root.node("shops").getList(Shop.class);
            if (loaded == null) return;

            for (Shop shop : loaded) {
                shop.shopPageProvider = providerFor(shop.shopType);
                shopList.add(shop);
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load shops.yml", e);
        }
    }

    private Function<org.bukkit.entity.Player, ? extends IGuiPage> providerFor(String type) {
        return switch (type) {
            case "item_shop" -> {
                File globalItemShop = new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "item_shop.yml");
                File localItemShop = new File(gameFolder, "item_shop.yml");

                try {
                    CommentedConfigurationNode globalNode = ConfigurateHelper.loader(globalItemShop).load();
                    CommentedConfigurationNode effective = localItemShop.exists()
                            ? globalNode.copy().mergeFrom(ConfigurateHelper.loader(localItemShop).load())
                            : globalNode;

                    Component guiTitle = effective.node("title").get(Component.class);

                    List<Category> categories = effective.node("categories").getList(Category.class);

                    yield player -> new ItemShopPage(gameInstance, player, guiTitle, categories);
                } catch (ConfigurateException e) {
                    throw new RuntimeException("Failed to load item_shop.yml", e);
                }
            }
            default -> throw new IllegalStateException("Unknown shop type: " + type);
        };
    }

    @Override
    public List<Shop> getValues() {
        return shopList;
    }

    @Override
    public void release() {
        shopList.clear();
    }

    @Override
    public void save(List<Shop> values) {
        try {
            CommentedConfigurationNode root = shopsLoader.load();
            root.node("shops").setList(Shop.class, values);
            shopsLoader.save(root);

            shopList.clear();
            shopList.addAll(values);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to save shops.yml", e);
        }
    }
}