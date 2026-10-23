package net.noralunan.duskveil.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraComponent;

class Count {
    static final String VALUE = "value";

    static int query(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (source.getEntity() == null) return 0;
        TideLevel tide = (TideLevel) source.getLevel();
        source.sendSystemMessage(NoraComponent.of(String.valueOf(tide.getEventCount())));
        return 1;
    }

    static int set(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        return execute(source, value(context), (TideLevel) source.getLevel());
    }

    static int add(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int value = value(context);
        TideLevel tide = (TideLevel) source.getLevel();
        return execute(source, tide.getEventCount() + value, tide);
    }

    static int decrease(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int value = value(context);
        TideLevel tide = (TideLevel) source.getLevel();
        int result = tide.getEventCount() - value;
        if (result < 0) {
            source.sendFailure(Component.translatable("duskveil.command.count.fail"));
            return 0;
        }
        return execute(source, result, tide);
    }

    private static int execute(CommandSourceStack source, int value, TideLevel tide) {
        tide.setEventCount(value);
        tide.syncData();
        source.sendSuccess(() -> Component.translatable("duskveil.command.count.success", value), true);
        return 1;
    }

    private static int value(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, VALUE);
    }
}
