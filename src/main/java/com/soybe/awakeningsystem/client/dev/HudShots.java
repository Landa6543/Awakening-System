package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.hudShots: photographs the HUD (minimap included) at a few
 * window sizes, then the HUD tab of the interface settings (screenshots/hs_*.png).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class HudShots {
    private HudShots() {}

    private static final boolean ON = System.getProperty("awakening.hudShots") != null;
    private static final int[][] SIZES = {{854, 480}, {1280, 720}, {1920, 1080}};
    private static int t = -1;
    private static boolean userFullDive;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        mc.options.pauseOnLostFocus = false;
        t++;
        if (t < 120 && t < 60 && mc.screen != null) mc.setScreen(null);
        if (System.getProperty("awakening.hudShots").equals("click") && t < 320 && t > 5) t = 319;   // straight to the settings
        if (System.getProperty("awakening.hudShots").startsWith("sao_")) {
            // a full-dive window, with a few test shadows in the army for the shadow command
            String which = System.getProperty("awakening.hudShots").substring(4);
            if (t == 60) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                var d = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                try {
                    var army = d.getStoredShadowArmy();
                    String[] names = {"Igris", "Iron", "Tank", "Beru", "Kaisel", "Greed"};
                    for (int i = 0; i < names.length; i++) {
                        var tag = new net.minecraft.nbt.CompoundTag();
                        tag.putString("ShadowBaseName", names[i]);
                        tag.putInt("RpgMobLevel", 20 + i * 15);
                        tag.putInt("ShadowRank", i % 4);
                        tag.putInt("SquadId", i % 3);
                        tag.putInt("ShadowKills", i * 7);
                        tag.putFloat("Health", 30 + i * 10);
                        tag.putFloat("MaxHealth", 90);
                        army.add(tag);
                    }
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] army is read-only: {}", e.toString());
                }
                mc.setScreen(switch (which) {
                    case "shadow" -> new com.soybe.awakeningsystem.client.gui.ShadowCommandScreen();
                    case "fables" -> new com.soybe.awakeningsystem.fable.FablesScreen(null);
                    case "config" -> new com.soybe.awakeningsystem.client.gui.GuiConfigScreen(null);
                    case "classpick" -> new com.soybe.awakeningsystem.ClassSelectionScreen(null);
                    case "cultivation" -> new com.soybe.awakeningsystem.CultivationScreen(null);
                    case "guild" -> new com.soybe.awakeningsystem.guild.GuildScreen(new net.minecraft.nbt.CompoundTag());
                    case "classtree" -> {
                        // the class set on the server and synced, as the tree asks the server for its data on opening
                        var server = mc.getSingleplayerServer();
                        server.execute(() -> {
                            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                            com.soybe.awakeningsystem.SystemEvents.data(sp).setHunterClass("sword_saint");
                            AwakeningSystemMod.sync(sp);
                        });
                        yield null;
                    }
                    default -> null;
                });
            }
            if (t == 70 && which.equals("classtree")) mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            if (t == 78 && which.equals("classtree")) mc.getWindow().setWindowed(1280, 720);
            if (t == 90) Screenshot.grab(mc.gameDirectory, "hs_sao_" + which + ".png", mc.getMainRenderTarget(), msg -> {});
            if (t == 95) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 100) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("fix")) {
            // a base class at level 60: both trees, cultivation and the inventory in full-dive, and the K menu opening
            var server = mc.getSingleplayerServer();
            if (t == 40 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                sp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                var d = com.soybe.awakeningsystem.SystemEvents.data(sp);
                d.resetHunterClass();
                d.setLevel(60);
                com.soybe.awakeningsystem.SystemEvents.selectHunterClass(sp, "base_assassin");
                AwakeningSystemMod.sync(sp);
            });
            if (t == 45) {
                userFullDive = com.soybe.awakeningsystem.client.sao.UiStyle.fullDive();
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            }
            if (t == 50) mc.getWindow().setWindowed(1600, 640);   // a wide window, as in the user's photo
            if (t == 60) mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            if (t == 80) Screenshot.grab(mc.gameDirectory, "hs_fix_tree.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 85) mc.getWindow().setWindowed(1280, 720);
            if (t == 90) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            }
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_fix_tree_sao.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 115) mc.setScreen(new com.soybe.awakeningsystem.CultivationScreen(null));
            if (t == 135) Screenshot.grab(mc.gameDirectory, "hs_fix_cult.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 140) mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
            if (t == 160) Screenshot.grab(mc.gameDirectory, "hs_fix_inv.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 165) mc.setScreen(null);
            if (t == 170) mc.setScreen(new com.soybe.awakeningsystem.SystemScreen());
            if (t == 176) Screenshot.grab(mc.gameDirectory, "hs_fix_menu_early.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 200) Screenshot.grab(mc.gameDirectory, "hs_fix_menu.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 205) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(userFullDive);   // the player's own choice back
            if (t == 210) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("inv")) {
            // the vanilla inventory in the full-dive look, with a few items in it
            var server = mc.getSingleplayerServer();
            if (t == 40 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                sp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                sp.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
                sp.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
                sp.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
                sp.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_LOG, 32));
                sp.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD, 12));
            });
            if (t == 50) mc.getWindow().setWindowed(1280, 720);
            if (t == 60) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                AwakeningSystemMod.LOGGER.info("[HudShots] inv: window is {}", mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
            }
            if (t == 85) Screenshot.grab(mc.gameDirectory, "hs_inv_sao.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 90) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 95) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("evo")) {
            // base class → trial → evolution, with photos of the windows on the way
            var server = mc.getSingleplayerServer();
            Runnable none = () -> {};
            java.util.function.Consumer<java.util.function.Consumer<net.minecraft.server.level.ServerPlayer>> onServer = job -> {
                if (server != null) server.execute(() -> job.accept(server.getPlayerList().getPlayer(mc.player.getUUID())));
            };
            if (t == 40) onServer.accept(sp -> {
                var d = com.soybe.awakeningsystem.SystemEvents.data(sp);
                d.resetHunterClass();
                d.setLevel(160);
                boolean advanced = com.soybe.awakeningsystem.SystemEvents.selectHunterClass(sp, "sword_saint");
                boolean picked = com.soybe.awakeningsystem.SystemEvents.selectHunterClass(sp, "base_warrior");
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: pick advanced directly {}, pick base {}, class {}, has start {}, webs {}",
                        advanced, picked, d.hunterClass(), d.skills().containsKey("wa_power_slash"), com.soybe.awakeningsystem.ClassSkillTree.treesOf(d).size());
                var tree = com.soybe.awakeningsystem.ClassSkillTree.getTree("base_warrior");
                for (int i = 0; i < 6 && tree != null; i++) {
                    var next = tree.nodes().stream().filter(n -> com.soybe.awakeningsystem.ClassSkillTree.canUnlock(d, n.skillId()))
                            .min(java.util.Comparator.comparingInt(com.soybe.awakeningsystem.ClassSkillTree.SkillNode::gridY)).orElse(null);
                    if (next == null) break;
                    com.soybe.awakeningsystem.SystemEvents.unlockSkillTreeNode(sp, next.skillId());
                }
                boolean slash = com.soybe.awakeningsystem.SystemEvents.activateSkill(sp, "wa_power_slash");
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: base nodes {}, skills from web {}, power slash used {}, evolve before trial {}",
                        d.treeNodes().size(), d.getUnlockedTreeSkillsCount(), slash, com.soybe.awakeningsystem.ClassEvolution.canEvolve(d));
                boolean started = com.soybe.awakeningsystem.ClassEvolution.startTrial(sp);
                var bosses = sp.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(30),
                        m -> m.getTags().contains("evolution_trial"));
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: trial started {}, bosses {} {}", started, bosses.size(),
                        bosses.isEmpty() ? "" : bosses.get(0).getName().getString());
                // bosses cap the damage of one hit, so the death itself is played out
                bosses.forEach(b -> {
                    b.setHealth(0);
                    b.die(sp.damageSources().playerAttack(sp));
                    b.discard();
                });
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: after the boss died, trial won {}, can evolve {}", d.evolutionTrialPassed(),
                        com.soybe.awakeningsystem.ClassEvolution.canEvolve(d));
                AwakeningSystemMod.sync(sp);
            });
            if (t == 50) mc.getWindow().setWindowed(1280, 720);
            if (t == 60) mc.setScreen(new com.soybe.awakeningsystem.ClassEvolutionScreen(null));
            if (t == 80) Screenshot.grab(mc.gameDirectory, "hs_evo_classic.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 85) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                mc.setScreen(new com.soybe.awakeningsystem.ClassEvolutionScreen(null));
            }
            if (t == 105) Screenshot.grab(mc.gameDirectory, "hs_evo_sao.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 108) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 110) mc.setScreen(new com.soybe.awakeningsystem.ClassSelectionScreen(null));
            if (t == 130) Screenshot.grab(mc.gameDirectory, "hs_evo_pick.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 135) onServer.accept(sp -> {
                var d = com.soybe.awakeningsystem.SystemEvents.data(sp);
                int before = d.treeNodes().size();
                boolean wrong = com.soybe.awakeningsystem.ClassEvolution.evolve(sp, "grand_archmage");
                boolean ok = com.soybe.awakeningsystem.ClassEvolution.evolve(sp, "sword_saint");
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: evolve to another base's class {}, to sword saint {}, class {}, signature {}, webs {}, base nodes kept {}/{}, attack bonus {}",
                        wrong, ok, d.hunterClass(), d.skills().containsKey("asrahan_mana_blade"), com.soybe.awakeningsystem.ClassSkillTree.treesOf(d).size(),
                        d.treeNodes().size(), before,
                        com.soybe.awakeningsystem.ClassSkillTree.getTotalAttributeBonus(d, com.soybe.awakeningsystem.ClassSkillTree.StatType.ATTACK_DAMAGE));
                boolean baseNode = com.soybe.awakeningsystem.ClassSkillTree.canUnlock(d, "base_warrior_n4");
                AwakeningSystemMod.LOGGER.info("[HudShots] evo: base web still learnable when evolved {}", baseNode);
            });
            if (t == 150) mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            if (t == 170) Screenshot.grab(mc.gameDirectory, "hs_evo_tree.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 175) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            }
            if (t == 180 && mc.screen != null) {
                try {
                    var f = mc.screen.getClass().getDeclaredField("viewBase");
                    f.setAccessible(true);
                    f.setBoolean(mc.screen, true);
                } catch (ReflectiveOperationException e) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] evo: no base tab: {}", e.toString());
                }
            }
            if (t == 200) Screenshot.grab(mc.gameDirectory, "hs_evo_base_sao.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 205) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 210) mc.stop();
            none.run();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("web")) {
            // the class web: a Sword Saint at level 120 learns a path of nodes, then both tree windows are photographed
            var server = mc.getSingleplayerServer();
            if (t == 40 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                var d = com.soybe.awakeningsystem.SystemEvents.data(sp);
                d.setHunterClass("sword_saint");
                d.setLevel(120);
                var tree = com.soybe.awakeningsystem.ClassSkillTree.getTree("sword_saint");
                AwakeningSystemMod.LOGGER.info("[HudShots] web: {} trees, sword_saint nodes {}, links {}, points {}",
                        com.soybe.awakeningsystem.ClassSkillTree.trees().size(), tree == null ? -1 : tree.nodes().size(),
                        tree == null ? -1 : tree.links().size(), d.skillPoints());
                if (tree == null) return;
                // learn the start, then walk up branch a (the nodes nearest the top first)
                com.soybe.awakeningsystem.SystemEvents.unlockSkillTreeNode(sp, tree.start());
                for (int step = 0; step < 9; step++) {
                    var next = tree.nodes().stream().filter(n -> com.soybe.awakeningsystem.ClassSkillTree.canUnlock(d, n.skillId()))
                            .min(java.util.Comparator.comparingInt(com.soybe.awakeningsystem.ClassSkillTree.SkillNode::gridY)).orElse(null);
                    if (next == null) break;
                    boolean ok = com.soybe.awakeningsystem.SystemEvents.unlockSkillTreeNode(sp, next.skillId());
                    AwakeningSystemMod.LOGGER.info("[HudShots] web: learn {} ({}) -> {}", next.skillId(), next.nodeType(), ok);
                }
                boolean notLinked = com.soybe.awakeningsystem.ClassSkillTree.canUnlock(d, "sword_heart_awakening");
                AwakeningSystemMod.LOGGER.info("[HudShots] web: points left {}, spent {}, stat nodes {}, skills from web {}, far node learnable {}, attack bonus {}, attack speed bonus {}",
                        d.skillPoints(), d.spentSkillPoints(), d.treeNodes().size(), d.getUnlockedTreeSkillsCount(), notLinked,
                        com.soybe.awakeningsystem.ClassSkillTree.getTotalAttributeBonus(d, com.soybe.awakeningsystem.ClassSkillTree.StatType.ATTACK_DAMAGE),
                        com.soybe.awakeningsystem.ClassSkillTree.getTotalAttributeBonus(d, com.soybe.awakeningsystem.ClassSkillTree.StatType.ATTACK_SPEED));
                // a refund gives every point back
                var copy = new com.soybe.awakeningsystem.SystemData();
                copy.deserializeNBT(sp.registryAccess(), d.serializeNBT(sp.registryAccess()));
                int back = copy.refundSkillTree();
                AwakeningSystemMod.LOGGER.info("[HudShots] web: refund gives back {}, points then {}, stat nodes then {}", back, copy.skillPoints(), copy.treeNodes().size());
                AwakeningSystemMod.sync(sp);
            });
            if (t == 70) mc.getWindow().setWindowed(1280, 720);
            java.util.function.BiConsumer<String, String> choose = (field, id) -> {
                try {
                    var f = mc.screen.getClass().getDeclaredField(field);
                    f.setAccessible(true);
                    f.set(mc.screen, id);
                } catch (ReflectiveOperationException | NullPointerException e) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] web: cannot choose a node: {}", e.toString());
                }
            };
            if (t == 80) mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            if (t == 90) choose.accept("selectedSkillId", "sword_saint_n4");
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_web_classic.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 120) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            }
            if (t == 130) choose.accept("chosen", "plum_blossom_rain");
            if (t == 150) Screenshot.grab(mc.gameDirectory, "hs_web_sao.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 155) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 160) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("goggles")) {
            // with Create installed: does Create see the goggles on a player wearing the Creator title?
            var server = mc.getSingleplayerServer();
            java.util.function.Supplier<Object> ask = () -> {
                try {
                    return Class.forName("com.simibubi.create.content.equipment.goggles.GogglesItem")
                            .getMethod("isWearingGoggles", net.minecraft.world.entity.player.Player.class).invoke(null, mc.player);
                } catch (Throwable e) { return e.toString(); }
            };
            if (t == 40) AwakeningSystemMod.LOGGER.info("[HudShots] goggles before title: {}", ask.get());
            if (t == 45 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                com.soybe.awakeningsystem.SystemEvents.data(sp).setEquippedTitle("creator");
                AwakeningSystemMod.sync(sp);
            });
            if (t == 80) AwakeningSystemMod.LOGGER.info("[HudShots] goggles with Creator title: {}", ask.get());
            if (t == 85) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("ring")) {
            // the Link Ring in hand and in the hotbar
            var server = mc.getSingleplayerServer();
            if (t == 60 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                sp.getInventory().selected = 0;
                sp.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "link_ring"))));
                sp.setXRot(10);
            });
            if (t == 90) mc.player.getInventory().selected = 0;
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_ring.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 115) mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
            if (t == 150) Screenshot.grab(mc.gameDirectory, "hs_ring3.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 155) mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            if (t == 170) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("notif")) {
            // a boss bar and the habit notice at once: the notice must sit under the bar
            var server = mc.getSingleplayerServer();
            if (t == 60 && server != null) server.execute(() -> {
                var src = server.createCommandSourceStack();
                server.getCommands().performPrefixedCommand(src, "bossbar add awakening_system:test \"Igris, Blood-Red Commander\"");
                server.getCommands().performPrefixedCommand(src, "bossbar set awakening_system:test players @a");
            });
            if (t == 90) com.soybe.awakeningsystem.AwakeningSystemClient.showNotification("awakening_system.notification.habit_upgrade.title",
                    "awakening_system.notification.habit_upgrade.message", "Ojo de Halcón:::3", 0xFF50C878);
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_notif.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 130) mc.stop();
            return;
        }
        for (int i = 0; i < SIZES.length; i++) {
            int at = 120 + i * 80;
            if (t == at) mc.getWindow().setWindowed(SIZES[i][0], SIZES[i][1]);
            if (t == at + 60) Screenshot.grab(mc.gameDirectory, "hs_" + SIZES[i][0] + ".png", mc.getMainRenderTarget(), msg -> {});
        }
        int cfg = 120 + SIZES.length * 80;
        if (t == cfg) {
            mc.getWindow().setWindowed(1280, 720);
            var screen = new com.soybe.awakeningsystem.client.gui.GuiConfigScreen(null);
            mc.setScreen(screen);
            try {   // straight to the HUD tab
                var f = screen.getClass().getDeclaredField("currentTab");
                f.setAccessible(true);
                f.setInt(screen, 2);
            } catch (ReflectiveOperationException ignored) {}
        }
        if (t == cfg + 40) Screenshot.grab(mc.gameDirectory, "hs_config.png", mc.getMainRenderTarget(), msg -> {});
        if (t == cfg + 50 && mc.screen != null) {
            // press the Minimap switch like a player would: down its column until the minimap turns off
            var s = mc.screen;
            boolean before = dev.diorama.Settings.get().minimap;
            int x = s.width / 2 + 60;
            for (int y = s.height / 2; y < s.height; y++) {
                s.mouseClicked(x, y, 0);
                if (dev.diorama.Settings.get().minimap != before) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] minimap switch at y {} turned it {}", y, dev.diorama.Settings.get().minimap);
                    break;
                }
                if (mc.screen != s) { mc.setScreen(s); }
                s.mouseClicked(x, y, 0);   // undo whatever else that row was
            }
            mc.setScreen(null);
        }
        if (t == cfg + 90) Screenshot.grab(mc.gameDirectory, "hs_off.png", mc.getMainRenderTarget(), msg -> {});
        if (t == cfg + 95) { var ds = dev.diorama.Settings.get(); ds.minimap = true; ds.save(); }
        if (t == cfg + 100) mc.stop();
    }
}
