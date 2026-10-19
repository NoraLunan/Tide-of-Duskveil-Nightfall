package net.noralunan.duskveil.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.DuskveilData;
import net.noralunan.duskveil.event.TideNightLifecycle;
import net.noralunan.library.common.NoraWorld;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

@OnlyIn(Dist.CLIENT)
public class DuskveilClient
{
    private static float[] rgb;
    private static final float[] DEFAULT;

    private static float direct;
    private static float inverse;

    private static float delta;

    private static Minecraft client;
    private static ClientLevel level;
    private static NoraWorld world;

    private static final float MIN_DELTA;
    private static final float MAX_DELTA;

    static
    {
        rgb = null;
        DEFAULT = new float[]{0.6f, 0.15f, 0.15f};
        resetProportion();

        delta = 1f;

        MIN_DELTA = 0.00125f;
        MAX_DELTA = 0.005f;
    }

    @SubscribeEvent
    public static void color(ViewportEvent.ComputeFogColor event)
    {
        if (client == null) client = Minecraft.getInstance();
        if (level == null) level = client.level;
        if (world == null) world = (NoraWorld) level;

        if (DuskveilConfig.CLIENT.other.modify_sky.get() == false) return;

        if (rgb == null) parse(null);
        event.setRed(event.getRed() * direct + rgb[0] * inverse);
        event.setGreen(event.getGreen() * direct + rgb[1] * inverse);
        event.setBlue(event.getBlue() * direct + rgb[2] * inverse);
    }

    @PeriodicTickEvent
    public static void change(NoraTickEvent.ClientTick event)
    {
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

            rgb = new float[]{r / 255f, g / 255f, b / 255f};
        }
        catch (NumberFormatException exception)
        {
            rgb = DEFAULT;
        }
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        delta = MAX_DELTA;
        resetProportion();
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        delta = 1f;
        resetProportion();
    }

    @SubscribeEvent
    public static void start(TideNightLifecycle.StartedEvent event)
    {
        delta = Mth.clamp(-1f / (13000 - world.dayTime()), -MAX_DELTA, -MIN_DELTA);
    }

    @SubscribeEvent
    public static void stop(TideNightLifecycle.StoppedEvent event)
    {
        delta = Mth.clamp(1f / (24000 - world.dayTime()), MIN_DELTA, MAX_DELTA);
    }

    private static void resetProportion()
    {
        direct = 1f;
        inverse = 0f;
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
