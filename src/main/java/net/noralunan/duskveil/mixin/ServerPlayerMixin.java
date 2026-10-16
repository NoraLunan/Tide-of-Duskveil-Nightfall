package net.noralunan.duskveil.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    /**
     * 阻止玩家完成进度
     */
    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void duskveil$startSleepInBed(CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Player player = (Player) (Object) this;
        if (!(player.level() instanceof TideLevel tide)) return;
        if (!tide.isTideNight()) return;

        if (DuskveilConfig.SERVER.event.no_sleep.get()) {
            cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
            cir.cancel();
            player.displayClientMessage(NoraComponent.line(DuskveilConfig.SERVER.event.sleep_message.get()).red(), true);
        }
    }
}
