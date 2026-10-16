package net.noralunan.duskveil.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @Final
    @Shadow
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void duskveil$readAdditionalSaveData(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (creeper.level().isClientSide) return;

        if (creeper.getTags().contains(SpawnExecutor.SPAWN) && DuskveilConfig.SERVER.entity.charged_creeper.get()) {
            if (!creeper.getEntityData().get(DATA_IS_POWERED)) {
                creeper.getEntityData().set(DATA_IS_POWERED, true);
            }
        }
    }
}
