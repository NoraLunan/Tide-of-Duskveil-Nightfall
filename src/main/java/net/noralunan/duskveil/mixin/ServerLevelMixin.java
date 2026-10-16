package net.noralunan.duskveil.mixin;

import net.minecraft.server.level.ServerLevel;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.core.TideLevel;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
@Implements(@Interface(iface = TideLevel.class, prefix = Duskveil.MOD_ID + "$"))
public abstract class ServerLevelMixin {}
