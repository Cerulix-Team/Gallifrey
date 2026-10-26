package com.timelordmod.gallifrey.item.custom;

import com.timelordmod.gallifrey.GallifreySounds;
import com.timelordmod.gallifrey.sonic.SonicCasing;
import com.timelordmod.gallifrey.sonic.SonicHandler;
import com.timelordmod.gallifrey.sonic.SonicMode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class SonicScrewdriver extends Item {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final double SONIC_RANGE = 16.0D;

    public static final int MAX_POWER = 100;

    private static final String POWER_KEY = "SonicPower";
    private static final String MODE_KEY = "SonicMode";
    private static final String CASING_KEY = "SonicCasing";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SonicScrewdriver(Settings settings) {
        super(settings);
    }

    // =========================================================
    // POWER
    // =========================================================

    public static int getPower(ItemStack stack) {
        return stack
                .getOrCreateNbt()
                .getInt(POWER_KEY);
    }

    public static void setPower(
            ItemStack stack,
            int power
    ) {

        power = Math.max(
                0,
                Math.min(
                        MAX_POWER,
                        power
                )
        );

        stack
                .getOrCreateNbt()
                .putInt(
                        POWER_KEY,
                        power
                );
    }

    public static void consumePower(
            ItemStack stack
    ) {

        int power = getPower(stack);

        if (power <= 0) {
            return;
        }

        setPower(
                stack,
                power - 1
        );
    }

    public static void recharge(
            ItemStack stack,
            int amount
    ) {

        int power = getPower(stack);

        if (power >= MAX_POWER) {
            return;
        }

        setPower(
                stack,
                power + amount
        );
    }

    // =========================================================
    // CASING
    // =========================================================

    public static SonicCasing getCasing(
            ItemStack stack
    ) {

        String casingId =
                stack
                        .getOrCreateNbt()
                        .getString(CASING_KEY);

        return SonicCasing.fromId(
                casingId
        );
    }

    public static void setCasing(
            ItemStack stack,
            SonicCasing casing
    ) {

        stack
                .getOrCreateNbt()
                .putString(
                        CASING_KEY,
                        casing.getId()
                );
    }

    // =========================================================
    // CHANGE CASING
    // =========================================================

    public static void changeCasing(
            ItemStack stack
    ) {

        SonicCasing currentCasing =
                getCasing(stack);

        SonicCasing[] casings =
                SonicCasing.values();

        int currentIndex = 0;

        for (int i = 0; i < casings.length; i++) {

            if (casings[i] == currentCasing) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex =
                (currentIndex + 1)
                        % casings.length;

        setCasing(
                stack,
                casings[nextIndex]
        );
    }

    // =========================================================
    // ACTIVE STATE
    // =========================================================

    public static boolean isOn(
            ItemStack stack
    ) {

        return getPower(stack) > 0;
    }

    // =========================================================
    // MODE
    // =========================================================

    public static SonicMode getMode(
            ItemStack stack
    ) {

        int mode =
                stack
                        .getOrCreateNbt()
                        .getInt(MODE_KEY);

        SonicMode[] modes = SonicMode.values();
        if (mode < 0 || mode >= modes.length || !isModeAvailable(stack, modes[mode])) {
            return SonicMode.INTERACTION;
        }
        return modes[mode];
    }

    public static void setMode(
            ItemStack stack,
            SonicMode mode
    ) {
        stack.getOrCreateNbt().putInt(MODE_KEY, mode.ordinal());
    }

    public static void toggleMode(ItemStack stack) {
        SonicMode[] modes = SonicMode.values();
        SonicMode next = getMode(stack);
        // Skip modes this sonic can't use right now (TARDIS mode needs Adventures in Time).
        do {
            next = modes[(next.ordinal() + 1) % modes.length];
        } while (!isModeAvailable(stack, next));
        setMode(stack, next);
    }

    /** TARDIS mode is only there with Adventures in Time installed, and not on the Sonic Shades. */
    public static boolean isModeAvailable(ItemStack stack, SonicMode mode) {
        return mode != SonicMode.TARDIS || com.timelordmod.gallifrey.compat.ait.AitSonics.canUseTardisMode(stack);
    }

    // =========================================================
    // INVENTORY TICK
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

        // -----------------------------------------------------
        // INITIAL POWER
        // -----------------------------------------------------

        if (
                !stack
                        .getOrCreateNbt()
                        .contains(POWER_KEY)
        ) {

            setPower(
                    stack,
                    MAX_POWER
            );
        }

        // -----------------------------------------------------
        // INITIAL MODE
        // -----------------------------------------------------

        if (
                !stack
                        .getOrCreateNbt()
                        .contains(MODE_KEY)
        ) {

            setMode(
                    stack,
                    SonicMode.INTERACTION
            );
        }

        // -----------------------------------------------------
        // INITIAL CASING
        // -----------------------------------------------------

        if (
                !stack
                        .getOrCreateNbt()
                        .contains(CASING_KEY)
        ) {

            setCasing(
                    stack,
                    SonicCasing.SECOND_DOCTOR
            );
        }
    }

    // =========================================================
    // BLOCK TARGET
    // =========================================================

    private static BlockHitResult getBlockTarget(
            PlayerEntity player
    ) {

        Vec3d start =
                player.getCameraPosVec(1.0F);

        Vec3d direction =
                player.getRotationVec(1.0F);

        Vec3d end =
                start.add(
                        direction.multiply(
                                SONIC_RANGE
                        )
                );

        return player
                .getWorld()
                .raycast(
                        new RaycastContext(
                                start,
                                end,
                                RaycastContext.ShapeType.OUTLINE,
                                RaycastContext.FluidHandling.NONE,
                                player
                        )
                );
    }

    // =========================================================
    // ENTITY TARGET
    // =========================================================

    private static EntityHitResult getEntityTarget(
            PlayerEntity player
    ) {

        World world =
                player.getWorld();

        Vec3d start =
                player.getCameraPosVec(1.0F);

        Vec3d direction =
                player.getRotationVec(1.0F);

        Vec3d end =
                start.add(
                        direction.multiply(
                                SONIC_RANGE
                        )
                );

        Box searchBox =
                player
                        .getBoundingBox()
                        .stretch(
                                direction.multiply(
                                        SONIC_RANGE
                                )
                        )
                        .expand(1.0D);

        List<Entity> entities =
                world.getOtherEntities(
                        player,
                        searchBox,
                        entity ->
                                entity.isAlive()
                                        && entity.isAttackable()
                );

        Entity closestEntity = null;
        Vec3d closestHit = null;

        double closestDistance =
                SONIC_RANGE * SONIC_RANGE;

        for (
                Entity entity :
                entities
        ) {

            Box box =
                    entity
                            .getBoundingBox()
                            .expand(0.3D);

            Optional<Vec3d> hit =
                    box.raycast(
                            start,
                            end
                    );

            if (hit.isPresent()) {

                double distance =
                        start.squaredDistanceTo(
                                hit.get()
                        );

                if (
                        distance
                                < closestDistance
                ) {

                    closestDistance =
                            distance;

                    closestEntity =
                            entity;

                    closestHit =
                            hit.get();
                }
            }
        }

        if (closestEntity == null) {
            return null;
        }

        // Keep the exact point the beam hit, so getSonicTarget can compare it with the block hit
        // like for like (an entity's position is its feet, which is often further away).
        return new EntityHitResult(
                closestEntity,
                closestHit
        );
    }

    // =========================================================
    // SONIC TARGET
    // =========================================================

    /** What the sonic is pointing at (block or entity, up to 16 blocks). Also used by the Sonic Shades scan HUD. */
    public static HitResult getSonicTarget(
            PlayerEntity player
    ) {

        BlockHitResult blockHit =
                getBlockTarget(player);

        EntityHitResult entityHit =
                getEntityTarget(player);

        double blockDistance =
                SONIC_RANGE * SONIC_RANGE;

        double entityDistance =
                SONIC_RANGE * SONIC_RANGE;

        // -----------------------------------------------------
        // BLOCK DISTANCE
        // -----------------------------------------------------

        if (
                blockHit.getType()
                        != HitResult.Type.MISS
        ) {

            blockDistance =
                    player
                            .getCameraPosVec(1.0F)
                            .squaredDistanceTo(
                                    blockHit.getPos()
                            );
        }

        // -----------------------------------------------------
        // ENTITY DISTANCE
        // -----------------------------------------------------

        if (entityHit != null) {

            entityDistance =
                    player
                            .getCameraPosVec(1.0F)
                            .squaredDistanceTo(
                                    entityHit
                                            .getPos()
                            );
        }

        // -----------------------------------------------------
        // CLOSEST TARGET
        // -----------------------------------------------------

        if (
                entityHit != null
                        && entityDistance
                        <= blockDistance
        ) {

            return entityHit;
        }

        return blockHit;
    }

    // =========================================================
    // SHARED SONIC ACTIVATION
    // =========================================================

    /**
     * Performs the same server-side sonic action used by right-clicking the
     * screwdriver. Wearable sonic devices can call this without duplicating
     * the targeting/SonicHandler implementation.
     */
    public static void activate(PlayerEntity player, World world, ItemStack stack) {
        if (world.isClient) {
            return;
        }

        if (getPower(stack) <= 0) {
            player.sendMessage(Text.literal("§cSONIC: Power depleted."), true);
            return;
        }

        HitResult target = getSonicTarget(player);
        SonicMode mode = getMode(stack);
        SonicCasing casing = getCasing(stack);

        if (mode == SonicMode.TARDIS) {
            // Adventures in Time: call, send off or park the linked TARDIS. AIT shows its own
            // message for what happened, so the power readout below is skipped.
            if (com.timelordmod.gallifrey.compat.ait.AitSonics.useTardisMode(player, world, stack)) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        GallifreySounds.SONIC, SoundCategory.PLAYERS, 1.0F, 1.0F);
                consumePower(stack);
            }
            return;
        }

        // Land claims (Flan, GOML, Open Parties and Claims...): nothing happens to blocks or creatures
        // in land this player isn't trusted in. No power is used.
        if (target instanceof BlockHitResult blockHit && target.getType() == HitResult.Type.BLOCK
                && !com.timelordmod.gallifrey.compat.claims.ClaimsCompat.canUseBlock(player, world, blockHit.getBlockPos())
                || target instanceof EntityHitResult entityHit
                && !com.timelordmod.gallifrey.compat.claims.ClaimsCompat.canUseOnEntity(player, entityHit.getEntity())) {
            player.sendMessage(Text.literal("§cSONIC: That's inside protected land."), true);
            return;
        }

        // Deo's Portal Gun portals and Tempad time doors: overload closes them, scan reads where they go.
        if (com.timelordmod.gallifrey.compat.deo.DeoPortals.handleSonic(player, target, mode)
                || com.timelordmod.gallifrey.compat.tempad.TempadDoors.handleSonic(player, target, mode)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    GallifreySounds.SONIC, SoundCategory.PLAYERS, 1.0F, 1.0F);
            consumePower(stack);
            return;
        }

        SonicHandler.use(player, world, target, mode);

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                GallifreySounds.SONIC,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );

        consumePower(stack);

        String sonicName = stack.getItem() instanceof SonicShadesItem
                ? "Sonic Shades"
                : casing.getDisplayName();

        player.sendMessage(
                Text.literal(
                        "§b" + sonicName
                                + " §7| §bPOWER: §f" + getPower(stack)
                                + "§7/§f" + MAX_POWER
                ),
                true
        );
    }

    // =========================================================
    // NIRVANA BLUNT: held like a joint while smoking
    // =========================================================

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.canSmoke(stack)
                ? com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.SMOKE_TICKS
                : super.getMaxUseTime(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.canSmoke(stack)
                ? UseAction.SPYGLASS
                : super.getUseAction(stack);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.canSmoke(stack)) {
            com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.smoke(stack, world, user);
        }
        return stack;
    }

    // =========================================================
    // USE
    // =========================================================

    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity player,
            Hand hand
    ) {

        ItemStack stack =
                player.getStackInHand(hand);

        // =====================================================
        // SNEAK + RIGHT CLICK
        // CHANGE MODE
        // =====================================================

        if (player.isSneaking()) {

            if (!world.isClient) {
                if (!isOn(stack)) {
                    player.sendMessage(Text.literal("§7SONIC: Inactive — recharge the sonic to use it."), true);
                    return TypedActionResult.success(stack, false);
                }

                toggleMode(stack);

                SonicMode mode =
                        getMode(stack);

                SonicCasing casing =
                        getCasing(stack);

                player.sendMessage(
                        Text.literal(
                                "§bSONIC MODE: §e"
                                        + mode.getDisplayName()
                                        + " §7| §bCASING: §e"
                                        + casing.getDisplayName()
                        ),
                        true
                );
            }

            return TypedActionResult.success(
                    stack,
                    world.isClient
            );
        }

        // =====================================================
        // NIRVANA: SMOKE THE BLUNT CASING
        // Pointed at nothing, a Blunt sonic is smoked like a joint
        // (see compat.nirvana.NirvanaBlunt). Pointed at something,
        // it's still a sonic.
        // =====================================================

        if (com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.canSmoke(stack)
                && getSonicTarget(player).getType() == HitResult.Type.MISS) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        // =====================================================
        // NO POWER = INACTIVE
        // =====================================================

        if (getPower(stack) <= 0) {

            if (!world.isClient) {

                player.sendMessage(
                        Text.literal(
                                "§cSONIC: Power depleted."
                        ),
                        true
                );
            }

            return TypedActionResult.success(
                    stack,
                    world.isClient
            );
        }

        // =====================================================
        // ACTUAL SONIC USE
        // =====================================================

        if (!world.isClient) {
            activate(player, world, stack);
        }

        return TypedActionResult.success(
                stack,
                world.isClient
        );
    }
}