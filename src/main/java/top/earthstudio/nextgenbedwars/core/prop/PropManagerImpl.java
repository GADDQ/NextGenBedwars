package top.earthstudio.nextgenbedwars.core.prop;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.PropManager;

import top.earthstudio.nextgenbedwars.core.prop.listener.PropInteractListener;

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
}