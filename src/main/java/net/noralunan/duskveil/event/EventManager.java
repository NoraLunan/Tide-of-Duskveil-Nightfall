package net.noralunan.duskveil.event;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EventConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;
import net.noralunan.library.common.NoraHelper;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

import java.util.List;

public class EventManager
{
    @PeriodicTickEvent
    public static void tick(NoraTickEvent.ServerTick event)
    {
        MinecraftServer server = event.getServer();
        if (server.getPlayerCount() == 0) return;

        TideLevel tide = (TideLevel) server.overworld();
        if (TimeChecker.during(tide) && tide.isTrigger())
        {
            start(tide);
        }
        else
        {
            stop(tide);
        }
    }

    private static void start(TideLevel tide)
    {
        if (tide.isTideNight()) return;

        boolean cancel = MinecraftForge.EVENT_BUS.post(new TideNightLifecycle.StartingEvent(tide));
        if (cancel) EventDetector.setStatus(TriggerType.NONE, tide);
        else
        {
            tide.setTideNight(true);
            tide.addEventCount();
            EventConfig config = DuskveilConfig.SERVER.event;
            String message = getMessage(config.start_message.get());
            MinecraftForge.EVENT_BUS.post(
                    new TideNightLifecycle.StartedEvent(
                            tide,
                            message,
                            NoraHelper.getEnum(MessageType.class, config.start_type.get())
                    )
            );
        }
        tide.syncData();
    }

    private static void stop(TideLevel tide)
    {
        if (!tide.isTideNight()) return;

        MinecraftForge.EVENT_BUS.post(new TideNightLifecycle.StoppingEvent(tide));

        tide.setTideNight(false);
        tide.syncData();

        EventConfig config = DuskveilConfig.SERVER.event;

        MinecraftForge.EVENT_BUS.post(
                new TideNightLifecycle.StoppedEvent(
                        tide,
                        getMessage(config.end_message.get()),
                        NoraHelper.getEnum(MessageType.class, config.end_type)
                )
        );
    }

    private static String getMessage(List<? extends String> list)
    {
        int random = NoraMath.nextInt(0, list.size() - 1);
        return list.get(random);
    }
}
