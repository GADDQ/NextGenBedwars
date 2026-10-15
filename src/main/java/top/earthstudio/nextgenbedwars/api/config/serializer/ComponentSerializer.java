package top.earthstudio.nextgenbedwars.api.config.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ComponentSerializer implements TypeSerializer<Component> {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public Component deserialize(Type type, ConfigurationNode node) {
        String str = node.getString("");
        return str.isEmpty() ? Component.empty() : MINI_MESSAGE.deserialize(str);
    }

    @Override
    public void serialize(Type type, Component obj, ConfigurationNode node) throws SerializationException {
        if (obj == null || obj.equals(Component.empty())) {
            node.raw(null);
        } else {
            node.set(MINI_MESSAGE.serialize(obj));
        }
    }
}