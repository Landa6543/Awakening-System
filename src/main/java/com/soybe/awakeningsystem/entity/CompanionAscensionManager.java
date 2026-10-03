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

        // Agregar invocados
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

        // Agregar contratos de inventario
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

        // Elegir índice seleccionado
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
            player.displayClientMessage(Component.literal("§cCompañero no encontrado."), true);
            return;
        }

        // Obtener datos actuales
        int level = entity != null ? entity.getCompanionLevel() : CompanionContractItem.getLevel(contract);
        int rank = entity != null ? entity.getAscensionRank() : CompanionContractItem.getAscensionRank(contract);
        int constel = entity != null ? entity.getConstellation() : CompanionContractItem.getConstellation(contract);
        int tAttack = entity != null ? entity.getTalentNormalAttack() : CompanionContractItem.getTalent(contract, "TalentNormalAttack", 1);
        int tSkill = entity != null ? entity.getTalentElementalSkill() : CompanionContractItem.getTalent(contract, "TalentElementalSkill", 1);
        int tBurst = entity != null ? entity.getTalentElementalBurst() : CompanionContractItem.getTalent(contract, "TalentElementalBurst", 1);
        String name = entity != null ? entity.getCompanionProfile().displayName() : CompanionContractItem.getProfile(contract).displayName();

        int maxPlayerLevel = Math.max(1, data.level());

        switch (payload.actionType()) {
            case 0 -> { // Subir de Nivel (+1)
                if (level >= maxPlayerLevel) {
                    player.displayClientMessage(Component.literal("§e" + name + " ya alcanzó tu nivel máximo de cazador (Nv. " + maxPlayerLevel + "). ¡Sube tu nivel para continuar!"), true);
                    return;
                }
                int rankCap = getRankMaxLevel(rank);
                if (level >= rankCap) {
                    player.displayClientMessage(Component.literal("§6Límite de fase alcanzado (Nv. " + rankCap + "). ¡Realiza una Ascensión de Rango para romper el límite!"), true);
                    return;
                }

                int expCost = 100 + (level - 1) * 50;
                int moraCost = level * 1000;

                if (data.mora() < moraCost) {
                    player.displayClientMessage(Component.literal("§cMora insuficiente. Requiere §6" + String.format("%,d", moraCost) + " Mora§c."), true);
                    return;
                }

                // Intentar consumir Ingenio del Héroe o Exp del Aventurero o EXP de Minecraft
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
                        // Consumir del sistema de experiencia
                        paidExp = true;
                    }
                }

                if (!paidExp) {
                    player.displayClientMessage(Component.literal("§cRequiere §b1x Ingenio del Héroe §co §a" + expCost + " EXP§c."), true);
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

                player.displayClientMessage(Component.literal("§a✦ " + name + " subió al §eNv. " + nextLvl + " §f(-" + String.format("%,d", moraCost) + " Mora)."), false);
            }
            case 1 -> { // Subir de Nivel (+10 o hasta el límite de fase)
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
                    player.displayClientMessage(Component.literal("§6§l✦ ¡SUBIDA RÁPIDA! §f" + name + " ascendió +" + upgrades + " niveles hasta el §eNv. " + level + "§f."), false);
                } else {
                    player.displayClientMessage(Component.literal("§cRecursos insuficientes o nivel máximo alcanzado."), true);
                }
            }
            case 2 -> { // Ascensión de Rango (Ascension Phase)
                int rankCap = getRankMaxLevel(rank);
                if (level < rankCap) {
                    player.displayClientMessage(Component.literal("§cRequiere alcanzar el Nivel " + rankCap + " antes de ascender de fase."), true);
                    return;
                }
                int costMora = (rank + 1) * 15000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.literal("§cMora insuficiente. Requiere §6" + String.format("%,d", costMora) + " Mora§c."), true);
                    return;
                }

                // Requerir 1x Polvo Estelar o 20 Protogemas
                boolean paidCatalyst = false;
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (!it.isEmpty() && it.is(SystemItems.MASTERLESS_STARDUST.get())) {
                        it.shrink(1);
                        paidCatalyst = true;
                        break;
                    }
                }
                if (!paidCatalyst) {
                    if (data.primogems() >= 20) {
                        data.spendPrimogems(20);
                        paidCatalyst = true;
                    }
                }

                if (!paidCatalyst) {
                    player.displayClientMessage(Component.literal("§cRequiere §d1x Polvo Estelar §co §b20 Protogemas §cpara la Ascensión."), true);
                    return;
                }

                data.spendMora(costMora);
                int nextRank = rank + 1;
                applyAscensionRank(entity, contract, nextRank);

                if (entity != null && entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 40, 0.6D, 1.0D, 0.6D, 0.1D);
                    entity.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F);
                }
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F);

                player.displayClientMessage(Component.literal("§6§l✦ ¡ASCENSIÓN CELESTIAL DE FASE " + nextRank + "! §f" + name + " rompió sus límites (+15% Vida y Daño Base permanente)."), false);
            }
            case 3 -> { // Subir Talento: Ataque Normal
                int maxTalent = 1 + rank * 2;
                if (tAttack >= maxTalent) {
                    player.displayClientMessage(Component.literal("§cTalento al límite para la fase actual. ¡Asciende de rango primero!"), true);
                    return;
                }
                int costMora = tAttack * 8000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.literal("§cRequiere §6" + String.format("%,d", costMora) + " Mora§c."), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentNormalAttack", tAttack + 1);
                player.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.4F);
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.4F);
                player.displayClientMessage(Component.literal("§e⚔ Ataque Normal subió al §6Nv. " + (tAttack + 1) + "§f."), false);
            }
            case 4 -> { // Subir Talento: Habilidad Elemental
                int maxTalent = 1 + rank * 2;
                if (tSkill >= maxTalent) {
                    player.displayClientMessage(Component.literal("§cTalento al límite para la fase actual. ¡Asciende de rango primero!"), true);
                    return;
                }
                int costMora = tSkill * 8000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.literal("§cRequiere §6" + String.format("%,d", costMora) + " Mora§c."), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentElementalSkill", tSkill + 1);
                player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.3F);
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.5F);
                player.displayClientMessage(Component.literal("§b⚡ Habilidad Elemental subió al §6Nv. " + (tSkill + 1) + "§f."), false);
            }
            case 5 -> { // Subir Talento: Ráfaga Definitiva
                int maxTalent = 1 + rank * 2;
                if (tBurst >= maxTalent) {
                    player.displayClientMessage(Component.literal("§cTalento al límite para la fase actual. ¡Asciende de rango primero!"), true);
                    return;
                }
                int costMora = tBurst * 12000;
                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.literal("§cRequiere §6" + String.format("%,d", costMora) + " Mora§c."), true);
                    return;
                }
                data.spendMora(costMora);
                applyTalent(entity, contract, "TalentElementalBurst", tBurst + 1);
                player.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                player.displayClientMessage(Component.literal("§d💥 Ráfaga Definitiva subió al §6Nv. " + (tBurst + 1) + "§f."), false);
            }
            case 6 -> { // Desbloquear / Subir Constelación (C1... C∞)
                int costMora = 30000;
                if (constel > 6) costMora += (constel - 6) * 10000;

                if (data.mora() < costMora) {
                    player.displayClientMessage(Component.literal("§cRequiere §6" + String.format("%,d", costMora) + " Mora§c."), true);
                    return;
                }

                // Buscar 2x Brillo Estelar o 160 Protogemas
                boolean paid = false;
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (!it.isEmpty() && it.is(SystemItems.MASTERLESS_STARGLITTER.get()) && it.getCount() >= 2) {
                        it.shrink(2);
                        paid = true;
                        break;
                    }
                }
                if (!paid && data.primogems() >= 160) {
                    data.spendPrimogems(160);
                    paid = true;
                }

                if (!paid) {
                    player.displayClientMessage(Component.literal("§cRequiere §62x Brillo Estelar §co §b160 Protogemas §cpara resonar con la Constelación."), true);
                    return;
                }

                data.spendMora(costMora);
                int nextConstel = constel + 1;
                applyConstellation(entity, contract, nextConstel);

                if (entity != null && entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 50, 0.8D, 1.2D, 0.8D, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 2, 0, 0, 0, 0);
                    entity.playSound(ModSounds.WISH_REVEAL_5STAR.get(), 1.0F, 1.0F);
                }
                player.playSound(ModSounds.WISH_REVEAL_5STAR.get(), 1.0F, 1.0F);
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.3F);

                if (nextConstel <= 6) {
                    player.displayClientMessage(Component.literal("§d§l✦ ¡CONSTELACIÓN DESBLOQUEADA! §f" + name + " activó §d[C" + nextConstel + "]§f."), false);
                } else {
                    int over = nextConstel - 6;
                    player.displayClientMessage(Component.literal("§b§l✦ ¡TRASCENDENCIA INFINITA C" + nextConstel + "! §f" + name + " suma §a+5% Daño Total, +50 Vida Máxima y +3% Daño Crítico §b(Nivel Trascendente +" + over + ")."), false);
                }
            }
        }

        AwakeningSystemMod.sync(player);
        // Actualizar interfaz en tiempo real
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
}
