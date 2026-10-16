package net.noralunan.duskveil.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void duskveil$setTarget(LivingEntity newt, CallbackInfo ci) {
        if (newt == null) return;
        if (DuskveilConfig.SERVER.entity.disable_infighting.get() == false) return;
        Mob self = (Mob) (Object) this;
        if (self.getTags().contains(SpawnExecutor.SPAWN) && newt.getTags().contains(SpawnExecutor.SPAWN)) {
            ci.cancel();
        }
    }
}
