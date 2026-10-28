package com.soybe.awakeningsystem.casino.pool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
public class PoolBall {
    public static final float RADIUS = 0.052F;

    private final int id;
    public float x;
    public float z;
    public float vx;
    public float vz;
    public boolean pocketed;
    public int pocketIndex = -1;

    public PoolBall(int id, float x, float z) {
        this.id = id;
        this.x = x;
        this.z = z;
        this.vx = 0.0F;
        this.vz = 0.0F;
        this.pocketed = false;
    }

    public int getId() {
        return id;
    }

    public boolean isCueBall() {
        return id == 0;
    }

    public boolean isEightBall() {
        return id == 8;
    }

    public boolean isSolid() {
        return id >= 1 && id <= 7;
    }

    public boolean isStripe() {
        return id >= 9 && id <= 15;
    }

    public boolean isMoving() {
        return !pocketed && (Math.abs(vx) > 0.0005F || Math.abs(vz) > 0.0005F);
    }

    public float getSpeed() {
        return (float) Math.sqrt(vx * vx + vz * vz);
    }

    public String getDisplayName() {
        if (id == 0) return "Bola Blanca";
        if (id == 8) return "Bola 8 (Negra)";
        String type = isSolid() ? "Lisa" : "Rayada";
        return "Bola " + id + " (" + type + ")";
    }

    /**
     * Color primario de la bola según las normas canónicas de la WPA (World Pool-Billiard Association).
     */
    public int getColorRGB() {
        int baseId = id > 8 ? id - 8 : id;
        return switch (baseId) {
            case 0 -> 0xFFFFFF;
            case 1 -> 0xFFD700;
            case 2 -> 0x1E88E5;
            case 3 -> 0xE53935;
            case 4 -> 0x8E24AA;
            case 5 -> 0xFB8C00;
            case 6 -> 0x2E7D32;
            case 7 -> 0x5D4037;
            case 8 -> 0x111111;
            default -> 0xCCCCCC;
        };
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeByte(id);
        buf.writeFloat(x);
        buf.writeFloat(z);
        buf.writeFloat(vx);
        buf.writeFloat(vz);
        buf.writeBoolean(pocketed);
        buf.writeByte(pocketIndex);
    }

    public static PoolBall readFromBuf(FriendlyByteBuf buf) {
        int id = buf.readByte();
        float x = buf.readFloat();
        float z = buf.readFloat();
        PoolBall ball = new PoolBall(id, x, z);
        ball.vx = buf.readFloat();
        ball.vz = buf.readFloat();
        ball.pocketed = buf.readBoolean();
        ball.pocketIndex = buf.readByte();
        return ball;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putFloat("x", x);
        tag.putFloat("z", z);
        tag.putFloat("vx", vx);
        tag.putFloat("vz", vz);
        tag.putBoolean("pocketed", pocketed);
        tag.putInt("pocketIndex", pocketIndex);
        return tag;
    }

    public void readTag(CompoundTag tag) {
        this.x = tag.getFloat("x");
        this.z = tag.getFloat("z");
        this.vx = tag.getFloat("vx");
        this.vz = tag.getFloat("vz");
        this.pocketed = tag.getBoolean("pocketed");
        this.pocketIndex = tag.getInt("pocketIndex");
    }
}
