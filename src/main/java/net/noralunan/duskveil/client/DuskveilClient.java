package net.noralunan.duskveil.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.DuskveilData;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.duskveil.event.TideNightLifecycle;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DuskveilClient
{
    @Nullable
    private static Float[] rgb;
    private static final Float[] DEFAULT;

    private static float direct;
    private static float inverse;

    private static TriggerType type;
    private static boolean natural;

    static
    {
        rgb = null;
        DEFAULT = new Float[]{0.6f, 0.15f, 0.15f};
        direct = 1f;
        inverse = 0f;
    }

    @SubscribeEvent
    public static void color(ViewportEvent.ComputeFogColor event)
    {
        if (DuskveilConfig.CLIENT.other.modify_sky.get() == false) return;
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null || type == null) return;

        if (natural)
        {
            float proto = level.getSkyDarken((float) event.getPartialTick()); // [0.2, 1.0]
            direct = Math.min(1f, proto * 2f - 0.4f);
            inverse = Math.max(0f, proto * -2f + 1.4f);
        }

        if (rgb == null) parse(null);
        event.setRed(event.getRed() * direct + rgb[0] * inverse);
        event.setGreen(event.getGreen() * direct + rgb[1] * inverse);
        event.setBlue(event.getBlue() * direct + rgb[2] * inverse);
    }

    @PeriodicTickEvent
    public static void change(NoraTickEvent.ClientTick event)
    {
        if (natural) return;
        float delta = type == TriggerType.NONE ? 0.005f : -0.005f;
        direct = Mth.clamp(direct + delta, 0f, 1f);
        inverse = Mth.clamp(inverse - delta, 0f, 1f);
    }

    @SubscribeEvent
    public static void parse(DuskveilConfig.ClientConfigReloadEvent event)
    {
        String code = DuskveilConfig.CLIENT.other.fog_color.get();
        if (code == null || code.isBlank())
        {
            rgb = DEFAULT;
            return;
        }
        String value = code.trim();
        if (value.startsWith("#"))
        {
            value = value.substring(1);
        }
        if (value.length() != 6 || !value.matches("[0-9a-fA-F]{6}"))
        {
            rgb = DEFAULT;
            return;
        }

        try
        {
            int r = Integer.parseInt(value.substring(0, 2), 16);
            int g = Integer.parseInt(value.substring(2, 4), 16);
            int b = Integer.parseInt(value.substring(4, 6), 16);

            rgb = new Float[]{r / 255f, g / 255f, b / 255f};
        }
        catch (Exception exception)
        {
            rgb = DEFAULT;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void check(TideNightLifecycle.StatusChangeEvent event)
    {
        type = event.getType();
        setNatural();
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        type = DuskveilData.getTrigger(event.getEntity().level());
        setNatural();
    }

    private static void setNatural()
    {
        natural = !(type == TriggerType.NONE || type == TriggerType.COMMAND);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        type = null;
        natural = false;
    }

    @OnlyIn(Dist.CLIENT)
    public static class GammaOverride
    {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event)
        {
            if (DuskveilConfig.CLIENT.other.lock_gamma.get())
            {
                Minecraft.getInstance().options.gamma().set(0d);
            }
        }
    }
}
