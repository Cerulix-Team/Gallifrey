package com.cerulixteam.gallifrey.tardis;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Turns panel-view coordinates (as the panel lies on the floor in front of you: u left to right,
 * h height, w near to far, in pixels) into the block's real orientation and back, using the same model
 * rotations as the blockstates (written by tools/tardis/control_assets.py). Used for the outline and for
 * working out which button a click landed on.
 */
public final class TardisPanelGeometry {
    private TardisPanelGeometry() {}

    /** The blockstate's model rotation (x, y) in degrees: same table as vanilla buttons and levers. */
    public static int[] rotation(WallMountLocation face, Direction facing) {
        int y = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        return switch (face) {
            case FLOOR -> new int[]{0, y};
            case WALL -> new int[]{90, y};
            case CEILING -> new int[]{180, (y + 180) % 360};
        };
    }

    private static Quaternionf quaternion(BlockState state) {
        int[] r = rotation(state.get(Properties.WALL_MOUNT_LOCATION), state.get(Properties.HORIZONTAL_FACING));
        // Same as Minecraft's ModelRotation.
        return new Quaternionf().rotateYXZ((float) Math.toRadians(-r[1]), (float) Math.toRadians(-r[0]), 0.0F);
    }

    /** Panel-view pixels -> model pixels -> rotated, as 0-1 block coordinates. */
    private static Vector3f toBlock(Quaternionf q, float u, float h, float w) {
        Vector3f v = new Vector3f((16 - u) / 16F - 0.5F, h / 16F - 0.5F, w / 16F - 0.5F);
        q.transform(v);
        return v.add(0.5F, 0.5F, 0.5F);
    }

    /** A panel-view box (u0, h0, w0, u1, h1, w1) placed in the block as the state is turned. */
    public static VoxelShape box(BlockState state, float[] b) {
        Quaternionf q = quaternion(state);
        double minX = 1, minY = 1, minZ = 1, maxX = 0, maxY = 0, maxZ = 0;
        for (int i = 0; i < 8; i++) {
            Vector3f c = toBlock(q, (i & 1) == 0 ? b[0] : b[3], (i & 2) == 0 ? b[1] : b[4], (i & 4) == 0 ? b[2] : b[5]);
            minX = Math.min(minX, c.x); maxX = Math.max(maxX, c.x);
            minY = Math.min(minY, c.y); maxY = Math.max(maxY, c.y);
            minZ = Math.min(minZ, c.z); maxZ = Math.max(maxZ, c.z);
        }
        return VoxelShapes.cuboid(clamp(minX), clamp(minY), clamp(minZ), clamp(maxX), clamp(maxY), clamp(maxZ));
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, Math.round(v * 1e5) / 1e5));
    }

    /** The whole outline of a panel in this state. */
    public static VoxelShape shape(BlockState state, TardisPanel panel) {
        VoxelShape shape = VoxelShapes.empty();
        for (float[] b : panel.layout().shape()) shape = VoxelShapes.union(shape, box(state, b));
        return shape.simplify();
    }

    /** Which button a click at this point (relative to the block's corner) is on, if any. */
    @Nullable
    public static TardisPanelLayout.Button buttonAt(BlockState state, TardisPanel panel, Vec3d local) {
        TardisPanelLayout.Layout layout = panel.layout();
        if (layout.lever()) return layout.buttons().get(0);
        Quaternionf inverse = quaternion(state).conjugate();
        Vector3f v = new Vector3f((float) local.x - 0.5F, (float) local.y - 0.5F, (float) local.z - 0.5F);
        inverse.transform(v);
        float u = 16 - (v.x + 0.5F) * 16, h = (v.y + 0.5F) * 16, w = (v.z + 0.5F) * 16;
        final float e = 0.2F;
        for (TardisPanelLayout.Button b : layout.buttons()) {
            if (u >= b.u0() - e && u <= b.u1() + e && h >= b.h0() - e && h <= b.h1() + e && w >= b.w0() - e && w <= b.w1() + e) {
                return b;
            }
        }
        return null;
    }

    public static int indexOf(TardisPanel panel, TardisPanelLayout.Button button) {
        return panel.layout().buttons().indexOf(button);
    }
}
