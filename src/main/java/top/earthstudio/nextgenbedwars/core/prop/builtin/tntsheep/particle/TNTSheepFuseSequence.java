package top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.particle;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Location;
import org.bukkit.Particle;

import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;

import java.util.Arrays;

public class TNTSheepFuseSequence {
    public static ParticleAnimationSequence get(Location location) {
        Location fuseLoc = location.clone().add(0, 1.35, 0);

        ParticleBuilder smoke = Particle.SMOKE.builder()
                .location(fuseLoc)
                .count(2)
                .offset(0.08, 0.05, 0.08)
                .extra(0.02);

        ParticleBuilder flame = Particle.FLAME.builder()
                .location(fuseLoc)
                .count(1)
                .offset(0.06, 0.04, 0.06)
                .extra(0.01);

        ParticleBuilder spark = Particle.LAVA.builder()
                .location(fuseLoc)
                .count(1)
                .offset(0.05, 0.02, 0.05)
                .extra(0.0);

        return new ParticleAnimationSequence(Arrays.asList(smoke, flame, smoke, spark), true);
    }
}