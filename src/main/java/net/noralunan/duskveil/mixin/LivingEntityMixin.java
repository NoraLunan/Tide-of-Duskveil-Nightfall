package net.noralunan.duskveil.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /**
     * 阻止一切实体睡觉
     */
    @Inject(method = "startSleeping", at = @At("HEAD"), cancellable = true)
    protected void duskveil$startSleeping(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity.level() instanceof TideLevel tide)) return;
        if (!tide.isTideNight()) return;
        if (DuskveilConfig.SERVER.event.no_sleep.get()) {
            ci.cancel();
        }
    }
}
