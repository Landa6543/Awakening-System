package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Módulo de compatibilidad reflectiva segura con Curios API.
 * Permite guardar y restituir todos los accesorios equipados sin provocar
 * dependencias duras ni fallos de compilación si Curios no está instalado.
 */
public final class CuriosCompat {
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private CuriosCompat() {}

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    public static CompoundTag saveCurios(LivingEntity entity) {
        if (!CURIOS_LOADED || entity == null) {
            return new CompoundTag();
        }

        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInvMethod = apiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object opt = getInvMethod.invoke(null, entity);
            if (opt instanceof Optional<?> optional && optional.isPresent()) {
                Object handler = optional.get();
                // Many versions of Curios provide writeTag or serializeNBT on the handler
                Method writeTagMethod = null;
                for (Method m : handler.getClass().getMethods()) {
                    if (m.getName().equals("writeTag") || m.getName().equals("serializeNBT")) {
                        if (m.getParameterCount() == 0 && CompoundTag.class.isAssignableFrom(m.getReturnType())) {
                            writeTagMethod = m;
                            break;
                        }
                    }
                }
                if (writeTagMethod != null) {
                    return (CompoundTag) writeTagMethod.invoke(handler);
                }
            }
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.debug("CuriosCompat: no se pudo serializar curios: {}", t.getMessage());
        }
        return new CompoundTag();
    }

    public static void restoreCurios(LivingEntity entity, CompoundTag tag) {
        if (!CURIOS_LOADED || entity == null || tag == null || tag.isEmpty()) {
            return;
        }

        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInvMethod = apiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object opt = getInvMethod.invoke(null, entity);
            if (opt instanceof Optional<?> optional && optional.isPresent()) {
                Object handler = optional.get();
                for (Method m : handler.getClass().getMethods()) {
                    if (m.getName().equals("readTag") || m.getName().equals("deserializeNBT")) {
                        if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(CompoundTag.class)) {
                            m.invoke(handler, tag);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.debug("CuriosCompat: no se pudo deserializar curios: {}", t.getMessage());
        }
    }
}
