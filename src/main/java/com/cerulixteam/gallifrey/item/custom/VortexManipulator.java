package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.GallifreySounds;
import com.cerulixteam.gallifrey.item.GallifreyModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VortexManipulator extends Item {

    private static final String IN_FLIGHT_KEY = "VMInFlight";

    /** Returns true when any non-empty fluid occupies the player's bounding box. */
    private static boolean isInAnyLiquid(PlayerEntity player) {
        Box box = player.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);
        int maxX = MathHelper.floor(box.maxX);
        int maxY = MathHelper.floor(box.maxY);
        int maxZ = MathHelper.floor(box.maxZ);

        for (BlockPos pos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (!player.getWorld().getFluidState(pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** NBT key for the jumps left before it needs refuelling (VMPacket uses the same key). */
    public static final String USES_KEY = "VMUsesRemaining";
    public static final int MAX_USES = 10;

    private static Hand otherHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    /** True when using the Vortex Manipulator in {@code hand} means refuelling: an Atrium Core is in the other hand. */
    public static boolean isRefuelling(PlayerEntity player, Hand hand) {
        return player.getStackInHand(hand).getItem() instanceof VortexManipulator
                && player.getStackInHand(otherHand(hand)).isOf(GallifreyModItems.ATRIUM_CORE);
    }

    public VortexManipulator(Settings settings) {
        super(settings
                .rarity(Rarity.RARE)
                .maxCount(1)
                .maxDamage(100)
                .fireproof()
        );
    }

    // =========================================================
    // USE
    // =========================================================

    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity user,
            Hand hand
    ) {

        ItemStack stack = user.getStackInHand(hand);

        // Refuel by holding an Atrium Core in the other hand
        // and using the Vortex Manipulator. One Atrium Core restores all ten uses.
        if (isRefuelling(user, hand)) {
            if (world.isClient) {
                // The server does the refuel; just swing the arm here.
                return TypedActionResult.success(stack, true);
            }
            ItemStack fuelStack = user.getStackInHand(otherHand(hand));
            // A Vortex Manipulator that has never jumped has no count yet: it's full (see VMPacket).
            int uses = stack.getOrCreateNbt().contains(USES_KEY) ? stack.getOrCreateNbt().getInt(USES_KEY) : MAX_USES;
            if (uses >= MAX_USES) {
                user.sendMessage(Text.literal("Vortex Manipulator power is already full (" + MAX_USES + "/" + MAX_USES + ")."), true);
                return TypedActionResult.fail(stack);
            }
            if (!user.getAbilities().creativeMode) {
                fuelStack.decrement(1);
            }
            stack.getOrCreateNbt().putInt(USES_KEY, MAX_USES);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    net.minecraft.sound.SoundEvents.BLOCK_BEACON_POWER_SELECT,
                    net.minecraft.sound.SoundCategory.PLAYERS, 0.6F, 1.6F);
            user.sendMessage(Text.literal("Vortex Manipulator refuelled: " + MAX_USES + "/" + MAX_USES + " uses."), true);
            return TypedActionResult.success(stack, false);
        }

        // The Vortex Manipulator cannot be activated while immersed in any fluid.
        if (isInAnyLiquid(user)) {
            user.sendMessage(Text.literal("VORTEX MANIPULATOR DISABLED: liquid interference detected."), true);
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient && user instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            VortexManipulatorData.ensureOwner(stack, serverPlayer);
            if (!VortexManipulatorData.isAuthorized(stack, serverPlayer.getUuid())) {
                serverPlayer.sendMessage(Text.literal("ISOMORPHIC LOCK: this Vortex Manipulator is keyed to another Time Lord."), true);
                return TypedActionResult.fail(stack);
            }
        }

        // -----------------------------------------------------
        // TAKE-OFF SOUND
        // -----------------------------------------------------

        if (!world.isClient) {

            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    GallifreySounds.VM_TAKE_OFF,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );

            // Mark the player as being in-flight.
            stack.getOrCreateNbt().putBoolean(
                    IN_FLIGHT_KEY,
                    true
            );
        }

        return TypedActionResult.success(
                stack,
                world.isClient()
        );
    }

    // =========================================================
    // LANDING DETECTION
    // =========================================================

    @Override
    public void inventoryTick(
            ItemStack stack,
            World world,
            Entity entity,
            int slot,
            boolean selected
    ) {

        super.inventoryTick(
                stack,
                world,
                entity,
                slot,
                selected
        );

        // Only run on the server.
        if (world.isClient) {
            return;
        }

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        boolean inFlight =
                stack
                        .getOrCreateNbt()
                        .getBoolean(IN_FLIGHT_KEY);

        if (!inFlight) {
            return;
        }

        // -----------------------------------------------------
        // PLAYER HAS LANDED
        // -----------------------------------------------------

        if (player.isOnGround()) {

            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    GallifreySounds.VM_LAND,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );

            stack.getOrCreateNbt().putBoolean(
                    IN_FLIGHT_KEY,
                    false
            );
        }
    }

    // =========================================================
    // TOOLTIP
    // =========================================================

    @Override
    public void appendTooltip(
            ItemStack stack,
            @Nullable World world,
            List<Text> tooltip,
            net.minecraft.client.item.TooltipContext context
    ) {

        tooltip.add(
                Text.literal(
                        "A dangerous tool used for easy space travel."
                )
        );

        tooltip.add(
                Text.literal(
                        "WARNING: Time Lords are not responsible for any side effects caused!"
                ).formatted(Formatting.RED)
        );

        tooltip.add(
                Text.literal(
                        "Blame UNIT!"
                ).formatted(Formatting.DARK_RED)
        );

        int uses = stack.getOrCreateNbt().contains("VMUsesRemaining") ? stack.getOrCreateNbt().getInt("VMUsesRemaining") : 10;
        tooltip.add(Text.literal("Power: " + uses + "/10 uses"));
        tooltip.add(Text.literal("Refuel using an Atrium Core in your other hand."));

        super.appendTooltip(
                stack,
                world,
                tooltip,
                context
        );
    }
}
