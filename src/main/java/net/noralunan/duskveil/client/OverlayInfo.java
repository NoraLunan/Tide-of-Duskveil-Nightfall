package net.noralunan.duskveil.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.OverlayConfig;
import net.noralunan.library.common.NoraComponent;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraWorld;
import net.noralunan.library.common.event.NoraEvent;
import net.noralunan.library.common.event.NoraTickEvent;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OverlayInfo
{
    private static final BiFunction<String, ChatFormatting, Component> TEXT = NoraComponent::of;

    protected static abstract class InfoEntry
    {
        protected abstract Component get();
    }

    protected static class Fps extends InfoEntry
    {
        @Override
        protected Component get()
        {
            int fps = Minecraft.getInstance().getFps();
            return TEXT.apply(fps + " FPS", color(fps));
        }

        private static ChatFormatting color(int fps)
        {
            if (fps < 5) return ChatFormatting.DARK_RED;
            else if (fps < 10) return ChatFormatting.RED;
            else if (fps < 20) return ChatFormatting.GOLD;
            else if (fps < 30) return ChatFormatting.YELLOW;
            else return ChatFormatting.GREEN;
        }
    }

    protected static class Time extends InfoEntry
    {
        @Override
        protected Component get()
        {
            return Component.translatable("duskveil.overlay.time").withStyle(ChatFormatting.YELLOW)
                    .append(TEXT.apply(hour() + ":" + minute(), ChatFormatting.GREEN))
                    .append(TEXT.apply(" (Day " + day() + ")", ChatFormatting.DARK_AQUA));
        }

        private static int day()
        {
            NoraWorld world = world();
            if (world == null) return -1;
            return world.day() + DuskveilConfig.CLIENT.overlay.day_offset.get();
        }

        private static String hour()
        {
            int hour = Math.floorDiv(dayTime(), 1000);
            return format(Math.floorMod(hour + 6, 24));
        }

        private static String minute()
        {
            int remain = Math.floorMod(dayTime(), 1000);
            return format(remain * 3 / 50);
        }

        private static int dayTime()
        {
            NoraWorld world = world();
            return world == null ? 0 : world.dayTime();
        }

        private static String format(int target)
        {
            return target < 10 ? "0" + target : String.valueOf(target);
        }
    }

    public static class Pos extends InfoEntry
    {
        @Override
        protected Component get()
        {
            String format = DuskveilConfig.CLIENT.overlay.camera_pos.get() ? cam() : pos();
            return Component.translatable("duskveil.overlay.pos").withStyle(ChatFormatting.YELLOW)
                    .append(TEXT.apply(format, ChatFormatting.GOLD));
        }

        private static String pos()
        {
            Player player = player();
            if (player == null) return "?";
            Vec3 position = player.position();
            return format(position.x, position.y, position.z);
        }

        private static String cam()
        {
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            return format(camera.x, camera.y, camera.z);
        }

        private static Function<Double, String> algorithm = null;
        private static String format(double x, double y, double z)
        {
            if (algorithm == null) setAlgorithm();
            return "X: " + algorithm.apply(x) + ", Y: " + algorithm.apply(y) + ", Z: " + algorithm.apply(z);
        }

        protected static void setAlgorithm()
        {
            int places = DuskveilConfig.CLIENT.overlay.decimal_places.get();
            algorithm = places == 0 ?
                    t -> String.valueOf(NoraMath.round(t)) :
                    t -> String.valueOf(NoraMath.retain(t, places));
        }

        @SubscribeEvent
        public static void reload(DuskveilConfig.ClientConfigReloadEvent event)
        {
            OverlayInfo.Pos.setAlgorithm();
        }
    }

    public static class EntityEntry extends InfoEntry
    {
        @Override
        protected Component get()
        {
            if (INSTANCE == null) return Component.empty();
            return Component.translatable("duskveil.overlay.entity").withStyle(ChatFormatting.YELLOW)
                    .append(TEXT.apply(String.valueOf(INSTANCE.entity), ChatFormatting.AQUA))
                    .append(TEXT.apply(", M: ", ChatFormatting.YELLOW))
                    .append(TEXT.apply(String.valueOf(INSTANCE.enemy), ChatFormatting.RED))
                    .append(TEXT.apply(", I: ", ChatFormatting.YELLOW))
                    .append(TEXT.apply(String.valueOf(INSTANCE.item), ChatFormatting.LIGHT_PURPLE));
        }

        private static int entity(ClientLevel level, AABB area)
        {
            return level.getEntities(null, area).size();
        }

        private static int enemy(ClientLevel level, AABB area)
        {
            return level.getEntities((Entity) null, area, Enemy.class::isInstance).size();
        }

        private static int item(ClientLevel level, AABB area)
        {
            return level.getEntities((Entity) null, area, ItemEntity.class::isInstance).size();
        }

        protected static final EntityEntry INSTANCE = new EntityEntry();

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event)
        {
            NoraEvent.addClient(DuskveilConfig.CLIENT.overlay.entity_recalc.get(), INSTANCE::tick);
        }

        private void tick(NoraTickEvent.ClientTick event)
        {
            NoraWorld world = world();
            if (world == null || !world.isClientSide()) return;
            OverlayConfig config = DuskveilConfig.CLIENT.overlay;
            if (config.enable_overlay.get() == false || config.show_entity.get() == false) return;

            Player player = player();
            if (player == null) return;
            double x = player.getX();
            double z = player.getZ();
            int radius = Minecraft.getInstance().options.renderDistance().get() * 16;

            ClientLevel level = (ClientLevel) world.level();
            AABB area = new AABB(
                    x - radius,
                    level.getMinBuildHeight(),
                    z - radius,
                    x + radius,
                    level.getMaxBuildHeight(),
                    z + radius
            );
            INSTANCE.entity = entity(level, area);
            INSTANCE.enemy = enemy(level, area);
            INSTANCE.item = item(level, area);
        }

        private int entity;
        private int enemy;
        private int item;
        private EntityEntry() {
            this.entity = 0;
            this.enemy = 0;
            this.item = 0;
        }
    }

    @Nullable
    private static Player player()
    {
        return Minecraft.getInstance().player;
    }

    @Nullable
    private static NoraWorld world()
    {
        Player player = player();
        if (player == null) return null;
        else return (NoraWorld) player.level();
    }
}
