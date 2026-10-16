package net.noralunan.duskveil.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.noralunan.duskveil.config.DuskveilConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin {
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void duskveil$set(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        Options options = minecraft.options;
        if (options == null) return;
        if ((OptionInstance<?>) (Object) this != options.gamma()) return;
        if (options.gamma().get() != 0d) return;
        if (DuskveilConfig.CLIENT.other.lock_gamma.get()) {
            ci.cancel();
        }
    }
}
