package top.earthstudio.nextgenbedwars.core.prop.builtin.compass;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.compass.CompassManager;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.FindNearestPlayer;

import java.util.Map;
import java.util.UUID;

public class CompassManagerImpl implements CompassManager { // ATTENTION: This was built by LLM.
    private static final String PROP_ID = "compass";
    private static final int SLICE_INTERVAL = 10;
    private static final double MOVE_THRESHOLD_SQ = 4.0;

    private final GameInstance gameInstance;
    private TeamManager teamManager;
    private FindNearestPlayer findNearestPlayer;
    private Map<UUID, Location> lastTargetLocations;

    public CompassManagerImpl(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.findNearestPlayer = new FindNearestPlayer(SLICE_INTERVAL);
        this.lastTargetLocations = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void onReady() {
        this.teamManager = gameInstance.get(TeamManager.class);
        gameInstance.get(ShopExecutor.class).register("compass", (player, shopItem, giveItem) -> {
            refresh(player, giveItem);
            return true;
        });
    }

    @Override
    public void update() {
        if (teamManager == null) return;
        World world = gameInstance.getWorld();
        if (world == null) return;

        findNearestPlayer.tick(world.getPlayers(), teamManager, (self, result) -> {
            PlayerInventory inv = self.getInventory();
            Location targetLoc = result.target().getLocation();
            Location lastLoc = lastTargetLocations.get(self.getUniqueId());

            boolean shouldUpdateMeta = lastLoc == null
                    || lastLoc.getWorld() != targetLoc.getWorld()
                    || lastLoc.distanceSquared(targetLoc) >= MOVE_THRESHOLD_SQ;

            boolean hasTracker = false;

            for (int slot = 0; slot < inv.getSize(); slot++) {
                ItemStack item = inv.getItem(slot);
                if (!isTrackerCompass(item)) continue;

                hasTracker = true;
                if (shouldUpdateMeta) {
                    item.editMeta(CompassMeta.class, meta -> {
                        meta.setLodestone(targetLoc);
                        meta.setLodestoneTracked(false);
                    });
                }
            }

            if (!hasTracker) {
                lastTargetLocations.remove(self.getUniqueId());
                return;
            }

            if (shouldUpdateMeta) {
                lastTargetLocations.put(self.getUniqueId(), targetLoc);
            }
        });
    }

    @Override
    public NearestTarget getNearestTarget(Player player) {
        if (teamManager == null) return null;
        World world = gameInstance.getWorld();
        if (world == null) return null;

        Team myTeam = teamManager.get(player);
        if (myTeam == null) return null;

        double sx = player.getX();
        double sy = player.getY();
        double sz = player.getZ();

        Player nearest = null;
        double minDistSq = Double.MAX_VALUE;

        for (Player target : world.getPlayers()) {
            if (target.getUniqueId().equals(player.getUniqueId())) continue;
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            Team targetTeam = teamManager.get(target);
            if (targetTeam == null || targetTeam.equals(myTeam)) continue;

            double dx = target.getX() - sx;
            double dy = target.getY() - sy;
            double dz = target.getZ() - sz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = target;
            }
        }

        return nearest == null ? null : new NearestTarget(nearest, Math.sqrt(minDistSq));
    }

    private boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        String propId = item.getPersistentDataContainer().get(PdcKeys.prop.id, PersistentDataType.STRING);
        return PROP_ID.equals(propId);
    }

    @Override
    public void refresh(Player player, ItemStack compassItem) {
        if (compassItem == null || compassItem.getType() != Material.COMPASS) return;

        NearestTarget result = getNearestTarget(player);
        if (result == null) return;

        Location targetLoc = result.target().getLocation();
        compassItem.editMeta(CompassMeta.class, meta -> {
            meta.setLodestone(targetLoc);
            meta.setLodestoneTracked(false);
        });
        lastTargetLocations.put(player.getUniqueId(), targetLoc);
    }

    @Override
    public void clear(UUID playerUuid) {
        lastTargetLocations.remove(playerUuid);
    }

    @Override
    public void shutdown() {
        lastTargetLocations.clear();
        lastTargetLocations = null;
        findNearestPlayer = null;
        teamManager = null;
    }
}