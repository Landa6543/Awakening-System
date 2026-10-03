package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
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

    // ==========================================
    // ACCIONES DE GREMIO (CORE LOGIC)
    // ==========================================

    public boolean createGuild(ServerPlayer creator, String name, String tag) {
        if (creator == null) return false;
        UUID uuid = creator.getUUID();

        if (playerToGuildId.containsKey(uuid)) {
            creator.sendSystemMessage(Component.literal("§cYa eres miembro de un gremio. Debes abandonarlo primero."));
            return false;
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 3 || trimmedName.length() > 16) {
            creator.sendSystemMessage(Component.literal("§cEl nombre del gremio debe tener entre 3 y 16 caracteres."));
            return false;
        }

        String trimmedTag = tag.trim().toUpperCase(Locale.ROOT);
        if (trimmedTag.length() < 2 || trimmedTag.length() > 5) {
            creator.sendSystemMessage(Component.literal("§cEl TAG del gremio debe tener entre 2 y 5 caracteres (ej. AHJ)."));
            return false;
        }

        if (guildNameToId.containsKey(trimmedName.toLowerCase(Locale.ROOT))) {
            creator.sendSystemMessage(Component.literal("§cYa existe un gremio con ese nombre."));
            return false;
        }

        // Costo de creación: 1,000 Monedas del Sistema
        int creationCost = 1000;
        SystemData sData = creator.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (sData.coins() < creationCost) {
            creator.sendSystemMessage(Component.literal("§cNo tienes suficientes Monedas del Sistema para fundar un gremio (Necesitas 1,000 Monedas)."));
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

        Component announcement = Component.literal("§6⚔ ¡El Cazador §e" + creator.getScoreboardName() + " §6ha fundado el gremio §f[" + trimmedTag + "] §b" + trimmedName + "§6!");
        creator.server.getPlayerList().broadcastSystemMessage(announcement, false);

        syncToPlayer(creator);
        return true;
    }

    public boolean disbandGuild(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder supremo puede disolver el gremio."));
            return false;
        }

        broadcastToGuild(guild, leader.server, Component.literal("§c☠ El gremio [" + guild.getTag() + "] " + guild.getName() + " ha sido disuelto por su Líder."));

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
        return true;
    }

    public boolean invitePlayer(ServerPlayer inviter, ServerPlayer target) {
        if (inviter == null || target == null) return false;
        Guild guild = getGuildByPlayer(inviter.getUUID());
        if (guild == null) {
            inviter.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(inviter.getUUID());
        if (role == null || !role.canInvite()) {
            inviter.sendSystemMessage(Component.literal("§cNo tienes permisos de Oficial para invitar nuevos miembros."));
            return false;
        }

        if (playerToGuildId.containsKey(target.getUUID())) {
            inviter.sendSystemMessage(Component.literal("§cEse cazador ya pertenece a un gremio."));
            return false;
        }

        // Registrar invitación válida por 60 segundos
        pendingInvites.put(target.getUUID(), new PendingInvite(guild.getId(), inviter.getUUID(), System.currentTimeMillis() + 60000L));

        inviter.sendSystemMessage(Component.literal("§aHas invitado a §e" + target.getScoreboardName() + " §aal gremio §f[" + guild.getTag() + "] " + guild.getName() + "§a."));

        Component inviteMsg = Component.literal("§6✦ Has recibido una invitación para unirte al gremio §f[" + guild.getTag() + "] §b" + guild.getName() + "§6 de parte de §e" + inviter.getScoreboardName() + "§6.\n")
                .append(Component.literal("§a[✔ ACEPTAR] ").withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Clic para unirte al gremio")))))
                .append(Component.literal(" §c[✕ RECHAZAR]").withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild decline"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Clic para rechazar la invitación")))));

        target.sendSystemMessage(inviteMsg);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    public boolean acceptInvite(ServerPlayer target) {
        if (target == null) return false;
        PendingInvite invite = pendingInvites.remove(target.getUUID());
        if (invite == null || System.currentTimeMillis() > invite.expiresAt()) {
            target.sendSystemMessage(Component.literal("§cNo tienes ninguna invitación pendiente o ha expirado."));
            return false;
        }

        Guild guild = getGuildById(invite.guildId());
        if (guild == null) {
            target.sendSystemMessage(Component.literal("§cEl gremio ya no existe."));
            return false;
        }

        guild.addMember(new GuildMember(target.getUUID(), target.getScoreboardName(), GuildRole.RECRUIT));
        playerToGuildId.put(target.getUUID(), guild.getId());
        setDirty();

        broadcastToGuild(guild, target.server, Component.literal("§a✦ ¡El cazador §e" + target.getScoreboardName() + " §ase ha unido al gremio!"));
        syncToPlayer(target);
        return true;
    }

    public boolean declineInvite(ServerPlayer target) {
        if (target == null) return false;
        PendingInvite invite = pendingInvites.remove(target.getUUID());
        if (invite != null) {
            target.sendSystemMessage(Component.literal("§7Has rechazado la invitación del gremio."));
            return true;
        }
        return false;
    }

    public boolean leaveGuild(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        if (guild.getLeaderUuid().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cEres el Líder del gremio. Transfiere el liderazgo o usa §e/guild disband §cpara disolverlo."));
            return false;
        }

        guild.removeMember(player.getUUID());
        playerToGuildId.remove(player.getUUID());
        setDirty();

        player.sendSystemMessage(Component.literal("§eHas abandonado el gremio."));
        broadcastToGuild(guild, player.server, Component.literal("§e✦ El cazador " + player.getScoreboardName() + " ha dejado el gremio."));
        syncToPlayer(player);
        return true;
    }

    public boolean kickMember(ServerPlayer officer, UUID targetUuid) {
        if (officer == null) return false;
        Guild guild = getGuildByPlayer(officer.getUUID());
        if (guild == null) return false;

        GuildRole oRole = guild.getMemberRole(officer.getUUID());
        if (oRole == null || !oRole.canKick()) {
            officer.sendSystemMessage(Component.literal("§cNo tienes permisos para expulsar miembros."));
            return false;
        }

        GuildMember targetMember = guild.getMember(targetUuid);
        if (targetMember == null) {
            officer.sendSystemMessage(Component.literal("§cEse miembro no está en el gremio."));
            return false;
        }

        if (targetMember.getRole().getLevel() >= oRole.getLevel()) {
            officer.sendSystemMessage(Component.literal("§cNo puedes expulsar a alguien con tu mismo rango o superior."));
            return false;
        }

        guild.removeMember(targetUuid);
        playerToGuildId.remove(targetUuid);
        setDirty();

        broadcastToGuild(guild, officer.server, Component.literal("§c✦ " + targetMember.getName() + " ha sido expulsado del gremio por " + officer.getScoreboardName() + "."));

        ServerPlayer targetPlayer = officer.server.getPlayerList().getPlayer(targetUuid);
        if (targetPlayer != null) {
            targetPlayer.sendSystemMessage(Component.literal("§cHas sido expulsado del gremio."));
            syncToPlayer(targetPlayer);
        }
        return true;
    }

    public boolean promoteMember(ServerPlayer leader, UUID targetUuid) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder puede ascender miembros."));
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
            leader.sendSystemMessage(Component.literal("§cEse miembro ya tiene el rango máximo antes de Líder."));
            return false;
        }

        m.setRole(newRole);
        setDirty();
        broadcastToGuild(guild, leader.server, Component.literal("§a✦ " + m.getName() + " ha sido ascendido a " + newRole.getLabel() + "§a!"));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean demoteMember(ServerPlayer leader, UUID targetUuid) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder puede degradar miembros."));
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
            leader.sendSystemMessage(Component.literal("§cEse miembro ya tiene el rango más bajo."));
            return false;
        }

        m.setRole(newRole);
        setDirty();
        broadcastToGuild(guild, leader.server, Component.literal("§e✦ " + m.getName() + " ha sido degradado a " + newRole.getLabel() + "§e."));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean depositMora(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data.mora() < amount) {
            player.sendSystemMessage(Component.literal("§cNo tienes suficientes Moras para donar (Tienes: " + data.mora() + ")."));
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

        broadcastToGuild(guild, player.server, Component.literal("§6🪙 §e" + player.getScoreboardName() + " §6ha donado §e" + amount + " Moras §6al Fondo del Gremio (+ " + (amount / 500L) + " EXP)."));
        syncAllGuildMembers(guild, player.server);
        return true;
    }

    public boolean depositCoins(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data.coins() < amount) {
            player.sendSystemMessage(Component.literal("§cNo tienes suficientes Monedas del Sistema para donar (Tienes: " + data.coins() + ")."));
            return false;
        }

        if (!data.spendCoins(amount)) {
            player.sendSystemMessage(Component.literal("§cError al procesar las monedas."));
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

        broadcastToGuild(guild, player.server, Component.literal("§e⭐ §f" + player.getScoreboardName() + " §eha donado §f" + amount + " Monedas del Sistema §eal Fondo del Gremio (+ " + (amount / 5L) + " EXP)."));
        syncAllGuildMembers(guild, player.server);
        return true;
    }

    public boolean withdrawMora(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.literal("§c[Gremio] Solo los Altos Mandos (Líder u Oficiales) están autorizados a retirar fondos de la Tesorería."));
            return false;
        }

        if (guild.getMoraVault() < amount) {
            player.sendSystemMessage(Component.literal("§c[Gremio] No hay suficientes Moras en la Tesorería (Saldo: " + guild.getMoraVault() + ")."));
            return false;
        }

        if (guild.withdrawMora(amount)) {
            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            data.addMora(amount);
            AwakeningSystemMod.sync(player);
            setDirty();

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

            broadcastToGuild(guild, player.server, Component.literal("§6🏦 " + role.getLabel() + " §e" + player.getScoreboardName() + " §6ha retirado §e" + amount + " Moras §6de la Tesorería."));
            syncAllGuildMembers(guild, player.server);
            return true;
        }
        return false;
    }

    public boolean withdrawCoins(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.literal("§c[Gremio] Solo los Altos Mandos (Líder u Oficiales) están autorizados a retirar fondos de la Tesorería."));
            return false;
        }

        if (guild.getCoinVault() < amount) {
            player.sendSystemMessage(Component.literal("§c[Gremio] No hay suficientes Monedas del Sistema en la Tesorería (Saldo: " + guild.getCoinVault() + ")."));
            return false;
        }

        if (guild.withdrawCoins(amount)) {
            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            data.addCoins(amount);
            AwakeningSystemMod.sync(player);
            setDirty();

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

            broadcastToGuild(guild, player.server, Component.literal("§6🏦 " + role.getLabel() + " §e" + player.getScoreboardName() + " §6ha retirado §e" + amount + " Monedas del Sistema §6de la Tesorería."));
            syncAllGuildMembers(guild, player.server);
            return true;
        }
        return false;
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
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.literal("§cSolo los Altos Mandos (Líder u Oficiales) pueden reclamar territorio."));
            return false;
        }

        String dim = player.level().dimension().location().toString();

        Guild existing = getGuildByChunk(dim, chunkX, chunkZ);
        if (existing != null) {
            if (existing.getId().equals(guild.getId())) {
                player.sendSystemMessage(Component.literal("§eEste chunk ya pertenece a tu gremio."));
            } else {
                player.sendSystemMessage(Component.literal("§cEste chunk ya está reclamado por el gremio " + existing.getName() + " [" + existing.getTag() + "]."));
            }
            return false;
        }

        if (guild.getClaimedChunks().size() >= guild.getMaxClaims()) {
            player.sendSystemMessage(Component.literal("§cLímite de territorio alcanzado (" + guild.getClaimedChunks().size() + "/" + guild.getMaxClaims() + " chunks para Nivel " + guild.getLevel() + "). Sube de nivel el gremio para expandirte."));
            return false;
        }

        if (guild.claimChunk(dim, chunkX, chunkZ)) {
            setDirty();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            broadcastToGuild(guild, player.server, Component.literal("§a⛳ " + player.getScoreboardName() + " ha reclamado el chunk [" + chunkX + ", " + chunkZ + "] para el Gremio (" + guild.getClaimedChunks().size() + "/" + guild.getMaxClaims() + ")."));
            syncAllGuildMembers(guild, player.server);
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
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canClaimTerritory()) {
            player.sendSystemMessage(Component.literal("§cSolo los Altos Mandos (Líder u Oficiales) pueden liberar territorio."));
            return false;
        }

        String dim = player.level().dimension().location().toString();

        if (guild.unclaimChunk(dim, chunkX, chunkZ)) {
            setDirty();
            player.sendSystemMessage(Component.literal("§e✔ Chunk [" + chunkX + ", " + chunkZ + "] liberado (" + guild.getClaimedChunks().size() + "/" + guild.getMaxClaims() + ")."));
            syncAllGuildMembers(guild, player.server);
            return true;
        } else {
            player.sendSystemMessage(Component.literal("§cEste chunk no estaba reclamado por tu gremio."));
            return false;
        }
    }

    public boolean unclaimAllChunks(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder supremo puede liberar todo el territorio del gremio."));
            return false;
        }

        guild.unclaimAllChunks();
        setDirty();
        leader.sendSystemMessage(Component.literal("§e✔ Se han liberado todos los chunks reclamados del gremio."));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean unlockPerk(ServerPlayer leader, String perkId) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null) return false;

        GuildRole role = guild.getMemberRole(leader.getUUID());
        if (role == null || !role.canManagePerks()) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder puede desbloquear bendiciones del gremio."));
            return false;
        }

        GuildPerk perk = GuildPerk.fromId(perkId);
        if (perk == null) {
            leader.sendSystemMessage(Component.literal("§cBendición no reconocida."));
            return false;
        }

        if (guild.hasPerk(perk)) {
            leader.sendSystemMessage(Component.literal("§cEsa bendición ya está desbloqueada."));
            return false;
        }

        if (guild.getLevel() < perk.getRequiredGuildLevel()) {
            leader.sendSystemMessage(Component.literal("§cEl gremio necesita alcanzar Nivel " + perk.getRequiredGuildLevel() + " para esta bendición."));
            return false;
        }

        if (guild.getMoraVault() < perk.getCostMora()) {
            leader.sendSystemMessage(Component.literal("§cEl fondo del gremio no tiene suficientes Moras (Necesita " + perk.getCostMora() + " Moras)."));
            return false;
        }

        if (guild.unlockPerk(perk)) {
            setDirty();
            broadcastToGuild(guild, leader.server, Component.literal("§6✨ ¡El Gremio ha desbloqueado la bendición §e" + perk.getTitle() + "§6! (" + perk.getDescription() + ")"));
            syncAllGuildMembers(guild, leader.server);
            return true;
        }
        return false;
    }

    public boolean setGuildHq(ServerPlayer leader) {
        if (leader == null) return false;
        Guild guild = getGuildByPlayer(leader.getUUID());
        if (guild == null || !guild.getLeaderUuid().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cSolo el Líder supremo puede establecer la Sede del gremio."));
            return false;
        }

        String dim = leader.level().dimension().location().toString();
        guild.setHq(dim, leader.getX(), leader.getY(), leader.getZ(), leader.getYRot(), leader.getXRot());
        setDirty();

        leader.sendSystemMessage(Component.literal("§a✔ Sede del gremio establecida en tus coordenadas actuales."));
        broadcastToGuild(guild, leader.server, Component.literal("§b🌀 La Sede del gremio ha sido actualizada por el Líder. Usa §e/guild home §bpara viajar."));
        syncAllGuildMembers(guild, leader.server);
        return true;
    }

    public boolean teleportToHq(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        if (!guild.hasPerk(GuildPerk.WARP_BEACON) || !guild.hasHq()) {
            player.sendSystemMessage(Component.literal("§cEl gremio aún no tiene una Sede establecida o faro de teletransporte."));
            return false;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(guild.getHqDimension()));
        ServerLevel targetLevel = player.server.getLevel(dimKey);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal("§cNo se pudo acceder a la dimensión de la Sede."));
            return false;
        }

        player.teleportTo(targetLevel, guild.getHqX(), guild.getHqY(), guild.getHqZ(), guild.getHqYaw(), guild.getHqPitch());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.literal("§a🌀 Has sido transportado a la Sede del Gremio."));
        return true;
    }

    public void sendGuildChat(ServerPlayer player, String message) {
        if (player == null || message == null || message.isBlank()) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        String rolePrefix = role != null ? role.getLabel() : "";

        Component chatMsg = Component.literal("§6[Gremio] §7" + rolePrefix + " §e" + player.getScoreboardName() + "§7: §f" + message);
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
        syncToPlayerWithTab(player, -1);
    }

    public CompoundTag getAllClaimsTag() {
        CompoundTag claims = new CompoundTag();
        for (Guild g : guildsById.values()) {
            for (String key : g.getClaimedChunks()) {
                claims.putString(key, g.getTag());
            }
        }
        return claims;
    }

    public void syncToPlayerWithTab(ServerPlayer player, int initialTab) {
        if (player == null) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        CompoundTag tag = new CompoundTag();
        if (guild != null) {
            tag.putBoolean("InGuild", true);
            tag.put("Guild", guild.save());
            tag.put("AllClaims", getAllClaimsTag());
            if (initialTab >= 0) {
                tag.putInt("InitialTab", initialTab);
            }
        } else {
            tag.putBoolean("InGuild", false);
        }
        PacketDistributor.sendToPlayer(player, new OpenGuildScreenPayload(tag));
    }

    public void requestWithdrawal(ServerPlayer player, String currency, int amount) {
        if (player == null) return;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return;
        }

        String currLabel = "COINS".equalsIgnoreCase(currency) ? "Monedas ⭐" : "Mora 🪙";
        String formattedAmt = amount > 0 ? String.format("%,d", amount) + " " + currLabel : "fondos";

        Component officerMsg = Component.literal("§e📩 [Gremio] §f" + player.getScoreboardName() + " §7ha solicitado un retiro de §6" + formattedAmt + " §7de la Bóveda a los Altos Mandos.");
        for (GuildMember m : guild.getMembers().values()) {
            if (m.getRole().canWithdraw()) {
                ServerPlayer officer = player.server.getPlayerList().getPlayer(m.getUuid());
                if (officer != null) {
                    officer.sendSystemMessage(officerMsg);
                }
            }
        }

        player.sendSystemMessage(Component.literal("§a✔ [Gremio] Solicitud de retiro enviada a los Altos Mandos del gremio."));
    }

    public boolean buyHuntingBoard(ServerPlayer player) {
        if (player == null) return false;
        Guild guild = getGuildByPlayer(player.getUUID());
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
            return false;
        }

        GuildRole role = guild.getMemberRole(player.getUUID());
        if (role == null || !role.canWithdraw()) {
            player.sendSystemMessage(Component.literal("§cSolo el Líder y Sublíderes pueden adquirir el Tablón de Cacerías oficial."));
            return false;
        }

        // Costo: 500 Monedas o 5,000 Moras del fondo del gremio
        boolean paid = guild.withdrawCoins(500);
        if (!paid) {
            paid = guild.withdrawMora(5000);
        }
        if (!paid) {
            player.sendSystemMessage(Component.literal("§cEl gremio no dispone de 500 Monedas (o 5,000 Moras) en la Bóveda para el Tablón."));
            return false;
        }

        setDirty();
        ItemStack boardItem = new ItemStack(com.soybe.awakeningsystem.block.SystemBlocks.GUILD_HUNTING_BOARD.get());
        if (!player.getInventory().add(boardItem)) {
            player.drop(boardItem, false);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 1.0F, 1.0F);
        broadcastToGuild(guild, player.server, Component.literal("§6📜 §e" + player.getScoreboardName() + " §6ha adquirido un §eTablón de Cacerías §6para la Sede del Gremio (-5,000 Moras de Bóveda)."));
        syncAllGuildMembers(guild, player.server);
        return true;
    }
}
