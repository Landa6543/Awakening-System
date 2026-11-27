package com.soybe.awakeningsystem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SystemCommands {
    private SystemCommands() {}

    public static boolean hasAdminOrCreatorPermission(CommandSourceStack source) {
        if (source == null) return false;
        if (source.hasPermission(2)) return true;
        return source.getEntity() instanceof ServerPlayer player && TitleDefinitions.isCreator(player);
    }

    public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        com.soybe.awakeningsystem.guild.GuildCommands.register(dispatcher);

        dispatcher.register(Commands.literal("creator")
                .executes(context -> claimCreatorTitle(context.getSource())));
        dispatcher.register(Commands.literal("nanda")
                .executes(context -> claimNandaTitle(context.getSource())));
        dispatcher.register(Commands.literal("misticpotato")
                .executes(context -> claimMisticPotatoTitle(context.getSource())));
        dispatcher.register(Commands.literal("potato")
                .executes(context -> claimMisticPotatoTitle(context.getSource())));
        dispatcher.register(Commands.literal("schvis")
                .executes(context -> claimSchvisTitle(context.getSource())));
        dispatcher.register(Commands.literal("tomcraft")
                .executes(context -> claimTomcraftTitle(context.getSource())));
        dispatcher.register(Commands.literal("pokemon")
                .executes(context -> claimTomcraftTitle(context.getSource())));
        dispatcher.register(Commands.literal("hatter")
                .executes(context -> claimHatterTitle(context.getSource())));
        dispatcher.register(Commands.literal("madhatter")
                .executes(context -> claimHatterTitle(context.getSource())));
        dispatcher.register(Commands.literal("orthax")
                .executes(context -> claimOrthaxTitle(context.getSource())));
        dispatcher.register(Commands.literal("wraith")
                .executes(context -> claimOrthaxTitle(context.getSource())));
        dispatcher.register(Commands.literal("welcome")
                .executes(context -> openWelcome(context.getSource())));
        dispatcher.register(Commands.literal("discord")
                .executes(context -> openDiscordLink(context.getSource())));

        dispatcher.register(Commands.literal("passive_qi")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> getPassiveQiConfigCommand(context.getSource()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setPassiveQiConfigCommand(context.getSource(), BoolArgumentType.getBool(context, "enabled")))));

        dispatcher.register(Commands.literal("cultivation")
                .then(Commands.literal("passive_qi")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> getPassiveQiConfigCommand(context.getSource()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setPassiveQiConfigCommand(context.getSource(), BoolArgumentType.getBool(context, "enabled"))))));

        dispatcher.register(Commands.literal("shadows")
                .then(Commands.literal("deploy")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                SystemEvents.deployAllArmy(player);
                            }
                            return 1;
                        })
                        .then(Commands.argument("squad", IntegerArgumentType.integer(1, 4))
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        int sq = IntegerArgumentType.getInteger(ctx, "squad") - 1;
                                        SystemEvents.deploySquad(player, sq);
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("recall")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                SystemEvents.recallAllArmy(player);
                            }
                            return 1;
                        })
                        .then(Commands.argument("squad", IntegerArgumentType.integer(1, 4))
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        int sq = IntegerArgumentType.getInteger(ctx, "squad") - 1;
                                        SystemEvents.recallSquad(player, sq);
                                    }
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("workout")
                .then(Commands.literal("push_ups").executes(context -> startWorkoutCommand(context.getSource(), 0)))
                .then(Commands.literal("flexiones").executes(context -> startWorkoutCommand(context.getSource(), 0)))
                .then(Commands.literal("sit_ups").executes(context -> startWorkoutCommand(context.getSource(), 1)))
                .then(Commands.literal("abdominales").executes(context -> startWorkoutCommand(context.getSource(), 1)))
                .then(Commands.literal("squats").executes(context -> startWorkoutCommand(context.getSource(), 2)))
                .then(Commands.literal("sentadillas").executes(context -> startWorkoutCommand(context.getSource(), 2))));

        dispatcher.register(Commands.literal("entrenamiento")
                .redirect(dispatcher.getRoot().getChild("workout")));

        var systemBuilder = Commands.literal("system")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> show(context.getSource()))
                .then(Commands.literal("welcome").executes(context -> openWelcome(context.getSource())))
                .then(Commands.literal("discord").executes(context -> openDiscordLink(context.getSource())))
                .then(Commands.literal("creator").executes(context -> claimCreatorTitle(context.getSource())))
                .then(Commands.literal("nanda").executes(context -> claimNandaTitle(context.getSource())))
                .then(Commands.literal("misticpotato").executes(context -> claimMisticPotatoTitle(context.getSource())))
                .then(Commands.literal("potato").executes(context -> claimMisticPotatoTitle(context.getSource())))
                .then(Commands.literal("schvis").executes(context -> claimSchvisTitle(context.getSource())))
                .then(Commands.literal("tomcraft").executes(context -> claimTomcraftTitle(context.getSource())))
                .then(Commands.literal("hatter").executes(context -> claimHatterTitle(context.getSource())))
                .then(Commands.literal("madhatter").executes(context -> claimHatterTitle(context.getSource())))
                .then(Commands.literal("orthax").executes(context -> claimOrthaxTitle(context.getSource())))
                .then(Commands.literal("wraith").executes(context -> claimOrthaxTitle(context.getSource())))
                .then(Commands.literal("stats").executes(context -> show(context.getSource())))
                .then(Commands.literal("give_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("unlock_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("max").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("habit").executes(context -> showHabits(context.getSource())))
                .then(Commands.literal("habito").executes(context -> showHabits(context.getSource())))
                .then(Commands.literal("evaluate").executes(context -> evaluateHunterCommand(context.getSource())))
                .then(Commands.literal("ascender").executes(context -> evaluateHunterCommand(context.getSource())))
                .then(Commands.literal("rankup").executes(context -> evaluateHunterCommand(context.getSource())))
                .then(Commands.literal("summon_evaluator").executes(context -> summonEvaluatorCommand(context.getSource())))
                .then(Commands.literal("penalty").executes(context -> triggerPenaltyCommand(context.getSource())))
                .then(Commands.literal("config")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.literal("passive_qi")
                                .executes(context -> getPassiveQiConfigCommand(context.getSource()))
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> setPassiveQiConfigCommand(context.getSource(), BoolArgumentType.getBool(context, "enabled"))))))
                .then(Commands.literal("passive_qi")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> getPassiveQiConfigCommand(context.getSource()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setPassiveQiConfigCommand(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))
                .then(Commands.literal("dev")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.literal("season")
                                .then(Commands.literal("halloween").executes(context -> setSeasonCommand(context.getSource(), "halloween")))
                                .then(Commands.literal("christmas").executes(context -> setSeasonCommand(context.getSource(), "christmas")))
                                .then(Commands.literal("off").executes(context -> setSeasonCommand(context.getSource(), "off")))
                                .then(Commands.literal("status").executes(context -> getSeasonStatusCommand(context.getSource()))))
                        .then(Commands.literal("tribulation")
                                .executes(context -> startTribulationDevCommand(context.getSource(), -1, -1))
                                .then(Commands.argument("realm", com.mojang.brigadier.arguments.IntegerArgumentType.integer(2, 14))
                                        .executes(context -> startTribulationDevCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "realm"), -1))
                                        .then(Commands.argument("strikes", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 81))
                                                .executes(context -> startTribulationDevCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "realm"), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "strikes")))))))
                .then(Commands.literal("tribulation")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> startTribulationDevCommand(context.getSource(), -1, -1))
                        .then(Commands.argument("realm", com.mojang.brigadier.arguments.IntegerArgumentType.integer(2, 14))
                                .executes(context -> startTribulationDevCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "realm"), -1))
                                .then(Commands.argument("strikes", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 81))
                                        .executes(context -> startTribulationDevCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "realm"), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "strikes"))))))
                .then(Commands.literal("season")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.literal("halloween").executes(context -> setSeasonCommand(context.getSource(), "halloween")))
                        .then(Commands.literal("christmas").executes(context -> setSeasonCommand(context.getSource(), "christmas")))
                        .then(Commands.literal("off").executes(context -> setSeasonCommand(context.getSource(), "off")))
                        .then(Commands.literal("status").executes(context -> getSeasonStatusCommand(context.getSource()))))
                .then(Commands.literal("workout")
                        .then(Commands.literal("push_ups").executes(context -> startWorkoutCommand(context.getSource(), 0)))
                        .then(Commands.literal("flexiones").executes(context -> startWorkoutCommand(context.getSource(), 0)))
                        .then(Commands.literal("sit_ups").executes(context -> startWorkoutCommand(context.getSource(), 1)))
                        .then(Commands.literal("abdominales").executes(context -> startWorkoutCommand(context.getSource(), 1)))
                        .then(Commands.literal("squats").executes(context -> startWorkoutCommand(context.getSource(), 2)))
                        .then(Commands.literal("sentadillas").executes(context -> startWorkoutCommand(context.getSource(), 2))))
                .then(Commands.literal("tower")
                        .executes(context -> enterTower(context.getSource()))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawnTowerStructure(context.getSource())))
                        .then(Commands.literal("structure")
                                .executes(context -> spawnTowerStructure(context.getSource())))
                        .then(Commands.literal("export")
                                .executes(context -> exportTowerCommand(context.getSource(), 0))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(10, 100))
                                        .executes(context -> exportTowerCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("reset")
                                .executes(context -> resetTowerCommand(context.getSource())))
                        .then(Commands.literal("reset_missions")
                                .executes(context -> resetTowerMissionsCommand(context.getSource())))
                        .then(Commands.literal("reset_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("respawn_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("floor")
                                .then(Commands.argument("floorNum", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> warpTowerFloor(context.getSource(), IntegerArgumentType.getInteger(context, "floorNum"))))))
                .then(Commands.literal("spawn_tower")
                        .executes(context -> spawnTowerStructure(context.getSource())))
                .then(Commands.literal("ranker_tower")
                        .executes(context -> enterTower(context.getSource()))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawnTowerStructure(context.getSource())))
                        .then(Commands.literal("structure")
                                .executes(context -> spawnTowerStructure(context.getSource())))
                        .then(Commands.literal("export")
                                .executes(context -> exportTowerCommand(context.getSource(), 0))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(10, 100))
                                        .executes(context -> exportTowerCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("reset")
                                .executes(context -> resetTowerCommand(context.getSource())))
                        .then(Commands.literal("reset_missions")
                                .executes(context -> resetTowerMissionsCommand(context.getSource())))
                        .then(Commands.literal("reset_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("respawn_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("floor")
                                .then(Commands.argument("floorNum", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> warpTowerFloor(context.getSource(), IntegerArgumentType.getInteger(context, "floorNum"))))))
                .then(Commands.literal("cultivate").executes(context -> cultivate(context.getSource())))
                .then(Commands.literal("meditate").executes(context -> cultivate(context.getSource())))
                .then(Commands.literal("reinos").executes(context -> listCultivationRealmsCommand(context.getSource())))
                .then(Commands.literal("dantian")
                        .then(Commands.literal("reinos").executes(context -> listCultivationRealmsCommand(context.getSource())))
                        .then(Commands.literal("list").executes(context -> listCultivationRealmsCommand(context.getSource()))))
                .then(Commands.literal("isekai").executes(context -> warpIsekai(context.getSource())))
                .then(Commands.literal("murim").executes(context -> warpMurim(context.getSource())))
                .then(Commands.literal("abyss")
                        .executes(context -> warpAbyss(context.getSource()))
                        .then(Commands.literal("cave").executes(context -> warpAbyssCave(context.getSource())))
                        .then(Commands.literal("cueva").executes(context -> warpAbyssCave(context.getSource())))
                        .then(Commands.literal("spiral").executes(context -> warpAbyss(context.getSource()))))
                .then(Commands.literal("cueva_abisal").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("abyss_cave").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("teyvat")
                        .executes(context -> warpTeyvat(context.getSource()))
                        .then(Commands.literal("golden_house").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                        .then(Commands.literal("childe").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                        .then(Commands.literal("tenshukaku").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                        .then(Commands.literal("raiden").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                        .then(Commands.literal("wolflord").executes(context -> warpTeyvatCapital(context.getSource(), "wolflord")))
                        .then(Commands.literal("azhdaha").executes(context -> warpTeyvatCapital(context.getSource(), "azhdaha")))
                        .then(Commands.literal("windrise").executes(context -> warpTeyvatCapital(context.getSource(), "windrise")))
                        .then(Commands.literal("jade_chamber").executes(context -> warpTeyvatCapital(context.getSource(), "jade_chamber")))
                        .then(Commands.literal("anemo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "anemo_hypostasis")))
                        .then(Commands.literal("electro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "electro_hypostasis")))
                        .then(Commands.literal("geo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "geo_hypostasis")))
                        .then(Commands.literal("cryo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "cryo_hypostasis")))
                        .then(Commands.literal("pyro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "pyro_hypostasis")))
                        .then(Commands.literal("hydro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "hydro_hypostasis")))
                        .then(Commands.literal("mondstadt").executes(context -> warpTeyvatCapital(context.getSource(), "mondstadt")))
                        .then(Commands.literal("liyue").executes(context -> warpTeyvatCapital(context.getSource(), "liyue")))
                        .then(Commands.literal("inazuma").executes(context -> warpTeyvatCapital(context.getSource(), "inazuma")))
                        .then(Commands.literal("sumeru").executes(context -> warpTeyvatCapital(context.getSource(), "sumeru")))
                        .then(Commands.literal("fontaine").executes(context -> warpTeyvatCapital(context.getSource(), "fontaine")))
                        .then(Commands.literal("racetrack")
                                .executes(context -> warpRacetrackCommand(context.getSource()))
                                .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                                .then(Commands.literal("upgrade").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                                .then(Commands.literal("update_materials").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                                .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                                .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                                .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))))
                        .then(Commands.literal("hipodromo")
                                .executes(context -> warpRacetrackCommand(context.getSource()))
                                .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                                .then(Commands.literal("upgrade").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                                .then(Commands.literal("update_materials").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                                .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                                .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                                .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))))
                        .then(Commands.literal("locate")
                                .executes(context -> locateTeyvatStructureCommand(context.getSource(), null))
                                .then(Commands.argument("structure", StringArgumentType.greedyString())
                                        .executes(context -> locateTeyvatStructureCommand(context.getSource(), StringArgumentType.getString(context, "structure")))))
                        .then(Commands.literal("spawn_structure")
                                .then(Commands.argument("structure", StringArgumentType.greedyString())
                                        .executes(context -> spawnTeyvatStructureCommand(context.getSource(), StringArgumentType.getString(context, "structure")))))
                        .then(Commands.literal("list").executes(context -> listTeyvatStructuresCommand(context.getSource())))
                        .then(Commands.literal("rebuild_capitals").executes(context -> rebuildCapitalsCommand(context.getSource())))
                        .then(Commands.literal("portal")
                                .executes(context -> warpAbyss(context.getSource())))
                        .then(Commands.literal("abyss")
                                .executes(context -> warpAbyss(context.getSource()))
                                .then(Commands.literal("active").executes(context -> setAbyssStateCommand(context.getSource(), true)))
                                .then(Commands.literal("unlock").executes(context -> setAbyssStateCommand(context.getSource(), true)))
                                .then(Commands.literal("locked").executes(context -> setAbyssStateCommand(context.getSource(), false)))
                                .then(Commands.literal("lock").executes(context -> setAbyssStateCommand(context.getSource(), false)))
                                .then(Commands.literal("toggle").executes(context -> toggleAbyssStateCommand(context.getSource())))
                                .then(Commands.literal("rebuild").executes(context -> rebuildAbyssCommand(context.getSource()))))
                        .then(Commands.literal("build_abyss")
                                .executes(context -> buildAbyssPortalCommand(context.getSource(), "3d"))
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .executes(context -> buildAbyssPortalCommand(context.getSource(), StringArgumentType.getString(context, "mode")))))
                        .then(Commands.argument("destination", StringArgumentType.word())
                                .executes(context -> warpTeyvatCapital(context.getSource(), StringArgumentType.getString(context, "destination")))))
                .then(Commands.literal("racetrack")
                        .executes(context -> warpRacetrackCommand(context.getSource()))
                        .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                        .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                        .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                        .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))))
                .then(Commands.literal("hipodromo")
                        .executes(context -> warpRacetrackCommand(context.getSource()))
                        .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                        .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                        .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                        .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))))
                .then(Commands.literal("gacha").executes(context -> openGachaCommand(context.getSource())))
                .then(Commands.literal("wish").executes(context -> openGachaCommand(context.getSource())))
                .then(Commands.literal("welkin")
                        .executes(context -> openWelkinCommand(context.getSource()))
                        .then(Commands.literal("open").executes(context -> openWelkinCommand(context.getSource())))
                        .then(Commands.literal("upgrade").executes(context -> upgradeWelkinCommand(context.getSource())))
                        .then(Commands.literal("quest")
                                .then(Commands.literal("start").executes(context -> startWelkinQuestCommand(context.getSource())))
                                .then(Commands.literal("complete").executes(context -> completeWelkinQuestCommand(context.getSource())))
                                .then(Commands.literal("reset").executes(context -> resetWelkinQuestCommand(context.getSource()))))
                        .then(Commands.literal("sanctuary").executes(context -> spawnWelkinSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("adddays")
                                .then(Commands.argument("days", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 365))
                                        .executes(context -> addWelkinDaysCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "days"))))))
                .then(Commands.literal("fables").executes(context -> showFables(context.getSource())))
                .then(Commands.literal("fabulas").executes(context -> showFables(context.getSource())))
                .then(Commands.literal("subespacio")
                        .executes(context -> openSubspaceCommand(context.getSource()))
                        .then(Commands.literal("enter").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitSubspaceCommand(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitSubspaceCommand(context.getSource())))
                        .then(Commands.literal("sync").executes(context -> syncSubspaceCommand(context.getSource())))
                        .then(Commands.literal("sincronizar").executes(context -> syncSubspaceCommand(context.getSource())))
                        .then(Commands.literal("upgrade").executes(context -> upgradeSubspaceCommand(context.getSource())))
                        .then(Commands.literal("mejorar").executes(context -> upgradeSubspaceCommand(context.getSource()))))
                .then(Commands.literal("subspace")
                        .executes(context -> openSubspaceCommand(context.getSource()))
                        .then(Commands.literal("enter").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitSubspaceCommand(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitSubspaceCommand(context.getSource())))
                        .then(Commands.literal("sync").executes(context -> syncSubspaceCommand(context.getSource())))
                        .then(Commands.literal("sincronizar").executes(context -> syncSubspaceCommand(context.getSource())))
                        .then(Commands.literal("upgrade").executes(context -> upgradeSubspaceCommand(context.getSource())))
                        .then(Commands.literal("mejorar").executes(context -> upgradeSubspaceCommand(context.getSource()))))
                .then(Commands.literal("sanctuary")
                        .executes(context -> enterSanctuaryCommand(context.getSource()))
                        .then(Commands.literal("door").executes(context -> summonSanctuaryDoorCommand(context.getSource())))
                        .then(Commands.literal("puerta").executes(context -> summonSanctuaryDoorCommand(context.getSource())))
                        .then(Commands.literal("enter").executes(context -> enterSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("upgrade").executes(context -> upgradeSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("mejorar").executes(context -> upgradeSanctuaryCommand(context.getSource()))))
                .then(Commands.literal("santuario")
                        .executes(context -> enterSanctuaryCommand(context.getSource()))
                        .then(Commands.literal("door").executes(context -> summonSanctuaryDoorCommand(context.getSource())))
                        .then(Commands.literal("puerta").executes(context -> summonSanctuaryDoorCommand(context.getSource())))
                        .then(Commands.literal("enter").executes(context -> enterSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("upgrade").executes(context -> upgradeSanctuaryCommand(context.getSource())))
                        .then(Commands.literal("mejorar").executes(context -> upgradeSanctuaryCommand(context.getSource()))))
                .then(Commands.literal("version")
                        .then(Commands.argument("ver", StringArgumentType.word())
                                .executes(context -> setSystemVersion(context.getSource(), StringArgumentType.getString(context, "ver")))))
                .then(Commands.literal("setversion")
                        .then(Commands.argument("ver", StringArgumentType.word())
                                .executes(context -> setSystemVersion(context.getSource(), StringArgumentType.getString(context, "ver")))))
                .then(Commands.literal("skipupdate").executes(context -> skipUpdateCommand(context.getSource())))
                .then(Commands.literal("grant_fable")
                        .then(Commands.argument("fableId", StringArgumentType.word())
                                .executes(context -> grantFableCommand(context.getSource(), StringArgumentType.getString(context, "fableId")))))
                .then(Commands.literal("physique")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(context -> setPhysiqueCommand(context.getSource(), StringArgumentType.getString(context, "type")))))
                .then(Commands.literal("worldtree")
                        .executes(context -> warpWorldTree(context.getSource(), null))
                        .then(Commands.literal("procedural")
                                .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                        .then(Commands.literal("nbt")
                                .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                        .then(Commands.literal("rebuild")
                                .executes(context -> warpWorldTree(context.getSource(), "rebuild"))))
                .then(Commands.literal("arboldelmundo")
                        .executes(context -> warpWorldTree(context.getSource(), null))
                        .then(Commands.literal("procedural")
                                .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                        .then(Commands.literal("nbt")
                                .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                        .then(Commands.literal("rebuild")
                                .executes(context -> warpWorldTree(context.getSource(), "rebuild"))))
                .then(Commands.literal("complete_update").executes(context -> completeUpdate(context.getSource())))
                .then(Commands.literal("reset")
                        .executes(context -> resetPlayer(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> resetPlayer(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("top").executes(context -> showTop(context.getSource())))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> inspect(context.getSource(),
                                        EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("attribute")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> addAttribute(context.getSource(),
                                        StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("coins")
                        .executes(context -> showCoins(context.getSource()))
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> giveCoins(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "amount"),
                                                EntityArgument.getPlayer(context, "target"))))))
                .then(Commands.literal("addcoins")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> giveCoins(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "amount"),
                                                EntityArgument.getPlayer(context, "target"))))))
                .then(Commands.literal("givecoins")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> giveCoins(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "amount"),
                                                EntityArgument.getPlayer(context, "target"))))))
                .then(Commands.literal("trance").executes(context -> triggerTrance(context.getSource())))
                .then(Commands.literal("combo")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0, 100))
                                .executes(context -> setCombo(context.getSource(), IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.literal("level")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 2000))
                                .executes(context -> setLevel(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("sponsor")
                        .then(Commands.argument("constellation", StringArgumentType.word())
                                .executes(context -> setSponsor(context.getSource(), StringArgumentType.getString(context, "constellation")))))
                .then(Commands.literal("scenario")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> setScenario(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("completescenario").executes(context -> completeScenario(context.getSource())))
                .then(Commands.literal("completeawakening").executes(context -> completeAwakeningCommand(context.getSource())))
                .then(Commands.literal("dungeon")
                        .executes(context -> triggerDungeon(context.getSource(), "D", "crypt"))
                        .then(Commands.argument("rankOrTheme", StringArgumentType.word())
                                .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), null))
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), StringArgumentType.getString(context, "theme"))))))
                .then(Commands.literal("learn")
                        .then(Commands.argument("skill", StringArgumentType.word())
                                .executes(context -> learnSkill(context.getSource(), StringArgumentType.getString(context, "skill")))))
                .then(Commands.literal("sponsor_selection").executes(context -> openSponsorSelection(context.getSource())))
                .then(Commands.literal("unique_list").executes(context -> listUniqueRanks(context.getSource())))
                .then(Commands.literal("unique_claim")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> claimUnique(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("constellation")
                        .then(Commands.literal("ascend")
                                .executes(context -> ascendConstellation(context.getSource(), null, ""))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), ""))
                                        .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                                .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "modifier"))))))
                        .then(Commands.literal("ascender")
                                .executes(context -> ascendConstellation(context.getSource(), null, ""))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), ""))
                                        .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                                .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "modifier"))))))
                        .then(Commands.literal("demote")
                                .executes(context -> demoteConstellation(context.getSource(), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> demoteConstellation(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(Commands.literal("degradar")
                                .executes(context -> demoteConstellation(context.getSource(), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> demoteConstellation(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(Commands.literal("name")
                                .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                        .executes(context -> renameConstellation(context.getSource(), StringArgumentType.getString(context, "modifier")))))
                        .then(Commands.literal("nombre")
                                .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                        .executes(context -> renameConstellation(context.getSource(), StringArgumentType.getString(context, "modifier"))))))
                .then(Commands.literal("constelacion")
                        .then(Commands.literal("ascender")
                                .executes(context -> ascendConstellation(context.getSource(), null, ""))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), ""))
                                        .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                                .executes(context -> ascendConstellation(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "modifier"))))))
                        .then(Commands.literal("degradar")
                                .executes(context -> demoteConstellation(context.getSource(), null))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> demoteConstellation(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(Commands.literal("nombre")
                                .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                        .executes(context -> renameConstellation(context.getSource(), StringArgumentType.getString(context, "modifier")))))
                        .then(Commands.literal("regresar")
                                .executes(context -> returnFromAstral(context.getSource()))))
                .then(Commands.literal("guide").executes(context -> openGuide(context.getSource())))
                .then(Commands.literal("bestiary").executes(context -> openBestiary(context.getSource())))
                .then(Commands.literal("bestiario").executes(context -> openBestiary(context.getSource())))
                .then(Commands.literal("structure")
                        .then(Commands.literal("tp")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                                        .executes(context -> tpStructureCommand(context.getSource(), StringArgumentType.getString(context, "name")))))
                        .then(Commands.literal("locate")
                                .executes(context -> locateStructureCommand(context.getSource(), null))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                                        .executes(context -> locateStructureCommand(context.getSource(), StringArgumentType.getString(context, "type")))))
                        .then(Commands.literal("list")
                                .executes(context -> listStructuresCommand(context.getSource())))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                                .executes(context -> spawnStructureCommand(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("npc")
                        .then(Commands.argument("character", StringArgumentType.word())
                                .executes(context -> spawnNpcCommand(context.getSource(), StringArgumentType.getString(context, "character")))))
                .then(Commands.literal("sombras")
                        .executes(context -> showShadowStatus(context.getSource()))
                        .then(Commands.literal("guardar").executes(context -> storeShadowsCommand(context.getSource())))
                        .then(Commands.literal("sacar").executes(context -> deployShadowsCommand(context.getSource())))
                        .then(Commands.literal("estado").executes(context -> showShadowStatus(context.getSource())))
                        .then(Commands.literal("liberar").executes(context -> dismissShadowsCommand(context.getSource()))))
                .then(Commands.literal("pagoda")
                        .executes(context -> enterPagodaTrial(context.getSource()))
                        .then(Commands.literal("enter").executes(context -> enterPagodaTrial(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterPagodaTrial(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitPagodaTrial(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitPagodaTrial(context.getSource())))
                        .then(Commands.literal("arena")
                                .requires(source -> hasAdminOrCreatorPermission(source))
                                .executes(context -> spawnPagodaArenaCommand(context.getSource(), 1))
                                .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                        .executes(context -> spawnPagodaArenaCommand(context.getSource(), IntegerArgumentType.getInteger(context, "stage")))))
                        .then(Commands.literal("warp")
                                .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                        .executes(context -> warpPagodaStage(context.getSource(), IntegerArgumentType.getInteger(context, "stage"))))))
                .then(Commands.literal("casino")
                        .executes(context -> warpCasinoCommand(context.getSource()))
                        .then(Commands.literal("tp").executes(context -> warpCasinoCommand(context.getSource())))
                        .then(Commands.literal("claim_jackpot").executes(context -> claimJackpotCommand(context.getSource())))
                        .then(Commands.literal("jackpot").executes(context -> claimJackpotCommand(context.getSource())))
                        .then(Commands.literal("build").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> rebuildCasinoCommand(context.getSource())))
                        .then(Commands.literal("rebuild").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> rebuildCasinoCommand(context.getSource()))))
                .then(Commands.literal("announcement")
                        .executes(context -> testAnnouncementCommand(context.getSource(), "mythic"))
                        .then(Commands.literal("test").executes(context -> testAnnouncementCommand(context.getSource(), "mythic")))
                        .then(Commands.literal("mythic").executes(context -> testAnnouncementCommand(context.getSource(), "mythic")))
                        .then(Commands.literal("gold").executes(context -> testAnnouncementCommand(context.getSource(), "gold")))
                        .then(Commands.literal("doncella").executes(context -> testAnnouncementCommand(context.getSource(), "doncella")))
                        .then(Commands.argument("title", StringArgumentType.string())
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> customAnnouncementCommand(context.getSource(),
                                                StringArgumentType.getString(context, "title"),
                                                StringArgumentType.getString(context, "message"))))));

        dispatcher.register(systemBuilder);

        var shadowsBuilder = Commands.literal("sombras")
                .executes(context -> showShadowStatus(context.getSource()))
                .then(Commands.literal("guardar").executes(context -> storeShadowsCommand(context.getSource())))
                .then(Commands.literal("sacar").executes(context -> deployShadowsCommand(context.getSource())))
                .then(Commands.literal("estado").executes(context -> showShadowStatus(context.getSource())))
                .then(Commands.literal("liberar").executes(context -> dismissShadowsCommand(context.getSource())));
        dispatcher.register(shadowsBuilder);

        dispatcher.register(Commands.literal("shadows")
                .redirect(dispatcher.getRoot().getChild("sombras")));

        dispatcher.register(Commands.literal("subespacio")
                .executes(context -> openSubspaceCommand(context.getSource()))
                .then(Commands.literal("enter").executes(context -> enterSubspaceCommand(context.getSource())))
                .then(Commands.literal("entrar").executes(context -> enterSubspaceCommand(context.getSource())))
                .then(Commands.literal("exit").executes(context -> exitSubspaceCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> exitSubspaceCommand(context.getSource())))
                .then(Commands.literal("sync").executes(context -> syncSubspaceCommand(context.getSource())))
                .then(Commands.literal("sincronizar").executes(context -> syncSubspaceCommand(context.getSource())))
                .then(Commands.literal("upgrade").executes(context -> upgradeSubspaceCommand(context.getSource())))
                .then(Commands.literal("mejorar").executes(context -> upgradeSubspaceCommand(context.getSource())))
                .then(Commands.literal("update_structure")
                        .executes(context -> requestUpdateSubspaceStructureCommand(context.getSource()))
                        .then(Commands.literal("confirm").executes(context -> confirmUpdateSubspaceStructureCommand(context.getSource()))))
                .then(Commands.literal("actualizar_estructura")
                        .executes(context -> requestUpdateSubspaceStructureCommand(context.getSource()))
                        .then(Commands.literal("confirmar").executes(context -> confirmUpdateSubspaceStructureCommand(context.getSource())))));
        dispatcher.register(Commands.literal("subspace")
                .redirect(dispatcher.getRoot().getChild("subespacio")));

        // The skill screen's upgrade button runs this, so it cannot sit behind the admin-only /system.
        dispatcher.register(Commands.literal("sanctuary")
                .then(Commands.literal("upgrade").executes(context -> upgradeSanctuaryCommand(context.getSource()))));

        dispatcher.register(Commands.literal("structure")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.literal("tp")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                                .executes(context -> tpStructureCommand(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("locate")
                        .executes(context -> locateStructureCommand(context.getSource(), null))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                                .executes(context -> locateStructureCommand(context.getSource(), StringArgumentType.getString(context, "type")))))
                .then(Commands.literal("list")
                        .executes(context -> listStructuresCommand(context.getSource())))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ALL_STRUCTURE_NAMES, builder))
                        .executes(context -> spawnStructureCommand(context.getSource(), StringArgumentType.getString(context, "name")))));

        dispatcher.register(Commands.literal("dokkaebi")
                .executes(context -> showDokkaebiQuestCommand(context.getSource()))
                .then(Commands.literal("spawn")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> spawnDokkaebiCommand(context.getSource())))
                .then(Commands.literal("quest")
                        .executes(context -> showDokkaebiQuestCommand(context.getSource())))
                .then(Commands.literal("scenario")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> triggerDokkaebiScenarioCommand(context.getSource(), 0))
                        .then(Commands.argument("target", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                                .executes(context -> triggerDokkaebiScenarioCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "target")))))
                .then(Commands.literal("dismiss")
                        .executes(context -> dismissDokkaebiCommand(context.getSource()))));

        dispatcher.register(Commands.literal("meteor")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.literal("test").executes(context -> {
                    // from the console too: one falls about 90 blocks from the world spawn
                    BlockPos at = com.soybe.awakeningsystem.world.SideriteMeteorManager.spawnTestMeteor(context.getSource().getServer().overworld());
                    com.soybe.awakeningsystem.AwakeningSystemMod.LOGGER.info("[Meteor] test meteor falling towards {}", at == null ? "nowhere (no flat natural ground)" : at.toShortString());
                    return at == null ? 0 : 1;
                }))
                .then(Commands.literal("heal").executes(context -> {
                    // for testing: every crater counts as 5 days old and heals now if nobody is close by
                    int n = com.soybe.awakeningsystem.world.MeteorCraterHealer.ageAll(context.getSource().getServer());
                    com.soybe.awakeningsystem.world.MeteorCraterHealer.tick(context.getSource().getServer());
                    com.soybe.awakeningsystem.AwakeningSystemMod.LOGGER.info("[Meteor] heal check on {} crater(s)", n);
                    return n;
                }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    boolean spawned = com.soybe.awakeningsystem.world.SideriteMeteorManager.spawnMeteorNearPlayer(player, true);
                    if (spawned) {
                        context.getSource().sendSuccess(() -> Component.translatable("awakening_system.msg.meteorito_se_ha_invocado_una_caida"), true);
                    }
                    return 1;
                }));

        dispatcher.register(Commands.literal("boss")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.argument("type", StringArgumentType.word())
                        .executes(context -> spawnBossCommand(context.getSource(), StringArgumentType.getString(context, "type")))));

        dispatcher.register(Commands.literal("abyss")
                .executes(context -> warpAbyss(context.getSource()))
                .then(Commands.literal("cave").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("cueva").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("spiral").executes(context -> warpAbyss(context.getSource())))
                .then(Commands.literal("menu").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("gui").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("open").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("advance").executes(context -> advanceAbyssFloor(context.getSource())))
                .then(Commands.literal("next").executes(context -> advanceAbyssFloor(context.getSource())))
                .then(Commands.literal("leave").executes(context -> leaveAbyssCommand(context.getSource())))
                .then(Commands.literal("exit").executes(context -> leaveAbyssCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> leaveAbyssCommand(context.getSource())))
                .then(Commands.literal("active").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> setAbyssStateCommand(context.getSource(), true)))
                .then(Commands.literal("unlock").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> setAbyssStateCommand(context.getSource(), true)))
                .then(Commands.literal("locked").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> setAbyssStateCommand(context.getSource(), false)))
                .then(Commands.literal("lock").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> setAbyssStateCommand(context.getSource(), false)))
                .then(Commands.literal("rebuild").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> rebuildAbyssCommand(context.getSource()))));

        dispatcher.register(Commands.literal("npc")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.argument("character", StringArgumentType.word())
                        .executes(context -> spawnNpcCommand(context.getSource(), StringArgumentType.getString(context, "character")))));

        dispatcher.register(Commands.literal("escape")
                .executes(context -> escapeCommand(context.getSource())));

        dispatcher.register(Commands.literal("dungeon")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("claim").executes(context -> claimDungeonReward(context.getSource())))
                .then(Commands.literal("asegurar").executes(context -> claimDungeonReward(context.getSource())))
                .then(Commands.literal("gamble").executes(context -> gambleDungeonReward(context.getSource())))
                .then(Commands.literal("apostar").executes(context -> gambleDungeonReward(context.getSource())))
                .then(Commands.literal("start")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> triggerDungeon(context.getSource(), "D", "crypt"))
                        .then(Commands.argument("rankOrTheme", StringArgumentType.word())
                                .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), null))
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), StringArgumentType.getString(context, "theme"))))))
                .then(Commands.argument("rankOrTheme", StringArgumentType.word())
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), null))
                        .then(Commands.argument("theme", StringArgumentType.word())
                                .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), StringArgumentType.getString(context, "theme")))))
                .then(Commands.literal("test_gate")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            BlockPos target = p.blockPosition().relative(p.getDirection(), 5);
                            DungeonManager.buildDungeonGateRuins(p.serverLevel(), target);
                            context.getSource().sendSuccess(() -> Component.translatable("awakening_system.msg.dungeon_gate_ruins_nbt_structure_placed", String.valueOf(target.toShortString())), true);
                            return 1;
                        }))
                .then(Commands.literal("ruins")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            BlockPos target = p.blockPosition().relative(p.getDirection(), 5);
                            DungeonManager.buildDungeonGateRuins(p.serverLevel(), target);
                            context.getSource().sendSuccess(() -> Component.translatable("awakening_system.msg.dungeon_gate_ruins_nbt_structure_placed", String.valueOf(target.toShortString())), true);
                            return 1;
                        }))
                .then(Commands.literal("rift")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer p = context.getSource().getPlayerOrException();
                                    DungeonManager.spawnRiftNear(p, StringArgumentType.getString(context, "rank"), "crypt", false);
                                    return 1;
                                })
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer p = context.getSource().getPlayerOrException();
                                            DungeonManager.spawnRiftNear(p, StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "theme"), false);
                                            return 1;
                                        })
                                        .then(Commands.argument("permanent", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerPlayer p = context.getSource().getPlayerOrException();
                                                    DungeonManager.spawnRiftNear(p, StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "theme"), BoolArgumentType.getBool(context, "permanent"));
                                                    return 1;
                                                }))))));

        dispatcher.register(Commands.literal("tower")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("enter")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> enterTower(context.getSource())))
                .then(Commands.literal("floor")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.argument("floor", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                                .executes(context -> warpTowerFloor(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "floor"))))));

        dispatcher.register(Commands.literal("sistema")
                .redirect(dispatcher.getRoot().getChild("system")));

        dispatcher.register(Commands.literal("murim")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> warpMurim(context.getSource())));

        dispatcher.register(Commands.literal("pagoda")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("enter")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> enterPagodaTrial(context.getSource())))
                .then(Commands.literal("entrar")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> enterPagodaTrial(context.getSource())))
                .then(Commands.literal("warp")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                .executes(context -> warpPagodaStage(context.getSource(), IntegerArgumentType.getInteger(context, "stage")))))
                .then(Commands.literal("arena")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> spawnPagodaArenaCommand(context.getSource(), 1))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                .executes(context -> spawnPagodaArenaCommand(context.getSource(), IntegerArgumentType.getInteger(context, "stage"))))));

        dispatcher.register(Commands.literal("constelacion")
                .then(Commands.literal("nombre")
                        .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                .executes(context -> renameConstellation(context.getSource(), StringArgumentType.getString(context, "modifier")))))
                .then(Commands.literal("regresar")
                        .executes(context -> returnFromAstral(context.getSource()))));

        dispatcher.register(Commands.literal("abyss_cave")
                .executes(context -> warpAbyssCave(context.getSource())));

        dispatcher.register(Commands.literal("cueva_abisal")
                .executes(context -> warpAbyssCave(context.getSource())));

        dispatcher.register(Commands.literal("guide")
                .executes(context -> openGuide(context.getSource())));
        dispatcher.register(Commands.literal("bestiary")
                .executes(context -> openBestiary(context.getSource())));
        dispatcher.register(Commands.literal("bestiario")
                .executes(context -> openBestiary(context.getSource())));

        dispatcher.register(Commands.literal("gacha")
                .executes(context -> openGachaCommand(context.getSource())));
        dispatcher.register(Commands.literal("wish")
                .executes(context -> openGachaCommand(context.getSource())));
        dispatcher.register(Commands.literal("wishes")
                .executes(context -> openGachaCommand(context.getSource())));

        dispatcher.register(Commands.literal("banner")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.literal("reset").executes(context -> resetBannerCommand(context.getSource())))
                .then(Commands.literal("set_phase")
                        .then(Commands.argument("phase", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 34))
                                .executes(context -> setBannerPhaseCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "phase"))))));

        dispatcher.register(Commands.literal("awakening")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.literal("banner")
                        .then(Commands.literal("reset").executes(context -> resetBannerCommand(context.getSource())))
                        .then(Commands.literal("set_phase")
                                .then(Commands.argument("phase", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 34))
                                        .executes(context -> setBannerPhaseCommand(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "phase")))))));

        dispatcher.register(Commands.literal("teyvat")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> warpTeyvat(context.getSource()))
                .then(Commands.literal("warp").executes(context -> warpTeyvat(context.getSource())))
                .then(Commands.literal("golden_house").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                .then(Commands.literal("childe").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                .then(Commands.literal("tenshukaku").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                .then(Commands.literal("raiden").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                .then(Commands.literal("wolflord").executes(context -> warpTeyvatCapital(context.getSource(), "wolflord")))
                .then(Commands.literal("azhdaha").executes(context -> warpTeyvatCapital(context.getSource(), "azhdaha")))
                .then(Commands.literal("windrise").executes(context -> warpTeyvatCapital(context.getSource(), "windrise")))
                .then(Commands.literal("jade_chamber").executes(context -> warpTeyvatCapital(context.getSource(), "jade_chamber")))
                .then(Commands.literal("anemo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "anemo_hypostasis")))
                .then(Commands.literal("electro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "electro_hypostasis")))
                .then(Commands.literal("geo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "geo_hypostasis")))
                .then(Commands.literal("cryo_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "cryo_hypostasis")))
                .then(Commands.literal("pyro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "pyro_hypostasis")))
                .then(Commands.literal("hydro_hypostasis").executes(context -> warpTeyvatCapital(context.getSource(), "hydro_hypostasis")))
                .then(Commands.literal("mondstadt").executes(context -> warpTeyvatCapital(context.getSource(), "mondstadt")))
                .then(Commands.literal("liyue").executes(context -> warpTeyvatCapital(context.getSource(), "liyue")))
                .then(Commands.literal("inazuma").executes(context -> warpTeyvatCapital(context.getSource(), "inazuma")))
                .then(Commands.literal("sumeru").executes(context -> warpTeyvatCapital(context.getSource(), "sumeru")))
                .then(Commands.literal("fontaine").executes(context -> warpTeyvatCapital(context.getSource(), "fontaine")))
                .then(Commands.literal("casino").executes(context -> warpCasinoCommand(context.getSource())))
                .then(Commands.literal("gran_casino").executes(context -> warpCasinoCommand(context.getSource())))
                .then(Commands.literal("spawn").executes(context -> warpTeyvat(context.getSource())))
                .then(Commands.literal("locate")
                        .executes(context -> locateTeyvatStructureCommand(context.getSource(), null))
                        .then(Commands.argument("structure", StringArgumentType.greedyString())
                                .executes(context -> locateTeyvatStructureCommand(context.getSource(), StringArgumentType.getString(context, "structure")))))
                .then(Commands.literal("spawn_structure")
                        .then(Commands.argument("structure", StringArgumentType.greedyString())
                                .executes(context -> spawnTeyvatStructureCommand(context.getSource(), StringArgumentType.getString(context, "structure")))))
                .then(Commands.literal("list").executes(context -> listTeyvatStructuresCommand(context.getSource())))
                .then(Commands.literal("sync_map").executes(context -> syncTeyvatMapCommand(context.getSource())))
                .then(Commands.literal("import_map").executes(context -> syncTeyvatMapCommand(context.getSource())))
                .then(Commands.literal("import").executes(context -> syncTeyvatMapCommand(context.getSource())))
                .then(Commands.literal("rebuild").executes(context -> rebuildCapitalsCommand(context.getSource())))
                .then(Commands.literal("cleanup").executes(context -> rebuildCapitalsCommand(context.getSource())))
                .then(Commands.literal("portal").executes(context -> buildAbyssPortalCommand(context.getSource(), "3d")))
                .then(Commands.literal("abyss").executes(context -> buildAbyssPortalCommand(context.getSource(), "3d")))
                .then(Commands.literal("build_abyss")
                        .executes(context -> buildAbyssPortalCommand(context.getSource(), "3d"))
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(context -> buildAbyssPortalCommand(context.getSource(), StringArgumentType.getString(context, "mode")))))
                .then(Commands.argument("destination", StringArgumentType.word())
                        .executes(context -> warpTeyvatCapital(context.getSource(), StringArgumentType.getString(context, "destination")))));

        dispatcher.register(Commands.literal("tp_teyvat")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> warpTeyvat(context.getSource()))
                .then(Commands.literal("golden_house").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                .then(Commands.literal("childe").executes(context -> warpTeyvatCapital(context.getSource(), "golden_house")))
                .then(Commands.literal("tenshukaku").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                .then(Commands.literal("raiden").executes(context -> warpTeyvatCapital(context.getSource(), "tenshukaku")))
                .then(Commands.literal("wolflord").executes(context -> warpTeyvatCapital(context.getSource(), "wolflord")))
                .then(Commands.literal("azhdaha").executes(context -> warpTeyvatCapital(context.getSource(), "azhdaha")))
                .then(Commands.literal("windrise").executes(context -> warpTeyvatCapital(context.getSource(), "windrise")))
                .then(Commands.literal("jade_chamber").executes(context -> warpTeyvatCapital(context.getSource(), "jade_chamber")))
                .then(Commands.literal("mondstadt").executes(context -> warpTeyvatCapital(context.getSource(), "mondstadt")))
                .then(Commands.literal("liyue").executes(context -> warpTeyvatCapital(context.getSource(), "liyue")))
                .then(Commands.literal("inazuma").executes(context -> warpTeyvatCapital(context.getSource(), "inazuma")))
                .then(Commands.literal("sumeru").executes(context -> warpTeyvatCapital(context.getSource(), "sumeru")))
                .then(Commands.literal("fontaine").executes(context -> warpTeyvatCapital(context.getSource(), "fontaine")))
                .then(Commands.literal("casino").executes(context -> warpCasinoCommand(context.getSource())))
                .then(Commands.literal("abyss")
                        .executes(context -> warpAbyss(context.getSource()))
                        .then(Commands.literal("cave").executes(context -> warpAbyssCave(context.getSource())))
                        .then(Commands.literal("cueva").executes(context -> warpAbyssCave(context.getSource())))
                        .then(Commands.literal("spiral").executes(context -> warpAbyss(context.getSource()))))
                .then(Commands.literal("cueva_abisal").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("abyss_cave").executes(context -> warpAbyssCave(context.getSource())))
                .then(Commands.literal("racetrack").executes(context -> warpRacetrackCommand(context.getSource())))
                .then(Commands.literal("hipodromo").executes(context -> warpRacetrackCommand(context.getSource())))
                .then(Commands.argument("capital", StringArgumentType.word())
                        .executes(context -> warpTeyvatCapital(context.getSource(), StringArgumentType.getString(context, "capital")))));

        // /setclass <class>: change the hunter class at once (to test the classes), for admins and the creator
        dispatcher.register(Commands.literal("setclass")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .then(Commands.argument("class", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (ClassEvolution.Base b : ClassEvolution.bases()) {
                                builder.suggest(b.id());
                                for (String evo : b.evolutions()) builder.suggest(evo);
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> setClassCommand(context.getSource(), context.getSource().getPlayerOrException(),
                                StringArgumentType.getString(context, "class")))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> setClassCommand(context.getSource(), EntityArgument.getPlayer(context, "target"),
                                        StringArgumentType.getString(context, "class"))))));

        dispatcher.register(Commands.literal("resetprogress")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer sp) {
                        return resetPlayer(context.getSource(), sp);
                    } else {
                        context.getSource().sendFailure(Component.translatable("awakening_system.msg.specify_player_resetprogress_player"));
                        return 0;
                    }
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> resetPlayer(context.getSource(), EntityArgument.getPlayer(context, "target")))));

        dispatcher.register(Commands.literal("trance")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> triggerTrance(context.getSource())));

        dispatcher.register(Commands.literal("cultivate")
                .executes(context -> cultivate(context.getSource())));
        dispatcher.register(Commands.literal("meditate")
                .executes(context -> cultivate(context.getSource())));
        dispatcher.register(Commands.literal("reinos")
                .executes(context -> listCultivationRealmsCommand(context.getSource())));

        dispatcher.register(Commands.literal("worldtree")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> warpWorldTree(context.getSource(), null))
                .then(Commands.literal("procedural")
                        .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                .then(Commands.literal("nbt")
                        .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                .then(Commands.literal("rebuild")
                        .executes(context -> warpWorldTree(context.getSource(), "rebuild"))));

        dispatcher.register(Commands.literal("arboldelmundo")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> warpWorldTree(context.getSource(), null))
                .then(Commands.literal("procedural")
                        .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                .then(Commands.literal("nbt")
                        .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                .then(Commands.literal("rebuild")
                        .executes(context -> warpWorldTree(context.getSource(), "rebuild"))));

        dispatcher.register(Commands.literal("coins")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> showCoins(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> giveCoins(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));

        dispatcher.register(Commands.literal("givecoins")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> showCoins(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> giveCoins(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));

        dispatcher.register(Commands.literal("carats")
                .executes(context -> showCarats(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> giveCarats(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCarats(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));

        dispatcher.register(Commands.literal("givecarats")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> showCarats(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> giveCarats(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCarats(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));

        dispatcher.register(Commands.literal("umahub")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        com.soybe.awakeningsystem.block.CustomRacetrackBlock.openHub(player, null);
                        return 1;
                    }
                    return 0;
                }));

        dispatcher.register(Commands.literal("racetrack")
                .executes(context -> warpRacetrackCommand(context.getSource()))
                .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                .then(Commands.literal("upgrade").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                .then(Commands.literal("update_materials").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                .then(Commands.literal("export_circuit").executes(context -> exportCircuitJsonCommand(context.getSource())))
                .then(Commands.literal("reload_circuit").executes(context -> reloadCircuitJsonCommand(context.getSource(), "custom_racetrack.json"))
                        .then(Commands.argument("file", StringArgumentType.string()).executes(context -> reloadCircuitJsonCommand(context.getSource(), StringArgumentType.getString(context, "file")))))
                .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))));

        dispatcher.register(Commands.literal("hipodromo")
                .executes(context -> warpRacetrackCommand(context.getSource()))
                .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource())))
                .then(Commands.literal("upgrade").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                .then(Commands.literal("update_materials").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                .then(Commands.literal("export_circuit").executes(context -> exportCircuitJsonCommand(context.getSource())))
                .then(Commands.literal("reload_circuit").executes(context -> reloadCircuitJsonCommand(context.getSource(), "custom_racetrack.json"))
                        .then(Commands.argument("file", StringArgumentType.string()).executes(context -> reloadCircuitJsonCommand(context.getSource(), StringArgumentType.getString(context, "file")))))
                .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))));

        dispatcher.register(Commands.literal("uma")
                .then(Commands.literal("track")
                        .then(Commands.literal("upgrade").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                        .then(Commands.literal("update_materials").executes(context -> upgradeRacetrackMaterialsCommand(context.getSource())))
                        .then(Commands.literal("export").executes(context -> exportRacetrackCommand(context.getSource(), 0))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(10, 300)).executes(context -> exportRacetrackCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("rebuild").executes(context -> rebuildRacetrackCommand(context.getSource())))
                        .then(Commands.literal("tp").executes(context -> warpRacetrackCommand(context.getSource()))))
                .then(Commands.literal("start").executes(context -> startRacetrackCommand(context.getSource())))
                .then(Commands.literal("hub").executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        com.soybe.awakeningsystem.block.CustomRacetrackBlock.openHub(player, null);
                        return 1;
                    }
                    return 0;
                })));

        dispatcher.register(Commands.literal("casino")
                .executes(context -> warpCasinoCommand(context.getSource()))
                .then(Commands.literal("tp").executes(context -> warpCasinoCommand(context.getSource())))
                .then(Commands.literal("claim_jackpot").executes(context -> claimJackpotCommand(context.getSource())))
                .then(Commands.literal("jackpot").executes(context -> claimJackpotCommand(context.getSource())))
                .then(Commands.literal("build").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> rebuildCasinoCommand(context.getSource())))
                .then(Commands.literal("rebuild").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> rebuildCasinoCommand(context.getSource())))
                .then(Commands.literal("clear_furniture").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> clearCasinoFurnitureCommand(context.getSource(), 40))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(5, 100))
                                .executes(context -> clearCasinoFurnitureCommand(context.getSource(), IntegerArgumentType.getInteger(context, "radius")))))
                .then(Commands.literal("export").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> exportCasinoFurnitureCommand(context.getSource(), "casino_custom.json", 40))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> exportCasinoFurnitureCommand(context.getSource(), StringArgumentType.getString(context, "name"), 40))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(5, 100))
                                        .executes(context -> exportCasinoFurnitureCommand(context.getSource(), StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context, "radius"))))))
                .then(Commands.literal("import").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> importCasinoFurnitureCommand(context.getSource(), "casino_custom.json"))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> importCasinoFurnitureCommand(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("populate").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> populateCasinoFurnitureCommand(context.getSource()))));

        dispatcher.register(Commands.literal("announcement")
                .requires(s -> hasAdminOrCreatorPermission(s))
                .executes(context -> testAnnouncementCommand(context.getSource(), "mythic"))
                .then(Commands.literal("test").executes(context -> testAnnouncementCommand(context.getSource(), "mythic")))
                .then(Commands.literal("mythic").executes(context -> testAnnouncementCommand(context.getSource(), "mythic")))
                .then(Commands.literal("gold").executes(context -> testAnnouncementCommand(context.getSource(), "gold")))
                .then(Commands.literal("doncella").executes(context -> testAnnouncementCommand(context.getSource(), "doncella")))
                .then(Commands.argument("title", StringArgumentType.string())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> customAnnouncementCommand(context.getSource(),
                                        StringArgumentType.getString(context, "title"),
                                        StringArgumentType.getString(context, "message"))))));

        dispatcher.register(Commands.literal("black_market")
                .executes(context -> warpBlackMarketCommand(context.getSource()))
                .then(Commands.literal("tp").executes(context -> warpBlackMarketCommand(context.getSource())))
                .then(Commands.literal("surface").executes(context -> warpBlackMarketSurfaceCommand(context.getSource())))
                .then(Commands.literal("superficie").executes(context -> warpBlackMarketSurfaceCommand(context.getSource())))
                .then(Commands.literal("locate").executes(context -> locateBlackMarketCommand(context.getSource())))
                .then(Commands.literal("build").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> buildBlackMarketCommand(context.getSource())))
                .then(Commands.literal("open").executes(context -> openBlackMarketCommand(context.getSource())))
                .then(Commands.literal("export").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> exportBlackMarketCommand(context.getSource(), "default", 16))
                        .then(Commands.argument("nombre", StringArgumentType.word())
                                .executes(context -> exportBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"), 16))
                                .then(Commands.argument("radio", IntegerArgumentType.integer(5, 64))
                                        .executes(context -> exportBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"), IntegerArgumentType.getInteger(context, "radio"))))))
                .then(Commands.literal("import").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> importBlackMarketCommand(context.getSource(), "default"))
                        .then(Commands.argument("nombre", StringArgumentType.word())
                                .executes(context -> importBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"))))));

        dispatcher.register(Commands.literal("mercado_negro")
                .executes(context -> warpBlackMarketCommand(context.getSource()))
                .then(Commands.literal("tp").executes(context -> warpBlackMarketCommand(context.getSource())))
                .then(Commands.literal("surface").executes(context -> warpBlackMarketSurfaceCommand(context.getSource())))
                .then(Commands.literal("superficie").executes(context -> warpBlackMarketSurfaceCommand(context.getSource())))
                .then(Commands.literal("locate").executes(context -> locateBlackMarketCommand(context.getSource())))
                .then(Commands.literal("build").requires(s -> hasAdminOrCreatorPermission(s)).executes(context -> buildBlackMarketCommand(context.getSource())))
                .then(Commands.literal("open").executes(context -> openBlackMarketCommand(context.getSource())))
                .then(Commands.literal("export").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> exportBlackMarketCommand(context.getSource(), "default", 16))
                        .then(Commands.argument("nombre", StringArgumentType.word())
                                .executes(context -> exportBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"), 16))
                                .then(Commands.argument("radio", IntegerArgumentType.integer(5, 64))
                                        .executes(context -> exportBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"), IntegerArgumentType.getInteger(context, "radio"))))))
                .then(Commands.literal("import").requires(s -> hasAdminOrCreatorPermission(s))
                        .executes(context -> importBlackMarketCommand(context.getSource(), "default"))
                        .then(Commands.argument("nombre", StringArgumentType.word())
                                .executes(context -> importBlackMarketCommand(context.getSource(), StringArgumentType.getString(context, "nombre"))))));

        dispatcher.register(Commands.literal("black_market_surface")
                .executes(context -> warpBlackMarketSurfaceCommand(context.getSource())));

        dispatcher.register(Commands.literal("mercado_negro_superficie")
                .executes(context -> warpBlackMarketSurfaceCommand(context.getSource())));
    }

    private static int resetBannerCommand(CommandSourceStack source) {
        net.minecraft.server.MinecraftServer server = source.getServer();
        long now = server.overworld().getDayTime();
        DimensionGenSavedData data = DimensionGenSavedData.get(server);
        data.setBannerEpochStartTick(now);
        PromotionalBanner.setBannerEpochTick(now);
        PromotionalBanner.BannerPhase phase1 = PromotionalBanner.getActivePhase(now, 0);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.banners_reset_the_promotional_banner_cycle", String.valueOf(phase1.bannerTitle()), String.valueOf(phase1.franchise().displayName())), true);
        return 1;
    }

    private static int setBannerPhaseCommand(CommandSourceStack source, int phase) {
        net.minecraft.server.MinecraftServer server = source.getServer();
        long now = server.overworld().getDayTime();
        long targetEpoch = now - (long) (phase - 1) * PromotionalBanner.BANNER_CYCLE_TICKS;
        DimensionGenSavedData data = DimensionGenSavedData.get(server);
        data.setBannerEpochStartTick(targetEpoch);
        PromotionalBanner.setBannerEpochTick(targetEpoch);
        PromotionalBanner.BannerPhase activeP = PromotionalBanner.getActivePhase(now, 0);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.phase_changed_set_to_phase_with", String.valueOf(phase), String.valueOf(activeP.bannerTitle()), String.valueOf(activeP.franchise().displayName())), true);
        return 1;
    }

    private static int show(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_level_exp_coins_points_str", String.valueOf(data.level()), String.valueOf(data.experience()), String.valueOf(data.nextLevelExperience()), String.valueOf(data.coins()), String.valueOf(data.attributePoints()), String.valueOf(data.strength()), String.valueOf(data.vitality()), String.valueOf(data.agility()), String.valueOf(data.perception()), String.valueOf(data.intelligence()), String.valueOf(data.willpower())), false);
        return 1;
    }

    private static int showCoins(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("awakening_system.msg.usage_coins_amount_player"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_current_balance_awakening_coins_use", String.valueOf(data.coins())), false);
        return 1;
    }

    private static int giveCoins(CommandSourceStack source, int amount, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.you_must_specify_target_player"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.addCoins(amount);
        SystemEvents.checkAchievements(player, data);
        AwakeningSystemMod.sync(player);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shop.coin_title",
                "awakening_system.notification.shop.coin_message", 0xFFFFD866, amount);
        final ServerPlayer finalPlayer = player;
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_added_coins_to_total_coins", String.valueOf(amount), String.valueOf(finalPlayer.getGameProfile().getName()), String.valueOf(data.coins())), true);
        return 1;
    }

    private static int showCarats(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("awakening_system.msg.usage_carats_amount_player"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.tracen_current_carats_balance", String.valueOf(data.getCarats())), false);
        return 1;
    }

    private static int giveCarats(CommandSourceStack source, int amount, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.you_must_specify_target_player"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.addCarats(amount);
        AwakeningSystemMod.sync(player);
        final ServerPlayer finalPlayer = player;
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.tracen_granted_carats_to_total", String.valueOf(amount), String.valueOf(finalPlayer.getGameProfile().getName()), String.valueOf(data.getCarats())), true);
        return 1;
    }

    private static int giveAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemEvents.grantAll(player);
        SystemData data = SystemEvents.data(player);
        data.recordHabitDamage(10000);
        data.recordHabitSprint(20000, "minecraft:plains");
        data.recordHabitDeepMining(10000);
        for (int i = 0; i < 5000; i++) data.recordHabitWeaponHit();
        data.recordHabitFocusTicks(7500);
        data.addCoins(1000000);
        var keyItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("awakening_system", "dungeon_key"));
        if (keyItem != null && keyItem != net.minecraft.world.item.Items.AIR) {
            player.getInventory().add(new ItemStack(keyItem, 16));
        }
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_full_unlock_complete_level_skills", String.valueOf(data.level()), String.valueOf(data.skills().size())), true);
        return 1;
    }

    private static int showHabits(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_habito_constancia"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.cuerpo_templado_niv_dano_resistido", String.valueOf(data.skills().getOrDefault("enduring_flesh", 0)), String.valueOf(data.habitDamageTaken()), String.valueOf(data.getHabitRequired("enduring_flesh"))), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.paso_del_viento_niv_esprint_biomas", String.valueOf(data.skills().getOrDefault("windrunner", 0)), String.valueOf((int)data.habitSprintDistance()), String.valueOf(data.getHabitRequired("windrunner")), String.valueOf(data.habitBiomesExplored().size())), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.maestria_del_acero_niv_golpes_de", String.valueOf(data.skills().getOrDefault("combat_instinct", 0)), String.valueOf(data.habitWeaponHits()), String.valueOf(data.getHabitRequired("combat_instinct"))), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.instinto_de_la_tierra_niv_bloques", String.valueOf(data.skills().getOrDefault("ore_sense", 0)), String.valueOf(data.habitDeepBlocksMined()), String.valueOf(data.getHabitRequired("ore_sense"))), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.mirada_del_cazador_niv_enfoque_punteria", String.valueOf(data.skills().getOrDefault("deadeye", 0)), String.valueOf(data.habitFocusTicks()), String.valueOf(data.getHabitRequired("deadeye"))), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.las_habilidades_de_habito_son_innatas"), false);
        return 1;
    }

    private static int evaluateHunterCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cEste comando solo puede ser ejecutado por un jugador."));
            return 0;
        }

        SystemData data = SystemEvents.data(player);
        if (data == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.no_se_pudo_acceder_los_datos"));
            return 0;
        }

        if (!data.isEvaluationPending() && data.level() < data.maxAllowedLevelForEvaluatedRank()) {
            player.sendSystemMessage(Component.translatable("awakening_system.msg.asociacion_de_cazadores_no_tienes_ninguna", String.valueOf(data.rank()), String.valueOf(data.maxAllowedLevelForEvaluatedRank())));
            return 0;
        }

        String prevRank = data.rank();
        int prevLevel = data.level();
        int banked = data.reserveExperience();

        if (data.passEvaluation()) {
            AwakeningSystemMod.sync(player);

            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

            player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.2, player.getZ(), 50, 0.8, 0.8, 0.8, 0.2);
            player.serverLevel().sendParticles(ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1.5, player.getZ(), 30, 0.6, 0.6, 0.6, 0.1);

            player.sendSystemMessage(Component.literal("§6§l╔═══════════════════════════════════════════════╗"));
            player.sendSystemMessage(Component.translatable("awakening_system.msg.evaluacion_de_rango_completada"));
            player.sendSystemMessage(Component.literal("§6§l╠═══════════════════════════════════════════════╣"));
            player.sendSystemMessage(Component.translatable("awakening_system.msg.rango_anterior_nuevo_rango", String.valueOf(prevRank), String.valueOf(data.rank())));
            player.sendSystemMessage(Component.translatable("awakening_system.msg.puntos_de_atributo_otorgados_por_ascender"));
            if (banked > 0) {
                player.sendSystemMessage(Component.translatable("awakening_system.msg.se_han_transferido_exp_en_reserva", String.valueOf(String.format(java.util.Locale.US, "%,d", banked))));
                if (data.level() > prevLevel) {
                    player.sendSystemMessage(Component.translatable("awakening_system.msg.has_subido_al_nivel", String.valueOf(data.level())));
                }
            }
            player.sendSystemMessage(Component.translatable("awakening_system.msg.proximo_umbral_de_evaluacion_nivel", String.valueOf(data.maxAllowedLevelForEvaluatedRank())));
            player.sendSystemMessage(Component.literal("§6§l╚═══════════════════════════════════════════════╝"));

            return 1;
        } else {
            player.sendSystemMessage(Component.translatable("awakening_system.msg.sistema_no_se_pudo_procesar_la"));
            return 0;
        }
    }

    private static int summonEvaluatorCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.entity.SeriesNpcEntity evaluator = new com.soybe.awakeningsystem.entity.SeriesNpcEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.HUNTER_EVALUATOR.get(),
                    player.level(),
                    com.soybe.awakeningsystem.entity.NpcProfile.HUNTER_EVALUATOR
            );
            evaluator.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            player.level().addFreshEntity(evaluator);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_inspector_de_la_asociacion_invocado"), true);
            return 1;
        }
        return 0;
    }

    /**
     * Puts the player in a class right away: a base class (with its starting skill) or an evolution (the trial
     * counts as passed). What was learnt in other classes' webs stays, so going back keeps it.
     */
    private static int setClassCommand(CommandSourceStack source, ServerPlayer target, String cls) {
        String id = cls.toLowerCase(java.util.Locale.ROOT);
        boolean known = false;
        for (ClassEvolution.Base b : ClassEvolution.bases()) {
            if (b.id().equals(id) || b.evolutions().contains(id)) known = true;
        }
        if (!known) {
            source.sendFailure(Component.translatable("awakening_system.command.setclass.unknown", cls));
            return 0;
        }
        SystemData data = SystemEvents.data(target);
        data.setHunterClass(id);
        if (!ClassEvolution.isBase(id)) data.setEvolutionTrialPassed(true);
        SystemEvents.grantClassStart(data, id);
        SystemAttributeEffects.apply(target);
        AwakeningSystemMod.sync(target);
        source.sendSuccess(() -> Component.translatable("awakening_system.command.setclass.done", target.getDisplayName(), id), true);
        return 1;
    }

    private static int resetPlayer(CommandSourceStack source, ServerPlayer target) {
        target.setData(AwakeningSystemMod.PLAYER_SYSTEM, new SystemData());
        SystemEvents.dismissAllShadows(target);
        SystemAttributeEffects.apply(target);
        target.setHealth(Math.min(target.getHealth(), target.getMaxHealth()));
        AwakeningSystemMod.sync(target);

        target.displayClientMessage(Component.translatable("awakening_system.command.reset_success"), true);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_all_progress_has_been_reset", String.valueOf(target.getGameProfile().getName())), true);
        return 1;
    }

    private static int addAttribute(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        if (!SystemEvents.spendAttribute(player, name)) {
            source.sendFailure(Component.translatable("awakening_system.msg.invalid_attribute_or_no_points_available"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.attribute_upgraded", String.valueOf(name)), false);
        return 1;
    }

    private static int inspect(CommandSourceStack source, ServerPlayer target) {
        SystemData data = SystemEvents.data(target);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.inspect", String.valueOf(target.getGameProfile().getName())).withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.level_prestige_coins", String.valueOf(data.level()), String.valueOf(data.prestigeLevel()), String.valueOf(data.coins())).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.str_vit_agi_per_int_wil", String.valueOf(data.strength()), String.valueOf(data.vitality()), String.valueOf(data.agility()), String.valueOf(data.perception()), String.valueOf(data.intelligence()), String.valueOf(data.willpower())).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.defeated_mobs_bosses_completed_quests", String.valueOf(data.creaturesDefeated()), String.valueOf(data.bossKills()), String.valueOf(data.missionsCompleted())).withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.skills", String.valueOf(data.skills().size()), String.valueOf(data.skills().keySet())).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private static int showTop(CommandSourceStack source) {
        if (source.getServer() == null) return 0;
        var players = new java.util.ArrayList<>(source.getServer().getPlayerList().getPlayers());
        players.sort((a, b) -> {
            SystemData da = SystemEvents.data(a);
            SystemData db = SystemEvents.data(b);
            int pCmp = Integer.compare(db.prestigeLevel(), da.prestigeLevel());
            return pCmp != 0 ? pCmp : Integer.compare(db.level(), da.level());
        });

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_leaderboard").withStyle(ChatFormatting.GOLD), false);
        int rank = 1;
        for (ServerPlayer player : players) {
            if (rank > 5) break;
            SystemData d = SystemEvents.data(player);
            final int pos = rank;
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.lvl_prestige_mobs", String.valueOf(pos), String.valueOf(player.getGameProfile().getName()), String.valueOf(d.level()), String.valueOf(d.prestigeLevel()), String.valueOf(d.creaturesDefeated())).withStyle(pos == 1 ? ChatFormatting.YELLOW : ChatFormatting.AQUA), false);
            rank++;
        }
        return 1;
    }

    private static int triggerTrance(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.triggerTranceDirect(200);
        AwakeningSystemMod.notify(player, "awakening_system.notification.trance.title",
                "awakening_system.notification.trance.message", 0xFFFF5555);
        player.playNotifySound(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.8F);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.trance_mode_activated_for_10_seconds").withStyle(ChatFormatting.RED), false);
        return 1;
    }

    private static int setCombo(CommandSourceStack source, int count) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setComboCount(count);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.combo_counter_set_to", String.valueOf(count)), false);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setLevel(level);
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.level_level_set_to_attribute_points", String.valueOf(level)), false);
        return 1;
    }

    private static int setSponsor(CommandSourceStack source, String constellationId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        if (!SystemEvents.selectConstellation(player, constellationId)) {
            source.sendFailure(Component.translatable("awakening_system.msg.invalid_constellation_id_or_level_below"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.constellation_sponsor_assigned", String.valueOf(constellationId)), false);
        return 1;
    }

    private static int setScenario(CommandSourceStack source, String scenarioId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        int target = switch (scenarioId) {
            case "depth_expedition" -> 15;
            case "abyssal_conquest" -> 1;
            case "survival_trial" -> 20;
            default -> 10;
        };
        data.setScenarioDirect(scenarioId, target);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.scenario_active_scenario_set_to_target", String.valueOf(scenarioId), String.valueOf(target)), false);
        return 1;
    }

    private static int completeScenario(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.completeScenarioDirect();
        SystemEvents.announceScenarioComplete(player, data);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.scenario_scenario_completed_open_the_menu"), false);
        return 1;
    }

    private static int learnSkill(CommandSourceStack source, String skillId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.skills().put(skillId, 1);
        data.gainSkillProgress(skillId, 1);
        SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
        AwakeningSystemMod.notify(player, "awakening_system.notification.skill_assimilated.title",
                "awakening_system.notification.skill_assimilated.message", 0xFFBB7DFF,
                def != null ? def.nameKey() : skillId);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.skill_skill_learned", String.valueOf((def != null ? def.nameKey() : skillId))), false);
        return 1;
    }

    private static int triggerDungeon(CommandSourceStack source, String rankOrTheme, String optionalTheme) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        String rank = "D";
        String theme = (optionalTheme != null && !optionalTheme.isEmpty()) ? optionalTheme : "crypt";
        boolean isRed = false;

        String lower = rankOrTheme != null ? rankOrTheme.toLowerCase(java.util.Locale.ROOT) : "d";
        if ("igris".equals(lower)) {
            rank = "S";
            theme = "igris";
        } else if ("baran".equals(lower)) {
            rank = "NATIONAL";
            theme = "baran";
        } else if ("red".equals(lower) || "redgate".equals(lower) || "rojo".equals(lower)) {
            rank = "S";
            theme = "igris";
            isRed = true;
        } else {
            rank = rankOrTheme.toUpperCase(java.util.Locale.ROOT);
            if (optionalTheme != null && !optionalTheme.isEmpty()) {
                theme = optionalTheme.toLowerCase(java.util.Locale.ROOT);
            }
        }

        DungeonManager.startDungeon(player, rank, theme, isRed);
        final String finalRank = rank;
        final String finalTheme = theme;
        final boolean finalRed = isRed;
        source.sendSuccess(() -> Component.translatable("awakening_system.command.dungeon.entering", finalRank, finalTheme, finalRed ? Component.translatable("awakening_system.command.dungeon.red_tag").getString() : ""), false);
        return 1;
    }

    private static int completeAwakeningCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        if (!data.isAwakened()) data.awakenWithPotential(data.potentialMeasured() ? data.potentialRank() : "F");
        data.addCoins(100);
        SystemEvents.awardExperience(player, data, 250);
        AwakeningSystemMod.notify(player, "awakening_system.notification.awakening_complete.title",
                "awakening_system.notification.awakening_complete.message", 0xFFFFD866);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.awakening_system_awakening_quest_completed"), false);
        return 1;
    }

    private static int openSponsorSelection(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenSponsorScreenPayload(false));
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.sponsorship_opening_constellation_selection_screen"), false);
        return 1;
    }

    private static int listUniqueRanks(CommandSourceStack source) {
        var server = source.getServer();
        UniqueRankManager mgr = UniqueRankManager.get(server);
        source.sendSuccess(() -> Component.translatable("awakening_system.command.unique.titles_header"), false);
        for (String tId : UniqueRankManager.UNIQUE_TITLES) {
            String holder = mgr.getTitleHolderName(tId);
            String status = holder != null ? "§c[CLAIMED: " + holder + "]" : "§a[AVAILABLE]";
            source.sendSuccess(() -> Component.literal("§e• " + tId + " §7-> " + status), false);
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.command.unique.skills_header"), false);
        for (String sId : UniqueRankManager.UNIQUE_SKILLS) {
            String holder = mgr.getSkillHolderName(sId);
            String status = holder != null ? "§c[CLAIMED: " + holder + "]" : "§a[AVAILABLE]";
            source.sendSuccess(() -> Component.literal("§b• " + sId + " §7-> " + status), false);
        }
        return 1;
    }

    private static int claimUnique(CommandSourceStack source, String id) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        var server = source.getServer();
        UniqueRankManager mgr = UniqueRankManager.get(server);
        if (UniqueRankManager.isUniqueTitle(id)) {
            if (mgr.claimTitle(id, player)) {
                SystemData data = SystemEvents.data(player);
                data.equipTitle(id);
                SystemAttributeEffects.apply(player);
                AwakeningSystemMod.sync(player);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.unique.title_claimed", id), false);
                return 1;
            } else {
                String holder = mgr.getTitleHolderName(id);
                source.sendFailure(Component.translatable("awakening_system.command.unique.title_held", id, (holder != null ? holder : "Unknown")));
                return 0;
            }
        } else if (UniqueRankManager.isUniqueSkill(id)) {
            if (mgr.claimSkill(id, player)) {
                SystemData data = SystemEvents.data(player);
                data.skills().put(id, 6);
                data.setEquippedSkill(id);
                AwakeningSystemMod.sync(player);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.unique.skill_claimed", id), false);
                return 1;
            } else {
                String holder = mgr.getSkillHolderName(id);
                source.sendFailure(Component.translatable("awakening_system.command.unique.skill_held", id, (holder != null ? holder : "Unknown")));
                return 0;
            }
        } else {
            source.sendFailure(Component.translatable("awakening_system.command.unique.invalid", id));
            return 0;
        }
    }

    private static int openGuide(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OpenGuidebookPayload());
            source.sendSuccess(() -> Component.translatable("awakening_system.command.guide.opening"), false);
            return 1;
        }
        return 0;
    }

    private static int openBestiary(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OpenBestiaryPayload());
            source.sendSuccess(() -> Component.translatable("awakening_system.command.bestiary.opening"), false);
            return 1;
        }
        return 0;
    }

    private static int ascendConstellation(CommandSourceStack source, ServerPlayer target, String customModifier) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.specify_target_player_system_constellation_ascend"));
            return 0;
        }
        final ServerPlayer finalP = player;
        ConstellationAscension.triggerAscension(finalP, "System Administrative Command",
                (customModifier == null || customModifier.isBlank()) ? null : customModifier.trim());
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_player_has_ascended_to", String.valueOf(finalP.getGameProfile().getName())), true);
        return 1;
    }

    private static int demoteConstellation(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.specify_target_player_system_constellation_demote"));
            return 0;
        }
        final ServerPlayer finalP = player;
        SystemData data = SystemEvents.data(finalP);
        data.setIsConstellation(false);
        data.setConstellationModifier("");
        AwakeningSystemMod.sync(finalP);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_constellation_status_of_has", String.valueOf(finalP.getGameProfile().getName())), true);
        finalP.displayClientMessage(Component.translatable("awakening_system.command.constellation_reverted"), true);
        return 1;
    }

    private static int renameConstellation(CommandSourceStack source, String modifier) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        if (!data.isConstellation()) {
            source.sendFailure(Component.translatable("awakening_system.msg.you_are_not_constellation_you_must"));
            return 0;
        }
        String clean = modifier.trim().replace("[", "").replace("]", "");
        data.setConstellationModifier(clean);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_your_true_name_as", String.valueOf(clean)), false);
        return 1;
    }

    private static int returnFromAstral(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        if (!ConstellationChannelHandler.isAstralWatching(player.getUUID())) {
            source.sendFailure(Component.translatable("awakening_system.msg.you_are_not_in_astral_projection"));
            return 0;
        }
        ConstellationChannelHandler.returnFromAstral(player);
        return 1;
    }

    private static int spawnStructureCommand(CommandSourceStack source, String name) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        if ("abyss".equalsIgnoreCase(name) || "abyss_portal".equalsIgnoreCase(name) || "spiral_abyss".equalsIgnoreCase(name) || "teyvat_portal".equalsIgnoreCase(name)) {
            Direction dir = (source.getEntity() instanceof ServerPlayer sp) ? sp.getDirection() : Direction.SOUTH;
            com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, pos, dir, com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive());
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_musk_reef_and_spiral_abyss", String.valueOf(pos.toShortString())), true);
            return 1;
        }
        boolean ok = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.generateStructure(level, pos, name);
        if (ok) {
            com.soybe.awakeningsystem.world.SeriesStructureWorldGen.checkAndEnsureNpcsPresent(level.getServer());
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_themed_structure_generated_at", String.valueOf(name), String.valueOf(pos.toShortString())), true);
            return 1;
        }

        boolean teyvatOk = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(level, pos, name);
        if (teyvatOk) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_teyvat_structure_successfully_generated_at", String.valueOf(name), String.valueOf(pos.toShortString())), true);
            return 1;
        }

        source.sendFailure(Component.translatable("awakening_system.msg.unknown_structure_type_options_teyvat_golden"));
        return 0;
    }

    private static int spawnTeyvatStructureCommand(CommandSourceStack source, String name) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        boolean ok = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(level, pos, name);
        if (ok) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_structure_successfully_generated_at", String.valueOf(name), String.valueOf(pos.toShortString())), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.unrecognized_teyvat_structure_options_golden_house"));
            return 0;
        }
    }

    record TeyvatLocateTarget(String id, String displayName, BlockPos canonicalPos, String cmd) {
        /** The reef moves out to sea in new worlds: its place is always read now, not when the list was made. */
        @Override
        public BlockPos canonicalPos() {
            return "abyss".equals(id) ? TeyvatWorldManager.MUSK_REEF_ISLAND_POS : canonicalPos;
        }
    }

    private static final java.util.List<TeyvatLocateTarget> TEYVAT_CANONICAL_TARGETS = java.util.List.of(
            new TeyvatLocateTarget("golden_house", "The Golden House (Tartaglia / Childe Boss)", TeyvatWorldManager.GOLDEN_HOUSE_POS.offset(0, 7, 4), "golden_house"),
            new TeyvatLocateTarget("tenshukaku", "Tenshukaku Palace (Raiden Shogun Boss)", TeyvatWorldManager.TENSHUKAKU_POS, "tenshukaku"),
            new TeyvatLocateTarget("wolflord", "Golden Wolflord Domain (Enkanomiya)", TeyvatWorldManager.WOLFLORD_POS, "wolflord"),
            new TeyvatLocateTarget("azhdaha", "Azhdaha's Underground Domain", TeyvatWorldManager.AZHDAHA_POS, "azhdaha"),
            new TeyvatLocateTarget("windrise", "Great Sacred Oak of Windrise", TeyvatWorldManager.WINDRISE_POS, "windrise"),
            new TeyvatLocateTarget("jade_chamber", "Floating Jade Chamber", TeyvatWorldManager.JADE_CHAMBER_POS, "jade_chamber"),
            new TeyvatLocateTarget("dawn_winery", "Dawn Winery", TeyvatWorldManager.WINERY_POS, "dawn_winery"),
            new TeyvatLocateTarget("beidou_ship", "The Alcor (Beidou's Crux Flagship)", TeyvatWorldManager.ALCOR_POS, "alcor"),
            new TeyvatLocateTarget("anemo_hypostasis", "Anemo Hypostasis Arena (Beth)", TeyvatWorldManager.ANEMO_HYPOSTASIS_POS, "anemo_hypostasis"),
            new TeyvatLocateTarget("electro_hypostasis", "Electro Hypostasis Arena (Aleph)", TeyvatWorldManager.ELECTRO_HYPOSTASIS_POS, "electro_hypostasis"),
            new TeyvatLocateTarget("geo_hypostasis", "Geo Hypostasis Arena (Gimel)", TeyvatWorldManager.GEO_HYPOSTASIS_POS, "geo_hypostasis"),
            new TeyvatLocateTarget("cryo_hypostasis", "Cryo Hypostasis Arena (Daleth)", TeyvatWorldManager.CRYO_HYPOSTASIS_POS, "cryo_hypostasis"),
            new TeyvatLocateTarget("pyro_hypostasis", "Pyro Hypostasis Arena (Ayin)", TeyvatWorldManager.PYRO_HYPOSTASIS_POS, "pyro_hypostasis"),
            new TeyvatLocateTarget("hydro_hypostasis", "Hydro Hypostasis Arena (He)", TeyvatWorldManager.HYDRO_HYPOSTASIS_POS, "hydro_hypostasis"),
            new TeyvatLocateTarget("cryo_regisvine", "Cryo Regisvine Arena", TeyvatWorldManager.CRYO_REGISVINE_POS, "cryo_regisvine"),
            new TeyvatLocateTarget("abyss", "Spiral Abyss (Musk Reef)", TeyvatWorldManager.MUSK_REEF_ISLAND_POS, "abyss"),
            new TeyvatLocateTarget("mondstadt", "Mondstadt (Favonius Citadel)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.MONDSTADT_POS, "mondstadt"),
            new TeyvatLocateTarget("liyue", "Liyue (Yujing Terrace)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.LIYUE_POS, "liyue"),
            new TeyvatLocateTarget("inazuma", "Inazuma (Tenshukaku)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.INAZUMA_POS, "inazuma"),
            new TeyvatLocateTarget("sumeru", "Sumeru (The Great Academiya)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.SUMERU_POS, "sumeru"),
            new TeyvatLocateTarget("fontaine", "Fontaine (Palais Mermonia)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.FONTAINE_POS, "fontaine")
    );

    private static int locateTeyvatStructureCommand(CommandSourceStack source, String filter) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        MinecraftServer server = level.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded_travel"));
            return 0;
        }

        BlockPos origin = BlockPos.containing(source.getPosition());
        String q = (filter == null) ? "" : filter.toLowerCase(java.util.Locale.ROOT).trim();
        var tData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);

        TeyvatLocateTarget matchedTarget = null;
        if (!q.isEmpty()) {
            for (var target : TEYVAT_CANONICAL_TARGETS) {
                if (target.id().equalsIgnoreCase(q) || target.cmd().equalsIgnoreCase(q)) {
                    matchedTarget = target;
                    break;
                }
                String dLow = target.displayName().toLowerCase(java.util.Locale.ROOT);
                if (dLow.contains(q)) {
                    matchedTarget = target;
                    break;
                }
                if ((q.contains("childe") || q.contains("tartaglia") || q.contains("golden_house") || q.contains("dorada")) && target.id().equals("golden_house")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("raiden") || q.contains("shogun") || q.contains("baal") || q.contains("tenshukaku") || q.contains("inazuma")) && target.id().equals("tenshukaku")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("wolflord") || q.contains("acechador") || q.contains("enkanomiya")) && target.id().equals("wolflord")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("azhdaha") || q.contains("dragon")) && target.id().equals("azhdaha")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("windrise") || q.contains("roble") || q.contains("arbol")) && target.id().equals("windrise")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("jade") || q.contains("camara")) && target.id().equals("jade_chamber")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("winery") || q.contains("vinedo")) && target.id().equals("dawn_winery")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("alcor") || q.contains("beidou") || q.contains("barco")) && target.id().equals("beidou_ship")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("anemo_cube") || q.contains("anemo_hypo")) && target.id().equals("anemo_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("electro_cube") || q.contains("electro_hypo")) && target.id().equals("electro_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("geo_cube") || q.contains("geo_hypo")) && target.id().equals("geo_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("cryo_cube") || q.contains("cryo_hypo")) && target.id().equals("cryo_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("pyro_cube") || q.contains("pyro_hypo")) && target.id().equals("pyro_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("hydro_cube") || q.contains("hydro_hypo")) && target.id().equals("hydro_hypostasis")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("regisvid") || q.contains("regisvine")) && target.id().equals("cryo_regisvine")) {
                    matchedTarget = target; break;
                }
                if ((q.contains("abyss") || q.contains("espiral") || q.contains("musk")) && target.id().equals("abyss")) {
                    matchedTarget = target; break;
                }
            }
        }

        if (matchedTarget != null) {
            final TeyvatLocateTarget finalTarget = matchedTarget;
            BlockPos actualPos = tData.getNamedStructure(finalTarget.id());
            boolean alreadyBuilt = (actualPos != null);
            BlockPos targetPos = alreadyBuilt ? actualPos : finalTarget.canonicalPos();
            int dist = (int) Math.sqrt(targetPos.distSqr(origin));

            var warpBtn = Component.translatable("awakening_system.msg.travel_enter")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GOLD)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/teyvat " + finalTarget.cmd()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("awakening_system.msg.click_to", String.valueOf((alreadyBuilt ? "travel" : "build and enter")), String.valueOf(finalTarget.displayName())))));

            var msg = Component.translatable("awakening_system.msg.teyvat_structure_coordinates", String.valueOf(finalTarget.displayName()), String.valueOf(targetPos.getX()), String.valueOf(targetPos.getY()), String.valueOf(targetPos.getZ()), String.valueOf(dist), String.valueOf((alreadyBuilt ? "§2✔ [Built]" : "§6★ [Canonical Site]")))
                    .append(warpBtn);

            source.sendSuccess(() -> msg, false);
            return 1;
        }

        for (var entry : tData.namedStructures.entrySet()) {
            if (!q.isEmpty() && entry.getKey().contains(q)) {
                BlockPos p = entry.getValue();
                int dist = (int) Math.sqrt(p.distSqr(origin));
                var tpBtn = Component.translatable("awakening_system.msg.teleport")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true)
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                        "/tp @s " + p.getX() + " " + (p.getY() + 1) + " " + p.getZ()))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("awakening_system.msg.click_to_teleport"))));
                var msg = Component.translatable("awakening_system.msg.teyvat_registered_structure_at", String.valueOf(entry.getKey()), String.valueOf(p.getX()), String.valueOf(p.getY()), String.valueOf(p.getZ()), String.valueOf(dist)).append(tpBtn);
                source.sendSuccess(() -> msg, false);
                return 1;
            }
        }

        java.util.List<BlockPos> featureList = null;
        String featureLabel = null;
        if (q.contains("shrine") || q.contains("santuario")) {
            featureList = tData.shrinesOfDepths;
            featureLabel = "Shrine of Depths";
        } else if (q.contains("statue") || q.contains("estatua")) {
            featureList = tData.statues;
            featureLabel = "Statue of the Seven";
        } else if (q.contains("waypoint") || q.contains("tp") || q.contains("teleport")) {
            featureList = tData.waypoints;
            featureLabel = "Teleport Waypoint";
        } else if (q.contains("camp") || q.contains("hilichurl") || q.contains("campamento")) {
            featureList = tData.camps;
            featureLabel = "Hilichurl Camp";
        }

        if (featureList != null) {
            final String finalFeatureLabel = featureLabel;
            if (featureList.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_no_have_been_discovered_on", String.valueOf(finalFeatureLabel)), false);
                return 1;
            }
            BlockPos nearest = null;
            double bestD = Double.MAX_VALUE;
            for (BlockPos p : featureList) {
                double d = p.distSqr(origin);
                if (d < bestD) {
                    bestD = d;
                    nearest = p;
                }
            }
            final BlockPos fp = nearest;
            int dist = (int) Math.sqrt(bestD);
            var tpBtn = Component.translatable("awakening_system.msg.teleport")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.YELLOW)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/tp @s " + fp.getX() + " " + (fp.getY() + 1) + " " + fp.getZ()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("awakening_system.msg.click_to_teleport_to", String.valueOf(finalFeatureLabel)))));
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_nearest_at", String.valueOf(finalFeatureLabel), String.valueOf(fp.getX()), String.valueOf(fp.getY()), String.valueOf(fp.getZ()), String.valueOf(dist)).append(tpBtn), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_no_structure_found_matching_term", String.valueOf(q)), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.use_teyvat_list_to_view_the"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.tartaglia_childe_teyvat_golden_house"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.raiden_shogun_teyvat_tenshukaku"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.golden_wolflord_teyvat_wolflord"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.azhdaha_dragon_teyvat_azhdaha"), false);
        return 1;
    }

    private static int listTeyvatStructuresCommand(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        MinecraftServer server = level.getServer();
        var tData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        BlockPos origin = BlockPos.containing(source.getPosition());

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_structure_catalog"), false);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.weekly_bosses_and_authentic_palaces"), false);
        sendLocateEntry(source, origin, "🏰 The Golden House (Tartaglia / Childe Boss)", "golden_house", TeyvatWorldManager.GOLDEN_HOUSE_POS.offset(0, 7, 4), tData);
        sendLocateEntry(source, origin, "⚡ Tenshukaku Palace (Raiden Shogun Boss)", "tenshukaku", TeyvatWorldManager.TENSHUKAKU_POS, tData);
        sendLocateEntry(source, origin, "🐺 Enkanomiya Island (Golden Wolflord)", "wolflord", TeyvatWorldManager.WOLFLORD_POS, tData);
        sendLocateEntry(source, origin, "🐉 Azhdaha's Underground Domain", "azhdaha", TeyvatWorldManager.AZHDAHA_POS, tData);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.elemental_hypostases_and_regisvine"), false);
        sendLocateEntry(source, origin, "🍃 Anemo Hypostasis (Beth)", "anemo_hypostasis", TeyvatWorldManager.ANEMO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "⚡ Electro Hypostasis (Aleph)", "electro_hypostasis", TeyvatWorldManager.ELECTRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔶 Geo Hypostasis (Gimel)", "geo_hypostasis", TeyvatWorldManager.GEO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Cryo Hypostasis (Daleth)", "cryo_hypostasis", TeyvatWorldManager.CRYO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔥 Pyro Hypostasis (Ayin)", "pyro_hypostasis", TeyvatWorldManager.PYRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "💧 Hydro Hypostasis (He)", "hydro_hypostasis", TeyvatWorldManager.HYDRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Cryo Regisvine", "cryo_regisvine", TeyvatWorldManager.CRYO_REGISVINE_POS, tData);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.monuments_and_nation_seats"), false);
        sendLocateEntry(source, origin, "🌳 Great Sacred Oak of Windrise", "windrise", TeyvatWorldManager.WINDRISE_POS, tData);
        sendLocateEntry(source, origin, "🏯 Floating Jade Chamber", "jade_chamber", TeyvatWorldManager.JADE_CHAMBER_POS, tData);
        sendLocateEntry(source, origin, "🍷 Dawn Winery", "dawn_winery", TeyvatWorldManager.WINERY_POS, tData);
        sendLocateEntry(source, origin, "⛵ The Alcor (Beidou's Flagship)", "alcor", TeyvatWorldManager.ALCOR_POS, tData);
        sendLocateEntry(source, origin, "🌀 Spiral Abyss (Musk Reef)", "abyss", TeyvatWorldManager.MUSK_REEF_ISLAND_POS, tData);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.exploration_and_resources"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.shrines_of_depths_statues_of_the", String.valueOf(tData.shrinesOfDepths.size()), String.valueOf(tData.statues.size()), String.valueOf(tData.waypoints.size()), String.valueOf(tData.camps.size())), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.use_teyvat_name_to_travel_directly"), false);
        source.sendSuccess(() -> Component.literal("§b§l=================================================================="), false);
        return 1;
    }

    private static void sendLocateEntry(CommandSourceStack source, BlockPos origin, String displayName, String cmdKey, BlockPos canonicalPos, com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData tData) {
        BlockPos actual = tData.getNamedStructure(cmdKey);
        boolean built = (actual != null);
        BlockPos p = built ? actual : canonicalPos;
        int dist = (int) Math.sqrt(p.distSqr(origin));

        var btn = Component.literal(built ? " §e§l[TRAVEL]" : " §6§l[ENTER/GENERATE]")
                .withStyle(style -> style
                        .withColor(built ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                "/teyvat " + cmdKey))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("awakening_system.msg.click_to_travel_to", String.valueOf(displayName), String.valueOf((built ? "" : " (Will materialize instantly)"))))));

        source.sendSuccess(() -> Component.literal("  §f" + displayName + " §7en §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §8(" + dist + "m) " + (built ? "§2✔" : "§7○")).append(btn), false);
    }

    private static int buildAbyssPortalCommand(CommandSourceStack source, String modeArg) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        Direction dir = (source.getEntity() instanceof ServerPlayer sp) ? sp.getDirection() : Direction.SOUTH;

        if ("island".equalsIgnoreCase(modeArg)) {
            com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, pos, dir, com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive());
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_musk_reef_island_and_spiral", String.valueOf(pos.toShortString())), true);
            return 1;
        }

        boolean active = "active".equalsIgnoreCase(modeArg) || ("locked".equalsIgnoreCase(modeArg) ? false : com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive());

        var buildMode = "flat".equalsIgnoreCase(modeArg)
                ? com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.BuildMode.FLAT
                : com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.BuildMode.FULL_3D;

        var centerMode = "nether".equalsIgnoreCase(modeArg)
                ? com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.CenterMode.NETHER
                : ("glass".equalsIgnoreCase(modeArg)
                ? com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.CenterMode.GLASS
                : com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.CenterMode.AIR);

        com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.build(level, pos, dir, buildMode, centerMode, active);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_spiral_abyss_monument_13x13_successfully", String.valueOf(pos.toShortString()), String.valueOf((active ? "§bACTIVE§a" : "§cLOCKED§a"))), true);
        return 1;
    }

    private static int warpAbyss(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.TeyvatWorldManager.teleportToAbyss(player);
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can teleport to the Spiral Abyss."));
        return 0;
    }

    private static int warpAbyssCave(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can teleport to the abyssal cave."));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teleporting_to_the_overworld_great_abyssal"), false);
        com.soybe.awakeningsystem.world.AbyssalCaveBuilder.teleportToAbyssalCave(player);
        return 1;
    }

    private static int openAbyssMenu(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssMenuFromCommand(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.spiral_abyss_opening_interface"), false);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can open the Spiral Abyss interface."));
        return 0;
    }

    private static int advanceAbyssFloor(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleAdvanceOrNextFloor(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can advance in the Spiral Abyss."));
        return 0;
    }

    private static int leaveAbyssCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleLeaveAbyss(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can leave the Spiral Abyss."));
        return 0;
    }

    private static int setAbyssStateCommand(CommandSourceStack source, boolean active) {
        var server = source.getServer();
        com.soybe.awakeningsystem.TeyvatWorldManager.setAbyssActive(server, active);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.spiral_abyss_status_updated_to", String.valueOf((active ? "§bACTIVE (Cosmic Vortex OPEN)" : "§cLOCKED (Portal SEALED)"))), true);
        return 1;
    }

    private static int toggleAbyssStateCommand(CommandSourceStack source) {
        boolean now = !com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive();
        return setAbyssStateCommand(source, now);
    }

    private static int rebuildAbyssCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat != null) {
            com.soybe.awakeningsystem.TeyvatWorldManager.forceRebuildStructures(teyvat);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.spiral_abyss_musk_reef_island_and"), true);
            return 1;
        }
        source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
        return 0;
    }

    public static final java.util.List<String> ALL_STRUCTURE_NAMES = java.util.List.of(
            "solo_leveling", "jinwoo", "hunter_office",
            "orv", "dokja", "subway", "orv_subway_carriage",
            "estate", "lloyd", "frontera_estate",
            "nano", "demon_pagoda",
            "tbate", "arthur", "aether_sanctuary",
            "ranker", "ranker_tower",
            "mount_hua", "chung_myung", "mount_hua_pavilion",
            "valter", "valter_camp",
            "tower_of_god", "shinsoo_shrine",
            "adventurers_guild",
            "black_market_den",
            "nine_tribulations_pagoda", "pagoda_arena", "pagoda_arena_stage_1",
            "yggdrasil_root_labyrinth",
            "ancestral_immortal_grotto",
            "elemental_floating_castles",
            "hunter_expedition_camp",
            "ruined_stargazer_outpost",
            "ancient_lore_stele",
            "ancient_alchemy_pavilion",
            "taoist_bamboo_shrine",
            "blood_sect_fortress",
            "spirit_valley",
            "dragon_sanctuary",
            "roulette_pavilion",
            "goddess_shrine",
            "ascension_pavilion",
            "anemo_shrine", "anemo_shrine_of_depths_final",
            "geo_shrine", "geo_shrine_of_depths_final",
            "electro_shrine", "electro_shrine_of_depths_final",
            "dendro_shrine", "dendro_shrine_of_depths_final",
            "plains_shrine", "desert_shrine", "mountain_shrine",
            "mora_temple", "interastral_bank", "hilichurl_camp",
            "chasm_mine", "overworld_miniboss_arena",
            "dawn_winery", "jade_chamber", "windrise_giant_tree",
            "beidou_ship", "grand_casino", "racetrack", "golden_house", "tenshukaku",
            "witch_island", "teyvat_treehouses", "abyssal_grotto", "forgotten_catacombs"
    );

    private static int tpStructureCommand(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cOnly players can teleport."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        var server = source.getServer();
        String target = name.toLowerCase(java.util.Locale.ROOT).trim();
        String canonical = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.canonicalizeStructureName(target);
        net.minecraft.resources.ResourceKey<Level> targetDim = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getTargetDimensionForStructure(canonical);
        ServerLevel resolvedTargetLevel = server.getLevel(targetDim);
        final ServerLevel targetLevel = (resolvedTargetLevel != null) ? resolvedTargetLevel : level;

        java.util.Map<String, BlockPos> all = new java.util.LinkedHashMap<>();
        var seriesData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);
        if (seriesData != null) all.putAll(seriesData.getAll());
        var teyvatData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        if (teyvatData != null && teyvatData.namedStructures != null) all.putAll(teyvatData.namedStructures);

        BlockPos origin = player.blockPosition();
        BlockPos bestDest = null;
        String bestKey = null;
        double bestDistSq = Double.MAX_VALUE;

        for (java.util.Map.Entry<String, BlockPos> e : all.entrySet()) {
            if (com.soybe.awakeningsystem.world.SeriesStructureWorldGen.matchesCanonical(e.getKey(), canonical)) {
                // Ensure the existing structure was recorded in the matching dimension
                net.minecraft.resources.ResourceKey<Level> entryDim = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getDimensionOfKey(e.getKey(), canonical);
                if (!entryDim.equals(targetDim)) {
                    continue; // Skip structures recorded in the wrong dimension (e.g. legacy Overworld murim)
                }

                // Check for damaged/corrupted placements (e.g. monoliths into water or steep dropoffs)
                if (targetDim.equals(Level.OVERWORLD) && !canonical.equals("orv") && !canonical.equals("black_market_den")) {
                    int checkY = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.findSolidSurfaceGround(targetLevel, e.getValue().getX(), e.getValue().getZ());
                    if (checkY < 50 || Math.abs(checkY - e.getValue().getY()) > 4) {
                        continue; // Corrupted terrain or placed in water; regenerate a pristine site instead
                    }
                }

                double dist = e.getValue().distSqr(origin);
                if (dist < bestDistSq) {
                    bestDistSq = dist;
                    bestDest = e.getValue();
                    bestKey = e.getKey();
                }
            }
        }

        if (bestDest != null) {
            BlockPos landing = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getSafeLandingPosition(targetLevel, bestDest, canonical);
            if (player.serverLevel() != targetLevel) {
                player.teleportTo(targetLevel, landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D, player.getYRot(), player.getXRot());
            } else {
                player.teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
            }
            final String k = bestKey;
            final int distBlocks = (int) Math.sqrt(bestDistSq);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_teleported_to_existing_structure_at", String.valueOf(k), String.valueOf(landing.getX()), String.valueOf(landing.getY()), String.valueOf(landing.getZ()), String.valueOf(targetLevel.dimension().location().getPath()), String.valueOf(distBlocks)), true);
            return 1;
        }

        // Si no existía todavía, localizar su ubicación natural en la semilla del mundo y generarla allí de forma orgánica
        var naturalResult = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.findOrCreateNaturalStructure(level, player, target);
        if (naturalResult != null) {
            ServerLevel resultLevel = naturalResult.level();
            BlockPos landing = naturalResult.landingPos();
            if (player.serverLevel() != resultLevel) {
                player.teleportTo(resultLevel, landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D, player.getYRot(), player.getXRot());
            } else {
                player.teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
            }
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_teleported_to_natural_site_of", String.valueOf(naturalResult.canonicalName()), String.valueOf(landing.getX()), String.valueOf(landing.getY()), String.valueOf(landing.getZ()), String.valueOf(resultLevel.dimension().location().getPath()), String.valueOf(naturalResult.biomeName())), true);
            return 1;
        }

        source.sendFailure(Component.translatable("awakening_system.msg.unknown_structure_type_use_structure_list", String.valueOf(name)));
        return 0;
    }

    private static int locateStructureCommand(CommandSourceStack source, String filterType) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        var server = level.getServer();
        var seriesData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);

        BlockPos origin = BlockPos.containing(source.getPosition());
        java.util.Map<String, BlockPos> all = new java.util.LinkedHashMap<>();
        if (seriesData != null) all.putAll(seriesData.getAll());
        var teyvatData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        if (teyvatData != null && teyvatData.namedStructures != null) all.putAll(teyvatData.namedStructures);

        String canonicalFilter = filterType != null && !filterType.isEmpty()
                ? com.soybe.awakeningsystem.world.SeriesStructureWorldGen.canonicalizeStructureName(filterType)
                : null;

        net.minecraft.resources.ResourceKey<Level> targetDim = (canonicalFilter != null)
                ? com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getTargetDimensionForStructure(canonicalFilter)
                : level.dimension();
        ServerLevel resolvedLocateTargetLevel = server.getLevel(targetDim);
        final ServerLevel targetLevel = (resolvedLocateTargetLevel != null) ? resolvedLocateTargetLevel : level;

        java.util.Map.Entry<String, BlockPos> nearest = null;
        double bestDistSq = Double.MAX_VALUE;

        for (java.util.Map.Entry<String, BlockPos> entry : all.entrySet()) {
            if (canonicalFilter != null) {
                if (!com.soybe.awakeningsystem.world.SeriesStructureWorldGen.matchesCanonical(entry.getKey(), canonicalFilter)) {
                    continue;
                }
                net.minecraft.resources.ResourceKey<Level> entryDim = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getDimensionOfKey(entry.getKey(), canonicalFilter);
                if (!entryDim.equals(targetDim)) {
                    continue;
                }
            }
            double dist = entry.getValue().distSqr(origin);
            if (dist < bestDistSq) {
                bestDistSq = dist;
                nearest = entry;
            }
        }

        if (nearest != null) {
            final var found = nearest;
            final BlockPos basePos = found.getValue();
            final String cName = canonicalFilter != null ? canonicalFilter : found.getKey();
            final BlockPos p = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.getSafeLandingPosition(targetLevel, basePos, cName);
            final int distBlocks = (int) Math.sqrt(bestDistSq);
            net.minecraft.network.chat.Component coords = Component.literal("§b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "]")
                .withStyle(style -> style
                    .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/structure tp " + (canonicalFilter != null ? canonicalFilter : found.getKey())))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("awakening_system.msg.click_to_teleport_here"))));
            net.minecraft.network.chat.Component msg = Component.translatable("awakening_system.msg.system_existing_structure_in", String.valueOf(found.getKey()), String.valueOf(targetLevel.dimension().location().getPath())).append(coords).append(Component.translatable("awakening_system.msg.blocks_away", String.valueOf(distBlocks)));
            source.sendSuccess(() -> msg, false);
            return 1;
        }

        if (source.getEntity() instanceof ServerPlayer sp && canonicalFilter != null) {
            var nat = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.findOrCreateNaturalStructure(level, sp, canonicalFilter);
            if (nat != null) {
                BlockPos p = nat.landingPos();
                int distBlocks = (int) Math.sqrt(p.distSqr(origin));
                net.minecraft.network.chat.Component coords = Component.literal("§b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "]")
                    .withStyle(style -> style
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/structure tp " + nat.canonicalName()))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("awakening_system.msg.click_to_teleport_here"))));
                net.minecraft.network.chat.Component msg = Component.translatable("awakening_system.msg.system_natural_structure_discovered_in", String.valueOf(nat.canonicalName()), String.valueOf(nat.level().dimension().location().getPath())).append(coords).append(Component.translatable("awakening_system.msg.biome_away", String.valueOf(nat.biomeName()), String.valueOf(distBlocks)));
                source.sendSuccess(() -> msg, false);
                return 1;
            }
        }

        source.sendFailure(Component.translatable("awakening_system.msg.no_coordinates_found_within_current_range"));
        return 0;
    }

    private static int listStructuresCommand(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        var server = level.getServer();
        var seriesData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);

        java.util.Map<String, BlockPos> all = new java.util.LinkedHashMap<>();
        if (seriesData != null) all.putAll(seriesData.getAll());
        var teyvatData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        if (teyvatData != null && teyvatData.namedStructures != null) all.putAll(teyvatData.namedStructures);

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_no_structures_have_generated_in"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.registered_structures", String.valueOf(all.size())), false);
        BlockPos origin = BlockPos.containing(source.getPosition());
        for (java.util.Map.Entry<String, BlockPos> entry : all.entrySet()) {
            BlockPos p = entry.getValue();
            int dist = (int) Math.sqrt(p.distSqr(origin));
            net.minecraft.network.chat.Component coords = Component.literal("§b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "]")
                .withStyle(style -> style
                    .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/structure tp " + entry.getKey()))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("awakening_system.msg.click_to_teleport_here"))));
            net.minecraft.network.chat.Component line = Component.literal("§e✦ " + entry.getKey() + " §7en ").append(coords).append(Component.literal(" §8(" + dist + "m)"));
            source.sendSuccess(() -> line, false);
        }
        return 1;
    }

    private static int spawnNpcCommand(CommandSourceStack source, String character) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cCommand only available on the server."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        com.soybe.awakeningsystem.entity.NpcProfile profile = com.soybe.awakeningsystem.entity.NpcProfile.getById(character);
        com.soybe.awakeningsystem.entity.SeriesNpcEntity npc = new com.soybe.awakeningsystem.entity.SeriesNpcEntity(
                com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), level, profile
        );
        npc.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        npc.setHomePos(pos);
        level.addFreshEntity(npc);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_npc_successfully_summoned", String.valueOf(profile.titleColor()), String.valueOf(profile.displayName())), true);
        return 1;
    }

    private static int storeShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        int count = SystemEvents.storePlayerShadows(player);
        if (count > 0) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.shadow_army_saved_soldier_to_your", String.valueOf(count)), false);
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.you_have_no_shadow_soldiers_nearby"));
        }
        return count;
    }

    private static int deployShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        int deployed = SystemEvents.deployPlayerShadows(player, 4);
        if (deployed > 0) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.shadow_army_deployed_soldier_to_battle", String.valueOf(deployed)), false);
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.you_have_no_shadow_soldiers_in"));
        }
        return deployed;
    }

    private static int showShadowStatus(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        String masterTag = "shadow_master_" + player.getUUID();
        long activeCount = 0;
        if (player.level() instanceof ServerLevel sl) {
            activeCount = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(96.0D),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)).size();
        }
        int stored = data.storedShadowCount();
        int energy = data.energy();
        final long finalActive = activeCount;
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.shadow_domain"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.active_soldiers_in_field", String.valueOf(finalActive)), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.stored_soldiers_in_domain", String.valueOf(stored)), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.current_mana_for_regeneration_100_regen", String.valueOf(energy)), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.quick_controls_to_extract_deploy_shift"), false);
        source.sendSuccess(() -> Component.literal("§8======================================================="), false);
        return 1;
    }

    private static int dismissShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemEvents.dismissAllShadows(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.shadow_army_all_your_active_shadows"), false);
        return 1;
    }

    private static int spawnDokkaebiCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player."));
            return 0;
        }
        SystemEvents.spawnDokkaebiNearPlayer(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_dokkaebi_successfully_summoned_beside"), false);
        return 1;
    }

    private static int showDokkaebiQuestCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        net.minecraft.world.phys.AABB nearby = player.getBoundingBox().inflate(32.0D);
        java.util.List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.DokkaebiEntity.class, nearby);
        if (dokkaebis.isEmpty()) {
            source.sendFailure(Component.translatable("awakening_system.msg.there_is_no_dokkaebi_broadcasting_nearby"));
            return 0;
        }
        var dokkaebi = dokkaebis.get(0);
        if (!dokkaebi.isQuestActive()) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_the_dokkaebi_has_not"), false);
        } else {
            String title = (dokkaebi.getQuestTitle() != null && !dokkaebi.getQuestTitle().isEmpty()) ? dokkaebi.getQuestTitle() : "Sub-Scenario";
            String desc = (dokkaebi.getQuestDesc() != null) ? dokkaebi.getQuestDesc() : "";
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_sub_escenario", String.valueOf(title), String.valueOf(dokkaebi.getQuestProgress()), String.valueOf(dokkaebi.getQuestTarget()), String.valueOf(desc)), false);
        }
        return 1;
    }

    private static int dismissDokkaebiCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        net.minecraft.world.phys.AABB nearby = player.getBoundingBox().inflate(32.0D);
        java.util.List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.DokkaebiEntity.class, nearby);
        if (dokkaebis.isEmpty()) {
            source.sendFailure(Component.translatable("awakening_system.msg.there_is_no_dokkaebi_nearby_to"));
            return 0;
        }
        for (var d : dokkaebis) {
            d.dismissWithParticles();
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_the_dokkaebi_has_closed"), false);
        return 1;
    }

    private static int triggerDokkaebiScenarioCommand(CommandSourceStack source, int forcedTarget) {
        var server = source.getServer();
        if (server == null) {
            source.sendFailure(Component.literal("Server is not available."));
            return 0;
        }
        boolean started = SystemEvents.triggerServerDokkaebiEvent(server, forcedTarget);
        if (started) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_server_wide_dokkaebi_scenario"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.could_not_start_scenario_no_eligible"));
            return 0;
        }
    }

    private static int spawnBossCommand(CommandSourceStack source, String type) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        if ("igris".equalsIgnoreCase(type)) {
            var igris = com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS.get().create(level);
            if (igris != null) {
                igris.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(igris);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_igris_the_bloodred_commander"), true);
                return 1;
            }
        } else if ("baran".equalsIgnoreCase(type)) {
            var baran = com.soybe.awakeningsystem.entity.ModEntities.BARAN_BOSS.get().create(level);
            if (baran != null) {
                baran.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(baran);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_baran_monarch_of_white"), true);
                return 1;
            }
        } else if ("patriarch".equalsIgnoreCase(type) || "patriarca".equalsIgnoreCase(type) || "blood_patriarch".equalsIgnoreCase(type)) {
            var patriarch = com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get().create(level);
            if (patriarch != null) {
                patriarch.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(patriarch);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_great_patriarch_of_the"), true);
                return 1;
            }
        } else if ("treant".equalsIgnoreCase(type) || "yggdrasil_treant".equalsIgnoreCase(type)) {
            var treant = com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get().create(level);
            if (treant != null) {
                treant.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(treant);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_ancestral_treant_of_yggdrasil"), true);
                return 1;
            }
        } else if ("childe".equalsIgnoreCase(type) || "tartaglia".equalsIgnoreCase(type) || "nobile".equalsIgnoreCase(type)) {
            var childe = com.soybe.awakeningsystem.entity.ModEntities.CHILDE_BOSS.get().create(level);
            if (childe != null) {
                childe.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(childe);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_tartaglia_childe_11th_of"), true);
                return 1;
            }
        } else if ("raiden".equalsIgnoreCase(type) || "shogun".equalsIgnoreCase(type) || "baal".equalsIgnoreCase(type)) {
            var raiden = com.soybe.awakeningsystem.entity.ModEntities.RAIDEN_SHOGUN_BOSS.get().create(level);
            if (raiden != null) {
                raiden.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(raiden);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_raiden_shogun_baal_god"), true);
                return 1;
            }
        } else if ("wolflord".equalsIgnoreCase(type) || "rey_acechador".equalsIgnoreCase(type) || "golden_wolflord".equalsIgnoreCase(type)) {
            var wolflord = com.soybe.awakeningsystem.entity.ModEntities.GOLDEN_WOLFLORD_BOSS.get().create(level);
            if (wolflord != null) {
                wolflord.moveTo(player.getX() + 2.0, player.getY() + 4.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(wolflord);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_golden_wolflord_monarch_of"), true);
                return 1;
            }
        } else if ("azhdaha".equalsIgnoreCase(type)) {
            var azhdaha = com.soybe.awakeningsystem.entity.ModEntities.AZHDAHA_BOSS.get().create(level);
            if (azhdaha != null) {
                azhdaha.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(azhdaha);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_azhdaha_sealed_primeval_dragon"), true);
                return 1;
            }
        } else if ("anemo_hypostasis".equalsIgnoreCase(type) || "anemo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_anemo_hypostasis_beth_has"), true);
                return 1;
            }
        } else if ("electro_hypostasis".equalsIgnoreCase(type) || "electro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_electro_hypostasis_aleph_has"), true);
                return 1;
            }
        } else if ("geo_hypostasis".equalsIgnoreCase(type) || "geo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_geo_hypostasis_gimel_has"), true);
                return 1;
            }
        } else if ("cryo_hypostasis".equalsIgnoreCase(type) || "cryo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.CRYO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_cryo_hypostasis_daleth_has"), true);
                return 1;
            }
        } else if ("pyro_hypostasis".equalsIgnoreCase(type) || "pyro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_pyro_hypostasis_ayin_has"), true);
                return 1;
            }
        } else if ("hydro_hypostasis".equalsIgnoreCase(type) || "hydro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.HYDRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_hydro_hypostasis_he_has"), true);
                return 1;
            }
        } else if ("cryo_regisvine".equalsIgnoreCase(type) || "regisvine".equalsIgnoreCase(type)) {
            var r = com.soybe.awakeningsystem.entity.ModEntities.CRYO_REGISVINE.get().create(level);
            if (r != null) {
                r.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(r);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_cryo_regisvine_has_appeared"), true);
                return 1;
            }
        } else if ("sculk".equalsIgnoreCase(type) || "warden".equalsIgnoreCase(type) || "colossus".equalsIgnoreCase(type) || "coloso".equalsIgnoreCase(type) || "sculk_warden".equalsIgnoreCase(type)) {
            var wardenBoss = com.soybe.awakeningsystem.entity.ModEntities.COLOSSAL_SCULK_WARDEN.get().create(level);
            if (wardenBoss != null) {
                wardenBoss.moveTo(player.getX() + 3.0, player.getY(), player.getZ() + 3.0, player.getYRot(), 0.0F);
                level.addFreshEntity(wardenBoss);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.boss_spawned_el_titan_colosal_de"), true);
                return 1;
            }
        } else if ("sculk_golem".equalsIgnoreCase(type) || "golem".equalsIgnoreCase(type)) {
            var golem = com.soybe.awakeningsystem.entity.ModEntities.SCULK_GOLEM.get().create(level);
            if (golem != null) {
                golem.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                golem.setStatue(false);
                level.addFreshEntity(golem);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.mob_spawned_golem_de_sculk_invocado"), true);
                return 1;
            }
        } else if ("sculk_statue".equalsIgnoreCase(type) || "statue".equalsIgnoreCase(type)) {
            var golem = com.soybe.awakeningsystem.entity.ModEntities.SCULK_GOLEM.get().create(level);
            if (golem != null) {
                golem.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                golem.setStatue(true);
                level.addFreshEntity(golem);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.mob_spawned_estatua_durmiente_de_golem"), true);
                return 1;
            }
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.jefe_desconocido_usa_boss_warden_colossus"));
            return 0;
        }
        return 0;
    }

    private static int triggerPenaltyCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can be sent to the Penalty Zone."));
            return 0;
        }
        com.soybe.awakeningsystem.penalty.PenaltyZoneManager.teleportToPenaltyZone(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_transportado_la_zona_de_castigo"), false);
        return 1;
    }

    private static int enterTower(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TowerManager.enterTower(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can enter the Tower."));
        return 0;
    }

    private static int warpTowerFloor(CommandSourceStack source, int floorNum) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().equals(TowerManager.RANKER_TOWER)) {
                TowerManager.warpFloor(player, floorNum);
            } else {
                com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, floorNum);
            }
            return 1;
        }
        source.sendFailure(Component.literal("Only players can change floors in the Tower."));
        return 0;
    }

    private static int spawnTowerStructure(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            BlockPos pos = player.blockPosition();
            com.soybe.awakeningsystem.world.TowerStructureGenerator.generate(level, pos);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_fachada_estructura_monumental_de_la", String.valueOf(pos.getX()), String.valueOf(pos.getZ())), true);
            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 0.8F);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can spawn the tower structure."));
        return 0;
    }

    private static int exportTowerCommand(CommandSourceStack source, int customRadius) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            BlockPos pos = player.blockPosition();
            int count = com.soybe.awakeningsystem.world.TowerStructureGenerator.exportTower(level, pos, customRadius);
            if (count > 0) {
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_torre_exportada_exitosamente_bloques_guardados", String.valueOf(count)), true);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                return 1;
            } else {
                source.sendFailure(Component.translatable("awakening_system.msg.error_al_exportar_la_torre_revisa"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("Only players can export the tower."));
        return 0;
    }

    private static int resetTowerCommand(CommandSourceStack source) {
        java.nio.file.Path p = com.soybe.awakeningsystem.world.TowerStructureGenerator.getExportFilePath();
        try {
            if (java.nio.file.Files.deleteIfExists(p)) {
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_archivo_custom_eliminado_ahora_la"), true);
                return 1;
            } else {
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_no_habia_ningun_archivo_custom"), false);
                return 1;
            }
        } catch (Exception e) {
            source.sendFailure(Component.translatable("awakening_system.msg.error_al_eliminar_archivo_custom", String.valueOf(e.getMessage())));
            return 0;
        }
    }

    private static int resetTowerMissionsCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            CompoundTag tag = player.getPersistentData();
            tag.remove("tower_mission_active");
            tag.remove("tower_mission_index");
            tag.remove("tower_mission_progress");
            tag.remove("tower_mission_needed");
            tag.remove("tower_mission_target");
            tag.remove("tower_mission_title");
            tag.remove("tower_mission_floor");
            for (int i = 0; i < com.soybe.awakeningsystem.tower.TowerDailyMissionManager.ALL_MISSIONS.size(); i++) {
                tag.remove("tower_mission_comp_day_" + i);
            }
            source.sendSuccess(() -> Component.literal("§6[SISTEMA] §aMisiones diarias de la torre reiniciadas con éxito."), true);
            com.soybe.awakeningsystem.tower.TowerDailyMissionManager.openBoardFor(player);
            return 1;
        }
        return 0;
    }

    private static int resetFloor10BossCommand(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        DimensionGenSavedData.get(server).setTowerFloor10BossDefeated(false);
        ServerLevel floor10 = server.getLevel(com.soybe.awakeningsystem.tower.GreatTowerWorldManager.TOWER_FLOOR_10);
        if (floor10 != null) {
            var existing = floor10.getEntities(com.soybe.awakeningsystem.entity.ModEntities.COLOSSAL_SCULK_WARDEN.get(), net.minecraft.world.entity.LivingEntity::isAlive);
            for (var b : existing) {
                b.discard();
            }
            com.soybe.awakeningsystem.tower.GreatTowerWorldManager.ensureFloor10Boss(floor10);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_el_titan_colosal_de_sculk"), true);
        } else {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.sistema_el_estado_del_titan_colosal"), true);
        }
        return 1;
    }

    private static int cultivate(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SystemEvents.handleCultivationAction(player, "toggle_meditation", 0);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can cultivate Qi."));
        return 0;
    }

    private static int warpIsekai(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            IsekaiWorldManager.teleportToIsekai(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.command.isekai_teleport"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can travel to the Isekai world."));
        return 0;
    }

    private static int completeUpdate(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data.isSystem40Updating()) {
                TeyvatWorldManager.completeSystem40Update(player, data);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_update_completed_instantly_without_waiting"), true);
                return 1;
            } else if (data.isSystem30Updating()) {
                MurimWorldManager.completeSystem30Update(player, data);
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_update_completed_instantly_without_waiting_2"), true);
                return 1;
            } else if (data.isSystemUpdating()) {
                IsekaiWorldManager.completeSystemUpdate(player, data);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.complete_update_success"), true);
                return 1;
            } else {
                source.sendFailure(Component.translatable("awakening_system.msg.there_is_no_update_in_progress"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("Only players can complete the System update."));
        return 0;
    }

    private static int skipUpdateCommand(CommandSourceStack source) {
        return completeUpdate(source);
    }

    private static int enterSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.enterSubspace(player);
            return 1;
        }
        source.sendFailure(Component.translatable("awakening_system.command.only_players"));
        return 0;
    }

    private static int exitSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.exitSubspace(player);
            return 1;
        }
        source.sendFailure(Component.translatable("awakening_system.command.only_players"));
        return 0;
    }

    private static int requestUpdateSubspaceStructureCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.requestUpdateStructure(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can update subspace structure."));
        return 0;
    }

    private static int confirmUpdateSubspaceStructureCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.regenerateSubspaceStructure(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can update subspace structure."));
        return 0;
    }

    private static int upgradeSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return com.soybe.awakeningsystem.subspace.SubspaceManager.upgradeSubspace(player) ? 1 : 0;
        }
        source.sendFailure(Component.translatable("awakening_system.command.only_players"));
        return 0;
    }

    private static int summonSanctuaryDoorCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            var door = com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.summonExteriorDoor(player);
            return door != null ? 1 : 0;
        }
        source.sendFailure(Component.literal("Solo jugadores pueden invocar la puerta del santuario."));
        return 0;
    }

    private static int enterSanctuaryCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.enterSanctuary(player, null);
            return 1;
        }
        source.sendFailure(Component.literal("Solo jugadores pueden entrar a su santuario."));
        return 0;
    }

    private static int exitSanctuaryCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.exitSanctuary(player, null);
            return 1;
        }
        source.sendFailure(Component.literal("Solo jugadores pueden salir del santuario."));
        return 0;
    }

    private static int upgradeSanctuaryCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.upgradeSanctuary(player) ? 1 : 0;
        }
        source.sendFailure(Component.literal("Solo jugadores pueden mejorar el santuario."));
        return 0;
    }

    private static int setSystemVersion(CommandSourceStack source, String verStr) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("awakening_system.command.only_players"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.setSystemUpdating(false);
        data.setSystem30Updating(false);
        data.setSystem40Updating(false);

        String v = verStr.toLowerCase().trim();
        if (v.equals("1") || v.equals("1.0") || v.equals("base") || v.equals("v1")) {
            data.setSystemUpdated(false);
            data.setIsekaiUnlocked(false);
            data.setSystem30Updated(false);
            data.setMurimUnlocked(false);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_version_switched_instantly_to_system"), true);
            return 1;
        } else if (v.equals("2") || v.equals("2.0") || v.equals("isekai") || v.equals("v2")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(false);
            data.setMurimUnlocked(false);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_version_switched_instantly_to_system_2"), true);
            return 1;
        } else if (v.equals("3") || v.equals("3.0") || v.equals("murim") || v.equals("v3")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_version_switched_instantly_to_system_3"), true);
            return 1;
        } else if (v.equals("4") || v.equals("4.0") || v.equals("teyvat") || v.equals("v4")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(true);
            data.setTeyvatUnlocked(true);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.system_version_switched_instantly_to_system_4"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.invalid_version_use_system_version"));
            return 0;
        }
    }

    private static int warpWorldTree(CommandSourceStack source, String mode) {
        if (source.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            if (server == null) return 0;
            ServerLevel isekaiLevel = server.getLevel(IsekaiWorldManager.ISEKAI_WORLD);
            if (isekaiLevel == null) {
                source.sendFailure(Component.translatable("awakening_system.tower.dimension_unavailable"));
                return 0;
            }

            if ("procedural".equalsIgnoreCase(mode)) {
                IsekaiWorldManager.setUseProceduralYggdrasil(true);
                IsekaiWorldManager.ensureWorldTreeDirect(isekaiLevel, true);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.worldtree_procedural_success"), true);
            } else if ("nbt".equalsIgnoreCase(mode)) {
                IsekaiWorldManager.setUseProceduralYggdrasil(false);
                IsekaiWorldManager.ensureWorldTreeDirect(isekaiLevel, true);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.worldtree_nbt_success"), true);
            } else if ("rebuild".equalsIgnoreCase(mode)) {
                IsekaiWorldManager.ensureWorldTreeDirect(isekaiLevel, true);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.worldtree_rebuild_success",
                        (IsekaiWorldManager.isUseProceduralYggdrasil() ? "Procedural" : "NBT")), true);
            } else {
                IsekaiWorldManager.ensureWorldTreeDirect(isekaiLevel, false);
            }

            int targetY = YggdrasilTreeGenerator.BASE_Y + 1;
            if (TowerManager.isInTower(player.getUUID())) {
                TowerManager.exitTowerToDimension(player, isekaiLevel, new net.minecraft.core.BlockPos(0, targetY, 26));
            } else {
                player.teleportTo(isekaiLevel, 0.5D, (double) targetY, 26.0D, 180.0F, 0.0F);
            }

            isekaiLevel.sendParticles(ParticleTypes.END_ROD, 0.5D, targetY + 0.5D, 26.0D, 100, 2.0D, 1.2D, 2.0D, 0.08D);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.2F);

            source.sendSuccess(() -> Component.translatable("awakening_system.command.worldtree_teleport"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can travel to the World Tree."));
        return 0;
    }

    private static int warpMurim(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            MurimWorldManager.teleportToMurim(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.command.murim_teleport"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can ascend to the Murim Realm."));
        return 0;
    }

    private static int warpTeyvat(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TeyvatWorldManager.teleportToTeyvat(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_traveling_to_teyvat_ceremonial_plaza"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can travel to Teyvat."));
        return 0;
    }

    private static int warpTeyvatCapital(CommandSourceStack source, String capital) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can travel around Teyvat."));
            return 0;
        }
        String clean = capital.toLowerCase(java.util.Locale.ROOT).trim();
        switch (clean) {
            case "golden_house", "childe", "tartaglia", "casa_dorada" -> TeyvatWorldManager.teleportToGoldenHouse(player);
            case "liyue", "yujing", "contratos" -> TeyvatWorldManager.teleportToLiyue(player);
            case "tenshukaku", "raiden", "shogun", "baal", "electric_castle", "electriccastle" -> TeyvatWorldManager.teleportToTenshukaku(player);
            case "inazuma", "eternidad" -> TeyvatWorldManager.teleportToInazuma(player);
            case "wolflord", "acechador", "enkanomiya", "rey_acechador", "golden_wolflord" -> TeyvatWorldManager.teleportToWolflord(player);
            case "azhdaha", "dragon", "dominio_azhdaha" -> TeyvatWorldManager.teleportToAzhdaha(player);
            case "windrise", "roble", "arbol_sagrado", "windrise_giant_tree" -> TeyvatWorldManager.teleportToWindrise(player);
            case "jade_chamber", "camara_jade", "jade" -> TeyvatWorldManager.teleportToJadeChamber(player);
            case "winery", "dawn_winery", "vinedo", "vinedo_amanecer" -> TeyvatWorldManager.teleportToWinery(player);
            case "alcor", "beidou", "barco", "beidou_ship" -> TeyvatWorldManager.teleportToAlcor(player);
            case "witch_island", "isla_bruja" -> TeyvatWorldManager.teleportToWitchIsland(player);
            case "treehouses", "teyvat_treehouses", "aldea_arbol" -> TeyvatWorldManager.teleportToTreehouses(player);
            case "anemo_hypostasis", "anemo_cube", "hipostasis_anemo" -> TeyvatWorldManager.teleportToHypostasis(player, "anemo");
            case "electro_hypostasis", "electro_cube", "hipostasis_electro" -> TeyvatWorldManager.teleportToHypostasis(player, "electro");
            case "geo_hypostasis", "geo_cube", "hipostasis_geo" -> TeyvatWorldManager.teleportToHypostasis(player, "geo");
            case "cryo_hypostasis", "cryo_cube", "hipostasis_cryo" -> TeyvatWorldManager.teleportToHypostasis(player, "cryo");
            case "pyro_hypostasis", "pyro_cube", "hipostasis_pyro" -> TeyvatWorldManager.teleportToHypostasis(player, "pyro");
            case "hydro_hypostasis", "hydro_cube", "hipostasis_hydro" -> TeyvatWorldManager.teleportToHypostasis(player, "hydro");
            case "cryo_regisvine", "regisvine", "regisvid" -> TeyvatWorldManager.teleportToHypostasis(player, "cryo_regisvine");
            case "mondstadt", "favonius", "viento" -> TeyvatWorldManager.teleportToMondstadt(player);
            case "sumeru", "academiya", "sabiduria" -> TeyvatWorldManager.teleportToSumeru(player);
            case "fontaine", "palais", "justicia", "mermonia" -> TeyvatWorldManager.teleportToFontaine(player);
            case "spawn", "plaza", "llegada", "inicio" -> TeyvatWorldManager.teleportToTeyvat(player);
            case "abyss", "spiral", "musk", "arrecife" -> {
                TeyvatWorldManager.teleportToAbyss(player);
                com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            }
            case "racetrack", "hipodromo", "uma", "carreras" -> {
                return warpRacetrackCommand(source);
            }
            case "casino", "gran_casino", "apuestas" -> {
                return warpCasinoCommand(source);
            }
            default -> {
                source.sendFailure(Component.translatable("awakening_system.msg.unrecognized_destination_available_options_weekly_bosses"));
                return 0;
            }
        }
        return 1;
    }

    private static int warpCasinoCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can travel to the Grand Casino of Teyvat."));
            return 0;
        }
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.buildCasinoIfAbsent(teyvat);
        TeyvatWorldManager.teleportToCasino(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.grand_casino_of_teyvat_you_have"), true);
        return 1;
    }

    private static int rebuildCasinoCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.forceRebuild(teyvat);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.grand_casino_of_teyvat_monte_carlo"), true);
        return 1;
    }

    private static int clearCasinoFurnitureCommand(CommandSourceStack source, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        int cleared = com.soybe.awakeningsystem.casino.CasinoJsonHelper.clearFurniture(level, origin, radius);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.casino_cleared_furniture_in_radius_of", String.valueOf(cleared), String.valueOf(radius), String.valueOf(origin.getX()), String.valueOf(origin.getY()), String.valueOf(origin.getZ())), true);
        return cleared;
    }

    private static int exportCasinoFurnitureCommand(CommandSourceStack source, String layoutName, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        java.io.File file = com.soybe.awakeningsystem.casino.CasinoJsonHelper.exportFurniture(level, origin, radius, layoutName);
        if (file != null) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.casino_furniture_successfully_exported_to_config", String.valueOf(file.getName())), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.error_exporting_casino_configuration_file"));
            return 0;
        }
    }

    private static int importCasinoFurnitureCommand(CommandSourceStack source, String layoutName) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        int placed = com.soybe.awakeningsystem.casino.CasinoJsonHelper.importFurniture(level, origin, layoutName);
        if (placed >= 0) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.layout_successfully_loaded_placed_furniture_and", String.valueOf(layoutName), String.valueOf(placed)), true);
            return placed;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.file_not_found_in_config_awakening", String.valueOf(layoutName)));
            return 0;
        }
    }

    private static int populateCasinoFurnitureCommand(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.populateDefaultFurniture(level, origin);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.casino_official_regulation_furniture_slots_blackjack"), true);
        return 1;
    }

    private static int claimJackpotCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can claim the Casino Jackpot."));
            return 0;
        }

        SystemData data = SystemEvents.data(player);
        data.addMora(50_000_000);
        data.addCoins(1600);
        data.addCasinoWinnings(50_000_000L);

        giveOrDrop(player, new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 10));
        giveOrDrop(player, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 10));
        giveOrDrop(player, new ItemStack(SystemItems.CREATOR_CLAYMORE.get(), 1));
        giveOrDrop(player, new ItemStack(SystemItems.END_VOID_KEY.get(), 1));

        ItemStack trophy = com.soybe.awakeningsystem.item.CasinoTrophyItem.createTrophy(
                3, player.getName().getString(), "Grand Casino 777 Jackpot"
        );
        giveOrDrop(player, trophy);

        AwakeningSystemMod.sync(player);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), ModSounds.CASINO_JACKPOT.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
        level.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.8D, player.getZ(), 80, 0.6D, 0.8D, 0.6D, 0.12D);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 70, 0.4D, 0.4D, 0.4D, 0.2D);

        source.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("awakening_system.msg.casino_mega_jackpot_player_has_claimed", String.valueOf(player.getName().getString())), false);

        player.sendSystemMessage(Component.translatable("awakening_system.msg.awakening_casino_you_have_successfully_claimed"));
        return 1;
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static int warpRacetrackCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can travel to the Teyvat Racetrack."));
            return 0;
        }
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.buildRacetrackIfAbsent(teyvat);
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.pruneDuplicateRunners();
        }
        player.teleportTo(teyvat, 122.5, 68.0, 224.5, 180.0F, 0.0F);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_racetrack_you_have_teleported_to"), true);
        return 1;
    }

    private static int upgradeRacetrackMaterialsCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        BlockPos center = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.TRACK_CENTER;
        if (source.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                center = player.blockPosition();
            }
        }
        int[] results = com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.upgradeRacetrackBlocks(teyvat, center);
        source.sendSuccess(() -> Component.literal(String.format("§6🏇 [Teyvat Racetrack] §aMaterials successfully upgraded! §e%d §astone fences replaced with §fWhite Paddock Fences §aand §e%d §adirt/mud blocks replaced with §2Racetrack Turf§a.", results[0], results[1])), true);
        return 1;
    }

    private static int rebuildRacetrackCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.forceRebuild(teyvat);
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.setupNewRace();
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_racetrack_track_completely_rebuilt_without"), true);
        return 1;
    }

    private static int startRacetrackCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.teyvat_dimension_is_not_loaded"));
            return 0;
        }
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.startRaceNow();
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_racetrack_race_countdown_forcefully_started"), true);
            return 1;
        }
        source.sendFailure(Component.translatable("awakening_system.msg.could_not_access_the_racetrack_manager"));
        return 0;
    }

    private static int exportRacetrackCommand(CommandSourceStack source, int customRadius) {
        ServerLevel level = source.getLevel();
        BlockPos center = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.TRACK_CENTER;
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                center = player.blockPosition();
            }
        }

        int rx = customRadius > 0 ? customRadius : 95;
        int rz = customRadius > 0 ? customRadius : 50;
        int minY = 65;
        int maxY = 82;

        com.google.gson.JsonObject root = new com.google.gson.JsonObject();
        root.addProperty("version", 1);
        root.addProperty("centerX", center.getX());
        root.addProperty("centerY", center.getY());
        root.addProperty("centerZ", center.getZ());
        root.addProperty("radiusX", rx);
        root.addProperty("radiusZ", rz);

        com.google.gson.JsonArray blockList = new com.google.gson.JsonArray();
        int count = 0;

        for (int x = center.getX() - rx; x <= center.getX() + rx; x++) {
            for (int z = center.getZ() - rz; z <= center.getZ() + rz; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        com.google.gson.JsonObject bObj = new com.google.gson.JsonObject();
                        bObj.addProperty("x", x - center.getX());
                        bObj.addProperty("y", y - center.getY());
                        bObj.addProperty("z", z - center.getZ());
                        bObj.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                        blockList.add(bObj);
                        count++;
                    }
                }
            }
        }

        root.addProperty("blockCount", count);
        root.add("blocks", blockList);

        java.nio.file.Path exportPath = com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.getExportFilePath();
        try {
            if (exportPath.getParent() != null) {
                java.nio.file.Files.createDirectories(exportPath.getParent());
            }
            try (java.io.Writer writer = java.nio.file.Files.newBufferedWriter(exportPath)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
            final int finalCount = count;
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.track_successfully_exported_scanned_blocks_in", String.valueOf(finalCount), String.valueOf(exportPath.getFileName())), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("awakening_system.msg.error_saving_exported_file", String.valueOf(e.getMessage())));
            return 0;
        }
    }

    private static int rebuildCapitalsCommand(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_artificial_capital_structures_have_been"), false);
        return 1;
    }

    private static int syncTeyvatMapCommand(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.teyvat_external_region_injection_is_disabled"), false);
        return 1;
    }

    private static int openGachaCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            GachaManager.openGachaScreen(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.gacha_opening_teyvat_wish_system_interface"), false);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can open the Wish System."));
        return 0;
    }

    private static int setPhysiqueCommand(CommandSourceStack source, String type) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            data.setMurimPhysique(type);
            AwakeningSystemMod.sync(player);
            Component pName = Component.translatable("awakening_system.physique." + type + ".name");
            source.sendSuccess(() -> Component.translatable("awakening_system.command.physique_set", pName), true);
            return 1;
        }
        return 0;
    }

    private static int enterPagodaTrial(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PagodaTrialManager.enterTrial(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can enter the Nine Tribulations Pagoda."));
        return 0;
    }

    private static int warpPagodaStage(CommandSourceStack source, int stage) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PagodaTrialManager.warpStage(player, stage);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can change stages in the Pagoda."));
        return 0;
    }

    private static int spawnPagodaArenaCommand(CommandSourceStack source, int stage) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can spawn the pagoda arena."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        PagodaTrialManager.generateArenaInWorld(level, origin, stage, true);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.pagoda_arena_del_piso_generada_en", String.valueOf(stage), String.valueOf(origin.toShortString()), String.valueOf(stage)), true);
        return 1;
    }

    private static int exitPagodaTrial(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PagodaTrialManager.exitTrial(player.getServer(), player.getUUID());
            return 1;
        }
        source.sendFailure(Component.literal("Only players can exit the Pagoda."));
        return 0;
    }

    private static int showFables(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§6========================================"), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.star_stream_chronicles", String.valueOf(player.getName().getString())), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.unlocked_fables_61", String.valueOf(data.unlockedFables().size())), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.total_narrative_weight_pts", String.valueOf(data.fableWeight())), false);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.eligible_for_constellation", String.valueOf((data.isConstellation() ? "§e[Already Ascended]" : (ConstellationAscension.isEligibleForAscension(data) ? "§a✔ [ELIGIBLE]" : "§c✖ [Missing requirements: Lv 50+, 8+ fables, 400+ weight, 1 Mythic/Giant, 2 Legendary/Semi, 3 Historical]")))), false);
        source.sendSuccess(() -> Component.literal("§8----------------------------------------"), false);
        for (String fid : data.unlockedFables()) {
            var f = com.soybe.awakeningsystem.fable.FableDefinitions.get(fid);
            if (f != null) {
                source.sendSuccess(() -> Component.literal(" §e" + f.icon() + " ").append(f.getFormattedTitle()).append(Component.translatable("awakening_system.msg.pts", String.valueOf(f.weight()), String.valueOf((f.isWorldFirst() ? " §6[★ PRIMACÍA]" : "")))), false);
            }
        }
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.abre_el_menu_del_sistema_presiona"), false);
        source.sendSuccess(() -> Component.literal("§6========================================"), false);
        return 1;
    }

    private static int grantFableCommand(CommandSourceStack source, String fableId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        var fable = com.soybe.awakeningsystem.fable.FableDefinitions.get(fableId);
        if (fable == null) {
            source.sendFailure(Component.translatable("awakening_system.msg.fabula_no_encontrada", String.valueOf(fableId)));
            return 0;
        }
        SystemEvents.grantFable(player, fableId);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.fabula_otorgada").append(fable.getFormattedTitle()), false);
        return 1;
    }

    private static int openSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.openSubspace(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can open the dimensional subspace."));
        return 0;
    }

    private static int syncSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = com.soybe.awakeningsystem.subspace.SubspaceManager.getSubspaceLevel(player.getServer());
            if (level != null) {
                com.soybe.awakeningsystem.subspace.SubspaceManager.syncWorldToSubspace(level, player);
                SystemData d = SystemEvents.data(player);
                player.sendSystemMessage(Component.translatable("awakening_system.subspace.updated", d.subspaceChestCount(), d.subspaceUnlockedSlots()));
                return 1;
            }
        }
        source.sendFailure(Component.literal("Only players can synchronize the subspace."));
        return 0;
    }

    private static int claimDungeonReward(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (DungeonManager.claimDungeonRewardSafe(player)) {
                return 1;
            } else {
                player.sendSystemMessage(Component.translatable("awakening_system.dungeon.claim_no_pending"));
            }
        }
        return 0;
    }

    private static int gambleDungeonReward(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (DungeonManager.gambleDungeonReward(player)) {
                return 1;
            } else {
                player.sendSystemMessage(Component.translatable("awakening_system.dungeon.gamble_no_pending"));
            }
        }
        return 0;
    }

    private static int escapeCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("awakening_system.command.only_players"));
            return 0;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return 0;
        ResourceKey<Level> dim = player.level().dimension();

        if (dim.equals(DungeonManager.DUNGEON_REALM)) {
            if (DungeonManager.isInDungeon(player.getUUID())) {
                DungeonManager.exitDungeon(server, player.getUUID());
            } else {
                DungeonManager.rescueStrandedPlayer(player);
            }
            source.sendSuccess(() -> Component.translatable("awakening_system.dungeon.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (dim.equals(TowerManager.RANKER_TOWER)) {
            if (TowerManager.isInTower(player.getUUID())) {
                TowerManager.exitTower(server, player.getUUID());
            } else {
                TowerManager.exitTowerToSpawn(server, player.getUUID());
            }
            source.sendSuccess(() -> Component.translatable("awakening_system.tower.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (dim.equals(PagodaTrialManager.PAGODA_REALM)) {
            PagodaTrialManager.exitTrial(server, player.getUUID());
            source.sendSuccess(() -> Component.translatable("awakening_system.pagoda.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (dim.equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.exitSubspace(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.subspace.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (dim.equals(com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.SANCTUARY_REALM)) {
            com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager.exitSanctuary(player, null);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.has_regresado_de_tu_santuario_personal").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (dim.equals(com.soybe.awakeningsystem.abyss.SpiralAbyssManager.SPIRAL_ABYSS_DIMENSION)) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.handleLeaveAbyss(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.abyss.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(player.level())) {
            com.soybe.awakeningsystem.tower.GreatTowerWorldManager.exitTower(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.tower.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        if (player.getY() < -50.0 || player.getY() > 320.0) {
            DungeonManager.rescueStrandedPlayer(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.dungeon.escape_success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        source.sendFailure(Component.translatable("awakening_system.dungeon.not_in_dungeon"));
        return 0;
    }

    private static int openWelkinCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        long currentDay = player.serverLevel().getDayTime() / 24000L;
        boolean canClaim = data.welkinDays() > 0 && currentDay > data.lastWelkinClaimDay();
        int reward = data.isWelkinEnhanced() ? 135 : 90;
        PacketDistributor.sendToPlayer(player, new OpenWelkinScreenPayload(
                data.welkinDays(),
                reward,
                canClaim,
                data.isWelkinEnhanced(),
                data.welkinQuestStep(),
                data.welkinQuestProgress()
        ));
        return 1;
    }

    private static int upgradeWelkinCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        boolean newState = !data.isWelkinEnhanced();
        data.setWelkinEnhanced(newState);
        if (newState) {
            data.setWelkinQuestStep(4);
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.columbina_blessing_enhanced_50_successfully_activated", String.valueOf(player.getName().getString())), true);
        } else {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.welkin_moon_blessing_restored_to_standard"), true);
        }
        AwakeningSystemMod.sync(player);
        return 1;
    }

    private static int startWelkinQuestCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        GachaManager.handleStartWelkinQuest(player);
        return 1;
    }

    private static int completeWelkinQuestCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        GachaManager.completeColumbinaAscension(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.columbina_ascension_quest_completed"), true);
        return 1;
    }

    private static int resetWelkinQuestCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        data.setWelkinEnhanced(false);
        data.setWelkinQuestStep(0);
        data.setWelkinQuestProgress(0);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.columbina_quest_progress_reset_to"), true);
        return 1;
    }

    private static int addWelkinDaysCommand(CommandSourceStack source, int days) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        data.addWelkinDays(days);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.added_days_of_welkin_moon_blessing", String.valueOf(days), String.valueOf(data.welkinDays())), true);
        return 1;
    }

    private static int spawnWelkinSanctuaryCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        net.minecraft.core.BlockPos center = player.blockPosition().below();
        TrilunarSanctuaryManager.buildSanctuary(level, center);
        player.teleportTo(center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.sanctuary_of_the_three_moon_goddesses"), true);
        return 1;
    }

    private static int exportCircuitJsonCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cCommand only available for players."));
            return 0;
        }
        BlockPos playerPos = player.blockPosition();
        com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity targetBe = null;
        for (BlockPos p : BlockPos.betweenClosed(playerPos.offset(-32, -12, -32), playerPos.offset(32, 12, 32))) {
            if (player.level().getBlockEntity(p) instanceof com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity be) {
                targetBe = be;
                break;
            }
        }
        if (targetBe != null) {
            java.io.File file = com.soybe.awakeningsystem.uma.RacetrackJsonHelper.exportTrack(targetBe, (ServerLevel) player.level());
            if (file != null) {
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.track_successfully_exported_to_json_config", String.valueOf(file.getName())), true);
                return 1;
            }
        }
        source.sendFailure(Component.translatable("awakening_system.msg.no_custom_racetrack_core_found_within"));
        return 0;
    }

    private static int reloadCircuitJsonCommand(CommandSourceStack source, String fileName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cCommand only available for players."));
            return 0;
        }
        BlockPos playerPos = player.blockPosition();
        com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity targetBe = null;
        for (BlockPos p : BlockPos.betweenClosed(playerPos.offset(-32, -12, -32), playerPos.offset(32, 12, 32))) {
            if (player.level().getBlockEntity(p) instanceof com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity be) {
                targetBe = be;
                break;
            }
        }
        if (targetBe != null) {
            boolean ok = com.soybe.awakeningsystem.uma.RacetrackJsonHelper.importTrack(targetBe, fileName);
            if (ok) {
                final int wpCount = targetBe.getWaypoints().size();
                source.sendSuccess(() -> Component.translatable("awakening_system.msg.track_successfully_reloaded_from_waypoints", String.valueOf(fileName), String.valueOf(wpCount)), true);
                return 1;
            } else {
                source.sendFailure(Component.translatable("awakening_system.msg.could_not_load_file_in_config", String.valueOf(fileName)));
                return 0;
            }
        }
        source.sendFailure(Component.translatable("awakening_system.msg.no_custom_racetrack_core_found_within"));
        return 0;
    }

    private static int testAnnouncementCommand(CommandSourceStack source, String type) {
        String playerName = source.getEntity() != null ? source.getEntity().getName().getString() : "Commander";
        String title;
        String message;
        int color;
        if ("doncella".equalsIgnoreCase(type)) {
            title = "✦ AWAKENED RELIC 5★ (0.02%)! ✦";
            message = playerName + " obtained: §dThe Immortal Maiden";
            color = 0xFFFF55FF;
        } else if ("gold".equalsIgnoreCase(type)) {
            title = "✦ 5★ CHARACTER OBTAINED! ✦";
            message = playerName + " obtained: §eRaiden Shogun";
            color = 0xFFFFCC00;
        } else {
            title = "✦ SUPREME MYTHIC FIND (0.008%)! ✦";
            message = playerName + " obtained: §cCreator's Claymore";
            color = 0xFFFFD700;
        }

        PacketDistributor.sendToAllPlayers(new com.soybe.awakeningsystem.network.SystemAnnouncementPayload(title, message, color));
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.global_announcement_test_announcement_sent_to", String.valueOf(title)), true);
        return 1;
    }

    private static int customAnnouncementCommand(CommandSourceStack source, String title, String message) {
        PacketDistributor.sendToAllPlayers(new com.soybe.awakeningsystem.network.SystemAnnouncementPayload(title, message, 0xFFFFD700));
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.global_announcement_custom_announcement_triggered", String.valueOf(title), String.valueOf(message)), true);
        return 1;
    }

    private static int openBlackMarketCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.blackmarket.BlackMarketServerHandler.openMarket(player);
            return 1;
        }
        source.sendFailure(Component.literal("Only players can open the Black Market interface."));
        return 0;
    }

    private static int locateBlackMarketCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can search for the Black Market."));
            return 0;
        }
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(source.getServer());
        BlockPos playerPos = player.blockPosition();
        BlockPos closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (var entry : data.getAll().entrySet()) {
            if (entry.getKey().startsWith("black_market_den")) {
                double dSq = entry.getValue().distSqr(playerPos);
                if (dSq < closestDistSq) {
                    closestDistSq = dSq;
                    closest = entry.getValue();
                }
            }
        }

        if (closest != null) {
            int dist = (int) Math.sqrt(closestDistSq);
            final BlockPos foundPos = closest;
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_nearest_underground_den_at", String.valueOf(foundPos.getX()), String.valueOf(foundPos.getY()), String.valueOf(foundPos.getZ()), String.valueOf(dist)), false);
            return 1;
        } else {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_no_den_has_been"), false);
            return 0;
        }
    }

    private static int warpBlackMarketCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can travel to the Black Market."));
            return 0;
        }
        var server = source.getServer();
        ServerLevel overworld = server.overworld();
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);
        BlockPos playerPos = player.blockPosition();

        BlockPos closest = null;
        double closestDistSq = Double.MAX_VALUE;
        for (var entry : data.getAll().entrySet()) {
            if (entry.getKey().startsWith("black_market_den")) {
                double dSq = entry.getValue().distSqr(playerPos);
                if (dSq < closestDistSq) {
                    closestDistSq = dSq;
                    closest = entry.getValue();
                }
            }
        }

        BlockPos denPos;
        if (closest != null) {
            denPos = closest;
        } else {
            int targetY = Math.min(20, Math.max(10, playerPos.getY() - 35));
            denPos = new BlockPos(playerPos.getX(), targetY, playerPos.getZ());
            com.soybe.awakeningsystem.world.SeriesStructureBuilder.buildBlackMarketDen(overworld, denPos);
            data.recordStructure("black_market_den", denPos);
        }

        BlockPos targetPos = denPos.offset(0, 1, 0);
        player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 0.0F, 0.0F);
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_you_have_descended_to", String.valueOf(denPos.getX()), String.valueOf(denPos.getY()), String.valueOf(denPos.getZ())), true);
        return 1;
    }

    private static int warpBlackMarketSurfaceCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can travel to the Black Market surface hatch."));
            return 0;
        }
        var server = source.getServer();
        ServerLevel overworld = server.overworld();
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(server);
        BlockPos playerPos = player.blockPosition();

        BlockPos closestHatch = null;
        double closestHatchDistSq = Double.MAX_VALUE;
        for (var entry : data.getAll().entrySet()) {
            if (entry.getKey().startsWith("black_market_surface_hatch")) {
                double dSq = entry.getValue().distSqr(playerPos);
                if (dSq < closestHatchDistSq) {
                    closestHatchDistSq = dSq;
                    closestHatch = entry.getValue();
                }
            }
        }

        if (closestHatch == null) {
            BlockPos closestDen = null;
            double closestDenDistSq = Double.MAX_VALUE;
            for (var entry : data.getAll().entrySet()) {
                if (entry.getKey().startsWith("black_market_den")) {
                    double dSq = entry.getValue().distSqr(playerPos);
                    if (dSq < closestDenDistSq) {
                        closestDenDistSq = dSq;
                        closestDen = entry.getValue();
                    }
                }
            }

            if (closestDen == null) {
                int targetY = Math.min(20, Math.max(10, playerPos.getY() - 35));
                closestDen = new BlockPos(playerPos.getX(), targetY, playerPos.getZ());
                com.soybe.awakeningsystem.world.SeriesStructureBuilder.buildBlackMarketDen(overworld, closestDen);
                data.recordStructure("black_market_den", closestDen);
            } else {
                com.soybe.awakeningsystem.world.SeriesStructureBuilder.buildBlackMarketSurfaceHut(overworld, closestDen.getX(), closestDen.getY(), closestDen.getZ());
            }

            for (var entry : data.getAll().entrySet()) {
                if (entry.getKey().startsWith("black_market_surface_hatch")) {
                    double dSq = entry.getValue().distSqr(playerPos);
                    if (dSq < closestHatchDistSq) {
                        closestHatchDistSq = dSq;
                        closestHatch = entry.getValue();
                    }
                }
            }
            if (closestHatch == null) {
                int sY = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, closestDen.getX(), closestDen.getZ() - 12);
                if (sY < 62) sY = 64;
                closestHatch = new BlockPos(closestDen.getX(), sY, closestDen.getZ() - 12);
            }
        }

        BlockPos targetPos = closestHatch.offset(0, 1, 0);
        player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 0.0F, 0.0F);
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        final BlockPos fHatch = closestHatch;
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_you_have_been_transported", String.valueOf(fHatch.getX()), String.valueOf(fHatch.getY()), String.valueOf(fHatch.getZ())), true);
        return 1;
    }

    private static int buildBlackMarketCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can construct a Black Market den."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos denPos = player.blockPosition().below();
        com.soybe.awakeningsystem.world.SeriesStructureBuilder.buildBlackMarketDen(level, denPos);
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(source.getServer());
        data.recordStructure("black_market_den", denPos);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_clandestine_den_built_at", String.valueOf(denPos.getX()), String.valueOf(denPos.getY()), String.valueOf(denPos.getZ())), true);
        return 1;
    }

    private static int exportBlackMarketCommand(CommandSourceStack source, String name, int radius) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can export the structure."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        java.io.File file = com.soybe.awakeningsystem.world.BlackMarketJsonHelper.exportStructure(level, origin, radius, name);
        if (file != null) {
            long size = file.length();
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_structure_exported_successfully_saved", String.valueOf(file.getName()), String.valueOf(size)), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.error_exporting_the_black_market_structure"));
            return 0;
        }
    }

    private static int importBlackMarketCommand(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can import the structure."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        int placed = com.soybe.awakeningsystem.world.BlackMarketJsonHelper.importStructure(level, origin, name);
        if (placed >= 0) {
            source.sendSuccess(() -> Component.translatable("awakening_system.msg.black_market_structure_loaded_successfully_placed", String.valueOf(name), String.valueOf(placed)), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("awakening_system.msg.file_not_found_in_config_awakening_2", String.valueOf(name)));
            return 0;
        }
    }

    private static int claimCreatorTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isCreator(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.creator.only_creator").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setCreator(true);
            data.equipTitle("creator");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.creator.title_screen")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.creator.subtitle_screen")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.sendSystemMessage(Component.literal("§6=================================================="));
            player.sendSystemMessage(Component.translatable("awakening_system.creator.privileges_activated"));
            player.sendSystemMessage(Component.translatable("awakening_system.msg.usuario_uuid", String.valueOf(player.getGameProfile().getName()), String.valueOf(player.getUUID())));
            player.sendSystemMessage(Component.translatable("awakening_system.creator.equipped"));
            player.sendSystemMessage(Component.literal("§6=================================================="));
            return 1;
        }
        return 0;
    }

    private static int claimNandaTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isNanda(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.nanda.only_nanda").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setNanda(true);
            data.equipTitle("michi_lover");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.nanda.title_screen")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.nanda.subtitle_screen")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.CAT_PURREOW, SoundSource.PLAYERS, 1.0F, 1.0F);
            return 1;
        }
        return 0;
    }

    private static int claimMisticPotatoTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isMisticPotato(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.potato.only_potato").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setMisticPotato(true);
            data.equipTitle("caffeine_addict");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.potato.title_screen")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.potato.subtitle_screen")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            return 1;
        }
        return 0;
    }

    private static int claimSchvisTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isSchvis(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.schvis.only_schvis").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setSchvis(true);
            data.equipTitle("futanari_hentai_master");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.schvis.title_screen")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.schvis.subtitle_screen")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            return 1;
        }
        return 0;
    }

    private static int claimOrthaxTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isWraith(player) && !TitleDefinitions.isCreator(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.orthax.only_wraith").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setWraith(true);
            data.equipTitle("orthax");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.title.orthax_unlocked.title")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.title.orthax_unlocked.subtitle")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            return 1;
        }
        return 0;
    }

    private static int claimTomcraftTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isTomcraft(player) && !TitleDefinitions.isCreator(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.pokemon.only_tomcraft").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setTomcraft(true);
            data.equipTitle("pokemon_master");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.pokemon.title_screen")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.pokemon.subtitle_screen")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.45F, 1.8F);
            return 1;
        }
        return 0;
    }

    private static int claimHatterTitle(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!TitleDefinitions.isTheRealHatter(player) && !TitleDefinitions.isCreator(player)) {
                player.sendSystemMessage(Component.translatable("awakening_system.hatter.only_hatter").withStyle(ChatFormatting.RED));
                return 0;
            }
            SystemData data = SystemEvents.data(player);
            data.setTheRealHatter(true);
            data.equipTitle("the_mad_hatter");
            AwakeningSystemMod.sync(player);
            AwakeningSystemMod.syncPlayerBadge(player);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.title.the_mad_hatter_unlocked.title")
            ));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.title.the_mad_hatter_unlocked.subtitle")
            ));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            return 1;
        }
        return 0;
    }

    private static int openWelcome(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenWelcomeScreenPayload());
            return 1;
        }
        return 0;
    }

    private static int startWorkoutCommand(CommandSourceStack source, int type) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cEste comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        SystemEvents.startWorkout(player, type);
        return 1;
    }

    private static int setSeasonCommand(CommandSourceStack source, String seasonStr) {
        if (!hasAdminOrCreatorPermission(source)) {
            source.sendFailure(Component.translatable("awakening_system.msg.acceso_denegado_solo_el_creador_del"));
            return 0;
        }

        com.soybe.awakeningsystem.seasonal.SeasonalEventManager.Season target =
                "off".equalsIgnoreCase(seasonStr) ? com.soybe.awakeningsystem.seasonal.SeasonalEventManager.Season.OFF :
                        com.soybe.awakeningsystem.seasonal.SeasonalEventManager.Season.fromString(seasonStr);

        com.soybe.awakeningsystem.seasonal.SeasonalEventManager.setDevOverride(target);

        source.sendSuccess(() -> Component.translatable("awakening_system.msg.seasonal_event_estado_de_temporada_fijado", String.valueOf(target.name()), String.valueOf((target == com.soybe.awakeningsystem.seasonal.SeasonalEventManager.Season.OFF ? " §7(Desactivado / Modo Calendario Real)" : " §a(Activado)"))), true);
        return 1;
    }

    private static int getSeasonStatusCommand(CommandSourceStack source) {
        var current = com.soybe.awakeningsystem.seasonal.SeasonalEventManager.getCurrentSeason();
        var override = com.soybe.awakeningsystem.seasonal.SeasonalEventManager.getDevOverride();
        source.sendSuccess(() -> Component.translatable("awakening_system.msg.seasonal_event_temporada_activa_actual", String.valueOf(current.name()), String.valueOf((override != null ? " §d[Forzado por Desarrollador: " + override.name() + "]" : " §b[Automático por Calendario Real]"))), false);
        return 1;
    }

    private static int startTribulationDevCommand(CommandSourceStack source, int realmArg, int strikesArg) {
        if (!hasAdminOrCreatorPermission(source)) {
            source.sendFailure(Component.translatable("awakening_system.msg.acceso_denegado_solo_el_creador_del"));
            return 0;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            SystemData data = SystemEvents.data(player);
            if (data == null) {
                source.sendFailure(Component.translatable("awakening_system.msg.error_no_se_encontro_perfil_de"));
                return 0;
            }

            int currentRealm = data.dantianRealm();
            int targetRealm = (realmArg >= 2 && realmArg <= 14) ? realmArg : Math.max(2, Math.min(14, currentRealm + 1));
            int strikes = strikesArg > 0 ? strikesArg : switch (targetRealm) {
                case 2 -> 3;
                case 3 -> 6;
                case 4 -> 9;
                case 5 -> 12;
                case 6 -> 15;
                case 7 -> 18;
                case 8 -> 27;
                case 9 -> 36;
                case 10 -> 45;
                case 11 -> 54;
                case 12 -> 63;
                case 13 -> 72;
                case 14 -> 81;
                default -> 3;
            };

            data.startTribulation(targetRealm, strikes);
            ServerLevel sl = player.serverLevel();
            sl.setWeatherParameters(0, strikes * 40 + 200, true, true);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.CELESTIAL_TRIBULATION.get(), SoundSource.WEATHER, 3.5F, 1.0F);
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getEyeY(), player.getZ(), 6, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 80, 1.5, 1.5, 1.5, 0.2);

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.translatable("awakening_system.msg.tribulacion_celestial")));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.translatable("awakening_system.msg.sobrevive_rayos_divinos_hacia_el_reino", String.valueOf(strikes), String.valueOf(targetRealm))));

            source.sendSuccess(() -> Component.translatable("awakening_system.msg.tribulacion_activada_juicio_del_cielo_iniciado", String.valueOf(targetRealm), String.valueOf(strikes)), true);

            AwakeningSystemMod.sync(player);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("awakening_system.msg.error_ejecutando_comando_de_tribulacion", String.valueOf(e.getMessage())));
            return 0;
        }
    }

    private static int openDiscordLink(CommandSourceStack source) {
        Component linkComp = Component.literal("§9§l✦ [DISCORD] §f¡Únete a la comunidad oficial del mod §b§lAwakening System§f en Discord!\n§9§l✦ §e§nhttps://discord.gg/DqzTE3Ux8\n§7(Haz clic aquí para abrir la invitación en tu navegador)")
                .withStyle(style -> style
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, "https://discord.gg/DqzTE3Ux8"))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("§aAbrir https://discord.gg/DqzTE3Ux8"))));
        source.sendSuccess(() -> linkComp, false);
        return 1;
    }

    private static int listCultivationRealmsCommand(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayer();
            SystemData data = player != null ? SystemEvents.data(player) : null;
            int curRealm = data != null ? data.dantianRealm() : 0;
            int curSub = data != null ? data.cultivationSubLevel() : 1;

            source.sendSuccess(() -> Component.literal("§6§l✦══════ [ JERARQUÍA DE REINOS DEL DAO CELESTIAL (1 - 14) ] ══════✦"), false);
            for (int r = 1; r <= 14; r++) {
                int realmNum = r;
                Component rName = SystemEvents.getDantianRealmComponent(realmNum);
                int stats = SystemEvents.getRealmBaseStats(realmNum);
                Component pill = SystemEvents.getRequiredPillComponent(realmNum);
                int strikes = SystemEvents.getTribulationStrikes(realmNum);

                net.minecraft.network.chat.MutableComponent line;
                if (realmNum == curRealm) {
                    line = Component.literal(" §e▶ §lReino " + realmNum + ": ").append(rName)
                            .append(Component.literal(" §6[TU REINO - Nv. " + curSub + "/9] §f(+" + stats + " Stats"));
                } else if (realmNum < curRealm) {
                    line = Component.literal(" §a✔ §fReino " + realmNum + ": ").append(rName)
                            .append(Component.literal(" §7(+" + stats + " Stats"));
                } else {
                    line = Component.literal(" §7🔒 Reino " + realmNum + ": ").append(rName)
                            .append(Component.literal(" §8(+" + stats + " Stats"));
                }

                if (strikes > 0) {
                    line.append(Component.literal(" | ⚡" + strikes + " Rayos"));
                }
                if (realmNum > 1) {
                    line.append(Component.literal(" | 💊 ").append(pill));
                }
                line.append(Component.literal(")"));

                source.sendSuccess(() -> line, false);
            }
            source.sendSuccess(() -> Component.literal("§6§l✦══════════════════════════════════════════════════════════════✦"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError al listar reinos: " + e.getMessage()));
            return 0;
        }
    }

    private static int getPassiveQiConfigCommand(CommandSourceStack source) {
        boolean enabled = SystemConfig.PASSIVE_QI_ENABLED.get();
        if (enabled) {
            source.sendSuccess(() -> Component.literal("§6[SISTEMA] §fRegeneración pasiva de Qi: §aACTIVADA §7(los cultivadores reciben Qi de fondo al estar conectados)"), false);
        } else {
            source.sendSuccess(() -> Component.literal("§6[SISTEMA] §fRegeneración pasiva de Qi: §cDESACTIVADA §7(el Qi solo se obtiene por cultivo activo, píldoras o combate)"), false);
        }
        return 1;
    }

    private static int setPassiveQiConfigCommand(CommandSourceStack source, boolean enabled) {
        if (!hasAdminOrCreatorPermission(source)) {
            source.sendFailure(Component.translatable("awakening_system.msg.acceso_denegado_solo_el_creador_del"));
            return 0;
        }
        SystemConfig.setPassiveQiEnabled(enabled);
        if (enabled) {
            source.sendSuccess(() -> Component.literal("§6[SISTEMA] §aRegeneración pasiva de Qi ACTIVADA con éxito. §7(Los cultivadores acumularán Qi automáticamente)").withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendSuccess(() -> Component.literal("§6[SISTEMA] §eRegeneración pasiva de Qi DESACTIVADA con éxito. §7(El Qi solo se obtendrá mediante cultivo activo o píldoras)").withStyle(ChatFormatting.YELLOW), true);
        }
        return 1;
    }
}
