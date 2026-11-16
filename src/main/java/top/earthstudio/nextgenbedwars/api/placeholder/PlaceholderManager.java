package top.earthstudio.nextgenbedwars.api.placeholder;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.function.Function;

@SuppressWarnings("unused")
public interface PlaceholderManager extends IGameSubSystem {
    void create(String id, Function<Player, Component> placeholderHook);
    void remove(String id);

    Component get(String id);
    Component get(String id, Player player);

    Component resolve(Component template);
    Component resolve(Component template, Player player);

    @Override
    default void update() {}
}
