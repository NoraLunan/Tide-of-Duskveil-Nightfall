package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.forge.NoraConfig;

public class OtherConfig
{
    public final ConfigValue<Boolean> modify_sky;
    public final ConfigValue<String> fog_color;
    public final ConfigValue<Boolean> lock_gamma;

    protected OtherConfig(NoraConfig config)
    {
        this.modify_sky = config
                .depict("Should the sky color be overridden during a Tide Night?")
                .entry("Override Sky", true);
        this.fog_color = config
                .depict("The color value for the sky during a Tide Night.")
                .entry("Sky Color", "#992626");
        this.lock_gamma = config
                .depict("Should player screen brightness be locked to a dim setting?")
                .entry("Lock Gamma Value", true);
    }
}
