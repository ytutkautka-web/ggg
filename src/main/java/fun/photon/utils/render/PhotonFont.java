package fun.photon.utils.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class PhotonFont {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "0123456789"
            + "!?@#$%^&*()-_=+[]{}|\\;:'\"<>,./`~©™® ";

    private static final int PAD = 2;

    private static final RenderPipeline TEXT = RenderPipeline.builder()
            .withLocation("photon/text")
            .withVertexShader("photon/photon_text")
            .withFragmentShader("photon/photon_text")
            .withUniform("Projection", com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final CachedOrthoProjectionMatrixBuffer PROJECTION =
            new CachedOrthoProjectionMatrixBuffer("photon-font", -1000.0F, 1000.0F, true);

    private final Font font;
    private final boolean antiAliasing;
    private final Map<Character, Glyph> glyphs = new HashMap<>();

    private int imgSize;
    private int fontHeight;
    private int ascent;
    private boolean ready;

    private DynamicTexture texture;
    private GpuBuffer vertexBuffer;
    private int vertexCapacity;

    public PhotonFont(Font font, boolean antiAliasing) {
        this.font = font;
        this.antiAliasing = antiAliasing;
    }

    private void ensureReady() {
        if (ready) return;
        generateAtlas();
        ready = true;
    }

    private void generateAtlas() {
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), antiAliasing, true);
        double maxW = 0, maxH = 0;
        for (int i = 0; i < CHARS.length(); i++) {
            Rectangle2D b = font.getStringBounds(String.valueOf(CHARS.charAt(i)), frc);
            maxW = Math.max(maxW, b.getWidth());
            maxH = Math.max(maxH, b.getHeight());
        }
        maxW += PAD * 2 + 2;
        maxH += PAD * 2 + 2;
        imgSize = (int) (Math.ceil(Math.sqrt(maxW * maxW * CHARS.length()) / maxW) * Math.max(maxW, maxH)) + 1;

        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = img.createGraphics();
        gfx.setFont(font);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gfx.setColor(Color.WHITE);

        FontMetrics fm = gfx.getFontMetrics();
        this.fontHeight = fm.getHeight();
        this.ascent = fm.getAscent();

        int posX = 0, posY = 0, rowH = 0;
        int cellH = fontHeight + PAD * 2;
        for (int i = 0; i < CHARS.length(); i++) {
            char ch = CHARS.charAt(i);
            int advance = fm.charWidth(ch);
            int cellW = advance + PAD * 2;

            if (posX + cellW >= imgSize) {
                posX = 0;
                posY += rowH;
                rowH = 0;
            }

            gfx.drawString(String.valueOf(ch), posX + PAD, posY + PAD + ascent);

            Glyph g = new Glyph();
            g.u0 = posX / (float) imgSize;
            g.v0 = posY / (float) imgSize;
            g.u1 = (posX + cellW) / (float) imgSize;
            g.v1 = (posY + cellH) / (float) imgSize;
            g.w = cellW;
            g.h = cellH;
            g.advance = advance;
            glyphs.put(ch, g);

            posX += cellW;
            rowH = Math.max(rowH, cellH);
        }
        gfx.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] bytes = baos.toByteArray();
            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            texture = new DynamicTexture(() -> "photon-font", NativeImage.read(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getWidth(String text) {
        ensureReady();
        float w = 0;
        for (int i = 0; i < text.length(); i++) {
            Glyph g = glyphs.get(text.charAt(i));
            if (g != null) w += g.advance;
        }
        return w;
    }

    public float getHeight() {
        ensureReady();
        return fontHeight;
    }

    public float drawString(String text, float x, float y, int color) {
        return draw(text, x, y, frac -> color);
    }

    public float drawCenteredString(String text, float cx, float y, int color) {
        return drawString(text, cx - getWidth(text) / 2f, y, color);
    }

    public float drawInRowY(String text, float x, float topY, float rowH, int color) {
        ensureReady();
        float cap = ascent * 0.72f;
        float baseline = topY + rowH / 2f + cap / 2f;
        float drawY = baseline - PAD - ascent;
        return drawString(text, x, drawY, color);
    }

    public float drawCenteredInRow(String text, float cx, float topY, float rowH, int color) {
        return drawInRowY(text, cx - getWidth(text) / 2f, topY, rowH, color);
    }

    public float drawRightInRow(String text, float rightX, float topY, float rowH, int color) {
        return drawInRowY(text, rightX - getWidth(text), topY, rowH, color);
    }

    public float drawRight(String text, float rightX, float y, int color) {
        return drawString(text, rightX - getWidth(text), y, color);
    }

    public float drawGradient(String text, float x, float y, int c1, int c2) {
        return draw(text, x, y, frac -> ColorUtil.interpolate(c1, c2, frac));
    }

    @FunctionalInterface
    private interface ColorAt {
        int color(float frac);
    }

    private float draw(String text, float x, float y, ColorAt colorAt) {
        ensureReady();
        if (texture == null || text.isEmpty()) return x;

        boolean anyDrawable = false;
        for (int i = 0; i < text.length(); i++) {
            if (glyphs.containsKey(text.charAt(i))) { anyDrawable = true; break; }
        }
        if (!anyDrawable) return x;

        float total = getWidth(text);
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float cursor = x;
        for (int i = 0; i < text.length(); i++) {
            Glyph gl = glyphs.get(text.charAt(i));
            if (gl == null) continue;
            int color = colorAt.color(total <= 0 ? 0 : (cursor - x) / total);
            int a = Math.round(ColorUtil.alpha(color) * RenderTransform.alpha);
            int r = ColorUtil.red(color);
            int g = ColorUtil.green(color);
            int b = ColorUtil.blue(color);
            float gx = cursor - PAD;
            float gy = y;
            float x0 = RenderTransform.tx(gx);
            float x1 = RenderTransform.tx(gx + gl.w);
            float y0 = RenderTransform.ty(gy);
            float y1 = RenderTransform.ty(gy + gl.h);
            bb.addVertex(x0, y1, 0).setUv(gl.u0, gl.v1).setColor(r, g, b, a);
            bb.addVertex(x1, y1, 0).setUv(gl.u1, gl.v1).setColor(r, g, b, a);
            bb.addVertex(x1, y0, 0).setUv(gl.u1, gl.v0).setColor(r, g, b, a);
            bb.addVertex(x0, y0, 0).setUv(gl.u0, gl.v0).setColor(r, g, b, a);
            cursor += gl.advance;
        }
        MeshData mesh = bb.buildOrThrow();
        try {
            ByteBuffer vb = mesh.vertexBuffer();
            int needed = vb.remaining();
            if (vertexBuffer == null || vertexCapacity < needed) {
                if (vertexBuffer != null) vertexBuffer.close();
                vertexCapacity = needed;
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Photon text VB", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, needed);
            }
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer.slice(0, needed), vb);

            int indexCount = mesh.drawState().indexCount();
            RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer indexBuffer = indices.getBuffer(indexCount);

            Window win = Minecraft.getInstance().getWindow();
            RenderSystem.setProjectionMatrix(
                    PROJECTION.getBuffer(win.getWidth(), win.getHeight()), ProjectionType.ORTHOGRAPHIC);

            RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Photon text", target.getColorTextureView(), OptionalInt.empty())) {
                pass.setPipeline(TEXT);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
                ScissorUtil.apply(pass);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, indexCount, 1);
            }
        } finally {
            mesh.close();
        }
        return cursor;
    }

    private static final class Glyph {
        float u0, v0, u1, v1;
        int w, h, advance;
    }
}
