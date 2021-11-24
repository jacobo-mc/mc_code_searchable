package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinSimplexNoise implements SurfaceNoise {
   private final SimplexNoise[] noiseLevels;
   private final double highestFreqValueFactor;
   private final double highestFreqInputFactor;

   public PerlinSimplexNoise(RandomSource pRandomSource, IntStream pOctaves) {
      this(pRandomSource, pOctaves.boxed().collect(ImmutableList.toImmutableList()));
   }

   public PerlinSimplexNoise(RandomSource pRandomSource, List<Integer> pOctaves) {
      this(pRandomSource, new IntRBTreeSet(pOctaves));
   }

   private PerlinSimplexNoise(RandomSource pRandomSource, IntSortedSet pOctaves) {
      if (pOctaves.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -pOctaves.firstInt();
         int j = pOctaves.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            SimplexNoise simplexnoise = new SimplexNoise(pRandomSource);
            int l = j;
            this.noiseLevels = new SimplexNoise[k];
            if (j >= 0 && j < k && pOctaves.contains(0)) {
               this.noiseLevels[j] = simplexnoise;
            }

            for(int i1 = j + 1; i1 < k; ++i1) {
               if (i1 >= 0 && pOctaves.contains(l - i1)) {
                  this.noiseLevels[i1] = new SimplexNoise(pRandomSource);
               } else {
                  pRandomSource.consumeCount(262);
               }
            }

            if (j > 0) {
               long k1 = (long)(simplexnoise.getValue(simplexnoise.xo, simplexnoise.yo, simplexnoise.zo) * (double)9.223372E18F);
               RandomSource randomsource = new WorldgenRandom(k1);

               for(int j1 = l - 1; j1 >= 0; --j1) {
                  if (j1 < k && pOctaves.contains(l - j1)) {
                     this.noiseLevels[j1] = new SimplexNoise(randomsource);
                  } else {
                     randomsource.consumeCount(262);
                  }
               }
            }

            this.highestFreqInputFactor = Math.pow(2.0D, (double)j);
            this.highestFreqValueFactor = 1.0D / (Math.pow(2.0D, (double)k) - 1.0D);
         }
      }
   }

   public double getValue(double pX, double pY, boolean pUseNoiseOffsets) {
      double d0 = 0.0D;
      double d1 = this.highestFreqInputFactor;
      double d2 = this.highestFreqValueFactor;

      for(SimplexNoise simplexnoise : this.noiseLevels) {
         if (simplexnoise != null) {
            d0 += simplexnoise.getValue(pX * d1 + (pUseNoiseOffsets ? simplexnoise.xo : 0.0D), pY * d1 + (pUseNoiseOffsets ? simplexnoise.yo : 0.0D)) * d2;
         }

         d1 /= 2.0D;
         d2 *= 2.0D;
      }

      return d0;
   }

   public double getSurfaceNoiseValue(double pX, double pY, double pZ, double pYMax) {
      return this.getValue(pX, pY, true) * 0.55D;
   }
}