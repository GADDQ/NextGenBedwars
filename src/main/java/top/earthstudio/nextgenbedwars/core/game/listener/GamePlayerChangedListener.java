package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.game.event.BedwarsGameStateChangedEvent;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.util.UUID;

// TODO: this might need be moved to proxy connector later
public final class GamePlayerChangedListener implements Listener { // TODO: detect connect instead of change world due to plugin is planned to support proxy only
    private final GameInstance gameInstance;
    private final UUID uuid = UUID.randomUUID();

    private final Scoreboard scoreboard;

    public GamePlayerChangedListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { // TODO: advanced starting countdown logic, disconnect event listener, clear/restore inventory
        Player eventPlayer = event.getPlayer();

        if (eventPlayer.getWorld() == gameInstance.getWorld()) { // JOIN
            switch (gameInstance.getGameState()) {
                case WAITING:
                    if (eventPlayer.getWorld().getPlayerCount() >= gameInstance.getGame().minPlayers) {
                        gameInstance.setGameState(GameState.STARTING);
                        Bukkit.getPluginManager().callEvent(new BedwarsGameStateChangedEvent(gameInstance, GameState.STARTING));

                        GlobalTicker.set(uuid, new TickerTask() { // TODO: wait tick can changed in config
                            int leftTick = 1200;
                            int second;

                            @Override
                            public void run() {
                                if (leftTick / 20 != second) {
                                    second = leftTick / 20;

                                    if (second % 10 == 0 || second < 10) {
                                        gameInstance.getWorld().getPlayers().forEach(player -> {
                                            player.showTitle(Title.title(
                                                    Component.text("游戏即将开始").color(NamedTextColor.GREEN),
                                                    Component.text("倒计时 ").append(Component.text(second)).append(Component.text(" 秒")),
                                                    Title.Times.times(
                                                            Ticks.duration(0),
                                                            Ticks.duration(30),
                                                            Ticks.duration(10))
                                            ));
                                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                                        });
                                    }
                                }

                                if (leftTick <= 0) {
                                    GlobalTicker.remove(uuid);

                                    TeamManager teamManager = gameInstance.get(TeamManager.class);

                                    teamManager.randomAssignTeam(gameInstance.getWorld().getPlayers());

                                    teamManager.getAll().forEach(team -> { // TODO: FIXME: this is a temp color solution, please refactor TeamManagerImpl to support ScoreBoard Team Natively!
                                        var sbTeam = scoreboard.registerNewTeam(((TextComponent) team.displayName).content());

                                        TextColor textColor = TextColor.color(team.teamColor.asRGB());
                                        NamedTextColor namedColor = NamedTextColor.nearestTo(textColor);
                                        sbTeam.color(namedColor);
                                    });

                                    teamManager.getAll().forEach(team -> {
                                        if (team.players.isEmpty()) {
                                            team.isBedAlive = false;
                                            return;
                                        }

                                        team.players.forEach(player -> {
                                            player.setFireTicks(0);
                                            player.clearActivePotionEffects();
                                            player.setFallDistance(0);
                                            player.heal(Double.MAX_VALUE);
                                            player.setFoodLevel(20);
                                            player.setSaturation(0);
                                            player.setGameMode(GameMode.SURVIVAL);
                                            player.setVelocity(new Vector(0, 0, 0));
                                            player.getInventory().clear();

                                            player.teleport(team.respawnLocation);
                                            player.setScoreboard(scoreboard);

                                            scoreboard.getTeam(((TextComponent) team.displayName).content()).addEntry(player.getName());
                                        });
                                    });

                                    gameInstance.setGameState(GameState.PLAYING);
                                    Bukkit.getPluginManager().callEvent(new BedwarsGameStateChangedEvent(gameInstance, GameState.PLAYING));
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
                    if (eventPlayer.getWorld().getPlayerCount() > gameInstance.getGame().maxPlayers) {
                        eventPlayer.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                        eventPlayer.sendMessage(Component.text("由于房间已满，您已被传送回大厅！").color(NamedTextColor.RED));
                        return;
                    }

                    if (eventPlayer.getWorld().getPlayerCount() == gameInstance.getGame().maxPlayers) {
                        // TODO: switch to 5s startup
                        return;
                    }

                    break;
                case ENDING:
                    eventPlayer.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                    eventPlayer.sendMessage(Component.text("您不能加入此房间，此房间已关闭！").color(NamedTextColor.RED));
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
                        Bukkit.getPluginManager().callEvent(new BedwarsGameStateChangedEvent(gameInstance, GameState.WAITING));
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
