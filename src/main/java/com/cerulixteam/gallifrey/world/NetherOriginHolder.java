package com.cerulixteam.gallifrey.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Added to every entity by NetherPortalReturnMixin: where it came from when it went through a
 * portal, so the portal back takes it home instead of always to the Overworld.
 *  - nether origin: the dimension it last entered the Nether from (null = the Overworld)
 *  - portal origins: for each White Point Star portal dimension (Gallifrey, Mars, ...), the
 *    dimension it entered that dimension from (see CustomPortalReturnMixin)
 */
public interface NetherOriginHolder {
    @Nullable RegistryKey<World> gallifrey$getNetherOrigin();

    void gallifrey$setNetherOrigin(@Nullable RegistryKey<World> origin);

    /** Portal destination dimension -> the dimension the entity came from. */
    Map<RegistryKey<World>, RegistryKey<World>> gallifrey$getPortalOrigins();
}
