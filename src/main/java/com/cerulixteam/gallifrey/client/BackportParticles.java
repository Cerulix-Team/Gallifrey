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

    /** A leaf that drifts down slowly, swaying and spinning, and settles on the ground. */
    public static class FallingLeaf extends SpriteBillboardParticle {
        private final float spin;
        private final float swayPhase;

        FallingLeaf(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
            super(world, x, y, z);
            this.setSprite(sprites);
            this.maxAge = 300;
            this.gravityStrength = 0.0F;
            this.scale = 0.1F + this.random.nextFloat() * 0.05F;
            this.spin = (this.random.nextFloat() - 0.5F) * 0.1F;
            this.swayPhase = this.random.nextFloat() * 6.28F;
            this.velocityY = -0.03;
            this.collidesWithWorld = true;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.onGround) {
                this.velocityX = this.velocityZ = 0;
                this.prevAngle = this.angle;
                if (this.age < this.maxAge - 40) this.age = this.maxAge - 40;
                return;
            }
            float t = (this.age + this.swayPhase * 20) * 0.08F;
            this.velocityX = MathHelper.sin(t) * 0.02;
            this.velocityZ = MathHelper.cos(t * 0.7F) * 0.02;
            this.velocityY = -0.03;
            this.prevAngle = this.angle;
            this.angle += this.spin;
            if (this.age > this.maxAge - 40) this.alpha = (this.maxAge - this.age) / 40.0F;
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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
