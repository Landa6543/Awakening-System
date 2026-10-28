package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RuneStoneItem extends Item {
    public enum RuneType {
        RULERS_AUTHORITY(
                "§9§l[RUNE STONE: RULER'S AUTHORITY]",
                "§b[Active Rune - System Socket [K]]: §fUnleash Ruler's Telekinesis. Pull enemies toward you or sneak to blast them with a violent gravity shockwave (Active Rune Key).",
                "rulers_authority"
        ),
        DRAGON_EYES(
                "§4§l[RUNE STONE: DRAGON EYES]",
                "§d[Passive Rune - System Socket [K]]: §fAwaken draconic vision. Displays exact health, level, and armor of any target in your crosshairs.",
                "dragon_eyes"
        ),
        BLOODTHIRST(
                "§c§l[RUNE STONE: BLOODTHIRST]",
                "§d[Passive Rune - System Socket [K]]: §fAssimilate martial instinct. Grants +15% permanent Lifesteal returned as health.",
                "bloodthirst"
        ),
        SHADOW_STEP(
                "§5§l[RUNE STONE: SHADOW STEP]",
                "§b[Active Rune - System Socket [K]]: §fVanish into shadows and reappear behind target with a guaranteed critical strike and invisibility (Active Rune Key).",
                "shadow_step"
        ),
        FOURTH_WALL(
                "§f§l[RUNE STONE: THE FOURTH WALL]",
                "§d[Passive Rune - System Socket [K]]: §fInscribe Kim Dokja's psychological barrier. Grants immunity to blindness and darkness; prevents fatal death (5m cooldown).",
                "fourth_wall"
        ),
        IRON_BODY(
                "§7§l[RUNE STONE: IRON BODY]",
                "§d[Passive Rune - System Socket [K]]: §fHardens flesh like tempered steel. Grants +4 permanent Armor and massive knockback resistance.",
                "enduring_flesh"
        );

        public final String header;
        public final String desc;
        public final String skillId;

        RuneType(String header, String desc, String skillId) {
            this.header = header;
            this.desc = desc;
            this.skillId = skillId;
        }
    }

    private final RuneType runeType;

    public RuneStoneItem(Properties properties, RuneType runeType) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC));
        this.runeType = runeType;
    }

    public RuneType getRuneType() {
        return runeType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("item.awakening_system.rune_stone.socket_hint"), true);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.rune_stone." + runeType.name().toLowerCase() + ".header"));
        tooltip.add(Component.translatable("awakening_system.rune_stone." + runeType.name().toLowerCase() + ".desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
