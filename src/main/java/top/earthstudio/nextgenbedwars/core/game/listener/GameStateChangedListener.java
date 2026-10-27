package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class GameStateChangedListener implements Listener { // TODO: detect connect instead of change world due to plugin is planned to support proxy only
    private final GameInstance gameInstance;
    private int playerCount = 0;

    public GameStateChangedListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { // TODO: advanced starting countdown logic
        Player player = event.getPlayer();

        if (event.getFrom() == gameInstance.getWorld()) {
            switch (gameInstance.getGameState()) {
                case WAITING:
                    playerCount--;
                    break;
                case STARTING:
                    playerCount--;
                    break;
                case PLAYING:
                    break;
                case ENDING:
                    break;
                default:
                    return;
            }
        }

        if (event.getPlayer().getWorld() == gameInstance.getWorld()) {
            switch (gameInstance.getGameState()) {
                case WAITING:
                    playerCount++;
                    break;
                case STARTING:
                    if (playerCount >= gameInstance.getGame().maxPlayers) {
                        player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                        player.sendMessage(Component.text("由于房间已满，您已被传送回大厅！").color(NamedTextColor.RED));
                        return;
                    }

                    playerCount++;
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
