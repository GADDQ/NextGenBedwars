package top.earthstudio.nextgenbedwars.core.world;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WorldProtectorImpl implements WorldProtector {
    // 用于 O(1) 按 UUID 查找与移除
    private Map<UUID, LongSet> protectGroups;

    // 用于 O(1) 快速遍历
    private Long2IntOpenHashMap refCounts;

    @Override
    public void initialize() {
        protectGroups = new Object2ObjectOpenHashMap<>();
        refCounts = new Long2IntOpenHashMap();
        refCounts.defaultReturnValue(0);
    }

    @Override
    public UUID addGroup(LongSet group) {
        UUID uuid = UUID.randomUUID();
        protectGroups.put(uuid, group);

        // 遍历该组的方块，计数全部 +1
        LongIterator it = group.iterator();
        while (it.hasNext())
            refCounts.addTo(it.nextLong(), 1);

        return uuid;
    }

    @Override
    public void removeGroup(UUID uuid) {
        LongSet group = protectGroups.remove(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        // 只减去这个组包含的方块计数
        LongIterator it = group.iterator();
        while (it.hasNext()) {
            long blockLong = it.nextLong();
            int currentCount = refCounts.addTo(blockLong, -1); // 计数减 1

            // 如果减完后计数归零，说明所有组都不再保护它，移出哈希表
            if (currentCount <= 0) {
                refCounts.remove(blockLong);
            }
        }
    }

    @Override
    public boolean contains(long blockLong) {
        return refCounts.get(blockLong) > 0;
    }

    @Override
    public List<UUID> getOwnerGroupUuids(long blockLong) {
        List<UUID> owners = new ObjectArrayList<>();
        for (Map.Entry<UUID, LongSet> entry : protectGroups.entrySet()) {
            if (entry.getValue().contains(blockLong)) {
                owners.add(entry.getKey());
            }
        }
        return owners.isEmpty() ? null : owners;
    }

    @Override
    public void addBlockToGroup(UUID uuid, long blockLong) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        if (group.add(blockLong))
            refCounts.addTo(blockLong, 1);
    }

    @Override
    public void addBlocksToGroup(UUID uuid, LongSet blockLongs) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        LongIterator it = blockLongs.iterator();
        while (it.hasNext()) {
            long block = it.nextLong();
            if (group.add(block))
                refCounts.addTo(block, 1);
        }
    }

    @Override
    public void addRegionToGroup(UUID uuid, Pair<Vector3i, Vector3i> region) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        Vector3i loc1 = region.left();
        Vector3i loc2 = region.right();
        int minX = Math.min(loc1.x, loc2.x), maxX = Math.max(loc1.x, loc2.x);
        int minY = Math.min(loc1.y, loc2.y), maxY = Math.max(loc1.y, loc2.y);
        int minZ = Math.min(loc1.z, loc2.z), maxZ = Math.max(loc1.z, loc2.z);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    long block = BlockPosUtil.asLong(x, y, z);
                    if (group.add(block))
                        refCounts.addTo(block, 1);
                }
            }
        }
    }

    @Override
    public void removeBlockFromGroup(UUID uuid, long blockLong) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        if (group.remove(blockLong)) {
            int currentCount = refCounts.addTo(blockLong, -1);
            if (currentCount <= 1) {
                refCounts.remove(blockLong);
            }
        }
    }

    @Override
    public void removeBlocksFromGroup(UUID uuid, LongSet blockLongs) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        LongIterator it = blockLongs.iterator();
        while (it.hasNext()) {
            long block = it.nextLong();
            if (group.remove(block)) {
                int currentCount = refCounts.addTo(block, -1);
                if (currentCount <= 1)
                    refCounts.remove(block);
            }
        }
    }
    
    @Override
    public void removeRegionFromGroup(UUID uuid, Pair<Vector3i, Vector3i> region) {
        LongSet group = protectGroups.get(uuid);
        if (group == null) throw new IllegalArgumentException("No such group!");

        Vector3i loc1 = region.left();
        Vector3i loc2 = region.right();
        int minX = Math.min(loc1.x, loc2.x), maxX = Math.max(loc1.x, loc2.x);
        int minY = Math.min(loc1.y, loc2.y), maxY = Math.max(loc1.y, loc2.y);
        int minZ = Math.min(loc1.z, loc2.z), maxZ = Math.max(loc1.z, loc2.z);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    long block = BlockPosUtil.asLong(x, y, z);
                    if (group.remove(block)) {
                        int currentCount = refCounts.addTo(block, -1);
                        if (currentCount <= 1)
                            refCounts.remove(block);
                    }
                }
            }
        }
    }

    @Override
    public void scanWorldToProtectLater(World world, Pair<Vector3i, Vector3i> region, Consumer<UUID> consumer) {
        Vector3i loc1 = region.left();
        Vector3i loc2 = region.right();

        int minChunkX = Math.min(loc1.x, loc2.x) >> 4;
        int maxChunkX = Math.max(loc1.x, loc2.x) >> 4;
        int minChunkZ = Math.min(loc1.z, loc2.z) >> 4;
        int maxChunkZ = Math.max(loc1.z, loc2.z) >> 4;

        List<CompletableFuture<ChunkSnapshot>> snapshotFutures = new ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                // Paper 原生异步加载区块，加载完后在线程池里直接提取只读快照
                snapshotFutures.add(
                        world.getChunkAtAsync(cx, cz).thenApply(Chunk::getChunkSnapshot).exceptionally(throwable -> {
                            ComponentLogger.logger().error("Get ChunkSnapshot async failed!", throwable);
                            return null;
                        })
                );
            }
        }

        CompletableFuture.allOf(snapshotFutures.toArray(new CompletableFuture[0]))
                .thenAcceptAsync(v -> {
                    LongSet scannedBlocks = new LongOpenHashSet();
                    int minHeight = world.getMinHeight();
                    int maxHeight = world.getMaxHeight();

                    for (CompletableFuture<ChunkSnapshot> future : snapshotFutures) {
                        ChunkSnapshot snapshot = future.join();

                        int baseBlockX = snapshot.getX() << 4;
                        int baseBlockZ = snapshot.getZ() << 4;

                        for (int y = minHeight; y < maxHeight; y += 16) {
                            // 如果 16 层全是空气，跳过
                            int sectionIndex = (y - minHeight) >> 4;
                            if (snapshot.isSectionEmpty(sectionIndex)) {
                                continue;
                            }

                            // 只有非空区域才细扫每一个方块
                            for (int dy = 0; dy < 16; dy++) {
                                int currentY = y + dy;
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        Material type = snapshot.getBlockType(x, currentY, z);
                                        // 跳过空气和可被替换方块（如：草，花）
                                        if (!type.isAir() && !Tag.REPLACEABLE.isTagged(type)) {
                                            long posKey = BlockPosUtil.asLong(baseBlockX + x, currentY, baseBlockZ + z);
                                            scannedBlocks.add(posKey);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(addGroup(scannedBlocks));
                        }
                    }.runTask(BedwarsAPI.getInstance().getPlugin());
                }).exceptionally(throwable -> {
                    ComponentLogger.logger().warn("Scan chunk async failed! World protection will NOT apply!", throwable);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(null);
                        }
                    }.runTask(BedwarsAPI.getInstance().getPlugin());
                    return null;
                });
    }

    @Override
    public void shutdown() {
        protectGroups.clear();
        protectGroups = null;
        refCounts.clear();
        refCounts = null;
    }
}
