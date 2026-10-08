package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.Game;

import java.io.File;
import java.util.Map;

/**
 * 从 <dataFolder>/Game/<name>/game.yml 加载 Game。
 *
 * <p>mapTemplate 固定推导为 <dataFolder>/Game/<name>/map/。
 * waitingLobby / spectatorRespawnPoint 的 world 在 GameInstanceImpl
 * 的 locationsModifier 中绑定。
 *
 * <p>load() 是一次性操作：Config 读完即 release，不持有任何状态。
 */
public final class GameConfig {
    private GameConfig() {}

    public static Game load(String name) {
        File gameFolder = new File(
                new File(BedwarsAPI.getInstance().getPlugin().getDataFolder(), "Game"),
                name
        );
        Config config = new Config(gameFolder, "game.yml");

        try {
            Component displayName = Component.text(
                    requireString(config.read("displayName"), "displayName"));
            Location waitingLobby = parseLocation(
                    config.read("waitingLobby"), "waitingLobby");
            Location spectatorRespawnPoint = parseLocation(
                    config.read("spectatorRespawnPoint"), "spectatorRespawnPoint");
            ObjectObjectImmutablePair<Vector3i, Vector3i> region =
                    parseRegion(config.read("region"));
            File mapTemplate = new File(gameFolder, "map");

            return new Game(name, displayName, mapTemplate,
                    waitingLobby, spectatorRespawnPoint, region);
        } finally {
            config.release();
        }
    }

    // ================= 解析 =================

    private static Location parseLocation(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException("game.yml '" + tag + "' must be a {x, y, z} map");
        return new Location(
                null,   // world 由 GameInstanceImpl.locationsModifier 绑
                requireCoord(map, "x", tag),
                requireCoord(map, "y", tag),
                requireCoord(map, "z", tag)
        );
    }

    private static ObjectObjectImmutablePair<Vector3i, Vector3i> parseRegion(Object raw) {
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException("game.yml 'region' must be a map");
        return new ObjectObjectImmutablePair<>(
                parseVector(map.get("min"), "region.min"),
                parseVector(map.get("max"), "region.max")
        );
    }

    private static Vector3i parseVector(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException("game.yml '" + tag + "' must be a {x, y, z} map");
        return new Vector3i(
                requireInt(map, "x", tag),
                requireInt(map, "y", tag),
                requireInt(map, "z", tag)
        );
    }

    private static String requireString(Object raw, String tag) {
        if (raw == null)
            throw new IllegalStateException("game.yml missing '" + tag + "'");
        if (!(raw instanceof String s))
            throw new IllegalStateException("game.yml '" + tag + "' must be a string");
        return s;
    }

    private static double requireCoord(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException("game.yml '" + tag + "." + key + "' missing");
        if (!(raw instanceof Number n))
            throw new IllegalStateException("game.yml '" + tag + "." + key + "' must be a number");
        return n.doubleValue();
    }

    private static int requireInt(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException("game.yml '" + tag + "." + key + "' missing");
        if (!(raw instanceof Number n))
            throw new IllegalStateException("game.yml '" + tag + "." + key + "' must be a number");
        return n.intValue();
    }
}