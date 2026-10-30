package com.cerulixteam.gallifrey.world.tree.decorator;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

/**
 * Gallifrey's own tree decorators. Must be registered from GallifreyMod.onInitialize(),
 * before any world loads, or the configured feature JSON that uses them won't parse.
 */
public final class ModTreeDecorators {
    private ModTreeDecorators() {}

    public static final TreeDecoratorType<PrehistoricTrunkVineTreeDecorator> PREHISTORIC_TRUNK_VINE =
            Registry.register(Registries.TREE_DECORATOR_TYPE, GallifreyMod.id("prehistoric_trunk_vine"),
                    new TreeDecoratorType<>(PrehistoricTrunkVineTreeDecorator.CODEC));

    public static final TreeDecoratorType<PrehistoricLeavesVineTreeDecorator> PREHISTORIC_LEAVES_VINE =
            Registry.register(Registries.TREE_DECORATOR_TYPE, GallifreyMod.id("prehistoric_leaves_vine"),
                    new TreeDecoratorType<>(PrehistoricLeavesVineTreeDecorator.CODEC));

    /** Call once from GallifreyMod.onInitialize() to trigger registration. */
    public static void register() {
        GallifreyMod.LOGGER.debug("[Gallifrey] Tree decorators registered.");
    }
}
