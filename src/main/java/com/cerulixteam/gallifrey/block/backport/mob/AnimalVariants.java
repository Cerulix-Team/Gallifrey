package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Farm animal variants (1.21.5) and wolf variants (1.20.5). Cows, pigs and chickens are temperate (0),
 * warm (1) or cold (2), picked by the biome they spawn in; babies take a parent's. Wolves get one of
 * nine coats by biome, plus Patch. The value lives on PassiveEntity (mixin/backport/PassiveEntityVariantMixin).
 *
 * Patch (index 9) is Gallifrey's own coat, in memory of Blue's dog Patch: creamy white with tan ears and a tan
 * patch by his tail. Where no biome coat applies, a wolf is as likely to be Patch as the usual pale wolf.
 */
public final class AnimalVariants {
    private AnimalVariants() {}

    public static final int TEMPERATE = 0, WARM = 1, COLD = 2;
    public static final String[] FARM = {"temperate", "warm", "cold"};
    public static final String[] WOLVES = {"pale", "ashen", "black", "chestnut", "rusty", "snowy", "spotted", "striped", "woods", "patch"};
    public static final int PATCH = 9;

    /** Implemented on PassiveEntity by the mixin. */
    public interface Holder {
        int gallifrey$getVariant();

        void gallifrey$setVariant(int variant);
    }

    public static boolean hasFarmVariants(Entity entity) {
        return (entity instanceof CowEntity && !(entity instanceof MooshroomEntity)) || entity instanceof PigEntity || entity instanceof ChickenEntity;
    }

    public static boolean hasVariants(Entity entity) {
        return hasFarmVariants(entity) || entity instanceof WolfEntity;
    }

    public static int get(Entity entity) {
        return entity instanceof Holder holder ? Math.max(0, holder.gallifrey$getVariant()) : 0;
    }

    public static void set(Entity entity, int variant) {
        if (entity instanceof Holder holder) holder.gallifrey$setVariant(variant);
    }

    /** The variant a newly spawned animal gets where it stands. */
    public static int forBiome(Entity entity, WorldView world, BlockPos pos) {
        RegistryEntry<Biome> biome = world.getBiome(pos);
        if (entity instanceof WolfEntity) {
            int coat = wolfFor(biome);
            return coat == 0 && entity.getWorld().getRandom().nextBoolean() ? PATCH : coat;
        }
        if (biome.isIn(BiomeTags.IS_NETHER)) return WARM;
        if (biome.isIn(BiomeTags.IS_END)) return COLD;
        float temperature = biome.value().getTemperature();
        if (temperature >= 0.95F || biome.isIn(BiomeTags.IS_JUNGLE) || biome.matchesKey(BiomeKeys.MANGROVE_SWAMP) || biome.matchesKey(BiomeKeys.WARM_OCEAN)) return WARM;
        if (temperature < 0.3F || biome.isIn(BiomeTags.IS_TAIGA) || biome.matchesKey(BiomeKeys.GROVE) || biome.matchesKey(BiomeKeys.DEEP_DARK)) return COLD;
        return TEMPERATE;
    }

    private static int wolfFor(RegistryEntry<Biome> biome) {
        if (biome.matchesKey(BiomeKeys.SNOWY_TAIGA)) return 1;
        if (biome.matchesKey(BiomeKeys.OLD_GROWTH_PINE_TAIGA)) return 2;
        if (biome.matchesKey(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA)) return 3;
        if (biome.matchesKey(BiomeKeys.SPARSE_JUNGLE)) return 4;
        if (biome.matchesKey(BiomeKeys.GROVE)) return 5;
        if (biome.matchesKey(BiomeKeys.SAVANNA_PLATEAU)) return 6;
        if (biome.matchesKey(BiomeKeys.WOODED_BADLANDS)) return 7;
        if (biome.matchesKey(BiomeKeys.FOREST)) return 8;
        return 0;
    }
}
