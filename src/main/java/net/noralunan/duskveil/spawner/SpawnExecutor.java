package net.noralunan.duskveil.spawner;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EntityConfig;
import net.noralunan.duskveil.config.SpawnerConfig;
import net.noralunan.duskveil.config.data.MoblistConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraPlayer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SpawnExecutor
{
    private static final NoraLogger LOGGER = Duskveil.logger();

    @SubscribeEvent
    public static void transfer(SpawnWaveEvent event)
    {
        TideLevel tide = event.overTide();
        if (tide.isPeaceful()) return;

        MobCountStats.clearInvalid();

        int mob_cap = DuskveilConfig.SERVER.spawner.mob_cap.get();
        if (MobCountStats.get() > mob_cap && mob_cap > 0) return;

        tide.server().getPlayerList().getPlayers().forEach(SpawnExecutor::run);
    }

    private static void run(ServerPlayer player)
    {
        run(player, MobSpawnType.SPAWNER);
    }

    public static final String SPAWN = Duskveil.MOD_ID + ".spawn";
    public static final String TARGET = Duskveil.MOD_ID + ".target";

    public static int run(ServerPlayer player, MobSpawnType type)
    {
        if (player.level() instanceof TideLevel tide)
        {
            SpawnerConfig config = DuskveilConfig.SERVER.spawner;

            boolean contains_alg = config.alg_whitelist.get().contains(tide.dimensionID());
            boolean inverted_alg = config.alg_blacklist.get();
            boolean surface = inverted_alg == contains_alg;

            return run(player, surface, type);
        }
        return -1;
    }

    public static int run(ServerPlayer player, boolean surface, MobSpawnType type)
    {
        SpawnerConfig config = DuskveilConfig.SERVER.spawner;
        int min_distance = config.min_distance.get();
        int max_distance = config.max_distance.get();

        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();

        Vec2 pending = NoraMath.nextPlane(x, z, min_distance, max_distance);

        boolean evenly_distribute = config.evenly_distribute.get();
        int rx = evenly_distribute ? x : NoraMath.round(pending.x);
        int rz = evenly_distribute ? z : NoraMath.round(pending.y);
        int min = evenly_distribute ? min_distance : 0;
        int max = evenly_distribute ? max_distance : max_distance >> 4;

        return spawn((TideLevel) player.level(), player, rx, y, rz, min, max, surface, type);
    }

    public static int run(ServerLevel level, ColumnPos pos, MobSpawnType type)
    {
        return run(level, new Vec3i(pos.x(), 0, pos.z()), true, type);
    }

    public static int run(ServerLevel level, Vec3i pos, boolean surface, MobSpawnType type)
    {
        return run((TideLevel) level, pos, surface, type);
    }

    public static int run(TideLevel tide, Vec3i pos, boolean surface, MobSpawnType type)
    {
        SpawnerConfig config = DuskveilConfig.SERVER.spawner;
        int max_distance = config.max_distance.get();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return spawn(tide, null, x, y, z, 0, max_distance>>4, surface, type);
    }

    private static int spawn(
            TideLevel tide,
            @Nullable ServerPlayer player,
            int x,
            int y,
            int z,
            int min,
            int max,
            boolean surface,
            MobSpawnType spawn_type)
    {
        SpawnerConfig config = DuskveilConfig.SERVER.spawner;
        boolean contains_dim = config.dim_blacklist.get().contains(tide.dimensionID());
        boolean inverted_dim = config.dim_whitelist.get();
        if (contains_dim ^ inverted_dim) return 0;

        int result = 0;
        List<? extends String> key_list = DuskveilConfig.COMMON.json.keyList();

        for (String key : key_list)
        {
            var moblist = MoblistConfig.get(key);

            if (moblist.start_his > tide.getEventCount()) continue;
            if (moblist.end_his < tide.getEventCount() && moblist.end_his > 0) continue;

            ResourceLocation resource = ResourceLocation.tryParse(moblist.mob_id);
            if (resource == null)
            {
                LOGGER.error("Invalid Mob ID format: {}", moblist.mob_id);
                continue;
            }

            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(resource);
            if (type == null) continue;

            ServerLevel level = tide.level();
            int index = findOtherPlayer(level, player, config) == null ? 0 : 1;
            int base = NoraMath.nextInt(moblist.min_count, moblist.max_count);
            int limit = Mth.ceil(Math.pow(config.count_limit.get(), index) * base);
            int count = Mth.ceil(Math.pow(config.wave_size.get(), tide.getWaveCount()) * limit);

            LOGGER.debug("Base Count: {}; For {} Entry", base, key);
            LOGGER.debug("Restricted Count: {}; For {} Entry", limit, key);
            LOGGER.debug("Final Count: {}; For {} Entry", count, key);

            for (int i = count; i > 0; i--)
            {
                int mob_cap = config.mob_cap.get();
                if (MobCountStats.get() >= mob_cap && mob_cap > 0) return result;

                Vec3 pos;
                int retries = config.max_retries.get();
                boolean force = config.force_spawn.get();

                if (surface)
                    pos = PositionFinder.WorldSurface.find(tide, x, z, min, max, retries, force);
                else
                    pos = PositionFinder.DynamicAlgorithm.find(tide, type, x, y, z, min, max, retries, force);

                if (pos == null) continue;

                Entity entity = type.create(level);
                if (entity == null) continue;
                if (entity instanceof Mob mob)
                {
                    mob.setPos(pos);
                    ForgeEventFactory.onFinalizeSpawn(
                            mob,
                            level,
                            level.getCurrentDifficultyAt(mob.getOnPos()),
                            spawn_type,
                            null,
                            null
                    );
                    if (config.no_baby.get() && mob.isBaby())
                    {
                        entity.discard();
                        continue;
                    }

                    EntityConfig cfg = DuskveilConfig.SERVER.entity;
                    if (cfg.shield_value.get() > 0)
                        mob.setAbsorptionAmount(cfg.shield_value.get());

                    if (cfg.enchant.get())
                    {
                        RandomSource random = level.random;
                        int enchantment_level = cfg.enchantment_level.get();
                        boolean allow_treasure = cfg.treasure_enchantments.get();
                        Arrays.stream(EquipmentSlot.values())
                                .filter(slot -> mob.getItemBySlot(slot).isEnchantable())
                                .forEach(slot -> EnchantmentHelper.enchantItem(
                                        random,
                                        mob.getItemBySlot(slot),
                                        enchantment_level,
                                        allow_treasure
                                ));
                    }

                    mob.addTag(SPAWN);
                    if (player != null)
                        mob.getPersistentData().putUUID(TARGET, player.getUUID());
                    if (DuskveilConfig.SERVER.event.retain.get())
                        mob.setPersistenceRequired();

                    level.addFreshEntity(entity);

                    result++;
                    MobCountStats.add(mob);
                }
            }
        }
        return result;
    }

    @Nullable
    private static ServerPlayer findOtherPlayer(ServerLevel level, @Nullable ServerPlayer self, SpawnerConfig config)
    {
        if (self == null) return null;
        int distance = Mth.clamp(config.max_distance.get(), 0, 64);
        double x = self.getX();
        double y = self.getY();
        double z = self.getZ();

        Predicate<Entity> predicate = entity -> {
            if (!(entity instanceof ServerPlayer player)) return false;
            if (player == self) return false;
            return NoraPlayer.noCheat(player);
        };

        return (ServerPlayer) level.getNearestPlayer(x, y, z, distance, predicate);
    }
}
