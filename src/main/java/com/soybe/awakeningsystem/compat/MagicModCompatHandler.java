package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Optional;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class MagicModCompatHandler {
    public static final String IRONS_SPELLS = "irons_spellbooks";
    public static final String ARS_NOUVEAU = "ars_nouveau";

    private MagicModCompatHandler() {}

    public static boolean isIronsSpellsLoaded() {
        return ModList.get().isLoaded(IRONS_SPELLS);
    }

    public static boolean isArsNouveauLoaded() {
        return ModList.get().isLoaded(ARS_NOUVEAU);
    }

    /**
     * Aplica modificadores dinámicos de poder mágico, reserva de maná y afinidades elementales
     * según las estadísticas de Inteligencia, Nivel, Clase de Cazador y Visión elemental del jugador.
     */
    public static void applyMagicAttributes(ServerPlayer player, SystemData data) {
        if (player == null || data == null) return;

        int intStat = data.intelligence();
        String hClass = data.hunterClass();
        String vision = data.activeVision() != null ? data.activeVision().toUpperCase(java.util.Locale.ROOT) : "NONE";

        // ==========================================
        // 1. SINERGIA CON IRON'S SPELLS 'N SPELLBOOKS
        // ==========================================
        if (isIronsSpellsLoaded()) {
            // A. Maná Máximo (+2 maná por punto de INT arriba de 10)
            double extraMaxMana = Math.max(0.0, (intStat - 10) * 2.0);
            if ("grand_archmage".equals(hClass)) extraMaxMana += 60.0;
            if ("spiritual_healer".equals(hClass)) extraMaxMana += 40.0;
            applyModifier(player, IRONS_SPELLS, "max_mana", "irons_compat_max_mana", extraMaxMana, AttributeModifier.Operation.ADD_VALUE);

            // B. Poder de Hechizos Global (+0.5% por punto de INT)
            double globalSpellPower = Math.max(0.0, (intStat - 10) * 0.005);
            if ("grand_archmage".equals(hClass)) globalSpellPower += 0.25;
            if ("star_apostle".equals(hClass)) globalSpellPower += 0.15;
            applyModifier(player, IRONS_SPELLS, "spell_power", "irons_compat_spell_power", globalSpellPower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            // C. Regeneración de Maná (+0.8% por punto de INT)
            double manaRegen = Math.max(0.0, (intStat - 10) * 0.008);
            if ("grand_archmage".equals(hClass)) manaRegen += 0.20;
            if ("HYDRO".equals(vision)) manaRegen += 0.15;
            applyModifier(player, IRONS_SPELLS, "mana_regen", "irons_compat_mana_regen", manaRegen, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            // D. Reducción de Enfriamientos (Cooldown Reduction, cap 30%)
            double cdr = Math.min(0.30, Math.max(0.0, (intStat - 10) * 0.003));
            if ("grand_archmage".equals(hClass)) cdr += 0.10;
            if ("ANEMO".equals(vision)) cdr += 0.10;
            applyModifier(player, IRONS_SPELLS, "cooldown_reduction", "irons_compat_cdr", cdr, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            // E. Afinidades Elementales y Escuelas de Magia
            double firePower = ("dragon_monarch".equals(hClass) ? 0.30 : 0.0) + ("PYRO".equals(vision) ? 0.20 : 0.0);
            applyModifier(player, IRONS_SPELLS, "fire_spell_power", "irons_compat_fire", firePower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double icePower = ("CRYO".equals(vision) ? 0.25 : 0.0);
            applyModifier(player, IRONS_SPELLS, "ice_spell_power", "irons_compat_ice", icePower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double lightningPower = ("lightning_emperor".equals(hClass) ? 0.35 : 0.0) + ("ELECTRO".equals(vision) ? 0.20 : 0.0);
            applyModifier(player, IRONS_SPELLS, "lightning_spell_power", "irons_compat_lightning", lightningPower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double holyPower = ("spiritual_healer".equals(hClass) ? 0.35 : 0.0);
            applyModifier(player, IRONS_SPELLS, "holy_spell_power", "irons_compat_holy", holyPower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double bloodPower = ("blood_emperor".equals(hClass) ? 0.35 : 0.0);
            applyModifier(player, IRONS_SPELLS, "blood_spell_power", "irons_compat_blood", bloodPower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double enderPower = ("shadow_monarch".equals(hClass) ? 0.30 : 0.0) + ("void_assassin".equals(hClass) ? 0.15 : 0.0);
            applyModifier(player, IRONS_SPELLS, "ender_spell_power", "irons_compat_ender", enderPower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double naturePower = ("DENDRO".equals(vision) ? 0.25 : 0.0);
            applyModifier(player, IRONS_SPELLS, "nature_spell_power", "irons_compat_nature", naturePower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            applyModifier(player, IRONS_SPELLS, "evocation_spell_power", "irons_compat_evocation", naturePower, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }

        // ==========================================
        // 2. SINERGIA CON ARS NOUVEAU
        // ==========================================
        if (isArsNouveauLoaded()) {
            double arsMana = Math.max(0.0, (intStat - 10) * 1.5);
            if ("grand_archmage".equals(hClass)) arsMana += 50.0;
            applyModifier(player, ARS_NOUVEAU, "ars_nouveau.perk.max_mana", "ars_compat_max_mana", arsMana, AttributeModifier.Operation.ADD_VALUE);
            applyModifier(player, ARS_NOUVEAU, "max_mana", "ars_compat_max_mana_alt", arsMana, AttributeModifier.Operation.ADD_VALUE);

            double arsDmg = Math.max(0.0, (intStat - 10) * 0.005);
            if ("grand_archmage".equals(hClass)) arsDmg += 0.25;
            applyModifier(player, ARS_NOUVEAU, "ars_nouveau.perk.spell_damage", "ars_compat_spell_damage", arsDmg, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            applyModifier(player, ARS_NOUVEAU, "spell_damage", "ars_compat_spell_damage_alt", arsDmg, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            double arsRegen = Math.max(0.0, (intStat - 10) * 0.006);
            if ("HYDRO".equals(vision)) arsRegen += 0.15;
            applyModifier(player, ARS_NOUVEAU, "ars_nouveau.perk.mana_regen", "ars_compat_mana_regen", arsRegen, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            applyModifier(player, ARS_NOUVEAU, "mana_regen", "ars_compat_mana_regen_alt", arsRegen, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    }

    private static void applyModifier(ServerPlayer player, String namespace, String path, String modIdName, double value, AttributeModifier.Operation op) {
        ResourceLocation attrLoc = ResourceLocation.fromNamespaceAndPath(namespace, path);
        Optional<Holder.Reference<Attribute>> opt = BuiltInRegistries.ATTRIBUTE.getHolder(attrLoc);
        if (opt.isPresent()) {
            AttributeInstance instance = player.getAttribute(opt.get());
            if (instance != null) {
                ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, modIdName);
                instance.removeModifier(modifierId);
                if (Math.abs(value) > 0.0001) {
                    instance.addPermanentModifier(new AttributeModifier(modifierId, value, op));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCombatDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && player.server != null) {
            String msgId = event.getSource().getMsgId();
            boolean isMagicDamage = event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MAGIC)
                    || event.getSource().is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)
                    || (msgId != null && (msgId.contains("spell") || msgId.contains("magic") || msgId.contains("irons_spellbooks") || msgId.contains("ars_nouveau")));

            if (isMagicDamage) {
                SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (data != null) {
                    float bonus = 1.0F;
                    if ("grand_archmage".equals(data.hunterClass())) {
                        bonus += 0.20F; // +20% a todos los hechizos
                    }
                    if (data.intelligence() > 20) {
                        bonus += (data.intelligence() - 20) * 0.003F; // +0.3% por punto de INT alto
                    }
                    if (bonus > 1.0F) {
                        event.setNewDamage(event.getNewDamage() * bonus);
                    }
                }
            }
        }
    }
}
