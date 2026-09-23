package top.earthstudio.nextgenbedwars.api;

import org.bukkit.NamespacedKey;

public final class PdcKeys {
    private PdcKeys() {}

    public static final class spawner {
        private spawner() {}
        public static final NamespacedKey drop = new NamespacedKey("nextgenbedwars", "spawner_drop");
        public static final NamespacedKey preventMerge = new NamespacedKey("nextgenbedwars", "prevent_merge");
    }
}
