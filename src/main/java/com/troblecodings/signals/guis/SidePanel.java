package com.troblecodings.signals.guis;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIOnUpdate;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

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
        final UILabel labelComponent = new UILabel(I18n.get("info.infolabel"));
        labelComponent.setTextColor(new UIEntity().getBasicTextColor());
        label.add(labelComponent);
        label.setY(25);
        label.setX(2);

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
            spacerEntity.setWidth(12);
            lowerEntity.update();
        }
        helpPage.forEach(entity -> {
            entity.setVisible(showHelpPage);
        });
        button.setVisible(true);
    }

    protected void setShowHelpPage(final boolean showHelpPage) {
        this.showHelpPage = showHelpPage;
        addHelpPageToPlane();
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
                new UIEntity().getBasicTextColor(), 0.8f));

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
        preview.add(new UIBorder(new UIEntity().getBasicTextColor()));

        helpPage.add(preview);
        helpPage.add(GuiElements.createSpacerV(5));
        helpPage.add(GuiElements.createLabel("[R] = " + I18n.get("info.editor.key.r"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpPage.add(GuiElements.createLabel("[LMB] = " + I18n.get("info.editor.key.lmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpPage.add(GuiElements.createLabel("[RMB] = " + I18n.get("info.editor.key.rmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpPage.add(GuiElements.createSpacerV(5));
        helpPage.add(GuiElements.createLabel(I18n.get("info.description"),
                new UIEntity().getBasicTextColor(), 0.8f));
        helpPage.add(GuiElements.createLabel(I18n.get("info." + modes.toString().toLowerCase()),
                new UIEntity().getInfoTextColor(), 0.5f));
        addHelpPageToPlane();
    }

    public void helpUsageMode(final Map<BlockPos, SubsidiaryHolder> subsidiaries,
            final SignalBoxNode node) {
        helpPage.clearChildren();
        helpPage.add(GuiElements.createLabel(I18n.get("info.keys"),
                new UIEntity().getBasicTextColor(), 0.8f));
        helpPage.add(GuiElements.createLabel("[LMB] = " + I18n.get("info.usage.key.lmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpPage.add(GuiElements.createLabel("[RMB] = " + I18n.get("info.usage.key.rmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        final Minecraft mc = Minecraft.getInstance();
        if (node != null) {
            final Map<ModeSet, PathOptionEntry> modes = node.getModes();
            final List<EnumGuiMode> guiModes = modes.keySet().stream().map(mode -> mode.mode)
                    .collect(Collectors.toList());
            helpPage.add(GuiElements.createLabel(I18n.get("info.usage.node"),
                    new UIEntity().getBasicTextColor(), 0.8f));
            final UIEntity reset = GuiElements.createButton(I18n.get("button.reset"),
                    e -> gui.resetPathwayOnServer(node));
            reset.setScaleX(0.8f);
            reset.setScaleY(0.8f);
            reset.setX(5);
            helpPage.add(reset);
            reset.add(new UIToolTip(I18n.get("button.reset.desc")));
            if (guiModes.contains(EnumGuiMode.HP)) {
                final UIEntity entity = GuiElements
                        .createBoolElement(BoolIntegerables.of("auto_pathway"), e -> {
                            gui.setAutoPoint(node.getPoint(), (byte) e);
                            node.setAutoPoint(e == 1 ? true : false);
                        }, node.isAutoPoint() ? 1 : 0);
                entity.setScaleX(0.8f);
                entity.setScaleY(0.8f);
                entity.setX(5);
                helpPage.add(entity);
            }
            for (final Map.Entry<ModeSet, PathOptionEntry> mapEntry : modes.entrySet()) {
                final ModeSet mode = mapEntry.getKey();
                final PathOptionEntry option = mapEntry.getValue();

                if (option.containsEntry(PathEntryType.SIGNAL)) {
                    final String signalName = ClientNameHandler.getClientName(new NameStateInfo(
                            mc.level, option.getEntry(PathEntryType.SIGNAL).get()));
                    helpPage.add(GuiElements.createLabel(
                            (signalName.isEmpty() ? "Rotaion: " + mode.rotation.toString()
                                    : signalName) + " - " + mode.mode.toString(),
                            new UIEntity().getBasicTextColor(), 0.8f));
                    final UIEntity entity = GuiElements.createButton(I18n.get("btn.subsidiary"),
                            e -> {
                                final UIBox hbox = new UIBox(UIBox.VBOX, 1);
                                final UIEntity list = new UIEntity();
                                list.setInherits(true);
                                list.add(hbox);
                                list.add(GuiElements.createButton(I18n.get("btn.return"),
                                        a -> gui.pop()));
                                SubsidiaryState.ALL_STATES.forEach(state -> {
                                    final int defaultValue = gui.container.grid.getSubsidiaryState(
                                            node.getPoint(), mode, state) ? 0 : 1;
                                    list.add(GuiElements.createEnumElement(
                                            new SizeIntegerables<>(state.getName(), 2,
                                                    i -> i == 1 ? "false" : "true"),
                                            a -> {
                                                final SubsidiaryEntry entry = new SubsidiaryEntry(
                                                        state, a == 0 ? true : false);
                                                gui.sendSubsidiaryRequest(entry, node.getPoint(),
                                                        mode);
                                                gui.container.grid.setClientState(node.getPoint(),
                                                        mode, entry);
                                                final BlockPos signalPos = option
                                                        .getEntry(PathEntryType.SIGNAL)
                                                        .orElse(null);
                                                if (signalPos != null) {
                                                    if (entry.state) {
                                                        subsidiaries.put(signalPos,
                                                                new SubsidiaryHolder(entry,
                                                                        node.getPoint(), mode));
                                                    } else {
                                                        subsidiaries.remove(signalPos);
                                                    }
                                                }
                                                gui.pop();
                                                helpUsageMode(subsidiaries, node);
                                            }, defaultValue));
                                });
                                final UIEntity screen = GuiElements.createScreen(selection -> {
                                    selection.add(list);
                                    selection.add(GuiElements.createPageSelect(hbox));
                                });
                                gui.push(screen);
                            });
                    entity.setScaleX(0.8f);
                    entity.setScaleY(0.8f);
                    entity.setX(5);
                    helpPage.add(entity);
                    entity.add(new UIToolTip(I18n.get("btn.subsidiary.desc")));
                }
            }
            final UIEntity edit = GuiElements.createButton(I18n.get("info.usage.edit"), e -> {
                helpUsageMode(subsidiaries, null);
                gui.initializePageTileConfig(node);
            });
            edit.setScaleX(0.8f);
            edit.setScaleY(0.8f);
            edit.setX(5);
            helpPage.add(edit);
            edit.add(new UIToolTip(I18n.get("info.usage.edit.desc")));
        }
        if (!subsidiaries.isEmpty()) {
            helpPage.add(GuiElements.createLabel(I18n.get("info.usage.subsidiary"),
                    new UIEntity().getBasicTextColor(), 0.8f));
            subsidiaries.forEach((pos, holder) -> {
                final String name = ClientNameHandler
                        .getClientName(new NameStateInfo(mc.level, pos));
                final UIEntity button = GuiElements.createButton(name, e -> {
                    final UIEntity screen = GuiElements.createScreen(selectionEntity -> {
                        final UIBox hbox = new UIBox(UIBox.VBOX, 3);
                        selectionEntity.add(hbox);
                        final UIEntity question = new UIEntity();
                        final UILabel label = new UILabel(
                                name + " : " + holder.entry.enumValue.toString().toUpperCase());
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
                button.setScaleX(0.8f);
                button.setScaleY(0.8f);
                button.setX(5);
                helpPage.add(button);
            });
        }
        addHelpPageToPlane();
    }

    public void setDisableSubdsidiary(final BiConsumer<BlockPos, SubsidiaryHolder> consumer) {
        this.disableSubsidiary = consumer;
    }
}