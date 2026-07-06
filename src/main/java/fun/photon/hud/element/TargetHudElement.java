package fun.photon.hud.element;

import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TargetHudElement extends HudElement {

    private final Animation health = new Animation(Easing.EASE_OUT_CUBIC, 250);
    private LivingEntity shown;
    private long lastSeen;

    public TargetHudElement() {
        super("targethud", "Цель", 6, 140);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.targethud.get();
    }

    private LivingEntity live() {
        if (mc.crosshairPickEntity instanceof LivingEntity le && le.isAlive()) return le;
        if (EDIT) return mc.player;
        return null;
    }

    @Override
    public boolean active() {
        if (!enabled()) return false;
        LivingEntity l = live();
        if (l != null) {
            shown = l;
            lastSeen = System.currentTimeMillis();
        }
        return shown != null && System.currentTimeMillis() - lastSeen < 1500;
    }

    @Override
    public float width() {
        return s(190);
    }

    @Override
    public float height() {
        return s(62);
    }

    @Override
    public void render() {
        if (shown == null) return;
        LivingEntity t = shown;

        PhotonFont f = font(14);
        PhotonFont small = font(11);
        float w = width();
        float h = height();
        float pad = s(9);

        panel(x, y, w, h);
        Render2DUtil.rounded(x, y, s(2), h, 0f, 0f, s(4), s(4), 1f, accent(), 0, 0f);

        float hs = h - pad * 2f;
        boolean hasHead = drawHead(t, x + pad, y + pad, hs);
        float textX = hasHead ? x + pad + hs + s(9) : x + pad + s(2);

        float barX = textX;
        float barW = x + w - pad - textX;
        f.drawString(clip(f, t.getName().getString(), barW), textX, y + s(8), Palette.TEXT_LIGHT);

        float hp = t.getHealth();
        float max = Math.max(1f, t.getMaxHealth());
        float absorb = t.getAbsorptionAmount();
        float frac = Math.max(0f, Math.min(1f, hp / max));
        health.run(frac);
        float cur = (float) health.getValue();

        float barH = s(7);
        float barY = y + s(8) + f.getHeight() + s(4);
        float r = barH / 2f;
        Render2DUtil.rounded(barX, barY, barW, barH, r, r, r, r, 1f, ColorUtil.rgba(38, 38, 44, 230), 0, 0f);

        float hw = barW * cur;
        float aw = barW * Math.min(absorb / max, 1f);
        int hc = ColorUtil.interpolate(0xFFFF3B30, 0xFF35FF6A, cur);
        if (hw > 0.5f)
            Render2DUtil.rounded(barX, barY, hw, barH, r, r, r, r, 1f, hc, 0, 0f);
        if (aw > 0.5f)
            Render2DUtil.rounded(barX, barY, aw, barH, r, r, r, r, 1f, 0xFFFFD23B, 0, 0f);

        float infoY = barY + barH + s(3);
        int hpInt = (int) Math.ceil(hp);
        String hpText = absorb > 0.5f
                ? hpInt + "+" + (int) absorb + " HP"
                : hpInt + "/" + (int) max + " HP";
        small.drawString(hpText, textX, infoY, hc);
        float dist = mc.player == null ? 0 : mc.player.distanceTo(t);
        small.drawRight(String.format("%.1fm", dist), x + w - pad, infoY, Palette.TEXT_MUTED);
    }

    private String clip(PhotonFont f, String s, float maxW) {
        if (f.getWidth(s) <= maxW) return s;
        while (s.length() > 1 && f.getWidth(s + "…") > maxW) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    private boolean drawHead(LivingEntity t, float hx, float hy, float hs) {
        if (!(t instanceof Player) || mc.getConnection() == null) return false;
        PlayerInfo pi = mc.getConnection().getPlayerInfo(t.getUUID());
        if (pi == null) return false;
        Identifier skin = pi.getSkin().body().texturePath();
        ImageUtil.drawTextureRegion(skin, hx, hy, hs, hs, 8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, -1);
        ImageUtil.drawTextureRegion(skin, hx, hy, hs, hs, 40f / 64f, 8f / 64f, 48f / 64f, 16f / 64f, -1);
        return true;
    }
}
