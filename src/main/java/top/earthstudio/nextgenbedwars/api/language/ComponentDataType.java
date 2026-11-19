package top.earthstudio.nextgenbedwars.api.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public final class ComponentDataType implements PersistentDataType<String, Component> {
    public static final ComponentDataType COMPONENT = new ComponentDataType();
    public static final PersistentDataType<List<String>, List<Component>> COMPONENT_LIST =
            PersistentDataType.LIST.listTypeFrom(COMPONENT);

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ComponentDataType() {}

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<Component> getComplexType() {
        return Component.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull Component complex, @NotNull PersistentDataAdapterContext context) {
        return MINI_MESSAGE.serialize(complex);
    }

    @Override
    public @NotNull Component fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return MINI_MESSAGE.deserialize(primitive);
    }
}