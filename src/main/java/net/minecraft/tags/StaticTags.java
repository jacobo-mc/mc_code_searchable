package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTags {
   private static final Set<ResourceKey<?>> HELPERS_IDS = Sets.newHashSet();
   private static final List<StaticTagHelper<?>> HELPERS = Lists.newArrayList();

   public static <T> StaticTagHelper<T> create(ResourceKey<? extends Registry<T>> pKey, String pDirectory) {
      if (!HELPERS_IDS.add(pKey)) {
         throw new IllegalStateException("Duplicate entry for static tag collection: " + pKey);
      } else {
         StaticTagHelper<T> statictaghelper = new StaticTagHelper<>(pKey, pDirectory);
         HELPERS.add(statictaghelper);
         return statictaghelper;
      }
   }

   public static void resetAll(TagContainer pContainer) {
      HELPERS.forEach((p_13273_) -> {
         p_13273_.reset(pContainer);
      });
   }

   public static void resetAllToEmpty() {
      HELPERS.forEach(StaticTagHelper::resetToEmpty);
   }

   public static Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> getAllMissingTags(TagContainer pContainer) {
      Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> multimap = HashMultimap.create();
      HELPERS.forEach((p_144348_) -> {
         multimap.putAll(p_144348_.getKey(), p_144348_.getMissingTags(pContainer));
      });
      return multimap;
   }

   public static void bootStrap() {
      makeSureAllKnownHelpersAreLoaded();
   }

   private static Set<StaticTagHelper<?>> getAllKnownHelpers() {
      return ImmutableSet.of(BlockTags.HELPER, ItemTags.HELPER, FluidTags.HELPER, EntityTypeTags.HELPER, GameEventTags.HELPER);
   }

   private static void makeSureAllKnownHelpersAreLoaded() {
      Set<ResourceKey<?>> set = getAllKnownHelpers().stream().map(StaticTagHelper::getKey).collect(Collectors.toSet());
      if (!Sets.difference(HELPERS_IDS, set).isEmpty()) {
         throw new IllegalStateException("Missing helper registrations");
      }
   }

   public static void visitHelpers(Consumer<StaticTagHelper<?>> pVisitor) {
      HELPERS.forEach(pVisitor);
   }

   public static TagContainer createCollection() {
      TagContainer.Builder tagcontainer$builder = new TagContainer.Builder();
      makeSureAllKnownHelpersAreLoaded();
      HELPERS.forEach((p_144344_) -> {
         p_144344_.addToCollection(tagcontainer$builder);
      });
      return tagcontainer$builder.build();
   }
}