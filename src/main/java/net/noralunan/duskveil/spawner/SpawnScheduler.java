package net.noralunan.duskveil.spawner;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.SpawnerConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.event.TideNightLifecycle;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

public class SpawnScheduler
{
    @PeriodicTickEvent
    public static void tick(NoraTickEvent.ServerTick event)
    {
        MinecraftServer server = event.getServer();
        if (server.getPlayerCount() == 0) return;

        TideLevel tide = (TideLevel) server.overworld();

        if (tide.isTideNight())
        {
            SpawnerConfig config = DuskveilConfig.SERVER.spawner;

            if (tide.dayTime() > tide.getNextWave() && tide.getNextWave() >= 0)
            {
                int interval = tide.dayTime() + randomInterval(config);
                tide.setNextWave(interval);
                tide.addWaveCount(1);
                tide.setDirty();

                MinecraftForge.EVENT_BUS.post(new SpawnWaveEvent(tide, interval));
            }
        }
    }

    @SubscribeEvent
    public static void start(TideNightLifecycle.StartedEvent event)
    {
        SpawnerConfig config = DuskveilConfig.SERVER.spawner;

        int cfg = config.first_delay.get();
        int regular = randomInterval(config);
        int delay = cfg < 0 ? regular : cfg;

        TideLevel tide = event.tideLevel();
        tide.setNextWave(tide.dayTime() + delay);
        tide.setDirty();
    }

    @SubscribeEvent
    public static void stop(TideNightLifecycle.StoppingEvent event)
    {
        TideLevel tide = event.tideLevel();
        tide.setNextWave(-1);
        tide.setWaveCount(0);
        tide.setDirty();
    }

    private static int randomInterval(SpawnerConfig config)
    {
        return NoraMath.nextInt(config.min_interval.get(), config.max_interval.get());
    }
}
