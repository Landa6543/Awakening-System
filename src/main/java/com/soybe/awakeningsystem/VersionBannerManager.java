package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.CompanionProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

/**
 * Gestor del Sistema de Versiones, Ciclos de Banners y Mantenimiento de Servidores.
 * Modela las actualizaciones oficiales (Fase 1 y Fase 2 con Banners Dobles simultáneos), el mantenimiento técnico
 * de 6 minutos reales (7,200 ticks = 6 horas MC) con el gacha desactivado, y la entrega
 * automática de 600 Protogemas de compensación al iniciar cada nueva fase rotativa.
 */
public final class VersionBannerManager {

    public static final long MC_DAY_TICKS = 24000L;
    public static final long PHASE_DAYS = PromotionalBanner.BANNER_CYCLE_DAYS;
    public static final long PHASE_TICKS = PHASE_DAYS * MC_DAY_TICKS;
    public static final long VERSION_DAYS = PHASE_DAYS * 2L;
    public static final long VERSION_TICKS = VERSION_DAYS * MC_DAY_TICKS;
    public static final long MAINTENANCE_TICKS = 7200L;

    public record VersionInfo(
            int versionNumber,
            String versionName,
            String phase1Title,
            List<CompanionProfile> phase1Characters,
            List<CompanionProfile> phase1RateUp4Star,
            String phase2Title,
            List<CompanionProfile> phase2Characters,
            List<CompanionProfile> phase2RateUp4Star,
            String nextVersionTeaser
    ) {}

    private VersionBannerManager() {}

    public static int getVersionNumber(long worldDayTime) {
        int phaseNum = PromotionalBanner.getPhaseNumber(worldDayTime);
        return ((phaseNum - 1) / 2) + 1;
    }

    public static int getPhaseNumber(long worldDayTime) {
        int phaseNum = PromotionalBanner.getPhaseNumber(worldDayTime);
        return ((phaseNum - 1) % 2) + 1;
    }

    public static VersionInfo getCurrentVersionInfo(long worldDayTime) {
        int vNum = getVersionNumber(worldDayTime);
        int totalPhases = Math.max(1, PromotionalBanner.getTotalPhases());

        int p1Idx = ((vNum - 1) * 2) % totalPhases;
        int p2Idx = (p1Idx + 1) % totalPhases;

        PromotionalBanner.BannerPhase bPhase1 = PromotionalBanner.getPhaseByIndex(p1Idx);
        PromotionalBanner.BannerPhase bPhase2 = PromotionalBanner.getPhaseByIndex(p2Idx);

        List<CompanionProfile> p1Chars = bPhase1.secondaryFeaturedCharacter() != null
                ? List.of(bPhase1.featuredCharacter(), bPhase1.secondaryFeaturedCharacter())
                : List.of(bPhase1.featuredCharacter());

        List<CompanionProfile> p2Chars = bPhase2.secondaryFeaturedCharacter() != null
                ? List.of(bPhase2.featuredCharacter(), bPhase2.secondaryFeaturedCharacter())
                : List.of(bPhase2.featuredCharacter());

        int nextVNum = vNum + 1;
        int nextPIdx = (p2Idx + 1) % totalPhases;
        PromotionalBanner.BannerPhase nextPhase = PromotionalBanner.getPhaseByIndex(nextPIdx);

        return new VersionInfo(
                vNum,
                "Version " + vNum + ".0 • " + bPhase1.bannerTitle(),
                "Phase 1: " + bPhase1.bannerTitle(),
                p1Chars,
                bPhase1.rateUp4StarCharacters(),
                "Phase 2: " + bPhase2.bannerTitle(),
                p2Chars,
                bPhase2.rateUp4StarCharacters(),
                "v" + nextVNum + ".0: " + nextPhase.bannerTitle() + " (" + nextPhase.franchise().displayName() + ")!"
        );
    }

    public static long getPhaseRemainingTicks(long worldDayTime) {
        long effTime = PromotionalBanner.getEffectiveWorldTime(worldDayTime);
        long currentCycleTicks = effTime % PHASE_TICKS;
        return PHASE_TICKS - currentCycleTicks;
    }

    public static String getPhaseRemainingFormatted(long worldDayTime) {
        long ticks = getPhaseRemainingTicks(worldDayTime);
        long days = ticks / MC_DAY_TICKS;
        long hours = (ticks % MC_DAY_TICKS) / 1000L;
        return days + " d. " + hours + " h.";
    }

    /**
     * Determina si el sistema se encuentra en los 6 minutos reales (7,200 ticks) de mantenimiento
     * técnico al finalizar el ciclo de cada fase de 14 días.
     */
    public static boolean isUnderMaintenance(long worldDayTime) {
        return isUnderMaintenanceByEffectiveTicks(PromotionalBanner.getEffectiveWorldTime(worldDayTime));
    }

    public static boolean isUnderMaintenanceByEffectiveTicks(long effTime) {
        long timeInPhase = effTime % PHASE_TICKS;
        return timeInPhase >= (PHASE_TICKS - MAINTENANCE_TICKS);
    }

    public static long getMaintenanceRemainingTicks(long worldDayTime) {
        if (!isUnderMaintenance(worldDayTime)) return 0L;
        long effTime = PromotionalBanner.getEffectiveWorldTime(worldDayTime);
        long timeInPhase = effTime % PHASE_TICKS;
        return PHASE_TICKS - timeInPhase;
    }

    public static String getMaintenanceRemainingFormatted(long worldDayTime) {
        long ticks = getMaintenanceRemainingTicks(worldDayTime);
        long seconds = ticks / 20L;
        long min = seconds / 60L;
        long sec = seconds % 60L;
        return String.format("%02d:%02d", min, sec);
    }

    /**
     * Verifica y entrega automáticamente la compensación de 600 Protogemas tras el mantenimiento
     * al comenzar cada nueva fase rotativa de 14 días para el jugador, de manera asincrónica.
     */
    public static boolean checkAndAwardMaintenanceCompensation(ServerPlayer player, SystemData data) {
        if (!data.teyvatUnlocked()) return false;

        long dayTime = player.serverLevel().getDayTime();
        if (data.gachaEpochTick() == 0L) {
            data.setGachaEpochTick(dayTime);
            data.setLastClaimedMaintenanceVersion(1);
            return false;
        }

        long playerEffTime = Math.max(0L, dayTime - data.gachaEpochTick());
        if (isUnderMaintenanceByEffectiveTicks(playerEffTime)) return false;

        int currentPhaseCycle = (int) (playerEffTime / PHASE_TICKS) + 1;
        if (currentPhaseCycle > data.lastClaimedMaintenanceVersion()) {
            data.setLastClaimedMaintenanceVersion(currentPhaseCycle);
            data.addPrimogems(600);
            PromotionalBanner.BannerPhase activePhase = PromotionalBanner.getActivePhaseByEffectiveTicks(playerEffTime, 0);
            int phaseNumber = PromotionalBanner.getPhaseNumberByEffectiveTicks(playerEffTime);
            player.sendSystemMessage(Component.translatable("awakening_system.banner.compensation", phaseNumber, activePhase.bannerTitle(), activePhase.franchise().displayName()));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            AwakeningSystemMod.sync(player);
            return true;
        }
        return false;
    }
}
