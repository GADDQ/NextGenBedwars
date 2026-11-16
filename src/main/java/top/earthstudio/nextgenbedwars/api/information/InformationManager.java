package top.earthstudio.nextgenbedwars.api.information;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

public interface InformationManager extends IGameSubSystem {
    void displayFor(Player player);
    void clearFor(Player player);
}