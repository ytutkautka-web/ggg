package net.minecraft.client.gui.screens;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.proton.ProtonAccount;
import net.minecraft.client.proton.ProtonAccountManager;
import net.minecraft.client.proton.ProtonSkinLoader;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccountSwitcherScreen extends Screen {
    private static final Component TITLE = Component.literal("AltManager");
    private static final Component ADD = Component.literal("Add");
    private static final Component HINT = Component.literal("Nickname");
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 70;

    private final Screen lastScreen;
    private final List<ProtonAccount> rows = new ArrayList<>();
    private EditBox nameBox;

    public AccountSwitcherScreen(Screen p_lastScreen) {
        super(TITLE);
        this.lastScreen = p_lastScreen;
    }

    @Override
    protected void init() {
        this.rows.clear();
        this.rows.addAll(ProtonAccountManager.get().getAccounts());

        int i = this.width / 2;
        this.nameBox = new EditBox(this.font, i - 100, 40, 154, 20, HINT);
        this.nameBox.setHint(HINT);
        this.addRenderableWidget(this.nameBox);
        this.addRenderableWidget(
            Button.builder(ADD, p_b -> this.addAccount()).bounds(i + 58, 40, 42, 20).build()
        );

        int j = LIST_TOP;
        for (ProtonAccount protonaccount : this.rows) {
            boolean flag = protonaccount.isCurrent();
            this.addRenderableWidget(
                Button.builder(Component.literal(flag ? "Active" : "Select"), p_b -> this.select(protonaccount))
                    .bounds(i + 20, j, 60, 20)
                    .build()
            ).active = !flag;
            this.addRenderableWidget(
                Button.builder(Component.literal("X"), p_b -> this.removeAccount(protonaccount)).bounds(i + 84, j, 20, 20).build()
            );
            j += ROW_HEIGHT;
        }

        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, p_b -> this.onClose()).bounds(i - 100, this.height - 28, 200, 20).build()
        );
    }

    private void addAccount() {
        ProtonAccountManager.get().add(this.nameBox.getValue());
        this.rebuildWidgets();
    }

    private void removeAccount(ProtonAccount p_account) {
        ProtonAccountManager.get().remove(p_account);
        this.rebuildWidgets();
    }

    private void select(ProtonAccount p_account) {
        ProtonAccountManager.get().switchTo(p_account);
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics p_g, int p_mouseX, int p_mouseY, float p_partial) {
        super.render(p_g, p_mouseX, p_mouseY, p_partial);
        p_g.drawCenteredString(this.font, this.title, this.width / 2, 16, -1);

        int i = this.width / 2;
        int j = LIST_TOP;
        for (ProtonAccount protonaccount : this.rows) {
            PlayerFaceRenderer.draw(p_g, ProtonSkinLoader.getSkin(protonaccount), i - 100, j + 2, 16);
            int k = protonaccount.isCurrent() ? ARGB.color(255, 85, 255, 85) : -1;
            p_g.drawString(this.font, protonaccount.getName(), i - 78, j + 6, k);
            j += ROW_HEIGHT;
        }

        if (this.rows.isEmpty()) {
            p_g.drawCenteredString(this.font, Component.literal("No accounts. Add a nickname above."), i, LIST_TOP + 10, -5592406);
        }
    }

    @Override
    public void renderBackground(GuiGraphics p_g, int p_mouseX, int p_mouseY, float p_partial) {
        super.renderBackground(p_g, p_mouseX, p_mouseY, p_partial);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
