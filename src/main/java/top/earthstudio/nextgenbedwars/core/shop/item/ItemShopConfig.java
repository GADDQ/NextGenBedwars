package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;

import java.util.List;
import java.util.function.Function;

public class ItemShopConfig extends Config<List<Category>> {
    private final GameInstance gameInstance;
    private final List<Category> categories;

    public ItemShopConfig(GameInstance gameInstance) {
        super(gameInstance, "item_shop.yml", true, "itemShop");
        this.gameInstance = gameInstance;
        this.categories = new ObjectArrayList<>();
    }

    @Override
    public List<Category> load() {
        categories.clear();
        categories.addAll(super.load());
        return categories;
    }

    // TODO: additional save function?

    public Function<Player, ? extends IGuiPage> getProvider() { // TODO: i18n
        return player -> new ItemShopPage(gameInstance, player, Component.text("物品商店").color(NamedTextColor.GOLD), categories);
    }
}
