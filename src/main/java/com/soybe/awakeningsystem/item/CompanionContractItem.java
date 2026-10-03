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
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("CompanionId")) {
            return CompanionProfile.getById(cd.copyTag().getString("CompanionId"));
        }
        return CompanionProfile.NOELLE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
            CompanionProfile profile = getProfile(context.getItemInHand());
            int compLevel = getLevel(context.getItemInHand());

            CompanionEntity companion = new CompanionEntity(ModEntities.COMPANION.get(), level, player, profile);
            companion.setCompanionLevel(compLevel);
            companion.setConstellation(getConstellation(context.getItemInHand()));
            companion.setTalentNormalAttack(getTalent(context.getItemInHand(), "TalentNormalAttack", 1));
            companion.setTalentElementalSkill(getTalent(context.getItemInHand(), "TalentElementalSkill", 1));
            companion.setTalentElementalBurst(getTalent(context.getItemInHand(), "TalentElementalBurst", 1));
            companion.setAscensionRank(getAscensionRank(context.getItemInHand()));

            // Restaurar artefactos equipados en el contrato
            CustomData cd = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
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

            player.displayClientMessage(Component.literal("§a§l✦ [INVOCACIÓN] §f¡" + profile.displayName() + " ha acudido a tu llamado!"), true);

            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        CompanionProfile p = getProfile(stack);
        String stars = "★".repeat(p.stars());
        int c = getConstellation(stack);
        String cStr = c > 0 ? " §d[C" + c + "]" : "";
        return Component.literal(p.colorCode() + "Pacto de Invocación: " + p.displayName() + " §e[" + stars + "]" + cStr);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompanionProfile p = getProfile(stack);
        String stars = "★".repeat(p.stars());
        int lvl = getLevel(stack);
        int c = getConstellation(stack);
        int rank = getAscensionRank(stack);
        tooltip.add(Component.literal("§eRareza: §6" + stars + (p.stars() == 5 ? " (Legendario 5★)" : " (Épico 4★)")));
        tooltip.add(Component.literal("§aNivel de Acompañante: §2Nv. " + lvl + " §7(Fase " + rank + "/6)"));
        if (c > 0) {
            tooltip.add(Component.literal(c <= 6 ? "§d✦ Constelación Despertada: C" + c : "§5✦ Constelación Trascendente: C" + c + " (Infinita)"));
        }
        tooltip.add(Component.literal("§7Elemento / Sendero: " + p.colorCode() + p.element()));
        tooltip.add(Component.literal("§7Título: §f" + p.title()));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§b✦ Habilidad Elemental en Combate:"));
        tooltip.add(Component.literal("§f" + p.skillDesc()));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§a✔ Clic derecho en un bloque para invocar al acompañante"));
        tooltip.add(Component.literal("§e✔ Shift + Clic derecho en el acompañante para guardarlo"));
        tooltip.add(Component.literal("§d✔ Mejora nivel, talentos y constelaciones infinitas en la Mesa de Ascensión"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
