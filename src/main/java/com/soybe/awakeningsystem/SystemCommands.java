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

        var systemBuilder = Commands.literal("system")
                .requires(source -> hasAdminOrCreatorPermission(source))
                .executes(context -> show(context.getSource()))
                .then(Commands.literal("creator").executes(context -> claimCreatorTitle(context.getSource())))
                .then(Commands.literal("nanda").executes(context -> claimNandaTitle(context.getSource())))
                .then(Commands.literal("misticpotato").executes(context -> claimMisticPotatoTitle(context.getSource())))
                .then(Commands.literal("potato").executes(context -> claimMisticPotatoTitle(context.getSource())))
                .then(Commands.literal("schvis").executes(context -> claimSchvisTitle(context.getSource())))
                .then(Commands.literal("tomcraft").executes(context -> claimTomcraftTitle(context.getSource())))
                .then(Commands.literal("stats").executes(context -> show(context.getSource())))
                .then(Commands.literal("give_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("unlock_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("max").executes(context -> giveAll(context.getSource())))
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
                        .then(Commands.literal("reset_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("respawn_boss")
                                .executes(context -> resetFloor10BossCommand(context.getSource())))
                        .then(Commands.literal("floor")
                                .then(Commands.argument("floorNum", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> warpTowerFloor(context.getSource(), IntegerArgumentType.getInteger(context, "floorNum"))))))
                .then(Commands.literal("cultivate").executes(context -> cultivate(context.getSource())))
                .then(Commands.literal("meditate").executes(context -> cultivate(context.getSource())))
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
                            context.getSource().sendSuccess(() -> Component.literal("§5✦ Dungeon Gate Ruins NBT structure placed at §e" + target.toShortString() + "§5!"), true);
                            return 1;
                        }))
                .then(Commands.literal("ruins")
                        .requires(source -> hasAdminOrCreatorPermission(source))
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            BlockPos target = p.blockPosition().relative(p.getDirection(), 5);
                            DungeonManager.buildDungeonGateRuins(p.serverLevel(), target);
                            context.getSource().sendSuccess(() -> Component.literal("§5✦ Dungeon Gate Ruins NBT structure placed at §e" + target.toShortString() + "§5!"), true);
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

        dispatcher.register(Commands.literal("resetprogress")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer sp) {
                        return resetPlayer(context.getSource(), sp);
                    } else {
                        context.getSource().sendFailure(Component.literal("Specify a player: /resetprogress <player>"));
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
        source.sendSuccess(() -> Component.literal("§6§l✦ [BANNERS RESET] §fThe promotional banner cycle has begun from §ePhase 1: " + phase1.bannerTitle() + " (" + phase1.franchise().displayName() + ") §fwith §a14 days remaining §f(336,000 ticks)."), true);
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
        source.sendSuccess(() -> Component.literal("§6§l✦ [PHASE CHANGED] §fSet to §ePhase " + phase + ": " + activeP.bannerTitle() + " (" + activeP.franchise().displayName() + ") §fwith §a14 days remaining§f."), true);
        return 1;
    }

    private static int show(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§b========== SYSTEM ==========\n" +
                "§fLevel: §e" + data.level() + " §7| §fEXP: §b" + data.experience() + "/" + data.nextLevelExperience() + "\n" +
                "§6Coins: §e" + data.coins() + " §8| §ePoints: §a" + data.attributePoints() + "\n" +
                "§c⚔ STR §f" + data.strength() + "  §a❤ VIT §f" + data.vitality() + "  §b⚡ AGI §f" + data.agility() + "\n" +
                "§d👁 PER §f" + data.perception() + "  §9🧠 INT §f" + data.intelligence() + "  §5🛡 WIL §f" + data.willpower()), false);
        return 1;
    }

    private static int showCoins(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Usage: /coins <amount> <player>"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§6§l[SYSTEM]§r Current balance: §e" + data.coins() + " §fAwakening coins. (Use: §e/coins <amount>§f)"), false);
        return 1;
    }

    private static int giveCoins(CommandSourceStack source, int amount, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.literal("You must specify a target player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.addCoins(amount);
        SystemEvents.checkAchievements(player, data);
        AwakeningSystemMod.sync(player);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shop.coin_title",
                "awakening_system.notification.shop.coin_message", 0xFFFFD866, amount);
        final ServerPlayer finalPlayer = player;
        source.sendSuccess(() -> Component.literal("§6§l[SYSTEM]§r Added §e" + amount + " §fcoins to §b"
                + finalPlayer.getGameProfile().getName() + "§f. Total: §e" + data.coins() + " §fcoins."), true);
        return 1;
    }

    private static int showCarats(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Usage: /carats <amount> [player]"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§6🥕 [TRACEN] §fCurrent Carats balance: §e" + data.getCarats() + " 🥕§f."), false);
        return 1;
    }

    private static int giveCarats(CommandSourceStack source, int amount, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.literal("You must specify a target player."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.addCarats(amount);
        AwakeningSystemMod.sync(player);
        final ServerPlayer finalPlayer = player;
        source.sendSuccess(() -> Component.literal("§6🥕 [TRACEN] §fGranted §e" + amount + " Carats 🥕 §fto §e"
                + finalPlayer.getGameProfile().getName() + "§f. Total: §e" + data.getCarats() + " 🥕§f."), true);
        return 1;
    }

    private static int giveAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemEvents.grantAll(player);
        SystemData data = SystemEvents.data(player);
        data.addCoins(1000000);
        var keyItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("awakening_system", "dungeon_key"));
        if (keyItem != null && keyItem != net.minecraft.world.item.Items.AIR) {
            player.getInventory().add(new ItemStack(keyItem, 16));
        }
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r Full unlock complete! Level " + data.level() + ", " + data.skills().size() + " skills unlocked at Level VI (active, passive, and manhwas), mythic titles, max affinities, 16 dungeon keys, and 1,000,000 coins."), true);
        return 1;
    }

    private static int resetPlayer(CommandSourceStack source, ServerPlayer target) {
        target.setData(AwakeningSystemMod.PLAYER_SYSTEM, new SystemData());
        SystemEvents.dismissAllShadows(target);
        SystemAttributeEffects.apply(target);
        target.setHealth(Math.min(target.getHealth(), target.getMaxHealth()));
        AwakeningSystemMod.sync(target);

        target.displayClientMessage(Component.translatable("awakening_system.command.reset_success"), true);
        source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r All progress has been reset for §e" + target.getGameProfile().getName() + "§r."), true);
        return 1;
    }

    private static int addAttribute(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        if (!SystemEvents.spendAttribute(player, name)) {
            source.sendFailure(Component.literal("Invalid attribute or no points available."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Attribute upgraded: " + name), false);
        return 1;
    }

    private static int inspect(CommandSourceStack source, ServerPlayer target) {
        SystemData data = SystemEvents.data(target);
        source.sendSuccess(() -> Component.literal("=== INSPECT: " + target.getGameProfile().getName() + " ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Level: " + data.level() + " | Prestige: " + data.prestigeLevel() + " | Coins: " + data.coins()).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("STR " + data.strength() + "  VIT " + data.vitality() + "  AGI " + data.agility()
                + "  PER " + data.perception() + "  INT " + data.intelligence() + "  WIL " + data.willpower()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Defeated Mobs: " + data.creaturesDefeated() + " | Bosses: " + data.bossKills()
                + " | Completed Quests: " + data.missionsCompleted()).withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal("Skills (" + data.skills().size() + "): " + data.skills().keySet()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
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

        source.sendSuccess(() -> Component.literal("=== SYSTEM LEADERBOARD ===").withStyle(ChatFormatting.GOLD), false);
        int rank = 1;
        for (ServerPlayer player : players) {
            if (rank > 5) break;
            SystemData d = SystemEvents.data(player);
            final int pos = rank;
            source.sendSuccess(() -> Component.literal("#" + pos + " " + player.getGameProfile().getName()
                    + " | Lvl: " + d.level() + " | Prestige: " + d.prestigeLevel()
                    + " | Mobs: " + d.creaturesDefeated()).withStyle(pos == 1 ? ChatFormatting.YELLOW : ChatFormatting.AQUA), false);
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
        source.sendSuccess(() -> Component.literal("§c§l[TRANCE MODE]§r Activated for 10 seconds. (+3.0 Damage and sweeping edge attacks enabled).").withStyle(ChatFormatting.RED), false);
        return 1;
    }

    private static int setCombo(CommandSourceStack source, int count) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setComboCount(count);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§e§l[COMBO]§r Counter set to: §f" + count), false);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setLevel(level);
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§a§l[LEVEL]§r Level set to: §e" + level + "§a. Attribute points calculated."), false);
        return 1;
    }

    private static int setSponsor(CommandSourceStack source, String constellationId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        if (!SystemEvents.selectConstellation(player, constellationId)) {
            source.sendFailure(Component.literal("Invalid constellation ID or level below 25. Available constellations: abyssal_black_dragon, demon_like_judge_of_fire, secretive_plotter, prisoner_of_the_golden_headband, master_of_the_underworld"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("§d§l[CONSTELLATION]§r Sponsor assigned: " + constellationId), false);
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
        source.sendSuccess(() -> Component.literal("§b§l[SCENARIO]§r Active scenario set to: " + scenarioId + " (Target: " + target + ")"), false);
        return 1;
    }

    private static int completeScenario(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.completeScenarioDirect();
        SystemEvents.announceScenarioComplete(player, data);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§6§l[SCENARIO]§r Scenario completed! Open the menu (Quests) to click [★ CLAIM]."), false);
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
        source.sendSuccess(() -> Component.literal("§b§l[SKILL]§r Skill learned: " + (def != null ? def.nameKey() : skillId)), false);
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
        for (int i = 0; i < 16; i++) data.recordAwakeningMining();
        for (int i = 0; i < 5; i++) data.recordAwakeningCombat();
        data.completeAwakening();
        data.addCoins(100);
        SystemEvents.awardExperience(player, data, 250);
        AwakeningSystemMod.notify(player, "awakening_system.notification.awakening_complete.title",
                "awakening_system.notification.awakening_complete.message", 0xFFFFD866);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§6§l[AWAKENING]§r System Awakening Quest completed!"), false);
        return 1;
    }

    private static int openSponsorSelection(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenSponsorScreenPayload(false));
        source.sendSuccess(() -> Component.literal("§6§l[SPONSORSHIP]§r Opening Constellation selection screen."), false);
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
            source.sendFailure(Component.literal("Specify a target player: /system constellation ascend <player> [name]"));
            return 0;
        }
        final ServerPlayer finalP = player;
        ConstellationAscension.triggerAscension(finalP, "System Administrative Command",
                (customModifier == null || customModifier.isBlank()) ? null : customModifier.trim());
        source.sendSuccess(() -> Component.literal("§d§l[STAR STREAM]§r Player §e" + finalP.getGameProfile().getName() + " §dhas ascended to Constellation."), true);
        return 1;
    }

    private static int demoteConstellation(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.literal("Specify a target player: /system constellation demote <player>"));
            return 0;
        }
        final ServerPlayer finalP = player;
        SystemData data = SystemEvents.data(finalP);
        data.setIsConstellation(false);
        data.setConstellationModifier("");
        AwakeningSystemMod.sync(finalP);
        source.sendSuccess(() -> Component.literal("§c§l[STAR STREAM]§r Constellation status of §e" + finalP.getGameProfile().getName() + " §chas been revoked."), true);
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
            source.sendFailure(Component.literal("§cYou are not a Constellation. You must ascend first through mythical events or transcendent feats."));
            return 0;
        }
        String clean = modifier.trim().replace("[", "").replace("]", "");
        data.setConstellationModifier(clean);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§d§l[STAR STREAM]§r Your True Name as a Constellation has been updated to: §e[" + clean + "§e]"), false);
        return 1;
    }

    private static int returnFromAstral(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        if (!ConstellationChannelHandler.isAstralWatching(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou are not in astral projection."));
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
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aMusk Reef and Spiral Abyss Island generated at " + pos.toShortString()), true);
            return 1;
        }
        boolean ok = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.generateStructure(level, pos, name);
        if (ok) {
            com.soybe.awakeningsystem.world.SeriesStructureWorldGen.checkAndEnsureNpcsPresent(level.getServer());
            source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r Themed structure '§e" + name + "§r' generated at " + pos.toShortString()), true);
            return 1;
        }

        boolean teyvatOk = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(level, pos, name);
        if (teyvatOk) {
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aTeyvat structure '§e" + name + "§a' successfully generated at " + pos.toShortString()), true);
            return 1;
        }

        source.sendFailure(Component.literal("§cUnknown structure type. Options:\n" +
                "§7Teyvat: §bgolden_house (Childe), tenshukaku (Raiden), wolflord (Wolflord), azhdaha, anemo_hypostasis, electro_hypostasis, geo_hypostasis, cryo_hypostasis, pyro_hypostasis, hydro_hypostasis, cryo_regisvine, anemo_shrine, geo_shrine, electro_shrine, dendro_shrine, statue, waypoint, camp, windrise, jade_chamber, beidou_ship, primal_temple, chasm_mine, karst, updraft, abyss.\n" +
                "§7Manhwa: §eabyss, solo_leveling, orv, estate, nano, tbate, ranker, mount_hua, valter, tower_of_god, ascension, etc."));
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
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aStructure '§e" + name + "§a' successfully generated at " + pos.toShortString()), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cUnrecognized Teyvat structure. Options: golden_house (Childe), tenshukaku (Raiden Shogun), wolflord (Wolflord), azhdaha, anemo_hypostasis, electro_hypostasis, geo_hypostasis, cryo_hypostasis, pyro_hypostasis, hydro_hypostasis, cryo_regisvine, anemo_shrine, geo_shrine, electro_shrine, dendro_shrine, statue, waypoint, camp, windrise, fix_windrise, fix_arenas, clean_arenas, jade_chamber, beidou_ship, primal_temple, chasm_mine, karst, updraft, abyss."));
            return 0;
        }
    }

    record TeyvatLocateTarget(String id, String displayName, BlockPos canonicalPos, String cmd) {}

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
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded. Travel first using /teyvat."));
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

            var warpBtn = Component.literal(" §e§l[TRAVEL & ENTER]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GOLD)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/teyvat " + finalTarget.cmd()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§aClick to " + (alreadyBuilt ? "travel" : "build and enter") + " " + finalTarget.displayName()))));

            var msg = Component.literal("§b§l✦ [TEYVAT] §aStructure: §e" + finalTarget.displayName() +
                    "\n§7Coordinates: §b[" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + "] §8(" + dist + "m) " +
                    (alreadyBuilt ? "§2✔ [Built]" : "§6★ [Canonical Site]") + " ")
                    .append(warpBtn);

            source.sendSuccess(() -> msg, false);
            return 1;
        }

        for (var entry : tData.namedStructures.entrySet()) {
            if (!q.isEmpty() && entry.getKey().contains(q)) {
                BlockPos p = entry.getValue();
                int dist = (int) Math.sqrt(p.distSqr(origin));
                var tpBtn = Component.literal(" §e§l[TELEPORT]")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true)
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                        "/tp @s " + p.getX() + " " + (p.getY() + 1) + " " + p.getZ()))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("§aClick to teleport"))));
                var msg = Component.literal("§b§l✦ [TEYVAT] §aRegistered structure: §e" + entry.getKey() + " §7at §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §8(" + dist + "m)").append(tpBtn);
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
                source.sendSuccess(() -> Component.literal("§e§l[TEYVAT]§r No " + finalFeatureLabel + "s have been discovered on the map yet. Explore new regions beyond 300 blocks."), false);
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
            var tpBtn = Component.literal(" §e§l[TELEPORT]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.YELLOW)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/tp @s " + fp.getX() + " " + (fp.getY() + 1) + " " + fp.getZ()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§aClick to teleport to " + finalFeatureLabel))));
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aNearest " + finalFeatureLabel + " at §b[" + fp.getX() + ", " + fp.getY() + ", " + fp.getZ() + "] §8(" + dist + "m)").append(tpBtn), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§e§l[TEYVAT]§r No structure found matching term '§c" + q + "§r'."), false);
        source.sendSuccess(() -> Component.literal("§7Use §b/teyvat list§7 to view the interactive catalog or travel directly to bosses:"), false);
        source.sendSuccess(() -> Component.literal("  §6✦ [Tartaglia / Childe] §7-> §e/teyvat golden_house"), false);
        source.sendSuccess(() -> Component.literal("  §d✦ [Raiden Shogun]      §7-> §e/teyvat tenshukaku"), false);
        source.sendSuccess(() -> Component.literal("  §6✦ [Golden Wolflord]    §7-> §e/teyvat wolflord"), false);
        source.sendSuccess(() -> Component.literal("  §e✦ [Azhdaha Dragon]     §7-> §e/teyvat azhdaha"), false);
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

        source.sendSuccess(() -> Component.literal("§b§l================ TEYVAT STRUCTURE CATALOG ================"), false);

        source.sendSuccess(() -> Component.literal("§6§l👑 WEEKLY BOSSES AND AUTHENTIC PALACES:"), false);
        sendLocateEntry(source, origin, "🏰 The Golden House (Tartaglia / Childe Boss)", "golden_house", TeyvatWorldManager.GOLDEN_HOUSE_POS.offset(0, 7, 4), tData);
        sendLocateEntry(source, origin, "⚡ Tenshukaku Palace (Raiden Shogun Boss)", "tenshukaku", TeyvatWorldManager.TENSHUKAKU_POS, tData);
        sendLocateEntry(source, origin, "🐺 Enkanomiya Island (Golden Wolflord)", "wolflord", TeyvatWorldManager.WOLFLORD_POS, tData);
        sendLocateEntry(source, origin, "🐉 Azhdaha's Underground Domain", "azhdaha", TeyvatWorldManager.AZHDAHA_POS, tData);

        source.sendSuccess(() -> Component.literal("§b§l💎 ELEMENTAL HYPOSTASES AND REGISVINE:"), false);
        sendLocateEntry(source, origin, "🍃 Anemo Hypostasis (Beth)", "anemo_hypostasis", TeyvatWorldManager.ANEMO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "⚡ Electro Hypostasis (Aleph)", "electro_hypostasis", TeyvatWorldManager.ELECTRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔶 Geo Hypostasis (Gimel)", "geo_hypostasis", TeyvatWorldManager.GEO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Cryo Hypostasis (Daleth)", "cryo_hypostasis", TeyvatWorldManager.CRYO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔥 Pyro Hypostasis (Ayin)", "pyro_hypostasis", TeyvatWorldManager.PYRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "💧 Hydro Hypostasis (He)", "hydro_hypostasis", TeyvatWorldManager.HYDRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Cryo Regisvine", "cryo_regisvine", TeyvatWorldManager.CRYO_REGISVINE_POS, tData);

        source.sendSuccess(() -> Component.literal("§a§l🌳 MONUMENTS AND NATION SEATS:"), false);
        sendLocateEntry(source, origin, "🌳 Great Sacred Oak of Windrise", "windrise", TeyvatWorldManager.WINDRISE_POS, tData);
        sendLocateEntry(source, origin, "🏯 Floating Jade Chamber", "jade_chamber", TeyvatWorldManager.JADE_CHAMBER_POS, tData);
        sendLocateEntry(source, origin, "🍷 Dawn Winery", "dawn_winery", TeyvatWorldManager.WINERY_POS, tData);
        sendLocateEntry(source, origin, "⛵ The Alcor (Beidou's Flagship)", "alcor", TeyvatWorldManager.ALCOR_POS, tData);
        sendLocateEntry(source, origin, "🌀 Spiral Abyss (Musk Reef)", "abyss", TeyvatWorldManager.MUSK_REEF_ISLAND_POS, tData);

        source.sendSuccess(() -> Component.literal("§e§l⛩ EXPLORATION AND RESOURCES:"), false);
        source.sendSuccess(() -> Component.literal("  §fShrines of Depths: §e" + tData.shrinesOfDepths.size() +
                " §7| §fStatues of the Seven: §e" + tData.statues.size() +
                " §7| §fWaypoints: §e" + tData.waypoints.size() +
                " §7| §fCamps: §e" + tData.camps.size()), false);
        source.sendSuccess(() -> Component.literal("§7Use §b/teyvat <name>§7 to travel directly to any structure or boss."), false);
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
                                Component.literal("§aClick to travel to " + displayName + (built ? "" : " (Will materialize instantly)")))));

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
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aMusk Reef Island and Spiral Abyss generated at " + pos.toShortString()), true);
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
        source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aSpiral Abyss Monument (13x13) successfully built at " + pos.toShortString() + " [" + (active ? "§bACTIVE§a" : "§cLOCKED§a") + "]"), true);
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
        source.sendSuccess(() -> Component.literal("§d✦ Teleporting to the Overworld Great Abyssal Cavern..."), false);
        com.soybe.awakeningsystem.world.AbyssalCaveBuilder.teleportToAbyssalCave(player);
        return 1;
    }

    private static int openAbyssMenu(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssMenuFromCommand(player);
            source.sendSuccess(() -> Component.literal("§b✦ [Spiral Abyss] Opening interface..."), false);
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
        source.sendSuccess(() -> Component.literal("§b§l✦ [SPIRAL ABYSS] §aStatus updated to: " + (active ? "§bACTIVE (Cosmic Vortex OPEN)" : "§cLOCKED (Portal SEALED)")), true);
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
            source.sendSuccess(() -> Component.literal("§b§l✦ [SPIRAL ABYSS] §aMusk Reef Island and portal successfully rebuilt."), true);
            return 1;
        }
        source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
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
            "witch_island", "teyvat_treehouses"
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
            source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r Teleported to existing structure: §e" + k + " §7at §b[" + landing.getX() + ", " + landing.getY() + ", " + landing.getZ() + "] §8(" + targetLevel.dimension().location().getPath() + ", " + distBlocks + "m)"), true);
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
            source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r Teleported to natural site of '§e" + naturalResult.canonicalName() + "§r' at §b[" + landing.getX() + ", " + landing.getY() + ", " + landing.getZ() + "] §7(Dimension: §b" + resultLevel.dimension().location().getPath() + "§7, Biome: §d" + naturalResult.biomeName() + "§7)"), true);
            return 1;
        }

        source.sendFailure(Component.literal("§cUnknown structure type: '§e" + name + "§c'.\n§7Use §b/structure list§7 to view registered structures."));
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
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("§aClick to teleport here"))));
            net.minecraft.network.chat.Component msg = Component.literal("§a§l[SYSTEM]§r Existing structure '§e" + found.getKey() + "§r' in (" + targetLevel.dimension().location().getPath() + "): ").append(coords).append(Component.literal(" §7(" + distBlocks + " blocks away)"));
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
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("§aClick to teleport here"))));
                net.minecraft.network.chat.Component msg = Component.literal("§6§l[SYSTEM]§r Natural structure '§e" + nat.canonicalName() + "§r' discovered in (" + nat.level().dimension().location().getPath() + "): ").append(coords).append(Component.literal(" §7(Biome: §d" + nat.biomeName() + "§7, " + distBlocks + "m away)"));
                source.sendSuccess(() -> msg, false);
                return 1;
            }
        }

        source.sendFailure(Component.literal("§cNo coordinates found within current range."));
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
            source.sendSuccess(() -> Component.literal("§e§l[SYSTEM]§r No structures have generated in this world yet. Explore new regions or use §b/structure tp <name>§r."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6§l=== REGISTERED STRUCTURES (" + all.size() + ") ==="), false);
        BlockPos origin = BlockPos.containing(source.getPosition());
        for (java.util.Map.Entry<String, BlockPos> entry : all.entrySet()) {
            BlockPos p = entry.getValue();
            int dist = (int) Math.sqrt(p.distSqr(origin));
            net.minecraft.network.chat.Component coords = Component.literal("§b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "]")
                .withStyle(style -> style
                    .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/structure tp " + entry.getKey()))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("§aClick to teleport here"))));
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
        source.sendSuccess(() -> Component.literal("§a§l[SYSTEM]§r NPC " + profile.titleColor() + profile.displayName() + "§r successfully summoned."), true);
        return 1;
    }

    private static int storeShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        int count = SystemEvents.storePlayerShadows(player);
        if (count > 0) {
            source.sendSuccess(() -> Component.literal("§8[§9Shadow Army§8] §aSaved §f" + count + " §asoldier(s) to your domain."), false);
        } else {
            source.sendFailure(Component.literal("§cYou have no shadow soldiers nearby in the field to store."));
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
            source.sendSuccess(() -> Component.literal("§8[§9Shadow Army§8] §bDeployed §f" + deployed + " §bsoldier(s) to battle."), false);
        } else {
            source.sendFailure(Component.literal("§cYou have no shadow soldiers in your domain reserve."));
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
        source.sendSuccess(() -> Component.literal("§8================ §9§lSHADOW DOMAIN §8================"), false);
        source.sendSuccess(() -> Component.literal("§7Active soldiers in field: §b" + finalActive), false);
        source.sendSuccess(() -> Component.literal("§7Stored soldiers in domain: §a" + stored), false);
        source.sendSuccess(() -> Component.literal("§7Current mana for regeneration: §d" + energy + " / 100 §f(Regen cost: 1 mana/s, revive: 15 mana)"), false);
        source.sendSuccess(() -> Component.literal("§7Quick controls: §e[V] §7to Extract/Deploy, §e[Shift + V] §7to Store in domain."), false);
        source.sendSuccess(() -> Component.literal("§8======================================================="), false);
        return 1;
    }

    private static int dismissShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }
        SystemEvents.dismissAllShadows(player);
        source.sendSuccess(() -> Component.literal("§8[§9Shadow Army§8] §eAll your active shadows have been dismissed."), false);
        return 1;
    }

    private static int spawnDokkaebiCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player."));
            return 0;
        }
        SystemEvents.spawnDokkaebiNearPlayer(player);
        source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r Dokkaebi successfully summoned beside you."), false);
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
            source.sendFailure(Component.literal("§cThere is no Dokkaebi broadcasting nearby. Wait for one to appear or use /dokkaebi spawn."));
            return 0;
        }
        var dokkaebi = dokkaebis.get(0);
        if (!dokkaebi.isQuestActive()) {
            source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r The Dokkaebi has not assigned you a Sub-Scenario yet. Right-click it to receive one."), false);
        } else {
            String title = (dokkaebi.getQuestTitle() != null && !dokkaebi.getQuestTitle().isEmpty()) ? dokkaebi.getQuestTitle() : "Sub-Scenario";
            String desc = (dokkaebi.getQuestDesc() != null) ? dokkaebi.getQuestDesc() : "";
            source.sendSuccess(() -> Component.literal("§8[§eStar Stream§8] §fSub-Escenario: §b" + title + " §7(" + dokkaebi.getQuestProgress() + "/" + dokkaebi.getQuestTarget() + ") §e- " + desc), false);
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
            source.sendFailure(Component.literal("§cThere is no Dokkaebi nearby to dismiss."));
            return 0;
        }
        for (var d : dokkaebis) {
            d.dismissWithParticles();
        }
        source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r The Dokkaebi has closed the broadcast."), false);
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
            source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r Server-wide Dokkaebi scenario successfully initiated for all active players."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cCould not start scenario: no eligible players found."));
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
                source.sendSuccess(() -> Component.literal("§c§l[Boss Spawned]§r Igris, the Bloodred Commander has appeared!"), true);
                return 1;
            }
        } else if ("baran".equalsIgnoreCase(type)) {
            var baran = com.soybe.awakeningsystem.entity.ModEntities.BARAN_BOSS.get().create(level);
            if (baran != null) {
                baran.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(baran);
                source.sendSuccess(() -> Component.literal("§b§l[Boss Spawned]§r Baran, Monarch of White Flames has appeared!"), true);
                return 1;
            }
        } else if ("patriarch".equalsIgnoreCase(type) || "patriarca".equalsIgnoreCase(type) || "blood_patriarch".equalsIgnoreCase(type)) {
            var patriarch = com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get().create(level);
            if (patriarch != null) {
                patriarch.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(patriarch);
                source.sendSuccess(() -> Component.literal("§4§l[Boss Spawned]§r Great Patriarch of the Blood Sect has appeared!"), true);
                return 1;
            }
        } else if ("treant".equalsIgnoreCase(type) || "yggdrasil_treant".equalsIgnoreCase(type)) {
            var treant = com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get().create(level);
            if (treant != null) {
                treant.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(treant);
                source.sendSuccess(() -> Component.literal("§2§l[Boss Spawned]§r Ancestral Treant of Yggdrasil has appeared!"), true);
                return 1;
            }
        } else if ("childe".equalsIgnoreCase(type) || "tartaglia".equalsIgnoreCase(type) || "nobile".equalsIgnoreCase(type)) {
            var childe = com.soybe.awakeningsystem.entity.ModEntities.CHILDE_BOSS.get().create(level);
            if (childe != null) {
                childe.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(childe);
                source.sendSuccess(() -> Component.literal("§b§l[Boss Spawned]§r Tartaglia (Childe), 11th of the Fatui Harbingers has appeared!"), true);
                return 1;
            }
        } else if ("raiden".equalsIgnoreCase(type) || "shogun".equalsIgnoreCase(type) || "baal".equalsIgnoreCase(type)) {
            var raiden = com.soybe.awakeningsystem.entity.ModEntities.RAIDEN_SHOGUN_BOSS.get().create(level);
            if (raiden != null) {
                raiden.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(raiden);
                source.sendSuccess(() -> Component.literal("§d§l[Boss Spawned]§r Raiden Shogun / Baal, God of Eternity has appeared!"), true);
                return 1;
            }
        } else if ("wolflord".equalsIgnoreCase(type) || "rey_acechador".equalsIgnoreCase(type) || "golden_wolflord".equalsIgnoreCase(type)) {
            var wolflord = com.soybe.awakeningsystem.entity.ModEntities.GOLDEN_WOLFLORD_BOSS.get().create(level);
            if (wolflord != null) {
                wolflord.moveTo(player.getX() + 2.0, player.getY() + 4.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(wolflord);
                source.sendSuccess(() -> Component.literal("§6§l[Boss Spawned]§r Golden Wolflord, Monarch of the Rift Hound Pack has appeared!"), true);
                return 1;
            }
        } else if ("azhdaha".equalsIgnoreCase(type)) {
            var azhdaha = com.soybe.awakeningsystem.entity.ModEntities.AZHDAHA_BOSS.get().create(level);
            if (azhdaha != null) {
                azhdaha.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(azhdaha);
                source.sendSuccess(() -> Component.literal("§6§l[Boss Spawned]§r Azhdaha, Sealed Primeval Dragon King has appeared!"), true);
                return 1;
            }
        } else if ("anemo_hypostasis".equalsIgnoreCase(type) || "anemo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§a§l[Boss Spawned]§r Anemo Hypostasis (Beth) has appeared!"), true);
                return 1;
            }
        } else if ("electro_hypostasis".equalsIgnoreCase(type) || "electro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§d§l[Boss Spawned]§r Electro Hypostasis (Aleph) has appeared!"), true);
                return 1;
            }
        } else if ("geo_hypostasis".equalsIgnoreCase(type) || "geo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§e§l[Boss Spawned]§r Geo Hypostasis (Gimel) has appeared!"), true);
                return 1;
            }
        } else if ("cryo_hypostasis".equalsIgnoreCase(type) || "cryo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.CRYO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§b§l[Boss Spawned]§r Cryo Hypostasis (Daleth) has appeared!"), true);
                return 1;
            }
        } else if ("pyro_hypostasis".equalsIgnoreCase(type) || "pyro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§c§l[Boss Spawned]§r Pyro Hypostasis (Ayin) has appeared!"), true);
                return 1;
            }
        } else if ("hydro_hypostasis".equalsIgnoreCase(type) || "hydro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.HYDRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§9§l[Boss Spawned]§r Hydro Hypostasis (He) has appeared!"), true);
                return 1;
            }
        } else if ("cryo_regisvine".equalsIgnoreCase(type) || "regisvine".equalsIgnoreCase(type)) {
            var r = com.soybe.awakeningsystem.entity.ModEntities.CRYO_REGISVINE.get().create(level);
            if (r != null) {
                r.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(r);
                source.sendSuccess(() -> Component.literal("§b§l[Boss Spawned]§r Cryo Regisvine has appeared!"), true);
                return 1;
            }
        } else if ("sculk".equalsIgnoreCase(type) || "warden".equalsIgnoreCase(type) || "colossus".equalsIgnoreCase(type) || "coloso".equalsIgnoreCase(type) || "sculk_warden".equalsIgnoreCase(type)) {
            var wardenBoss = com.soybe.awakeningsystem.entity.ModEntities.COLOSSAL_SCULK_WARDEN.get().create(level);
            if (wardenBoss != null) {
                wardenBoss.moveTo(player.getX() + 3.0, player.getY(), player.getZ() + 3.0, player.getYRot(), 0.0F);
                level.addFreshEntity(wardenBoss);
                source.sendSuccess(() -> Component.literal("§3§l[Boss Spawned]§r ¡El Titán Colosal de Sculk ha despertado!"), true);
                return 1;
            }
        } else if ("sculk_golem".equalsIgnoreCase(type) || "golem".equalsIgnoreCase(type)) {
            var golem = com.soybe.awakeningsystem.entity.ModEntities.SCULK_GOLEM.get().create(level);
            if (golem != null) {
                golem.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                golem.setStatue(false);
                level.addFreshEntity(golem);
                source.sendSuccess(() -> Component.literal("§3§l[Mob Spawned]§r Golem de Sculk invocado."), true);
                return 1;
            }
        } else if ("sculk_statue".equalsIgnoreCase(type) || "statue".equalsIgnoreCase(type)) {
            var golem = com.soybe.awakeningsystem.entity.ModEntities.SCULK_GOLEM.get().create(level);
            if (golem != null) {
                golem.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                golem.setStatue(true);
                level.addFreshEntity(golem);
                source.sendSuccess(() -> Component.literal("§7§l[Mob Spawned]§r Estatua durmiente de Golem de Sculk invocada."), true);
                return 1;
            }
        } else {
            source.sendFailure(Component.literal("§cJefe desconocido. Usa: /boss <warden | colossus | sculk_golem | igris | baran | patriarch | treant | childe | raiden | wolflord | azhdaha | anemo_hypostasis | electro_hypostasis | geo_hypostasis | cryo_hypostasis | pyro_hypostasis | hydro_hypostasis | cryo_regisvine>"));
            return 0;
        }
        return 0;
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
            source.sendSuccess(() -> Component.literal("§a[Sistema] ¡Fachada y estructura monumental de la Torre generadas con éxito en X=" + pos.getX() + ", Z=" + pos.getZ() + "!"), true);
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
                source.sendSuccess(() -> Component.literal("§a[Sistema] ¡Torre exportada exitosamente! §e(" + count + " bloques guardados en config/awakening_system/custom_tower/tower_exterior.json)"), true);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cError al exportar la torre. Revisa los logs."));
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
                source.sendSuccess(() -> Component.literal("§a[Sistema] Archivo custom eliminado. Ahora la torre se generará con el diseño procedural por defecto."), true);
                return 1;
            } else {
                source.sendSuccess(() -> Component.literal("§e[Sistema] No había ningún archivo custom activo. Ya se usa el diseño procedural por defecto."), false);
                return 1;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError al eliminar archivo custom: " + e.getMessage()));
            return 0;
        }
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
            source.sendSuccess(() -> Component.literal("§a[Sistema] §fEl Titán Colosal de Sculk ha sido reaparecido en la arena del Piso 10."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§e[Sistema] §fEl estado del Titán Colosal ha sido reiniciado a no derrotado."), true);
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
                source.sendSuccess(() -> Component.literal("§a✔ System 4.0 update completed instantly without waiting."), true);
                return 1;
            } else if (data.isSystem30Updating()) {
                MurimWorldManager.completeSystem30Update(player, data);
                source.sendSuccess(() -> Component.literal("§a✔ System 3.0 update completed instantly without waiting."), true);
                return 1;
            } else if (data.isSystemUpdating()) {
                IsekaiWorldManager.completeSystemUpdate(player, data);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.complete_update_success"), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cThere is no update in progress. You can force the version with: /system version <1|2|3|4>"));
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
            source.sendSuccess(() -> Component.literal("§e[SYSTEM] §aVersion switched instantly to: §b§lSystem 1.0 (Awakening Base)"), true);
            return 1;
        } else if (v.equals("2") || v.equals("2.0") || v.equals("isekai") || v.equals("v2")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(false);
            data.setMurimUnlocked(false);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SYSTEM] §aVersion switched instantly to: §d§lSystem 2.0 (Isekai & Constellations)"), true);
            return 1;
        } else if (v.equals("3") || v.equals("3.0") || v.equals("murim") || v.equals("v3")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SYSTEM] §aVersion switched instantly to: §a§lSystem 3.0 (Murim & Xianxia Cultivation)"), true);
            return 1;
        } else if (v.equals("4") || v.equals("4.0") || v.equals("teyvat") || v.equals("v4")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(true);
            data.setTeyvatUnlocked(true);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SYSTEM] §aVersion switched instantly to: §6§lSystem 4.0 (Teyvat & Celestial Impact)"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cInvalid version. Use: /system version <1 | 2 | 3 | 4>"));
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
            source.sendSuccess(() -> Component.literal("§b[Teyvat] Traveling to Teyvat Ceremonial Plaza..."), true);
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
                source.sendFailure(Component.literal("§cUnrecognized destination. Available options:\n" +
                        "§6Weekly Bosses: §e/teyvat golden_house §7(Childe), §e/teyvat tenshukaku §7(Raiden), §e/teyvat wolflord §7(Wolflord), §e/teyvat azhdaha\n" +
                        "§bHypostases: §e/teyvat anemo_hypostasis§7, §eelectro_hypostasis§7, §egeo_hypostasis§7, §ecryo_hypostasis§7, §epyro_hypostasis§7, §ehydro_hypostasis§7, §ecryo_regisvine\n" +
                        "§aLandmarks & Monuments: §e/teyvat windrise§7, §ejade_chamber§7, §edawn_winery§7, §ealcor§7, §eabyss§7, §emondstadt§7, §esumeru§7, §efontaine\n" +
                        "§6Grand Casino of Teyvat: §e/teyvat casino §7(or /casino / /system casino build)\n" +
                        "§dUma Racetrack: §e/teyvat racetrack §7(or /system racetrack rebuild / start)"));
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
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.buildCasinoIfAbsent(teyvat);
        TeyvatWorldManager.teleportToCasino(player);
        source.sendSuccess(() -> Component.literal("§6✦ [Grand Casino of Teyvat] §aYou have teleported to the Grand Royal Casino esplanade of Teyvat!"), true);
        return 1;
    }

    private static int rebuildCasinoCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.forceRebuild(teyvat);
        source.sendSuccess(() -> Component.literal("§6✦ [Grand Casino of Teyvat] §aMonte Carlo Casino monumental structure successfully rebuilt!"), true);
        return 1;
    }

    private static int clearCasinoFurnitureCommand(CommandSourceStack source, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        int cleared = com.soybe.awakeningsystem.casino.CasinoJsonHelper.clearFurniture(level, origin, radius);
        source.sendSuccess(() -> Component.literal("§6✦ [Casino] §aCleared §e" + cleared + " §afurniture in a radius of §e" + radius + " §ablocks around (" + origin.getX() + ", " + origin.getY() + ", " + origin.getZ() + "). Ready to arrange as you wish!"), true);
        return cleared;
    }

    private static int exportCasinoFurnitureCommand(CommandSourceStack source, String layoutName, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        java.io.File file = com.soybe.awakeningsystem.casino.CasinoJsonHelper.exportFurniture(level, origin, radius, layoutName);
        if (file != null) {
            source.sendSuccess(() -> Component.literal("§a✅ Casino furniture successfully exported to: §econfig/awakening_system/custom_casino/" + file.getName() + "§a!"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cError exporting casino configuration file."));
            return 0;
        }
    }

    private static int importCasinoFurnitureCommand(CommandSourceStack source, String layoutName) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        int placed = com.soybe.awakeningsystem.casino.CasinoJsonHelper.importFurniture(level, origin, layoutName);
        if (placed >= 0) {
            source.sendSuccess(() -> Component.literal("§a✅ Layout §e" + layoutName + " §asuccessfully loaded! Placed §e" + placed + " §afurniture and machines."), true);
            return placed;
        } else {
            source.sendFailure(Component.literal("§cFile not found: §e" + layoutName + "§c in config/awakening_system/custom_casino/"));
            return 0;
        }
    }

    private static int populateCasinoFurnitureCommand(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos origin = (source.getEntity() instanceof ServerPlayer sp) ? sp.blockPosition() : com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.getOrFindCasinoPos(level);
        com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.populateDefaultFurniture(level, origin);
        source.sendSuccess(() -> Component.literal("§6✦ [Casino] §aOfficial regulation furniture (Slots, Blackjack 2x2, Roulette 2x3, Pool 3x4, Chairs, and Trophies) successfully deployed!"), true);
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
                Component.literal("§6§l✦ CASINO MEGA JACKPOT! §ePlayer §f" + player.getName().getString() +
                        " §ehas claimed their legitimate prize for §6TRIPLE 777 §ewith Platinum Chip: §6+50,000,000 Mora, 10 Platinum Chips, 1,600 Primogems, and Legendary Trophy!"), false);

        player.sendSystemMessage(Component.literal("§a§l✔ [Awakening Casino] §eYou have successfully claimed all complete rewards from the Grand Platinum 777 Jackpot!"));
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
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.buildRacetrackIfAbsent(teyvat);
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.pruneDuplicateRunners();
        }
        player.teleportTo(teyvat, 122.5, 68.0, 224.5, 180.0F, 0.0F);
        source.sendSuccess(() -> Component.literal("§6🏇 [Teyvat Racetrack] §aYou have teleported to the Official Tracen Betting Pavilion!"), true);
        return 1;
    }

    private static int upgradeRacetrackMaterialsCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
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
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
            return 0;
        }
        com.soybe.awakeningsystem.world.TeyvatRacetrackBuilder.forceRebuild(teyvat);
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.setupNewRace();
        }
        source.sendSuccess(() -> Component.literal("§6🏇 [Teyvat Racetrack] §aTrack completely rebuilt without gaps, tree clearing done, and starting gates ready!"), true);
        return 1;
    }

    private static int startRacetrackCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.literal("§cTeyvat dimension is not loaded."));
            return 0;
        }
        var manager = com.soybe.awakeningsystem.uma.TeyvatRacetrackManager.get(teyvat);
        if (manager != null) {
            manager.startRaceNow();
            source.sendSuccess(() -> Component.literal("§6🏇 [Teyvat Racetrack] §eRace countdown forcefully started!"), true);
            return 1;
        }
        source.sendFailure(Component.literal("§cCould not access the racetrack manager."));
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
            source.sendSuccess(() -> Component.literal("§a✅ Track successfully exported! Scanned §e" + finalCount +
                    " blocks §ain: §b" + exportPath.getFileName() +
                    "§a. Mod generator and wizard can now read your changes directly."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError saving exported file: " + e.getMessage()));
            return 0;
        }
    }

    private static int rebuildCapitalsCommand(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e[TEYVAT] Artificial capital structures have been permanently removed and disabled. The world generates its natural terrain cleanly."), false);
        return 1;
    }

    private static int syncTeyvatMapCommand(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e[Teyvat] External region injection is disabled. The dimension generates its natural terrain cleanly."), false);
        return 1;
    }

    private static int openGachaCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            GachaManager.openGachaScreen(player);
            source.sendSuccess(() -> Component.literal("§6[Gacha] Opening Teyvat Wish System interface..."), false);
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
        source.sendSuccess(() -> Component.literal("§6§l[PAGODA] §aArena del Piso " + stage + " generada en §e" + origin.toShortString() + "§a!\n§eSe coloco un §bBloque de Estructuras §epreconfigurado frente a la puerta para guardar como §bawakening_system:pagoda_arena_stage_" + stage + "§e (o §bawakening_system:pagoda_arena§e)."), true);
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
        source.sendSuccess(() -> Component.literal("§e✦ STAR STREAM CHRONICLES §8| §f" + player.getName().getString()), false);
        source.sendSuccess(() -> Component.literal("§7Unlocked Fables: §e" + data.unlockedFables().size() + "/61"), false);
        source.sendSuccess(() -> Component.literal("§7Total Narrative Weight: §b" + data.fableWeight() + " pts"), false);
        source.sendSuccess(() -> Component.literal("§7Eligible for Constellation: " + (data.isConstellation() ? "§e[Already Ascended]" : (ConstellationAscension.isEligibleForAscension(data) ? "§a✔ [ELIGIBLE]" : "§c✖ [Missing requirements: Lv 50+, 8+ fables, 400+ weight, 1 Mythic/Giant, 2 Legendary/Semi, 3 Historical]"))), false);
        source.sendSuccess(() -> Component.literal("§8----------------------------------------"), false);
        for (String fid : data.unlockedFables()) {
            var f = com.soybe.awakeningsystem.fable.FableDefinitions.get(fid);
            if (f != null) {
                source.sendSuccess(() -> Component.literal(" §e" + f.icon() + " " + f.grade().colorCode() + "[" + f.title() + "] §8(+§b" + f.weight() + " pts§8)" + (f.isWorldFirst() ? " §6[★ WORLD FIRST]" : "")), false);
            }
        }
        source.sendSuccess(() -> Component.literal("§7Open System menu (§eH§7) and click on §6[FABLES]§7 for the full interface."), false);
        source.sendSuccess(() -> Component.literal("§6========================================"), false);
        return 1;
    }

    private static int grantFableCommand(CommandSourceStack source, String fableId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        var fable = com.soybe.awakeningsystem.fable.FableDefinitions.get(fableId);
        if (fable == null) {
            source.sendFailure(Component.literal("§cFable not found: " + fableId));
            return 0;
        }
        SystemEvents.grantFable(player, fableId);
        source.sendSuccess(() -> Component.literal("§aFable granted: " + fable.title()), false);
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
            source.sendSuccess(() -> Component.literal("§d✦ Columbina's Blessing (Enhanced +50%) successfully activated for " + player.getName().getString() + "."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§7✦ Welkin Moon Blessing restored to standard (90 primos/day)."), true);
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
        source.sendSuccess(() -> Component.literal("§d✦ Columbina Ascension Quest completed."), true);
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
        source.sendSuccess(() -> Component.literal("§e✦ Columbina Quest progress reset to 0."), true);
        return 1;
    }

    private static int addWelkinDaysCommand(CommandSourceStack source, int days) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        data.addWelkinDays(days);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§a✦ Added " + days + " days of Welkin Moon Blessing. Total days: " + data.welkinDays()), true);
        return 1;
    }

    private static int spawnWelkinSanctuaryCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        net.minecraft.core.BlockPos center = player.blockPosition().below();
        TrilunarSanctuaryManager.buildSanctuary(level, center);
        player.teleportTo(center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D);
        source.sendSuccess(() -> Component.literal("§d✦ Sanctuary of the Three Moon Goddesses (Aria, Sonnet, Canon) successfully manifested."), true);
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
                source.sendSuccess(() -> Component.literal("§a💾 Track successfully exported to JSON!: §econfig/awakening_system/custom_racetracks/" + file.getName()), true);
                return 1;
            }
        }
        source.sendFailure(Component.literal("§cNo Custom Racetrack Core found within 32 blocks radius."));
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
                source.sendSuccess(() -> Component.literal("§a✔ Track successfully reloaded from §e" + fileName + " §a(" + wpCount + " waypoints)."), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cCould not load file §e" + fileName + "§c in config/awakening_system/custom_racetracks/"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("§cNo Custom Racetrack Core found within 32 blocks radius."));
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
        source.sendSuccess(() -> Component.literal("§6✦ [Global Announcement] §aTest announcement sent to all players: §f" + title), true);
        return 1;
    }

    private static int customAnnouncementCommand(CommandSourceStack source, String title, String message) {
        PacketDistributor.sendToAllPlayers(new com.soybe.awakeningsystem.network.SystemAnnouncementPayload(title, message, 0xFFFFD700));
        source.sendSuccess(() -> Component.literal("§6✦ [Global Announcement] §aCustom announcement triggered: §f" + title + " §7- " + message), true);
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
            source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aNearest underground den at: §eX=" + foundPos.getX() + ", Y=" + foundPos.getY() + ", Z=" + foundPos.getZ() + " §7(Distance: §d" + dist + " blocks§7). Use §d/black_market tp §7to teleport!"), false);
            return 1;
        } else {
            source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §eNo den has been registered in this world yet. Generates in the underground Overworld (Y=10 to 20) beneath forests, swamps, or jungles, or run §d/black_market tp §eto locate or spawn one immediately."), false);
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
        source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aYou have descended to the clandestine den of Valerius the Silent at (" + denPos.getX() + ", " + denPos.getY() + ", " + denPos.getZ() + ")!"), true);
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
        source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aYou have been transported to the ground-level surface hatch at (" + fHatch.getX() + ", " + fHatch.getY() + ", " + fHatch.getZ() + ")! Open the trapdoor on the ground to descend into the shaft."), true);
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

        source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aClandestine den built at §e(" + denPos.getX() + ", " + denPos.getY() + ", " + denPos.getZ() + ") §awith Valerius the Silent ready to trade!"), true);
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
            source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aStructure exported successfully! Saved to: §econfig/awakening_system/custom_black_market/" + file.getName() + " §7(" + size + " bytes)"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cError exporting the Black Market structure."));
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
            source.sendSuccess(() -> Component.literal("§5☠ [Black Market] §aStructure §e" + name + " §aloaded successfully! Placed §e" + placed + " §ablocks."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFile not found: §e" + name + "§c in config/awakening_system/custom_black_market/"));
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
            player.sendSystemMessage(Component.literal("§fUsuario: §e" + player.getGameProfile().getName() + " §7(UUID: " + player.getUUID() + ")"));
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
}
