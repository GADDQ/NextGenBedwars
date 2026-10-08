package top.earthstudio.nextgenbedwars.core.team;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.util.ConfigUtil;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TeamConfig implements IConfig<Team> {
    private final Config config;
    private final List<Team> teamList;

    public TeamConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());
        this.config = new Config(gameFolder, "team.yml");
        this.teamList = new ObjectArrayList<>();

        loadFromConfig(config);
    }

    private void loadFromConfig(Config config) {
        Object raw = config.read("teams");
        if (raw == null) {
            return;
        }
        if (!(raw instanceof List<?> list)) {
            throw new IllegalStateException("'teams' must be a list in team.yml");
        }
        if (list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map)) {
                throw new IllegalStateException("teams[" + i + "] must be a map");
            }
            teamList.add(buildTeam(map, i));
        }
    }

    private Team buildTeam(Map<?, ?> map, int index) {
        String tag = "teams[" + index + "]";
        Component displayName = ConfigUtil.parseComponent(map.get("displayName"), tag + ".displayName");
        Color color = ConfigUtil.parseColor(map.get("color"), tag);
        int maxPlayerCount = ConfigUtil.parseInt(map, "maxPlayerCount", tag);
        int respawnTick = ConfigUtil.parseInt(map, "respawnTick", tag);
        Location respawn = ConfigUtil.parseLocation(map.get("respawn"), tag);
        LongLongPair bed = parseBed(map.get("bed"), tag);

        return new Team(displayName, color, maxPlayerCount, respawnTick, respawn, bed);
    }

    private static LongLongPair parseBed(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing 'bed'");
        if (!(raw instanceof List<?> list) || list.size() != 2) {
            throw new IllegalStateException(tag + " field 'bed' must be a list of exactly 2 {x, y, z} maps");
        }

        long first = compressBlock(list.get(0), tag + ".bed[0]");
        long second = compressBlock(list.get(1), tag + ".bed[1]");
        return LongLongPair.of(first, second);
    }

    private static long compressBlock(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException(tag + " must be a {x, y, z} map");
        }
        int x = (int) ConfigUtil.parseDouble(map, "x", tag);
        int y = (int) ConfigUtil.parseDouble(map, "y", tag);
        int z = (int) ConfigUtil.parseDouble(map, "z", tag);
        return BlockPosUtil.asLong(x, y, z);
    }

    @Override
    public List<Team> getValues() {
        return teamList;
    }

    @Override
    public void release() {
        config.release();
    }
}