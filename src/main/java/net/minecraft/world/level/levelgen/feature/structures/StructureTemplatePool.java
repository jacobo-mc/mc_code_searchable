package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureTemplatePool {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int SIZE_UNSET = Integer.MIN_VALUE;
   public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create((p_69267_) -> {
      return p_69267_.group(ResourceLocation.CODEC.fieldOf("name").forGetter(StructureTemplatePool::getName), ResourceLocation.CODEC.fieldOf("fallback").forGetter(StructureTemplatePool::getFallback), Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight")).codec().listOf().fieldOf("elements").forGetter((p_161683_) -> {
         return p_161683_.rawTemplates;
      })).apply(p_69267_, StructureTemplatePool::new);
   });
   public static final Codec<Supplier<StructureTemplatePool>> CODEC = RegistryFileCodec.create(Registry.TEMPLATE_POOL_REGISTRY, DIRECT_CODEC);
   private final ResourceLocation name;
   private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
   private final List<StructurePoolElement> templates;
   private final ResourceLocation fallback;
   private int maxSize = Integer.MIN_VALUE;

   public StructureTemplatePool(ResourceLocation p_69255_, ResourceLocation p_69256_, List<Pair<StructurePoolElement, Integer>> p_69257_) {
      this.name = p_69255_;
      this.rawTemplates = p_69257_;
      this.templates = Lists.newArrayList();

      for(Pair<StructurePoolElement, Integer> pair : p_69257_) {
         StructurePoolElement structurepoolelement = pair.getFirst();

         for(int i = 0; i < pair.getSecond(); ++i) {
            this.templates.add(structurepoolelement);
         }
      }

      this.fallback = p_69256_;
   }

   public StructureTemplatePool(ResourceLocation p_69259_, ResourceLocation p_69260_, List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> p_69261_, StructureTemplatePool.Projection p_69262_) {
      this.name = p_69259_;
      this.rawTemplates = Lists.newArrayList();
      this.templates = Lists.newArrayList();

      for(Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> pair : p_69261_) {
         StructurePoolElement structurepoolelement = pair.getFirst().apply(p_69262_);
         this.rawTemplates.add(Pair.of(structurepoolelement, pair.getSecond()));

         for(int i = 0; i < pair.getSecond(); ++i) {
            this.templates.add(structurepoolelement);
         }
      }

      this.fallback = p_69260_;
   }

   public int getMaxSize(StructureManager pTemplateManager) {
      if (this.maxSize == Integer.MIN_VALUE) {
         this.maxSize = this.templates.stream().filter((p_161681_) -> {
            return p_161681_ != EmptyPoolElement.INSTANCE;
         }).mapToInt((p_161686_) -> {
            return p_161686_.getBoundingBox(pTemplateManager, BlockPos.ZERO, Rotation.NONE).getYSpan();
         }).max().orElse(0);
      }

      return this.maxSize;
   }

   public ResourceLocation getFallback() {
      return this.fallback;
   }

   public StructurePoolElement getRandomTemplate(Random pRandom) {
      return this.templates.get(pRandom.nextInt(this.templates.size()));
   }

   public List<StructurePoolElement> getShuffledTemplates(Random pRandom) {
      return ImmutableList.copyOf(ObjectArrays.shuffle(this.templates.toArray(new StructurePoolElement[0]), pRandom));
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public int size() {
      return this.templates.size();
   }

   public static enum Projection implements StringRepresentable {
      TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
      RIGID("rigid", ImmutableList.of());

      public static final Codec<StructureTemplatePool.Projection> CODEC = StringRepresentable.fromEnum(StructureTemplatePool.Projection::values, StructureTemplatePool.Projection::byName);
      private static final Map<String, StructureTemplatePool.Projection> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(StructureTemplatePool.Projection::getName, (p_69294_) -> {
         return p_69294_;
      }));
      private final String name;
      private final ImmutableList<StructureProcessor> processors;

      private Projection(String p_69290_, ImmutableList<StructureProcessor> p_69291_) {
         this.name = p_69290_;
         this.processors = p_69291_;
      }

      public String getName() {
         return this.name;
      }

      public static StructureTemplatePool.Projection byName(String p_69296_) {
         return BY_NAME.get(p_69296_);
      }

      public ImmutableList<StructureProcessor> getProcessors() {
         return this.processors;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}