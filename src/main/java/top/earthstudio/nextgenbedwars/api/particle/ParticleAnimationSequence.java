package top.earthstudio.nextgenbedwars.api.particle;

import com.destroystokyo.paper.ParticleBuilder;

import java.util.List;

@SuppressWarnings("unused")
public class ParticleAnimationSequence { // struct ParticleAnimationSequence
    public List<ParticleBuilder> particleSequence;
    public int frame;
    public boolean isLoop;
    public boolean isPause = false;
    public boolean isReversed = false;

    public ParticleAnimationSequence(List<ParticleBuilder> particleSequence, int frame, boolean isLoop) {
        this.particleSequence = particleSequence;
        this.frame = frame;
        this.isLoop = isLoop;
    }

    public ParticleAnimationSequence(List<ParticleBuilder> particleSequence, boolean isLoop) {
        this(particleSequence, 0, isLoop);
    }
}
