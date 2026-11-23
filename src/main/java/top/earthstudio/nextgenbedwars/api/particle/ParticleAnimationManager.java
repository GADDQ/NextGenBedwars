package top.earthstudio.nextgenbedwars.api.particle;

import org.bukkit.Location;

import java.util.UUID;

@SuppressWarnings("unused")
public interface ParticleAnimationManager {
    UUID set(Location location, ParticleAnimationSequence particleAnimationSequence);
    void remove(Location location, UUID uuid);
    void removeAll(Location location);
    UUID playComplex(Location location, IParticleComplexAnimation animation);
    void stopComplex(UUID complexUuid);
}
