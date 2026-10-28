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
                                player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
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
                                        player.sendSystemMessage(Component.translatable("guild.msg.guild_not_found"));
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
                                player.sendSystemMessage(Component.translatable("guild.msg.no_guilds_founded"));
                                return 1;
                            }
                            player.sendSystemMessage(Component.translatable("guild.msg.guild_list_header"));
                            for (Guild g : guilds) {
                                player.sendSystemMessage(Component.literal("§e✦ [" + g.getTag() + "] §b" + g.getName() + " §7- Nv. §a" + g.getLevel() + " §7(Miembros: §f" + g.getMembers().size() + "§7)"));
                            }
                            return 1;
                        }))
                .then(Commands.literal("portal")
                        .then(Commands.literal("claim")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    GuildSavedData.get(player.server).claimNearbyPortal(player);
                                    return 1;
                                }))
                        .then(Commands.literal("unclaim")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    GuildSavedData.get(player.server).unclaimNearbyPortal(player);
                                    return 1;
                                }))
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    GuildSavedData.get(player.server).listGuildPortals(player);
                                    return 1;
                                }))
                        .then(Commands.literal("setfee")
                                .then(Commands.argument("mora", IntegerArgumentType.integer(0, 100000000))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int mora = IntegerArgumentType.getInteger(ctx, "mora");
                                            GuildSavedData.get(player.server).setPortalRentalFee(player, mora);
                                            player.sendSystemMessage(Component.translatable("guild.msg.portal_rental_fee_updated", String.format("%,d", mora)));
                                            return 1;
                                        })))
                        .then(Commands.literal("fee")
                                .then(Commands.argument("mora", IntegerArgumentType.integer(0, 100000000))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int mora = IntegerArgumentType.getInteger(ctx, "mora");
                                            GuildSavedData.get(player.server).setPortalRentalFee(player, mora);
                                            player.sendSystemMessage(Component.translatable("guild.msg.portal_rental_fee_updated", String.format("%,d", mora)));
                                            return 1;
                                        })))
                        .then(Commands.literal("rent")
                                .then(Commands.literal("request")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                .executes(ctx -> {
                                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                                    int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                    int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                    GuildSavedData.get(player.server).requestPortalRent(player, x, y, z);
                                                                    return 1;
                                                                })))))
                                .then(Commands.literal("accept")
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(ctx -> {
                                                                            ServerPlayer officer = ctx.getSource().getPlayerOrException();
                                                                            String targetStr = StringArgumentType.getString(ctx, "target");
                                                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                            int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                            try {
                                                                                UUID targetUuid = UUID.fromString(targetStr);
                                                                                GuildSavedData.get(officer.server).acceptPortalRent(officer, targetUuid, x, y, z);
                                                                            } catch (IllegalArgumentException e) {
                                                                                officer.sendSystemMessage(Component.translatable("guild.msg.invalid_uuid"));
                                                                            }
                                                                            return 1;
                                                                        }))))))
                                .then(Commands.literal("deny")
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(ctx -> {
                                                                            ServerPlayer officer = ctx.getSource().getPlayerOrException();
                                                                            String targetStr = StringArgumentType.getString(ctx, "target");
                                                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                            int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                            try {
                                                                                UUID targetUuid = UUID.fromString(targetStr);
                                                                                GuildSavedData.get(officer.server).denyPortalRent(officer, targetUuid, x, y, z);
                                                                            } catch (IllegalArgumentException e) {
                                                                                officer.sendSystemMessage(Component.translatable("guild.msg.invalid_uuid"));
                                                                            }
                                                                            return 1;
                                                                        }))))))));

        dispatcher.register(root);

        var clanRoot = Commands.literal("clan")
                .redirect(dispatcher.getRoot().getChild("guild"));
        dispatcher.register(clanRoot);

        var gcCmd = Commands.literal("gc")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Guild g = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                    if (g == null) {
                        player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
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
                        player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
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
                        player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
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
        player.sendSystemMessage(Component.translatable("awakening_system.guild.info_level", g.getLevel(), g.getExp(), g.getExpForNextLevel()));
        player.sendSystemMessage(Component.translatable("awakening_system.guild.info_vault", g.getMoraVault()));
        player.sendSystemMessage(Component.translatable("awakening_system.guild.info_members", g.getMembers().size()));
        for (GuildMember m : g.getMembers().values()) {
            boolean isOnline = player.server.getPlayerList().getPlayer(m.getUuid()) != null;
            String status = isOnline ? "§a●" : "§7○";
            player.sendSystemMessage(Component.translatable("awakening_system.guild.info_member_entry", status, m.getRole().getLabel(), m.getName(), m.getContributedMora()));
        }
        player.sendSystemMessage(Component.translatable("awakening_system.guild.info_active_perks", g.getUnlockedPerks().size()));
        for (String pId : g.getUnlockedPerks()) {
            GuildPerk p = GuildPerk.fromId(pId);
            if (p != null) {
                player.sendSystemMessage(Component.literal("  §b✔ ").append(p.getTitleComponent()).append(Component.literal(" §8- ")).append(p.getDescriptionComponent()));
            }
        }
        player.sendSystemMessage(Component.literal("§6══════════════════════════════════"));
    }
}
