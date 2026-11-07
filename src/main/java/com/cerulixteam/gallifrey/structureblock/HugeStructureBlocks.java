package com.cerulixteam.gallifrey.structureblock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Huge structure blocks, built into Gallifrey (it used to need the Huge Structure Blocks mod).
 *
 * Vanilla 1.20.1 caps a structure block at 48 blocks each way and finds corner blocks within 80. Here both
 * go up to {@link #LIMIT}. The size and offset travel as full ints rather than bytes, the bounding box can
 * be seen from further away, and jigsaw structures (and /place jigsaw) may be bigger and deeper.
 *
 * Finding corner blocks over that range by scanning every block would take far too long, so each world
 * keeps a set of where its structure blocks are (see mixin/structureblock).
 */
public final class HugeStructureBlocks {
    private HugeStructureBlocks() {}

    /** How big (and how far out) a structure block can reach. */
    public static final int LIMIT = 512;
    /** The deepest /place jigsaw may go (vanilla: 7). */
    public static final int MAX_JIGSAW_DEPTH = 20;
    /** How far away structure block outlines are drawn (vanilla: 96). */
    public static final int RENDER_DISTANCE = 256;

    private static final Map<World, Set<BlockPos>> STRUCTURE_BLOCKS = new WeakHashMap<>();

    public static synchronized void add(World world, BlockPos pos) {
        STRUCTURE_BLOCKS.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    public static synchronized void remove(World world, BlockPos pos) {
        Set<BlockPos> set = STRUCTURE_BLOCKS.get(world);
        if (set != null) set.remove(pos);
    }

    public static synchronized Set<BlockPos> all(World world) {
        Set<BlockPos> set = STRUCTURE_BLOCKS.get(world);
        return set == null ? Collections.emptySet() : new HashSet<>(set);
    }
}
