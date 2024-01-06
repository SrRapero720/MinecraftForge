/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.world;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class BiomeGenerationSettingsBuilder extends BiomeGenerationSettings.Builder
{
    public BiomeGenerationSettingsBuilder(BiomeGenerationSettings orig)
    {
        surfaceBuilder = Optional.of(orig.getSurfaceBuilder());
        orig.getCarvingStages().forEach(k -> carvers.put(k, new ArrayList<>(orig.getCarvers(k))));
        orig.features().forEach(l -> features.add(new ArrayList<>(l)));
        structureStarts.addAll(orig.structures());
    }

    public List<Supplier<ConfiguredFeature<?, ?>>> getFeatures(GenerationStep.Decoration stage) {
        addFeatureStepsUpTo(stage.ordinal());
        return features.get(stage.ordinal());
    }

    public Optional<Supplier<ConfiguredSurfaceBuilder<?>>> getSurfaceBuilder() {
        return surfaceBuilder;
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving stage) {
        return carvers.computeIfAbsent(stage, key -> new ArrayList<>());
    }

    public List<Supplier<ConfiguredStructureFeature<?, ?>>> getStructures() {
        return structureStarts;
    }
}