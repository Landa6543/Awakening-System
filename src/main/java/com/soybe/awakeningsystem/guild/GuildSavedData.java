package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.DungeonWorldSavedData;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_guild_data";

    public record PendingInvite(String guildId, UUID inviterUuid, long expiresAt) {}

    private final Map<String, Guild> guildsById = new HashMap<>();
    private final Map<String, String> guildNameToId = new HashMap<>();
    private final Map<UUID, String> playerToGuildId = new HashMap<>();
    private final Map<UUID, PendingInvite> pendingInvites = new ConcurrentHashMap<>();

    public GuildSavedData() {}

    public static GuildSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(GuildSavedData::new, GuildSavedData::load, null),
                DATA_NAME
        );
    }

    public static GuildSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        GuildSavedData data = new GuildSavedData();
        if (tag.contains("Guilds", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Guilds", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                Guild g = Guild.load(list.getCompound(i));
                data.guildsById.put(g.getId(), g);
                data.guildNameToId.put(g.getName().toLowerCase(Locale.ROOT), g.getId());
                for (UUID mUuid : g.getMembers().keySet()) {
                    data.playerToGuildId.put(mUuid, g.getId());
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Guild g : guildsById.values()) {
            list.add(g.save());
        }
        tag.put("Guilds", list);
        return tag;
    }

    public Guild getGuildById(String id) {
        return id != null ? guildsById.get(id) : null;
    }

    public Guild getGuildByName(String name) {
        if (name == null) return null;
        String id = guildNameToId.get(name.toLowerCase(Locale.ROOT));
        return getGuildById(id);
    }

    public Guild getGuildByPlayer(UUID playerUuid) {
        if (playerUuid == null) return null;
        String id = playerToGuildId.get(playerUuid);
        return getGuildById(id);
    }

    public Collection<Guild> getAllGuilds() {
        return guildsById.values();
    }

    public boolean createGuild(ServerPlayer creator, String name, String tag) {
        if (creator == null) return false;
        UUID uuid = creator.getUUID();

        if (playerToGuildId.containsKey(uuid)) {
            creator.sendSystemMessage(Component.translatable("guild.msg.already_in_guild"));
            return false;
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 3 || trimmedName.length() > 16) {
            creator.sendSystemMessage(Component.translatable("guild.msg.invalid_name_length"));
            return false;
        }

        String trimmedTag = tag.trim().toUpperCase(Locale.ROOT);
        if (trimmedTag.length() < 2 || trimmedTag.length() > 5) {
            creator.sendSystemMessage(Component.translatable("guild.msg.invalid_tag_length"));
            return false;
        }

        if (guildNameToId.containsKey(trimmedName.toLowerCase(Locale.ROOT))) {
            creator.sendSystemMessage(Component.translatable("guild.msg.name_taken"));
            return false;
        }

        int creationCost = 1000;
        SystemData sData = creator.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (sData.coins() < creationCost) {
            creator.sendSystemMessage(Component.translatable("guild.msg.insufficient_coins"));
            return false;
        }

        sData.spendCoins(creationCost);
        AwakeningSystemMod.sync(creator);

        String id = UUID.randomUUID().toString();
        Guild guild = new Guild(id, trimmedName, trimmedTag, uuid);
        guild.addMember(new GuildMember(uuid, creator.getScoreboardName(), GuildRole.LEADER));

        guildsById.put(id, guild);
        guildNameToId.put(trimmedName.toLowerCase(Locale.ROOT), id);
        playerToGuildId.put(uuid, id);
        setDirty();

        creator.level().playSound(null, creator.getX(), creator.getY(), creator.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

        Component announcement = Component.translatable("awakening_system.guild.broadcast_founded", creator.getScoreboardName(), trimmedTag, trimmedName);
        creator.server.getPlayerList().broadcastSystemMessage(announcement, false);

        syncToPlayer(creator);
        return true;
    }

    public boolean disbandGuild(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.translatable("guild.msg.leader_only_disband"));
            return false;
        }

        broadcastToGuild(guild, leader.server, Component.translatable("guild.msg.disbanded", guild.getTag(), guild.getName()));

        for (UUID mUuid : guild.getMembers().keySet()) {
            playerToGuildId.remove(mUuid);
            ServerPlayer online = leader.server.getPlayerList().getPlayer(mUuid);
            if (online != null) {
                syncToPlayer(online);
            }
        }

        guildsById.remove(guild.getId());
        guildNameToId.remove(guild.getName().toLowerCase(Locale.ROOT));
        setDirty();
        syncAllClaimsToAll(leader.server);
        return true;
    }

    public boolean invitePlayer(ServerPlayer inviter, ServerPlayer target) {
        if (inviter == null || target == null) return false;
        Guild guild = getGuildByPlayer(inviter.getUUID());
        if (guild == null) {
            inviter.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(inviter.getUUID());
        if (role == null || !role.canInvite()) {
            inviter.sendSystemMessage(Component.translatable("guild.msg.no_invite_permission"));
            return false;
        }

        if (playerToGuildId.containsKey(target.getUUID())) {
            inviter.sendSystemMessage(Component.translatable("guild.msg.target_already_in_guild"));
            return false;
        }

        pendingInvites.put(target.getUUID(), new PendingInvite(guild.getId(), inviter.getUUID(), System.currentTimeMillis() + 60000L));

        inviter.sendSystemMessage(Component.translatable("guild.msg.invited_player", target.getScoreboardName(), guild.getTag(), guild.getName()));

        Component inviteMsg = Component.translatable("guild.msg.invite_received", guild.getTag(), guild.getName(), inviter.getScoreboardName()).append("\n")
                .append(Component.translatable("guild.msg.invite_btn_accept").withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("guild.msg.invite_hover_accept")))))
                .append(Component.translatable("guild.msg.invite_btn_decline").withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild decline"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("guild.msg.invite_hover_decline")))));

        target.sendSystemMessage(inviteMsg);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    public boolean acceptInvite(ServerPlayer target) {
        if (target == null) return false;
        PendingInvite invite = pendingInvites.remove(target.getUUID());
        if (invite == null || System.currentTimeMillis() > invite.expiresAt()) {
            target.sendSystemMessage(Component.translatable("guild.msg.no_pending_invite"));
            return false;
        }

        Guild guild = getGuildById(invite.guildId());
        if (guild == null) {
            target.sendSystemMessage(Component.translatable("guild.msg.guild_no_longer_exists"));
            return false;
        }

        guild.addMember(new GuildMember(target.getUUID(), target.getScoreboardName(), GuildRole.RECRUIT));
        playerToGuildId.put(target.getUUID(), guild.getId());
        setDirty();

        broadcastToGuild(guild, target.server, Component.translatable("guild.msg.player_joined", target.getScoreboardName()));
        syncToPlayer(target);
        return true;
    }

    public boolean declineInvite(ServerPlayer target) {
        if (target == null) return false;
        PendingInvite invite = pendingInvites.remove(target.getUUID());
        if (invite != null) {
            target.sendSystemMessage(Component.translatable("guild.msg.invite_declined"));
            return true;
        }
        return false;
    }

    public boolean leaveGuild(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        if (guild.getLeaderUuid().equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("guild.msg.leader_cannot_leave"));
            return false;
        }

        guild.removeMember(player.getUUID());
        playerToGuildId.remove(player.getUUID());
        setDirty();

        player.sendSystemMessage(Component.translatable("guild.msg.left_guild"));
        broadcastToGuild(guild, player.server, Component.translatable("guild.msg.left_guild_broadcast", player.getScoreboardName()));
        syncToPlayer(player);
        return true;
    }

    public boolean kickMember(ServerPlayer officer, UUID targetUuid) {
        if (officer == null) return false;
        Guild guild = getGuildByPlayer(officer.getUUID());
        if (guild == null) return false;

        GuildRole oRole = guild.getMemberRole(officer.getUUID());
        if (oRole == null || !oRole.canKick()) {
            officer.sendSystemMessage(Component.translatable("guild.msg.no_kick_permission"));
            return false;
        }

        GuildMember targetMember = guild.getMember(targetUuid);
        if (targetMember == null) {
            officer.sendSystemMessage(Component.translatable("guild.msg.member_not_in_guild"));
            return false;
        }

        if (targetMember.getRole().getLevel() >= oRole.getLevel()) {
            officer.sendSystemMessage(Component.translatable("guild.msg.cannot_kick_higher_rank"));
            return false;
        }

        guild.removeMember(targetUuid);
        playerToGuildId.remove(targetUuid);
        setDirty();

        broadcastToGuild(guild, officer.server, Component.translatable("guild.msg.kicked_broadcast", targetMember.getName(), officer.getScoreboardName()));

        ServerPlayer targetPlayer = officer.server.getPlayerList().getPlayer(targetUuid);
        if (targetPlayer != null) {
            targetPlayer.sendSystemMessage(Component.translatable("guild.msg.kicked_notify"));
            syncToPlayer(targetPlayer);
        }
        return true;
    }

    public boolean promoteMember(ServerPlayer leader, UUID targetUuid) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.translatable("guild.msg.only_leader_promote"));
            return false;
        }

        GuildMember m = guild.getMember(targetUuid);
        if (m == null) return false;

        GuildRole newRole = switch (m.getRole()) {
            case RECRUIT -> GuildRole.MEMBER;
            case MEMBER -> GuildRole.VETERAN;
            case VETERAN -> GuildRole.OFFICER;
            default -> null;
        };

        if (newRole == null) {
            leader.sendSystemMessage(Component.translatable("guild.msg.max_role_reached"));
            return false;
        }

        m.setRole(newRole);
        setDirty();
        broadcastToGuild(guild, leader.server, Component.translatable("guild.msg.promoted_broadcast", m.getName(), newRole.getLabel()));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean demoteMember(ServerPlayer leader, UUID targetUuid) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.translatable("guild.msg.only_leader_demote"));
            return false;
        }

        GuildMember m = guild.getMember(targetUuid);
        if (m == null) return false;

        GuildRole newRole = switch (m.getRole()) {
            case OFFICER -> GuildRole.VETERAN;
            case VETERAN -> GuildRole.MEMBER;
            case MEMBER -> GuildRole.RECRUIT;
            default -> null;
        };

        if (newRole == null) {
            leader.sendSystemMessage(Component.translatable("guild.msg.min_role_reached"));
            return false;
        }

        m.setRole(newRole);
        setDirty();
        broadcastToGuild(guild, leader.server, Component.translatable("guild.msg.demoted_broadcast", m.getName(), newRole.getLabel()));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean depositMora(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data.mora() < amount) {
            player.sendSystemMessage(Component.translatable("guild.msg.donate_insufficient_mora", data.mora()));
            return false;
        }

        data.spendMora(amount);
        AwakeningSystemMod.sync(player);

        guild.depositMora(amount);
        GuildMember m = guild.getMember(player.getUUID());
        if (m != null) {
            m.addContribution(amount);
        }
        setDirty();

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);

        syncAllGuildMembers(guild, player.server);
        return true;
    }

    public boolean depositCoins(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data.coins() < amount) {
            player.sendSystemMessage(Component.translatable("guild.msg.donate_insufficient_coins", data.coins()));
            return false;
        }

        if (!data.spendCoins(amount)) {
            player.sendSystemMessage(Component.translatable("guild.msg.error_processing_coins"));
            return false;
        }
        AwakeningSystemMod.sync(player);

        guild.depositCoins(amount);
        GuildMember m = guild.getMember(player.getUUID());
        if (m != null) {
            m.addContribution(amount * 10);
        }
        setDirty();

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);

        syncAllGuildMembers(guild, player.server);
        return true;
    }

    public boolean withdrawMora(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_no_permission"));
            return false;
        }

        if (guild.getMoraVault() < amount) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_insufficient_mora", guild.getMoraVault()));
            return false;
        }

        if (guild.withdrawMora(amount)) {
            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            data.addMora(amount);
            AwakeningSystemMod.sync(player);
            setDirty();

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

            syncAllGuildMembers(guild, player.server);
            return true;
        }
        return false;
    }

    public boolean withdrawCoins(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_no_permission"));
            return false;
        }

        if (guild.getCoinVault() < amount) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_insufficient_coins", guild.getCoinVault()));
            return false;
        }

        if (guild.withdrawCoins(amount)) {
            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            data.addCoins(amount);
            AwakeningSystemMod.sync(player);
            setDirty();

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

            syncAllGuildMembers(guild, player.server);
            return true;
        }
        return false;
    }

    public boolean withdrawAllMora(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }
        long vault = guild.getMoraVault();
        if (vault <= 0) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_empty_vault"));
            return false;
        }
        int amountToWithdraw = (int) Math.min((long) Integer.MAX_VALUE, vault);
        return withdrawMora(player, amountToWithdraw);
    }

    public boolean withdrawAllCoins(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }
        long vault = guild.getCoinVault();
        if (vault <= 0) {
            player.sendSystemMessage(Component.translatable("guild.msg.withdraw_empty_vault"));
            return false;
        }
        int amountToWithdraw = (int) Math.min((long) Integer.MAX_VALUE, vault);
        return withdrawCoins(player, amountToWithdraw);
    }

    public Guild getGuildByChunk(String dim, int chunkX, int chunkZ) {
        for (Guild g : guildsById.values()) {
            if (g.isChunkClaimed(dim, chunkX, chunkZ)) {
                return g;
            }
        }
        return null;
    }

    public boolean claimCurrentChunk(ServerPlayer player) {
        if (player == null) return false;
        return claimSpecificChunk(player, player.chunkPosition().x, player.chunkPosition().z);
    }

    public boolean claimSpecificChunk(ServerPlayer player, int chunkX, int chunkZ) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.claim_territory_no_permission"));
            return false;
        }

        String dim = player.level().dimension().location().toString();

        Guild existing = getGuildByChunk(dim, chunkX, chunkZ);
        if (existing != null) {
            if (existing.getId().equals(guild.getId())) {
                player.sendSystemMessage(Component.translatable("guild.msg.chunk_already_claimed_by_us"));
            } else {
                player.sendSystemMessage(Component.translatable("guild.msg.chunk_claimed_by_other", existing.getName(), existing.getTag()));
            }
            return false;
        }

        if (guild.getClaimedChunks().size() >= guild.getMaxClaims()) {
            player.sendSystemMessage(Component.translatable("guild.msg.claim_limit_reached", guild.getClaimedChunks().size(), guild.getMaxClaims(), guild.getLevel()));
            return false;
        }

        if (guild.claimChunk(dim, chunkX, chunkZ)) {
            setDirty();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            broadcastToGuild(guild, player.server, Component.translatable("guild.msg.claim_success_broadcast", player.getScoreboardName(), chunkX, chunkZ, guild.getClaimedChunks().size(), guild.getMaxClaims()));
            syncAllGuildMembers(guild, player.server);
            syncAllClaimsToAll(player.server);
            return true;
        }
        return false;
    }

    public boolean unclaimCurrentChunk(ServerPlayer player) {
        if (player == null) return false;
        return unclaimSpecificChunk(player, player.chunkPosition().x, player.chunkPosition().z);
    }

    public boolean unclaimSpecificChunk(ServerPlayer player, int chunkX, int chunkZ) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.claim_territory_no_permission"));
            return false;
        }

        String dim = player.level().dimension().location().toString();

        if (guild.unclaimChunk(dim, chunkX, chunkZ)) {
            setDirty();
            player.sendSystemMessage(Component.translatable("guild.msg.unclaim_success", chunkX, chunkZ, guild.getClaimedChunks().size(), guild.getMaxClaims()));
            syncAllGuildMembers(guild, player.server);
            syncAllClaimsToAll(player.server);
            return true;
        } else {
            player.sendSystemMessage(Component.translatable("guild.msg.chunk_not_claimed_by_us"));
            return false;
        }
    }

    public boolean unclaimAllChunks(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.translatable("guild.msg.only_leader_unclaim_all"));
            return false;
        }

        guild.unclaimAllChunks();
        setDirty();
        leader.sendSystemMessage(Component.translatable("guild.msg.unclaim_all_success"));
        syncAllGuildMembers(guild, leader.server);
        syncAllClaimsToAll(leader.server);
        return true;
    }

    public boolean unlockPerk(ServerPlayer leader, String perkId) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null) return false;

        GuildRole role = guild.getMemberRole(leader.getUUID());
        if (role == null || !role.canManagePerks()) {
            leader.sendSystemMessage(Component.translatable("guild.msg.only_leader_perks"));
            return false;
        }

        GuildPerk perk = GuildPerk.fromId(perkId);
        if (perk == null) {
            leader.sendSystemMessage(Component.translatable("guild.msg.perk_unknown"));
            return false;
        }

        if (guild.hasPerk(perk)) {
            leader.sendSystemMessage(Component.translatable("guild.msg.perk_already_unlocked"));
            return false;
        }

        if (guild.getLevel() < perk.getRequiredGuildLevel()) {
            leader.sendSystemMessage(Component.translatable("guild.msg.perk_level_needed", perk.getRequiredGuildLevel()));
            return false;
        }

        if (guild.getMoraVault() < perk.getCostMora()) {
            leader.sendSystemMessage(Component.translatable("guild.msg.perk_mora_needed", perk.getCostMora()));
            return false;
        }

        if (guild.unlockPerk(perk)) {
            setDirty();
            broadcastToGuild(guild, leader.server, Component.translatable("guild.msg.perk_unlocked_broadcast", perk.getTitle(), perk.getDescription()));
            syncAllGuildMembers(guild, leader.server);
            return true;
        }
        return false;
    }

    public boolean setGuildHq(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.translatable("guild.msg.only_leader_set_hq"));
            return false;
        }

        String dim = leader.level().dimension().location().toString();
        guild.setHq(dim, leader.getX(), leader.getY(), leader.getZ(), leader.getYRot(), leader.getXRot());
        setDirty();

        leader.sendSystemMessage(Component.translatable("guild.msg.hq_set_success"));
        broadcastToGuild(guild, leader.server, Component.translatable("guild.msg.hq_updated_broadcast"));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean teleportToHq(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        if (!guild.hasPerk(GuildPerk.WARP_BEACON) || !guild.hasHq()) {
            player.sendSystemMessage(Component.translatable("guild.msg.hq_not_established"));
            return false;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(guild.getHqDimension()));
        ServerLevel targetLevel = player.server.getLevel(dimKey);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.hq_dimension_failed"));
            return false;
        }

        player.teleportTo(targetLevel, guild.getHqX(), guild.getHqY(), guild.getHqZ(), guild.getHqYaw(), guild.getHqPitch());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("guild.msg.teleport_hq_success"));
        return true;
    }

    public void sendGuildChat(ServerPlayer player, String message) {
        if (player == null || message == null || message.isBlank()) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        String rolePrefix = role != null ? role.getLabel() : "";

        Component chatMsg = Component.literal("§6[Guild] §7" + rolePrefix + " §e" + player.getScoreboardName() + "§7: §f" + message);
        broadcastToGuild(guild, player.server, chatMsg);
    }

    public void broadcastToGuild(Guild guild, MinecraftServer server, Component msg) {
        if (guild == null || server == null) return;
        for (UUID uuid : guild.getMembers().keySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            if (p != null) {
                p.sendSystemMessage(msg);
            }
        }
    }

    public void syncAllGuildMembers(Guild guild, MinecraftServer server) {
        if (guild == null || server == null) return;
        for (UUID uuid : guild.getMembers().keySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            if (p != null) {
                syncToPlayer(p);
            }
        }
    }

    public void syncToPlayer(ServerPlayer player) {
        syncToPlayerWithTab(player, -1, false);
    }

    public void openGuildScreenForPlayer(ServerPlayer player) {
        syncToPlayerWithTab(player, -1, true);
    }

    public static int getGuildColor(String idOrTag) {
        if (idOrTag == null || idOrTag.isEmpty()) return 0x3498DB;
        int hash = Math.abs(idOrTag.hashCode());
        float hue = (hash % 360) / 360.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.75f, 0.90f) & 0xFFFFFF;
    }

    public CompoundTag getAllClaimsTag() {
        CompoundTag claims = new CompoundTag();
        CompoundTag details = new CompoundTag();
        for (Guild g : guildsById.values()) {
            for (String key : g.getClaimedChunks()) {
                claims.putString(key, g.getTag());
            }
            CompoundTag gData = new CompoundTag();
            gData.putString("id", g.getId());
            gData.putString("name", g.getName());
            gData.putString("tag", g.getTag());
            gData.putInt("level", g.getLevel());
            gData.putInt("members", g.getMembers().size());
            gData.putInt("color", getGuildColor(g.getId()));
            details.put(g.getId(), gData);
            details.put(g.getTag(), gData);
        }
        claims.put("_Guilds", details);
        return claims;
    }

    public void syncAllClaimsToPlayer(ServerPlayer player) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new SyncGuildClaimsPayload(getAllClaimsTag()));
    }

    public void syncAllClaimsToAll(MinecraftServer server) {
        if (server == null) return;
        PacketDistributor.sendToAllPlayers(new SyncGuildClaimsPayload(getAllClaimsTag()));
    }

    public void syncToPlayerWithTab(ServerPlayer player, int initialTab) {
        syncToPlayerWithTab(player, initialTab, true);
    }

    public void syncToPlayerWithTab(ServerPlayer player, int initialTab, boolean explicitOpen) {
        if (player == null) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ExplicitOpen", explicitOpen);
        if (guild != null) {
            tag.putBoolean("InGuild", true);
            tag.put("Guild", guild.save());
            tag.put("AllClaims", getAllClaimsTag());
            tag.put("AllGuilds", getAllGuildsSummaryTag(guild));

            // Sincronizar portales permanentes del gremio
            ListTag portalsList = new ListTag();
            for (DungeonManager.WorldRift rift : DungeonManager.getPermanentRifts()) {
                if (guild.getId().equals(rift.owningGuildId)) {
                    CompoundTag pt = new CompoundTag();
                    pt.putInt("x", rift.pos.getX());
                    pt.putInt("y", rift.pos.getY());
                    pt.putInt("z", rift.pos.getZ());
                    pt.putString("dimension", rift.dimension.location().toString());
                    pt.putString("rank", rift.rank);
                    pt.putString("biome", rift.biome);
                    pt.putInt("fee", rift.rentalFeeMora);
                    pt.putBoolean("feeInCoins", rift.feeInCoins);
                    boolean cooling = (rift.cooldownUntil > player.level().getGameTime());
                    pt.putBoolean("cooling", cooling);
                    portalsList.add(pt);
                }
            }
            tag.put("GuildPortals", portalsList);

            // Detectar portal permanente cercano (a <= 16 bloques)
            DungeonManager.WorldRift nearby = DungeonManager.findNearestPermanentRift(player.blockPosition(), player.level().dimension(), 16.0D);
            if (nearby != null) {
                CompoundTag nearTag = new CompoundTag();
                nearTag.putInt("x", nearby.pos.getX());
                nearTag.putInt("y", nearby.pos.getY());
                nearTag.putInt("z", nearby.pos.getZ());
                nearTag.putString("rank", nearby.rank);
                nearTag.putString("biome", nearby.biome);
                nearTag.putInt("fee", nearby.rentalFeeMora);
                nearTag.putBoolean("feeInCoins", nearby.feeInCoins);
                nearTag.putString("owningGuildId", nearby.owningGuildId != null ? nearby.owningGuildId : "");
                boolean isOwnedByMyGuild = guild.getId().equals(nearby.owningGuildId);
                nearTag.putBoolean("isOwnedByMyGuild", isOwnedByMyGuild);
                if (nearby.owningGuildId != null && !nearby.owningGuildId.isEmpty()) {
                    Guild owner = getGuildById(nearby.owningGuildId);
                    nearTag.putString("ownerName", owner != null ? owner.getName() : "Desconocido");
                    nearTag.putString("ownerTag", owner != null ? owner.getTag() : "???");
                }
                boolean cooling = (nearby.cooldownUntil > player.level().getGameTime());
                nearTag.putBoolean("cooling", cooling);
                tag.put("NearbyPortal", nearTag);
            }

            if (initialTab >= 0) {
                tag.putInt("InitialTab", initialTab);
            }
        } else {
            tag.putBoolean("InGuild", false);
            tag.put("AllGuilds", getAllGuildsSummaryTag(null));
        }
        PacketDistributor.sendToPlayer(player, new OpenGuildScreenPayload(tag));
    }

    public void requestWithdrawal(ServerPlayer player, String currency, int amount) {
        if (player == null) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return;
        }

        String currLabel = "COINS".equalsIgnoreCase(currency) ? "Coins ⭐" : "Mora 🪙";
        String formattedAmt = amount > 0 ? String.format("%,d", amount) + " " + currLabel : "fondos";

        Component officerMsg = Component.translatable("guild.msg.officer_withdraw_request", player.getScoreboardName(), formattedAmt);
        for (GuildMember m : guild.getMembers().values()) {
            if (m.getRole().canWithdraw()) {
                ServerPlayer officer = player.server.getPlayerList().getPlayer(m.getUuid());
                if (officer != null) {
                    officer.sendSystemMessage(officerMsg);
                }
            }
        }

        player.sendSystemMessage(Component.translatable("guild.msg.withdraw_request_sent"));
    }

    public boolean buyHuntingBoard(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.translatable("guild.msg.only_officers_buy_hunting_board"));
            return false;
        }

        boolean paid = guild.withdrawCoins(500);
        if (!paid) {
            paid = guild.withdrawMora(5000);
        }
        if (!paid) {
            player.sendSystemMessage(Component.translatable("guild.msg.hunting_board_insufficient_funds"));
            return false;
        }

        setDirty();
        ItemStack boardItem = new ItemStack(com.soybe.awakeningsystem.block.SystemBlocks.GUILD_HUNTING_BOARD.get());
        if (!player.getInventory().add(boardItem)) {
            player.drop(boardItem, false);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 1.0F, 1.0F);
        broadcastToGuild(guild, player.server, Component.translatable("guild.msg.hunting_board_bought_broadcast", player.getScoreboardName()));
        syncAllGuildMembers(guild, player.server);
        return true;
    }

    public ListTag getAllGuildsSummaryTag(Guild requestingGuild) {
        ListTag list = new ListTag();
        for (Guild g : guildsById.values()) {
            CompoundTag s = new CompoundTag();
            s.putString("Id", g.getId());
            s.putString("Name", g.getName());
            s.putString("Tag", g.getTag());
            s.putInt("Level", g.getLevel());
            s.putInt("MemberCount", g.getMembers().size());
            String leaderName = "Desconocido";
            GuildMember leader = g.getMember(g.getLeaderUuid());
            if (leader != null) {
                leaderName = leader.getName();
            }
            s.putString("LeaderName", leaderName);
            s.putBoolean("IsAlly", requestingGuild != null && requestingGuild.isAlliedWith(g.getId()));
            s.putBoolean("IsSelf", requestingGuild != null && requestingGuild.getId().equals(g.getId()));
            list.add(s);
        }
        return list;
    }

    public void toggleAlliance(ServerPlayer player, String targetGuildId) {
        if (player == null || targetGuildId == null || targetGuildId.isBlank()) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || role != GuildRole.LEADER) {
            player.sendSystemMessage(Component.translatable("guild.msg.only_leader_alliances"));
            return;
        }

        Guild targetGuild = getGuildById(targetGuildId);
        if (targetGuild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.alliance_guild_not_found"));
            return;
        }

        if (targetGuild.getId().equals(guild.getId())) {
            player.sendSystemMessage(Component.translatable("guild.msg.cannot_ally_self"));
            return;
        }

        if (guild.isAlliedWith(targetGuild.getId())) {
            guild.removeAlly(targetGuild.getId());
            targetGuild.removeAlly(guild.getId());
            setDirty();
            broadcastToGuild(guild, player.server, Component.translatable("guild.msg.alliance_dissolved_us", targetGuild.getTag(), targetGuild.getName()));
            broadcastToGuild(targetGuild, player.server, Component.translatable("guild.msg.alliance_dissolved_them", guild.getTag(), guild.getName()));
        } else {
            guild.addAlly(targetGuild.getId());
            targetGuild.addAlly(guild.getId());
            setDirty();
            broadcastToGuild(guild, player.server, Component.translatable("guild.msg.alliance_formed_us", targetGuild.getTag(), targetGuild.getName()));
            broadcastToGuild(targetGuild, player.server, Component.translatable("guild.msg.alliance_formed_them", guild.getTag(), guild.getName()));
        }

        syncAllGuildMembers(guild, player.server);
        syncAllGuildMembers(targetGuild, player.server);
        syncToPlayerWithTab(player, 5);
    }

    public boolean claimPortalAt(ServerPlayer player, String posStr) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_claim_no_permission"));
            return false;
        }

        DungeonManager.WorldRift rift = null;
        if (posStr != null && !posStr.isEmpty() && !posStr.equals("nearby")) {
            try {
                String[] p = posStr.contains(":") ? posStr.split(":") : posStr.split(",");
                if (p.length >= 3) {
                    BlockPos bPos = new BlockPos(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
                    rift = DungeonManager.getRiftAt(bPos, player.level().dimension());
                }
            } catch (Exception ignored) {}
        }
        if (rift == null) {
            rift = DungeonManager.findNearestPermanentRift(player.blockPosition(), player.level().dimension(), 16.0D);
        }

        if (rift == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_none_nearby"));
            return false;
        }

        if (rift.owningGuildId != null && !rift.owningGuildId.isEmpty()) {
            if (rift.owningGuildId.equals(guild.getId())) {
                player.sendSystemMessage(Component.translatable("guild.msg.portal_already_owned_by_us"));
                return false;
            }
            Guild other = getGuildById(rift.owningGuildId);
            if (other != null) {
                player.sendSystemMessage(Component.translatable("guild.msg.portal_claimed_by_other", other.getName()));
                return false;
            }
        }

        rift.owningGuildId = guild.getId();
        DungeonWorldSavedData.get(player.server).setDirty();
        broadcastToGuild(guild, player.server, Component.translatable("guild.msg.portal_claimed_broadcast", role.getLabel(), player.getScoreboardName(), rift.rank, rift.pos.toShortString()));
        player.level().playSound(null, rift.pos.getX(), rift.pos.getY(), rift.pos.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    public boolean claimNearbyPortal(ServerPlayer player) {
        return claimPortalAt(player, "");
    }

    public boolean unclaimPortalAt(ServerPlayer player, String posStr) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_unclaim_no_permission"));
            return false;
        }

        DungeonManager.WorldRift rift = null;
        if (posStr != null && !posStr.isEmpty() && !posStr.equals("nearby")) {
            try {
                String[] p = posStr.contains(":") ? posStr.split(":") : posStr.split(",");
                if (p.length >= 3) {
                    BlockPos bPos = new BlockPos(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
                    rift = DungeonManager.getRiftAt(bPos, player.level().dimension());
                }
            } catch (Exception ignored) {}
        }
        if (rift == null) {
            rift = DungeonManager.findNearestPermanentRift(player.blockPosition(), player.level().dimension(), 16.0D);
        }

        if (rift == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_not_found_to_release"));
            return false;
        }

        if (rift.owningGuildId == null || !rift.owningGuildId.equals(guild.getId())) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_not_owned_by_us"));
            return false;
        }

        rift.owningGuildId = null;
        DungeonWorldSavedData.get(player.server).setDirty();
        broadcastToGuild(guild, player.server, Component.translatable("guild.msg.portal_released_broadcast", rift.pos.toShortString()));
        return true;
    }

    public boolean unclaimNearbyPortal(ServerPlayer player) {
        return unclaimPortalAt(player, "");
    }

    public boolean setPortalRentalFeeAt(ServerPlayer player, String posStr, int fee) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_fee_no_permission"));
            return false;
        }

        DungeonManager.WorldRift rift = null;
        if (posStr != null && !posStr.isEmpty() && !posStr.equals("nearby")) {
            try {
                String[] p = posStr.contains(":") ? posStr.split(":") : posStr.split(",");
                if (p.length >= 3) {
                    BlockPos bPos = new BlockPos(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
                    rift = DungeonManager.getRiftAt(bPos, player.level().dimension());
                }
            } catch (Exception ignored) {}
        }
        if (rift == null) {
            rift = DungeonManager.findNearestPermanentRift(player.blockPosition(), player.level().dimension(), 16.0D);
        }

        if (rift == null || rift.owningGuildId == null || !rift.owningGuildId.equals(guild.getId())) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_not_found_for_guild"));
            return false;
        }

        rift.rentalFeeMora = Math.max(0, Math.min(100000000, fee));
        DungeonWorldSavedData.get(player.server).setDirty();
        return true;
    }

    public boolean setPortalRentalFee(ServerPlayer player, int fee) {
        return setPortalRentalFeeAt(player, "", fee);
    }

    public boolean togglePortalCurrencyAt(ServerPlayer player, String posStr) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_currency_no_permission"));
            return false;
        }

        DungeonManager.WorldRift rift = null;
        if (posStr != null && !posStr.isEmpty() && !posStr.equals("nearby")) {
            try {
                String[] p = posStr.contains(":") ? posStr.split(":") : posStr.split(",");
                if (p.length >= 3) {
                    BlockPos bPos = new BlockPos(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
                    rift = DungeonManager.getRiftAt(bPos, player.level().dimension());
                }
            } catch (Exception ignored) {}
        }
        if (rift == null) {
            rift = DungeonManager.findNearestPermanentRift(player.blockPosition(), player.level().dimension(), 16.0D);
        }

        if (rift == null || rift.owningGuildId == null || !rift.owningGuildId.equals(guild.getId())) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_not_found_for_guild"));
            return false;
        }

        rift.feeInCoins = !rift.feeInCoins;
        DungeonWorldSavedData.get(player.server).setDirty();
        return true;
    }

    public void listGuildPortals(ServerPlayer player) {
        if (player == null) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return;
        }

        List<DungeonManager.WorldRift> portals = new ArrayList<>();
        for (DungeonManager.WorldRift rift : DungeonManager.getPermanentRifts()) {
            if (guild.getId().equals(rift.owningGuildId)) {
                portals.add(rift);
            }
        }

        if (portals.isEmpty()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_list_empty"));
            return;
        }

        player.sendSystemMessage(Component.translatable("guild.msg.portal_list_header", guild.getName()));
        for (DungeonManager.WorldRift rift : portals) {
            boolean cooling = (rift.cooldownUntil > player.level().getGameTime());
            String status = cooling ? "§c⏳ Cooldown" : "§a✔ Active";
            String curr = rift.feeInCoins ? "🪙 Coins" : "Mora";
            player.sendSystemMessage(Component.translatable("guild.msg.portal_list_entry", rift.rank, rift.biome, rift.pos.toShortString(), rift.rentalFeeMora, curr, status));
        }
        player.sendSystemMessage(Component.literal("§6════════════════════════════════════════════════"));
    }

    public boolean requestPortalRent(ServerPlayer player, int x, int y, int z) {
        if (player == null) return false;
        BlockPos targetPos = new BlockPos(x, y, z);
        DungeonManager.WorldRift rift = DungeonManager.getRiftAt(targetPos, player.level().dimension());
        if (rift == null || !rift.isPermanent) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_not_found_at_coords"));
            return false;
        }

        if (player.distanceToSqr(x + 0.5D, y + 1.0D, z + 0.5D) > 20.0D * 20.0D) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_too_far"));
            return false;
        }

        if (rift.owningGuildId == null || rift.owningGuildId.isEmpty()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_no_guild"));
            return false;
        }

        Guild owningGuild = getGuildById(rift.owningGuildId);
        if (owningGuild == null) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_guild_extinct"));
            return false;
        }

        Guild playerGuild = getGuildByPlayer(player.getUUID());
        if (playerGuild != null && (playerGuild.getId().equals(owningGuild.getId()) || owningGuild.isAlliedWith(playerGuild.getId()))) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_is_ally_or_member"));
            return false;
        }

        if (rift.isRenterAuthorized(player.getUUID(), player.level().getGameTime())) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_already_rented"));
            return false;
        }

        SystemData pData = SystemEvents.data(player);
        int playerTier = DungeonManager.getRankTier(SystemData.rankForLevel(pData.level()));
        int riftTier = DungeonManager.getRankTier(rift.rank);
        int minRequiredTier = Math.max(1, riftTier - 1);
        if (playerTier < minRequiredTier) {
            String minRankName = DungeonManager.getRepresentativeRankForTier(minRequiredTier);
            player.sendSystemMessage(Component.translatable("guild.msg.portal_rank_too_low", minRankName));
            return false;
        }

        if (rift.feeInCoins) {
            if (pData.coins() < rift.rentalFeeMora) {
                player.sendSystemMessage(Component.translatable("guild.msg.portal_rent_insufficient_coins", pData.coins(), rift.rentalFeeMora));
                return false;
            }
        } else {
            if (pData.mora() < rift.rentalFeeMora) {
                player.sendSystemMessage(Component.translatable("guild.msg.portal_rent_insufficient_mora", pData.mora(), rift.rentalFeeMora));
                return false;
            }
        }

        List<ServerPlayer> onlineOfficers = new ArrayList<>();
        for (UUID memberId : owningGuild.getMembers().keySet()) {
            GuildRole role = owningGuild.getMemberRole(memberId);
            if (role != null && (role == GuildRole.LEADER || role == GuildRole.OFFICER)) {
                ServerPlayer sp = player.server.getPlayerList().getPlayer(memberId);
                if (sp != null) {
                    onlineOfficers.add(sp);
                }
            }
        }

        if (onlineOfficers.isEmpty()) {
            player.sendSystemMessage(Component.translatable("guild.msg.portal_no_officers_online", owningGuild.getName()));
            return false;
        }

        String currName = rift.feeInCoins ? "Coins 🪙" : "Mora";
        String playerRankStr = SystemData.rankForLevel(pData.level());
        Component requestNotif = Component.literal("§6═══════ ")
                .append(Component.translatable("guild.msg.portal_rent_request_title"))
                .append(" §6═══════\n")
                .append(Component.translatable("guild.msg.portal_rent_request_hunter", player.getScoreboardName(), playerRankStr)).append("\n")
                .append(Component.translatable("guild.msg.portal_rent_request_detail", rift.rank, x, y, z, rift.rentalFeeMora, currName)).append("\n")
                .append(Component.literal("  ")
                        .append(Component.translatable("guild.msg.portal_rent_btn_accept")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withBold(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild portal rent accept " + player.getUUID() + " " + x + " " + y + " " + z))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("guild.msg.portal_rent_hover_accept", rift.rentalFeeMora, currName))))))
                        .append(Component.literal("   "))
                        .append(Component.translatable("guild.msg.portal_rent_btn_deny")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.RED)
                                        .withBold(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild portal rent deny " + player.getUUID() + " " + x + " " + y + " " + z))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("guild.msg.portal_rent_hover_deny")))))
                .append(Component.literal("\n§6═════════════════════════════════════════════"));

        for (ServerPlayer officer : onlineOfficers) {
            officer.sendSystemMessage(requestNotif);
            officer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        player.sendSystemMessage(Component.translatable("guild.msg.portal_rent_request_sent", owningGuild.getName()));
        return true;
    }

    public boolean acceptPortalRent(ServerPlayer officer, UUID requesterUuid, int x, int y, int z) {
        if (officer == null) return false;
        Guild guild = getGuildByPlayer(officer.getUUID());
        if (guild == null) {
            officer.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
            return false;
        }

        GuildRole role = guild.getMemberRole(officer.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            officer.sendSystemMessage(Component.translatable("guild.msg.portal_rent_accept_no_permission"));
            return false;
        }

        BlockPos targetPos = new BlockPos(x, y, z);
        DungeonManager.WorldRift rift = DungeonManager.getRiftAt(targetPos, officer.level().dimension());
        if (rift == null || !guild.getId().equals(rift.owningGuildId)) {
            officer.sendSystemMessage(Component.translatable("guild.msg.portal_not_owned_by_us"));
            return false;
        }

        ServerPlayer requester = officer.server.getPlayerList().getPlayer(requesterUuid);
        if (requester == null) {
            officer.sendSystemMessage(Component.translatable("guild.msg.portal_requester_offline"));
            return false;
        }

        SystemData reqData = SystemEvents.data(requester);
        if (rift.feeInCoins) {
            if (reqData.coins() < rift.rentalFeeMora) {
                officer.sendSystemMessage(Component.translatable("guild.msg.portal_officer_requester_no_coins", rift.rentalFeeMora));
                requester.sendSystemMessage(Component.translatable("guild.msg.portal_requester_no_coins"));
                return false;
            }
            reqData.spendCoins(rift.rentalFeeMora);
            guild.depositCoins(rift.rentalFeeMora);
        } else {
            if (reqData.mora() < rift.rentalFeeMora) {
                officer.sendSystemMessage(Component.translatable("guild.msg.portal_officer_requester_no_mora", rift.rentalFeeMora));
                requester.sendSystemMessage(Component.translatable("guild.msg.portal_requester_no_mora"));
                return false;
            }
            reqData.spendMora(rift.rentalFeeMora);
            guild.depositMora(rift.rentalFeeMora);
        }
        AwakeningSystemMod.sync(requester);
        setDirty();

        long expiry = officer.server.overworld().getGameTime() + 24000L;
        rift.authorizeRenter(requester.getUUID(), expiry);
        DungeonWorldSavedData.get(officer.server).setDirty();

        String currLabel = rift.feeInCoins ? "Coins 🪙" : "Mora";
        broadcastToGuild(guild, officer.server, Component.translatable("guild.msg.portal_rent_accepted_broadcast", role.getLabel(), officer.getScoreboardName(), requester.getScoreboardName(), rift.rentalFeeMora, currLabel));
        requester.sendSystemMessage(Component.translatable("guild.msg.portal_rent_approved", guild.getName(), rift.rank));
        requester.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    public boolean denyPortalRent(ServerPlayer officer, UUID requesterUuid, int x, int y, int z) {
        if (officer == null) return false;
        Guild guild = getGuildByPlayer(officer.getUUID());
        if (guild == null) return false;

        GuildRole role = guild.getMemberRole(officer.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            officer.sendSystemMessage(Component.translatable("guild.msg.portal_rent_deny_no_permission"));
            return false;
        }

        officer.sendSystemMessage(Component.translatable("guild.msg.portal_rent_denied_officer"));
        ServerPlayer requester = officer.server.getPlayerList().getPlayer(requesterUuid);
        if (requester != null) {
            requester.sendSystemMessage(Component.translatable("guild.msg.portal_rent_denied_requester", guild.getName()));
            requester.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
