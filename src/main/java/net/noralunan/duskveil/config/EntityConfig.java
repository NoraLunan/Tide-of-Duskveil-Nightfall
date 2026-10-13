package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.forge.NoraConfig;

import java.util.ArrayList;
import java.util.List;

public class EntityConfig
{
    public final ConfigValue<Boolean> neutral_aggressive;
    public final ConfigValue<Double> health_mul;
    public final ConfigValue<Double> armor_mul;
    public final ConfigValue<Double> toughness_mul;
    public final ConfigValue<Double> attack_mul;
    public final ConfigValue<Double> speed_mul;
    public final ConfigValue<List<? extends String>> buff_list;
    public final ConfigValue<Boolean> pathfind_move;
    public final ConfigValue<Integer> pathfind_recalc;
    public final ConfigValue<Integer> shield_value;
    public final ConfigValue<Boolean> charged_creeper;
    public final ConfigValue<Boolean> disable_infighting;
    public final ConfigValue<Boolean> enchant;
    public final ConfigValue<Integer> enchantment_level;
    public final ConfigValue<Boolean> treasure_enchantments;

    protected EntityConfig(NoraConfig config)
    {
        this.neutral_aggressive = config
                .depict("Do neutral mobs actively attack players during a Tide Night?")
                .entry("Neutral Aggressive", true);
        String mul = "What is the multiplier for the %s of entities spawned by this mod?";
        this.health_mul = config
                .depict(String.format(mul, "max health"))
                .min(0d)
                .entry("Max Health Multiplier", 1d);
        this.armor_mul = config
                .depict(String.format(mul, "armor value"))
                .min(0d)
                .entry("Armor Multiplier", 1d);
        this.toughness_mul = config
                .depict(String.format(mul, "armor toughness"))
                .min(0d)
                .entry("Toughness Multiplier", 1d);
        this.attack_mul = config
                .depict(String.format(mul, "melee attack damage"))
                .min(0d)
                .entry("Melee Damage Multiplier", 1d);
        this.speed_mul = config
                .depict(String.format(mul, "movement speed"))
                .min(0d)
                .entry("Movement Speed Multiplier", 1d);
        this.buff_list = config
                .depict("What status effects are applied to entities spawned by this mod? Format: ID|Level.")
                .depict("The level corresponds directly to the in-game effect level (not the amplifier).")
                .depict("If no level is specified, it defaults to 1.")
                .list(true)
                .validator(value -> value instanceof String string && string.contains(":"))
                .entry("Status Effect List", new ArrayList<>());
        this.pathfind_move = config
                .depict("Do entities spawned by this mod pathfind towards the player's position upon spawning?")
                .restart()
                .entry("Pathfind to Player", true);
        this.pathfind_recalc = config
                .depict("If \"Pathfind to Player\" is enabled, what is the interval for recalculating the entity's pathfinding AI?")
                .min(1)
                .entry("Pathfind Recalc Interval", 40);
        this.shield_value = config
                .depict("How much shield value do entities spawned by this mod have?")
                .min(0)
                .entry("Initial Shield Value", 0);
        this.charged_creeper = config
                .depict("If the entity is a Creeper, is it always spawned in a charged state?")
                .entry("Creeper Always Charged", true);
        this.disable_infighting = config
                .depict("If true, entities spawned by this mod will not attack each other.")
                .entry("Disable Attack Each Other", false);
        this.enchant = config
                .depict("Should equipment worn by spawned entities be enchanted?")
                .entry("Enchant Equipment", true);
        this.enchantment_level = config
                .depict("The enchantment power applied to the equipment of spawned entities acts as the \"enchanting table level\",")
                .depict("rather than being the direct level of the enchantment itself.")
                .min(0)
                .entry("Enchantment Power", 30);
        this.treasure_enchantments = config
                .depict("Can treasure enchantments be included in the enchantments for spawned entities' equipment?")
                .entry("Allow Treasure Enchantment", true);
    }
}
