package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder;

import io.papermc.paper.datacomponent.DataComponentTypes;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationManager;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.listener.WarpPowderListener;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle.WarpPowderRespawnPointSequence;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle.WarpPowderTeleportSequence;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle.WarpPowderUsingAnimation;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class WarpPowderManagerImpl implements WarpPowderManager {
    private Map<UUID, WarpPowderProp> activeWarpPowders;
    private Map<WarpPowderProp, Integer> warpPowderCountdowns;
    private Map<UUID, UUID> ringAnimations;
    private Map<UUID, ObjectArrayList<UUID>> animationSequences;

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
        addSequence(player, WarpPowderTeleportSequence.get(player.getLocation()));
        addSequence(player, WarpPowderRespawnPointSequence.get(teamManager.get(player).respawnLocation));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.7f);
        player.getWorld().playSound(teamManager.get(player).respawnLocation, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.7f);

        activeWarpPowders.put(player.getUniqueId(), warpPowder);
        warpPowderCountdowns.put(warpPowder, warpPowder.usingTick);
        return true;
    }

    @Override
    public boolean contains(Player player) {
        return activeWarpPowders.get(player.getUniqueId()) != null;
    }

    @Override
    public void remove(Player player) {
        particleAnimationManager.stopComplex(ringAnimations.remove(player.getUniqueId()));
        var sequences = animationSequences.get(player.getUniqueId());
        sequences.forEach(uuid -> particleAnimationManager.stop(uuid));
        sequences.clear();
        WarpPowderProp warpPowder = activeWarpPowders.remove(player.getUniqueId());
        warpPowder.propItem.setData(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey());
        warpPowder.propItem.editPersistentDataContainer(pdc -> pdc.remove(PdcKeys.prop.warp_powder_active));
        warpPowder.setCooldownFor(player);
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
            pendingRemoves.forEach(player -> {
                var warpPowder = activeWarpPowders.get(player.getUniqueId());
                remove(player);
                warpPowder.propItem.setAmount(warpPowder.propItem.getAmount() - 1);
            });
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

        animationSequences.forEach((playerUuid, sequences) -> sequences.forEach(sequenceUuid -> particleAnimationManager.stop(sequenceUuid)));
        animationSequences.clear();
        animationSequences = null;

        particleAnimationManager = null;

        warpPowderCountdowns.clear();
        warpPowderCountdowns = null;

        activeWarpPowders.clear();
        activeWarpPowders = null;
    }

    private void addSequence(Player player, ParticleAnimationSequence sequence) {
        if (!animationSequences.containsKey(player.getUniqueId()))
            animationSequences.put(player.getUniqueId(), new ObjectArrayList<>());

        animationSequences.get(player.getUniqueId()).add(particleAnimationManager.play(player.getLocation(), sequence));
    }
}
