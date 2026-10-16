package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.forge.NoraConfig;

import java.util.ArrayList;
import java.util.List;

public class SpawnerConfig
{
    public final ConfigValue<List<? extends String>> dim_blacklist;
    public final ConfigValue<Boolean> dim_whitelist;
    public final ConfigValue<Integer> min_interval;
    public final ConfigValue<Integer> max_interval;
    public final ConfigValue<Integer> first_delay;
    public final ConfigValue<Integer> max_retries;
    public final ConfigValue<Boolean> force_spawn;
    public final ConfigValue<Integer> min_distance;
    public final ConfigValue<Integer> max_distance;
    public final ConfigValue<Integer> light_level;
    public final ConfigValue<Boolean> no_baby;
    public final ConfigValue<Double> count_limit;
    public final ConfigValue<Double> wave_size;
    public final ConfigValue<Boolean> evenly_distribute;
    public final ConfigValue<Integer> mob_cap;
    public final ConfigValue<List<? extends String>> alg_whitelist;
    public final ConfigValue<Boolean> alg_blacklist;

    protected SpawnerConfig(NoraConfig config)
    {
        this.dim_blacklist = config
                .depict("Which dimensions will the mod never spawn entities in?")
                .list(true)
                .validator(value -> value instanceof String string && string.contains(":"))
                .entry("Dimension Blacklist", new ArrayList<>());
        this.dim_whitelist = config
                .depict("If you want to use the above list as a Whitelist, set it to true.")
                .entry("Dimension Blacklist Inverted", false);
        this.min_interval = config
                .depict("How often does the mod attempt to spawn a wave of entities during a Tide Night?")
                .range(1, 10000)
                .entry("Min Spawn Interval", 400);
        this.max_interval = config
                .lineBreak()
                .range(1, 10000)
                .entry("Max Spawn Interval", 1000);
        this.first_delay = config
                .depict("How long after Tide Night starts will the first entity wave spawn?")
                .depict("The regular interval above will only take effect after the first wave.")
                .depict("A negative value will use the regular interval directly.")
                .max(10000)
                .entry("First Wave Delay", 500);
        this.max_retries = config
                .depict("What is the maximum number of retries if an entity spawn attempt fails?")
                .depict("A negative value will cause the mod to keep retrying indefinitely until it succeeds.")
                .entry("Max Spawn Retries", 8);
        this.force_spawn = config
                .depict("If retries fail, force spawn the entity?")
                .entry("Force Spawn", false);
        this.min_distance = config
                .depict("What is the distance from the player that entities are allowed to spawn?")
                .min(0)
                .entry("Min Spawn Distance", 12);
        this.max_distance = config
                .lineBreak()
                .min(0)
                .entry("Max Spawn Distance", 64);
        this.light_level = config
                .depict("What is the maximum allowed light level for a valid spawn position?")
                .depict("This limit is ignored for dimensions listed in \"Algorithm Whitelist\".")
                .lineBreak()
                .range(0, 14)
                .entry("Allowed Light Level", 7);
        this.no_baby = config
                .lineBreak()
                .entry("No Baby Mob", true);
        this.count_limit = config
                .depict("This setting controls the multiplier applied to the total spawned entity count when multiple players are nearby.")
                .lineBreak()
                .min(0d)
                .entry("Count Limit", 0.75);
        this.wave_size = config
                .depict("What is the wave size multiplier applied after each entity wave spawns during a Tide Night?")
                .min(1d)
                .entry("Progressive Wave Size", 1.05);
        this.evenly_distribute = config
                .depict("When spawning an entity wave, should entities be evenly distributed around the player instead of clustered in a single spot?")
                .lineBreak()
                .entry("Evenly Distribute", false);
        this.mob_cap = config
                .depict("At what total entity count spawned by this mod will it stop spawning entity?")
                .min(0)
                .entry("Enemy Count Cap", 400);
        this.alg_whitelist = config
                .depict("For which dimensions will the mod adjust the entity spawn height relative to the player's Y-level,")
                .depict("instead of spawning on the default World Surface? This feature is incomplete.")
                .list(true)
                .validator(value -> value instanceof String string && string.contains(":"))
                .entry("Algorithm Whitelist", new ArrayList<>(List.of("minecraft:the_nether")));
        this.alg_blacklist = config
                .depict("If you want to use the above list as a Blacklist, set it to true.")
                .entry("Algorithm Whitelist Inverted", false);
    }
}
