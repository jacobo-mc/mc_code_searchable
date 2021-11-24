package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
   private final Item item;
   private final int duration;

   public ClientboundCooldownPacket(Item pItem, int pDuration) {
      this.item = pItem;
      this.duration = pDuration;
   }

   public ClientboundCooldownPacket(FriendlyByteBuf pBuffer) {
      this.item = Item.byId(pBuffer.readVarInt());
      this.duration = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(Item.getId(this.item));
      pBuffer.writeVarInt(this.duration);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleItemCooldown(this);
   }

   public Item getItem() {
      return this.item;
   }

   public int getDuration() {
      return this.duration;
   }
}