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
    private static final Random RNG = new Random();

    private GachaManager() {}

    public static void openGachaScreen(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        List<String> histStrings = new ArrayList<>();
        for (SystemData.WishLogEntry entry : data.wishHistory()) {
            histStrings.add(entry.itemName() + "::" + entry.stars() + "::" + entry.bannerName() + "::" + entry.timestamp());
        }

        // Chequeo diario de la Bendición Lunar (Welkin Moon)
        long currentDay = player.serverLevel().getDayTime() / 24000L;
        boolean unclaimedWelkin = (data.welkinDays() > 0 && currentDay > data.lastWelkinClaimDay());

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

        // 1. Si el objeto es apilable (monedas, carne, lingotes, lapis, etc.), intentar fusionar en stacks existentes del inventario
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

        // 2. Si aún quedan unidades o es no apilable (armas, contratos), buscar un slot libre en el inventario (0..35)
        int freeSlot = player.getInventory().getFreeSlot();
        if (freeSlot >= 0) {
            player.getInventory().items.set(freeSlot, remaining.copy());
            remaining.setCount(0);
            player.inventoryMenu.broadcastChanges();
            return;
        }

        // 3. El inventario del jugador está 100% LLENO (freeSlot == -1):
        // Enviar DIRECTAMENTE al Subespacio del Sistema (evita que el modo creativo de Minecraft destruya el objeto)
        ItemStack leftover = data.addToSubspace(remaining);
        if (sentToSubspace != null && sentToSubspace.length > 0) sentToSubspace[0] = true;
        if (subspaceCounter != null && subspaceCounter.length > 0) subspaceCounter[0]++;

        // 4. Si incluso el Subespacio de 54 casillas estuviera lleno, soltar al suelo para no perderlo jamás
        if (!leftover.isEmpty()) {
            player.drop(leftover, false);
        }
    }

    public static void deliverGachaReward(ServerPlayer player, SystemData data, ItemStack stack) {
        deliverGachaReward(player, data, stack, new boolean[]{false}, new int[]{0});
    }

    public static void handleWish(ServerPlayer player, GachaWishPayload payload) {
        SystemData data = SystemEvents.data(player);
        int banner = payload.bannerId();
        int count = Math.min(10, Math.max(1, payload.pullCount()));

        PromotionalBanner.BannerPhase rotatingPromoPhase = PromotionalBanner.getActivePhase(player.serverLevel().getDayTime());
        PromotionalBanner.Franchise promoFranchise = rotatingPromoPhase.franchise();

        String bannerName;
        if (banner == 0) bannerName = "Armas 5★";
        else if (banner == 2) bannerName = "Permanente";
        else if (banner == 3) bannerName = "Principiantes";
        else {
            bannerName = rotatingPromoPhase.bannerTitle();
        }

        // Cobro y verificación según Banner
        boolean isPromotional = (banner == 0 || banner == 1 || (banner >= 4 && banner <= 8));
        if (banner == 3) {
            // BANNER DE PRINCIPIANTES (Beginners' Wish)
            if (data.beginnerPulls() >= 20) {
                player.displayClientMessage(Component.literal("§c[Gacha] ¡Has completado el límite máximo de 20 tiradas del Deseo de Principiantes!"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
            count = 10; // Solo se tira de 10 en 10
            int fateCost = 8; // 20% de descuento (8 destinos del encuentro)
            int primoCost = 1280; // 20% de descuento (1,280 protogemas)

            if (data.acquaintFates() >= fateCost) {
                data.spendAcquaintFates(fateCost);
            } else if (!data.spendPrimogems(primoCost)) {
                player.displayClientMessage(Component.literal("§c[Gacha] ¡No tienes suficientes Destinos del Encuentro (8) ni Protogemas (" + primoCost + " requeridas con 20% dto.)!"), true);
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
        } else if (isPromotional) {
            // BANNERS PROMOCIONALES MULTIVERSO (Destinos Entrelazados o Protogemas)
            if (data.intertwinedFates() >= count) {
                data.spendIntertwinedFates(count);
            } else {
                int costPrimos = count * 160;
                if (!data.spendPrimogems(costPrimos)) {
                    player.displayClientMessage(Component.literal("§c[Gacha] ¡No tienes suficientes Destinos Entrelazados ni Protogemas (" + costPrimos + " requeridas)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
        } else {
            // BANNER PERMANENTE ESTÁNDAR (Destinos del Encuentro o Protogemas)
            if (data.acquaintFates() >= count) {
                data.spendAcquaintFates(count);
            } else {
                int costPrimos = count * 160;
                if (!data.spendPrimogems(costPrimos)) {
                    player.displayClientMessage(Component.literal("§c[Gacha] ¡No tienes suficientes Destinos del Encuentro ni Protogemas (" + costPrimos + " requeridas)!"), true);
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

            // Si es la primera tirada de 10 del banner de principiantes, garantizar a Noelle en el slot 0
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

            // 0. Chequeo de Mandoble del Creador: Juicio del Génesis (0.008% / ~1 en 12500 tiradas)
            if (RNG.nextDouble() < 0.00008D) {
                highestRarity = 5;
                ItemStack creatorSword = new ItemStack(SystemItems.CREATOR_CLAYMORE.get());
                wonItems.add(creatorSword);
                itemRarities.add(5);
                data.addStarglitter(100); // 100 Brillo Estelar por reliquia del Creador
                data.addMora(250000); // 250,000 Mora
                data.addWishHistoryEntry(creatorSword.getHoverName().getString(), 5, bannerName);

                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 1.4F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

                Component creatorAnnounce = Component.literal("§6§l✦✦✦✦✦✦✦ ¡¡¡CREACIÓN SUPREMA DEL CREADOR (0.008%)!!! §e" + player.getName().getString() +
                        " §fha obtenido del Gacha la mítica arma: §6" + creatorSword.getHoverName().getString() + "§6§l! ✦✦✦✦✦✦✦");
                for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
                    online.displayClientMessage(creatorAnnounce, false);
                }
                continue;
            }

            // 0.1 Chequeo de La Doncella (0.02% / ~1 en 5000 tiradas directas en cualquier banner)
            if (RNG.nextDouble() < 0.00020D) {
                highestRarity = 5;
                ItemStack doncellaSword = new ItemStack(SystemItems.LA_DONCELLA.get());
                wonItems.add(doncellaSword);
                itemRarities.add(5);
                data.addStarglitter(30); // 30 Brillo Estelar
                data.addMora(50000); // 50,000 Mora
                data.addWishHistoryEntry(doncellaSword.getHoverName().getString(), 5, bannerName);

                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.3F, 1.1F);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

                Component doncellaAnnounce = Component.literal("§d§l✦✦✦✦✦ ¡RELIQUIA DESPIERTA (0.02%)! §e" + player.getName().getString() +
                        " §fha obtenido del Gacha: §d" + doncellaSword.getHoverName().getString() + "§d§l! ✦✦✦✦✦");
                for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
                    online.displayClientMessage(doncellaAnnounce, false);
                }
                continue;
            }

            // Probabilidad 5★ con Pity suave (73+) y Hard Pity (80)
            double rate5 = 0.006D;
            if (pity5 >= 73) {
                rate5 += (pity5 - 73) * 0.075D;
            }
            if (pity5 >= 79) {
                rate5 = 1.0D;
            }

            // Probabilidad 4★ (Hard pity en 10)
            double rate4 = 0.051D;
            if (pity4 >= 9) {
                rate4 = 1.0D;
            }

            double roll = RNG.nextDouble();
            if (roll < rate5) {
                // 5★ OBTENIDO
                highestRarity = 5;
                data.setGachaPity5Star(0);
                data.incrementGachaPity4Star();

                ItemStack item5;
                if (banner == 0) {
                    PromotionalBanner.BannerPhase phase = PromotionalBanner.getActivePhase(player.serverLevel().getDayTime());
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
                                player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡SENDA DIVINA CUMPLIDA! Has obtenido tu arma fijada: " + item5.getHoverName().getString() + "! ✦✦✦✦✦"), false);
                            } else if (RNG.nextBoolean()) {
                                item5 = wp1.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡SENDA DIVINA ACERTADA! Has obtenido tu arma fijada: " + item5.getHoverName().getString() + "! ✦✦✦✦✦"), false);
                            } else {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(1);
                                player.displayClientMessage(Component.literal("§e[Senda Divina] Has obtenido la otra arma promocional (" + item5.getHoverName().getString() + "). §6¡Punto de Destino (1/1)! Tu próxima arma será la elegida."), false);
                            }
                        } else if (target == 2) {
                            if (data.weaponBannerFatePoints() >= 1) {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡SENDA DIVINA CUMPLIDA! Has obtenido tu arma fijada: " + item5.getHoverName().getString() + "! ✦✦✦✦✦"), false);
                            } else if (RNG.nextBoolean()) {
                                item5 = wp2.copy();
                                data.setWeaponBannerFatePoints(0);
                                player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡SENDA DIVINA ACERTADA! Has obtenido tu arma fijada: " + item5.getHoverName().getString() + "! ✦✦✦✦✦"), false);
                            } else {
                                item5 = wp1.copy();
                                data.setWeaponBannerFatePoints(1);
                                player.displayClientMessage(Component.literal("§e[Senda Divina] Has obtenido la otra arma promocional (" + item5.getHoverName().getString() + "). §6¡Punto de Destino (1/1)! Tu próxima arma será la elegida."), false);
                            }
                        } else {
                            ItemStack featuredWp = (phase.secondaryFeaturedWeapon() != null && RNG.nextBoolean()) ? wp2 : wp1;
                            item5 = featuredWp.copy();
                            player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡HAS OBTENIDO EL ARMA CELESTIAL PROMOCIONAL: " + item5.getHoverName().getString() + "! ✦✦✦✦✦"), false);
                        }
                    } else {
                        data.setGachaGuaranteedRateUp(true);
                        if (target > 0) {
                            data.setWeaponBannerFatePoints(1);
                        }
                        item5 = rollStandardWeapon5Star();
                        player.displayClientMessage(Component.literal("§c[Gacha] ¡Has perdido el 75/25 con un arma estándar (" + item5.getHoverName().getString() + ")! ¡Tu próxima arma 5★ está 100% GARANTIZADA!"), false);
                    }
                } else if (banner == 1 || (banner >= 4 && banner <= 9)) {
                    PromotionalBanner.BannerPhase phase = rotatingPromoPhase;
                    boolean winFeatured = data.gachaGuaranteedRateUp() || RNG.nextBoolean();
                    if (winFeatured) {
                        data.setGachaGuaranteedRateUp(false);
                        CompanionProfile targetProfile;
                        if (phase.secondaryFeaturedCharacter() != null) {
                            targetProfile = (data.characterBannerTarget() == 1) ? phase.secondaryFeaturedCharacter() : phase.featuredCharacter();
                        } else {
                            targetProfile = phase.featuredCharacter();
                        }
                        item5 = CompanionContractItem.createContract(targetProfile);
                        player.displayClientMessage(Component.literal("§6§l✦✦✦✦✦ ¡HAS OBTENIDO AL PERSONAJE PROMOCIONAL: " + targetProfile.displayName() + " 5★ (" + promoFranchise.displayName() + ")! ✦✦✦✦✦"), false);
                    } else {
                        data.setGachaGuaranteedRateUp(true);
                        CompanionProfile[] perm5Pool = {CompanionProfile.DILUC, CompanionProfile.JEAN, CompanionProfile.ZHONGLI, CompanionProfile.GANYU};
                        CompanionProfile perm5 = perm5Pool[RNG.nextInt(perm5Pool.length)];
                        item5 = CompanionContractItem.createContract(perm5);
                        player.displayClientMessage(Component.literal("§c[Gacha] ¡Has perdido el 50/50 con " + perm5.displayName() + " 5★ (Permanente)! ¡Tu próximo 5★ está 100% GARANTIZADO!"), false);
                    }
                } else {
                    item5 = rollStandard5Star();
                }

                wonItems.add(item5);
                itemRarities.add(5);
                data.addStarglitter(10); // 10 Brillo Estelar por 5★
                data.addMora(25000); // 25,000 Mora por 5★
                data.addWishHistoryEntry(item5.getHoverName().getString(), 5, bannerName);

            } else if (roll < rate5 + rate4) {
                // 4★ OBTENIDO
                highestRarity = Math.max(highestRarity, 4);
                data.incrementGachaPity5Star();
                data.setGachaPity4Star(0);

                ItemStack item4 = roll4StarReward(banner, player.serverLevel().getDayTime());
                wonItems.add(item4);
                itemRarities.add(4);
                data.addStarglitter(2); // 2 Brillo Estelar por 4★
                data.addMora(5000); // 5,000 Mora por 4★
                data.addWishHistoryEntry(item4.getHoverName().getString(), 4, bannerName);
            } else {
                // 3★ OBTENIDO
                data.incrementGachaPity5Star();
                data.incrementGachaPity4Star();

                ItemStack item3 = roll3StarReward();
                wonItems.add(item3);
                itemRarities.add(3);
                data.addStardust(15); // 15 Polvo Estelar por 3★
                data.addMora(1000); // 1,000 Mora por 3★
                data.addWishHistoryEntry(item3.getHoverName().getString(), 3, bannerName);
            }
        }

        if (banner == 3) {
            data.incrementBeginnerPulls(10);
        }

        // Entregar objetos al jugador (con protección automática al Subespacio si el inventario está lleno)
        boolean[] anySentToSubspace = new boolean[]{false};
        int[] subspaceCount = new int[]{0};
        for (ItemStack stack : wonItems) {
            deliverGachaReward(player, data, stack, anySentToSubspace, subspaceCount);
        }

        if (anySentToSubspace[0]) {
            player.displayClientMessage(Component.literal("§6✦ [SISTEMA] §e¡Tu inventario estaba lleno! §b" + subspaceCount[0] + " objeto(s) §fse guardaron de forma segura en tu §eSubespacio del Sistema§f."), false);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
        }

        AwakeningSystemMod.sync(player);

        // Enviar resultado visual a la pantalla del cliente con saldo sincronizado
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
            case 0 -> { // 75 Polvo Estelar -> 1 Destino Entrelazado
                int cost = 75 * count;
                if (data.spendStardust(cost)) {
                    data.addIntertwinedFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.literal("§b✦ [GANGAS DE PAIMON] §fHas canjeado §b" + count + " Destino(s) Entrelazado(s) §fpor §e" + cost + " Polvo Estelar§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Polvo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 1 -> { // 75 Polvo Estelar -> 1 Destino del Encuentro
                int cost = 75 * count;
                if (data.spendStardust(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.literal("§b✦ [GANGAS DE PAIMON] §fHas canjeado §b" + count + " Destino(s) del Encuentro §fpor §e" + cost + " Polvo Estelar§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Polvo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 2 -> { // 5 Brillo Estelar -> 1 Destino Entrelazado
                int cost = 5 * count;
                if (data.spendStarglitter(cost)) {
                    data.addIntertwinedFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.literal("§6✦ [GANGAS DE PAIMON] §fHas canjeado §6" + count + " Destino(s) Entrelazado(s) §fpor §6" + cost + " Brillo Estelar§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Brillo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 3 -> { // 5 Brillo Estelar -> 1 Destino del Encuentro
                int cost = 5 * count;
                if (data.spendStarglitter(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.literal("§6✦ [GANGAS DE PAIMON] §fHas canjeado §6" + count + " Destino(s) del Encuentro §fpor §6" + cost + " Brillo Estelar§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Brillo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 4 -> { // 34 Brillo Estelar -> Contrato: Noelle (4★)
                int cost = 34;
                if (data.spendStarglitter(cost)) {
                    ItemStack contract = CompanionContractItem.createContract(CompanionProfile.NOELLE);
                    deliverGachaReward(player, data, contract);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
                    player.displayClientMessage(Component.literal("§6✦ [GANGAS DE PAIMON] §f¡Has adquirido el §ePacto de Invocación: Noelle 4★§f!"), false);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Brillo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 5 -> { // 34 Brillo Estelar -> Contrato: Cha Hae-In (4★)
                int cost = 34;
                if (data.spendStarglitter(cost)) {
                    ItemStack contract = CompanionContractItem.createContract(CompanionProfile.CHA_HAEIN);
                    deliverGachaReward(player, data, contract);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
                    player.displayClientMessage(Component.literal("§6✦ [GANGAS DE PAIMON] §f¡Has adquirido el §ePacto de Invocación: Cha Hae-In 4★§f!"), false);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Brillo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 6 -> { // 15 Polvo Estelar -> 15,000 Mora
                int cost = 15 * count;
                if (data.spendStardust(cost)) {
                    data.addMora(15000 * count);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.literal("§e✦ [GANGAS DE PAIMON] §fHas canjeado §e+" + String.format("%,d", 15000 * count) + " Mora§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Polvo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 7 -> { // 10 Polvo Estelar -> 8 Botellas de Experiencia
                int cost = 10 * count;
                if (data.spendStardust(cost)) {
                    ItemStack exp = new ItemStack(Items.EXPERIENCE_BOTTLE, 8 * count);
                    deliverGachaReward(player, data, exp);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.literal("§a✦ [GANGAS DE PAIMON] §fHas canjeado §a+" + (8 * count) + " Botellas de Experiencia§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Polvo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            case 8 -> { // 10 Brillo Estelar -> 50,000 Mora
                int cost = 10 * count;
                if (data.spendStarglitter(cost)) {
                    data.addMora(50000 * count);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.5F);
                    player.displayClientMessage(Component.literal("§6✦ [GANGAS DE PAIMON] §fHas canjeado §6+" + String.format("%,d", 50000 * count) + " Mora§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Brillo Estelar (" + cost + " requerido)!"), true);
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    return;
                }
            }
            default -> { // 30,000 Mora -> 1 Destino del Encuentro
                int cost = 30000 * count;
                if (data.spendMora(cost)) {
                    data.addAcquaintFates(count);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    player.displayClientMessage(Component.literal("§b✦ [GANGAS DE PAIMON] §fHas adquirido §b" + count + " Destino(s) del Encuentro §fpor §e" + String.format("%,d", cost) + " Mora§f."), true);
                } else {
                    player.displayClientMessage(Component.literal("§c[Gangas de Paimon] ¡No tienes suficiente Mora (" + String.format("%,d", cost) + " requerida)!"), true);
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
            // Senda divina para armas
            if (data.weaponBannerTarget() != payload.targetIndex()) {
                data.setWeaponBannerTarget(payload.targetIndex());
                data.setWeaponBannerFatePoints(0); // Reiniciar puntos de destino al cambiar de arma
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(Component.literal("§6[Senda Divina] §fHas fijado el arma objetivo para la Encarnación Divina."), true);
            }
        } else if (payload.bannerType() == 1) {
            // Objetivo en banner doble de personajes
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
        int pick = RNG.nextInt(23);
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
            // HSR & ZZZ & Honkai 5★
            case 14 -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 15 -> SystemItems.QINGMING_SWORD.isBound() ? new ItemStack(SystemItems.QINGMING_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 16 -> SystemItems.TAILLESS.isBound() ? new ItemStack(SystemItems.TAILLESS.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 17 -> SystemItems.SHUNGUANG_SWORD.isBound() ? new ItemStack(SystemItems.SHUNGUANG_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 18 -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 19 -> SystemItems.LIGHT_CONE_PATIENCE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 20 -> SystemItems.LIGHT_CONE_DREAMS_REST.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 21 -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 22 -> SystemItems.LA_DONCELLA.isBound() ? new ItemStack(SystemItems.LA_DONCELLA.get()) : new ItemStack(Items.NETHERITE_SWORD);
            default -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD);
        };
    }

    public static ItemStack rollFeaturedShonen5Star() {
        int pick = RNG.nextInt(28);
        return switch (pick) {
            // Compañeros Invocables 5★ (Genshin, WuWa, HSR & Shonen)
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
            // Armas & Reliquias Místicas 5★
            case 15 -> SystemItems.LIGHT_CONE_MILKY_WAY.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 16 -> SystemItems.COMMAND_SPELL.isBound() ? new ItemStack(SystemItems.COMMAND_SPELL.get()) : new ItemStack(Items.NETHER_STAR);
            case 17 -> rollVisionItem();
            case 18 -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE);
            case 19 -> SystemItems.QINGMING_SWORD.isBound() ? new ItemStack(SystemItems.QINGMING_SWORD.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 20 -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 21 -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 22 -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD);
            case 23 -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 24 -> SystemItems.LIGHT_CONE_PATIENCE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 25 -> SystemItems.LIGHT_CONE_DREAMS_REST.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 26 -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD);
            default -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE);
        };
    }

    public static ItemStack rollStandard5Star() {
        int pick = RNG.nextInt(12);
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
            case 9 -> CompanionContractItem.createContract(CompanionProfile.ZHONGLI);
            case 10 -> CompanionContractItem.createContract(CompanionProfile.RAIDEN_SHOGUN);
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

    // EL POOL 4★ CONTIENE PERSONAJES 4★, ARMAS Y RELIQUIAS 4★
    public static ItemStack roll4StarReward(int banner, long dayTime) {
        if (banner == 0 || banner == 1 || (banner >= 4 && banner <= 9)) {
            PromotionalBanner.BannerPhase phase = PromotionalBanner.getActivePhase(dayTime);
            // 50% de probabilidad de rate-up 4★
            if (RNG.nextBoolean()) {
                if (RNG.nextBoolean() && !phase.rateUp4StarCharacters().isEmpty()) {
                    CompanionProfile char4 = phase.rateUp4StarCharacters().get(RNG.nextInt(phase.rateUp4StarCharacters().size()));
                    return CompanionContractItem.createContract(char4);
                } else if (!phase.rateUp4StarWeapons().isEmpty()) {
                    return phase.rateUp4StarWeapons().get(RNG.nextInt(phase.rateUp4StarWeapons().size())).get().copy();
                }
            }
        }

        // 50% de probabilidad de Personaje 4★, 50% de Armas y Reliquias 4★
        if (RNG.nextBoolean()) {
            CompanionProfile[] chars4 = new CompanionProfile[]{
                    CompanionProfile.MARCH_7TH,
                    CompanionProfile.NOELLE,
                    CompanionProfile.CHA_HAEIN,
                    CompanionProfile.BENNETT,
                    CompanionProfile.NEZUKO,
                    CompanionProfile.TANJIRO,
                    CompanionProfile.MEGUMI,
                    CompanionProfile.YUJI_ITADORI,
                    CompanionProfile.ARCHER
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
        } catch (Exception ignored) {}
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
                data.addStellarJade(crystals);
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

            // Entregar la Guía Sagrada Trilunar
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
        data.addGenesisCrystals(450); // Cristales Génesis de Ascensión
        data.addPrimogems(300); // Bonificación de ascensión

        // Entregar el Contrato de Invocación 5★ de Columbina
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
