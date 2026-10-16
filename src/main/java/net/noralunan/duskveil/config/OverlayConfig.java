package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.duskveil.client.DuskveilOverlay;
import net.noralunan.library.config.forge.NoraConfig;

public class OverlayConfig
{
    public final ConfigValue<Boolean> enable_overlay;
    public final ConfigValue<String> overlay_pos;
    public final ConfigValue<Boolean> hide_f1;
    public final ConfigValue<Boolean> hide_f3;
    public final ConfigValue<Integer> line_spacing;
    public final ConfigValue<Integer> day_offset;
    public final ConfigValue<Boolean> camera_pos;
    public final ConfigValue<Integer> decimal_places;
    public final ConfigValue<Integer> entity_recalc;
    public final ConfigValue<Boolean> enable_shadow;
    public final ConfigValue<Integer> x_offset;
    public final ConfigValue<Integer> y_offset;

    public final ConfigValue<Boolean> show_fps;
    public final ConfigValue<Boolean> show_time;
    public final ConfigValue<Boolean> show_pos;
    public final ConfigValue<Boolean> show_entity;

    protected OverlayConfig(NoraConfig config)
    {
        this.enable_overlay = config
                .depict("Should a real-time information overlay be displayed on the player's screen?")
                .entry("Enable Info Overlay", true);
        this.overlay_pos = config
                .entry("Overlay Position", DuskveilOverlay.Position.BOTTOM_LEFT);
        this.hide_f1 = config
                .depict("Should the information overlay be hidden when the UI is hidden?")
                .entry("Hide when F1", true);
        this.hide_f3 = config
                .depict("Should the information overlay be hidden when the debug screen is open?")
                .entry("Hide when F3", true);
        this.line_spacing = config
                .depict("What should be the line spacing between each entry in the information overlay?")
                .min(0)
                .entry("Line Spacing", 10);
        this.day_offset = config
                .depict("What is the offset for the game day displayed in the overlay compared to Minecraft's internal day count?")
                .depict("For example, if set to 1, Minecraft Day 0 will be displayed as Day 1.")
                .entry("Day Display Offset", 1);
        this.camera_pos = config
                .depict("By default, the Pos entry returns the player's position. Set to true to use the current client camera position instead.")
                .lineBreak()
                .entry("Use Camera Position", false);
        this.decimal_places = config
                .depict("How many decimal places should be retained for each value in the Pos entry?")
                .range(0, 8)
                .entry("Pos Decimal Places", 1);
        this.entity_recalc = config
                .depict("How often is the Entity entry in the information overlay recalculated?")
                .depict(DuskveilConfig.RELOAD)
                .restart()
                .entry("Entity Recalc Interval", 20);
        this.enable_shadow = config
                .depict("Should the text displayed in the overlay have a shadow?")
                .entry("Overlay Text Shadow", false);
        this.x_offset = config
                .depict("What is the horizontal offset of the overlay?")
                .entry("Overlay X Offset", 0);
        this.y_offset = config
                .depict("What is the vertical offset of the overlay?")
                .entry("Overlay Y Offset", 0);
        this.show_fps = config
                .lineBreak()
                .entry("Enable Fps Entry", true);
        this.show_time = config
                .lineBreak()
                .entry("Enable Time Entry", true);
        this.show_pos = config
                .lineBreak()
                .entry("Enable Pos Entry", true);
        this.show_entity = config
                .lineBreak()
                .entry("Enable Entity Entry", true);
    }
}
