package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.game.event.BedwarsGameOverEvent;
import top.earthstudio.nextgenbedwars.api.game.event.BedwarsGameStateChangedEvent;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.team.event.BedwarsPlayerDeadEvent;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;
import top.earthstudio.nextgenbedwars.core.game.GameManager;

import java.time.Duration;
import java.util.UUID;

public final class GameOverListener implements Listener {
    public final GameInstance gameInstance;

    public GameOverListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(BedwarsPlayerDeadEvent event) {
        if (event.gameInstance != gameInstance)
            return;

        if (!event.isFinalDeath)
            return;

        TeamManager teamManager = gameInstance.get(TeamManager.class);

        int hasBedTeamCount = 0;
        int eliminatedTeamCount = 0;
        Team winningTeam = null;
        for (Team team : teamManager.getAll()) {
            if (team.isBedAlive)
                hasBedTeamCount++;

            if (team.players.size() == teamManager.getSpectators(team).size())
                eliminatedTeamCount++;
            else
                winningTeam = team;
        }

        if (hasBedTeamCount > 1)
            return;

        if (eliminatedTeamCount < teamManager.getAll().size() - 1)
            return;

        gameInstance.setGameState(GameState.ENDING);
        Bukkit.getPluginManager().callEvent(new BedwarsGameStateChangedEvent(gameInstance, GameState.ENDING));
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(gameInstance, winningTeam));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStateChange(BedwarsGameOverEvent event) {
        if (event.gameInstance != gameInstance)
            return;

        UUID uuid = UUID.randomUUID();

        GlobalTicker.set(uuid, new TickerTask() { // TODO: config how long to shutdown the instance, i18n, textAnimation? Sound, Effect, etc.
            int leftTick = 200;

            @Override
            public void run() {
                if (leftTick <= 0) {
                    GlobalTicker.remove(uuid);
                    GameManager.removeInstance(gameInstance.getUUID());
                    return;
                }

                gameInstance.get(TeamManager.class).getAll().forEach(team -> {
                    Title title;

                    if (team == event.winningTeam) {
                        title = Title.title(
                                Component.text("胜利").color(NamedTextColor.GOLD),
                                Component.empty(),
                                Title.Times.times(
                                        Duration.ZERO,
                                        Ticks.duration(30),
                                        Duration.ZERO
                                )
                        );
                    } else {
                        title = Title.title(
                                Component.text("游戏结束").color(NamedTextColor.RED),
                                Component.empty(),
                                Title.Times.times(
                                        Duration.ZERO,
                                        Ticks.duration(30),
                                        Duration.ZERO
                                )
                        );
                    }

                    team.players.forEach(player -> player.showTitle(title));
                });
                leftTick--;
            }
        });
    }
}
