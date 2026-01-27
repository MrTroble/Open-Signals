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
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxUtil;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class ModeDropDownBoxUI {

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
            top.add(GuiElements.createButton("↓", 20, e -> changeShowState()));
        } else {
            top.add(GuiElements.createButton("→", 20, e -> changeShowState()));
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

                final SizeIntegerables<Integer> size = new SizeIntegerables<>("speed", 15, i -> i);
                final UIEntity speedSelection = GuiElements.createEnumElement(size, id -> {
                    final int speed = id > 0 ? id : 127;
                    final Optional<Integer> opt = option.getEntry(PathEntryType.SPEED);
                    if (speed == 127 && opt.isPresent()) {
                        option.removeEntry(PathEntryType.SPEED);
                    } else if ((opt.isPresent() && opt.get() != speed)
                            || (!opt.isPresent() && speed != 127)) {
                        option.setEntry(PathEntryType.SPEED, speed);
                    }
                }, option.getEntry(PathEntryType.SPEED).filter(n -> n < 16).orElse(127));
                parent.add(speedSelection);

                gui.selectLink(parent, node, option, entrySet, LinkType.OUTPUT,
                        PathEntryType.OUTPUT, mode, rotation);

                parent.add(getTextFieldEntityforType(mode, rotation, PathEntryType.PATHWAY_COSTS,
                        I18Wrapper.format("property.pathway_costs.name"),
                        SignalBoxUtil.getDefaultCosts(modeSet)));

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
                Optional<TCBoolean> opt = option.getEntry(PathEntryType.ZS6);
                parent.add(GuiElements.createBoolElement(BoolIntegerables.of("zs6_state"), e -> {
                    final boolean state = e == 1 ? true : false;
                    if (state) {
                        option.setEntry(PathEntryType.ZS6, TCBoolean.valueOf(state));
                    } else {
                        option.removeEntry(PathEntryType.ZS6);
                    }
                }, opt.isPresent() && opt.get().booleanValue() ? 1 : 0));
            }
                break;
            case VP:
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                final Optional<Boolean> opt = option.getEntry(PathEntryType.SIGNAL_REPEATER);
                parent.add(
                        GuiElements.createBoolElement(BoolIntegerables.of("signal_repeater"), e -> {
                            final boolean state = e == 1 ? true : false;
                            if (state) {
                                option.setEntry(PathEntryType.SIGNAL_REPEATER, state);
                            } else {
                                option.removeEntry(PathEntryType.SIGNAL_REPEATER);
                            }
                        }, opt.isPresent() && opt.get() ? 1 : 0));
                break;
            case HP: {
                final List<PosIdentifier> preSignalsList = option.getEntry(PathEntryType.PRESIGNALS)
                        .orElseGet(() -> new ArrayList<>());
                final UIEntity preSignalEntity = GuiElements
                        .createButton(I18Wrapper.format("property.presignals.name"), e -> {
                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));

                            final BoxEntity boxEntity = UISignalBoxRendering.createSignalBoxEntity(
                                    grid, false, (rendering, point, mouseKey) -> {
                                        final SignalBoxNode node = grid.getNodeChecked(point)
                                                .orElse(new SignalBoxNode(grid.getNetwork()));
                                        if (mouseKey != MouseEvent.LEFT_MOUSE || node.isEmpty())
                                            return;
                                        final AtomicReference<PosIdentifier> vp =
                                                new AtomicReference<>();
                                        node.getModes().forEach((nodeMode, entry) -> {
                                            if (!(nodeMode.mode.equals(EnumGuiMode.VP)
                                                    || nodeMode.mode.equals(EnumGuiMode.ZS3)))
                                                return;
                                            final BlockPos linkedSignal = entry
                                                    .getEntry(PathEntryType.SIGNAL).orElse(null);
                                            if (linkedSignal == null)
                                                return;
                                            vp.set(new PosIdentifier(point, nodeMode,
                                                    linkedSignal));
                                        });
                                        final PosIdentifier ident = vp.get();
                                        if (ident == null)
                                            return;
                                        if (preSignalsList.contains(ident)) {
                                            preSignalsList.remove(ident);
                                            rendering.removeColoredPoint(
                                                    GuiSignalBox.SELECTION_COLOR, point);
                                        } else {
                                            preSignalsList.add(ident);
                                            rendering.addColoredPoint(GuiSignalBox.SELECTION_COLOR,
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
                                boxEntity.rendering.addColoredPoint(GuiSignalBox.SELECTION_COLOR,
                                        ident.getPoint());
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
                                    gui.container.grid, false, (rendering, point, mouseKey) -> {
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
                                            rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
                                                    point, SelectionType.FIRST);
                                            option.setEntry(PathEntryType.PROTECTIONWAY_END, point);
                                        }
                                    });

                            if (!selcetedPoint.equals(new Point(-1, -1))) {
                                boxEntity.rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
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
                        I18Wrapper.format("property.reset_protectionway_delay.name"), 0));
            }
            case RS: {
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                parent.add(GuiElements.createBoolElement(BoolIntegerables.of("can_be_overstepped"),
                        e -> {
                            final boolean state = e == 1 ? true : false;
                            if (state) {
                                option.setEntry(PathEntryType.CAN_BE_OVERSTPEPPED, state);
                            } else {
                                option.removeEntry(PathEntryType.CAN_BE_OVERSTPEPPED);
                            }
                        },
                        option.getEntry(PathEntryType.CAN_BE_OVERSTPEPPED).orElse(false) ? 1 : 0));
                break;
            }
            case BUE: {
                parent.add(GuiElements.createEnumElement(
                        new SizeIntegerables<>("delay", 60, get -> String.valueOf(get)), i -> {
                            if (i == 0) {
                                option.removeEntry(PathEntryType.DELAY);
                            } else {
                                option.setEntry(PathEntryType.DELAY, i);
                            }
                        }, option.getEntry(PathEntryType.DELAY).orElse(0)));
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
                                    gui.container.grid, false, (rendering, point, mouseKey) -> {
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
                                            rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
                                                    point, SelectionType.FIRST);
                                            option.setEntry(PathEntryType.POINT, point);
                                        }
                                    });

                            if (!selcetedPoint.equals(new Point(-1, -1))) {
                                boxEntity.rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
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
                                    gui.container.grid, false, (rendering, point, mouseKey) -> {
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
                            entity.rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
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
                    entity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
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
                        rendering.addSelection(GuiSignalBox.SELECTION_COLOR, node.getPoint(),
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
            rendering.addSelection(GuiSignalBox.SELECTION_COLOR, node.getPoint(),
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
            final PathEntryType<Integer> type, final String labelName, final int defaultValue) {
        final UIEntity hentity = new UIEntity();
        hentity.setInheritWidth(true);
        hentity.setHeight(20);
        hentity.add(new UIBox(UIBox.HBOX, 0));

        final UIEntity labelEntity = new UIEntity();
        labelEntity.setInheritWidth(true);
        labelEntity.setHeight(20);
        labelEntity.add(new UILabel(labelName));
        hentity.add(labelEntity);

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
                if (i < 0 || i > 120)
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

}
