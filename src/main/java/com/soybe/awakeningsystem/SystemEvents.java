package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.ClassSkillTree.SkillNode;
import com.soybe.awakeningsystem.ConstellationDefinitions.Constellation;
import com.soybe.awakeningsystem.ConstellationDefinitions.ReactionType;
import com.soybe.awakeningsystem.DungeonManager.DungeonSession;
import com.soybe.awakeningsystem.PagodaTrialManager.PagodaSession;
import com.soybe.awakeningsystem.SellDefinitions.SellEntry;
import com.soybe.awakeningsystem.SkillDefinitions.Definition;
import com.soybe.awakeningsystem.SponsorQuestDefinitions.Quest;
import com.soybe.awakeningsystem.TeyvatWorldManager.AbyssPortalEntry;
import com.soybe.awakeningsystem.TowerManager.TowerSession;
import com.soybe.awakeningsystem.abyss.SpiralAbyssManager;
import com.soybe.awakeningsystem.artifact.ArtifactSet;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.BountyBoardBlock;
import com.soybe.awakeningsystem.block.CasinoVipGateBlock;
import com.soybe.awakeningsystem.block.ElementalMonumentBlock;
import com.soybe.awakeningsystem.block.GoddessAltarBlock;
import com.soybe.awakeningsystem.block.StatueOfTheSevenBlock;
import com.soybe.awakeningsystem.block.SubwayGrabHandleBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TowerMissionBoardBlock;
import com.soybe.awakeningsystem.block.TrilunarAltarBlock;
import com.soybe.awakeningsystem.block.GoddessAltarBlock.TrialState;
import com.soybe.awakeningsystem.block.TrilunarAltarBlock.TrialSession;
import com.soybe.awakeningsystem.casino.BlackjackServerHandler;
import com.soybe.awakeningsystem.compat.CreateCompatHandler;
import com.soybe.awakeningsystem.compat.CuriosCompatHandler;
import com.soybe.awakeningsystem.compat.iceandfire.IceAndFireCompat;
import com.soybe.awakeningsystem.config.BossCompatibilityManager;
import com.soybe.awakeningsystem.config.CustomLootConfigManager;
import com.soybe.awakeningsystem.effect.ModMobEffects;
import com.soybe.awakeningsystem.element.ElementType;
import com.soybe.awakeningsystem.element.ElementalReactionManager;
import com.soybe.awakeningsystem.element.ElementalReactionManager.ReactionResult;
import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.entity.AstralDragonEntity;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
import com.soybe.awakeningsystem.entity.BeruBossEntity;
import com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity;
import com.soybe.awakeningsystem.entity.CelestialDragonEntity;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.entity.CryoRegisvineEntity;
import com.soybe.awakeningsystem.entity.DemonicCultivatorEntity;
import com.soybe.awakeningsystem.entity.DokkaebiEntity;
import com.soybe.awakeningsystem.entity.DragonBodyPart;
import com.soybe.awakeningsystem.entity.ElectroRegisvineEntity;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
import com.soybe.awakeningsystem.entity.ElementalLordBossEntity;
import com.soybe.awakeningsystem.entity.GoblinShamanEntity;
import com.soybe.awakeningsystem.entity.GreatSnowboarKingBossEntity;
import com.soybe.awakeningsystem.entity.IgrisBossEntity;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity;
import com.soybe.awakeningsystem.entity.JadeGuardianBossEntity;
import com.soybe.awakeningsystem.entity.JiangshiEntity;
import com.soybe.awakeningsystem.entity.LimboEntity;
import com.soybe.awakeningsystem.entity.LimboOphanimEntity;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import com.soybe.awakeningsystem.entity.MeditationSeatEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import com.soybe.awakeningsystem.entity.PaimonEntity;
import com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity;
import com.soybe.awakeningsystem.entity.PyroRegisvineEntity;
import com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity;
import com.soybe.awakeningsystem.entity.RuinSerpentBossEntity;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity;
import com.soybe.awakeningsystem.entity.ShadowSoulEntity;
import com.soybe.awakeningsystem.entity.StatueOfGodEntity;
import com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import com.soybe.awakeningsystem.fable.FableDefinitions;
import com.soybe.awakeningsystem.fable.FableWorldSavedData;
import com.soybe.awakeningsystem.fable.FableDefinitions.Fable;
import com.soybe.awakeningsystem.fable.FableDefinitions.Grade;
import com.soybe.awakeningsystem.item.AwakeningWeaponItem;
import com.soybe.awakeningsystem.item.DoctorsRobeItem;
import com.soybe.awakeningsystem.item.LaDoncellaSwordItem;
import com.soybe.awakeningsystem.item.ManaPotionItem;
import com.soybe.awakeningsystem.item.ManhwaArmorItem;
import com.soybe.awakeningsystem.item.ManhwaArtifactItem;
import com.soybe.awakeningsystem.item.RobeOfTheBeautyItem;
import com.soybe.awakeningsystem.item.ShadowDaggersItem;
import com.soybe.awakeningsystem.item.TeyvatVisionItem;
import com.soybe.awakeningsystem.item.VigorPotionItem;
import com.soybe.awakeningsystem.item.WeaponLevelManager;
import com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType;
import com.soybe.awakeningsystem.item.ManhwaArtifactItem.ArtifactType;
import com.soybe.awakeningsystem.item.RuneStoneItem.RuneType;
import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import com.soybe.awakeningsystem.network.SystemAnnouncementPayload;
import com.soybe.awakeningsystem.subspace.SubspaceManager;
import com.soybe.awakeningsystem.tower.GreatTowerWorldManager;
import com.soybe.awakeningsystem.tower.TowerDailyMissionManager;
import com.soybe.awakeningsystem.uma.TeyvatRacetrackManager;
import com.soybe.awakeningsystem.world.SeriesStructureBuilder;
import com.soybe.awakeningsystem.world.SeriesStructureWorldGen;
import com.soybe.awakeningsystem.world.TeyvatCasinoBuilder;
import com.soybe.awakeningsystem.world.TeyvatWorldGenManager;
import com.soybe.awakeningsystem.world.WorldBossScalingManager;
import com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData;
import com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.event.level.ChunkEvent.Load;
import net.neoforged.neoforge.event.level.ExplosionEvent.Detonate;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class SystemEvents {
   private static final Map<UUID, NonNullList<ItemStack>> SAVED_DUNGEON_DEATH_INVENTORIES = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> WELKIN_OFFERED_DAYS = new ConcurrentHashMap<>();
   private static final ThreadLocal<Boolean> IS_PROCESSING_DAMAGE = ThreadLocal.withInitial(() -> Boolean.FALSE);
   public static final Map<UUID, SystemEvents.PlayerChargingState> ACTIVE_CHARGES = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> LAST_COMBAT_CONSTELLATION_REACTION = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> LAST_LEVEL_UP_CONSTELLATION_REACTION = new ConcurrentHashMap<>();
   private static UUID lastDokkaebiAnchorUuid = null;
   private static final ResourceLocation NETHERITE_CHEST_BUFF = ResourceLocation.fromNamespaceAndPath("awakening_system", "netherite_chest_buff");
   private static final ResourceLocation NETHERITE_LEGS_BUFF = ResourceLocation.fromNamespaceAndPath("awakening_system", "netherite_legs_buff");

   private SystemEvents() {
   }

   public static SystemData data(Player player) {
      return (SystemData)player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            if (player.level() instanceof ServerLevel teyvatLevel) {
               TeyvatWorldManager.ensureTeyvatCityAndStructures(teyvatLevel);
               TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(teyvatLevel);
               if (manager != null) {
                  manager.pruneDuplicateRunners();
               }
            }

            ensurePaimonInTeyvat(player);
         }

         player.getPersistentData().putBoolean("awakening_attached", true);
         refreshPlayer(player);
         SystemData data = data(player);
         if (TitleDefinitions.isCreator(player)) {
            boolean wasCreator = data.isCreator();
            data.setCreator(true);
            if (!wasCreator) {
               if (data.equippedTitle().isEmpty()) {
                  data.setEquippedTitle("creator");
               }

               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.title.creator_unlocked.title")));
               player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.title.creator_unlocked.subtitle")));
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.creator_title.title", "awakening_system.notification.creator_title.message", -10496, new Object[0]
               );
            }
         } else if (TitleDefinitions.isNanda(player)) {
            boolean wasNanda = data.isNanda();
            data.setNanda(true);
            if (!wasNanda || data.equippedTitle().isEmpty()) {
               data.setEquippedTitle("michi_lover");
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
               player.playNotifySound(SoundEvents.CAT_PURREOW, SoundSource.PLAYERS, 1.0F, 1.0F);
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.title.michi_lover_unlocked.title")));
               player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.title.michi_lover_unlocked.subtitle")));
               if (!wasNanda) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.michi_lover_title.title",
                     "awakening_system.notification.michi_lover_title.message",
                     -34868,
                     new Object[0]
                  );
               }
            }
         } else if (TitleDefinitions.isMisticPotato(player)) {
            boolean wasMistic = data.isMisticPotato();
            data.setMisticPotato(true);
            if (!wasMistic || data.equippedTitle().isEmpty()) {
               data.setEquippedTitle("caffeine_addict");
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
               player.playNotifySound(SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.title.caffeine_addict_unlocked.title")));
               player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.title.caffeine_addict_unlocked.subtitle")));
               if (!wasMistic) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.caffeine_addict_title.title",
                     "awakening_system.notification.caffeine_addict_title.message",
                     -2982611,
                     new Object[0]
                  );
               }
            }
         } else if (TitleDefinitions.isSchvis(player)) {
            boolean wasSchvis = data.isSchvis();
            data.setSchvis(true);
            if (!wasSchvis || data.equippedTitle().isEmpty()) {
               data.setEquippedTitle("futanari_hentai_master");
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
               player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.connection
                  .send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.title.futanari_hentai_master_unlocked.title")));
               player.connection
                  .send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.title.futanari_hentai_master_unlocked.subtitle")));
               if (!wasSchvis) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.futanari_hentai_master_title.title",
                     "awakening_system.notification.futanari_hentai_master_title.message",
                     -38476,
                     new Object[0]
                  );
               }
            }
         } else if (TitleDefinitions.isTomcraft(player)) {
            boolean wasTomcraft = data.isTomcraft();
            data.setTomcraft(true);
            if (!wasTomcraft || data.equippedTitle().isEmpty()) {
               data.setEquippedTitle("pokemon_master");
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
               player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.45F, 1.8F);
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.title.pokemon_master_unlocked.title")));
               player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.title.pokemon_master_unlocked.subtitle")));
               if (!wasTomcraft) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.pokemon_master_title.title",
                     "awakening_system.notification.pokemon_master_title.message",
                     -52429,
                     new Object[0]
                  );
               }
            }
         } else if ((
               data.conqueredRankerTower()
                  || data.conqueredNineTribulationsPagoda()
                  || data.conqueredBloodSect()
                  || data.conqueredYggdrasilLabyrinth()
                  || data.bossKills() >= 1
                  || data.level() >= 40
            )
            && data.equippedTitle().isEmpty()) {
            data.setEquippedTitle("mod_conqueror");
            AwakeningSystemMod.sync(player);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
            player.sendSystemMessage(Component.translatable("awakening_system.title.mod_conqueror_unlocked"));
         }

         AwakeningSystemMod.syncAllBadgesToPlayer(player);
         data.addVisitedDimension(player.level().dimension().location().toString());
         long today = player.level().getDayTime() / 24000L;
         if (data.lastDailyLoginDay() != today) {
            data.setLastDailyLoginDay(today);
            int bonusCoins = (Integer)SystemConfig.DAILY_LOGIN_BONUS_COINS.get();
            int bonusExp = (Integer)SystemConfig.DAILY_LOGIN_BONUS_EXP.get();
            if (bonusCoins > 0) {
               data.addCoins(bonusCoins);
            }

            if (bonusExp > 0) {
               awardExperience(player, data, bonusExp);
            }

            AwakeningSystemMod.notify(
               player, "awakening_system.notification.daily_login.title", "awakening_system.notification.daily_login.message", -10138, new Object[]{bonusCoins}
            );
            AwakeningSystemMod.sync(player);
         }

         if (data.hasNoAffinityProgress()) {
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.awakened.title", "awakening_system.notification.awakened.message", -9377537, new Object[0]
            );
         }

         if (data.hasHunterClass()) {
            grantFable(player, "first_awakening");
         }

         if (data.hasHunterClass() && data.level() >= 50) {
            int expectedTotal = 1 + (data.level() - 50) / 5;
            int actualTotal = data.skillPoints() + data.spentSkillPoints();
            if (actualTotal < expectedTotal) {
               data.addSkillPoints(expectedTotal - actualTotal);
               AwakeningSystemMod.sync(player);
            }
         }

         if (SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound()) {
            int guideCount = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
               ItemStack it = player.getInventory().getItem(i);
               if (it.is((Item)SystemItems.HUNTER_GUIDE.value())) {
                  if (++guideCount > 1) {
                     player.getInventory().setItem(i, ItemStack.EMPTY);
                  } else if (it.getCount() > 1) {
                     it.setCount(1);
                  }
               }
            }

            AwakeningPlayerRegistryData registryData = player.getServer() != null ? AwakeningPlayerRegistryData.get(player.getServer()) : null;
            CompoundTag persistedTag = player.getPersistentData().getCompound("PlayerPersisted");
            boolean alreadyReceived = data.hasReceivedHunterGuide()
               || player.getPersistentData().getBoolean("awakening_received_guide")
               || persistedTag.getBoolean("awakening_received_guide")
               || registryData != null && registryData.hasReceivedHunterGuide(player.getUUID());
            if (guideCount <= 0 && !alreadyReceived) {
               data.setReceivedHunterGuide(true);
               player.getPersistentData().putBoolean("awakening_received_guide", true);
               persistedTag.putBoolean("awakening_received_guide", true);
               player.getPersistentData().put("PlayerPersisted", persistedTag);
               if (registryData != null) {
                  registryData.markReceivedHunterGuide(player.getUUID());
               }

               player.addItem(new ItemStack((ItemLike)SystemItems.HUNTER_GUIDE.get()));
            } else {
               data.setReceivedHunterGuide(true);
               player.getPersistentData().putBoolean("awakening_received_guide", true);
               persistedTag.putBoolean("awakening_received_guide", true);
               player.getPersistentData().put("PlayerPersisted", persistedTag);
               if (registryData != null) {
                  registryData.markReceivedHunterGuide(player.getUUID());
               }
            }
         }

         if (SystemItems.HUNTER_BESTIARY != null && SystemItems.HUNTER_BESTIARY.isBound()) {
            int bestiaryCount = 0;

            for (int ix = 0; ix < player.getInventory().getContainerSize(); ix++) {
               ItemStack it = player.getInventory().getItem(ix);
               if (it.is((Item)SystemItems.HUNTER_BESTIARY.value())) {
                  if (++bestiaryCount > 1) {
                     player.getInventory().setItem(ix, ItemStack.EMPTY);
                  } else if (it.getCount() > 1) {
                     it.setCount(1);
                  }
               }
            }

            CompoundTag persistedTag = player.getPersistentData().getCompound("PlayerPersisted");
            boolean alreadyReceivedBestiary = player.getPersistentData().getBoolean("awakening_received_bestiary")
               || persistedTag.getBoolean("awakening_received_bestiary");
            if (bestiaryCount <= 0 && !alreadyReceivedBestiary) {
               player.getPersistentData().putBoolean("awakening_received_bestiary", true);
               persistedTag.putBoolean("awakening_received_bestiary", true);
               player.getPersistentData().put("PlayerPersisted", persistedTag);
               player.addItem(new ItemStack((ItemLike)SystemItems.HUNTER_BESTIARY.get()));
            } else {
               player.getPersistentData().putBoolean("awakening_received_bestiary", true);
               persistedTag.putBoolean("awakening_received_bestiary", true);
               player.getPersistentData().put("PlayerPersisted", persistedTag);
            }
         }

         DungeonManager.syncRiftsToPlayer(player);
         if (player.getServer() != null) {
            String masterTag = "shadow_master_" + player.getUUID();

            for (ServerLevel sLevel : player.getServer().getAllLevels()) {
               for (Entity ent : sLevel.getAllEntities()) {
                  if (ent instanceof Mob m) {
                     if (!m.getTags().contains(masterTag) && !m.getTags().contains("shadow_soldier")) {
                        if (ent instanceof CompanionEntity comp && player.getUUID().equals(comp.getOwnerUUID())) {
                           comp.setTarget(null);
                           comp.setLastHurtByMob(null);
                        }
                     } else {
                        if (m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID())) {
                           m.setTarget(null);
                        }

                        if (m.getLastHurtByMob() != null && m.getLastHurtByMob().getUUID().equals(player.getUUID())) {
                           m.setLastHurtByMob(null);
                        }
                     }
                  }
               }
            }
         }

         if (GreatTowerWorldManager.isGreatTowerDimension(player.serverLevel())) {
            GreatTowerWorldManager.syncBorderToPlayer(player);
         }

         DungeonManager.onPlayerLogin(player);
         TowerManager.onPlayerLogin(player);
         PagodaTrialManager.onPlayerLogin(player);
         if (data.teyvatUnlocked()) {
            VersionBannerManager.checkAndAwardMaintenanceCompensation(player, data);
         }

         List<ItemStack> pendingUma = TeyvatRacetrackManager.claimPendingRewards(player.getUUID());
         if (!pendingUma.isEmpty()) {
            for (ItemStack reward : pendingUma) {
               if (!player.getInventory().add(reward)) {
                  player.drop(reward, false);
               }
            }

            player.sendSystemMessage(Component.translatable("awakening_system.derby.offline_earnings"));
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
         }

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
   public static void onPlayerClone(Clone event) {
      if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
         try {
            CompoundTag systemNbt = ((SystemData)oldPlayer.getData(AwakeningSystemMod.PLAYER_SYSTEM)).serializeNBT(oldPlayer.registryAccess());
            ((SystemData)newPlayer.getData(AwakeningSystemMod.PLAYER_SYSTEM)).deserializeNBT(newPlayer.registryAccess(), systemNbt);
         } catch (Exception var11) {
         }

         CompoundTag oldPersisted = oldPlayer.getPersistentData().getCompound("PlayerPersisted");
         if (oldPlayer.getPersistentData().getBoolean("awakening_received_guide") || oldPersisted.getBoolean("awakening_received_guide")) {
            newPlayer.getPersistentData().putBoolean("awakening_received_guide", true);
            CompoundTag newPersisted = newPlayer.getPersistentData().getCompound("PlayerPersisted");
            newPersisted.putBoolean("awakening_received_guide", true);
            newPlayer.getPersistentData().put("PlayerPersisted", newPersisted);
         }

         if (event.isWasDeath()) {
            if (oldPlayer.getPersistentData().getBoolean("died_in_dungeon_limbo")) {
               newPlayer.getPersistentData().putBoolean("died_in_dungeon_limbo", true);
            }

            NonNullList<ItemStack> saved = SAVED_DUNGEON_DEATH_INVENTORIES.remove(oldPlayer.getUUID());
            boolean restored = false;
            if (saved != null) {
               for (int i = 0; i < saved.size() && i < newPlayer.getInventory().getContainerSize(); i++) {
                  newPlayer.getInventory().setItem(i, ((ItemStack)saved.get(i)).copy());
               }

               restored = true;
            } else if (oldPlayer.getPersistentData().contains("dungeon_preserved_inventory", 9)) {
               ListTag listTag = oldPlayer.getPersistentData().getList("dungeon_preserved_inventory", 10);

               for (int i = 0; i < listTag.size(); i++) {
                  CompoundTag slotTag = listTag.getCompound(i);
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

            CuriosCompatHandler.restoreCuriosOnRespawn(oldPlayer, newPlayer);
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDrops(LivingDropsEvent event) {
      if (event.getEntity() instanceof ServerPlayer player
         && (
            DungeonManager.isInDungeon(player.getUUID())
               || TowerManager.isInTower(player.getUUID())
               || PagodaTrialManager.isInTrial(player.getUUID())
               || player.level().dimension().equals(DungeonManager.DUNGEON_REALM)
               || player.level().dimension().equals(TowerManager.RANKER_TOWER)
               || player.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
               || SAVED_DUNGEON_DEATH_INVENTORIES.containsKey(player.getUUID())
               || player.getPersistentData().contains("dungeon_preserved_inventory", 9)
         )) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SystemData data = data(player);
         data.recordStatistic("deaths", 1);
         if (player.getServer() != null) {
            DungeonManager.exitDungeon(player.getServer(), player.getUUID());
            TowerManager.onPlayerRespawn(player);
            PagodaTrialManager.onPlayerRespawn(player);
         }

         if (player.getPersistentData().getBoolean("died_in_dungeon_limbo")) {
            player.getPersistentData().remove("died_in_dungeon_limbo");
            player.clearFire();
            player.fallDistance = 0.0F;
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.setHealth(player.getMaxHealth());
            data.setMana(data.maxMana());
            player.invulnerableTime = 60;
            LimboWorldManager.teleportToLimbo(player);
            player.sendSystemMessage(Component.translatable("awakening_system.limbo.dungeon_death_rescue"));
            refreshPlayer(player);
            return;
         }

         if (!player.getPersistentData().getBoolean("dungeon_loot_preserved")) {
            NonNullList<ItemStack> saved = SAVED_DUNGEON_DEATH_INVENTORIES.remove(player.getUUID());
            boolean fallbackRestored = false;
            if (saved != null) {
               for (int i = 0; i < saved.size() && i < player.getInventory().getContainerSize(); i++) {
                  player.getInventory().setItem(i, ((ItemStack)saved.get(i)).copy());
               }

               fallbackRestored = true;
            } else if (player.getPersistentData().contains("dungeon_preserved_inventory", 9)) {
               ListTag listTag = player.getPersistentData().getList("dungeon_preserved_inventory", 10);

               for (int i = 0; i < listTag.size(); i++) {
                  CompoundTag slotTag = listTag.getCompound(i);
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

         CuriosCompatHandler.restoreCuriosOnRespawn(player, player);
         refreshPlayer(player);
         player.setHealth(player.getMaxHealth());
         data.setMana(data.maxMana());
         DungeonManager.syncRiftsToPlayer(player);
         if (player.getServer() != null) {
            String masterTag = "shadow_master_" + player.getUUID();

            for (ServerLevel sLevel : player.getServer().getAllLevels()) {
               for (Entity ent : sLevel.getAllEntities()) {
                  if (ent instanceof Mob m) {
                     if (!m.getTags().contains(masterTag) && !m.getTags().contains("shadow_soldier")) {
                        if (ent instanceof CompanionEntity comp && player.getUUID().equals(comp.getOwnerUUID())) {
                           comp.setTarget(null);
                           comp.setLastHurtByMob(null);
                        }
                     } else {
                        if (m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID())) {
                           m.setTarget(null);
                        }

                        if (m.getLastHurtByMob() != null && m.getLastHurtByMob().getUUID().equals(player.getUUID())) {
                           m.setLastHurtByMob(null);
                        }
                     }
                  }
               }
            }
         }

         if (GreatTowerWorldManager.isGreatTowerDimension(player.serverLevel())) {
            GreatTowerWorldManager.syncBorderToPlayer(player);
         }

         if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            ensurePaimonInTeyvat(player);
         }

         AwakeningSystemMod.sync(player);
         AwakeningSystemMod.syncPlayerBadge(player);
         if (player.getServer() != null) {
            MinecraftServer server = player.getServer();
            server.tell(new TickTask(server.getTickCount() + 5, () -> {
               if (player.isAlive() && !player.hasDisconnected()) {
                  refreshPlayer(player);
                  AwakeningSystemMod.sync(player);
               }
            }));
         }
      }
   }

   @SubscribeEvent
   public static void onStartTracking(StartTracking event) {
      if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
         AwakeningSystemMod.syncPlayerBadge(target);
         AwakeningSystemMod.syncPlayerBadge(watcher);
      }
   }

   @SubscribeEvent
   public static void onEntityMount(EntityMountEvent event) {
      if (event.isMounting() && event.getEntityMounting() instanceof ServerPlayer sp) {
         CreateCompatHandler.onEntityMount(sp, event.getEntityBeingMounted());
      }
   }

   public static boolean grantFable(ServerPlayer player, String fableId) {
      if (player != null && fableId != null) {
         Fable fable = FableDefinitions.get(fableId);
         if (fable == null) {
            return false;
         } else {
            SystemData data = data(player);
            if (data != null && !data.hasFable(fableId)) {
               MinecraftServer server = player.getServer();
               boolean isFirstInWorld = false;
               if (server != null && fable.isWorldFirst()) {
                  FableWorldSavedData worldData = FableWorldSavedData.get(server);
                  isFirstInWorld = worldData.tryClaimWorldFirst(fableId, player.getUUID(), player.getScoreboardName());
               }

               int effectiveWeight = fable.weight() + (isFirstInWorld ? 15 : 0);
               if (!data.unlockFable(fableId, effectiveWeight)) {
                  return false;
               } else {
                  if (server != null) {
                     String pName = player.getScoreboardName();
                     boolean isSuperRare = fable.grade() == Grade.SEMI_MYTH || fable.grade() == Grade.MYTH || fable.grade() == Grade.GIANT_STORY;
                     if (isSuperRare) {
                        String bannerTitle = switch (fable.grade()) {
                           case GIANT_STORY -> "✦ GIANT STORY FABLE OBTAINED! ✦";
                           case MYTH -> "✦ ANCESTRAL MYTHIC FABLE UNLOCKED! ✦";
                           default -> "✦ SEMI-MYTHIC FABLE OBTAINED! ✦";
                        };
                        String bannerDesc = pName + " acquired: " + fable.grade().colorCode() + "[" + fable.title() + "]";
                        PacketDistributor.sendToAllPlayers(
                           new SystemAnnouncementPayload(bannerTitle, bannerDesc, fable.grade().hexColor()), new CustomPacketPayload[0]
                        );
                     }

                     for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                        p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.2F);
                        if (isFirstInWorld) {
                           AwakeningSystemMod.notify(
                              p,
                              "awakening_system.notification.fable_world_first.title",
                              "awakening_system.notification.fable_world_first.desc",
                              fable.grade().hexColor(),
                              new Object[]{pName, fable.grade().colorCode() + "[" + fable.title() + "]"}
                           );
                        } else if (p.getUUID().equals(player.getUUID())) {
                           AwakeningSystemMod.notify(
                              p,
                              "awakening_system.notification.fable_acquired.title",
                              "awakening_system.notification.fable_acquired.desc",
                              fable.grade().hexColor(),
                              new Object[]{fable.grade().colorCode() + "[" + fable.title(), effectiveWeight}
                           );
                        }
                     }
                  }

                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 45, 0.8, 1.0, 0.8, 0.1);
                     sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.6, 0.8, 0.6, 0.2);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
                  }

                  if (data.unlockedFables().size() >= 15 && !data.hasFable("giant_story_myth_creator")) {
                     grantFable(player, "giant_story_myth_creator");
                  }

                  ConstellationAscension.checkFableAscension(player);
                  AwakeningSystemMod.sync(player);
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
         DungeonManager.exitDungeon(player.getServer(), player.getUUID());
         TowerManager.onPlayerLogout(player);
         PagodaTrialManager.onPlayerLogout(player);
         BlackjackServerHandler.onPlayerLogout(player);
         ACTIVE_CHARGES.remove(player.getUUID());
         LAST_COMBAT_CONSTELLATION_REACTION.remove(player.getUUID());
         LAST_LEVEL_UP_CONSTELLATION_REACTION.remove(player.getUUID());
         WELKIN_OFFERED_DAYS.remove(player.getUUID());
         SubwayGrabHandleBlock.GRABBED_PLAYERS.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onServerStarting(ServerStartingEvent event) {
      TeyvatRacetrackManager.clearInstances();
      TeyvatWorldManager.ensureMondstadtOnServerStart(event.getServer());
      DungeonWorldSavedData.get(event.getServer()).ensureRiftsLoaded(event.getServer());
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      TeyvatRacetrackManager.clearInstances();
      DungeonManager.clearAll();
      TowerManager.clearAll();
      PagodaTrialManager.clearAll();
      PhysicalRouletteManager.clearAll();
      SpiralAbyssManager.clearAll();
      SeriesStructureWorldGen.clearCache();
      TeyvatWorldGenManager.clearCache();
      ElementalReactionManager.clearAll();
      SAVED_DUNGEON_DEATH_INVENTORIES.clear();
      WELKIN_OFFERED_DAYS.clear();
      ACTIVE_CHARGES.clear();
      LAST_COMBAT_CONSTELLATION_REACTION.clear();
      LAST_LEVEL_UP_CONSTELLATION_REACTION.clear();
      SubwayGrabHandleBlock.GRABBED_PLAYERS.clear();
   }

   @SubscribeEvent
   public static void onChunkLoad(Load event) {
      if (event.getLevel() instanceof ServerLevel serverLevel) {
         SeriesStructureWorldGen.onChunkLoaded(serverLevel, event.getChunk().getPos());
         DungeonManager.onChunkLoadedForExploration(serverLevel, event.getChunk().getPos());
         if (GreatTowerWorldManager.isGreatTowerDimension(serverLevel)) {
            GreatTowerWorldManager.onChunkLoaded(serverLevel, event.getChunk());
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         DungeonManager.tick(server);
         TowerManager.tick(server);
         PagodaTrialManager.tick(server);
         PhysicalRouletteManager.tick(server);
         TeyvatWorldManager.tick(server);
         tickShadowSoldiers(server);

         for (ServerLevel sl : server.getAllLevels()) {
            ElementalReactionManager.tickServer(sl);
         }

         if (server.getTickCount() % 40 == 0) {
            SeriesStructureWorldGen.tickNaturalGeneration(server);
         }

         tickDokkaebiServerEvent(server);
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         data(player).addVisitedDimension(event.getTo().location().toString());
         if (event.getTo().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            SystemData pData = data(player);
            if (!pData.teyvatUnlocked()) {
               pData.setTeyvatUnlocked(true);
               if (pData.gachaEpochTick() == 0L) {
                  pData.setGachaEpochTick(player.serverLevel().getDayTime());
                  pData.setLastClaimedMaintenanceVersion(1);
               }

               AwakeningSystemMod.sync(player);
            }

            if (player.level() instanceof ServerLevel teyvatLevel) {
               TeyvatWorldManager.ensureTeyvatCityAndStructures(teyvatLevel);
               TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(teyvatLevel);
               if (manager != null) {
                  manager.pruneDuplicateRunners();
               }
            }

            ensurePaimonInTeyvat(player);
         } else if (event.getFrom().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            dismissTeyvatPaimon(player);
         }

         if (event.getTo().equals(Level.NETHER)) {
            grantFable(player, "nether_traveler");
         } else if (event.getTo().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            grantFable(player, "isekai_pioneer");
         } else if (event.getTo().equals(MurimWorldManager.MURIM_WORLD)) {
            grantFable(player, "murim_celestial_cultivator");
         } else if (event.getTo().location().getPath().contains("limbo")) {
            grantFable(player, "limbo_void_walker");
         }

         refreshPlayer(player);
         teleportShadowsAcrossDimensions(player);
         DungeonManager.syncRiftsToPlayer(player);
         if (player.getServer() != null) {
            String masterTag = "shadow_master_" + player.getUUID();

            for (ServerLevel sLevel : player.getServer().getAllLevels()) {
               for (Entity ent : sLevel.getAllEntities()) {
                  if (ent instanceof Mob m) {
                     if (!m.getTags().contains(masterTag) && !m.getTags().contains("shadow_soldier")) {
                        if (ent instanceof CompanionEntity comp && player.getUUID().equals(comp.getOwnerUUID())) {
                           comp.setTarget(null);
                           comp.setLastHurtByMob(null);
                        }
                     } else {
                        if (m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID())) {
                           m.setTarget(null);
                        }

                        if (m.getLastHurtByMob() != null && m.getLastHurtByMob().getUUID().equals(player.getUUID())) {
                           m.setLastHurtByMob(null);
                        }
                     }
                  }
               }
            }
         }

         if (GreatTowerWorldManager.isGreatTowerDimension(player.serverLevel())) {
            GreatTowerWorldManager.syncBorderToPlayer(player);
         }

         AwakeningSystemMod.sync(player);
         AwakeningSystemMod.syncPlayerBadge(player);
         if (player.getServer() != null) {
            MinecraftServer server = player.getServer();
            server.tell(new TickTask(server.getTickCount() + 5, () -> {
               if (player.isAlive() && !player.hasDisconnected()) {
                  refreshPlayer(player);
                  AwakeningSystemMod.sync(player);
                  AwakeningSystemMod.syncPlayerBadge(player);
               }
            }));
            server.tell(new TickTask(server.getTickCount() + 20, () -> {
               if (player.isAlive() && !player.hasDisconnected()) {
                  refreshPlayer(player);
                  AwakeningSystemMod.sync(player);
                  AwakeningSystemMod.syncPlayerBadge(player);
               }
            }));
         }
      }
   }

   public static void ensurePaimonInTeyvat(ServerPlayer player) {
      if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
         if (player.level() instanceof ServerLevel sl) {
            List<PaimonEntity> nearby = sl.getEntitiesOfClass(
               PaimonEntity.class,
               player.getBoundingBox().inflate(128.0),
               p -> p.isAlive() && (player.getUUID().equals(p.getBoundPlayerUuid()) || p.getBoundPlayerUuid() == null)
            );
            if (nearby.isEmpty()) {
               PaimonEntity paimon = new PaimonEntity((EntityType)ModEntities.PAIMON.get(), sl);
               Vec3 look = player.getLookAngle();
               Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
               double spawnX = player.getX() + right.x * 2.0 - look.x * 2.0;
               double spawnZ = player.getZ() + right.z * 2.0 - look.z * 2.0;
               double spawnY = player.getY() + 1.4;
               paimon.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 0.0F);
               paimon.setBoundPlayer(player);
               paimon.addTag("teyvat_autospawn");
               sl.addFreshEntity(paimon);
               sl.sendParticles(ParticleTypes.END_ROD, spawnX, spawnY + 0.5, spawnZ, 30, 0.3, 0.4, 0.3, 0.05);
               sl.sendParticles(ParticleTypes.ENCHANT, spawnX, spawnY + 0.5, spawnZ, 25, 0.4, 0.4, 0.4, 0.1);
               sl.playSound(null, spawnX, spawnY, spawnZ, (SoundEvent)ModSounds.PAIMON_SPAWN.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
               player.displayClientMessage(Component.translatable("awakening_system.teyvat.paimon_welcome"), false);
            } else {
               PaimonEntity primary = nearby.get(0);
               primary.setBoundPlayer(player);
               primary.addTag("teyvat_autospawn");

               for (int i = 1; i < nearby.size(); i++) {
                  PaimonEntity dup = nearby.get(i);
                  sl.sendParticles(ParticleTypes.POOF, dup.getX(), dup.getY() + 0.5, dup.getZ(), 15, 0.2, 0.2, 0.2, 0.05);
                  dup.discard();
               }
            }
         }
      }
   }

   public static void dismissTeyvatPaimon(ServerPlayer player) {
      if (player.getServer() != null) {
         ServerLevel teyvatLevel = player.getServer().getLevel(TeyvatWorldManager.TEYVAT_WORLD);
         if (teyvatLevel != null) {
            for (PaimonEntity paimon : teyvatLevel.getEntitiesOfClass(
               PaimonEntity.class,
               player.getBoundingBox().inflate(160.0),
               p -> player.getUUID().equals(p.getBoundPlayerUuid()) || p.getBoundPlayerUuid() == null && p.getTags().contains("teyvat_autospawn")
            )) {
               teyvatLevel.sendParticles(ParticleTypes.PORTAL, paimon.getX(), paimon.getY() + 0.5, paimon.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
               teyvatLevel.playSound(
                  null, paimon.getX(), paimon.getY(), paimon.getZ(), (SoundEvent)ModSounds.PAIMON_VANISH.get(), SoundSource.PLAYERS, 1.0F, 1.0F
               );
               paimon.discard();
            }
         }
      }
   }

   private static void teleportDistantShadowSoldiers(ServerPlayer player) {
      if (player.level() instanceof ServerLevel sl) {
         String var17 = "shadow_master_" + player.getUUID();

         for (Mob soldier : sl.getEntitiesOfClass(
            Mob.class, player.getBoundingBox().inflate(128.0), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(var17)
         )) {
            double distSq = soldier.distanceToSqr(player);
            if (distSq > 3600.0) {
               double angle = Math.toRadians(player.getYRot() + 180.0F + (soldier.getId() % 5 - 2) * 25.0F);
               double tx = player.getX() + Math.sin(angle) * 2.5;
               double tz = player.getZ() - Math.cos(angle) * 2.5;
               double ty = player.getY();
               sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
               sl.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 25, 0.3, 0.4, 0.3, 0.02);
               Vec3 safePos = TeleportHelper.findSafeTeleportPos(sl, soldier, tx, ty, tz, player.position());
               soldier.teleportTo(safePos.x, safePos.y, safePos.z);
               sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, safePos.x, safePos.y + 0.5, safePos.z, 25, 0.3, 0.5, 0.3, 0.05);
               sl.sendParticles(ParticleTypes.PORTAL, safePos.x, safePos.y + 0.5, safePos.z, 20, 0.2, 0.3, 0.2, 0.1);
               sl.playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.2F);
               soldier.setTarget(null);
            }
         }
      }
   }

   private static void teleportShadowsAcrossDimensions(ServerPlayer player) {
      if (player.getServer() != null) {
         String masterTag = "shadow_master_" + player.getUUID();
         ServerLevel newLevel = player.serverLevel();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            if (level != newLevel) {
               for (Mob soldier : level.getEntities(
                  EntityTypeTest.forClass(Mob.class), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
               )) {
                  Vec3 safePos = TeleportHelper.findSafeTeleportPos(newLevel, soldier, player.getX(), player.getY(), player.getZ(), player.position());
                  soldier.teleportTo(newLevel, safePos.x, safePos.y, safePos.z, Set.of(), soldier.getYRot(), soldier.getXRot());
               }
            }
         }
      }
   }

   public static void dismissAllShadows(ServerPlayer player) {
      if (player.getServer() != null) {
         String masterTag = "shadow_master_" + player.getUUID();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Mob soldier : level.getEntities(
               EntityTypeTest.forClass(Mob.class), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
            )) {
               level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
               level.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
               soldier.discard();
            }
         }
      }
   }

   public static void refreshPlayer(ServerPlayer player) {
      SystemData data = data(player);
      if (data.skills().containsKey("caregiver_blessing") && !data.skills().containsKey("sh_caregiver_blessing")) {
         data.skills().put("sh_caregiver_blessing", (Integer)data.skills().get("caregiver_blessing"));
      }

      if (data.skills().containsKey("electrification") && !data.skills().containsKey("le_electrification")) {
         data.skills().put("le_electrification", (Integer)data.skills().get("electrification"));
      }

      if (data.skills().containsKey("bloodthirst") && "blood_emperor".equals(data.hunterClass()) && !data.skills().containsKey("be_bloodthirst")) {
         data.skills().put("be_bloodthirst", (Integer)data.skills().get("bloodthirst"));
      }

      if (data.skills().containsKey("seismic_roar") && "beast_monarch".equals(data.hunterClass()) && !data.skills().containsKey("bm_seismic_roar")) {
         data.skills().put("bm_seismic_roar", (Integer)data.skills().get("seismic_roar"));
      }

      if (data.skills().containsKey("dragon_dive") && "colossal_berserker".equals(data.hunterClass()) && !data.skills().containsKey("cb_valterian_instinct")) {
         data.skills().put("cb_valterian_instinct", (Integer)data.skills().get("dragon_dive"));
      }

      if (data.skills().containsKey("rulers_authority") && "star_apostle".equals(data.hunterClass()) && !data.skills().containsKey("fourth_wall")) {
         data.skills().put("fourth_wall", (Integer)data.skills().get("rulers_authority"));
      }

      data.removeUnavailableContent();
      if (data.hasHunterClass()) {
         grantFable(player, "first_awakening");
      }

      if (data.hasHunterClass() && data.level() >= 50) {
         int expectedTotal = 1 + (data.level() - 50) / 5;
         int actualTotal = data.skillPoints() + data.spentSkillPoints();
         if (actualTotal < expectedTotal) {
            data.addSkillPoints(expectedTotal - actualTotal);
         }
      }

      SystemAttributeEffects.apply(player);
      AwakeningSystemMod.sync(player);
   }

   public static void handleRequestSync(ServerPlayer player) {
      if (player != null && !player.isRemoved()) {
         refreshPlayer(player);
         AwakeningSystemMod.sync(player);
         AwakeningSystemMod.syncPlayerBadge(player);
      }
   }

   @SubscribeEvent
   public static void onBlockPlace(EntityPlaceEvent event) {
      if (event.getEntity() == null
         || !event.getEntity().level().dimension().equals(DungeonManager.DUNGEON_REALM)
            && !event.getEntity().level().dimension().equals(TowerManager.RANKER_TOWER)
            && !event.getEntity().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
            && (
               !(event.getEntity() instanceof ServerPlayer spx)
                  || !DungeonManager.isInDungeon(spx.getUUID()) && !TowerManager.isInTower(spx.getUUID()) && !PagodaTrialManager.isInTrial(spx.getUUID())
            )
         || event.getEntity() instanceof ServerPlayer sp && (sp.isCreative() || sp.getAbilities().instabuild)) {
         if (event.getEntity() instanceof Player player
            && TeyvatCasinoBuilder.isInsideCasino(player.level(), event.getPos())
            && !player.isCreative()
            && !player.getAbilities().instabuild
            && !player.hasPermissions(2)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("awakening_system.casino.build_prohibited"), true);
         } else {
            if (event.getEntity() instanceof ServerPlayer spxx && spxx.serverLevel().dimension().equals(SubspaceManager.SUBSPACE_REALM)) {
               BlockState placed = event.getPlacedBlock();
               if (SubspaceManager.isStorageBlockState(placed, spxx.serverLevel(), event.getPos()) && spxx.getServer() != null) {
                  spxx.getServer()
                     .execute(
                        () -> {
                           SubspaceManager.syncWorldToSubspace(spxx.serverLevel(), spxx);
                           SystemData d = data(spxx);
                           spxx.displayClientMessage(
                              Component.translatable("awakening_system.subspace.expanded", new Object[]{d.subspaceChestCount(), d.subspaceUnlockedSlots()}),
                              true
                           );
                        }
                     );
               }
            }

            if (event.getEntity() != null && event.getEntity().level().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
               BlockPos pos = event.getPos();
               if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 7225 && pos.getY() >= 83) {
                  BlockState placed = event.getPlacedBlock();
                  if (placed.is(Blocks.FIRE) || placed.is(Blocks.SOUL_FIRE) || placed.is(Blocks.LAVA)) {
                     event.setCanceled(true);
                  }
               }
            }

            if (event.getEntity() != null && LimboWorldManager.isLimboDimension(event.getEntity().level())) {
               BlockState placed = event.getPlacedBlock();
               boolean isJack = placed.is((Block)SystemBlocks.JACK_PUMPKIN.get());
               if (!isJack) {
                  int light = placed.getLightEmission(event.getLevel(), event.getPos());
                  if ((
                        light > 0
                           || placed.is(Blocks.TORCH)
                           || placed.is(Blocks.WALL_TORCH)
                           || placed.is(Blocks.SOUL_TORCH)
                           || placed.is(Blocks.SOUL_WALL_TORCH)
                           || placed.is(Blocks.LANTERN)
                           || placed.is(Blocks.SOUL_LANTERN)
                           || placed.is(Blocks.CAMPFIRE)
                           || placed.is(Blocks.SOUL_CAMPFIRE)
                           || placed.is(Blocks.GLOWSTONE)
                           || placed.is(Blocks.SEA_LANTERN)
                           || placed.is(Blocks.SHROOMLIGHT)
                           || placed.is(Blocks.OCHRE_FROGLIGHT)
                           || placed.is(Blocks.PEARLESCENT_FROGLIGHT)
                           || placed.is(Blocks.VERDANT_FROGLIGHT)
                           || placed.is(Blocks.CANDLE)
                           || placed.is(Blocks.CANDLE_CAKE)
                           || placed.is(Blocks.LAVA)
                           || placed.is(Blocks.FIRE)
                           || placed.is(Blocks.SOUL_FIRE)
                     )
                     && !(event.getEntity() instanceof Player p && p.isCreative())) {
                     event.setCanceled(true);
                     if (event.getEntity().level() instanceof ServerLevel sl) {
                        sl.sendParticles(
                           ParticleTypes.SMOKE, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.05
                        );
                     }

                     if (event.getEntity() instanceof Player player) {
                        player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);
                        player.displayClientMessage(Component.translatable("awakening_system.limbo.light_extinguished"), true);
                     }

                     return;
                  }
               }
            }
         }
      } else {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onYggdrasilInteract(RightClickBlock event) {
      if (event.getLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
         BlockPos pos = event.getPos();
         if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 7225 && pos.getY() >= 83) {
            ItemStack item = event.getItemStack();
            if (item.is(Items.FLINT_AND_STEEL) || item.is(Items.FIRE_CHARGE) || item.is(Items.LAVA_BUCKET)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onYggdrasilNeighborNotify(NeighborNotifyEvent event) {
      if (event.getLevel() instanceof ServerLevel sl && sl.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
         BlockPos pos = event.getPos();
         if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= 7225
            && pos.getY() >= 83
            && (event.getState().is(Blocks.FIRE) || event.getState().is(Blocks.SOUL_FIRE))) {
            sl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public static void onYggdrasilMobFinalizeSpawn(FinalizeSpawnEvent event) {
      if (event.getLevel() != null && event.getLevel().getLevel() != null) {
         ServerLevel sl = event.getLevel().getLevel();
         if (sl.dimension().equals(DungeonManager.DUNGEON_REALM)
            && !event.getEntity().getTags().contains("dungeon_mob")
            && !event.getEntity().getTags().contains("shadow_soldier")) {
            event.setSpawnCancelled(true);
            return;
         }

         if (GreatTowerWorldManager.isGreatTowerDimension(sl)) {
            MobSpawnType spawnType = event.getSpawnType();
            if (spawnType != MobSpawnType.SPAWNER
               && spawnType != MobSpawnType.SPAWN_EGG
               && spawnType != MobSpawnType.COMMAND
               && spawnType != MobSpawnType.MOB_SUMMONED
               && spawnType != MobSpawnType.TRIGGERED) {
               String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getNamespace();
               if (!namespace.equals("awakening_system") && event.getEntity().getType() != EntityType.WARDEN) {
                  event.setSpawnCancelled(true);
                  return;
               }

               return;
            }

            return;
         }

         if (sl.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            double x = event.getX();
            double y = event.getY();
            double z = event.getZ();
            if (x * x + z * z <= 7225.0 && y >= 83.0 && !(event.getEntity() instanceof ManaPixieEntity)) {
               event.setSpawnCancelled(true);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onYggdrasilEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()
         && event.getLevel().dimension().equals(DungeonManager.DUNGEON_REALM)
         && event.getEntity() instanceof LivingEntity living
         && !(living instanceof Player)) {
         boolean isDungeonMob = living.getTags().contains("dungeon_mob");
         boolean isShadowSoldier = living.getTags().contains("shadow_soldier");
         boolean isTamedPet = living instanceof TamableAnimal tamed && tamed.isTame();
         if (!isDungeonMob && !isShadowSoldier && !isTamedPet) {
            event.setCanceled(true);
            living.discard();
            return;
         }
      }

      if (!event.getLevel().isClientSide() && GreatTowerWorldManager.isGreatTowerDimension(event.getLevel()) && event.getEntity() instanceof Mob mob) {
         int floor = GreatTowerWorldManager.getFloorForPos(event.getLevel(), mob.blockPosition());
         if (floor == 11) {
            if (mob instanceof Monster) {
               event.setCanceled(true);
               return;
            }
         } else if (floor == 15) {
            if (mob instanceof Monster) {
               event.setCanceled(true);
               return;
            }
         } else {
            String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace();
            if (!namespace.equals("awakening_system") && mob.getType() != EntityType.WARDEN) {
               event.setCanceled(true);
               return;
            }
         }
      }

      if (!event.getLevel().isClientSide() && event.getLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
         double x = event.getEntity().getX();
         double y = event.getEntity().getY();
         double z = event.getEntity().getZ();
         if (x * x + z * z <= 7225.0 && y >= 83.0) {
            if (event.getEntity() instanceof LightningBolt bolt) {
               bolt.setVisualOnly(true);
            } else if (event.getEntity() instanceof Monster) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onExplosionDetonate(Detonate event) {
      if (event.getLevel().dimension().equals(DungeonManager.DUNGEON_REALM)
         || event.getLevel().dimension().equals(TowerManager.RANKER_TOWER)
         || event.getLevel().dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
         event.getAffectedBlocks().clear();
      }

      event.getAffectedBlocks()
         .removeIf(pos -> TeyvatCasinoBuilder.isInsideCasino(event.getLevel(), pos) || SeriesStructureBuilder.isInsideBlackMarketDen(event.getLevel(), pos));
   }

   @SubscribeEvent
   public static void onEntityTeleport(EntityTeleportEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ResourceKey<Level> dim = player.level().dimension();
         if (dim.equals(DungeonManager.DUNGEON_REALM)) {
            DungeonSession session = DungeonManager.getSession(player.getUUID());
            if (session != null) {
               if (DungeonManager.isTargetOutOfBounds(session, event.getTargetX(), event.getTargetY(), event.getTargetZ())) {
                  BlockPos safeCenter = DungeonManager.getCurrentRoomCenter(session);
                  if (safeCenter == null) {
                     safeCenter = session.arenaCenter;
                  }

                  double safeY = DungeonManager.findSafeFloorY(player.serverLevel(), safeCenter.getX() + 0.5, safeCenter.getY(), safeCenter.getZ() + 0.5);
                  event.setTargetX(safeCenter.getX() + 0.5);
                  event.setTargetY(safeY);
                  event.setTargetZ(safeCenter.getZ() + 0.5);
                  player.displayClientMessage(Component.translatable("awakening_system.dungeon.teleport_contained"), true);
               }
            } else if (!player.isSpectator()) {
               event.setCanceled(true);
               DungeonManager.rescueStrandedPlayer(player);
            }
         } else if (dim.equals(TowerManager.RANKER_TOWER)) {
            TowerSession session = TowerManager.getSession(player.getUUID());
            if (session != null && session.floorOrigin != null) {
               double dx = event.getTargetX() - (session.floorOrigin.getX() + 0.5);
               double dz = event.getTargetZ() - (session.floorOrigin.getZ() + 0.5);
               double distSq = dx * dx + dz * dz;
               double minY = session.floorOrigin.getY();
               double maxY = session.floorOrigin.getY() + 10 + 1.0;
               if (distSq > 210.25 || event.getTargetY() < minY || event.getTargetY() > maxY) {
                  event.setTargetX(session.floorOrigin.getX() + 0.5);
                  event.setTargetY(session.floorOrigin.getY() + 1.0);
                  event.setTargetZ(session.floorOrigin.getZ() + 0.5);
                  player.displayClientMessage(Component.translatable("awakening_system.tower.teleport_contained"), true);
               }
            } else if (!player.isSpectator()) {
               event.setCanceled(true);
               if (player.getServer() != null) {
                  TowerManager.exitTowerToSpawn(player.getServer(), player.getUUID());
               }
            }
         } else if (dim.equals(PagodaTrialManager.PAGODA_REALM)) {
            PagodaSession session = PagodaTrialManager.getSession(player.getUUID());
            if (session != null && session.stageOrigin != null) {
               double dx = event.getTargetX() - (session.stageOrigin.getX() + 0.5);
               double dz = event.getTargetZ() - (session.stageOrigin.getZ() + 0.5);
               double distSq = dx * dx + dz * dz;
               int maxR = (session.stage == 9 ? 28 : 20) - 2;
               int maxH = session.stage == 9 ? 26 : 14;
               double minY = session.stageOrigin.getY();
               double maxY = session.stageOrigin.getY() + maxH;
               if (distSq > maxR * maxR || event.getTargetY() < minY || event.getTargetY() > maxY) {
                  event.setTargetX(session.stageOrigin.getX() + 0.5);
                  event.setTargetY(session.stageOrigin.getY() + 1.0);
                  event.setTargetZ(session.stageOrigin.getZ() + 0.5);
                  player.displayClientMessage(Component.translatable("awakening_system.pagoda.teleport_contained"), true);
               }
            } else if (!player.isSpectator()) {
               event.setCanceled(true);
               if (player.getServer() != null) {
                  PagodaTrialManager.exitTrial(player.getServer(), player.getUUID());
               }
            }
         }

         BlockPos targetPos = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
         if (TeyvatCasinoBuilder.isInsideVipZone(player.level(), targetPos) && !CasinoVipGateBlock.hasPlayerVipAccess(player)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("awakening_system.casino.vip_teleport_prohibited"), true);
         }
      }
   }

   @SubscribeEvent
   public static void onBlockBreak(BreakEvent event) {
      if (event.getPlayer() != null
         && GreatTowerWorldManager.isGreatTowerDimension(event.getPlayer().level())
         && !event.getPlayer().isCreative()
         && !event.getPlayer().getAbilities().instabuild) {
         BlockState bState = event.getState();
         if (bState.is(Blocks.BARRIER) || bState.is((Block)SystemBlocks.ASCENSION_PORTAL.get())) {
            event.setCanceled(true);
            return;
         }
      }

      if (event.getPlayer() == null
         || !event.getPlayer().level().dimension().equals(DungeonManager.DUNGEON_REALM)
            && !event.getPlayer().level().dimension().equals(TowerManager.RANKER_TOWER)
            && !event.getPlayer().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
            && !DungeonManager.isInDungeon(event.getPlayer().getUUID())
            && !TowerManager.isInTower(event.getPlayer().getUUID())
            && !PagodaTrialManager.isInTrial(event.getPlayer().getUUID())) {
         if (event.getPlayer() != null) {
            Level pLevel = event.getPlayer().level();
            BlockPos bPos = event.getPos();
            if ((TeyvatCasinoBuilder.isInsideCasino(pLevel, bPos) || SeriesStructureBuilder.isInsideBlackMarketDen(pLevel, bPos))
               && !event.getPlayer().isCreative()
               && !event.getPlayer().getAbilities().instabuild
               && !event.getPlayer().hasPermissions(2)) {
               event.setCanceled(true);
               event.getPlayer().displayClientMessage(Component.translatable("awakening_system.underworld.facility_protected"), true);
               return;
            }
         }

         if (event.getPlayer() instanceof ServerPlayer player && !event.isCanceled()) {
            BlockState soulState = event.getState();
            if (soulState.is((Block)SystemBlocks.SOUL_ALLOY_ORE.get()) || soulState.is((Block)SystemBlocks.SOUL_ALLOY_BLOCK.get())) {
               ServerLevel sLevel = player.serverLevel();
               BlockPos bPos = event.getPos();
               sLevel.sendParticles(ParticleTypes.SCULK_SOUL, bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5, 24, 0.35, 0.45, 0.35, 0.08);
               sLevel.sendParticles(ParticleTypes.SOUL, bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5, 16, 0.25, 0.35, 0.25, 0.04);
               sLevel.playSound(
                  null, bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5, (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.4F, 0.85F
               );
               sLevel.playSound(null, bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5, SoundEvents.WARDEN_ROAR, SoundSource.BLOCKS, 0.5F, 1.8F);
               player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.1F, 1.4F);
               if (!player.isCreative() && !player.isSpectator()) {
                  player.hurt(player.damageSources().magic(), 1.0F);
                  player.displayClientMessage(Component.translatable("awakening_system.soul_alloy.soul_shock"), true);
               }
            }

            if (player.serverLevel().dimension().equals(SubspaceManager.SUBSPACE_REALM)) {
               BlockState broken = event.getState();
               if (SubspaceManager.isStorageBlockState(broken, player.serverLevel(), event.getPos()) && player.getServer() != null) {
                  player.getServer()
                     .execute(
                        () -> {
                           SubspaceManager.syncWorldToSubspace(player.serverLevel(), player);
                           SystemData d = data(player);
                           player.displayClientMessage(
                              Component.translatable("awakening_system.subspace.updated", new Object[]{d.subspaceChestCount(), d.subspaceUnlockedSlots()}),
                              true
                           );
                        }
                     );
               }
            }

            SystemData data = data(player);
            if (event.getState().is(BlockTags.DIAMOND_ORES) || event.getState().is(Blocks.DIAMOND_ORE) || event.getState().is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
               grantFable(player, "miner_depths");
            }

            boolean before = data.skills().containsKey("stone_strike");
            int rawMining = isOre(event.getState()) ? 2 : 1;
            int gained = data.learn("mining", rawMining);
            int miningRepetition = data.repetition("mining");
            int experience = data.affinityExperience(isOre(event.getState()) ? 3 : 1);
            if (gained <= 0) {
               experience = 0;
            }

            boolean changed = gained > 0;
            if (isOre(event.getState())) {
               changed |= learnAndAnnounce(player, data, "perception", 2, "ore_sense") > 0;
               Integer oreSense = (Integer)data.skills().get("ore_sense");
               if (oreSense != null) {
                  int oreLevel = oreSense;
                  boolean evolved = oreLevel >= 6;
                  player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, (evolved ? 160 : 80) + oreLevel * 20, 0, false, false, true));
                  if (evolved) {
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80 + oreLevel * 10, 0, false, false, true));
                  }

                  changed |= gainSkillProgress(player, data, "ore_sense");
               }
            }

            if (data.isDokkaebiScenarioActive() && event.getState().is(BlockTags.DIAMOND_ORES)) {
               for (DokkaebiEntity d : player.serverLevel()
                  .getEntitiesOfClass(
                     DokkaebiEntity.class,
                     player.getBoundingBox().inflate(64.0),
                     dokkaebi -> "mine_diamonds".equals(dokkaebi.getQuestType()) && dokkaebi.isQuestActive()
                  )) {
                  d.addProgress(1, player);
               }
            }

            if (event.getState().getBlock() instanceof CropBlock) {
               changed |= learnAndAnnounce(player, data, "nature", 2, "green_thumb") > 0;
               Integer greenThumb = (Integer)data.skills().get("green_thumb");
               if (greenThumb != null) {
                  int greenLevel = greenThumb;
                  boolean evolved = greenLevel >= 6;
                  player.getFoodData().eat(evolved ? 2 : 1, greenLevel * (evolved ? 0.2F : 0.1F));
                  if (evolved) {
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, true));
                  }

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
               int skillLevel = (Integer)data.skills().get("stone_strike");
               boolean evolved = skillLevel >= 6;
               if (miningRepetition <= 600) {
                  experience += evolved ? skillLevel * 2 : skillLevel;
               }

               player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60 + skillLevel * 10, Math.min(2, skillLevel - 1), false, false, true));
               if (data.canAdvanceSkill("stone_strike") && data.gainSkillProgress("stone_strike", 1)) {
                  announceSkillLevel(player, "stone_strike", (Integer)data.skills().get("stone_strike"));
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
            if (isOre(event.getState())) {
               data.recordStatistic("ores_mined", 1);
            }

            long week = player.level().getDayTime() / 168000L;
            if ((Boolean)SystemConfig.WEEKLY_MISSIONS_ENABLED.get()) {
               data.recordWeeklyProgress(week, 1, 1);
               if (data.completeWeekly(week)) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.weekly_complete.title",
                     "awakening_system.notification.weekly_complete.message",
                     -7607811,
                     new Object[0]
                  );
                  changed = true;
               }
            }

            checkAchievements(player, data);
            long day = player.level().getDayTime() / 24000L;
            changed |= updateDailyProgress(player, data, day, 0, 1);
            if (data.completeDaily(player.level().getGameTime())) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.daily_complete.title", "awakening_system.notification.daily_complete.message", -9377537, new Object[0]
               );
               changed = true;
            }

            if (isOre(event.getState())
               && player.getY() < 0.0
               && data.activeScenario().equals("depth_expedition")
               && data.progressScenario("depth_expedition", 1)) {
               announceScenarioComplete(player, data);
               changed = true;
            }

            if (event.getState().is(BlockTags.DIAMOND_ORES) && data.completeSecretMission()) {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.secret_complete.title",
                  "awakening_system.notification.secret_complete.message",
                  -4489729,
                  new Object[0]
               );
               changed = true;
            }

            if (data.recordAwakeningMining()) {
               changed = true;
            }

            completeAwakening(player, data);
            completeClassMission(player, data);
            if (changed) {
               AwakeningSystemMod.sync(player);
            }
         }
      } else if (!event.getPlayer().isCreative() && !event.getPlayer().getAbilities().instabuild) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer sp) {
         if (LimboWorldManager.isLimboDimension(sp.level())) {
            event.setCanceled(true);
            sp.setHealth(sp.getMaxHealth());
            sp.clearFire();
            sp.fallDistance = 0.0F;
            sp.displayClientMessage(Component.translatable("awakening_system.limbo.immortal_pulse"), true);
            return;
         }

         if (DungeonManager.isInDungeon(sp.getUUID()) || sp.level().dimension().equals(DungeonManager.DUNGEON_REALM)) {
            event.setCanceled(true);
            sp.setHealth(sp.getMaxHealth());
            sp.clearFire();
            sp.fallDistance = 0.0F;
            sp.setDeltaMovement(0.0, 0.0, 0.0);
            sp.invulnerableTime = 60;
            DungeonManager.exitDungeonSilently(sp);
            LimboWorldManager.teleportToLimbo(sp);
            sp.sendSystemMessage(Component.translatable("awakening_system.limbo.dungeon_death_rescue"));
            return;
         }
      }

      if (event.getEntity() instanceof ServerPlayer player && SystemItems.ALARM_CLOCK != null && SystemItems.ALARM_CLOCK.isBound()) {
         boolean clockTriggered = false;

         for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is((Item)SystemItems.ALARM_CLOCK.value())) {
               stack.shrink(1);
               clockTriggered = true;
               break;
            }
         }

         if (clockTriggered) {
            event.setCanceled(true);
            player.setHealth(6.0F);

            for (Holder<MobEffect> eff : player.getActiveEffects()
               .stream()
               .filter(e -> ((MobEffect)e.getEffect().value()).getCategory() == MobEffectCategory.HARMFUL)
               .map(MobEffectInstance::getEffect)
               .toList()) {
               player.removeEffect(eff);
            }

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.level().broadcastEntityEvent(player, (byte)35);
            player.displayClientMessage(Component.translatable("awakening_system.item.alarm_clock.saved"), true);
            return;
         }
      }

      DungeonManager.onEntityKilled(event.getEntity().getUUID());
      TowerManager.onEntityKilled(event.getEntity());
      PagodaTrialManager.onEntityKilled(event.getEntity().getUUID());
      if (!event.getEntity().level().isClientSide() && event.getEntity().level() instanceof ServerLevel slDrops) {
         RpgLevelZoneManager.handleMobDrops(event.getEntity(), slDrops);
         handleDimensionalKeyDrops(event.getEntity(), slDrops);
      }

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
                     TrialState trialState = (TrialState)GoddessAltarBlock.ACTIVE_TRIALS.get(altarPos);
                     if (trialState != null && sl.getServer() != null) {
                        killer = sl.getServer().getPlayerList().getPlayer(trialState.playerId);
                     }
                  }

                  if (killer != null) {
                     GoddessAltarBlock.checkTrialProgress(sl, altarPos, killer);
                  }
               } catch (Exception var27) {
                  AwakeningSystemMod.LOGGER.debug("Error checking Goddess altar trial progress: {}", var27.getMessage(), var27);
               }
               break;
            }
         }
      }

      if (event.getEntity().getTags().contains("trilunar_trial_target") && event.getEntity().level() instanceof ServerLevel sl) {
         String trialType = event.getEntity().getTags().contains("trilunar_aria") ? "aria" : "canon";

         for (String tagx : event.getEntity().getTags()) {
            if (tagx.startsWith("altar_")) {
               try {
                  long posLongx = Long.parseLong(tagx.substring("altar_".length()));
                  BlockPos altarPosx = BlockPos.of(posLongx);
                  ServerPlayer killerx = null;
                  if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                     killerx = sp;
                  } else if (event.getEntity().getLastHurtByMob() instanceof ServerPlayer sp) {
                     killerx = sp;
                  }

                  if (killerx == null) {
                     TrialSession trialState = (TrialSession)TrilunarAltarBlock.ACTIVE_TRIALS.get(altarPosx);
                     if (trialState != null && sl.getServer() != null) {
                        killerx = sl.getServer().getPlayerList().getPlayer(trialState.playerId);
                     }
                  }

                  if (killerx != null) {
                     TrilunarAltarBlock.checkTrialProgress(sl, altarPosx, killerx, trialType);
                  }
               } catch (Exception var26) {
                  AwakeningSystemMod.LOGGER.debug("Error checking Trilunar altar trial progress: {}", var26.getMessage(), var26);
               }
               break;
            }
         }
      }

      if (event.getEntity().getTags().contains("shadow_soldier") && event.getEntity() instanceof Mob soldier) {
         String masterUUIDStr = null;

         for (String tagxx : soldier.getTags()) {
            if (tagxx.startsWith("shadow_master_")) {
               masterUUIDStr = tagxx.substring("shadow_master_".length());
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
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.8, soldier.getZ(), 35, 0.4, 0.5, 0.4, 0.08);
                        sl.sendParticles(ParticleTypes.SCULK_SOUL, soldier.getX(), soldier.getY() + 1.0, soldier.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
                        sl.playSound(null, soldier.getX(), soldier.getY(), soldier.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 1.3F);
                     }

                     String soldierName = soldier.getCustomName() != null
                        ? soldier.getCustomName().getString()
                        : Component.translatable("awakening_system.mob.shadow_infantry").getString();
                     AwakeningSystemMod.notify(
                        master,
                        "awakening_system.notification.shadow_reconstruct.title",
                        "awakening_system.notification.shadow_reconstruct.desc",
                        -6723841,
                        new Object[]{soldierName, 15}
                     );
                     AwakeningSystemMod.sync(master);
                     return;
                  }

                  AwakeningSystemMod.notify(
                     master, "awakening_system.notification.shadow_mana_low.title", "awakening_system.notification.shadow_mana_low.desc", -35226, new Object[0]
                  );
                  AwakeningSystemMod.sync(master);
               }
            } catch (Exception var25) {
               AwakeningSystemMod.LOGGER.debug("Error during shadow soldier reconstruction: {}", var25.getMessage(), var25);
            }
         }
      }

      if (event.getSource().getEntity() instanceof Mob killerMob && killerMob.getTags().contains("shadow_soldier")) {
         promoteOrTrackShadowKill(killerMob);
      }

      if (event.getEntity() instanceof ServerPlayer player) {
         SystemData playerData = data(player);
         playerData.resetEmergencySurvival();
         if (player.getPersistentData().getInt("nascent_soul_guard_ticks") > 0) {
            player.getPersistentData().putInt("nascent_soul_guard_ticks", 0);
            event.setCanceled(true);
            player.setHealth(12.0F);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            if (player.level() instanceof ServerLevel sl) {
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
               sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 1.0, 1.2, 1.0, 0.25);
               sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.8, 1.0, 0.8, 0.1);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.1F);
            }

            player.displayClientMessage(Component.translatable("awakening_system.dao.nascent_soul.cheat_death"), true);
            AwakeningSystemMod.sync(player);
            return;
         }

         boolean hasPhoenixFeather = ManhwaArtifactItem.hasArtifact(player, ArtifactType.PHOENIX_FEATHER);
         if ((playerData.isSkillEnabled("phoenix_rebirth") || hasPhoenixFeather) && player.level().getGameTime() >= playerData.phoenixCooldownUntil()) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth() * 0.5F);
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true, true));
            playerData.setPhoenixCooldownUntil(player.level().getGameTime() + 12000L);
            if (player.level() instanceof ServerLevel sl) {
               spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 3.5, 6.0, 6, 18);
               spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.LAVA, 6.0, 32, 0.15);
               sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 70, 0.8, 1.0, 0.8, 0.25);
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.0F);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.4F);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 0.8F);
            }

            AwakeningSystemMod.notify(
               player, "awakening_system.notification.phoenix.title", "awakening_system.notification.phoenix.message", -36797, new Object[0]
            );
            AwakeningSystemMod.sync(player);
            return;
         }

         boolean hasClock = false;

         for (int ix = 0; ix < player.getInventory().getContainerSize(); ix++) {
            if (player.getInventory().getItem(ix).is((Item)SystemItems.DEATH_KING_CLOCK.get())) {
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
            player.getPersistentData().putLong("death_king_rewind_cooldown", gameTime + 6000L);
            if (player.level() instanceof ServerLevel sl) {
               spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, ParticleTypes.ENCHANT, 4.0);
               spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.END_ROD, 5.0, 32, 0.08);
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.0F);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.3F);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.6F);
            }

            AwakeningSystemMod.notify(
               player, "awakening_system.event.kim_gongja_rewind_title", "awakening_system.event.kim_gongja_rewind_msg", -11141291, new Object[0]
            );
            AwakeningSystemMod.sync(player);
            return;
         }

         boolean inInstance = DungeonManager.isInDungeon(player.getUUID())
            || TowerManager.isInTower(player.getUUID())
            || PagodaTrialManager.isInTrial(player.getUUID())
            || player.level().dimension().equals(DungeonManager.DUNGEON_REALM)
            || player.level().dimension().equals(TowerManager.RANKER_TOWER)
            || player.level().dimension().equals(PagodaTrialManager.PAGODA_REALM);
         if (inInstance) {
            NonNullList<ItemStack> saved = NonNullList.withSize(player.getInventory().getContainerSize(), ItemStack.EMPTY);
            ListTag listTag = new ListTag();

            for (int ixx = 0; ixx < player.getInventory().getContainerSize(); ixx++) {
               ItemStack stack = player.getInventory().getItem(ixx);
               saved.set(ixx, stack.copy());
               if (!stack.isEmpty()) {
                  CompoundTag slotTag = new CompoundTag();
                  slotTag.putInt("Slot", ixx);
                  slotTag.put("Item", stack.save(player.registryAccess()));
                  listTag.add(slotTag);
               }
            }

            SAVED_DUNGEON_DEATH_INVENTORIES.put(player.getUUID(), saved);
            player.getPersistentData().put("dungeon_preserved_inventory", listTag);
            CuriosCompatHandler.saveCuriosOnDeath(player);
         }
      }

      ServerPlayer player = null;
      if (event.getSource().getEntity() instanceof ServerPlayer sp) {
         player = sp;
      } else if (event.getSource().getDirectEntity() instanceof ServerPlayer sp) {
         player = sp;
      } else if (event.getEntity().getLastHurtByMob() instanceof ServerPlayer sp) {
         player = sp;
      } else {
         Entity var80 = event.getSource().getEntity();
         if (var80 instanceof Entity) {
            Entity attacker = var80;

            for (String tagxxx : var80.getTags()) {
               if (tagxxx.startsWith("shadow_master_")) {
                  try {
                     UUID masterUuid = UUID.fromString(tagxxx.substring("shadow_master_".length()));
                     if (attacker.level() instanceof ServerLevel sl && sl.getServer() != null) {
                        ServerPlayer master = sl.getServer().getPlayerList().getPlayer(masterUuid);
                        if (master != null) {
                           player = master;
                           break;
                        }
                     }
                  } catch (Exception var28) {
                  }
               }
            }

            if (player == null && attacker instanceof TamableAnimal tamable && tamable.getOwner() instanceof ServerPlayer sp) {
               player = sp;
            }
         }
      }

      if (player != null && event.getEntity() != player) {
         SystemData data = data(player);
         boolean before = data.skills().containsKey("combat_instinct");
         int gained = data.learn("combat", 5);
         if (event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse_expansion")
            || event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("eclipse")) {
            data.learn("eclipse", 5);
         }

         boolean isBoss = BossCompatibilityManager.isBoss(event.getEntity());
         int rpgMobLvl = event.getEntity().getPersistentData().getInt("rpg_mob_level");
         boolean isTowerMob = event.getEntity().getPersistentData().getBoolean("TowerMob")
            || event.getEntity().level().dimension() != null && event.getEntity().level().dimension().equals(TowerManager.RANKER_TOWER);
         boolean isTowerChampion = event.getEntity().getPersistentData().getBoolean("TowerChampion")
            || event.getEntity().getPersistentData().getBoolean("TowerFinalBoss");
         int killExp;
         if (isTowerMob || isTowerChampion) {
            int floor = event.getEntity().getPersistentData().getInt("TowerFloor");
            if (floor <= 0 && rpgMobLvl > 10) {
               floor = (int)Math.round((rpgMobLvl - 10) / 1.25);
            }

            floor = Math.max(1, floor);
            if (!isTowerChampion && !isBoss) {
               killExp = (int)(rpgMobLvl * 5 + Math.min(100.0F, event.getEntity().getMaxHealth() * 0.03F) + floor * 5);
            } else {
               killExp = (int)(rpgMobLvl * 12 + Math.min(250.0F, event.getEntity().getMaxHealth() * 0.05F) + floor * 10);
            }

            killExp = Math.min(800, killExp);
            player.giveExperiencePoints(Math.max(6, Math.min(40, (int)(rpgMobLvl * 1.5))));
         } else if (isBoss) {
            int hpExp = (int)Math.min(500.0F, event.getEntity().getMaxHealth() * 0.05F);
            killExp = Math.max(100, Math.min(1200, hpExp + rpgMobLvl * 8));
            player.giveExperiencePoints(Math.max(25, Math.min(80, (int)(rpgMobLvl * 2.0))));
         } else {
            int hpExp = (int)Math.min(60.0F, event.getEntity().getMaxHealth() * 0.03F);
            killExp = Math.max(5, Math.min(200, hpExp + rpgMobLvl * 2));
            if (rpgMobLvl >= 15) {
               player.giveExperiencePoints(Math.max(2, Math.min(20, rpgMobLvl / 4)));
            }
         }

         awardExperience(player, data, killExp);
         if (!before && data.skills().containsKey("combat_instinct")) {
            announceSkill(player, "combat_instinct");
         }

         int reward = ShopDefinitions.mobReward(event.getEntity().getMaxHealth(), !event.getEntity().getType().getCategory().isFriendly());
         if (isTowerMob || isTowerChampion) {
            reward += (int)(rpgMobLvl * 2.5);
         } else if (rpgMobLvl > 0) {
            reward += (int)(rpgMobLvl * 1.5);
         }

         if (isBoss) {
            reward = (int)Math.round(reward * (Double)SystemConfig.BOSS_COIN_MULTIPLIER.get());
            data.recordStatistic("boss_kills", 1);
            triggerConstellationReaction(player, ReactionType.BOSS_KILL);
            BossCompatibilityManager.handleBossDefeat(event.getEntity(), player, data);
            if (event.getEntity() instanceof EnderDragon) {
               if (!grantFable(player, "first_ender_dragon")) {
                  grantFable(player, "solo_dragon_slayer");
               }
            } else if (event.getEntity() instanceof WitherBoss) {
               grantFable(player, "first_wither");
            } else if (event.getEntity() instanceof BaranBossEntity) {
               grantFable(player, "demon_king_subjugator");
               boolean isHighTier = event.getEntity().getPersistentData().getInt("ScaledMobTier") >= 7
                  || event.getEntity().getTags().contains("dungeon_anomaly_mob")
                  || event.getEntity().getTags().contains("dungeon_boss");
               if (isHighTier) {
                  grantFable(player, "giant_story_demon_king");
                  grantFable(player, "first_monarch_defeat");
               }
            } else if (event.getEntity() instanceof IgrisBossEntity) {
               grantFable(player, "igris_crimson_conqueror");
            } else if (event.getEntity() instanceof BeruBossEntity) {
               grantFable(player, "beru_ant_king_crusher");
            } else if (event.getEntity() instanceof StatueOfGodEntity) {
               grantFable(player, "god_slayer_statue");
            } else if (event.getEntity() instanceof ColossalSculkWardenEntity || event.getEntity() instanceof Warden) {
               grantFable(player, "colossal_sculk_warden_slayer");
               grantFable(player, "deep_dark_explorer");
            } else if (event.getEntity() instanceof LimboEntity || event.getEntity() instanceof LimboOphanimEntity) {
               grantFable(player, "limbo_void_walker");
            } else if (event.getEntity() instanceof AstralDragonEntity || event.getEntity() instanceof AncientCelestialDragonBossEntity) {
               grantFable(player, "astral_dragon_subjugator");
               grantFable(player, "dragon_monarch_purger");
            }

            if (!data.hasFable("first_blood_hunter")) {
               grantFable(player, "first_blood_hunter");
            }
         }

         boolean isEliteScaled = event.getEntity().getPersistentData().contains("ScaledMobTier");
         if (data.level() >= 40 && (isBoss || isEliteScaled)) {
            EntityType<?> entityType = event.getEntity().getType();
            String mobSkillToLearn = null;
            int absorbChance = isBoss ? 12 : 4;
            if (entityType == EntityType.ENDERMAN) {
               mobSkillToLearn = "dimensional_blink";
            } else if (entityType == EntityType.BLAZE) {
               mobSkillToLearn = "blaze_ignition";
            } else if (entityType == EntityType.RAVAGER || entityType == EntityType.WARDEN || entityType == EntityType.IRON_GOLEM) {
               mobSkillToLearn = "seismic_roar";
               absorbChance = isBoss ? 18 : 6;
            } else if (entityType == EntityType.PHANTOM || entityType == EntityType.VEX) {
               mobSkillToLearn = "spectral_shift";
            } else if (entityType == EntityType.SKELETON || entityType == EntityType.STRAY || entityType == EntityType.BOGGED) {
               mobSkillToLearn = "piercing_shot";
            } else if (entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER) {
               mobSkillToLearn = "web_snare";
            } else if (entityType == EntityType.CREEPER) {
               mobSkillToLearn = "kinetic_blast";
            } else if (entityType == EntityType.DROWNED || entityType == EntityType.GUARDIAN || entityType == EntityType.ELDER_GUARDIAN) {
               mobSkillToLearn = "tidal_surge";
            }

            if (mobSkillToLearn != null && !data.skills().containsKey(mobSkillToLearn) && player.getRandom().nextInt(100) < absorbChance) {
               data.skills().put(mobSkillToLearn, 1);
               data.gainSkillProgress(mobSkillToLearn, 1);
               Definition skillDef = SkillDefinitions.get(mobSkillToLearn);
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_assimilated.title",
                  "awakening_system.notification.skill_assimilated.message",
                  -4489729,
                  new Object[]{skillDef != null ? skillDef.nameKey() : mobSkillToLearn}
               );
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.3F);
               if (player.level() instanceof ServerLevel slx) {
                  slx.sendParticles(
                     ParticleTypes.TOTEM_OF_UNDYING, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 35, 0.5, 0.8, 0.5, 0.1
                  );
               }
            }
         }

         boolean hasShadowMonarch = data.hasHunterClass() && "shadow_monarch".equals(data.hunterClass())
            || data.skills().containsKey("unique_absolute_extraction");
         if (hasShadowMonarch
            && IceAndFireCompat.isHostileOrExtractable(event.getEntity())
            && !(event.getEntity() instanceof ServerPlayer)
            && !event.getEntity().getTags().contains("shadow_soldier")
            && !(event.getEntity() instanceof CelestialDragonEntity)
            && !(event.getEntity() instanceof AncientCelestialDragonBossEntity)
            && !event.getEntity().getTags().contains("no_shadow_extraction")
            && !PagodaTrialManager.isTrialEnemy(event.getEntity().getUUID())
            && player.level() instanceof ServerLevel slx) {
            LivingEntity defeated = event.getEntity();
            if (defeated instanceof Mob defeatedMob) {
               ShadowSoulEntity soul = new ShadowSoulEntity(slx, defeated.getX(), defeated.getY(), defeated.getZ(), defeatedMob, player.getUUID());
               slx.addFreshEntity(soul);
               slx.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, defeated.getX(), defeated.getY() + 0.3, defeated.getZ(), 25, 0.3, 0.3, 0.3, 0.05);
               slx.sendParticles(ParticleTypes.SCULK_SOUL, defeated.getX(), defeated.getY() + 0.4, defeated.getZ(), 15, 0.2, 0.3, 0.2, 0.03);
               slx.playSound(
                  null, defeated.getX(), defeated.getY(), defeated.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.8F, 1.2F
               );
               if (isBoss) {
                  String defeatedName = defeatedMob.getCustomName() != null
                     ? defeatedMob.getCustomName().getString()
                     : defeatedMob.getType().getDescription().getString();
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.shadow_soul_dropped.title",
                     "awakening_system.notification.shadow_soul_dropped.desc",
                     -6723841,
                     new Object[]{defeatedName}
                  );
               }
            }
         }

         if (data.hasSponsor() && !event.getEntity().getType().getCategory().isFriendly()) {
            List<Quest> quests = SponsorQuestDefinitions.getQuestsForSponsor(data.sponsorConstellation());
            if (!quests.isEmpty()) {
               if (isBoss) {
                  for (Quest q : quests) {
                     if (q.targetCount() <= 3) {
                        data.addCoins(q.coinReward());
                        data.addAttribute(q.bonusStat());
                        SystemAttributeEffects.apply(player);
                        AwakeningSystemMod.notify(
                           player,
                           "awakening_system.notification.sponsor_quest_complete.title",
                           "awakening_system.notification.sponsor_quest_complete.msg",
                           -11470213,
                           new Object[]{q.title().getString(), q.bonusStat().toUpperCase()}
                        );
                        break;
                     }
                  }
               } else if (player.getRandom().nextInt(100) < 12) {
                  Quest qx = quests.get(player.getRandom().nextInt(quests.size()));
                  int bounty = 20 + player.getRandom().nextInt(20);
                  data.addCoins(bounty);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.sponsor_quest_progress.title",
                     "awakening_system.notification.sponsor_quest_progress.msg",
                     -10496,
                     new Object[]{qx.title().getString(), String.valueOf(bounty)}
                  );
               }
            }
         }

         if (!data.activeScenario().equals("night_hunt") || !player.level().isNight() || event.getEntity().getType().getCategory().isFriendly()) {
            if (data.activeScenario().equals("depth_expedition") && player.getY() < 0.0 && !event.getEntity().getType().getCategory().isFriendly()) {
               if (data.progressScenario("depth_expedition", 1)) {
                  announceScenarioComplete(player, data);
               }
            } else if (!data.activeScenario().equals("abyssal_conquest") || !isBoss) {
               if (data.activeScenario().equals("survival_trial")
                  && !event.getEntity().getType().getCategory().isFriendly()
                  && data.progressScenario("survival_trial", 1)) {
                  announceScenarioComplete(player, data);
               }
            } else if (data.progressScenario("abyssal_conquest", 1)) {
               announceScenarioComplete(player, data);
            }
         } else if (data.progressScenario("night_hunt", 1)) {
            announceScenarioComplete(player, data);
         }

         boolean isHostileMob = event.getEntity() instanceof Enemy
            || !event.getEntity().getType().getCategory().isFriendly()
            || event.getEntity() instanceof Mob mob && mob.isAggressive();
         if (data.isDokkaebiScenarioActive() && isHostileMob && !(event.getEntity() instanceof ServerPlayer)) {
            UUID playerUuid = player.getUUID();
            List<DokkaebiEntity> dokkaebis = player.serverLevel()
               .getEntitiesOfClass(
                  DokkaebiEntity.class,
                  player.getBoundingBox().inflate(64.0),
                  dokkaebi -> playerUuid.equals(dokkaebi.getBoundPlayerUuid()) || dokkaebi.getBoundPlayerUuid() == null
               );
            boolean isKillQuest = dokkaebis.isEmpty();

            for (DokkaebiEntity d : dokkaebis) {
               if ("kill_monsters".equals(d.getQuestType())) {
                  isKillQuest = true;
                  break;
               }
            }

            if (data.dokkaebiScenarioTitle() != null
               && (
                  data.dokkaebiScenarioTitle().contains("kill_monsters")
                     || data.dokkaebiScenarioTitle().contains("Emergency")
                     || data.dokkaebiScenarioTitle().contains("Caza")
                     || data.dokkaebiScenarioTitle().contains("hunt")
               )) {
               isKillQuest = true;
            }

            if (isKillQuest) {
               data.recordDokkaebiKill();
               int kills = data.dokkaebiScenarioKills();
               int target = data.dokkaebiScenarioTargetKills();

               for (DokkaebiEntity dx : dokkaebis) {
                  dx.syncFromPlayerData(player);
               }

               AwakeningSystemMod.sync(player);
               if (kills == target) {
                  player.level()
                     .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.2F);
                  player.displayClientMessage(Component.translatable("awakening_system.dokkaebi.quota_reached", new Object[]{target}), true);
               } else if (kills > target) {
                  int extra = kills - target;
                  int rateCoins = getOverkillCoinRate(extra);
                  int rateExp = getOverkillExpRate(extra);
                  Component rankTag = getOverkillRankTag(extra);
                  player.displayClientMessage(
                     Component.translatable("awakening_system.dokkaebi.overkill_actionbar", new Object[]{extra, rankTag.getString(), rateCoins, rateExp}), true
                  );
                  player.level()
                     .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.7F,
                        1.4F + Math.min(0.6F, extra * 0.05F)
                     );
                  if (extra == 5 || extra == 10 || extra == 15 || extra == 20 || extra == 30) {
                     SystemEvents.DokkaebiOverkillReward currentOverkill = calculateDokkaebiOverkill(extra);
                     player.level()
                        .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.3F, 1.2F);
                     String giftStr = !currentOverkill.crystalGift.isEmpty() ? " + 1x " + currentOverkill.crystalGift.getHoverName().getString() : "";
                     player.sendSystemMessage(
                        Component.translatable(
                           "awakening_system.dokkaebi.milestone_hit",
                           new Object[]{extra, currentOverkill.rankName.getString(), currentOverkill.milestoneDonation, giftStr}
                        )
                     );
                  }
               }
            }
         }

         if (!data.isWelkinEnhanced() && data.welkinQuestStep() > 0) {
            if (data.welkinQuestStep() == 1
               && !event.getEntity().getType().getCategory().isFriendly()
               && (player.level().isNight() || player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD))) {
               data.addWelkinQuestProgress(1);
               if (data.welkinQuestProgress() >= 20) {
                  data.setWelkinQuestStep(2);
                  data.setWelkinQuestProgress(0);
                  player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                  AwakeningSystemMod.notify(
                     player, "awakening_system.welkin.quest.step1_complete_title", "awakening_system.welkin.quest.step2_desc", -3238952, new Object[0]
                  );
                  player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.step2_started_chat"), false);
               } else if (data.welkinQuestProgress() % 5 == 0) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.welkin.quest.progress_title",
                     "awakening_system.welkin.quest.step1_progress_msg",
                     -4560696,
                     new Object[]{String.valueOf(data.welkinQuestProgress()), "20"}
                  );
               }

               AwakeningSystemMod.sync(player);
            } else if (data.welkinQuestStep() == 2 && isBoss) {
               data.setWelkinQuestStep(3);
               data.setWelkinQuestProgress(0);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
               AwakeningSystemMod.notify(
                  player, "awakening_system.welkin.quest.step2_complete_title", "awakening_system.welkin.quest.step3_desc", -3238952, new Object[0]
               );
               player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.step3_started_chat"), false);
               AwakeningSystemMod.sync(player);
            }
         }

         CompoundTag pTag = player.getPersistentData();
         if (pTag.getBoolean("bounty_active")) {
            int killed = pTag.getInt("bounty_killed");
            int needed = pTag.getInt("bounty_needed");
            if (killed < needed) {
               String target = pTag.getString("bounty_target");
               boolean match = false;
               LivingEntity killedEntity = event.getEntity();
               String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType()).toString();
               if ("any_isekai".equals(target)) {
                  match = killedEntity.level().dimension() == IsekaiWorldManager.ISEKAI_WORLD || !killedEntity.getType().getCategory().isFriendly();
               } else if ("isekai_slime".equals(target)) {
                  match = typeId.contains("slime");
               } else if ("giant_slime".equals(target)) {
                  match = typeId.contains("slime") && killedEntity instanceof Slime slm && slm.getSize() >= 4;
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
                  match = typeId.contains("goblin_scout") || typeId.contains("goblin") && !typeId.contains("shaman") && !typeId.contains("warrior");
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
                  pTag.putInt("bounty_killed", ++killed);
                  String bName = pTag.getString("bounty_name");
                  if (killed >= needed) {
                     player.displayClientMessage(
                        Component.translatable("awakening_system.bounty.ready_to_claim", new Object[]{bName})
                           .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}),
                        true
                     );
                     player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                  } else {
                     player.displayClientMessage(
                        Component.translatable("awakening_system.bounty.progress_toast", new Object[]{bName, killed, needed}).withStyle(ChatFormatting.YELLOW),
                        true
                     );
                     player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
                  }
               }
            }
         }

         double coinMultiplier = (Double)SystemConfig.COIN_MULTIPLIER.get();
         if ((Boolean)SystemConfig.EVENT_NIGHT_BONUS.get() && player.level().isNight()) {
            coinMultiplier += 0.5;
         }

         if ("queen_of_the_darkest_night".equals(data.sponsorConstellation())) {
            coinMultiplier += 0.5;
         }

         if ("star_apostle".equals(data.hunterClass())) {
            coinMultiplier++;
         }

         coinMultiplier += data.prestigeLevel() * SystemConfig.PRESTIGE_COIN_BONUS_PERCENT.get();
         reward = Math.max(1, (int)Math.round(reward * coinMultiplier));
         data.addCoins(reward);
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.shop.coin_title", "awakening_system.notification.shop.coin_message", -10138, new Object[]{reward}
         );
         data.recordStatistic("creatures_defeated", 1);
         long week = player.level().getDayTime() / 168000L;
         if ((Boolean)SystemConfig.WEEKLY_MISSIONS_ENABLED.get()) {
            data.recordWeeklyProgress(week, 0, 1);
            if (data.completeWeekly(week)) {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.weekly_complete.title",
                  "awakening_system.notification.weekly_complete.message",
                  -7607811,
                  new Object[0]
               );
            }
         }

         checkAchievements(player, data);
         applyCompatibilityCombatSkills(player, data);
         long day = player.level().getDayTime() / 24000L;
         updateDailyProgress(player, data, day, 1, 1);
         if (data.completeDaily(player.level().getGameTime())) {
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.daily_complete.title", "awakening_system.notification.daily_complete.message", -9377537, new Object[0]
            );
         }

         data.recordAwakeningCombat();
         completeAwakening(player, data);
         completeClassMission(player, data);
         ItemStack heldWp = player.getMainHandItem();
         if (WeaponLevelManager.isLevelableWeapon(heldWp)) {
            WeaponLevelManager.recordKill(heldWp);
            int mobExp = isBoss ? 1500 : 45;
            WeaponLevelManager.addExp(heldWp, mobExp, player);
         }

         AwakeningSystemMod.sync(player);
      }
   }

   private static void handleDimensionalKeyDrops(LivingEntity entity, ServerLevel level) {
      RandomSource rand = level.getRandom();
      if (entity.getTags().contains("dungeon_mob") || level.dimension().equals(DungeonManager.DUNGEON_REALM)) {
         DungeonSession session = null;

         for (DungeonSession s : DungeonManager.SESSIONS.values()) {
            if (s.dungeonDimension.equals(level.dimension()) && (s.arenaCenter == null || s.arenaCenter.distSqr(entity.blockPosition()) <= 4000000.0)) {
               session = s;
               break;
            }
         }

         boolean isSubjefe = entity.getTags().contains("dungeon_miniboss")
            || entity.getTags().contains("dungeon_chamber_boss")
            || entity.getTags().contains("dungeon_guardian")
            || entity.getCustomName() != null
               && (
                  entity.getCustomName().getString().contains("[MINIJEFE]")
                     || entity.getCustomName().getString().contains("[GUARDIÁN]")
                     || entity.getCustomName().getString().contains("[JEFE DE CÁMARA]")
               );
         if (isSubjefe) {
            entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.DUNGEON_KEY.get(), 1));
            if (session != null) {
               session.minibossesDefeated++;
               if (session.minibossesDefeated >= 2 && !session.bossBarrierOpen) {
                  session.openBossBarrierDirectly(level);
               } else if (level.getServer() != null) {
                  for (ServerPlayer sp : session.getAllPlayers(level.getServer())) {
                     if (sp != null) {
                        sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        sp.sendSystemMessage(Component.translatable("awakening_system.dungeon.subboss_defeated_key"));
                     }
                  }
               }
            }
         } else if (session != null && session.dungeonKeysDropped < 2) {
            float dropChance = session.dungeonKeysDropped == 0 ? 0.05F : 0.01F;
            if (rand.nextFloat() < dropChance) {
               session.dungeonKeysDropped++;
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.OVERWORLD_DUNGEON_KEY.get(), 1));
               if (level.getServer() != null) {
                  for (ServerPlayer spx : session.getAllPlayers(level.getServer())) {
                     if (spx != null) {
                        spx.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.2F, 1.4F);
                        spx.displayClientMessage(Component.translatable("awakening_system.dungeon.key_dropped"), true);
                     }
                  }
               }
            }
         }
      }

      if (entity.getPersistentData().getBoolean("is_overworld_miniboss")) {
         entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.OVERWORLD_DUNGEON_KEY.get(), 1));
      } else {
         String dim = level.dimension().location().getPath().toLowerCase(Locale.ROOT);
         if (dim.contains("nether")) {
            if (entity instanceof WitherBoss) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.NETHER_FLAME_KEY.get(), 1));
            } else if ((entity instanceof WitherSkeleton || entity instanceof Blaze || entity instanceof PiglinBrute || entity instanceof Ghast)
               && rand.nextFloat() < 0.08F) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.NETHER_FLAME_KEY.get(), 1));
            }
         } else if (dim.contains("end")) {
            if (entity instanceof EnderDragon) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.END_VOID_KEY.get(), 1));
            } else if (entity instanceof Shulker) {
               if (rand.nextFloat() < 0.25F) {
                  entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.END_VOID_KEY.get(), 1));
               }
            } else if (entity instanceof EnderMan && rand.nextFloat() < 0.03F) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.END_VOID_KEY.get(), 1));
            }
         } else if (dim.contains("isekai")) {
            if (entity instanceof YggdrasilTreantBossEntity || entity instanceof ElementalDemonKingBossEntity || entity instanceof ElementalLordBossEntity) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.ISEKAI_MANA_KEY.get(), 1));
            } else if ((entity instanceof GoblinShamanEntity || entity instanceof BeastmanWarriorEntity) && rand.nextFloat() < 0.12F) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.ISEKAI_MANA_KEY.get(), 1));
            }
         } else if (dim.contains("murim")) {
            if (entity instanceof JadeGuardianBossEntity
               || entity instanceof BloodPatriarchBossEntity
               || entity instanceof CelestialDragonEntity
               || entity instanceof AncientCelestialDragonBossEntity) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MURIM_DAO_KEY.get(), 1));
            } else if ((entity instanceof DemonicCultivatorEntity || entity instanceof JiangshiEntity || entity instanceof JadeArmoredBeastEntity)
               && rand.nextFloat() < 0.12F) {
               entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MURIM_DAO_KEY.get(), 1));
            }
         } else if (dim.contains("teyvat")
            && (
               entity instanceof HypostasisBaseBossEntity
                  || entity instanceof PyroRegisvineEntity
                  || entity instanceof CryoRegisvineEntity
                  || entity instanceof ElectroRegisvineEntity
                  || entity instanceof GreatSnowboarKingBossEntity
                  || entity instanceof RhodeiaOceanidBossEntity
                  || entity instanceof PerpetualMechanicalArrayBossEntity
                  || entity instanceof RuinSerpentBossEntity
            )) {
            entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.TEYVAT_SHRINE_KEY.get(), 1));
         }
      }
   }

   @SubscribeEvent
   public static void onItemEntityPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event) {
      if (event.getPlayer() instanceof ServerPlayer player) {
         CompoundTag pTag = player.getPersistentData();
         if (pTag.getBoolean("bounty_active")) {
            String target = pTag.getString("bounty_target");
            if (target.startsWith("collect_")) {
               int needed = pTag.getInt("bounty_needed");
               int currentCount = BountyBoardBlock.countInventoryItems(player, target);
               int prevKilled = pTag.getInt("bounty_killed");
               int newKilled = Math.min(needed, currentCount);
               pTag.putInt("bounty_killed", newKilled);
               if (newKilled > prevKilled) {
                  String bName = pTag.getString("bounty_name");
                  if (newKilled >= needed) {
                     player.displayClientMessage(
                        Component.translatable("awakening_system.bounty.ready_to_claim", new Object[]{bName})
                           .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}),
                        true
                     );
                     player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                  } else {
                     player.displayClientMessage(
                        Component.translatable("awakening_system.bounty.progress_toast", new Object[]{bName, newKilled, needed})
                           .withStyle(ChatFormatting.YELLOW),
                        true
                     );
                     player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
      if (event.getNewAboutToBeSetTarget() instanceof Player p) {
         LivingEntity attacker = event.getEntity();
         if (attacker.getTags().contains("shadow_soldier") && attacker.getTags().contains("shadow_master_" + p.getUUID())) {
            event.setNewAboutToBeSetTarget(null);
            if (attacker instanceof Mob m) {
               m.setTarget(null);
            }
         } else if (attacker instanceof CompanionEntity comp && p.getUUID().equals(comp.getOwnerUUID())) {
            event.setNewAboutToBeSetTarget(null);
            comp.setTarget(null);
         }
      }
   }

   @SubscribeEvent
   public static void onCombatDamage(Pre event) {
      if (event.getEntity() instanceof ServerPlayer player
         && event.getSource().is(DamageTypes.FALL)
         && SystemItems.GOLD_HORSESHOE != null
         && SystemItems.GOLD_HORSESHOE.isBound()) {
         boolean hasGoldShoe = player.getItemBySlot(EquipmentSlot.FEET).is((Item)SystemItems.GOLD_HORSESHOE.value());
         if (!hasGoldShoe) {
            for (ItemStack invStack : player.getInventory().items) {
               if (invStack.is((Item)SystemItems.GOLD_HORSESHOE.value())) {
                  hasGoldShoe = true;
                  break;
               }
            }
         }

         if (hasGoldShoe) {
            event.setNewDamage(0.0F);
            return;
         }
      }

      if (event.getEntity().getTags().contains("shadow_soldier")) {
         if (event.getSource().is(DamageTypes.IN_WALL)) {
            event.setNewDamage(0.0F);
            if (event.getEntity().level() instanceof ServerLevel sl) {
               for (String tag : event.getEntity().getTags()) {
                  if (tag.startsWith("shadow_master_")) {
                     try {
                        UUID masterId = UUID.fromString(tag.substring("shadow_master_".length()));
                        ServerPlayer master = sl.getServer().getPlayerList().getPlayer(masterId);
                        if (master != null) {
                           Vec3 safe = TeleportHelper.findSafeTeleportPos(sl, event.getEntity(), master.getX(), master.getY(), master.getZ(), master.position());
                           event.getEntity().teleportTo(safe.x, safe.y, safe.z);
                           break;
                        }
                     } catch (Exception var25) {
                        AwakeningSystemMod.LOGGER.debug("Error teleporting void-falling shadow soldier to master: {}", var25.getMessage(), var25);
                     }
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

      if (event.getEntity() instanceof CompanionEntity comp
         && event.getSource().getEntity() instanceof ServerPlayer sp
         && sp.getUUID().equals(comp.getOwnerUUID())) {
         event.setNewDamage(0.0F);
      } else if (event.getSource().getEntity() instanceof CompanionEntity comp
         && event.getEntity() instanceof ServerPlayer sp
         && sp.getUUID().equals(comp.getOwnerUUID())) {
         event.setNewDamage(0.0F);
      } else if (event.getSource().getEntity() == null
         || (!TowerManager.isTowerEnemy(event.getEntity().getUUID()) || !TowerManager.isTowerEnemy(event.getSource().getEntity().getUUID()))
            && (!PagodaTrialManager.isTrialEnemy(event.getEntity().getUUID()) || !PagodaTrialManager.isTrialEnemy(event.getSource().getEntity().getUUID()))) {
         if (event.getEntity().getMaxHealth() > 300.0F) {
            String typeName = event.getSource().typeHolder().getRegisteredName();
            String msgId = event.getSource().getMsgId();
            if ((typeName != null && typeName.contains("current_hp") || msgId != null && msgId.contains("current_hp")) && event.getNewDamage() > 45.0F) {
               event.setNewDamage(45.0F);
            }
         }

         if (event.getEntity() instanceof Mob bossMob && WorldBossScalingManager.isWorldBoss(bossMob)) {
            if (event.getSource().getEntity() instanceof ServerPlayer attackerPlayer) {
               WorldBossScalingManager.handleWorldBossIncomingDamage(event, bossMob, attackerPlayer);
            }
         } else if (event.getEntity() instanceof Mob targetMob) {
            if (event.getSource().getEntity() instanceof ServerPlayer attackerPlayer) {
               RpgLevelZoneManager.scaleMobToPlayer(targetMob, attackerPlayer);
               int mobLvl = targetMob.getPersistentData().getInt("rpg_mob_level");
               int pLvl = Math.max(1, data(attackerPlayer).level());
               if (mobLvl > pLvl && pLvl > 0) {
                  float multiplier = Math.min(3.5F, (float)mobLvl / pLvl);
                  event.setNewDamage(event.getNewDamage() * multiplier);
               }
            } else if (event.getSource().getEntity() instanceof Mob shadowAttacker && shadowAttacker.getTags().contains("shadow_soldier")) {
               int shadowLvl = shadowAttacker.getPersistentData().getInt("rpg_mob_level");
               int targetLvl = targetMob.getPersistentData().getInt("rpg_mob_level");
               if (shadowLvl > targetLvl && targetLvl > 0) {
                  float multiplier = Math.min(3.0F, 1.0F + (float)(shadowLvl - targetLvl) * 0.01F);
                  event.setNewDamage(event.getNewDamage() * multiplier);
               }
            }
         } else if (event.getEntity() instanceof ServerPlayer victimPlayer && event.getSource().getEntity() instanceof Mob attackerMob) {
            RpgLevelZoneManager.scaleMobToPlayer(attackerMob, victimPlayer);
            int mobLvl = attackerMob.getPersistentData().getInt("rpg_mob_level");
            int pLvl = Math.max(1, data(victimPlayer).level());
            if (mobLvl > pLvl && mobLvl > 0) {
               float ratio = Math.max(0.2F, (float)pLvl / mobLvl);
               event.setNewDamage(event.getNewDamage() * ratio);
            }
         } else if (event.getEntity() instanceof Mob shadowVictim && shadowVictim.getTags().contains("shadow_soldier") && event.getSource().getEntity() instanceof Mob enemyAttacker) {
            int shadowLvl = shadowVictim.getPersistentData().getInt("rpg_mob_level");
            int enemyLvl = enemyAttacker.getPersistentData().getInt("rpg_mob_level");
            if (shadowLvl > enemyLvl && shadowLvl > 0) {
               float ratio = Math.max(0.35F, (float)enemyLvl / shadowLvl);
               event.setNewDamage(event.getNewDamage() * ratio);
            }
         }

         if (event.getEntity() instanceof ServerPlayer victim) {
            float incoming = event.getNewDamage();
            SystemData vData = data(victim);
            if (vData.prestigeLevel() > 0) {
               double presReduct = Math.min(0.5, vData.prestigeLevel() * 0.01);
               incoming = (float)(incoming * (1.0 - presReduct));
               event.setNewDamage(incoming);
            }

            if (vData.prestigeLevel() >= 25 && incoming >= victim.getHealth()) {
               long lastCheat = victim.getPersistentData().getLong("prestige_cheat_death_cd");
               long now = victim.level().getGameTime();
               if (now >= lastCheat) {
                  victim.getPersistentData().putLong("prestige_cheat_death_cd", now + 12000L);
                  event.setNewDamage(0.0F);
                  victim.setHealth(Math.max(victim.getHealth(), victim.getMaxHealth() * 0.5F));
                  victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 4, false, false, true));
                  victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, true));
                  if (victim.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0, victim.getZ(), 60, 0.6, 0.8, 0.6, 0.2);
                     sl.playSound(null, victim.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
                  }

                  victim.displayClientMessage(Component.translatable("awakening_system.cheat_death.diamond_body"), false);
                  return;
               }
            }

            if (vData.hasPassiveRune(RuneType.IRON_BODY)) {
               incoming *= 0.85F;
               event.setNewDamage(incoming);
            }

            if (vData.hasPassiveRune(RuneType.FOURTH_WALL) && incoming >= victim.getHealth()) {
               long lastWall = victim.getPersistentData().getLong("fourth_wall_rune_cd");
               long now = victim.level().getGameTime();
               if (now >= lastWall) {
                  victim.getPersistentData().putLong("fourth_wall_rune_cd", now + 6000L);
                  event.setNewDamage(0.0F);
                  victim.setHealth(Math.max(victim.getHealth(), victim.getMaxHealth() * 0.4F));
                  victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 2, false, false, true));
                  victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 3, false, false, true));
                  if (victim.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY() + 1.0, victim.getZ(), 50, 0.5, 0.8, 0.5, 0.15);
                     sl.playSound(null, victim.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.4F, 1.2F);
                  }

                  victim.displayClientMessage(Component.translatable("awakening_system.cheat_death.fourth_wall"), true);
                  return;
               }
            }

            if ((incoming >= victim.getHealth() || victim.getHealth() - incoming <= 6.0F && incoming >= 6.0F)
               && RobeOfTheBeautyItem.tryCosmicPreservation(victim, incoming)) {
               event.setNewDamage(incoming * 0.5F);
               incoming = event.getNewDamage();
            }

            if (ManhwaArmorItem.hasFullSet(victim, SetType.MORA)
               && (incoming >= victim.getHealth() || victim.getHealth() - incoming <= 4.0F && incoming >= 6.0F)) {
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
                     if (!st.isEmpty()
                        && (st.is((Item)SystemItems.MORA.get()) || st.is((Item)SystemItems.MORA_POUCH.get()) || st.is((Item)SystemItems.MORA_BUNDLE.get()))) {
                        st.shrink(1);
                        paid = true;
                        break;
                     }
                  }
               }

               if (paid) {
                  event.setNewDamage(0.0F);
                  victim.setHealth(Math.max(victim.getHealth(), 10.0F));
                  victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, false, true));
                  victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, false, true));
                  victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, false, true));
                  if (victim.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 0.5, 0.5, 0.5, 0.2);
                     sl.playSound(null, victim.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                  }

                  victim.sendSystemMessage(Component.translatable("awakening_system.cheat_death.mora_barrier"));
                  AwakeningSystemMod.sync(victim);
                  return;
               }
            }

            if (victim.getHealth() - incoming <= victim.getMaxHealth() * 0.5F) {
               for (int ix = 0; ix < victim.getInventory().getContainerSize(); ix++) {
                  ItemStack st = victim.getInventory().getItem(ix);
                  if (!st.isEmpty() && SystemItems.DOCTORS_ROBE.isBound() && st.is((Item)SystemItems.DOCTORS_ROBE.get())) {
                     DoctorsRobeItem.trigger(victim);
                     break;
                  }
               }
            }
         }

         if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() != player) {
            if (IS_PROCESSING_DAMAGE.get()) return;
            IS_PROCESSING_DAMAGE.set(true);
            try {
            SystemData data = data(player);
            boolean changed = false;
            if (data.prestigeLevel() > 0) {
               double prestigeDmgMult = 1.0 + data.prestigeLevel() * 0.015;
               event.setNewDamage((float)(event.getNewDamage() * prestigeDmgMult));
            }

            ItemStack weaponHeld = player.getMainHandItem();
            if (WeaponLevelManager.isLevelableWeapon(weaponHeld)) {
               double wMult = WeaponLevelManager.getDamageMultiplier(weaponHeld);
               if (wMult > 1.0) {
                  event.setNewDamage((float)(event.getNewDamage() * wMult));
               }

               float bonusCrit = WeaponLevelManager.getBonusCritChance(weaponHeld);
               if (bonusCrit > 0.0F && player.getRandom().nextFloat() < bonusCrit) {
                  event.setNewDamage(event.getNewDamage() * 1.5F);
                  if (player.level() instanceof ServerLevel slCrit) {
                     slCrit.sendParticles(
                        ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 20, 0.4, 0.6, 0.4, 0.15
                     );
                  }
               }

               if (WeaponLevelManager.hasMilestone(weaponHeld, 25) && player.getRandom().nextFloat() < 0.25F && player.level() instanceof ServerLevel slWave) {
                  slWave.sendParticles(
                     ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 3, 0.3, 0.3, 0.3, 0.0
                  );

                  for (LivingEntity target : slWave.getEntitiesOfClass(
                     LivingEntity.class, event.getEntity().getBoundingBox().inflate(3.5), e -> e != player && e != event.getEntity() && !(e instanceof Player)
                  )) {
                     target.hurt(player.damageSources().playerAttack(player), event.getNewDamage() * 0.4F);
                  }
               }

               if (WeaponLevelManager.hasMilestone(weaponHeld, 100)) {
                  event.setNewDamage(event.getNewDamage() * 1.5F);
               }

               int weaponExpGain = Math.max(1, Math.min(100, (int)(event.getNewDamage() * 0.2F)));
               WeaponLevelManager.addExp(weaponHeld, weaponExpGain, player);
            }

            double robeMult = RobeOfTheBeautyItem.getDamageMultiplier(player);
            if (robeMult > 1.0) {
               event.setNewDamage((float)(event.getNewDamage() * robeMult));
               if (player.level() instanceof ServerLevel sl && player.getRandom().nextFloat() < 0.4F) {
                  sl.sendParticles(
                     ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY() + 1.2, event.getEntity().getZ(), 6, 0.2, 0.3, 0.2, 0.05
                  );
                  sl.sendParticles(
                     ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 8, 0.3, 0.4, 0.3, 0.1
                  );
               }
            }

            float artifactAtk = data.getArtifactBonusAttack();
            if (artifactAtk > 0.0F) {
               event.setNewDamage(event.getNewDamage() + artifactAtk);
            }

            float critRate = data.getArtifactBonusCritRate();
            if (critRate > 0.0F && player.getRandom().nextFloat() * 100.0F < critRate) {
               float critMultiplier = 1.5F + data.getArtifactBonusCritDmg() / 100.0F;
               event.setNewDamage(event.getNewDamage() * critMultiplier);
               if (player.level() instanceof ServerLevel slCrit) {
                  slCrit.sendParticles(
                     ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 25, 0.5, 0.7, 0.5, 0.2
                  );
                  slCrit.sendParticles(
                     ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 15, 0.4, 0.5, 0.4, 0.1
                  );
               }

               player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.1F);
            }

            if ("caffeine_addict".equals(data.equippedTitle()) && player.getRandom().nextFloat() < 0.35F) {
               event.setNewDamage(event.getNewDamage() * 1.4F);
               if (player.level() instanceof ServerLevel slCrit) {
                  slCrit.sendParticles(
                     ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 20, 0.4, 0.5, 0.4, 0.15
                  );
                  slCrit.sendParticles(
                     ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 10, 0.3, 0.4, 0.3, 0.1
                  );
               }

               player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.2F);
            }

            if ("futanari_hentai_master".equals(data.equippedTitle()) && player.getRandom().nextFloat() < 0.3F) {
               event.setNewDamage(event.getNewDamage() * 1.35F);
               float lifesteal = Math.max(1.0F, event.getNewDamage() * 0.2F);
               player.heal(lifesteal);
               if (player.level() instanceof ServerLevel slLust) {
                  slLust.sendParticles(
                     ParticleTypes.HEART, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 8, 0.4, 0.5, 0.4, 0.1
                  );
                  slLust.sendParticles(
                     ParticleTypes.WITCH, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 10, 0.3, 0.3, 0.3, 0.05
                  );
               }

               player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.4F);
            }

            if ("pokemon_master".equals(data.equippedTitle()) && player.getRandom().nextFloat() < 0.35F) {
               event.setNewDamage(event.getNewDamage() * 1.45F);
               if (player.level() instanceof ServerLevel slPoke) {
                  slPoke.sendParticles(
                     ParticleTypes.ELECTRIC_SPARK, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 20, 0.4, 0.6, 0.4, 0.12
                  );
                  slPoke.sendParticles(
                     ParticleTypes.FIREWORK, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 8, 0.3, 0.5, 0.3, 0.08
                  );
               }

               player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.45F, 1.8F);
               player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.3F);
            }

            if ("creator".equals(data.equippedTitle()) && player.getRandom().nextFloat() < 0.4F) {
               event.setNewDamage(event.getNewDamage() * 1.5F);
               if (player.level() instanceof ServerLevel slCrit) {
                  slCrit.sendParticles(
                     ParticleTypes.TOTEM_OF_UNDYING, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 12, 0.3, 0.5, 0.3, 0.1
                  );
                  slCrit.sendParticles(
                     ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY() + 1.2, event.getEntity().getZ(), 6, 0.2, 0.3, 0.2, 0.04
                  );
               }
            }

            if (data.hasEquippedLightCone()) {
               float coneAtk = data.getLightConeBonusAttack();
               if (coneAtk > 0.0F) {
                  event.setNewDamage(event.getNewDamage() * (1.0F + coneAtk));
               }

               float coneCritRate = data.getLightConeBonusCritRate() * 100.0F;
               float coneCritDmg = data.getLightConeBonusCritDmg();
               if (coneCritRate > 0.0F && player.getRandom().nextFloat() * 100.0F < coneCritRate) {
                  event.setNewDamage(event.getNewDamage() * (1.5F + coneCritDmg));
                  if (player.level() instanceof ServerLevel slCrit) {
                     slCrit.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        event.getEntity().getX(),
                        event.getEntity().getY() + 1.0,
                        event.getEntity().getZ(),
                        20,
                        0.4,
                        0.6,
                        0.4,
                        0.15
                     );
                     slCrit.sendParticles(
                        ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY() + 1.2, event.getEntity().getZ(), 10, 0.3, 0.4, 0.3, 0.08
                     );
                  }
               }
            }

            if (data.hasPassiveRune(RuneType.BLOODTHIRST)) {
               float healAmount = Math.max(1.0F, event.getNewDamage() * 0.15F);
               player.heal(healAmount);
               if (player.level() instanceof ServerLevel slHeal && player.getRandom().nextFloat() < 0.35F) {
                  slHeal.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.3, 0.3, 0.3, 0.05);
               }
            }

            if (data.hasPassiveRune(RuneType.DRAGON_EYES) && event.getEntity() instanceof Monster) {
               event.setNewDamage(event.getNewDamage() * 1.1F);
            }

            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            TeyvatVisionItem equippedVision = null;
            if (mainStack.getItem() instanceof TeyvatVisionItem v) {
               equippedVision = v;
            } else if (offStack.getItem() instanceof TeyvatVisionItem v) {
               equippedVision = v;
            }

            ElementType elem = null;
            if (equippedVision != null) {
               elem = ElementType.fromString(equippedVision.getElement().name);
            } else if (data.hasActiveVision()) {
               elem = ElementType.fromString(data.activeVision());
            }

            if (elem != null) {
               ReactionResult reactionRes = ElementalReactionManager.applyElement(event.getEntity(), elem, player, event.getNewDamage());
               if (reactionRes.reacted()) {
                  event.setNewDamage(event.getNewDamage() * reactionRes.damageMultiplier() + reactionRes.bonusDamage());
               }

               switch (elem) {
                  case PYRO:
                     event.getEntity().setRemainingFireTicks(120);
                     break;
                  case CRYO:
                     event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                     event.getEntity().setTicksFrozen(120);
                     break;
                  case ELECTRO:
                     if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(
                           ParticleTypes.ELECTRIC_SPARK,
                           event.getEntity().getX(),
                           event.getEntity().getY() + 1.0,
                           event.getEntity().getZ(),
                           12,
                           0.3,
                           0.5,
                           0.3,
                           0.1
                        );
                     }
                     break;
                  case HYDRO:
                     player.heal(Math.max(1.0F, event.getNewDamage() * 0.05F));
                     break;
                  case GEO:
                     player.setAbsorptionAmount(Math.min(20.0F, player.getAbsorptionAmount() + 2.0F));
                     break;
                  case ANEMO:
                     Vec3 dir = event.getEntity().position().subtract(player.position()).normalize().scale(0.8);
                     event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().add(dir.x, 0.4, dir.z));
                     break;
                  case DENDRO:
                     event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
               }
            }

            if (event.getEntity().getPersistentData().getLong("superconduct_vuln_until") > event.getEntity().level().getGameTime()) {
               event.setNewDamage(event.getNewDamage() * 1.4F);
            }

            if (ElementalReactionManager.checkShatter(event.getEntity(), player, event.getNewDamage())) {
               event.setNewDamage(event.getNewDamage() + 28.0F);
            }

            long shieldUntil = event.getEntity().getPersistentData().getLong("genshin_shield_until");
            if (shieldUntil > event.getEntity().level().getGameTime()) {
               float shieldAmt = event.getEntity().getPersistentData().getFloat("genshin_shield_amount");
               if (shieldAmt > 0.0F) {
                  float absorbed = Math.min(shieldAmt, event.getNewDamage());
                  event.getEntity().getPersistentData().putFloat("genshin_shield_amount", shieldAmt - absorbed);
                  event.setNewDamage(event.getNewDamage() - absorbed);
                  if (event.getEntity().level() instanceof ServerLevel slShield) {
                     slShield.sendParticles(
                        ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 10, 0.3, 0.4, 0.3, 0.1
                     );
                  }
               }
            }

            if (data.hasArtifactSetBonus(ArtifactSet.GLADIATORS_FINALE, 4) && player.getMainHandItem().getItem() instanceof SwordItem) {
               event.setNewDamage(event.getNewDamage() * 1.35F);
            }

            if (data.hasArtifactSetBonus(ArtifactSet.CRIMSON_WITCH, 4)) {
               event.getEntity().setRemainingFireTicks(120);
               if (player.level() instanceof ServerLevel slFlame) {
                  slFlame.sendParticles(
                     ParticleTypes.FLAME, event.getEntity().getX(), event.getEntity().getY() + 0.5, event.getEntity().getZ(), 30, 0.5, 0.5, 0.5, 0.15
                  );
               }
            }

            if (data.hasArtifactSetBonus(ArtifactSet.VIRIDESCENT_VENERER, 4)) {
               LivingEntity var137 = event.getEntity();
               if (var137 instanceof LivingEntity) {
                  var137.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, false));
                  var137.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false));
               }
            }

            if (data.hasArtifactSetBonus(ArtifactSet.MAIDEN_BELOVED, 4)) {
               float rawHeal = event.getNewDamage() * 0.1F;
               float maxHeal = Math.min(12.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
               player.heal(Math.min(rawHeal, maxHeal));
            }

            LivingEntity var144 = event.getEntity();
            if (var144 instanceof LivingEntity) {
               LivingEntity victim = var144;
               if (!var144.getTags().contains("shadow_soldier") && player.level() instanceof ServerLevel slVictim) {
                  String mTag = "shadow_master_" + player.getUUID();

                  for (Mob shadow : slVictim.getEntitiesOfClass(
                     Mob.class, player.getBoundingBox().inflate(64.0), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(mTag)
                  )) {
                     if (shadow.getTarget() != victim) {
                        shadow.setTarget(victim);
                     }
                  }
               }
            }

            if (data.isInTrance() && player.level() instanceof ServerLevel serverLevel) {
               if (player.tickCount % 4 == 0) {
                  serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.4, 0.3, 0.05);
                  serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.2, player.getZ(), 2, 0.2, 0.1, 0.2, 0.02);
               }

               player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 0, false, false, false));
               player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 0, false, false, false));
            }

            Integer skillLevel = data.isSkillEnabled("combat_instinct") ? (Integer)data.skills().get("combat_instinct") : null;
            if (skillLevel != null) {
               boolean evolved = skillLevel >= 6;
               event.setNewDamage(event.getNewDamage() + skillLevel.intValue() * (evolved ? 0.75F : 0.5F));
               if (evolved) {
                  player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, false, false, true));
               }

               changed |= gainSkillProgress(player, data, "combat_instinct");
            }

            if (data.isInTrance()) {
               event.setNewDamage(event.getNewDamage() + 3.0F);
            }

            if (data.isSkillEnabled("bloodthirst")) {
               float rawHeal = event.getNewDamage() * 0.15F;
               float maxHeal = Math.min(14.0F, Math.max(2.0F, player.getMaxHealth() * 0.05F));
               player.heal(Math.min(rawHeal, maxHeal));
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 3, 0.25, 0.25, 0.25, 0.05);
                  sl.sendParticles(
                     ParticleTypes.CRIMSON_SPORE, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 12, 0.3, 0.4, 0.3, 0.05
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 0.9F, 1.4F);
               }
            }

            boolean hasBlackFlashSkill = data.isSkillEnabled("black_flash") || data.isSkillEnabled("divergent_fist");
            if (hasBlackFlashSkill) {
               int comboCount = data.comboCount();
               double blackFlashChance = comboCount >= 5 ? 0.25 : (comboCount >= 2 ? 0.12 : 0.04);
               if (player.getRandom().nextDouble() < blackFlashChance && player.level() instanceof ServerLevel slBf) {
                  event.setNewDamage(event.getNewDamage() * 2.5F);
                  LivingEntity victim = event.getEntity();
                  slBf.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 20, 0.3, 0.3, 0.3, 0.2);
                  slBf.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 35, 0.4, 0.4, 0.4, 0.2);
                  slBf.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, victim.getX(), victim.getY() + 1.0, victim.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
                  slBf.sendParticles(ParticleTypes.FLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                  slBf.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.2F);
                  slBf.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 0.7F);
                  player.displayClientMessage(Component.translatable("awakening_system.skill.black_flash.msg"), true);
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false, true));
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, true));
               }
            }

            ItemStack mainWeapon = player.getMainHandItem();
            if (RpgAffixManager.hasAffixes(mainWeapon)) {
               float bonusMult = RpgAffixManager.getDamageBonus(mainWeapon);
               if (bonusMult > 0.0F) {
                  event.setNewDamage(event.getNewDamage() * (1.0F + bonusMult));
               }

               float lifesteal = RpgAffixManager.getLifesteal(mainWeapon);
               if (lifesteal > 0.0F) {
                  float rawHeal = event.getNewDamage() * lifesteal;
                  float maxHeal = Math.min(12.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
                  player.heal(Math.min(rawHeal, maxHeal));
               }
            }

            if (ManhwaArmorItem.hasFullSet(player, SetType.ADAMANTITE)) {
               float missingRatio = 1.0F - player.getHealth() / player.getMaxHealth();
               if (missingRatio > 0.05F) {
                  float bonusDmg = event.getNewDamage() * (missingRatio * 0.6F);
                  event.setNewDamage(event.getNewDamage() + bonusDmg);
                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(
                        ParticleTypes.FLAME, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 6, 0.3, 0.3, 0.3, 0.05
                     );
                     sl.sendParticles(
                        ParticleTypes.LAVA, event.getEntity().getX(), event.getEntity().getY() + 0.5, event.getEntity().getZ(), 2, 0.2, 0.2, 0.2, 0.05
                     );
                  }
               }
            }

            if (ManhwaArmorItem.hasFullSet(player, SetType.CELESTIAL)) {
               event.setNewDamage(event.getNewDamage() * 1.3F);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 4, 0.2, 0.2, 0.2, 0.05
                  );
               }
            }

            if (ManhwaArmorItem.hasFullSet(player, SetType.PRIMORDIAL)) {
               event.setNewDamage(event.getNewDamage() * 1.4F);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.DRAGON_BREATH, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 6, 0.2, 0.2, 0.2, 0.05
                  );
               }
            }

            float physDmgMult = data.getPhysiqueDamageMultiplier();
            if (physDmgMult > 0.0F) {
               event.setNewDamage(event.getNewDamage() * (1.0F + physDmgMult));
            }

            if ("heavenly_demon".equals(data.murimPhysique())) {
               float hpRatio = player.getHealth() / player.getMaxHealth();
               if (hpRatio <= 0.5F) {
                  event.setNewDamage(event.getNewDamage() * 1.35F);
                  player.heal(event.getNewDamage() * 0.2F);
                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.2, 0.3, 0.2, 0.02);
                  }
               }
            } else if ("pure_yang".equals(data.murimPhysique())) {
               event.getEntity().setRemainingFireTicks(60);
            } else if ("nine_yin".equals(data.murimPhysique())) {
               event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }

            if (data.isSkillEnabled("sword_heart_awakening") && player.getMainHandItem().getItem() instanceof SwordItem) {
               int lvl = data.skills().getOrDefault("sword_heart_awakening", 1);
               float bonus = 1.25F + (lvl - 1) * 0.15F;
               event.setNewDamage(event.getNewDamage() * bonus);
               if (player.level() instanceof ServerLevel sl) {
                  spawnShockwaveRing(
                     sl, event.getEntity().getX(), event.getEntity().getY() + 0.3, event.getEntity().getZ(), ParticleTypes.CHERRY_LEAVES, 1.8, 14, 0.05
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.7F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.1F, 1.6F);
               }
            }

            if (data.isSkillEnabled("limitless_sword_intent") && player.getHealth() >= player.getMaxHealth() * 0.8F) {
               int lvl = data.skills().getOrDefault("limitless_sword_intent", 1);
               float bonus = 1.3F + (lvl - 1) * 0.15F;
               event.setNewDamage(event.getNewDamage() * bonus);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.ENCHANTED_HIT,
                     event.getEntity().getX(),
                     event.getEntity().getY() + 1.0,
                     event.getEntity().getZ(),
                     10 * lvl,
                     0.3,
                     0.3,
                     0.3,
                     0.1
                  );
                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.1F, 1.8F
                  );
               }
            }

            if (data.isSkillEnabled("character_summary_scan")) {
               int lvl = data.skills().getOrDefault("character_summary_scan", 1);
               float bonus = 1.2F + (lvl - 1) * 0.12F;
               event.setNewDamage(event.getNewDamage() * bonus);
               LivingEntity var215 = event.getEntity();
               if (var215 instanceof LivingEntity) {
                  float curHp = Math.max(0.0F, var215.getHealth() - event.getNewDamage());
                  player.displayClientMessage(
                     Component.translatable(
                        "awakening_system.skill.omniscient_reader.scan",
                        new Object[]{
                           ClassSkillTree.getRankStars(lvl),
                           var215.getName().getString(),
                           String.format(Locale.US, "%.1f", curHp),
                           String.format(Locale.US, "%.1f", var215.getMaxHealth())
                        }
                     ),
                     true
                  );
               }

               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.ENCHANT, event.getEntity().getX(), event.getEntity().getY() + 1.2, event.getEntity().getZ(), 8, 0.3, 0.3, 0.3, 0.1
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.7F);
               }
            }

            if (data.isSkillEnabled("bookmark_mastery")) {
               int lvlx = data.skills().getOrDefault("bookmark_mastery", 1);
               float chance = 0.25F + (lvlx - 1) * 0.15F;
               if (player.getRandom().nextFloat() < chance) {
                  int buff = player.getRandom().nextInt(3);
                  int amp = lvlx >= 3 ? 2 : 1;
                  if (buff == 0) {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100 + lvlx * 30, amp));
                  } else if (buff == 1) {
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100 + lvlx * 30, amp));
                  } else {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100 + lvlx * 30, amp));
                  }

                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 14, 0.4, 0.5, 0.4, 0.1);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.8F);
                  }
               }
            }

            if (data.isSkillEnabled("blood_frenzy_passive")) {
               int lvlx = data.skills().getOrDefault("blood_frenzy_passive", 1);
               float missingHpRatio = 1.0F - player.getHealth() / player.getMaxHealth();
               if (missingHpRatio > 0.1F) {
                  event.setNewDamage(event.getNewDamage() * (1.0F + missingHpRatio * (0.5F + (lvlx - 1) * 0.25F)));
               }

               float rawHeal = event.getNewDamage() * (0.1F + (lvlx - 1) * 0.05F);
               float maxHeal = Math.min(14.0F, Math.max(2.0F, player.getMaxHealth() * 0.05F));
               player.heal(Math.min(rawHeal, maxHeal));
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.1F, 1.6F);
               }
            }

            if (data.isSkillEnabled("asura_bloodline")) {
               int lvlxx = data.skills().getOrDefault("asura_bloodline", 1);
               event.setNewDamage(event.getNewDamage() * (1.3F + (lvlxx - 1) * 0.15F));
               event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, 80 + lvlxx * 20, lvlxx >= 3 ? 2 : 1));
               data.addCultivationQi((int)Math.max(1.0F, event.getNewDamage() * (0.1F + (lvlxx - 1) * 0.05F)));
               float rawHeal = event.getNewDamage() * (0.05F + (lvlxx - 1) * 0.03F);
               float maxHeal = Math.min(10.0F, Math.max(2.0F, player.getMaxHealth() * 0.04F));
               player.heal(Math.min(rawHeal, maxHeal));
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.SOUL_FIRE_FLAME, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 12, 0.3, 0.4, 0.3, 0.05
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 1.4F);
               }
            }

            if (data.isSkillEnabled("phantom_ambush") && (player.isShiftKeyDown() || player.hasEffect(MobEffects.INVISIBILITY))) {
               int lvlxx = data.skills().getOrDefault("phantom_ambush", 1);
               event.setNewDamage(event.getNewDamage() * (2.0F + (lvlxx - 1) * 0.4F));
               event.getEntity().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80 + lvlxx * 20, 0));
               event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80 + lvlxx * 20, lvlxx >= 3 ? 3 : 2));
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.FLASH, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 1, 0.0, 0.0, 0.0, 0.0
                  );
                  sl.sendParticles(
                     ParticleTypes.SQUID_INK, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 16, 0.3, 0.4, 0.3, 0.05
                  );
                  sl.sendParticles(
                     ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 20 * lvlxx, 0.4, 0.5, 0.4, 0.2
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.3F, 1.6F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.4F);
               }
            }

            if (data.isSkillEnabled("death_mark_execution") && event.getEntity().getHealth() <= event.getEntity().getMaxHealth() * 0.3F) {
               int lvlxx = data.skills().getOrDefault("death_mark_execution", 1);
               event.setNewDamage(event.getNewDamage() * (1.5F + (lvlxx - 1) * 0.25F));
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.SCULK_SOUL,
                     event.getEntity().getX(),
                     event.getEntity().getY() + 1.0,
                     event.getEntity().getZ(),
                     15 * lvlxx,
                     0.3,
                     0.5,
                     0.3,
                     0.1
                  );
                  sl.sendParticles(
                     ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 10 * lvlxx, 0.3, 0.4, 0.3, 0.15
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.1F, 1.9F);
               }
            }

            if (ManhwaArtifactItem.hasArtifact(player, ArtifactType.KING_SLAYER_NECKLACE)) {
               LivingEntity victim = event.getEntity();
               boolean isBoss = victim.getMaxHealth() >= 100.0F
                  || victim instanceof WitherBoss
                  || victim instanceof EnderDragon
                  || victim instanceof ShadowMonarchBossEntity
                  || victim instanceof IgrisBossEntity
                  || victim instanceof BaranBossEntity;
               if (isBoss) {
                  event.setNewDamage(event.getNewDamage() * 1.25F);
                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
                  }
               }
            }

            if (ManhwaArtifactItem.hasArtifact(player, ArtifactType.DEMON_MONARCH_RING)) {
               LivingEntity victim = event.getEntity();
               event.setNewDamage(event.getNewDamage() + 4.0F);
               data.restoreEnergy(2);
               changed = true;
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
               }
            }

            if ("sword_saint".equals(data.hunterClass()) && player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater()) {
               event.setNewDamage(event.getNewDamage() * 1.35F);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.CHERRY_LEAVES, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 15, 0.4, 0.5, 0.4, 0.1
                  );
               }
            }

            if ("colossal_berserker".equals(data.hunterClass()) && event.getEntity().isBlocking() && event.getEntity() instanceof Player targetPlayer) {
               targetPlayer.disableShield();
               player.level()
                  .playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.2F, 0.9F);
            }

            if (data.isSkillEnabled("dragon_sense") && player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater()) {
               event.setNewDamage(event.getNewDamage() * 1.35F);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.CRIT, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 10, 0.3, 0.3, 0.3, 0.1
                  );
               }
            }

            if (data.cultivationQi() >= 2) {
               data.consumeCultivationQi(2);
               changed = true;
               int realm = data.dantianRealm();
               int openMeridians = data.getOpenedMeridiansCount();
               float qiBonus = (realm + 1) * 2.5F + openMeridians * 0.6F;
               if (data.isMeridianOpen(4) && player.fallDistance > 0.0F) {
                  qiBonus *= 1.35F;
               }

               if (data.isMeridianOpen(5)) {
                  event.getEntity().setRemainingFireTicks(60);
               }

               if (data.isMeridianOpen(11)) {
                  qiBonus += 4.0F;
                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(
                        ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.8, event.getEntity().getZ(), 2, 0.2, 0.2, 0.2, 0.02
                     );
                  }
               }

               event.setNewDamage(event.getNewDamage() + qiBonus);
               if (player.level() instanceof ServerLevel sl) {
                  sl.sendParticles(
                     ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + 1.0, event.getEntity().getZ(), 6, 0.2, 0.3, 0.2, 0.1
                  );
               }
            }

            applyCompatibilityDamageSkills(event, player, data);
            data.recordStatistic("damage_dealt", (int)Math.ceil(event.getNewDamage()));
            checkAchievements(player, data);
            if (event.getSource().getDirectEntity() instanceof AbstractArrow) {
               changed |= learnAndAnnounce(player, data, "ranged", 3, "deadeye") > 0;
               Integer deadeye = data.isSkillEnabled("deadeye") ? (Integer)data.skills().get("deadeye") : null;
               if (deadeye != null) {
                  boolean evolved = deadeye >= 6;
                  event.setNewDamage(event.getNewDamage() + deadeye.intValue() * (evolved ? 1.0F : 0.75F));
                  changed |= gainSkillProgress(player, data, "deadeye");
               }
            }

            if (data.emergencyWounded() && player.getHealth() > player.getMaxHealth() * 0.5F) {
               if (data.confirmEmergencyRecovery()) {
                  changed |= data.completeEmergency();
                  if (data.emergencyComplete()) {
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.emergency_complete.title",
                        "awakening_system.notification.emergency_complete.message",
                        -35226,
                        new Object[0]
                     );
                  }
               }

               changed = true;
            }

            if (changed) {
               AwakeningSystemMod.sync(player);
            }
            } finally {
               IS_PROCESSING_DAMAGE.set(false);
            }
         }
      } else {
         event.setNewDamage(0.0F);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingHeal(LivingHealEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (event.getAmount() <= 8.0F) {
            return;
         }

         if (Math.abs(event.getAmount() - player.getMaxHealth()) < 0.01F) {
            return;
         }

         boolean inCombat = player.tickCount - player.getLastHurtMobTimestamp() < 100 || player.tickCount - player.getLastHurtByMobTimestamp() < 100;
         if (inCombat) {
            float maxCombatHeal = Math.max(16.0F, player.getMaxHealth() * 0.08F);
            if (event.getAmount() > maxCombatHeal) {
               event.setAmount(maxCombatHeal);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
      if (IS_PROCESSING_DAMAGE.get()) return;
      IS_PROCESSING_DAMAGE.set(true);
      try {
      if (event.getEntity() instanceof ServerPlayer player && event.getNewDamage() > 0.0F) {
         if (player.getVehicle() instanceof MeditationSeatEntity seat) {
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
         data.recordStatistic("damage_taken", (int)Math.ceil(event.getNewDamage()));
         if (event.getNewDamage() >= 35.0F) {
            grantFable(player, "unbroken_rampart");
         }

         checkAchievements(player, data);
         Integer skillLevel = data.isSkillEnabled("enduring_flesh") ? (Integer)data.skills().get("enduring_flesh") : null;
         boolean changed = gained > 0;
         data.resetCombo();
         if (data.isDokkaebiScenarioActive()) {
            AABB searchBox = player.getBoundingBox().inflate(64.0);

            for (DokkaebiEntity d : player.serverLevel()
               .getEntitiesOfClass(DokkaebiEntity.class, searchBox, dx -> "survive_damage".equals(dx.getQuestType()) && dx.isQuestActive())) {
               d.addProgress(1, player);
            }
         }

         if (player.getHealth() > 0.0F && player.getHealth() <= player.getMaxHealth() * 0.25F) {
            changed |= data.recordEmergencySurvival();
            triggerConstellationReaction(player, ReactionType.LOW_HEALTH);
         }

         if (skillLevel != null) {
            boolean evolved = skillLevel >= 6;
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 + skillLevel * 10, evolved ? 1 : 0, false, false, true));
            if (evolved) {
               player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 + skillLevel * 10, 0, false, false, true));
            }

            if (data.canAdvanceSkill("enduring_flesh") && data.gainSkillProgress("enduring_flesh", 1)) {
               announceSkillLevel(player, "enduring_flesh", (Integer)data.skills().get("enduring_flesh"));
               changed = true;
            }
         }

         if (!before && data.skills().containsKey("enduring_flesh")) {
            announceSkill(player, "enduring_flesh");
            changed = true;
         }

         ItemStack mainHold = player.getMainHandItem();
         ItemStack offHold = player.getOffhandItem();
         boolean hasTransformedDoncella = mainHold.getItem() == SystemItems.LA_DONCELLA.get() && LaDoncellaSwordItem.isTransformed(mainHold)
            || offHold.getItem() == SystemItems.LA_DONCELLA.get() && LaDoncellaSwordItem.isTransformed(offHold);
         if (hasTransformedDoncella && event.getSource().getEntity() instanceof LivingEntity attackerMob && attackerMob != player) {
            attackerMob.hurt(player.damageSources().thorns(player), 32.0F);
            attackerMob.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
            if (player.level() instanceof ServerLevel sl) {
               sl.playSound(null, attackerMob.getX(), attackerMob.getY(), attackerMob.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.3F, 0.7F);
               sl.sendParticles(ParticleTypes.CRIMSON_SPORE, attackerMob.getX(), attackerMob.getY() + 1.0, attackerMob.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
            }
         }

         if (changed) {
            AwakeningSystemMod.sync(player);
         }
      }

      if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() != attacker && event.getNewDamage() > 0.0F) {
         SystemData attackerData = data(attacker);
         boolean tranceActivated = attackerData.addCombo();
         if (tranceActivated) {
            AwakeningSystemMod.notify(
               attacker, "awakening_system.notification.trance.title", "awakening_system.notification.trance.message", -43691, new Object[0]
            );
            attacker.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1.8F);
            triggerConstellationReaction(attacker, ReactionType.COMBO_TRANCE);
         }

         if (attackerData.isInTrance() && attacker.level() instanceof ServerLevel serverLevel) {
            AABB aabb = event.getEntity().getBoundingBox().inflate(3.5);
            List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
               LivingEntity.class, aabb, e -> e != attacker && e != event.getEntity() && e.isAlive() && !e.getType().getCategory().isFriendly()
            );
            float cleaveDamage = Math.max(1.0F, event.getNewDamage() * 0.5F);

            for (LivingEntity target : nearby) {
               target.hurt(attacker.damageSources().playerAttack(attacker), cleaveDamage);
               serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8, target.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
            }

            serverLevel.sendParticles(
               ParticleTypes.SWEEP_ATTACK, event.getEntity().getX(), event.getEntity().getY() + 0.5, event.getEntity().getZ(), 1, 0.0, 0.0, 0.0, 0.0
            );
         }

         AwakeningSystemMod.sync(attacker);
      }
      } finally {
         IS_PROCESSING_DAMAGE.set(false);
      }
   }

   @SubscribeEvent
   public static void onCraft(ItemCraftedEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SystemData data = data(player);
         data.recordStatistic("items_crafted", 1);
         checkAchievements(player, data);
         if (learnAndAnnounce(player, data, "crafting", 2, "artisan_hand") > 0) {
            long day = player.level().getDayTime() / 24000L;
            updateDailyProgress(player, data, day, 2, 1);
            if (data.completeDaily(player.level().getGameTime())) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.daily_complete.title", "awakening_system.notification.daily_complete.message", -9377537, new Object[0]
               );
            }

            Integer level = (Integer)data.skills().get("artisan_hand");
            if (level != null) {
               awardExperience(player, data, level * (level >= 6 ? 2 : 1));
               gainSkillProgress(player, data, "artisan_hand");
            }

            AwakeningSystemMod.sync(player);
         }
      }
   }

   @SubscribeEvent
   public static void onRightClickBlock(RightClickBlock event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (!player.isCreative() && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            ItemStack held = event.getItemStack();
            if (held.getItem() instanceof BucketItem
               || held.is(Items.WATER_BUCKET)
               || held.is(Items.LAVA_BUCKET)
               || held.is(Items.POWDER_SNOW_BUCKET)
               || held.is(Items.BUCKET)) {
               BlockPos targetPos = event.getPos();
               BlockPos placePos = event.getPos().relative(event.getFace());
               if (TeyvatCasinoBuilder.isInsideCasino(player.level(), targetPos) || TeyvatCasinoBuilder.isInsideCasino(player.level(), placePos)) {
                  event.setCanceled(true);
                  player.displayClientMessage(Component.translatable("awakening_system.casino.bucket_prohibited"), true);
                  return;
               }
            }
         }

         if (LimboWorldManager.isLimboDimension(player.level())) {
            BlockPos clicked = event.getPos();
            if (Math.abs(clicked.getX()) <= 4 && Math.abs(clicked.getZ() - -7) <= 4 && clicked.getY() >= 99 && clicked.getY() <= 107) {
               LimboWorldManager.returnFromLimbo(player);
               event.setCanceled(true);
               return;
            }

            ItemStack held = event.getItemStack();
            if (!player.isCreative() && !held.isEmpty() && isConventionalLightItem(held)) {
               event.setCanceled(true);
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.SMOKE, event.getPos().getX() + 0.5, event.getPos().getY() + 1.0, event.getPos().getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05
                  );
               player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);
               player.displayClientMessage(Component.translatable("awakening_system.limbo.light_extinguished"), true);
               return;
            }
         }

         if (DungeonManager.onRightClickChest(player, event.getPos())) {
            event.setCanceled(true);
            return;
         }

         if (handleTowerMissionBoardInteraction(player, event)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
         }

         if (handleAbyssPortalInteraction(player, event)) {
            event.setCanceled(true);
            return;
         }

         if (SpiralAbyssManager.handleKeyInteraction(player, event.getPos())) {
            event.setCanceled(true);
            return;
         }

         if (SpiralAbyssManager.handleChestInteraction(player, event.getPos())) {
            event.setCanceled(true);
            return;
         }

         handleSeriesStructureInteractions(player, event);
      }
   }

   private static boolean handleTowerMissionBoardInteraction(ServerPlayer player, RightClickBlock event) {
      BlockPos clickedPos = event.getPos();
      Level level = player.level();
      if (level.getBlockState(clickedPos).getBlock() instanceof TowerMissionBoardBlock) {
         TowerDailyMissionManager.openBoardFor(player);
         return true;
      } else {
         for (int dy = -2; dy <= 0; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -2; dz <= 2; dz++) {
                  BlockPos candidatePos = clickedPos.offset(dx, dy, dz);
                  BlockState candidateState = level.getBlockState(candidatePos);
                  if (candidateState.getBlock() instanceof TowerMissionBoardBlock) {
                     Direction facing = (Direction)candidateState.getValue(TowerMissionBoardBlock.FACING);
                     Direction right = facing.getClockWise();
                     int relY = clickedPos.getY() - candidatePos.getY();
                     if (relY >= 0 && relY <= 2) {
                        int lateral = (clickedPos.getX() - candidatePos.getX()) * right.getStepX()
                           + (clickedPos.getZ() - candidatePos.getZ()) * right.getStepZ();
                        int depth = (clickedPos.getX() - candidatePos.getX()) * facing.getStepX()
                           + (clickedPos.getZ() - candidatePos.getZ()) * facing.getStepZ();
                        if (lateral >= -2 && lateral <= 2 && (depth == 0 || depth == -1)) {
                           TowerDailyMissionManager.openBoardFor(player);
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   private static boolean handleAbyssPortalInteraction(ServerPlayer player, RightClickBlock event) {
      BlockPos pos = event.getPos();
      Level level = player.level();

      for (int dy = -3; dy <= 1; dy++) {
         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos candidate = pos.offset(dx, dy, dz);
               if (level.getBlockState(candidate).getBlock() instanceof AscensionPortalBlock) {
                  AscensionPortalBlock.handlePortalActivation(player, candidate, level);
                  return true;
               }
            }
         }
      }

      AbyssPortalEntry portal = TeyvatWorldManager.findNearPortal(player.level().dimension(), pos, 14.0);
      if (portal == null) {
         return false;
      } else {
         if (!portal.active) {
            portal.active = true;
            TeyvatWorldManager.setAbyssActive(player.getServer(), true);
            player.sendSystemMessage(Component.translatable("awakening_system.abyss.seal_broken"));
            event.getLevel().playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 1.0F);
            event.getLevel().playSound(null, pos, (SoundEvent)ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.BLOCKS, 1.5F, 0.9F);
         }

         player.playNotifySound((SoundEvent)ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
         SpiralAbyssManager.openAbyssScreen(player);
         return true;
      }
   }

   @SubscribeEvent
   public static void onEntityInteract(EntityInteract event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (event.getHand() == InteractionHand.MAIN_HAND) {
            Entity target = event.getTarget();
            if (IceAndFireCompat.isExtractableCorpse(target)) {
               SystemData data = data(player);
               boolean hasShadowMonarch = data.hasHunterClass() && "shadow_monarch".equals(data.hunterClass())
                  || data.skills().containsKey("unique_absolute_extraction");
               if (hasShadowMonarch) {
                  boolean emptyHand = player.getMainHandItem().isEmpty();
                  boolean holdingMonarchWeapon = player.getMainHandItem().getItem() instanceof ShadowDaggersItem;
                  if (emptyHand || holdingMonarchWeapon || player.isShiftKeyDown()) {
                     boolean isUnique = data.skills().containsKey("unique_absolute_extraction");
                     int intel = data.intelligence();
                     int will = data.willpower();
                     boolean extracted = IceAndFireCompat.attemptExtractCorpse(player, target, isUnique, intel, will);
                     if (extracted) {
                        event.setCanceled(true);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRightClickItem(RightClickItem event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ItemStack var5 = event.getItemStack();
         if (!player.isCreative()
            && !player.getAbilities().instabuild
            && !player.hasPermissions(2)
            && (
               var5.getItem() instanceof BucketItem
                  || var5.is(Items.WATER_BUCKET)
                  || var5.is(Items.LAVA_BUCKET)
                  || var5.is(Items.POWDER_SNOW_BUCKET)
                  || var5.is(Items.BUCKET)
            )
            && TeyvatCasinoBuilder.isInsideCasino(player.level(), player.blockPosition())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("awakening_system.casino.bucket_prohibited"), true);
         } else if (SystemItems.DUNGEON_KEY != null && SystemItems.DUNGEON_KEY.isBound() && var5.is((Item)SystemItems.DUNGEON_KEY.value())) {
          if (LimboWorldManager.isLimboDimension(player.level())) {
               player.displayClientMessage(Component.translatable("awakening_system.limbo.dungeon_blocked"), true);
               event.setCanceled(true);
            } else {
               SystemData data = data(player);
               PacketDistributor.sendToPlayer(player, new OpenDungeonKeyPayload(data.level(), data.rank()), new CustomPacketPayload[0]);
               event.setCanceled(true);
            }
         } else if (SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound() && var5.is((Item)SystemItems.HUNTER_GUIDE.value())) {
            PacketDistributor.sendToPlayer(player, new OpenGuidebookPayload(), new CustomPacketPayload[0]);
            event.setCanceled(true);
         } else if (SystemItems.HUNTER_BESTIARY != null && SystemItems.HUNTER_BESTIARY.isBound() && var5.is((Item)SystemItems.HUNTER_BESTIARY.value())) {
            PacketDistributor.sendToPlayer(player, new OpenBestiaryPayload(), new CustomPacketPayload[0]);
            event.setCanceled(true);
         } else if (SystemItems.RANKER_TOWER_TICKET != null
            && SystemItems.RANKER_TOWER_TICKET.isBound()
            && var5.is((Item)SystemItems.RANKER_TOWER_TICKET.value())) {
          if (LimboWorldManager.isLimboDimension(player.level())) {
               player.displayClientMessage(Component.translatable("awakening_system.limbo.escape_blocked"), true);
               event.setCanceled(true);
            } else {
               TowerManager.enterTower(player);
               if (!player.isCreative()) {
                  var5.shrink(1);
               }

               event.setCanceled(true);
            }
         } else {
            if (SystemItems.PRIMOGEM != null && SystemItems.PRIMOGEM.isBound() && var5.is((Item)SystemItems.PRIMOGEM.value())) {
               SystemData data = data(player);
               if (!data.isWelkinEnhanced()
                  && data.welkinQuestStep() == 3
                  && player.getXRot() < -35.0F
                  && (player.level().isNight() || player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD))) {
                  GachaManager.completeColumbinaAscension(player);
                  event.setCanceled(true);
                  return;
               }
            }

            if (var5.is(Items.POTION)) {
               SystemData data = data(player);
               if (learnAndAnnounce(player, data, "alchemy", 3, "alchemist_eye") > 0) {
                  Integer level = (Integer)data.skills().get("alchemist_eye");
                  if (level != null) {
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * level, level >= 6 ? 1 : 0, false, false, true));
                     gainSkillProgress(player, data, "alchemist_eye");
                  }

                  AwakeningSystemMod.sync(player);
               }
            }
         }
      }
   }

   public static void handleSelectDungeon(ServerPlayer player, String rank, String biome) {
      if (LimboWorldManager.isLimboDimension(player.level())) {
         player.displayClientMessage(Component.translatable("awakening_system.limbo.dungeon_blocked"), true);
      } else if (DungeonManager.isInDungeon(player.getUUID())) {
         player.displayClientMessage(Component.translatable("awakening_system.action.already_in_dungeon"), true);
      } else {
         if (!player.isCreative()) {
            boolean found = false;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
               ItemStack stack = player.getInventory().getItem(i);
               if (SystemItems.DUNGEON_KEY != null && SystemItems.DUNGEON_KEY.isBound() && stack.is((Item)SystemItems.DUNGEON_KEY.value())) {
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

         String r = rank != null && !rank.isEmpty() ? rank : "E";
         String b = biome != null && !biome.isEmpty() ? biome : "crypt";
         DungeonManager.spawnRiftNear(player, r, b);
         player.displayClientMessage(Component.translatable("awakening_system.dungeon.portal_invoked", new Object[]{r, b.toUpperCase()}), true);
      }
   }

   @SubscribeEvent
   public static void onAttackShadow(AttackEntityEvent event) {
      if (event.getTarget() instanceof LivingEntity target
         && target.getTags().contains("shadow_soldier")
         && target.getTags().contains("shadow_master_" + event.getEntity().getUUID())) {
         event.setCanceled(true);
      }

      if (event.getEntity().getVehicle() instanceof AncientDragonEntity dragon) {
         if (event.getTarget() == dragon || event.getTarget() instanceof DragonBodyPart part && part.getParent() == dragon) {
            event.setCanceled(true);
         }

         dragon.shootPlayerBreath(event.getEntity());
      }
   }

   @SubscribeEvent
   public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer sp && LimboWorldManager.isLimboDimension(sp.level()) && event.getAmount() >= sp.getHealth()) {
         event.setCanceled(true);
         sp.setHealth(Math.max(4.0F, sp.getHealth()));
         sp.displayClientMessage(Component.translatable("awakening_system.limbo.immortal_pulse"), true);
      } else if (!event.getSource().is(DamageTypes.HOT_FLOOR)
         || !(event.getEntity() instanceof ServerPlayer)
            && !event.getEntity().getTags().contains("shadow_soldier")
            && !(event.getEntity() instanceof IsekaiSlimeEntity)
            && !event.getEntity().level().dimension().equals(DungeonManager.DUNGEON_REALM)
            && !event.getEntity().level().dimension().equals(TowerManager.RANKER_TOWER)
            && !event.getEntity().level().dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
         if (event.getSource().is(DamageTypes.FALL) && event.getEntity() instanceof ServerPlayer sp) {
            SystemData d = data(sp);
            if ("creator".equals(d.equippedTitle())) {
               event.setCanceled(true);
               return;
            }

            if ("michi_lover".equals(d.equippedTitle())) {
               float reduced = event.getAmount() * 0.4F;
               if (reduced < 2.0F) {
                  event.setCanceled(true);
               } else {
                  event.setAmount(reduced);
               }

               sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CAT_PURREOW, SoundSource.PLAYERS, 0.7F, 1.3F);
               return;
            }

            if ("caffeine_addict".equals(d.equippedTitle())) {
               float reduced = event.getAmount() * 0.4F;
               if (reduced < 3.0F) {
                  event.setCanceled(true);
               } else {
                  event.setAmount(reduced);
               }

               return;
            }

            if ("pokemon_master".equals(d.equippedTitle())) {
               float reduced = event.getAmount() * 0.5F;
               if (reduced < 3.0F) {
                  event.setCanceled(true);
               } else {
                  event.setAmount(reduced);
               }

               sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.6F);
               return;
            }
         }

         if (event.getEntity().getTags().contains("shadow_soldier")) {
            Entity srcEntity = event.getSource().getEntity();
            Entity directEntity = event.getSource().getDirectEntity();

            for (String tag : event.getEntity().getTags()) {
               if (tag.startsWith("shadow_master_")) {
                  String masterUuid = tag.substring("shadow_master_".length());
                  if (srcEntity != null && srcEntity.getStringUUID().equals(masterUuid)
                     || directEntity != null && directEntity.getStringUUID().equals(masterUuid)) {
                     event.setCanceled(true);
                     return;
                  }
               }
            }

            if (srcEntity != null && srcEntity.getTags().contains("shadow_soldier")
               || directEntity != null && directEntity.getTags().contains("shadow_soldier")) {
               event.setCanceled(true);
               return;
            }
         }

         if (event.getEntity() instanceof ServerPlayer sp) {
            Entity srcEntity = event.getSource().getEntity();
            Entity directEntity = event.getSource().getDirectEntity();
            String masterTag = "shadow_master_" + sp.getUUID();
            if (srcEntity != null && srcEntity.getTags().contains(masterTag) || directEntity != null && directEntity.getTags().contains(masterTag)) {
               event.setCanceled(true);
               return;
            }

            if (srcEntity instanceof LivingEntity attacker && !attacker.getTags().contains("shadow_soldier") && sp.level() instanceof ServerLevel slAttacker) {
               for (Mob shadow : slAttacker.getEntitiesOfClass(
                  Mob.class, sp.getBoundingBox().inflate(64.0), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
               )) {
                  if (shadow.getTarget() != attacker) {
                     shadow.setTarget(attacker);
                  }
               }
            }

            SystemData spData = data(sp);
            long gameTime = sp.level().getGameTime();
            long bellUntil = sp.getPersistentData().getLong("spirit_jade_bell_active_until");
            if (gameTime < bellUntil) {
               event.setCanceled(true);
               sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.4F);
               if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != sp) {
                  if (!IS_PROCESSING_DAMAGE.get()) {
                  IS_PROCESSING_DAMAGE.set(true);
                  try {
                     attacker.hurt(sp.damageSources().playerAttack(sp), 15.0F);
                  } finally {
                     IS_PROCESSING_DAMAGE.set(false);
                  }
               }
                  attacker.knockback(1.5, sp.getX() - attacker.getX(), sp.getZ() - attacker.getZ());
               }

               if (sp.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0, sp.getZ(), 25, 0.6, 0.6, 0.6, 0.1);
                  sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0, sp.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
               }

               return;
            }

            if (ManhwaArmorItem.hasFullSet(sp, SetType.MITHRIL) && event.getSource().is(DamageTypeTags.IS_FALL)) {
               event.setCanceled(true);
               return;
            }

            if (ManhwaArmorItem.hasFullSet(sp, SetType.ADAMANTITE) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
               event.setCanceled(true);
               return;
            }

            if (ManhwaArmorItem.hasFullSet(sp, SetType.SPIRIT_JADE)) {
               float absorbed = event.getAmount() * 0.25F;
               event.setAmount(Math.max(1.0F, event.getAmount() - absorbed));
               spData.addCultivationQi((int)Math.max(1.0F, absorbed * 2.0F));
               spData.restoreEnergy((int)Math.max(1.0F, absorbed));
               sp.heal(absorbed * 0.4F);
               if (sp.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0, sp.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
                  sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.2, sp.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
               }
            }

            boolean hasFourthWall = spData.isSkillEnabled("fourth_wall");
            if (hasFourthWall && event.getAmount() >= sp.getHealth()) {
               long cd = sp.getPersistentData().getLong("fourth_wall_fatal_cd");
               if (gameTime >= cd) {
                  sp.getPersistentData().putLong("fourth_wall_fatal_cd", gameTime + 6000L);
                  event.setCanceled(true);
                  sp.setHealth(10.0F);
                  sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
                  sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
                  sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
                  sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
                  sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
                  if (sp.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0, sp.getZ(), 50, 0.5, 0.8, 0.5, 0.3);
                     sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0, sp.getZ(), 40, 0.6, 0.6, 0.6, 0.2);
                  }

                  AwakeningSystemMod.notify(
                     sp, "awakening_system.notification.fourth_wall.title", "awakening_system.notification.fourth_wall.desc", -9377537, new Object[0]
                  );
                  return;
               }
            }

            if ("star_apostle".equals(spData.hunterClass()) && event.getAmount() >= 12.0F) {
               long cd = sp.getPersistentData().getLong("star_apostle_barrier_cd");
               if (gameTime >= cd) {
                  sp.getPersistentData().putLong("star_apostle_barrier_cd", gameTime + 1800L);
                  sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 3));
                  sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
                  sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.5F);
                  if (sp.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0, sp.getZ(), 30, 0.5, 0.8, 0.5, 0.1);
                  }

                  AwakeningSystemMod.notify(
                     sp, "awakening_system.notification.ether_barrier.title", "awakening_system.notification.ether_barrier.desc", -10496, new Object[0]
                  );
               }
            }

            if (spData.isSkillEnabled("black_heart_resurrection") && event.getAmount() >= sp.getHealth()) {
               long cd = sp.getPersistentData().getLong("black_heart_fatal_cd");
               if (gameTime >= cd) {
                  sp.getPersistentData().putLong("black_heart_fatal_cd", gameTime + 6000L);
                  event.setCanceled(true);
                  sp.setHealth(sp.getMaxHealth() * 0.5F);
                  spData.restoreMana(spData.maxMana());
                  sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 3));
                  sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 2));
                  if (sp.level() instanceof ServerLevel sl) {
                     spawnVortexPillar(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 3.5, 6.0, 6, 18);
                     spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2, sp.getZ(), ParticleTypes.SCULK_SOUL, 6.0, 32, 0.12);
                     sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5, sp.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.2F, 0.6F);
                  }

                  AwakeningSystemMod.notify(
                     sp, "awakening_system.notification.black_heart.title", "awakening_system.notification.black_heart.desc", -6723841, new Object[0]
                  );
                  return;
               }
            }

            if (spData.isSkillEnabled("immortal_berserk") && event.getAmount() >= sp.getHealth()) {
               long cd = sp.getPersistentData().getLong("immortal_berserk_fatal_cd");
               if (gameTime >= cd) {
                  sp.getPersistentData().putLong("immortal_berserk_fatal_cd", gameTime + 6000L);
                  event.setCanceled(true);
                  sp.setHealth(1.0F);
                  sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4));
                  sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3));
                  sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                  if (sp.level() instanceof ServerLevel sl) {
                     spawnShockwaveRing(sl, sp.getX(), sp.getY() + 0.2, sp.getZ(), ParticleTypes.LAVA, 6.0, 32, 0.15);
                     spawnVortexPillar(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.FLAME, 3.0, 5.0, 5, 16);
                     sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5, sp.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5F, 0.7F);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.1F);
                  }

                  AwakeningSystemMod.notify(
                     sp, "awakening_system.notification.immortal_fury.title", "awakening_system.notification.immortal_fury.desc", -52429, new Object[0]
                  );
                  return;
               }
            }

            if (spData.isSkillEnabled("qi_reversal_absorption")) {
               float absorbed = event.getAmount() * 0.25F;
               event.setAmount(Math.max(1.0F, event.getAmount() - absorbed));
               spData.addCultivationQi((int)Math.max(1.0F, absorbed * 2.0F));
               sp.heal(absorbed * 0.3F);
               if (sp.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0, sp.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
               }
            }

            if (spData.isSkillEnabled("mana_shield_barrier") && spData.mana() > 10) {
               float absorbed = Math.min(event.getAmount() * 0.35F, spData.mana() / 4.0F);
               if (absorbed > 0.5F) {
                  event.setAmount(Math.max(0.5F, event.getAmount() - absorbed));
                  spData.spendMana((int)(absorbed * 4.0F));
                  if (sp.level() instanceof ServerLevel sl) {
                     spawnExpandingDome(sl, sp.getX(), sp.getY(), sp.getZ(), ParticleTypes.ENCHANT, 2.2, 3, 14);
                     sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0, sp.getZ(), 15, 0.4, 0.5, 0.4, 0.05);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.7F);
                     sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.1F, 1.5F);
                  }
               }
            }

            if (spData.isSkillEnabled("dragon_scales_hardening")) {
               event.setAmount(event.getAmount() * 0.7F);
               if (sp.level() instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0, sp.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
                  sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.8F);
               }

               if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                  event.setCanceled(true);
                  return;
               }
            }
         }
      } else {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onFireOrBlockedDamage(Pre event) {
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

      if (event.getSource().getEntity() != null
         && event.getSource().getEntity().getTags().contains("shadow_soldier")
         && event.getEntity() instanceof ServerPlayer sp
         && event.getSource().getEntity().getTags().contains("shadow_master_" + sp.getUUID())) {
         event.setNewDamage(0.0F);
      } else if (event.getEntity() instanceof ServerPlayer player) {
         SystemData var11 = data(player);
         boolean changed = false;
         if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            changed |= learnAndAnnounce(player, var11, "fire", 2, "fire_ward") > 0;
            Integer level = var11.isSkillEnabled("fire_ward") ? (Integer)var11.skills().get("fire_ward") : null;
            if (level != null) {
               boolean evolved = level >= 6;
               event.setNewDamage(Math.max(0.0F, event.getNewDamage() - (evolved ? level + 2 : level)));
               if (evolved) {
                  player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
               }

               changed |= gainSkillProgress(player, var11, "fire_ward");
            }
         }

         if (player.isBlocking() && event.getNewDamage() > 0.0F) {
            changed |= learnAndAnnounce(player, var11, "protection", 2, "guardian_stance") > 0;
            Integer level = var11.isSkillEnabled("guardian_stance") ? (Integer)var11.skills().get("guardian_stance") : null;
            if (level != null) {
               boolean evolved = level >= 6;
               event.setNewDamage(Math.max(0.0F, event.getNewDamage() - (level.intValue() * 0.5F + (evolved ? 1.0F : 0.0F))));
               changed |= gainSkillProgress(player, var11, "guardian_stance");
            }
         }

         if (var11.isSkillEnabled("blue_dragon_scales")) {
            if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
               event.setNewDamage(0.0F);
               player.clearFire();
            }

            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) || event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
               event.setNewDamage(event.getNewDamage() * 0.75F);
            }
         }

         if (var11.isSkillEnabled("fourth_wall")) {
            float maxHealth = player.getMaxHealth();
            if (event.getNewDamage() > maxHealth * 0.4F) {
               event.setNewDamage(event.getNewDamage() * 0.5F);
               if (player.level() instanceof ServerLevel sl) {
                  spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.END_ROD, 2.5, 20, 0.08);
                  sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.4, 0.6, 0.4, 0.1);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.8F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.3F, 1.5F);
               }
            }
         }

         if (var11.isSkillEnabled("iron_will")
            && player.getHealth() <= player.getMaxHealth() * 0.3F
            && player.level().getGameTime() >= var11.ironWillCooldownUntil()) {
            var11.setIronWillCooldownUntil(player.level().getGameTime() + 900L);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true));
            if (player.level() instanceof ServerLevel sl) {
               spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.8, 4, 18);
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
               sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.3F, 1.3F);
               sl.playSound(
                  null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.2F, 1.1F
               );
            }

            AwakeningSystemMod.notify(
               player, "awakening_system.notification.iron_will.title", "awakening_system.notification.iron_will.message", -10138, new Object[0]
            );
            changed = true;
         }

         if (changed) {
            AwakeningSystemMod.sync(player);
         }
      }
   }

   @SubscribeEvent
   public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
      if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide()) {
         if (mob.tickCount % 20 == 0) {
            WorldBossScalingManager.handleBossLeash(mob);
         }

         if (mob.tickCount % 40 == 0 && mob instanceof Enemy && mob.getTarget() instanceof ServerPlayer sp) {
            RpgLevelZoneManager.scaleMobToPlayer(mob, sp);
         }
      }
   }

   @SubscribeEvent
   public static void onPickupXp(PickupXp event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         boolean var4 = false;
         if (player.getPersistentData().getInt("UmaBitterJuiceTicks") > 0) {
            var4 = true;
         }

         if (!var4
            && SystemItems.ANKLE_WEIGHTS != null
            && SystemItems.ANKLE_WEIGHTS.isBound()
            && player.getItemBySlot(EquipmentSlot.FEET).is((Item)SystemItems.ANKLE_WEIGHTS.value())) {
            var4 = true;
         }

         if (var4 && event.getOrb() != null) {
            int extra = event.getOrb().getValue();
            if (extra > 0) {
               player.giveExperiencePoints(extra);
            }
         }
      }
   }

   private static boolean isConventionalLightItem(ItemStack stack) {
      return stack != null && !stack.isEmpty()
         ? stack.is(Items.TORCH)
            || stack.is(Items.SOUL_TORCH)
            || stack.is(Items.LANTERN)
            || stack.is(Items.SOUL_LANTERN)
            || stack.is(Items.CAMPFIRE)
            || stack.is(Items.SOUL_CAMPFIRE)
            || stack.is(Items.GLOWSTONE)
            || stack.is(Items.SEA_LANTERN)
            || stack.is(Items.SHROOMLIGHT)
            || stack.is(Items.OCHRE_FROGLIGHT)
            || stack.is(Items.PEARLESCENT_FROGLIGHT)
            || stack.is(Items.VERDANT_FROGLIGHT)
            || stack.is(Items.END_ROD)
            || stack.is(Items.CANDLE)
            || stack.is(Items.FLINT_AND_STEEL)
            || stack.is(Items.FIRE_CHARGE)
            || stack.is(Items.LAVA_BUCKET)
         : false;
   }

   private static boolean isNearLimboSafeZone(ServerPlayer player) {
      BlockPos pPos = player.blockPosition();
      if (pPos.closerThan(LimboWorldManager.SPAWN_POS, 16.0)) {
         return true;
      } else {
         for (BlockPos door : LimboWorldManager.CARDINAL_DOORS) {
            if (pPos.closerThan(door, 50.0)) {
               return true;
            }
         }

         Level level = player.level();

         for (BlockPos testPos : BlockPos.betweenClosed(pPos.offset(-10, -3, -10), pPos.offset(10, 5, 10))) {
            if (level.getBlockState(testPos).is((Block)SystemBlocks.JACK_PUMPKIN.get())) {
               return true;
            }
         }

         return false;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         tickPlayerCharging(player);
         if (player.tickCount % 4 == 0) {
            GreatTowerWorldManager.enforcePlayerBoundary(player);
         }

         if (player.tickCount % 40 == 0) {
            SystemData tickData = data(player);
            if (tickData != null) {
               if (player.getArmorValue() >= 15) {
                  grantFable(player, "iron_will");
               }

               if (tickData.coins() >= 25000) {
                  grantFable(player, "wealth_of_nations");
               }

               if (!tickData.hasFable("speed_demon") && player.getDeltaMovement().horizontalDistance() > 0.42) {
                  grantFable(player, "speed_demon");
               }
            }
         }

         if (LimboWorldManager.isLimboDimension(player.level())) {
            if (player.getHealth() < player.getMaxHealth() && player.tickCount % 20 == 0) {
               player.heal(4.0F);
            }

            if (player.tickCount % 40 == 0 && player.level() instanceof ServerLevel sl) {
               LimboWorldManager.checkNearDoors(sl, player.blockPosition());
            }

            if (!player.isCreative() && !player.isSpectator() && player.tickCount % 20 == 0) {
               boolean holdsPumpkin = player.getMainHandItem().is((Item)SystemItems.JACK_PUMPKIN.get())
                  || player.getOffhandItem().is((Item)SystemItems.JACK_PUMPKIN.get());
               if (!holdsPumpkin) {
                  boolean nearSafe = isNearLimboSafeZone(player);
                  if (!nearSafe) {
                     player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 65, 0, false, false, false));
                  }
               }
            }

            if (!player.isCreative() && !player.isSpectator() && player.tickCount % 40 == 0) {
               ItemStack main = player.getMainHandItem();
               ItemStack off = player.getOffhandItem();
               if (isConventionalLightItem(main) || isConventionalLightItem(off)) {
                  if (player.level() instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.15, 0.2, 0.15, 0.02);
                  }

                  player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.4F);
                  player.displayClientMessage(Component.translatable("awakening_system.limbo.light_extinguished"), true);
               }
            }

            if (player.getY() < 0.0) {
               player.teleportTo(player.serverLevel(), 0.5, 1.0, 0.5, player.getYRot(), player.getXRot());
               player.fallDistance = 0.0F;
               player.clearFire();
               player.setHealth(player.getMaxHealth());
               player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, 0.5, 1.5, 0.5, 50, 1.0, 1.0, 1.0, 0.05);
               player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
               player.displayClientMessage(Component.translatable("awakening_system.limbo.void_rescue"), true);
            }
         }

         SystemData pData = data(player);
         if ("michi_lover".equals(pData.equippedTitle())) {
            if (player.tickCount % 35 == 0 && player.serverLevel() != null) {
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.CHERRY_LEAVES,
                     player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.8,
                     player.getY() + 0.8 + (player.getRandom().nextDouble() - 0.5) * 0.5,
                     player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.8,
                     1,
                     0.02,
                     0.05,
                     0.02,
                     0.01
                  );
            }

            if (player.isShiftKeyDown() && player.tickCount % 50 == 0) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CAT_PURR, SoundSource.PLAYERS, 0.65F, 1.0F);
               if (player.serverLevel() != null) {
                  player.serverLevel()
                     .sendParticles(
                        ParticleTypes.HEART,
                        player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                        player.getY() + 1.2,
                        player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                        1,
                        0.0,
                        0.02,
                        0.0,
                        0.01
                     );
               }
            } else if (player.tickCount % 400 == 0 && player.getRandom().nextFloat() < 0.35F) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CAT_PURREOW, SoundSource.PLAYERS, 0.5F, 1.1F);
            }

            if (player.tickCount % 20 == 0) {
               AABB catBox = player.getBoundingBox().inflate(10.0);

               for (Creeper creeper : player.level().getEntitiesOfClass(Creeper.class, catBox)) {
                  if (creeper.getTarget() == player) {
                     creeper.setTarget(null);
                  }
               }
            }
         }

         if ("caffeine_addict".equals(pData.equippedTitle())) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false, false));
            if (player.tickCount % 15 == 0 && player.serverLevel() != null && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.SMOKE,
                     player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                     player.getY() + 0.1,
                     player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                     2,
                     0.02,
                     0.05,
                     0.02,
                     0.01
                  );
            }

            if (player.isShiftKeyDown() && player.tickCount % 60 == 0) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.7F, 1.1F);
               if (player.serverLevel() != null) {
                  player.serverLevel()
                     .sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.2, player.getZ(), 1, 0.01, 0.04, 0.01, 0.01);
               }
            }
         }

         if ("futanari_hentai_master".equals(pData.equippedTitle())) {
            if (player.tickCount % 40 == 0 && player.serverLevel() != null) {
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.HEART,
                     player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.8,
                     player.getY() + 0.8 + (player.getRandom().nextDouble() - 0.5) * 0.5,
                     player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.8,
                     1,
                     0.02,
                     0.05,
                     0.02,
                     0.01
                  );
            }

            if (player.isShiftKeyDown() && player.tickCount % 60 == 0) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.4F);
               if (player.serverLevel() != null) {
                  player.serverLevel().sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.1, 0.1, 0.1, 0.02);
                  player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.15, 0.15, 0.15, 0.02);
               }
            }

            if (player.tickCount % 80 == 0 && player.getHealth() < player.getMaxHealth()) {
               player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, false, false, false));
            }
         }

         if ("pokemon_master".equals(pData.equippedTitle())) {
            if (player.tickCount % 20 == 0 && player.serverLevel() != null && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.ELECTRIC_SPARK,
                     player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                     player.getY() + 0.1,
                     player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                     3,
                     0.02,
                     0.04,
                     0.02,
                     0.02
                  );
            }

            if (player.isShiftKeyDown() && player.tickCount % 50 == 0) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.8F);
               if (player.serverLevel() != null) {
                  player.serverLevel()
                     .sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.25, 0.25, 0.25, 0.05);
                  player.serverLevel().sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.2, player.getZ(), 3, 0.1, 0.15, 0.1, 0.02);
               }
            }

            if (player.tickCount % 60 == 0 && player.getHealth() < player.getMaxHealth() * 0.3F) {
               player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 70, 0, false, false, false));
               player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 70, 0, false, false, false));
               if (player.serverLevel() != null) {
                  player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 4, 0.2, 0.3, 0.2, 0.02);
               }
            }
         }

         if ("creator".equals(pData.equippedTitle())) {
            if (player.tickCount % 25 == 0 && player.serverLevel() != null && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
               player.serverLevel()
                  .sendParticles(
                     ParticleTypes.TOTEM_OF_UNDYING,
                     player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                     player.getY() + 0.2,
                     player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                     2,
                     0.02,
                     0.04,
                     0.02,
                     0.02
                  );
            }

            if (player.isShiftKeyDown() && player.tickCount % 60 == 0 && player.serverLevel() != null) {
               player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 3, 0.1, 0.15, 0.1, 0.02);
               player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.8, player.getZ(), 4, 0.2, 0.2, 0.2, 0.05);
            }

            if (player.tickCount % 50 == 0 && player.getHealth() < player.getMaxHealth()) {
               player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, false));
            }
         }

         if (player.tickCount % 4 == 0) {
            checkVipZoneBarrier(player);
         }

         if (ConstellationChannelHandler.isAstralWatching(player.getUUID()) && player.isShiftKeyDown()) {
            ConstellationChannelHandler.returnFromAstral(player);
         }

         if (SubwayGrabHandleBlock.GRABBED_PLAYERS.containsKey(player.getUUID())) {
            BlockPos handlePos = (BlockPos)SubwayGrabHandleBlock.GRABBED_PLAYERS.get(player.getUUID());
            if (!player.isShiftKeyDown()
               && !(player.distanceToSqr(handlePos.getX() + 0.5, player.getY(), handlePos.getZ() + 0.5) > 6.0)
               && player.level().getBlockState(handlePos).getBlock() instanceof SubwayGrabHandleBlock) {
               player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
               player.fallDistance = 0.0F;
            } else {
               SubwayGrabHandleBlock.GRABBED_PLAYERS.remove(player.getUUID());
               player.displayClientMessage(Component.translatable("awakening_system.metro.handrail_released"), true);
            }
         }

         if (player.tickCount % 60 == 0 && SystemItems.HUNTER_GUIDE != null && SystemItems.HUNTER_GUIDE.isBound()) {
            int gCount = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
               ItemStack it = player.getInventory().getItem(i);
               if (it.is((Item)SystemItems.HUNTER_GUIDE.value())) {
                  if (++gCount > 1) {
                     player.getInventory().setItem(i, ItemStack.EMPTY);
                  } else if (it.getCount() > 1) {
                     it.setCount(1);
                  }
               }
            }
         }

         if (player.tickCount % 60 == 0 && SystemItems.HUNTER_BESTIARY != null && SystemItems.HUNTER_BESTIARY.isBound()) {
            int bCount = 0;

            for (int ix = 0; ix < player.getInventory().getContainerSize(); ix++) {
               ItemStack it = player.getInventory().getItem(ix);
               if (it.is((Item)SystemItems.HUNTER_BESTIARY.value())) {
                  if (++bCount > 1) {
                     player.getInventory().setItem(ix, ItemStack.EMPTY);
                  } else if (it.getCount() > 1) {
                     it.setCount(1);
                  }
               }
            }
         }

         if (player.getPersistentData().contains("UmaBitterJuiceTicks")) {
            int bTicks = player.getPersistentData().getInt("UmaBitterJuiceTicks");
            if (bTicks > 1) {
               player.getPersistentData().putInt("UmaBitterJuiceTicks", bTicks - 1);
            } else {
               player.getPersistentData().remove("UmaBitterJuiceTicks");
               player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 1));
               player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
               player.displayClientMessage(Component.translatable("awakening_system.item.bitter_juice.expired"), true);
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 0.7F);
            }
         }

         boolean wearingWeights = SystemItems.ANKLE_WEIGHTS != null
            && SystemItems.ANKLE_WEIGHTS.isBound()
            && player.getItemBySlot(EquipmentSlot.FEET).is((Item)SystemItems.ANKLE_WEIGHTS.value());
         boolean hadWeights = player.getPersistentData().getBoolean("UmaWoreAnkleWeights");
         if (wearingWeights) {
            if (!hadWeights) {
               player.getPersistentData().putBoolean("UmaWoreAnkleWeights", true);
            }

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false, false));
         } else if (hadWeights) {
            player.getPersistentData().remove("UmaWoreAnkleWeights");
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 3, false, true, true));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.2F, 1.4F);
            player.displayClientMessage(Component.translatable("awakening_system.item.ankle_weights.boost"), true);
         }

         if (player.tickCount % 20 == 0) {
            SystemAttributeEffects.apply(player);
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
         if (player.tickCount % 20 == 0 && player.isAlive()) {
            float bonusHp = data.getArtifactBonusHp();
            AttributeInstance maxHpAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null) {
               ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("awakening_system", "artifact_hp_bonus");
               AttributeModifier existing = maxHpAttr.getModifier(modifierId);
               if (bonusHp > 0.0F) {
                  if (existing == null || existing.amount() != bonusHp) {
                     maxHpAttr.removeModifier(modifierId);
                     maxHpAttr.addPermanentModifier(new AttributeModifier(modifierId, bonusHp, Operation.ADD_VALUE));
                  }
               } else if (existing != null) {
                  maxHpAttr.removeModifier(modifierId);
               }
            }

            if (data.hasEquippedLightCone()) {
               float energyRecharge = data.getLightConeBonusEnergyRecharge();
               if (energyRecharge > 0.0F && data.energy() < data.maxEnergy()) {
                  data.addEnergy((int)Math.ceil(2.0F * energyRecharge));
                  changed = true;
               }
            }
         }

         if (player.tickCount % 20 == 0 && player.isAlive() && !player.isSpectator() && data.welkinDays() > 0) {
            long currentDay = player.serverLevel().getDayTime() / 24000L;
            if (currentDay > data.lastWelkinClaimDay()) {
               Long lastOffered = WELKIN_OFFERED_DAYS.get(player.getUUID());
               if (lastOffered == null || lastOffered < currentDay) {
                  WELKIN_OFFERED_DAYS.put(player.getUUID(), currentDay);
                  int reward = data.isWelkinEnhanced() ? 135 : 90;
                  PacketDistributor.sendToPlayer(
                     player,
                     new OpenWelkinScreenPayload(data.welkinDays(), reward, true, data.isWelkinEnhanced(), data.welkinQuestStep(), data.welkinQuestProgress()),
                     new CustomPacketPayload[0]
                  );
               }
            }
         }

         boolean inTranceBefore = data.isInTrance();
         int comboBefore = data.comboCount();
         data.tickCombat();
         if (inTranceBefore != data.isInTrance() || comboBefore > 0 && data.comboCount() == 0) {
            changed = true;
         }

         if (data.getPhysiqueSkillCooldown() > 0) {
            data.tickPhysiqueSkillCooldown();
         }

         if (data.daoSkillCooldown() > 0) {
            data.tickDaoSkillCooldown();
         }

         if (data.tribulationShieldTicks() > 0) {
            data.tickTribulationShield();
         }

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
               if (data.getTribulationTicks() == 12 && remaining > 0) {
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.2F, 1.8F);
                  sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY() + 1.2, player.getZ(), 18, 0.35, 0.45, 0.35, 0.08);
                  player.displayClientMessage(Component.translatable("awakening_system.tribulation.sky_roars", new Object[]{currentStrike, total}), true);
               }

               if (data.getTribulationTicks() <= 0 && remaining > 0) {
                  sl.setWeatherParameters(0, 400, true, true);
                  LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create(sl);
                  if (bolt != null) {
                     bolt.moveTo(player.position());
                     bolt.setVisualOnly(true);
                     sl.addFreshEntity(bolt);
                  }

                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.CELESTIAL_TRIBULATION.value(), SoundSource.WEATHER, 3.5F, 1.0F
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.5F, 0.65F);
                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.8F, 1.2F
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4F, 0.6F);
                  sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
                  spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 3.8, 32, 0.16);
                  spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.2, 4.5, 4, 14);
                  sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(), 16, 0.6, 0.2, 0.6, 0.02);
                  if (targetRealm >= 6) {
                     sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.2, player.getZ(), 35, 0.8, 0.8, 0.8, 0.06);
                  }

                  player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 15, 0, false, false, false));

                  float baseDmg = switch (targetRealm) {
                     case 2 -> 6.0F;
                     case 3 -> 9.0F;
                     case 4 -> 13.0F;
                     case 5 -> 17.0F;
                     case 6 -> 22.0F;
                     case 7 -> 28.0F;
                     case 8 -> 35.0F;
                     case 9 -> 44.0F;
                     default -> 5.0F;
                  };
                  String phys = data.murimPhysique();
                  float reduction = 1.0F;
                  if ("celestial_dragon".equals(phys) || "pure_yang".equals(phys) || "primordial_chaos".equals(phys)) {
                     reduction -= 0.3F;
                  }

                  if (player.isShiftKeyDown() || player.getVehicle() instanceof MeditationSeatEntity) {
                     reduction -= 0.2F;
                  }

                  boolean hasTribShield = data.tribulationShieldTicks() > 0;
                  if (hasTribShield) {
                     reduction -= 0.7F;
                  }

                  float finalDmg = Math.max(1.0F, baseDmg * Math.max(0.1F, reduction));
                  if (hasTribShield && finalDmg >= player.getHealth()) {
                     finalDmg = Math.max(0.5F, player.getHealth() - 1.0F);
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 45, 0.6, 0.6, 0.6, 0.15);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                     player.displayClientMessage(Component.translatable("awakening_system.tribulation.shield_dispersed"), true);
                  }

                  player.hurt(player.damageSources().lightningBolt(), finalDmg);
                  player.sendSystemMessage(Component.translatable("message.awakening_system.tribulation_strike", new Object[]{currentStrike, total, finalDmg}));
                  Component shieldFeedback = hasTribShield
                     ? Component.translatable("awakening_system.tribulation.shield_tag")
                     : (reduction < 1.0F ? Component.translatable("awakening_system.tribulation.stance_tag") : Component.empty());
                  player.displayClientMessage(
                     Component.translatable(
                        "awakening_system.tribulation.strike_damage",
                        new Object[]{currentStrike, total, String.format(Locale.US, "%.1f", finalDmg), shieldFeedback}
                     ),
                     true
                  );
                  data.decrementTribulationStrikes();
                  if (data.getTribulationStrikesRemaining() > 0) {
                     data.resetTribulationTicks(25);
                  } else {
                     data.clearTribulation();
                     data.setDantianRealm(targetRealm);
                     data.setCultivationSubLevel(1);
                     data.setCultivationQi(0);
                     sl.setWeatherParameters(6000, 0, false, false);
                     player.removeAllEffects();
                     player.setHealth(player.getMaxHealth());
                     data.restoreEnergy(data.maxEnergy());
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 2.5F, 1.0F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
                     spawnGeometricMagicCircle(
                        sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.5
                     );
                     spawnMurimCircleGround(sl, player, 7.5F, 120, -10496);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 8, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 140, 1.8, 1.8, 1.8, 0.35);
                     sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 90, 1.5, 1.8, 1.5, 0.1);
                     sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0, player.getZ(), 80, 1.5, 1.4, 1.5, 0.1);
                     player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.awakening_system.ascent_title")));
                     player.connection
                        .send(
                           new ClientboundSetSubtitleTextPacket(
                              Component.translatable("title.awakening_system.ascent_subtitle", new Object[]{getDantianRealmComponent(targetRealm)})
                           )
                        );
                     player.sendSystemMessage(
                        Component.translatable("message.awakening_system.tribulation_success", new Object[]{getDantianRealmComponent(targetRealm)})
                     );
                     if (targetRealm >= 7) {
                        Component broadMsg = Component.translatable(
                           "awakening_system.cultivation.ascension_broadcast", new Object[]{player.getScoreboardName(), getDantianRealmComponent(targetRealm)}
                        );

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
            for (int ixx = 0; ixx < player.getInventory().getContainerSize(); ixx++) {
               ItemStack st = player.getInventory().getItem(ixx);
               if (!st.isEmpty()
                  && (st.is(Items.NETHERITE_INGOT) || st.is(Items.NETHERITE_SWORD) || st.is(Items.NETHERITE_CHESTPLATE) || st.is(Items.NETHERITE_PICKAXE))) {
                  grantFable(player, "first_netherite");
                  break;
               }
            }
         }

         if (player.tickCount % 8 == 0) {
            ServerLevel pLevel = player.serverLevel();
            String masterTag = "shadow_master_" + player.getUUID();

            for (Mob soldier : pLevel.getEntitiesOfClass(
               Mob.class, player.getBoundingBox().inflate(28.0), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
            )) {
               if (!soldier.hasEffect(MobEffects.GLOWING)) {
                  soldier.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));
               }

               try {
                  Scoreboard sb = pLevel.getScoreboard();
                  PlayerTeam team = sb.getPlayerTeam("shadows");
                  if (team == null) {
                     team = sb.addPlayerTeam("shadows");
                     team.setColor(ChatFormatting.AQUA);
                     team.setDisplayName(Component.translatable("team.awakening_system.shadows"));
                  }

                  String member = soldier.getScoreboardName();
                  if (!team.getPlayers().contains(member)) {
                     sb.addPlayerToTeam(member, team);
                  }
               } catch (Exception var31) {
                  AwakeningSystemMod.LOGGER.debug("Error assigning shadow soldier to team: {}", var31.getMessage(), var31);
               }

               if (soldier.tickCount <= 60 && soldier.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                  pLevel.sendParticles(ParticleTypes.SQUID_INK, soldier.getX(), soldier.getY() + 0.05, soldier.getZ(), 8, 0.4, 0.02, 0.4, 0.01);
                  pLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.05, soldier.getZ(), 6, 0.35, 0.02, 0.35, 0.01);
               } else {
                  pLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getEyeY() - 0.05, soldier.getZ(), 2, 0.08, 0.04, 0.08, 0.005);
                  pLevel.sendParticles(
                     ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + soldier.getBbHeight() * 0.45, soldier.getZ(), 1, 0.15, 0.2, 0.15, 0.01
                  );
                  pLevel.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.15, soldier.getZ(), 2, 0.15, 0.1, 0.15, 0.01);
               }
            }
         }

         if (data.isSystemUpdating()) {
            data.decrementSystemUpdateTicks();
            changed = true;
            if (data.systemUpdateRemainingTicks() <= 0) {
               IsekaiWorldManager.completeSystemUpdate(player, data);
            }
         }

         if (data.isSystem30Updating()) {
            data.decrementSystem30UpdateTicks();
            changed = true;
            if (data.system30UpdateRemainingTicks() <= 0) {
               MurimWorldManager.completeSystem30Update(player, data);
            }
         }

         if (data.isSystem40Updating()) {
            data.decrementSystem40UpdateTicks();
            changed = true;
            if (data.system40UpdateRemainingTicks() <= 0) {
               TeyvatWorldManager.completeSystem40Update(player, data);
            }
         }

         if (player.tickCount % 40 == 0) {
            ServerLevel slevel = player.serverLevel();
            if (slevel.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) || slevel.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
               DungeonManager.syncRiftsToPlayer(player);
            }

            if (GreatTowerWorldManager.isGreatTowerDimension(player.serverLevel())) {
               GreatTowerWorldManager.syncBorderToPlayer(player);
            }
         }

         if (player.serverLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) && player.tickCount % 4 == 0) {
            BlockPos gatePos = YggdrasilTreeGenerator.ASCENSION_GATE_POS;
            double distGateSq = player.distanceToSqr(gatePos.getX() + 0.5, gatePos.getY() + 0.5, gatePos.getZ() + 0.5);
            if (distGateSq <= 80.0 && !player.getPersistentData().getBoolean("awakening_discovered_yggdrasil_gate")) {
               player.getPersistentData().putBoolean("awakening_discovered_yggdrasil_gate", true);
               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.discovery.yggdrasil_gate.title")));
               player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.discovery.yggdrasil_gate.subtitle")));
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
               player.serverLevel()
                  .sendParticles(ParticleTypes.TOTEM_OF_UNDYING, gatePos.getX() + 0.5, gatePos.getY() + 2.0, gatePos.getZ() + 0.5, 80, 2.0, 1.5, 2.0, 0.3);
               player.serverLevel()
                  .sendParticles(ParticleTypes.END_ROD, gatePos.getX() + 0.5, gatePos.getY() + 2.0, gatePos.getZ() + 0.5, 40, 1.5, 1.5, 1.5, 0.05);
            }

            if (distGateSq <= 3.5 && !player.isOnPortalCooldown()) {
               player.setPortalCooldown();
               MurimWorldManager.teleportToMurim(player);
            } else if (player.getServer() != null) {
               SeriesStructureData sData = SeriesStructureData.get(player.getServer());

               for (Entry<String, BlockPos> entry : sData.getAll().entrySet()) {
                  if (entry.getKey().startsWith("ascension_pavilion")) {
                     BlockPos pavPos = entry.getValue().above(1);
                     if (player.distanceToSqr(pavPos.getX() + 0.5, pavPos.getY() + 1.0, pavPos.getZ() + 0.5) <= 6.5) {
                        BlockPos summit = YggdrasilTreeGenerator.APEX_SUMMIT_POS;
                        player.teleportTo(player.serverLevel(), summit.getX() + 0.5, summit.getY() + 1.0, summit.getZ() + 0.5, 0.0F, 0.0F);
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0, false, false));
                        player.serverLevel()
                           .sendParticles(ParticleTypes.FLASH, summit.getX() + 0.5, summit.getY() + 1.5, summit.getZ() + 0.5, 10, 0.0, 0.0, 0.0, 0.0);
                        player.serverLevel()
                           .sendParticles(ParticleTypes.END_ROD, summit.getX() + 0.5, summit.getY() + 1.0, summit.getZ() + 0.5, 50, 1.2, 1.2, 1.2, 0.05);
                        player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.0F);
                        player.displayClientMessage(Component.translatable("awakening_system.action.yggdrasil_summit"), true);
                        break;
                     }
                  }
               }
            }
         }

         if (player.serverLevel().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD) && player.tickCount % 4 == 0) {
            BlockPos gatePosx = YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS;
            double gateY = gatePosx.getY() + 1.0;
            double distSq = player.distanceToSqr(gatePosx.getX() + 0.5, gateY, gatePosx.getZ() + 0.5);
            if (distSq <= 6.5) {
               MinecraftServer srv = player.getServer();
               if (srv != null) {
                  ServerLevel overworld = srv.getLevel(Level.OVERWORLD);
                  if (overworld != null) {
                     BlockPos respawn = player.getRespawnPosition();
                     if (respawn == null) {
                        respawn = overworld.getSharedSpawnPos();
                     }

                     player.teleportTo(overworld, respawn.getX() + 0.5, respawn.getY() + 1.0, respawn.getZ() + 0.5, player.getYRot(), 0.0F);
                     overworld.sendParticles(ParticleTypes.PORTAL, respawn.getX() + 0.5, respawn.getY() + 1.5, respawn.getZ() + 0.5, 60, 1.0, 1.0, 1.0, 0.1);
                     player.playNotifySound((SoundEvent)ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                     player.displayClientMessage(Component.translatable("awakening_system.tower.returned_to_overworld"), true);
                  }
               }
            }
         }

         if (player.serverLevel().dimension().equals(MurimWorldManager.MURIM_WORLD) && player.tickCount % 4 == 0) {
            double returnY = 237.0;
            double distSq = player.distanceToSqr(0.5, returnY, 14.0);
            if (distSq <= 5.0 && !player.isOnPortalCooldown()) {
               MinecraftServer srv = player.getServer();
               if (srv != null) {
                  ServerLevel isekai = srv.getLevel(IsekaiWorldManager.ISEKAI_WORLD);
                  if (isekai == null) {
                     isekai = srv.overworld();
                  }

                  player.setPortalCooldown();
                  BlockPos target = YggdrasilTreeGenerator.ASCENSION_GATE_POS.offset(0, 0, 2);
                  player.teleportTo(isekai, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 180.0F, 0.0F);
                  player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
                  player.playNotifySound((SoundEvent)ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                  player.displayClientMessage(Component.translatable("awakening_system.murim.returned_to_isekai"), true);
               }
            }
         }

         if (player.tickCount % 20 == 0 && (player.isInWater() || player.isInWaterOrRain())) {
            ServerLevel pLevel = player.serverLevel();
            BlockPos pPos = player.blockPosition();
            boolean nearGoddessAltar = false;

            for (BlockPos testPos : BlockPos.betweenClosed(pPos.offset(-7, -2, -7), pPos.offset(7, 2, 7))) {
               if (SystemBlocks.GODDESS_ALTAR.isBound() && pLevel.getBlockState(testPos).is((Block)SystemBlocks.GODDESS_ALTAR.get())) {
                  nearGoddessAltar = true;
                  break;
               }
            }

            if (nearGoddessAltar) {
               player.removeEffect(MobEffects.POISON);
               player.removeEffect(MobEffects.WITHER);
               player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
               player.removeEffect(MobEffects.WEAKNESS);
               player.removeEffect(MobEffects.CONFUSION);
               player.removeEffect(MobEffects.BLINDNESS);
               if (player.getHealth() < player.getMaxHealth()) {
                  player.heal(1.5F);
               }

               if (data.restoreEnergy(3)) {
                  changed = true;
               }

               pLevel.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0.01);
               pLevel.sendParticles(ParticleTypes.BUBBLE_POP, player.getX(), player.getY() + 0.3, player.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
            }
         }

         if (player.tickCount % 20 == 0
            && (player.isInWater() || player.isInWaterOrRain())
            && player.serverLevel().dimension().equals(MurimWorldManager.MURIM_WORLD)
            && player.getServer() != null) {
            SeriesStructureData sData = SeriesStructureData.get(player.getServer());

            for (Entry<String, BlockPos> entryx : sData.getAll().entrySet()) {
               if (entryx.getKey().startsWith("spirit_valley")) {
                  BlockPos vPos = entryx.getValue();
                  if (player.distanceToSqr(vPos.getX() + 0.5, vPos.getY() + 1.0, vPos.getZ() + 0.5) <= 256.0) {
                     if (player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0F);
                     }

                     player.serverLevel()
                        .sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0.02);
                     player.serverLevel()
                        .sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(), 2, 0.2, 0.1, 0.2, 0.01);
                     break;
                  }
               }
            }
         }

         if (player.tickCount % 40 == 0 && (data.dantianRealm() > 0 || data.getOpenedMeridiansCount() > 0)) {
            float zoneDensity = getZoneQiDensity(player);
            int openM = data.getOpenedMeridiansCount();
            int passiveQi = 1 + data.dantianRealm() / 2 + (openM >= 12 ? 3 : (openM >= 6 ? 2 : (openM > 0 ? 1 : 0)));
            if (data.dantianRealm() >= 2) {
               passiveQi++;
            }

            if (data.dantianRealm() >= 4) {
               passiveQi += 2;
            }

            passiveQi = Math.max(1, Math.round(passiveQi * Math.max(0.8F, zoneDensity) * data.getPhysiqueQiRegenMultiplier()));
            data.addCultivationQi(passiveQi);
            changed = true;
         }

         int regenTicks = (Integer)SystemConfig.ENERGY_REGEN_TICKS.get();
         int regenAmount = data.energyRegenRate();
         if (player.hasEffect(ModMobEffects.MANA_SATURATION)) {
            MobEffectInstance satEffect = player.getEffect(ModMobEffects.MANA_SATURATION);
            int amp = satEffect != null ? satEffect.getAmplifier() : 0;
            if (amp == 0) {
               regenTicks *= 2;
               regenAmount = Math.max(1, regenAmount / 2);
            } else if (amp == 1) {
               regenTicks *= 3;
               regenAmount = Math.max(1, (int)Math.round(regenAmount * 0.3));
            } else {
               regenTicks *= 4;
               regenAmount = 1;
            }
         }

         if (player.tickCount % Math.max(10, regenTicks) == 0 && data.restoreEnergy(regenAmount)) {
            changed = true;
         }

         if (player.tickCount % 40 == 0 && ManhwaArtifactItem.hasArtifact(player, ArtifactType.ASRAHAN_MANA_CHALICE) && data.restoreEnergy(1)) {
            changed = true;
         }

         boolean isLotusMounted = player.getVehicle() instanceof MeditationSeatEntity;
         boolean isSneakMeditating = !isLotusMounted && player.isShiftKeyDown() && player.getDeltaMovement().lengthSqr() < 0.005;
         if (!isLotusMounted && !isSneakMeditating) {
            player.getPersistentData().putInt("murim_meditation_ticks", 0);
         } else {
            int medTicks = player.getPersistentData().getInt("murim_meditation_ticks") + 1;
            player.getPersistentData().putInt("murim_meditation_ticks", medTicks);
            if (isLotusMounted && medTicks % 20 == 0 || !isLotusMounted && medTicks >= 40 && medTicks % 30 == 0) {
               int realm = data.dantianRealm();
               int subLevel = data.cultivationSubLevel();
               int openMeridians = data.getOpenedMeridiansCount();
               int qiGain = 4 + realm * 4 + subLevel * 2 + openMeridians * 3;
               if (isLotusMounted) {
                  qiGain += 4;
               }

               double dantianMult = 1.15;
               if (realm >= 2) {
                  dantianMult += 0.3;
               }

               if (realm >= 4) {
                  dantianMult += 0.4;
               }

               if (openMeridians == 12) {
                  dantianMult *= 1.6;
               } else if (openMeridians >= 6) {
                  dantianMult *= 1.3;
               }

               qiGain = (int)Math.round(qiGain * dantianMult);
               if ("ranker_of_the_tower".equals(data.equippedTitle()) || "high_ranker_of_the_tower".equals(data.equippedTitle())) {
                  qiGain = (int)Math.round(qiGain * 1.5);
               }

               if (data.systemUpdated()) {
                  qiGain = (int)Math.round(qiGain * 1.5);
               }

               if (data.system30Updated()) {
                  qiGain = (int)Math.round(qiGain * 1.8);
               }

               if (player.serverLevel().dimension().equals(SubspaceManager.SUBSPACE_REALM)) {
                  double subMult = 1.0 + data.subspaceLevel() * 0.25;
                  qiGain = (int)Math.round(qiGain * subMult);
               }

               float zoneDensityx = getZoneQiDensity(player);
               qiGain = Math.max(1, Math.round(qiGain * zoneDensityx * data.getPhysiqueQiRegenMultiplier()));
               data.addCultivationQi(qiGain);
               float healGain = 2.0F + realm * 1.0F + openMeridians * 0.4F;
               if (realm >= 2) {
                  healGain *= 1.5F;
               }

               if (data.isMeridianOpen(2)) {
                  healGain *= 1.3F;
               }

               if (data.isMeridianOpen(4)) {
                  healGain *= 1.3F;
               }

               if (data.isMeridianOpen(7)) {
                  healGain *= 1.3F;
               }

               player.heal(healGain);
               if (openMeridians == 12 && medTicks % 80 == 0) {
                  player.removeEffect(MobEffects.POISON);
                  player.removeEffect(MobEffects.WITHER);
                  player.removeEffect(MobEffects.CONFUSION);
               }

               changed = true;
               if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                  data.setCultivationSubLevel(subLevel + 1);
                  data.setCultivationQi(0);
                  player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                  player.displayClientMessage(
                     Component.translatable(
                        "awakening_system.cultivation.sublevel_success", new Object[]{data.cultivationSubLevel(), getDantianRealmComponent(realm)}
                     ),
                     true
                  );
               } else {
                  player.displayClientMessage(
                     Component.translatable(
                        "awakening_system.cultivation.action_bar",
                        new Object[]{data.cultivationQi(), data.maxCultivationQi(), qiGain, getDantianRealmComponent(realm), data.cultivationSubLevel()}
                     ),
                     true
                  );
               }

               if (player.level() instanceof ServerLevel slx) {
                  double angle = medTicks * 0.1;
                  double rx = Math.cos(angle) * 0.85;
                  double rz = Math.sin(angle) * 0.85;
                  double particleY = isLotusMounted ? player.getY() + 0.82 : player.getY() + 0.25;
                  slx.sendParticles(ParticleTypes.END_ROD, player.getX() + rx, particleY, player.getZ() + rz, 2, 0.0, 0.04, 0.0, 0.01);
                  slx.sendParticles(ParticleTypes.ENCHANT, player.getX(), particleY + 0.35, player.getZ(), 4, 0.35, 0.3, 0.35, 0.05);
                  if (medTicks % 60 == 0) {
                     slx.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.3F);
                  }

                  if (medTicks % 200 == 0) {
                     spawnMurimCircleGround(slx, player, 2.8F, 220, -11470213);
                  }
               }
            }
         }

         if ((isLotusMounted || isSneakMeditating || data.dantianRealm() > 0) && player.level() instanceof ServerLevel slAura) {
            renderCultivationQiAura(player, data, slAura, isLotusMounted || isSneakMeditating);
         }

         SetType currentFullSet = ManhwaArmorItem.getFullSet(player);
         if (currentFullSet == SetType.ABYSSAL) {
            boolean isUnderground = player.getY() <= 62.0 || !player.level().canSeeSky(player.blockPosition());
            if (isUnderground) {
               if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                  player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, true, false, true));
               }

               if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, true, false, true));
               }

               player.getPersistentData().putBoolean("abyssal_armor_underground", true);
            } else if (player.getPersistentData().getBoolean("abyssal_armor_underground")) {
               player.getPersistentData().remove("abyssal_armor_underground");
               player.removeEffect(MobEffects.NIGHT_VISION);
               player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
         } else if (player.getPersistentData().getBoolean("abyssal_armor_underground")) {
            player.getPersistentData().remove("abyssal_armor_underground");
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
         }

         if (currentFullSet == SetType.MITHRIL) {
            if (!player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
               player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, true, false, false));
            }

            player.getPersistentData().putBoolean("had_mithril_set", true);
            if (player.isShiftKeyDown() && !player.onGround() && player.getDeltaMovement().y < -0.02) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 15, 0, false, false, false));
               player.resetFallDistance();
               if (player.tickCount % 4 == 0 && player.level() instanceof ServerLevel slx) {
                  slx.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.1, 0.05, 0.1, 0.01);
               }
            }
         } else if (player.getPersistentData().getBoolean("had_mithril_set")) {
            player.getPersistentData().remove("had_mithril_set");
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
         }

         if (currentFullSet == SetType.ADAMANTITE) {
            player.clearFire();
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
               player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, true, false, true));
            }

            player.getPersistentData().putBoolean("had_adamantite_set", true);
            if (player.isInLava()) {
               if (player.tickCount % 20 == 0) {
                  player.heal(1.0F);
               }

               player.setDeltaMovement(player.getDeltaMovement().scale(1.12));
               if (player.tickCount % 6 == 0 && player.level() instanceof ServerLevel slx) {
                  slx.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 2, 0.2, 0.1, 0.2, 0.02);
               }
            }
         } else if (player.getPersistentData().getBoolean("had_adamantite_set")) {
            player.getPersistentData().remove("had_adamantite_set");
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
         }

         if (currentFullSet == SetType.SPIRIT_JADE) {
            if (player.tickCount % 20 == 0) {
               player.removeEffect(MobEffects.POISON);
               player.removeEffect(MobEffects.WITHER);
               player.removeEffect(MobEffects.CONFUSION);
               player.removeEffect(MobEffects.BLINDNESS);
            }

            long bellUntil = player.getPersistentData().getLong("spirit_jade_bell_active_until");
            if (player.level().getGameTime() < bellUntil && player.level() instanceof ServerLevel slx) {
               double anglex = player.tickCount * 0.35;

               for (int ixxx = 0; ixxx < 4; ixxx++) {
                  double a = anglex + ixxx * (Math.PI / 2);
                  double bx = Math.cos(a) * 1.25;
                  double bz = Math.sin(a) * 1.25;
                  slx.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX() + bx, player.getY() + 0.6, player.getZ() + bz, 1, 0.0, 0.02, 0.0, 0.0);
                  slx.sendParticles(ParticleTypes.GLOW, player.getX() + bx, player.getY() + 1.2, player.getZ() + bz, 1, 0.0, 0.02, 0.0, 0.0);
               }
            }
         }

         boolean hasAstralSet = currentFullSet == SetType.ASTRAL;
         boolean hasCelestialSet = currentFullSet == SetType.CELESTIAL;
         boolean hasPrimordialSet = currentFullSet == SetType.PRIMORDIAL;
         boolean hasFlightArmor = hasCelestialSet || hasPrimordialSet;
         if (hasAstralSet) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, true, false, true));
            player.resetFallDistance();
            if (player.tickCount % 6 == 0 && player.level() instanceof ServerLevel slx) {
               slx.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, -0.02);
               slx.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.15, 0.05, 0.15, 0.01);
            }

            player.getPersistentData().putBoolean("had_astral_set", true);
         } else if (player.getPersistentData().getBoolean("had_astral_set")) {
            player.getPersistentData().remove("had_astral_set");
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.JUMP);
         }

         if (hasFlightArmor) {
            if (!player.getAbilities().mayfly) {
               player.getAbilities().mayfly = true;
               player.onUpdateAbilities();
               player.getPersistentData().putBoolean("had_astral_flight", true);
            }

            if (player.getAbilities().flying) {
               player.resetFallDistance();
               if (player.tickCount % 3 == 0 && player.level() instanceof ServerLevel slx) {
                  if (hasPrimordialSet) {
                     slx.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.1, player.getZ(), 3, 0.2, 0.05, 0.2, -0.05);
                     slx.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.15, 0.05, 0.15, 0.02);
                  } else if (hasCelestialSet) {
                     slx.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, 0.02);
                     slx.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.15, 0.05, 0.15, 0.02);
                  }
               }
            }

            if (hasPrimordialSet && player.tickCount % 10 == 0) {
               player.removeEffect(MobEffects.WITHER);
               player.removeEffect(MobEffects.POISON);
               player.removeEffect(MobEffects.DARKNESS);
               player.removeEffect(MobEffects.BLINDNESS);
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

         if (hasCelestialSet) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, true));
            player.getPersistentData().putBoolean("had_celestial_set", true);
         } else if (player.getPersistentData().getBoolean("had_celestial_set")) {
            player.getPersistentData().remove("had_celestial_set");
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
         }

         if ((hasFlightArmor || hasAstralSet) && player.getY() < -50.0) {
            int highestY = player.level().getHeight(Types.MOTION_BLOCKING, (int)player.getX(), (int)player.getZ());
            double targetY = Math.max(80.0, highestY + 2.0);
            player.teleportTo(player.serverLevel(), player.getX(), targetY, player.getZ(), player.getYRot(), player.getXRot());
            player.setDeltaMovement(0.0, 0.2, 0.0);
            player.resetFallDistance();
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.5F);
            if (player.level() instanceof ServerLevel slxx) {
               slxx.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
               slxx.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 50, 1.0, 1.0, 1.0, 0.1);
            }

            AwakeningSystemMod.notify(
               player, "awakening_system.notification.astral_rescue.title", "awakening_system.notification.astral_rescue.desc", -43521, new Object[0]
            );
         }

         if (data.isDokkaebiScenarioActive()) {
            data.tickDokkaebiTimer();
            changed = true;
            if (data.dokkaebiScenarioTimer() <= 0) {
               int target = data.dokkaebiScenarioTargetKills();
               if (data.dokkaebiScenarioKills() >= target) {
                  int extraKills = Math.max(0, data.dokkaebiScenarioKills() - target);
                  SystemEvents.DokkaebiOverkillReward overkill = calculateDokkaebiOverkill(extraKills);
                  int baseCoins = getDokkaebiBaseCoins(target);
                  int baseExp = getDokkaebiBaseExp(target);
                  int totalCoins = baseCoins + overkill.bonusCoins + overkill.milestoneDonation;
                  int totalExp = baseExp + overkill.bonusExp;
                  data.addCoins(totalCoins);
                  awardExperience(player, data, totalExp);
                  if (!overkill.crystalGift.isEmpty() && !player.getInventory().add(overkill.crystalGift.copy())) {
                     player.drop(overkill.crystalGift.copy(), false);
                  }

                  if (extraKills > 0) {
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.dokkaebi_overkill.title",
                        "awakening_system.notification.dokkaebi_overkill.desc",
                        -10496,
                        new Object[]{extraKills, totalCoins, totalExp}
                     );
                     String donationStr = overkill.milestoneDonation > 0 ? " + " + overkill.milestoneDonation : "";
                     String giftMsg = !overkill.crystalGift.isEmpty() ? " §b[+1 " + overkill.crystalGift.getHoverName().getString() + "]" : "";
                     player.sendSystemMessage(
                        Component.translatable(
                           "awakening_system.dokkaebi.finish_overkill",
                           new Object[]{overkill.rankName.getString(), totalCoins, baseCoins, overkill.bonusCoins, donationStr, totalExp, giftMsg}
                        )
                     );
                     player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                  } else {
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.dokkaebi_success.title",
                        "awakening_system.notification.dokkaebi_success.desc",
                        -10496,
                        new Object[]{totalCoins, totalExp}
                     );
                     player.sendSystemMessage(Component.translatable("awakening_system.dokkaebi.finish_standard", new Object[]{totalCoins, totalExp}));
                     player.displayClientMessage(Component.translatable("awakening_system.action.constellations_cheer_coins"), true);
                  }

                  player.level()
                     .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                  if (player.level() instanceof ServerLevel slxx) {
                     slxx.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.5, player.getZ(), 40 + extraKills * 5, 0.6, 0.8, 0.6, 0.15);
                  }
               } else {
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.dokkaebi_failed.title", "awakening_system.notification.dokkaebi_failed.desc", -43691, new Object[0]
                  );
                  player.sendSystemMessage(
                     Component.translatable("awakening_system.dokkaebi.finish_failed", new Object[]{data.dokkaebiScenarioKills(), target})
                  );
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1));
                  player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
                  player.hurt(player.damageSources().lightningBolt(), 6.0F);
                  if (player.level() instanceof ServerLevel slxx) {
                     slxx.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 50, 1.0, 1.0, 1.0, 0.2);
                  }
               }

               data.stopDokkaebiScenario();
            }
         }

         if (player.tickCount % 20 != 0) {
            if (changed) {
               AwakeningSystemMod.sync(player);
            }
         } else {
            tickStructureAmbientEffects(player);
            if (player.tickCount > 0 && player.tickCount % 36000 == 0) {
               long gameTime = player.serverLevel().getGameTime();
               long nextAllowed = player.getPersistentData().getLong("next_wild_dokkaebi_time");
               if (gameTime >= nextAllowed
                  && player.getRandom().nextFloat() < 0.25F
                  && isDokkaebiAllowedInDimension(player.serverLevel().dimension())
                  && !data.isDokkaebiScenarioActive()) {
                  spawnDokkaebiNearPlayer(player);
                  player.getPersistentData().putLong("next_wild_dokkaebi_time", gameTime + 72000L);
               }
            }

            if (data.comboCount() >= 6 && data.isDokkaebiScenarioActive()) {
               AABB searchBox = player.getBoundingBox().inflate(64.0);

               for (DokkaebiEntity d : player.serverLevel()
                  .getEntitiesOfClass(
                     DokkaebiEntity.class,
                     searchBox,
                     dx -> "combo_strike".equals(dx.getQuestType()) && dx.isQuestActive() && dx.getQuestProgress() < dx.getQuestTarget()
                  )) {
                  d.addProgress(6, player);
               }
            }

            if (player.tickCount % 12000 == 0) {
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

            if (data.isSkillEnabled("nano_healing")
               && player.tickCount % 40 == 0
               && player.tickCount - player.getLastHurtByMobTimestamp() > 80
               && player.getHealth() < player.getMaxHealth()) {
               player.heal(1.0F);
               if (player.level() instanceof ServerLevel slxx) {
                  slxx.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 0.8, player.getZ(), 4, 0.2, 0.3, 0.2, 0.05);
               }
            }

            if (data.isSkillEnabled("dragon_sense")
               && (player.level().getRawBrightness(player.blockPosition(), 0) < 8 || player.getY() < 40.0 || DungeonManager.isInDungeon(player.getUUID()))) {
               player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false, false));
            }

            if (player.getY() < 32.0) {
               if (data.isSkillEnabled("deep_dark_resonance")) {
                  int level = (Integer)data.skills().get("deep_dark_resonance");
                  player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, false, false, true));
                  if (level >= 6) {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, true));
                  }

                  changed |= gainSkillProgress(player, data, "deep_dark_resonance");
               }

               changed |= updateDailyProgress(player, data, player.level().getDayTime() / 24000L, 4, 1);
               if (data.completeDaily(player.level().getGameTime())) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.daily_complete.title",
                     "awakening_system.notification.daily_complete.message",
                     -9377537,
                     new Object[0]
                  );
                  changed = true;
               }

               boolean before = data.skills().containsKey("deep_breath");
               int gained = data.learn("depth", 1);
               changed |= gained > 0;
               Integer skillLevel = data.isSkillEnabled("deep_breath") ? (Integer)data.skills().get("deep_breath") : null;
               if (skillLevel != null && player.isUnderWater()) {
                  player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false, true));
                  if (skillLevel >= 6) {
                     player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false, true));
                  }

                  if (data.canAdvanceSkill("deep_breath") && data.gainSkillProgress("deep_breath", 1)) {
                     announceSkillLevel(player, "deep_breath", (Integer)data.skills().get("deep_breath"));
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
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.daily_complete.title",
                     "awakening_system.notification.daily_complete.message",
                     -9377537,
                     new Object[0]
                  );
                  changed = true;
               }

               if (data.isSkillEnabled("aether_ascension")) {
                  int level = (Integer)data.skills().get("aether_ascension");
                  player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, level >= 6 ? 1 : 0, false, false, true));
                  changed |= gainSkillProgress(player, data, "aether_ascension");
               }

               boolean beforex = data.skills().containsKey("windrunner");
               int gainedx = data.learn("mobility", 1);
               changed |= gainedx > 0;
               Integer skillLevelx = data.isSkillEnabled("windrunner") ? (Integer)data.skills().get("windrunner") : null;
               if (skillLevelx != null) {
                  boolean evolved = skillLevelx >= 6;
                  if (evolved) {
                     player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, false, false, true));
                  }

                  if (data.canAdvanceSkill("windrunner") && data.gainSkillProgress("windrunner", 1)) {
                     announceSkillLevel(player, "windrunner", (Integer)data.skills().get("windrunner"));
                     changed = true;
                  }
               }

               if (!beforex && data.skills().containsKey("windrunner")) {
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
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.emergency_complete.title",
                        "awakening_system.notification.emergency_complete.message",
                        -35226,
                        new Object[0]
                     );
                  }
               }

               changed = true;
            }

            if (changed) {
               AwakeningSystemMod.sync(player);
            }
         }
      }
   }

   private static void checkVipZoneBarrier(ServerPlayer player) {
      if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
         if (player.tickCount % 10 == 0) {
            if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
               BlockPos pos = player.blockPosition();
               if (TeyvatCasinoBuilder.isInsideVipZone(player.level(), pos) && !CasinoVipGateBlock.hasPlayerVipAccess(player)) {
                  ServerLevel sl = player.serverLevel();
                  TeyvatFeaturesData tData = TeyvatFeaturesData.get(sl.getServer());
                  double pushX = 0.0;
                  double pushZ = 1.2;
                  if (tData != null) {
                     List<BlockPos> origins = new ArrayList<>(tData.casinos);
                     BlockPos named = tData.getNamedStructure("casino_origin");
                     if (named != null && !origins.contains(named)) {
                        origins.add(named);
                     }

                     if (origins.isEmpty()) {
                        origins.add(TeyvatCasinoBuilder.CASINO_POS);
                     }

                     for (BlockPos orig : origins) {
                        if (pos.closerThan(orig, 60.0)) {
                           if (pos.getX() > orig.getX() + 14) {
                              pushX = -1.2;
                              pushZ = 0.0;
                           }

                           if (pos.getZ() < orig.getZ() - 15) {
                              pushZ = 1.2;
                           }
                           break;
                        }
                     }
                  }

                  player.setDeltaMovement(pushX, 0.25, pushZ);
                  player.hurtMarked = true;
                  sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.5, 0.4, 0.1);
                  sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.8, player.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                  player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 0.9F);
                  player.displayClientMessage(Component.translatable("awakening_system.casino.vip_barrier"), true);
               }
            }
         }
      }
   }

   public static boolean evolveSkill(ServerPlayer player, String skillId) {
      if (!SkillDefinitions.isAvailable(skillId)) {
         return false;
      } else {
         SystemData data = data(player);
         if (!data.evolveSkill(skillId, player.level().getGameTime())) {
            return false;
         } else {
            Definition skill = SkillDefinitions.get(skillId);
            if (skill != null) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.evolved.title", "awakening_system.notification.evolved.message", -10138, new Object[]{skill.nameKey()}
               );
            }

            if (data.completeEvolutionMission()) {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.evolution_complete.title",
                  "awakening_system.notification.evolution_complete_narrative.message",
                  -10138,
                  new Object[0]
               );
            }

            completeClassMission(player, data);
            AwakeningSystemMod.sync(player);
            return true;
         }
      }
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
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.shop.error_title", "awakening_system.notification.shop.insufficient", -35226, new Object[0]
            );
            return false;
         } else {
            data.recordPurchase(cost);
            checkAchievements(player, data);
            SystemEvents.BoxLootResult lootResult = giveRouletteLoot(player, data);
            spawnBoxFx(player, 5);
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.shop.title",
               "awakening_system.notification.box_opened_summary",
               -10496,
               new Object[]{lootResult.summary()}
            );
            PacketDistributor.sendToPlayer(
               player,
               new ChestRewardPayload(entryId, 5, lootResult.summary(), lootResult.coins(), lootResult.itemIds(), lootResult.itemCounts()),
               new CustomPacketPayload[0]
            );
            AwakeningSystemMod.sync(player);
            return true;
         }
      } else {
         com.soybe.awakeningsystem.ShopDefinitions.Entry entry = ShopDefinitions.get(entryId);
         if (entry != null && ShopDefinitions.isAvailable(entry)) {
            SystemData data = data(player);
            if (ShopDefinitions.isUnlocked(entry, data)) {
               if (!data.spendCoins(entry.cost())) {
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.shop.error_title", "awakening_system.notification.shop.insufficient", -35226, new Object[0]
                  );
                  return false;
               } else {
                  grantFable(player, "first_trade");
                  if (!"box_common".equals(entryId)
                     && !"box_rare".equals(entryId)
                     && !"box_epic".equals(entryId)
                     && !"box_legendary".equals(entryId)
                     && !"lucky_box".equals(entryId)) {
                     ItemStack stack = ShopDefinitions.createStack(entry);
                     if (stack.isEmpty()) {
                        data.addCoins(entry.cost());
                        AwakeningSystemMod.notify(
                           player, "awakening_system.notification.shop.error_title", "awakening_system.notification.shop.unavailable", -35226, new Object[0]
                        );
                        AwakeningSystemMod.sync(player);
                        return false;
                     } else {
                        if (!player.getInventory().add(stack)) {
                           player.drop(stack, false);
                        }

                        player.containerMenu.broadcastChanges();
                        data.recordPurchase(entry.cost());
                        checkAchievements(player, data);
                        AwakeningSystemMod.notify(
                           player,
                           "awakening_system.notification.shop.title",
                           "awakening_system.notification.shop.message",
                           -9377537,
                           new Object[]{entry.nameKey()}
                        );
                        AwakeningSystemMod.sync(player);
                        return true;
                     }
                  } else {
                     data.recordPurchase(entry.cost());
                     checkAchievements(player, data);

                     int tier = switch (entryId) {
                        case "box_rare" -> 2;
                        case "box_epic" -> 3;
                        case "box_legendary" -> 4;
                        default -> 1;
                     };
                     SystemEvents.BoxLootResult lootResult;
                     if ("lucky_box".equals(entryId)) {
                        lootResult = giveRandomLoot(player, data);
                     } else {
                        lootResult = giveBoxLoot(player, data, tier);
                     }

                     spawnBoxFx(player, tier);

                     int notifyColor = switch (tier) {
                        case 2 -> -11162881;
                        case 3 -> -3123713;
                        case 4 -> -22016;
                        case 5 -> -10496;
                        default -> "lucky_box".equals(entryId) ? -10138 : -2039584;
                     };
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.shop.title",
                        "awakening_system.notification.box_opened_summary",
                        notifyColor,
                        new Object[]{lootResult.summary()}
                     );
                     PacketDistributor.sendToPlayer(
                        player,
                        new ChestRewardPayload(entryId, tier, lootResult.summary(), lootResult.coins(), lootResult.itemIds(), lootResult.itemCounts()),
                        new CustomPacketPayload[0]
                     );
                     AwakeningSystemMod.sync(player);
                     return true;
                  }
               }
            } else {
               boolean lvlLock = entry.minLevel() > 0 && data.level() < entry.minLevel();
               boolean dimLock = !entry.requiredDimension().isEmpty() && !data.hasVisitedDimension(entry.requiredDimension());
               if (lvlLock && dimLock) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.shop.error_title",
                     "awakening_system.screen.shop.lock.dual_msg:::" + entry.minLevel() + ":::" + entry.requiredDimension(),
                     -35226,
                     new Object[0]
                  );
               } else if (lvlLock) {
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.shop.error_title",
                     "awakening_system.shop.level_required_msg:::" + entry.minLevel(),
                     -35226,
                     new Object[0]
                  );
               } else {
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.shop.error_title", "awakening_system.notification.shop.dimension_locked", -35226, new Object[0]
                  );
               }

               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean sellShopItem(ServerPlayer player, String entryId, boolean sellAll) {
      SellEntry entry = SellDefinitions.get(entryId);
      if (entry != null && SellDefinitions.isAvailable(entry)) {
         ResourceLocation itemRl = ResourceLocation.parse(entry.itemId());
         if (!BuiltInRegistries.ITEM.containsKey(itemRl)) {
            return false;
         } else {
            Item targetItem = (Item)BuiltInRegistries.ITEM.get(itemRl);
            int totalCount = 0;
            Inventory inventory = player.getInventory();

            for (ItemStack stack : inventory.items) {
               if (!stack.isEmpty() && stack.is(targetItem)) {
                  totalCount += stack.getCount();
               }
            }

            if (totalCount < entry.amount()) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.sell.error_title", "awakening_system.notification.sell.insufficient_items", -35226, new Object[0]
               );
               return false;
            } else {
               int toSell = sellAll ? totalCount / entry.amount() * entry.amount() : entry.amount();
               if (toSell <= 0) {
                  return false;
               } else {
                  int remainingToRemove = toSell;

                  for (int i = 0; i < inventory.items.size() && remainingToRemove > 0; i++) {
                     ItemStack stackx = (ItemStack)inventory.items.get(i);
                     if (!stackx.isEmpty() && stackx.is(targetItem)) {
                        int take = Math.min(stackx.getCount(), remainingToRemove);
                        stackx.shrink(take);
                        remainingToRemove -= take;
                        if (stackx.isEmpty()) {
                           inventory.items.set(i, ItemStack.EMPTY);
                        }
                     }
                  }

                  player.containerMenu.broadcastChanges();
                  int earnedCoins = toSell / entry.amount() * entry.unitPrice();
                  SystemData data = data(player);
                  data.addCoins(earnedCoins);
                  player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);
                  ItemStack sellStack = SellDefinitions.createStack(entry);
                  String soldItemName = !sellStack.isEmpty() ? sellStack.getHoverName().getString() : Component.translatable(entry.nameKey()).getString();
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.sell.title",
                     "awakening_system.notification.sell.success",
                     -11470213,
                     new Object[]{toSell + "x " + soldItemName + " (+" + earnedCoins + " ⛃)"}
                  );
                  AwakeningSystemMod.sync(player);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean handleBankTransaction(ServerPlayer player, String action, int amount) {
      if (player == null) {
         return false;
      } else {
         SystemData data = data(player);
         Item coinItem = (Item)SystemItems.AWAKENING_COIN.get();
         Inventory inventory = player.getInventory();
         if (!"deposit".equalsIgnoreCase(action)) {
            if ("withdraw".equalsIgnoreCase(action)) {
               if (amount <= 0) {
                  return false;
               } else if (data.coins() < amount) {
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.insufficient", -35226, new Object[]{amount}
                  );
                  return false;
               } else if (!data.spendCoins(amount)) {
                  return false;
               } else {
                  int toGive = amount;

                  while (toGive > 0) {
                     int batch = Math.min(toGive, 64);
                     ItemStack stack = new ItemStack(coinItem, batch);
                     if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                     }

                     toGive -= batch;
                  }

                  AwakeningSystemMod.sync(player);
                  player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.3F);
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.bank.title", "awakening_system.notification.bank.withdrawn", -10138, new Object[]{amount}
                  );
                  return true;
               }
            } else {
               return false;
            }
         } else {
            int totalPhysical = 0;

            for (ItemStack stack : inventory.items) {
               if (!stack.isEmpty() && stack.is(coinItem)) {
                  totalPhysical += stack.getCount();
               }
            }

            if (totalPhysical <= 0) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.bank.title_error", "awakening_system.notification.bank.no_coins", -35226, new Object[0]
               );
               return false;
            } else {
               int toDeposit = amount <= 0 ? totalPhysical : Math.min(amount, totalPhysical);
               int remaining = toDeposit;

               for (int i = 0; i < inventory.items.size() && remaining > 0; i++) {
                  ItemStack stackx = (ItemStack)inventory.items.get(i);
                  if (!stackx.isEmpty() && stackx.is(coinItem)) {
                     int take = Math.min(stackx.getCount(), remaining);
                     stackx.shrink(take);
                     remaining -= take;
                     if (stackx.isEmpty()) {
                        inventory.items.set(i, ItemStack.EMPTY);
                     }
                  }
               }

               data.addCoins(toDeposit);
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.bank.title",
                  "awakening_system.notification.bank.deposited",
                  -11470213,
                  new Object[]{toDeposit, data.coins()}
               );
               return true;
            }
         }
      }
   }

   private static void spawnBoxFx(ServerPlayer player, int tier) {
      if (player.level() instanceof ServerLevel serverLevel) {
         SimpleParticleType particle = switch (tier) {
            case 1 -> ParticleTypes.CRIT;
            case 2 -> ParticleTypes.ENCHANT;
            case 3 -> ParticleTypes.WITCH;
            case 4 -> ParticleTypes.TOTEM_OF_UNDYING;
            default -> ParticleTypes.TOTEM_OF_UNDYING;
         };
         serverLevel.sendParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.4, 0.5, 0.4, 0.2);
         serverLevel.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            tier >= 3 ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.9F,
            1.0F + tier * 0.1F
         );
      }
   }

   private static void addLootItem(ServerPlayer player, List<String> ids, List<Integer> counts, Item item, int count) {
      player.getInventory().add(new ItemStack(item, count));
      ids.add(BuiltInRegistries.ITEM.getKey(item).toString());
      counts.add(count);
   }

   private static SystemEvents.BoxLootResult giveRouletteLoot(ServerPlayer player, SystemData data) {
      int roll = player.getRandom().nextInt(100);
      List<String> ids = new ArrayList<>();
      List<Integer> counts = new ArrayList<>();
      int bonusCoins = roll % 3 == 0 ? 100 : (roll % 2 == 0 ? 50 : 0);
      if (bonusCoins > 0) {
         data.addCoins(bonusCoins);
      }

      if (roll < 22) {
         addLootItem(player, ids, counts, Items.ENDER_PEARL, 8);
         return new SystemEvents.BoxLootResult("Ender Pearls x8", bonusCoins, ids, counts);
      } else if (roll < 42) {
         addLootItem(player, ids, counts, Items.GOLDEN_APPLE, 3);
         return new SystemEvents.BoxLootResult("Golden Apples x3", bonusCoins, ids, counts);
      } else if (roll < 58) {
         addLootItem(player, ids, counts, Items.NETHERITE_SCRAP, 2);
         return new SystemEvents.BoxLootResult("Netherite Scraps x2", bonusCoins, ids, counts);
      } else if (roll < 72) {
         addLootItem(player, ids, counts, Items.DIAMOND_BLOCK, 1);
         return new SystemEvents.BoxLootResult("Diamond Block x1", bonusCoins, ids, counts);
      } else if (roll < 83) {
         addLootItem(player, ids, counts, Items.NETHER_STAR, 1);
         return new SystemEvents.BoxLootResult("Nether Star x1", bonusCoins, ids, counts);
      } else if (roll < 92) {
         addLootItem(player, ids, counts, Items.ENCHANTED_GOLDEN_APPLE, 1);
         return new SystemEvents.BoxLootResult("Enchanted Golden Apple x1", bonusCoins, ids, counts);
      } else if (roll < 97) {
         addLootItem(player, ids, counts, Items.TOTEM_OF_UNDYING, 1);
         return new SystemEvents.BoxLootResult("Totem of Undying x1", bonusCoins, ids, counts);
      } else {
         addLootItem(player, ids, counts, Items.NETHERITE_INGOT, 2);
         return new SystemEvents.BoxLootResult("Netherite Ingots x2", bonusCoins, ids, counts);
      }
   }

   private static SystemEvents.BoxLootResult giveBoxLoot(ServerPlayer player, SystemData data, int tier) {
      String boxId = switch (tier) {
         case 2 -> "box_rare";
         case 3 -> "box_epic";
         case 4 -> "box_legendary";
         default -> "box_common";
      };
      return CustomLootConfigManager.rollBoxLoot(boxId, player, player.getRandom());
   }

   private static SystemEvents.BoxLootResult giveRandomLoot(ServerPlayer player, SystemData data) {
      return CustomLootConfigManager.rollBoxLoot("lucky_box", player, player.getRandom());
   }

   public static boolean equipTitle(ServerPlayer player, String titleId) {
      SystemData data = data(player);
      if (titleId == null || titleId.isEmpty() || "none".equalsIgnoreCase(titleId)) {
         String previousTitle = data.equippedTitle();
         if (previousTitle != null && !previousTitle.isEmpty() && !"none".equalsIgnoreCase(previousTitle)) {
            MinecraftServer server = player.getServer();
            if (server != null && UniqueRankManager.isUniqueTitle(previousTitle)) {
               UniqueRankManager.get(server).relinquishTitle(previousTitle, player.getUUID());
            }

            data.setEquippedTitle("");
            SystemAttributeEffects.apply(player);
            player.displayClientMessage(Component.translatable("awakening_system.title.unequipped_msg"), true);
            AwakeningSystemMod.sync(player);
            return true;
         } else {
            return false;
         }
      } else if (!TitleDefinitions.isAvailable(titleId)) {
         return false;
      } else {
         MinecraftServer server = player.getServer();
         if (server != null) {
            UniqueRankManager uniqueMgr = UniqueRankManager.get(server);
            String previousTitle = data.equippedTitle();
            if (UniqueRankManager.isUniqueTitle(titleId)) {
               if (!uniqueMgr.canClaimTitle(titleId, player.getUUID())) {
                  String holder = uniqueMgr.getTitleHolderName(titleId);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.unique_locked.title",
                     "awakening_system.notification.unique_title_claimed",
                     -48060,
                     new Object[]{holder != null ? holder : "Another hunter"}
                  );
                  return false;
               }

               if (previousTitle != null && UniqueRankManager.isUniqueTitle(previousTitle) && !previousTitle.equals(titleId)) {
                  uniqueMgr.relinquishTitle(previousTitle, player.getUUID());
               }

               uniqueMgr.claimTitle(titleId, player);
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.unique_claimed.title", "awakening_system.notification.unique_title_success", -10496, new Object[0]
               );
            } else if (previousTitle != null && UniqueRankManager.isUniqueTitle(previousTitle)) {
               uniqueMgr.relinquishTitle(previousTitle, player.getUUID());
            }
         }

         if (!data.equipTitle(titleId)) {
            return false;
         } else {
            SystemAttributeEffects.apply(player);
            return true;
         }
      }
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
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.unique_locked.title",
                  "awakening_system.notification.unique_skill_claimed",
                  -48060,
                  new Object[]{holder != null ? holder : "Another hunter"}
               );
               return false;
            }

            if (previousSkill != null && UniqueRankManager.isUniqueSkill(previousSkill) && !previousSkill.equals(skillId)) {
               uniqueMgr.relinquishSkill(previousSkill, player.getUUID());
            }

            uniqueMgr.claimSkill(skillId, player);
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.unique_claimed.title", "awakening_system.notification.unique_skill_success", -10496, new Object[0]
            );
         } else if (previousSkill != null && UniqueRankManager.isUniqueSkill(previousSkill)) {
            uniqueMgr.relinquishSkill(previousSkill, player.getUUID());
         }
      }

      if (data.setEquippedSkill(skillId)) {
         AwakeningSystemMod.notify(
            player,
            "awakening_system.notification.skill_equipped.title",
            "awakening_system.notification.skill_equipped.message",
            -9377537,
            new Object[]{SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).nameKey() : skillId}
         );
         AwakeningSystemMod.sync(player);
         return true;
      } else {
         return false;
      }
   }

   public static boolean spendAttribute(ServerPlayer player, String attribute) {
      boolean spent = data(player).addAttribute(attribute);
      if (spent) {
         double oldMaxHealth = player.getMaxHealth();
         SystemAttributeEffects.apply(player);
         if ("vitality".equalsIgnoreCase(attribute) || "vitalidad".equalsIgnoreCase(attribute)) {
            player.setHealth((float)Math.min((double)player.getMaxHealth(), player.getHealth() + player.getMaxHealth() - oldMaxHealth));
         }

         AwakeningSystemMod.sync(player);
      }

      return spent;
   }

   private static void applyCompatibilityCombatSkills(ServerPlayer player, SystemData data) {
      if (data.skills().containsKey("beast_bond")) {
         int level = (Integer)data.skills().get("beast_bond");
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, Math.min(1, level / 4), false, false, true));
         gainSkillProgress(player, data, "beast_bond");
      }

      if (data.skills().containsKey("cataclysm_breaker") && player.level().dimension().location().getNamespace().equals("cataclysm")) {
         int level = (Integer)data.skills().get("cataclysm_breaker");
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, Math.min(1, level / 4), false, false, true));
         gainSkillProgress(player, data, "cataclysm_breaker");
      }
   }

   private static void applyCompatibilityDamageSkills(Pre event, ServerPlayer player, SystemData data) {
      if (data.skills().containsKey("cataclysm_breaker")
         && event.getEntity().getType().builtInRegistryHolder().key().location().getNamespace().equals("cataclysm")) {
         int level = (Integer)data.skills().get("cataclysm_breaker");
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
      } else {
         String skill = data.getEquippedActiveSkill(slot);
         if (skill != null && !skill.isEmpty()) {
            return activateSkill(player, skill);
         } else {
            player.displayClientMessage(Component.translatable("awakening_system.action.slot_empty_hint", new Object[]{slot + 1}), true);
            player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
            return false;
         }
      }
   }

   public static boolean activateWeaponSkill(ServerPlayer player, int handOrdinal) {
      if (player == null) {
         return false;
      } else {
         InteractionHand hand = handOrdinal == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack stack = player.getItemInHand(hand);
         if (!AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (AwakeningWeaponItem.isAwakeningWeapon(otherStack)) {
               hand = otherHand;
               stack = otherStack;
            }
         }

         if (AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            stack.use(player.level(), player, hand);
            player.swing(hand, true);
            return true;
         } else {
            return false;
         }
      }
   }

   public static void equipSkillSlot(ServerPlayer player, boolean isPassive, int slot, String skillId) {
      SystemData data = data(player);
      if (isPassive) {
         data.setEquippedPassiveSkill(slot, skillId);
         if (skillId != null && !skillId.isEmpty()) {
            Component sName = (Component)(SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).name() : Component.literal(skillId));
            player.displayClientMessage(Component.translatable("awakening_system.action.passive_assigned", new Object[]{slot + 1, sName}), true);
         } else {
            player.displayClientMessage(Component.translatable("awakening_system.action.passive_cleared", new Object[]{slot + 1}), true);
         }
      } else {
         data.setEquippedActiveSkill(slot, skillId);
         if (skillId != null && !skillId.isEmpty()) {
            Component sName = (Component)(SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).name() : Component.literal(skillId));
            player.displayClientMessage(Component.translatable("awakening_system.action.active_assigned", new Object[]{slot + 1, sName}), true);
         } else {
            player.displayClientMessage(Component.translatable("awakening_system.action.active_cleared", new Object[]{slot + 1}), true);
         }
      }

      player.playNotifySound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
      AwakeningSystemMod.sync(player);
   }

   public static boolean activateStigma(ServerPlayer player) {
      SystemData data = data(player);
      if (data.isAnySystemUpdating()) {
         player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
         player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
         return false;
      } else {
         String sponsor = data.sponsorConstellation();
         if (sponsor != null && !sponsor.isEmpty()) {
            long now = player.level().getGameTime();
            if (now < data.stigmaCooldownUntil()) {
               int secLeft = (int)((data.stigmaCooldownUntil() - now + 19L) / 20L);
               player.displayClientMessage(Component.translatable("awakening_system.action.stigma_cooldown", new Object[]{secLeft}), true);
               player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.6F, 1.0F);
               return false;
            } else {
               data.setStigmaCooldownUntil(now + 900L);
               ServerLevel sl = (ServerLevel)player.level();
               double px = player.getX();
               double py = player.getY();
               double pz = player.getZ();
               switch (sponsor) {
                  case "demon_like_judge_of_fire":
                     player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false, true));
                     spawnArcaneCircleGround(sl, player, 6.0F, 60, -52480);

                     for (int dy = 0; dy <= 16; dy += 2) {
                        sl.sendParticles(ParticleTypes.FLAME, px, py + dy, pz, 40, 1.8, 1.0, 1.8, 0.1);
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py + dy, pz, 20, 1.5, 0.8, 1.5, 0.08);
                     }

                     sl.sendParticles(ParticleTypes.FLASH, px, py + 2.0, pz, 3, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, px, py, pz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.3F, 1.0F);
                     sl.playSound(null, px, py, pz, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 1.2F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(12.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.setRemainingFireTicks(200);
                        e.hurt(player.damageSources().magic(), 45.0F);
                        e.setDeltaMovement(0.0, 0.8, 0.0);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.judge_flames"), true);
                     break;
                  case "prisoner_of_the_golden_headband":
                     player.setDeltaMovement(0.0, 1.6, 0.0);
                     player.hurtMarked = true;
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false, false));
                     spawnArcaneCircleGround(sl, player, 5.5F, 50, -10496);
                     sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 50, 1.2, 0.3, 1.2, 0.08);
                     sl.playSound(null, px, py, pz, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.7F);
                     sl.getServer()
                        .tell(
                           new TickTask(
                              sl.getServer().getTickCount() + 15,
                              () -> {
                                 if (player.isAlive()) {
                                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                                    sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 0.5, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                                    sl.playSound(
                                       null,
                                       player.getX(),
                                       player.getY(),
                                       player.getZ(),
                                       (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(),
                                       SoundSource.PLAYERS,
                                       1.4F,
                                       0.7F
                                    );
                                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.8F);

                                    for (LivingEntity t : sl.getEntitiesOfClass(
                                       LivingEntity.class,
                                       player.getBoundingBox().inflate(10.0),
                                       ex -> ex != player
                                          && ex.isAlive()
                                          && !ex.getType().getCategory().isFriendly()
                                          && !ex.getTags().contains("shadow_soldier")
                                    )) {
                                       t.hurt(player.damageSources().playerAttack(player), 50.0F);
                                       double tdx = t.getX() - player.getX();
                                       double tdz = t.getZ() - player.getZ();
                                       double distx = Math.max(0.1, Math.sqrt(tdx * tdx + tdz * tdz));
                                       t.setDeltaMovement(tdx / distx * 2.2, 0.9, tdz / distx * 2.2);
                                       t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                                    }
                                 }
                              }
                           )
                        );
                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.ruyi_staff"), true);
                     break;
                  case "supreme_monarch_of_shadows":
                     spawnArcaneCircleGround(sl, player, 7.0F, 70, -11922292);
                     sl.sendParticles(ParticleTypes.SONIC_BOOM, px, py + 1.0, pz, 1, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.SCULK_SOUL, px, py + 0.5, pz, 120, 3.0, 0.5, 3.0, 0.1);
                     sl.playSound(null, px, py, pz, SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, px, py, pz, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.1F, 1.1F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(14.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.setDeltaMovement(0.0, -2.5, 0.0);
                        e.hurt(player.damageSources().magic(), 45.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                        e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.shadow_monarch"), true);
                     break;
                  case "monarch_of_destruction_antares":
                  case "abyssal_black_dragon":
                     spawnArcaneCircleGround(sl, player, 7.5F, 70, -56832);

                     for (int deg = 0; deg < 360; deg += 12) {
                        double rad = Math.toRadians(deg);
                        sl.sendParticles(ParticleTypes.DRAGON_BREATH, px + Math.cos(rad) * 4.0, py + 0.5, pz + Math.sin(rad) * 4.0, 5, 0.2, 0.2, 0.2, 0.05);
                        sl.sendParticles(ParticleTypes.FLAME, px + Math.cos(rad) * 6.0, py + 0.5, pz + Math.sin(rad) * 6.0, 8, 0.3, 0.3, 0.3, 0.08);
                     }

                     sl.playSound(null, px, py, pz, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.4F, 0.8F);
                     sl.playSound(null, px, py, pz, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.7F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(14.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.setRemainingFireTicks(240);
                        e.hurt(player.damageSources().playerAttack(player), 48.0F);
                        double edx = e.getX() - px;
                        double edz = e.getZ() - pz;
                        double dist = Math.max(0.1, Math.sqrt(edx * edx + edz * edz));
                        e.setDeltaMovement(edx / dist * 2.0, 0.6, edz / dist * 2.0);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.chaos_dragon"), true);
                     break;
                  case "supreme_swordsman_joonghyuk":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 2, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1, false, false, true));
                     spawnArcaneCircleGround(sl, player, 5.5F, 60, -16718337);
                     sl.sendParticles(ParticleTypes.END_ROD, px, py + 1.2, pz, 60, 1.2, 1.0, 1.2, 0.1);
                     sl.playSound(null, px, py, pz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(16.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 4));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 3));
                        e.hurt(player.damageSources().playerAttack(player), 20.0F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.regressor_eye"), true);
                     break;
                  case "plum_blossom_sword_saint":
                     spawnMurimCircleGround(sl, player, 6.0F, 70, -38476);
                     sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px, py + 1.0, pz, 150, 4.0, 1.5, 4.0, 0.2);

                     for (int deg = 0; deg < 360; deg += 30) {
                        double rad = Math.toRadians(deg);
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px + Math.cos(rad) * 3.0, py + 0.8, pz + Math.sin(rad) * 3.0, 2, 0.2, 0.2, 0.2, 0.05);
                     }

                     sl.playSound(null, px, py, pz, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.2F);
                     sl.playSound(null, px, py, pz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(12.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.hurt(player.damageSources().playerAttack(player), 44.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.plum_blossom"), true);
                     break;
                  default:
                     player.heal(player.getMaxHealth() * 0.4F);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false, true));
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 1.0, pz, 80, 1.5, 1.0, 1.5, 0.2);
                     sl.playSound(null, px, py, pz, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

                     for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(10.0),
                        ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                     )) {
                        e.hurt(player.damageSources().playerAttack(player), 30.0F);
                        double bdx = e.getX() - px;
                        double bdz = e.getZ() - pz;
                        double dist = Math.max(0.1, Math.sqrt(bdx * bdx + bdz * bdz));
                        e.setDeltaMovement(bdx / dist * 1.8, 0.4, bdz / dist * 1.8);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.stigma.activated.sponsor_blessing"), true);
               }

               triggerConstellationReaction(player, ReactionType.COMBO_TRANCE);
               AwakeningSystemMod.sync(player);
               return true;
            }
         } else {
            player.displayClientMessage(Component.translatable("awakening_system.action.no_sponsor_stigma"), true);
            player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
            return false;
         }
      }
   }

   public static void handleCultivationAction(ServerPlayer player, String action, int targetIndex) {
      SystemData data = data(player);
      if (data.isAnySystemUpdating()) {
         player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
         player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
      } else {
         if ("toggle_meditation".equals(action)) {
            if (player.getVehicle() instanceof MeditationSeatEntity seat) {
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
               MeditationSeatEntity seat = (MeditationSeatEntity)((EntityType)ModEntities.MEDITATION_SEAT.get()).create(sl);
               if (seat != null) {
                  seat.setPos(player.getX(), player.getY(), player.getZ());
                  sl.addFreshEntity(seat);
                  player.startRiding(seat, true);
                  player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
                  player.displayClientMessage(Component.translatable("awakening_system.cultivation.started"), true);
               }
            }
         } else if ("open_meridian".equals(action)) {
            if (targetIndex < 0 || targetIndex >= 12) {
               return;
            }

            if (data.isMeridianOpen(targetIndex)) {
               player.displayClientMessage(Component.translatable("awakening_system.action.meridian_already_open"), true);
               return;
            }

            int qiCost = (targetIndex + 1) * 35;
            if (data.cultivationQi() < qiCost) {
               player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", new Object[]{qiCost}), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            data.consumeCultivationQi(qiCost);
            data.openMeridian(targetIndex);
            player.playNotifySound((SoundEvent)ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            Component meridianName = Component.translatable("awakening_system.meridian." + targetIndex + ".name");
            Component organName = Component.translatable("awakening_system.meridian." + targetIndex + ".organ");
            player.displayClientMessage(Component.translatable("awakening_system.action.meridian_success", new Object[]{meridianName, organName}), true);
            AwakeningSystemMod.sync(player);
         } else if ("breakthrough_sublevel".equals(action)) {
            int currentSub = data.cultivationSubLevel();
            if (currentSub >= 9) {
               player.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_max"), true);
               return;
            }

            int reqQi = data.maxCultivationQi();
            if (data.cultivationQi() < reqQi) {
               player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", new Object[]{reqQi}), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            data.setCultivationSubLevel(currentSub + 1);
            data.setCultivationQi(0);
            if (player.level() instanceof ServerLevel sl) {
               sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
               sl.playSound(
                  null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 1.2F, 1.0F
               );
            }

            player.displayClientMessage(
               Component.translatable(
                  "awakening_system.cultivation.sublevel_success", new Object[]{currentSub + 1, getDantianRealmComponent(data.dantianRealm())}
               ),
               true
            );
            AwakeningSystemMod.sync(player);
         } else if ("advance_realm".equals(action)) {
            int current = data.dantianRealm();
            if (current >= 9) {
               player.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_max"), true);
               return;
            }

            if (data.isUnderTribulation()) {
               player.displayClientMessage(Component.translatable("awakening_system.action.tribulation_active"), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            if (data.cultivationSubLevel() < 9) {
               player.displayClientMessage(Component.translatable("awakening_system.cultivation.need_sublevel_9"), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            int requiredMeridians = Math.min(12, (current + 1) * 2);
            int reqQix = (current + 1) * 150;
            if (data.getOpenedMeridiansCount() < requiredMeridians) {
               player.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_req", new Object[]{reqQix}), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            if (data.cultivationQi() < reqQix) {
               player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", new Object[]{reqQix}), true);
               player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
               return;
            }

            int targetRealm = current + 1;
            if (targetRealm >= 2) {
               int strikes = switch (targetRealm) {
                  case 2 -> 3;
                  case 3 -> 6;
                  case 4 -> 9;
                  case 5 -> 12;
                  case 6 -> 15;
                  case 7 -> 18;
                  case 8 -> 27;
                  case 9 -> 36;
                  default -> 3;
               };
               data.consumeCultivationQi(reqQix);
               data.startTribulation(targetRealm, strikes);
               if (player.level() instanceof ServerLevel sl) {
                  sl.setWeatherParameters(0, strikes * 35 + 200, true, true);
                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.CELESTIAL_TRIBULATION.value(), SoundSource.WEATHER, 3.0F, 1.0F
                  );
                  sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
                  sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.5, 1.5, 1.5, 0.2);
               }

               player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.awakening_system.tribulation_title")));
               player.connection
                  .send(new ClientboundSetSubtitleTextPacket(Component.translatable("title.awakening_system.tribulation_subtitle", new Object[]{strikes})));
               player.sendSystemMessage(Component.translatable("message.awakening_system.tribulation_start"));
               AwakeningSystemMod.sync(player);
               return;
            }

            data.setDantianRealm(1);
            data.setCultivationSubLevel(1);
            data.setCultivationQi(0);
            if (player.level() instanceof ServerLevel sl) {
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
               sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 1.0, 1.0, 1.0, 0.2);
               sl.playSound(
                  null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.DANTIAN_BREAKTHROUGH.value(), SoundSource.PLAYERS, 1.4F, 1.0F
               );
            }

            player.displayClientMessage(
               Component.translatable("awakening_system.action.dantian_realm_success", new Object[]{getDantianRealmComponent(1)}), true
            );
            AwakeningSystemMod.sync(player);
         } else if (!"awaken_physique".equals(action) && !"cycle_physique".equals(action) && !"select_physique".equals(action)) {
            if ("use_physique_skill".equals(action)) {
               String phys = data.murimPhysique();
               if (phys == null || phys.isEmpty()) {
                  player.displayClientMessage(Component.translatable("message.awakening_system.physique_no_physique"), true);
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               if (data.getPhysiqueSkillCooldown() > 0) {
                  int sec = (data.getPhysiqueSkillCooldown() + 19) / 20;
                  player.displayClientMessage(Component.translatable("message.awakening_system.physique_cooldown", new Object[]{sec}), true);
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               int energyCost = 25;
               if (data.energy() < energyCost) {
                  player.displayClientMessage(Component.translatable("awakening_system.action.not_enough_energy", new Object[]{energyCost}), true);
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               data.consumeEnergy(energyCost);
               data.setPhysiqueSkillCooldown(400);
               ServerLevel sl = player.serverLevel();
               sl.playSound(
                  null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.MARTIAL_SKILL_ACTIVATE.value(), SoundSource.PLAYERS, 1.4F, 1.0F
               );
               int realm = data.dantianRealm();
               switch (phys) {
                  case "pure_yang":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_pure_yang"));
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.3F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.8F);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 80, 2.5, 0.8, 2.5, 0.15);
                     sl.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.5, player.getZ(), 25, 2.0, 0.5, 2.0, 0.1);
                     float dmg = 26.0F + realm * 5.0F;

                     for (LivingEntity mob : sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(8.5), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     )) {
                        mob.hurt(player.damageSources().playerAttack(player), dmg);
                        mob.igniteForSeconds(8.0F);
                        mob.setDeltaMovement(mob.getDeltaMovement().add((mob.getX() - player.getX()) * 0.25, 0.35, (mob.getZ() - player.getZ()) * 0.25));
                     }

                     player.heal(10.0F);
                     player.removeEffect(MobEffects.POISON);
                     player.removeEffect(MobEffects.WITHER);
                     player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                     player.removeEffect(MobEffects.WEAKNESS);
                     break;
                  case "nine_yin":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_nine_yin"));
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.8F, 0.7F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4F, 1.5F);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.8, player.getZ(), 90, 3.0, 0.8, 3.0, 0.12);
                     sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.5, player.getZ(), 30, 2.0, 0.5, 2.0, 0.05);
                     float dmgFreeze = 24.0F + realm * 4.5F;

                     for (LivingEntity mob : sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(9.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     )) {
                        mob.hurt(player.damageSources().magic(), dmgFreeze);
                        mob.setTicksFrozen(240);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                     break;
                  case "innate_sword":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_sword_body"));
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.8F, 1.2F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.5F, 1.4F
                     );
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 16, 2.5, 0.5, 2.5, 0.1);
                     sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.2, player.getZ(), 60, 2.5, 1.0, 2.5, 0.2);
                     float perSwordDmg = 15.0F + realm * 3.5F;
                     List<LivingEntity> targets = sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(14.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     );
                     if (!targets.isEmpty()) {
                        for (int i = 0; i < 6; i++) {
                           LivingEntity target = targets.get(i % targets.size());
                           target.hurt(player.damageSources().playerAttack(player), perSwordDmg);
                           sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.4, 0.4, 0.2);
                           sl.playSound(
                              null, target.getX(), target.getY(), target.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.4F + i * 0.1F
                           );
                        }
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
                     break;
                  case "celestial_dragon":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_celestial_dragon"));
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0F, 1.2F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 1.4F);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 70, 3.5, 1.0, 3.5, 0.15);
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 50, 3.0, 1.0, 3.0, 0.2);
                     float dmgSword = 28.0F + realm * 6.0F;

                     for (LivingEntity mob : sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(12.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     )) {
                        mob.hurt(player.damageSources().playerAttack(player), dmgSword);
                        double dx = mob.getX() - player.getX();
                        double dz = mob.getZ() - player.getZ();
                        double dist = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        mob.setDeltaMovement(dx / dist * 1.8, 0.6, dz / dist * 1.8);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1));
                     break;
                  case "heavenly_demon":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_heavenly_demon"));
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.4F, 1.5F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.8F, 0.7F
                     );
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 80, 3.0, 0.8, 3.0, 0.15);
                     sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 0.5, player.getZ(), 40, 2.5, 0.5, 2.5, 0.08);
                     float dmgGinseng = 22.0F + realm * 5.0F;

                     for (LivingEntity mob : sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     )) {
                        mob.hurt(player.damageSources().wither(), dmgGinseng);
                        mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                        mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                     player.heal(12.0F);
                     break;
                  case "primordial_chaos":
                     player.sendSystemMessage(Component.translatable("message.awakening_system.physique_primordial_chaos"));
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.PLAYERS, 1.3F, 0.9F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 0.8F);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 100, 3.5, 1.5, 3.5, 0.3);
                     sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 40, 2.5, 1.2, 2.5, 0.1);
                     sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.8, player.getZ(), 40, 3.0, 1.0, 3.0, 0.15);
                     float dmgThunder = 36.0F + realm * 6.5F;

                     for (LivingEntity mob : sl.getEntitiesOfClass(
                        LivingEntity.class, player.getBoundingBox().inflate(11.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                     )) {
                        double dx = player.getX() - mob.getX();
                        double dz = player.getZ() - mob.getZ();
                        double dist = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        mob.setDeltaMovement(dx / dist * 0.9, 0.25, dz / dist * 0.9);
                        mob.hurt(player.damageSources().magic(), dmgThunder);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 5));
                     }

                     data.addCultivationQi(250);
                     data.restoreEnergy(50);
               }

               AwakeningSystemMod.sync(player);
            } else if ("select_dao_skill".equals(action)) {
               if (targetIndex >= 0 && targetIndex < 8) {
                  data.setSelectedDaoSkill(targetIndex);
                  AwakeningSystemMod.sync(player);
                  player.playNotifySound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
               }
            } else if ("use_dao_skill".equals(action)) {
               int skillIdx = targetIndex >= 0 && targetIndex < 8 ? targetIndex : data.selectedDaoSkill();
               int currentRealm = data.dantianRealm();

               int reqRealm = switch (skillIdx) {
                  case 0 -> 1;
                  case 1 -> 2;
                  case 2 -> 3;
                  case 3 -> 4;
                  case 4 -> 5;
                  case 5 -> 3;
                  case 6 -> 4;
                  case 7 -> 5;
                  default -> 1;
               };

               int qiCost = switch (skillIdx) {
                  case 0 -> 20;
                  case 1 -> 35;
                  case 2 -> 50;
                  case 3 -> 75;
                  case 4 -> 45;
                  case 5 -> 30;
                  case 6 -> 65;
                  case 7 -> 80;
                  default -> 20;
               };
               if (currentRealm < reqRealm) {
                  player.displayClientMessage(
                     Component.translatable("message.awakening_system.dao_skill_realm_req", new Object[]{getDantianRealmComponent(reqRealm)}), true
                  );
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               if (data.daoSkillCooldown() > 0) {
                  int sec = (data.daoSkillCooldown() + 19) / 20;
                  player.displayClientMessage(Component.translatable("message.awakening_system.dao_skill_cooldown", new Object[]{sec}), true);
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               if (data.cultivationQi() < qiCost) {
                  player.displayClientMessage(Component.translatable("awakening_system.action.meridian_not_enough_qi", new Object[]{qiCost}), true);
                  player.playNotifySound((SoundEvent)ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
                  return;
               }

               if (skillIdx == 1 || skillIdx == 2 || skillIdx == 3) {
                  SystemEvents.PlayerChargingState active = ACTIVE_CHARGES.get(player.getUUID());
                  if (active != null) {
                     if (active.daoSkillIdx == skillIdx) {
                        if (active.currentTicks >= Math.max(8, (int)(active.maxTicks * 0.35F))) {
                           releaseActiveCharge(player, false);
                           return;
                        }

                        player.displayClientMessage(Component.translatable("awakening_system.channeling.qi"), true);
                        return;
                     }

                     cancelChargingSkill(player, false);
                  }

                  startChargingSkill(player, "dao_skill_" + skillIdx, -1, skillIdx);
                  return;
               }

               executeDaoSkillDirect(player, skillIdx, 1.0F, false);
            }
         } else {
            String[] physiques = new String[]{"pure_yang", "nine_yin", "innate_sword", "celestial_dragon", "heavenly_demon", "primordial_chaos"};
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
                  player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                  return;
               }

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
               sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
               sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.8, 1.0, 0.8, 0.2);
               sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.5, player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
            }

            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.3F);

            int physColor = switch (chosen) {
               case "primordial_chaos" -> -2074883;
               case "heavenly_demon" -> -56747;
               case "celestial_dragon" -> -10496;
               case "innate_sword" -> -2252579;
               case "nine_yin" -> -9377537;
               default -> -35226;
            };
            Component pName = Component.translatable("awakening_system.physique." + chosen + ".name");
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.physique_awakened_title",
               "awakening_system.notification.physique_awakened_desc",
               physColor,
               new Object[]{pName.getString()}
            );
            player.displayClientMessage(Component.translatable("awakening_system.cultivation.physique_selected", new Object[]{pName}), true);
            AwakeningSystemMod.sync(player);
         }
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
         case 5 -> 30;
         case 6 -> 65;
         case 7 -> 80;
         default -> 20;
      };

      int cooldownTicks = switch (skillIdx) {
         case 0 -> 140;
         case 1 -> 200;
         case 2 -> 360;
         case 3 -> 500;
         case 4 -> 160;
         case 5 -> 240;
         case 6 -> 400;
         case 7 -> 600;
         default -> 140;
      };
      data.consumeCultivationQi(qiCost);
      data.setDaoSkillCooldown(cooldownTicks);
      ServerLevel sl = player.serverLevel();
      switch (skillIdx) {
         case 0: {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.4F);
            sl.playSound(
               null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.MARTIAL_SKILL_ACTIVATE.value(), SoundSource.PLAYERS, 1.2F, 1.3F
            );
            Vec3 lookVec = player.getLookAngle();
            spawnMurimCircleGround(sl, player, 3.6F, 45, -16711732);
            spawnMurimCircleVertical(sl, player, 2.2F, 40, -16711732, false);
            float dmg = (18.0F + currentRealm * 4.5F) * chargeMult;
            List<LivingEntity> targets = sl.getEntitiesOfClass(
               LivingEntity.class, player.getBoundingBox().inflate(18.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e) && player.hasLineOfSight(e)
            );
            targets.sort((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)));
            int struck = 0;

            for (LivingEntity mobx : targets) {
               mobx.hurt(player.damageSources().magic(), dmg);
               sl.sendParticles(ParticleTypes.SWEEP_ATTACK, mobx.getX(), mobx.getY() + 1.0, mobx.getZ(), 4, 0.2, 0.2, 0.2, 0.05);
               sl.sendParticles(ParticleTypes.ENCHANTED_HIT, mobx.getX(), mobx.getY() + 1.0, mobx.getZ(), 16, 0.3, 0.3, 0.3, 0.1);
               sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, mobx.getX(), mobx.getY() + 1.0, mobx.getZ(), 12, 0.2, 0.2, 0.2, 0.1);
               if (++struck >= 4) {
                  break;
               }
            }

            if (struck == 0) {
               for (int step = 2; step <= 16; step += 2) {
                  double px = player.getX() + lookVec.x * step;
                  double py = player.getEyeY() + lookVec.y * step;
                  double pz = player.getZ() + lookVec.z * step;
                  sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                  sl.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py, pz, 6, 0.2, 0.2, 0.2, 0.05);
               }
            }

            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_swords"));
            break;
         }
         case 1: {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.4F, 1.1F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.4F);
            Vec3 lookVec = player.getLookAngle().normalize();
            spawnMurimCircleVertical(sl, player, isOvercharged ? 4.5F : 3.6F, 40, -8333057, false);
            float dmg = (28.0F + currentRealm * 6.0F) * chargeMult;
            spawnShockwaveRing(sl, player.getX() + lookVec.x * 3.0, player.getY() + 1.0, player.getZ() + lookVec.z * 3.0, ParticleTypes.POOF, 4.0, 24, 0.15);
            spawnShockwaveRing(sl, player.getX() + lookVec.x * 6.0, player.getY() + 1.0, player.getZ() + lookVec.z * 6.0, ParticleTypes.EXPLOSION, 6.0, 18, 0.2);
            if (isOvercharged) {
               spawnShockwaveRing(
                  sl, player.getX() + lookVec.x * 9.0, player.getY() + 1.0, player.getZ() + lookVec.z * 9.0, ParticleTypes.SONIC_BOOM, 8.0, 24, 0.25
               );
            }

            for (LivingEntity mob : sl.getEntitiesOfClass(
               LivingEntity.class, player.getBoundingBox().inflate(14.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
            )) {
               double dx = mob.getX() - player.getX();
               double dz = mob.getZ() - player.getZ();
               double dist = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
               double dot = (dx * lookVec.x + dz * lookVec.z) / dist;
               if (dot > 0.35) {
                  mob.hurt(player.damageSources().playerAttack(player), dmg);
                  mob.setDeltaMovement(lookVec.x * (isOvercharged ? 2.2 : 1.6), 0.55, lookVec.z * (isOvercharged ? 2.2 : 1.6));
                  mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, isOvercharged ? 3 : 2));
                  mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, isOvercharged ? 2 : 1));
               }
            }

            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_palm"));
            break;
         }
         case 2:
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 0.9F);
            spawnMurimCircleGround(sl, player, 6.5F, 80, -10496);
            spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 8.0, 40, 0.2);
            spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.END_ROD, 12.0, 50, 0.25);
            if (isOvercharged) {
               spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLASH, 16.0, 30, 0.3);
            }

            player.heal((14.0F + currentRealm * 2.0F) * chargeMult);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, isOvercharged ? 2 : 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, isOvercharged ? 2 : 1));
            data.addCultivationQi((int)((60 + currentRealm * 15) * (isOvercharged ? 1.5F : 1.0F)));

            for (LivingEntity mob : sl.getEntitiesOfClass(
               LivingEntity.class, player.getBoundingBox().inflate(12.0), e -> e != player && e.isAlive() && !player.isAlliedTo(e)
            )) {
               mob.hurt(player.damageSources().magic(), (18.0F + currentRealm * 3.5F) * chargeMult);
               mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
               mob.igniteForSeconds(10.0F);
            }

            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_domain"));
            break;
         case 3: {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.4F);
            Vec3 lookVec = player.getLookAngle();
            spawnMurimCircleGround(sl, player, 4.8F, 70, -2080517);
            spawnMurimCircleVertical(sl, player, 3.6F, 70, -4560696, false);
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.2, player.getZ(), 60, 0.8, 1.2, 0.8, 0.08);
            sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 70, 1.0, 1.0, 1.0, 0.2);
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, isOvercharged ? 4 : 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, isOvercharged ? 2 : 1));
            player.getPersistentData().putInt("nascent_soul_guard_ticks", (int)(400.0F * chargeMult));
            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_nascent"));
            break;
         }
         case 4: {
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();
            Vec3 lookVec = player.getLookAngle();
            spawnMurimCircleGround(sl, oldX, oldY, oldZ, 3.2F, 35, -16718337);
            double targetX = oldX + lookVec.x * 15.0;
            double targetY = oldY + lookVec.y * 15.0;
            double targetZ = oldZ + lookVec.z * 15.0;
            BlockPos targetPos = BlockPos.containing(targetX, targetY, targetZ);

            while (!sl.getBlockState(targetPos).isAir() && targetPos.getY() < 315) {
               targetPos = targetPos.above();
            }

            player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            spawnMurimCircleGround(sl, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 3.2F, 35, -16718337);
            sl.playSound(null, oldX, oldY, oldZ, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 1.2F);
            sl.sendParticles(ParticleTypes.FLASH, oldX, oldY + 1.0, oldZ, 3, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, oldX, oldY + 1.0, oldZ, 60, 1.8, 1.8, 1.8, 0.25);
            sl.sendParticles(ParticleTypes.EXPLOSION, oldX, oldY + 1.0, oldZ, 4, 1.0, 1.0, 1.0, 0.1);
            float dmg = (36.0F + currentRealm * 7.0F) * chargeMult;

            for (LivingEntity mob : sl.getEntitiesOfClass(
               LivingEntity.class,
               new AABB(oldX - 7.0, oldY - 4.0, oldZ - 7.0, oldX + 7.0, oldY + 4.0, oldZ + 7.0),
               e -> e != player && e.isAlive() && !player.isAlliedTo(e)
            )) {
               mob.hurt(player.damageSources().lightningBolt(), dmg);
               mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
            }

            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 1.2F);
            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_flash"));
            break;
         }
         case 5:
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.4F, 1.3F);
            sl.playSound(
               null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.MARTIAL_SKILL_ACTIVATE.value(), SoundSource.PLAYERS, 1.2F, 1.5F
            );
            Vec3 look = player.getLookAngle();
            double tX = player.getX() + look.x * 12.0;
            double tY = player.getY() + Math.max(0.5, look.y * 6.0);
            double tZ = player.getZ() + look.z * 12.0;

            for (int step = 0; step <= 12; step++) {
               double px = player.getX() + look.x * step;
               double py = player.getY() + 0.5 + look.y * step * 0.5;
               double pz = player.getZ() + look.z * step;
               sl.sendParticles(ParticleTypes.CLOUD, px, py, pz, 4, 0.3, 0.3, 0.3, 0.05);
               sl.sendParticles(ParticleTypes.POOF, px, py, pz, 2, 0.2, 0.2, 0.2, 0.02);
            }

            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0));
            player.teleportTo(tX, tY, tZ);
            player.resetFallDistance();
            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_cloudstep"));
            break;
         case 6:
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 0.9F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.3F, 1.1F);
            Vec3 lookVec = player.getLookAngle();
            float dmg = (32.0F + currentRealm * 6.0F) * chargeMult;

            for (int step = 1; step <= 16; step++) {
               double px = player.getX() + lookVec.x * step;
               double py = player.getEyeY() + lookVec.y * step;
               double pz = player.getZ() + lookVec.z * step;
               sl.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
               sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 2, 0.3, 0.3, 0.3, 0.05);
               sl.sendParticles(ParticleTypes.PORTAL, px, py, pz, 8, 0.4, 0.4, 0.4, 0.1);

               for (LivingEntity m : sl.getEntitiesOfClass(
                  LivingEntity.class,
                  new AABB(px - 1.5, py - 1.5, pz - 1.5, px + 1.5, py + 1.5, pz + 1.5),
                  e -> e != player && e.isAlive() && !player.isAlliedTo(e)
               )) {
                  m.hurt(player.damageSources().magic(), dmg);
               }
            }

            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_void_cleave"));
            break;
         case 7:
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.9F, 1.4F);
            spawnMurimCircleGround(sl, player, 4.5F, 60, -10496);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.8, 1.0, 0.8, 0.3);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.8, 0.6, 0.1);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.8, 0.6, 0.1);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            data.addCultivationQi(40);
            player.sendSystemMessage(Component.translatable("message.awakening_system.dao_skill_five_elements"));
      }

      AwakeningSystemMod.sync(player);
   }

   public static float getZoneQiDensity(Player player) {
      if (player == null) {
         return 1.0F;
      } else {
         Level level = player.level();
         ResourceLocation dimLoc = level.dimension().location();
         if (player.getServer() != null) {
            SeriesStructureData sData = SeriesStructureData.get(player.getServer());

            for (Entry<String, BlockPos> entry : sData.getAll().entrySet()) {
               if (entry.getKey().startsWith("spirit_valley")) {
                  BlockPos vPos = entry.getValue();
                  if (player.distanceToSqr(vPos.getX() + 0.5, vPos.getY() + 1.0, vPos.getZ() + 0.5) <= 1024.0) {
                     return 5.0F;
                  }
               }
            }
         }

         BlockPos pPos = player.blockPosition();

         for (BlockPos check : BlockPos.betweenClosed(pPos.offset(-20, -6, -20), pPos.offset(20, 6, 20))) {
            if (level.getBlockState(check).is(Blocks.EMERALD_BLOCK) && level.getBlockState(check.above()).is(Blocks.WATER)) {
               return 5.0F;
            }
         }

         if (dimLoc.getPath().contains("murim_world")) {
            return 3.5F;
         } else if (dimLoc.getPath().contains("isekai_world")) {
            double distToTree = Math.sqrt(player.getX() * player.getX() + player.getZ() * player.getZ());
            return distToTree <= 55.0 ? 2.5F : 1.5F;
         } else {
            return player.getY() >= 120.0 ? 1.8F : 1.0F;
         }
      }
   }

   public static String getZoneQiDensityLabel(Player player) {
      if (player == null) {
         return "Normal";
      } else {
         Level level = player.level();
         ResourceLocation dimLoc = level.dimension().location();
         if (player.getServer() != null) {
            SeriesStructureData sData = SeriesStructureData.get(player.getServer());

            for (Entry<String, BlockPos> entry : sData.getAll().entrySet()) {
               if (entry.getKey().startsWith("spirit_valley")) {
                  BlockPos vPos = entry.getValue();
                  if (player.distanceToSqr(vPos.getX() + 0.5, vPos.getY() + 1.0, vPos.getZ() + 0.5) <= 1024.0) {
                     return "Spirit Beast Valley (Jade Hot Springs)";
                  }
               }
            }
         }

         BlockPos pPos = player.blockPosition();

         for (BlockPos check : BlockPos.betweenClosed(pPos.offset(-20, -6, -20), pPos.offset(20, 6, 20))) {
            if (level.getBlockState(check).is(Blocks.EMERALD_BLOCK) && level.getBlockState(check.above()).is(Blocks.WATER)) {
               return "Spirit Beast Valley (Jade Hot Springs)";
            }
         }

         if (dimLoc.getPath().contains("murim_world")) {
            return "Immortal Murim Realm";
         } else if (dimLoc.getPath().contains("isekai_world")) {
            double distToTree = Math.sqrt(player.getX() * player.getX() + player.getZ() * player.getZ());
            return distToTree <= 55.0 ? "Well of Urðr" : "Isekai World";
         } else {
            return player.getY() >= 120.0 ? "Mountain Peak" : "Common Ground";
         }
      }
   }

   public static Component getDantianRealmComponent(int realm) {
      return Component.translatable("awakening_system.cultivation.realm." + Math.max(0, Math.min(9, realm)));
   }

   public static String getDantianRealmName(int realm) {
      return getDantianRealmComponent(realm).getString();
   }

   public static void spawnShockwaveRing(ServerLevel level, double x, double y, double z, ParticleOptions particle, double radius, int count, double speed) {
      double step = (Math.PI * 2) / Math.max(1, count);

      for (int i = 0; i < count; i++) {
         double angle = i * step;
         double dx = Math.cos(angle);
         double dz = Math.sin(angle);
         double px = x + dx * radius;
         double pz = z + dz * radius;
         level.sendParticles(particle, px, y, pz, 1, dx * speed, 0.02, dz * speed, speed);
      }
   }

   public static void spawnVortexPillar(
      ServerLevel level, double x, double y, double z, ParticleOptions particle, double maxRadius, double height, int layers, int particlesPerLayer
   ) {
      for (int l = 0; l < layers; l++) {
         double py = y + height * ((double)l / Math.max(1, layers));
         double radius = maxRadius * (1.0 - l / (layers * 2.0));
         double angleOffset = l * Math.PI / 4.0;
         double step = (Math.PI * 2) / Math.max(1, particlesPerLayer);

         for (int p = 0; p < particlesPerLayer; p++) {
            double angle = p * step + angleOffset;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;
            level.sendParticles(particle, px, py, pz, 1, 0.0, 0.05, 0.0, 0.02);
         }
      }
   }

   public static void spawnExpandingDome(ServerLevel level, double x, double y, double z, ParticleOptions particle, double radius, int rings, int pointsPerRing) {
      for (int r = 0; r < rings; r++) {
         double phi = (Math.PI / 2) * ((double)r / Math.max(1, rings));
         double ringRadius = radius * Math.cos(phi);
         double py = y + radius * Math.sin(phi);
         double step = (Math.PI * 2) / Math.max(1, pointsPerRing);

         for (int p = 0; p < pointsPerRing; p++) {
            double theta = p * step;
            double px = x + ringRadius * Math.cos(theta);
            double pz = z + ringRadius * Math.sin(theta);
            level.sendParticles(particle, px, py, pz, 1, 0.0, 0.02, 0.0, 0.01);
         }
      }
   }

   public static void spawnHelixBeam(
      ServerLevel level, Vec3 start, Vec3 dir, double length, ParticleOptions coreParticle, ParticleOptions spiralParticle, double spiralRadius
   ) {
      Vec3 norm = dir.normalize();
      Vec3 up = Math.abs(norm.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = norm.cross(up).normalize();
      up = right.cross(norm).normalize();
      double stepDist = 0.5;
      int steps = (int)(length / stepDist);

      for (int i = 0; i <= steps; i++) {
         double d = i * stepDist;
         Vec3 corePos = start.add(norm.scale(d));
         if (coreParticle != null) {
            level.sendParticles(coreParticle, corePos.x, corePos.y, corePos.z, 1, 0.0, 0.0, 0.0, 0.0);
         }

         if (spiralParticle != null) {
            double angle1 = d * 1.5;
            double angle2 = angle1 + Math.PI;
            Vec3 offset1 = right.scale(Math.cos(angle1) * spiralRadius).add(up.scale(Math.sin(angle1) * spiralRadius));
            Vec3 offset2 = right.scale(Math.cos(angle2) * spiralRadius).add(up.scale(Math.sin(angle2) * spiralRadius));
            level.sendParticles(spiralParticle, corePos.x + offset1.x, corePos.y + offset1.y, corePos.z + offset1.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(spiralParticle, corePos.x + offset2.x, corePos.y + offset2.y, corePos.z + offset2.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   public static void spawnBurstCross(ServerLevel level, double x, double y, double z, ParticleOptions particle, double length, int points) {
      double half = length * 0.5;
      double step = length / Math.max(1, points);

      for (int i = 0; i <= points; i++) {
         double offset = -half + i * step;
         level.sendParticles(particle, x + offset, y + 0.8 + offset * 0.3, z + offset, 1, 0.0, 0.02, 0.0, 0.02);
         level.sendParticles(particle, x + offset, y + 0.8 - offset * 0.3, z - offset, 1, 0.0, 0.02, 0.0, 0.02);
      }
   }

   public static void spawnMagicCircleEffect(
      ServerLevel level,
      int casterEntityId,
      double x,
      double y,
      double z,
      Vec3 lookDir,
      float radius,
      int durationTicks,
      int color,
      int circleType,
      boolean isCharging
   ) {
      SpawnMagicCirclePayload packet = new SpawnMagicCirclePayload(
         casterEntityId, x, y, z, (float)lookDir.x, (float)lookDir.y, (float)lookDir.z, radius, durationTicks, color, circleType, isCharging
      );
      PacketDistributor.sendToPlayersNear(level, null, x, y, z, 96.0, packet, new CustomPacketPayload[0]);
      level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.5F);
   }

   public static void spawnMagicCircleEffect(
      ServerLevel level, double x, double y, double z, Vec3 lookDir, float radius, int durationTicks, int color, int circleType
   ) {
      spawnMagicCircleEffect(level, -1, x, y, z, lookDir, radius, durationTicks, color, circleType, false);
   }

   public static void cancelMagicCircles(ServerPlayer player) {
      if (player.level() instanceof ServerLevel sl) {
         PacketDistributor.sendToPlayersNear(
            sl, null, player.getX(), player.getY(), player.getZ(), 96.0, new CancelMagicCirclePayload(player.getId()), new CustomPacketPayload[0]
         );
      }
   }

   public static void spawnArcaneCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
      spawnMagicCircleEffect(
         level, player.getId(), player.getX(), player.getY(), player.getZ(), new Vec3(0.0, 0.0, 1.0), radius, durationTicks, color, 0, isCharging
      );
   }

   public static void spawnArcaneCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
      spawnArcaneCircleGround(level, player, radius, durationTicks, color, false);
   }

   public static void spawnArcaneCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
      Vec3 look = player.getLookAngle().normalize();
      double dist = radius > 4.0F ? Math.max(2.5, Math.min(5.0, radius * 0.4)) : 1.65;
      Vec3 center = player.position().add(0.0, player.getEyeHeight(), 0.0).add(look.scale(dist));
      spawnMagicCircleEffect(level, player.getId(), center.x, center.y, center.z, look, radius, durationTicks, color, 1, isCharging);
   }

   public static void spawnArcaneCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
      spawnArcaneCircleVertical(level, player, radius, durationTicks, color, false);
   }

   public static void spawnMurimCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
      spawnMagicCircleEffect(
         level, player.getId(), player.getX(), player.getY(), player.getZ(), new Vec3(0.0, 0.0, 1.0), radius, durationTicks, color, 2, isCharging
      );
   }

   public static void spawnMurimCircleGround(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
      spawnMurimCircleGround(level, player, radius, durationTicks, color, false);
   }

   public static void spawnMurimCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color, boolean isCharging) {
      Vec3 look = player.getLookAngle().normalize();
      double dist = radius > 4.0F ? Math.max(2.5, Math.min(5.0, radius * 0.4)) : 1.65;
      Vec3 center = player.position().add(0.0, player.getEyeHeight(), 0.0).add(look.scale(dist));
      spawnMagicCircleEffect(level, player.getId(), center.x, center.y, center.z, look, radius, durationTicks, color, 3, isCharging);
   }

   public static void spawnMurimCircleVertical(ServerLevel level, ServerPlayer player, float radius, int durationTicks, int color) {
      spawnMurimCircleVertical(level, player, radius, durationTicks, color, false);
   }

   public static void spawnArcaneCircleGround(ServerLevel level, double x, double y, double z, float radius, int durationTicks, int color) {
      spawnMagicCircleEffect(level, x, y, z, new Vec3(0.0, 0.0, 1.0), radius, durationTicks, color, 0);
   }

   public static void spawnArcaneCircleVertical(ServerLevel level, Vec3 center, Vec3 lookDir, float radius, int durationTicks, int color) {
      spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, radius, durationTicks, color, 1);
   }

   public static void spawnMurimCircleGround(ServerLevel level, double x, double y, double z, float radius, int durationTicks, int color) {
      spawnMagicCircleEffect(level, x, y, z, new Vec3(0.0, 0.0, 1.0), radius, durationTicks, color, 2);
   }

   public static void spawnMurimCircleVertical(ServerLevel level, Vec3 center, Vec3 lookDir, float radius, int durationTicks, int color) {
      spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, radius, durationTicks, color, 3);
   }

   public static boolean isChargeableSkill(String skillId) {
      if (skillId == null) {
         return false;
      } else {
         return switch (skillId) {
            case "elemental_spear", "gravity_crush", "supernova_incineration", "shonen_zoltraak", "shonen_infinite_void", "isekai_mana_burst" -> true;
            default -> false;
         };
      }
   }

   public static int getChargeSkillMaxTicks(String skillId, int daoSkillIdx) {
      if (daoSkillIdx >= 0) {
         return switch (daoSkillIdx) {
            case 1 -> 30;
            case 2 -> 40;
            case 3 -> 40;
            default -> 30;
         };
      } else {
         return switch (skillId) {
            case "elemental_spear" -> 30;
            case "gravity_crush" -> 35;
            case "supernova_incineration" -> 45;
            case "shonen_zoltraak" -> 35;
            case "shonen_infinite_void" -> 50;
            case "isekai_mana_burst" -> 30;
            default -> 30;
         };
      }
   }

   public static String getChargeSkillDisplayName(String skillId, int daoSkillIdx) {
      if (daoSkillIdx >= 0) {
         return switch (daoSkillIdx) {
            case 1 -> "Celestial Void Palm";
            case 2 -> "Golden Core Domain";
            case 3 -> "Nascent Soul Projection";
            default -> "Dao Cultivation Art";
         };
      } else {
         return switch (skillId) {
            case "elemental_spear" -> "Arcane Elemental Spear";
            case "gravity_crush" -> "Gravitational Collapse";
            case "supernova_incineration" -> "Supernova Incineration";
            case "shonen_zoltraak" -> "Zoltraak (Destructive Magic)";
            case "shonen_infinite_void" -> "Infinite Void (Domain)";
            case "isekai_mana_burst" -> "Isekai Mana Burst";
            default -> SkillDefinitions.get(skillId) != null ? SkillDefinitions.get(skillId).nameKey() : skillId;
         };
      }
   }

   public static void spawnChargingCirclesForSkill(ServerLevel level, ServerPlayer player, String skillId, int daoSkillIdx, int maxTicks) {
      int duration = maxTicks + 25;
      if (daoSkillIdx >= 0) {
         switch (daoSkillIdx) {
            case 1:
               spawnMurimCircleVertical(level, player, 3.2F, duration, -8333057, true);
               break;
            case 2:
               spawnMurimCircleGround(level, player, 6.0F, duration, -10496, true);
               break;
            case 3:
               spawnMurimCircleGround(level, player, 5.0F, duration, -2080517, true);
         }
      } else {
         switch (skillId) {
            case "elemental_spear":
               spawnArcaneCircleVertical(level, player, 2.5F, duration, -35072, true);
               break;
            case "gravity_crush":
               spawnArcaneCircleGround(level, player, 5.8F, duration, -6736897, true);
               break;
            case "supernova_incineration":
               spawnArcaneCircleVertical(level, player, 4.2F, duration, -47872, true);
               break;
            case "shonen_zoltraak":
               spawnArcaneCircleVertical(level, player, 3.0F, duration, -16718337, true);
               break;
            case "shonen_infinite_void":
               spawnArcaneCircleGround(level, player, 8.5F, duration, -8630785, true);
               break;
            case "isekai_mana_burst":
               spawnArcaneCircleGround(level, player, 5.0F, duration, -16711681, true);
         }
      }
   }

   public static boolean startChargingSkill(ServerPlayer player, String skillId, int slot, int daoSkillIdx) {
      if (player != null && player.level() instanceof ServerLevel level) {
         SystemData var9 = data(player);
         if (daoSkillIdx < 0) {
            if (!(Boolean)SystemConfig.ACTIVE_SKILLS_ENABLED.get()) {
               AwakeningSystemMod.notify(
                  player, "awakening_system.notification.skill_activate_failed.title", "awakening_system.notification.skills_disabled", -35226, new Object[0]
               );
               return false;
            }

            if (var9.isAnySystemUpdating()) {
               player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
               player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
               return false;
            }

            int cost = "architect_of_the_system".equals(var9.sponsorConstellation()) ? 20 : 35;
            if (!var9.skills().containsKey(skillId) || !SkillDefinitions.isAvailable(skillId)) {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_activate_failed.title",
                  "awakening_system.notification.skill_activate_failed.message",
                  -35226,
                  new Object[0]
               );
               return false;
            }

            if (var9.activeCooldownSeconds(skillId, player.level().getGameTime()) > 0) {
               int sec = var9.activeCooldownSeconds(skillId, player.level().getGameTime());
               player.displayClientMessage(Component.translatable("awakening_system.action.skill_cooldown", new Object[]{sec}), true);
               player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
               return false;
            }

            if (var9.energy() < cost) {
               player.displayClientMessage(Component.translatable("awakening_system.action.not_enough_energy"), true);
               player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
               return false;
            }
         }

         int maxTicks = getChargeSkillMaxTicks(skillId, daoSkillIdx);
         String displayName = getChargeSkillDisplayName(skillId, daoSkillIdx);
         spawnChargingCirclesForSkill(level, player, skillId, daoSkillIdx, maxTicks);
         SystemEvents.PlayerChargingState state = new SystemEvents.PlayerChargingState(skillId, slot, daoSkillIdx, displayName, maxTicks);
         ACTIVE_CHARGES.put(player.getUUID(), state);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.8F);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.3F, 1.2F);
         return true;
      } else {
         return false;
      }
   }

   public static void tickPlayerCharging(ServerPlayer player) {
      if (player.isAlive() && !player.hasDisconnected()) {
         SystemEvents.PlayerChargingState state = ACTIVE_CHARGES.get(player.getUUID());
         if (state != null) {
            if (player.isShiftKeyDown()) {
               cancelChargingSkill(player, true);
            } else {
               state.currentTicks++;
               float progress = Math.min(1.0F, (float)state.currentTicks / state.maxTicks);
               player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, false, false, false));
               if (state.currentTicks % 4 == 0) {
                  float pitch = 0.8F + 1.0F * progress;
                  player.serverLevel()
                     .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, pitch);
               }

               ServerLevel sl = player.serverLevel();
               double ang = player.tickCount * 0.45;
               double r = 2.2 * (1.0 - 0.7 * progress);
               double px = player.getX() + Math.cos(ang) * r;
               double pz = player.getZ() + Math.sin(ang) * r;
               SimpleParticleType swirlParticle = state.daoSkillIdx >= 0 ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.PORTAL;
               sl.sendParticles(swirlParticle, px, player.getY() + 0.8, pz, 2, 0.05, 0.05, 0.05, 0.01);
               int percent = (int)(progress * 100.0F);
               int filledBars = percent * 10 / 100;
               StringBuilder bar = new StringBuilder();

               for (int i = 0; i < 10; i++) {
                  if (i < filledBars) {
                     bar.append("▓");
                  } else {
                     bar.append("░");
                  }
               }

               String color = progress >= 1.0F ? "§6§l" : (progress >= 0.7F ? "§e" : "§b");
               Component barComp = Component.literal("§e§l✦ §f" + state.displayName + " " + color + "[" + bar + "] §f" + percent + "% §e§l✦");
               player.displayClientMessage(barComp, true);
               if (state.currentTicks >= state.maxTicks) {
                  releaseActiveCharge(player, true);
               }
            }
         }
      } else {
         cancelChargingSkill(player, false);
      }
   }

   public static void cancelChargingSkill(ServerPlayer player, boolean notify) {
      SystemEvents.PlayerChargingState state = ACTIVE_CHARGES.remove(player.getUUID());
      if (state != null) {
         cancelMagicCircles(player);
         if (notify) {
            player.displayClientMessage(Component.translatable("awakening_system.channeling.cancelled"), true);
            player.serverLevel()
               .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.8F);
         }
      }
   }

   public static void releaseActiveCharge(ServerPlayer player, boolean isOvercharged) {
      SystemEvents.PlayerChargingState state = ACTIVE_CHARGES.remove(player.getUUID());
      if (state != null) {
         cancelMagicCircles(player);
         float chargeRatio = Math.min(1.0F, (float)state.currentTicks / state.maxTicks);
         float chargeMult = isOvercharged ? 1.5F : 0.5F + 0.5F * chargeRatio;
         ServerLevel sl = player.serverLevel();
         if (isOvercharged) {
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.4F, 1.3F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.6F);
            player.displayClientMessage(Component.translatable("awakening_system.channeling.max_overcharge"), true);
         } else {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.2F, 1.2F);
            int pct = (int)(chargeRatio * 100.0F);
            player.displayClientMessage(Component.translatable("awakening_system.channeling.quick_release", new Object[]{pct}), true);
         }

         if (state.daoSkillIdx >= 0) {
            executeDaoSkillDirect(player, state.daoSkillIdx, chargeMult, isOvercharged);
         } else {
            activateSkill(player, state.skillId, chargeMult, isOvercharged);
         }
      }
   }

   public static void spawnMagicCircle(
      ServerLevel level, double x, double y, double z, ParticleOptions ringParticle, ParticleOptions runeParticle, double radius
   ) {
      spawnGeometricMagicCircle(level, x, y, z, ringParticle, runeParticle, ParticleTypes.END_ROD, radius);
   }

   public static void spawnGeometricMagicCircle(
      ServerLevel level,
      double x,
      double y,
      double z,
      ParticleOptions ringParticle,
      ParticleOptions runeParticle,
      ParticleOptions centerParticle,
      double radius
   ) {
      int defaultColor = -6736897;
      if (ringParticle == ParticleTypes.FLAME || ringParticle == ParticleTypes.LAVA) {
         defaultColor = -43776;
      } else if (ringParticle == ParticleTypes.END_ROD) {
         defaultColor = -11475713;
      } else if (ringParticle == ParticleTypes.PORTAL) {
         defaultColor = -8630785;
      } else if (ringParticle == ParticleTypes.TOTEM_OF_UNDYING) {
         defaultColor = -10496;
      }

      spawnMagicCircleEffect(level, x, y, z, new Vec3(0.0, 0.0, 1.0), (float)radius, 45, defaultColor, 0);
      spawnShockwaveRing(level, x, y + 0.08, z, ringParticle, radius, 48, 0.02);
      spawnShockwaveRing(level, x, y + 0.08, z, ringParticle, radius * 0.6, 32, 0.01);
      spawnShockwaveRing(level, x, y + 0.08, z, ringParticle, radius * 0.25, 16, 0.01);
      if (runeParticle != null) {
         double[] angles = new double[8];
         double[] vx = new double[8];
         double[] vz = new double[8];

         for (int i = 0; i < 8; i++) {
            angles[i] = i * (Math.PI / 4);
            vx[i] = x + Math.cos(angles[i]) * (radius * 0.9);
            vz[i] = z + Math.sin(angles[i]) * (radius * 0.9);
            level.sendParticles(runeParticle, vx[i], y + 0.15, vz[i], 2, 0.05, 0.05, 0.05, 0.01);
         }

         int[] sq1 = new int[]{0, 2, 4, 6, 0};

         for (int s = 0; s < 4; s++) {
            drawParticleLine(level, vx[sq1[s]], y + 0.08, vz[sq1[s]], vx[sq1[s + 1]], y + 0.08, vz[sq1[s + 1]], runeParticle, 7);
         }

         int[] sq2 = new int[]{1, 3, 5, 7, 1};

         for (int s = 0; s < 4; s++) {
            drawParticleLine(level, vx[sq2[s]], y + 0.08, vz[sq2[s]], vx[sq2[s + 1]], y + 0.08, vz[sq2[s + 1]], runeParticle, 7);
         }

         for (int i = 0; i < 8; i++) {
            double inX = x + Math.cos(angles[i]) * (radius * 0.25);
            double inZ = z + Math.sin(angles[i]) * (radius * 0.25);
            drawParticleLine(level, inX, y + 0.08, inZ, vx[i], y + 0.08, vz[i], ringParticle, 5);
         }
      }

      if (centerParticle != null) {
         for (double dy = 0.0; dy <= 2.2; dy += 0.3) {
            level.sendParticles(centerParticle, x, y + dy, z, 2, 0.08, 0.05, 0.08, 0.02);
         }
      }
   }

   public static void drawParticleLine(ServerLevel level, double x1, double y1, double z1, double x2, double y2, double z2, ParticleOptions particle, int steps) {
      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         double px = x1 + t * (x2 - x1);
         double py = y1 + t * (y2 - y1);
         double pz = z1 + t * (z2 - z1);
         level.sendParticles(particle, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   public static void spawnVerticalMagicCircle(
      ServerLevel level, Vec3 center, Vec3 lookDir, ParticleOptions ringParticle, ParticleOptions runeParticle, double radius
   ) {
      int defaultColor = -11475713;
      if (ringParticle == ParticleTypes.FLAME) {
         defaultColor = -35072;
      } else if (ringParticle == ParticleTypes.ELECTRIC_SPARK) {
         defaultColor = -16718337;
      }

      spawnMagicCircleEffect(level, center.x, center.y, center.z, lookDir, (float)radius, 40, defaultColor, 1);
      Vec3 normLook = lookDir.normalize();
      Vec3 upRef = Math.abs(normLook.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = normLook.cross(upRef).normalize();
      Vec3 up = right.cross(normLook).normalize();

      for (int i = 0; i < 36; i++) {
         double angle = i * (Math.PI / 18);
         Vec3 pos = center.add(right.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
         level.sendParticles(ringParticle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      for (int i = 0; i < 24; i++) {
         double angle = i * (Math.PI / 12);
         Vec3 pos = center.add(right.scale(Math.cos(angle) * radius * 0.6)).add(up.scale(Math.sin(angle) * radius * 0.6));
         level.sendParticles(ringParticle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      if (runeParticle != null) {
         Vec3[] verts = new Vec3[6];

         for (int i = 0; i < 6; i++) {
            double a = i * (Math.PI / 3);
            verts[i] = center.add(right.scale(Math.cos(a) * radius * 0.85)).add(up.scale(Math.sin(a) * radius * 0.85));
         }

         draw3DParticleLine(level, verts[0], verts[2], runeParticle, 6);
         draw3DParticleLine(level, verts[2], verts[4], runeParticle, 6);
         draw3DParticleLine(level, verts[4], verts[0], runeParticle, 6);
         draw3DParticleLine(level, verts[1], verts[3], runeParticle, 6);
         draw3DParticleLine(level, verts[3], verts[5], runeParticle, 6);
         draw3DParticleLine(level, verts[5], verts[1], runeParticle, 6);
      }
   }

   public static void draw3DParticleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int steps) {
      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         Vec3 p = start.add(end.subtract(start).scale(t));
         level.sendParticles(particle, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   public static void spawnImpactCrater(ServerLevel level, double x, double y, double z, double radius) {
      spawnShockwaveRing(level, x, y + 0.1, z, ParticleTypes.POOF, radius, 28, 0.12);
      spawnShockwaveRing(level, x, y + 0.1, z, ParticleTypes.CAMPFIRE_COSY_SMOKE, radius * 0.7, 18, 0.08);
      spawnShockwaveRing(level, x, y + 0.1, z, ParticleTypes.CRIT, radius * 0.5, 20, 0.15);
      level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 3, 0.5, 0.2, 0.5, 0.1);
   }

   public static void renderCultivationQiAura(ServerPlayer player, SystemData data, ServerLevel sl, boolean isMeditating) {
      int realm = data.dantianRealm();
      if (realm >= 0) {
         if (realm != 0 || isMeditating) {
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();
            if (isMeditating) {
               if (player.tickCount % 4 == 0) {
                  double t = player.tickCount * 0.2;
                  double r = 1.35;

                  for (int i = 0; i < 3; i++) {
                     double angle = t + i * (Math.PI * 2.0 / 3.0);
                     double rx = Math.cos(angle) * r;
                     double rz = Math.sin(angle) * r;
                     switch (realm) {
                        case 0:
                           sl.sendParticles(ParticleTypes.ENCHANT, px + rx, py + 0.15, pz + rz, 1, 0.0, 0.02, 0.0, 0.01);
                           sl.sendParticles(ParticleTypes.END_ROD, px + rx, py + 0.1, pz + rz, 1, 0.0, 0.01, 0.0, 0.005);
                           break;
                        case 1:
                           sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px + rx, py + 0.15, pz + rz, 1, 0.0, 0.02, 0.0, 0.01);
                           break;
                        case 2:
                           sl.sendParticles(ParticleTypes.GLOW, px + rx, py + 0.2, pz + rz, 1, 0.0, 0.03, 0.0, 0.01);
                           break;
                        case 3:
                           sl.sendParticles(ParticleTypes.WAX_ON, px + rx, py + 0.25, pz + rz, 1, 0.0, 0.04, 0.0, 0.01);
                           break;
                        case 4:
                           sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px + rx, py + 0.25, pz + rz, 1, 0.0, 0.04, 0.0, 0.01);
                           break;
                        case 5:
                           sl.sendParticles(ParticleTypes.DRAGON_BREATH, px + rx, py + 0.3, pz + rz, 1, 0.0, 0.05, 0.0, 0.02);
                           break;
                        case 6:
                           sl.sendParticles(ParticleTypes.SCULK_SOUL, px + rx, py + 0.3, pz + rz, 1, 0.0, 0.05, 0.0, 0.02);
                           break;
                        case 7:
                           sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px + rx, py + 0.35, pz + rz, 2, 0.05, 0.05, 0.05, 0.02);
                           break;
                        case 8:
                           sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + rx, py + 0.4, pz + rz, 2, 0.08, 0.06, 0.08, 0.05);
                           break;
                        case 9:
                           sl.sendParticles(ParticleTypes.END_ROD, px + rx, py + 0.45, pz + rz, 2, 0.05, 0.08, 0.05, 0.02);
                           sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + rx, py + 0.2, pz + rz, 1, 0.0, 0.05, 0.0, 0.02);
                     }
                  }

                  if (player.tickCount % 8 == 0) {
                     sl.sendParticles(ParticleTypes.ENCHANT, px, py + 0.7, pz, 4, 0.4, 0.3, 0.4, 0.05);
                     if (realm >= 3) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py + 0.8, pz, 2, 0.25, 0.25, 0.25, 0.02);
                     }

                     if (realm >= 7) {
                        sl.sendParticles(ParticleTypes.END_ROD, px, py + 0.9, pz, 2, 0.2, 0.3, 0.2, 0.02);
                     }
                  }
               }
            } else if (player.tickCount % 6 == 0) {
               switch (realm) {
                  case 1:
                     sl.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        py + 0.2 + player.getRandom().nextDouble() * 0.6,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        1,
                        0.0,
                        0.02,
                        0.0,
                        0.01
                     );
                     break;
                  case 2:
                     sl.sendParticles(
                        ParticleTypes.GLOW,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.8,
                        py + 0.3 + player.getRandom().nextDouble() * 0.8,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.8,
                        1,
                        0.0,
                        0.03,
                        0.0,
                        0.01
                     );
                     break;
                  case 3:
                     sl.sendParticles(
                        ParticleTypes.WAX_ON,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        py + 0.4 + player.getRandom().nextDouble() * 0.9,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        1,
                        0.0,
                        0.03,
                        0.0,
                        0.01
                     );
                     if (player.tickCount % 12 == 0) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py + 0.8, pz, 1, 0.3, 0.4, 0.3, 0.02);
                     }
                     break;
                  case 4:
                     sl.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.85,
                        py + 0.5 + player.getRandom().nextDouble() * 1.0,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.85,
                        1,
                        0.0,
                        0.03,
                        0.0,
                        0.01
                     );
                     sl.sendParticles(ParticleTypes.ENCHANT, px, py + 1.0, pz, 2, 0.35, 0.35, 0.35, 0.05);
                     break;
                  case 5:
                     sl.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        py + 0.5 + player.getRandom().nextDouble() * 1.1,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        0.01
                     );
                     sl.sendParticles(
                        ParticleTypes.PORTAL,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        py + 0.6,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        1,
                        0.0,
                        0.05,
                        0.0,
                        0.02
                     );
                     break;
                  case 6:
                     sl.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        py + 0.6 + player.getRandom().nextDouble() * 1.0,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.9,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        0.02
                     );
                     sl.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        py + 0.4,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.7,
                        1,
                        0.0,
                        0.03,
                        0.0,
                        0.01
                     );
                     break;
                  case 7:
                     sl.sendParticles(
                        ParticleTypes.CHERRY_LEAVES,
                        px + (player.getRandom().nextDouble() - 0.5) * 1.0,
                        py + 0.8 + player.getRandom().nextDouble() * 0.8,
                        pz + (player.getRandom().nextDouble() - 0.5) * 1.0,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        0.02
                     );
                     sl.sendParticles(
                        ParticleTypes.END_ROD,
                        px + (player.getRandom().nextDouble() - 0.5) * 0.6,
                        py + 0.7,
                        pz + (player.getRandom().nextDouble() - 0.5) * 0.6,
                        1,
                        0.0,
                        0.04,
                        0.0,
                        0.01
                     );
                     break;
                  case 8:
                     sl.sendParticles(
                        ParticleTypes.TOTEM_OF_UNDYING,
                        px + (player.getRandom().nextDouble() - 0.5) * 1.1,
                        py + 0.7 + player.getRandom().nextDouble() * 0.9,
                        pz + (player.getRandom().nextDouble() - 0.5) * 1.1,
                        2,
                        0.05,
                        0.08,
                        0.05,
                        0.03
                     );
                     sl.sendParticles(ParticleTypes.END_ROD, px, py + 1.2, pz, 1, 0.3, 0.4, 0.3, 0.02);
                     break;
                  case 9:
                     double headY = player.getEyeY() + 0.45;
                     double haloAngle = player.tickCount * 0.25;
                     double hx = Math.cos(haloAngle) * 0.45;
                     double hz = Math.sin(haloAngle) * 0.45;
                     sl.sendParticles(ParticleTypes.END_ROD, px + hx, headY, pz + hz, 1, 0.0, 0.01, 0.0, 0.01);
                     sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px - hx, headY, pz - hz, 1, 0.0, 0.02, 0.0, 0.01);
                     if (player.tickCount % 12 == 0) {
                        sl.sendParticles(ParticleTypes.FLASH, px, headY + 0.1, pz, 1, 0.0, 0.0, 0.0, 0.0);
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
         SystemEvents.PlayerChargingState active = ACTIVE_CHARGES.get(player.getUUID());
         if (active != null) {
            if (skillId.equals(active.skillId)) {
               if (active.currentTicks >= Math.max(8, (int)(active.maxTicks * 0.35F))) {
                  releaseActiveCharge(player, false);
                  return true;
               }

               player.displayClientMessage(Component.translatable("awakening_system.channeling.power"), true);
               return false;
            }

            cancelChargingSkill(player, false);
         }

         return startChargingSkill(player, skillId, -1, -1);
      } else {
         return activateSkill(player, skillId, 1.0F, false);
      }
   }

   public static boolean activateSkill(ServerPlayer player, String skillId, float chargeMult, boolean isOvercharged) {
      if ((Boolean)SystemConfig.ACTIVE_SKILLS_ENABLED.get()) {
         SystemData data = data(player);
         if (data.isAnySystemUpdating()) {
            player.displayClientMessage(Component.translatable("awakening_system.action.skills_locked_maintenance"), true);
            player.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.0F);
            return false;
         } else {
            if (skillId == null || skillId.isEmpty()) {
               skillId = data.equippedSkill();
            }

            if ((skillId == null || skillId.isEmpty())
               && (data.skills().containsKey("shadow_monarch") || data.skills().containsKey("unique_absolute_extraction"))) {
               skillId = !data.skills().containsKey("unique_absolute_extraction") ? "shadow_monarch" : "unique_absolute_extraction";
            }

            if (UniqueRankManager.isUniqueSkill(skillId)) {
               MinecraftServer server = player.getServer();
               if (server != null) {
                  UniqueRankManager uniqueMgr = UniqueRankManager.get(server);
                  if (!uniqueMgr.canClaimSkill(skillId, player.getUUID())) {
                     String holder = uniqueMgr.getSkillHolderName(skillId);
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.unique_locked.title",
                        "awakening_system.notification.unique_skill_claimed",
                        -48060,
                        new Object[]{holder == null ? "Another hunter" : holder}
                     );
                     return false;
                  }

                  uniqueMgr.claimSkill(skillId, player);
               }
            }

            boolean isStoringShadows = player.isShiftKeyDown() && ("shadow_monarch".equals(skillId) || "unique_absolute_extraction".equals(skillId));
            if (isStoringShadows) {
               int stored = storePlayerShadows(player);
               if (stored <= 0) {
                  AwakeningSystemMod.notify(
                     player, "awakening_system.notification.shadow_stored.title", "awakening_system.notification.shadow_no_souls.desc", -35226, new Object[0]
                  );
               }

               return true;
            } else if (data.activateSkill(skillId, player.level().getGameTime())) {
               int level = data.skills().getOrDefault(skillId, 1);
               boolean evolved = level >= 6;
               float treeSkillMult = ClassSkillTree.getNode(skillId) == null ? 1.0F : 1.0F + (Math.min(3, Math.max(1, level)) - 1) * 0.35F;
               if (ClassSkillTree.getNode(skillId) == null) {
                  boolean var10000 = false;
               } else {
                  int var700 = (int)((Math.min(3, Math.max(1, level)) - 1) * 0.25F * 200.0F);
               }

               if (player.level() instanceof ServerLevel slMaster && ClassSkillTree.getNode(skillId) != null && level >= 3) {
                  slMaster.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                  slMaster.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 1.0, 0.6, 0.08);
                  slMaster.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.6F);
               }

               int str = data.strength();
               int intel = data.intelligence();
               int agi = data.agility();
               int vit = data.vitality();
               int will = data.willpower();
               int per = data.perception();
               switch (skillId) {
                  case "combat_instinct": {
                     int durationxxxxx = 200 + str * 2;
                     int amp = !evolved ? 0 : (str < 40 ? 1 : 2);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationxxxxx, amp, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, durationxxxxx, agi < 30 ? 0 : 1, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SWEEP_ATTACK, 2.5, 16, 0.08);
                        spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.2, 12);
                        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getEyeY() + 0.2, player.getZ(), 6, 0.25, 0.1, 0.25, 0.02);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.2F, 0.9F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8F, 1.7F);
                     }
                     break;
                  }
                  case "stone_strike": {
                     int durationxxxx = 300 + str * 3;
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, durationxxxx, !evolved ? 1 : 2, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 3.5);
                        sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.5, player.getZ(), 35, 0.4, 0.3, 0.4, 0.15);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 1.4F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.4F, 0.6F);
                     }
                     break;
                  }
                  case "enduring_flesh": {
                     int regenDur = 140 + vit * 2;
                     int resDur = 180 + vit * 2;
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDur, !evolved ? 1 : 2, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, resDur, !evolved ? 0 : 1, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.HEART, 1.8, 3.0, 4, 10);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.HAPPY_VILLAGER, 2.5, 18, 0.05);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.3F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.3F, 0.9F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);
                     }
                     break;
                  }
                  case "windrunner": {
                     int durationxxx = 260 + agi * 3;
                     player.addEffect(new MobEffectInstance(MobEffects.JUMP, durationxxx, !evolved ? 1 : 2, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, durationxxx, !evolved ? 0 : 1, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CLOUD, 2.2, 3.5, 4, 12);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SWEEP_ATTACK, 2.8, 16, 0.12);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.3F, 1.2F);
                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_1.value(),
                           SoundSource.PLAYERS,
                           1.1F,
                           1.5F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.2F, 1.3F);
                     }
                     break;
                  }
                  case "deep_breath": {
                     int durationxx = 400 + vit * 4;
                     player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, durationxx, 0, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, durationxx - 100, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.BUBBLE_COLUMN_UP, 2.5, 3, 14);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SPLASH, 3.0, 24, 0.08);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.3F, 1.1F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.1F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DOLPHIN_PLAY, SoundSource.PLAYERS, 1.0F, 1.0F);
                     }
                     break;
                  }
                  case "ore_sense": {
                     player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400 + per * 4, 0, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200 + per * 3, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0, player.getZ(), ParticleTypes.GLOW, 4.0, 28, 0.04);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0, player.getZ(), ParticleTypes.END_ROD, 7.0, 36, 0.06);
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.GLOW, 3.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.6F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.1F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.2F, 1.5F);
                     }
                     break;
                  }
                  case "shadow_step": {
                     Vec3 look = player.getLookAngle();
                     double targetDist = (!evolved ? 8.0 : 12.0) + Math.min(8.0, agi * 0.1);
                     double targetXx = player.getX() + look.x * targetDist;
                     double targetYx = player.getY() + look.y * (targetDist * 0.4);
                     double targetZx = player.getZ() + look.z * targetDist;
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SQUID_INK, 1.5, 2.5, 3, 10);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SCULK_SOUL, 2.0, 16, 0.06);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.2F, 1.4F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F
                        );
                     }

                     Vec3 safe = TeleportHelper.findSafeTeleportPos(player.level(), player, targetXx, targetYx, targetZx, player.position());
                     player.teleportTo(safe.x, safe.y, safe.z);
                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, safe.x, safe.y + 0.1, safe.z, ParticleTypes.SOUL_FIRE_FLAME, 2.5, 20, 0.08);
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, safe.x, safe.y + 1.0, safe.z, 25, 0.4, 0.6, 0.4, 0.05);
                        sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 0.8F);
                        sl.playSound(null, safe.x, safe.y, safe.z, (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.1F);
                     }
                     break;
                  }
                  case "dimensional_blink": {
                     Vec3 look = player.getLookAngle();
                     double dist = (!evolved ? 10.0 : 16.0) + Math.min(10.0, agi * 0.12);
                     double targetX = player.getX() + look.x * dist;
                     double targetY = player.getY() + look.y * (dist * 0.5);
                     double targetZ = player.getZ() + look.z * dist;
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0, player.getZ(), ParticleTypes.PORTAL, 2.5, 24, 0.1);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.3F, 1.8F);
                     }

                     Vec3 safe = TeleportHelper.findSafeTeleportPos(player.level(), player, targetX, targetY, targetZ, player.position());
                     player.teleportTo(safe.x, safe.y, safe.z);
                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, safe.x, safe.y + 1.0, safe.z, ParticleTypes.REVERSE_PORTAL, 3.0, 30, 0.12);
                        sl.sendParticles(ParticleTypes.FLASH, safe.x, safe.y + 1.0, safe.z, 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 1.1F);
                        sl.playSound(null, safe.x, safe.y, safe.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.4F, 1.4F);
                     }
                     break;
                  }
                  case "seismic_roar": {
                     double radius = (!evolved ? 6.0 : 9.0) + Math.min(4.0, str * 0.05);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = 8.0F + level * 2.0F + str * 0.2F;

                     for (LivingEntity e : enemies) {
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 1.8, 0.65, dz / distx * 1.8);
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), radius * 0.6);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.EXPLOSION, radius, 24, 0.18);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.1F, 0.6F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.8F
                        );
                     }
                     break;
                  }
                  case "blaze_ignition": {
                     double radius = (!evolved ? 7.0 : 10.0) + Math.min(5.0, intel * 0.06);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = 6.0F + level * 1.5F + intel * 0.22F;

                     for (LivingEntity e : enemies) {
                        e.setRemainingFireTicks(100 + level * 30 + intel * 2);
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, radius * 0.5, 4.5, 5, 14);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.LAVA, radius, 32, 0.12);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_WARN, SoundSource.PLAYERS, 1.1F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 0.7F);
                     }
                     break;
                  }
                  case "spectral_shift": {
                     int dur = !evolved ? 120 : 160;
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 2, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 2.5, 3, 16);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.SOUL, 3.0, 20, 0.05);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.2F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 1.5F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.3F, 1.2F
                        );
                     }
                     break;
                  }
                  case "magnetic_pull": {
                     double radius = (!evolved ? 12.0 : 18.0) + Math.min(12.0, per * 0.15);
                     AABB aabb = player.getBoundingBox().inflate(radius);

                     for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, aabb)) {
                        item.setPos(player.getX(), player.getY() + 0.5, player.getZ());
                     }

                     for (ExperienceOrb xp : player.level().getEntitiesOfClass(ExperienceOrb.class, aabb)) {
                        xp.setPos(player.getX(), player.getY() + 0.5, player.getZ());
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.REVERSE_PORTAL, radius * 0.5, 36, -0.15);
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, ParticleTypes.GLOW, 2.5);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.2F, 1.3F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.1F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.8F);
                     }
                     break;
                  }
                  case "piercing_shot": {
                     Vec3 look = player.getLookAngle();
                     Arrow arrow = new Arrow(player.level(), player, new ItemStack(Items.ARROW), null);
                     arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                     float speed = (!evolved ? 2.8F : 3.8F) + agi * 0.015F;
                     arrow.shoot(look.x, look.y, look.z, speed, 0.15F);
                     arrow.setBaseDamage((!evolved ? 9.0 : 14.0) + per * 0.15 + str * 0.1);
                     arrow.setCritArrow(true);
                     player.level().addFreshEntity(arrow);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnHelixBeam(sl, player.getEyePosition(), look, 16.0, ParticleTypes.CRIT, ParticleTypes.END_ROD, 0.3);
                        sl.sendParticles(
                           ParticleTypes.SONIC_BOOM, player.getX() + look.x * 1.5, player.getEyeY(), player.getZ() + look.z * 1.5, 1, 0.0, 0.0, 0.0, 0.0
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.1F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2F, 0.9F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.3F, 1.4F
                        );
                     }
                     break;
                  }
                  case "web_snare": {
                     double radius = (!evolved ? 5.0 : 8.0) + Math.min(4.0, intel * 0.05);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = (!evolved ? 5.0F : 8.0F) + intel * 0.12F;

                     for (LivingEntity e : enemies) {
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, !evolved ? 100 : 140, 3));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, !evolved ? 100 : 140, 1));
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ITEM_COBWEB, ParticleTypes.WHITE_ASH, radius * 0.8);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.ITEM_COBWEB, radius, 30, 0.06);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 1.2F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BAMBOO_WOOD_BREAK, SoundSource.PLAYERS, 1.3F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.1F, 0.9F);
                     }
                     break;
                  }
                  case "kinetic_blast": {
                     double radius = (!evolved ? 6.0 : 9.0) + Math.min(4.0, str * 0.05);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = 9.0F + level * 2.5F + str * 0.25F;

                     for (LivingEntity e : enemies) {
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 2.4, 0.75, dz / distx * 2.4);
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.EXPLOSION, radius * 0.7, 24, 0.2);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.SWEEP_ATTACK, radius, 32, 0.15);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.4F, 1.2F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.3F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.4F, 1.1F);
                     }
                     break;
                  }
                  case "tidal_surge": {
                     double radius = (!evolved ? 7.0 : 10.0) + Math.min(4.0, intel * 0.05);
                     player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 300, 0, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, true, true));
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float baseDmg = (!evolved ? 7.0F : 10.0F) + intel * 0.22F;

                     for (LivingEntity e : enemies) {
                        float dmg = !e.isInWaterOrRain() && !player.isInWaterOrRain() ? baseDmg : baseDmg + 8.0F;
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SPLASH, radius * 0.5, 36, 0.12);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SPLASH, radius, 48, 0.16);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.BUBBLE, radius * 0.4, 3.5, 4, 14);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.4F, 0.8F);
                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_3.value(),
                           SoundSource.PLAYERS,
                           1.2F,
                           1.1F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.3F, 0.7F);
                     }
                     break;
                  }
                  case "rulers_authority": {
                     double radius = (!evolved ? 9.0 : 13.0) + Math.min(5.0, intel * 0.06);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     Vec3 look = player.getLookAngle();
                     Vec3 pullPoint = player.position().add(look.x * 3.0, 1.0, look.z * 3.0);
                     float dmg = (!evolved ? 15.0F : 20.0F) + intel * 0.25F + str * 0.15F;

                     for (LivingEntity e : enemies) {
                        double dx = pullPoint.x - e.getX();
                        double dz = pullPoint.z - e.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 2.0, -1.8, dz / distx * 2.0);
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, radius * 0.7, 40, 0.12);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 3.0, 4.5, 5, 14);
                        sl.sendParticles(
                           ParticleTypes.SONIC_BOOM, player.getX() + look.x * 2.0, player.getEyeY(), player.getZ() + look.z * 2.0, 2, 0.0, 0.0, 0.0, 0.0
                        );
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.3F, 1.1F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.1F, 1.8F);
                     }
                     break;
                  }
                  case "asrahan_mana_blade": {
                     int durationx = !evolved ? 300 : 420;
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationx, !evolved ? 1 : 2, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationx, 0, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANTED_HIT, ParticleTypes.GLOW, 2.5);
                        spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.5, 14);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.3F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.2F, 1.2F);
                     }
                     break;
                  }
                  case "electrification": {
                     int duration = !evolved ? 220 : 320;
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, !evolved ? 1 : 2, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 1, false, true, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 2.0, 3.5, 5, 16);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 3.5, 32, 0.25);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.1F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 1.4F, 1.6F);
                     }
                     break;
                  }
                  case "nano_blade_overdrive": {
                     double radius = (!evolved ? 7.0 : 9.0) + Math.min(3.0, agi * 0.04);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = (!evolved ? 13.0F : 18.0F) + str * 0.2F + agi * 0.18F;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 1.6, 0.4, dz / distx * 1.6);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SWEEP_ATTACK, radius * 0.6, 3, 16);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.CRIT, radius, 36, 0.15);
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.6, 0.5, 0.2);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.1F, 1.6F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.2F, 1.3F);
                     }
                     break;
                  }
                  case "dragon_dive": {
                     player.setDeltaMovement(0.0, 1.45, 0.0);
                     player.hurtMarked = true;
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 90, 3, false, false, false));
                     double radius = (!evolved ? 7.0 : 10.0) + Math.min(4.0, str * 0.06);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float dmg = (!evolved ? 13.0F : 18.0F) + str * 0.25F;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 2.2, 0.6, dz / distx * 2.2);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.DRAGON_BREATH, radius * 0.6, 32, 0.12);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.EXPLOSION, radius, 24, 0.18);
                        spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), radius * 0.5);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.4F, 1.1F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.8F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.3F, 0.9F);
                     }
                     break;
                  }
                  case "black_fire_wave": {
                     Vec3 look = player.getLookAngle();
                     double range = 14.0 + Math.min(8.0, intel * 0.1);
                     float dmgx = (!evolved ? 12.0F : 16.0F) + intel * 0.22F;
                     if (player.level() instanceof ServerLevel sl) {
                        spawnHelixBeam(sl, player.getEyePosition(), look, range, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.DRAGON_BREATH, 0.6);

                        for (double d = 1.0; d <= range; d++) {
                           double fx = player.getX() + look.x * d;
                           double fy = player.getEyeY() + look.y * d - 0.2;
                           double fz = player.getZ() + look.z * d;
                           AABB hitAABB = new AABB(fx - 1.4, fy - 1.4, fz - 1.4, fx + 1.4, fy + 1.4, fz + 1.4);

                           for (LivingEntity target : sl.getEntitiesOfClass(
                              LivingEntity.class,
                              hitAABB,
                              exxx -> exxx != player
                                 && exxx.isAlive()
                                 && !exxx.getType().getCategory().isFriendly()
                                 && !exxx.getTags().contains("shadow_soldier")
                           )) {
                              target.setRemainingFireTicks(140);
                              target.hurt(player.damageSources().playerAttack(player), dmgx);
                           }
                        }

                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.6F);
                     }
                     break;
                  }
                  case "caregiver_blessing": {
                     float healAmt = (!evolved ? 10.0F : 14.0F) + vit * 0.15F;
                     player.heal(healAmt);
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 320, 0, false, true, true));
                     AABB aabb = player.getBoundingBox().inflate(12.0);

                     for (LivingEntity ally : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player
                              && (
                                 ex instanceof TamableAnimal t && player.getUUID().equals(t.getOwnerUUID())
                                    || ex instanceof ServerPlayer
                                    || ex.getTags().contains("shadow_soldier")
                              )
                        )) {
                        ally.heal(healAmt);
                        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1));
                        ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.HEART, 4.0);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 2.5, 4.0, 4, 12);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.3F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.3F);
                     }
                     break;
                  }
                  case "shadow_monarch":
                  case "unique_absolute_extraction": {
                     boolean isUnique = "unique_absolute_extraction".equals(skillId);
                     double searchRadius = !evolved && !isUnique ? 32.0 : 48.0;
                     ServerLevel sl = (ServerLevel)player.level();
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.ARISE_VOICE.get(), SoundSource.PLAYERS, 1.8F, 1.0F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.SHADOW_EXTRACT.get(), SoundSource.PLAYERS, 1.3F, 1.0F
                     );
                     List<ShadowSoulEntity> nearbySouls = sl.getEntitiesOfClass(
                        ShadowSoulEntity.class,
                        player.getBoundingBox().inflate(searchRadius),
                        s -> s.isAlive() && (s.getOwnerUUID() == null || s.getOwnerUUID().equals(player.getUUID()))
                     );
                     List<Entity> nearbyCorpses = IceAndFireCompat.findNearbyCorpses(sl, player, searchRadius);
                     if (nearbySouls.isEmpty() && nearbyCorpses.isEmpty()) {
                        if (data.storedShadowCount() <= 0) {
                           AwakeningSystemMod.notify(
                              player,
                              "awakening_system.notification.shadow_no_souls.title",
                              "awakening_system.notification.shadow_no_souls.desc",
                              -35226,
                              new Object[0]
                           );
                        } else {
                           int toDeploy = !isUnique && !evolved ? 2 : 4;
                           deployPlayerShadows(player, toDeploy);
                        }
                     } else {
                        int maxExtract = !isUnique && !evolved ? 4 : 8;
                        int extracted = 0;

                        for (ShadowSoulEntity soul : nearbySouls) {
                           if (extracted >= maxExtract) {
                              break;
                           }

                           int currentAttempt = soul.getExtractionAttempts() + 1;
                           String entityId = soul.getMobEntityTypeId().toLowerCase();
                           if (!entityId.contains("celestial_dragon")) {
                              int mobTier = 3;
                              if (entityId.contains("boss")
                                 || entityId.contains("dragon")
                                 || entityId.contains("wither")
                                 || entityId.contains("warden")
                                 || entityId.contains("baran")
                                 || entityId.contains("igris")) {
                                 mobTier = 9;
                              } else if (entityId.contains("golem") || entityId.contains("ravager") || entityId.contains("evoker")) {
                                 mobTier = 6;
                              }

                              double bonus = !isUnique ? (!evolved ? 0.0 : 25.0) : 40.0;
                              double chance = intel * 0.35 + will * 0.35 + bonus - mobTier * 6.0 + 35.0;
                              chance = Math.max(15.0, Math.min(95.0, chance));
                              if (!(player.getRandom().nextDouble() * 100.0 < chance)) {
                                 soul.incrementExtractionAttempts();
                                 if (currentAttempt >= 3) {
                                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, soul.getX(), soul.getY() + 0.5, soul.getZ(), 30, 0.4, 0.4, 0.4, 0.05);
                                    sl.playSound(null, soul.getX(), soul.getY(), soul.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.9F, 0.7F);
                                    player.displayClientMessage(
                                       Component.translatable("awakening_system.shadow.arise_final_fail", new Object[]{soul.getMobNameComponent()}), true
                                    );
                                    soul.discard();
                                 } else {
                                    sl.sendParticles(ParticleTypes.SCULK_SOUL, soul.getX(), soul.getY() + 0.4, soul.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
                                    sl.playSound(
                                       null,
                                       soul.getX(),
                                       soul.getY(),
                                       soul.getZ(),
                                       (SoundEvent)SoundEvents.SOUL_ESCAPE.value(),
                                       SoundSource.PLAYERS,
                                       1.0F,
                                       0.9F
                                    );
                                    soul.setCustomName(
                                       Component.translatable(
                                          "awakening_system.shadow.soul_name_attempts", new Object[]{soul.getMobNameComponent(), currentAttempt, 3}
                                       )
                                    );
                                    player.displayClientMessage(
                                       Component.translatable(
                                          "awakening_system.shadow.arise_attempt_fail", new Object[]{currentAttempt, soul.getMobNameComponent()}
                                       ),
                                       true
                                    );
                                 }
                              } else {
                                 summonShadowFromSoul(player, soul);
                                 soul.discard();
                                 extracted++;
                                 player.displayClientMessage(
                                    Component.translatable("awakening_system.shadow.arise_chat_success", new Object[]{soul.getMobNameComponent()}), true
                                 );
                              }
                           } else {
                              player.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
                              soul.discard();
                           }
                        }

                        for (Entity corpse : nearbyCorpses) {
                           if (extracted >= maxExtract) {
                              break;
                           }

                           boolean success = IceAndFireCompat.attemptExtractCorpse(player, corpse, isUnique, intel, will);
                           if (success) {
                              extracted++;
                           }
                        }

                        for (LivingEntity enemy : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(searchRadius * 0.85),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier") && !exxx.getType().getCategory().isFriendly()
                        )) {
                           double dx = enemy.getX() - player.getX();
                           double dz = enemy.getZ() - player.getZ();
                           double d = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                           enemy.setDeltaMovement(dx / d * 1.8, 0.5, dz / d * 1.8);
                        }

                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, false, false));
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SCULK_SOUL, 6.0, 32, 0.08);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 12.0, 48, 0.12);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SQUID_INK, 18.0, 64, 0.15);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.5, 5.0, 6, 14);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.2F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.5F);
                        AwakeningSystemMod.notify(
                           player,
                           "awakening_system.notification.shadow_extracted.title",
                           "awakening_system.notification.shadow_extracted.message",
                           -6723841,
                           new Object[]{String.valueOf(extracted)}
                        );
                     }
                     break;
                  }
                  case "unique_rulers_authority_prime": {
                     double radius = 18.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getTags().contains("shadow_soldier") && !ex.getType().getCategory().isFriendly()
                        );
                     float dmg = 26.0F + str * 0.32F + intel * 0.28F;

                     for (LivingEntity e : enemies) {
                        e.setDeltaMovement(0.0, 1.5, 0.0);
                        e.hurt(player.damageSources().playerAttack(player), dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SONIC_BOOM, 6.0, 16, 0.1);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SONIC_BOOM, 12.0, 24, 0.15);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 18.0, 64, 0.2);
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.ELECTRIC_SPARK, 5.0);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 0.9F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.3F, 0.6F);
                     }
                     break;
                  }
                  case "unique_fourth_wall_infinite": {
                     player.removeAllEffects();
                     player.heal(player.getMaxHealth() * 0.5F);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 4, false, false, false));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 320, 4, false, false, false));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 2, false, false, false));
                     double radius = 12.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);

                     for (LivingEntity e : player.level()
                        .getEntitiesOfClass(LivingEntity.class, aabb, ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly())) {
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3));
                        e.hurt(player.damageSources().playerAttack(player), 14.0F + will * 0.35F);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 6.0, 5, 24);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.ENCHANT, 10.0, 48, 0.1);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.2, 1.2, 1.2, 0.2);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.6F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 1.2F);
                     }
                     break;
                  }
                  case "unique_dragon_breath_annihilation": {
                     Vec3 look = player.getLookAngle();
                     float dmg = 30.0F + str * 0.38F + intel * 0.25F;
                     if (player.level() instanceof ServerLevel sl) {
                        spawnHelixBeam(sl, player.getEyePosition(), look, 26.0, ParticleTypes.DRAGON_BREATH, ParticleTypes.SOUL_FIRE_FLAME, 0.9);

                        for (double d = 1.0; d <= 26.0; d += 0.8) {
                           double fx = player.getX() + look.x * d;
                           double fy = player.getEyeY() + look.y * d - 0.2;
                           double fz = player.getZ() + look.z * d;
                           sl.sendParticles(ParticleTypes.FLAME, fx, fy, fz, 12, 0.3, 0.3, 0.3, 0.05);
                           AABB hitAABB = new AABB(fx - 1.6, fy - 1.6, fz - 1.6, fx + 1.6, fy + 1.6, fz + 1.6);

                           for (LivingEntity target : sl.getEntitiesOfClass(
                              LivingEntity.class,
                              hitAABB,
                              exxx -> exxx != player
                                 && exxx.isAlive()
                                 && !exxx.getType().getCategory().isFriendly()
                                 && !exxx.getTags().contains("shadow_soldier")
                           )) {
                              target.setRemainingFireTicks(220);
                              target.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                              target.hurt(player.damageSources().playerAttack(player), dmg);
                              target.setDeltaMovement(look.x * 1.6, 0.5, look.z * 1.6);
                           }
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, 8.0, 24, 0.15);
                        sl.sendParticles(
                           ParticleTypes.FLASH,
                           player.getX() + look.x * 2.0,
                           player.getEyeY() + look.y * 2.0,
                           player.getZ() + look.z * 2.0,
                           3,
                           0.0,
                           0.0,
                           0.0,
                           0.0
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 1.5F, 1.4F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.6F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.3F, 0.8F);
                     }
                     break;
                  }
                  case "isekai_predator": {
                     double radius = 12.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float drainHp = 0.0F;

                     for (LivingEntity e : enemies) {
                        float dmg = 24.0F + intel * 0.25F;
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
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.REVERSE_PORTAL, radius * 0.6, 40, -0.2);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.0, 4.0, 5, 14);
                        sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0, player.getZ(), 50, 1.2, 1.0, 1.2, 0.1);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_DEATH, SoundSource.PLAYERS, 1.3F, 1.2F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.4F, 0.7F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.8F
                        );
                     }
                     break;
                  }
                  case "isekai_dimensional_slash": {
                     double radius = 16.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float atomicDmg = 120.0F + str * 0.45F + intel * 0.4F;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().magic(), atomicDmg);
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 3.0, 1.2, dz / distx * 3.0);
                        e.setRemainingFireTicks(160);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 8.0, 6, 28);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SONIC_BOOM, 10.0, 20, 0.2);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, 16.0, 32, 0.25);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4F, 0.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.7F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.5F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 0.6F);
                     }
                     break;
                  }
                  case "isekai_dragon_roar": {
                     double radius = 18.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float roarDmg = 45.0F + will * 0.35F + str * 0.2F;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().playerAttack(player), roarDmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, 6.0, 20, 0.12);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.DRAGON_BREATH, 12.0, 36, 0.18);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLAME, 18.0, 50, 0.2);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.6F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 0.9F);
                     }
                     break;
                  }
                  case "isekai_mana_burst": {
                     double radius = 14.0 + Math.min(6.0, Math.max(0, intel - 10) * 0.08);
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float intScale = 1.0F + Math.max(0, intel - 10) * 0.035F;
                     boolean manaSurge = data.energy() >= data.maxEnergy() * 0.7F;
                     float surgeBonus = !manaSurge ? 1.0F : 1.2F;
                     float burstDmg = (60.0F + intel * 0.5F + will * 0.35F) * intScale * surgeBonus * chargeMult;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().magic(), burstDmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                        e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                        double dx = e.getX() - player.getX();
                        double dz = e.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        e.setDeltaMovement(dx / distx * 2.4, 0.9, dz / distx * 2.4);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        double circleRadius = Math.min(8.0, 4.5 + Math.max(0, intel - 10) * 0.05);
                        spawnGeometricMagicCircle(
                           sl,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           ParticleTypes.ENCHANTED_HIT,
                           ParticleTypes.TOTEM_OF_UNDYING,
                           ParticleTypes.FLASH,
                           circleRadius
                        );
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FIREWORK, 7.5, 4, 22);
                        if (isOvercharged) {
                           spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLASH, 12.0, 32, 0.2);
                        }

                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.4F, 1.2F
                        );
                     }

                     Component surgeTag = !manaSurge ? Component.empty() : Component.translatable("awakening_system.skill.tag.mana_surge_20");
                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.max_release");
                     player.displayClientMessage(
                        Component.translatable(
                           "awakening_system.skill.isekai_mana_burst.msg", new Object[]{String.format(Locale.US, "%.0f", burstDmg), surgeTag, chargeTag}
                        ),
                        true
                     );
                     break;
                  }
                  case "shonen_infinite_void": {
                     double radius = !isOvercharged ? 24.0 : 32.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);
                     List<LivingEntity> enemies = player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        );
                     float voidDmg = (70.0F + intel * 0.45F) * chargeMult;

                     for (LivingEntity e : enemies) {
                        e.hurt(player.damageSources().magic(), voidDmg);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 255));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 255));
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0));
                        e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 180, 0));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                     data.restoreEnergy(!isOvercharged ? 50 : 75);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, radius, 8, 48);
                        spawnGeometricMagicCircle(
                           sl,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           ParticleTypes.PORTAL,
                           ParticleTypes.SCULK_SOUL,
                           ParticleTypes.END_ROD,
                           radius * 0.75
                        );
                        if (isOvercharged) {
                           spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLASH, radius * 0.9, 40, 0.3);
                        }

                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.8F, 0.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.4F);
                     }

                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.absolute_domain");
                     player.displayClientMessage(Component.translatable("awakening_system.skill.infinite_void.msg", new Object[]{chargeTag}), true);
                     break;
                  }
                  case "shonen_getsuga_tensho": {
                     Vec3 look = player.getLookAngle().normalize();
                     Vec3 start = player.getEyePosition();
                     float slashDmg = 75.0F + str * 0.55F + intel * 0.35F;
                     double maxRange = 26.0;
                     if (player.level() instanceof ServerLevel sl) {
                        Vec3 up = new Vec3(0.0, 1.0, 0.0);
                        Vec3 right = look.cross(up).normalize();

                        for (double d = 1.0; d <= maxRange; d += 0.8) {
                           Vec3 center = start.add(look.scale(d));

                           for (double arc = -2.5; arc <= 2.5; arc += 0.4) {
                              Vec3 arcPos = center.add(right.scale(arc)).add(0.0, (1.0 - arc * arc / 6.25) * 1.5, 0.0);
                              sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, arcPos.x, arcPos.y, arcPos.z, 2, 0.05, 0.05, 0.05, 0.02);
                              sl.sendParticles(ParticleTypes.CRIMSON_SPORE, arcPos.x, arcPos.y, arcPos.z, 2, 0.05, 0.05, 0.05, 0.02);
                              sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, arcPos.x, arcPos.y, arcPos.z, 1, 0.02, 0.02, 0.02, 0.05);
                           }
                        }

                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(maxRange),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getType().getCategory().isFriendly() && !exxx.getTags().contains("shadow_soldier")
                        )) {
                           Vec3 toEnt = exx.position().subtract(start);
                           double proj = toEnt.dot(look);
                           if (proj > 0.0 && proj <= maxRange) {
                              Vec3 closest = start.add(look.scale(proj));
                              if (exx.position().distanceTo(closest) <= 3.2) {
                                 exx.hurt(player.damageSources().playerAttack(player), slashDmg);
                                 exx.setDeltaMovement(look.x * 2.2, 0.8, look.z * 2.2);
                                 exx.setRemainingFireTicks(120);
                              }
                           }
                        }

                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_3.value(),
                           SoundSource.PLAYERS,
                           1.5F,
                           1.2F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.6F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.1F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.getsuga_tensho.msg"), true);
                     break;
                  }
                  case "shonen_conquerors_haki": {
                     double radius = 20.0;
                     AABB aabb = player.getBoundingBox().inflate(radius);

                     for (LivingEntity e : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           aabb,
                           ex -> ex != player && ex.isAlive() && !ex.getType().getCategory().isFriendly() && !ex.getTags().contains("shadow_soldier")
                        )) {
                        if (!(e.getMaxHealth() <= 50.0F)) {
                           float shockDmg = 40.0F + will * 0.45F;
                           e.hurt(player.damageSources().magic(), shockDmg);
                           e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                           e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 3));
                        } else {
                           e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 255));
                           e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 255));
                           e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 1));
                           e.setDeltaMovement(0.0, -0.4, 0.0);
                        }
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnGeometricMagicCircle(
                           sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.SONIC_BOOM, 16.0
                        );
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SONIC_BOOM, 14.0, 24, 0.2);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.CAMPFIRE_COSY_SMOKE, 20.0, 36, 0.15);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.6F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4F, 0.7F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.conquerors_haki.msg"), true);
                     break;
                  }
                  case "shonen_zoltraak": {
                     Vec3 look = player.getLookAngle().normalize();
                     Vec3 up = new Vec3(0.0, 1.0, 0.0);
                     Vec3 right = look.cross(up);
                     right = !(right.lengthSqr() < 0.001) ? right.normalize() : new Vec3(1.0, 0.0, 0.0);
                     boolean isLeft = player.getMainArm() == HumanoidArm.LEFT;
                     double armSide = !isLeft ? 0.28 : -0.28;
                     double armDist = 1.35;
                     double armDown = 0.2;
                     Vec3 origin = player.position().add(0.0, player.getEyeHeight() - armDown, 0.0).add(look.scale(armDist)).add(right.scale(armSide));
                     double maxRange = !isOvercharged ? 32.0 : 40.0;
                     Vec3 end = origin.add(look.scale(maxRange));
                     float beamDmg = (90.0F + intel * 0.6F) * chargeMult;
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVerticalMagicCircle(sl, origin, look, ParticleTypes.END_ROD, ParticleTypes.ENCHANT, !isOvercharged ? 2.4 : 3.6);
                        draw3DParticleLine(sl, origin, end, ParticleTypes.FLASH, !isOvercharged ? 18 : 28);
                        draw3DParticleLine(sl, origin, end, ParticleTypes.END_ROD, !isOvercharged ? 36 : 50);
                        draw3DParticleLine(sl, origin, end, ParticleTypes.FIREWORK, !isOvercharged ? 28 : 40);
                        if (isOvercharged) {
                           draw3DParticleLine(sl, origin, end, ParticleTypes.SONIC_BOOM, 14);
                        }

                        for (LivingEntity ex : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(maxRange),
                           exx -> exx != player && exx.isAlive() && !exx.getType().getCategory().isFriendly() && !exx.getTags().contains("shadow_soldier")
                        )) {
                           Vec3 toEnt = ex.position().subtract(origin);
                           double proj = toEnt.dot(look);
                           if (proj > 0.0 && proj <= maxRange) {
                              Vec3 closest = origin.add(look.scale(proj));
                              if (ex.position().distanceTo(closest) <= (!isOvercharged ? 2.2 : 3.2)) {
                                 ex.hurt(player.damageSources().magic(), beamDmg);
                                 ex.setDeltaMovement(look.x * (!isOvercharged ? 2.5 : 3.2), 0.6, look.z * (!isOvercharged ? 2.5 : 3.2));
                              }
                           }
                        }

                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.6F, 0.6F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.8F
                        );
                        if (isOvercharged) {
                           sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.5F);
                        }
                     }

                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.overcharge_50");
                     player.displayClientMessage(Component.translatable("awakening_system.skill.zoltraak.msg", new Object[]{chargeTag}), true);
                     break;
                  }
                  case "shonen_rasenshuriken": {
                     Vec3 look = player.getLookAngle().normalize();
                     double throwDist = 16.0;
                     Vec3 impactPos = player.getEyePosition().add(look.scale(throwDist));
                     float windDmg = 65.0F + agi * 0.35F + intel * 0.3F;
                     if (player.level() instanceof ServerLevel sl) {
                        for (double s = 1.0; s <= throwDist; s++) {
                           Vec3 pos = player.getEyePosition().add(look.scale(s));
                           sl.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 5, 0.3, 0.3, 0.3, 0.05);
                           sl.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 2, 0.2, 0.2, 0.2, 0.02);
                        }

                        spawnExpandingDome(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.CLOUD, 8.0, 6, 32);
                        spawnShockwaveRing(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.SWEEP_ATTACK, 8.0, 28, 0.25);
                        spawnShockwaveRing(sl, impactPos.x, impactPos.y, impactPos.z, ParticleTypes.EXPLOSION, 6.0, 20, 0.2);

                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           new AABB(impactPos.x - 8.0, impactPos.y - 8.0, impactPos.z - 8.0, impactPos.x + 8.0, impactPos.y + 8.0, impactPos.z + 8.0),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getType().getCategory().isFriendly() && !exxx.getTags().contains("shadow_soldier")
                        )) {
                           exx.hurt(player.damageSources().playerAttack(player), windDmg);
                           double dx = impactPos.x - exx.getX();
                           double dz = impactPos.z - exx.getZ();
                           exx.setDeltaMovement(dx * 0.3, 1.1, dz * 0.3);
                           exx.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 1));
                           exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        }

                        sl.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.6F, 1.4F);
                        sl.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.5F, 1.8F);
                        sl.playSound(
                           null, impactPos.x, impactPos.y, impactPos.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.9F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.rasenshuriken.msg"), true);
                     break;
                  }
                  case "shadow_exchange": {
                     ServerLevel sl = (ServerLevel)player.level();
                     String masterTag = "shadow_master_" + player.getUUID();
                     List<Mob> shadows = sl.getEntitiesOfClass(
                        Mob.class,
                        player.getBoundingBox().inflate(64.0),
                        m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
                     );
                     if (shadows.isEmpty()) {
                        Vec3 lookx = player.getLookAngle();
                        double txx = player.getX() + lookx.x * 14.0;
                        double tyx = player.getY() + lookx.y * 14.0;
                        double tzx = player.getZ() + lookx.z * 14.0;
                        player.teleportTo(txx, tyx, tzx);
                        spawnShockwaveRing(sl, txx, tyx + 0.1, tzx, ParticleTypes.SOUL_FIRE_FLAME, 2.5, 24, 0.08);
                        sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.8, 0.5, 0.05);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 0.7F);
                     } else {
                        Mob target = shadows.get(0);
                        double px = player.getX();
                        double py = player.getY();
                        double pz = player.getZ();
                        player.teleportTo(target.getX(), target.getY(), target.getZ());
                        target.teleportTo(px, py, pz);
                        spawnShockwaveRing(sl, px, py + 0.1, pz, ParticleTypes.SOUL_FIRE_FLAME, 2.5, 24, 0.08);
                        spawnShockwaveRing(sl, target.getX(), target.getY() + 0.1, target.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.5, 24, 0.08);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 0.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0F, 1.5F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.2F
                        );
                     }
                     break;
                  }
                  case "monarchs_domain": {
                     ServerLevel sl = (ServerLevel)player.level();
                     AABB aabb = player.getBoundingBox().inflate(20.0);

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        aabb,
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier") && !exxx.getType().getCategory().isFriendly()
                     )) {
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                        exx.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0));
                        exx.hurt(player.damageSources().magic(), 25.0F + intel * 0.3F);
                     }

                     String mTag = "shadow_master_" + player.getUUID();

                     for (Mob s : sl.getEntitiesOfClass(
                        Mob.class, aabb, m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(mTag)
                     )) {
                        s.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                        s.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                        s.heal(20.0F);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SCULK_SOUL, 6.0, 32, 0.08);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 12.0, 48, 0.12);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SQUID_INK, 18.0, 64, 0.15);
                     spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SCULK_SOUL, 3.5, 5.0, 6, 12);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.7F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.5F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.2F, 0.8F);
                     break;
                  }
                  case "shadow_colossus_infusion": {
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL, 2.5, 4.0, 5, 14);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 4.0, 28, 0.1);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.2F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.3F, 1.1F
                        );
                     }
                     break;
                  }
                  case "stealth_stalker": {
                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, false, true));
                     player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, true));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SQUID_INK, 2.5, 24, 0.08);
                        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.6, 0.4, 0.03);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.2F, 1.6F);
                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.ARMOR_EQUIP_LEATHER.value(),
                           SoundSource.PLAYERS,
                           1.2F,
                           1.4F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.8F);
                     }
                     break;
                  }
                  case "bloodlust_gaze": {
                     ServerLevel sl = (ServerLevel)player.level();

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(14.0),
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                     )) {
                        exx.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4));
                        exx.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, exx.getX(), exx.getEyeY(), exx.getZ(), 8, 0.3, 0.2, 0.3, 0.02);
                     }

                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.ANGRY_VILLAGER, 4.0, 24, 0.08);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.3F, 1.3F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.4F, 1.4F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.8F);
                     break;
                  }
                  case "plum_blossom_rain": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();
                     Vec3 center = player.position().add(look.scale(3.0));
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(center.x - 4.0, center.y - 2.0, center.z - 4.0, center.x + 4.0, center.y + 3.0, center.z + 4.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (40.0F + str * 0.5F + agi * 0.3F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                        exx.setDeltaMovement(exx.getDeltaMovement().add(look.x * 0.8, 0.4, look.z * 0.8));
                     }

                     spawnVortexPillar(sl, center.x, center.y, center.z, ParticleTypes.CHERRY_LEAVES, 3.5, 4.0, 5, 18);
                     spawnBurstCross(sl, center.x, center.y, center.z, ParticleTypes.SWEEP_ATTACK, 4.0, 14);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.6F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.3F, 1.4F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.8F);
                     break;
                  }
                  case "twenty_four_sword_forms": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(7.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (30.0F + str * 0.4F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.SWEEP_ATTACK, 4.5, 28, 0.12);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.CRIT, 5.0, 32, 0.18);
                     sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0, player.getZ(), 45, 1.5, 0.8, 1.5, 0.15);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.5F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.4F, 1.2F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.4F);
                     break;
                  }
                  case "plum_blossom_domain": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 360, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 360, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.CHERRY_LEAVES, 4.0, 32, 0.05);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.CHERRY_LEAVES, 8.0, 48, 0.08);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CHERRY_LEAVES, 4.5, 5.0, 6, 16);
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 60, 2.0, 1.0, 2.0, 0.1);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.6F);
                     }
                     break;
                  }
                  case "daoist_clarity": {
                     player.removeAllEffects();
                     player.heal(player.getMaxHealth() * 0.35F);
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.HAPPY_VILLAGER, 3.0, 3, 16);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.END_ROD, 3.5, 24, 0.05);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.6F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.7F);
                     }
                     break;
                  }
                  case "readers_viewpoint": {
                     ServerLevel sl = (ServerLevel)player.level();

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(32.0),
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                     )) {
                        exx.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                     spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.END_ROD, 3.5);
                     sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.2, player.getZ(), 40, 0.6, 0.8, 0.6, 0.1);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.4F, 1.4F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.3F, 1.8F);
                     break;
                  }
                  case "constellation_favor": {
                     int coinGift = 25 + player.getRandom().nextInt(25);
                     data.addCoins(coinGift);
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 2));
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.star_stream_sponsor.title",
                        "awakening_system.notification.star_stream_sponsor.desc",
                        -10496,
                        new Object[]{coinGift}
                     );
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 2.5, 4.0, 4, 14);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.END_ROD, 3.5, 24, 0.08);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.2F);
                     }
                     break;
                  }
                  case "fourth_wall_shatter": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(12.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (45.0F + intel * 0.4F + will * 0.3F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        double dx = exx.getX() - player.getX();
                        double dz = exx.getZ() - player.getZ();
                        double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                        exx.setDeltaMovement(dx / distx * 1.8, 0.6, dz / distx * 1.8);
                     }

                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.END_ROD, 5.0, 36, 0.1);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.ENCHANT, 10.0, 48, 0.15);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.5, 0.5, 0.5, 0.05);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.6F, 0.7F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.2F, 1.1F);
                     break;
                  }
                  case "star_stream_climax": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(18.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier") && !exx.getType().getCategory().isFriendly()
                     );
                     float dmg = (65.0F + intel * 0.6F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        exx.setRemainingFireTicks(100);
                        sl.sendParticles(ParticleTypes.FLASH, exx.getX(), exx.getY() + 1.0, exx.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.sendParticles(ParticleTypes.END_ROD, exx.getX(), exx.getY() + 1.0, exx.getZ(), 25, 0.4, 1.0, 0.4, 0.1);
                     }

                     spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 5.0, 12.0, 8, 20);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, 14.0, 50, 0.12);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0, player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.3F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 1.4F);
                     break;
                  }
                  case "aura_smash": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(8.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (45.0F + str * 0.6F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                        exx.setDeltaMovement(0.0, 0.9, 0.0);
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                     }

                     spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 4.0);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.EXPLOSION, 7.0, 20, 0.15);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.5F, 0.7F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.1F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.3F, 0.7F);
                     break;
                  }
                  case "cross_impact": {
                     Vec3 look = player.getLookAngle();
                     player.setDeltaMovement(look.x * 1.6, 0.4, look.z * 1.6);
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(6.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (50.0F + str * 0.7F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     spawnBurstCross(sl, player.getX() + look.x * 2.0, player.getY(), player.getZ() + look.z * 2.0, ParticleTypes.SWEEP_ATTACK, 3.5, 16);
                     sl.sendParticles(
                        ParticleTypes.CRIT, player.getX() + look.x * 1.5, player.getY() + 1.0, player.getZ() + look.z * 1.5, 25, 0.5, 0.5, 0.5, 0.1
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.4F, 1.3F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.3F, 1.1F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.4F);
                     break;
                  }
                  case "valterian_gigantomachia": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 4));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 3.5, 6.0, 6, 16);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.LAVA, 6.0, 32, 0.1);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.EXPLOSION, 9.0, 24, 0.15);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5F, 0.7F);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.9F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.7F);
                     }
                     break;
                  }
                  case "iron_muscle_hardening": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 260, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 260, 2));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.5, 3, 16);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.ARMOR_EQUIP_NETHERITE.value(),
                           SoundSource.PLAYERS,
                           1.3F,
                           1.2F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2F, 1.3F);
                     }
                     break;
                  }
                  case "heavenly_demon_steps": {
                     Vec3 look = player.getLookAngle();
                     double tx = player.getX() + look.x * 12.0;
                     double ty = player.getY() + look.y * 12.0;
                     double tz = player.getZ() + look.z * 12.0;
                     ServerLevel sl = (ServerLevel)player.level();
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 2.5, 24, 0.08);
                     player.teleportTo(tx, ty, tz);
                     spawnShockwaveRing(sl, tx, ty + 0.1, tz, ParticleTypes.FLAME, 3.0, 28, 0.1);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 1.2F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.5F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.1F, 0.9F);
                     break;
                  }
                  case "demonic_blood_slash": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();
                     AABB aabb = player.getBoundingBox().inflate(8.0);
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class, aabb, exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (50.0F + str * 0.4F + data.dantianRealm() * 5.0F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        exx.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                     }

                     spawnBurstCross(sl, player.getX() + look.x * 2.5, player.getY(), player.getZ() + look.z * 2.5, ParticleTypes.CRIMSON_SPORE, 4.0, 18);
                     sl.sendParticles(
                        ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 2.0, player.getY() + 1.0, player.getZ() + look.z * 2.0, 10, 0.5, 0.3, 0.5, 0.05
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 1.4F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 1.1F, 1.2F);
                     break;
                  }
                  case "sky_demon_nine_reversals": {
                     data.addCultivationQi(150);
                     player.heal(player.getMaxHealth() * 0.3F);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                     ServerLevel sl = (ServerLevel)player.level();

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(10.0),
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                     )) {
                        exx.hurt(player.damageSources().magic(), 40.0F * treeSkillMult);
                        exx.setRemainingFireTicks(100);
                     }

                     spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.SOUL_FIRE_FLAME, 4.0, 7.0, 7, 18);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.CRIMSON_SPORE, 8.0, 40, 0.12);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 0.9F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.5F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.3F, 1.2F);
                     break;
                  }
                  case "nano_overdrive_suit": {
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 500, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 2.5, 4, 18);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.1F, 2.0F);
                     }
                     break;
                  }
                  case "shadow_step_blink": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(16.0),
                        ex -> ex != player && ex.isAlive() && !ex.getTags().contains("shadow_soldier")
                     );
                     if (enemies.isEmpty()) {
                        Vec3 lookx = player.getLookAngle();
                        player.teleportTo(player.getX() + lookx.x * 10.0, player.getY() + lookx.y * 10.0, player.getZ() + lookx.z * 10.0);
                     } else {
                        LivingEntity target = enemies.get(0);
                        Vec3 dir = target.getLookAngle().scale(-1.8);
                        player.teleportTo(target.getX() + dir.x, target.getY(), target.getZ() + dir.z);
                        player.setYRot(target.getYRot());
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.PORTAL, 2.5, 24, 0.08);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.3F, 1.5F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.2F, 1.8F);
                     break;
                  }
                  case "vital_strike": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (45.0F + agi * 0.7F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                        exx.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                     }

                     spawnBurstCross(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, 2.8, 14);
                     sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.5F, 1.4F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.2F, 1.5F);
                     break;
                  }
                  case "infinite_mirror_blades": {
                     ServerLevel sl = (ServerLevel)player.level();
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(8.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (55.0F + agi * 0.5F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().playerAttack(player), dmg);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.SWEEP_ATTACK, 4.0, 24, 0.15);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 1.0, player.getZ(), ParticleTypes.SWEEP_ATTACK, 6.0, 32, 0.2);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 1.5F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.4F, 1.8F);
                     break;
                  }
                  case "smoke_bomb_escape": {
                     ServerLevel sl = (ServerLevel)player.level();

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(8.0),
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                     )) {
                        exx.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2));
                     Vec3 look = player.getLookAngle();
                     player.setDeltaMovement(-look.x * 1.5, 0.5, -look.z * 1.5);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.CAMPFIRE_COSY_SMOKE, 5.0, 36, 0.08);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.4F, 1.1F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.2F, 1.3F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1.3F, 0.9F);
                     break;
                  }
                  case "elemental_spear": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();
                     double range = !isOvercharged ? Math.min(26.0, 14.0 + Math.max(0, intel - 10) * 0.12) : 36.0;
                     Vec3 center = player.position().add(look.scale(range * 0.4));
                     AABB aabb = new AABB(center.x - 4.0, center.y - 3.0, center.z - 4.0, center.x + 4.0, center.y + 3.0, center.z + 4.0);
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class, aabb, exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float intMult = 1.0F + Math.max(0, intel - 10) * 0.035F;
                     boolean manaSurge = data.energy() >= data.maxEnergy() * 0.7F;
                     float surgeBonus = !manaSurge ? 1.0F : 1.2F;
                     float dmg = (55.0F + intel * 0.7F) * treeSkillMult * intMult * surgeBonus * chargeMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        exx.setRemainingFireTicks(!isOvercharged ? 90 : 160);
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, !isOvercharged ? 2 : 3));
                     }

                     Vec3 normLook = look.normalize();
                     Vec3 up = new Vec3(0.0, 1.0, 0.0);
                     Vec3 right = normLook.cross(up);
                     right = !(right.lengthSqr() < 0.001) ? right.normalize() : new Vec3(1.0, 0.0, 0.0);
                     boolean isLeft = player.getMainArm() == HumanoidArm.LEFT;
                     double armSide = !isLeft ? 0.28 : -0.28;
                     double armDist = 1.35;
                     double armDown = 0.2;
                     Vec3 armOrigin = player.position().add(0.0, player.getEyeHeight() - armDown, 0.0).add(normLook.scale(armDist)).add(right.scale(armSide));
                     spawnVerticalMagicCircle(sl, armOrigin, normLook, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME, !isOvercharged ? 1.8 : 2.8);
                     spawnHelixBeam(sl, armOrigin, normLook, range, ParticleTypes.FLAME, ParticleTypes.ELECTRIC_SPARK, !isOvercharged ? 0.6 : 0.9);
                     if (!isOvercharged) {
                        sl.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 3, 0.0, 0.0, 0.0, 0.0);
                     } else {
                        sl.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 6, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.6F, 1.2F);
                     }

                     sl.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.4F, 1.4F);
                     sl.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.3F, 1.2F);
                     sl.playSound(null, center.x, center.y, center.z, (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                     Component surgeTag = !manaSurge ? Component.empty() : Component.translatable("awakening_system.skill.tag.mana_surge_20");
                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.max_release_50");
                     player.displayClientMessage(
                        Component.translatable(
                           "awakening_system.skill.elemental_spear.msg", new Object[]{String.format(Locale.US, "%.0f", dmg), surgeTag, chargeTag}
                        ),
                        true
                     );
                     break;
                  }
                  case "gravity_crush": {
                     ServerLevel sl = (ServerLevel)player.level();
                     double crushRadius = !isOvercharged ? Math.min(20.0, 12.0 + Math.max(0, intel - 10) * 0.08) : 24.0;
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(crushRadius),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float intMult = 1.0F + Math.max(0, intel - 10) * 0.035F;
                     boolean manaSurge = data.energy() >= data.maxEnergy() * 0.7F;
                     float surgeBonus = !manaSurge ? 1.0F : 1.2F;
                     float dmg = (50.0F + intel * 0.6F) * treeSkillMult * intMult * surgeBonus * chargeMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        exx.setDeltaMovement(0.0, !isOvercharged ? -2.2 : -3.0, 0.0);
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, !isOvercharged ? 4 : 5));
                     }

                     spawnGeometricMagicCircle(
                        sl,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ParticleTypes.PORTAL,
                        ParticleTypes.SCULK_SOUL,
                        ParticleTypes.SONIC_BOOM,
                        !isOvercharged ? 6.0 : 8.5
                     );
                     spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.REVERSE_PORTAL, !isOvercharged ? 6.0 : 9.0, 4, 24);
                     spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.PORTAL, 5.0, 6.0, 6, 20);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.SONIC_BOOM, 8.0, 16, 0.1);
                     if (isOvercharged) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.EXPLOSION, 16.0, 32, 0.25);
                     }

                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 0.7F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.6F);
                     Component surgeTag = !manaSurge ? Component.empty() : Component.translatable("awakening_system.skill.tag.mana_surge_20");
                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.max_collapse_50");
                     player.displayClientMessage(
                        Component.translatable(
                           "awakening_system.skill.gravity_crush.msg", new Object[]{String.format(Locale.US, "%.0f", dmg), surgeTag, chargeTag}
                        ),
                        true
                     );
                     break;
                  }
                  case "supernova_incineration": {
                     ServerLevel sl = (ServerLevel)player.level();
                     double blastRadius = !isOvercharged ? Math.min(22.0, 15.0 + Math.max(0, intel - 10) * 0.08) : 26.0;
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(blastRadius),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float intMult = 1.0F + Math.max(0, intel - 10) * 0.04F;
                     boolean manaSurge = data.energy() >= data.maxEnergy() * 0.7F;
                     float surgeBonus = !manaSurge ? 1.0F : 1.25F;
                     float dmg = (90.0F + intel * 1.0F) * treeSkillMult * intMult * surgeBonus * chargeMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().magic(), dmg);
                        exx.setRemainingFireTicks(!isOvercharged ? 180 : 260);
                     }

                     Vec3 look = player.getLookAngle().normalize();
                     Vec3 up = new Vec3(0.0, 1.0, 0.0);
                     Vec3 right = look.cross(up);
                     right = !(right.lengthSqr() < 0.001) ? right.normalize() : new Vec3(1.0, 0.0, 0.0);
                     boolean isLeft = player.getMainArm() == HumanoidArm.LEFT;
                     double armSide = !isLeft ? 0.28 : -0.28;
                     double armDist = 1.35;
                     double armDown = 0.2;
                     Vec3 armOrigin = player.position().add(0.0, player.getEyeHeight() - armDown, 0.0).add(look.scale(armDist)).add(right.scale(armSide));
                     spawnVerticalMagicCircle(sl, armOrigin, look, ParticleTypes.LAVA, ParticleTypes.FLAME, !isOvercharged ? 3.2 : 4.5);
                     spawnGeometricMagicCircle(
                        sl,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ParticleTypes.LAVA,
                        ParticleTypes.FLAME,
                        ParticleTypes.FLASH,
                        !isOvercharged ? 8.5 : 11.0
                     );
                     spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.FLAME, 4.0, 7.5, 6, 20);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLAME, 6.0, 36, 0.12);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.LAVA, 12.0, 48, 0.16);
                     spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, !isOvercharged ? 18.0 : 24.0, 36, 0.22);
                     if (!isOvercharged) {
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                     } else {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLASH, 28.0, 48, 0.3);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0, player.getZ(), 8, 0.0, 0.0, 0.0, 0.0);
                     }

                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.6F
                     );
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.2F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                     Component surgeTag = !manaSurge ? Component.empty() : Component.translatable("awakening_system.skill.tag.solar_surge_125");
                     Component chargeTag = !isOvercharged ? Component.empty() : Component.translatable("awakening_system.skill.tag.max_supernova_50");
                     player.displayClientMessage(
                        Component.translatable("awakening_system.skill.supernova.msg", new Object[]{String.format(Locale.US, "%.0f", dmg), surgeTag, chargeTag}),
                        true
                     );
                     break;
                  }
                  case "arcane_overload": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 360, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                     int energyRestore = (int)Math.min((float)data.maxEnergy(), data.maxEnergy() * 0.5F + intel * 2.5F);
                     data.restoreEnergy(energyRestore);
                     data.restoreMana(data.maxMana() / 2);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnGeometricMagicCircle(
                           sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ENCHANT, ParticleTypes.GLOW, ParticleTypes.END_ROD, 4.2
                        );
                        spawnVerticalMagicCircle(
                           sl, player.position().add(0.0, 1.2, 0.0), new Vec3(0.0, 1.0, 0.0), ParticleTypes.PORTAL, ParticleTypes.ENCHANT, 2.5
                        );
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.3, player.getZ(), ParticleTypes.END_ROD, 4.5, 28, 0.08);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 1.7F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.3F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.6F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.arcane_overload.msg", new Object[]{energyRestore}), true);
                     break;
                  }
                  case "time_dilation": {
                     ServerLevel sl = (ServerLevel)player.level();

                     for (LivingEntity exx : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(16.0),
                        exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                     )) {
                        exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                        exx.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 3));
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 3));
                     spawnGeometricMagicCircle(
                        sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.FLASH, 7.5
                     );
                     spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 8.0, 4, 24);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.6F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.3F, 1.5F);
                     player.displayClientMessage(Component.translatable("awakening_system.skill.time_dilation.msg"), true);
                     break;
                  }
                  case "dragon_breath": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();
                     Vec3 center = player.position().add(look.scale(5.0));
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(center.x - 5.0, center.y - 3.0, center.z - 5.0, center.x + 5.0, center.y + 3.0, center.z + 5.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (50.0F + str * 0.4F + intel * 0.4F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().dragonBreath(), dmg);
                        exx.setRemainingFireTicks(120);
                     }

                     spawnHelixBeam(sl, player.getEyePosition(), look, 10.0, ParticleTypes.DRAGON_BREATH, ParticleTypes.FLAME, 0.7);
                     sl.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.5, center.z, 20, 1.0, 0.5, 1.0, 0.1);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 1.0F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.3F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.9F);
                     break;
                  }
                  case "meteor_fall": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();
                     Vec3 impact = player.position().add(look.scale(10.0));
                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(impact.x - 6.0, impact.y - 3.0, impact.z - 6.0, impact.x + 6.0, impact.y + 4.0, impact.z + 6.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (70.0F + str * 0.5F + intel * 0.5F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().explosion(null), dmg);
                        exx.setRemainingFireTicks(140);
                        exx.setDeltaMovement(0.0, 0.8, 0.0);
                     }

                     spawnImpactCrater(sl, impact.x, impact.y, impact.z, 5.0);
                     spawnShockwaveRing(sl, impact.x, impact.y + 0.2, impact.z, ParticleTypes.LAVA, 6.0, 36, 0.12);
                     spawnShockwaveRing(sl, impact.x, impact.y + 0.2, impact.z, ParticleTypes.FLAME, 10.0, 48, 0.18);
                     sl.sendParticles(ParticleTypes.FLASH, impact.x, impact.y + 1.0, impact.z, 3, 0.0, 0.0, 0.0, 0.0);
                     sl.playSound(null, impact.x, impact.y, impact.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7F, 0.6F);
                     sl.playSound(null, impact.x, impact.y, impact.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.4F, 1.1F);
                     sl.playSound(null, impact.x, impact.y, impact.z, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
                     break;
                  }
                  case "breath_of_annihilation": {
                     ServerLevel sl = (ServerLevel)player.level();
                     Vec3 look = player.getLookAngle();

                     for (int d = 2; d <= 20; d += 2) {
                        Vec3 pos = player.position().add(look.scale(d));
                        sl.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + 1.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
                     }

                     List<LivingEntity> enemies = sl.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(16.0),
                        exx -> exx != player && exx.isAlive() && !exx.getTags().contains("shadow_soldier")
                     );
                     float dmg = (90.0F + str * 0.6F + intel * 0.6F) * treeSkillMult;

                     for (LivingEntity exx : enemies) {
                        exx.hurt(player.damageSources().dragonBreath(), dmg);
                        exx.setDeltaMovement(look.x * 2.0, 0.8, look.z * 2.0);
                     }

                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 0.8F);
                     sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.9F);
                     sl.playSound(
                        null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.7F
                     );
                     break;
                  }
                  case "dragon_wings_flight": {
                     player.setDeltaMovement(0.0, 1.5, 0.0);
                     player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 3));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.DRAGON_BREATH, 4.0, 32, 0.12);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DRAGON_BREATH, 2.5, 3.5, 4, 14);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.6F, 1.0F);
                        sl.playSound(
                           null,
                           player.getX(),
                           player.getY(),
                           player.getZ(),
                           (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_3.value(),
                           SoundSource.PLAYERS,
                           1.4F,
                           1.2F
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.3F, 1.1F);
                     }
                     break;
                  }
                  case "dragon_form_avatar": {
                     player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4));
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(12.0),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                        )) {
                           exx.hurt(player.damageSources().dragonBreath(), 40.0F);
                           double dx = exx.getX() - player.getX();
                           double dz = exx.getZ() - player.getZ();
                           double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                           exx.setDeltaMovement(dx / distx * 1.6, 0.5, dz / distx * 1.6);
                        }

                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DRAGON_BREATH, 5.0, 7.0, 6, 20);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, 8.0, 28, 0.18);
                        sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 2.0, player.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 1.4F, 1.1F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 0.8F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 1.1F);
                     }
                     break;
                  }
                  case "sh_caregiver_blessing": {
                     float healAmt = 12.0F + vit * 0.15F + intel * 0.1F;
                     player.heal(healAmt * treeSkillMult);
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 360, 1, false, true, true));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, false, true, true));

                     for (LivingEntity ally : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(14.0),
                           exx -> exx != player
                              && (
                                 exx instanceof TamableAnimal t && player.getUUID().equals(t.getOwnerUUID())
                                    || exx instanceof ServerPlayer
                                    || exx.getTags().contains("shadow_soldier")
                              )
                        )) {
                        ally.heal(healAmt * treeSkillMult);
                        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 360, 1));
                        ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnMagicCircle(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.HEART, 4.5);
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 2.5, 4.0, 4, 14);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.5F);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.caregiver_blessing.msg"), true);
                     break;
                  }
                  case "sh_holy_touch": {
                     player.removeEffect(MobEffects.WITHER);
                     player.removeEffect(MobEffects.POISON);
                     player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                     player.removeEffect(MobEffects.WEAKNESS);
                     player.heal((16.0F + intel * 0.15F) * treeSkillMult);
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 0, false, true, true));

                     for (LivingEntity ally : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(10.0),
                           exx -> exx != player
                              && (
                                 exx instanceof TamableAnimal t && player.getUUID().equals(t.getOwnerUUID())
                                    || exx instanceof ServerPlayer
                                    || exx.getTags().contains("shadow_soldier")
                              )
                        )) {
                        ally.removeEffect(MobEffects.WITHER);
                        ally.removeEffect(MobEffects.POISON);
                        ally.heal(12.0F * treeSkillMult);
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.HAPPY_VILLAGER, 3.5, 20, 0.06);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.3F, 1.6F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.sacred_touch.msg"), true);
                     break;
                  }
                  case "sh_divine_grace": {
                     int durx = (int)(300.0F * treeSkillMult);
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durx, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.SATURATION, durx, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durx, 1));
                     data.restoreMana(data.maxMana() / 3);

                     for (LivingEntity ally : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(14.0),
                           exx -> exx != player
                              && (
                                 exx instanceof TamableAnimal t && player.getUUID().equals(t.getOwnerUUID())
                                    || exx instanceof ServerPlayer
                                    || exx.getTags().contains("shadow_soldier")
                              )
                        )) {
                        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durx, 2));
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durx, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnExpandingDome(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.GLOW, 6.0, 4, 18);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.4F, 1.6F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.divine_grace.msg"), true);
                     break;
                  }
                  case "sh_sacred_sanctuary": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2));
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity u : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(8.0), exx -> exx != player && exx.isAlive() && exx.isInvertedHealAndHarm()
                        )) {
                           u.hurt(player.damageSources().magic(), 35.0F * treeSkillMult);
                           u.setRemainingFireTicks(120);
                           double dx = u.getX() - player.getX();
                           double dz = u.getZ() - player.getZ();
                           double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                           u.setDeltaMovement(dx / distx * 1.5, 0.4, dz / distx * 1.5);
                        }

                        spawnGeometricMagicCircle(
                           sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.0
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.4F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.sacred_sanctuary.msg"), true);
                     break;
                  }
                  case "sh_cleansing_radiance": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(12.0),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                        )) {
                           float dmg = (!exx.isInvertedHealAndHarm() ? 45.0F : 80.0F) * treeSkillMult;
                           exx.hurt(player.damageSources().magic(), dmg);
                           exx.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                           exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.FLASH, 10.0, 36, 0.2);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.2F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.cleansing_radiance.msg"), true);
                     break;
                  }
                  case "sh_aegis_immortal": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 3));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.5, player.getZ(), ParticleTypes.END_ROD, 3.0, 24, 0.05);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.5F, 1.0F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.aegis_immortal.msg"), true);
                     break;
                  }
                  case "sh_tamer_bond": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));

                     for (LivingEntity pet : player.level()
                        .getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(20.0),
                           exx -> exx != player
                              && (exx instanceof TamableAnimal t && player.getUUID().equals(t.getOwnerUUID()) || exx.getTags().contains("shadow_soldier"))
                        )) {
                        pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                        pet.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 2));
                        pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.2F, 1.1F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.tamer_bond.msg"), true);
                     break;
                  }
                  case "sh_awakening_inspire": {
                     int durx = 500;
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durx, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, durx, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durx, 1));

                     for (ServerPlayer a : player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(16.0), p -> p != player)) {
                        a.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durx, 1));
                        a.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, durx, 1));
                     }

                     if (player.level() instanceof ServerLevel sl) {
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.END_ROD, 3.0, 5.0, 5, 16);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4F, 1.2F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.awakening_inspire.msg"), true);
                     break;
                  }
                  case "le_electrification": {
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(8.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().lightningBolt(), (30.0F + agi * 0.3F) * treeSkillMult);
                           exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 6.0, 36, 0.18);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.2F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.electrification.msg"), true);
                     break;
                  }
                  case "le_thunder_spear": {
                     if (player.level() instanceof ServerLevel sl) {
                        Vec3 lookx = player.getLookAngle();

                        for (int d = 1; d <= 22; d += 2) {
                           Vec3 p = player.getEyePosition().add(lookx.scale(d));
                           sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 5, 0.2, 0.2, 0.2, 0.05);
                        }

                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class,
                           player.getBoundingBox().inflate(16.0),
                           exxx -> exxx != player && exxx.isAlive() && !exxx.getTags().contains("shadow_soldier")
                        )) {
                           exx.hurt(player.damageSources().lightningBolt(), (60.0F + agi * 0.5F) * treeSkillMult);
                           exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                        }

                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.6F, 1.1F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.thunder_spear.msg"), true);
                     break;
                  }
                  case "le_plasma_burst": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(9.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().lightningBolt(), (55.0F + str * 0.4F + agi * 0.4F) * treeSkillMult);
                           exx.setDeltaMovement(0.0, 1.0, 0.0);
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.ELECTRIC_SPARK, 8.0, 40, 0.22);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.2F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.plasma_burst.msg"), true);
                     break;
                  }
                  case "le_wrath_of_thunder_god": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(16.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().lightningBolt(), (85.0F + agi * 0.6F) * treeSkillMult);
                           sl.sendParticles(ParticleTypes.FLASH, exx.getX(), exx.getY() + 1.0, exx.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                        }

                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, 5.0, 8.0, 6, 24);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.8F, 0.9F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.wrath_thunder_god.msg"), true);
                     break;
                  }
                  case "le_flash_step": {
                     Vec3 look = player.getLookAngle();
                     double tx = player.getX() + look.x * 12.0;
                     double ty = player.getY() + look.y * 5.0;
                     double tz = player.getZ() + look.z * 12.0;
                     Vec3 safe = TeleportHelper.findSafeTeleportPos(player.level(), player, tx, ty, tz, player.position());
                     player.teleportTo(safe.x, safe.y, safe.z);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, safe.x, safe.y + 0.1, safe.z, ParticleTypes.ELECTRIC_SPARK, 3.0, 20, 0.1);
                        sl.playSound(null, safe.x, safe.y, safe.z, (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.4F, 1.3F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.flash_step.msg"), true);
                     break;
                  }
                  case "le_overdrive_speed":
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.overdrive_speed.msg"), true);
                     break;
                  case "le_lightning_armor":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.lightning_armor.msg"), true);
                     break;
                  case "le_vajra_indestructible": {
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnGeometricMagicCircle(
                           sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, ParticleTypes.FLASH, 6.0
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 1.2F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.vajra_indestructible.msg"), true);
                     break;
                  }
                  case "be_bloodthirst": {
                     float dmg = 35.0F * treeSkillMult;
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(10.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().magic(), dmg);
                        }

                        player.heal(10.0F * treeSkillMult);
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.DAMAGE_INDICATOR, 5.0, 24, 0.1);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 1.4F);
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.bloodthirst.msg"), true);
                     break;
                  }
                  case "be_blood_lance": {
                     if (player.level() instanceof ServerLevel sl) {
                        Vec3 lookx = player.getLookAngle();

                        for (int d = 1; d <= 20; d += 2) {
                           Vec3 p = player.getEyePosition().add(lookx.scale(d));
                           sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 4, 0.2, 0.2, 0.2, 0.05);
                        }

                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(14.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().playerAttack(player), (65.0F + str * 0.5F) * treeSkillMult);
                        }

                        player.heal(12.0F * treeSkillMult);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.4F, 0.8F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.blood_lance.msg"), true);
                     break;
                  }
                  case "be_crimson_eruption": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(10.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().magic(), (70.0F + str * 0.5F) * treeSkillMult);
                           exx.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SWEEP_ATTACK, 8.0, 32, 0.15);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.3F, 1.2F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.crimson_eruption.msg"), true);
                     break;
                  }
                  case "be_blood_apocalypse": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(14.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().magic(), (95.0F + str * 0.6F) * treeSkillMult);
                        }

                        player.heal(player.getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                        spawnVortexPillar(sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.DAMAGE_INDICATOR, 6.0, 8.0, 6, 30);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.8F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.blood_apocalypse.msg"), true);
                     break;
                  }
                  case "be_crimson_rush": {
                     Vec3 look = player.getLookAngle();
                     player.setDeltaMovement(look.x * 2.0, 0.3, look.z * 2.0);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.crimson_rush.msg"), true);
                     break;
                  }
                  case "be_berserk_blood":
                     player.hurt(player.damageSources().magic(), 2.0F);
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 320, 3));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.berserk_blood.msg"), true);
                     break;
                  case "be_blood_barrier":
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.blood_barrier.msg"), true);
                     break;
                  case "be_dragon_blood_avatar":
                     player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.dragon_blood_avatar.msg"), true);
                     break;
                  case "bm_seismic_roar": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(10.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().playerAttack(player), (40.0F + str * 0.4F) * treeSkillMult);
                           exx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                           double dx = exx.getX() - player.getX();
                           double dz = exx.getZ() - player.getZ();
                           double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                           exx.setDeltaMovement(dx / distx * 1.8, 0.4, dz / distx * 1.8);
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.SONIC_BOOM, 6.0, 24, 0.15);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4F, 0.9F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.seismic_roar.msg"), true);
                     break;
                  }
                  case "bm_predator_leap": {
                     Vec3 look = player.getLookAngle();
                     player.setDeltaMovement(look.x * 1.8, 0.8, look.z * 1.8);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.1, player.getZ(), ParticleTypes.CRIT, 4.0, 18, 0.08);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.4F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.predator_leap.msg"), true);
                     break;
                  }
                  case "bm_feral_rend": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(6.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().playerAttack(player), (70.0F + str * 0.6F) * treeSkillMult);
                           exx.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                        }

                        spawnBurstCross(sl, player.getX(), player.getY() + 1.0, player.getZ(), ParticleTypes.SWEEP_ATTACK, 3.0, 16);
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5F, 0.8F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.feral_rend.msg"), true);
                     break;
                  }
                  case "bm_white_tiger_transformation": {
                     player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
                     if (player.level() instanceof ServerLevel sl) {
                        spawnGeometricMagicCircle(
                           sl, player.getX(), player.getY(), player.getZ(), ParticleTypes.CRIT, ParticleTypes.ANGRY_VILLAGER, ParticleTypes.FLASH, 6.0
                        );
                        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.6F, 1.1F);
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.white_tiger_transformation.msg"), true);
                     break;
                  }
                  case "bm_scent_tracking": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity m : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(40.0), exx -> exx != player && exx.isAlive()
                        )) {
                           m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                        }
                     }

                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.scent_tracking.msg"), true);
                     break;
                  }
                  case "bm_pack_frenzy":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.pack_frenzy.msg"), true);
                     break;
                  case "bm_thick_fur":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 280, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.thick_fur.msg"), true);
                     break;
                  case "bm_titan_roar": {
                     if (player.level() instanceof ServerLevel sl) {
                        for (LivingEntity exx : sl.getEntitiesOfClass(
                           LivingEntity.class, player.getBoundingBox().inflate(14.0), exxx -> exxx != player && exxx.isAlive()
                        )) {
                           exx.hurt(player.damageSources().playerAttack(player), (75.0F + str * 0.6F) * treeSkillMult);
                           double dx = exx.getX() - player.getX();
                           double dz = exx.getZ() - player.getZ();
                           double distx = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                           exx.setDeltaMovement(dx / distx * 2.2, 0.8, dz / distx * 2.2);
                        }

                        spawnShockwaveRing(sl, player.getX(), player.getY() + 0.2, player.getZ(), ParticleTypes.EXPLOSION, 10.0, 36, 0.25);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.7F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.titan_roar.msg"), true);
                     break;
                  }
                  case "cb_valterian_instinct":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 1));
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.valterian_instinct.msg"), true);
                     break;
                  case "cb_pain_suppression":
                  case "sa_pain_suppression":
                  case "ga_pain_suppression":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.pain_suppression.msg"), true);
                     break;
                  case "cb_bloodthirst":
                  case "dm_bloodthirst":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                     player.heal(8.0F);
                     player.displayClientMessage(Component.translatable("awakening_system.skill.bloodthirst_satiated.msg"), true);
                     break;
                  case "dc_nano_blade_overdrive":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.nano_blade_overdrive.msg"), true);
                     break;
                  case "va_stealth_stalker":
                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.stealth_stalker.msg"), true);
                     break;
                  case "va_phantom_ambush":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.phantom_ambush.msg"), true);
                     break;
                  case "va_death_mark":
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
                     player.displayClientMessage(Component.translatable("awakening_system.skill.death_mark.msg"), true);
                     break;
                  case "dm_dragon_dive": {
                     player.setDeltaMovement(0.0, -2.0, 0.0);
                     if (player.level() instanceof ServerLevel sl) {
                        spawnImpactCrater(sl, player.getX(), player.getY(), player.getZ(), 4.0);
                        sl.playSound(
                           null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.8F
                        );
                     }

                     player.displayClientMessage(Component.translatable("awakening_system.skill.dragon_dive.msg"), true);
                     break;
                  }
                  default:
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true, true));
               }

               if (player.level() instanceof ServerLevel serverLevel) {
                  serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.35, 0.45, 0.35, 0.15);
                  serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.85F, 1.25F);
               }

               checkAchievements(player, data);
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_activated.title",
                  "awakening_system.notification.skill_activated.message",
                  -11470213,
                  new Object[]{SkillDefinitions.get(skillId) == null ? skillId : SkillDefinitions.get(skillId).nameKey()}
               );
               AwakeningSystemMod.sync(player);
               return true;
            } else {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_activate_failed.title",
                  "awakening_system.notification.skill_activate_failed.message",
                  -35226,
                  new Object[0]
               );
               return false;
            }
         }
      } else {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.skill_activate_failed.title", "awakening_system.notification.skills_disabled", -35226, new Object[0]
         );
         return false;
      }
   }

   public static void summonShadowSoldier(ServerPlayer master, LivingEntity defeated) {
      if (master.level() instanceof ServerLevel sl) {
         Mob shadowMob;
         if (defeated.getType().create(sl) instanceof Mob mob) {
            shadowMob = mob;
         } else {
            shadowMob = new Wolf(EntityType.WOLF, sl);
         }

         shadowMob.setPos(defeated.getX(), defeated.getY(), defeated.getZ());
         shadowMob.addTag("shadow_soldier");
         shadowMob.addTag("shadow_master_" + master.getUUID());
         String mobName = defeated.getCustomName() != null ? defeated.getCustomName().getString() : defeated.getType().getDescription().getString();
         shadowMob.setCustomName(Component.literal("§8§l[SOMBRA] §9" + mobName));
         shadowMob.setCustomNameVisible(true);
         if (defeated instanceof Mob defeatedMob) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
               ItemStack item = defeatedMob.getItemBySlot(slot);
               if (!item.isEmpty()) {
                  shadowMob.setItemSlot(slot, item.copy());
               }
            }
         }

         setupShadowMob(sl, master, shadowMob);
         AwakeningSystemMod.notify(
            master,
            "awakening_system.notification.shadow_extracted.title",
            "awakening_system.notification.shadow_extracted.message",
            -6723841,
            new Object[]{mobName}
         );
      }
   }

   public static void applyShadowRankStats(Mob shadowMob, ShadowRank rank) {
      if (shadowMob != null && rank != null) {
         AttributeInstance maxHpAttr = shadowMob.getAttribute(Attributes.MAX_HEALTH);
         if (maxHpAttr != null) {
            double baseHp = shadowMob.getPersistentData().contains("shadow_original_max_hp")
               ? shadowMob.getPersistentData().getDouble("shadow_original_max_hp")
               : maxHpAttr.getBaseValue();
            if (baseHp < 1.0) baseHp = maxHpAttr.getBaseValue();
            shadowMob.getPersistentData().putDouble("shadow_original_max_hp", baseHp);
            maxHpAttr.setBaseValue(Math.max(baseHp, baseHp * rank.getHealthMultiplier()));
            if (shadowMob.getHealth() > 0.0F) {
               shadowMob.setHealth(shadowMob.getMaxHealth());
            }
         }

         AttributeInstance dmgAttr = shadowMob.getAttribute(Attributes.ATTACK_DAMAGE);
         if (dmgAttr != null) {
            double baseDmg = shadowMob.getPersistentData().contains("shadow_original_dmg")
               ? shadowMob.getPersistentData().getDouble("shadow_original_dmg")
               : dmgAttr.getBaseValue();
            if (baseDmg < 1.0) baseDmg = dmgAttr.getBaseValue();
            shadowMob.getPersistentData().putDouble("shadow_original_dmg", baseDmg);
            dmgAttr.setBaseValue(baseDmg + rank.getBonusAttackDamage());
         }

         AttributeInstance armorAttr = shadowMob.getAttribute(Attributes.ARMOR);
         if (armorAttr != null && shadowMob.getPersistentData().contains("shadow_original_armor")) {
            double baseArmor = shadowMob.getPersistentData().getDouble("shadow_original_armor");
            armorAttr.setBaseValue(Math.max(armorAttr.getBaseValue(), baseArmor));
         }

         AttributeInstance toughAttr = shadowMob.getAttribute(Attributes.ARMOR_TOUGHNESS);
         if (toughAttr != null && shadowMob.getPersistentData().contains("shadow_original_toughness")) {
            double baseTough = shadowMob.getPersistentData().getDouble("shadow_original_toughness");
            toughAttr.setBaseValue(Math.max(toughAttr.getBaseValue(), baseTough));
         }

         AttributeInstance kbAttr = shadowMob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
         if (kbAttr != null && shadowMob.getPersistentData().contains("shadow_original_kb")) {
            double baseKb = shadowMob.getPersistentData().getDouble("shadow_original_kb");
            kbAttr.setBaseValue(Math.max(kbAttr.getBaseValue(), baseKb));
         }

         AttributeInstance speedAttr = shadowMob.getAttribute(Attributes.MOVEMENT_SPEED);
         if (speedAttr != null && shadowMob.getPersistentData().contains("shadow_original_speed")) {
            double baseSpeed = shadowMob.getPersistentData().getDouble("shadow_original_speed");
            if (baseSpeed > 0.05) {
               speedAttr.setBaseValue(Math.max(speedAttr.getBaseValue(), baseSpeed));
            }
         }

         AttributeInstance followAttr = shadowMob.getAttribute(Attributes.FOLLOW_RANGE);
         if (followAttr != null) {
            followAttr.setBaseValue(48.0 + rank.getLevel() * 16.0);
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
   }

   public static void promoteOrTrackShadowKill(Mob shadowSoldier) {
      if (shadowSoldier != null && shadowSoldier.level() instanceof ServerLevel sl) {
         int curRankLevel = shadowSoldier.getPersistentData().getInt("shadow_rank");
         ShadowRank currentRank = ShadowRank.byLevel(curRankLevel);
         if (currentRank != ShadowRank.GRAND_MARSHAL) {
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
               sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, ox, oy + 1.0, oz, 70, 0.6, 0.8, 0.6, 0.2);
               sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, oy + 0.8, oz, 80, 0.8, 0.8, 0.8, 0.1);
               sl.sendParticles(ParticleTypes.SCULK_SOUL, ox, oy + 1.0, oz, 40, 0.5, 0.6, 0.5, 0.08);
               sl.sendParticles(ParticleTypes.SONIC_BOOM, ox, oy + 1.0, oz, 1, 0.0, 0.0, 0.0, 0.0);
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
                        AwakeningSystemMod.notify(
                           master,
                           "awakening_system.notification.shadow_promoted.title",
                           "awakening_system.notification.shadow_promoted.desc",
                           nextRank.getColor(),
                           new Object[]{baseName, nextRank.getPrefix()}
                        );
                        master.connection.send(new ClientboundSetTitlesAnimationPacket(5, 35, 15));
                        master.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.shadow.promotion_title")));
                        master.connection
                           .send(
                              new ClientboundSetSubtitleTextPacket(
                                 Component.translatable("awakening_system.shadow.promotion_subtitle", new Object[]{baseName, nextRank.getPrefix()})
                              )
                           );
                        master.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.1F);
                     }
                  } catch (Exception var17) {
                     AwakeningSystemMod.LOGGER.debug("Error sending shadow promotion title packet: {}", var17.getMessage(), var17);
                  }
               }
            }
         }
      }
   }

   public static void summonShadowFromSoul(ServerPlayer master, ShadowSoulEntity soul) {
      if (master.level() instanceof ServerLevel sl) {
         if (soul.getMobEntityTypeId().toLowerCase().contains("celestial_dragon")) {
            master.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
         } else {
            EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(soul.getMobEntityTypeId()));
            if (type == null) {
               type = EntityType.ZOMBIE;
            }

            Mob shadowMob;
            if (type.create(sl) instanceof Mob mob) {
               shadowMob = mob;
            } else {
               shadowMob = new Wolf(EntityType.WOLF, sl);
            }

            if (soul.getSavedEntityNbt() != null && !soul.getSavedEntityNbt().isEmpty()) {
               try {
                  CompoundTag nbtCopy = soul.getSavedEntityNbt().copy();
                  nbtCopy.putBoolean("ModelDead", false);
                  nbtCopy.putInt("DeadProg", 0);
                  nbtCopy.putInt("DeathTime", 0);
                  nbtCopy.putBoolean("NoAI", false);
                  nbtCopy.remove("DeathTime");
                  shadowMob.readAdditionalSaveData(nbtCopy);
               } catch (Throwable ignored) {}
            }

            shadowMob.setPos(soul.getX(), soul.getY(), soul.getZ());
            shadowMob.addTag("shadow_soldier");
            shadowMob.addTag("shadow_master_" + master.getUUID());
            shadowMob.addTag("rpg_scaled");
            String mobName = soul.getMobName();
            if (mobName == null || mobName.isEmpty()) {
               mobName = type.getDescription().getString();
            }

            int rankLvl = soul.getShadowRank();
            int mobLvl = soul.getRpgMobLevel();
            shadowMob.getPersistentData().putInt("shadow_rank", rankLvl);
            shadowMob.getPersistentData().putInt("shadow_kills", 0);
            shadowMob.getPersistentData().putString("shadow_base_name", mobName);
            if (mobLvl > 0) shadowMob.getPersistentData().putInt("rpg_mob_level", mobLvl);
            shadowMob.getPersistentData().putDouble("shadow_original_max_hp", soul.getMobMaxHealth());
            shadowMob.getPersistentData().putDouble("shadow_original_dmg", soul.getMobAttackDamage());
            shadowMob.getPersistentData().putDouble("shadow_original_armor", soul.getMobArmor());
            shadowMob.getPersistentData().putDouble("shadow_original_toughness", soul.getMobArmorToughness());
            shadowMob.getPersistentData().putDouble("shadow_original_kb", soul.getMobKnockbackResistance());
            shadowMob.getPersistentData().putDouble("shadow_original_speed", soul.getMobMovementSpeed());
            ShadowRank rank = ShadowRank.byLevel(rankLvl);
            String lvlPrefix = mobLvl > 0 ? "§e[Lv. " + mobLvl + "] " : "";
            shadowMob.setCustomName(Component.literal(rank.getPrefix() + " §9" + lvlPrefix).append(soul.getMobNameComponent()));
            shadowMob.setCustomNameVisible(true);

            for (Entry<EquipmentSlot, ItemStack> entry : soul.getSavedEquipment().entrySet()) {
               if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                  shadowMob.setItemSlot(entry.getKey(), entry.getValue().copy());
               }
            }

            setupShadowMob(sl, master, shadowMob);
         }
      }
   }

   public static void summonShadowFromTag(ServerPlayer master, CompoundTag tag, double x, double y, double z) {
      if (master.level() instanceof ServerLevel sl) {
         String typeStr = tag.getString("MobEntityTypeId");
         if (typeStr.toLowerCase().contains("celestial_dragon")) {
            master.displayClientMessage(Component.translatable("awakening_system.shadow.cannot_extract_celestial_dragon"), true);
         } else {
            EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(typeStr));
            if (type == null) {
               type = EntityType.ZOMBIE;
            }

            Mob shadowMob;
            if (type.create(sl) instanceof Mob mob) {
               shadowMob = mob;
            } else {
               shadowMob = new Wolf(EntityType.WOLF, sl);
            }

            if (tag.contains("SavedEntityNbt")) {
               try {
                  CompoundTag nbtCopy = tag.getCompound("SavedEntityNbt").copy();
                  nbtCopy.putBoolean("ModelDead", false);
                  nbtCopy.putInt("DeadProg", 0);
                  nbtCopy.putInt("DeathTime", 0);
                  nbtCopy.putBoolean("NoAI", false);
                  nbtCopy.remove("DeathTime");
                  shadowMob.readAdditionalSaveData(nbtCopy);
               } catch (Throwable ignored) {}
            }

            shadowMob.setPos(x, y, z);
            shadowMob.addTag("shadow_soldier");
            shadowMob.addTag("shadow_master_" + master.getUUID());
            shadowMob.addTag("rpg_scaled");
            int rankLvl = tag.contains("ShadowRank") ? tag.getInt("ShadowRank") : 0;
            int kills = tag.contains("ShadowKills") ? tag.getInt("ShadowKills") : 0;
            String baseName = tag.contains("ShadowBaseName")
               ? tag.getString("ShadowBaseName")
               : (tag.contains("MobName") ? tag.getString("MobName") : type.getDescription().getString());
            baseName = baseName.replaceAll("§[0-9a-fk-or]", "").replaceAll("\\[.*?\\]", "").trim();
            if (baseName.isEmpty()) {
               baseName = type.getDescription().getString();
            }

            int mobLvl = tag.contains("RpgMobLevel") ? tag.getInt("RpgMobLevel") : 0;
            shadowMob.getPersistentData().putInt("shadow_rank", rankLvl);
            shadowMob.getPersistentData().putInt("shadow_kills", kills);
            shadowMob.getPersistentData().putString("shadow_base_name", baseName);
            if (mobLvl > 0) shadowMob.getPersistentData().putInt("rpg_mob_level", mobLvl);
            if (tag.contains("OriginalMaxHp")) shadowMob.getPersistentData().putDouble("shadow_original_max_hp", tag.getDouble("OriginalMaxHp"));
            if (tag.contains("OriginalDmg")) shadowMob.getPersistentData().putDouble("shadow_original_dmg", tag.getDouble("OriginalDmg"));
            if (tag.contains("OriginalArmor")) shadowMob.getPersistentData().putDouble("shadow_original_armor", tag.getDouble("OriginalArmor"));
            if (tag.contains("OriginalToughness")) shadowMob.getPersistentData().putDouble("shadow_original_toughness", tag.getDouble("OriginalToughness"));
            if (tag.contains("OriginalKb")) shadowMob.getPersistentData().putDouble("shadow_original_kb", tag.getDouble("OriginalKb"));
            if (tag.contains("OriginalSpeed")) shadowMob.getPersistentData().putDouble("shadow_original_speed", tag.getDouble("OriginalSpeed"));
            IceAndFireCompat.restoreMobData(shadowMob, tag);
            ShadowRank rank = ShadowRank.byLevel(rankLvl);
            String lvlPrefix = mobLvl > 0 ? "§e[Lv. " + mobLvl + "] " : "";
            shadowMob.setCustomName(Component.literal(rank.getPrefix() + " §9" + lvlPrefix + baseName));
            shadowMob.setCustomNameVisible(true);
            if (tag.contains("Health")) {
               shadowMob.setHealth(Math.min(shadowMob.getMaxHealth(), tag.getFloat("Health")));
            }

            if (tag.contains("Equipment")) {
               CompoundTag eqTag = tag.getCompound("Equipment");

               for (EquipmentSlot slot : EquipmentSlot.values()) {
                  if (eqTag.contains(slot.getName())) {
                     ItemStack.parse(master.registryAccess(), eqTag.getCompound(slot.getName())).ifPresent(stack -> shadowMob.setItemSlot(slot, stack));
                  }
               }
            }

            setupShadowMob(sl, master, shadowMob);
         }
      }
   }

   public static void setupShadowMob(ServerLevel sl, ServerPlayer master, Mob shadowMob) {
      int rankLvl = shadowMob.getPersistentData().getInt("shadow_rank");
      ShadowRank rank = ShadowRank.byLevel(rankLvl);
      applyShadowRankStats(shadowMob, rank);

      try {
         shadowMob.targetSelector.removeAllGoals(g -> true);
         shadowMob.targetSelector
            .addGoal(
               1,
               new NearestAttackableTargetGoal<Mob>(
                  shadowMob,
                  Mob.class,
                  10,
                  true,
                  false,
                  target -> target != null
                     && target.isAlive()
                     && !target.getTags().contains("shadow_soldier")
                     && !target.getUUID().equals(master.getUUID())
                     && (target instanceof Enemy || !target.getType().getCategory().isFriendly())
               )
            );
      } catch (Exception var25) {
         AwakeningSystemMod.LOGGER.debug("Error adding attack goal to summoned shadow: {}", var25.getMessage(), var25);
      }

      shadowMob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 255, false, false, false));
      shadowMob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 255, false, false, false));
      shadowMob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 4, false, false, false));
      shadowMob.setDeltaMovement(0.0, 0.0, 0.0);
      shadowMob.setOnGround(true);

      try {
         Scoreboard sb = sl.getScoreboard();
         PlayerTeam team = sb.getPlayerTeam("shadows");
         if (team == null) {
            team = sb.addPlayerTeam("shadows");
            team.setColor(ChatFormatting.AQUA);
            team.setDisplayName(Component.translatable("team.awakening_system.shadows"));
         }

         String member = shadowMob.getScoreboardName();
         if (!team.getPlayers().contains(member)) {
            sb.addPlayerToTeam(member, team);
         }

         String uuidStr = shadowMob.getStringUUID();
         if (!team.getPlayers().contains(uuidStr)) {
            sb.addPlayerToTeam(uuidStr, team);
         }
      } catch (Exception var24) {
         AwakeningSystemMod.LOGGER.debug("Error adding summoned shadow to scoreboard team: {}", var24.getMessage(), var24);
      }

      sl.addFreshEntity(shadowMob);
      ShadowEmergencePayload emergencePayload = new ShadowEmergencePayload(
         shadowMob.getId(), shadowMob.getX(), shadowMob.getY(), shadowMob.getZ(), 70, shadowMob.getBbHeight()
      );
      if (master != null) {
         PacketDistributor.sendToPlayer(master, emergencePayload, new CustomPacketPayload[0]);
      }

      try {
         PacketDistributor.sendToPlayersTrackingEntity(shadowMob, emergencePayload, new CustomPacketPayload[0]);
      } catch (Exception var23) {
         AwakeningSystemMod.LOGGER.debug("Error sending emergence payload to tracking players: {}", var23.getMessage(), var23);
      }

      double ox = shadowMob.getX();
      double oy = shadowMob.getY();
      double oz = shadowMob.getZ();

      for (int ring = 1; ring <= 3; ring++) {
         double radius = ring * 0.75;
         int points = ring * 12;

         for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2) * i / points;
            double px = ox + radius * Math.cos(angle);
            double pz = oz + radius * Math.sin(angle);
            sl.sendParticles(ParticleTypes.SQUID_INK, px, oy + 0.05, pz, 1, 0.0, 0.005, 0.0, 0.005);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, oy + 0.05, pz, 1, 0.0, 0.01, 0.0, 0.005);
         }
      }

      sl.sendParticles(ParticleTypes.SQUID_INK, ox, oy + 0.5, oz, 45, 0.4, 0.5, 0.4, 0.02);
      sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, oy + 0.5, oz, 40, 0.35, 0.5, 0.35, 0.02);
      sl.sendParticles(ParticleTypes.SCULK_SOUL, ox, oy + 0.4, oz, 25, 0.3, 0.4, 0.3, 0.01);
      sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 0.4, oz, 30, 0.3, 0.4, 0.3, 0.05);
      sl.sendParticles(ParticleTypes.LARGE_SMOKE, ox, oy + 0.2, oz, 20, 0.3, 0.3, 0.3, 0.01);
      sl.playSound(null, ox, oy, oz, (SoundEvent)ModSounds.SHADOW_EXTRACT.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
      if (master != null) {
         master.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0, false, false, false));
         master.connection.send(new ClientboundSetTitlesAnimationPacket(5, 30, 15));
         master.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("awakening_system.shadow.arise_title")));
         String rawName = shadowMob.getPersistentData().getString("shadow_base_name");
         if (rawName == null || rawName.isEmpty()) {
            rawName = shadowMob.getType().getDescription().getString();
         }

         master.connection
            .send(
               new ClientboundSetSubtitleTextPacket(Component.translatable("awakening_system.shadow.arise_subtitle", new Object[]{rank.getPrefix(), rawName}))
            );
      }
   }

   public static int storePlayerShadows(ServerPlayer player) {
      if (!(player.level() instanceof ServerLevel sl)) {
         return 0;
      } else {
         SystemData var18 = data(player);
         String masterTag = "shadow_master_" + player.getUUID();
         List<Mob> soldiers = sl.getEntitiesOfClass(
            Mob.class, player.getBoundingBox().inflate(96.0), m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
         );
         if (soldiers.isEmpty()) {
            return 0;
         } else {
            int count = 0;

            for (Mob soldier : soldiers) {
               String typeKey = EntityType.getKey(soldier.getType()).toString();
               if (typeKey.toLowerCase(Locale.ROOT).contains("celestial_dragon")) {
                  soldier.discard();
               } else {
                  CompoundTag tag = new CompoundTag();
                  tag.putString("MobEntityTypeId", typeKey);
                  String sName = soldier.getCustomName() != null ? soldier.getCustomName().getString() : soldier.getType().getDescription().getString();
                  tag.putString("MobName", sName);
                  tag.putFloat("Health", soldier.getHealth());
                  tag.putFloat("MaxHealth", soldier.getMaxHealth());
                  tag.putInt("ShadowRank", soldier.getPersistentData().getInt("shadow_rank"));
                  tag.putInt("ShadowKills", soldier.getPersistentData().getInt("shadow_kills"));
                   tag.putInt("RpgMobLevel", soldier.getPersistentData().getInt("rpg_mob_level"));
                   if (soldier.getPersistentData().contains("shadow_original_max_hp")) {
                      tag.putDouble("OriginalMaxHp", soldier.getPersistentData().getDouble("shadow_original_max_hp"));
                   } else {
                      tag.putDouble("OriginalMaxHp", (double)soldier.getMaxHealth());
                   }
                   if (soldier.getPersistentData().contains("shadow_original_dmg")) {
                      tag.putDouble("OriginalDmg", soldier.getPersistentData().getDouble("shadow_original_dmg"));
                   }
                   if (soldier.getPersistentData().contains("shadow_original_armor")) {
                      tag.putDouble("OriginalArmor", soldier.getPersistentData().getDouble("shadow_original_armor"));
                   }
                   if (soldier.getPersistentData().contains("shadow_original_toughness")) {
                      tag.putDouble("OriginalToughness", soldier.getPersistentData().getDouble("shadow_original_toughness"));
                   }
                   if (soldier.getPersistentData().contains("shadow_original_kb")) {
                      tag.putDouble("OriginalKb", soldier.getPersistentData().getDouble("shadow_original_kb"));
                   }
                   if (soldier.getPersistentData().contains("shadow_original_speed")) {
                      tag.putDouble("OriginalSpeed", soldier.getPersistentData().getDouble("shadow_original_speed"));
                   }
                   try {
                      CompoundTag nbt = new CompoundTag();
                      soldier.saveWithoutId(nbt);
                      tag.put("SavedEntityNbt", nbt);
                   } catch (Throwable ignored) {}
                  String bName = soldier.getPersistentData().getString("shadow_base_name");
                  if (bName == null || bName.isEmpty()) {
                     bName = soldier.getType().getDescription().getString();
                  }

                  tag.putString("ShadowBaseName", bName);
                  CompoundTag eqTag = new CompoundTag();

                  for (EquipmentSlot slot : EquipmentSlot.values()) {
                     ItemStack it = soldier.getItemBySlot(slot);
                     if (!it.isEmpty()) {
                        eqTag.put(slot.getName(), it.saveOptional(soldier.registryAccess()));
                     }
                  }

                  tag.put("Equipment", eqTag);
                  IceAndFireCompat.captureMobData(soldier, tag);
                  var18.storeShadow(tag);
                  sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 25, 0.3, 0.4, 0.3, 0.05);
                  sl.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 30, 0.3, 0.5, 0.3, 0.02);
                  soldier.discard();
                  count++;
               }
            }

            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.shadow_stored.title",
               "awakening_system.notification.shadow_stored.desc",
               -6723841,
               new Object[]{String.valueOf(count), String.valueOf(var18.storedShadowCount())}
            );
            AwakeningSystemMod.sync(player);
            return count;
         }
      }
   }

   public static int deployPlayerShadows(ServerPlayer player, int maxCount) {
      if (player.level() instanceof ServerLevel sl) {
         SystemData data = data(player);
         if (data.storedShadowCount() <= 0) {
            return 0;
         } else {
            int toDeploy = Math.min(maxCount, data.storedShadowCount());

            for (int i = 0; i < toDeploy; i++) {
               CompoundTag shadowTag = data.popStoredShadow();
               if (shadowTag != null) {
                  double angle = Math.toRadians(player.getYRot() + 180.0F + (i % 3 - 1) * 35.0F);
                  double sx = player.getX() + Math.sin(angle) * 2.0;
                  double sz = player.getZ() - Math.cos(angle) * 2.0;
                  summonShadowFromTag(player, shadowTag, sx, player.getY(), sz);
               }
            }

            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.shadow_deployed.title",
               "awakening_system.notification.shadow_deployed.desc",
               -6723841,
               new Object[]{String.valueOf(toDeploy)}
            );
            AwakeningSystemMod.sync(player);
            return toDeploy;
         }
      } else {
         return 0;
      }
   }

   private static void tickShadowSoldiers(MinecraftServer server) {
      if (server.getTickCount() % 20 == 0) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != null && player.isAlive() && !player.hasDisconnected() && player.level() instanceof ServerLevel sl) {
               try {
                  SystemData data = data(player);
                  String masterTag = "shadow_master_" + player.getUUID();
                  List<Mob> soldiers = sl.getEntitiesOfClass(
                     Mob.class,
                     player.getBoundingBox().inflate(128.0),
                     m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
                  );
                  boolean synced = false;
                  boolean lowMana = false;

                  for (Mob soldier : soldiers) {
                     double distSq = soldier.distanceToSqr(player);
                     if (distSq > 3600.0) {
                        double angle = Math.toRadians(player.getYRot() + 180.0F + (soldier.getId() % 5 - 2) * 25.0F);
                        double tx = player.getX() + Math.sin(angle) * 2.5;
                        double tz = player.getZ() - Math.cos(angle) * 2.5;
                        double ty = player.getY();
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
                        sl.sendParticles(ParticleTypes.SMOKE, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 25, 0.3, 0.4, 0.3, 0.02);
                        Vec3 safePos = TeleportHelper.findSafeTeleportPos(sl, soldier, tx, ty, tz, player.position());
                        soldier.teleportTo(safePos.x, safePos.y, safePos.z);
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, safePos.x, safePos.y + 0.5, safePos.z, 25, 0.3, 0.5, 0.3, 0.05);
                        sl.sendParticles(ParticleTypes.PORTAL, safePos.x, safePos.y + 0.5, safePos.z, 20, 0.2, 0.3, 0.2, 0.1);
                        sl.playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.2F);
                        soldier.setTarget(null);
                     }

                     if (!soldier.hasEffect(MobEffects.GLOWING)) {
                        soldier.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false, false));
                     }

                     if (soldier.isOnFire() && !soldier.isInLava()) {
                        soldier.clearFire();
                     }

                     int sRank = soldier.getPersistentData().getInt("shadow_rank");
                     int flameCount = Math.max(1, sRank * 2);
                     sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.3, soldier.getZ(), flameCount, 0.25, 0.3, 0.25, 0.01);
                     if (sRank >= 2) {
                        sl.sendParticles(
                           ParticleTypes.SCULK_SOUL, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), Math.min(3, sRank - 1), 0.2, 0.3, 0.2, 0.01
                        );
                     }

                     if (sRank >= 4) {
                        sl.sendParticles(ParticleTypes.PORTAL, soldier.getX(), soldier.getY() + 0.5, soldier.getZ(), 4, 0.3, 0.4, 0.3, 0.02);
                     }

                     if (soldier.getHealth() < soldier.getMaxHealth()) {
                        if (data.energy() >= 1) {
                           data.spendEnergy(1);
                           soldier.heal(2.5F);
                           sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, soldier.getX(), soldier.getY() + 0.6, soldier.getZ(), 4, 0.2, 0.3, 0.2, 0.02);
                           synced = true;
                        } else {
                           lowMana = true;
                        }
                     }
                  }

                  if (lowMana && player.tickCount % 200 == 0) {
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.notification.shadow_mana_low.title",
                        "awakening_system.notification.shadow_mana_low.desc",
                        -35226,
                        new Object[0]
                     );
                  }

                  if (synced) {
                     AwakeningSystemMod.sync(player);
                  }
               } catch (Exception var22) {
                  AwakeningSystemMod.LOGGER.debug("Error during shadow dismissal: {}", var22.getMessage(), var22);
               }
            }
         }
      }
   }

   public static void triggerDungeonGate(ServerPlayer player, String rank) {
      DungeonManager.startDungeon(player, rank);
   }

   public static boolean prestigePlayer(ServerPlayer player) {
      SystemData data = data(player);
      if (!data.prestige()) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.prestige_failed.title", "awakening_system.notification.prestige_failed.message", -35226, new Object[0]
         );
         return false;
      } else {
         SystemAttributeEffects.apply(player);
         if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.6, 0.8, 0.6, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.9F);
         }

         AwakeningSystemMod.notify(
            player,
            "awakening_system.notification.prestige.title",
            "awakening_system.notification.prestige.message",
            -18324,
            new Object[]{data.prestigeLevel()}
         );
         AwakeningSystemMod.sync(player);
         return true;
      }
   }

   public static void checkAchievements(ServerPlayer player, SystemData data) {
      for (com.soybe.awakeningsystem.AchievementDefinitions.Definition achievement : AchievementDefinitions.definitions()) {
         if (!data.hasAchievement(achievement.id())
            && AchievementDefinitions.statValue(data, achievement.statRequired()) >= achievement.target()
            && data.unlockAchievement(achievement.id())) {
            data.addExperience(achievement.expReward());
            data.addCoins(achievement.coinReward());
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.achievement.title",
               "awakening_system.notification.achievement.message",
               -10138,
               new Object[]{achievement.nameKey()}
            );
         }
      }
   }

   private static void completeAwakening(ServerPlayer player, SystemData data) {
      if (data.completeAwakening()) {
         data.addCoins(100);
         awardExperience(player, data, 250);
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.awakening_complete.title", "awakening_system.notification.awakening_complete.message", -10138, new Object[0]
         );
         player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
         AwakeningSystemMod.sync(player);
      }
   }

   private static void completeClassMission(ServerPlayer player, SystemData data) {
      if (data.completeClassMission()) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.class_complete.title", "awakening_system.notification.class_complete.message", -10138, new Object[0]
         );
      }
   }

   private static boolean updateDailyProgress(ServerPlayer player, SystemData data, long day, int type, int amount) {
      return !SystemConfig.DAILY_MISSIONS_ENABLED.get() ? false : data.recordDailyProgress(day, type, amount);
   }

   private static int learnAndAnnounce(ServerPlayer player, SystemData data, String affinity, int amount, String skillId) {
      boolean before = data.skills().containsKey(skillId);
      int gained = data.learn(affinity, amount);
      if (!before && data.skills().containsKey(skillId)) {
         announceSkill(player, skillId);
      }

      return gained;
   }

   private static boolean gainSkillProgress(ServerPlayer player, SystemData data, String skillId) {
      if (!data.gainSkillProgress(skillId, 1)) {
         return false;
      } else {
         announceSkillLevel(player, skillId, (Integer)data.skills().get(skillId));
         return true;
      }
   }

   private static boolean isOre(BlockState state) {
      return state.is(BlockTags.COAL_ORES)
         || state.is(BlockTags.COPPER_ORES)
         || state.is(BlockTags.IRON_ORES)
         || state.is(BlockTags.GOLD_ORES)
         || state.is(BlockTags.REDSTONE_ORES)
         || state.is(BlockTags.LAPIS_ORES)
         || state.is(BlockTags.DIAMOND_ORES)
         || state.is(BlockTags.EMERALD_ORES);
   }

   public static void awardExperience(ServerPlayer player, SystemData data, int amount) {
      double expMultiplier = (Double)SystemConfig.EXP_MULTIPLIER.get();
      if ((Boolean)SystemConfig.EVENT_NIGHT_BONUS.get() && player.level().isNight()) {
         expMultiplier += 0.25;
      }

      if (data.prestigeLevel() > 0) {
         expMultiplier += data.prestigeLevel() * 0.1;
      }

      if (player.getPersistentData().getInt("UmaBitterJuiceTicks") > 0) {
         expMultiplier *= 2.0;
      }

      if (SystemItems.ANKLE_WEIGHTS != null
         && SystemItems.ANKLE_WEIGHTS.isBound()
         && player.getItemBySlot(EquipmentSlot.FEET).is((Item)SystemItems.ANKLE_WEIGHTS.value())) {
         expMultiplier *= 2.0;
      }

      int finalExp = Math.max(1, (int)Math.round(amount * expMultiplier));
      if (data.addExperience(finalExp)) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.level_up.title", "awakening_system.notification.level_up.message", -10138, new Object[]{data.level()}
         );
         triggerConstellationReaction(player, ReactionType.LEVEL_UP);
         ConstellationAscension.checkLevelMilestone(player, data.level());
         if (data.level() >= 50 && !data.hasHunterClass() && !player.getPersistentData().getBoolean("notified_hunter_class")) {
            player.getPersistentData().putBoolean("notified_hunter_class", true);
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.job_available.title", "awakening_system.notification.job_available.desc", -10496, new Object[0]
            );
         }

         if (data.level() >= 50 && (data.level() - 50) % 5 == 0) {
            data.addSkillPoints(1);
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.sp_gained.title", "awakening_system.notification.sp_gained.desc", -10496, new Object[0]
            );
         }

         if (data.level() >= 100) {
            grantFable(player, "first_level_100");
         }

         if (data.level() >= 50 && (data.level() % 50 == 0) && (data.sponsorConstellation() == null || data.sponsorConstellation().isEmpty())) {
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.constellation_ceremony.title",
               "awakening_system.notification.constellation_ceremony.message",
               -10496,
               new Object[0]
            );
         }
      }

      int rankMilestone;
      while ((rankMilestone = data.claimRankReward()) > 0) {
         AwakeningSystemMod.notify(
            player,
            "awakening_system.notification.rank_reward.title",
            "awakening_system.notification.rank_reward.message",
            -10138,
            new Object[]{SystemData.rankTranslationKey(rankMilestone)}
         );
      }
   }

   private static void announceSkillLevel(ServerPlayer player, String skillId, int level) {
      Definition skill = SkillDefinitions.get(skillId);
      if (skill != null) {
         AwakeningSystemMod.notify(
            player,
            "awakening_system.notification.skill_level.title",
            "awakening_system.notification.skill_level.message",
            -10138,
            new Object[]{skill.nameKey()}
         );
      }
   }

   private static void announceSkill(ServerPlayer player, String skillId) {
      Definition skill = SkillDefinitions.get(skillId);
      if (skill != null) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.skill.title", "awakening_system.notification.skill.message", -1792001, new Object[]{skill.nameKey()}
         );
      }
   }

   public static void announceScenarioComplete(ServerPlayer player, SystemData data) {
      AwakeningSystemMod.notify(
         player,
         "awakening_system.notification.scenario_complete.title",
         "awakening_system.notification.scenario_complete.message",
         -9377537,
         new Object[]{"awakening_system.scenario." + data.activeScenario() + ".name"}
      );
      player.playNotifySound((SoundEvent)ModSounds.SYSTEM_QUEST_COMPLETE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   public static boolean claimScenario(ServerPlayer player) {
      return claimScenarioReward(player);
   }

   public static boolean claimScenarioReward(ServerPlayer player) {
      SystemData data = data(player);
      if (!data.isScenarioComplete()) {
         return false;
      } else {
         String scenario = data.activeScenario();

         int coins = switch (scenario) {
            case "depth_expedition" -> 75;
            case "abyssal_conquest" -> 150;
            case "survival_trial" -> 100;
            default -> 50;
         };
         int exp = 200;
         data.addCoins(coins);
         awardExperience(player, data, exp);
         data.advanceToNextScenario();
         AwakeningSystemMod.notify(
            player,
            "awakening_system.notification.scenario_claimed.title",
            "awakening_system.notification.scenario_claimed.message",
            -11470213,
            new Object[]{String.valueOf(coins)}
         );
         player.playNotifySound((SoundEvent)ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
         AwakeningSystemMod.sync(player);
         return true;
      }
   }

   public static boolean selectHunterClass(ServerPlayer player, String hunterClass) {
      SystemData data = data(player);
      if (data.level() < 50) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.job_low_level.title", "awakening_system.notification.job_low_level.desc", -43691, new Object[0]
         );
         return false;
      } else if (data.hasHunterClass()) {
         AwakeningSystemMod.notify(player, "awakening_system.class.already_chosen_title", "awakening_system.class.already_chosen_locked", -43691, new Object[0]);
         return false;
      } else {
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
               case "beast_monarch":
                  data.skills().put("seismic_roar", 1);
                  break;
               case "blood_emperor":
                  data.skills().put("bloodthirst", 1);
                  break;
               case "lightning_emperor":
                  data.skills().put("electrification", 1);
                  break;
               case "spiritual_healer":
                  data.skills().put("caregiver_blessing", 1);
                  break;
               case "colossal_berserker":
                  data.skills().put("dragon_dive", 1);
            }

            data.setEquippedActiveSkill(0, signatureSkill);
         }
         String className = switch (hunterClass) {
            case "shadow_monarch" -> "§9Shadow Monarch";
            case "sword_saint" -> "§aSword Saint";
            case "star_apostle" -> "§eStar Stream Apostle";
            case "colossal_berserker" -> "§cColossal Berserker";
            case "demonic_cultivator" -> "§5Heavenly Demon";
            case "void_assassin" -> "§bVoid Assassin";
            case "grand_archmage" -> "§dGrand Archmage";
            case "dragon_monarch" -> "§6Dragon Monarch";
            case "beast_monarch" -> "§6Beast Monarch";
            case "blood_emperor" -> "§cBlood Emperor";
            case "lightning_emperor" -> "§bLightning Emperor";
            case "spiritual_healer" -> "§aCelestial Healer";
            default -> hunterClass;
         };
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.job_awakened.title", "awakening_system.notification.job_awakened.desc", -10496, new Object[]{className}
         );
         player.serverLevel()
            .playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.SYSTEM_AWAKENING.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
         int initialSp = 1 + (data.level() - 50) / 5;
         if (data.skillPoints() + data.spentSkillPoints() < initialSp) {
            data.addSkillPoints(initialSp - (data.skillPoints() + data.spentSkillPoints()));
         }

         grantFable(player, "first_awakening");
         SystemAttributeEffects.apply(player);
         AwakeningSystemMod.sync(player);
         return true;
      }
   }

   public static boolean isPlayerNearResonanceAltar(ServerPlayer player) {
      if (player.isCreative()) {
         return true;
      } else {
         BlockPos pPos = player.blockPosition();
         Level level = player.level();
         int r = 12;

         for (BlockPos pos : BlockPos.betweenClosed(pPos.offset(-r, -6, -r), pPos.offset(r, 6, r))) {
            Block block = level.getBlockState(pos).getBlock();
            if (block instanceof StatueOfTheSevenBlock || block instanceof ElementalMonumentBlock) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean consumeSigils(ServerPlayer player, String vision, int amount) {
      if (player.isCreative()) {
         return true;
      } else {
         Item primarySigil = switch (vision) {
            case "ANEMO" -> (Item)SystemItems.ANEMO_SIGIL.get();
            case "GEO" -> (Item)SystemItems.GEO_SIGIL.get();
            case "ELECTRO" -> (Item)SystemItems.ELECTRO_SIGIL.get();
            case "DENDRO" -> (Item)SystemItems.DENDRO_SIGIL.get();
            case "HYDRO" -> (Item)SystemItems.HYDRO_SIGIL.get();
            case "PYRO" -> (Item)SystemItems.PYRO_SIGIL.get();
            case "CRYO" -> (Item)SystemItems.CRYO_SIGIL.get();
            default -> null;
         };
         Item wildcard1 = (Item)SystemItems.GUARDIANS_SIGIL.get();
         Item wildcard2 = (Item)SystemItems.LIGHT_REALM_SIGIL.get();
         int totalAvailable = 0;

         for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               if (primarySigil != null && stack.is(primarySigil)) {
                  totalAvailable += stack.getCount();
               } else if (stack.is(wildcard1) || stack.is(wildcard2)) {
                  totalAvailable += stack.getCount();
               }
            }
         }

         if (totalAvailable < amount) {
            return false;
         } else {
            int needed = amount;

            for (int ix = 0; ix < player.getInventory().items.size() && needed > 0; ix++) {
               ItemStack stack = player.getInventory().getItem(ix);
               if (!stack.isEmpty() && primarySigil != null && stack.is(primarySigil)) {
                  int take = Math.min(stack.getCount(), needed);
                  stack.shrink(take);
                  needed -= take;
               }
            }

            for (int ixx = 0; ixx < player.getInventory().items.size() && needed > 0; ixx++) {
               ItemStack stack = player.getInventory().getItem(ixx);
               if (!stack.isEmpty() && (stack.is(wildcard1) || stack.is(wildcard2))) {
                  int take = Math.min(stack.getCount(), needed);
                  stack.shrink(take);
                  needed -= take;
               }
            }

            return true;
         }
      }
   }

   public static void handleSelectVision(ServerPlayer player, String vision) {
      SystemData data = data(player);
      String v = vision != null && !vision.isBlank() ? vision.trim().toUpperCase(Locale.ROOT) : "NONE";
      if ("NONE".equalsIgnoreCase(v)) {
         data.setActiveVision("NONE");
         AwakeningSystemMod.sync(player);
         player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
         player.displayClientMessage(Component.translatable("awakening_system.vision.removed_resonance"), true);
      } else if (!isPlayerNearResonanceAltar(player)) {
         player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
         player.displayClientMessage(Component.translatable("awakening_system.vision.need_statue_or_monument"), true);
      } else {
         boolean isInitial = !data.hasChosenInitialVision() || "NONE".equalsIgnoreCase(data.activeVision());
         long now = player.level().getGameTime();
         if (!isInitial && now < data.getVisionResonanceCooldownUntil() && !player.isCreative()) {
            long secRemaining = Math.max(1L, (data.getVisionResonanceCooldownUntil() - now + 19L) / 20L);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("awakening_system.vision.resonance_cooldown", new Object[]{secRemaining}), true);
         } else {
            int requiredSigils = 5;
            if (!isInitial && !consumeSigils(player, v, requiredSigils)) {
               Component sigilComp = switch (v) {
                  case "ANEMO" -> Component.translatable("item.awakening_system.anemo_sigil");
                  case "GEO" -> Component.translatable("item.awakening_system.geo_sigil");
                  case "ELECTRO" -> Component.translatable("item.awakening_system.electro_sigil");
                  case "DENDRO" -> Component.translatable("item.awakening_system.dendro_sigil");
                  case "HYDRO" -> Component.translatable("item.awakening_system.hydro_sigil");
                  case "PYRO" -> Component.translatable("item.awakening_system.pyro_sigil");
                  case "CRYO" -> Component.translatable("item.awakening_system.cryo_sigil");
                  default -> Component.translatable("item.awakening_system.guardian_sigil");
               };
               player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
               player.displayClientMessage(Component.translatable("awakening_system.vision.requires_sigils", new Object[]{sigilComp}), true);
            } else {
               data.setActiveVision(v);
               data.setChosenInitialVision(true);
               data.setVisionResonanceCooldownUntil(now + 6000L);
               AwakeningSystemMod.sync(player);
               player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
               player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.3F);
               player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);

               Component elemComp = switch (v) {
                  case "PYRO" -> Component.translatable("awakening_system.element.pyro")
                     .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
                  case "HYDRO" -> Component.translatable("awakening_system.element.hydro")
                     .withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD});
                  case "CRYO" -> Component.translatable("awakening_system.element.cryo")
                     .withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD});
                  case "ANEMO" -> Component.translatable("awakening_system.element.anemo")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD});
                  case "ELECTRO" -> Component.translatable("awakening_system.element.electro")
                     .withStyle(new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD});
                  case "GEO" -> Component.translatable("awakening_system.element.geo")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
                  case "DENDRO" -> Component.translatable("awakening_system.element.dendro")
                     .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GREEN, ChatFormatting.BOLD});
                  default -> Component.literal(v);
               };
               player.displayClientMessage(Component.translatable("awakening_system.vision.resonated", new Object[]{elemComp}), false);
               player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 60, 0.6, 0.8, 0.6, 0.2);
               player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.5, player.getZ(), 40, 0.5, 0.7, 0.5, 0.1);
            }
         }
      }
   }

   public static void tickPlayerVision(ServerPlayer player, SystemData data) {
      String vision = data.activeVision();
      if (vision != null && !"NONE".equalsIgnoreCase(vision)) {
         ServerLevel level = player.serverLevel();
         int tick = player.tickCount;
         switch (vision) {
            case "PYRO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
               }

               BlockPos under = player.blockPosition().below();
               if (level.getBlockState(under).is(Blocks.WATER) && tick % 4 == 0) {
                  level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, 0.02);
                  level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.1, 0.02, 0.1, 0.01);
               }
               break;
            case "HYDRO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, false, false, true));
                  if (player.isInWater()) {
                     player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, false, false, true));
                  }
               }

               if (player.isOnFire()) {
                  player.clearFire();
                  level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
                  level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.8F, 1.2F);
               }
               break;
            case "CRYO":
               if (player.getTicksFrozen() > 0) {
                  player.setTicksFrozen(0);
               }

               if (player.onGround()) {
                  BlockPos cryoUnder = player.blockPosition().below();

                  for (BlockPos p : BlockPos.betweenClosed(cryoUnder.offset(-1, 0, -1), cryoUnder.offset(1, 0, 1))) {
                     if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                        level.scheduleTick(p, Blocks.FROSTED_ICE, Mth.nextInt(level.random, 60, 120));
                     }
                  }
               }
               break;
            case "ANEMO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, false));
                  player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false, false));
               }

               player.fallDistance = 0.0F;
               if (!player.onGround() && player.isShiftKeyDown() && player.getDeltaMovement().y < -0.1) {
                  player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false, false));
                  if (tick % 3 == 0) {
                     level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.15, 0.02, 0.15, 0.02);
                  }
               }
               break;
            case "ELECTRO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, false, false, false));
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, false));
               }
               break;
            case "GEO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, false, false, true));
               }
               break;
            case "DENDRO":
               if (tick % 20 == 0) {
                  player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
               }

               if (player.isShiftKeyDown() && tick % 40 == 0) {
                  BlockPos p = player.blockPosition();

                  for (BlockPos target : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                     BlockState state = level.getBlockState(target);
                     if (state.getBlock() instanceof BonemealableBlock bm && bm.isValidBonemealTarget(level, target, state)) {
                        bm.performBonemeal(level, level.random, target, state);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
                        break;
                     }
                  }
               }
         }
      }
   }

   public static boolean unlockSkillTreeNode(ServerPlayer player, String skillId) {
      SystemData data = data(player);
      if (!data.hasHunterClass()) {
         return false;
      } else {
         SkillNode node = ClassSkillTree.getNode(data.hunterClass(), skillId);
         if (node == null) {
            return false;
         } else if (!ClassSkillTree.canUnlock(data, skillId)) {
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.skill_req_not_met.title", "awakening_system.notification.skill_req_not_met.desc", -43691, new Object[0]
            );
            return false;
         } else if (node.spCost() > 0 && !data.spendSkillPoints(node.spCost())) {
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.skill_sp_needed.title",
               "awakening_system.notification.skill_sp_needed.desc",
               -43691,
               new Object[]{node.spCost()}
            );
            return false;
         } else {
            data.skills().put(skillId, 1);
            String sName = node.getName().getString();
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.skill_learned.title", "awakening_system.notification.skill_learned.desc", -10496, new Object[]{sName}
            );
            player.serverLevel()
               .playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
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
      }
   }

   public static boolean upgradeSkillTreeNode(ServerPlayer player, String skillId) {
      SystemData data = data(player);
      if (!data.hasHunterClass()) {
         return false;
      } else {
         SkillNode node = ClassSkillTree.getNode(data.hunterClass(), skillId);
         if (node == null) {
            return false;
         } else if (!ClassSkillTree.canUpgrade(data, skillId)) {
            AwakeningSystemMod.notify(
               player, "awakening_system.notification.skill_upgrade_req.title", "awakening_system.notification.skill_upgrade_req.desc", -43691, new Object[0]
            );
            return false;
         } else {
            int currentLvl = data.skills().getOrDefault(skillId, 1);
            int targetLvl = currentLvl + 1;
            int spCost = ClassSkillTree.getSpCostForRank(node, targetLvl);
            if (!data.spendSkillPoints(spCost)) {
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_upgrade_sp.title",
                  "awakening_system.notification.skill_upgrade_sp.desc",
                  -43691,
                  new Object[]{spCost}
               );
               return false;
            } else {
               data.skills().put(skillId, targetLvl);
               String sName = node.getName().getString();
               String roman = targetLvl == 2 ? "II" : "III (DEFINITIVE MASTERY)";
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.skill_upgraded.title",
                  "awakening_system.notification.skill_upgraded.desc",
                  -10496,
                  new Object[]{sName, roman}
               );
               player.serverLevel()
                  .playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.SYSTEM_SKILL_UNLOCK.value(), SoundSource.PLAYERS, 1.3F, 1.1F
                  );
               player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.6, 0.4, 0.08);
               SystemAttributeEffects.apply(player);
               AwakeningSystemMod.sync(player);
               return true;
            }
         }
      }
   }

   public static boolean selectConstellation(ServerPlayer player, String constellationId) {
      SystemData data = data(player);
      if (data.level() < 100) {
         AwakeningSystemMod.notify(
            player, "awakening_system.notification.constellation.failed_title", "awakening_system.screen.sponsor.locked_desc", -43691, new Object[0]
         );
         return false;
      } else if (!data.setSponsorConstellation(constellationId)) {
         return false;
      } else {
         Constellation constellation = ConstellationDefinitions.get(constellationId);
         if (constellation != null) {
            if (constellation.defaultDonation() > 0) {
               data.addCoins(constellation.defaultDonation());
            }

            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.constellation.title",
               "awakening_system.notification.constellation.sponsored",
               -3123713,
               new Object[]{constellation.nameKey()}
            );
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
         }

         SystemAttributeEffects.apply(player);
         AwakeningSystemMod.sync(player);
         return true;
      }
   }

   public static void triggerConstellationReaction(ServerPlayer player, ReactionType type) {
      SystemData data = data(player);
      if (data != null) {
         if (type == ReactionType.LEVEL_UP) {
            long now = System.currentTimeMillis();
            Long last = LAST_LEVEL_UP_CONSTELLATION_REACTION.get(player.getUUID());
            if (last != null && now - last < 20000L) {
               return;
            }

            LAST_LEVEL_UP_CONSTELLATION_REACTION.put(player.getUUID(), now);
         }

         if (type == ReactionType.LOW_HEALTH || type == ReactionType.COMBO_TRANCE) {
            long now = System.currentTimeMillis();
            Long last = LAST_COMBAT_CONSTELLATION_REACTION.get(player.getUUID());
            if (last != null && now - last < 35000L) {
               return;
            }

            LAST_COMBAT_CONSTELLATION_REACTION.put(player.getUUID(), now);
         }

         String sponsorId = data.sponsorConstellation();
         Constellation contractedSponsor = ConstellationDefinitions.get(sponsorId);
         if (type == ReactionType.BOSS_KILL) {
            if (contractedSponsor != null) {
               boolean isExorbitant = player.getRandom().nextFloat() < 0.2F;
               int officialDonation = isExorbitant ? 3000 + player.getRandom().nextInt(3001) : 600 + player.getRandom().nextInt(901);
               if ("queen_of_the_darkest_night".equals(contractedSponsor.id())) {
                  officialDonation = (int)(officialDonation * 1.5);
               }

               data.addCoins(officialDonation);
               String officialMsgKey = ConstellationDefinitions.reactionKey(contractedSponsor.id(), type);
               AwakeningSystemMod.notify(
                  player,
                  "awakening_system.notification.official_sponsor_donation.title",
                  officialMsgKey,
                  -10138,
                  new Object[]{contractedSponsor.nameKey(), String.valueOf(officialDonation)}
               );
            }

            Constellation guest = ConstellationDefinitions.randomOtherConstellation(contractedSponsor != null ? contractedSponsor.id() : null);
            boolean isGuestExorbitant = player.getRandom().nextFloat() < 0.12F;
            int guestDonation = isGuestExorbitant ? 2000 + player.getRandom().nextInt(2001) : 350 + player.getRandom().nextInt(451);
            if ("queen_of_the_darkest_night".equals(sponsorId)) {
               guestDonation = (int)(guestDonation * 1.5);
            }

            data.addCoins(guestDonation);
            String guestMsgKey = ConstellationDefinitions.reactionKey(guest.id(), type);
            AwakeningSystemMod.notify(
               player,
               "awakening_system.notification.constellation_donation.title",
               guestMsgKey,
               -9377537,
               new Object[]{guest.nameKey(), String.valueOf(guestDonation)}
            );
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.2F);
         } else {
            boolean isOfficial = contractedSponsor != null && player.getRandom().nextFloat() < 0.4F;
            Constellation reactor = isOfficial
               ? contractedSponsor
               : ConstellationDefinitions.randomOtherConstellation(contractedSponsor != null ? contractedSponsor.id() : null);

            int baseDonation = switch (type) {
               case LEVEL_UP -> 100 + player.getRandom().nextInt(151);
               case LOW_HEALTH -> 60 + player.getRandom().nextInt(61);
               case DUNGEON_ENTER -> 50 + player.getRandom().nextInt(51);
               case COMBO_TRANCE -> 75 + player.getRandom().nextInt(76);
               default -> 60;
            };
            int finalDonation = isOfficial ? (int)(baseDonation * 1.5) : baseDonation;
            if ("queen_of_the_darkest_night".equals(sponsorId)) {
               finalDonation = (int)(finalDonation * 1.5);
            }

            data.addCoins(finalDonation);
            String titleKey = isOfficial
               ? "awakening_system.notification.official_sponsor_donation.title"
               : "awakening_system.notification.constellation_donation.title";
            int color = isOfficial ? -10138 : -10496;
            String msgKey = ConstellationDefinitions.reactionKey(reactor.id(), type);
            AwakeningSystemMod.notify(player, titleKey, msgKey, color, new Object[]{reactor.nameKey(), String.valueOf(finalDonation)});
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);
         }

         AwakeningSystemMod.sync(player);
      }
   }


   public static boolean isPagodaSanctumGate(ServerLevel level, BlockPos pos) {
      if (level == null || pos == null) return false;
      var server = level.getServer();
      if (server != null) {
         var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);
         if (data != null && data.hasStructureNearbyOfType("nine_tribulations_pagoda", pos, 60.0)) {
            return true;
         }
      }

      boolean hasBaseGilded = level.getBlockState(pos.below()).is(Blocks.GILDED_BLACKSTONE) || level.getBlockState(pos.below(2)).is(Blocks.GILDED_BLACKSTONE);
      boolean hasSurroundings = level.getBlockState(pos.east()).is(Blocks.CRYING_OBSIDIAN)
            || level.getBlockState(pos.west()).is(Blocks.CRYING_OBSIDIAN)
            || level.getBlockState(pos.north(2)).is(Blocks.RESPAWN_ANCHOR)
            || level.getBlockState(pos.south(2)).is(Blocks.RESPAWN_ANCHOR);
      if (hasBaseGilded && hasSurroundings) return true;

      BlockState below = level.getBlockState(pos.below());
      BlockState below2 = level.getBlockState(pos.below(2));
      boolean isPillarFloor = below.is(Blocks.GILDED_BLACKSTONE) || below.is(Blocks.DEEPSLATE_TILES) || below.is(Blocks.DEEPSLATE_BRICKS)
            || below.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || below.is(Blocks.POLISHED_BLACKSTONE)
            || below2.is(Blocks.GILDED_BLACKSTONE) || below2.is(Blocks.DEEPSLATE_TILES);

      BlockState east = level.getBlockState(pos.east());
      BlockState west = level.getBlockState(pos.west());
      BlockState north = level.getBlockState(pos.north());
      BlockState south = level.getBlockState(pos.south());
      boolean isPillarWall = east.is(Blocks.DEEPSLATE_BRICKS) || west.is(Blocks.DEEPSLATE_BRICKS)
            || north.is(Blocks.DEEPSLATE_BRICKS) || south.is(Blocks.DEEPSLATE_BRICKS)
            || east.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || west.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
            || east.is(Blocks.POLISHED_DEEPSLATE) || west.is(Blocks.POLISHED_DEEPSLATE);

      return isPillarFloor && isPillarWall;
   }

   private static void handleSeriesStructureInteractions(ServerPlayer player, RightClickBlock event) {
      ServerLevel level = player.serverLevel();
      BlockPos pos = event.getPos();
      BlockState state = level.getBlockState(pos);
      if (PagodaTrialManager.isInTrial(player.getUUID())) {
         PagodaTrialManager.handleDoorInteraction(player, pos);
         event.setCanceled(true);
      } else {
         if (state.is(Blocks.CRIMSON_DOOR)) {
            if (isPagodaSanctumGate(level, pos)) {
               long cd = player.getPersistentData().getLong("pagoda_exit_cooldown");
               if (level.getGameTime() < cd) {
                  return;
               }
               PagodaTrialManager.enterTrial(player);
               event.setCanceled(true);
               return;
            }
         }

         BlockPos boardPos = BountyBoardBlock.findNearbyBountyBoard(level, pos, event.getFace());
         if (boardPos != null) {
            BountyBoardBlock.openBoardFor(player);
            event.setCanceled(true);
         } else {
            if (state.is(Blocks.BELL)) {
               AABB searchBox = new AABB(pos).inflate(16.0);
               boolean nearYeowoon = !level.getEntitiesOfClass(SeriesNpcEntity.class, searchBox, e -> e.getNpcProfile() == NpcProfile.CHEON_YEOWOON).isEmpty();
               if (nearYeowoon) {
                  level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.8F);
                  level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.05);
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1, false, true));
                  AwakeningSystemMod.notify(
                     player, "awakening_system.event.demonic_bell_title", "awakening_system.event.demonic_bell_msg", -56798, new Object[0]
                  );
                  event.setCanceled(true);
                  return;
               }
            }

            if (state.is(Blocks.OCHRE_FROGLIGHT) || state.is(Blocks.BLACK_CONCRETE)) {
               AABB searchBox = new AABB(pos).inflate(16.0);
               boolean nearDokja = !level.getEntitiesOfClass(SeriesNpcEntity.class, searchBox, e -> e.getNpcProfile() == NpcProfile.KIM_DOKJA).isEmpty();
               if (nearDokja) {
                  String tag = "claimed_orv_stream_" + level.getDayTime() / 24000L;
                  if (!player.getTags().contains(tag)) {
                     player.addTag(tag);
                     SystemData data = data(player);
                     data.addCoins(100);
                     AwakeningSystemMod.sync(player);
                     level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                     level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.5, 0.5, 0.5, 0.1);
                     AwakeningSystemMod.notify(
                        player, "awakening_system.event.bihyung_channel_title", "awakening_system.event.bihyung_channel_msg", -10496, new Object[0]
                     );
                  } else {
                     level.playSound(null, pos, (SoundEvent)SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 1.5F);
                     AwakeningSystemMod.notify(
                        player,
                        "awakening_system.event.bihyung_already_claimed_title",
                        "awakening_system.event.bihyung_already_claimed_msg",
                        -5592406,
                        new Object[0]
                     );
                  }

                  event.setCanceled(true);
                  return;
               }
            }

            if (state.is(Blocks.RESPAWN_ANCHOR)) {
               AABB searchBox = new AABB(pos).inflate(16.0);
               boolean nearArthur = !level.getEntitiesOfClass(SeriesNpcEntity.class, searchBox, e -> e.getNpcProfile() == NpcProfile.ARTHUR_LEYWIN).isEmpty();
               if (nearArthur) {
                  level.playSound(null, pos, (SoundEvent)SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                  level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 35, 0.6, 0.6, 0.6, 0.1);
                  player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2, false, true));
                  player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true));
                  AwakeningSystemMod.notify(
                     player, "awakening_system.event.death_king_altar_title", "awakening_system.event.death_king_altar_msg", -11141121, new Object[0]
                  );
                  event.setCanceled(true);
                  return;
               }
            }

            if (state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.MAGMA_BLOCK)) {
               AABB searchBox = new AABB(pos).inflate(16.0);
               boolean nearYeonwoo = !level.getEntitiesOfClass(SeriesNpcEntity.class, searchBox, e -> e.getNpcProfile() == NpcProfile.CHA_YEONWOO).isEmpty();
               if (nearYeonwoo) {
                  level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 0.8F, 1.3F);
                  level.sendParticles(ParticleTypes.DRAGON_BREATH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.05);
                  player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true));
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1, false, true));
                  AwakeningSystemMod.notify(
                     player, "awakening_system.event.black_dragon_heart_title", "awakening_system.event.black_dragon_heart_msg", -52429, new Object[0]
                  );
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   private static void tickStructureAmbientEffects(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      BlockPos p = player.blockPosition();
      BlockState state = level.getBlockState(p);
      BlockState below = level.getBlockState(p.below());
      if (state.is(Blocks.WATER)) {
         AABB box = new AABB(p).inflate(12.0);
         boolean nearArthur = !level.getEntitiesOfClass(SeriesNpcEntity.class, box, e -> e.getNpcProfile() == NpcProfile.ARTHUR_LEYWIN).isEmpty();
         if (nearArthur) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, false));
            level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0.05);
         }
      }

      if (below.is(Blocks.CRYING_OBSIDIAN) || below.is(Blocks.SEA_LANTERN)) {
         AABB box = new AABB(p).inflate(10.0);
         boolean nearJinwoo = !level.getEntitiesOfClass(SeriesNpcEntity.class, box, e -> e.getNpcProfile() == NpcProfile.SUNG_JINWOO).isEmpty();
         if (nearJinwoo) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, false, false, false));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 3, 0.2, 0.1, 0.2, 0.02);
         }
      }

      if (below.is(Blocks.ORANGE_STAINED_GLASS) || below.is(Blocks.BRICKS)) {
         AABB box = new AABB(p).inflate(10.0);
         boolean nearLloyd = !level.getEntitiesOfClass(SeriesNpcEntity.class, box, e -> e.getNpcProfile() == NpcProfile.LLOYD_FRONTERA).isEmpty();
         if (nearLloyd) {
            player.setTicksFrozen(0);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, false, false, false));
            if (player.tickCount % 40 == 0) {
               level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.1, 0.2, 0.01);
            }
         }
      }
   }

   public static int getDokkaebiBaseCoins(int target) {
      if (target <= 10) {
         return 250;
      } else if (target <= 17) {
         return 400;
      } else {
         return target <= 24 ? 600 : 850;
      }
   }

   public static int getDokkaebiBaseExp(int target) {
      if (target <= 10) {
         return 400;
      } else if (target <= 17) {
         return 700;
      } else {
         return target <= 24 ? 1100 : 1600;
      }
   }

   public static int getOverkillCoinRate(int extra) {
      if (extra <= 5) {
         return 50;
      } else if (extra <= 10) {
         return 75;
      } else {
         return extra <= 20 ? 100 : 150;
      }
   }

   public static int getOverkillExpRate(int extra) {
      if (extra <= 5) {
         return 80;
      } else if (extra <= 10) {
         return 120;
      } else {
         return extra <= 20 ? 160 : 250;
      }
   }

   public static Component getOverkillRankTag(int extra) {
      if (extra >= 20) {
         return Component.translatable("awakening_system.dokkaebi.rank_calamity_tag");
      } else if (extra >= 15) {
         return Component.translatable("awakening_system.dokkaebi.rank_massacre_tag");
      } else if (extra >= 10) {
         return Component.translatable("awakening_system.dokkaebi.rank_frenzy_tag");
      } else {
         return extra >= 5 ? Component.translatable("awakening_system.dokkaebi.rank_overload_tag") : Component.empty();
      }
   }

   public static SystemEvents.DokkaebiOverkillReward calculateDokkaebiOverkill(int extraKills) {
      if (extraKills <= 0) {
         return new SystemEvents.DokkaebiOverkillReward(0, 0, 0, Component.translatable("awakening_system.dokkaebi.rank_none"), ItemStack.EMPTY);
      } else {
         int totalBonusCoins = 0;
         int totalBonusExp = 0;

         for (int i = 1; i <= extraKills; i++) {
            totalBonusCoins += getOverkillCoinRate(i);
            totalBonusExp += getOverkillExpRate(i);
         }

         int donation = 0;
         Component rank = Component.translatable("awakening_system.dokkaebi.rank_overload_basic");
         ItemStack gift = ItemStack.EMPTY;
         if (extraKills >= 30) {
            donation = 2500;
            rank = Component.translatable("awakening_system.dokkaebi.rank_absolute_calamity");
            gift = new ItemStack((ItemLike)SystemItems.MANA_CRYSTAL_SHARD_A.get(), 1);
         } else if (extraKills >= 20) {
            donation = 1200;
            rank = Component.translatable("awakening_system.dokkaebi.rank_calamity");
            gift = new ItemStack((ItemLike)SystemItems.MANA_CRYSTAL_SHARD_B.get(), 1);
         } else if (extraKills >= 15) {
            donation = 700;
            rank = Component.translatable("awakening_system.dokkaebi.rank_massacre");
            gift = new ItemStack((ItemLike)SystemItems.MANA_CRYSTAL_SHARD_C.get(), 1);
         } else if (extraKills >= 10) {
            donation = 350;
            rank = Component.translatable("awakening_system.dokkaebi.rank_frenzy");
            gift = new ItemStack((ItemLike)SystemItems.MANA_CRYSTAL_SHARD_D.get(), 1);
         } else if (extraKills >= 5) {
            donation = 150;
            rank = Component.translatable("awakening_system.dokkaebi.rank_overload");
            gift = new ItemStack((ItemLike)SystemItems.MANA_CRYSTAL_SHARD_E.get(), 1);
         }

         return new SystemEvents.DokkaebiOverkillReward(totalBonusCoins, totalBonusExp, donation, rank, gift);
      }
   }

   public static DokkaebiEntity spawnServerDokkaebi(ServerPlayer player, int target, int durationTicks, int baseCoins) {
      ServerLevel level = player.serverLevel();
      AABB nearby = player.getBoundingBox().inflate(48.0);
      List<DokkaebiEntity> list = level.getEntitiesOfClass(DokkaebiEntity.class, nearby);
      DokkaebiEntity dokkaebi;
      if (!list.isEmpty()) {
         dokkaebi = list.get(0);
      } else {
         dokkaebi = new DokkaebiEntity((EntityType)ModEntities.DOKKAEBI.get(), level);
         double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
         double dist = 2.5 + player.getRandom().nextDouble() * 1.5;
         double spawnX = player.getX() + Math.cos(angle) * dist;
         double spawnZ = player.getZ() + Math.sin(angle) * dist;
         double spawnY = player.getY() + 1.2;
         BlockPos checkPos = BlockPos.containing(spawnX, spawnY, spawnZ);
         if (level.getBlockState(checkPos).isSolid() || level.getBlockState(checkPos.above()).isSolid()) {
            Vec3 look = player.getLookAngle();
            spawnX = player.getX() + look.x * 2.0;
            spawnZ = player.getZ() + look.z * 2.0;
            spawnY = player.getEyeY() - 0.2;
            if (level.getBlockState(BlockPos.containing(spawnX, spawnY, spawnZ)).isSolid()) {
               spawnX = player.getX();
               spawnY = player.getEyeY();
               spawnZ = player.getZ();
            }
         }

         dokkaebi.moveTo(spawnX, spawnY, spawnZ, player.getYRot() + 180.0F, 0.0F);
         dokkaebi.bindPlayer(player);
         level.addFreshEntity(dokkaebi);
      }

      dokkaebi.setQuestDetails(
         "kill_monsters",
         "awakening_system.dokkaebi.quest.kill_monsters.title",
         "awakening_system.dokkaebi.quest.kill_monsters.desc",
         target,
         durationTicks,
         baseCoins
      );
      level.playSound(null, dokkaebi.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.2F, 1.3F);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, dokkaebi.getX(), dokkaebi.getY() + 0.5, dokkaebi.getZ(), 45, 0.5, 0.6, 0.5, 0.2);
      return dokkaebi;
   }

   public static boolean triggerServerDokkaebiEvent(MinecraftServer server, int forcedTarget) {
      if (server == null) {
         return false;
      } else {
         List<ServerPlayer> eligiblePlayers = new ArrayList<>();

         for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.isAlive() && !sp.isSpectator() && !DungeonManager.isInDungeon(sp.getUUID()) && isDokkaebiAllowedInDimension(sp.serverLevel().dimension())) {
               SystemData pData = data(sp);
               if (!pData.isDokkaebiScenarioActive()) {
                  AABB nearby = sp.getBoundingBox().inflate(64.0);
                  if (sp.serverLevel().getEntitiesOfClass(DokkaebiEntity.class, nearby).isEmpty()) {
                     eligiblePlayers.add(sp);
                  }
               }
            }
         }

         if (eligiblePlayers.isEmpty()) {
            return false;
         } else {
            int maxLevel = 1;

            for (ServerPlayer spx : eligiblePlayers) {
               maxLevel = Math.max(maxLevel, data(spx).level());
            }

            RandomSource rng = server.overworld().getRandom();
            int durationMinutes;
            int targetKills;
            int durationTicks;
            if (forcedTarget > 0) {
               targetKills = forcedTarget;
               if (forcedTarget <= 10) {
                  durationTicks = 7200;
                  durationMinutes = 6;
               } else if (forcedTarget <= 18) {
                  durationTicks = 9600;
                  durationMinutes = 8;
               } else if (forcedTarget <= 24) {
                  durationTicks = 12000;
                  durationMinutes = 10;
               } else {
                  durationTicks = 14400;
                  durationMinutes = 12;
               }
            } else {
               int tier;
               if (maxLevel < 25) {
                  tier = 1;
               } else if (maxLevel < 45) {
                  tier = rng.nextFloat() < 0.4F ? 1 : 2;
               } else if (maxLevel < 70) {
                  tier = rng.nextFloat() < 0.5F ? 2 : 3;
               } else {
                  tier = rng.nextFloat() < 0.4F ? 3 : 4;
               }

               switch (tier) {
                  case 2:
                     targetKills = 15 + rng.nextInt(4);
                     durationTicks = 9600;
                     durationMinutes = 8;
                     break;
                  case 3:
                     targetKills = 20 + rng.nextInt(5);
                     durationTicks = 12000;
                     durationMinutes = 10;
                     break;
                  case 4:
                     targetKills = 25 + rng.nextInt(8);
                     durationTicks = 14400;
                     durationMinutes = 12;
                     break;
                  default:
                     targetKills = 8 + rng.nextInt(3);
                     durationTicks = 7200;
                     durationMinutes = 6;
               }
            }

            ServerPlayer anchorPlayer = null;
            if (eligiblePlayers.size() > 1 && lastDokkaebiAnchorUuid != null) {
               List<ServerPlayer> others = eligiblePlayers.stream().filter(p -> !p.getUUID().equals(lastDokkaebiAnchorUuid)).toList();
               if (!others.isEmpty()) {
                  anchorPlayer = others.get(rng.nextInt(others.size()));
               }
            }

            if (anchorPlayer == null) {
               anchorPlayer = eligiblePlayers.get(rng.nextInt(eligiblePlayers.size()));
            }

            lastDokkaebiAnchorUuid = anchorPlayer.getUUID();
            spawnServerDokkaebi(anchorPlayer, targetKills, durationTicks, getDokkaebiBaseCoins(targetKills));

            for (ServerPlayer spx : eligiblePlayers) {
               SystemData spData = data(spx);
               spData.startDokkaebiScenario("awakening_system.dokkaebi.quest.kill_monsters.title", durationTicks, targetKills);
               AwakeningSystemMod.sync(spx);
               AwakeningSystemMod.notify(
                  spx,
                  "awakening_system.notification.dokkaebi_scenario_start.title",
                  "awakening_system.notification.dokkaebi_scenario_start.desc",
                  -48060,
                  new Object[]{targetKills, durationMinutes}
               );
               spx.level().playSound(null, spx.getX(), spx.getY(), spx.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5F, 0.8F);
               spx.level()
                  .playSound(null, spx.getX(), spx.getY(), spx.getZ(), (SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
               spx.sendSystemMessage(Component.translatable("awakening_system.dokkaebi.broadcast_announcement", new Object[]{targetKills, durationMinutes}));
            }

            return true;
         }
      }
   }

   private static void tickDokkaebiServerEvent(MinecraftServer server) {
      if (server != null) {
         long tickCount = server.getTickCount();
         if (tickCount > 0L && tickCount % 72000L == 0L) {
            if (!(server.overworld().getRandom().nextFloat() >= 0.5F)) {
               boolean anyActive = false;

               for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                  if (data(sp).isDokkaebiScenarioActive()) {
                     anyActive = true;
                     break;
                  }
               }

               if (!anyActive) {
                  triggerServerDokkaebiEvent(server, 0);
               }
            }
         }
      }
   }

   public static boolean isDokkaebiAllowedInDimension(ResourceKey<Level> dim) {
      if (dim == null) {
         return false;
      } else {
         String path = dim.location().getPath();
         return !path.equals("limbo")
            && !path.equals("subspace_realm")
            && !path.equals("dungeon")
            && !path.equals("ranker_tower")
            && !path.equals("pagoda_realm");
      }
   }

   @SubscribeEvent
   public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
      ItemStack stack = event.getItemStack();
      if (stack.is(Items.NETHERITE_CHESTPLATE)) {
         event.addModifier(Attributes.ARMOR, new AttributeModifier(NETHERITE_CHEST_BUFF, 1.0, Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
      } else if (stack.is(Items.NETHERITE_LEGGINGS)) {
         event.addModifier(Attributes.ARMOR, new AttributeModifier(NETHERITE_LEGS_BUFF, 1.0, Operation.ADD_VALUE), EquipmentSlotGroup.LEGS);
      }
   }

   public static void spawnDokkaebiNearPlayer(ServerPlayer player) {
      if (isDokkaebiAllowedInDimension(player.serverLevel().dimension())) {
         if (!data(player).isDokkaebiScenarioActive()) {
            ServerLevel level = player.serverLevel();
            AABB nearby = player.getBoundingBox().inflate(64.0);
            if (level.getEntitiesOfClass(DokkaebiEntity.class, nearby).isEmpty()) {
               DokkaebiEntity dokkaebi = new DokkaebiEntity((EntityType)ModEntities.DOKKAEBI.get(), level);
               double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
               double dist = 2.5 + player.getRandom().nextDouble() * 1.5;
               double spawnX = player.getX() + Math.cos(angle) * dist;
               double spawnZ = player.getZ() + Math.sin(angle) * dist;
               double spawnY = player.getY() + 1.2;
               BlockPos checkPos = BlockPos.containing(spawnX, spawnY, spawnZ);
               if (level.getBlockState(checkPos).isSolid() || level.getBlockState(checkPos.above()).isSolid()) {
                  Vec3 look = player.getLookAngle();
                  spawnX = player.getX() + look.x * 2.0;
                  spawnZ = player.getZ() + look.z * 2.0;
                  spawnY = player.getEyeY() - 0.2;
                  if (level.getBlockState(BlockPos.containing(spawnX, spawnY, spawnZ)).isSolid()) {
                     spawnX = player.getX();
                     spawnY = player.getEyeY();
                     spawnZ = player.getZ();
                  }
               }

               dokkaebi.moveTo(spawnX, spawnY, spawnZ, player.getYRot() + 180.0F, 0.0F);
               dokkaebi.bindPlayer(player);
               level.addFreshEntity(dokkaebi);
               level.playSound(null, dokkaebi.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.3F);
               level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnX, spawnY + 0.5, spawnZ, 35, 0.4, 0.5, 0.4, 0.15);
               AwakeningSystemMod.notify(
                  player, "awakening_system.event.wild_dokkaebi_title", "awakening_system.event.wild_dokkaebi_msg", -10496, new Object[0]
               );
            }
         }
      }
   }

   @SubscribeEvent
   public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob && event.getLevel() instanceof ServerLevel slSpawn) {
         if (WorldBossScalingManager.isWorldBoss(mob)) {
            WorldBossScalingManager.scaleWorldBossToNearbyPlayers(mob, slSpawn);
         } else {
            RpgLevelZoneManager.scaleMobOnSpawn(mob, slSpawn);
         }
      }

      if (event.getLevel().dimension().location().equals(IsekaiWorldManager.ISEKAI_WORLD.location())) {
         if (event.getEntity().getType() == EntityType.ENDERMAN) {
            event.setCanceled(true);
            return;
         }

         if (!event.loadedFromDisk() && isVanillaHostileMonster(event.getEntity()) && event.getLevel() instanceof ServerLevel serverLevel) {
            Monster monster = (Monster)event.getEntity();
            RandomSource random = monster.getRandom();
            if (random.nextFloat() < 0.65F) {
               float roll = random.nextFloat();
               EntityType<?> targetType;
               if (roll < 0.35F) {
                  targetType = (EntityType<?>)ModEntities.GOBLIN_SCOUT.get();
               } else if (roll < 0.65F) {
                  targetType = (EntityType<?>)ModEntities.GOBLIN_WARRIOR.get();
               } else if (roll < 0.8F) {
                  targetType = (EntityType<?>)ModEntities.GOBLIN_SHAMAN.get();
               } else {
                  targetType = (EntityType<?>)ModEntities.BEASTMAN_WARRIOR.get();
               }

               Entity customMob = targetType.create(serverLevel);
               if (customMob != null) {
                  customMob.moveTo(monster.getX(), monster.getY(), monster.getZ(), monster.getYRot(), monster.getXRot());
                  serverLevel.addFreshEntity(customMob);
                  event.setCanceled(true);
                  return;
               }
            }
         }

         if (!event.loadedFromDisk() && event.getEntity() instanceof Animal animal) {
            RandomSource random = animal.getRandom();
            if (random.nextFloat() < 0.28F && event.getLevel() instanceof ServerLevel serverLevelx) {
               EntityType<?> moobloomType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE
                  .getOptional(ResourceLocation.fromNamespaceAndPath("friendsandfoes", "moobloom"))
                  .orElse(null);
               EntityType<?> glareType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE
                  .getOptional(ResourceLocation.fromNamespaceAndPath("friendsandfoes", "glare"))
                  .orElse(null);
               EntityType<?> crabType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE
                  .getOptional(ResourceLocation.fromNamespaceAndPath("friendsandfoes", "crab"))
                  .orElse(null);
               EntityType<?> targetTypex = null;
               int rollx = random.nextInt(3);
               if (rollx == 0 && moobloomType != null) {
                  targetTypex = moobloomType;
               } else if (rollx == 1 && glareType != null) {
                  targetTypex = glareType;
               } else if (rollx == 2 && crabType != null) {
                  targetTypex = crabType;
               }

               if (targetTypex != null) {
                  Entity customMob = targetTypex.create(serverLevelx);
                  if (customMob != null) {
                     customMob.moveTo(animal.getX(), animal.getY(), animal.getZ(), animal.getYRot(), animal.getXRot());
                     serverLevelx.addFreshEntity(customMob);
                     event.setCanceled(true);
                  }
               }
            }
         }
      }

      if (event.getLevel().dimension().location().equals(MurimWorldManager.MURIM_WORLD.location())
         && !event.loadedFromDisk()
         && isVanillaHostileMonster(event.getEntity())
         && event.getLevel() instanceof ServerLevel serverLevelx) {
         Monster monster = (Monster)event.getEntity();
         RandomSource random = monster.getRandom();
         if (random.nextFloat() < 0.7F) {
            float rollxx = random.nextFloat();
            EntityType<?> targetTypexx;
            if (rollxx < 0.35F) {
               targetTypexx = (EntityType<?>)ModEntities.JIANGSHI.get();
            } else if (rollxx < 0.65F) {
               targetTypexx = (EntityType<?>)ModEntities.DEMONIC_CULTIVATOR.get();
            } else if (rollxx < 0.85F) {
               targetTypexx = (EntityType<?>)ModEntities.MOUNT_HUA_DISCIPLE.get();
            } else {
               targetTypexx = (EntityType<?>)ModEntities.JADE_ARMORED_BEAST.get();
            }

            Entity customMob = targetTypexx.create(serverLevelx);
            if (customMob != null) {
               customMob.moveTo(monster.getX(), monster.getY(), monster.getZ(), monster.getYRot(), monster.getXRot());
               serverLevelx.addFreshEntity(customMob);
               event.setCanceled(true);
            }
         }
      }
   }

   private static boolean isVanillaHostileMonster(Entity entity) {
      if (!(entity instanceof Monster)) {
         return false;
      } else {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         return key != null && "minecraft".equals(key.getNamespace())
            ? entity.getType() == EntityType.ZOMBIE
               || entity.getType() == EntityType.ZOMBIE_VILLAGER
               || entity.getType() == EntityType.SKELETON
               || entity.getType() == EntityType.SPIDER
               || entity.getType() == EntityType.CREEPER
               || entity.getType() == EntityType.CAVE_SPIDER
               || entity.getType() == EntityType.DROWNED
               || entity.getType() == EntityType.HUSK
               || entity.getType() == EntityType.STRAY
            : false;
      }
   }

   public static void handleDimensionTravel(ServerPlayer player, String dimension) {
      if (player != null && player.getServer() != null) {
         SystemData data = data(player);
         MinecraftServer server = player.getServer();
         boolean isDevOrOp = player.hasPermissions(2) || player.isCreative();
         if (!"overworld".equalsIgnoreCase(dimension)
            && !"earth".equalsIgnoreCase(dimension)
            && !"leave".equalsIgnoreCase(dimension)
            && !"exit".equalsIgnoreCase(dimension)) {
            if ("isekai".equalsIgnoreCase(dimension)) {
               if (!isDevOrOp && !data.systemUpdated() && !data.isekaiUnlocked() && !data.system30Updated() && !data.system40Updated()) {
                  player.displayClientMessage(Component.translatable("awakening_system.action.isekai_requires_sys20"), true);
                  player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                  return;
               }

               IsekaiWorldManager.teleportToIsekai(player);
            } else if ("murim".equalsIgnoreCase(dimension)) {
               if (!isDevOrOp && !data.system30Updated() && !data.murimUnlocked() && !data.system40Updated()) {
                  player.displayClientMessage(Component.translatable("awakening_system.action.murim_requires_sys30"), true);
                  player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                  return;
               }

               MurimWorldManager.teleportToMurim(player);
            } else if ("teyvat".equalsIgnoreCase(dimension)) {
               TeyvatWorldManager.teleportToTeyvat(player);
            } else if ("limbo".equalsIgnoreCase(dimension)) {
               LimboWorldManager.teleportToLimbo(player);
            }
         } else {
            if (GreatTowerWorldManager.isGreatTowerDimension(player.level())) {
               GreatTowerWorldManager.exitTower(player);
               return;
            }

            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) {
               return;
            }

            BlockPos targetPos = player.getRespawnPosition();
            if (targetPos == null) {
               targetPos = overworld.getSharedSpawnPos();
            }

            player.teleportTo(overworld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, player.getYRot(), 0.0F);
            overworld.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5, targetPos.getY() + 1.2, targetPos.getZ() + 0.5, 60, 1.0, 1.0, 1.0, 0.1);
            player.playNotifySound((SoundEvent)ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.tower.returned_to_overworld"), true);
         }
      }
   }

   @SubscribeEvent
   public static void onSleepFinished(SleepFinishedTimeEvent event) {
      if (event.getLevel() instanceof ServerLevel level) {
         long newTime = event.getNewTime();
         MinecraftServer server = level.getServer();
         if (server != null) {
            ServerLevel overworld = server.overworld();
            ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
            if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
               if (overworld != null) {
                  overworld.setDayTime(newTime);
                  if (overworld.isRaining()) {
                     overworld.setWeatherParameters(0, 0, false, false);
                  }
               }

               for (ServerPlayer sp : level.players()) {
                  sp.displayClientMessage(Component.translatable("awakening_system.teyvat.sleep_rest"), true);
               }
            } else if (level.dimension().equals(Level.OVERWORLD) && teyvat != null) {
               teyvat.setDayTime(newTime);
               if (teyvat.isRaining()) {
                  teyvat.setWeatherParameters(0, 0, false, false);
               }
            }
         }
      }
   }

   public static void activateArmorSkill(ServerPlayer player) {
      if (player != null && player.isAlive()) {
         SetType activeSet = ManhwaArmorItem.getFullSet(player);
         if (activeSet == null) {
            player.displayClientMessage(Component.translatable("awakening_system.action.no_armor_skill"), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
         } else {
            long gameTime = player.level().getGameTime();
            ServerLevel sl = player.serverLevel();
            switch (activeSet) {
               case SOUL_ALLOY:
                  long cd = player.getPersistentData().getLong("soul_voracity_cd");
                  if (gameTime < cd) {
                     long left = Math.max(1L, (cd - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.soul_voracity.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("soul_voracity_cd", gameTime + 300L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.6F, 0.8F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.4F, 1.2F);
                  spawnArcaneCircleGround(sl, player, 6.0F, 60, -16715521);
                  sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 80, 2.0, 1.0, 2.0, 0.1);
                  sl.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 50, 1.5, 1.5, 1.5, 0.15);
                  List<LivingEntity> enemies = sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(9.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  );
                  float totalDamageDealt = 0.0F;

                  for (LivingEntity enemy : enemies) {
                     float dmg = 28.0F;
                     enemy.hurt(player.damageSources().playerAttack(player), dmg);
                     enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 1));
                     enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                     totalDamageDealt += dmg;
                  }

                  if (totalDamageDealt > 0.0F) {
                     float heal = totalDamageDealt * 0.15F;
                     player.heal(heal);
                     sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.8, player.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.soul_voracity.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_soul_voracity.title",
                     "awakening_system.notification.armor_skill_soul_voracity.desc",
                     -16715521,
                     new Object[0]
                  );
                  break;
               case ASTRALITE:
                  long cdx = player.getPersistentData().getLong("astralite_supernova_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.astralite_supernova.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("astralite_supernova_cd", gameTime + 360L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.8F, 1.5F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.4F, 1.3F);
                  spawnArcaneCircleGround(sl, player, 7.5F, 80, -5011201);
                  sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 90, 2.5, 1.5, 2.5, 0.2);
                  sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 100, 2.0, 1.5, 2.0, 0.4);
                  sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 4, 0.0, 0.0, 0.0, 0.0);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(11.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     Vec3 pull = player.position().subtract(enemy.position()).normalize().scale(1.2);
                     enemy.setDeltaMovement(enemy.getDeltaMovement().add(pull.x, 0.4, pull.z));
                     enemy.hurt(player.damageSources().playerAttack(player), 34.0F);
                     enemy.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));
                     enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.astralite_supernova.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_astralite_supernova.title",
                     "awakening_system.notification.armor_skill_astralite_supernova.desc",
                     -5011201,
                     new Object[0]
                  );
                  break;
               case ABYSSAL:
                  cdx = player.getPersistentData().getLong("abyssal_vortex_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.abyssal_vortex.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("abyssal_vortex_cd", gameTime + 200L);
                  Vec3 pPos = player.position();

                  for (LivingEntity target : sl.getEntitiesOfClass(
                     LivingEntity.class, player.getBoundingBox().inflate(14.0), e -> e != player && e.isAlive() && !e.isAlliedTo(player)
                  )) {
                     Vec3 pull = pPos.subtract(target.position()).normalize().scale(1.2);
                     target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.4, pull.z));
                     target.hurt(sl.damageSources().magic(), 24.0F);
                     target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                     target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                  }

                  sl.sendParticles(ParticleTypes.PORTAL, pPos.x, pPos.y + 1.0, pPos.z, 120, 2.5, 1.0, 2.5, 0.5);
                  sl.sendParticles(ParticleTypes.REVERSE_PORTAL, pPos.x, pPos.y + 1.0, pPos.z, 60, 1.5, 1.0, 1.5, 0.2);
                  sl.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2F, 0.8F);
                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.abyssal_vortex.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_abyssal_vortex.title",
                     "awakening_system.notification.armor_skill_abyssal_vortex.desc",
                     -16755201,
                     new Object[0]
                  );
                  break;
               case MITHRIL:
                  cdx = player.getPersistentData().getLong("mithril_blink_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.mithril_blink.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("mithril_blink_cd", gameTime + 140L);
                  Vec3 start = player.position().add(0.0, player.getEyeHeight() * 0.5, 0.0);
                  Vec3 look = player.getLookAngle().normalize();
                  double dist = 12.0;
                  Vec3 target = start.add(look.scale(dist));
                  ClipContext clipCtx = new ClipContext(start, target, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, player);
                  BlockHitResult hit = player.level().clip(clipCtx);
                  Vec3 finalPos = hit.getType() == Type.BLOCK ? hit.getLocation().subtract(look.scale(0.8)) : target;
                  double destX = finalPos.x;
                  double destY = Math.max(player.level().getMinBuildHeight() + 1.0, finalPos.y);
                  double destZ = finalPos.z;
                  int steps = 16;

                  for (int i = 0; i <= steps; i++) {
                     double progress = (double)i / steps;
                     double px = start.x + (destX - start.x) * progress;
                     double py = start.y + (destY - start.y) * progress;
                     double pz = start.z + (destZ - start.z) * progress;
                     sl.sendParticles(ParticleTypes.CLOUD, px, py, pz, 2, 0.1, 0.1, 0.1, 0.01);
                     sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                     if (i % 4 == 0) {
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                     }
                  }

                  AABB lineBox = new AABB(start, new Vec3(destX, destY, destZ)).inflate(1.5);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class, lineBox, e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     enemy.hurt(player.damageSources().playerAttack(player), 18.0F);
                     enemy.knockback(0.8, -look.x, -look.z);
                     sl.sendParticles(ParticleTypes.CRIT, enemy.getX(), enemy.getY() + 0.8, enemy.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
                  }

                  player.teleportTo(destX, destY, destZ);
                  player.resetFallDistance();
                  player.setDeltaMovement(look.scale(0.6));
                  player.hasImpulse = true;
                  sl.playSound(null, destX, destY, destZ, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.4F);
                  sl.playSound(null, destX, destY, destZ, (SoundEvent)SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.2F, 1.5F);
                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.mithril_blink.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_blink.title",
                     "awakening_system.notification.armor_skill_blink.desc",
                     -11141121,
                     new Object[0]
                  );
                  break;
               case ADAMANTITE:
                  cdx = player.getPersistentData().getLong("adamantite_fissure_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.adamantite_fissure.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("adamantite_fissure_cd", gameTime + 280L);
                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.75F
                  );
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.5F, 0.6F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.8F);
                  spawnArcaneCircleGround(sl, player, 6.5F, 60, -47872);
                  double[] radii = new double[]{2.5, 5.0, 7.5};

                  for (double r : radii) {
                     int pts = (int)(r * 10.0);

                     for (int i = 0; i < pts; i++) {
                        double angle = (Math.PI * 2) / pts * i;
                        double px = player.getX() + Math.cos(angle) * r;
                        double pz = player.getZ() + Math.sin(angle) * r;
                        sl.sendParticles(ParticleTypes.LAVA, px, player.getY() + 0.1, pz, 2, 0.1, 0.5, 0.1, 0.05);
                        sl.sendParticles(ParticleTypes.FLAME, px, player.getY() + 0.2, pz, 3, 0.1, 0.4, 0.1, 0.08);
                        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, player.getY() + 0.1, pz, 1, 0.05, 0.2, 0.05, 0.02);
                     }
                  }

                  sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5, player.getZ(), 4, 1.5, 0.5, 1.5, 0.1);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(7.5),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     enemy.hurt(player.damageSources().playerAttack(player), 24.0F);
                     enemy.setRemainingFireTicks(160);
                     enemy.setDeltaMovement(enemy.getDeltaMovement().x * 0.3, 0.95, enemy.getDeltaMovement().z * 0.3);
                     enemy.hurtMarked = true;
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.adamantite_fissure.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_magma.title",
                     "awakening_system.notification.armor_skill_magma.desc",
                     -47872,
                     new Object[0]
                  );
                  break;
               case SPIRIT_JADE:
                  cdx = player.getPersistentData().getLong("spirit_jade_bell_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.spirit_jade_bell.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("spirit_jade_bell_cd", gameTime + 440L);
                  player.getPersistentData().putLong("spirit_jade_bell_active_until", gameTime + 100L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 2.0F, 1.0F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.3F);
                  spawnMurimCircleGround(sl, player, 4.5F, 80, -11470213);
                  sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.2, 1.2, 1.2, 0.2);
                  sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 40, 1.0, 1.0, 1.0, 0.1);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(5.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     enemy.knockback(1.8, player.getX() - enemy.getX(), player.getZ() - enemy.getZ());
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.spirit_jade_bell.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_jade_bell.title",
                     "awakening_system.notification.armor_skill_jade_bell.desc",
                     -11141291,
                     new Object[0]
                  );
                  break;
               case ASTRAL:
                  cdx = player.getPersistentData().getLong("astral_burst_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.astral_burst.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("astral_burst_cd", gameTime + 360L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.6F, 1.1F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.8F);
                  sl.playSound(
                     null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.3F
                  );
                  spawnArcaneCircleGround(sl, player, 6.5F, 70, -2080517);
                  sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 80, 2.0, 1.5, 2.0, 0.15);
                  sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 60, 2.5, 2.0, 2.5, 0.2);
                  sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                  sl.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.0, player.getZ(), 40, 1.5, 1.5, 1.5, 0.25);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(12.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     enemy.hurt(player.damageSources().playerAttack(player), 32.0F);
                     enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                     enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                     enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                     double dx = enemy.getX() - player.getX();
                     double dz = enemy.getZ() - player.getZ();
                     enemy.knockback(1.5, -dx, -dz);
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.astral_burst.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_astral_burst.title",
                     "awakening_system.notification.armor_skill_astral_burst.desc",
                     -43521,
                     new Object[0]
                  );
                  break;
               case CELESTIAL:
                  cdx = player.getPersistentData().getLong("celestial_flare_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(Component.translatable("awakening_system.armor_skill.celestial_flare.cooldown", new Object[]{left}), true);
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("celestial_flare_cd", gameTime + 320L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 1.2F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 1.6F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.6F, 1.4F);
                  spawnArcaneCircleGround(sl, player, 7.5F, 80, -16718337);
                  sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 90, 2.5, 2.0, 2.5, 0.25);
                  sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.0, player.getZ(), 80, 2.0, 1.5, 2.0, 0.2);
                  sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.5, player.getZ(), 6, 0.0, 0.0, 0.0, 0.0);
                  sl.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.2, player.getZ(), 50, 1.8, 1.5, 1.8, 0.2);
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1));
                  player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(14.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     enemy.hurt(player.damageSources().playerAttack(player), 40.0F);
                     enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
                     enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3));
                     enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 2));
                     double dx = enemy.getX() - player.getX();
                     double dz = enemy.getZ() - player.getZ();
                     enemy.knockback(1.8, -dx, -dz);
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.celestial_flare.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_celestial_flare.title",
                     "awakening_system.notification.armor_skill_celestial_flare.desc",
                     -16718337,
                     new Object[0]
                  );
                  break;
               case PRIMORDIAL:
                  cdx = player.getPersistentData().getLong("primordial_singularity_cd");
                  if (gameTime < cdx) {
                     long left = Math.max(1L, (cdx - gameTime) / 20L);
                     player.displayClientMessage(
                        Component.translatable("awakening_system.armor_skill.primordial_singularity.cooldown", new Object[]{left}), true
                     );
                     player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                     return;
                  }

                  player.getPersistentData().putLong("primordial_singularity_cd", gameTime + 400L);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0F, 0.7F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.4F, 1.2F);
                  sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.5F, 1.5F);
                  spawnArcaneCircleGround(sl, player, 8.5F, 90, -65409);
                  sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1.2, player.getZ(), 4, 0.5, 0.5, 0.5, 0.05);
                  sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 100, 3.0, 2.0, 3.0, 0.25);
                  sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 80, 2.5, 2.0, 2.5, 0.3);
                  sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);

                  for (LivingEntity enemy : sl.getEntitiesOfClass(
                     LivingEntity.class,
                     player.getBoundingBox().inflate(16.0),
                     e -> e != player && !e.isAlliedTo(player) && !e.getTags().contains("shadow_soldier")
                  )) {
                     Vec3 pull = player.position().subtract(enemy.position()).normalize().scale(1.5);
                     enemy.setDeltaMovement(enemy.getDeltaMovement().add(pull.x, 0.6, pull.z));
                     enemy.hurt(player.damageSources().playerAttack(player), 52.0F);
                     enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                     enemy.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
                     enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                     enemy.hurtMarked = true;
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.armor_skill.primordial_singularity.activated"), true);
                  AwakeningSystemMod.notify(
                     player,
                     "awakening_system.notification.armor_skill_primordial_singularity.title",
                     "awakening_system.notification.armor_skill_primordial_singularity.desc",
                     -65409,
                     new Object[0]
                  );
            }
         }
      }
   }

   @SubscribeEvent
   public static void onItemUseFinish(Finish event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ItemStack stack = event.getItem();
         if (stack.is(Items.POTION)
            || stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION)
            || stack.getItem() instanceof VigorPotionItem
            || stack.getItem() instanceof ManaPotionItem) {
            grantFable(player, "apprentice_alchemist");
         }

         if ((stack.is(ItemTags.PIGLIN_FOOD) || stack.has(DataComponents.FOOD))
            && player.level().getBlockStates(player.getBoundingBox().inflate(4.0)).anyMatch(bs -> bs.is(Blocks.CAMPFIRE) || bs.is(Blocks.SOUL_CAMPFIRE))) {
            grantFable(player, "culinary_survivor");
         }
      }
   }

   @SubscribeEvent
   public static void onAnimalTame(AnimalTameEvent event) {
      if (event.getTamer() instanceof ServerPlayer sp) {
         grantFable(sp, "first_pet_tamed");
      }
   }

   public record BoxLootResult(String summary, int coins, List<String> itemIds, List<Integer> itemCounts) {
   }

   public static class DokkaebiOverkillReward {
      public final int bonusCoins;
      public final int bonusExp;
      public final int milestoneDonation;
      public final Component rankName;
      public final ItemStack crystalGift;

      public DokkaebiOverkillReward(int bonusCoins, int bonusExp, int milestoneDonation, Component rankName, ItemStack crystalGift) {
         this.bonusCoins = bonusCoins;
         this.bonusExp = bonusExp;
         this.milestoneDonation = milestoneDonation;
         this.rankName = rankName;
         this.crystalGift = crystalGift;
      }
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
}
