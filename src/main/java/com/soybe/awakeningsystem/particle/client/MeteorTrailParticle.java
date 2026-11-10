package com.soybe.awakeningsystem.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The meteorite's trail: big glowing puffs that start white-hot, turn orange, then red, and end as dark smoke
 * that drifts up and fades. Lit by itself, so it shows at night and from far away.
 */
@OnlyIn(Dist.CLIENT)
public class MeteorTrailParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float startSize;

    protected MeteorTrailParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 34 + this.random.nextInt(20);
        this.gravity = -0.01F;
        this.friction = 0.9F;
        this.hasPhysics = false;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.startSize = 1.1F + this.random.nextFloat() * 0.9F;
        this.quadSize = startSize;
        this.setSpriteFromAge(sprites);
        tint(0.0F);
    }

    /** White-hot, then orange, red, and finally dark smoke. */
    private void tint(float a) {
        if (a < 0.15F) { float t = a / 0.15F; setColor(1.0F, 1.0F - 0.25F * t, 0.75F - 0.55F * t); }
        else if (a < 0.45F) { float t = (a - 0.15F) / 0.3F; setColor(1.0F, 0.75F - 0.45F * t, 0.2F - 0.15F * t); }
        else if (a < 0.7F) { float t = (a - 0.45F) / 0.25F; setColor(1.0F - 0.75F * t, 0.3F - 0.15F * t, 0.05F + 0.08F * t); }
        else setColor(0.25F, 0.15F + 0.05F * (a - 0.7F), 0.13F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float a = (float) this.age / this.lifetime;
        tint(a);
        // the fire swells a little; the smoke at the end spreads out
        this.quadSize = startSize * (1.0F + a * 1.6F);
        this.alpha = a < 0.7F ? 0.95F : 0.95F * (1.0F - (a - 0.7F) / 0.3F);
    }

    @Override
    public int getLightColor(float partialTick) {
        float a = (float) this.age / this.lifetime;
        // glowing while it burns, ordinary once it is smoke
        return a < 0.6F ? 0xF000F0 : super.getLightColor(partialTick);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xs, double ys, double zs) {
            return new MeteorTrailParticle(level, x, y, z, xs, ys, zs, this.sprites);
        }
    }
}
