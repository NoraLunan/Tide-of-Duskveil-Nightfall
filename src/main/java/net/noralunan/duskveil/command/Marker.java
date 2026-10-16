package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.duskveil.event.EventDetector;

class Marker {
    static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TideLevel tide = (TideLevel) source.getLevel();
        EventDetector.setStatus(TriggerType.COMMAND, tide);
        source.sendSuccess(() -> Component.translatable("duskveil.command.marker.success"), true);
        return 1;
    }
}
