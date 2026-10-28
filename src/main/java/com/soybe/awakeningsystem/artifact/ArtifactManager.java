package com.soybe.awakeningsystem.artifact;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.item.CompanionContractItem;
import com.soybe.awakeningsystem.network.CompanionActionPayload;
import com.soybe.awakeningsystem.network.EquipArtifactPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class ArtifactManager {
    private ArtifactManager() {}

    public static void handleEquipArtifact(ServerPlayer player, EquipArtifactPayload payload) {
        int slot = payload.slot();
        if (slot < 0 || slot >= 5) return;

        if (!payload.isCompanion()) {
            SystemData data = SystemEvents.data(player);
            if (payload.invSlot() >= 0 && payload.invSlot() < player.getInventory().items.size()) {
                ItemStack held = player.getInventory().getItem(payload.invSlot());
                if (!held.isEmpty() && held.getItem() instanceof GenshinArtifactItem art) {
                    if (art.getArtifactType().getSlotIndex() == slot) {
                        ItemStack currentlyEquipped = data.getArtifact(slot);
                        data.setArtifact(slot, held.copy());
                        player.getInventory().setItem(payload.invSlot(), currentlyEquipped);
                        player.playNotifySound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        AwakeningSystemMod.sync(player);
                        player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.equipped", art.getArtifactSet().getColorCode(), art.getArtifactSet().getDisplayNameComponent(), art.getArtifactType().getDisplayNameComponent()), true);
                    } else {
                        player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.wrong_slot", art.getArtifactType().getDisplayNameComponent()), true);
                    }
                }
            } else if (payload.invSlot() == -1) {
                ItemStack currentlyEquipped = data.getArtifact(slot);
                if (!currentlyEquipped.isEmpty()) {
                    data.setArtifact(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(currentlyEquipped)) {
                        player.drop(currentlyEquipped, false);
                    }
                    player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    AwakeningSystemMod.sync(player);
                    player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.unequipped"), true);
                }
            }
        } else {
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof CompanionEntity companion && companion.isOwnedBy(player)) {
                if (payload.invSlot() >= 0 && payload.invSlot() < player.getInventory().items.size()) {
                    ItemStack held = player.getInventory().getItem(payload.invSlot());
                    if (!held.isEmpty() && held.getItem() instanceof GenshinArtifactItem art) {
                        if (art.getArtifactType().getSlotIndex() == slot) {
                            ItemStack currentlyEquipped = companion.getArtifact(slot);
                            companion.setArtifact(slot, held.copy());
                            player.getInventory().setItem(payload.invSlot(), currentlyEquipped);
                            companion.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                            player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.companion_equipped", art.getArtifactSet().getColorCode(), art.getArtifactSet().getDisplayNameComponent(), companion.getCompanionProfile().displayName()), true);
                        } else {
                            player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.wrong_slot", art.getArtifactType().getDisplayNameComponent()), true);
                        }
                    }
                } else if (payload.invSlot() == -1) {
                    ItemStack currentlyEquipped = companion.getArtifact(slot);
                    if (!currentlyEquipped.isEmpty()) {
                        companion.setArtifact(slot, ItemStack.EMPTY);
                        if (!player.getInventory().add(currentlyEquipped)) {
                            player.drop(currentlyEquipped, false);
                        }
                        companion.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                        player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.companion_unequipped", companion.getCompanionProfile().displayName()), true);
                    }
                }
            }
        }
    }

    public static void handleCompanionAction(ServerPlayer player, CompanionActionPayload payload) {
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof CompanionEntity companion) || !companion.isOwnedBy(player)) return;

        switch (payload.actionType()) {
            case 0 -> {
                int rankCap = com.soybe.awakeningsystem.entity.CompanionAscensionManager.getRankMaxLevel(companion.getAscensionRank());
                if (companion.getCompanionLevel() >= rankCap) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.phase_cap_reached", rankCap, companion.getCompanionProfile().displayName()), true);
                    return;
                }

                if (companion.getCompanionLevel() >= 90) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.max_level_reached", companion.getCompanionProfile().displayName()), true);
                    return;
                }

                boolean creative = player.isCreative();
                int costMora = companion.getCompanionLevel() * 1500;
                SystemData data = SystemEvents.data(player);

                if (!creative) {
                    ItemStack bookStack = ItemStack.EMPTY;
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack it = player.getInventory().getItem(i);
                        if (!it.isEmpty() && (it.is(SystemItems.HEROS_WIT.get()) || it.is(SystemItems.ADVENTURERS_EXPERIENCE.get()))) {
                            bookStack = it;
                            break;
                        }
                    }

                    if (!bookStack.isEmpty()) {
                        if (data.mora() < costMora) {
                            player.displayClientMessage(Component.translatable("companion.awakening_system.insufficient_mora_simple", String.format(java.util.Locale.US, "%,d", costMora)), true);
                            return;
                        }
                        data.spendMora(costMora);
                        bookStack.shrink(1);
                    } else if (data.mora() >= costMora * 2) {
                        data.spendMora(costMora * 2);
                    } else if (data.coins() >= 5) {
                        data.spendCoins(5);
                    } else {
                        player.displayClientMessage(Component.translatable("companion.awakening_system.requires_heros_wit_or_mora", String.format(java.util.Locale.US, "%,d", costMora * 2)), true);
                        return;
                    }
                }

                companion.setCompanionLevel(companion.getCompanionLevel() + 1);
                companion.heal(30.0F);

                if (companion.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, companion.getX(), companion.getY() + 1.0D, companion.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, companion.getX(), companion.getY() + 1.2D, companion.getZ(), 20, 0.6D, 0.6D, 0.6D, 0.05D);
                }
                companion.playSound(ModSounds.GENSHIN_LEVELUP.get(), 1.2F, 1.0F);
                AwakeningSystemMod.sync(player);
                String infoCost = creative ? " §7(Modo Creativo)" : "";
                player.displayClientMessage(Component.translatable("companion.awakening_system.level_up_info", companion.getCompanionProfile().displayName(), companion.getCompanionLevel(), infoCost), true);
            }
            case 1 -> {
                boolean nextGuard = !companion.isGuarding();
                companion.setGuarding(nextGuard);
                companion.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.2F);
                if (nextGuard) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.guarding_here", companion.getCompanionProfile().displayName()), true);
                } else {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.following_you", companion.getCompanionProfile().displayName()), true);
                }
            }
            case 2 -> {
                ItemStack contract = CompanionContractItem.createContract(companion.getCompanionProfile(), companion.getCompanionLevel());
                net.minecraft.nbt.CompoundTag contractTag = contract.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                contractTag.putInt("CompanionConstellation", companion.getConstellation());
                contractTag.putInt("TalentNormalAttack", companion.getTalentNormalAttack());
                contractTag.putInt("TalentElementalSkill", companion.getTalentElementalSkill());
                contractTag.putInt("TalentElementalBurst", companion.getTalentElementalBurst());
                contractTag.putInt("CompanionAscensionRank", companion.getAscensionRank());

                net.minecraft.nbt.ListTag artList = new net.minecraft.nbt.ListTag();
                for (int i = 0; i < 5; i++) {
                    ItemStack art = companion.getArtifact(i);
                    if (!art.isEmpty()) {
                        net.minecraft.nbt.CompoundTag slotTag = new net.minecraft.nbt.CompoundTag();
                        slotTag.putByte("Slot", (byte) i);
                        artList.add(art.save(player.registryAccess(), slotTag));
                    }
                }
                contractTag.put("CompanionArtifacts", artList);
                contract.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(contractTag));

                if (!player.getInventory().add(contract)) {
                    player.drop(contract, false);
                }
                companion.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.2F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.saved_to_contract", companion.getCompanionProfile().displayName()), true);
                companion.discard();
            }
        }
    }

    public static void handleUpgradeArtifact(ServerPlayer player, com.soybe.awakeningsystem.network.UpgradeArtifactPayload payload) {
        int slot = payload.slot();
        if (slot < 0 || slot >= 5) return;

        SystemData data = SystemEvents.data(player);
        ItemStack target;
        CompanionEntity companion = null;

        if (payload.isCompanion()) {
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof CompanionEntity comp && comp.isOwnedBy(player)) {
                companion = comp;
                target = companion.getArtifact(slot);
            } else {
                return;
            }
        } else {
            target = data.getArtifact(slot);
        }

        if (target.isEmpty() || !(target.getItem() instanceof GenshinArtifactItem art)) {
            return;
        }

        int curLvl = GenshinArtifactItem.getLevel(target);
        if (curLvl >= 20) {
            player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.max_level"), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }

        int costCoins = (curLvl + 1) * 350;
        boolean usedOre = false;

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty() && (st.is(SystemItems.ENHANCEMENT_ORE.get()) || st.is(SystemItems.FINE_ENHANCEMENT_ORE.get()) || st.is(SystemItems.MYSTIC_ENHANCEMENT_ORE.get()))) {
                st.shrink(1);
                usedOre = true;
                break;
            }
        }

        if (!usedOre) {
            if (data.coins() < costCoins) {
                player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.insufficient_funds", costCoins, curLvl + 1), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
            data.spendCoins(costCoins);
        }

        int newLvl = curLvl + 1;
        GenshinArtifactItem.setLevel(target, newLvl);

        if (!payload.isCompanion()) {
            data.setArtifact(slot, target);
            AwakeningSystemMod.sync(player);
        }

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);
            sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2D, player.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);
        }

        Component costComp = usedOre ? Component.translatable("awakening_system.artifact.msg.cost_ore") : Component.translatable("awakening_system.artifact.msg.cost_coins", costCoins);
        player.displayClientMessage(Component.translatable("awakening_system.artifact.msg.upgraded", target.getHoverName(), newLvl, costComp), true);
    }
}
