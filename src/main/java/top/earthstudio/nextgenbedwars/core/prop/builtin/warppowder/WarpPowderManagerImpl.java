package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import org.bukkit.event.inventory.InventoryType;
import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.listener.WarpPowderListener;

import java.util.Map;
import java.util.UUID;

public class WarpPowderManagerImpl implements WarpPowderManager {
    private Map<UUID, WarpPowderProp> activeWarpPowders;
    private Map<WarpPowderProp, Integer> warpPowderCountdowns;

    private GameInstance gameInstance;
    private TeamManager teamManager;

    private WarpPowderListener warpPowderListener;

    public WarpPowderManagerImpl(GameInstance gameInstance) {
        activeWarpPowders = new Object2ObjectOpenHashMap<>();
        warpPowderCountdowns = new Object2IntOpenHashMap<>();

        this.gameInstance = gameInstance;

        warpPowderListener = new WarpPowderListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(warpPowderListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void onReady() {
        teamManager = gameInstance.get(TeamManager.class);
    }

    @Override
    public void use(Player player, WarpPowderProp warpPowder) {
        activeWarpPowders.put(player.getUniqueId(), warpPowder);
        warpPowderCountdowns.put(warpPowder, warpPowder.countdownTick);
    }

    @Override
    public boolean contains(Player player) {
        return activeWarpPowders.get(player.getUniqueId()) != null;
    }

    @Override
    public void remove(Player player) {
        WarpPowderProp warpPowder = activeWarpPowders.remove(player.getUniqueId());
        warpPowder.propItem.editPersistentDataContainer(pdc -> pdc.remove(PdcKeys.prop.warp_powder_active));
        warpPowderCountdowns.remove(warpPowder);
    }

    @Override
    public void update() {
        ObjectArrayList<Player> pendingRemoves = new ObjectArrayList<>();
        for (var entry : activeWarpPowders.entrySet()) {
            WarpPowderProp warpPowder = entry.getValue();
            int leftTick = warpPowderCountdowns.get(warpPowder);
            if (leftTick <= 0) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Team team = teamManager.get(player);

                // TODO: FIXME!!! This is a temp fix, should use statement system later to remove player when player logout
                if (player != null) {
                    InventoryType type = player.getOpenInventory().getType();
                    if (type != InventoryType.CRAFTING && type != InventoryType.CREATIVE)
                        player.closeInventory();

                    player.teleport(team.respawnLocation);
                    warpPowder.propItem.setAmount(warpPowder.propItem.getAmount() - 1);
                }
                pendingRemoves.add(player);
                continue;
            }
            warpPowderCountdowns.put(warpPowder, leftTick - 1);
        }

        if (!pendingRemoves.isEmpty()) {
            pendingRemoves.forEach(this::remove);
            pendingRemoves.clear();
        }
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(warpPowderListener);
        warpPowderListener = null;

        this.gameInstance = null;
        this.teamManager = null;

        warpPowderCountdowns.clear();
        warpPowderCountdowns = null;

        activeWarpPowders.clear();
        activeWarpPowders = null;
    }
}
