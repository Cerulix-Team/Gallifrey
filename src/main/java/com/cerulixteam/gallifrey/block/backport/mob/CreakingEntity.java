package com.cerulixteam.gallifrey.block.backport.mob;

import com.cerulixteam.gallifrey.block.backport.BackportBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Creaking (1.21.4). Can't move while a player is looking at it. One spawned by a Creaking Heart can't be
 * hurt (hitting it makes the heart grow resin instead), crumbles at dawn, and dies when its heart is
 * broken. One from a spawn egg has no heart and dies in one hit.
 */
public class CreakingEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable private BlockPos heart;

    public CreakingEntity(EntityType<? extends CreakingEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    public void setHeart(@Nullable BlockPos heart) {
        this.heart = heart;
    }

    @Nullable
    public BlockPos getHeart() {
        return this.heart;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override public boolean canStart() { return !CreakingEntity.this.isWatched() && super.canStart(); }
            @Override public boolean shouldContinue() { return !CreakingEntity.this.isWatched() && super.shouldContinue(); }
        });
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8) {
            @Override public boolean canStart() { return !CreakingEntity.this.isWatched() && super.canStart(); }
        });
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /** Whether any player nearby is looking at it. */
    public boolean isWatched() {
        for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (player.isSpectator() || player.isCreative() || player.squaredDistanceTo(this) > 32 * 32) continue;
            Vec3d look = player.getRotationVec(1.0F).normalize();
            Vec3d to = this.getEyePos().subtract(player.getEyePos());
            double distance = to.length();
            if (distance > 0.1 && look.dotProduct(to.normalize()) > 1.0 - 0.025 * Math.max(1, 12 / distance) && player.canSee(this)) return true;
        }
        return false;
    }

    @Override
    public void travel(Vec3d input) {
        if (this.isWatched()) {
            this.getNavigation().stop();
            this.setVelocity(0, Math.min(0, this.getVelocity().y), 0);
            super.travel(Vec3d.ZERO);
            return;
        }
        super.travel(input);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getWorld() instanceof ServerWorld world) || this.heart == null || this.age % 20 != 0) return;
        BlockState state = world.getBlockState(this.heart);
        if (!state.isOf(com.cerulixteam.gallifrey.block.backport.BackportExtras.CREAKING_HEART)) {
            this.crumble(world);
        } else if (world.isDay()) {
            this.crumble(world);
        }
    }

    private void crumble(ServerWorld world) {
        world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, BackportBlocks.PALE_OAK_WOOD.getDefaultState()),
                this.getX(), this.getBodyY(0.5), this.getZ(), 40, 0.3, 0.8, 0.3, 0.1);
        this.playSound(com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_CREAKING_DEATH, 1.0F, 1.0F);
        this.discard();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.heart != null && !source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
            if (this.getWorld() instanceof ServerWorld world) {
                this.playSound(com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_CREAKING_HEART_HURT, 1.0F, 1.0F);
                this.growResin(world);
            }
            return false;
        }
        return super.damage(source, amount);
    }

    /** Hitting a heart-bound Creaking makes resin grow on a pale oak log near its heart. */
    private void growResin(ServerWorld world) {
        if (this.heart == null) return;
        for (int i = 0; i < 16; i++) {
            BlockPos log = this.heart.add(this.random.nextInt(5) - 2, this.random.nextInt(5) - 2, this.random.nextInt(5) - 2);
            if (!world.getBlockState(log).isOf(BackportBlocks.PALE_OAK_LOG)) continue;
            Direction side = Direction.random(this.random);
            BlockPos at = log.offset(side);
            if (world.isAir(at)) {
                BlockState clump = BackportBlocks.RESIN_CLUMP.getDefaultState()
                        .with(net.minecraft.block.MultifaceGrowthBlock.getProperty(side.getOpposite()), true);
                world.setBlockState(at, clump);
                return;
            }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_CREAKING_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_CREAKING_SWAY; }
    @Override protected SoundEvent getDeathSound() { return com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_CREAKING_DEATH; }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return this.heart == null && super.canImmediatelyDespawn(distanceSquared);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.heart != null) nbt.put("home_pos", NbtHelper.fromBlockPos(this.heart));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.heart = nbt.contains("home_pos") ? NbtHelper.toBlockPos(nbt.getCompound("home_pos")) : null;
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    /** Used by the Creaking Heart: spawns a creaking bound to it, if it doesn't have one. */
    public static void spawnFor(ServerWorld world, BlockPos heart) {
        if (!world.getEntitiesByClass(CreakingEntity.class, new net.minecraft.util.math.Box(heart).expand(32), c -> heart.equals(c.heart)).isEmpty()) return;
        for (int i = 0; i < 12; i++) {
            BlockPos at = heart.add(world.random.nextInt(17) - 8, world.random.nextInt(9) - 4, world.random.nextInt(17) - 8);
            if (world.isAir(at) && world.isAir(at.up()) && world.isAir(at.up(2)) && world.getBlockState(at.down()).isSolidBlock(world, at.down())) {
                CreakingEntity creaking = BackportMobs.CREAKING.create(world);
                if (creaking == null) return;
                creaking.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360, 0);
                creaking.setHeart(heart.toImmutable());
                creaking.setPersistent();
                world.spawnEntity(creaking);
                world.playSound(null, at, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_CREAKING_HEART_SPAWN, net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 1.0F);
                return;
            }
        }
    }
}
