package top.earthstudio.nextgenbedwars.api.information;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface InformationManager extends IGameSubSystem {
    default void create(String id, Supplier<Component> informationHook) {
        createLines(id, player -> List.of(informationHook.get()));
    }

    default void create(String id, Function<Player, Component> informationHook) {
        createLines(id, player -> List.of(informationHook.apply(player)));
    }

    default void createLines(String id, Supplier<List<Component>> informationHook) {
        createLines(id, player -> informationHook.get());
    }

    void createLines(String id, Function<Player, List<Component>> informationHook);
    void remove(String id);

    void displayFor(Player player);
    void clearFor(Player player);
}
