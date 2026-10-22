package top.earthstudio.nextgenbedwars.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.core.command.admin.AdminCommand;
import top.earthstudio.nextgenbedwars.core.command.edit.EditCommand;

public class BedwarsCommand {

    public BedwarsCommand(JavaPlugin plugin) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bw");

        AdminCommand.build(root);
        EditCommand.build(root);

        plugin.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(root.build())
        );
    }
}