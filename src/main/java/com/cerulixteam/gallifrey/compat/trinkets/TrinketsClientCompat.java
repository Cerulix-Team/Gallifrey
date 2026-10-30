package com.cerulixteam.gallifrey.compat.trinkets;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import com.cerulixteam.gallifrey.screens.VortexManipulatorScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Client half of the Trinkets support (see TrinketsCompat):
 *  - the "Vortex Manipulator (worn)" key, K by default, opens a Vortex Manipulator worn in a
 *    glove slot, the same as right-clicking one in your hand;
 *  - a worn Vortex Manipulator is drawn on the wrist, a belted sonic on the right hip.
 *
 * The renderers are Trinkets' TrinketRenderer interface, implemented with a reflection proxy so
 * there's no compile-time dependency. The positions are the constants below if they need nudging.
 */
public final class TrinketsClientCompat {
    private TrinketsClientCompat() {}

    // Vortex Manipulator on the wrist: offset from the hand end of the arm, size.
    private static final float WRIST_OUT = 0.135F;   // out from the centre of the arm, onto its outer face
    private static final float WRIST_UP = -0.13F;    // up the arm from the hand
    private static final float WRIST_SCALE = 0.32F;

    // Sonic on the belt: offset from the neck, size.
    private static final float BELT_SIDE = 0.27F;
    private static final float BELT_DOWN = 0.70F;
    private static final float BELT_SCALE = 0.42F;

    private static KeyBinding openWornVortexManipulator;

    public static void register() {
        if (!TrinketsCompat.isLoaded()) return;

        openWornVortexManipulator = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gallifrey.worn_vortex_manipulator", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.gallifrey"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openWornVortexManipulator.wasPressed()) {
                if (client.player == null || client.currentScreen != null) continue;
                if (!TrinketsCompat.findWorn(client.player, GallifreyModItems.VORTEX_MANIPULATOR).isEmpty()) {
                    client.setScreen(new VortexManipulatorScreen());
                }
            }
        });

        try {
            Class<?> rendererInterface = Class.forName("dev.emi.trinkets.api.client.TrinketRenderer");
            Method registerRenderer = Class.forName("dev.emi.trinkets.api.client.TrinketRendererRegistry")
                    .getMethod("registerRenderer", Item.class, rendererInterface);
            registerRenderer.invoke(null, GallifreyModItems.VORTEX_MANIPULATOR,
                    proxy(rendererInterface, TrinketsClientCompat::renderOnWrist));
            registerRenderer.invoke(null, GallifreyModItems.SONIC_SCREWDRIVER,
                    proxy(rendererInterface, TrinketsClientCompat::renderOnBelt));
        } catch (ReflectiveOperationException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't register Trinkets renderers; worn items won't be drawn", e);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------------------------

    /** The arguments of TrinketRenderer.render, in order. */
    private record RenderCall(ItemStack stack, Object slotReference, Object contextModel, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, LivingEntity entity) {}

    @FunctionalInterface
    private interface Renderer {
        void render(RenderCall call);
    }

    private static Object proxy(Class<?> rendererInterface, Renderer renderer) {
        InvocationHandler handler = (self, method, args) -> switch (method.getName()) {
            case "render" -> {
                renderer.render(new RenderCall((ItemStack) args[0], args[1], args[2], (MatrixStack) args[3],
                        (VertexConsumerProvider) args[4], (Integer) args[5], (LivingEntity) args[6]));
                yield null;
            }
            case "hashCode" -> System.identityHashCode(self);
            case "equals" -> self == args[0];
            case "toString" -> "Gallifrey TrinketRenderer";
            default -> null;
        };
        return Proxy.newProxyInstance(TrinketsClientCompat.class.getClassLoader(), new Class<?>[]{rendererInterface}, handler);
    }

    private static void renderOnWrist(RenderCall call) {
        if (!(call.contextModel() instanceof BipedEntityModel<?> model)) return;
        LivingEntity entity = call.entity();
        // "hand" group = main hand, "offhand" group = the other one.
        boolean mainHandSlot = !"offhand".equals(slotGroup(call.slotReference()));
        Arm arm = mainHandSlot ? entity.getMainArm() : entity.getMainArm().getOpposite();
        boolean right = arm == Arm.RIGHT;

        MatrixStack m = call.matrices();
        m.push();
        translateToArm(m, model, entity, right);
        m.translate(right ? -WRIST_OUT : WRIST_OUT, WRIST_UP, 0.0F);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(right ? -90.0F : 90.0F));
        m.scale(WRIST_SCALE, WRIST_SCALE, WRIST_SCALE);
        drawItem(call, m);
        m.pop();
    }

    private static void renderOnBelt(RenderCall call) {
        if (!(call.contextModel() instanceof BipedEntityModel<?> model)) return;
        LivingEntity entity = call.entity();

        MatrixStack m = call.matrices();
        m.push();
        if (entity.isInSneakingPose() && !model.riding && !entity.isSwimming()) {
            m.translate(0.0F, 0.2F, 0.0F);
            m.multiply(RotationAxis.POSITIVE_X.rotation(model.body.pitch));
        }
        m.multiply(RotationAxis.POSITIVE_Y.rotation(model.body.yaw));
        m.translate(-BELT_SIDE, BELT_DOWN, 0.0F);                       // right hip
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));    // face outwards
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45.0F));   // hang tip-down
        m.scale(BELT_SCALE, BELT_SCALE, BELT_SCALE);
        drawItem(call, m);
        m.pop();
    }

    /** Same as Trinkets' TrinketRenderer.translateToRightArm / translateToLeftArm. */
    private static void translateToArm(MatrixStack m, BipedEntityModel<?> model, LivingEntity entity, boolean right) {
        if (entity.isInSneakingPose() && !model.riding && !entity.isSwimming()) {
            m.translate(0.0F, 0.2F, 0.0F);
        }
        var limb = right ? model.rightArm : model.leftArm;
        m.multiply(RotationAxis.POSITIVE_Y.rotation(model.body.yaw));
        m.translate(right ? -0.3125F : 0.3125F, 0.15625F, 0.0F);
        m.multiply(RotationAxis.POSITIVE_Z.rotation(limb.roll));
        m.multiply(RotationAxis.POSITIVE_Y.rotation(limb.yaw));
        m.multiply(RotationAxis.POSITIVE_X.rotation(limb.pitch));
        m.translate(right ? -0.0625F : 0.0625F, 0.625F, 0.0F);
    }

    private static void drawItem(RenderCall call, MatrixStack m) {
        // The item models are drawn upside down in entity space; flip back.
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        MinecraftClient.getInstance().getItemRenderer().renderItem(call.entity(), call.stack(),
                ModelTransformationMode.FIXED, false, m, call.vertexConsumers(), call.entity().getWorld(),
                call.light(), OverlayTexture.DEFAULT_UV, call.entity().getId());
    }

    /** slotReference.inventory().getSlotType().getGroup(), or "" if that fails. */
    private static String slotGroup(Object slotReference) {
        try {
            Object inventory = slotReference.getClass().getMethod("inventory").invoke(slotReference);
            Object slotType = inventory.getClass().getMethod("getSlotType").invoke(inventory);
            return (String) slotType.getClass().getMethod("getGroup").invoke(slotType);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return "";
        }
    }
}
