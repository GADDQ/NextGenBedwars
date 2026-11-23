package top.earthstudio.nextgenbedwars.api.particle;

import org.bukkit.Location;

public interface IParticleAnimationSequence {
    ParticleData get(Location location);
}
