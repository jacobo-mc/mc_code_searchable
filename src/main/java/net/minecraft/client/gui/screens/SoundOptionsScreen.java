package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
   public SoundOptionsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, new TranslatableComponent("options.sounds.title"));
   }

   protected void init() {
      int i = 0;
      this.addRenderableWidget(new VolumeSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundSource.MASTER, 310));
      i = i + 2;

      for(SoundSource soundsource : SoundSource.values()) {
         if (soundsource != SoundSource.MASTER) {
            this.addRenderableWidget(new VolumeSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundsource, 150));
            ++i;
         }
      }

      int j = this.width / 2 - 75;
      int k = this.height / 6 - 12;
      ++i;
      this.addRenderableWidget(Option.SHOW_SUBTITLES.createButton(this.options, j, k + 24 * (i >> 1), 150));
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, (p_96713_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 15, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }
}