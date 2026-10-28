package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.entity.LimboOphanimEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Sonido ambiental continuo y fluido (looping) para el Ángel Ofanim del Limbo.
 * Se reproduce sin pausas, micro-retrasos ni vacíos acústicos (looping = true, delay = 0),
 * siguiendo la posición de la entidad en 3D mientras esté con vida.
 */
@OnlyIn(Dist.CLIENT)
public class OphanimAmbientSoundInstance extends AbstractTickableSoundInstance {
    private final LimboOphanimEntity ophanim;

    public OphanimAmbientSoundInstance(LimboOphanimEntity ophanim) {
        super(ModSounds.LIMBO_OPHANIM_AMBIENT.get(), SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.ophanim = ophanim;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.8F;
        this.pitch = 1.0F;
        this.x = ophanim.getX();
        this.y = ophanim.getY();
        this.z = ophanim.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !this.ophanim.isSilent();
    }

    @Override
    public void tick() {
        if (this.ophanim.isRemoved() || !this.ophanim.isAlive()) {
            this.stop();
        } else {
            this.x = this.ophanim.getX();
            this.y = this.ophanim.getY();
            this.z = this.ophanim.getZ();
        }
    }
}
