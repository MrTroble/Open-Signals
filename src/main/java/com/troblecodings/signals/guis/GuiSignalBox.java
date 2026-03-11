package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.TCBoolean;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryHolder;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.enums.ShowTypes;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.enums.SignalBoxPage;
import com.troblecodings.signals.guis.UISignalBoxProfile.UIBorderSettings;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;
import com.troblecodings.signals.guis.UISignalBoxRendering.SignalBoxConsumer;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Path;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class GuiSignalBox extends GuiBase {

    public static final ResourceLocation REDSTONE_OFF =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/redstone_off.png");
    public static final ResourceLocation REDSTONE_OFF_BLOCKED =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/redstone_off_blocked.png");
    public static final ResourceLocation REDSTONE_ON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/redstone_on.png");
    public static final ResourceLocation REDSTONE_ON_BLOCKED =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/redstone_on_blocked.png");

    private final UIEntity lowerEntity = new UIEntity();
    private final UIEntity bottomEntity = new UIEntity();
    protected final ContainerSignalBox container;
    private SignalBoxPage page = SignalBoxPage.OPERATION;
    private SignalBoxNode lastTile = null;
    private UIEntity mainButton;
    private final GuiInfo info;
    private final Map<Point, SignalBoxNode> changedModes = new HashMap<>();
    private UIEntity splitter = new UIEntity();
    private boolean allPacketsRecived = false;
    private SidePanel helpPage;
    protected UISignalBoxProfile profile = UISignalBoxProfile.DEFAULT;
    protected UISignalBoxRendering rendering;
    protected final Map<BlockPos, SubsidiaryHolder> enabledSubsidiaries = new HashMap<>();
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public GuiSignalBox(final GuiInfo info) {
        super(info);
        this.container = (ContainerSignalBox) info.base;
        container.setInfoConsumer(this::infoUpdate);
        container.setColorUpdater(this::applyColorChanges);
        container.setConuterUpdater(this::updateCounter);
        container.setTrainNumberUpdater(this::updateTrainNumbers);
        container.updateSignalState = this::updateSignalState;
        this.info = info;
    }

    public SignalBoxPage getPage() {
        return page;
    }

    private void updateSignalState(final SignalBoxNode node) {
        node.forEach((mode) -> {
            checkForSubsidiary(node, mode);
            rendering.updateSignalState(node.getPoint(), mode, node.getState(mode));
        });
    }

    private void checkForSubsidiary(final SignalBoxNode node, final ModeSet mode) {
        final Map<ModeSet, SubsidiaryState> subsidiary =
                container.enabledSubsidiaryTypes.getOrDefault(node.getPoint(), new HashMap<>());
        final SubsidiaryState state = subsidiary.get(mode);
        if (state != null) {
            node.updateState(mode, SignalState.combine(state.getSubsidiaryShowType()));
        }
    }

    public void infoUpdate(final String errorString) {
        final UIToolTip tooltip = new UIToolTip(errorString, true);
        lowerEntity.add(tooltip);
        executor.schedule(() -> lowerEntity.remove(tooltip), 3, TimeUnit.SECONDS);
        return;
    }

    private void updateTrainNumbers(final List<SignalBoxNode> nodes) {
        nodes.forEach(node -> {
            node.iterator().forEachRemaining(modeSet -> {
                if (!(modeSet.mode == EnumGuiMode.TRAIN_NUMBER))
                    return;
                node.getOption(modeSet).ifPresent(option -> {
                    final TrainNumber number =
                            option.getEntry(PathEntryType.TRAINNUMBER).orElse(TrainNumber.DEFAULT);
                    final ModeIdentifier modeIdent = new ModeIdentifier(node.getPoint(), modeSet);
                    if (number.trainNumber.isEmpty()) {
                        rendering.removeTrainNumber(modeIdent);
                    } else {
                        rendering.putTrainNumber(modeIdent, number.trainNumber);
                    }
                });
            });
        });
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
                .getClientName(new StateInfo(mc.level, signalPos)).replace("[n]", " ");
        return String.format("%s (x=%d, y=%d. z=%d)", customName == null
                ? (type.equals(LinkType.SIGNAL) ? "" : I18Wrapper.format("type." + type.name()))
                : customName, signalPos.getX(), signalPos.getY(), signalPos.getZ());
    }

    protected void disableSubsidiary(final BlockPos pos, final SubsidiaryHolder holder) {
        final SubsidiaryState state = holder.entry;
        sendSubsidiaryRequest(state, holder.point, holder.modeSet, false);
        container.updateClientSubsidiary(holder.point, holder.modeSet, state, false);
        enabledSubsidiaries.remove(pos);
        helpPage.helpUsageMode(null);

        container.grid.getNodeChecked(holder.point).ifPresent(node -> {
            node.removeSubsidiaryState(holder.modeSet);
            node.updateState(holder.modeSet, SignalState.RED);
            rendering.updateSignalState(holder.point, holder.modeSet, SignalState.RED);
        });
    }

    private void updateTileWithMode(final UIMenu menu, final UISignalBoxRendering rendering,
            final Point point, final int mouse) {
        if ((mouse != MouseEvent.LEFT_MOUSE) || !splitter.isHovered())
            return;
        final EnumGuiMode mode = EnumGuiMode.values()[menu.getSelection()];
        final Rotation rotation = Rotation.values()[menu.getRotation()];

        final ModeSet modeSet = new ModeSet(mode, rotation);

        container.grid.updateMode(point, modeSet);
        if (rendering.has(point, modeSet)) {
            rendering.removeMode(point, modeSet);
        } else {
            rendering.addMode(point, modeSet);
        }
        this.changedModes.put(point, container.grid.getNode(point));
    }

    private void tileNormal(final UISignalBoxRendering rendering, final Point tile,
            final int mouse) {
        if (mouse == MouseEvent.RIGHT_MOUSE) {
            this.container.grid.getNodeChecked(tile).ifPresentOrElse(this::openNodeShortcuts,
                    () -> {
                        this.helpPage.setShowHelpPage(false);
                        rendering.clearSelection();
                        this.lastTile = null;
                    });
            return;
        }
        this.helpPage.setShowHelpPage(false);
        rendering.clearSelection();
        if (mouse != MouseEvent.LEFT_MOUSE)
            return;
        this.container.grid.getNodeChecked(tile).ifPresent(node -> {
            if (lastTile == null) {
                if (node.isValidStart()) {
                    this.lastTile = node;
                    this.rendering.addSelection(
                            profile.getOperationModeSettings().getUserSelectionColor(), tile,
                            SelectionType.FIRST);
                }
            } else {
                if (lastTile == node) {
                    rendering.clearSelection();
                } else if (node.isValidEnd()) {
                    this.rendering.addSelection(
                            profile.getOperationModeSettings().getUserSelectionColor(), tile,
                            SelectionType.SECOND);
                    this.executor.schedule(rendering::clearSelection, 500, TimeUnit.MICROSECONDS);
                    checkForMultiplePathTypes(lastTile, node);
                }
                this.lastTile = null;
            }
        });
    }

    private void checkForMultiplePathTypes(final SignalBoxNode start, final SignalBoxNode end) {
        final List<PathType> possibleTypes = start.getPossibleTypes(end);
        if (possibleTypes.isEmpty()) {
            infoUpdate(
                    I18Wrapper.format("error." + PathwayRequestMode.NO_EQUAL_PATH_TYPE.getName()));
        } else if (possibleTypes.size() == 1) {
            sendPWRequest(lastTile.getPoint(), end.getPoint(), possibleTypes.get(0));
        } else if (possibleTypes.size() > 1) {
            push(GuiElements.createScreen(entity -> {
                entity.add(GuiElements.createButton(I18Wrapper.format("btn.return"), e -> pop()));
                entity.add(GuiElements.createSpacerV(10));
                entity.add(GuiElements.createLabel(I18Wrapper.format("gui.signalbox.choosetypes"),
                        0xffffff));
                entity.add(GuiElements.createSpacerV(10));
                possibleTypes
                        .forEach(type -> entity.add(GuiElements.createButton(type.name(), e -> {
                            sendPWRequest(start.getPoint(), end.getPoint(), type);
                            pop();
                        })));
            }));
        }
    }

    private void resetSelection(final UIEntity entity) {
        final UIEntity parent = entity.getParent();
        parent.findRecursive(UIClickable.class).forEach(click -> click.setVisible(true));
        parent.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(true));
        entity.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(false));
        entity.findRecursive(UIClickable.class).forEach(click -> click.setVisible(false));
    }

    private void openNodeShortcuts(final SignalBoxNode node) {
        if (node.isEmpty())
            return;
        final Point point = node.getPoint();
        final boolean alredySelected = rendering.hasSelection(
                profile.getOperationModeSettings().getEditColor(), point, SelectionType.FIRST);
        if (!alredySelected) {
            rendering.addSelection(profile.getOperationModeSettings().getEditColor(), point,
                    SelectionType.FIRST);
            helpPage.helpUsageMode(node);
            lastTile = null;
        } else {
            rendering.removeSelection(SelectionType.FIRST);
        }
        helpPage.setShowHelpPage(!alredySelected);
    }

    protected void initializePageTileConfig(final SignalBoxNode node) {
        if (node.isEmpty())
            return;
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

        final UIEntity labelEntity =
                GuiElements.createLabel(I18Wrapper.format("info.node.text"), 1.25f);
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
            rendering.updateNodeLabeling(node.getPoint(), str);
        });

        dropDowns.forEach(dropDown -> {
            list.add(dropDown.getTop());
            dropDown.addElements(list);
        });
        lowerEntity.add(GuiElements.createPageSelect(box));

        final UIEntity bottomRow = new UIEntity();
        bottomRow.setHeight(20);
        bottomRow.setInheritWidth(true);
        bottomRow.add(new UIBox(UIBox.HBOX, 0));
        lowerEntity.add(bottomRow);

        bottomRow.add(GuiElements.createSpacerV(20));
        final String text = I18Wrapper.format("gui.signalbox.return");
        final Font font = Minecraft.getInstance().font;
        bottomRow.add(GuiElements.createButton(text, font.width(text) + 8,
                e -> initializeFieldUsage(mainButton)));
        lowerEntity.add(new UIClickable(e -> initializeFieldUsage(mainButton), 1));
    }

    private void initializePageSettings(final UIEntity entity) {
        this.initializePageSettings(entity, container.getPositionForTypes());
    }

    private int lastValue = 0;

    private void initializePageSettings(final UIEntity entity,
            final Map<BlockPos, LinkType> types) {
        reset();
        disableBottomEntity();
        page = SignalBoxPage.LINKING;
        lowerEntity.add(new UIBox(UIBox.VBOX, 2));
        lowerEntity.setInherits(true);

        final IIntegerable<String> uiProfiles =
                SizeIntegerables.of("ui_profiles", UISignalBoxProfile.UI_PROFILES.size(),
                        i -> I18Wrapper.format("gui.signalbox.profile."
                                + UISignalBoxProfile.UI_PROFILES.get(i).getName()));
        lowerEntity.add(GuiElements.createEnumElement(uiProfiles, i -> updateUIProfile(i)));

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
                    initializePageSettings(entity, container.getPositionForTypes().entrySet()
                            .stream().filter(entry -> entry.getValue().equals(LinkType.INPUT))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    break;
                }
                case OUTPUT: {
                    initializePageSettings(entity, container.getPositionForTypes().entrySet()
                            .stream().filter(entry -> entry.getValue().equals(LinkType.OUTPUT))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    break;
                }
                case SIGNAL: {
                    initializePageSettings(entity, container.getPositionForTypes().entrySet()
                            .stream().filter(entry -> entry.getValue().equals(LinkType.SIGNAL))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    break;
                }
                case SIGNALBOX: {
                    initializePageSettings(entity, container.getPositionForTypes().entrySet()
                            .stream().filter(entry -> entry.getValue().equals(LinkType.SIGNALBOX))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
                    : new UITexture(UISignalBoxIcons.ICON, 0.2 * id, 0.5, 0.2 * id + 0.2, 1);
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
        page = SignalBoxPage.OPERATION;
        initializeFieldTemplate(this::tileNormal,
                profile.getOperationModeSettings().getUIBorderSettings());
        resetSelection(entity);
        helpPage.helpUsageMode(null);
        disableBottomEntity();
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
                page = SignalBoxPage.EDITOR;
                final UIMenu menu = new UIMenu();
                initializeFieldTemplate((rendering, point, mouse) -> this.updateTileWithMode(menu,
                        rendering, point, mouse),
                        profile.getEditorModeSettings().getUIBorderSettings());
                menu.setConsumer(
                        (selection, rotation) -> helpPage.updateNextNode(selection, rotation));
                resetSelection(entity);
                resetAllPathways();
                helpPage.updateNextNode(menu.getSelection(), menu.getRotation());
                this.lastTile = null;

                bottomEntity.setHeight(34);
                bottomEntity.add(new UIColor(0xFF8B8B8B));
                bottomEntity.add(new UIBorder(0xFF000000, 2));
                bottomEntity.add(menu);
                bottomEntity.getParent().update();
            });
            final UIEntity buttonNo =
                    GuiElements.createButton(I18Wrapper.format("btn.no"), e -> pop());
            buttons.setInherits(true);
            final UIBox vbox = new UIBox(UIBox.HBOX, 1);
            buttons.add(vbox);
            buttons.add(buttonYes);
            buttons.add(buttonNo);
            selectionEntity.add(buttons);
        });
        push(screen);
    }

    private void initializeFieldTemplate(final SignalBoxConsumer consumer,
            final UIBorderSettings settings) {
        final BoxEntity entitys = UISignalBoxRendering.createSignalBoxEntity(container.grid,
                profile, settings, consumer);
        splitter = entitys.entity;
        rendering = entitys.rendering;

        lowerEntity.add(new UIBox(UIBox.HBOX, 2));
        lowerEntity.add(splitter);
        helpPage = new SidePanel(lowerEntity, this);

        final List<SignalBoxNode> nodes = container.grid.getNodes();
        buildColors(nodes);
        updateTrainNumbers(nodes);
    }

    public void updateCounter() {
        helpPage.updateCounterButton();
    }

    private void initializeBasicUI() {
        final String name = I18Wrapper.format("block." + OpenSignalsMain.MODID + ".signalbox");

        final UILabel titlelabel = new UILabel(name);
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInherits(true);

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
        mainButton =
                GuiElements.createButton(I18Wrapper.format("btn.main"), this::initializeFieldUsage);
        header.add(mainButton);
        resetSelection(mainButton);

        bottomEntity.setHeight(0);

        final UIEntity middlePart = new UIEntity();
        middlePart.setInherits(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(lowerEntity);
        middlePart.add(bottomEntity);

        lowerEntity.setInherits(true);
        initializeFieldTemplate(this::tileNormal,
                profile.getOperationModeSettings().getUIBorderSettings());

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));
        helpPage.helpUsageMode(null);

        bottomEntity.setWidth(middlePart.getWidth() - 4);
    }

    private void disableBottomEntity() {
        bottomEntity.clear();
        bottomEntity.setHeight(0);
        bottomEntity.getParent().update();
    }

    private void updateUIProfile(final int profileID) {
        this.profile = UISignalBoxProfile.UI_PROFILES.get(profileID);
        // TODO Sent to Server
    }

    private void sendPWRequest(final Point start, final Point end, final PathType type) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REQUEST_PW);
        start.writeNetwork(buffer);
        end.writeNetwork(buffer);
        buffer.putEnumValue(type);
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

    protected void sendZS6Entry(final boolean value, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation, final PathEntryType<TCBoolean> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_ZS6_ENTRY);
        buffer.putBoolean(value);
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
        rendering.clearTrainNumbers();
    }

    private void sendModeChanges() {
        if (changedModes.isEmpty() || !allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_CHANGED_MODES);
        buffer.putINetworkSaveableMap(changedModes);
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

    protected void sendSubsidiaryRequest(final SubsidiaryState entry, final Point point,
            final ModeSet mode, final boolean enable) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.REQUEST_SUBSIDIARY);
        entry.writeNetwork(buffer);
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
        buffer.putBoolean(enable);
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
        rendering.setColor(point, mode, state ? profile.getOperationModeSettings().getOutputColor()
                : profile.getOperationModeSettings().getFreeColor());
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
        container.grid.resetAllSignals();
    }

    protected void sendPosIdentList(final List<PosIdentifier> list, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation,
            final PathEntryType<List<PosIdentifier>> entry) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_POSIDENT_LIST);
        buffer.putISaveableList(list);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) entry.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendConnetedTrainNumbers(final ModeIdentifier ident, final SignalBoxNode node,
            final EnumGuiMode mode, final Rotation rotation) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_CONNECTED_TRAINNUMBERS);
        ident.writeNetwork(buffer);
        node.getPoint().writeNetwork(buffer);
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
        buffer.putByte((byte) PathEntryType.CONNECTED_TRAINNUMBER.getID());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void updateSignalStateOnServer(final Point point, final ModeSet mode,
            final SignalState state) {
        if (!allPacketsRecived)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SET_SIGNAL_STATE);
        point.writeNetwork(buffer);
        buffer.putByte((byte) mode.mode.ordinal());
        buffer.putByte((byte) mode.rotation.ordinal());
        buffer.putEnumValue(state);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void reset() {
        lowerEntity.clear();
    }

    @Override
    public void updateFromContainer() {
        if (!allPacketsRecived) {
            loadSignalBoxUIProfile();
            updateEnabledSubsidiaries();
            initializeBasicUI();
            enabledSubsidiaries.values()
                    .forEach(holder -> updateSignalState(container.grid.getNode(holder.point)));
            allPacketsRecived = true;
        }
    }

    private void loadSignalBoxUIProfile() {
        final String profileString = container.signalBoxUIProfile;
        if (!(profileString == null || profileString.isEmpty())) {
            for (final UISignalBoxProfile profile : UISignalBoxProfile.UI_PROFILES) {
                if (profile.getName().equals(profileString)) {
                    this.profile = profile;
                    return;
                }
            }
        }
    }

    private void updateEnabledSubsidiaries() {
        container.enabledSubsidiaryTypes.forEach((point, map) -> map.forEach((modeSet, state) -> {
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
            this.rendering.setColor(node.getPoint(), mode -> {
                if (mode.mode == EnumGuiMode.TRAIN_NUMBER)
                    return profile.getOperationModeSettings().getTrainnumberBackgroundColor();
                if (node.containsManuellOutput(mode))
                    return profile.getOperationModeSettings().getOutputColor();
                return node.getOption(mode).get().getEntry(PathEntryType.PATHUSAGE)
                        .orElseGet(() -> EnumPathUsage.FREE)
                        .getColor(profile.getOperationModeSettings());
            });
        });
    }

    private void resetColors(final List<SignalBoxNode> nodes) {
        nodes.forEach(node -> {
            node.forEach(mode -> {
                final EnumGuiMode guiMode = mode.mode;
                final PathOptionEntry entry = node.getOption(mode).get();
                switch (guiMode) {
                    case STRAIGHT:
                    case CORNER:
                    case CROSSING:
                        rendering.setColor(node.getPoint(), mode,
                                profile.getOperationModeSettings().getFreeColor());
                        entry.getEntry(PathEntryType.PATHUSAGE).ifPresent(
                                _u -> entry.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.FREE));
                        break;
                    case TRAIN_NUMBER:
                        entry.getEntry(PathEntryType.TRAINNUMBER)
                                .ifPresent(_u -> entry.removeEntry(PathEntryType.TRAINNUMBER));
                        break;
                    default:
                        break;
                }
            });
        });
    }

    private void applyColorChanges(final List<SignalBoxNode> listOfNodes) {
        for (int i = listOfNodes.size() - 2; i > 0; i--) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final Path path = new Path(oldPos, newPos);
            final SignalBoxNode current = listOfNodes.get(i);
            final ModeSet modeSet = current.getMode(path);
            current.getOption(modeSet)
                    .ifPresent(poe -> rendering.setColor(current.getPoint(), modeSet,
                            poe.getEntry(PathEntryType.PATHUSAGE)
                                    .orElseGet(() -> EnumPathUsage.FREE)
                                    .getColor(profile.getOperationModeSettings())));
        }
    }

}