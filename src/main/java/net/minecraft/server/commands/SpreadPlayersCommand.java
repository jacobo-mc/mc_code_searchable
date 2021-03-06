package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
   private static final int MAX_ITERATION_COUNT = 10000;
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType((p_138745_, p_138746_, p_138747_, p_138748_) -> {
      return new TranslatableComponent("commands.spreadplayers.failed.teams", p_138745_, p_138746_, p_138747_, p_138748_);
   });
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType((p_138723_, p_138724_, p_138725_, p_138726_) -> {
      return new TranslatableComponent("commands.spreadplayers.failed.entities", p_138723_, p_138724_, p_138725_, p_138726_);
   });

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("spreadplayers").requires((p_138701_) -> {
         return p_138701_.hasPermission(2);
      }).then(Commands.argument("center", Vec2Argument.vec2()).then(Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F)).then(Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F)).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((p_138743_) -> {
         return spreadPlayers(p_138743_.getSource(), Vec2Argument.getVec2(p_138743_, "center"), FloatArgumentType.getFloat(p_138743_, "spreadDistance"), FloatArgumentType.getFloat(p_138743_, "maxRange"), p_138743_.getSource().getLevel().getMaxBuildHeight(), BoolArgumentType.getBool(p_138743_, "respectTeams"), EntityArgument.getEntities(p_138743_, "targets"));
      }))).then(Commands.literal("under").then(Commands.argument("maxHeight", IntegerArgumentType.integer(0)).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((p_138699_) -> {
         return spreadPlayers(p_138699_.getSource(), Vec2Argument.getVec2(p_138699_, "center"), FloatArgumentType.getFloat(p_138699_, "spreadDistance"), FloatArgumentType.getFloat(p_138699_, "maxRange"), IntegerArgumentType.getInteger(p_138699_, "maxHeight"), BoolArgumentType.getBool(p_138699_, "respectTeams"), EntityArgument.getEntities(p_138699_, "targets"));
      })))))))));
   }

   private static int spreadPlayers(CommandSourceStack p_138703_, Vec2 p_138704_, float p_138705_, float p_138706_, int p_138707_, boolean p_138708_, Collection<? extends Entity> p_138709_) throws CommandSyntaxException {
      Random random = new Random();
      double d0 = (double)(p_138704_.x - p_138706_);
      double d1 = (double)(p_138704_.y - p_138706_);
      double d2 = (double)(p_138704_.x + p_138706_);
      double d3 = (double)(p_138704_.y + p_138706_);
      SpreadPlayersCommand.Position[] aspreadplayerscommand$position = createInitialPositions(random, p_138708_ ? getNumberOfTeams(p_138709_) : p_138709_.size(), d0, d1, d2, d3);
      spreadPositions(p_138704_, (double)p_138705_, p_138703_.getLevel(), random, d0, d1, d2, d3, p_138707_, aspreadplayerscommand$position, p_138708_);
      double d4 = setPlayerPositions(p_138709_, p_138703_.getLevel(), aspreadplayerscommand$position, p_138707_, p_138708_);
      p_138703_.sendSuccess(new TranslatableComponent("commands.spreadplayers.success." + (p_138708_ ? "teams" : "entities"), aspreadplayerscommand$position.length, p_138704_.x, p_138704_.y, String.format(Locale.ROOT, "%.2f", d4)), true);
      return aspreadplayerscommand$position.length;
   }

   private static int getNumberOfTeams(Collection<? extends Entity> pEntities) {
      Set<Team> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         if (entity instanceof Player) {
            set.add(entity.getTeam());
         } else {
            set.add((Team)null);
         }
      }

      return set.size();
   }

   private static void spreadPositions(Vec2 p_138711_, double p_138712_, ServerLevel p_138713_, Random p_138714_, double p_138715_, double p_138716_, double p_138717_, double p_138718_, int p_138719_, SpreadPlayersCommand.Position[] p_138720_, boolean p_138721_) throws CommandSyntaxException {
      boolean flag = true;
      double d0 = (double)Float.MAX_VALUE;

      int i;
      for(i = 0; i < 10000 && flag; ++i) {
         flag = false;
         d0 = (double)Float.MAX_VALUE;

         for(int j = 0; j < p_138720_.length; ++j) {
            SpreadPlayersCommand.Position spreadplayerscommand$position = p_138720_[j];
            int k = 0;
            SpreadPlayersCommand.Position spreadplayerscommand$position1 = new SpreadPlayersCommand.Position();

            for(int l = 0; l < p_138720_.length; ++l) {
               if (j != l) {
                  SpreadPlayersCommand.Position spreadplayerscommand$position2 = p_138720_[l];
                  double d1 = spreadplayerscommand$position.dist(spreadplayerscommand$position2);
                  d0 = Math.min(d1, d0);
                  if (d1 < p_138712_) {
                     ++k;
                     spreadplayerscommand$position1.x += spreadplayerscommand$position2.x - spreadplayerscommand$position.x;
                     spreadplayerscommand$position1.z += spreadplayerscommand$position2.z - spreadplayerscommand$position.z;
                  }
               }
            }

            if (k > 0) {
               spreadplayerscommand$position1.x /= (double)k;
               spreadplayerscommand$position1.z /= (double)k;
               double d2 = spreadplayerscommand$position1.getLength();
               if (d2 > 0.0D) {
                  spreadplayerscommand$position1.normalize();
                  spreadplayerscommand$position.moveAway(spreadplayerscommand$position1);
               } else {
                  spreadplayerscommand$position.randomize(p_138714_, p_138715_, p_138716_, p_138717_, p_138718_);
               }

               flag = true;
            }

            if (spreadplayerscommand$position.clamp(p_138715_, p_138716_, p_138717_, p_138718_)) {
               flag = true;
            }
         }

         if (!flag) {
            for(SpreadPlayersCommand.Position spreadplayerscommand$position3 : p_138720_) {
               if (!spreadplayerscommand$position3.isSafe(p_138713_, p_138719_)) {
                  spreadplayerscommand$position3.randomize(p_138714_, p_138715_, p_138716_, p_138717_, p_138718_);
                  flag = true;
               }
            }
         }
      }

      if (d0 == (double)Float.MAX_VALUE) {
         d0 = 0.0D;
      }

      if (i >= 10000) {
         if (p_138721_) {
            throw ERROR_FAILED_TO_SPREAD_TEAMS.create(p_138720_.length, p_138711_.x, p_138711_.y, String.format(Locale.ROOT, "%.2f", d0));
         } else {
            throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(p_138720_.length, p_138711_.x, p_138711_.y, String.format(Locale.ROOT, "%.2f", d0));
         }
      }
   }

   private static double setPlayerPositions(Collection<? extends Entity> p_138730_, ServerLevel p_138731_, SpreadPlayersCommand.Position[] p_138732_, int p_138733_, boolean p_138734_) {
      double d0 = 0.0D;
      int i = 0;
      Map<Team, SpreadPlayersCommand.Position> map = Maps.newHashMap();

      for(Entity entity : p_138730_) {
         SpreadPlayersCommand.Position spreadplayerscommand$position;
         if (p_138734_) {
            Team team = entity instanceof Player ? entity.getTeam() : null;
            if (!map.containsKey(team)) {
               map.put(team, p_138732_[i++]);
            }

            spreadplayerscommand$position = map.get(team);
         } else {
            spreadplayerscommand$position = p_138732_[i++];
         }

         entity.teleportToWithTicket((double)Mth.floor(spreadplayerscommand$position.x) + 0.5D, (double)spreadplayerscommand$position.getSpawnY(p_138731_, p_138733_), (double)Mth.floor(spreadplayerscommand$position.z) + 0.5D);
         double d2 = Double.MAX_VALUE;

         for(SpreadPlayersCommand.Position spreadplayerscommand$position1 : p_138732_) {
            if (spreadplayerscommand$position != spreadplayerscommand$position1) {
               double d1 = spreadplayerscommand$position.dist(spreadplayerscommand$position1);
               d2 = Math.min(d1, d2);
            }
         }

         d0 += d2;
      }

      return p_138730_.size() < 2 ? 0.0D : d0 / (double)p_138730_.size();
   }

   private static SpreadPlayersCommand.Position[] createInitialPositions(Random pRandom, int pCount, double pMinX, double pMinZ, double pMaxX, double pMaxZ) {
      SpreadPlayersCommand.Position[] aspreadplayerscommand$position = new SpreadPlayersCommand.Position[pCount];

      for(int i = 0; i < aspreadplayerscommand$position.length; ++i) {
         SpreadPlayersCommand.Position spreadplayerscommand$position = new SpreadPlayersCommand.Position();
         spreadplayerscommand$position.randomize(pRandom, pMinX, pMinZ, pMaxX, pMaxZ);
         aspreadplayerscommand$position[i] = spreadplayerscommand$position;
      }

      return aspreadplayerscommand$position;
   }

   static class Position {
      double x;
      double z;

      double dist(SpreadPlayersCommand.Position pOther) {
         double d0 = this.x - pOther.x;
         double d1 = this.z - pOther.z;
         return Math.sqrt(d0 * d0 + d1 * d1);
      }

      void normalize() {
         double d0 = this.getLength();
         this.x /= d0;
         this.z /= d0;
      }

      double getLength() {
         return Math.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(SpreadPlayersCommand.Position pOther) {
         this.x -= pOther.x;
         this.z -= pOther.z;
      }

      public boolean clamp(double pMinX, double pMinZ, double pMaxX, double pMaxZ) {
         boolean flag = false;
         if (this.x < pMinX) {
            this.x = pMinX;
            flag = true;
         } else if (this.x > pMaxX) {
            this.x = pMaxX;
            flag = true;
         }

         if (this.z < pMinZ) {
            this.z = pMinZ;
            flag = true;
         } else if (this.z > pMaxZ) {
            this.z = pMaxZ;
            flag = true;
         }

         return flag;
      }

      public int getSpawnY(BlockGetter p_138759_, int p_138760_) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(this.x, (double)(p_138760_ + 1), this.z);
         boolean flag = p_138759_.getBlockState(blockpos$mutableblockpos).isAir();
         blockpos$mutableblockpos.move(Direction.DOWN);

         boolean flag2;
         for(boolean flag1 = p_138759_.getBlockState(blockpos$mutableblockpos).isAir(); blockpos$mutableblockpos.getY() > p_138759_.getMinBuildHeight(); flag1 = flag2) {
            blockpos$mutableblockpos.move(Direction.DOWN);
            flag2 = p_138759_.getBlockState(blockpos$mutableblockpos).isAir();
            if (!flag2 && flag1 && flag) {
               return blockpos$mutableblockpos.getY() + 1;
            }

            flag = flag1;
         }

         return p_138760_ + 1;
      }

      public boolean isSafe(BlockGetter p_138774_, int p_138775_) {
         BlockPos blockpos = new BlockPos(this.x, (double)(this.getSpawnY(p_138774_, p_138775_) - 1), this.z);
         BlockState blockstate = p_138774_.getBlockState(blockpos);
         Material material = blockstate.getMaterial();
         return blockpos.getY() < p_138775_ && !material.isLiquid() && material != Material.FIRE;
      }

      public void randomize(Random pRandom, double pMinX, double pMinZ, double pMaxX, double pMaZx) {
         this.x = Mth.nextDouble(pRandom, pMinX, pMaxX);
         this.z = Mth.nextDouble(pRandom, pMinZ, pMaZx);
      }
   }
}