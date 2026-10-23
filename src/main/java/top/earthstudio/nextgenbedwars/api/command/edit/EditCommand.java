package top.earthstudio.nextgenbedwars.api.command.edit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface EditCommand {
    static EditCommand getInstance() { // TODO: this is temp, edit command should as a part of api to get
        return EditCommandHolder.INSTANCE;
    }

    @SuppressWarnings("unused")
    void addSubCommand(LiteralArgumentBuilder<CommandSourceStack> subCommandBuilder);

    static void set(EditCommand editCommand) {
        EditCommandHolder.INSTANCE = editCommand;
    }

    class EditCommandHolder {
        static EditCommand INSTANCE;
    }
}
