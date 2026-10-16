package net.noralunan.duskveil.spawner;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.event.TideNightLifecycle;
import net.noralunan.library.common.NoraLiving;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

public class MobCountStats
{
    @Nullable
    private static Set<NoraLiving> spawned = null;

    static void clearInvalid()
    {
        if (spawned == null) return;
        spawned.removeIf(mob -> !mob.toLiving().isAlive() || mob.toLiving().deathTime > 0);
    }

    static void add(NoraLiving mob)
    {
        if (spawned == null) return;
        if (!mob.containsTag(SpawnExecutor.SPAWN)) return;
        spawned.add(mob);
    }

    static void add(Mob mob)
    {
        add((NoraLiving) mob);
    }

    static int get()
    {
        if (spawned == null) return -1;
        return spawned.size();
    }

    @SubscribeEvent
    public static void clearAll(TideNightLifecycle.StoppedEvent event)
    {
        if (spawned == null) return;
        spawned.clear();
    }

    @SubscribeEvent
    public static void start(ServerStartingEvent event)
    {
        spawned = new HashSet<>();
        event.getServer().getAllLevels().forEach(level -> StreamSupport
                .stream(level.getAllEntities().spliterator(), false)
                .filter(entity -> entity instanceof NoraLiving mob && mob.containsTag(SpawnExecutor.SPAWN))
                .forEach(entity -> spawned.add((NoraLiving) entity))
        );
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event)
    {
        clearAll(null);
        spawned = null;
    }
}
