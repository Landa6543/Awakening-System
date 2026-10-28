package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Altar de la Diosa de la Luz y Manantial de Maná:
 * Bloque sagrado interactivo ubicado en los Santuarios de la dimensión Isekai.
 * Permite ofrendar gemas y reliquias a cambio de poderosas bendiciones celestiales,
 * o restos de monstruos para desafiar el Juicio Celestial con recompensas arcanas.
 */
public class GoddessAltarBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<GoddessAltarBlock> CODEC = simpleCodec(GoddessAltarBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public static final Map<BlockPos, TrialState> ACTIVE_TRIALS = new ConcurrentHashMap<>();

    public static class TrialState {
        public final UUID playerId;
        public final BlockPos altarPos;
        public int remainingEnemies;
        public int timeoutTicks;

        public TrialState(UUID playerId, BlockPos altarPos, int enemies) {
            this.playerId = playerId;
            this.altarPos = altarPos;
            this.remainingEnemies = enemies;
            this.timeoutTicks = 20 * 90;
        }
    }

    public GoddessAltarBlock(BlockBehaviour.Properties properties) {
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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer sp)) {
            return ItemInteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        SystemData data = SystemEvents.data(sp);

        if (stack.is(Items.NETHER_STAR)) {
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            applySupremeBlessing(serverLevel, pos, sp, data);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.DIAMOND)) {
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            applyManaSurgeBlessing(serverLevel, pos, sp, data);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.EMERALD) || stack.is(Items.GOLD_INGOT) || stack.is(Items.AMETHYST_SHARD)) {
            boolean isEmerald = stack.is(Items.EMERALD);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            applyFortuneBlessing(serverLevel, pos, sp, data, isEmerald);
            return ItemInteractionResult.SUCCESS;
        }

        if (SystemItems.AWAKENING_COIN.isBound() && stack.is(SystemItems.AWAKENING_COIN.get())) {
            if (stack.getCount() >= 64 || data.coins() >= 100) {
                if (stack.getCount() >= 64 && !sp.getAbilities().instabuild) {
                    stack.shrink(64);
                } else if (data.coins() >= 100) {
                    data.addCoins(-100);
                }
                int roll = serverLevel.random.nextInt(3);
                if (roll == 0) {
                    applyManaSurgeBlessing(serverLevel, pos, sp, data);
                } else if (roll == 1) {
                    applyFortuneBlessing(serverLevel, pos, sp, data, true);
                } else {
                    applyAegisBlessing(serverLevel, pos, sp, data);
                }
                return ItemInteractionResult.SUCCESS;
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.need_coins"), true);
                return ItemInteractionResult.FAIL;
            }
        }

        if (isMonsterTrophy(stack)) {
            if (ACTIVE_TRIALS.containsKey(pos)) {
                sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.trial_active").withStyle(ChatFormatting.RED), true);
                serverLevel.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 0.8F);
                return ItemInteractionResult.FAIL;
            }

            if (!sp.getAbilities().instabuild) stack.shrink(1);
            startCelestialTrial(serverLevel, pos, sp);
            return ItemInteractionResult.SUCCESS;
        }

        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.hint").withStyle(ChatFormatting.AQUA), true);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.3F);
        return ItemInteractionResult.SUCCESS;
    }

    private static boolean isMonsterTrophy(ItemStack stack) {
        return stack.is(Items.SLIME_BALL)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.BONE)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.GUNPOWDER)
                || stack.is(Items.MAGMA_CREAM);
    }

    private static void applySupremeBlessing(ServerLevel level, BlockPos pos, ServerPlayer sp, SystemData data) {
        data.restoreEnergy(data.maxEnergy());
        data.addCoins(1000);
        sp.heal(40.0F);

        sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 3));
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));
        sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12000, 1));

        triggerSacredEffects(level, pos, sp);
        sp.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.blessing_supreme").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.blessing_supreme_toast"), true);
    }

    private static void applyManaSurgeBlessing(ServerLevel level, BlockPos pos, ServerPlayer sp, SystemData data) {
        data.restoreEnergy(data.maxEnergy());
        sp.heal(20.0F);

        sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12000, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12000, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 0));

        triggerSacredEffects(level, pos, sp);
        sp.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.blessing_mana").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.blessing_mana_toast"), true);
    }

    private static void applyFortuneBlessing(ServerLevel level, BlockPos pos, ServerPlayer sp, SystemData data, boolean isEmerald) {
        data.restoreEnergy(50);
        int coins = isEmerald ? 450 : 300;
        data.addCoins(coins);

        sp.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 2));
        sp.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 6000, 1));

        triggerSacredEffects(level, pos, sp);
        sp.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.blessing_fortune", coins).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.blessing_fortune_toast", coins), true);
    }

    private static void applyAegisBlessing(ServerLevel level, BlockPos pos, ServerPlayer sp, SystemData data) {
        data.restoreEnergy(40);
        sp.heal(15.0F);

        sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));

        triggerSacredEffects(level, pos, sp);
        sp.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.blessing_aegis").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.blessing_aegis_toast"), true);
    }

    private static void triggerSacredEffects(ServerLevel level, BlockPos pos, ServerPlayer sp) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.2D;
        double pz = pos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.FLASH, px, py, pz, 4, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 60, 1.2D, 1.0D, 1.2D, 0.08D);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 0.5D, pz, 40, 1.0D, 1.2D, 1.0D, 0.25D);

        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);
        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.6F);
    }

    private static void startCelestialTrial(ServerLevel level, BlockPos pos, ServerPlayer sp) {
        int enemyCount = 4;
        ACTIVE_TRIALS.put(pos, new TrialState(sp.getUUID(), pos, enemyCount));

        level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 3, 0.4D, 0.4D, 0.4D, 0.05D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 50, 2.0D, 0.5D, 2.0D, 0.05D);
        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.8F, 1.5F);
        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.7F, 1.4F);

        sp.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.trial_started").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        sp.displayClientMessage(Component.translatable("awakening_system.goddess_altar.trial_started_toast"), true);

        int[][] spawnOffsets = {{4, 0}, {-4, 0}, {0, 4}, {0, -4}};
        for (int[] off : spawnOffsets) {
            BlockPos spawnPos = pos.offset(off[0], 1, off[1]);
            IsekaiSlimeEntity guardian = new IsekaiSlimeEntity(ModEntities.ISEKAI_SLIME.get(), level);
            guardian.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
            guardian.setCustomName(Component.translatable("awakening_system.altar.celestial_slime_guardian"));
            guardian.setCustomNameVisible(true);
            guardian.setTarget(sp);
            guardian.addTag("goddess_trial_target");
            guardian.addTag("altar_" + pos.asLong());
            level.addFreshEntity(guardian);

            level.sendParticles(ParticleTypes.PORTAL, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, 20, 0.4D, 0.4D, 0.4D, 0.05D);
        }
    }

    public static void checkTrialProgress(ServerLevel level, BlockPos altarPos, ServerPlayer killer) {
        TrialState state = ACTIVE_TRIALS.get(altarPos);
        if (state == null) return;

        state.remainingEnemies--;
        if (state.remainingEnemies <= 0) {
            ACTIVE_TRIALS.remove(altarPos);
            completeCelestialTrial(level, altarPos, killer);
        } else {
            killer.displayClientMessage(Component.translatable("awakening_system.goddess_altar.trial_progress", state.remainingEnemies), true);
        }
    }

    private static void completeCelestialTrial(ServerLevel level, BlockPos altarPos, ServerPlayer player) {
        double px = altarPos.getX() + 0.5D;
        double py = altarPos.getY() + 1.2D;
        double pz = altarPos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.FLASH, px, py, pz, 6, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 0.8D, pz, 80, 1.5D, 1.5D, 1.5D, 0.3D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py + 0.5D, pz, 40, 1.5D, 1.0D, 1.5D, 0.1D);

        level.playSound(null, altarPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.4F, 1.0F);
        level.playSound(null, altarPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.translatable("awakening_system.goddess_altar.trial_cleared").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.displayClientMessage(Component.translatable("awakening_system.goddess_altar.trial_cleared_toast"), true);

        SystemData data = SystemEvents.data(player);
        data.addCoins(750);
        data.addExperience(1500);

        BlockPos chestPos = altarPos.relative(level.getBlockState(altarPos).getValue(FACING));
        if (level.isEmptyBlock(chestPos) || level.getBlockState(chestPos).canBeReplaced()) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chestBE) {
                chestBE.setItem(0, new ItemStack(Items.DIAMOND, 3 + level.random.nextInt(3)));
                chestBE.setItem(1, new ItemStack(Items.EMERALD, 6 + level.random.nextInt(6)));
                chestBE.setItem(2, new ItemStack(Items.GOLDEN_APPLE, 2 + level.random.nextInt(2)));
                chestBE.setItem(3, new ItemStack(Items.AMETHYST_SHARD, 4 + level.random.nextInt(5)));
                chestBE.setItem(4, new ItemStack(Items.PRISMARINE_CRYSTALS, 8));
                if (SystemItems.AWAKENING_COIN.isBound()) {
                    chestBE.setItem(5, new ItemStack(SystemItems.AWAKENING_COIN.get(), 64));
                }
                if (SystemItems.BOTTLED_MANA_PIXIE.isBound()) {
                    chestBE.setItem(6, new ItemStack(SystemItems.BOTTLED_MANA_PIXIE.get(), 1));
                }
            }
        } else {
            Containers.dropItemStack(level, px, py + 0.5D, pz, new ItemStack(Items.DIAMOND, 4));
            Containers.dropItemStack(level, px, py + 0.5D, pz, new ItemStack(Items.GOLDEN_APPLE, 2));
            Containers.dropItemStack(level, px, py + 0.5D, pz, new ItemStack(Items.EMERALD, 8));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.05D;
        double pz = pos.getZ() + 0.5D;

        if (random.nextFloat() < 0.65F) {
            double rx = px + (random.nextDouble() - 0.5D) * 0.55D;
            double rz = pz + (random.nextDouble() - 0.5D) * 0.55D;
            level.addParticle(ParticleTypes.END_ROD, rx, py, rz, 0.0D, 0.03D, 0.0D);
        }
        if (random.nextFloat() < 0.40F) {
            double rx = px + (random.nextDouble() - 0.5D) * 0.75D;
            double rz = pz + (random.nextDouble() - 0.5D) * 0.75D;
            level.addParticle(ParticleTypes.ENCHANT, rx, py + 0.4D, rz, (random.nextDouble() - 0.5D) * 0.05D, 0.05D, (random.nextDouble() - 0.5D) * 0.05D);
            level.addParticle(ParticleTypes.GLOW, rx, py + 0.1D, rz, 0.0D, 0.02D, 0.0D);
        }
    }
}
