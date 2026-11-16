package com.cerulixteam.gallifrey.client.backport;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * GeckoLib plumbing for the backported mobs: they use Mojang's Bedrock models, animated in code (Bedrock's
 * animation files lean on Molang queries GeckoLib doesn't have), with overlay layers for eyes, armour and
 * so on. Cows, pigs and chickens of the warm and cold kinds use the same through GeoReplacedEntityRenderer.
 */
public final class BackportGeo {
    private BackportGeo() {}

    static final Identifier EMPTY_ANIMATION = new Identifier("gallifrey", "animations/backport/empty.animation.json");

    public static Identifier geo(String name) {
        return new Identifier("gallifrey", "geo/backport/" + name + ".geo.json");
    }

    public static Identifier tex(String path) {
        return new Identifier("gallifrey", "textures/entity/backport/" + path + ".png");
    }

    /** What an animator gets each frame. Angles are in degrees. */
    public record Pose(LivingEntity entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch, float partial) {
        public float walk(float speed, float degrees, float phase) {
            return MathHelper.cos(this.limbSwing * speed + phase) * degrees * this.limbAmount;
        }
    }

    public interface Animator {
        void animate(Bones bones, Pose pose);
    }

    /** Bone access for animators. Rotations are added to the model's own rest pose. */
    public static final class Bones {
        private final GeoModel<?> model;

        Bones(GeoModel<?> model) {
            this.model = model;
        }

        @Nullable
        public GeoBone get(String name) {
            return this.model.getBone(name).orElse(null);
        }

        public void rot(String name, float x, float y, float z) {
            GeoBone b = this.get(name);
            if (b == null) return;
            b.setRotX(b.getInitialSnapshot().getRotX() + x * MathHelper.RADIANS_PER_DEGREE);
            b.setRotY(b.getInitialSnapshot().getRotY() + y * MathHelper.RADIANS_PER_DEGREE);
            b.setRotZ(b.getInitialSnapshot().getRotZ() + z * MathHelper.RADIANS_PER_DEGREE);
        }

        public void pos(String name, float x, float y, float z) {
            GeoBone b = this.get(name);
            if (b == null) return;
            b.setPosX(b.getInitialSnapshot().getOffsetX() + x);
            b.setPosY(b.getInitialSnapshot().getOffsetY() + y);
            b.setPosZ(b.getInitialSnapshot().getOffsetZ() + z);
        }

        public void scale(String name, float x, float y, float z) {
            GeoBone b = this.get(name);
            if (b == null) return;
            b.setScaleX(x);
            b.setScaleY(y);
            b.setScaleZ(z);
        }

        public void hide(String name, boolean hidden) {
            GeoBone b = this.get(name);
            if (b != null) b.setHidden(hidden);
        }

        public void head(String name, Pose pose) {
            this.rot(name, pose.headPitch(), pose.headYaw(), 0.0F);
        }

        /** Four legs, diagonal pairs in step: front-right, front-left, hind-right, hind-left. */
        public void quadruped(Pose pose, String fr, String fl, String hr, String hl) {
            float a = pose.walk(0.6662F, 80.0F, 0.0F);
            this.rot(fr, -a, 0, 0);
            this.rot(hl, -a, 0, 0);
            this.rot(fl, a, 0, 0);
            this.rot(hr, a, 0, 0);
        }

        public void biped(Pose pose, String rightArm, String leftArm, String rightLeg, String leftLeg, float armDegrees) {
            float a = pose.walk(0.6662F, 80.0F, 0.0F);
            float arm = pose.walk(0.6662F, armDegrees, 0.0F);
            this.rot(rightLeg, -a, 0, 0);
            this.rot(leftLeg, a, 0, 0);
            this.rot(rightArm, arm, 0, 0);
            this.rot(leftArm, -arm, 0, 0);
        }
    }

    // =========================================================
    // MODEL
    // =========================================================

    public static class Model<T extends GeoAnimatable> extends GeoModel<T> {
        private final Function<LivingEntity, String> geo;
        private final Function<LivingEntity, Identifier> texture;
        private final Animator animator;
        private final boolean translucent;
        private final Bones bones = new Bones(this);
        /** The entity being drawn, for replaced entities (whose animatable is a shared object). */
        LivingEntity current;

        public Model(Function<LivingEntity, String> geo, Function<LivingEntity, Identifier> texture, Animator animator, boolean translucent) {
            this.geo = geo;
            this.texture = texture;
            this.animator = animator;
            this.translucent = translucent;
        }

        public Model(String geo, Function<LivingEntity, Identifier> texture, Animator animator) {
            this(e -> geo, texture, animator, false);
        }

        LivingEntity entity(T animatable) {
            return animatable instanceof LivingEntity living ? living : this.current;
        }

        @Override
        public Identifier getModelResource(T animatable) {
            return geo(this.geo.apply(this.entity(animatable)));
        }

        @Override
        public Identifier getTextureResource(T animatable) {
            return this.texture.apply(this.entity(animatable));
        }

        @Override
        public Identifier getAnimationResource(T animatable) {
            return EMPTY_ANIMATION;
        }

        @Override
        public RenderLayer getRenderType(T animatable, Identifier texture) {
            return this.translucent ? RenderLayer.getEntityTranslucent(texture) : RenderLayer.getEntityCutoutNoCull(texture);
        }

        @Override
        public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> state) {
            LivingEntity entity = this.entity(animatable);
            EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
            if (entity == null || data == null) return;
            float age = entity.age + state.getPartialTick();
            this.animator.animate(this.bones, new Pose(entity, state.getLimbSwing(), state.getLimbSwingAmount(), age,
                    data.netHeadYaw(), data.headPitch(), state.getPartialTick()));
        }
    }

    // =========================================================
    // LAYERS
    // =========================================================

    public enum Kind { CUTOUT, TRANSLUCENT, EYES }

    /**
     * Draws the mob again with another texture (and optionally another Bedrock geometry that shares its bone
     * names, posed to match). The texture function returns null to skip.
     */
    public static class Overlay<T extends GeoAnimatable> extends GeoRenderLayer<T> {
        private final Function<LivingEntity, Identifier> texture;
        @Nullable private final Function<LivingEntity, String> geo;
        private final Kind kind;
        private final ToIntFunction<LivingEntity> color;

        public Overlay(GeoRenderer<T> renderer, Function<LivingEntity, Identifier> texture, @Nullable Function<LivingEntity, String> geo, Kind kind,
                       ToIntFunction<LivingEntity> color) {
            super(renderer);
            this.texture = texture;
            this.geo = geo;
            this.kind = kind;
            this.color = color;
        }

        public Overlay(GeoRenderer<T> renderer, Function<LivingEntity, Identifier> texture, @Nullable String geo, Kind kind) {
            this(renderer, texture, geo == null ? null : e -> geo, kind, e -> 0xFFFFFF);
        }

        @Override
        public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource,
                           VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            LivingEntity entity = animatable instanceof LivingEntity living ? living
                    : this.getGeoModel() instanceof Model<T> model ? model.current : null;
            if (entity == null || entity.isInvisible()) return;
            Identifier tex = this.texture.apply(entity);
            if (tex == null) return;
            BakedGeoModel model = bakedModel;
            if (this.geo != null) {
                String name = this.geo.apply(entity);
                if (name == null) return;
                model = GeckoLibCache.getBakedModels().get(geo(name));
                if (model == null) return;
                for (GeoBone bone : model.topLevelBones()) copyPose(bone, bakedModel);
            }
            RenderLayer layer = switch (this.kind) {
                case CUTOUT -> RenderLayer.getEntityCutoutNoCull(tex);
                case TRANSLUCENT -> RenderLayer.getEntityTranslucent(tex);
                case EYES -> RenderLayer.getEyes(tex);
            };
            int rgb = this.color.applyAsInt(entity);
            float r = ((rgb >> 16) & 255) / 255.0F, g = ((rgb >> 8) & 255) / 255.0F, b = (rgb & 255) / 255.0F;
            this.getRenderer().reRender(model, poseStack, bufferSource, animatable, layer, bufferSource.getBuffer(layer), partialTick,
                    this.kind == Kind.EYES ? 0xF00000 : packedLight, packedOverlay, r, g, b, 1.0F);
        }

        private static void copyPose(GeoBone bone, BakedGeoModel from) {
            from.getBone(bone.getName()).ifPresent(src -> {
                bone.setRotX(src.getRotX());
                bone.setRotY(src.getRotY());
                bone.setRotZ(src.getRotZ());
                bone.setPosX(src.getPosX());
                bone.setPosY(src.getPosY());
                bone.setPosZ(src.getPosZ());
                bone.setScaleX(src.getScaleX());
                bone.setScaleY(src.getScaleY());
                bone.setScaleZ(src.getScaleZ());
                bone.setHidden(src.isHidden());
            });
            for (GeoBone child : bone.getChildBones()) copyPose(child, from);
        }
    }

    // =========================================================
    // RENDERERS
    // =========================================================

    /** For the new mobs (GeoEntity). */
    public static class Renderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {
        private Function<T, Float> scale = e -> e.isBaby() ? 0.5F : 1.0F;

        public Renderer(EntityRendererFactory.Context ctx, Model<T> model, float shadow) {
            super(ctx, model);
            this.shadowRadius = shadow;
        }

        public Renderer<T> scale(Function<T, Float> scale) {
            this.scale = scale;
            return this;
        }

        public Renderer<T> overlay(Function<LivingEntity, Identifier> texture, @Nullable String geo, Kind kind) {
            this.addRenderLayer(new Overlay<>(this, texture, geo, kind));
            return this;
        }

        public Renderer<T> overlay(Overlay<T> layer) {
            this.addRenderLayer(layer);
            return this;
        }

        @Override
        public void scaleModelForRender(float widthScale, float heightScale, MatrixStack poseStack, T animatable, BakedGeoModel model, boolean isReRender,
                                        float partialTick, int packedLight, int packedOverlay) {
            float s = this.scale.apply(animatable);
            super.scaleModelForRender(widthScale * s, heightScale * s, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        }
    }

    /** The shared animatable behind a replaced vanilla animal (cows, pigs and chickens of the warm and cold kinds). */
    public static final class Replaced implements GeoReplacedEntity {
        private final net.minecraft.entity.EntityType<?> type;
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        public Replaced(net.minecraft.entity.EntityType<?> type) {
            this.type = type;
        }

        @Override
        public net.minecraft.entity.EntityType<?> getReplacingEntityType() {
            return this.type;
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }

    public static class ReplacedRenderer<E extends AnimalEntity> extends GeoReplacedEntityRenderer<E, Replaced> {
        private final Model<Replaced> model;

        public ReplacedRenderer(EntityRendererFactory.Context ctx, Model<Replaced> model, Replaced animatable, float shadow) {
            super(ctx, model, animatable);
            this.model = model;
            this.shadowRadius = shadow;
        }

        public ReplacedRenderer<E> overlay(Function<LivingEntity, Identifier> texture, @Nullable String geo, Kind kind) {
            this.addRenderLayer(new Overlay<>(this, texture, geo, kind));
            return this;
        }

        @Override
        public void render(E entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
            this.model.current = entity;
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }

        @Override
        public void scaleModelForRender(float widthScale, float heightScale, MatrixStack poseStack, Replaced animatable, BakedGeoModel model, boolean isReRender,
                                        float partialTick, int packedLight, int packedOverlay) {
            float s = this.model.current != null && this.model.current.isBaby() ? 0.5F : 1.0F;
            super.scaleModelForRender(widthScale * s, heightScale * s, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        }
    }
}
