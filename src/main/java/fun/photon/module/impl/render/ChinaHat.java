package fun.photon.module.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fun.photon.clickgui.Palette;
import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventRender2D;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.BooleanSetting;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@ModuleInfo(
        name = "ChinaHat",
        category = Category.RENDER,
        description = "Китайская шляпа над игроками"
)
public class ChinaHat extends Module {

    public static ChinaHat INSTANCE;

    private static final int SEGMENTS = 90;
    private static final float PI_SEGMENT = Mth.TWO_PI / SEGMENTS;

    private final BooleanSetting self = register(new BooleanSetting("Себя", true));
    private final BooleanSetting players = register(new BooleanSetting("Игроки", true));
    private final BooleanSetting friends = register(new BooleanSetting("Друзья", true));
    private final BooleanSetting outline = register(new BooleanSetting("Обводка", true));
    private final NumberSetting opacity = register(new NumberSetting("Прозрачность", 50, 5, 100, 5));
    private final NumberSetting height = register(new NumberSetting("Высота", 0, -60, 60, 1));

    private final java.util.Set<Integer> drawnThisFrame = new java.util.HashSet<>();
    private static final java.util.Map<Integer, float[]> headPose = new java.util.HashMap<>();

    public ChinaHat() {
        INSTANCE = this;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        drawnThisFrame.clear();
    }

    public static void captureHead(ModelPart head, AvatarRenderState av) {
        headPose.put(av.id, new float[]{head.x, head.y, head.z, head.xRot, head.yRot, head.zRot});
    }

    public static void apply(SubmitNodeStorage.ModelSubmit<?> submit, PoseStack pose, MultiBufferSource.BufferSource src) {
        ChinaHat m = INSTANCE;
        if (m == null || !m.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        if (!(submit.state() instanceof AvatarRenderState av)) return;

        boolean isSelf = mc.player != null && av.id == mc.player.getId();
        if (isSelf) {
            if (!m.self.get()) return;
        } else {
            boolean isFriend = m.isFriendState(av);
            if (isFriend) {
                if (!m.friends.get()) return;
            } else if (!m.players.get()) {
                return;
            }
        }

        if (!m.drawnThisFrame.add(av.id)) return;
        if (av.isInvisible) return;

        float radius = av.boundingBoxWidth;

        Matrix4f mat = new Matrix4f(pose.last().pose());

        float[] hp = headPose.get(av.id);
        if (hp != null) {
            mat.translate(hp[0] / 16f, hp[1] / 16f, hp[2] / 16f);
            mat.rotate(new Quaternionf().rotationZYX(hp[5], hp[4], hp[3]));
        }

        mat.translate(0, m.height.getFloat() / 100f, 0);
        mat.scale(1, -1, 1);

        m.renderChinaHat(mat, src, radius, m.opacity.getFloat() / 100f, m.outline.get());
    }

    private void renderChinaHat(Matrix4f mat, MultiBufferSource.BufferSource src, float radius, float alpha, boolean drawOutline) {
        float coneHeight = 0.35f;
        long time = System.currentTimeMillis();

        VertexConsumer vcFilled = src.getBuffer(RenderTypes.debugQuads());

        for (int i = 0; i < SEGMENTS; i++) {
            float angle1 = i * PI_SEGMENT;
            float angle2 = (i + 1) * PI_SEGMENT;

            float x1 = Mth.sin(angle1) * radius;
            float z1 = Mth.cos(angle1) * radius;
            float x2 = Mth.sin(angle2) * radius;
            float z2 = Mth.cos(angle2) * radius;

            int colorAngle = i * 4;
            int baseColor = ColorUtil.interpolate(
                    Palette.ACCENT,
                    ColorUtil.multAlpha(Palette.ACCENT, 0.5f) | 0xFF000000,
                    (float) ((Math.sin(colorAngle / 180.0 * Math.PI + time / 400.0) + 1.0) / 2.0)
            );
            int apexColor = Palette.ACCENT;

            int fillColor = applyAlpha(baseColor, alpha * 0.5f);
            int fillApex = applyAlpha(apexColor, alpha);

            vcFilled.addVertex(mat, x2, 0, z2).setColor(fillColor);
            vcFilled.addVertex(mat, x1, 0, z1).setColor(fillColor);
            vcFilled.addVertex(mat, 0, coneHeight, 0).setColor(fillApex);
            vcFilled.addVertex(mat, 0, coneHeight, 0).setColor(fillApex);
        }

        if (drawOutline) {
            VertexConsumer vcOutline = src.getBuffer(RenderTypes.debugQuads());

            for (int i = 0; i < SEGMENTS; i++) {
                float angle = i * PI_SEGMENT;
                float x = Mth.sin(angle) * radius;
                float z = Mth.cos(angle) * radius;

                int colorAngle = i * 4;
                int outlineColor = ColorUtil.interpolate(
                        Palette.ACCENT,
                        ColorUtil.multAlpha(Palette.ACCENT, 0.3f) | 0xFF000000,
                        (float) ((Math.sin(colorAngle / 180.0 * Math.PI + time / 500.0) + 1.0) / 2.0)
                );
                int outlineArgb = applyAlpha(outlineColor, alpha);

                float nextAngle = (i + 1) * PI_SEGMENT;
                float xNext = Mth.sin(nextAngle) * radius;
                float zNext = Mth.cos(nextAngle) * radius;

                vcOutline.addVertex(mat, x, 0, z).setColor(outlineArgb);
                vcOutline.addVertex(mat, xNext, 0, zNext).setColor(outlineArgb);
                vcOutline.addVertex(mat, xNext, 0, zNext).setColor(outlineArgb);
                vcOutline.addVertex(mat, x, 0, z).setColor(outlineArgb);
            }
        }
    }

    private boolean isFriendState(AvatarRenderState av) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return false;
            net.minecraft.world.entity.Entity e = mc.level.getEntity(av.id);
            if (e == null) return false;
            return fun.photon.friend.FriendManager.get().isFriend(e.getName().getString());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255f);
        return (a << 24) | (color & 0xFFFFFF);
    }
}