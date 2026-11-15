package com.soybe.awakeningsystem.dialogue;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The weaponsmith of the floor 11 town. Besides talking he sells plain weapons for coins, and forges the mod's
 * weapons to order: the player brings the ingots of the tower's ores (found on higher floors) and the coins, and
 * the weapon is ready a full Minecraft day later (24000 ticks, twenty real minutes). One order at a time. While
 * it is in the forge, a bar on floor 11 counts down the time left.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class SmithForge {
    private SmithForge() {}

    public static final String DIALOGUE = "town_smith";
    private static final long FORGE_TICKS = 24000L;

    private record Sale(String item, int price) {}

    private record Recipe(String weapon, String matA, int countA, String matB, int countB, int coins) {}

    private static final List<Sale> SALES = List.of(
            new Sale("minecraft:iron_sword", 70), new Sale("minecraft:iron_axe", 70),
            new Sale("minecraft:diamond_sword", 8000), new Sale("minecraft:diamond_axe", 8000),
            new Sale("minecraft:netherite_sword", 52000));

    /** Weapons he forges, the weaker ones from the ores of the lower floors. */
    private static final List<Recipe> RECIPES = List.of(
            new Recipe("siderite_sword", "ignite_ingot", 6, "grave_steel_ingot", 1, 2000),
            new Recipe("siderite_hamaxe", "ignite_ingot", 8, "grave_steel_ingot", 2, 2500),
            new Recipe("mirror_blade", "grave_steel_ingot", 6, "ignite_ingot", 2, 4000),
            new Recipe("earth_sword", "grave_steel_ingot", 8, "ignite_ingot", 3, 4500),
            new Recipe("durandal", "orichalcum_ingot", 6, "grave_steel_ingot", 4, 8000),
            new Recipe("darklight_greatsword", "orichalcum_ingot", 8, "ignite_ingot", 4, 9000),
            new Recipe("old_reaper", "gelidite_ingot", 6, "orichalcum_ingot", 4, 15000),
            new Recipe("void_edge", "verdanite_ingot", 6, "frost_crystal", 3, 25000),
            new Recipe("scarlet_devil", "perennite_ingot", 6, "verdanite_ingot", 4, 30000),
            new Recipe("exoblade", "dracolead_ingot", 6, "eridanium", 2, 60000),
            new Recipe("galaxia", "exodite_ingot", 6, "xenium_ingot", 4, 120000),
            new Recipe("ark_of_the_cosmos", "lumenite_ingot", 6, "exodite_ingot", 2, 200000));

    private static final int PAGE = 6;

    private static Item item(String id) {
        ResourceLocation rl = ResourceLocation.parse(id.contains(":") ? id : AwakeningSystemMod.MOD_ID + ":" + id);
        return BuiltInRegistries.ITEM.containsKey(rl) ? BuiltInRegistries.ITEM.get(rl) : null;
    }

    private static String key(String id) {
        Item it = item(id);
        return it == null ? id : it.getDescriptionId();
    }

    private static int count(ServerPlayer p, Item it) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(it)) n += s.getCount();
        return n;
    }

    private static void take(ServerPlayer p, Item it, int n) {
        for (ItemStack s : p.getInventory().items) {
            if (n <= 0) return;
            if (!s.is(it)) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
        }
    }

    private static String time(long ticks) {
        long s = Math.max(0, ticks) / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static final String T = "awakening_system.smith.";

    // ── the talk ─────────────────────────────────────────────────────────────

    public static void node(ServerPlayer player, Entity npc, String node) {
        String speaker = NpcDialogueManager.townSpeaker(npc);
        Order order = Data.get(player.getServer()).orders.get(player.getUUID());
        long now = player.getServer().overworld().getGameTime();
        if (node.equals("root")) {
            String status = order == null ? T + "collect_none" : now >= order.readyAt ? T + "collect_ready|" + key(order.weapon)
                    : T + "collect_wait|" + key(order.weapon) + "|" + time(order.readyAt - now);
            NpcDialogueManager.sendNodePublic(player, npc.getId(), DIALOGUE, "root", "DEFAULT", speaker, T + "hello", List.of(
                    new DialogueChoice(0, T + "ask.buy", "buy", "NEXT"),
                    new DialogueChoice(1, T + "ask.order", "order0", "NEXT"),
                    new DialogueChoice(2, status, "collect", "NEXT"),
                    new DialogueChoice(3, "awakening_system.town.talk.ask.work", "talk", "NEXT"),
                    new DialogueChoice(4, "awakening_system.town.talk.ask.bye", "goodbye", "CLOSE")));
            return;
        }
        if (node.equals("buy")) {
            List<DialogueChoice> c = new ArrayList<>();
            for (int i = 0; i < SALES.size(); i++)
                c.add(new DialogueChoice(i, T + "buy_choice|" + key(SALES.get(i).item) + "|" + SALES.get(i).price, "buy", "NEXT"));
            c.add(new DialogueChoice(90, "awakening_system.town.talk.ask.more", "root", "NEXT"));
            NpcDialogueManager.sendNodePublic(player, npc.getId(), DIALOGUE, "buy", "DEFAULT", speaker,
                    T + "buy_text|" + SystemEvents.data(player).coins(), c);
            return;
        }
        if (node.startsWith("order")) {
            int page = Integer.parseInt(node.substring(5));
            List<DialogueChoice> c = new ArrayList<>();
            for (int i = page * PAGE; i < Math.min(RECIPES.size(), (page + 1) * PAGE); i++) {
                Recipe r = RECIPES.get(i);
                c.add(new DialogueChoice(i, T + "order_choice|" + key(r.weapon) + "|" + key(r.matA) + "|" + r.countA + "|" + key(r.matB) + "|" + r.countB + "|" + r.coins,
                        node, "NEXT"));
            }
            if ((page + 1) * PAGE < RECIPES.size()) c.add(new DialogueChoice(80, T + "ask.next_page", node, "NEXT"));
            c.add(new DialogueChoice(90, "awakening_system.town.talk.ask.more", "root", "NEXT"));
            NpcDialogueManager.sendNodePublic(player, npc.getId(), DIALOGUE, node, "DEFAULT", speaker,
                    order != null ? T + "order_busy|" + key(order.weapon) : T + "order_text", c);
            return;
        }
        say(player, npc, T + "nothing");
    }

    /** A line from the smith with a way back to his offers. */
    private static void say(ServerPlayer player, Entity npc, String text) {
        NpcDialogueManager.sendNodePublic(player, npc.getId(), DIALOGUE, "said", "DEFAULT", NpcDialogueManager.townSpeaker(npc), text, List.of(
                new DialogueChoice(90, "awakening_system.town.talk.ask.more", "root", "NEXT"),
                new DialogueChoice(91, "awakening_system.town.talk.ask.bye", "goodbye", "CLOSE")));
    }

    public static void choice(ServerPlayer player, Entity npc, String node, int choice) {
        if (choice == 90) { node(player, npc, "root"); return; }
        if (choice == 91 || choice == 4 && node.equals("root")) { NpcDialogueManager.closeDialogue(player); return; }
        Data data = Data.get(player.getServer());
        long now = player.getServer().overworld().getGameTime();
        switch (node) {
            case "root" -> {
                if (choice == 0) node(player, npc, "buy");
                else if (choice == 1) node(player, npc, "order0");
                else if (choice == 2) collect(player, npc, data, now);
                else if (choice == 3) NpcDialogueManager.startDialogue(player, npc, "town_npc");
            }
            case "buy" -> {
                if (choice < 0 || choice >= SALES.size()) return;
                Sale s = SALES.get(choice);
                Item it = item(s.item);
                SystemData d = SystemEvents.data(player);
                if (it == null || !d.spendCoins(s.price)) { say(player, npc, T + "no_coins|" + s.price); return; }
                give(player, new ItemStack(it));
                AwakeningSystemMod.sync(player);
                player.playNotifySound(SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 0.6F, 1.2F);
                say(player, npc, T + "sold|" + key(s.item));
            }
            default -> {
                if (!node.startsWith("order")) return;
                int page = Integer.parseInt(node.substring(5));
                if (choice == 80) { node(player, npc, "order" + (page + 1)); return; }
                if (choice < 0 || choice >= RECIPES.size()) return;
                if (data.orders.containsKey(player.getUUID())) { say(player, npc, T + "order_busy|" + key(data.orders.get(player.getUUID()).weapon)); return; }
                Recipe r = RECIPES.get(choice);
                Item a = item(r.matA), b = item(r.matB), w = item(r.weapon);
                SystemData d = SystemEvents.data(player);
                if (a == null || b == null || w == null) return;
                if (count(player, a) < r.countA || count(player, b) < r.countB) {
                    say(player, npc, T + "missing|" + key(r.matA) + "|" + r.countA + "|" + key(r.matB) + "|" + r.countB);
                    return;
                }
                if (!d.spendCoins(r.coins)) { say(player, npc, T + "no_coins|" + r.coins); return; }
                take(player, a, r.countA);
                take(player, b, r.countB);
                data.orders.put(player.getUUID(), new Order(r.weapon, now + FORGE_TICKS));
                data.setDirty();
                AwakeningSystemMod.sync(player);
                player.playNotifySound(SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 0.7F, 0.9F);
                say(player, npc, T + "ordered|" + key(r.weapon) + "|" + time(FORGE_TICKS));
            }
        }
    }

    private static void collect(ServerPlayer player, Entity npc, Data data, long now) {
        Order o = data.orders.get(player.getUUID());
        if (o == null) { say(player, npc, T + "collect_none"); return; }
        if (now < o.readyAt) { say(player, npc, T + "collect_wait|" + key(o.weapon) + "|" + time(o.readyAt - now)); return; }
        Item w = item(o.weapon);
        data.orders.remove(player.getUUID());
        data.setDirty();
        if (w != null) give(player, new ItemStack(w));
        hideBar(player.getUUID());
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.7F, 1.0F);
        say(player, npc, T + "collected|" + key(o.weapon));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    // ── the countdown bar ───────────────────────────────────────────────────

    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

    private static void hideBar(UUID id) {
        ServerBossEvent bar = BARS.remove(id);
        if (bar != null) bar.removeAllPlayers();
    }

    /** Once a second: a bar for each player with a weapon in the forge, shown while they are on floor 11. */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) return;
        Data data = Data.get(server);
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Order o = data.orders.get(p.getUUID());
            boolean onFloor = com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(p.level())
                    && com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getFloorForPos(p.level(), p.blockPosition()) == 11;
            if (o == null || !onFloor) { hideBar(p.getUUID()); continue; }
            ServerBossEvent bar = BARS.computeIfAbsent(p.getUUID(), id -> new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10));
            long left = o.readyAt - now;
            if (left > 0) {
                bar.setName(Component.translatable(T + "bar_wait", Component.translatable(key(o.weapon)), time(left)));
                bar.setProgress(1.0F - (float) left / FORGE_TICKS);
                bar.setColor(BossEvent.BossBarColor.YELLOW);
            } else {
                bar.setName(Component.translatable(T + "bar_ready", Component.translatable(key(o.weapon))));
                bar.setProgress(1.0F);
                bar.setColor(BossEvent.BossBarColor.GREEN);
            }
            if (!bar.getPlayers().contains(p)) bar.addPlayer(p);
        }
    }

    // ── saved orders ────────────────────────────────────────────────────────

    private record Order(String weapon, long readyAt) {}

    private static final class Data extends SavedData {
        final Map<UUID, Order> orders = new HashMap<>();

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(Data::new, Data::load, null), "awakening_smith_orders");
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            for (String k : tag.getAllKeys()) {
                CompoundTag o = tag.getCompound(k);
                try { d.orders.put(UUID.fromString(k), new Order(o.getString("Weapon"), o.getLong("ReadyAt"))); } catch (IllegalArgumentException ignored) {}
            }
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            orders.forEach((id, o) -> {
                CompoundTag c = new CompoundTag();
                c.putString("Weapon", o.weapon);
                c.putLong("ReadyAt", o.readyAt);
                tag.put(id.toString(), c);
            });
            return tag;
        }
    }
}
