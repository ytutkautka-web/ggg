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
import fun.photon.Photon;
import fun.photon.utils.render.svg.SvgRasterizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public final class ImageUtil {

    private ImageUtil() {}

    private static final RenderPipeline IMAGE = RenderPipeline.builder()
            .withLocation("photon/image")
            .withVertexShader("photon/photon_image")
            .withFragmentShader("photon/photon_image")
            .withUniform("Projection", com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final CachedOrthoProjectionMatrixBuffer PROJECTION =
            new CachedOrthoProjectionMatrixBuffer("photon-image", -1000.0F, 1000.0F, true);

    private static final Map<String, DynamicTexture> PNG_CACHE = new HashMap<>();
    private static final Map<String, DynamicTexture> SVG_CACHE = new HashMap<>();

    private static com.mojang.blaze3d.textures.GpuSampler linearSampler() {
        return RenderSystem.getSamplerCache()
                .getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR);
    }

    private static GpuBuffer vertexBuffer;
    private static int vertexCapacity;

    public static void drawPng(String name, float x, float y, float w, float h, int color) {

        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        String key = name + "@" + iw + "x" + ih;
        if (!PNG_CACHE.containsKey(key)) {
            PNG_CACHE.put(key, loadPng(name, iw, ih));
        }
        DynamicTexture tex = PNG_CACHE.get(key);
        if (tex != null) draw(tex, x, y, w, h, color);
    }

    private static final int[] PRELOAD_SIZES = {8, 10, 12, 14, 16, 18, 20, 24, 28, 32};
    private static volatile boolean preloadStarted;
    private static final Map<String, byte[]> SVG_BYTES = new java.util.concurrent.ConcurrentHashMap<>();

    public static void drawSvg(String name, float x, float y, float w, float h, int color) {
        if (!preloadStarted) preloadAsync();
        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        String key = name + "@" + iw + "x" + ih;
        DynamicTexture tex = SVG_CACHE.get(key);
        if (tex == null && !SVG_CACHE.containsKey(key)) {
            byte[] pre = SVG_BYTES.remove(key);
            tex = pre != null ? textureFromPngSafe(name, pre) : loadSvg(name, iw, ih);
            SVG_CACHE.put(key, tex);
        }
        if (tex != null) draw(tex, x, y, w, h, color);
    }

    public static void preloadAsync() {
        if (preloadStarted) return;
        preloadStarted = true;
        Thread t = new Thread(() -> {
            try {
                int n = 0;
                for (String name : listSvgNames()) {
                    String path = Photon.imageResource(name);
                    for (int s : PRELOAD_SIZES) {
                        String key = name + "@" + s + "x" + s;
                        if (SVG_BYTES.containsKey(key) || SVG_CACHE.containsKey(key)) continue;
                        try {
                            BufferedImage img = SvgRasterizer.rasterize(path, s, s);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(img, "png", baos);
                            SVG_BYTES.put(key, baos.toByteArray());
                            n++;
                        } catch (Throwable ignored) {}
                    }
                }
                System.out.println("[Photon] pre-rasterized " + n + " svg images");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, "Photon-SvgPreload");
        t.setDaemon(true);
        t.start();
    }

    private static DynamicTexture textureFromPngSafe(String name, byte[] bytes) {
        try {
            return textureFromPng(name, bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static java.util.List<String> listSvgNames() throws Exception {
        java.util.List<String> names = new java.util.ArrayList<>();
        String root = Photon.IMAGES_PATH;
        java.net.URL url = ImageUtil.class.getResource(root);
        if (url == null) return names;
        if ("file".equals(url.getProtocol())) {
            java.nio.file.Path base = java.nio.file.Paths.get(url.toURI());
            try (java.util.stream.Stream<java.nio.file.Path> st = java.nio.file.Files.walk(base)) {
                st.filter(p -> p.toString().toLowerCase().endsWith(".svg")).forEach(p ->
                        names.add(base.relativize(p).toString().replace('\\', '/')));
            }
        } else if ("jar".equals(url.getProtocol())) {
            String s = url.getPath();
            String jarPath = s.substring(5, s.indexOf("!"));
            String prefix = (root.startsWith("/") ? root.substring(1) : root) + "/";
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                java.util.Enumeration<java.util.jar.JarEntry> e = jar.entries();
                while (e.hasMoreElements()) {
                    String n = e.nextElement().getName();
                    if (n.startsWith(prefix) && n.toLowerCase().endsWith(".svg")) {
                        names.add(n.substring(prefix.length()));
                    }
                }
            }
        }
        return names;
    }

    private static DynamicTexture loadPng(String name, int w, int h) {
        String path = Photon.imageResource(name);
        try (InputStream is = ImageUtil.class.getResourceAsStream(path)) {
            if (is == null) {
                System.out.println("[Photon] png not found on classpath: " + path);
                return null;
            }
            BufferedImage src = ImageIO.read(is);
            if (src == null) {
                System.out.println("[Photon] failed to decode png: " + path);
                return null;
            }
            BufferedImage scaled = (src.getWidth() == w && src.getHeight() == h)
                    ? src : scale(src, w, h);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(scaled, "png", baos);
            return textureFromPng(name, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage scale(BufferedImage src, int tw, int th) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage cur = src;
        while (w / 2 >= tw && h / 2 >= th) {
            w /= 2;
            h /= 2;
            cur = scaleStep(cur, w, h);
        }
        return scaleStep(cur, tw, th);
    }

    private static BufferedImage scaleStep(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static DynamicTexture loadSvg(String name, int w, int h) {
        BufferedImage img = SvgRasterizer.rasterize(Photon.imageResource(name), w, h);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return textureFromPng(name, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DynamicTexture textureFromPng(String name, byte[] pngBytes) throws Exception {
        ByteBuffer data = BufferUtils.createByteBuffer(pngBytes.length).put(pngBytes);
        data.flip();
        return new DynamicTexture(() -> "photon-image-" + name, NativeImage.read(data));
    }

    public static void drawTexture(DynamicTexture texture, float x, float y, float w, float h, int color) {
        if (texture != null) draw(texture, x, y, w, h, color);
    }

    private static com.mojang.blaze3d.textures.GpuSampler nearestSampler() {
        return RenderSystem.getSamplerCache()
                .getClampToEdge(com.mojang.blaze3d.textures.FilterMode.NEAREST);
    }

    public static void drawTextureRegion(net.minecraft.resources.Identifier id, float x, float y, float w, float h,
                                         float u0, float v0, float u1, float v1, int color) {
        net.minecraft.client.renderer.texture.AbstractTexture tex =
                Minecraft.getInstance().getTextureManager().getTexture(id);
        if (tex == null) return;
        drawView(tex.getTextureView(), nearestSampler(), x, y, w, h, u0, v0, u1, v1, color);
    }

    private static void drawView(com.mojang.blaze3d.textures.GpuTextureView view,
                                 com.mojang.blaze3d.textures.GpuSampler sampler,
                                 float x, float y, float w, float h,
                                 float u0, float v0, float u1, float v1, int color) {
        int a = Math.round(ColorUtil.alpha(color) * RenderTransform.alpha);
        int r = ColorUtil.red(color);
        int g = ColorUtil.green(color);
        int b = ColorUtil.blue(color);

        float x0 = RenderTransform.tx(x);
        float x1v = RenderTransform.tx(x + w);
        float y0 = RenderTransform.ty(y);
        float y1v = RenderTransform.ty(y + h);

        BufferBuilder bb = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bb.addVertex(x0, y1v, 0).setUv(u0, v1).setColor(r, g, b, a);
        bb.addVertex(x1v, y1v, 0).setUv(u1, v1).setColor(r, g, b, a);
        bb.addVertex(x1v, y0, 0).setUv(u1, v0).setColor(r, g, b, a);
        bb.addVertex(x0, y0, 0).setUv(u0, v0).setColor(r, g, b, a);

        MeshData mesh = bb.buildOrThrow();
        try {
            ByteBuffer vb = mesh.vertexBuffer();
            int needed = vb.remaining();
            if (vertexBuffer == null || vertexCapacity < needed) {
                if (vertexBuffer != null) vertexBuffer.close();
                vertexCapacity = needed;
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Photon image VB", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, needed);
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
                    .createRenderPass(() -> "Photon image", target.getColorTextureView(), OptionalInt.empty())) {
                pass.setPipeline(IMAGE);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", view, sampler);
                ScissorUtil.apply(pass);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, indexCount, 1);
            }
        } finally {
            mesh.close();
        }
    }

    private static void draw(DynamicTexture texture, float x, float y, float w, float h, int color) {
        int a = Math.round(ColorUtil.alpha(color) * RenderTransform.alpha);
        int r = ColorUtil.red(color);
        int g = ColorUtil.green(color);
        int b = ColorUtil.blue(color);

        float x0 = RenderTransform.tx(x);
        float x1 = RenderTransform.tx(x + w);
        float y0 = RenderTransform.ty(y);
        float y1 = RenderTransform.ty(y + h);

        BufferBuilder bb = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        bb.addVertex(x0, y1, 0).setUv(0f, 1f).setColor(r, g, b, a);
        bb.addVertex(x1, y1, 0).setUv(1f, 1f).setColor(r, g, b, a);
        bb.addVertex(x1, y0, 0).setUv(1f, 0f).setColor(r, g, b, a);
        bb.addVertex(x0, y0, 0).setUv(0f, 0f).setColor(r, g, b, a);

        MeshData mesh = bb.buildOrThrow();
        try {
            ByteBuffer vb = mesh.vertexBuffer();
            int needed = vb.remaining();
            if (vertexBuffer == null || vertexCapacity < needed) {
                if (vertexBuffer != null) vertexBuffer.close();
                vertexCapacity = needed;
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Photon image VB", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, needed);
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
                    .createRenderPass(() -> "Photon image", target.getColorTextureView(), OptionalInt.empty())) {
                pass.setPipeline(IMAGE);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", texture.getTextureView(), linearSampler());
                ScissorUtil.apply(pass);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, indexCount, 1);
            }
        } finally {
            mesh.close();
        }
    }
}
