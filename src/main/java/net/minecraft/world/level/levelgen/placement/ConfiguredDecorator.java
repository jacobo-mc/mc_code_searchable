package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Decoratable;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> implements Decoratable<ConfiguredDecorator<?>> {
   public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR.dispatch("type", (p_70488_) -> {
      return p_70488_.decorator;
   }, FeatureDecorator::configuredCodec);
   private final FeatureDecorator<DC> decorator;
   private final DC config;

   public ConfiguredDecorator(FeatureDecorator<DC> pDecorator, DC pConfig) {
      this.decorator = pDecorator;
      this.config = pConfig;
   }

   public Stream<BlockPos> getPositions(DecorationContext pContext, Random pRandom, BlockPos pPos) {
      return this.decorator.getPositions(pContext, pRandom, this.config, pPos);
   }

   public String toString() {
      return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
   }

   public ConfiguredDecorator<?> decorated(ConfiguredDecorator<?> pDecorator) {
      return new ConfiguredDecorator<>(FeatureDecorator.DECORATED, new DecoratedDecoratorConfiguration(pDecorator, this));
   }

   public DC config() {
      return this.config;
   }
}