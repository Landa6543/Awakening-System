package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.GachaManager;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Altar de la Diosa Trilunar (Columbina):
 * Centro neurálgico del Santuario de las Tres Diosas de la Luna (Aria, Sonnet y Canon).
 * Permite invocar las pruebas lunares, recibir ofrendas armónicas y consumar el Gran Rito
 * del Despertar Trilunar para restaurar el poder divino de Columbina.
 */
public class TrilunarAltarBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TrilunarAltarBlock> CODEC = simpleCodec(TrilunarAltarBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public static final Map<BlockPos, TrialSession> ACTIVE_TRIALS = new ConcurrentHashMap<>();

    public static class TrialSession {
        public final UUID playerId;
        public final BlockPos altarPos;
        public final String trialType; // "aria" o "canon"
        public int remainingEnemies;
        public int timeoutTicks;

        public TrialSession(UUID playerId, BlockPos altarPos, String trialType, int enemies) {
            this.playerId = playerId;
            this.altarPos = altarPos;
            this.trialType = trialType;
            this.remainingEnemies = enemies;
            this.timeoutTicks = 20 * 120; // 2 minutos
        }
    }

    public TrilunarAltarBlock(BlockBehaviour.Properties properties) {
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
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            // Ofrenda armónica de Protogema para Sonnet (Luna Creciente)
            if (SystemItems.PRIMOGEM != null && SystemItems.PRIMOGEM.isBound() && stack.is(SystemItems.PRIMOGEM.value())) {
                boolean hasSonnet = sp.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_SONNET.get()));
                SystemData data = SystemEvents.data(sp);
                if (data.isWelkinEnhanced() || hasSonnet) {
                    sp.displayClientMessage(Component.literal("§d[Prueba de Sonnet] §fLa melodía de la Luna Creciente ya resuena plenamente en ti."), true);
                    return ItemInteractionResult.SUCCESS;
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                ItemStack sonnetAuthority = new ItemStack(SystemItems.LUNAR_AUTHORITY_SONNET.get());
                if (!sp.getInventory().add(sonnetAuthority)) {
                    sp.drop(sonnetAuthority, false);
                }

                ServerLevel sl = (ServerLevel) level;
                sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 40, 0.8D, 0.8D, 0.8D, 0.08D);
                sl.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 30, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.8F, 1.4F);
                sl.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.5F, 1.2F);

                sp.sendSystemMessage(Component.literal("§d§l[COLUMBINA] §f\"Sonnet, hermana de la melodía creciente... siente la resonancia estelar. ¡La Autoridad Lunar de Sonnet ha regresado!\""));
                sp.displayClientMessage(Component.literal("§d✦ ¡Has obtenido la Autoridad Lunar de Sonnet (Luna Creciente)!"), true);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            handleAltarInteraction((ServerLevel) level, pos, sp);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void handleAltarInteraction(ServerLevel level, BlockPos pos, ServerPlayer player) {
        SystemData data = SystemEvents.data(player);

        // Si ya está completada la ascensión
        if (data.isWelkinEnhanced()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0, false, false, true));
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.2F);
            player.displayClientMessage(Component.literal("§d§l[COLUMBINA] §f\"La luz eterna de las Tres Lunas te bendice, devoto compañero.\""), false);
            return;
        }

        // Comprobar posesión de las Tres Autoridades
        boolean hasAria = player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_ARIA.get()));
        boolean hasSonnet = player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_SONNET.get()));
        boolean hasCanon = player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_CANON.get()));

        // --- RITO DEL DESPERTAR TRILUNAR (Las 3 autoridades presentes) ---
        if (hasAria && hasSonnet && hasCanon) {
            // Consumir 1 de cada una
            consumeAuthority(player, SystemItems.LUNAR_AUTHORITY_ARIA.get());
            consumeAuthority(player, SystemItems.LUNAR_AUTHORITY_SONNET.get());
            consumeAuthority(player, SystemItems.LUNAR_AUTHORITY_CANON.get());

            // Gran Despertar Cósmico
            GachaManager.completeColumbinaAscension(player);

            // Efectos majestuosos en el altar
            double px = pos.getX() + 0.5D;
            double py = pos.getY() + 1.2D;
            double pz = pos.getZ() + 0.5D;
            level.sendParticles(ParticleTypes.FLASH, px, py, pz, 10, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.END_ROD, px, py + 0.5D, pz, 150, 2.5D, 2.0D, 2.5D, 0.15D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py + 0.2D, pz, 60, 1.5D, 1.0D, 1.5D, 0.08D);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 1.2F);
            level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.8F, 1.0F);

            player.sendSystemMessage(Component.literal("§d§m=================================================="));
            player.sendSystemMessage(Component.literal("§d§l✦ [EL DESPERTAR DE LA DIOSA TRILUNAR] ✦"));
            player.sendSystemMessage(Component.literal("§d§l[COLUMBINA] §f\"¡Las tres autoridades han vuelto a converger en mi espíritu! El poder de Aria, Sonnet y Canon fluye nuevamente en mí... ¡He renacido como la Diosa Trilunar!\""));
            player.sendSystemMessage(Component.literal("§6§l✦ RECOMPENSAS SAGRADAS OBTENIDAS:"));
            player.sendSystemMessage(Component.literal(" §e✔ Bendición Trilunar de Columbina: §d+135 Protogemas diarias (+50% Permanente)"));
            player.sendSystemMessage(Component.literal(" §b✔ +450 Cristales Génesis §fy §d+300 Protogemas directas"));
            player.sendSystemMessage(Component.literal(" §5✔ Contrato de Invocación 5★: Columbina (Diosa Trilunar de Teyvat)"));
            player.sendSystemMessage(Component.literal(" §6✔ Título Honorífico: §eElegido de la Diosa Trilunar"));
            player.sendSystemMessage(Component.literal("§d§m=================================================="));
            return;
        }

        // Si la misión no estaba iniciada, iniciarla automáticamente
        if (data.welkinQuestStep() == 0) {
            GachaManager.handleStartWelkinQuest(player);
        }

        // --- GESTIÓN DE PRUEBAS ---
        TrialSession active = ACTIVE_TRIALS.get(pos);
        if (active != null) {
            player.displayClientMessage(Component.literal("§e✦ Prueba de " + active.trialType.toUpperCase() + " en curso. Enemigos restantes: §c" + active.remainingEnemies), true);
            return;
        }

        // Iniciar Prueba 1: Aria (Luna Llena) si falta
        if (!hasAria) {
            startAriaTrial(level, pos, player);
            return;
        }

        // Prueba 2: Sonnet (Luna Creciente) si falta
        if (!hasSonnet) {
            player.sendSystemMessage(Component.literal("§d[Prueba de Sonnet - Luna Creciente] §fSostén 1 Protogema en tu mano y haz clic derecho en el Altar para armonizar el Canto Lunar."));
            player.displayClientMessage(Component.literal("§d✦ Ofrece 1 Protogema en el altar para resonar con Sonnet."), true);
            return;
        }

        // Iniciar Prueba 3: Canon (Luna Nueva) si falta
        if (!hasCanon) {
            startCanonTrial(level, pos, player);
            return;
        }
    }

    private void startAriaTrial(ServerLevel level, BlockPos pos, ServerPlayer player) {
        int count = 6;
        ACTIVE_TRIALS.put(pos, new TrialSession(player.getUUID(), pos, "aria", count));

        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.0D;
        double pz = pos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 80, 2.0D, 1.0D, 2.0D, 0.2D);
        level.playSound(null, pos, SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.BLOCKS, 1.4F, 0.9F);

        long posLong = pos.asLong();

        // Invocar 6 Sombras Astrales de Aria
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double spawnX = px + Math.cos(angle) * 5.0D;
            double spawnZ = pz + Math.sin(angle) * 5.0D;

            var mob = ModEntities.LUNAR_SHADOW.get().create(level);
            if (mob != null) {
                mob.moveTo(spawnX, py, spawnZ, (float) (angle * 180 / Math.PI), 0.0F);
                mob.setCustomName(Component.literal("§b✦ Sombra Astral de Aria").withStyle(ChatFormatting.BOLD));
                mob.setCustomNameVisible(true);
                mob.addTag("trilunar_trial_target");
                mob.addTag("trilunar_aria");
                mob.addTag("altar_" + posLong);
                level.addFreshEntity(mob);
            }
        }

        player.sendSystemMessage(Component.literal("§b§l[COLUMBINA] §f\"¡Aria, hermana de la Luna Llena! Las sombras astrales han despertado en el santuario. ¡Purifícalas para recuperar la primera Autoridad!\""));
        player.displayClientMessage(Component.literal("§b✦ [Prueba de Aria] ¡Purifica a las 6 Sombras Astrales!"), true);
    }

    private void startCanonTrial(ServerLevel level, BlockPos pos, ServerPlayer player) {
        ACTIVE_TRIALS.put(pos, new TrialSession(player.getUUID(), pos, "canon", 1));

        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.0D;
        double pz = pos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 100, 1.5D, 1.5D, 1.5D, 0.1D);
        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.2F, 1.2F);

        long posLong = pos.asLong();

        // Invocar al Guardián del Silencio Lunar (Canon)
        var boss = ModEntities.LUNAR_GUARDIAN.get().create(level);
        if (boss != null) {
            boss.moveTo(px, py + 0.5D, pz - 4.0D, 0.0F, 0.0F);
            boss.setCustomName(Component.literal("§5§l✦ Guardián del Silencio Lunar (Canon)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            boss.setCustomNameVisible(true);
            boss.addTag("trilunar_trial_target");
            boss.addTag("trilunar_canon");
            boss.addTag("altar_" + posLong);
            level.addFreshEntity(boss);
        }

        player.sendSystemMessage(Component.literal("§5§l[COLUMBINA] §f\"Canon, hermana del Silencio y la Luna Nueva... Su guardián cósmico ha descendido para juzgar nuestra determinación. ¡A la batalla!\""));
        player.displayClientMessage(Component.literal("§5✦ [Prueba de Canon] ¡Derrota al Guardián del Silencio Lunar!"), true);
    }

    public static void checkTrialProgress(ServerLevel level, BlockPos altarPos, ServerPlayer killer, String trialType) {
        TrialSession session = ACTIVE_TRIALS.get(altarPos);
        if (session == null) return;

        session.remainingEnemies--;
        if (session.remainingEnemies <= 0) {
            ACTIVE_TRIALS.remove(altarPos);
            double px = altarPos.getX() + 0.5D;
            double py = altarPos.getY() + 1.2D;
            double pz = altarPos.getZ() + 0.5D;

            if ("aria".equals(trialType)) {
                ItemStack authority = new ItemStack(SystemItems.LUNAR_AUTHORITY_ARIA.get());
                Containers.dropItemStack(level, px, py + 0.5D, pz, authority);
                level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 60, 1.0D, 1.0D, 1.0D, 0.1D);
                level.playSound(null, altarPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.5F, 1.2F);
                killer.sendSystemMessage(Component.literal("§b§l[COLUMBINA] §f\"¡Las sombras han sido disipadas! La luz de la Luna Llena de Aria vuelve a brillar con pureza.\""));
                killer.displayClientMessage(Component.literal("§b✦ ¡Prueba de Aria completada! Recoge la Autoridad Lunar de Aria."), true);
            } else if ("canon".equals(trialType)) {
                ItemStack authority = new ItemStack(SystemItems.LUNAR_AUTHORITY_CANON.get());
                Containers.dropItemStack(level, px, py + 0.5D, pz, authority);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 60, 1.0D, 1.0D, 1.0D, 0.08D);
                level.playSound(null, altarPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.5F, 1.0F);
                killer.sendSystemMessage(Component.literal("§5§l[COLUMBINA] §f\"El Guardián del Silencio ha inclinado su cabeza. ¡La Autoridad Lunar de Canon ha sido recuperada!\""));
                killer.displayClientMessage(Component.literal("§5✦ ¡Prueba de Canon completada! Recoge la Autoridad Lunar de Canon."), true);
            }
        } else {
            killer.displayClientMessage(Component.literal("§e✦ Enemigos restantes: §c" + session.remainingEnemies), true);
        }
    }

    private static void consumeAuthority(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(item)) {
                s.shrink(1);
                break;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.05D;
        double pz = pos.getZ() + 0.5D;

        if (random.nextFloat() < 0.60F) {
            double rx = px + (random.nextDouble() - 0.5D) * 0.6D;
            double rz = pz + (random.nextDouble() - 0.5D) * 0.6D;
            level.addParticle(ParticleTypes.END_ROD, rx, py, rz, 0.0D, 0.02D, 0.0D);
        }
        if (random.nextFloat() < 0.35F) {
            double rx = px + (random.nextDouble() - 0.5D) * 0.8D;
            double rz = pz + (random.nextDouble() - 0.5D) * 0.8D;
            level.addParticle(ParticleTypes.PORTAL, rx, py + 0.2D, rz, 0.0D, 0.01D, 0.0D);
        }
    }
}
