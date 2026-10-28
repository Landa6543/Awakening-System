package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

public final class CreateCompatHandler {
    private static final String CREATE_MOD = "create";
    private static final String AERONAUTICS_MOD = "create_aeronautics";
    private static final String AERONAUTICS_ALT_MOD = "createaeronautics";

    private CreateCompatHandler() {}

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MOD);
    }

    public static boolean isAeronauticsLoaded() {
        return ModList.get().isLoaded(AERONAUTICS_MOD) || ModList.get().isLoaded(AERONAUTICS_ALT_MOD);
    }

    /**
     * Se llama cuando un jugador monta o interactúa con una entidad para detectar trenes o aeronaves.
     */
    public static void onEntityMount(ServerPlayer player, Entity vehicle) {
        if (player == null || vehicle == null) return;

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        String path = typeId.getPath().toLowerCase(java.util.Locale.ROOT);
        String namespace = typeId.getNamespace().toLowerCase(java.util.Locale.ROOT);
        String className = vehicle.getClass().getName().toLowerCase(java.util.Locale.ROOT);

        boolean isTrain = namespace.contains("create") && (path.contains("carriage") || path.contains("train") || path.contains("contraption"))
                || className.contains("carriagecontraption") || className.contains("train");

        if (isTrain) {
            if (!SystemEvents.grantFable(player, "first_create_train")) {
                SystemEvents.grantFable(player, "steam_colossus");
            }
        }

        boolean isAirship = (namespace.contains("aeronautics") || namespace.contains("spaceeye") || path.contains("airship") || path.contains("aircraft") || path.contains("ship"))
                || className.contains("aeronautics") || className.contains("airship") || className.contains("shipcontraption");

        if (isAirship) {
            if (!SystemEvents.grantFable(player, "first_create_aircraft")) {
                SystemEvents.grantFable(player, "sky_dreadnought_commander");
            }
        }
    }
}
