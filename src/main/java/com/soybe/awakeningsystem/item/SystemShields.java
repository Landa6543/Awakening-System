package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Escudos del Sistema (sección 58). Son escudos de Minecraft puros ({@link ShieldItem} tal cual, no
 * una subclase): Epic Fight elige la pose, el render y el bloqueo de escudo por la clase exacta del
 * ítem, así que solo así se sostienen igual que el escudo normal. Lo propio de cada escudo (efecto
 * al bloquear, reparación con su material y descripción) se engancha aquí con eventos.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class SystemShields {
    public enum Kind { SIDERITE_KITE, BASTION_TOWER, SPIRIT_JADE_ROUND }

    private SystemShields() {}

    /** Un escudo normal con su durabilidad y los atributos que da en la mano secundaria. */
    public static ShieldItem create(Kind kind, int durability, double armor, double knockbackResistance, double maxHealth, Item.Properties props) {
        String base = kind.name().toLowerCase();
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        if (armor > 0) b.add(Attributes.ARMOR, mod(base + "_armor", armor), EquipmentSlotGroup.OFFHAND);
        if (knockbackResistance > 0) b.add(Attributes.KNOCKBACK_RESISTANCE, mod(base + "_knockback", knockbackResistance), EquipmentSlotGroup.OFFHAND);
        if (maxHealth > 0) b.add(Attributes.MAX_HEALTH, mod(base + "_health", maxHealth), EquipmentSlotGroup.OFFHAND);
        return new ShieldItem(props.durability(durability).attributes(b.build()));
    }

    private static AttributeModifier mod(String id, double amount) {
        return new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shield_" + id), amount, AttributeModifier.Operation.ADD_VALUE);
    }

    static Kind kindOf(ItemStack stack) {
        if (stack.is(SystemItems.SIDERITE_KITE_SHIELD.get())) return Kind.SIDERITE_KITE;
        if (stack.is(SystemItems.BASTION_TOWER_SHIELD.get())) return Kind.BASTION_TOWER;
        if (stack.is(SystemItems.SPIRIT_JADE_ROUND_SHIELD.get())) return Kind.SPIRIT_JADE_ROUND;
        return null;
    }

    static Item repairMaterial(Kind kind) {
        return switch (kind) {
            case SIDERITE_KITE -> SystemItems.SIDERITE_INGOT.get();
            case BASTION_TOWER -> Items.IRON_INGOT;
            case SPIRIT_JADE_ROUND -> SystemItems.SPIRIT_JADE.get();
        };
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Kind kind = kindOf(event.getItemStack());
        if (kind == null) return;
        String key = event.getItemStack().getDescriptionId() + ".desc";
        event.getToolTip().add(1, Component.translatable(key).withStyle(ChatFormatting.GRAY));
        if (kind == Kind.BASTION_TOWER) event.getToolTip().add(2, Component.translatable(key + "_class").withStyle(ChatFormatting.DARK_AQUA));
    }

    /** Reparar en el yunque con el material del escudo: cada unidad devuelve un cuarto de la durabilidad, como las herramientas. */
    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft(), right = event.getRight();
        Kind kind = kindOf(left);
        if (kind == null || !left.isDamaged() || !right.is(repairMaterial(kind))) return;
        int perUnit = Math.max(1, left.getMaxDamage() / 4);
        int units = Math.min(right.getCount(), (left.getDamageValue() + perUnit - 1) / perUnit);
        ItemStack out = left.copy();
        out.setDamageValue(Math.max(0, left.getDamageValue() - perUnit * units));
        event.setOutput(out);
        event.setMaterialCost(units);
        event.setCost(units);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked()) return;
        LivingEntity blocker = event.getEntity();
        Kind kind = kindOf(blocker.getUseItem());
        if (kind == null || !(blocker.level() instanceof ServerLevel level)) return;
        float blocked = event.getBlockedDamage();
        LivingEntity attacker = event.getDamageSource().getDirectEntity() instanceof LivingEntity le && le != blocker ? le : null;

        switch (kind) {
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
                // muralla: cada bloqueo da Resistencia; el Baluarte de Hierro además devuelve parte del golpe
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
                blocker.heal(Math.min(4.0F, Math.max(1.0F, blocked * 0.15F)));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blocker.getX(), blocker.getY() + 1.2, blocker.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                level.playSound(null, blocker.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.3F);
            }
        }
    }
}
