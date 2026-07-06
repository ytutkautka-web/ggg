package fun.photon.module.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.BooleanSetting;
import fun.photon.setting.ColorSetting;
import fun.photon.setting.ModeSetting;
import fun.photon.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.ChamsRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

@ModuleInfo(
        name = "Chams",
        category = Category.RENDER,
        description = "Подсветка моделей через шейдеры"
)
public class Chams extends Module {

    public static Chams INSTANCE;

    private static final String[] STYLES = {
            "Плоский", "Френель", "Сплошной", "Контур", "Неон", "Свет",
            "Тун", "Блик", "Рентген", "Заливка", "Радуга", "Иней"};

    private static final String[] VIS = {"Сквозь стены", "По глубине"};

    private final BooleanSetting self = register(new BooleanSetting("Себя", false));
    private final ModeSetting selfStyle = register(new ModeSetting("Стиль себя", "Неон", STYLES));
    private final ModeSetting selfVis = register(new ModeSetting("Видимость себя", "Сквозь стены", VIS));
    private final ColorSetting selfColor = register(new ColorSetting("Цвет себя", 0xFF00E5FF));
    private final BooleanSetting players = register(new BooleanSetting("Игроки", true));
    private final ModeSetting playersStyle = register(new ModeSetting("Стиль игроков", "Неон", STYLES));
    private final ModeSetting playersVis = register(new ModeSetting("Видимость игроков", "Сквозь стены", VIS));
    private final ColorSetting playersColor = register(new ColorSetting("Цвет игроков", 0xFFFF3050));
    private final BooleanSetting friends = register(new BooleanSetting("Друзья", false));
    private final ModeSetting friendsStyle = register(new ModeSetting("Стиль друзей", "Неон", STYLES));
    private final ModeSetting friendsVis = register(new ModeSetting("Видимость друзей", "Сквозь стены", VIS));
    private final ColorSetting friendsColor = register(new ColorSetting("Цвет друзей", 0xFF00FF62));
    private final BooleanSetting mobs = register(new BooleanSetting("Мобы", false));
    private final ModeSetting mobsStyle = register(new ModeSetting("Стиль мобов", "Неон", STYLES));
    private final ModeSetting mobsVis = register(new ModeSetting("Видимость мобов", "Сквозь стены", VIS));
    private final ColorSetting mobsColor = register(new ColorSetting("Цвет мобов", 0xFF35FF6A));
    private final BooleanSetting items = register(new BooleanSetting("Предметы", false));
    private final ModeSetting itemsStyle = register(new ModeSetting("Стиль предметов", "Неон", STYLES));
    private final ModeSetting itemsVis = register(new ModeSetting("Видимость предметов", "Сквозь стены", VIS));
    private final ColorSetting itemsColor = register(new ColorSetting("Цвет предметов", 0xFFFFC400));
    private final BooleanSetting hit = register(new BooleanSetting("Подсветка хита", true));
    private final ColorSetting hitColor = register(new ColorSetting("Цвет хита", 0xFFFF2A2A));
    private final NumberSetting hitMs = register(new NumberSetting("Длительность хита", 350, 50, 2000, 10));

    private final java.util.Map<Integer, Long> hits = new java.util.concurrent.ConcurrentHashMap<>();

    public static void onHit(net.minecraft.world.entity.Entity entity) {
        Chams m = INSTANCE;
        if (m == null || entity == null) return;
        m.hits.put(entity.getId(), System.currentTimeMillis());
    }

    private float hitFactor(Object state) {
        if (!hit.get() || hits.isEmpty()) return 0f;
        if (!(state instanceof net.minecraft.client.renderer.entity.state.EntityRenderState ers)) return 0f;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return 0f;
        long now = System.currentTimeMillis();
        long dur = Math.max(1, hitMs.getInt());
        float best = 0f;
        java.util.Iterator<java.util.Map.Entry<Integer, Long>> it = hits.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<Integer, Long> e = it.next();
            long age = now - e.getValue();
            if (age > dur) { it.remove(); continue; }
            net.minecraft.world.entity.Entity en = mc.level.getEntity(e.getKey());
            if (en == null) { it.remove(); continue; }
            double dx = en.getX() - ers.x;
            double dy = en.getY() - ers.y;
            double dz = en.getZ() - ers.z;
            if (dx * dx + dy * dy + dz * dz < 0.36) {
                float f = 1f - (float) age / (float) dur;
                if (f > best) best = f;
            }
        }
        return best;
    }
    private final NumberSetting fadeMs = register(new NumberSetting("Анимация", 200, 0, 1000, 10));

    private boolean active;
    private double fadeFrom;
    private double fadeTo;
    private long animStart = System.currentTimeMillis();

    public Chams() {
        INSTANCE = this;
    }

    @Override
    protected void onEnable() {
        active = true;
        startFade(1.0);
    }

    @Override
    protected void onDisable() {
        active = false;
        startFade(0.0);
    }

    private void startFade(double to) {
        fadeFrom = currentFade();
        fadeTo = to;
        animStart = System.currentTimeMillis();
    }

    private double currentFade() {
        long dur = Math.max(1, fadeMs.getInt());
        double p = Math.min(1.0, (System.currentTimeMillis() - animStart) / (double) dur);
        double e = p * p * (3.0 - 2.0 * p);
        return fadeFrom + (fadeTo - fadeFrom) * e;
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

    private int styleIndex(ModeSetting s) {
        int i = s.getOptions().indexOf(s.getValue());
        return i < 0 ? 0 : i;
    }

    private ModeSetting styleFor(int cat) {
        switch (cat) {
            case 0: return selfStyle;
            case 1: return playersStyle;
            case 2: return mobsStyle;
            case 4: return friendsStyle;
            default: return itemsStyle;
        }
    }

    private ModeSetting visFor(int cat) {
        switch (cat) {
            case 0: return selfVis;
            case 1: return playersVis;
            case 2: return mobsVis;
            case 4: return friendsVis;
            default: return itemsVis;
        }
    }

    private int colorFor(int cat) {
        switch (cat) {
            case 0: return selfColor.get();
            case 1: return playersColor.get();
            case 2: return mobsColor.get();
            case 4: return friendsColor.get();
            default: return itemsColor.get();
        }
    }

    private int category(Object state) {
        Minecraft mc = Minecraft.getInstance();
        if (state instanceof AvatarRenderState av) {
            boolean isSelf = mc.player != null && av.id == mc.player.getId();
            if (isSelf) return self.get() ? 0 : -1;
            // Друзья имеют приоритет над обычными игроками
            if (friends.get() && isFriendState(av)) return 4;
            return players.get() ? 1 : -1;
        }
        if (state instanceof ItemEntityRenderState) {
            return items.get() ? 3 : -1;
        }
        if (state instanceof LivingEntityRenderState) {
            return mobs.get() ? 2 : -1;
        }
        return -1;
    }

    public static <S> void apply(SubmitNodeStorage.ModelSubmit<S> submit, PoseStack pose, MultiBufferSource.BufferSource src) {
        try {
            ChinaHat.apply(submit, pose, src);
        } catch (Throwable ignored) {
        }
        Chams m = INSTANCE;
        if (m == null) return;
        float a = (float) m.currentFade();
        if (a <= 0.01f) return;
        int cat = m.category(submit.state());
        if (cat < 0) return;

        int base = m.colorFor(cat);
        float hf = m.hitFactor(submit.state());
        if (hf > 0f) {
            base = blend(base, m.hitColor.get(), hf);
        }
        int alpha = (int) (((base >>> 24) & 0xFF) * a);
        if (alpha <= 1) return;
        int col = (alpha << 24) | (base & 0xFFFFFF);

        boolean through = m.visFor(cat).is("Сквозь стены");
        RenderType type = ChamsRenderTypes.get(m.styleIndex(m.styleFor(cat)), through);
        submit.model().renderToBuffer(pose, src.getBuffer(type), 15728880, OverlayTexture.NO_OVERLAY, col);
    }

    public static void applyItem(net.minecraft.world.item.ItemDisplayContext ctx, PoseStack pose,
                                 MultiBufferSource.BufferSource src, int light,
                                 java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> quads) {
        Chams m = INSTANCE;
        if (m == null || quads == null || quads.isEmpty()) return;
        boolean fp = ctx.firstPerson();
        boolean enabled = fp ? m.self.get() : m.items.get();
        if (!enabled) return;
        float a = (float) m.currentFade();
        if (a <= 0.01f) return;
        int base = fp ? m.selfColor.get() : m.itemsColor.get();
        int alpha = (int) (((base >>> 24) & 0xFF) * a);
        if (alpha <= 1) return;
        int col = (alpha << 24) | (base & 0xFFFFFF);
        net.minecraft.resources.Identifier atlas = quads.get(0).sprite().atlasLocation();
        RenderType type = ChamsRenderTypes.getItem(m.styleIndex(fp ? m.selfStyle : m.itemsStyle), atlas);

        float aF = ((col >>> 24) & 0xFF) / 255f;
        float rF = ((col >> 16) & 0xFF) / 255f;
        float gF = ((col >> 8) & 0xFF) / 255f;
        float bF = (col & 0xFF) / 255f;
        com.mojang.blaze3d.vertex.VertexConsumer vc = src.getBuffer(type);
        PoseStack.Pose p = pose.last();
        for (net.minecraft.client.renderer.block.model.BakedQuad quad : quads) {
            vc.putBulkData(p, quad, rF, gF, bF, aF, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void applyHand(net.minecraft.client.model.geom.ModelPart part, PoseStack pose,
                                 net.minecraft.client.renderer.OrderedSubmitNodeCollector collector, int light) {
        Chams m = INSTANCE;
        if (m == null || !m.self.get()) return;
        float a = (float) m.currentFade();
        if (a <= 0.01f) return;
        int base = m.selfColor.get();
        int alpha = (int) (((base >>> 24) & 0xFF) * a);
        if (alpha <= 1) return;
        int col = (alpha << 24) | (base & 0xFFFFFF);
        boolean through = m.selfVis.is("Сквозь стены");
        RenderType type = ChamsRenderTypes.get(m.styleIndex(m.selfStyle), through);
        collector.submitModelPart(part, pose, type, light, OverlayTexture.NO_OVERLAY, null, col, null);
    }

    private static int blend(int a, int b, float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (a & 0xFF000000) | (r << 16) | (g << 8) | bl;
    }
}