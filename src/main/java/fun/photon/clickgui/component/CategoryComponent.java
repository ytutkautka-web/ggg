package fun.photon.clickgui.component;

import fun.photon.Photon;
import fun.photon.clickgui.Palette;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.utils.math.Mathf;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.RectUtil;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.ScissorUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryComponent {

    public static final float WIDTH = 232f;
    public static final float HEADER = 44f;
    public static final float HEIGHT = 540f;
    public static final float PAD = 12f;
    public static final float CARD_GAP = 9f;
    public static final float ROUND = 16f;

    private final Category category;
    private final String icon;
    private final List<ModuleComponent> modules = new ArrayList<>();

    private float x, y;
    private float scroll, animatedScroll, contentHeight;
    private float clipTop, clipBottom;

    public CategoryComponent(Category category) {
        this.category = category;
        this.icon = iconFor(category);
        List<Module> list = Photon.getInstance().getModuleManager().getModulesByCategory(category);
        list.sort(Comparator.comparing(Module::getName));
        for (Module m : list) {
            modules.add(new ModuleComponent(m, WIDTH - PAD * 2));
        }
    }

    private static String iconFor(Category c) {
        switch (c) {
            case COMBAT:   return "KiCon/combat.svg";
            case MOVEMENT: return "KiCon/movement.svg";
            case RENDER:   return "KiCon/monitor.svg";
            case PLAYER:   return "KiCon/user.svg";
            default:       return "KiCon/settings.svg";
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isInside(double mx, double my) {
        return mx >= x && mx <= x + WIDTH && my >= y && my <= y + HEIGHT;
    }

    public void render(double mouseX, double mouseY) {

        Render2DUtil.roundedRect(x, y, WIDTH, HEIGHT, ROUND, Palette.PANEL_BG);

        Render2DUtil.rounded(x, y, WIDTH, HEADER, 0f, ROUND, 0f, ROUND, 1f, Palette.HEADER_BG, 0, 0f);

        PhotonFont f = FontUtil.mitr(20);
        float ih = f.getHeight();
        ImageUtil.drawSvg(icon, x + PAD, y + (HEADER - ih) / 2f, ih, ih, Palette.TEXT_DARK);
        f.drawInRowY(category.getName(), x + PAD + ih + 8, y, HEADER, Palette.TEXT_DARK);

        clipTop = y + HEADER + PAD;
        clipBottom = y + HEIGHT - PAD;
        float visible = clipBottom - clipTop;

        float cardX = x + PAD;
        float cardW = WIDTH - PAD * 2;

        animatedScroll += (scroll - animatedScroll) * 0.2f;

        ScissorUtil.scissor(x, clipTop, WIDTH, visible);
        float start = clipTop + animatedScroll;
        float yy = start;
        for (ModuleComponent m : modules) {
            m.setLayout(cardX, yy, cardW);
            m.render(mouseX, mouseY, x, WIDTH, clipTop, clipBottom);
            yy += m.getHeight() + CARD_GAP;
        }
        contentHeight = yy - start;
        ScissorUtil.disable();

        float maxScroll = Math.max(0f, contentHeight - visible);
        scroll = (float) Mathf.clamp(-maxScroll, 0f, scroll);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (ModuleComponent m : modules) {
            m.mouseClicked(mouseX, mouseY, button, clipTop, clipBottom);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleComponent m : modules) m.mouseReleased(mouseX, mouseY, button);
    }

    public void keyPressed(int key) {
        for (ModuleComponent m : modules) m.keyPressed(key);
    }

    public void addScroll(double delta) {
        float maxScroll = Math.max(0f, contentHeight - (clipBottom - clipTop));
        scroll = (float) Mathf.clamp(-maxScroll, 0f, scroll + (float) (delta * 26));
    }

    public void onClose() {
        scroll = 0;
        animatedScroll = 0;
        for (ModuleComponent m : modules) m.onClose();
    }
}
