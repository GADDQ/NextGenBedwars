package top.earthstudio.nextgenbedwars.core.world;

import it.unimi.dsi.fastutil.Pair;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.codehaus.plexus.util.FileUtils;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.world.event.BedwarsWorldReadyEvent;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    private static JavaPlugin plugin;
    private static Map<UUID, World> worlds;

    private static Set<UUID> pendingUuids;
    private static Set<UUID> pendingDestroyUuids;
    private static Set<UUID> exportingUuids;
    private static Map<UUID, File> pendingDestroyAfterExport;

    private WorldManager() {}

    static public void initialize(JavaPlugin plugin) {
        WorldManager.plugin = plugin;
        pendingUuids = ConcurrentHashMap.newKeySet();
        pendingDestroyUuids = ConcurrentHashMap.newKeySet();
        exportingUuids = ConcurrentHashMap.newKeySet();
        pendingDestroyAfterExport = new ConcurrentHashMap<>();
        worlds = new ConcurrentHashMap<>();

        cleanupOrphanedWorlds();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static public UUID create(File mapTemplate) {
        UUID uuid = UUID.randomUUID();
        String worldName = "bw-" + uuid;
        File targetDir = new File(Bukkit.getWorldContainer(), worldName);

        pendingUuids.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyDirectoryStructure(mapTemplate, targetDir);
                    new File(targetDir, "uid.dat").delete();
                    new File(targetDir, "session.lock").delete();
                } catch (IOException e) {
                    pendingUuids.remove(uuid);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, null, false));
                        }
                    }.runTask(plugin);
                    throw new RuntimeException(e);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (pendingDestroyUuids.contains(uuid)) {
                            pendingDestroyUuids.remove(uuid);
                            pendingUuids.remove(uuid);
                            Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, null, false));

                            tryDeleteAsync(targetDir);
                            return;
                        }

                        WorldCreator creator = new WorldCreator(worldName);
                        creator.generator(new ChunkGenerator() {});
                        creator.generateStructures(false);
                        World world = Bukkit.createWorld(creator);

                        if (world == null) {
                            Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, null, false));
                            tryDeleteAsync(targetDir);
                            pendingUuids.remove(uuid);
                            throw new IllegalStateException("Failed to create temp world: " + worldName);
                        }

                        world.setAutoSave(false);

                        worlds.put(uuid, world);
                        pendingUuids.remove(uuid);

                        Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, world, true));
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return uuid;
    }

    public static UUID createEmptyTemp() {
        UUID uuid = UUID.randomUUID();
        String worldName = "bw-" + uuid;
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new ChunkGenerator() {});
        creator.generateStructures(false);
        World world = Bukkit.createWorld(creator);

        if (world == null) {
            Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, null, false));
            throw new IllegalStateException("Failed to create temp world: " + worldName);
        }

        world.setAutoSave(false);

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setTime(6000);

        world.getBlockAt(0, 64, 0).setType(Material.BEDROCK);
        world.setSpawnLocation(new Location(world, 0.5, 65.0, 0.5, 0f, 0f));

        worlds.put(uuid, world);

        Bukkit.getPluginManager().callEvent(new BedwarsWorldReadyEvent(uuid, world, true));
        return uuid;
    }

    static public World get(UUID uuid) {
        return worlds.get(uuid);
    }

    public static void exportWorld(UUID uuid, File targetTemplateDir) { // TODO: reference count to fix delete problem
        World world = worlds.get(uuid);
        if (world == null) return;

        world.save();
        exportingUuids.add(uuid);

        Runnable baseExport = () -> {
            try {
                copyWorldFolderSafely(world.getWorldFolder(), targetTemplateDir);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to export world!\n" + e);
            } finally {
                exportingUuids.remove(uuid);

                File folderToDelete = pendingDestroyAfterExport.remove(uuid);
                if (folderToDelete != null) {
                    tryDeleteAsync(folderToDelete);
                }
            }
        };

        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    baseExport.run();
                }
            }.runTaskAsynchronously(plugin);
        } catch (IllegalPluginAccessException e) {
            baseExport.run();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void copyWorldFolderSafely(File source, File target) throws IOException {
        if (!source.exists()) return;

        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            File[] files = source.listFiles();
            if (files == null) return;

            for (File file : files) {
                String name = file.getName();
                if (name.equals("session.lock") || name.equals("uid.dat")) {
                    continue;
                }
                copyWorldFolderSafely(file, new File(target, name));
            }
        } else {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try {
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.NoSuchFileException | java.io.FileNotFoundException ignored) {}
        }
    }

    static public boolean uuidExist(UUID uuid) {
        return pendingUuids.contains(uuid) || worlds.containsKey(uuid);
    }

    static public void forceLoadRegions(UUID uuid, Pair<Vector3i, Vector3i> region) {
        Vector3i loc1 = region.left();
        Vector3i loc2 = region.right();

        int minChunkX = Math.min(loc1.x, loc2.x) >> 4;
        int maxChunkX = Math.max(loc1.x, loc2.x) >> 4;
        int minChunkZ = Math.min(loc1.z, loc2.z) >> 4;
        int maxChunkZ = Math.max(loc1.z, loc2.z) >> 4;

        WorldReadyListener.addTask(uuid, world -> {
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    world.setChunkForceLoaded(cx, cz, true);
                }
            }
        });
    }

    public static void destroy(UUID uuid) {
        if (pendingUuids.contains(uuid)) {
            pendingDestroyUuids.add(uuid);
            return;
        }

        World world = worlds.remove(uuid);
        if (world == null) return;

        File worldFolder = world.getWorldFolder();

        Location fallbackLobby = Bukkit.getWorlds().getFirst().getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(fallbackLobby);
        }
        for (Chunk chunk : world.getLoadedChunks()) {
            if (chunk.isForceLoaded()) chunk.setForceLoaded(false);
        }
        Bukkit.unloadWorld(world, false);

        if (exportingUuids.contains(uuid)) {
            pendingDestroyAfterExport.put(uuid, worldFolder);
        } else {
            tryDeleteAsync(worldFolder);
        }
    }

    static public void shutdown() { // TODO: auto save editing game
        worlds.forEach((uuid, world) -> deleteTempWorld(world));

        while (!pendingUuids.isEmpty() || !exportingUuids.isEmpty()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }

        worlds.clear();
        worlds = null;
        pendingUuids.clear();
        pendingUuids = null;
        exportingUuids.clear();
        exportingUuids = null;
        pendingDestroyAfterExport.clear();
        pendingDestroyAfterExport = null;

        plugin = null;
    }

    private static void deleteTempWorld(World world) { // TODO: Teleport need to replace to teleport to main server
        File worldFolder = world.getWorldFolder();

        Location fallbackLobby = Bukkit.getWorlds().getFirst().getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(fallbackLobby);
        }

        for (Chunk chunk : world.getLoadedChunks()) {
            if (chunk.isForceLoaded()) {
                chunk.setForceLoaded(false);
            }
        }
        Bukkit.unloadWorld(world, false);

        tryDeleteAsync(worldFolder);
    }

    private static void tryDeleteAsync(File targetDir) {
        Runnable baseDelete = () -> {
            try {
                FileUtils.deleteDirectory(targetDir);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to delete temp world: " + targetDir.getName());
            }
        };

        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    baseDelete.run();
                }
            }.runTaskAsynchronously(plugin);
        } catch (IllegalPluginAccessException e) {
            baseDelete.run();
        }
    }

    private static void cleanupOrphanedWorlds() {
        File[] candidates = Bukkit.getWorldContainer().listFiles(
                (dir, name) -> name.startsWith("bw-")
        );
        if (candidates == null) return;

        for (File dir : candidates) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to clean up orphaned temp world: " + dir.getName());
                    }
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
