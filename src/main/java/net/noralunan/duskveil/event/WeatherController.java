package net.noralunan.duskveil.event;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraHelper;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

public class WeatherController
{
    private static void modify(TideLevel tide)
    {
        if (!tide.booleanRule(GameRules.RULE_WEATHER_CYCLE)) return;
        int time = tide.getTideNightRemain();
        switch (NoraHelper.getEnum(WeatherType.class, DuskveilConfig.SERVER.event.weather))
        {
            case SUNNY -> tide.setSunny(time);
            case RAIN -> tide.setRain(time);
            case THUNDER -> tide.setThunder(time);
        }
    }

    @SubscribeEvent
    public static void start(TideNightLifecycle.StartedEvent event)
    {
        modify(event.tideLevel());
    }

    @PeriodicTickEvent(interval = 200)
    public static void tick(NoraTickEvent.LevelTick event)
    {
        if (event.getWorld() instanceof TideLevel tide)
        {
            if (tide.isTideNight())
            {
                int remain = tide.getTideNightRemain();
                int rain = tide.levelData().getRainTime();
                if (Math.abs(remain - rain) > 10)
                {
                    modify(tide);
                }
            }
        }
    }
}
