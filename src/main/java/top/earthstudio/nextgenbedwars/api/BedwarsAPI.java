package top.earthstudio.nextgenbedwars.api;

import org.bukkit.plugin.java.JavaPlugin;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;

public interface BedwarsAPI {
    /**
     * 外部调用的唯一入口
     */
    static BedwarsAPI getInstance() {
        BedwarsAPI api = Holder.INSTANCE;
        if (api == null) {
            throw new IllegalStateException("NextGenBedwars API is not initialized yet or has been shut down!");
        }
        return api;
    }

    // ================= 暴露给外部调用的能力接口 =================
    JavaPlugin getPlugin();

    <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor);
    // TODO: 以后在这里扩展：GameManager getGameManager(); 等等? GameInstance info? 如何开放多例抽象单例api？

    // ================= 核心挂载与注销通道 =================
    static void register(BedwarsAPI implementation) {
        Holder.set(implementation);
    }

    static void unregister() {
        Holder.set(null);
    }

    final class Holder {
        private static BedwarsAPI INSTANCE;

        private static void set(BedwarsAPI api) {
            if (INSTANCE != null && api != null) {
                throw new IllegalStateException("BedwarsAPI is already registered!");
            }
            INSTANCE = api;
        }
    }
}