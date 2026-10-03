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
        // --- ARMAS DE ASTA (POLEARMS) ---
        ENGULFING_LIGHTNING("engulfing_lightning", "Luz del Segador", 5, 13, -2.4F, "Naginata sagrada de la Arconte Electro"),
        PRIMORDIAL_JADE_WINGED_SPEAR("primordial_jade_winged_spear", "Halcón de Jade", 5, 14, -2.5F, "Lanza de jade forjada para el Conquistador de Demonios"),
        CRIMSON_MOONS_SEMBLANCE("crimson_moons_semblance", "Semblanza de la Luna Carmesí", 5, 15, -2.8F, "Guadaña carmesí de la Sota de los Fatui"),
        VORTEX_VANQUISHER("vortex_vanquisher", "Rompe Rocas", 5, 14, -2.6F, "Lanza mítica capaz de hender las montañas de Liyue"),
        THE_CATCH("the_catch", "La Captura", 4, 10, -2.4F, "Tridente de pesca legendario con una tremenda carga energética"),
        BLACK_TASSEL("black_tassel", "Borla Negra", 3, 8, -2.5F, "Lanza de cazador tradicional con punta reforzada y letal contra slimes"),

        // --- MANDOBLES (CLAYMORES) ---
        WOLFS_GRAVESTONE("wolfs_gravestone", "Lápida del Lobo", 5, 16, -3.0F, "Mandoble de acero puro templado con el alma del Rey del Viento"),
        REDHORN_STONETHRESHER("redhorn_stonethresher", "Espadón Cornirrojo", 5, 15, -3.0F, "Espadón de cuerno dorado del líder supremo de la Banda de Arataki"),
        VERDICT("verdict", "Sentencia", 5, 16, -3.1F, "Hacha ceremonial dorada de la Presidenta de Spina di Rosula"),

        // --- ESPADAS LIGERAS (SWORDS) ---
        SKYWARD_BLADE("skyward_blade", "Hoja Afilada Celestial", 5, 13, -2.2F, "Espada que simboliza el honor del Caballero de Favonius y la bendición del Dragón del Viento"),
        MISTSPLITTER_REFORGED("mistsplitter_reforged", "Reflejo de las Tinieblas", 5, 13, -2.2F, "Espada que brilla con la luz de una noche sin nubes"),
        PRIMORDIAL_JADE_CUTTER("primordial_jade_cutter", "Cortador de Jade Primordial", 5, 12, -2.2F, "Hoja tallada meticulosamente a partir de jade puro"),
        FREEDOM_SWORN("freedom_sworn", "Juramento por la Libertad", 5, 12, -2.2F, "Espada tan recta como el juramento de libertad del Viento"),
        KEY_OF_KHAJ_NISUT("key_of_khaj_nisut", "Llave de la Coronación", 5, 12, -2.3F, "Espada ceremonial de los sacerdotes del desierto de Sumeru"),
        ABSOLUTION("absolution", "Expiación", 5, 13, -2.2F, "Florete supremo imbuido con el juicio del Pacto del Fin"),
        THE_FLUTE("the_flute", "La Espada de la Flauta", 4, 10, -2.4F, "Espada ligera con orificios armónicos que emite melodías al ser blandida"),
        THE_BLACK_SWORD("the_black_sword", "La Espada Negra", 4, 11, -2.3F, "Espada ensangrentada que anhela el caos del combate y regenera al portador"),
        FLEUVE_CENDRE_FERRYMAN("fleuve_cendre_ferryman", "Vado del Río Ceniciento", 4, 9, -2.3F, "Herramienta de hierro de los fontaneros clandestinos"),

        // --- HONKAI: STAR RAIL ---
        HERTA_HAMMER("herta_hammer", "Martillo de Herta", 5, 15, -3.0F, "Martillo colosal de la Miembro #83 de la Sociedad de Genios. ¡Kuru Kuru!"),

        // --- ZENLESS ZONE ZERO ---
        QINGMING_SWORD("qingming_sword", "Espada Qingming", 5, 14, -2.4F, "Espada de alta frecuencia con filo de éter animado de Nueva Eridu"),
        TAILLESS("tailless", "Espada Sin Cola (Tailless)", 5, 15, -2.8F, "Mandoble cibernético pesado utilizado por agentes de élite"),
        SHUNGUANG_SWORD("shunguang_sword", "Espada Shunguang", 5, 13, -2.2F, "Katana electrificada con filo relampagueante de reacción rápida"),
        SHIYUAN_SWORD("shiyuan_sword", "Espada Shiyuan", 4, 10, -2.3F, "Espada equilibrada de combate de los inspectores de la Comisión"),

        // --- HONKAI IMPACT 3RD ---
        DOMAIN_OF_SANCTION("domain_of_sanction", "Dominio de la Sanción", 5, 14, -2.2F, "Katana sagrada forjada con la autoridad y la furia del trueno divino");


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
            // 1. Refinamiento de arma con copia duplicada en mano secundaria y Mora
            if (sp.isShiftKeyDown()) {
                ItemStack offHand = sp.getOffhandItem();
                if (offHand.getItem() instanceof GenshinWeaponItem other && other.getWeaponType() == this.weaponType) {
                    int currentRank = getRefinement(stack);
                    if (currentRank >= 5) {
                        sp.displayClientMessage(Component.literal("§e✦ ¡Esta arma ya ha alcanzado el Rango Máximo de Refinamiento (R5)!"), true);
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

                        sp.displayClientMessage(Component.literal("§6§l✦ ¡REFINAMIENTO EXITOSO! §f" + weaponType.displayName + " §eha alcanzado el Rango §6R" + nextRank + " §f(-" + String.format("%,d", costMora) + " Mora)."), false);
                        return InteractionResultHolder.consume(stack);
                    } else {
                        sp.displayClientMessage(Component.literal("§cSaldo insuficiente de Mora. Necesitas §6" + String.format("%,d", costMora) + " Mora §cpara refinar a R" + (currentRank + 1) + "."), true);
                        return InteractionResultHolder.consume(stack);
                    }
                }
            }

            long gameTime = level.getGameTime();
            String cdKey = "genshin_skill_cd_" + weaponType.id;
            long cdUntil = sp.getPersistentData().getLong(cdKey);

            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§c§l✦ " + weaponType.displayName + " en enfriamiento: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            // Ejecutar habilidad activa según el arma
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
                // Musou Isshin (16s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 320L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true, true));

                // Rayos celestiales a enemigos cercanos
                AABB area = sp.getBoundingBox().inflate(10.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive() && !e.isAlliedTo(sp));
                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sp.damageSources().lightningBolt(), 14.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                }
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.4F);
                sp.displayClientMessage(Component.literal("§d§l✦ [LUZ DEL SEGADOR] §e¡SUEÑO ETERNO DE MUSOU ISSHIN ACTIVADO!"), true);
            }

            case PRIMORDIAL_JADE_WINGED_SPEAR -> {
                // Salto y Azote de Todo Mal (12s CD)
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
                sp.displayClientMessage(Component.literal("§a§l✦ [HALCÓN DE JADE] §f¡RUINA DE TODO MAL DESATADA!"), true);
            }

            case CRIMSON_MOONS_SEMBLANCE -> {
                // Banquete de la Luna Roja (14s CD)
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
                sp.displayClientMessage(Component.literal("§4§l✦ [SEMBLANZA DE LA LUNA CARMESÍ] §c¡CORTE DEL PACTO CARMESÍ!"), true);
            }

            case VORTEX_VANQUISHER -> {
                // Pilar de Geo y Petrificación (14s CD)
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
                sp.displayClientMessage(Component.literal("§6§l✦ [ROMPE ROCAS] §e¡ORDEN CELESTIAL Y DOMINIO TELÚRICO!"), true);
            }

            case WOLFS_GRAVESTONE -> {
                // Rugido del Lobo del Norte (16s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 320L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));

                // Buff a aliados en 12 bloques
                AABB area = sp.getBoundingBox().inflate(12.0D);
                List<Player> allies = sl.getEntitiesOfClass(Player.class, area, p -> p.isAlive());
                for (Player ally : allies) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, true, true));
                }
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.08D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.6F, 0.8F);
                sp.displayClientMessage(Component.literal("§c§l✦ [LÁPIDA DEL LOBO] §f¡AULLIDO DEL REY LOBO! (+40% ATQ ALIADOS)"), true);
            }

            case REDHORN_STONETHRESHER -> {
                // Furia del Gran Oni (14s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 280L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, false, false));

                sl.sendParticles(ParticleTypes.LAVA, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.4F, 0.9F);
                sp.displayClientMessage(Component.literal("§6§l✦ [ESPADÓN CORNIRROJO] §e¡FURIA DEL REY ONI DORADO ACTIVADA!"), true);
            }

            case VERDICT -> {
                // Salva Ceremonial de Spina di Rosula (12s CD)
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
                sp.displayClientMessage(Component.literal("§e§l✦ [SENTENCIA] §6¡DISPARO DE CAÑÓN CEREMONIAL EJECUTADO!"), true);
            }

            case MISTSPLITTER_REFORGED -> {
                // Corte de Niebla Trascendente (10s CD)
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
                sp.displayClientMessage(Component.literal("§9§l✦ [REFLEJO DE LAS TINIEBLAS] §b¡CORTE DE NIEBLA TRASCENDENTE!"), true);
            }

            case PRIMORDIAL_JADE_CUTTER -> {
                // Danza Esmeralda de Purificación (12s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.removeAllEffects();
                sp.heal(sp.getMaxHealth() * 0.35F);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true, true));

                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.6D, 0.8D, 0.6D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.6F, 1.2F);
                sp.displayClientMessage(Component.literal("§a§l✦ [CORTADOR DE JADE PRIMORDIAL] §f¡PURIFICACIÓN ESMERALDA COMPLETA!"), true);
            }

            case FREEDOM_SWORN -> {
                // Tempestad Céfira (12s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 1, false, true, true));

                // Atraer enemigos con un vórtice
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
                sp.displayClientMessage(Component.literal("§b§l✦ [JURAMENTO POR LA LIBERTAD] §f¡VÓRTICE CÉFIRO DE LA REBELIÓN!"), true);
            }

            case KEY_OF_KHAJ_NISUT -> {
                // Danza de la Inundación Sagrada (15s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 300L);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 180, 2, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));

                sl.sendParticles(ParticleTypes.SPLASH, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.DRIPPING_WATER, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.4F, 1.0F);
                sp.displayClientMessage(Component.literal("§3§l✦ [LLAVE DE LA CORONACIÓN] §b¡AGUAS CURATIVAS DEL OASIS DESATADAS!"), true);
            }

            case ABSOLUTION -> {
                // Caza de las Sombras / Paso Perforante (8s CD)
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
                sp.displayClientMessage(Component.literal("§d§l✦ [EXPIACIÓN] §f¡ESTOCADA FULGURANTE DE LOS DUELISTAS!"), true);
            }

            case THE_CATCH -> {
                // Carga del Pescador (10s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(35);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, false, true, true));
                sl.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.literal("§b§l✦ [LA CAPTURA] §f¡ENERGÍA ELEMENTAL RECARGADA (+35 MANÁ)!"), true);
                AwakeningSystemMod.sync(sp);
            }

            case FLEUVE_CENDRE_FERRYMAN -> {
                // Presión de Tubería Cenicienta (8s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 160L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(25);
                sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 140, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, false, true, true));
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.5F);
                sp.displayClientMessage(Component.literal("§7§l✦ [VADO DEL RÍO CENICIENTO] §f¡REGENERACIÓN Y VELOCIDAD CLANDESTINA!"), true);
                AwakeningSystemMod.sync(sp);
            }

            case SKYWARD_BLADE -> {
                // Poder Perforacielos (12s CD)
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
                sp.displayClientMessage(Component.literal("§b§l✦ [HOJA AFILADA CELESTIAL] §f¡PODER PERFORACIELOS DESATADO!"), true);
            }

            case THE_FLUTE -> {
                // Obertura Melódica (10s CD)
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
                sp.displayClientMessage(Component.literal("§e§l✦ [LA ESPADA DE LA FLAUTA] §6¡OBERTURA MELÓDICA RESONANTE!"), true);
            }

            case THE_BLACK_SWORD -> {
                // Sed de Batalla (12s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 240L);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true, true));

                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8F, 1.6F);
                sp.displayClientMessage(Component.literal("§4§l✦ [LA ESPADA NEGRA] §c¡SED DE SANGRE Y COMBATE DESPERTADA!"), true);
            }

            case BLACK_TASSEL -> {
                // Estocada Cazadora (8s CD)
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
                sp.displayClientMessage(Component.literal("§7§l✦ [BORLA NEGRA] §f¡ESTOCADA CAZADORA DE ALIMAÑAS!"), true);
            }

            case HERTA_HAMMER -> {
                // Kuru Kuru! ~ Kururin (12s CD)
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
                // ¡El sonidito icónico de Herta (Kuru Kuru~ o Kururin~)!
                net.minecraft.sounds.SoundEvent kuruSound = sp.getRandom().nextBoolean()
                        ? com.soybe.awakeningsystem.ModSounds.HERTA_KURU.get()
                        : com.soybe.awakeningsystem.ModSounds.HERTA_KURURIN.get();
                sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), kuruSound, SoundSource.VOICE, 2.0F, 1.0F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.HERTA_SWING.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                sp.displayClientMessage(Component.literal("§d§l✦ [MARTILLO DE HERTA] §b¡KURU KURU! ~ ¡KURURIN! (Torbellino Gélido)"), true);
            }

            case QINGMING_SWORD -> {
                // Floración de Escarcha de Éter (10s CD)
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
                sp.displayClientMessage(Component.literal("§b§l✦ [ESPADA QINGMING] §3¡FLORACIÓN DE ÉTER Y ESCARCHA DESATADA!"), true);
            }

            case TAILLESS -> {
                // Impacto de Demolición Cibernética (12s CD)
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
                sp.displayClientMessage(Component.literal("§6§l✦ [TAILLESS] §c¡RUPTURA DE ANOMALÍA CIBERNÉTICA EJECUTADA!"), true);
            }

            case SHUNGUANG_SWORD -> {
                // Destello Relámpago Fugaz (8s CD)
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
                sp.displayClientMessage(Component.literal("§e§l✦ [ESPADA SHUNGUANG] §f¡DESTELLO RELÁMPAGO FUGAZ!"), true);
            }

            case SHIYUAN_SWORD -> {
                // Pulso del Origen (10s CD)
                sp.getPersistentData().putLong(cdKey, gameTime + 200L);
                SystemData data = SystemEvents.data(sp);
                data.restoreEnergy(30);
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0, false, true, true));

                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
                sp.displayClientMessage(Component.literal("§a§l✦ [ESPADA SHIYUAN] §f¡PULSO DEL ORIGEN ACTIVADO (+30 MANÁ)!"), true);
                AwakeningSystemMod.sync(sp);
            }

            case DOMAIN_OF_SANCTION -> {
                // Dominio de la Sanción: Juicio de la Herrscher del Trueno (12s CD)
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
                sp.displayClientMessage(Component.literal("§d§l✦ [DOMINIO DE LA SANCIÓN] §e¡JUICIO DE LA HERRSCHER DEL TRUENO!"), true);
            }
        }
    }


    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && player.level() instanceof ServerLevel sl) {
            switch (weaponType) {
                case ENGULFING_LIGHTNING -> {
                    // Electro shock y maná
                    target.hurt(attacker.damageSources().magic(), 4.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                    if (player instanceof ServerPlayer sp) {
                        SystemEvents.data(sp).restoreEnergy(5);
                    }
                }
                case PRIMORDIAL_JADE_WINGED_SPEAR -> {
                    // Cuchillas de viento jade
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.02D);
                }
                case CRIMSON_MOONS_SEMBLANCE -> {
                    // Sangrado carmesí y robo de vida
                    player.heal(1.5F);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 0.8D, target.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.05D);
                }
                case WOLFS_GRAVESTONE -> {
                    // Rematar si objetivo < 30% HP
                    if (target.getHealth() <= target.getMaxHealth() * 0.30F) {
                        target.hurt(attacker.damageSources().playerAttack(player), 12.0F);
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, true, true));
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                    }
                }
                case REDHORN_STONETHRESHER -> {
                    // Escalar con armadura del jugador
                    int armor = player.getArmorValue();
                    float extraDmg = Math.min(18.0F, armor * 0.45F);
                    target.hurt(attacker.damageSources().playerAttack(player), extraDmg);
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.05D);
                }
                case VERDICT -> {
                    // Metralla cada 3 golpes
                    long count = player.getPersistentData().getLong("verdict_hits") + 1;
                    player.getPersistentData().putLong("verdict_hits", count);
                    if (count % 3 == 0) {
                        target.hurt(attacker.damageSources().explosion(player, player), 8.0F);
                        sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.01D);
                    }
                }
                case MISTSPLITTER_REFORGED -> {
                    // Hielo y relámpago
                    target.setTicksFrozen(60);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.02D);
                }
                case PRIMORDIAL_JADE_CUTTER -> {
                    // Daño extra basado en vida máxima
                    float bonus = player.getMaxHealth() * 0.08F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
                case FREEDOM_SWORN -> {
                    // Ráfaga céfira
                    sl.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.5D, target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                case SKYWARD_BLADE -> {
                    // Cuchilla de viento adicional
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
                    // Robo de vida
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
            // Bono de +20 Vida Máxima (10 corazones) mientras se empuña
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
            // Bono de +4 Vida Máxima (2 corazones) mientras se empuña
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
        String starStr = switch (weaponType.stars) {
            case 5 -> "⭐⭐⭐⭐⭐ [Arma Legendaria 5★]";
            case 4 -> "⭐⭐⭐⭐ [Arma Épica 4★]";
            default -> "⭐⭐⭐ [Arma de Aventurero 3★]";
        };
        tooltip.add(Component.literal("§6" + starStr).withStyle(weaponType.stars == 5 ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE));
        int rank = getRefinement(stack);
        tooltip.add(Component.literal("§e✦ Rango de Refinamiento: §6R" + rank + "§8/R5"));
        if (rank < 5) {
            long cost = (weaponType.stars >= 5 ? 30000L : 15000L) * rank;
            tooltip.add(Component.literal("  §8▸ Shift + Clic Der con duplicado en mano secundaria: §6" + String.format("%,d", cost) + " Mora §8(R" + (rank + 1) + ")"));
        } else {
            tooltip.add(Component.literal("  §a✔ Rango Máximo de Refinamiento Alcanzado (R5)"));
        }
        tooltip.add(Component.literal("§7" + weaponType.lore).withStyle(ChatFormatting.GRAY));

        // Descripción de habilidad
        switch (weaponType) {
            case ENGULFING_LIGHTNING -> {
                tooltip.add(Component.literal("§d▸ Habilidad Activa: Sueño de Musou Isshin (Clic Derecho / ~)").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.literal("  §7Descarga rayos sobre enemigos en 10 bloques y otorga Fuerza, Resistencia y Velocidad.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§e▸ Pasiva: Eternidad Inquebrantable").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Ataques electrocutan e infunden +5 Maná por golpe.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case PRIMORDIAL_JADE_WINGED_SPEAR -> {
                tooltip.add(Component.literal("§a▸ Habilidad Activa: Azote de Todo Mal (Clic Derecho / ~)").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.literal("  §7Propulsa al jugador a los cielos para ejecutar un devastador ataque en picada.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§2▸ Pasiva: Justicia de Jade").withStyle(ChatFormatting.DARK_GREEN));
                tooltip.add(Component.literal("  §7Aumenta el daño de ataque con cada impacto certero consecutivo.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case CRIMSON_MOONS_SEMBLANCE -> {
                tooltip.add(Component.literal("§4▸ Habilidad Activa: Banquete de la Luna Roja (Clic Derecho / ~)").withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.literal("  §7Barre un corte de guadaña en 360°, descompone enemigos y drena su salud.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§c▸ Pasiva: Pacto Militar Carmesí").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("  §7Restaura salud al jugador en cada corte de guadaña.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case VORTEX_VANQUISHER -> {
                tooltip.add(Component.literal("§6▸ Habilidad Activa: Dominio Telúrico (Clic Derecho / ~)").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7Petrifica e inmoviliza a enemigos cercanos y otorga Escudos de Absorción III.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§e▸ Pasiva: Pilar Inconmovible").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Otorga resistencia continua mientras luchas.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case WOLFS_GRAVESTONE -> {
                tooltip.add(Component.literal("§c▸ Habilidad Activa: Aullido del Rey Lobo (Clic Derecho / ~)").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("  §7Desata el aullido ancestral: otorga +40% de Daño de Ataque a ti y a tus aliados.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§6▸ Pasiva: Cazador Implacable").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7Ataques a enemigos con menos del 30% HP infligen daño mortal ejecutante.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case REDHORN_STONETHRESHER -> {
                tooltip.add(Component.literal("§e▸ Habilidad Activa: Furia del Rey Oni Dorado (Clic Derecho / ~)").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Activa Fuerza III y Resistencia III con impactos de terremoto.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§6▸ Pasiva: Gokadaijin del Rey Oni").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7El daño de ataque escala con los puntos totales de armadura del portador.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case VERDICT -> {
                tooltip.add(Component.literal("§e▸ Habilidad Activa: Salva Ceremonial (Clic Derecho / ~)").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Dispara una salva frontal de cañón que arrasa enemigos en línea recta.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§6▸ Pasiva: Metralla Solar").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7Cada 3er golpe produce una explosión de metralla cristalina.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case SKYWARD_BLADE -> {
                tooltip.add(Component.literal("§b▸ Habilidad Activa: Poder Perforacielos (Clic Derecho / ~)").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Desata la velocidad de los vientos de Dvalin: Fuerza II, Velocidad II y Prisa II.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§3▸ Pasiva: Colmillo Celestial").withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.literal("  §7Genera cuchillas de viento cortantes con cada golpe básico.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case MISTSPLITTER_REFORGED -> {
                tooltip.add(Component.literal("§9▸ Habilidad Activa: Corte de Niebla Trascendente (Clic Derecho / ~)").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.literal("  §7Lanza un filo cortante de escarcha y niebla que congela a los enemigos.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§b▸ Pasiva: Emblema Cortanebulosas").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Aumenta la velocidad y congela instantáneamente al impactar.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case PRIMORDIAL_JADE_CUTTER -> {
                tooltip.add(Component.literal("§a▸ Habilidad Activa: Purificación Esmeralda (Clic Derecho / ~)").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.literal("  §7Disipa todas las desventajas y restaura un 35% de la Salud Máxima.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§2▸ Pasiva: Pureza de Jade").withStyle(ChatFormatting.DARK_GREEN));
                tooltip.add(Component.literal("  §7Otorga +10 Corazones Adicionales (+20 HP) mientras la empuñas.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case FREEDOM_SWORN -> {
                tooltip.add(Component.literal("§b▸ Habilidad Activa: Vórtice Céfiro de la Rebelión (Clic Derecho / ~)").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Crea un vórtice de viento que arrastra y eleva a los enemigos circundantes.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§3▸ Pasiva: Concierto Revolucionario").withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.literal("  §7Otorga velocidad de movimiento y salto mejorado.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case KEY_OF_KHAJ_NISUT -> {
                tooltip.add(Component.literal("§3▸ Habilidad Activa: Danza de la Inundación Sagrada (Clic Derecho / ~)").withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.literal("  §7Despliega aguas sagradas que regeneran salud continua y otorgan absorción.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case ABSOLUTION -> {
                tooltip.add(Component.literal("§d▸ Habilidad Activa: Estocada Fulgurante (Clic Derecho / ~)").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.literal("  §7Embiste a gran velocidad atravesando enemigos con un ataque perforante eléctrico.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case THE_FLUTE -> {
                tooltip.add(Component.literal("§e▸ Habilidad Activa: Obertura Melódica (Clic Derecho / ~)").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Emite un acorde resonante que repele a los enemigos y otorga Regeneración.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§6▸ Pasiva: Acorde Armónico").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7Acumula notas armónicas al atacar; cada 5 golpes detona una onda musical de daño.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case THE_BLACK_SWORD -> {
                tooltip.add(Component.literal("§4▸ Habilidad Activa: Sed de Sangre (Clic Derecho / ~)").withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.literal("  §7Entra en un frenesí voraz otorgando Fuerza II y Velocidad I.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§c▸ Pasiva: Justicia Sombría").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("  §7Restaura salud al asestar impactos certeros a los oponentes.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case THE_CATCH -> {
                tooltip.add(Component.literal("§b▸ Habilidad Activa: Carga del Pescador (Clic Derecho / ~)").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Recarga 35 puntos de maná/energía al instante y aumenta el poder crítico.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case BLACK_TASSEL -> {
                tooltip.add(Component.literal("§7▸ Habilidad Activa: Estocada Cazadora (Clic Derecho / ~)").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("  §7Carga frontal perforante letal contra monstruos amorfos.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§f▸ Pasiva: Perdición del Slime").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("  §7Inflige daño masivo demoledor a Slimes y Magma Cubes, y otorga +2 Corazones.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case FLEUVE_CENDRE_FERRYMAN -> {
                tooltip.add(Component.literal("§7▸ Habilidad Activa: Presión Clandestina (Clic Derecho / ~)").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("  §7Recupera maná y acelera la velocidad de minado y ataque.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case HERTA_HAMMER -> {
                tooltip.add(Component.literal("§d▸ Habilidad Activa: ¡Kuru Kuru! ~ Kururin (Clic Derecho / ~)").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.literal("  §7Gira en un torbellino helado de 360° repeliendo y congelando enemigos.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§b▸ Pasiva: Golpe Adicional de la Genio").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Impactar a enemigos con menos del 50% HP detona un ataque adicional de escarcha.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case QINGMING_SWORD -> {
                tooltip.add(Component.literal("§b▸ Habilidad Activa: Floración de Escarcha de Éter (Clic Derecho / ~)").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  §7Embiste en línea recta detonando escarcha y explosión de éter.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case TAILLESS -> {
                tooltip.add(Component.literal("§6▸ Habilidad Activa: Ruptura de Anomalía (Clic Derecho / ~)").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("  §7Golpe sísmico descendente que fractura la armadura del enemigo.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case SHUNGUANG_SWORD -> {
                tooltip.add(Component.literal("§e▸ Habilidad Activa: Destello Relámpago Fugaz (Clic Derecho / ~)").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §7Teletransporte instantáneo perforando enemigos con descarga eléctrica.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case SHIYUAN_SWORD -> {
                tooltip.add(Component.literal("§a▸ Habilidad Activa: Pulso del Origen (Clic Derecho / ~)").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.literal("  §7Otorga escudos de absorción y restaura +30 Maná.").withStyle(ChatFormatting.DARK_GRAY));
            }
            case DOMAIN_OF_SANCTION -> {
                tooltip.add(Component.literal("§d▸ Habilidad Activa: Juicio de la Herrscher (Clic Derecho / ~)").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.literal("  §7Desata una tormenta de rayos que paraliza enemigos con 20 de daño y otorga Fuerza y Velocidad.").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("§e▸ Pasiva: Furia Eléctrica").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  §735% de probabilidad de electrocutar enemigos al impactar con rayos.").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
