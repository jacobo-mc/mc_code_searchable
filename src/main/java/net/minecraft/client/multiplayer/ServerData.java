package net.minecraft.client.multiplayer;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerData {
   public String name;
   public String ip;
   public Component status;
   public Component motd;
   public long ping;
   public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
   public Component version = new TextComponent(SharedConstants.getCurrentVersion().getName());
   public boolean pinged;
   public List<Component> playerList = Collections.emptyList();
   private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
   @Nullable
   private String iconB64;
   private boolean lan;

   public ServerData(String pName, String pIp, boolean pLan) {
      this.name = pName;
      this.ip = pIp;
      this.lan = pLan;
   }

   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", this.name);
      compoundtag.putString("ip", this.ip);
      if (this.iconB64 != null) {
         compoundtag.putString("icon", this.iconB64);
      }

      if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
         compoundtag.putBoolean("acceptTextures", true);
      } else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
         compoundtag.putBoolean("acceptTextures", false);
      }

      return compoundtag;
   }

   public ServerData.ServerPackStatus getResourcePackStatus() {
      return this.packStatus;
   }

   public void setResourcePackStatus(ServerData.ServerPackStatus pPackStatus) {
      this.packStatus = pPackStatus;
   }

   public static ServerData read(CompoundTag pNbtCompound) {
      ServerData serverdata = new ServerData(pNbtCompound.getString("name"), pNbtCompound.getString("ip"), false);
      if (pNbtCompound.contains("icon", 8)) {
         serverdata.setIconB64(pNbtCompound.getString("icon"));
      }

      if (pNbtCompound.contains("acceptTextures", 1)) {
         if (pNbtCompound.getBoolean("acceptTextures")) {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
         } else {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
         }
      } else {
         serverdata.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
      }

      return serverdata;
   }

   @Nullable
   public String getIconB64() {
      return this.iconB64;
   }

   public void setIconB64(@Nullable String pIconB64) {
      this.iconB64 = pIconB64;
   }

   public boolean isLan() {
      return this.lan;
   }

   public void copyFrom(ServerData pServerData) {
      this.ip = pServerData.ip;
      this.name = pServerData.name;
      this.setResourcePackStatus(pServerData.getResourcePackStatus());
      this.iconB64 = pServerData.iconB64;
      this.lan = pServerData.lan;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ServerPackStatus {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final Component name;

      private ServerPackStatus(String p_105399_) {
         this.name = new TranslatableComponent("addServer.resourcePack." + p_105399_);
      }

      public Component getName() {
         return this.name;
      }
   }
}