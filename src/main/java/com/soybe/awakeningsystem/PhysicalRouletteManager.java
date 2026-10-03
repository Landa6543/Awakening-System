package com.soybe.awakeningsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PhysicalRouletteManager {
    private PhysicalRouletteManager() {}

    public static class ActiveSpin {
        public final UUID spinId;
        public final UUID playerId;
        public final ServerLevel level;
        public final Vec3 centerPos;
        public final boolean onAltarBlock;
        public int ticks = 0;
        public double ballAngle = 0.0D;
        public ItemEntity floatingItem;
        public final ItemStack winningStack;
        public final int coinsWon;

        public ActiveSpin(UUID spinId, UUID playerId, ServerLevel level, Vec3 centerPos, boolean onAltarBlock, ItemStack winningStack, int coinsWon) {
            this.spinId = spinId;
            this.playerId = playerId;
            this.level = level;
            this.centerPos = centerPos;
            this.onAltarBlock = onAltarBlock;
            this.winningStack = winningStack;
            this.coinsWon = coinsWon;
        }
    }

    private static final Map<UUID, ActiveSpin> ACTIVE_SPINS = new ConcurrentHashMap<>();

    private static final ItemStack[] DEFAULT_CANDIDATES = new ItemStack[]{
            new ItemStack(Items.NETHERITE_INGOT, 2),
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2),
            new ItemStack(Items.TOTEM_OF_UNDYING, 1),
            new ItemStack(Items.DIAMOND_BLOCK, 2),
            new ItemStack(Items.GOLDEN_APPLE, 4),
            new ItemStack(Items.DIAMOND, 8),
            new ItemStack(Items.EXPERIENCE_BOTTLE, 16),
            new ItemStack(Items.NETHERITE_SCRAP, 3),
            new ItemStack(Items.GOLD_INGOT, 12),
            new ItemStack(Items.ENDER_PEARL, 6),
            new ItemStack(Items.EMERALD_BLOCK, 2),
            new ItemStack(Items.STICK, 1),
            new ItemStack(Items.POISONOUS_POTATO, 1),
            new ItemStack(Items.STRING, 2),
            new ItemStack(Items.IRON_INGOT, 8)
    };

    public static ItemStack getCandidate(int index) {
        List<ItemStack> pool = new ArrayList<>();
        if (SystemItems.PLUM_BLOSSOM_SWORD.isBound()) pool.add(new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()));
        if (SystemItems.IMMORTAL_DRAGON_SCALE.isBound()) pool.add(new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 2));
        if (SystemItems.CELESTIAL_DRAGON_PEARL.isBound()) pool.add(new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get()));
        if (SystemItems.SHADOW_DAGGERS.isBound()) pool.add(new ItemStack(SystemItems.SHADOW_DAGGERS.get()));
        if (SystemItems.SKY_DEMON_SWORD.isBound()) pool.add(new ItemStack(SystemItems.SKY_DEMON_SWORD.get()));
        if (SystemItems.THOUSAND_YEAR_GINSENG.isBound()) pool.add(new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 2));
        if (SystemItems.SNOW_LOTUS.isBound()) pool.add(new ItemStack(SystemItems.SNOW_LOTUS.get(), 2));
        if (SystemItems.AWAKENING_COIN.isBound()) pool.add(new ItemStack(SystemItems.AWAKENING_COIN.get(), 64));
        pool.addAll(Arrays.asList(DEFAULT_CANDIDATES));
        return pool.get(Math.floorMod(index, pool.size()));
    }

    public static boolean hasActiveSpin(UUID playerId) {
        for (ActiveSpin spin : ACTIVE_SPINS.values()) {
            if (spin.playerId.equals(playerId)) return true;
        }
        return false;
    }

    public static boolean startRoulette(ServerPlayer player, Vec3 targetPos, boolean onAltarBlock) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return false;
        if (hasActiveSpin(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
            return false;
        }

        UUID spinId = UUID.randomUUID();
        ItemStack winning = rollWinningLoot(player);
        int bonusCoins = 50 * (1 + player.getRandom().nextInt(4)); // 50, 100, 150 o 200 Monedas limpias

        ActiveSpin spin = new ActiveSpin(spinId, player.getUUID(), level, targetPos, onAltarBlock, winning, bonusCoins);

        double spawnY = targetPos.y + (onAltarBlock ? 0.95D : 0.85D);
        ItemEntity entity = new ItemEntity(level, targetPos.x, spawnY, targetPos.z, getCandidate(0));
        entity.setNoGravity(true);
        entity.setPickUpDelay(32767);
        entity.setNeverPickUp();
        entity.setGlowingTag(true);
        entity.setDeltaMovement(0, 0, 0);
        level.addFreshEntity(entity);
        spin.floatingItem = entity;

        ACTIVE_SPINS.put(spinId, spin);

        // Sonido inicial de activación
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.5F);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.8F);

        player.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning_short"), true);
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE_SPINS.isEmpty()) return;

        Iterator<Map.Entry<UUID, ActiveSpin>> iter = ACTIVE_SPINS.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, ActiveSpin> entry = iter.next();
            ActiveSpin spin = entry.getValue();
            spin.ticks++;

            ServerLevel level = spin.level;
            Vec3 pos = spin.centerPos;
            ServerPlayer player = server.getPlayerList().getPlayer(spin.playerId);

            // Si la entidad flotante fue eliminada prematuramente, abortar
            if (spin.floatingItem == null || !spin.floatingItem.isAlive()) {
                iter.remove();
                continue;
            }

            // 1. Efectos visuales de la Ruleta Física (Círculo Rúnico con 8 sectores)
            int sectors = 8;
            double radius = 1.35D;
            double baseY = pos.y + (spin.onAltarBlock ? 1.02D : 0.05D);
            for (int s = 0; s < sectors; s++) {
                double rad = Math.toRadians(s * (360.0D / sectors) + (spin.ticks * 2.0D));
                double px = pos.x + Math.cos(rad) * radius;
                double pz = pos.z + Math.sin(rad) * radius;
                level.sendParticles(ParticleTypes.ENCHANT, px, baseY, pz, 1, 0, 0.05, 0, 0.02);
                if (spin.ticks % 4 == 0) {
                    level.sendParticles(ParticleTypes.GLOW, px, baseY + 0.02, pz, 1, 0, 0.01, 0, 0.01);
                }
            }

            // 2. Bola de luz que orbita a toda velocidad y desacelera como en una ruleta real
            double speedFactor = Math.max(0.04D, 0.65D * (1.0D - (double) spin.ticks / 78.0D));
            spin.ballAngle += speedFactor;
            double ballDist = Math.max(0.35D, radius * (1.0D - (double) spin.ticks / 90.0D));
            double bx = pos.x + Math.cos(spin.ballAngle) * ballDist;
            double bz = pos.z + Math.sin(spin.ballAngle) * ballDist;
            level.sendParticles(ParticleTypes.END_ROD, bx, baseY + 0.15D, bz, 1, 0, 0.01, 0, 0.02);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bx, baseY + 0.12D, bz, 1, 0, 0, 0, 0.01);

            // 3. Desaceleración del ciclo de ítems candidatos
            int cycleInterval = spin.ticks < 25 ? 2 : (spin.ticks < 45 ? 4 : (spin.ticks < 60 ? 7 : (spin.ticks < 72 ? 11 : 16)));
            if (spin.ticks < 75 && spin.ticks % cycleInterval == 0) {
                spin.floatingItem.setItem(getCandidate(spin.ticks / cycleInterval));

                // Sonido adictivo de trinquete de ruleta mecánica: clic seco + campana de ranura + chime (idéntico al sistema)
                float progress = Math.min(1.0F, (float) spin.ticks / 75.0F);
                int sectorIndex = (int) (spin.ballAngle / (Math.PI / 4.0D));
                float clickPitch = 1.5F + (progress * 0.5F);
                float bellPitch = 1.2F + ((Math.abs(sectorIndex) % 8) * 0.1F);
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        ModSounds.ROULETTE_SPIN.value(), SoundSource.BLOCKS, 0.9F, clickPitch);
            }

            // Flotación suave
            spin.floatingItem.setDeltaMovement(0, Math.sin(spin.ticks * 0.2D) * 0.003D, 0);

            // 4. Parada en el ítem premiado (Tick 75)
            if (spin.ticks == 75) {
                spin.floatingItem.setItem(spin.winningStack.copy());
                spin.floatingItem.setDeltaMovement(0, 0, 0);

                // Gran explosión de fanfarria
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, baseY + 0.5D, pos.z, 90, 0.45D, 0.5D, 0.45D, 0.25D);
                level.sendParticles(ParticleTypes.FIREWORK, pos.x, baseY + 0.5D, pos.z, 45, 0.35D, 0.4D, 0.35D, 0.12D);
                level.sendParticles(ParticleTypes.FLASH, pos.x, baseY + 0.5D, pos.z, 2, 0, 0, 0, 0);

                // Fanfarria triunfal de parada con el sonido custom de la Ruleta del Sistema
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        ModSounds.ROULETTE_WIN.value(), SoundSource.BLOCKS, 1.4F, 1.0F);

                if (player != null && player.isAlive()) {
                    SystemData data = SystemEvents.data(player);
                    data.addCoins(spin.coinsWon);
                    data.recordStatistic("missions_completed", 1);
                    SystemEvents.checkAchievements(player, data);
                    AwakeningSystemMod.sync(player);

                    ItemStack reward = spin.winningStack.copy();
                    if (!player.getInventory().add(reward)) {
                        player.drop(reward, false);
                    }

                    AwakeningSystemMod.notify(player,
                            "awakening_system.chest.roulette.title",
                            "awakening_system.chest.roulette.awarded",
                            0xFFFFD700);

                    String rewardDesc = spin.winningStack.getHoverName().getString() + " x" + spin.winningStack.getCount() +
                            (spin.coinsWon > 0 ? " §6(+§e" + spin.coinsWon + " ⛃§6)" : "");
                    player.displayClientMessage(Component.translatable("awakening_system.roulette.reward_chat", rewardDesc), true);
                }
            }

            // Cascada de campanadas musicales y monedas mientras el ítem premiado flota y se entrega al jugador (ticks 76 a 92)
            if (spin.ticks > 75 && spin.ticks <= 92 && spin.ticks % 2 == 0) {
                int step = (spin.ticks - 76) / 2;
                float bellPitch = Math.min(2.0F, 1.2F + (step * 0.12F));
                float coinPitch = 1.1F + (step * 0.08F);
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 0.65F, bellPitch);
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.45F, bellPitch);
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.85F, coinPitch);
                level.playSound(null, pos.x, pos.y + 1.0D, pos.z,
                        SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.5F, 1.6F);
            }

            // 5. Finalización y limpieza (Tick 95)
            if (spin.ticks >= 95) {
                spin.floatingItem.discard();
                level.sendParticles(ParticleTypes.POOF, pos.x, baseY + 0.5D, pos.z, 15, 0.2D, 0.2D, 0.2D, 0.05D);
                iter.remove();
            }
        }
    }

    private static ItemStack rollWinningLoot(ServerPlayer player) {
        int roll = player.getRandom().nextInt(100);

        // 10% Botín Legendario Mítico
        if (roll < 2) {
            if (SystemItems.PLUM_BLOSSOM_SWORD.isBound()) return new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get());
            return new ItemStack(Items.NETHERITE_INGOT, 2);
        } else if (roll < 4) {
            if (SystemItems.CELESTIAL_DRAGON_PEARL.isBound()) return new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get());
            return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2);
        } else if (roll < 6) {
            if (SystemItems.IMMORTAL_DRAGON_SCALE.isBound()) return new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 2);
            return new ItemStack(Items.NETHERITE_BLOCK, 1);
        } else if (roll < 8) {
            if (SystemItems.SHADOW_DAGGERS.isBound()) return new ItemStack(SystemItems.SHADOW_DAGGERS.get());
            return new ItemStack(Items.TOTEM_OF_UNDYING, 1);
        } else if (roll < 10) {
            if (SystemItems.SKY_DEMON_SWORD.isBound()) return new ItemStack(SystemItems.SKY_DEMON_SWORD.get());
            return new ItemStack(Items.NETHERITE_INGOT, 2);
        }
        // 25% Botín Épico / Manhwa Raro
        else if (roll < 14) {
            if (SystemItems.THOUSAND_YEAR_GINSENG.isBound()) return new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 2);
            return new ItemStack(Items.DIAMOND_BLOCK, 2);
        } else if (roll < 18) {
            if (SystemItems.SNOW_LOTUS.isBound()) return new ItemStack(SystemItems.SNOW_LOTUS.get(), 2);
            return new ItemStack(Items.GOLDEN_APPLE, 4);
        } else if (roll < 22) {
            if (SystemItems.RUNE_STONE_RULERS_AUTHORITY.isBound()) return new ItemStack(SystemItems.RUNE_STONE_RULERS_AUTHORITY.get());
            return new ItemStack(Items.NETHERITE_SCRAP, 3);
        } else if (roll < 26) {
            if (SystemItems.RUNE_STONE_DRAGON_EYES.isBound()) return new ItemStack(SystemItems.RUNE_STONE_DRAGON_EYES.get());
            return new ItemStack(Items.DIAMOND_BLOCK, 2);
        } else if (roll < 30) {
            if (SystemItems.RUNE_STONE_IRON_BODY.isBound()) return new ItemStack(SystemItems.RUNE_STONE_IRON_BODY.get());
            return new ItemStack(Items.TOTEM_OF_UNDYING, 1);
        } else if (roll < 35) {
            if (SystemItems.AWAKENING_COIN.isBound()) return new ItemStack(SystemItems.AWAKENING_COIN.get(), 64);
            return new ItemStack(Items.EMERALD_BLOCK, 4);
        }
        // 40% Botín Valioso de Aventura
        else if (roll < 45) {
            return new ItemStack(Items.DIAMOND, 8);
        } else if (roll < 55) {
            return new ItemStack(Items.GOLDEN_APPLE, 3);
        } else if (roll < 65) {
            return new ItemStack(Items.EXPERIENCE_BOTTLE, 32);
        } else if (roll < 75) {
            if (SystemItems.AWAKENING_COIN.isBound()) return new ItemStack(SystemItems.AWAKENING_COIN.get(), 32);
            return new ItemStack(Items.GOLD_INGOT, 16);
        }
        // 25% Botín Especial / Cómico con Encantamientos
        else if (roll < 83) {
            ItemStack stick = new ItemStack(Items.STICK, 1);
            stick.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("awakening_system.roulette.fate_stick"));
            stick.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            return stick;
        } else if (roll < 91) {
            ItemStack potato = new ItemStack(Items.POISONOUS_POTATO, 1);
            potato.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("awakening_system.roulette.quantum_potato"));
            potato.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            return potato;
        } else {
            return new ItemStack(Items.ENDER_PEARL, 8);
        }
    }
}
