package top.earthstudio.nextgenbedwars.core.world;

import it.unimi.dsi.fastutil.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.codehaus.plexus.util.FileUtils;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.world.event.WorldReadyEvent;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager { // TODO: return real UUID from world.getUID(), rebuild this shit
    private static JavaPlugin plugin;
    private static WorldReadyListener worldReadyListener;
    private static Map<UUID, World> worlds;

    private static Set<UUID> pendingUuids;
    private static Set<UUID> pendingDestroyUuids;

    private WorldManager() {}

    static public void initialize(JavaPlugin plugin, WorldReadyListener worldReadyListener) {
        WorldManager.plugin = plugin;
        WorldManager.worldReadyListener = worldReadyListener;
        pendingUuids = ConcurrentHashMap.newKeySet();
        pendingDestroyUuids = ConcurrentHashMap.newKeySet();
        worlds = new ConcurrentHashMap<>();

        // TODO: 清理可能的意外残留世界
    }

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
                            Bukkit.getPluginManager().callEvent(new WorldReadyEvent(uuid, null, false));
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
                            Bukkit.getPluginManager().callEvent(new WorldReadyEvent(uuid, null, false));

                            tryDeleteAsync(targetDir);
                            return;
                        }

                        WorldCreator creator = new WorldCreator(worldName);
                        creator.generator(new ChunkGenerator() {});
                        World world = Bukkit.createWorld(creator);

                        if (world == null) {
                            Bukkit.getPluginManager().callEvent(new WorldReadyEvent(uuid, null, false));
                            tryDeleteAsync(targetDir);
                            throw new IllegalStateException("Failed to create temp world: " + worldName);
                        }

                        world.setAutoSave(false);

                        worlds.put(uuid, world);
                        pendingUuids.remove(uuid);

                        Bukkit.getPluginManager().callEvent(new WorldReadyEvent(uuid, world, true));
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return uuid;
    }

    static public World get(UUID uuid) {
        return worlds.get(uuid);
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

        worldReadyListener.addTask(uuid, world -> {
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    world.setChunkForceLoaded(cx, cz, true);
                }
            }
        });
    }

    static public void destroy(UUID uuid) {
        if (pendingUuids.contains(uuid)) {
            pendingDestroyUuids.add(uuid);
            return;
        }
        World world = worlds.remove(uuid);
        if (world == null)
            throw new IllegalStateException("UUID not exist!");
        deleteTempWorld(world);
    }

    static public void shutdown() {
        worlds.forEach((uuid, world) -> {
            deleteTempWorld(world);
        });
        worlds.clear();
        worlds = null;

        pendingDestroyUuids.clear();
        pendingDestroyUuids = null;
        pendingUuids.clear();
        pendingUuids = null;

        worldReadyListener = null;
        plugin = null;
    }

    private static void deleteTempWorld(World world) {
        File worldFolder = world.getWorldFolder();

        Location fallbackLobby = Bukkit.getWorlds().getFirst().getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(fallbackLobby);
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
}
