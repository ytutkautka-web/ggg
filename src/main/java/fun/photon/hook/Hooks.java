package fun.photon.hook;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.photon.Photon;
import fun.photon.events.impl.EventClick;
import fun.photon.events.impl.EventKey;
import fun.photon.events.impl.EventRender2D;
import fun.photon.events.impl.EventScreen;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.impl.render.Chams;
import fun.photon.module.impl.сombat.NoFriendDamage;
import fun.photon.utils.TpsTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.List;

public final class Hooks {

    private Hooks() {}

    public static void post(Object event) {
        try {
            Photon.getInstance().getEventBus().post(event);
        } catch (Throwable ignored) {
        }
    }

    public static void update() {
        post(new EventUpdate());
    }

    public static void render2D(GuiGraphics graphics, float partialTicks) {
        post(new EventRender2D(graphics, partialTicks));
    }

    public static void key(int key) {
        post(new EventKey(key));
    }

    public static void screen(Screen now, Screen prev) {
        post(new EventScreen(now, prev));
    }

    public static EventClick click(double x, double y, int button, int action) {
        EventClick event = new EventClick(x, y, button, action);
        post(event);
        return event;
    }

    public static <S> void chams(SubmitNodeStorage.ModelSubmit<S> submit, PoseStack pose, MultiBufferSource.BufferSource src) {
        try {
            Chams.apply(submit, pose, src);
        } catch (Throwable ignored) {
        }
    }

    public static <S> void chinaHat(SubmitNodeStorage.ModelSubmit<S> submit, PoseStack pose, MultiBufferSource.BufferSource src) {
        try {
            fun.photon.module.impl.render.ChinaHat.apply(submit, pose, src);
        } catch (Throwable ignored) {
        }
    }

    public static void chinaHatHead(ModelPart head, AvatarRenderState av) {
        try {
            fun.photon.module.impl.render.ChinaHat.captureHead(head, av);
        } catch (Throwable ignored) {
        }
    }

    public static void chamsItem(ItemDisplayContext ctx, PoseStack pose, MultiBufferSource.BufferSource src, int light, List<BakedQuad> quads) {
        try {
            Chams.applyItem(ctx, pose, src, light, quads);
        } catch (Throwable ignored) {
        }
    }

    public static void chamsHand(net.minecraft.client.model.geom.ModelPart part, PoseStack pose, OrderedSubmitNodeCollector collector, int light) {
        try {
            Chams.applyHand(part, pose, collector, light);
        } catch (Throwable ignored) {
        }
    }

    public static void hit(Entity entity) {
        try {
            Chams.onHit(entity);
        } catch (Throwable ignored) {
        }
    }

    public static void timeSync(long gameTime) {
        try {
            TpsTracker.onTimeSync(gameTime);
        } catch (Throwable ignored) {
        }
    }

    public static void blockOverlay(PoseStack pose) {
        try {
            fun.photon.module.impl.render.BlockOverlay.render(pose);
        } catch (Throwable ignored) {
        }
    }

    public static boolean seeInvisibleGhost(Entity entity) {
        try {
            return fun.photon.module.impl.render.SeeInvisible.shouldGhost(entity);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isLocalPlayer(Entity entity) {
        return entity == net.minecraft.client.Minecraft.getInstance().player;
    }

    public static boolean noEntityPush(Entity entity) {
        try {
            return isLocalPlayer(entity) && fun.photon.module.impl.player.NoPush.noEntityPush();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean noBlockPush(Entity entity) {
        try {
            return isLocalPlayer(entity) && fun.photon.module.impl.player.NoPush.noBlockPush();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean noWaterPush(Entity entity) {
        try {
            return isLocalPlayer(entity) && fun.photon.module.impl.player.NoPush.noWaterPush();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean freeCamActive() {
        try {
            fun.photon.module.impl.render.FreeCam m =
                    Photon.getInstance().getModuleManager().getModule(fun.photon.module.impl.render.FreeCam.class);
            return m != null && m.isActive();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static fun.photon.module.impl.render.FreeCam freeCam() {
        try {
            return Photon.getInstance().getModuleManager().getModule(fun.photon.module.impl.render.FreeCam.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean noFriendDamage(Entity target) {
        try {
            return NoFriendDamage.shouldCancel(target);
        } catch (Throwable ignored) {
            return false;
        }
    }
}