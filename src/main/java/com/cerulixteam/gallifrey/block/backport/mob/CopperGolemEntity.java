package com.cerulixteam.gallifrey.block.backport.mob;

import com.cerulixteam.gallifrey.block.backport.CopperChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Copper Golem (1.21.9). Build it with a carved pumpkin on a copper block. It takes items out of Copper
 * Chests and sorts them into normal chests nearby (ones that already hold that item first, then empty
 * ones). It rusts over time; scrape it with an axe, wax it with honeycomb. Fully rusted, it sometimes
 * freezes like a statue until scraped. Lightning cleans it.
 */
public class CopperGolemEntity extends GolemEntity implements GeoEntity {
    private static final TrackedData<Integer> OXIDATION = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> WAXED = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> STATUE = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int RANGE = 16;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int rustTimer;

    public CopperGolemEntity(EntityType<? extends CopperGolemEntity> type, World world) {
        super(type, world);
        this.rustTimer = 12000 + this.random.nextInt(12000);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return GolemEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OXIDATION, 0);
        this.dataTracker.startTracking(WAXED, false);
        this.dataTracker.startTracking(STATUE, false);
    }

    public int getOxidation() { return this.dataTracker.get(OXIDATION); }
    public void setOxidation(int level) { this.dataTracker.set(OXIDATION, Math.max(0, Math.min(3, level))); }
    public boolean isStatue() { return this.dataTracker.get(STATUE); }
    public void setWaxed(boolean waxed) { this.dataTracker.set(WAXED, waxed); }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SortGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8) {
            @Override public boolean canStart() { return !CopperGolemEntity.this.isStatue() && super.canStart(); }
        });
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public void travel(Vec3d input) {
        super.travel(this.isStatue() ? Vec3d.ZERO : input);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient) return;
        if (!this.dataTracker.get(WAXED) && --this.rustTimer <= 0) {
            this.rustTimer = 12000 + this.random.nextInt(12000);
            if (this.getOxidation() < 3) this.setOxidation(this.getOxidation() + 1);
            else if (!this.isStatue() && this.random.nextInt(4) == 0) this.dataTracker.set(STATUE, true);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = this.getWorld();
        if (stack.getItem() instanceof AxeItem && (this.getOxidation() > 0 || this.dataTracker.get(WAXED) || this.isStatue())) {
            if (!world.isClient) {
                if (this.dataTracker.get(WAXED)) this.dataTracker.set(WAXED, false);
                else this.setOxidation(this.getOxidation() - 1);
                this.dataTracker.set(STATUE, false);
                this.playSound(SoundEvents.ITEM_AXE_SCRAPE, 1.0F, 1.0F);
                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.HONEYCOMB) && !this.dataTracker.get(WAXED)) {
            if (!world.isClient) {
                this.dataTracker.set(WAXED, true);
                this.playSound(SoundEvents.ITEM_HONEYCOMB_WAX_ON, 1.0F, 1.0F);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isEmpty() && !this.getMainHandStack().isEmpty()) {
            if (!world.isClient) {
                player.setStackInHand(hand, this.getMainHandStack());
                this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            return ActionResult.success(world.isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setOxidation(0);
        this.dataTracker.set(STATUE, false);
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.BLOCK_COPPER_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.BLOCK_COPPER_BREAK; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("oxidation", this.getOxidation());
        nbt.putBoolean("waxed", this.dataTracker.get(WAXED));
        nbt.putBoolean("statue", this.isStatue());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setOxidation(nbt.getInt("oxidation"));
        this.dataTracker.set(WAXED, nbt.getBoolean("waxed"));
        this.dataTracker.set(STATUE, nbt.getBoolean("statue"));
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    /** Fetch from a copper chest, deliver to a normal chest, repeat. */
    static class SortGoal extends Goal {
        private final CopperGolemEntity golem;
        @Nullable private BlockPos target;
        private int cooldown;

        SortGoal(CopperGolemEntity golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.golem.isStatue() || --this.cooldown > 0) return false;
            this.cooldown = 40;
            this.target = this.golem.getMainHandStack().isEmpty() ? this.findSource() : this.findDestination(this.golem.getMainHandStack());
            return this.target != null;
        }

        @Override
        public boolean shouldContinue() {
            return this.target != null && !this.golem.isStatue();
        }

        @Override
        public void start() {
            if (this.target != null) this.golem.getNavigation().startMovingTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.0);
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            if (this.golem.squaredDistanceTo(Vec3d.ofCenter(this.target)) > 4.0) {
                if (this.golem.getNavigation().isIdle()) this.start();
                return;
            }
            BlockEntity be = this.golem.getWorld().getBlockEntity(this.target);
            if (be instanceof Inventory inventory) {
                ItemStack held = this.golem.getMainHandStack();
                if (held.isEmpty()) {
                    for (int i = 0; i < inventory.size(); i++) {
                        ItemStack stack = inventory.getStack(i);
                        if (!stack.isEmpty()) {
                            this.golem.equipStack(EquipmentSlot.MAINHAND, inventory.removeStack(i, Math.min(16, stack.getCount())));
                            break;
                        }
                    }
                } else {
                    ItemStack left = net.minecraft.block.entity.HopperBlockEntity.transfer(null, inventory, held, null);
                    this.golem.equipStack(EquipmentSlot.MAINHAND, left);
                }
                inventory.markDirty();
                this.golem.playSound(SoundEvents.BLOCK_CHEST_OPEN, 0.5F, 1.4F);
            }
            this.target = null;
            this.golem.getNavigation().stop();
        }

        @Nullable
        private BlockPos findSource() {
            for (BlockPos pos : BlockPos.iterateOutwards(this.golem.getBlockPos(), RANGE, 4, RANGE)) {
                if (this.golem.getWorld().getBlockEntity(pos) instanceof CopperChestBlockEntity chest && !chest.isEmpty()) return pos.toImmutable();
            }
            return null;
        }

        @Nullable
        private BlockPos findDestination(ItemStack held) {
            BlockPos empty = null;
            for (BlockPos pos : BlockPos.iterateOutwards(this.golem.getBlockPos(), RANGE, 4, RANGE)) {
                BlockEntity be = this.golem.getWorld().getBlockEntity(pos);
                if (!(be instanceof ChestBlockEntity chest) || be instanceof CopperChestBlockEntity) continue;
                boolean hasRoom = false, hasSame = false;
                for (int i = 0; i < chest.size(); i++) {
                    ItemStack stack = chest.getStack(i);
                    if (stack.isEmpty()) hasRoom = true;
                    else if (ItemStack.canCombine(stack, held)) {
                        hasSame = true;
                        if (stack.getCount() < stack.getMaxCount()) hasRoom = true;
                    }
                }
                if (hasSame && hasRoom) return pos.toImmutable();
                if (empty == null && chest.isEmpty()) empty = pos.toImmutable();
            }
            return empty;
        }
    }
}
