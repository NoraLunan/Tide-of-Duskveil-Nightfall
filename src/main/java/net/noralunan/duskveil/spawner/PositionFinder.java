package net.noralunan.duskveil.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.library.common.NoraMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PositionFinder
{
    protected static class WorldSurface
    {
        /**
         * @param x         Center X
         * @param min       Min Distance
         * @param retries   失败后重新尝试的最大次数而非总尝试次数
         */
        protected static Vec3 find(TideLevel tide, int x, int z, int min, int max, int retries, boolean force)
        {
            if (min > max)
                throw new IllegalArgumentException(String.format("Invalid range: min(%d) and max(%d)", min, max));

            Vec3i result;
            boolean success;
            int attempt = retries + 1;

            do
            {
                ColumnPos vec2 = NoraMath.nextPlane(x, z, min, max);
                int rx = NoraMath.round(vec2.x());
                int rz = NoraMath.round(vec2.z());
                int ry = tide.worldSurface(rx, rz);
                result = new Vec3i(rx, ry, rz);
                attempt--;
                success = check(tide, result);
            }
            while (!success && attempt != 0);

            if (force || success)
                return new Vec3(result.getX(), result.getY(), result.getZ());
            else
                return null;
        }

        private static boolean check(TideLevel tide, Vec3i target)
        {
            if (!tide.level().getWorldBorder().isWithinBounds(target.getX(), target.getY(), target.getZ()))
            {
                return false;
            }
            BlockPos pos = new BlockPos(target);
            int light = tide.brightness(pos).block;
            return light <= DuskveilConfig.SERVER.spawner.light_level.get();
        }
    }


    protected static class DynamicAlgorithm
    {
        private static final int TRIES_PER = 8;
        private static final int OFFSET_RANGE = 4;

        private static final Set<Block> BAD_BLOCKS = Set.of(Blocks.NETHER_PORTAL, Blocks.END_PORTAL);
        private static final Set<Fluid> BAD_FLUIDS = Set.of(Fluids.LAVA);

        protected static Vec3 find(TideLevel tide, EntityType<?> type, int x, int y, int z, int min, int max, int retries, boolean force)
        {
            if (min >= max)
                throw new IllegalArgumentException(String.format("Invalid range: min(%d) and max(%d)", min, max));

            ServerLevel level = tide.level();
            int surfaceY = tide.worldSurface(x, z);

            if (y >= surfaceY)
            {
                Vec3 surfaceResult = WorldSurface.find(tide, x, z, min, max, 0, true);
                if (surfaceResult != null)
                {
                    return surfaceResult;
                }
            }

            BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos valid = null;
            boolean success = false;
            int attempt = retries + 1;

            do
            {
                ColumnPos vec2 = NoraMath.nextPlane(x, z, min, max);
                int rx = NoraMath.round(vec2.x());
                int rz = NoraMath.round(vec2.z());

                if (!level.getWorldBorder().isWithinBounds(rx, 0, rz))
                {
                    attempt--;
                    continue;
                }

                int baseY = y + NoraMath.nextInt(-TRIES_PER, TRIES_PER + 1);
                List<Integer> candidateY = new ArrayList<>();
                for (int dy = -OFFSET_RANGE; dy <= OFFSET_RANGE; dy++)
                {
                    candidateY.add(baseY + dy);
                }

                Collections.shuffle(candidateY);

                for (int ty : candidateY)
                {
                    candidate.set(rx, ty, rz);
                    if (check(level, type, candidate))
                    {
                        success = true;
                        valid = new BlockPos.MutableBlockPos(candidate.getX(), candidate.getY(), candidate.getZ());
                        break;
                    }
                }
                attempt--;
            }
            while (!success && attempt != 0);

            if (valid != null)
                return new Vec3(valid.getX() + 0.5, valid.getY(), valid.getZ() + 0.5);
            else if (force)
                return new Vec3(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
            else
                return null;

        }

        private static boolean check(ServerLevel level, EntityType<?> type, BlockPos.MutableBlockPos target)
        {
            if (!level.getWorldBorder().isWithinBounds(target))
            {
                return false;
            }

            double hw = type.getWidth() / 2d;
            double hh = type.getHeight();
            AABB area = new AABB(
                    target.getX() + 0.5 - hw,
                    target.getY(),
                    target.getZ() + 0.5 - hw,
                    target.getX() + 0.5 + hw,
                    target.getY() + hh,
                    target.getZ() + 0.5 + hw
            );

            if (!level.noCollision(area))
            {
                return false;
            }

            int minX = Mth.floor(area.minX);
            int maxX = Mth.floor(area.maxX);
            int minY = Mth.floor(area.minY);
            int maxY = Mth.floor(area.maxY);
            int minZ = Mth.floor(area.minZ);
            int maxZ = Mth.floor(area.maxZ);

            BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
            for (int cx = minX; cx <= maxX; cx++)
            {
                for (int cy = minY; cy <= maxY; cy++)
                {
                    for (int cz = minZ; cz <= maxZ; cz++)
                    {
                        mp.set(cx, cy, cz);
                        BlockState state = level.getBlockState(mp);
                        FluidState fluid = level.getFluidState(mp);

                        if (!NaturalSpawner.isValidEmptySpawnBlock(level, mp, state, fluid, type))
                        {
                            return false;
                        }
                        if (BAD_BLOCKS.contains(state.getBlock()) || BAD_FLUIDS.contains(fluid.getType()))
                        {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }
}
