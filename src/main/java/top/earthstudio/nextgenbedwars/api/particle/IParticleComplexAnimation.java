package top.earthstudio.nextgenbedwars.api.particle;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Location;

@SuppressWarnings("unused")
public abstract class IParticleComplexAnimation {
    public int frame;
    public boolean isLoop;
    public boolean isPause = false;
    public boolean isReversed = false;

    public Location location;

    public Int2ObjectSortedMap<ObjectArrayList<ParticleAnimationSequence>> timeLine = new Int2ObjectRBTreeMap<>();

    protected IParticleComplexAnimation(Location location) {
        this(location, false, 0);
    }

    protected IParticleComplexAnimation(Location location, boolean isLoop) {
        this(location, isLoop, 0);
    }

    protected IParticleComplexAnimation(Location location, boolean isLoop, int startFrame) {
        this.location = location.clone();
        this.isLoop = isLoop;
        this.frame = startFrame;
    }

    protected void put(int frame, ParticleAnimationSequence sequence) {
        timeLine.computeIfAbsent(frame, k -> new ObjectArrayList<>()).add(sequence);
    }
}