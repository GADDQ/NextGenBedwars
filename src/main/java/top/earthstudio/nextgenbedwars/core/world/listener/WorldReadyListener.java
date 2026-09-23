package top.earthstudio.nextgenbedwars.core.world.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.api.world.WorldReadyEvent;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class WorldReadyListener implements Listener {
    private final Map<UUID, Queue<Consumer<World>>> pendingTasks = new ConcurrentHashMap<>();

    @EventHandler
    public void onWorldReady(WorldReadyEvent event) {
        UUID worldUUID = event.worldUUID;
        if (!event.isSuccess) {
            pendingTasks.remove(worldUUID);
            return;
        }
        World world = event.world;

        Queue<Consumer<World>> queue = pendingTasks.remove(worldUUID);
        if (queue != null)
            queue.forEach(task -> task.accept(world));
    }

    /**
     * 注册一个等待指定世界就绪的任务
     */
    public void addTask(UUID worldUUID, Consumer<World> task) {
        // 如果注册的时候世界其实已经建好了，立即执行
        World alreadyLoaded = WorldManager.get(worldUUID);
        if (alreadyLoaded != null) {
            task.accept(alreadyLoaded);
            return;
        }

        if (!WorldManager.uuidExist(worldUUID))
            throw new IllegalStateException("UUID not exist!");

        // 否则压入该世界专属的无锁队列中等待事件唤醒
        pendingTasks.computeIfAbsent(worldUUID, k -> new ConcurrentLinkedQueue<>()).add(task);
    }
}
