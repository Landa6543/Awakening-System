package com.soybe.awakeningsystem.celestial;

import com.soybe.awakeningsystem.item.AwakeningWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/** One weapon of the celestial armory: a strong blade with a passive on every hit and a skill on right-click. */
public class CelestialWeaponItem extends SwordItem implements AwakeningWeaponItem {
    public final CelestialWeapon weapon;

    public CelestialWeaponItem(CelestialWeapon weapon) {
        super(Tiers.NETHERITE, new Properties().fireResistant().rarity(weapon.tier >= 3 ? Rarity.EPIC : Rarity.RARE)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, weapon.bonus(), weapon.kind.speed)));
        this.weapon = weapon;
    }

    // ── Galaxia's two forms ──────────────────────────────────────────────────

    public static boolean capricorn(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean("capricorn");
    }

    // ── passive ──────────────────────────────────────────────────────────────

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyEffect(weapon, target, attacker, weapon.swing());
        if (attacker.level() instanceof ServerLevel sl)
            sl.sendParticles(dust(weapon.color, 1.4F), target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 14, 0.4, 0.4, 0.4, 0.02);
        return super.hurtEnemy(stack, target, attacker);
    }

    static void applyEffect(CelestialWeapon w, LivingEntity target, Entity attacker, float damage) {
        switch (w.effect) {
            case FIRE -> target.igniteForSeconds(4);
            case WITHER -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            case SLOW -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            case LIFESTEAL -> { if (attacker instanceof LivingEntity l) l.heal(Math.max(1.0F, damage * 0.08F)); }
            case NONE -> {}
        }
    }

    static DustParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255F, ((rgb >> 8) & 255) / 255F, (rgb & 255) / 255F), size);
    }

    // ── skill ────────────────────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (weapon == CelestialWeapon.GALAXIA && player.isShiftKeyDown()) {
            boolean next = !capricorn(stack);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("capricorn", next));
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, next ? 0.8F : 1.3F);
            player.displayClientMessage(Component.translatable(next ? "item.awakening_system.galaxia.capricorn" : "item.awakening_system.galaxia.cancer")
                    .withStyle(next ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!(level instanceof ServerLevel sl)) return InteractionResultHolder.success(stack);
        CelestialWeapon.Ability ability = weapon.ability;
        int count = weapon.count, spread = weapon.spread;
        if (weapon == CelestialWeapon.GALAXIA && capricorn(stack)) {
            ability = CelestialWeapon.Ability.BEAM;
            count = 5;
            spread = 36;
        }
        float dmg = weapon.swing();
        switch (ability) {
            case BEAM -> beams(sl, player, stack, count, spread, dmg * 0.8F);
            case STARFALL -> starfall(sl, player, count, dmg * 0.7F);
            case SCYTHE -> {
                CelestialBoltEntity b = CelestialBoltEntity.create(sl, player, weapon, CelestialBoltEntity.SCYTHE, stack, "", dmg * 0.9F);
                b.launch(player.getLookAngle(), 1.3);
                sl.addFreshEntity(b);
                sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.7F);
            }
            case JAVELIN -> {
                CelestialBoltEntity b = CelestialBoltEntity.create(sl, player, weapon, CelestialBoltEntity.JAVELIN, stack, weapon.sprite, dmg * 1.4F);
                b.splits = count;
                b.launch(player.getLookAngle(), 2.2);
                sl.addFreshEntity(b);
                sl.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            case VEHEMENCE -> {
                CelestialBoltEntity b = CelestialBoltEntity.create(sl, player, weapon, CelestialBoltEntity.VEHEMENCE, stack, weapon.sprite, dmg * 1.2F);
                b.launch(player.getLookAngle(), 2.0);
                sl.addFreshEntity(b);
                sl.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            case NEBULA -> {
                Vec3 at = aim(sl, player, 24.0);
                CelestialCloudEntity c = CelestialCloudEntity.create(sl, player, weapon, CelestialCloudEntity.NEBULA, at, 4.0F, 120, dmg * 0.35F);
                sl.addFreshEntity(c);
                sl.playSound(null, at.x, at.y, at.z, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.2F, 0.7F);
            }
            case SLAM -> slam(sl, player, dmg * 1.3F);
            case DASH -> dash(sl, player, weapon.kind == CelestialWeapon.Kind.SPEAR ? 7.0 : 9.0, dmg);
        }
        player.getCooldowns().addCooldown(this, ability.cooldown);
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    private void beams(ServerLevel sl, Player player, ItemStack stack, int count, int spread, float dmg) {
        Vec3 look = player.getLookAngle();
        for (int i = 0; i < count; i++) {
            float off = count == 1 ? 0 : spread >= 360 ? i * 360F / count : -spread / 2F + spread * i / (float) (count - 1);
            Vec3 dir = look.yRot((float) Math.toRadians(off));
            CelestialBoltEntity b = CelestialBoltEntity.create(sl, player, weapon, CelestialBoltEntity.BEAM, stack, "", dmg);
            b.launch(dir, 1.8);
            sl.addFreshEntity(b);
        }
        sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.3F);
        sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private void starfall(ServerLevel sl, Player player, int count, float dmg) {
        Vec3 at = aim(sl, player, 32.0);
        for (int i = 0; i < count; i++) {
            Vec3 from = at.add(sl.random.nextGaussian() * 4, 14 + sl.random.nextFloat() * 6, sl.random.nextGaussian() * 4);
            Vec3 to = at.add(sl.random.nextGaussian() * 1.2, 0, sl.random.nextGaussian() * 1.2);
            CelestialBoltEntity b = CelestialBoltEntity.create(sl, player, weapon, CelestialBoltEntity.STAR, ItemStack.EMPTY, weapon.sprite, dmg);
            b.setPos(from);
            b.launch(to.subtract(from).normalize(), 1.2 + sl.random.nextFloat() * 0.4);
            sl.addFreshEntity(b);
        }
        sl.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    private void slam(ServerLevel sl, Player player, float dmg) {
        Vec3 c = player.position();
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(5.5, 2.5, 5.5), e -> e != player && e.isAlive())) {
            if (e.distanceToSqr(c) > 30.0) continue;
            e.hurt(sl.damageSources().playerAttack(player), dmg);
            applyEffect(weapon, e, player, dmg);
            Vec3 push = e.position().subtract(c).normalize().scale(0.9);
            e.push(push.x, 0.55, push.z);
            e.hurtMarked = true;
        }
        for (int r = 1; r <= 5; r++)
            for (int k = 0; k < r * 10; k++) {
                double a = Math.PI * 2 * k / (r * 10);
                sl.sendParticles(dust(weapon.color, 1.8F), c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0);
            }
        sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.5, c.z, 3, 1.5, 0.2, 1.5, 0);
        sl.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        sl.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.6F);
    }

    private void dash(ServerLevel sl, Player player, double length, float dmg) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, Math.max(-0.2, Math.min(0.35, look.y)), look.z).normalize();
        Vec3 from = player.position().add(0, 0.9, 0);
        BlockHitResult wall = sl.clip(new ClipContext(from, from.add(flat.scale(length)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = wall.getType() == HitResult.Type.MISS ? length : Math.max(0, wall.getLocation().distanceTo(from) - 0.6);
        Vec3 to = from.add(flat.scale(reach));
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(1.3), e -> e != player && e.isAlive())) {
            e.hurt(sl.damageSources().playerAttack(player), dmg);
            applyEffect(weapon, e, player, dmg);
        }
        for (double d = 0; d < reach; d += 0.4)
            sl.sendParticles(dust(weapon.color, 1.6F), from.x + flat.x * d, from.y + flat.y * d, from.z + flat.z * d, 2, 0.2, 0.3, 0.2, 0);
        player.setDeltaMovement(flat.scale(Math.min(2.4, reach * 0.33)).add(0, 0.15, 0));
        player.hurtMarked = true;
        player.fallDistance = 0;
        sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.6F);
        sl.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.6F, 1.8F);
    }

    /** Where the player is looking: the first creature or block within {@code range}, else that far ahead. */
    private static Vec3 aim(ServerLevel sl, Player player, double range) {
        Vec3 eye = player.getEyePosition(), end = eye.add(player.getLookAngle().scale(range));
        BlockHitResult block = sl.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 stop = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(sl, player, eye, stop,
                new AABB(eye, stop).inflate(1.0), e -> e instanceof LivingEntity && e != player && e.isAlive(), 0.3F);
        if (hit != null) return hit.getEntity().position();
        if (block.getType() != HitResult.Type.MISS) return stop;
        // nothing hit: drop to the ground under the end point
        BlockHitResult down = sl.clip(new ClipContext(stop, stop.add(0, -24, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return down.getType() == HitResult.Type.MISS ? stop : down.getLocation();
    }

    // ── tooltip ──────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.celestial.kind." + weapon.kind.name().toLowerCase())
                .withStyle(ChatFormatting.DARK_GRAY));
        String ability = weapon.ability.name().toLowerCase();
        if (weapon == CelestialWeapon.GALAXIA) {
            ability = capricorn(stack) ? "beam" : "starfall";
            tooltip.add(Component.translatable(capricorn(stack) ? "item.awakening_system.galaxia.capricorn" : "item.awakening_system.galaxia.cancer")
                    .withStyle(capricorn(stack) ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.translatable("item.awakening_system.celestial.skill",
                Component.translatable("item.awakening_system.celestial.ability." + ability)).withStyle(ChatFormatting.AQUA));
        if (weapon.effect != CelestialWeapon.Effect.NONE)
            tooltip.add(Component.translatable("item.awakening_system.celestial.effect." + weapon.effect.name().toLowerCase())
                    .withStyle(ChatFormatting.GOLD));
        if (weapon == CelestialWeapon.GALAXIA)
            tooltip.add(Component.translatable("item.awakening_system.galaxia.switch").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.celestial.cooldown", String.valueOf(Mth.ceil(weapon.ability.cooldown / 20F)))
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
