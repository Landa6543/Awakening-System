package com.yungnickyoung.minecraft.bettercaves.worldgen.liquidregion;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class LiquidRegionsController {
    public static @Nullable LiquidRegionsController INSTANCE;
    public static LiquidRegionsController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LiquidRegionsController();
            useDefaultSettings();
        }
        return INSTANCE;
    }

    public static void useDefaultSettings() {
        LiquidRegionsController controller = new LiquidRegionsController();
        controller.settingsByDimensionId.put(ResourceLocation.withDefaultNamespace("overworld"), LiquidRegions.Settings.DEFAULT.copy());
        controller.settingsByDimensionId.put(ResourceLocation.fromNamespaceAndPath("awakening_system", "teyvat_world"), LiquidRegions.Settings.DEFAULT.copy());
        controller.settingsByDimensionId.put(ResourceLocation.fromNamespaceAndPath("awakening_system", "isekai_world"), LiquidRegions.Settings.DEFAULT.copy());
        INSTANCE = controller;
    }

    @SerializedName("liquidRegions")
    private final Map<ResourceLocation, LiquidRegions.Settings> settingsByDimensionId = new ConcurrentHashMap<>();

    public boolean hasSettingsForDimension(ResourceLocation dimensionId) {
        return this.settingsByDimensionId.containsKey(dimensionId);
    }

    public LiquidRegions.Settings getSettingsForDimension(ResourceLocation dimensionId) {
        return this.settingsByDimensionId.get(dimensionId);
    }

    public LiquidRegions.@Nullable Settings getSettings() {
        var context = LiquidRegionsContext.get();
        if (context == null) {
            return null;
        } else {
            return context.settings();
        }
    }

    @FunctionalInterface
    public interface CallableOp<R, X extends Throwable> {
        R call() throws X;
    }

    public <R, X extends Throwable> R withSettings(final ServerLevel level, final CallableOp<R, X> callable) throws X {
        var dimensionId = level.dimension().location();
        if (!this.hasSettingsForDimension(dimensionId)) {
            return callable.call();
        } else {
            return LiquidRegionsContext.call(this.settingsByDimensionId.get(dimensionId), callable);
        }
    }

    private static record LiquidRegionsContext(LiquidRegions.Settings settings) {
        private static final ThreadLocal<LiquidRegionsContext> CONTEXT = new ThreadLocal<>();

        public static <R, X extends Throwable> R call(LiquidRegions.Settings settings, CallableOp<R, X> callable) throws X {
            CONTEXT.set(new LiquidRegionsContext(settings));
            try {
                return callable.call();
            } finally {
                CONTEXT.remove();
            }
        }

        public static LiquidRegionsController.@Nullable LiquidRegionsContext get() {
            return CONTEXT.get();
        }
    }
}
