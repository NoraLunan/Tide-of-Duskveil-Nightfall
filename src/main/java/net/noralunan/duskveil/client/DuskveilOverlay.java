package net.noralunan.duskveil.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.OverlayConfig;
import net.noralunan.library.common.NoraHelper;

@OnlyIn(Dist.CLIENT)
public class DuskveilOverlay
{
    private static DuskveilOverlay overlay = null;

    public static void setup(FMLClientSetupEvent ignored)
    {
        if (overlay == null) overlay = new DuskveilOverlay();
        MinecraftForge.EVENT_BUS.register(overlay);
    }

    private final Minecraft client;
    private final Font font;
    private boolean left;
    private boolean top;

    private boolean enable;
    private boolean hide_f1;
    private boolean hide_f3;
    private int line_spacing;
    private int xo;
    private int yo;
    private boolean shadow;

    private int count;

    private final OverlayInfo.Fps fps;
    private final OverlayInfo.Time time;
    private final OverlayInfo.Pos pos;
    private final OverlayInfo.EntityEntry entity;

    private boolean show_fps;
    private boolean show_time;
    private boolean show_pos;
    private boolean show_entity;

    protected DuskveilOverlay()
    {
        this.client = Minecraft.getInstance();
        this.font = this.client.font;
        reload(null);
        this.count = 0;

        this.fps = new OverlayInfo.Fps();
        this.time = new OverlayInfo.Time();
        this.pos = new OverlayInfo.Pos();
        this.entity = OverlayInfo.EntityEntry.INSTANCE;
    }

    @SubscribeEvent
    public void reload(DuskveilConfig.ClientConfigReloadEvent event)
    {
        OverlayConfig config = DuskveilConfig.CLIENT.overlay;
        Position pos = NoraHelper.getEnum(Position.class, config.overlay_pos);
        this.left = pos == Position.BOTTOM_LEFT || pos == Position.TOP_LEFT;
        this.top = pos == Position.TOP_LEFT || pos == Position.TOP_RIGHT;

        this.enable = config.enable_overlay.get();
        this.hide_f1 = config.hide_f1.get();
        this.hide_f3 = config.hide_f3.get();
        this.line_spacing = config.line_spacing.get();
        this.xo = config.x_offset.get();
        this.yo = config.y_offset.get();
        this.shadow = config.enable_shadow.get();

        this.show_fps = config.show_fps.get();
        this.show_time = config.show_time.get();
        this.show_pos = config.show_pos.get();
        this.show_entity = config.show_entity.get();
    }

    @SubscribeEvent
    public void render(RenderGuiEvent.Post event)
    {
        if (this.client.player == null || !this.enable) return;
        if (this.client.options.hideGui && this.hide_f1) return;
        if (this.client.options.renderDebug && this.hide_f3) return;
        GuiGraphics hud = event.getGuiGraphics();

        draw(hud, this.show_entity, this.entity);
        draw(hud, this.show_pos, this.pos);
        draw(hud, this.show_time, this.time);
        draw(hud, this.show_fps, this.fps);

        this.count = 0;
    }

    private void draw(GuiGraphics hud, boolean enable, OverlayInfo.InfoEntry info)
    {
        if (!enable) return;
        Window window = this.client.getWindow();

        Component t = info.get();
        int x = calculateX(window, t);
        int y = calculateY(window);

        hud.drawString(this.font, t, x, y, 0, this.shadow);
    }

    private int calculateX(Window window, Component content)
    {
        int width = window.getGuiScaledWidth();
        return (this.left ? 2 : width - 2 - this.font.width(content)) + this.xo;
    }

    private int calculateY(Window window)
    {
        int height = window.getGuiScaledHeight();
        int total = this.line_spacing * this.count;
        int result = (this.top ? total : height - total - this.line_spacing) + this.yo;
        this.count++;
        return result;
    }

    public enum Position
    {
        BOTTOM_LEFT,
        TOP_LEFT,
        BOTTOM_RIGHT,
        TOP_RIGHT
    }
}
