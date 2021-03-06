package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
   private static final int SELF_SIZE_IN_BITS = 64;
   public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
      public EndTag load(DataInput p_128550_, int p_128551_, NbtAccounter p_128552_) {
         p_128552_.accountBits(64L);
         return EndTag.INSTANCE;
      }

      public String getName() {
         return "END";
      }

      public String getPrettyName() {
         return "TAG_End";
      }

      public boolean isValue() {
         return true;
      }
   };
   public static final EndTag INSTANCE = new EndTag();

   private EndTag() {
   }

   public void write(DataOutput pOutput) throws IOException {
   }

   public byte getId() {
      return 0;
   }

   public TagType<EndTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   public EndTag copy() {
      return this;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitEnd(this);
   }
}