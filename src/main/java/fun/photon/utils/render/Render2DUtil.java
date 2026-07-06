package fun.photon.utils.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public final class Render2DUtil {

    private Render2DUtil() {}

    public static float QUALITY = 1f;

    private static final RenderPipeline ROUNDED = RenderPipeline.builder()
            .withLocation("photon/rounded")
            .withVertexShader("photon/photon_rounded")
            .withFragmentShader("photon/photon_rounded")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("PhotonShape", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final int UBO_SIZE = new Std140SizeCalculator()
            .putVec4().putVec4().putVec4().putVec4().putVec4().get();

    private static final CachedOrthoProjectionMatrixBuffer PROJECTION =
            new CachedOrthoProjectionMatrixBuffer("photon", -1000.0F, 1000.0F, true);

    private static GpuBuffer quadBuffer;
    private static GpuBuffer uboBuffer;

    private static void ensureResources() {
        if (quadBuffer == null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer bb = stack.malloc(4 * 3 * Float.BYTES);
                bb.putFloat(0f).putFloat(0f).putFloat(0f);
                bb.putFloat(0f).putFloat(1f).putFloat(0f);
                bb.putFloat(1f).putFloat(1f).putFloat(0f);
                bb.putFloat(1f).putFloat(0f).putFloat(0f);
                bb.flip();
                quadBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> "Photon quad", GpuBuffer.USAGE_VERTEX, bb);
            }
        }
        if (uboBuffer == null) {
            uboBuffer = RenderSystem.getDevice()
                    .createBuffer(() -> "Photon shape UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UBO_SIZE);
        }
    }

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    public static int screenWidth() {
        return window().getWidth();
    }

    public static int screenHeight() {
        return window().getHeight();
    }

    public static void rounded(float x, float y, float width, float height,
                               float rTopRight, float rBottomRight, float rTopLeft, float rBottomLeft,
                               float softness, int fillColor, int outlineColor, float thickness) {
        ensureResources();

        float s = RenderTransform.scale;
        if (s != 1f) {
            x = RenderTransform.tx(x);
            y = RenderTransform.ty(y);
            width *= s;
            height *= s;
            rTopRight *= s; rBottomRight *= s; rTopLeft *= s; rBottomLeft *= s;
            thickness *= s;
        }
        float ga = RenderTransform.alpha;
        softness *= QUALITY;

        Window win = window();
        RenderSystem.setProjectionMatrix(
                PROJECTION.getBuffer(win.getWidth(), win.getHeight()),
                ProjectionType.ORTHOGRAPHIC
        );

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb = Std140Builder.onStack(stack, UBO_SIZE)
                    .putVec4(x, y, width, height)
                    .putVec4(rTopRight, rBottomRight, rTopLeft, rBottomLeft)
                    .putVec4(ColorUtil.redf(fillColor), ColorUtil.greenf(fillColor), ColorUtil.bluef(fillColor), ColorUtil.alphaf(fillColor) * ga)
                    .putVec4(ColorUtil.redf(outlineColor), ColorUtil.greenf(outlineColor), ColorUtil.bluef(outlineColor), ColorUtil.alphaf(outlineColor) * ga)
                    .putVec4(thickness, softness, 0f, 0f)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), bb);
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);

        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Photon rounded", target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(ROUNDED);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("PhotonShape", uboBuffer);
            ScissorUtil.apply(pass);
            pass.setVertexBuffer(0, quadBuffer);
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, 6, 1);
        }
    }

    public static void roundedRect(float x, float y, float w, float h, float radius, int color) {
        rounded(x, y, w, h, radius, radius, radius, radius, 1.0f, color, 0, 0f);
    }

    public static void rect(float x, float y, float w, float h, int color) {
        rounded(x, y, w, h, 0f, 0f, 0f, 0f, 1.0f, color, 0, 0f);
    }

    public static void roundedOutline(float x, float y, float w, float h, float radius, float thickness, int color) {
        rounded(x, y, w, h, radius, radius, radius, radius, 1.0f, 0, color, thickness);
    }

    public static void circle(float centerX, float centerY, float radius, int color) {
        rounded(centerX - radius, centerY - radius, radius * 2f, radius * 2f,
                radius, radius, radius, radius, 1.0f, color, 0, 0f);
    }
}
