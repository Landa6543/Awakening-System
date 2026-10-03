package com.soybe.awakeningsystem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class SystemEvents {
    private static final Map<UUID, NonNullList<ItemStack>> SAVED_DUNGEON_DEATH_INVENTORIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> WELKIN_OFFERED_DAYS = new ConcurrentHashMap<>();

    private SystemEvents() {}

    public static SystemData data(Player player) { return player.getData(AwakeningSystemMod.PLAYER_SYSTEM); }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                if (player.level() instanceof ServerLevel teyvatLevel) {
                    TeyvatWorldManager.ensureTeyvatCityAndStructures(teyvatLevel);
                }
            }
            player.getPersistentData().putBoolean("awakening_attached", true);
            refreshPlayer(player);
            SystemData data = data(player);
            data.addVisitedDimension(player.level().dimension().location().toString());
            long today = player.level().getDayTime() / 24000L;
            if (data.lastDailyLoginDay() != today) {
                data.setLastDailyLoginDay(today);
                int bonusCoins = SystemConfig.DAILY_LOGIN_BONUS_COINS.get();
                int bonusExp = SystemConfig.DAILY_LOGIN_BONUS_EXP.get();
                if (bonusCoins > 0) data.addCoins(bonusCoins);
                if (bonusExp > 0) awardExperience(player, data, bonusExp);
                AwakeningSystemMod.notify(player, "awakening_system.notification.daily_login.title",
                        "awakening_system.notification.daily_login.message", 0xFFFFD866, bonusCoins);
                AwakeningSystemMod.sync(player);
            }
            if (data.hasNoAffinityProgress()) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.awakened.title",
                        "awakening_system.notification.awakened.message", 0xFF70E8FF);
            }

            // Otorgar SP acumulado según nivel si ya tiene clase
            if (data.hasHunterClass() && data.level() >= 30) {
                int expectedTotal = 1 + (data.level() - 30) / 5;
                int actualTotal = data.skillPoints() + data.spentSkillPoints();
                if (actualTotal < expectedTotal) {
                    data.addSkillPoints(expectedTotal - actualTotal);
                    AwakeningSystemMod.sync(player);
                }
            }

            // Otorgar Guía del Cazador a los jugadores (solo 1 vez en toda la vida del jugador, deduplicar si tenían múltiples copias)
            if (SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound()) {
                int guideCount = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (it.is(SystemItems.HUNTER_GUIDE.value())) {
                        guideCount++;
                        if (guideCount > 1) {
                            // Duplicado encontrado en el inventario: purgar inmediatamente
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        } else if (it.getCount() > 1) {
                            it.setCount(1);
                        }
                    }
                }

                AwakeningPlayerRegistryData registryData = player.getServer() != null ? AwakeningPlayerRegistryData.get(player.getServer()) : null;
                CompoundTag persistedTag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                boolean alreadyReceived = data.hasReceivedHunterGuide()
                        || player.getPersistentData().getBoolean("awakening_received_guide")
                        || persistedTag.getBoolean("awakening_received_guide")
                        || (registryData != null && registryData.hasReceivedHunterGuide(player.getUUID()));

                if (guideCount > 0 || alreadyReceived) {
                    data.setReceivedHunterGuide(true);
                    player.getPersistentData().putBoolean("awakening_received_guide", true);
                    persistedTag.putBoolean("awakening_received_guide", true);
                    player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedTag);
                    if (registryData != null) {
                        registryData.markReceivedHunterGuide(player.getUUID());
                    }
                } else {
                    // Primera vez absoluta recibiendo la guía
                    data.setReceivedHunterGuide(true);
                    player.getPersistentData().putBoolean("awakening_received_guide", true);
                    persistedTag.putBoolean("awakening_received_guide", true);
                    player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedTag);
                    if (registryData != null) {
                        registryData.markReceivedHunterGuide(player.getUUID());
                    }
                    player.addItem(new ItemStack(SystemItems.HUNTER_GUIDE.get()));
                }
            }
            DungeonManager.syncRiftsToPlayer(player);
            DungeonManager.onPlayerLogin(player);
            TowerManager.onPlayerLogin(player);
            PagodaTrialManager.onPlayerLogin(player);
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);
            if (player.getServer() != null) {
                for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                    AwakeningSystemMod.syncPlayerBadge(other);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
        ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
        CompoundTag oldPersisted = oldPlayer.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (oldPlayer.getPersistentData().getBoolean("awakening_received_guide") || oldPersisted.getBoolean("awakening_received_guide")) {
            newPlayer.getPersistentData().putBoolean("awakening_received_guide", true);
            CompoundTag newPersisted = newPlayer.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            newPersisted.putBoolean("awakening_received_guide", true);
            newPlayer.getPersistentData().put(Player.PERSISTED_NBT_TAG, newPersisted);
        }

        if (event.isWasDeath()) {
            NonNullList<ItemStack> saved = SAVED_DUNGEON_DEATH_INVENTORIES.remove(oldPlayer.getUUID());
            boolean restored = false;
            if (saved != null) {
                for (int i = 0; i < saved.size() && i < newPlayer.getInventory().getContainerSize(); i++) {
                    newPlayer.getInventory().setItem(i, saved.get(i).copy());
                }
                restored = true;
            } else if (oldPlayer.getPersistentData().contains("dungeon_preserved_inventory", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag listTag = oldPlayer.getPersistentData().getList("dungeon_preserved_inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); i++) {
                    net.minecraft.nbt.CompoundTag slotTag = listTag.getCompound(i);
                    int slot = slotTag.getInt("Slot");
                    ItemStack stack = ItemStack.parse(newPlayer.registryAccess(), slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                    if (slot >= 0 && slot < newPlayer.getInventory().getContainerSize()) {
                        newPlayer.getInventory().setItem(slot, stack);
                    }
                }
                restored = true;
            }
            oldPlayer.getPersistentData().remove("dungeon_preserved_inventory");
            if (restored) {
                newPlayer.getPersistentData().putBoolean("dungeon_loot_preserved", true);
            }
            com.soybe.awakeningsystem.compat.CuriosCompatHandler.restoreCuriosOnRespawn(oldPlayer, newPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (DungeonManager.isInDungeon(player.getUUID())
                    || TowerManager.isInTower(player.getUUID())
                    || PagodaTrialManager.isInTrial(player.getUUID())
                    || player.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                    || player.level().dimension().equals(TowerManager.RANKER_TOWER)
                    || player.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                    || SAVED_DUNGEON_DEATH_INVENTORIES.containsKey(player.getUUID())
                    || player.getPersistentData().contains("dungeon_preserved_inventory", net.minecraft.nbt.Tag.TAG_LIST)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SystemData data = data(player);
            data.recordStatistic("deaths", 1);
            if (player.getServer() != null) {
                DungeonManager.exitDungeon(player.getServer(), player.getUUID());
                TowerManager.onPlayerRespawn(player);
                PagodaTrialManager.onPlayerRespawn(player);
            }
            // Restauración de respaldo si PlayerEvent.Clone no procesó el inventario
            if (!player.getPersistentData().getBoolean("dungeon_loot_preserved")) {
                NonNullList<ItemStack> saved = SAVED_DUNGEON_DEATH_INVENTORIES.remove(player.getUUID());
                boolean fallbackRestored = false;
                if (saved != null) {
                    for (int i = 0; i < saved.size() && i < player.getInventory().getContainerSize(); i++) {
                        player.getInventory().setItem(i, saved.get(i).copy());
                    }
                    fallbackRestored = true;
                } else if (player.getPersistentData().contains("dungeon_preserved_inventory", net.minecraft.nbt.Tag.TAG_LIST)) {
                    net.minecraft.nbt.ListTag listTag = player.getPersistentData().getList("dungeon_preserved_inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
                    for (int i = 0; i < listTag.size(); i++) {
                        net.minecraft.nbt.CompoundTag slotTag = listTag.getCompound(i);
                        int slot = slotTag.getInt("Slot");
                        ItemStack stack = ItemStack.parse(player.registryAccess(), slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                        if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                            player.getInventory().setItem(slot, stack);
                        }
                    }
                    fallbackRestored = true;
                }
                if (fallbackRestored) {
                    player.getPersistentData().putBoolean("dungeon_loot_preserved", true);
                }
            }
            player.getPersistentData().remove("dungeon_preserved_inventory");
            if (player.getPersistentData().getBoolean("dungeon_loot_preserved")) {
                player.getPersistentData().remove("dungeon_loot_preserved");
                player.displayClientMessage(Component.translatable("awakening_system.dungeon.inventory_preserved"), false);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            com.soybe.awakeningsystem.compat.CuriosCompatHandler.restoreCuriosOnRespawn(player, player);
            refreshPlayer(player);
            player.setHealth(player.getMaxHealth());
            data.setMana(data.maxMana());
            DungeonManager.syncRiftsToPlayer(player);
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target
                && event.getEntity() instanceof ServerPlayer watcher) {
            AwakeningSystemMod.syncPlayerBadge(target);
            AwakeningSystemMod.syncPlayerBadge(watcher);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(net.neoforged.neoforge.event.entity.EntityMountEvent event) {
        if (event.isMounting() && event.getEntityMounting() instanceof ServerPlayer sp) {
            com.soybe.awakeningsystem.compat.CreateCompatHandler.onEntityMount(sp, event.getEntityBeingMounted());
        }
    }

    public static boolean grantFable(ServerPlayer player, String fableId) {
        if (player == null || fableId == null) return false;
        com.soybe.awakeningsystem.fable.FableDefinitions.Fable fable = com.soybe.awakeningsystem.fable.FableDefinitions.get(fableId);
        if (fable == null) return false;
        SystemData data = data(player);
        if (data == null || data.hasFable(fableId)) return false;

        MinecraftServer server = player.getServer();
        if (server != null && fable.isWorldFirst()) {
            com.soybe.awakeningsystem.fable.FableWorldSavedData worldData = com.soybe.awakeningsystem.fable.FableWorldSavedData.get(server);
            if (!worldData.tryClaimWorldFirst(fableId, player.getUUID(), player.getScoreboardName())) {
                return false;
            }
        }

        if (!data.unlockFable(fableId, fable.weight())) return false;

        if (server != null) {
            String pName = player.getScoreboardName();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.2F);
                if (fable.isWorldFirst()) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.fable_world_first.title",
                            "awakening_system.notification.fable_world_first.desc",
                            fable.grade().hexColor(),
                            pName, fable.grade().colorCode() + "[" + fable.title() + "]");
                } else if (p.getUUID().equals(player.getUUID())) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.fable_acquired.title",
                            "awakening_system.notification.fable_acquired.desc",
                            fable.grade().hexColor(),
                            fable.grade().colorCode() + "[" + fable.title(), fable.weight());
                }
            }
        }

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 45, 0.8D, 1.0D, 0.8D, 0.1D);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 0.6D, 0.8D, 0.6D, 0.2D);
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.0D);
        }

        ConstellationAscension.checkFableAscension(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            DungeonManager.exitDungeon(player.getServer(), player.getUUID());
            TowerManager.onPlayerLogout(player);
            PagodaTrialManager.onPlayerLogout(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        TeyvatWorldManager.ensureMondstadtOnServerStart(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // En 1.21.1, ServerLifecycleHooks permite obtener el servidor o ServerTickEvent
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            DungeonManager.tick(server);
            TowerManager.tick(server);
            PagodaTrialManager.tick(server);
            PhysicalRouletteManager.tick(server);
            TeyvatWorldManager.tick(server);
            tickShadowSoldiers(server);
            for (ServerLevel sl : server.getAllLevels()) {
                com.soybe.awakeningsystem.element.ElementalReactionManager.tickServer(sl);
            }
            if (server.getTickCount() % 100 == 0) {
                com.soybe.awakeningsystem.world.SeriesStructureWorldGen.tickNaturalGeneration(server);
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            data(player).addVisitedDimension(event.getTo().location().toString());
            if (event.getTo().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                if (player.level() instanceof ServerLevel teyvatLevel) {
                    TeyvatWorldManager.ensureTeyvatCityAndStructures(teyvatLevel);
                }
            }
            refreshPlayer(player);
            teleportShadowsAcrossDimensions(player);
            DungeonManager.syncRiftsToPlayer(player);
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);
        }
    }

    private static void teleportDistantShadowSoldiers(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel sl)) return;
        String masterTag = "shadow_master_" + player.getUUID();

        var soldiers = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(128.0D),
                m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));

        for (var soldier : soldiers) {
            double distSq = soldier.distanceToSqr(player);
            if (distSq > 3600.0D) { // > 60 bloques (antes 16 bloques) para dar libertad en el campo de batalla
                double angle = Math.toRadians(player.getYRot() + 180.0F + ((soldier.getId() % 5) - 2) * 25.0F);
                double tx = player.getX() + Math.sin(angle) * 2.5D;
                double tz = player.getZ() - Math.cos(angle) * 2.5D;
                double ty = player.getY();

                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 25, 0.3D, 0.4D, 0.3D, 0.02D);

                net.minecraft.world.phys.Vec3 safePos = TeleportHelper.findSafeTeleportPos(sl, soldier, tx, ty, tz, player.position());
                soldier.teleportTo(safePos.x, safePos.y, safePos.z);

                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, safePos.x, safePos.y + 0.5D, safePos.z, 25, 0.3D, 0.5D, 0.3D, 0.05D);
                sl.sendParticles(ParticleTypes.PORTAL, safePos.x, safePos.y + 0.5D, safePos.z, 20, 0.2D, 0.3D, 0.2D, 0.1D);
                sl.playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.2F);

                soldier.setTarget(null);
            }
        }
    }

    private static void teleportShadowsAcrossDimensions(ServerPlayer player) {
        if (player.getServer() == null) return;
        String masterTag = "shadow_master_" + player.getUUID();
        ServerLevel newLevel = player.serverLevel();

        for (ServerLevel level : player.getServer().getAllLevels()) {
            if (level == newLevel) continue;
            var soldiers = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(net.minecraft.world.entity.Mob.class),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));
            for (var soldier : soldiers) {
                net.minecraft.world.phys.Vec3 safePos = TeleportHelper.findSafeTeleportPos(newLevel, soldier, player.getX(), player.getY(), player.getZ(), player.position());
                soldier.teleportTo(newLevel, safePos.x, safePos.y, safePos.z, java.util.Set.of(), soldier.getYRot(), soldier.getXRot());
            }
        }
    }

    public static void dismissAllShadows(ServerPlayer player) {
        if (player.getServer() == null) return;
        String masterTag = "shadow_master_" + player.getUUID();

        for (ServerLevel level : player.getServer().getAllLevels()) {
            var soldiers = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(net.minecraft.world.entity.Mob.class),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));
            for (var soldier : soldiers) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                level.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.02D);
                soldier.discard();
            }
        }
    }

    private static void refreshPlayer(ServerPlayer player) {
        SystemData data = data(player);
        if (data.skills().containsKey("caregiver_blessing") && !data.skills().containsKey("sh_caregiver_blessing")) {
            data.skills().put("sh_caregiver_blessing", data.skills().get("caregiver_blessing"));
        }
        if (data.skills().containsKey("electrification") && !data.skills().containsKey("le_electrification")) {
            data.skills().put("le_electrification", data.skills().get("electrification"));
        }
        if (data.skills().containsKey("bloodthirst") && "blood_emperor".equals(data.hunterClass()) && !data.skills().containsKey("be_bloodthirst")) {
            data.skills().put("be_bloodthirst", data.skills().get("bloodthirst"));
        }
        if (data.skills().containsKey("seismic_roar") && "beast_monarch".equals(data.hunterClass()) && !data.skills().containsKey("bm_seismic_roar")) {
            data.skills().put("bm_seismic_roar", data.skills().get("seismic_roar"));
        }
        if (data.skills().containsKey("dragon_dive") && "colossal_berserker".equals(data.hunterClass()) && !data.skills().containsKey("cb_valterian_instinct")) {
            data.skills().put("cb_valterian_instinct", data.skills().get("dragon_dive"));
        }
        if (data.skills().containsKey("rulers_authority") && "star_apostle".equals(data.hunterClass()) && !data.skills().containsKey("fourth_wall")) {
            data.skills().put("fourth_wall", data.skills().get("rulers_authority"));
        }
        data.removeUnavailableContent();
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
    }

        @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() != null && (event.getEntity().level().dimension().equals(DungeonManager.DUNGEON_REALM)
                || event.getEntity().level().dimension().equals(TowerManager.RANKER_TOWER)
                || event.getEntity().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || (event.getEntity() instanceof ServerPlayer sp && (DungeonManager.isInDungeon(sp.getUUID()) || TowerManager.isInTower(sp.getUUID()) || PagodaTrialManager.isInTrial(sp.getUUID()))))) {
            if (event.getEntity() instanceof ServerPlayer sp && (sp.isCreative() || sp.getAbilities().instabuild)) {
                // Permitir modo creativo
            } else {
                event.setCanceled(true);
                return;
            }
        }
        // Subespacio Dimensional: Detección en tiempo real de colocación de cofres/barriles
        if (event.getEntity() instanceof ServerPlayer sp && sp.serverLevel().dimension().equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
            net.minecraft.world.level.block.state.BlockState placed = event.getPlacedBlock();
            if (placed.is(Blocks.CHEST) || placed.is(Blocks.TRAPPED_CHEST) || placed.is(Blocks.BARREL)) {
                if (sp.getServer() != null) {
                    sp.getServer().execute(() -> {
                        com.soybe.awakeningsystem.subspace.SubspaceManager.syncWorldToSubspace(sp.serverLevel(), sp);
                        SystemData d = data(sp);
                        sp.displayClientMessage(Component.literal("§a✔ Almacén expandido: §e" + d.subspaceChestCount() + " Cofres §7(§b" + d.subspaceUnlockedSlots() + " ranuras§7)"), true);
                    });
                }
            }
        }
        // Inmunidad total al fuego de Yggdrasil: prohibir colocar fuego o lava en el árbol sagrado
        if (event.getEntity() != null && event.getEntity().level().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            BlockPos pos = event.getPos();
            if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 85 * 85 && pos.getY() >= YggdrasilTreeGenerator.BASE_Y - 5) {
                net.minecraft.world.level.block.state.BlockState placed = event.getPlacedBlock();
                if (placed.is(Blocks.FIRE) || placed.is(Blocks.SOUL_FIRE) || placed.is(Blocks.LAVA)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onYggdrasilInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            BlockPos pos = event.getPos();
            if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 85 * 85 && pos.getY() >= YggdrasilTreeGenerator.BASE_Y - 5) {
                ItemStack item = event.getItemStack();
                if (item.is(Items.FLINT_AND_STEEL) || item.is(Items.FIRE_CHARGE) || item.is(Items.LAVA_BUCKET)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onYggdrasilNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel sl && sl.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            BlockPos pos = event.getPos();
            if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 85 * 85 && pos.getY() >= YggdrasilTreeGenerator.BASE_Y - 5) {
                if (event.getState().is(Blocks.FIRE) || event.getState().is(Blocks.SOUL_FIRE)) {
                    sl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onYggdrasilMobFinalizeSpawn(net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent event) {
        if (event.getLevel() != null && event.getLevel().getLevel() != null) {
            ServerLevel sl = event.getLevel().getLevel();
            if (sl.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
                double x = event.getX();
                double y = event.getY();
                double z = event.getZ();
                if (x * x + z * z <= 85.0D * 85.0D && y >= YggdrasilTreeGenerator.BASE_Y - 5) {
                    if (!(event.getEntity() instanceof com.soybe.awakeningsystem.entity.ManaPixieEntity)) {
                        event.setSpawnCancelled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onYggdrasilEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            double x = event.getEntity().getX();
            double y = event.getEntity().getY();
            double z = event.getEntity().getZ();
            if (x * x + z * z <= 85.0D * 85.0D && y >= YggdrasilTreeGenerator.BASE_Y - 5) {
                if (event.getEntity() instanceof net.minecraft.world.entity.LightningBolt bolt) {
                    bolt.setVisualOnly(true);
                } else if (event.getEntity() instanceof net.minecraft.world.entity.monster.Monster) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension().equals(DungeonManager.DUNGEON_REALM)
                || event.getLevel().dimension().equals(TowerManager.RANKER_TOWER)
                || event.getLevel().dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(net.neoforged.neoforge.event.entity.EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceKey<Level> dim = player.level().dimension();

        if (dim.equals(DungeonManager.DUNGEON_REALM)) {
            DungeonManager.DungeonSession session = DungeonManager.getSession(player.getUUID());
            if (session != null) {
                if (DungeonManager.isTargetOutOfBounds(session, event.getTargetX(), event.getTargetY(), event.getTargetZ())) {
                    // Contener el teletransporte dentro de la sala activa actual
                    BlockPos safeCenter = DungeonManager.getCurrentRoomCenter(session);
                    if (safeCenter == null) safeCenter = session.arenaCenter;
                    double safeY = DungeonManager.findSafeFloorY(player.serverLevel(), safeCenter.getX() + 0.5D, safeCenter.getY(), safeCenter.getZ() + 0.5D);
                    event.setTargetX(safeCenter.getX() + 0.5D);
                    event.setTargetY(safeY);
                    event.setTargetZ(safeCenter.getZ() + 0.5D);
                    player.displayClientMessage(Component.translatable("awakening_system.dungeon.teleport_contained"), true);
                }
            } else if (!player.isSpectator()) {
                // Jugador sin sesión en Dungeon Realm: cancelar y rescatar
                event.setCanceled(true);
                DungeonManager.rescueStrandedPlayer(player);
            }
        } else if (dim.equals(TowerManager.RANKER_TOWER)) {
            TowerManager.TowerSession session = TowerManager.getSession(player.getUUID());
            if (session != null && session.floorOrigin != null) {
                double dx = event.getTargetX() - (session.floorOrigin.getX() + 0.5D);
                double dz = event.getTargetZ() - (session.floorOrigin.getZ() + 0.5D);
                double distSq = dx * dx + dz * dz;
                double minY = session.floorOrigin.getY();
                double maxY = session.floorOrigin.getY() + TowerManager.CHAMBER_HEIGHT + 1.0;
                if (distSq > (TowerManager.ARENA_RADIUS - 1.5) * (TowerManager.ARENA_RADIUS - 1.5) || event.getTargetY() < minY || event.getTargetY() > maxY) {
                    event.setTargetX(session.floorOrigin.getX() + 0.5D);
                    event.setTargetY(session.floorOrigin.getY() + 1.0D);
                    event.setTargetZ(session.floorOrigin.getZ() + 0.5D);
                    player.displayClientMessage(Component.translatable("awakening_system.tower.teleport_contained"), true);
                }
            } else if (!player.isSpectator()) {
                event.setCanceled(true);
                if (player.getServer() != null) {
                    TowerManager.exitTowerToSpawn(player.getServer(), player.getUUID());
                }
            }
        } else if (dim.equals(PagodaTrialManager.PAGODA_REALM)) {
            PagodaTrialManager.PagodaSession session = PagodaTrialManager.getSession(player.getUUID());
            if (session != null && session.stageOrigin != null) {
                double dx = event.getTargetX() - (session.stageOrigin.getX() + 0.5D);
                double dz = event.getTargetZ() - (session.stageOrigin.getZ() + 0.5D);
                double distSq = dx * dx + dz * dz;
                int maxR = (session.stage == 9 ? 28 : PagodaTrialManager.ARENA_RADIUS) - 2;
                int maxH = (session.stage == 9 ? 26 : PagodaTrialManager.ARENA_HEIGHT);
                double minY = session.stageOrigin.getY();
                double maxY = session.stageOrigin.getY() + maxH;
                if (distSq > maxR * maxR || event.getTargetY() < minY || event.getTargetY() > maxY) {
                    event.setTargetX(session.stageOrigin.getX() + 0.5D);
                    event.setTargetY(session.stageOrigin.getY() + 1.0D);
                    event.setTargetZ(session.stageOrigin.getZ() + 0.5D);
                    player.displayClientMessage(Component.translatable("awakening_system.pagoda.teleport_contained"), true);
                }
            } else if (!player.isSpectator()) {
                event.setCanceled(true);
                if (player.getServer() != null) {
                    PagodaTrialManager.exitTrial(player.getServer(), player.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && (event.getPlayer().level().dimension().equals(DungeonManager.DUNGEON_REALM)
                || event.getPlayer().level().dimension().equals(TowerManager.RANKER_TOWER)
                || event.getPlayer().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || DungeonManager.isInDungeon(event.getPlayer().getUUID())
                || TowerManager.isInTower(event.getPlayer().getUUID())
                || PagodaTrialManager.isInTrial(event.getPlayer().getUUID()))) {
            if (event.getPlayer().isCreative() || event.getPlayer().getAbilities().instabuild) {
                // Permitir modo creativo romper cualquier bloque, incluido Bedrock
                return;
            }
            event.setCanceled(true);
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player) || event.isCanceled()) return;

        // Subespacio Dimensional: Detección en tiempo real de remoción de cofres/barriles
        if (player.serverLevel().dimension().equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
            net.minecraft.world.level.block.state.BlockState broken = event.getState();
            if (broken.is(Blocks.CHEST) || broken.is(Blocks.TRAPPED_CHEST) || broken.is(Blocks.BARREL)) {
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        com.soybe.awakeningsystem.subspace.SubspaceManager.syncWorldToSubspace(player.serverLevel(), player);
                        SystemData d = data(player);
                        player.displayClientMessage(Component.literal("§e✦ Almacén actualizado: §e" + d.subspaceChestCount() + " Cofres §7(§b" + d.subspaceUnlockedSlots() + " ranuras§7)"), true);
                    });
                }
            }
        }

        SystemData data = data(player);
        boolean before = data.skills().containsKey("stone_strike");
        int rawMining = event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE) ? 2 : 1;
        int gained = data.learn("mining", rawMining);
        int miningRepetition = data.repetition("mining");
        int experience = data.affinityExperience(isOre(event.getState()) ? 3 : 1);
        if (gained <= 0) experience = 0;
        boolean changed = gained > 0;
        if (isOre(event.getState())) {
            changed |= learnAndAnnounce(player, data, "perception", 2, "ore_sense") > 0;
            Integer oreSense = data.skills().get("ore_sense");
            if (oreSense != null) {
                int oreLevel = oreSense;
                boolean evolved = oreLevel >= 6;
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, (evolved ? 160 : 80) + oreLevel * 20, 0, false, false, true));
                if (evolved) player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80 + oreLevel * 10, 0, false, false, true));
                changed |= gainSkillProgress(player, data, "ore_sense");
            }
        }
        if (event.getState().getBlock() instanceof CropBlock) {
            changed |= learnAndAnnounce(player, data, "nature", 2, "green_thumb") > 0;
            Integer greenThumb = data.skills().get("green_thumb");
            if (greenThumb != null) {
                int greenLevel = greenThumb;
                boolean evolved = greenLevel >= 6;
                player.getFoodData().eat(evolved ? 2 : 1, greenLevel * (evolved ? 0.2F : 0.1F));
                if (evolved) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, true));
                changed |= gainSkillProgress(player, data, "green_thumb");
            }
        }
        if (data.isSkillEnabled("estate_workmanship")) {
            if (isOre(event.getState())) {
                awardExperience(player, data, 4);
            }
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && held.isDamageableItem() && player.getRandom().nextBoolean()) {
                held.setDamageValue(Math.max(0, held.getDamageValue() - 1));
            }
        }
        if (event.getState().is(BlockTags.BASE_STONE_OVERWORLD) && data.isSkillEnabled("stone_strike")) {
            int skillLevel = data.skills().get("stone_strike");
            boolean evolved = skillLevel >= 6;
            if (miningRepetition <= SystemData.REPETITION_DISABLE) {
                experience += evolved ? skillLevel * 2 : skillLevel;
            }
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60 + skillLevel * 10, Math.min(2, skillLevel - 1), false, false, true));
            if (data.canAdvanceSkill("stone_strike") && data.gainSkillProgress("stone_strike", 1)) {
                announceSkillLevel(player, "stone_strike", data.skills().get("stone_strike"));
                changed = true;
            }
        }
        if (experience > 0) {
            awardExperience(player, data, experience);
            changed = true;
        }
        if (!before && data.skills().containsKey("stone_strike")) {
            announceSkill(player, "stone_strike");
            changed = true;
        }
        data.recordStatistic("blocks_mined", 1);
        if (isOre(event.getState())) data.recordStatistic("ores_mined", 1);
        long week = player.level().getDayTime() / SystemData.WEEK_TICKS;
        if (SystemConfig.WEEKLY_MISSIONS_ENABLED.get()) {
            data.recordWeeklyProgress(week, 1, 1);
            if (data.completeWeekly(week)) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.weekly_complete.title",
                        "awakening_system.notification.weekly_complete.message", 0xFF8BE9FD);
                changed = true;
            }
        }
        checkAchievements(player, data);
        long day = player.level().getDayTime() / 24000L;
        changed |= updateDailyProgress(player, data, day, 0, 1);
        if (data.completeDaily(player.level().getGameTime())) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.daily_complete.title",
                    "awakening_system.notification.daily_complete.message", 0xFF70E8FF);
            changed = true;
        }
                if (isOre(event.getState()) && player.getY() < 0 && data.activeScenario().equals("depth_expedition")) {
            if (data.progressScenario("depth_expedition", 1)) {
                announceScenarioComplete(player, data);
                changed = true;
            }
        }
        if (event.getState().is(BlockTags.DIAMOND_ORES) && data.completeSecretMission()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.secret_complete.title",
                    "awakening_system.notification.secret_complete.message", 0xFFBB7DFF);
            changed = true;
        }
        if (data.recordAwakeningMining()) {
            changed = true;
        }
        completeAwakening(player, data);
        completeClassMission(player, data);
        if (changed) AwakeningSystemMod.sync(player);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        DungeonManager.onEntityKilled(event.getEntity().getUUID());
        TowerManager.onEntityKilled(event.getEntity().getUUID());
        PagodaTrialManager.onEntityKilled(event.getEntity().getUUID());

        if (!event.getEntity().level().isClientSide() && event.getEntity().level() instanceof ServerLevel slDrops) {
            RpgLevelZoneManager.handleMobDrops(event.getEntity(), slDrops);
            handleDimensionalKeyDrops(event.getEntity(), slDrops);
        }

        // Trial de la Diosa de la Luz (Santuario Isekai)
        if (event.getEntity().getTags().contains("goddess_trial_target") && event.getEntity().level() instanceof ServerLevel sl) {
            for (String tag : event.getEntity().getTags()) {
                if (tag.startsWith("altar_")) {
                    try {
                        long posLong = Long.parseLong(tag.substring("altar_".length()));
                        BlockPos altarPos = BlockPos.of(posLong);
                        ServerPlayer killer = null;
                        if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                            killer = sp;
                        } else if (event.getEntity().getLastHurtByMob() instanceof ServerPlayer sp) {
                            killer = sp;
                        }
                        if (killer == null) {
                            var trialState = com.soybe.awakeningsystem.block.GoddessAltarBlock.ACTIVE_TRIALS.get(altarPos);
                            if (trialState != null && sl.getServer() != null) {
                                killer = sl.getServer().getPlayerList().getPlayer(trialState.playerId);
                            }
                        }
                        if (killer != null) {
                            com.soybe.awakeningsystem.block.GoddessAltarBlock.checkTrialProgress(sl, altarPos, killer);
                        }
                    } catch (Exception ignored) {}
                    break;
                }
            }
        }

        // Pruebas del Santuario Trilunar de Columbina (Aria y Canon)
        if (event.getEntity().getTags().contains("trilunar_trial_target") && event.getEntity().level() instanceof ServerLevel sl) {
            String trialType = event.getEntity().getTags().contains("trilunar_aria") ? "aria" : "canon";
            for (String tag : event.getEntity().getTags()) {
                if (tag.startsWith("altar_")) {
                    try {
                        long posLong = Long.parseLong(tag.substring("altar_".length()));
                        BlockPos altarPos = BlockPos.of(posLong);
                        ServerPlayer killer = null;
                        if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                            killer = sp;
                        } else if (event.getEntity().getLastHurtByMob() instanceof ServerPlayer sp) {
                            killer = sp;
                        }
                        if (killer == null) {
                            var trialState = com.soybe.awakeningsystem.block.TrilunarAltarBlock.ACTIVE_TRIALS.get(altarPos);
                            if (trialState != null && sl.getServer() != null) {
                                killer = sl.getServer().getPlayerList().getPlayer(trialState.playerId);
                            }
                        }
                        if (killer != null) {
                            com.soybe.awakeningsystem.block.TrilunarAltarBlock.checkTrialProgress(sl, altarPos, killer, trialType);
                        }
                    } catch (Exception ignored) {}
                    break;
                }
            }
        }

        // Reconstrucción de Soldados Sombra con maná del portador (Solo Leveling)
        if (event.getEntity().getTags().contains("shadow_soldier") && event.getEntity() instanceof net.minecraft.world.entity.Mob soldier) {
            String masterUUIDStr = null;
            for (String tag : soldier.getTags()) {
                if (tag.startsWith("shadow_master_")) {
                    masterUUIDStr = tag.substring("shadow_master_".length());
                    break;
                }
            }
            if (masterUUIDStr != null && soldier.level().getServer() != null) {
                try {
                    UUID masterUUID = UUID.fromString(masterUUIDStr);
                    ServerPlayer master = soldier.level().getServer().getPlayerList().getPlayer(masterUUID);
                    if (master != null && master.isAlive()) {
                        SystemData masterData = data(master);
                        if (masterData.energy() >= 15) {
                            masterData.spendEnergy(15);
                            event.setCanceled(true);
                            soldier.setHealth(soldier.getMaxHealth() * 0.5F);
                            soldier.removeAllEffects();
                            soldier.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 2, false, false, false));
                            soldier.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false, false));
                            soldier.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));

                            if (soldier.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.8D, soldier.getZ(), 35, 0.4D, 0.5D, 0.4D, 0.08D);
                                sl.sendParticles(ParticleTypes.SCULK_SOUL, soldier.getX(), soldier.getY() + 1.0D, soldier.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                                sl.playSound(null, soldier.getX(), soldier.getY(), soldier.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 1.3F);
                            }

                            String soldierName = soldier.getCustomName() != null ? soldier.getCustomName().getString() : Component.translatable("awakening_system.mob.shadow_infantry").getString();
                            AwakeningSystemMod.notify(master, "awakening_system.notification.shadow_reconstruct.title",
                                    "awakening_system.notification.shadow_reconstruct.desc", 0xFF9966FF, soldierName, 15);
                            AwakeningSystemMod.sync(master);
                            return;
                        } else {
                            AwakeningSystemMod.notify(master, "awakening_system.notification.shadow_mana_low.title",
                                    "awakening_system.notification.shadow_mana_low.desc", 0xFFFF7666);
                            AwakeningSystemMod.sync(master);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Registro de Bajas y Ascensos de Rango para Soldados Sombra (Solo Leveling)
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.Mob killerMob
                && killerMob.getTags().contains("shadow_soldier")) {
            promoteOrTrackShadowKill(killerMob);
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            SystemData playerData = data(player);
            playerData.resetEmergencySurvival();

            // Intercepción de Daño Letal de la Proyección del Alma Naciente (Nascent Soul Avatar)
            if (player.getPersistentData().getInt("nascent_soul_guard_ticks") > 0) {
                player.getPersistentData().putInt("nascent_soul_guard_ticks", 0);
                event.setCanceled(true);
                player.setHealth(12.0F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.0D, 1.2D, 1.0D, 0.25D);
                    sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.8D, 1.0D, 0.8D, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.1F);
                }
                player.displayClientMessage(Component.literal("§d§l✦ [ALMA NACIENTE INTERCEPTÓ LA MUERTE] §f¡Tu Alma Naciente absorbió el golpe fatal y te devolvió a la vida!"), true);
                AwakeningSystemMod.sync(player);
                return;
            }

            boolean hasPhoenixFeather = com.soybe.awakeningsystem.item.ManhwaArtifactItem.hasArtifact(player, com.soybe.awakeningsystem.item.ManhwaArtifactItem.ArtifactType.PHOENIX_FEATHER);
            if ((playerData.isSkillEnabled("phoenix_rebirth") || hasPhoenixFeather)
                    && player.level().getGameTime() >= playerData.phoenixCooldownUntil()) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth() * 0.5F);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true, true));
                playerData.setPhoenixCooldownUntil(player.level().getGameTime() + 12000L);
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 3.5D, 6.0D, 6, 18);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.LAVA, 6.0D, 32, 0.15D);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 70, 0.8D, 1.0D, 0.8D, 0.25D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.0F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 0.8F);
                }
                AwakeningSystemMod.notify(player, "awakening_system.notification.phoenix.title",
                        "awakening_system.notification.phoenix.message", 0xFFFF7043);
                AwakeningSystemMod.sync(player);
                return;
            }

            // Rebobinado del Rey de la Muerte (Legado de Kim Gong-ja / Reloj Sagrado)
            boolean hasClock = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(SystemItems.DEATH_KING_CLOCK.get())) {
                    hasClock = true;
                    break;
                }
            }
            long rewindCdUntil = player.getPersistentData().getLong("death_king_rewind_cooldown");
            long gameTime = player.level().getGameTime();

            if ((hasClock || playerData.isSkillEnabled("death_king_rewind")) && gameTime >= rewindCdUntil) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth() * 0.4F);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true, true));
                player.getPersistentData().putLong("death_king_rewind_cooldown", gameTime + 6000L); // 5 min cooldown

                if (player.level() instanceof ServerLevel sl) {
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, ParticleTypes.ENCHANT, 4.0D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.END_ROD, 5.0D, 32, 0.08D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.0F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.6F);
                }

                AwakeningSystemMod.notify(player,
                        "awakening_system.event.kim_gongja_rewind_title",
                        "awakening_system.event.kim_gongja_rewind_msg",
                        0xFF55FF55);
                AwakeningSystemMod.sync(player);
                return;
            }

            // Captura de inventario en caso de muerte dentro de portales/mazmorras/torres/pagodas
            boolean inInstance = DungeonManager.isInDungeon(player.getUUID())
                    || TowerManager.isInTower(player.getUUID())
                    || PagodaTrialManager.isInTrial(player.getUUID())
                    || player.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                    || player.level().dimension().equals(TowerManager.RANKER_TOWER)
                    || player.level().dimension().equals(PagodaTrialManager.PAGODA_REALM);
            if (inInstance) {
                NonNullList<ItemStack> saved = NonNullList.withSize(player.getInventory().getContainerSize(), ItemStack.EMPTY);
                net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    saved.set(i, stack.copy());
                    if (!stack.isEmpty()) {
                        net.minecraft.nbt.CompoundTag slotTag = new net.minecraft.nbt.CompoundTag();
                        slotTag.putInt("Slot", i);
                        slotTag.put("Item", stack.save(player.registryAccess()));
                        listTag.add(slotTag);
                    }
                }
                SAVED_DUNGEON_DEATH_INVENTORIES.put(player.getUUID(), saved);
                player.getPersistentData().put("dungeon_preserved_inventory", listTag);
                com.soybe.awakeningsystem.compat.CuriosCompatHandler.saveCuriosOnDeath(player);
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() != player) {
            // Progreso de caza para misiones del Dokkaebi
            if (event.getEntity() instanceof net.minecraft.world.entity.monster.Monster) {
                AABB searchBox = player.getBoundingBox().inflate(32.0D);
                List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = player.serverLevel().getEntitiesOfClass(
                        com.soybe.awakeningsystem.entity.DokkaebiEntity.class, searchBox,
                        d -> "kill_monsters".equals(d.getQuestType()) && d.isQuestActive());
                for (var d : dokkaebis) {
                    d.addProgress(1, player);
                }
            }
            SystemData data = data(player);
            boolean before = data.skills().containsKey("combat_instinct");
            int gained = data.learn("combat", 5);
            if (event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse_expansion")
                    || event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse")) {
                data.learn("eclipse", 5);
            }
            boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(event.getEntity());

            // Experiencia de combate balanceada según salud y tipo de criatura (máx 35 para monstruos normales, máx 500 para jefes)
            int killExp;
            if (isBoss) {
                killExp = Math.min(500, Math.max(50, (int) (event.getEntity().getMaxHealth() * 0.15F)));
            } else {
                killExp = Math.min(35, Math.max(5, (int) (event.getEntity().getMaxHealth() * 0.15F)));
            }
            awardExperience(player, data, killExp);
            if (!before && data.skills().containsKey("combat_instinct")) announceSkill(player, "combat_instinct");
            int reward = ShopDefinitions.mobReward(event.getEntity().getMaxHealth(), event.getEntity().getType().getCategory().isFriendly() == false);
            if (isBoss) {
                reward = (int) Math.round(reward * SystemConfig.BOSS_COIN_MULTIPLIER.get());
                data.recordStatistic("boss_kills", 1);
                triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.BOSS_KILL);
                com.soybe.awakeningsystem.config.BossCompatibilityManager.handleBossDefeat(event.getEntity(), player, data);

                if (event.getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
                    if (!grantFable(player, "first_ender_dragon")) {
                        grantFable(player, "solo_dragon_slayer");
                    }
                } else if (event.getEntity() instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
                    grantFable(player, "first_wither");
                } else if (event.getEntity() instanceof com.soybe.awakeningsystem.entity.BaranBossEntity) {
                    grantFable(player, "first_monarch_defeat");
                    grantFable(player, "giant_story_demon_king");
                } else if (event.getEntity() instanceof com.soybe.awakeningsystem.entity.IgrisBossEntity) {
                    grantFable(player, "first_monarch_defeat");
                }
            }
            // Asimilacion de Habilidades de Monstruos (SSS-Class Revival Hunter)
            // Balanceado: Requiere nivel 40+ y que la criatura sea jefe o monstruo de élite escalado
            boolean isEliteScaled = event.getEntity().getPersistentData().contains("ScaledMobTier");
            if (data.level() >= 40 && (isBoss || isEliteScaled)) {
                var entityType = event.getEntity().getType();
                String mobSkillToLearn = null;
                int absorbChance = isBoss ? 12 : 4;
                if (entityType == net.minecraft.world.entity.EntityType.ENDERMAN) {
                    mobSkillToLearn = "dimensional_blink";
                } else if (entityType == net.minecraft.world.entity.EntityType.BLAZE) {
                    mobSkillToLearn = "blaze_ignition";
                } else if (entityType == net.minecraft.world.entity.EntityType.RAVAGER || entityType == net.minecraft.world.entity.EntityType.WARDEN || entityType == net.minecraft.world.entity.EntityType.IRON_GOLEM) {
                    mobSkillToLearn = "seismic_roar";
                    absorbChance = isBoss ? 18 : 6;
                } else if (entityType == net.minecraft.world.entity.EntityType.PHANTOM || entityType == net.minecraft.world.entity.EntityType.VEX) {
                    mobSkillToLearn = "spectral_shift";
                } else if (entityType == net.minecraft.world.entity.EntityType.SKELETON || entityType == net.minecraft.world.entity.EntityType.STRAY || entityType == net.minecraft.world.entity.EntityType.BOGGED) {
                    mobSkillToLearn = "piercing_shot";
                } else if (entityType == net.minecraft.world.entity.EntityType.SPIDER || entityType == net.minecraft.world.entity.EntityType.CAVE_SPIDER) {
                    mobSkillToLearn = "web_snare";
                } else if (entityType == net.minecraft.world.entity.EntityType.CREEPER) {
                    mobSkillToLearn = "kinetic_blast";
                } else if (entityType == net.minecraft.world.entity.EntityType.DROWNED || entityType == net.minecraft.world.entity.EntityType.GUARDIAN || entityType == net.minecraft.world.entity.EntityType.ELDER_GUARDIAN) {
                    mobSkillToLearn = "tidal_surge";
                }
                if (mobSkillToLearn != null && !data.skills().containsKey(mobSkillToLearn)) {
                    if (player.getRandom().nextInt(100) < absorbChance) {
                        data.skills().put(mobSkillToLearn, 1);
                        data.gainSkillProgress(mobSkillToLearn, 1);
                        SkillDefinitions.Definition skillDef = SkillDefinitions.get(mobSkillToLearn);
                        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_assimilated.title",
                                "awakening_system.notification.skill_assimilated.message", 0xFFBB7DFF,
                                skillDef != null ? skillDef.nameKey() : mobSkillToLearn);
                        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.3F);
                        if (player.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
                        }
                    }
                }
            }
            // Runa del Monarca de las Sombras: Deja Resto de Alma de Sombra en el suelo para extracción ("¡SURGE!")
            boolean hasShadowMonarch = (data.hasHunterClass() && "shadow_monarch".equals(data.hunterClass()))
                    || data.skills().containsKey("unique_absolute_extraction");
            if (hasShadowMonarch
                    && !event.getEntity().getType().getCategory().isFriendly()
                    && !(event.getEntity() instanceof ServerPlayer)
                    && !event.getEntity().getTags().contains("shadow_soldier")
                    && !(event.getEntity() instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity)
                    && !(event.getEntity() instanceof com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity)
                    && !event.getEntity().getTags().contains("no_shadow_extraction")
                    && !PagodaTrialManager.isTrialEnemy(event.getEntity().getUUID())
                    && player.level() instanceof ServerLevel sl) {
                var defeated = event.getEntity();
                if (defeated instanceof net.minecraft.world.entity.Mob defeatedMob) {
                    var soul = new com.soybe.awakeningsystem.entity.ShadowSoulEntity(
                            sl, defeated.getX(), defeated.getY(), defeated.getZ(), defeatedMob, player.getUUID()
                    );
                    sl.addFreshEntity(soul);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, defeated.getX(), defeated.getY() + 0.3D, defeated.getZ(), 25, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.SCULK_SOUL, defeated.getX(), defeated.getY() + 0.4D, defeated.getZ(), 15, 0.2D, 0.3D, 0.2D, 0.03D);
                    sl.playSound(null, defeated.getX(), defeated.getY(), defeated.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.8F, 1.2F);

                    String defeatedName = defeatedMob.getCustomName() != null ? defeatedMob.getCustomName().getString() : defeatedMob.getType().getDescription().getString();
                    AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_soul_dropped.title",
                            "awakening_system.notification.shadow_soul_dropped.desc", 0xFF9966FF, defeatedName);
                }
            }
            // Progreso de Misiones Temáticas de Patrocinador (ORV & Solo Leveling)
            if (data.hasSponsor() && !event.getEntity().getType().getCategory().isFriendly()) {
                var quests = SponsorQuestDefinitions.getQuestsForSponsor(data.sponsorConstellation());
                if (!quests.isEmpty()) {
                    if (isBoss) {
                        for (var q : quests) {
                            if (q.targetCount() <= 3) {
                                data.addCoins(q.coinReward());
                                data.addAttribute(q.bonusStat());
                                SystemAttributeEffects.apply(player);
                                AwakeningSystemMod.notify(player, "awakening_system.notification.sponsor_quest_complete.title",
                                        "awakening_system.notification.sponsor_quest_complete.msg", 0xFF50FA7B,
                                        q.title().getString(), q.bonusStat().toUpperCase());
                                break;
                            }
                        }
                    } else if (player.getRandom().nextInt(100) < 12) {
                        var q = quests.get(player.getRandom().nextInt(quests.size()));
                        int bounty = 20 + player.getRandom().nextInt(20);
                        data.addCoins(bounty);
                        AwakeningSystemMod.notify(player, "awakening_system.notification.sponsor_quest_progress.title",
                                "awakening_system.notification.sponsor_quest_progress.msg", 0xFFFFD700,
                                q.title().getString(), String.valueOf(bounty));
                    }
                }
            }
            // Progreso de Escenarios Mundiales Dinamicos (ORV)
            if (data.activeScenario().equals("night_hunt") && player.level().isNight() && !event.getEntity().getType().getCategory().isFriendly()) {
                if (data.progressScenario("night_hunt", 1)) announceScenarioComplete(player, data);
            } else if (data.activeScenario().equals("depth_expedition") && player.getY() < 0 && !event.getEntity().getType().getCategory().isFriendly()) {
                if (data.progressScenario("depth_expedition", 1)) announceScenarioComplete(player, data);
            } else if (data.activeScenario().equals("abyssal_conquest") && isBoss) {
                if (data.progressScenario("abyssal_conquest", 1)) announceScenarioComplete(player, data);
            } else if (data.activeScenario().equals("survival_trial") && !event.getEntity().getType().getCategory().isFriendly()) {
                if (data.progressScenario("survival_trial", 1)) announceScenarioComplete(player, data);
            }
            if (data.isDokkaebiScenarioActive()) {
                data.recordDokkaebiKill();
            }
            // Progreso de Misión de Ascensión de Columbina (Bendición Lunar Mejorada)
            if (!data.isWelkinEnhanced() && data.welkinQuestStep() > 0) {
                if (data.welkinQuestStep() == 1 && !event.getEntity().getType().getCategory().isFriendly()
                        && (player.level().isNight() || player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD))) {
                    data.addWelkinQuestProgress(1);
                    if (data.welkinQuestProgress() >= 20) {
                        data.setWelkinQuestStep(2);
                        data.setWelkinQuestProgress(0);
                        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                        AwakeningSystemMod.notify(player,
                                "awakening_system.welkin.quest.step1_complete_title",
                                "awakening_system.welkin.quest.step2_desc",
                                0xFFCE93D8);
                        player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.step2_started_chat"), false);
                    } else if (data.welkinQuestProgress() % 5 == 0) {
                        AwakeningSystemMod.notify(player,
                                "awakening_system.welkin.quest.progress_title",
                                "awakening_system.welkin.quest.step1_progress_msg",
                                0xFFBA68C8,
                                String.valueOf(data.welkinQuestProgress()), "20");
                    }
                    AwakeningSystemMod.sync(player);
                } else if (data.welkinQuestStep() == 2 && isBoss) {
                    data.setWelkinQuestStep(3);
                    data.setWelkinQuestProgress(0);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                    AwakeningSystemMod.notify(player,
                            "awakening_system.welkin.quest.step2_complete_title",
                            "awakening_system.welkin.quest.step3_desc",
                            0xFFCE93D8);
                    player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.step3_started_chat"), false);
                    AwakeningSystemMod.sync(player);
                }
            }
            // Progreso de Contratos de Cacería del Tablón del Gremio (Isekai)
            CompoundTag pTag = player.getPersistentData();
            if (pTag.getBoolean("bounty_active")) {
                int killed = pTag.getInt("bounty_killed");
                int needed = pTag.getInt("bounty_needed");
                if (killed < needed) {
                    String target = pTag.getString("bounty_target");
                    boolean match = false;
                    var killedEntity = event.getEntity();
                    String typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType()).toString();

                    if ("any_isekai".equals(target)) {
                        match = killedEntity.level().dimension() == com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD
                                || !killedEntity.getType().getCategory().isFriendly();
                    } else if ("isekai_slime".equals(target)) {
                        match = typeId.contains("slime");
                    } else if ("giant_slime".equals(target)) {
                        match = typeId.contains("slime") && (killedEntity instanceof net.minecraft.world.entity.monster.Slime slm && slm.getSize() >= 4);
                    } else if ("isekai_wolf".equals(target)) {
                        match = typeId.contains("wolf") || typeId.contains("spider") || typeId.contains("fox");
                    } else if ("isekai_skeleton".equals(target)) {
                        match = typeId.contains("skeleton") || typeId.contains("stray") || typeId.contains("bogged");
                    } else if ("isekai_spider".equals(target)) {
                        match = typeId.contains("spider") || typeId.contains("cave_spider");
                    } else if ("isekai_beholder".equals(target)) {
                        match = typeId.contains("beholder") || typeId.contains("eye") || typeId.contains("phantom");
                    } else if ("isekai_guardian".equals(target)) {
                        match = typeId.contains("guardian") || typeId.contains("golem") || typeId.contains("warden");
                    } else if ("dragon_threat".equals(target)) {
                        match = typeId.contains("dragon") || isBoss;
                    } else if ("goblin_scout".equals(target)) {
                        match = typeId.contains("goblin_scout") || (typeId.contains("goblin") && !typeId.contains("shaman") && !typeId.contains("warrior"));
                    } else if ("goblin_warrior".equals(target)) {
                        match = typeId.contains("goblin_warrior");
                    } else if ("goblin_shaman".equals(target)) {
                        match = typeId.contains("goblin_shaman");
                    } else if ("goblin_threat".equals(target)) {
                        match = typeId.contains("goblin");
                    } else if ("isekai_monster".equals(target)) {
                        match = typeId.contains("beholder") || typeId.contains("mandrake") || typeId.contains("pixie") || isBoss;
                    } else if ("bat_hunt".equals(target)) {
                        match = typeId.contains("bat");
                    } else if ("drowned_hunt".equals(target)) {
                        match = typeId.contains("drowned");
                    } else if ("hilichurl_hunt".equals(target)) {
                        match = typeId.contains("hilichurl");
                    } else if ("mitachurl_hunt".equals(target)) {
                        match = typeId.contains("mitachurl");
                    } else if ("ruin_guard_hunt".equals(target)) {
                        match = typeId.contains("ruin_guard");
                    } else if ("shadow_direwolf".equals(target)) {
                        match = typeId.contains("shadow_direwolf") || typeId.contains("wolf");
                    } else if ("geovishap_hunt".equals(target)) {
                        match = typeId.contains("geovishap") || typeId.contains("vishap");
                    } else if ("ruin_hunter_hunt".equals(target)) {
                        match = typeId.contains("ruin_hunter");
                    } else if ("ruin_drake_hunt".equals(target)) {
                        match = typeId.contains("ruin_drake");
                    } else if ("boss_snowboar_king".equals(target)) {
                        match = typeId.contains("great_snowboar_king") || typeId.contains("snowboar");
                    } else if ("spirit_fox".equals(target)) {
                        match = typeId.contains("spirit_fox") || typeId.contains("fox") || typeId.contains("hilichurl");
                    } else if ("creeper_hunt".equals(target)) {
                        match = typeId.contains("creeper");
                    } else if ("frost_elf".equals(target)) {
                        match = typeId.contains("frost_elf");
                    } else if ("mandrake_hunt".equals(target)) {
                        match = typeId.contains("mandrake");
                    } else if ("cave_spider".equals(target)) {
                        match = typeId.contains("cave_spider");
                    } else if ("beastman_warrior".equals(target)) {
                        match = typeId.contains("beastman");
                    } else if ("blood_disciple".equals(target)) {
                        match = typeId.contains("blood_sect_disciple") || typeId.contains("cultivator") || typeId.contains("mitachurl");
                    } else if ("enderman_hunt".equals(target)) {
                        match = typeId.contains("enderman");
                    } else if ("witch_hunt".equals(target)) {
                        match = typeId.contains("witch");
                    } else if ("abyssal_orc".equals(target)) {
                        match = typeId.contains("abyssal_orc");
                    } else if ("abyssal_necromancer".equals(target)) {
                        match = typeId.contains("abyssal_necromancer");
                    } else if ("jiangshi_hunt".equals(target)) {
                        match = typeId.contains("jiangshi") || typeId.contains("ruin_guard");
                    } else if ("demonic_cultivator".equals(target)) {
                        match = typeId.contains("demonic_cultivator") || typeId.contains("shadow_direwolf");
                    } else if ("ant_soldier".equals(target)) {
                        match = typeId.contains("ant_soldier");
                    } else if ("thunder_tiger".equals(target)) {
                        match = typeId.contains("white_thunder_tiger") || typeId.contains("tiger") || typeId.contains("geovishap");
                    } else if ("cloud_crane".equals(target)) {
                        match = typeId.contains("cloud_crane") || typeId.contains("crane") || typeId.contains("ruin_hunter");
                    } else if ("jade_beast".equals(target)) {
                        match = typeId.contains("jade_armored_beast") || typeId.contains("ruin_drake");
                    } else if ("warden_hunt".equals(target)) {
                        match = typeId.contains("warden") || typeId.contains("iron_golem");
                    } else if ("boss_elemental_demon_king".equals(target)) {
                        match = typeId.contains("elemental_demon_king") || typeId.contains("elemental_lord");
                    } else if ("boss_blood_patriarch".equals(target)) {
                        match = typeId.contains("blood_patriarch") || typeId.contains("great_snowboar_king");
                    } else if ("boss_yggdrasil_treant".equals(target)) {
                        match = typeId.contains("yggdrasil_treant");
                    } else if ("boss_shadow_monarch".equals(target)) {
                        match = typeId.contains("shadow_monarch") || typeId.contains("igris") || typeId.contains("baran");
                    }

                    if (match) {
                        killed++;
                        pTag.putInt("bounty_killed", killed);
                        String bName = pTag.getString("bounty_name");
                        if (killed >= needed) {
                            player.displayClientMessage(Component.translatable("awakening_system.bounty.ready_to_claim", bName)
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                        } else {
                            player.displayClientMessage(Component.translatable("awakening_system.bounty.progress_toast", bName, killed, needed)
                                    .withStyle(ChatFormatting.YELLOW), true);
                            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
                        }
                    }
                }
            }
            double coinMultiplier = SystemConfig.COIN_MULTIPLIER.get();
            if (SystemConfig.EVENT_NIGHT_BONUS.get() && player.level().isNight()) coinMultiplier += 0.5D;
            if ("queen_of_the_darkest_night".equals(data.sponsorConstellation())) coinMultiplier += 0.5D;
            if ("star_apostle".equals(data.hunterClass())) coinMultiplier += 1.0D; // +100% Monedas para Apóstol del Star Stream
            coinMultiplier += data.prestigeLevel() * SystemConfig.PRESTIGE_COIN_BONUS_PERCENT.get();
            reward = Math.max(1, (int) Math.round(reward * coinMultiplier));
            data.addCoins(reward);
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.coin_title",
                    "awakening_system.notification.shop.coin_message", 0xFFFFD866, reward);
            data.recordStatistic("creatures_defeated", 1);
            long week = player.level().getDayTime() / SystemData.WEEK_TICKS;
            if (SystemConfig.WEEKLY_MISSIONS_ENABLED.get()) {
                data.recordWeeklyProgress(week, 0, 1);
                if (data.completeWeekly(week)) {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.weekly_complete.title",
                            "awakening_system.notification.weekly_complete.message", 0xFF8BE9FD);
                }
            }
            checkAchievements(player, data);
            applyCompatibilityCombatSkills(player, data);
            long day = player.level().getDayTime() / 24000L;
            updateDailyProgress(player, data, day, 1, 1);
            if (data.completeDaily(player.level().getGameTime())) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.daily_complete.title",
                        "awakening_system.notification.daily_complete.message", 0xFF70E8FF);
            }
            data.recordAwakeningCombat();
            completeAwakening(player, data);
            completeClassMission(player, data);

            // Registro de bajas y otorgamiento de EXP al arma empuñada
            ItemStack heldWp = player.getMainHandItem();
            if (com.soybe.awakeningsystem.item.WeaponLevelManager.isLevelableWeapon(heldWp)) {
                com.soybe.awakeningsystem.item.WeaponLevelManager.recordKill(heldWp);
                int mobExp = isBoss ? 1500 : 45;
                com.soybe.awakeningsystem.item.WeaponLevelManager.addExp(heldWp, mobExp, player);
            }

            AwakeningSystemMod.sync(player);
        }
    }

    private static void handleDimensionalKeyDrops(LivingEntity entity, ServerLevel level) {
        var rand = level.getRandom();
        // 1. Minijefes del Overworld
        if (entity.getPersistentData().getBoolean("is_overworld_miniboss")) {
            entity.spawnAtLocation(new ItemStack(SystemItems.OVERWORLD_DUNGEON_KEY.get(), 1));
            return;
        }

        String dim = level.dimension().location().getPath().toLowerCase(java.util.Locale.ROOT);
        // 2. Nether
        if (dim.contains("nether")) {
            if (entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
                entity.spawnAtLocation(new ItemStack(SystemItems.NETHER_FLAME_KEY.get(), 1));
            } else if (entity instanceof net.minecraft.world.entity.monster.WitherSkeleton
                    || entity instanceof net.minecraft.world.entity.monster.Blaze
                    || entity instanceof net.minecraft.world.entity.monster.piglin.PiglinBrute
                    || entity instanceof net.minecraft.world.entity.monster.Ghast) {
                if (rand.nextFloat() < 0.08F) {
                    entity.spawnAtLocation(new ItemStack(SystemItems.NETHER_FLAME_KEY.get(), 1));
                }
            }
        }
        // 3. End
        else if (dim.contains("end")) {
            if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
                entity.spawnAtLocation(new ItemStack(SystemItems.END_VOID_KEY.get(), 1));
            } else if (entity instanceof net.minecraft.world.entity.monster.Shulker) {
                if (rand.nextFloat() < 0.25F) {
                    entity.spawnAtLocation(new ItemStack(SystemItems.END_VOID_KEY.get(), 1));
                }
            } else if (entity instanceof net.minecraft.world.entity.monster.EnderMan) {
                if (rand.nextFloat() < 0.03F) {
                    entity.spawnAtLocation(new ItemStack(SystemItems.END_VOID_KEY.get(), 1));
                }
            }
        }
        // 4. Isekai
        else if (dim.contains("isekai")) {
            if (entity instanceof com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.ElementalLordBossEntity) {
                entity.spawnAtLocation(new ItemStack(SystemItems.ISEKAI_MANA_KEY.get(), 1));
            } else if (entity instanceof com.soybe.awakeningsystem.entity.GoblinShamanEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.BeastmanWarriorEntity) {
                if (rand.nextFloat() < 0.12F) {
                    entity.spawnAtLocation(new ItemStack(SystemItems.ISEKAI_MANA_KEY.get(), 1));
                }
            }
        }
        // 5. Murim
        else if (dim.contains("murim")) {
            if (entity instanceof com.soybe.awakeningsystem.entity.JadeGuardianBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity) {
                entity.spawnAtLocation(new ItemStack(SystemItems.MURIM_DAO_KEY.get(), 1));
            } else if (entity instanceof com.soybe.awakeningsystem.entity.DemonicCultivatorEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.JiangshiEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity) {
                if (rand.nextFloat() < 0.12F) {
                    entity.spawnAtLocation(new ItemStack(SystemItems.MURIM_DAO_KEY.get(), 1));
                }
            }
        }
        // 6. Teyvat
        else if (dim.contains("teyvat")) {
            if (entity instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.PyroRegisvineEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.CryoRegisvineEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.ElectroRegisvineEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.GreatSnowboarKingBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity
                    || entity instanceof com.soybe.awakeningsystem.entity.RuinSerpentBossEntity) {
                entity.spawnAtLocation(new ItemStack(SystemItems.TEYVAT_SHRINE_KEY.get(), 1));
            }
        }
    }

    @SubscribeEvent
    public static void onItemEntityPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        CompoundTag pTag = player.getPersistentData();
        if (!pTag.getBoolean("bounty_active")) return;

        String target = pTag.getString("bounty_target");
        if (!target.startsWith("collect_")) return;

        int needed = pTag.getInt("bounty_needed");
        int currentCount = com.soybe.awakeningsystem.block.BountyBoardBlock.countInventoryItems(player, target);
        int prevKilled = pTag.getInt("bounty_killed");
        int newKilled = Math.min(needed, currentCount);

        pTag.putInt("bounty_killed", newKilled);

        if (newKilled > prevKilled) {
            String bName = pTag.getString("bounty_name");
            if (newKilled >= needed) {
                player.displayClientMessage(Component.translatable("awakening_system.bounty.ready_to_claim", bName)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.bounty.progress_toast", bName, newKilled, needed)
                        .withStyle(ChatFormatting.YELLOW), true);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onCombatDamage(LivingDamageEvent.Pre event) {
        // Protección de fuego amigo y sofocación en bloques para Soldados Sombra
        if (event.getEntity().getTags().contains("shadow_soldier")) {
            if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
                event.setNewDamage(0.0F);
                if (event.getEntity().level() instanceof ServerLevel sl) {
                    for (var tag : event.getEntity().getTags()) {
                        if (tag.startsWith("shadow_master_")) {
                            try {
                                java.util.UUID masterId = java.util.UUID.fromString(tag.substring("shadow_master_".length()));
                                ServerPlayer master = sl.getServer().getPlayerList().getPlayer(masterId);
                                if (master != null) {
                                    net.minecraft.world.phys.Vec3 safe = TeleportHelper.findSafeTeleportPos(sl, event.getEntity(), master.getX(), master.getY(), master.getZ(), master.position());
                                    event.getEntity().teleportTo(safe.x, safe.y, safe.z);
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                return;
            }
            if (event.getSource().getEntity() instanceof ServerPlayer sp && event.getEntity().getTags().contains("shadow_master_" + sp.getUUID())) {
                event.setNewDamage(0.0F);
                return;
            }
        }
        if (event.getSource().getEntity() != null && event.getSource().getEntity().getTags().contains("shadow_soldier")) {
            if (event.getEntity() instanceof ServerPlayer sp && event.getSource().getEntity().getTags().contains("shadow_master_" + sp.getUUID())) {
                event.setNewDamage(0.0F);
                return;
            }
            if (event.getEntity().getTags().contains("shadow_soldier")) {
                event.setNewDamage(0.0F);
                return;
            }
        }

        // Prevención de fuego amigo entre enemigos de la Torre de los Rankers y Pagoda de Nueve Tribulaciones
        if (event.getSource().getEntity() != null &&
                ((TowerManager.isTowerEnemy(event.getEntity().getUUID()) && TowerManager.isTowerEnemy(event.getSource().getEntity().getUUID()))
                || (PagodaTrialManager.isTrialEnemy(event.getEntity().getUUID()) && PagodaTrialManager.isTrialEnemy(event.getSource().getEntity().getUUID())))) {
            event.setNewDamage(0.0F);
            return;
        }

        // Balanceo contra jefes y monstruos con mucha vida (> 300 HP):
        // Limitar fuentes de daño porcentual directo (% vida actual / current HP damage de Apotheosis o similares)
        // para que no inflijan 800+ de daño de un solo golpe activando a su vez robos de vida descomunales.
        if (event.getEntity().getMaxHealth() > 300.0F) {
            String typeName = event.getSource().typeHolder().getRegisteredName();
            String msgId = event.getSource().getMsgId();
            if ((typeName != null && typeName.contains("current_hp")) || (msgId != null && msgId.contains("current_hp"))) {
                if (event.getNewDamage() > 45.0F) {
                    event.setNewDamage(45.0F);
                }
            }
        }

        // Escalado dinámico y protección anti-one-shot de Jefes de Mundo frente al jugador
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob bossMob
                && com.soybe.awakeningsystem.world.WorldBossScalingManager.isWorldBoss(bossMob)) {
            if (event.getSource().getEntity() instanceof ServerPlayer attackerPlayer) {
                com.soybe.awakeningsystem.world.WorldBossScalingManager.handleWorldBossIncomingDamage(event, bossMob, attackerPlayer);
            }
        }

        // Preservación Cósmica del Manto de la Belleza al recibir daño letal o crítico
        if (event.getEntity() instanceof ServerPlayer victim) {
            float incoming = event.getNewDamage();
            SystemData vData = data(victim);

            // Reducción de daño permanente por nivel de prestigio (+1.0% por prestigio, cap 50%)
            if (vData.prestigeLevel() > 0) {
                double presReduct = Math.min(0.50D, vData.prestigeLevel() * 0.010D);
                incoming = (float) (incoming * (1.0D - presReduct));
                event.setNewDamage(incoming);
            }

            // Hito Prestigio 25: Cuerpo de Diamante Inmortal (Salva de muerte una vez cada 10 minutos)
            if (vData.prestigeLevel() >= 25 && incoming >= victim.getHealth()) {
                long lastCheat = victim.getPersistentData().getLong("prestige_cheat_death_cd");
                long now = victim.level().getGameTime();
                if (now >= lastCheat) {
                    victim.getPersistentData().putLong("prestige_cheat_death_cd", now + (20 * 60 * 10L));
                    event.setNewDamage(0.0F);
                    victim.setHealth(Math.max(victim.getHealth(), victim.getMaxHealth() * 0.50F));
                    victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 4, false, false, true));
                    victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, true));
                    if (victim.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 60, 0.6D, 0.8D, 0.6D, 0.2D);
                        sl.playSound(null, victim.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
                    }
                    victim.displayClientMessage(Component.literal("§6§l✦ [CUERPO DE DIAMANTE INMORTAL] §e¡Tu poder de Prestigio ha evitado tu muerte! (Recarga: 10 min)"), false);
                    return;
                }
            }

            if (incoming >= victim.getHealth() || (victim.getHealth() - incoming <= 6.0F && incoming >= 6.0F)) {
                if (com.soybe.awakeningsystem.item.RobeOfTheBeautyItem.tryCosmicPreservation(victim, incoming)) {
                    event.setNewDamage(incoming * 0.5F);
                    incoming = event.getNewDamage();
                }
            }

            // Conjunto de Armadura de Mora Áurea (Salación Vital por Riqueza)
            if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(victim, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MORA)) {
                if (incoming >= victim.getHealth() || (victim.getHealth() - incoming <= 4.0F && incoming >= 6.0F)) {
                    boolean paid = false;
                    if (vData.mora() >= 5000) {
                        vData.spendMora(5000);
                        paid = true;
                    } else if (vData.coins() >= 300) {
                        vData.spendCoins(300);
                        paid = true;
                    } else {
                        for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
                            ItemStack st = victim.getInventory().getItem(i);
                            if (!st.isEmpty() && (st.is(SystemItems.MORA.get()) || st.is(SystemItems.MORA_POUCH.get()) || st.is(SystemItems.MORA_BUNDLE.get()))) {
                                st.shrink(1);
                                paid = true;
                                break;
                            }
                        }
                    }

                    if (paid) {
                        event.setNewDamage(0.0F);
                        victim.setHealth(Math.max(victim.getHealth(), 10.0F));
                        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 20 * 15, 3, false, false, true));
                        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 20 * 6, 1, false, false, true));
                        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, true));
                        if (victim.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 0.5, 0.5, 0.5, 0.2);
                            sl.playSound(null, victim.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                        }
                        victim.sendSystemMessage(Component.literal("§6§l[BARRERA ÁUREA DE MORA] §e¡El Dios del Comercio absorbió el golpe letal a cambio de tus riquezas!"));
                        AwakeningSystemMod.sync(victim);
                        return;
                    }
                }
            }

            // Curio: Manto del Doctor (activación por daño severo al caer bajo el 50% de HP)
            if (victim.getHealth() - incoming <= victim.getMaxHealth() * 0.5F) {
                for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
                    ItemStack st = victim.getInventory().getItem(i);
                    if (!st.isEmpty() && SystemItems.DOCTORS_ROBE.isBound() && st.is(SystemItems.DOCTORS_ROBE.get())) {
                        com.soybe.awakeningsystem.item.DoctorsRobeItem.trigger(victim);
                        break;
                    }
                }
            }
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() == player) return;
        SystemData data = data(player);
        boolean changed = false;

        // === BONIFICADOR PERMANENTE DE DAÑO POR PRESTIGIO (+1.5% POR PRESTIGIO) ===
        if (data.prestigeLevel() > 0) {
            double prestigeDmgMult = 1.0D + (data.prestigeLevel() * 0.015D);
            event.setNewDamage((float) (event.getNewDamage() * prestigeDmgMult));
        }

        // === SISTEMA UNIVERSAL DE SUBIDA DE NIVEL DE ARMAS ===
        ItemStack weaponHeld = player.getMainHandItem();
        if (com.soybe.awakeningsystem.item.WeaponLevelManager.isLevelableWeapon(weaponHeld)) {
            double wMult = com.soybe.awakeningsystem.item.WeaponLevelManager.getDamageMultiplier(weaponHeld);
            if (wMult > 1.0D) {
                event.setNewDamage((float) (event.getNewDamage() * wMult));
            }
            float bonusCrit = com.soybe.awakeningsystem.item.WeaponLevelManager.getBonusCritChance(weaponHeld);
            if (bonusCrit > 0 && player.getRandom().nextFloat() < bonusCrit) {
                event.setNewDamage(event.getNewDamage() * 1.5F);
                if (player.level() instanceof ServerLevel slCrit) {
                    slCrit.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 20, 0.4D, 0.6D, 0.4D, 0.15D);
                }
            }
            // Hito 25: Resonancia Marcial (onda de choque de corte a enemigos cercanos)
            if (com.soybe.awakeningsystem.item.WeaponLevelManager.hasMilestone(weaponHeld, 25) && player.getRandom().nextFloat() < 0.25F) {
                if (player.level() instanceof ServerLevel slWave) {
                    slWave.sendParticles(ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.8D, event.getEntity().getZ(), 3, 0.3D, 0.3D, 0.3D, 0.0D);
                    var nearby = slWave.getEntitiesOfClass(LivingEntity.class, event.getEntity().getBoundingBox().inflate(3.5D), e -> e != player && e != event.getEntity() && !(e instanceof Player));
                    for (var target : nearby) {
                        target.hurt(player.damageSources().playerAttack(player), event.getNewDamage() * 0.40F);
                    }
                }
            }
            // Hito 100: Trascendencia Divina (Daño Final x1.5)
            if (com.soybe.awakeningsystem.item.WeaponLevelManager.hasMilestone(weaponHeld, 100)) {
                event.setNewDamage(event.getNewDamage() * 1.5F);
            }
            // Otorgar EXP de arma en base al daño
            int weaponExpGain = Math.max(1, Math.min(100, (int) (event.getNewDamage() * 0.20F)));
            com.soybe.awakeningsystem.item.WeaponLevelManager.addExp(weaponHeld, weaponExpGain, player);
        }

        // === CURIO MANTO DE LA BELLEZA & FRAGMENTOS CÓSMICOS (HSR / PRIMOGEMCRAFT) ===
        double robeMult = com.soybe.awakeningsystem.item.RobeOfTheBeautyItem.getDamageMultiplier(player);
        if (robeMult > 1.0) {
            event.setNewDamage((float) (event.getNewDamage() * robeMult));
            if (player.level() instanceof ServerLevel sl && player.getRandom().nextFloat() < 0.40F) {
                sl.sendParticles(ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY() + 1.2D, event.getEntity().getZ(), 6, 0.2D, 0.3D, 0.2D, 0.05D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 8, 0.3D, 0.4D, 0.3D, 0.1D);
            }
        }

        // === BONIFICADORES DE ARTEFACTOS DEL MONARCA (GENSHIN ARTIFACTS) ===
        float artifactAtk = data.getArtifactBonusAttack();
        if (artifactAtk > 0) {
            event.setNewDamage(event.getNewDamage() + artifactAtk);
        }
        float critRate = data.getArtifactBonusCritRate();
        if (critRate > 0 && player.getRandom().nextFloat() * 100.0F < critRate) {
            float critMultiplier = 1.5F + (data.getArtifactBonusCritDmg() / 100.0F);
            event.setNewDamage(event.getNewDamage() * critMultiplier);
            if (player.level() instanceof ServerLevel slCrit) {
                slCrit.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 25, 0.5D, 0.7D, 0.5D, 0.2D);
                slCrit.sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 15, 0.4D, 0.5D, 0.4D, 0.1D);
            }
            player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.1F);
        }

        // === INFUSIÓN Y REACCIONES ELEMENTALES DE VISIONES DE TEYVAT ===
        ItemStack mainStack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();
        com.soybe.awakeningsystem.item.TeyvatVisionItem equippedVision = null;
        if (mainStack.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatVisionItem v) equippedVision = v;
        else if (offStack.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatVisionItem v) equippedVision = v;

        com.soybe.awakeningsystem.element.ElementType elem = null;
        if (equippedVision != null) {
            elem = com.soybe.awakeningsystem.element.ElementType.fromString(equippedVision.getElement().name);
        } else if (data.hasActiveVision()) {
            elem = com.soybe.awakeningsystem.element.ElementType.fromString(data.activeVision());
        }

        if (elem != null) {
            var reactionRes = com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(event.getEntity(), elem, player, event.getNewDamage());
            if (reactionRes.reacted()) {
                event.setNewDamage(event.getNewDamage() * reactionRes.damageMultiplier() + reactionRes.bonusDamage());
            }

            // Efectos elementales de contacto al golpear
            switch (elem) {
                case PYRO -> event.getEntity().setRemainingFireTicks(120);
                case CRYO -> {
                    event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                    event.getEntity().setTicksFrozen(120);
                }
                case ELECTRO -> {
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 12, 0.3D, 0.5D, 0.3D, 0.1D);
                    }
                }
                case HYDRO -> player.heal(Math.max(1.0F, event.getNewDamage() * 0.05F));
                case GEO -> player.setAbsorptionAmount(Math.min(20.0F, player.getAbsorptionAmount() + 2.0F));
                case ANEMO -> {
                    net.minecraft.world.phys.Vec3 dir = event.getEntity().position().subtract(player.position()).normalize().scale(0.8D);
                    event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().add(dir.x, 0.4D, dir.z));
                }
                case DENDRO -> event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
        }

        // === VULNERABILIDAD FÍSICA POR SUPERCONDUCTOR (-40% RESISTENCIA FÍSICA) ===
        if (event.getEntity().getPersistentData().getLong("superconduct_vuln_until") > event.getEntity().level().getGameTime()) {
            event.setNewDamage(event.getNewDamage() * 1.4F);
        }

        // === ROMPEHIELO (SHATTER) EN ENEMIGOS CONGELADOS ===
        if (com.soybe.awakeningsystem.element.ElementalReactionManager.checkShatter(event.getEntity(), player, event.getNewDamage())) {
            event.setNewDamage(event.getNewDamage() + 28.0F);
        }

        // === ABSORCIÓN DE DAÑO POR ESCUDO DE CRISTALIZACIÓN ===
        long shieldUntil = event.getEntity().getPersistentData().getLong("genshin_shield_until");
        if (shieldUntil > event.getEntity().level().getGameTime()) {
            float shieldAmt = event.getEntity().getPersistentData().getFloat("genshin_shield_amount");
            if (shieldAmt > 0) {
                float absorbed = Math.min(shieldAmt, event.getNewDamage());
                event.getEntity().getPersistentData().putFloat("genshin_shield_amount", shieldAmt - absorbed);
                event.setNewDamage(event.getNewDamage() - absorbed);
                if (event.getEntity().level() instanceof ServerLevel slShield) {
                    slShield.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 10, 0.3D, 0.4D, 0.3D, 0.1D);
                }
            }
        }
        if (data.hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE, 4)) {
            if (player.getMainHandItem().getItem() instanceof net.minecraft.world.item.SwordItem) {
                event.setNewDamage(event.getNewDamage() * 1.35F);
            }
        }
        if (data.hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH, 4)) {
            event.getEntity().setRemainingFireTicks(120);
            if (player.level() instanceof ServerLevel slFlame) {
                slFlame.sendParticles(ParticleTypes.FLAME, event.getEntity().getX(), event.getEntity().getY() + 0.5D, event.getEntity().getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);
            }
        }
        if (data.hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER, 4)) {
            if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, false));
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false));
            }
        }
        if (data.hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED, 4)) {
            float rawHeal = event.getNewDamage() * 0.10F;
            float maxHeal = Math.min(12.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
            player.heal(Math.min(rawHeal, maxHeal));
        }

        // Coordinación de Enjambre del Monarca: ordenar a las sombras cercanas enfocar el ataque en la víctima
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity victim && !victim.getTags().contains("shadow_soldier") && player.level() instanceof ServerLevel slVictim) {
            String mTag = "shadow_master_" + player.getUUID();
            var nearbyShadows = slVictim.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(64.0D),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(mTag));
            for (var shadow : nearbyShadows) {
                if (shadow.getTarget() != victim) {
                    shadow.setTarget(victim);
                }
            }
        }
        if (data.isInTrance() && player.level() instanceof ServerLevel serverLevel) {
            if (player.tickCount % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 0.3D, 0.4D, 0.3D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.2D, player.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.02D);
            }
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 0, false, false, false));
        }
        Integer skillLevel = data.isSkillEnabled("combat_instinct") ? data.skills().get("combat_instinct") : null;
        if (skillLevel != null) {
            boolean evolved = skillLevel >= 6;
            event.setNewDamage(event.getNewDamage() + skillLevel * (evolved ? 0.75F : 0.5F));
            if (evolved) player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, false, false, true));
            changed |= gainSkillProgress(player, data, "combat_instinct");
        }
        if (data.isInTrance()) {
            event.setNewDamage(event.getNewDamage() + 3.0F);
        }
        // Runa de Sed de Sangre (Bloodthirst): 15% robo de vida constante (con tope equilibrado)
        if (data.isSkillEnabled("bloodthirst")) {
            float rawHeal = event.getNewDamage() * 0.15F;
            float maxHeal = Math.min(14.0F, Math.max(2.0F, player.getMaxHealth() * 0.05F));
            player.heal(Math.min(rawHeal, maxHeal));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2D, player.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.05D);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 0.9F, 1.4F);
            }
        }

        // Mecánica Shonen: Destello Negro (Black Flash - Jujutsu Kaisen)
        // Ocurre cuando el impacto físico conecta en una distorsión espacial de 0.000001s durante un combo
        int comboCount = data.comboCount();
        double blackFlashChance = comboCount >= 5 ? 0.25D : (comboCount >= 2 ? 0.12D : 0.04D);
        if (player.getRandom().nextDouble() < blackFlashChance && player.level() instanceof ServerLevel slBf) {
            event.setNewDamage(event.getNewDamage() * 2.5F);
            var victim = event.getEntity();
            slBf.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.2D);
            slBf.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 35, 0.4D, 0.4D, 0.4D, 0.2D);
            slBf.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
            slBf.sendParticles(ParticleTypes.FLASH, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 1, 0, 0, 0, 0);
            slBf.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.2F);
            slBf.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 0.7F);
            player.displayClientMessage(Component.literal("§0§l[§4§l⚡ DESTELLO NEGRO ⚡§0§l] §c§l¡IMPACTO ESPACIAL x2.5!"), true);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, true));
        }

        // Bonificaciones de Afijos RPG en el arma del jugador
        ItemStack mainWeapon = player.getMainHandItem();
        if (RpgAffixManager.hasAffixes(mainWeapon)) {
            float bonusMult = RpgAffixManager.getDamageBonus(mainWeapon);
            if (bonusMult > 0.0F) {
                event.setNewDamage(event.getNewDamage() * (1.0F + bonusMult));
            }
            float lifesteal = RpgAffixManager.getLifesteal(mainWeapon);
            if (lifesteal > 0.0F) {
                float rawHeal = event.getNewDamage() * lifesteal;
                // Tope de curación: máximo 4% de la vida máxima del jugador o 12 HP por golpe
                float maxHeal = Math.min(12.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
                player.heal(Math.min(rawHeal, maxHeal));
            }
        }

        // Set Bonus Pasivo: Armadura de Adamantita (Furia Berserker: hasta +60% de daño a baja salud)
        if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.ADAMANTITE)) {
            float missingRatio = 1.0F - (player.getHealth() / player.getMaxHealth());
            if (missingRatio > 0.05F) {
                float bonusDmg = event.getNewDamage() * (missingRatio * 0.6F);
                event.setNewDamage(event.getNewDamage() + bonusDmg);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, event.getEntity().getX(), event.getEntity().getY() + 0.8D, event.getEntity().getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.LAVA, event.getEntity().getX(), event.getEntity().getY() + 0.5D, event.getEntity().getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
                }
            }
        }

        // Bonificaciones de Físicos Marciales de Murim
        float physDmgMult = data.getPhysiqueDamageMultiplier();
        if (physDmgMult > 0.0F) {
            event.setNewDamage(event.getNewDamage() * (1.0F + physDmgMult));
        }
        if ("heavenly_demon".equals(data.murimPhysique())) {
            float hpRatio = player.getHealth() / player.getMaxHealth();
            if (hpRatio <= 0.50F) {
                event.setNewDamage(event.getNewDamage() * 1.35F);
                player.heal(event.getNewDamage() * 0.20F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.02D);
                }
            }
        } else if ("pure_yang".equals(data.murimPhysique())) {
            event.getEntity().setRemainingFireTicks(60);
        } else if ("nine_yin".equals(data.murimPhysique())) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }

        // --- Pasivas de Árbol de Habilidades de Clase (Escaladas por Rango I, II y III) ---
        // Santo de la Espada: Despertar del Corazón de la Espada (+25% a +55% daño con espadas)
        if (data.isSkillEnabled("sword_heart_awakening") && player.getMainHandItem().getItem() instanceof net.minecraft.world.item.SwordItem) {
            int lvl = data.skills().getOrDefault("sword_heart_awakening", 1);
            float bonus = 1.25F + (lvl - 1) * 0.15F;
            event.setNewDamage(event.getNewDamage() * bonus);
            if (player.level() instanceof ServerLevel sl) {
                spawnShockwaveRing(sl, event.getEntity().getX(), event.getEntity().getY() + 0.3D, event.getEntity().getZ(), ParticleTypes.CHERRY_LEAVES, 1.8D, 14, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.7F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.1F, 1.6F);
            }
        }
        // Santo de la Espada: Intención de Espada Ilimitada (+30% a +60% penetración si HP >= 80%)
        if (data.isSkillEnabled("limitless_sword_intent") && player.getHealth() >= player.getMaxHealth() * 0.8F) {
            int lvl = data.skills().getOrDefault("limitless_sword_intent", 1);
            float bonus = 1.30F + (lvl - 1) * 0.15F;
            event.setNewDamage(event.getNewDamage() * bonus);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 10 * lvl, 0.3D, 0.3D, 0.3D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.1F, 1.8F);
            }
        }
        // Lector Omnisciente: Resumen de Personaje (Muestra HP y +20% a +44% daño)
        if (data.isSkillEnabled("character_summary_scan")) {
            int lvl = data.skills().getOrDefault("character_summary_scan", 1);
            float bonus = 1.20F + (lvl - 1) * 0.12F;
            event.setNewDamage(event.getNewDamage() * bonus);
            if (event.getEntity() instanceof LivingEntity targetMob) {
                float curHp = Math.max(0, targetMob.getHealth() - event.getNewDamage());
                player.displayClientMessage(Component.literal("§6[Lector Omnisciente " + ClassSkillTree.getRankStars(lvl) + "] §e" + targetMob.getName().getString() + " §7❤ §c" + String.format("%.1f", curHp) + "§7/§c" + String.format("%.1f", targetMob.getMaxHealth())), true);
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, event.getEntity().getX(), event.getEntity().getY() + 1.2D, event.getEntity().getZ(), 8, 0.3D, 0.3D, 0.3D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.7F);
            }
        }
        // Lector Omnisciente: Maestría de Marcadores (Probabilidad de buff aleatorio)
        if (data.isSkillEnabled("bookmark_mastery")) {
            int lvl = data.skills().getOrDefault("bookmark_mastery", 1);
            float chance = 0.25F + (lvl - 1) * 0.15F;
            if (player.getRandom().nextFloat() < chance) {
                int buff = player.getRandom().nextInt(3);
                int amp = lvl >= 3 ? 2 : 1;
                if (buff == 0) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100 + lvl * 30, amp));
                else if (buff == 1) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100 + lvl * 30, amp));
                else player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100 + lvl * 30, amp));
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 14, 0.4D, 0.5D, 0.4D, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.8F);
                }
            }
        }
        // Berserker: Frenesí Sanguinario (+5% por cada 10% vida perdida, y 10% a 20% lifesteal)
        if (data.isSkillEnabled("blood_frenzy_passive")) {
            int lvl = data.skills().getOrDefault("blood_frenzy_passive", 1);
            float missingHpRatio = 1.0F - (player.getHealth() / player.getMaxHealth());
            if (missingHpRatio > 0.1F) {
                event.setNewDamage(event.getNewDamage() * (1.0F + missingHpRatio * (0.5F + (lvl - 1) * 0.25F)));
            }
            float rawHeal = event.getNewDamage() * (0.10F + (lvl - 1) * 0.05F);
            float maxHeal = Math.min(14.0F, Math.max(2.0F, player.getMaxHealth() * 0.05F));
            player.heal(Math.min(rawHeal, maxHeal));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.1F, 1.6F);
            }
        }
        // Cultivador Demoníaco: Linaje de Asura (+30% a +60% daño demoníaco, Wither y robo de Qi)
        if (data.isSkillEnabled("asura_bloodline")) {
            int lvl = data.skills().getOrDefault("asura_bloodline", 1);
            event.setNewDamage(event.getNewDamage() * (1.30F + (lvl - 1) * 0.15F));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, 80 + lvl * 20, lvl >= 3 ? 2 : 1));
            data.addCultivationQi((int) Math.max(1, event.getNewDamage() * (0.1F + (lvl - 1) * 0.05F)));
            float rawHeal = event.getNewDamage() * (0.05F + (lvl - 1) * 0.03F);
            float maxHeal = Math.min(10.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
            player.heal(Math.min(rawHeal, maxHeal));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 1.4F);
            }
        }
        // Asesino Fantasma: Emboscada Fantasma (x2.0 a x2.8 daño si agachado o invisible)
        if (data.isSkillEnabled("phantom_ambush") && (player.isShiftKeyDown() || player.hasEffect(MobEffects.INVISIBILITY))) {
            int lvl = data.skills().getOrDefault("phantom_ambush", 1);
            event.setNewDamage(event.getNewDamage() * (2.0F + (lvl - 1) * 0.40F));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80 + lvl * 20, 0));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80 + lvl * 20, lvl >= 3 ? 3 : 2));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SQUID_INK, event.getEntity().getX(), event.getEntity().getY() + 0.8D, event.getEntity().getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 20 * lvl, 0.4D, 0.5D, 0.4D, 0.2D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.3F, 1.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.4F);
            }
        }
        // Asesino Fantasma: Marca de Muerte (Si objetivo < 30% HP, +50% a +100% daño de ejecución)
        if (data.isSkillEnabled("death_mark_execution") && event.getEntity().getHealth() <= event.getEntity().getMaxHealth() * 0.3F) {
            int lvl = data.skills().getOrDefault("death_mark_execution", 1);
            event.setNewDamage(event.getNewDamage() * (1.50F + (lvl - 1) * 0.25F));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SCULK_SOUL, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 15 * lvl, 0.3D, 0.5D, 0.3D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 10 * lvl, 0.3D, 0.4D, 0.3D, 0.15D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.1F, 1.9F);
            }
        }

        // Reliquia Pasiva: Collar del Asesino de Reyes (+25% daño a jefes o entidades >= 100 HP)
        if (com.soybe.awakeningsystem.item.ManhwaArtifactItem.hasArtifact(player, com.soybe.awakeningsystem.item.ManhwaArtifactItem.ArtifactType.KING_SLAYER_NECKLACE)) {
            LivingEntity victim = event.getEntity();
            boolean isBoss = victim.getMaxHealth() >= 100.0F
                    || victim instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                    || victim instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                    || victim instanceof com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity
                    || victim instanceof com.soybe.awakeningsystem.entity.IgrisBossEntity
                    || victim instanceof com.soybe.awakeningsystem.entity.BaranBossEntity;
            if (isBoss) {
                event.setNewDamage(event.getNewDamage() * 1.25F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.1D);
                }
            }
        }
        // Reliquia Pasiva: Anillo del Monarca Demonio Baran (+4 daño eléctrico y +2 EP)
        if (com.soybe.awakeningsystem.item.ManhwaArtifactItem.hasArtifact(player, com.soybe.awakeningsystem.item.ManhwaArtifactItem.ArtifactType.DEMON_MONARCH_RING)) {
            LivingEntity victim = event.getEntity();
            event.setNewDamage(event.getNewDamage() + 4.0F);
            data.restoreEnergy(2);
            changed = true;
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.1D);
            }
        }
        // Clase de Cazador: Santo de la Espada (Críticos potenciados con pétalos de ciruelo)
        if ("sword_saint".equals(data.hunterClass()) && (player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater())) {
            event.setNewDamage(event.getNewDamage() * 1.35F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 15, 0.4D, 0.5D, 0.4D, 0.1D);
            }
        }
        // Clase de Cazador: Berserker Colosal (Rompe escudos enemigos)
        if ("colossal_berserker".equals(data.hunterClass()) && event.getEntity().isBlocking() && event.getEntity() instanceof Player targetPlayer) {
            targetPlayer.disableShield();
            player.level().playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.2F, 0.9F);
        }
        if (data.isSkillEnabled("dragon_sense") && (player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater())) {
            event.setNewDamage(event.getNewDamage() * 1.35F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 0.8D, event.getEntity().getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
            }
        }

        // Bonificación de Daño de Qi Marcial (Cultivo Murim / Dantian)
        if (data.cultivationQi() >= 2) {
            data.consumeCultivationQi(2);
            changed = true;
            int realm = data.dantianRealm();
            int openMeridians = data.getOpenedMeridiansCount();
            float qiBonus = (realm + 1) * 2.5F + (openMeridians * 0.6F);

            // Meridiano 5 (Corazón): +35% daño en salto crítico
            if (data.isMeridianOpen(4) && player.fallDistance > 0.0F) {
                qiBonus *= 1.35F;
            }
            // Meridiano 6 (Intestino Delgado): Quemadura de Qi Fuego
            if (data.isMeridianOpen(5)) {
                event.getEntity().setRemainingFireTicks(60);
            }
            // Meridiano 12 (Hígado): Onda cortante de Qi
            if (data.isMeridianOpen(11)) {
                qiBonus += 4.0F;
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.8D, event.getEntity().getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            }

            event.setNewDamage(event.getNewDamage() + qiBonus);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0D, event.getEntity().getZ(), 6, 0.2D, 0.3D, 0.2D, 0.1D);
            }
        }
        applyCompatibilityDamageSkills(event, player, data);
        data.recordStatistic("damage_dealt", (int) Math.ceil(event.getNewDamage()));
        checkAchievements(player, data);
        if (event.getSource().getDirectEntity() instanceof AbstractArrow) {
            changed |= learnAndAnnounce(player, data, "ranged", 3, "deadeye") > 0;
            Integer deadeye = data.isSkillEnabled("deadeye") ? data.skills().get("deadeye") : null;
            if (deadeye != null) {
                boolean evolved = deadeye >= 6;
                event.setNewDamage(event.getNewDamage() + deadeye * (evolved ? 1.0F : 0.75F));
                changed |= gainSkillProgress(player, data, "deadeye");
            }
        }
        if (data.emergencyWounded() && player.getHealth() > player.getMaxHealth() * 0.5F) {
            if (data.confirmEmergencyRecovery()) {
                changed |= data.completeEmergency();
                if (data.emergencyComplete()) {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.emergency_complete.title",
                            "awakening_system.notification.emergency_complete.message", 0xFFFF7666);
                }
            }
            changed = true;
        }
        if (changed) AwakeningSystemMod.sync(player);
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Curaciones normales o pequeñas (pociones de regeneración, manzanas doradas, etc.)
            if (event.getAmount() <= 8.0F) return;

            // Si es una curación completa intencional como Píldora de la Tortuga Negra, Altar de la Diosa o Tótem
            if (Math.abs(event.getAmount() - player.getMaxHealth()) < 0.01F) return;

            // Si el jugador está en combate (atacó o fue atacado en los últimos 5 segundos = 100 ticks)
            boolean inCombat = (player.tickCount - player.getLastHurtMobTimestamp() < 100)
                            || (player.tickCount - player.getLastHurtByMobTimestamp() < 100);

            if (inCombat) {
                // Límite de curación instantánea en combate por golpe / tick:
                // Máximo 16 HP (8 corazones) o 8% de la salud máxima del jugador.
                // Esto previene que afijos desbalanceados de robo de vida (vampirismo / lifesteal) curen 80+ HP por golpe
                // volviendo al jugador inmortal contra jefes o mobs de 8,000 HP.
                float maxCombatHeal = Math.max(16.0F, player.getMaxHealth() * 0.08F);
                if (event.getAmount() > maxCombatHeal) {
                    event.setAmount(maxCombatHeal);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getNewDamage() > 0) {
            if (player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity seat) {
                double sx = seat.getX();
                double sy = seat.getY();
                double sz = seat.getZ();
                player.stopRiding();
                seat.discard();
                player.teleportTo(sx, sy, sz);
                player.displayClientMessage(Component.translatable("awakening_system.cultivation.interrupted"), true);
                player.playNotifySound(SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.9F, 0.9F);
            }
            SystemData data = data(player);
            boolean before = data.skills().containsKey("enduring_flesh");
            int gained = data.learn("survival", 2);
            data.recordStatistic("damage_taken", (int) Math.ceil(event.getNewDamage()));
            checkAchievements(player, data);
            Integer skillLevel = data.isSkillEnabled("enduring_flesh") ? data.skills().get("enduring_flesh") : null;
            boolean changed = gained > 0;
            data.resetCombo();
            if (player.getHealth() > 0 && player.getHealth() <= player.getMaxHealth() * 0.25F) {
                changed |= data.recordEmergencySurvival();
                triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.LOW_HEALTH);
            }
            if (skillLevel != null) {
                boolean evolved = skillLevel >= 6;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 + skillLevel * 10, evolved ? 1 : 0, false, false, true));
                if (evolved) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 + skillLevel * 10, 0, false, false, true));
                if (data.canAdvanceSkill("enduring_flesh") && data.gainSkillProgress("enduring_flesh", 1)) {
                    announceSkillLevel(player, "enduring_flesh", data.skills().get("enduring_flesh"));
                    changed = true;
                }
            }
            if (!before && data.skills().containsKey("enduring_flesh")) {
                announceSkill(player, "enduring_flesh");
                changed = true;
            }
            // Pasiva Defensiva de La Doncella (Forma Despierta: Corona de Espinas)
            ItemStack mainHold = player.getMainHandItem();
            ItemStack offHold = player.getOffhandItem();
            boolean hasTransformedDoncella = (mainHold.getItem() == SystemItems.LA_DONCELLA.get() && com.soybe.awakeningsystem.item.LaDoncellaSwordItem.isTransformed(mainHold))
                    || (offHold.getItem() == SystemItems.LA_DONCELLA.get() && com.soybe.awakeningsystem.item.LaDoncellaSwordItem.isTransformed(offHold));
            if (hasTransformedDoncella && event.getSource().getEntity() instanceof LivingEntity attackerMob && attackerMob != player) {
                attackerMob.hurt(player.damageSources().thorns(player), 32.0F);
                attackerMob.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
                if (player.level() instanceof ServerLevel sl) {
                    sl.playSound(null, attackerMob.getX(), attackerMob.getY(), attackerMob.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.3F, 0.7F);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, attackerMob.getX(), attackerMob.getY() + 1.0D, attackerMob.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
                }
            }

            if (changed) AwakeningSystemMod.sync(player);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() != attacker && event.getNewDamage() > 0) {
            SystemData attackerData = data(attacker);
            boolean tranceActivated = attackerData.addCombo();
            if (tranceActivated) {
                AwakeningSystemMod.notify(attacker, "awakening_system.notification.trance.title",
                        "awakening_system.notification.trance.message", 0xFFFF5555);
                attacker.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1.8F);
                triggerConstellationReaction(attacker, ConstellationDefinitions.ReactionType.COMBO_TRANCE);
            }
            if (attackerData.isInTrance() && attacker.level() instanceof ServerLevel serverLevel) {
                var aabb = event.getEntity().getBoundingBox().inflate(3.5D);
                var nearby = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != attacker && e != event.getEntity() && e.isAlive() && !e.getType().getCategory().isFriendly());
                float cleaveDamage = Math.max(1.0F, event.getNewDamage() * 0.5F);
                for (var target : nearby) {
                    target.hurt(attacker.damageSources().playerAttack(attacker), cleaveDamage);
                    serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8D, target.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.05D);
                }
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.5D, event.getEntity().getZ(), 1, 0, 0, 0, 0);
            }
            AwakeningSystemMod.sync(attacker);
        }
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SystemData data = data(player);
        data.recordStatistic("items_crafted", 1);
        checkAchievements(player, data);
        if (learnAndAnnounce(player, data, "crafting", 2, "artisan_hand") > 0) {
            long day = player.level().getDayTime() / 24000L;
            updateDailyProgress(player, data, day, 2, 1);
            if (data.completeDaily(player.level().getGameTime())) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.daily_complete.title",
                        "awakening_system.notification.daily_complete.message", 0xFF70E8FF);
            }
            Integer level = data.skills().get("artisan_hand");
            if (level != null) {
                awardExperience(player, data, level * (level >= 6 ? 2 : 1));
                gainSkillProgress(player, data, "artisan_hand");
            }
            AwakeningSystemMod.sync(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (DungeonManager.onRightClickChest(player, event.getPos())) {
                event.setCanceled(true);
                return;
            }
            if (handleAbyssPortalInteraction(player, event)) {
                event.setCanceled(true);
                return;
            }
            if (com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleKeyInteraction(player, event.getPos())) {
                event.setCanceled(true);
                return;
            }
            if (com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleChestInteraction(player, event.getPos())) {
                event.setCanceled(true);
                return;
            }
            handleSeriesStructureInteractions(player, event);
        }
    }

    private static boolean handleAbyssPortalInteraction(ServerPlayer player, PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        TeyvatWorldManager.AbyssPortalEntry portal = TeyvatWorldManager.findNearPortal(player.level().dimension(), pos, 14.0D);
        if (portal == null) return false;

        // Si el portal estaba bloqueado, activarlo automáticamente
        if (!portal.active) {
            portal.active = true;
            TeyvatWorldManager.setAbyssActive(player.getServer(), true);
            player.sendSystemMessage(Component.literal("§6§l✦ [ESPIRAL DEL ABISMO] §f¡Has roto el Sello Celestial! El vórtice hacia el Abismo se ha abierto."));
            event.getLevel().playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 1.0F);
            event.getLevel().playSound(null, pos, ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.BLOCKS, 1.5F, 0.9F);
        }

        // Abrir inmediatamente la pantalla de la Espiral del Abismo
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
        com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
        return true;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItemStack();
        if (SystemItems.DUNGEON_KEY != null && SystemItems.DUNGEON_KEY.isBound() && stack.is(SystemItems.DUNGEON_KEY.value())) {
            SystemData data = data(player);
            PacketDistributor.sendToPlayer(player, new OpenDungeonKeyPayload(data.level(), data.rank()));
            event.setCanceled(true);
            return;
        }
        if (SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound() && stack.is(SystemItems.HUNTER_GUIDE.value())) {
            PacketDistributor.sendToPlayer(player, new OpenGuidebookPayload());
            event.setCanceled(true);
            return;
        }
        if (SystemItems.HUNTER_BESTIARY != null && SystemItems.HUNTER_BESTIARY.isBound() && stack.is(SystemItems.HUNTER_BESTIARY.value())) {
            PacketDistributor.sendToPlayer(player, new OpenBestiaryPayload());
            event.setCanceled(true);
            return;
        }
        if (SystemItems.RANKER_TOWER_TICKET != null && SystemItems.RANKER_TOWER_TICKET.isBound() && stack.is(SystemItems.RANKER_TOWER_TICKET.value())) {
            TowerManager.enterTower(player);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            event.setCanceled(true);
            return;
        }
        if (SystemItems.PRIMOGEM != null && SystemItems.PRIMOGEM.isBound() && stack.is(SystemItems.PRIMOGEM.value())) {
            SystemData data = data(player);
            if (!data.isWelkinEnhanced() && data.welkinQuestStep() == 3) {
                if (player.getXRot() < -35.0F && (player.level().isNight() || player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD))) {
                    GachaManager.completeColumbinaAscension(player);
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (stack.is(Items.POTION)) {
            SystemData data = data(player);
            if (learnAndAnnounce(player, data, "alchemy", 3, "alchemist_eye") > 0) {
                Integer level = data.skills().get("alchemist_eye");
                if (level != null) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * level, level >= 6 ? 1 : 0, false, false, true));
                    gainSkillProgress(player, data, "alchemist_eye");
                }
                AwakeningSystemMod.sync(player);
            }
        }
    }

    public static void handleSelectDungeon(ServerPlayer player, String rank, String biome) {
        if (DungeonManager.isInDungeon(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.action.already_in_dungeon"), true);
            return;
        }

        // Si no está en creativo, consumir 1 llave del inventario
        if (!player.isCreative()) {
            boolean found = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (SystemItems.DUNGEON_KEY != null && SystemItems.DUNGEON_KEY.isBound() && stack.is(SystemItems.DUNGEON_KEY.value())) {
                    stack.shrink(1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                player.displayClientMessage(Component.translatable("awakening_system.action.need_key"), true);
                return;
            }
        }

        String r = (rank == null || rank.isEmpty()) ? "E" : rank;
        String b = (biome == null || biome.isEmpty()) ? "crypt" : biome;
        DungeonManager.spawnRiftNear(player, r, b);
        player.displayClientMessage(Component.literal("§a§l[SISTEMA] §bPortal Rango §e" + r + " §b(" + b.toUpperCase() + ") invocado."), true);
    }

    @SubscribeEvent
    public static void onAttackShadow(AttackEntityEvent event) {
        if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target && target.getTags().contains("shadow_soldier")) {
            if (target.getTags().contains("shadow_master_" + event.getEntity().getUUID())) {
                event.setCanceled(true);
            }
        }
        if (event.getEntity().getVehicle() instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity dragon) {
            if (event.getTarget() == dragon || (event.getTarget() instanceof com.soybe.awakeningsystem.entity.DragonBodyPart part && part.getParent() == dragon)) {
                event.setCanceled(true);
            }
            dragon.shootPlayerBreath(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        // Inmunidad total al daño por bloques de magma (HOT_FLOOR)
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
            if (event.getEntity() instanceof ServerPlayer
                    || event.getEntity().getTags().contains("shadow_soldier")
                    || event.getEntity() instanceof com.soybe.awakeningsystem.entity.IsekaiSlimeEntity
                    || event.getEntity().level().dimension().equals(DungeonManager.DUNGEON_REALM)
                    || event.getEntity().level().dimension().equals(TowerManager.RANKER_TOWER)
                    || event.getEntity().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
                event.setCanceled(true);
                return;
            }
        }

        // Prevención total de fuego amigo y daño accidental a soldados de sombra
        if (event.getEntity().getTags().contains("shadow_soldier")) {
            net.minecraft.world.entity.Entity srcEntity = event.getSource().getEntity();
            net.minecraft.world.entity.Entity directEntity = event.getSource().getDirectEntity();
            for (String tag : event.getEntity().getTags()) {
                if (tag.startsWith("shadow_master_")) {
                    String masterUuid = tag.substring("shadow_master_".length());
                    if ((srcEntity != null && srcEntity.getStringUUID().equals(masterUuid)) ||
                        (directEntity != null && directEntity.getStringUUID().equals(masterUuid))) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
            if ((srcEntity != null && srcEntity.getTags().contains("shadow_soldier")) ||
                (directEntity != null && directEntity.getTags().contains("shadow_soldier"))) {
                event.setCanceled(true);
                return;
            }
        }

        // Prevención si una sombra aliada intenta infligir daño a su amo y efectos de armaduras/clases defensivas
        if (event.getEntity() instanceof ServerPlayer sp) {
            net.minecraft.world.entity.Entity srcEntity = event.getSource().getEntity();
            net.minecraft.world.entity.Entity directEntity = event.getSource().getDirectEntity();
            String masterTag = "shadow_master_" + sp.getUUID();
            if ((srcEntity != null && srcEntity.getTags().contains(masterTag)) ||
                (directEntity != null && directEntity.getTags().contains(masterTag))) {
                event.setCanceled(true);
                return;
            }

            // Coordinación Defensiva del Monarca: las sombras cercanas defienden a su amo atacando al agresor
            if (srcEntity instanceof net.minecraft.world.entity.LivingEntity attacker && !attacker.getTags().contains("shadow_soldier") && sp.level() instanceof ServerLevel slAttacker) {
                var nearbyShadows = slAttacker.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(64.0D),
                        m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));
                for (var shadow : nearbyShadows) {
                    if (shadow.getTarget() != attacker) {
                        shadow.setTarget(attacker);
                    }
                }
            }

            SystemData spData = data(sp);
            long gameTime = sp.level().getGameTime();

            // Set Bonus Activo: Campana Dorada de Jade (Invulnerabilidad absoluta y reflejo)
            long bellUntil = sp.getPersistentData().getLong("spirit_jade_bell_active_until");
            if (gameTime < bellUntil) {
                event.setCanceled(true);
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.4F);
                if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != sp) {
                    attacker.hurt(sp.damageSources().playerAttack(sp), 15.0F);
                    attacker.knockback(1.5D, sp.getX() - attacker.getX(), sp.getZ() - attacker.getZ());
                }
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.1D);
                    sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.05D);
                }
                return;
            }

            // Set Bonus Pasivo: Armadura de Mithril (Inmunidad absoluta a daño de caída)
            if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(sp, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MITHRIL)) {
                if (event.getSource().is(DamageTypeTags.IS_FALL)) {
                    event.setCanceled(true);
                    return;
                }
            }

            // Set Bonus Pasivo: Armadura de Adamantita (Inmunidad absoluta a fuego y quemaduras)
            if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(sp, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.ADAMANTITE)) {
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                    return;
                }
            }

            // Set Bonus Pasivo: Armadura de Jade Espiritual (Transmutación de 25% de daño en Qi y Maná)
            if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(sp, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.SPIRIT_JADE)) {
                float absorbed = event.getAmount() * 0.25F;
                event.setAmount(Math.max(1.0F, event.getAmount() - absorbed));
                spData.addCultivationQi((int) Math.max(1, absorbed * 2));
                spData.restoreEnergy((int) Math.max(1, absorbed));
                sp.heal(absorbed * 0.4F);
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            }

            // Habilidad La Cuarta Pared (Absorción de Daño Fatal)
            boolean hasFourthWall = spData.isSkillEnabled("fourth_wall");
            if (hasFourthWall && event.getAmount() >= sp.getHealth()) {
                long cd = sp.getPersistentData().getLong("fourth_wall_fatal_cd");
                if (gameTime >= cd) {
                    sp.getPersistentData().putLong("fourth_wall_fatal_cd", gameTime + 6000L); // 5 min CD
                    event.setCanceled(true);
                    sp.setHealth(10.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
                    sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
                    sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
                    sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
                    if (sp.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.5D, 0.8D, 0.5D, 0.3D);
                        sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.6D, 0.6D, 0.6D, 0.2D);
                    }
                    AwakeningSystemMod.notify(sp, "awakening_system.notification.fourth_wall.title", "awakening_system.notification.fourth_wall.desc", 0xFF70E8FF);
                    return;
                }
            }

            // Clase de Cazador: Apóstol del Star Stream (Barrera de Éter al recibir golpe masivo)
            if ("star_apostle".equals(spData.hunterClass()) && event.getAmount() >= 12.0F) {
                long cd = sp.getPersistentData().getLong("star_apostle_barrier_cd");
                if (gameTime >= cd) {
                    sp.getPersistentData().putLong("star_apostle_barrier_cd", gameTime + 1800L); // 90s CD
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 3));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
                    sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.5F);
                    if (sp.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
                    }
                    AwakeningSystemMod.notify(sp, "awakening_system.notification.ether_barrier.title", "awakening_system.notification.ether_barrier.desc", 0xFFFFD700);
                }
            }

            // --- Pasivas Defensivas de Árbol de Clase ---
            // Monarca de las Sombras: Corazón Negro (Resurrección Definitiva)
            if (spData.isSkillEnabled("black_heart_resurrection") && event.getAmount() >= sp.getHealth()) {
                long cd = sp.getPersistentData().getLong("black_heart_fatal_cd");
                if (gameTime >= cd) {
                    sp.getPersistentData().putLong("black_heart_fatal_cd", gameTime + 6000L); // 5 min CD
                    event.setCanceled(true);
                    sp.setHealth(sp.getMaxHealth() * 0.5F);
                    spData.restoreMana(spData.maxMana());
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 3));
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 2));
                    if (sp.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 3.5D, 6.0D, 6, 18);
                        spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.SCULK_SOUL, 6.0D, 32, 0.12D);
                        sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 2, 0, 0, 0, 0);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.3F, 0.8F);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.2F, 0.6F);
                    }
                    AwakeningSystemMod.notify(sp, "awakening_system.notification.black_heart.title", "awakening_system.notification.black_heart.desc", 0xFF9966FF);
                    return;
                }
            }

            // Berserker: Furia Inmortal (Resurrección Sangrienta)
            if (spData.isSkillEnabled("immortal_berserk") && event.getAmount() >= sp.getHealth()) {
                long cd = sp.getPersistentData().getLong("immortal_berserk_fatal_cd");
                if (gameTime >= cd) {
                    sp.getPersistentData().putLong("immortal_berserk_fatal_cd", gameTime + 6000L); // 5 min CD
                    event.setCanceled(true);
                    sp.setHealth(1.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3));
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                    if (sp.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2D, sp.getZ(), ParticleTypes.LAVA, 6.0D, 32, 0.15D);
                        spawnVortexPillar(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.FLAME, 3.0D, 5.0D, 5, 16);
                        sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 2, 0, 0, 0, 0);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5F, 0.7F);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.1F);
                    }
                    AwakeningSystemMod.notify(sp, "awakening_system.notification.immortal_fury.title", "awakening_system.notification.immortal_fury.desc", 0xFFFF3333);
                    return;
                }
            }

            // Cultivador Demoníaco: Inversión de Qi
            if (spData.isSkillEnabled("qi_reversal_absorption")) {
                float absorbed = event.getAmount() * 0.25F;
                event.setAmount(Math.max(1.0F, event.getAmount() - absorbed));
                spData.addCultivationQi((int) Math.max(1, absorbed * 2));
                sp.heal(absorbed * 0.3F);
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                }
            }

            // Mago Supremo: Barrera de Escudo de Maná
            if (spData.isSkillEnabled("mana_shield_barrier") && spData.mana() > 10) {
                float absorbed = Math.min(event.getAmount() * 0.35F, spData.mana() / 4.0F);
                if (absorbed > 0.5F) {
                    event.setAmount(Math.max(0.5F, event.getAmount() - absorbed));
                    spData.spendMana((int) (absorbed * 4));
                    if (sp.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.ENCHANT, 2.2D, 3, 14);
                        sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 15, 0.4D, 0.5D, 0.4D, 0.05D);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.7F);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.1F, 1.5F);
                    }
                }
            }

            // Soberano Dragón: Endurecimiento de Escamas Dragón
            if (spData.isSkillEnabled("dragon_scales_hardening")) {
                event.setAmount(event.getAmount() * 0.70F);
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.8F);
                }
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFireOrBlockedDamage(LivingDamageEvent.Pre event) {
        // Prevención de fuego amigo para sombras
        if (event.getEntity().getTags().contains("shadow_soldier")) {
            if (event.getSource().getEntity() instanceof ServerPlayer sp && event.getEntity().getTags().contains("shadow_master_" + sp.getUUID())) {
                event.setNewDamage(0.0F);
                return;
            }
            if (event.getSource().getEntity() != null && event.getSource().getEntity().getTags().contains("shadow_soldier")) {
                event.setNewDamage(0.0F);
                return;
            }
        }
        if (event.getSource().getEntity() != null && event.getSource().getEntity().getTags().contains("shadow_soldier")) {
            if (event.getEntity() instanceof ServerPlayer sp && event.getSource().getEntity().getTags().contains("shadow_master_" + sp.getUUID())) {
                event.setNewDamage(0.0F);
                return;
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SystemData data = data(player);
        boolean changed = false;
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            changed |= learnAndAnnounce(player, data, "fire", 2, "fire_ward") > 0;
            Integer level = data.isSkillEnabled("fire_ward") ? data.skills().get("fire_ward") : null;
            if (level != null) {
                boolean evolved = level >= 6;
                event.setNewDamage(Math.max(0.0F, event.getNewDamage() - (evolved ? level + 2 : level)));
                if (evolved) player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
                changed |= gainSkillProgress(player, data, "fire_ward");
            }
        }
        if (player.isBlocking() && event.getNewDamage() > 0) {
            changed |= learnAndAnnounce(player, data, "protection", 2, "guardian_stance") > 0;
            Integer level = data.isSkillEnabled("guardian_stance") ? data.skills().get("guardian_stance") : null;
            if (level != null) {
                boolean evolved = level >= 6;
                event.setNewDamage(Math.max(0.0F, event.getNewDamage() - (level * 0.5F + (evolved ? 1.0F : 0.0F))));
                changed |= gainSkillProgress(player, data, "guardian_stance");
            }
        }
        if (data.isSkillEnabled("blue_dragon_scales")) {
            if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                event.setNewDamage(0.0F);
                player.clearFire();
            }
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) || event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                event.setNewDamage(event.getNewDamage() * 0.75F);
            }
        }
        if (data.isSkillEnabled("fourth_wall")) {
            float maxHealth = player.getMaxHealth();
            if (event.getNewDamage() > maxHealth * 0.4F) {
                event.setNewDamage(event.getNewDamage() * 0.5F);
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.END_ROD, 2.5D, 20, 0.08D);
                    sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.3F, 1.5F);
                }
            }
        }
        if (data.isSkillEnabled("iron_will") && player.getHealth() <= player.getMaxHealth() * 0.3F && player.level().getGameTime() >= data.ironWillCooldownUntil()) {
            data.setIronWillCooldownUntil(player.level().getGameTime() + 45L * 20L);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true));
            if (player.level() instanceof ServerLevel sl) {
                spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.8D, 4, 18);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.3F, 1.3F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.2F, 1.1F);
            }
            AwakeningSystemMod.notify(player, "awakening_system.notification.iron_will.title", "awakening_system.notification.iron_will.message", 0xFFFFD866);
            changed = true;
        }
        if (changed) AwakeningSystemMod.sync(player);
    }

    @SubscribeEvent
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob && !mob.level().isClientSide()) {
            if (mob.tickCount % 20 == 0) {
                com.soybe.awakeningsystem.world.WorldBossScalingManager.handleBossLeash(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        tickPlayerCharging(player);

        if (ConstellationChannelHandler.isAstralWatching(player.getUUID()) && player.isShiftKeyDown()) {
            ConstellationChannelHandler.returnFromAstral(player);
        }

        if (com.soybe.awakeningsystem.block.SubwayGrabHandleBlock.GRABBED_PLAYERS.containsKey(player.getUUID())) {
            BlockPos handlePos = com.soybe.awakeningsystem.block.SubwayGrabHandleBlock.GRABBED_PLAYERS.get(player.getUUID());
            if (player.isShiftKeyDown() || player.distanceToSqr(handlePos.getX() + 0.5, player.getY(), handlePos.getZ() + 0.5) > 6.0D || !(player.level().getBlockState(handlePos).getBlock() instanceof com.soybe.awakeningsystem.block.SubwayGrabHandleBlock)) {
                com.soybe.awakeningsystem.block.SubwayGrabHandleBlock.GRABBED_PLAYERS.remove(player.getUUID());
                player.displayClientMessage(Component.literal("§7[Metro] Has soltado el pasamanos."), true);
            } else {
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                player.fallDistance = 0.0F;
            }
        }

        // Deduplicación continua de la Guía del Cazador en el inventario del jugador
        if (player.tickCount % 60 == 0 && SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound()) {
            int gCount = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack it = player.getInventory().getItem(i);
                if (it.is(SystemItems.HUNTER_GUIDE.value())) {
                    gCount++;
                    if (gCount > 1) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    } else if (it.getCount() > 1) {
                        it.setCount(1);
                    }
                }
            }
        }

        SystemData data = data(player);
        boolean changed = false;

        if (player.tickCount % 2 == 0) {
            TeyvatWorldManager.checkPlayerAbyssPortal(player);
        }
        if (player.tickCount % 5 == 0) {
            TeyvatWorldManager.checkPlayerAnemoUpdraft(player);
        }
        tickPlayerVision(player, data);

        // Sincronizar bono de salud máxima de artefactos de Teyvat (Flor de la Vida)
        if (player.tickCount % 20 == 0 && player.isAlive()) {
            float bonusHp = data.getArtifactBonusHp();
            var maxHpAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (maxHpAttr != null) {
                var modifierId = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "artifact_hp_bonus");
                var existing = maxHpAttr.getModifier(modifierId);
                if (bonusHp > 0) {
                    if (existing == null || existing.amount() != bonusHp) {
                        maxHpAttr.removeModifier(modifierId);
                        maxHpAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                modifierId, bonusHp, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                        ));
                    }
                } else if (existing != null) {
                    maxHpAttr.removeModifier(modifierId);
                }
            }
        }

        // Chequeo y apertura automática de la Bendición Lunar cada nuevo día del juego
        if (player.tickCount % 20 == 0 && player.isAlive() && !player.isSpectator()) {
            if (data.welkinDays() > 0) {
                long currentDay = player.serverLevel().getDayTime() / 24000L;
                if (currentDay > data.lastWelkinClaimDay()) {
                    Long lastOffered = WELKIN_OFFERED_DAYS.get(player.getUUID());
                    if (lastOffered == null || lastOffered < currentDay) {
                        WELKIN_OFFERED_DAYS.put(player.getUUID(), currentDay);
                        int reward = data.isWelkinEnhanced() ? 135 : 90;
                        PacketDistributor.sendToPlayer(player, new OpenWelkinScreenPayload(
                                data.welkinDays(),
                                reward,
                                true,
                                data.isWelkinEnhanced(),
                                data.welkinQuestStep(),
                                data.welkinQuestProgress()
                        ));
                    }
                }
            }
        }

        // Tick combat combo y modo trance
        boolean inTranceBefore = data.isInTrance();
        int comboBefore = data.comboCount();
        data.tickCombat();
        if (inTranceBefore != data.isInTrance() || (comboBefore > 0 && data.comboCount() == 0)) {
            changed = true;
        }

        // Tick de enfriamiento de habilidad de Físico Marcial y Técnicas del Dao
        if (data.getPhysiqueSkillCooldown() > 0) {
            data.tickPhysiqueSkillCooldown();
        }
        if (data.daoSkillCooldown() > 0) {
            data.tickDaoSkillCooldown();
        }
        if (data.tribulationShieldTicks() > 0) {
            data.tickTribulationShield();
        }

        // Manejo de la Tribulación Celestial de Cultivo Murim (Heavenly Tribulation 雷劫)
        if (data.isUnderTribulation()) {
            if (player.isDeadOrDying()) {
                data.clearTribulation();
                changed = true;
            } else {
                data.tickTribulation();
                ServerLevel sl = player.serverLevel();
                int remaining = data.getTribulationStrikesRemaining();
                int total = data.getTribulationTotalStrikes();
                int currentStrike = total - remaining + 1;
                int targetRealm = data.getPendingTribulationRealm();

                // 1. Advertencia previa de estruendo 12 ticks antes de la caída del rayo
                if (data.getTribulationTicks() == 12 && remaining > 0) {
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.2F, 1.8F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY() + 1.2D, player.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.08D);
                    player.displayClientMessage(Component.literal("§c§l⚡ [TRIBULACIÓN CELESTIAL] §e¡El cielo ruge con ira divina! Rayo #" + currentStrike + "/" + total + "..."), true);
                }

                // 2. Impacto fulminante del Rayo Celestial del Dao
                if (data.getTribulationTicks() <= 0 && remaining > 0) {
                    // Mantener tormenta durante la tribulación
                    sl.setWeatherParameters(0, 400, true, true);

                    // Invocar rayo celestial visual sin destruir bloques
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(player.position());
                        bolt.setVisualOnly(true);
                        sl.addFreshEntity(bolt);
                    }

                    // Sonidos en capas: Trueno cósmico del Dao + estruendo grave + explosión + campana sagrada
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CELESTIAL_TRIBULATION.value(), SoundSource.WEATHER, 3.5F, 1.0F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.5F, 0.65F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.8F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4F, 0.6F);

                    // Partículas celestiales divinas en onda expansiva y pilar
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 3.8D, 32, 0.16D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.2D, 4.5D, 4, 14);
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2D, player.getZ(), 16, 0.6D, 0.2D, 0.6D, 0.02D);
                    if (targetRealm >= 6) {
                        sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.2D, player.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.06D);
                    }

                    // Breve ceguera sensorial por el destello cósmico
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 15, 0, false, false, false));

                    // Cálculo de daño de Qi de la tribulación escalado hasta Reino 9
                    float baseDmg = switch (targetRealm) {
                        case 2 -> 6.0F;  // Cimientos (3 rayos)
                        case 3 -> 9.0F;  // Núcleo Dorado (6 rayos)
                        case 4 -> 13.0F; // Alma Naciente (9 rayos)
                        case 5 -> 17.0F; // Transformación Alma (12 rayos)
                        case 6 -> 22.0F; // Refinamiento Vacío (15 rayos)
                        case 7 -> 28.0F; // Integración Dao (18 rayos)
                        case 8 -> 35.0F; // Gran Mahayana (27 rayos)
                        case 9 -> 44.0F; // Ascensión Inmortal (36 rayos)
                        default -> 5.0F;
                    };

                    // Reducción por físico marcial, postura de cultivo y Píldora de Escudo de Tribulación
                    String phys = data.murimPhysique();
                    float reduction = 1.0F;
                    if ("celestial_dragon".equals(phys) || "pure_yang".equals(phys) || "primordial_chaos".equals(phys)) {
                        reduction -= 0.30F;
                    }
                    if (player.isShiftKeyDown() || player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity) {
                        reduction -= 0.20F;
                    }
                    boolean hasTribShield = data.tribulationShieldTicks() > 0;
                    if (hasTribShield) {
                        reduction -= 0.70F; // Píldora de Protección absorbe el 70% del impacto
                    }
                    float finalDmg = Math.max(1.0F, baseDmg * Math.max(0.1F, reduction));

                    // Protección sagrada contra muerte por tribulación si tiene la píldora activa
                    if (hasTribShield && finalDmg >= player.getHealth()) {
                        finalDmg = Math.max(0.5F, player.getHealth() - 1.0F); // Deja con 1 HP salvavidas
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.6D, 0.6D, 0.15D);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                        player.displayClientMessage(Component.literal("§6§l✦ [ESCUDO CELESTIAL] §f¡La Píldora dispersó el rayo fatal del Dao!"), true);
                    }

                    player.hurt(player.damageSources().lightningBolt(), finalDmg);
                    player.sendSystemMessage(Component.translatable("message.awakening_system.tribulation_strike", currentStrike, total, finalDmg));

                    String shieldFeedback = hasTribShield ? " §b[ESCUDO -70%]" : (reduction < 1.0F ? " §a[POSTURA/FÍSICO]" : "");
                    player.displayClientMessage(Component.literal("§c§l⚡ [RAYO CELESTIAL " + currentStrike + "/" + total + "] §fImpacto: §c-" + String.format(java.util.Locale.US, "%.1f", finalDmg) + " HP" + shieldFeedback), true);

                    data.decrementTribulationStrikes();
                    if (data.getTribulationStrikesRemaining() > 0) {
                        data.resetTribulationTicks(25);
                    } else {
                        // ¡Tribulación Celestial conquistada con éxito!
                        data.clearTribulation();
                        data.setDantianRealm(targetRealm);
                        data.setCultivationSubLevel(1);
                        data.setCultivationQi(0);

                        // Cielos despejados al triunfar sobre el Dao
                        sl.setWeatherParameters(6000, 0, false, false);

                        player.removeAllEffects();
                        player.setHealth(player.getMaxHealth());
                        data.restoreEnergy(data.maxEnergy());

                        // Bendición de Iluminación Celestial tras sobrevivir
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));

                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 2.5F, 1.0F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);

                        spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.5D);
                        spawnMurimCircleGround(sl, player, 7.5F, 120, 0xFFFFD700);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 8, 0, 0, 0, 0);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 140, 1.8D, 1.8D, 1.8D, 0.35D);
                        sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 90, 1.5D, 1.8D, 1.5D, 0.1D);
                        sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.5D, 1.4D, 1.5D, 0.1D);

                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                                Component.translatable("title.awakening_system.ascent_title")));
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                                Component.translatable("title.awakening_system.ascent_subtitle", getDantianRealmComponent(targetRealm))));
                        player.sendSystemMessage(Component.translatable("message.awakening_system.tribulation_success", getDantianRealmComponent(targetRealm)));

                        if (targetRealm >= 7) {
                            Component broadMsg = Component.literal("§6§l☯ [ASCENSIÓN DEL DAO] §f¡El cultivador §e" + player.getScoreboardName() + " §fha superado la Gran Calamidad de los Rayos Celestes y ha ascendido a " + getDantianRealmComponent(targetRealm).getString() + "§f! ☯");
                            for (ServerPlayer otherP : sl.getServer().getPlayerList().getPlayers()) {
                                otherP.sendSystemMessage(broadMsg);
                                otherP.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                            }
                        }
                    }
                    changed = true;
                }
            }
        }

        if (player.tickCount % 20 == 0 && !data.hasFable("first_netherite")) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack st = player.getInventory().getItem(i);
                if (!st.isEmpty() && (st.is(net.minecraft.world.item.Items.NETHERITE_INGOT)
                        || st.is(net.minecraft.world.item.Items.NETHERITE_SWORD)
                        || st.is(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE)
                        || st.is(net.minecraft.world.item.Items.NETHERITE_PICKAXE))) {
                    grantFable(player, "first_netherite");
                    break;
                }
            }
        }

        // Aura visual continua de Soldados de las Sombras: Glow azul eléctrico, ojos cian y humo oscuro
        if (player.tickCount % 8 == 0) {
            ServerLevel pLevel = player.serverLevel();
            String masterTag = "shadow_master_" + player.getUUID();
            var soldiers = pLevel.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(28.0D),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));
            for (var soldier : soldiers) {
                if (!soldier.hasEffect(MobEffects.GLOWING)) {
                    soldier.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));
                }
                try {
                    net.minecraft.world.scores.Scoreboard sb = pLevel.getScoreboard();
                    net.minecraft.world.scores.PlayerTeam team = sb.getPlayerTeam("shadows");
                    if (team == null) {
                        team = sb.addPlayerTeam("shadows");
                        team.setColor(ChatFormatting.AQUA);
                        team.setDisplayName(Component.literal("Sombras"));
                    }
                    String member = soldier.getScoreboardName();
                    if (!team.getPlayers().contains(member)) {
                        sb.addPlayerToTeam(member, team);
                    }
                } catch (Exception ignored) {}

                // Si está en fase de emergencia saliendo del suelo (primeros 60 ticks)
                if (soldier.tickCount <= 60 && soldier.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    pLevel.sendParticles(ParticleTypes.SQUID_INK, soldier.getX(), soldier.getY() + 0.05D, soldier.getZ(), 8, 0.4D, 0.02D, 0.4D, 0.01D);
                    pLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.05D, soldier.getZ(), 6, 0.35D, 0.02D, 0.35D, 0.01D);
                } else {
                    // Ojos eléctricos cian de fuego de alma
                    pLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getEyeY() - 0.05D, soldier.getZ(), 2, 0.08D, 0.04D, 0.08D, 0.005D);
                    // Vaho espectral azul sobre el torso
                    pLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + soldier.getBbHeight() * 0.45D, soldier.getZ(), 1, 0.15D, 0.2D, 0.15D, 0.01D);
                    // Humo sombrío en los pies
                    pLevel.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.15D, soldier.getZ(), 2, 0.15D, 0.1D, 0.15D, 0.01D);
                }
            }
        }

        // Actualización del Sistema 2.0 (5 minutos de bloqueo con temporizador regresivo)
        if (data.isSystemUpdating()) {
            data.decrementSystemUpdateTicks();
            changed = true;
            if (data.systemUpdateRemainingTicks() <= 0) {
                IsekaiWorldManager.completeSystemUpdate(player, data);
            }
        }

        // Actualización del Sistema 3.0 (5 minutos de ascensión celestial al Reino Murim)
        if (data.isSystem30Updating()) {
            data.decrementSystem30UpdateTicks();
            changed = true;
            if (data.system30UpdateRemainingTicks() <= 0) {
                MurimWorldManager.completeSystem30Update(player, data);
            }
        }

        // Actualización del Sistema 4.0 (5 minutos de sintonía con las líneas ley celestiales de Teyvat)
        if (data.isSystem40Updating()) {
            data.decrementSystem40UpdateTicks();
            changed = true;
            if (data.system40UpdateRemainingTicks() <= 0) {
                TeyvatWorldManager.completeSystem40Update(player, data);
            }
        }

        // Sincronizar puertas dimensionales
        if (player.tickCount % 40 == 0) {
            ServerLevel slevel = player.serverLevel();
            if (slevel.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) || slevel.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
                DungeonManager.syncRiftsToPlayer(player);
            }
        }

        // Ascensión al Gran Árbol de Yggdrasil y Portal Secreto a Murim en la Cúspide
        if (player.serverLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) && player.tickCount % 4 == 0) {
            BlockPos gatePos = YggdrasilTreeGenerator.ASCENSION_GATE_POS; // (0, 291, 6)
            double distGateSq = player.distanceToSqr(gatePos.getX() + 0.5D, gatePos.getY() + 0.5D, gatePos.getZ() + 0.5D);

            // Anuncio por primera vez al descubrir la Puerta de los Nueve Cielos
            if (distGateSq <= 80.0D && !player.getPersistentData().getBoolean("awakening_discovered_yggdrasil_gate")) {
                player.getPersistentData().putBoolean("awakening_discovered_yggdrasil_gate", true);
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                        Component.literal("§6§l✦ ¡DESCUBRIMIENTO HISTÓRICO! ✦")
                ));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        Component.literal("§bHas alcanzado la Puerta de los Nueve Cielos en la cima de Yggdrasil")
                ));
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, gatePos.getX() + 0.5D, gatePos.getY() + 2.0D, gatePos.getZ() + 0.5D, 80, 2.0D, 1.5D, 2.0D, 0.3D);
                player.serverLevel().sendParticles(ParticleTypes.END_ROD, gatePos.getX() + 0.5D, gatePos.getY() + 2.0D, gatePos.getZ() + 0.5D, 40, 1.5D, 1.5D, 1.5D, 0.05D);
            }

            // 1. Portal Visible Secreto: solo teletransporta a Murim al entrar físicamente al arco
            if (distGateSq <= 3.5D && !player.isOnPortalCooldown()) {
                player.setPortalCooldown();
                MurimWorldManager.teleportToMurim(player);
            } else if (player.getServer() != null) {
                // 2. Pabellón de Ascensión terrestre: transporta a la CÚSPIDE REAL (sobre todas las hojas), no directo al portal
                var sData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(player.getServer());
                for (var entry : sData.getAll().entrySet()) {
                    if (entry.getKey().startsWith("ascension_pavilion")) {
                        BlockPos pavPos = entry.getValue().above(1);
                        if (player.distanceToSqr(pavPos.getX() + 0.5D, pavPos.getY() + 1.0D, pavPos.getZ() + 0.5D) <= 6.5D) {
                            BlockPos summit = YggdrasilTreeGenerator.APEX_SUMMIT_POS; // (0, 304, 0)
                            player.teleportTo(player.serverLevel(), summit.getX() + 0.5D, (double) summit.getY() + 1.0D, summit.getZ() + 0.5D, 0.0F, 0.0F);
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 400, 0, false, false));
                            player.serverLevel().sendParticles(ParticleTypes.FLASH, summit.getX() + 0.5D, (double) summit.getY() + 1.5D, summit.getZ() + 0.5D, 10, 0, 0, 0, 0);
                            player.serverLevel().sendParticles(ParticleTypes.END_ROD, summit.getX() + 0.5D, (double) summit.getY() + 1.0D, summit.getZ() + 0.5D, 50, 1.2D, 1.2D, 1.2D, 0.05D);
                            player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.0F);
                            player.displayClientMessage(Component.translatable("awakening_system.action.yggdrasil_summit"), true);
                            break;
                        }
                    }
                }
            }
        }

        // Portal de Retorno del Gran Árbol del Mundo en la Dimensión Isekai al Overworld (en las raíces de Yggdrasil)
        if (player.serverLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) && player.tickCount % 4 == 0) {
            BlockPos gatePos = YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS;
            double gateY = (double) gatePos.getY() + 1.0D;
            double distSq = player.distanceToSqr(gatePos.getX() + 0.5D, gateY, gatePos.getZ() + 0.5D);
            if (distSq <= 6.5D) {
                MinecraftServer srv = player.getServer();
                if (srv != null) {
                    ServerLevel overworld = srv.getLevel(net.minecraft.world.level.Level.OVERWORLD);
                    if (overworld != null) {
                        BlockPos respawn = player.getRespawnPosition();
                        if (respawn == null) respawn = overworld.getSharedSpawnPos();
                        player.teleportTo(overworld, respawn.getX() + 0.5D, respawn.getY() + 1.0D, respawn.getZ() + 0.5D, player.getYRot(), 0.0F);
                        overworld.sendParticles(ParticleTypes.PORTAL, respawn.getX() + 0.5D, respawn.getY() + 1.5D, respawn.getZ() + 0.5D, 60, 1.0D, 1.0D, 1.0D, 0.1D);
                        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                        player.displayClientMessage(Component.translatable("awakening_system.tower.returned_to_overworld"), true);
                    }
                }
            }
        }

        // Portal de Retorno en la Gran Pagoda Flotante de Murim (0, SECT_ISLAND_Y + 2, 14)
        if (player.serverLevel().dimension().equals(MurimWorldManager.MURIM_WORLD) && player.tickCount % 4 == 0) {
            double returnY = (double) (MurimWorldManager.SECT_ISLAND_Y + 2);
            double distSq = player.distanceToSqr(0.5D, returnY, 14.0D);
            if (distSq <= 5.0D && !player.isOnPortalCooldown()) {
                MinecraftServer srv = player.getServer();
                if (srv != null) {
                    ServerLevel isekai = srv.getLevel(IsekaiWorldManager.ISEKAI_WORLD);
                    if (isekai == null) isekai = srv.overworld();
                    player.setPortalCooldown();
                    BlockPos target = YggdrasilTreeGenerator.ASCENSION_GATE_POS.offset(0, 0, 2);
                    player.teleportTo(isekai, target.getX() + 0.5D, (double) target.getY(), target.getZ() + 0.5D, 180.0F, 0.0F);
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 200, 0, false, false));
                    player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                    player.displayClientMessage(Component.translatable("awakening_system.murim.returned_to_isekai"), true);
                }
            }
        }

        // Manantial de Maná Sagrado de la Diosa (Purificación y Regeneración al estar en el agua sagrada)
        if (player.tickCount % 20 == 0 && (player.isInWater() || player.isInWaterOrRain())) {
            ServerLevel pLevel = player.serverLevel();
            BlockPos pPos = player.blockPosition();
            boolean nearGoddessAltar = false;
            for (BlockPos testPos : BlockPos.betweenClosed(pPos.offset(-7, -2, -7), pPos.offset(7, 2, 7))) {
                if (com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.isBound()
                        && pLevel.getBlockState(testPos).is(com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.get())) {
                    nearGoddessAltar = true;
                    break;
                }
            }
            if (nearGoddessAltar) {
                // Purga de aflicciones y estados negativos
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WITHER);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.WEAKNESS);
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.BLINDNESS);

                // Regeneración de vida y maná / energía sagrada
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.5F);
                }
                if (data.restoreEnergy(3)) {
                    changed = true;
                }

                pLevel.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.01D);
                pLevel.sendParticles(ParticleTypes.BUBBLE_POP, player.getX(), player.getY() + 0.3D, player.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }

        // Aguas Termales de Qi del Valle de las Bestias Espirituales (Curación y Vaho Termal)
        if (player.tickCount % 20 == 0 && (player.isInWater() || player.isInWaterOrRain())
                && player.serverLevel().dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            if (player.getServer() != null) {
                var sData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(player.getServer());
                for (var entry : sData.getAll().entrySet()) {
                    if (entry.getKey().startsWith("spirit_valley")) {
                        BlockPos vPos = entry.getValue();
                        if (player.distanceToSqr(vPos.getX() + 0.5D, vPos.getY() + 1.0D, vPos.getZ() + 0.5D) <= 16.0D * 16.0D) {
                            if (player.getHealth() < player.getMaxHealth()) {
                                player.heal(1.0F);
                            }
                            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.02D);
                            player.serverLevel().sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2D, player.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.01D);
                            break;
                        }
                    }
                }
            }
        }

        // Regeneración pasiva en Zonas de Alto Qi (Murim / Urðr)
        if (player.tickCount % 60 == 0 && data.dantianRealm() > 0) {
            float zoneDensity = getZoneQiDensity(player);
            if (zoneDensity >= 1.5F) {
                int passiveQi = Math.max(1, (int) Math.round(zoneDensity * data.getPhysiqueQiRegenMultiplier()));
                data.addCultivationQi(passiveQi);
                changed = true;
            }
        }

        int regenTicks = SystemConfig.ENERGY_REGEN_TICKS.get();
        int regenAmount = data.energyRegenRate();
        if (player.tickCount % Math.max(10, regenTicks) == 0) {
            if (data.restoreEnergy(regenAmount)) changed = true;
        }

        // Regeneración pasiva continua con Cáliz de Maná de Asrahan (1 EP cada 2 segundos)
        if (player.tickCount % 40 == 0) {
            if (com.soybe.awakeningsystem.item.ManhwaArtifactItem.hasArtifact(player, com.soybe.awakeningsystem.item.ManhwaArtifactItem.ArtifactType.ASRAHAN_MANA_CHALICE)) {
                if (data.restoreEnergy(1)) changed = true;
            }
        }

        // Meditación de Loto y Cultivo de Qi (Sentado en MeditationSeatEntity o agachado)
        boolean isLotusMounted = player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity;
        boolean isSneakMeditating = !isLotusMounted && player.isShiftKeyDown() && player.getDeltaMovement().lengthSqr() < 0.005D;

        if (isLotusMounted || isSneakMeditating) {
            int medTicks = player.getPersistentData().getInt("murim_meditation_ticks") + 1;
            player.getPersistentData().putInt("murim_meditation_ticks", medTicks);

            if ((isLotusMounted && medTicks % 20 == 0) || (!isLotusMounted && medTicks >= 60 && medTicks % 40 == 0)) {
                int realm = data.dantianRealm();
                int subLevel = data.cultivationSubLevel();
                int openMeridians = data.getOpenedMeridiansCount();
                int qiGain = 2 + realm * 3 + (subLevel - 1) + (openMeridians / 2);
                if (isLotusMounted) qiGain += 2; // Bonificación de postura de loto
                if ("ranker_of_the_tower".equals(data.equippedTitle()) || "high_ranker_of_the_tower".equals(data.equippedTitle())) {
                    qiGain = (int) Math.round(qiGain * 1.5D);
                }
                if (data.systemUpdated()) {
                    qiGain = (int) Math.round(qiGain * 1.5D);
                }
                if (data.system30Updated()) {
                    qiGain = (int) Math.round(qiGain * 1.8D);
                }
                if (player.serverLevel().dimension().equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
                    double subMult = 1.0 + data.subspaceLevel() * 0.25; // Nivel 1: +25%, Nivel 5: +125% Qi en Reino Interior
                    qiGain = (int) Math.round(qiGain * subMult);
                }
                float zoneDensity = getZoneQiDensity(player);
                qiGain = Math.max(1, (int) Math.round(qiGain * zoneDensity * data.getPhysiqueQiRegenMultiplier()));
                data.addCultivationQi(qiGain);

                float healGain = 1.0F + realm * 0.5F;
                if (data.isMeridianOpen(2)) healGain *= 1.5F; // Meridiano 3 (Estómago): +50% curación
                player.heal(healGain);
                changed = true;

                // Avance automático de sub-nivel si se alcanza la capacidad máxima y aún no llega a etapa 9
                if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                    data.setCultivationSubLevel(subLevel + 1);
                    data.setCultivationQi(0);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    player.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), getDantianRealmComponent(realm)), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cultivation.action_bar",
                            data.cultivationQi(), data.maxCultivationQi(), qiGain, getDantianRealmComponent(realm), data.cultivationSubLevel()), true);
                }

                if (player.level() instanceof ServerLevel sl) {
                    double angle = (medTicks * 0.1D);
                    double rx = Math.cos(angle) * 0.85D;
                    double rz = Math.sin(angle) * 0.85D;
                    double particleY = isLotusMounted ? (player.getY() + 0.82D) : (player.getY() + 0.25D);
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX() + rx, particleY, player.getZ() + rz, 2, 0, 0.04D, 0, 0.01D);
                    sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), particleY + 0.35D, player.getZ(), 4, 0.35D, 0.3D, 0.35D, 0.05D);
                    if (medTicks % 60 == 0) {
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.3F);
                    }
                    if (medTicks % 200 == 0) {
                        spawnMurimCircleGround(sl, player, 2.8F, 220, 0xFF50FA7B);
                    }
                }
            }
        } else {
            player.getPersistentData().putInt("murim_meditation_ticks", 0);
        }

        // Aura Visible de Qi según el Reino de Dantian (Reinos 1 a 9)
        if (data.dantianRealm() > 0 && player.level() instanceof ServerLevel slAura) {
            renderCultivationQiAura(player, data, slAura, isLotusMounted || isSneakMeditating);
        }

        // Set Bonus: Armadura de Mithril (Gracia Élfica: +25% velocidad pasiva y caída suave al agacharse en el aire)
        if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MITHRIL)) {
            if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || (player.getEffect(MobEffects.MOVEMENT_SPEED) != null && player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() < 10)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false, false));
            }
            if (player.isShiftKeyDown() && !player.onGround() && player.getDeltaMovement().y < -0.02D) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 15, 0, false, false, false));
                player.resetFallDistance();
                if (player.tickCount % 4 == 0 && player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1D, player.getZ(), 1, 0.1D, 0.05D, 0.1D, 0.01D);
                }
            }
        }

        // Set Bonus: Armadura de Adamantita (Furia Volcánica: inmunidad a fuego y nado en lava con regeneración)
        if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.ADAMANTITE)) {
            player.clearFire();
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE) || (player.getEffect(MobEffects.FIRE_RESISTANCE) != null && player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() < 10)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));
            }
            if (player.isInLava()) {
                if (player.tickCount % 20 == 0) {
                    player.heal(1.0F);
                }
                player.setDeltaMovement(player.getDeltaMovement().scale(1.12D));
                if (player.tickCount % 6 == 0 && player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3D, player.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.02D);
                }
            }
        }

        // Set Bonus: Armadura de Jade Espiritual (Cuerpo Inmortal de Qi: purificación y aura de la Campana Dorada)
        if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.SPIRIT_JADE)) {
            if (player.tickCount % 20 == 0) {
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WITHER);
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.BLINDNESS);
            }
            long bellUntil = player.getPersistentData().getLong("spirit_jade_bell_active_until");
            if (player.level().getGameTime() < bellUntil && player.level() instanceof ServerLevel sl) {
                double angle = player.tickCount * 0.35D;
                for (int i = 0; i < 4; i++) {
                    double a = angle + i * (Math.PI / 2.0D);
                    double bx = Math.cos(a) * 1.25D;
                    double bz = Math.sin(a) * 1.25D;
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX() + bx, player.getY() + 0.6D, player.getZ() + bz, 1, 0, 0.02D, 0, 0);
                    sl.sendParticles(ParticleTypes.GLOW, player.getX() + bx, player.getY() + 1.2D, player.getZ() + bz, 1, 0, 0.02D, 0, 0);
                }
            }
        }

        // Set Bonus: Armadura Astral (Soberanía del Dragón Celestial: Vuelo cósmico libre y rescate del vacío)
        boolean hasAstralSet = com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.ASTRAL);
        if (hasAstralSet) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
                player.getPersistentData().putBoolean("had_astral_flight", true);
            }
            if (player.getAbilities().flying) {
                player.resetFallDistance();
                if (player.tickCount % 3 == 0 && player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.2D, 0.05D, 0.2D, 0.02D);
                    sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.2D, 0.05D, 0.2D, -0.05D);
                }
            }
            // Rescate milagroso del vacío
            if (player.getY() < -50.0D) {
                int highestY = player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) player.getX(), (int) player.getZ());
                double targetY = Math.max(80.0D, (double) highestY + 2.0D);
                player.teleportTo(player.serverLevel(), player.getX(), targetY, player.getZ(), player.getYRot(), player.getXRot());
                player.setDeltaMovement(0, 0.2D, 0);
                player.resetFallDistance();
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.2F);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.5F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 5, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.1D);
                }
                AwakeningSystemMod.notify(player, "awakening_system.notification.astral_rescue.title", "awakening_system.notification.astral_rescue.desc", 0xFFFF55FF);
            }
        } else if (player.getPersistentData().getBoolean("had_astral_flight")) {
            player.getPersistentData().remove("had_astral_flight");
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false, false));
            }
        }

        // Dokkaebi Forced Survival Scenario Tick
        if (data.isDokkaebiScenarioActive()) {
            data.tickDokkaebiTimer();
            changed = true;
            if (data.dokkaebiScenarioTimer() <= 0) {
                if (data.dokkaebiScenarioKills() >= data.dokkaebiScenarioTargetKills()) {
                    int extraKills = Math.max(0, data.dokkaebiScenarioKills() - data.dokkaebiScenarioTargetKills());
                    int bonusCoins = extraKills * 50;
                    int bonusExp = extraKills * 80;
                    int totalCoins = 150 + bonusCoins;
                    int totalExp = 500 + bonusExp;
                    data.addCoins(totalCoins);
                    data.addExperience(totalExp);
                    if (extraKills > 0) {
                        AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_overkill.title",
                                "awakening_system.notification.dokkaebi_overkill.desc", 0xFFFFD700, extraKills, totalCoins, totalExp);
                        player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                    } else {
                        AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_success.title",
                                "awakening_system.notification.dokkaebi_success.desc", 0xFFFFD700);
                        player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                    }
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.5D, player.getZ(), 40 + extraKills * 5, 0.6D, 0.8D, 0.6D, 0.15D);
                    }
                } else {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_failed.title",
                            "awakening_system.notification.dokkaebi_failed.desc", 0xFFFF5555);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
                    player.hurt(player.damageSources().lightningBolt(), 6.0F);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.2D);
                    }
                }
                data.stopDokkaebiScenario();
            }
        } else if (player.tickCount % 24000 == 0 && data.level() >= 15 && !DungeonManager.isInDungeon(player.getUUID())) {
            if (player.getRandom().nextFloat() < 0.35F) {
                int target = 8 + player.getRandom().nextInt(5);
                data.startDokkaebiScenario("Supervivencia y Cacería de Emergencia", 3600, target);
                AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_scenario_start.title",
                        "awakening_system.notification.dokkaebi_scenario_start.desc", 0xFFFF4444, target);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5F, 0.8F);
                changed = true;
            }
        }

        if (player.tickCount % 20 != 0) {
            if (changed) AwakeningSystemMod.sync(player);
            return;
        }
        teleportDistantShadowSoldiers(player);
        tickStructureAmbientEffects(player);

        // Aparición periódica del Dokkaebi (cada 15-20 minutos de juego activo)
        if (player.tickCount > 0 && player.tickCount % 18000 == 0) {
            spawnDokkaebiNearPlayer(player);
        }

        // Seguimiento de racha de combo para misiones del Dokkaebi
        if (data.comboCount() >= 6) {
            AABB searchBox = player.getBoundingBox().inflate(32.0D);
            List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = player.serverLevel().getEntitiesOfClass(
                    com.soybe.awakeningsystem.entity.DokkaebiEntity.class, searchBox,
                    d -> "combo_strike".equals(d.getQuestType()) && d.isQuestActive());
            for (var d : dokkaebis) {
                d.addProgress(6, player);
            }
        }
        if (player.tickCount % 600 == 0) {
            DungeonManager.checkSpawnWorldRift(player);
        }
        if (data.isSkillEnabled("fourth_wall")) {
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
        }
        if (data.isSkillEnabled("estate_workmanship")) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, false, false, false));
        }
        if (data.isSkillEnabled("nano_healing") && player.tickCount % 40 == 0 && (player.tickCount - player.getLastHurtByMobTimestamp() > 80) && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 0.8D, player.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.05D);
            }
        }
        if (data.isSkillEnabled("dragon_sense") && (player.level().getRawBrightness(player.blockPosition(), 0) < 8 || player.getY() < 40 || DungeonManager.isInDungeon(player.getUUID()))) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false, false));
        }
        if (player.getY() < 32) {
            if (data.isSkillEnabled("deep_dark_resonance")) {
                int level = data.skills().get("deep_dark_resonance");
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, false, false, true));
                if (level >= 6) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, true));
                changed |= gainSkillProgress(player, data, "deep_dark_resonance");
            }
            changed |= updateDailyProgress(player, data, player.level().getDayTime() / 24000L, 4, 1);
            if (data.completeDaily(player.level().getGameTime())) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.daily_complete.title",
                        "awakening_system.notification.daily_complete.message", 0xFF70E8FF);
                changed = true;
            }
            boolean before = data.skills().containsKey("deep_breath");
            int gained = data.learn("depth", 1);
            changed |= gained > 0;
            Integer skillLevel = data.isSkillEnabled("deep_breath") ? data.skills().get("deep_breath") : null;
            if (skillLevel != null && player.isUnderWater()) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false, true));
                if (skillLevel >= 6) player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false, true));
                if (data.canAdvanceSkill("deep_breath") && data.gainSkillProgress("deep_breath", 1)) {
                    announceSkillLevel(player, "deep_breath", data.skills().get("deep_breath"));
                    changed = true;
                }
            }
            if (!before && data.skills().containsKey("deep_breath")) {
                announceSkill(player, "deep_breath");
                changed = true;
            }
        } else {
            data.resetRepetition("depth");
        }
        if (player.isSprinting()) {
            data.recordStatistic("distance_moved", 1);
            changed |= updateDailyProgress(player, data, player.level().getDayTime() / 24000L, 3, 1);
            if (data.completeDaily(player.level().getGameTime())) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.daily_complete.title",
                        "awakening_system.notification.daily_complete.message", 0xFF70E8FF);
                changed = true;
            }
            if (data.isSkillEnabled("aether_ascension")) {
                int level = data.skills().get("aether_ascension");
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, level >= 6 ? 1 : 0, false, false, true));
                changed |= gainSkillProgress(player, data, "aether_ascension");
            }
            boolean before = data.skills().containsKey("windrunner");
            int gained = data.learn("mobility", 1);
            changed |= gained > 0;
            Integer skillLevel = data.isSkillEnabled("windrunner") ? data.skills().get("windrunner") : null;
            if (skillLevel != null) {
                boolean evolved = skillLevel >= 6;
                if (evolved) player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, false, false, true));
                if (data.canAdvanceSkill("windrunner") && data.gainSkillProgress("windrunner", 1)) {
                    announceSkillLevel(player, "windrunner", data.skills().get("windrunner"));
                    changed = true;
                }
            }
            if (!before && data.skills().containsKey("windrunner")) {
                announceSkill(player, "windrunner");
                changed = true;
            }
        } else {
            data.resetRepetition("mobility");
        }
        if (data.emergencyWounded() && player.getHealth() > player.getMaxHealth() * 0.5F) {
            if (data.confirmEmergencyRecovery()) {
                changed |= data.completeEmergency();
                if (data.emergencyComplete()) {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.emergency_complete.title",
                            "awakening_system.notification.emergency_complete.message", 0xFFFF7666);
                }
            }
            changed = true;
        }
        if (changed) AwakeningSystemMod.sync(player);
    }

    public static boolean evolveSkill(ServerPlayer player, String skillId) {
        if (!SkillDefinitions.isAvailable(skillId)) return false;
        SystemData data = data(player);
        if (!data.evolveSkill(skillId, player.level().getGameTime())) return false;
        SkillDefinitions.Definition skill = SkillDefinitions.get(skillId);
        if (skill != null) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.evolved.title",
                    "awakening_system.notification.evolved.message", 0xFFFFD866, skill.nameKey());
        }
        if (data.completeEvolutionMission()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.evolution_complete.title",
                    "awakening_system.notification.evolution_complete_narrative.message", 0xFFFFD866);
        }
        completeClassMission(player, data);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void grantAll(ServerPlayer player) {
        SystemData data = data(player);
        data.grantAll();
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
    }

    public static boolean buyShopItem(ServerPlayer player, String entryId) {
        if ("system_roulette".equals(entryId)) {
            SystemData data = data(player);
            int cost = 1000;
            if (!data.spendCoins(cost)) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.shop.error_title",
                        "awakening_system.notification.shop.insufficient", 0xFFFF7666);
                return false;
            }
            data.recordPurchase(cost);
            checkAchievements(player, data);
            BoxLootResult lootResult = giveRouletteLoot(player, data);
            spawnBoxFx(player, 5);
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.title",
                    "awakening_system.notification.box_opened_summary", 0xFFFFD700, lootResult.summary());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new ChestRewardPayload(
                    entryId, 5, lootResult.summary(), lootResult.coins(), lootResult.itemIds(), lootResult.itemCounts()
            ));
            AwakeningSystemMod.sync(player);
            return true;
        }

        ShopDefinitions.Entry entry = ShopDefinitions.get(entryId);
        if (entry == null || !ShopDefinitions.isAvailable(entry)) return false;
        SystemData data = data(player);
        if (!ShopDefinitions.isUnlocked(entry, data)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.error_title",
                    "awakening_system.notification.shop.dimension_locked", 0xFFFF7666);
            return false;
        }
        if (!data.spendCoins(entry.cost())) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.error_title",
                    "awakening_system.notification.shop.insufficient", 0xFFFF7666);
            return false;
        }
        if ("attribute_reset".equals(entryId)) {
            if (!data.resetAttributes()) {
                data.addCoins(entry.cost());
                AwakeningSystemMod.notify(player, "awakening_system.notification.shop.error_title",
                        "awakening_system.notification.reset_failed", 0xFFFF7666);
                return false;
            }
            SystemAttributeEffects.apply(player);
            data.recordPurchase(entry.cost());
            checkAchievements(player, data);
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.title",
                    "awakening_system.notification.reset_success", 0xFF50FA7B);
            AwakeningSystemMod.sync(player);
            return true;
        }
        if ("energy_elixir".equals(entryId)) {
            data.restoreEnergy(50);
            data.recordPurchase(entry.cost());
            checkAchievements(player, data);
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.title",
                    "awakening_system.notification.energy_restored", 0xFF50FA7B);
            AwakeningSystemMod.sync(player);
            return true;
        }
        if ("box_common".equals(entryId) || "box_rare".equals(entryId) || "box_epic".equals(entryId)
                || "box_legendary".equals(entryId) || "lucky_box".equals(entryId)) {
            data.recordPurchase(entry.cost());
            checkAchievements(player, data);
            int tier = switch (entryId) {
                case "box_rare" -> 2;
                case "box_epic" -> 3;
                case "box_legendary" -> 4;
                default -> 1;
            };
            BoxLootResult lootResult;
            if ("lucky_box".equals(entryId)) {
                lootResult = giveRandomLoot(player, data);
            } else {
                lootResult = giveBoxLoot(player, data, tier);
            }
            spawnBoxFx(player, tier);
            int notifyColor = switch (tier) {
                case 2 -> 0xFF55AAFF;
                case 3 -> 0xFFD055FF;
                case 4 -> 0xFFFFAA00;
                case 5 -> 0xFFFFD700;
                default -> "lucky_box".equals(entryId) ? 0xFFFFD866 : 0xFFE0E0E0;
            };
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.title",
                    "awakening_system.notification.box_opened_summary", notifyColor, lootResult.summary());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new ChestRewardPayload(
                    entryId, tier, lootResult.summary(), lootResult.coins(), lootResult.itemIds(), lootResult.itemCounts()
            ));
            AwakeningSystemMod.sync(player);
            return true;
        }
        net.minecraft.world.item.ItemStack stack = ShopDefinitions.createStack(entry);
        if (stack.isEmpty()) {
            data.addCoins(entry.cost());
            AwakeningSystemMod.notify(player, "awakening_system.notification.shop.error_title",
                    "awakening_system.notification.shop.unavailable", 0xFFFF7666);
            AwakeningSystemMod.sync(player);
            return false;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.containerMenu.broadcastChanges();
        data.recordPurchase(entry.cost());
        checkAchievements(player, data);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shop.title",
                "awakening_system.notification.shop.message", 0xFF70E8FF, entry.nameKey());
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean sellShopItem(ServerPlayer player, String entryId, boolean sellAll) {
        SellDefinitions.SellEntry entry = SellDefinitions.get(entryId);
        if (entry == null || !SellDefinitions.isAvailable(entry)) return false;

        net.minecraft.resources.ResourceLocation itemRl = net.minecraft.resources.ResourceLocation.parse(entry.itemId());
        if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(itemRl)) return false;
        var targetItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemRl);

        int totalCount = 0;
        var inventory = player.getInventory();
        for (net.minecraft.world.item.ItemStack stack : inventory.items) {
            if (!stack.isEmpty() && stack.is(targetItem)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount < entry.amount()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.sell.error_title",
                    "awakening_system.notification.sell.insufficient_items", 0xFFFF7666);
            return false;
        }

        int toSell = sellAll ? (totalCount / entry.amount()) * entry.amount() : entry.amount();
        if (toSell <= 0) return false;

        int remainingToRemove = toSell;
        for (int i = 0; i < inventory.items.size() && remainingToRemove > 0; i++) {
            net.minecraft.world.item.ItemStack stack = inventory.items.get(i);
            if (!stack.isEmpty() && stack.is(targetItem)) {
                int take = Math.min(stack.getCount(), remainingToRemove);
                stack.shrink(take);
                remainingToRemove -= take;
                if (stack.isEmpty()) {
                    inventory.items.set(i, net.minecraft.world.item.ItemStack.EMPTY);
                }
            }
        }
        player.containerMenu.broadcastChanges();

        int earnedCoins = (toSell / entry.amount()) * entry.unitPrice();
        SystemData data = data(player);
        data.addCoins(earnedCoins);
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);

        AwakeningSystemMod.notify(player, "awakening_system.notification.sell.title",
                "awakening_system.notification.sell.success", 0xFF50FA7B,
                toSell + "x " + net.minecraft.network.chat.Component.translatable(entry.nameKey()).getString() + " (+" + earnedCoins + " ⛃)");
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean handleBankTransaction(ServerPlayer player, String action, int amount) {
        if (player == null) return false;
        SystemData data = data(player);
        var coinItem = SystemItems.AWAKENING_COIN.get();
        var inventory = player.getInventory();

        if ("deposit".equalsIgnoreCase(action)) {
            int totalPhysical = 0;
            for (net.minecraft.world.item.ItemStack stack : inventory.items) {
                if (!stack.isEmpty() && stack.is(coinItem)) {
                    totalPhysical += stack.getCount();
                }
            }

            if (totalPhysical <= 0) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.no_coins", 0xFFFF7666);
                return false;
            }

            int toDeposit = (amount <= 0) ? totalPhysical : Math.min(amount, totalPhysical);
            int remaining = toDeposit;
            for (int i = 0; i < inventory.items.size() && remaining > 0; i++) {
                net.minecraft.world.item.ItemStack stack = inventory.items.get(i);
                if (!stack.isEmpty() && stack.is(coinItem)) {
                    int take = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                    if (stack.isEmpty()) {
                        inventory.items.set(i, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
            }

            data.addCoins(toDeposit);
            AwakeningSystemMod.sync(player);
            player.playNotifySound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.4F);
            AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title", "awakening_system.notification.bank.deposited", 0xFF50FA7B, toDeposit, data.coins());
            return true;
        } else if ("withdraw".equalsIgnoreCase(action)) {
            if (amount <= 0) return false;
            if (data.coins() < amount) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.insufficient", 0xFFFF7666, amount);
                return false;
            }

            if (!data.spendCoins(amount)) return false;

            int toGive = amount;
            while (toGive > 0) {
                int batch = Math.min(toGive, 64);
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(coinItem, batch);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                toGive -= batch;
            }

            AwakeningSystemMod.sync(player);
            player.playNotifySound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.3F);
            AwakeningSystemMod.notify(player, "awakening_system.notification.bank.title", "awakening_system.notification.bank.withdrawn", 0xFFFFD866, amount);
            return true;
        }
        return false;
    }

    private static void spawnBoxFx(ServerPlayer player, int tier) {
        if (player.level() instanceof ServerLevel serverLevel) {
            net.minecraft.core.particles.SimpleParticleType particle = switch (tier) {
                case 1 -> ParticleTypes.CRIT;
                case 2 -> ParticleTypes.ENCHANT;
                case 3 -> ParticleTypes.WITCH;
                case 4 -> ParticleTypes.TOTEM_OF_UNDYING;
                default -> ParticleTypes.TOTEM_OF_UNDYING;
            };
            serverLevel.sendParticles(particle, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 0.4D, 0.5D, 0.4D, 0.2D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    tier >= 3 ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.9F, 1.0F + tier * 0.1F);
        }
    }

    public record BoxLootResult(String summary, int coins, List<String> itemIds, List<Integer> itemCounts) {}

    private static void addLootItem(ServerPlayer player, List<String> ids, List<Integer> counts, net.minecraft.world.item.Item item, int count) {
        player.getInventory().add(new net.minecraft.world.item.ItemStack(item, count));
        ids.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        counts.add(count);
    }

    private static BoxLootResult giveRouletteLoot(ServerPlayer player, SystemData data) {
        int roll = player.getRandom().nextInt(100);
        List<String> ids = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        int bonusCoins = (roll % 3 == 0) ? 100 : (roll % 2 == 0 ? 50 : 0);
        if (bonusCoins > 0) {
            data.addCoins(bonusCoins);
        }

        // Premios de la Ruleta del Sistema (acordes a los 8 pods de la rueda)
        if (roll < 22) {
            addLootItem(player, ids, counts, Items.ENDER_PEARL, 8);
            return new BoxLootResult("Perlas de Ender x8", bonusCoins, ids, counts);
        } else if (roll < 42) {
            addLootItem(player, ids, counts, Items.GOLDEN_APPLE, 3);
            return new BoxLootResult("Manzanas Doradas x3", bonusCoins, ids, counts);
        } else if (roll < 58) {
            addLootItem(player, ids, counts, Items.NETHERITE_SCRAP, 2);
            return new BoxLootResult("Fragmentos de Netherite x2", bonusCoins, ids, counts);
        } else if (roll < 72) {
            addLootItem(player, ids, counts, Items.DIAMOND_BLOCK, 1);
            return new BoxLootResult("Bloque de Diamante x1", bonusCoins, ids, counts);
        } else if (roll < 83) {
            addLootItem(player, ids, counts, Items.NETHER_STAR, 1);
            return new BoxLootResult("Estrella del Nether x1", bonusCoins, ids, counts);
        } else if (roll < 92) {
            addLootItem(player, ids, counts, Items.ENCHANTED_GOLDEN_APPLE, 1);
            return new BoxLootResult("Manzana Dorada Encantada x1", bonusCoins, ids, counts);
        } else if (roll < 97) {
            addLootItem(player, ids, counts, Items.TOTEM_OF_UNDYING, 1);
            return new BoxLootResult("Tótem de Inmortalidad x1", bonusCoins, ids, counts);
        } else {
            addLootItem(player, ids, counts, Items.NETHERITE_INGOT, 2);
            return new BoxLootResult("Lingotes de Netherite x2", bonusCoins, ids, counts);
        }
    }

    private static BoxLootResult giveBoxLoot(ServerPlayer player, SystemData data, int tier) {
        String boxId = switch (tier) {
            case 2 -> "box_rare";
            case 3 -> "box_epic";
            case 4 -> "box_legendary";
            default -> "box_common";
        };
        return com.soybe.awakeningsystem.config.CustomLootConfigManager.rollBoxLoot(boxId, player, player.getRandom());
    }

    private static BoxLootResult giveRandomLoot(ServerPlayer player, SystemData data) {
        return com.soybe.awakeningsystem.config.CustomLootConfigManager.rollBoxLoot("lucky_box", player, player.getRandom());
    }

    public static boolean equipTitle(ServerPlayer player, String titleId) {
        SystemData data = data(player);
        if (titleId == null || !TitleDefinitions.isAvailable(titleId)) return false;

        MinecraftServer server = player.getServer();
        if (server != null) {
            UniqueRankManager uniqueMgr = UniqueRankManager.get(server);
            String previousTitle = data.equippedTitle();

            if (UniqueRankManager.isUniqueTitle(titleId)) {
                if (!uniqueMgr.canClaimTitle(titleId, player.getUUID())) {
                    String holder = uniqueMgr.getTitleHolderName(titleId);
                    AwakeningSystemMod.notify(player, "awakening_system.notification.unique_locked.title",
                            "awakening_system.notification.unique_title_claimed", 0xFFFF4444, holder != null ? holder : "Otro cazador");
                    return false;
                }
                if (previousTitle != null && UniqueRankManager.isUniqueTitle(previousTitle) && !previousTitle.equals(titleId)) {
                    uniqueMgr.relinquishTitle(previousTitle, player.getUUID());
                }
                uniqueMgr.claimTitle(titleId, player);
                AwakeningSystemMod.notify(player, "awakening_system.notification.unique_claimed.title",
                        "awakening_system.notification.unique_title_success", 0xFFFFD700);
            } else if (previousTitle != null && UniqueRankManager.isUniqueTitle(previousTitle)) {
                uniqueMgr.relinquishTitle(previousTitle, player.getUUID());
            }
        }

        if (!data.equipTitle(titleId)) return false;
        SystemAttributeEffects.apply(player);
        return true;
    }

    public static boolean equipSkill(ServerPlayer player, String skillId) {
        SystemData data = data(player);
        MinecraftServer server = player.getServer();
        if (server != null) {
            UniqueRankManager uniqueMgr = UniqueRankManager.get(server);
            String previousSkill = data.equippedSkill();

            if (UniqueRankManager.isUniqueSkill(skillId)) {
                if (!uniqueMgr.canClaimSkill(skillId, player.getUUID())) {
                    String holder = uniqueMgr.getSkillHolderName(skillId);
                    AwakeningSystemMod.notify(player, "awakening_system.notification.unique_locked.title",
                            "awakening_system.notification.unique_skill_claimed", 0xFFFF4444, holder != null ? holder : "Otro cazador");
                    return false;
                }
                if (previousSkill != null && UniqueRankManager.isUniqueSkill(previousSkill) && !previousSkill.equals(skillId)) {
                    uniqueMgr.relinquishSkill(previousSkill, player.getUUID());
                }
                uniqueMgr.claimSkill(skillId, player);
                AwakeningSystemMod.notify(player, "awakening_system.notification.unique_claimed.title",
                        "awakening_system.notification.unique_skill_success", 0xFFFFD700);
            } else if (previousSkill != null && UniqueRankManager.isUniqueSkill(previousSkill)) {
                uniqueMgr.relinquishSkill(previousSkill, player.getUUID());
            }
        }

        if (data.setEquippedSkill(skillId)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_equipped.title",
                    "awakening_system.notification.skill_equipped.message", 0xFF70E8FF,
                    SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).nameKey() : skillId);
            AwakeningSystemMod.sync(player);
            return true;
        }
        return false;
    }

    public static boolean spendAttribute(ServerPlayer player, String attribute) {
        boolean spent = data(player).addAttribute(attribute);
        if (spent) {
            double oldMaxHealth = player.getMaxHealth();
            SystemAttributeEffects.apply(player);
            if ("vitality".equalsIgnoreCase(attribute) || "vitalidad".equalsIgnoreCase(attribute)) {
                player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + player.getMaxHealth() - oldMaxHealth));
            }
            AwakeningSystemMod.sync(player);
        }
        return spent;
    }

    private static void applyCompatibilityCombatSkills(ServerPlayer player, SystemData data) {
        if (data.skills().containsKey("beast_bond")) {
            int level = data.skills().get("beast_bond");
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, Math.min(1, level / 4), false, false, true));
            gainSkillProgress(player, data, "beast_bond");
        }
        if (data.skills().containsKey("cataclysm_breaker")
                && player.level().dimension().location().getNamespace().equals("cataclysm")) {
            int level = data.skills().get("cataclysm_breaker");
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, Math.min(1, level / 4), false, false, true));
            gainSkillProgress(player, data, "cataclysm_breaker");
        }
    }

    private static void applyCompatibilityDamageSkills(LivingDamageEvent.Pre event, ServerPlayer player, SystemData data) {
        if (data.skills().containsKey("cataclysm_breaker")
                && event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("cataclysm")) {
            int level = data.skills().get("cataclysm_breaker");
            event.setNewDamage(event.getNewDamage() + level * 0.5F);
            gainSkillProgress(player, data, "cataclysm_breaker");
        }
        if (data.skills().containsKey("eclipse_edge") || data.skills().containsKey("eclipse_core")) {
            int level = Math.max(data.skills().getOrDefault("eclipse_edge", 0), data.skills().getOrDefault("eclipse_core", 0));
            if (event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse_expansion")
                    || event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse")) {
                event.setNewDamage(event.getNewDamage() + level * 0.5F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, Math.min(2, level / 3), false, false, true));
                gainSkillProgress(player, data, data.skills().containsKey("eclipse_core") ? "eclipse_core" : "eclipse_edge");
            }
        }
    }

    public static boolean activateSkillSlot(ServerPlayer player, int slot) {
        SystemData data = data(player);
        if (data.isAnySystemUpdating()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
            player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
            return false;
        }
        String skill = data.getEquippedActiveSkill(slot);
        if (skill == null || skill.isEmpty()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.slot_empty_hint", slot + 1), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
            return false;
        }
        return activateSkill(player, skill);
    }

    public static boolean activateWeaponSkill(ServerPlayer player, int handOrdinal) {
        if (player == null) return false;
        net.minecraft.world.InteractionHand hand = handOrdinal == 1 ? net.minecraft.world.InteractionHand.OFF_HAND : net.minecraft.world.InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            net.minecraft.world.InteractionHand otherHand = hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.InteractionHand.OFF_HAND : net.minecraft.world.InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(otherStack)) {
                hand = otherHand;
                stack = otherStack;
            }
        }
        if (com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            stack.use(player.level(), player, hand);
            player.swing(hand, true);
            return true;
        }
        return false;
    }

    public static void equipSkillSlot(ServerPlayer player, boolean isPassive, int slot, String skillId) {
        SystemData data = data(player);
        if (isPassive) {
            data.setEquippedPassiveSkill(slot, skillId);
            if (skillId != null && !skillId.isEmpty()) {
                Component sName = SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).name() : Component.literal(skillId);
                player.displayClientMessage(Component.translatable("awakening_system.action.passive_assigned", slot + 1, sName), true);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.action.passive_cleared", slot + 1), true);
            }
        } else {
            data.setEquippedActiveSkill(slot, skillId);
            if (skillId != null && !skillId.isEmpty()) {
                Component sName = SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).name() : Component.literal(skillId);
                player.displayClientMessage(Component.translatable("awakening_system.action.active_assigned", slot + 1, sName), true);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.action.active_cleared", slot + 1), true);
            }
        }
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
        AwakeningSystemMod.sync(player);
    }

    public static boolean activateStigma(ServerPlayer player) {
        SystemData data = data(player);
        if (data.isAnySystemUpdating()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
            player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
            return false;
        }
        String sponsor = data.sponsorConstellation();
        if (sponsor == null || sponsor.isEmpty()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.no_sponsor_stigma"), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
            return false;
        }

        long now = player.level().getGameTime();
        if (now < data.stigmaCooldownUntil()) {
            int secLeft = (int) ((data.stigmaCooldownUntil() - now + 19) / 20);
            player.displayClientMessage(Component.translatable("awakening_system.action.stigma_cooldown", secLeft), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.6F, 1.0F);
            return false;
        }

        // 45 segundos de enfriamiento
        data.setStigmaCooldownUntil(now + (45L * 20L));

        ServerLevel sl = (ServerLevel) player.level();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        switch (sponsor) {
            case "demon_like_judge_of_fire" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false, true));
                spawnArcaneCircleGround(sl, player, 6.0F, 60, 0xFFFF3300);
                for (int dy = 0; dy <= 16; dy += 2) {
                    sl.sendParticles(ParticleTypes.FLAME, px, py + dy, pz, 40, 1.8D, 1.0D, 1.8D, 0.1D);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py + dy, pz, 20, 1.5D, 0.8D, 1.5D, 0.08D);
                }
                sl.sendParticles(ParticleTypes.FLASH, px, py + 2.0D, pz, 3, 0, 0, 0, 0);
                sl.playSound(null, px, py, pz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.3F, 1.0F);
                sl.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 1.2F);

                var enemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.setRemainingFireTicks(200);
                    e.hurt(player.damageSources().magic(), 45.0F);
                    e.setDeltaMovement(0, 0.8D, 0);
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.judge_flames"), true);
            }
            case "prisoner_of_the_golden_headband" -> {
                player.setDeltaMovement(0, 1.6D, 0);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false, false));
                spawnArcaneCircleGround(sl, player, 5.5F, 50, 0xFFFFD700);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 50, 1.2D, 0.3D, 1.2D, 0.08D);
                sl.playSound(null, px, py, pz, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.7F);

                sl.getServer().tell(new net.minecraft.server.TickTask(sl.getServer().getTickCount() + 15, () -> {
                    if (player.isAlive()) {
                        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 2, 0, 0, 0, 0);
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 0.5D, player.getZ(), 1, 0, 0, 0, 0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.8F);

                        var slamTargets = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(10.0D),
                                e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                        for (var t : slamTargets) {
                            t.hurt(player.damageSources().playerAttack(player), 50.0F);
                            double tdx = t.getX() - player.getX();
                            double tdz = t.getZ() - player.getZ();
                            double dist = Math.max(0.1D, Math.sqrt(tdx * tdx + tdz * tdz));
                            t.setDeltaMovement(tdx / dist * 2.2D, 0.9D, tdz / dist * 2.2D);
                            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                        }
                    }
                }));
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.ruyi_staff"), true);
            }
            case "supreme_monarch_of_shadows" -> {
                spawnArcaneCircleGround(sl, player, 7.0F, 70, 0xFF4A148C);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, px, py + 1.0D, pz, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, px, py + 0.5D, pz, 120, 3.0D, 0.5D, 3.0D, 0.1D);
                sl.playSound(null, px, py, pz, SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, px, py, pz, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.1F, 1.1F);

                var shadowEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : shadowEnemies) {
                    e.setDeltaMovement(0, -2.5D, 0);
                    e.hurt(player.damageSources().magic(), 45.0F);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.shadow_monarch"), true);
            }
            case "monarch_of_destruction_antares", "abyssal_black_dragon" -> {
                spawnArcaneCircleGround(sl, player, 7.5F, 70, 0xFFFF2200);
                for (int deg = 0; deg < 360; deg += 12) {
                    double rad = Math.toRadians(deg);
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, px + Math.cos(rad) * 4.0D, py + 0.5D, pz + Math.sin(rad) * 4.0D, 5, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.FLAME, px + Math.cos(rad) * 6.0D, py + 0.5D, pz + Math.sin(rad) * 6.0D, 8, 0.3D, 0.3D, 0.3D, 0.08D);
                }
                sl.playSound(null, px, py, pz, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.4F, 0.8F);
                sl.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.7F);

                var fireEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : fireEnemies) {
                    e.setRemainingFireTicks(240);
                    e.hurt(player.damageSources().playerAttack(player), 48.0F);
                    double edx = e.getX() - px;
                    double edz = e.getZ() - pz;
                    double dist = Math.max(0.1D, Math.sqrt(edx * edx + edz * edz));
                    e.setDeltaMovement(edx / dist * 2.0D, 0.6D, edz / dist * 2.0D);
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.chaos_dragon"), true);
            }
            case "supreme_swordsman_joonghyuk" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1, false, false, true));
                spawnArcaneCircleGround(sl, player, 5.5F, 60, 0xFF00E5FF);
                sl.sendParticles(ParticleTypes.END_ROD, px, py + 1.2D, pz, 60, 1.2D, 1.0D, 1.2D, 0.1D);
                sl.playSound(null, px, py, pz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);

                var slowTargets = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : slowTargets) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 3));
                    e.hurt(player.damageSources().playerAttack(player), 20.0F);
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.regressor_eye"), true);
            }
            case "plum_blossom_sword_saint" -> {
                spawnMurimCircleGround(sl, player, 6.0F, 70, 0xFFFF69B4);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px, py + 1.0D, pz, 150, 4.0D, 1.5D, 4.0D, 0.2D);
                for (int deg = 0; deg < 360; deg += 30) {
                    double rad = Math.toRadians(deg);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px + Math.cos(rad) * 3.0D, py + 0.8D, pz + Math.sin(rad) * 3.0D, 2, 0.2D, 0.2D, 0.2D, 0.05D);
                }
                sl.playSound(null, px, py, pz, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.2F);
                sl.playSound(null, px, py, pz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);

                var plumEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : plumEnemies) {
                    e.hurt(player.damageSources().playerAttack(player), 44.0F);
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.plum_blossom"), true);
            }
            default -> {
                player.heal(player.getMaxHealth() * 0.4F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false, true));
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 1.0D, pz, 80, 1.5D, 1.0D, 1.5D, 0.2D);
                sl.playSound(null, px, py, pz, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

                var nearby = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(10.0D),
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : nearby) {
                    e.hurt(player.damageSources().playerAttack(player), 30.0F);
                    double bdx = e.getX() - px;
                    double bdz = e.getZ() - pz;
                    double dist = Math.max(0.1D, Math.sqrt(bdx * bdx + bdz * bdz));
                    e.setDeltaMovement(bdx / dist * 1.8D, 0.4D, bdz / dist * 1.8D);
                }
                player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.sponsor_blessing"), true);
            }
        }

        triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.COMBO_TRANCE);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void handleCultivationAction(ServerPlayer player, String action, int targetIndex) {
        SystemData data = data(player);
        if (data.isAnySystemUpdating()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
            player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }
        if ("toggle_meditation".equals(action)) {
            if (player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity seat) {
                double sx = seat.getX();
                double sy = seat.getY();
                double sz = seat.getZ();
                player.stopRiding();
                seat.discard();
                player.teleportTo(sx, sy, sz);
                cancelMagicCircles(player);
                player.displayClientMessage(Component.translatable("awakening_system.cultivation.stopped"), true);
                player.playNotifySound(SoundEvents.CHEST_CLOSE, SoundSource.PLAYERS, 0.8F, 1.2F);
            } else {
                if (!player.onGround() && !player.isInWater()) {
                    player.displayClientMessage(Component.translatable("awakening_system.cultivation.must_be_grounded"), true);
                    return;
                }
                ServerLevel sl = player.serverLevel();
                com.soybe.awakeningsystem.entity.MeditationSeatEntity seat = com.soybe.awakeningsystem.entity.ModEntities.MEDITATION_SEAT.get().create(sl);
                if (seat != null) {
                    seat.setPos(player.getX(), player.getY(), player.getZ());
                    sl.addFreshEntity(seat);
                    player.startRiding(seat, true);
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
                    player.displayClientMessage(Component.translatable("awakening_system.cultivation.started"), true);
                }
            }
        } else if ("open_meridian".equals(action)) {
            if (targetIndex < 0 || targetIndex >= 12) return;
            if (data.isMeridianOpen(targetIndex)) {
                player.displayClientMessage(Component.translatable("awakening_system.action.meridian_already_open"), true);
                return;
            }
            int qiCost = (targetIndex + 1) * 35;
            if (data.cultivationQi() < qiCost) {
                player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", qiCost), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            data.consumeCultivationQi(qiCost);
            data.openMeridian(targetIndex);
            player.playNotifySound(ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            Component meridianName = Component.translatable("awakening_system.meridian." + targetIndex + ".name");
            Component organName = Component.translatable("awakening_system.meridian." + targetIndex + ".organ");
            player.displayClientMessage(Component.translatable("awakening_system.action.meridian_success", meridianName, organName), true);
            AwakeningSystemMod.sync(player);
        } else if ("breakthrough_sublevel".equals(action)) {
            int currentSub = data.cultivationSubLevel();
            if (currentSub >= 9) {
                player.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_max"), true);
                return;
            }
            int reqQi = data.maxCultivationQi();
            if (data.cultivationQi() < reqQi) {
                player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", reqQi), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            data.setCultivationSubLevel(currentSub + 1);
            data.setCultivationQi(0);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
            }
            player.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", currentSub + 1, getDantianRealmComponent(data.dantianRealm())), true);
            AwakeningSystemMod.sync(player);
        } else if ("advance_realm".equals(action)) {
            int current = data.dantianRealm();
            if (current >= 9) {
                player.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_max"), true);
                return;
            }
            if (data.isUnderTribulation()) {
                player.displayClientMessage(Component.translatable("awakening_system.action.tribulation_active"), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            if (data.cultivationSubLevel() < 9) {
                player.displayClientMessage(Component.translatable("awakening_system.cultivation.need_sublevel_9"), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            int requiredMeridians = Math.min(12, (current + 1) * 2);
            int reqQi = (current + 1) * 150;
            if (data.getOpenedMeridiansCount() < requiredMeridians) {
                player.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_req", reqQi), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            if (data.cultivationQi() < reqQi) {
                player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", reqQi), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            int targetRealm = current + 1;
            if (targetRealm >= 2) {
                // Iniciar Tribulación Celestial de Murim (雷劫)
                int strikes = switch (targetRealm) {
                    case 2 -> 3;  // Cimientos: 3 rayos
                    case 3 -> 6;  // Núcleo Dorado: 6 rayos
                    case 4 -> 9;  // Alma Naciente: 9 rayos
                    case 5 -> 12; // Transformación Alma: 12 rayos
                    case 6 -> 15; // Refinamiento Vacío: 15 rayos
                    case 7 -> 18; // Integración Dao: 18 rayos
                    case 8 -> 27; // Gran Mahayana: 27 rayos
                    case 9 -> 36; // Ascensión Inmortal Verdadera: 36 rayos
                    default -> 3;
                };
                data.consumeCultivationQi(reqQi);
                data.startTribulation(targetRealm, strikes);

                if (player.level() instanceof ServerLevel sl) {
                    sl.setWeatherParameters(0, strikes * 35 + 200, true, true);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CELESTIAL_TRIBULATION.value(), SoundSource.WEATHER, 3.0F, 1.0F);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 1.5D, 1.5D, 1.5D, 0.2D);
                }

                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                        Component.translatable("title.awakening_system.tribulation_title")));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        Component.translatable("title.awakening_system.tribulation_subtitle", strikes)));
                player.sendSystemMessage(Component.translatable("message.awakening_system.tribulation_start"));
                AwakeningSystemMod.sync(player);
                return;
            }

            data.setDantianRealm(1);
            data.setCultivationSubLevel(1);
            data.setCultivationQi(0);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.0D, 1.0D, 1.0D, 0.2D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
            }
            player.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_success", getDantianRealmComponent(1)), true);
            AwakeningSystemMod.sync(player);
        } else if ("awaken_physique".equals(action) || "cycle_physique".equals(action) || "select_physique".equals(action)) {
            String[] physiques = {"pure_yang", "nine_yin", "innate_sword", "celestial_dragon", "heavenly_demon", "primordial_chaos"};
            String chosen;
            if ("select_physique".equals(action) && targetIndex >= 0 && targetIndex < physiques.length) {
                chosen = physiques[targetIndex];
            } else if ("cycle_physique".equals(action) && player.isCreative()) {
                int currentIdx = 0;
                for (int i = 0; i < physiques.length; i++) {
                    if (physiques[i].equals(data.murimPhysique())) {
                        currentIdx = (i + 1) % physiques.length;
                        break;
                    }
                }
                chosen = physiques[currentIdx];
            } else {
                boolean isFirstAwakening = data.murimPhysique().isEmpty();
                if (!isFirstAwakening && !player.isCreative()) {
                    player.displayClientMessage(Component.translatable("awakening_system.action.physique_already_set"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }

                // Sorteo de Afinidad del Dao Celestial:
                // 38% Pure Yang, 35% Nine Yin, 16% Innate Sword, 8% Celestial Dragon, 2.5% Heavenly Demon, 0.5% Primordial Chaos
                int roll = player.getRandom().nextInt(1000);
                if (roll < 5) {
                    chosen = "primordial_chaos";
                } else if (roll < 30) {
                    chosen = "heavenly_demon";
                } else if (roll < 110) {
                    chosen = "celestial_dragon";
                } else if (roll < 270) {
                    chosen = "innate_sword";
                } else if (roll < 620) {
                    chosen = "nine_yin";
                } else {
                    chosen = "pure_yang";
                }
            }

            data.setMurimPhysique(chosen);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 0.8D, 1.0D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.5D, player.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.05D);
            }
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.3F);

            int physColor = switch (chosen) {
                case "primordial_chaos" -> 0xFFE056FD;
                case "heavenly_demon" -> 0xFFFF2255;
                case "celestial_dragon" -> 0xFFFFD700;
                case "innate_sword" -> 0xFFDDA0DD;
                case "nine_yin" -> 0xFF70E8FF;
                default -> 0xFFFF7666;
            };
            Component pName = Component.translatable("awakening_system.physique." + chosen + ".name");
            AwakeningSystemMod.notify(player,
                    "awakening_system.notification.physique_awakened_title",
                    "awakening_system.notification.physique_awakened_desc",
                    physColor, pName.getString());
            player.displayClientMessage(Component.translatable("awakening_system.cultivation.physique_selected", pName), true);
            AwakeningSystemMod.sync(player);
        } else if ("use_physique_skill".equals(action)) {
            String phys = data.murimPhysique();
            if (phys == null || phys.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.awakening_system.physique_no_physique"), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            if (data.getPhysiqueSkillCooldown() > 0) {
                int sec = (data.getPhysiqueSkillCooldown() + 19) / 20;
                player.displayClientMessage(Component.translatable("message.awakening_system.physique_cooldown", sec), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            int energyCost = 25;
            if (data.energy() < energyCost) {
                player.displayClientMessage(Component.translatable("awakening_system.action.not_enough_energy", energyCost), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }

            data.consumeEnergy(energyCost);
            data.setPhysiqueSkillCooldown(400); // 20s cooldown

            ServerLevel sl = player.serverLevel();
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MARTIAL_SKILL_ACTIVATE.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
            int realm = data.dantianRealm();

            switch (phys) {
                case "pure_yang" -> {
                    // Explosión de Fuego Solar Purificador
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_pure_yang"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.8F);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8D, player.getZ(), 80, 2.5D, 0.8D, 2.5D, 0.15D);
                    sl.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.5D, player.getZ(), 25, 2.0D, 0.5D, 2.0D, 0.1D);

                    float dmg = 26.0F + realm * 5.0F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.5D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                    for (LivingEntity mob : targets) {
                        mob.hurt(player.damageSources().playerAttack(player), dmg);
                        mob.igniteForSeconds(8);
                        mob.setDeltaMovement(mob.getDeltaMovement().add(
                                (mob.getX() - player.getX()) * 0.25D,
                                0.35D,
                                (mob.getZ() - player.getZ()) * 0.25D));
                    }
                    player.heal(10.0F);
                    player.removeEffect(MobEffects.POISON);
                    player.removeEffect(MobEffects.WITHER);
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    player.removeEffect(MobEffects.WEAKNESS);
                }
                case "nine_yin" -> {
                    // Ola Glacial del Cero Absoluto del Inframundo
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_nine_yin"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.8F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.8D, player.getZ(), 90, 3.0D, 0.8D, 3.0D, 0.12D);
                    sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.5D, player.getZ(), 30, 2.0D, 0.5D, 2.0D, 0.05D);

                    float dmg = 24.0F + realm * 4.5F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(9.0D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                    for (LivingEntity mob : targets) {
                        mob.hurt(player.damageSources().magic(), dmg);
                        mob.setTicksFrozen(240);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                }
                case "innate_sword" -> {
                    // Seis Espadas de Qi Voladoras (Vórtice Cortante de Espadas)
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_sword_body"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.8F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.5F, 1.4F);

                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 16, 2.5D, 0.5D, 2.5D, 0.1D);
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.2D, player.getZ(), 60, 2.5D, 1.0D, 2.5D, 0.2D);

                    float perSwordDmg = 15.0F + realm * 3.5F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));

                    if (!targets.isEmpty()) {
                        for (int i = 0; i < 6; i++) {
                            LivingEntity target = targets.get(i % targets.size());
                            target.hurt(player.damageSources().playerAttack(player), perSwordDmg);
                            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.2D);
                            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.4F + (i * 0.1F));
                        }
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
                }
                case "celestial_dragon" -> {
                    // Rugido Primordial del Dragón Celestial
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_celestial_dragon"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 1.4F);

                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0D, player.getZ(), 70, 3.5D, 1.0D, 3.5D, 0.15D);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 3.0D, 1.0D, 3.0D, 0.2D);

                    float dmg = 28.0F + realm * 6.0F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                    for (LivingEntity mob : targets) {
                        mob.hurt(player.damageSources().playerAttack(player), dmg);
                        double dx = mob.getX() - player.getX();
                        double dz = mob.getZ() - player.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        mob.setDeltaMovement((dx / dist) * 1.8D, 0.6D, (dz / dist) * 1.8D);
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1));
                }
                case "heavenly_demon" -> {
                    // Dominio de Sangre y Fuego Negro Asura
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_heavenly_demon"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.8F, 0.7F);

                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.8D, player.getZ(), 80, 3.0D, 0.8D, 3.0D, 0.15D);
                    sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 0.5D, player.getZ(), 40, 2.5D, 0.5D, 2.5D, 0.08D);

                    float dmg = 22.0F + realm * 5.0F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                    for (LivingEntity mob : targets) {
                        mob.hurt(player.damageSources().wither(), dmg);
                        mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                        mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                    player.heal(12.0F);
                }
                case "primordial_chaos" -> {
                    // Fisura Espaciotemporal del Génesis (Vórtice de Vacío Gravitatorio)
                    player.sendSystemMessage(Component.translatable("message.awakening_system.physique_primordial_chaos"));
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.PLAYERS, 1.3F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 0.8F);

                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 5, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 100, 3.5D, 1.5D, 3.5D, 0.3D);
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 40, 2.5D, 1.2D, 2.5D, 0.1D);
                    sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.8D, player.getZ(), 40, 3.0D, 1.0D, 3.0D, 0.15D);

                    float dmg = 36.0F + realm * 6.5F;
                    var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(11.0D),
                            e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                    for (LivingEntity mob : targets) {
                        double dx = player.getX() - mob.getX();
                        double dz = player.getZ() - mob.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        mob.setDeltaMovement((dx / dist) * 0.9D, 0.25D, (dz / dist) * 0.9D);

                        mob.hurt(player.damageSources().magic(), dmg);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 5));
                    }
                    data.addCultivationQi(250);
                    data.restoreEnergy(50);
                }
            }

            AwakeningSystemMod.sync(player);
        } else if ("select_dao_skill".equals(action)) {
            if (targetIndex >= 0 && targetIndex < 5) {
                data.setSelectedDaoSkill(targetIndex);
                AwakeningSystemMod.sync(player);
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        } else if ("use_dao_skill".equals(action)) {
            int skillIdx = (targetIndex >= 0 && targetIndex < 5) ? targetIndex : data.selectedDaoSkill();
            int currentRealm = data.dantianRealm();

            // Requisitos de Reino y Coste de Qi
            int reqRealm = switch (skillIdx) {
                case 0 -> 1; // Espada Espiritual: Reino 1 (Condensación de Qi)
                case 1 -> 2; // Palma del Vacío: Reino 2 (Cimientos)
                case 2 -> 3; // Dominio Núcleo Dorado: Reino 3 (Núcleo Dorado)
                case 3 -> 4; // Alma Naciente: Reino 4 (Alma Naciente)
                case 4 -> 5; // Paso Celestial: Reino 5 (Transformación Alma / Vacío)
                default -> 1;
            };
            int qiCost = switch (skillIdx) {
                case 0 -> 20;
                case 1 -> 35;
                case 2 -> 50;
                case 3 -> 75;
                case 4 -> 45;
                default -> 20;
            };

            if (currentRealm < reqRealm) {
                player.displayClientMessage(Component.translatable("message.awakening_system.dao_skill_realm_req", getDantianRealmComponent(reqRealm)), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            if (data.daoSkillCooldown() > 0) {
                int sec = (data.daoSkillCooldown() + 19) / 20;
                player.displayClientMessage(Component.translatable("message.awakening_system.dao_skill_cooldown", sec), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }
            if (data.cultivationQi() < qiCost) {
                player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", qiCost), true);
                player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                return;
            }

            // Habilidades Dao canalizables (Palma, Dominio, Alma Naciente)
            if (skillIdx == 1 || skillIdx == 2 || skillIdx == 3) {
                PlayerChargingState active = ACTIVE_CHARGES.get(player.getUUID());
                if (active != null) {
                    if (active.daoSkillIdx == skillIdx) {
                        if (active.currentTicks >= Math.max(8, (int) (active.maxTicks * 0.35F))) {
                            releaseActiveCharge(player, false);
                            return;
                        } else {
                            player.displayClientMessage(Component.literal("§e✦ ¡Canalizando Qi ancestral! Aguarda un instante..."), true);
                            return;
                        }
                    } else {
                        cancelChargingSkill(player, false);
                    }
                }
                startChargingSkill(player, "dao_skill_" + skillIdx, -1, skillIdx);
                return;
            }

            executeDaoSkillDirect(player, skillIdx, 1.0F, false);
        }
    }

    public static void executeDaoSkillDirect(ServerPlayer player, int skillIdx, float chargeMult, boolean isOvercharged) {
        SystemData data = data(player);
        int currentRealm = data.dantianRealm();
        int qiCost = switch (skillIdx) {
            case 0 -> 20;
            case 1 -> 35;
            case 2 -> 50;
            case 3 -> 75;
            case 4 -> 45;
            default -> 20;
        };
        int cooldownTicks = switch (skillIdx) {
            case 0 -> 140; // 7s
            case 1 -> 200; // 10s
            case 2 -> 360; // 18s
            case 3 -> 500; // 25s
            case 4 -> 160; // 8s
            default -> 140;
        };

        data.consumeCultivationQi(qiCost);
        data.setDaoSkillCooldown(cooldownTicks);
        ServerLevel sl = player.serverLevel();

        switch (skillIdx) {
            case 0 -> {
                // Skill 0: Espada Espiritual Voladora (Qi Flying Swords)
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MARTIAL_SKILL_ACTIVATE.value(), SoundSource.PLAYERS, 1.2F, 1.3F);

                var lookVec = player.getLookAngle();
                spawnMurimCircleGround(sl, player, 3.6F, 45, 0xFF00FFCC);
                spawnMurimCircleVertical(sl, player, 2.2F, 40, 0xFF00FFCC, false);

                float dmg = (18.0F + currentRealm * 4.5F) * chargeMult;
                var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(18.0D),
                        e -> e != player && e.isAlive() && !player.isAlliedTo(e) && player.hasLineOfSight(e));
                targets.sort((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)));

                int struck = 0;
                for (LivingEntity mob : targets) {
                    mob.hurt(player.damageSources().magic(), dmg);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, mob.getX(), mob.getY() + 1.0D, mob.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, mob.getX(), mob.getY() + 1.0D, mob.getZ(), 16, 0.3D, 0.3D, 0.3D, 0.1D);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + 1.0D, mob.getZ(), 12, 0.2D, 0.2D, 0.2D, 0.1D);
                    struck++;
                    if (struck >= 4) break;
                }
                if (struck == 0) {
                    for (int step = 2; step <= 16; step += 2) {
                        double px = player.getX() + lookVec.x * step;
                        double py = player.getEyeY() + lookVec.y * step;
                        double pz = player.getZ() + lookVec.z * step;
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0, 0, 0, 0);
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py, pz, 6, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                }
                player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_swords"));
            }
            case 1 -> {
                // Skill 1: Palma del Vacío Celestial (Nine Heavens Void Palm)
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.4F, 1.1F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.4F);

                var lookVec = player.getLookAngle().normalize();
                spawnMurimCircleVertical(sl, player, isOvercharged ? 4.5F : 3.6F, 40, 0xFF80D8FF, false);

                float dmg = (28.0F + currentRealm * 6.0F) * chargeMult;

                spawnShockwaveRing(sl, player.getX() + lookVec.x * 3.0D, player.getY() + 1.0D, player.getZ() + lookVec.z * 3.0D, ParticleTypes.POOF, 4.0D, 24, 0.15D);
                spawnShockwaveRing(sl, player.getX() + lookVec.x * 6.0D, player.getY() + 1.0D, player.getZ() + lookVec.z * 6.0D, ParticleTypes.EXPLOSION, 6.0D, 18, 0.2D);
                if (isOvercharged) {
                    spawnShockwaveRing(sl, player.getX() + lookVec.x * 9.0D, player.getY() + 1.0D, player.getZ() + lookVec.z * 9.0D, ParticleTypes.SONIC_BOOM, 8.0D, 24, 0.25D);
                }

                var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                for (LivingEntity mob : targets) {
                    double dx = mob.getX() - player.getX();
                    double dz = mob.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    double dot = (dx * lookVec.x + dz * lookVec.z) / dist;
                    if (dot > 0.35D) {
                        mob.hurt(player.damageSources().playerAttack(player), dmg);
                        mob.setDeltaMovement(lookVec.x * (isOvercharged ? 2.2D : 1.6D), 0.55D, lookVec.z * (isOvercharged ? 2.2D : 1.6D));
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, isOvercharged ? 3 : 2));
                        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, isOvercharged ? 2 : 1));
                    }
                }
                player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_palm"));
            }
            case 2 -> {
                // Skill 2: Dominio del Núcleo Dorado (Golden Core Domain)
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 0.9F);

                spawnMurimCircleGround(sl, player, 6.5F, 80, 0xFFFFD700);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 8.0D, 40, 0.2D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.END_ROD, 12.0D, 50, 0.25D);
                if (isOvercharged) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLASH, 16.0D, 30, 0.3D);
                }

                player.heal((14.0F + currentRealm * 2.0F) * chargeMult);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, isOvercharged ? 2 : 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, isOvercharged ? 2 : 1));
                data.addCultivationQi((int) ((60 + currentRealm * 15) * (isOvercharged ? 1.5F : 1.0F)));

                var targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                        e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                for (LivingEntity mob : targets) {
                    mob.hurt(player.damageSources().magic(), (18.0F + currentRealm * 3.5F) * chargeMult);
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                    mob.igniteForSeconds(10);
                }
                player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_domain"));
            }
            case 3 -> {
                // Skill 3: Proyección del Alma Naciente (Nascent Soul Avatar)
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.2F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.4F);

                var lookVec = player.getLookAngle();
                spawnMurimCircleGround(sl, player, 4.8F, 70, 0xFFE040FB);
                spawnMurimCircleVertical(sl, player, 3.6F, 70, 0xFFBA68C8, false);

                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.2D, player.getZ(), 60, 0.8D, 1.2D, 0.8D, 0.08D);
                sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 70, 1.0D, 1.0D, 1.0D, 0.2D);

                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, isOvercharged ? 4 : 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, isOvercharged ? 2 : 1));
                player.getPersistentData().putInt("nascent_soul_guard_ticks", (int) (400 * chargeMult));

                player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_nascent"));
            }
            case 4 -> {
                // Skill 4: Paso Celestial del Destello Espiritual (Void Flash Step)
                double oldX = player.getX();
                double oldY = player.getY();
                double oldZ = player.getZ();

                var lookVec = player.getLookAngle();
                spawnMurimCircleGround(sl, oldX, oldY, oldZ, 3.2F, 35, 0xFF00E5FF);

                double targetX = oldX + lookVec.x * 15.0D;
                double targetY = oldY + lookVec.y * 15.0D;
                double targetZ = oldZ + lookVec.z * 15.0D;

                BlockPos targetPos = BlockPos.containing(targetX, targetY, targetZ);
                while (!sl.getBlockState(targetPos).isAir() && targetPos.getY() < 315) {
                    targetPos = targetPos.above();
                }
                player.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                spawnMurimCircleGround(sl, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 3.2F, 35, 0xFF00E5FF);

                sl.playSound(null, oldX, oldY, oldZ, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 1.2F);
                sl.sendParticles(ParticleTypes.FLASH, oldX, oldY + 1.0D, oldZ, 3, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, oldX, oldY + 1.0D, oldZ, 60, 1.8D, 1.8D, 1.8D, 0.25D);
                sl.sendParticles(ParticleTypes.EXPLOSION, oldX, oldY + 1.0D, oldZ, 4, 1.0D, 1.0D, 1.0D, 0.1D);

                float dmg = (36.0F + currentRealm * 7.0F) * chargeMult;
                var targets = sl.getEntitiesOfClass(LivingEntity.class, new AABB(oldX - 7, oldY - 4, oldZ - 7, oldX + 7, oldY + 4, oldZ + 7),
                        e -> e != player && e.isAlive() && !player.isAlliedTo(e));
                for (LivingEntity mob : targets) {
                    mob.hurt(player.damageSources().lightningBolt(), dmg);
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
                }

                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 1.2F);
                player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_flash"));
            }
        }
        AwakeningSystemMod.sync(player);
    }

    public static float getZoneQiDensity(net.minecraft.world.entity.player.Player player) {
        if (player == null) return 1.0F;
        Level level = player.level();
        ResourceLocation dimLoc = level.dimension().location();

        // 1. Verificación por estructura registrada en el servidor
        if (player.getServer() != null) {
            var sData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(player.getServer());
            for (var entry : sData.getAll().entrySet()) {
                if (entry.getKey().startsWith("spirit_valley")) {
                    BlockPos vPos = entry.getValue();
                    if (player.distanceToSqr(vPos.getX() + 0.5D, vPos.getY() + 1.0D, vPos.getZ() + 0.5D) <= 32.0D * 32.0D) {
                        return 5.0F; // Concentración Suprema de Qi: x5.0
                    }
                }
            }
        }

        // 2. Detección universal en Cliente o Servidor (cuenca sagrada de jade y aguas termales)
        BlockPos pPos = player.blockPosition();
        for (BlockPos check : BlockPos.betweenClosed(pPos.offset(-20, -6, -20), pPos.offset(20, 6, 20))) {
            if (level.getBlockState(check).is(Blocks.EMERALD_BLOCK) && level.getBlockState(check.above()).is(Blocks.WATER)) {
                return 5.0F; // Hotspot de Qi x5.0 detectado en cualquier mundo (Isekai, Murim u Overworld)
            }
        }

        if (dimLoc.getPath().contains("murim_world")) {
            return 3.5F;
        } else if (dimLoc.getPath().contains("isekai_world")) {
            double distToTree = Math.sqrt(player.getX() * player.getX() + player.getZ() * player.getZ());
            if (distToTree <= 55.0D) {
                return 2.5F; // Urðarbrunnr / Manantial Sagrado del Gran Árbol
            }
            return 1.5F;
        } else {
            if (player.getY() >= 120.0D) {
                return 1.8F; // Cima de alta montaña
            }
        }
        return 1.0F;
    }

    public static String getZoneQiDensityLabel(net.minecraft.world.entity.player.Player player) {
        if (player == null) return "Normal";
        Level level = player.level();
        ResourceLocation dimLoc = level.dimension().location();

        // 1. Verificación por estructura registrada en servidor
        if (player.getServer() != null) {
            var sData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(player.getServer());
            for (var entry : sData.getAll().entrySet()) {
                if (entry.getKey().startsWith("spirit_valley")) {
                    BlockPos vPos = entry.getValue();
                    if (player.distanceToSqr(vPos.getX() + 0.5D, vPos.getY() + 1.0D, vPos.getZ() + 0.5D) <= 32.0D * 32.0D) {
                        return "Valle de las Bestias Espirituales (Aguas Termales de Jade)";
                    }
                }
            }
        }

        // 2. Detección universal en Cliente o Servidor
        BlockPos pPos = player.blockPosition();
        for (BlockPos check : BlockPos.betweenClosed(pPos.offset(-20, -6, -20), pPos.offset(20, 6, 20))) {
            if (level.getBlockState(check).is(Blocks.EMERALD_BLOCK) && level.getBlockState(check.above()).is(Blocks.WATER)) {
                return "Valle de las Bestias Espirituales (Aguas Termales de Jade)";
            }
        }

        if (dimLoc.getPath().contains("murim_world")) {
            return "Reino Murim Inmortal";
        } else if (dimLoc.getPath().contains("isekai_world")) {
            double distToTree = Math.sqrt(player.getX() * player.getX() + player.getZ() * player.getZ());
            if (distToTree <= 55.0D) {
                return "Manantial de Urðr";
            }
            return "Mundo Isekai";
        } else {
            if (player.getY() >= 120.0D) {
                return "Cima Montañosa";
            }
        }
        return "Tierra Común";
    }

    public static Component getDantianRealmComponent(int realm) {
        return Component.translatable("awakening_system.cultivation.realm." + Math.max(0, Math.min(9, realm)));
    }

    public static String getDantianRealmName(int realm) {
        return getDantianRealmComponent(realm).getString();
    }

    public static void spawnShockwaveRing(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions particle, double radius, int count, double speed) {
        double step = (2.0 * Math.PI) / Math.max(1, count);
        for (int i = 0; i < count; i++) {
            double angle = i * step;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double px = x + dx * radius;
            double pz = z + dz * radius;
            level.sendParticles(particle, px, y, pz, 1, dx * speed, 0.02D, dz * speed, speed);
        }
    }

    public static void spawnVortexPillar(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions particle, double maxRadius, double height, int layers, int particlesPerLayer) {
        for (int l = 0; l < layers; l++) {
            double py = y + (height * ((double) l / Math.max(1, layers)));
            double radius = maxRadius * (1.0 - (double) l / (layers * 2.0));
            double angleOffset = (l * Math.PI) / 4.0;
            double step = (2.0 * Math.PI) / Math.max(1, particlesPerLayer);
            for (int p = 0; p < particlesPerLayer; p++) {
                double angle = p * step + angleOffset;
                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;
                level.sendParticles(particle, px, py, pz, 1, 0, 0.05D, 0, 0.02D);
            }
        }
    }

    public static void spawnExpandingDome(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions particle, double radius, int rings, int pointsPerRing) {
        for (int r = 0; r < rings; r++) {
            double phi = (Math.PI * 0.5D) * ((double) r / Math.max(1, rings));
            double ringRadius = radius * Math.cos(phi);
            double py = y + radius * Math.sin(phi);
            double step = (2.0D * Math.PI) / Math.max(1, pointsPerRing);
            for (int p = 0; p < pointsPerRing; p++) {
                double theta = p * step;
                double px = x + ringRadius * Math.cos(theta);
                double pz = z + ringRadius * Math.sin(theta);
                level.sendParticles(particle, px, py, pz, 1, 0, 0.02D, 0, 0.01D);
            }
        }
    }

    public static void spawnHelixBeam(ServerLevel level, net.minecraft.world.phys.Vec3 start, net.minecraft.world.phys.Vec3 dir, double length, net.minecraft.core.particles.ParticleOptions coreParticle, net.minecraft.core.particles.ParticleOptions spiralParticle, double spiralRadius) {
        net.minecraft.world.phys.Vec3 norm = dir.normalize();
        net.minecraft.world.phys.Vec3 up = Math.abs(norm.y) > 0.9D ? new net.minecraft.world.phys.Vec3(1, 0, 0) : new net.minecraft.world.phys.Vec3(0, 1, 0);
        net.minecraft.world.phys.Vec3 right = norm.cross(up).normalize();
        up = right.cross(norm).normalize();

        double stepDist = 0.5D;
        int steps = (int) (length / stepDist);
        for (int i = 0; i <= steps; i++) {
            double d = i * stepDist;
            net.minecraft.world.phys.Vec3 corePos = start.add(norm.scale(d));
            if (coreParticle != null) {
                level.sendParticles(coreParticle, corePos.x, corePos.y, corePos.z, 1, 0, 0, 0, 0);
            }
            if (spiralParticle != null) {
                double angle1 = d * 1.5D;
                double angle2 = angle1 + Math.PI;
                net.minecraft.world.phys.Vec3 offset1 = right.scale(Math.cos(angle1) * spiralRadius).add(up.scale(Math.sin(angle1) * spiralRadius));
                net.minecraft.world.phys.Vec3 offset2 = right.scale(Math.cos(angle2) * spiralRadius).add(up.scale(Math.sin(angle2) * spiralRadius));
                level.sendParticles(spiralParticle, corePos.x + offset1.x, corePos.y + offset1.y, corePos.z + offset1.z, 1, 0, 0, 0, 0);
                level.sendParticles(spiralParticle, corePos.x + offset2.x, corePos.y + offset2.y, corePos.z + offset2.z, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void spawnBurstCross(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions particle, double length, int points) {
        double half = length * 0.5D;
        double step = length / Math.max(1, points);
        for (int i = 0; i <= points; i++) {
            double offset = -half + i * step;
            level.sendParticles(particle, x + offset, y + 0.8D + offset * 0.3D, z + offset, 1, 0, 0.02D, 0, 0.02D);
            level.sendParticles(particle, x + offset, y + 0.8D - offset * 0.3D, z - offset, 1, 0, 0.02D, 0, 0.02D);
        }
    }

    public static void spawnMagicCircleEffect(ServerLevel level, int casterEntityId, double x, double y, double z, Vec3 lookDir, float radius, int durationTicks, int color, int circleType, boolean isCharging) {
        var packet = new SpawnMagicCirclePayload(casterEntityId, x, y, z, (float) lookDir.x, (float) lookDir.y, (float) lookDir.z, radius, durationTicks, color, circleType, isCharging);
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, 96.0D, packet);
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.5F);
    }

    public static void spawnMagicCircleEffect(ServerLevel level, double x, double y, double z, Vec3 lookDir, float radius, int durationTicks, int color, int circleType) {
        spawnMagicCircleEffect(level, -1, x, y, z, lookDir, radius, durationTicks, color, circleType, false);
    }

    public static void cancelMagicCircles(ServerPlayer player) {
        if (player.level() instanceof ServerLevel sl) {
            PacketDistributor.sendToPlayersNear(sl, null, player.getX(), player.getY(), player.getZ(), 96.0D, new CancelMagicCirclePayload(player.getId()));
        }
    }

    public static void spawnArcaneCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
        spawnMagicCircleEffect(level, player.getId(), player.getX(), player.getY(), player.getZ(), new Vec3(0, 0, 1), radius, durationTicks, color, 0, isCharging);
    }

    public static void spawnArcaneCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
        spawnArcaneCircleGround(level, player, radius, durationTicks, color, false);
    }

    public static void spawnArcaneCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
        Vec3 look = player.getLookAngle().normalize();
        double dist = (radius > 4.0F) ? Math.max(2.5D, Math.min(5.0D, radius * 0.40D)) : 1.65D;
        Vec3 center = player.position().add(0, player.getEyeHeight(), 0).add(look.scale(dist));
        spawnMagicCircleEffect(level, player.getId(), center.x, center.y, center.z, look, radius, durationTicks, color, 1, isCharging);
    }

    public static void spawnArcaneCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
        spawnArcaneCircleVertical(level, player, radius, durationTicks, color, false);
    }

    public static void spawnMurimCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
        spawnMagicCircleEffect(level, player.getId(), player.getX(), player.getY(), player.getZ(), new Vec3(0, 0, 1), radius, durationTicks, color, 2, isCharging);
    }

    public static void spawnMurimCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
        spawnMurimCircleGround(level, player, radius, durationTicks, color, false);
    }

    public static void spawnMurimCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
        Vec3 look = player.getLookAngle().normalize();
        double dist = (radius > 4.0F) ? Math.max(2.5D, Math.min(5.0D, radius * 0.40D)) : 1.65D;
        Vec3 center = player.position().add(0, player.getEyeHeight(), 0).add(look.scale(dist));
        spawnMagicCircleEffect(level, player.getId(), center.x, center.y, center.z, look, radius, durationTicks, color, 3, isCharging);
    }

    public static void spawnMurimCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
        spawnMurimCircleVertical(level, player, radius, durationTicks, color, false);
    }

    public static void spawnArcaneCircleGround(ServerLevel level, double x, double y, double z, float radius, int durationTicks, int color) {
        spawnMagicCircleEffect(level, x, y, z, new Vec3(0, 0, 1), radius, durationTicks, color, 0);
    }

    public static void spawnArcaneCircleVertical(ServerLevel level, Vec3 center, Vec3 lookDir, float radius, int durationTicks, int color) {
        spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, radius, durationTicks, color, 1);
    }

    public static void spawnMurimCircleGround(ServerLevel level, double x, double y, double z, float radius, int durationTicks, int color) {
        spawnMagicCircleEffect(level, x, y, z, new Vec3(0, 0, 1), radius, durationTicks, color, 2);
    }

    public static void spawnMurimCircleVertical(ServerLevel level, Vec3 center, Vec3 lookDir, float radius, int durationTicks, int color) {
        spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, radius, durationTicks, color, 3);
    }

    public static class PlayerChargingState {
        public final String skillId;
        public final int slot;
        public final int daoSkillIdx;
        public final String displayName;
        public final int maxTicks;
        public int currentTicks;

        public PlayerChargingState(String skillId, int slot, int daoSkillIdx, String displayName, int maxTicks) {
            this.skillId = skillId;
            this.slot = slot;
            this.daoSkillIdx = daoSkillIdx;
            this.displayName = displayName;
            this.maxTicks = maxTicks;
            this.currentTicks = 0;
        }
    }

    public static final java.util.Map<java.util.UUID, PlayerChargingState> ACTIVE_CHARGES = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean isChargeableSkill(String skillId) {
        if (skillId == null) return false;
        return switch (skillId) {
            case "elemental_spear", "gravity_crush", "supernova_incineration",
                 "shonen_zoltraak", "shonen_infinite_void", "isekai_mana_burst" -> true;
            default -> false;
        };
    }

    public static int getChargeSkillMaxTicks(String skillId, int daoSkillIdx) {
        if (daoSkillIdx >= 0) {
            return switch (daoSkillIdx) {
                case 1 -> 30; // 1.5s Palma del Vacío
                case 2 -> 40; // 2.0s Dominio Núcleo Dorado
                case 3 -> 40; // 2.0s Alma Naciente
                default -> 30;
            };
        }
        return switch (skillId) {
            case "elemental_spear" -> 30; // 1.5s
            case "gravity_crush" -> 35; // 1.75s
            case "supernova_incineration" -> 45; // 2.25s
            case "shonen_zoltraak" -> 35; // 1.75s
            case "shonen_infinite_void" -> 50; // 2.5s
            case "isekai_mana_burst" -> 30; // 1.5s
            default -> 30;
        };
    }

    public static String getChargeSkillDisplayName(String skillId, int daoSkillIdx) {
        if (daoSkillIdx >= 0) {
            return switch (daoSkillIdx) {
                case 1 -> "Palma del Vacío Celestial";
                case 2 -> "Dominio del Núcleo Dorado";
                case 3 -> "Proyección del Alma Naciente";
                default -> "Arte de Cultivación Dao";
            };
        }
        return switch (skillId) {
            case "elemental_spear" -> "Lanza Elemental Arcano";
            case "gravity_crush" -> "Colapso Gravitacional";
            case "supernova_incineration" -> "Incineración Supernova";
            case "shonen_zoltraak" -> "Zoltraak (Magia Destructora)";
            case "shonen_infinite_void" -> "Vacío Infinito (Dominio)";
            case "isekai_mana_burst" -> "Estallido de Maná Isekai";
            default -> SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).nameKey() : skillId;
        };
    }

    public static void spawnChargingCirclesForSkill(ServerLevel level, ServerPlayer player, String skillId, int daoSkillIdx, int maxTicks) {
        int duration = maxTicks + 25;
        if (daoSkillIdx >= 0) {
            switch (daoSkillIdx) {
                case 1 -> {
                    // Magia de ataque Qi: Palma del Vacío -> Círculo vertical en frente al brazo
                    spawnMurimCircleVertical(level, player, 3.2F, duration, 0xFF80D8FF, true);
                }
                case 2 -> {
                    // Buff defensivo: Dominio del Núcleo Dorado -> Círculo en los pies
                    spawnMurimCircleGround(level, player, 6.0F, duration, 0xFFFFD700, true);
                }
                case 3 -> {
                    // Buff espiritual: Proyección del Alma Naciente -> Círculo en los pies
                    spawnMurimCircleGround(level, player, 5.0F, duration, 0xFFE040FB, true);
                }
            }
            return;
        }

        switch (skillId) {
            case "elemental_spear" -> {
                // Magia de ataque: Lanza Elemental -> Círculo vertical en frente al brazo
                spawnArcaneCircleVertical(level, player, 2.5F, duration, 0xFFFF7700, true);
            }
            case "gravity_crush" -> {
                // Dominio de Área: Colapso Gravitacional -> Círculo en los pies
                spawnArcaneCircleGround(level, player, 5.8F, duration, 0xFF9933FF, true);
            }
            case "supernova_incineration" -> {
                // Magia de ataque: Incineración Supernova -> Círculo vertical en frente a las manos
                spawnArcaneCircleVertical(level, player, 4.2F, duration, 0xFFFF4500, true);
            }
            case "shonen_zoltraak" -> {
                // Magia de ataque: Zoltraak -> Círculo vertical en frente al brazo
                spawnArcaneCircleVertical(level, player, 3.0F, duration, 0xFF00E5FF, true);
            }
            case "shonen_infinite_void" -> {
                // Expansión de Dominio: Vacío Infinito -> Círculo en los pies
                spawnArcaneCircleGround(level, player, 8.5F, duration, 0xFF7C4DFF, true);
            }
            case "isekai_mana_burst" -> {
                // Buff / Descarga de maná -> Círculo en los pies
                spawnArcaneCircleGround(level, player, 5.0F, duration, 0xFF00FFFF, true);
            }
        }
    }

    public static boolean startChargingSkill(ServerPlayer player, String skillId, int slot, int daoSkillIdx) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return false;
        SystemData data = data(player);

        if (daoSkillIdx < 0) {
            if (!SystemConfig.ACTIVE_SKILLS_ENABLED.get()) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.skill_activate_failed.title",
                        "awakening_system.notification.skills_disabled", 0xFFFF7666);
                return false;
            }
            if (data.isAnySystemUpdating()) {
                player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
                player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
                return false;
            }
            int cost = "architect_of_the_system".equals(data.sponsorConstellation()) ? 20 : SystemData.ACTIVE_SKILL_COST;
            if (!data.skills().containsKey(skillId) || !SkillDefinitions.isAvailable(skillId)) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.skill_activate_failed.title",
                        "awakening_system.notification.skill_activate_failed.message", 0xFFFF7666);
                return false;
            }
            if (data.activeCooldownSeconds(skillId, player.level().getGameTime()) > 0) {
                int sec = data.activeCooldownSeconds(skillId, player.level().getGameTime());
                player.displayClientMessage(Component.translatable("awakening_system.action.skill_cooldown", sec), true);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
                return false;
            }
            if (data.energy() < cost) {
                player.displayClientMessage(Component.translatable("awakening_system.action.not_enough_energy"), true);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
                return false;
            }
        }

        int maxTicks = getChargeSkillMaxTicks(skillId, daoSkillIdx);
        String displayName = getChargeSkillDisplayName(skillId, daoSkillIdx);

        spawnChargingCirclesForSkill(level, player, skillId, daoSkillIdx, maxTicks);

        PlayerChargingState state = new PlayerChargingState(skillId, slot, daoSkillIdx, displayName, maxTicks);
        ACTIVE_CHARGES.put(player.getUUID(), state);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.3F, 1.2F);
        return true;
    }

    public static void tickPlayerCharging(ServerPlayer player) {
        if (!player.isAlive() || player.hasDisconnected()) {
            cancelChargingSkill(player, false);
            return;
        }

        PlayerChargingState state = ACTIVE_CHARGES.get(player.getUUID());
        if (state == null) return;

        // Shift cancela la canalización de forma segura
        if (player.isShiftKeyDown()) {
            cancelChargingSkill(player, true);
            return;
        }

        state.currentTicks++;
        float progress = Math.min(1.0F, (float) state.currentTicks / state.maxTicks);

        // Ralentización leve mientras se canaliza poder
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, false, false, false));

        // Sonidos ascendentes cada 4 ticks
        if (state.currentTicks % 4 == 0) {
            float pitch = 0.8F + 1.0F * progress;
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, pitch);
        }

        // Partículas que convergen hacia el jugador
        ServerLevel sl = player.serverLevel();
        double ang = player.tickCount * 0.45D;
        double r = 2.2D * (1.0D - 0.7D * progress);
        double px = player.getX() + Math.cos(ang) * r;
        double pz = player.getZ() + Math.sin(ang) * r;
        var swirlParticle = (state.daoSkillIdx >= 0) ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.PORTAL;
        sl.sendParticles(swirlParticle, px, player.getY() + 0.8D, pz, 2, 0.05D, 0.05D, 0.05D, 0.01D);

        // Barra de acción interactiva en el HUD
        int percent = (int) (progress * 100);
        int filledBars = (percent * 10) / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) bar.append("▓");
            else bar.append("░");
        }
        String color = progress >= 1.0F ? "§6§l" : (progress >= 0.7F ? "§e" : "§b");
        Component barComp = Component.literal("§e§l✦ §f" + state.displayName + " " + color + "[" + bar + "] §f" + percent + "% §e§l✦");
        player.displayClientMessage(barComp, true);

        // Auto-liberación al 100% con sobrecarga
        if (state.currentTicks >= state.maxTicks) {
            releaseActiveCharge(player, true);
        }
    }

    public static void cancelChargingSkill(ServerPlayer player, boolean notify) {
        PlayerChargingState state = ACTIVE_CHARGES.remove(player.getUUID());
        if (state == null) return;

        cancelMagicCircles(player);

        if (notify) {
            player.displayClientMessage(Component.literal("§c§l✖ Canalización cancelada"), true);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.8F);
        }
    }

    public static void releaseActiveCharge(ServerPlayer player, boolean isOvercharged) {
        PlayerChargingState state = ACTIVE_CHARGES.remove(player.getUUID());
        if (state == null) return;

        cancelMagicCircles(player);

        float chargeRatio = Math.min(1.0F, (float) state.currentTicks / state.maxTicks);
        float chargeMult = isOvercharged ? 1.50F : (0.50F + 0.50F * chargeRatio);

        ServerLevel sl = player.serverLevel();
        if (isOvercharged) {
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0, 0, 0, 0);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.4F, 1.3F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.6F);
            player.displayClientMessage(Component.literal("§6§l✦✦✦ ¡LIBERACIÓN MÁXIMA (SOBRECARGA +50%)! ✦✦✦"), true);
        } else {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.2F, 1.2F);
            int pct = (int) (chargeRatio * 100);
            player.displayClientMessage(Component.literal("§b§l✦ ¡Liberación Rápida! (" + pct + "%) ✦"), true);
        }

        if (state.daoSkillIdx >= 0) {
            executeDaoSkillDirect(player, state.daoSkillIdx, chargeMult, isOvercharged);
        } else {
            activateSkill(player, state.skillId, chargeMult, isOvercharged);
        }
    }

    public static void spawnMagicCircle(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions ringParticle, net.minecraft.core.particles.ParticleOptions runeParticle, double radius) {
        spawnGeometricMagicCircle(level, x, y, z, ringParticle, runeParticle, net.minecraft.core.particles.ParticleTypes.END_ROD, radius);
    }

    public static void spawnGeometricMagicCircle(ServerLevel level, double x, double y, double z,
                                                  net.minecraft.core.particles.ParticleOptions ringParticle,
                                                  net.minecraft.core.particles.ParticleOptions runeParticle,
                                                  net.minecraft.core.particles.ParticleOptions centerParticle,
                                                  double radius) {
        int defaultColor = 0xFF9933FF;
        if (ringParticle == ParticleTypes.FLAME || ringParticle == ParticleTypes.LAVA) defaultColor = 0xFFFF5500;
        else if (ringParticle == ParticleTypes.END_ROD) defaultColor = 0xFF50E4FF;
        else if (ringParticle == ParticleTypes.PORTAL) defaultColor = 0xFF7C4DFF;
        else if (ringParticle == ParticleTypes.TOTEM_OF_UNDYING) defaultColor = 0xFFFFD700;
        spawnMagicCircleEffect(level, x, y, z, new Vec3(0, 0, 1), (float) radius, 45, defaultColor, 0);

        // 1. Concentric Runic Rings
        spawnShockwaveRing(level, x, y + 0.08D, z, ringParticle, radius, 48, 0.02D);
        spawnShockwaveRing(level, x, y + 0.08D, z, ringParticle, radius * 0.60D, 32, 0.01D);
        spawnShockwaveRing(level, x, y + 0.08D, z, ringParticle, radius * 0.25D, 16, 0.01D);

        // 2. Geometric Octagram (Two Intersecting Squares in Particle Vectors)
        if (runeParticle != null) {
            double[] angles = new double[8];
            double[] vx = new double[8];
            double[] vz = new double[8];
            for (int i = 0; i < 8; i++) {
                angles[i] = i * (Math.PI / 4.0D);
                vx[i] = x + Math.cos(angles[i]) * (radius * 0.90D);
                vz[i] = z + Math.sin(angles[i]) * (radius * 0.90D);
                // Vertex rune flare
                level.sendParticles(runeParticle, vx[i], y + 0.15D, vz[i], 2, 0.05D, 0.05D, 0.05D, 0.01D);
            }

            // Square 1: 0 -> 2 -> 4 -> 6 -> 0
            int[] sq1 = {0, 2, 4, 6, 0};
            for (int s = 0; s < 4; s++) {
                drawParticleLine(level, vx[sq1[s]], y + 0.08D, vz[sq1[s]], vx[sq1[s + 1]], y + 0.08D, vz[sq1[s + 1]], runeParticle, 7);
            }
            // Square 2: 1 -> 3 -> 5 -> 7 -> 1
            int[] sq2 = {1, 3, 5, 7, 1};
            for (int s = 0; s < 4; s++) {
                drawParticleLine(level, vx[sq2[s]], y + 0.08D, vz[sq2[s]], vx[sq2[s + 1]], y + 0.08D, vz[sq2[s + 1]], runeParticle, 7);
            }

            // Radial spokes connecting center ring to outer vertices
            for (int i = 0; i < 8; i++) {
                double inX = x + Math.cos(angles[i]) * (radius * 0.25D);
                double inZ = z + Math.sin(angles[i]) * (radius * 0.25D);
                drawParticleLine(level, inX, y + 0.08D, inZ, vx[i], y + 0.08D, vz[i], ringParticle, 5);
            }
        }

        // 3. Central Column of Ascending Arcane Light
        if (centerParticle != null) {
            for (double dy = 0.0D; dy <= 2.2D; dy += 0.30D) {
                level.sendParticles(centerParticle, x, y + dy, z, 2, 0.08D, 0.05D, 0.08D, 0.02D);
            }
        }
    }

    public static void drawParticleLine(ServerLevel level, double x1, double y1, double z1, double x2, double y2, double z2,
                                        net.minecraft.core.particles.ParticleOptions particle, int steps) {
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / (double) steps;
            double px = x1 + t * (x2 - x1);
            double py = y1 + t * (y2 - y1);
            double pz = z1 + t * (z2 - z1);
            level.sendParticles(particle, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnVerticalMagicCircle(ServerLevel level, Vec3 center, Vec3 lookDir,
                                                net.minecraft.core.particles.ParticleOptions ringParticle,
                                                net.minecraft.core.particles.ParticleOptions runeParticle,
                                                double radius) {
        int defaultColor = 0xFF50E4FF;
        if (ringParticle == ParticleTypes.FLAME) defaultColor = 0xFFFF7700;
        else if (ringParticle == ParticleTypes.ELECTRIC_SPARK) defaultColor = 0xFF00E5FF;
        spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, (float) radius, 40, defaultColor, 1);

        Vec3 normLook = lookDir.normalize();
        Vec3 upRef = Math.abs(normLook.y) > 0.95D ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = normLook.cross(upRef).normalize();
        Vec3 up = right.cross(normLook).normalize();

        // Outer Ring
        for (int i = 0; i < 36; i++) {
            double angle = i * (Math.PI * 2.0D / 36.0D);
            Vec3 pos = center.add(right.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
            level.sendParticles(ringParticle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
        // Middle Ring
        for (int i = 0; i < 24; i++) {
            double angle = i * (Math.PI * 2.0D / 24.0D);
            Vec3 pos = center.add(right.scale(Math.cos(angle) * radius * 0.6D)).add(up.scale(Math.sin(angle) * radius * 0.6D));
            level.sendParticles(ringParticle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }

        // Hexagram Star
        if (runeParticle != null) {
            Vec3[] verts = new Vec3[6];
            for (int i = 0; i < 6; i++) {
                double a = i * (Math.PI / 3.0D);
                verts[i] = center.add(right.scale(Math.cos(a) * radius * 0.85D)).add(up.scale(Math.sin(a) * radius * 0.85D));
            }
            draw3DParticleLine(level, verts[0], verts[2], runeParticle, 6);
            draw3DParticleLine(level, verts[2], verts[4], runeParticle, 6);
            draw3DParticleLine(level, verts[4], verts[0], runeParticle, 6);
            draw3DParticleLine(level, verts[1], verts[3], runeParticle, 6);
            draw3DParticleLine(level, verts[3], verts[5], runeParticle, 6);
            draw3DParticleLine(level, verts[5], verts[1], runeParticle, 6);
        }
    }

    public static void draw3DParticleLine(ServerLevel level, Vec3 start, Vec3 end, net.minecraft.core.particles.ParticleOptions particle, int steps) {
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / (double) steps;
            Vec3 p = start.add(end.subtract(start).scale(t));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnImpactCrater(ServerLevel level, double x, double y, double z, double radius) {
        spawnShockwaveRing(level, x, y + 0.1D, z, ParticleTypes.POOF, radius, 28, 0.12D);
        spawnShockwaveRing(level, x, y + 0.1D, z, ParticleTypes.CAMPFIRE_COSY_SMOKE, radius * 0.7D, 18, 0.08D);
        spawnShockwaveRing(level, x, y + 0.1D, z, ParticleTypes.CRIT, radius * 0.5D, 20, 0.15D);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5D, z, 3, 0.5D, 0.2D, 0.5D, 0.1D);
    }

    public static void renderCultivationQiAura(ServerPlayer player, SystemData data, ServerLevel sl, boolean isMeditating) {
        int realm = data.dantianRealm();
        if (realm <= 0) return;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        if (isMeditating) {
            // Mandalas y matrices de meditación espiritual según el Reino
            if (player.tickCount % 4 == 0) {
                double t = (player.tickCount * 0.20D);
                double r = 1.35D;
                // Anillo de meditación giratorio
                for (int i = 0; i < 3; i++) {
                    double angle = t + (i * (Math.PI * 2.0D / 3.0D));
                    double rx = Math.cos(angle) * r;
                    double rz = Math.sin(angle) * r;
                    switch (realm) {
                        case 1 -> sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px + rx, py + 0.15D, pz + rz, 1, 0, 0.02D, 0, 0.01D);
                        case 2 -> sl.sendParticles(ParticleTypes.GLOW, px + rx, py + 0.20D, pz + rz, 1, 0, 0.03D, 0, 0.01D);
                        case 3 -> sl.sendParticles(ParticleTypes.WAX_ON, px + rx, py + 0.25D, pz + rz, 1, 0, 0.04D, 0, 0.01D);
                        case 4 -> sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px + rx, py + 0.25D, pz + rz, 1, 0, 0.04D, 0, 0.01D);
                        case 5 -> sl.sendParticles(ParticleTypes.DRAGON_BREATH, px + rx, py + 0.30D, pz + rz, 1, 0, 0.05D, 0, 0.02D);
                        case 6 -> sl.sendParticles(ParticleTypes.SCULK_SOUL, px + rx, py + 0.30D, pz + rz, 1, 0, 0.05D, 0, 0.02D);
                        case 7 -> sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px + rx, py + 0.35D, pz + rz, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                        case 8 -> sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + rx, py + 0.40D, pz + rz, 2, 0.08D, 0.06D, 0.08D, 0.05D);
                        case 9 -> {
                            sl.sendParticles(ParticleTypes.END_ROD, px + rx, py + 0.45D, pz + rz, 2, 0.05D, 0.08D, 0.05D, 0.02D);
                            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + rx, py + 0.20D, pz + rz, 1, 0, 0.05D, 0, 0.02D);
                        }
                    }
                }

                // Qi ascendente hacia el Dantian del cultivador
                if (player.tickCount % 8 == 0) {
                    sl.sendParticles(ParticleTypes.ENCHANT, px, py + 0.7D, pz, 4, 0.4D, 0.3D, 0.4D, 0.05D);
                    if (realm >= 3) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py + 0.8D, pz, 2, 0.25D, 0.25D, 0.25D, 0.02D);
                    }
                    if (realm >= 7) {
                        sl.sendParticles(ParticleTypes.END_ROD, px, py + 0.9D, pz, 2, 0.2D, 0.3D, 0.2D, 0.02D);
                    }
                }
            }
        } else {
            // Aura de Qi pasiva en movimiento / combate según Reino
            if (player.tickCount % 6 == 0) {
                switch (realm) {
                    case 1 -> { // Condensación de Qi: Sutil vapor de Qi esmeralda
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px + (player.getRandom().nextDouble() - 0.5D) * 0.7D,
                                py + 0.2D + player.getRandom().nextDouble() * 0.6D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.7D, 1, 0, 0.02D, 0, 0.01D);
                    }
                    case 2 -> { // Establecimiento de Cimientos: Marea de Qi cian y condensación espiritual
                        sl.sendParticles(ParticleTypes.GLOW, px + (player.getRandom().nextDouble() - 0.5D) * 0.8D,
                                py + 0.3D + player.getRandom().nextDouble() * 0.8D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.8D, 1, 0, 0.03D, 0, 0.01D);
                    }
                    case 3 -> { // Núcleo Dorado: Radiación solar dorada del Dan
                        sl.sendParticles(ParticleTypes.WAX_ON, px + (player.getRandom().nextDouble() - 0.5D) * 0.9D,
                                py + 0.4D + player.getRandom().nextDouble() * 0.9D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.9D, 1, 0, 0.03D, 0, 0.01D);
                        if (player.tickCount % 12 == 0) {
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py + 0.8D, pz, 1, 0.3D, 0.4D, 0.3D, 0.02D);
                        }
                    }
                    case 4 -> { // Alma Naciente: Fuego de Alma astral y orbes espirituales
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px + (player.getRandom().nextDouble() - 0.5D) * 0.85D,
                                py + 0.5D + player.getRandom().nextDouble() * 1.0D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.85D, 1, 0, 0.03D, 0, 0.01D);
                        sl.sendParticles(ParticleTypes.ENCHANT, px, py + 1.0D, pz, 2, 0.35D, 0.35D, 0.35D, 0.05D);
                    }
                    case 5 -> { // Transformación de Alma: Nebulosa cósmica púrpura
                        sl.sendParticles(ParticleTypes.DRAGON_BREATH, px + (player.getRandom().nextDouble() - 0.5D) * 0.9D,
                                py + 0.5D + player.getRandom().nextDouble() * 1.1D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.9D, 2, 0.05D, 0.05D, 0.05D, 0.01D);
                        sl.sendParticles(ParticleTypes.PORTAL, px + (player.getRandom().nextDouble() - 0.5D) * 0.7D,
                                py + 0.6D, pz + (player.getRandom().nextDouble() - 0.5D) * 0.7D, 1, 0, 0.05D, 0, 0.02D);
                    }
                    case 6 -> { // Refinamiento del Vacío: Distorsión espacial y zarcillos del vacío
                        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, px + (player.getRandom().nextDouble() - 0.5D) * 0.9D,
                                py + 0.6D + player.getRandom().nextDouble() * 1.0D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 0.9D, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                        sl.sendParticles(ParticleTypes.SCULK_SOUL, px + (player.getRandom().nextDouble() - 0.5D) * 0.7D,
                                py + 0.4D, pz + (player.getRandom().nextDouble() - 0.5D) * 0.7D, 1, 0, 0.03D, 0, 0.01D);
                    }
                    case 7 -> { // Integración con el Dao: Pétalos de Yin-Yang celestiales
                        sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px + (player.getRandom().nextDouble() - 0.5D) * 1.0D,
                                py + 0.8D + player.getRandom().nextDouble() * 0.8D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 1.0D, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                        sl.sendParticles(ParticleTypes.END_ROD, px + (player.getRandom().nextDouble() - 0.5D) * 0.6D,
                                py + 0.7D, pz + (player.getRandom().nextDouble() - 0.5D) * 0.6D, 1, 0, 0.04D, 0, 0.01D);
                    }
                    case 8 -> { // Gran Mahayana: Halo áureo divino y destellos celestes
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + (player.getRandom().nextDouble() - 0.5D) * 1.1D,
                                py + 0.7D + player.getRandom().nextDouble() * 0.9D,
                                pz + (player.getRandom().nextDouble() - 0.5D) * 1.1D, 2, 0.05D, 0.08D, 0.05D, 0.03D);
                        sl.sendParticles(ParticleTypes.END_ROD, px, py + 1.2D, pz, 1, 0.3D, 0.4D, 0.3D, 0.02D);
                    }
                    case 9 -> { // Inmortal Verdadero del Dao: Corona Inmortal flotando sobre la cabeza y aura estelar
                        double headY = player.getEyeY() + 0.45D;
                        double haloAngle = (player.tickCount * 0.25D);
                        double hx = Math.cos(haloAngle) * 0.45D;
                        double hz = Math.sin(haloAngle) * 0.45D;
                        sl.sendParticles(ParticleTypes.END_ROD, px + hx, headY, pz + hz, 1, 0, 0.01D, 0, 0.01D);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px - hx, headY, pz - hz, 1, 0, 0.02D, 0, 0.01D);
                        if (player.tickCount % 12 == 0) {
                            sl.sendParticles(ParticleTypes.FLASH, px, headY + 0.1D, pz, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    public static boolean activateSkill(ServerPlayer player, String skillId) {
        if (skillId == null || skillId.isEmpty()) {
            SystemData d = data(player);
            skillId = d.equippedSkill();
        }
        if (isChargeableSkill(skillId)) {
            PlayerChargingState active = ACTIVE_CHARGES.get(player.getUUID());
            if (active != null) {
                if (skillId.equals(active.skillId)) {
                    if (active.currentTicks >= Math.max(8, (int) (active.maxTicks * 0.35F))) {
                        releaseActiveCharge(player, false);
                        return true;
                    } else {
                        player.displayClientMessage(Component.literal("§e✦ ¡Canalizando poder! Aguarda un instante..."), true);
                        return false;
                    }
                } else {
                    cancelChargingSkill(player, false);
                }
            }
            return startChargingSkill(player, skillId, -1, -1);
        }
        return activateSkill(player, skillId, 1.0F, false);
    }

    public static boolean activateSkill(ServerPlayer player, String skillId, float chargeMult, boolean isOvercharged) {
        if (!SystemConfig.ACTIVE_SKILLS_ENABLED.get()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_activate_failed.title",
                    "awakening_system.notification.skills_disabled", 0xFFFF7666);
            return false;
        }
        SystemData data = data(player);
        if (data.isAnySystemUpdating()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
            player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
            return false;
        }

        if (skillId == null || skillId.isEmpty()) {
            skillId = data.equippedSkill();
        }
        if ((skillId == null || skillId.isEmpty()) && (data.skills().containsKey("shadow_monarch") || data.skills().containsKey("unique_absolute_extraction"))) {
            skillId = data.skills().containsKey("unique_absolute_extraction") ? "unique_absolute_extraction" : "shadow_monarch";
        }

        // Validación de exclusividad de Rango Único (1 solo jugador por mundo/servidor)
        if (UniqueRankManager.isUniqueSkill(skillId)) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                UniqueRankManager uniqueMgr = UniqueRankManager.get(server);
                if (!uniqueMgr.canClaimSkill(skillId, player.getUUID())) {
                    String holder = uniqueMgr.getSkillHolderName(skillId);
                    AwakeningSystemMod.notify(player, "awakening_system.notification.unique_locked.title",
                            "awakening_system.notification.unique_skill_claimed", 0xFFFF4444, holder != null ? holder : "Otro cazador");
                    return false;
                }
                uniqueMgr.claimSkill(skillId, player);
            }
        }

        boolean isStoringShadows = player.isShiftKeyDown() && ("shadow_monarch".equals(skillId) || "unique_absolute_extraction".equals(skillId));
        if (isStoringShadows) {
            // Guardar sombras en el dominio NO requiere cooldown ni gasta maná ni entra en enfriamiento
            int stored = storePlayerShadows(player);
            if (stored <= 0) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_stored.title",
                        "awakening_system.notification.shadow_no_souls.desc", 0xFFFF7666);
            }
            return true;
        }

        if (!data.activateSkill(skillId, player.level().getGameTime())) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_activate_failed.title",
                    "awakening_system.notification.skill_activate_failed.message", 0xFFFF7666);
            return false;
        }

        int level = data.skills().getOrDefault(skillId, 1);
        boolean evolved = level >= 6;
        float treeSkillMult = (ClassSkillTree.getNode(skillId) != null) ? (1.0F + (Math.min(3, Math.max(1, level)) - 1) * 0.35F) : 1.0F;
        int treeDurationBonus = (ClassSkillTree.getNode(skillId) != null) ? (int)((Math.min(3, Math.max(1, level)) - 1) * 0.25F * 200) : 0;
        if (player.level() instanceof ServerLevel slMaster && ClassSkillTree.getNode(skillId) != null && level >= 3) {
            slMaster.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0, 0, 0, 0);
            slMaster.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.6D, 1.0D, 0.6D, 0.08D);
            slMaster.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.6F);
        }
        int str = data.strength();
        int intel = data.intelligence();
        int agi = data.agility();
        int vit = data.vitality();
        int will = data.willpower();
        int per = data.perception();

        switch (skillId) {
            case "combat_instinct" -> {
                int duration = 200 + str * 2;
                int amp = evolved ? (str >= 40 ? 2 : 1) : 0;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amp, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, agi >= 30 ? 1 : 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 2.5D, 16, 0.08D);
                    spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.2D, 12);
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getEyeY() + 0.2D, player.getZ(), 6, 0.25D, 0.1D, 0.25D, 0.02D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.2F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8F, 1.7F);
                }
            }
            case "stone_strike" -> {
                int duration = 300 + str * 3;
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, evolved ? 2 : 1, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 3.5D);
                    sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.5D, player.getZ(), 35, 0.4D, 0.3D, 0.4D, 0.15D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.4F, 0.6F);
                }
            }
            case "enduring_flesh" -> {
                int regenDur = 140 + vit * 2;
                int resDur = 180 + vit * 2;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDur, evolved ? 2 : 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, resDur, evolved ? 1 : 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.HEART, 1.8D, 3.0D, 4, 10);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.HAPPY_VILLAGER, 2.5D, 18, 0.05D);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.3F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);
                }
            }
            case "windrunner" -> {
                int duration = 260 + agi * 3;
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, evolved ? 2 : 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, evolved ? 1 : 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CLOUD, 2.2D, 3.5D, 4, 12);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 2.8D, 16, 0.12D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.3F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.1F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.2F, 1.3F);
                }
            }
            case "deep_breath" -> {
                int duration = 400 + vit * 4;
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, duration - 100, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.BUBBLE_COLUMN_UP, 2.5D, 3, 14);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SPLASH, 3.0D, 24, 0.08D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.3F, 1.1F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.1F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DOLPHIN_PLAY, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            case "ore_sense" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400 + per * 4, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200 + per * 3, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0D, player.getZ(), ParticleTypes.GLOW, 4.0D, 28, 0.04D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0D, player.getZ(), ParticleTypes.END_ROD, 7.0D, 36, 0.06D);
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.GLOW, 3.0D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.1F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.2F, 1.5F);
                }
            }
            case "shadow_step" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double targetDist = (evolved ? 12.0D : 8.0D) + Math.min(8.0D, agi * 0.1D);
                double targetX = player.getX() + look.x * targetDist;
                double targetY = player.getY() + look.y * (targetDist * 0.4D);
                double targetZ = player.getZ() + look.z * targetDist;
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SQUID_INK, 1.5D, 2.5D, 3, 10);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SCULK_SOUL, 2.0D, 16, 0.06D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                }
                net.minecraft.world.phys.Vec3 safe = TeleportHelper.findSafeTeleportPos(player.level(), player, targetX, targetY, targetZ, player.position());
                player.teleportTo(safe.x, safe.y, safe.z);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, safe.x, safe.y + 0.1D, safe.z, ParticleTypes.SOUL_FIRE_FLAME, 2.5D, 20, 0.08D);
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, safe.x, safe.y + 1.0D, safe.z, 25, 0.4D, 0.6D, 0.4D, 0.05D);
                    sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 0.8F);
                    sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.1F);
                }
            }
            case "dimensional_blink" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double dist = (evolved ? 16.0D : 10.0D) + Math.min(10.0D, agi * 0.12D);
                double targetX = player.getX() + look.x * dist;
                double targetY = player.getY() + look.y * (dist * 0.5D);
                double targetZ = player.getZ() + look.z * dist;
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0D, player.getZ(), ParticleTypes.PORTAL, 2.5D, 24, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.3F, 1.8F);
                }
                net.minecraft.world.phys.Vec3 safe = TeleportHelper.findSafeTeleportPos(player.level(), player, targetX, targetY, targetZ, player.position());
                player.teleportTo(safe.x, safe.y, safe.z);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, safe.x, safe.y + 1.0D, safe.z, ParticleTypes.REVERSE_PORTAL, 3.0D, 30, 0.12D);
                    sl.sendParticles(ParticleTypes.FLASH, safe.x, safe.y + 1.0D, safe.z, 1, 0, 0, 0, 0);
                    sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 1.1F);
                    sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.4F, 1.4F);
                }
            }
            case "seismic_roar" -> {
                double radius = (evolved ? 9.0D : 6.0D) + Math.min(4.0D, str * 0.05D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = 8.0F + level * 2.0F + (float) str * 0.20F;
                for (var e : enemies) {
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 1.8D, 0.65D, dz / dist * 1.8D);
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), radius * 0.6D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.EXPLOSION, radius, 24, 0.18D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.1F, 0.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
                }
            }
            case "blaze_ignition" -> {
                double radius = (evolved ? 10.0D : 7.0D) + Math.min(5.0D, intel * 0.06D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = 6.0F + level * 1.5F + (float) intel * 0.22F;
                for (var e : enemies) {
                    e.setRemainingFireTicks(100 + level * 30 + intel * 2);
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, radius * 0.5D, 4.5D, 5, 14);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.LAVA, radius, 32, 0.12D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_WARN, SoundSource.PLAYERS, 1.1F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 0.7F);
                }
            }
            case "spectral_shift" -> {
                int dur = evolved ? 160 : 120;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 2, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 2.5D, 3, 16);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.SOUL, 3.0D, 20, 0.05D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.3F, 1.2F);
                }
            }
            case "magnetic_pull" -> {
                double radius = (evolved ? 18.0D : 12.0D) + Math.min(12.0D, per * 0.15D);
                var aabb = player.getBoundingBox().inflate(radius);
                var items = player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, aabb);
                for (var item : items) {
                    item.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                }
                var xps = player.level().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, aabb);
                for (var xp : xps) {
                    xp.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.REVERSE_PORTAL, radius * 0.5D, 36, -0.15D);
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, ParticleTypes.GLOW, 2.5D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.2F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.1F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.8F);
                }
            }
            case "piercing_shot" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(
                        player.level(), player, new ItemStack(Items.ARROW), null);
                arrow.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
                float speed = (evolved ? 3.8F : 2.8F) + (float) agi * 0.015F;
                arrow.shoot(look.x, look.y, look.z, speed, 0.15F);
                arrow.setBaseDamage((evolved ? 14.0D : 9.0D) + per * 0.15D + str * 0.10D);
                arrow.setCritArrow(true);
                player.level().addFreshEntity(arrow);
                if (player.level() instanceof ServerLevel sl) {
                    spawnHelixBeam(sl, player.getEyePosition(), look, 16.0D, ParticleTypes.CRIT, ParticleTypes.END_ROD, 0.3D);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX() + look.x * 1.5D, player.getEyeY(), player.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.1F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.3F, 1.4F);
                }
            }
            case "web_snare" -> {
                double radius = (evolved ? 8.0D : 5.0D) + Math.min(4.0D, intel * 0.05D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = (evolved ? 8.0F : 5.0F) + (float) intel * 0.12F;
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, evolved ? 140 : 100, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, evolved ? 140 : 100, 1));
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ITEM_COBWEB, ParticleTypes.WHITE_ASH, radius * 0.8D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.ITEM_COBWEB, radius, 30, 0.06D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 1.2F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BAMBOO_WOOD_BREAK, SoundSource.PLAYERS, 1.3F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.1F, 0.9F);
                }
            }
            case "kinetic_blast" -> {
                double radius = (evolved ? 9.0D : 6.0D) + Math.min(4.0D, str * 0.05D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = 9.0F + level * 2.5F + (float) str * 0.25F;
                for (var e : enemies) {
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 2.4D, 0.75D, dz / dist * 2.4D);
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.EXPLOSION, radius * 0.7D, 24, 0.2D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.SWEEP_ATTACK, radius, 32, 0.15D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.4F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.4F, 1.1F);
                }
            }
            case "tidal_surge" -> {
                double radius = (evolved ? 10.0D : 7.0D) + Math.min(4.0D, intel * 0.05D);
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 300, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, true, true));
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float baseDmg = (evolved ? 10.0F : 7.0F) + (float) intel * 0.22F;
                for (var e : enemies) {
                    float dmg = (e.isInWaterOrRain() || player.isInWaterOrRain()) ? (baseDmg + 8.0F) : baseDmg;
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SPLASH, radius * 0.5D, 36, 0.12D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SPLASH, radius, 48, 0.16D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.BUBBLE, radius * 0.4D, 3.5D, 4, 14);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.4F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2F, 1.1F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.3F, 0.7F);
                }
            }
            case "rulers_authority" -> {
                double radius = (evolved ? 13.0D : 9.0D) + Math.min(5.0D, intel * 0.06D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.phys.Vec3 pullPoint = player.position().add(look.x * 3.0D, 1.0D, look.z * 3.0D);
                float dmg = (evolved ? 20.0F : 15.0F) + (float) intel * 0.25F + (float) str * 0.15F;
                for (var e : enemies) {
                    double dx = pullPoint.x - e.getX();
                    double dz = pullPoint.z - e.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 2.0D, -1.8D, dz / dist * 2.0D);
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, radius * 0.7D, 40, 0.12D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 3.0D, 4.5D, 5, 14);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX() + look.x * 2.0D, player.getEyeY(), player.getZ() + look.z * 2.0D, 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.3F, 1.1F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.1F, 1.8F);
                }
            }
            case "asrahan_mana_blade" -> {
                int duration = evolved ? 420 : 300;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, evolved ? 2 : 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANTED_HIT, ParticleTypes.GLOW, 2.5D);
                    spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.5D, 14);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.3F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.2F, 1.2F);
                }
            }
            case "electrification" -> {
                int duration = evolved ? 320 : 220;
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, evolved ? 2 : 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 1, false, true, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 2.0D, 3.5D, 5, 16);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 3.5D, 32, 0.25D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.1F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 1.4F, 1.6F);
                }
            }
            case "nano_blade_overdrive" -> {
                double radius = (evolved ? 9.0D : 7.0D) + Math.min(3.0D, agi * 0.04D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = (evolved ? 18.0F : 13.0F) + (float) str * 0.20F + (float) agi * 0.18F;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 1.6D, 0.4D, dz / dist * 1.6D);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SWEEP_ATTACK, radius * 0.6D, 3, 16);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.CRIT, radius, 36, 0.15D);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.6D, 0.5D, 0.2D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.1F, 1.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.2F, 1.3F);
                }
            }
            case "dragon_dive" -> {
                player.setDeltaMovement(0, 1.45D, 0);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 90, 3, false, false, false));
                double radius = (evolved ? 10.0D : 7.0D) + Math.min(4.0D, str * 0.06D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float dmg = (evolved ? 18.0F : 13.0F) + (float) str * 0.25F;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 2.2D, 0.6D, dz / dist * 2.2D);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.DRAGON_BREATH, radius * 0.6D, 32, 0.12D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.EXPLOSION, radius, 24, 0.18D);
                    spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), radius * 0.5D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.4F, 1.1F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.3F, 0.9F);
                }
            }
            case "black_fire_wave" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double range = 14.0D + Math.min(8.0D, intel * 0.1D);
                float dmg = (evolved ? 16.0F : 12.0F) + (float) intel * 0.22F;
                if (player.level() instanceof ServerLevel sl) {
                    spawnHelixBeam(sl, player.getEyePosition(), look, range, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.DRAGON_BREATH, 0.6D);
                    for (double d = 1.0D; d <= range; d += 1.2D) {
                        double fx = player.getX() + look.x * d;
                        double fy = player.getEyeY() + look.y * d - 0.2D;
                        double fz = player.getZ() + look.z * d;
                        var hitAABB = new net.minecraft.world.phys.AABB(fx - 1.4D, fy - 1.4D, fz - 1.4D, fx + 1.4D, fy + 1.4D, fz + 1.4D);
                        var hitEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, hitAABB,
                                e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                        for (var target : hitEnemies) {
                            target.setRemainingFireTicks(140);
                            target.hurt(player.damageSources().playerAttack(player), dmg);
                        }
                    }
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.6F);
                }
            }
            case "caregiver_blessing" -> {
                float healAmt = (evolved ? 14.0F : 10.0F) + (float) vit * 0.15F;
                player.heal(healAmt);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 320, 0, false, true, true));
                var aabb = player.getBoundingBox().inflate(12.0D);
                var allies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && (e instanceof net.minecraft.world.entity.TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || e instanceof ServerPlayer || e.getTags().contains("shadow_soldier")));
                for (var ally : allies) {
                    ally.heal(healAmt);
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1));
                    ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.HEART, 4.0D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 2.5D, 4.0D, 4, 12);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.3F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.3F);
                }
            }

            // === HABILIDADES DE RANGO ÚNICO (EXCLUSIVAS 1 POR SERVIDOR) ===
            case "shadow_monarch", "unique_absolute_extraction" -> {
                boolean isUnique = "unique_absolute_extraction".equals(skillId);
                double searchRadius = (evolved || isUnique) ? 48.0D : 32.0D;
                ServerLevel sl = (ServerLevel) player.level();

                var nearbySouls = sl.getEntitiesOfClass(com.soybe.awakeningsystem.entity.ShadowSoulEntity.class, player.getBoundingBox().inflate(searchRadius),
                        s -> s.isAlive() && (s.getOwnerUUID() == null || s.getOwnerUUID().equals(player.getUUID())));

                if (!nearbySouls.isEmpty()) {
                    int maxExtract = (isUnique || evolved) ? 8 : 4;
                    int extracted = 0;
                    for (var soul : nearbySouls) {
                        if (extracted >= maxExtract) break;

                        int currentAttempt = soul.getExtractionAttempts() + 1;
                        String entityId = soul.getMobEntityTypeId().toLowerCase();
                        if (entityId.contains("celestial_dragon")) {
                            player.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
                            soul.discard();
                            continue;
                        }
                        int mobTier = 3;
                        if (entityId.contains("boss") || entityId.contains("dragon") || entityId.contains("wither") || entityId.contains("warden") || entityId.contains("baran") || entityId.contains("igris")) {
                            mobTier = 9;
                        } else if (entityId.contains("golem") || entityId.contains("ravager") || entityId.contains("evoker")) {
                            mobTier = 6;
                        }

                        double bonus = (isUnique ? 40.0 : (evolved ? 25.0 : 0.0));
                        double chance = (intel * 0.35 + will * 0.35 + bonus) - (mobTier * 6.0) + 35.0;
                        chance = Math.max(15.0, Math.min(95.0, chance));

                        if (player.getRandom().nextDouble() * 100.0 < chance) {
                            summonShadowFromSoul(player, soul);
                            soul.discard();
                            extracted++;
                            player.displayClientMessage(Component.translatable("awakening_system.shadow.arise_chat_success", soul.getMobName()), true);
                        } else {
                            soul.incrementExtractionAttempts();
                            if (currentAttempt < 3) {
                                sl.sendParticles(ParticleTypes.SCULK_SOUL, soul.getX(), soul.getY() + 0.4D, soul.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.05D);
                                sl.playSound(null, soul.getX(), soul.getY(), soul.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.9F);
                                soul.setCustomName(Component.literal("§8§l✦ ALMA: §c" + soul.getMobName() + " §4[" + currentAttempt + "/3] ✦"));
                                player.displayClientMessage(Component.translatable("awakening_system.shadow.arise_attempt_fail", currentAttempt, soul.getMobName()), true);
                            } else {
                                sl.sendParticles(ParticleTypes.LARGE_SMOKE, soul.getX(), soul.getY() + 0.5D, soul.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.05D);
                                sl.playSound(null, soul.getX(), soul.getY(), soul.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 0.7F);
                                player.displayClientMessage(Component.translatable("awakening_system.shadow.arise_final_fail", soul.getMobName()), true);
                                soul.discard();
                            }
                        }
                    }

                    var nearbyEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(searchRadius * 0.85D),
                            e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier") && !e.getType().getCategory().isFriendly());
                    for (var enemy : nearbyEnemies) {
                        double dx = enemy.getX() - player.getX();
                        double dz = enemy.getZ() - player.getZ();
                        double d = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        enemy.setDeltaMovement(dx / d * 1.8D, 0.5D, dz / d * 1.8D);
                    }

                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, false, false));
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SCULK_SOUL, 6.0D, 32, 0.08D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 12.0D, 48, 0.12D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SQUID_INK, 18.0D, 64, 0.15D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.5D, 5.0D, 6, 14);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.2F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.5F);

                    AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_extracted.title",
                            "awakening_system.notification.shadow_extracted.message", 0xFF9966FF, String.valueOf(extracted));
                } else if (data.storedShadowCount() > 0) {
                    int toDeploy = (isUnique || evolved) ? 4 : 2;
                    deployPlayerShadows(player, toDeploy);
                } else {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_no_souls.title",
                            "awakening_system.notification.shadow_no_souls.desc", 0xFFFF7666);
                }
            }
            case "unique_rulers_authority_prime" -> {
                double radius = 18.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier") && !e.getType().getCategory().isFriendly());
                float dmg = 26.0F + (float) str * 0.32F + (float) intel * 0.28F;
                for (var e : enemies) {
                    e.setDeltaMovement(0, 1.5D, 0);
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SONIC_BOOM, 6.0D, 16, 0.1D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SONIC_BOOM, 12.0D, 24, 0.15D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 18.0D, 64, 0.2D);
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.ELECTRIC_SPARK, 5.0D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.3F, 0.6F);
                }
            }
            case "unique_fourth_wall_infinite" -> {
                player.removeAllEffects();
                player.heal(player.getMaxHealth() * 0.5F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 4, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 320, 4, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 2, false, false, false));
                double radius = 12.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly());
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3));
                    e.hurt(player.damageSources().playerAttack(player), 14.0F + (float) will * 0.35F);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 6.0D, 5, 24);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.ENCHANT, 10.0D, 48, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 3, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.2D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.6F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.2F);
                }
            }
            case "unique_dragon_breath_annihilation" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                float dmg = 30.0F + (float) str * 0.38F + (float) intel * 0.25F;
                if (player.level() instanceof ServerLevel sl) {
                    spawnHelixBeam(sl, player.getEyePosition(), look, 26.0D, ParticleTypes.DRAGON_BREATH, ParticleTypes.SOUL_FIRE_FLAME, 0.9D);
                    for (double d = 1.0D; d <= 26.0D; d += 0.8D) {
                        double fx = player.getX() + look.x * d;
                        double fy = player.getEyeY() + look.y * d - 0.2D;
                        double fz = player.getZ() + look.z * d;
                        sl.sendParticles(ParticleTypes.FLAME, fx, fy, fz, 12, 0.3D, 0.3D, 0.3D, 0.05D);
                        var hitAABB = new net.minecraft.world.phys.AABB(fx - 1.6D, fy - 1.6D, fz - 1.6D, fx + 1.6D, fy + 1.6D, fz + 1.6D);
                        var hitEnemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, hitAABB,
                                e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                        for (var target : hitEnemies) {
                            target.setRemainingFireTicks(220);
                            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                            target.hurt(player.damageSources().playerAttack(player), dmg);
                            target.setDeltaMovement(look.x * 1.6D, 0.5D, look.z * 1.6D);
                        }
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, 8.0D, 24, 0.15D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX() + look.x * 2.0D, player.getEyeY() + look.y * 2.0D, player.getZ() + look.z * 2.0D, 3, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 1.5F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.3F, 0.8F);
                }
            }
            case "isekai_predator" -> {
                double radius = 12.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float drainHp = 0.0F;
                for (var e : enemies) {
                    float dmg = 24.0F + (float) intel * 0.25F;
                    e.hurt(player.damageSources().magic(), dmg);
                    drainHp += dmg * 0.35F;
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }
                player.heal(Math.min(drainHp, 40.0F));
                data.restoreEnergy(45);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 140, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.REVERSE_PORTAL, radius * 0.6D, 40, -0.2D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.0D, 4.0D, 5, 14);
                    sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.2D, 1.0D, 1.2D, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_DEATH, SoundSource.PLAYERS, 1.3F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.4F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
                }
            }
            case "isekai_dimensional_slash" -> {
                double radius = 16.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float atomicDmg = 120.0F + (float) str * 0.45F + (float) intel * 0.40F;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), atomicDmg);
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 3.0D, 1.2D, dz / dist * 3.0D);
                    e.setRemainingFireTicks(160);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 8.0D, 6, 28);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SONIC_BOOM, 10.0D, 20, 0.2D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, 16.0D, 32, 0.25D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 5, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4F, 0.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 0.6F);
                }
            }
            case "isekai_dragon_roar" -> {
                double radius = 18.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float roarDmg = 45.0F + (float) will * 0.35F + (float) str * 0.20F;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), roarDmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, 6.0D, 20, 0.12D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.DRAGON_BREATH, 12.0D, 36, 0.18D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLAME, 18.0D, 50, 0.2D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.6F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 0.9F);
                }
            }
            case "isekai_mana_burst" -> {
                double radius = 14.0D + Math.min(6.0D, Math.max(0, intel - 10) * 0.08D);
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float intScale = 1.0F + Math.max(0, intel - 10) * 0.035F;
                boolean manaSurge = data.energy() >= data.maxEnergy() * 0.70F;
                float surgeBonus = manaSurge ? 1.20F : 1.0F;
                float burstDmg = (60.0F + (float) intel * 0.50F + (float) will * 0.35F) * intScale * surgeBonus * chargeMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), burstDmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 2.4D, 0.9D, dz / dist * 2.4D);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                if (player.level() instanceof ServerLevel sl) {
                    double circleRadius = Math.min(8.0D, 4.5D + Math.max(0, intel - 10) * 0.05D);
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANTED_HIT, ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.FLASH, circleRadius);
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FIREWORK, 7.5D, 4, 22);
                    if (isOvercharged) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLASH, 12.0D, 32, 0.2D);
                    }
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 4, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.4F, 1.2F);
                }
                String surgeTag = manaSurge ? " §e[SOBRECARGA ARCANICA +20%]" : "";
                String chargeTag = isOvercharged ? " §6§l[LIBERACIÓN MÁXIMA]" : "";
                player.displayClientMessage(Component.literal("§b§l✦ §d§l¡ESTALLIDO DE MANÁ ISEKAI! §f(Impacto: §b" + String.format(java.util.Locale.US, "%.0f", burstDmg) + "§f)" + surgeTag + chargeTag), true);
            }

            // --- HABILIDADES SHONEN LEGENDARIAS (MULTIVERSO ANIME 3.0) ---
            case "shonen_infinite_void" -> {
                double radius = isOvercharged ? 32.0D : 24.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                float voidDmg = (70.0F + (float) intel * 0.45F) * chargeMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), voidDmg);
                    // Sobrecarga de información: Congelado total y anulación de ataque
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 255));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 255));
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 180, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                data.restoreEnergy(isOvercharged ? 75 : 50);
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, radius, 8, 48);
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, ParticleTypes.SCULK_SOUL, ParticleTypes.END_ROD, radius * 0.75D);
                    if (isOvercharged) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLASH, radius * 0.9D, 40, 0.3D);
                    }
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 5, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.8F, 0.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.4F);
                }
                String chargeTag = isOvercharged ? " §6§l✦ ¡DOMINIO ABSOLUTO (+50%)! ✦" : "";
                player.displayClientMessage(Component.literal("§5§l[§d§lEXPANSIÓN DE DOMINIO§5§l] §f§l¡VACÍO INFINITO!" + chargeTag), true);
            }
            case "shonen_getsuga_tensho" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle().normalize();
                net.minecraft.world.phys.Vec3 start = player.getEyePosition();
                float slashDmg = 75.0F + (float) str * 0.55F + (float) intel * 0.35F;
                double maxRange = 26.0D;
                if (player.level() instanceof ServerLevel sl) {
                    net.minecraft.world.phys.Vec3 up = new net.minecraft.world.phys.Vec3(0, 1, 0);
                    net.minecraft.world.phys.Vec3 right = look.cross(up).normalize();
                    for (double d = 1.0D; d <= maxRange; d += 0.8D) {
                        net.minecraft.world.phys.Vec3 center = start.add(look.scale(d));
                        // Arco creciente en media luna
                        for (double arc = -2.5D; arc <= 2.5D; arc += 0.4D) {
                            net.minecraft.world.phys.Vec3 arcPos = center.add(right.scale(arc)).add(0, (1.0D - (arc * arc) / 6.25D) * 1.5D, 0);
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, arcPos.x, arcPos.y, arcPos.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, arcPos.x, arcPos.y, arcPos.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, arcPos.x, arcPos.y, arcPos.z, 1, 0.02D, 0.02D, 0.02D, 0.05D);
                        }
                    }
                    var hitEntities = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(maxRange),
                            e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                    for (var e : hitEntities) {
                        net.minecraft.world.phys.Vec3 toEnt = e.position().subtract(start);
                        double proj = toEnt.dot(look);
                        if (proj > 0 && proj <= maxRange) {
                            net.minecraft.world.phys.Vec3 closest = start.add(look.scale(proj));
                            if (e.position().distanceTo(closest) <= 3.2D) {
                                e.hurt(player.damageSources().playerAttack(player), slashDmg);
                                e.setDeltaMovement(look.x * 2.2D, 0.8D, look.z * 2.2D);
                                e.setRemainingFireTicks(120);
                            }
                        }
                    }
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.5F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.1F);
                }
                player.displayClientMessage(Component.literal("§4§l✦ §0§l¡GETSUGA TENSHO! §4§l✦"), true);
            }
            case "shonen_conquerors_haki" -> {
                double radius = 20.0D;
                var aabb = player.getBoundingBox().inflate(radius);
                var targets = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                for (var e : targets) {
                    if (e.getMaxHealth() <= 50.0F) {
                        // Mobs normales caen inconscientes por la abrumadora presencia
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 255));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 255));
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 1));
                        e.setDeltaMovement(0, -0.4D, 0);
                    } else {
                        // Jefes y élites: armadura resquebrajada y aturdimiento
                        float shockDmg = 40.0F + (float) will * 0.45F;
                        e.hurt(player.damageSources().magic(), shockDmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 3));
                    }
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.SONIC_BOOM, 16.0D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SONIC_BOOM, 14.0D, 24, 0.2D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.CAMPFIRE_COSY_SMOKE, 20.0D, 36, 0.15D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4F, 0.7F);
                }
                player.displayClientMessage(Component.literal("§c§l[§0§l⚡ HAKI DEL CONQUISTADOR ⚡§c§l] §4§l¡VOLUNTAD SUPREMA!"), true);
            }
            case "shonen_zoltraak" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle().normalize();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = look.cross(up);
                right = right.lengthSqr() < 0.001D ? new Vec3(1, 0, 0) : right.normalize();
                boolean isLeft = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
                double armSide = isLeft ? -0.28D : 0.28D;
                double armDist = 1.35D;
                double armDown = 0.20D;
                net.minecraft.world.phys.Vec3 origin = player.position().add(0, player.getEyeHeight() - armDown, 0).add(look.scale(armDist)).add(right.scale(armSide));
                double maxRange = isOvercharged ? 40.0D : 32.0D;
                net.minecraft.world.phys.Vec3 end = origin.add(look.scale(maxRange));
                float beamDmg = (90.0F + (float) intel * 0.60F) * chargeMult;
                if (player.level() instanceof ServerLevel sl) {
                    // Círculo mágico vertical de alta precisión frente al brazo
                    spawnVerticalMagicCircle(sl, origin, look, ParticleTypes.END_ROD, ParticleTypes.ENCHANT, isOvercharged ? 3.6D : 2.4D);
                    draw3DParticleLine(sl, origin, end, ParticleTypes.FLASH, isOvercharged ? 28 : 18);
                    draw3DParticleLine(sl, origin, end, ParticleTypes.END_ROD, isOvercharged ? 50 : 36);
                    draw3DParticleLine(sl, origin, end, ParticleTypes.FIREWORK, isOvercharged ? 40 : 28);
                    if (isOvercharged) {
                        draw3DParticleLine(sl, origin, end, ParticleTypes.SONIC_BOOM, 14);
                    }
                    var beamTargets = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(maxRange),
                            e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                    for (var e : beamTargets) {
                        net.minecraft.world.phys.Vec3 toEnt = e.position().subtract(origin);
                        double proj = toEnt.dot(look);
                        if (proj > 0 && proj <= maxRange) {
                            net.minecraft.world.phys.Vec3 closest = origin.add(look.scale(proj));
                            if (e.position().distanceTo(closest) <= (isOvercharged ? 3.2D : 2.2D)) {
                                e.hurt(player.damageSources().magic(), beamDmg);
                                e.setDeltaMovement(look.x * (isOvercharged ? 3.2D : 2.5D), 0.6D, look.z * (isOvercharged ? 3.2D : 2.5D));
                            }
                        }
                    }
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.6F, 0.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.8F);
                    if (isOvercharged) {
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.5F);
                    }
                }
                String chargeTag = isOvercharged ? " §6§l[SOBRECARGA +50%]" : "";
                player.displayClientMessage(Component.literal("§f§l✦ §b§lZOLTRAAK §f§l(MAGIA DESTRUCTORA DE DEMONIOS) ✦" + chargeTag), true);
            }
            case "shonen_rasenshuriken" -> {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle().normalize();
                double throwDist = 16.0D;
                net.minecraft.world.phys.Vec3 impactPos = player.getEyePosition().add(look.scale(throwDist));
                float windDmg = 65.0F + (float) agi * 0.35F + (float) intel * 0.30F;
                if (player.level() instanceof ServerLevel sl) {
                    // Estela del shuriken giratorio en vuelo
                    for (double s = 1.0D; s <= throwDist; s += 1.0D) {
                        net.minecraft.world.phys.Vec3 pos = player.getEyePosition().add(look.scale(s));
                        sl.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 5, 0.3D, 0.3D, 0.3D, 0.05D);
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 2, 0.2D, 0.2D, 0.2D, 0.02D);
                    }
                    // Explosión micro-tormenta celular en el destino
                    spawnExpandingDome(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.CLOUD, 8.0D, 6, 32);
                    spawnShockwaveRing(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.SWEEP_ATTACK, 8.0D, 28, 0.25D);
                    spawnShockwaveRing(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.EXPLOSION, 6.0D, 20, 0.2D);
                    var vortexTargets = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                            new net.minecraft.world.phys.AABB(impactPos.x - 8.0D, impactPos.y - 8.0D, impactPos.z - 8.0D,
                                    impactPos.x + 8.0D, impactPos.y + 8.0D, impactPos.z + 8.0D),
                            e -> e != player && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));
                    for (var e : vortexTargets) {
                        e.hurt(player.damageSources().playerAttack(player), windDmg);
                        // Succión al vórtice y elevación de viento cortante
                        double dx = impactPos.x - e.getX();
                        double dz = impactPos.z - e.getZ();
                        e.setDeltaMovement(dx * 0.3D, 1.1D, dz * 0.3D);
                        e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    }
                    sl.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.6F, 1.4F);
                    sl.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.5F, 1.8F);
                    sl.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.9F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §f§l¡FUTON: RASENSHURIKEN! §b§l✦"), true);
            }

            // --- HABILIDADES ACTIVAS DE ÁRBOL DE CLASE ---
            // 1. Monarca de las Sombras
            case "shadow_exchange" -> {
                ServerLevel sl = (ServerLevel) player.level();
                String masterTag = "shadow_master_" + player.getUUID();
                var shadows = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(64.0D),
                        m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));
                if (!shadows.isEmpty()) {
                    var target = shadows.get(0);
                    double px = player.getX(), py = player.getY(), pz = player.getZ();
                    player.teleportTo(target.getX(), target.getY(), target.getZ());
                    target.teleportTo(px, py, pz);
                    spawnShockwaveRing(sl, px, py + 0.1D, pz, ParticleTypes.SOUL_FIRE_FLAME, 2.5D, 24, 0.08D);
                    spawnShockwaveRing(sl, target.getX(), target.getY() + 0.1D, target.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.5D, 24, 0.08D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
                } else {
                    var look = player.getLookAngle();
                    double tx = player.getX() + look.x * 14.0D;
                    double ty = player.getY() + look.y * 14.0D;
                    double tz = player.getZ() + look.z * 14.0D;
                    player.teleportTo(tx, ty, tz);
                    spawnShockwaveRing(sl, tx, ty + 0.1D, tz, ParticleTypes.SOUL_FIRE_FLAME, 2.5D, 24, 0.08D);
                    sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 0.7F);
                }
            }
            case "monarchs_domain" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var aabb = player.getBoundingBox().inflate(20.0D);
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier") && !e.getType().getCategory().isFriendly());
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0));
                    e.hurt(player.damageSources().magic(), 25.0F + intel * 0.3F);
                }
                String mTag = "shadow_master_" + player.getUUID();
                var shadows = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, aabb,
                        m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(mTag));
                for (var s : shadows) {
                    s.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                    s.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                    s.heal(20.0F);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SCULK_SOUL, 6.0D, 32, 0.08D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 12.0D, 48, 0.12D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SQUID_INK, 18.0D, 64, 0.15D);
                spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.5D, 5.0D, 6, 12);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.7F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            case "shadow_colossus_infusion" -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL, 2.5D, 4.0D, 5, 14);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 4.0D, 28, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.3F, 1.1F);
                }
            }
            case "stealth_stalker" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, true));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SQUID_INK, 2.5D, 24, 0.08D);
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.03D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.2F, 1.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1.2F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.8F);
                }
            }
            case "bloodlust_gaze" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, e.getX(), e.getEyeY(), e.getZ(), 8, 0.3D, 0.2D, 0.3D, 0.02D);
                }
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.ANGRY_VILLAGER, 4.0D, 24, 0.08D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.3F, 1.3F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.4F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.8F);
            }

            // 2. Santo de la Espada
            case "plum_blossom_rain" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                var center = player.position().add(look.scale(3.0D));
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, new AABB(center.x - 4, center.y - 2, center.z - 4, center.x + 4, center.y + 3, center.z + 4),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (40.0F + str * 0.5F + agi * 0.3F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.setDeltaMovement(e.getDeltaMovement().add(look.x * 0.8D, 0.4D, look.z * 0.8D));
                }
                spawnVortexPillar(sl, center.x, center.y, center.z, ParticleTypes.CHERRY_LEAVES, 3.5D, 4.0D, 5, 18);
                spawnBurstCross(sl, center.x, center.y, center.z, ParticleTypes.SWEEP_ATTACK, 4.0D, 14);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.3F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.8F);
            }
            case "twenty_four_sword_forms" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(7.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (30.0F + str * 0.4F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 4.5D, 28, 0.12D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.CRIT, 5.0D, 32, 0.18D);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 1.5D, 0.8D, 1.5D, 0.15D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.4F, 1.2F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.4F);
            }
            case "plum_blossom_domain" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 360, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 360, 1));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.CHERRY_LEAVES, 4.0D, 32, 0.05D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.CHERRY_LEAVES, 8.0D, 48, 0.08D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CHERRY_LEAVES, 4.5D, 5.0D, 6, 16);
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 2.0D, 1.0D, 2.0D, 0.1D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.6F);
                }
            }
            case "daoist_clarity" -> {
                player.removeAllEffects();
                player.heal(player.getMaxHealth() * 0.35F);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.HAPPY_VILLAGER, 3.0D, 3, 16);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.END_ROD, 3.5D, 24, 0.05D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.6F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.7F);
                }
            }

            // 3. Lector Omnisciente
            case "readers_viewpoint" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(32.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.END_ROD, 3.5D);
                sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.2D, player.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.4F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.3F, 1.8F);
            }
            case "constellation_favor" -> {
                int coinGift = 25 + player.getRandom().nextInt(25);
                data.addCoins(coinGift);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 2));
                AwakeningSystemMod.notify(player, "awakening_system.notification.star_stream_sponsor.title", "awakening_system.notification.star_stream_sponsor.desc", 0xFFFFD700, coinGift);
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 2.5D, 4.0D, 4, 14);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.END_ROD, 3.5D, 24, 0.08D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.2F);
                }
            }
            case "fourth_wall_shatter" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (45.0F + intel * 0.4F + will * 0.3F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    double dx = e.getX() - player.getX();
                    double dz = e.getZ() - player.getZ();
                    double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    e.setDeltaMovement(dx / dist * 1.8D, 0.6D, dz / dist * 1.8D);
                }
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.END_ROD, 5.0D, 36, 0.1D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.ENCHANT, 10.0D, 48, 0.15D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 4, 0.5D, 0.5D, 0.5D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.6F, 0.7F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.2F, 1.1F);
            }
            case "star_stream_climax" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(18.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier") && !e.getType().getCategory().isFriendly());
                float dmg = (65.0F + intel * 0.6F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    e.setRemainingFireTicks(100);
                    sl.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 1.0D, e.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1.0D, e.getZ(), 25, 0.4D, 1.0D, 0.4D, 0.1D);
                }
                spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 5.0D, 12.0D, 8, 20);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 14.0D, 50, 0.12D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0D, player.getZ(), 3, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.3F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.4F);
            }

            // 4. Berserker
            case "aura_smash" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (45.0F + str * 0.6F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.setDeltaMovement(0, 0.9D, 0);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
                spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 4.0D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.EXPLOSION, 7.0D, 20, 0.15D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.5F, 0.7F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.1F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.3F, 0.7F);
            }
            case "cross_impact" -> {
                var look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.6D, 0.4D, look.z * 1.6D);
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (50.0F + str * 0.7F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                spawnBurstCross(sl, player.getX() + look.x * 2.0D, player.getY(), player.getZ() + look.z * 2.0D, ParticleTypes.SWEEP_ATTACK, 3.5D, 16);
                sl.sendParticles(ParticleTypes.CRIT, player.getX() + look.x * 1.5D, player.getY() + 1.0D, player.getZ() + look.z * 1.5D, 25, 0.5D, 0.5D, 0.5D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.4F, 1.3F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.3F, 1.1F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.4F);
            }
            case "valterian_gigantomachia" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2));
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 3.5D, 6.0D, 6, 16);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.LAVA, 6.0D, 32, 0.1D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.EXPLOSION, 9.0D, 24, 0.15D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5D, player.getZ(), 2, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5F, 0.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.9F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.7F);
                }
            }
            case "iron_muscle_hardening" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 260, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 260, 2));
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.5D, 3, 16);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.3F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2F, 1.3F);
                }
            }

            // 5. Cultivador Demoníaco
            case "heavenly_demon_steps" -> {
                var look = player.getLookAngle();
                double tx = player.getX() + look.x * 12.0D;
                double ty = player.getY() + look.y * 12.0D;
                double tz = player.getZ() + look.z * 12.0D;
                ServerLevel sl = (ServerLevel) player.level();
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.5D, 24, 0.08D);
                player.teleportTo(tx, ty, tz);
                spawnShockwaveRing(sl, tx, ty + 0.1D, tz, ParticleTypes.FLAME, 3.0D, 28, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 1.2F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.1F, 0.9F);
            }
            case "demonic_blood_slash" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                var aabb = player.getBoundingBox().inflate(8.0D);
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (50.0F + str * 0.4F + (float) data.dantianRealm() * 5.0F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                }
                spawnBurstCross(sl, player.getX() + look.x * 2.5D, player.getY(), player.getZ() + look.z * 2.5D, ParticleTypes.CRIMSON_SPORE, 4.0D, 18);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 2.0D, player.getY() + 1.0D, player.getZ() + look.z * 2.0D, 10, 0.5D, 0.3D, 0.5D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 1.1F, 1.2F);
            }
            case "sky_demon_nine_reversals" -> {
                data.addCultivationQi(150);
                player.heal(player.getMaxHealth() * 0.3F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), 40.0F * treeSkillMult);
                    e.setRemainingFireTicks(100);
                }
                spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 4.0D, 7.0D, 7, 18);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.CRIMSON_SPORE, 8.0D, 40, 0.12D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 0.9F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 1.2F);
            }
            case "nano_overdrive_suit" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 500, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 2.5D, 4, 18);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.1F, 2.0F);
                }
            }

            // 6. Asesino Fantasma
            case "shadow_step_blink" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                if (!enemies.isEmpty()) {
                    var target = enemies.get(0);
                    var dir = target.getLookAngle().scale(-1.8D);
                    player.teleportTo(target.getX() + dir.x, target.getY(), target.getZ() + dir.z);
                    player.setYRot(target.getYRot());
                } else {
                    var look = player.getLookAngle();
                    player.teleportTo(player.getX() + look.x * 10.0D, player.getY() + look.y * 10.0D, player.getZ() + look.z * 10.0D);
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.PORTAL, 2.5D, 24, 0.08D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.2F, 1.8F);
            }
            case "vital_strike" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (45.0F + agi * 0.7F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                }
                spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.8D, 14);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.5F, 1.4F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.2F, 1.5F);
            }
            case "infinite_mirror_blades" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (55.0F + agi * 0.5F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 4.0D, 24, 0.15D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 6.0D, 32, 0.2D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 1.5F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.4F, 1.8F);
            }
            case "smoke_bomb_escape" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                }
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2));
                var look = player.getLookAngle();
                player.setDeltaMovement(-look.x * 1.5D, 0.5D, -look.z * 1.5D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.CAMPFIRE_COSY_SMOKE, 5.0D, 36, 0.08D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.4F, 1.1F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.2F, 1.3F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1.3F, 0.9F);
            }

            // 7. Mago Supremo
            case "elemental_spear" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                double range = (isOvercharged ? 36.0D : Math.min(26.0D, 14.0D + Math.max(0, intel - 10) * 0.12D));
                var center = player.position().add(look.scale(range * 0.4D));
                var aabb = new AABB(center.x - 4, center.y - 3, center.z - 4, center.x + 4, center.y + 3, center.z + 4);
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, aabb,
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float intMult = 1.0F + Math.max(0, intel - 10) * 0.035F;
                boolean manaSurge = data.energy() >= data.maxEnergy() * 0.70F;
                float surgeBonus = manaSurge ? 1.20F : 1.0F;
                float dmg = (55.0F + intel * 0.70F) * treeSkillMult * intMult * surgeBonus * chargeMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    e.setRemainingFireTicks(isOvercharged ? 160 : 90);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, isOvercharged ? 3 : 2));
                }
                // Círculo mágico vertical frente al brazo al disparar la lanza elemental
                Vec3 normLook = look.normalize();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = normLook.cross(up);
                right = right.lengthSqr() < 0.001D ? new Vec3(1, 0, 0) : right.normalize();
                boolean isLeft = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
                double armSide = isLeft ? -0.28D : 0.28D;
                double armDist = 1.35D;
                double armDown = 0.20D;
                Vec3 armOrigin = player.position().add(0, player.getEyeHeight() - armDown, 0).add(normLook.scale(armDist)).add(right.scale(armSide));
                spawnVerticalMagicCircle(sl, armOrigin, normLook, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME, isOvercharged ? 2.8D : 1.8D);
                spawnHelixBeam(sl, armOrigin, normLook, range, ParticleTypes.FLAME, ParticleTypes.ELECTRIC_SPARK, isOvercharged ? 0.9D : 0.6D);
                if (isOvercharged) {
                    sl.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0D, center.z, 6, 0, 0, 0, 0);
                    sl.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.6F, 1.2F);
                } else {
                    sl.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0D, center.z, 3, 0, 0, 0, 0);
                }
                sl.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.4F, 1.4F);
                sl.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.3F, 1.2F);
                sl.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                String surgeTag = manaSurge ? " §e[SOBRECARGA +20%]" : "";
                String chargeTag = isOvercharged ? " §6§l✦ ¡LIBERACIÓN MÁXIMA (+50%)! ✦" : "";
                player.displayClientMessage(Component.literal("§b§l✦ §e§l[LANZA ELEMENTAL] §f¡Impacto Mágico! §b(" + String.format(java.util.Locale.US, "%.0f", dmg) + " dmg)" + surgeTag + chargeTag), true);
            }
            case "gravity_crush" -> {
                ServerLevel sl = (ServerLevel) player.level();
                double crushRadius = isOvercharged ? 24.0D : Math.min(20.0D, 12.0D + Math.max(0, intel - 10) * 0.08D);
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(crushRadius),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float intMult = 1.0F + Math.max(0, intel - 10) * 0.035F;
                boolean manaSurge = data.energy() >= data.maxEnergy() * 0.70F;
                float surgeBonus = manaSurge ? 1.20F : 1.0F;
                float dmg = (50.0F + intel * 0.60F) * treeSkillMult * intMult * surgeBonus * chargeMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    e.setDeltaMovement(0, isOvercharged ? -3.0D : -2.2D, 0);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, isOvercharged ? 5 : 4));
                }
                // Gran círculo mágico rúnico de gravedad
                spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, ParticleTypes.SCULK_SOUL, ParticleTypes.SONIC_BOOM, isOvercharged ? 8.5D : 6.0D);
                spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, isOvercharged ? 9.0D : 6.0D, 4, 24);
                spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, 5.0D, 6.0D, 6, 20);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.SONIC_BOOM, 8.0D, 16, 0.1D);
                if (isOvercharged) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.EXPLOSION, 16.0D, 32, 0.25D);
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 0.7F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.6F);
                String surgeTag = manaSurge ? " §e[SOBRECARGA +20%]" : "";
                String chargeTag = isOvercharged ? " §6§l✦ ¡COLAPSO MÁXIMO (+50%)! ✦" : "";
                player.displayClientMessage(Component.literal("§5§l✦ §d§l[COLAPSO GRAVITACIONAL] §f¡Masa Crítica! §5(" + String.format(java.util.Locale.US, "%.0f", dmg) + " dmg)" + surgeTag + chargeTag), true);
            }
            case "supernova_incineration" -> {
                ServerLevel sl = (ServerLevel) player.level();
                double blastRadius = isOvercharged ? 26.0D : Math.min(22.0D, 15.0D + Math.max(0, intel - 10) * 0.08D);
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(blastRadius),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float intMult = 1.0F + Math.max(0, intel - 10) * 0.040F;
                boolean manaSurge = data.energy() >= data.maxEnergy() * 0.70F;
                float surgeBonus = manaSurge ? 1.25F : 1.0F;
                float dmg = (90.0F + intel * 1.0F) * treeSkillMult * intMult * surgeBonus * chargeMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().magic(), dmg);
                    e.setRemainingFireTicks(isOvercharged ? 260 : 180);
                }
                // Círculo solar vertical frente al brazo invocador y vórtice ígneo
                Vec3 look = player.getLookAngle().normalize();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = look.cross(up);
                right = right.lengthSqr() < 0.001D ? new Vec3(1, 0, 0) : right.normalize();
                boolean isLeft = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
                double armSide = isLeft ? -0.28D : 0.28D;
                double armDist = 1.35D;
                double armDown = 0.20D;
                Vec3 armOrigin = player.position().add(0, player.getEyeHeight() - armDown, 0).add(look.scale(armDist)).add(right.scale(armSide));
                spawnVerticalMagicCircle(sl, armOrigin, look, ParticleTypes.LAVA, ParticleTypes.FLAME, isOvercharged ? 4.5D : 3.2D);
                spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.LAVA, ParticleTypes.FLAME, ParticleTypes.FLASH, isOvercharged ? 11.0D : 8.5D);
                spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 4.0D, 7.5D, 6, 20);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLAME, 6.0D, 36, 0.12D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.LAVA, 12.0D, 48, 0.16D);
                spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, isOvercharged ? 24.0D : 18.0D, 36, 0.22D);
                if (isOvercharged) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLASH, 28.0D, 48, 0.3D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0D, player.getZ(), 8, 0, 0, 0, 0);
                } else {
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5D, player.getZ(), 5, 0, 0, 0, 0);
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                String surgeTag = manaSurge ? " §e[SOBRECARGA SOLAR x1.25]" : "";
                String chargeTag = isOvercharged ? " §6§l✦ ¡SUPERNOVA MÁXIMA (+50%)! ✦" : "";
                player.displayClientMessage(Component.literal("§6§l✦ §4§l[SUPERNOVA] §f¡INCINERACIÓN TOTAL! §6(" + String.format(java.util.Locale.US, "%.0f", dmg) + " dmg)" + surgeTag + chargeTag), true);
            }
            case "arcane_overload" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 360, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                int energyRestore = (int) Math.min(data.maxEnergy(), data.maxEnergy() * 0.50F + intel * 2.5F);
                data.restoreEnergy(energyRestore);
                data.restoreMana(data.maxMana() / 2);
                if (player.level() instanceof ServerLevel sl) {
                    // Círculos mágicos gemelos: en el suelo y vertical
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.GLOW, ParticleTypes.END_ROD, 4.2D);
                    spawnVerticalMagicCircle(sl, player.position().add(0, 1.2D, 0), new Vec3(0, 1, 0), ParticleTypes.PORTAL, ParticleTypes.ENCHANT, 2.5D);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3D, player.getZ(), ParticleTypes.END_ROD, 4.5D, 28, 0.08D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 1.7F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.3F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.6F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §9§l[SOBRECARGA ARCANA] §f¡Maná sobrecargado (+ " + energyRestore + " EP)! §a+Poder Mágico Supremo"), true);
            }
            case "time_dilation" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                for (var e : enemies) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 3));
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 3));
                // Círculo mágico de cronomancia rúnica
                spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.FLASH, 7.5D);
                spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 8.0D, 4, 24);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.3F, 1.5F);
                player.displayClientMessage(Component.literal("§3§l✦ §b§l[DILATACIÓN TEMPORAL] §f¡El flujo del tiempo se congela para tus enemigos!"), true);
            }

            // 8. Soberano Dragón
            case "dragon_breath" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                var center = player.position().add(look.scale(5.0D));
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, new AABB(center.x - 5, center.y - 3, center.z - 5, center.x + 5, center.y + 3, center.z + 5),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (50.0F + str * 0.4F + intel * 0.4F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().dragonBreath(), dmg);
                    e.setRemainingFireTicks(120);
                }
                spawnHelixBeam(sl, player.getEyePosition(), look, 10.0D, ParticleTypes.DRAGON_BREATH, ParticleTypes.FLAME, 0.7D);
                sl.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.5D, center.z, 20, 1.0D, 0.5D, 1.0D, 0.1D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 1.0F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.9F);
            }
            case "meteor_fall" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                var impact = player.position().add(look.scale(10.0D));
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, new AABB(impact.x - 6, impact.y - 3, impact.z - 6, impact.x + 6, impact.y + 4, impact.z + 6),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (70.0F + str * 0.5F + intel * 0.5F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().explosion(null), dmg);
                    e.setRemainingFireTicks(140);
                    e.setDeltaMovement(0, 0.8D, 0);
                }
                spawnImpactCrater(sl, impact.x, impact.y, impact.z, 5.0D);
                spawnShockwaveRing(sl, impact.x, impact.y + 0.2D, impact.z, ParticleTypes.LAVA, 6.0D, 36, 0.12D);
                spawnShockwaveRing(sl, impact.x, impact.y + 0.2D, impact.z, ParticleTypes.FLAME, 10.0D, 48, 0.18D);
                sl.sendParticles(ParticleTypes.FLASH, impact.x, impact.y + 1.0D, impact.z, 3, 0, 0, 0, 0);
                sl.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.6F);
                sl.playSound(null, impact.x, impact.y, impact.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.4F, 1.1F);
                sl.playSound(null, impact.x, impact.y, impact.z, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
            }
            case "breath_of_annihilation" -> {
                ServerLevel sl = (ServerLevel) player.level();
                var look = player.getLookAngle();
                for (int d = 2; d <= 20; d += 2) {
                    var pos = player.position().add(look.scale(d));
                    sl.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
                }
                var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                float dmg = (90.0F + str * 0.6F + intel * 0.6F) * treeSkillMult;
                for (var e : enemies) {
                    e.hurt(player.damageSources().dragonBreath(), dmg);
                    e.setDeltaMovement(look.x * 2.0D, 0.8D, look.z * 2.0D);
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.9F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.7F);
            }
            case "dragon_wings_flight" -> {
                player.setDeltaMovement(0, 1.5D, 0);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 3));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.DRAGON_BREATH, 4.0D, 32, 0.12D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DRAGON_BREATH, 2.5D, 3.5D, 4, 14);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.6F, 1.0F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.4F, 1.2F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.3F, 1.1F);
                }
            }
            case "dragon_form_avatar" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4));
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                            e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                    for (var e : enemies) {
                        e.hurt(player.damageSources().dragonBreath(), 40.0F);
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / dist * 1.6D, 0.5D, dz / dist * 1.6D);
                    }
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DRAGON_BREATH, 5.0D, 7.0D, 6, 20);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, 8.0D, 28, 0.18D);
                    sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0D, player.getZ(), 3, 0, 0, 0, 0);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 1.4F, 1.1F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 1.1F);
                }
            }

            // =========================================================================
            // 9. SANADOR CELESTIAL (The S-Classes That I Raised)
            // =========================================================================
            case "sh_caregiver_blessing" -> {
                float healAmt = 12.0F + (float) vit * 0.15F + (float) intel * 0.10F;
                player.heal(healAmt * treeSkillMult);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 360, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, false, true, true));
                var allies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && (e instanceof net.minecraft.world.entity.TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || e instanceof ServerPlayer || e.getTags().contains("shadow_soldier")));
                for (var ally : allies) {
                    ally.heal(healAmt * treeSkillMult);
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 360, 1));
                    ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.HEART, 4.5D);
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 2.5D, 4.0D, 4, 14);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§a§l✦ §2§l[BENDICIÓN DEL CUIDADOR] §f¡Sanación milagrosa aplicada a ti y a tus aliados!"), true);
            }
            case "sh_holy_touch" -> {
                player.removeEffect(MobEffects.WITHER);
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.WEAKNESS);
                player.heal((16.0F + (float) intel * 0.15F) * treeSkillMult);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 0, false, true, true));
                var allies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(10.0D),
                        e -> e != player && (e instanceof net.minecraft.world.entity.TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || e instanceof ServerPlayer || e.getTags().contains("shadow_soldier")));
                for (var ally : allies) {
                    ally.removeEffect(MobEffects.WITHER);
                    ally.removeEffect(MobEffects.POISON);
                    ally.heal(12.0F * treeSkillMult);
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.HAPPY_VILLAGER, 3.5D, 20, 0.06D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.3F, 1.6F);
                }
                player.displayClientMessage(Component.literal("§a§l✦ §f¡Tacto Sagrado! Purificación completa y vida restaurada."), true);
            }
            case "sh_divine_grace" -> {
                int dur = (int) (300 * treeSkillMult);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, dur, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 1));
                data.restoreMana(data.maxMana() / 3);
                var allies = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && (e instanceof net.minecraft.world.entity.TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || e instanceof ServerPlayer || e.getTags().contains("shadow_soldier")));
                for (var ally : allies) {
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 2));
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.GLOW, 6.0D, 4, 18);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                }
                player.displayClientMessage(Component.literal("§e§l✦ §6§l[GRACIA DIVINA] §f¡Aura celestial de regeneración suprema activa!"), true);
            }
            case "sh_sacred_sanctuary" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2));
                if (player.level() instanceof ServerLevel sl) {
                    var undeads = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D),
                            e -> e != player && e.isAlive() && e.isInvertedHealAndHarm());
                    for (var u : undeads) {
                        u.hurt(player.damageSources().magic(), 35.0F * treeSkillMult);
                        u.setRemainingFireTicks(120);
                        double dx = u.getX() - player.getX();
                        double dz = u.getZ() - player.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        u.setDeltaMovement(dx / dist * 1.5D, 0.4D, dz / dist * 1.5D);
                    }
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.0D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.4F);
                }
                player.displayClientMessage(Component.literal("§a§l✦ §fSantuario Sagrado desplegado. No-muertos repelidos."), true);
            }
            case "sh_cleansing_radiance" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                            e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                    for (var e : enemies) {
                        float dmg = (e.isInvertedHealAndHarm() ? 80.0F : 45.0F) * treeSkillMult;
                        e.hurt(player.damageSources().magic(), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.FLASH, 10.0D, 36, 0.2D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§e§l✦ §f¡Resplandor Purificador! Estallido de luz sagrada."), true);
            }
            case "sh_aegis_immortal" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 3));
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5D, player.getZ(), ParticleTypes.END_ROD, 3.0D, 24, 0.05D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.5F, 1.0F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §f¡Égida Celestial! Invulnerabilidad absoluta por 6 segundos."), true);
            }
            case "sh_tamer_bond" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
                var pets = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(20.0D),
                        e -> e != player && (e instanceof net.minecraft.world.entity.TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || e.getTags().contains("shadow_soldier")));
                for (var pet : pets) {
                    pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                    pet.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 2));
                    pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.2F, 1.1F);
                }
                player.displayClientMessage(Component.literal("§6§l✦ §fVínculo de Bestias activado para todas tus mascotas."), true);
            }
            case "sh_awakening_inspire" -> {
                int dur = 500;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 1));
                var allies = player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(16.0D), p -> p != player);
                for (var a : allies) {
                    a.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur, 1));
                    a.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, 1));
                }
                if (player.level() instanceof ServerLevel sl) {
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 3.0D, 5.0D, 5, 16);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§a§l✦ §2§l[INSPIRACIÓN DEL MENTOR] §f¡+25% Potencia de combate a ti y tu equipo!"), true);
            }

            // =========================================================================
            // 10. EMPERADOR DEL RELÁMPAGO (Lightning Degree)
            // =========================================================================
            case "le_electrification" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().lightningBolt(), (30.0F + (float) agi * 0.3F) * treeSkillMult);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 6.0D, 36, 0.18D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §3§l[ELECTRIFICACIÓN] §f¡Cuerpo imbuido en plasma celestial!"), true);
            }
            case "le_thunder_spear" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var look = player.getLookAngle();
                    for (int d = 1; d <= 22; d += 2) {
                        var p = player.getEyePosition().add(look.scale(d));
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 5, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                            e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));
                    for (var e : enemies) {
                        e.hurt(player.damageSources().lightningBolt(), (60.0F + (float) agi * 0.5F) * treeSkillMult);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                    }
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.6F, 1.1F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §f¡Lanza del Trueno disparada con éxito!"), true);
            }
            case "le_plasma_burst" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(9.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().lightningBolt(), (55.0F + (float) str * 0.4F + (float) agi * 0.4F) * treeSkillMult);
                        e.setDeltaMovement(0, 1.0D, 0);
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 8.0D, 40, 0.22D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§3§l✦ §b§l[ESTALLIDO DE PLASMA] §f¡Descarga circular omnidireccional!"), true);
            }
            case "le_wrath_of_thunder_god" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().lightningBolt(), (85.0F + (float) agi * 0.6F) * treeSkillMult);
                        sl.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 1.0D, e.getZ(), 2, 0, 0, 0, 0);
                    }
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 5.0D, 8.0D, 6, 24);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.8F, 0.9F);
                }
                player.displayClientMessage(Component.literal("§e§l✦ §6§l[IRA DEL DIOS DEL TRUENO] §f¡Tormenta eléctrica celestial desatada!"), true);
            }
            case "le_flash_step" -> {
                var look = player.getLookAngle();
                double tx = player.getX() + look.x * 12.0D;
                double ty = player.getY() + look.y * 5.0D;
                double tz = player.getZ() + look.z * 12.0D;
                var safe = TeleportHelper.findSafeTeleportPos(player.level(), player, tx, ty, tz, player.position());
                player.teleportTo(safe.x, safe.y, safe.z);
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, safe.x, safe.y + 0.1D, safe.z, ParticleTypes.ELECTRIC_SPARK, 3.0D, 20, 0.1D);
                    sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.4F, 1.3F);
                }
                player.displayClientMessage(Component.literal("§b§l✦ §fPaso Relampagueante ejecutado."), true);
            }
            case "le_overdrive_speed" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                player.displayClientMessage(Component.literal("§e§l✦ §f¡Sobrecarga de Voltaje activa!"), true);
            }
            case "le_lightning_armor" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));
                player.displayClientMessage(Component.literal("§3§l✦ §fArmadura Electrificada activa."), true);
            }
            case "le_vajra_indestructible" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2));
                if (player.level() instanceof ServerLevel sl) {
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.0D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§6§l✦ §f¡Cuerpo Vajra Dorado activado!"), true);
            }

            // =========================================================================
            // 11. EMPERADOR DE SANGRE (Doom Breaker)
            // =========================================================================
            case "be_bloodthirst" -> {
                float dmg = 35.0F * treeSkillMult;
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().magic(), dmg);
                    }
                    player.heal(10.0F * treeSkillMult);
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.DAMAGE_INDICATOR, 5.0D, 24, 0.1D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 1.4F);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                player.displayClientMessage(Component.literal("§c§l✦ §4§l[SED DE SANGRE] §f¡Vida drenada a tus enemigos!"), true);
            }
            case "be_blood_lance" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var look = player.getLookAngle();
                    for (int d = 1; d <= 20; d += 2) {
                        var p = player.getEyePosition().add(look.scale(d));
                        sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 4, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), (65.0F + (float) str * 0.5F) * treeSkillMult);
                    }
                    player.heal(12.0F * treeSkillMult);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.4F, 0.8F);
                }
                player.displayClientMessage(Component.literal("§c§l✦ §f¡Lanza de Sangre perforante disparada!"), true);
            }
            case "be_crimson_eruption" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().magic(), (70.0F + (float) str * 0.5F) * treeSkillMult);
                        e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 8.0D, 32, 0.15D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 1.2F);
                }
                player.displayClientMessage(Component.literal("§4§l✦ §c§l[ERUPCIÓN CARMESÍ] §f¡Púas de sangre empalaron a los enemigos!"), true);
            }
            case "be_blood_apocalypse" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().magic(), (95.0F + (float) str * 0.6F) * treeSkillMult);
                    }
                    player.heal(player.getMaxHealth());
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                    spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DAMAGE_INDICATOR, 6.0D, 8.0D, 6, 30);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.8F);
                }
                player.displayClientMessage(Component.literal("§4§l✦ §c§l[APOCALIPSIS DE SANGRE] §f¡Salud restaurada al máximo!"), true);
            }
            case "be_crimson_rush" -> {
                var look = player.getLookAngle();
                player.setDeltaMovement(look.x * 2.0D, 0.3D, look.z * 2.0D);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
                player.displayClientMessage(Component.literal("§c§l✦ §fArremetida Sanguinaria ejecutada."), true);
            }
            case "be_berserk_blood" -> {
                player.hurt(player.damageSources().magic(), 2.0F);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 320, 3));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 2));
                player.displayClientMessage(Component.literal("§4§l✦ §f¡Sangre Hirviente desatada!"), true);
            }
            case "be_blood_barrier" -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4));
                player.displayClientMessage(Component.literal("§c§l✦ §fBarrera de Sangre Cristalina activa."), true);
            }
            case "be_dragon_blood_avatar" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                player.displayClientMessage(Component.literal("§4§l✦ §c§l[AVATAR DEL DRAGÓN CARMESÍ] §fForma dracónica activa."), true);
            }

            // =========================================================================
            // 12. MONARCA DE LAS BESTIAS (Solo Leveling / White Tiger)
            // =========================================================================
            case "bm_seismic_roar" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), (40.0F + (float) str * 0.4F) * treeSkillMult);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / dist * 1.8D, 0.4D, dz / dist * 1.8D);
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.SONIC_BOOM, 6.0D, 24, 0.15D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.9F);
                }
                player.displayClientMessage(Component.literal("§6§l✦ §f¡Rugido Sísmico de la Bestia liberado!"), true);
            }
            case "bm_predator_leap" -> {
                var look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.8D, 0.8D, look.z * 1.8D);
                if (player.level() instanceof ServerLevel sl) {
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1D, player.getZ(), ParticleTypes.CRIT, 4.0D, 18, 0.08D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.4F);
                }
                player.displayClientMessage(Component.literal("§6§l✦ §fSalto Depredador ejecutado."), true);
            }
            case "bm_feral_rend" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), (70.0F + (float) str * 0.6F) * treeSkillMult);
                        e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                    }
                    spawnBurstCross(sl, player.getX(), player.getY() + 1.0D, player.getZ(), ParticleTypes.SWEEP_ATTACK, 3.0D, 16);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5F, 0.8F);
                }
                player.displayClientMessage(Component.literal("§c§l✦ §f¡Desgarro Feroz cuádruple aplicado!"), true);
            }
            case "bm_white_tiger_transformation" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
                if (player.level() instanceof ServerLevel sl) {
                    spawnGeometricMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, ParticleTypes.ANGRY_VILLAGER, ParticleTypes.FLASH, 6.0D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.6F, 1.1F);
                }
                player.displayClientMessage(Component.literal("§f§l✦ §6§l[TIGRE BLANCO] §f¡Transformación bestial activa!"), true);
            }
            case "bm_scent_tracking" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var mobs = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(40.0D), e -> e != player && e.isAlive());
                    for (var m : mobs) {
                        m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                player.displayClientMessage(Component.literal("§6§l✦ §fRastreo de Presas activo. Enemigos revelados."), true);
            }
            case "bm_pack_frenzy" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                player.displayClientMessage(Component.literal("§6§l✦ §fFuria de la Manada activa."), true);
            }
            case "bm_thick_fur" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 280, 2));
                player.displayClientMessage(Component.literal("§7§l✦ §fPelaje de Hierro endurecido."), true);
            }
            case "bm_titan_roar" -> {
                if (player.level() instanceof ServerLevel sl) {
                    var enemies = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D), e -> e != player && e.isAlive());
                    for (var e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), (75.0F + (float) str * 0.6F) * treeSkillMult);
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / dist * 2.2D, 0.8D, dz / dist * 2.2D);
                    }
                    spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2D, player.getZ(), ParticleTypes.EXPLOSION, 10.0D, 36, 0.25D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.7F);
                }
                player.displayClientMessage(Component.literal("§c§l✦ §6§l[RUGIDO DEL TITÁN] §f¡Onda expansiva monstruosa!"), true);
            }

            // Aliases de árbol
            case "cb_valterian_instinct" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 1));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
                player.displayClientMessage(Component.literal("§c§l✦ §fInstinto Valteriano desatado."), true);
            }
            case "cb_pain_suppression", "sa_pain_suppression", "ga_pain_suppression" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                player.displayClientMessage(Component.literal("§7§l✦ §fSupresión de dolor activa."), true);
            }
            case "cb_bloodthirst", "dm_bloodthirst" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                player.heal(8.0F);
                player.displayClientMessage(Component.literal("§c§l✦ §fSed de sangre saciada."), true);
            }
            case "dc_nano_blade_overdrive" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                player.displayClientMessage(Component.literal("§5§l✦ §fSobrecarga de nanocuchilla activa."), true);
            }
            case "va_stealth_stalker" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                player.displayClientMessage(Component.literal("§8§l✦ §fAcecho sigiloso iniciado."), true);
            }
            case "va_phantom_ambush" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 2));
                player.displayClientMessage(Component.literal("§b§l✦ §fEmboscada fantasmal lista."), true);
            }
            case "va_death_mark" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
                player.displayClientMessage(Component.literal("§4§l✦ §fMarca de muerte fijada."), true);
            }
            case "dm_dragon_dive" -> {
                player.setDeltaMovement(0, -2.0D, 0);
                if (player.level() instanceof ServerLevel sl) {
                    spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 4.0D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.8F);
                }
                player.displayClientMessage(Component.literal("§6§l✦ §fCaída dracónica de impacto."), true);
            }
            default -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true, true));
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.35D, 0.45D, 0.35D, 0.15D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.85F, 1.25F);
        }
        checkAchievements(player, data);
        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_activated.title",
                "awakening_system.notification.skill_activated.message", 0xFF50FA7B, SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).nameKey() : skillId);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void summonShadowSoldier(ServerPlayer master, net.minecraft.world.entity.LivingEntity defeated) {
        if (!(master.level() instanceof ServerLevel sl)) return;

        net.minecraft.world.entity.Entity created = defeated.getType().create(sl);
        net.minecraft.world.entity.Mob shadowMob;
        if (created instanceof net.minecraft.world.entity.Mob mob) {
            shadowMob = mob;
        } else {
            shadowMob = new net.minecraft.world.entity.animal.Wolf(net.minecraft.world.entity.EntityType.WOLF, sl);
        }

        shadowMob.setPos(defeated.getX(), defeated.getY(), defeated.getZ());
        shadowMob.addTag("shadow_soldier");
        shadowMob.addTag("shadow_master_" + master.getUUID());

        String mobName = defeated.getCustomName() != null ? defeated.getCustomName().getString() : defeated.getType().getDescription().getString();
        shadowMob.setCustomName(Component.literal("§8§l[SOMBRA] §9" + mobName));
        shadowMob.setCustomNameVisible(true);

        if (defeated instanceof net.minecraft.world.entity.Mob defeatedMob) {
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                ItemStack item = defeatedMob.getItemBySlot(slot);
                if (!item.isEmpty()) {
                    shadowMob.setItemSlot(slot, item.copy());
                }
            }
        }

        setupShadowMob(sl, master, shadowMob);
        AwakeningSystemMod.notify(master, "awakening_system.notification.shadow_extracted.title",
                "awakening_system.notification.shadow_extracted.message", 0xFF9966FF, mobName);
    }

    public static void applyShadowRankStats(net.minecraft.world.entity.Mob shadowMob, ShadowRank rank) {
        if (shadowMob == null || rank == null) return;
        var maxHpAttr = shadowMob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHpAttr != null) {
            double baseHp = shadowMob.getPersistentData().contains("shadow_original_max_hp")
                    ? shadowMob.getPersistentData().getDouble("shadow_original_max_hp")
                    : maxHpAttr.getBaseValue();
            shadowMob.getPersistentData().putDouble("shadow_original_max_hp", baseHp);
            maxHpAttr.setBaseValue(baseHp * rank.getHealthMultiplier());
            if (shadowMob.getHealth() > 0) {
                shadowMob.setHealth(shadowMob.getMaxHealth());
            }
        }
        var dmgAttr = shadowMob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null && rank.getBonusAttackDamage() > 0) {
            double baseDmg = shadowMob.getPersistentData().contains("shadow_original_dmg")
                    ? shadowMob.getPersistentData().getDouble("shadow_original_dmg")
                    : dmgAttr.getBaseValue();
            shadowMob.getPersistentData().putDouble("shadow_original_dmg", baseDmg);
            dmgAttr.setBaseValue(baseDmg + rank.getBonusAttackDamage());
        }
        var followAttr = shadowMob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        if (followAttr != null) {
            followAttr.setBaseValue(48.0D + rank.getLevel() * 16.0D);
        }
        shadowMob.removeAllEffects();
        if (rank.getStrengthAmp() >= 0) {
            shadowMob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 72000, rank.getStrengthAmp(), false, false, false));
        }
        if (rank.getResistanceAmp() >= 0) {
            shadowMob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 72000, rank.getResistanceAmp(), false, false, false));
        }
        if (rank.getSpeedAmp() >= 0) {
            shadowMob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 72000, rank.getSpeedAmp(), false, false, false));
        }
        shadowMob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 72000, 0, false, false, false));
        shadowMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));

        if (rank.getLevel() >= 3) {
            shadowMob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 72000, rank.getLevel() >= 4 ? 1 : 0, false, false, false));
        }
        if (rank.getLevel() >= 4) {
            shadowMob.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 72000, rank.getLevel() >= 5 ? 3 : 1, false, false, false));
        }
    }

    public static void promoteOrTrackShadowKill(net.minecraft.world.entity.Mob shadowSoldier) {
        if (shadowSoldier == null || !(shadowSoldier.level() instanceof ServerLevel sl)) return;
        int curRankLevel = shadowSoldier.getPersistentData().getInt("shadow_rank");
        ShadowRank currentRank = ShadowRank.byLevel(curRankLevel);
        if (currentRank == ShadowRank.GRAND_MARSHAL) return;

        int kills = shadowSoldier.getPersistentData().getInt("shadow_kills") + 1;
        shadowSoldier.getPersistentData().putInt("shadow_kills", kills);

        int required = currentRank.getKillsToPromote();
        if (required > 0 && kills >= required) {
            ShadowRank nextRank = ShadowRank.byLevel(curRankLevel + 1);
            shadowSoldier.getPersistentData().putInt("shadow_rank", nextRank.getLevel());
            shadowSoldier.getPersistentData().putInt("shadow_kills", 0);

            String baseName = shadowSoldier.getPersistentData().getString("shadow_base_name");
            if (baseName == null || baseName.isEmpty()) {
                baseName = shadowSoldier.getType().getDescription().getString();
            }
            shadowSoldier.setCustomName(Component.literal(nextRank.getPrefix() + " §9" + baseName));
            applyShadowRankStats(shadowSoldier, nextRank);

            double ox = shadowSoldier.getX();
            double oy = shadowSoldier.getY();
            double oz = shadowSoldier.getZ();
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, ox, oy + 1.0D, oz, 70, 0.6D, 0.8D, 0.6D, 0.2D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, oy + 0.8D, oz, 80, 0.8D, 0.8D, 0.8D, 0.1D);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, ox, oy + 1.0D, oz, 40, 0.5D, 0.6D, 0.5D, 0.08D);
            sl.sendParticles(ParticleTypes.SONIC_BOOM, ox, oy + 1.0D, oz, 1, 0, 0, 0, 0);
            sl.playSound(null, ox, oy, oz, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.3F, 0.9F);
            sl.playSound(null, ox, oy, oz, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.85F, 1.4F);

            String masterUUIDStr = null;
            for (String tag : shadowSoldier.getTags()) {
                if (tag.startsWith("shadow_master_")) {
                    masterUUIDStr = tag.substring("shadow_master_".length());
                    break;
                }
            }
            if (masterUUIDStr != null && sl.getServer() != null) {
                try {
                    UUID mUUID = UUID.fromString(masterUUIDStr);
                    ServerPlayer master = sl.getServer().getPlayerList().getPlayer(mUUID);
                    if (master != null) {
                        AwakeningSystemMod.notify(master, "awakening_system.notification.shadow_promoted.title",
                                "awakening_system.notification.shadow_promoted.desc", nextRank.getColor(),
                                baseName, nextRank.getPrefix());
                        master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(5, 35, 15));
                        master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                                Component.literal("§9§l« ASCENSO EN LA LEGIÓN »")
                        ));
                        master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                                Component.literal("§f" + baseName + " §7ha ascendido a " + nextRank.getPrefix())
                        ));
                        master.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.1F);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public static void summonShadowFromSoul(ServerPlayer master, com.soybe.awakeningsystem.entity.ShadowSoulEntity soul) {
        if (!(master.level() instanceof ServerLevel sl)) return;
        if (soul.getMobEntityTypeId().toLowerCase().contains("celestial_dragon")) {
            master.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
            return;
        }

        net.minecraft.world.entity.EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(
                net.minecraft.resources.ResourceLocation.tryParse(soul.getMobEntityTypeId())
        );
        if (type == null) type = net.minecraft.world.entity.EntityType.ZOMBIE;

        net.minecraft.world.entity.Entity created = type.create(sl);
        net.minecraft.world.entity.Mob shadowMob;
        if (created instanceof net.minecraft.world.entity.Mob mob) {
            shadowMob = mob;
        } else {
            shadowMob = new net.minecraft.world.entity.animal.Wolf(net.minecraft.world.entity.EntityType.WOLF, sl);
        }

        shadowMob.setPos(soul.getX(), soul.getY(), soul.getZ());
        shadowMob.addTag("shadow_soldier");
        shadowMob.addTag("shadow_master_" + master.getUUID());

        String mobName = soul.getMobName();
        if (mobName == null || mobName.isEmpty()) {
            mobName = type.getDescription().getString();
        }
        int rankLvl = soul.getShadowRank();
        shadowMob.getPersistentData().putInt("shadow_rank", rankLvl);
        shadowMob.getPersistentData().putInt("shadow_kills", 0);
        shadowMob.getPersistentData().putString("shadow_base_name", mobName);

        ShadowRank rank = ShadowRank.byLevel(rankLvl);
        shadowMob.setCustomName(Component.literal(rank.getPrefix() + " §9" + mobName));
        shadowMob.setCustomNameVisible(true);

        for (var entry : soul.getSavedEquipment().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                shadowMob.setItemSlot(entry.getKey(), entry.getValue().copy());
            }
        }

        setupShadowMob(sl, master, shadowMob);
    }

    public static void summonShadowFromTag(ServerPlayer master, CompoundTag tag, double x, double y, double z) {
        if (!(master.level() instanceof ServerLevel sl)) return;

        String typeStr = tag.getString("MobEntityTypeId");
        if (typeStr.toLowerCase().contains("celestial_dragon")) {
            master.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
            return;
        }
        net.minecraft.world.entity.EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(
                net.minecraft.resources.ResourceLocation.tryParse(typeStr)
        );
        if (type == null) type = net.minecraft.world.entity.EntityType.ZOMBIE;

        net.minecraft.world.entity.Entity created = type.create(sl);
        net.minecraft.world.entity.Mob shadowMob;
        if (created instanceof net.minecraft.world.entity.Mob mob) {
            shadowMob = mob;
        } else {
            shadowMob = new net.minecraft.world.entity.animal.Wolf(net.minecraft.world.entity.EntityType.WOLF, sl);
        }

        shadowMob.setPos(x, y, z);
        shadowMob.addTag("shadow_soldier");
        shadowMob.addTag("shadow_master_" + master.getUUID());

        int rankLvl = tag.contains("ShadowRank") ? tag.getInt("ShadowRank") : 0;
        int kills = tag.contains("ShadowKills") ? tag.getInt("ShadowKills") : 0;
        String baseName = tag.contains("ShadowBaseName") ? tag.getString("ShadowBaseName") : (tag.contains("MobName") ? tag.getString("MobName") : type.getDescription().getString());
        baseName = baseName.replaceAll("§[0-9a-fk-or]", "").replaceAll("\\[.*?\\]", "").trim();
        if (baseName.isEmpty()) baseName = type.getDescription().getString();

        shadowMob.getPersistentData().putInt("shadow_rank", rankLvl);
        shadowMob.getPersistentData().putInt("shadow_kills", kills);
        shadowMob.getPersistentData().putString("shadow_base_name", baseName);

        ShadowRank rank = ShadowRank.byLevel(rankLvl);
        shadowMob.setCustomName(Component.literal(rank.getPrefix() + " §9" + baseName));
        shadowMob.setCustomNameVisible(true);

        if (tag.contains("Health")) {
            shadowMob.setHealth(Math.min(shadowMob.getMaxHealth(), tag.getFloat("Health")));
        }

        if (tag.contains("Equipment")) {
            CompoundTag eqTag = tag.getCompound("Equipment");
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (eqTag.contains(slot.getName())) {
                    ItemStack.parse(master.registryAccess(), eqTag.getCompound(slot.getName()))
                            .ifPresent(stack -> shadowMob.setItemSlot(slot, stack));
                }
            }
        }

        setupShadowMob(sl, master, shadowMob);
    }

    private static void setupShadowMob(ServerLevel sl, ServerPlayer master, net.minecraft.world.entity.Mob shadowMob) {
        int rankLvl = shadowMob.getPersistentData().getInt("shadow_rank");
        ShadowRank rank = ShadowRank.byLevel(rankLvl);
        applyShadowRankStats(shadowMob, rank);

        try {
            shadowMob.targetSelector.removeAllGoals(g -> true);
            shadowMob.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                    shadowMob, net.minecraft.world.entity.Mob.class, 10, true, false,
                    target -> target != null && target.isAlive()
                            && !target.getTags().contains("shadow_soldier")
                            && !target.getUUID().equals(master.getUUID())
                            && (target instanceof net.minecraft.world.entity.monster.Enemy || !target.getType().getCategory().isFriendly())
            ));
        } catch (Exception ignored) {}

        // Emergencia solemne y pausada (animación de alzamiento saliendo desde el subsuelo):
        // Se ancla al suelo con velocidad cero, debilidad y lentitud 255 mientras emerge gradualmente durante 3.5 segundos
        shadowMob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 255, false, false, false));
        shadowMob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 255, false, false, false));
        shadowMob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 4, false, false, false));
        shadowMob.setDeltaMovement(0, 0, 0);
        shadowMob.setOnGround(true);

        // Registro en el Equipo del Scoreboard con color AQUA (Cian/Azul eléctrico brillante) para el Glow distintivo
        try {
            net.minecraft.world.scores.Scoreboard sb = sl.getScoreboard();
            net.minecraft.world.scores.PlayerTeam team = sb.getPlayerTeam("shadows");
            if (team == null) {
                team = sb.addPlayerTeam("shadows");
                team.setColor(ChatFormatting.AQUA);
                team.setDisplayName(Component.literal("Sombras"));
            }
            String member = shadowMob.getScoreboardName();
            if (!team.getPlayers().contains(member)) {
                sb.addPlayerToTeam(member, team);
            }
            String uuidStr = shadowMob.getStringUUID();
            if (!team.getPlayers().contains(uuidStr)) {
                sb.addPlayerToTeam(uuidStr, team);
            }
        } catch (Exception ignored) {}

        sl.addFreshEntity(shadowMob);

        // Sincronización de la cinemática de emergencia hacia el cliente
        ShadowEmergencePayload emergencePayload = new ShadowEmergencePayload(
                shadowMob.getId(), shadowMob.getX(), shadowMob.getY(), shadowMob.getZ(), 70, shadowMob.getBbHeight()
        );
        if (master != null) {
            PacketDistributor.sendToPlayer(master, emergencePayload);
        }
        try {
            PacketDistributor.sendToPlayersTrackingEntity(shadowMob, emergencePayload);
        } catch (Exception ignored) {}

        double ox = shadowMob.getX();
        double oy = shadowMob.getY();
        double oz = shadowMob.getZ();

        // Charco de sombras líquido en el suelo (anillos concéntricos oscuros y azul eléctrico)
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 0.75D;
            int points = ring * 12;
            for (int i = 0; i < points; i++) {
                double angle = (2.0 * Math.PI * i) / points;
                double px = ox + radius * Math.cos(angle);
                double pz = oz + radius * Math.sin(angle);
                sl.sendParticles(ParticleTypes.SQUID_INK, px, oy + 0.05D, pz, 1, 0, 0.005D, 0, 0.005D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, oy + 0.05D, pz, 1, 0, 0.01D, 0, 0.005D);
            }
        }

        // Explosión pausada de sombras y almas ascendiendo con lentitud
        sl.sendParticles(ParticleTypes.SQUID_INK, ox, oy + 0.5D, oz, 45, 0.4D, 0.5D, 0.4D, 0.02D);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, oy + 0.5D, oz, 40, 0.35D, 0.5D, 0.35D, 0.02D);
        sl.sendParticles(ParticleTypes.SCULK_SOUL, ox, oy + 0.4D, oz, 25, 0.3D, 0.4D, 0.3D, 0.01D);
        sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 0.4D, oz, 30, 0.3D, 0.4D, 0.3D, 0.05D);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, ox, oy + 0.2D, oz, 20, 0.3D, 0.3D, 0.3D, 0.01D);

        // Paisaje sonoro cinematográfico (graves profundos)
        sl.playSound(null, ox, oy, oz, ModSounds.SHADOW_EXTRACT.value(), SoundSource.PLAYERS, 1.4F, 1.0F);

        // Pulso visual cinematográfico en el Monarca de las Sombras
        if (master != null) {
            master.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0, false, false, false));
            master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(5, 30, 15));
            master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.shadow.arise_title")
            ));
            String rawName = shadowMob.getPersistentData().getString("shadow_base_name");
            if (rawName == null || rawName.isEmpty()) {
                rawName = shadowMob.getType().getDescription().getString();
            }
            master.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.shadow.arise_subtitle", rank.getPrefix(), rawName)
            ));
        }
    }

    public static int storePlayerShadows(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel sl)) return 0;
        SystemData data = data(player);
        String masterTag = "shadow_master_" + player.getUUID();
        var soldiers = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(96.0D),
                m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));

        if (soldiers.isEmpty()) return 0;
        int count = 0;
        for (var soldier : soldiers) {
            String typeKey = net.minecraft.world.entity.EntityType.getKey(soldier.getType()).toString();
            if (typeKey.toLowerCase(java.util.Locale.ROOT).contains("celestial_dragon")) {
                soldier.discard();
                continue;
            }

            CompoundTag tag = new CompoundTag();
            tag.putString("MobEntityTypeId", typeKey);
            String sName = soldier.getCustomName() != null ? soldier.getCustomName().getString() : soldier.getType().getDescription().getString();
            tag.putString("MobName", sName);
            tag.putFloat("Health", soldier.getHealth());
            tag.putFloat("MaxHealth", soldier.getMaxHealth());
            tag.putInt("ShadowRank", soldier.getPersistentData().getInt("shadow_rank"));
            tag.putInt("ShadowKills", soldier.getPersistentData().getInt("shadow_kills"));
            String bName = soldier.getPersistentData().getString("shadow_base_name");
            if (bName == null || bName.isEmpty()) bName = soldier.getType().getDescription().getString();
            tag.putString("ShadowBaseName", bName);

            CompoundTag eqTag = new CompoundTag();
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                ItemStack it = soldier.getItemBySlot(slot);
                if (!it.isEmpty()) {
                    eqTag.put(slot.getName(), it.saveOptional(soldier.registryAccess()));
                }
            }
            tag.put("Equipment", eqTag);

            data.storeShadow(tag);

            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 25, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 30, 0.3D, 0.5D, 0.3D, 0.02D);
            soldier.discard();
            count++;
        }
        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_stored.title",
                "awakening_system.notification.shadow_stored.desc", 0xFF9966FF,
                String.valueOf(count), String.valueOf(data.storedShadowCount()));
        AwakeningSystemMod.sync(player);
        return count;
    }

    public static int deployPlayerShadows(ServerPlayer player, int maxCount) {
        if (!(player.level() instanceof ServerLevel sl)) return 0;
        SystemData data = data(player);
        if (data.storedShadowCount() <= 0) return 0;

        int toDeploy = Math.min(maxCount, data.storedShadowCount());
        for (int i = 0; i < toDeploy; i++) {
            CompoundTag shadowTag = data.popStoredShadow();
            if (shadowTag != null) {
                double angle = Math.toRadians(player.getYRot() + 180.0F + ((i % 3) - 1) * 35.0F);
                double sx = player.getX() + Math.sin(angle) * 2.0D;
                double sz = player.getZ() - Math.cos(angle) * 2.0D;
                summonShadowFromTag(player, shadowTag, sx, player.getY(), sz);
            }
        }
        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_deployed.title",
                "awakening_system.notification.shadow_deployed.desc", 0xFF9966FF,
                String.valueOf(toDeploy));
        AwakeningSystemMod.sync(player);
        return toDeploy;
    }

    private static void tickShadowSoldiers(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == null || !player.isAlive() || player.hasDisconnected()) continue;
            if (!(player.level() instanceof ServerLevel sl)) continue;

            try {
                teleportDistantShadowSoldiers(player);

                SystemData data = data(player);
                String masterTag = "shadow_master_" + player.getUUID();

                var soldiers = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(96.0D),
                        m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag));

                boolean synced = false;
                boolean lowMana = false;
                for (var soldier : soldiers) {
                    if (!soldier.hasEffect(MobEffects.GLOWING)) {
                        soldier.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));
                    }
                    if (soldier.isOnFire() && !soldier.isInLava()) {
                        soldier.clearFire();
                    }

                    // Escalado de aura azul según el rango jerárquico de la sombra
                    int sRank = soldier.getPersistentData().getInt("shadow_rank");
                    int flameCount = Math.max(1, sRank * 2);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.3D, soldier.getZ(), flameCount, 0.25D, 0.3D, 0.25D, 0.01D);
                    if (sRank >= 2) {
                        sl.sendParticles(ParticleTypes.SCULK_SOUL, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), Math.min(3, sRank - 1), 0.2D, 0.3D, 0.2D, 0.01D);
                    }
                    if (sRank >= 4) {
                        sl.sendParticles(ParticleTypes.PORTAL, soldier.getX(), soldier.getY() + 0.5D, soldier.getZ(), 4, 0.3D, 0.4D, 0.3D, 0.02D);
                    }

                    if (soldier.getHealth() < soldier.getMaxHealth()) {
                        if (data.energy() >= 1) {
                            data.spendEnergy(1);
                            soldier.heal(2.5F);
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.6D, soldier.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.02D);
                            synced = true;
                        } else {
                            lowMana = true;
                        }
                    }
                }

                if (lowMana && player.tickCount % 200 == 0) {
                    AwakeningSystemMod.notify(player, "awakening_system.notification.shadow_mana_low.title",
                            "awakening_system.notification.shadow_mana_low.desc", 0xFFFF7666);
                }

                if (synced) {
                    AwakeningSystemMod.sync(player);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void triggerDungeonGate(ServerPlayer player, String rank) {
        DungeonManager.startDungeon(player, rank);
    }

    public static boolean prestigePlayer(ServerPlayer player) {
        SystemData data = data(player);
        if (!data.prestige()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.prestige_failed.title",
                    "awakening_system.notification.prestige_failed.message", 0xFFFF7666);
            return false;
        }
        SystemAttributeEffects.apply(player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.1D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
        AwakeningSystemMod.notify(player, "awakening_system.notification.prestige.title",
                "awakening_system.notification.prestige.message", 0xFFFFB86C, data.prestigeLevel());
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void checkAchievements(ServerPlayer player, SystemData data) {
        for (AchievementDefinitions.Definition achievement : AchievementDefinitions.definitions()) {
            if (!data.hasAchievement(achievement.id())
                    && AchievementDefinitions.statValue(data, achievement.statRequired()) >= achievement.target()) {
                if (data.unlockAchievement(achievement.id())) {
                    data.addExperience(achievement.expReward());
                    data.addCoins(achievement.coinReward());
                    AwakeningSystemMod.notify(player, "awakening_system.notification.achievement.title",
                            "awakening_system.notification.achievement.message", 0xFFFFD866, achievement.nameKey());
                }
            }
        }
    }

    private static void completeAwakening(ServerPlayer player, SystemData data) {
        if (data.completeAwakening()) {
            data.addCoins(100);
            awardExperience(player, data, 250);
            AwakeningSystemMod.notify(player, "awakening_system.notification.awakening_complete.title",
                    "awakening_system.notification.awakening_complete.message", 0xFFFFD866);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            AwakeningSystemMod.sync(player);
        }
    }

    private static void completeClassMission(ServerPlayer player, SystemData data) {
        if (data.completeClassMission()) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.class_complete.title",
                    "awakening_system.notification.class_complete.message", 0xFFFFD866);
        }
    }
    private static boolean updateDailyProgress(ServerPlayer player, SystemData data, long day, int type, int amount) {
        if (!SystemConfig.DAILY_MISSIONS_ENABLED.get()) return false;
        return data.recordDailyProgress(day, type, amount);
    }

    private static int learnAndAnnounce(ServerPlayer player, SystemData data, String affinity, int amount, String skillId) {
        boolean before = data.skills().containsKey(skillId);
        int gained = data.learn(affinity, amount);
        if (!before && data.skills().containsKey(skillId)) announceSkill(player, skillId);
        return gained;
    }

    private static boolean gainSkillProgress(ServerPlayer player, SystemData data, String skillId) {
        if (!data.gainSkillProgress(skillId, 1)) return false;
        announceSkillLevel(player, skillId, data.skills().get(skillId));
        return true;
    }

    private static boolean isOre(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES);
    }

    public static void awardExperience(ServerPlayer player, SystemData data, int amount) {
        double expMultiplier = SystemConfig.EXP_MULTIPLIER.get();
        if (SystemConfig.EVENT_NIGHT_BONUS.get() && player.level().isNight()) expMultiplier += 0.25D;
        if (data.prestigeLevel() > 0) {
            expMultiplier += data.prestigeLevel() * 0.10D; // +10% EXP permanente por nivel de prestigio
        }
        int finalExp = Math.max(1, (int) Math.round(amount * expMultiplier));
        if (data.addExperience(finalExp)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.level_up.title",
                    "awakening_system.notification.level_up.message", 0xFFFFD866, data.level());
            triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.LEVEL_UP);
            ConstellationAscension.checkLevelMilestone(player, data.level());
            if (data.level() >= 30 && !data.hasHunterClass() && !player.getPersistentData().getBoolean("notified_hunter_class")) {
                player.getPersistentData().putBoolean("notified_hunter_class", true);
                AwakeningSystemMod.notify(player, "awakening_system.notification.job_available.title",
                        "awakening_system.notification.job_available.desc", 0xFFFFD700);
            }
            if (data.level() >= 30 && (data.level() - 30) % 5 == 0) {
                data.addSkillPoints(1);
                AwakeningSystemMod.notify(player, "awakening_system.notification.sp_gained.title",
                        "awakening_system.notification.sp_gained.desc", 0xFFFFD700);
            }
        }
        int rankMilestone;
        while ((rankMilestone = data.claimRankReward()) > 0) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.rank_reward.title",
                    "awakening_system.notification.rank_reward.message", 0xFFFFD866,
                    SystemData.rankTranslationKey(rankMilestone));
        }
    }

    private static void announceSkillLevel(ServerPlayer player, String skillId, int level) {
        SkillDefinitions.Definition skill = SkillDefinitions.get(skillId);
        if (skill != null) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_level.title",
                    "awakening_system.notification.skill_level.message", 0xFFFFD866, skill.nameKey());
        }
    }

    private static void announceSkill(ServerPlayer player, String skillId) {
        SkillDefinitions.Definition skill = SkillDefinitions.get(skillId);
        if (skill != null) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill.title",
                    "awakening_system.notification.skill.message", 0xFFE4A7FF, skill.nameKey());
        }
    }

    public static void announceScenarioComplete(ServerPlayer player, SystemData data) {
        AwakeningSystemMod.notify(player, "awakening_system.notification.scenario_complete.title",
                "awakening_system.notification.scenario_complete.message", 0xFF70E8FF,
                "awakening_system.scenario." + data.activeScenario() + ".name");
        player.playNotifySound(ModSounds.SYSTEM_QUEST_COMPLETE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static boolean claimScenario(ServerPlayer player) {
        return claimScenarioReward(player);
    }

    public static boolean claimScenarioReward(ServerPlayer player) {
        SystemData data = data(player);
        if (!data.isScenarioComplete()) return false;
        int coins = switch (data.activeScenario()) {
            case "depth_expedition" -> 75;
            case "abyssal_conquest" -> 150;
            case "survival_trial" -> 100;
            default -> 50;
        };
        int exp = 200;
        data.addCoins(coins);
        awardExperience(player, data, exp);
        data.advanceToNextScenario();
        AwakeningSystemMod.notify(player, "awakening_system.notification.scenario_claimed.title",
                "awakening_system.notification.scenario_claimed.message", 0xFF50FA7B, String.valueOf(coins));
        player.playNotifySound(ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean selectHunterClass(ServerPlayer player, String hunterClass) {
        SystemData data = data(player);
        if (data.level() < 30) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.job_low_level.title", "awakening_system.notification.job_low_level.desc", 0xFFFF5555);
            return false;
        }
        if (data.hasHunterClass()) {
            AwakeningSystemMod.notify(player, "awakening_system.class.already_chosen_title",
                    "awakening_system.class.already_chosen_locked", 0xFFFF5555);
            return false;
        }
        data.setHunterClass(hunterClass);

        String signatureSkill = switch (hunterClass) {
            case "shadow_monarch" -> "shadow_monarch";
            case "sword_saint" -> "asrahan_mana_blade";
            case "star_apostle" -> "fourth_wall";
            case "colossal_berserker" -> "cb_valterian_instinct";
            case "demonic_cultivator" -> "black_fire_wave";
            case "void_assassin" -> "nano_blade_overdrive";
            case "grand_archmage" -> "isekai_mana_burst";
            case "dragon_monarch" -> "isekai_dragon_roar";
            case "beast_monarch" -> "bm_seismic_roar";
            case "blood_emperor" -> "be_bloodthirst";
            case "lightning_emperor" -> "le_electrification";
            case "spiritual_healer" -> "sh_caregiver_blessing";
            default -> null;
        };

        if (signatureSkill != null) {
            data.skills().put(signatureSkill, 1);
            switch (hunterClass) {
                case "beast_monarch" -> data.skills().put("seismic_roar", 1);
                case "blood_emperor" -> data.skills().put("bloodthirst", 1);
                case "lightning_emperor" -> data.skills().put("electrification", 1);
                case "spiritual_healer" -> data.skills().put("caregiver_blessing", 1);
                case "colossal_berserker" -> data.skills().put("dragon_dive", 1);
            }
            data.setEquippedActiveSkill(0, signatureSkill);
        }

        String className = switch (hunterClass) {
            case "shadow_monarch" -> "§9Monarca de las Sombras";
            case "sword_saint" -> "§aSanto de la Espada";
            case "star_apostle" -> "§eApóstol del Star Stream";
            case "colossal_berserker" -> "§cBerserker Colosal";
            case "demonic_cultivator" -> "§5Demonio Celestial";
            case "void_assassin" -> "§bAsesino del Vacío";
            case "grand_archmage" -> "§dGran Archimago";
            case "dragon_monarch" -> "§6Soberano Dragón";
            case "beast_monarch" -> "§6Monarca de las Bestias";
            case "blood_emperor" -> "§cEmperador de Sangre";
            case "lightning_emperor" -> "§bEmperador del Relámpago";
            case "spiritual_healer" -> "§aSanador Celestial";
            default -> hunterClass;
        };
        AwakeningSystemMod.notify(player, "awakening_system.notification.job_awakened.title",
                "awakening_system.notification.job_awakened.desc", 0xFFFFD700, className);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SYSTEM_AWAKENING.value(), SoundSource.PLAYERS, 1.4F, 1.0F);

        int initialSp = 1 + (data.level() - 30) / 5;
        if (data.skillPoints() + data.spentSkillPoints() < initialSp) {
            data.addSkillPoints(initialSp - (data.skillPoints() + data.spentSkillPoints()));
        }

        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void handleSelectVision(ServerPlayer player, String vision) {
        SystemData data = data(player);
        String v = (vision == null || vision.isBlank()) ? "NONE" : vision.trim().toUpperCase(java.util.Locale.ROOT);
        data.setActiveVision(v);
        AwakeningSystemMod.sync(player);

        if ("NONE".equalsIgnoreCase(v)) {
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
            player.displayClientMessage(Component.literal("§7[Visión] Has retirado tu resonancia elemental."), true);
        } else {
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.2F);
            String elemName = switch (v) {
                case "PYRO" -> "§c§lPyro (Fuego)";
                case "HYDRO" -> "§9§lHydro (Agua)";
                case "CRYO" -> "§b§lCryo (Hielo)";
                case "ANEMO" -> "§a§lAnemo (Viento)";
                case "ELECTRO" -> "§d§lElectro (Rayo)";
                case "GEO" -> "§6§lGeo (Roca)";
                case "DENDRO" -> "§2§lDendro (Naturaleza)";
                default -> v;
            };
            player.displayClientMessage(Component.literal("§6✦ ¡Resonancia Elemental activada: " + elemName + "§6!"), true);
            player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.5D, 0.4D, 0.1D);
        }
    }

    public static void tickPlayerVision(ServerPlayer player, SystemData data) {
        String vision = data.activeVision();
        if (vision == null || "NONE".equalsIgnoreCase(vision)) return;

        ServerLevel level = player.serverLevel();
        int tick = player.tickCount;

        switch (vision) {
            case "PYRO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
                }
                BlockPos under = player.blockPosition().below();
                if (level.getBlockState(under).is(net.minecraft.world.level.block.Blocks.WATER)) {
                    if (tick % 4 == 0) {
                        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.2D, 0.05D, 0.2D, 0.02D);
                        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1D, player.getZ(), 1, 0.1D, 0.02D, 0.1D, 0.01D);
                    }
                }
            }
            case "HYDRO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, false, false, true));
                    if (player.isInWater()) {
                        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, false, false, true));
                    }
                }
                if (player.isOnFire()) {
                    player.clearFire();
                    level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.1D);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.8F, 1.2F);
                }
            }
            case "CRYO" -> {
                if (player.getTicksFrozen() > 0) {
                    player.setTicksFrozen(0);
                }
                if (player.onGround()) {
                    BlockPos under = player.blockPosition().below();
                    for (BlockPos p : BlockPos.betweenClosed(under.offset(-1, 0, -1), under.offset(1, 0, 1))) {
                        if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                            level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState());
                            level.scheduleTick(p, net.minecraft.world.level.block.Blocks.FROSTED_ICE, net.minecraft.util.Mth.nextInt(level.random, 60, 120));
                        }
                    }
                }
            }
            case "ANEMO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false, true));
                }
                player.fallDistance = 0.0F;
                if (!player.onGround() && player.isShiftKeyDown() && player.getDeltaMovement().y < -0.1D) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false, false));
                    if (tick % 3 == 0) {
                        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1D, player.getZ(), 1, 0.15D, 0.02D, 0.15D, 0.02D);
                    }
                }
            }
            case "ELECTRO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
                }
            }
            case "GEO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, false, false, true));
                }
            }
            case "DENDRO" -> {
                if (tick % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                }
                if (player.isShiftKeyDown() && tick % 40 == 0) {
                    BlockPos p = player.blockPosition();
                    for (BlockPos target : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                        var state = level.getBlockState(target);
                        if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bm && bm.isValidBonemealTarget(level, target, state)) {
                            bm.performBonemeal(level, level.random, target, state);
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.05D);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static boolean unlockSkillTreeNode(ServerPlayer player, String skillId) {
        SystemData data = data(player);
        if (!data.hasHunterClass()) return false;
        ClassSkillTree.SkillNode node = ClassSkillTree.getNode(data.hunterClass(), skillId);
        if (node == null) return false;

        if (!ClassSkillTree.canUnlock(data, skillId)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_req_not_met.title", "awakening_system.notification.skill_req_not_met.desc", 0xFFFF5555);
            return false;
        }

        if (node.spCost() > 0 && !data.spendSkillPoints(node.spCost())) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_sp_needed.title", "awakening_system.notification.skill_sp_needed.desc", 0xFFFF5555, node.spCost());
            return false;
        }

        data.skills().put(skillId, 1);
        String sName = node.getName().getString();

        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_learned.title",
                "awakening_system.notification.skill_learned.desc", 0xFFFFD700, sName);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.2F, 1.0F);

        // Si es activa y tiene ranura vacía, equipar automáticamente
        if (node.isActive()) {
            for (int i = 0; i < data.getUnlockedActiveSkillSlots(); i++) {
                if (data.getEquippedActiveSkill(i).isEmpty()) {
                    data.setEquippedActiveSkill(i, skillId);
                    break;
                }
            }
        }

        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean upgradeSkillTreeNode(ServerPlayer player, String skillId) {
        SystemData data = data(player);
        if (!data.hasHunterClass()) return false;
        ClassSkillTree.SkillNode node = ClassSkillTree.getNode(data.hunterClass(), skillId);
        if (node == null) return false;

        if (!ClassSkillTree.canUpgrade(data, skillId)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgrade_req.title", "awakening_system.notification.skill_upgrade_req.desc", 0xFFFF5555);
            return false;
        }

        int currentLvl = data.skills().getOrDefault(skillId, 1);
        int targetLvl = currentLvl + 1;
        int spCost = ClassSkillTree.getSpCostForRank(node, targetLvl);

        if (!data.spendSkillPoints(spCost)) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgrade_sp.title", "awakening_system.notification.skill_upgrade_sp.desc", 0xFFFF5555, spCost);
            return false;
        }

        data.skills().put(skillId, targetLvl);
        String sName = node.getName().getString();

        String roman = targetLvl == 2 ? "II" : "III (MAESTRÍA DEFINITIVA)";
        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_upgraded.title",
                "awakening_system.notification.skill_upgraded.desc", 0xFFFFD700, sName, roman);

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.3F, 1.1F);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.08D);

        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean selectConstellation(ServerPlayer player, String constellationId) {
        SystemData data = data(player);
        if (data.level() < 100) {
            AwakeningSystemMod.notify(player, "awakening_system.notification.constellation.failed_title",
                    "awakening_system.screen.sponsor.locked_desc", 0xFFFF5555);
            return false;
        }
        if (!data.setSponsorConstellation(constellationId)) return false;
        ConstellationDefinitions.Constellation constellation = ConstellationDefinitions.get(constellationId);
        if (constellation != null) {
            if (constellation.defaultDonation() > 0) {
                data.addCoins(constellation.defaultDonation());
            }
            AwakeningSystemMod.notify(player, "awakening_system.notification.constellation.title",
                    "awakening_system.notification.constellation.sponsored", 0xFFD055FF, constellation.nameKey());
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    private static final java.util.Map<java.util.UUID, Long> LAST_COMBAT_CONSTELLATION_REACTION = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> LAST_LEVEL_UP_CONSTELLATION_REACTION = new java.util.concurrent.ConcurrentHashMap<>();

    public static void triggerConstellationReaction(ServerPlayer player, ConstellationDefinitions.ReactionType type) {
        SystemData data = data(player);
        if (data == null) return;

        // Si el jugador no es una Constelación ni tiene un Patrocinador contratado, las constelaciones no reaccionan
        if (!data.hasSponsor() && !data.isConstellation()) {
            return;
        }

        if (type == ConstellationDefinitions.ReactionType.LEVEL_UP) {
            long now = System.currentTimeMillis();
            Long last = LAST_LEVEL_UP_CONSTELLATION_REACTION.get(player.getUUID());
            if (last != null && (now - last) < 20000L) {
                return; // Cooldown de 20s en subidas de nivel para evitar spam en subidas consecutivas
            }
            LAST_LEVEL_UP_CONSTELLATION_REACTION.put(player.getUUID(), now);
        }

        if (type == ConstellationDefinitions.ReactionType.LOW_HEALTH || type == ConstellationDefinitions.ReactionType.COMBO_TRANCE) {
            long now = System.currentTimeMillis();
            Long last = LAST_COMBAT_CONSTELLATION_REACTION.get(player.getUUID());
            if (last != null && (now - last) < 35000L) {
                return; // Cooldown de 35s en combate para evitar saturación de pantalla
            }
            LAST_COMBAT_CONSTELLATION_REACTION.put(player.getUUID(), now);
        }
        String sponsorId = data.sponsorConstellation();
        ConstellationDefinitions.Constellation contractedSponsor = ConstellationDefinitions.get(sponsorId);

        // Sistema de Patrocinadores estilo Omniscient Reader's Viewpoint (ORV):
        // Cualquier constelación del Transmisor Estelar puede observar y donar en cualquier momento.
        // Si el jugador eligió un patrocinador oficial, éste le otorga su Estigma y ventajas permanentes,
        // además de donar con mayor frecuencia y un +50% de bonificación de lealtad.
        // ¡Pero las demás constelaciones del universo continúan donando y reaccionando activamente!
        if (type == ConstellationDefinitions.ReactionType.BOSS_KILL) {
            // Clímax de Escenario: Donación del patrocinador oficial (si existe) Y de una constelación espectadora invitada
            if (contractedSponsor != null) {
                int officialDonation = 80 + player.getRandom().nextInt(50);
                if ("queen_of_the_darkest_night".equals(contractedSponsor.id())) {
                    officialDonation = (int)(officialDonation * 1.5);
                }
                data.addCoins(officialDonation);
                String officialMsgKey = ConstellationDefinitions.reactionKey(contractedSponsor.id(), type);
                AwakeningSystemMod.notify(player, "awakening_system.notification.official_sponsor_donation.title",
                        officialMsgKey, 0xFFFFD866, contractedSponsor.nameKey(), String.valueOf(officialDonation));
            }

            ConstellationDefinitions.Constellation guest = ConstellationDefinitions.randomOtherConstellation(contractedSponsor != null ? contractedSponsor.id() : null);
            int guestDonation = 50 + player.getRandom().nextInt(40);
            if ("queen_of_the_darkest_night".equals(sponsorId)) {
                guestDonation = (int)(guestDonation * 1.5);
            }
            data.addCoins(guestDonation);
            String guestMsgKey = ConstellationDefinitions.reactionKey(guest.id(), type);
            AwakeningSystemMod.notify(player, "awakening_system.notification.constellation_donation.title",
                    guestMsgKey, 0xFF70E8FF, guest.nameKey(), String.valueOf(guestDonation));

            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.3F);
        } else {
            // Eventos habituales (LEVEL_UP, LOW_HEALTH, DUNGEON_ENTER, COMBO_TRANCE)
            // Si tiene patrocinador oficial: 40% de probabilidad de ser el oficial (con bonus +50%), 60% cualquier otra constelación
            // Si no tiene patrocinador oficial: 100% cualquier constelación del Transmisor Estelar
            boolean isOfficial = contractedSponsor != null && player.getRandom().nextFloat() < 0.40F;
            ConstellationDefinitions.Constellation reactor = isOfficial ? contractedSponsor :
                    ConstellationDefinitions.randomOtherConstellation(contractedSponsor != null ? contractedSponsor.id() : null);

            int baseDonation = switch (type) {
                case LEVEL_UP -> 25 + player.getRandom().nextInt(25);
                case LOW_HEALTH -> 20 + player.getRandom().nextInt(20);
                case DUNGEON_ENTER -> 15 + player.getRandom().nextInt(15);
                case COMBO_TRANCE -> 20 + player.getRandom().nextInt(20);
                default -> 20;
            };

            int finalDonation = isOfficial ? (int)(baseDonation * 1.5) : baseDonation;
            if ("queen_of_the_darkest_night".equals(sponsorId)) {
                finalDonation = (int)(finalDonation * 1.5);
            }

            data.addCoins(finalDonation);
            String titleKey = isOfficial ? "awakening_system.notification.official_sponsor_donation.title" :
                    "awakening_system.notification.constellation_donation.title";
            int color = isOfficial ? 0xFFFFD866 : 0xFFFFD700;
            String msgKey = ConstellationDefinitions.reactionKey(reactor.id(), type);

            AwakeningSystemMod.notify(player, titleKey, msgKey, color, reactor.nameKey(), String.valueOf(finalDonation));
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);
        }

        AwakeningSystemMod.sync(player);
    }

    private static void handleSeriesStructureInteractions(ServerPlayer player, PlayerInteractEvent.RightClickBlock event) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = event.getPos();
        var state = level.getBlockState(pos);

        // -1. Interacciones dentro de la Pagoda de las Nueve Tribulaciones (Puerta manifiesta o puerta de retorno)
        if (PagodaTrialManager.isInTrial(player.getUUID())) {
            PagodaTrialManager.handleDoorInteraction(player, pos);
            event.setCanceled(true);
            return;
        }

        // -0.5. Puerta del Santuario de la Pagoda de las Nueve Tribulaciones en el mundo exterior
        if (state.is(Blocks.CRIMSON_DOOR)) {
            boolean isPagodaSanctumGate = (level.getBlockState(pos.below()).is(Blocks.GILDED_BLACKSTONE) || level.getBlockState(pos.below(2)).is(Blocks.GILDED_BLACKSTONE))
                    && (level.getBlockState(pos.east()).is(Blocks.CRYING_OBSIDIAN) || level.getBlockState(pos.west()).is(Blocks.CRYING_OBSIDIAN)
                    || level.getBlockState(pos.north(2)).is(Blocks.RESPAWN_ANCHOR) || level.getBlockState(pos.south(2)).is(Blocks.RESPAWN_ANCHOR));
            if (isPagodaSanctumGate) {
                PagodaTrialManager.enterTrial(player);
                event.setCanceled(true);
                return;
            }
        }

        // 0. Gran Tablón de Cacerías del Gremio (huella 3x2 o clic en pared detrás del tablón)
        BlockPos boardPos = com.soybe.awakeningsystem.block.BountyBoardBlock.findNearbyBountyBoard(level, pos, event.getFace());
        if (boardPos != null) {
            com.soybe.awakeningsystem.block.BountyBoardBlock.openBoardFor(player);
            event.setCanceled(true);
            return;
        }

        // 1. Campana Sagrada del Culto Demoníaco (Nano Machine)
        if (state.is(Blocks.BELL)) {
            AABB searchBox = new AABB(pos).inflate(16.0D);
            boolean nearYeowoon = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, searchBox,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.CHEON_YEOWOON).isEmpty();
            if (nearYeowoon) {
                level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.8F);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20, 0.4D, 0.4D, 0.4D, 0.05D);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1, false, true));
                AwakeningSystemMod.notify(player,
                        "awakening_system.event.demonic_bell_title",
                        "awakening_system.event.demonic_bell_msg",
                        0xFFFF2222);
                event.setCanceled(true);
                return;
            }
        }

        // 2. Terminal del Star Stream de Bihyung (ORV)
        if (state.is(Blocks.OCHRE_FROGLIGHT) || state.is(Blocks.BLACK_CONCRETE)) {
            AABB searchBox = new AABB(pos).inflate(16.0D);
            boolean nearDokja = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, searchBox,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.KIM_DOKJA).isEmpty();
            if (nearDokja) {
                String tag = "claimed_orv_stream_" + (level.getDayTime() / 24000L);
                if (!player.getTags().contains(tag)) {
                    player.addTag(tag);
                    var data = data(player);
                    data.addCoins(100);
                    AwakeningSystemMod.sync(player);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 25, 0.5D, 0.5D, 0.5D, 0.1D);
                    AwakeningSystemMod.notify(player,
                            "awakening_system.event.bihyung_channel_title",
                            "awakening_system.event.bihyung_channel_msg",
                            0xFFFFD700);
                } else {
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 1.5F);
                    AwakeningSystemMod.notify(player,
                            "awakening_system.event.bihyung_already_claimed_title",
                            "awakening_system.event.bihyung_already_claimed_msg",
                            0xFFAAAAAA);
                }
                event.setCanceled(true);
                return;
            }
        }

        // 3. Altar del Rey de la Muerte (Kim Gong-ja en TBATE)
        if (state.is(Blocks.RESPAWN_ANCHOR)) {
            AABB searchBox = new AABB(pos).inflate(16.0D);
            boolean nearArthur = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, searchBox,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.ARTHUR_LEYWIN).isEmpty();
            if (nearArthur) {
                level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 35, 0.6D, 0.6D, 0.6D, 0.1D);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true));
                AwakeningSystemMod.notify(player,
                        "awakening_system.event.death_king_altar_title",
                        "awakening_system.event.death_king_altar_msg",
                        0xFF55FFFF);
                event.setCanceled(true);
                return;
            }
        }

        // 4. Brasero del Corazón del Dragón (Second Life Ranker)
        if (state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.MAGMA_BLOCK)) {
            AABB searchBox = new AABB(pos).inflate(16.0D);
            boolean nearYeonwoo = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, searchBox,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.CHA_YEONWOO).isEmpty();
            if (nearYeonwoo) {
                level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 0.8F, 1.3F);
                level.sendParticles(ParticleTypes.DRAGON_BREATH, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 30, 0.5D, 0.5D, 0.5D, 0.05D);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1, false, true));
                AwakeningSystemMod.notify(player,
                        "awakening_system.event.black_dragon_heart_title",
                        "awakening_system.event.black_dragon_heart_msg",
                        0xFFFF3333);
                event.setCanceled(true);
            }
        }
    }

    private static void tickStructureAmbientEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos p = player.blockPosition();
        var state = level.getBlockState(p);
        var below = level.getBlockState(p.below());

        // A. Manantial de Éter de Dicathen (TBATE) - Jugador en agua cerca de Arthur Leywin
        if (state.is(Blocks.WATER)) {
            AABB box = new AABB(p).inflate(12.0D);
            boolean nearArthur = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, box,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.ARTHUR_LEYWIN).isEmpty();
            if (nearArthur) {
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WITHER);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, false));
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }

        // B. Círculo del Monarca de las Sombras (Solo Leveling) - Jugador sobre obsidiana llorosa / linterna cerca de Jin-Woo
        if (below.is(Blocks.CRYING_OBSIDIAN) || below.is(Blocks.SEA_LANTERN)) {
            AABB box = new AABB(p).inflate(10.0D);
            boolean nearJinwoo = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, box,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.SUNG_JINWOO).isEmpty();
            if (nearJinwoo) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, false, false, false));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1D, player.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.02D);
            }
        }

        // C. Calefacción Ondol de Lloyd (The Greatest Estate Developer) - Jugador sobre cristal naranja cerca de Lloyd
        if (below.is(Blocks.ORANGE_STAINED_GLASS) || below.is(Blocks.BRICKS)) {
            AABB box = new AABB(p).inflate(10.0D);
            boolean nearLloyd = !level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class, box,
                    e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.LLOYD_FRONTERA).isEmpty();
            if (nearLloyd) {
                player.setTicksFrozen(0);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, false, false, false));
                if (player.tickCount % 40 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.01D);
                }
            }
        }
    }

    public static void spawnDokkaebiNearPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB nearby = player.getBoundingBox().inflate(48.0D);
        if (!level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.DokkaebiEntity.class, nearby).isEmpty()) {
            return; // Ya hay un Dokkaebi cerca
        }
        var dokkaebi = new com.soybe.awakeningsystem.entity.DokkaebiEntity(com.soybe.awakeningsystem.entity.ModEntities.DOKKAEBI.get(), level);
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        double dist = 2.5D + player.getRandom().nextDouble() * 1.5D;
        double spawnX = player.getX() + Math.cos(angle) * dist;
        double spawnZ = player.getZ() + Math.sin(angle) * dist;
        double spawnY = player.getY() + 1.2D;
        dokkaebi.moveTo(spawnX, spawnY, spawnZ, player.getYRot() + 180.0F, 0.0F);
        dokkaebi.bindPlayer(player);
        level.addFreshEntity(dokkaebi);
        level.playSound(null, dokkaebi.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.3F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnX, spawnY + 0.5D, spawnZ, 35, 0.4D, 0.5D, 0.4D, 0.15D);
        AwakeningSystemMod.notify(player,
                "awakening_system.event.wild_dokkaebi_title",
                "awakening_system.event.wild_dokkaebi_msg",
                0xFFFFD700);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        // Escalar criaturas según las zonas RPG o escalar Jefes de Mundo según los jugadores
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.world.entity.Mob mob && event.getLevel() instanceof ServerLevel slSpawn) {
            if (com.soybe.awakeningsystem.world.WorldBossScalingManager.isWorldBoss(mob)) {
                com.soybe.awakeningsystem.world.WorldBossScalingManager.scaleWorldBossToNearbyPlayers(mob, slSpawn);
            } else {
                RpgLevelZoneManager.scaleMobOnSpawn(mob, slSpawn);
            }
        }

        if (event.getLevel().dimension().location().equals(IsekaiWorldManager.ISEKAI_WORLD.location())) {
            // Erradicar totalmente a los Endermans en la Dimensión Isekai
            if (event.getEntity().getType() == net.minecraft.world.entity.EntityType.ENDERMAN) {
                event.setCanceled(true);
                return;
            }

            // Sustitución de monstruos hostiles vanilla por fauna autóctona de Isekai (Goblins y Hombres Bestia)
            if (!event.loadedFromDisk() && isVanillaHostileMonster(event.getEntity())
                    && event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                var monster = (net.minecraft.world.entity.monster.Monster) event.getEntity();
                var random = monster.getRandom();
                if (random.nextFloat() < 0.65F) {
                    float roll = random.nextFloat();
                    net.minecraft.world.entity.EntityType<?> targetType;
                    if (roll < 0.35F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SCOUT.get();
                    } else if (roll < 0.65F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_WARRIOR.get();
                    } else if (roll < 0.80F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SHAMAN.get();
                    } else {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.BEASTMAN_WARRIOR.get();
                    }
                    var customMob = targetType.create(serverLevel);
                    if (customMob != null) {
                        customMob.moveTo(monster.getX(), monster.getY(), monster.getZ(), monster.getYRot(), monster.getXRot());
                        serverLevel.addFreshEntity(customMob);
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            // Integración dinámica de criaturas pasivas de Friends & Foes si está instalado
            if (!event.loadedFromDisk() && event.getEntity() instanceof net.minecraft.world.entity.animal.Animal animal) {
                var random = animal.getRandom();
                if (random.nextFloat() < 0.28F && event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    var moobloomType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "moobloom")).orElse(null);
                    var glareType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "glare")).orElse(null);
                    var crabType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "crab")).orElse(null);

                    net.minecraft.world.entity.EntityType<?> targetType = null;
                    int roll = random.nextInt(3);
                    if (roll == 0 && moobloomType != null) targetType = moobloomType;
                    else if (roll == 1 && glareType != null) targetType = glareType;
                    else if (roll == 2 && crabType != null) targetType = crabType;

                    if (targetType != null) {
                        var customMob = targetType.create(serverLevel);
                        if (customMob != null) {
                            customMob.moveTo(animal.getX(), animal.getY(), animal.getZ(), animal.getYRot(), animal.getXRot());
                            serverLevel.addFreshEntity(customMob);
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }

        // Población autóctona de Murim (Jiangshi, Cultivadores Demoníacos y Bestia Acorazada de Jade)
        // Solo convierte monstruos hostiles VANILLA, nunca criaturas del mod ni invocaciones
        if (event.getLevel().dimension().location().equals(MurimWorldManager.MURIM_WORLD.location())) {
            if (!event.loadedFromDisk() && isVanillaHostileMonster(event.getEntity())
                    && event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                var monster = (net.minecraft.world.entity.monster.Monster) event.getEntity();
                var random = monster.getRandom();
                if (random.nextFloat() < 0.70F) {
                    float roll = random.nextFloat();
                    net.minecraft.world.entity.EntityType<?> targetType;
                    if (roll < 0.35F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.JIANGSHI.get();
                    } else if (roll < 0.65F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.DEMONIC_CULTIVATOR.get();
                    } else if (roll < 0.85F) {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.MOUNT_HUA_DISCIPLE.get();
                    } else {
                        targetType = com.soybe.awakeningsystem.entity.ModEntities.JADE_ARMORED_BEAST.get();
                    }
                    var customMob = targetType.create(serverLevel);
                    if (customMob != null) {
                        customMob.moveTo(monster.getX(), monster.getY(), monster.getZ(), monster.getYRot(), monster.getXRot());
                        serverLevel.addFreshEntity(customMob);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private static boolean isVanillaHostileMonster(net.minecraft.world.entity.Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.monster.Monster)) return false;
        net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null || !"minecraft".equals(key.getNamespace())) return false;
        return entity.getType() == net.minecraft.world.entity.EntityType.ZOMBIE
                || entity.getType() == net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER
                || entity.getType() == net.minecraft.world.entity.EntityType.SKELETON
                || entity.getType() == net.minecraft.world.entity.EntityType.SPIDER
                || entity.getType() == net.minecraft.world.entity.EntityType.CREEPER
                || entity.getType() == net.minecraft.world.entity.EntityType.CAVE_SPIDER
                || entity.getType() == net.minecraft.world.entity.EntityType.DROWNED
                || entity.getType() == net.minecraft.world.entity.EntityType.HUSK
                || entity.getType() == net.minecraft.world.entity.EntityType.STRAY;
    }

    public static void handleDimensionTravel(ServerPlayer player, String dimension) {
        if (player == null || player.getServer() == null) return;
        SystemData data = data(player);
        net.minecraft.server.MinecraftServer server = player.getServer();

        if ("overworld".equalsIgnoreCase(dimension)) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) return;
            BlockPos targetPos = player.getRespawnPosition();
            if (targetPos == null) targetPos = overworld.getSharedSpawnPos();
            player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, player.getYRot(), 0.0F);
            overworld.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5D, targetPos.getY() + 1.2D, targetPos.getZ() + 0.5D, 60, 1.0D, 1.0D, 1.0D, 0.1D);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.tower.returned_to_overworld"), true);
        } else if ("isekai".equalsIgnoreCase(dimension)) {
            if (!data.systemUpdated() && !data.isekaiUnlocked() && !data.system30Updated() && !data.system40Updated()) {
                player.displayClientMessage(Component.translatable("awakening_system.action.isekai_requires_sys20"), true);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                return;
            }
            IsekaiWorldManager.teleportToIsekai(player);
        } else if ("murim".equalsIgnoreCase(dimension)) {
            if (!data.system30Updated() && !data.murimUnlocked() && !data.system40Updated()) {
                player.displayClientMessage(Component.translatable("awakening_system.action.murim_requires_sys30"), true);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                return;
            }
            MurimWorldManager.teleportToMurim(player);
        } else if ("teyvat".equalsIgnoreCase(dimension)) {
            TeyvatWorldManager.teleportToTeyvat(player);
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(net.neoforged.neoforge.event.level.SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            long newTime = event.getNewTime();
            MinecraftServer server = level.getServer();
            if (server != null) {
                ServerLevel overworld = server.overworld();
                ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
                if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                    if (overworld != null) {
                        overworld.setDayTime(newTime);
                        if (overworld.isRaining()) overworld.setWeatherParameters(0, 0, false, false);
                    }
                    for (ServerPlayer sp : level.players()) {
                        sp.displayClientMessage(Component.literal("§e§l✦ [TEYVAT] §fHas descansado plácidamente. Un nuevo amanecer ilumina las tierras celestiales."), true);
                    }
                } else if (level.dimension().equals(Level.OVERWORLD)) {
                    if (teyvat != null) {
                        teyvat.setDayTime(newTime);
                        if (teyvat.isRaining()) teyvat.setWeatherParameters(0, 0, false, false);
                    }
                }
            }
        }
    }

    public static void activateArmorSkill(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;
        com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType activeSet = null;
        for (com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType type : com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.values()) {
            if (com.soybe.awakeningsystem.item.ManhwaArmorItem.hasFullSet(player, type)) {
                activeSet = type;
                break;
            }
        }

        if (activeSet == null) {
            player.displayClientMessage(Component.translatable("awakening_system.action.no_armor_skill"), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }

        long gameTime = player.level().getGameTime();
        ServerLevel sl = player.serverLevel();

        switch (activeSet) {
            case ABYSSAL -> {
                long cd = player.getPersistentData().getLong("abyssal_vortex_cd");
                if (gameTime < cd) {
                    long left = Math.max(1, (cd - gameTime) / 20L);
                    player.displayClientMessage(Component.literal("§9[Vórtice Abisal en enfriamiento: " + left + "s]"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }
                player.getPersistentData().putLong("abyssal_vortex_cd", gameTime + 200L);

                Vec3 pPos = player.position();
                List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D),
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                for (LivingEntity target : targets) {
                    Vec3 pull = pPos.subtract(target.position()).normalize().scale(1.2D);
                    target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.4D, pull.z));
                    target.hurt(sl.damageSources().magic(), 24.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
                sl.sendParticles(ParticleTypes.PORTAL, pPos.x, pPos.y + 1.0D, pPos.z, 120, 2.5D, 1.0D, 2.5D, 0.5D);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, pPos.x, pPos.y + 1.0D, pPos.z, 60, 1.5D, 1.0D, 1.5D, 0.2D);
                sl.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2F, 0.8F);
                player.displayClientMessage(Component.literal("§9§l¡VÓRTICE ABISAL DESATADO!"), true);
            }
            case MITHRIL -> {
                long cd = player.getPersistentData().getLong("mithril_blink_cd");
                if (gameTime < cd) {
                    long left = Math.max(1, (cd - gameTime) / 20L);
                    player.displayClientMessage(Component.literal("§b[Parpadeo Dimensional en enfriamiento: " + left + "s]"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }
                player.getPersistentData().putLong("mithril_blink_cd", gameTime + 140L);

                net.minecraft.world.phys.Vec3 start = player.position().add(0, player.getEyeHeight() * 0.5D, 0);
                net.minecraft.world.phys.Vec3 look = player.getLookAngle().normalize();
                double dist = 12.0D;
                net.minecraft.world.phys.Vec3 target = start.add(look.scale(dist));
                net.minecraft.world.level.ClipContext clipCtx = new net.minecraft.world.level.ClipContext(
                        start, target, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player);
                net.minecraft.world.phys.BlockHitResult hit = player.level().clip(clipCtx);
                net.minecraft.world.phys.Vec3 finalPos = hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                        ? hit.getLocation().subtract(look.scale(0.8D)) : target;

                double destX = finalPos.x;
                double destY = Math.max((double) player.level().getMinBuildHeight() + 1.0D, finalPos.y);
                double destZ = finalPos.z;

                int steps = 16;
                for (int i = 0; i <= steps; i++) {
                    double progress = (double) i / steps;
                    double px = start.x + (destX - start.x) * progress;
                    double py = start.y + (destY - start.y) * progress;
                    double pz = start.z + (destZ - start.z) * progress;
                    sl.sendParticles(ParticleTypes.CLOUD, px, py, pz, 2, 0.1D, 0.1D, 0.1D, 0.01D);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0, 0, 0, 0);
                    if (i % 4 == 0) {
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0, 0, 0, 0);
                    }
                }

                net.minecraft.world.phys.AABB lineBox = new net.minecraft.world.phys.AABB(start, new net.minecraft.world.phys.Vec3(destX, destY, destZ)).inflate(1.5D);
                var enemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, lineBox,
                        e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier"));
                for (var enemy : enemies) {
                    enemy.hurt(player.damageSources().playerAttack(player), 18.0F);
                    enemy.knockback(0.8D, -look.x, -look.z);
                    sl.sendParticles(ParticleTypes.CRIT, enemy.getX(), enemy.getY() + 0.8D, enemy.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.1D);
                }

                player.teleportTo(destX, destY, destZ);
                player.resetFallDistance();
                player.setDeltaMovement(look.scale(0.6D));
                player.hasImpulse = true;

                sl.playSound(null, destX, destY, destZ, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.4F);
                sl.playSound(null, destX, destY, destZ, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_blink.title", "awakening_system.notification.armor_skill_blink.desc", 0xFF55FFFF);
            }

            case ADAMANTITE -> {
                long cd = player.getPersistentData().getLong("adamantite_fissure_cd");
                if (gameTime < cd) {
                    long left = Math.max(1, (cd - gameTime) / 20L);
                    player.displayClientMessage(Component.literal("§c[Fisura de Magma en enfriamiento: " + left + "s]"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }
                player.getPersistentData().putLong("adamantite_fissure_cd", gameTime + 280L);

                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.75F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.5F, 0.6F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.8F);
                spawnArcaneCircleGround(sl, player, 6.5F, 60, 0xFFFF4500);

                double[] radii = {2.5D, 5.0D, 7.5D};
                for (double r : radii) {
                    int pts = (int) (r * 10);
                    for (int i = 0; i < pts; i++) {
                        double angle = (2.0D * Math.PI / pts) * i;
                        double px = player.getX() + Math.cos(angle) * r;
                        double pz = player.getZ() + Math.sin(angle) * r;
                        sl.sendParticles(ParticleTypes.LAVA, px, player.getY() + 0.1D, pz, 2, 0.1D, 0.5D, 0.1D, 0.05D);
                        sl.sendParticles(ParticleTypes.FLAME, px, player.getY() + 0.2D, pz, 3, 0.1D, 0.4D, 0.1D, 0.08D);
                        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, player.getY() + 0.1D, pz, 1, 0.05D, 0.2D, 0.05D, 0.02D);
                    }
                }
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 1.5D, 0.5D, 1.5D, 0.1D);

                var enemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(7.5D),
                        e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier"));
                for (var enemy : enemies) {
                    enemy.hurt(player.damageSources().playerAttack(player), 24.0F);
                    enemy.setRemainingFireTicks(160);
                    enemy.setDeltaMovement(enemy.getDeltaMovement().x * 0.3D, 0.95D, enemy.getDeltaMovement().z * 0.3D);
                    enemy.hurtMarked = true;
                }

                AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_magma.title", "awakening_system.notification.armor_skill_magma.desc", 0xFFFF4500);
            }

            case SPIRIT_JADE -> {
                long cd = player.getPersistentData().getLong("spirit_jade_bell_cd");
                if (gameTime < cd) {
                    long left = Math.max(1, (cd - gameTime) / 20L);
                    player.displayClientMessage(Component.literal("§a[Campana Dorada en enfriamiento: " + left + "s]"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }
                player.getPersistentData().putLong("spirit_jade_bell_cd", gameTime + 440L);
                player.getPersistentData().putLong("spirit_jade_bell_active_until", gameTime + 100L);

                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 2.0F, 1.0F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.3F);
                spawnMurimCircleGround(sl, player, 4.5F, 80, 0xFF50FA7B);

                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 1.0D, 1.0D, 1.0D, 0.1D);

                var enemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(5.0D),
                        e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier"));
                for (var enemy : enemies) {
                    enemy.knockback(1.8D, player.getX() - enemy.getX(), player.getZ() - enemy.getZ());
                }

                AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_jade_bell.title", "awakening_system.notification.armor_skill_jade_bell.desc", 0xFF55FF55);
            }

            case ASTRAL -> {
                long cd = player.getPersistentData().getLong("astral_burst_cd");
                if (gameTime < cd) {
                    long left = Math.max(1, (cd - gameTime) / 20L);
                    player.displayClientMessage(Component.literal("§5[Estallido Astral en enfriamiento: " + left + "s]"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    return;
                }
                player.getPersistentData().putLong("astral_burst_cd", gameTime + 360L);

                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.6F, 1.1F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.8F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.3F);
                spawnArcaneCircleGround(sl, player, 6.5F, 70, 0xFFE040FB);

                sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 2.0D, 1.5D, 2.0D, 0.15D);
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 2.5D, 2.0D, 2.5D, 0.2D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5D, player.getZ(), 5, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 1.5D, 1.5D, 1.5D, 0.25D);

                var enemies = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                        e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier"));
                for (var enemy : enemies) {
                    enemy.hurt(player.damageSources().playerAttack(player), 32.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                    double dx = enemy.getX() - player.getX();
                    double dz = enemy.getZ() - player.getZ();
                    enemy.knockback(1.5D, -dx, -dz);
                }

                AwakeningSystemMod.notify(player, "awakening_system.notification.armor_skill_astral_burst.title", "awakening_system.notification.armor_skill_astral_burst.desc", 0xFFFF55FF);
            }
        }
    }
}

