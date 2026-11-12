package top.earthstudio.nextgenbedwars.api.respawn;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

@SuppressWarnings("unused")
public interface RespawnManager extends IGameSubSystem {
    void setRespawn(Player player, int tick);

    int getRemainRespawnTick(Player player);

    void addIgnore(Player player);
    void removeIgnore(Player player);
}
