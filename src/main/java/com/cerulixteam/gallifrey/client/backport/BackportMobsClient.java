package com.cerulixteam.gallifrey.client.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import com.cerulixteam.gallifrey.block.backport.mob.ArmadilloEntity;
import com.cerulixteam.gallifrey.block.backport.mob.BackportMobs;
import com.cerulixteam.gallifrey.block.backport.mob.BackportSkeletonEntity;
import com.cerulixteam.gallifrey.block.backport.mob.BreezeEntity;
import com.cerulixteam.gallifrey.block.backport.mob.CopperGolemEntity;
import com.cerulixteam.gallifrey.block.backport.mob.CreakingEntity;
import com.cerulixteam.gallifrey.block.backport.mob.HappyGhastEntity;
import com.cerulixteam.gallifrey.block.backport.mob.NautilusEntity;
import com.cerulixteam.gallifrey.block.backport.mob.SulfurCubeEntity;
import com.cerulixteam.gallifrey.block.backport.mob.WolfArmorItem;
import com.cerulixteam.gallifrey.client.backport.BackportGeo.Kind;
import com.cerulixteam.gallifrey.client.backport.BackportGeo.Model;
import com.cerulixteam.gallifrey.client.backport.BackportGeo.Renderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.CamelEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.Map;

/** Renderers and item colours for the backported mobs (see BackportMobs). */
public final class BackportMobsClient {
    private BackportMobsClient() {}

    static final EntityModelLayer WOLF_ARMOR = new EntityModelLayer(new Identifier("gallifrey", "wolf_armor"), "main");
    static final String[] NAUTILUS_ARMOR = {"copper", "iron", "gold", "diamond", "netherite"};
    static final String[] OXIDATION = {"", "_exposed", "_weathered", "_oxidized"};
    static final String[] STATUE_POSES = {"copper_golem", "copper_golem_running", "copper_golem_sitting", "copper_golem_star"};

    /** Renderers for warm and cold cows, pigs and chickens, built along with the vanilla ones. */
    static final Map<EntityType<?>, BackportGeo.ReplacedRenderer<?>> FARM = new HashMap<>();

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(WOLF_ARMOR, BackportMobsClient::wolfArmorModel);
        net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
                com.cerulixteam.gallifrey.block.backport.trial.BackportTrials.VAULT_ENTITY, VaultRenderer::new);
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.render.RenderLayer.getCutout(),
                com.cerulixteam.gallifrey.block.backport.trial.BackportTrials.TRIAL_SPAWNER, com.cerulixteam.gallifrey.block.backport.trial.BackportTrials.VAULT,
                com.cerulixteam.gallifrey.block.backport.trial.BackportTrials.GALLIFREYAN_VAULT);

        EntityRendererRegistry.register(BackportMobs.BREEZE, ctx -> new Renderer<BreezeEntity>(ctx, new Model<>("breeze", e -> BackportGeo.tex("breeze/breeze"),
                BackportMobsClient::breeze), 0.6F)
                .overlay(e -> BackportGeo.tex("breeze/breeze_eyes"), "breeze_eyes", Kind.EYES)
                .overlay(e -> BackportGeo.tex("breeze/breeze_wind"), "breeze_wind_bottom", Kind.TRANSLUCENT)
                .overlay(e -> BackportGeo.tex("breeze/breeze_wind"), "breeze_wind_mid", Kind.TRANSLUCENT)
                .overlay(e -> BackportGeo.tex("breeze/breeze_wind"), "breeze_wind_top", Kind.TRANSLUCENT));
        EntityRendererRegistry.register(BackportMobs.ARMADILLO, ctx -> new Renderer<ArmadilloEntity>(ctx, new Model<>("armadillo",
                e -> BackportGeo.tex("armadillo/armadillo"), BackportMobsClient::armadillo), 0.4F).scale(e -> e.isBaby() ? 0.6F : 1.0F));
        EntityRendererRegistry.register(BackportMobs.CREAKING, ctx -> new Renderer<CreakingEntity>(ctx, new Model<>("creaking",
                e -> BackportGeo.tex("creaking/creaking"), BackportMobsClient::creaking), 0.6F)
                .overlay(e -> BackportGeo.tex("creaking/creaking_eyes"), null, Kind.EYES));
        EntityRendererRegistry.register(BackportMobs.HAPPY_GHAST, ctx -> new Renderer<HappyGhastEntity>(ctx, new Model<>("happy_ghast",
                e -> BackportGeo.tex(e.isBaby() ? "happy_ghast/ghastling" : "happy_ghast/happy_ghast"), BackportMobsClient::happyGhast), 2.0F)
                .scale(e -> e.isBaby() ? 0.2375F : 0.99F)
                .overlay(e -> ((HappyGhastEntity) e).getHarness() >= 0 && !e.isBaby()
                        ? BackportGeo.tex("harness/" + net.minecraft.util.DyeColor.byId(((HappyGhastEntity) e).getHarness()).getName() + "_harness") : null,
                        "harness", Kind.CUTOUT)
                .overlay(e -> ((HappyGhastEntity) e).getHarness() >= 0 && ((HappyGhastEntity) e).isLeashed() ? BackportGeo.tex("happy_ghast/happy_ghast_ropes") : null,
                        "happy_ghast_ropes", Kind.CUTOUT));
        EntityRendererRegistry.register(BackportMobs.COPPER_GOLEM, ctx -> new Renderer<CopperGolemEntity>(ctx, new Model<>(e -> ((CopperGolemEntity) e).isStatue()
                ? STATUE_POSES[((CopperGolemEntity) e).getStatuePose()] : "copper_golem",
                e -> BackportGeo.tex(((CopperGolemEntity) e).isGilded() ? "copper_golem/gilded_copper_golem"
                        : "copper_golem/copper_golem" + OXIDATION[((CopperGolemEntity) e).getOxidation()]), BackportMobsClient::copperGolem, false), 0.4F)
                .overlay(e -> ((CopperGolemEntity) e).hasFlower() ? BackportGeo.tex("copper_golem/copper_golem_flower") : null, "copper_golem_flower", Kind.CUTOUT)
                .overlay(e -> ((CopperGolemEntity) e).isStatue() ? null
                        : BackportGeo.tex("copper_golem/copper_golem_eyes" + OXIDATION[((CopperGolemEntity) e).getOxidation()]), null, Kind.EYES));
        EntityRendererFactory<NautilusEntity> nautilus = ctx -> new Renderer<NautilusEntity>(ctx, new Model<>("nautilus",
                e -> BackportGeo.tex(((NautilusEntity) e).isZombie() ? "nautilus/zombie_nautilus" : e.isBaby() ? "nautilus/nautilus_baby" : "nautilus/nautilus"),
                BackportMobsClient::nautilus), 0.5F)
                .overlay(e -> ((NautilusEntity) e).isSaddled() ? BackportGeo.tex("nautilus/nautilus_saddle") : null, "nautilus_saddle", Kind.CUTOUT)
                .overlay(e -> ((NautilusEntity) e).getArmor2() >= 0
                        ? BackportGeo.tex("nautilus/armor/nautilus_armor_" + NAUTILUS_ARMOR[((NautilusEntity) e).getArmor2()]) : null, "nautilus_armor", Kind.CUTOUT);
        EntityRendererRegistry.register(BackportMobs.NAUTILUS, nautilus);
        EntityRendererRegistry.register(BackportMobs.ZOMBIE_NAUTILUS, nautilus);
        EntityRendererRegistry.register(BackportMobs.SULFUR_CUBE, SulfurCubeRenderer::new);
        EntityRendererRegistry.register(BackportMobs.VARIANT_EGG, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(BackportMobs.TEMPORAL_SHARD_PROJECTILE, ctx -> new FlyingItemEntityRenderer<>(ctx, 1.0F, true));
        EntityRendererRegistry.register(BackportMobs.SHARDWARDEN, ctx -> new Renderer<com.cerulixteam.gallifrey.block.backport.mob.ShardwardenEntity>(ctx,
                new Model<>("shardwarden", e -> BackportGeo.tex("shardwarden/shardwarden"), BackportMobsClient::shardwarden), 0.5F)
                .scale(e -> 1.0F)
                .overlay(e -> BackportGeo.tex("shardwarden/shardwarden_glow"), null, Kind.EYES));

        EntityRendererRegistry.register(BackportMobs.BOGGED, ctx -> new BackportSkeletonRenderer(ctx, true));
        EntityRendererRegistry.register(BackportMobs.PARCHED, ctx -> new BackportSkeletonRenderer(ctx, false));
        EntityRendererRegistry.register(BackportMobs.CAMEL_HUSK, ctx -> new CamelEntityRenderer(ctx, EntityModelLayers.CAMEL) {
            @Override
            public Identifier getTexture(CamelEntity camel) {
                return BackportGeo.tex("camel_husk");
            }
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof WolfEntityRenderer wolves) {
                helper.register(new WolfArmorFeature(wolves, new WolfEntityModel<>(ctx.getPart(WOLF_ARMOR))));
            }
        });

        ModelPredicateProviderRegistry.register(BackportMobs.WOLF_ARMOR, new Identifier("gallifrey", "dyed"),
                (stack, world, entity, seed) -> ((DyeableItem) stack.getItem()).hasColor(stack) ? 1.0F : 0.0F);
        ColorProviderRegistry.ITEM.register((stack, tint) -> tint == 1 ? ((DyeableItem) stack.getItem()).getColor(stack) : -1, BackportMobs.WOLF_ARMOR);
    }

    /** Called by the farm animal render mixin; built lazily, since renderers need a factory context. */
    @SuppressWarnings("unchecked")
    public static <E extends AnimalEntity> BackportGeo.ReplacedRenderer<E> farm(E entity) {
        return (BackportGeo.ReplacedRenderer<E>) FARM.get(entity.getType());
    }

    /** Builds the farm animal renderers; the mixin calls this from the vanilla renderers' constructors. */
    public static void buildFarm(EntityRendererFactory.Context ctx, EntityType<?> type) {
        if (type == EntityType.COW) {
            FARM.put(type, new BackportGeo.ReplacedRenderer<>(ctx, new Model<>(e -> AnimalVariants.get(e) == AnimalVariants.WARM ? "cow_warm" : "cow_cold",
                    e -> BackportGeo.tex("cow/cow_" + AnimalVariants.FARM[AnimalVariants.get(e)]), BackportMobsClient::cow, false),
                    new BackportGeo.Replaced(type), 0.7F));
        } else if (type == EntityType.PIG) {
            FARM.put(type, new BackportGeo.ReplacedRenderer<PigEntity>(ctx, new Model<>(e -> "pig",
                    e -> BackportGeo.tex("pig/pig_" + AnimalVariants.FARM[AnimalVariants.get(e)]), BackportMobsClient::cow, false),
                    new BackportGeo.Replaced(type), 0.7F)
                    .overlay(e -> ((PigEntity) e).isSaddled() ? BackportGeo.tex("pig/pig_saddle") : null, null, Kind.CUTOUT));
        } else if (type == EntityType.CHICKEN) {
            FARM.put(type, new BackportGeo.ReplacedRenderer<>(ctx, new Model<>(e -> AnimalVariants.get(e) == AnimalVariants.COLD ? "chicken_cold" : "chicken",
                    e -> BackportGeo.tex("chicken/chicken_" + AnimalVariants.FARM[AnimalVariants.get(e)]), BackportMobsClient::chicken, false),
                    new BackportGeo.Replaced(type), 0.3F));
        }
    }

    // =========================================================
    // ANIMATIONS (degrees; Bedrock models, so X and Y run the other way from Java's)
    // =========================================================

    static void breeze(BackportGeo.Bones b, BackportGeo.Pose p) {
        b.head("head", p);
        b.rot("rods", 0, p.age() * 8.0F, 0);
        float bob = MathHelper.sin(p.age() * 0.1F);
        b.pos("body", 0, bob, 0);
        b.rot("tornado_bottom", 0, -p.age() * 20.0F, 0);
        b.rot("tornado_mid", 0, p.age() * 14.0F, 0);
        b.rot("tornado_top", 0, -p.age() * 10.0F, 0);
        b.pos("tornado_mid", MathHelper.cos(p.age() * 0.2F), 0, MathHelper.sin(p.age() * 0.2F));
        b.pos("tornado_top", MathHelper.cos(p.age() * 0.15F + 1.0F) * 1.5F, 0, MathHelper.sin(p.age() * 0.15F + 1.0F) * 1.5F);
    }

    /** Shardwarden (Gallifrey's own): the plates orbit the core, faster when it's hurt; the halo turns the other way. */
    static void shardwarden(BackportGeo.Bones b, BackportGeo.Pose p) {
        float hurt = p.entity().hurtTime > 0 ? 3.0F : 1.0F;
        b.rot("plates", 0, p.age() * 4.0F * hurt, 0);
        b.rot("halo", 0, -p.age() * 1.5F, MathHelper.sin(p.age() * 0.05F) * 6.0F);
        b.pos("core", 0, MathHelper.sin(p.age() * 0.1F) * 0.6F, 0);
        b.rot("core", 0, p.headYaw(), 0);
        b.rot("crown", 0, p.age() * 2.0F, 0);
        for (int i = 0; i < 4; i++) b.rot("plate" + i, MathHelper.sin(p.age() * 0.08F + i * 1.57F) * 12.0F, 0, 0);
    }

    static void armadillo(BackportGeo.Bones b, BackportGeo.Pose p) {
        boolean rolled = p.entity() instanceof ArmadilloEntity a && a.isRolledUp();
        for (String bone : new String[]{"body", "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"}) b.hide(bone, rolled);
        b.hide("body_rolled_up", !rolled);
        if (rolled) return;
        b.rot("head", MathHelper.clamp(p.headPitch(), -22.5F, 25.0F), MathHelper.clamp(p.headYaw(), -32.5F, 32.5F), 0);
        b.quadruped(p, "right_front_leg", "left_front_leg", "right_hind_leg", "left_hind_leg");
        b.rot("tail", 0, 0, p.walk(0.6662F, 20.0F, 0.0F));
    }

    static void creaking(BackportGeo.Bones b, BackportGeo.Pose p) {
        b.rot("head", 0, p.headYaw(), 0);
        b.biped(p, "rightArm", "leftArm", "rightLeg", "leftLeg", 50.0F);
    }

    static void happyGhast(BackportGeo.Bones b, BackportGeo.Pose p) {
        for (int i = 0; i < 9; i++) {
            float deg = MathHelper.sin((p.age() / 20.0F * 360.0F + i * 57.3F) * MathHelper.RADIANS_PER_DEGREE) * 11.5F + 23.0F;
            b.rot("tentacles_" + i, -deg, 0, 0);
        }
    }

    static void copperGolem(BackportGeo.Bones b, BackportGeo.Pose p) {
        if (p.entity() instanceof CopperGolemEntity golem && golem.isStatue()) return;
        b.head("head", p);
        b.biped(p, "right_arm", "left_arm", "right_leg", "left_leg", 60.0F);
    }

    static void nautilus(BackportGeo.Bones b, BackportGeo.Pose p) {
        float swim = MathHelper.sin(p.age() * 0.12F);
        b.rot("nautilus", MathHelper.clamp(p.headPitch(), -10, 10), 0, 0);
        b.pos("nautilus", 0, swim * 0.5F, 0);
        b.rot("mouth_top", -3.0F - swim * 4.0F, 0, 0);
        b.rot("mouth_bottom", 3.0F + swim * 4.0F, 0, 0);
    }

    static void cow(BackportGeo.Bones b, BackportGeo.Pose p) {
        b.head("head", p);
        float a = p.walk(0.6662F, 80.0F, 0.0F);
        b.rot("leg0", -a, 0, 0);
        b.rot("leg3", -a, 0, 0);
        b.rot("leg1", a, 0, 0);
        b.rot("leg2", a, 0, 0);
    }

    static void chicken(BackportGeo.Bones b, BackportGeo.Pose p) {
        b.head("head", p);
        float a = p.walk(0.6662F, 80.0F, 0.0F);
        b.rot("leg0", -a, 0, 0);
        b.rot("leg1", a, 0, 0);
        if (p.entity() instanceof ChickenEntity chicken) {
            float flap = MathHelper.lerp(p.partial(), chicken.prevFlapProgress, chicken.flapProgress);
            float spread = MathHelper.lerp(p.partial(), chicken.prevMaxWingDeviation, chicken.maxWingDeviation);
            float wing = (MathHelper.sin(flap) + 1.0F) * spread * MathHelper.DEGREES_PER_RADIAN;
            b.rot("wing0", 0, 0, wing);
            b.rot("wing1", 0, 0, -wing);
        }
    }

    // =========================================================
    // OTHER RENDERERS
    // =========================================================

    /** Bogged and Parched: Java's skeleton model with their own textures (and the Bogged's mossy outer layer). */
    static class BackportSkeletonRenderer extends SkeletonEntityRenderer {
        private final boolean bogged;

        BackportSkeletonRenderer(EntityRendererFactory.Context ctx, boolean bogged) {
            super(ctx, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
            this.bogged = bogged;
            if (bogged) {
                SkeletonEntityModel<AbstractSkeletonEntity> outer = new SkeletonEntityModel<>(ctx.getPart(EntityModelLayers.STRAY_OUTER));
                this.addFeature(new FeatureRenderer<>(this) {
                    @Override
                    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, AbstractSkeletonEntity entity, float limbAngle, float limbDistance,
                                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
                        if (entity instanceof BackportSkeletonEntity s && s.isSheared()) return;
                        render(this.getContextModel(), outer, BackportGeo.tex("skeleton/bogged_overlay"), matrices, consumers, light, entity, limbAngle, limbDistance,
                                animationProgress, headYaw, headPitch, tickDelta, 1.0F, 1.0F, 1.0F);
                    }
                });
            }
        }

        @Override
        public Identifier getTexture(AbstractSkeletonEntity entity) {
            return BackportGeo.tex(this.bogged ? "skeleton/bogged" : "skeleton/parched");
        }
    }

    /** Sulfur Cube: the inner cube, then its translucent shell, with whatever block it swallowed in the middle. */
    static class SulfurCubeRenderer extends Renderer<SulfurCubeEntity> {
        SulfurCubeRenderer(EntityRendererFactory.Context ctx) {
            super(ctx, new Model<>(e -> ((SulfurCubeEntity) e).isLarge() ? "sulfur_cube" : "sulfur_cube_small",
                    e -> BackportGeo.tex(((SulfurCubeEntity) e).isLarge() ? "sulfur_cube/sulfur_cube_inner" : "sulfur_cube/sulfur_cube_inner_small"),
                    (b, p) -> {}, false), 0.5F);
            this.scale(e -> 1.0F);
            this.overlay(e -> BackportGeo.tex(((SulfurCubeEntity) e).isLarge() ? "sulfur_cube/sulfur_cube_outer" : "sulfur_cube/sulfur_cube_outer_small"),
                    null, Kind.TRANSLUCENT);
        }

        @Override
        public void render(SulfurCubeEntity entity, float yaw, float partialTick, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
            entity.getBlock().ifPresent(state -> {
                float size = entity.isLarge() ? 0.6F : 0.35F;
                matrices.push();
                matrices.translate(0.0, entity.getHeight() / 2.0F, 0.0);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MathHelper.lerpAngleDegrees(partialTick, entity.prevBodyYaw, entity.bodyYaw)));
                matrices.scale(size, size, size);
                matrices.translate(-0.5, -0.5, -0.5);
                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, consumers, light, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            });
            super.render(entity, yaw, partialTick, matrices, consumers, light);
        }
    }

    /** Wolf Armor on a wolf, with its dye colour and cracks as it wears out. */
    static class WolfArmorFeature extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {
        private final WolfEntityModel<WolfEntity> model;

        WolfArmorFeature(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context, WolfEntityModel<WolfEntity> model) {
            super(context);
            this.model = model;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, WolfEntity wolf, float limbAngle, float limbDistance, float tickDelta,
                           float animationProgress, float headYaw, float headPitch) {
            if (!(wolf instanceof WolfArmorItem.Holder holder)) return;
            ItemStack armor = holder.gallifrey$getArmor();
            if (armor.isEmpty() || !(armor.getItem() instanceof WolfArmorItem item)) return;
            this.getContextModel().copyStateTo(this.model);
            this.model.animateModel(wolf, limbAngle, limbDistance, tickDelta);
            this.model.setAngles(wolf, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            draw(this.model, BackportGeo.tex("wolf_armor/armadillo_scute"), matrices, consumers, light, 1.0F, 1.0F, 1.0F);
            int color = item.getColor(armor);
            if (color >= 0) {
                draw(this.model, BackportGeo.tex("wolf_armor/armadillo_scute_overlay"), matrices, consumers, light,
                        ((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F);
            }
            float wear = armor.getDamage() / (float) armor.getMaxDamage();
            String crack = wear >= 0.75F ? "high" : wear >= 0.5F ? "medium" : wear >= 0.25F ? "low" : null;
            if (crack != null) {
                this.model.render(matrices, consumers.getBuffer(RenderLayer.getEntityTranslucent(BackportGeo.tex("wolf_armor/crackiness_" + crack))),
                        light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        private static void draw(WolfEntityModel<WolfEntity> model, Identifier texture, MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                 float r, float g, float b) {
            model.render(matrices, consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);
        }
    }

    /** Java's wolf model, a little inflated so the armour sits on the coat (vanilla 1.20.5 uses 0.2). */
    static TexturedModelData wolfArmorModel() {
        Dilation d = new Dilation(0.2F);
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData head = root.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, 13.5F, -7.0F));
        head.addChild("real_head", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, d)
                .uv(16, 14).cuboid(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, d).uv(16, 14).cuboid(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, d)
                .uv(0, 10).cuboid(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F, d), ModelTransform.NONE);
        root.addChild("body", ModelPartBuilder.create().uv(18, 14).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F, d),
                ModelTransform.of(0.0F, 14.0F, 2.0F, MathHelper.HALF_PI, 0.0F, 0.0F));
        root.addChild("upper_body", ModelPartBuilder.create().uv(21, 0).cuboid(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, d),
                ModelTransform.of(-1.0F, 14.0F, -3.0F, MathHelper.HALF_PI, 0.0F, 0.0F));
        ModelPartBuilder leg = ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, d);
        root.addChild("right_hind_leg", leg, ModelTransform.pivot(-2.5F, 16.0F, 7.0F));
        root.addChild("left_hind_leg", leg, ModelTransform.pivot(0.5F, 16.0F, 7.0F));
        root.addChild("right_front_leg", leg, ModelTransform.pivot(-2.5F, 16.0F, -4.0F));
        root.addChild("left_front_leg", leg, ModelTransform.pivot(0.5F, 16.0F, -4.0F));
        ModelPartData tail = root.addChild("tail", ModelPartBuilder.create(), ModelTransform.of(-1.0F, 12.0F, 8.0F, 0.62831855F, 0.0F, 0.0F));
        tail.addChild("real_tail", ModelPartBuilder.create().uv(9, 18).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, d), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 32);
    }
}
