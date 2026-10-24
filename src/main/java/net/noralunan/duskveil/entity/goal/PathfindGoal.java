package net.noralunan.duskveil.entity.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EntityConfig;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraPlayer;

import java.util.EnumSet;

public class PathfindGoal extends Goal
{
    private final Mob self;
    private final ServerPlayer target;
    private final EntityConfig config;
    private int timer;
    private final double follow_range;

    public PathfindGoal(Mob self, Player target)
    {
        this.self = self;
        this.target = target instanceof ServerPlayer player ? player : null;
        this.config = DuskveilConfig.SERVER.entity;
        this.timer = Integer.MAX_VALUE;

        AttributeInstance attribute = self.getAttribute(Attributes.FOLLOW_RANGE);
        this.follow_range = attribute == null ? 32 : attribute.getValue();

        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse()
    {
        return canContinueToUse() && this.self.isAlive() && this.config.pathfind_move.get();
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.target != null && this.target.isAlive();
    }

    @Override
    public boolean isInterruptable()
    {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void start()
    {
        this.timer = NoraMath.nextInt(0, this.config.pathfind_recalc.get());
    }

    @Override
    public void tick()
    {
        if (this.target == null) return;

        if (this.self.distanceTo(this.target) <= this.follow_range && this.self.hasLineOfSight(this.target))
        {
            if (NoraPlayer.noCheat(this.target))
            {
                if (!(this.self instanceof NeutralMob) || this.config.neutral_aggressive.get())
                {
                    this.self.setTarget(this.target);
                    return;
                }
            }
        }

        if (timer-- > 0) return;
        this.timer = this.config.pathfind_recalc.get();
        this.self.getNavigation().moveTo(this.target, 1);
    }
}
