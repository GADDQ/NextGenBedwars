package top.earthstudio.nextgenbedwars.core.shop;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.Shop;

import java.util.List;

public class ShopConfig extends IConfig<List<Shop>> {
    protected ShopConfig(GameInstance gameInstance) {
        super(gameInstance, "shops.yml", false, "shops");
    }

    @Override
    public List<Shop> load() {
        return super.load();
    }

    @Override
    public void save(List<Shop> object) {
        super.save(object);
    }
}
