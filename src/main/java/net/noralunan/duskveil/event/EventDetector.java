package net.noralunan.duskveil.event;

import net.minecraftforge.common.MinecraftForge;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EventConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

public class EventDetector
{
    @PeriodicTickEvent(interval = 100)
    public static void tick(NoraTickEvent.ServerTick event)
    {
        TideLevel tide = (TideLevel) event.getServer().overworld();

        if (TimeChecker.start(tide))
        {
            if (tide.isInspected())
            {
                tide.setInspected(false);
                tide.syncData();
            }
            return;
        }

        if (tide.isInspected()) return;
        tide.setInspected(true);

        boolean trigger = false;
        TriggerType type = TriggerType.NONE;

        EventConfig config = DuskveilConfig.SERVER.event;
        if (config.full_moon.get())
        {
            if (tide.isFullMoon())
            {
                trigger = true;
                type = TriggerType.FULL_MOON;
            }
        }
        else
        {
            int fix = config.fix_day.get();
            if (tide.getNextFixDay() <= tide.getLastTide())
            {
                tide.setNextFixDay(tide.getLastTide() + fix);
            }
            if (tide.getNextFixDay() <= tide.day() && fix > 0)
            {
                trigger = true;
                type = TriggerType.FIX_INTERVAL;
            }
        }

        if (!trigger && NoraMath.nextDouble() < config.trigger_chance.get())
        {
            type = TriggerType.CHANCE;
        }

        setStatus(type, tide);
    }

    private static final NoraLogger LOGGER = Duskveil.logger();

    public static void setStatus(TriggerType type, TideLevel tide)
    {
        var evt = new TideNightLifecycle.StatusChangeEvent(tide, type);
        if (MinecraftForge.EVENT_BUS.post(evt))
            tide.setTrigger(TriggerType.NONE);
        else
        {
            TriggerType finalType = evt.getType();
            if (finalType == TriggerType.NONE)
            {
                tide.setTrigger(finalType);
            }
            else
            {
                if (!tide.isTrigger())
                {
                    tide.setTrigger(finalType);
                    LOGGER.info("Triggered by {}", finalType.name().toLowerCase().replace("_", " "));
                }
            }
        }

        tide.syncData();
    }
}
