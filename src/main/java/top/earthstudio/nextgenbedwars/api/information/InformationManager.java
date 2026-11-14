package top.earthstudio.nextgenbedwars.api.information;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.function.Function;
import java.util.function.Supplier;

public interface InformationManager extends IGameSubSystem {
    default void create(String id, Supplier<Component> information) {
        create(id, player -> information.get());
    }

    void create(String id, Function<Player, Component> information);
    void remove(String id);

    void displayFor(Player player);
    void clearFor(Player player);
}
