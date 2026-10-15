package top.earthstudio.nextgenbedwars.api.config.serializer;

import it.unimi.dsi.fastutil.longs.LongLongPair;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import top.earthstudio.nextgenbedwars.api.util.BlockPosUtil;

import java.lang.reflect.Type;
import java.util.List;

public class LongLongPairSerializer implements TypeSerializer<LongLongPair> {
    @Override
    public LongLongPair deserialize(Type type, ConfigurationNode node) {
        List<? extends ConfigurationNode> children = node.childrenList();
        if (children.size() < 2) {
            return null;
        }
        long first = toLong(children.get(0));
        long second = toLong(children.get(1));
        return LongLongPair.of(first, second);
    }

    @Override
    public void serialize(Type type, LongLongPair obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        ConfigurationNode n1 = node.appendListNode();
        ConfigurationNode n2 = node.appendListNode();
        writeLong(n1, obj.firstLong());
        writeLong(n2, obj.secondLong());
    }

    private long toLong(ConfigurationNode n) {
        return BlockPosUtil.asLong(
                n.node("x").getInt(0),
                n.node("y").getInt(0),
                n.node("z").getInt(0)
        );
    }

    private void writeLong(ConfigurationNode n, long packed) throws SerializationException {
        n.node("x").set(BlockPosUtil.getX(packed));
        n.node("y").set(BlockPosUtil.getY(packed));
        n.node("z").set(BlockPosUtil.getZ(packed));
    }
}