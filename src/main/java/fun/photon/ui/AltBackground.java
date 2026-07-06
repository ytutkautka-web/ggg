package fun.photon.ui;

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

public final class AltBackground {

    private AltBackground() {}

    private static final RenderPipeline BG = RenderPipeline.builder()
            .withLocation("photon/alt_bg")
            .withVertexShader("photon/photon_alt_bg")
            .withFragmentShader("photon/photon_alt_bg")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("PhotonAltBG", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();

    private static final CachedOrthoProjectionMatrixBuffer PROJECTION =
            new CachedOrthoProjectionMatrixBuffer("photon-alt-bg", -1000.0F, 1000.0F, true);

    private static final long START = System.currentTimeMillis();

    private static GpuBuffer quadBuffer;
    private static GpuBuffer uboBuffer;

    private static float smX = 0.5f, smY = 0.5f;

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
                        .createBuffer(() -> "Photon alt bg quad", GpuBuffer.USAGE_VERTEX, bb);
            }
        }
        if (uboBuffer == null) {
            uboBuffer = RenderSystem.getDevice()
                    .createBuffer(() -> "Photon alt bg UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UBO_SIZE);
        }
    }

    public static void render(double mouseX, double mouseY, float alpha) {
        ensureResources();

        Window win = Minecraft.getInstance().getWindow();
        RenderSystem.setProjectionMatrix(
                PROJECTION.getBuffer(win.getWidth(), win.getHeight()),
                ProjectionType.ORTHOGRAPHIC
        );

        float t = (System.currentTimeMillis() - START) / 1000f;

        float nx = (float) (mouseX / Math.max(win.getWidth(), 1));
        float ny = (float) (mouseY / Math.max(win.getHeight(), 1));

        smX += (nx - smX) * 0.08f;
        smY += (ny - smY) * 0.08f;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb = Std140Builder.onStack(stack, UBO_SIZE)
                    .putVec4(win.getWidth(), win.getHeight(), t, 0f)
                    .putVec4(smX, smY, alpha, 0f)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), bb);
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);

        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Photon alt bg", target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(BG);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("PhotonAltBG", uboBuffer);
            pass.setVertexBuffer(0, quadBuffer);
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, 6, 1);
        }
    }
}
