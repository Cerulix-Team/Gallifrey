package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.NetherOriginHolder;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Vanilla nether portals always lead from the Nether to the Overworld. This remembers which
 * dimension an entity came into the Nether from (Pete's World, say) and sends it back there,
 * so a portal built in Pete's World works both ways. Coming from the Overworld works as normal.
 *
 * The Classic dimensions have their own portal handling (ClassicNetherPortalMixin), which runs
 * first and never reaches this.
 */
@Mixin(Entity.class)
public abstract class NetherPortalReturnMixin implements NetherOriginHolder {

    @Unique private static final String NBT_KEY = "GallifreyNetherOrigin";
    @Unique private static final String PORTAL_NBT_KEY = "GallifreyPortalOrigins";

    @Unique private RegistryKey<World> gallifrey$netherOrigin;
    /** Custom portal destination -> where this entity came from (CustomPortalReturnMixin). */
    @Unique private final Map<RegistryKey<World>, RegistryKey<World>> gallifrey$portalOrigins = new HashMap<>();

    @Shadow public abstract World getWorld();

    @ModifyVariable(method = "tickPortal", at = @At("STORE"), ordinal = 0)
    private RegistryKey<World> gallifrey$portalDestination(RegistryKey<World> destination) {
        if (!(this.getWorld() instanceof ServerWorld world)) return destination;
        RegistryKey<World> here = world.getRegistryKey();

        if (here == World.NETHER) {
            // Leaving the Nether: go back where we came from, if that world still exists.
            if (gallifrey$netherOrigin != null && world.getServer().getWorld(gallifrey$netherOrigin) != null) {
                return gallifrey$netherOrigin;
            }
            return destination;
        }

        // Heading into the Nether: remember where from (null = the Overworld, vanilla's default).
        gallifrey$netherOrigin = here == World.OVERWORLD ? null : here;
        return destination;
    }

    @Override
    public RegistryKey<World> gallifrey$getNetherOrigin() {
        return gallifrey$netherOrigin;
    }

    @Override
    public void gallifrey$setNetherOrigin(RegistryKey<World> origin) {
        gallifrey$netherOrigin = origin;
    }

    @Override
    public Map<RegistryKey<World>, RegistryKey<World>> gallifrey$getPortalOrigins() {
        return gallifrey$portalOrigins;
    }

    // Saved with the entity, so it survives relogging and entities changing dimension.
    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void gallifrey$saveNetherOrigin(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (gallifrey$netherOrigin != null) {
            nbt.putString(NBT_KEY, gallifrey$netherOrigin.getValue().toString());
        }
        if (!gallifrey$portalOrigins.isEmpty()) {
            NbtCompound origins = new NbtCompound();
            gallifrey$portalOrigins.forEach((dest, origin) ->
                    origins.putString(dest.getValue().toString(), origin.getValue().toString()));
            nbt.put(PORTAL_NBT_KEY, origins);
        }
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void gallifrey$loadNetherOrigin(NbtCompound nbt, CallbackInfo ci) {
        Identifier id = nbt.contains(NBT_KEY) ? Identifier.tryParse(nbt.getString(NBT_KEY)) : null;
        gallifrey$netherOrigin = id != null ? RegistryKey.of(RegistryKeys.WORLD, id) : null;

        gallifrey$portalOrigins.clear();
        NbtCompound origins = nbt.getCompound(PORTAL_NBT_KEY);
        for (String dest : origins.getKeys()) {
            Identifier destId = Identifier.tryParse(dest);
            Identifier originId = Identifier.tryParse(origins.getString(dest));
            if (destId != null && originId != null) {
                gallifrey$portalOrigins.put(RegistryKey.of(RegistryKeys.WORLD, destId),
                        RegistryKey.of(RegistryKeys.WORLD, originId));
            }
        }
    }
}
