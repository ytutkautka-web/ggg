package fun.photon.hud.element;

import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.hud.RowList;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.LinkedHashMap;
import java.util.List;

public class PotionsElement extends HudElement {

    private final RowList list = new RowList(180);

    public PotionsElement() {
        super("potions", "Эффекты", 6, 60);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.potions.get();
    }

    @Override
    public boolean active() {
        return enabled() && (EDIT || (mc.player != null && !mc.player.getActiveEffects().isEmpty()));
    }

    private boolean rightSide() {
        return x + width() / 2f > Render2DUtil.screenWidth() / 2f;
    }

    private String name(MobEffectInstance e) {
        return e.getEffect().value().getDisplayName().getString();
    }

    private String time(MobEffectInstance e) {
        int d = e.getDuration();
        if (d < 0) return "∞";
        int sec = d / 20;
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    private TextureAtlasSprite spriteOf(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> h) {
        try {
            net.minecraft.resources.Identifier id = net.minecraft.client.gui.Gui.getMobEffectSprite(h);
            return mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.GUI).getSprite(id);
        } catch (Throwable t) {
            return null;
        }
    }

    private net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> randomEffect;
    private long lastRandom;

    private void cycleRandom() {
        long now = System.currentTimeMillis();
        if (randomEffect != null && now - lastRandom < 1000) return;
        try {
            java.util.List<net.minecraft.core.Holder.Reference<net.minecraft.world.effect.MobEffect>> all =
                    net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.listElements()
                            .collect(java.util.stream.Collectors.toList());
            if (!all.isEmpty()) {
                randomEffect = all.get(new java.util.Random().nextInt(all.size()));
                lastRandom = now;
            }
        } catch (Throwable ignored) {}
    }

    private LinkedHashMap<String, RowList.Row> rows() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        boolean empty = mc.player == null || mc.player.getActiveEffects().isEmpty();
        if (empty) {
            if (EDIT) {
                cycleRandom();
                if (randomEffect != null) {
                    RowList.Row r = new RowList.Row(randomEffect.value().getDisplayName().getString(), "**:**", 0, 0);
                    r.lvl = 1;
                    r.bad = randomEffect.value().getCategory() == MobEffectCategory.HARMFUL;
                    r.sprite = spriteOf(randomEffect);
                    map.put("sample", r);
                }
            }
            return map;
        }
        for (MobEffectInstance e : mc.player.getActiveEffects()) {
            RowList.Row r = new RowList.Row(name(e), time(e), 0, 0);
            r.lvl = e.getAmplifier() + 1;
            r.bad = e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
            r.sprite = spriteOf(e.getEffect());
            int d = e.getDuration();
            if (d >= 0 && d <= 200) {
                double o = 0.5 + 0.5 * Math.cos(2 * Math.PI * (System.currentTimeMillis() % 700) / 700.0);
                r.blink = (float) (0.4 + 0.6 * o);
            } else {
                r.blink = 1f;
            }
            map.put(e.getEffect().value().getDescriptionId(), r);
        }
        return map;
    }

    private float rowH() {
        return font(12).getHeight() + s(6);
    }

    private float headerH() {
        return font(13).getHeight() + s(9);
    }

    @Override
    public float width() {
        PhotonFont f = font(12);
        PhotonFont lf = font(10);
        float titleW = font(13).getWidth("Эффекты") + s(16);
        float max = titleW;
        for (RowList.Item it : list.cached()) {
            RowList.Row r = it.row;
            float lvlW = r.lvl > 1 ? lf.getWidth(" " + r.lvl) : 0;
            float row = s(11) + s(5) + f.getWidth(r.left) + lvlW + s(10) + f.getWidth(r.right) + s(8);
            max = Math.max(max, row);
        }
        return max + s(14);
    }

    @Override
    public float height() {
        float sum = 0;
        for (RowList.Item it : list.cached()) sum += rowH() * it.v;
        return headerH() + sum + s(6);
    }

    @Override
    public void render() {
        List<RowList.Item> items = list.sync(rows());
        float sum = 0;
        for (RowList.Item it : items) sum += rowH() * it.v;

        PhotonFont title = font(13);
        PhotonFont f = font(12);
        PhotonFont lf = font(10);
        float w = width();
        float headH = headerH();
        float h = headH + sum + s(6);
        boolean right = rightSide();

        panel(x, y, w, h);
        accentBar(x, y, h);
        title.drawInRowY("Эффекты", x + s(8), y + s(2), headH, accent());

        float base = RenderTransform.alpha;
        float cy = y + headH;
        for (RowList.Item it : items) {
            float v = (float) it.v;
            RowList.Row r = it.row;
            float rh = rowH();
            RenderTransform.alpha = base * v * r.blink;

            float ty = cy + (rh - f.getHeight()) / 2f;
            float ic = s(11);
            float iy = cy + (rh - ic) / 2f;

            float lx = x + s(8);
            if (r.sprite != null) {
                ImageUtil.drawTextureRegion(r.sprite.atlasLocation(), lx, iy, ic, ic,
                        r.sprite.getU0(), r.sprite.getV0(), r.sprite.getU1(), r.sprite.getV1(), -1);
            } else {
                Render2DUtil.rounded(lx, iy, ic, ic, s(3), s(3), s(3), s(3), 1f, accent(), 0, 0f);
            }
            float nameX = lx + ic + s(5);
            int nameCol = r.bad ? 0xFFFF554B : Palette.TEXT_LIGHT;
            f.drawString(r.left, nameX, ty, nameCol);
            if (r.lvl > 1) {
                lf.drawString(" " + r.lvl, nameX + f.getWidth(r.left), ty + s(1), ColorUtil.rgba(157, 157, 160, 255));
            }

            float dw = f.getWidth(r.right);
            float boxW = dw + s(8);
            float boxX = x + w - s(8) - boxW;
            float boxH = f.getHeight() + s(3);
            float boxY = cy + (rh - boxH) / 2f;
            Render2DUtil.rounded(boxX, boxY, boxW, boxH, s(2), s(2), s(2), s(2), 1f,
                    ColorUtil.multAlpha(accent(), 0.22f), 0, 0f);
            f.drawCenteredInRow(r.right, boxX + boxW / 2f, boxY, boxH, 0xFFFFFFFF);

            cy += rh * v;
        }
        RenderTransform.alpha = base;
    }
}
