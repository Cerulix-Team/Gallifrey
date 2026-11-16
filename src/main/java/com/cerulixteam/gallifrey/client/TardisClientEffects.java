package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.TardisPanelBlock;
import com.cerulixteam.gallifrey.block.TardisMonitorBlock;
import com.cerulixteam.gallifrey.block.TardisThrottleBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import com.cerulixteam.gallifrey.tardis.TardisPanelLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;

/**
 * Client side of TARDIS flight and controls:
 *  - camera shake inside the TARDIS on take-off, in flight and on landing (sent by the server);
 *  - the name of the TARDIS panel button you're looking at, under the crosshair (Dalek Mod style).
 */
public final class TardisClientEffects {
    private TardisClientEffects() {}

    private static float shakeIntensity;
    private static int shakeTicks;
    private static int shakeTotal;
    private static int time;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            time++;
            if (shakeTicks > 0) shakeTicks--;
        });
        HudRenderCallback.EVENT.register(TardisClientEffects::renderControlName);
    }

    /** Start (or strengthen) a shake. intensity 1 = a hard jolt. */
    public static void shake(float intensity, int ticks) {
        if (shakeTicks <= 0 || intensity >= currentIntensity(0)) {
            shakeIntensity = intensity;
            shakeTicks = ticks;
            shakeTotal = Math.max(1, ticks);
        }
    }

    private static float currentIntensity(float tickDelta) {
        if (shakeTicks <= 0) return 0;
        float left = Math.max(0, shakeTicks - tickDelta) / shakeTotal;
        return shakeIntensity * left;
    }

    /** Called from the camera (GameRenderer mixin): tilts and jiggles the view while shaking. */
    public static void applyShake(MatrixStack matrices, float tickDelta) {
        float i = currentIntensity(tickDelta);
        if (i <= 0.001F) return;
        float t = time + tickDelta;
        float roll = (float) (Math.sin(t * 1.7) + 0.5 * Math.sin(t * 4.3)) * 1.6F * i;
        float pitch = (float) (Math.sin(t * 2.3 + 1.0) + 0.4 * Math.sin(t * 6.1)) * 1.1F * i;
        float yaw = (float) Math.sin(t * 3.1 + 2.0) * 0.6F * i;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
    }

    private static void renderControlName(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.options.hudHidden || client.currentScreen != null) return;
        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockState state = client.world.getBlockState(blockHit.getBlockPos());
        Block block = state.getBlock();
        Text name;
        String hint;
        if (block instanceof TardisPanelBlock panel) {
            TardisPanelLayout.Button button = panel.buttonAt(state, blockHit.getBlockPos(), blockHit.getPos());
            if (button == null) {
                name = block.getName();
                hint = null;
            } else {
                name = Text.translatable("tardis.gallifrey.button." + button.id());
                hint = button.action().cycles() ? "tardis.gallifrey.hint.cycle" : "tardis.gallifrey.hint.use";
            }
        } else if (block instanceof TardisThrottleBlock || block instanceof TardisMonitorBlock) {
            name = block.getName();
            hint = "tardis.gallifrey.hint.use";
        } else {
            return;
        }
        int x = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() / 2 + 10;
        context.drawCenteredTextWithShadow(client.textRenderer, name, x, y, 0xFFE8C860);
        if (hint != null) context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable(hint), x, y + 11, 0xFFB0B0B0);
    }
}
