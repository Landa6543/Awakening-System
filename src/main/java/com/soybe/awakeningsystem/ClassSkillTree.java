package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import java.util.*;

public final class ClassSkillTree {
    private ClassSkillTree() {}

    public enum NodeType {
        ROOT,
        MAJOR_ACTIVE,
        MAJOR_PASSIVE,
        ATTRIBUTE_NODE
    }

    public enum StatType {
        NONE,
        ATTACK_DAMAGE,
        MAX_HEALTH,
        MAX_MANA,
        ATTACK_SPEED,
        MOVEMENT_SPEED,
        ARMOR,
        KNOCKBACK_RESISTANCE
    }

    public record SkillNode(
            String skillId,
            String hunterClass,
            String branch, // "root", "branch_a", "branch_b", "branch_c"
            int tier,      // 0 to 5
            int gridX,
            int gridY,
            int requiredLevel,
            int spCost,
            List<String> prerequisiteIds,
            NodeType nodeType,
            StatType statType,
            double statValue,
            String customName,
            String customDesc,
            String iconSymbol
    ) {
        // Constructor para habilidades mayores
        public SkillNode(String skillId, String hunterClass, String branch, int tier, int gridX, int gridY, int requiredLevel, int spCost, List<String> prerequisiteIds, NodeType nodeType, String iconSymbol) {
            this(skillId, hunterClass, branch, tier, gridX, gridY, requiredLevel, spCost, prerequisiteIds, nodeType, StatType.NONE, 0.0D, null, null, iconSymbol);
        }

        // Constructor para nodos de atributos
        public SkillNode(String skillId, String hunterClass, String branch, int tier, int gridX, int gridY, int requiredLevel, int spCost, List<String> prerequisiteIds, StatType statType, double statValue, String customName, String customDesc, String iconSymbol) {
            this(skillId, hunterClass, branch, tier, gridX, gridY, requiredLevel, spCost, prerequisiteIds, NodeType.ATTRIBUTE_NODE, statType, statValue, customName, customDesc, iconSymbol);
        }

        public boolean isRoot() {
            return nodeType == NodeType.ROOT;
        }

        public boolean isAttribute() {
            return nodeType == NodeType.ATTRIBUTE_NODE;
        }

        public boolean isActive() {
            return nodeType == NodeType.MAJOR_ACTIVE || (!isAttribute() && !isRoot() && SkillDefinitions.isActive(skillId));
        }

        public String prerequisiteSkillId() {
            return (prerequisiteIds != null && !prerequisiteIds.isEmpty()) ? prerequisiteIds.get(0) : null;
        }

        public Component getName() {
            SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
            if (def != null) return def.name();
            String skillKey = "awakening_system.skill." + skillId + ".name";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".name";
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, customName != null ? customName : skillId).getString());
        }

        public Component getDescription() {
            SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
            if (def != null) return def.description();
            String skillKey = "awakening_system.skill." + skillId + ".description";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".desc";
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, customDesc != null ? customDesc : "").getString());
        }

        public Component getEffect() {
            if (isAttribute() && statType != StatType.NONE) {
                String valStr = (statValue > 0 ? "+" : "") + (statType == StatType.MOVEMENT_SPEED ? String.format(Locale.US, "%.1f%%", statValue * 100) : (statType == StatType.ATTACK_SPEED ? String.format(Locale.US, "%.0f%%", statValue * 100) : String.format(Locale.US, "%.1f", statValue)));
                return Component.literal(valStr + " ").append(statName(statType));
            }
            SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
            if (def != null) return def.effect();
            String skillKey = "awakening_system.skill." + skillId + ".effect";
            return Component.translatableWithFallback(skillKey, "");
        }

        public SkillDefinitions.Rarity getRarity() {
            if (isRoot()) return SkillDefinitions.Rarity.UNIQUE;
            if (isAttribute()) return SkillDefinitions.Rarity.UNCOMMON;
            SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
            return def != null ? def.rarity() : SkillDefinitions.Rarity.RARE;
        }
    }

    public static Component statName(StatType type) {
        return switch (type) {
            case ATTACK_DAMAGE -> Component.translatable("awakening_system.stat.attack_damage");
            case MAX_HEALTH -> Component.translatable("awakening_system.stat.max_health");
            case MAX_MANA -> Component.translatable("awakening_system.stat.max_mana");
            case ATTACK_SPEED -> Component.translatable("awakening_system.stat.attack_speed");
            case MOVEMENT_SPEED -> Component.translatable("awakening_system.stat.movement_speed");
            case ARMOR -> Component.translatable("awakening_system.stat.armor");
            case KNOCKBACK_RESISTANCE -> Component.translatable("awakening_system.stat.knockback_resistance");
            default -> Component.empty();
        };
    }

    private static final Map<String, List<SkillNode>> CLASS_TREES = new LinkedHashMap<>();
    private static final Map<String, Map<String, SkillNode>> NODES_BY_CLASS_AND_SKILL = new LinkedHashMap<>();
    private static final Map<String, SkillNode> NODES_BY_SKILL = new LinkedHashMap<>();

    private static void register(SkillNode node) {
        CLASS_TREES.computeIfAbsent(node.hunterClass(), k -> new ArrayList<>()).add(node);
        NODES_BY_CLASS_AND_SKILL.computeIfAbsent(node.hunterClass(), k -> new LinkedHashMap<>()).put(node.skillId(), node);
        NODES_BY_SKILL.put(node.skillId(), node);
    }

    static {
        // =========================================================================
        // 1. MONARCA DE LAS SOMBRAS (Solo Leveling)
        // =========================================================================
        register(new SkillNode("shadow_monarch", "shadow_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "👑"));
        // Rama A: Ejército Inmortal (Nigromancia / Daño)
        register(new SkillNode("sm_str_1", "shadow_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("shadow_monarch"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Fuerza de las Sombras I", "Aumenta la fuerza destructiva de tus ataques físicos.", "⚔"));
        register(new SkillNode("shadow_exchange", "shadow_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("sm_str_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("sm_mana_1", "shadow_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("sm_str_1"), StatType.MAX_MANA, 50.0D, "✦ Maná Abisal I", "Amplía la reserva de maná para sostener más invocaciones.", "🔮"));
        register(new SkillNode("monarchs_domain", "shadow_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("shadow_exchange", "sm_mana_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sm_armor_1", "shadow_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("monarchs_domain"), StatType.ARMOR, 3.0D, "✦ Armadura de Penumbra", "Endurece tu defensa envolviéndote en una coraza oscura.", "🛡"));
        register(new SkillNode("shadow_colossus_infusion", "shadow_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("monarchs_domain"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("sm_apex_1", "shadow_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("sm_armor_1", "shadow_colossus_infusion"), StatType.ATTACK_DAMAGE, 5.0D, "✦ Legión del Fin del Mundo", "Poder final: Otorga +5.0 de Daño de Ataque absoluto a todas tus acciones.", "★"));

        // Rama B: Corte del Monarca (Sigilo / Velocidad / Letalidad)
        register(new SkillNode("sm_spd_1", "shadow_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("shadow_monarch"), StatType.MOVEMENT_SPEED, 0.025D, "✦ Zancada del Abismo I", "Aumenta la velocidad al deslizarte entre las sombras.", "⚡"));
        register(new SkillNode("stealth_stalker", "shadow_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("sm_spd_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("sm_atrspd_1", "shadow_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("sm_spd_1"), StatType.ATTACK_SPEED, 0.04D, "✦ Reflejos del Cazador I", "Acelera el ritmo con el que desenvainas tus dagas de sombra.", "⚔"));
        register(new SkillNode("bloodlust_gaze", "shadow_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("stealth_stalker", "sm_atrspd_1"), NodeType.MAJOR_ACTIVE, "☠"));
        register(new SkillNode("sm_crit_1", "shadow_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("bloodlust_gaze"), StatType.ATTACK_DAMAGE, 3.5D, "✦ Dagas del Terror", "Incrementa el impacto letal de cada tajo imbuido de sombra.", "🗡"));
        register(new SkillNode("phantom_ambush", "shadow_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("bloodlust_gaze"), NodeType.MAJOR_PASSIVE, "✦"));
        register(new SkillNode("sm_apex_2", "shadow_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("sm_crit_1", "phantom_ambush"), StatType.ATTACK_SPEED, 0.08D, "✦ Danza Mortal del Monarca", "Poder final: Maximiza la cadencia de asalto de sombras con +8% Vel. Ataque.", "★"));

        // Rama C: Autoridad y Soberanía (Gravedad / Supervivencia)
        register(new SkillNode("sm_hp_1", "shadow_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("shadow_monarch"), StatType.MAX_HEALTH, 6.0D, "✦ Vitalidad del Inframundo I", "Fortalece tu corazón con el aliento de los no-muertos.", "❤"));
        register(new SkillNode("rulers_authority", "shadow_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("sm_hp_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("sm_res_1", "shadow_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("sm_hp_1"), StatType.KNOCKBACK_RESISTANCE, 0.15D, "✦ Temple del Tirano", "Resiste el empuje enemigo con la gravedad del soberano.", "🛡"));
        register(new SkillNode("black_heart_resurrection", "shadow_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("rulers_authority", "sm_res_1"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("sm_hp_2", "shadow_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("black_heart_resurrection"), StatType.MAX_HEALTH, 10.0D, "✦ Vitalidad del Inframundo II", "Otorga una vitalidad suprema forjada en el reino espectral.", "❤"));
        register(new SkillNode("death_mark_execution", "shadow_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("black_heart_resurrection"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("sm_apex_3", "shadow_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("sm_hp_2", "death_mark_execution"), StatType.ARMOR, 5.0D, "✦ Forma del Monarca Supremo", "Poder final: Sella tu cuerpo inmortal otorgando +5.0 Armadura permanente.", "★"));

        // =========================================================================
        // 2. SANTO DE LA ESPADA (Return of the Blossoming Blade / Asrahan)
        // =========================================================================
        register(new SkillNode("asrahan_mana_blade", "sword_saint", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🗡"));
        // Rama A: 24 Pétalos de Ciruelo
        register(new SkillNode("ss_atrspd_1", "sword_saint", "branch_a", 1, 255, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.ATTACK_SPEED, 0.05D, "✦ Esgrima de Hua I", "Acelera el balanceo fluido de tu espada.", "⚔"));
        register(new SkillNode("plum_blossom_rain", "sword_saint", "branch_a", 2, 230, 135, 35, 1, List.of("ss_atrspd_1"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("ss_atk_1", "sword_saint", "branch_a", 2, 280, 135, 35, 1, List.of("ss_atrspd_1"), StatType.ATTACK_DAMAGE, 2.0D, "✦ Filo de Pétalos", "Afila el corte de la espada de maná.", "🗡"));
        register(new SkillNode("twenty_four_sword_forms", "sword_saint", "branch_a", 3, 255, 195, 45, 1, List.of("plum_blossom_rain", "ss_atk_1"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("ss_spd_1", "sword_saint", "branch_a", 4, 230, 255, 55, 1, List.of("twenty_four_sword_forms"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Pasos de Flor de Ciruelo", "Desplazamiento ágil en el campo de batalla.", "⚡"));
        register(new SkillNode("plum_blossom_domain", "sword_saint", "branch_a", 4, 280, 255, 55, 1, List.of("twenty_four_sword_forms"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("plum_blossom_sword_god", "sword_saint", "branch_a", 5, 255, 320, 65, 1, List.of("ss_spd_1", "plum_blossom_domain"), NodeType.MAJOR_ACTIVE, "★"));

        // Rama B: Intención de Espada Daoísta
        register(new SkillNode("ss_atk_2", "sword_saint", "branch_b", 1, 425, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Intención Marcial I", "Condensa Qi puro en cada estocada.", "⚔"));
        register(new SkillNode("sword_intent", "sword_saint", "branch_b", 2, 400, 135, 35, 1, List.of("ss_atk_2"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("ss_mana_1", "sword_saint", "branch_b", 2, 450, 135, 35, 1, List.of("ss_atk_2"), StatType.MAX_MANA, 40.0D, "✦ Dantian de Maná", "Amplía el maná canalizado a través del arma.", "🔮"));
        register(new SkillNode("daoist_clarity", "sword_saint", "branch_b", 3, 425, 195, 45, 1, List.of("sword_intent", "ss_mana_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("ss_atrspd_2", "sword_saint", "branch_b", 4, 400, 255, 55, 1, List.of("daoist_clarity"), StatType.ATTACK_SPEED, 0.06D, "✦ Esgrima Trascendente", "Velocidad de estocada cegadora.", "⚔"));
        register(new SkillNode("sword_heart_awakening", "sword_saint", "branch_b", 4, 450, 255, 55, 1, List.of("daoist_clarity"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("limitless_sword_intent", "sword_saint", "branch_b", 5, 425, 320, 65, 1, List.of("ss_atrspd_2", "sword_heart_awakening"), NodeType.MAJOR_PASSIVE, "★"));

        // Rama C: Bastión de Acero y Maná Asrahan
        register(new SkillNode("ss_hp_1", "sword_saint", "branch_c", 1, 85, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.MAX_HEALTH, 6.0D, "✦ Fortaleza Asrahan I", "Acondicionamiento físico del caballero de sangre de hierro.", "❤"));
        register(new SkillNode("ss_armor_1", "sword_saint", "branch_c", 2, 60, 135, 35, 1, List.of("ss_hp_1"), StatType.ARMOR, 3.0D, "✦ Coraza de Plata", "Armadura reforzada con filamentos de maná.", "🛡"));
        register(new SkillNode("ss_kb_1", "sword_saint", "branch_c", 2, 110, 135, 35, 1, List.of("ss_hp_1"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Firmeza Inamovible", "Inmunidad al empuje en duelos frontales.", "🛡"));
        register(new SkillNode("iron_will", "sword_saint", "branch_c", 3, 85, 195, 45, 1, List.of("ss_armor_1", "ss_kb_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("ss_hp_2", "sword_saint", "branch_c", 4, 60, 255, 55, 1, List.of("iron_will"), StatType.MAX_HEALTH, 8.0D, "✦ Fortaleza Asrahan II", "Vitalidad inquebrantable de caballero maestro.", "❤"));
        register(new SkillNode("ss_armor_2", "sword_saint", "branch_c", 4, 110, 255, 55, 1, List.of("iron_will"), StatType.ARMOR, 4.0D, "✦ Coraza Inmortal", "Blindaje total contra cortes y golpes contundentes.", "🛡"));
        register(new SkillNode("ss_apex_3", "sword_saint", "branch_c", 5, 85, 320, 65, 1, List.of("ss_hp_2", "ss_armor_2"), StatType.ATTACK_DAMAGE, 4.0D, "✦ Juicio de Acero Asrahan", "Poder final: Convierte la resistencia pura en +4.0 Daño de Ataque.", "★"));

        // =========================================================================
        // 3. APÓSTOL DEL STAR STREAM (Omniscient Reader's Viewpoint)
        // =========================================================================
        register(new SkillNode("fourth_wall", "star_apostle", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "📜"));
        // Rama A: Puntos de Vista del Lector
        register(new SkillNode("sa_mana_1", "star_apostle", "branch_a", 1, 255, 75, 30, 1, List.of("fourth_wall"), StatType.MAX_MANA, 50.0D, "✦ Caudal de Historias I", "Mayor reserva de maná para canalizar relatos estelares.", "🔮"));
        register(new SkillNode("readers_viewpoint", "star_apostle", "branch_a", 2, 230, 135, 35, 1, List.of("sa_mana_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("sa_atk_1", "star_apostle", "branch_a", 2, 280, 135, 35, 1, List.of("sa_mana_1"), StatType.ATTACK_DAMAGE, 2.0D, "✦ Fragmento de Fábula", "Potencia el impacto de tus golpes con fragmentos de historias.", "⚔"));
        register(new SkillNode("constellation_favor", "star_apostle", "branch_a", 3, 255, 195, 45, 1, List.of("readers_viewpoint", "sa_atk_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sa_spd_1", "star_apostle", "branch_a", 4, 230, 255, 55, 1, List.of("constellation_favor"), StatType.MOVEMENT_SPEED, 0.025D, "✦ Paso del Protagonista", "Movilidad ligera para escapar de situaciones críticas.", "⚡"));
        register(new SkillNode("fourth_wall_shatter", "star_apostle", "branch_a", 4, 280, 255, 55, 1, List.of("constellation_favor"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("star_stream_climax", "star_apostle", "branch_a", 5, 255, 320, 65, 1, List.of("sa_spd_1", "fourth_wall_shatter"), NodeType.MAJOR_ACTIVE, "★"));

        // Rama B: Contratos y Destino
        register(new SkillNode("sa_atrspd_1", "star_apostle", "branch_b", 1, 425, 75, 30, 1, List.of("fourth_wall"), StatType.ATTACK_SPEED, 0.04D, "✦ Agilidad del Cronista I", "Velocidad de reacción ante ataques inesperados.", "⚔"));
        register(new SkillNode("bookmark_skill", "star_apostle", "branch_b", 2, 400, 135, 35, 1, List.of("sa_atrspd_1"), NodeType.MAJOR_ACTIVE, "📑"));
        register(new SkillNode("sa_mana_2", "star_apostle", "branch_b", 2, 450, 135, 35, 1, List.of("sa_atrspd_1"), StatType.MAX_MANA, 60.0D, "✦ Registro Infinito", "Maná expandido para sostener múltiples marcadores activos.", "🔮"));
        register(new SkillNode("probability_distortion", "star_apostle", "branch_b", 3, 425, 195, 45, 1, List.of("bookmark_skill", "sa_mana_2"), NodeType.MAJOR_ACTIVE, "🎲"));
        register(new SkillNode("sa_atk_2", "star_apostle", "branch_b", 4, 400, 255, 55, 1, List.of("probability_distortion"), StatType.ATTACK_DAMAGE, 3.5D, "✦ Resonancia de Fábula", "Aumenta el daño de ataque en sinergia con el Star Stream.", "⚔"));
        register(new SkillNode("bookmark_mastery", "star_apostle", "branch_b", 4, 450, 255, 55, 1, List.of("probability_distortion"), NodeType.MAJOR_PASSIVE, "📑"));
        register(new SkillNode("character_summary_scan", "star_apostle", "branch_b", 5, 425, 320, 65, 1, List.of("sa_atk_2", "bookmark_mastery"), NodeType.MAJOR_PASSIVE, "★"));

        // Rama C: La Cuarta Pared (Defensa Conceptual)
        register(new SkillNode("sa_hp_1", "star_apostle", "branch_c", 1, 85, 75, 30, 1, List.of("fourth_wall"), StatType.MAX_HEALTH, 6.0D, "✦ Protección de la Pared I", "Aumenta la salud máxima reduciendo el impacto emocional.", "❤"));
        register(new SkillNode("sa_wall_shield", "star_apostle", "branch_c", 2, 60, 135, 35, 1, List.of("sa_hp_1"), NodeType.MAJOR_PASSIVE, "🧱"));
        register(new SkillNode("sa_armor_1", "star_apostle", "branch_c", 2, 110, 135, 35, 1, List.of("sa_hp_1"), StatType.ARMOR, 3.0D, "✦ Escudo de Texto", "Armadura conceptual generada por las líneas del guion.", "🛡"));
        register(new SkillNode("sa_pain_suppression", "star_apostle", "branch_c", 3, 85, 195, 45, 1, List.of("sa_wall_shield", "sa_armor_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("sa_hp_2", "star_apostle", "branch_c", 4, 60, 255, 55, 1, List.of("sa_pain_suppression"), StatType.MAX_HEALTH, 8.0D, "✦ Muro Infranqueable", "Vitalidad suprema ante peligros de calibre cósmico.", "❤"));
        register(new SkillNode("sa_res_1", "star_apostle", "branch_c", 4, 110, 255, 55, 1, List.of("sa_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Estabilidad del Lector", "Firmeza absoluta ante embates de monstruos de escenario.", "🛡"));
        register(new SkillNode("sa_apex_3", "star_apostle", "branch_c", 5, 85, 320, 65, 1, List.of("sa_hp_2", "sa_res_1"), StatType.MAX_HEALTH, 10.0D, "✦ Epílogo Eterno", "Poder final: Otorga +10.0 de Salud Máxima convirtiéndote en inmortal de relato.", "★"));

        // =========================================================================
        // 4. BERSERKER COLOSAL (Survival Story of a Sword King)
        // =========================================================================
        register(new SkillNode("cb_valterian_instinct", "colossal_berserker", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🪓"));
        // Rama A: Fuerza Bruta de la Guía
        register(new SkillNode("cb_atk_1", "colossal_berserker", "branch_a", 1, 255, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.ATTACK_DAMAGE, 3.0D, "✦ Músculos Valterianos I", "Poder físico monstruoso que destruye defensas.", "⚔"));
        register(new SkillNode("aura_smash", "colossal_berserker", "branch_a", 2, 230, 135, 35, 1, List.of("cb_atk_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_kb_1", "colossal_berserker", "branch_a", 2, 280, 135, 35, 1, List.of("cb_atk_1"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Peso Colosal", "Masa muscular imparable que ignora el empuje.", "🛡"));
        register(new SkillNode("cross_impact", "colossal_berserker", "branch_a", 3, 255, 195, 45, 1, List.of("aura_smash", "cb_kb_1"), NodeType.MAJOR_ACTIVE, "⚔"));
        register(new SkillNode("cb_atk_2", "colossal_berserker", "branch_a", 4, 230, 255, 55, 1, List.of("cross_impact"), StatType.ATTACK_DAMAGE, 4.0D, "✦ Impacto Demoledor", "Daño masivo concentrado en cada tajo de mandoble.", "🪓"));
        register(new SkillNode("valterian_gigantomachia", "colossal_berserker", "branch_a", 4, 280, 255, 55, 1, List.of("cross_impact"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_apex_1", "colossal_berserker", "branch_a", 5, 255, 320, 65, 1, List.of("cb_atk_2", "valterian_gigantomachia"), StatType.ATTACK_DAMAGE, 6.0D, "✦ Titán de la Destrucción", "Poder final: +6.0 Daño de Ataque devastador permanente.", "★"));

        // Rama B: Supervivencia Extrema
        register(new SkillNode("cb_hp_1", "colossal_berserker", "branch_b", 1, 425, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.MAX_HEALTH, 8.0D, "✦ Constitución Bárbara I", "Incrementa la vitalidad de supervivencia.", "❤"));
        register(new SkillNode("iron_muscle_hardening", "colossal_berserker", "branch_b", 2, 400, 135, 35, 1, List.of("cb_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("cb_armor_1", "colossal_berserker", "branch_b", 2, 450, 135, 35, 1, List.of("cb_hp_1"), StatType.ARMOR, 3.5D, "✦ Piel de Acero", "Piel endurecida por incontables batallas a muerte.", "🛡"));
        register(new SkillNode("immortal_berserk", "colossal_berserker", "branch_b", 3, 425, 195, 45, 1, List.of("iron_muscle_hardening", "cb_armor_1"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("cb_hp_2", "colossal_berserker", "branch_b", 4, 400, 255, 55, 1, List.of("immortal_berserk"), StatType.MAX_HEALTH, 12.0D, "✦ Constitución Bárbara II", "Vitalidad titanica capaz de soportar golpes de dragón.", "❤"));
        register(new SkillNode("blood_frenzy_passive", "colossal_berserker", "branch_b", 4, 450, 255, 55, 1, List.of("immortal_berserk"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("cb_apex_2", "colossal_berserker", "branch_b", 5, 425, 320, 65, 1, List.of("cb_hp_2", "blood_frenzy_passive"), StatType.MAX_HEALTH, 16.0D, "✦ Inmortalidad Valteriana", "Poder final: Otorga +16.0 de Salud Máxima absoluta.", "★"));

        // Rama C: Furia Sanguinaria
        register(new SkillNode("cb_atrspd_1", "colossal_berserker", "branch_c", 1, 85, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.ATTACK_SPEED, 0.04D, "✦ Furia en Combate", "Acelera el ritmo desenfrenado de golpes con armas pesadas.", "⚔"));
        register(new SkillNode("cb_bloodthirst", "colossal_berserker", "branch_c", 2, 60, 135, 35, 1, List.of("cb_atrspd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("cb_spd_1", "colossal_berserker", "branch_c", 2, 110, 135, 35, 1, List.of("cb_atrspd_1"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Carga del Berserker", "Mayor velocidad de persecución hacia la presa.", "⚡"));
        register(new SkillNode("seismic_roar", "colossal_berserker", "branch_c", 3, 85, 195, 45, 1, List.of("cb_bloodthirst", "cb_spd_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_armor_2", "colossal_berserker", "branch_c", 4, 60, 255, 55, 1, List.of("seismic_roar"), StatType.ARMOR, 4.0D, "✦ Blindaje de Guerra", "Armadura colosal reforzada para resistir asedios.", "🛡"));
        register(new SkillNode("cb_pain_suppression", "colossal_berserker", "branch_c", 4, 110, 255, 55, 1, List.of("seismic_roar"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("cb_apex_3", "colossal_berserker", "branch_c", 5, 85, 320, 65, 1, List.of("cb_armor_2", "cb_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.40D, "✦ Fortaleza Indomable", "Poder final: +0.40 Resistencia al Empuje inamovible.", "★"));

        // =========================================================================
        // 5. DEMONIO CELESTIAL (Nano Machine)
        // =========================================================================
        register(new SkillNode("black_fire_wave", "demonic_cultivator", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🔥"));
        // Rama A: Espada del Demonio Celestial
        register(new SkillNode("dc_atk_1", "demonic_cultivator", "branch_a", 1, 255, 75, 30, 1, List.of("black_fire_wave"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Qi Demoníaco I", "Condensa fuego oscuro en la punta de tu espada.", "⚔"));
        register(new SkillNode("heavenly_demon_steps", "demonic_cultivator", "branch_a", 2, 230, 135, 35, 1, List.of("dc_atk_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("dc_atrspd_1", "demonic_cultivator", "branch_a", 2, 280, 135, 35, 1, List.of("dc_atk_1"), StatType.ATTACK_SPEED, 0.05D, "✦ Danza de Tajos I", "Acelera los tajos circulares de esgrima demoníaca.", "⚔"));
        register(new SkillNode("demonic_blood_slash", "demonic_cultivator", "branch_a", 3, 255, 195, 45, 1, List.of("heavenly_demon_steps", "dc_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("dc_atk_2", "demonic_cultivator", "branch_a", 4, 230, 255, 55, 1, List.of("demonic_blood_slash"), StatType.ATTACK_DAMAGE, 4.0D, "✦ Filo de Sangre Celestial", "Incrementa el daño letal de Qi demoníaco.", "⚔"));
        register(new SkillNode("sky_demon_nine_reversals", "demonic_cultivator", "branch_a", 4, 280, 255, 55, 1, List.of("demonic_blood_slash"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("dc_apex_1", "demonic_cultivator", "branch_a", 5, 255, 320, 65, 1, List.of("dc_atk_2", "sky_demon_nine_reversals"), StatType.ATTACK_DAMAGE, 5.0D, "✦ Señor del Murim Celestial", "Poder final: +5.0 Daño de Ataque supremo.", "★"));

        // Rama B: Reconfiguración Nano-Molecular
        register(new SkillNode("dc_mana_1", "demonic_cultivator", "branch_b", 1, 425, 75, 30, 1, List.of("black_fire_wave"), StatType.MAX_MANA, 50.0D, "✦ Batería Nano I", "Expande los suministros energéticos de nanomáquinas.", "🔮"));
        register(new SkillNode("nano_scan_weakness", "demonic_cultivator", "branch_b", 2, 400, 135, 35, 1, List.of("dc_mana_1"), NodeType.MAJOR_ACTIVE, "🔍"));
        register(new SkillNode("dc_spd_1", "demonic_cultivator", "branch_b", 2, 450, 135, 35, 1, List.of("dc_mana_1"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Aceleración Mioeléctrica", "Impulso neural que acelera la carrera.", "⚡"));
        register(new SkillNode("nano_cellular_regeneration", "demonic_cultivator", "branch_b", 3, 425, 195, 45, 1, List.of("nano_scan_weakness", "dc_spd_1"), NodeType.MAJOR_PASSIVE, "💉"));
        register(new SkillNode("dc_armor_1", "demonic_cultivator", "branch_b", 4, 400, 255, 55, 1, List.of("nano_cellular_regeneration"), StatType.ARMOR, 3.5D, "✦ Blindaje de Nanotubos", "Endurece la piel con capas microscópicas de carbono.", "🛡"));
        register(new SkillNode("nano_overdrive_suit", "demonic_cultivator", "branch_b", 4, 450, 255, 55, 1, List.of("nano_cellular_regeneration"), NodeType.MAJOR_ACTIVE, "🤖"));
        register(new SkillNode("dc_apex_2", "demonic_cultivator", "branch_b", 5, 425, 320, 65, 1, List.of("dc_armor_1", "nano_overdrive_suit"), StatType.ATTACK_SPEED, 0.08D, "✦ Sobrecarga Neuronal Nano", "Poder final: +8% Velocidad de Ataque y procesamiento instantáneo.", "★"));

        // Rama C: Cultivo del Dantian y Linaje Asura
        register(new SkillNode("dc_hp_1", "demonic_cultivator", "branch_c", 1, 85, 75, 30, 1, List.of("black_fire_wave"), StatType.MAX_HEALTH, 6.0D, "✦ Dantian Infernal I", "Condensa vitalidad marcial oscura.", "❤"));
        register(new SkillNode("dc_nano_blade_overdrive", "demonic_cultivator", "branch_c", 2, 60, 135, 35, 1, List.of("dc_hp_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("dc_mana_2", "demonic_cultivator", "branch_c", 2, 110, 135, 35, 1, List.of("dc_hp_1"), StatType.MAX_MANA, 60.0D, "✦ Circulación de Qi Inverso", "Mayor fluidez de Qi en los meridianos mayores.", "🔮"));
        register(new SkillNode("asura_bloodline", "demonic_cultivator", "branch_c", 3, 85, 195, 45, 1, List.of("dc_nano_blade_overdrive", "dc_mana_2"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("dc_hp_2", "demonic_cultivator", "branch_c", 4, 60, 255, 55, 1, List.of("asura_bloodline"), StatType.MAX_HEALTH, 8.0D, "✦ Cuerpo del Asura Inmortal", "Salud máxima multiplicada por el cultivo demoníaco.", "❤"));
        register(new SkillNode("dc_kb_1", "demonic_cultivator", "branch_c", 4, 110, 255, 55, 1, List.of("asura_bloodline"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Firmeza del Soberano Oscuro", "Resistencia inamovible al impacto enemigo.", "🛡"));
        register(new SkillNode("dc_apex_3", "demonic_cultivator", "branch_c", 5, 85, 320, 65, 1, List.of("dc_hp_2", "dc_kb_1"), StatType.MAX_HEALTH, 12.0D, "✦ Despertar del Gran Demonio", "Poder final: +12.0 Salud Máxima absoluta de emperador marcial.", "★"));

        // =========================================================================
        // 6. ASESINO DEL VACÍO (Solo Max-Level Newbie)
        // =========================================================================
        register(new SkillNode("nano_blade_overdrive", "void_assassin", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🗡"));
        // Rama A: Sombra y Ejecución
        register(new SkillNode("va_spd_1", "void_assassin", "branch_a", 1, 255, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.MOVEMENT_SPEED, 0.03D, "✦ Agilidad Fantasmal I", "Velocidad de movimiento superior.", "⚡"));
        register(new SkillNode("shadow_step_blink", "void_assassin", "branch_a", 2, 230, 135, 35, 1, List.of("va_spd_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("va_atk_1", "void_assassin", "branch_a", 2, 280, 135, 35, 1, List.of("va_spd_1"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Filo Letal", "Daño directo en puntos vitales.", "⚔"));
        register(new SkillNode("vital_strike", "void_assassin", "branch_a", 3, 255, 195, 45, 1, List.of("shadow_step_blink", "va_atk_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("va_crit_1", "void_assassin", "branch_a", 4, 230, 255, 55, 1, List.of("vital_strike"), StatType.ATTACK_DAMAGE, 3.5D, "✦ Daga Envenenada", "Incrementa la letalidad en emboscadas.", "🗡"));
        register(new SkillNode("infinite_mirror_blades", "void_assassin", "branch_a", 4, 280, 255, 55, 1, List.of("vital_strike"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("va_apex_1", "void_assassin", "branch_a", 5, 255, 320, 65, 1, List.of("va_crit_1", "infinite_mirror_blades"), StatType.ATTACK_DAMAGE, 5.0D, "✦ Ejecución Absoluta del Vacío", "Poder final: +5.0 Daño de Ataque permanente.", "★"));

        // Rama B: Tácticas del Jugador Veterano
        register(new SkillNode("va_atrspd_1", "void_assassin", "branch_b", 1, 425, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.ATTACK_SPEED, 0.05D, "✦ Reflejos de Veterano I", "Cadencia de ataque ultra rápida.", "⚔"));
        register(new SkillNode("predator_sense", "void_assassin", "branch_b", 2, 400, 135, 35, 1, List.of("va_atrspd_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("va_mana_1", "void_assassin", "branch_b", 2, 450, 135, 35, 1, List.of("va_atrspd_1"), StatType.MAX_MANA, 40.0D, "✦ Estamina del Rango S", "Maná para habilidades de sigilo y escape.", "🔮"));
        register(new SkillNode("smoke_bomb_escape", "void_assassin", "branch_b", 3, 425, 195, 45, 1, List.of("predator_sense", "va_mana_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("va_atrspd_2", "void_assassin", "branch_b", 4, 400, 255, 55, 1, List.of("smoke_bomb_escape"), StatType.ATTACK_SPEED, 0.07D, "✦ Reflejos Hipersónicos", "Acelera los combos de dagas a máxima velocidad.", "⚔"));
        register(new SkillNode("max_level_privilege", "void_assassin", "branch_b", 4, 450, 255, 55, 1, List.of("smoke_bomb_escape"), NodeType.MAJOR_PASSIVE, "✦"));
        register(new SkillNode("va_phantom_ambush", "void_assassin", "branch_b", 5, 425, 320, 65, 1, List.of("va_atrspd_2", "max_level_privilege"), NodeType.MAJOR_PASSIVE, "★"));

        // Rama C: Supervivencia en las Sombras
        register(new SkillNode("va_hp_1", "void_assassin", "branch_c", 1, 85, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.MAX_HEALTH, 5.0D, "✦ Resistencia de las Sombras", "Vitalidad para aguantar duelos prolongados.", "❤"));
        register(new SkillNode("va_stealth_stalker", "void_assassin", "branch_c", 2, 60, 135, 35, 1, List.of("va_hp_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("va_spd_2", "void_assassin", "branch_c", 2, 110, 135, 35, 1, List.of("va_hp_1"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Desplazamiento Silencioso", "Pisar ligero sin alertar a los enemigos.", "⚡"));
        register(new SkillNode("va_death_mark", "void_assassin", "branch_c", 3, 85, 195, 45, 1, List.of("va_stealth_stalker", "va_spd_2"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("va_armor_1", "void_assassin", "branch_c", 4, 60, 255, 55, 1, List.of("va_death_mark"), StatType.ARMOR, 3.0D, "✦ Manto del Vacío", "Protección contra daños furtivos.", "🛡"));
        register(new SkillNode("va_hp_2", "void_assassin", "branch_c", 4, 110, 255, 55, 1, List.of("va_death_mark"), StatType.MAX_HEALTH, 7.0D, "✦ Corazón del Fantasma", "Aumento de vitalidad de evasión.", "❤"));
        register(new SkillNode("va_apex_3", "void_assassin", "branch_c", 5, 85, 320, 65, 1, List.of("va_armor_1", "va_hp_2"), StatType.MOVEMENT_SPEED, 0.04D, "✦ Señor del Vacío Trascendente", "Poder final: +4% Velocidad de Movimiento y paso incorpóreo.", "★"));

        // =========================================================================
        // 7. GRAN ARCHIMAGO (The Archmage Returns After 4000 Years)
        // =========================================================================
        register(new SkillNode("isekai_mana_burst", "grand_archmage", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🔮"));
        // Rama A: Magia de 9º Círculo
        register(new SkillNode("ga_mana_1", "grand_archmage", "branch_a", 1, 255, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MAX_MANA, 60.0D, "✦ Círculo Arcano I", "Expande la reserva de maná elemental.", "🔮"));
        register(new SkillNode("elemental_spear", "grand_archmage", "branch_a", 2, 230, 135, 35, 1, List.of("ga_mana_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("ga_atk_1", "grand_archmage", "branch_a", 2, 280, 135, 35, 1, List.of("ga_mana_1"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Potencia Mágica", "Aumenta la fuerza de tus proyectiles arcanos.", "⚔"));
        register(new SkillNode("gravity_crush", "grand_archmage", "branch_a", 3, 255, 195, 45, 1, List.of("elemental_spear", "ga_atk_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("ga_mana_2", "grand_archmage", "branch_a", 4, 230, 255, 55, 1, List.of("gravity_crush"), StatType.MAX_MANA, 80.0D, "✦ Círculo Arcano II", "Maná descomunal para conjuros supremos.", "🔮"));
        register(new SkillNode("supernova_incineration", "grand_archmage", "branch_a", 4, 280, 255, 55, 1, List.of("gravity_crush"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("ga_apex_1", "grand_archmage", "branch_a", 5, 255, 320, 65, 1, List.of("ga_mana_2", "supernova_incineration"), StatType.MAX_MANA, 120.0D, "✦ Génesis del 10º Círculo", "Poder final: +120 Maná Máximo de deidad mágica.", "★"));

        // Rama B: Manipulación Arcana y Trascendencia
        register(new SkillNode("ga_spd_1", "grand_archmage", "branch_b", 1, 425, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Paso Etéreo I", "Desplazamiento flotante sobre corrientes de maná.", "⚡"));
        register(new SkillNode("mana_shield_barrier", "grand_archmage", "branch_b", 2, 400, 135, 35, 1, List.of("ga_spd_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("ga_armor_1", "grand_archmage", "branch_b", 2, 450, 135, 35, 1, List.of("ga_spd_1"), StatType.ARMOR, 3.0D, "✦ Manto de Runas", "Armadura que dispersa impactos físicos.", "🛡"));
        register(new SkillNode("arcane_overload", "grand_archmage", "branch_b", 3, 425, 195, 45, 1, List.of("mana_shield_barrier", "ga_armor_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("ga_atrspd_1", "grand_archmage", "branch_b", 4, 400, 255, 55, 1, List.of("arcane_overload"), StatType.ATTACK_SPEED, 0.05D, "✦ Celeridad de Hechizo", "Acelera la velocidad de lanzamiento de conjuros.", "⚔"));
        register(new SkillNode("time_dilation", "grand_archmage", "branch_b", 4, 450, 255, 55, 1, List.of("arcane_overload"), NodeType.MAJOR_ACTIVE, "⏳"));
        register(new SkillNode("ga_apex_2", "grand_archmage", "branch_b", 5, 425, 320, 65, 1, List.of("ga_atrspd_1", "time_dilation"), StatType.ATTACK_DAMAGE, 5.0D, "✦ Singularidad Espacio-Temporal", "Poder final: +5.0 de Daño Arcano en todos los ataques.", "★"));

        // Rama C: Fisiología de Maná Puro
        register(new SkillNode("ga_hp_1", "grand_archmage", "branch_c", 1, 85, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MAX_HEALTH, 5.0D, "✦ Regeneración Celular de Maná", "Salud fortalecida por la esencia mágica.", "❤"));
        register(new SkillNode("ga_mana_3", "grand_archmage", "branch_c", 2, 60, 135, 35, 1, List.of("ga_hp_1"), StatType.MAX_MANA, 70.0D, "✦ Dantian de Maná Puro", "Depósito inagotable de energía arcana.", "🔮"));
        register(new SkillNode("ga_armor_2", "grand_archmage", "branch_c", 2, 110, 135, 35, 1, List.of("ga_hp_1"), StatType.ARMOR, 3.0D, "✦ Escudo Prismático", "Defensa mágica impenetrable.", "🛡"));
        register(new SkillNode("ga_pain_suppression", "grand_archmage", "branch_c", 3, 85, 195, 45, 1, List.of("ga_mana_3", "ga_armor_2"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("ga_hp_2", "grand_archmage", "branch_c", 4, 60, 255, 55, 1, List.of("ga_pain_suppression"), StatType.MAX_HEALTH, 8.0D, "✦ Cuerpo del Sabio Ancestral", "Salud inquebrantable tras 4000 años de trascendencia.", "❤"));
        register(new SkillNode("ga_kb_1", "grand_archmage", "branch_c", 4, 110, 255, 55, 1, List.of("ga_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Gravedad Inmutable", "Resistencia al empuje de ondas de choque.", "🛡"));
        register(new SkillNode("ga_apex_3", "grand_archmage", "branch_c", 5, 85, 320, 65, 1, List.of("ga_hp_2", "ga_kb_1"), StatType.MAX_HEALTH, 12.0D, "✦ Inmortalidad del Archimago", "Poder final: Otorga +12.0 de Salud Máxima suprema.", "★"));

        // =========================================================================
        // 8. SOBERANO DRAGÓN (Solo Leveling / Dragon Sovereign)
        // =========================================================================
        register(new SkillNode("isekai_dragon_roar", "dragon_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🐉"));
        // Rama A: Llamas de la Destrucción
        register(new SkillNode("dm_atk_1", "dragon_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.ATTACK_DAMAGE, 3.0D, "✦ Fuego del Caos I", "Alienta las llamas devastadoras de cada ataque.", "🔥"));
        register(new SkillNode("dragon_breath", "dragon_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("dm_atk_1"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("dm_mana_1", "dragon_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("dm_atk_1"), StatType.MAX_MANA, 50.0D, "✦ Corazón de Fuego", "Maná para mantener llamaradas sostenidas.", "🔮"));
        register(new SkillNode("meteor_fall", "dragon_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("dragon_breath", "dm_mana_1"), NodeType.MAJOR_ACTIVE, "☄"));
        register(new SkillNode("dm_atk_2", "dragon_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("meteor_fall"), StatType.ATTACK_DAMAGE, 4.0D, "✦ Aliento Calcinador", "Aumenta la fuerza explosiva de los impactos de dragón.", "🔥"));
        register(new SkillNode("breath_of_annihilation", "dragon_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("meteor_fall"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("dm_apex_1", "dragon_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("dm_atk_2", "breath_of_annihilation"), StatType.ATTACK_DAMAGE, 6.0D, "✦ Apocalipsis del Dragón Ancestral", "Poder final: +6.0 Daño de Ataque absoluto.", "★"));

        // Rama B: Fisiología del Soberano
        register(new SkillNode("dm_armor_1", "dragon_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.ARMOR, 3.5D, "✦ Escamas Carmesí I", "Coraza de escamas dracónicas que repelen filos.", "🛡"));
        register(new SkillNode("dragon_scales_hardening", "dragon_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("dm_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("dm_hp_1", "dragon_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("dm_armor_1"), StatType.MAX_HEALTH, 8.0D, "✦ Vitalidad de Dragón I", "Incrementa la salud con el poder de bestia mítica.", "❤"));
        register(new SkillNode("dragon_wings_flight", "dragon_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("dragon_scales_hardening", "dm_hp_1"), NodeType.MAJOR_ACTIVE, "🪽"));
        register(new SkillNode("dm_armor_2", "dragon_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("dragon_wings_flight"), StatType.ARMOR, 4.5D, "✦ Escamas Carmesí II", "Blindaje que anula cortes y proyectiles.", "🛡"));
        register(new SkillNode("dragon_form_avatar", "dragon_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("dragon_wings_flight"), NodeType.MAJOR_ACTIVE, "🐉"));
        register(new SkillNode("dm_apex_2", "dragon_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("dm_armor_2", "dragon_form_avatar"), StatType.MAX_HEALTH, 14.0D, "✦ Majestad del Soberano Dragón", "Poder final: +14.0 Salud Máxima colosal.", "★"));

        // Rama C: Furia Dracónica Ancestral
        register(new SkillNode("dm_kb_1", "dragon_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.KNOCKBACK_RESISTANCE, 0.25D, "✦ Imparable como Dragón", "Masa colosal inmune a empujes y aturdimientos.", "🛡"));
        register(new SkillNode("dm_dragon_dive", "dragon_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("dm_kb_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("dm_spd_1", "dragon_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("dm_kb_1"), StatType.MOVEMENT_SPEED, 0.02D, "✦ Zancada del Coloso", "Mayor velocidad de persecución de presas.", "⚡"));
        register(new SkillNode("dm_bloodthirst", "dragon_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("dm_dragon_dive", "dm_spd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("dm_hp_2", "dragon_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("dm_bloodthirst"), StatType.MAX_HEALTH, 10.0D, "✦ Sangre de Dragón Supremo", "Vitalidad que desafía el daño de asalto.", "❤"));
        register(new SkillNode("dm_atrspd_1", "dragon_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("dm_bloodthirst"), StatType.ATTACK_SPEED, 0.05D, "✦ Garras Rápidas de Dragón", "Velocidad de zarpazos implacables.", "⚔"));
        register(new SkillNode("dm_apex_3", "dragon_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("dm_hp_2", "dm_atrspd_1"), StatType.KNOCKBACK_RESISTANCE, 0.35D, "✦ Dominio del Soberano de la Destrucción", "Poder final: +0.35 Resistencia al Empuje absoluta.", "★"));

        // =========================================================================
        // 9. SANADOR CELESTIAL (The S-Classes That I Raised)
        // =========================================================================
        register(new SkillNode("sh_caregiver_blessing", "spiritual_healer", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "✨"));
        // Rama A: Milagros Sagrados y Sanación (Curación y Vida)
        register(new SkillNode("sh_mana_1", "spiritual_healer", "branch_a", 1, 255, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.MAX_MANA, 50.0D, "✦ Maná Santo I", "Canaliza maná de alta pureza para artes curativas.", "🔮"));
        register(new SkillNode("sh_holy_touch", "spiritual_healer", "branch_a", 2, 230, 135, 35, 1, List.of("sh_mana_1"), NodeType.MAJOR_ACTIVE, "💚"));
        register(new SkillNode("sh_hp_1", "spiritual_healer", "branch_a", 2, 280, 135, 35, 1, List.of("sh_mana_1"), StatType.MAX_HEALTH, 6.0D, "✦ Gracia Vital I", "Fortalece la longevidad y el cuerpo del sanador.", "❤"));
        register(new SkillNode("sh_divine_grace", "spiritual_healer", "branch_a", 3, 255, 195, 45, 1, List.of("sh_holy_touch", "sh_hp_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sh_res_1", "spiritual_healer", "branch_a", 4, 230, 255, 55, 1, List.of("sh_divine_grace"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "✦ Temple Sagrado", "Firmeza espiritual que impide ser derribado durante rezos.", "🛡"));
        register(new SkillNode("sh_martyr_soul", "spiritual_healer", "branch_a", 4, 280, 255, 55, 1, List.of("sh_divine_grace"), NodeType.MAJOR_PASSIVE, "👼"));
        register(new SkillNode("sh_apex_miracle", "spiritual_healer", "branch_a", 5, 255, 320, 65, 1, List.of("sh_res_1", "sh_martyr_soul"), StatType.MAX_HEALTH, 14.0D, "✦ Milagro Supremo de la Creación", "Poder final: +14.0 Salud Máxima absoluta de salvador.", "★"));

        // Rama B: Barreras y Purificación (Protección y Fuego Santo)
        register(new SkillNode("sh_armor_1", "spiritual_healer", "branch_b", 1, 425, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.ARMOR, 3.0D, "✦ Manto Sagrado I", "Vestidura bendita que amortigua golpes físicos.", "🛡"));
        register(new SkillNode("sh_sacred_sanctuary", "spiritual_healer", "branch_b", 2, 400, 135, 35, 1, List.of("sh_armor_1"), NodeType.MAJOR_ACTIVE, "⛪"));
        register(new SkillNode("sh_mana_2", "spiritual_healer", "branch_b", 2, 450, 135, 35, 1, List.of("sh_armor_1"), StatType.MAX_MANA, 60.0D, "✦ Maná Puro II", "Maná extendido para sostener barreras continuas.", "🔮"));
        register(new SkillNode("sh_cleansing_radiance", "spiritual_healer", "branch_b", 3, 425, 195, 45, 1, List.of("sh_sacred_sanctuary", "sh_mana_2"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("sh_armor_2", "spiritual_healer", "branch_b", 4, 400, 255, 55, 1, List.of("sh_cleansing_radiance"), StatType.ARMOR, 4.0D, "✦ Manto Sagrado II", "Blindaje de luz refractaria contra proyectiles.", "🛡"));
        register(new SkillNode("sh_aegis_immortal", "spiritual_healer", "branch_b", 4, 450, 255, 55, 1, List.of("sh_cleansing_radiance"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("sh_apex_sanctuary", "spiritual_healer", "branch_b", 5, 425, 320, 65, 1, List.of("sh_armor_2", "sh_aegis_immortal"), StatType.ARMOR, 6.0D, "✦ Bastión Inviolable de Luz", "Poder final: +6.0 Armadura permanente de santuario viviente.", "★"));

        // Rama C: Lazos del Cuidador y Empoderamiento (Buffs a Aliados y Bestias)
        register(new SkillNode("sh_spd_1", "spiritual_healer", "branch_c", 1, 85, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.MOVEMENT_SPEED, 0.025D, "✦ Pasos de Ángel I", "Desplazamiento veloz para auxiliar a compañeros caídos.", "⚡"));
        register(new SkillNode("sh_tamer_bond", "spiritual_healer", "branch_c", 2, 60, 135, 35, 1, List.of("sh_spd_1"), NodeType.MAJOR_ACTIVE, "🐾"));
        register(new SkillNode("sh_hp_2", "spiritual_healer", "branch_c", 2, 110, 135, 35, 1, List.of("sh_spd_1"), StatType.MAX_HEALTH, 8.0D, "✦ Vínculo Espiritual", "Salud fortalecida por la devoción a tus compañeros.", "❤"));
        register(new SkillNode("sh_awakening_inspire", "spiritual_healer", "branch_c", 3, 85, 195, 45, 1, List.of("sh_tamer_bond", "sh_hp_2"), NodeType.MAJOR_ACTIVE, "🌟"));
        register(new SkillNode("sh_atrspd_1", "spiritual_healer", "branch_c", 4, 60, 255, 55, 1, List.of("sh_awakening_inspire"), StatType.ATTACK_SPEED, 0.05D, "✦ Fervor Sagrado", "Acelera el ritmo de bendiciones y ataques de soporte.", "⚔"));
        register(new SkillNode("sh_beast_empathy", "spiritual_healer", "branch_c", 4, 110, 255, 55, 1, List.of("sh_awakening_inspire"), NodeType.MAJOR_PASSIVE, "🕊"));
        register(new SkillNode("sh_apex_caregiver", "spiritual_healer", "branch_c", 5, 85, 320, 65, 1, List.of("sh_atrspd_1", "sh_beast_empathy"), StatType.MAX_MANA, 100.0D, "✦ Cuidador de Rango S Trascendente", "Poder final: +100 Maná Máximo para sanación inagotable.", "★"));

        // =========================================================================
        // 10. EMPERADOR DEL RELÁMPAGO (Lightning Degree)
        // =========================================================================
        register(new SkillNode("le_electrification", "lightning_emperor", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "⚡"));
        // Rama A: Rayo Destructor (Ataque y Plasma)
        register(new SkillNode("le_atk_1", "lightning_emperor", "branch_a", 1, 255, 75, 30, 1, List.of("le_electrification"), StatType.ATTACK_DAMAGE, 2.5D, "✦ Qi del Relámpago I", "Imbuye voltios celestiales en la punta de tus ataques.", "⚔"));
        register(new SkillNode("le_thunder_spear", "lightning_emperor", "branch_a", 2, 230, 135, 35, 1, List.of("le_atk_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("le_mana_1", "lightning_emperor", "branch_a", 2, 280, 135, 35, 1, List.of("le_atk_1"), StatType.MAX_MANA, 50.0D, "✦ Dantian Eléctrico I", "Maná condensado para sostener tempestades de plasma.", "🔮"));
        register(new SkillNode("le_plasma_burst", "lightning_emperor", "branch_a", 3, 255, 195, 45, 1, List.of("le_thunder_spear", "le_mana_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("le_atk_2", "lightning_emperor", "branch_a", 4, 230, 255, 55, 1, List.of("le_plasma_burst"), StatType.ATTACK_DAMAGE, 4.0D, "✦ Qi del Relámpago II", "Voltaje destructor capaz de partir montañas.", "⚔"));
        register(new SkillNode("le_wrath_of_thunder_god", "lightning_emperor", "branch_a", 4, 280, 255, 55, 1, List.of("le_plasma_burst"), NodeType.MAJOR_ACTIVE, "🌩"));
        register(new SkillNode("le_apex_thunder", "lightning_emperor", "branch_a", 5, 255, 320, 65, 1, List.of("le_atk_2", "le_wrath_of_thunder_god"), StatType.ATTACK_DAMAGE, 6.0D, "✦ Soberano Celestial del Relámpago", "Poder final: +6.0 Daño de Ataque permanente.", "★"));

        // Rama B: Paso Centella y Celeridad (Velocidad y Reflejos)
        register(new SkillNode("le_spd_1", "lightning_emperor", "branch_b", 1, 425, 75, 30, 1, List.of("le_electrification"), StatType.MOVEMENT_SPEED, 0.03D, "✦ Paso Centella I", "Desplazamiento fulgurante a la velocidad de la luz.", "⚡"));
        register(new SkillNode("le_flash_step", "lightning_emperor", "branch_b", 2, 400, 135, 35, 1, List.of("le_spd_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("le_atrspd_1", "lightning_emperor", "branch_b", 2, 450, 135, 35, 1, List.of("le_spd_1"), StatType.ATTACK_SPEED, 0.05D, "✦ Reflejos Eléctricos I", "Acelera tus ataques con impulsos bioeléctricos.", "⚔"));
        register(new SkillNode("le_overdrive_speed", "lightning_emperor", "branch_b", 3, 425, 195, 45, 1, List.of("le_flash_step", "le_atrspd_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("le_spd_2", "lightning_emperor", "branch_b", 4, 400, 255, 55, 1, List.of("le_overdrive_speed"), StatType.MOVEMENT_SPEED, 0.04D, "✦ Paso Centella II", "Velocidad sónica que distorsiona el aire.", "⚡"));
        register(new SkillNode("le_static_discharge", "lightning_emperor", "branch_b", 4, 450, 255, 55, 1, List.of("le_overdrive_speed"), NodeType.MAJOR_PASSIVE, "⚡"));
        register(new SkillNode("le_apex_speed", "lightning_emperor", "branch_b", 5, 425, 320, 65, 1, List.of("le_spd_2", "le_static_discharge"), StatType.ATTACK_SPEED, 0.08D, "✦ Dios del Viento y el Trueno", "Poder final: +8% Velocidad de Ataque instantánea.", "★"));

        // Rama C: Cuerpo Vajra del Rayo (Defensa y Resistencia)
        register(new SkillNode("le_hp_1", "lightning_emperor", "branch_c", 1, 85, 75, 30, 1, List.of("le_electrification"), StatType.MAX_HEALTH, 6.0D, "✦ Constitución Eléctrica I", "Músculos endurecidos por descargas de alta tensión.", "❤"));
        register(new SkillNode("le_lightning_armor", "lightning_emperor", "branch_c", 2, 60, 135, 35, 1, List.of("le_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("le_armor_1", "lightning_emperor", "branch_c", 2, 110, 135, 35, 1, List.of("le_hp_1"), StatType.ARMOR, 3.0D, "✦ Escudo de Plasma", "Campo magnético que desvía cortes e impactos.", "🛡"));
        register(new SkillNode("le_grounding_soul", "lightning_emperor", "branch_c", 3, 85, 195, 45, 1, List.of("le_lightning_armor", "le_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("le_hp_2", "lightning_emperor", "branch_c", 4, 60, 255, 55, 1, List.of("le_grounding_soul"), StatType.MAX_HEALTH, 10.0D, "✦ Constitución Eléctrica II", "Vitalidad que resiste fulguraciones letales.", "❤"));
        register(new SkillNode("le_vajra_indestructible", "lightning_emperor", "branch_c", 4, 110, 255, 55, 1, List.of("le_grounding_soul"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("le_apex_vajra", "lightning_emperor", "branch_c", 5, 85, 320, 65, 1, List.of("le_hp_2", "le_vajra_indestructible"), StatType.MAX_HEALTH, 14.0D, "✦ Cuerpo Vajra Inmortal", "Poder final: +14.0 Salud Máxima inquebrantable.", "★"));

        // =========================================================================
        // 11. EMPERADOR DE SANGRE (Doom Breaker)
        // =========================================================================
        register(new SkillNode("be_bloodthirst", "blood_emperor", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🩸"));
        // Rama A: Hemomancia Carmesí (Daño y Drenaje)
        register(new SkillNode("be_atk_1", "blood_emperor", "branch_a", 1, 255, 75, 30, 1, List.of("be_bloodthirst"), StatType.ATTACK_DAMAGE, 3.0D, "✦ Filo de Sangre I", "Condensa sangre letal en el filo de tu arma.", "⚔"));
        register(new SkillNode("be_blood_lance", "blood_emperor", "branch_a", 2, 230, 135, 35, 1, List.of("be_atk_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("be_mana_1", "blood_emperor", "branch_a", 2, 280, 135, 35, 1, List.of("be_atk_1"), StatType.MAX_MANA, 40.0D, "✦ Reserva Carmesí", "Energía vital para manifestar picos de hemoglobina.", "🔮"));
        register(new SkillNode("be_crimson_eruption", "blood_emperor", "branch_a", 3, 255, 195, 45, 1, List.of("be_blood_lance", "be_mana_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("be_atk_2", "blood_emperor", "branch_a", 4, 230, 255, 55, 1, List.of("be_crimson_eruption"), StatType.ATTACK_DAMAGE, 4.5D, "✦ Filo de Sangre II", "Cortes que devoran la vitalidad enemiga.", "⚔"));
        register(new SkillNode("be_blood_apocalypse", "blood_emperor", "branch_a", 4, 280, 255, 55, 1, List.of("be_crimson_eruption"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("be_apex_damage", "blood_emperor", "branch_a", 5, 255, 320, 65, 1, List.of("be_atk_2", "be_blood_apocalypse"), StatType.ATTACK_DAMAGE, 6.0D, "✦ Soberano Carmesí Supremo", "Poder final: +6.0 Daño de Ataque absoluto.", "★"));

        // Rama B: Frenesí de Asalto y Robo de Vida (Velocidad y Sed)
        register(new SkillNode("be_atrspd_1", "blood_emperor", "branch_b", 1, 425, 75, 30, 1, List.of("be_bloodthirst"), StatType.ATTACK_SPEED, 0.04D, "✦ Tajo Frenético I", "Acelera tus tajos con sed desenfrenada.", "⚔"));
        register(new SkillNode("be_crimson_rush", "blood_emperor", "branch_b", 2, 400, 135, 35, 1, List.of("be_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("be_spd_1", "blood_emperor", "branch_b", 2, 450, 135, 35, 1, List.of("be_atrspd_1"), StatType.MOVEMENT_SPEED, 0.025D, "✦ Pasos Carmesí", "Velocidad de persecución sobre la sangre de tus presas.", "⚡"));
        register(new SkillNode("be_vampiric_awakening", "blood_emperor", "branch_b", 3, 425, 195, 45, 1, List.of("be_crimson_rush", "be_spd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("be_atrspd_2", "blood_emperor", "branch_b", 4, 400, 255, 55, 1, List.of("be_vampiric_awakening"), StatType.ATTACK_SPEED, 0.06D, "✦ Tajo Frenético II", "Cadencia de golpes que despedazan carne y hueso.", "⚔"));
        register(new SkillNode("be_berserk_blood", "blood_emperor", "branch_b", 4, 450, 255, 55, 1, List.of("be_vampiric_awakening"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("be_apex_frenzy", "blood_emperor", "branch_b", 5, 425, 320, 65, 1, List.of("be_atrspd_2", "be_berserk_blood"), StatType.ATTACK_SPEED, 0.08D, "✦ Tirano del Frenesí Sanguinario", "Poder final: +8% Velocidad de Ataque imparable.", "★"));

        // Rama C: Inmortalidad Dracónica y Resistencia (Defensa y Tanque)
        register(new SkillNode("be_hp_1", "blood_emperor", "branch_c", 1, 85, 75, 30, 1, List.of("be_bloodthirst"), StatType.MAX_HEALTH, 8.0D, "✦ Vitalidad Carmesí I", "Corazón imperecedero forjado en sangre dracónica.", "❤"));
        register(new SkillNode("be_blood_barrier", "blood_emperor", "branch_c", 2, 60, 135, 35, 1, List.of("be_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("be_armor_1", "blood_emperor", "branch_c", 2, 110, 135, 35, 1, List.of("be_hp_1"), StatType.ARMOR, 3.5D, "✦ Escamas de Sangre I", "Coraza de sangre cristalizada que detiene filos.", "🛡"));
        register(new SkillNode("be_undying_vitality", "blood_emperor", "branch_c", 3, 85, 195, 45, 1, List.of("be_blood_barrier", "be_armor_1"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("be_hp_2", "blood_emperor", "branch_c", 4, 60, 255, 55, 1, List.of("be_undying_vitality"), StatType.MAX_HEALTH, 12.0D, "✦ Vitalidad Carmesí II", "Salud colosal capaz de desafiar la muerte misma.", "❤"));
        register(new SkillNode("be_dragon_blood_avatar", "blood_emperor", "branch_c", 4, 110, 255, 55, 1, List.of("be_undying_vitality"), NodeType.MAJOR_ACTIVE, "🐉"));
        register(new SkillNode("be_apex_immortal", "blood_emperor", "branch_c", 5, 85, 320, 65, 1, List.of("be_hp_2", "be_dragon_blood_avatar"), StatType.MAX_HEALTH, 16.0D, "✦ Inmortalidad Dracónica", "Poder final: +16.0 Salud Máxima de semidiós.", "★"));

        // =========================================================================
        // 12. MONARCA DE LAS BESTIAS (Solo Leveling / White Tiger)
        // =========================================================================
        register(new SkillNode("bm_seismic_roar", "beast_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🐯"));
        // Rama A: Garras Salvajes y Desgarre (Ataque Letal)
        register(new SkillNode("bm_atk_1", "beast_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("bm_seismic_roar"), StatType.ATTACK_DAMAGE, 3.0D, "✦ Garras de Acero I", "Afila tus zarpazos con instinto depredador.", "⚔"));
        register(new SkillNode("bm_predator_leap", "beast_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("bm_atk_1"), NodeType.MAJOR_ACTIVE, "🐾"));
        register(new SkillNode("bm_atrspd_1", "beast_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("bm_atk_1"), StatType.ATTACK_SPEED, 0.04D, "✦ Reflejos Bestiales I", "Acelera la cadencia salvaje de desgarre.", "⚔"));
        register(new SkillNode("bm_feral_rend", "beast_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("bm_predator_leap", "bm_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("bm_atk_2", "beast_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("bm_feral_rend"), StatType.ATTACK_DAMAGE, 4.5D, "✦ Garras de Acero II", "Poder destructivo capaz de atravesar blindajes.", "⚔"));
        register(new SkillNode("bm_white_tiger_transformation", "beast_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("bm_feral_rend"), NodeType.MAJOR_ACTIVE, "🐯"));
        register(new SkillNode("bm_apex_claws", "beast_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("bm_atk_2", "bm_white_tiger_transformation"), StatType.ATTACK_DAMAGE, 6.0D, "✦ Señor Feroz del Bosque Blanco", "Poder final: +6.0 Daño de Ataque absoluto.", "★"));

        // Rama B: Instinto Alfa y Caza (Movilidad y Rastreo)
        register(new SkillNode("bm_spd_1", "beast_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("bm_seismic_roar"), StatType.MOVEMENT_SPEED, 0.03D, "✦ Zancada del Tigre I", "Acelera la persecución implacable de presas.", "⚡"));
        register(new SkillNode("bm_scent_tracking", "beast_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("bm_spd_1"), NodeType.MAJOR_ACTIVE, "👃"));
        register(new SkillNode("bm_atrspd_2", "beast_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("bm_spd_1"), StatType.ATTACK_SPEED, 0.05D, "✦ Reflejos Bestiales II", "Velocidad de reacción instintiva ante emboscadas.", "⚔"));
        register(new SkillNode("bm_pack_frenzy", "beast_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("bm_scent_tracking", "bm_atrspd_2"), NodeType.MAJOR_ACTIVE, "🐺"));
        register(new SkillNode("bm_spd_2", "beast_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("bm_pack_frenzy"), StatType.MOVEMENT_SPEED, 0.04D, "✦ Zancada del Tigre II", "Velocidad felina insuperable en campo abierto.", "⚡"));
        register(new SkillNode("bm_alpha_intimidation", "beast_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("bm_pack_frenzy"), NodeType.MAJOR_PASSIVE, "👑"));
        register(new SkillNode("bm_apex_instinct", "beast_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("bm_spd_2", "bm_alpha_intimidation"), StatType.ATTACK_SPEED, 0.08D, "✦ Depredador Alfa Supremo", "Poder final: +8% Velocidad de Ataque y reflejos felinos.", "★"));

        // Rama C: Pellejo de Coloso y Resistencia Animal (Defensa y Masa)
        register(new SkillNode("bm_hp_1", "beast_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("bm_seismic_roar"), StatType.MAX_HEALTH, 8.0D, "✦ Vigor Animal I", "Masa muscular descomunal de depredador ápice.", "❤"));
        register(new SkillNode("bm_thick_fur", "beast_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("bm_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("bm_armor_1", "beast_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("bm_hp_1"), StatType.ARMOR, 3.5D, "✦ Pelaje Denso", "Manto peludo grueso que absorbe garras y colmillos.", "🛡"));
        register(new SkillNode("bm_wild_resilience", "beast_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("bm_thick_fur", "bm_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("bm_hp_2", "beast_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("bm_wild_resilience"), StatType.MAX_HEALTH, 12.0D, "✦ Vigor Animal II", "Salud colosal de monstruo mitológico.", "❤"));
        register(new SkillNode("bm_titan_roar", "beast_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("bm_wild_resilience"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("bm_apex_hide", "beast_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("bm_hp_2", "bm_titan_roar"), StatType.KNOCKBACK_RESISTANCE, 0.35D, "✦ Fortaleza de la Bestia Titánica", "Poder final: +0.35 Resistencia al Empuje inamovible.", "★"));
    }

    public static final int MAX_SKILL_LEVEL = 3;

    public static String normalizeClassId(String hunterClass) {
        if (hunterClass == null) return "";
        return switch (hunterClass.toLowerCase(Locale.ROOT)) {
            case "necromancer" -> "shadow_monarch";
            case "swordsman", "warrior" -> "sword_saint";
            case "mage" -> "grand_archmage";
            case "assassin" -> "void_assassin";
            case "berserker" -> "colossal_berserker";
            case "healer" -> "spiritual_healer";
            case "lightning" -> "lightning_emperor";
            case "blood" -> "blood_emperor";
            case "beast" -> "beast_monarch";
            case "dragon" -> "dragon_monarch";
            default -> hunterClass;
        };
    }

    public static boolean isSkillTreeNode(String skillId) {
        return NODES_BY_SKILL.containsKey(skillId);
    }

    public static List<SkillNode> getNodesForClass(String hunterClass) {
        return CLASS_TREES.getOrDefault(normalizeClassId(hunterClass), Collections.emptyList());
    }

    public static int getPointsInBranch(SystemData data, String hunterClass, String branch) {
        if (data == null || hunterClass == null || branch == null) return 0;
        List<SkillNode> nodes = getNodesForClass(hunterClass);
        int total = 0;
        for (SkillNode node : nodes) {
            if (branch.equalsIgnoreCase(node.branch())) {
                int lvl = data.skills().getOrDefault(node.skillId(), 0);
                total += lvl;
            }
        }
        return total;
    }

    public static SkillNode getNode(String hunterClass, String skillId) {
        if (hunterClass != null) {
            Map<String, SkillNode> map = NODES_BY_CLASS_AND_SKILL.get(normalizeClassId(hunterClass));
            if (map != null && map.containsKey(skillId)) {
                return map.get(skillId);
            }
        }
        return NODES_BY_SKILL.get(skillId);
    }

    public static SkillNode getNode(String skillId) {
        return NODES_BY_SKILL.get(skillId);
    }

    public static int getRequiredLevelForRank(SkillNode node, int targetRank) {
        if (targetRank <= 1) return node.requiredLevel();
        if (targetRank == 2) return node.requiredLevel() + 10;
        return node.requiredLevel() + 20;
    }

    public static int getSpCostForRank(SkillNode node, int targetRank) {
        if (targetRank <= 1) return node.spCost();
        return 1;
    }

    /**
     * Sistema de Niebla de Guerra (Fog of War):
     * Un nodo es visible solo si ya está desbloqueado, si es la raíz, o si alguno de sus prerrequisitos inmediatos ya está desbloqueado.
     * Si no, permanece completamente oculto ("no se deberían de ver").
     */
    public static boolean isRevealed(SystemData data, SkillNode node) {
        if (node == null) return false;
        if (node.isRoot()) return true;
        if (data.skills().containsKey(node.skillId())) return true; // Ya desbloqueado
        if (node.prerequisiteIds() == null || node.prerequisiteIds().isEmpty()) return true;
        for (String prereq : node.prerequisiteIds()) {
            if (data.skills().containsKey(prereq)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUnlock(SystemData data, String skillId) {
        SkillNode node = getNode(data != null ? data.hunterClass() : null, skillId);
        if (node == null) return false;
        if (!node.hunterClass().equals(normalizeClassId(data.hunterClass()))) return false;
        if (data.skills().containsKey(skillId)) return false; // ya desbloqueada
        if (!isRevealed(data, node)) return false; // oculta en la niebla
        if (data.level() < node.requiredLevel()) return false;
        if (data.skillPoints() < node.spCost()) return false;
        return true;
    }

    public static boolean canUpgrade(SystemData data, String skillId) {
        SkillNode node = getNode(data != null ? data.hunterClass() : null, skillId);
        if (node == null) return false;
        if (node.isAttribute()) return false; // Nodos de atributo son de 1 rango
        if (!node.hunterClass().equals(normalizeClassId(data.hunterClass()))) return false;
        int curLvl = data.skills().getOrDefault(skillId, 0);
        if (curLvl < 1 || curLvl >= MAX_SKILL_LEVEL) return false;
        int nextRank = curLvl + 1;
        if (data.level() < getRequiredLevelForRank(node, nextRank)) return false;
        if (data.skillPoints() < getSpCostForRank(node, nextRank)) return false;
        return true;
    }

    public static double getTotalAttributeBonus(SystemData data, StatType type) {
        if (data == null || !data.hasHunterClass()) return 0.0D;
        List<SkillNode> nodes = CLASS_TREES.get(normalizeClassId(data.hunterClass()));
        if (nodes == null) return 0.0D;
        double total = 0.0D;
        for (SkillNode node : nodes) {
            if (node.isAttribute() && node.statType() == type && data.skills().containsKey(node.skillId())) {
                total += node.statValue();
            }
        }
        return total;
    }

    public static String getRankStars(int level) {
        return switch (level) {
            case 1 -> "★☆☆";
            case 2 -> "★★☆";
            case 3 -> "★★★";
            default -> "☆☆☆";
        };
    }

    public static Component getRankTitle(int level) {
        return switch (level) {
            case 1 -> Component.translatable("awakening_system.skill_tree.rank_1");
            case 2 -> Component.translatable("awakening_system.skill_tree.rank_2");
            case 3 -> Component.translatable("awakening_system.skill_tree.rank_3");
            default -> Component.translatable("awakening_system.skill_tree.locked");
        };
    }

    public static Component getUpgradeBonusDescription(SkillNode node, int targetRank) {
        if (node.isActive()) {
            if (targetRank == 2) {
                return Component.translatable("awakening_system.skill_tree.upgrade.active_2");
            } else if (targetRank == 3) {
                return Component.translatable("awakening_system.skill_tree.upgrade.active_3");
            }
        } else {
            if (targetRank == 2) {
                return Component.translatable("awakening_system.skill_tree.upgrade.passive_2");
            } else if (targetRank == 3) {
                return Component.translatable("awakening_system.skill_tree.upgrade.passive_3");
            }
        }
        return Component.empty();
    }
}
