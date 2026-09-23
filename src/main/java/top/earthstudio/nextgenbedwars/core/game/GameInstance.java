package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;

import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldProtectListener;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class GameInstance {
    public Game game;
    public GameSubSystem gameSubSystem;

    public String displayName;
    public File mapTemplate;
    public UUID worldUUID;

    private WorldProtectListener worldProtectListener;
    private boolean isShutdown = false;
    private boolean isReady = false;

    public GameInstance(Game game, GameSubSystem gameSubSystem, String displayName, File mapTemplate, WorldReadyListener worldReadyListener) {
        this.game = game;
        this.gameSubSystem = gameSubSystem;

        this.displayName = displayName;
        this.mapTemplate = mapTemplate;

        worldUUID = WorldManager.create(mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            game.locationsModifier(world);
            WorldManager.forceLoadRegions(worldUUID, game.region);

            this.worldProtectListener = new WorldProtectListener(gameSubSystem.get(WorldProtector.class), world);
            Bukkit.getPluginManager().registerEvents(worldProtectListener, BedwarsAPI.getInstance().getPlugin());

            scanWorldToProtect(world, game.region);
        });
    }

    public void update() {
        if (!isReady || isShutdown) {
            return;
        }
        gameSubSystem.update();
    }

    public void shutdown() {
        isShutdown = true;

        if (worldProtectListener != null)
            HandlerList.unregisterAll(worldProtectListener);

        worldProtectListener = null;

        gameSubSystem.shutdown();
        gameSubSystem = null;

        game = null;

        WorldManager.destroy(worldUUID);

        worldUUID = null;
    }

    private void scanWorldToProtect(World world, Pair<Vector3i, Vector3i> region) {
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
                            if (isShutdown) return;

                            gameSubSystem.get(WorldProtector.class).addGroup(scannedBlocks);
                            isReady = true;
                        }
                    }.runTask(BedwarsAPI.getInstance().getPlugin());
                }).exceptionally(throwable -> {
                    ComponentLogger.logger().error("Scan chunk async failed!", throwable);
                    if (isShutdown) return null;

                    isReady = true;
                    return null;
                });;
    }
}
