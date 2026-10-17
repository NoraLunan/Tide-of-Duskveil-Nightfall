package net.noralunan.duskveil.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EventConfig;
import net.noralunan.duskveil.core.MessageType;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import net.noralunan.library.common.NoraComponent;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EventProcessor
{
    @SubscribeEvent
    public static void start(TideNightLifecycle.StartedEvent event)
    {
        TideLevel tide = event.tideLevel();
        tide.setLastTide(tide.day());
        tide.syncData();

        Component message = NoraComponent.line(event.getMessage()).darkRed();
        broadcast(message, event.messageType(), tide);

        DuskveilConfig.SERVER.event.start_cmd.get().forEach(tide::command);
    }

    @SubscribeEvent
    public static void stop(TideNightLifecycle.StoppedEvent event)
    {
        TideLevel tide = event.tideLevel();
        EventConfig config = DuskveilConfig.SERVER.event;

        if (tide.getLastTide() >= tide.getNextFixDay())
        {
            tide.setNextFixDay(config.fix_day.get() + tide.day());
            tide.syncData();
        }

        Component message = NoraComponent.of(event.getMessage());
        broadcast(message, event.messageType(), tide);

        if (config.despawn.get())
        {
            clearMob(tide.level());
        }

        config.end_cmd.get().forEach(tide::command);
    }

    private static void broadcast(Component component, MessageType type, TideLevel tide)
    {
        switch (type)
        {
            case TITLE -> tide.sendTitle(component, false);
            case SUBTITLE -> tide.sendTitle(component, true);
            case CHAT -> tide.sendMessage(component, false);
            case ACTIONBAR -> tide.sendMessage(component, true);
        }
    }

    public static void clearMob(ServerLevel level)
    {
        level.getServer().getAllLevels().forEach(lvl -> {
            Set<Entity> collect = StreamSupport
                    .stream(lvl.getAllEntities().spliterator(), false)
                    .filter(Objects::nonNull)
                    .filter(entity -> entity.getTags().contains(SpawnExecutor.SPAWN))
                    .collect(Collectors.toSet());
            collect.forEach(Entity::discard);
        });
    }
}
