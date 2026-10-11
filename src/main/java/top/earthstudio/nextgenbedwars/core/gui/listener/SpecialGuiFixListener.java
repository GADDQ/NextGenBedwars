package top.earthstudio.nextgenbedwars.core.gui.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;

@SuppressWarnings("UnstableApiUsage")
public class SpecialGuiFixListener implements Listener {
    @EventHandler
    public void onPrepareAnvil(org.bukkit.event.inventory.PrepareAnvilEvent event) {
        IGuiHolder guiHolder = GuiManagerImpl.getGuiHolder(event.getView());

        if (guiHolder == null)
            return;

        String text = event.getView().getRenameText();

        if (text == null || text.isBlank())
            event.setResult(null);
    }
}
