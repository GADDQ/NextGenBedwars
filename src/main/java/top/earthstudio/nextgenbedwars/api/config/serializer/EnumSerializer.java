package top.earthstudio.nextgenbedwars.api.config.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumSerializer implements TypeSerializer<Enum<?>> {

    @Override
    public Enum<?> deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String val = node.getString();
        if (val == null || val.isBlank()) {
            return null;
        }

        if (type instanceof Class<?> clazz && clazz.isEnum()) {
            Class<? extends Enum> enumClass = clazz.asSubclass(Enum.class);
            try {
                return Enum.valueOf(enumClass, val.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new SerializationException(node, type, "Unknown enum constant '" + val + "' for enum " + clazz.getSimpleName());
            }
        }
        return null;
    }

    @Override
    public void serialize(Type type, @Nullable Enum<?> obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
        } else {
            node.set(obj.name().toUpperCase(Locale.ROOT));
        }
    }
}