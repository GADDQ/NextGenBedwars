package top.earthstudio.nextgenbedwars.api.particle;

import com.destroystokyo.paper.ParticleBuilder;

import java.util.List;

@SuppressWarnings("unused")
public class ParticleData { // struct ParticleData
    public List<ParticleBuilder> particleSequence;
    public int frame;
    public boolean loop;

    public ParticleData(List<ParticleBuilder> particleSequence, int frame, boolean loop) {
        this.particleSequence = particleSequence;
        this.frame = frame;
        this.loop = loop;
    }

    public ParticleData(List<ParticleBuilder> particleSequence, boolean loop) {
        this(particleSequence, 0, loop);
    }
}
