package top.earthstudio.nextgenbedwars.api.config.serializer;

import org.bukkit.Color;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

import java.lang.reflect.Type;

public class ColorSerializer implements TypeSerializer<Color> {

    @Override
    public Color deserialize(Type type, ConfigurationNode node) {
        return ColorUtil.parseColor(node.getString("#FFFFFF"), Color.WHITE);
    }

    @Override
    public void serialize(Type type, Color obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set("#FFFFFF");
        } else {
            node.set(ColorUtil.colorToHex(obj));
        }
    }
}