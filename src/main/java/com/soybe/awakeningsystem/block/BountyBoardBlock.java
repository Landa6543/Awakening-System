package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.GuildRankManager;
import com.soybe.awakeningsystem.OpenBountyBoardPayload;
import com.soybe.awakeningsystem.SelectBountyPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gran Tablón de Cacerías y Contratos del Gremio de Aventureros (Isekai).
 * Gestiona una rica base de datos de contratos distribuidos de manera aleatoria
 * e individual según el rango de cada jugador.
 */
public class BountyBoardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BountyBoardBlock> CODEC = simpleCodec(BountyBoardBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final int OFFERS_COUNT = 8;

    private static final VoxelShape SHAPE_NORTH = Block.box(-16.0D, 0.0D, 11.5D, 32.0D, 32.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 32.0D, 4.5D);
    private static final VoxelShape SHAPE_WEST  = Block.box(11.5D, 0.0D, -16.0D, 16.0D, 32.0D, 32.0D);
    private static final VoxelShape SHAPE_EAST  = Block.box(0.0D, 0.0D, -16.0D, 4.5D, 32.0D, 32.0D);

    public static class BountyTemplate {
        public final String nameKey;
        public final String rank;
        public final String targetType;
        public final int count;
        public final int coins;
        public final int exp;
        public final int medals;

        public BountyTemplate(String nameKey, String rank, String targetType, int count, int coins, int exp, int medals) {
            this.nameKey = nameKey;
            this.rank = rank;
            this.targetType = targetType;
            this.count = count;
            this.coins = coins;
            this.exp = exp;
            this.medals = medals;
        }
    }

    public static final List<BountyTemplate> ALL_BOUNTIES = List.of(
            new BountyTemplate("awakening_system.bounty.flower_gathering", "F", "collect_flower", 10, 450, 500, 1),
            new BountyTemplate("awakening_system.bounty.slime_hunt", "F", "isekai_slime", 6, 500, 600, 1),
            new BountyTemplate("awakening_system.bounty.goblin_scout_hunt", "F", "goblin_scout", 6, 600, 750, 1),
            new BountyTemplate("awakening_system.bounty.wild_wood_gathering", "F", "collect_wood", 16, 550, 650, 1),
            new BountyTemplate("awakening_system.bounty.fresh_wheat_harvest", "F", "collect_wheat", 16, 500, 600, 1),
            new BountyTemplate("awakening_system.bounty.cave_bat_purge", "F", "bat_hunt", 5, 520, 620, 1),
            new BountyTemplate("awakening_system.bounty.wild_wolf_hunt", "F", "isekai_wolf", 4, 600, 700, 1),
            new BountyTemplate("awakening_system.bounty.mana_feather_gathering", "F", "collect_feather", 8, 580, 700, 1),
            new BountyTemplate("awakening_system.bounty.skeleton_bone_salvage", "F", "collect_bone", 8, 620, 750, 1),
            new BountyTemplate("awakening_system.bounty.forest_berry_harvest", "F", "collect_berries", 16, 480, 580, 1),

            new BountyTemplate("awakening_system.bounty.giant_slime_cull", "E", "giant_slime", 3, 800, 1000, 2),
            new BountyTemplate("awakening_system.bounty.goblin_patrol", "E", "goblin_scout", 10, 950, 1200, 2),
            new BountyTemplate("awakening_system.bounty.wolf_threat", "E", "isekai_wolf", 8, 850, 1100, 2),
            new BountyTemplate("awakening_system.bounty.iron_prospecting", "E", "collect_iron", 10, 900, 1200, 2),
            new BountyTemplate("awakening_system.bounty.mana_pork_supply", "E", "collect_mana_pork", 6, 950, 1250, 2),
            new BountyTemplate("awakening_system.bounty.drowned_shore_purge", "E", "drowned_hunt", 8, 900, 1150, 2),
            new BountyTemplate("awakening_system.bounty.leather_procurement", "E", "collect_leather", 10, 880, 1100, 2),
            new BountyTemplate("awakening_system.bounty.bamboo_stalk_harvest", "E", "collect_bamboo", 24, 820, 1050, 2),
            new BountyTemplate("awakening_system.bounty.hilichurl_patrol", "E", "hilichurl_hunt", 6, 1000, 1300, 2),
            new BountyTemplate("awakening_system.bounty.forest_spider_clearing", "E", "isekai_spider", 8, 880, 1150, 2),

            new BountyTemplate("awakening_system.bounty.goblin_warrior_cull", "D", "goblin_warrior", 6, 1400, 1800, 3),
            new BountyTemplate("awakening_system.bounty.forest_clearing", "D", "any_isekai", 10, 1200, 1500, 3),
            new BountyTemplate("awakening_system.bounty.skeleton_patrol", "D", "isekai_skeleton", 8, 1300, 1600, 3),
            new BountyTemplate("awakening_system.bounty.beholder_recon", "D", "isekai_beholder", 4, 1450, 1800, 3),
            new BountyTemplate("awakening_system.bounty.gold_prospecting", "D", "collect_gold", 8, 1350, 1700, 3),
            new BountyTemplate("awakening_system.bounty.mana_wool_procurement", "D", "collect_mana_wool", 8, 1400, 1750, 3),
            new BountyTemplate("awakening_system.bounty.creeper_volatile_purge", "D", "creeper_hunt", 6, 1500, 1900, 3),
            new BountyTemplate("awakening_system.bounty.frost_elf_ambush", "D", "frost_elf", 4, 1550, 2000, 3),
            new BountyTemplate("awakening_system.bounty.redstone_excavation", "D", "collect_redstone", 16, 1300, 1650, 3),
            new BountyTemplate("awakening_system.bounty.screaming_mandrake_harvest", "D", "mandrake_hunt", 4, 1500, 1900, 3),

            new BountyTemplate("awakening_system.bounty.goblin_shaman_purge", "C", "goblin_shaman", 4, 1900, 2500, 5),
            new BountyTemplate("awakening_system.bounty.shadow_aberration", "C", "isekai_monster", 8, 1700, 2200, 4),
            new BountyTemplate("awakening_system.bounty.cave_spider_infestation", "C", "cave_spider", 10, 1850, 2400, 4),
            new BountyTemplate("awakening_system.bounty.dungeon_patrol", "C", "any_isekai", 14, 2000, 2600, 5),
            new BountyTemplate("awakening_system.bounty.beastman_clash", "C", "beastman_warrior", 6, 2100, 2700, 5),
            new BountyTemplate("awakening_system.bounty.lapis_lazuli_mining", "C", "collect_lapis", 12, 1950, 2500, 5),
            new BountyTemplate("awakening_system.bounty.mitachurl_cull", "C", "mitachurl_hunt", 4, 2200, 2800, 5),
            new BountyTemplate("awakening_system.bounty.enderman_aberration", "C", "enderman_hunt", 4, 2300, 2900, 5),
            new BountyTemplate("awakening_system.bounty.swamp_witch_coven", "C", "witch_hunt", 4, 2150, 2750, 5),
            new BountyTemplate("awakening_system.bounty.nether_quartz_mining", "C", "collect_quartz", 16, 2000, 2600, 5),

            new BountyTemplate("awakening_system.bounty.goblin_clan_extermination", "B", "goblin_threat", 18, 3000, 3800, 7),
            new BountyTemplate("awakening_system.bounty.apex_beasts", "B", "giant_slime", 5, 2500, 3200, 6),
            new BountyTemplate("awakening_system.bounty.ruin_guardians", "B", "isekai_guardian", 5, 2800, 3600, 7),
            new BountyTemplate("awakening_system.bounty.isekai_dungeon_purge", "B", "any_isekai", 20, 3200, 4000, 8),
            new BountyTemplate("awakening_system.bounty.abyssal_orc_purge", "B", "abyssal_orc", 8, 3100, 3900, 8),
            new BountyTemplate("awakening_system.bounty.abyssal_necromancer_cull", "B", "abyssal_necromancer", 4, 3300, 4200, 8),
            new BountyTemplate("awakening_system.bounty.ruin_guard_cull", "B", "ruin_guard_hunt", 3, 3200, 4100, 8),
            new BountyTemplate("awakening_system.bounty.shadow_direwolf_hunt", "B", "shadow_direwolf", 6, 3400, 4300, 8),
            new BountyTemplate("awakening_system.bounty.blaze_core_harvest", "B", "collect_blaze_rod", 8, 3200, 4000, 8),
            new BountyTemplate("awakening_system.bounty.celestial_diamond_mining", "B", "collect_diamond", 6, 3500, 4500, 9),

            new BountyTemplate("awakening_system.bounty.calamity_subjugation", "A", "any_isekai", 25, 4000, 5500, 10),
            new BountyTemplate("awakening_system.bounty.abyssal_commander", "A", "isekai_monster", 12, 4800, 6500, 12),
            new BountyTemplate("awakening_system.bounty.ant_soldier_invasion", "A", "ant_soldier", 12, 4600, 6200, 12),
            new BountyTemplate("awakening_system.bounty.geovishap_cull", "A", "geovishap_hunt", 3, 5000, 6800, 13),
            new BountyTemplate("awakening_system.bounty.ruin_hunter_cull", "A", "ruin_hunter_hunt", 2, 4900, 6600, 13),
            new BountyTemplate("awakening_system.bounty.ruin_drake_cull", "A", "ruin_drake_hunt", 3, 5200, 7000, 14),
            new BountyTemplate("awakening_system.bounty.ancestral_warden_trial", "A", "warden_hunt", 2, 5500, 7500, 15),
            new BountyTemplate("awakening_system.bounty.void_beholder_purge", "A", "isekai_beholder", 10, 4800, 6400, 12),

            new BountyTemplate("awakening_system.bounty.ancient_dragon_threat", "S", "dragon_threat", 1, 7500, 10000, 16),
            new BountyTemplate("awakening_system.bounty.world_calamity", "S", "any_isekai", 35, 9000, 13000, 20),
            new BountyTemplate("awakening_system.bounty.elemental_demon_king_bounty", "S", "boss_elemental_demon_king", 1, 8500, 12000, 18),
            new BountyTemplate("awakening_system.bounty.snowboar_king_bounty", "S", "boss_snowboar_king", 1, 8800, 12500, 19),
            new BountyTemplate("awakening_system.bounty.yggdrasil_treant_bounty", "S", "boss_yggdrasil_treant", 1, 8500, 12000, 18),
            new BountyTemplate("awakening_system.bounty.shadow_monarch_bounty", "S", "boss_shadow_monarch", 1, 10000, 15000, 22)
    );

    public static final List<BountyTemplate> BOUNTIES = ALL_BOUNTIES;

    public static boolean isFlowerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(net.minecraft.tags.ItemTags.FLOWERS)) return true;
        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi) {
            Block block = bi.getBlock();
            if (block instanceof net.minecraft.world.level.block.FlowerBlock
                    || block instanceof net.minecraft.world.level.block.TallFlowerBlock) {
                return true;
            }
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("flower") || path.contains("ginseng") || path.contains("lotus")
                || path.contains("tulip") || path.contains("rose") || path.contains("orchid")
                || path.contains("dandelion") || path.contains("poppy") || path.contains("daisy")
                || path.contains("peony") || path.contains("allium") || path.contains("bluet")
                || path.contains("lily") || path.contains("sunflower") || path.contains("lilac")
                || path.contains("cornflower") || path.contains("herb") || path.contains("flora");
    }

    public static boolean isWoodItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(net.minecraft.tags.ItemTags.LOGS)) return true;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("log") || path.contains("wood") || path.contains("stem");
    }

    public static boolean isIronItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.IRON_INGOT) || stack.is(Items.RAW_IRON)) return true;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.equals("iron_ingot") || path.equals("raw_iron");
    }

    public static boolean isWheatItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.WHEAT) || stack.is(Items.WHEAT_SEEDS);
    }

    public static boolean isFeatherItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.FEATHER)) return true;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("feather");
    }

    public static boolean isBoneItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.BONE);
    }

    public static boolean isBerryItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES);
    }

    public static boolean isManaPorkItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (SystemItems.MANA_PORKCHOP.isBound() && stack.is(SystemItems.MANA_PORKCHOP.get())) return true;
        if (SystemItems.COOKED_MANA_PORKCHOP.isBound() && stack.is(SystemItems.COOKED_MANA_PORKCHOP.get())) return true;
        return stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP);
    }

    public static boolean isLeatherItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.LEATHER);
    }

    public static boolean isBambooItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.BAMBOO);
    }

    public static boolean isGoldItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.GOLD_INGOT) || stack.is(Items.RAW_GOLD);
    }

    public static boolean isManaWoolItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("wool");
    }

    public static boolean isRedstoneItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.REDSTONE);
    }

    public static boolean isLapisItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.LAPIS_LAZULI);
    }

    public static boolean isQuartzItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.QUARTZ);
    }

    public static boolean isBlazeRodItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.BLAZE_ROD);
    }

    public static boolean isDiamondItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.DIAMOND);
    }

    public static boolean matchesItemTarget(ItemStack stack, String targetType) {
        return switch (targetType) {
            case "collect_flower" -> isFlowerItem(stack);
            case "collect_wood" -> isWoodItem(stack);
            case "collect_iron" -> isIronItem(stack);
            case "collect_wheat" -> isWheatItem(stack);
            case "collect_feather" -> isFeatherItem(stack);
            case "collect_bone" -> isBoneItem(stack);
            case "collect_berries" -> isBerryItem(stack);
            case "collect_mana_pork" -> isManaPorkItem(stack);
            case "collect_leather" -> isLeatherItem(stack);
            case "collect_bamboo" -> isBambooItem(stack);
            case "collect_gold" -> isGoldItem(stack);
            case "collect_mana_wool" -> isManaWoolItem(stack);
            case "collect_redstone" -> isRedstoneItem(stack);
            case "collect_lapis" -> isLapisItem(stack);
            case "collect_quartz" -> isQuartzItem(stack);
            case "collect_blaze_rod" -> isBlazeRodItem(stack);
            case "collect_diamond" -> isDiamondItem(stack);
            default -> false;
        };
    }

    public static int countInventoryItems(Player player, String targetType) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            if (matchesItemTarget(stack, targetType)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean consumeInventoryItems(Player player, String targetType, int amountNeeded) {
        if (countInventoryItems(player, targetType) < amountNeeded) return false;
        int remaining = amountNeeded;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;
            if (matchesItemTarget(stack, targetType)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public static int rankTier(String r) {
        return switch (r) {
            case "F" -> 0;
            case "E" -> 1;
            case "D" -> 2;
            case "C" -> 3;
            case "B" -> 4;
            case "A" -> 5;
            default  -> 6;
        };
    }

    public static boolean isBountyAvailableForRank(String playerRank, String bountyRank) {
        int p = rankTier(playerRank);
        int b = rankTier(bountyRank);
        return b <= p + 1;
    }

    public static int[] rollBountiesForRank(String rank, int pinnedActiveIndex) {
        List<Integer> candidateIndices = new ArrayList<>();
        for (int i = 0; i < ALL_BOUNTIES.size(); i++) {
            BountyTemplate t = ALL_BOUNTIES.get(i);
            if (isBountyAvailableForRank(rank, t.rank)) {
                candidateIndices.add(i);
            }
        }
        if (candidateIndices.size() < OFFERS_COUNT) {
            for (int i = 0; i < ALL_BOUNTIES.size(); i++) {
                if (!candidateIndices.contains(i)) candidateIndices.add(i);
            }
        }

        Collections.shuffle(candidateIndices);
        int[] result = new int[OFFERS_COUNT];
        int ptr = 0;

        if (pinnedActiveIndex >= 0 && pinnedActiveIndex < ALL_BOUNTIES.size()) {
            result[0] = pinnedActiveIndex;
            candidateIndices.remove(Integer.valueOf(pinnedActiveIndex));
            ptr = 1;
        }

        for (int i = ptr; i < OFFERS_COUNT && (i - ptr) < candidateIndices.size(); i++) {
            result[i] = candidateIndices.get(i - ptr);
        }
        return result;
    }

    public static int[] getOrRollPlayerBounties(ServerPlayer sp) {
        CompoundTag pTag = sp.getPersistentData();
        int[] existing = pTag.getIntArray("bounty_offers");
        boolean active = pTag.getBoolean("bounty_active");
        int activeIdx = pTag.getInt("bounty_index");

        if (existing.length == OFFERS_COUNT) {
            boolean valid = true;
            boolean containsActive = !active;
            for (int idx : existing) {
                if (idx < 0 || idx >= ALL_BOUNTIES.size()) {
                    valid = false;
                    break;
                }
                if (active && idx == activeIdx) {
                    containsActive = true;
                }
            }
            if (valid && containsActive) {
                return existing;
            }
        }

        int completed = pTag.getInt("bounties_total_completed");
        String rank = GuildRankManager.getGuildRank(completed);
        int[] newOffers = rollBountiesForRank(rank, active ? activeIdx : -1);
        pTag.putIntArray("bounty_offers", newOffers);
        return newOffers;
    }

    public static int[] rerollPlayerBounties(ServerPlayer sp) {
        CompoundTag pTag = sp.getPersistentData();
        boolean active = pTag.getBoolean("bounty_active");
        int activeIdx = pTag.getInt("bounty_index");
        int completed = pTag.getInt("bounties_total_completed");
        String rank = GuildRankManager.getGuildRank(completed);
        int[] newOffers = rollBountiesForRank(rank, active ? activeIdx : -1);
        pTag.putIntArray("bounty_offers", newOffers);
        return newOffers;
    }

    public static void replaceClaimedBountySlot(ServerPlayer sp, int claimedIndex) {
        CompoundTag pTag = sp.getPersistentData();
        int[] offers = pTag.getIntArray("bounty_offers");
        if (offers.length != OFFERS_COUNT) {
            getOrRollPlayerBounties(sp);
            return;
        }
        int completed = pTag.getInt("bounties_total_completed");
        String rank = GuildRankManager.getGuildRank(completed);

        List<Integer> candidateIndices = new ArrayList<>();
        for (int i = 0; i < ALL_BOUNTIES.size(); i++) {
            BountyTemplate t = ALL_BOUNTIES.get(i);
            if (isBountyAvailableForRank(rank, t.rank)) {
                boolean inOffers = false;
                for (int o : offers) {
                    if (o == i) { inOffers = true; break; }
                }
                if (!inOffers) candidateIndices.add(i);
            }
        }
        if (candidateIndices.isEmpty()) {
            for (int i = 0; i < ALL_BOUNTIES.size(); i++) {
                boolean inOffers = false;
                for (int o : offers) {
                    if (o == i) { inOffers = true; break; }
                }
                if (!inOffers) candidateIndices.add(i);
            }
        }

        Collections.shuffle(candidateIndices);
        int replacement = candidateIndices.isEmpty() ? (claimedIndex + 1) % ALL_BOUNTIES.size() : candidateIndices.get(0);

        for (int i = 0; i < offers.length; i++) {
            if (offers[i] == claimedIndex) {
                offers[i] = replacement;
                break;
            }
        }
        pTag.putIntArray("bounty_offers", offers);
    }

    public BountyBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(FACING, clickedFace);
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            openBoardFor(sp);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }

    public static void openBoardFor(ServerPlayer sp) {
        CompoundTag pTag = sp.getPersistentData();
        int completed = pTag.getInt("bounties_total_completed");
        String guildRank = GuildRankManager.getGuildRank(completed);
        int nextRankNeeded = GuildRankManager.getNextRankNeeded(completed);

        boolean active = pTag.getBoolean("bounty_active");
        int activeIndex = pTag.getInt("bounty_index");
        String curTarget = pTag.getString("bounty_target");
        if (active && curTarget.startsWith("collect_")) {
            int currentItems = countInventoryItems(sp, curTarget);
            pTag.putInt("bounty_killed", Math.min(pTag.getInt("bounty_needed"), currentItems));
        }
        int killed = pTag.getInt("bounty_killed");
        int needed = pTag.getInt("bounty_needed");

        int[] offers = getOrRollPlayerBounties(sp);

        PacketDistributor.sendToPlayer(sp, new OpenBountyBoardPayload(
                guildRank, completed, nextRankNeeded,
                active, activeIndex, killed, needed, offers
        ));

        sp.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static BlockPos findNearbyBountyBoard(Level level, BlockPos clickedPos, Direction clickedFace) {
        BlockState state = level.getBlockState(clickedPos);
        if (state.getBlock() instanceof BountyBoardBlock) {
            return clickedPos;
        }

        for (int dy = -1; dy <= 0; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    BlockPos checkPos = clickedPos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(checkPos);
                    if (s.getBlock() instanceof BountyBoardBlock) {
                        Direction boardFacing = s.getValue(FACING);
                        if (isPosInBountyBoardFootprint(checkPos, boardFacing, clickedPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean isPosInBountyBoardFootprint(BlockPos boardPos, Direction facing, BlockPos testPos) {
        int dy = testPos.getY() - boardPos.getY();
        if (dy < 0 || dy > 1) return false;

        Direction right = facing.getClockWise();
        BlockPos wallPos = boardPos.relative(facing.getOpposite());

        for (int w = -1; w <= 1; w++) {
            BlockPos frontPos = boardPos.relative(right, w).above(dy);
            BlockPos backPos = wallPos.relative(right, w).above(dy);
            if (testPos.equals(frontPos) || testPos.equals(backPos)) {
                return true;
            }
        }
        return false;
    }

    public static void handleSelectBounty(ServerPlayer sp, SelectBountyPayload payload) {
        CompoundTag pTag = sp.getPersistentData();
        SystemData data = SystemEvents.data(sp);
        ServerLevel sl = sp.serverLevel();

        if (payload.refresh()) {
            int[] offers = rerollPlayerBounties(sp);
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.15D);
            sp.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);
            sp.displayClientMessage(Component.translatable("awakening_system.bounty.rerolled_toast"), true);

            int completed = pTag.getInt("bounties_total_completed");
            String guildRank = GuildRankManager.getGuildRank(completed);
            int nextRankNeeded = GuildRankManager.getNextRankNeeded(completed);
            boolean active = pTag.getBoolean("bounty_active");
            int activeIndex = pTag.getInt("bounty_index");
            int killed = pTag.getInt("bounty_killed");
            int needed = pTag.getInt("bounty_needed");

            PacketDistributor.sendToPlayer(sp, new OpenBountyBoardPayload(
                    guildRank, completed, nextRankNeeded,
                    active, activeIndex, killed, needed, offers
            ));
            return;
        }

        if (payload.claim()) {
            boolean active = pTag.getBoolean("bounty_active");
            int killed = pTag.getInt("bounty_killed");
            int needed = pTag.getInt("bounty_needed");
            int activeIdx = pTag.getInt("bounty_index");

            if (active && killed >= needed) {
                String target = pTag.getString("bounty_target");
                if (target.startsWith("collect_")) {
                    if (!consumeInventoryItems(sp, target, needed)) {
                        sp.displayClientMessage(Component.translatable("awakening_system.bounty.insufficient_supplies"), true);
                        return;
                    }
                }
                pTag.putBoolean("bounty_active", false);
                int rewardCoins = pTag.getInt("bounty_coins");
                int rewardExp = pTag.getInt("bounty_exp");
                int rewardMedals = pTag.getInt("bounty_medals");
                String bName = pTag.getString("bounty_name");
                String rank = pTag.getString("bounty_rank");

                data.addCoins(rewardCoins);
                data.addExperience(rewardExp);

                if (SystemItems.ADVENTURER_MEDAL.isBound()) {
                    ItemStack medalStack = new ItemStack(SystemItems.ADVENTURER_MEDAL.get(), rewardMedals);
                    if (!sp.getInventory().add(medalStack)) {
                        sp.drop(medalStack, false);
                    }
                }

                int completedTotal = pTag.getInt("bounties_total_completed") + 1;
                pTag.putInt("bounties_total_completed", completedTotal);
                String newGuildRank = GuildRankManager.getGuildRank(completedTotal);
                pTag.putString("guild_rank", newGuildRank);
                com.soybe.awakeningsystem.AwakeningSystemMod.syncPlayerBadge(sp);

                replaceClaimedBountySlot(sp, activeIdx);
                int[] offers = getOrRollPlayerBounties(sp);

                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 0.4D, 0.4D, 0.4D, 0.15D);
                sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                sp.sendSystemMessage(Component.translatable("awakening_system.bounty.completed_title", rank, bName)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                sp.sendSystemMessage(Component.translatable("awakening_system.bounty.reward_summary", rewardCoins, rewardExp, rewardMedals)
                        .withStyle(ChatFormatting.GREEN));
                sp.displayClientMessage(Component.translatable("awakening_system.bounty.claimed_toast"), true);

                com.soybe.awakeningsystem.SystemEvents.grantFable(sp, "bounty_hunter_novice");
                if (completedTotal >= 15) {
                    com.soybe.awakeningsystem.SystemEvents.grantFable(sp, "guild_veteran");
                }
                if (completedTotal >= 50) {
                    com.soybe.awakeningsystem.SystemEvents.grantFable(sp, "giant_story_star_stream_savior");
                }

                int nextReq = GuildRankManager.getNextRankNeeded(completedTotal);
                PacketDistributor.sendToPlayer(sp, new OpenBountyBoardPayload(
                        newGuildRank, completedTotal, nextReq,
                        false, 0, 0, 0, offers
                ));
            }
        } else {
            int idx = payload.bountyIndex();
            if (idx >= 0 && idx < ALL_BOUNTIES.size()) {
                BountyTemplate template = ALL_BOUNTIES.get(idx);
                String localizedName = Component.translatable(template.nameKey).getString();

                int initialProgress = 0;
                if (template.targetType.startsWith("collect_")) {
                    initialProgress = Math.min(template.count, countInventoryItems(sp, template.targetType));
                }

                pTag.putBoolean("bounty_active", true);
                pTag.putInt("bounty_index", idx);
                pTag.putString("bounty_name", localizedName);
                pTag.putString("bounty_target", template.targetType);
                pTag.putString("bounty_rank", template.rank);
                pTag.putInt("bounty_needed", template.count);
                pTag.putInt("bounty_killed", initialProgress);
                pTag.putInt("bounty_coins", template.coins);
                pTag.putInt("bounty_exp", template.exp);
                pTag.putInt("bounty_medals", template.medals);

                sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.2D);
                sp.playNotifySound(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 1.0F, 1.0F);
                sp.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);

                sp.sendSystemMessage(Component.translatable("awakening_system.bounty.new_contract_title", template.rank, localizedName)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
                sp.sendSystemMessage(Component.translatable("awakening_system.bounty.new_contract_desc", template.count, template.coins, template.medals)
                        .withStyle(ChatFormatting.GRAY));
                sp.displayClientMessage(Component.translatable("awakening_system.bounty.contract_accepted_toast"), true);

                int completed = pTag.getInt("bounties_total_completed");
                String guildRank = GuildRankManager.getGuildRank(completed);
                int nextReq = GuildRankManager.getNextRankNeeded(completed);
                int[] offers = getOrRollPlayerBounties(sp);

                PacketDistributor.sendToPlayer(sp, new OpenBountyBoardPayload(
                        guildRank, completed, nextReq,
                        true, idx, initialProgress, template.count, offers
                ));
            }
        }
    }
}
