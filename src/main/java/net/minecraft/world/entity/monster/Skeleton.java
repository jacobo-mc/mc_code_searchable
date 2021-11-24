package net.minecraft.world.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Skeleton extends AbstractSkeleton {
   private static final EntityDataAccessor<Boolean> DATA_STRAY_CONVERSION_ID = SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.BOOLEAN);
   public static final String CONVERSION_TAG = "StrayConversionTime";
   private int inPowderSnowTime;
   private int conversionTime;

   public Skeleton(EntityType<? extends Skeleton> p_33570_, Level p_33571_) {
      super(p_33570_, p_33571_);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_STRAY_CONVERSION_ID, false);
   }

   public boolean isFreezeConverting() {
      return this.getEntityData().get(DATA_STRAY_CONVERSION_ID);
   }

   public void setFreezeConverting(boolean p_149843_) {
      this.entityData.set(DATA_STRAY_CONVERSION_ID, p_149843_);
   }

   public boolean isShaking() {
      return this.isFreezeConverting();
   }

   public void tick() {
      if (!this.level.isClientSide && this.isAlive() && !this.isNoAi()) {
         if (this.isFreezeConverting()) {
            --this.conversionTime;
            if (this.conversionTime < 0) {
               this.doFreezeConversion();
            }
         } else if (this.isInPowderSnow) {
            ++this.inPowderSnowTime;
            if (this.inPowderSnowTime >= 140) {
               this.startFreezeConversion(300);
            }
         } else {
            this.inPowderSnowTime = -1;
         }
      }

      super.tick();
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("StrayConversionTime", this.isFreezeConverting() ? this.conversionTime : -1);
   }

   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("StrayConversionTime", 99) && pCompound.getInt("StrayConversionTime") > -1) {
         this.startFreezeConversion(pCompound.getInt("StrayConversionTime"));
      }

   }

   private void startFreezeConversion(int p_149831_) {
      this.conversionTime = p_149831_;
      this.entityData.set(DATA_STRAY_CONVERSION_ID, true);
   }

   protected void doFreezeConversion() {
      this.convertTo(EntityType.STRAY, true);
      if (!this.isSilent()) {
         this.level.levelEvent((Player)null, 1048, this.blockPosition(), 0);
      }

   }

   public boolean canFreeze() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SKELETON_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.SKELETON_STEP;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      Entity entity = pSource.getEntity();
      if (entity instanceof Creeper) {
         Creeper creeper = (Creeper)entity;
         if (creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(Items.SKELETON_SKULL);
         }
      }

   }
}