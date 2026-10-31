package com.yungnickyoung.minecraft.bettercaves.worldgen.context;

import com.yungnickyoung.minecraft.bettercaves.worldgen.liquidregion.LiquidRegions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record AquiferContext(@Nullable LiquidRegions liquidRegions) {
    private static final ThreadLocal<AquiferContext> CONTEXT = new ThreadLocal<>();

    @FunctionalInterface
    public interface CallableOp<R, X extends Throwable> {
        R call() throws X;
    }

    public static <R, X extends Throwable> R call(LiquidRegions liquidRegions, CallableOp<R, X> callable) throws X {
        CONTEXT.set(new AquiferContext(liquidRegions));
        try {
            return callable.call();
        } finally {
            CONTEXT.remove();
        }
    }

    public static <R, X extends Throwable> R callWithoutRegions(CallableOp<R, X> callable) throws X {
        CONTEXT.set(new AquiferContext(null));
        try {
            return callable.call();
        } finally {
            CONTEXT.remove();
        }
    }

    public static @Nullable AquiferContext get() {
        return CONTEXT.get();
    }
}
