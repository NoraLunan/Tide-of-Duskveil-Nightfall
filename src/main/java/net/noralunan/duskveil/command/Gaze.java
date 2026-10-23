package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.entity.gaze.GazeMechanic;
import net.noralunan.library.common.NoraPlayer;

class Gaze {
    static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TideLevel tide = (TideLevel) source.getLevel();
        if (!DuskveilConfig.SERVER.gaze.always_cmd.get() && !tide.isTideNight()) {
            source.sendFailure(Component.translatable("duskveil.command.gaze.fail.no_during"));
            return 0;
        }

        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "target");
            if (NoraPlayer.isCheat(player)) {
                source.sendFailure(Component.translatable("duskveil.command.gaze.fail.gamemode", player.getName()));
                return 0;
            }

            GazeMechanic.execute(source.getLevel(), player);

            source.sendSuccess(() -> Component.translatable("duskveil.command.gaze.success", player.getName()), true);
            return 1;
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.translatable("duskveil.command.fail.player_exception"));
            return 0;
        }
    }
}
