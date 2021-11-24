package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i {
   public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap((p_121967_) -> {
      return Util.fixedSize(p_121967_, 3).map((p_175270_) -> {
         return new BlockPos(p_175270_[0], p_175270_[1], p_175270_[2]);
      });
   }, (p_121924_) -> {
      return IntStream.of(p_121924_.getX(), p_121924_.getY(), p_121924_.getZ());
   }).stable();
   private static final Logger LOGGER = LogManager.getLogger();
   public static final BlockPos ZERO = new BlockPos(0, 0, 0);
   private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
   private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
   public static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
   private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
   private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
   private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
   private static final int Y_OFFSET = 0;
   private static final int Z_OFFSET = PACKED_Y_LENGTH;
   private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

   public BlockPos(int pX, int pY, int pZ) {
      super(pX, pY, pZ);
   }

   public BlockPos(double p_121865_, double p_121866_, double p_121867_) {
      super(p_121865_, p_121866_, p_121867_);
   }

   public BlockPos(Vec3 pVector) {
      this(pVector.x, pVector.y, pVector.z);
   }

   public BlockPos(Position pPos) {
      this(pPos.x(), pPos.y(), pPos.z());
   }

   public BlockPos(Vec3i pVector) {
      this(pVector.getX(), pVector.getY(), pVector.getZ());
   }

   public static long offset(long pPos, Direction pDirection) {
      return offset(pPos, pDirection.getStepX(), pDirection.getStepY(), pDirection.getStepZ());
   }

   public static long offset(long pPos, int pDx, int pDy, int pDz) {
      return asLong(getX(pPos) + pDx, getY(pPos) + pDy, getZ(pPos) + pDz);
   }

   public static int getX(long pPackedPos) {
      return (int)(pPackedPos << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
   }

   public static int getY(long pPackedPos) {
      return (int)(pPackedPos << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
   }

   public static int getZ(long pPackedPos) {
      return (int)(pPackedPos << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
   }

   public static BlockPos of(long pPackedPos) {
      return new BlockPos(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int pX, int pY, int pZ) {
      long i = 0L;
      i = i | ((long)pX & PACKED_X_MASK) << X_OFFSET;
      i = i | ((long)pY & PACKED_Y_MASK) << 0;
      return i | ((long)pZ & PACKED_Z_MASK) << Z_OFFSET;
   }

   public static long getFlatIndex(long pPackedPos) {
      return pPackedPos & -16L;
   }

   public BlockPos offset(double pDx, double pDy, double pDz) {
      return pDx == 0.0D && pDy == 0.0D && pDz == 0.0D ? this : new BlockPos((double)this.getX() + pDx, (double)this.getY() + pDy, (double)this.getZ() + pDz);
   }

   public BlockPos offset(int pDx, int pDy, int pDz) {
      return pDx == 0 && pDy == 0 && pDz == 0 ? this : new BlockPos(this.getX() + pDx, this.getY() + pDy, this.getZ() + pDz);
   }

   public BlockPos offset(Vec3i pVector) {
      return this.offset(pVector.getX(), pVector.getY(), pVector.getZ());
   }

   public BlockPos subtract(Vec3i pVector) {
      return this.offset(-pVector.getX(), -pVector.getY(), -pVector.getZ());
   }

   public BlockPos multiply(int pScalar) {
      if (pScalar == 1) {
         return this;
      } else {
         return pScalar == 0 ? ZERO : new BlockPos(this.getX() * pScalar, this.getY() * pScalar, this.getZ() * pScalar);
      }
   }

   public BlockPos above() {
      return this.relative(Direction.UP);
   }

   public BlockPos above(int pDistance) {
      return this.relative(Direction.UP, pDistance);
   }

   public BlockPos below() {
      return this.relative(Direction.DOWN);
   }

   public BlockPos below(int pDistance) {
      return this.relative(Direction.DOWN, pDistance);
   }

   public BlockPos north() {
      return this.relative(Direction.NORTH);
   }

   public BlockPos north(int pDistance) {
      return this.relative(Direction.NORTH, pDistance);
   }

   public BlockPos south() {
      return this.relative(Direction.SOUTH);
   }

   public BlockPos south(int pDistance) {
      return this.relative(Direction.SOUTH, pDistance);
   }

   public BlockPos west() {
      return this.relative(Direction.WEST);
   }

   public BlockPos west(int pDistance) {
      return this.relative(Direction.WEST, pDistance);
   }

   public BlockPos east() {
      return this.relative(Direction.EAST);
   }

   public BlockPos east(int pDistance) {
      return this.relative(Direction.EAST, pDistance);
   }

   public BlockPos relative(Direction pDirection) {
      return new BlockPos(this.getX() + pDirection.getStepX(), this.getY() + pDirection.getStepY(), this.getZ() + pDirection.getStepZ());
   }

   public BlockPos relative(Direction pDirection, int pDistance) {
      return pDistance == 0 ? this : new BlockPos(this.getX() + pDirection.getStepX() * pDistance, this.getY() + pDirection.getStepY() * pDistance, this.getZ() + pDirection.getStepZ() * pDistance);
   }

   public BlockPos relative(Direction.Axis pAxis, int pDistance) {
      if (pDistance == 0) {
         return this;
      } else {
         int i = pAxis == Direction.Axis.X ? pDistance : 0;
         int j = pAxis == Direction.Axis.Y ? pDistance : 0;
         int k = pAxis == Direction.Axis.Z ? pDistance : 0;
         return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
      }
   }

   public BlockPos rotate(Rotation pRotation) {
      switch(pRotation) {
      case NONE:
      default:
         return this;
      case CLOCKWISE_90:
         return new BlockPos(-this.getZ(), this.getY(), this.getX());
      case CLOCKWISE_180:
         return new BlockPos(-this.getX(), this.getY(), -this.getZ());
      case COUNTERCLOCKWISE_90:
         return new BlockPos(this.getZ(), this.getY(), -this.getX());
      }
   }

   public BlockPos cross(Vec3i pVector) {
      return new BlockPos(this.getY() * pVector.getZ() - this.getZ() * pVector.getY(), this.getZ() * pVector.getX() - this.getX() * pVector.getZ(), this.getX() * pVector.getY() - this.getY() * pVector.getX());
   }

   public BlockPos atY(int pY) {
      return new BlockPos(this.getX(), pY, this.getZ());
   }

   public BlockPos immutable() {
      return this;
   }

   public BlockPos.MutableBlockPos mutable() {
      return new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
   }

   public static Iterable<BlockPos> randomInCube(Random p_175265_, int p_175266_, BlockPos p_175267_, int p_175268_) {
      return randomBetweenClosed(p_175265_, p_175266_, p_175267_.getX() - p_175268_, p_175267_.getY() - p_175268_, p_175267_.getZ() - p_175268_, p_175267_.getX() + p_175268_, p_175267_.getY() + p_175268_, p_175267_.getZ() + p_175268_);
   }

   public static Iterable<BlockPos> randomBetweenClosed(Random pRandom, int pAmount, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      int i = pMaxX - pMinX + 1;
      int j = pMaxY - pMinY + 1;
      int k = pMaxZ - pMinZ + 1;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
            int counter = pAmount;

            protected BlockPos computeNext() {
               if (this.counter <= 0) {
                  return this.endOfData();
               } else {
                  BlockPos blockpos = this.nextPos.set(pMinX + pRandom.nextInt(i), pMinY + pRandom.nextInt(j), pMinZ + pRandom.nextInt(k));
                  --this.counter;
                  return blockpos;
               }
            }
         };
      };
   }

   public static Iterable<BlockPos> withinManhattan(BlockPos pPos, int pXSize, int pYSize, int pZSize) {
      int i = pXSize + pYSize + pZSize;
      int j = pPos.getX();
      int k = pPos.getY();
      int l = pPos.getZ();
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
               if (this.zMirror) {
                  this.zMirror = false;
                  this.cursor.setZ(l - (this.cursor.getZ() - l));
                  return this.cursor;
               } else {
                  BlockPos blockpos;
                  for(blockpos = null; blockpos == null; ++this.y) {
                     if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                           ++this.currentDepth;
                           if (this.currentDepth > i) {
                              return this.endOfData();
                           }

                           this.maxX = Math.min(pXSize, this.currentDepth);
                           this.x = -this.maxX;
                        }

                        this.maxY = Math.min(pYSize, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                     }

                     int i1 = this.x;
                     int j1 = this.y;
                     int k1 = this.currentDepth - Math.abs(i1) - Math.abs(j1);
                     if (k1 <= pZSize) {
                        this.zMirror = k1 != 0;
                        blockpos = this.cursor.set(j + i1, k + j1, l + k1);
                     }
                  }

                  return blockpos;
               }
            }
         };
      };
   }

   public static Optional<BlockPos> findClosestMatch(BlockPos pPos, int pWidth, int pHeight, Predicate<BlockPos> pPosFilter) {
      return withinManhattanStream(pPos, pWidth, pHeight, pWidth).filter(pPosFilter).findFirst();
   }

   public static Stream<BlockPos> withinManhattanStream(BlockPos pPos, int pXSize, int pYSize, int pZSize) {
      return StreamSupport.stream(withinManhattan(pPos, pXSize, pYSize, pZSize).spliterator(), false);
   }

   public static Iterable<BlockPos> betweenClosed(BlockPos pFirstPos, BlockPos pSecondPos) {
      return betweenClosed(Math.min(pFirstPos.getX(), pSecondPos.getX()), Math.min(pFirstPos.getY(), pSecondPos.getY()), Math.min(pFirstPos.getZ(), pSecondPos.getZ()), Math.max(pFirstPos.getX(), pSecondPos.getX()), Math.max(pFirstPos.getY(), pSecondPos.getY()), Math.max(pFirstPos.getZ(), pSecondPos.getZ()));
   }

   public static Stream<BlockPos> betweenClosedStream(BlockPos pFirstPos, BlockPos pSecondPos) {
      return StreamSupport.stream(betweenClosed(pFirstPos, pSecondPos).spliterator(), false);
   }

   public static Stream<BlockPos> betweenClosedStream(BoundingBox pBox) {
      return betweenClosedStream(Math.min(pBox.minX(), pBox.maxX()), Math.min(pBox.minY(), pBox.maxY()), Math.min(pBox.minZ(), pBox.maxZ()), Math.max(pBox.minX(), pBox.maxX()), Math.max(pBox.minY(), pBox.maxY()), Math.max(pBox.minZ(), pBox.maxZ()));
   }

   public static Stream<BlockPos> betweenClosedStream(AABB pAabb) {
      return betweenClosedStream(Mth.floor(pAabb.minX), Mth.floor(pAabb.minY), Mth.floor(pAabb.minZ), Mth.floor(pAabb.maxX), Mth.floor(pAabb.maxY), Mth.floor(pAabb.maxZ));
   }

   public static Stream<BlockPos> betweenClosedStream(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      return StreamSupport.stream(betweenClosed(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ).spliterator(), false);
   }

   public static Iterable<BlockPos> betweenClosed(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2) {
      int i = pX2 - pX1 + 1;
      int j = pY2 - pY1 + 1;
      int k = pZ2 - pZ1 + 1;
      int l = i * j * k;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
               if (this.index == l) {
                  return this.endOfData();
               } else {
                  int i1 = this.index % i;
                  int j1 = this.index / i;
                  int k1 = j1 % j;
                  int l1 = j1 / j;
                  ++this.index;
                  return this.cursor.set(pX1 + i1, pY1 + k1, pZ1 + l1);
               }
            }
         };
      };
   }

   public static Iterable<BlockPos.MutableBlockPos> spiralAround(BlockPos p_121936_, int p_121937_, Direction p_121938_, Direction p_121939_) {
      Validate.validState(p_121938_.getAxis() != p_121939_.getAxis(), "The two directions cannot be on the same axis");
      return () -> {
         return new AbstractIterator<BlockPos.MutableBlockPos>() {
            private final Direction[] directions = new Direction[]{p_121938_, p_121939_, p_121938_.getOpposite(), p_121939_.getOpposite()};
            private final BlockPos.MutableBlockPos cursor = p_121936_.mutable().move(p_121939_);
            private final int legs = 4 * p_121937_;
            private int leg = -1;
            private int legSize;
            private int legIndex;
            private int lastX = this.cursor.getX();
            private int lastY = this.cursor.getY();
            private int lastZ = this.cursor.getZ();

            protected BlockPos.MutableBlockPos computeNext() {
               this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
               this.lastX = this.cursor.getX();
               this.lastY = this.cursor.getY();
               this.lastZ = this.cursor.getZ();
               if (this.legIndex >= this.legSize) {
                  if (this.leg >= this.legs) {
                     return this.endOfData();
                  }

                  ++this.leg;
                  this.legIndex = 0;
                  this.legSize = this.leg / 2 + 1;
               }

               ++this.legIndex;
               return this.cursor;
            }
         };
      };
   }

   public static class MutableBlockPos extends BlockPos {
      public MutableBlockPos() {
         this(0, 0, 0);
      }

      public MutableBlockPos(int p_122130_, int p_122131_, int p_122132_) {
         super(p_122130_, p_122131_, p_122132_);
      }

      public MutableBlockPos(double p_122126_, double p_122127_, double p_122128_) {
         this(Mth.floor(p_122126_), Mth.floor(p_122127_), Mth.floor(p_122128_));
      }

      public BlockPos offset(double pDx, double pDy, double pDz) {
         return super.offset(pDx, pDy, pDz).immutable();
      }

      public BlockPos offset(int pDx, int pDy, int pDz) {
         return super.offset(pDx, pDy, pDz).immutable();
      }

      public BlockPos multiply(int pScalar) {
         return super.multiply(pScalar).immutable();
      }

      public BlockPos relative(Direction pDirection, int pDistance) {
         return super.relative(pDirection, pDistance).immutable();
      }

      public BlockPos relative(Direction.Axis pAxis, int pDistance) {
         return super.relative(pAxis, pDistance).immutable();
      }

      public BlockPos rotate(Rotation pRotation) {
         return super.rotate(pRotation).immutable();
      }

      public BlockPos.MutableBlockPos set(int pX, int pY, int pZ) {
         this.setX(pX);
         this.setY(pY);
         this.setZ(pZ);
         return this;
      }

      public BlockPos.MutableBlockPos set(double pX, double pY, double pZ) {
         return this.set(Mth.floor(pX), Mth.floor(pY), Mth.floor(pZ));
      }

      public BlockPos.MutableBlockPos set(Vec3i pVector) {
         return this.set(pVector.getX(), pVector.getY(), pVector.getZ());
      }

      public BlockPos.MutableBlockPos set(long pPackedPos) {
         return this.set(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
      }

      public BlockPos.MutableBlockPos set(AxisCycle pCycle, int pX, int pY, int pZ) {
         return this.set(pCycle.cycle(pX, pY, pZ, Direction.Axis.X), pCycle.cycle(pX, pY, pZ, Direction.Axis.Y), pCycle.cycle(pX, pY, pZ, Direction.Axis.Z));
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i pPos, Direction pDirection) {
         return this.set(pPos.getX() + pDirection.getStepX(), pPos.getY() + pDirection.getStepY(), pPos.getZ() + pDirection.getStepZ());
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i pVector, int pOffsetX, int pOffsetY, int pOffsetZ) {
         return this.set(pVector.getX() + pOffsetX, pVector.getY() + pOffsetY, pVector.getZ() + pOffsetZ);
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i p_175307_, Vec3i p_175308_) {
         return this.set(p_175307_.getX() + p_175308_.getX(), p_175307_.getY() + p_175308_.getY(), p_175307_.getZ() + p_175308_.getZ());
      }

      public BlockPos.MutableBlockPos move(Direction pDirection) {
         return this.move(pDirection, 1);
      }

      public BlockPos.MutableBlockPos move(Direction pDirection, int pN) {
         return this.set(this.getX() + pDirection.getStepX() * pN, this.getY() + pDirection.getStepY() * pN, this.getZ() + pDirection.getStepZ() * pN);
      }

      public BlockPos.MutableBlockPos move(int pX, int pY, int pZ) {
         return this.set(this.getX() + pX, this.getY() + pY, this.getZ() + pZ);
      }

      public BlockPos.MutableBlockPos move(Vec3i p_122194_) {
         return this.set(this.getX() + p_122194_.getX(), this.getY() + p_122194_.getY(), this.getZ() + p_122194_.getZ());
      }

      public BlockPos.MutableBlockPos clamp(Direction.Axis pAxis, int pMin, int pMax) {
         switch(pAxis) {
         case X:
            return this.set(Mth.clamp(this.getX(), pMin, pMax), this.getY(), this.getZ());
         case Y:
            return this.set(this.getX(), Mth.clamp(this.getY(), pMin, pMax), this.getZ());
         case Z:
            return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), pMin, pMax));
         default:
            throw new IllegalStateException("Unable to clamp axis " + pAxis);
         }
      }

      public BlockPos.MutableBlockPos setX(int pX) {
         super.setX(pX);
         return this;
      }

      public BlockPos.MutableBlockPos setY(int pY) {
         super.setY(pY);
         return this;
      }

      public BlockPos.MutableBlockPos setZ(int pZ) {
         super.setZ(pZ);
         return this;
      }

      public BlockPos immutable() {
         return new BlockPos(this);
      }
   }
}