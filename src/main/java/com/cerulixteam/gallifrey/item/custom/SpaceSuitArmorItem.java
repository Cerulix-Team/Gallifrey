
package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.client.SpaceSuitArmorRenderer;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpaceSuitArmorItem extends ArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private final Supplier<Object> renderProvider =
            GeoItem.makeRenderer(this);

    public SpaceSuitArmorItem(
            ArmorMaterial material,
            Type type,
            Item.Settings settings
    ) {
        super(material, type, settings);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {

            private SpaceSuitArmorRenderer renderer;

            @Override
            public BipedEntityModel<net.minecraft.entity.LivingEntity>
            getHumanoidArmorModel(
                    net.minecraft.entity.LivingEntity livingEntity,
                    net.minecraft.item.ItemStack itemStack,
                    EquipmentSlot equipmentSlot,
                    BipedEntityModel<net.minecraft.entity.LivingEntity> original
            ) {
                if (renderer == null) {
                    renderer = new SpaceSuitArmorRenderer();
                }

                renderer.prepForRender(
                        livingEntity,
                        itemStack,
                        equipmentSlot,
                        original
                );

                return renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        // The Space Suit does not need animation controllers yet.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

