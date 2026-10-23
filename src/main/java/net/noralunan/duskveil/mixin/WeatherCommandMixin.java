package net.noralunan.duskveil.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.WeatherCommand;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.event.WeatherType;
import net.noralunan.library.common.NoraHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public abstract class WeatherCommandMixin {
    @Inject(method = "setClear", at = @At("HEAD"), cancellable = true)
    private static void duskveil$setClear(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        duskveil$prevent(source, cir);
    }

    @Inject(method = "setRain", at = @At("HEAD"), cancellable = true)
    private static void duskveil$setRain(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        duskveil$prevent(source, cir);
    }

    @Inject(method = "setThunder", at = @At("HEAD"), cancellable = true)
    private static void duskveil$setThunder(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        duskveil$prevent(source, cir);
    }

    @Unique
    private static void duskveil$prevent(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        if (NoraHelper.getEnum(WeatherType.class, DuskveilConfig.SERVER.event.weather) == WeatherType.NONE) return;
        if (source.getLevel() instanceof TideLevel tide && tide.isTideNight()) {
            source.sendFailure(Component.translatable("duskveil.command.weather.fail"));
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
