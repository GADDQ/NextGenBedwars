package top.earthstudio.nextgenbedwars.api.config.serializer;

import org.bukkit.Color;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ColorSerializer implements TypeSerializer<Color> {
    @Override
    public Color deserialize(Type type, ConfigurationNode node) {
        String hex = node.getString("#FFFFFF");
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        try {
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }

    @Override
    public void serialize(Type type, Color obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set("#FFFFFF");
        } else {
            node.set(String.format("#%02X%02X%02X", obj.getRed(), obj.getGreen(), obj.getBlue()));
        }
    }
}