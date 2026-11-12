package top.earthstudio.nextgenbedwars.core.team.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.respawn.event.BedwarsPlayerDeadEvent;
import top.earthstudio.nextgenbedwars.api.respawn.event.BedwarsPlayerRespawnEvent;

public final class TeamRespawnListener implements Listener { // TODO: List<UUID> runningTasks, remove tasks when shutdown
    private final TeamManager teamManager;
    private final GameInstance gameInstance;

    public TeamRespawnListener(TeamManager teamManager, GameInstance gameInstance) {
        this.teamManager = teamManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDead(BedwarsPlayerDeadEvent event) {
        Player player = event.player;
        if (player.getWorld() != gameInstance.getWorld())
            return;

        Team team = teamManager.get(player);

        if (!team.isBedAlive)
            teamManager.getSpectators(team).add(player);

        // TODO: kill message

        doTeamDead(player, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(BedwarsPlayerRespawnEvent event) {
        Player player = event.player;
        if (player.getWorld() != gameInstance.getWorld())
            return;

        Team team = teamManager.get(player);

        doTeamRespawn(player, team);
    }

    private void doTeamDead(Player player, boolean teleportToSpectatorRespawnPoint) {
        player.setFireTicks(0);
        player.clearActivePotionEffects();
        player.setFallDistance(0);
        player.heal(Double.MAX_VALUE);
        player.setFoodLevel(20);
        player.setSaturation(0);
        clearInventory(player);
        player.setGameMode(GameMode.SPECTATOR); // TODO: this need replace with fake spectator mode (adventure) in the future!
        player.setVelocity(new Vector(0, 0, 0));

        if (teleportToSpectatorRespawnPoint)
            Bukkit.getScheduler().runTaskLater(BedwarsAPI.getInstance().plugin(), () -> player.teleport(gameInstance.getGame().spectatorRespawnLocation), 1L);
    }

    private void doTeamRespawn(Player player, Team team) { // TODO: respawn cannot see other player, this == below TODO; configurable
        player.teleport(team.respawnLocation);
        player.setGameMode(GameMode.SURVIVAL);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
    }

    private void clearInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack[] storageContents = inventory.getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            if (shouldKeep(storageContents[i])) continue;
            if (storageContents[i] != null && !storageContents[i].getType().isAir()) {
                inventory.clear(i);
            }
        }

        ItemStack[] armorContents = inventory.getArmorContents();
        boolean armorChanged = false;
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack item = armorContents[i];
            if (item == null || item.getType().isAir()) continue;
            if (shouldKeep(item)) continue;
            armorContents[i] = null;
            armorChanged = true;
        }
        if (armorChanged) {
            inventory.setArmorContents(armorContents);
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (!offHand.getType().isAir() && !shouldKeep(offHand)) {
            inventory.setItemInOffHand(null);
        }
    }

    private boolean shouldKeep(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(PdcKeys.shop.keepWhenDead);
    }
}
