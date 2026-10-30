package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.client.GeoHeadwearRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A helmet-slot item worn as a GeckoLib 3D model.
 *
 * Reusable: every GeckoLib hat can be an instance of this class. The model name
 * picks the files:
 *   assets/gallifrey/geo/item/armor/<modelName>.geo.json
 *   assets/gallifrey/textures/item/armor/<modelName>.png
 *
 * No animation controllers, so no .animation.json is needed. To animate one later,
 * add a controller in registerControllers() and the matching animation file.
 */
public class GeoHeadwearItem extends ArmorItem implements GeoItem {

    /** Default hat scale: clears the head and the skin's hat layer. See GeoHeadwearRenderer. */
    public static final float DEFAULT_HEAD_SCALE = 1.2F;

    private final String modelName;
    private final float headScale;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public GeoHeadwearItem(String modelName, ArmorMaterial material, Settings settings) {
        this(modelName, DEFAULT_HEAD_SCALE, material, settings);
    }

    /**
     * @param headScale 1.0 = exactly as modelled in Blockbench. Use 1.0 for a model
     *                  that already sits clear of the head (at +-4.6 or further out).
     */
    public GeoHeadwearItem(String modelName, float headScale, ArmorMaterial material, Settings settings) {
        super(material, ArmorItem.Type.HELMET, settings);
        this.modelName = modelName;
        this.headScale = headScale;
    }

    public String getModelName() {
        return this.modelName;
    }

    // Only ever called on the client, so the client-only renderer class is never
    // loaded on a dedicated server. This is GeckoLib's standard pattern.
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoHeadwearRenderer renderer;

            @Override
            public BipedEntityModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                         EquipmentSlot equipmentSlot, BipedEntityModel<LivingEntity> original) {
                if (this.renderer == null) {
                    this.renderer = new GeoHeadwearRenderer(GeoHeadwearItem.this.modelName, GeoHeadwearItem.this.headScale);
                }
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model: no animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
