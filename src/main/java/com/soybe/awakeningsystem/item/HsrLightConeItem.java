package com.soybe.awakeningsystem.item;

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

import java.util.List;

/**
 * Conos de Luz auténticos de Honkai: Star Rail (3★ y 4★).
 * Cada cono pertenece a una Vía (Cacería, Abundancia, Conservación, Erudición, Armonía, Nihilidad, Destrucción)
 * y posee efectos activos balanceados con efectos visuales y sonoros cósmicos.
 */
public class HsrLightConeItem extends Item {
    private final String coneId;
    private final String coneDisplayName;
    private final int stars;
    private final String pathName;
    private final String lore;
    private final String skillName;
    private final String skillDesc;

    public HsrLightConeItem(String coneId, String coneDisplayName, int stars, String pathName, String lore, String skillName, String skillDesc) {
        super(new Properties().stacksTo(stars == 3 ? 16 : 1).rarity(stars == 4 ? Rarity.UNCOMMON : Rarity.COMMON));
        this.coneId = coneId;
        this.coneDisplayName = coneDisplayName;
        this.stars = stars;
        this.pathName = pathName;
        this.lore = lore;
        this.skillName = skillName;
        this.skillDesc = skillDesc;
    }

    public String getConeId() { return coneId; }
    public String getConeDisplayName() { return coneDisplayName; }
    public int getStars() { return stars; }
    public String getPathName() { return pathName; }
    public String getLore() { return lore; }
    public String getSkillName() { return skillName; }
    public String getSkillDesc() { return skillDesc; }

    @Override
    public Component getName(ItemStack stack) {
        String key = this.getDescriptionId(stack);
        return Component.translatableWithFallback(key, this.coneDisplayName);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            String cdKey = "hsr_cone_cd_" + coneId;
            long cdUntil = sp.getPersistentData().getLong(cdKey);
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.translatable("item.awakening_system.hsr_light_cone.cd_msg", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong(cdKey, gameTime + (stars == 4 ? 280L : 200L));

            switch (pathName.toLowerCase(java.util.Locale.ROOT)) {
                case "hunt" -> {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, stars == 4 ? 300 : 200, stars == 4 ? 2 : 1));
                    sp.addEffect(new MobEffectInstance(MobEffects.JUMP, stars == 4 ? 300 : 200, 1));
                }
                case "abundance" -> {
                    sp.heal(stars == 4 ? 8.0F : 4.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, stars == 4 ? 200 : 120, stars == 4 ? 1 : 0));
                }
                case "preservation" -> {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, stars == 4 ? 300 : 200, stars == 4 ? 2 : 1));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, stars == 4 ? 400 : 240, stars == 4 ? 1 : 0));
                }
                case "erudition" -> {
                    sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, stars == 4 ? 300 : 200, stars == 4 ? 2 : 1));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, stars == 4 ? 240 : 160, 0));
                }
                case "harmony" -> {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0));
                    AABB box = sp.getBoundingBox().inflate(12.0D);
                    List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != sp && e.isAlive() && (e instanceof Player || e.getTags().contains("companion_entity")));
                    for (LivingEntity ally : allies) {
                        ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                    }
                }
                case "nihility" -> {
                    AABB box = sp.getBoundingBox().inflate(stars == 4 ? 12.0D : 8.0D);
                    List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                    for (LivingEntity enemy : enemies) {
                        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
                        enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, stars == 4 ? 1 : 0));
                        if (stars == 4) enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
                    }
                }
                case "destruction" -> {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, stars == 4 ? 300 : 200, stars == 4 ? 1 : 0));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, stars == 4 ? 240 : 160, 0));
                }
                default -> sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.08D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.5F);
            }

            Component coneNameComp = Component.translatableWithFallback(this.getDescriptionId(stack), coneDisplayName);
            Component pathComp = Component.translatableWithFallback("item.awakening_system.hsr_path." + pathName.toLowerCase(), pathName);
            sp.displayClientMessage(Component.translatable("item.awakening_system.hsr_light_cone.activate_msg", coneNameComp, pathComp), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starColor = (stars == 4) ? "§d★★★★" : "§b★★★";
        Component rarityTag = Component.translatable(stars == 4 ? "item.awakening_system.hsr_light_cone.rarity_4" : "item.awakening_system.hsr_light_cone.rarity_3");
        Component path = Component.translatable("item.awakening_system.hsr_path." + pathName.toLowerCase());
        Component pathLabel = Component.translatable("item.awakening_system.hsr_light_cone.path_label");
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.header", starColor, rarityTag, pathLabel, path).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".lore", "§7\"" + lore + "\"").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.skill_header", Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".skill_name", skillName)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  ").append(Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".skill_desc", "§7" + skillDesc)).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
