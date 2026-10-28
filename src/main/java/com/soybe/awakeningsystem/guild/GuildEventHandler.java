package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.neoforge.event.ServerChatEvent;
import net.minecraft.network.chat.Component;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GuildEventHandler {

    public static final Set<UUID> GUILD_CHAT_MODE_PLAYERS = ConcurrentHashMap.newKeySet();
    private record PlayerChunkLoc(net.minecraft.resources.ResourceLocation dim, long chunkPos) {}
    private static final java.util.Map<UUID, PlayerChunkLoc> LAST_PLAYER_CHUNK = new ConcurrentHashMap<>();

    public static boolean toggleGuildChat(ServerPlayer player) {
        if (GUILD_CHAT_MODE_PLAYERS.contains(player.getUUID())) {
            GUILD_CHAT_MODE_PLAYERS.remove(player.getUUID());
            return false;
        } else {
            GUILD_CHAT_MODE_PLAYERS.add(player.getUUID());
            return true;
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null || player.server == null) return;
        String raw = event.getRawText();
        if (raw == null || raw.isBlank()) return;

        raw = raw.trim();
        boolean hasGuildPrefix = raw.startsWith("#") || raw.startsWith("@g ") || raw.startsWith("!g ");
        boolean isToggled = GUILD_CHAT_MODE_PLAYERS.contains(player.getUUID());

        if (hasGuildPrefix || isToggled) {
            if (isToggled && raw.startsWith("!") && !raw.startsWith("!g ")) {
                return;
            }

            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild == null) {
                GUILD_CHAT_MODE_PLAYERS.remove(player.getUUID());
                player.sendSystemMessage(Component.translatable("guild.msg.not_in_guild"));
                event.setCanceled(true);
                return;
            }

            String msg = raw;
            if (raw.startsWith("#")) {
                msg = raw.substring(1).trim();
            } else if (raw.startsWith("@g ") || raw.startsWith("!g ")) {
                msg = raw.substring(3).trim();
            }

            if (!msg.isEmpty()) {
                event.setCanceled(true);
                GuildSavedData.get(player.server).sendGuildChat(player, msg);
            }
        }
    }

    @SubscribeEvent
    public static void onCombatDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer victim && event.getSource().getEntity() instanceof ServerPlayer attacker && attacker.server != null) {
            Guild vGuild = GuildSavedData.get(victim.server).getGuildByPlayer(victim.getUUID());
            Guild aGuild = GuildSavedData.get(attacker.server).getGuildByPlayer(attacker.getUUID());
            if (vGuild != null && aGuild != null && (vGuild.getId().equals(aGuild.getId()) || vGuild.isAlliedWith(aGuild.getId()))) {
                event.setNewDamage(0.0F);
                return;
            }
        }

        if (event.getEntity() instanceof ServerPlayer victim && victim.server != null) {
            Guild guild = GuildSavedData.get(victim.server).getGuildByPlayer(victim.getUUID());
            if (guild != null && guild.hasPerk(GuildPerk.COLLECTIVE_FORTITUDE)) {
                event.setNewDamage(event.getNewDamage() * 0.92F);
            }
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker.server != null) {
            Guild guild = GuildSavedData.get(attacker.server).getGuildByPlayer(attacker.getUUID());
            if (guild != null) {
                float multiplier = 1.0F;

                if (guild.hasPerk(GuildPerk.GUILD_MIGHT)) {
                    multiplier += 0.10F;
                }

                if (guild.hasPerk(GuildPerk.DUNGEON_RESONANCE)) {
                    LivingEntity target = event.getEntity();
                    boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(target);
                    if (isBoss) {
                        multiplier += 0.15F;
                    }
                }

                if (multiplier > 1.0F) {
                    event.setNewDamage(event.getNewDamage() * multiplier);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ServerPlayer player && player.server != null) {
            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild != null && guild.hasPerk(GuildPerk.BOND_OF_HUNTERS)) {
                int original = event.getDroppedExperience();
                event.setDroppedExperience((int) Math.round(original * 1.15));
                guild.addExp(Math.max(1, original / 2));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && player.server != null) {
            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild != null && guild.hasPerk(GuildPerk.COMMON_TREASURY)) {
                LivingEntity entity = event.getEntity();
                if (player.getRandom().nextFloat() < 0.35F) {
                    if (entity.level().dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD)) {
                        if (SystemItems.MORA != null && SystemItems.MORA.isBound()) {
                            int moraBonus = 1 + player.getRandom().nextInt(3);
                            ItemStack moraStack = new ItemStack(SystemItems.MORA.get(), moraBonus);
                            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), moraStack);
                            event.getDrops().add(itemEntity);
                        }
                    } else {
                        if (SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                            int coinBonus = 1 + player.getRandom().nextInt(4);
                            ItemStack coinStack = new ItemStack(SystemItems.AWAKENING_COIN.get(), coinBonus);
                            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), coinStack);
                            event.getDrops().add(itemEntity);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server != null) {
            com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enforcePlayerBoundary(player);
            GuildSavedData.get(player.server).syncAllClaimsToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_PLAYER_CHUNK.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server != null) {
            com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enforcePlayerBoundary(player);
            if (player.tickCount % 5 == 0) {
                net.minecraft.resources.ResourceLocation dim = player.level().dimension().location();
                long cPos = player.chunkPosition().toLong();
                PlayerChunkLoc prev = LAST_PLAYER_CHUNK.put(player.getUUID(), new PlayerChunkLoc(dim, cPos));
                if (prev != null && (prev.chunkPos() != cPos || !prev.dim().equals(dim))) {
                    checkTerritoryTransition(player, prev.dim().toString(), net.minecraft.world.level.ChunkPos.getX(prev.chunkPos()), net.minecraft.world.level.ChunkPos.getZ(prev.chunkPos()),
                            dim.toString(), player.chunkPosition().x, player.chunkPosition().z);
                }
            }

            if (player.tickCount % 40 == 0) {
                Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
                if (guild != null && guild.hasPerk(GuildPerk.REGENERATION_AURA)) {
                    if (player.getHealth() < player.getMaxHealth() && player.getFoodData().getFoodLevel() > 6) {
                        player.heal(0.5F);
                    }
                    SystemData data = SystemEvents.data(player);
                    if (data.mana() < data.maxMana()) {
                        data.setMana(Math.min(data.maxMana(), data.mana() + 5));
                        AwakeningSystemMod.sync(player);
                    }
                }
            }
        }
    }

    private static void checkTerritoryTransition(ServerPlayer player, String prevDim, int prevX, int prevZ, String currDim, int currX, int currZ) {
        try {
            GuildSavedData data = GuildSavedData.get(player.server);
            Guild prevGuild = data.getGuildByChunk(prevDim, prevX, prevZ);
            Guild currGuild = data.getGuildByChunk(currDim, currX, currZ);

            String prevId = prevGuild != null ? prevGuild.getId() : "";
            String currId = currGuild != null ? currGuild.getId() : "";

            if (prevId.equals(currId)) {
                return;
            }

            if (currGuild != null) {
                if (currGuild.isMember(player.getUUID())) {
                    player.sendSystemMessage(Component.translatable("guild.territory.enter_own", currGuild.getName(), currGuild.getTag()), true);
                    player.playNotifySound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.2F);
                } else {
                    player.sendSystemMessage(Component.translatable("guild.territory.enter_other", currGuild.getName(), currGuild.getTag(), currGuild.getLevel()), true);
                    player.playNotifySound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 0.8F);
                }
            } else if (prevGuild != null) {
                player.sendSystemMessage(Component.translatable("guild.territory.wilderness"), true);
                player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.4F, 1.0F);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Error updating chunk guild message for player: {}", e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.server != null && !player.isCreative()) {
            String dim = player.level().dimension().location().toString();
            int cx = event.getPos().getX() >> 4;
            int cz = event.getPos().getZ() >> 4;

            Guild owner = GuildSavedData.get(player.server).getGuildByChunk(dim, cx, cz);
            if (owner != null && !owner.isMember(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("guild.territory.protected_break", owner.getName(), owner.getTag()), true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server != null && !player.isCreative()) {
            String dim = player.level().dimension().location().toString();
            int cx = event.getPos().getX() >> 4;
            int cz = event.getPos().getZ() >> 4;

            Guild owner = GuildSavedData.get(player.server).getGuildByChunk(dim, cx, cz);
            if (owner != null && !owner.isMember(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("guild.territory.protected_place", owner.getName(), owner.getTag()), true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server != null && !player.isCreative()) {
            String dim = player.level().dimension().location().toString();
            int cx = event.getPos().getX() >> 4;
            int cz = event.getPos().getZ() >> 4;

            Guild owner = GuildSavedData.get(player.server).getGuildByChunk(dim, cx, cz);
            if (owner != null && !owner.isMember(player.getUUID())) {
                net.minecraft.world.level.block.state.BlockState state = event.getLevel().getBlockState(event.getPos());
                boolean isProtected = state.getBlock() instanceof net.minecraft.world.level.block.EntityBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.ButtonBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.LeverBlock;
                if (isProtected) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.translatable("guild.territory.protected_interact", owner.getName(), owner.getTag()), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        if (event.getLevel().getServer() != null) {
            String dim = event.getLevel().dimension().location().toString();
            var data = GuildSavedData.get(event.getLevel().getServer());
            event.getAffectedBlocks().removeIf(pos -> {
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                return data.getGuildByChunk(dim, cx, cz) != null;
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && player.server != null) {
            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild != null) {
                LivingEntity victim = event.getEntity();
                boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(victim);
                if (isBoss) {
                    guild.addExp(300);
                    guild.progressHunt("boss", 1, player.server);
                    GuildMember m = guild.getMember(player.getUUID());
                    if (m != null) m.addContribution(150);
                    GuildSavedData.get(player.server).setDirty();
                    player.displayClientMessage(Component.translatable("guild.awakening_system.boss_exp_reward"), true);
                }

                boolean isTowerOrInstance = victim.level().dimension().location().getPath().contains("ranker_tower")
                        || victim.level().dimension().location().getPath().contains("pagoda_realm")
                        || victim.getPersistentData().getBoolean("TowerMinion")
                        || victim.getPersistentData().getBoolean("TowerChampion")
                        || victim.getPersistentData().getBoolean("DungeonMinion")
                        || victim.getPersistentData().getBoolean("PagodaTrialMob");

                if (!isTowerOrInstance) {
                    String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).getPath().toLowerCase();
                    boolean isAbyss = entityId.contains("void") || entityId.contains("abyss") || entityId.contains("shadow")
                            || entityId.contains("hilichurl") || entityId.contains("specter") || entityId.contains("cultist")
                            || entityId.contains("wither") || victim.level().dimension().location().getPath().contains("end");

                    if (isAbyss) {
                        guild.progressHunt("abyss", 1, player.server);
                    }

                    boolean isElite = victim.getPersistentData().contains("MobTier")
                            || victim.getPersistentData().getBoolean("IsMiniBoss")
                            || victim.getTags().contains("elite");

                    if (isElite) {
                        guild.progressHunt("elite", 1, player.server);
                    }
                }
            }
        }
    }
}
