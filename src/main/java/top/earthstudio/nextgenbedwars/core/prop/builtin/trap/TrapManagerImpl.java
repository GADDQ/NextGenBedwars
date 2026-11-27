package top.earthstudio.nextgenbedwars.core.prop.builtin.trap;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.trap.TrapManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.util.BlockUtil;

import top.earthstudio.nextgenbedwars.core.prop.builtin.trap.listener.TrapListener;

import java.util.Map;

public class TrapManagerImpl implements TrapManager {
    private TrapListener trapListener;

    private Map<Long, Team> posList = new Long2ObjectOpenHashMap<>();

    public TrapManagerImpl(GameInstance gameInstance) {
        trapListener = new TrapListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(trapListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void add(Block block, Team team) {
        posList.put(BlockUtil.asLong(block), team);
    }

    @Override
    public boolean contains(Block block) {
        return posList.containsKey(BlockUtil.asLong(block));
    }

    @Override
    public Team getTeam(Block block) {
        return posList.get(BlockUtil.asLong(block));
    }

    @Override
    public Team remove(Block block) {
        return posList.remove(BlockUtil.asLong(block));
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(trapListener);
        trapListener = null;

        posList.clear();
        posList = null;
    }
}
