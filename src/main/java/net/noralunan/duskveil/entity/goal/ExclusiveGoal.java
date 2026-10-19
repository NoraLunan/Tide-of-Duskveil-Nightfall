package net.noralunan.duskveil.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EntityConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.entity.EntityDriver;

public class ExclusiveGoal extends Goal
{
    private final TideLevel tide;
    private final Mob self;
    private final EntityConfig config;

    private int fire_cooling;
    private int fall_cooling;

    public ExclusiveGoal(TideLevel tide, Mob self)
    {
        this.tide = tide;
        this.self = self;
        this.config = DuskveilConfig.SERVER.entity;
    }

    @Override
    public boolean canUse()
    {
        return this.tide.isTideNight() && this.self.isAlive();
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void start()
    {
        EntityDriver.modifyAttribute(this.self);
        EntityDriver.addEffect(this.tide, this.self);
    }

    @Override
    public void stop()
    {
        EntityDriver.restoreAttribute(this.self);
    }

    @Override
    public void tick()
    {
        if (this.self.isInWall() && this.config.break_block.get())
        {
            destroyBlock();
        }

        if (this.fall_cooling > 0) this.fall_cooling--;
        if (this.fire_cooling > 0) this.fire_cooling--;

        if (!this.self.onGround() && !(this.self instanceof FlyingMob) && this.config.immune_fall.get())
        {
            this.fall_cooling = 100;
            this.self.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 120, 0, false, false, false
            ));
        }
        if (this.self.isInLava() || this.self.isOnFire())
        {
            if (this.config.immune_fire.get())
            {
                this.fire_cooling = 100;
                this.self.addEffect(new MobEffectInstance(
                   MobEffects.FIRE_RESISTANCE, 120, 0, false, false, false
                ));
            }
        }
    }

    private void destroyBlock()
    {
        int rw = Mth.ceil(this.self.getBbWidth() / 2);
        int x1 = this.self.getBlockX() - rw;
        int y1 = this.self.getBlockY();
        int z1 = this.self.getBlockZ() - rw;
        int x2 = this.self.getBlockX() + rw;
        int y2 = this.self.getBlockY() + Mth.ceil(this.self.getBbHeight()) - 1;
        int z2 = this.self.getBlockZ() + rw;

        for (int x = x1; x <= x2; x++)
        {
            for (int y = y1; y <= y2; y++)
            {
                for (int z = z1; z <= z2; z++)
                {
                    ServerLevel level = tide.level();
                    BlockPos pos = BlockPos.containing(x, y, z);

                    float hardness = level.getBlockState(pos).getDestroySpeed(level, pos);
                    if (hardness >= 0 && hardness < this.config.max_hardness.get())
                    {
                        level.destroyBlock(pos, false, this.self);
                    }
                }
            }
        }
        this.self.swing(InteractionHand.MAIN_HAND, true);
    }
}
