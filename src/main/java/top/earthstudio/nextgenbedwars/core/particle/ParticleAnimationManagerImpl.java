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
    private Map<Location, Map<UUID, ParticleAnimationSequence>> particlesMap;
    private List<IParticleComplexAnimation> complexAnimations;

    private UUID uuid;

    private static ParticleAnimationManager handler;

    public static ParticleAnimationManager get() {
        return handler;
    }

    public ParticleAnimationManagerImpl() {
        particlesMap = new Object2ObjectOpenHashMap<>();
        complexAnimations = new ObjectArrayList<>();

        uuid = GlobalTicker.add(new TickerTask() {
            @Override
            public void shutdown() {
                particlesMap.clear();
            }

            @Override
            public void run() {
                ObjectArrayList<ObjectObjectImmutablePair<Location, UUID>> pendingRemoveSequences = new ObjectArrayList<>();
                ObjectArrayList<IParticleComplexAnimation> pendingRemoveComplexAnimations = new ObjectArrayList<>();

                complexAnimations.forEach(complexAnimation -> {
                    ObjectArrayList<ParticleAnimationSequence> sequences = complexAnimation.timeLine.get(complexAnimation.frame);
                    if (sequences != null) {
                        sequences.forEach(sequence -> sequence.isPause = complexAnimation.isPause);

                        if (!complexAnimation.isPause) {
                            sequences.forEach(sequence -> set(complexAnimation.location, sequence));
                        }
                    }

                    if (!complexAnimation.isReversed) {
                        if (complexAnimation.frame < complexAnimation.timeLine.keySet().stream().toList().getLast())
                            complexAnimation.frame++;
                        else if (complexAnimation.isLoop)
                            complexAnimation.frame = 0;
                        else pendingRemoveComplexAnimations.add(complexAnimation);
                    } else {
                        if (complexAnimation.frame > 0)
                            complexAnimation.frame--;
                        else if (complexAnimation.isLoop)
                            complexAnimation.frame = complexAnimation.timeLine.keySet().stream().toList().getLast();
                        else pendingRemoveComplexAnimations.add(complexAnimation);
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
                    pendingRemoveComplexAnimations.forEach(complexAnimation -> complexAnimations.remove(complexAnimation));
                    pendingRemoveComplexAnimations.clear();
                }

                if (!pendingRemoveSequences.isEmpty()) {
                    pendingRemoveSequences.forEach(pair -> remove(pair.left(), pair.right()));
                    pendingRemoveSequences.clear();
                }
            }
        });

        handler = this;
    }

    @Override
    public UUID set(Location location, ParticleAnimationSequence particleAnimationSequence) {
        UUID uuid = UUID.randomUUID();
        particlesMap.computeIfAbsent(location.clone(), k -> new ConcurrentHashMap<>()).put(uuid, particleAnimationSequence);
        return uuid;
    }

    /**
     * 移除特定 UUID 的动画
     */
    @Override
    public void remove(Location location, UUID uuid) {
        Map<UUID, ParticleAnimationSequence> map = particlesMap.get(location);
        if (map != null) {
            map.remove(uuid);
            if (map.isEmpty()) {
                particlesMap.remove(location);
            }
        }
    }

    /**
     * 移除该位置下的所有动画
     */
    @Override
    public void removeAll(Location location) {
        particlesMap.remove(location);
    }

    @Override
    public UUID playComplex(Location location, IParticleComplexAnimation animation) {
        return null;
    }

    @Override
    public void stopComplex(UUID complexUuid) {

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