package com.soybe.awakeningsystem.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShadowAuraParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ShadowAuraParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 22 + this.random.nextInt(16);
        this.gravity = -0.012F;
        this.friction = 0.95F;
        this.xd = vx + (this.random.nextDouble() - 0.5D) * 0.015D;
        this.yd = vy + 0.018D + this.random.nextDouble() * 0.015D;
        this.zd = vz + (this.random.nextDouble() - 0.5D) * 0.015D;
        this.quadSize = 0.09F + this.random.nextFloat() * 0.06F;

        this.rCol = 0.65F;
        this.gCol = 0.15F;
        this.bCol = 0.95F;
        this.alpha = 0.88F;
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

        float ageRatio = (float) this.age / (float) this.lifetime;

        // Billow and darken over time towards obsidian mist
        if (ageRatio > 0.4F) {
            float t = (ageRatio - 0.4F) / 0.6F;
            this.rCol = Math.max(0.1F, 0.65F - t * 0.5F);
            this.gCol = Math.max(0.05F, 0.15F - t * 0.1F);
            this.bCol = Math.max(0.2F, 0.95F - t * 0.6F);
        }

        // Organic swirling drift
        this.xd += Math.sin((this.age * 0.3D) + (this.hashCode() % 8)) * 0.0025D;
        this.zd += Math.cos((this.age * 0.3D) + (this.hashCode() % 8)) * 0.0025D;

        this.alpha = 0.88F * (1.0F - ageRatio);
        this.quadSize = (0.09F + this.random.nextFloat() * 0.06F) * (1.0F + ageRatio * 0.6F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ShadowAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
