package top.earthstudio.nextgenbedwars.api.command.edit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface EditCommand {
    @SuppressWarnings("unused")
    void addSubCommand(LiteralArgumentBuilder<CommandSourceStack> subCommandBuilder);
}