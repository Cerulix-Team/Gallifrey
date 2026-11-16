package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.item.custom.FezSlotAccess;
import com.cerulixteam.gallifrey.item.custom.SonicShadesSlotAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stores the Sonic Shades and fez slots on every player. They're tracked data, so the server sends
 * them to every client (other players can see what you're wearing), and they're saved with the
 * player. On death they drop like the rest of the inventory, unless keepInventory is on
 * (GallifreyAccessories copies them to the respawned player then).
 */
@Mixin(PlayerEntity.class)
public abstract class SonicShadesSlotMixin implements SonicShadesSlotAccess, FezSlotAccess {
    @Unique private static final TrackedData<ItemStack> GALLIFREY_SONIC_SHADES =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    @Unique private static final TrackedData<ItemStack> GALLIFREY_FEZ =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void gallifrey$registerAccessorySlots(CallbackInfo ci) {
        DataTracker tracker = ((Entity) (Object) this).getDataTracker();
        tracker.startTracking(GALLIFREY_SONIC_SHADES, ItemStack.EMPTY);
        tracker.startTracking(GALLIFREY_FEZ, ItemStack.EMPTY);
    }

    @Override
    public ItemStack gallifrey$getSonicShades() {
        return ((Entity) (Object) this).getDataTracker().get(GALLIFREY_SONIC_SHADES);
    }

    @Override
    public void gallifrey$setSonicShades(ItemStack stack) {
        // Always store a fresh copy: Minecraft only syncs tracked data when it's set to a new object.
        ((Entity) (Object) this).getDataTracker().set(GALLIFREY_SONIC_SHADES, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    @Override
    public ItemStack gallifrey$getFez() {
        return ((Entity) (Object) this).getDataTracker().get(GALLIFREY_FEZ);
    }

    @Override
    public void gallifrey$setFez(ItemStack stack) {
        ((Entity) (Object) this).getDataTracker().set(GALLIFREY_FEZ, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void gallifrey$saveAccessorySlots(NbtCompound nbt, CallbackInfo ci) {
        ItemStack shades = gallifrey$getSonicShades();
        if (!shades.isEmpty()) nbt.put("GallifreySonicShades", shades.writeNbt(new NbtCompound()));
        else nbt.remove("GallifreySonicShades");
        ItemStack fez = gallifrey$getFez();
        if (!fez.isEmpty()) nbt.put("GallifreyFez", fez.writeNbt(new NbtCompound()));
        else nbt.remove("GallifreyFez");
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void gallifrey$loadAccessorySlots(NbtCompound nbt, CallbackInfo ci) {
        gallifrey$setSonicShades(nbt.contains("GallifreySonicShades", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(nbt.getCompound("GallifreySonicShades")) : ItemStack.EMPTY);
        gallifrey$setFez(nbt.contains("GallifreyFez", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(nbt.getCompound("GallifreyFez")) : ItemStack.EMPTY);
    }

    /** Drop the worn shades and fez on death, like the rest of the inventory. */
    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void gallifrey$dropAccessorySlots(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;
        ItemStack shades = gallifrey$getSonicShades();
        if (!shades.isEmpty()) {
            self.dropItem(shades.copy(), true, false);
            gallifrey$setSonicShades(ItemStack.EMPTY);
        }
        ItemStack fez = gallifrey$getFez();
        if (!fez.isEmpty()) {
            self.dropItem(fez.copy(), true, false);
            gallifrey$setFez(ItemStack.EMPTY);
        }
    }
}
