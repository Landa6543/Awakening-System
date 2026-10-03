package com.soybe.awakeningsystem.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiritFoxfireParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SpiritFoxfireParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 22 + this.random.nextInt(12);
        this.gravity = -0.012F;
        this.friction = 0.96F;
        this.xd = vx + (this.random.nextDouble() - 0.5D) * 0.012D;
        this.yd = vy + 0.01D + this.random.nextDouble() * 0.01D;
        this.zd = vz + (this.random.nextDouble() - 0.5D) * 0.012D;
        this.quadSize = 0.075F + this.random.nextFloat() * 0.035F;

        this.rCol = 0.25F;
        this.gCol = 0.88F;
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

        if (ageRatio > 0.35F) {
            float t = (ageRatio - 0.35F) / 0.65F;
            this.rCol = 0.25F + t * 0.75F;
            this.gCol = 0.88F - t * 0.08F;
            this.bCol = 1.0F - t * 0.75F;
        }

        this.alpha = 0.85F * (1.0F - ageRatio);
        this.xd += Math.sin((this.age + this.hashCode() % 10) * 0.2D) * 0.002D;
        this.zd += Math.cos((this.age + this.hashCode() % 10) * 0.2D) * 0.002D;
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
            return new SpiritFoxfireParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
