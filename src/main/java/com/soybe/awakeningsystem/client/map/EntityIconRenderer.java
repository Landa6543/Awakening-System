package com.soybe.awakeningsystem.client.map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.*;
import com.soybe.awakeningsystem.entity.animal.*;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import com.soybe.awakeningsystem.uma.UmaRacerEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderizador de rostros e iconos tácticos compactos para el Minimapa y Mapa Mundial.
 * Proporciona renderizado de cabezas con texturas reales de mobs (JourneyMap / Xaero's style),
 * soporte de capas dobles (base + sombrero/overlay), aguja direccional táctica compacta (heading),
 * marcos coloreados según hostilidad y micro-flechas de elevación (▲ / ▼).
 */
public final class EntityIconRenderer {

    private static final ResourceLocation COW_TEX = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");
    private static final ResourceLocation MOOSHROOM_TEX = ResourceLocation.withDefaultNamespace("textures/entity/cow/red_mooshroom.png");
    private static final ResourceLocation CHICKEN_TEX = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");
    private static final ResourceLocation SALMON_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/fish/salmon.png");
    private static final ResourceLocation COD_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/fish/cod.png");
    private static final ResourceLocation PUFFERFISH_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/fish/pufferfish.png");
    private static final ResourceLocation TROPICAL_FISH_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/fish/tropical.png");
    private static final ResourceLocation SQUID_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/squid.png");
    private static final ResourceLocation DOLPHIN_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/dolphin.png");
    private static final ResourceLocation BAT_ICON = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/map/entity_icon/bat.png");
    private static final ResourceLocation PIG_TEX = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");
    private static final ResourceLocation SHEEP_TEX = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep.png");
    private static final ResourceLocation VILLAGER_TEX = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private static final ResourceLocation WANDERING_TRADER_TEX = ResourceLocation.withDefaultNamespace("textures/entity/wandering_trader.png");

    public record MobTextureRegion(
            ResourceLocation texture,
            float u, float v, int uW, int vH,
            int texW, int texH,
            boolean hasHat,
            float hatU, float hatV, int hatW, int hatH,
            ResourceLocation overlayTexture
    ) {
        public MobTextureRegion(ResourceLocation texture, float u, float v, int uW, int vH, int texW, int texH) {
            this(texture, u, v, uW, vH, texW, texH, false, 0, 0, 0, 0, null);
        }

        public MobTextureRegion(ResourceLocation texture, float u, float v, int uW, int vH, int texW, int texH, boolean hasHat, float hatU, float hatV) {
            this(texture, u, v, uW, vH, texW, texH, hasHat, hatU, hatV, uW, vH, null);
        }

        public MobTextureRegion(ResourceLocation texture, float u, float v, int uW, int vH, int texW, int texH, ResourceLocation overlay) {
            this(texture, u, v, uW, vH, texW, texH, false, 0, 0, 0, 0, overlay);
        }
    }

    private static final Map<EntityType<?>, MobTextureRegion> MOB_REGISTRY = new HashMap<>();

    static {
        // Mobs Hostiles Bipedales con capa base y sombrero/overlay
        register(EntityType.ZOMBIE, "textures/entity/zombie/zombie.png", 8, 8, 8, 8, 64, 64, true, 40, 8);
        register(EntityType.DROWNED, "textures/entity/zombie/drowned.png", 8, 8, 8, 8, 64, 64, true, 40, 8);
        register(EntityType.HUSK, "textures/entity/zombie/husk.png", 8, 8, 8, 8, 64, 64, true, 40, 8);
        register(EntityType.ZOMBIE_VILLAGER, "textures/entity/zombie_villager/zombie_villager.png", 8, 8, 8, 10, 64, 64, true, 40, 8);

        register(EntityType.SKELETON, "textures/entity/skeleton/skeleton.png", 8, 8, 8, 8, 64, 32);
        register(EntityType.STRAY, "textures/entity/skeleton/stray.png", 8, 8, 8, 8, 64, 32,
                ResourceLocation.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png"));
        register(EntityType.WITHER_SKELETON, "textures/entity/skeleton/wither_skeleton.png", 8, 8, 8, 8, 64, 32);
        register(EntityType.BOGGED, "textures/entity/skeleton/bogged.png", 8, 8, 8, 8, 64, 32,
                ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png"));

        register(EntityType.CREEPER, "textures/entity/creeper/creeper.png", 8, 8, 8, 8, 64, 32);
        // Ojos de araña en vanilla: textures/entity/spider_eyes.png
        register(EntityType.SPIDER, "textures/entity/spider/spider.png", 40, 12, 8, 8, 64, 32,
                ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));
        register(EntityType.CAVE_SPIDER, "textures/entity/spider/cave_spider.png", 40, 12, 8, 8, 64, 32,
                ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));
        register(EntityType.ENDERMAN, "textures/entity/enderman/enderman.png", 8, 8, 8, 8, 64, 32,
                ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png"));
        register(EntityType.WITCH, "textures/entity/witch.png", 8, 8, 8, 10, 64, 128);
        register(EntityType.BLAZE, "textures/entity/blaze.png", 8, 8, 8, 8, 64, 32);
        register(EntityType.GHAST, "textures/entity/ghast/ghast.png", 16, 16, 16, 16, 64, 64);
        register(EntityType.PHANTOM, "textures/entity/phantom.png", 3, 3, 7, 3, 64, 64,
                ResourceLocation.withDefaultNamespace("textures/entity/phantom_eyes.png"));
        register(EntityType.BREEZE, "textures/entity/breeze/breeze.png", 8, 8, 8, 8, 64, 64);
        register(EntityType.SILVERFISH, "textures/entity/silverfish.png", 0, 0, 6, 4, 64, 32);
        register(EntityType.ENDERMITE, "textures/entity/endermite.png", 0, 0, 6, 4, 64, 32);
        register(EntityType.SHULKER, "textures/entity/shulker/shulker.png", 8, 8, 8, 8, 64, 64);

        // Nether & End
        register(EntityType.PIGLIN, "textures/entity/piglin/piglin.png", 8, 8, 10, 8, 64, 64, true, 40, 8);
        register(EntityType.PIGLIN_BRUTE, "textures/entity/piglin/piglin_brute.png", 8, 8, 10, 8, 64, 64, true, 40, 8);
        register(EntityType.ZOMBIFIED_PIGLIN, "textures/entity/piglin/zombified_piglin.png", 8, 8, 10, 8, 64, 64, true, 40, 8);
        register(EntityType.HOGLIN, "textures/entity/hoglin/hoglin.png", 24, 24, 14, 12, 128, 64);
        register(EntityType.ZOGLIN, "textures/entity/hoglin/zoglin.png", 24, 24, 14, 12, 128, 64);
        register(EntityType.STRIDER, "textures/entity/strider/strider.png", 16, 16, 16, 16, 64, 128);

        // Illagers
        register(EntityType.PILLAGER, "textures/entity/illager/pillager.png", 8, 8, 8, 10, 64, 64, true, 40, 8);
        register(EntityType.VINDICATOR, "textures/entity/illager/vindicator.png", 8, 8, 8, 10, 64, 64, true, 40, 8);
        register(EntityType.EVOKER, "textures/entity/illager/evoker.png", 8, 8, 8, 10, 64, 64, true, 40, 8);
        register(EntityType.RAVAGER, "textures/entity/illager/ravager.png", 16, 16, 16, 24, 128, 128);
        register(EntityType.VEX, "textures/entity/illager/vex.png", 8, 8, 8, 8, 64, 64);

        // Bosses
        register(EntityType.WARDEN, "textures/entity/warden/warden.png", 24, 24, 16, 16, 128, 128,
                ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_bioluminescent_layer.png"));
        register(EntityType.ENDER_DRAGON, "textures/entity/enderdragon/dragon.png", 180, 24, 24, 24, 256, 256);
        register(EntityType.ELDER_GUARDIAN, "textures/entity/guardian_elder.png", 16, 16, 16, 16, 64, 64);
        register(EntityType.GUARDIAN, "textures/entity/guardian.png", 16, 16, 16, 16, 64, 64);

        // Animales y Pasivos
        register(EntityType.GOAT, "textures/entity/goat/goat.png", 10, 8, 8, 10, 64, 64);
        register(EntityType.CAMEL, "textures/entity/camel/camel.png", 0, 0, 10, 10, 128, 128);
        register(EntityType.SNIFFER, "textures/entity/sniffer/sniffer.png", 16, 16, 16, 16, 128, 128);
        register(EntityType.ARMADILLO, "textures/entity/armadillo.png", 8, 8, 6, 6, 64, 64);
        register(EntityType.ALLAY, "textures/entity/allay/allay.png", 8, 8, 8, 8, 32, 32);
        register(EntityType.FROG, "textures/entity/frog/temperate_frog.png", 8, 8, 8, 8, 64, 32);
        register(EntityType.AXOLOTL, "textures/entity/axolotl/axolotl_lucy.png", 8, 8, 8, 8, 64, 64);
        register(EntityType.TURTLE, "textures/entity/turtle/big_sea_turtle.png", 9, 6, 6, 5, 128, 64);
        register(EntityType.PANDA, "textures/entity/panda/panda.png", 18, 18, 16, 14, 64, 64);
        register(EntityType.POLAR_BEAR, "textures/entity/bear/polarbear.png", 12, 12, 14, 10, 128, 64);
        register(EntityType.LLAMA, "textures/entity/llama/creamy.png", 9, 9, 4, 4, 128, 64);
        register(EntityType.TRADER_LLAMA, "textures/entity/llama/white.png", 9, 9, 4, 4, 128, 64);
        register(EntityType.PARROT, "textures/entity/parrot/parrot_red_blue.png", 6, 6, 4, 6, 32, 32);
        register(EntityType.OCELOT, "textures/entity/cat/ocelot.png", 5, 5, 5, 4, 64, 64);
    }

    private static void register(EntityType<?> type, String path, float u, float v, int uW, int vH, int texW, int texH) {
        MOB_REGISTRY.put(type, new MobTextureRegion(ResourceLocation.withDefaultNamespace(path), u, v, uW, vH, texW, texH));
    }

    private static void register(EntityType<?> type, String path, float u, float v, int uW, int vH, int texW, int texH, boolean hasHat, float hatU, float hatV) {
        MOB_REGISTRY.put(type, new MobTextureRegion(ResourceLocation.withDefaultNamespace(path), u, v, uW, vH, texW, texH, hasHat, hatU, hatV));
    }

    private static void register(EntityType<?> type, String path, float u, float v, int uW, int vH, int texW, int texH, ResourceLocation overlay) {
        MOB_REGISTRY.put(type, new MobTextureRegion(ResourceLocation.withDefaultNamespace(path), u, v, uW, vH, texW, texH, overlay));
    }

    private static void registerMod(EntityType<?> type, String path, float u, float v, int uW, int vH, int texW, int texH) {
        if (type != null) {
            MOB_REGISTRY.put(type, new MobTextureRegion(ResourceLocation.fromNamespaceAndPath("awakening_system", path), u, v, uW, vH, texW, texH));
        }
    }

    private static void registerMod(EntityType<?> type, String path, float u, float v, int uW, int vH, int texW, int texH, boolean hasHat, float hatU, float hatV) {
        if (type != null) {
            MOB_REGISTRY.put(type, new MobTextureRegion(ResourceLocation.fromNamespaceAndPath("awakening_system", path), u, v, uW, vH, texW, texH, hasHat, hatU, hatV));
        }
    }

    private static boolean modMobsRegistered = false;

    private static synchronized void ensureModMobsRegistered() {
        if (modMobsRegistered) return;
        try {
            registerMod(ModEntities.SHADOW_INFANTRY.get(), "textures/entity/shadow_infantry.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.SHADOW_MONARCH_BOSS.get(), "textures/entity/shadow_monarch_boss.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.DOKKAEBI.get(), "textures/entity/dokkaebi.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.BARAN_BOSS.get(), "textures/entity/baran.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.IGRIS_BOSS.get(), "textures/entity/igris.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.BLOOD_PATRIARCH_BOSS.get(), "textures/entity/blood_patriarch.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.BLOOD_SECT_DISCIPLE.get(), "textures/entity/blood_disciple.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.MOUNT_HUA_DISCIPLE.get(), "textures/entity/mount_hua_disciple.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.CHILDE_BOSS.get(), "textures/entity/boss/childe.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.RAIDEN_SHOGUN_BOSS.get(), "textures/entity/boss/raiden_shogun.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.WILDFIRE.get(), "textures/entity/wildfire/wildfire.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.SCREAMING_MANDRAKE.get(), "textures/entity/screaming_mandrake.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.WATER_MIMIC.get(), "textures/entity/waterminion_texture.png", 8, 8, 8, 8, 64, 64);
            registerMod(ModEntities.LUNAR_SHADOW.get(), "textures/entity/lunar_shadow.png", 8, 8, 8, 8, 64, 64, true, 40, 8);
            registerMod(ModEntities.LUNAR_GUARDIAN.get(), "textures/entity/lunar_guardian.png", 8, 8, 8, 8, 128, 128, true, 36, 0);
            registerMod(ModEntities.GLARE.get(), "textures/entity/glare.png", 9, 9, 12, 9, 64, 128);
            modMobsRegistered = true;
        } catch (Exception ignored) {}
    }

    private EntityIconRenderer() {}

    /**
     * Dibuja exclusivamente el rostro / icono frontal de la entidad (sin aguja de orientación ni flechas de elevación).
     * Diseñado especialmente para fichas tácticas, tooltips de mapa y marcos de objetivos RPG.
     */
    public static void renderEntityFaceOnly(GuiGraphics g, LivingEntity entity, int x, int y, int size) {
        if (entity == null) return;
        ensureModMobsRegistered();

        if (entity instanceof AbstractClientPlayer clientPlayer) {
            PlayerFaceRenderer.draw(g, clientPlayer.getSkin(), x, y, size);
            return;
        }

        if (renderCompoundIcon(g, entity, x, y, size)) {
            return;
        }

        MobTextureRegion reg = MOB_REGISTRY.get(entity.getType());
        if (reg != null) {
            blitNearest(g, reg.texture(), x, y, size, size, reg.u(), reg.v(), reg.uW(), reg.vH(), reg.texW(), reg.texH());
            if (reg.hasHat()) {
                blitNearest(g, reg.texture(), x, y, size, size, reg.hatU(), reg.hatV(), reg.hatW(), reg.hatH(), reg.texW(), reg.texH());
            }
            if (reg.overlayTexture() != null) {
                blitNearest(g, reg.overlayTexture(), x, y, size, size, reg.u(), reg.v(), reg.uW(), reg.vH(), reg.texW(), reg.texH());
            }
            return;
        }

        // Fallback estilizado para entidades de mods externos
        boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(entity);
        int fallbackCol = isBoss ? 0xFFFFD700 : (entity instanceof net.minecraft.world.entity.monster.Enemy ? 0xFFFF3333 : (entity instanceof NeutralMob ? 0xFFFFB86C : 0xFF50FA7B));
        g.fill(x, y, x + size, y + size, 0xFF14202B);
        g.renderOutline(x, y, size, size, fallbackCol);
        String iconChar = isBoss ? "☠" : (entity instanceof net.minecraft.world.entity.monster.Enemy ? "⚔" : "☘");
        g.drawCenteredString(Minecraft.getInstance().font, iconChar, x + size / 2, y + (size - 8) / 2, fallbackCol);
    }

    /**
     * Dibuja el rostro/icono táctico compacto de la entidad con sombras, capas compuestas,
     * aguja de orientación sutil (heading) y flechas de elevación tridimensional.
     */
    public static void renderEntityIcon(GuiGraphics g, LivingEntity entity, int x, int y, int size, double playerY) {
        ensureModMobsRegistered();

        Font font = Minecraft.getInstance().font;
        boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(entity);
        int deltaY = (int) Math.round(entity.getY() - playerY);

        int halfSize = size / 2;
        int drawX = x - halfSize;
        int drawY = y - halfSize;

        boolean isEnemy = entity instanceof net.minecraft.world.entity.monster.Enemy;
        boolean isPassive = entity instanceof Animal || entity instanceof AbstractVillager;
        if (!com.soybe.awakeningsystem.client.gui.GuiConfig.minimapShowHostiles && isEnemy) return;
        if (!com.soybe.awakeningsystem.client.gui.GuiConfig.minimapShowPassives && isPassive) return;

        // Determinar color táctico de hostilidad
        int borderCol;
        if (isBoss) {
            borderCol = 0xFFFFD700; // Dorado Jefe
        } else if (entity instanceof net.minecraft.world.entity.monster.Enemy) {
            borderCol = 0xFFFF3333; // Rojo Enemigo
        } else if (entity instanceof NeutralMob) {
            borderCol = 0xFFFFB86C; // Ámbar Neutral
        } else if (entity instanceof Animal || entity instanceof AbstractVillager) {
            borderCol = 0xFF50FA7B; // Verde Pacífico
        } else {
            borderCol = 0xFF00E5FF; // Cian Aliado / Jugador
        }

        // 1. Sombra suave compacta
        g.fill(drawX, drawY + 1, drawX + size, drawY + size + 1, 0x66000000);

        // 2. Renderizado del rostro / icono
        if (entity instanceof AbstractClientPlayer clientPlayer) {
            int rankColor = 0xFF00E5FF;
            var pData = clientPlayer.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM);
            if (pData != null && pData.rank() != null) {
                String rk = pData.rank().toUpperCase(java.util.Locale.ROOT);
                if (rk.contains("MONARCH") || rk.contains("NATIONAL") || rk.contains("GOD") || rk.contains("RULER")) {
                    rankColor = 0xFFFF55FF; // Cósmico / Monarca
                } else if (rk.contains("S") || rk.contains("EX")) {
                    rankColor = 0xFFFFD700; // S-Rank Dorado
                } else if (rk.contains("A")) {
                    rankColor = 0xFFFF3355; // A-Rank Carmesí
                } else if (rk.contains("B")) {
                    rankColor = 0xFF9D4EDD; // B-Rank Morado
                } else if (rk.contains("C")) {
                    rankColor = 0xFF3399FF; // C-Rank Azul
                } else if (rk.contains("D")) {
                    rankColor = 0xFF50FA7B; // D-Rank Verde
                } else {
                    rankColor = 0xFFE0E0E0; // E-Rank Blanco / Gris
                }
            }
            PlayerFaceRenderer.draw(g, clientPlayer.getSkin(), drawX, drawY, size);
            g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, rankColor);
            drawHeadingPointer(g, x, y, clientPlayer.getYHeadRot(), size, rankColor);
        } else if (renderCompoundIcon(g, entity, drawX, drawY, size)) {
            // Renderizado compuesto completado (lobos, ovejas, slimes, hombres bestia, etc.)
            drawHeadingPointer(g, x, y, entity.getYHeadRot(), size, borderCol);
            if (isBoss) {
                g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFFFFD700);
                g.drawCenteredString(font, "☠", x, drawY - 8, 0xFFFF2D55);
            } else {
                drawHostilityDot(g, drawX + size - 2, drawY + size - 2, borderCol);
            }
        } else {
            MobTextureRegion reg = MOB_REGISTRY.get(entity.getType());

            if (reg != null) {
                // Capa base del rostro
                blitNearest(g, reg.texture(), drawX, drawY, size, size, reg.u(), reg.v(), reg.uW(), reg.vH(), reg.texW(), reg.texH());

                // Capa de sombrero/overlay (para zombis, aldeanos, piglins, saqueadores, etc.)
                if (reg.hasHat()) {
                    blitNearest(g, reg.texture(), drawX, drawY, size, size, reg.hatU(), reg.hatV(), reg.hatW(), reg.hatH(), reg.texW(), reg.texH());
                }

                // Textura emisiva o capa superpuesta
                if (reg.overlayTexture() != null) {
                    blitNearest(g, reg.overlayTexture(), drawX, drawY, size, size, reg.u(), reg.v(), reg.uW(), reg.vH(), reg.texW(), reg.texH());
                }

                drawHeadingPointer(g, x, y, entity.getYHeadRot(), size, borderCol);

                if (isBoss) {
                    g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFFFFD700);
                    g.drawCenteredString(font, "☠", x, drawY - 8, 0xFFFF2D55);
                } else {
                    drawHostilityDot(g, drawX + size - 2, drawY + size - 2, borderCol);
                }
            } else {
                // Compatibilidad universal con mods externos (Alex's Mobs, Mowzie's, Cataclysm, etc.):
                // NUNCA muestrear texturas al azar. Renderizar un punto táctico nítido de hostilidad con aguja de orientación y flechas de elevación.
                int r = Math.max(2, size / 3);
                g.fill(x - r - 1, y - r - 1, x + r + 1, y + r + 1, 0xEE08111A);
                g.fill(x - r, y - r, x + r, y + r, borderCol);

                drawHeadingPointer(g, x, y, entity.getYHeadRot(), size, borderCol);

                if (isBoss) {
                    g.renderOutline(x - r - 2, y - r - 2, (r * 2) + 4, (r * 2) + 4, 0xFFFFD700);
                    g.drawCenteredString(font, "☠", x, y - r - 8, 0xFFFF2D55);
                }
            }
        }

        // 3. Flechas de Elevación micro-tácticas (▲ / ▼) compactas (2px de alto, sin texto gigante)
        if (deltaY >= 5) {
            // Flecha ▲ verde arriba del icono (ancho 3px, alto 2px)
            g.fill(x, drawY - 3, x + 1, drawY - 2, 0xFF50FA7B);
            g.fill(x - 1, drawY - 2, x + 2, drawY - 1, 0xFF50FA7B);
        } else if (deltaY <= -5) {
            // Flecha ▼ roja abajo del icono (ancho 3px, alto 2px)
            g.fill(x - 1, drawY + size + 1, x + 2, drawY + size + 2, 0xFFFF4444);
            g.fill(x, drawY + size + 2, x + 1, drawY + size + 3, 0xFFFF4444);
        }
    }

    /**
     * Dibuja una aguja direccional o flecha táctica compacta en el borde del icono apuntando al ángulo de la cabeza.
     */
    private static void drawHeadingPointer(GuiGraphics g, int cx, int cy, float headYaw, int size, int color) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(headYaw + 180.0F));

        int tipY = -size / 2 - 1;
        // Borde oscuro sutil
        g.fill(-1, tipY - 1, 2, tipY + 1, 0xDD08111A);
        // Punta direccional nítida
        g.fill(0, tipY, 1, tipY + 1, color);
        g.fill(0, tipY - 1, 1, tipY, color);

        pose.popPose();
    }

    /**
     * Dibuja un punto táctico de hostilidad en la esquina inferior del icono.
     */
    private static void drawHostilityDot(GuiGraphics g, int dotX, int dotY, int color) {
        g.fill(dotX - 1, dotY - 1, dotX + 3, dotY + 3, 0xEE08111A);
        g.fill(dotX, dotY, dotX + 2, dotY + 2, color);
    }

    /**
     * Renderizado compuesto con detalles específicos (orejas, cuernos, hocicos, collares tintados, etc.).
     */
    private static boolean renderCompoundIcon(GuiGraphics g, LivingEntity entity, int drawX, int drawY, int size) {
        EntityType<?> type = entity.getType();

        // 0. Animales Silvanos de Maná (Mana Cow, Mana Sheep, Mana Pig, Mana Chicken) con aura radiante
        if (entity instanceof ManaCowEntity || type == ModEntities.MANA_COW.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/animal/mana_cow.png");
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x4400E5FF);
            int hornW = Math.max(1, size / 5);
            int hornH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX - 1, drawY - hornH + 1, hornW, hornH, 23.0F, 1.0F, 1, 3, 64, 32);
            blitNearest(g, tex, drawX + size - hornW + 1, drawY - hornH + 1, hornW, hornH, 23.0F, 1.0F, 1, 3, 64, 32);
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 6.0F, 8, 8, 64, 32);
            g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFF00E5FF);
            return true;
        }
        if (entity instanceof ManaSheepEntity || type == ModEntities.MANA_SHEEP.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/animal/mana_sheep.png");
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0xFF00E5FF);
            int fW = Math.max(4, size - 2);
            int fH = Math.max(4, size - 2);
            blitNearest(g, tex, drawX + (size - fW) / 2, drawY + (size - fH) / 2, fW, fH, 8.0F, 8.0F, 6, 6, 64, 32);
            g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFF00E5FF);
            return true;
        }
        if (entity instanceof ManaPigEntity || type == ModEntities.MANA_PIG.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/animal/mana_pig.png");
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x4400E5FF);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 32);
            int snW = Math.max(2, (size * 4) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, tex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 16.0F, 16.0F, 4, 3, 64, 32);
            g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFF00E5FF);
            return true;
        }
        if (entity instanceof ManaChickenEntity || type == ModEntities.MANA_CHICKEN.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/animal/mana_chicken.png");
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x4400E5FF);
            int hW = Math.max(3, (size * 5) / 6);
            int hX = drawX + (size - hW) / 2;
            blitNearest(g, tex, hX, drawY, hW, size, 3.0F, 3.0F, 4, 6, 64, 32);
            int bkW = Math.max(2, (size * 4) / 6);
            int bkH = Math.max(2, size / 3);
            int bkX = drawX + (size - bkW) / 2;
            int bkY = drawY + (size / 3);
            blitNearest(g, tex, bkX, bkY, bkW, bkH, 16.0F, 2.0F, 4, 2, 64, 32);
            int wtW = Math.max(1, bkW / 2);
            int wtH = Math.max(1, bkH);
            blitNearest(g, tex, bkX + (bkW - wtW) / 2, bkY + bkH - 1, wtW, wtH, 16.0F, 6.0F, 2, 2, 64, 32);
            g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, 0xFF00E5FF);
            return true;
        }

        // 1. Lobo (Wolf) con collar coloreado si está domesticado
        if (entity instanceof Wolf wolf) {
            ResourceLocation wTex = getDynamicOrFallback(wolf, ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png"));

            // Orejas de lobo
            int earW = Math.max(2, size / 3);
            int earH = Math.max(2, size / 3);
            blitNearest(g, wTex, drawX, drawY - 1, earW, earH, 16.0F, 14.0F, 2, 2, 64, 32);
            blitNearest(g, wTex, drawX + size - earW, drawY - 1, earW, earH, 16.0F, 14.0F, 2, 2, 64, 32);

            // Rostro frontal
            blitNearest(g, wTex, drawX, drawY, size, size, 4.0F, 4.0F, 6, 6, 64, 32);

            // Hocico 3D
            int snW = Math.max(2, (size * 4) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, wTex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 0.0F, 10.0F, 3, 3, 64, 32);

            // Collar coloreado si está domesticado
            if (wolf.isTame()) {
                int collarRgb = wolf.getCollarColor().getTextureDiffuseColor() | 0xFF000000;
                int colH = Math.max(1, size / 6);
                g.fill(drawX + 1, drawY + size - colH, drawX + size - 1, drawY + size, collarRgb);
            }
            return true;
        }

        // 2. Oveja (Sheep y ManaSheep) con lana tintada según su color
        if (entity instanceof Sheep sheep) {
            ResourceLocation sTex = getDynamicOrFallback(sheep, SHEEP_TEX);
            if (!sheep.isSheared()) {
                int woolCol = sheep.getColor().getTextureDiffuseColor() | 0xFF000000;
                g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, woolCol);

                int fW = Math.max(4, size - 3);
                int fH = Math.max(4, size - 3);
                int fX = drawX + (size - fW) / 2;
                int fY = drawY + (size - fH) / 2;
                blitNearest(g, sTex, fX, fY, fW, fH, 8.0F, 8.0F, 6, 6, 64, 32);
            } else {
                blitNearest(g, sTex, drawX, drawY, size, size, 8.0F, 8.0F, 6, 6, 64, 32);
            }
            return true;
        }

        // 3. Slime y Magma Cube
        if (entity instanceof Slime) {
            boolean isMagma = entity instanceof MagmaCube;
            ResourceLocation sTex = isMagma ? ResourceLocation.withDefaultNamespace("textures/entity/slime/magmacube.png")
                    : ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

            if (isMagma) {
                blitNearest(g, sTex, drawX + 1, drawY + 1, size - 2, size - 2, 40.0F, 8.0F, 8, 8, 64, 32);
                blitNearest(g, sTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 32);
            } else {
                g.fill(drawX, drawY, drawX + size, drawY + size, 0x6655EE55);
                blitNearest(g, sTex, drawX + 1, drawY + 1, size - 2, size - 2, 40.0F, 8.0F, 8, 8, 64, 32);
                blitNearest(g, sTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 32);
            }
            return true;
        }

        // 4. Wither Boss (3 Cabezas icónicas)
        if (entity instanceof WitherBoss wither) {
            ResourceLocation wTex = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");
            int cSize = (size * 3) / 4;
            int cX = drawX + (size - cSize) / 2;
            int cY = drawY + (size - cSize) / 2;
            blitNearest(g, wTex, cX, cY, cSize, cSize, 8.0F, 8.0F, 8, 8, 64, 64);

            int sSize = Math.max(3, size / 2);
            blitNearest(g, wTex, drawX - 2, drawY + 2, sSize, sSize, 40.0F, 8.0F, 6, 6, 64, 64);
            blitNearest(g, wTex, drawX + size - sSize + 2, drawY + 2, sSize, sSize, 40.0F, 8.0F, 6, 6, 64, 64);

            if (wither.getInvulnerableTicks() > 0) {
                g.renderOutline(drawX - 2, drawY - 1, size + 4, size + 2, 0xFF00E5FF);
            }
            return true;
        }

        // 5. Iron Golem
        if (entity instanceof IronGolem) {
            ResourceLocation igTex = ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");
            blitNearest(g, igTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 10, 128, 128);
            int nW = Math.max(2, size / 4);
            int nH = Math.max(3, (size * 4) / 10);
            blitNearest(g, igTex, drawX + (size - nW) / 2, drawY + (size * 4) / 10, nW, nH, 24.0F, 0.0F, 2, 4, 128, 128);
            return true;
        }

        // 6. Snow Golem
        if (entity instanceof SnowGolem sg) {
            if (sg.hasPumpkin()) {
                ResourceLocation pTex = ResourceLocation.withDefaultNamespace("textures/block/carved_pumpkin.png");
                blitNearest(g, pTex, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            } else {
                ResourceLocation sTex = ResourceLocation.withDefaultNamespace("textures/entity/snow_golem.png");
                blitNearest(g, sTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            }
            return true;
        }

        // 7. Creeper
        if (entity instanceof Creeper creeper) {
            ResourceLocation cTex = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");
            blitNearest(g, cTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 32);
            if (creeper.isPowered()) {
                int chargeColor = ((int) (System.currentTimeMillis() / 100) % 2 == 0) ? 0xFF00FFFF : 0xFFFFFFFF;
                g.renderOutline(drawX - 1, drawY - 1, size + 2, size + 2, chargeColor);
            }
            return true;
        }

        // 8. Abeja (Bee)
        if (entity instanceof Bee bee) {
            ResourceLocation bTex = bee.isAngry() ? ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry.png")
                    : (bee.hasNectar() ? ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_nectar.png")
                    : ResourceLocation.withDefaultNamespace("textures/entity/bee/bee.png"));
            blitNearest(g, bTex, drawX, drawY, size, size, 9.0F, 10.0F, 8, 7, 64, 64);
            return true;
        }

        // 9. Vaca, Champiñaca y Vaca de Maná (Cow, Mooshroom, ManaCow) - Textura 64x32
        if (entity instanceof Cow || type == EntityType.COW || type == EntityType.MOOSHROOM) {
            ResourceLocation tex = getDynamicOrFallback(entity, type == EntityType.MOOSHROOM ? MOOSHROOM_TEX : COW_TEX);
            int hornW = Math.max(1, size / 5);
            int hornH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX - 1, drawY - hornH + 1, hornW, hornH, 23.0F, 1.0F, 1, 3, 64, 32);
            blitNearest(g, tex, drawX + size - hornW + 1, drawY - hornH + 1, hornW, hornH, 23.0F, 1.0F, 1, 3, 64, 32);
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 6.0F, 8, 8, 64, 32);
            return true;
        }

        // 10. Pollo y Pollo de Maná (Chicken & ManaChicken)
        if (entity instanceof Chicken || type == EntityType.CHICKEN) {
            ResourceLocation chTex = getDynamicOrFallback(entity, CHICKEN_TEX);
            int hW = Math.max(3, (size * 5) / 6);
            int hX = drawX + (size - hW) / 2;
            blitNearest(g, chTex, hX, drawY, hW, size, 3.0F, 3.0F, 4, 6, 64, 32);
            int bkW = Math.max(2, (size * 4) / 6);
            int bkH = Math.max(2, size / 3);
            int bkX = drawX + (size - bkW) / 2;
            int bkY = drawY + (size / 3);
            blitNearest(g, chTex, bkX, bkY, bkW, bkH, 16.0F, 2.0F, 4, 2, 64, 32);
            int wtW = Math.max(1, bkW / 2);
            int wtH = Math.max(1, bkH);
            blitNearest(g, chTex, bkX + (bkW - wtW) / 2, bkY + bkH - 1, wtW, wtH, 16.0F, 6.0F, 2, 2, 64, 32);
            return true;
        }

        // 11. Peces y Criaturas Acuáticas con sprites 16x16 limpios (estilo JourneyMap)
        if (type == EntityType.SALMON) {
            blitNearest(g, SALMON_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.COD) {
            blitNearest(g, COD_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.PUFFERFISH) {
            blitNearest(g, PUFFERFISH_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.TROPICAL_FISH) {
            blitNearest(g, TROPICAL_FISH_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.SQUID || type == EntityType.GLOW_SQUID) {
            if (type == EntityType.GLOW_SQUID) {
                g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x4400FFFF);
            }
            blitNearest(g, SQUID_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.DOLPHIN) {
            blitNearest(g, DOLPHIN_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }
        if (type == EntityType.BAT) {
            blitNearest(g, BAT_ICON, drawX, drawY, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
            return true;
        }

        // 12. Cerdo y Cerdo de Maná (Pig & ManaPig)
        if (entity instanceof Pig || type == EntityType.PIG) {
            ResourceLocation pTex = getDynamicOrFallback(entity, PIG_TEX);
            blitNearest(g, pTex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 32);
            int snW = Math.max(2, (size * 4) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, pTex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 17.0F, 17.0F, 4, 3, 64, 32);
            return true;
        }

        // 13. Zorro (Fox)
        if (entity instanceof Fox fox) {
            boolean isSnow = fox.getVariant() == Fox.Type.SNOW;
            ResourceLocation foxTex = isSnow ? ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png")
                    : ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png");
            int earW = Math.max(2, size / 3);
            int earH = Math.max(2, size / 3);
            blitNearest(g, foxTex, drawX, drawY - 1, earW, earH, 8.0F, 1.0F, 2, 2, 64, 32);
            blitNearest(g, foxTex, drawX + size - earW, drawY - 1, earW, earH, 12.0F, 1.0F, 2, 2, 64, 32);
            blitNearest(g, foxTex, drawX, drawY, size, size, 6.0F, 5.0F, 8, 6, 64, 32);
            int snW = Math.max(2, (size * 5) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, foxTex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 6.0F, 15.0F, 4, 2, 64, 32);
            return true;
        }

        // 14. Gato (Cat)
        if (entity instanceof Cat cat) {
            ResourceLocation cTex = getDynamicOrFallback(cat, ResourceLocation.withDefaultNamespace("textures/entity/cat/tabby.png"));
            int earW = Math.max(2, size / 4);
            int earH = Math.max(2, size / 4);
            blitNearest(g, cTex, drawX, drawY - 1, earW, earH, 2.0F, 1.0F, 2, 2, 64, 64);
            blitNearest(g, cTex, drawX + size - earW, drawY - 1, earW, earH, 6.0F, 1.0F, 2, 2, 64, 64);
            blitNearest(g, cTex, drawX, drawY, size, size, 5.0F, 5.0F, 5, 4, 64, 64);
            return true;
        }

        // 15. Caballos reales (Horse, Donkey, Mule, SkeletonHorse, ZombieHorse)
        if (entity instanceof AbstractHorse horse && !(entity instanceof Llama) && !(entity instanceof Camel)) {
            ResourceLocation hTex = getDynamicOrFallback(horse, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_white.png"));
            boolean isDonkeyOrMule = entity instanceof Donkey || entity instanceof Mule;
            boolean isSkeleton = entity instanceof SkeletonHorse;
            boolean isZombie = entity instanceof ZombieHorse;

            // 1. Base sólida de contraste: evita que caballos negros o marrones oscuros se vuelvan invisibles o parezcan un bug sobre fondos oscuros
            int baseCoat = isSkeleton ? 0xFFDAD5C9 : (isZombie ? 0xFF4A6836 : 0xFF38312A);
            g.fill(drawX, drawY, drawX + size, drawY + size, baseCoat);

            // 2. Orejas erguidas sobresaliendo limpiamente arriba de la cabeza
            int earW = Math.max(2, size / 4);
            int earH = isDonkeyOrMule ? Math.max(3, (size * 5) / 8) : Math.max(2, size / 3);
            int earY = drawY - earH + 1;
            blitNearest(g, hTex, drawX + 1, earY, earW, earH, 19.0F, 16.0F, 2, 3, 64, 64);
            blitNearest(g, hTex, drawX + size - earW - 1, earY, earW, earH, 19.0F, 16.0F, 2, 3, 64, 64);
            // Realce interior de orejas para visibilidad sobre cualquier fondo
            g.fill(drawX + 1, earY + 1, drawX + earW, earY + earH - 1, 0x33FFFFFF);
            g.fill(drawX + size - earW, earY + 1, drawX + size - 1, earY + earH - 1, 0x33FFFFFF);

            // 3. Rostro frontal y pelaje con la textura dinámica de la variante (blanco, negro, café, etc.)
            blitNearest(g, hTex, drawX, drawY, size, size, 7.0F, 20.0F, 6, 5, 64, 64);

            // 4. Crin central equina
            int maneW = Math.max(1, size / 6);
            int maneX = drawX + (size - maneW) / 2;
            int maneCol = isSkeleton ? 0xFFD0C8B8 : 0x55000000;
            g.fill(maneX, drawY, maneX + maneW, drawY + (size * 3) / 5, maneCol);

            // 5. Ojos expresivos y nítidos en los laterales del rostro
            int eyeY = drawY + Math.max(1, (size * 2) / 7);
            int eyeW = Math.max(2, size / 4);
            int eyeH = Math.max(1, size / 5);
            if (isSkeleton) {
                // Cuencas oculares oscuras de calavera esquelética
                g.fill(drawX + 1, eyeY, drawX + 1 + eyeW, eyeY + eyeH, 0xFF1B1B1B);
                g.fill(drawX + size - eyeW - 1, eyeY, drawX + size - 1, eyeY + eyeH, 0xFF1B1B1B);
            } else if (isZombie) {
                // Ojo zombi verde lima podrido luminiscente
                g.fill(drawX + 1, eyeY, drawX + 1 + eyeW, eyeY + eyeH, 0xFFA4E02C);
                g.fill(drawX + size - eyeW - 1, eyeY, drawX + size - 1, eyeY + eyeH, 0xFFA4E02C);
            } else {
                // Ojo equino vivo: esclerótica clara y pupila oscura
                g.fill(drawX + 1, eyeY, drawX + 1 + eyeW, eyeY + eyeH, 0xFFF5F2EB);
                g.fill(drawX + 1, eyeY, drawX + 2, eyeY + eyeH, 0xFF161412);

                g.fill(drawX + size - eyeW - 1, eyeY, drawX + size - 1, eyeY + eyeH, 0xFFF5F2EB);
                g.fill(drawX + size - 2, eyeY, drawX + size - 1, eyeY + eyeH, 0xFF161412);
            }

            // 6. Hocico frontal 3D con volumen y orificios nasales bien contrastados
            int snW = Math.max(3, (size * 5) / 8);
            int snH = Math.max(2, (size * 4) / 8);
            int snX = drawX + (size - snW) / 2;
            int snY = drawY + size - snH;

            if (isDonkeyOrMule) {
                // Hocico crema claro característico de burros y mulas
                g.fill(snX, snY, snX + snW, snY + snH, 0xFFD8D0C2);
            } else {
                // Base del hocico con textura
                g.fill(snX, snY, snX + snW, snY + snH, 0x44000000);
                blitNearest(g, hTex, snX, snY, snW, snH, 5.0F, 30.0F, 4, 5, 64, 64);
                // Borde superior de separación que aporta volumen 3D
                g.fill(snX, snY, snX + snW, snY + 1, 0x33FFFFFF);
            }

            // Orificios nasales oscuros bien definidos
            int nosY = snY + snH - 2;
            g.fill(snX + 1, nosY, snX + 2, nosY + 1, 0xFF111111);
            g.fill(snX + snW - 2, nosY, snX + snW - 1, nosY + 1, 0xFF111111);
            return true;
        }

        // 16. Aldeanos y Vendedores Ambulantes
        if (entity instanceof AbstractVillager villager) {
            ResourceLocation tex = entity instanceof WanderingTrader ? WANDERING_TRADER_TEX : getDynamicOrFallback(villager, VILLAGER_TEX);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 10, 64, 64);
            blitNearest(g, tex, drawX, drawY, size, size, 40.0F, 8.0F, 8, 10, 64, 64);
            int nW = Math.max(2, size / 4);
            int nH = Math.max(2, (size * 4) / 10);
            blitNearest(g, tex, drawX + (size - nW) / 2, drawY + (size * 4) / 10, nW, nH, 24.0F, 0.0F, 2, 4, 64, 64);
            return true;
        }

        // 16.2 Bruja (Witch) - Sombrero cónico puntiagudo, rostro con verruga y nariz prominente
        if (entity instanceof Witch witch) {
            ResourceLocation tex = ResourceLocation.withDefaultNamespace("textures/entity/witch.png");
            // 1. Rostro frontal (8, 8, 8, 10 en 64x128)
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 10, 64, 128);

            // 2. Nariz prominente con verruga (24, 0, 2, 4 en 64x128)
            int nW = Math.max(2, size / 4);
            int nH = Math.max(2, (size * 4) / 10);
            blitNearest(g, tex, drawX + (size - nW) / 2, drawY + (size * 4) / 10, nW, nH, 24.0F, 0.0F, 2, 4, 64, 128);

            // 3. Sombrero de bruja: Ala ancha y cono morado/negro puntiagudo arriba
            int brimH = Math.max(1, size / 5);
            g.fill(drawX - 2, drawY - brimH + 1, drawX + size + 2, drawY + 1, 0xFF1D1526);
            int coneW = Math.max(2, (size * 5) / 8);
            int coneH = Math.max(3, (size * 5) / 8);
            int coneX = drawX + (size - coneW) / 2;
            int coneY = drawY - brimH - coneH + 1;
            g.fill(coneX, coneY + coneH / 2, coneX + coneW, coneY + coneH, 0xFF2D1B3E);
            g.fill(coneX + 1, coneY, coneX + coneW - 1, coneY + coneH / 2, 0xFF2D1B3E);
            // Hebilla/cinta verde esmeralda del sombrero de bruja
            g.fill(coneX, drawY - brimH, coneX + coneW, drawY - brimH + 1, 0xFF2ECC71);
            return true;
        }

        // 16.5 Conejo (Rabbit) - Orejas largas erguidas, rostro y nariz con textura según variante
        if (entity instanceof Rabbit rabbit) {
            ResourceLocation rTex = getDynamicOrFallback(rabbit, ResourceLocation.withDefaultNamespace("textures/entity/rabbit/brown.png"));

            // 1. Orejas largas erguidas sobresaliendo arriba
            int earW = Math.max(2, size / 3);
            int earH = Math.max(3, (size * 5) / 8);
            // Oreja derecha (53, 1, 2, 5 en 64x32)
            blitNearest(g, rTex, drawX + 1, drawY - earH + 2, earW, earH, 53.0F, 1.0F, 2, 5, 64, 32);
            // Oreja izquierda (59, 1, 2, 5 en 64x32)
            blitNearest(g, rTex, drawX + size - earW - 1, drawY - earH + 2, earW, earH, 59.0F, 1.0F, 2, 5, 64, 32);

            // 2. Rostro / Cabeza frontal (37, 5, 5, 4 en 64x32)
            int fW = Math.max(4, (size * 6) / 7);
            int fH = Math.max(3, (size * 5) / 7);
            int fX = drawX + (size - fW) / 2;
            int fY = drawY + size - fH;
            blitNearest(g, rTex, fX, fY, fW, fH, 37.0F, 5.0F, 5, 4, 64, 32);

            // 3. Nariz pequeña (33, 10, 1, 1 en 64x32)
            int nSize = Math.max(1, size / 5);
            blitNearest(g, rTex, drawX + (size - nSize) / 2, fY + fH - nSize - 1, nSize, nSize, 33.0F, 10.0F, 1, 1, 64, 32);
            return true;
        }

        // 17. Entidades Personalizadas del Awakening System
        if (type == com.soybe.awakeningsystem.entity.ModEntities.DOKKAEBI.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/dokkaebi.png");
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }
        if (type == com.soybe.awakeningsystem.entity.ModEntities.SHADOW_INFANTRY.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/shadow_infantry.png");
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }
        if (type == com.soybe.awakeningsystem.entity.ModEntities.SHADOW_MONARCH_BOSS.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/shadow_monarch_boss.png");
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }
        if (type == com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/spirit_fox/spirit_fox.png");
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 5.0F, 8, 6, 64, 32);
            return true;
        }
        if (type == com.soybe.awakeningsystem.entity.ModEntities.SHADOW_DIREWOLF.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/shadow_direwolf/shadow_direwolf.png");
            // Orejas de lobo sombrío
            int earW = Math.max(2, size / 3);
            int earH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX, drawY - 1, earW, earH, 66.0F, 26.0F, 2, 3, 128, 128);
            blitNearest(g, tex, drawX + size - earW, drawY - 1, earW, earH, 74.0F, 26.0F, 2, 3, 128, 128);
            // Rostro frontal
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 31.0F, 6, 6, 128, 128);
            // Hocico 3D
            int snW = Math.max(2, (size * 4) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, tex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 31.0F, 30.0F, 4, 3, 128, 128);
            // Capa emisiva de ojos y relámpago
            ResourceLocation glowTex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/shadow_direwolf/shadow_direwolf_glow.png");
            blitNearest(g, glowTex, drawX, drawY, size, size, 6.0F, 31.0F, 6, 6, 128, 128);
            blitNearest(g, glowTex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 31.0F, 30.0F, 4, 3, 128, 128);
            return true;
        }
        if (type == com.soybe.awakeningsystem.entity.ModEntities.WHITE_THUNDER_TIGER.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/white_thunder_tiger/white_thunder_tiger.png");
            // Orejas de tigre
            int earW = Math.max(2, size / 4);
            int earH = Math.max(2, size / 4);
            blitNearest(g, tex, drawX, drawY - 1, earW, earH, 84.0F, 26.0F, 2, 2, 128, 128);
            blitNearest(g, tex, drawX + size - earW, drawY - 1, earW, earH, 90.0F, 26.0F, 2, 2, 128, 128);
            // Rostro
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 32.0F, 7, 6, 128, 128);
            // Hocico
            int snW = Math.max(2, (size * 5) / 8);
            int snH = Math.max(2, (size * 3) / 8);
            blitNearest(g, tex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 30.0F, 30.0F, 5, 4, 128, 128);
            // Capa emisiva de tigre celestial
            ResourceLocation glowTex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/white_thunder_tiger/white_thunder_tiger_glow.png");
            blitNearest(g, glowTex, drawX, drawY, size, size, 6.0F, 32.0F, 7, 6, 128, 128);
            return true;
        }

        // 18. Hada de Maná Silvana (Mana Pixie)
        if (type == com.soybe.awakeningsystem.entity.ModEntities.MANA_PIXIE.get()) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/mana_pixie.png");
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x4400E5FF);
            blitNearest(g, tex, drawX, drawY, size, size, 5.0F, 5.0F, 5, 5, 64, 32);
            ResourceLocation glowTex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/mana_pixie_glow.png");
            blitNearest(g, glowTex, drawX, drawY, size, size, 5.0F, 5.0F, 5, 5, 64, 32);
            return true;
        }

        // 19. Slimes Isekai (Gota fluida, brillo especular gloss y ojos anime con sonrisa)
        if (entity instanceof IsekaiSlimeEntity isekaiSlime) {
            ResourceLocation sTex = switch (isekaiSlime.getVariant()) {
                case AZURE -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_azure.png");
                case SAKURA -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_pink.png");
                case EMERALD -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_green.png");
                case GOLD -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_gold.png");
                case PURPLE -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_purple.png");
                case RUBY -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_ruby.png");
                case AMBER -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_amber.png");
                case MINT -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_mint.png");
                case FROST -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_frost.png");
                case OBSIDIAN -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/isekai_slime/slime_obsidian.png");
            };
            int fillCol = switch (isekaiSlime.getVariant()) {
                case AZURE -> 0xFF3BA7FF;
                case SAKURA -> 0xFFFF70B8;
                case EMERALD -> 0xFF2ED573;
                case GOLD -> 0xFFFFBE1A;
                case PURPLE -> 0xFFA55EEA;
                case RUBY -> 0xFFFF3838;
                case AMBER -> 0xFFFF7F11;
                case MINT -> 0xFF20BF6B;
                case FROST -> 0xFF45AAF2;
                case OBSIDIAN -> 0xFF3D3D4D;
            };
            // Silueta de gota fluida de slime
            g.fill(drawX + 1, drawY + 1, drawX + size - 1, drawY + size, fillCol);
            g.fill(drawX, drawY + 2, drawX + size, drawY + size - 1, fillCol);
            g.fill(drawX + size / 2 - 1, drawY, drawX + size / 2 + 1, drawY + 2, fillCol);
            // Brillo gloss especular anime en la esquina superior izquierda
            g.fill(drawX + 2, drawY + 2, drawX + 4, drawY + 4, 0x88FFFFFF);
            // Ojos anime expresivos (derecho 49, 1, 2, 3; izquierdo 55, 1, 2, 3)
            int eyeW = Math.max(2, size / 4);
            int eyeH = Math.max(2, (size * 3) / 8);
            int eyeY = drawY + size / 3;
            blitNearest(g, sTex, drawX + 2, eyeY, eyeW, eyeH, 49.0F, 1.0F, 2, 3, 64, 64);
            blitNearest(g, sTex, drawX + size - eyeW - 2, eyeY, eyeW, eyeH, 55.0F, 1.0F, 2, 3, 64, 64);
            // Boca tierna (49, 6, 1, 1)
            blitNearest(g, sTex, drawX + (size - 2) / 2, eyeY + eyeH - 1, 2, 2, 49.0F, 6.0F, 1, 1, 64, 64);
            return true;
        }

        // 20. Slimes Celestiales de Genshin
        if (entity instanceof com.soybe.awakeningsystem.entity.GenshinSlimeEntity genshinSlime) {
            String elemName = genshinSlime.getElement().getName().toLowerCase(java.util.Locale.ROOT);
            ResourceLocation sTex = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/genshin_slime/slime_" + elemName + ".png");
            int elemColor = switch (genshinSlime.getElement()) {
                case PYRO -> 0xFFFF5722;
                case HYDRO -> 0xFF2196F3;
                case ANEMO -> 0xFF00E5FF;
                case ELECTRO -> 0xFF9C27B0;
                case CRYO -> 0xFF80DEEA;
                case GEO -> 0xFFFFC107;
                case DENDRO -> 0xFF4CAF50;
            };
            g.fill(drawX + 1, drawY + 1, drawX + size - 1, drawY + size, elemColor);
            g.fill(drawX, drawY + 2, drawX + size, drawY + size - 1, elemColor);
            g.fill(drawX + 2, drawY + 2, drawX + 4, drawY + 4, 0x66FFFFFF);
            blitNearest(g, sTex, drawX, drawY, size, size, 1.0F, 1.0F, 8, 8, 64, 64);
            return true;
        }

        // 21. Hombre Bestia Guerrero (Beastman Warrior - Orejas, rostro 3D y hocico)
        if (entity instanceof BeastmanWarriorEntity beastman) {
            ResourceLocation tex = getDynamicOrFallback(beastman, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/beastman/beastman_warrior.png"));
            // Orejas de bestia erguidas arriba
            int earW = Math.max(2, (size * 3) / 8);
            int earH = Math.max(2, (size * 3) / 8);
            blitNearest(g, tex, drawX + 1, drawY - earH + 1, earW, earH, 1.0F, 1.0F, 3, 3, 128, 64);
            blitNearest(g, tex, drawX + size - earW - 1, drawY - earH + 1, earW, earH, 1.0F, 1.0F, 3, 3, 128, 64);
            // Rostro frontal (cabeza 8x8x8 en texOffs 0,0)
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 64);
            // Hocico 3D canino/felino (texOffs 32,0)
            int snW = Math.max(3, (size * 5) / 8);
            int snH = Math.max(2, (size * 4) / 8);
            blitNearest(g, tex, drawX + (size - snW) / 2, drawY + size - snH, snW, snH, 36.0F, 4.0F, 5, 4, 128, 64);
            return true;
        }

        // 22. Tribu Goblin (Explorador, Guerrero, Chamán)
        if (entity instanceof GoblinScoutEntity goblin) {
            ResourceLocation tex = getDynamicOrFallback(goblin, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/goblin/goblin_scout.png"));
            // Orejas puntiagudas alargadas sobresaliendo a los costados
            int earW = Math.max(3, (size * 4) / 8);
            int earH = Math.max(2, size / 3);
            int earY = drawY + (size - earH) / 2;
            blitNearest(g, tex, drawX - earW + 2, earY, earW, earH, 25.0F, 5.0F, 4, 2, 64, 64);
            blitNearest(g, tex, drawX + size - 2, earY, earW, earH, 25.0F, 5.0F, 4, 2, 64, 64);
            // Rostro verde goblin
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 6.0F, 6, 6, 64, 64);
            // Nariz puntiaguda 3D
            int nW = Math.max(1, size / 4);
            int nH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX + (size - nW) / 2, drawY + size - nH, nW, nH, 26.0F, 2.0F, 1, 2, 64, 64);
            return true;
        }
        if (entity instanceof GoblinWarriorEntity goblin) {
            ResourceLocation tex = getDynamicOrFallback(goblin, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/goblin/goblin_warrior.png"));
            // Orejas de guerrero sobresaliendo a los costados
            int earW = Math.max(3, (size * 4) / 8);
            int earH = Math.max(2, size / 3);
            int earY = drawY + (size - earH) / 2;
            blitNearest(g, tex, drawX - earW + 2, earY, earW, earH, 29.0F, 13.0F, 4, 2, 64, 64);
            blitNearest(g, tex, drawX + size - 2, earY, earW, earH, 29.0F, 13.0F, 4, 2, 64, 64);
            // Rostro
            blitNearest(g, tex, drawX, drawY, size, size, 7.0F, 7.0F, 7, 7, 64, 64);
            // Casco de hierro con remaches
            int helmH = Math.max(2, (size * 4) / 7);
            blitNearest(g, tex, drawX - 1, drawY - 1, size + 2, helmH, 36.0F, 8.0F, 8, 4, 64, 64);
            return true;
        }
        if (entity instanceof GoblinShamanEntity goblin) {
            ResourceLocation tex = getDynamicOrFallback(goblin, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/goblin/goblin_shaman.png"));
            // Pluma chamánica arriba
            int fW = Math.max(2, size / 4);
            int fH = Math.max(3, size / 2);
            blitNearest(g, tex, drawX + (size - fW) / 2, drawY - fH + 1, fW, fH, 43.0F, 1.0F, 2, 4, 64, 64);
            // Orejas de chamán
            int earW = Math.max(3, (size * 4) / 8);
            int earH = Math.max(2, size / 3);
            int earY = drawY + (size - earH) / 2;
            blitNearest(g, tex, drawX - earW + 2, earY, earW, earH, 25.0F, 11.0F, 4, 2, 64, 64);
            blitNearest(g, tex, drawX + size - 2, earY, earW, earH, 25.0F, 11.0F, 4, 2, 64, 64);
            // Rostro
            blitNearest(g, tex, drawX, drawY, size, size, 6.0F, 6.0F, 6, 6, 64, 64);
            // Máscara ritual de madera y calavera
            blitNearest(g, tex, drawX, drawY, size, size, 26.0F, 2.0F, 7, 8, 64, 64);
            return true;
        }

        // 23. Jiangshi y Cultivador Demoniaco
        if (entity instanceof JiangshiEntity jiangshi) {
            ResourceLocation tex = getDynamicOrFallback(jiangshi, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/murim/jiangshi.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 64);
            blitNearest(g, tex, drawX - 1, drawY - 1, size + 2, Math.max(2, size / 5), 32.0F, 0.0F, 10, 1, 128, 64);
            int talW = Math.max(2, size / 3);
            int talH = Math.max(3, (size * 3) / 5);
            blitNearest(g, tex, drawX + (size - talW) / 2, drawY + 1, talW, talH, 108.0F, 0.0F, 4, 7, 128, 64);
            return true;
        }
        if (entity instanceof DemonicCultivatorEntity cultivator) {
            ResourceLocation tex = getDynamicOrFallback(cultivator, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/murim/demonic_cultivator.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 64);
            blitNearest(g, tex, drawX - 2, drawY - 2, size + 4, Math.max(3, size / 4), 32.0F, 0.0F, 12, 2, 128, 64);
            blitNearest(g, tex, drawX, drawY + size / 2, size, size / 2, 0.0F, 50.0F, 9, 4, 128, 64);
            return true;
        }

        // 24. Tribu Hilichurl, Mitachurl y Lawachurls
        if (entity instanceof HilichurlEntity || entity instanceof HilichurlShooterEntity || entity instanceof HilichurlBerserkerEntity) {
            ResourceLocation tex = getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/hilichurl/hilichurl.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            int mW = Math.max(3, (size * 3) / 4);
            int mH = Math.max(3, (size * 3) / 4);
            blitNearest(g, tex, drawX + (size - mW) / 2, drawY + (size - mH) / 2, mW, mH, 54.0F, 0.0F, 4, 4, 64, 64);
            return true;
        }
        if (entity instanceof MitachurlEntity mitachurl) {
            ResourceLocation tex = getDynamicOrFallback(mitachurl, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/mitachurl/mitachurl_shield.png"));
            blitNearest(g, tex, drawX, drawY - 2, size, size / 3, 118.0F, 132.0F, 6, 3, 256, 256);
            blitNearest(g, tex, drawX, drawY, size, size, 121.0F, 78.0F, 5, 5, 256, 256);
            blitNearest(g, tex, drawX + 1, drawY + 1, size - 2, size - 2, 70.0F, 129.0F, 4, 4, 256, 256);
            return true;
        }
        if (entity instanceof StonehideLawachurlEntity || entity instanceof FrostarmLawachurlEntity || entity instanceof ThunderhelmLawachurlEntity) {
            ResourceLocation tex = entity instanceof FrostarmLawachurlEntity
                    ? getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/lawachurl/frostarm_lawachurl.png"))
                    : (entity instanceof ThunderhelmLawachurlEntity
                    ? getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/thunderhelm_lawachurl.png"))
                    : getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/lawachurl/stonehide_lawachurl.png")));
            blitNearest(g, tex, drawX, drawY, size, size, 50.0F, 108.0F, 10, 12, 224, 224);
            blitNearest(g, tex, drawX + 1, drawY + 1, size - 2, (size * 5) / 10, 55.0F, 25.0F, 10, 5, 224, 224);
            return true;
        }

        // 25. Magos del Abismo (Pyro, Hydro, Cryo, Electro)
        if (entity instanceof AbstractAbyssMageEntity mage) {
            ResourceLocation tex = getDynamicOrFallback(mage, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/abyss_mage_" + mage.getMageElement().name().toLowerCase(java.util.Locale.ROOT) + ".png"));
            int earW = Math.max(2, size / 4);
            int earH = Math.max(2, size / 2);
            blitNearest(g, tex, drawX - 1, drawY - earH / 2, earW, earH, 24.0F, 0.0F, 2, 4, 64, 64);
            blitNearest(g, tex, drawX + size - earW + 1, drawY - earH / 2, earW, earH, 24.0F, 0.0F, 2, 4, 64, 64);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            blitNearest(g, tex, drawX, drawY, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }

        // 26. Asesino Elfo Escarchado, Orco del Abismo, Nigromante, Soldado Hormiga, Ojo Abisal, Grulla Celestial
        if (entity instanceof FrostElfAssassinEntity elf) {
            ResourceLocation tex = getDynamicOrFallback(elf, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/frost_elf_assassin.png"));
            int earW = Math.max(2, size / 3);
            int earH = Math.max(1, size / 4);
            blitNearest(g, tex, drawX - 1, drawY + 2, earW, earH, 28.0F, 0.0F, 4, 2, 128, 128);
            blitNearest(g, tex, drawX + size - earW + 1, drawY + 2, earW, earH, 28.0F, 0.0F, 4, 2, 128, 128);
            blitNearest(g, tex, drawX, drawY, size, size, 7.0F, 7.0F, 7, 8, 128, 128);
            blitNearest(g, tex, drawX, drawY + size / 3, size, (size * 4) / 8, 47.0F, 11.0F, 8, 4, 128, 128);
            return true;
        }
        if (entity instanceof AbyssalOrcEntity orc) {
            ResourceLocation tex = getDynamicOrFallback(orc, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/abyssal_orc.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 9, 8, 128, 128);
            int tW = Math.max(2, size / 3);
            int tH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX + 1, drawY + size - tH, tW, tH, 36.0F, 0.0F, 2, 3, 128, 128);
            blitNearest(g, tex, drawX + size - tW - 1, drawY + size - tH, tW, tH, 36.0F, 0.0F, 2, 3, 128, 128);
            return true;
        }
        if (entity instanceof AbyssalNecromancerEntity necromancer) {
            ResourceLocation tex = getDynamicOrFallback(necromancer, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/abyssal_necromancer.png"));
            int hW = Math.max(2, size / 4);
            int hH = Math.max(2, size / 2);
            blitNearest(g, tex, drawX - 1, drawY - hH / 2, hW, hH, 68.0F, 0.0F, 2, 5, 128, 128);
            blitNearest(g, tex, drawX + size - hW + 1, drawY - hH / 2, hW, hH, 68.0F, 0.0F, 2, 5, 128, 128);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 128);
            blitNearest(g, tex, drawX - 1, drawY - 1, size + 2, size + 2, 41.0F, 9.0F, 9, 9, 128, 128);
            return true;
        }
        if (entity instanceof AntSoldierEntity ant) {
            ResourceLocation tex = getDynamicOrFallback(ant, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ant_soldier/ant_soldier.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 7.0F, 31.0F, 7, 7, 128, 128);
            int mW = Math.max(2, size / 4);
            int mH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX + 1, drawY + size - mH, mW, mH, 66.0F, 24.0F, 2, 3, 128, 128);
            blitNearest(g, tex, drawX + size - mW - 1, drawY + size - mH, mW, mH, 66.0F, 24.0F, 2, 3, 128, 128);
            g.fill(drawX + 2, drawY + size / 3, drawX + 4, drawY + size / 3 + 2, 0xFFFF0033);
            g.fill(drawX + size - 4, drawY + size / 3, drawX + size - 2, drawY + size / 3 + 2, 0xFFFF0033);
            return true;
        }
        if (entity instanceof AbyssalBeholderEntity beholder) {
            ResourceLocation tex = getDynamicOrFallback(beholder, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/abyssal_beholder.png"));
            g.fill(drawX, drawY, drawX + size, drawY + size, 0xFF4A0033);
            blitNearest(g, tex, drawX, drawY, size, size, 16.0F, 16.0F, 16, 16, 128, 128);
            blitNearest(g, tex, drawX + 1, drawY + 1, size - 2, size - 2, 68.0F, 2.0F, 12, 12, 128, 128);
            g.fill(drawX + size / 2 - 1, drawY + size / 2 - 1, drawX + size / 2 + 1, drawY + size / 2 + 1, 0xFFFF0000);
            return true;
        }
        if (entity instanceof CloudCraneEntity crane) {
            ResourceLocation tex = getDynamicOrFallback(crane, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/cloud_crane/cloud_crane.png"));
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x3300E5FF);
            blitNearest(g, tex, drawX, drawY, size, size, 33.0F, 25.0F, 3, 3, 128, 128);
            int bkW = Math.max(2, size / 3);
            int bkH = Math.max(2, size / 2);
            blitNearest(g, tex, drawX + (size - bkW) / 2, drawY + size - bkH, bkW, bkH, 46.0F, 20.0F, 1, 2, 128, 128);
            return true;
        }

        // 27. Bestia Acorazada de Jade, Geovishap y Dragarto del Abismo
        if (entity instanceof JadeArmoredBeastEntity || entity instanceof GeovishapEntity || entity instanceof BathysmalVishapEntity) {
            ResourceLocation tex = entity instanceof BathysmalVishapEntity
                    ? getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/primordial_bathysmal_vishap.png"))
                    : (entity instanceof GeovishapEntity
                    ? getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/geovishap.png"))
                    : getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/murim/jade_armored_beast.png")));
            int hW = Math.max(2, size / 3);
            int hH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX + (size - hW) / 2, drawY - 2, hW, hH, 102.0F, 0.0F, 2, 5, 128, 64);
            blitNearest(g, tex, drawX, drawY, size, size, 56.0F, 6.0F, 7, 6, 128, 64);
            return true;
        }

        // 28. Paimon, NPCs de Serie, Compañeros y Corredoras Uma
        if (entity instanceof PaimonEntity paimon) {
            ResourceLocation tex = getDynamicOrFallback(paimon, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/paimon.png"));
            g.renderOutline(drawX + 1, drawY - 3, size - 2, 2, 0xFFFFD700);
            blitNearest(g, tex, drawX, drawY, size, size, 26.0F, 24.0F, 6, 5, 80, 80);
            return true;
        }
        if (entity instanceof SeriesNpcEntity npc) {
            ResourceLocation skin = npc.getNpcProfile().getTextureLocation();
            blitNearest(g, skin, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            blitNearest(g, skin, drawX, drawY, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }
        if (entity instanceof CompanionEntity comp) {
            ResourceLocation skin = comp.getCompanionProfile() != null ? comp.getCompanionProfile().getTextureLocation()
                    : ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/companion/noelle.png");
            blitNearest(g, skin, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            blitNearest(g, skin, drawX, drawY, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }
        if (entity instanceof UmaRacerEntity uma) {
            ResourceLocation skin = getDynamicOrFallback(uma, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/uma/tracen_default.png"));
            int earW = Math.max(2, size / 4);
            g.fill(drawX, drawY - 2, drawX + earW, drawY, 0xFF8B5A2B);
            g.fill(drawX + size - earW, drawY - 2, drawX + size, drawY, 0xFF8B5A2B);
            blitNearest(g, skin, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            blitNearest(g, skin, drawX, drawY, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            return true;
        }

        // 29. Jefes Épicos y Mitológicos
        if (entity instanceof YggdrasilTreantBossEntity treant) {
            ResourceLocation tex = getDynamicOrFallback(treant, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/yggdrasil_treant.png"));
            blitNearest(g, tex, drawX - 1, drawY - 2, size + 2, size / 3, 50.0F, 10.0F, 10, 4, 128, 128);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 128);
            return true;
        }
        if (entity instanceof JadeGuardianBossEntity guardian) {
            ResourceLocation tex = getDynamicOrFallback(guardian, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/boss/jade_guardian.png"));
            int hW = Math.max(2, size / 3);
            int hH = Math.max(2, size / 3);
            blitNearest(g, tex, drawX + (size - hW) / 2, drawY - 2, hW, hH, 35.0F, 4.0F, 3, 4, 128, 64);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 64);
            return true;
        }
        if (entity instanceof ElementalDemonKingBossEntity demonKing) {
            ResourceLocation tex = getDynamicOrFallback(demonKing, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/boss/elemental_demon_king.png"));
            blitNearest(g, tex, drawX - 1, drawY - 2, size + 2, size / 3, 41.0F, 9.0F, 9, 9, 128, 128);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 128, 128);
            return true;
        }
        if (entity instanceof GreatSnowboarKingBossEntity boar) {
            ResourceLocation tex = getDynamicOrFallback(boar, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/snowboar_king.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 24.0F, 24.0F, 24, 24, 192, 96);
            int tW = Math.max(3, (size * 5) / 8);
            int tH = Math.max(2, (size * 4) / 8);
            blitNearest(g, tex, drawX + (size - tW) / 2, drawY + size - tH, tW, tH, 51.0F, 51.0F, 12, 9, 192, 96);
            return true;
        }
        if (entity instanceof RhodeiaOceanidBossEntity oceanid) {
            ResourceLocation tex = getDynamicOrFallback(oceanid, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/hateful_oceanid.png"));
            g.fill(drawX - 1, drawY - 1, drawX + size + 1, drawY + size + 1, 0x662196F3);
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 20.0F, 8, 8, 64, 64);
            return true;
        }

        // 30. Maquinaria de las Ruinas
        if (entity instanceof RuinGuardEntity || entity instanceof RuinGraderEntity) {
            ResourceLocation tex = entity instanceof RuinGraderEntity
                    ? getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_grader.png"))
                    : getDynamicOrFallback(entity, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_guard/ruin_guard.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 116.0F, 176.0F, 19, 11, 256, 256);
            g.fill(drawX + size / 2 - 1, drawY + size / 2 - 1, drawX + size / 2 + 1, drawY + size / 2 + 1, 0xFFFFB800);
            return true;
        }
        if (entity instanceof RuinHunterEntity hunter) {
            ResourceLocation tex = getDynamicOrFallback(hunter, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_hunter/ruin_hunter.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 21.0F, 102.0F, 34, 26, 160, 160);
            g.fill(drawX + size / 2 - 1, drawY + size / 2 - 1, drawX + size / 2 + 1, drawY + size / 2 + 1, 0xFFFF5500);
            return true;
        }
        if (entity instanceof RuinSentinelEntity sentinel) {
            ResourceLocation tex = getDynamicOrFallback(sentinel, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_cruiser.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            blitNearest(g, tex, drawX + 1, drawY + 1, size - 2, size - 2, 34.0F, 2.0F, 6, 6, 64, 64);
            return true;
        }
        if (entity instanceof RuinDrakeEntity drake) {
            ResourceLocation tex = getDynamicOrFallback(drake, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_drake_skywatch.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 18.0F, 106.0F, 12, 14, 256, 256);
            return true;
        }
        if (entity instanceof RuinSerpentBossEntity serpent) {
            ResourceLocation tex = getDynamicOrFallback(serpent, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ruin_serpent_remodel_fixed.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 14.0F, 14.0F, 12, 12, 128, 128);
            int dW = Math.max(2, size / 2);
            int dH = Math.max(2, size / 2);
            blitNearest(g, tex, drawX + (size - dW) / 2, drawY + (size - dH) / 2, dW, dH, 58.0F, 20.0F, 4, 4, 128, 128);
            return true;
        }
        if (entity instanceof PerpetualMechanicalArrayBossEntity arrayBoss) {
            ResourceLocation tex = getDynamicOrFallback(arrayBoss, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/perpetual_mechanical_array.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 16.0F, 16.0F, 16, 16, 128, 128);
            g.renderOutline(drawX, drawY, size, size, 0xFFFFD700);
            return true;
        }

        // 31. Hipóstasis Celestiales (Anemo, Electro, Geo, Cryo, Pyro, Hydro, Dendro)
        if (entity instanceof HypostasisBaseBossEntity hyp) {
            ResourceLocation tex = getDynamicOrFallback(hyp, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/hypostasis/anemo_hypostasis.png"));
            int glowCol = hyp.getElementalColorHex() | 0xFF000000;
            g.fill(drawX, drawY, drawX + size, drawY + size, glowCol & 0x44FFFFFF);
            blitNearest(g, tex, drawX + 1, drawY + 1, size - 2, size - 2, 16.0F, 16.0F, 16, 16, 128, 128);
            g.renderOutline(drawX, drawY, size, size, glowCol);
            return true;
        }

        // 32. Vides Regisvine (Cryo, Pyro, Electro Regisvine)
        if (entity instanceof CryoRegisvineEntity || entity instanceof PyroRegisvineEntity || entity instanceof ElectroRegisvineEntity) {
            ResourceLocation fallbackTex = entity instanceof CryoRegisvineEntity
                    ? ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/regisvine/cryo_regisvine.png")
                    : (entity instanceof PyroRegisvineEntity
                    ? ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/regisvine/pyro_regisvine.png")
                    : ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/regisvine/electro_regisvine.png"));
            ResourceLocation tex = getDynamicOrFallback(entity, fallbackTex);
            int elemGlow = entity instanceof CryoRegisvineEntity ? 0xFF80DEEA : (entity instanceof PyroRegisvineEntity ? 0xFFFF5722 : 0xFF9C27B0);
            g.fill(drawX, drawY, drawX + size, drawY + size, elemGlow & 0x44FFFFFF);
            blitNearest(g, tex, drawX, drawY, size, size, 75.0F, 26.0F, 7, 6, 128, 128);
            return true;
        }

        // 33. Megafloras y Espectros Elementales
        if (entity instanceof WhopperflowerEntity wf) {
            ResourceLocation tex = getDynamicOrFallback(wf, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/" + wf.getFlowerType().getId() + "_whopperflower.png"));
            int elemColor = wf.getFlowerType().getColor() | 0xFF000000;
            g.fill(drawX, drawY, drawX + size, drawY + size, elemColor & 0x44FFFFFF);
            blitNearest(g, tex, drawX, drawY, size, size, 0.0F, 0.0F, 8, 8, 64, 64);
            return true;
        }
        if (entity instanceof SpecterEntity specter) {
            ResourceLocation tex = getDynamicOrFallback(specter, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/specter_" + specter.getElement().getId() + ".png"));
            int elemColor = specter.getElement().getColor() | 0xFF000000;
            g.fill(drawX, drawY, drawX + size, drawY + size, elemColor & 0x44FFFFFF);
            blitNearest(g, tex, drawX, drawY, size, size, 12.0F, 12.0F, 12, 12, 64, 64);
            return true;
        }

        // 34. Azhdaha y Rey Acechador Dorado (Wolflord)
        if (entity instanceof AzhdahaBossEntity azhdaha) {
            ResourceLocation tex = getDynamicOrFallback(azhdaha, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/boss/azhdaha.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 508.0F, 407.0F, 37, 16, 816, 816);
            return true;
        }
        if (entity instanceof GoldenWolflordBossEntity wolflord) {
            ResourceLocation tex = getDynamicOrFallback(wolflord, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/boss/golden_wolflord.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 24.0F, 24.0F, 24, 24, 256, 256);
            return true;
        }

        // 35. Dragones Ancestrales, Wyvern y Dragón Astral
        if (entity instanceof WyvernEntity wyvern) {
            ResourceLocation tex = getDynamicOrFallback(wyvern, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/wyvern.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 73.0F, 9.0F, 8, 6, 128, 128);
            return true;
        }
        if (entity instanceof CelestialDragonEntity celestial) {
            ResourceLocation tex = getDynamicOrFallback(celestial, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/celestial_dragon.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 43.0F, 8.0F, 6, 4, 128, 128);
            return true;
        }
        if (entity instanceof AncientCelestialDragonBossEntity ancientDragon) {
            ResourceLocation tex = getDynamicOrFallback(ancientDragon, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon_boss.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 43.0F, 8.0F, 6, 4, 128, 128);
            return true;
        }
        if (entity instanceof AstralDragonEntity astral) {
            ResourceLocation tex = getDynamicOrFallback(astral, ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/astral_dragon/astral_dragon.png"));
            blitNearest(g, tex, drawX, drawY, size, size, 54.0F, 4.0F, 5, 4, 128, 128);
            return true;
        }
        if (entity instanceof AncientDragonEntity ad) {
            ResourceLocation fallbackTex = switch (ad.getVariant()) {
                case AncientDragonEntity.VARIANT_ICE -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_ice.png");
                case AncientDragonEntity.VARIANT_FOREST -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_forest.png");
                case AncientDragonEntity.VARIANT_SHADOW -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_shadow.png");
                case AncientDragonEntity.VARIANT_LIGHTNING -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_lightning.png");
                case AncientDragonEntity.VARIANT_CELESTIAL -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_celestial.png");
                default -> ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/entity/ancient_dragon/ancient_dragon_fire.png");
            };
            ResourceLocation tex = getDynamicOrFallback(ad, fallbackTex);
            blitNearest(g, tex, drawX, drawY, size, size, 122.0F, 154.0F, 8, 6, 256, 256);
            return true;
        }

        return false;
    }

    private static void blitNearest(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, float u, float v, int uW, int vH, int texW, int texH) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(tex, x, y, w, h, u, v, uW, vH, texW, texH);
    }

    private static final java.util.Map<Integer, ResourceLocation> ENTITY_TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearTextureCache() {
        ENTITY_TEXTURE_CACHE.clear();
    }

    private static ResourceLocation getDynamicTexture(LivingEntity entity) {
        if (entity == null) return null;
        int id = entity.getId();
        ResourceLocation cached = ENTITY_TEXTURE_CACHE.get(id);
        if (cached != null) return cached;

        try {
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer != null) {
                ResourceLocation loc = renderer.getTextureLocation(entity);
                if (loc != null) {
                    if (ENTITY_TEXTURE_CACHE.size() > 300) {
                        ENTITY_TEXTURE_CACHE.clear();
                    }
                    ENTITY_TEXTURE_CACHE.put(id, loc);
                    return loc;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static ResourceLocation getDynamicOrFallback(LivingEntity entity, ResourceLocation fallback) {
        ResourceLocation dyn = getDynamicTexture(entity);
        return dyn != null ? dyn : fallback;
    }

    /**
     * Dibuja el marcador del jugador con la cara de su skin y una sutil aguja direccional en el borde.
     */
    public static void drawPlayerMarker(GuiGraphics g, Player player, int px, int py, float yawDegrees, int size) {
        if (player == null) {
            drawPlayerArrow(g, px, py, yawDegrees);
            return;
        }

        var pose = g.pose();
        pose.pushPose();
        pose.translate(px, py, 0);

        // 1. Sombra suave circular compacta
        g.fill(-size / 2 - 1, -size / 2 - 1, size / 2 + 1, size / 2 + 1, 0x66000000);

        // 2. Rostro del skin del jugador
        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerFaceRenderer.draw(g, clientPlayer.getSkin(), -size / 2, -size / 2, size);
        } else {
            g.fill(-size / 2, -size / 2, size / 2, size / 2, 0xFF00E5FF);
        }

        // 3. Borde elegante sci-fi
        g.renderOutline(-size / 2 - 1, -size / 2 - 1, size + 2, size + 2, 0xFF4EE2EC);

        // 4. Pequeña flecha direccional compacta en el borde
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(yawDegrees + 180.0F));
        int tipY = -size / 2 - 2;
        g.fill(-1, tipY, 2, tipY + 1, 0xFF00E5FF);
        g.fill(0, tipY - 1, 1, tipY, 0xFFFFFFFF);
        pose.popPose();

        pose.popPose();
    }

    /**
     * Dibuja el marcador del jugador (mantiene compatibilidad hacia atrás).
     */
    public static void drawPlayerArrow(GuiGraphics g, int px, int py, float yawDegrees) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            drawPlayerMarker(g, mc.player, px, py, yawDegrees, 8);
            return;
        }

        var pose = g.pose();
        pose.pushPose();
        pose.translate(px, py, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(yawDegrees + 180.0F));

        g.fill(-3, -5, 4, 5, 0x55000000);
        g.fill(-1, -6, 2, -4, 0xFF440000);
        g.fill(-2, -4, 3, -2, 0xFFFF2222);
        g.fill(-3, -2, 4, 0, 0xFFFF2222);
        g.fill(0, -5, 1, -4, 0xFFFFAAAA);

        pose.popPose();
    }
}
