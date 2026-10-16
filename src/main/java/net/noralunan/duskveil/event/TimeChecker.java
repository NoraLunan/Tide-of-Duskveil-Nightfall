package net.noralunan.duskveil.event;

import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.library.common.NoraLevel;

public class TimeChecker
{
    public static boolean during(NoraLevel level)
    {
        return start(level) && end(level);
    }

    public static boolean start(NoraLevel level)
    {
        return level.dayTime() > DuskveilConfig.SERVER.event.start_time.get();
    }

    public static boolean end(NoraLevel level)
    {
        return level.dayTime() < DuskveilConfig.SERVER.event.end_time.get();
    }
}
