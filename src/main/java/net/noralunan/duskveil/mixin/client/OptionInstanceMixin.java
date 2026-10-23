package net.noralunan.duskveil.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.noralunan.duskveil.config.DuskveilConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin
{
    @SuppressWarnings("all")
    @ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true)
    private Object duskveil$set(Object value)
    {
        try
        {
            if (DuskveilConfig.CLIENT.other.lock_gamma.get() == false) return value;

            var instance = (OptionInstance<?>) (Object) this;
            var options = Minecraft.getInstance().options;

            if (instance != null && options != null && instance == options.gamma())
            {
                return 0d;
            }
        }
        catch (Exception ignored) {}
        return value;
    }
}
