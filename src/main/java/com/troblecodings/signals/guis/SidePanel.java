package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIOnUpdate;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.signalbox.MainSignalIdentifier;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class SidePanel {

    private boolean showHelpPage = false;
    private final UIEntity helpPage = new UIEntity();
    private final UIEntity infoEntity = new UIEntity();
    private final UIButton helpPageButton = new UIButton(">");
    private final UIEntity lowerEntity;
    private final UIEntity button = new UIEntity();
    private final UIEntity label = new UIEntity();
    private final UIEntity spacerEntity = new UIEntity();
    private final UIEntity helpPageSpacer = new UIEntity();
    private UILabel counterLabel = new UILabel("");
    private final GuiSignalBox gui;

    private BiConsumer<BlockPos, SubsidiaryHolder> disableSubsidiary;

    public SidePanel(final UIEntity lowerEntity, final GuiSignalBox gui) {
        this.lowerEntity = lowerEntity;
        this.gui = gui;

        counterLabel.setCenterY(false);

        infoEntity.setInherits(true);
        infoEntity.add(new UIBox(UIBox.VBOX, 2));

        helpPage.setInherits(true);
        helpPage.add(new UIBox(UIBox.VBOX, 2));

        final UIRotate rotate = new UIRotate();
        rotate.setRotateZ((float) Math.PI / 2.0f);
        label.add(rotate);
        final UILabel labelComponent = new UILabel(I18Wrapper.format("info.infolabel"));
        labelComponent.setTextColor(new UIEntity().getBasicTextColor());
        label.add(labelComponent);
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

        spacerEntity.setInheritHeight(true);
        spacerEntity.add(new UIBox(UIBox.VBOX, 0));
        spacerEntity.add(button);
        spacerEntity.add(entity);
        spacerEntity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        lowerEntity.add(spacerEntity);

        helpPageSpacer.setHeight(30);
    }

    public void addHelpPageToPlane() {
        if (showHelpPage) {
            helpPageButton.setText(I18Wrapper.format("info.info") + "  >");
            spacerEntity.setWidth(80);
            helpPage.clearChildren();
            helpPage.add(infoEntity);
            lowerEntity.update();
        } else {
            helpPageButton.setText("<");
            helpPage.clearChildren();
            helpPage.add(helpPageSpacer);
            helpPage.add(label);
            spacerEntity.setWidth(12);
            lowerEntity.update();
        }
        infoEntity.forEach(entity -> entity.setVisible(showHelpPage));
        label.setVisible(true);
        button.setVisible(true);
        helpPageSpacer.setVisible(true);
        helpPage.update();
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
        infoEntity.clearChildren();
        infoEntity.add(GuiElements.createSpacerV(2));
        infoEntity.add(GuiElements.createLabel(I18Wrapper.format("info.nextelement"),
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

        infoEntity.add(preview);
        infoEntity.add(GuiElements.createSpacerV(2));
        infoEntity.add(GuiElements.createLabel("[R] = " + I18Wrapper.format("info.editor.key.r"),
                new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity
                .add(GuiElements.createLabel("[LMB] = " + I18Wrapper.format("info.editor.key.lmb"),
                        new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity
                .add(GuiElements.createLabel("[RMB] = " + I18Wrapper.format("info.editor.key.rmb"),
                        new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity.add(GuiElements.createSpacerV(2));
        infoEntity.add(GuiElements.createLabel(I18Wrapper.format("info.description"),
                new UIEntity().getBasicTextColor(), 0.8f));
        infoEntity.add(
                GuiElements.createLabel(I18Wrapper.format("info." + modes.toString().toLowerCase()),
                        new UIEntity().getInfoTextColor(), 0.5f));
        addHelpPageToPlane();
    }

    public void helpUsageMode(final Map<BlockPos, SubsidiaryHolder> subsidiaries,
            final SignalBoxNode node,
            final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries) {
        infoEntity.clearChildren();

        final UIEntity helpScroll = new UIEntity();
        helpScroll.setInherits(true);
        helpScroll.add(new UIBox(UIBox.HBOX, 0));
        helpScroll.add(new UIScissor());
        infoEntity.add(helpScroll);

        final UIEntity helpList = new UIEntity();
        helpScroll.add(helpList);
        helpList.setInherits(true);

        final UIScrollBox helpScrollbox = new UIScrollBox(UIBox.VBOX, 2);

        if (node != null)
            helpList.add(GuiElements.createLabel(node.getPoint().toShortString(),
                    new UIEntity().getBasicTextColor(), 0.8f));

        helpList.add(GuiElements.createLabel(I18Wrapper.format("info.keys"),
                new UIEntity().getBasicTextColor(), 0.8f));
        helpList.add(GuiElements.createLabel("[LMB] = " + I18Wrapper.format("info.usage.key.lmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpList.add(GuiElements.createLabel("[RMB] = " + I18Wrapper.format("info.usage.key.rmb"),
                new UIEntity().getInfoTextColor(), 0.5f));

        final Minecraft mc = Minecraft.getInstance();
        if (node != null) {
            final Map<ModeSet, PathOptionEntry> modes = node.getModes();
            final List<EnumGuiMode> guiModes = modes.keySet().stream().map(mode -> mode.mode)
                    .collect(Collectors.toList());
            helpList.add(GuiElements.createLabel(I18Wrapper.format("info.usage.node"),
                    new UIEntity().getBasicTextColor(), 0.8f));
            final UIEntity reset = GuiElements.createButton(I18Wrapper.format("button.reset"),
                    e -> {
                        final UIEntity screen = GuiElements.createScreen(selectionEntity -> {
                            final UIBox hbox = new UIBox(UIBox.VBOX, 3);
                            selectionEntity.add(hbox);
                            final UIEntity question = new UIEntity();
                            final UILabel label = new UILabel(I18Wrapper.format("sb.resetpage"));
                            label.setTextColor(0xFFFFFFFF);
                            question.setScaleX(1.1f);
                            question.setScaleY(1.1f);
                            question.add(label);
                            question.setInherits(true);
                            final UILabel info = new UILabel(I18Wrapper.format("sb.pathwayreset"));
                            info.setTextColor(0xFFFFFFFF);
                            final UIEntity infoEntity = new UIEntity();
                            infoEntity.add(info);
                            infoEntity.setInherits(true);
                            selectionEntity.add(question);
                            selectionEntity.add(infoEntity);
                            final UIEntity buttons = new UIEntity();
                            final UIEntity buttonYes = GuiElements
                                    .createButton(I18Wrapper.format("btn.yes"), e1 -> {
                                        gui.pop();
                                        gui.resetPathwayOnServer(node);
                                    });
                            final UIEntity buttonNo = GuiElements
                                    .createButton(I18Wrapper.format("btn.no"), e2 -> gui.pop());
                            buttons.setInherits(true);
                            final UIBox vbox = new UIBox(UIBox.HBOX, 1);
                            buttons.add(vbox);
                            buttons.add(buttonYes);
                            buttons.add(buttonNo);
                            selectionEntity.add(buttons);
                        });
                        gui.push(screen);
                    });
            reset.setScale(0.8f);
            reset.setX(5);
            helpList.add(reset);
            reset.add(new UIToolTip(I18Wrapper.format("button.reset.desc")));
            if (guiModes.contains(EnumGuiMode.HP)) {
                final UIEntity entity = GuiElements
                        .createBoolElement(BoolIntegerables.of("auto_pathway"), e -> {
                            gui.setAutoPoint(node.getPoint(), (byte) e);
                            node.setAutoPoint(e == 1 ? true : false);
                        }, node.isAutoPoint() ? 1 : 0);
                entity.setScale(0.8f);
                entity.setX(5);
                helpList.add(entity);
            }
            for (final Map.Entry<ModeSet, PathOptionEntry> mapEntry : modes.entrySet()) {
                final ModeSet mode = mapEntry.getKey();
                final PathOptionEntry option = mapEntry.getValue();

                if (option.containsEntry(PathEntryType.SIGNAL)) {
                    final BlockPos signalPos = option.getEntry(PathEntryType.SIGNAL).get();
                    final String signalName = ClientNameHandler
                            .getClientName(new StateInfo(mc.level, signalPos));
                    helpList.add(GuiElements.createLabel(
                            (signalName.isEmpty() ? "Rotaion: " + mode.rotation.toString()
                                    : signalName) + " - " + mode.mode.toString(),
                            new UIEntity().getBasicTextColor(), 0.8f));
                    if (!(mode.mode == EnumGuiMode.HP || mode.mode == EnumGuiMode.RS))
                        continue;
                    final UIEntity entity = GuiElements
                            .createButton(I18Wrapper.format("btn.subsidiary"), e -> {
                                final UIBox hbox = new UIBox(UIBox.VBOX, 1);
                                final UIEntity list = new UIEntity();
                                list.setInherits(true);
                                list.add(hbox);
                                list.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                        a -> gui.pop()));
                                final List<SubsidiaryState> possibleSubsidiaires = possibleSubsidiaries
                                        .getOrDefault(signalPos, SubsidiaryState.ALL_STATES);
                                possibleSubsidiaires.forEach(state -> {
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
                                                helpUsageMode(subsidiaries, node,
                                                        possibleSubsidiaries);
                                                final MainSignalIdentifier identifier = //
                                                        new MainSignalIdentifier(
                                                                new ModeIdentifier(node.getPoint(),
                                                                        mode),
                                                                signalPos, SignalState.combine(state
                                                                        .getSubsidiaryShowType()));
                                                final List<MainSignalIdentifier> greenSignals = //
                                                        gui.container.greenSignals.computeIfAbsent(
                                                                identifier.getPoint(),
                                                                _u -> new ArrayList<>());
                                                if (entry.state) {
                                                    if (greenSignals.contains(identifier))
                                                        greenSignals.remove(identifier);
                                                    greenSignals.add(identifier);
                                                } else {
                                                    greenSignals.remove(identifier);
                                                }
                                                gui.updateSignals(
                                                        ImmutableList.of(node.getPoint()));
                                                if (state.isCountable() && entry.state) {
                                                    gui.container.grid.countOne();
                                                    gui.updateCounter();
                                                    gui.sendCurrentCounterToServer();
                                                }
                                            }, defaultValue));
                                });
                                final UIEntity screen = GuiElements.createScreen(selection -> {
                                    selection.add(list);
                                    selection.add(GuiElements.createPageSelect(hbox));
                                });
                                gui.push(screen);
                            });
                    entity.setScale(0.8f);
                    entity.setX(5);
                    helpList.add(entity);
                    entity.add(new UIToolTip(I18Wrapper.format("btn.subsidiary.desc")));
                }
            }
            final UIEntity edit = GuiElements.createButton(I18Wrapper.format("info.usage.edit"),
                    e -> {
                        helpUsageMode(subsidiaries, null, possibleSubsidiaries);
                        gui.initializePageTileConfig(node);
                    });
            edit.setScale(0.8f);
            edit.setX(5);
            helpList.add(edit);
            edit.add(new UIToolTip(I18Wrapper.format("info.usage.edit.desc")));
        }

        if (!subsidiaries.isEmpty()) {
            helpList.add(GuiElements.createLabel(I18Wrapper.format("info.usage.subsidiary"),
                    new UIEntity().getBasicTextColor(), 0.8f));
            subsidiaries.forEach((pos, holder) -> {
                final String name = ClientNameHandler.getClientName(new StateInfo(mc.level, pos));
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
                        final UILabel info = new UILabel(I18Wrapper.format("sb.disablesubsidiary"));
                        info.setTextColor(0xFFFFFFFF);
                        final UIEntity infoEntity = new UIEntity();
                        infoEntity.add(info);
                        infoEntity.setInherits(true);
                        selectionEntity.add(question);
                        selectionEntity.add(infoEntity);
                        final UIEntity buttons = new UIEntity();
                        final UIEntity buttonYes = GuiElements
                                .createButton(I18Wrapper.format("btn.yes"), e1 -> {
                                    gui.pop();
                                    disableSubsidiary.accept(pos, holder);
                                });
                        final UIEntity buttonNo = GuiElements
                                .createButton(I18Wrapper.format("btn.no"), e2 -> gui.pop());
                        buttons.setInherits(true);
                        final UIBox vbox = new UIBox(UIBox.HBOX, 1);
                        buttons.add(vbox);
                        buttons.add(buttonYes);
                        buttons.add(buttonNo);
                        selectionEntity.add(buttons);
                    });
                    gui.push(screen);
                });
                button.setScale(0.8f);
                button.setX(5);
                helpList.add(button);
            });
        }
        final UIScroll helpScrolling = new UIScroll();
        final UIEntity helpScrollBar = GuiElements.createScrollBar(helpScrollbox, 7, helpScrolling);
        helpScrollbox.setConsumer(size -> {
            if (size > helpList.getHeight()) {
                helpScroll.add(helpScrolling);
                helpScroll.add(helpScrollBar);
            } else {
                helpScroll.remove(helpScrollBar);
                helpScroll.remove(helpScrolling);
            }
        });
        helpList.add(helpScrollbox);
        addHelpPageToPlane();
    }

    public void setDisableSubdsidiary(final BiConsumer<BlockPos, SubsidiaryHolder> consumer) {
        this.disableSubsidiary = consumer;
    }
}