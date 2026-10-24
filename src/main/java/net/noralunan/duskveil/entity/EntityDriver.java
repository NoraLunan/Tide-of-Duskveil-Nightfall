package net.noralunan.duskveil.entity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.entity.goal.ExclusiveGoal;
import net.noralunan.duskveil.entity.goal.NeutralGoal;
import net.noralunan.duskveil.entity.goal.PathfindGoal;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.common.event.NoraTickEvent;
import net.noralunan.library.common.event.PeriodicTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.StreamSupport;

public class EntityDriver
{
    private static final NoraLogger LOGGER = Duskveil.logger();
    private static final Map<UUID, Map<UUID, List<Goal>>> CACHE = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        TideLevel tide = (TideLevel) player.level();
        Map<UUID, List<Goal>> map = new HashMap<>();
        server.getAllLevels().forEach(level -> StreamSupport
                .stream(level.getAllEntities().spliterator(), false)
                .filter(entity -> {
                    if (entity instanceof Mob mob
                            && mob.getTags().contains(SpawnExecutor.SPAWN)
                            && mob.isAlive())
                    {
                        UUID u = parseUUID(mob);
                        return u != null && u.equals(uuid);
                    }
                    return false;
                }).forEach(entity -> {
                    Mob mob = (Mob) entity;
                    addAI(tide, mob, player, map);
                })
        );

        if (!map.isEmpty()) CACHE.put(uuid, map);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Map<UUID, List<Goal>> grouped = CACHE.remove(playerUUID);
        if (grouped == null || grouped.isEmpty()) return;

        grouped.forEach((uuid, goals) -> {
            for (ServerLevel level : server.getAllLevels())
            {
                Entity entity = level.getEntity(uuid);
                if (entity instanceof Mob mob)
                {
                    goals.forEach(mob.goalSelector::removeGoal);
                    break;
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void join(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof Mob mob)
        {
            if (event.getLevel() instanceof ServerLevel level)
            {
                if (mob.getTags().contains(SpawnExecutor.SPAWN))
                {
                    UUID uuid = parseUUID(mob);
                    if (uuid == null) return;
                    Player player = level.getServer().getPlayerList().getPlayer(uuid);
                    if (player != null)
                    {
                        Map<UUID, List<Goal>> mobMap = CACHE.computeIfAbsent(
                                player.getUUID(), string -> new HashMap<>());
                        addAI((TideLevel) level, mob, player, mobMap);
                    }
                }
                else
                {
                    if (mob instanceof NeutralMob)
                    {
                        mob.goalSelector.addGoal(2, new NeutralGoal((TideLevel) level, mob));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event)
    {
        if (event.getEntity() instanceof Mob mob)
        {
            if (!mob.getTags().contains(SpawnExecutor.SPAWN)) return;
            UUID uuid = parseUUID(mob);
            if (uuid == null) return;
            Map<UUID, List<Goal>> inner = CACHE.get(uuid);
            if (inner != null)
            {
                List<Goal> goals = inner.remove(mob.getUUID());
                if (inner.isEmpty())
                {
                    CACHE.remove(uuid);
                }
                if (goals != null)
                {
                    goals.forEach(mob.goalSelector::removeGoal);
                }
            }
        }
    }

    @PeriodicTickEvent(interval = 1200)
    public static void tick(NoraTickEvent.ServerTick event)
    {
        TideLevel tide = (TideLevel) event.getServer().overworld();

        Iterator<Map.Entry<UUID, Map<UUID, List<Goal>>>> outerIt = CACHE.entrySet().iterator();
        while (outerIt.hasNext())
        {
            Map.Entry<UUID, Map<UUID, List<Goal>>> outerEntry = outerIt.next();
            Map<UUID, List<Goal>> inner = outerEntry.getValue();
            Iterator<Map.Entry<UUID, List<Goal>>> innerIt = inner.entrySet().iterator();
            while (innerIt.hasNext())
            {
                UUID uuid = innerIt.next().getKey();
                Entity entity = judge(tide.server(), uuid);
                if (entity == null || !entity.isAlive())
                {
                    innerIt.remove();
                }
            }
            if (inner.isEmpty())
            {
                outerIt.remove();
            }
        }
    }

    private static void addAI(TideLevel tide, Mob mob, Player player, Map<UUID, List<Goal>> cache)
    {
        Goal pathfind = new PathfindGoal(mob, player);
        Goal exclusive = new ExclusiveGoal(tide, mob);

        GoalSelector goal = mob.goalSelector;
        goal.addGoal(3, pathfind);
        goal.addGoal(4, exclusive);
        List<Goal> goals = List.of(pathfind, exclusive);

        cache.put(mob.getUUID(), goals);
    }

    @Nullable
    private static UUID parseUUID(Entity entity)
    {
        try
        {
            return entity.getPersistentData().getUUID(SpawnExecutor.TARGET);
        }
        catch (NullPointerException a)
        {
            // TODO: 兼容旧存档数据，将在下个版本移除
            try
            {
                String old = entity.getPersistentData().getString(SpawnExecutor.TARGET);
                return UUID.fromString(old);
            }
            catch (IllegalArgumentException b)
            {
                LOGGER.warn("Failed to parse entity UUID: {}", entity.getName());
                return null;
            }
        }
    }

    @Nullable
    private static Entity judge(MinecraftServer server, UUID uuid)
    {
        for (ServerLevel level : server.getAllLevels())
        {
            Entity entity = level.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    public static void modifyAttribute(Mob mob)
    {
        new MobAttribute(mob).modify();
    }

    public static void restoreAttribute(Mob mob)
    {
        new MobAttribute(mob).restore();
    }

    public static void addEffect(TideLevel tide, Mob mob)
    {
        StatusEffect.add(tide, mob);
    }
}
