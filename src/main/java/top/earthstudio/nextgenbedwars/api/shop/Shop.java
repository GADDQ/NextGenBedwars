package top.earthstudio.nextgenbedwars.api.shop;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;

import java.util.function.Supplier;

public final class Shop {
    public Component title;
    public Component subTitle;
    public EntityType entityType;
    public Location location;
    public Supplier<ChestGuiPage> shopPageProvider;

    public Shop(Component title, Component subTitle, EntityType entityType, Location location, Supplier<ChestGuiPage> shopPageProvider) {
        this.title = title;
        this.subTitle = subTitle;
        this.entityType = entityType;
        this.location = location;
        this.shopPageProvider = shopPageProvider;

    }

    public Shop(Component title, EntityType entityType, Location location, Supplier<ChestGuiPage> shopPageProvider) {
        this(title, Component.text(""), entityType, location, shopPageProvider);
    }
}
