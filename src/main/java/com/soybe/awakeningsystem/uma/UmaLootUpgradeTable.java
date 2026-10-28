package com.soybe.awakeningsystem.uma;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de ascensión y mejora de botín para la Casa de Apuestas del Hipódromo de Teyvat.
 * Permite a los jugadores apostar los drops obtenidos en Mazmorras (Dungeons)
 * bajo la regla de "Doble o Nada / Ascensión de Rango".
 */
public final class UmaLootUpgradeTable {

    private UmaLootUpgradeTable() {}

    public record UpgradeResult(List<ItemStack> upgradedItems, Component celebrationMessage) {}

    /**
     * Procesa la victoria de la Uma y calcula la versión ascendida o multiplicada del botín apostado.
     *
     * @param originalItem Ítem de mazmorra apostado
     * @param winningUma Perfil de la corredora ganadora (determina multiplicador por cuota)
     * @return Resultado con los ítems ascendidos y mensaje para el jugador
     */
    public static UpgradeResult upgradeLoot(ItemStack originalItem, UmaProfile winningUma) {
        if (originalItem == null || originalItem.isEmpty()) {
            return new UpgradeResult(List.of(), Component.translatable("awakening_system.uma.loot.no_bet"));
        }

        List<ItemStack> results = new ArrayList<>();
        double odds = winningUma.getBaseOdds();
        int originalCount = originalItem.getCount();

        if (originalItem.getItem() instanceof CasinoChipItem chip) {
            int payoutCount = Math.max(1, (int) Math.round(originalCount * odds));
            results.add(new ItemStack(chip, payoutCount));
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.chips_win", odds, payoutCount, chip.getTier().getDisplayName()));
        }

        if (SystemItems.MORA != null && originalItem.is(SystemItems.MORA.get())) {
            int payoutCount = Math.max(1, (int) Math.round(originalCount * odds));
            results.add(new ItemStack(SystemItems.MORA.get(), payoutCount));
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.mora_win", payoutCount, winningUma.getDisplayName()));
        }

        if (SystemItems.AWAKENING_COIN != null && originalItem.is(SystemItems.AWAKENING_COIN.get())) {
            int payoutCount = Math.max(1, (int) Math.round(originalCount * odds));
            results.add(new ItemStack(SystemItems.AWAKENING_COIN.get(), payoutCount));
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.coins_win", payoutCount));
        }

        if (SystemItems.FABLE_SCROLL != null && originalItem.is(SystemItems.FABLE_SCROLL.get())) {
            int payoutCount = Math.min(64, originalCount * 2);
            results.add(new ItemStack(SystemItems.FABLE_SCROLL.get(), payoutCount));
            if (SystemItems.CASINO_CHIP_GOLD != null) {
                results.add(new ItemStack(SystemItems.CASINO_CHIP_GOLD.get(), 2));
            }
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.fable_win", payoutCount));
        }

        if (SystemItems.OLIVE_ELIXIR != null && originalItem.is(SystemItems.OLIVE_ELIXIR.get())) {
            int payoutCount = Math.min(64, originalCount * 2);
            results.add(new ItemStack(SystemItems.OLIVE_ELIXIR.get(), payoutCount));
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.elixir_win"));
        }

        if (SystemItems.VIGOR_POTION != null && originalItem.is(SystemItems.VIGOR_POTION.get())) {
            int payoutCount = Math.min(64, originalCount * 2);
            results.add(new ItemStack(SystemItems.VIGOR_POTION.get(), payoutCount));
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.vigor_win"));
        }

        if (originalItem.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatSigilItem) {
            int payoutCount = Math.min(64, (int) Math.round(originalCount * odds));
            results.add(new ItemStack(originalItem.getItem(), payoutCount));
            if (SystemItems.MORA != null) {
                results.add(new ItemStack(SystemItems.MORA.get(), 500));
            }
            return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.sigil_win", payoutCount));
        }

        int multipliedCount = Math.min(originalItem.getMaxStackSize(), Math.max(1, originalCount * 2));
        ItemStack duplicated = originalItem.copy();
        duplicated.setCount(multipliedCount);
        results.add(duplicated);

        if (SystemItems.MORA != null) {
            results.add(new ItemStack(SystemItems.MORA.get(), 250));
        }

        return new UpgradeResult(results, Component.translatable("awakening_system.uma.loot.default_win", multipliedCount, originalItem.getHoverName()));
    }
}
