package net.noralunan.duskveil.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraLogger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusEffect
{
    private static final NoraLogger LOGGER = Duskveil.logger();
    private static List<Map.Entry<MobEffect, Integer>> cache = null;

    static void add(TideLevel tide, Mob mob)
    {
        if (cache == null) return;
        cache.stream()
                .filter(effect -> effect.getKey() != null)
                .forEach(effect -> {
            MobEffectInstance status_effect = new MobEffectInstance(
                    effect.getKey(),
                    tide.getTideNightRemain(),
                    effect.getValue(),
                    false,
                    false,
                    false
            );
            mob.forceAddEffect(status_effect, null);
        });
    }

    @SubscribeEvent
    public static void start(ServerStartedEvent event)
    {
        refresh();
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event)
    {
        cache.clear();
        cache = null;
    }

    @SubscribeEvent
    public static void reload(DuskveilConfig.ServerConfigReloadEvent event)
    {
        refresh();
    }

    private static void refresh()
    {
        if (cache == null) cache = new ArrayList<>();
        else cache.clear();

        DuskveilConfig.SERVER.entity.buff_list.get().forEach(buff -> {
            String id;
            int amplifier;
            if (buff.contains("|"))
            {
                int index = buff.indexOf("|");
                id = buff.substring(0, index);
                try
                {
                    amplifier = Math.max(0, Integer.parseInt(buff.substring(index + 1)) - 1);
                }
                catch (Exception exception)
                {
                    amplifier = 0;
                }
            }
            else
            {
                id = buff;
                amplifier = 0;
            }

            try
            {
                ResourceLocation resource = ResourceLocation.tryParse(id);
                if (resource != null)
                {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(resource);
                    cache.add(new AbstractMap.SimpleEntry<>(effect, amplifier));
                }
            }
            catch (Exception ignored)
            {
                LOGGER.error("Invalid effect ID: {}", id);
            }
        });
    }
}
