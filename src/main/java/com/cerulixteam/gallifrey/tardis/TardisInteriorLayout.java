package com.cerulixteam.gallifrey.tardis;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where the exit door and the console spot are in each bundled interior.
 *
 * The interior structures were saved with two marker blocks from another mod: "ait:door_block" (where the
 * door goes, its "facing" pointing into the room) and "ait:console" (the middle of the console area). Those
 * blocks don't exist here, so they load as air; this reads their positions straight from the structure file
 * instead, so the door and the controls land in the right place in every interior.
 */
public final class TardisInteriorLayout {
    /** Positions are relative to the structure's corner (where it is placed). */
    public record Layout(@Nullable BlockPos door, Direction doorFacing, @Nullable BlockPos console) {}

    private static final Layout NONE = new Layout(null, Direction.SOUTH, null);
    private static final Map<String, Layout> CACHE = new ConcurrentHashMap<>();

    private TardisInteriorLayout() {}

    public static Layout get(ResourceManager resources, String interior) {
        return CACHE.computeIfAbsent(interior, name -> read(resources, name));
    }

    private static Layout read(ResourceManager resources, String name) {
        Identifier file = new Identifier("gallifrey", "structures/interiors/" + name + ".nbt");
        Optional<Resource> resource = resources.getResource(file);
        if (resource.isEmpty()) return NONE;
        try (InputStream in = resource.get().getInputStream()) {
            NbtCompound nbt = NbtIo.readCompressed(in);
            NbtList palette = nbt.getList("palette", NbtElement.COMPOUND_TYPE);
            if (palette.isEmpty()) {
                NbtList palettes = nbt.getList("palettes", NbtElement.LIST_TYPE);
                if (!palettes.isEmpty()) palette = palettes.getList(0);
            }
            BlockPos door = null;
            BlockPos console = null;
            Direction facing = Direction.SOUTH;
            NbtList blocks = nbt.getList("blocks", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < blocks.size(); i++) {
                NbtCompound block = blocks.getCompound(i);
                NbtCompound state = palette.getCompound(block.getInt("state"));
                String id = state.getString("Name");
                boolean isDoor = id.equals("ait:door_block");
                if (!isDoor && !id.equals("ait:console")) continue;
                NbtList p = block.getList("pos", NbtElement.INT_TYPE);
                BlockPos pos = new BlockPos(p.getInt(0), p.getInt(1), p.getInt(2));
                if (isDoor) {
                    door = pos;
                    Direction d = Direction.byName(state.getCompound("Properties").getString("facing"));
                    if (d != null && d.getAxis().isHorizontal()) facing = d;
                } else {
                    console = pos;
                }
            }
            return new Layout(door, facing, console);
        } catch (Exception e) {
            GallifreyMod.LOGGER.warn("Couldn't read the layout of TARDIS interior {}", name, e);
            return NONE;
        }
    }
}
