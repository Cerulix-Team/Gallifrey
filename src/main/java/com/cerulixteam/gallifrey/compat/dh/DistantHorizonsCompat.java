package com.cerulixteam.gallifrey.compat.dh;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.cerulixteam.gallifrey.GallifreyMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/**
 * Distant Horizons support. DH draws simplified far-away terrain past the normal render distance.
 * Mars and Lost Reality limit how far you can see with thick fog (96 blocks on a clear Martian
 * day, much less in a sandstorm, 40 in Lost Reality), so DH's far terrain showed up as a sharp
 * landscape floating behind the fog. In those two dimensions DH's far terrain is switched off;
 * everywhere else DH works as normal.
 *
 * Only GallifreyModClient may reference this class, and only after checking DH is installed:
 * it uses DH's API classes, which only exist when DH is. build.gradle compiles against DH
 * without making it a dependency.
 */
public final class DistantHorizonsCompat {
    private DistantHorizonsCompat() {}

    /** Only call when Distant Horizons is installed (GallifreyModClient checks). */
    public static void register() {
        DhApi.events.bind(DhApiBeforeRenderEvent.class, new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
                if (isFoggyDimension()) event.cancelEvent();
            }
        });
    }

    private static boolean isFoggyDimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        Identifier id = client.world.getRegistryKey().getValue();
        return GallifreyMod.MOD_ID.equals(id.getNamespace())
                && (id.getPath().equals("mars") || id.getPath().equals("lost_reality"));
    }
}
