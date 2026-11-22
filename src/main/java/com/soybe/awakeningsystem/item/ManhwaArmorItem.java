package com.soybe.awakeningsystem.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
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
        SIDERITE("siderite", "§e§l[SET: SIDERITA METEÓRICA]",
                "§7Set Siderita: §eVuelo celestial con Alas Siderales (estilo Terraria), planeo en caída [Shift] e inmunidad total a caída. Mayor velocidad bajo el cielo nocturno. [Z] Lluvia de Fragmentos Siderales."),
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
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        if (super.supportsEnchantment(stack, enchantment)) return true;
        net.minecraft.resources.ResourceLocation loc = enchantment.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
        if (loc == null) return false;
        String path = loc.getPath().toLowerCase(java.util.Locale.ROOT);
        if (path.contains("unbreaking") || path.contains("mending") || path.contains("protection") || path.contains("thorns")) {
            return true;
        }
        if (this.getType() == Type.HELMET && (path.contains("respiration") || path.contains("aqua_affinity"))) {
            return true;
        }
        if (this.getType() == Type.LEGGINGS && path.contains("swift_sneak")) {
            return true;
        }
        if (this.getType() == Type.BOOTS && (path.contains("feather_falling") || path.contains("depth_strider") || path.contains("frost_walker") || path.contains("soul_speed"))) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        if (super.isPrimaryItemFor(stack, enchantment)) return true;
        return supportsEnchantment(stack, enchantment);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        // High-level RPG mobs deal hundreds or thousands of damage per strike.
        // In vanilla, damage / 4 would instantly disintegrate armor durability in 2-3 hits.
        // We cap the durability wear to at most 1 point per attack,
        // and give high tiers natural deflection resistance:
        if (setType == SetType.PRIMORDIAL) {
            if (entity != null && entity.getRandom().nextFloat() < 0.75F) return 0;
        } else if (setType == SetType.CELESTIAL || setType == SetType.ASTRAL || setType == SetType.ASTRALITE) {
            if (entity != null && entity.getRandom().nextFloat() < 0.50F) return 0;
        } else if (setType == SetType.SPIRIT_JADE || setType == SetType.ADAMANTITE || setType == SetType.SIDERITE) {
            if (entity != null && entity.getRandom().nextFloat() < 0.25F) return 0;
        }
        return Math.min(amount, 1);
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
        if (setType == SetType.SIDERITE) {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/siderite_armor.png");
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
        controllers.add(new software.bernie.geckolib.animation.AnimationController<>(this, "controller", 0, state -> software.bernie.geckolib.animation.PlayState.STOP));
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
            private com.soybe.awakeningsystem.item.client.SideriteArmorRenderer sideriteRenderer;

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
                if (setType == SetType.SIDERITE) {
                    if (this.sideriteRenderer == null) {
                        this.sideriteRenderer = new com.soybe.awakeningsystem.item.client.SideriteArmorRenderer();
                    }
                    this.sideriteRenderer.prepForRender(entity, stack, slot, original);
                    return this.sideriteRenderer;
                }
                return null;
            }
        });
    }
}
