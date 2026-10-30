package com.cerulixteam.gallifrey.world.portal;

import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Portal frame tester with a larger minimum size than the nether-style 2x3.
 * Sizes are the inner opening (portal blocks), not including the frame.
 */
public class GallifreyPortalAreaHelper extends VanillaPortalAreaHelper {

    public static final int MIN_WIDTH = 8;
    public static final int MIN_HEIGHT = 5;

    public GallifreyPortalAreaHelper() {
        super();
    }

    @Override
    public boolean isValidFrame() {
        return this.lowerCorner != null
                && this.width >= MIN_WIDTH && this.width <= maxWidth
                && this.height >= MIN_HEIGHT && this.height <= maxHeight;
    }

    // The parent hardcodes "new VanillaPortalAreaHelper()" here, so this must be overridden
    @Override
    public Optional<PortalFrameTester> getOrEmpty(WorldAccess worldAccess, BlockPos blockPos,
                                                  Predicate<PortalFrameTester> predicate,
                                                  Direction.Axis axis, Block... foundations) {
        Optional<PortalFrameTester> optional = Optional.of(
                new GallifreyPortalAreaHelper().init(worldAccess, blockPos, axis, foundations)
        ).filter(predicate);
        if (optional.isPresent()) return optional;

        Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(
                new GallifreyPortalAreaHelper().init(worldAccess, blockPos, axis2, foundations)
        ).filter(predicate);
    }

    // Space check for auto-generating the portal on the other side
    @Override
    public BlockPos doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis) {
        for (int x = 0; x < MIN_WIDTH; x++) {
            if (!canHoldPortal(world, attemptPos.offset(axis, x).down())) return null;
            for (int y = 0; y < MIN_HEIGHT; y++) {
                if (!isEmptySpace(world.getBlockState(attemptPos.offset(axis, x).up(y)))) return null;
            }
        }
        return attemptPos;
    }

    // Builds the destination-side portal at MIN size instead of 2x3
    @Override
    public void createPortal(World world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        Direction.Axis rotatedAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

        // Side columns (including corners)
        for (int y = -1; y <= MIN_HEIGHT; y++) {
            world.setBlockState(pos.up(y).offset(axis, -1), frameBlock);
            world.setBlockState(pos.up(y).offset(axis, MIN_WIDTH), frameBlock);
        }
        // Bottom and top rows
        for (int x = 0; x < MIN_WIDTH; x++) {
            world.setBlockState(pos.down().offset(axis, x), frameBlock);
            world.setBlockState(pos.up(MIN_HEIGHT).offset(axis, x), frameBlock);
        }
        // Clear space in front of and behind the portal, plus landing pads
        for (int x = -1; x <= MIN_WIDTH; x++) {
            for (int y = 0; y <= MIN_HEIGHT; y++) {
                fillAirAroundPortal(world, pos.offset(axis, x).up(y).offset(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.offset(axis, x).up(y).offset(rotatedAxis, -1));
            }
        }
        for (int x = 0; x < MIN_WIDTH; x++) {
            placeLandingPad(world, pos.down().offset(axis, x).offset(rotatedAxis, 1), frameBlock);
            placeLandingPad(world, pos.down().offset(axis, x).offset(rotatedAxis, -1), frameBlock);
        }

        this.lowerCorner = pos;
        this.width = MIN_WIDTH;
        this.height = MIN_HEIGHT;
        this.axis = axis;
        this.world = world;
        this.foundPortalBlocks = MIN_WIDTH * MIN_HEIGHT;
        lightPortal(frameBlock.getBlock());
    }
}
