package top.earthstudio.nextgenbedwars.api.config.serializer;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.joml.Vector3i;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@SuppressWarnings("rawtypes")
public class RegionSerializer implements TypeSerializer<ObjectObjectImmutablePair> {

    @Override
    public ObjectObjectImmutablePair deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Vector3i min = node.node("min").get(Vector3i.class);
        Vector3i max = node.node("max").get(Vector3i.class);
        if (min == null || max == null) {
            return new ObjectObjectImmutablePair<>(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
        }
        return new ObjectObjectImmutablePair<>(min, max);
    }

    @Override
    public void serialize(Type type, ObjectObjectImmutablePair obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        node.node("min").set(Vector3i.class, (Vector3i) obj.left());
        node.node("max").set(Vector3i.class, (Vector3i) obj.right());
    }
}