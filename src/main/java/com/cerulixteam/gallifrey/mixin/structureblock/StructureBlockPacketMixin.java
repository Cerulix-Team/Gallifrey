package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla sends a structure block's offset and size as bytes, clamped to 48. The full values go on the end
 * of the packet as ints and replace them on arrival.
 */
@Mixin(UpdateStructureBlockC2SPacket.class)
public abstract class StructureBlockPacketMixin {
    @Shadow @Final @Mutable private BlockPos offset;
    @Shadow @Final @Mutable private Vec3i size;

    @Inject(method = "write", at = @At("TAIL"))
    private void gallifrey$writeFull(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeInt(this.offset.getX());
        buf.writeInt(this.offset.getY());
        buf.writeInt(this.offset.getZ());
        buf.writeInt(this.size.getX());
        buf.writeInt(this.size.getY());
        buf.writeInt(this.size.getZ());
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void gallifrey$readFull(PacketByteBuf buf, CallbackInfo ci) {
        if (buf.readableBytes() < 24) return;
        int l = HugeStructureBlocks.LIMIT;
        this.offset = new BlockPos(MathHelper.clamp(buf.readInt(), -l, l), MathHelper.clamp(buf.readInt(), -l, l), MathHelper.clamp(buf.readInt(), -l, l));
        this.size = new Vec3i(MathHelper.clamp(buf.readInt(), 0, l), MathHelper.clamp(buf.readInt(), 0, l), MathHelper.clamp(buf.readInt(), 0, l));
    }
}
