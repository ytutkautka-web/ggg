package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class ChamsRenderTypes {

    private static final RenderType[][] CACHE = new RenderType[12][2];
    private static final Map<String, RenderType> ITEM_CACHE = new HashMap<>();

    private ChamsRenderTypes() {}

    public static RenderType getItem(int style, Identifier atlas) {
        if (style < 0) style = 0;
        if (style > 11) style = 11;
        String key = style + "|" + atlas;
        RenderType t = ITEM_CACHE.get(key);
        if (t == null) {
            t = buildItem(style, atlas);
            ITEM_CACHE.put(key, t);
        }
        return t;
    }

    private static RenderType buildItem(int style, Identifier atlas) {
        RenderPipeline pipeline = RenderPipeline.builder()
            .withLocation("photon/chams_item_" + style)
            .withVertexShader("photon/photon_chams_item")
            .withFragmentShader("photon/photon_chams_item_" + style)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
            .build();
        RenderSetup setup = RenderSetup.builder(pipeline).withTexture("Sampler0", atlas).createRenderSetup();
        return RenderType.create("photon_chams_item_" + style + "_" + atlas.getPath(), setup);
    }

    public static RenderType get(int style, boolean through) {
        if (style < 0) style = 0;
        if (style > 11) style = 11;
        int d = through ? 1 : 0;
        RenderType t = CACHE[style][d];
        if (t == null) {
            t = build(style, through);
            CACHE[style][d] = t;
        }
        return t;
    }

    private static RenderType build(int style, boolean through) {
        RenderPipeline pipeline = RenderPipeline.builder()
            .withLocation("photon/chams_" + style + (through ? "_through" : "_depth"))
            .withVertexShader("photon/photon_chams")
            .withFragmentShader("photon/photon_chams_" + style)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(through ? DepthTestFunction.NO_DEPTH_TEST : DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
            .build();
        RenderSetup setup = RenderSetup.builder(pipeline).createRenderSetup();
        return RenderType.create("photon_chams_" + style + (through ? "_t" : "_d"), setup);
    }
}
