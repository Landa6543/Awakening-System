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
import net.minecraft.sounds.SoundEvents;
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
    public enum CapsuleRarity {
        BLUE(3, Rarity.RARE, "§9✦✦✦ Gacha Capsule (3★)"),
        PURPLE(4, Rarity.EPIC, "§d✦✦✦✦ Epic Gacha Capsule (4★)"),
        GOLD(5, Rarity.EPIC, "§6✦✦✦✦✦ Legendary Gacha Capsule (5★)");

        private final int stars;
        private final Rarity rarity;
        private final String displayName;

        CapsuleRarity(int stars, Rarity rarity, String displayName) {
            this.stars = stars;
            this.rarity = rarity;
            this.displayName = displayName;
        }

        public int getStars() { return stars; }
        public Rarity getRarity() { return rarity; }
        public String getDisplayName() { return displayName; }
    }

    private static final Random RNG = new Random();
    private final CapsuleRarity capsuleRarity;

    public GachaCapsuleItem(Properties properties) {
        this(properties, CapsuleRarity.BLUE);
    }

    public GachaCapsuleItem(Properties properties, CapsuleRarity capsuleRarity) {
        super(properties.rarity(capsuleRarity.getRarity()).stacksTo(16));
        this.capsuleRarity = capsuleRarity;
    }

    public CapsuleRarity getCapsuleRarity() {
        return capsuleRarity;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return capsuleRarity == CapsuleRarity.GOLD || super.isFoil(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            SystemData data = SystemEvents.data(sp);

            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.2F, 1.1F);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);

            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.08D);

            ItemStack rewardStack = rollReward(sp, data, level, sl);

            boolean isCreative = sp.getAbilities().instabuild;
            boolean isSingle = (held.getCount() == 1);

            if (!isCreative) {
                held.shrink(1);
            }

            if (isSingle && !isCreative) {
                if (!rewardStack.isEmpty()) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        sp.getInventory().setItem(sp.getInventory().selected, rewardStack.copy());
                    } else {
                        sp.getInventory().offhand.set(0, rewardStack.copy());
                    }
                    sp.inventoryMenu.broadcastChanges();
                    sp.containerMenu.broadcastChanges();
                    return InteractionResultHolder.sidedSuccess(rewardStack, level.isClientSide());
                } else {
                    sp.inventoryMenu.broadcastChanges();
                    sp.containerMenu.broadcastChanges();
                    return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
                }
            } else {
                if (!rewardStack.isEmpty()) {
                    deliverRewardExcludingHand(sp, hand, rewardStack);
                }
                sp.inventoryMenu.broadcastChanges();
                sp.containerMenu.broadcastChanges();
                return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
            }
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private ItemStack rollReward(ServerPlayer sp, SystemData data, Level level, ServerLevel sl) {
        double creatorChance = capsuleRarity == CapsuleRarity.GOLD ? 0.0008D :
                              (capsuleRarity == CapsuleRarity.PURPLE ? 0.0002D : 0.00008D);
        double doncellaChance = capsuleRarity == CapsuleRarity.GOLD ? 0.0020D :
                               (capsuleRarity == CapsuleRarity.PURPLE ? 0.0005D : 0.00020D);

        if (RNG.nextDouble() < creatorChance) {
            ItemStack creatorSword = new ItemStack(SystemItems.CREATOR_CLAYMORE.get());
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 1.5F);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 150, 1.2D, 1.2D, 1.2D, 0.25D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 100, 0.8D, 0.8D, 0.8D, 0.2D);
            sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 1.0D, 1.0D, 1.0D, 0.1D);

            Component creatorAnnounce = Component.translatable("item.awakening_system.gacha_capsule.creator_announce", sp.getName(), creatorSword.getHoverName());
            for (ServerPlayer online : sp.server.getPlayerList().getPlayers()) {
                online.displayClientMessage(creatorAnnounce, false);
            }
            return creatorSword;
        }

        if (RNG.nextDouble() < doncellaChance) {
            ItemStack doncellaSword = new ItemStack(SystemItems.LA_DONCELLA.get());
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 80, 0.8D, 0.8D, 0.8D, 0.2D);
            sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);

            Component doncellaAnnounce = Component.translatable("item.awakening_system.gacha_capsule.doncella_announce", sp.getName(), doncellaSword.getHoverName());
            for (ServerPlayer online : sp.server.getPlayerList().getPlayers()) {
                online.displayClientMessage(doncellaAnnounce, false);
            }
            return doncellaSword;
        }

        int tier;
        if (capsuleRarity == CapsuleRarity.GOLD) {
            tier = 5;
        } else if (capsuleRarity == CapsuleRarity.PURPLE) {
            tier = RNG.nextInt(100) < 25 ? 5 : 4;
        } else {
            int roll = RNG.nextInt(100);
            if (roll < 10) {
                tier = 5;
            } else if (roll < 40) {
                tier = 4;
            } else {
                tier = 3;
            }
        }

        if (tier == 5) {
            return giveTier5Reward(sp, data, level, sl);
        } else if (tier == 4) {
            return giveTier4Reward(sp, data, level, sl);
        } else {
            return giveTier3Reward(sp, data, level, sl);
        }
    }

    private ItemStack giveTier5Reward(ServerPlayer sp, SystemData data, Level level, ServerLevel sl) {
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.2F);
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 80, 0.8D, 0.8D, 0.8D, 0.25D);
        sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 50, 0.6D, 0.6D, 0.6D, 0.15D);
        sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.1D);

        int subRoll = RNG.nextInt(7);
        if (subRoll == 0) {
            ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_light_cone", lightCone.getHoverName()), false);
            return lightCone;
        } else if (subRoll == 1) {
            ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_light_cone", lightCone.getHoverName()), false);
            return lightCone;
        } else if (subRoll == 2) {
            ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_light_cone", lightCone.getHoverName()), false);
            return lightCone;
        } else if (subRoll == 3) {
            ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_light_cone", lightCone.getHoverName()), false);
            return lightCone;
        } else if (subRoll == 4) {
            ItemStack lightCone = new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_light_cone", lightCone.getHoverName()), false);
            return lightCone;
        } else if (subRoll == 5) {
            ItemStack fate = new ItemStack(SystemItems.INTERTWINED_FATE.get());
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_fate"), false);
            return fate;
        } else {
            int jackpotMora = 100000;
            data.addMora(jackpotMora);
            com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier5_mora"), false);
            return ItemStack.EMPTY;
        }
    }

    private ItemStack giveTier4Reward(ServerPlayer sp, SystemData data, Level level, ServerLevel sl) {
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.5F);
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.85F);
        sl.sendParticles(ParticleTypes.WITCH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 0.6D, 0.6D, 0.6D, 0.1D);
        sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);

        int subRoll = RNG.nextInt(6);
        if (subRoll == 0) {
            Item[] runes = new Item[]{
                    SystemItems.RUNE_STONE_RULERS_AUTHORITY.get(),
                    SystemItems.RUNE_STONE_DRAGON_EYES.get(),
                    SystemItems.RUNE_STONE_BLOODTHIRST.get(),
                    SystemItems.RUNE_STONE_SHADOW_STEP.get(),
                    SystemItems.RUNE_STONE_FOURTH_WALL.get(),
                    SystemItems.RUNE_STONE_IRON_BODY.get()
            };
            ItemStack runeStack = new ItemStack(runes[RNG.nextInt(runes.length)]);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier4_reward", runeStack.getHoverName()), false);
            return runeStack;
        } else if (subRoll == 1) {
            ItemStack starglitter = new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get(), 4);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier4_starglitter"), false);
            return starglitter;
        } else if (subRoll == 2) {
            ItemStack pills = new ItemStack(SystemItems.RISING_DRAGON_PILL.get(), 2);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier4_pills"), false);
            return pills;
        } else if (subRoll == 3) {
            ItemStack sigils = new ItemStack(SystemItems.GUARDIANS_SIGIL.get(), 2);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier4_sigils"), false);
            return sigils;
        } else {
            int moraWon = 25000;
            data.addMora(moraWon);
            com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier4_mora"), false);
            return ItemStack.EMPTY;
        }
    }

    private ItemStack giveTier3Reward(ServerPlayer sp, SystemData data, Level level, ServerLevel sl) {
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.1D);

        int subRoll = RNG.nextInt(4);
        if (subRoll == 0) {
            ItemStack exp = new ItemStack(SystemItems.HEROS_WIT.get(), 3);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier3_exp"), false);
            return exp;
        } else if (subRoll == 1) {
            ItemStack stardust = new ItemStack(SystemItems.MASTERLESS_STARDUST.get(), 15);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier3_stardust"), false);
            return stardust;
        } else if (subRoll == 2) {
            Item[] sigilPool = new Item[]{
                    SystemItems.ANEMO_SIGIL.get(),
                    SystemItems.GEO_SIGIL.get(),
                    SystemItems.ELECTRO_SIGIL.get(),
                    SystemItems.DENDRO_SIGIL.get()
            };
            ItemStack sigil = new ItemStack(sigilPool[RNG.nextInt(sigilPool.length)], 3);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier3_sigil", sigil.getHoverName()), false);
            return sigil;
        } else {
            int moraWon = 7500;
            data.addMora(moraWon);
            com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
            sp.sendSystemMessage(Component.translatable("item.awakening_system.gacha_capsule.tier3_mora"), false);
            return ItemStack.EMPTY;
        }
    }

    private static void deliverRewardExcludingHand(ServerPlayer player, InteractionHand hand, ItemStack rewardStack) {
        if (rewardStack == null || rewardStack.isEmpty()) return;

        SystemData data = SystemEvents.data(player);
        int excludedSlot = (hand == InteractionHand.MAIN_HAND) ? player.getInventory().selected : -1;
        ItemStack remaining = rewardStack.copy();

        if (remaining.isStackable()) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                if (i == excludedSlot) continue;
                ItemStack slotStack = player.getInventory().items.get(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                    int space = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (space > 0) {
                        int toMove = Math.min(space, remaining.getCount());
                        slotStack.grow(toMove);
                        remaining.shrink(toMove);
                        if (remaining.isEmpty()) {
                            player.inventoryMenu.broadcastChanges();
                            return;
                        }
                    }
                }
            }
        }

        int targetSlot = -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (i == excludedSlot) continue;
            if (player.getInventory().items.get(i).isEmpty()) {
                targetSlot = i;
                break;
            }
        }

        if (targetSlot >= 0) {
            player.getInventory().items.set(targetSlot, remaining.copy());
            remaining.setCount(0);
            player.inventoryMenu.broadcastChanges();
            return;
        }

        ItemStack leftover = data.addToSubspace(remaining);
        player.sendSystemMessage(Component.literal("§6✦ [SYSTEM] §eYour inventory was full! §fReward (" + rewardStack.getHoverName().getString() + ") was stored in your §eSystem Subspace§f."));
        player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.PLAYERS, 0.8f, 1.2f);
        com.soybe.awakeningsystem.AwakeningSystemMod.sync(player);

        if (!leftover.isEmpty()) {
            player.drop(leftover, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (capsuleRarity == CapsuleRarity.GOLD) {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_gold").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_gold").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.guarantee_gold").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_gold").withStyle(ChatFormatting.GOLD));
        } else if (capsuleRarity == CapsuleRarity.PURPLE) {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_purple").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_purple").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rewards_purple").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_purple").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_blue").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_blue").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rewards_blue").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_blue").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.open").withStyle(ChatFormatting.YELLOW));
    }
}
