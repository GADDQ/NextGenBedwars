package top.earthstudio.nextgenbedwars.api.level;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public interface LevelManager extends IGameSubSystem {
    UUID createGroup();
    void createGroup(UUID group);
    void removeGroup(UUID group);

    void create(UUID group, String id, int maxLevel);
    void remove(UUID group, String id);

    int getLevel(UUID group, String id);
    void setMaxLevel(UUID group, String id, int maxLevel);

    void upgrade(UUID group, String id, int level);
    void downgrade(UUID group, String id, int level);

    void upgradeAll(UUID group, int level);
    void upgradeAllGroup(int level);
    void downgradeAll(UUID group, int level);
    void downgradeAllGroup(int level);

    Map<String, IntIntMutablePair> getAll(UUID group);

    @Override
    default void update() {}
}
