package top.earthstudio.nextgenbedwars.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.core.command.admin.AdminCommand;
import top.earthstudio.nextgenbedwars.core.command.common.CommonCommand;
import top.earthstudio.nextgenbedwars.core.command.edit.EditCommandImpl;

public class BedwarsCommand {

    public BedwarsCommand(JavaPlugin plugin) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bw");

        CommonCommand.build(root);
        AdminCommand.build(root);
        EditCommandImpl.build(root);

        var node = root.build();

        EditCommandImpl.root = node;

        plugin.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(node)
        );
    }
}