package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TeyvatVisionItem extends Item {
    public enum Element {
        PYRO("Pyro", "Fuego", 0xFFFF4422, ChatFormatting.RED),
        ELECTRO("Electro", "Rayo", 0xFFA044FF, ChatFormatting.LIGHT_PURPLE),
        CRYO("Cryo", "Hielo", 0xFF66DDFF, ChatFormatting.AQUA),
        ANEMO("Anemo", "Viento", 0xFF44FFAA, ChatFormatting.GREEN),
        HYDRO("Hydro", "Agua", 0xFF2288FF, ChatFormatting.BLUE),
        GEO("Geo", "Tierra", 0xFFFFBB33, ChatFormatting.GOLD),
        DENDRO("Dendro", "Naturaleza", 0xFF44CC33, ChatFormatting.DARK_GREEN);

        public final String id;
        public final String name;
        public final int color;
        public final ChatFormatting format;

        Element(String id, String name, int color, ChatFormatting format) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.format = format;
        }
    }

    private final Element element;

    public TeyvatVisionItem(Element element, Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("awakening_system.vision.info"), true);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void triggerElementalBurst(ServerPlayer sp, Element element) {
        Level level = sp.level();
        SystemData data = SystemEvents.data(sp);
        if (data.energy() < 30) {
            sp.displayClientMessage(Component.translatable("awakening_system.vision.insufficient_mana"), true);
            sp.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7F, 1.5F);
            return;
        }

        long gameTime = level.getGameTime();
        String cdKey = "vision_" + element.id.toLowerCase() + "_cd";
        long cdUntil = sp.getPersistentData().getLong(cdKey);
        if (gameTime < cdUntil) {
            long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
            sp.displayClientMessage(Component.translatable("awakening_system.vision.cooldown", element.name, sec), true);
            return;
        }

        data.spendEnergy(30);
        sp.getPersistentData().putLong(cdKey, gameTime + 240L);

        AABB box = sp.getBoundingBox().inflate(10.0D, 4.0D, 10.0D);
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

        if (level instanceof ServerLevel sl) {
            switch (element) {
                case PYRO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.PYRO, sp, 35.0F);
                        float dmg = 35.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.setRemainingFireTicks(160);
                        sl.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + 1.0D, e.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
                    }
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.FLAME, 8.0D, 32, 0.15D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.9F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.pyro"), true);
                }
                case ELECTRO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.ELECTRO, sp, 32.0F);
                        float dmg = 32.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 1.0D, e.getZ(), 30, 0.3D, 0.5D, 0.3D, 0.2D);
                    }
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.ELECTRIC_SPARK, 10.0D, 36, 0.2D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.3F, 1.4F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.electro"), true);
                }
                case CRYO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.CRYO, sp, 30.0F);
                        float dmg = 30.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                        e.setTicksFrozen(200);
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, e.getX(), e.getY() + 1.0D, e.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.05D);
                    }
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.SNOWFLAKE, 9.0D, 30, 0.1D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.cryo"), true);
                }
                case ANEMO -> {
                    Vec3 center = sp.position();
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.ANEMO, sp, 28.0F);
                        float dmg = 28.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        Vec3 diff = center.subtract(e.position()).normalize();
                        e.setDeltaMovement(diff.x * 1.5D, 0.8D, diff.z * 1.5D);
                        e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                        sl.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 1.0D, e.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.1D);
                    }
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.SWEEP_ATTACK, 8.0D, 24, 0.18D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.5F, 1.2F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.anemo"), true);
                }
                case HYDRO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.HYDRO, sp, 30.0F);
                        float dmg = 30.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        sl.sendParticles(ParticleTypes.SPLASH, e.getX(), e.getY() + 1.0D, e.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.15D);
                        sl.sendParticles(ParticleTypes.BUBBLE, e.getX(), e.getY() + 1.2D, e.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.05D);
                    }
                    sp.heal(10.0F);
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.SPLASH, 8.5D, 32, 0.15D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.5F, 1.0F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.3F, 1.6F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.hydro"), true);
                }
                case GEO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.GEO, sp, 34.0F);
                        float dmg = 34.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4));
                        sl.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + 1.0D, e.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.2D);
                    }
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2));
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.CRIT, 9.0D, 36, 0.25D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.4F, 1.2F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.geo"), true);
                }
                case DENDRO -> {
                    for (LivingEntity e : enemies) {
                        var r = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(e, com.soybe.awakeningsystem.element.ElementType.DENDRO, sp, 28.0F);
                        float dmg = 28.0F * r.damageMultiplier() + r.bonusDamage();
                        e.hurt(sp.damageSources().playerAttack(sp), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getY() + 1.0D, e.getZ(), 25, 0.5D, 0.7D, 0.5D, 0.05D);
                    }
                    SystemEvents.spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.HAPPY_VILLAGER, 8.0D, 28, 0.15D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.6F, 1.2F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.3F, 1.5F);
                    sp.displayClientMessage(Component.translatable("awakening_system.vision.burst.dendro"), true);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.element").append(Component.literal(element.name)).withStyle(element.format));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.resonance").withStyle(ChatFormatting.AQUA));
    }
}
