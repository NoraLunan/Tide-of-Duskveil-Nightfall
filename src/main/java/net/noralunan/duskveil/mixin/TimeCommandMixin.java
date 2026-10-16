package net.noralunan.duskveil.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TimeCommand;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {
    @Inject(method = "setTime", at = @At("HEAD"), cancellable = true)
    private static void duskveil$setTime(CommandSourceStack source, int amount, CallbackInfoReturnable<Integer> cir) {
        duskveil$prevent(source, cir);
    }

    @Inject(method = "addTime", at = @At("HEAD"), cancellable = true)
    private static void duskveil$addTime(CommandSourceStack source, int amount, CallbackInfoReturnable<Integer> cir) {
        duskveil$prevent(source, cir);
    }

    @Unique
    private static void duskveil$prevent(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        if (!DuskveilConfig.SERVER.event.bad_time_cmd.get()) return;
        if (source.getLevel() instanceof TideLevel tide && tide.isTideNight()) {
            source.sendFailure(Component.translatable("duskveil.command.time.fail"));
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
