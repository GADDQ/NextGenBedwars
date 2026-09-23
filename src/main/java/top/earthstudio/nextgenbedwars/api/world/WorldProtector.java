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
     * 查出这个方块由哪些组持有
     */
    public List<UUID> getOwnerGroupUuids(long blockLong);

    public void addBlockToGroup(UUID uuid, long blockLong);
    public void addBlocksToGroup(UUID uuid, LongSet blockLongs);
    public void addRegionToGroup(UUID uuid, Pair<Vector3i, Vector3i> region);
    public void removeBlockFromGroup(UUID uuid, long blockLong);
    public void removeBlocksFromGroup(UUID uuid, LongSet blockLongs);
    public void removeRegionFromGroup(UUID uuid, Pair<Vector3i, Vector3i> region);
}
