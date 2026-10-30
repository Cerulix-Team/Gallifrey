package com.cerulixteam.gallifrey.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class GallifreyMixinPlugin implements IMixinConfigPlugin {

    private static final String TITLE_SCREEN_MIXIN =
            "client.TitleScreenVortexMixin";

    @Override
    public void onLoad(String mixinPackage) {
        System.out.println(
                "[Gallifrey] Mixin plugin loaded: " + mixinPackage
        );
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(
            String targetClassName,
            String mixinClassName
    ) {
        if (mixinClassName.endsWith(TITLE_SCREEN_MIXIN)) {
            System.out.println(
                    "[Gallifrey] Title screen mixin requested for "
                            + targetClassName
            );
        }

        // Allow Gallifrey's mixins to apply even when AiT is installed.
        return true;
    }

    @Override
    public void acceptTargets(
            Set<String> myTargets,
            Set<String> otherTargets
    ) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
        if (mixinClassName.endsWith(TITLE_SCREEN_MIXIN)) {
            System.out.println(
                    "[Gallifrey] Applying title screen mixin to "
                            + targetClassName
            );
        }
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
        if (mixinClassName.endsWith(TITLE_SCREEN_MIXIN)) {
            System.out.println(
                    "[Gallifrey] Title screen mixin applied to "
                            + targetClassName
            );
        }
    }
}