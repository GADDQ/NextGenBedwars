package top.earthstudio.nextgenbedwars.core.spawner;

import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

public class SpawnerAnimator {

    // ================= 补间动画相关 =================
    private static final int ANIM_PERIOD = 160;
    private int animTick = 0;

    // 内存复用，避免每一帧 new JOML 对象造成 GC 压力
    private int bufferIndex = 0;
    private final Transformation[] transformBuffers = new Transformation[] {
            new Transformation(
                    new Vector3f(0, 2.3f, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(0.75f, 0.75f, 0.75f),
                    new AxisAngle4f(0, 0, 0, 0)
            ),

            new Transformation(
                    new Vector3f(0, 2.3f, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(0.75f, 0.75f, 0.75f),
                    new AxisAngle4f(0, 0, 0, 0)
            )
    };

    private static final int KEYFRAME_STRIDE = 5; // 每 5 tick 一个关键帧
    private int frameCounter = 0;
    private static final float[] COS_LUT = new float[ANIM_PERIOD];
    static {
        for (int i = 0; i < ANIM_PERIOD; i++) {
            COS_LUT[i] = (float) Math.cos((double) i / ANIM_PERIOD * 2.0 * Math.PI);
        }
    }
    // ===============================================

    public void update(ItemDisplay itemDisplay, Spawner spawner) {
        if (spawner.holoMaterial == null || spawner.holoMaterial == Material.AIR)
            return;

        frameCounter++;
        if (frameCounter < KEYFRAME_STRIDE) return;
        frameCounter = 0;

        animTick = (animTick + KEYFRAME_STRIDE) % ANIM_PERIOD;
        float cosVal = COS_LUT[animTick];

        bufferIndex = 1 - bufferIndex;
        Transformation current = transformBuffers[bufferIndex];
        current.getTranslation().y = 2.3f - 0.1f * cosVal;
        current.getLeftRotation().setAngleAxis((float) (2 * Math.PI * (1.0 - cosVal)), 0, 1, 0);

        itemDisplay.setInterpolationDuration(KEYFRAME_STRIDE); // 让客户端在这几个 tick 里自己补间
        itemDisplay.setInterpolationDelay(0);
        itemDisplay.setTransformation(current);
    }
}
