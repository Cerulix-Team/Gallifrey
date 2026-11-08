package com.cerulixteam.gallifrey.block.backport;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * Weapons, tools and armour from 1.21 to 1.21.11: the Mace and its enchantments, Wind Charges, Breeze
 * Rods, Spears and Lunge, and copper tools and armour (1.21.9). See {@link BackportBlocks}.
 *
 * Breeze Rods and Heavy Cores come from Breezes and Trial Chambers in vanilla, which aren't backported,
 * so here they have Gallifrey-only crafting recipes.
 */
public final class BackportCombat {
    private BackportCombat() {}

    private static final String MOD_ID = "gallifrey";

    // =========================================================
    // COPPER (1.21.9)
    // =========================================================

    public static final ToolMaterial COPPER = new ToolMaterial() {
        @Override public int getDurability() { return 190; }
        @Override public float getMiningSpeedMultiplier() { return 5.0F; }
        @Override public float getAttackDamage() { return 1.0F; }
        @Override public int getMiningLevel() { return 1; }
        @Override public int getEnchantability() { return 13; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.COPPER_INGOT); }
    };

    /** Copper armour. The textures are at minecraft:textures/models/armor/gallifrey_copper_layer_1/2.png, where 1.20.1 looks for them. */
    public static final ArmorMaterial COPPER_ARMOR = new ArmorMaterial() {
        private final int[] protection = {1, 3, 4, 2}; // boots, leggings, chestplate, helmet
        @Override public int getDurability(ArmorItem.Type type) { return new int[]{13, 15, 16, 11}[type.getEquipmentSlot().getEntitySlotId()] * 11; }
        @Override public int getProtection(ArmorItem.Type type) { return this.protection[type.getEquipmentSlot().getEntitySlotId()]; }
        @Override public int getEnchantability() { return 8; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_CHAIN; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.COPPER_INGOT); }
        @Override public String getName() { return "gallifrey_copper"; }
        @Override public float getToughness() { return 0.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };

    public static final Item COPPER_SWORD = item("copper_sword", new SwordItem(COPPER, 3, -2.4F, new FabricItemSettings()));
    public static final Item COPPER_SHOVEL = item("copper_shovel", new ShovelItem(COPPER, 1.5F, -3.0F, new FabricItemSettings()));
    public static final Item COPPER_PICKAXE = item("copper_pickaxe", new PickaxeItem(COPPER, 1, -2.8F, new FabricItemSettings()));
    public static final Item COPPER_AXE = item("copper_axe", new AxeItem(COPPER, 7.0F, -3.2F, new FabricItemSettings()));
    public static final Item COPPER_HOE = item("copper_hoe", new HoeItem(COPPER, -1, -2.0F, new FabricItemSettings()));
    public static final Item COPPER_HELMET = item("copper_helmet", new ArmorItem(COPPER_ARMOR, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item COPPER_CHESTPLATE = item("copper_chestplate", new ArmorItem(COPPER_ARMOR, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item COPPER_LEGGINGS = item("copper_leggings", new ArmorItem(COPPER_ARMOR, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item COPPER_BOOTS = item("copper_boots", new ArmorItem(COPPER_ARMOR, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // =========================================================
    // SPEARS (1.21.11): jab damage, attack speed, charge strength;
    // timings(jab length, lowering time, dismount, knockback and damage windows) in seconds, from 26.3
    // =========================================================

    public static final Item WOODEN_SPEAR = item("wooden_spear", new SpearItem(ToolMaterials.WOOD, 1, 1.54F, 0.7F, new FabricItemSettings()).timings(0.65F, 0.75F, 5.0F, 10.0F, 15.0F));
    public static final Item STONE_SPEAR = item("stone_spear", new SpearItem(ToolMaterials.STONE, 2, 1.33F, 0.82F, new FabricItemSettings()).timings(0.75F, 0.7F, 4.5F, 9.0F, 13.75F));
    public static final Item COPPER_SPEAR = item("copper_spear", new SpearItem(COPPER, 2, 1.18F, 0.82F, new FabricItemSettings()).timings(0.85F, 0.65F, 4.0F, 8.25F, 12.5F));
    public static final Item IRON_SPEAR = item("iron_spear", new SpearItem(ToolMaterials.IRON, 3, 1.05F, 0.95F, new FabricItemSettings()).timings(0.95F, 0.6F, 2.5F, 6.75F, 11.25F));
    public static final Item GOLDEN_SPEAR = item("golden_spear", new SpearItem(ToolMaterials.GOLD, 1, 1.05F, 0.7F, new FabricItemSettings()).timings(0.95F, 0.7F, 3.5F, 8.5F, 13.75F));
    public static final Item DIAMOND_SPEAR = item("diamond_spear", new SpearItem(ToolMaterials.DIAMOND, 4, 0.95F, 1.075F, new FabricItemSettings()).timings(1.05F, 0.5F, 3.0F, 6.5F, 10.0F));
    public static final Item NETHERITE_SPEAR = item("netherite_spear", new SpearItem(ToolMaterials.NETHERITE, 5, 0.87F, 1.2F, new FabricItemSettings().fireproof()).timings(1.15F, 0.4F, 2.5F, 5.5F, 8.75F));
    public static final Item[] SPEARS = {WOODEN_SPEAR, STONE_SPEAR, COPPER_SPEAR, IRON_SPEAR, GOLDEN_SPEAR, DIAMOND_SPEAR, NETHERITE_SPEAR};

    // =========================================================
    // MACE AND WIND CHARGES (1.21)
    // =========================================================

    public static final Item BREEZE_ROD = item("breeze_rod", new Item(new FabricItemSettings()));
    public static final Item MACE = item("mace", new MaceItem(new FabricItemSettings().maxDamage(500).rarity(Rarity.EPIC)));
    public static final Item WIND_CHARGE = item("wind_charge", new WindChargeItem(new FabricItemSettings()));

    public static final EntityType<WindChargeEntity> WIND_CHARGE_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier(MOD_ID, "wind_charge"),
            FabricEntityTypeBuilder.<WindChargeEntity>create(SpawnGroup.MISC, WindChargeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.3125F, 0.3125F)).trackRangeChunks(4).trackedUpdateRate(10).build());

    // =========================================================
    // ENCHANTMENTS
    // =========================================================

    private static boolean isMace(ItemStack stack) {
        return stack.isOf(MACE);
    }

    private static boolean isSpear(ItemStack stack) {
        return stack.getItem() instanceof SpearItem;
    }

    public static final Enchantment DENSITY = enchantment("density", new BackportEnchantment(Enchantment.Rarity.UNCOMMON, 5, 5, 8, 20, false,
            BackportCombat::isMace, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS));
    public static final Enchantment BREACH = enchantment("breach", new BackportEnchantment(Enchantment.Rarity.RARE, 4, 15, 9, 50, false,
            BackportCombat::isMace, DENSITY, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS));
    public static final Enchantment WIND_BURST = enchantment("wind_burst", new BackportEnchantment(Enchantment.Rarity.RARE, 3, 15, 9, 50, false,
            BackportCombat::isMace));
    public static final Enchantment LUNGE = enchantment("lunge", new BackportEnchantment(Enchantment.Rarity.UNCOMMON, 3, 5, 8, 20, false,
            BackportCombat::isSpear));

    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
        BackportBlocks.TAB.add(item);
        return item;
    }

    private static Enchantment enchantment(String name, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, name), enchantment);
    }

    public static void register() {
    }
}
