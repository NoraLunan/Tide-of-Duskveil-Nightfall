package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.duskveil.event.EventDetector;
import net.noralunan.duskveil.event.TimeChecker;

class Start {
    static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        TideLevel tide = (TideLevel) level;
        if (tide.isTideNight()) {
            source.sendFailure(Component.translatable("duskveil.command.start.fail"));
            return 0;
        }
        if (!TimeChecker.start(tide)) {
            tide.dayTime(DuskveilConfig.SERVER.event.start_time.get());
        }
        EventDetector.setStatus(TriggerType.COMMAND, tide);
        source.sendSuccess(() -> Component.translatable("duskveil.command.start.success"), true);
        return 1;
    }
}
