package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.noralunan.duskveil.event.EventProcessor;

class Clear {
    static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        EventProcessor.clearMob(source.getLevel());
        source.sendSuccess(() -> Component.translatable("duskveil.command.clear.success"), true);
        return 1;
    }
}
