package top.earthstudio.nextgenbedwars.core.team.listener;

import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

public final class TeamChatListener implements Listener { // TODO: global chat
    private final GameInstance gameInstance;
    private final TeamManager teamManager;

    public TeamChatListener(TeamManager teamManager, GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.teamManager = teamManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSendMessage(AsyncChatEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        if (gameInstance.getGameState() != GameState.PLAYING)
            return;

        event.setCancelled(true);

        Team team = teamManager.get(event.getPlayer());

        if (team == null)
            return; // TODO: spectator logic

        for (Player player : team.players) { // TODO: allow config how message are presented, is allow to use color
            player.sendMessage(
                    Component.text("<").color(NamedTextColor.WHITE).append(
                                    event.getPlayer().displayName().color(TextColor.color(team.teamColor.asARGB()))
                    ).append(
                            Component.text("> ").color(NamedTextColor.WHITE)
                    ).append(
                            event.originalMessage().color(NamedTextColor.WHITE)
                    )
            );
        }
    }
}
