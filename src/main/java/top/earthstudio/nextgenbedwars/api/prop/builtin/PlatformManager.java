package top.earthstudio.nextgenbedwars.api.prop.builtin;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.core.prop.builtin.platform.PlatformProp;

public interface PlatformManager extends IGameSubSystem {
    void use(PlatformProp platform);
    LongArrayList getAll();
}
