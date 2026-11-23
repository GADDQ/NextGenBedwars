package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle;

import com.destroystokyo.paper.ParticleBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;

import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;

import java.util.List;

public class WarpPowderRingSequenceBase {
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final int totalFrames;

    public WarpPowderRingSequenceBase(double offsetX, double offsetY, double offsetZ, int totalFrames) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.totalFrames = totalFrames;
    }

    public ParticleAnimationSequence get(Location location) {
        Location targetLoc = location.clone().add(offsetX, offsetY, offsetZ);
        ParticleBuilder builder = Particle.FIREWORK.builder()
                .location(targetLoc)
                .count(1)
                .extra(0);

        List<ParticleBuilder> frames = new ObjectArrayList<>(totalFrames);
        for (int i = 0; i < totalFrames; i++) {
            frames.add(builder);
        }

        return new ParticleAnimationSequence(frames, false);
    }
}