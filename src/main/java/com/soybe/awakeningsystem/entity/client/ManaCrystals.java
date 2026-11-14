package com.soybe.awakeningsystem.entity.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * Mana crystals growing out of the mana animals. The animals keep their familiar vanilla shape; this takes
 * the vanilla mesh, lets each animal add its crystals, and doubles the texture height so the crystals have
 * their own space (rows 32-63) under the original skin.
 */
public final class ManaCrystals {
    private ManaCrystals() {}

    /** The vanilla mesh of {@code base}, with extra parts added, on a 64x64 texture. */
    public static LayerDefinition extend(LayerDefinition base, Consumer<PartDefinition> add) {
        try {
            Field f = LayerDefinition.class.getDeclaredField("mesh");
            f.setAccessible(true);
            MeshDefinition mesh = (MeshDefinition) f.get(base);
            add.accept(mesh.getRoot());
            return LayerDefinition.create(mesh, 64, 64);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not extend a vanilla animal model", e);
        }
    }

    private static int n;

    /**
     * A crystal shard standing on {@code parent} at (x, y, z), {@code h} pixels tall, pointing up and leaning
     * by rx / rz. Big shards also get a little companion growing from their foot.
     */
    public static void shard(PartDefinition parent, float x, float y, float z, int h, float rx, float rz) {
        boolean big = h >= 5;
        CubeListBuilder c = CubeListBuilder.create();
        if (big) c.texOffs(0, 32).addBox(-1.5F, -h, -1.5F, 3.0F, h, 3.0F);
        else c.texOffs(16, 32).addBox(-1.0F, -h, -1.0F, 2.0F, h, 2.0F);
        PartDefinition p = parent.addOrReplaceChild("mana_crystal_" + (n++), c, PartPose.offsetAndRotation(x, y, z, rx, 0.0F, rz));
        if (big) p.addOrReplaceChild("mana_crystal_" + (n++), CubeListBuilder.create().texOffs(28, 32).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 0.0F, 0.5F, 0.3F, 0.0F, -0.6F));
    }

    /**
     * A shard on the animal's back. The vanilla animal models only draw their known parts (head, body, legs),
     * never extra children of the root, so the shard hangs from the body. The body is turned 90 degrees on X
     * with its pivot at (0, bodyY, bodyZ); a mount undoes that turn, so x/y/z and the lean stay in the usual
     * upright model space.
     */
    public static void backShard(PartDefinition root, float bodyY, float bodyZ, float x, float y, float z, int h, float rx, float rz) {
        PartDefinition mount = root.getChild("body").addOrReplaceChild("mana_mount_" + (n++), CubeListBuilder.create(),
                PartPose.offsetAndRotation(x, z - bodyZ, -(y - bodyY), -(float) (Math.PI / 2), 0.0F, 0.0F));
        shard(mount, 0.0F, 0.0F, 0.0F, h, rx, rz);
    }

    /** A curling horn of crystal: three shrinking segments, each bending a little more. */
    public static void horn(PartDefinition parent, float x, float y, float z, int side) {
        PartDefinition a = parent.addOrReplaceChild("mana_horn_" + (n++), CubeListBuilder.create().texOffs(16, 32).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(x, y, z, -0.6F, 0.0F, side * 0.7F));
        PartDefinition b = a.addOrReplaceChild("mana_horn_" + (n++), CubeListBuilder.create().texOffs(16, 32).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.6F, 0.0F, -0.9F, 0.0F, 0.0F));
        b.addOrReplaceChild("mana_horn_" + (n++), CubeListBuilder.create().texOffs(28, 32).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.6F, 0.0F, -1.0F, 0.0F, 0.0F));
    }
}
