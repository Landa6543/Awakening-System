package com.soybe.awakeningsystem.worldgen;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;

import java.util.function.Consumer;

public final class TerraBlenderCompat {
    private TerraBlenderCompat() {}

    public static void registerRegions() {
        try {
            Regions.register(new AwakeningOverworldRegion(
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_region"),
                    3
            ));
            AwakeningSystemMod.LOGGER.info("Awakening System: TerraBlender Overworld region registered successfully!");
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Awakening System: Failed to register TerraBlender regions", t);
        }
    }

    public static void registerSurfaceRules() {
        try {
            terrablender.api.SurfaceRuleManager.addSurfaceRules(
                    terrablender.api.SurfaceRuleManager.RuleCategory.OVERWORLD,
                    AwakeningSystemMod.MOD_ID,
                    makeSurfaceRules()
            );
            AwakeningSystemMod.LOGGER.info("Awakening System: TerraBlender Overworld surface rules registered successfully!");
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Awakening System: Failed to register TerraBlender surface rules", t);
        }
    }

    public static net.minecraft.world.level.levelgen.SurfaceRules.RuleSource makeSurfaceRules() {
        ResourceKey<Biome> astralDripstone = ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_astral_dripstone_chasm"));
        ResourceKey<Biome> penaltyDesert = ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "penalty_desert"));
        ResourceKey<Biome> penaltyDunes = ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "penalty_dunes"));
        ResourceKey<Biome> penaltyCanyon = ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "penalty_canyon"));

        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource sand = net.minecraft.world.level.levelgen.SurfaceRules.state(
                net.minecraft.world.level.block.Blocks.SAND.defaultBlockState());
        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource sandstone = net.minecraft.world.level.levelgen.SurfaceRules.state(
                net.minecraft.world.level.block.Blocks.SANDSTONE.defaultBlockState());
        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource redSand = net.minecraft.world.level.levelgen.SurfaceRules.state(
                net.minecraft.world.level.block.Blocks.RED_SAND.defaultBlockState());
        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource redSandstone = net.minecraft.world.level.levelgen.SurfaceRules.state(
                net.minecraft.world.level.block.Blocks.RED_SANDSTONE.defaultBlockState());

        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource yellowDesert = net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.ON_FLOOR, sand),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.UNDER_FLOOR, sandstone),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.DEEP_UNDER_FLOOR, sandstone)
        );

        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource canyonDesert = net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.ON_FLOOR, redSand),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.UNDER_FLOOR, redSandstone),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(net.minecraft.world.level.levelgen.SurfaceRules.DEEP_UNDER_FLOOR, redSandstone)
        );

        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource abyssalStone = net.minecraft.world.level.levelgen.SurfaceRules.state(
                com.soybe.awakeningsystem.block.SystemBlocks.ABYSSAL_STONE.get().defaultBlockState());
        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource abyssalGrowth = net.minecraft.world.level.levelgen.SurfaceRules.state(
                com.soybe.awakeningsystem.block.SystemBlocks.ABYSSAL_GROWTH.get().defaultBlockState());

        net.minecraft.world.level.levelgen.SurfaceRules.RuleSource caveRules = net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.ON_CEILING,
                        abyssalStone
                ),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.UNDER_CEILING,
                        abyssalStone
                ),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.ON_FLOOR,
                        abyssalGrowth
                ),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.UNDER_FLOOR,
                        abyssalStone
                ),
                abyssalStone
        );

        // Retorna reglas de desierto para la zona de castigo y reglas abisales para el chasm
        return net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.isBiome(penaltyCanyon),
                        canyonDesert
                ),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.isBiome(penaltyDesert, penaltyDunes),
                        yellowDesert
                ),
                net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                        net.minecraft.world.level.levelgen.SurfaceRules.isBiome(astralDripstone),
                        net.minecraft.world.level.levelgen.SurfaceRules.ifTrue(
                                net.minecraft.world.level.levelgen.SurfaceRules.not(
                                        net.minecraft.world.level.levelgen.SurfaceRules.yBlockCheck(
                                                net.minecraft.world.level.levelgen.VerticalAnchor.absolute(-10), 0
                                        )
                                ),
                                caveRules
                        )
                )
        );
    }

    public static class AwakeningOverworldRegion extends Region {
        public AwakeningOverworldRegion(ResourceLocation name, int weight) {
            super(name, RegionType.OVERWORLD, weight);
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
                ResourceKey<Biome> mysticBlossom = ResourceKey.create(Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_mystic_blossom_valley"));
                ResourceKey<Biome> spiritGrove = ResourceKey.create(Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_spirit_sacred_grove"));
                ResourceKey<Biome> astralDripstone = ResourceKey.create(Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_astral_dripstone_chasm"));

                builder.replaceBiome(Biomes.FLOWER_FOREST, mysticBlossom);
                builder.replaceBiome(Biomes.MEADOW, mysticBlossom);
                builder.replaceBiome(Biomes.CHERRY_GROVE, mysticBlossom);

                builder.replaceBiome(Biomes.GROVE, spiritGrove);
                builder.replaceBiome(Biomes.DARK_FOREST, spiritGrove);
                builder.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, spiritGrove);

                // Solo reemplazar dripstone caves de forma equilibrada, no lush caves
                builder.replaceBiome(Biomes.DRIPSTONE_CAVES, astralDripstone);
            });
        }
    }
}
