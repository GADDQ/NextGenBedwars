package top.earthstudio.nextgenbedwars.core.team.listener;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.team.event.BedBrokenEvent;
import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import java.util.List;

public class TeamInteractListener implements Listener {  // TODO: i18n
    private final GameInstance gameInstance;
    private final TeamManager teamManager;

    public TeamInteractListener(TeamManager teamManager, GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPvp(PrePlayerAttackEntityEvent event) {
        Player attacked;
        if (event.getAttacked() instanceof Player p) {
            attacked = p;
        } else
            return;

        Player player = event.getPlayer();
        Team teamA = teamManager.get(player);
        Team teamB = teamManager.get(attacked);
        if (teamA == null || teamB == null)
            return;

        if (teamA == teamB)
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Team team = teamManager.get(player);
        if (team == null)
            return;

        Block block = event.getBlock();
        if (!Tag.BEDS.isTagged(block.getType()))
            return;

        long blockLong = BlockPosUtil.asLong(block);
        if (team.bedBlockLongs.firstLong() == blockLong || team.bedBlockLongs.secondLong() == blockLong) {
            player.sendMessage(Component.text("你不能破坏自己的床！").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        List<Team> teamList = teamManager.getAll();
        for (Team targetTeam : teamList) {
            if (targetTeam == team)
                continue;

            if (targetTeam.bedBlockLongs.firstLong() == blockLong || targetTeam.bedBlockLongs.secondLong() == blockLong) {
                event.setDropItems(false);
                targetTeam.isBedAlive = false;

                WorldProtector protector = gameInstance.get(WorldProtector.class);
                protector.removeBlockFromWorldGroup(targetTeam.bedBlockLongs.firstLong());
                protector.removeBlockFromWorldGroup(targetTeam.bedBlockLongs.secondLong());

                Bukkit.getPluginManager().callEvent(new BedBrokenEvent(gameInstance, targetTeam, player));
                player.getWorld().sendMessage(targetTeam.displayName.append(Component.text(" 的床被 ").color(NamedTextColor.RED).append(player.displayName().asComponent().append(Component.text("摧毁了！")))));
                return;
            }
        }
    }
}
