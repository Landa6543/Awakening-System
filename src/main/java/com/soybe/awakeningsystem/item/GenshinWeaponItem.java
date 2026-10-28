package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;

public class GenshinWeaponItem extends SwordItem implements AwakeningWeaponItem {

    public enum WeaponType {
        ENGULFING_LIGHTNING("engulfing_lightning", "Engulfing Lightning", 5, 13, -2.4F, "Sacred naginata of the Electro Archon"),
        PRIMORDIAL_JADE_WINGED_SPEAR("primordial_jade_winged_spear", "Primordial Jade Winged-Spear", 5, 14, -2.5F, "A jade polearm forged for the Conqueror of Demons"),
        CRIMSON_MOONS_SEMBLANCE("crimson_moons_semblance", "Crimson Moon's Semblance", 5, 15, -2.8F, "Crimson scythe of the Knave of the Fatui Harbingers"),
        VORTEX_VANQUISHER("vortex_vanquisher", "Vortex Vanquisher", 5, 14, -2.6F, "A mythical polearm capable of cleaving the mountains of Liyue"),
        THE_CATCH("the_catch", "\"The Catch\"", 4, 10, -2.4F, "A legendary fishing trident imbued with tremendous energetic charge"),
        BLACK_TASSEL("black_tassel", "Black Tassel", 3, 8, -2.5F, "A traditional hunter's spear with a reinforced tip lethal against slimes"),

        WOLFS_GRAVESTONE("wolfs_gravestone", "Wolf's Gravestone", 5, 16, -3.0F, "A pure steel greatsword tempered with the soul of the King of the Wind"),
        REDHORN_STONETHRESHER("redhorn_stonethresher", "Redhorn Stonethresher", 5, 15, -3.0F, "A golden-horned greatsword wielded by the supreme leader of the Arataki Gang"),
        VERDICT("verdict", "Verdict", 5, 16, -3.1F, "Golden ceremonial axe of the President of Spina di Rosula"),

        SKYWARD_BLADE("skyward_blade", "Skyward Blade", 5, 13, -2.2F, "A sword symbolizing the honor of the Knights of Favonius and the blessing of the Dragon of the Wind"),
        MISTSPLITTER_REFORGED("mistsplitter_reforged", "Mistsplitter Reforged", 5, 13, -2.2F, "A sword that blazes with the light of a cloudless night"),
        PRIMORDIAL_JADE_CUTTER("primordial_jade_cutter", "Primordial Jade Cutter", 5, 12, -2.2F, "A ceremonial blade meticulously carved from pure jade"),
        FREEDOM_SWORN("freedom_sworn", "Freedom-Sworn", 5, 12, -2.2F, "A straight sword echoing the Wind's everlasting oath of freedom"),
        KEY_OF_KHAJ_NISUT("key_of_khaj_nisut", "Key of Khaj-Nisut", 5, 12, -2.3F, "A ceremonial sword once wielded by the desert priests of Sumeru"),
        ABSOLUTION("absolution", "Absolution", 5, 13, -2.2F, "A supreme rapier imbued with the judgment of the Covenant of the End"),
        THE_FLUTE("the_flute", "The Flute", 4, 10, -2.4F, "A lightweight sword with harmonic holes that sings melodies as it swings"),
        THE_BLACK_SWORD("the_black_sword", "The Black Sword", 4, 11, -2.3F, "A bloodstained sword yearning for slaughter that heals its wielder"),
        FLEUVE_CENDRE_FERRYMAN("fleuve_cendre_ferryman", "Fleuve Cendre Ferryman", 4, 9, -2.3F, "A handy iron pipe tool of clandestine pipefitters"),

        HERTA_HAMMER("herta_hammer", "Herta's Hammer", 5, 15, -3.0F, "Colossal hammer of Genius Society Member #83. Kuru Kuru!"),

        QINGMING_SWORD("qingming_sword", "Qingming Sword", 5, 14, -2.4F, "High-frequency ether-blade forged in New Eridu"),
        TAILLESS("tailless", "Tailless Claymore", 5, 15, -2.8F, "Heavy cybernetic greatsword deployed by elite special agents"),
        SHUNGUANG_SWORD("shunguang_sword", "Shunguang Sword", 5, 13, -2.2F, "Electrified katana with lightning-fast reactive cutting edge"),
        SHIYUAN_SWORD("shiyuan_sword", "Shiyuan Sword", 4, 10, -2.3F, "Balanced combat sword issued to Commission inspectors"),

        DOMAIN_OF_SANCTION("domain_of_sanction", "Domain of Sanction", 5, 14, -2.2F, "Sacred katana forged with the authority and fury of divine thunder");

        public final String id;
        public final String displayName;
        public final int stars;
        public final int attackDamage;
        public final float attackSpeed;
        public final String lore;

        WeaponType(String id, String displayName, int stars, int attackDamage, float attackSpeed, String lore) {
            this.id = id;
            this.displayName = displayName;
            this.stars = stars;
            this.attackDamage = attackDamage;
            this.attackSpeed = attackSpeed;
            this.lore = lore;
        }
    }

    private final WeaponType weaponType;
    private static final ResourceLocation JADE_CUTTER_HP_MOD = ResourceLocation.fromNamespaceAndPath("awakening_system", "jade_cutter_hp");
    private static final ResourceLocation BLACK_TASSEL_HP_MOD = ResourceLocation.fromNamespaceAndPath("awakening_system", "black_tassel_hp");

    public GenshinWeaponItem(WeaponType type, Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(type.stars == 5 ? Rarity.EPIC : Rarity.RARE)
                .fireResistant()
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, type.attackDamage, type.attackSpeed)));
        this.weaponType = type;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public static int getRefinement(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("Refinement")) {
            return Math.max(1, Math.min(5, cd.copyTag().getInt("Refinement")));
        }
        return 1;
    }

    public static void setRefinement(ItemStack stack, int rank) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        tag.putInt("Refinement", Math.max(1, Math.min(5, rank)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (sp.isShiftKeyDown()) {
                ItemStack offHand = sp.getOffhandItem();
                if (offHand.getItem() instanceof GenshinWeaponItem other && other.getWeaponType() == this.weaponType) {
                    int currentRank = getRefinement(stack);
                    if (currentRank >= 5) {
                        sp.displayClientMessage(Component.translatable("item.awakening_system.genshin_weapon.max_refinement"), true);
                        return InteractionResultHolder.fail(stack);
                    }
                    int costMora = (weaponType.stars == 5 ? 30000 : 15000) * currentRank;
                    SystemData data = SystemEvents.data(sp);
                    if (data.mora() >= costMora) {
                        data.spendMora(costMora);
                        offHand.shrink(1);
                        int nextRank = currentRank + 1;
                        setRefinement(stack, nextRank);

                        ServerLevel sl = (ServerLevel) level;
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.4D, 0.6D, 0.4D, 0.15D);
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.2F, 1.1F);
                        AwakeningSystemMod.sync(sp);

                        sp.displayClientMessage(Component.translatable("item.awakening_system.genshin_weapon.refinement_success", Component.translatable(stack.getDescriptionId()), nextRank, String.format(java.util.Locale.US, "%,d", costMora)), false);
                        return InteractionResultHolder.consume(stack);
                    } else {
                        sp.displayClientMessage(Component.translatable("item.awakening_system.genshin_weapon.insufficient_mora", String.format(java.util.Locale.US, "%,d", costMora), (currentRank + 1)), true);
                        return InteractionResultHolder.consume(stack);
                    }
                }
            }

            long gameTime = level.getGameTime();
            String cdKey = "genshin_skill_cd_" + weaponType.id;
            long cdUntil = sp.getPersistentData().getLong(cdKey);

            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("item.awakening_system.genshin_weapon.cooldown", Component.translatable(stack.getDescriptionId()), sec), true);
                return InteractionResultHolder.fail(stack);
            }

            executeActiveSkill(sp, (ServerLevel) level, gameTime, cdKey);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void executeActiveSkill(ServerPlayer sp, ServerLevel sl, long gameTime, String cdKey) {
        int cdTicks = switch (weaponType) {
            case ENGULFING_LIGHTNING, WOLFS_GRAVESTONE -> 320;
            case CRIMSON_MOONS_SEMBLANCE, VORTEX_VANQUISHER, REDHORN_STONETHRESHER -> 280;
            case PRIMORDIAL_JADE_WINGED_SPEAR, VERDICT, PRIMORDIAL_JADE_CUTTER, FREEDOM_SWORN, SKYWARD_BLADE, THE_BLACK_SWORD, HERTA_HAMMER, TAILLESS, DOMAIN_OF_SANCTION -> 240;
            case KEY_OF_KHAJ_NISUT -> 300;
            case MISTSPLITTER_REFORGED, THE_CATCH, THE_FLUTE, QINGMING_SWORD, SHIYUAN_SWORD -> 200;
            case ABSOLUTION, FLEUVE_CENDRE_FERRYMAN, BLACK_TASSEL, SHUNGUANG_SWORD -> 160;
        };
        sp.getCooldowns().addCooldown(this, cdTicks);

        switch (weaponType) {
            case ENGULFING_LIGHTNING -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 320L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(10.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().lightningBolt(), 14.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.4F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.engulfing_lightning.active_msg"), true);
            }

            case PRIMORDIAL_JADE_WINGED_SPEAR -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                if (sp.onGround()) {
                    sp.setDeltaMovement(sp.getDeltaMovement().x, 1.35D, sp.getDeltaMovement().z);
                } else {
                    sp.setDeltaMovement(sp.getDeltaMovement().x, -1.2D, sp.getDeltaMovement().z);
                }
                sp.hurtMarked = true;
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));

                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY(), sp.getZ(), 30, 0.8D, 0.5D, 0.8D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.4F, 1.1F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.primordial_jade_winged_spear.active_msg"), true);
            }

            case CRIMSON_MOONS_SEMBLANCE -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 280L);
                AABB area = sp.getBoundingBox().inflate(6.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                float totalHeal = 0.0F;
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().wither(), 16.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 25, 0.5D, 0.8D, 0.5D, 0.05D);
                    totalHeal += 3.0F;
                }
                sp.heal(Math.min(totalHeal, 14.0F));
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 0.7F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.9F, 1.5F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.crimson_moons_semblance.active_msg"), true);
            }

            case VORTEX_VANQUISHER -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 280L);
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(7.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().playerAttack(sp), 10.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 4));
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
                    sl.sendParticles(ParticleTypes.CRIT, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.8F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.vortex_vanquisher.active_msg"), true);
            }

            case WOLFS_GRAVESTONE -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 320L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(12.0D);
                List<Player> allies = sl.getEntitiesOfClass(Player.class, area, p -> p.isAlive());
                for (Player ally : allies) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, true, true));
                }
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.08D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.6F, 0.8F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.wolfs_gravestone.active_msg"), true);
            }

            case REDHORN_STONETHRESHER -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 280L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, false, false));

                sl.sendParticles(ParticleTypes.LAVA, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.4F, 0.9F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.redhorn_stonethresher.active_msg"), true);
            }

            case VERDICT -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                Vec3 look = sp.getLookAngle();
                AABB area = sp.getBoundingBox().expandTowards(look.scale(8.0D)).inflate(2.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().explosion(sp, sp), 18.0F);
                    enemy.knockback(1.5D, -look.x, -look.z);
                    sl.sendParticles(ParticleTypes.EXPLOSION, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.verdict.active_msg"), true);
            }

            case MISTSPLITTER_REFORGED -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2, false, true, true));

                Vec3 look = sp.getLookAngle();
                for (int step = 1; step <= 8; step++) {
                    Vec3 pos = sp.getEyePosition().add(look.scale(step));
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.x, pos.y, pos.z, 8, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 4, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                AABB area = sp.getBoundingBox().expandTowards(look.scale(8.0D)).inflate(1.5D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().freeze(), 15.0F);
                    enemy.setTicksFrozen(140);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.6F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.mistsplitter_reforged.active_msg"), true);
            }

            case PRIMORDIAL_JADE_CUTTER -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.removeAllEffects();
                sp.heal(sp.getMaxHealth() * 0.35F);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true, true));

                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.6D, 0.8D, 0.6D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.6F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.primordial_jade_cutter.active_msg"), true);
            }

            case FREEDOM_SWORN -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(8.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    Vec3 pull = sp.position().subtract(enemy.position()).normalize().scale(0.8D);
                    enemy.setDeltaMovement(pull.x, 0.3D, pull.z);
                    enemy.hurtMarked = true;
                    enemy.hurt(sp.damageSources().playerAttack(sp), 8.0F);
                    sl.sendParticles(ParticleTypes.POOF, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.4F, 1.4F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.freedom_sworn.active_msg"), true);
            }

            case KEY_OF_KHAJ_NISUT -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 300L);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 180, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));

                sl.sendParticles(ParticleTypes.SPLASH, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.DRIPPING_WATER, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.4F, 1.0F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.key_of_khaj_nisut.active_msg"), true);
            }

            case ABSOLUTION -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 160L);
                Vec3 look = sp.getLookAngle();
                sp.setDeltaMovement(look.scale(1.8D).add(0, 0.2D, 0));
                sp.hurtMarked = true;

                AABB area = sp.getBoundingBox().expandTowards(look.scale(7.0D)).inflate(1.5D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().playerAttack(sp), 15.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 1.8F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.absolution.active_msg"), true);
            }

            case THE_CATCH -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(35);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, false, true, true));
                sl.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.the_catch.active_msg"), true);
                AwakeningSystemMod.sync(sp);
            }

            case FLEUVE_CENDRE_FERRYMAN -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 160L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(25);
                sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 140, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, false, true, true));
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.5F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.fleuve_cendre_ferryman.active_msg"), true);
                AwakeningSystemMod.sync(sp);
            }

            case SKYWARD_BLADE -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(6.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().playerAttack(sp), 12.0F);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.POOF, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.skyward_blade.active_msg"), true);
            }

            case THE_FLUTE -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, true, true));

                AABB area = sp.getBoundingBox().inflate(5.5D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    Vec3 push = enemy.position().subtract(sp.position()).normalize().scale(1.2D).add(0, 0.2D, 0);
                    enemy.setDeltaMovement(push);
                    enemy.hurtMarked = true;
                    enemy.hurt(sp.damageSources().playerAttack(sp), 9.0F);
                    sl.sendParticles(ParticleTypes.NOTE, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 6, 0.3D, 0.4D, 0.3D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.6F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.6F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.the_flute.active_msg"), true);
            }

            case THE_BLACK_SWORD -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true, true));

                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8F, 1.6F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.the_black_sword.active_msg"), true);
            }

            case BLACK_TASSEL -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 160L);
                Vec3 look = sp.getLookAngle();
                sp.setDeltaMovement(look.scale(1.3D).add(0, 0.1D, 0));
                sp.hurtMarked = true;

                AABB area = sp.getBoundingBox().expandTowards(look.scale(5.0D)).inflate(1.2D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    float dmg = (enemy instanceof net.minecraft.world.entity.monster.Slime || enemy instanceof net.minecraft.world.entity.monster.MagmaCube) ? 24.0F : 9.0F;
                    enemy.hurt(sp.damageSources().playerAttack(sp), dmg);
                    sl.sendParticles(ParticleTypes.CRIT, enemy.getX(), enemy.getY() + 0.8D, enemy.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.black_tassel.active_msg"), true);
            }

            case HERTA_HAMMER -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(6.5D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().playerAttack(sp), 16.0F);
                    enemy.setTicksFrozen(120);
                    Vec3 knock = enemy.position().subtract(sp.position()).normalize().scale(1.4D).add(0, 0.4D, 0);
                    enemy.setDeltaMovement(knock);
                    enemy.hurtMarked = true;
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                }
                net.minecraft.sounds.SoundEvent kuruSound = sp.getRandom().nextBoolean()
                        ? com.soybe.awakeningsystem.ModSounds.HERTA_KURU.get()
                        : com.soybe.awakeningsystem.ModSounds.HERTA_KURURIN.get();
                sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), kuruSound, SoundSource.VOICE, 2.0F, 1.0F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.HERTA_SWING.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.herta_hammer.active_msg"), true);
            }

            case QINGMING_SWORD -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                Vec3 look = sp.getLookAngle();
                sp.setDeltaMovement(look.scale(1.6D).add(0, 0.15D, 0));
                sp.hurtMarked = true;

                AABB area = sp.getBoundingBox().expandTowards(look.scale(8.0D)).inflate(1.8D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().freeze(), 15.0F);
                    enemy.setTicksFrozen(140);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, enemy.getX(), enemy.getY() + 0.8D, enemy.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.05D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.6F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.qingming_sword.active_msg"), true);
            }

            case TAILLESS -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, true, true));

                AABB area = sp.getBoundingBox().inflate(6.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().playerAttack(sp), 18.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                    sl.sendParticles(ParticleTypes.EXPLOSION, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 1, 0, 0, 0, 0);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 0.6F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.tailless.active_msg"), true);
            }

            case SHUNGUANG_SWORD -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 160L);
                Vec3 look = sp.getLookAngle();
                Vec3 targetPos = sp.position().add(look.scale(7.0D));
                sp.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 2, false, true, true));

                AABB area = sp.getBoundingBox().inflate(4.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().lightningBolt(), 14.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.1D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.8F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.shunguang_sword.active_msg"), true);
            }

            case SHIYUAN_SWORD -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(30);
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0, false, true, true));

                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.shiyuan_sword.active_msg"), true);
                AwakeningSystemMod.sync(sp);
            }

            case DOMAIN_OF_SANCTION -> {
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true, true));

                AABB area = sp.getBoundingBox().inflate(6.5D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().lightningBolt(), 20.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true, true));
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
                sp.playNotifySound(com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.4F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.domain_of_sanction.active_msg"), true);
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && player.level() instanceof ServerLevel sl) {
            switch (weaponType) {
                case ENGULFING_LIGHTNING -> {
                    target.hurt(attacker.damageSources().magic(), 4.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                    if (player instanceof ServerPlayer sp) {
                        SystemEvents.data(sp).restoreEnergy(5);
                    }
                }
                case PRIMORDIAL_JADE_WINGED_SPEAR -> {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.02D);
                }
                case CRIMSON_MOONS_SEMBLANCE -> {
                    player.heal(1.5F);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 0.8D, target.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.05D);
                }
                case WOLFS_GRAVESTONE -> {
                    if (target.getHealth() <= target.getMaxHealth() * 0.30F) {
                        target.hurt(attacker.damageSources().playerAttack(player), 12.0F);
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, true, true));
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                    }
                }
                case REDHORN_STONETHRESHER -> {
                    int armor = player.getArmorValue();
                    float extraDmg = Math.min(18.0F, armor * 0.45F);
                    target.hurt(attacker.damageSources().playerAttack(player), extraDmg);
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                }
                case VERDICT -> {
                    long count = player.getPersistentData().getLong("verdict_hits") + 1;
                    player.getPersistentData().putLong("verdict_hits", count);
                    if (count % 3 == 0) {
                        target.hurt(attacker.damageSources().explosion(player, player), 8.0F);
                        sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.01D);
                    }
                }
                case MISTSPLITTER_REFORGED -> {
                    target.setTicksFrozen(60);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.02D);
                }
                case PRIMORDIAL_JADE_CUTTER -> {
                    float bonus = player.getMaxHealth() * 0.08F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
                case FREEDOM_SWORN -> {
                    sl.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.5D, target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                case SKYWARD_BLADE -> {
                    target.hurt(attacker.damageSources().magic(), 4.0F);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                    sl.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.5D, target.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                case THE_FLUTE -> {
                    long chords = player.getPersistentData().getLong("the_flute_chords") + 1;
                    player.getPersistentData().putLong("the_flute_chords", chords);
                    sl.sendParticles(ParticleTypes.NOTE, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.2D, 0.3D, 0.2D, 0.02D);
                    if (chords % 5 == 0) {
                        AABB noteArea = target.getBoundingBox().inflate(3.5D);
                        List<LivingEntity> near = sl.getEntitiesOfClass(LivingEntity.class, noteArea, e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                        for (LivingEntity e : near) {
                            e.hurt(attacker.damageSources().playerAttack(player), 7.0F);
                            sl.sendParticles(ParticleTypes.NOTE, e.getX(), e.getY() + 0.8D, e.getZ(), 6, 0.3D, 0.4D, 0.3D, 0.05D);
                        }
                        sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.5F, 1.8F);
                    }
                }
                case THE_BLACK_SWORD -> {
                    player.heal(1.5F);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                    if (target.getHealth() <= target.getMaxHealth() * 0.4F) {
                        player.heal(2.0F);
                    }
                }
                case BLACK_TASSEL -> {
                    if (target instanceof net.minecraft.world.entity.monster.Slime || target instanceof net.minecraft.world.entity.monster.MagmaCube) {
                        target.hurt(attacker.damageSources().playerAttack(player), 18.0F);
                        sl.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 0.5D, target.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
                    }
                }
                case THE_CATCH, FLEUVE_CENDRE_FERRYMAN -> {
                    if (player instanceof ServerPlayer sp) {
                        SystemEvents.data(sp).restoreEnergy(4);
                    }
                }
                case HERTA_HAMMER -> {
                    target.setTicksFrozen(60);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.02D);
                    if (target.getHealth() <= target.getMaxHealth() * 0.5F) {
                        target.hurt(attacker.damageSources().playerAttack(player), 6.0F);
                        sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.HERTA_HIT.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
                    }
                }
                case QINGMING_SWORD -> {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                case TAILLESS -> {
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                case SHUNGUANG_SWORD -> {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                }
                case SHIYUAN_SWORD -> {
                    if (player instanceof ServerPlayer sp) {
                        SystemEvents.data(sp).restoreEnergy(5);
                    }
                }
                case DOMAIN_OF_SANCTION -> {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.08D);
                    if (attacker.getRandom().nextFloat() < 0.35F) {
                        target.hurt(attacker.damageSources().lightningBolt(), 5.0F);
                        sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8F, 1.8F);
                    }
                }
                default -> {}
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (selected && entity instanceof LivingEntity living && weaponType == WeaponType.PRIMORDIAL_JADE_CUTTER) {
            var maxHpAttr = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && !maxHpAttr.hasModifier(JADE_CUTTER_HP_MOD)) {
                maxHpAttr.addTransientModifier(new AttributeModifier(JADE_CUTTER_HP_MOD, 20.0D, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (!selected && entity instanceof LivingEntity living && weaponType == WeaponType.PRIMORDIAL_JADE_CUTTER) {
            var maxHpAttr = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && maxHpAttr.hasModifier(JADE_CUTTER_HP_MOD)) {
                maxHpAttr.removeModifier(JADE_CUTTER_HP_MOD);
            }
        }

        if (selected && entity instanceof LivingEntity living && weaponType == WeaponType.BLACK_TASSEL) {
            var maxHpAttr = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && !maxHpAttr.hasModifier(BLACK_TASSEL_HP_MOD)) {
                maxHpAttr.addTransientModifier(new AttributeModifier(BLACK_TASSEL_HP_MOD, 4.0D, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (!selected && entity instanceof LivingEntity living && weaponType == WeaponType.BLACK_TASSEL) {
            var maxHpAttr = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && maxHpAttr.hasModifier(BLACK_TASSEL_HP_MOD)) {
                maxHpAttr.removeModifier(BLACK_TASSEL_HP_MOD);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starKey = switch (weaponType.stars) {
            case 5 -> "item.awakening_system.genshin_weapon.star5";
            case 4 -> "item.awakening_system.genshin_weapon.star4";
            default -> "item.awakening_system.genshin_weapon.star3";
        };
        tooltip.add(Component.literal("§6").append(Component.translatable(starKey)).withStyle(weaponType.stars == 5 ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE));
        int rank = getRefinement(stack);
        tooltip.add(Component.translatable("item.awakening_system.genshin_weapon.refine_rank", rank));
        if (rank < 5) {
            long cost = (weaponType.stars >= 5 ? 30000L : 15000L) * rank;
            tooltip.add(Component.translatable("item.awakening_system.genshin_weapon.refine_hint", String.format("%,d", cost), rank + 1));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.genshin_weapon.refine_max"));
        }
        tooltip.add(Component.translatableWithFallback("item.awakening_system.genshin_weapon." + weaponType.id + ".lore", "§7" + weaponType.lore).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.awakening_system.genshin_weapon." + weaponType.id + ".active.title"));
        tooltip.add(Component.translatable("item.awakening_system.genshin_weapon." + weaponType.id + ".active.desc"));

        if (hasPassive(weaponType)) {
            tooltip.add(Component.translatable("item.awakening_system.genshin_weapon." + weaponType.id + ".passive.title"));
            tooltip.add(Component.translatable("item.awakening_system.genshin_weapon." + weaponType.id + ".passive.desc"));
        }
    }

    private boolean hasPassive(WeaponType type) {
        return switch (type) {
            case ENGULFING_LIGHTNING, PRIMORDIAL_JADE_WINGED_SPEAR, CRIMSON_MOONS_SEMBLANCE,
                 VORTEX_VANQUISHER, WOLFS_GRAVESTONE, REDHORN_STONETHRESHER, VERDICT,
                 SKYWARD_BLADE, MISTSPLITTER_REFORGED, PRIMORDIAL_JADE_CUTTER, FREEDOM_SWORN,
                 THE_FLUTE, THE_BLACK_SWORD, BLACK_TASSEL, HERTA_HAMMER, DOMAIN_OF_SANCTION -> true;
            default -> false;
        };
    }
}
