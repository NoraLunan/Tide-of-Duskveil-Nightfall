package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.forge.NoraConfig;

public class GazeConfig
{
    public final ConfigValue<Boolean> enable;
    public final ConfigValue<Integer> interval;
    public final ConfigValue<Double> success_chance;
    public final ConfigValue<Boolean> neutral_gaze;
    public final ConfigValue<Boolean> send_message;
    public final ConfigValue<String> gaze_message;
    public final ConfigValue<Boolean> always_cmd;

    protected GazeConfig(NoraConfig config)
    {
        this.enable = config
                .depict("Is the Gaze mechanic enabled during a Tide Night?")
                .entry("Gaze Mechanic", false);
        this.interval = config
                .depict("How often do entities near a player attempt to gaze at them?")
                .min(1)
                .entry("Attempt Interval", 200);
        this.success_chance = config
                .depict("What is the success chance for each gaze attempt on a player?")
                .range(0d, 1d)
                .entry("Success Chance", 0.3);
        this.neutral_gaze = config
                .depict("Can non-Monster type neutral mobs gaze at players?")
                .entry("Allow Neutral Gaze", true);
        this.send_message = config
                .depict("Should an action bar message be sent to the player when a gaze succeeds?")
                .entry("Send Actionbar Message", true);
        this.gaze_message = config
                .depict("What is the action bar message sent to the player when a gaze succeeds?")
                .lineBreak()
                .entry("Message Content", "They see you...");
        this.always_cmd = config
                .depict("Does command-triggered forced gaze ignore whether a Tide Night is currently active?")
                .entry("Command Gaze", true);
    }
}
