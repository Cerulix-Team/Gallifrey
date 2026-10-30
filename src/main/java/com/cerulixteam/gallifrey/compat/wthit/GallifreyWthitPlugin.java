package com.cerulixteam.gallifrey.compat.wthit;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * WTHIT integration, matching the Jade one: looking at a Tree Tapper shows what it's collecting
 * and how full it is (or that it isn't on a living tree). Client-only; everything it needs is
 * already on the client.
 *
 * WTHIT finds this through waila_plugins.json, so it's only loaded when WTHIT is installed.
 * Uses the IWailaPlugin API, which every WTHIT 8.x release for 1.20.1 supports.
 * It can be switched off in WTHIT's plugin settings.
 */
@SuppressWarnings("deprecation")
public class GallifreyWthitPlugin implements IWailaPlugin {
    public static final Identifier TAPPER = new Identifier(GallifreyMod.MOD_ID, "tapper");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(TAPPER, true);
        registrar.addComponent(TapperInfo.INSTANCE, TooltipPosition.BODY, TapperBlock.class);
    }

    public enum TapperInfo implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(TAPPER)) return;
            BlockState state = accessor.getBlockState();
            Item product = TapperBlock.tappedProduct(accessor.getLevel(), state, accessor.getPosition());
            if (product != null) {
                tooltip.addLine(Text.translatable("jade.gallifrey.tapper.collecting", product.getName()));
            }

            int level = state.get(TapperBlock.LEVEL);
            if (level >= TapperBlock.MAX_LEVEL) {
                tooltip.addLine(Text.translatable("jade.gallifrey.tapper.full").formatted(Formatting.GREEN));
            } else {
                tooltip.addLine(Text.translatable("jade.gallifrey.tapper.level", level, TapperBlock.MAX_LEVEL));
                if (!TapperBlock.isFilling(accessor.getLevel(), state, accessor.getPosition())) {
                    tooltip.addLine(Text.translatable("block.gallifrey.tapper.not_a_tree").formatted(Formatting.RED));
                }
            }
        }
    }
}
