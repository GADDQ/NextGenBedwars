package top.earthstudio.nextgenbedwars.core.respawn;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.respawn.RespawnManager;
import top.earthstudio.nextgenbedwars.api.respawn.event.BedwarsPlayerDeadEvent;
import top.earthstudio.nextgenbedwars.api.respawn.event.BedwarsPlayerRespawnEvent;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import top.earthstudio.nextgenbedwars.core.respawn.listener.RespawnListener;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RespawnManagerImpl implements RespawnManager { // TODO: player changed (e.g. exit, rejoin)
    private Map<UUID, Integer> respawnTicks;
    private List<UUID> ignorePlayers;

    private GameInstance gameInstance;

    private RespawnListener respawnListener;

    public RespawnManagerImpl(GameInstance gameInstance) {
        respawnTicks = new Object2IntOpenHashMap<>();
        ignorePlayers = new ObjectArrayList<>();

        this.gameInstance = gameInstance;

        respawnListener = new RespawnListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(respawnListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void setRespawn(Player player, int tick) {
        if (ignorePlayers.contains(player.getUniqueId()))
            return;

        if (gameInstance.get(TeamManager.class).get(player) == null)
            return;

        if (!respawnTicks.containsKey(player.getUniqueId())) {
            var event = new BedwarsPlayerDeadEvent(gameInstance, player);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;
        }

        respawnTicks.put(player.getUniqueId(), tick);
    }

    @Override
    public int getRemainRespawnTick(Player player) {
        var tick = respawnTicks.get(player.getUniqueId());
        return tick == null ? -1 : tick;
    }

    @Override
    public void addIgnore(Player player) {
        ignorePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeIgnore(Player player) {
        ignorePlayers.remove(player.getUniqueId());
    }

    @Override
    public void update() { // TODO: i18n
        List<UUID> pendingRemoves = new ObjectArrayList<>();
        respawnTicks.replaceAll((uuid, tick) -> {
            if (ignorePlayers.contains(uuid)) {
                pendingRemoves.add(uuid);
                return 0;
            }

            if (tick <= 0)
                pendingRemoves.add(uuid);

            if (tick % 20 == 0 && tick > 0) {
                int remainingSeconds = tick / 20;
                Player player = Bukkit.getServer().getPlayer(uuid);

                if (player == null) // TODO: this is a temp solution, replace it when player changed listener is ready!
                    return tick;

                player.showTitle(Title.title(
                        Component.text("你死了！").color(NamedTextColor.RED),
                        Component.text("你将在 ").append(Component.text(remainingSeconds).color(NamedTextColor.YELLOW)).append(Component.text(" 秒后重生")),
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
                ));
            }

            return tick - 1;
        });

        pendingRemoves.forEach(uuid -> {
            Player player = Bukkit.getServer().getPlayer(uuid);

            if (player == null)
                return;

            var event = new BedwarsPlayerRespawnEvent(gameInstance, player);
            Bukkit.getPluginManager().callEvent(event);
            player.showTitle(Title.title(
                    Component.text("已重生！").color(NamedTextColor.GREEN),
                    Component.text(""),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
            ));
            respawnTicks.remove(uuid);
        });
        pendingRemoves.clear();
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(respawnListener);
        respawnListener = null;

        gameInstance = null;

        ignorePlayers.clear();
        ignorePlayers = null;
        respawnTicks.clear();
        respawnTicks = null;
    }
}
