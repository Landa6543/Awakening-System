package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.entity.CompanionProfile;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class CompanionContractItem extends Item {
    public CompanionContractItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    public static ItemStack createContract(CompanionProfile profile) {
        return createContract(profile, 1);
    }

    public static ItemStack createContract(CompanionProfile profile, int level) {
        ItemStack stack = new ItemStack(SystemItems.COMPANION_CONTRACT.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("CompanionId", profile.id());
        tag.putInt("CompanionLevel", Math.max(1, level));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static int getLevel(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("CompanionLevel")) {
            return Math.max(1, cd.copyTag().getInt("CompanionLevel"));
        }
        return 1;
    }

    public static int getConstellation(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("CompanionConstellation")) {
            return Math.max(0, cd.copyTag().getInt("CompanionConstellation"));
        }
        return 0;
    }

    public static int getTalent(ItemStack stack, String key, int def) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains(key)) {
            return Math.max(1, cd.copyTag().getInt(key));
        }
        return def;
    }

    public static int getAscensionRank(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("CompanionAscensionRank")) {
            return Math.max(0, cd.copyTag().getInt("CompanionAscensionRank"));
        }
        return 0;
    }

    public static CompanionProfile getProfile(ItemStack stack) {
        if (isUmaContract(stack)) {
            return null;
        }
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("CompanionId")) {
            return CompanionProfile.getById(cd.copyTag().getString("CompanionId"));
        }
        return CompanionProfile.NOELLE;
    }

    public static boolean isUmaContract(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.copyTag().contains("UmaId");
    }

    public static com.soybe.awakeningsystem.uma.UmaProfile getUmaProfile(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return com.soybe.awakeningsystem.uma.UmaProfile.SPECIAL_WEEK;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaId")) {
            return com.soybe.awakeningsystem.uma.UmaProfile.byId(cd.copyTag().getString("UmaId"));
        }
        return com.soybe.awakeningsystem.uma.UmaProfile.SPECIAL_WEEK;
    }

    public static int getUmaRarity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaRarity")) {
            return Math.max(1, cd.copyTag().getInt("UmaRarity"));
        }
        return 1;
    }

    public static boolean isUmaNew(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaNew")) {
            return cd.copyTag().getBoolean("UmaNew");
        }
        return true;
    }

    public static int getUmaShards(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaShards")) {
            return Math.max(0, cd.copyTag().getInt("UmaShards"));
        }
        return 0;
    }

    public static int getUmaStatues(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaStatues")) {
            return Math.max(1, cd.copyTag().getInt("UmaStatues"));
        }
        int rarity = getUmaRarity(stack);
        return rarity == 3 ? 20 : (rarity == 2 ? 3 : 1);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
            CustomData cd = context.getItemInHand().get(DataComponents.CUSTOM_DATA);

            if (cd != null && cd.copyTag().contains("UmaId")) {
                String umaId = cd.copyTag().getString("UmaId");
                com.soybe.awakeningsystem.uma.UmaProfile umaProf = com.soybe.awakeningsystem.uma.UmaProfile.byId(umaId);
                com.soybe.awakeningsystem.uma.UmaRacerEntity racer = new com.soybe.awakeningsystem.uma.UmaRacerEntity(
                        com.soybe.awakeningsystem.entity.ModEntities.UMA_RACER.get(), level);
                racer.setProfileId(umaId);
                racer.setCompanion(true);
                racer.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
                sl.addFreshEntity(racer);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, 30, 0.5D, 0.8D, 0.5D, 0.1D);
                level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
                player.displayClientMessage(Component.translatable("item.awakening_system.companion_contract.uma_summoned", umaProf.getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }

            CompanionProfile profile = getProfile(context.getItemInHand());
            int compLevel = getLevel(context.getItemInHand());

            CompanionEntity companion = new CompanionEntity(ModEntities.COMPANION.get(), level, player, profile);
            companion.setCompanionLevel(compLevel);
            companion.setConstellation(getConstellation(context.getItemInHand()));
            companion.setTalentNormalAttack(getTalent(context.getItemInHand(), "TalentNormalAttack", 1));
            companion.setTalentElementalSkill(getTalent(context.getItemInHand(), "TalentElementalSkill", 1));
            companion.setTalentElementalBurst(getTalent(context.getItemInHand(), "TalentElementalBurst", 1));
            companion.setAscensionRank(getAscensionRank(context.getItemInHand()));

            if (cd != null && cd.copyTag().contains("CompanionArtifacts", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag artList = cd.copyTag().getList("CompanionArtifacts", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < artList.size(); i++) {
                    net.minecraft.nbt.CompoundTag slotTag = artList.getCompound(i);
                    int s = slotTag.getByte("Slot") & 255;
                    if (s < 5) {
                        ItemStack.parse(sl.registryAccess(), slotTag).ifPresent(stack -> companion.setArtifact(s, stack));
                    }
                }
            }

            companion.recalculateCompanionStats();
            companion.setHealth(companion.getMaxHealth());

            companion.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
            sl.addFreshEntity(companion);

            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, 40, 0.5D, 0.8D, 0.5D, 0.1D);
            sl.sendParticles(ParticleTypes.END_ROD, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.2D, spawnPos.getZ() + 0.5D, 30, 0.4D, 0.6D, 0.4D, 0.05D);
            if (player instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
            }
            level.playSound(null, spawnPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
            level.playSound(null, spawnPos, profile.getSummonSound(), SoundSource.VOICE, 1.3F, 1.0F);

            player.displayClientMessage(Component.translatable("item.awakening_system.companion_contract.companion_summoned", profile.displayName()), true);

            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaId")) {
            CompoundTag tag = cd.copyTag();
            com.soybe.awakeningsystem.uma.UmaProfile up = com.soybe.awakeningsystem.uma.UmaProfile.byId(tag.getString("UmaId"));
            int r = tag.getInt("UmaRarity");
            String col = r == 3 ? "§6§l" : (r == 2 ? "§d§l" : "§b§l");
            return Component.literal(col + "Racer Contract: " + up.getDisplayName() + " §e[" + "★".repeat(r) + "]");
        }
        CompanionProfile p = getProfile(stack);
        String stars = "★".repeat(p.stars());
        int c = getConstellation(stack);
        String cStr = c > 0 ? " §d[C" + c + "]" : "";
        return Component.literal(p.colorCode() + "Summoning Contract: " + p.displayName() + " §e[" + stars + "]" + cStr);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaId")) {
            CompoundTag tag = cd.copyTag();
            com.soybe.awakeningsystem.uma.UmaProfile up = com.soybe.awakeningsystem.uma.UmaProfile.byId(tag.getString("UmaId"));
            int r = tag.getInt("UmaRarity");
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.rarity", "★".repeat(r), r));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.tactique", up.getTactique()));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.track_info", up.getDistance(), up.getSurface()));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.skill", up.getSkillName()));
            tooltip.add(Component.empty());
            if (tag.getBoolean("UmaNew")) {
                tooltip.add(Component.translatable("item.awakening_system.companion_contract.unlocked"));
            } else {
                tooltip.add(Component.translatable("item.awakening_system.companion_contract.shards", tag.getInt("UmaShards")));
            }
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.race_hint"));
            return;
        }

        CompanionProfile p = getProfile(stack);
        String stars = "★".repeat(p.stars());
        int lvl = getLevel(stack);
        int c = getConstellation(stack);
        int rank = getAscensionRank(stack);
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.rarity", stars, p.stars()).append(Component.translatable(p.stars() == 5 ? "item.awakening_system.companion_contract.companion_rarity_5" : "item.awakening_system.companion_contract.companion_rarity_4")));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.companion_level", lvl, rank));
        if (c > 0) {
            tooltip.add(Component.translatable(c <= 6 ? "item.awakening_system.companion_contract.constellation_awakened" : "item.awakening_system.companion_contract.constellation_transcendent", c));
        }
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.element_path", p.colorCode(), p.getElementComponent()));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.title", p.getTitleComponent()));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.combat_skill_title"));
        tooltip.add(Component.literal("§f").append(p.getSkillDescComponent()));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.summon_hint"));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.store_hint"));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.upgrade_hint"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
