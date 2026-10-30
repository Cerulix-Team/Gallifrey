package com.cerulixteam.gallifrey.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import com.cerulixteam.gallifrey.GallifreySounds;
import org.joml.Vector3f;

/**
 * Client-side visual weather for Gallifrey's custom planets.
 *
 * Mondas gets a much denser, wind-driven blizzard. Mars periodically enters a
 * red sandstorm for a short period, then returns to its normal clear weather.
 * These effects are deliberately client-side so they do not change vanilla
 * server weather or save data.
 */
public final class PlanetWeatherClient {
    private static final int MARS_MIN_CALM_TICKS = 6_000;   // 5 minutes
    private static final int MARS_MAX_CALM_TICKS = 14_400;  // 12 minutes
    private static final int MARS_MIN_STORM_TICKS = 1_200;  // 60 seconds
    private static final int MARS_MAX_STORM_TICKS = 2_400;  // 120 seconds

    private static int marsCalmTicks = MARS_MIN_CALM_TICKS;
    private static int marsStormTicks = 0;
    private static boolean marsSandstorm = false;

    private static final int MONDAS_MIN_CALM_TICKS = 5_400;   // 4.5 minutes
    private static final int MONDAS_MAX_CALM_TICKS = 12_000;  // 10 minutes
    private static final int MONDAS_MIN_STORM_TICKS = 900;    // 45 seconds
    private static final int MONDAS_MAX_STORM_TICKS = 1_800;  // 90 seconds
    private static final int MONDAS_WIND_REPEAT_TICKS = 160;  // just under 8 seconds

    private static int mondasCalmTicks = 5_400;
    private static int mondasStormTicks = 0;
    private static int mondasTick = 0;
    private static int mondasWindTicks = 0;
    private static boolean mondasBlizzard = false;

    private PlanetWeatherClient() {}

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        ClientWorld world = client.world;
        String dimension = world.getRegistryKey().getValue().getPath();

        if ("mondas".equals(dimension)) {
            tickMondasBlizzard(world, client);
        } else if ("mars".equals(dimension)) {
            resetMondas();
            tickMarsSandstorm(world, client);
        } else {
            resetMondas();
            marsSandstorm = false;
            marsStormTicks = 0;
            marsCalmTicks = MARS_MIN_CALM_TICKS;
        }
    }

    public static boolean isMondasBlizzard() {
        return mondasBlizzard;
    }

    /** 0..1 intensity, useful for Mondas fog and other client effects. */
    public static float mondasBlizzardIntensity(float tickDelta) {
        if (!mondasBlizzard) return 0.0F;
        float fadeIn = Math.min(1.0F, (MONDAS_MIN_STORM_TICKS - mondasStormTicks + 80.0F) / 80.0F);
        float fadeOut = Math.min(1.0F, (mondasStormTicks + tickDelta) / 80.0F);
        return Math.min(fadeIn, fadeOut);
    }

    public static boolean isMarsSandstorm() {
        return marsSandstorm;
    }

    /** 0..1 intensity, useful for fog and other client effects. */
    public static float marsStormIntensity(float tickDelta) {
        if (!marsSandstorm) return 0.0F;

        float fadeIn = Math.min(1.0F, (MARS_MIN_STORM_TICKS - marsStormTicks + 100.0F) / 100.0F);
        float fadeOut = Math.min(1.0F, (marsStormTicks + tickDelta) / 100.0F);
        return Math.min(fadeIn, fadeOut);
    }

    private static void tickMondasBlizzard(ClientWorld world, MinecraftClient client) {
        mondasTick++;

        if (!mondasBlizzard) {
            // Mondas naturally snows even between blizzard events. The biome is
            // configured for snow precipitation; these ambient flakes keep the
            // surface visually snowy even while the vanilla weather timer is clear.
            spawnGentleMondasSnow(world, client);

            if (--mondasCalmTicks <= 0) {
                // Each weather window has a 30% chance of becoming a blizzard.
                if (world.random.nextFloat() < 0.30F) {
                    mondasBlizzard = true;
                    mondasStormTicks = randomMondasStormDuration(world);
                    mondasWindTicks = 0;
                } else {
                    mondasCalmTicks = randomMondasCalmDuration(world);
                }
            }
            return;
        }

        mondasStormTicks--;
        mondasWindTicks--;

        if (mondasWindTicks <= 0) {
            // The sound is deliberately local and short, then restarted with a
            // slight pitch change so the wind does not sound like one frozen loop.
            world.playSound(
                    client.player.getX(), client.player.getY(), client.player.getZ(),
                    GallifreySounds.MONDAS_BLIZZARD_WIND,
                    net.minecraft.sound.SoundCategory.WEATHER,
                    0.72F,
                    0.82F + world.random.nextFloat() * 0.18F,
                    false
            );
            mondasWindTicks = MONDAS_WIND_REPEAT_TICKS + world.random.nextInt(25);
        }

        // Heavy snowfall: several snowflakes every tick over a much larger area.
        int particles = 18 + world.random.nextInt(12);
        if (mondasTick % 2 == 0) {
            particles += 12;
        }

        for (int i = 0; i < particles; i++) {
            double x = client.player.getX() + (world.random.nextDouble() * 52.0D - 26.0D);
            double y = client.player.getY() + 8.0D + world.random.nextDouble() * 20.0D;
            double z = client.player.getZ() + (world.random.nextDouble() * 52.0D - 26.0D);

            double windX = 0.16D + world.random.nextDouble() * 0.34D;
            double windZ = -0.12D + world.random.nextDouble() * 0.24D;
            double fall = -0.42D - world.random.nextDouble() * 0.32D;

            world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, windX, fall, windZ);
        }

        if (mondasTick % 3 == 0) {
            for (int i = 0; i < 6; i++) {
                double x = client.player.getX() + (world.random.nextDouble() * 40.0D - 20.0D);
                double y = client.player.getY() + 6.0D + world.random.nextDouble() * 16.0D;
                double z = client.player.getZ() + (world.random.nextDouble() * 40.0D - 20.0D);
                world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z,
                        0.22D + world.random.nextDouble() * 0.18D,
                        -0.28D - world.random.nextDouble() * 0.15D, 0.0D);
            }
        }

        if (mondasStormTicks <= 0) {
            mondasBlizzard = false;
            mondasCalmTicks = randomMondasCalmDuration(world);
            mondasWindTicks = 0;
        }
    }


    private static void spawnGentleMondasSnow(ClientWorld world, MinecraftClient client) {
        int particles = 2 + world.random.nextInt(3);
        for (int i = 0; i < particles; i++) {
            double x = client.player.getX() + (world.random.nextDouble() * 34.0D - 17.0D);
            double y = client.player.getY() + 7.0D + world.random.nextDouble() * 15.0D;
            double z = client.player.getZ() + (world.random.nextDouble() * 34.0D - 17.0D);
            double driftX = -0.018D + world.random.nextDouble() * 0.036D;
            double driftZ = -0.018D + world.random.nextDouble() * 0.036D;
            double fall = -0.075D - world.random.nextDouble() * 0.07D;
            world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, driftX, fall, driftZ);
        }
    }

    private static void resetMondas() {
        mondasBlizzard = false;
        mondasStormTicks = 0;
        mondasCalmTicks = MONDAS_MIN_CALM_TICKS;
        mondasWindTicks = 0;
    }

    private static int randomMondasCalmDuration(ClientWorld world) {
        return MONDAS_MIN_CALM_TICKS
                + world.random.nextInt(MONDAS_MAX_CALM_TICKS - MONDAS_MIN_CALM_TICKS + 1);
    }

    private static int randomMondasStormDuration(ClientWorld world) {
        return MONDAS_MIN_STORM_TICKS
                + world.random.nextInt(MONDAS_MAX_STORM_TICKS - MONDAS_MIN_STORM_TICKS + 1);
    }

    private static void tickMarsSandstorm(ClientWorld world, MinecraftClient client) {
        if (marsSandstorm) {
            marsStormTicks--;
            if (marsStormTicks <= 0) {
                marsSandstorm = false;
                marsCalmTicks = randomCalmDuration(world);
                return;
            }

            // Dense red dust moving mostly horizontally, like a genuine sandstorm.
            DustParticleEffect dust = new DustParticleEffect(
                    new Vector3f(0.72F, 0.16F, 0.045F),
                    1.15F
            );

            int particles = 14 + world.random.nextInt(11);
            for (int i = 0; i < particles; i++) {
                double x = client.player.getX() + (world.random.nextDouble() * 42.0D - 21.0D);
                double y = client.player.getY() + world.random.nextDouble() * 14.0D;
                double z = client.player.getZ() + (world.random.nextDouble() * 42.0D - 21.0D);

                double speed = 0.45D + world.random.nextDouble() * 0.9D;
                double angle = world.random.nextDouble() * Math.PI * 2.0D;
                double vx = Math.cos(angle) * speed;
                double vz = Math.sin(angle) * speed;
                double vy = -0.02D + world.random.nextDouble() * 0.08D;

                world.addParticle(dust, x, y, z, vx, vy, vz);
            }

            // A few larger dust bursts close to the player make the storm feel severe.
            if (world.random.nextInt(3) == 0) {
                for (int i = 0; i < 3; i++) {
                    Vec3d pos = client.player.getPos().add(
                            world.random.nextDouble() * 10.0D - 5.0D,
                            world.random.nextDouble() * 5.0D,
                            world.random.nextDouble() * 10.0D - 5.0D
                    );
                    world.addParticle(dust, pos.x, pos.y, pos.z,
                            world.random.nextDouble() * 0.8D - 0.4D,
                            0.0D,
                            world.random.nextDouble() * 0.8D - 0.4D);
                }
            }
            return;
        }

        if (--marsCalmTicks <= 0) {
            marsSandstorm = true;
            marsStormTicks = randomStormDuration(world);
        }
    }

    private static int randomCalmDuration(ClientWorld world) {
        return MARS_MIN_CALM_TICKS
                + world.random.nextInt(MARS_MAX_CALM_TICKS - MARS_MIN_CALM_TICKS + 1);
    }

    private static int randomStormDuration(ClientWorld world) {
        return MARS_MIN_STORM_TICKS
                + world.random.nextInt(MARS_MAX_STORM_TICKS - MARS_MIN_STORM_TICKS + 1);
    }
}
