package net.noralunan.duskveil.entity.gaze;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.GazeConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraComponent;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraPlayer;

public class GazeMechanic
{
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) return;

        if (event.player instanceof ServerPlayer player && player.level() instanceof TideLevel tide)
        {
            if (NoraPlayer.isCheat(player)) return;

            GazeConfig config = DuskveilConfig.SERVER.gaze;
            if (config.enable.get() == false) return;
            if (!tide.isTideNight()) return;
            if (!NoraMath.divisible(tide.playTime(), config.interval.get())) return;

            if (NoraMath.nextDouble() < config.success_chance.get())
            {
                execute(tide.level(), player);
            }
        }
    }

    public static void execute(ServerLevel level, ServerPlayer player)
    {
        double radius = DuskveilConfig.SERVER.spawner.max_distance.get() * 1.0625; // max_distance + max_distance >> 4
        AABB area = new AABB(
                player.getX() - radius,
                level.getMinBuildHeight(),
                player.getZ() - radius,
                player.getX() + radius,
                level.getMaxBuildHeight(),
                player.getZ() + radius
        );

        GazeConfig config = DuskveilConfig.SERVER.gaze;
        level.getEntities(player, area).stream()
                .filter(entity -> entity instanceof Mob mob
                        && !mob.isAggressive()
                        && (mob instanceof NeutralMob ? config.neutral_gaze.get() : mob instanceof Enemy)
                ).forEach(entity -> ((Mob) entity).setTarget(player));

        if (config.send_message.get())
        {
            player.displayClientMessage(NoraComponent.of(config.gaze_message.get()), true);
        }
    }
}
