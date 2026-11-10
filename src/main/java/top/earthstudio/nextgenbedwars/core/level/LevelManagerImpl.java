package top.earthstudio.nextgenbedwars.core.level;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.level.LevelManager;

import java.util.Map;
import java.util.UUID;

public class LevelManagerImpl implements LevelManager {
    private Map<UUID, Map<String, IntIntMutablePair>> levelGroups;

    public LevelManagerImpl(GameInstance gameInstance) {
        levelGroups = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public UUID createGroup() {
        UUID uuid = UUID.randomUUID();
        levelGroups.put(uuid, new Object2ObjectOpenHashMap<>());
        return uuid;
    }

    @Override
    public void createGroup(UUID group) {
        levelGroups.putIfAbsent(group, new Object2ObjectOpenHashMap<>());
    }

    @Override
    public void removeGroup(UUID group) {
        levelGroups.remove(group).clear();
    }

    @Override
    public void create(UUID group, String id, int maxLevel) {
        levelGroups.get(group).putIfAbsent(id, new IntIntMutablePair(0, maxLevel));
    }

    @Override
    public void remove(UUID group, String id) {
        levelGroups.get(group).remove(id);
    }

    @Override
    public int getLevel(UUID group, String id) {
        return levelGroups.get(group).get(id).firstInt();
    }

    @Override
    public void setMaxLevel(UUID group, String id, int maxLevel) {
        levelGroups.get(group).get(id).second(maxLevel);
    }

    @Override
    public void upgrade(UUID group, String id, int level) {
        var pair = levelGroups.get(group).get(id);
        pair.first(Math.min(pair.firstInt() + level, pair.secondInt()));
    }

    @Override
    public void downgrade(UUID group, String id, int level) {
        var pair = levelGroups.get(group).get(id);
        pair.first(Math.max(pair.firstInt() - level, 0));
    }

    @Override
    public void upgradeAll(UUID group, int level) {
        levelGroups.get(group).forEach((id, pair) -> pair.first(Math.min(pair.firstInt() + level, pair.secondInt())));
    }

    @Override
    public void upgradeAllGroup(int level) {
        levelGroups.forEach((group, map) -> upgradeAll(group, level));
    }

    @Override
    public void downgradeAll(UUID group, int level) {
        levelGroups.get(group).forEach((id, pair) -> pair.first(Math.max(pair.firstInt() - level, 0)));
    }

    @Override
    public void downgradeAllGroup(int level) {
        levelGroups.forEach((group, map) -> downgradeAll(group, level));
    }

    @Override
    public Map<String, IntIntMutablePair> getAll(UUID group) {
        return levelGroups.get(group);
    }

    @Override
    public void shutdown() {
        levelGroups.clear();
        levelGroups = null;
    }
}
