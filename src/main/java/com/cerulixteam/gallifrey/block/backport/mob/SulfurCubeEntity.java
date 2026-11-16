package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

/**
 * Sulfur Cube (26.2): a jelly cube from the Sulfur Caves. A big one swallows a block (dropped near it, fed
 * to it, or from a dispenser) and then stops moving and turns into a ball whose physics depend on the
 * block: bouncy wood, heavy metal, floaty wool, slippery ice, sticky honey, and so on. Shears get the block
 * back out. TNT inside can be lit (flint and steel, fire, or an explosion). Magma hurts what touches it.
 * Feed a small cube slime balls to grow it.
 */
public class SulfurCubeEntity extends MobEntity implements GeoEntity {
    private static final TrackedData<Boolean> LARGE = DataTracker.registerData(SulfurCubeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<BlockState>> BLOCK = DataTracker.registerData(SulfurCubeEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int hopTimer;
    private int fuse = -1;
    private int slimeBalls;
    private double lastFallSpeed;

    /** A physics preset: knockback taken, bounce, ground friction, gravity scale, and whether it floats. */
    public enum Archetype {
        REGULAR(1.0, 0.5, 0.9, 1.0, true), BOUNCY(1.4, 0.85, 0.92, 1.0, true), SLOW_FLAT(0.4, 0.1, 0.6, 1.3, false),
        FAST_FLAT(1.2, 0.15, 0.97, 1.0, true), LIGHT(1.3, 0.5, 0.9, 0.3, true), FAST_SLIDING(1.0, 0.1, 0.995, 1.0, false),
        SLOW_SLIDING(0.6, 0.1, 0.97, 1.0, false), HIGH_RESISTANCE(0.1, 0.05, 0.5, 1.0, false), STICKY(0.6, 0.0, 0.2, 1.0, false),
        EXPLOSIVE(1.0, 0.5, 0.9, 1.0, true), HOT(1.0, 0.5, 0.9, 1.0, true), SLOW_BOUNCY(0.5, 0.6, 0.8, 1.0, true);

        final double knockback, bounce, friction, gravity;
        final boolean floats;

        Archetype(double knockback, double bounce, double friction, double gravity, boolean floats) {
            this.knockback = knockback;
            this.bounce = bounce;
            this.friction = friction;
            this.gravity = gravity;
            this.floats = floats;
        }

        static Archetype of(BlockState state) {
            Block b = state.getBlock();
            if (b == Blocks.TNT) return EXPLOSIVE;
            if (b == Blocks.MAGMA_BLOCK) return HOT;
            if (b == Blocks.HONEYCOMB_BLOCK) return STICKY;
            if (b == Blocks.SOUL_SAND || b == Blocks.SOUL_SOIL) return HIGH_RESISTANCE;
            if (state.isIn(BlockTags.WOOL)) return LIGHT;
            if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS) || b == Blocks.BAMBOO_BLOCK) return BOUNCY;
            if (b == Blocks.ICE || b == Blocks.PACKED_ICE || b == Blocks.BLUE_ICE || b == Blocks.SNOW_BLOCK) return FAST_SLIDING;
            if (b == Blocks.MYCELIUM || b == Blocks.NETHER_WART_BLOCK || b == Blocks.SHROOMLIGHT || b == Blocks.RED_MUSHROOM_BLOCK
                    || b == Blocks.BROWN_MUSHROOM_BLOCK || b == Blocks.MUSHROOM_STEM) return SLOW_SLIDING;
            if (b == Blocks.IRON_BLOCK || b == Blocks.GOLD_BLOCK || b == Blocks.NETHERITE_BLOCK || b == Blocks.COPPER_BLOCK) return SLOW_FLAT;
            if (b == Blocks.PUMPKIN || b == Blocks.MELON || b == Blocks.MOSS_BLOCK || b == Blocks.DRIED_KELP_BLOCK || state.isIn(BlockTags.CORAL_BLOCKS)) return FAST_FLAT;
            if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) || state.isIn(BlockTags.STONE_BRICKS) || b == Blocks.BRICKS
                    || state.isIn(BlockTags.COAL_ORES) || state.isIn(BlockTags.IRON_ORES)) return SLOW_BOUNCY;
            return REGULAR;
        }
    }

    public SulfurCubeEntity(EntityType<? extends SulfurCubeEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(LARGE, true);
        this.dataTracker.startTracking(BLOCK, Optional.empty());
    }

    public boolean isLarge() { return this.dataTracker.get(LARGE); }
    public Optional<BlockState> getBlock() { return this.dataTracker.get(BLOCK); }

    public void setLarge(boolean large) {
        this.dataTracker.set(LARGE, large);
        this.calculateDimensions();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(this.isLarge() ? 1.0F : 0.5F);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (LARGE.equals(data)) this.calculateDimensions();
        super.onTrackedDataSet(data);
    }

    private Archetype archetype() {
        return this.getBlock().map(Archetype::of).orElse(Archetype.REGULAR);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;
        Vec3d v = this.getVelocity();
        if (this.getBlock().isPresent()) {
            Archetype a = this.archetype();
            if (this.isOnGround()) {
                if (this.lastFallSpeed < -0.2 && a.bounce > 0) {
                    this.setVelocity(v.x, -this.lastFallSpeed * a.bounce, v.z);
                } else {
                    this.setVelocity(v.x * a.friction, v.y, v.z * a.friction);
                }
            } else {
                this.addVelocity(0, 0.08 * (1.0 - a.gravity), 0);
            }
            if (this.isTouchingWater() && a.floats) this.addVelocity(0, 0.05, 0);
            if (a == Archetype.HOT && this.age % 20 == 0) {
                for (LivingEntity touching : this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(0.1), e -> e != this)) {
                    touching.damage(this.getDamageSources().hotFloor(), 1.0F);
                }
            }
            if (a == Archetype.EXPLOSIVE && this.fuse >= 0 && --this.fuse <= 0) {
                this.fuse = -1;
                this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.5), this.getZ(), 4.0F, World.ExplosionSourceType.TNT);
                this.dataTracker.set(BLOCK, Optional.empty());
            }
        } else {
            // Empty cubes hop about like slimes, and big ones swallow blocks dropped next to them.
            if (this.isOnGround() && --this.hopTimer <= 0) {
                this.hopTimer = 20 + this.random.nextInt(40);
                float yaw = this.random.nextFloat() * 360;
                Vec3d dir = Vec3d.fromPolar(0, yaw).multiply(0.25);
                this.setVelocity(dir.x, 0.42, dir.z);
                this.setYaw(yaw);
                this.playSound(SoundEvents.ENTITY_SLIME_JUMP_SMALL, 0.6F, 0.8F);
            }
            if (this.isLarge()) {
                for (ItemEntity item : this.getWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.5), e -> e.getStack().getItem() instanceof BlockItem)) {
                    ItemStack stack = item.getStack();
                    this.swallow(((BlockItem) stack.getItem()).getBlock().getDefaultState());
                    stack.decrement(1);
                    break;
                }
            }
        }
        this.lastFallSpeed = this.getVelocity().y;
    }

    public void swallow(BlockState state) {
        this.getBlock().ifPresent(old -> this.dropItem(old.getBlock()));
        this.dataTracker.set(BLOCK, Optional.of(state));
        this.fuse = -1;
        this.playSound(SoundEvents.ENTITY_SLIME_SQUISH, 1.0F, 0.7F);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = this.getWorld();
        if (stack.isOf(Items.BUCKET) && !this.isLarge() && this.isAlive()) {
            if (!world.isClient) {
                this.playSound(SoundEvents.ITEM_BUCKET_FILL_AXOLOTL, 1.0F, 1.0F);
                ItemStack bucket = SulfurCubeBucketItem.of(this);
                ItemStack result = net.minecraft.item.ItemUsage.exchangeStack(stack, player, bucket, false);
                player.setStackInHand(hand, result);
                this.discard();
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.SHEARS) && this.getBlock().isPresent()) {
            if (!world.isClient) {
                this.dropItem(this.getBlock().get().getBlock());
                this.dataTracker.set(BLOCK, Optional.empty());
                this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.FLINT_AND_STEEL) && this.archetype() == Archetype.EXPLOSIVE && this.fuse < 0) {
            if (!world.isClient) {
                this.fuse = 120;
                this.playSound(SoundEvents.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.SLIME_BALL) && !this.isLarge()) {
            if (!world.isClient) {
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                if (++this.slimeBalls >= 4) this.setLarge(true);
                this.playSound(SoundEvents.ENTITY_SLIME_SQUISH_SMALL, 1.0F, 1.2F);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.getItem() instanceof BlockItem block && this.isLarge()) {
            if (!world.isClient) {
                this.swallow(block.getBlock().getDefaultState());
                if (!player.getAbilities().creativeMode) stack.decrement(1);
            }
            return ActionResult.success(world.isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getBlock().isPresent()) {
            if (this.archetype() == Archetype.EXPLOSIVE && (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)
                    || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION)) && this.fuse < 0) {
                this.fuse = source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION) ? 15 + this.random.nextInt(45) : 120;
                return false;
            }
            // With a block inside, hits knock it about instead of hurting it.
            Entity attacker = source.getAttacker();
            if (attacker != null) {
                Vec3d push = this.getPos().subtract(attacker.getPos()).normalize().multiply(0.6 * this.archetype().knockback);
                this.addVelocity(push.x, 0.3 * this.archetype().knockback, push.z);
                this.velocityModified = true;
                this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 0.8F);
            }
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_SLIME_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_SLIME_DEATH; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("large", this.isLarge());
        this.getBlock().ifPresent(state -> nbt.put("block", NbtHelper.fromBlockState(state)));
        nbt.putInt("slime_balls", this.slimeBalls);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setLarge(!nbt.contains("large") || nbt.getBoolean("large"));
        this.dataTracker.set(BLOCK, nbt.contains("block")
                ? Optional.of(NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("block")))
                : Optional.empty());
        this.slimeBalls = nbt.getInt("slime_balls");
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
