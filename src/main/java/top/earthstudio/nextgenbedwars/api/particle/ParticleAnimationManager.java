package top.earthstudio.nextgenbedwars.api.particle;

import org.bukkit.Location;

import java.util.UUID;

@SuppressWarnings("unused")
public interface ParticleAnimationManager {
    UUID play(Location location, ParticleAnimationSequence particleAnimationSequence);
    void stop(UUID uuid);
    void stopAll(Location location);
    UUID playComplex(Location location, IParticleComplexAnimation animation);
    void stopComplex(UUID complexUuid);
}
