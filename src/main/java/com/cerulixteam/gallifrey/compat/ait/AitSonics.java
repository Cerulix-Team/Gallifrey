package com.cerulixteam.gallifrey.compat.ait;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;
import com.cerulixteam.gallifrey.item.custom.SonicShadesItem;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Adventures in Time (AIT) support for Gallifrey's sonic screwdriver.
 *
 *  - Linking: right-click an AIT console's sonic port with a Gallifrey sonic to link it to that
 *    TARDIS. Same rules as AIT's own sonic, including TARDIS security keys. Unlike AIT's sonic it
 *    stays in your hand rather than being slotted into the port.
 *  - TARDIS mode: with AIT installed, the sonic gets a fourth mode, TARDIS (sneak + right-click to
 *    cycle to it, as with the other modes). Using it on a linked TARDIS does what AIT's TARDIS mode
 *    does, using AIT's own code so it behaves identically:
 *      - normally: summon the TARDIS to just in front of you (needs stabilisers, fuel, same
 *        dimension and within 256 blocks, as in AIT)
 *      - looking straight up: take off (handbrake off, dematerialise)
 *      - looking straight down: park and refuel (handbrake on, refuelling on)
 *
 * Sonic Shades don't get TARDIS mode, since they can't be put to a sonic port to link.
 *
 * No compile-time dependency, like the TARDIS Refined and Ad Astra compat: AIT is looked up by
 * name, and none of this runs without it. Written against AIT 1.2.9 for 1.20.1. If AIT's code
 * changes, this logs a warning and switches itself off (and TARDIS mode disappears) rather than
 * crashing.
 */
public final class AitSonics {
    private AitSonics() {}

    private static final String MOD_ID = "ait";
    /** Where a Gallifrey sonic stores the TARDIS it's linked to. */
    private static final String LINK_KEY = "GallifreyAitTardis";
    /** Where AIT's own sonic stores it (LinkableItem's default path). */
    private static final String AIT_LINK_KEY = "tardis";
    private static final Identifier SONIC_PORT = new Identifier(MOD_ID, "sonic_port");

    private static boolean active = false;
    private static Item aitSonic;
    private static Class<?> controlEntityClass;
    private static Method getControl;     // ConsoleControlEntity#getControl()
    private static Method controlId;      // Control#id()
    private static Method entityTardis;   // ConsoleControlEntity#tardis() -> TardisRef
    private static Method canRun;         // Control#canRun(Tardis, ServerPlayerEntity)
    private static Object tardisMode;     // SonicMode.Modes.TARDIS
    private static Method process;        // TardisSonicMode#process(ItemStack, World, PlayerEntity)

    /** True when AIT is installed and this compat switched on. */
    public static boolean isActive() {
        return active;
    }

    /** True for sonics that can use TARDIS mode (the hand-held sonic, not the Sonic Shades). */
    public static boolean canUseTardisMode(ItemStack stack) {
        return active && stack.getItem() instanceof SonicScrewdriver && !(stack.getItem() instanceof SonicShadesItem);
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            controlEntityClass = Class.forName("dev.amble.ait.core.entities.ConsoleControlEntity");
            getControl = controlEntityClass.getMethod("getControl");
            entityTardis = controlEntityClass.getMethod("tardis");
            Class<?> controlClass = Class.forName("dev.amble.ait.core.tardis.control.Control");
            controlId = controlClass.getMethod("id");
            Class<?> tardisClass = Class.forName("dev.amble.ait.core.tardis.Tardis");
            canRun = controlClass.getMethod("canRun", tardisClass, ServerPlayerEntity.class);

            Class<?> modes = Class.forName("dev.amble.ait.core.item.sonic.SonicMode$Modes");
            tardisMode = modes.getField("TARDIS").get(null);
            process = tardisMode.getClass().getMethod("process", ItemStack.class, World.class, PlayerEntity.class);

            UseEntityCallback.EVENT.register(AitSonics::onUseEntity);
            active = true;
            GallifreyMod.LOGGER.info("[Gallifrey] Adventures in Time found: sonics can link to AIT TARDISes");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            GallifreyMod.LOGGER.warn("[Gallifrey] Adventures in Time is installed but its code wasn't what Gallifrey expected; sonic compat is off", e);
        }
    }

    // ------------------------------------------------------------------
    // Linking at the sonic port
    // ------------------------------------------------------------------

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity,
                                            @Nullable EntityHitResult hit) {
        if (hand != Hand.MAIN_HAND || player.isSpectator()) return ActionResult.PASS;
        ItemStack sonic = player.getMainHandStack();
        if (!canUseTardisMode(sonic) || !controlEntityClass.isInstance(entity)) return ActionResult.PASS;
        try {
            Object control = getControl.invoke(entity);
            if (control == null || !SONIC_PORT.equals(controlId.invoke(control))) return ActionResult.PASS;
            if (world.isClient) return ActionResult.SUCCESS;

            Object ref = entityTardis.invoke(entity);
            Object tardis = ref == null ? null : ref.getClass().getMethod("get").invoke(ref);
            if (tardis == null) return ActionResult.PASS;

            if (!(boolean) canRun.invoke(control, tardis, player)) {
                player.sendMessage(Text.literal("§cSONIC: This TARDIS won't accept your sonic."), true);
                return ActionResult.SUCCESS;
            }

            UUID id = (UUID) tardis.getClass().getMethod("getUuid").invoke(tardis);
            sonic.getOrCreateNbt().putUuid(LINK_KEY, id);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.sendMessage(Text.literal("§bSONIC: Linked to this TARDIS. §7Switch to TARDIS mode to call it."), true);
            return ActionResult.SUCCESS;
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't link a sonic to an AIT TARDIS", e);
            return ActionResult.PASS;
        }
    }

    // ------------------------------------------------------------------
    // TARDIS mode
    // ------------------------------------------------------------------

    /**
     * Runs AIT's TARDIS sonic mode for a Gallifrey sonic. Returns true if it did something
     * (so the sonic should use power and make its sound).
     */
    public static boolean useTardisMode(PlayerEntity player, World world, ItemStack sonic) {
        if (!active || world.isClient || !(player instanceof ServerPlayerEntity)) return false;
        NbtCompound nbt = sonic.getNbt();
        if (nbt == null || !nbt.containsUuid(LINK_KEY)) {
            player.sendMessage(Text.literal("§7SONIC: Not linked to a TARDIS. Use the sonic on a TARDIS console's sonic port."), true);
            return false;
        }

        // Looked up here rather than at startup: AIT may register its items after Gallifrey.
        if (aitSonic == null || aitSonic == Items.AIR) {
            aitSonic = Registries.ITEM.get(new Identifier(MOD_ID, "sonic_screwdriver"));
            if (aitSonic == Items.AIR) return false;
        }

        // A stand-in AIT sonic, linked to the same TARDIS, so AIT's own code can run.
        ItemStack standIn = new ItemStack(aitSonic);
        standIn.getOrCreateNbt().putUuid(AIT_LINK_KEY, nbt.getUuid(LINK_KEY));

        // AIT decides what to do by which hand its sonic is in: main hand summons the TARDIS,
        // off hand takes off (looking up) or parks and refuels (looking down).
        float pitch = player.getPitch();
        Hand hand = (pitch < -60.0F || pitch > 60.0F) ? Hand.OFF_HAND : Hand.MAIN_HAND;

        ItemStack original = player.getStackInHand(hand);
        player.setStackInHand(hand, standIn);
        try {
            return (boolean) process.invoke(tardisMode, standIn, world, player);
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] AIT's TARDIS sonic mode failed", e);
            return false;
        } finally {
            player.setStackInHand(hand, original);
        }
    }
}
