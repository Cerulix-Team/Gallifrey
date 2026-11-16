package com.cerulixteam.gallifrey.block.backport.trial;

import com.cerulixteam.gallifrey.block.backport.BackportBlocks;
import com.cerulixteam.gallifrey.block.backport.BackportCombat;
import com.cerulixteam.gallifrey.block.backport.BackportEffect;
import com.cerulixteam.gallifrey.block.backport.WindChargeEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Vec3d;

/**
 * Trial Chambers (1.21): the Trial Spawner, the Vault, Trial Keys and Trial Omen. The structure itself is
 * data (data/gallifrey/worldgen/structure/trial_chambers.json and its templates), converted from 26.3.
 */
public final class BackportTrials {
    private BackportTrials() {}

    private static Identifier id(String name) {
        return new Identifier("gallifrey", name);
    }

    private static <B extends Block> B block(String name, B block, Rarity rarity) {
        Registry.register(Registries.BLOCK, id(name), block);
        Item item = Registry.register(Registries.ITEM, id(name), new BlockItem(block, new FabricItemSettings().rarity(rarity)));
        BackportBlocks.TAB.add(item);
        return block;
    }

    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, id(name), item);
        BackportBlocks.TAB.add(item);
        return item;
    }

    public static final TrialSpawnerBlock TRIAL_SPAWNER = block("trial_spawner", new TrialSpawnerBlock(FabricBlockSettings.copyOf(Blocks.SPAWNER)
            .mapColor(MapColor.STONE_GRAY).luminance(s -> s.get(TrialSpawnerBlock.STATE).light).strength(50.0F).sounds(com.cerulixteam.gallifrey.block.backport.BackportSoundGroups.TRIAL_SPAWNER)
            .nonOpaque().blockVision((s, w, p) -> false)), Rarity.COMMON);
    public static final VaultBlock VAULT = block("vault", new VaultBlock(FabricBlockSettings.copyOf(Blocks.SPAWNER)
            .mapColor(MapColor.STONE_GRAY).luminance(s -> s.get(VaultBlock.STATE).light).strength(50.0F).sounds(com.cerulixteam.gallifrey.block.backport.BackportSoundGroups.VAULT)
            .nonOpaque().blockVision((s, w, p) -> false)), Rarity.COMMON);

    /** The Gallifreyan Trial Chambers' vault: the same, in white and gold, opened with Gallifreyan Trial Keys. */
    public static final VaultBlock GALLIFREYAN_VAULT = block("gallifreyan_vault", new VaultBlock(FabricBlockSettings.copyOf(VAULT).mapColor(MapColor.GOLD)), Rarity.UNCOMMON);

    public static final BlockEntityType<TrialSpawnerBlockEntity> TRIAL_SPAWNER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("trial_spawner"),
            FabricBlockEntityTypeBuilder.create(TrialSpawnerBlockEntity::new, TRIAL_SPAWNER).build());
    public static final BlockEntityType<VaultBlockEntity> VAULT_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("vault"),
            FabricBlockEntityTypeBuilder.create(VaultBlockEntity::new, VAULT, GALLIFREYAN_VAULT).build());

    public static final Item TRIAL_KEY = item("trial_key", new Item(new FabricItemSettings()));
    public static final Item OMINOUS_TRIAL_KEY = item("ominous_trial_key", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)));

    public static final Item GALLIFREYAN_TRIAL_KEY = item("gallifreyan_trial_key", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item OMINOUS_GALLIFREYAN_TRIAL_KEY = item("ominous_gallifreyan_trial_key", new Item(new FabricItemSettings().rarity(Rarity.RARE)));

    public static final StatusEffect TRIAL_OMEN = Registry.register(Registries.STATUS_EFFECT, id("trial_omen"),
            new BackportEffect(StatusEffectCategory.NEUTRAL, 0x16A6A6));

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new TrialSpawnerConfig.Loader());
    }

    /**
     * Whether this player makes a Trial Spawner ominous. Bad Omen (from an Ominous Bottle) turns into Trial
     * Omen near a Trial Spawner, 15 minutes per level, as in 1.21.
     */
    static boolean takeOmen(PlayerEntity player) {
        if (player.hasStatusEffect(TRIAL_OMEN)) return true;
        StatusEffectInstance bad = player.getStatusEffect(StatusEffects.BAD_OMEN);
        if (bad == null) return false;
        player.removeStatusEffect(StatusEffects.BAD_OMEN);
        player.addStatusEffect(new StatusEffectInstance(TRIAL_OMEN, 18000 * (bad.getAmplifier() + 1), 0, false, false, true));
        player.getWorld().playSound(null, player.getBlockPos(), com.cerulixteam.gallifrey.block.backport.BackportSounds.EVENT_MOB_EFFECT_TRIAL_OMEN, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /** One of an ominous Trial Spawner's drops: potions smash, arrows and wind charges fly down, anything else falls. */
    static void dropOminousItem(ServerWorld world, Vec3d at, ItemStack stack) {
        if (stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
            PotionEntity potion = new PotionEntity(world, at.x, at.y, at.z);
            potion.setItem(stack);
            potion.setVelocity(0, -0.2, 0);
            world.spawnEntity(potion);
        } else if (stack.isOf(Items.ARROW) || stack.isOf(Items.TIPPED_ARROW)) {
            ArrowEntity arrow = new ArrowEntity(world, at.x, at.y, at.z);
            if (stack.isOf(Items.TIPPED_ARROW)) arrow.initFromStack(stack);
            arrow.pickupType = net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission.DISALLOWED;
            arrow.setVelocity(0, -1.0, 0);
            world.spawnEntity(arrow);
        } else if (stack.isOf(BackportCombat.WIND_CHARGE)) {
            WindChargeEntity charge = new WindChargeEntity(BackportCombat.WIND_CHARGE_ENTITY, world);
            charge.refreshPositionAndAngles(at.x, at.y, at.z, 0, 0);
            charge.setVelocity(0, -0.5, 0);
            world.spawnEntity(charge);
        } else {
            world.spawnEntity(new ItemEntity(world, at.x, at.y, at.z, stack));
        }
    }
}
