package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class GlStateManager {
   public static final int TEXTURE_COUNT = 12;
   private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
   private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
   private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
   private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
   private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
   private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
   private static final GlStateManager.ScissorState SCISSOR = new GlStateManager.ScissorState();
   private static int activeTexture;
   private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 12).mapToObj((p_157120_) -> {
      return new GlStateManager.TextureState();
   }).toArray((p_157122_) -> {
      return new GlStateManager.TextureState[p_157122_];
   });
   private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();

   public static void _disableScissorTest() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      SCISSOR.mode.disable();
   }

   public static void _enableScissorTest() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      SCISSOR.mode.enable();
   }

   public static void _scissorBox(int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL20.glScissor(pX, pY, pWidth, pHeight);
   }

   public static void _disableDepthTest() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      DEPTH.mode.disable();
   }

   public static void _enableDepthTest() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      DEPTH.mode.enable();
   }

   public static void _depthFunc(int pDepthFunc) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      if (pDepthFunc != DEPTH.func) {
         DEPTH.func = pDepthFunc;
         GL11.glDepthFunc(pDepthFunc);
      }

   }

   public static void _depthMask(boolean pFlag) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pFlag != DEPTH.mask) {
         DEPTH.mask = pFlag;
         GL11.glDepthMask(pFlag);
      }

   }

   public static void _disableBlend() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      BLEND.mode.disable();
   }

   public static void _enableBlend() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      BLEND.mode.enable();
   }

   public static void _blendFunc(int pSourceFactor, int pDestFactor) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pSourceFactor != BLEND.srcRgb || pDestFactor != BLEND.dstRgb) {
         BLEND.srcRgb = pSourceFactor;
         BLEND.dstRgb = pDestFactor;
         GL11.glBlendFunc(pSourceFactor, pDestFactor);
      }

   }

   public static void _blendFuncSeparate(int pSrcFactor, int pDstFactor, int pSrcFactorAlpha, int pDstFactorAlpha) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pSrcFactor != BLEND.srcRgb || pDstFactor != BLEND.dstRgb || pSrcFactorAlpha != BLEND.srcAlpha || pDstFactorAlpha != BLEND.dstAlpha) {
         BLEND.srcRgb = pSrcFactor;
         BLEND.dstRgb = pDstFactor;
         BLEND.srcAlpha = pSrcFactorAlpha;
         BLEND.dstAlpha = pDstFactorAlpha;
         glBlendFuncSeparate(pSrcFactor, pDstFactor, pSrcFactorAlpha, pDstFactorAlpha);
      }

   }

   public static void _blendEquation(int pMode) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL14.glBlendEquation(pMode);
   }

   public static int glGetProgrami(int pProgram, int pPname) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetProgrami(pProgram, pPname);
   }

   public static void glAttachShader(int pProgram, int pShader) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glAttachShader(pProgram, pShader);
   }

   public static void glDeleteShader(int pShader) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glDeleteShader(pShader);
   }

   public static int glCreateShader(int pType) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glCreateShader(pType);
   }

   public static void glShaderSource(int p_157117_, List<String> p_157118_) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      StringBuilder stringbuilder = new StringBuilder();

      for(String s : p_157118_) {
         stringbuilder.append(s);
      }

      byte[] abyte = stringbuilder.toString().getBytes(Charsets.UTF_8);
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(abyte.length + 1);
      bytebuffer.put(abyte);
      bytebuffer.put((byte)0);
      bytebuffer.flip();

      try {
         MemoryStack memorystack = MemoryStack.stackPush();

         try {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            pointerbuffer.put(bytebuffer);
            GL20C.nglShaderSource(p_157117_, 1, pointerbuffer.address0(), 0L);
         } catch (Throwable throwable1) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (memorystack != null) {
            memorystack.close();
         }
      } finally {
         MemoryUtil.memFree(bytebuffer);
      }

   }

   public static void glCompileShader(int pShader) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glCompileShader(pShader);
   }

   public static int glGetShaderi(int pShader, int pPname) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetShaderi(pShader, pPname);
   }

   public static void _glUseProgram(int pProgram) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUseProgram(pProgram);
   }

   public static int glCreateProgram() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glCreateProgram();
   }

   public static void glDeleteProgram(int pProgram) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glDeleteProgram(pProgram);
   }

   public static void glLinkProgram(int pProgram) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glLinkProgram(pProgram);
   }

   public static int _glGetUniformLocation(int pProgram, CharSequence pName) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetUniformLocation(pProgram, pName);
   }

   public static void _glUniform1(int pLocation, IntBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform1iv(pLocation, pValue);
   }

   public static void _glUniform1i(int pLocation, int pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform1i(pLocation, pValue);
   }

   public static void _glUniform1(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform1fv(pLocation, pValue);
   }

   public static void _glUniform2(int pLocation, IntBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform2iv(pLocation, pValue);
   }

   public static void _glUniform2(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform2fv(pLocation, pValue);
   }

   public static void _glUniform3(int pLocation, IntBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform3iv(pLocation, pValue);
   }

   public static void _glUniform3(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform3fv(pLocation, pValue);
   }

   public static void _glUniform4(int pLocation, IntBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform4iv(pLocation, pValue);
   }

   public static void _glUniform4(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniform4fv(pLocation, pValue);
   }

   public static void _glUniformMatrix2(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniformMatrix2fv(pLocation, pTranspose, pValue);
   }

   public static void _glUniformMatrix3(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniformMatrix3fv(pLocation, pTranspose, pValue);
   }

   public static void _glUniformMatrix4(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glUniformMatrix4fv(pLocation, pTranspose, pValue);
   }

   public static int _glGetAttribLocation(int pProgram, CharSequence pName) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetAttribLocation(pProgram, pName);
   }

   public static void _glBindAttribLocation(int pProgram, int pIndex, CharSequence pName) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glBindAttribLocation(pProgram, pIndex, pName);
   }

   public static int _glGenBuffers() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL15.glGenBuffers();
   }

   public static int _glGenVertexArrays() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL30.glGenVertexArrays();
   }

   public static void _glBindBuffer(int pTarget, int pBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL15.glBindBuffer(pTarget, pBuffer);
   }

   public static void _glBindVertexArray(int pArray) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glBindVertexArray(pArray);
   }

   public static void _glBufferData(int pTarget, ByteBuffer pData, int pUsage) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL15.glBufferData(pTarget, pData, pUsage);
   }

   public static void _glBufferData(int pTarget, long pSize, int pUsage) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL15.glBufferData(pTarget, pSize, pUsage);
   }

   @Nullable
   public static ByteBuffer _glMapBuffer(int pTarget, int pAccess) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL15.glMapBuffer(pTarget, pAccess);
   }

   public static void _glUnmapBuffer(int pTarget) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL15.glUnmapBuffer(pTarget);
   }

   public static void _glDeleteBuffers(int pBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL15.glDeleteBuffers(pBuffer);
   }

   public static void _glCopyTexSubImage2D(int pTarget, int pLevel, int pXOffset, int pYOffset, int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL20.glCopyTexSubImage2D(pTarget, pLevel, pXOffset, pYOffset, pX, pY, pWidth, pHeight);
   }

   public static void _glDeleteVertexArrays(int pArray) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL30.glDeleteVertexArrays(pArray);
   }

   public static void _glBindFramebuffer(int pTarget, int pFramebuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glBindFramebuffer(pTarget, pFramebuffer);
   }

   public static void _glBlitFrameBuffer(int pSrcX0, int pSrcY0, int pSrcX1, int pSrcY1, int pDstX0, int pDstY0, int pDstX1, int pDstY1, int pMask, int pFilter) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glBlitFramebuffer(pSrcX0, pSrcY0, pSrcX1, pSrcY1, pDstX0, pDstY0, pDstX1, pDstY1, pMask, pFilter);
   }

   public static void _glBindRenderbuffer(int pTarget, int pRenderBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glBindRenderbuffer(pTarget, pRenderBuffer);
   }

   public static void _glDeleteRenderbuffers(int pRenderBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glDeleteRenderbuffers(pRenderBuffer);
   }

   public static void _glDeleteFramebuffers(int pFrameBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glDeleteFramebuffers(pFrameBuffer);
   }

   public static int glGenFramebuffers() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL30.glGenFramebuffers();
   }

   public static int glGenRenderbuffers() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL30.glGenRenderbuffers();
   }

   public static void _glRenderbufferStorage(int pTarget, int pInternalFormat, int pWidth, int pHeight) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glRenderbufferStorage(pTarget, pInternalFormat, pWidth, pHeight);
   }

   public static void _glFramebufferRenderbuffer(int pTarget, int pAttachment, int pRenderBufferTarget, int pRenderBuffer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glFramebufferRenderbuffer(pTarget, pAttachment, pRenderBufferTarget, pRenderBuffer);
   }

   public static int glCheckFramebufferStatus(int pTarget) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL30.glCheckFramebufferStatus(pTarget);
   }

   public static void _glFramebufferTexture2D(int pTarget, int pAttachment, int pTexTarget, int pTexture, int pLevel) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL30.glFramebufferTexture2D(pTarget, pAttachment, pTexTarget, pTexture, pLevel);
   }

   public static int getBoundFramebuffer() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return _getInteger(36006);
   }

   public static void glActiveTexture(int pTexture) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL13.glActiveTexture(pTexture);
   }

   public static void glBlendFuncSeparate(int pSFactorRGB, int pDFactorRGB, int pSFactorAlpha, int pDFactorAlpha) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL14.glBlendFuncSeparate(pSFactorRGB, pDFactorRGB, pSFactorAlpha, pDFactorAlpha);
   }

   public static String glGetShaderInfoLog(int pShader, int pMaxLength) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetShaderInfoLog(pShader, pMaxLength);
   }

   public static String glGetProgramInfoLog(int pProgram, int pMaxLength) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL20.glGetProgramInfoLog(pProgram, pMaxLength);
   }

   public static void setupLevelDiffuseLighting(Vector3f pLightingVector1, Vector3f pLightingVector2, Matrix4f pMatrix) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      Vector4f vector4f = new Vector4f(pLightingVector1);
      vector4f.transform(pMatrix);
      Vector4f vector4f1 = new Vector4f(pLightingVector2);
      vector4f1.transform(pMatrix);
      RenderSystem.setShaderLights(new Vector3f(vector4f), new Vector3f(vector4f1));
   }

   public static void setupGuiFlatDiffuseLighting(Vector3f pLighting1, Vector3f pLighting2) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.setIdentity();
      matrix4f.multiply(Matrix4f.createScaleMatrix(1.0F, -1.0F, 1.0F));
      matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5F));
      matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0F));
      setupLevelDiffuseLighting(pLighting1, pLighting2, matrix4f);
   }

   public static void setupGui3DDiffuseLighting(Vector3f pLightingVector1, Vector3f pLightingVector2) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.setIdentity();
      matrix4f.multiply(Vector3f.YP.rotationDegrees(62.0F));
      matrix4f.multiply(Vector3f.XP.rotationDegrees(185.5F));
      matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5F));
      matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0F));
      setupLevelDiffuseLighting(pLightingVector1, pLightingVector2, matrix4f);
   }

   public static void _enableCull() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      CULL.enable.enable();
   }

   public static void _disableCull() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      CULL.enable.disable();
   }

   public static void _polygonMode(int pFace, int pMode) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glPolygonMode(pFace, pMode);
   }

   public static void _enablePolygonOffset() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      POLY_OFFSET.fill.enable();
   }

   public static void _disablePolygonOffset() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      POLY_OFFSET.fill.disable();
   }

   public static void _polygonOffset(float pFactor, float pUnits) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pFactor != POLY_OFFSET.factor || pUnits != POLY_OFFSET.units) {
         POLY_OFFSET.factor = pFactor;
         POLY_OFFSET.units = pUnits;
         GL11.glPolygonOffset(pFactor, pUnits);
      }

   }

   public static void _enableColorLogicOp() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      COLOR_LOGIC.enable.enable();
   }

   public static void _disableColorLogicOp() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      COLOR_LOGIC.enable.disable();
   }

   public static void _logicOp(int pLogicOperation) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pLogicOperation != COLOR_LOGIC.op) {
         COLOR_LOGIC.op = pLogicOperation;
         GL11.glLogicOp(pLogicOperation);
      }

   }

   public static void _activeTexture(int pTexture) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (activeTexture != pTexture - '\u84c0') {
         activeTexture = pTexture - '\u84c0';
         glActiveTexture(pTexture);
      }

   }

   public static void _enableTexture() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      TEXTURES[activeTexture].enable = true;
   }

   public static void _disableTexture() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      TEXTURES[activeTexture].enable = false;
   }

   public static void _texParameter(int pTarget, int pParameterName, float pParameter) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glTexParameterf(pTarget, pParameterName, pParameter);
   }

   public static void _texParameter(int pTarget, int pParameterName, int pParameter) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glTexParameteri(pTarget, pParameterName, pParameter);
   }

   public static int _getTexLevelParameter(int pTarget, int pLevel, int pParameterName) {
      RenderSystem.assertThread(RenderSystem::isInInitPhase);
      return GL11.glGetTexLevelParameteri(pTarget, pLevel, pParameterName);
   }

   public static int _genTexture() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL11.glGenTextures();
   }

   public static void _genTextures(int[] pTextures) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glGenTextures(pTextures);
   }

   public static void _deleteTexture(int pTexture) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glDeleteTextures(pTexture);

      for(GlStateManager.TextureState glstatemanager$texturestate : TEXTURES) {
         if (glstatemanager$texturestate.binding == pTexture) {
            glstatemanager$texturestate.binding = -1;
         }
      }

   }

   public static void _deleteTextures(int[] pTextures) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);

      for(GlStateManager.TextureState glstatemanager$texturestate : TEXTURES) {
         for(int i : pTextures) {
            if (glstatemanager$texturestate.binding == i) {
               glstatemanager$texturestate.binding = -1;
            }
         }
      }

      GL11.glDeleteTextures(pTextures);
   }

   public static void _bindTexture(int pTexture) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      if (pTexture != TEXTURES[activeTexture].binding) {
         TEXTURES[activeTexture].binding = pTexture;
         GL11.glBindTexture(3553, pTexture);
      }

   }

   public static int _getTextureId(int pTextureId) {
      return pTextureId >= 0 && pTextureId < 12 && TEXTURES[pTextureId].enable ? TEXTURES[pTextureId].binding : 0;
   }

   public static int _getActiveTexture() {
      return activeTexture + '\u84c0';
   }

   public static void _texImage2D(int pTarget, int pLevel, int pInternalFormat, int pWidth, int pHeight, int pBorder, int pFormat, int pType, @Nullable IntBuffer pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glTexImage2D(pTarget, pLevel, pInternalFormat, pWidth, pHeight, pBorder, pFormat, pType, pPixels);
   }

   public static void _texSubImage2D(int pTarget, int pLevel, int pXOffset, int pYOffset, int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glTexSubImage2D(pTarget, pLevel, pXOffset, pYOffset, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void _getTexImage(int pTex, int pLevel, int pFormat, int pType, long pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glGetTexImage(pTex, pLevel, pFormat, pType, pPixels);
   }

   public static void _viewport(int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GlStateManager.Viewport.INSTANCE.x = pX;
      GlStateManager.Viewport.INSTANCE.y = pY;
      GlStateManager.Viewport.INSTANCE.width = pWidth;
      GlStateManager.Viewport.INSTANCE.height = pHeight;
      GL11.glViewport(pX, pY, pWidth, pHeight);
   }

   public static void _colorMask(boolean pRed, boolean pGreen, boolean pBlue, boolean pAlpha) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pRed != COLOR_MASK.red || pGreen != COLOR_MASK.green || pBlue != COLOR_MASK.blue || pAlpha != COLOR_MASK.alpha) {
         COLOR_MASK.red = pRed;
         COLOR_MASK.green = pGreen;
         COLOR_MASK.blue = pBlue;
         COLOR_MASK.alpha = pAlpha;
         GL11.glColorMask(pRed, pGreen, pBlue, pAlpha);
      }

   }

   public static void _stencilFunc(int pFunc, int pRef, int pMask) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pFunc != STENCIL.func.func || pFunc != STENCIL.func.ref || pFunc != STENCIL.func.mask) {
         STENCIL.func.func = pFunc;
         STENCIL.func.ref = pRef;
         STENCIL.func.mask = pMask;
         GL11.glStencilFunc(pFunc, pRef, pMask);
      }

   }

   public static void _stencilMask(int pMask) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pMask != STENCIL.mask) {
         STENCIL.mask = pMask;
         GL11.glStencilMask(pMask);
      }

   }

   public static void _stencilOp(int pSfail, int pDpfail, int pDppass) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (pSfail != STENCIL.fail || pDpfail != STENCIL.zfail || pDppass != STENCIL.zpass) {
         STENCIL.fail = pSfail;
         STENCIL.zfail = pDpfail;
         STENCIL.zpass = pDppass;
         GL11.glStencilOp(pSfail, pDpfail, pDppass);
      }

   }

   public static void _clearDepth(double pDepth) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glClearDepth(pDepth);
   }

   public static void _clearColor(float pRed, float pGreen, float pBlue, float pAlpha) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glClearColor(pRed, pGreen, pBlue, pAlpha);
   }

   public static void _clearStencil(int pIndex) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glClearStencil(pIndex);
   }

   public static void _clear(int pMask, boolean pCheckError) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glClear(pMask);
      if (pCheckError) {
         _getError();
      }

   }

   public static void _glDrawPixels(int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glDrawPixels(pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void _vertexAttribPointer(int pIndex, int pSize, int pType, boolean pNormalized, int pStride, long pPointer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glVertexAttribPointer(pIndex, pSize, pType, pNormalized, pStride, pPointer);
   }

   public static void _vertexAttribIPointer(int pIndex, int pSize, int pType, int pStride, long pPointer) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL30.glVertexAttribIPointer(pIndex, pSize, pType, pStride, pPointer);
   }

   public static void _enableVertexAttribArray(int pIndex) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glEnableVertexAttribArray(pIndex);
   }

   public static void _disableVertexAttribArray(int pIndex) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL20.glDisableVertexAttribArray(pIndex);
   }

   public static void _drawElements(int pMode, int pCount, int pType, long pIndices) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glDrawElements(pMode, pCount, pType, pIndices);
   }

   public static void _pixelStore(int pParameterName, int pParam) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GL11.glPixelStorei(pParameterName, pParam);
   }

   public static void _readPixels(int pX, int pY, int pWidth, int pHeight, int pFormat, int pType, ByteBuffer pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glReadPixels(pX, pY, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void _readPixels(int pX, int pY, int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glReadPixels(pX, pY, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static int _getError() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL11.glGetError();
   }

   public static String _getString(int pName) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GL11.glGetString(pName);
   }

   public static int _getInteger(int pPname) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GL11.glGetInteger(pPname);
   }

   @OnlyIn(Dist.CLIENT)
   static class BlendState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
      public int srcRgb = 1;
      public int dstRgb = 0;
      public int srcAlpha = 1;
      public int dstAlpha = 0;
   }

   @OnlyIn(Dist.CLIENT)
   static class BooleanState {
      private final int state;
      private boolean enabled;

      public BooleanState(int pState) {
         this.state = pState;
      }

      public void disable() {
         this.setEnabled(false);
      }

      public void enable() {
         this.setEnabled(true);
      }

      public void setEnabled(boolean pEnabled) {
         RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
         if (pEnabled != this.enabled) {
            this.enabled = pEnabled;
            if (pEnabled) {
               GL11.glEnable(this.state);
            } else {
               GL11.glDisable(this.state);
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   static class ColorLogicState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
      public int op = 5379;
   }

   @OnlyIn(Dist.CLIENT)
   static class ColorMask {
      public boolean red = true;
      public boolean green = true;
      public boolean blue = true;
      public boolean alpha = true;
   }

   @OnlyIn(Dist.CLIENT)
   static class CullState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
      public int mode = 1029;
   }

   @OnlyIn(Dist.CLIENT)
   static class DepthState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
      public boolean mask = true;
      public int func = 513;
   }

   @OnlyIn(Dist.CLIENT)
   @DontObfuscate
   public static enum DestFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private DestFactor(int p_84652_) {
         this.value = p_84652_;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum LogicOp {
      AND(5377),
      AND_INVERTED(5380),
      AND_REVERSE(5378),
      CLEAR(5376),
      COPY(5379),
      COPY_INVERTED(5388),
      EQUIV(5385),
      INVERT(5386),
      NAND(5390),
      NOOP(5381),
      NOR(5384),
      OR(5383),
      OR_INVERTED(5389),
      OR_REVERSE(5387),
      SET(5391),
      XOR(5382);

      public final int value;

      private LogicOp(int p_84721_) {
         this.value = p_84721_;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class PolygonOffsetState {
      public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
      public final GlStateManager.BooleanState line = new GlStateManager.BooleanState(10754);
      public float factor;
      public float units;
   }

   @OnlyIn(Dist.CLIENT)
   static class ScissorState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3089);
   }

   @OnlyIn(Dist.CLIENT)
   @DontObfuscate
   public static enum SourceFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_ALPHA_SATURATE(776),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private SourceFactor(int p_84757_) {
         this.value = p_84757_;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class StencilFunc {
      public int func = 519;
      public int ref;
      public int mask = -1;
   }

   @OnlyIn(Dist.CLIENT)
   static class StencilState {
      public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
      public int mask = -1;
      public int fail = 7680;
      public int zfail = 7680;
      public int zpass = 7680;
   }

   @OnlyIn(Dist.CLIENT)
   static class TextureState {
      public boolean enable;
      public int binding;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Viewport {
      INSTANCE;

      protected int x;
      protected int y;
      protected int width;
      protected int height;

      public static int x() {
         return INSTANCE.x;
      }

      public static int y() {
         return INSTANCE.y;
      }

      public static int width() {
         return INSTANCE.width;
      }

      public static int height() {
         return INSTANCE.height;
      }
   }
}