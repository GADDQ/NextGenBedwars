package top.earthstudio.nextgenbedwars.api.world;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.joml.Vector3i;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.List;
import java.util.UUID;

public interface WorldProtector extends IGameSubSystem {
    @Override
    default void update() {

    }

    /**
     * 注册保护组
     * */
    public UUID addGroup(LongSet group);

    /**
     * 移除保护组
     * */
    public void removeGroup(UUID uuid);

    /**
     * 判断某个方块是否被保护
     */
    public boolean contains(long blockLong);

    /**
     * 查询指定方块当前被哪些保护组持有。
     *
     * <p><b>前置条件：</b>该方块必须被至少一个保护组持有，即
     * {@link #contains(long)} 返回 {@code true}。
     * 否则说明调用方的逻辑存在错误，将返回 {@code null}。
     *
     * <p>典型用法：
     * <pre>{@code
     * if (protector.contains(blockLong)) {
     *     List<UUID> owners = protector.getOwnerGroupUuids(blockLong);
     *     // owners 保证非 null 且非空
     * }
     * }</pre>
     *
     * @param blockLong 由 {@link top.earthstudio.nextgenbedwars.api.util.BlockPosUtil#asLong}
     *                  压缩后的方块坐标
     * @return 持有该方块的所有保护组 UUID，保证非 null 且非空
     */
    public List<UUID> getOwnerGroupUuids(long blockLong);

    public void addBlockToGroup(UUID uuid, long blockLong);
    public void addBlocksToGroup(UUID uuid, LongSet blockLongs);
    public void addRegionToGroup(UUID uuid, Pair<Vector3i, Vector3i> region);
    public void removeBlockFromGroup(UUID uuid, long blockLong);
    public void removeBlocksFromGroup(UUID uuid, LongSet blockLongs);
    public void removeRegionFromGroup(UUID uuid, Pair<Vector3i, Vector3i> region);
}
