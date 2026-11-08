package com.soybe.awakeningsystem.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Bóveda Rúnica. Cada cazador puede abrirla una sola vez con una Llave de Cámara; el botín sale
 * de la tabla {@code awakening_system:vault/trial_chamber} y se expulsa objeto a objeto.
 */
public class TrialVaultBlockEntity extends BlockEntity {

    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("awakening_system", "vault/trial_chamber"));
    private static final int EJECT_INTERVAL = 8;

    private final Set<UUID> openedBy = new HashSet<>();
    private final Deque<ItemStack> queue = new ArrayDeque<>();
    private int ejectTimer = 0;

    public TrialVaultBlockEntity(BlockPos pos, BlockState state) {
        super(TrialRegistry.VAULT_ENTITY.get(), pos, state);
    }

    public void tryOpen(Player player, ItemStack held) {
        if (!(level instanceof ServerLevel sl)) return;
        if (openedBy.contains(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.trial.vault_already_open"), true);
            sl.playSound(null, worldPosition, SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        if (!held.is(TrialRegistry.TRIAL_KEY.get())) {
            player.displayClientMessage(Component.translatable("awakening_system.trial.vault_need_key"), true);
            sl.playSound(null, worldPosition, SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        if (!player.getAbilities().instabuild) held.shrink(1);
        openedBy.add(player.getUUID());

        LootTable table = sl.getServer().reloadableRegistries().getLootTable(LOOT);
        LootParams params = new LootParams.Builder(sl)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);
        table.getRandomItems(params).forEach(queue::add);

        ejectTimer = 20;
        sl.playSound(null, worldPosition, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        sl.playSound(null, worldPosition, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.trial.vault_opened"), true);
        setChanged();
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, TrialVaultBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) return;
        if (be.queue.isEmpty()) {
            if (level.getGameTime() % 20 == 0) {
                sl.sendParticles(ParticleTypes.SMALL_FLAME, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 1, 0.2D, 0.0D, 0.2D, 0.0D);
            }
            return;
        }
        if (--be.ejectTimer > 0) return;
        be.ejectTimer = EJECT_INTERVAL;
        ItemStack stack = be.queue.poll();
        ItemEntity item = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, stack,
                (sl.random.nextDouble() - 0.5D) * 0.12D, 0.25D, (sl.random.nextDouble() - 0.5D) * 0.12D);
        item.setDefaultPickUpDelay();
        sl.addFreshEntity(item);
        sl.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.9F + sl.random.nextFloat() * 0.2F);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 6, 0.15D, 0.1D, 0.15D, 0.03D);
        if (be.queue.isEmpty()) sl.playSound(null, pos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        openedBy.forEach(id -> list.add(NbtUtils.createUUID(id)));
        tag.put("OpenedBy", list);
        ListTag pending = new ListTag();
        for (ItemStack stack : queue) pending.add(stack.save(registries));
        tag.put("Pending", pending);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        openedBy.clear();
        for (Tag t : tag.getList("OpenedBy", Tag.TAG_INT_ARRAY)) openedBy.add(NbtUtils.loadUUID(t));
        queue.clear();
        for (Tag t : tag.getList("Pending", Tag.TAG_COMPOUND)) {
            ItemStack.parse(registries, t).ifPresent(queue::add);
        }
    }
}
