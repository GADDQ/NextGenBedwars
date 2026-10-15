package top.earthstudio.nextgenbedwars.api.config.serializer;

import org.joml.Vector3i;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class Vector3iSerializer implements TypeSerializer<Vector3i> {
    @Override
    public Vector3i deserialize(Type type, ConfigurationNode node) {
        return new Vector3i(
                node.node("x").getInt(0),
                node.node("y").getInt(0),
                node.node("z").getInt(0)
        );
    }

    @Override
    public void serialize(Type type, Vector3i obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        node.node("x").set(obj.x);
        node.node("y").set(obj.y);
        node.node("z").set(obj.z);
    }
}