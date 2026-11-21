package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tools level up by doing their own work (mining, digging, chopping, farming) and, instead of damage, unlock
 * abilities of their trade at levels 10, 25, 50, 75 and 100. Holding Shift turns the area abilities off for that block.
 * <ul>
 *   <li>Pickaxe: faster mining, extra ore drops (fortune), vein mining of ores, ores smelted as they drop.</li>
 *   <li>Axe: faster chopping, felling the whole tree, a sapling replanted, extra logs.</li>
 *   <li>Shovel: faster digging, 3x3 digging, finds, 5x5 digging.</li>
 *   <li>Hoe: 3x3 tilling, 3x3 harvest that replants, extra crops, 5x5 tilling.</li>
 * </ul>
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class ToolLevelAbilities {
    private ToolLevelAbilities() {}

    public enum Kind { PICKAXE, AXE, SHOVEL, HOE }

    /** Blocks broken by an ability itself, so they don't set off more abilities or count twice. */
    private static final ThreadLocal<Boolean> CHAINING = ThreadLocal.withInitial(() -> false);

    public static Kind kind(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var item = stack.getItem();
        if (item instanceof PickaxeItem) return Kind.PICKAXE;
        if (item instanceof AxeItem) return Kind.AXE;
        if (item instanceof ShovelItem) return Kind.SHOVEL;
        if (item instanceof HoeItem) return Kind.HOE;
        return null;
    }

    /** A pickaxe, shovel or hoe: levels by work alone and gets no combat bonus (an axe is still a weapon too). */
    public static boolean isPureTool(ItemStack stack) {
        Kind k = kind(stack);
        return k != null && k != Kind.AXE;
    }

    private static int level(ItemStack stack) {
        return WeaponLevelManager.getLevel(stack);
    }

    // ── experience from work ─────────────────────────────────────────────────

    private static int workExp(Kind kind, BlockState state) {
        return switch (kind) {
            case PICKAXE -> state.is(Tags.Blocks.ORES) ? 40 : state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? 10 : 0;
            case AXE -> state.is(BlockTags.LOGS) ? 20 : state.is(BlockTags.MINEABLE_WITH_AXE) ? 6 : 0;
            case SHOVEL -> state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? 8 : 0;
            case HOE -> state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) ? 15
                    : state.is(BlockTags.MINEABLE_WITH_HOE) ? 5 : 0;
        };
    }

    // ── breaking ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void broke(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer sp) || !(event.getLevel() instanceof ServerLevel level)) return;
        if (sp.isCreative()) return;
        ItemStack tool = sp.getMainHandItem();
        Kind kind = kind(tool);
        if (kind == null) return;
        BlockState state = event.getState();
        int exp = workExp(kind, state);
        if (exp <= 0) return;
        WeaponLevelManager.addExp(tool, exp, sp);
        if (CHAINING.get() || sp.isShiftKeyDown()) return;

        int lvl = level(tool);
        BlockPos pos = event.getPos();
        List<BlockPos> more = switch (kind) {
            case PICKAXE -> lvl >= 50 && state.is(Tags.Blocks.ORES) ? vein(level, pos, state, lvl >= 100 ? 32 : 16) : List.of();
            case AXE -> lvl >= 25 && state.is(BlockTags.LOGS) ? tree(level, pos, lvl >= 75 ? 160 : 64) : List.of();
            case SHOVEL -> lvl >= 25 && state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? area(level, pos, sp, lvl >= 100 ? 2 : 1, BlockTags.MINEABLE_WITH_SHOVEL) : List.of();
            case HOE -> lvl >= 50 && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) ? crops(level, pos) : List.of();
        };
        if (more.isEmpty()) {
            if (kind == Kind.HOE && lvl >= 50) replant(level, pos, state);
            if (kind == Kind.AXE && lvl >= 50 && state.is(BlockTags.LOGS)) replantSapling(level, pos, state);
            return;
        }
        CHAINING.set(true);
        try {
            for (BlockPos p : more) {
                if (tool.isEmpty() || kind(sp.getMainHandItem()) != kind) break;   // the tool broke
                BlockState s = level.getBlockState(p);
                if (sp.gameMode.destroyBlock(p) && kind == Kind.HOE) replant(level, p, s);
            }
        } finally {
            CHAINING.set(false);
        }
        if (kind == Kind.HOE) replant(level, pos, state);
        if (kind == Kind.AXE && lvl >= 50) replantSapling(level, pos, state);
    }

    /** The ores of the same kind touching this one, nearest first. */
    private static List<BlockPos> vein(ServerLevel level, BlockPos start, BlockState kind, int max) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && out.size() < max) {
            BlockPos p = queue.poll();
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (seen.contains(q)) continue;
                BlockPos qi = q.immutable();
                seen.add(qi);
                if (level.getBlockState(qi).is(kind.getBlock())) {
                    out.add(qi);
                    queue.add(qi);
                    if (out.size() >= max) break;
                }
            }
        }
        return out;
    }

    /** The logs of the tree above and around this one: only when leaves are near, so a log wall or a house isn't felled. */
    private static List<BlockPos> tree(ServerLevel level, BlockPos start, int max) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        boolean leaves = false;
        while (!queue.isEmpty() && out.size() < max) {
            BlockPos p = queue.poll();
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, 0, -1), p.offset(1, 1, 1))) {
                if (seen.contains(q)) continue;
                BlockPos qi = q.immutable();
                seen.add(qi);
                BlockState s = level.getBlockState(qi);
                if (s.is(BlockTags.LEAVES)) leaves = true;
                if (s.is(BlockTags.LOGS) && Math.abs(qi.getX() - start.getX()) <= 6 && Math.abs(qi.getZ() - start.getZ()) <= 6) {
                    out.add(qi);
                    queue.add(qi);
                }
            }
        }
        return leaves ? out : List.of();
    }

    /** The square of blocks around the broken one, across the face the player is digging into. */
    private static List<BlockPos> area(ServerLevel level, BlockPos center, ServerPlayer sp, int r,
                                       net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag) {
        var hit = sp.pick(6.0, 1.0F, false);
        Direction face = hit instanceof BlockHitResult bh ? bh.getDirection() : Direction.UP;
        List<BlockPos> out = new ArrayList<>();
        for (int a = -r; a <= r; a++) for (int b = -r; b <= r; b++) {
            if (a == 0 && b == 0) continue;
            BlockPos p = switch (face.getAxis()) {
                case Y -> center.offset(a, 0, b);
                case X -> center.offset(0, a, b);
                case Z -> center.offset(a, b, 0);
            };
            BlockState s = level.getBlockState(p);
            if (s.is(tag) && s.getDestroySpeed(level, p) >= 0 && s.getDestroySpeed(level, p) <= 2.0F) out.add(p);
        }
        return out;
    }

    /** The ripe crops in the 3x3 around this one. */
    private static List<BlockPos> crops(ServerLevel level, BlockPos center) {
        List<BlockPos> out = new ArrayList<>();
        for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
            if (a == 0 && b == 0) continue;
            BlockPos p = center.offset(a, 0, b);
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && crop.isMaxAge(s)) out.add(p);
        }
        return out;
    }

    /** A harvested crop is sown again where it stood (the seed taken from what it dropped is not needed). */
    private static void replant(ServerLevel level, BlockPos pos, BlockState was) {
        if (!(was.getBlock() instanceof CropBlock crop)) return;
        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).is(Blocks.FARMLAND))
            level.setBlock(pos, crop.getStateForAge(0), 3);
    }

    /** A felled tree's stump gets a sapling of its kind, when the ground takes one. */
    private static void replantSapling(ServerLevel level, BlockPos pos, BlockState log) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) return;
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(log.getBlock()).getPath()
                .replace("stripped_", "").replace("_log", "").replace("_wood", "");
        var sapling = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace(id + "_sapling"));
        if (sapling != Blocks.AIR && sapling.defaultBlockState().canSurvive(level, pos)) level.setBlock(pos, sapling.defaultBlockState(), 3);
    }

    // ── speed ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void speed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        Kind kind = kind(tool);
        if (kind == null || kind == Kind.HOE) return;
        BlockState s = event.getState();
        boolean right = switch (kind) {
            case PICKAXE -> s.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> s.is(BlockTags.MINEABLE_WITH_AXE);
            case SHOVEL -> s.is(BlockTags.MINEABLE_WITH_SHOVEL);
            default -> false;
        };
        if (!right) return;
        int lvl = level(tool);
        float bonus = kind == Kind.PICKAXE ? (lvl >= 75 ? 1.75F : lvl >= 25 ? 1.5F : lvl >= 10 ? 1.25F : 1.0F)
                : (lvl >= 75 ? 1.5F : lvl >= 10 ? 1.25F : 1.0F);
        if (bonus > 1.0F) event.setNewSpeed(event.getNewSpeed() * bonus);
    }

    // ── drops ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void drops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer sp)) return;
        ItemStack tool = event.getTool();
        Kind kind = kind(tool);
        if (kind == null) return;
        int lvl = level(tool);
        var r = event.getLevel().getRandom();
        BlockState s = event.getState();
        boolean silk = tool.getEnchantmentLevel(event.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH)) > 0;
        switch (kind) {
            case PICKAXE -> {
                if (!s.is(Tags.Blocks.ORES) || silk) return;
                // (fortune is added to the enchantment itself, see fortune() below)
                // level 100: raw ores come out smelted
                if (lvl >= 100) for (ItemEntity e : event.getDrops()) {
                    var in = new SingleRecipeInput(e.getItem());
                    event.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, in, event.getLevel()).ifPresent(h -> {
                        ItemStack out = h.value().getResultItem(event.getLevel().registryAccess()).copy();
                        if (!out.isEmpty()) { out.setCount(out.getCount() * e.getItem().getCount()); e.setItem(out); }
                    });
                }
            }
            case AXE -> {
                if (lvl >= 100 && s.is(BlockTags.LOGS) && r.nextFloat() < 0.25F)
                    for (ItemEntity e : new ArrayList<>(event.getDrops())) event.getDrops().add(copy(e, e.getItem().copy()));
            }
            case SHOVEL -> {
                // level 50: finds in the earth now and then
                if (lvl >= 50 && s.is(BlockTags.MINEABLE_WITH_SHOVEL) && r.nextFloat() < 0.03F) {
                    ItemStack find = switch (r.nextInt(4)) {
                        case 0 -> new ItemStack(net.minecraft.world.item.Items.FLINT);
                        case 1 -> new ItemStack(net.minecraft.world.item.Items.CLAY_BALL, 2);
                        case 2 -> new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 2);
                        default -> new ItemStack(net.minecraft.world.item.Items.BONE);
                    };
                    event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, find));
                }
            }
            case HOE -> {
                if (lvl >= 75 && s.getBlock() instanceof CropBlock crop && crop.isMaxAge(s) && r.nextFloat() < 0.5F)
                    for (ItemEntity e : new ArrayList<>(event.getDrops())) event.getDrops().add(copy(e, e.getItem().copy()));
            }
        }
    }

    /** Fortune levels a pickaxe's level adds to its Fortune enchantment: +1 from level 10, +2 from 75, +3 at 100. */
    public static int fortuneBonus(ItemStack stack) {
        if (kind(stack) != Kind.PICKAXE) return 0;
        int lvl = level(stack);
        return lvl >= 100 ? 3 : lvl >= 75 ? 2 : lvl >= 10 ? 1 : 0;
    }

    /**
     * The pickaxe's level counts as more Fortune: Fortune III on a level 75 pickaxe mines as Fortune V. The
     * enchantment on the item stays as it is; only the level the game reads while mining goes up. Silk Touch wins.
     */
    @SubscribeEvent
    public static void fortune(net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent event) {
        if (!event.isTargetting(net.minecraft.world.item.enchantment.Enchantments.FORTUNE)) return;
        int bonus = fortuneBonus(event.getStack());
        if (bonus <= 0) return;
        var ench = event.getEnchantments();
        var silk = event.getHolder(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);
        if (silk.isPresent() && ench.getLevel(silk.get()) > 0) return;
        event.getHolder(net.minecraft.world.item.enchantment.Enchantments.FORTUNE).ifPresent(h -> ench.set(h, ench.getLevel(h) + bonus));
    }

    private static ItemEntity copy(ItemEntity like, ItemStack stack) {
        ItemEntity e = new ItemEntity(like.level(), like.getX(), like.getY(), like.getZ(), stack);
        e.setDefaultPickUpDelay();
        return e;
    }

    // ── hoe: tilling an area ────────────────────────────────────────────────

    @SubscribeEvent
    public static void till(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack tool = sp.getMainHandItem();
        if (kind(tool) != Kind.HOE || sp.isShiftKeyDown()) return;
        int lvl = level(tool);
        if (lvl < 25) return;
        int r = lvl >= 100 ? 2 : 1;
        var level = sp.serverLevel();
        BlockPos c = event.getPos();
        int tilled = 0;
        for (int a = -r; a <= r; a++) for (int b = -r; b <= r; b++) {
            if (a == 0 && b == 0) continue;   // the game tills the one clicked
            BlockPos p = c.offset(a, 0, b);
            if (!level.getBlockState(p.above()).isAir()) continue;
            var ctx = new UseOnContext(sp, InteractionHand.MAIN_HAND, new BlockHitResult(p.getCenter(), Direction.UP, p, false));
            BlockState till = level.getBlockState(p).getToolModifiedState(ctx, ItemAbilities.HOE_TILL, false);
            if (till != null) { level.setBlock(p, till, 11); tilled++; }
        }
        if (tilled > 0) {
            tool.hurtAndBreak(Math.max(1, tilled / 3), sp, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            WeaponLevelManager.addExp(tool, tilled * 2, sp);
        }
    }

    // ── tooltip ──────────────────────────────────────────────────────────────

    /** The tool's abilities, unlocked ones bright and the next ones grey. */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        Kind kind = kind(stack);
        if (kind == null) return;
        int lvl = level(stack);
        String k = kind.name().toLowerCase(java.util.Locale.ROOT);
        tooltip.add(Component.translatable("awakening_system.tool_level.header").withStyle(ChatFormatting.GOLD));
        for (int m : new int[]{10, 25, 50, 75, 100}) {
            boolean on = lvl >= m;
            tooltip.add(Component.literal(on ? "§a✔ " : "§8✖ ").append(Component.translatable("awakening_system.tool_level.lv", m)
                    .withStyle(on ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY)).append(Component.literal(" "))
                    .append(Component.translatable("awakening_system.tool_level." + k + "." + m).withStyle(on ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY)));
        }
        tooltip.add(Component.translatable("awakening_system.tool_level.shift").withStyle(ChatFormatting.DARK_GRAY));
    }
}
