package top.earthstudio.nextgenbedwars.core.prop.builtin.platform;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;

import org.bukkit.event.HandlerList;
import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.PlatformManager;
import top.earthstudio.nextgenbedwars.api.util.BlockUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;
import top.earthstudio.nextgenbedwars.core.prop.builtin.platform.listener.PlatformListener;

import java.util.Map;

public class PlatformManagerImpl implements PlatformManager {
    private Map<PlatformProp, Integer> platforms;
    private Map<PlatformProp, LongArrayList> blockLongsMap;
    private ObjectArrayList<PlatformProp> pendingRemoves;

    private final int disappearTick = 200; // TODO: configurable

    private GameInstance gameInstance;
    private WorldProtector worldProtector;

    private PlatformListener platformListener;

    public PlatformManagerImpl(GameInstance gameInstance) {
        this.gameInstance = gameInstance;

        platforms = new Object2IntOpenHashMap<>();
        blockLongsMap = new Object2ObjectOpenHashMap<>();
        pendingRemoves = new ObjectArrayList<>();

        platformListener = new PlatformListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(platformListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void onReady() {
        worldProtector = gameInstance.get(WorldProtector.class);
    }

    @Override
    public void update() {
        for (var entry : platforms.entrySet()) {
            if (entry.getValue() <= 0) {
                pendingRemoves.add(entry.getKey());
                continue;
            }
            entry.setValue(entry.getValue() - 1);
        }

        if (!pendingRemoves.isEmpty()) {
            for (PlatformProp platform : pendingRemoves) {
                removeBlockFor(platform);
                platforms.remove(platform);
            }
            pendingRemoves.clear();
        }
    }

    @Override
    public void use(PlatformProp platform) {
        placeBlockFor(platform);
        platforms.put(platform, disappearTick);
    }

    @Override
    public LongArrayList getAll() {
        LongArrayList list = new LongArrayList();
        blockLongsMap.forEach((platform, longs) -> list.addAll(longs));
        return list;
    }

    @Override
    public boolean contains(Block block) {
        for (var list : blockLongsMap.values()) {
            if (list.contains(BlockUtil.asLong(block)))
                return true;
        }
        return false;
    }

    @Override
    public void remove(Block block) {
        for (var entry : blockLongsMap.entrySet()) {
            if (entry.getValue().rem(BlockUtil.asLong(block)))
                return;
        }
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(platformListener);
        platformListener = null;

        pendingRemoves.clear();
        pendingRemoves = null;

        for (PlatformProp platform : platforms.keySet()) {
            removeBlockFor(platform);
        }

        blockLongsMap.clear();
        blockLongsMap = null;

        platforms.clear();
        platforms = null;

        worldProtector = null;
        gameInstance = null;
    }

    private void placeBlockFor(PlatformProp platform) {
        LongArrayList list = new LongArrayList();
        Block center = platform.centerLocation.getBlock();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = center.getRelative(x, 0, z);
                long blockLong = tryReplace(block);
                if (blockLong != -1)
                    list.add(blockLong);


                if (x != 0 && z != 0) {
                    block = center.getRelative(x * 2, 0, z);
                    blockLong = tryReplace(block);
                    if (blockLong != -1)
                        list.add(blockLong);

                    block = center.getRelative(x, 0, z * 2);
                    blockLong = tryReplace(block);
                    if (blockLong != -1)
                        list.add(blockLong);
                }
            }
        }

        blockLongsMap.put(platform, list);
    }

    private void removeBlockFor(PlatformProp platform) {
        LongArrayList list = blockLongsMap.remove(platform);
        if (list == null) return;

        World world = gameInstance.getWorld();
        if (world == null) return;

        for (int i = 0; i < list.size(); i++) {
            Block block = BlockUtil.getBlock(world, list.getLong(i));
            if (block.getType() == Material.SLIME_BLOCK) {
                block.setType(Material.AIR);
            }
        }
    }

    private long tryReplace(Block block) {
        if (!Tag.AIR.isTagged(block.getType()))
            return -1;

        long blockLong = BlockUtil.asLong(block.getX(), block.getY(), block.getZ());
        if (worldProtector.contains(blockLong))
            return -1;

        block.setType(Material.SLIME_BLOCK);
        return blockLong;
    }
}