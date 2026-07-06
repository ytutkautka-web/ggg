package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.ListSetting;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;

public class ListValueComponent extends ValueComponent {

    private final ListSetting list;

    public ListValueComponent(ListSetting setting, float width) {
        super(setting, width - 10);
        this.list = setting;
    }

    private static final float PAD = 8;
    private static final float NAME_ROW = 20;
    private static final float PILL_H = 19;
    private static final float PILL_PX = 11;
    private static final float GAP = 6;

    @Override
    public void render(double mouseX, double mouseY) {
        PhotonFont f = font(14);
        f.drawInRowY(list.getName(), x + PAD, y, NAME_ROW, fg);

        float top = y + NAME_ROW + 2;
        float maxX = x + PAD + width;
        float px = x + PAD, py = top;

        for (String opt : list.getOptions()) {
            boolean selected = list.isSelected(opt);
            float pw = f.getWidth(opt) + PILL_PX * 2;
            if (px + pw > maxX && px > x + PAD) {
                px = x + PAD;
                py += PILL_H + GAP;
            }
            Render2DUtil.rounded(px, py, pw, PILL_H, PILL_H / 2f, PILL_H / 2f, PILL_H / 2f, PILL_H / 2f, 1f,
                    selected ? Palette.PILL_ON : Palette.PILL_OFF, 0, 0f);
            f.drawCenteredInRow(opt, px + pw / 2f, py, PILL_H,
                    selected ? Palette.PILL_TEXT_ON : Palette.PILL_TEXT_OFF);

            if (hover(mouseX, mouseY, px, py, pw, PILL_H)) Cursors.set(Cursors.hand());
            px += pw + GAP;
        }

        height = (py + PILL_H) - y + PAD;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        PhotonFont f = font(14);
        float top = y + NAME_ROW + 2;
        float maxX = x + PAD + width;
        float px = x + PAD, py = top;

        for (String opt : list.getOptions()) {
            float pw = f.getWidth(opt) + PILL_PX * 2;
            if (px + pw > maxX && px > x + PAD) {
                px = x + PAD;
                py += PILL_H + GAP;
            }
            if (hover(mouseX, mouseY, px, py, pw, PILL_H)) {
                list.toggle(opt);
            }
            px += pw + GAP;
        }
    }
}
