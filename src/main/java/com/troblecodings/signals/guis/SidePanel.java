package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
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

    private boolean showHelpPage = true;
    private final UIEntity helpPage = new UIEntity();
    private final UIEntity infoEntity = new UIEntity();
    private final UIButton helpPageButton = new UIButton(">");
    private final UIEntity lowerEntity;
    private final UIEntity button = new UIEntity();
    private final UIEntity label = new UIEntity();
    private final UIEntity spacerEntity = new UIEntity();
    private final UIEntity helpPageSpacer = new UIEntity();
    private final GuiSignalBox gui;

    public SidePanel(final UIEntity lowerEntity, final GuiSignalBox gui) {
        this.lowerEntity = lowerEntity;
        this.gui = gui;

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

    public void updateNextNode(final int selection, final int rotation) {
        infoEntity.clearChildren();
        infoEntity.add(GuiElements.createSpacerV(2));
        infoEntity.add(GuiElements.createLabel(I18Wrapper.format("info.nextelement"),
                new UIEntity().getBasicTextColor(), 0.8f));

        final UIEntity preview = new UIEntity();
        preview.setWidth(70);
        preview.setHeight(70);
        preview.add(new UIColor(0xFFAFAFAF));
        final SignalBoxNode node = new SignalBoxNode(new Point(-1, -1));
        final EnumGuiMode modes = EnumGuiMode.values()[selection];
        node.add(new ModeSet(modes, Rotation.values()[rotation]));

        final UISignalBoxTile sbt = new UISignalBoxTile(node);
        final UIEntity sbtEntity = new UIEntity();
        sbtEntity.setWidth(50);
        sbtEntity.setHeight(50);
        sbtEntity.setX(10);
        sbtEntity.setY(10);
        sbtEntity.add(sbt);

        preview.add(sbtEntity);
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

    public void helpUsageMode(final SignalBoxNode node) {
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

        final Map<BlockPos, SubsidiaryHolder> subsidiaries = gui.enabledSubsidiaries;
        final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = gui.container.possibleSubsidiaries;
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

            final List<UIEntity> manuellOutputs = new ArrayList<>();
            final AtomicBoolean canBeManuelChanged = new AtomicBoolean(true);
            boolean isPathBlocked = false;

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
                                                helpUsageMode(node);
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

                } else if (option.containsEntry(PathEntryType.OUTPUT)) {
                    if (canBeManuelChanged.get())
                        for (final Map.Entry<ModeSet, PathOptionEntry> entry : modes.entrySet()) {
                            final Optional<EnumPathUsage> usage = entry.getValue()
                                    .getEntry(PathEntryType.PATHUSAGE);
                            if (usage.isPresent() && !usage.get().equals(EnumPathUsage.FREE)) {
                                canBeManuelChanged.set(false);
                                break;
                            }
                        }
                    final UILabel currentStatus = new UILabel(I18Wrapper.format("info.usage.status")
                            + " : " + I18Wrapper.format("info.usage.status.free"));
                    currentStatus.setTextColor(new UIEntity().getBasicTextColor());
                    final UIEntity statusEntity = new UIEntity();
                    statusEntity.setInheritWidth(true);
                    statusEntity.setHeight(20);
                    statusEntity.add(new UIScale(1.1f, 1.1f, 1));
                    statusEntity.add(currentStatus);

                    final String modeName = I18Wrapper.format("property." + mode.mode.name());
                    final String rotationName = I18Wrapper
                            .format("property." + mode.rotation.name() + ".rotation");
                    final UIEntity manuelButton = GuiElements
                            .createButton(I18Wrapper.format("info.usage.manuel") + " : " + modeName
                                    + " - " + rotationName, e1 -> {
                                        final Optional<EnumPathUsage> usage = option
                                                .getEntry(PathEntryType.PATHUSAGE);
                                        final UIEntity info = new UIEntity();
                                        info.setInherits(true);
                                        info.add(new UIBox(UIBox.VBOX, 5));
                                        info.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
                                        info.add(new UIClickable(_u -> gui.pop(), 1));
                                        info.add(statusEntity);
                                        final UIEntity textureEntity = new UIEntity();
                                        textureEntity.setHeight(40);
                                        textureEntity.setWidth(40);
                                        textureEntity.setX(120);
                                        textureEntity.add(new UIToolTip(
                                                I18Wrapper.format("info.usage.rs.desc")));
                                        if (canBeManuelChanged.get()) {
                                            if (node.containsManuellOutput(mode)) {
                                                textureEntity.add(
                                                        new UITexture(GuiSignalBox.REDSTONE_ON));
                                            } else {
                                                textureEntity.add(
                                                        new UITexture(GuiSignalBox.REDSTONE_OFF));
                                            }
                                        } else {
                                            if (usage.isPresent()
                                                    && !usage.get().equals(EnumPathUsage.FREE)) {
                                                textureEntity.add(new UITexture(
                                                        GuiSignalBox.REDSTONE_ON_BLOCKED));
                                            } else {
                                                textureEntity.add(new UITexture(
                                                        GuiSignalBox.REDSTONE_OFF_BLOCKED));
                                            }
                                        }
                                        info.add(textureEntity);
                                        final UILabel outputStatus = new UILabel(((usage.isPresent()
                                                && !usage.get().equals(EnumPathUsage.FREE))
                                                || node.containsManuellOutput(mode))
                                                        ? I18Wrapper.format("info.usage.rs.true")
                                                        : I18Wrapper.format("info.usage.rs.false"));
                                        outputStatus.setCenterY(false);
                                        outputStatus
                                                .setTextColor(new UIEntity().getBasicTextColor());
                                        final UIEntity outputEntity = new UIEntity();
                                        outputEntity.setInheritWidth(true);
                                        outputEntity.setHeight(20);
                                        outputEntity.add(outputStatus);
                                        info.add(outputEntity);
                                        if (canBeManuelChanged.get()) {
                                            info.add(GuiElements.createButton(
                                                    I18Wrapper.format("info.usage.change"), i -> {
                                                        final boolean turnOff = node
                                                                .containsManuellOutput(mode);
                                                        textureEntity.clear();
                                                        textureEntity.add(new UIToolTip(I18Wrapper
                                                                .format("info.usage.rs.desc")));
                                                        if (turnOff) {
                                                            gui.changeRedstoneOutput(
                                                                    node.getPoint(), mode, false);
                                                            outputStatus.setText(I18Wrapper
                                                                    .format("info.usage.rs.false"));
                                                            textureEntity.add(new UITexture(
                                                                    GuiSignalBox.REDSTONE_OFF));
                                                        } else {
                                                            gui.changeRedstoneOutput(
                                                                    node.getPoint(), mode, true);
                                                            outputStatus.setText(I18Wrapper
                                                                    .format("info.usage.rs.true"));
                                                            textureEntity.add(new UITexture(
                                                                    GuiSignalBox.REDSTONE_ON));
                                                        }
                                                    }));
                                        }
                                        gui.pop();
                                        final UIEntity screen = GuiElements
                                                .createScreen(e -> e.add(info));
                                        gui.push(screen);
                                    });
                    manuelButton.add(new UIToolTip(I18Wrapper.format("info.usage.manuel.desc")));
                    manuellOutputs.add(manuelButton);
                }
                final EnumPathUsage path = option.getEntry(PathEntryType.PATHUSAGE)
                        .orElse(EnumPathUsage.FREE);
                if (path.equals(EnumPathUsage.BLOCKED)) {
                    isPathBlocked = true;
                }
            }

            if (isPathBlocked) {
                final UIEntity trainNumberButton = GuiElements
                        .createButton(I18Wrapper.format("info.usage.trainnumber"), e -> {
                            final UIEntity layout = new UIEntity();
                            layout.add(new UIBox(UIBox.VBOX, 10));
                            layout.setInherits(true);
                            final UIEntity inputEntity = new UIEntity();
                            inputEntity.setInheritWidth(true);
                            inputEntity.setHeight(20);
                            final UITextInput input = new UITextInput("");
                            inputEntity.add(input);
                            inputEntity
                                    .add(new UIToolTip(I18Wrapper.format("sb.trainnumber.change")));
                            layout.add(GuiElements.createSpacerV(30));
                            layout.add(GuiElements.createLabel(
                                    I18Wrapper.format("info.usage.changetrainnumber"), 0xFFFFFFFF));
                            layout.add(inputEntity);

                            final UIEntity lowerEntity = new UIEntity();
                            lowerEntity.setInherits(true);
                            lowerEntity.add(new UIBox(UIBox.HBOX, 5));
                            lowerEntity.add(GuiElements.createSpacerH(7));
                            final UIEntity save = GuiElements
                                    .createButton(I18Wrapper.format("btn.save"), e1 -> {
                                        gui.sendTrainNumber(node.getPoint(), input.getText());
                                        input.setText("");
                                        gui.pop();
                                    });
                            save.add(new UIToolTip(I18Wrapper.format("sb.trainnumber.save")));
                            lowerEntity.add(save);
                            final UIEntity remove = GuiElements.createButton("x", e1 -> {
                                gui.deleteTrainNumber(node.getPoint());
                                gui.pop();
                            });
                            remove.add(new UIToolTip(I18Wrapper.format("sb.trainnumber.remove")));
                            lowerEntity.add(remove);

                            layout.add(lowerEntity);
                            layout.add(new UIClickable(e1 -> gui.pop(), 1));
                            gui.push(GuiElements.createScreen(screen -> screen.add(layout)));
                        });
                trainNumberButton.add(new UIToolTip("info.usage.trainnumber.desc"));
                trainNumberButton.setScale(0.8f);
                trainNumberButton.setX(5);
                helpList.add(trainNumberButton);
            }

            if (!manuellOutputs.isEmpty()) {
                final UIEntity manuellOutputList = GuiElements
                        .createButton(I18Wrapper.format("info.usage.manuel"), e -> {
                            gui.push(GuiElements.createScreen(screen -> {
                                manuellOutputs.forEach(screen::add);
                                screen.add(new UIClickable(e1 -> gui.pop(), 1));
                            }));
                        });
                manuellOutputList.add(new UIToolTip(I18Wrapper.format("info.usage.manuel.desc")));
                manuellOutputList.setScale(0.8f);
                manuellOutputList.setX(5);
                helpList.add(manuellOutputList);
            }

            final UIEntity edit = GuiElements.createButton(I18Wrapper.format("info.usage.edit"),
                    e -> {
                        helpUsageMode(null);
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
                                    gui.disableSubsidiary(pos, holder);
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
}