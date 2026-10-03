package com.soybe.awakeningsystem.guild;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

public final class GuildCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("guild")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    GuildSavedData.get(player.server).syncToPlayer(player);
                    return 1;
                })
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    String tag = name.length() > 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
                                    GuildSavedData.get(player.server).createGuild(player, name, tag);
                                    return 1;
                                })
                                .then(Commands.argument("tag", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String name = StringArgumentType.getString(ctx, "name");
                                            String tag = StringArgumentType.getString(ctx, "tag");
                                            GuildSavedData.get(player.server).createGuild(player, name, tag);
                                            return 1;
                                        }))))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer inviter = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    GuildSavedData.get(inviter.server).invitePlayer(inviter, target);
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).acceptInvite(player);
                            return 1;
                        }))
                .then(Commands.literal("decline")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).declineInvite(player);
                            return 1;
                        }))
                .then(Commands.literal("leave")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).leaveGuild(player);
                            return 1;
                        }))
                .then(Commands.literal("disband")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).disbandGuild(player);
                            return 1;
                        }))
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                    GuildSavedData.get(player.server).depositMora(player, amt);
                                    return 1;
                                }))
                        .then(Commands.literal("mora")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                            GuildSavedData.get(player.server).depositMora(player, amt);
                                            return 1;
                                        })))
                        .then(Commands.literal("coins")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                            GuildSavedData.get(player.server).depositCoins(player, amt);
                                            return 1;
                                        }))))
                .then(Commands.literal("withdraw")
                        .then(Commands.literal("mora")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                            GuildSavedData.get(player.server).withdrawMora(player, amt);
                                            return 1;
                                        })))
                        .then(Commands.literal("coins")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                            GuildSavedData.get(player.server).withdrawCoins(player, amt);
                                            return 1;
                                        }))))
                .then(Commands.literal("claim")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).claimCurrentChunk(player);
                            return 1;
                        }))
                .then(Commands.literal("unclaim")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).unclaimCurrentChunk(player);
                            return 1;
                        }))
                .then(Commands.literal("unclaimall")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).unclaimAllChunks(player);
                            return 1;
                        }))
                .then(Commands.literal("map")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).syncToPlayerWithTab(player, 2);
                            return 1;
                        }))
                .then(Commands.literal("sethq")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).setGuildHq(player);
                            return 1;
                        }))
                .then(Commands.literal("sethome")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).setGuildHq(player);
                            return 1;
                        }))
                .then(Commands.literal("home")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            GuildSavedData.get(player.server).teleportToHq(player);
                            return 1;
                        }))
                .then(Commands.literal("chat")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String msg = StringArgumentType.getString(ctx, "message");
                                    GuildSavedData.get(player.server).sendGuildChat(player, msg);
                                    return 1;
                                })))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Guild g = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                            if (g == null) {
                                player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
                                return 0;
                            }
                            printGuildInfo(player, g);
                            return 1;
                        })
                        .then(Commands.argument("guildName", StringArgumentType.string())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String gName = StringArgumentType.getString(ctx, "guildName");
                                    Guild g = GuildSavedData.get(player.server).getGuildByName(gName);
                                    if (g == null) {
                                        player.sendSystemMessage(Component.literal("§cNo se encontró ningún gremio con ese nombre."));
                                        return 0;
                                    }
                                    printGuildInfo(player, g);
                                    return 1;
                                })))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Collection<Guild> guilds = GuildSavedData.get(player.server).getAllGuilds();
                            if (guilds.isEmpty()) {
                                player.sendSystemMessage(Component.literal("§7No hay gremios fundados en este mundo aún. ¡Usa §e/guild create §7para fundar uno!"));
                                return 1;
                            }
                            player.sendSystemMessage(Component.literal("§6═══════ §f[LISTA DE GREMIOS DE CAZADORES] §6═══════"));
                            for (Guild g : guilds) {
                                player.sendSystemMessage(Component.literal("§e✦ [" + g.getTag() + "] §b" + g.getName() + " §7- Nv. §a" + g.getLevel() + " §7(Miembros: §f" + g.getMembers().size() + "§7)"));
                            }
                            return 1;
                        }));

        // Registrar /guild
        dispatcher.register(root);

        // Registrar alias /clan
        var clanRoot = Commands.literal("clan")
                .redirect(dispatcher.getRoot().getChild("guild"));
        dispatcher.register(clanRoot);

        // Comandos de Chat de Gremio: /gc, /g, /clanchat (soporta mensaje directo o alternar modo)
        var gcCmd = Commands.literal("gc")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Guild g = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                    if (g == null) {
                        player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
                        return 0;
                    }
                    boolean active = GuildEventHandler.toggleGuildChat(player);
                    if (active) {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_on"));
                    } else {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_off"));
                    }
                    return 1;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            GuildSavedData.get(player.server).sendGuildChat(player, msg);
                            return 1;
                        }));
        dispatcher.register(gcCmd);

        var gCmd = Commands.literal("g")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Guild g = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                    if (g == null) {
                        player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
                        return 0;
                    }
                    boolean active = GuildEventHandler.toggleGuildChat(player);
                    if (active) {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_on"));
                    } else {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_off"));
                    }
                    return 1;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            GuildSavedData.get(player.server).sendGuildChat(player, msg);
                            return 1;
                        }));
        dispatcher.register(gCmd);

        var clanChatCmd = Commands.literal("clanchat")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Guild g = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                    if (g == null) {
                        player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
                        return 0;
                    }
                    boolean active = GuildEventHandler.toggleGuildChat(player);
                    if (active) {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_on"));
                    } else {
                        player.sendSystemMessage(Component.translatable("awakening_system.guild.chat_mode_off"));
                    }
                    return 1;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            GuildSavedData.get(player.server).sendGuildChat(player, msg);
                            return 1;
                        }));
        dispatcher.register(clanChatCmd);
    }

    private static void printGuildInfo(ServerPlayer player, Guild g) {
        player.sendSystemMessage(Component.literal("§6═══════ §f[" + g.getTag() + "] §e" + g.getName() + " §6═══════"));
        player.sendSystemMessage(Component.literal("§7Nivel: §a" + g.getLevel() + " §7| EXP: §b" + g.getExp() + " / " + g.getExpForNextLevel()));
        player.sendSystemMessage(Component.literal("§7Fondo de Moras: §e" + g.getMoraVault() + " Moras"));
        player.sendSystemMessage(Component.literal("§7Miembros (§f" + g.getMembers().size() + "§7):"));
        for (GuildMember m : g.getMembers().values()) {
            boolean isOnline = player.server.getPlayerList().getPlayer(m.getUuid()) != null;
            String status = isOnline ? "§a●" : "§7○";
            player.sendSystemMessage(Component.literal("  " + status + " " + m.getRole().getLabel() + " §f" + m.getName() + " §7(Donado: §e" + m.getContributedMora() + "§7)"));
        }
        player.sendSystemMessage(Component.literal("§7Bendiciones Activas: §d" + g.getUnlockedPerks().size()));
        for (String pId : g.getUnlockedPerks()) {
            GuildPerk p = GuildPerk.fromId(pId);
            if (p != null) {
                player.sendSystemMessage(Component.literal("  §b✔ " + p.getTitle() + " §8- " + p.getDescription()));
            }
        }
        player.sendSystemMessage(Component.literal("§6══════════════════════════════════"));
    }
}
