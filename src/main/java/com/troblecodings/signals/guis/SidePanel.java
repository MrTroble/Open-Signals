package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIOnUpdate;
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
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class SidePanel {

    public static final ResourceLocation REDSTONE_OFF = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/redstone_off.png");
    public static final ResourceLocation REDSTONE_OFF_BLOCKED = new ResourceLocation(
            OpenSignalsMain.MODID, "gui/textures/redstone_off_blocked.png");
    public static final ResourceLocation REDSTONE_ON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/redstone_on.png");
    public static final ResourceLocation REDSTONE_ON_BLOCKED = new ResourceLocation(
            OpenSignalsMain.MODID, "gui/textures/redstone_on_blocked.png");

    private boolean showHelpPage = false;
    private final UIEntity helpPage = new UIEntity();
    private final UIEntity infoEntity = new UIEntity();
    private final UIButton helpPageButton = new UIButton(">");
    private final UIEntity lowerEntity;
    private final UIEntity button = new UIEntity();
    private final UIEntity label = new UIEntity();
    private final UIEntity spacerEntity = new UIEntity();
    private final UIEntity helpPageSpacer = new UIEntity();
    private final GuiSignalBox gui;

    private BiConsumer<BlockPos, SubsidiaryHolder> disableSubsidiary;

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
        infoEntity.add(GuiElements.createSpacerV(5));
        infoEntity.add(GuiElements.createLabel("[R] = " + I18Wrapper.format("info.editor.key.r"),
                new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity
                .add(GuiElements.createLabel("[LMB] = " + I18Wrapper.format("info.editor.key.lmb"),
                        new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity
                .add(GuiElements.createLabel("[RMB] = " + I18Wrapper.format("info.editor.key.rmb"),
                        new UIEntity().getInfoTextColor(), 0.5f));
        infoEntity.add(GuiElements.createSpacerV(5));
        infoEntity.add(GuiElements.createLabel(I18Wrapper.format("info.description"),
                new UIEntity().getBasicTextColor(), 0.8f));
        infoEntity.add(
                GuiElements.createLabel(I18Wrapper.format("info." + modes.toString().toLowerCase()),
                        new UIEntity().getInfoTextColor(), 0.5f));
        addHelpPageToPlane();
    }

    public void helpUsageMode(final Map<BlockPos, SubsidiaryHolder> subsidiaries,
            final SignalBoxNode node, final List<SignalBoxNode> allNodes,
            final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries) {
        infoEntity.clearChildren();

        final UIEntity helpScroll = new UIEntity();
        helpScroll.setInheritHeight(true);
        helpScroll.setInheritWidth(true);
        helpScroll.add(new UIBox(UIBox.HBOX, 0));
        helpScroll.add(new UIScissor());
        infoEntity.add(helpScroll);

        final UIEntity helpList = new UIEntity();
        helpScroll.add(helpList);
        helpList.setInheritHeight(true);
        helpList.setInheritWidth(true);

        final UIScrollBox helpScrollbox = new UIScrollBox(UIBox.VBOX, 2);

        helpList.add(GuiElements.createLabel(I18Wrapper.format("info.keys"),
                new UIEntity().getBasicTextColor(), 0.8f));
        helpList.add(GuiElements.createLabel("[LMB] = " + I18Wrapper.format("info.usage.key.lmb"),
                new UIEntity().getInfoTextColor(), 0.5f));
        helpList.add(GuiElements.createLabel("[RMB] = " + I18Wrapper.format("info.usage.key.rmb"),
                new UIEntity().getInfoTextColor(), 0.5f));

        final Minecraft mc = Minecraft.getInstance();
        if (!allNodes.isEmpty()) {
            final UIEntity manuelButton = GuiElements
                    .createButton(I18Wrapper.format("info.usage.manuel"), e -> {
                        final UIEntity screen = GuiElements.createScreen(searchPanel -> {
                            final UIEntity searchBar = new UIEntity();
                            searchBar.setInheritWidth(true);
                            searchBar.setHeight(20);
                            final UITextInput input = new UITextInput("");
                            searchBar.add(input);
                            searchPanel.add(searchBar);

                            final UIEntity listWithScroll = new UIEntity();
                            listWithScroll.setInheritHeight(true);
                            listWithScroll.setInheritWidth(true);
                            listWithScroll.add(new UIBox(UIBox.HBOX, 2));
                            listWithScroll.add(new UIScissor());
                            listWithScroll.add(new UIBorder(0xFF00FFFF));
                            searchPanel.add(listWithScroll);

                            final UIEntity list = new UIEntity();
                            listWithScroll.add(list);
                            list.setInherits(true);

                            final UIScrollBox scrollbox = new UIScrollBox(UIBox.VBOX, 2);
                            list.add(scrollbox);
                            final Map<String, UIEntity> nameToUIEntity = new HashMap<>();

                            allNodes.forEach(currentNode -> {
                                final UILabel currentStatus = new UILabel(
                                        I18Wrapper.format("info.usage.status") + " : "
                                                + I18Wrapper.format("info.usage.status.free"));
                                currentStatus.setTextColor(new UIEntity().getBasicTextColor());
                                final UIEntity statusEntity = new UIEntity();
                                statusEntity.setInheritWidth(true);
                                statusEntity.setHeight(20);
                                statusEntity.add(new UIScale(1.1f, 1.1f, 1));
                                statusEntity.add(currentStatus);
                                final AtomicBoolean canBeManuelChanged = new AtomicBoolean(true);
                                currentNode.getModes().forEach((mode, entry) -> {
                                    final Optional<EnumPathUsage> pathUsage = entry
                                            .getEntry(PathEntryType.PATHUSAGE);
                                    if (pathUsage.isPresent()
                                            && !pathUsage.get().equals(EnumPathUsage.FREE)) {
                                        currentStatus.setText(I18Wrapper.format("info.usage.status")
                                                + " : "
                                                + I18Wrapper.format("info.usage.status.blocked"));
                                        canBeManuelChanged.set(false);
                                    }
                                    if (!entry.containsEntry(PathEntryType.OUTPUT))
                                        return;
                                    final String name = currentNode.getPoint().toString() + " - "
                                            + ClientNameHandler.getClientName(new NameStateInfo(
                                                    mc.level,
                                                    entry.getEntry(PathEntryType.OUTPUT).get()));
                                    final UIEntity button = GuiElements.createButton(name, e1 -> {
                                        gui.pop();
                                        final UIEntity info = new UIEntity();
                                        info.setInherits(true);
                                        info.add(new UIBox(UIBox.VBOX, 5));
                                        info.add(new UIClickable(_u -> gui.pop(), 1));
                                        info.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
                                        info.add(statusEntity);
                                        final UIEntity textureEntity = new UIEntity();
                                        textureEntity.setHeight(40);
                                        textureEntity.setWidth(40);
                                        textureEntity.setX(120);
                                        textureEntity.add(new UIToolTip(
                                                I18Wrapper.format("info.usage.rs.desc")));
                                        if (canBeManuelChanged.get()) {
                                            if (currentNode.containsManuellOutput(mode)) {
                                                textureEntity.add(new UITexture(REDSTONE_ON));
                                            } else {
                                                textureEntity.add(new UITexture(REDSTONE_OFF));
                                            }
                                        } else {
                                            if (pathUsage.isPresent() && !pathUsage.get()
                                                    .equals(EnumPathUsage.FREE)) {
                                                textureEntity
                                                        .add(new UITexture(REDSTONE_ON_BLOCKED));
                                            } else {
                                                textureEntity
                                                        .add(new UITexture(REDSTONE_OFF_BLOCKED));
                                            }
                                        }
                                        info.add(textureEntity);
                                        final UILabel outputStatus = new UILabel(((pathUsage
                                                .isPresent()
                                                && !pathUsage.get().equals(EnumPathUsage.FREE))
                                                || currentNode.containsManuellOutput(mode))
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
                                        if (!canBeManuelChanged.get()) {
                                            gui.push(GuiElements
                                                    .createScreen(entity -> entity.add(info)));
                                            return;
                                        }
                                        info.add(GuiElements.createButton(
                                                I18Wrapper.format("info.usage.change"), i -> {
                                                    final boolean turnOff = currentNode
                                                            .containsManuellOutput(mode);
                                                    textureEntity.clear();
                                                    textureEntity.add(new UIToolTip(I18Wrapper
                                                            .format("info.usage.rs.desc")));
                                                    if (turnOff) {
                                                        gui.changeRedstoneOutput(
                                                                currentNode.getPoint(), mode,
                                                                false);
                                                        outputStatus.setText(I18Wrapper
                                                                .format("info.usage.rs.false"));
                                                        textureEntity
                                                                .add(new UITexture(REDSTONE_OFF));
                                                    } else {
                                                        gui.changeRedstoneOutput(
                                                                currentNode.getPoint(), mode, true);
                                                        outputStatus.setText(I18Wrapper
                                                                .format("info.usage.rs.true"));
                                                        textureEntity
                                                                .add(new UITexture(REDSTONE_ON));
                                                    }
                                                }));
                                        gui.push(GuiElements
                                                .createScreen(entity -> entity.add(info)));
                                    });
                                    list.add(button);
                                    nameToUIEntity.put(name.toLowerCase(), button);

                                });
                            });
                            final UIScroll scroll = new UIScroll();
                            final UIEntity scrollBar = GuiElements.createScrollBar(scrollbox, 10,
                                    scroll);
                            scrollbox.setConsumer(size -> {
                                if (size > list.getHeight()) {
                                    listWithScroll.add(scroll);
                                    listWithScroll.add(scrollBar);
                                } else {
                                    listWithScroll.remove(scrollBar);
                                    listWithScroll.remove(scroll);
                                }
                            });
                            input.setOnTextUpdate(string -> {
                                nameToUIEntity.forEach((name, entity) -> {
                                    if (!name.contains(string.toLowerCase())) {
                                        list.remove(entity);
                                    } else {
                                        list.add(entity);
                                    }
                                });
                            });
                        });
                        screen.add(new UIClickable(e1 -> gui.pop(), 1));
                        gui.push(screen);
                    });
            manuelButton.setScaleX(0.8f);
            manuelButton.setScaleY(0.8f);
            manuelButton.setX(5);
            helpList.add(manuelButton);
            manuelButton.add(new UIToolTip(I18Wrapper.format("info.usage.manuel.desc")));
        }

        final UIEntity savedPathways = GuiElements
                .createButton(I18Wrapper.format("info.usage.savedpathways"), e -> {
                    final UIEntity screen = GuiElements.createScreen(entity -> {
                        final UIEntity listWithScroll = new UIEntity();
                        listWithScroll.setInheritHeight(true);
                        listWithScroll.setInheritWidth(true);
                        listWithScroll.add(new UIBox(UIBox.HBOX, 2));
                        listWithScroll.add(new UIScissor());
                        listWithScroll.add(new UIBorder(0xFF00FFFF));
                        entity.add(listWithScroll);

                        final UIEntity list = new UIEntity();
                        listWithScroll.add(list);
                        list.setInherits(true);

                        final UIScrollBox scrollbox = new UIScrollBox(UIBox.VBOX, 2);
                        list.add(scrollbox);

                        gui.container.nextPathways.forEach(entry -> {
                            final UIEntity layout = new UIEntity();
                            layout.setHeight(20);
                            layout.setInheritWidth(true);
                            layout.add(new UIBox(UIBox.HBOX, 2));
                            final UIEntity button = GuiElements
                                    .createButton("Start: " + entry.getKey().toShortString()
                                            + ", End: " + entry.getValue().toShortString());
                            layout.add(button);
                            layout.add(GuiElements
                                    .createButton(I18Wrapper.format("info.usage.show"), 40, _u -> {
                                        gui.resetTileSelection();
                                        gui.pop();
                                        setShowHelpPage(false);
                                        addColorToTile(entry.getKey(), entry.getValue(),
                                                GuiSignalBox.SELECTION_COLOR);
                                        // TODO Maby other color?
                                    }));
                            layout.add(GuiElements.createButton("x", 20, _u -> {
                                gui.container.nextPathways.remove(entry);
                                list.remove(layout);
                                gui.removeNextPathwayFromServer(entry.getKey(), entry.getValue());
                                gui.pop();
                            }));
                            list.add(layout);
                        });

                        final UIScroll scroll = new UIScroll();
                        final UIEntity scrollBar = GuiElements.createScrollBar(scrollbox, 10,
                                scroll);
                        scrollbox.setConsumer(size -> {
                            if (size > list.getHeight()) {
                                listWithScroll.add(scroll);
                                listWithScroll.add(scrollBar);
                            } else {
                                listWithScroll.remove(scrollBar);
                                listWithScroll.remove(scroll);
                            }
                        });
                    });
                    screen.add(new UIClickable(_u -> gui.pop(), 1));
                    gui.push(screen);
                });
        savedPathways.setScaleX(0.8f);
        savedPathways.setScaleY(0.8f);
        savedPathways.setX(5);
        helpList.add(savedPathways);

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
            reset.setScaleX(0.8f);
            reset.setScaleY(0.8f);
            reset.setX(5);
            helpList.add(reset);
            reset.add(new UIToolTip(I18Wrapper.format("button.reset.desc")));
            if (guiModes.contains(EnumGuiMode.HP)) {
                final UIEntity entity = GuiElements
                        .createBoolElement(BoolIntegerables.of("auto_pathway"), e -> {
                            gui.setAutoPoint(node.getPoint(), (byte) e);
                            node.setAutoPoint(e == 1 ? true : false);
                        }, node.isAutoPoint() ? 1 : 0);
                entity.setScaleX(0.8f);
                entity.setScaleY(0.8f);
                entity.setX(5);
                helpList.add(entity);
            }
            for (final Map.Entry<ModeSet, PathOptionEntry> mapEntry : modes.entrySet()) {
                final ModeSet mode = mapEntry.getKey();
                final PathOptionEntry option = mapEntry.getValue();

                if (option.containsEntry(PathEntryType.SIGNAL)) {
                    final BlockPos signalPos = option.getEntry(PathEntryType.SIGNAL).get();
                    final String signalName = ClientNameHandler
                            .getClientName(new NameStateInfo(mc.level, signalPos));
                    helpList.add(GuiElements.createLabel(
                            (signalName.isEmpty() ? "Rotaion: " + mode.rotation.toString()
                                    : signalName) + " - " + mode.mode.toString(),
                            new UIEntity().getBasicTextColor(), 0.8f));
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
                                                helpUsageMode(subsidiaries, node, allNodes,
                                                        possibleSubsidiaries);
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
                    helpList.add(entity);
                    entity.add(new UIToolTip(I18Wrapper.format("btn.subsidiary.desc")));
                }
            }
            final UIEntity edit = GuiElements.createButton(I18Wrapper.format("info.usage.edit"),
                    e -> {
                        helpUsageMode(subsidiaries, null, allNodes, possibleSubsidiaries);
                        gui.initializePageTileConfig(node);
                    });
            edit.setScaleX(0.8f);
            edit.setScaleY(0.8f);
            edit.setX(5);
            helpList.add(edit);
            edit.add(new UIToolTip(I18Wrapper.format("info.usage.edit.desc")));
        }

        if (!subsidiaries.isEmpty()) {
            helpList.add(GuiElements.createLabel(I18Wrapper.format("info.usage.subsidiary"),
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
                button.setScaleX(0.8f);
                button.setScaleY(0.8f);
                button.setX(5);
                helpList.add(button);
            });
        }
        final UIScroll helpScrolling = new UIScroll();
        final UIEntity helpScrollBar = GuiElements.createScrollBar(helpScrollbox, 7, helpScrolling);
        helpScrollbox.setConsumer(size -> {
            if (size > helpList.getHeight()) {
                helpScroll.add(helpScroll);
                helpScroll.add(helpScrollBar);
            } else {
                helpScroll.remove(helpScrollBar);
                helpScroll.remove(helpScroll);
            }
        });
        helpList.add(helpScrollbox);
        addHelpPageToPlane();
    }

    private void addColorToTile(final Point start, final Point end, final int color) {
        final UISignalBoxTile startTile = gui.allTiles.get(start);
        final UISignalBoxTile endTile = gui.allTiles.get(end);
        if (startTile == null || endTile == null)
            return;
        final UIColor uiColor = new UIColor(color);
        new Thread(() -> {
            startTile.getParent().add(uiColor);
            endTile.getParent().add(uiColor);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startTile.getParent().remove(uiColor);
            endTile.getParent().remove(uiColor);
        }, "GuiSignalBox:showNextPathway").start();
    }

    public void setDisableSubdsidiary(final BiConsumer<BlockPos, SubsidiaryHolder> consumer) {
        this.disableSubsidiary = consumer;
    }
}