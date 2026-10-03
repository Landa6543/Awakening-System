package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Random;

public final class RpgLevelZoneManager {
    private static final Random RNG = new Random();

    private RpgLevelZoneManager() {}

    public static int getZoneLevel(Level level, BlockPos pos) {
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM)) {
            DungeonManager.DungeonSession session = DungeonManager.getSessionAt(pos);
            if (session != null) {
                return DungeonManager.getRankBaseLevel(session.rank) + Math.max(0, session.currentFloor - 1) * 2;
            }
            return 20;
        }
        if (level.dimension().equals(TowerManager.RANKER_TOWER)) {
            return 50;
        }
        if (level.dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
            return 60;
        }
        if (level.dimension() == Level.NETHER) {
            return 50 + RNG.nextInt(25); // Nivel 50-75 en el Nether
        }
        if (level.dimension() == Level.END) {
            return 80 + RNG.nextInt(21); // Nivel 80-100 en el End
        }

        BlockPos spawn = level.getSharedSpawnPos();
        double dist = Math.sqrt(pos.distSqr(spawn));

        if (dist <= 500.0D) {
            return 1 + (int) ((dist / 500.0D) * 19.0D); // Nivel 1 - 20
        } else if (dist <= 1500.0D) {
            return 20 + (int) (((dist - 500.0D) / 1000.0D) * 30.0D); // Nivel 20 - 50
        } else if (dist <= 3500.0D) {
            return 50 + (int) (((dist - 1500.0D) / 2000.0D) * 30.0D); // Nivel 50 - 80
        } else {
            return 80 + Math.min(25, (int) (((dist - 3500.0D) / 3000.0D) * 20.0D)); // Nivel 80 - 100+
        }
    }

    public static String getZoneName(int level) {
        if (level < 20) return "§aPraderas del Novicio (Nv. 1-20)";
        if (level < 50) return "§eBosque y Ruinas Prohibidas (Nv. 20-50)";
        if (level < 80) return "§cTierras de Ceniza y Furia (Nv. 50-80)";
        return "§4§lAbismo de Calamidades (Nv. 80-100+)";
    }

    public static void scaleMobOnSpawn(Mob mob, ServerLevel level) {
        if (mob.getTags().contains("rpg_scaled") || mob.getTags().contains("shadow_soldier")
                || mob.getTags().contains("dungeon_mob") || mob.getTags().contains("tower_mob")
                || mob.getTags().contains("pagoda_trial_mob")) {
            return;
        }
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM)
                || level.dimension().equals(TowerManager.RANKER_TOWER)
                || level.dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
            return;
        }
        if (!(mob instanceof Enemy)) return;
        if (mob instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity
                || mob instanceof com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity
                || mob instanceof com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity) {
            return;
        }

        mob.addTag("rpg_scaled");
        int mobLevel = getZoneLevel(level, mob.blockPosition());
        mob.getPersistentData().putInt("rpg_mob_level", mobLevel);

        // Escalar vida y daño según el nivel de zona
        double hpMultiplier = 1.0D + (mobLevel * 0.035D);
        double dmgMultiplier = 1.0D + (mobLevel * 0.025D);

        var maxHpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHpAttr != null) {
            double newHp = maxHpAttr.getBaseValue() * hpMultiplier;
            maxHpAttr.setBaseValue(newHp);
            mob.setHealth((float) newHp);
        }

        var attackAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(attackAttr.getBaseValue() * dmgMultiplier);
        }

        // Nombre con indicador de nivel RPG
        String originalName = mob.getType().getDescription().getString();
        String color = mobLevel >= 80 ? "§4§l" : (mobLevel >= 50 ? "§c" : (mobLevel >= 20 ? "§e" : "§a"));
        mob.setCustomName(Component.literal(color + "[Nv. " + mobLevel + "] §f" + originalName));
        // Si la criatura está en zona 50-80 a cielo abierto, posibilidad de avistar un Wyvern salvaje
        if (mobLevel >= 50 && mobLevel <= 80 && level.canSeeSky(mob.blockPosition()) && RNG.nextDouble() < 0.04D) {
            com.soybe.awakeningsystem.entity.WyvernEntity wyvern = new com.soybe.awakeningsystem.entity.WyvernEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.WYVERN.get(), level);
            wyvern.setPos(mob.getX() + RNG.nextInt(15) - 7, mob.getY() + 12 + RNG.nextInt(10), mob.getZ() + RNG.nextInt(15) - 7);
            wyvern.setFlying(true);
            wyvern.setVariant(RNG.nextInt(3));
            level.addFreshEntity(wyvern);
        }
    }

    public static void handleMobDrops(LivingEntity entity, ServerLevel level) {
        // Drops garantizados para el Wyvern
        if (entity instanceof com.soybe.awakeningsystem.entity.WyvernEntity) {
            int scales = 2 + RNG.nextInt(4);
            entity.spawnAtLocation(new ItemStack(SystemItems.WYVERN_SCALE.get(), scales));
            int fangs = 1 + RNG.nextInt(3);
            entity.spawnAtLocation(new ItemStack(SystemItems.WYVERN_FANG.get(), fangs));
            if (RNG.nextDouble() < 0.18D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.WYVERN_EGG.get(), 1));
            }
        }

        // Detección de jugador con buff de Resina Concentrada
        boolean doubleDrops = false;
        if (entity.getLastHurtByMob() instanceof net.minecraft.world.entity.player.Player killer) {
            long resinUntil = killer.getPersistentData().getLong("condensed_resin_until");
            if (resinUntil > level.getGameTime()) {
                doubleDrops = true;
            }
        }

        // Detección de si estamos en la dimensión de Teyvat (Mora y Protos SOLO se consiguen en Teyvat)
        boolean isTeyvat = level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD);

        // === DROPS CANÓNICOS DE GENSCHIN IMPACT ===

        // 1. SLIMES (GenshinSlimeEntity y Slime de Minecraft)
        if (entity instanceof com.soybe.awakeningsystem.entity.GenshinSlimeEntity gSlime) {
            boolean isBig = gSlime.isBig();
            int multiplier = doubleDrops ? 2 : 1;
            int condCount = (1 + RNG.nextInt(isBig ? 4 : 2)) * multiplier;
            if (SystemItems.SLIME_CONDENSATE.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SLIME_CONDENSATE.get(), condCount));
            }
            if (isBig && RNG.nextDouble() < 0.40D && SystemItems.SLIME_SECRETIONS.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SLIME_SECRETIONS.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (isBig && RNG.nextDouble() < 0.15D && SystemItems.SLIME_CONCENTRATE.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SLIME_CONCENTRATE.get(), multiplier));
            }
            if (RNG.nextDouble() < 0.12D && SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (2 + RNG.nextInt(5)) * multiplier));
            }
        } else if (entity instanceof net.minecraft.world.entity.monster.Slime vSlime && !(entity instanceof net.minecraft.world.entity.monster.MagmaCube)) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.SLIME_CONDENSATE.isBound() && RNG.nextDouble() < 0.65D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SLIME_CONDENSATE.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (vSlime.getSize() >= 4 && RNG.nextDouble() < 0.30D && SystemItems.SLIME_SECRETIONS.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SLIME_SECRETIONS.get(), multiplier));
            }
        }

        // 2. HILICHURLS (HilichurlEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.HilichurlEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            int maskCount = (1 + RNG.nextInt(2)) * multiplier;
            if (SystemItems.DAMAGED_MASK.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.DAMAGED_MASK.get(), maskCount));
            }
            if (RNG.nextDouble() < 0.20D && SystemItems.STAINED_MASK.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.STAINED_MASK.get(), multiplier));
            }
            if (RNG.nextDouble() < 0.25D && SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (3 + RNG.nextInt(6)) * multiplier));
            }
            if (isTeyvat && RNG.nextDouble() < 0.08D && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
        }

        // 3. MITACHURLS (MitachurlEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.MitachurlEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.HEAVY_HORN.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.HEAVY_HORN.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (SystemItems.STAINED_MASK.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.STAINED_MASK.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (RNG.nextDouble() < 0.35D && SystemItems.OMINOUS_MASK.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.OMINOUS_MASK.get(), multiplier));
            }
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(12)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(4)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), multiplier));
            }
        }

        // 4. CAZADOR DE LAS RUINAS (RuinHunterEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.RuinHunterEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.CHAOS_DEVICE.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_DEVICE.get(), (3 + RNG.nextInt(4)) * multiplier));
            }
            if (SystemItems.CHAOS_CIRCUIT.isBound() && RNG.nextDouble() < 0.85D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CIRCUIT.get(), (1 + RNG.nextInt(3)) * multiplier));
            }
            if (SystemItems.CHAOS_CORE.isBound() && RNG.nextDouble() < 0.50D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            entity.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, (2 + RNG.nextInt(4)) * multiplier));
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (12 + RNG.nextInt(14)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (3 + RNG.nextInt(4)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
        }

        // 5. GUARDIÁN DE LAS RUINAS (RuinGuardEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.RuinGuardEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.CHAOS_DEVICE.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_DEVICE.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
            if (SystemItems.CHAOS_CIRCUIT.isBound() && RNG.nextDouble() < 0.70D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CIRCUIT.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (SystemItems.CHAOS_CORE.isBound() && RNG.nextDouble() < 0.35D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), multiplier));
            }
            entity.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, (2 + RNG.nextInt(3)) * multiplier));
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(12)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), multiplier));
            }
        }

        // 6. STONEHIDE LAWACHURL (StonehideLawachurlEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.StonehideLawachurlEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.HEAVY_HORN.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.HEAVY_HORN.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
            if (SystemItems.BLACK_BRONZE_HORN.isBound() && RNG.nextDouble() < 0.75D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.BLACK_BRONZE_HORN.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (SystemItems.BLACK_CRYSTAL_HORN.isBound() && RNG.nextDouble() < 0.40D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.BLACK_CRYSTAL_HORN.get(), multiplier));
            }
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (15 + RNG.nextInt(15)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (3 + RNG.nextInt(4)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
        }

        // 7. CRYO REGISVINE (CryoRegisvineEntity)
        if (entity instanceof com.soybe.awakeningsystem.entity.CryoRegisvineEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.HOARFROST_CORE.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.HOARFROST_CORE.get(), (1 + RNG.nextInt(3)) * multiplier));
            }
            if (SystemItems.SHIVADA_JADE_GEMSTONE.isBound() && RNG.nextDouble() < 0.30D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_GEMSTONE.get(), multiplier));
            } else if (SystemItems.SHIVADA_JADE_CHUNK.isBound() && RNG.nextDouble() < 0.70D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_CHUNK.get(), (1 + RNG.nextInt(2)) * multiplier));
            } else if (SystemItems.SHIVADA_JADE_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_FRAGMENT.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (4 + RNG.nextInt(6)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
        }

        // 8. MAGOS DEL ABISMO (AbstractAbyssMageEntity: Pyro, Hydro, Cryo, Electro)
        if (entity instanceof com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity) {
            int multiplier = doubleDrops ? 2 : 1;
            if (SystemItems.DEAD_LEY_LINE_BRANCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.DEAD_LEY_LINE_BRANCH.get(), (1 + RNG.nextInt(3)) * multiplier));
            }
            if (SystemItems.DEAD_LEY_LINE_LEAVES.isBound() && RNG.nextDouble() < 0.70D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.DEAD_LEY_LINE_LEAVES.get(), (1 + RNG.nextInt(2)) * multiplier));
            }
            if (SystemItems.LEY_LINE_SPROUT.isBound() && RNG.nextDouble() < 0.35D) {
                entity.spawnAtLocation(new ItemStack(SystemItems.LEY_LINE_SPROUT.get(), multiplier));
            }
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(10)) * multiplier));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(3)) * multiplier));
            }
            if (isTeyvat && SystemItems.MORA_POUCH.isBound()) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA_POUCH.get(), multiplier));
            }
        }

        if (!entity.getPersistentData().contains("rpg_mob_level")) return;
        int mobLevel = entity.getPersistentData().getInt("rpg_mob_level");

        // Probabilidad de Protogemas y Moras (RESTRINGIDO EXCLUSIVAMENTE a la dimensión de Teyvat)
        if (isTeyvat) {
            double primoChance = 0.05D + (mobLevel * 0.002D);
            if (RNG.nextDouble() < primoChance) {
                int amount = 1 + RNG.nextInt(Math.max(1, mobLevel / 20));
                ItemStack primos = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get(), amount) : new ItemStack(Items.AMETHYST_SHARD, amount);
                entity.spawnAtLocation(primos);
            }
            double moraChance = 0.15D + (mobLevel * 0.002D);
            if (RNG.nextDouble() < moraChance && SystemItems.MORA.isBound()) {
                int amount = 1 + RNG.nextInt(Math.max(1, mobLevel / 30));
                entity.spawnAtLocation(new ItemStack(SystemItems.MORA.get(), amount));
            }
        }


        // Probabilidad de pieza de equipo con afijos RPG aleatorios
        double equipChance = 0.03D + (mobLevel * 0.001D);
        if (RNG.nextDouble() < equipChance) {
            ItemStack dropStack = rollRandomRpgEquipment(mobLevel);
            RpgAffixManager.rollAffixes(dropStack, mobLevel);
            entity.spawnAtLocation(dropStack);
        }
    }

    public static ItemStack rollRandomRpgEquipment(int mobLevel) {
        boolean isArmor = RNG.nextInt(100) < 65; // 65% armaduras completas, 35% armas
        if (isArmor) {
            int slot = RNG.nextInt(4); // 0: Casco, 1: Pechera, 2: Pantalones, 3: Botas
            if (mobLevel < 25) {
                int tier = RNG.nextInt(3);
                if (tier == 0) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.LEATHER_HELMET);
                        case 1 -> new ItemStack(Items.LEATHER_CHESTPLATE);
                        case 2 -> new ItemStack(Items.LEATHER_LEGGINGS);
                        default -> new ItemStack(Items.LEATHER_BOOTS);
                    };
                } else if (tier == 1) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.CHAINMAIL_HELMET);
                        case 1 -> new ItemStack(Items.CHAINMAIL_CHESTPLATE);
                        case 2 -> new ItemStack(Items.CHAINMAIL_LEGGINGS);
                        default -> new ItemStack(Items.CHAINMAIL_BOOTS);
                    };
                } else {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.IRON_HELMET);
                        case 1 -> new ItemStack(Items.IRON_CHESTPLATE);
                        case 2 -> new ItemStack(Items.IRON_LEGGINGS);
                        default -> new ItemStack(Items.IRON_BOOTS);
                    };
                }
            } else if (mobLevel < 60) {
                int tier = RNG.nextInt(3);
                if (tier == 0) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.IRON_HELMET);
                        case 1 -> new ItemStack(Items.IRON_CHESTPLATE);
                        case 2 -> new ItemStack(Items.IRON_LEGGINGS);
                        default -> new ItemStack(Items.IRON_BOOTS);
                    };
                } else if (tier == 1) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.GOLDEN_HELMET);
                        case 1 -> new ItemStack(Items.GOLDEN_CHESTPLATE);
                        case 2 -> new ItemStack(Items.GOLDEN_LEGGINGS);
                        default -> new ItemStack(Items.GOLDEN_BOOTS);
                    };
                } else {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.DIAMOND_HELMET);
                        case 1 -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                        case 2 -> new ItemStack(Items.DIAMOND_LEGGINGS);
                        default -> new ItemStack(Items.DIAMOND_BOOTS);
                    };
                }
            } else if (mobLevel < 85) {
                int tier = RNG.nextInt(3);
                if (tier == 0) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.DIAMOND_HELMET);
                        case 1 -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                        case 2 -> new ItemStack(Items.DIAMOND_LEGGINGS);
                        default -> new ItemStack(Items.DIAMOND_BOOTS);
                    };
                } else if (tier == 1) {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.NETHERITE_HELMET);
                        case 1 -> new ItemStack(Items.NETHERITE_CHESTPLATE);
                        case 2 -> new ItemStack(Items.NETHERITE_LEGGINGS);
                        default -> new ItemStack(Items.NETHERITE_BOOTS);
                    };
                } else {
                    if (SystemItems.MITHRIL_HELMET.isBound()) {
                        return switch (slot) {
                            case 0 -> new ItemStack(SystemItems.MITHRIL_HELMET.get());
                            case 1 -> new ItemStack(SystemItems.MITHRIL_CHESTPLATE.get());
                            case 2 -> new ItemStack(SystemItems.MITHRIL_LEGGINGS.get());
                            default -> new ItemStack(SystemItems.MITHRIL_BOOTS.get());
                        };
                    } else {
                        return switch (slot) {
                            case 0 -> new ItemStack(Items.NETHERITE_HELMET);
                            case 1 -> new ItemStack(Items.NETHERITE_CHESTPLATE);
                            case 2 -> new ItemStack(Items.NETHERITE_LEGGINGS);
                            default -> new ItemStack(Items.NETHERITE_BOOTS);
                        };
                    }
                }
            } else {
                int tier = RNG.nextInt(3);
                if (tier == 0 && SystemItems.ADAMANTITE_HELMET.isBound()) {
                    return switch (slot) {
                        case 0 -> new ItemStack(SystemItems.ADAMANTITE_HELMET.get());
                        case 1 -> new ItemStack(SystemItems.ADAMANTITE_CHESTPLATE.get());
                        case 2 -> new ItemStack(SystemItems.ADAMANTITE_LEGGINGS.get());
                        default -> new ItemStack(SystemItems.ADAMANTITE_BOOTS.get());
                    };
                } else if (tier == 1 && SystemItems.SPIRIT_JADE_HELMET.isBound()) {
                    return switch (slot) {
                        case 0 -> new ItemStack(SystemItems.SPIRIT_JADE_HELMET.get());
                        case 1 -> new ItemStack(SystemItems.SPIRIT_JADE_CHESTPLATE.get());
                        case 2 -> new ItemStack(SystemItems.SPIRIT_JADE_LEGGINGS.get());
                        default -> new ItemStack(SystemItems.SPIRIT_JADE_BOOTS.get());
                    };
                } else {
                    return switch (slot) {
                        case 0 -> new ItemStack(Items.NETHERITE_HELMET);
                        case 1 -> new ItemStack(Items.NETHERITE_CHESTPLATE);
                        case 2 -> new ItemStack(Items.NETHERITE_LEGGINGS);
                        default -> new ItemStack(Items.NETHERITE_BOOTS);
                    };
                }
            }
        } else {
            boolean isAxe = RNG.nextBoolean();
            if (mobLevel < 25) {
                return isAxe ? new ItemStack(Items.IRON_AXE) : new ItemStack(Items.IRON_SWORD);
            } else if (mobLevel < 60) {
                return isAxe ? (RNG.nextBoolean() ? new ItemStack(Items.IRON_AXE) : new ItemStack(Items.DIAMOND_AXE))
                             : (RNG.nextBoolean() ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.DIAMOND_SWORD));
            } else if (mobLevel < 85) {
                return isAxe ? (RNG.nextBoolean() ? new ItemStack(Items.DIAMOND_AXE) : new ItemStack(Items.NETHERITE_AXE))
                             : (RNG.nextBoolean() ? new ItemStack(Items.DIAMOND_SWORD) : new ItemStack(Items.NETHERITE_SWORD));
            } else {
                return isAxe ? new ItemStack(Items.NETHERITE_AXE) : new ItemStack(Items.NETHERITE_SWORD);
            }
        }
    }
}
