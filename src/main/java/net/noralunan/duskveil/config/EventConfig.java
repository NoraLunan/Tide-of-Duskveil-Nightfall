package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.core.MessageType;
import net.noralunan.duskveil.core.WeatherType;
import net.noralunan.library.config.forge.NoraConfig;

import java.util.List;

public class EventConfig
{
    public final ConfigValue<Boolean> full_moon;
    public final ConfigValue<Integer> fix_day;
    public final ConfigValue<Double> trigger_chance;
    public final ConfigValue<Boolean> no_sleep;
    public final ConfigValue<String> weather;
    public final ConfigValue<Boolean> retain;
    public final ConfigValue<Boolean> despawn;
    public final ConfigValue<Boolean> bad_time_cmd;
    public final ConfigValue<Integer> start_time;
    public final ConfigValue<List<? extends String>> start_message;
    public final ConfigValue<String> start_type;
    public final ConfigValue<List<? extends String>> start_cmd;
    public final ConfigValue<Integer> end_time;
    public final ConfigValue<List<? extends String>> end_message;
    public final ConfigValue<String> end_type;
    public final ConfigValue<List<? extends String>> end_cmd;

    protected EventConfig(NoraConfig config)
    {
        config.depict("Tide Night Event Settings").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.SERVER);
        config.layer("Event");
        this.full_moon = config
                .depict("Does a full moon always trigger a Tide Night?")
                .entry("Trigger on Full Moon", true);
        this.fix_day = config
                .depict("How many days between fixed Tide Night triggers? Set to 0 to disable.")
                .depict("This setting is ignored if \"Trigger on Full Moon\" is enabled.")
                .min(0)
                .entry("Fix Interval Day", 0);
        this.trigger_chance = config
                .depict("What is the probability of triggering a Tide Night each night?")
                .range(0d, 1d)
                .entry("Trigger Chance", 0.05);
        this.no_sleep = config
                .depict("Is sleeping prevented for entities during a Tide Night?")
                .entry("Cannot Sleep", true);
        this.weather = config
                .depict("How should the weather change when a Tide Night starts, if the current dimension supports weather mechanics?")
                .entry("Weather Change", WeatherType.NONE);
        this.retain = config
                .depict("Do entities spawned by this mod ignore Minecraft's natural despawning system?")
                .entry("Ignore Despawn System", true);
        this.despawn = config
                .depict("Do entities spawned by this mod despawn automatically when a Tide Night ends?")
                .entry("Despawn on End", false);
        this.bad_time_cmd = config
                .depict("Is the \"/time\" command blocked for all players during a Tide Night?")
                .entry("Bad Time Command", true);
        config.depict("Settings for when a Tide Night starts").layer("Start");
        this.start_time = config
                .depict("What is the start time of a Tide Night?")
                .range(12000, 12999)
                .entry("Start Time", 12500);
        this.start_message = config
                .depict("What is the possible message sent when a Tide Night starts?")
                .lineBreak()
                .list(false)
                .entry("Message List", List.of("They are here...No way out", "Veil descends...Hunt initiates"));
        this.start_type = config
                .entry("Display Type", MessageType.CHAT);
        this.start_cmd = config
                .depict("Commands to execute with server permissions when a Tide Night starts.")
                .depict("Do NOT use relative coordinates (~) or self-selector (@s) unless wrapped in an 'execute' command.")
                .list(true)
                .validator(entry -> entry instanceof String)
                .entry("Command List", List.of());
        config.out();
        config.depict("Settings for when a Tide Night ends").layer("End");
        this.end_time = config
                .depict("What is the end time of a Tide Night?")
                .range(13000, 23999)
                .entry("End Time", 23500);
        this.end_message = config
                .depict("What is the possible message sent when a Tide Night ends?")
                .lineBreak()
                .list(false)
                .entry("Message List", List.of("Silence returns...Stains endure", "Uproar fades...Contamination persists"));
        this.end_type = config
                .entry("Display Type", MessageType.CHAT);
        this.end_cmd = config
                .depict("Commands to execute with server permissions when a Tide Night ends")
                .depict("Do NOT use relative coordinates (~) or self-selector (@s) unless wrapped in an 'execute' command.")
                .list(true)
                .validator(entry -> entry instanceof String)
                .entry("Command List", List.of());
        config.out(2);
    }
}
