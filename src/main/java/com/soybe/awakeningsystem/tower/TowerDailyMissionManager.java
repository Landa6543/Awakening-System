package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.OpenTowerMissionBoardPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Gestor del Tablón Diario de Misiones de la Torre del Ranker (Sección 14.3).
 * Administra el catálogo de misiones por piso, la rotación diaria determinista por ciclo,
 * la progresión estricta basada exclusivamente en savedTowerFloor(), y el flujo de objetivos y recompensas.
 */
public final class TowerDailyMissionManager {
    private TowerDailyMissionManager() {}

    public static final int DAILY_OFFERS_COUNT = 10;

    public static final List<TowerMissionTemplate> ALL_MISSIONS = new ArrayList<>();

    static {
        // --- PISOS 1 A 5 (TIER INICIAL) ---
        register(0, 1,
                "Prueba de Entrada: Autómatas de Headon",
                "Headon ha liberado sus constructos de piedra y acero en la primera planta para medir el valor de los aspirantes. El Administrador exige desmantelar a los centinelas para verificar tu temple.",
                "kill_headon_beast", "Bestias de Headon (Gólems)", 3,
                1500, 1200, 5, new ItemStack(Items.CRYING_OBSIDIAN, 4), "Obsidiana Llorosa");

        register(1, 1,
                "Cimentación de Pizarra Profunda",
                "Los arquitectos de la asociación de cazadores requieren muestras sólidas de pizarra de la base de la torre para estabilizar la barrera exterior contra el flujo salvaje de Shinsu.",
                "collect_deepslate", "Pizarra Profunda", 16,
                1200, 1000, 3, new ItemStack(Items.POLISHED_DEEPSLATE, 8), "Pizarra Pulida");

        register(2, 2,
                "Juicio Ígneo: Efigies de Evankhell",
                "El Piso de Pruebas arde con las llamas sagradas de Evankhell. Varios jueces llameantes han tomado posición en el cuadrilátero, esperando a cazadores que no teman las quemaduras del infierno.",
                "kill_evankhell_judge", "Jueces Ígneos de Evankhell (Blazes)", 4,
                2200, 1800, 8, new ItemStack(Items.BLAZE_ROD, 4), "Varas Llameantes");

        register(3, 2,
                "Brasas del Horno Abisal",
                "Se solicita una remesa de ladrillos y brasas forjadas en el calor del segundo piso para abastecer a los herreros rúnicos del campamento.",
                "collect_nether_bricks", "Ladrillos del Nether", 12,
                1800, 1500, 5, new ItemStack(Items.MAGMA_BLOCK, 6), "Bloques de Magma");

        register(4, 3,
                "El Ojo Vigilante del Océano de Shinsu",
                "En el tercer piso, densas corrientes de Shinsu azulado han despertado a los antiguos Ojos Vigilantes. Neutraliza a estos colosos marinos antes de que ahoguen la cámara de prueba.",
                "kill_shinsoo_eye", "Ojos de Shinsu (Elder Guardians)", 2,
                3000, 2500, 10, new ItemStack(Items.PRISMARINE_SHARD, 8), "Esquirlas de Prismarina");

        register(5, 3,
                "Gemas de la Marea Resonante",
                "Los alquimistas de la torre precisan prismarina pulida imbuida con la presión acuática del Piso 3 para fabricar elixires de respiración de Shinsu.",
                "collect_prismarine", "Bloques de Prismarina", 8,
                2500, 2000, 7, new ItemStack(Items.SEA_LANTERN, 4), "Linternas Marinas");

        register(6, 4,
                "Duelo de la Corona: Colosos de Piedra",
                "El juego de la Corona del cuarto piso ha comenzado. Titanes de piedra negra custodian el trono central; desafíalos cara a cara y arrebátales su insignia marcial.",
                "kill_crown_champion", "Campeones de la Corona", 4,
                3800, 3200, 12, new ItemStack(Items.GOLD_INGOT, 6), "Lingotes de Oro Refinado");

        register(7, 4,
                "Tributo Áureo del Conquistador",
                "Para ser reconocido por los oficiantes del cuarto piso, entrega lingotes de oro forjados como testimonio de tu riqueza y poder en combate.",
                "collect_gold", "Lingotes de Oro", 10,
                3200, 2800, 10, new ItemStack(Items.GILDED_BLACKSTONE, 4), "Piedra Negra Dorada");

        register(8, 5,
                "Cacería de Nombres de la Mauler",
                "Criaturas voraces conocidas como Maulers acechan en los desfiladeros del quinto piso. Destrúyelas para evitar que devoren a las comitivas de novatos.",
                "kill_alpha_mauler", "Maulers Alfas de la Torre", 5,
                4600, 4000, 15, new ItemStack(Items.CRYING_OBSIDIAN, 6), "Obsidiana Resonante");

        register(9, 5,
                "Ecos Púrpuras del Vacío",
                "Los monolitos del quinto piso desprenden resonancias púrpuras. Recolecta fragmentos de purpur de alta densidad para las cámaras de teletransporte.",
                "collect_purpur", "Bloques de Purpur", 12,
                4000, 3500, 12, new ItemStack(Items.PURPUR_PILLAR, 6), "Pilares de Purpur");

        // --- PISOS 6 A 10 (TIER INTERMEDIO) ---
        register(10, 6,
                "Expreso del Infierno: Bestias Glaciales",
                "Las vías heladas del Piso 6 son patrulladas por feroces bestias de nieve y hechiceros de escarcha. Pon a prueba tu resistencia térmica eliminando a los atacantes.",
                "kill_iceologer", "Bestias Glaciales del Expreso", 3,
                5500, 4800, 18, new ItemStack(Items.PACKED_ICE, 12), "Hielo Compacto Rúnico");

        register(11, 6,
                "Piedras de Andesita Glacial",
                "Se necesitan bloques densos de andesita del sexto piso para apuntalar las vías congeladas del ferrocarril antes del próximo convoy.",
                "collect_andesite", "Andesita Pulida", 16,
                4800, 4200, 14, new ItemStack(Items.AMETHYST_SHARD, 6), "Esquirlas de Amatista");

        register(12, 7,
                "Guardián del Piso de la Muerte",
                "El suelo del séptimo piso está impregnado de sculk y almas errantes. El Guardián de las Sombras vigila la puerta y no permitirá el paso a los débiles.",
                "kill_death_warden", "Guardianes de la Muerte (Wither)", 3,
                6800, 6000, 20, new ItemStack(Items.SCULK_CATALYST, 2), "Catalizador de Sculk");

        register(13, 7,
                "Osamentas Ancestrales del Umbral",
                "Consigue huesos impregnados de la energía arcana del séptimo piso para alimentar los calderos de transmutación del sistema.",
                "collect_bone", "Huesos Arcanos", 20,
                5800, 5000, 16, new ItemStack(Items.BONE_BLOCK, 4), "Bloques de Hueso Macizo");

        register(14, 8,
                "Ira del Monarca del Fuego Salvaje",
                "Tormentas de fuego azotan la octava planta. Entidades superiores de fuego puro consumen a todo aquel que ose desafiar la gravedad de la torre.",
                "kill_wildfire_monarch", "Monarcas del Fuego Salvaje", 3,
                8000, 7200, 25, new ItemStack(Items.FIRE_CHARGE, 6), "Cargas Ígneas Arcanas");

        register(15, 8,
                "Obsidiana de Corazón Incandescente",
                "Mina y entrega bloques de obsidiana llorosa capaces de soportar las altísimas temperaturas del octavo piso.",
                "collect_crying_obsidian", "Obsidiana Llorosa Pura", 8,
                7000, 6200, 20, new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS, 8), "Ladrillos Negros Pulidos");

        register(16, 9,
                "Avatares del Alto Rango",
                "Figuras ilusorias que replican el poder de antiguos Altos Rankers guardan los accesos al noveno piso. Derrótalos para demostrar tu supremacía.",
                "kill_high_ranker", "Avatares de Alto Ranker", 2,
                9500, 8500, 30, new ItemStack(Items.DIAMOND, 4), "Diamantes Celestiales");

        register(17, 9,
                "Filones de Cuarzo Rúnico",
                "Extrae cuarzo de pureza celestial del noveno piso para alimentar las balizas luminosas de la corona de la torre.",
                "collect_quartz", "Cuarzo Rúnico", 16,
                8200, 7400, 24, new ItemStack(Items.QUARTZ_BLOCK, 4), "Bloques de Cuarzo");

        register(18, 10,
                "Prueba Mayor: El Meridiano de la Década",
                "El décimo piso marca la primera gran barrera para los aspirantes. Ningún cazador ordinario pasa sin purgar a los ocho centinelas de élite de la cámara principal.",
                "kill_floor_mobs", "Enemigos de Élite del Piso 10", 8,
                12000, 11000, 35, new ItemStack(Items.NETHERITE_SCRAP, 2), "Fragmentos de Netherite");

        // --- PISOS 12 A 20 (TIER AVANZADO) ---
        register(19, 12,
                "Incursión en los Bosques Luminiscentes",
                "En el piso 12, hongos arcanos gigantes iluminan la oscuridad. Cosecha lámparas naturales para iluminar los campamentos base de la asociación.",
                "collect_shroomlight", "Hongos Luminiscentes (Shroomlight)", 8,
                7500, 6500, 20, new ItemStack(Items.GLOWSTONE, 8), "Piedra Brillante");

        register(20, 15,
                "Asalto a la Fortaleza de Cristal",
                "El piso 15 está repleto de constructos de cristal y amatista que emiten pulsos sónicos. Destrúyelos para asegurar el anillo de transporte.",
                "kill_floor_mobs", "Enemigos de Cristal del Piso 15", 7,
                9000, 8000, 25, new ItemStack(Items.AMETHYST_CLUSTER, 4), "Cúmulos de Amatista");

        register(21, 18,
                "Cosecha Ígnea en el Abismo Magmático",
                "El piso 18 es un lago de lava en ebullición. Se precisan varas de fuego y bloques de magma para energizar los ascensores rúnicos.",
                "collect_blaze_rod", "Varas de Blaze", 8,
                10000, 9000, 28, new ItemStack(Items.MAGMA_BLOCK, 8), "Bloques de Magma");

        register(22, 20,
                "Campaña de la Segunda Década",
                "Conquista el piso 20 y somete a los campeones que resguardan la puerta del piso superior. Solo los verdaderos Rankers pueden reclamar esta gloria.",
                "kill_floor_mobs", "Guardianes de la Segunda Década", 8,
                14000, 13000, 40, new ItemStack(Items.GOLD_BLOCK, 4), "Bloques de Oro Sólido");

        // --- PISOS 25 A 50 (TIER MAESTRO) ---
        register(23, 25,
                "Vanguardia de Acero Sombrío",
                "Las sombras del piso 25 se organizan para repeler a los cazadores. Acaba con ocho de sus vanguardias para debilitar su línea de defensa.",
                "kill_floor_mobs", "Vanguardias del Piso 25", 8,
                16000, 15000, 45, new ItemStack(Items.DIAMOND, 6), "Diamantes Puros");

        register(24, 30,
                "Cámara del Vacío: Autómatas Rúnicos",
                "Los autómatas del trigésimo piso son invulnerables a ataques mundanos. Elimínalos utilizando tus mejores habilidades del Sistema.",
                "kill_floor_mobs", "Autómatas del Piso 30", 8,
                18000, 17000, 50, new ItemStack(Items.NETHERITE_SCRAP, 3), "Fragmentos de Netherite");

        register(25, 35,
                "Espectros del Shinsu Negro",
                "En el piso 35 el Shinsu se ha tornado oscuro como el alquitrán. Extermina a diez espectros para permitir que los eruditos purifiquen el ambiente.",
                "kill_floor_mobs", "Espectros del Piso 35", 10,
                22000, 20000, 55, new ItemStack(Items.LAPIS_LAZULI, 16), "Lapislázuli Arcano");

        register(26, 40,
                "Prueba del Meridiano Central",
                "El cuadragésimo piso alberga a los guardianes veteranos de la Torre. Supera este umbral para consagrarte como una leyenda del ranking.",
                "kill_floor_mobs", "Veteranos del Piso 40", 10,
                26000, 24000, 60, new ItemStack(Items.NETHERITE_INGOT, 1), "Lingote de Netherite");

        register(27, 45,
                "Titanes de la Brecha Dimensional",
                "Colosales bestias emergen de fracturas dimensionales en el piso 45. Neutralízalas antes de que su masa colapse la cámara entera.",
                "kill_floor_mobs", "Titanes del Piso 45", 10,
                30000, 28000, 65, new ItemStack(Items.DIAMOND, 10), "Diamantes Puros");

        register(28, 50,
                "El Trono de la Quincuagésima Planta",
                "Has alcanzado el ecuador de la Torre. Conquistar la planta 50 exige una masacre sin piedad contra los defensores imperiales.",
                "kill_floor_mobs", "Defensores del Piso 50", 12,
                35000, 32000, 75, new ItemStack(Items.NETHERITE_INGOT, 2), "Lingotes de Netherite");

        // --- PISOS 60 A 100 (TIER SUPREMO / CIMA DIVINA) ---
        register(29, 60,
                "Anomalía en la Zona Muerta",
                "En el piso 60 no rigen las leyes naturales de la física ni la gravedad. Elimina a las doce abominaciones nacidas de la distorsión.",
                "kill_floor_mobs", "Abominaciones del Piso 60", 12,
                40000, 38000, 80, new ItemStack(Items.DIAMOND_BLOCK, 2), "Bloques de Diamante");

        register(30, 70,
                "Confrontación en el Abismo Astral",
                "Las fuerzas del abismo cósmico custodian el piso 70. Los cazadores que llegan aquí son leyendas vivientes. Extermina a sus legiones.",
                "kill_floor_mobs", "Legionarios del Piso 70", 12,
                46000, 44000, 90, new ItemStack(Items.NETHERITE_INGOT, 3), "Lingotes de Netherite");

        register(31, 80,
                "Cónclave de los Altos Monarcas",
                "El octogésimo piso es la corte privada de antiguos reyes caídos. Derrótalos en su propio salón y arrebata sus insignias reales.",
                "kill_floor_mobs", "Monarcas del Piso 80", 12,
                52000, 50000, 100, new ItemStack(Items.NETHERITE_INGOT, 4), "Lingotes de Netherite");

        register(32, 90,
                "El Penúltimo Umbral Celestial",
                "A solo diez pisos de la cima, el Shinsu es tan denso que puede aplastar montañas. Desafía y vence a catorce avatares divinos.",
                "kill_floor_mobs", "Avatares Divinos del Piso 90", 14,
                60000, 60000, 120, new ItemStack(Items.NETHERITE_BLOCK, 1), "Bloque de Netherite");

        register(33, 95,
                "Vanguardia del Gran Administrador",
                "Los lugartenientes personales del Administrador del Piso 100 defienden las escalinatas hacia la cumbre. Ningún paso atrás.",
                "kill_floor_mobs", "Lugartenientes del Piso 95", 15,
                70000, 70000, 140, new ItemStack(Items.NETHERITE_INGOT, 5), "Lingotes de Netherite");

        register(34, 100,
                "CIMA DIVINA: El Gran Administrador",
                "El desafío máximo de la Torre. Enfréntate a la entidad suprema que rige los 100 pisos en la cumbre absoluta de la existencia.",
                "kill_grand_administrator", "Gran Administrador de la Cima", 1,
                100000, 100000, 200, new ItemStack(Items.NETHERITE_BLOCK, 2), "Bloques de Netherite Divino");

        register(35, 100,
                "Purgatorio de los Guardianes Divinos",
                "Cuatro guardianes celestiales protegen el trono del Administrador. Extermínalos a todos para coronarte como el Soberano de la Torre.",
                "kill_divine_guardian", "Guardianes Divinos del Piso 100", 4,
                85000, 85000, 160, new ItemStack(Items.NETHERITE_INGOT, 6), "Lingotes de Netherite");
    }

    private static void register(int id, int floor, String title, String narrative,
                                 String targetType, String targetName, int targetCount,
                                 int coins, int exp, int gems, ItemStack material, String materialName) {
        ALL_MISSIONS.add(new TowerMissionTemplate(id, floor, title, narrative, targetType, targetName, targetCount, coins, exp, gems, material, materialName));
    }

    /**
     * Determina las 10 misiones del tablón diario para el día especificado.
     * La selección está estratificada a lo largo de todo el espectro de la Torre (pisos 1 a 100):
     * - Slot 0: Contrato inicial garantizado (Piso 1: índices 0 o 1)
     * - Slot 1: Piso 2 (índices 2 o 3)
     * - Slot 2: Pisos 3 y 4 (índices 4..7)
     * - Slot 3: Piso 5 (índices 8 o 9)
     * - Slot 4: Pisos 6 y 7 (índices 10..13)
     * - Slot 5: Pisos 8 y 9 (índices 14..17)
     * - Slot 6: Pisos 10 y 12 (índices 18 o 19)
     * - Slot 7: Pisos 15, 18 y 20 (índices 20..22)
     * - Slot 8: Pisos 25 a 50 (índices 23..28)
     * - Slot 9: Pisos 60 a 100 (índices 29..35) con el Sello Carmesí de la Cumbre
     * Esto asegura que los aspirantes siempre tengan contratos accesibles desde el primer día,
     * y al mismo tiempo puedan contemplar contratos épicos de pisos superiores.
     */
    public static int[] getDailyMissionIndicesForDay(long worldDay) {
        Random rng = new Random(worldDay * 0x7F4A7C15L + 1337L);

        // Slot 0: Piso 1 (0..1)
        List<Integer> pool0 = new ArrayList<>(List.of(0, 1));
        Collections.shuffle(pool0, rng);

        // Slot 1: Piso 2 (2..3)
        List<Integer> pool1 = new ArrayList<>(List.of(2, 3));
        Collections.shuffle(pool1, rng);

        // Slot 2: Pisos 3..4 (4..7)
        List<Integer> pool2 = new ArrayList<>(List.of(4, 5, 6, 7));
        Collections.shuffle(pool2, rng);

        // Slot 3: Piso 5 (8..9)
        List<Integer> pool3 = new ArrayList<>(List.of(8, 9));
        Collections.shuffle(pool3, rng);

        // Slot 4: Pisos 6..7 (10..13)
        List<Integer> pool4 = new ArrayList<>(List.of(10, 11, 12, 13));
        Collections.shuffle(pool4, rng);

        // Slot 5: Pisos 8..9 (14..17)
        List<Integer> pool5 = new ArrayList<>(List.of(14, 15, 16, 17));
        Collections.shuffle(pool5, rng);

        // Slot 6: Pisos 10..12 (18..19)
        List<Integer> pool6 = new ArrayList<>(List.of(18, 19));
        Collections.shuffle(pool6, rng);

        // Slot 7: Pisos 15..20 (20..22)
        List<Integer> pool7 = new ArrayList<>(List.of(20, 21, 22));
        Collections.shuffle(pool7, rng);

        // Slot 8: Pisos 25..50 (23..28)
        List<Integer> pool8 = new ArrayList<>(List.of(23, 24, 25, 26, 27, 28));
        Collections.shuffle(pool8, rng);

        // Slot 9: Pisos 60..100 (29..35)
        List<Integer> pool9 = new ArrayList<>(List.of(29, 30, 31, 32, 33, 34, 35));
        Collections.shuffle(pool9, rng);

        int[] result = new int[DAILY_OFFERS_COUNT];
        result[0] = pool0.get(0);
        result[1] = pool1.get(0);
        result[2] = pool2.get(0);
        result[3] = pool3.get(0);
        result[4] = pool4.get(0);
        result[5] = pool5.get(0);
        result[6] = pool6.get(0);
        result[7] = pool7.get(0);
        result[8] = pool8.get(0);
        result[9] = pool9.get(0);

        return result;
    }

    public static long getCurrentWorldDay(Level level) {
        return level.getDayTime() / 24000L;
    }

    public static boolean isMissionLockedForPlayer(Player player, TowerMissionTemplate template) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("tower_has_entered")) return true;
        SystemData data = SystemEvents.data(player);
        int savedFloor = data.savedTowerFloor();
        return template.floor() > savedFloor;
    }

    public static int countCollectionItems(Player player, String targetType) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            if (matchesCollectionTarget(stack, targetType)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean consumeCollectionItems(Player player, String targetType, int amountNeeded) {
        if (countCollectionItems(player, targetType) < amountNeeded) return false;
        int remaining = amountNeeded;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;
            if (matchesCollectionTarget(stack, targetType)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    private static boolean matchesCollectionTarget(ItemStack stack, String targetType) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return switch (targetType) {
            case "collect_deepslate" -> path.contains("deepslate");
            case "collect_nether_bricks" -> path.contains("nether_brick");
            case "collect_prismarine" -> path.contains("prismarine");
            case "collect_gold" -> stack.is(Items.GOLD_INGOT) || stack.is(Items.RAW_GOLD);
            case "collect_purpur" -> path.contains("purpur");
            case "collect_andesite" -> path.contains("andesite");
            case "collect_bone" -> stack.is(Items.BONE);
            case "collect_crying_obsidian" -> stack.is(Items.CRYING_OBSIDIAN);
            case "collect_quartz" -> stack.is(Items.QUARTZ);
            case "collect_shroomlight" -> stack.is(Items.SHROOMLIGHT);
            case "collect_blaze_rod" -> stack.is(Items.BLAZE_ROD);
            default -> false;
        };
    }

    public static void openBoardFor(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        long currentDay = getCurrentWorldDay(player.level());
        SystemData data = SystemEvents.data(player);
        boolean hasEntered = tag.getBoolean("tower_has_entered");
        int savedFloor = hasEntered ? data.savedTowerFloor() : 0;

        int[] dailyIndices = getDailyMissionIndicesForDay(currentDay);

        boolean hasActive = tag.getBoolean("tower_mission_active");
        int activeIdx = tag.getInt("tower_mission_index");
        int activeKilled = tag.getInt("tower_mission_progress");
        int activeNeeded = tag.getInt("tower_mission_needed");

        // Si es misión de recolección, actualizar progreso basado en inventario
        if (hasActive && activeIdx >= 0 && activeIdx < ALL_MISSIONS.size()) {
            TowerMissionTemplate template = ALL_MISSIONS.get(activeIdx);
            if (template.isCollection()) {
                activeKilled = Math.min(template.targetCount(), countCollectionItems(player, template.targetType()));
                tag.putInt("tower_mission_progress", activeKilled);
            }
        }

        boolean activeReadyToClaim = hasActive && activeKilled >= activeNeeded;

        // Comprobar misiones completadas hoy
        boolean[] completedToday = new boolean[DAILY_OFFERS_COUNT];
        for (int i = 0; i < DAILY_OFFERS_COUNT; i++) {
            int gIdx = dailyIndices[i];
            completedToday[i] = tag.getLong("tower_mission_comp_day_" + gIdx) == currentDay;
        }

        PacketDistributor.sendToPlayer(player, new OpenTowerMissionBoardPayload(
                currentDay, savedFloor, dailyIndices,
                hasActive, activeIdx, activeKilled, activeNeeded, activeReadyToClaim,
                completedToday
        ));

        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void acceptMission(ServerPlayer player, int missionIndex) {
        if (missionIndex < 0 || missionIndex >= ALL_MISSIONS.size()) return;
        TowerMissionTemplate template = ALL_MISSIONS.get(missionIndex);
        SystemData data = SystemEvents.data(player);

        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("tower_has_entered")) {
            player.sendSystemMessage(Component.literal("§c[!] Debes cruzar primero el Portal hacia la Torre del Ranker para registrarte antes de poder aceptar misiones."));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        // Comprobación de piso estricta
        if (template.floor() > data.savedTowerFloor()) {
            player.sendSystemMessage(Component.literal("§c[!] Este contrato está bloqueado. Requiere conquistar el Piso " + template.floor() + " de la Torre."));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        long currentDay = getCurrentWorldDay(player.level());

        // Comprobar si ya la completó hoy
        if (tag.getLong("tower_mission_comp_day_" + missionIndex) == currentDay) {
            player.sendSystemMessage(Component.literal("§e[!] Ya has completado este contrato de la torre en el día de hoy."));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        int initialProgress = 0;
        if (template.isCollection()) {
            initialProgress = Math.min(template.targetCount(), countCollectionItems(player, template.targetType()));
        }

        tag.putBoolean("tower_mission_active", true);
        tag.putInt("tower_mission_index", missionIndex);
        tag.putInt("tower_mission_floor", template.floor());
        tag.putString("tower_mission_target", template.targetType());
        tag.putString("tower_mission_title", template.title());
        tag.putInt("tower_mission_needed", template.targetCount());
        tag.putInt("tower_mission_progress", initialProgress);

        ServerLevel sl = player.serverLevel();
        sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.2D);
        player.playNotifySound(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.literal("§6📜 §l[CONTRATO DE LA TORRE ACEPTADO] §e" + template.title()));
        player.sendSystemMessage(Component.literal("§7Objetivo: " + template.targetName() + " x" + template.targetCount() + " en el Piso " + template.floor()));
        player.displayClientMessage(Component.literal("§a¡Misión de la Torre aceptada!"), true);

        openBoardFor(player);
    }

    public static void claimMission(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("tower_mission_active")) return;

        int missionIndex = tag.getInt("tower_mission_index");
        if (missionIndex < 0 || missionIndex >= ALL_MISSIONS.size()) return;
        TowerMissionTemplate template = ALL_MISSIONS.get(missionIndex);

        int progress = tag.getInt("tower_mission_progress");
        int needed = tag.getInt("tower_mission_needed");

        if (template.isCollection()) {
            if (!consumeCollectionItems(player, template.targetType(), needed)) {
                player.displayClientMessage(Component.literal("§cNo tienes los materiales requeridos en tu inventario."), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                return;
            }
        } else {
            if (progress < needed) {
                player.displayClientMessage(Component.literal("§cEl objetivo de la misión aún no está completo (" + progress + "/" + needed + ")."), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                return;
            }
        }

        // Marcar completada
        long currentDay = getCurrentWorldDay(player.level());
        tag.putBoolean("tower_mission_active", false);
        tag.putLong("tower_mission_comp_day_" + missionIndex, currentDay);

        // Otorgar Recompensas: Monedas, EXP, Gemas y Materiales
        SystemData data = SystemEvents.data(player);
        data.addCoins(template.rewardCoins());
        data.addExperience(template.rewardExp());

        // Otorgar Primogemas
        if (template.rewardGems() > 0 && SystemItems.PRIMOGEM.isBound()) {
            ItemStack gemStack = new ItemStack(SystemItems.PRIMOGEM.get(), template.rewardGems());
            if (!player.getInventory().add(gemStack)) {
                player.drop(gemStack, false);
            }
        }

        // Otorgar Material
        if (template.rewardMaterial() != null && !template.rewardMaterial().isEmpty()) {
            ItemStack matCopy = template.rewardMaterial().copy();
            if (!player.getInventory().add(matCopy)) {
                player.drop(matCopy, false);
            }
        }

        // Efectos visuales y sonoros
        ServerLevel sl = player.serverLevel();
        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.4D, 0.4D, 0.4D, 0.15D);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.literal("§6§l🏆 [MISIÓN DE LA TORRE COMPLETADA] §f" + template.title())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(String.format("§aRecompensas: §e+%d Monedas §7| §b+%d EXP §7| §d+%d Primogemas §7| §f+%s",
                template.rewardCoins(), template.rewardExp(), template.rewardGems(), template.rewardMaterialName())));
        player.displayClientMessage(Component.literal("§a¡Recompensa del Tablón reclamada con éxito!"), true);

        openBoardFor(player);
    }

    public static void abandonMission(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.getBoolean("tower_mission_active")) {
            tag.putBoolean("tower_mission_active", false);
            player.sendSystemMessage(Component.literal("§c[!] Has abandonado tu misión actual de la torre."));
            player.playNotifySound(SoundEvents.LEASH_KNOT_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
            openBoardFor(player);
        }
    }

    /**
     * Llamado por TowerManager cuando un enemigo muere dentro de la Torre.
     */
    public static void onTowerKill(ServerPlayer player, int sessionFloor, String mobCustomName, String entityTypeKey) {
        if (player == null) return;
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("tower_mission_active")) return;

        int activeIdx = tag.getInt("tower_mission_index");
        if (activeIdx < 0 || activeIdx >= ALL_MISSIONS.size()) return;
        TowerMissionTemplate template = ALL_MISSIONS.get(activeIdx);

        // Si la misión especifica un piso, debe coincidir con el piso actual
        if (template.floor() != sessionFloor) return;

        boolean matches = false;
        String tType = template.targetType();

        if ("kill_headon_beast".equals(tType) && (mobCustomName.contains("Headon") || mobCustomName.contains("Gólem") || mobCustomName.contains("Golem"))) {
            matches = true;
        } else if ("kill_evankhell_judge".equals(tType) && (mobCustomName.contains("Evankhell") || mobCustomName.contains("Juez") || mobCustomName.contains("Judge"))) {
            matches = true;
        } else if ("kill_shinsoo_eye".equals(tType) && (mobCustomName.contains("Shinsu") || mobCustomName.contains("Shinsoo") || mobCustomName.contains("Eye") || mobCustomName.contains("Ojo"))) {
            matches = true;
        } else if ("kill_crown_champion".equals(tType) && (mobCustomName.contains("Corona") || mobCustomName.contains("Crown") || mobCustomName.contains("Champion") || mobCustomName.contains("Campeón"))) {
            matches = true;
        } else if ("kill_alpha_mauler".equals(tType) && (mobCustomName.contains("Mauler") || mobCustomName.contains("Alfa") || mobCustomName.contains("Name Hunter") || mobCustomName.contains("Cazador de Nombres"))) {
            matches = true;
        } else if ("kill_iceologer".equals(tType) && (mobCustomName.contains("Iceologer") || mobCustomName.contains("Expreso") || mobCustomName.contains("Train") || mobCustomName.contains("Hielo"))) {
            matches = true;
        } else if ("kill_death_warden".equals(tType) && (mobCustomName.contains("Muerte") || mobCustomName.contains("Death") || mobCustomName.contains("Warden") || mobCustomName.contains("Illusioner"))) {
            matches = true;
        } else if ("kill_wildfire_monarch".equals(tType) && (mobCustomName.contains("Fuego Salvaje") || mobCustomName.contains("Wildfire") || mobCustomName.contains("Monarch") || mobCustomName.contains("Monarca"))) {
            matches = true;
        } else if ("kill_high_ranker".equals(tType) && (mobCustomName.contains("Ranker") || mobCustomName.contains("Avatar") || mobCustomName.contains("Alto"))) {
            matches = true;
        } else if ("kill_grand_administrator".equals(tType) && (mobCustomName.contains("Administrador") || mobCustomName.contains("Administrator") || mobCustomName.contains("Grand"))) {
            matches = true;
        } else if ("kill_divine_guardian".equals(tType) && (mobCustomName.contains("Guardian") || mobCustomName.contains("Guardián") || mobCustomName.contains("Divine"))) {
            matches = true;
        } else if ("kill_floor_mobs".equals(tType)) {
            matches = true;
        }

        if (matches) {
            int killed = tag.getInt("tower_mission_progress") + 1;
            int needed = tag.getInt("tower_mission_needed");
            tag.putInt("tower_mission_progress", Math.min(killed, needed));

            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);

            if (killed == needed) {
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                player.sendSystemMessage(Component.literal("§6§l✨ [OBJETIVO DE LA TORRE COMPLETADO] §a" + template.title()));
                player.sendSystemMessage(Component.literal("§7Regresa al Tablón de Misiones en la entrada exterior para reclamar tu recompensa."));
                player.displayClientMessage(Component.literal("§a¡Misión completada! Reclama tu recompensa en el Tablón exterior."), true);
            } else if (killed < needed) {
                player.displayClientMessage(Component.literal("§e" + template.targetName() + ": §a" + killed + "§7/§e" + needed), true);
            }
        }
    }
}
