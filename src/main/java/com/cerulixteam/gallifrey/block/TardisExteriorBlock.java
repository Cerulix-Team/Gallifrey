package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class TardisExteriorBlock extends Block implements BlockEntityProvider {

    // 8 steps around the compass, 45 degrees apart
    // 0 = south, 2 = west, 4 = north, 6 = east
    public static final IntProperty ROTATION =
            IntProperty.of("rotation", 0, 7);

    public TardisExteriorBlock(Settings settings) {
        super(settings);

        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(ROTATION, 0)
        );
    }

    @Override
    protected void appendProperties(
            StateManager.Builder<Block, BlockState> builder
    ) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {

        float yaw = ctx.getPlayerYaw();

        int rotation = MathHelper.floor(
                (double) ((yaw + 180.0F) * 8.0F / 360.0F)
                        + 0.5D
        ) & 7;

        return this.getDefaultState()
                .with(ROTATION, rotation);
    }

    @Override
    public BlockEntity createBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new TardisExteriorBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (!world.isClient &&
                player instanceof ServerPlayerEntity serverPlayer) {

            MinecraftServer server =
                    serverPlayer.getServer();

            if (server != null) {

                RegistryKey<World> tardisWorldKey =
                        RegistryKey.of(
                                RegistryKeys.WORLD,
                                new Identifier(
                                        "gallifrey",
                                        "tardis"
                                )
                        );

                ServerWorld tardisWorld =
                        server.getWorld(
                                tardisWorldKey
                        );

                if (tardisWorld != null) {

                    BlockEntity blockEntity =
                            world.getBlockEntity(pos);

                    if (blockEntity
                            instanceof TardisExteriorBlockEntity tardis) {

                        tardis.generateInterior(
                                tardisWorld
                        );
                    }

                    serverPlayer.teleport(
                            tardisWorld,
                            0.5,
                            65.0,
                            0.5,
                            serverPlayer.getYaw(),
                            serverPlayer.getPitch()
                    );

                    com.cerulixteam.gallifrey.tardis.TardisEntryTracker.entered(
                            serverPlayer.getUuid(), world.getRegistryKey(), pos);
                    com.cerulixteam.gallifrey.compat.pehkui.PehkuiCompat.squeezeThroughDoor(serverPlayer);

                    return ActionResult.SUCCESS;
                }

                player.sendMessage(
                        Text.literal(
                                "TARDIS dimension could not be found!"
                        ),
                        false
                );
            }
        }

        return ActionResult.PASS;
    }




}


