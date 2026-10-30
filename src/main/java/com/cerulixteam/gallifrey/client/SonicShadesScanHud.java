package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.custom.GallifreyAccessories;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;
import com.cerulixteam.gallifrey.item.custom.SonicShadesItem;
import com.cerulixteam.gallifrey.sonic.SonicMode;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * While the worn Sonic Shades are in Scanning mode, shows a live readout of whatever you're looking
 * at (up to 16 blocks, the same targeting as the sonic itself), under the spinning symbol.
 *
 * Blocks: name, ID, position, distance, hardness, blast resistance, the tool it needs, light level
 * and up to four of its block-state properties. Entities: name, type, health, armour, distance and
 * whether it's hostile, a baby, or tamed.
 *
 * Everything is read on the client, so nothing extra is sent over the network. The scan is redone
 * once per tick and only drawn each frame.
 */
public final class SonicShadesScanHud {
    private SonicShadesScanHud() {}

    private static final int MAX_PROPERTIES = 4;
    private static List<Text> lines = Collections.emptyList();

    /** Call once per client tick. */
    public static void tick(MinecraftClient client) {
        lines = scan(client);
    }

    /** Draws the readout with its right edge at {@code right}, so it sits under the top-right symbol. */
    public static void render(DrawContext context, int right, int y) {
        if (lines.isEmpty()) return;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int width = 0;
        for (Text line : lines) width = Math.max(width, font.getWidth(line));
        int x = right - width;
        int height = lines.size() * 10;
        context.fill(x - 3, y - 3, x + width + 3, y + height + 1, 0x90000814); // dark blue-black panel
        context.fill(x - 3, y - 3, x + width + 3, y - 2, 0xFF2E9BE6);           // sonic-blue top edge
        for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(font, lines.get(i), x, y + i * 10, 0xFFFFFF);
        }
    }

    private static List<Text> scan(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null || player.isSpectator()) return Collections.emptyList();
        ItemStack shades = GallifreyAccessories.getShades(player);
        if (!(shades.getItem() instanceof SonicShadesItem)) return Collections.emptyList();
        // Read the mode from a copy: getMode adds an empty tag if there isn't one.
        if (SonicScrewdriver.getMode(shades.copy()) != SonicMode.SCAN) return Collections.emptyList();

        HitResult target = SonicScrewdriver.getSonicTarget(player);
        List<Text> out = new ArrayList<>();
        out.add(Text.literal("SONIC SCAN").formatted(Formatting.AQUA, Formatting.BOLD));

        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            describeEntity(player, entityHit.getEntity(), out);
        } else if (target instanceof BlockHitResult blockHit && target.getType() == HitResult.Type.BLOCK) {
            describeBlock(client, player, blockHit, out);
        } else {
            out.add(Text.literal("Nothing detected").formatted(Formatting.GRAY));
        }
        return out;
    }

    private static void describeBlock(MinecraftClient client, PlayerEntity player, BlockHitResult hit, List<Text> out) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        out.add(state.getBlock().getName().copy().formatted(Formatting.YELLOW));
        out.add(row("ID", Registries.BLOCK.getId(state.getBlock()).toString()));
        out.add(row("Position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        out.add(row("Distance", String.format("%.1f blocks", Math.sqrt(player.getEyePos().squaredDistanceTo(hit.getPos())))));

        float hardness = state.getHardness(client.world, pos);
        out.add(row("Hardness", hardness < 0 ? "Unbreakable" : String.format("%.1f", hardness)));
        out.add(row("Blast resistance", String.format("%.1f", state.getBlock().getBlastResistance())));
        out.add(row("Tool", toolFor(state)));

        // Light on the face you're looking at (the block itself is usually solid and dark inside).
        BlockPos lit = pos.offset(hit.getSide());
        int block = client.world.getLightLevel(LightType.BLOCK, lit);
        int sky = client.world.getLightLevel(LightType.SKY, lit);
        out.add(row("Light", "block " + block + ", sky " + sky));

        int shown = 0;
        for (Property<?> property : state.getProperties()) {
            if (shown++ >= MAX_PROPERTIES) break;
            out.add(row(property.getName(), valueName(state, property)));
        }
    }

    private static void describeEntity(PlayerEntity player, Entity entity, List<Text> out) {
        out.add(entity.getDisplayName().copy().formatted(Formatting.YELLOW));
        out.add(row("Type", Registries.ENTITY_TYPE.getId(entity.getType()).toString()));
        out.add(row("Distance", String.format("%.1f blocks", player.distanceTo(entity))));
        if (entity instanceof LivingEntity living) {
            out.add(row("Health", String.format("%.1f / %.1f", living.getHealth(), living.getMaxHealth())));
            if (living.getArmor() > 0) out.add(row("Armour", String.valueOf(living.getArmor())));
            if (living.isBaby()) out.add(Text.literal("Baby").formatted(Formatting.GREEN));
        }
        if (entity instanceof Monster) out.add(Text.literal("Hostile").formatted(Formatting.RED));
        if (entity instanceof TameableEntity tameable && tameable.isTamed()) {
            out.add(Text.literal("Tamed").formatted(Formatting.GREEN));
        }
    }

    private static String toolFor(BlockState state) {
        List<String> tools = new ArrayList<>();
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) tools.add("Pickaxe");
        if (state.isIn(BlockTags.AXE_MINEABLE)) tools.add("Axe");
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) tools.add("Shovel");
        if (state.isIn(BlockTags.HOE_MINEABLE)) tools.add("Hoe");
        if (tools.isEmpty()) return "Any";
        String tier = state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) ? "Diamond "
                : state.isIn(BlockTags.NEEDS_IRON_TOOL) ? "Iron "
                : state.isIn(BlockTags.NEEDS_STONE_TOOL) ? "Stone " : "";
        return tier + String.join(" / ", tools);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }

    private static Text row(String label, String value) {
        return Text.literal(label + ": ").formatted(Formatting.WHITE)
                .append(Text.literal(value).formatted(Formatting.GRAY));
    }
}
