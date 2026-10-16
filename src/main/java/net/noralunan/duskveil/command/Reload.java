package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.config.ModConfig;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.data.MoblistConfig;

import java.io.File;

class Reload {
    static int server(CommandContext<CommandSourceStack> context) {
        return execute(context, Duskveil.getConfig(ModConfig.Type.SERVER));
    }

    static int client(CommandContext<CommandSourceStack> context) {
        return execute(context, Duskveil.getConfig(ModConfig.Type.CLIENT));
    }

    static int moblist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        MoblistConfig.load(null);
        source.sendSuccess(() -> Component.translatable("duskveil.command.reload.success", fileName(DuskveilConfig.MOBLIST_PATH)), true);
        return 1;
    }

    private static int execute(CommandContext<CommandSourceStack> context, ModConfig config) {
        CommandSourceStack source = context.getSource();

        if (DuskveilConfig.reload(config)) {
            source.sendSuccess(() -> Component.translatable(
                            "duskveil.command.reload.success", fileName(config.getFileName())),
                    config.getSpec() != DuskveilConfig.CLIENT_SPEC);
            return 1;
        } else {
            source.sendFailure(Component.translatable("command.failed"));
            return 0;
        }
    }

    private static String fileName(String path) {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }
}
