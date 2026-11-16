package com.cerulixteam.gallifrey.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

/** Falling leaves (pale oak, poplars) and fireflies. */
public final class BackportParticles {
    private BackportParticles() {}

    /**
     * A falling leaf, using 26.3's FallingLeavesParticle settings for pale oak and poplar leaves
     * (fall 0.07, wind 10, swirling, size 2x). It disappears when it lands or hits a wall, like vanilla.
     */
    public static class FallingLeaf extends SpriteBillboardParticle {
        private static final float ACCELERATION_SCALE = 0.0025F;
        private static final int LIFETIME = 300;
        private float rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        private final float spinAcceleration = (float) Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        private final float windBig = 10.0F;
        private final double swirlPeriod;
        private int lifeLeft = LIFETIME;

        FallingLeaf(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
            super(world, x, y, z);
            this.setSprite(sprites.getSprite(this.random));
            this.maxAge = LIFETIME;
            this.gravityStrength = 0.07F * 1.2F * ACCELERATION_SCALE;
            float size = 2.0F * (this.random.nextBoolean() ? 0.05F : 0.075F);
            this.scale = size;
            this.setBoundingBoxSpacing(size, size);
            this.velocityMultiplier = 1.0F;
            this.velocityY = -0.021F;
            this.swirlPeriod = Math.toRadians(1000.0F + this.random.nextFloat() * 3000.0F);
        }

        @Override
        public void tick() {
            this.prevPosX = this.x;
            this.prevPosY = this.y;
            this.prevPosZ = this.z;
            if (this.lifeLeft-- <= 0) this.markDead();
            if (this.dead) return;

            float relativeAge = Math.min((LIFETIME - this.lifeLeft) / (float) LIFETIME, 1.0F);
            double xa = relativeAge * Math.cos(relativeAge * this.swirlPeriod) * this.windBig;
            double za = relativeAge * Math.sin(relativeAge * this.swirlPeriod) * this.windBig;
            this.velocityX += xa * ACCELERATION_SCALE;
            this.velocityZ += za * ACCELERATION_SCALE;
            this.velocityY -= this.gravityStrength;
            this.rotSpeed += this.spinAcceleration / 20.0F;
            this.prevAngle = this.angle;
            this.angle += this.rotSpeed / 20.0F;
            this.move(this.velocityX, this.velocityY, this.velocityZ);
            if (this.onGround || this.lifeLeft < LIFETIME - 1 && (this.velocityX == 0.0 || this.velocityZ == 0.0)) {
                this.markDead();
            }
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
        }
    }

    /** A firefly: wanders about, glowing, fading in and out. */
    public static class Firefly extends SpriteBillboardParticle {
        Firefly(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
            super(world, x, y, z);
            this.setSprite(sprites);
            this.maxAge = 120 + this.random.nextInt(120);
            this.gravityStrength = 0.0F;
            this.scale = 0.05F;
            this.collidesWithWorld = true;
            this.alpha = 0;
        }

        @Override
        public void tick() {
            super.tick();
            this.velocityX += (this.random.nextFloat() - 0.5F) * 0.004;
            this.velocityY += (this.random.nextFloat() - 0.5F) * 0.004;
            this.velocityZ += (this.random.nextFloat() - 0.5F) * 0.004;
            this.velocityX *= 0.95;
            this.velocityY *= 0.95;
            this.velocityZ *= 0.95;
            float life = this.age / (float) this.maxAge;
            this.alpha = MathHelper.clamp(Math.min(life * 6, (1 - life) * 6), 0, 1) * (0.6F + 0.4F * MathHelper.sin(this.age * 0.3F));
        }

        @Override
        protected int getBrightness(float tint) {
            return 0xF000F0;
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public record LeafFactory(SpriteProvider sprites) implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new FallingLeaf(world, x, y, z, this.sprites);
        }
    }

    public record FireflyFactory(SpriteProvider sprites) implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new Firefly(world, x, y, z, this.sprites);
        }
    }
}
