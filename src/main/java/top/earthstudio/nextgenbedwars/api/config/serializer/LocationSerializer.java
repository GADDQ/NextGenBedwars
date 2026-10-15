package top.earthstudio.nextgenbedwars.api.config.serializer;

import org.bukkit.Location;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class LocationSerializer implements TypeSerializer<Location> {
    @Override
    public Location deserialize(Type type, ConfigurationNode node) {
        double x = node.node("x").getDouble(0.0);
        double y = node.node("y").getDouble(0.0);
        double z = node.node("z").getDouble(0.0);
        float yaw = node.node("yaw").getFloat(0.0f);
        float pitch = node.node("pitch").getFloat(0.0f);
        return new Location(null, x, y, z, yaw, pitch);
    }

    @Override
    public void serialize(Type type, Location obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        node.node("x").set(obj.getX());
        node.node("y").set(obj.getY());
        node.node("z").set(obj.getZ());
        if (obj.getYaw() != 0.0f) node.node("yaw").set((double) obj.getYaw());
        if (obj.getPitch() != 0.0f) node.node("pitch").set((double) obj.getPitch());
    }
}