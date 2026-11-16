package com.cerulixteam.gallifrey.block.backport;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;
import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

/** The backported items that aren't just a block: torches, nuggets, resin bricks, and the Pale Oak signs and boats. */
public final class BackportItems {
    private BackportItems() {}

    public static final Item COPPER_TORCH = item("copper_torch",
            new VerticallyAttachableBlockItem(BackportBlocks.COPPER_TORCH, BackportBlocks.COPPER_WALL_TORCH, new FabricItemSettings(), Direction.DOWN));
    public static final Item COPPER_NUGGET = item("copper_nugget", new Item(new FabricItemSettings()));
    public static final Item RESIN_BRICK = item("resin_brick", new Item(new FabricItemSettings()));

    public static final Item PALE_OAK_SIGN = item("pale_oak_sign",
            new SignItem(new FabricItemSettings().maxCount(16), BackportBlocks.PALE_OAK_SIGN, BackportBlocks.PALE_OAK_WALL_SIGN));
    public static final Item PALE_OAK_HANGING_SIGN = item("pale_oak_hanging_sign",
            new HangingSignItem(BackportBlocks.PALE_OAK_HANGING_SIGN, BackportBlocks.PALE_OAK_WALL_HANGING_SIGN, new FabricItemSettings().maxCount(16)));

    public static final Identifier PALE_OAK_BOAT_ID = new Identifier("gallifrey", "pale_oak_boat");
    public static final Identifier PALE_OAK_CHEST_BOAT_ID = new Identifier("gallifrey", "pale_oak_chest_boat");
    public static final RegistryKey<TerraformBoatType> PALE_OAK_BOAT_KEY = TerraformBoatTypeRegistry.createKey(PALE_OAK_BOAT_ID);
    public static final Item PALE_OAK_BOAT = TerraformBoatItemHelper.registerBoatItem(PALE_OAK_BOAT_ID, PALE_OAK_BOAT_KEY, false);
    public static final Item PALE_OAK_CHEST_BOAT = TerraformBoatItemHelper.registerBoatItem(PALE_OAK_CHEST_BOAT_ID, PALE_OAK_BOAT_KEY, true);

    private static Item item(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("gallifrey", name), item);
    }

    static void registerBoat() {
        TerraformBoatType paleOak = new TerraformBoatType.Builder()
                .item(PALE_OAK_BOAT)
                .chestItem(PALE_OAK_CHEST_BOAT)
                .planks(BackportBlocks.PALE_OAK_PLANKS.asItem())
                .build();
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, PALE_OAK_BOAT_KEY, paleOak);
    }
}
