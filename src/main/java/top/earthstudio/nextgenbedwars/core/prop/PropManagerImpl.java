package top.earthstudio.nextgenbedwars.core.prop;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.PropManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.compass.CompassManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.platform.PlatformManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep.TNTSheepManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.trap.TrapManager;
import top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder.WarpPowderManager;

import top.earthstudio.nextgenbedwars.core.prop.builtin.compass.CompassManagerImpl;
import top.earthstudio.nextgenbedwars.core.prop.builtin.compass.CompassProp;
import top.earthstudio.nextgenbedwars.core.prop.builtin.magnetshoe.MagnetShoes;
import top.earthstudio.nextgenbedwars.core.prop.builtin.magnetshoe.listener.MagnetShoesListener;
import top.earthstudio.nextgenbedwars.core.prop.builtin.platform.PlatformManagerImpl;
import top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.TNTSheepManagerImpl;
import top.earthstudio.nextgenbedwars.core.prop.builtin.tntsheep.TNTSheepProp;
import top.earthstudio.nextgenbedwars.core.prop.builtin.trap.TrapManagerImpl;
import top.earthstudio.nextgenbedwars.core.prop.builtin.trap.TrapProp;
import top.earthstudio.nextgenbedwars.core.prop.builtin.wall.WallProp;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.WarpPowderManagerImpl;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.WarpPowderProp;
import top.earthstudio.nextgenbedwars.core.prop.listener.PropInteractListener;
import top.earthstudio.nextgenbedwars.core.prop.builtin.platform.PlatformProp;

import java.util.Map;
import java.util.function.Supplier;

public class PropManagerImpl implements PropManager {
    private Map<String, Supplier<? extends IProp>> propTemplates;
    private PropInteractListener propInteractListener;

    public PropManagerImpl(GameInstance gameInstance) {
        this.propTemplates = new Object2ObjectOpenHashMap<>();

        this.propInteractListener = new PropInteractListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(propInteractListener, BedwarsAPI.getInstance().plugin());

        create("test", TestProp::new); // TODO: DEBUG ONLY
        registerBuiltInPropSubSystem(gameInstance);
    }

    @Override
    public void create(String id, Supplier<? extends IProp> factory) {
        if (propTemplates.putIfAbsent(id, factory) != null) {
            throw new IllegalStateException("Prop id already registered: " + id);
        }
    }

    @Override
    public IProp get(String id) {
        Supplier<? extends IProp> supplier = propTemplates.get(id);
        return supplier != null ? supplier.get() : null;
    }

    @Override
    public void remove(String id) {
        propTemplates.remove(id);
    }

    @Override
    public void shutdown() {
        if (propInteractListener != null) {
            HandlerList.unregisterAll(propInteractListener);
            propInteractListener = null;
        }
        if (propTemplates != null) {
            propTemplates.clear();
            propTemplates = null;
        }
    }

    @Override
    public void onReady() { // built-in prop register
        create("platform", PlatformProp::new);
        create("wall", WallProp::new);
        create("warp_powder", WarpPowderProp::new);
        create("compass", CompassProp::new);
        create("magnet_shoes", MagnetShoes::new);
        create("trap", TrapProp::new);
        create("tnt_sheep", TNTSheepProp::new);
    }

    private void registerBuiltInPropSubSystem(GameInstance gameInstance) {
        gameInstance.inject(PlatformManager.class, PlatformManagerImpl::new);
        gameInstance.inject(WarpPowderManager.class, WarpPowderManagerImpl::new);
        gameInstance.inject(CompassManager.class, CompassManagerImpl::new);
        gameInstance.inject(MagnetShoesListener.class, MagnetShoesListener::new);
        gameInstance.inject(TrapManager.class, TrapManagerImpl::new);
        gameInstance.inject(TNTSheepManager.class, TNTSheepManagerImpl::new);
    }
}