package com.soybe.awakeningsystem.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Partícula de Impacto y Tajo Marcial / Manhwa (StrikeEffectParticle).
 * Se proyecta en tamaño monumental (1.4m - 1.8m) frente al punto de golpeo
 * con orientación frontal a la cámara y brillo total (Full-Bright).
 */
@OnlyIn(Dist.CLIENT)
public class StrikeEffectParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected StrikeEffectParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz,
                                  SpriteSet sprites, float size, float r, float g, float b) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 8 + this.random.nextInt(4);
        this.gravity = 0.0F;
        this.xd = vx * 0.1D;
        this.yd = vy * 0.1D;
        this.zd = vz * 0.1D;
        this.quadSize = size;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0.0F, 1.0F - (progress * progress));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0; // 100% Emisivo hiperbrillante
    }

    @OnlyIn(Dist.CLIENT)
    public static class SlashProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public SlashProvider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new StrikeEffectParticle(level, x, y, z, vx, vy, vz, this.sprites, 1.55F, 1.0F, 0.45F, 0.75F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClawProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public ClawProvider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new StrikeEffectParticle(level, x, y, z, vx, vy, vz, this.sprites, 1.6F, 0.85F, 0.3F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CleaveProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public CleaveProvider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new StrikeEffectParticle(level, x, y, z, vx, vy, vz, this.sprites, 1.75F, 1.0F, 0.65F, 0.2F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CelestialProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public CelestialProvider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new StrikeEffectParticle(level, x, y, z, vx, vy, vz, this.sprites, 1.65F, 0.4F, 0.9F, 1.0F);
        }
    }
}
