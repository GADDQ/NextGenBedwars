package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.util.UUID;

// TODO: this might need be moved to proxy connector later
public class GamePlayerChangedListener implements Listener { // TODO: detect connect instead of change world due to plugin is planned to support proxy only
    private final GameInstance gameInstance;
    private final UUID uuid = UUID.randomUUID();

    public GamePlayerChangedListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { // TODO: advanced starting countdown logic, disconnect event listener
        Player player = event.getPlayer();

        if (player.getWorld() == gameInstance.getWorld()) { // JOIN
            switch (gameInstance.getGameState()) {
                case WAITING:
                    if (player.getWorld().getPlayerCount() >= gameInstance.getGame().minPlayers) {
                        gameInstance.setGameState(GameState.STARTING);

                        GlobalTicker.set(uuid, new TickerTask() { // TODO: wait tick can changed in config
                            int leftTick = 1200;
                            int second;

                            @Override
                            public void run() {
                                if (leftTick / 20 != second) {
                                    second = leftTick / 20;

                                    if (second % 10 == 0 || second < 10) {
                                        gameInstance.getWorld().getPlayers().forEach(player1 -> {
                                            player1.showTitle(Title.title(
                                                    Component.text("游戏即将开始").color(NamedTextColor.GREEN),
                                                    Component.text("倒计时 ").append(Component.text(second)).append(Component.text(" 秒")),
                                                    Title.Times.times(
                                                            Ticks.duration(0),
                                                            Ticks.duration(30),
                                                            Ticks.duration(10))
                                            ));
                                            player1.playSound(player1, Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                                        });
                                    }
                                }

                                if (leftTick <= 0) {
                                    GlobalTicker.remove(uuid);

                                    TeamManager teamManager = gameInstance.get(TeamManager.class);

                                    teamManager.randomAssignTeam(gameInstance.getWorld().getPlayers());

                                    teamManager.getAll().forEach(team ->
                                            team.players.forEach(player1 ->
                                                    player1.teleport(team.respawnLocation)
                                            )
                                    );

                                    gameInstance.setGameState(GameState.PLAYING);
                                    gameInstance.getWorld().getPlayers().forEach(player1 -> player1.showTitle(Title.title( // TODO: isShowStartTitle
                                            Component.text("游戏开始").color(NamedTextColor.GREEN),
                                            Component.text("保护你的床！").color(NamedTextColor.RED),
                                            Title.Times.times(
                                                    Ticks.duration(0),
                                                    Ticks.duration(20),
                                                    Ticks.duration(10))
                                    )));
                                    return;
                                }

                                leftTick--;
                            }
                        });
                    }
                    break;
                case STARTING:
                    if (player.getWorld().getPlayerCount() > gameInstance.getGame().maxPlayers) {
                        player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                        player.sendMessage(Component.text("由于房间已满，您已被传送回大厅！").color(NamedTextColor.RED));
                        return;
                    }

                    if (player.getWorld().getPlayerCount() == gameInstance.getGame().maxPlayers) {
                        // TODO: switch to 5s startup
                        return;
                    }

                    break;
                case ENDING:
                    player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                    player.sendMessage(Component.text("您不能加入此房间，此房间已关闭！").color(NamedTextColor.RED));
                    break;
            }
        }

        if (event.getFrom() == gameInstance.getWorld()) { // LEAVE
            switch (gameInstance.getGameState()) {
                case WAITING:
                    break;
                case STARTING:
                    if (event.getFrom().getPlayerCount() < gameInstance.getGame().minPlayers) {
                        gameInstance.setGameState(GameState.WAITING);
                        gameInstance.getWorld().getPlayers().forEach(player1 -> player1.showTitle(Title.title(
                                Component.text("启动已暂停").color(NamedTextColor.RED),
                                Component.text("玩家数量不足")
                        )));

                        GlobalTicker.remove(uuid);
                    }
                    break;
            }
        }
    }

    public void shutdown() {
        GlobalTicker.remove(uuid);
    }
}
