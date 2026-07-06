package fun.photon.hud;

import fun.photon.clickgui.Palette;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ListElement extends HudElement {

    protected final RowList list = new RowList(220);

    protected ListElement(String id, String name, float x, float y) {
        super(id, name, x, y);
    }

    protected abstract LinkedHashMap<String, RowList.Row> build();

    @Override
    public boolean active() {
        return enabled() && (EDIT || !build().isEmpty());
    }

    protected String header() { return null; }

    protected int headerColor() { return accent(); }

    protected boolean chip() { return false; }

    protected boolean bar() { return false; }

    protected boolean rightSide() { return false; }

    protected boolean showSample() { return EDIT; }

    protected LinkedHashMap<String, RowList.Row> sample() { return new LinkedHashMap<>(); }

    private float rowH() {
        return font(12).getHeight() + (bar() ? s(11) : s(7));
    }

    private float headerH() {
        return header() == null ? 0 : font(13).getHeight() + s(5);
    }

    private LinkedHashMap<String, RowList.Row> currentRows() {
        LinkedHashMap<String, RowList.Row> cur = build();
        if (cur.isEmpty() && showSample()) cur = sample();
        return cur;
    }

    @Override
    public float width() {
        PhotonFont f = font(12);
        float chipW = chip() ? s(10) + s(6) : 0;
        float max = header() == null ? s(30) : font(13).getWidth(header());
        for (RowList.Item it : list.cached()) {
            RowList.Row r = it.row;
            float wRow = chipW + f.getWidth(r.left == null ? "" : r.left);
            if (r.right != null) wRow += s(14) + f.getWidth(r.right);
            max = Math.max(max, wRow);
        }
        return max + s(16);
    }

    @Override
    public float height() {
        float sum = 0;
        for (RowList.Item it : list.cached()) sum += rowH() * it.v;
        if (sum <= 0 && header() == null) return 0;
        return headerH() + sum + s(8);
    }

    @Override
    public void render() {
        List<RowList.Item> items = list.sync(currentRows());
        float sum = 0;
        for (RowList.Item it : items) sum += rowH() * it.v;
        if (sum <= 0 && header() == null) return;

        float w = width();
        float h = headerH() + sum + s(8);
        boolean right = rightSide();
        panel(x, y, w, h);
        if (right) Render2DUtil.rounded(x + w - s(2), y, s(2), h, s(4), s(4), 0f, 0f, 1f, accent(), 0, 0f);
        else accentBar(x, y, h);

        float cy = y + s(4);
        if (header() != null) {
            PhotonFont hf = font(13);
            if (right) hf.drawRight(header(), x + w - s(8), cy, headerColor());
            else hf.drawString(header(), x + s(8), cy, headerColor());
            cy += headerH();
        }

        float base = RenderTransform.alpha;
        PhotonFont f = font(12);
        for (RowList.Item it : items) {
            float v = (float) it.v;
            RenderTransform.alpha = base * v;
            drawRow(f, it.row, cy, rowH(), w, right, v);
            cy += rowH() * v;
        }
        RenderTransform.alpha = base;
    }

    private void drawRow(PhotonFont f, RowList.Row r, float cy, float rowH, float w, boolean right, float v) {
        float pad = s(8);
        float chip = s(11);
        float textTop = bar() ? cy + s(2) : cy + (rowH - f.getHeight()) / 2f;
        float chipY = textTop + (f.getHeight() - chip) / 2f;

        if (right) {
            float rx = x + w - pad;
            if (chip()) {
                drawChip(r, rx - chip, chipY, chip, v);
                rx -= chip + s(6);
            }
            if (r.left != null) f.drawRight(r.left, rx, textTop, Palette.TEXT_LIGHT);
            if (r.right != null) f.drawString(r.right, x + pad, textTop, Palette.TEXT_MUTED);
        } else {
            float lx = x + pad;
            if (chip()) {
                drawChip(r, lx, chipY, chip, v);
                lx += chip + s(6);
            }
            if (r.left != null) f.drawString(r.left, lx, textTop, Palette.TEXT_LIGHT);
            if (r.right != null) f.drawRight(r.right, x + w - pad, textTop, Palette.TEXT_MUTED);
        }

        if (bar()) drawBar(f, r, cy, w);
    }

    private void drawChip(RowList.Row r, float cx, float cy, float chip, float v) {
        if (r.stack != null && !r.stack.isEmpty()) {
            float sz = chip * v;
            itemIcon(r.stack, cx + (chip - sz) / 2f, cy + (chip - sz) / 2f, sz);
            return;
        }
        if (r.head != null) {
            playerHead(r.head, cx, cy, chip);
            return;
        }
        TextureAtlasSprite sp = r.sprite;
        if (sp != null) {
            ImageUtil.drawTextureRegion(sp.atlasLocation(), cx, cy, chip, chip,
                    sp.getU0(), sp.getV0(), sp.getU1(), sp.getV1(), -1);
        } else {
            Render2DUtil.rounded(cx, cy, chip, chip, s(3), s(3), s(3), s(3), 1f, r.color, 0, 0f);
        }
    }

    private void drawBar(PhotonFont f, RowList.Row r, float cy, float w) {
        float pad = s(8);
        float by = cy + f.getHeight() + s(3);
        float bw = w - pad * 2f;
        float bh = s(3);
        Render2DUtil.rounded(x + pad, by, bw, bh, bh / 2f, bh / 2f, bh / 2f, bh / 2f, 1f, ColorUtil.rgba(40, 40, 46, 220), 0, 0f);
        if (r.pct > 0.01f)
            Render2DUtil.rounded(x + pad, by, bw * r.pct, bh, bh / 2f, bh / 2f, bh / 2f, bh / 2f, 1f, accent(), 0, 0f);
    }
}
