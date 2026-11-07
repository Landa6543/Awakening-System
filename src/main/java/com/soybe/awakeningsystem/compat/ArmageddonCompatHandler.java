package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.ShadowRank;
import com.soybe.awakeningsystem.SkillDefinitions;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.fml.ModList;

import java.util.Locale;
import java.util.Map;

/**
 * Módulo de integración cruzada completa con el mod 'Armageddon' (choupicul) y su ecosistema RPG.
 * Proporciona:
 * 1. Detección condicional limpia (sin dependencias forzadas en compilación ni ejecución).
 * 2. Reconocimiento de los 11 jefes principales de progresión pre y post Dragón.
 * 3. Desbloqueo de +5 Puntos de Atributo libres y monedas masivas del Sistema por cada hito/bloqueo de tier superado.
 * 4. Títulos y Fábulas exclusivas de Cazador con atributos de combate dedicados.
 * 5. Extracción de sombras habilitada para todos los jefes y criaturas de Armageddon (rangos Elite Knight a Grand Marshal).
 * 6. Adquisición automática de la habilidad mítica 'armageddon_breaker' (Voluntad del Armagedón).
 */
public final class ArmageddonCompatHandler {

    public static final String MOD_ID = "armageddon";
    public static final String MOD_ID_ALT = "armageddon_mod";

    public record ArmageddonBossInfo(
            String key,
            String titleId,
            String fableId,
            int coinReward,
            ShadowRank shadowRank,
            String englishName,
            String spanishName
    ) {}

    private static final Map<String, ArmageddonBossInfo> BOSS_MAP = Map.ofEntries(
            Map.entry("iron_colossus", new ArmageddonBossInfo(
                    "iron_colossus", "armageddon_colossus_breaker", "armageddon_iron_unlocked",
                    2500, ShadowRank.ELITE_KNIGHT, "The Iron Colossus", "El Coloso de Hierro")),
            Map.entry("goblin_lord", new ArmageddonBossInfo(
                    "goblin_lord", "armageddon_goblin_subjugator", "armageddon_gold_unlocked",
                    3500, ShadowRank.ELITE_KNIGHT, "The Goblin Lord", "El Señor Duende")),
            Map.entry("arion", new ArmageddonBossInfo(
                    "arion", "armageddon_emerald_conqueror", "armageddon_emerald_unlocked",
                    5000, ShadowRank.COMMANDER, "Arion, Tyrant of the Emerald Wrath", "Arion, Tirano de la Furia Esmeralda")),
            Map.entry("eldorath", new ArmageddonBossInfo(
                    "eldorath", "armageddon_nether_gatekeeper", "armageddon_nether_unlocked",
                    7000, ShadowRank.COMMANDER, "Eldorath, the Ancient Builder", "Eldorath, el Constructor Antiguo")),
            Map.entry("zoranth", new ArmageddonBossInfo(
                    "zoranth", "armageddon_void_reaper", "armageddon_netherite_unlocked",
                    9000, ShadowRank.COMMANDER, "Zoranth, the Forgotten One", "Zoranth, el Olvidado")),
            Map.entry("elvenite_paladin", new ArmageddonBossInfo(
                    "elvenite_paladin", "armageddon_elven_slayer", "armageddon_elvenite_unlocked",
                    10000, ShadowRank.COMMANDER, "The Elvenite Paladin", "El Paladín de Elvenita")),
            Map.entry("vaedric", new ArmageddonBossInfo(
                    "vaedric", "armageddon_doom_bringer_slayer", "armageddon_voiderite_unlocked",
                    12000, ShadowRank.COMMANDER, "Vaedric, the Fallen Wanderer", "Vaedric, el Errante Caído")),
            Map.entry("bringer_of_doom", new ArmageddonBossInfo(
                    "bringer_of_doom", "armageddon_doom_bringer_slayer", "armageddon_doom_defeated",
                    14000, ShadowRank.GRAND_MARSHAL, "The Bringer of Doom", "El Heraldo del Destino")),
            Map.entry("calamities", new ArmageddonBossInfo(
                    "calamities", "armageddon_calamity_overlord", "armageddon_calamity_slayer",
                    16000, ShadowRank.GRAND_MARSHAL, "The Calamities", "Las Calamidades")),
            Map.entry("zoranth_zenith", new ArmageddonBossInfo(
                    "zoranth_zenith", "armageddon_calamity_overlord", "armageddon_zenith_slayer",
                    18000, ShadowRank.GRAND_MARSHAL, "Zoranth, Newborn of the Zenith", "Zoranth, Nacido del Cenit")),
            Map.entry("nyxaris", new ArmageddonBossInfo(
                    "nyxaris", "armageddon_calamity_overlord", "armageddon_oblivion_conquered",
                    20000, ShadowRank.GRAND_MARSHAL, "Nyxaris, the Veil of Oblivion", "Nyxaris, el Velo del Olvido"))
    );

    private ArmageddonCompatHandler() {}

    /**
     * Comprueba si el mod Armageddon está presente en el modpack.
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded(MOD_ID_ALT);
    }

    /**
     * Determina si una entidad pertenece al mod Armageddon.
     */
    public static boolean isArmageddonEntity(LivingEntity entity) {
        if (entity == null) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return false;
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        return ns.equals(MOD_ID) || ns.equals(MOD_ID_ALT);
    }

    /**
     * Obtiene la información del jefe de Armageddon según el identificador de la entidad.
     */
    public static ArmageddonBossInfo getBossInfo(LivingEntity entity) {
        if (entity == null) return null;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return null;
        String path = id.getPath().toLowerCase(Locale.ROOT);

        for (Map.Entry<String, ArmageddonBossInfo> entry : BOSS_MAP.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Determina si la entidad es un jefe de Armageddon.
     */
    public static boolean isArmageddonBoss(LivingEntity entity) {
        return getBossInfo(entity) != null;
    }

    /**
     * Retorna el rango inicial de sombra apropiado para una criatura de Armageddon.
     */
    public static ShadowRank getShadowRankForEntity(Mob mob) {
        if (mob == null) return ShadowRank.SOLDIER;
        ArmageddonBossInfo info = getBossInfo(mob);
        if (info != null) {
            return info.shadowRank();
        }
        float hp = mob.getMaxHealth();
        if (hp >= 400.0F) return ShadowRank.GRAND_MARSHAL;
        if (hp >= 250.0F) return ShadowRank.COMMANDER;
        if (hp >= 100.0F) return ShadowRank.ELITE_KNIGHT;
        if (hp >= 50.0F) return ShadowRank.KNIGHT;
        return ShadowRank.ELITE;
    }

    /**
     * Procesa la derrota de un jefe de Armageddon otorgando los beneficios y desbloqueos del Sistema.
     */
    public static void handleArmageddonBossDefeat(ServerPlayer player, LivingEntity boss, SystemData data) {
        if (player == null || boss == null || data == null) return;
        ArmageddonBossInfo info = getBossInfo(boss);
        if (info == null) return;

        // Fábulas y títulos de tier
        boolean isFirstVictory = !data.hasFable(info.fableId());
        SystemEvents.grantFable(player, info.fableId());

        if (info.titleId() != null && !info.titleId().isEmpty()) {
            SystemEvents.grantFable(player, info.titleId());
        }

        // Si es la primera vez que supera este bloqueo de tier:
        if (isFirstVictory) {
            // 1. Recompensa de Atributos del Sistema: +5 Puntos de Atributo libres
            data.addFreePoints(5);

            // 2. Monedas del Sistema
            data.addCoins(info.coinReward());

            // 3. Desbloqueo / Otorgamiento de la habilidad mítica 'armageddon_breaker'
            if (!data.skills().containsKey("armageddon_breaker")) {
                data.skills().put("armageddon_breaker", 1);
                data.gainSkillProgress("armageddon_breaker", 1);

                AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.skill_acquired.title",
                        "awakening_system.skill.armageddon_breaker.name",
                        -12345,
                        new Object[]{}
                );
            }

            // 4. Notificación Holográfica Triunfal del Sistema
            String localizedBossName = player.getServer() != null ? info.spanishName() : info.englishName();
            AwakeningSystemMod.notify(
                    player,
                    "awakening_system.notification.armageddon_tier_unlocked.title",
                    "awakening_system.notification.armageddon_tier_unlocked.desc",
                    0xFFA500,
                    new Object[]{localizedBossName, info.coinReward()}
            );

            // 5. Efectos de Audio y Partículas Triunfales
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, boss.getX(), boss.getY() + 1.2, boss.getZ(), 80, 0.8, 1.0, 0.8, 0.25);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), boss.getY() + 1.0, boss.getZ(), 50, 0.6, 0.8, 0.6, 0.1);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, boss.getX(), boss.getY() + 1.5, boss.getZ(), 30, 0.5, 0.5, 0.5, 0.08);
                sl.playSound(null, player.blockPosition(), ModSounds.SYSTEM_SKILL_UNLOCK.get(), SoundSource.PLAYERS, 1.4F, 1.0F);
                sl.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.1F);
            }

            AwakeningSystemMod.sync(player);
        }
    }
}
