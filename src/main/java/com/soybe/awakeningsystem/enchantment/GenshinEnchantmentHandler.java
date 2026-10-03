package com.soybe.awakeningsystem.enchantment;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GenshinEnchantmentHandler {

    private GenshinEnchantmentHandler() {}

    public static int getEnchantmentLevel(ItemStack stack, String enchantmentPath) {
        if (stack.isEmpty()) return 0;
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return 0;
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (holder.unwrapKey().map(k -> k.location().getPath().equals(enchantmentPath)).orElse(false)) {
                return enchants.getLevel(holder);
            }
        }
        return 0;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;

        ServerLevel sl = (ServerLevel) target.level();
        String targetName = target.getType().toString().toLowerCase();

        // 1. Electric Absorb (Absorción Eléctrica)
        int lvlElectric = getEnchantmentLevel(weapon, "electric_absorb");
        if (lvlElectric > 0) {
            if (targetName.contains("electro") || target.isInWaterOrRain()) {
                event.setAmount(event.getAmount() + (4.0F * lvlElectric));
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
            }
        }

        // 2. Icebreaker (Rompehielos)
        int lvlIce = getEnchantmentLevel(weapon, "icebreaker");
        if (lvlIce > 0) {
            if (targetName.contains("cryo") || target.getTicksFrozen() > 0 || targetName.contains("ice")) {
                event.setAmount(event.getAmount() + (5.0F * lvlIce));
                sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.4F);
            }
        }

        // 3. Rock Smasher (Destructor de Rocas)
        int lvlRock = getEnchantmentLevel(weapon, "rock_smasher");
        if (lvlRock > 0) {
            if (targetName.contains("geo") || targetName.contains("stone") || targetName.contains("golem") || targetName.contains("vishap") || targetName.contains("lawachurl")) {
                event.setAmount(event.getAmount() + (4.5F * lvlRock));
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.4, 0.4, 0.2);
            }
        }

        // 4. Soft Amber (Ámbar Suave)
        int lvlAmber = getEnchantmentLevel(weapon, "soft_amber");
        if (lvlAmber > 0) {
            if (sl.random.nextFloat() < 0.35F) {
                target.setRemainingFireTicks(120 * lvlAmber);
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            }
        }

        // 5. Windblower (Ráfaga de Viento)
        int lvlWind = getEnchantmentLevel(weapon, "windblower");
        if (lvlWind > 0) {
            if (targetName.contains("anemo") || !target.onGround()) {
                event.setAmount(event.getAmount() + (3.5F * lvlWind));
                target.knockback(0.6 * lvlWind, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
        }

        // 6. Fire Extinguisher (Extintor de Fuego)
        int lvlFire = getEnchantmentLevel(weapon, "fire_extinguisher");
        if (lvlFire > 0) {
            if (targetName.contains("pyro") || target.isOnFire() || targetName.contains("blaze") || targetName.contains("magma")) {
                event.setAmount(event.getAmount() + (4.5F * lvlFire));
                if (target.isOnFire()) {
                    target.clearFire();
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
            }
        }

        // 7. Hydrophobia (Hidrofobia)
        int lvlHydro = getEnchantmentLevel(weapon, "hydrophobia");
        if (lvlHydro > 0) {
            if (targetName.contains("hydro") || targetName.contains("water") || targetName.contains("mimic") || targetName.contains("guardian")) {
                event.setAmount(event.getAmount() + (4.0F * lvlHydro));
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.4, 0.4, 0.2);
            }
        }

        // 8. Magic Destroyer (Destructor Arcano)
        int lvlMagic = getEnchantmentLevel(weapon, "magic_destroyer");
        if (lvlMagic > 0) {
            if (targetName.contains("abyss") || targetName.contains("ruin") || targetName.contains("husk") || targetName.contains("rifthound")) {
                event.setAmount(event.getAmount() + (5.0F * lvlMagic));
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 18, 0.5, 0.5, 0.5, 0.2);
            }
        }

        // 9. Enemy of Nature (Enemigo de la Naturaleza)
        int lvlNature = getEnchantmentLevel(weapon, "enemy_of_nature");
        if (lvlNature > 0) {
            if (targetName.contains("dendro") || targetName.contains("fungus") || targetName.contains("shroom") || targetName.contains("whopperflower")) {
                event.setAmount(event.getAmount() + (4.5F * lvlNature));
                sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.4, 0.4, 0.1);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile proj = event.getProjectile();
        if (proj.level().isClientSide()) return;
        if (!(proj.getOwner() instanceof LivingEntity owner)) return;

        ItemStack bow = owner.getMainHandItem();
        int lvlShock = getEnchantmentLevel(bow, "shocking_shooting");
        if (lvlShock <= 0) {
            lvlShock = getEnchantmentLevel(owner.getOffhandItem(), "shocking_shooting");
        }

        if (lvlShock > 0) {
            ServerLevel sl = (ServerLevel) proj.level();
            if (sl.random.nextFloat() < (0.35F * lvlShock)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(proj.position());
                    bolt.setVisualOnly(false);
                    sl.addFreshEntity(bolt);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        // Registrar contador total de pesca
        int currentTotal = sp.getPersistentData().getInt("total_fish_caught");
        sp.getPersistentData().putInt("total_fish_caught", currentTotal + 1);

        ItemStack rod = sp.getMainHandItem();
        int lvlFish = getEnchantmentLevel(rod, "fishing_stabilizing");
        if (lvlFish <= 0) {
            lvlFish = getEnchantmentLevel(sp.getOffhandItem(), "fishing_stabilizing");
        }

        // Comprobar si tiene el Estabilizador de Pesca en el inventario
        boolean hasStabilizer = false;
        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack stack = sp.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(SystemItems.FISHING_LINE_STABILIZER.get())) {
                hasStabilizer = true;
                break;
            }
        }

        ServerLevel sl = (ServerLevel) sp.level();
        String dimPath = sl.dimension().location().getPath();
        boolean isTeyvat = dimPath.contains("teyvat");
        String biomePath = sl.getBiome(sp.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("");

        float roll = sl.random.nextFloat();
        float baseChance = isTeyvat ? 0.65F : 0.35F;
        if (lvlFish > 0) baseChance += 0.20F * lvlFish;
        if (hasStabilizer) baseChance += 0.25F;

        if (roll < baseChance) {
            float rarityRoll = sl.random.nextFloat();
            ItemStack extraFish;

            // Inazuma o tormenta: Pez Ángel Raimei
            if ((biomePath.contains("narukami") || biomePath.contains("yashiori") || sl.isThundering()) && rarityRoll < 0.12F) {
                extraFish = new ItemStack(SystemItems.RAIMEI_ANGELFISH.get());
            } else if ((biomePath.contains("jueyun") || biomePath.contains("karst") || biomePath.contains("chenyu")) && rarityRoll < 0.15F) {
                extraFish = new ItemStack(SystemItems.ABIDING_ANGELFISH.get());
            } else if (rarityRoll < 0.10F) {
                extraFish = new ItemStack(SystemItems.RAIMEI_ANGELFISH.get());
            } else if (rarityRoll < 0.20F) {
                extraFish = new ItemStack(SystemItems.ABIDING_ANGELFISH.get());
            } else if (rarityRoll < 0.35F) {
                extraFish = new ItemStack(SystemItems.VENOMSPINE_FISH.get());
            } else if (rarityRoll < 0.50F) {
                extraFish = new ItemStack(SystemItems.CRYSTALFISH.get());
            } else if (rarityRoll < 0.65F) {
                extraFish = new ItemStack(SystemItems.GLAZE_MEDAKA.get());
            } else if (rarityRoll < 0.80F) {
                extraFish = new ItemStack(SystemItems.SWEET_FLOWER_MEDAKA.get());
            } else if (rarityRoll < 0.90F) {
                extraFish = new ItemStack(SystemItems.AIZEN_MEDAKA.get());
            } else {
                extraFish = new ItemStack(SystemItems.MEDAKA.get());
            }

            event.getDrops().add(extraFish);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.playSound(null, sp.blockPosition(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.4F);
            sp.displayClientMessage(Component.literal("§b✦ ¡Has pescado un §e" + extraFish.getHoverName().getString() + "§b de Teyvat!"), true);
        }
    }
}
