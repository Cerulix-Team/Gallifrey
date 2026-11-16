package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Gives special items to specific players (team, supporters) the first time they join a world. */
public final class UuidRewards {
    private UuidRewards() {}

    private static final UUID BLUE = UUID.fromString( "d21bc86a-14ce-4d4b-bc15-62f6a4c58fe3");
    private static final UUID DEO = UUID.fromString(  "c36e27d8-1658-4465-b8ed-29ff2bc622ec");
    private static final UUID STEVE = UUID.fromString("d1700a3f-b346-4edc-9aad-a6a499682982");

    /** Everyone on the list gets these. */
    private static final Supplier<List<Item>> SHARED = () -> List.of(
            GallifreyModItems.VORTEX_MANIPULATOR,
            GallifreyModItems.SONIC_SCREWDRIVER,
            GallifreyModBlocks.SONIC_WORKSHOP.asItem()
    );

    /** Extra items for each person. */
    private static final Map<UUID, Supplier<List<Item>>> EXTRAS = Map.of(
            BLUE, () -> List.of(
                    GallifreyModItems.SONIC_SHADES,
                    GallifreyModItems.DARKBLUEFEZ,
                    GallifreyModItems.TRUSTABLE_HAT,
                    GallifreyModItems.DIAMOND_MINECART
            ),

            STEVE, () -> List.of(
                    GallifreyModItems.BLUEFEZ,
                    GallifreyModItems.TRUSTABLE_HAT
            ),

            DEO, () -> List.of(
                    GallifreyModItems.SONIC_SHADES,
                    GallifreyModItems.FANCYFEZ,
                    GallifreyModItems.SPACE_SUIT_HELMET,
                    GallifreyModItems.SPACE_SUIT_CHESTPLATE,
                    GallifreyModItems.SPACE_SUIT_LEGGINGS,
                    GallifreyModItems.SPACE_SUIT_BOOTS
            )
    );

    /** Stored on the player so they only get it once per world. */
    private static final String GIVEN_TAG = "gallifrey_uuid_reward";

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Supplier<List<Item>> extras = EXTRAS.get(player.getUuid());
            if (extras == null || player.getCommandTags().contains(GIVEN_TAG)) return;

            List<Item> items = new ArrayList<>(SHARED.get());
            items.addAll(extras.get());
            for (Item item : items) {
                ItemStack stack = new ItemStack(item);
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false); // inventory full: drop it at their feet
                }
            }
            player.addCommandTag(GIVEN_TAG);
            player.sendMessage(Text.literal("§bThanks for being part of Gallifrey!"), false);
        });
    }
}