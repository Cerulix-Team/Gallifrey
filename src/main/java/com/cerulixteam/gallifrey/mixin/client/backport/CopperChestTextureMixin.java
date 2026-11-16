package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.CopperChestBlock;
import com.cerulixteam.gallifrey.block.backport.CopperChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Copper chests (1.21.9) use vanilla's chest renderer with their own textures, one per rust stage. */
@Mixin(TexturedRenderLayers.class)
public abstract class CopperChestTextureMixin {
    @Inject(method = "getChestTextureId", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$copperChest(BlockEntity blockEntity, ChestType type, boolean christmas, CallbackInfoReturnable<SpriteIdentifier> cir) {
        if (!(blockEntity instanceof CopperChestBlockEntity) || !(blockEntity.getCachedState().getBlock() instanceof CopperChestBlock chest)) return;
        String stage = switch (chest.level) {
            case EXPOSED -> "_exposed";
            case WEATHERED -> "_weathered";
            case OXIDIZED -> "_oxidized";
            default -> "";
        };
        String side = type == ChestType.LEFT ? "_left" : type == ChestType.RIGHT ? "_right" : "";
        cir.setReturnValue(new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier("gallifrey", "entity/chest/copper" + stage + side)));
    }
}
