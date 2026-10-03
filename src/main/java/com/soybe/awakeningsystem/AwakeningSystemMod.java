package com.soybe.awakeningsystem;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;

@Mod(AwakeningSystemMod.MOD_ID)
public final class AwakeningSystemMod {
    public static final String MOD_ID = "awakening_system";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM.key(), MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATURES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FEATURE, MOD_ID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.GiantFantasyTreeFeature> GIANT_FANTASY_TREE =
            FEATURES.register("giant_fantasy_tree", () -> new com.soybe.awakeningsystem.worldgen.GiantFantasyTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.MysticCrystalTreeFeature> MYSTIC_CRYSTAL_TREE =
            FEATURES.register("mystic_crystal_tree", () -> new com.soybe.awakeningsystem.worldgen.MysticCrystalTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.GoldenAutumnTreeFeature> GOLDEN_AUTUMN_TREE =
            FEATURES.register("golden_autumn_tree", () -> new com.soybe.awakeningsystem.worldgen.GoldenAutumnTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.DragonNestFeature> DRAGON_NEST =
            FEATURES.register("dragon_nest", () -> new com.soybe.awakeningsystem.worldgen.DragonNestFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.SuperGiantMurimPineFeature> SUPER_GIANT_MURIM_PINE =
            FEATURES.register("super_giant_murim_pine", () -> new com.soybe.awakeningsystem.worldgen.SuperGiantMurimPineFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.SuperGiantPlumFeature> SUPER_GIANT_PLUM =
            FEATURES.register("super_giant_plum", () -> new com.soybe.awakeningsystem.worldgen.SuperGiantPlumFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.SuperGiantWorldTreeFeature> SUPER_GIANT_WORLD_TREE =
            FEATURES.register("super_giant_world_tree", () -> new com.soybe.awakeningsystem.worldgen.SuperGiantWorldTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatCuihuaTreeFeature> TEYVAT_CUIHUA_TREE =
            FEATURES.register("teyvat_cuihua_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatCuihuaTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatSandbearerTreeFeature> TEYVAT_SANDBEARER_TREE =
            FEATURES.register("teyvat_sandbearer_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatSandbearerTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatAdhigamaTreeFeature> TEYVAT_ADHIGAMA_TREE =
            FEATURES.register("teyvat_adhigama_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatAdhigamaTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.AraliaTreeFeature> TEYVAT_ARALIA_TREE =
            FEATURES.register("teyvat_aralia_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.AraliaTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.OtogiTreeFeature> TEYVAT_OTOGI_TREE =
            FEATURES.register("teyvat_otogi_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.OtogiTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.YumemiruTreeFeature> TEYVAT_YUMEMIRU_TREE =
            FEATURES.register("teyvat_yumemiru_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.YumemiruTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.MapleTreeFeature> TEYVAT_MAPLE_TREE =
            FEATURES.register("teyvat_maple_tree", () -> new com.soybe.awakeningsystem.worldgen.TeyvatInazumaTreeFeatures.MapleTreeFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatHilichurlCampFeature> TEYVAT_HILICHURL_CAMP =
            FEATURES.register("teyvat_hilichurl_camp", () -> new com.soybe.awakeningsystem.worldgen.TeyvatHilichurlCampFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatShrineOfDepthsFeature> TEYVAT_SHRINE_OF_DEPTHS =
            FEATURES.register("teyvat_shrine_of_depths", () -> new com.soybe.awakeningsystem.worldgen.TeyvatShrineOfDepthsFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatStatueFeature> TEYVAT_STATUE =
            FEATURES.register("teyvat_statue_of_the_seven", () -> new com.soybe.awakeningsystem.worldgen.TeyvatStatueFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatWaypointFeature> TEYVAT_WAYPOINT =
            FEATURES.register("teyvat_teleport_waypoint", () -> new com.soybe.awakeningsystem.worldgen.TeyvatWaypointFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, com.soybe.awakeningsystem.worldgen.TeyvatRegionalStructureFeature> TEYVAT_REGIONAL_STRUCTURE =
            FEATURES.register("teyvat_regional_structure", () -> new com.soybe.awakeningsystem.worldgen.TeyvatRegionalStructureFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    public static final Supplier<AttachmentType<SystemData>> PLAYER_SYSTEM = ATTACHMENTS.register(
            "player_system", () -> AttachmentType.serializable(SystemData::new).copyOnDeath().build());

    public AwakeningSystemMod(IEventBus modBus) {
        com.soybe.awakeningsystem.util.AttributeLimitFixer.fixLimits();
        SystemItems.register();
        com.soybe.awakeningsystem.block.SystemBlocks.BLOCKS.register(modBus);
        com.soybe.awakeningsystem.block.entity.ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        com.soybe.awakeningsystem.furnace.ModMenus.MENUS.register(modBus);
        ATTACHMENTS.register(modBus);
        ITEMS.register(modBus);
        FEATURES.register(modBus);
        com.soybe.awakeningsystem.item.ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        com.soybe.awakeningsystem.particle.ModParticles.PARTICLE_TYPES.register(modBus);
        com.soybe.awakeningsystem.effect.ModMobEffects.MOB_EFFECTS.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(SystemItems::addCreativeTabs);
        modBus.addListener(this::commonSetup);
        com.soybe.awakeningsystem.entity.ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(com.soybe.awakeningsystem.entity.ModEntities::registerAttributes);
        modBus.addListener(com.soybe.awakeningsystem.entity.ModEntities::registerSpawnPlacements);
        net.neoforged.fml.ModContainer modContainer = net.neoforged.fml.ModList.get().getModContainerById(MOD_ID).orElse(null);
        if (modContainer != null) {
            modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, SystemConfig.SPEC);
        }
        modBus.addListener(AwakeningSystemMod::registerPayloads);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modBus.addListener(AwakeningSystemClient::onClientSetup);
            modBus.addListener(AwakeningSystemClient::registerScreens);
            modBus.addListener(AwakeningSystemClient::registerKeyMappings);
            modBus.addListener(AwakeningSystemClient::registerEntityRenderers);
            modBus.addListener(AwakeningSystemClient::registerLayerDefinitions);
            modBus.addListener(AwakeningSystemClient::registerParticleProviders);
            modBus.addListener(AwakeningSystemClient::registerClientExtensions);
            modBus.addListener(AwakeningSystemClient::registerDimensionSpecialEffects);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onClientTick);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onRenderLevelStage);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onRenderLivingPre);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onRenderPlayerPre);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onMovementInputUpdate);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onComputeCameraAngles);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onRenderNameTag);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::registerClientCommands);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onMouseButtonPre);
            NeoForge.EVENT_BUS.addListener(AwakeningSystemClient::onItemTooltip);
        }
        NeoForge.EVENT_BUS.register(SystemEvents.class);
        NeoForge.EVENT_BUS.addListener(SystemCommands::register);
    }

    private void commonSetup(final net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (net.neoforged.fml.ModList.get().isLoaded("terrablender")) {
                com.soybe.awakeningsystem.worldgen.TerraBlenderCompat.registerRegions();
            }
            com.soybe.awakeningsystem.config.CustomLootConfigManager.init();
            com.soybe.awakeningsystem.config.BossCompatibilityManager.init();
        });
    }

    private static void registerPayloads(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(SpendAttributePayload.TYPE, SpendAttributePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.spendAttribute(player, payload.attribute());
                    }
                }));
        registrar.playToServer(ActivateSkillPayload.TYPE, ActivateSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.activateSkill(player, payload.skillId());
                    }
                }));
        registrar.playToServer(ActivateSkillSlotPayload.TYPE, ActivateSkillSlotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.activateSkillSlot(player, payload.slot());
                    }
                }));
        registrar.playToServer(ActivateWeaponSkillPayload.TYPE, ActivateWeaponSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.activateWeaponSkill(player, payload.handOrdinal());
                    }
                }));
        registrar.playToServer(EquipSkillSlotPayload.TYPE, EquipSkillSlotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.equipSkillSlot(player, payload.isPassive(), payload.slot(), payload.skillId());
                    }
                }));
        registrar.playToServer(ActivateStigmaPayload.TYPE, ActivateStigmaPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.activateStigma(player);
                    }
                }));
        registrar.playToServer(CultivationActionPayload.TYPE, CultivationActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.handleCultivationAction(player, payload.action(), payload.targetIndex());
                    }
                }));
        registrar.playToServer(RequestLeaderboardPayload.TYPE, RequestLeaderboardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        sendLeaderboard(player);
                    }
                }));
        registrar.playToClient(LeaderboardPayload.TYPE, LeaderboardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.setLeaderboard(payload.entries())));
        registrar.playToServer(PrestigePayload.TYPE, PrestigePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.prestigePlayer(player);
                    }
                }));
        registrar.playToServer(EvolveSkillPayload.TYPE, EvolveSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.evolveSkill(player, payload.skillId());
                    }
                }));
        registrar.playToServer(BuyShopItemPayload.TYPE, BuyShopItemPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && SystemEvents.buyShopItem(player, payload.entryId())) {
                        AwakeningSystemMod.sync(player);
                    }
                }));
        registrar.playToServer(SellShopItemPayload.TYPE, SellShopItemPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && SystemEvents.sellShopItem(player, payload.entryId(), payload.sellAll())) {
                        AwakeningSystemMod.sync(player);
                    }
                }));
        registrar.playToServer(BankTransactionPayload.TYPE, BankTransactionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.handleBankTransaction(player, payload.action(), payload.amount());
                    }
                }));
        registrar.playToServer(EquipTitlePayload.TYPE, EquipTitlePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && SystemEvents.equipTitle(player, payload.titleId())) {
                        AwakeningSystemMod.sync(player);
                    }
                }));
        registrar.playToServer(TravelDimensionPayload.TYPE, TravelDimensionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.handleDimensionTravel(player, payload.dimension());
                    }
                }));
        registrar.playToServer(EquipSkillPayload.TYPE, EquipSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.equipSkill(player, payload.skillId());
                    }
                }));
        registrar.playToServer(DragonFlightControlPayload.TYPE, DragonFlightControlPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        if (player.getVehicle() instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity dragon) {
                            dragon.setFlightInput(payload.space(), payload.shift(), payload.sprint());
                        } else if (player.getVehicle() instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity dragon) {
                            dragon.setFlightInput(payload.space(), payload.shift(), payload.sprint());
                        } else if (player.getVehicle() instanceof com.soybe.awakeningsystem.entity.WyvernEntity wyvern) {
                            wyvern.setFlightInput(payload.space(), payload.shift(), payload.sprint());
                        }
                    }
                }));
        registrar.playToServer(SelectHunterClassPayload.TYPE, SelectHunterClassPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.selectHunterClass(player, payload.hunterClass());
                    }
                }));
        registrar.playToServer(SelectVisionPayload.TYPE, SelectVisionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.handleSelectVision(player, payload.vision());
                    }
                }));
        registrar.playToServer(UnlockSkillTreeNodePayload.TYPE, UnlockSkillTreeNodePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.unlockSkillTreeNode(player, payload.skillId());
                    }
                }));
        registrar.playToServer(UpgradeSkillTreeNodePayload.TYPE, UpgradeSkillTreeNodePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.upgradeSkillTreeNode(player, payload.skillId());
                    }
                }));
        registrar.playToServer(ToggleSkillPayload.TYPE, ToggleSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemData data = SystemEvents.data(player);
                        if (data.toggleSkill(payload.skillId())) {
                            SystemAttributeEffects.apply(player);
                            AwakeningSystemMod.sync(player);
                        }
                    }
                }));
        registrar.playToServer(SelectConstellationPayload.TYPE, SelectConstellationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && SystemEvents.selectConstellation(player, payload.constellationId())) {
                        AwakeningSystemMod.sync(player);
                    }
                }));
        registrar.playToServer(ClaimScenarioPayload.TYPE, ClaimScenarioPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && SystemEvents.claimScenario(player)) {
                        AwakeningSystemMod.sync(player);
                    }
                }));
        registrar.playToServer(SelectDungeonPayload.TYPE, SelectDungeonPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.handleSelectDungeon(player, payload.rank(), payload.biome());
                    }
                }));
        registrar.playToServer(ActivateArmorSkillPayload.TYPE, ActivateArmorSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemEvents.activateArmorSkill(player);
                    }
                }));
        registrar.playToClient(SystemSyncPayload.TYPE, SystemSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> context.player()
                        .getData(PLAYER_SYSTEM)
                        .deserializeNBT(context.player().registryAccess(), payload.data())));
        registrar.playToClient(SystemNotificationPayload.TYPE, SystemNotificationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.showNotification(
                        payload.titleKey(), payload.messageKey(), payload.argument(), payload.color())));
        registrar.playToClient(ChestRewardPayload.TYPE, ChestRewardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.onChestReward(payload)));
        registrar.playToClient(OpenDungeonChestPayload.TYPE, OpenDungeonChestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openDungeonChestScreen(payload)));
        registrar.playToClient(SyncRiftsPayload.TYPE, SyncRiftsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.syncRifts(payload.rifts())));
        registrar.playToClient(OpenSponsorScreenPayload.TYPE, OpenSponsorScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openSponsorScreen(payload.forced())));
        registrar.playToClient(OpenDungeonKeyPayload.TYPE, OpenDungeonKeyPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openDungeonKeyScreen(payload.playerLevel(), payload.currentRank())));
        registrar.playToClient(OpenClassSelectionPayload.TYPE, OpenClassSelectionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openClassSelectionScreen()));
        registrar.playToClient(OpenGuidebookPayload.TYPE, OpenGuidebookPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openGuidebookScreen()));
        registrar.playToClient(OpenBestiaryPayload.TYPE, OpenBestiaryPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openBestiaryScreen()));
        registrar.playToClient(SpawnMagicCirclePayload.TYPE, SpawnMagicCirclePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.spawnMagicCircle(payload)));
        registrar.playToClient(CancelMagicCirclePayload.TYPE, CancelMagicCirclePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.cancelMagicCircle(payload)));
        registrar.playToServer(DonateConstellationPayload.TYPE, DonateConstellationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        ConstellationChannelHandler.handleDonate(player, payload);
                    }
                }));
        registrar.playToServer(RequestStarStreamPayload.TYPE, RequestStarStreamPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        ConstellationChannelHandler.handleRequestStarStream(player);
                    }
                }));
        registrar.playToClient(SyncStarStreamPayload.TYPE, SyncStarStreamPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.syncStarStream(payload.incarnations())));
        registrar.playToClient(BossCinematicPayload.TYPE, BossCinematicPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.triggerBossCinematic(
                        payload.altarPos(), payload.durationTicks(), payload.dragonEntityId())));
        registrar.playToServer(SpectateIncarnationPayload.TYPE, SpectateIncarnationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        ConstellationChannelHandler.handleSpectate(player, payload.targetPlayerId());
                    }
                }));
        registrar.playToServer(SpinPhysicalRoulettePayload.TYPE, SpinPhysicalRoulettePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SystemData data = SystemEvents.data(player);
                        int cost = 1000;
                        if (data.coins() >= cost) {
                            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                            net.minecraft.world.phys.Vec3 target = player.position().add(look.x * 2.5D, 0.0D, look.z * 2.5D);
                            net.minecraft.core.BlockPos groundPos = net.minecraft.core.BlockPos.containing(target);
                            if (player.level().getBlockState(groundPos).isAir() && !player.level().getBlockState(groundPos.below()).isAir()) {
                                target = new net.minecraft.world.phys.Vec3(groundPos.getX() + 0.5D, groundPos.getY(), groundPos.getZ() + 0.5D);
                            }
                            if (PhysicalRouletteManager.startRoulette(player, target, false)) {
                                data.spendCoins(cost);
                                sync(player);
                            }
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.roulette.insufficient_coins"), true);
                        }
                    }
                }));
        registrar.playToClient(OpenBountyBoardPayload.TYPE, OpenBountyBoardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openBountyBoardScreen(payload)));
        registrar.playToServer(SelectBountyPayload.TYPE, SelectBountyPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.block.BountyBoardBlock.handleSelectBounty(player, payload);
                    }
                }));
        registrar.playToClient(OpenFishingStationPayload.TYPE, OpenFishingStationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openFishingStationScreen(payload)));
        registrar.playToServer(FishingStationActionPayload.TYPE, FishingStationActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.block.FishingStationBlock.handleAction(player, payload);
                    }
                }));
        registrar.playToClient(OpenGuildReceptionPayload.TYPE, OpenGuildReceptionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openGuildReceptionScreen(payload)));
        registrar.playToServer(GuildReceptionActionPayload.TYPE, GuildReceptionActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        SeriesNpcEntity.handleReceptionAction(player, payload);
                    }
                }));
        registrar.playToClient(com.soybe.awakeningsystem.guild.OpenGuildScreenPayload.TYPE, com.soybe.awakeningsystem.guild.OpenGuildScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openGuildScreen(payload.data())));
        registrar.playToServer(com.soybe.awakeningsystem.guild.GuildActionPayload.TYPE, com.soybe.awakeningsystem.guild.GuildActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        handleGuildAction(player, payload);
                    }
                }));
        registrar.playToClient(ShadowEmergencePayload.TYPE, ShadowEmergencePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.triggerShadowEmergence(
                        payload.entityId(), payload.x(), payload.y(), payload.z(), payload.durationTicks(), payload.height())));
        registrar.playToClient(OpenMurimAlchemyPayload.TYPE, OpenMurimAlchemyPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openMurimAlchemyScreen(payload)));
        registrar.playToServer(MurimAlchemyRefinePayload.TYPE, MurimAlchemyRefinePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.block.MurimAlchemyCauldronBlock.handleRefinePacket(player, payload);
                    }
                }));
        registrar.playToClient(SyncPlayerBadgePayload.TYPE, SyncPlayerBadgePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.handleSyncPlayerBadge(payload)));
        registrar.playToClient(OpenGachaScreenPayload.TYPE, OpenGachaScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openGachaScreen(payload)));
        registrar.playToServer(GachaWishPayload.TYPE, GachaWishPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handleWish(player, payload);
                    }
                }));
        registrar.playToClient(GachaWishResultPayload.TYPE, GachaWishResultPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.handleGachaWishResult(payload)));
        registrar.playToServer(PaimonBargainBuyPayload.TYPE, PaimonBargainBuyPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handlePaimonBargain(player, payload);
                    }
                }));
        registrar.playToServer(SetGachaPathPayload.TYPE, SetGachaPathPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handleSetPath(player, payload);
                    }
                }));
        registrar.playToServer(GachaTopUpPayload.TYPE, GachaTopUpPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handleTopUp(player, payload);
                    }
                }));
        registrar.playToClient(OpenWelkinScreenPayload.TYPE, OpenWelkinScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openWelkinScreen(payload)));
        registrar.playToServer(ClaimWelkinDailyPayload.TYPE, ClaimWelkinDailyPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handleClaimWelkin(player, payload);
                    }
                }));
        registrar.playToServer(StartWelkinQuestPayload.TYPE, StartWelkinQuestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GachaManager.handleStartWelkinQuest(player);
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.network.EquipArtifactPayload.TYPE, com.soybe.awakeningsystem.network.EquipArtifactPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.artifact.ArtifactManager.handleEquipArtifact(player, payload);
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.network.UpgradeArtifactPayload.TYPE, com.soybe.awakeningsystem.network.UpgradeArtifactPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.artifact.ArtifactManager.handleUpgradeArtifact(player, payload);
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.network.CompanionActionPayload.TYPE, com.soybe.awakeningsystem.network.CompanionActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.artifact.ArtifactManager.handleCompanionAction(player, payload);
                    }
                }));
        registrar.playToClient(com.soybe.awakeningsystem.network.OpenCompanionScreenPayload.TYPE, com.soybe.awakeningsystem.network.OpenCompanionScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openCompanionScreen(payload)));
        registrar.playToClient(com.soybe.awakeningsystem.network.OpenCompanionAscensionPayload.TYPE, com.soybe.awakeningsystem.network.OpenCompanionAscensionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openCompanionAscensionScreen(payload)));
        registrar.playToServer(com.soybe.awakeningsystem.network.CompanionAscensionActionPayload.TYPE, com.soybe.awakeningsystem.network.CompanionAscensionActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.entity.CompanionAscensionManager.handleAction(player, payload);
                    }
                }));
        registrar.playToClient(com.soybe.awakeningsystem.casino.network.OpenSlotMachinePayload.TYPE, com.soybe.awakeningsystem.casino.network.OpenSlotMachinePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openSlotMachineScreen(payload)));
        registrar.playToServer(com.soybe.awakeningsystem.casino.network.SlotSpinC2SPayload.TYPE, com.soybe.awakeningsystem.casino.network.SlotSpinC2SPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.casino.SlotMachineServerHandler.handleSpin(player, payload);
                    }
                }));
        registrar.playToClient(com.soybe.awakeningsystem.casino.network.SlotSpinResultS2CPayload.TYPE, com.soybe.awakeningsystem.casino.network.SlotSpinResultS2CPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.handleSlotSpinResult(payload)));
        registrar.playToServer(com.soybe.awakeningsystem.casino.network.CloseSlotMachineC2SPayload.TYPE, com.soybe.awakeningsystem.casino.network.CloseSlotMachineC2SPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.casino.SlotMachineServerHandler.handleClose(player, payload);
                    }
                }));
        registrar.playToServer(OpenSubspaceC2SPayload.TYPE, OpenSubspaceC2SPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.subspace.SubspaceManager.openSubspace(player);
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.network.SubspaceActionPayload.TYPE, com.soybe.awakeningsystem.network.SubspaceActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        if ("enter".equalsIgnoreCase(payload.action())) {
                            com.soybe.awakeningsystem.subspace.SubspaceManager.enterSubspace(player);
                        } else if ("upgrade".equalsIgnoreCase(payload.action())) {
                            com.soybe.awakeningsystem.subspace.SubspaceManager.upgradeSubspace(player);
                        } else if ("exit".equalsIgnoreCase(payload.action())) {
                            com.soybe.awakeningsystem.subspace.SubspaceManager.exitSubspace(player);
                        } else if (payload.action().startsWith("page:")) {
                            try {
                                int page = Integer.parseInt(payload.action().substring(5));
                                if (player.containerMenu instanceof com.soybe.awakeningsystem.subspace.SubspaceMenu sm) {
                                    sm.setPage(page);
                                    player.containerMenu.broadcastChanges();
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }));
        registrar.playToClient(com.soybe.awakeningsystem.abyss.OpenSpiralAbyssPayload.TYPE, com.soybe.awakeningsystem.abyss.OpenSpiralAbyssPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AwakeningSystemClient.openSpiralAbyssScreen(payload)));
        registrar.playToServer(com.soybe.awakeningsystem.abyss.StartAbyssFloorPayload.TYPE, com.soybe.awakeningsystem.abyss.StartAbyssFloorPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleStartFloor(player, payload.floorNumber());
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.abyss.ClaimAbyssRewardPayload.TYPE, com.soybe.awakeningsystem.abyss.ClaimAbyssRewardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleClaimReward(player, payload.floorNumber());
                    }
                }));
        registrar.playToServer(com.soybe.awakeningsystem.network.PlaceTrackLinePayload.TYPE, com.soybe.awakeningsystem.network.PlaceTrackLinePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.soybe.awakeningsystem.util.TrackPlacementHelper.executeTrackPlacement(player, payload.startPos(), payload.endPos(), payload.isWideTrack());
                    }
                }));
    }

    public static void syncPlayerBadge(net.minecraft.server.level.ServerPlayer player) {
        if (player == null) return;
        SystemData data = player.getData(PLAYER_SYSTEM);
        int completedBounties = player.getPersistentData().getInt("bounties_total_completed");
        String guildRank = GuildRankManager.getGuildRank(completedBounties);
        String hunterRank = data.rank();
        SyncPlayerBadgePayload payload = new SyncPlayerBadgePayload(
                player.getUUID(),
                data.hunterClass(),
                data.level(),
                hunterRank,
                guildRank,
                data.constellationModifier(),
                data.isConstellation()
        );
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sync(net.minecraft.server.level.ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SystemSyncPayload(
                player.getData(PLAYER_SYSTEM).serializeNBT(player.registryAccess())));
        syncPlayerBadge(player);
    }

    public static void sendLeaderboard(net.minecraft.server.level.ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;
        List<LeaderboardPayload.LeaderboardEntry> list = new ArrayList<>();
        java.util.Set<String> names = new java.util.HashSet<>();
        for (var p : server.getPlayerList().getPlayers()) {
            SystemData d = p.getData(PLAYER_SYSTEM);
            String pName = p.getGameProfile().getName();
            names.add(pName);
            list.add(new LeaderboardPayload.LeaderboardEntry(
                    pName,
                    d.level(),
                    d.prestigeLevel(),
                    d.creaturesDefeated(),
                    d.coins()
            ));
        }

        // Benchmark Hunter rankings: recalculate every day as if hunters are actively clearing dungeons
        long inGameDay = Math.max(0, player.serverLevel().getDayTime() / 24000L);
        long realDay = java.time.LocalDate.now().toEpochDay() % 365;
        long day = inGameDay + realDay;

        record HunterProfile(String name, int baseLvl, int basePres, int baseKills, int baseCoins, int killsPerDay, int coinsPerDay, int daysPerLvl) {}

        HunterProfile[] benchmarks = new HunterProfile[]{
                // Solo Leveling
                new HunterProfile("Sung Jin-Woo", 120, 5, 12500, 65000, 150, 800, 2),
                new HunterProfile("Thomas Andre", 105, 4, 8900, 32000, 110, 550, 3),
                new HunterProfile("Liu Zhigang", 102, 4, 8400, 28000, 100, 500, 3),
                new HunterProfile("Christopher Reed", 98, 3, 7100, 22000, 90, 450, 4),
                new HunterProfile("Cha Hae-In", 92, 3, 5800, 16000, 80, 380, 4),
                new HunterProfile("Go Gun-Hee", 90, 3, 5200, 19000, 45, 420, 5),
                new HunterProfile("Choi Jong-In", 85, 2, 4200, 13000, 65, 320, 5),
                new HunterProfile("Baek Yoonho", 82, 2, 3900, 11000, 60, 290, 6),
                new HunterProfile("Lim Tae-Gyu", 76, 1, 2900, 8000, 48, 220, 7),
                new HunterProfile("Min Byung-Gyu", 74, 1, 1900, 9500, 25, 260, 8),
                new HunterProfile("Woo Jin-Chul", 72, 1, 2400, 7200, 42, 210, 8),

                // Omniscient Reader's Viewpoint (ORV)
                new HunterProfile("Yoo Joonghyuk", 118, 5, 11900, 60000, 145, 770, 2),
                new HunterProfile("Kim Dokja", 110, 4, 9500, 99999, 120, 999, 3),
                new HunterProfile("Han Sooyoung", 96, 3, 6500, 24000, 85, 400, 4),
                new HunterProfile("Jung Heewon", 89, 2, 5300, 14500, 78, 340, 5),
                new HunterProfile("Lee Hyunsung", 84, 2, 4100, 12500, 55, 280, 6),
                new HunterProfile("Yoo Sangah", 80, 2, 3600, 13800, 50, 310, 6),
                new HunterProfile("Shin Yoosung", 75, 1, 3100, 8800, 52, 230, 7),
                new HunterProfile("Lee Gilyoung", 73, 1, 2800, 7900, 48, 220, 7),

                // The Greatest Estate Developer
                new HunterProfile("Javier Asrahan", 108, 4, 9800, 35000, 130, 600, 3),
                new HunterProfile("Lloyd Frontera", 88, 3, 3200, 88888, 35, 950, 5),

                // The Beginning After The End (TBATE)
                new HunterProfile("Arthur Leywin", 115, 5, 11200, 54000, 135, 700, 2),

                // Second Life Ranker
                new HunterProfile("Cha Yeon-woo", 112, 4, 10400, 48000, 125, 660, 3),

                // Solo Max-Level Newbie
                new HunterProfile("Kang Jinhyuk", 106, 4, 9200, 42000, 115, 580, 3),

                // The S-Classes That I Raised
                new HunterProfile("Han Yoohyun", 94, 3, 6200, 18000, 82, 370, 4),
                new HunterProfile("Bak Yerim", 86, 2, 4600, 13500, 70, 330, 5),
                new HunterProfile("Han Yoojin", 68, 1, 1200, 15000, 20, 350, 9),

                // Doom Breaker
                new HunterProfile("Zephyr", 104, 4, 8700, 38000, 112, 540, 3),

                // Nano Machine
                new HunterProfile("Cheon Yeo-woon", 109, 4, 9900, 45000, 128, 640, 3),

                // Solo Leveling Ragnarok & Additional Hunters
                new HunterProfile("Sung Suho", 116, 5, 11400, 58000, 140, 750, 2),
                new HunterProfile("Siddharth Bachchan", 104, 4, 8600, 31000, 105, 520, 3),
                new HunterProfile("Goto Ryuji", 99, 3, 7500, 23000, 92, 460, 4),
                new HunterProfile("Lennart Niermann", 97, 3, 6900, 21000, 88, 430, 4),
                new HunterProfile("Jonas", 95, 3, 6400, 19000, 84, 410, 4),
                new HunterProfile("Hwang Dongsoo", 83, 2, 4000, 11500, 58, 285, 6),
                new HunterProfile("Kim Chul", 71, 1, 2300, 6800, 40, 195, 8),
                new HunterProfile("Lee Joohee", 65, 1, 1500, 6200, 22, 210, 9),
                new HunterProfile("Song Chi-Yul", 62, 1, 1400, 5900, 25, 180, 9),

                // Omniscient Reader Constellations & Mythological
                new HunterProfile("Sun Wukong", 138, 7, 18500, 88000, 180, 1200, 1),
                new HunterProfile("Secretive Plotter", 140, 7, 19200, 95000, 190, 1300, 1),
                new HunterProfile("Uriel", 135, 6, 17000, 82000, 175, 1100, 2),
                new HunterProfile("Abyssal Flame Dragon", 132, 6, 16500, 79000, 170, 1050, 2),
                new HunterProfile("Hades", 139, 7, 18900, 91000, 185, 1250, 1),
                new HunterProfile("Persephone", 136, 6, 17500, 84000, 172, 1120, 2),
                new HunterProfile("Dionysus", 130, 6, 16000, 76000, 165, 1020, 2),

                // Return of the Mount Hua Sect
                new HunterProfile("Chung Myung", 125, 6, 13200, 72000, 155, 820, 2),
                new HunterProfile("Baek Cheon", 95, 3, 6100, 17500, 84, 380, 4),
                new HunterProfile("Yu Yiseol", 93, 3, 5900, 16800, 81, 370, 4),
                new HunterProfile("Jo Gul", 87, 2, 4800, 13200, 72, 320, 5),
                new HunterProfile("Yoon Jong", 85, 2, 4500, 12800, 68, 310, 5),

                // Survival Story of a Sword King
                new HunterProfile("Ryu Hanbin", 122, 5, 12800, 68000, 150, 790, 2),
                new HunterProfile("Kivier", 114, 4, 9600, 41000, 120, 620, 3),
                new HunterProfile("Atisse", 92, 3, 5600, 17000, 78, 360, 4),

                // Tower of God
                new HunterProfile("Twenty-Fifth Baam", 128, 6, 14500, 85000, 160, 900, 2),
                new HunterProfile("Urek Mazino", 142, 8, 21000, 110000, 200, 1400, 1),
                new HunterProfile("Zahard", 145, 8, 22500, 120000, 210, 1500, 1),
                new HunterProfile("Khun Aguero Agnis", 102, 4, 8200, 36000, 108, 540, 3),
                new HunterProfile("Rak Wraithraiser", 100, 4, 8500, 29000, 110, 480, 3),
                new HunterProfile("Ha Jinsung", 118, 5, 11500, 56000, 138, 710, 2),
                new HunterProfile("Yuri Zahard", 107, 4, 9100, 34000, 114, 560, 3),

                // Solo Leveling Monarchs & Shadow Army
                new HunterProfile("Ashborn", 146, 8, 23000, 125000, 220, 1600, 1),
                new HunterProfile("Antares", 144, 8, 22000, 118000, 215, 1550, 1),
                new HunterProfile("Bellion", 119, 5, 12100, 62000, 142, 760, 2),
                new HunterProfile("Beru", 112, 4, 10800, 49000, 130, 670, 3),
                new HunterProfile("Igris", 110, 4, 10200, 46000, 126, 650, 3),

                // Reaper of the Drifting Moon
                new HunterProfile("Pyo Wol", 115, 5, 11800, 52000, 135, 690, 2),

                // SSS-Class Suicide Hunter
                new HunterProfile("Kim Gong-ja", 117, 5, 11900, 63000, 140, 740, 2),
                new HunterProfile("Raviel Iv-Consience", 105, 4, 8800, 33000, 112, 530, 3),

                // Pick Me Up, Infinite Gacha
                new HunterProfile("Han Yslat", 91, 3, 5700, 21000, 76, 390, 5),

                // Genshin Impact (Arcontes y Leyendas de Teyvat)
                new HunterProfile("Raiden Shogun", 136, 6, 17200, 85000, 170, 1100, 2),
                new HunterProfile("Zhongli", 138, 7, 18400, 92000, 178, 1180, 1),
                new HunterProfile("Neuvillette", 134, 6, 16800, 81000, 165, 1050, 2),
                new HunterProfile("Arlecchino", 126, 5, 14200, 71000, 152, 850, 2),
                new HunterProfile("Xiao", 122, 5, 13100, 64000, 148, 780, 2),
                new HunterProfile("Ganyu", 108, 4, 9600, 39000, 118, 590, 3),
                new HunterProfile("Yelan", 106, 4, 9400, 37000, 115, 570, 3),
                new HunterProfile("Hu Tao", 98, 3, 7200, 26000, 90, 480, 4),
                new HunterProfile("Furina", 92, 3, 5800, 22000, 75, 420, 4),
                new HunterProfile("Diluc", 102, 4, 8800, 34000, 110, 530, 3),

                // Honkai: Star Rail (Emanadores y Cazadores Estelares)
                new HunterProfile("Acheron", 137, 6, 17900, 89000, 175, 1150, 2),
                new HunterProfile("Feixiao", 130, 6, 15900, 78000, 160, 980, 2),
                new HunterProfile("Dan Heng • IL", 125, 5, 14000, 69000, 150, 830, 2),
                new HunterProfile("Jing Yuan", 124, 5, 13800, 67000, 147, 810, 2),
                new HunterProfile("Firefly", 121, 5, 13200, 63000, 145, 770, 2),
                new HunterProfile("Aventurine", 105, 4, 8900, 48000, 110, 640, 3),
                new HunterProfile("March 7th", 88, 2, 5100, 18000, 74, 340, 5),

                // Zenless Zone Zero (Agentes de Élite de Nueva Eridu)
                new HunterProfile("Hoshimi Miyabi", 120, 5, 12600, 62000, 144, 760, 2),
                new HunterProfile("Ellen Joe", 107, 4, 9500, 38000, 116, 580, 3),
                new HunterProfile("Zhu Yuan", 101, 4, 8600, 32000, 108, 510, 3),
                new HunterProfile("Soldier 11", 97, 3, 7300, 25000, 92, 450, 4),
                new HunterProfile("Billy Kid", 82, 2, 4200, 14000, 65, 290, 6)
        };

        for (var bench : benchmarks) {
            if (list.size() >= 100) break;
            if (!names.contains(bench.name())) {
                java.util.Random rng = new java.util.Random(bench.name().hashCode() * 31L + day * 1013L);
                int dayVarKills = rng.nextInt(Math.max(1, bench.killsPerDay() / 2));
                int dayVarCoins = rng.nextInt(Math.max(1, bench.coinsPerDay() / 2));

                int extraKills = (int) (day * bench.killsPerDay()) + dayVarKills;
                int extraCoins = (int) (day * bench.coinsPerDay()) + dayVarCoins;
                int extraLevels = (int) (day / bench.daysPerLvl());
                int extraPrestige = (int) (day / 45);

                int lvl = Math.min(1000, bench.baseLvl() + extraLevels);
                int pres = Math.min(100, bench.basePres() + extraPrestige);
                int kills = bench.baseKills() + extraKills;
                int coins = bench.baseCoins() + extraCoins;

                list.add(new LeaderboardPayload.LeaderboardEntry(bench.name(), lvl, pres, kills, coins));
                names.add(bench.name());
            }
        }

        list.sort((a, b) -> {
            int pCmp = Integer.compare(b.prestige(), a.prestige());
            if (pCmp != 0) return pCmp;
            int lvlCmp = Integer.compare(b.level(), a.level());
            if (lvlCmp != 0) return lvlCmp;
            int kCmp = Integer.compare(b.kills(), a.kills());
            if (kCmp != 0) return kCmp;
            return Integer.compare(b.coins(), a.coins());
        });
        PacketDistributor.sendToPlayer(player, new LeaderboardPayload(list));
    }

    public static void notify(net.minecraft.server.level.ServerPlayer player, String titleKey, String messageKey, int color, Object... arguments) {
        String argument;
        if (arguments.length == 0) {
            argument = "";
        } else if (arguments.length == 1) {
            argument = String.valueOf(arguments[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) sb.append(":::");
                sb.append(String.valueOf(arguments[i]));
            }
            argument = sb.toString();
        }
        PacketDistributor.sendToPlayer(player, new SystemNotificationPayload(titleKey, messageKey, argument, color));
    }

    public static void notifyAll(net.minecraft.server.MinecraftServer server, String titleKey, String messageKey, int color, Object... arguments) {
        if (server == null) return;
        for (var p : server.getPlayerList().getPlayers()) {
            notify(p, titleKey, messageKey, color, arguments);
        }
    }

    public static void handleGuildAction(net.minecraft.server.level.ServerPlayer player, com.soybe.awakeningsystem.guild.GuildActionPayload payload) {
        if (player == null || payload == null) return;
        var data = com.soybe.awakeningsystem.guild.GuildSavedData.get(player.server);
        switch (payload.action()) {
            case "CREATE" -> {
                String[] parts = payload.target().split(":", 2);
                String name = parts[0];
                String tag = parts.length > 1 ? parts[1] : (name.length() > 3 ? name.substring(0, 3) : name);
                data.createGuild(player, name, tag);
            }
            case "DISBAND" -> data.disbandGuild(player);
            case "LEAVE" -> data.leaveGuild(player);
            case "INVITE" -> {
                var target = player.server.getPlayerList().getPlayerByName(payload.target());
                if (target != null) {
                    data.invitePlayer(player, target);
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cEl jugador no está en línea."));
                }
            }
            case "ACCEPT" -> data.acceptInvite(player);
            case "DECLINE" -> data.declineInvite(player);
            case "KICK" -> {
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(payload.target());
                    data.kickMember(player, uuid);
                } catch (Exception ignored) {}
            }
            case "PROMOTE" -> {
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(payload.target());
                    data.promoteMember(player, uuid);
                } catch (Exception ignored) {}
            }
            case "DEMOTE" -> {
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(payload.target());
                    data.demoteMember(player, uuid);
                } catch (Exception ignored) {}
            }
            case "DEPOSIT" -> data.depositMora(player, payload.amount());
            case "DEPOSIT_COINS" -> data.depositCoins(player, payload.amount());
            case "WITHDRAW_MORA" -> data.withdrawMora(player, payload.amount());
            case "WITHDRAW_COINS" -> data.withdrawCoins(player, payload.amount());
            case "REQUEST_WITHDRAW" -> data.requestWithdrawal(player, payload.target(), payload.amount());
            case "BUY_HUNTING_BOARD" -> data.buyHuntingBoard(player);
            case "CLAIM_CHUNK" -> {
                if (payload.target() != null && payload.target().contains(":")) {
                    String[] coords = payload.target().split(":", 2);
                    try {
                        int cx = Integer.parseInt(coords[0].trim());
                        int cz = Integer.parseInt(coords[1].trim());
                        data.claimSpecificChunk(player, cx, cz);
                    } catch (NumberFormatException e) {
                        data.claimCurrentChunk(player);
                    }
                } else {
                    data.claimCurrentChunk(player);
                }
            }
            case "UNCLAIM_CHUNK" -> {
                if (payload.target() != null && payload.target().contains(":")) {
                    String[] coords = payload.target().split(":", 2);
                    try {
                        int cx = Integer.parseInt(coords[0].trim());
                        int cz = Integer.parseInt(coords[1].trim());
                        data.unclaimSpecificChunk(player, cx, cz);
                    } catch (NumberFormatException e) {
                        data.unclaimCurrentChunk(player);
                    }
                } else {
                    data.unclaimCurrentChunk(player);
                }
            }
            case "PERK" -> data.unlockPerk(player, payload.target());
            case "SET_HOME" -> data.setGuildHq(player);
            case "HOME" -> data.teleportToHq(player);
            case "OPEN", "SYNC" -> data.syncToPlayer(player);
        }
    }
}
