package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.item.custom.AccessorySlotIndices;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In creative mode the client tells the server about inventory changes slot by slot, and vanilla
 * only accepts slots 1 to 45. That ignored the Sonic Shades slot, so in creative the shades looked
 * equipped but the server never saw them. This accepts the Sonic Shades and fez slots too.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class CreativeAccessorySlotsMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCreativeInventoryAction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void gallifrey$creativeAccessorySlots(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        if (!this.player.interactionManager.isCreative()) return;
        AccessorySlotIndices indices = (AccessorySlotIndices) this.player.playerScreenHandler;
        int index = packet.getSlot();
        // -1 is vanilla's "drop this item" (and means "no slot" here, with Trinkets): leave it to vanilla.
        if (index < 0) return;
        if (index != indices.gallifrey$shadesSlotIndex() && index != indices.gallifrey$fezSlotIndex()) return;

        ci.cancel();
        ItemStack stack = packet.getItemStack();
        Slot slot = this.player.playerScreenHandler.getSlot(index);
        if (stack.isEmpty() || slot.canInsert(stack)) {
            slot.setStack(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
            this.player.playerScreenHandler.sendContentUpdates();
        }
    }
}
