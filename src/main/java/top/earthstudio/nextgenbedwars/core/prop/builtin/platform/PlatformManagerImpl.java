package top.earthstudio.nextgenbedwars.core.prop.builtin.platform;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.PlatformManager;
import top.earthstudio.nextgenbedwars.api.util.BlockUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import java.util.Map;

public class PlatformManagerImpl implements PlatformManager {
    private Map<PlatformProp, Integer> platforms;
    private Map<PlatformProp, LongList> blockLongsList;
    private ObjectArrayList<PlatformProp> pendingRemoves;

    private int disappearTick = 200;

    private GameInstance gameInstance;
    private WorldProtector worldProtector;

    public PlatformManagerImpl(GameInstance gameInstance) {
        this.gameInstance = gameInstance;

        platforms = new Object2IntOpenHashMap<>();
        blockLongsList = new Object2ObjectOpenHashMap<>();
        pendingRemoves = new ObjectArrayList<>();
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
        blockLongsList.forEach((platform, longs) -> list.addAll(longs));
        return list;
    }

    @Override
    public void shutdown() {
        for (PlatformProp platform : platforms.keySet()) {
            removeBlockFor(platform);
        }

        pendingRemoves.clear();
        pendingRemoves = null;

        blockLongsList.clear();
        blockLongsList = null;

        platforms.clear();
        platforms = null;

        worldProtector = null;
        gameInstance = null;
    }

    private void placeBlockFor(PlatformProp platform) {
        LongList list = new LongArrayList();
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

        blockLongsList.put(platform, list);
    }

    private void removeBlockFor(PlatformProp platform) {
        LongList list = blockLongsList.remove(platform);
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
        if (Tag.BEDS.isTagged(block.getType()))
            return -1;

        long blockLong = BlockUtil.asLong(block.getX(), block.getY(), block.getZ());
        if (worldProtector.contains(blockLong))
            return -1;

        block.setType(Material.SLIME_BLOCK);
        return blockLong;
    }
}