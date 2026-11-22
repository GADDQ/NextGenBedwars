package top.earthstudio.nextgenbedwars.api;

import org.bukkit.NamespacedKey;

public final class PdcKeys {
    private PdcKeys() {}

    public static final class spawner {
        private spawner() {}
        public static final NamespacedKey drop = new NamespacedKey("nextgenbedwars", "spawner_drop");
        public static final NamespacedKey preventMerge = new NamespacedKey("nextgenbedwars", "prevent_merge");
    }

    public static final class shop {
        private shop() {}
        public static final NamespacedKey downgradeWhenDead = new NamespacedKey("nextgenbedwars", "shop_downgrade_when_dead");
        public static final NamespacedKey keepWhenDead = new NamespacedKey("nextgenbedwars", "shop_keep_when_dead");
        public static final NamespacedKey throwable = new NamespacedKey("nextgenbedwars", "shop_throwable");
        public static final NamespacedKey itemId = new NamespacedKey("nextgenbedwars", "shop_item_id");
    }

    public static final class prop {
        private prop() {}
        public static final NamespacedKey id = new NamespacedKey("nextgenbedwars", "prop_id");

        // ---------- Built-In Prop Relative ----------
        public static final NamespacedKey warp_powder_active = new NamespacedKey("nextgenbedwars", "prop_warp_powder_active");
    }

    public static final class i18n {
        private i18n() {}
        public static final NamespacedKey name = new NamespacedKey("nextgenbedwars", "translatable_name");
        public static final NamespacedKey lore = new NamespacedKey("nextgenbedwars", "translatable_lore");
    }
}
