package com.cerulixteam.gallifrey.entity.custom;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Pre-1.14 style villager: the old robe look, one of six jobs, and a fixed list of old-style
 * trades paid in Classic Emeralds. No beds, job blocks or gossip, just like the old ones.
 */
public class ClassicVillagerEntity extends MerchantEntity {

    /** Order matters: it is saved to NBT as a number. Texture names are the old 1.13 ones. */
    public enum Profession {
        FARMER("farmer"), LIBRARIAN("librarian"), PRIEST("priest"),
        BLACKSMITH("smith"), BUTCHER("butcher"), NITWIT("villager");

        public final String texture;
        Profession(String texture) { this.texture = texture; }

        public static Profession byId(int id) {
            Profession[] all = values();
            return all[Math.floorMod(id, all.length)];
        }
    }

    private static final TrackedData<Integer> PROFESSION =
            DataTracker.registerData(ClassicVillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public ClassicVillagerEntity(EntityType<? extends ClassicVillagerEntity> type, World world) {
        super(type, world);
        ((MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);
        this.getNavigation().setCanSwim(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    // ------------------------------------------------------------------ profession

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PROFESSION, 0);
    }

    public Profession getProfession() {
        return Profession.byId(this.dataTracker.get(PROFESSION));
    }

    public void setProfession(Profession profession) {
        this.dataTracker.set(PROFESSION, profession.ordinal());
        this.offers = null; // rebuild trades for the new job
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        // Village pieces save a "Profession" in the structure; anything else (spawn egg, /summon) gets a random job.
        if (entityNbt == null || !entityNbt.contains("Profession")) {
            this.setProfession(Profession.byId(this.random.nextInt(Profession.values().length)));
        }
        return super.initialize(world, difficulty, reason, entityData, entityNbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Profession", this.getProfession().ordinal());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Profession")) this.dataTracker.set(PROFESSION, nbt.getInt("Profession"));
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable(this.getType().getTranslationKey() + "." + this.getProfession().name().toLowerCase());
    }

    // ------------------------------------------------------------------ AI

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.6, 0.6));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 0.6));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(3, new LongDoorInteractGoal(this, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.35));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(11, new LookAroundGoal(this));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    // ------------------------------------------------------------------ trading

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) player.incrementStat(Stats.TALKED_TO_VILLAGER);

            if (this.getOffers().isEmpty()) {
                // Nitwits just shake their head, like the old ones did.
                this.setHeadRollingTimeLeft(40);
                if (!this.getWorld().isClient) this.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, this.getSoundPitch());
                return ActionResult.success(this.getWorld().isClient);
            }
            if (!this.getWorld().isClient) {
                this.setCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList list = this.getOffers();
        switch (this.getProfession()) {
            case FARMER -> {
                list.add(buy(Items.WHEAT, 18));
                list.add(buy(Items.CARROT, 15));
                list.add(sell(Items.BREAD, 6, 1));
                list.add(sell(GallifreyModItems.CLASSIC_APPLE, 5, 1));
                list.add(sell(Items.COOKIE, 8, 1));
            }
            case LIBRARIAN -> {
                list.add(buy(Items.PAPER, 24));
                list.add(buy(Items.BOOK, 4));
                list.add(sell(GallifreyModBlocks.CLASSIC_BOOKSHELF, 1, 3));
                list.add(sell(GallifreyModBlocks.CLASSIC_GLASS, 4, 1));
                list.add(sell(Items.COMPASS, 1, 4));
            }
            case PRIEST -> {
                list.add(buy(Items.ROTTEN_FLESH, 36));
                list.add(buy(Items.GOLD_INGOT, 8));
                list.add(sell(Items.REDSTONE, 2, 1));
                list.add(sell(GallifreyModBlocks.CLASSIC_NETHER_GLOWSTONE, 1, 2));
                list.add(sell(Items.ENDER_PEARL, 1, 4));
            }
            case BLACKSMITH -> {
                list.add(buy(GallifreyModItems.CLASSIC_COAL, 16));
                list.add(buy(GallifreyModItems.CLASSIC_IRON_INGOT, 7));
                list.add(sell(GallifreyModItems.CLASSIC_IRON_PICKAXE, 1, 3));
                list.add(sell(GallifreyModItems.CLASSIC_IRON_SWORD, 1, 5));
                list.add(sell(GallifreyModItems.CLASSIC_IRON_CHESTPLATE, 1, 9));
                list.add(sell(GallifreyModItems.CLASSIC_DIAMOND_SWORD, 1, 14));
            }
            case BUTCHER -> {
                list.add(buy(Items.PORKCHOP, 14));
                list.add(buy(Items.CHICKEN, 14));
                list.add(sell(Items.COOKED_PORKCHOP, 6, 1));
                list.add(sell(Items.COOKED_CHICKEN, 8, 1));
                list.add(sell(GallifreyModItems.CLASSIC_LEATHER_CHESTPLATE, 1, 4));
            }
            case NITWIT -> { /* no trades */ }
        }
    }

    /** Villager buys {@code count} of an item for 1 Classic Emerald. */
    private static TradeOffer buy(ItemConvertible item, int count) {
        return new TradeOffer(new ItemStack(item, count), new ItemStack(GallifreyModItems.CLASSIC_EMERALD), 16, 2, 0.05F);
    }

    /** Villager sells {@code count} of an item for {@code price} Classic Emeralds. */
    private static TradeOffer sell(ItemConvertible item, int count, int price) {
        return new TradeOffer(new ItemStack(GallifreyModItems.CLASSIC_EMERALD, price), new ItemStack(item, count), 12, 2, 0.05F);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            int xp = 3 + this.random.nextInt(4);
            this.getWorld().spawnEntity(new ExperienceOrbEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), xp));
        }
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    // ------------------------------------------------------------------ sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasCustomer() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }
}
