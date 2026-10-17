package net.noralunan.duskveil.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {

    @Shadow
    public abstract boolean isPowered();

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    protected CreeperMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void duskveil$tick(CallbackInfo ci)
    {
        if (!(this.getTags().contains(SpawnExecutor.SPAWN) && DuskveilConfig.SERVER.entity.charged_creeper.get()))
        {
            return;
        }

        if (this.level() instanceof TideLevel tide)
        {
            if (this.isPowered())
            {
                if (!tide.isTideNight())
                {
                    this.entityData.set(DATA_IS_POWERED, false);
                }
            }
            else
            {
                if (tide.isTideNight())
                {
                    this.entityData.set(DATA_IS_POWERED, true);
                }
            }
        }
    }
}
