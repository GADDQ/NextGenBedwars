package top.earthstudio.nextgenbedwars.api.shop;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.UUID;

@ConfigSerializable
public final class Shop {
    public String shopType;
    public Component title;
    public Component subTitle;
    public EntityType entityType;
    public Location location;

    public transient UUID uuid;

    public Shop() {}
}
