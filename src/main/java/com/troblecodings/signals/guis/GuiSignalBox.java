package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIStack;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Path;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxUtil;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class GuiSignalBox extends GuiBase {

    private static final int SELECTION_COLOR = 0x2900FF00;
    private static final int BACKGROUND_COLOR = 0xFF8B8B8B;
    private static final int LINE_COLOR = 0xFF5B5B5B;

    private static final float[] ALL_LINES = getLines();
    private static final int TILE_WIDTH = 10;
    private static final int TILE_COUNT = 100;

    private static float[] getLines() {
        final float[] lines = new float[2 * (TILE_COUNT + 1) * 4];
        final float step = 1.0f / TILE_COUNT;
        for (int i = 0; i <= TILE_COUNT; i++) {
            final int offset = i * 4;
            final float pos = i * step;
            lines[offset] = pos;
            lines[offset + 1] = 0;
            lines[offset + 2] = pos;
            lines[offset + 3] = 1;

            final int offset2 = (i + TILE_COUNT + 1) * 4;
            lines[offset2] = 0;
            lines[offset2 + 1] = pos;
            lines[offset2 + 2] = 1;
            lines[offset2 + 3] = pos;
        }
        return lines;
    }

    private final UIEntity lowerEntity = new UIEntity();
    private final ContainerSignalBox container;
    private UISignalBoxTile lastTile = null;
    @SuppressWarnings("unused")
    private Page page = Page.USAGE;
    private UIEntity mainButton;
    private final GuiInfo info;
    private final Map<Point, SignalBoxNode> changedModes = new HashMap<>();
    private UIEntity plane = null;
    private boolean allPacketsRecived = false;
    private final Map<Point, UISignalBoxTile> allTiles = new HashMap<>();

    public GuiSignalBox(final GuiInfo info) {
        super(info);
        this.container = (ContainerSignalBox) info.base;
        container.setConsumer(this::update);
        container.setColorUpdater(this::applyColorChanges);
        info.player.containerMenu = this.container;
        this.info = info;
    }

    public void update(final String errorString) {
        this.resetTileSelection();
        final UIToolTip tooltip = new UIToolTip(errorString);
        lowerEntity.add(tooltip);
        new Thread(() -> {
            try {
                Thread.sleep(4000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            lowerEntity.remove(tooltip);
        }).start();
        return;
    }

    private void resetTileSelection() {
        this.entity.findRecursive(UIColor.class).stream()
                .filter(color -> color.getColor() == SELECTION_COLOR)
                .forEach(color -> color.getParent().remove(color));
    }

    private void selectLink(final UIEntity parent, final SignalBoxNode node,
            final PathOptionEntry option, final Set<Entry<BlockPos, LinkType>> entrySet,
            final LinkType type, final PathEntryType<BlockPos> entryType, final EnumGuiMode mode,
            final Rotation rotation) {
        this.selectLink(parent, node, option, entrySet, type, entryType, mode, rotation, "");
    }

    private void selectLink(final UIEntity parent, final SignalBoxNode node,
            final PathOptionEntry option, final Set<Entry<BlockPos, LinkType>> entrySet,
            final LinkType type, final PathEntryType<BlockPos> entryType, final EnumGuiMode mode,
            final Rotation rotation, final String suffix) {
        final List<BlockPos> positions = new ArrayList<>();
        positions.addAll(entrySet.stream().filter(e -> e.getValue().equals(type))
                .map(e -> e.getKey()).collect(Collectors.toList()));
        if (positions.size() > 0) {
            final DisableIntegerable<String> blockPos = new DisableIntegerable<>(
                    SizeIntegerables.of("prop." + type.name() + suffix, positions.size(), id -> {
                        final BlockPos pos = positions.get(id);
                        if (pos == null)
                            return "Disabled";
                        return getSignalInfo(pos, type);
                    }));
            final UIEntity blockSelect = GuiElements.createEnumElement(blockPos, id -> {
                final BlockPos setPos = id >= 0 ? positions.get(id) : null;
                if (setPos == null) {
                    if (option.getEntry(entryType).isEmpty())
                        return;
                    option.removeEntry(entryType);
                    removeEntryFromServer(node, mode, rotation, entryType);
                } else {
                    final Optional<BlockPos> pathEntry = option.getEntry(entryType);
                    if (pathEntry.isPresent() && pathEntry.get().equals(setPos))
                        return;
                    option.setEntry(entryType, setPos);
                    sendPosEntryToServer(setPos, node, mode, rotation, entryType);
                }
            }, option.getEntry(entryType).map(entry -> positions.indexOf(entry)).orElse(-1));
            parent.add(blockSelect);
        }
    }

    private String getSignalInfo(final BlockPos signalPos, final LinkType type) {
        final String customName = ClientNameHandler
                .getClientName(new NameStateInfo(mc.level, signalPos));
        return String.format("%s (x=%d, y=%d. z=%d)",
                customName == null
                        ? (type.equals(LinkType.SIGNAL) ? "" : I18n.get("type." + type.name()))
                        : customName,
                signalPos.getX(), signalPos.getY(), signalPos.getZ());
    }

    private void setupModeSettings(final UIEntity parent, final EnumGuiMode mode,
            final Rotation rotation, final SignalBoxNode node, final PathOptionEntry option) {
        final String modeName = I18n.get("property." + mode.name());
        final String rotationName = I18n.get("property." + rotation.name() + ".rotation");
        final UIEntity entity = new UIEntity();
        entity.setInheritWidth(true);
        entity.setHeight(20);
        entity.add(new UIColor(BACKGROUND_COLOR));
        entity.add(new UIScale(1.1f, 1.1f, 1));
        final UILabel modeLabel = new UILabel(modeName + " - " + rotationName);
        modeLabel.setCenterX(false);
        entity.add(modeLabel);
        parent.add(entity);
        final Set<Entry<BlockPos, LinkType>> entrySet = container.getPositionForTypes().entrySet();

        switch (mode) {
            case CORNER:
            case STRAIGHT: {
                final EnumPathUsage path = option.getEntry(PathEntryType.PATHUSAGE)
                        .orElse(EnumPathUsage.FREE);
                final UIEntity stateEntity = new UIEntity();
                stateEntity.setInheritWidth(true);
                stateEntity.setHeight(15);
                final String pathUsageName = I18n.get("property.status") + ": ";
                final String pathUsage = I18n.get("property." + path);
                stateEntity.add(new UILabel(pathUsageName + pathUsage));
                parent.add(stateEntity);

                final SizeIntegerables<Integer> size = new SizeIntegerables<>("speed", 15, i -> i);
                final UIEntity speedSelection = GuiElements.createEnumElement(size, id -> {
                    final int speed = id > 0 ? id : 127;
                    final Optional<Integer> opt = option.getEntry(PathEntryType.SPEED);
                    if (speed == 127 && opt.isPresent()) {
                        removeEntryFromServer(node, mode, rotation, PathEntryType.SPEED);
                        option.removeEntry(PathEntryType.SPEED);
                    } else if ((opt.isPresent() && opt.get() != speed)
                            || (opt.isEmpty() && speed != 127)) {
                        sendIntEntryToServer(speed, node, mode, rotation, PathEntryType.SPEED);
                        option.setEntry(PathEntryType.SPEED, speed);
                    }
                }, option.getEntry(PathEntryType.SPEED).filter(n -> n < 16).orElse(127));
                parent.add(speedSelection);

                selectLink(parent, node, option, entrySet, LinkType.OUTPUT, PathEntryType.OUTPUT,
                        mode, rotation);
                if (option.getEntry(PathEntryType.OUTPUT).isPresent())
                    parent.add(
                            GuiElements.createBoolElement(BoolIntegerables.of("manuell_rs"), e -> {
                                changeRedstoneOutput(node.getPoint(), new ModeSet(mode, rotation),
                                        e == 1 ? true : false);
                            }, node.containsManuellOutput(new ModeSet(mode, rotation)) ? 1 : 0));

                selectLink(parent, node, option, entrySet, LinkType.INPUT, PathEntryType.BLOCKING,
                        mode, rotation, ".blocking");
                selectLink(parent, node, option, entrySet, LinkType.INPUT, PathEntryType.RESETING,
                        mode, rotation, ".resetting");

                final UIEntity zs2Entity = GuiElements.createEnumElement(JsonEnumHolder.ZS32, e -> {
                    if (e == 0) {
                        removeEntryFromServer(node, mode, rotation, PathEntryType.ZS2);
                    } else {
                        sendZS2Entry((byte) e, node, mode, rotation, PathEntryType.ZS2);
                    }
                }, option.getEntry(PathEntryType.ZS2).orElse((byte) 0));
                parent.add(zs2Entity);

                parent.add(GuiElements.createButton(I18n.get("button.reset"), e -> {
                    reset();
                    initializeFieldUsage(mainButton);
                    resetPathwayOnServer(node);
                }));
            }
                break;
            case VP:
                selectLink(parent, node, option, entrySet, LinkType.SIGNAL, PathEntryType.SIGNAL,
                        mode, rotation);
                break;
            case HP: {
                parent.add(GuiElements.createBoolElement(BoolIntegerables.of("auto_pathway"), e -> {
                    setAutoPoint(node.getPoint(), (byte) e);
                    node.setAutoPoint(e == 1 ? true : false);
                }, node.isAutoPoint() ? 1 : 0));
            }
            case RS: {
                if (option.containsEntry(PathEntryType.SIGNAL))
                    parent.add(GuiElements.createButton(I18n.get("btn.subsidiary"), e -> {
                        final UIBox hbox = new UIBox(UIBox.VBOX, 1);
                        final UIEntity list = new UIEntity();
                        list.setInherits(true);
                        list.add(hbox);
                        list.add(GuiElements.createButton(I18n.get("btn.return"), a -> {
                            pop();
                        }));
                        final ModeSet modeSet = new ModeSet(mode, rotation);
                        SubsidiaryState.ALL_STATES.forEach(state -> {
                            final int defaultValue = container.grid
                                    .getSubsidiaryState(node.getPoint(), modeSet, state) ? 0 : 1;
                            list.add(GuiElements.createEnumElement(new SizeIntegerables<>(
                                    state.getName(), 2, i -> i == 1 ? "false" : "true"), a -> {
                                        final SubsidiaryEntry entry = new SubsidiaryEntry(state,
                                                a == 0 ? true : false);
                                        sendSubsidiaryRequest(entry, node, modeSet);
                                        container.grid.setClientState(node.getPoint(), modeSet,
                                                entry);
                                        pop();
                                    }, defaultValue));
                        });
                        final UIEntity screen = GuiElements.createScreen(selection -> {
                            selection.add(list);
                            selection.add(GuiElements.createPageSelect(hbox));
                        });
                        push(screen);
                    }));
                selectLink(parent, node, option, entrySet, LinkType.SIGNAL, PathEntryType.SIGNAL,
                        mode, rotation);
            }
                break;
            default:
                break;
        }
    }

    private void tileEdit(final UIEntity tile, final UIMenu menu, final UISignalBoxTile sbt) {
        tile.add(new UIClickable(e -> {
            final EnumGuiMode mode = EnumGuiMode.values()[menu.getSelection()];
            final Rotation rotation = Rotation.values()[menu.getRotation()];
            final ModeSet modeSet = new ModeSet(mode, rotation);
            final SignalBoxNode node = sbt.getNode();
            if (sbt.has(modeSet)) {
                sbt.remove(modeSet);
            } else {
                sbt.add(modeSet);
            }
            changedModes.put(sbt.getPoint(), node);
        }));
    }

    private void tileNormal(final UIEntity tile, final UISignalBoxTile currentTile) {
        tile.add(new UIClickable(c -> {
            if (!currentTile.isValidStart())
                return;
            c.add(new UIColor(SELECTION_COLOR));
            if (lastTile == null) {
                lastTile = currentTile;
            } else {
                if (lastTile == currentTile) {
                    this.resetTileSelection();
                    lastTile = null;
                    return;
                }
                sendPWRequest(currentTile.getNode());
                this.resetTileSelection();
                lastTile = null;
            }
        }));
        tile.add(new UIClickable(e -> initializePageTileConfig(currentTile.getNode()), 1));
    }

    private void resetSelection(final UIEntity entity) {
        final UIEntity parent = entity.getParent();
        parent.findRecursive(UIClickable.class).forEach(click -> click.setVisible(true));
        parent.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(true));
        entity.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(false));
        entity.findRecursive(UIClickable.class).forEach(click -> click.setVisible(false));
    }

    private void initializePageTileConfig(final SignalBoxNode node) {
        if (node.isEmpty())
            return;
        reset();
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox box = new UIBox(UIBox.VBOX, 1);
        list.add(box);
        lowerEntity.add(new UIBox(UIBox.VBOX, 3));
        lowerEntity.add(list);
        final UIEntity input = new UIEntity();
        input.setInheritWidth(true);
        final UITextInput namingInput = new UITextInput(node.getCustomText());
        input.add(namingInput);
        input.setHeight(20);
        list.add(input);
        namingInput.setOnTextUpdate(str -> {
            node.setCustomText(str);
            sendName(node.getPoint(), str);
        });
        node.forEach(modeSet -> setupModeSettings(list, modeSet.mode, modeSet.rotation, node,
                node.getOption(modeSet).get()));
        lowerEntity.add(GuiElements.createPageSelect(box));
        lowerEntity.add(new UIClickable(e -> {
            initializeFieldUsage(mainButton);
        }, 1));
        pageCheck(Page.TILE_CONFIG);
    }

    private void pageCheck(final Page page) {
        this.page = page;
    }

    private void initializePageSettings(final UIEntity entity) {
        reset();
        this.pageCheck(Page.SETTINGS);
        lowerEntity.add(new UIBox(UIBox.VBOX, 2));
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        final UIEntity list = new UIEntity();
        final UIBox uibox = new UIBox(UIBox.VBOX, 2);
        list.add(uibox);
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        container.getPositionForTypes().forEach((p, t) -> {
            final String name = getSignalInfo(p, t);
            final UIEntity layout = new UIEntity();
            layout.setHeight(20);
            layout.setInheritWidth(true);
            layout.add(new UIBox(UIBox.HBOX, 2));

            final int id = t.ordinal();
            final UIEntity icon = new UIEntity();
            icon.add(new UITexture(UISignalBoxTile.ICON, 0.2 * id, 0.5, 0.2 * id + 0.2, 1));
            icon.setHeight(20);
            icon.setWidth(20);
            icon.add(new UIToolTip(I18n.get("type." + t.name())));
            layout.add(icon);

            layout.add(GuiElements.createButton(name));
            layout.add(GuiElements.createButton("x", 20, e -> {
                removeBlockPos(p);
                list.remove(layout);
            }));
            list.add(layout);
        });
        lowerEntity.add(list);
        lowerEntity.add(GuiElements.createPageSelect(uibox));
        resetSelection(entity);
    }

    private void initializeFieldUsage(final UIEntity entity) {
        reset();
        sendModeChanges();
        initializeFieldTemplate(this::tileNormal);
        resetSelection(entity);
        this.pageCheck(Page.USAGE);
    }

    private void initializeFieldEdit(final UIEntity entity) {
        final UIEntity screen = GuiElements.createScreen(selectionEntity -> {
            final UIBox hbox = new UIBox(UIBox.VBOX, 3);
            selectionEntity.add(hbox);
            final UIEntity question = new UIEntity();
            final UILabel label = new UILabel(I18n.get("sb.editmode"));
            label.setTextColor(0xFFFFFFFF);
            question.setScaleX(1.1f);
            question.setScaleY(1.1f);
            question.add(label);
            question.setInherits(true);
            final UILabel info = new UILabel(I18n.get("sb.allreset"));
            info.setTextColor(0xFFFFFFFF);
            final UIEntity infoEntity = new UIEntity();
            infoEntity.add(info);
            infoEntity.setInherits(true);
            selectionEntity.add(question);
            selectionEntity.add(infoEntity);
            final UIEntity buttons = new UIEntity();
            final UIEntity buttonYes = GuiElements.createButton(I18n.get("btn.yes"), e -> {
                pop();
                reset();
                final UIMenu menu = new UIMenu();
                menu.setVisible(false);
                initializeFieldTemplate(
                        (fieldEntity, name) -> this.tileEdit(fieldEntity, menu, name));
                lowerEntity.add(menu);
                menu.setConsumer((selection, rotation) -> updateNextNode(selection, rotation));
                resetSelection(entity);
                this.pageCheck(Page.EDIT);
                resetAllPathways();
                updateNextNode(menu.getSelection(), menu.getRotation());
            });
            final UIEntity buttonNo = GuiElements.createButton(I18n.get("btn.no"), e -> {
                pop();
            });
            buttons.setInherits(true);
            final UIBox vbox = new UIBox(UIBox.HBOX, 1);
            buttons.add(vbox);
            buttons.add(buttonYes);
            buttons.add(buttonNo);
            selectionEntity.add(buttons);
        });
        push(screen);
        pageCheck(Page.CHANGE_MODE);
    }

    private void updateNextNode(final int selection, final int rotation) {
        helpPage.clearChildren();

        final UIEntity list = new UIEntity();
        list.add(new UIBox(UIBox.VBOX, 2));
        list.setInherits(true);

        final UIEntity nextNode = new UIEntity();
        nextNode.add(new UIBox(UIBox.VBOX, 0));
        nextNode.setHeight(30);
        nextNode.setInheritWidth(true);

        final UIEntity labelEntity = new UIEntity();
        final UILabel nameLabel = new UILabel(I18n.get("label.nextnode"));
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setCenterY(false);
        labelEntity.add(new UIScale(0.8f, 0.8f, 0.8f));
        labelEntity.add(nameLabel);
        labelEntity.setInherits(true);
        nextNode.add(labelEntity);

        final UIEntity preview = new UIEntity();
        preview.setHeight(20);
        preview.setWidth(20);
        preview.setX(23);
        preview.add(new UIColor(0xFFAFAFAF));
        final SignalBoxNode node = new SignalBoxNode(new Point(-1, -1));
        node.add(new ModeSet(EnumGuiMode.values()[selection], Rotation.values()[rotation]));
        final UISignalBoxTile sbt = new UISignalBoxTile(node);
        preview.add(sbt);
        nextNode.add(preview);

        list.add(nextNode);
        list.add(GuiElements.createSpacerV(5));

        final UIEntity rKeyEntity = new UIEntity();
        final UILabel rKey = new UILabel(I18n.get("label.rkey"));
        rKey.setTextColor(0xFFFFFFFF);
        rKey.setCenterY(false);
        rKeyEntity.add(new UIScale(0.8f, 0.8f, 0.8f));
        rKeyEntity.add(rKey);
        rKeyEntity.setInherits(true);
        list.add(rKeyEntity);

        helpPage.add(list);
    }

    private boolean showHelpPage = false;
    private UIEntity helpPage;

    private void addHelpPageToPlane() {
        if (showHelpPage) {
            lowerEntity.add(helpPage);
        } else {
            lowerEntity.remove(helpPage);
        }
    }

    private void initializeFieldTemplate(final BiConsumer<UIEntity, UISignalBoxTile> consumer) {
        lowerEntity.add(new UIColor(BACKGROUND_COLOR));
        lowerEntity.add(new UIStack());
        lowerEntity.add(new UIScissor());

        plane = new UIEntity();
        plane.clearChildren();
        plane.setWidth(TILE_COUNT * TILE_WIDTH);
        plane.setHeight(TILE_COUNT * TILE_WIDTH);
        lowerEntity.add(new UIScroll(s -> {
            final float newScale = (float) (plane.getScaleX() + s * 0.05f);
            if (newScale <= 0)
                return;
            plane.setScaleX(newScale);
            plane.setScaleY(newScale);
            plane.update();
        }));
        lowerEntity.add(new UIDrag((x, y) -> {
            plane.setX(plane.getX() + x);
            plane.setY(plane.getY() + y);
            plane.update();
        }, 2));
        final UILines allLines = new UILines(ALL_LINES, 0.5F);
        allLines.setColor(LINE_COLOR);
        plane.add(allLines);
        final UIBox vbox = new UIBox(UIBox.VBOX, 0);
        vbox.setPageable(false);
        plane.add(vbox);
        allTiles.clear();
        for (int x = 0; x < TILE_COUNT; x++) {
            final UIEntity row = new UIEntity();
            final UIBox hbox = new UIBox(UIBox.HBOX, 0);
            hbox.setPageable(false);
            row.add(hbox);
            row.setHeight(TILE_WIDTH);
            row.setWidth(TILE_WIDTH);
            for (int y = 0; y < TILE_COUNT; y++) {
                final UIEntity tile = new UIEntity();
                tile.setHeight(TILE_WIDTH);
                tile.setWidth(TILE_WIDTH);
                final Point name = new Point(y, x);
                SignalBoxNode node = container.grid.getNode(name);
                if (node == null) {
                    node = new SignalBoxNode(name);
                }
                final UISignalBoxTile sbt = new UISignalBoxTile(node);
                if (!node.isEmpty())
                    allTiles.put(name, sbt);
                tile.add(sbt);
                if (!node.getCustomText().isEmpty()) {
                    final UIEntity inputEntity = new UIEntity();
                    inputEntity.add(new UIScale(0.7f, 0.7f, 0.7f));
                    final UILabel label = new UILabel(node.getCustomText());
                    label.setTextColor(0xFFFFFFFF);
                    inputEntity.add(label);
                    inputEntity.setX(5);
                    tile.add(inputEntity);
                }
                consumer.accept(tile, sbt);
                row.add(tile);
            }
            plane.add(row);
        }
        lowerEntity.add(plane);

        final UIEntity frame = new UIEntity();
        frame.setInheritHeight(true);
        frame.setInheritWidth(true);
        frame.add(new UIBorder(0xFF000000, 4));
        lowerEntity.add(frame);
        if (helpPage == null) {
            helpPage = new UIEntity();
            helpPage.setWidth(80);
            helpPage.setInheritHeight(true);
            helpPage.add(new UIBox(UIBox.VBOX, 2));
            helpPage.setX(260);
            helpPage.add(new UIColor(BACKGROUND_COLOR));
            helpPage.add(new UIBorder(LINE_COLOR, 2));
        } else {
            helpPage.clearChildren();
        }
        addHelpPageToPlane();
        buildColors(container.grid.getNodes());
    }

    private void initializeBasicUI() {
        final String name = I18n.get("tile.signalbox.name");

        final UILabel titlelabel = new UILabel(name);
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInheritHeight(true);
        titel.setInheritWidth(true);

        final UIEntity header = new UIEntity();
        header.setInheritWidth(true);
        header.setHeight(20);
        header.add(new UIBox(UIBox.HBOX, 4));
        header.add(titel);
        header.add(GuiElements.createSpacerH(40));
        header.add(
                GuiElements.createButton(I18n.get("btn.settings"), this::initializePageSettings));
        header.add(GuiElements.createButton(I18n.get("btn.edit"), this::initializeFieldEdit));
        mainButton = GuiElements.createButton(I18n.get("btn.main"), this::initializeFieldUsage);
        header.add(mainButton);
        final UIEntity helpBool = GuiElements.createBoolElement(BoolIntegerables.of("btn.help"),
                e -> {
                    showHelpPage = e == 1 ? true : false;
                    addHelpPageToPlane();
                });
        helpBool.setY(5);
        header.add(GuiElements.createSpacerH(5));
        header.add(helpBool);
        resetSelection(mainButton);

        final UIEntity middlePart = new UIEntity();
        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(lowerEntity);

        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        initializeFieldTemplate(this::tileNormal);

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));
    }

    private void sendPWRequest(final SignalBoxNode currentNode) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.REQUEST_PW.ordinal());
        lastTile.getPoint().writeNetwork(buffer);
        currentNode.getPoint().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void resetPathwayOnServer(final SignalBoxNode node) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.RESET_PW.ordinal());
        node.getPoint().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void sendPosEntryToServer(final BlockPos pos, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<BlockPos> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_POS_ENTRY.ordinal());
        buffer.putBlockPos(pos);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
        node.addAndSetEntry(new ModeSet(mode, rotation), entry, pos);
        container.grid.putNode(node.getPoint(), node);
    }

    private void sendIntEntryToServer(final int speed, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<Integer> entry) {
        if (speed == 127 || !allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_INT_ENTRY.ordinal());
        buffer.putByte((byte) speed);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
        node.addAndSetEntry(new ModeSet(mode, rotation), entry, speed);
        container.grid.putNode(node.getPoint(), node);
    }

    private void sendZS2Entry(final byte value, final SignalBoxNode node, final EnumGuiMode mode,
            final Rotation rotation, final PathEntryType<Byte> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_ZS2_ENTRY.ordinal());
        buffer.putByte(value);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
        node.addAndSetEntry(new ModeSet(mode, rotation), entry, value);
        container.grid.putNode(node.getPoint(), node);
    }

    private void removeEntryFromServer(final SignalBoxNode node, final EnumGuiMode mode,
            final Rotation rotation, final PathEntryType<?> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.REMOVE_ENTRY.ordinal());
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
        node.getOption(new ModeSet(mode, rotation)).get().removeEntry(entry);
    }

    private void resetAllPathways() {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.RESET_ALL_PW.ordinal());
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
        resetColors(container.grid.getNodes());
    }

    private void sendModeChanges() {
        if (changedModes.isEmpty() || !allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_CHANGED_MODES.ordinal());
        buffer.putInt(changedModes.size());
        changedModes.forEach((point, node) -> {
            point.writeNetwork(buffer);
            node.writeNetwork(buffer);
        });
        container.grid.putAllNodes(changedModes);
        changedModes.clear();
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void removeBlockPos(final BlockPos pos) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.REMOVE_POS.ordinal());
        buffer.putBlockPos(pos);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void sendSubsidiaryRequest(final SubsidiaryEntry entry, final SignalBoxNode node,
            final ModeSet mode) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.REQUEST_SUBSIDIARY.ordinal());
        entry.writeNetwork(buffer);
        node.getPoint().writeNetwork(buffer);
        mode.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void changeRedstoneOutput(final Point point, final ModeSet mode, final boolean state) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.UPDATE_RS_OUTPUT.ordinal());
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
        buffer.putByte((byte) (state ? 1 : 0));
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void setAutoPoint(final Point point, final byte state) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SET_AUTO_POINT.ordinal());
        point.writeNetwork(buffer);
        buffer.putByte(state);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void sendName(final Point point, final String name) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_NAME.ordinal());
        point.writeNetwork(buffer);
        final byte[] array = name.getBytes();
        buffer.putByte((byte) array.length);
        for (final byte b : array)
            buffer.putByte(b);
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void reset() {
        this.page = Page.NONE;
        lowerEntity.clear();
    }

    private static enum Page {
        USAGE, EDIT, SETTINGS, TILE_CONFIG, CHANGE_MODE, NONE;
    }

    @Override
    public void updateFromContainer() {
        if (!allPacketsRecived) {
            initializeBasicUI();
            allPacketsRecived = true;
        }
    }

    private void buildColors(final List<SignalBoxNode> nodes) {
        nodes.forEach(node -> {
            final UISignalBoxTile tile = allTiles.get(node.getPoint());
            node.forEach(mode -> tile.setColor(mode,
                    node.getOption(mode).get().getEntry(PathEntryType.PATHUSAGE)
                            .orElseGet(() -> EnumPathUsage.FREE).getColor()));
        });
    }

    private void resetColors(final List<SignalBoxNode> nodes) {
        nodes.forEach(node -> {
            final UISignalBoxTile tile = allTiles.get(node.getPoint());
            node.forEach(mode -> {
                tile.setColor(mode, SignalBoxUtil.FREE_COLOR);
                final PathOptionEntry entry = node.getOption(mode).get();
                entry.getEntry(PathEntryType.PATHUSAGE).ifPresent(
                        _u -> entry.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.FREE));
            });
        });
    }

    private void applyColorChanges(final List<SignalBoxNode> listOfNodes) {
        for (int i = listOfNodes.size() - 2; i > 0; i--) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final Path path = new Path(oldPos, newPos);
            final SignalBoxNode current = listOfNodes.get(i);
            final UISignalBoxTile uiTile = allTiles.get(current.getPoint());
            final ModeSet modeSet = current.getMode(path);
            current.getOption(modeSet)
                    .ifPresent(poe -> uiTile.setColor(modeSet, poe.getEntry(PathEntryType.PATHUSAGE)
                            .orElseGet(() -> EnumPathUsage.FREE).getColor()));
        }
    }
}