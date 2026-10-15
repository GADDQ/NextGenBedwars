package top.earthstudio.nextgenbedwars.api.shop;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import java.util.function.Function;

@ConfigSerializable
public final class Shop {
    public String shopType;
    public Component title;
    public Component subTitle;
    public EntityType entityType;
    public Location location;

    public transient Function<Player, ? extends IGuiPage> shopPageProvider;

    public Shop() {}
}
