package top.earthstudio.nextgenbedwars.core.team;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TeamConfig {
    private final Config config;
    public final List<Team> teamList;

    public TeamConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());

        this.config = new Config(gameFolder, "team.yml");
        this.teamList = new ObjectArrayList<>();

        loadFromConfig(config);
    }

    private void loadFromConfig(Config config) {
        Object raw = config.read("teams");
        if (raw == null)
            throw new IllegalStateException(
                    "Missing 'teams' in team.yml for game '"
                            + config.parentFolder.getName() + "'");

        if (!(raw instanceof List<?> list))
            throw new IllegalStateException("'teams' must be a list in team.yml");

        // 空列表允许 —— 表示"这张图还没编辑队伍"
        if (list.isEmpty())
            return;

        for (int i = 0; i < list.size(); i++) {
            Object entry = list.get(i);
            if (!(entry instanceof Map<?, ?> map))
                throw new IllegalStateException(
                        "teams[" + i + "] must be a map");

            teamList.add(buildTeam(map, i));
        }
    }

    private Team buildTeam(Map<?, ?> map, int index) {
        // displayName 优先读，它同时用作后续错误信息的标识
        String displayName = requireString(map, "displayName", "teams[" + index + "]");
        String tag = "team '" + displayName + "'";

        Color color = parseColor(map.get("color"), tag);
        int maxPlayerCount = requireInt(map, "maxPlayerCount", tag);
        int respawnTick = requireInt(map, "respawnTick", tag);
        Location respawn = parseLocation(map.get("respawn"), tag);
        LongLongPair bed = parseBed(map.get("bed"), tag);

        return new Team(
                Component.text(displayName),
                color,
                maxPlayerCount,
                respawnTick,
                respawn,
                bed
        );
    }

    // ================= 字段解析 =================

    private static String requireString(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(raw instanceof String s))
            throw new IllegalStateException(
                    tag + " field '" + key + "' must be a string, got "
                            + raw.getClass().getSimpleName());
        return s;
    }

    private static int requireInt(Map<?, ?> map, String key, String tag) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(raw instanceof Number n))
            throw new IllegalStateException(
                    tag + " field '" + key + "' must be a number, got "
                            + raw.getClass().getSimpleName());
        return n.intValue();
    }

    private static Color parseColor(Object raw, String tag) {
        if (raw == null)
            throw new IllegalStateException(tag + " missing 'color'");
        if (!(raw instanceof String s))
            throw new IllegalStateException(
                    tag + " field 'color' must be a string, got "
                            + raw.getClass().getSimpleName());

        String hex = s.startsWith("#") ? s.substring(1) : s;
        try {
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    tag + " has invalid color '" + s + "', expected '#RRGGBB'");
        }
    }

    private static Location parseLocation(Object raw, String tag) {
        if (raw == null)
            throw new IllegalStateException(tag + " missing 'respawn'");
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException(
                    tag + " field 'respawn' must be a {x, y, z} map");

        return new Location(
                null,   // world 由 TeamManagerImpl.add 时绑定
                requireCoord(map, "x", tag, "respawn"),
                requireCoord(map, "y", tag, "respawn"),
                requireCoord(map, "z", tag, "respawn")
        );
    }

    private static LongLongPair parseBed(Object raw, String tag) {
        if (raw == null)
            throw new IllegalStateException(tag + " missing 'bed'");
        if (!(raw instanceof List<?> list) || list.size() != 2)
            throw new IllegalStateException(
                    tag + " field 'bed' must be a list of exactly 2 {x, y, z}");

        long first  = compressBlock(list.get(0), tag, "bed[0]");
        long second = compressBlock(list.get(1), tag, "bed[1]");
        return LongLongPair.of(first, second);
    }

    private static long compressBlock(Object raw, String tag, String field) {
        if (!(raw instanceof Map<?, ?> map))
            throw new IllegalStateException(
                    tag + " field '" + field + "' must be a {x, y, z} map");

        int x = (int) requireCoord(map, "x", tag, field);
        int y = (int) requireCoord(map, "y", tag, field);
        int z = (int) requireCoord(map, "z", tag, field);
        return BlockPosUtil.asLong(x, y, z);
    }

    private static double requireCoord(Map<?, ?> map, String key, String tag, String field) {
        Object raw = map.get(key);
        if (raw == null)
            throw new IllegalStateException(tag + " field '" + field + "." + key + "' missing");
        if (!(raw instanceof Number n))
            throw new IllegalStateException(
                    tag + " field '" + field + "." + key + "' must be a number, got "
                            + raw.getClass().getSimpleName());
        return n.doubleValue();
    }

    // ================= 生命周期 =================

    public void release() {
        config.release();
    }
}