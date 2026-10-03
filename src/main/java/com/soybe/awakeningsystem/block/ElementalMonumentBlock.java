package com.soybe.awakeningsystem.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import com.soybe.awakeningsystem.block.entity.LockedTreasureChestBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

public class ElementalMonumentBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // 15x30x15: dimensiones auténticas de monumentos de Genshin
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 30.0D, 15.0D);

    public enum MonumentElement {
        ANEMO("anemo", "Anemo", ChatFormatting.AQUA, "Viento"),
        GEO("geo", "Geo", ChatFormatting.GOLD, "Tierra / Roca"),
        ELECTRO("electro", "Electro", ChatFormatting.DARK_PURPLE, "Rayo / Trueno"),
        DENDRO("dendro", "Dendro", ChatFormatting.GREEN, "Naturaleza / Flora"),
        HYDRO("hydro", "Hydro", ChatFormatting.BLUE, "Agua / Hidro"),
        PYRO("pyro", "Pyro", ChatFormatting.RED, "Fuego / Piro"),
        CRYO("cryo", "Cryo", ChatFormatting.LIGHT_PURPLE, "Hielo / Escarcha"),
        BLANK("blank", "Inactivo", ChatFormatting.GRAY, "Vacío");

        private final String id;
        private final String displayName;
        private final ChatFormatting color;
        private final String description;

        MonumentElement(String id, String displayName, ChatFormatting color, String description) {
            this.id = id;
            this.displayName = displayName;
            this.color = color;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public ChatFormatting getColor() { return color; }
        public String getDescription() { return description; }

        public static MonumentElement fromId(String id) {
            for (MonumentElement e : values()) {
                if (e.id.equalsIgnoreCase(id)) return e;
            }
            return BLANK;
        }
    }

    public static final MapCodec<ElementalMonumentBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("element").forGetter(b -> b.element.getId()),
                    propertiesCodec()
            ).apply(instance, (elemId, props) -> new ElementalMonumentBlock(MonumentElement.fromId(elemId), props))
    );

    private final MonumentElement element;

    public ElementalMonumentBlock(MonumentElement element, BlockBehaviour.Properties properties) {
        super(properties);
        this.element = element;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public MonumentElement getElement() {
        return this.element;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public boolean matchesElement(Player player, ItemStack stack) {
        if (this.element == MonumentElement.BLANK) return false;

        // 1. Visión activa seleccionada por el jugador en su interfaz
        SystemData data = SystemEvents.data(player);
        if (data != null && data.hasActiveVision()) {
            String active = data.activeVision();
            if (active.equalsIgnoreCase(this.element.getId())) {
                return true;
            }
        }

        // 2. Ítems, catalizadores y esencias sostenidos en mano
        if (stack != null && !stack.isEmpty()) {
            String name = stack.getItem().toString().toLowerCase(Locale.ROOT);
            String desc = stack.getHoverName().getString().toLowerCase(Locale.ROOT);

            switch (this.element) {
                case PYRO -> {
                    if (stack.is(Items.FIRE_CHARGE) || stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.BLAZE_ROD)
                            || stack.is(Items.BLAZE_POWDER) || stack.is(Items.LAVA_BUCKET) || stack.is(Items.TORCH)
                            || stack.is(Items.CAMPFIRE) || name.contains("pyro") || name.contains("agnidus") || name.contains("fire")
                            || desc.contains("piro") || desc.contains("fuego")) {
                        return true;
                    }
                }
                case HYDRO -> {
                    if (stack.is(Items.WATER_BUCKET) || stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)
                            || stack.is(Items.HEART_OF_THE_SEA) || stack.is(Items.PRISMARINE_SHARD)
                            || name.contains("hydro") || name.contains("varunada") || name.contains("water")
                            || desc.contains("hidro") || desc.contains("agua")) {
                        return true;
                    }
                }
                case CRYO -> {
                    if (stack.is(Items.SNOWBALL) || stack.is(Items.ICE) || stack.is(Items.PACKED_ICE)
                            || stack.is(Items.BLUE_ICE) || stack.is(Items.SNOW_BLOCK) || stack.is(Items.POWDER_SNOW_BUCKET)
                            || name.contains("cryo") || name.contains("shivada") || name.contains("ice") || name.contains("frost")
                            || desc.contains("crio") || desc.contains("hielo") || desc.contains("escarcha")) {
                        return true;
                    }
                }
                case ELECTRO -> {
                    if (stack.is(Items.LIGHTNING_ROD) || stack.is(Items.REDSTONE) || stack.is(Items.REDSTONE_BLOCK)
                            || stack.is(Items.GLOWSTONE) || stack.is(Items.COPPER_INGOT)
                            || name.contains("electro") || name.contains("vajrada") || name.contains("lightning")
                            || desc.contains("electro") || desc.contains("trueno") || desc.contains("rayo")) {
                        return true;
                    }
                }
                case ANEMO -> {
                    if (stack.is(Items.FEATHER) || stack.is(Items.WIND_CHARGE) || stack.is(Items.BREEZE_ROD)
                            || stack.is(Items.ELYTRA)
                            || name.contains("anemo") || name.contains("vayuda") || name.contains("wind") || name.contains("breeze")
                            || desc.contains("anemo") || desc.contains("viento")) {
                        return true;
                    }
                }
                case GEO -> {
                    if (stack.is(Items.RAW_GOLD) || stack.is(Items.GOLD_INGOT) || stack.is(Items.EMERALD)
                            || stack.is(Items.DIAMOND) || stack.is(Items.AMETHYST_SHARD) || stack.is(Items.LODESTONE)
                            || name.contains("geo") || name.contains("prithiva") || name.contains("cor_lapis")
                            || desc.contains("geo") || desc.contains("roca") || desc.contains("tierra")) {
                        return true;
                    }
                }
                case DENDRO -> {
                    if (stack.is(Items.BONE_MEAL) || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MOSS_BLOCK)
                            || stack.is(Items.OAK_SAPLING) || stack.is(Items.FERN) || stack.is(Items.FLOWERING_AZALEA)
                            || name.contains("dendro") || name.contains("nagadus") || name.contains("wood") || name.contains("leaf")
                            || desc.contains("dendro") || desc.contains("flora") || desc.contains("planta")) {
                        return true;
                    }
                }
                default -> {}
            }
        }

        return false;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (this.element == MonumentElement.BLANK) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§7✦ La energía elemental de este monumento ya descansa en paz. ✦"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (matchesElement(player, stack)) {
            triggerActivation(level, pos, state, player);
            return ItemInteractionResult.SUCCESS;
        } else {
            sendMismatchHint(level, player);
            return ItemInteractionResult.SUCCESS;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (this.element == MonumentElement.BLANK) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§7✦ La energía elemental de este monumento ya descansa en paz. ✦"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (matchesElement(player, ItemStack.EMPTY)) {
            triggerActivation(level, pos, state, player);
            return InteractionResult.SUCCESS;
        } else {
            sendMismatchHint(level, player);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if (!level.isClientSide() && this.element != MonumentElement.BLANK) {
            if (matchesElement(player, player.getMainHandItem())) {
                triggerActivation(level, pos, state, player);
            }
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (level.isClientSide() || this.element == MonumentElement.BLANK) return;

        boolean matches = false;
        if (projectile.isOnFire() && this.element == MonumentElement.PYRO) {
            matches = true;
        } else if (projectile instanceof net.minecraft.world.entity.projectile.Snowball && this.element == MonumentElement.CRYO) {
            matches = true;
        } else if ((projectile.getType().toString().contains("wind_charge") || projectile.getType().toString().contains("breeze")) && this.element == MonumentElement.ANEMO) {
            matches = true;
        } else if (projectile.getOwner() instanceof Player player) {
            matches = matchesElement(player, player.getMainHandItem());
        }

        if (matches) {
            Player p = projectile.getOwner() instanceof Player pl ? pl : level.getNearestPlayer(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ(), 32.0D, false);
            if (p != null) {
                triggerActivation(level, hit.getBlockPos(), state, p);
            }
        }
    }

    private void sendMismatchHint(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.8F, 1.2F);
            player.displayClientMessage(Component.literal("§c✦ Requiere poder elemental de " + this.element.getColor() + "§l" + this.element.getDisplayName() + "§r§c (" + this.element.getDescription() + ") ✦"), true);
        }
    }

    public void triggerActivation(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) return;
        ServerLevel sl = (ServerLevel) level;

        // 1. Transformar a BlankMonumentBlock conservando orientación y agua
        Direction facing = state.getValue(FACING);
        boolean waterlogged = state.getValue(WATERLOGGED);
        BlockState blankState = SystemBlocks.BLANK_MONUMENT.get().defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, waterlogged);
        sl.setBlock(pos, blankState, 3);

        // 2. Efectos de sonido mágicos auténticos
        sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0F, 0.9F);
        sl.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.4F);
        sl.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.4F, 1.1F);

        // 3. Estallido de partículas elementales
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.8D;
        double pz = pos.getZ() + 0.5D;

        switch (this.element) {
            case PYRO -> {
                sl.sendParticles(ParticleTypes.FLAME, px, py, pz, 50, 0.4D, 0.6D, 0.4D, 0.08D);
                sl.sendParticles(ParticleTypes.LAVA, px, py, pz, 15, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            case HYDRO -> {
                sl.sendParticles(ParticleTypes.SPLASH, px, py, pz, 60, 0.5D, 0.5D, 0.5D, 0.15D);
                sl.sendParticles(ParticleTypes.BUBBLE, px, py, pz, 30, 0.4D, 0.5D, 0.4D, 0.1D);
            }
            case CRYO -> {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 60, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, px, py, pz, 20, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            case ELECTRO -> {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 70, 0.5D, 0.6D, 0.5D, 0.2D);
                sl.sendParticles(ParticleTypes.WITCH, px, py, pz, 25, 0.4D, 0.4D, 0.4D, 0.05D);
            }
            case ANEMO -> {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 25, 0.4D, 0.3D, 0.4D, 0.1D);
                sl.sendParticles(ParticleTypes.CLOUD, px, py, pz, 40, 0.5D, 0.5D, 0.5D, 0.05D);
            }
            case GEO -> {
                sl.sendParticles(ParticleTypes.CRIT, px, py, pz, 50, 0.5D, 0.5D, 0.5D, 0.15D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py, pz, 30, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            case DENDRO -> {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 40, 0.5D, 0.5D, 0.5D, 0.1D);
                sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, px, py, pz, 45, 0.6D, 0.6D, 0.6D, 0.05D);
            }
            default -> {}
        }

        // 4. Recompensas directas y sincronización al jugador
        if (player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            if (data != null) {
                int primogems = 20 + sl.random.nextInt(16); // 20 a 35 Protogemas
                int mora = 12000 + sl.random.nextInt(15001); // 12,000 a 27,000 Mora
                data.addPrimogems(primogems);
                data.addMora(mora);
                data.addExperience(600);
                AwakeningSystemMod.sync(sp);

                sp.displayClientMessage(Component.literal("§6✦ ¡MONUMENTO " + this.element.getDisplayName().toUpperCase(Locale.ROOT) + " DESPERTADO! ✦"), true);
                sp.sendSystemMessage(Component.literal("§e✦ ¡Has canalizado el poder de " + this.element.getColor() + this.element.getDisplayName() + "§e y resuelto el misterio! Recibes §b+" + primogems + " Protogemas §ey §6+" + String.format("%,d", mora) + " Mora§e. ✦"));
            }
        }

        // 5. Spawnear Cofre del Tesoro desbloqueado frente al monumento
        spawnRewardChest(sl, pos, facing);
    }

    private void spawnRewardChest(ServerLevel sl, BlockPos monumentPos, Direction facing) {
        // Buscar un bloque adyacente despejado para el cofre (preferentemente al frente)
        BlockPos targetPos = monumentPos.relative(facing, 2);
        if (!sl.getBlockState(targetPos).isAir() && !sl.getBlockState(targetPos).canBeReplaced()) {
            targetPos = monumentPos.relative(facing, 1);
            if (!sl.getBlockState(targetPos).isAir() && !sl.getBlockState(targetPos).canBeReplaced()) {
                targetPos = monumentPos.relative(facing.getClockWise(), 1);
            }
        }

        // Asegurar que el cofre no quede flotando en el vacío
        while (targetPos.getY() > sl.getMinBuildHeight() && sl.getBlockState(targetPos.below()).isAir()) {
            targetPos = targetPos.below();
        }

        BlockState chestState = SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState()
                .setValue(LockedTreasureChestBlock.FACING, facing.getOpposite())
                .setValue(LockedTreasureChestBlock.LOCKED, false);

        sl.setBlock(targetPos, chestState, 3);

        // Llenar botín exquisito en el cofre
        BlockEntity be = sl.getBlockEntity(targetPos);
        if (be instanceof LockedTreasureChestBlockEntity chestBe) {
            int slot = 0;
            // Protogemas físicas
            chestBe.setItem(slot++, SystemItems.PRIMOGEM.get().getDefaultInstance().copyWithCount(10 + sl.random.nextInt(11)));
            // Mora
            chestBe.setItem(slot++, SystemItems.MORA_POUCH.get().getDefaultInstance().copyWithCount(1 + sl.random.nextInt(2)));

            // Fragmento / Trozo elemental según el elemento del monumento
            ItemStack gemPiece = switch (this.element) {
                case PYRO -> SystemItems.AGNIDUS_AGATE_FRAGMENT.get().getDefaultInstance();
                case HYDRO -> SystemItems.VARUNADA_LAZURITE_FRAGMENT.get().getDefaultInstance();
                case CRYO -> SystemItems.SHIVADA_JADE_FRAGMENT.get().getDefaultInstance();
                case ELECTRO -> SystemItems.VAJRADA_AMETHYST_FRAGMENT.get().getDefaultInstance();
                case ANEMO -> SystemItems.VAYUDA_TURQUOISE_FRAGMENT.get().getDefaultInstance();
                case GEO -> SystemItems.PRITHIVA_TOPAZ_FRAGMENT.get().getDefaultInstance();
                case DENDRO -> SystemItems.NAGADUS_EMERALD_FRAGMENT.get().getDefaultInstance();
                default -> SystemItems.AGNIDUS_AGATE_SLIVER.get().getDefaultInstance();
            };
            chestBe.setItem(slot++, gemPiece);

            // 35% de probabilidad de Destino Entrelazado (Tirada Gacha)
            if (sl.random.nextFloat() < 0.35F) {
                chestBe.setItem(slot++, SystemItems.INTERTWINED_FATE.get().getDefaultInstance());
            }

            // 20% de probabilidad de Llave del Santuario de Teyvat
            if (sl.random.nextFloat() < 0.20F) {
                chestBe.setItem(slot++, SystemItems.TEYVAT_SHRINE_KEY.get().getDefaultInstance());
            }

            // Artefacto de Genshin aleatorio (Bruja Carmesí o Gladiador)
            ItemStack[] artifacts = new ItemStack[] {
                    SystemItems.ARTIFACT_CRIMSON_WITCH_FLOWER.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_CRIMSON_WITCH_PLUME.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_CRIMSON_WITCH_SANDS.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_CRIMSON_WITCH_GOBLET.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_CRIMSON_WITCH_CIRCLET.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_GLADIATORS_FINALE_FLOWER.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_GLADIATORS_FINALE_PLUME.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_GLADIATORS_FINALE_SANDS.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_GLADIATORS_FINALE_GOBLET.get().getDefaultInstance(),
                    SystemItems.ARTIFACT_GLADIATORS_FINALE_CIRCLET.get().getDefaultInstance()
            };
            ItemStack artifact = artifacts[sl.random.nextInt(artifacts.length)].copy();
            chestBe.setItem(slot++, artifact);
        }

        // Partículas doradas de aparición celestial de cofre
        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, targetPos.getX() + 0.5D, targetPos.getY() + 0.6D, targetPos.getZ() + 0.5D, 35, 0.4D, 0.4D, 0.4D, 0.1D);
        sl.sendParticles(ParticleTypes.WAX_OFF, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 20, 0.3D, 0.3D, 0.3D, 0.08D);
        sl.playSound(null, targetPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0F, 1.2F);
    }
}
