package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.mob.BackportMobs;
import com.cerulixteam.gallifrey.block.backport.mob.WolfArmorItem;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wolf Armor (1.20.5): worn by tamed wolves, soaks up their damage, comes off with shears. */
@Mixin(WolfEntity.class)
public abstract class WolfArmorMixin implements WolfArmorItem.Holder {
    @Unique
    private static final TrackedData<ItemStack> GALLIFREY_ARMOR = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Unique
    private WolfEntity gallifrey$self() {
        return (WolfEntity) (Object) this;
    }

    @Override
    public ItemStack gallifrey$getArmor() {
        return this.gallifrey$self().getDataTracker().get(GALLIFREY_ARMOR);
    }

    @Unique
    private void gallifrey$setArmor(ItemStack stack) {
        this.gallifrey$self().getDataTracker().set(GALLIFREY_ARMOR, stack);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void gallifrey$trackArmor(CallbackInfo ci) {
        this.gallifrey$self().getDataTracker().startTracking(GALLIFREY_ARMOR, ItemStack.EMPTY);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void gallifrey$writeArmor(NbtCompound nbt, CallbackInfo ci) {
        ItemStack armor = this.gallifrey$getArmor();
        if (!armor.isEmpty()) nbt.put("GallifreyBodyArmor", armor.writeNbt(new NbtCompound()));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void gallifrey$readArmor(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("GallifreyBodyArmor")) this.gallifrey$setArmor(ItemStack.fromNbt(nbt.getCompound("GallifreyBodyArmor")));
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void gallifrey$armorInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        WolfEntity wolf = this.gallifrey$self();
        ItemStack held = player.getStackInHand(hand);
        ItemStack armor = this.gallifrey$getArmor();
        if (!wolf.isTamed() || !wolf.isOwner(player) || wolf.isBaby()) return;
        if (held.isOf(BackportMobs.WOLF_ARMOR) && armor.isEmpty()) {
            if (!wolf.getWorld().isClient) {
                this.gallifrey$setArmor(held.copyWithCount(1));
                if (!player.getAbilities().creativeMode) held.decrement(1);
                wolf.getWorld().playSoundFromEntity(null, wolf, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
            cir.setReturnValue(ActionResult.success(wolf.getWorld().isClient));
        } else if (held.isOf(Items.SHEARS) && !armor.isEmpty()) {
            if (!wolf.getWorld().isClient) {
                this.gallifrey$setArmor(ItemStack.EMPTY);
                wolf.dropStack(armor);
                wolf.getWorld().playSoundFromEntity(null, wolf, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                held.damage(1, player, p -> p.sendToolBreakStatus(hand));
            }
            cir.setReturnValue(ActionResult.success(wolf.getWorld().isClient));
        } else if (held.isOf(BackportMobs.ARMADILLO_SCUTE) && !armor.isEmpty() && armor.isDamaged()) {
            if (!wolf.getWorld().isClient) {
                ItemStack repaired = armor.copy();
                repaired.setDamage(Math.max(0, armor.getDamage() - armor.getMaxDamage() / 8));
                this.gallifrey$setArmor(repaired);
                if (!player.getAbilities().creativeMode) held.decrement(1);
                wolf.getWorld().playSoundFromEntity(null, wolf, com.cerulixteam.gallifrey.block.backport.BackportSounds.ITEM_WOLF_ARMOR_REPAIR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
            cir.setReturnValue(ActionResult.success(wolf.getWorld().isClient));
        }
    }

    /** The armour takes the hit; whatever it can't soak (once it breaks) still hurts. */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float gallifrey$armorAbsorbs(float amount, DamageSource source) {
        WolfEntity wolf = this.gallifrey$self();
        ItemStack armor = this.gallifrey$getArmor();
        if (armor.isEmpty() || wolf.getWorld().isClient || amount <= 0.0F || source.isIn(DamageTypeTags.BYPASSES_ARMOR) || wolf.isInvulnerableTo(source)) {
            return amount;
        }
        int hit = (int) Math.ceil(amount);
        int left = armor.getMaxDamage() - armor.getDamage();
        ItemStack worn = armor.copy();
        if (hit >= left) {
            this.gallifrey$setArmor(ItemStack.EMPTY);
            wolf.getWorld().playSoundFromEntity(null, wolf, com.cerulixteam.gallifrey.block.backport.BackportSounds.ITEM_WOLF_ARMOR_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            return hit - left;
        }
        worn.setDamage(armor.getDamage() + hit);
        this.gallifrey$setArmor(worn);
        wolf.getWorld().playSoundFromEntity(null, wolf, com.cerulixteam.gallifrey.block.backport.BackportSounds.ITEM_WOLF_ARMOR_DAMAGE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        return 0.0F;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void gallifrey$dropArmor(DamageSource source, CallbackInfo ci) {
        ItemStack armor = this.gallifrey$getArmor();
        if (!armor.isEmpty() && !this.gallifrey$self().getWorld().isClient) {
            this.gallifrey$self().dropStack(armor);
            this.gallifrey$setArmor(ItemStack.EMPTY);
        }
    }
}
