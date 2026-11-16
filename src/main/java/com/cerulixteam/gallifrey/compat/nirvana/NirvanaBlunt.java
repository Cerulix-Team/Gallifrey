package com.cerulixteam.gallifrey.compat.nirvana;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;
import com.cerulixteam.gallifrey.item.custom.SonicShadesItem;
import com.cerulixteam.gallifrey.sonic.SonicCasing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Nirvana support: a sonic screwdriver in the Blunt casing can be smoked like Nirvana's joint.
 *
 * Hold right-click with the Blunt sonic while not pointing it at anything (pointed at a block or
 * mob, it still works as a normal sonic). After the same 1.6 seconds as a joint, it gives exactly
 * what a joint gives: Nirvana's Peace effect to everyone nearby, the same radius and duration
 * (from Nirvana's own config), the same stacking (Reefers and all), smoke and sound. Nirvana's
 * own code works all of that out, using a joint as the source. The sonic isn't used up and
 * doesn't use power; there's a one-second cooldown, like a joint.
 *
 * Without Nirvana installed, the Blunt casing is just a casing.
 *
 * No compile-time dependency: Nirvana's joint is looked up by id and its methods by name. Written
 * against Nirvana for 1.20.1 (main/1.20.x). If Nirvana changes, this logs a warning and switches
 * itself off rather than crashing.
 */
public final class NirvanaBlunt {
    private NirvanaBlunt() {}

    private static final String MOD_ID = "nirvana";
    private static final Identifier JOINT_ID = new Identifier(MOD_ID, "joint");
    private static final Identifier SMOKING_SOUND = new Identifier(MOD_ID, "item.use.smoking");
    /** Same as Nirvana's SmokingItem. */
    public static final int SMOKE_TICKS = 32;

    private static boolean active = false;
    private static Method applyEffect;   // static SmokingItem#applyEffect(StatusEffectInstance, ItemStack, LivingEntity, LivingEntity)
    private static Item joint;
    private static Method getEffects;    // JointItem#getEffects(ItemStack, World, LivingEntity) -> Stream
    private static Method getRadius;     // JointItem#getRadius(ItemStack, World, LivingEntity) -> double

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            Class<?> smoking = Class.forName("galena.nirvana.world.item.SmokingItem");
            applyEffect = smoking.getMethod("applyEffect",
                    StatusEffectInstance.class, ItemStack.class, LivingEntity.class, LivingEntity.class);
            active = true;
            GallifreyMod.LOGGER.info("[Gallifrey] Nirvana found: the Blunt sonic casing can be smoked");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            GallifreyMod.LOGGER.warn("[Gallifrey] Nirvana is installed but wasn't what Gallifrey expected; Blunt sonic compat is off", e);
        }
    }

    /** True for a hand-held sonic in the Blunt casing, with Nirvana installed. */
    public static boolean canSmoke(ItemStack stack) {
        return active
                && stack.getItem() instanceof SonicScrewdriver
                && !(stack.getItem() instanceof SonicShadesItem)
                && SonicScrewdriver.getCasing(stack) == SonicCasing.BLUNT_SONIC;
    }

    /** Looked up the first time it's needed: Nirvana may register its items after Gallifrey starts. */
    private static boolean lookUpJoint() {
        if (joint != null) return true;
        Item item = Registries.ITEM.get(JOINT_ID);
        if (item == Items.AIR) return false;
        Method effects = findMethod(item.getClass(), "getEffects");
        Method radius = findMethod(item.getClass(), "getRadius");
        if (effects == null || radius == null) return false;
        getEffects = effects;
        getRadius = radius;
        joint = item;
        return true;
    }

    /** Nirvana's getEffects/getRadius are package-private and declared on JointItem (or a subclass). */
    private static Method findMethod(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(name, ItemStack.class, World.class, LivingEntity.class);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                // try the superclass
            } catch (RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    /** Called when the player finishes smoking the Blunt sonic. */
    @SuppressWarnings("unchecked")
    public static void smoke(ItemStack sonic, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) player.getItemCooldownManager().set(sonic.getItem(), 20);
        if (!(world instanceof ServerWorld serverWorld) || !lookUpJoint()) return;

        // A joint stands in as the source, so Nirvana treats this exactly like smoking one.
        ItemStack source = new ItemStack(joint);
        try {
            double range = (double) getRadius.invoke(joint, source, world, user) * 2;
            Vec3d pos = user.getPos();
            for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, Box.of(pos, range, range, range), e -> true)) {
                Stream<StatusEffectInstance> effects = (Stream<StatusEffectInstance>) getEffects.invoke(joint, source, world, user);
                for (StatusEffectInstance effect : (Iterable<StatusEffectInstance>) effects::iterator) {
                    applyEffect.invoke(null, effect, source, target, user);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't apply Nirvana's effects from the Blunt sonic", e);
            return;
        }

        // Smoke from the mouth and Nirvana's smoking sound, as with a joint.
        Vec3d look = user.getRotationVector().multiply(0.6);
        serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                user.getX() + look.x, user.getEyeY() + look.y, user.getZ() + look.z,
                5, 0.0, 0.2 + user.getRandom().nextDouble() * 0.1, 0.0, 0.02);
        SoundEvent sound = Registries.SOUND_EVENT.get(SMOKING_SOUND);
        if (sound != null) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }
}
