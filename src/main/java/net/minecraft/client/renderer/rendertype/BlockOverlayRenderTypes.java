package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.LayeringTransform;

public final class BlockOverlayRenderTypes {

    private static final RenderType[] CACHE = new RenderType[5];
    private static RenderType SIMPLE_CACHE = null;

    private BlockOverlayRenderTypes() {}

    public static RenderType get(int style) {
        if (style < 1) style = 1;
        if (style > 4) style = 4;
        RenderType t = CACHE[style];
        if (t == null) {
            t = build(style);
            CACHE[style] = t;
        }
        return t;
    }

    private static RenderType build(int style) {
        RenderPipeline pipeline = RenderPipeline.builder()
            .withLocation("photon/block_" + style)
            .withVertexShader("photon/photon_block")
            .withFragmentShader("photon/photon_block_" + style)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();
        RenderSetup setup = RenderSetup.builder(pipeline).createRenderSetup();
        return RenderType.create("photon_block_" + style, setup);
    }

    public static RenderType getSimple() {
        if (SIMPLE_CACHE == null) {
            SIMPLE_CACHE = buildSimple();
        }
        return SIMPLE_CACHE;
    }

    private static RenderType buildSimple() {
        RenderPipeline pipeline = RenderPipeline.builder()
            .withLocation("photon/block_overlay_simple")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();
        RenderSetup setup = RenderSetup.builder(pipeline)
            .sortOnUpload()
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup();
        return RenderType.create("photon_block_overlay_simple", setup);
    }
}
