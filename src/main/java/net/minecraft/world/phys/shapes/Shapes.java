package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class Shapes {
   public static final double EPSILON = 1.0E-7D;
   public static final double BIG_EPSILON = 1.0E-6D;
   private static final VoxelShape BLOCK = Util.make(() -> {
      DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(1, 1, 1);
      discretevoxelshape.fill(0, 0, 0);
      return new CubeVoxelShape(discretevoxelshape);
   });
   public static final VoxelShape INFINITY = box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   private static final VoxelShape EMPTY = new ArrayVoxelShape(new BitSetDiscreteVoxelShape(0, 0, 0), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})));

   public static VoxelShape empty() {
      return EMPTY;
   }

   public static VoxelShape block() {
      return BLOCK;
   }

   public static VoxelShape box(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ) {
      if (!(pMinX > pMaxX) && !(pMinY > pMaxY) && !(pMinZ > pMaxZ)) {
         return create(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ);
      } else {
         throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
      }
   }

   public static VoxelShape create(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ) {
      if (!(pMaxX - pMinX < 1.0E-7D) && !(pMaxY - pMinY < 1.0E-7D) && !(pMaxZ - pMinZ < 1.0E-7D)) {
         int i = findBits(pMinX, pMaxX);
         int j = findBits(pMinY, pMaxY);
         int k = findBits(pMinZ, pMaxZ);
         if (i >= 0 && j >= 0 && k >= 0) {
            if (i == 0 && j == 0 && k == 0) {
               return block();
            } else {
               int l = 1 << i;
               int i1 = 1 << j;
               int j1 = 1 << k;
               BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.withFilledBounds(l, i1, j1, (int)Math.round(pMinX * (double)l), (int)Math.round(pMinY * (double)i1), (int)Math.round(pMinZ * (double)j1), (int)Math.round(pMaxX * (double)l), (int)Math.round(pMaxY * (double)i1), (int)Math.round(pMaxZ * (double)j1));
               return new CubeVoxelShape(bitsetdiscretevoxelshape);
            }
         } else {
            return new ArrayVoxelShape(BLOCK.shape, (DoubleList)DoubleArrayList.wrap(new double[]{pMinX, pMaxX}), (DoubleList)DoubleArrayList.wrap(new double[]{pMinY, pMaxY}), (DoubleList)DoubleArrayList.wrap(new double[]{pMinZ, pMaxZ}));
         }
      } else {
         return empty();
      }
   }

   public static VoxelShape create(AABB pAabb) {
      return create(pAabb.minX, pAabb.minY, pAabb.minZ, pAabb.maxX, pAabb.maxY, pAabb.maxZ);
   }

   @VisibleForTesting
   protected static int findBits(double pMinBits, double pMaxBits) {
      if (!(pMinBits < -1.0E-7D) && !(pMaxBits > 1.0000001D)) {
         for(int i = 0; i <= 3; ++i) {
            int j = 1 << i;
            double d0 = pMinBits * (double)j;
            double d1 = pMaxBits * (double)j;
            boolean flag = Math.abs(d0 - (double)Math.round(d0)) < 1.0E-7D * (double)j;
            boolean flag1 = Math.abs(d1 - (double)Math.round(d1)) < 1.0E-7D * (double)j;
            if (flag && flag1) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   protected static long lcm(int pAa, int pBb) {
      return (long)pAa * (long)(pBb / IntMath.gcd(pAa, pBb));
   }

   public static VoxelShape or(VoxelShape p_83111_, VoxelShape p_83112_) {
      return join(p_83111_, p_83112_, BooleanOp.OR);
   }

   public static VoxelShape or(VoxelShape p_83125_, VoxelShape... p_83126_) {
      return Arrays.stream(p_83126_).reduce(p_83125_, Shapes::or);
   }

   public static VoxelShape join(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pFunction) {
      return joinUnoptimized(pShape1, pShape2, pFunction).optimize();
   }

   public static VoxelShape joinUnoptimized(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pFunction) {
      if (pFunction.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (pShape1 == pShape2) {
         return pFunction.apply(true, true) ? pShape1 : empty();
      } else {
         boolean flag = pFunction.apply(true, false);
         boolean flag1 = pFunction.apply(false, true);
         if (pShape1.isEmpty()) {
            return flag1 ? pShape2 : empty();
         } else if (pShape2.isEmpty()) {
            return flag ? pShape1 : empty();
         } else {
            IndexMerger indexmerger = createIndexMerger(1, pShape1.getCoords(Direction.Axis.X), pShape2.getCoords(Direction.Axis.X), flag, flag1);
            IndexMerger indexmerger1 = createIndexMerger(indexmerger.size() - 1, pShape1.getCoords(Direction.Axis.Y), pShape2.getCoords(Direction.Axis.Y), flag, flag1);
            IndexMerger indexmerger2 = createIndexMerger((indexmerger.size() - 1) * (indexmerger1.size() - 1), pShape1.getCoords(Direction.Axis.Z), pShape2.getCoords(Direction.Axis.Z), flag, flag1);
            BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.join(pShape1.shape, pShape2.shape, indexmerger, indexmerger1, indexmerger2, pFunction);
            return (VoxelShape)(indexmerger instanceof DiscreteCubeMerger && indexmerger1 instanceof DiscreteCubeMerger && indexmerger2 instanceof DiscreteCubeMerger ? new CubeVoxelShape(bitsetdiscretevoxelshape) : new ArrayVoxelShape(bitsetdiscretevoxelshape, indexmerger.getList(), indexmerger1.getList(), indexmerger2.getList()));
         }
      }
   }

   public static boolean joinIsNotEmpty(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pResultOperator) {
      if (pResultOperator.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else {
         boolean flag = pShape1.isEmpty();
         boolean flag1 = pShape2.isEmpty();
         if (!flag && !flag1) {
            if (pShape1 == pShape2) {
               return pResultOperator.apply(true, true);
            } else {
               boolean flag2 = pResultOperator.apply(true, false);
               boolean flag3 = pResultOperator.apply(false, true);

               for(Direction.Axis direction$axis : AxisCycle.AXIS_VALUES) {
                  if (pShape1.max(direction$axis) < pShape2.min(direction$axis) - 1.0E-7D) {
                     return flag2 || flag3;
                  }

                  if (pShape2.max(direction$axis) < pShape1.min(direction$axis) - 1.0E-7D) {
                     return flag2 || flag3;
                  }
               }

               IndexMerger indexmerger = createIndexMerger(1, pShape1.getCoords(Direction.Axis.X), pShape2.getCoords(Direction.Axis.X), flag2, flag3);
               IndexMerger indexmerger1 = createIndexMerger(indexmerger.size() - 1, pShape1.getCoords(Direction.Axis.Y), pShape2.getCoords(Direction.Axis.Y), flag2, flag3);
               IndexMerger indexmerger2 = createIndexMerger((indexmerger.size() - 1) * (indexmerger1.size() - 1), pShape1.getCoords(Direction.Axis.Z), pShape2.getCoords(Direction.Axis.Z), flag2, flag3);
               return joinIsNotEmpty(indexmerger, indexmerger1, indexmerger2, pShape1.shape, pShape2.shape, pResultOperator);
            }
         } else {
            return pResultOperator.apply(!flag, !flag1);
         }
      }
   }

   private static boolean joinIsNotEmpty(IndexMerger pMergerX, IndexMerger pMergerY, IndexMerger pMergerZ, DiscreteVoxelShape pPrimaryShape, DiscreteVoxelShape pSecondaryShape, BooleanOp pResultOperator) {
      return !pMergerX.forMergedIndexes((p_83100_, p_83101_, p_83102_) -> {
         return pMergerY.forMergedIndexes((p_166046_, p_166047_, p_166048_) -> {
            return pMergerZ.forMergedIndexes((p_166036_, p_166037_, p_166038_) -> {
               return !pResultOperator.apply(pPrimaryShape.isFullWide(p_83100_, p_166046_, p_166036_), pSecondaryShape.isFullWide(p_83101_, p_166047_, p_166037_));
            });
         });
      });
   }

   public static double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, Stream<VoxelShape> pPossibleHits, double pDesiredOffset) {
      for(Iterator<VoxelShape> iterator = pPossibleHits.iterator(); iterator.hasNext(); pDesiredOffset = iterator.next().collide(pMovementAxis, pCollisionBox, pDesiredOffset)) {
         if (Math.abs(pDesiredOffset) < 1.0E-7D) {
            return 0.0D;
         }
      }

      return pDesiredOffset;
   }

   public static double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, LevelReader pLevelReader, double pDesiredOffset, CollisionContext pSelectionContext, Stream<VoxelShape> pPossibleHits) {
      return collide(pCollisionBox, pLevelReader, pDesiredOffset, pSelectionContext, AxisCycle.between(pMovementAxis, Direction.Axis.Z), pPossibleHits);
   }

   private static double collide(AABB pCollisionBox, LevelReader pLevelReader, double pDesiredOffset, CollisionContext pSelectionContext, AxisCycle pRotationAxis, Stream<VoxelShape> pPossibleHits) {
      if (!(pCollisionBox.getXsize() < 1.0E-6D) && !(pCollisionBox.getYsize() < 1.0E-6D) && !(pCollisionBox.getZsize() < 1.0E-6D)) {
         if (Math.abs(pDesiredOffset) < 1.0E-7D) {
            return 0.0D;
         } else {
            AxisCycle axiscycle = pRotationAxis.inverse();
            Direction.Axis direction$axis = axiscycle.cycle(Direction.Axis.X);
            Direction.Axis direction$axis1 = axiscycle.cycle(Direction.Axis.Y);
            Direction.Axis direction$axis2 = axiscycle.cycle(Direction.Axis.Z);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            int i = Mth.floor(pCollisionBox.min(direction$axis) - 1.0E-7D) - 1;
            int j = Mth.floor(pCollisionBox.max(direction$axis) + 1.0E-7D) + 1;
            int k = Mth.floor(pCollisionBox.min(direction$axis1) - 1.0E-7D) - 1;
            int l = Mth.floor(pCollisionBox.max(direction$axis1) + 1.0E-7D) + 1;
            double d0 = pCollisionBox.min(direction$axis2) - 1.0E-7D;
            double d1 = pCollisionBox.max(direction$axis2) + 1.0E-7D;
            boolean flag = pDesiredOffset > 0.0D;
            int i1 = flag ? Mth.floor(pCollisionBox.max(direction$axis2) - 1.0E-7D) - 1 : Mth.floor(pCollisionBox.min(direction$axis2) + 1.0E-7D) + 1;
            int j1 = lastC(pDesiredOffset, d0, d1);
            int k1 = flag ? 1 : -1;
            int l1 = i1;

            while(true) {
               if (flag) {
                  if (l1 > j1) {
                     break;
                  }
               } else if (l1 < j1) {
                  break;
               }

               for(int i2 = i; i2 <= j; ++i2) {
                  for(int j2 = k; j2 <= l; ++j2) {
                     int k2 = 0;
                     if (i2 == i || i2 == j) {
                        ++k2;
                     }

                     if (j2 == k || j2 == l) {
                        ++k2;
                     }

                     if (l1 == i1 || l1 == j1) {
                        ++k2;
                     }

                     if (k2 < 3) {
                        blockpos$mutableblockpos.set(axiscycle, i2, j2, l1);
                        BlockState blockstate = pLevelReader.getBlockState(blockpos$mutableblockpos);
                        if ((k2 != 1 || blockstate.hasLargeCollisionShape()) && (k2 != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                           pDesiredOffset = blockstate.getCollisionShape(pLevelReader, blockpos$mutableblockpos, pSelectionContext).collide(direction$axis2, pCollisionBox.move((double)(-blockpos$mutableblockpos.getX()), (double)(-blockpos$mutableblockpos.getY()), (double)(-blockpos$mutableblockpos.getZ())), pDesiredOffset);
                           if (Math.abs(pDesiredOffset) < 1.0E-7D) {
                              return 0.0D;
                           }

                           j1 = lastC(pDesiredOffset, d0, d1);
                        }
                     }
                  }
               }

               l1 += k1;
            }

            double[] adouble = new double[]{pDesiredOffset};
            pPossibleHits.forEach((p_83143_) -> {
               adouble[0] = p_83143_.collide(direction$axis2, pCollisionBox, adouble[0]);
            });
            return adouble[0];
         }
      } else {
         return pDesiredOffset;
      }
   }

   private static int lastC(double pDesiredOffset, double pMin, double pMax) {
      return pDesiredOffset > 0.0D ? Mth.floor(pMax + pDesiredOffset) + 1 : Mth.floor(pMin + pDesiredOffset) - 1;
   }

   public static boolean blockOccudes(VoxelShape pShape, VoxelShape pAdjacentShape, Direction pSide) {
      if (pShape == block() && pAdjacentShape == block()) {
         return true;
      } else if (pAdjacentShape.isEmpty()) {
         return false;
      } else {
         Direction.Axis direction$axis = pSide.getAxis();
         Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pShape : pAdjacentShape;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pAdjacentShape : pShape;
         BooleanOp booleanop = direction$axisdirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
         return DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D) && !joinIsNotEmpty(new SliceShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SliceShape(voxelshape1, direction$axis, 0), booleanop);
      }
   }

   public static VoxelShape getFaceShape(VoxelShape pVoxelShape, Direction pDirection) {
      if (pVoxelShape == block()) {
         return block();
      } else {
         Direction.Axis direction$axis = pDirection.getAxis();
         boolean flag;
         int i;
         if (pDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            flag = DoubleMath.fuzzyEquals(pVoxelShape.max(direction$axis), 1.0D, 1.0E-7D);
            i = pVoxelShape.shape.getSize(direction$axis) - 1;
         } else {
            flag = DoubleMath.fuzzyEquals(pVoxelShape.min(direction$axis), 0.0D, 1.0E-7D);
            i = 0;
         }

         return (VoxelShape)(!flag ? empty() : new SliceShape(pVoxelShape, direction$axis, i));
      }
   }

   public static boolean mergedFaceOccludes(VoxelShape pShape, VoxelShape pAdjacentShape, Direction pSide) {
      if (pShape != block() && pAdjacentShape != block()) {
         Direction.Axis direction$axis = pSide.getAxis();
         Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pShape : pAdjacentShape;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pAdjacentShape : pShape;
         if (!DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D)) {
            voxelshape = empty();
         }

         if (!DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D)) {
            voxelshape1 = empty();
         }

         return !joinIsNotEmpty(block(), joinUnoptimized(new SliceShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SliceShape(voxelshape1, direction$axis, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST);
      } else {
         return true;
      }
   }

   public static boolean faceShapeOccludes(VoxelShape pVoxelShape1, VoxelShape pVoxelShape2) {
      if (pVoxelShape1 != block() && pVoxelShape2 != block()) {
         if (pVoxelShape1.isEmpty() && pVoxelShape2.isEmpty()) {
            return false;
         } else {
            return !joinIsNotEmpty(block(), joinUnoptimized(pVoxelShape1, pVoxelShape2, BooleanOp.OR), BooleanOp.ONLY_FIRST);
         }
      } else {
         return true;
      }
   }

   @VisibleForTesting
   protected static IndexMerger createIndexMerger(int p_83059_, DoubleList p_83060_, DoubleList p_83061_, boolean p_83062_, boolean p_83063_) {
      int i = p_83060_.size() - 1;
      int j = p_83061_.size() - 1;
      if (p_83060_ instanceof CubePointRange && p_83061_ instanceof CubePointRange) {
         long k = lcm(i, j);
         if ((long)p_83059_ * k <= 256L) {
            return new DiscreteCubeMerger(i, j);
         }
      }

      if (p_83060_.getDouble(i) < p_83061_.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(p_83060_, p_83061_, false);
      } else if (p_83061_.getDouble(j) < p_83060_.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(p_83061_, p_83060_, true);
      } else {
         return (IndexMerger)(i == j && Objects.equals(p_83060_, p_83061_) ? new IdenticalMerger(p_83060_) : new IndirectMerger(p_83060_, p_83061_, p_83062_, p_83063_));
      }
   }

   public interface DoubleLineConsumer {
      void consume(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ);
   }
}