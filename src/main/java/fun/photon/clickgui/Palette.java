package fun.photon.clickgui;

import fun.photon.utils.render.ColorUtil;

public final class Palette {

    private Palette() {}

    public static int PANEL_BG   = ColorUtil.rgba(0, 0, 0, 171);
    public static int HEADER_BG  = ColorUtil.rgba(248, 248, 248, 255);

    public static int ACCENT     = ColorUtil.rgba(64, 115, 242, 255);

    public static int MODULE_OFF = ColorUtil.rgba(34, 36, 44, 215);

    public static int CARD_FILL    = ColorUtil.rgba(255, 255, 255, 255);
    public static int CARD_A_OFF   = 150;
    public static int CARD_A_ON    = 171;
    public static int CARD_A_BODY  = 61;
    public static int CARD_OUTLINE = ColorUtil.rgba(255, 255, 255, 125);

    public static int HOVER        = ColorUtil.rgba(255, 255, 255, 40);

    public static int TEXT_DARK  = ColorUtil.rgba(0, 0, 0, 255);
    public static int TEXT_WHITE = ColorUtil.rgba(255, 255, 255, 255);
    public static int TEXT_MUTED = ColorUtil.rgba(150, 150, 155, 255);
    public static int TEXT_GREY  = ColorUtil.rgba(120, 120, 125, 255);
    public static int TEXT_LIGHT = ColorUtil.rgba(235, 238, 245, 255);

    public static int TRACK      = ColorUtil.rgba(170, 170, 175, 255);
    public static int DIVIDER    = ColorUtil.rgba(255, 255, 255, 60);
    public static int DIM        = ColorUtil.rgba(0, 0, 0, 150);

    public static int TOGGLE_OFF = ColorUtil.rgba(40, 40, 45, 200);
    public static int TOGGLE_ON  = ColorUtil.rgba(20, 20, 20, 255);
    public static int TOGGLE_BORDER = ColorUtil.rgba(10, 10, 10, 255);
    public static int KNOB_OFF   = ColorUtil.rgba(225, 225, 230, 255);
    public static int KNOB_ON    = ColorUtil.rgba(248, 248, 248, 255);

    public static int PILL_OFF      = ColorUtil.rgba(20, 20, 20, 220);
    public static int PILL_ON       = ColorUtil.rgba(248, 248, 248, 255);
    public static int PILL_TEXT_OFF = ColorUtil.rgba(200, 200, 205, 255);
    public static int PILL_TEXT_ON  = ColorUtil.rgba(20, 20, 20, 255);
}
