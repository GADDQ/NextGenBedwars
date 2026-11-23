package top.earthstudio.nextgenbedwars.api.particle;

import org.bukkit.Location;

import java.util.UUID;

@SuppressWarnings("unused")
public interface ParticleAnimationManager {
    UUID set(Location location, IParticleAnimationSequence sequence);
    UUID set(Location location, ParticleData particleData);
    void remove(Location location, UUID uuid);
    void removeAll(Location location);
}
