package fun.photon.hud.element;

import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.render.PhotonFont;

public class CoordsElement extends HudElement {

    public CoordsElement() {
        super("coords", "Координаты", 6, 6);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.coords.get();
    }

    private String text() {
        if (mc.player == null) return "0, 0, 0";
        return (int) mc.player.getX() + ", " + (int) mc.player.getY() + ", " + (int) mc.player.getZ();
    }

    @Override
    public float width() {
        return font(12).getWidth("XYZ  " + text()) + s(16);
    }

    @Override
    public float height() {
        return font(12).getHeight() + s(10);
    }

    @Override
    public void render() {
        PhotonFont f = font(12);
        float w = width();
        float h = height();
        float pad = s(8);
        panel(x, y, w, h);
        accentBar(x, y, h);
        f.drawInRowY("XYZ", x + pad, y, h, accent());
        f.drawInRowY(text(), x + pad + f.getWidth("XYZ "), y, h, Palette.TEXT_LIGHT);
    }
}
