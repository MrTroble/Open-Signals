package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.TCBoolean;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UICheckBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.guis.UISignalBoxProfile.OperationModeSettings;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxUtil;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Rotation;

public class ModeDropDownBoxUI {

    private final OperationModeSettings settings;
    private ModeSet modeSet = null;
    private final Runnable update;
    private PathOptionEntry option = null;
    private GuiSignalBox gui = null;
    private SignalBoxNode node = null;
    private boolean open = false;

    public ModeDropDownBoxUI(final ModeSet modeSet, final PathOptionEntry option,
            final SignalBoxNode node, final GuiSignalBox gui, final Runnable update) {
        this.modeSet = modeSet;
        this.update = update;
        this.option = option;
        this.gui = gui;
        this.node = node;
        this.settings = gui.profile.getOperationModeSettings();
    }

    public UIEntity getTop() {
        final String modeName = I18Wrapper.format("property." + modeSet.mode.name());
        final String rotationName =
                I18Wrapper.format("property." + modeSet.rotation.name() + ".rotation");

        final UIEntity top = new UIEntity();
        top.setInheritWidth(true);
        top.setHeight(20);
        top.add(new UIBox(UIBox.HBOX, 5));
        top.add(new UIColor(0xFF8B8B8B));

        if (open) {
            top.add(GuiElements.createButton("-", 20, e -> changeShowState()));
        } else {
            top.add(GuiElements.createButton("+", 20, e -> changeShowState()));
        }

        final UIEntity modeInfo = new UIEntity();
        modeInfo.setHeight(20);
        modeInfo.setInheritWidth(true);
        final UILabel modeLabel = new UILabel(modeName + " - " + rotationName);
        modeLabel.setCenterX(false);
        modeInfo.add(modeLabel);
        modeInfo.add(new UIScale(1.1f, 1.1f, 1));
        top.add(modeInfo);
        return top;
    }

    public void addElements(final UIEntity parent) {
        if (!open)
            return;

        final SignalBoxGrid grid = gui.container.grid;
        final Set<Map.Entry<BlockPos, LinkType>> entrySet =
                gui.container.getPositionForTypes().entrySet();
        final EnumGuiMode mode = modeSet.mode;
        final Rotation rotation = modeSet.rotation;
        switch (mode) {
            case CORNER:
            case STRAIGHT:
            case CROSSING: {
                final EnumPathUsage path =
                        option.getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE);
                final UIEntity stateEntity = new UIEntity();
                stateEntity.setInheritWidth(true);
                stateEntity.setHeight(15);
                final String pathUsageName = I18Wrapper.format("property.status") + ": ";
                final String pathUsage = I18Wrapper.format("property." + path);
                stateEntity.add(new UILabel(pathUsageName + pathUsage));
                parent.add(stateEntity);

                gui.selectLink(parent, node, option, entrySet, LinkType.OUTPUT,
                        PathEntryType.OUTPUT, mode, rotation);

                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.BLOCKING, mode, rotation, ".blocking");
                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.RESETING, mode, rotation, ".resetting");

                final UIEntity zs2Entity = GuiElements.createEnumElement(JsonEnumHolder.ZS32, e -> {
                    if (e == 0) {
                        option.removeEntry(PathEntryType.ZS2);
                    } else {
                        option.setEntry(PathEntryType.ZS2, (byte) e);
                    }
                }, option.getEntry(PathEntryType.ZS2).orElse((byte) 0));
                parent.add(zs2Entity);

                parent.add(getTextFieldEntityforType(mode, rotation, PathEntryType.SPEED, "speed",
                        0, 20));

                parent.add(getTextFieldEntityforType(mode, rotation, PathEntryType.PATHWAY_COSTS,
                        "pathway_costs", SignalBoxUtil.getDefaultCosts(modeSet), 120));

                parent.add(getCheckBoxEntityforType(mode, rotation, PathEntryType.ZS6, "zs6_state",
                        TCBoolean.FALSE));
            }
                break;
            case VP:
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                parent.add(getCheckBoxEntityforType(mode, rotation, PathEntryType.SIGNAL_REPEATER,
                        "signal_repeater", false));
                break;
            case HP: {
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                final List<ModeIdentifier> preSignalsList = option
                        .getEntry(PathEntryType.PRESIGNALS).orElseGet(() -> new ArrayList<>());
                final UIEntity preSignalEntity = GuiElements
                        .createButton(I18Wrapper.format("property.presignals.name"), e -> {
                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));

                            final BoxEntity boxEntity =
                                    UISignalBoxRendering.createSignalBoxEntity(grid, gui.profile,
                                            gui.profile.getOperationModeSettings().borderSettings,
                                            (rendering, point, mouseKey) -> {
                                                final SignalBoxNode node =
                                                        grid.getNodeChecked(point)
                                                                .orElseGet(() -> new SignalBoxNode(
                                                                        gui.container.network));
                                                if (mouseKey != MouseEvent.LEFT_MOUSE
                                                        || node.isEmpty())
                                                    return;
                                                final AtomicReference<ModeIdentifier> vp =
                                                        new AtomicReference<>();
                                                node.getModes().forEach((nodeMode, entry) -> {
                                                    if (!(nodeMode.mode.equals(EnumGuiMode.VP)
                                                            || nodeMode.mode
                                                                    .equals(EnumGuiMode.ZS3)))
                                                        return;
                                                    vp.set(new ModeIdentifier(point, nodeMode));
                                                });
                                                final ModeIdentifier ident = vp.get();
                                                if (ident == null)
                                                    return;
                                                if (preSignalsList.contains(ident)) {
                                                    preSignalsList.remove(ident);
                                                    rendering.removeColoredPoint(
                                                            settings.getUserSelectionColor(),
                                                            point);
                                                } else {
                                                    preSignalsList.add(ident);
                                                    rendering.addColoredPoint(
                                                            settings.getUserSelectionColor(),
                                                            point);
                                                }
                                                if (preSignalsList.isEmpty()) {
                                                    option.removeEntry(PathEntryType.PRESIGNALS);
                                                } else {
                                                    option.setEntry(PathEntryType.PRESIGNALS,
                                                            preSignalsList);
                                                }
                                            });
                            preSignalsList.forEach(ident -> {
                                boxEntity.rendering.addColoredPoint(
                                        settings.getUserSelectionColor(), ident.point);
                            });
                            screen.add(boxEntity.entity);

                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                preSignalEntity.add(new UIToolTip(I18Wrapper.format("property.presignals.desc")));
                parent.add(preSignalEntity);

                final UIEntity protectionWay = GuiElements
                        .createButton(I18Wrapper.format("property.protectionway.name"), e -> {
                            final Point selcetedPoint =
                                    option.getEntry(PathEntryType.PROTECTIONWAY_END)
                                            .orElse(new Point(-1, -1));

                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));

                            final BoxEntity boxEntity = UISignalBoxRendering.createSignalBoxEntity(
                                    gui.container.grid, gui.profile,
                                    gui.profile.getOperationModeSettings().getUIBorderSettings(),
                                    (rendering, point, mouseKey) -> {
                                        final SignalBoxNode node = grid.getNodeChecked(point)
                                                .orElse(new SignalBoxNode(grid.getNetwork()));
                                        if (mouseKey != MouseEvent.LEFT_MOUSE || node.isEmpty())
                                            return;
                                        final Point select =
                                                option.getEntry(PathEntryType.PROTECTIONWAY_END)
                                                        .orElse(new Point(-1, -1));
                                        if (point.equals(select)) {
                                            rendering.removeSelection(SelectionType.FIRST);
                                            option.removeEntry(PathEntryType.PROTECTIONWAY_END);
                                        } else {
                                            rendering.addSelection(settings.getUserSelectionColor(),
                                                    point, SelectionType.FIRST);
                                            option.setEntry(PathEntryType.PROTECTIONWAY_END, point);
                                        }
                                    });
                            if (!selcetedPoint.equals(new Point(-1, -1))) {
                                boxEntity.rendering.addSelection(settings.getUserSelectionColor(),
                                        selcetedPoint, SelectionType.FIRST);
                            }

                            screen.add(boxEntity.entity);
                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                protectionWay.add(new UIToolTip(I18Wrapper.format("property.protectionway.desc")));
                parent.add(protectionWay);
                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.PROTECTIONWAY_RESET, mode, rotation, ".protectionway_reset");

                parent.add(getTextFieldEntityforType(mode, rotation, PathEntryType.DELAY,
                        "reset_protectionway_delay", 0, 120));
            }
            case RS: {
                if (mode.equals(EnumGuiMode.RS)) {
                    gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                            PathEntryType.SIGNAL, mode, rotation);
                }
                parent.add(getCheckBoxEntityforType(mode, rotation,
                        PathEntryType.CAN_BE_OVERSTPEPPED, "can_be_overstepped", false));
                break;
            }
            case BUE: {
                parent.add(getTextFieldEntityforType(mode, rotation, PathEntryType.DELAY, "delay",
                        0, 120));
                break;
            }
            case OUT_CONNECTION: {
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNALBOX,
                        PathEntryType.SIGNALBOX, mode, rotation);
                final Optional<BlockPos> boxPos = option.getEntry(PathEntryType.SIGNALBOX);
                if (!boxPos.isPresent()) {
                    break;
                }

                final List<Point> validInConnections = gui.container.validInConnections
                        .getOrDefault(boxPos.get(), new ArrayList<>());
                if (validInConnections.isEmpty()) {
                    break;
                }
                final IIntegerable<String> integerable = new DisableIntegerable<>(
                        SizeIntegerables.of("inconnection", validInConnections.size(), id -> {
                            final Point point = validInConnections.get(id);
                            if (point == null)
                                return "Disabled";
                            return point.toShortString();
                        }));
                parent.add(GuiElements.createEnumElement(integerable, e -> {
                    final Point point = e >= 0 ? validInConnections.get(e) : null;
                    if (point == null) {
                        option.removeEntry(PathEntryType.POINT);
                    } else {
                        option.setEntry(PathEntryType.POINT, point);
                    }
                }, option.getEntry(PathEntryType.POINT)
                        .map(point -> validInConnections.indexOf(point)).orElse(-1)));
                break;
            }

            case IN_CONNECTION: {
                final UIEntity inConnections = GuiElements
                        .createButton(I18Wrapper.format("property.inconnection.name"), e -> {
                            final Point selcetedPoint =
                                    option.getEntry(PathEntryType.POINT).orElse(new Point(-1, -1));

                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));

                            final BoxEntity boxEntity = UISignalBoxRendering.createSignalBoxEntity(
                                    gui.container.grid, gui.profile,
                                    gui.profile.getOperationModeSettings().getUIBorderSettings(),
                                    (rendering, point, mouseKey) -> {
                                        final SignalBoxNode node = grid.getNodeChecked(point)
                                                .orElse(new SignalBoxNode(grid.getNetwork()));
                                        if (mouseKey != MouseEvent.LEFT_MOUSE || !node.isValidEnd())
                                            return;
                                        final Point select = option.getEntry(PathEntryType.POINT)
                                                .orElse(new Point(-1, -1));
                                        if (point.equals(select)) {
                                            rendering.removeSelection(SelectionType.FIRST);
                                            option.removeEntry(PathEntryType.POINT);
                                        } else {
                                            rendering.addSelection(settings.getUserSelectionColor(),
                                                    point, SelectionType.FIRST);
                                            option.setEntry(PathEntryType.POINT, point);
                                        }
                                    });

                            if (!selcetedPoint.equals(new Point(-1, -1))) {
                                boxEntity.rendering.addSelection(settings.getUserSelectionColor(),
                                        selcetedPoint, SelectionType.FIRST);
                            }

                            screen.add(boxEntity.entity);
                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                inConnections.add(new UIToolTip(I18Wrapper.format("property.inconnection.desc")));
                parent.add(inConnections);
                break;
            }
            case ZS3: {
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                break;
            }
            case TRAIN_NUMBER: {
                final UIEntity button = GuiElements
                        .createButton(I18Wrapper.format("btn.connect.trainnumber"), e -> {
                            final ModeIdentifier identifier =
                                    option.getEntry(PathEntryType.CONNECTED_TRAINNUMBER)
                                            .orElse(new ModeIdentifier(new Point(-1, -1), null));
                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));
                            final BoxEntity entity = UISignalBoxRendering.createSignalBoxEntity(
                                    gui.container.grid, gui.profile,
                                    gui.profile.getOperationModeSettings().getUIBorderSettings(),
                                    (rendering, point, mouseKey) -> {
                                        if (mouseKey != MouseEvent.LEFT_MOUSE)
                                            return;
                                        final SignalBoxNode node =
                                                gui.container.grid.getNodeChecked(point).orElse(
                                                        new SignalBoxNode(grid.getNetwork()));
                                        if (node.isEmpty())
                                            return;

                                        final List<ModeSet> pathModes = new ArrayList<>();
                                        node.toPathIdentifier().stream()
                                                .map(ident -> ident.getMode()).forEach(modeSet -> {
                                                    if (!pathModes.contains(modeSet)) {
                                                        pathModes.add(modeSet);
                                                    }
                                                });

                                        if (pathModes.isEmpty()) {
                                            final UIToolTip tip = new UIToolTip(
                                                    I18Wrapper.format("gui.tile.notvalid"), true);
                                            screen.add(tip);
                                            gui.executor.schedule(() -> screen.remove(tip), 3,
                                                    TimeUnit.SECONDS);
                                        } else if (pathModes.size() == 1) {
                                            handleTrainNumberChange(node, pathModes.get(0),
                                                    rendering, false);
                                        } else {
                                            final UIEnumerable enumerable = new UIEnumerable(
                                                    pathModes.size(), "mode_select");
                                            enumerable.setMin(-1);
                                            enumerable.setIndex(-1);
                                            enumerable.setOnChange(i -> {
                                                final ModeSet modeSet = pathModes.get(i);
                                                handleTrainNumberChange(node, modeSet, rendering,
                                                        true);
                                            });
                                            gui.push(GuiElements.createSelectionScreen(enumerable,
                                                    SizeIntegerables.of("mode_select",
                                                            pathModes.size(), id -> {
                                                                final ModeSet modeSet =
                                                                        pathModes.get(id);
                                                                return modeSet.mode.toString()
                                                                        + " - "
                                                                        + SignalBoxUtil
                                                                                .getDegreeStringFromRotation(
                                                                                        modeSet.rotation);
                                                            })));
                                        }
                                    });
                            entity.rendering.addSelection(settings.getUserSelectionColor(),
                                    identifier.point, SelectionType.FIRST);
                            screen.add(entity.entity);
                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                parent.add(button);
                break;
            }
            default:
                break;
        }
    }

    private void handleTrainNumberChange(final SignalBoxNode node, final ModeSet mode,
            final UISignalBoxRendering rendering, final boolean wereMultipleEntries) {
        final PathOptionEntry optionEntry = node.getOption(mode).get();
        final ModeIdentifier thisIdent = new ModeIdentifier(this.node.getPoint(), modeSet);
        if (optionEntry.containsEntry(PathEntryType.CONNECTED_TRAINNUMBER)) {
            final ModeIdentifier otherIdent =
                    optionEntry.getEntry(PathEntryType.CONNECTED_TRAINNUMBER).get();
            if (!thisIdent.equals(otherIdent)) {
                gui.push(GuiElements.createScreen(screen -> {
                    final UIEntity entity = new UIEntity();
                    entity.setInherits(true);
                    entity.add(new UIBox(UIBox.VBOX, 5));
                    entity.add(new UIColor(gui.profile.getBackgroundColor()));
                    entity.add(GuiElements.createSpacerV(30));
                    entity.add(GuiElements.createLabel(I18Wrapper.format("info.key.removeother")));
                    entity.add(GuiElements.createSpacerV(30));

                    final UIEntity lowerEntity = new UIEntity();
                    lowerEntity.setInherits(true);
                    lowerEntity.add(new UIBox(UIBox.HBOX, 5));
                    lowerEntity.add(GuiElements.createSpacerH(10));
                    lowerEntity.add(GuiElements.createButton(I18Wrapper.format("btn.yes"), e -> {
                        disconnectFromEachOther(thisIdent, otherIdent, gui.container.grid, gui);
                        connectToEachOther(thisIdent, new ModeIdentifier(node.getPoint(), mode),
                                gui.container.grid, gui);
                        rendering.addSelection(settings.getUserSelectionColor(), node.getPoint(),
                                SelectionType.FIRST);
                        gui.pop();
                        if (wereMultipleEntries) {
                            gui.pop();
                        }
                    }));
                    lowerEntity.add(GuiElements.createSpacerH(20));
                    lowerEntity.add(GuiElements.createButton(I18Wrapper.format("btn.no"), e -> {
                        gui.pop();
                        if (wereMultipleEntries) {
                            gui.pop();
                        }
                    }));
                    entity.add(lowerEntity);
                    screen.add(entity);
                }));
                if (wereMultipleEntries) {
                    gui.push(new UIEntity());
                }
                return;
            }
            disconnectFromEachOther(thisIdent, new ModeIdentifier(node.getPoint(), mode),
                    gui.container.grid, gui);
            rendering.removeSelection(SelectionType.FIRST);
        } else {
            this.node.getOption(modeSet)
                    .ifPresent(numberOption -> numberOption
                            .getEntry(PathEntryType.CONNECTED_TRAINNUMBER)
                            .ifPresent(entry -> disconnectFromEachOther(thisIdent, entry,
                                    gui.container.grid, gui)));
            connectToEachOther(new ModeIdentifier(node.getPoint(), mode), thisIdent,
                    gui.container.grid, gui);
            rendering.addSelection(settings.getUserSelectionColor(), node.getPoint(),
                    SelectionType.FIRST);
        }
    }

    private static void connectToEachOther(final ModeIdentifier ident1, final ModeIdentifier ident2,
            final SignalBoxGrid grid, final GuiSignalBox gui) {
        final SignalBoxNode node1 = grid.getNode(ident1.point);
        node1.getOption(ident1.mode).get().setEntry(PathEntryType.CONNECTED_TRAINNUMBER, ident2);

        final SignalBoxNode node2 = grid.getNode(ident2.point);
        node2.getOption(ident2.mode).get().setEntry(PathEntryType.CONNECTED_TRAINNUMBER, ident1);
    }

    private static void disconnectFromEachOther(final ModeIdentifier ident1,
            final ModeIdentifier ident2, final SignalBoxGrid grid, final GuiSignalBox gui) {
        final SignalBoxNode node1 = grid.getNode(ident1.point);
        node1.getOption(ident1.mode)
                .ifPresent(entry -> entry.removeEntry(PathEntryType.CONNECTED_TRAINNUMBER));

        final SignalBoxNode node2 = grid.getNode(ident2.point);
        node2.getOption(ident2.mode)
                .ifPresent(entry -> entry.removeEntry(PathEntryType.CONNECTED_TRAINNUMBER));
    }

    private void changeShowState() {
        open = !open;
        update.run();
    }

    private UIEntity getTextFieldEntityforType(final EnumGuiMode mode, final Rotation rotation,
            final PathEntryType<Integer> type, final String name, final int defaultValue,
            final int max) {
        final UIEntity hentity = new UIEntity();
        hentity.setInheritWidth(true);
        hentity.setHeight(21);
        hentity.add(new UIBox(UIBox.HBOX, 0));

        final UIEntity labelEntity = new UIEntity();
        labelEntity.setInheritWidth(true);
        labelEntity.setHeight(20);
        labelEntity.add(new UILabel(I18Wrapper.format("property." + name + ".name")));
        hentity.add(labelEntity);

        hentity.add(new UIToolTip(I18Wrapper.format("property." + name + ".desc")));

        final UIEntity textInputEntity = new UIEntity();
        textInputEntity.setInheritWidth(true);
        textInputEntity.setHeight(20);

        final UITextInput input =
                new UITextInput(String.valueOf(option.getEntry(type).orElse(defaultValue)));
        input.setValidator(str -> {
            if (str.isEmpty())
                return true;
            try {
                final int i = Integer.valueOf(str);
                if (i < 0 || i > max)
                    return false;
            } catch (final Exception e) {
                return false;
            }
            return true;
        });
        input.setOnTextUpdate(str -> {
            int i = 0;
            if (!str.isEmpty()) {
                try {
                    i = Integer.valueOf(str);
                } catch (final Exception e) {
                }
            }
            if (i != defaultValue) {
                option.setEntry(type, i);
            } else {
                option.removeEntry(type);
            }
        });
        textInputEntity.add(input);

        hentity.add(textInputEntity);
        return hentity;
    }

    private UIEntity getCheckBoxEntityforType(final EnumGuiMode mode, final Rotation rotation,
            final PathEntryType<TCBoolean> type, final String name, final TCBoolean defaultValue) {
        final SoundManager handler = Minecraft.getInstance().getSoundManager();

        final UIEntity hentity = new UIEntity();
        hentity.setInheritWidth(true);
        hentity.setHeight(22);
        hentity.add(new UIBox(UIBox.HBOX, 0));

        hentity.add(getBoolLabelEntity(name));
        hentity.add(new UIToolTip(I18Wrapper.format("property." + name + ".desc")));
        final UIEntity checkBoxEntity = getBoolCheckBoxEntity();

        final UICheckBox checkBox = new UICheckBox("");
        checkBox.setChecked((option.getEntry(type).orElseGet(() -> defaultValue)).booleanValue());
        final UIClickable clickable = new UIClickable(e -> {
            checkBox.setChecked(!checkBox.isChecked());
            handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (checkBox.isChecked() != defaultValue.booleanValue()) {
                option.setEntry(type, TCBoolean.valueOf(checkBox.isChecked()));
            } else {
                option.removeEntry(type);
            }
        });
        checkBoxEntity.add(checkBox);
        checkBoxEntity.add(clickable);

        hentity.add(checkBoxEntity);
        return hentity;
    }

    private UIEntity getCheckBoxEntityforType(final EnumGuiMode mode, final Rotation rotation,
            final PathEntryType<Boolean> type, final String name, final boolean defaultValue) {
        final SoundManager handler = Minecraft.getInstance().getSoundManager();

        final UIEntity hentity = new UIEntity();
        hentity.setInheritWidth(true);
        hentity.setHeight(22);
        hentity.add(new UIBox(UIBox.HBOX, 0));

        hentity.add(getBoolLabelEntity(name));
        hentity.add(new UIToolTip(I18Wrapper.format("property." + name + ".desc")));
        final UIEntity checkBoxEntity = getBoolCheckBoxEntity();

        final UICheckBox checkBox = new UICheckBox("");
        checkBox.setChecked((option.getEntry(type).orElseGet(() -> defaultValue)));
        final UIClickable clickable = new UIClickable(e -> {
            checkBox.setChecked(!checkBox.isChecked());
            handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (checkBox.isChecked() != defaultValue) {
                option.setEntry(type, checkBox.isChecked());
            } else {
                option.removeEntry(type);
            }
        });
        checkBoxEntity.add(checkBox);
        checkBoxEntity.add(clickable);

        hentity.add(checkBoxEntity);
        return hentity;
    }

    private static UIEntity getBoolLabelEntity(final String name) {
        final UIEntity labelEntity = new UIEntity();
        labelEntity.setInheritWidth(true);
        labelEntity.setHeight(20);
        labelEntity.add(new UILabel(I18Wrapper.format("property." + name + ".name")));
        return labelEntity;
    }

    private static UIEntity getBoolCheckBoxEntity() {
        final UIEntity checkBoxEntity = new UIEntity();
        checkBoxEntity.setInheritWidth(true);
        checkBoxEntity.setHeight(20);
        checkBoxEntity.setScale(1.3f);
        return checkBoxEntity;
    }

}