package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import com.cerulixteam.gallifrey.tardis.TardisExteriorCatalog;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/** The in-room police-box door used as the reliable TARDIS exit control. */
public class TardisInteriorDoorBlockEntity extends BlockEntity {
    private UUID tardisId;
    private String exteriorStyle = "policebox";
    private boolean powered = true;
    /** Follows the exterior doors (TardisFlight.setDoors). */
    private boolean open;

    public TardisInteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(GallifreyModBlockEntities.TARDIS_INTERIOR_DOOR, pos, state);
    }

    public UUID getTardisId() { return tardisId; }

    public void setTardisId(UUID id) {
        tardisId = id;
        markDirty();
    }

    public boolean isPowered() { return powered; }

    public void setPowered(boolean powered) {
        this.powered = powered;
        markDirty();
        if (world != null) world.updateListeners(pos, getCachedState(), getCachedState(), 3);
    }

    public boolean isOpen() { return open; }

    public void setOpen(boolean open) {
        this.open = open;
        markDirty();
        if (world != null) world.updateListeners(pos, getCachedState(), getCachedState(), 3);
    }

    public String getExteriorStyle() {
        return TardisExteriorCatalog.get(exteriorStyle).id();
    }

    public void setExteriorStyle(String style) {
        exteriorStyle = TardisExteriorCatalog.get(style).id();
        markDirty();
        if (world != null) world.updateListeners(pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (tardisId != null) nbt.putUuid("TardisId", tardisId);
        nbt.putString("ExteriorStyle", exteriorStyle);
        nbt.putBoolean("Powered", powered);
        nbt.putBoolean("Open", open);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.containsUuid("TardisId")) tardisId = nbt.getUuid("TardisId");
        exteriorStyle = TardisExteriorCatalog.get(nbt.getString("ExteriorStyle")).id();
        powered = !nbt.contains("Powered") || nbt.getBoolean("Powered");
        open = nbt.getBoolean("Open");
    }

    @Override
    public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt() {
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public net.minecraft.network.packet.Packet<net.minecraft.network.listener.ClientPlayPacketListener> toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }
}
