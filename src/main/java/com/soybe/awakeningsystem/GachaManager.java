package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.CompanionProfile;
import com.soybe.awakeningsystem.item.CompanionContractItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GachaManager {
    public static final int BANNER_UMA = 11;
    public static final int BANNER_STELLAR_WARP = 12;
    public static final int BANNER_PROMO_2 = 13;
    private static final Random RNG = new Random();

    public static ItemStack createUmaContract(com.soybe.awakeningsystem.uma.UmaProfile profile, int rarity, boolean isNew, int shards) {
        ItemStack stack = new ItemStack(SystemItems.COMPANION_CONTRACT.get());
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("UmaId", profile.getId());
        tag.putInt("UmaRarity", rarity);
        tag.putBoolean("UmaNew", isNew);
        tag.putInt("UmaShards", shards);
        tag.putInt("UmaStatues", rarity == 3 ? 20 : (rarity == 2 ? 3 : 1));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        String col = rarity == 3 ? "§6§l" : (rarity == 2 ? "§d§l" : "§b§l");
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(col + profile.getDisplayName() + " (" + "★".repeat(rarity) + ")"));
        return stack;
    }

    private GachaManager() {}

    public static void openGachaScreen(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        if (!data.teyvatUnlocked()) {
            player.sendSystemMessage(Component.translatable("awakening_system.gacha.unlock_required"));
            return;
        }
        VersionBannerManager.checkAndAwardMaintenanceCompensation(player, data);

        List<String> histStrings = new ArrayList<>();
        for (SystemData.WishLogEntry entry : data.wishHistory()) {
            histStrings.add(entry.itemName() + "::" + entry.stars() + "::" + entry.bannerName() + "::" + entry.timestamp());
        }

        long currentDay = player.serverLevel().getDayTime() / 24000L;
        boolean unclaimedWelkin = (data.welkinDays() > 0 && currentDay > data.lastWelkinClaimDay());

        long epochTick = data.gachaEpochTick();
        if (epochTick == 0L) {
            epochTick = player.serverLevel().getDayTime();
            data.setGachaEpochTick(epochTick);
        }
        PromotionalBanner.setBannerEpochTick(epochTick);

        PacketDistributor.sendToPlayer(player, new OpenGachaScreenPayload(
                data.primogems(),
                data.intertwinedFates(),
                data.acquaintFates(),
                data.stardust(),
                data.starglitter(),
                data.gachaPity5Star(),
                data.gachaPity4Star(),
                data.gachaGuaranteedRateUp(),
                data.beginnerPulls(),
                data.mora(),
                data.weaponBannerTarget(),
                data.weaponBannerFatePoints(),
                data.characterBannerTarget(),
                data.coins(),
                data.usdBalance(),
                data.genesisCrystals(),
                data.stellarJade(),
                data.welkinDays(),
                data.totalGachaPulls(),
                epochTick,
                histStrings
        ));

        if (unclaimedWelkin) {
            int reward = data.isWelkinEnhanced() ? 135 : 90;
            PacketDistributor.sendToPlayer(player, new OpenWelkinScreenPayload(
                    data.welkinDays(),
                    reward,
                    true,
                    data.isWelkinEnhanced(),
                    data.welkinQuestStep(),
                    data.welkinQuestProgress()
            ));
        }
    }

    public static void deliverGachaReward(ServerPlayer player, SystemData data, ItemStack stack, boolean[] sentToSubspace, int[] subspaceCounter) {
        if (stack == null || stack.isEmpty()) return;
        ItemStack remaining = stack.copy();

        if (remaining.isStackable()) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack slotStack = player.getInventory().items.get(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                    int max = slotStack.getMaxStackSize();
                    int space = max - slotStack.getCount();
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

        int freeSlot = player.getInventory().getFreeSlot();
        if (freeSlot >= 0) {
            player.getInventory().items.set(freeSlot, remaining.copy());
            remaining.setCount(0);
            player.inventoryMenu.broadcastChanges();
            return;
        }

        ItemStack leftover = data.addToSubspace(remaining);
        if (sentToSubspace != null && sentToSubspace.length > 0) sentToSubspace[0] = true;
        if (subspaceCounter != null && subspaceCounter.length > 0) subspaceCounter[0]++;

        if (!leftover.isEmpty()) {
            player.drop(leftover, false);
        }
    }

    public static void deliverGachaReward(ServerPlayer player, SystemData data, ItemStack stack) {
        deliverGachaReward(player, data, stack, new boolean[]{false}, new int[]{0});
    }

    public static void handleWish(ServerPlayer player, GachaWishPayload payload) {
        long dayTime = player.serverLevel().getDayTime();
        SystemData data = SystemEvents.data(player);
        if (!data.teyvatUnlocked()) {
            player.sendSystemMessage(Component.translatable("awakening_system.gacha.unlock_required"));
            return;
        }
        VersionBannerManager.checkAndAwardMaintenanceCompensation(player, data);

        long playerEffTime = Math.max(0L, dayTime - data.gachaEpochTick());
        if (VersionBannerManager.isUnderMaintenanceByEffectiveTicks(playerEffTime)) {
            player.displayClientMessage(Component.translatable("awakening_system.gacha.maintenance_technical", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }

        int banner = payload.bannerId();
        int count = Math.min(10, Math.max(1, payload.pullCount()));

        boolean isPromo2 = (banner == BANNER_PROMO_2);
        int promoIdx = isPromo2 ? 1 : 0;
        PromotionalBanner.BannerPhase rotatingPromoPhase = PromotionalBanner.getActivePhaseByEffectiveTicks(playerEffTime, promoIdx);
        PromotionalBanner.Franchise promoFranchise = rotatingPromoPhase.franchise();

        String bannerName;
        if (banner == 0) bannerName = "5★ Weapons";
        else if (banner == 2) bannerName = "Standard";
        else if (banner == 3) bannerName = "Beginners";
        else if (banner == BANNER_UMA) bannerName = "Tracen Academy";
        else if (banner == BANNER_STELLAR_WARP) bannerName = "Stellar Warp (HSR)";
        else {
            bannerName = rotatingPromoPhase.bannerTitle();
        }

        if (banner == BANNER_STELLAR_WARP) {
            int passesNeeded = count;
            int passesAvailable = 0;
            for (ItemStack invStack : player.getInventory().items) {
                if (!invStack.isEmpty() && invStack.is(SystemItems.STAR_RAIL_SPECIAL_PASS.get())) {
                    passesAvailable += invStack.getCount();
                }
            }
            int intertwinedAvailable = data.intertwinedFates();
            int totalFreeTickets = passesAvailable + intertwinedAvailable;

            int ticketsToUse = Math.min(passesNeeded, totalFreeTickets);
            int remainingPulls = passesNeeded - ticketsToUse;
            int totalCostCurrency = remainingPulls * 160;

            int availableCurrency = data.stellarJade() + data.primogems();
            if (remainingPulls > 0 && availableCurrency < totalCostCurrency) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.stellar_warp.insufficient_currency", totalCostCurrency), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }

            int passesToDeduct = Math.min(passesAvailable, ticketsToUse);
            if (passesToDeduct > 0) {
                int toDeduct = passesToDeduct;
                for (ItemStack invStack : player.getInventory().items) {
                    if (!invStack.isEmpty() && invStack.is(SystemItems.STAR_RAIL_SPECIAL_PASS.get())) {
                        int take = Math.min(toDeduct, invStack.getCount());
                        invStack.shrink(take);
                        toDeduct -= take;
                        if (toDeduct <= 0) break;
                    }
                }
                player.inventoryMenu.broadcastChanges();
            }

            int fatesToDeduct = ticketsToUse - passesToDeduct;
            if (fatesToDeduct > 0) {
                data.spendIntertwinedFates(fatesToDeduct);
            }

            if (totalCostCurrency > 0) {
                int jadeToSpend = Math.min(totalCostCurrency, data.stellarJade());
                if (jadeToSpend > 0) {
                    data.spendStellarJade(jadeToSpend);
                }
                int primosToSpend = totalCostCurrency - jadeToSpend;
                if (primosToSpend > 0) {
                    data.spendPrimogems(primosToSpend);
                }
            }

            List<ItemStack> wonItems = new ArrayList<>();
            List<Integer> itemRarities = new ArrayList<>();
            int highestRarity = 3;

            List<CompanionProfile> hsr5Stars = List.of(
                    CompanionProfile.ACHERON, CompanionProfile.KAFKA, CompanionProfile.FIREFLY,
                    CompanionProfile.DAN_HENG_IL, CompanionProfile.FEIXIAO, CompanionProfile.AVENTURINE,
                    CompanionProfile.ROBIN, CompanionProfile.JING_YUAN, CompanionProfile.JINGLIU,
                    CompanionProfile.BAILU, CompanionProfile.CLARA, CompanionProfile.HUOHUO,
                    CompanionProfile.SPARKLE, CompanionProfile.BLACK_SWAN, CompanionProfile.TOPAZ,
                    CompanionProfile.RUAN_MEI, CompanionProfile.SUNDAY
            );

            List<java.util.function.Supplier<ItemStack>> hsr5LightCones = List.of(
                    () -> new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get())
            );

            List<CompanionProfile> hsr4Stars = List.of(
                    CompanionProfile.MARCH_7TH, CompanionProfile.DAN_HENG, CompanionProfile.HERTA,
                    CompanionProfile.SERVAL, CompanionProfile.NATASHA, CompanionProfile.ASTA,
                    CompanionProfile.TINGYUN, CompanionProfile.QINGQUE, CompanionProfile.GALLAGHER,
                    CompanionProfile.XUEYI, CompanionProfile.LYNX
            );

            List<java.util.function.Supplier<ItemStack>> hsr4LightCones = List.of(
                    () -> new ItemStack(SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_GOOD_NIGHT_SLEEP_WELL.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_THE_DAY_COSMOS_FELL.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_EYES_OF_THE_PREY.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_POST_OP_CONVERSATION.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_BIRTH_OF_THE_SELF.get())
            );

            List<java.util.function.Supplier<ItemStack>> hsr3LightCones = List.of(
                    () -> new ItemStack(SystemItems.LIGHT_CONE_ARROWS.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_MULTIPLICATION.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_DEFENSE.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_PASSKEY.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_CHORUS.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_LOOP.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_COLLAPSING_SKY.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_CORNUCOPIA.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_VOID.get()),
                    () -> new ItemStack(SystemItems.LIGHT_CONE_AMBER.get())
            );

            for (int i = 0; i < count; i++) {
                data.incrementTotalGachaPulls(1);
                int pity5 = data.gachaPity5Star();
                int pity4 = data.gachaPity4Star();

                double rate5 = 0.006D;
                if (pity5 >= 73) rate5 += (pity5 - 73) * 0.075D;
                if (pity5 >= 79) rate5 = 1.0D;

                double rate4 = 0.051D;
                if (pity4 >= 8) rate4 += (pity4 - 8) * 0.35D;
                if (pity4 >= 9) rate4 = 1.0D;

                double roll = RNG.nextDouble();
                if (roll < rate5) {
                    data.setGachaPity5Star(0);
                    data.incrementGachaPity4Star();
                    highestRarity = 5;

                    ItemStack won;
                    String dropName;
                    if (RNG.nextDouble() < 0.70D || hsr5LightCones.isEmpty()) {
                        CompanionProfile wonProf = hsr5Stars.get(RNG.nextInt(hsr5Stars.size()));
                        won = CompanionContractItem.createContract(wonProf);
                        dropName = wonProf.displayName() + " (5★)";
                        Component ann = Component.literal("§b§l✦✦✦✦✦ [STELLAR WARP] §fYou summoned §d§l" + wonProf.displayName() + " §ffrom the Astral Express!");
                        player.displayClientMessage(ann, true);
                    } else {
                        won = hsr5LightCones.get(RNG.nextInt(hsr5LightCones.size())).get().copy();
                        dropName = won.getHoverName().getString() + " (5★)";
                        Component ann = Component.literal("§6§l✦✦✦✦✦ [STELLAR WARP] §fYou obtained the Legendary Light Cone §e§l" + won.getHoverName().getString() + "§f!");
                        player.displayClientMessage(ann, true);
                    }

                    wonItems.add(won);
                    itemRarities.add(5);
                    data.addStarglitter(10);
                    data.addWishHistoryEntry(dropName, 5, "Salto Estelar (HSR)");
                    deliverGachaReward(player, data, won);

                    player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                } else if (roll < (rate5 + rate4)) {
                    data.incrementGachaPity5Star();
                    data.setGachaPity4Star(0);

                    ItemStack won;
                    String dropName;
                    if (RNG.nextBoolean() && !hsr4Stars.isEmpty()) {
                        CompanionProfile won4 = hsr4Stars.get(RNG.nextInt(hsr4Stars.size()));
                        won = CompanionContractItem.createContract(won4);
                        dropName = won4.displayName() + " (4★)";
                    } else {
                        won = hsr4LightCones.get(RNG.nextInt(hsr4LightCones.size())).get().copy();
                        dropName = won.getHoverName().getString() + " (4★)";
                    }

                    wonItems.add(won);
                    itemRarities.add(4);
                    data.addStarglitter(2);
                    data.addWishHistoryEntry(dropName, 4, "Salto Estelar (HSR)");
                    deliverGachaReward(player, data, won);
                } else {
                    data.incrementGachaPity5Star();
                    data.incrementGachaPity4Star();
                    ItemStack cone3 = hsr3LightCones.get(RNG.nextInt(hsr3LightCones.size())).get().copy();
                    wonItems.add(cone3);
                    itemRarities.add(3);
                    data.addStardust(15);
                    data.addWishHistoryEntry(cone3.getHoverName().getString() + " (3★)", 3, "Salto Estelar (HSR)");
                    deliverGachaReward(player, data, cone3);
                }
            }

            data.incrementTotalGachaPulls(count);
            AwakeningSystemMod.sync(player);

            PacketDistributor.sendToPlayer(player, new GachaWishResultPayload(
                    highestRarity,
                    wonItems,
                    itemRarities,
                    data.gachaPity5Star(),
                    data.gachaGuaranteedRateUp(),
                    data.primogems(),
                    data.intertwinedFates(),
                    data.acquaintFates(),
                    data.stardust(),
                    data.starglitter(),
                    data.beginnerPulls(),
                    data.mora(),
                    data.weaponBannerTarget(),
                    data.weaponBannerFatePoints(),
                    data.characterBannerTarget(),
                    data.totalGachaPulls()
            ));
            return;
        }

        if (banner == BANNER_UMA) {
            int pullsNeeded = count;
            int ticketsAvailable = 0;
            for (ItemStack invStack : player.getInventory().items) {
                if (!invStack.isEmpty() && (invStack.is(SystemItems.GACHA_TICKET.get()) || invStack.is(SystemItems.GOLDEN_GACHA_TICKET.get()))) {
                    ticketsAvailable += invStack.getCount();
                }
            }
            int intertwinedAvailable = data.intertwinedFates();
            int totalFreeTickets = ticketsAvailable + intertwinedAvailable;

            int ticketsToUse = Math.min(pullsNeeded, totalFreeTickets);
            int remainingPulls = pullsNeeded - ticketsToUse;

            int caratsAvailable = data.getCarats();
            int caratsPulls = Math.min(remainingPulls, caratsAvailable / 150);
            int pullsStillNeeded = remainingPulls - caratsPulls;
            int primosNeeded = pullsStillNeeded * 160;

            if (pullsStillNeeded > 0 && data.primogems() < primosNeeded) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.tracen.insufficient_currency"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }

            int ticketsToDeduct = Math.min(ticketsAvailable, ticketsToUse);
            if (ticketsToDeduct > 0) {
                int toDeduct = ticketsToDeduct;
                for (ItemStack invStack : player.getInventory().items) {
                    if (!invStack.isEmpty() && (invStack.is(SystemItems.GACHA_TICKET.get()) || invStack.is(SystemItems.GOLDEN_GACHA_TICKET.get()))) {
                        int take = Math.min(toDeduct, invStack.getCount());
                        invStack.shrink(take);
                        toDeduct -= take;
                        if (toDeduct <= 0) break;
                    }
                }
                player.inventoryMenu.broadcastChanges();
            }

            int fatesToDeduct = ticketsToUse - ticketsToDeduct;
            if (fatesToDeduct > 0) {
                data.spendIntertwinedFates(fatesToDeduct);
            }

            if (caratsPulls > 0) {
                data.spendCarats(caratsPulls * 150);
            }

            if (primosNeeded > 0) {
                data.spendPrimogems(primosNeeded);
            }

            com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.BannerPhase umaPhase = com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.getActivePhase(player.serverLevel().getDayTime());
            List<com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.PullResult> results = com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.performPullWithoutCarats(player, data, umaPhase, count);

            List<ItemStack> wonItems = new ArrayList<>();
            List<Integer> itemRarities = new ArrayList<>();
            int highestRarity = 3;

            for (var r : results) {
                com.soybe.awakeningsystem.uma.UmaProfile profile = com.soybe.awakeningsystem.uma.UmaProfile.byId(r.umaId());
                int rarity = r.rarity();
                int visualStars = rarity == 3 ? 5 : (rarity == 2 ? 4 : 3);
                highestRarity = Math.max(highestRarity, visualStars);

                ItemStack item = createUmaContract(profile, rarity, r.isNew(), r.shardsGained());
                wonItems.add(item);
                itemRarities.add(visualStars);
                data.addWishHistoryEntry(profile.getDisplayName() + " (" + rarity + "★)", visualStars, "Academia Tracen");
            }

            data.incrementTotalGachaPulls(count);
            AwakeningSystemMod.sync(player);

            PacketDistributor.sendToPlayer(player, new GachaWishResultPayload(
                    highestRarity,
                    wonItems,
                    itemRarities,
                    data.gachaPity5Star(),
                    data.gachaGuaranteedRateUp(),
                    data.primogems(),
                    data.intertwinedFates(),
                    data.acquaintFates(),
                    data.stardust(),
                    data.starglitter(),
                    data.beginnerPulls(),
                    data.mora(),
                    data.weaponBannerTarget(),
                    data.weaponBannerFatePoints(),
                    data.characterBannerTarget(),
                    data.totalGachaPulls()
            ));
            return;
        }

        boolean isPromotional = (banner == 0 || banner == 1 || banner == BANNER_PROMO_2 || (banner >= 4 && banner <= 10));
        if (banner == 3) {
            if (data.beginnerPulls() >= 20) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.beginner_limit_reached"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
            count = 10;
            int fateCost = 8;
            int primoCost = 1280;

            if (data.acquaintFates() >= fateCost) {
                data.spendAcquaintFates(fateCost);
            } else if (!data.spendPrimogems(primoCost)) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.beginner_insufficient_primos", primoCost), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
        } else if (isPromotional) {
            if (data.intertwinedFates() >= count) {
                data.spendIntertwinedFates(count);
            } else {
                int costPrimos = count * 160;
                if (!data.spendPrimogems(costPrimos)) {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.promo_insufficient_primos", costPrimos), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
        } else {
            if (data.acquaintFates() >= count) {
                data.spendAcquaintFates(count);
            } else {
                int costPrimos = count * 160;
                if (!data.spendPrimogems(costPrimos)) {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.standard_insufficient_primos", costPrimos), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
        }

        List<ItemStack> wonItems = new ArrayList<>();
        List<Integer> itemRarities = new ArrayList<>();
        int highestRarity = 3;

        boolean guaranteedNoelleThisMulti = (banner == 3 && data.beginnerPulls() == 0);

        for (int i = 0; i < count; i++) {
            data.incrementTotalGachaPulls(1);
            int pity5 = data.gachaPity5Star();
            int pity4 = data.gachaPity4Star();

            if (guaranteedNoelleThisMulti && i == 0) {
                highestRarity = Math.max(highestRarity, 4);
                data.incrementGachaPity5Star();
                data.setGachaPity4Star(0);
                ItemStack noelle = CompanionContractItem.createContract(CompanionProfile.NOELLE);
                wonItems.add(noelle);
                itemRarities.add(4);
                data.addStarglitter(2);
                data.addWishHistoryEntry(noelle.getHoverName().getString(), 4, bannerName);
                continue;
            }

            if (RNG.nextDouble() < 0.00008D) {
                highestRarity = 5;
                ItemStack creatorSword = new ItemStack(SystemItems.CREATOR_CLAYMORE.get());
                wonItems.add(creatorSword);
                itemRarities.add(5);
                data.addStarglitter(100);
                data.addMora(250000);
                data.addWishHistoryEntry(creatorSword.getHoverName().getString(), 5, bannerName);

                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 1.4F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

                PacketDistributor.sendToAllPlayers(new com.soybe.awakeningsystem.network.SystemAnnouncementPayload(
                        "✦ SUPREME MYTHIC DISCOVERY (0.008%)! ✦",
                        player.getName().getString() + " obtained: " + creatorSword.getHoverName().getString(),
                        0xFFFFD700
                ));
                continue;
            }

            if (RNG.nextDouble() < 0.00020D) {
                highestRarity = 5;
                ItemStack doncellaSword = new ItemStack(SystemItems.LA_DONCELLA.get());
                wonItems.add(doncellaSword);
                itemRarities.add(5);
                data.addStarglitter(30);
                data.addMora(50000);
                data.addWishHistoryEntry(doncellaSword.getHoverName().getString(), 5, bannerName);

                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.3F, 1.1F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

                PacketDistributor.sendToAllPlayers(new com.soybe.awakeningsystem.network.SystemAnnouncementPayload(
                        "✦ AWAKENED 5★ RELIC (0.02%)! ✦",
                        player.getName().getString() + " obtained: " + doncellaSword.getHoverName().getString(),
                        0xFFFF55FF
                ));
                continue;
            }

            double rate5 = 0.006D;
            if (pity5 >= 73) {
                rate5 += (pity5 - 73) * 0.075D;
            }
            if (pity5 >= 79) {
                rate5 = 1.0D;
            }

            double rate4 = 0.051D;
            if (pity4 >= 9) {
                rate4 = 1.0D;
            }

            double roll = RNG.nextDouble();
            if (roll < rate5) {
                highestRarity = 5;
                data.setGachaPity5Star(0);
                data.incrementGachaPity4Star();

                ItemStack item5;
                if (banner == 0) {
                    PromotionalBanner.BannerPhase phase = PromotionalBanner.getActiveWeaponPhaseByEffectiveTicks(playerEffTime);
                    boolean winFeatured = data.gachaGuaranteedRateUp() || (RNG.nextDouble() < 0.75D);
                    ItemStack wp1 = phase.primaryFeaturedWeapon().get();
                    ItemStack wp2 = phase.secondaryFeaturedWeapon() != null ? phase.secondaryFeaturedWeapon().get() : wp1;
                    int target = data.weaponBannerTarget();

                    if (winFeatured) {
                        data.setGachaGuaranteedRateUp(false);
                        if (target == 1) {
                            if (data.weaponBannerFatePoints() >= 1) {
                                item5 = wp1.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_fulfilled", item5.getHoverName()), true);
                            } else if (RNG.nextBoolean()) {
                                item5 = wp1.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_won", item5.getHoverName()), true);
                            } else {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(1);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_fate_point", item5.getHoverName()), true);
                            }
                        } else if (target == 2) {
                            if (data.weaponBannerFatePoints() >= 1) {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_fulfilled", item5.getHoverName()), true);
                            } else if (RNG.nextBoolean()) {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_won", item5.getHoverName()), true);
                            } else {
                                item5 = wp1.copy();
                                data.setWeaponBannerFatePoints(1);
                                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_fate_point", item5.getHoverName()), true);
                            }
                        } else {
                            ItemStack featuredWp = (phase.secondaryFeaturedWeapon() != null && RNG.nextBoolean()) ? wp2 : wp1;
                            item5 = featuredWp.copy();
                            player.displayClientMessage(Component.translatable("awakening_system.gacha.won_featured_weapon", item5.getHoverName()), true);
                        }
                    } else {
                        data.setGachaGuaranteedRateUp(true);
                        if (target > 0) {
                            data.setWeaponBannerFatePoints(1);
                        }
                        item5 = rollStandardWeapon5Star();
                        player.displayClientMessage(Component.translatable("awakening_system.gacha.lost_75_25", item5.getHoverName()), true);
                    }
                } else if (banner == 1 || banner == BANNER_PROMO_2 || (banner >= 4 && banner <= 10)) {
                    PromotionalBanner.BannerPhase phase = rotatingPromoPhase;
                    boolean winFeatured = data.gachaGuaranteedRateUp() || RNG.nextBoolean();
                    if (winFeatured) {
                        data.setGachaGuaranteedRateUp(false);
                        CompanionProfile targetProfile = phase.featuredCharacter();
                        item5 = CompanionContractItem.createContract(targetProfile);
                        player.displayClientMessage(Component.translatable("awakening_system.gacha.won_featured_character", targetProfile.displayName()), true);
                    } else {
                        data.setGachaGuaranteedRateUp(true);
                        CompanionProfile[] perm5Pool = {
                                CompanionProfile.DILUC, CompanionProfile.JEAN, CompanionProfile.QIQI,
                                CompanionProfile.BAILU, CompanionProfile.CLARA,
                                CompanionProfile.VERINA, CompanionProfile.JIANXIN, CompanionProfile.CALCHARO,
                                CompanionProfile.KOLEDA_BELOBOG, CompanionProfile.GRACE_HOWARD
                        };
                        CompanionProfile perm5 = perm5Pool[RNG.nextInt(perm5Pool.length)];
                        item5 = CompanionContractItem.createContract(perm5);
                        player.displayClientMessage(Component.translatable("awakening_system.gacha.lost_50_50", perm5.displayName()), true);
                    }
                } else {
                    item5 = rollStandard5Star();
                }

                wonItems.add(item5);
                itemRarities.add(5);
                data.addStarglitter(10);
                data.addMora(25000);
                data.addWishHistoryEntry(item5.getHoverName().getString(), 5, bannerName);

            } else if (roll < rate5 + rate4) {
                highestRarity = Math.max(highestRarity, 4);
                data.incrementGachaPity5Star();
                data.setGachaPity4Star(0);

                ItemStack item4 = roll4StarReward(banner, playerEffTime);
                wonItems.add(item4);
                itemRarities.add(4);
                data.addStarglitter(2);
                data.addMora(5000);
                data.addWishHistoryEntry(item4.getHoverName().getString(), 4, bannerName);
            } else {
                data.incrementGachaPity5Star();
                data.incrementGachaPity4Star();

                ItemStack item3 = roll3StarReward();
                wonItems.add(item3);
                itemRarities.add(3);
                data.addStardust(15);
                data.addMora(1000);
                data.addWishHistoryEntry(item3.getHoverName().getString(), 3, bannerName);
            }
        }

        if (banner == 3) {
            data.incrementBeginnerPulls(10);
        }

        boolean[] anySentToSubspace = new boolean[]{false};
        int[] subspaceCount = new int[]{0};
        for (ItemStack stack : wonItems) {
            deliverGachaReward(player, data, stack, anySentToSubspace, subspaceCount);
        }

        if (anySentToSubspace[0]) {
            player.displayClientMessage(Component.translatable("awakening_system.gacha.subspace_stored", subspaceCount[0]), false);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
        }

        AwakeningSystemMod.sync(player);

        PacketDistributor.sendToPlayer(player, new GachaWishResultPayload(
                highestRarity,
                wonItems,
                itemRarities,
                data.gachaPity5Star(),
                data.gachaGuaranteedRateUp(),
                data.primogems(),
                data.intertwinedFates(),
                data.acquaintFates(),
                data.stardust(),
                data.starglitter(),
                data.beginnerPulls(),
                data.mora(),
                data.weaponBannerTarget(),
                data.weaponBannerFatePoints(),
                data.characterBannerTarget(),
                data.totalGachaPulls()
        ));
    }

    public static void handlePaimonBargain(ServerPlayer player, PaimonBargainBuyPayload payload) {
        SystemData data = SystemEvents.data(player);
        int offer = payload.offerId();
        int count = Math.max(1, Math.min(20, payload.count()));

        switch (offer) {
            case 0 -> {
                int cost = 75 * count;
                if (data.spendStardust(cost)) {
                    data.addIntertwinedFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.intertwined_stardust", count, cost), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_stardust", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 1 -> {
                int cost = 75 * count;
                if (data.spendStardust(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.acquaint_stardust", count, cost), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_stardust", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 2 -> {
                int cost = 5 * count;
                if (data.spendStarglitter(cost)) {
                    data.addIntertwinedFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.intertwined_starglitter", count, cost), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_starglitter", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 3 -> {
                int cost = 5 * count;
                if (data.spendStarglitter(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.acquaint_starglitter", count, cost), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_starglitter", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 4 -> {
                int cost = 34;
                if (data.spendStarglitter(cost)) {
                    ItemStack contract = CompanionContractItem.createContract(CompanionProfile.NOELLE);
                    deliverGachaReward(player, data, contract);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.pact_noelle"), false);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_starglitter", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 5 -> {
                int cost = 34;
                if (data.spendStarglitter(cost)) {
                    ItemStack contract = CompanionContractItem.createContract(CompanionProfile.CHA_HAEIN);
                    deliverGachaReward(player, data, contract);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.pact_cha"), false);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_starglitter", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 6 -> {
                int cost = 15 * count;
                if (data.spendStardust(cost)) {
                    data.addMora(15000 * count);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.mora_stardust", String.format(java.util.Locale.US, "%,d", 15000 * count)), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_stardust", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 7 -> {
                int cost = 10 * count;
                if (data.spendStardust(cost)) {
                    ItemStack exp = new ItemStack(Items.EXPERIENCE_BOTTLE, 8 * count);
                    deliverGachaReward(player, data, exp);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.exp_stardust", (8 * count)), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_stardust", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 8 -> {
                int cost = 10 * count;
                if (data.spendStarglitter(cost)) {
                    data.addMora(50000 * count);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.mora_starglitter", String.format(java.util.Locale.US, "%,d", 50000 * count)), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_starglitter", cost), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            default -> {
                int cost = 30000 * count;
                if (data.spendMora(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.acquaint_mora", count, String.format(java.util.Locale.US, "%,d", cost)), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.bargain.need_mora", String.format(java.util.Locale.US, "%,d", cost)), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
        }

        AwakeningSystemMod.sync(player);
        openGachaScreen(player);
    }

    public static void handleSetPath(ServerPlayer player, SetGachaPathPayload payload) {
        SystemData data = SystemEvents.data(player);
        if (payload.bannerType() == 0) {
            if (data.weaponBannerTarget() != payload.targetIndex()) {
                data.setWeaponBannerTarget(payload.targetIndex());
                data.setWeaponBannerFatePoints(0);
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.epitomized_target_set"), true);
            }
        } else if (payload.bannerType() == 1) {
            data.setCharacterBannerTarget(payload.targetIndex());
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        AwakeningSystemMod.sync(player);
        openGachaScreen(player);
    }

    public static ItemStack rollStandardWeapon5Star() {
        int pick = RNG.nextInt(8);
        return switch (pick) {
            case 0 -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 1 -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 2 -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 3 -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 4 -> SystemItems.DAWNS_BALLAD.isBound() ? new ItemStack(SystemItems.DAWNS_BALLAD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 5 -> SystemItems.ELUCIDATOR.isBound() ? new ItemStack(SystemItems.ELUCIDATOR.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 6 -> SystemItems.DARK_REPULSER.isBound() ? new ItemStack(SystemItems.DARK_REPULSER.get()) : new ItemStack(Items.DIAMOND_SWORD);
            default -> SystemItems.LAMBENT_LIGHT.isBound() ? new ItemStack(SystemItems.LAMBENT_LIGHT.get()) : new ItemStack(Items.DIAMOND_SWORD);
        };
    }

    public static ItemStack rollFeaturedWeapon5Star() {
        int pick = RNG.nextInt(22);
        return switch (pick) {
            case 0 -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 1 -> SystemItems.ENGULFING_LIGHTNING.isBound() ? new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 2 -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 3 -> SystemItems.CRIMSON_MOONS_SEMBLANCE.isBound() ? new ItemStack(SystemItems.CRIMSON_MOONS_SEMBLANCE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 4 -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 5 -> SystemItems.REDHORN_STONETHRESHER.isBound() ? new ItemStack(SystemItems.REDHORN_STONETHRESHER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 6 -> SystemItems.VERDICT.isBound() ? new ItemStack(SystemItems.VERDICT.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 7 -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 8 -> SystemItems.PRIMORDIAL_JADE_CUTTER.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_CUTTER.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 9 -> SystemItems.FREEDOM_SWORN.isBound() ? new ItemStack(SystemItems.FREEDOM_SWORN.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 10 -> SystemItems.KEY_OF_KHAJ_NISUT.isBound() ? new ItemStack(SystemItems.KEY_OF_KHAJ_NISUT.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 11 -> SystemItems.ABSOLUTION.isBound() ? new ItemStack(SystemItems.ABSOLUTION.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 12 -> SystemItems.VORTEX_VANQUISHER.isBound() ? new ItemStack(SystemItems.VORTEX_VANQUISHER.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 13 -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 14 -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 15 -> SystemItems.QINGMING_SWORD.isBound() ? new ItemStack(SystemItems.QINGMING_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 16 -> SystemItems.TAILLESS.isBound() ? new ItemStack(SystemItems.TAILLESS.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 17 -> SystemItems.SHUNGUANG_SWORD.isBound() ? new ItemStack(SystemItems.SHUNGUANG_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 18 -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 19 -> SystemItems.LIGHT_CONE_PATIENCE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 20 -> SystemItems.LIGHT_CONE_DREAMS_REST.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 21 -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD);
            default -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD);
        };
    }

    public static ItemStack rollFeaturedShonen5Star() {
        int pick = RNG.nextInt(71);
        return switch (pick) {
            case 0 -> CompanionContractItem.createContract(CompanionProfile.RAIDEN_SHOGUN);
            case 1 -> CompanionContractItem.createContract(CompanionProfile.HU_TAO);
            case 2 -> CompanionContractItem.createContract(CompanionProfile.ZHONGLI);
            case 3 -> CompanionContractItem.createContract(CompanionProfile.FURINA);
            case 4 -> CompanionContractItem.createContract(CompanionProfile.NAHIDA);
            case 5 -> CompanionContractItem.createContract(CompanionProfile.KAZUHA);
            case 6 -> CompanionContractItem.createContract(CompanionProfile.GOJO_SATORU);
            case 7 -> CompanionContractItem.createContract(CompanionProfile.SUNG_JINWOO);
            case 8 -> CompanionContractItem.createContract(CompanionProfile.CHANGLI);
            case 9 -> CompanionContractItem.createContract(CompanionProfile.FIREFLY);
            case 10 -> CompanionContractItem.createContract(CompanionProfile.ACHERON);
            case 11 -> CompanionContractItem.createContract(CompanionProfile.YINLIN);
            case 12 -> CompanionContractItem.createContract(CompanionProfile.ROBIN);
            case 13 -> CompanionContractItem.createContract(CompanionProfile.FEIXIAO);
            case 14 -> CompanionContractItem.createContract(CompanionProfile.ARLECCHINO);
            case 15 -> CompanionContractItem.createContract(CompanionProfile.GANYU);
            case 16 -> CompanionContractItem.createContract(CompanionProfile.NEUVILLETTE);
            case 17 -> CompanionContractItem.createContract(CompanionProfile.DILUC);
            case 18 -> CompanionContractItem.createContract(CompanionProfile.JEAN);
            case 19 -> CompanionContractItem.createContract(CompanionProfile.XIAO);
            case 20 -> CompanionContractItem.createContract(CompanionProfile.JINGLIU);
            case 21 -> CompanionContractItem.createContract(CompanionProfile.DAN_HENG_IL);
            case 22 -> CompanionContractItem.createContract(CompanionProfile.AVENTURINE);
            case 23 -> CompanionContractItem.createContract(CompanionProfile.JING_YUAN);
            case 24 -> CompanionContractItem.createContract(CompanionProfile.ELLEN_JOE);
            case 25 -> CompanionContractItem.createContract(CompanionProfile.HOSHIMI_MIYABI);
            case 26 -> CompanionContractItem.createContract(CompanionProfile.JANE_DOE);
            case 27 -> CompanionContractItem.createContract(CompanionProfile.CAESAR_KING);
            case 28 -> CompanionContractItem.createContract(CompanionProfile.BURNICE_WHITE);
            case 29 -> CompanionContractItem.createContract(CompanionProfile.ZHU_YUAN);
            case 30 -> CompanionContractItem.createContract(CompanionProfile.JIYAN);
            case 31 -> CompanionContractItem.createContract(CompanionProfile.JINSHI);
            case 32 -> CompanionContractItem.createContract(CompanionProfile.SHOREKEEPER);
            case 33 -> CompanionContractItem.createContract(CompanionProfile.CAMELLYA);
            case 34 -> CompanionContractItem.createContract(CompanionProfile.ROVER);
            case 35 -> CompanionContractItem.createContract(CompanionProfile.KLEE);
            case 36 -> CompanionContractItem.createContract(CompanionProfile.SIGEWINNE);
            case 37 -> CompanionContractItem.createContract(CompanionProfile.QIQI);
            case 38 -> CompanionContractItem.createContract(CompanionProfile.YELAN);
            case 39 -> CompanionContractItem.createContract(CompanionProfile.NAVIA);
            case 40 -> CompanionContractItem.createContract(CompanionProfile.CLORINDE);
            case 41 -> CompanionContractItem.createContract(CompanionProfile.KINICH);
            case 42 -> CompanionContractItem.createContract(CompanionProfile.XILONEN);
            case 43 -> CompanionContractItem.createContract(CompanionProfile.MAVUIKA);
            case 44 -> CompanionContractItem.createContract(CompanionProfile.SKIRK);
            case 45 -> CompanionContractItem.createContract(CompanionProfile.BAILU);
            case 46 -> CompanionContractItem.createContract(CompanionProfile.CLARA);
            case 47 -> CompanionContractItem.createContract(CompanionProfile.HUOHUO);
            case 48 -> CompanionContractItem.createContract(CompanionProfile.SPARKLE);
            case 49 -> CompanionContractItem.createContract(CompanionProfile.BLACK_SWAN);
            case 50 -> CompanionContractItem.createContract(CompanionProfile.TOPAZ);
            case 51 -> CompanionContractItem.createContract(CompanionProfile.RUAN_MEI);
            case 52 -> CompanionContractItem.createContract(CompanionProfile.SUNDAY);
            case 53 -> CompanionContractItem.createContract(CompanionProfile.ENCORE);
            case 54 -> CompanionContractItem.createContract(CompanionProfile.VERINA);
            case 55 -> CompanionContractItem.createContract(CompanionProfile.JIANXIN);
            case 56 -> CompanionContractItem.createContract(CompanionProfile.CALCHARO);
            case 57 -> CompanionContractItem.createContract(CompanionProfile.KOLEDA_BELOBOG);
            case 58 -> CompanionContractItem.createContract(CompanionProfile.GRACE_HOWARD);
            case 59 -> SystemItems.LIGHT_CONE_MILKY_WAY.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 60 -> SystemItems.COMMAND_SPELL.isBound() ? new ItemStack(SystemItems.COMMAND_SPELL.get()) : new ItemStack(Items.NETHER_STAR);
            case 61 -> rollVisionItem();
            case 62 -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 63 -> SystemItems.QINGMING_SWORD.isBound() ? new ItemStack(SystemItems.QINGMING_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 64 -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 65 -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 66 -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 67 -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 68 -> SystemItems.LIGHT_CONE_PATIENCE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 69 -> SystemItems.LIGHT_CONE_DREAMS_REST.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()) : new ItemStack(Items.NETHERITE_SWORD);
            default -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD);
        };
    }

    public static ItemStack rollStandard5Star() {
        int pick = RNG.nextInt(20);
        return switch (pick) {
            case 0 -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 1 -> SystemItems.DAWNS_BALLAD.isBound() ? new ItemStack(SystemItems.DAWNS_BALLAD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 2 -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 3 -> SystemItems.TRANSCENDENT_THORN.isBound() ? new ItemStack(SystemItems.TRANSCENDENT_THORN.get()) : new ItemStack(Items.BOW);
            case 4 -> rollFeaturedWeapon5Star();
            case 5 -> rollVisionItem();
            case 6 -> SystemItems.HEAVEN_SHAKING_SWORD.isBound() ? new ItemStack(SystemItems.HEAVEN_SHAKING_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 7 -> SystemItems.UNBREAKABLE_FAITH.isBound() ? new ItemStack(SystemItems.UNBREAKABLE_FAITH.get()) : new ItemStack(Items.SHIELD);
            case 8 -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 9 -> CompanionContractItem.createContract(CompanionProfile.DILUC);
            case 10 -> CompanionContractItem.createContract(CompanionProfile.JEAN);
            case 11 -> CompanionContractItem.createContract(CompanionProfile.QIQI);
            case 12 -> CompanionContractItem.createContract(CompanionProfile.BAILU);
            case 13 -> CompanionContractItem.createContract(CompanionProfile.CLARA);
            case 14 -> CompanionContractItem.createContract(CompanionProfile.VERINA);
            case 15 -> CompanionContractItem.createContract(CompanionProfile.JIANXIN);
            case 16 -> CompanionContractItem.createContract(CompanionProfile.CALCHARO);
            case 17 -> CompanionContractItem.createContract(CompanionProfile.KOLEDA_BELOBOG);
            case 18 -> CompanionContractItem.createContract(CompanionProfile.GRACE_HOWARD);
            default -> SystemItems.TAILLESS.isBound() ? new ItemStack(SystemItems.TAILLESS.get()) : new ItemStack(Items.NETHERITE_SWORD);
        };
    }

    public static ItemStack rollVisionItem() {
        int elem = RNG.nextInt(7);
        return switch (elem) {
            case 0 -> SystemItems.VISION_PYRO.isBound() ? new ItemStack(SystemItems.VISION_PYRO.get()) : new ItemStack(Items.FIRE_CHARGE);
            case 1 -> SystemItems.VISION_ELECTRO.isBound() ? new ItemStack(SystemItems.VISION_ELECTRO.get()) : new ItemStack(Items.LIGHTNING_ROD);
            case 2 -> SystemItems.VISION_CRYO.isBound() ? new ItemStack(SystemItems.VISION_CRYO.get()) : new ItemStack(Items.ICE);
            case 3 -> SystemItems.VISION_ANEMO.isBound() ? new ItemStack(SystemItems.VISION_ANEMO.get()) : new ItemStack(Items.FEATHER);
            case 4 -> SystemItems.VISION_HYDRO.isBound() ? new ItemStack(SystemItems.VISION_HYDRO.get()) : new ItemStack(Items.HEART_OF_THE_SEA);
            case 5 -> SystemItems.VISION_GEO.isBound() ? new ItemStack(SystemItems.VISION_GEO.get()) : new ItemStack(Items.GOLD_INGOT);
            default -> SystemItems.VISION_DENDRO.isBound() ? new ItemStack(SystemItems.VISION_DENDRO.get()) : new ItemStack(Items.OAK_SAPLING);
        };
    }

    public static ItemStack roll4StarReward(int banner, long dayTime) {
        if (banner == 0 || banner == 1 || banner == BANNER_PROMO_2 || (banner >= 4 && banner <= 10)) {
            PromotionalBanner.BannerPhase phase = PromotionalBanner.getActivePhase(dayTime);
            if (RNG.nextBoolean()) {
                if (RNG.nextBoolean() && !phase.rateUp4StarCharacters().isEmpty()) {
                    CompanionProfile char4 = phase.rateUp4StarCharacters().get(RNG.nextInt(phase.rateUp4StarCharacters().size()));
                    return CompanionContractItem.createContract(char4);
                } else if (!phase.rateUp4StarWeapons().isEmpty()) {
                    return phase.rateUp4StarWeapons().get(RNG.nextInt(phase.rateUp4StarWeapons().size())).get().copy();
                }
            }
        }

        if (RNG.nextBoolean()) {
            CompanionProfile[] chars4 = new CompanionProfile[]{
                    CompanionProfile.MARCH_7TH,
                    CompanionProfile.DAN_HENG,
                    CompanionProfile.HERTA,
                    CompanionProfile.SERVAL,
                    CompanionProfile.NATASHA,
                    CompanionProfile.ASTA,
                    CompanionProfile.TINGYUN,
                    CompanionProfile.QINGQUE,
                    CompanionProfile.GALLAGHER,
                    CompanionProfile.XUEYI,
                    CompanionProfile.LYNX,
                    CompanionProfile.NOELLE,
                    CompanionProfile.CHA_HAEIN,
                    CompanionProfile.BENNETT,
                    CompanionProfile.NEZUKO,
                    CompanionProfile.TANJIRO,
                    CompanionProfile.MEGUMI,
                    CompanionProfile.YUJI_ITADORI,
                    CompanionProfile.ARCHER,
                    CompanionProfile.NICOLE_DEMARA,
                    CompanionProfile.MINT,
                    CompanionProfile.YAOYAO,
                    CompanionProfile.SAYU,
                    CompanionProfile.DIONA,
                    CompanionProfile.SANHUA,
                    CompanionProfile.DANJIN,
                    CompanionProfile.CHIXIA,
                    CompanionProfile.BAIZHI,
                    CompanionProfile.PIPER_WHEEL,
                    CompanionProfile.LUCY,
                    CompanionProfile.CORIN_WICKES,
                    CompanionProfile.SOUKAKU,
                    CompanionProfile.ANBY_DEMARA,
                    CompanionProfile.BILLY_KID
            };
            return CompanionContractItem.createContract(chars4[RNG.nextInt(chars4.length)]);
        }

        int pick = RNG.nextInt(12);
        return switch (pick) {
            case 0 -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD);
            case 1 -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD);
            case 2 -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD);
            case 3 -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD);
            case 4 -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD);
            case 5 -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD);
            case 6 -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS);
            case 7 -> rollRandomArtifact(4);
            case 8 -> SystemItems.HEROS_WIT.isBound() ? new ItemStack(SystemItems.HEROS_WIT.get(), 2) : new ItemStack(Items.GOLDEN_APPLE);
            case 9 -> SystemItems.MYSTIC_ENHANCEMENT_ORE.isBound() ? new ItemStack(SystemItems.MYSTIC_ENHANCEMENT_ORE.get(), 3) : new ItemStack(Items.IRON_INGOT, 8);
            case 10 -> SystemItems.MORA_BUNDLE.isBound() ? new ItemStack(SystemItems.MORA_BUNDLE.get()) : SystemItems.coin(5000);
            default -> SystemItems.ADEPTUS_TEMPTATION.isBound() ? new ItemStack(SystemItems.ADEPTUS_TEMPTATION.get()) : new ItemStack(Items.GOLDEN_APPLE);
        };
    }

    public static ItemStack roll4StarReward(int banner) {
        return roll4StarReward(banner, 0L);
    }

    public static ItemStack rollRandomArtifact(int stars) {
        com.soybe.awakeningsystem.artifact.ArtifactSet[] sets = (stars >= 5) ?
                new com.soybe.awakeningsystem.artifact.ArtifactSet[]{
                        com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY
                } :
                new com.soybe.awakeningsystem.artifact.ArtifactSet[]{
                        com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                        com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART
                };
        com.soybe.awakeningsystem.artifact.ArtifactSet set = sets[RNG.nextInt(sets.length)];
        com.soybe.awakeningsystem.artifact.ArtifactType type = com.soybe.awakeningsystem.artifact.ArtifactType.values()[RNG.nextInt(com.soybe.awakeningsystem.artifact.ArtifactType.values().length)];

        String fieldName = "ARTIFACT_" + set.name() + "_" + type.name();
        try {
            var field = SystemItems.class.getField(fieldName);
            var holder = (net.neoforged.neoforge.registries.DeferredHolder<?, ?>) field.get(null);
            if (holder != null && holder.isBound()) {
                return new ItemStack((net.minecraft.world.item.Item) holder.get());
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Failed to reflect artifact item for field {}: {}", fieldName, e.getMessage());
        }
        return new ItemStack(Items.EMERALD);
    }

    public static ItemStack roll3StarReward() {
        int pick = RNG.nextInt(6);
        return switch (pick) {
            case 0 -> rollRandomArtifact(3);
            case 1 -> SystemItems.FINE_ENHANCEMENT_ORE.isBound() ? new ItemStack(SystemItems.FINE_ENHANCEMENT_ORE.get(), 2) : new ItemStack(Items.IRON_INGOT, 4);
            case 2 -> SystemItems.ADVENTURERS_EXPERIENCE.isBound() ? new ItemStack(SystemItems.ADVENTURERS_EXPERIENCE.get(), 2) : new ItemStack(Items.EXPERIENCE_BOTTLE, 4);
            case 3 -> SystemItems.MORA_POUCH.isBound() ? new ItemStack(SystemItems.MORA_POUCH.get()) : SystemItems.coin(1500);
            case 4 -> SystemItems.BLACK_TASSEL.isBound() ? new ItemStack(SystemItems.BLACK_TASSEL.get()) : new ItemStack(Items.GOLD_INGOT, 4);
            default -> SystemItems.SWEET_MADAME.isBound() ? new ItemStack(SystemItems.SWEET_MADAME.get(), 2) : new ItemStack(Items.COOKED_BEEF, 4);
        };
    }

    public static void handleTopUp(ServerPlayer player, GachaTopUpPayload payload) {
        SystemData data = SystemEvents.data(player);
        int action = payload.actionType();
        int pack = payload.packId();

        double[] usdPrices = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
        int[] crystalAmounts = {60, 330, 1090, 2240, 3880, 8080, 300};

        if (action == 0) {
            if (pack < 0 || pack >= usdPrices.length) return;
            double costUsd = usdPrices[pack];
            if (!data.spendUsdBalance(costUsd)) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_usd", String.format(java.util.Locale.US, "%.2f", costUsd)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }

            if (pack == 6) {
                int bonusCrystals = data.isWelkinEnhanced() ? 450 : 300;
                data.addGenesisCrystals(bonusCrystals);
                data.addWelkinDays(30);
                if (data.isWelkinEnhanced()) {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.welkin_enhanced_activated"), false);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.welkin_activated"), false);
                }
            } else {
                int baseCrystals = crystalAmounts[pack];
                boolean isFirstBonus = !data.hasClaimedFirstTopUp(pack);
                int crystals = isFirstBonus ? (baseCrystals * 2) : baseCrystals;
                if (isFirstBonus) {
                    data.setClaimedFirstTopUp(pack);
                }
                data.addGenesisCrystals(crystals);
                if (isFirstBonus) {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.success_double", String.format(java.util.Locale.US, "%,d", crystals), String.format(java.util.Locale.US, "%,d", crystals)), false);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.success", String.format(java.util.Locale.US, "%,d", crystals), String.format(java.util.Locale.US, "%,d", crystals)), false);
                }
            }
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            AwakeningSystemMod.sync(player);
        } else if (action == 1) {
            int crystals = data.genesisCrystals();
            if (crystals <= 0) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.no_crystals"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }
            int toConvert = (pack > 0 && pack <= crystals) ? pack : crystals;
            data.spendGenesisCrystals(toConvert);
            data.addPrimogems(toConvert);
            player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.converted_crystals", toConvert, toConvert), true);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            AwakeningSystemMod.sync(player);
        } else if (action == 2) {
            int jade = data.stellarJade();
            if (jade <= 0) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.no_jade"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }
            int toConvert = (pack > 0 && pack <= jade) ? pack : jade;
            data.spendStellarJade(toConvert);
            data.addPrimogems(toConvert);
            player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.converted_jade", toConvert, toConvert), true);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            AwakeningSystemMod.sync(player);
        } else if (action == 3) {
            int[] gcValues = {5, 15, 30, 50, 100};
            int[] gcCoins = {25000, 75000, 150000, 250000, 500000};
            if (pack < 0 || pack >= gcValues.length) return;
            int cost = gcCoins[pack];
            if (data.spendCoins(cost)) {
                data.addUsdBalance(gcValues[pack]);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.card_bought", gcValues[pack]), true);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                AwakeningSystemMod.sync(player);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_coins", String.format(java.util.Locale.US, "%,d", cost)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
            }
        } else if (action == 4) {
            int[] caratAmounts = {150, 1500, 4500, 15000};
            int[] coinCosts = {10000, 75000, 200000, 500000};
            if (pack < 0 || pack >= caratAmounts.length) return;
            int cost = coinCosts[pack];
            int carats = caratAmounts[pack];
            if (data.spendCoins(cost)) {
                data.addCarats(carats);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.buy_carats_coins", String.format(java.util.Locale.US, "%,d", carats), String.format(java.util.Locale.US, "%,d", cost)), true);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                AwakeningSystemMod.sync(player);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_coins", String.format(java.util.Locale.US, "%,d", cost)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
            }
        } else if (action == 5) {
            int[] caratAmounts = {150, 1500, 5000, 12000, 25000};
            double[] usdCosts = {0.99, 4.99, 14.99, 29.99, 49.99};
            if (pack < 0 || pack >= caratAmounts.length) return;
            double cost = usdCosts[pack];
            int carats = caratAmounts[pack];
            if (data.spendUsdBalance(cost)) {
                data.addCarats(carats);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.buy_carats_usd", String.format(java.util.Locale.US, "%,d", carats), String.format(java.util.Locale.US, "%.2f", cost)), true);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                AwakeningSystemMod.sync(player);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_usd", String.format(java.util.Locale.US, "%.2f", cost)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
            }
        } else if (action == 6) {
            int crystals = data.genesisCrystals();
            if (crystals <= 0) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.convert.no_crystals"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }
            int toConvert = (pack > 0 && pack <= crystals) ? pack : crystals;
            data.spendGenesisCrystals(toConvert);
            data.addCarats(toConvert);
            player.displayClientMessage(Component.translatable("awakening_system.gacha.convert_carats", toConvert, toConvert), true);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            AwakeningSystemMod.sync(player);
        } else if (action == 7) {
            int[] jadeAmounts = {60, 330, 1090, 2240, 3880, 8080, 300};
            if (pack < 0 || pack >= usdPrices.length) return;
            double costUsd = usdPrices[pack];
            if (!data.spendUsdBalance(costUsd)) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_usd", String.format(java.util.Locale.US, "%.2f", costUsd)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }

            if (pack == 6) {
                int bonusJade = data.isWelkinEnhanced() ? 450 : 300;
                data.addStellarJade(bonusJade);
                data.addWelkinDays(30);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.welkin_activated", bonusJade), false);
            } else {
                int baseJade = jadeAmounts[pack];
                boolean isFirstBonus = !data.hasClaimedFirstJadeTopUp(pack);
                int jade = isFirstBonus ? (baseJade * 2) : baseJade;
                if (isFirstBonus) {
                    data.setClaimedFirstJadeTopUp(pack);
                }
                data.addStellarJade(jade);
                player.displayClientMessage(Component.translatable(isFirstBonus ? "awakening_system.gacha.topup.success_2x" : "awakening_system.gacha.topup.success", String.format(java.util.Locale.US, "%,d", jade)), false);
            }
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            AwakeningSystemMod.sync(player);
        } else if (action == 8) {
            int[] jadeAmounts = {160, 1600, 4800, 16000};
            int[] coinCosts = {10000, 75000, 200000, 500000};
            if (pack < 0 || pack >= jadeAmounts.length) return;
            int cost = coinCosts[pack];
            int jade = jadeAmounts[pack];
            if (data.spendCoins(cost)) {
                data.addStellarJade(jade);
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.buy_success", String.format(java.util.Locale.US, "%,d", jade), String.format(java.util.Locale.US, "%,d", cost)), true);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                AwakeningSystemMod.sync(player);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.insufficient_coins", String.format(java.util.Locale.US, "%,d", cost)), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
            }
        } else if (action == 9) {
            int crystals = data.genesisCrystals();
            if (crystals <= 0) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.convert.no_crystals"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return;
            }
            int toConvert = (pack > 0 && pack <= crystals) ? pack : crystals;
            data.spendGenesisCrystals(toConvert);
            data.addStellarJade(toConvert);
            player.displayClientMessage(Component.translatable("awakening_system.gacha.convert.success", toConvert, toConvert), true);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            AwakeningSystemMod.sync(player);
        }
    }

    public static void handleClaimWelkin(ServerPlayer player, ClaimWelkinDailyPayload payload) {
        SystemData data = SystemEvents.data(player);
        long currentDay = player.serverLevel().getDayTime() / 24000L;
        if (data.welkinDays() > 0 && currentDay > data.lastWelkinClaimDay()) {
            int reward = data.isWelkinEnhanced() ? 135 : 90;
            data.addPrimogems(reward);
            data.setWelkinDays(data.welkinDays() - 1);
            data.setLastWelkinClaimDay(currentDay);
            if (data.isWelkinEnhanced()) {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.welkin_enhanced_daily_claimed", reward, data.welkinDays()), false);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.gacha.topup.welkin_daily_claimed", data.welkinDays()), false);
            }
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            AwakeningSystemMod.sync(player);
        }
    }

    public static void handleStartWelkinQuest(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        if (data.isWelkinEnhanced()) {
            player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.already_enhanced"), false);
            return;
        }
        if (data.welkinQuestStep() == 0) {
            data.setWelkinQuestStep(1);
            data.setWelkinQuestProgress(0);

            ItemStack guide = new ItemStack(SystemItems.TRILUNAR_GUIDE.get());
            if (!player.getInventory().add(guide)) {
                player.drop(guide, false);
            }

            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            AwakeningSystemMod.notify(player,
                    "awakening_system.welkin.quest.started_title",
                    "awakening_system.welkin.quest.step1_desc",
                    0xFFCE93D8);
            player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.started_chat"), false);
            AwakeningSystemMod.sync(player);
        } else {
            String stepKey = switch (data.welkinQuestStep()) {
                case 1 -> "awakening_system.welkin.quest.step1_status";
                case 2 -> "awakening_system.welkin.quest.step2_status";
                case 3 -> "awakening_system.welkin.quest.step3_status";
                default -> "awakening_system.welkin.quest.step1_status";
            };
            player.displayClientMessage(Component.translatable(stepKey, data.welkinQuestProgress()), false);
        }
    }

    public static void completeColumbinaAscension(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        if (data.isWelkinEnhanced()) return;
        data.setWelkinEnhanced(true);
        data.setWelkinQuestStep(4);
        data.setWelkinQuestProgress(0);
        data.addGenesisCrystals(450);
        data.addPrimogems(300);

        ItemStack contract = com.soybe.awakeningsystem.item.CompanionContractItem.createContract(com.soybe.awakeningsystem.entity.CompanionProfile.COLUMBINA);
        if (!player.getInventory().add(contract)) {
            player.drop(contract, false);
        }

        if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 50, 0.6D, 0.6D, 0.6D, 0.08D);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.04D);
        }
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        AwakeningSystemMod.notify(player,
                "awakening_system.welkin.quest.completed_title",
                "awakening_system.welkin.quest.completed_desc",
                0xFFCE93D8);
        player.displayClientMessage(Component.translatable("awakening_system.welkin.quest.completed_chat"), false);
        AwakeningSystemMod.sync(player);
    }
}
