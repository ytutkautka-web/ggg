package fun.photon.clickgui.component;

import fun.photon.clickgui.theme.ThemeManager;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.Render2DUtil;

import java.util.ArrayList;
import java.util.List;

public final class ThemeParticles {

    private static final class P {
        float x, y, vx, vy, life, maxLife, size;
        int color;
    }

    private final List<P> particles = new ArrayList<>();
    private long lastTime = System.nanoTime();

    public void burst(float cx, float cy, int count, float dir) {
        int accent = ThemeManager.get().accent();
        for (int i = 0; i < count; i++) {
            P p = new P();
            double ang = Math.random() * Math.PI * 2;
            double spd = 60 + Math.random() * 220;
            float radius = dir < 0 ? (40 + (float) Math.random() * 90) : 0f;
            p.x = cx + (float) Math.cos(ang) * radius;
            p.y = cy + (float) Math.sin(ang) * radius;
            p.vx = (float) (Math.cos(ang) * spd * dir);
            p.vy = (float) (Math.sin(ang) * spd * dir) - 40f;
            p.maxLife = 0.45f + (float) Math.random() * 0.5f;
            p.life = p.maxLife;
            p.size = 1.5f + (float) Math.random() * 3.5f;
            int tint = (i % 3 == 0) ? ColorUtil.rgba(255, 255, 255, 255) : accent;
            p.color = tint;
            particles.add(p);
        }
    }

    public void update() {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastTime) / 1_000_000_000f);
        lastTime = now;
        for (int i = particles.size() - 1; i >= 0; i--) {
            P p = particles.get(i);
            p.life -= dt;
            if (p.life <= 0) {
                particles.remove(i);
                continue;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vx *= 0.92f;
            p.vy = p.vy * 0.92f + 90f * dt;
        }
    }

    public void render() {
        update();
        for (P p : particles) {
            float a = Math.max(0f, Math.min(1f, p.life / p.maxLife));
            int col = ColorUtil.multAlpha(p.color, a * a);
            float s = p.size * (0.5f + 0.5f * a);
            Render2DUtil.circle(p.x, p.y, s, col);
        }
    }

    public boolean isEmpty() {
        return particles.isEmpty();
    }

    public void clear() {
        particles.clear();
    }
}
