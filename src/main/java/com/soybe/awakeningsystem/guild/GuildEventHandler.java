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
            // Si el modo permanente está activo y el jugador quiere enviar un mensaje global rápido con '!'
            if (isToggled && raw.startsWith("!") && !raw.startsWith("!g ")) {
                return;
            }

            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild == null) {
                GUILD_CHAT_MODE_PLAYERS.remove(player.getUUID());
                player.sendSystemMessage(Component.literal("§cNo perteneces a ningún gremio."));
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
        // 1. Reducción de daño para miembros con bendición de Fortaleza Colectiva
        if (event.getEntity() instanceof ServerPlayer victim && victim.server != null) {
            Guild guild = GuildSavedData.get(victim.server).getGuildByPlayer(victim.getUUID());
            if (guild != null && guild.hasPerk(GuildPerk.COLLECTIVE_FORTITUDE)) {
                event.setNewDamage(event.getNewDamage() * 0.92F); // -8% daño recibido
            }
        }

        // 2. Aumento de daño para miembros atacantes con Poder del Gremio o Resonancia
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker.server != null) {
            Guild guild = GuildSavedData.get(attacker.server).getGuildByPlayer(attacker.getUUID());
            if (guild != null) {
                float multiplier = 1.0F;

                if (guild.hasPerk(GuildPerk.GUILD_MIGHT)) {
                    multiplier += 0.10F; // +10% daño
                }

                if (guild.hasPerk(GuildPerk.DUNGEON_RESONANCE)) {
                    LivingEntity target = event.getEntity();
                    boolean isBoss = target instanceof EnderDragon || target instanceof WitherBoss
                            || target.getMaxHealth() >= 80.0F
                            || target.getTags().contains("boss")
                            || target.getTags().contains("dungeon_boss");
                    if (isBoss) {
                        multiplier += 0.15F; // +15% contra jefes
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
                event.setDroppedExperience((int) Math.round(original * 1.15)); // +15% EXP
                // Otorgar también EXP al gremio
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
                // 35% de probabilidad de drop extra al cazar:
                // MORAS únicamente si se encuentra en Teyvat; MONEDAS DE DESPERTAR en cualquier otra dimensión
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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server != null && player.tickCount % 40 == 0) {
            Guild guild = GuildSavedData.get(player.server).getGuildByPlayer(player.getUUID());
            if (guild != null && guild.hasPerk(GuildPerk.REGENERATION_AURA)) {
                // Regenerar vida sutilmente
                if (player.getHealth() < player.getMaxHealth() && player.getFoodData().getFoodLevel() > 6) {
                    player.heal(0.5F);
                }
                // Regenerar maná en SystemData
                SystemData data = SystemEvents.data(player);
                if (data.mana() < data.maxMana()) {
                    data.setMana(Math.min(data.maxMana(), data.mana() + 5));
                    AwakeningSystemMod.sync(player);
                }
            }
        }
    }

    // ==========================================
    // ✦ PROTECCIÓN DE TERRITORIO (ANTI-GRIEFING)
    // ==========================================

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.server != null && !player.isCreative()) {
            String dim = player.level().dimension().location().toString();
            int cx = event.getPos().getX() >> 4;
            int cz = event.getPos().getZ() >> 4;

            Guild owner = GuildSavedData.get(player.server).getGuildByChunk(dim, cx, cz);
            if (owner != null && !owner.isMember(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c🛡️ [Territorio Protegido] Este terreno pertenece al gremio " + owner.getName() + " [" + owner.getTag() + "]."), true);
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
                player.sendSystemMessage(Component.literal("§c🛡️ [Territorio Protegido] No puedes construir en el territorio de " + owner.getName() + " [" + owner.getTag() + "]."), true);
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
                // Prevenir interacción con cofres, barriles, puertas, trampillas, tolvas, yunques, botones y palancas
                boolean isProtected = state.getBlock() instanceof net.minecraft.world.level.block.EntityBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.ButtonBlock
                        || state.getBlock() instanceof net.minecraft.world.level.block.LeverBlock;
                if (isProtected) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c🛡️ [Territorio Protegido] Este mecanismo/contenedor pertenece a " + owner.getName() + " [" + owner.getTag() + "]."), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        if (event.getLevel().getServer() != null) {
            String dim = event.getLevel().dimension().location().toString();
            var data = GuildSavedData.get(event.getLevel().getServer());
            // Proteger todos los bloques en chunks reclamados contra destrucción por explosiones (Creepers, TNT, etc.)
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
                boolean isBoss = victim instanceof EnderDragon || victim instanceof WitherBoss
                        || victim.getMaxHealth() >= 80.0F
                        || victim.getTags().contains("boss")
                        || victim.getTags().contains("dungeon_boss");
                if (isBoss) {
                    guild.addExp(300);
                    guild.progressHunt("boss", 1, player.server);
                    GuildMember m = guild.getMember(player.getUUID());
                    if (m != null) m.addContribution(150);
                    GuildSavedData.get(player.server).setDirty();
                    player.sendSystemMessage(Component.literal("§6⚔️ ¡Tu gremio ha recibido §e+300 EXP §6por derrotar un Jefe!"), true);
                }

                String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).getPath().toLowerCase();
                boolean isAbyss = entityId.contains("void") || entityId.contains("abyss") || entityId.contains("shadow")
                        || entityId.contains("hilichurl") || entityId.contains("specter") || entityId.contains("cultist")
                        || entityId.contains("wither") || victim.level().dimension().location().getPath().contains("end");

                if (isAbyss) {
                    guild.progressHunt("abyss", 1, player.server);
                }

                boolean isElite = victim.getPersistentData().contains("MobTier")
                        || victim.getMaxHealth() >= 50.0F
                        || victim.getPersistentData().getBoolean("IsMiniBoss");

                if (isElite) {
                    guild.progressHunt("elite", 1, player.server);
                }
            }
        }
    }
}
