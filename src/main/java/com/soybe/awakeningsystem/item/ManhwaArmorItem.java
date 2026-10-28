package com.soybe.awakeningsystem.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class ManhwaArmorItem extends ArmorItem implements GeoItem {
    public enum SetType {
        SOUL_ALLOY("soul_alloy", "§b§l[SET: FORJA DE ALMAS ARDIENTES]",
                "§74 Piezas: §bInmunidad a daño de Wither y Fuego de Almas. Las almas atrapadas devoran la vida enemiga curándote un 15% del daño infligido. [Z] Almas Voraces (Onda de choque espectral)."),
        ABYSSAL("abyssal", "§9§l[SET: PRIMORDIAL ABYSS RESONANCE]",
                "§74 Pieces: §9+20% Resistance in caverns, passive subterranean night vision. [Z] Abyssal Vortex (Gravity Collapse)."),
        MITHRIL("mithril", "§b§l[SET: ELVEN MITHRIL GRACE]",
                "§74 Pieces: §b+25% Passive movement speed, slow fall when sneaking mid-air. [Z] Dimensional Blink (Sonic Dash)."),
        ADAMANTITE("adamantite", "§c§l[SET: ADAMANTITE VOLCANIC FURY]",
                "§74 Pieces: §cTotal immunity to fire and lava, +60% damage at low health. [Z] Magma Fissure (Volcanic Eruption)."),
        SPIRIT_JADE("spirit_jade", "§a§l[SET: SPIRIT JADE IMMORTAL BODY]",
                "§74 Pieces: §a25% damage taken converted into Qi, Dao purification. [Z] Golden Bell (Invulnerability & Reflection)."),
        ASTRAL("astral", "§5§l[SET: CELESTIAL DRAGON SOVEREIGNTY]",
                "§74 Pieces: §5Cosmic Agility (Speed II, Jump Boost II), fall immunity & void rescue. [Z] Astral Burst (Celestial Dragon Supernova)."),
        ASTRALITE("astralite", "§d§l[SET: SOBERANÍA CÓSMICA DE ASTRALITA]",
                "§74 Piezas: §dResonancia estelar del End. Inmunidad a daño de vacío y caída, +35% Maná máximo. [Z] Supernova Astral (Estallido estelar y colapso gravitacional)."),
        MORA("mora", "§e§l[SET: MORA GOLDEN BARRIER]",
                "§74 Pieces: §eCommerce God's Shield. Lethal/critical damage is automatically absorbed by spending Coins/Mora to save your life."),
        CELESTIAL("celestial", "§b§l[SET: TEYVAT CELESTIAL BLESSING]",
                "§74 Pieces: §bCreative flight, +30% Damage, divine regen & celestial speed. [Z] Celestial Radiance (Stellar Comet Shower)."),
        PRIMORDIAL("primordial", "§d§l[SET: PRIMORDIAL GENESIS]",
                "§74 Pieces: §dCosmic flight, +40% Crit Damage, void & abyssal corruption immunity. [Z] Genesis Singularity (Destructive Cosmic Collapse).");

        public final String id;
        public final String header;
        public final String bonusDesc;

        SetType(String id, String header, String bonusDesc) {
            this.id = id;
            this.header = header;
            this.bonusDesc = bonusDesc;
        }
    }

    private final SetType setType;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ManhwaArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, SetType setType) {
        super(material, type, properties);
        this.setType = setType;
    }

    public SetType getSetType() {
        return setType;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        if (setType == SetType.ABYSSAL) {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/abyssal_armor.png");
        }
        if (setType == SetType.SOUL_ALLOY) {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/soulforge_armor.png");
        }
        if (setType == SetType.ASTRALITE) {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/astralite_armor.png");
        }
        String layerNum = (slot == EquipmentSlot.LEGS || inner) ? "2" : "1";
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/" + setType.id + "_layer_" + layerNum + ".png");
    }

    public static SetType getFullSet(Player player) {
        if (player == null) return null;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty() || !(head.getItem() instanceof ManhwaArmorItem hMai)) return null;
        SetType type = hMai.getSetType();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ManhwaArmorItem cMai) || cMai.getSetType() != type) return null;

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (legs.isEmpty() || !(legs.getItem() instanceof ManhwaArmorItem lMai) || lMai.getSetType() != type) return null;

        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        if (feet.isEmpty() || !(feet.getItem() instanceof ManhwaArmorItem fMai) || fMai.getSetType() != type) return null;

        return type;
    }

    public static boolean hasFullSet(Player player, SetType set) {
        return getFullSet(player) == set;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.armor_set." + setType.id + ".header"));
        tooltip.add(Component.translatable("awakening_system.armor_set." + setType.id + ".desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private com.soybe.awakeningsystem.item.client.AbyssalArmorRenderer abyssalRenderer;
            private com.soybe.awakeningsystem.item.client.SoulforgeArmorRenderer soulforgeRenderer;
            private com.soybe.awakeningsystem.item.client.AstraliteArmorRenderer astraliteRenderer;

            @Override
            public <T extends LivingEntity> net.minecraft.client.model.HumanoidModel<?> getGeoArmorRenderer(T entity, ItemStack stack, EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<T> original) {
                if (setType == SetType.ABYSSAL) {
                    if (this.abyssalRenderer == null) {
                        this.abyssalRenderer = new com.soybe.awakeningsystem.item.client.AbyssalArmorRenderer();
                    }
                    this.abyssalRenderer.prepForRender(entity, stack, slot, original);
                    return this.abyssalRenderer;
                }
                if (setType == SetType.SOUL_ALLOY) {
                    if (this.soulforgeRenderer == null) {
                        this.soulforgeRenderer = new com.soybe.awakeningsystem.item.client.SoulforgeArmorRenderer();
                    }
                    this.soulforgeRenderer.prepForRender(entity, stack, slot, original);
                    return this.soulforgeRenderer;
                }
                if (setType == SetType.ASTRALITE) {
                    if (this.astraliteRenderer == null) {
                        this.astraliteRenderer = new com.soybe.awakeningsystem.item.client.AstraliteArmorRenderer();
                    }
                    this.astraliteRenderer.prepForRender(entity, stack, slot, original);
                    return this.astraliteRenderer;
                }
                return null;
            }
        });
    }
}
