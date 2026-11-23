package top.earthstudio.nextgenbedwars.core.particle;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.particle.IParticleAnimationSequence;
import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationManager;
import top.earthstudio.nextgenbedwars.api.particle.ParticleData;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleAnimationManagerImpl implements ParticleAnimationManager {
    private Map<Location, Map<UUID, ParticleData>> particlesMap;
    private UUID uuid;

    private static ParticleAnimationManager handler;

    public static ParticleAnimationManager get() {
        return handler;
    }

    public ParticleAnimationManagerImpl() {
        particlesMap = new Object2ObjectOpenHashMap<>();

        uuid = GlobalTicker.add(new TickerTask() {
            @Override
            public void shutdown() {
                particlesMap.clear();
            }

            @Override
            public void run() {
                ObjectArrayList<ObjectObjectImmutablePair<Location, UUID>> pendingRemoves = new ObjectArrayList<>();

                particlesMap.forEach((location, map) ->
                        map.forEach((uuid, particleData) -> {
                            // 1. 播放粒子
                            if (particleData.particleSequence.get(particleData.frame) != null) {
                                particleData.particleSequence.get(particleData.frame)
                                        .clone()
                                        .spawn();
                            }

                            // 2. 帧数计算
                            if (!particleData.isPause) {
                                if (particleData.frame < particleData.particleSequence.size() - 1) {
                                    if (!particleData.isReversed)
                                        particleData.frame++;
                                    else
                                        particleData.frame--;
                                }
                            } else if (!particleData.isLoop) {
                                // 非循环动画播放完毕后移除
                                pendingRemoves.add(new ObjectObjectImmutablePair<>(location, uuid));
                            } else {
                                // 循环动画重置
                                if (!particleData.isReversed)
                                    particleData.frame = 0;
                                else
                                    particleData.frame = particleData.particleSequence.size() - 1;
                            }
                        })
                );

                if (!pendingRemoves.isEmpty()) {
                    pendingRemoves.forEach(pair -> remove(pair.left(), pair.right()));
                    pendingRemoves.clear();
                }
            }
        });

        handler = this;
    }

    @Override
    public UUID set(Location location, IParticleAnimationSequence sequence) {
        return set(location, sequence.get(location));
    }

    @Override
    public UUID set(Location location, ParticleData particleData) {
        UUID uuid = UUID.randomUUID();
        particlesMap.computeIfAbsent(location.clone(), k -> new ConcurrentHashMap<>()).put(uuid, particleData);
        return uuid;
    }

    /**
     * 移除特定 UUID 的动画
     */
    @Override
    public void remove(Location location, UUID uuid) {
        Map<UUID, ParticleData> map = particlesMap.get(location);
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

    public void shutdown() {
        handler = null;

        GlobalTicker.remove(uuid);
        uuid = null;

        particlesMap.clear();
        particlesMap = null;
    }
}