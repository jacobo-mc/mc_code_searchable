package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class NopeSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public NopeSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75102_) {
      super(p_75102_);
   }

   public void apply(Random pRandom, ChunkAccess pChunk, Biome pBiome, int pX, int pZ, int pHeight, double pNoise, BlockState pDefaultBlock, BlockState pDefaultFluid, int pSeaLevel, int pMinSurfaceLevel, long pSeed, SurfaceBuilderBaseConfiguration pConfig) {
   }
}