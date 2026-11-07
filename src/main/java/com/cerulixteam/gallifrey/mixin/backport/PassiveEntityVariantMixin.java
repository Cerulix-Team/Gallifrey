package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stores the cow, pig, chicken and wolf variants (see AnimalVariants) and picks one by biome at spawn. */
@Mixin(PassiveEntity.class)
public abstract class PassiveEntityVariantMixin implements AnimalVariants.Holder {
    @Unique
    private static final TrackedData<Integer> GALLIFREY_VARIANT = DataTracker.registerData(PassiveEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Override
    public int gallifrey$getVariant() {
        return ((PassiveEntity) (Object) this).getDataTracker().get(GALLIFREY_VARIANT);
    }

    @Override
    public void gallifrey$setVariant(int variant) {
        ((PassiveEntity) (Object) this).getDataTracker().set(GALLIFREY_VARIANT, variant);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void gallifrey$trackVariant(CallbackInfo ci) {
        ((PassiveEntity) (Object) this).getDataTracker().startTracking(GALLIFREY_VARIANT, 0);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void gallifrey$writeVariant(NbtCompound nbt, CallbackInfo ci) {
        if (AnimalVariants.hasVariants((PassiveEntity) (Object) this)) nbt.putInt("GallifreyVariant", this.gallifrey$getVariant());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void gallifrey$readVariant(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("GallifreyVariant")) this.gallifrey$setVariant(nbt.getInt("GallifreyVariant"));
    }

    @Inject(method = "initialize", at = @At("TAIL"))
    private void gallifrey$pickVariant(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason, EntityData data, NbtCompound nbt,
                                       CallbackInfoReturnable<EntityData> cir) {
        PassiveEntity self = (PassiveEntity) (Object) this;
        if (AnimalVariants.hasVariants(self) && reason != SpawnReason.BREEDING) {
            this.gallifrey$setVariant(AnimalVariants.forBiome(self, world, self.getBlockPos()));
        }
    }
}
