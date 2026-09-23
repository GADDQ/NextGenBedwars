package top.earthstudio.nextgenbedwars.api.util;

import org.bukkit.block.Block;
import org.joml.Vector3i;

public final class BlockPosUtil {

    private BlockPosUtil() {}

    // ================= 压缩：将 (x, y, z) 压成 64 位 long =================

    /**
     * 将三维整型坐标压缩为 1 个 long
     * 空间划分：X 占 26位, Y 占 12位, Z 占 26位 (总计 64 bits)
     */
    public static long asLong(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (y & 0xFFF) << 26) | ((long) (z & 0x3FFFFFF));
    }

    public static long asLong(Block block) {
        return asLong(block.getX(), block.getY(), block.getZ());
    }

    public static long asLong(Vector3i vec) {
        return asLong(vec.x, vec.y, vec.z);
    }

    // ================= 解压：从 long 反解出 x, y, z (自动保留正负号) =================

    /**
     * 解压 X 坐标（最高 26 位）
     */
    public static int getX(long packed) {
        return (int) (packed >> 38);
    }

    /**
     * 解压 Y 坐标（中间 12 位）
     */
    public static int getY(long packed) {
        return (int) ((packed << 26) >> 52);
    }

    /**
     * 解压 Z 坐标（最低 26 位）
     */
    public static int getZ(long packed) {
        return (int) ((packed << 38) >> 38);
    }
}