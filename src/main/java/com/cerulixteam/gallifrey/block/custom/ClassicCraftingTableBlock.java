package com.cerulixteam.gallifrey.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A crafting table that actually works.
 *
 * Vanilla's crafting screen closes itself straight away unless the block is
 * minecraft:crafting_table. This uses a crafting screen that checks for THIS block instead,
 * so it behaves exactly like a normal 3x3 crafting table (recipe book included).
 */
public class ClassicCraftingTableBlock extends CraftingTableBlock {
    private static final Text TITLE = Text.translatable("container.crafting");

    public ClassicCraftingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        Block self = this;
        return new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, player) -> new Handler(syncId, inventory, ScreenHandlerContext.create(world, pos), self),
                TITLE);
    }

    private static class Handler extends CraftingScreenHandler {
        private final ScreenHandlerContext context;
        private final Block table;

        Handler(int syncId, PlayerInventory inventory, ScreenHandlerContext context, Block table) {
            super(syncId, inventory, context);
            this.context = context;
            this.table = table;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return canUse(this.context, player, this.table);
        }
    }
}
