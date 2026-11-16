package com.cerulixteam.gallifrey.compat.jade;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade integration: looking at a Tree Tapper shows what it's collecting and how full it is
 * (or that it isn't on a living tree, so it isn't filling). Everything it needs is already on
 * the client, so there's no server side to this.
 *
 * Jade finds this through the "jade" entrypoint in fabric.mod.json, so it's only loaded when Jade
 * is installed. Nothing else in the mod may reference this package. It can be switched off in
 * Jade's plugin settings ("Tree Tapper").
 */
@WailaPlugin
public class GallifreyJadePlugin implements IWailaPlugin {
    public static final Identifier TAPPER = new Identifier(GallifreyMod.MOD_ID, "tapper");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TapperInfo.INSTANCE, TapperBlock.class);
    }

    public enum TapperInfo implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            Item product = TapperBlock.tappedProduct(accessor.getLevel(), state, accessor.getPosition());
            if (product != null) {
                tooltip.add(Text.translatable("jade.gallifrey.tapper.collecting", product.getName()));
            }

            int level = state.get(TapperBlock.LEVEL);
            if (level >= TapperBlock.MAX_LEVEL) {
                tooltip.add(Text.translatable("jade.gallifrey.tapper.full").formatted(Formatting.GREEN));
            } else {
                tooltip.add(Text.translatable("jade.gallifrey.tapper.level", level, TapperBlock.MAX_LEVEL));
                if (!TapperBlock.isFilling(accessor.getLevel(), state, accessor.getPosition())) {
                    tooltip.add(Text.translatable("block.gallifrey.tapper.not_a_tree").formatted(Formatting.RED));
                }
            }
        }

        @Override
        public Identifier getUid() {
            return TAPPER;
        }
    }
}
