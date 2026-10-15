package top.earthstudio.nextgenbedwars.core.command.edit;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class EditCommand {
    public EditCommand() {
        Commands.literal("bw").then(
                Commands.literal("spawner").then(
                        Commands.literal("add").executes(this::addSpawner)
                ).then(
                        Commands.literal("remove").executes(this::removeSpawner)
                )
        ).then(
                Commands.literal("team").then(
                        Commands.literal("add")
                ).then(
                        Commands.literal("remove")
                )
        ).then(
                Commands.literal("shop").then(
                        Commands.literal("add")
                ).then(
                        Commands.literal("remove").executes(this::removeShop)
                )
        ).then(
                Commands.literal("save").executes(this::save)
        );
    }

    private int addSpawner(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }

    private int removeSpawner(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }

    private int removeShop(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }

    private int save(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }
}
