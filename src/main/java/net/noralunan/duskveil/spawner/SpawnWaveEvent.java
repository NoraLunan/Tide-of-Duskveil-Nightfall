package net.noralunan.duskveil.spawner;

import net.minecraftforge.eventbus.api.Event;
import net.noralunan.duskveil.core.TideLevel;

public class SpawnWaveEvent extends Event
{
    private final TideLevel tide;
    private final int interval;
    private final int wave;

    SpawnWaveEvent(TideLevel tide, int interval)
    {
        this.tide = tide;
        this.interval = interval;
        this.wave = tide.getWaveCount();
    }

    public TideLevel overTide()
    {
        return this.tide;
    }

    public int getNextWaveInterval()
    {
        return this.interval;
    }

    public int getWaveCount()
    {
        return this.wave;
    }
}
