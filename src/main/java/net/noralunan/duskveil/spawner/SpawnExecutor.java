package net.noralunan.duskveil.spawner;

import net.minecraft.resources.ResourceLocation;
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

        tide.server().getPlayerList().getPlayers().forEach(SpawnExecutor::spawn);
    }

    public static final String SPAWN = Duskveil.MOD_ID + ".spawn";
    public static final String TARGET = Duskveil.MOD_ID + ".target";

    private static void spawn(ServerPlayer player)
    {
        if (player.level() instanceof TideLevel tide)
        {
            String dim = tide.dimensionID();
            SpawnerConfig config = DuskveilConfig.SERVER.spawner;

            boolean contains_dim = config.dim_blacklist.get().contains(dim);
            boolean inverted_dim = config.dim_whitelist.get();
            if (contains_dim ^ inverted_dim) return;

            int min_distance = config.min_distance.get();
            int max_distance = config.max_distance.get();

            int x = player.getBlockX();
            int z = player.getBlockZ();

            Vec2 pending = NoraMath.nextPlane(x, z, min_distance, max_distance);

            boolean evenly_distribute = config.evenly_distribute.get();
            int rx = evenly_distribute ? x : NoraMath.round(pending.x);
            int rz = evenly_distribute ? z : NoraMath.round(pending.y);
            int min = evenly_distribute ? min_distance : 0;
            int max = evenly_distribute ? max_distance : max_distance >> 4;

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
                    if (MobCountStats.get() >= mob_cap && mob_cap > 0) return;

                    Vec3 pos;
                    int retries = config.max_retries.get();
                    boolean force = config.force_spawn.get();
                    boolean contains_alg = config.alg_whitelist.get().contains(dim);
                    boolean inverted_alg = config.alg_blacklist.get();
                    boolean surface = inverted_alg == contains_alg;
                    if (surface)
                        pos = PositionFinder.WorldSurface.find(tide, rx, rz, min, max, retries, force);
                    else
                    {
                        int ry = player.getBlockY();
                        pos = PositionFinder.DynamicAlgorithm.find(tide, type, rx, ry, rz, min, max, retries, force);
                    }
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
                                MobSpawnType.SPAWNER,
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
                            Arrays.stream(EquipmentSlot.values())
                                    .filter(slot -> mob.getItemBySlot(slot).isEnchantable())
                                    .forEach(slot -> EnchantmentHelper.enchantItem(
                                            random,
                                            mob.getItemBySlot(slot),
                                            cfg.enchantment_level.get(),
                                            cfg.treasure_enchantments.get()
                                    ));
                        }

                        mob.addTag(SPAWN);
                        mob.getPersistentData().putString(TARGET, player.getStringUUID());
                        if (DuskveilConfig.SERVER.event.retain.get())
                            mob.setPersistenceRequired();

                        level.addFreshEntity(entity);

                        MobCountStats.add(mob);
                    }
                }
            }
        }
    }


    private static ServerPlayer findOtherPlayer(ServerLevel level, ServerPlayer self, SpawnerConfig config)
    {
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
