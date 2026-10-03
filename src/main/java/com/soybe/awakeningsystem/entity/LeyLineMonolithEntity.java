package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

/**
 * Entidad 3D Canónica del Monolito de las Líneas Ley de Genshin Impact (Ley Line Monolith).
 * Obelisco de piedra antigua levitante con anillos rúnicos y canales de energía cian.
 * En las cámaras de defensa del Abismo, los monstruos se abalanzan sobre él para intentar destruirlo.
 */
public class LeyLineMonolithEntity extends LivingEntity {
    private int aggroPulseCooldown = 20;
    private int invulnTicks = 0;

    public LeyLineMonolithEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§b§l✦ Monolito de las Líneas Ley [100%] ✦"));
        this.setCustomNameVisible(true);
        this.addTag("ley_line_monolith");
        this.addTag("abyss_defense_target");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        // Obelisco masivo anclado por magia antigua
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // INMUNIDAD TOTAL a daño causado directa o indirectamente por el jugador
        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player ||
            source.getDirectEntity() instanceof net.minecraft.world.entity.player.Player) {
            return false;
        }
        if (source.getEntity() != null) {
            var tags = source.getEntity().getTags();
            if (tags.contains("shadow_soldier") || tags.contains("abyss_friendly") || tags.contains("companion") || tags.contains("tamed")) {
                return false;
            }
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            return false;
        }

        // Enfriamiento interno de daño (I-Frames): máximo 1 impacto registrado cada 12 ticks (~1.6 impactos/seg)
        if (this.invulnTicks > 0) {
            return false;
        }
        this.invulnTicks = 12;

        // Limitar daño por golpe individual (máximo 15.0F = 0.5% de vida) para evitar que lo destrocen en segundos
        float cappedAmount = Math.min(amount, 15.0F);

        boolean damaged = super.hurt(source, cappedAmount);
        if (damaged) {
            // Sonido canónico de cristal resonante e impacto de piedra
            this.level().playSound(null, this.getX(), this.getY() + 1.5, this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 1.2F, 0.8F);
            this.level().playSound(null, this.getX(), this.getY() + 1.5, this.getZ(),
                    SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE, 1.0F, 0.6F);

            if (this.level() instanceof ServerLevel sl) {
                // Chispas celestes al recibir impacto
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY() + 1.8, this.getZ(),
                        12, 0.6, 0.8, 0.6, 0.1);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 1.2, this.getZ(),
                        6, 0.4, 0.5, 0.4, 0.02);
            }

            // Actualizar porcentaje de vida en la etiqueta holográfica
            float pct = Math.max(0.0F, this.getHealth() / this.getMaxHealth());
            String color = pct >= 0.6F ? "§a" : (pct >= 0.3F ? "§e" : "§c");
            this.setCustomName(Component.literal("§b§l✦ Monolito: " + color + (int)(pct * 100) + "% §b✦"));
        }
        return damaged;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) {
            // Destrucción catastrófica del monolito
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5, this.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 80, 1.5, 1.5, 1.5, 0.2);
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.5, this.getZ(), 3, 0, 0, 0, 0);
            sl.playSound(null, this.getX(), this.getY() + 1.5, this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
            sl.playSound(null, this.getX(), this.getY() + 1.5, this.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0F, 0.8F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.invulnTicks > 0) {
            this.invulnTicks--;
        }

        if (this.level().isClientSide) {
            // Partículas cian y runas que flotan alrededor del monolito
            double angle = (this.tickCount * 0.06D);
            double radius = 1.3D;
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            double py = this.getY() + 0.6D + (this.tickCount % 60) * 0.035D;

            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0.0D, 0.02D, 0.0D);
            if (this.random.nextFloat() < 0.2F) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        this.getY() + 1.0D + this.random.nextDouble() * 2.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        0.0D, 0.03D, 0.0D);
            }
        } else {
            // Pulso de provocación / aggro hacia los monstruos del Abismo
            this.aggroPulseCooldown--;
            if (this.aggroPulseCooldown <= 0) {
                this.aggroPulseCooldown = 20;
                pulseAggroAura();
            }
        }
    }

    private void pulseAggroAura() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        AABB aggroBox = this.getBoundingBox().inflate(36.0D, 12.0D, 36.0D);
        List<Mob> hostiles = sl.getEntitiesOfClass(Mob.class, aggroBox,
                m -> m.isAlive() && !m.getTags().contains("abyss_defense_target") && !m.getTags().contains("abyss_friendly"));

        for (Mob mob : hostiles) {
            // Si el monstruo ya está peleando contra un jugador o aliado, ¡NUNCA robarle el aggro!
            if (mob.getTarget() instanceof net.minecraft.world.entity.player.Player ||
                (mob.getTarget() != null && mob.getTarget().getTags().contains("abyss_friendly"))) {
                continue;
            }
            // Solo los monstruos sin objetivo se dirigen hacia el monolito
            if (mob.getTarget() == null) {
                mob.setTarget(this);
            }
        }
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
