package com.petrolpark.destroy.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.petrolpark.destroy.client.gui.DestroyGuiTextures;
import com.petrolpark.destroy.client.gui.menu.CustomExplosiveMenu;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CustomExplosiveScreen extends AbstractSimiContainerScreen<CustomExplosiveMenu> {

    private final DestroyGuiTextures background;

    public CustomExplosiveScreen(CustomExplosiveMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        background = DestroyGuiTextures.CUSTOM_EXPLOSIVE_BACKGROUND;
    };

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 112);
        super.init();
    };

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(leftPos, topPos, 0d);

        background.render(graphics, 0, 0);
        renderPlayerInventory(graphics, 0, background.height + 4);

        ms.popPose();
    };
    
};