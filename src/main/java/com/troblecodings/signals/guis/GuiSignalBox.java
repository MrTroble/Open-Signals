package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
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
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.ShowTypes;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.signalbox.MainSignalIdentifier;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Path;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxUtil;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class GuiSignalBox extends GuiBase {

    public static final int SELECTION_COLOR = 0x2900FF00;
    public static final int BACKGROUND_COLOR = ConfigHandler.CLIENT.signalboxBackgroundColor.get();
    public static final int GRID_COLOR = 0xFF5B5B5B;
    public static final int EDIT_COLOR = 0x5000A2FF;
    public static final int OUTPUT_COLOR = 0xffff00;

    public static final ResourceLocation COUNTER_TEXTURE = new ResourceLocation(
            OpenSignalsMain.MODID, "gui/textures/counter.png");
    public static final ResourceLocation REDSTONE_OFF = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/redstone_off.png");
    public static final ResourceLocation REDSTONE_OFF_BLOCKED = new ResourceLocation(
            OpenSignalsMain.MODID, "gui/textures/redstone_off_blocked.png");
    public static final ResourceLocation REDSTONE_ON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/redstone_on.png");
    public static final ResourceLocation REDSTONE_ON_BLOCKED = new ResourceLocation(
            OpenSignalsMain.MODID, "gui/textures/redstone_on_blocked.png");

    private static final float[] ALL_LINES = getLines();
    protected static final int TILE_WIDTH = 10;
    protected static final int TILE_COUNT = 100;

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
    protected final ContainerSignalBox container;
    private UISignalBoxTile lastTile = null;
    private UIEntity mainButton;
    private final GuiInfo info;
    private final Map<Point, SignalBoxNode> changedModes = new HashMap<>();
    private UIEntity splitter = new UIEntity();
    private boolean allPacketsRecived = false;
    protected final Map<Point, UISignalBoxTile> allTiles = new HashMap<>();
    private SidePanel helpPage;
    private final Map<BlockPos, SubsidiaryHolder> enabledSubsidiaries = new HashMap<>();
    private final Map<Point, UIColor> colors = new HashMap<>();
    private UILabel counterLabel = new UILabel("");
    private UIEntity infoLine = new UIEntity();

    public GuiSignalBox(final GuiInfo info) {
        super(info);
        this.container = (ContainerSignalBox) info.base;
        container.setInfoConsumer(this::infoUpdate);
        container.setColorUpdater(this::applyColorChanges);
        container.setSignalUpdater(this::updateSignals);
        container.setConuterUpdater(this::updateCounter);
        container.setTrainNumberUpdater(this::updateTrainNumber);
        this.info = info;
    }

    public void infoUpdate(final String errorString) {
        this.resetTileSelection();
        final UIToolTip tooltip = new UIToolTip(errorString, true);
        lowerEntity.add(tooltip);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            lowerEntity.remove(tooltip);
        }).start();
        return;
    }

    public void updateSignals(final Iterable<Point> updated) {
        updated.forEach(point -> {
            final UISignalBoxTile tile = allTiles.get(point);
            tile.setGreenSignals(container.greenSignals.getOrDefault(point, new ArrayList<>()));
            tile.getNode().forEach(mode -> {
                if (mode.mode.equals(EnumGuiMode.HP) || mode.mode.equals(EnumGuiMode.VP)
                        || mode.mode.equals(EnumGuiMode.RS)) {
                    tile.updateModeSet(mode);
                }
            });
        });
    }

    private void updateTrainNumber(final List<Point> points) {
        points.forEach(point -> {
            final UISignalBoxTile tile = allTiles.get(point);
            tile.updateTrainNumber();
        });
        lowerEntity.update();
    }

    protected void resetTileSelection() {
        colors.values().forEach(color -> color.getParent().remove(color));
        colors.clear();
        this.lastTile = null;
    }

    protected void selectLink(final UIEntity parent, final SignalBoxNode node,
            final PathOptionEntry option, final Set<Entry<BlockPos, LinkType>> entrySet,
            final LinkType type, final PathEntryType<BlockPos> entryType, final EnumGuiMode mode,
            final Rotation rotation) {
        this.selectLink(parent, node, option, entrySet, type, entryType, mode, rotation, "");
    }

    protected void selectLink(final UIEntity parent, final SignalBoxNode node,
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

    public static String getSignalInfo(final BlockPos signalPos, final LinkType type) {
        final Minecraft mc = Minecraft.getInstance();
        final String customName = ClientNameHandler
                .getClientName(new StateInfo(mc.level, signalPos));
        return String.format("%s (x=%d, y=%d. z=%d)", customName == null
                ? (type.equals(LinkType.SIGNAL) ? "" : I18Wrapper.format("type." + type.name()))
                : customName, signalPos.getX(), signalPos.getY(), signalPos.getZ());
    }

    private void disableSubsidiary(final BlockPos pos, final SubsidiaryHolder holder) {
        final SubsidiaryEntry entry = new SubsidiaryEntry(holder.entry.enumValue, false);
        sendSubsidiaryRequest(entry, holder.point, holder.modeSet);
        container.grid.setClientState(holder.point, holder.modeSet, entry);
        enabledSubsidiaries.remove(pos);
        helpPage.helpUsageMode(enabledSubsidiaries, null, container.possibleSubsidiaries);
        this.resetTileSelection();

        final MainSignalIdentifier identifier = new MainSignalIdentifier(
                new ModeIdentifier(holder.point, holder.modeSet), pos,
                SignalState.combine(entry.enumValue.getSubsidiaryShowType()));
        final List<MainSignalIdentifier> greenSignals = container.greenSignals
                .computeIfAbsent(identifier.getPoint(), _u -> new ArrayList<>());
        greenSignals.remove(identifier);
        updateSignals(ImmutableList.of(holder.point));
    }

    private void tileEdit(final UIEntity tile, final UIMenu menu, final UISignalBoxTile sbt) {
        tile.add(new UIClickable(e -> {
            if (!splitter.isHovered())
                return;
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
            if (lastTile == null) {
                if (currentTile.isValidStart()) {
                    this.lastTile = currentTile;
                    final UIColor previous = colors.get(currentTile.getPoint());
                    if (previous != null)
                        previous.getParent().remove(previous);

                    final UIColor newColor = new UIColor(SELECTION_COLOR);
                    c.add(newColor);
                    colors.put(currentTile.getPoint(), newColor);
                }
            } else {
                if (lastTile == currentTile) {
                    this.resetTileSelection();
                    return;
                }
                if (currentTile.isValidEnd()) {
                    sendPWRequest(currentTile.getNode());
                    this.resetTileSelection();
                    return;
                }
            }
        }));
        tile.add(new UIClickable(e -> openNodeShortcuts(currentTile.getNode(), e), 1));
    }

    private void resetSelection(final UIEntity entity) {
        final UIEntity parent = entity.getParent();
        parent.findRecursive(UIClickable.class).forEach(click -> click.setVisible(true));
        parent.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(true));
        entity.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(false));
        entity.findRecursive(UIClickable.class).forEach(click -> click.setVisible(false));
    }

    private void openNodeShortcuts(final SignalBoxNode node, final UIEntity entity) {
        if (node.isEmpty())
            return;
        final UIColor previous = colors.get(node.getPoint());
        if (previous != null && previous.getColor() == EDIT_COLOR) {
            helpPage.helpUsageMode(enabledSubsidiaries, null, container.possibleSubsidiaries);
            helpPage.setShowHelpPage(false);
            this.resetTileSelection();
            return;
        }
        this.resetTileSelection();

        final UIColor newColor = new UIColor(EDIT_COLOR);
        entity.add(newColor);
        colors.put(node.getPoint(), newColor);
        helpPage.helpUsageMode(enabledSubsidiaries, node, container.possibleSubsidiaries);
        helpPage.setShowHelpPage(true);
    }

    protected void initializePageTileConfig(final SignalBoxNode node) {
        if (node.isEmpty())
            return;
        disableInfoLine();
        final List<ModeDropDownBoxUI> dropDowns = new ArrayList<>();
        final Runnable update = () -> buildTileConfigList(node, dropDowns);
        node.forEach(modeSet -> dropDowns.add(
                new ModeDropDownBoxUI(modeSet, node.getOption(modeSet).get(), node, this, update)));
        buildTileConfigList(node, dropDowns);
    }

    private void buildTileConfigList(final SignalBoxNode node,
            final List<ModeDropDownBoxUI> dropDowns) {
        reset();
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        final UIBox box = new UIBox(UIBox.VBOX, 1);
        list.add(box);
        lowerEntity.add(new UIBox(UIBox.VBOX, 3));
        lowerEntity.add(list);

        final UIEntity nameEntity = new UIEntity();
        nameEntity.setInheritWidth(true);
        nameEntity.setHeight(20);
        nameEntity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity labelEntity = GuiElements.createLabel(I18Wrapper.format("info.node.text"),
                1.25f);
        labelEntity.setInheritWidth(false);
        labelEntity.setWidth(100);
        nameEntity.add(labelEntity);

        final UITextInput namingInput = new UITextInput(node.getCustomText());
        final UIEntity inputEntity = new UIEntity();
        inputEntity.setHeight(20);
        inputEntity.setInheritWidth(true);

        inputEntity.add(namingInput);
        nameEntity.add(inputEntity);
        list.add(nameEntity);
        list.add(GuiElements.createSpacerV(5));

        namingInput.setOnTextUpdate(str -> {
            node.setCustomText(str);
            sendName(node.getPoint(), str);
        });

        dropDowns.forEach(dropDown -> {
            list.add(dropDown.getTop());
            dropDown.addElements(list);
        });
        lowerEntity.add(GuiElements.createPageSelect(box));
        lowerEntity.add(new UIClickable(e -> {
            enableInfoLine();
            initializeFieldUsage(mainButton);
        }, 1));
    }

    private void initializePageSettings(final UIEntity entity) {
        this.initializePageSettings(entity, container.getPositionForTypes());
    }

    private int lastValue = 0;

    private void initializePageSettings(final UIEntity entity,
            final Map<BlockPos, LinkType> types) {
        reset();
        lowerEntity.add(new UIBox(UIBox.VBOX, 2));
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        final IIntegerable<ShowTypes> sorting = new EnumIntegerable<>(ShowTypes.class);
        lowerEntity.add(GuiElements.createEnumElement(sorting, i -> {
            lastValue = i;
            if (i == -1)
                return;
            final ShowTypes option = ShowTypes.values()[i];
            switch (option) {
                case ALL: {
                    initializePageSettings(entity);
                    break;
                }
                case INPUT: {
                    initializePageSettings(entity,
                            container.getPositionForTypes().entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(LinkType.INPUT))
                                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                            Map.Entry::getValue)));
                    break;
                }
                case OUTPUT: {
                    initializePageSettings(entity,
                            container.getPositionForTypes().entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(LinkType.OUTPUT))
                                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                            Map.Entry::getValue)));
                    break;
                }
                case SIGNAL: {
                    initializePageSettings(entity,
                            container.getPositionForTypes().entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(LinkType.SIGNAL))
                                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                            Map.Entry::getValue)));
                    break;
                }
                case SIGNALBOX: {
                    initializePageSettings(entity,
                            container.getPositionForTypes().entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(LinkType.SIGNALBOX))
                                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                            Map.Entry::getValue)));
                    break;
                }
                default:
                    break;
            }
        }, lastValue));
        final UIEntity inputEntity = new UIEntity();
        inputEntity.setInheritWidth(true);
        inputEntity.setHeight(20);
        final UITextInput input = new UITextInput("");
        inputEntity.add(input);
        lowerEntity.add(inputEntity);
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        final UIBox uibox = new UIBox(UIBox.VBOX, 2);
        list.add(uibox);
        final Map<String, UIEntity> nameToUIEntity = new HashMap<>();
        types.forEach((p, t) -> {
            final String name = getSignalInfo(p, t);
            final UIEntity layout = new UIEntity();
            layout.setHeight(20);
            layout.setInheritWidth(true);
            layout.add(new UIBox(UIBox.HBOX, 2));

            final int id = t.ordinal();
            final UIEntity icon = new UIEntity();
            final UITexture texture = t.equals(LinkType.SIGNALBOX)
                    ? new UITexture(new ResourceLocation(OpenSignalsMain.MODID,
                            "textures/blocks/signalbox.png"))
                    : new UITexture(UISignalBoxTile.ICON, 0.2 * id, 0.5, 0.2 * id + 0.2, 1);
            icon.add(texture);
            icon.setHeight(20);
            icon.setWidth(20);
            icon.add(new UIToolTip(I18Wrapper.format("type." + t.name())));
            layout.add(icon);

            layout.add(GuiElements.createButton(name));
            layout.add(GuiElements.createButton("x", 20, e -> {
                removeBlockPos(p);
                list.remove(layout);
            }));
            list.add(layout);
            nameToUIEntity.put(name.toLowerCase(), layout);
        });
        lowerEntity.add(list);
        lowerEntity.add(GuiElements.createPageSelect(uibox));
        resetSelection(entity);
        input.setOnTextUpdate(string -> {
            nameToUIEntity.forEach((name, e) -> {
                if (!name.contains(string.toLowerCase())) {
                    list.remove(e);
                } else {
                    list.add(e);
                }
            });
        });
    }

    private void initializeFieldUsage(final UIEntity entity) {
        reset();
        sendModeChanges();
        initializeFieldTemplate(this::tileNormal, false);
        resetSelection(entity);
        helpPage.helpUsageMode(enabledSubsidiaries, null, container.possibleSubsidiaries);
    }

    private void initializeFieldEdit(final UIEntity entity) {
        final UIEntity screen = GuiElements.createScreen(selectionEntity -> {
            final UIBox hbox = new UIBox(UIBox.VBOX, 3);
            selectionEntity.add(hbox);
            final UIEntity question = new UIEntity();
            final UILabel label = new UILabel(I18Wrapper.format("sb.editmode"));
            label.setTextColor(0xFFFFFFFF);
            question.setScaleX(1.1f);
            question.setScaleY(1.1f);
            question.add(label);
            question.setInherits(true);
            final UILabel info = new UILabel(I18Wrapper.format("sb.allreset"));
            info.setTextColor(0xFFFFFFFF);
            final UIEntity infoEntity = new UIEntity();
            infoEntity.add(info);
            infoEntity.setInherits(true);
            selectionEntity.add(question);
            selectionEntity.add(infoEntity);
            final UIEntity buttons = new UIEntity();
            final UIEntity buttonYes = GuiElements.createButton(I18Wrapper.format("btn.yes"), e -> {
                pop();
                reset();
                final UIMenu menu = new UIMenu();
                menu.setVisible(false);
                initializeFieldTemplate(
                        (fieldEntity, name) -> this.tileEdit(fieldEntity, menu, name), true);
                lowerEntity.add(menu);
                menu.setConsumer(
                        (selection, rotation) -> helpPage.updateNextNode(selection, rotation));
                resetSelection(entity);
                resetAllPathways();
                helpPage.updateNextNode(menu.getSelection(), menu.getRotation());
                this.lastTile = null;
            });
            final UIEntity buttonNo = GuiElements.createButton(I18Wrapper.format("btn.no"), e -> {
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
    }

    private void initializeFieldTemplate(final BiConsumer<UIEntity, UISignalBoxTile> consumer,
            final boolean showLines) {
        splitter.clear();
        final UIEntity plane = new UIEntity();
        plane.setWidth(TILE_COUNT * TILE_WIDTH);
        plane.setHeight(TILE_COUNT * TILE_WIDTH);
        splitter.add(new UIScroll(s -> {
            final float newScale = (float) (plane.getScaleX() + s * 0.05f);
            if (newScale <= 0)
                return;
            plane.setScaleX(newScale);
            plane.setScaleY(newScale);
            plane.update();
        }));
        splitter.add(new UIDrag((x, y) -> {
            plane.setX(plane.getX() + x);
            plane.setY(plane.getY() + y);
            plane.update();
        }, 2));
        if (showLines) {
            final UILines allLines = new UILines(ALL_LINES, 0.5F);
            allLines.setColor(GRID_COLOR);
            plane.add(allLines);
        }
        plane.add(new UIBox(UIBox.VBOX, 0).setPageable(false));
        allTiles.clear();
        for (int x = 0; x < TILE_COUNT; x++) {
            final UIEntity row = new UIEntity();
            row.add(new UIBox(UIBox.HBOX, 0).setPageable(false));
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
                sbt.setGreenSignals(container.greenSignals.getOrDefault(name, new ArrayList<>()));
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
                sbt.updateTrainNumber();
            }
            plane.add(row);
        }
        splitter.add(new UIColor(BACKGROUND_COLOR));
        splitter.add(new UIScissor());
        splitter.add(new UIBorder(0xFF000000, 4));
        splitter.add(plane);
        splitter.setInherits(true);
        lowerEntity.add(new UIBox(UIBox.HBOX, 2));
        lowerEntity.add(splitter);
        helpPage = new SidePanel(lowerEntity, this);
        helpPage.setDisableSubdsidiary(this::disableSubsidiary);

        buildColors(container.grid.getNodes());
    }

    public void updateCounter() {
        counterLabel.setText(String.format("%04d", container.grid.getCurrentCounter()));
    }

    private void initializeBasicUI() {
        final String name = I18Wrapper.format("tile.signalbox.name");

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
        header.add(GuiElements.createSpacerH(20));
        header.add(GuiElements.createButton(I18Wrapper.format("btn.settings"),
                this::initializePageSettings));
        header.add(
                GuiElements.createButton(I18Wrapper.format("btn.edit"), this::initializeFieldEdit));
        mainButton = GuiElements.createButton(I18Wrapper.format("btn.main"),
                this::initializeFieldUsage);
        header.add(mainButton);
        resetSelection(mainButton);

        infoLine.setInheritWidth(true);
        infoLine.setHeight(20);
        infoLine.add(new UIBox(UIBox.HBOX, 3));
        infoLine.add(new UIColor(BACKGROUND_COLOR));
        infoLine.add(GuiElements.createSpacerH(10));
        addInfoLine(infoLine);

        final UIEntity middlePart = new UIEntity();
        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(infoLine);
        middlePart.add(lowerEntity);

        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        initializeFieldTemplate(this::tileNormal, false);

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));
        helpPage.helpUsageMode(enabledSubsidiaries, null, container.possibleSubsidiaries);
    }

    private void addInfoLine(final UIEntity entity) {
        final UIEntity shButton = GuiElements.createButton(I18Wrapper.format("info.usage.sh"),
                e -> resetAllSignals());
        shButton.add(new UIToolTip(I18Wrapper.format("info.usage.sh.desc")));
        entity.add(shButton);

        final UIEntity counterButton = new UIEntity();
        counterButton.setHeight(20);
        counterButton.setInheritWidth(true);

        counterButton.add(new UITexture(COUNTER_TEXTURE));
        final UIEntity labelEntity = GuiElements
                .createLabel(String.format("%04d", container.grid.getCurrentCounter()), 0xFFFFFFFF);
        labelEntity.setX(46);
        labelEntity.setY(4.5f);
        labelEntity.setScale(1.5f);
        counterButton.add(labelEntity);

        labelEntity.findRecursive(UILabel.class).forEach(label -> counterLabel = label);
        counterButton.add(new UIToolTip(I18Wrapper.format("btn.counter.tooltip")));

        entity.add(counterButton);

        final List<SignalBoxNode> allNodes = container.grid.getNodes();
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
                                            + ClientNameHandler.getClientName(new StateInfo(
                                                    mc.level,
                                                    entry.getEntry(PathEntryType.OUTPUT).get()));
                                    final UIEntity button = GuiElements.createButton(name, e1 -> {
                                        pop();
                                        final UIEntity info = new UIEntity();
                                        info.setInherits(true);
                                        info.add(new UIBox(UIBox.VBOX, 5));
                                        info.add(new UIClickable(_u -> pop(), 1));
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
                                            push(GuiElements.createScreen(
                                                    screenEntity -> screenEntity.add(info)));
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
                                                        changeRedstoneOutput(currentNode.getPoint(),
                                                                mode, false);
                                                        outputStatus.setText(I18Wrapper
                                                                .format("info.usage.rs.false"));
                                                        textureEntity
                                                                .add(new UITexture(REDSTONE_OFF));
                                                    } else {
                                                        changeRedstoneOutput(currentNode.getPoint(),
                                                                mode, true);
                                                        outputStatus.setText(I18Wrapper
                                                                .format("info.usage.rs.true"));
                                                        textureEntity
                                                                .add(new UITexture(REDSTONE_ON));
                                                    }
                                                }));
                                        push(GuiElements.createScreen(
                                                screenEntity -> screenEntity.add(info)));
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
                                nameToUIEntity.forEach((name, nameEntity) -> {
                                    if (!name.contains(string.toLowerCase())) {
                                        list.remove(nameEntity);
                                    } else {
                                        list.add(nameEntity);
                                    }
                                });
                            });
                        });
                        screen.add(new UIClickable(e1 -> pop(), 1));
                        push(screen);
                    });
            entity.add(manuelButton);
            manuelButton.add(new UIToolTip(I18Wrapper.format("info.usage.manuel.desc")));
        }

        final UIEntity savedPathways = GuiElements
                .createButton(I18Wrapper.format("info.usage.savedpathways"), e -> {
                    final UIEntity screen = GuiElements.createScreen(screenEntity -> {
                        final UIEntity listWithScroll = new UIEntity();
                        listWithScroll.setInherits(true);
                        listWithScroll.add(new UIBox(UIBox.HBOX, 2));
                        listWithScroll.add(new UIScissor());
                        listWithScroll.add(new UIBorder(0xFF00FFFF));
                        screenEntity.add(listWithScroll);

                        final UIEntity list = new UIEntity();
                        listWithScroll.add(list);
                        list.setInherits(true);

                        final UIScrollBox scrollbox = new UIScrollBox(UIBox.VBOX, 2);
                        list.add(scrollbox);

                        container.nextPathways.forEach(entry -> {
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
                                        resetTileSelection();
                                        pop();
                                        addColorToTile(entry.getKey(), entry.getValue(),
                                                SELECTION_COLOR);
                                    }));
                            layout.add(GuiElements.createButton("x", 20, _u -> {
                                container.nextPathways.remove(entry);
                                list.remove(layout);
                                removeNextPathwayFromServer(entry.getKey(), entry.getValue());
                                pop();
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
                    screen.add(new UIClickable(_u -> pop(), 1));
                    push(screen);
                });
        entity.add(savedPathways);
    }

    private void disableInfoLine() {
        infoLine.clearChildren();
        infoLine.setHeight(0);
        infoLine.getParent().update();
    }

    private void enableInfoLine() {
        addInfoLine(infoLine);
        infoLine.setHeight(20);
        infoLine.getParent().update();
    }

    private void sendPWRequest(final SignalBoxNode currentNode) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REQUEST_PW);
        lastTile.getPoint().writeNetwork(buffer);
        currentNode.getPoint().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void resetPathwayOnServer(final SignalBoxNode node) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.RESET_PW);
        node.getPoint().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void sendPosEntryToServer(final BlockPos pos, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<BlockPos> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_POS_ENTRY);
        buffer.putBlockPos(pos);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendIntEntryToServer(final int speed, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<Integer> entry) {
        if (speed == 127 || !allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_INT_ENTRY);
        buffer.putByte((byte) speed);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendZS2Entry(final byte value, final SignalBoxNode node, final EnumGuiMode mode,
            final Rotation rotation, final PathEntryType<Byte> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_ZS2_ENTRY);
        buffer.putByte(value);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendPointEntry(final Point point, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<Point> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_POINT_ENTRY);
        point.writeNetwork(buffer);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void removeEntryFromServer(final SignalBoxNode node, final EnumGuiMode mode,
            final Rotation rotation, final PathEntryType<?> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REMOVE_ENTRY);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void resetAllPathways() {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.RESET_ALL_PW);
        OpenSignalsMain.network.sendTo(info.player, buffer);
        resetColors(container.grid.getNodes());
    }

    private void sendModeChanges() {
        if (changedModes.isEmpty() || !allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_CHANGED_MODES);
        buffer.putInt(changedModes.size());
        changedModes.forEach((point, node) -> {
            point.writeNetwork(buffer);
            node.writeNetwork(buffer);
        });
        container.grid.putAllNodes(changedModes);
        changedModes.clear();
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void removeBlockPos(final BlockPos pos) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REMOVE_POS);
        buffer.putBlockPos(pos);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendSubsidiaryRequest(final SubsidiaryEntry entry, final Point point,
            final ModeSet mode) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REQUEST_SUBSIDIARY);
        entry.writeNetwork(buffer);
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void changeRedstoneOutput(final Point point, final ModeSet mode,
            final boolean state) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.UPDATE_RS_OUTPUT);
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
        buffer.putBoolean(state);
        OpenSignalsMain.network.sendTo(info.player, buffer);
        final UISignalBoxTile tile = allTiles.get(point);
        tile.setColor(mode, state ? OUTPUT_COLOR : SignalBoxUtil.FREE_COLOR);
    }

    protected void setAutoPoint(final Point point, final byte state) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SET_AUTO_POINT);
        point.writeNetwork(buffer);
        buffer.putBoolean(state == 1);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void sendName(final Point point, final String name) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_NAME);
        point.writeNetwork(buffer);
        buffer.putString(name);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendBoolEntry(final boolean state, final Point point, final ModeSet mode,
            final PathEntryType<Boolean> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_BOOL_ENTRY);
        buffer.putBoolean(state);
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void removeNextPathwayFromServer(final Point start, final Point end) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REMOVE_SAVEDPW);
        start.writeNetwork(buffer);
        end.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendCurrentCounterToServer() {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_COUNTER);
        buffer.putInt(container.grid.getCurrentCounter());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendTrainNumber(final Point point, final String number) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_TRAIN_NUMBER);
        point.writeNetwork(buffer);
        buffer.putString(number);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void deleteTrainNumber(final Point point) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_TRAIN_NUMBER);
        point.writeNetwork(buffer);
        buffer.putString(TrainNumber.DEFAULT.trainNumber);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void resetAllSignals() {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.RESET_ALL_SIGNALS);
        OpenSignalsMain.network.sendTo(info.player, buffer);
        final Set<Point> set = ImmutableSet.copyOf(container.greenSignals.keySet());
        container.greenSignals.clear();
        updateSignals(set);
    }

    protected void sendPosIdentList(final List<PosIdentifier> list, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation,
            final PathEntryType<List<PosIdentifier>> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_POSIDENT_LIST);
        buffer.putInt(list.size());
        for (final PosIdentifier posIdent : list) {
            posIdent.writeNetwork(buffer);
        }
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void reset() {
        lowerEntity.clear();
    }

    private void addColorToTile(final Point start, final Point end, final int color) {
        final UISignalBoxTile startTile = allTiles.get(start);
        final UISignalBoxTile endTile = allTiles.get(end);
        if (startTile == null || endTile == null)
            return;
        final UIColor uiColor = new UIColor(color);
        new Thread(() -> {
            startTile.getParent().add(uiColor);
            endTile.getParent().add(uiColor);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            startTile.getParent().remove(uiColor);
            endTile.getParent().remove(uiColor);
        }, "GuiSignalBox:showNextPathway").start();
    }

    @Override
    public void updateFromContainer() {
        if (!allPacketsRecived) {
            updateEnabledSubsidiaries();
            initializeBasicUI();
            allPacketsRecived = true;
        }
    }

    private void updateEnabledSubsidiaries() {
        container.enabledSubsidiaryTypes.forEach((point, map) -> map.forEach((modeSet, state) -> {
            if (!state.state)
                return;
            final SignalBoxNode node = container.grid.getNode(point);
            if (node == null)
                return;
            node.getOption(modeSet).get().getEntry(PathEntryType.SIGNAL)
                    .ifPresent(pos -> enabledSubsidiaries.put(pos,
                            new SubsidiaryHolder(state, point, modeSet)));
        }));
    }

    private void buildColors(final List<SignalBoxNode> nodes) {
        nodes.forEach(node -> {
            final UISignalBoxTile tile = allTiles.get(node.getPoint());
            node.forEach(mode -> {
                if (node.containsManuellOutput(mode)) {
                    tile.setColor(mode, OUTPUT_COLOR);
                    return;
                }
                final int color = node.getOption(mode).get().getEntry(PathEntryType.PATHUSAGE)
                        .orElseGet(() -> EnumPathUsage.FREE).getColor();
                tile.setColor(mode, color);
            });
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
            if (uiTile == null)
                continue;
            final ModeSet modeSet = current.getMode(path);
            current.getOption(modeSet)
                    .ifPresent(poe -> uiTile.setColor(modeSet, poe.getEntry(PathEntryType.PATHUSAGE)
                            .orElseGet(() -> EnumPathUsage.FREE).getColor()));
        }
    }
}