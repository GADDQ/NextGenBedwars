package top.earthstudio.nextgenbedwars.core.information.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.information.InformationManager;

public class InformationPlayerChangedListener implements Listener { // TODO: proxy support
    private final InformationManager informationManager;
    private final GameInstance gameInstance;

    public InformationPlayerChangedListener(InformationManager informationManager, GameInstance gameInstance) {
        this.informationManager = informationManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerChangedWorldEvent event) {
        if (event.getPlayer().getWorld() == gameInstance.getWorld()) { // join
            informationManager.displayFor(event.getPlayer());
            return;
        }

        if (event.getFrom() == gameInstance.getWorld()) { // exit
            informationManager.clearFor(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerServerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getWorld() == gameInstance.getWorld())
            informationManager.displayFor(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getWorld() == gameInstance.getWorld())
            informationManager.clearFor(event.getPlayer());
    }
}