package top.earthstudio.nextgenbedwars.api.config;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import org.joml.Vector3i;

import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.NamingSchemes;

import top.earthstudio.nextgenbedwars.api.config.serializer.*;

public final class ConfigurateSerializers {
    private ConfigurateSerializers() {}

    public static TypeSerializerCollection createCollection() {
        return TypeSerializerCollection.defaults().childBuilder()
                .registerAnnotatedObjects(ObjectMapper.factoryBuilder()
                        .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                        .build())
                .register(Component.class, new ComponentSerializer())
                .register(Location.class, new LocationSerializer())
                .register(Color.class, new ColorSerializer())
                .register(Vector3i.class, new Vector3iSerializer())
                .register(LongLongPair.class, new LongLongPairSerializer())
                .register(ItemStack.class, new ItemStackSerializer())
                .register(ObjectObjectImmutablePair.class, new RegionSerializer())
                .register(type -> type instanceof Class<?> clazz && clazz.isEnum(), new EnumSerializer())
                .build();
    }
}