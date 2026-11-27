package top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep;

import com.destroystokyo.paper.ParticleBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Transformation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;
import top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep.ActiveSheep;
import top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep.TNTSheepManager;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.FindNearestPlayer;

import top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.particle.TNTSheepFuseSequence;
import top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.listener.TNTSheepListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TNTSheepManagerImpl implements TNTSheepManager { // ATTENTION: This was built by LLM.
    private static final int FUSE_TICKS = 160;
    private static final float EXPLOSION_YIELD = 4.0f;
    private static final double MOVE_SPEED = 1.35;
    private static final int PATH_UPDATE_INTERVAL = 5;

    private static final BlockData TNT_BLOCK = Material.TNT.createBlockData();
    private static final BlockData FLASH_BLOCK = Material.WHITE_CONCRETE.createBlockData();

    private final GameInstance gameInstance;
    private TeamManager teamManager;
    private TNTSheepListener listener;

    private Map<UUID, ActiveSheep> activeBySheep;
    private List<UUID> pendingExplode;
    private Team explodingTeam;
    private int tickCounter;

    public TNTSheepManagerImpl(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.activeBySheep = new Object2ObjectOpenHashMap<>();
        this.pendingExplode = new ObjectArrayList<>();
        this.listener = new TNTSheepListener(this, gameInstance);
    }

    @Override
    public void onReady() {
        this.teamManager = gameInstance.get(TeamManager.class);
        Bukkit.getPluginManager().registerEvents(listener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public boolean spawn(Player owner, Location location) {
        if (teamManager == null) return false;
        World world = gameInstance.getWorld();
        if (world == null) return false;

        FindNearestPlayer.Result result = FindNearestPlayer.findNearest(owner, world.getPlayers(), teamManager);
        if (result == null) return false;

        Player target = result.target();
        Team ownerTeam = teamManager.get(owner);
        DyeColor sheepColor = resolveDyeColor(ownerTeam != null ? ownerTeam.teamColor : Color.WHITE);

        Sheep sheep = world.spawn(location, Sheep.class, entity -> {
            entity.setAdult();
            entity.setBreed(false);
            entity.setColor(sheepColor);
            Bukkit.getMobGoals().removeAllGoals(entity);
            var followRange = entity.getAttribute(Attribute.FOLLOW_RANGE);
            if (followRange != null) {
                followRange.setBaseValue(128.0);
            }
        });

        BlockDisplay tntDisplay = world.spawn(location, BlockDisplay.class, entity -> {
            entity.setBlock(TNT_BLOCK);
            entity.setTeleportDuration(2);
            entity.setRotation(sheep.getBodyYaw(), 0.0f);
            entity.setTransformation(new Transformation(
                    new Vector3f(-0.425f, -0.70f, -0.425f),
                    new Quaternionf(),
                    new Vector3f(0.85f, 0.85f, 0.85f),
                    new Quaternionf()
            ));
        });

        TextDisplay textDisplay = world.spawn(location, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setShadowed(true);
            entity.text(formatCountdown(FUSE_TICKS));
            entity.setTransformation(new Transformation(
                    new Vector3f(0.0f, 0.50f, 0.0f),
                    new Quaternionf(),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Quaternionf()
            ));
        });

        Location fuseBaseLoc = sheep.getLocation();
        ParticleAnimationSequence fuseSequence = TNTSheepFuseSequence.get(fuseBaseLoc);
        UUID fuseParticleUuid = BedwarsAPI.getInstance()
                .getParticleAnimationManager()
                .play(fuseBaseLoc, fuseSequence);

        sheep.addPassenger(tntDisplay);
        sheep.addPassenger(textDisplay);

        world.playSound(location, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

        activeBySheep.put(
                sheep.getUniqueId(),
                new ActiveSheep(sheep, tntDisplay, textDisplay, owner, ownerTeam, target, FUSE_TICKS, fuseSequence, fuseParticleUuid)
        );
        return true;
    }

    @Override
    public void update() {
        if (activeBySheep.isEmpty()) return;

        tickCounter++;
        boolean shouldUpdatePath = (tickCounter % PATH_UPDATE_INTERVAL) == 0;

        for (var entry : activeBySheep.entrySet()) {
            ActiveSheep active = entry.getValue();
            if (!active.sheep.isValid()) {
                pendingExplode.add(entry.getKey());
                continue;
            }

            active.tntDisplay.setRotation(active.sheep.getBodyYaw(), 0.0f);

            active.remainingTicks--;
            if (active.remainingTicks <= 0) {
                pendingExplode.add(entry.getKey());
                continue;
            }

            Location fuseLoc = active.sheep.getLocation().add(0, 1.35, 0);
            for (ParticleBuilder builder : active.fuseSequence.particleSequence) {
                if (builder != null) {
                    builder.location(fuseLoc);
                }
            }

            int flashInterval = active.remainingTicks <= 40 ? 2 : 5;
            if (active.remainingTicks % flashInterval == 0) {
                active.flashState = !active.flashState;
                active.tntDisplay.setBlock(active.flashState ? FLASH_BLOCK : TNT_BLOCK);
            }

            if (active.remainingTicks % 2 == 0) {
                active.textDisplay.text(formatCountdown(active.remainingTicks));
            }

            if (shouldUpdatePath && active.target.isOnline()
                    && active.target.getWorld() == active.sheep.getWorld()
                    && active.target.getGameMode() != GameMode.SPECTATOR) {
                active.sheep.getPathfinder().moveTo(active.target, MOVE_SPEED);
            }
        }

        if (!pendingExplode.isEmpty()) {
            for (UUID sheepUuid : pendingExplode) {
                detonate(sheepUuid);
            }
            pendingExplode.clear();
        }
    }

    private void detonate(UUID sheepUuid) {
        ActiveSheep active = activeBySheep.remove(sheepUuid);
        if (active == null) return;

        Location explodeLoc = active.sheep.isValid() ? active.sheep.getLocation() : null;
        cleanupActive(active);

        if (explodeLoc != null && explodeLoc.getWorld() != null) {
            explodingTeam = active.ownerTeam;
            try {
                explodeLoc.getWorld().createExplosion(explodeLoc, EXPLOSION_YIELD, false, true, active.owner);
            } finally {
                explodingTeam = null;
            }
        }
    }

    private void cleanupActive(ActiveSheep active) {
        if (active.fuseParticleUuid != null) {
            BedwarsAPI.getInstance().getParticleAnimationManager().stop(active.fuseParticleUuid);
        }
        if (active.tntDisplay.isValid()) active.tntDisplay.remove();
        if (active.textDisplay.isValid()) active.textDisplay.remove();
        if (active.sheep.isValid()) active.sheep.remove();
    }

    private Component formatCountdown(int ticks) {
        double seconds = ticks / 20.0;
        NamedTextColor color = ticks <= 40 ? NamedTextColor.RED : (ticks <= 80 ? NamedTextColor.YELLOW : NamedTextColor.GREEN);
        return Component.text(String.format("%.1fs", seconds), color);
    }

    private DyeColor resolveDyeColor(Color color) {
        if (color == null) return DyeColor.WHITE;
        DyeColor exact = DyeColor.getByColor(color);
        if (exact != null) return exact;

        DyeColor closest = DyeColor.WHITE;
        int minDist = Integer.MAX_VALUE;
        for (DyeColor dye : DyeColor.values()) {
            Color c = dye.getColor();
            int dr = c.getRed() - color.getRed();
            int dg = c.getGreen() - color.getGreen();
            int db = c.getBlue() - color.getBlue();
            int dist = dr * dr + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                closest = dye;
            }
        }
        return closest;
    }

    @Override
    public boolean isManagedSheep(Entity entity) {
        return entity != null && activeBySheep.containsKey(entity.getUniqueId());
    }

    @Override
    public Team getExplodingTeam() {
        return explodingTeam;
    }

    @Override
    public void removeBySheep(UUID sheepUuid) {
        ActiveSheep active = activeBySheep.remove(sheepUuid);
        if (active != null) {
            cleanupActive(active);
        }
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(listener);
        listener = null;

        for (ActiveSheep active : activeBySheep.values()) {
            cleanupActive(active);
        }

        activeBySheep.clear();
        activeBySheep = null;
        pendingExplode.clear();
        pendingExplode = null;
        teamManager = null;
    }
}