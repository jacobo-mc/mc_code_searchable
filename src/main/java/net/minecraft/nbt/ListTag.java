package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
   private static final int SELF_SIZE_IN_BITS = 296;
   public static final TagType<ListTag> TYPE = new TagType<ListTag>() {
      public ListTag load(DataInput p_128792_, int p_128793_, NbtAccounter p_128794_) throws IOException {
         p_128794_.accountBits(296L);
         if (p_128793_ > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            byte b0 = p_128792_.readByte();
            int i = p_128792_.readInt();
            if (b0 == 0 && i > 0) {
               throw new RuntimeException("Missing type on ListTag");
            } else {
               p_128794_.accountBits(32L * (long)i);
               TagType<?> tagtype = TagTypes.getType(b0);
               List<Tag> list = Lists.newArrayListWithCapacity(i);

               for(int j = 0; j < i; ++j) {
                  list.add(tagtype.load(p_128792_, p_128793_ + 1, p_128794_));
               }

               return new ListTag(list, b0);
            }
         }
      }

      public String getName() {
         return "LIST";
      }

      public String getPrettyName() {
         return "TAG_List";
      }
   };
   private final List<Tag> list;
   private byte type;

   ListTag(List<Tag> pList, byte pType) {
      this.list = pList;
      this.type = pType;
   }

   public ListTag() {
      this(Lists.newArrayList(), (byte)0);
   }

   public void write(DataOutput pOutput) throws IOException {
      if (this.list.isEmpty()) {
         this.type = 0;
      } else {
         this.type = this.list.get(0).getId();
      }

      pOutput.writeByte(this.type);
      pOutput.writeInt(this.list.size());

      for(Tag tag : this.list) {
         tag.write(pOutput);
      }

   }

   public byte getId() {
      return 9;
   }

   public TagType<ListTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   private void updateTypeAfterRemove() {
      if (this.list.isEmpty()) {
         this.type = 0;
      }

   }

   public Tag remove(int p_128751_) {
      Tag tag = this.list.remove(p_128751_);
      this.updateTypeAfterRemove();
      return tag;
   }

   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public CompoundTag getCompound(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 10) {
            return (CompoundTag)tag;
         }
      }

      return new CompoundTag();
   }

   public ListTag getList(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 9) {
            return (ListTag)tag;
         }
      }

      return new ListTag();
   }

   public short getShort(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 2) {
            return ((ShortTag)tag).getAsShort();
         }
      }

      return 0;
   }

   public int getInt(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 3) {
            return ((IntTag)tag).getAsInt();
         }
      }

      return 0;
   }

   public int[] getIntArray(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 11) {
            return ((IntArrayTag)tag).getAsIntArray();
         }
      }

      return new int[0];
   }

   public long[] getLongArray(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 11) {
            return ((LongArrayTag)tag).getAsLongArray();
         }
      }

      return new long[0];
   }

   public double getDouble(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 6) {
            return ((DoubleTag)tag).getAsDouble();
         }
      }

      return 0.0D;
   }

   public float getFloat(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         if (tag.getId() == 5) {
            return ((FloatTag)tag).getAsFloat();
         }
      }

      return 0.0F;
   }

   public String getString(int pIndex) {
      if (pIndex >= 0 && pIndex < this.list.size()) {
         Tag tag = this.list.get(pIndex);
         return tag.getId() == 8 ? tag.getAsString() : tag.toString();
      } else {
         return "";
      }
   }

   public int size() {
      return this.list.size();
   }

   public Tag get(int p_128781_) {
      return this.list.get(p_128781_);
   }

   public Tag set(int p_128760_, Tag p_128761_) {
      Tag tag = this.get(p_128760_);
      if (!this.setTag(p_128760_, p_128761_)) {
         throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", p_128761_.getId(), this.type));
      } else {
         return tag;
      }
   }

   public void add(int p_128753_, Tag p_128754_) {
      if (!this.addTag(p_128753_, p_128754_)) {
         throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", p_128754_.getId(), this.type));
      }
   }

   public boolean setTag(int pIndex, Tag pNbt) {
      if (this.updateType(pNbt)) {
         this.list.set(pIndex, pNbt);
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, Tag pNbt) {
      if (this.updateType(pNbt)) {
         this.list.add(pIndex, pNbt);
         return true;
      } else {
         return false;
      }
   }

   private boolean updateType(Tag pTag) {
      if (pTag.getId() == 0) {
         return false;
      } else if (this.type == 0) {
         this.type = pTag.getId();
         return true;
      } else {
         return this.type == pTag.getId();
      }
   }

   public ListTag copy() {
      Iterable<Tag> iterable = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
      List<Tag> list = Lists.newArrayList(iterable);
      return new ListTag(list, this.type);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof ListTag && Objects.equals(this.list, ((ListTag)pOther).list);
      }
   }

   public int hashCode() {
      return this.list.hashCode();
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitList(this);
   }

   public byte getElementType() {
      return this.type;
   }

   public void clear() {
      this.list.clear();
      this.type = 0;
   }
}