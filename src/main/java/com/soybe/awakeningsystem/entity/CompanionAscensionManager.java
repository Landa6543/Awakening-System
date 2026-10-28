package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.item.CompanionContractItem;
import com.soybe.awakeningsystem.network.CompanionAscensionActionPayload;
import com.soybe.awakeningsystem.network.OpenCompanionAscensionPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class CompanionAscensionManager {
    private CompanionAscensionManager() {}

    public static void openTableForPlayer(ServerPlayer player) {
        openTableForPlayer(player, -1, -1);
    }

    public static void openTableForPlayer(ServerPlayer player, int preferEntityId, int preferSlot) {
        SystemData data = SystemEvents.data(player);
        List<CompanionEntity> nearbyCompanions = player.level().getEntitiesOfClass(
                CompanionEntity.class,
                new AABB(player.blockPosition()).inflate(32.0D),
                c -> c.isOwnedBy(player)
        );

        List<Integer> contractSlots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CompanionContractItem) {
                contractSlots.add(i);
            }
        }

        CompoundTag payloadTag = new CompoundTag();
        payloadTag.putInt("PlayerLevel", data.level());
        payloadTag.putLong("PlayerMora", data.mora());
        payloadTag.putInt("PlayerExp", player.totalExperience);

        ListTag compList = new ListTag();

        for (CompanionEntity comp : nearbyCompanions) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("EntityId", comp.getId());
            entry.putInt("ContractSlot", -1);
            entry.putString("ProfileId", comp.getCompanionProfile().id());
            entry.putString("DisplayName", comp.getCompanionProfile().displayName());
            entry.putString("ColorCode", comp.getCompanionProfile().colorCode());
            entry.putString("Element", comp.getCompanionProfile().element());
            entry.putInt("Stars", comp.getCompanionProfile().stars());
            entry.putInt("Level", comp.getCompanionLevel());
            entry.putInt("AscensionRank", comp.getAscensionRank());
            entry.putInt("Constellation", comp.getConstellation());
            entry.putInt("TalentAttack", comp.getTalentNormalAttack());
            entry.putInt("TalentSkill", comp.getTalentElementalSkill());
            entry.putInt("TalentBurst", comp.getTalentElementalBurst());
            entry.putFloat("Health", comp.getHealth());
            entry.putFloat("MaxHealth", comp.getMaxHealth());
            double atk = comp.getAttribute(Attributes.ATTACK_DAMAGE) != null ? comp.getAttribute(Attributes.ATTACK_DAMAGE).getValue() : 20.0D;
            entry.putDouble("AttackDamage", atk);
            compList.add(entry);
        }

        for (int slot : contractSlots) {
            ItemStack stack = player.getInventory().getItem(slot);
            CompanionProfile profile = CompanionContractItem.getProfile(stack);
            CompoundTag entry = new CompoundTag();
            entry.putInt("EntityId", -1);
            entry.putInt("ContractSlot", slot);
            entry.putString("ProfileId", profile.id());
            entry.putString("DisplayName", profile.displayName());
            entry.putString("ColorCode", profile.colorCode());
            entry.putString("Element", profile.element());
            entry.putInt("Stars", profile.stars());
            entry.putInt("Level", CompanionContractItem.getLevel(stack));
            entry.putInt("AscensionRank", CompanionContractItem.getAscensionRank(stack));
            entry.putInt("Constellation", CompanionContractItem.getConstellation(stack));
            entry.putInt("TalentAttack", CompanionContractItem.getTalent(stack, "TalentNormalAttack", 1));
            entry.putInt("TalentSkill", CompanionContractItem.getTalent(stack, "TalentElementalSkill", 1));
            entry.putInt("TalentBurst", CompanionContractItem.getTalent(stack, "TalentElementalBurst", 1));
            entry.putFloat("Health", 300.0F);
            entry.putFloat("MaxHealth", 300.0F);
            entry.putDouble("AttackDamage", 25.0D);
            compList.add(entry);
        }

        payloadTag.put("Companions", compList);

        int selectedIndex = 0;
        if (preferEntityId >= 0) {
            for (int i = 0; i < compList.size(); i++) {
                if (compList.getCompound(i).getInt("EntityId") == preferEntityId) {
                    selectedIndex = i;
                    break;
                }
            }
        } else if (preferSlot >= 0) {
            for (int i = 0; i < compList.size(); i++) {
                if (compList.getCompound(i).getInt("ContractSlot") == preferSlot) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        payloadTag.putInt("SelectedIndex", selectedIndex);

        PacketDistributor.sendToPlayer(player, new OpenCompanionAscensionPayload(payloadTag));
    }

    public static void handleAction(ServerPlayer player, CompanionAscensionActionPayload payload) {
        SystemData data = SystemEvents.data(player);
        CompanionEntity entity = null;
        ItemStack contract = ItemStack.EMPTY;
        int slot = payload.contractSlot();

        if (payload.entityId() >= 0) {
            var e = player.level().getEntity(payload.entityId());
            if (e instanceof CompanionEntity comp && comp.isOwnedBy(player)) {
                entity = comp;
            }
        } else if (slot >= 0 && slot < player.getInventory().items.size()) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof CompanionContractItem) {
                contract = stack;
            }
        }

        if (entity == null && contract.isEmpty()) {
            player.displayClientMessage(Component.translatable("companion.awakening_system.not_found"), true);
            return;
        }

        int level = entity != null ? entity.getCompanionLevel() : CompanionContractItem.getLevel(contract);
        int rank = entity != null ? entity.getAscensionRank() : CompanionContractItem.getAscensionRank(contract);
        int constel = entity != null ? entity.getConstellation() : CompanionContractItem.getConstellation(contract);
        int tAttack = entity != null ? entity.getTalentNormalAttack() : CompanionContractItem.getTalent(contract, "TalentNormalAttack", 1);
        int tSkill = entity != null ? entity.getTalentElementalSkill() : CompanionContractItem.getTalent(contract, "TalentElementalSkill", 1);
        int tBurst = entity != null ? entity.getTalentElementalBurst() : CompanionContractItem.getTalent(contract, "TalentElementalBurst", 1);
        String name = entity != null ? entity.getCompanionProfile().displayName() : CompanionContractItem.getProfile(contract).displayName();

        int maxPlayerLevel = Math.max(1, data.level());

        switch (payload.actionType()) {
            case 0 -> {
                if (level >= maxPlayerLevel) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.hunter_cap", name, maxPlayerLevel), true);
                    return;
                }
                int rankCap = getRankMaxLevel(rank);
                if (level >= rankCap) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.ascend_prompt", rankCap), true);
                    return;
                }

                int expCost = 100 + (level - 1) * 50;
                int moraCost = level * 1000;

                if (data.mora() < moraCost) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.insufficient_mora_simple", String.format(java.util.Locale.US, "%,d", moraCost)), true);
                    return;
                }

                boolean paidExp = false;
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (!it.isEmpty() && (it.is(SystemItems.HEROS_WIT.get()) || it.is(SystemItems.ADVENTURERS_EXPERIENCE.get()))) {
                        it.shrink(1);
                        paidExp = true;
                        break;
                    }
                }
                if (!paidExp) {
                    if (player.totalExperience >= expCost) {
                        player.giveExperiencePoints(-expCost);
                        paidExp = true;
                    } else if (data.experience() >= expCost) {
                        paidExp = true;
                    }
                }

                if (!paidExp) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.requires_heros_wit_or_exp", expCost), true);
                    return;
                }

                data.spendMora(moraCost);
                int nextLvl = level + 1;
                applyLevel(entity, contract, nextLvl);

                if (entity != null && entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                    entity.playSound(ModSounds.GENSHIN_LEVELUP.get(), 1.0F, 1.0F);
                }
                player.playSound(ModSounds.GENSHIN_LEVELUP.get(), 1.0F, 1.0F);
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);

                player.displayClientMessage(Component.translatable("companion.awakening_system.level_up_success", name, nextLvl, String.format(java.util.Locale.US, "%,d", moraCost)), true);
            }
            case 1 -> {
                int upgrades = 0;
                int rankCap = getRankMaxLevel(rank);
                int targetMax = Math.min(maxPlayerLevel, rankCap);

                while (level < targetMax && upgrades < 10) {
                    int expCost = 100 + (level - 1) * 50;
                    int moraCost = level * 1000;
                    if (data.mora() < moraCost) break;

                    boolean paidExp = false;
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack it = player.getInventory().getItem(i);
                        if (!it.isEmpty() && (it.is(SystemItems.HEROS_WIT.get()) || it.is(SystemItems.ADVENTURERS_EXPERIENCE.get()))) {
                            it.shrink(1);
                            paidExp = true;
                            break;
                        }
                    }
                    if (!paidExp && player.totalExperience >= expCost) {
                        player.giveExperiencePoints(-expCost);
                        paidExp = true;
                    }
                    if (!paidExp) break;

                    data.spendMora(moraCost);
                    level++;
                    upgrades++;
                }

                if (upgrades > 0) {
                    applyLevel(entity, contract, level);
                    if (entity != null) entity.playSound(ModSounds.GENSHIN_LEVELUP.get(), 1.0F, 1.0F);
                    player.playSound(ModSounds.GENSHIN_LEVELUP.get(), 1.0F, 1.0F);
                    player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.1F);
                    player.displayClientMessage(Component.translatable("companion.awakening_system.quick_level_up", name, upgrades, level), true);
                } else {
                    if (level >= rankCap) {
                        player.displayClientMessage(Component.translatable("companion.awakening_system.phase_materials_prompt", rankCap), true);
                    } else {
                        player.displayClientMessage(Component.translatable("companion.awakening_system.insufficient_or_max"), true);
                    }
                }
            }
            case 2 -> {
                int rankCap = getRankMaxLevel(rank);
                if (level < rankCap) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.requires_level_for_phase", rankCap), true);
                    return;
                }
                CompanionProfile prof = entity != null ? entity.getCompanionProfile() : CompanionContractItem.getProfile(contract);
                AscensionRequirement req = getAscensionRequirement(prof, rank);

                boolean isCreative = player.isCreative();
                if (!isCreative) {
                    if (data.mora() < req.moraCost()) {
                        player.displayClientMessage(Component.translatable("companion.awakening_system.insufficient_mora_simple", String.format(java.util.Locale.US, "%,d", req.moraCost())), true);
                        return;
                    }
                    int hasGem = countItem(player, req.primaryGem());
                    if (hasGem < req.gemCount()) {
                        player.displayClientMessage(Component.translatable("companion.awakening_system.requires_gem", req.gemCount(), req.primaryGem().getName(ItemStack.EMPTY).getString(), hasGem, req.gemCount()), true);
                        return;
                    }
                    if (req.bossDropCount() > 0 && !req.bossDrops().isEmpty()) {
                        int hasBoss = countAnyItem(player, req.bossDrops());
                        if (hasBoss < req.bossDropCount()) {
                            String bossName = req.bossDrops().get(0).getName(ItemStack.EMPTY).getString();
                            player.displayClientMessage(Component.translatable("companion.awakening_system.requires_boss_drop", req.bossDropCount(), bossName, hasBoss, req.bossDropCount()), true);
                            return;
                        }
                    }
                    int hasCommon = countAnyItem(player, req.commonMaterials());
                    if (hasCommon < req.commonCount()) {
                        String commonName = req.commonMaterials().get(0).getName(ItemStack.EMPTY).getString();
                        player.displayClientMessage(Component.translatable("companion.awakening_system.requires_common_drop", req.commonCount(), commonName, hasCommon, req.commonCount()), true);
                        return;
                    }

                    data.spendMora(req.moraCost());
                    shrinkItem(player, req.primaryGem(), req.gemCount());
                    if (req.bossDropCount() > 0 && !req.bossDrops().isEmpty()) {
                        shrinkAnyItem(player, req.bossDrops(), req.bossDropCount());
                    }
                    shrinkAnyItem(player, req.commonMaterials(), req.commonCount());
                }

                int nextRank = rank + 1;
                applyAscensionRank(entity, contract, nextRank);

                if (entity != null && entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 40, 0.6D, 1.0D, 0.6D, 0.1D);
                    entity.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F);
                }
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F);

                player.displayClientMessage(Component.translatable("companion.awakening_system.celestial_phase_ascended", nextRank, name), true);
            }
            case 3 -> {
                int maxTalent = 1 + rank * 2;
                if (tAttack >= maxTalent) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_phase_limited"), true);
                    return;
                }
                int costMora = tAttack * 8000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_requires_mora", String.format(java.util.Locale.US, "%,d", costMora)), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentNormalAttack", tAttack + 1);
                player.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.4F);
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.4F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.normal_attack_upgraded", (tAttack + 1)), true);
            }
            case 4 -> {
                int maxTalent = 1 + rank * 2;
                if (tSkill >= maxTalent) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_phase_limited"), true);
                    return;
                }
                int costMora = tSkill * 8000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_requires_mora", String.format(java.util.Locale.US, "%,d", costMora)), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentElementalSkill", tSkill + 1);
                player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.3F);
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.5F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.elemental_skill_upgraded", (tSkill + 1)), true);
            }
            case 5 -> {
                int maxTalent = 1 + rank * 2;
                if (tBurst >= maxTalent) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_phase_limited"), true);
                    return;
                }
                int costMora = tBurst * 12000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_requires_mora", String.format(java.util.Locale.US, "%,d", costMora)), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentElementalBurst", tBurst + 1);
                player.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.elemental_burst_upgraded", (tBurst + 1)), true);
            }
            case 6 -> {
                int costMora = 30000;
                if (constel > 6) costMora += (constel - 6) * 10000;

                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.talent_requires_mora", String.format(java.util.Locale.US, "%,d", costMora)), true);
                    return;
                }

                CompanionProfile targetProfile = entity != null ? entity.getCompanionProfile() : CompanionContractItem.getProfile(contract);
                if (targetProfile == null) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.cannot_identify"), true);
                    return;
                }

                ItemStack duplicatePactStack = null;
                if (contract != null && contract.getCount() > 1) {
                    duplicatePactStack = contract;
                } else {
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack it = player.getInventory().getItem(i);
                        if (!it.isEmpty() && it.getItem() instanceof CompanionContractItem && it != contract) {
                            CompanionProfile p = CompanionContractItem.getProfile(it);
                            if (p == targetProfile) {
                                duplicatePactStack = it;
                                break;
                            }
                        }
                    }
                }

                if (duplicatePactStack == null) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.requires_duplicate_pact", name), true);
                    return;
                }

                duplicatePactStack.shrink(1);
                data.spendMora(costMora);
                int nextConstel = constel + 1;
                applyConstellation(entity, contract, nextConstel);

                if (entity != null && entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 50, 0.8D, 1.2D, 0.8D, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 2, 0, 0, 0, 0);
                    entity.playSound(ModSounds.WISH_REVEAL_5STAR.get(), 1.0F, 1.0F);
                }
                player.playSound(ModSounds.WISH_REVEAL_5STAR.get(), 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.constellation_activated", name, nextConstel), true);
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.3F);

                if (nextConstel <= 6) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.constellation_unlocked", name, nextConstel), true);
                } else {
                    int over = nextConstel - 6;
                    player.displayClientMessage(Component.translatable("companion.awakening_system.infinite_transcendence", nextConstel, name, over), true);
                }
            }
        }

        AwakeningSystemMod.sync(player);
        openTableForPlayer(player, payload.entityId(), payload.contractSlot());
    }

    public static int getRankMaxLevel(int rank) {
        return switch (rank) {
            case 0 -> 20;
            case 1 -> 40;
            case 2 -> 50;
            case 3 -> 60;
            case 4 -> 70;
            case 5 -> 80;
            case 6 -> 90;
            default -> 90 + (rank - 6) * 10;
        };
    }

    private static void applyLevel(CompanionEntity entity, ItemStack contract, int newLevel) {
        if (entity != null) {
            entity.setCompanionLevel(newLevel);
            entity.heal(30.0F);
        } else if (!contract.isEmpty()) {
            CompoundTag tag = contract.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("CompanionLevel", newLevel);
            contract.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void applyAscensionRank(CompanionEntity entity, ItemStack contract, int newRank) {
        if (entity != null) {
            entity.setAscensionRank(newRank);
            entity.recalculateCompanionStats();
            entity.heal(entity.getMaxHealth());
        } else if (!contract.isEmpty()) {
            CompoundTag tag = contract.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("CompanionAscensionRank", newRank);
            contract.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void applyTalent(CompanionEntity entity, ItemStack contract, String key, int newTalentLvl) {
        if (entity != null) {
            switch (key) {
                case "TalentNormalAttack" -> entity.setTalentNormalAttack(newTalentLvl);
                case "TalentElementalSkill" -> entity.setTalentElementalSkill(newTalentLvl);
                case "TalentElementalBurst" -> entity.setTalentElementalBurst(newTalentLvl);
            }
            entity.recalculateCompanionStats();
        } else if (!contract.isEmpty()) {
            CompoundTag tag = contract.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt(key, newTalentLvl);
            contract.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void applyConstellation(CompanionEntity entity, ItemStack contract, int newConstel) {
        if (entity != null) {
            entity.setConstellation(newConstel);
            entity.recalculateCompanionStats();
            entity.heal(50.0F);
        } else if (!contract.isEmpty()) {
            CompoundTag tag = contract.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("CompanionConstellation", newConstel);
            contract.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public enum GenshinElement {
        PYRO, CRYO, ANEMO, HYDRO, GEO, DENDRO, ELECTRO;

        public static GenshinElement fromString(String element) {
            if (element == null) return GEO;
            String el = element.toLowerCase();
            if (el.contains("pyro") || el.contains("fuego") || el.contains("fire") || el.contains("llama")) return PYRO;
            if (el.contains("cryo") || el.contains("hielo") || el.contains("ice") || el.contains("escarcha") || el.contains("lunar")) return CRYO;
            if (el.contains("hydro") || el.contains("agua") || el.contains("water") || el.contains("mar")) return HYDRO;
            if (el.contains("anemo") || el.contains("viento") || el.contains("wind") || el.contains("infinito") || el.contains("aero")) return ANEMO;
            if (el.contains("electro") || el.contains("rayo") || el.contains("electric") || el.contains("lightning") || el.contains("sombra") || el.contains("trueno")) return ELECTRO;
            if (el.contains("dendro") || el.contains("planta") || el.contains("nature") || el.contains("bosque")) return DENDRO;
            if (el.contains("geo") || el.contains("tierra") || el.contains("roca") || el.contains("metal")) return GEO;
            return GEO;
        }

        public Item getGem(int tier) {
            return switch (tier) {
                case 1 -> switch (this) {
                    case PYRO -> SystemItems.AGNIDUS_AGATE_SLIVER.get();
                    case CRYO -> SystemItems.SHIVADA_JADE_SLIVER.get();
                    case ANEMO -> SystemItems.VAYUDA_TURQUOISE_SLIVER.get();
                    case HYDRO -> SystemItems.VARUNADA_LAZURITE_SLIVER.get();
                    case GEO -> SystemItems.PRITHIVA_TOPAZ_SLIVER.get();
                    case DENDRO -> SystemItems.NAGADUS_EMERALD_SLIVER.get();
                    case ELECTRO -> SystemItems.VAJRADA_AMETHYST_SLIVER.get();
                };
                case 2 -> switch (this) {
                    case PYRO -> SystemItems.AGNIDUS_AGATE_FRAGMENT.get();
                    case CRYO -> SystemItems.SHIVADA_JADE_FRAGMENT.get();
                    case ANEMO -> SystemItems.VAYUDA_TURQUOISE_FRAGMENT.get();
                    case HYDRO -> SystemItems.VARUNADA_LAZURITE_FRAGMENT.get();
                    case GEO -> SystemItems.PRITHIVA_TOPAZ_FRAGMENT.get();
                    case DENDRO -> SystemItems.NAGADUS_EMERALD_FRAGMENT.get();
                    case ELECTRO -> SystemItems.VAJRADA_AMETHYST_FRAGMENT.get();
                };
                case 3 -> switch (this) {
                    case PYRO -> SystemItems.AGNIDUS_AGATE_CHUNK.get();
                    case CRYO -> SystemItems.SHIVADA_JADE_CHUNK.get();
                    case ANEMO -> SystemItems.VAYUDA_TURQUOISE_CHUNK.get();
                    case HYDRO -> SystemItems.VARUNADA_LAZURITE_CHUNK.get();
                    case GEO -> SystemItems.PRITHIVA_TOPAZ_CHUNK.get();
                    case DENDRO -> SystemItems.NAGADUS_EMERALD_CHUNK.get();
                    case ELECTRO -> SystemItems.VAJRADA_AMETHYST_CHUNK.get();
                };
                default -> switch (this) {
                    case PYRO -> SystemItems.AGNIDUS_AGATE_GEMSTONE.get();
                    case CRYO -> SystemItems.SHIVADA_JADE_GEMSTONE.get();
                    case ANEMO -> SystemItems.VAYUDA_TURQUOISE_GEMSTONE.get();
                    case HYDRO -> SystemItems.VARUNADA_LAZURITE_GEMSTONE.get();
                    case GEO -> SystemItems.PRITHIVA_TOPAZ_GEMSTONE.get();
                    case DENDRO -> SystemItems.NAGADUS_EMERALD_GEMSTONE.get();
                    case ELECTRO -> SystemItems.VAJRADA_AMETHYST_GEMSTONE.get();
                };
            };
        }

        public List<Item> getBossDrops() {
            return switch (this) {
                case PYRO -> List.of(SystemItems.EVERFLAME_SEED.get(), SystemItems.SMOLDERING_PEARL.get());
                case CRYO -> List.of(SystemItems.HOARFROST_CORE.get(), SystemItems.CRYSTALLINE_BLOOM.get());
                case ANEMO -> List.of(SystemItems.HURRICANE_SEED.get());
                case HYDRO -> List.of(SystemItems.DEW_OF_REPUDIATION.get());
                case GEO -> List.of(SystemItems.BASALT_PILLAR.get());
                case DENDRO -> List.of(SystemItems.HURRICANE_SEED.get());
                case ELECTRO -> List.of(SystemItems.LIGHTNING_PRISM.get());
            };
        }
    }

    public static List<Item> getCommonMonsterMaterials(int tier) {
        return switch (tier) {
            case 1 -> List.of(
                    SystemItems.SLIME_CONDENSATE.get(),
                    SystemItems.DAMAGED_MASK.get()
            );
            case 2 -> List.of(
                    SystemItems.SLIME_SECRETIONS.get(),
                    SystemItems.STAINED_MASK.get(),
                    SystemItems.HEAVY_HORN.get()
            );
            default -> List.of(
                    SystemItems.SLIME_CONCENTRATE.get(),
                    SystemItems.OMINOUS_MASK.get(),
                    SystemItems.BLACK_BRONZE_HORN.get(),
                    SystemItems.BLACK_CRYSTAL_HORN.get()
            );
        };
    }

    public record AscensionRequirement(
            int moraCost,
            Item primaryGem,
            int gemCount,
            int gemTier,
            List<Item> bossDrops,
            int bossDropCount,
            List<Item> commonMaterials,
            int commonCount,
            int commonTier
    ) {}

    public static AscensionRequirement getAscensionRequirement(CompanionProfile profile, int currentRank) {
        GenshinElement elem = GenshinElement.fromString(profile != null ? profile.element() : "Geo");
        return switch (currentRank) {
            case 0 -> new AscensionRequirement(
                    20000,
                    elem.getGem(1), 1, 1,
                    List.of(), 0,
                    getCommonMonsterMaterials(1), 3, 1
            );
            case 1 -> new AscensionRequirement(
                    40000,
                    elem.getGem(2), 3, 2,
                    elem.getBossDrops(), 2,
                    getCommonMonsterMaterials(1), 10, 1
            );
            case 2 -> new AscensionRequirement(
                    60000,
                    elem.getGem(2), 6, 2,
                    elem.getBossDrops(), 4,
                    getCommonMonsterMaterials(2), 10, 2
            );
            case 3 -> new AscensionRequirement(
                    80000,
                    elem.getGem(3), 3, 3,
                    elem.getBossDrops(), 8,
                    getCommonMonsterMaterials(2), 15, 2
            );
            case 4 -> new AscensionRequirement(
                    100000,
                    elem.getGem(3), 6, 3,
                    elem.getBossDrops(), 12,
                    getCommonMonsterMaterials(3), 12, 3
            );
            case 5 -> new AscensionRequirement(
                    120000,
                    elem.getGem(4), 6, 4,
                    elem.getBossDrops(), 20,
                    getCommonMonsterMaterials(3), 20, 3
            );
            default -> new AscensionRequirement(
                    150000,
                    elem.getGem(4), 6, 4,
                    elem.getBossDrops(), 10,
                    getCommonMonsterMaterials(3), 15, 3
            );
        };
    }

    public static int countItem(net.minecraft.world.entity.player.Player player, Item item) {
        if (item == null) return 0;
        int total = 0;
        for (ItemStack st : player.getInventory().items) {
            if (!st.isEmpty() && st.is(item)) {
                total += st.getCount();
            }
        }
        return total;
    }

    public static int countAnyItem(net.minecraft.world.entity.player.Player player, List<Item> items) {
        if (items == null || items.isEmpty()) return 0;
        int total = 0;
        for (ItemStack st : player.getInventory().items) {
            if (!st.isEmpty()) {
                for (Item it : items) {
                    if (st.is(it)) {
                        total += st.getCount();
                        break;
                    }
                }
            }
        }
        return total;
    }

    public static boolean shrinkItem(net.minecraft.world.entity.player.Player player, Item item, int amount) {
        int rem = amount;
        for (ItemStack st : player.getInventory().items) {
            if (!st.isEmpty() && st.is(item)) {
                int take = Math.min(st.getCount(), rem);
                st.shrink(take);
                rem -= take;
                if (rem <= 0) return true;
            }
        }
        return rem <= 0;
    }

    public static boolean shrinkAnyItem(net.minecraft.world.entity.player.Player player, List<Item> items, int amount) {
        int rem = amount;
        for (ItemStack st : player.getInventory().items) {
            if (!st.isEmpty()) {
                for (Item it : items) {
                    if (st.is(it)) {
                        int take = Math.min(st.getCount(), rem);
                        st.shrink(take);
                        rem -= take;
                        if (rem <= 0) return true;
                        break;
                    }
                }
            }
        }
        return rem <= 0;
    }
}
