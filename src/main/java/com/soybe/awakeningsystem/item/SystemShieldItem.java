package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/**
 * Escudos del Sistema (sección 58). Funcionan como el escudo normal, pero llevan modelo 3D propio,
 * dan armadura en la mano secundaria y tienen un efecto al bloquear. Los puede usar cualquiera;
 * el Bastión de Hierro saca un extra del Escudo Torre.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public class SystemShieldItem extends ShieldItem {
    public enum Kind { SIDERITE_KITE, BASTION_TOWER, SPIRIT_JADE_ROUND }

    private final Kind kind;
    private final Supplier<Ingredient> repair;

    public SystemShieldItem(Kind kind, Supplier<Ingredient> repair, int durability, double armor, double knockbackResistance, double maxHealth, Properties props) {
        super(props.durability(durability).attributes(attributes(kind, armor, knockbackResistance, maxHealth)));
        this.kind = kind;
        this.repair = repair;
    }

    private static ItemAttributeModifiers attributes(Kind kind, double armor, double kb, double hp) {
        String base = kind.name().toLowerCase();
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        if (armor > 0) b.add(Attributes.ARMOR, mod(base + "_armor", armor), EquipmentSlotGroup.OFFHAND);
        if (kb > 0) b.add(Attributes.KNOCKBACK_RESISTANCE, mod(base + "_knockback", kb), EquipmentSlotGroup.OFFHAND);
        if (hp > 0) b.add(Attributes.MAX_HEALTH, mod(base + "_health", hp), EquipmentSlotGroup.OFFHAND);
        return b.build();
    }

    private static AttributeModifier mod(String id, double amount) {
        return new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shield_" + id), amount, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack material) {
        return repair.get().test(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        String key = getDescriptionId() + ".desc";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        if (kind == Kind.BASTION_TOWER) tooltip.add(Component.translatable(key + "_class").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, ctx, tooltip, flag);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked()) return;
        LivingEntity blocker = event.getEntity();
        if (!(blocker.getUseItem().getItem() instanceof SystemShieldItem shield)) return;
        if (!(blocker.level() instanceof ServerLevel level)) return;
        float blocked = event.getBlockedDamage();
        LivingEntity attacker = event.getDamageSource().getDirectEntity() instanceof LivingEntity le && le != blocker ? le : null;

        switch (shield.kind) {
            case SIDERITE_KITE -> {
                // metal de meteorito: el golpe rebota y empuja al atacante cuerpo a cuerpo
                if (attacker != null) {
                    double dx = attacker.getX() - blocker.getX(), dz = attacker.getZ() - blocker.getZ();
                    attacker.knockback(0.8, -dx, -dz);
                    attacker.hurtMarked = true;
                    level.sendParticles(ParticleTypes.FLAME, attacker.getX(), attacker.getY() + 1.0, attacker.getZ(), 6, 0.2, 0.3, 0.2, 0.02);
                }
                level.playSound(null, blocker.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.35F, 1.6F);
            }
            case BASTION_TOWER -> {
                // muralla: cada bloqueo da Resistencia; el Bastión de Hierro además devuelve parte del golpe
                blocker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, true));
                if (attacker != null && blocker instanceof Player p
                        && "iron_bastion".equals(((SystemData) p.getData(AwakeningSystemMod.PLAYER_SYSTEM)).hunterClass())) {
                    attacker.hurt(blocker.damageSources().thorns(blocker), Math.max(1.0F, blocked * 0.30F));
                    level.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getY() + 1.0, attacker.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
                }
                level.playSound(null, blocker.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 0.5F, 0.7F);
            }
            case SPIRIT_JADE_ROUND -> {
                // jade espiritual: el Qi del golpe detenido cura al portador
                float heal = Math.min(4.0F, Math.max(1.0F, blocked * 0.15F));
                blocker.heal(heal);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blocker.getX(), blocker.getY() + 1.2, blocker.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                level.playSound(null, blocker.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.3F);
            }
        }
    }
}
