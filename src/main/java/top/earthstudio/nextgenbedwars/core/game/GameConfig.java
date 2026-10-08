package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.joml.Vector3i;
import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.ConfigParser;

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
            // 1. 支持 MiniMessage / & 颜色代码的房间展示名
            Component displayName = ConfigParser.parseComponent(
                    config.read("displayName"), "game.yml.displayName");

            // 2. 复用 ConfigParser：自动兼顾 yaw / pitch 视角朝向！
            Location waitingLobby = ConfigParser.parseLocation(
                    config.read("waitingLobby"), "game.yml.waitingLobby");
            Location spectatorRespawnPoint = ConfigParser.parseLocation(
                    config.read("spectatorRespawnPoint"), "game.yml.spectatorRespawnPoint");

            // 3. 选区包围盒
            ObjectObjectImmutablePair<Vector3i, Vector3i> region =
                    parseRegion(config.read("region"));

            File mapTemplate = new File(gameFolder, "map");

            return new Game(name, displayName, mapTemplate,
                    waitingLobby, spectatorRespawnPoint, region);
        } finally {
            config.release();
        }
    }

    // ================= 选区解析 =================

    private static ObjectObjectImmutablePair<Vector3i, Vector3i> parseRegion(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException("game.yml 'region' must be a map");
        }
        return new ObjectObjectImmutablePair<>(
                parseVector(map.get("min"), "region.min"),
                parseVector(map.get("max"), "region.max")
        );
    }

    private static Vector3i parseVector(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException("game.yml '" + tag + "' must be a {x, y, z} map");
        }
        return new Vector3i(
                ConfigParser.parseInt(map, "x", tag),
                ConfigParser.parseInt(map, "y", tag),
                ConfigParser.parseInt(map, "z", tag)
        );
    }
}