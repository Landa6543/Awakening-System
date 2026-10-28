package com.soybe.awakeningsystem.util;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class AttributeLimitFixer {
    private static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.warn("AttributeLimitFixer: Could not obtain Unsafe instance: {}", t.getMessage());
        }
    }

    private AttributeLimitFixer() {}

    public static void fixLimits() {
        try {
            expandLimit(Attributes.MAX_HEALTH, 1_000_000.0D);
            expandLimit(Attributes.ARMOR, 10_000.0D);
            expandLimit(Attributes.ARMOR_TOUGHNESS, 10_000.0D);
            expandLimit(Attributes.ATTACK_DAMAGE, 100_000.0D);
            expandLimit(Attributes.KNOCKBACK_RESISTANCE, 100.0D);
            AwakeningSystemMod.LOGGER.info("AttributeLimitFixer: Successfully updated attribute maximum limits.");
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("AttributeLimitFixer: Error expanding attribute limits: {}", t.getMessage(), t);
        }
    }

    private static void expandLimit(Holder<Attribute> holder, double newMax) {
        if (holder == null) return;
        Attribute attr = holder.value();
        if (!(attr instanceof RangedAttribute rangedAttr)) return;

        double oldMax = rangedAttr.getMaxValue();
        if (oldMax >= newMax) return;

        boolean modified = false;

        if (unsafe != null) {
            Class<?> c = rangedAttr.getClass();
            while (c != null && c != Object.class) {
                for (Field field : c.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (field.getType() == double.class) {
                        try {
                            long offset = unsafe.objectFieldOffset(field);
                            double currentVal = unsafe.getDouble(rangedAttr, offset);
                            if (currentVal == oldMax || field.getName().equalsIgnoreCase("maxValue") || field.getName().equals("f_22308_")) {
                                unsafe.putDouble(rangedAttr, offset, newMax);
                                modified = true;
                            }
                        } catch (Throwable t) {
                            AwakeningSystemMod.LOGGER.debug("AttributeLimitFixer unsafe patch failed for field {}: {}", field.getName(), t.getMessage());
                        }
                    }
                }
                c = c.getSuperclass();
            }
        }

        if (!modified || rangedAttr.getMaxValue() < newMax) {
            Class<?> c = rangedAttr.getClass();
            while (c != null && c != Object.class) {
                for (Field field : c.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (field.getType() == double.class) {
                        try {
                            field.setAccessible(true);
                            double val = field.getDouble(rangedAttr);
                            if (val == oldMax || field.getName().equalsIgnoreCase("maxValue")) {
                                field.setDouble(rangedAttr, newMax);
                                modified = true;
                            }
                        } catch (Throwable t) {
                            AwakeningSystemMod.LOGGER.debug("AttributeLimitFixer reflection patch failed for field {}: {}", field.getName(), t.getMessage());
                        }
                    }
                }
                c = c.getSuperclass();
            }
        }

        AwakeningSystemMod.LOGGER.info("AttributeLimitFixer: {} max value changed from {} to {}",
                attr.getDescriptionId(), oldMax, rangedAttr.getMaxValue());
    }
}
