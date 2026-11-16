package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.client.HeadwearFlatModels;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Inventory slots: swap headwear to its flat 2D model as soon as DrawContext looks the model up,
 * so the slot's lighting and depth are chosen for a flat icon too. (Swapping later, in
 * ItemRenderer, would light the flat icon like a 3D model and make it look dark.)
 */
@Mixin(DrawContext.class)
public abstract class DrawContextHeadwearMixin {

    @ModifyVariable(
            method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private BakedModel gallifrey$useFlatHeadwearModel(BakedModel model, LivingEntity entity, World world,
                                                      ItemStack stack, int x, int y, int seed, int z) {
        BakedModel flat = HeadwearFlatModels.flatModelFor(stack);
        return flat != null ? flat : model;
    }
}
