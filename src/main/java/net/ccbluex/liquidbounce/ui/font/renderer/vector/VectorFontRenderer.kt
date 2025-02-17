/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractAwtFontRender
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * 矢量字体渲染器
 * @author liulihaocai
 */
class VectorFontRenderer(font: Font) : AbstractAwtFontRender(font) {

    private val epsilon = font.size * 0.02

    override fun drawChar(char: String, x: Float, y: Float): Int {
        if (cachedChars.containsKey(char)) {
            val cached = cachedChars[char]!! as CachedVectorFont

            GL11.glCallList(cached.displayList)
            GL11.glCallList(cached.displayList) // TODO: stupid solutions, find a better way
            cached.lastUsage = System.currentTimeMillis()

            return cached.width
        }

        val list = GL11.glGenLists(1)
        GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE)

        RenderUtils.drawAWTShape(font.createGlyphVector(FontRenderContext(AffineTransform(), true, false), char).getOutline(x, y + 1f + fontMetrics.ascent), epsilon)

        GL11.glEndList()

        val width = fontMetrics.stringWidth(char)
        cachedChars[char] = CachedVectorFont(list, width)

        return width
    }

    override fun preGlHints() {
        GlStateManager.enableColorMaterial()
        GlStateManager.enableAlpha()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        RenderUtils.clearCaps()
        RenderUtils.enableGlCap(GL13.GL_MULTISAMPLE)
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST)
        RenderUtils.enableGlCap(GL11.GL_POLYGON_SMOOTH)
        RenderUtils.disableGlCap(GL11.GL_CULL_FACE) // 不要剔除模型的背面
    }

    override fun postGlHints() {
        RenderUtils.resetCaps()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}