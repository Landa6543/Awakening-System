package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.item.WeaponLevelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;

/**
 * A development check, only active with -Dawakening.toolShots: tries each tool's level abilities on blocks set up
 * beside a survival player and logs what was left (vein, tree, 3x3 digging, 3x3 harvest and replant, exp, tooltip).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class ToolShots {
    private ToolShots() {}

    private static final boolean ON = System.getProperty("awakening.toolShots") != null;
    private static int t = -1;

    private static ItemStack leveled(net.minecraft.world.item.Item item, int lvl) {
        ItemStack s = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putInt(WeaponLevelManager.TAG_WEAPON_LEVEL, lvl);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return s;
    }

    private static void log(String s, Object... a) {
        AwakeningSystemMod.LOGGER.info("[ToolShots] " + s, a);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        t++;
        if (t == 120) mc.stop();
        if (t != 60) return;
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode survival " + sp.getGameProfile().getName());
            ServerLevel lvl = sp.serverLevel();
            BlockPos o = sp.blockPosition().offset(6, 0, 6);
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -3, -4), o.offset(16, 12, 4))) lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -4, -4), o.offset(16, -4, 4))) lvl.setBlock(p, Blocks.STONE.defaultBlockState(), 2);

            // pickaxe 50: a 6-block iron vein
            for (int i = 0; i < 6; i++) lvl.setBlock(o.offset(i % 3, -3, i / 3), Blocks.IRON_ORE.defaultBlockState(), 2);
            ItemStack pick = leveled(Items.DIAMOND_PICKAXE, 50);
            sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pick);
            sp.gameMode.destroyBlock(o.offset(0, -3, 0));
            int ores = 0; for (int i = 0; i < 6; i++) if (lvl.getBlockState(o.offset(i % 3, -3, i / 3)).is(Blocks.IRON_ORE)) ores++;
            log("pickaxe lv50: iron ores left {} of 6, tool exp now {}", ores, WeaponLevelManager.getExp(sp.getMainHandItem()));

            // axe 50: a 6-high oak with leaves on top
            BlockPos tr = o.offset(5, -3, 0);
            lvl.setBlock(tr.below(), Blocks.DIRT.defaultBlockState(), 2);
            for (int y = 0; y < 6; y++) lvl.setBlock(tr.above(y), Blocks.OAK_LOG.defaultBlockState(), 2);
            for (BlockPos p : BlockPos.betweenClosed(tr.offset(-2, 4, -2), tr.offset(2, 6, 2)))
                if (lvl.getBlockState(p).isAir()) lvl.setBlock(p, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), 2);
            sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, leveled(Items.DIAMOND_AXE, 50));
            sp.gameMode.destroyBlock(tr);
            int logs = 0; for (int y = 0; y < 6; y++) if (lvl.getBlockState(tr.above(y)).is(Blocks.OAK_LOG)) logs++;
            log("axe lv50: oak logs left {} of 6, stump now {}", logs, lvl.getBlockState(tr).getBlock());

            // shovel 25: a 3x3 of dirt, dug from above
            BlockPos sh = o.offset(10, -3, 0);
            for (BlockPos p : BlockPos.betweenClosed(sh.offset(-1, 0, -1), sh.offset(1, 0, 1))) lvl.setBlock(p, Blocks.DIRT.defaultBlockState(), 2);
            sp.teleportTo(sh.getX() + 0.5, sh.getY() + 1, sh.getZ() + 0.5);
            sp.setXRot(90);
            sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, leveled(Items.DIAMOND_SHOVEL, 25));
            sp.gameMode.destroyBlock(sh);
            int dirt = 0; for (BlockPos p : BlockPos.betweenClosed(sh.offset(-1, 0, -1), sh.offset(1, 0, 1))) if (lvl.getBlockState(p).is(Blocks.DIRT)) dirt++;
            log("shovel lv25: dirt left {} of 9", dirt);

            // hoe 50: ripe wheat 3x3 on farmland
            BlockPos ho = o.offset(14, -3, 0);
            for (BlockPos p : BlockPos.betweenClosed(ho.offset(-1, -1, -1), ho.offset(1, -1, 1))) lvl.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 2);
            for (BlockPos p : BlockPos.betweenClosed(ho.offset(-1, 0, -1), ho.offset(1, 0, 1))) lvl.setBlock(p, ((CropBlock) Blocks.WHEAT).getStateForAge(7), 2);
            sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, leveled(Items.DIAMOND_HOE, 50));
            sp.gameMode.destroyBlock(ho);
            int ripe = 0, young = 0;
            for (BlockPos p : BlockPos.betweenClosed(ho.offset(-1, 0, -1), ho.offset(1, 0, 1))) {
                var s = lvl.getBlockState(p);
                if (s.getBlock() instanceof CropBlock c) { if (c.isMaxAge(s)) ripe++; else young++; }
            }
            log("hoe lv50: ripe left {}, replanted {} of 9", ripe, young);

            // tooltips: a pickaxe shows its trade, no damage
            var tip = new ArrayList<Component>();
            WeaponLevelManager.appendWeaponLevelTooltip(leveled(Items.DIAMOND_PICKAXE, 30), tip);
            for (Component c : tip) log("tip: {}", c.getString());
            log("pickaxe damage multiplier at lv30: {}", WeaponLevelManager.getDamageMultiplier(leveled(Items.DIAMOND_PICKAXE, 30)));
        });
    }
}
