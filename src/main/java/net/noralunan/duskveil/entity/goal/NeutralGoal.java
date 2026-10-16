package net.noralunan.duskveil.entity.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EntityConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraPlayer;

import java.util.EnumSet;

public class NeutralGoal extends Goal
{
    private final TideLevel tide;
    private final Mob self;
    private ServerPlayer player;
    private final EntityConfig config;
    private final double follow_range;
    private boolean because_this;

    public NeutralGoal(TideLevel tide, Mob self)
    {
        this.tide = tide;
        this.self = self;
        this.player = null;
        this.config = DuskveilConfig.SERVER.entity;

        AttributeInstance attribute = self.getAttribute(Attributes.FOLLOW_RANGE);
        this.follow_range = attribute == null ? 32 : attribute.getValue();

        this.because_this = false;

        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse()
    {
        return canContinueToUse() && this.self.isAlive() && this.config.neutral_aggressive.get();
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.tide.isTideNight() && !this.self.isAggressive();
    }

    @Override
    public void stop()
    {
        if (this.self.isAggressive() && this.because_this)
        {
            this.self.setTarget(null);
        }
    }

    @Override
    public void tick()
    {
        if (!NoraMath.divisible(this.tide.playTime(), this.config.pathfind_recalc.get())) return;

        Player p = this.tide.level().getNearestPlayer(this.self, this.follow_range);
        this.player = (p instanceof ServerPlayer sp) ? sp : null;
        if (this.player == null || NoraPlayer.isCheat(player))
        {
            if (this.because_this)
            {
                this.because_this = false;
            }
            return;
        }

        if (this.self.hasLineOfSight(this.player))
        {
            this.self.setTarget(this.player);
            this.because_this = true;
        }
    }
}
