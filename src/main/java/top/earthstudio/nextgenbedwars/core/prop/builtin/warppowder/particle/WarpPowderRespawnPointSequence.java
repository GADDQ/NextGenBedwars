package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Location;
import org.bukkit.Particle;

import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;

import java.util.List;

public class WarpPowderRespawnPointSequence {
    static public ParticleAnimationSequence get(Location location) {
        Location targetLoc = location.clone().add(0, 1, 0);

        ParticleBuilder builder = Particle.REVERSE_PORTAL.builder()
                .location(targetLoc)
                .count(25)
                .extra(0.5);

        return new ParticleAnimationSequence(List.of(builder), true);
    }
}
