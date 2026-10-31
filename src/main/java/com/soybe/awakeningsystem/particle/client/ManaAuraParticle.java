package com.soybe.awakeningsystem.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ManaAuraParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ManaAuraParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 18 + this.random.nextInt(14);
        this.gravity = -0.015F;
        this.friction = 0.94F;
        this.xd = vx + (this.random.nextDouble() - 0.5D) * 0.02D;
        this.yd = vy + 0.02D + this.random.nextDouble() * 0.015D;
        this.zd = vz + (this.random.nextDouble() - 0.5D) * 0.02D;
        this.quadSize = 0.08F + this.random.nextFloat() * 0.05F;

        this.rCol = 0.2F;
        this.gCol = 0.9F;
        this.bCol = 1.0F;
        this.alpha = 0.85F;
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

        // Gentle sinusoidal wave drift
        this.xd += Math.sin((this.age + this.hashCode() % 12) * 0.25D) * 0.003D;
        this.zd += Math.cos((this.age + this.hashCode() % 12) * 0.25D) * 0.003D;

        // Soft fade out
        this.alpha = 0.85F * (1.0F - ageRatio);
        this.quadSize = (0.08F + this.random.nextFloat() * 0.05F) * (1.0F + ageRatio * 0.4F);
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
            return new ManaAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
