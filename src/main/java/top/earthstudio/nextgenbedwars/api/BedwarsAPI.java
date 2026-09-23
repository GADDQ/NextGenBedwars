package top.earthstudio.nextgenbedwars.api;

import org.bukkit.plugin.java.JavaPlugin;

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
    // TODO: 以后在这里扩展：GameManager getGameManager(); 等等

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