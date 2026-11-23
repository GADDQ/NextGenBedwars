package top.earthstudio.nextgenbedwars.core;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.command.edit.EditCommand;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.api.gui.GuiManager;
import top.earthstudio.nextgenbedwars.api.language.LanguageTranslator;

import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationManager;
import top.earthstudio.nextgenbedwars.core.command.edit.EditCommandImpl;
import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;
import top.earthstudio.nextgenbedwars.core.language.LanguageTranslatorImpl;
import top.earthstudio.nextgenbedwars.core.particle.ParticleAnimationManagerImpl;

public record BedwarsAPIImpl(JavaPlugin plugin) implements BedwarsAPI {
    @Override
    public <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor) {
        GameManager.registerSubSystem(type, constructor);
    }

    @Override
    public GuiManager getGuiManager() {
        return GuiManagerImpl.get();
    }

    @Override
    public EditCommand getEditCommand() {
        return EditCommandImpl.get();
    }

    @Override
    public LanguageTranslator getLanguageTranslator() {
        return LanguageTranslatorImpl.get();
    }

    @Override
    public ParticleAnimationManager getParticleAnimationManager() { return ParticleAnimationManagerImpl.get(); }
}