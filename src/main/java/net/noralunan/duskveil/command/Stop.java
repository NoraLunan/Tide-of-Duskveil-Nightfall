package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.duskveil.event.WeatherType;
import net.noralunan.duskveil.event.EventDetector;
import net.noralunan.library.common.NoraHelper;

class Stop {
    static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TideLevel tide = (TideLevel) source.getLevel();
        if (!tide.isTideNight()) {
            source.sendFailure(Component.translatable("duskveil.command.stop.fail"));
            return 0;
        }
        EventDetector.setStatus(TriggerType.NONE, tide);
        source.sendSuccess(() -> Component.translatable("duskveil.command.stop.success"), true);

        WeatherType type = NoraHelper.getEnum(WeatherType.class, DuskveilConfig.SERVER.event.weather);
        if (type == WeatherType.RAIN || type == WeatherType.THUNDER) {
            tide.setSunny(ServerLevel.RAIN_DELAY.sample(tide.level().getRandom()));
        }
        return 1;
    }
}
