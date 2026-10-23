package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.noralunan.library.config.forge.NoraConfig;

public class CommandConfig
{
    public final ForgeConfigSpec.ConfigValue<Boolean> marker;
    public final ForgeConfigSpec.ConfigValue<Boolean> start;
    public final ForgeConfigSpec.ConfigValue<Boolean> stop;
    public final ForgeConfigSpec.ConfigValue<Boolean> count;
    public final ForgeConfigSpec.ConfigValue<Boolean> clear;
    public final ForgeConfigSpec.ConfigValue<Boolean> gaze;
    public final ForgeConfigSpec.ConfigValue<Boolean> reload;
    public final ForgeConfigSpec.ConfigValue<Boolean> spawn;

    protected CommandConfig(NoraConfig config)
    {
        this.marker = config.restart().lineBreak().entry("Enable Marker Command", true);
        this.start = config.restart().lineBreak().entry("Enable Start Command", true);
        this.stop = config.restart().lineBreak().entry("Enable Stop Command", true);
        this.count = config.restart().lineBreak().entry("Enable Count Command", true);
        this.clear = config.restart().lineBreak().entry("Enable Clear Command", true);
        this.gaze = config.restart().lineBreak().entry("Enable Gaze Command", true);
        this.reload = config.restart().lineBreak().entry("Enable Reload Command", true);
        this.spawn = config.restart().lineBreak().entry("Enable Spawn Command", true);
    }
}
