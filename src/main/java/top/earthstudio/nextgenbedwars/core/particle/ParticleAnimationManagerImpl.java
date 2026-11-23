package top.earthstudio.nextgenbedwars.core.particle;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.particle.IParticleComplexAnimation;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationManager;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleAnimationManagerImpl implements ParticleAnimationManager {
    private static final class ComplexAnimationEntry {
        final IParticleComplexAnimation animation;
        final Location location;
        final List<ObjectObjectImmutablePair<UUID, ParticleAnimationSequence>> activeChildren = new ObjectArrayList<>();

        ComplexAnimationEntry(IParticleComplexAnimation animation, Location location) {
            this.animation = animation;
            this.location = location;
        }
    }

    private Map<Location, Map<UUID, ParticleAnimationSequence>> particlesMap;
    private Map<UUID, ComplexAnimationEntry> complexAnimations;

    private UUID uuid;

    private static ParticleAnimationManager handler;

    public static ParticleAnimationManager get() {
        return handler;
    }

    public ParticleAnimationManagerImpl() {
        particlesMap = new Object2ObjectOpenHashMap<>();
        complexAnimations = new Object2ObjectOpenHashMap<>();

        uuid = GlobalTicker.add(new TickerTask() {
            @Override
            public void shutdown() {
                complexAnimations.clear();
                particlesMap.clear();
            }

            @Override
            public void run() {
                ObjectArrayList<ObjectObjectImmutablePair<Location, UUID>> pendingRemoveSequences = new ObjectArrayList<>();
                ObjectArrayList<UUID> pendingRemoveComplexAnimations = new ObjectArrayList<>();

                complexAnimations.forEach((complexUuid, entry) -> {
                    IParticleComplexAnimation complexAnimation = entry.animation;

                    Map<UUID, ParticleAnimationSequence> locationMap = particlesMap.get(entry.location);
                    if (locationMap == null) {
                        entry.activeChildren.clear();
                    } else {
                        entry.activeChildren.removeIf(pair -> !locationMap.containsKey(pair.left()));
                        for (var child : entry.activeChildren) {
                            child.right().isPause = complexAnimation.isPause;
                        }
                    }

                    if (complexAnimation.isPause) {
                        return;
                    }

                    ObjectArrayList<ParticleAnimationSequence> sequences = complexAnimation.timeLine.get(complexAnimation.frame);
                    if (sequences != null) {
                        for (ParticleAnimationSequence sequence : sequences) {
                            sequence.isPause = false;
                            sequence.frame = sequence.isReversed ? sequence.particleSequence.size() - 1 : 0;
                            UUID childUuid = play(entry.location, sequence);
                            entry.activeChildren.add(new ObjectObjectImmutablePair<>(childUuid, sequence));
                        }
                    }

                    int lastFrame = complexAnimation.timeLine.lastIntKey();
                    if (!complexAnimation.isReversed) {
                        if (complexAnimation.frame < lastFrame)
                            complexAnimation.frame++;
                        else if (complexAnimation.isLoop)
                            complexAnimation.frame = 0;
                        else
                            pendingRemoveComplexAnimations.add(complexUuid);
                    } else {
                        if (complexAnimation.frame > 0)
                            complexAnimation.frame--;
                        else if (complexAnimation.isLoop)
                            complexAnimation.frame = lastFrame;
                        else
                            pendingRemoveComplexAnimations.add(complexUuid);
                    }
                });

                particlesMap.forEach((location, map) ->
                        map.forEach((uuid, particleAnimationSequence) -> {
                            // 1. 播放粒子
                            if (particleAnimationSequence.particleSequence.get(particleAnimationSequence.frame) != null) {
                                particleAnimationSequence.particleSequence.get(particleAnimationSequence.frame)
                                        .clone()
                                        .spawn();
                            }

                            // 2. 帧数计算
                            if (!particleAnimationSequence.isPause) {
                                if (!particleAnimationSequence.isReversed) {
                                    if (particleAnimationSequence.frame < particleAnimationSequence.particleSequence.size() - 1) {
                                        particleAnimationSequence.frame++;
                                    } else if (!particleAnimationSequence.isLoop) {
                                        // 非循环动画播放完毕后移除
                                        pendingRemoveSequences.add(new ObjectObjectImmutablePair<>(location, uuid));
                                    } else {
                                        // 循环动画重置
                                        particleAnimationSequence.frame = 0;
                                    }
                                } else {
                                    if (particleAnimationSequence.frame > 0) {
                                        particleAnimationSequence.frame--;
                                    } else if (!particleAnimationSequence.isLoop) {
                                        // 非循环动画播放完毕后移除
                                        pendingRemoveSequences.add(new ObjectObjectImmutablePair<>(location, uuid));
                                    } else {
                                        // 循环动画重置
                                        particleAnimationSequence.frame = particleAnimationSequence.particleSequence.size() - 1;
                                    }
                                }
                            }
                        })
                );

                if (!pendingRemoveComplexAnimations.isEmpty()) {
                    pendingRemoveComplexAnimations.forEach(complexAnimations::remove);
                    pendingRemoveComplexAnimations.clear();
                }

                if (!pendingRemoveSequences.isEmpty()) {
                    pendingRemoveSequences.forEach(pair -> stop(pair.right()));
                    pendingRemoveSequences.clear();
                }
            }
        });

        handler = this;
    }

    @Override
    public UUID play(Location location, ParticleAnimationSequence particleAnimationSequence) {
        UUID uuid = UUID.randomUUID();
        particlesMap.computeIfAbsent(location.clone(), k -> new ConcurrentHashMap<>()).put(uuid, particleAnimationSequence);
        return uuid;
    }

    /**
     * 移除特定 UUID 的动画
     */
    @Override
    public void stop(UUID uuid) {
        for (var map : particlesMap.values()) {
            if (map.remove(uuid) != null)
                return;
        }
    }

    /**
     * 移除该位置下的所有动画
     */
    @Override
    public void stopAll(Location location) {
        particlesMap.remove(location);
    }

    @Override
    public UUID playComplex(Location location, IParticleComplexAnimation animation) {
        Location targetLocation = location.clone();
        animation.location = targetLocation;

        UUID complexUuid = UUID.randomUUID();
        complexAnimations.put(complexUuid, new ComplexAnimationEntry(animation, targetLocation));
        return complexUuid;
    }

    @Override
    public void stopComplex(UUID complexUuid) {
        ComplexAnimationEntry entry = complexAnimations.remove(complexUuid);
        if (entry != null) {
            for (var child : entry.activeChildren) {
                stop(child.left());
            }
            entry.activeChildren.clear();
        }
    }

    public void shutdown() {
        handler = null;

        GlobalTicker.remove(uuid);
        uuid = null;

        complexAnimations.clear();
        complexAnimations = null;
        particlesMap.clear();
        particlesMap = null;
    }
}