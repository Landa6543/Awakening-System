package com.soybe.awakeningsystem;

import com.mojang.brigadier.CommandDispatcher;
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

    public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Registrar comandos de Gremio / Clan
        com.soybe.awakeningsystem.guild.GuildCommands.register(dispatcher);

        // 1. Comando principal /system
        var systemBuilder = Commands.literal("system")
                .requires(source -> source.hasPermission(2))
                .executes(context -> show(context.getSource()))
                .then(Commands.literal("stats").executes(context -> show(context.getSource())))
                .then(Commands.literal("give_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("unlock_all").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("max").executes(context -> giveAll(context.getSource())))
                .then(Commands.literal("tower")
                        .executes(context -> enterTower(context.getSource()))
                        .then(Commands.literal("floor")
                                .then(Commands.argument("floorNum", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> warpTowerFloor(context.getSource(), IntegerArgumentType.getInteger(context, "floorNum"))))))
                .then(Commands.literal("ranker_tower")
                        .executes(context -> enterTower(context.getSource()))
                        .then(Commands.literal("floor")
                                .then(Commands.argument("floorNum", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> warpTowerFloor(context.getSource(), IntegerArgumentType.getInteger(context, "floorNum"))))))
                .then(Commands.literal("cultivate").executes(context -> cultivate(context.getSource())))
                .then(Commands.literal("meditate").executes(context -> cultivate(context.getSource())))
                .then(Commands.literal("isekai").executes(context -> warpIsekai(context.getSource())))
                .then(Commands.literal("murim").executes(context -> warpMurim(context.getSource())))
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
                        .then(Commands.literal("upgrade").executes(context -> upgradeSubspaceCommand(context.getSource())))
                        .then(Commands.literal("mejorar").executes(context -> upgradeSubspaceCommand(context.getSource()))))
                .then(Commands.literal("subspace")
                        .executes(context -> openSubspaceCommand(context.getSource()))
                        .then(Commands.literal("enter").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("entrar").executes(context -> enterSubspaceCommand(context.getSource())))
                        .then(Commands.literal("exit").executes(context -> exitSubspaceCommand(context.getSource())))
                        .then(Commands.literal("salir").executes(context -> exitSubspaceCommand(context.getSource())))
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
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500))
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
                        .then(Commands.argument("name", StringArgumentType.word())
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
                        .then(Commands.literal("warp")
                                .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                        .executes(context -> warpPagodaStage(context.getSource(), IntegerArgumentType.getInteger(context, "stage"))))));

        dispatcher.register(systemBuilder);

        // Atajo directo /sombras y /shadows para gestión del ejército del Monarca
        var shadowsBuilder = Commands.literal("sombras")
                .executes(context -> showShadowStatus(context.getSource()))
                .then(Commands.literal("guardar").executes(context -> storeShadowsCommand(context.getSource())))
                .then(Commands.literal("sacar").executes(context -> deployShadowsCommand(context.getSource())))
                .then(Commands.literal("estado").executes(context -> showShadowStatus(context.getSource())))
                .then(Commands.literal("liberar").executes(context -> dismissShadowsCommand(context.getSource())));
        dispatcher.register(shadowsBuilder);

        dispatcher.register(Commands.literal("shadows")
                .redirect(dispatcher.getRoot().getChild("sombras")));

        // Atajo directo /subespacio y /subspace para acceso al inventario dimensional
        dispatcher.register(Commands.literal("subespacio")
                .executes(context -> openSubspaceCommand(context.getSource())));
        dispatcher.register(Commands.literal("subspace")
                .redirect(dispatcher.getRoot().getChild("subespacio")));

        // Atajo directo /structure <name|locate|list>
        dispatcher.register(Commands.literal("structure")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("locate")
                        .executes(context -> locateStructureCommand(context.getSource(), null))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(context -> locateStructureCommand(context.getSource(), StringArgumentType.getString(context, "type")))))
                .then(Commands.literal("list")
                        .executes(context -> listStructuresCommand(context.getSource())))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> spawnStructureCommand(context.getSource(), StringArgumentType.getString(context, "name")))));

        // Atajo directo /npc <character>
                // Atajo directo /dokkaebi <spawn|quest|dismiss>
        dispatcher.register(Commands.literal("dokkaebi")
                .executes(context -> showDokkaebiQuestCommand(context.getSource()))
                .then(Commands.literal("spawn")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawnDokkaebiCommand(context.getSource())))
                .then(Commands.literal("quest")
                        .executes(context -> showDokkaebiQuestCommand(context.getSource())))
                .then(Commands.literal("scenario")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> triggerDokkaebiScenarioCommand(context.getSource())))
                .then(Commands.literal("dismiss")
                        .executes(context -> dismissDokkaebiCommand(context.getSource()))));

        dispatcher.register(Commands.literal("boss")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .executes(context -> spawnBossCommand(context.getSource(), StringArgumentType.getString(context, "type")))));

        dispatcher.register(Commands.literal("abyss")
                .executes(context -> warpAbyss(context.getSource()))
                .then(Commands.literal("menu").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("gui").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("open").executes(context -> openAbyssMenu(context.getSource())))
                .then(Commands.literal("active").requires(s -> s.hasPermission(2)).executes(context -> setAbyssStateCommand(context.getSource(), true)))
                .then(Commands.literal("unlock").requires(s -> s.hasPermission(2)).executes(context -> setAbyssStateCommand(context.getSource(), true)))
                .then(Commands.literal("locked").requires(s -> s.hasPermission(2)).executes(context -> setAbyssStateCommand(context.getSource(), false)))
                .then(Commands.literal("lock").requires(s -> s.hasPermission(2)).executes(context -> setAbyssStateCommand(context.getSource(), false)))
                .then(Commands.literal("rebuild").requires(s -> s.hasPermission(2)).executes(context -> rebuildAbyssCommand(context.getSource()))));

        dispatcher.register(Commands.literal("npc")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("character", StringArgumentType.word())
                        .executes(context -> spawnNpcCommand(context.getSource(), StringArgumentType.getString(context, "character")))));

        // Comando /escape accesible para TODOS los jugadores sin permisos OP
        dispatcher.register(Commands.literal("escape")
                .executes(context -> escapeCommand(context.getSource())));

        // Comando /dungeon con subcomando /dungeon exit y /dungeon escape para todos los jugadores
        dispatcher.register(Commands.literal("dungeon")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("start")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> triggerDungeon(context.getSource(), "D", "crypt"))
                        .then(Commands.argument("rankOrTheme", StringArgumentType.word())
                                .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), null))
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), StringArgumentType.getString(context, "theme"))))))
                .then(Commands.argument("rankOrTheme", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), null))
                        .then(Commands.argument("theme", StringArgumentType.word())
                                .executes(context -> triggerDungeon(context.getSource(), StringArgumentType.getString(context, "rankOrTheme"), StringArgumentType.getString(context, "theme"))))));

        // Comando /tower exit para todos los jugadores
        dispatcher.register(Commands.literal("tower")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("enter")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> enterTower(context.getSource()))));

        // Alias en español /sistema que redirige a /system
        dispatcher.register(Commands.literal("sistema")
                .redirect(dispatcher.getRoot().getChild("system")));

        // Atajo directo /murim
        dispatcher.register(Commands.literal("murim")
                .requires(source -> source.hasPermission(2))
                .executes(context -> warpMurim(context.getSource())));

        // Atajo directo /pagoda
        dispatcher.register(Commands.literal("pagoda")
                .then(Commands.literal("exit").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("salir").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escapeCommand(context.getSource())))
                .then(Commands.literal("enter")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> enterPagodaTrial(context.getSource())))
                .then(Commands.literal("entrar")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> enterPagodaTrial(context.getSource())))
                .then(Commands.literal("warp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                .executes(context -> warpPagodaStage(context.getSource(), IntegerArgumentType.getInteger(context, "stage"))))));

        // Atajo directo para que las Constelaciones puedan renombrarse o regresar de proyección astral
        dispatcher.register(Commands.literal("constelacion")
                .then(Commands.literal("nombre")
                        .then(Commands.argument("modifier", StringArgumentType.greedyString())
                                .executes(context -> renameConstellation(context.getSource(), StringArgumentType.getString(context, "modifier")))))
                .then(Commands.literal("regresar")
                        .executes(context -> returnFromAstral(context.getSource()))));

        // Atajo directo /guide para todos los jugadores
        dispatcher.register(Commands.literal("guide")
                .executes(context -> openGuide(context.getSource())));
        dispatcher.register(Commands.literal("bestiary")
                .executes(context -> openBestiary(context.getSource())));
        dispatcher.register(Commands.literal("bestiario")
                .executes(context -> openBestiary(context.getSource())));

        // Atajo directo /gacha, /wish, /deseos para todos los jugadores
        dispatcher.register(Commands.literal("gacha")
                .executes(context -> openGachaCommand(context.getSource())));
        dispatcher.register(Commands.literal("wish")
                .executes(context -> openGachaCommand(context.getSource())));
        dispatcher.register(Commands.literal("deseos")
                .executes(context -> openGachaCommand(context.getSource())));

        // Atajo directo /teyvat [mondstadt | liyue | inazuma | sumeru | fontaine | spawn | abyss | rebuild]
        dispatcher.register(Commands.literal("teyvat")
                .requires(source -> source.hasPermission(2))
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

        // Atajo directo /tp_teyvat [destination]
        dispatcher.register(Commands.literal("tp_teyvat")
                .requires(source -> source.hasPermission(2))
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
                .then(Commands.literal("spawn").executes(context -> warpTeyvat(context.getSource())))
                .then(Commands.literal("abyss").executes(context -> warpAbyss(context.getSource())))
                .then(Commands.argument("capital", StringArgumentType.word())
                        .executes(context -> warpTeyvatCapital(context.getSource(), StringArgumentType.getString(context, "capital")))));

        // Atajo directo /resetprogress para reiniciar progreso personal o de terceros
        dispatcher.register(Commands.literal("resetprogress")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer sp) {
                        return resetPlayer(context.getSource(), sp);
                    } else {
                        context.getSource().sendFailure(Component.literal("Especifica un jugador: /resetprogress <jugador>"));
                        return 0;
                    }
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> resetPlayer(context.getSource(), EntityArgument.getPlayer(context, "target")))));

        // Atajo directo /trance
        dispatcher.register(Commands.literal("trance")
                .requires(source -> source.hasPermission(2))
                .executes(context -> triggerTrance(context.getSource())));

        // Atajo directo /cultivate y /meditate
        dispatcher.register(Commands.literal("cultivate")
                .executes(context -> cultivate(context.getSource())));
        dispatcher.register(Commands.literal("meditate")
                .executes(context -> cultivate(context.getSource())));

        // Atajo directo /worldtree y /arboldelmundo
        dispatcher.register(Commands.literal("worldtree")
                .requires(source -> source.hasPermission(2))
                .executes(context -> warpWorldTree(context.getSource(), null))
                .then(Commands.literal("procedural")
                        .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                .then(Commands.literal("nbt")
                        .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                .then(Commands.literal("rebuild")
                        .executes(context -> warpWorldTree(context.getSource(), "rebuild"))));

        dispatcher.register(Commands.literal("arboldelmundo")
                .requires(source -> source.hasPermission(2))
                .executes(context -> warpWorldTree(context.getSource(), null))
                .then(Commands.literal("procedural")
                        .executes(context -> warpWorldTree(context.getSource(), "procedural")))
                .then(Commands.literal("nbt")
                        .executes(context -> warpWorldTree(context.getSource(), "nbt")))
                .then(Commands.literal("rebuild")
                        .executes(context -> warpWorldTree(context.getSource(), "rebuild"))));

        // 2. Comandos directos de conveniencia /coins y /givecoins
        dispatcher.register(Commands.literal("coins")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showCoins(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> giveCoins(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));

        dispatcher.register(Commands.literal("givecoins")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showCoins(context.getSource()))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> giveCoins(context.getSource(),
                                IntegerArgumentType.getInteger(context, "amount"), null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveCoins(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        EntityArgument.getPlayer(context, "target"))))));
    }

    private static int show(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§b========== SISTEMA ==========\n" +
                "§fNivel: §e" + data.level() + " §7| §fEXP: §b" + data.experience() + "/" + data.nextLevelExperience() + "\n" +
                "§6Monedas: §e" + data.coins() + " §8| §ePuntos: §a" + data.attributePoints() + "\n" +
                "§c⚔ FUE §f" + data.strength() + "  §a❤ VIT §f" + data.vitality() + "  §b⚡ AGI §f" + data.agility() + "\n" +
                "§d👁 PER §f" + data.perception() + "  §9🧠 INT §f" + data.intelligence() + "  §5🛡 VOL §f" + data.willpower()), false);
        return 1;
    }

    private static int showCoins(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Uso: /coins <cantidad> <jugador>"));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§6§l[SISTEMA]§r Saldo actual: §e" + data.coins() + " §fmonedas del Despertar. (Usa: §e/coins <cantidad>§f)"), false);
        return 1;
    }

    private static int giveCoins(CommandSourceStack source, int amount, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.literal("Debes especificar un jugador objetivo."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        data.addCoins(amount);
        SystemEvents.checkAchievements(player, data);
        AwakeningSystemMod.sync(player);
        AwakeningSystemMod.notify(player, "awakening_system.notification.shop.coin_title",
                "awakening_system.notification.shop.coin_message", 0xFFFFD866, amount);
        final ServerPlayer finalPlayer = player;
        source.sendSuccess(() -> Component.literal("§6§l[SISTEMA]§r Se han añadido §e" + amount + " §fmonedas a §b"
                + finalPlayer.getGameProfile().getName() + "§f. Total: §e" + data.coins() + " §fmonedas."), true);
        return 1;
    }

    private static int giveAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
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
        source.sendSuccess(() -> Component.literal("§a§l[SISTEMA]§r ¡Desbloqueo total completado! Nivel 490, 47 habilidades desbloqueadas a Nivel VI (activas, pasivas y manhwas), títulos míticos, afinidades al máximo, 16 llaves de mazmorra y 1.000.000 de monedas."), true);
        return 1;
    }

    private static int resetPlayer(CommandSourceStack source, ServerPlayer target) {
        target.setData(AwakeningSystemMod.PLAYER_SYSTEM, new SystemData());
        SystemEvents.dismissAllShadows(target);
        SystemAttributeEffects.apply(target);
        target.setHealth(Math.min(target.getHealth(), target.getMaxHealth()));
        AwakeningSystemMod.sync(target);

        target.displayClientMessage(Component.translatable("awakening_system.command.reset_success"), true);
        source.sendSuccess(() -> Component.literal("§a§l[SISTEMA]§r Se ha restablecido todo el progreso de §e" + target.getGameProfile().getName() + "§r."), true);
        return 1;
    }

    private static int addAttribute(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        if (!SystemEvents.spendAttribute(player, name)) {
            source.sendFailure(Component.literal("Atributo invalido o no tienes puntos disponibles."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Atributo mejorado: " + name), false);
        return 1;
    }

    private static int inspect(CommandSourceStack source, ServerPlayer target) {
        SystemData data = SystemEvents.data(target);
        source.sendSuccess(() -> Component.literal("=== INSPECCIÓN: " + target.getGameProfile().getName() + " ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Nivel: " + data.level() + " | Prestigio: " + data.prestigeLevel() + " | Monedas: " + data.coins()).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("FUE " + data.strength() + "  VIT " + data.vitality() + "  AGI " + data.agility()
                + "  PER " + data.perception() + "  INT " + data.intelligence() + "  VOL " + data.willpower()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Mobs derrotados: " + data.creaturesDefeated() + " | Jefes: " + data.bossKills()
                + " | Misiones completadas: " + data.missionsCompleted()).withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal("Habilidades (" + data.skills().size() + "): " + data.skills().keySet()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
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

        source.sendSuccess(() -> Component.literal("=== CLASIFICACION DEL SISTEMA ===").withStyle(ChatFormatting.GOLD), false);
        int rank = 1;
        for (ServerPlayer player : players) {
            if (rank > 5) break;
            SystemData d = SystemEvents.data(player);
            final int pos = rank;
            source.sendSuccess(() -> Component.literal("#" + pos + " " + player.getGameProfile().getName()
                    + " | Nvl: " + d.level() + " | Prestigio: " + d.prestigeLevel()
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
        source.sendSuccess(() -> Component.literal("§c§l[MODO TRANCE]§r Activado por 10 segundos. (Daño +3.0 y ataques de barrido activados).").withStyle(ChatFormatting.RED), false);
        return 1;
    }

    private static int setCombo(CommandSourceStack source, int count) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setComboCount(count);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§e§l[COMBO]§r Contador fijado en: §f" + count), false);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.setLevel(level);
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§a§l[NIVEL]§r Nivel establecido en: §e" + level + "§a. Puntos de atributo calculados."), false);
        return 1;
    }

    private static int setSponsor(CommandSourceStack source, String constellationId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        if (!SystemEvents.selectConstellation(player, constellationId)) {
            source.sendFailure(Component.literal("ID de constelación inválida o nivel menor a 25. Constelaciones disponibles: abyssal_black_dragon, demon_like_judge_of_fire, secretive_plotter, prisoner_of_the_golden_headband, master_of_the_underworld"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("§d§l[CONSTELACIÓN]§r Patrocinador asignado: " + constellationId), false);
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
        source.sendSuccess(() -> Component.literal("§b§l[ESCENARIO]§r Escenario activo establecido en: " + scenarioId + " (Objetivo: " + target + ")"), false);
        return 1;
    }

    private static int completeScenario(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        data.completeScenarioDirect();
        SystemEvents.announceScenarioComplete(player, data);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§6§l[ESCENARIO]§r ¡Escenario completado! Abre el menú (Misiones) para pulsar [★ RECLAMAR]."), false);
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
        source.sendSuccess(() -> Component.literal("§b§l[HABILIDAD]§r Habilidad aprendida: " + (def != null ? def.nameKey() : skillId)), false);
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
        source.sendSuccess(() -> Component.literal("§c§l[CALABOZO]§r Entrando a calabozo de rango §e" + finalRank + " §7(Tema: §b" + finalTheme + (finalRed ? " §4[RED GATE]" : "") + "§7)"), false);
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
        source.sendSuccess(() -> Component.literal("§6§l[DESPERTAR]§r ¡Misión del Despertar del Sistema completada!"), false);
        return 1;
    }

    private static int openSponsorSelection(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenSponsorScreenPayload(false));
        source.sendSuccess(() -> Component.literal("§6§l[PATROCINIO]§r Abriendo pantalla de selección de Constelación."), false);
        return 1;
    }

    private static int listUniqueRanks(CommandSourceStack source) {
        var server = source.getServer();
        UniqueRankManager mgr = UniqueRankManager.get(server);
        source.sendSuccess(() -> Component.literal("§6=== TÍTULOS DE RANGO ÚNICO (1 POR SERVIDOR) ==="), false);
        for (String tId : UniqueRankManager.UNIQUE_TITLES) {
            String holder = mgr.getTitleHolderName(tId);
            String status = holder != null ? "§c[OCUPADO: " + holder + "]" : "§a[DISPONIBLE]";
            source.sendSuccess(() -> Component.literal("§e• " + tId + " §7-> " + status), false);
        }
        source.sendSuccess(() -> Component.literal("§d=== HABILIDADES DE RANGO ÚNICO (1 POR SERVIDOR) ==="), false);
        for (String sId : UniqueRankManager.UNIQUE_SKILLS) {
            String holder = mgr.getSkillHolderName(sId);
            String status = holder != null ? "§c[OCUPADO: " + holder + "]" : "§a[DISPONIBLE]";
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
                source.sendSuccess(() -> Component.literal("§6§l[RANGO ÚNICO]§r ¡Has reclamado con éxito el título único: §e" + id), false);
                return 1;
            } else {
                String holder = mgr.getTitleHolderName(id);
                source.sendFailure(Component.literal("§cEl título único '" + id + "' ya pertenece al cazador: " + (holder != null ? holder : "Desconocido")));
                return 0;
            }
        } else if (UniqueRankManager.isUniqueSkill(id)) {
            if (mgr.claimSkill(id, player)) {
                SystemData data = SystemEvents.data(player);
                data.skills().put(id, 6);
                data.setEquippedSkill(id);
                AwakeningSystemMod.sync(player);
                source.sendSuccess(() -> Component.literal("§b§l[RANGO ÚNICO]§r ¡Has reclamado con éxito la habilidad única: §e" + id), false);
                return 1;
            } else {
                String holder = mgr.getSkillHolderName(id);
                source.sendFailure(Component.literal("§cLa habilidad única '" + id + "' ya pertenece al cazador: " + (holder != null ? holder : "Desconocido")));
                return 0;
            }
        } else {
            source.sendFailure(Component.literal("§c'" + id + "' no es un título ni una habilidad de Rango Único válida."));
            return 0;
        }
    }

    private static int openGuide(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OpenGuidebookPayload());
            source.sendSuccess(() -> Component.literal("§aAbriendo la Guía del Cazador..."), false);
            return 1;
        }
        return 0;
    }

    private static int openBestiary(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OpenBestiaryPayload());
            source.sendSuccess(() -> Component.literal("§aAbriendo el Bestiario de Cazadores..."), false);
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
            source.sendFailure(Component.literal("Especifica un jugador objetivo: /system constelacion ascender <jugador> [nombre]"));
            return 0;
        }
        final ServerPlayer finalP = player;
        ConstellationAscension.triggerAscension(finalP, "Comando Administrativo del Sistema",
                (customModifier == null || customModifier.isBlank()) ? null : customModifier.trim());
        source.sendSuccess(() -> Component.literal("§d§l[STAR STREAM]§r El jugador §e" + finalP.getGameProfile().getName() + " §dha ascendido a Constelación."), true);
        return 1;
    }

    private static int demoteConstellation(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer player = target;
        if (player == null && source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        }
        if (player == null) {
            source.sendFailure(Component.literal("Especifica un jugador objetivo: /system constelacion degradar <jugador>"));
            return 0;
        }
        final ServerPlayer finalP = player;
        SystemData data = SystemEvents.data(finalP);
        data.setIsConstellation(false);
        data.setConstellationModifier("");
        AwakeningSystemMod.sync(finalP);
        source.sendSuccess(() -> Component.literal("§c§l[STAR STREAM]§r El estatus de Constelación de §e" + finalP.getGameProfile().getName() + " §cha sido revocado."), true);
        finalP.displayClientMessage(Component.translatable("awakening_system.command.constellation_reverted"), true);
        return 1;
    }

    private static int renameConstellation(CommandSourceStack source, String modifier) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        if (!data.isConstellation()) {
            source.sendFailure(Component.literal("§cNo eres una Constelación. Debes ascender primero mediante eventos míticos o hazañas trascendentes."));
            return 0;
        }
        String clean = modifier.trim().replace("[", "").replace("]", "");
        data.setConstellationModifier(clean);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§d§l[STAR STREAM]§r Tu Nombre Propio como Constelación ha sido actualizado a: §e[" + clean + "§e]"), false);
        return 1;
    }

    private static int returnFromAstral(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        if (!ConstellationChannelHandler.isAstralWatching(player.getUUID())) {
            source.sendFailure(Component.literal("§cNo te encuentras en proyección astral."));
            return 0;
        }
        ConstellationChannelHandler.returnFromAstral(player);
        return 1;
    }

    private static int spawnStructureCommand(CommandSourceStack source, String name) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        if ("abyss".equalsIgnoreCase(name) || "abyss_portal".equalsIgnoreCase(name) || "spiral_abyss".equalsIgnoreCase(name) || "teyvat_portal".equalsIgnoreCase(name)) {
            Direction dir = (source.getEntity() instanceof ServerPlayer sp) ? sp.getDirection() : Direction.SOUTH;
            com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, pos, dir, com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive());
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aArrecife de Musk e Isla de la Espiral del Abismo generados en " + pos.toShortString()), true);
            return 1;
        }
        boolean ok = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.generateStructure(level, pos, name);
        if (ok) {
            com.soybe.awakeningsystem.world.SeriesStructureWorldGen.checkAndEnsureNpcsPresent(level.getServer());
            source.sendSuccess(() -> Component.literal("§a§l[SISTEMA]§r Estructura temática '§e" + name + "§r' generada en " + pos.toShortString()), true);
            return 1;
        }

        // Probar si es una estructura o arena de jefe de Teyvat (Genshin Impact)
        boolean teyvatOk = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(level, pos, name);
        if (teyvatOk) {
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aEstructura de Teyvat '§e" + name + "§a' generada exitosamente en " + pos.toShortString()), true);
            return 1;
        }

        source.sendFailure(Component.literal("§cTipo de estructura desconocido. Opciones:\n" +
                "§7Teyvat: §bgolden_house (Childe), tenshukaku (Raiden), wolflord (Acechador), azhdaha, anemo_hypostasis, electro_hypostasis, geo_hypostasis, cryo_hypostasis, pyro_hypostasis, hydro_hypostasis, cryo_regisvine, anemo_shrine, geo_shrine, electro_shrine, dendro_shrine, statue, waypoint, camp, windrise, jade_chamber, beidou_ship, primal_temple, chasm_mine, karst, updraft, abyss.\n" +
                "§7Manhwa: §eabyss, solo_leveling, orv, estate, nano, tbate, ranker, mount_hua, valter, tower_of_god, ascension, etc."));
        return 0;
    }

    private static int spawnTeyvatStructureCommand(CommandSourceStack source, String name) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        boolean ok = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(level, pos, name);
        if (ok) {
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aEstructura '§e" + name + "§a' generada exitosamente en " + pos.toShortString()), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cEstructura de Teyvat no reconocida. Opciones: golden_house (Childe), tenshukaku (Raiden Shogun), wolflord (Rey Acechador), azhdaha, anemo_hypostasis, electro_hypostasis, geo_hypostasis, cryo_hypostasis, pyro_hypostasis, hydro_hypostasis, cryo_regisvine, anemo_shrine, geo_shrine, electro_shrine, dendro_shrine, statue, waypoint, camp, windrise, fix_windrise, fix_arenas, clean_arenas, jade_chamber, beidou_ship, primal_temple, chasm_mine, karst, updraft, abyss."));
            return 0;
        }
    }

    record TeyvatLocateTarget(String id, String displayName, BlockPos canonicalPos, String cmd) {}

    private static final java.util.List<TeyvatLocateTarget> TEYVAT_CANONICAL_TARGETS = java.util.List.of(
            new TeyvatLocateTarget("golden_house", "La Casa Dorada (Jefe Tartaglia / Childe)", TeyvatWorldManager.GOLDEN_HOUSE_POS.offset(0, 7, 4), "golden_house"),
            new TeyvatLocateTarget("tenshukaku", "Palacio Tenshukaku (Jefe Raiden Shogun)", TeyvatWorldManager.TENSHUKAKU_POS, "tenshukaku"),
            new TeyvatLocateTarget("wolflord", "Dominio del Rey Acechador Dorado (Enkanomiya)", TeyvatWorldManager.WOLFLORD_POS, "wolflord"),
            new TeyvatLocateTarget("azhdaha", "Dominio Subterráneo del Dragón Azhdaha", TeyvatWorldManager.AZHDAHA_POS, "azhdaha"),
            new TeyvatLocateTarget("windrise", "Gran Roble Sagrado de Windrise", TeyvatWorldManager.WINDRISE_POS, "windrise"),
            new TeyvatLocateTarget("jade_chamber", "Cámara de Jade Flotante", TeyvatWorldManager.JADE_CHAMBER_POS, "jade_chamber"),
            new TeyvatLocateTarget("dawn_winery", "Viñedo del Amanecer (Dawn Winery)", TeyvatWorldManager.WINERY_POS, "dawn_winery"),
            new TeyvatLocateTarget("beidou_ship", "El Alcor (Navío de Beidou)", TeyvatWorldManager.ALCOR_POS, "alcor"),
            new TeyvatLocateTarget("anemo_hypostasis", "Arena Hipóstasis Anemo (Beth)", TeyvatWorldManager.ANEMO_HYPOSTASIS_POS, "anemo_hypostasis"),
            new TeyvatLocateTarget("electro_hypostasis", "Arena Hipóstasis Electro (Aleph)", TeyvatWorldManager.ELECTRO_HYPOSTASIS_POS, "electro_hypostasis"),
            new TeyvatLocateTarget("geo_hypostasis", "Arena Hipóstasis Geo (Gimel)", TeyvatWorldManager.GEO_HYPOSTASIS_POS, "geo_hypostasis"),
            new TeyvatLocateTarget("cryo_hypostasis", "Arena Hipóstasis Cryo (Daleth)", TeyvatWorldManager.CRYO_HYPOSTASIS_POS, "cryo_hypostasis"),
            new TeyvatLocateTarget("pyro_hypostasis", "Arena Hipóstasis Pyro (Ayin)", TeyvatWorldManager.PYRO_HYPOSTASIS_POS, "pyro_hypostasis"),
            new TeyvatLocateTarget("hydro_hypostasis", "Arena Hipóstasis Hydro (He)", TeyvatWorldManager.HYDRO_HYPOSTASIS_POS, "hydro_hypostasis"),
            new TeyvatLocateTarget("cryo_regisvine", "Arena de Regisvid Cryo", TeyvatWorldManager.CRYO_REGISVINE_POS, "cryo_regisvine"),
            new TeyvatLocateTarget("abyss", "Espiral del Abismo (Musk Reef)", TeyvatWorldManager.MUSK_REEF_ISLAND_POS, "abyss"),
            new TeyvatLocateTarget("mondstadt", "Mondstadt (Ciudadela Favonius)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.MONDSTADT_POS, "mondstadt"),
            new TeyvatLocateTarget("sumeru", "Sumeru (La Gran Academiya)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.SUMERU_POS, "sumeru"),
            new TeyvatLocateTarget("fontaine", "Fontaine (Palais Mermonia)", com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.FONTAINE_POS, "fontaine")
    );

    private static int locateTeyvatStructureCommand(CommandSourceStack source, String filter) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        MinecraftServer server = level.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) {
            source.sendFailure(Component.literal("§cLa dimensión de Teyvat no está cargada. Viaja primero con /teyvat."));
            return 0;
        }

        BlockPos origin = BlockPos.containing(source.getPosition());
        String q = (filter == null) ? "" : filter.toLowerCase(java.util.Locale.ROOT).trim();
        var tData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);

        // 1. Coincidencia con estructuras canónicas (Jefes, Palacios, Hipóstasis, etc.)
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
                // Alias comunes
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

            var warpBtn = Component.literal(" §e§l[VIAJAR Y ENTRAR]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GOLD)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/teyvat " + finalTarget.cmd()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§aClic para " + (alreadyBuilt ? "viajar" : "construir e ingresar") + " a " + finalTarget.displayName()))));

            var msg = Component.literal("§b§l✦ [TEYVAT] §aEstructura: §e" + finalTarget.displayName() +
                    "\n§7Coordenadas: §b[" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + "] §8(" + dist + "m) " +
                    (alreadyBuilt ? "§2✔ [Construida]" : "§6★ [Sede Canónica]") + " ")
                    .append(warpBtn);

            source.sendSuccess(() -> msg, false);
            return 1;
        }

        // 2. Si busca por estructuras secundarias o genéricas registradas en namedStructures
        for (var entry : tData.namedStructures.entrySet()) {
            if (!q.isEmpty() && entry.getKey().contains(q)) {
                BlockPos p = entry.getValue();
                int dist = (int) Math.sqrt(p.distSqr(origin));
                var tpBtn = Component.literal(" §e§l[TELETRANSPORTAR]")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true)
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                        "/tp @s " + p.getX() + " " + (p.getY() + 1) + " " + p.getZ()))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("§aClic para teletransportarte"))));
                var msg = Component.literal("§b§l✦ [TEYVAT] §aEstructura registrada: §e" + entry.getKey() + " §7en §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §8(" + dist + "m)").append(tpBtn);
                source.sendSuccess(() -> msg, false);
                return 1;
            }
        }

        // 3. Características de exploración (Santuarios, Estatuas, Waypoints, Campamentos)
        java.util.List<BlockPos> featureList = null;
        String featureLabel = null;
        if (q.contains("shrine") || q.contains("santuario")) {
            featureList = tData.shrinesOfDepths;
            featureLabel = "Santuario de las Profundidades";
        } else if (q.contains("statue") || q.contains("estatua")) {
            featureList = tData.statues;
            featureLabel = "Estatua de los Siete";
        } else if (q.contains("waypoint") || q.contains("tp") || q.contains("teleport")) {
            featureList = tData.waypoints;
            featureLabel = "Teleport Waypoint";
        } else if (q.contains("camp") || q.contains("hilichurl") || q.contains("campamento")) {
            featureList = tData.camps;
            featureLabel = "Campamento Hilichurl";
        }

        if (featureList != null) {
            final String finalFeatureLabel = featureLabel;
            if (featureList.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§e§l[TEYVAT]§r Aún no se han descubierto " + finalFeatureLabel + "s en el mapa. Explora nuevas regiones a más de 300 bloques."), false);
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
            var tpBtn = Component.literal(" §e§l[TELETRANSPORTAR]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.YELLOW)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/tp @s " + fp.getX() + " " + (fp.getY() + 1) + " " + fp.getZ()))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§aClic para teletransportarte al " + finalFeatureLabel))));
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §a" + finalFeatureLabel + " más cercano en §b[" + fp.getX() + ", " + fp.getY() + ", " + fp.getZ() + "] §8(" + dist + "m)").append(tpBtn), false);
            return 1;
        }

        // 4. Si no se encontró, mostrar catálogo de acceso rápido
        source.sendSuccess(() -> Component.literal("§e§l[TEYVAT]§r No se encontró ninguna estructura con el término '§c" + q + "§r'."), false);
        source.sendSuccess(() -> Component.literal("§7Usa §b/teyvat list§7 para ver el catálogo interactivo o viaja directo a los jefes:"), false);
        source.sendSuccess(() -> Component.literal("  §6✦ [Tartaglia / Childe] §7-> §e/teyvat golden_house"), false);
        source.sendSuccess(() -> Component.literal("  §d✦ [Raiden Shogun]      §7-> §e/teyvat tenshukaku"), false);
        source.sendSuccess(() -> Component.literal("  §6✦ [Rey Acechador]      §7-> §e/teyvat wolflord"), false);
        source.sendSuccess(() -> Component.literal("  §e✦ [Dragón Azhdaha]     §7-> §e/teyvat azhdaha"), false);
        return 1;
    }

    private static int listTeyvatStructuresCommand(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        MinecraftServer server = level.getServer();
        var tData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        BlockPos origin = BlockPos.containing(source.getPosition());

        source.sendSuccess(() -> Component.literal("§b§l================ CATÁLOGO DE ESTRUCTURAS DE TEYVAT ================"), false);

        // 1. Jefes Semanales y Grandes Palacios
        source.sendSuccess(() -> Component.literal("§6§l👑 JEFES SEMANALES Y PALACIOS AUTÉNTICOS:"), false);
        sendLocateEntry(source, origin, "🏰 La Casa Dorada (Jefe Tartaglia / Childe)", "golden_house", TeyvatWorldManager.GOLDEN_HOUSE_POS.offset(0, 7, 4), tData);
        sendLocateEntry(source, origin, "⚡ Palacio Tenshukaku (Jefe Raiden Shogun)", "tenshukaku", TeyvatWorldManager.TENSHUKAKU_POS, tData);
        sendLocateEntry(source, origin, "🐺 Isla Enkanomiya (Rey Acechador Dorado)", "wolflord", TeyvatWorldManager.WOLFLORD_POS, tData);
        sendLocateEntry(source, origin, "🐉 Dominio Subterráneo del Dragón Azhdaha", "azhdaha", TeyvatWorldManager.AZHDAHA_POS, tData);

        // 2. Hipóstasis Elementales
        source.sendSuccess(() -> Component.literal("§b§l💎 HIPÓSTASIS ELEMENTALES Y REGISVID:"), false);
        sendLocateEntry(source, origin, "🍃 Hipóstasis Anemo (Beth)", "anemo_hypostasis", TeyvatWorldManager.ANEMO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "⚡ Hipóstasis Electro (Aleph)", "electro_hypostasis", TeyvatWorldManager.ELECTRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔶 Hipóstasis Geo (Gimel)", "geo_hypostasis", TeyvatWorldManager.GEO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Hipóstasis Cryo (Daleth)", "cryo_hypostasis", TeyvatWorldManager.CRYO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "🔥 Hipóstasis Pyro (Ayin)", "pyro_hypostasis", TeyvatWorldManager.PYRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "💧 Hipóstasis Hydro (He)", "hydro_hypostasis", TeyvatWorldManager.HYDRO_HYPOSTASIS_POS, tData);
        sendLocateEntry(source, origin, "❄ Regisvid Cryo", "cryo_regisvine", TeyvatWorldManager.CRYO_REGISVINE_POS, tData);

        // 3. Lugares Emblemáticos
        source.sendSuccess(() -> Component.literal("§a§l🌳 MONUMENTOS Y SEDES DE NACIONES:"), false);
        sendLocateEntry(source, origin, "🌳 Gran Roble Sagrado de Windrise", "windrise", TeyvatWorldManager.WINDRISE_POS, tData);
        sendLocateEntry(source, origin, "🏯 Cámara de Jade Flotante", "jade_chamber", TeyvatWorldManager.JADE_CHAMBER_POS, tData);
        sendLocateEntry(source, origin, "🍷 Viñedo del Amanecer (Dawn Winery)", "dawn_winery", TeyvatWorldManager.WINERY_POS, tData);
        sendLocateEntry(source, origin, "⛵ El Alcor (Navío de Beidou)", "alcor", TeyvatWorldManager.ALCOR_POS, tData);
        sendLocateEntry(source, origin, "🌀 Espiral del Abismo (Musk Reef)", "abyss", TeyvatWorldManager.MUSK_REEF_ISLAND_POS, tData);

        // 4. Conteo de exploración
        source.sendSuccess(() -> Component.literal("§e§l⛩ EXPLORACIÓN Y RECURSOS:"), false);
        source.sendSuccess(() -> Component.literal("  §fSantuarios de las Profundidades: §e" + tData.shrinesOfDepths.size() +
                " §7| §fEstatuas de los Siete: §e" + tData.statues.size() +
                " §7| §fWaypoints: §e" + tData.waypoints.size() +
                " §7| §fCampamentos: §e" + tData.camps.size()), false);
        source.sendSuccess(() -> Component.literal("§7Usa §b/teyvat <nombre>§7 para viajar directamente a cualquier estructura o jefe."), false);
        source.sendSuccess(() -> Component.literal("§b§l=================================================================="), false);
        return 1;
    }

    private static void sendLocateEntry(CommandSourceStack source, BlockPos origin, String displayName, String cmdKey, BlockPos canonicalPos, com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData tData) {
        BlockPos actual = tData.getNamedStructure(cmdKey);
        boolean built = (actual != null);
        BlockPos p = built ? actual : canonicalPos;
        int dist = (int) Math.sqrt(p.distSqr(origin));

        var btn = Component.literal(built ? " §e§l[VIAJAR]" : " §6§l[ENTRAR/GENERAR]")
                .withStyle(style -> style
                        .withColor(built ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                "/teyvat " + cmdKey))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                Component.literal("§aClic para viajar a " + displayName + (built ? "" : " (Se materializará al instante)")))));

        source.sendSuccess(() -> Component.literal("  §f" + displayName + " §7en §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §8(" + dist + "m) " + (built ? "§2✔" : "§7○")).append(btn), false);
    }

    private static int buildAbyssPortalCommand(CommandSourceStack source, String modeArg) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        BlockPos pos = BlockPos.containing(source.getPosition());
        Direction dir = (source.getEntity() instanceof ServerPlayer sp) ? sp.getDirection() : Direction.SOUTH;

        if ("island".equalsIgnoreCase(modeArg)) {
            com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, pos, dir, com.soybe.awakeningsystem.TeyvatWorldManager.isAbyssActive());
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aIsla del Arrecife de Musk y Espiral del Abismo generadas en " + pos.toShortString()), true);
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
        source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aMonumento de la Espiral del Abismo (13x13) construido con éxito en " + pos.toShortString() + " [" + (active ? "§bACTIVO§a" : "§cBLOQUEADO§a") + "]"), true);
        return 1;
    }

    private static int warpAbyss(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.TeyvatWorldManager.teleportToAbyss(player);
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden teletransportarse a la Espiral del Abismo."));
        return 0;
    }

    private static int openAbyssMenu(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            source.sendSuccess(() -> Component.literal("§b✦ [Espiral del Abismo] Abriendo interfaz..."), false);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden abrir la interfaz de la Espiral del Abismo."));
        return 0;
    }

    private static int setAbyssStateCommand(CommandSourceStack source, boolean active) {
        var server = source.getServer();
        com.soybe.awakeningsystem.TeyvatWorldManager.setAbyssActive(server, active);
        source.sendSuccess(() -> Component.literal("§b§l✦ [ESPIRAL DEL ABISMO] §aEstado actualizado a: " + (active ? "§bACTIVO (Vórtice Cósmico ABIERTO)" : "§cBLOQUEADO (Portal SELLADO)")), true);
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
            source.sendSuccess(() -> Component.literal("§b§l✦ [ESPIRAL DEL ABISMO] §aIsla del Arrecife de Musk y portal reconstruidos con éxito."), true);
            return 1;
        }
        source.sendFailure(Component.literal("§cLa dimensión de Teyvat no está cargada."));
        return 0;
    }

    private static int locateStructureCommand(CommandSourceStack source, String filterType) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        BlockPos origin = BlockPos.containing(source.getPosition());
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
        java.util.Map<String, BlockPos> all = data.getAll();

        java.util.Map.Entry<String, BlockPos> nearest = null;
        double bestDistSq = Double.MAX_VALUE;

        for (java.util.Map.Entry<String, BlockPos> entry : all.entrySet()) {
            if (filterType != null && !filterType.isEmpty() && !entry.getKey().toLowerCase(java.util.Locale.ROOT).contains(filterType.toLowerCase(java.util.Locale.ROOT))) {
                continue;
            }
            double dist = entry.getValue().distSqr(origin);
            if (dist < bestDistSq) {
                bestDistSq = dist;
                nearest = entry;
            }
        }

        if (nearest != null) {
            final var found = nearest;
            final BlockPos p = found.getValue();
            final int distBlocks = (int) Math.sqrt(bestDistSq);
            source.sendSuccess(() -> Component.literal("§a§l[SISTEMA]§r Estructura descubierta '§e" + found.getKey() + "§r' en: §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §7(" + distBlocks + " bloques de distancia)"), false);
            return 1;
        }

        // Si aún no se ha generado en el mundo, predecir la zona natural más cercana de la cuadrícula
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        int pRegionX = Math.floorDiv(originChunkX, 32);
        int pRegionZ = Math.floorDiv(originChunkZ, 32);

        BlockPos bestCand = null;
        double bestCandDistSq = Double.MAX_VALUE;

        for (int rx = pRegionX - 2; rx <= pRegionX + 2; rx++) {
            for (int rz = pRegionZ - 2; rz <= pRegionZ + 2; rz++) {
                long seed = ((long) rx * 341873128712L + (long) rz * 132897987541L) ^ level.getSeed();
                java.util.Random rand = new java.util.Random(seed);
                int targetChunkX = rx * 32 + rand.nextInt(24) + 4;
                int targetChunkZ = rz * 32 + rand.nextInt(24) + 4;
                int bx = targetChunkX * 16 + 8;
                int bz = targetChunkZ * 16 + 8;
                int by = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, bx, bz);
                BlockPos cand = new BlockPos(bx, by, bz);
                double d = cand.distSqr(origin);
                if (d < bestCandDistSq) {
                    bestCandDistSq = d;
                    bestCand = cand;
                }
            }
        }

        if (bestCand != null) {
            final BlockPos candidate = bestCand;
            int dist = (int) Math.sqrt(bestCandDistSq);
            source.sendSuccess(() -> Component.literal("§6§l[SISTEMA]§r Próxima zona de generación natural predicha: §b[" + candidate.getX() + ", " + candidate.getY() + ", " + candidate.getZ() + "] §7(" + dist + " bloques). Explora esa zona para que emerja."), false);
            return 1;
        }

        source.sendFailure(Component.literal("§cNo se encontraron coordenadas en el rango actual."));
        return 0;
    }

    private static int listStructuresCommand(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
            return 0;
        }
        var data = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
        java.util.Map<String, BlockPos> all = data.getAll();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§e§l[SISTEMA]§r Aún no se han generado estructuras en este mundo. Explora nuevas regiones o usa §b/structure <nombre>§r."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6§l=== ESTRUCTURAS DE SERIES GENERADAS (" + all.size() + ") ==="), false);
        BlockPos origin = BlockPos.containing(source.getPosition());
        for (java.util.Map.Entry<String, BlockPos> entry : all.entrySet()) {
            BlockPos p = entry.getValue();
            int dist = (int) Math.sqrt(p.distSqr(origin));
            source.sendSuccess(() -> Component.literal("§e✦ " + entry.getKey() + " §7en §b[" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "] §8(" + dist + "m)"), false);
        }
        return 1;
    }

    private static int spawnNpcCommand(CommandSourceStack source, String character) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("§cComando solo disponible en el servidor."));
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
        source.sendSuccess(() -> Component.literal("§a§l[SISTEMA]§r NPC " + profile.titleColor() + profile.displayName() + "§r invocado con éxito."), true);
        return 1;
    }

    private static int storeShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        int count = SystemEvents.storePlayerShadows(player);
        if (count > 0) {
            source.sendSuccess(() -> Component.literal("§8[§9Ejército de Sombras§8] §aSe han guardado §f" + count + " §asoldado(s) en tu dominio."), false);
        } else {
            source.sendFailure(Component.literal("§cNo tienes soldados de sombra cercanos en el campo para guardar."));
        }
        return count;
    }

    private static int deployShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        int deployed = SystemEvents.deployPlayerShadows(player, 4);
        if (deployed > 0) {
            source.sendSuccess(() -> Component.literal("§8[§9Ejército de Sombras§8] §bHas desplegado §f" + deployed + " §bsoldado(s) al combate."), false);
        } else {
            source.sendFailure(Component.literal("§cNo tienes soldados de sombra en la reserva de tu dominio."));
        }
        return deployed;
    }

    private static int showShadowStatus(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
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
        source.sendSuccess(() -> Component.literal("§8================ §9§lDOMINIO DE SOMBRAS §8================"), false);
        source.sendSuccess(() -> Component.literal("§7Soldados activos en campo: §b" + finalActive), false);
        source.sendSuccess(() -> Component.literal("§7Soldados resguardados en dominio: §a" + stored), false);
        source.sendSuccess(() -> Component.literal("§7Maná actual para regeneración: §d" + energy + " / 100 §f(Costo regen: 1 maná/s, revivir: 15 maná)"), false);
        source.sendSuccess(() -> Component.literal("§7Controles rápidos: §e[V] §7para Extraer/Sacar, §e[Shift + V] §7para Guardar en dominio."), false);
        source.sendSuccess(() -> Component.literal("§8======================================================="), false);
        return 1;
    }

    private static int dismissShadowsCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador."));
            return 0;
        }
        SystemEvents.dismissAllShadows(player);
        source.sendSuccess(() -> Component.literal("§8[§9Ejército de Sombras§8] §eTodas tus sombras activas han sido disipadas."), false);
        return 1;
    }

    private static int spawnDokkaebiCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        SystemEvents.spawnDokkaebiNearPlayer(player);
        source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r Dokkaebi invocado con éxito junto a ti."), false);
        return 1;
    }

    private static int showDokkaebiQuestCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        net.minecraft.world.phys.AABB nearby = player.getBoundingBox().inflate(32.0D);
        java.util.List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.DokkaebiEntity.class, nearby);
        if (dokkaebis.isEmpty()) {
            source.sendFailure(Component.literal("§cNo hay ningún Dokkaebi cerca transmitiendo. Espera a que aparezca o usa /dokkaebi spawn."));
            return 0;
        }
        var dokkaebi = dokkaebis.get(0);
        if (!dokkaebi.isQuestActive()) {
            source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r El Dokkaebi aún no te ha asignado ningún Sub-Escenario. Hazle clic derecho para recibir uno."), false);
        } else {
            String title = (dokkaebi.getQuestTitle() != null && !dokkaebi.getQuestTitle().isEmpty()) ? dokkaebi.getQuestTitle() : "Sub-Escenario";
            String desc = (dokkaebi.getQuestDesc() != null) ? dokkaebi.getQuestDesc() : "";
            source.sendSuccess(() -> Component.literal("§8[§eStar Stream§8] §fSub-Escenario: §b" + title + " §7(" + dokkaebi.getQuestProgress() + "/" + dokkaebi.getQuestTarget() + ") §e- " + desc), false);
        }
        return 1;
    }

    private static int dismissDokkaebiCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        net.minecraft.world.phys.AABB nearby = player.getBoundingBox().inflate(32.0D);
        java.util.List<com.soybe.awakeningsystem.entity.DokkaebiEntity> dokkaebis = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.DokkaebiEntity.class, nearby);
        if (dokkaebis.isEmpty()) {
            source.sendFailure(Component.literal("§cNo hay ningún Dokkaebi cercano para despedir."));
            return 0;
        }
        for (var d : dokkaebis) {
            d.dismissWithParticles();
        }
        source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r El Dokkaebi ha cerrado la transmisión."), false);
        return 1;
    }

    private static int triggerDokkaebiScenarioCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        SystemData data = SystemEvents.data(player);
        int target = 10;
        data.startDokkaebiScenario("Supervivencia y Cacería de Emergencia", 3600, target);
        AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_scenario_start.title", "awakening_system.notification.dokkaebi_scenario_start.desc", 0xFFFF4444, target);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BELL_BLOCK, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.8F);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§e§l[Star Stream]§r Escenario Forzado del Dokkaebi iniciado con éxito."), false);
        return 1;
    }

    private static int spawnBossCommand(CommandSourceStack source, String type) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        if ("igris".equalsIgnoreCase(type)) {
            var igris = com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS.get().create(level);
            if (igris != null) {
                igris.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(igris);
                source.sendSuccess(() -> Component.literal("§c§l[Jefe Invocado]§r ¡Igris, Comandante Carmesí ha aparecido!"), true);
                return 1;
            }
        } else if ("baran".equalsIgnoreCase(type)) {
            var baran = com.soybe.awakeningsystem.entity.ModEntities.BARAN_BOSS.get().create(level);
            if (baran != null) {
                baran.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(baran);
                source.sendSuccess(() -> Component.literal("§b§l[Jefe Invocado]§r ¡Baran, Monarca de las Llamas Blancas ha aparecido!"), true);
                return 1;
            }
        } else if ("patriarch".equalsIgnoreCase(type) || "patriarca".equalsIgnoreCase(type) || "blood_patriarch".equalsIgnoreCase(type)) {
            var patriarch = com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get().create(level);
            if (patriarch != null) {
                patriarch.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(patriarch);
                source.sendSuccess(() -> Component.literal("§4§l[Jefe Invocado]§r ¡Gran Patriarca de la Secta de Sangre ha aparecido!"), true);
                return 1;
            }
        } else if ("treant".equalsIgnoreCase(type) || "yggdrasil_treant".equalsIgnoreCase(type)) {
            var treant = com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get().create(level);
            if (treant != null) {
                treant.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(treant);
                source.sendSuccess(() -> Component.literal("§2§l[Jefe Invocado]§r ¡Treant Ancestral de Yggdrasil ha aparecido!"), true);
                return 1;
            }
        } else if ("childe".equalsIgnoreCase(type) || "tartaglia".equalsIgnoreCase(type) || "nobile".equalsIgnoreCase(type)) {
            var childe = com.soybe.awakeningsystem.entity.ModEntities.CHILDE_BOSS.get().create(level);
            if (childe != null) {
                childe.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(childe);
                source.sendSuccess(() -> Component.literal("§b§l[Jefe Invocado]§r ¡Nobile / Tartaglia (Childe), Undécimo Heraldo de Fatui ha aparecido!"), true);
                return 1;
            }
        } else if ("raiden".equalsIgnoreCase(type) || "shogun".equalsIgnoreCase(type) || "baal".equalsIgnoreCase(type)) {
            var raiden = com.soybe.awakeningsystem.entity.ModEntities.RAIDEN_SHOGUN_BOSS.get().create(level);
            if (raiden != null) {
                raiden.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(raiden);
                source.sendSuccess(() -> Component.literal("§d§l[Jefe Invocado]§r ¡Shogun Raiden / Baal, Diosa de la Eternidad ha aparecido!"), true);
                return 1;
            }
        } else if ("wolflord".equalsIgnoreCase(type) || "rey_acechador".equalsIgnoreCase(type) || "golden_wolflord".equalsIgnoreCase(type)) {
            var wolflord = com.soybe.awakeningsystem.entity.ModEntities.GOLDEN_WOLFLORD_BOSS.get().create(level);
            if (wolflord != null) {
                wolflord.moveTo(player.getX() + 2.0, player.getY() + 4.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(wolflord);
                source.sendSuccess(() -> Component.literal("§6§l[Jefe Invocado]§r ¡Rey Acechador Dorado, Soberano del Can Menor de la Grieta ha aparecido!"), true);
                return 1;
            }
        } else if ("azhdaha".equalsIgnoreCase(type)) {
            var azhdaha = com.soybe.awakeningsystem.entity.ModEntities.AZHDAHA_BOSS.get().create(level);
            if (azhdaha != null) {
                azhdaha.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(azhdaha);
                source.sendSuccess(() -> Component.literal("§6§l[Jefe Invocado]§r ¡Azhdaha, Rey Dragón Primigenio Sellado ha aparecido!"), true);
                return 1;
            }
        } else if ("anemo_hypostasis".equalsIgnoreCase(type) || "anemo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§a§l[Jefe Invocado]§r ¡Hipóstasis Anemo (Beth) ha aparecido!"), true);
                return 1;
            }
        } else if ("electro_hypostasis".equalsIgnoreCase(type) || "electro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§d§l[Jefe Invocado]§r ¡Hipóstasis Electro (Aleph) ha aparecido!"), true);
                return 1;
            }
        } else if ("geo_hypostasis".equalsIgnoreCase(type) || "geo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§e§l[Jefe Invocado]§r ¡Hipóstasis Geo (Gimel) ha aparecido!"), true);
                return 1;
            }
        } else if ("cryo_hypostasis".equalsIgnoreCase(type) || "cryo_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.CRYO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§b§l[Jefe Invocado]§r ¡Hipóstasis Cryo (Daleth) ha aparecido!"), true);
                return 1;
            }
        } else if ("pyro_hypostasis".equalsIgnoreCase(type) || "pyro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§c§l[Jefe Invocado]§r ¡Hipóstasis Pyro (Ayin) ha aparecido!"), true);
                return 1;
            }
        } else if ("hydro_hypostasis".equalsIgnoreCase(type) || "hydro_cube".equalsIgnoreCase(type)) {
            var h = com.soybe.awakeningsystem.entity.ModEntities.HYDRO_HYPOSTASIS.get().create(level);
            if (h != null) {
                h.moveTo(player.getX() + 2.0, player.getY() + 2.0, player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(h);
                source.sendSuccess(() -> Component.literal("§9§l[Jefe Invocado]§r ¡Hipóstasis Hydro (He) ha aparecido!"), true);
                return 1;
            }
        } else if ("cryo_regisvine".equalsIgnoreCase(type) || "regisvine".equalsIgnoreCase(type)) {
            var r = com.soybe.awakeningsystem.entity.ModEntities.CRYO_REGISVINE.get().create(level);
            if (r != null) {
                r.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, player.getYRot(), 0.0F);
                level.addFreshEntity(r);
                source.sendSuccess(() -> Component.literal("§b§l[Jefe Invocado]§r ¡Regisvid Criogénica ha aparecido!"), true);
                return 1;
            }
        } else {
            source.sendFailure(Component.literal("§cJefe desconocido. Usa: /boss <igris | baran | patriarch | treant | childe | raiden | wolflord | azhdaha | anemo_hypostasis | electro_hypostasis | geo_hypostasis | cryo_hypostasis | pyro_hypostasis | hydro_hypostasis | cryo_regisvine>"));
            return 0;
        }
        return 0;
    }

    private static int enterTower(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TowerManager.enterTower(player);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden ingresar a la Torre."));
        return 0;
    }

    private static int warpTowerFloor(CommandSourceStack source, int floorNum) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TowerManager.warpFloor(player, floorNum);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden cambiar de piso en la Torre."));
        return 0;
    }

    private static int cultivate(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SystemEvents.handleCultivationAction(player, "toggle_meditation", 0);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden cultivar Qi."));
        return 0;
    }

    private static int warpIsekai(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            IsekaiWorldManager.teleportToIsekai(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.command.isekai_teleport"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden viajar al mundo Isekai."));
        return 0;
    }

    private static int completeUpdate(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data.isSystem40Updating()) {
                TeyvatWorldManager.completeSystem40Update(player, data);
                source.sendSuccess(() -> Component.literal("§a✔ Actualización a Sistema 4.0 completada instantáneamente sin esperas."), true);
                return 1;
            } else if (data.isSystem30Updating()) {
                MurimWorldManager.completeSystem30Update(player, data);
                source.sendSuccess(() -> Component.literal("§a✔ Actualización a Sistema 3.0 completada instantáneamente sin esperas."), true);
                return 1;
            } else if (data.isSystemUpdating()) {
                IsekaiWorldManager.completeSystemUpdate(player, data);
                source.sendSuccess(() -> Component.translatable("awakening_system.command.complete_update_success"), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cNo hay ninguna actualización en curso. Puedes forzar la versión con: /system version <1|2|3|4>"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden completar la actualización del Sistema."));
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
            source.sendSuccess(() -> Component.literal("§e[SISTEMA] §aVersión cambiada instantáneamente a: §b§lSistema 1.0 (Awakening Base)"), true);
            return 1;
        } else if (v.equals("2") || v.equals("2.0") || v.equals("isekai") || v.equals("v2")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(false);
            data.setMurimUnlocked(false);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SISTEMA] §aVersión cambiada instantáneamente a: §d§lSistema 2.0 (Isekai & Constelaciones)"), true);
            return 1;
        } else if (v.equals("3") || v.equals("3.0") || v.equals("murim") || v.equals("v3")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(false);
            data.setTeyvatUnlocked(false);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SISTEMA] §aVersión cambiada instantáneamente a: §a§lSistema 3.0 (Murim & Cultivo Xianxia)"), true);
            return 1;
        } else if (v.equals("4") || v.equals("4.0") || v.equals("teyvat") || v.equals("v4")) {
            data.setSystemUpdated(true);
            data.setIsekaiUnlocked(true);
            data.setSystem30Updated(true);
            data.setMurimUnlocked(true);
            data.setSystem40Updated(true);
            data.setTeyvatUnlocked(true);
            AwakeningSystemMod.sync(player);
            source.sendSuccess(() -> Component.literal("§e[SISTEMA] §aVersión cambiada instantáneamente a: §6§lSistema 4.0 (Teyvat & Impacto Celestial)"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cVersión no válida. Usa: /system version <1 | 2 | 3 | 4>"));
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

            // Si estaba en la Torre, remover sesión de torre limpiamente
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
        source.sendFailure(Component.literal("Solo los jugadores pueden viajar al Árbol del Mundo."));
        return 0;
    }

    private static int warpMurim(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            MurimWorldManager.teleportToMurim(player);
            source.sendSuccess(() -> Component.translatable("awakening_system.command.murim_teleport"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden ascender al Reino Murim."));
        return 0;
    }

    private static int warpTeyvat(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TeyvatWorldManager.teleportToTeyvat(player);
            source.sendSuccess(() -> Component.literal("§b[Teyvat] Viajando a la Plaza Ceremonial de Teyvat..."), true);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden viajar a Teyvat."));
        return 0;
    }

    private static int warpTeyvatCapital(CommandSourceStack source, String capital) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Solo los jugadores pueden viajar por Teyvat."));
            return 0;
        }
        String clean = capital.toLowerCase(java.util.Locale.ROOT).trim();
        switch (clean) {
            case "golden_house", "childe", "tartaglia", "casa_dorada", "liyue", "yujing", "contratos" -> TeyvatWorldManager.teleportToGoldenHouse(player);
            case "tenshukaku", "raiden", "shogun", "baal", "inazuma", "eternidad", "electric_castle", "electriccastle" -> TeyvatWorldManager.teleportToTenshukaku(player);
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
            default -> {
                source.sendFailure(Component.literal("§cDestino no reconocido. Opciones disponibles:\n" +
                        "§6Jefes Semanales: §e/teyvat golden_house §7(Childe), §e/teyvat tenshukaku §7(Raiden), §e/teyvat wolflord §7(Rey Acechador), §e/teyvat azhdaha\n" +
                        "§bHipóstasis: §e/teyvat anemo_hypostasis§7, §eelectro_hypostasis§7, §egeo_hypostasis§7, §ecryo_hypostasis§7, §epyro_hypostasis§7, §ehydro_hypostasis§7, §ecryo_regisvine\n" +
                        "§aLugares y Monumentos: §e/teyvat windrise§7, §ejade_chamber§7, §edawn_winery§7, §ealcor§7, §eabyss§7, §emondstadt§7, §esumeru§7, §efontaine"));
                return 0;
            }
        }
        return 1;
    }

    private static int rebuildCapitalsCommand(CommandSourceStack source) {
        var server = source.getServer();
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat != null) {
            TeyvatWorldManager.forceRebuildStructures(teyvat);
            source.sendSuccess(() -> Component.literal("§b§l✦ [TEYVAT] §aEstructuras procedurales eliminadas y terreno restaurado a su estado natural."), true);
            return 1;
        }
        source.sendFailure(Component.literal("§cLa dimensión de Teyvat no está cargada."));
        return 0;
    }

    private static int syncTeyvatMapCommand(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e[Teyvat] La inyección de regiones externas está desactivada. La dimensión genera su terreno natural de forma limpia."), false);
        return 1;
    }

    private static int openGachaCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            GachaManager.openGachaScreen(player);
            source.sendSuccess(() -> Component.literal("§6[Gacha] Abriendo interfaz del Sistema de Deseos de Teyvat..."), false);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden abrir el Sistema de Deseos."));
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
        source.sendFailure(Component.literal("Solo los jugadores pueden entrar a la Pagoda de las Nueve Tribulaciones."));
        return 0;
    }

    private static int warpPagodaStage(CommandSourceStack source, int stage) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PagodaTrialManager.warpStage(player, stage);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden cambiar de etapa en la Pagoda."));
        return 0;
    }

    private static int exitPagodaTrial(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            PagodaTrialManager.exitTrial(player.getServer(), player.getUUID());
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden salir de la Pagoda."));
        return 0;
    }

    private static int showFables(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        SystemData data = SystemEvents.data(player);
        source.sendSuccess(() -> Component.literal("§6========================================"), false);
        source.sendSuccess(() -> Component.literal("§e✦ CRÓNICAS DEL STAR STREAM §8| §f" + player.getName().getString()), false);
        source.sendSuccess(() -> Component.literal("§7Fábulas Desbloqueadas: §e" + data.unlockedFables().size() + "/18"), false);
        source.sendSuccess(() -> Component.literal("§7Peso Narrativo Total: §b" + data.fableWeight() + " pts"), false);
        source.sendSuccess(() -> Component.literal("§7Apto para Constelación: " + (data.isConstellation() ? "§e[Ya Ascendido]" : (data.unlockedFables().size() >= 5 && data.fableWeight() >= 150 ? "§a✔ [APTO]" : "§c✖ [Faltan requisitos: 5 fábulas y 150 peso]"))), false);
        source.sendSuccess(() -> Component.literal("§8----------------------------------------"), false);
        for (String fid : data.unlockedFables()) {
            var f = com.soybe.awakeningsystem.fable.FableDefinitions.get(fid);
            if (f != null) {
                source.sendSuccess(() -> Component.literal(" §e" + f.icon() + " " + f.grade().colorCode() + "[" + f.title() + "] §8(+§b" + f.weight() + " pts§8)" + (f.isWorldFirst() ? " §6[★ PRIMACÍA]" : "")), false);
            }
        }
        source.sendSuccess(() -> Component.literal("§7Abre el menú del Sistema (§eH§7) y haz clic en §6[FÁBULAS]§7 para la interfaz completa."), false);
        source.sendSuccess(() -> Component.literal("§6========================================"), false);
        return 1;
    }

    private static int grantFableCommand(CommandSourceStack source, String fableId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        var fable = com.soybe.awakeningsystem.fable.FableDefinitions.get(fableId);
        if (fable == null) {
            source.sendFailure(Component.literal("§cFábula no encontrada: " + fableId));
            return 0;
        }
        SystemEvents.grantFable(player, fableId);
        source.sendSuccess(() -> Component.literal("§aFábula concedida: " + fable.title()), false);
        return 1;
    }

    private static int openSubspaceCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.openSubspace(player);
            return 1;
        }
        source.sendFailure(Component.literal("Solo los jugadores pueden abrir el subespacio dimensional."));
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
            source.sendSuccess(() -> Component.literal("§a✔ Regresaste exitosamente de tu Subespacio Dimensional.").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        // Si está en el vacío fuera del mundo o atascado en otra dimensión de prueba
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
            source.sendSuccess(() -> Component.literal("§d✦ Bendición de Columbina (Mejorada +50%) activada exitosamente para " + player.getName().getString() + "."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§7✦ Bendición Lunar restablecida a estándar (90 protos/día)."), true);
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
        source.sendSuccess(() -> Component.literal("§d✦ Misión de Ascensión de Columbina completada."), true);
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
        source.sendSuccess(() -> Component.literal("§e✦ Progreso de Misión de Columbina reiniciado a 0."), true);
        return 1;
    }

    private static int addWelkinDaysCommand(CommandSourceStack source, int days) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SystemData data = SystemEvents.data(player);
        data.addWelkinDays(days);
        AwakeningSystemMod.sync(player);
        source.sendSuccess(() -> Component.literal("§a✦ Se añadieron " + days + " días de Bendición Lunar. Días totales: " + data.welkinDays()), true);
        return 1;
    }

    private static int spawnWelkinSanctuaryCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        net.minecraft.core.BlockPos center = player.blockPosition().below();
        TrilunarSanctuaryManager.buildSanctuary(level, center);
        player.teleportTo(center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D);
        source.sendSuccess(() -> Component.literal("§d✦ Santuario de las Tres Diosas de la Luna (Aria, Sonnet, Canon) manifestado con éxito."), true);
        return 1;
    }
}
