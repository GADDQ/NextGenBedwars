package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder;

import io.papermc.paper.datacomponent.DataComponentTypes;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.listener.WarpPowderListener;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle.WarpPowderUsingAnimation;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class WarpPowderManagerImpl implements WarpPowderManager {
    private Map<UUID, WarpPowderProp> activeWarpPowders;
    private Map<WarpPowderProp, Integer> warpPowderCountdowns;
    private Map<UUID, UUID> ringAnimations;
    private Map<UUID, UUID> animationSequences;

    private GameInstance gameInstance;
    private TeamManager teamManager;
    private ParticleAnimationManager particleAnimationManager;

    private WarpPowderListener warpPowderListener;

    public WarpPowderManagerImpl(GameInstance gameInstance) {
        activeWarpPowders = new Object2ObjectOpenHashMap<>();
        warpPowderCountdowns = new Object2IntOpenHashMap<>();
        ringAnimations = new Object2ObjectOpenHashMap<>();
        animationSequences = new Object2ObjectOpenHashMap<>();

        this.gameInstance = gameInstance;
        this.particleAnimationManager = BedwarsAPI.getInstance().getParticleAnimationManager();

        warpPowderListener = new WarpPowderListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(warpPowderListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void onReady() {
        teamManager = gameInstance.get(TeamManager.class);
    }

    @Override
    public boolean use(Player player, WarpPowderProp warpPowder) {
        if (activeWarpPowders.containsKey(player.getUniqueId()))
            return false;

        warpPowder.propItem.setData(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey());
        ringAnimations.put(player.getUniqueId(), particleAnimationManager.playComplex(player.getLocation(), new WarpPowderUsingAnimation(player.getLocation())));
        activeWarpPowders.put(player.getUniqueId(), warpPowder);
        warpPowderCountdowns.put(warpPowder, warpPowder.countdownTick);
        return true;
    }

    @Override
    public boolean contains(Player player) {
        return activeWarpPowders.get(player.getUniqueId()) != null;
    }

    @Override
    public void remove(Player player) {
        particleAnimationManager.stopComplex(ringAnimations.remove(player.getUniqueId()));
        WarpPowderProp warpPowder = activeWarpPowders.remove(player.getUniqueId());
        warpPowder.propItem.setData(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey());
        warpPowder.propItem.editPersistentDataContainer(pdc -> pdc.remove(PdcKeys.prop.warp_powder_active));
        warpPowder.propItem.setAmount(warpPowder.propItem.getAmount() - 1);
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

                // TODO: FIXME!!! This is a temp fix, should use statement system later to remove player when player logout
                if (player != null) {
                    Team team = teamManager.get(player);
                    InventoryType type = player.getOpenInventory().getType();
                    if (type != InventoryType.CRAFTING && type != InventoryType.CREATIVE)
                        player.closeInventory();

                    player.teleport(team.respawnLocation);
                    pendingRemoves.add(player);
                }
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

        ringAnimations.forEach((playerUuid, animationUuid) -> particleAnimationManager.stopComplex(animationUuid));
        ringAnimations.clear();
        ringAnimations = null;

        animationSequences.forEach((playerUuid, animationUuid) -> particleAnimationManager.stop(animationUuid));
        animationSequences.clear();
        animationSequences = null;

        particleAnimationManager = null;

        warpPowderCountdowns.clear();
        warpPowderCountdowns = null;

        activeWarpPowders.clear();
        activeWarpPowders = null;
    }
}
