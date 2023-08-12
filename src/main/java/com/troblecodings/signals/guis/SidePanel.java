package com.troblecodings.signals.guis;

import java.util.Map;
import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIOnUpdate;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class SidePanel {

    private boolean showHelpPage = false;
    private final UIEntity helpPage = new UIEntity();
    private final UIButton helpPageButton = new UIButton(">");
    private final UIEntity lowerEntity;
    private final UIEntity button = new UIEntity();
    private final UIEntity label = new UIEntity();
    private final UIEntity spacerEntity = new UIEntity();
    private final GuiSignalBox gui;

    private BiConsumer<BlockPos, SubsidiaryHolder> disableSubsidiary;

    public SidePanel(final UIEntity lowerEntity, final GuiSignalBox gui) {
        this.lowerEntity = lowerEntity;
        this.gui = gui;

        helpPage.setInherits(true);
        helpPage.add(new UIBox(UIBox.VBOX, 2));

        final UIRotate rotate = new UIRotate();
        rotate.setRotateZ((float) Math.PI / 2.0f);
        label.add(rotate);
        final UIIndependentTranslate tl = new UIIndependentTranslate();
        tl.setX(-30);
        label.add(tl);
        final UILabel labelComponent = new UILabel(I18n.get("info.infolabel"));
        labelComponent.setTextColor(UIColor.BASIC_COLOR_PRIMARY);
        label.add(labelComponent);
        label.add(new UIOnUpdate(
                () -> label.setY((helpPage.getHeight() - labelComponent.getTextWidth()) / 2)));

        button.setInheritWidth(true);
        button.setHeight(20);
        button.add(helpPageButton);
        button.add(new UIClickable(entity -> {
            showHelpPage = !showHelpPage;
            addHelpPageToPlane();
        }));

        addHelpPageToPlane();

        final UIEntity entity = new UIEntity();
        entity.setInherits(true);
        entity.add(new UIBox(UIBox.HBOX, 0));
        entity.add(GuiElements.createSpacerH(4));
        entity.add(helpPage);
        entity.add(GuiElements.createSpacerH(4));

        spacerEntity.setInheritHeight(true);
        spacerEntity.add(new UIBox(UIBox.VBOX, 0));
        spacerEntity.add(button);
        spacerEntity.add(entity);
        spacerEntity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        lowerEntity.add(spacerEntity);
    }

    public void addHelpPageToPlane() {
        if (showHelpPage) {
            helpPageButton.setText(I18n.get("info.info") + "  >");
            helpPage.remove(label);
            spacerEntity.setWidth(80);
            lowerEntity.update();
        } else {
            helpPageButton.setText("<");
            helpPage.add(label);
            label.setX(2);
            spacerEntity.setWidth(12);
            lowerEntity.update();
        }
        helpPage.forEach(entity -> {
            entity.setVisible(showHelpPage);
        });
        button.setVisible(true);
        label.setVisible(true);
    }

    public void reset() {

    }

    public void add(final UIEntity entity) {
        this.helpPage.add(entity);
    }

    public void updateNextNode(final int selection, final int rotation) {
        helpPage.clearChildren();
        helpPage.add(GuiElements.createSpacerV(2));
        helpPage.add(GuiElements.createLabel(I18n.get("info.nextelement"),
                UIColor.BASIC_COLOR_PRIMARY, 0.8f));

        final UIEntity preview = new UIEntity();
        preview.setInheritWidth(true);
        preview.add(new UIOnUpdate(() -> {
            if (preview.getHeight() != preview.getWidth()) {
                preview.setHeight(preview.getWidth());
                helpPage.update();
            }
        }));
        preview.add(new UIColor(0xFFAFAFAF));
        final SignalBoxNode node = new SignalBoxNode(new Point(-1, -1));
        final EnumGuiMode modes = EnumGuiMode.values()[selection];
        node.add(new ModeSet(modes, Rotation.values()[rotation]));
        final UISignalBoxTile sbt = new UISignalBoxTile(node);
        preview.add(sbt);
        preview.add(new UIBorder(UIColor.BASIC_COLOR_PRIMARY));

        helpPage.add(preview);
        helpPage.add(GuiElements.createSpacerV(5));
        helpPage.add(GuiElements.createLabel("[R] = " + I18n.get("info.editor.key.r"),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        helpPage.add(GuiElements.createLabel("[LMB] = " + I18n.get("info.editor.key.lmb"),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        helpPage.add(GuiElements.createLabel("[RMB] = " + I18n.get("info.editor.key.rmb"),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        helpPage.add(GuiElements.createSpacerV(5));
        helpPage.add(GuiElements.createLabel(I18n.get("info.description"),
                UIColor.BASIC_COLOR_PRIMARY, 0.8f));
        helpPage.add(GuiElements.createLabel(I18n.get("info." + modes.toString().toLowerCase()),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        addHelpPageToPlane();
    }

    public void helpUsageMode(final Map<BlockPos, SubsidiaryHolder> subsidiaries) {
        helpPage.clearChildren();
        helpPage.add(
                GuiElements.createLabel(I18n.get("info.keys"), UIColor.BASIC_COLOR_PRIMARY, 0.8f));
        helpPage.add(GuiElements.createLabel("[LMB] = " + I18n.get("info.usage.key.lmb"),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        helpPage.add(GuiElements.createLabel("[RMB] = " + I18n.get("info.usage.key.rmb"),
                UIColor.INFO_COLOR_PRIMARY, 0.5f));
        if (!subsidiaries.isEmpty()) {
            helpPage.add(GuiElements.createLabel(I18n.get("info.usage.subsidiary"),
                    UIColor.BASIC_COLOR_PRIMARY, 0.8f));
            final Minecraft mc = Minecraft.getInstance();
            subsidiaries.forEach((pos, holder) -> {
                final String name = ClientNameHandler
                        .getClientName(new NameStateInfo(mc.level, pos)) + " : "
                        + holder.entry.enumValue.toString().toUpperCase();
                final UIEntity button = GuiElements.createButton(name, e -> {
                    final UIEntity screen = GuiElements.createScreen(selectionEntity -> {
                        final UIBox hbox = new UIBox(UIBox.VBOX, 3);
                        selectionEntity.add(hbox);
                        final UIEntity question = new UIEntity();
                        final UILabel label = new UILabel(name);
                        label.setTextColor(0xFFFFFFFF);
                        question.setScaleX(1.1f);
                        question.setScaleY(1.1f);
                        question.add(label);
                        question.setInherits(true);
                        final UILabel info = new UILabel(I18n.get("sb.disablesubsidiary"));
                        info.setTextColor(0xFFFFFFFF);
                        final UIEntity infoEntity = new UIEntity();
                        infoEntity.add(info);
                        infoEntity.setInherits(true);
                        selectionEntity.add(question);
                        selectionEntity.add(infoEntity);
                        final UIEntity buttons = new UIEntity();
                        final UIEntity buttonYes = GuiElements.createButton(I18n.get("btn.yes"),
                                e1 -> {
                                    gui.pop();
                                    disableSubsidiary.accept(pos, holder);
                                });
                        final UIEntity buttonNo = GuiElements.createButton(I18n.get("btn.no"),
                                e2 -> gui.pop());
                        buttons.setInherits(true);
                        final UIBox vbox = new UIBox(UIBox.HBOX, 1);
                        buttons.add(vbox);
                        buttons.add(buttonYes);
                        buttons.add(buttonNo);
                        selectionEntity.add(buttons);
                    });
                    gui.push(screen);
                });
                helpPage.add(button);
            });
        }
        addHelpPageToPlane();
    }

    public void setDisableSubdsidiary(final BiConsumer<BlockPos, SubsidiaryHolder> consumer) {
        this.disableSubsidiary = consumer;
    }
}