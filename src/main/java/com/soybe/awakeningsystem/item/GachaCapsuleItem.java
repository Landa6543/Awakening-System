package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

public class GachaCapsuleItem extends Item {
    private static final Random RNG = new Random();

    public GachaCapsuleItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            held.shrink(1);
            ServerLevel sl = (ServerLevel) level;
            SystemData data = SystemEvents.data(sp);

            // Sonido de apertura
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    ModSounds.GACHA_OPEN_PRIZE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            // Partículas mágicas iniciales
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.08D);

            // === 0. JACKPOT DEL CREADOR (0.008%) ===
            if (RNG.nextDouble() < 0.00008D) {
                ItemStack creatorSword = new ItemStack(SystemItems.CREATOR_CLAYMORE.get());
                giveOrDrop(sp, creatorSword);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 1.5F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        net.minecraft.sounds.SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 150, 1.2D, 1.2D, 1.2D, 0.25D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 100, 0.8D, 0.8D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 1.0D, 1.0D, 1.0D, 0.1D);

                Component creatorAnnounce = Component.literal("§6§l✦✦✦✦✦✦✦ ¡¡¡CÁPSULA DEL CREADOR (0.008%)!!! §e" + sp.getName().getString() +
                        " §fha desbloqueado la reliquia suprema: §6" + creatorSword.getHoverName().getString() + "§6§l! ✦✦✦✦✦✦✦");
                for (ServerPlayer online : sp.server.getPlayerList().getPlayers()) {
                    online.displayClientMessage(creatorAnnounce, false);
                }
                return InteractionResultHolder.consume(held);
            }

            // === 0.1 PREMIO DE LA DONCELLA (0.020% / ~1 en 5000 cápsulas) ===
            if (RNG.nextDouble() < 0.00020D) {
                ItemStack doncellaSword = new ItemStack(SystemItems.LA_DONCELLA.get());
                giveOrDrop(sp, doncellaSword);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 80, 0.8D, 0.8D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);

                Component doncellaAnnounce = Component.literal("§d§l✦✦✦✦✦ ¡CÁPSULA MÍSTICA (0.020%)! §e" + sp.getName().getString() +
                        " §fha obtenido: §d" + doncellaSword.getHoverName().getString() + "§d§l! ✦✦✦✦✦");
                for (ServerPlayer online : sp.server.getPlayerList().getPlayers()) {
                    online.displayClientMessage(doncellaAnnounce, false);
                }
                return InteractionResultHolder.consume(held);
            }

            int roll = RNG.nextInt(100);

            if (roll >= 90) {
                // === 5★ LEGENDARIO (10%) ===
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        ModSounds.GACHA_LEGENDARY_PRIZE.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 50, 0.6D, 0.6D, 0.6D, 0.2D);
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

                int subRoll = RNG.nextInt(3);
                if (subRoll == 0) {
                    ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get());
                    giveOrDrop(sp, lightCone);
                    sp.sendSystemMessage(Component.literal("§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: Noche en la Vía Láctea§e!"), false);
                } else if (subRoll == 1) {
                    ItemStack fate = new ItemStack(SystemItems.INTERTWINED_FATE.get());
                    giveOrDrop(sp, fate);
                    sp.sendSystemMessage(Component.literal("§6§l¡✦✦✦✦✦ RECOMPENSA DORADA! §e¡Has obtenido §d1x Destino Entrelazado§e!"), false);
                } else {
                    int jackpotMora = 100000;
                    data.addMora(jackpotMora);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.sendSystemMessage(Component.literal("§6§l¡✦✦✦✦✦ BOLSA DEL DRAGÓN! §e¡Has ganado §6+100,000 Mora§e directa a tu monedero!"), false);
                }
            } else if (roll >= 65) {
                // === 4★ RARO (25%) ===
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        ModSounds.GACHA_RARE_PRIZE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                sl.sendParticles(ParticleTypes.WITCH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

                int subRoll = RNG.nextInt(3);
                if (subRoll == 0) {
                    ItemStack starglitter = new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get(), 4);
                    giveOrDrop(sp, starglitter);
                    sp.sendSystemMessage(Component.literal("§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §d4x Brillo Estelar§f."), false);
                } else if (subRoll == 1) {
                    ItemStack pills = new ItemStack(SystemItems.RISING_DRAGON_PILL.get(), 2);
                    giveOrDrop(sp, pills);
                    sp.sendSystemMessage(Component.literal("§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §b2x Píldora del Dragón Naciente§f."), false);
                } else {
                    int moraWon = 25000;
                    data.addMora(moraWon);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.sendSystemMessage(Component.literal("§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §6+25,000 Mora§f."), false);
                }
            } else {
                // === 3★ COMÚN (65%) ===
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        ModSounds.GACHA_COMMON_PRIZE.get(), SoundSource.PLAYERS, 0.9F, 1.0F);

                int subRoll = RNG.nextInt(3);
                if (subRoll == 0) {
                    ItemStack exp = new ItemStack(SystemItems.HEROS_WIT.get(), 3);
                    giveOrDrop(sp, exp);
                    sp.sendSystemMessage(Component.literal("§b✦✦✦ Recompensa: §fHas obtenido §93x Ingenio del Héroe§f."), false);
                } else if (subRoll == 1) {
                    ItemStack stardust = new ItemStack(SystemItems.MASTERLESS_STARDUST.get(), 15);
                    giveOrDrop(sp, stardust);
                    sp.sendSystemMessage(Component.literal("§b✦✦✦ Recompensa: §fHas obtenido §b15x Polvo Estelar§f."), false);
                } else {
                    int moraWon = 7500;
                    data.addMora(moraWon);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.sendSystemMessage(Component.literal("§b✦✦✦ Recompensa: §fHas obtenido §6+7,500 Mora§f."), false);
                }
            }

            return InteractionResultHolder.consume(held);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        SystemData data = SystemEvents.data(player);
        boolean[] sentToSubspace = new boolean[]{false};
        int[] counter = new int[]{0};
        com.soybe.awakeningsystem.GachaManager.deliverGachaReward(player, data, stack, sentToSubspace, counter);
        if (sentToSubspace[0]) {
            player.sendSystemMessage(Component.literal("§6✦ [SISTEMA] §e¡Tu inventario estaba lleno! §fEl premio (" + stack.getHoverName().getString() + ") se guardó en tu §eSubespacio del Sistema§f."));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.CHEST_OPEN, SoundSource.PLAYERS, 0.8f, 1.2f);
            com.soybe.awakeningsystem.AwakeningSystemMod.sync(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.literal("§7Cápsula mágica sellada obtenida del Gachapon de Teyvat.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§eHaz clic derecho §7para abrir y revelar su tesoro.").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§6✦ Posibles Premios: §fMandoble del Creador, La Doncella, Conos de Luz 3D, Destinos, Mora.").withStyle(ChatFormatting.GOLD));
    }
}
