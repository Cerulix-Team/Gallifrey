package com.cerulixteam.gallifrey.block.backport;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

import static com.cerulixteam.gallifrey.block.backport.BackportBlocks.*;

/**
 * Sets up the backported blocks (see {@link BackportBlocks}). Call {@link #register()} from
 * GallifreyMod.onInitialize, after GallifreyModBlocks.register().
 */
public final class Backport {
    private Backport() {}

    public static void register() {
        BackportSounds.register();
        BackportBlocks.register();
        Backport26.register();
        BackportCombat.register();
        BackportExtras.register();
        com.cerulixteam.gallifrey.block.backport.mob.BackportMobs.register();
        com.cerulixteam.gallifrey.block.backport.trial.BackportTrials.register();
        com.cerulixteam.gallifrey.block.backport.BackportBiomes.register();
        BackportItems.registerBoat();
        Backport26.registerBoat();
        register26();

        StrippableBlockRegistry.register(PALE_OAK_LOG, STRIPPED_PALE_OAK_LOG);
        StrippableBlockRegistry.register(PALE_OAK_WOOD, STRIPPED_PALE_OAK_WOOD);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        for (Block block : new Block[]{PALE_OAK_LOG, PALE_OAK_WOOD, STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD}) flammable.add(block, 5, 5);
        for (Block block : new Block[]{PALE_OAK_PLANKS, PALE_OAK_STAIRS, PALE_OAK_SLAB, PALE_OAK_FENCE, PALE_OAK_FENCE_GATE}) flammable.add(block, 5, 20);
        flammable.add(PALE_OAK_LEAVES, 30, 60);
        for (Block block : new Block[]{PALE_MOSS_BLOCK, PALE_MOSS_CARPET, PALE_HANGING_MOSS, LEAF_LITTER, WILDFLOWERS, BUSH, FIREFLY_BUSH,
                SHORT_DRY_GRASS, TALL_DRY_GRASS, OPEN_EYEBLOSSOM, CLOSED_EYEBLOSSOM, CACTUS_FLOWER}) flammable.add(block, 60, 100);

        CompostingChanceRegistry compost = CompostingChanceRegistry.INSTANCE;
        compost.add(PALE_OAK_LEAVES, 0.3F);
        compost.add(PALE_OAK_SAPLING, 0.3F);
        compost.add(LEAF_LITTER, 0.3F);
        compost.add(SHORT_DRY_GRASS, 0.3F);
        compost.add(TALL_DRY_GRASS, 0.3F);
        compost.add(PALE_HANGING_MOSS, 0.3F);
        compost.add(PALE_MOSS_CARPET, 0.3F);
        compost.add(BUSH, 0.3F);
        compost.add(CACTUS_FLOWER, 0.3F);
        compost.add(WILDFLOWERS, 0.3F);
        compost.add(FIREFLY_BUSH, 0.3F);
        compost.add(OPEN_EYEBLOSSOM, 0.65F);
        compost.add(CLOSED_EYEBLOSSOM, 0.65F);
        compost.add(PALE_MOSS_BLOCK, 0.65F);

        // Tags (mining tools, walls, logs and so on) are in this always-on built-in data pack, so they
        // merge with the datagen tag files instead of clashing with them.
        FabricLoader.getInstance().getModContainer("gallifrey").ifPresent(mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("gallifrey", "backported"), mod,
                        Text.translatable("pack.gallifrey.backported"), ResourcePackActivationType.ALWAYS_ENABLED));

        // Where the new plants and trees grow, so they can be found in survival.
        add("backport_pale_oak", BiomeKeys.DARK_FOREST);
        add("backport_pale_moss_patch", BiomeKeys.DARK_FOREST, BackportBiomes.PALE_GARDEN);
        add("backport_eyeblossom_patch", BiomeKeys.DARK_FOREST, BackportBiomes.PALE_GARDEN);
        add("backport_leaf_litter", BiomeKeys.FOREST, BackportBiomes.DAPPLED_FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.DARK_FOREST, BiomeKeys.WINDSWEPT_FOREST);
        add("backport_wildflowers", BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.MEADOW);
        add("backport_bush", BiomeKeys.PLAINS, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.WINDSWEPT_HILLS);
        add("backport_firefly_bush", BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP, BackportBiomes.PALE_GARDEN);
        add("backport_dry_grass", BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS, BiomeKeys.ERODED_BADLANDS);
        add("backport_cactus_flower", BiomeKeys.DESERT, BiomeKeys.BADLANDS);
    }

    /** The parts of the 26.x blocks that need more than registering. */
    private static void register26() {
        StrippableBlockRegistry.register(Backport26.POPLAR_LOG, Backport26.STRIPPED_POPLAR_LOG);
        StrippableBlockRegistry.register(Backport26.POPLAR_WOOD, Backport26.STRIPPED_POPLAR_WOOD);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        for (Block block : new Block[]{Backport26.POPLAR_LOG, Backport26.POPLAR_WOOD, Backport26.STRIPPED_POPLAR_LOG, Backport26.STRIPPED_POPLAR_WOOD}) flammable.add(block, 5, 5);
        for (Block block : new Block[]{Backport26.POPLAR_PLANKS, Backport26.POPLAR_STAIRS, Backport26.POPLAR_SLAB, Backport26.POPLAR_FENCE, Backport26.POPLAR_FENCE_GATE}) flammable.add(block, 5, 20);
        for (Block block : new Block[]{Backport26.RED_POPLAR_LEAVES, Backport26.ORANGE_POPLAR_LEAVES, Backport26.YELLOW_POPLAR_LEAVES}) flammable.add(block, 30, 60);
        for (Block block : Backport26.WOOL_SLABS) flammable.add(block, 30, 60);
        for (Block block : Backport26.WOOL_STAIRS) flammable.add(block, 30, 60);
        flammable.add(Backport26.RED_SHRUB, 60, 100);
        flammable.add(Backport26.GOLDEN_DANDELION, 60, 100);
        flammable.add(Backport26.STRAW_BED, 60, 20);

        CompostingChanceRegistry compost = CompostingChanceRegistry.INSTANCE;
        for (Block block : new Block[]{Backport26.RED_POPLAR_LEAVES, Backport26.ORANGE_POPLAR_LEAVES, Backport26.YELLOW_POPLAR_LEAVES, Backport26.POPLAR_SAPLING, Backport26.RED_SHRUB}) compost.add(block, 0.3F);
        compost.add(Backport26.SHELF_MUSHROOM, 0.65F);
        compost.add(Backport26.GOLDEN_DANDELION, 0.65F);

        // Straw Beds: never set your spawn, and fall apart once you get up.
        net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, pos) ->
                !(player.getWorld().getBlockState(pos).getBlock() instanceof StrawBedBlock));
        net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.STOP_SLEEPING.register((entity, pos) ->
                StrawBedBlock.used(entity.getWorld(), pos));

        // Golden Dandelion: feed it to a baby animal to stop it growing up; feed it another to let it grow again.
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            net.minecraft.item.ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Backport26.GOLDEN_DANDELION.asItem()) || !(entity instanceof net.minecraft.entity.passive.PassiveEntity baby) || !baby.isBaby()) {
                return net.minecraft.util.ActionResult.PASS;
            }
            if (!world.isClient) {
                boolean stay = !baby.getCommandTags().contains(STAY_BABY_TAG);
                if (stay) baby.addCommandTag(STAY_BABY_TAG);
                else baby.removeScoreboardTag(STAY_BABY_TAG);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                ((net.minecraft.server.world.ServerWorld) world).spawnParticles(stay ? net.minecraft.particle.ParticleTypes.WAX_ON : net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER,
                        baby.getX(), baby.getBodyY(0.6), baby.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                world.playSound(null, baby.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EAT, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0F, 1.2F);
            }
            return net.minecraft.util.ActionResult.success(world.isClient);
        });

        // Worldgen: poplars, red shrubs and sulfur/cinnabar (there's no Dappled Forest or Sulfur Caves biome in 1.20.1).
        add("backport_poplars", BiomeKeys.FLOWER_FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.MEADOW);
        add("backport_red_shrub", BackportBiomes.DAPPLED_FOREST, BiomeKeys.FLOWER_FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("gallifrey", "backport_ore_sulfur")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("gallifrey", "backport_ore_cinnabar")));
    }

    /** Babies with this tag don't grow up (see PassiveEntityGrowthMixin). */
    public static final String STAY_BABY_TAG = "gallifrey.stay_baby";

    @SafeVarargs
    private static void add(String feature, RegistryKey<net.minecraft.world.biome.Biome>... biomes) {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(biomes), GenerationStep.Feature.VEGETAL_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("gallifrey", feature)));
    }

    /** Everything in the Backported section of the Gallifrey tab, in order. */
    public static List<ItemConvertible> tabItems() {
        List<ItemConvertible> items = new ArrayList<>(TAB);
        // Put the extra items next to their blocks.
        insertAfter(items, COPPER_LANTERN[7].asItem(), BackportItems.COPPER_TORCH, BackportItems.COPPER_NUGGET);
        insertAfter(items, PALE_OAK_BUTTON.asItem(), BackportItems.PALE_OAK_SIGN, BackportItems.PALE_OAK_HANGING_SIGN,
                BackportItems.PALE_OAK_BOAT, BackportItems.PALE_OAK_CHEST_BOAT);
        insertAfter(items, RESIN_BLOCK.asItem(), BackportItems.RESIN_BRICK);
        insertAfter(items, Backport26.POPLAR_HANGING_SIGN_ITEM, Backport26.POPLAR_BOAT, Backport26.POPLAR_CHEST_BOAT);
        return items;
    }

    public static ItemStack icon() {
        return new ItemStack(TUFF_BRICKS);
    }

    private static void insertAfter(List<ItemConvertible> items, ItemConvertible after, ItemConvertible... add) {
        int at = items.indexOf(after);
        if (at < 0) at = items.size() - 1;
        items.addAll(at + 1, List.of(add));
    }
}
