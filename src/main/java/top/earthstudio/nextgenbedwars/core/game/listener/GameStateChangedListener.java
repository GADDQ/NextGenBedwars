package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;

public class GameStateChangedListener implements Listener { // TODO: detect connect instead of change world due to plugin is planned to support proxy only
    private final GameInstance gameInstance;

    public GameStateChangedListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { // TODO: advanced starting countdown logic, disconnect event listener
        Player player = event.getPlayer();

        if (event.getPlayer().getWorld() == gameInstance.getWorld()) { // JOIN
            switch (gameInstance.getGameState()) {
                case WAITING:
                    if (event.getPlayer().getWorld().getPlayerCount() >= gameInstance.getGame().minPlayers) {
                        gameInstance.setGameState(GameState.STARTING);
                        // TODO: show title and start timer
                    }
                    break;
                case STARTING:
                    if (event.getPlayer().getWorld().getPlayerCount() > gameInstance.getGame().maxPlayers) {
                        player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                        player.sendMessage(Component.text("由于房间已满，您已被传送回大厅！").color(NamedTextColor.RED));
                        return;
                    }

                    if (event.getPlayer().getWorld().getPlayerCount() == gameInstance.getGame().maxPlayers) {
                        // TODO: switch to 5s startup
                        return;
                    }

                    break;
                case PLAYING:
                    break;
                case ENDING:
                    break;
                default:
                    return;
            }
        }

        if (event.getFrom() == gameInstance.getWorld()) { // LEAVE
            switch (gameInstance.getGameState()) {
                case WAITING:
                    break;
                case STARTING:
                    if (event.getFrom().getPlayerCount() < gameInstance.getGame().minPlayers) {
                        gameInstance.setGameState(GameState.WAITING);
                        // TODO: show title and cancel timer
                    }
                    break;
                case PLAYING:
                    break;
                case ENDING:
                    break;
                default:
                    return;
            }
        }
    }
}
