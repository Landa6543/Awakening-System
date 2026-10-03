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
                "§9§l[PIEDRA DE RUNA: AUTORIDAD DEL GOBERNANTE]",
                "§7Clic Derecho: §fDesata la telequinesis del Gobernante. §bAtrae enemigos hacia ti §f(o agáchate para §bempujarlos violentamente§f con una onda de gravedad). (6s CD)",
                "rulers_authority"
        ),
        DRAGON_EYES(
                "§4§l[PIEDRA DE RUNA: OJOS DRACÓNICOS]",
                "§7Clic Derecho: §fDespierta la visión dracónica. §cMuestra vida exacta, nivel y armadura §fde cualquier objetivo que mires en tu HUD.",
                "dragon_eyes"
        ),
        BLOODTHIRST(
                "§c§l[PIEDRA DE RUNA: SED DE SANGRE]",
                "§7Clic Derecho: §fAsimila la runa marcial. Otorga §4Robo de Vida pasivo permanente §f(+15% del daño infligido devuelto como salud).",
                "bloodthirst"
        ),
        SHADOW_STEP(
                "§5§l[PIEDRA DE RUNA: PASO SOMBRÍO]",
                "§7Clic Derecho: §fTe desvaneces en las sombras y §dreapareces tras la espalda del objetivo §fcon golpe crítico e invisibilidad. (8s CD)",
                "shadow_step"
        ),
        FOURTH_WALL(
                "§f§l[PIEDRA DE RUNA: LA CUARTA PARED]",
                "§7Clic Derecho: §fInscribe la barrera psicológica de Kim Dokja. §fInmune a ceguera, oscuridad y previene la muerte. (5m CD)",
                "fourth_wall"
        ),
        IRON_BODY(
                "§7§l[PIEDRA DE RUNA: CUERPO DE HIERRO]",
                "§7Clic Derecho: §fEndurece tu cuerpo como acero templado. Otorga §e+4 de Armadura pasiva permanente §fy resistencia a empuje.",
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
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            if (runeType == RuneType.RULERS_AUTHORITY) {
                long gameTime = level.getGameTime();
                long cd = sp.getPersistentData().getLong("rulers_auth_cd");
                if (gameTime < cd) {
                    long sec = (cd - gameTime) / 20;
                    sp.displayClientMessage(Component.translatable("awakening_system.rune_stone.cooldown", sec), true);
                    return InteractionResultHolder.fail(stack);
                }
                sp.getPersistentData().putLong("rulers_auth_cd", gameTime + 120L); // 6s CD

                Vec3 look = sp.getLookAngle();
                boolean push = sp.isShiftKeyDown();

                if (level instanceof ServerLevel sl) {
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 1.2F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.8F);

                    AABB box = sp.getBoundingBox().inflate(10.0D, 4.0D, 10.0D);
                    for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                        if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                            if (push) {
                                // Repel
                                Vec3 force = nearby.position().subtract(sp.position()).normalize().scale(1.8D);
                                nearby.setDeltaMovement(force.x, 0.5D, force.z);
                                nearby.hurt(nearby.damageSources().playerAttack(sp), 12.0F);
                                sl.sendParticles(ParticleTypes.SONIC_BOOM, nearby.getX(), nearby.getY() + 0.5D, nearby.getZ(), 1, 0, 0, 0, 0);
                            } else {
                                // Pull towards player
                                Vec3 force = sp.position().subtract(nearby.position()).normalize().scale(1.4D);
                                nearby.setDeltaMovement(force.x, 0.35D, force.z);
                                nearby.hurt(nearby.damageSources().playerAttack(sp), 8.0F);
                                sl.sendParticles(ParticleTypes.PORTAL, nearby.getX(), nearby.getY() + 0.5D, nearby.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
                            }
                        }
                    }
                }
                sp.displayClientMessage(Component.translatable(push ? "awakening_system.rune_stone.rulers_gravity_wave" : "awakening_system.rune_stone.rulers_attraction"), true);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            // Consumable skill unlock for the other runes:
            if (!data.hasSkill(runeType.skillId)) {
                data.unlockSkill(runeType.skillId);
                data.enableSkill(runeType.skillId);
                stack.shrink(1);
                if (level instanceof ServerLevel sl) {
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 1.0F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.2D);
                }
                AwakeningSystemMod.notify(sp, "awakening_system.rune_stone.skill_awakened_title",
                        "awakening_system.rune_stone.skill_awakened_msg",
                        0xFF50FA7B,
                        Component.translatable("awakening_system.rune_stone." + runeType.name().toLowerCase() + ".header").getString());
                AwakeningSystemMod.sync(sp);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.rune_stone.already_known"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.rune_stone." + runeType.name().toLowerCase() + ".header"));
        tooltip.add(Component.translatable("awakening_system.rune_stone." + runeType.name().toLowerCase() + ".desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
