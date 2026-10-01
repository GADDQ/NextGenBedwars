package top.earthstudio.nextgenbedwars.core.team.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.time.Duration;
import java.util.UUID;

public class TeamRespawnListener implements Listener {
    private final TeamManager teamManager;
    private final GameInstance gameInstance;

    public TeamRespawnListener(TeamManager teamManager, GameInstance gameInstance) {
        this.teamManager = teamManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler
    public void onPLayerFallInVoid(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.VOID)
            return;

        Player player = (Player) event.getEntity();
        if (player.getWorld() != gameInstance.getWorld())
            return;
        // TODO: spectator logic

        if (teamManager.get(player) == null)
            return;

        event.setCancelled(true);
        tryRespawn(player, teamManager.get(player), true);
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld() != gameInstance.getWorld())
            return;

        if (teamManager.get(player) == null)
            return;

        event.setCancelled(true);
        event.deathMessage(null);

        // TODO: kill message

        tryRespawn(player, teamManager.get(player), true);
    }

    private void tryRespawn(Player player, Team team, boolean teleportToSpectatorRespawnPoint) { // TODO: respawn cannot see other player, this == below TODO
        player.heal(Double.MAX_VALUE);
        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR); // TODO: this need replace with fake spectator mode (adventure) in the future!
        player.setVelocity(new Vector(0, 0, 0));
        UUID uuid = UUID.randomUUID();

        if (!team.isBedAlive) {
            GlobalTicker.set(uuid, () -> {
                GlobalTicker.remove(uuid);
                player.teleport(gameInstance.getGame().spectatorRespawnPoint);
            });
            return;
        }

        GlobalTicker.set(uuid, new TickerTask() {
            boolean isFirst = true;
            int tick = team.respawnTick;

            @Override
            public void run() { // TODO: i18n
                if (isFirst) {
                    if (teleportToSpectatorRespawnPoint)
                        player.teleport(gameInstance.getGame().spectatorRespawnPoint); // TODO: Configurable

                    isFirst = false;
                }

                if (tick % 20 == 0 && tick > 0) {
                    int remainingSeconds = tick / 20;
                    player.showTitle(Title.title(
                            Component.text("你死了！").color(NamedTextColor.RED),
                            Component.text("你将在 ").append(Component.text(remainingSeconds).color(NamedTextColor.YELLOW)).append(Component.text(" 秒后重生")),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
                    ));
                }

                if (tick <= 0) {
                    GlobalTicker.remove(uuid);
                    player.teleport(team.respawnLocation);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.showTitle(Title.title(
                            Component.text("已重生！").color(NamedTextColor.GREEN),
                            Component.text(""),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
                    ));
                }

                tick--;
            }
        });
    }
}
