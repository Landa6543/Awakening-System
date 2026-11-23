package com.soybe.awakeningsystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * The class skill webs. Each class has one big web (in the style of a passive tree): a start node in the middle,
 * many small stat nodes, big stat nodes, and the class's active and passive skills as the notable nodes. A node can
 * be learnt when one of the nodes linked to it is already learnt.
 *
 * <p>The webs are data files, {@code data/<namespace>/class_trees/<class>.json}, loaded with the server's data
 * (so a data pack can change them without recompiling) and sent to each player on joining. Skills are kept in the
 * player's skills (they are equipped and levelled like any skill); stat nodes are kept apart, in the player's tree
 * nodes, so they never fill the skill lists.
 */
public final class ClassSkillTree {
    private ClassSkillTree() {}

    /** Raised when the webs change shape in a way that needs a free re-spend: players below it get their points back. */
    public static final int TREE_VERSION = 2;

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

    /**
     * One node. {@code prerequisiteIds} are all the nodes linked to it (links go both ways); {@code minor} marks the
     * small stat nodes (a big stat node is an attribute node that is not minor).
     */
    public record SkillNode(
            String skillId,
            String hunterClass,
            String branch,
            int tier,
            int gridX,
            int gridY,
            int requiredLevel,
            int spCost,
            List<String> prerequisiteIds,
            NodeType nodeType,
            StatType statType,
            double statValue,
            boolean minor,
            boolean activeSkill,
            String iconSymbol
    ) {
        public boolean isRoot() {
            return nodeType == NodeType.ROOT;
        }

        public boolean isAttribute() {
            return nodeType == NodeType.ATTRIBUTE_NODE;
        }

        /** An active skill: the active notables, and a start node marked "active" (the base classes start with one). */
        public boolean isActive() {
            return nodeType == NodeType.MAJOR_ACTIVE || activeSkill;
        }

        /** A node learnt once and kept in the tree nodes, not the skills: the stat nodes. */
        public boolean isStatNode() {
            return isAttribute();
        }

        public String prerequisiteSkillId() {
            return prerequisiteIds.isEmpty() ? null : prerequisiteIds.get(0);
        }

        public Component getName() {
            if (isAttribute() && minor) return statName(statType);
            String skillKey = "awakening_system.skill." + skillId + ".name";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".name";
            if (isAttribute()) {
                // a big stat node: the old ones have their own names, the added ones (<class>_n<number>) are "Greater <stat>"
                if (skillId.matches(".*_n\\d+$")) return Component.translatable("awakening_system.skill_tree.major_name", statName(statType));
                return Component.translatableWithFallback(nodeKey, skillId);
            }
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, skillId).getString());
        }

        public Component getDescription() {
            if (isAttribute()) {
                Component plain = Component.translatable(minor ? "awakening_system.skill_tree.minor_desc" : "awakening_system.skill_tree.major_desc");
                if (minor || skillId.matches(".*_n\\d+$")) return plain;
                return Component.translatableWithFallback("awakening_system.skill_tree.node." + skillId + ".desc", plain.getString());
            }
            String skillKey = "awakening_system.skill." + skillId + ".description";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".desc";
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, "").getString());
        }

        public Component getEffect() {
            if (isAttribute() && statType != StatType.NONE) return statLine(statType, statValue);
            String skillKey = "awakening_system.skill." + skillId + ".effect";
            return Component.translatableWithFallback(skillKey, "");
        }

        public SkillDefinitions.Rarity getRarity() {
            if (isRoot()) return SkillDefinitions.Rarity.UNIQUE;
            if (isAttribute()) return minor ? SkillDefinitions.Rarity.COMMON : SkillDefinitions.Rarity.UNCOMMON;
            if (nodeType == NodeType.MAJOR_ACTIVE || nodeType == NodeType.MAJOR_PASSIVE) return SkillDefinitions.Rarity.EPIC;
            return SkillDefinitions.Rarity.RARE;
        }
    }

    /** A branch name shown on the web at a point. */
    public record Branch(String id, String name, int x, int y) {}

    /** One class's web. */
    public record Tree(String hunterClass, String start, List<SkillNode> nodes, Map<String, SkillNode> byId, List<Branch> branches,
                       List<String[]> links, int minX, int minY, int maxX, int maxY) {}

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

    /** "+0.5 Attack Damage", "+2% Attack Speed" and so on. */
    public static Component statLine(StatType type, double value) {
        String v = (value > 0 ? "+" : "") + switch (type) {
            case MOVEMENT_SPEED -> String.format(Locale.US, "%.1f%%", value * 100);
            case ATTACK_SPEED -> String.format(Locale.US, "%.0f%%", value * 100);
            case KNOCKBACK_RESISTANCE -> String.format(Locale.US, "%.0f%%", value * 100);
            default -> String.format(Locale.US, value == Math.rint(value) ? "%.0f" : "%.1f", value);
        };
        return Component.literal(v + " ").append(statName(type));
    }

    // ---- the loaded webs (swapped whole, so a reload never shows half a web to another thread)

    private record Loaded(Map<String, Tree> trees, Map<String, SkillNode> byId, Map<String, String> json) {}

    private static volatile Loaded loaded = new Loaded(Map.of(), Map.of(), Map.of());

    /** Installs the webs from their JSON texts (class id → JSON), on the server after loading data or on the client from the server. */
    public static void install(Map<String, String> jsonByClass) {
        Map<String, Tree> trees = new LinkedHashMap<>();
        Map<String, SkillNode> byId = new LinkedHashMap<>();
        Map<String, String> json = new LinkedHashMap<>();
        for (var e : jsonByClass.entrySet()) {
            try {
                JsonObject o = com.google.gson.JsonParser.parseString(e.getValue()).getAsJsonObject();
                Tree t = parse(o);
                trees.put(t.hunterClass(), t);
                for (SkillNode n : t.nodes()) byId.putIfAbsent(n.skillId(), n);
                json.put(t.hunterClass(), e.getValue());
            } catch (RuntimeException ex) {
                AwakeningSystemMod.LOGGER.error("Class tree {} could not be read: {}", e.getKey(), ex.toString());
            }
        }
        loaded = new Loaded(Collections.unmodifiableMap(trees), Collections.unmodifiableMap(byId), Collections.unmodifiableMap(json));
        AwakeningSystemMod.LOGGER.info("Class trees: {} webs, {} nodes", trees.size(), byId.size());
    }

    /** The JSON of every loaded web, as sent to players. */
    public static Map<String, String> json() {
        return loaded.json();
    }

    private static Tree parse(JsonObject o) {
        String cls = o.get("class").getAsString();
        String start = o.get("start").getAsString();
        Map<String, List<String>> nb = new LinkedHashMap<>();
        List<String[]> links = new ArrayList<>();
        JsonArray la = o.getAsJsonArray("links");
        for (JsonElement l : la) {
            JsonArray p = l.getAsJsonArray();
            String a = p.get(0).getAsString(), b = p.get(1).getAsString();
            links.add(new String[]{a, b});
            nb.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
            nb.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
        }
        List<SkillNode> nodes = new ArrayList<>();
        Map<String, SkillNode> byId = new LinkedHashMap<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (JsonElement el : o.getAsJsonArray("nodes")) {
            JsonObject n = el.getAsJsonObject();
            String id = n.get("id").getAsString();
            String kind = n.get("kind").getAsString();
            NodeType type = switch (kind) {
                case "start" -> NodeType.ROOT;
                case "active" -> NodeType.MAJOR_ACTIVE;
                case "passive" -> NodeType.MAJOR_PASSIVE;
                default -> NodeType.ATTRIBUTE_NODE;   // minor, major
            };
            StatType stat = StatType.NONE;
            if (n.has("stat")) {
                try { stat = StatType.valueOf(n.get("stat").getAsString().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ignored) {}
            }
            int x = n.get("x").getAsInt(), y = n.get("y").getAsInt();
            SkillNode node = new SkillNode(id, cls, str(n, "branch", "root"), n.has("tier") ? n.get("tier").getAsInt() : 0, x, y,
                    n.has("level") ? n.get("level").getAsInt() : 0, n.has("cost") ? n.get("cost").getAsInt() : 1,
                    List.copyOf(nb.getOrDefault(id, List.of())), type, stat, n.has("value") ? n.get("value").getAsDouble() : 0,
                    "minor".equals(kind), n.has("active") && n.get("active").getAsBoolean(), str(n, "icon", "•"));
            nodes.add(node);
            byId.put(id, node);
            minX = Math.min(minX, x); minY = Math.min(minY, y); maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
        }
        List<Branch> branches = new ArrayList<>();
        if (o.has("branches")) {
            for (JsonElement el : o.getAsJsonArray("branches")) {
                JsonObject b = el.getAsJsonObject();
                branches.add(new Branch(b.get("id").getAsString(), b.get("name").getAsString(), b.get("x").getAsInt(), b.get("y").getAsInt()));
                minX = Math.min(minX, b.get("x").getAsInt()); minY = Math.min(minY, b.get("y").getAsInt());
                maxX = Math.max(maxX, b.get("x").getAsInt()); maxY = Math.max(maxY, b.get("y").getAsInt());
            }
        }
        links.removeIf(l -> !byId.containsKey(l[0]) || !byId.containsKey(l[1]));
        if (!byId.containsKey(start)) throw new IllegalStateException("start node " + start + " is not in the web");
        return new Tree(cls, start, List.copyOf(nodes), Collections.unmodifiableMap(byId), List.copyOf(branches), List.copyOf(links),
                minX, minY, maxX, maxY);
    }

    private static String str(JsonObject o, String k, String def) {
        return o.has(k) ? o.get(k).getAsString() : def;
    }

    // ---- queries

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
            case "tank", "bastion", "guardian", "iron_bastion" -> "iron_bastion";
            default -> hunterClass;
        };
    }

    public static Tree getTree(String hunterClass) {
        return loaded.trees().get(normalizeClassId(hunterClass));
    }

    public static Collection<Tree> trees() {
        return loaded.trees().values();
    }

    public static boolean isSkillTreeNode(String skillId) {
        return loaded.byId().containsKey(skillId);
    }

    public static List<SkillNode> getNodesForClass(String hunterClass) {
        Tree t = getTree(hunterClass);
        return t == null ? Collections.emptyList() : t.nodes();
    }

    public static SkillNode getNode(String hunterClass, String skillId) {
        if (hunterClass != null) {
            Tree t = getTree(hunterClass);
            if (t != null && t.byId().containsKey(skillId)) return t.byId().get(skillId);
        }
        return loaded.byId().get(skillId);
    }

    public static SkillNode getNode(String skillId) {
        return loaded.byId().get(skillId);
    }

    /** Whether the player has learnt the node (stat nodes in the tree nodes, the rest in the skills). */
    public static boolean has(SystemData data, SkillNode node) {
        if (data == null || node == null) return false;
        return node.isStatNode() ? data.treeNodes().contains(node.skillId()) : data.skills().containsKey(node.skillId());
    }

    public static boolean has(SystemData data, String skillId) {
        SkillNode node = getNode(data == null ? null : data.hunterClass(), skillId);
        return node != null && has(data, node);
    }

    /** The node's rank: 0 not learnt, 1 for learnt stat nodes and the start, 1-3 for skills. */
    public static int rank(SystemData data, SkillNode node) {
        if (!has(data, node)) return 0;
        return node.isStatNode() ? 1 : Math.max(1, data.skills().getOrDefault(node.skillId(), 1));
    }

    public static int getPointsInBranch(SystemData data, String hunterClass, String branch) {
        if (data == null || hunterClass == null || branch == null) return 0;
        int total = 0;
        for (SkillNode node : getNodesForClass(hunterClass)) {
            if (branch.equalsIgnoreCase(node.branch())) total += rank(data, node);
        }
        return total;
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

    /** The whole web is always shown, so a path can be planned. */
    public static boolean isRevealed(SystemData data, SkillNode node) {
        return node != null;
    }

    /**
     * The webs the player spends points in: the base class's web, and once evolved the evolution's web too (an
     * evolved player keeps what was learnt as a base class).
     */
    public static List<Tree> treesOf(SystemData data) {
        if (data == null || !data.hasHunterClass()) return List.of();
        String cls = normalizeClassId(data.hunterClass());
        List<Tree> out = new ArrayList<>(2);
        String base = ClassEvolution.baseOf(data);
        if (base != null && !base.equals(cls)) {
            Tree b = getTree(base);
            if (b != null) out.add(b);
        }
        Tree t = getTree(cls);
        if (t != null) out.add(t);
        return out;
    }

    /** Whether the node belongs to one of the player's webs. */
    public static boolean isOwnNode(SystemData data, SkillNode node) {
        if (node == null) return false;
        for (Tree t : treesOf(data)) {
            if (t.hunterClass().equals(node.hunterClass())) return true;
        }
        return false;
    }

    /** Whether a learnt node is linked to this one (the start needs none). */
    public static boolean isReachable(SystemData data, SkillNode node) {
        if (node.isRoot()) return true;
        Tree t = getTree(node.hunterClass());
        if (t == null) return false;
        for (String id : node.prerequisiteIds()) {
            SkillNode p = t.byId().get(id);
            if (p != null && has(data, p)) return true;
        }
        return false;
    }

    public static boolean canUnlock(SystemData data, String skillId) {
        if (data == null || !data.hasHunterClass()) return false;
        SkillNode node = getNode(data.hunterClass(), skillId);
        if (node == null) return false;
        if (!isOwnNode(data, node)) return false;
        if (has(data, node)) return false;
        if (!isReachable(data, node)) return false;
        if (data.level() < node.requiredLevel()) return false;
        return data.skillPoints() >= node.spCost();
    }

    public static boolean canUpgrade(SystemData data, String skillId) {
        if (data == null || !data.hasHunterClass()) return false;
        SkillNode node = getNode(data.hunterClass(), skillId);
        if (node == null) return false;
        if (node.isAttribute() || node.isRoot()) return false;
        if (!isOwnNode(data, node)) return false;
        int curLvl = data.skills().getOrDefault(skillId, 0);
        if (curLvl < 1 || curLvl >= MAX_SKILL_LEVEL) return false;
        int nextRank = curLvl + 1;
        if (data.level() < getRequiredLevelForRank(node, nextRank)) return false;
        return data.skillPoints() >= getSpCostForRank(node, nextRank);
    }

    public static double getTotalAttributeBonus(SystemData data, StatType type) {
        if (data == null || !data.hasHunterClass()) return 0.0D;
        double total = 0.0D;
        for (Tree t : treesOf(data)) {
            for (String id : data.treeNodes()) {
                SkillNode node = t.byId().get(id);
                if (node != null && node.isAttribute() && node.statType() == type) total += node.statValue();
            }
        }
        return total;
    }

    /** Every node id of every web (for clearing a player's old tree). */
    public static Set<String> allNodeIds() {
        return loaded.byId().keySet();
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
            if (targetRank == 2) return Component.translatable("awakening_system.skill_tree.upgrade.active_2");
            if (targetRank == 3) return Component.translatable("awakening_system.skill_tree.upgrade.active_3");
        } else {
            if (targetRank == 2) return Component.translatable("awakening_system.skill_tree.upgrade.passive_2");
            if (targetRank == 3) return Component.translatable("awakening_system.skill_tree.upgrade.passive_3");
        }
        return Component.empty();
    }
}
