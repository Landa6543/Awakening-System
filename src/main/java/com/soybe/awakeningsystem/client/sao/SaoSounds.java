package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * The full-dive menu's sounds (sounds.json "sao.*"): the series' menu sounds for opening, confirming, going
 * back and closing (sounds/sao/), and a soft amethyst tick for hovering.
 */
public final class SaoSounds {
    private SaoSounds() {}

    private static final RandomSource RANDOM = RandomSource.create();
    private static long lastHover;

    private static void play(String name, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(new SimpleSoundInstance(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, name),
                SoundSource.MASTER, 1.0F, pitch, RANDOM, false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
    }

    public static void open() {
        play("sao.open", 1.0F);
    }

    public static void click() {
        play("sao.click", 1.0F);
    }

    /** A soft tick when the mouse moves onto something new; never more than one every 60 ms. */
    public static void hover() {
        long now = System.currentTimeMillis();
        if (now - lastHover < 60) return;
        lastHover = now;
        play("sao.hover", 0.95F + RANDOM.nextFloat() * 0.1F);
    }

    public static void back() {
        play("sao.back", 1.0F);
    }

    public static void close() {
        play("sao.close", 1.0F);
    }

    public static void confirm() {
        play("sao.confirm", 1.0F);
    }
}
