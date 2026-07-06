package fun.photon.module.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventRender2D;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.ColorSetting;
import fun.photon.setting.ModeSetting;
import fun.photon.setting.NumberSetting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.BlockOverlayRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@ModuleInfo(
        name = "BlockOverlay",
        category = Category.RENDER,
        description = "Заливка наводимого блока"
)
public class BlockOverlay extends Module {

    public static BlockOverlay INSTANCE;

    private static final String[] MODES = {"Обычный", "Шейдер"};
    private static final String[] STYLES = {"Заливка", "Грани", "Пульс", "Поток"};

    private final ModeSetting mode = register(new ModeSetting("Режим", "Обычный", MODES));
    private final ModeSetting style = register(new ModeSetting("Стиль", "Грани", STYLES));
    private final ColorSetting color = register(new ColorSetting("Цвет", 0x6600E5FF));
    private final NumberSetting opacity = register(new NumberSetting("Прозрачность", 40, 0, 100, 1));

    private boolean drawn;

    public BlockOverlay() {
        INSTANCE = this;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        drawn = false;
    }

    public static void render(PoseStack pose) {
        BlockOverlay m = INSTANCE;
        if (m == null || !m.isEnabled() || m.drawn) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        HitResult hr = mc.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || hr.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = bhr.getBlockPos();
        VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
        AABB bb = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();

        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 c = cam.position();
        double e = 0.002;
        float x0 = (float) (pos.getX() + bb.minX - e - c.x);
        float y0 = (float) (pos.getY() + bb.minY - e - c.y);
        float z0 = (float) (pos.getZ() + bb.minZ - e - c.z);
        float x1 = (float) (pos.getX() + bb.maxX + e - c.x);
        float y1 = (float) (pos.getY() + bb.maxY + e - c.y);
        float z1 = (float) (pos.getZ() + bb.maxZ + e - c.z);

        int base = m.color.get();
        float op = m.opacity.getFloat() / 100f;
        int a = (int) (((base >>> 24) & 0xFF) * op);
        if (a <= 1) { m.drawn = true; return; }
        int argb = (a << 24) | (base & 0xFFFFFF);

        MultiBufferSource.BufferSource src = mc.renderBuffers().bufferSource();
        PoseStack.Pose p = pose.last();

        if (m.mode.is("Шейдер")) {
            int si = m.style.getOptions().indexOf(m.style.getValue());
            VertexConsumer vc = src.getBuffer(BlockOverlayRenderTypes.get((si < 0 ? 0 : si) + 1));
            shaderBox(vc, p, x0, y0, z0, x1, y1, z1, argb);
        } else {
            VertexConsumer vc = src.getBuffer(RenderTypes.debugFilledBox());
            fillBox(vc, p, x0, y0, z0, x1, y1, z1, argb);
        }
        m.drawn = true;
    }

    private static void fillBox(VertexConsumer vc, PoseStack.Pose p,
                                float x0, float y0, float z0, float x1, float y1, float z1, int c) {
        q(vc, p, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, c);
        q(vc, p, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
        q(vc, p, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
        q(vc, p, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, c);
        q(vc, p, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, c);
        q(vc, p, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
    }

    private static void q(VertexConsumer vc, PoseStack.Pose p,
                          float ax, float ay, float az, float bx, float by, float bz,
                          float cx, float cy, float cz, float dx, float dy, float dz, int col) {
        vc.addVertex(p, ax, ay, az).setColor(col);
        vc.addVertex(p, bx, by, bz).setColor(col);
        vc.addVertex(p, cx, cy, cz).setColor(col);
        vc.addVertex(p, dx, dy, dz).setColor(col);
    }

    private static void shaderBox(VertexConsumer vc, PoseStack.Pose p,
                                  float x0, float y0, float z0, float x1, float y1, float z1, int c) {
        qs(vc, p, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, c);
        qs(vc, p, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
        qs(vc, p, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
        qs(vc, p, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, c);
        qs(vc, p, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, c);
        qs(vc, p, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
    }

    private static void qs(VertexConsumer vc, PoseStack.Pose p,
                           float ax, float ay, float az, float bx, float by, float bz,
                           float cx, float cy, float cz, float dx, float dy, float dz, int col) {
        v(vc, p, ax, ay, az, 0, 0, col);
        v(vc, p, bx, by, bz, 1, 0, col);
        v(vc, p, cx, cy, cz, 1, 1, col);
        v(vc, p, dx, dy, dz, 0, 1, col);
    }

    private static void v(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z,
                          float u, float vv, int col) {
        vc.addVertex(p, x, y, z).setUv(u, vv).setColor(col);
    }
}
