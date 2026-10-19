package net.noralunan.duskveil.mixin.compat.tweakeroo;

import fi.dy.masa.tweakeroo.config.Callbacks;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.OtherClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(Callbacks.FeatureCallbackGamma.class)
public abstract class GammaOverrideMixin
{
    @ModifyVariable(method = "applyValue", at = @At("HEAD"), argsOnly = true, remap = false)
    private double duskveil$applyValue(double value)
    {
        OtherClientConfig config = DuskveilConfig.CLIENT.other;
        if (config.lock_gamma.get() && config.disable_gammaoverride.get())
        {
            return 0d;
        }
        return value;
    }
}
