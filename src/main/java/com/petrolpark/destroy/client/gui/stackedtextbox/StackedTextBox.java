package com.petrolpark.destroy.client.gui.stackedTextBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StackedTextBox extends AbstractStackedTextBox {

    private static final int PERMANENCE_LIFETIME = 20;

    private Area activationArea;
    private boolean isActivationAreaHovered;

    /**
     * A list of Activation Areas and the translation keys of the definitions they will show
     */
    private List<Pair<Area, String>> possibleChildActivationAreas;

    private int lifetime;

    private final List<Component> lines;

    public StackedTextBox(Minecraft minecraft, int x, int y, AbstractStackedTextBox parent) {
        super(x, y, parent, AbstractStackedTextBox.NOTHING);

        this.minecraft = minecraft;

        activationArea = new Area(x, y, width, height);
        isActivationAreaHovered = true; // Should always be true when first opened, but this is set just in case

        possibleChildActivationAreas = List.of();

        lifetime = 0;
        lines = new ArrayList<>();
    };

    public StackedTextBox withActivationArea(Area area) {
        activationArea = area;
        return this;
    };

    public StackedTextBox withText(String text) {
        LinesAndActivationAreas result = getTextAndActivationAreas(text, x, y, 200, minecraft.font);

        lines.clear();
        lines.addAll(result.lines());
        possibleChildActivationAreas = result.areas();
        width = result.width();
        height = result.height();

        return this;
    };

    @Override
    protected void beforeRender(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        super.beforeRender(ms, mouseX, mouseY, partialTicks);
        isActivationAreaHovered = activationArea.isIn(mouseX, mouseY);

        // If there is a potential child to render
        if (child == AbstractStackedTextBox.NOTHING && lifetime >= PERMANENCE_LIFETIME) {
            for (Pair<Area, String> pair : possibleChildActivationAreas) {
                Area area = pair.getFirst();
                if (area.isIn(mouseX, mouseY)) {
                    child = new StackedTextBox(minecraft, mouseX, mouseY + 5, this)
                        .withActivationArea(area)
                        .withText(Component.translatable(pair.getSecond()).getString());
                };
            };
        };
    };

    @Override
    public void render(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        super.render(ms, mouseX, mouseY, partialTicks);

        // Don't render if the mouse isn't in the right place
        if (!isActive()) return;

        // Render the text
        Screen screen = minecraft.screen;
        ms.pushPose();
        if (screen != null) {
            ms.translate(0, 0, 10);
            List<Component> allLines = new ArrayList<>(lines);
            allLines.add(progressBar());
            screen.renderTooltip(ms, allLines, Optional.empty(), x - 17, y + 5);
        };

        // Render the next text box in the chain (if it exists)
        ms.pushPose();
        ms.translate(0, 0, 1);
        child.render(ms, mouseX, mouseY, partialTicks);
        ms.popPose();

        ms.popPose();
    };
    
    @Override
    protected void renderBg(PoseStack ms, Minecraft minecraft, int mouseX, int mouseY) {
        super.renderBg(ms, minecraft, mouseX, mouseY);
    };
    
    @Override
    public void tick() {
        super.tick();
        if (lifetime < PERMANENCE_LIFETIME) {
            lifetime++;
        };
        if (!isActive()) close();
        child.tick();
    };

    @Override
    public boolean isActive() {
        if (!active || !visible) return false;
        if (child.isActive()) return true;
        if (lifetime < PERMANENCE_LIFETIME) {
            return isActivationAreaHovered;
        } else {
            return isHovered || isActivationAreaHovered;
        }
    };

    @Override
    public void close() {
        lifetime = 0;
        child.close();
        if (parent != AbstractStackedTextBox.NOTHING) parent.child = AbstractStackedTextBox.NOTHING;
    };

    private Component progressBar() {
        float charWidth = minecraft.font.width("|");

        int total = (int) ((width - 5) / charWidth);
        int current = (int) ((float)lifetime * total / (float)PERMANENCE_LIFETIME);

        String bars = "";
        bars += ChatFormatting.GRAY + Strings.repeat("|", current);
        bars += ChatFormatting.DARK_GRAY + Strings.repeat("|", total - current);
        return Components.literal(bars);
    };
};