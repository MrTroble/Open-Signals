package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxUtil;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class ModeDropDownBoxUI {

    private final ModeSet modeSet;
    private final Runnable update;
    private final PathOptionEntry option;
    private final GuiSignalBox gui;
    private final SignalBoxNode node;
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
        final String rotationName = I18Wrapper
                .format("property." + modeSet.rotation.name() + ".rotation");

        final UIEntity top = new UIEntity();
        top.setInheritWidth(true);
        top.setHeight(20);
        top.add(new UIBox(UIBox.HBOX, 5));
        top.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));

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
        final Set<Map.Entry<BlockPos, LinkType>> entrySet = gui.container.getPositionForTypes()
                .entrySet();
        final EnumGuiMode mode = modeSet.mode;
        final Rotation rotation = modeSet.rotation;
        switch (mode) {
            case CORNER:
            case STRAIGHT: {
                final EnumPathUsage path = option.getEntry(PathEntryType.PATHUSAGE)
                        .orElse(EnumPathUsage.FREE);
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
                        gui.removeEntryFromServer(node, mode, rotation, PathEntryType.SPEED);
                        option.removeEntry(PathEntryType.SPEED);
                    } else if ((opt.isPresent() && opt.get() != speed)
                            || (!opt.isPresent() && speed != 127)) {
                        gui.sendIntEntryToServer(speed, node, mode, rotation, PathEntryType.SPEED);
                        option.setEntry(PathEntryType.SPEED, speed);
                    }
                }, option.getEntry(PathEntryType.SPEED).filter(n -> n < 16).orElse(127));
                parent.add(speedSelection);

                gui.selectLink(parent, node, option, entrySet, LinkType.OUTPUT,
                        PathEntryType.OUTPUT, mode, rotation);

                final SizeIntegerables<Integer> pathwayCosts = new SizeIntegerables<>(
                        "pathway_costs", 20, i -> i);
                final UIEntity costSelection = GuiElements.createEnumElement(pathwayCosts, i -> {
                    option.setEntry(PathEntryType.PATHWAY_COSTS, i);
                    gui.sendIntEntryToServer(i, node, mode, rotation, PathEntryType.PATHWAY_COSTS);
                }, option.getEntry(PathEntryType.PATHWAY_COSTS)
                        .orElse(SignalBoxUtil.getDefaultCosts(modeSet)));
                parent.add(costSelection);

                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.BLOCKING, mode, rotation, ".blocking");
                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.RESETING, mode, rotation, ".resetting");

                final UIEntity zs2Entity = GuiElements.createEnumElement(JsonEnumHolder.ZS32, e -> {
                    if (e == 0) {
                        gui.removeEntryFromServer(node, mode, rotation, PathEntryType.ZS2);
                        option.removeEntry(PathEntryType.ZS2);
                    } else {
                        gui.sendZS2Entry((byte) e, node, mode, rotation, PathEntryType.ZS2);
                        option.setEntry(PathEntryType.ZS2, (byte) e);
                    }
                }, option.getEntry(PathEntryType.ZS2).orElse((byte) 0));
                parent.add(zs2Entity);
            }
                break;
            case VP:
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                final Optional<Boolean> opt = option.getEntry(PathEntryType.SIGNAL_REPEATER);
                parent.add(
                        GuiElements.createBoolElement(BoolIntegerables.of("signal_repeater"), e -> {
                            final boolean state = e == 1 ? true : false;
                            gui.sendBoolEntry(state, node.getPoint(), modeSet,
                                    PathEntryType.SIGNAL_REPEATER);
                            option.setEntry(PathEntryType.SIGNAL_REPEATER, state);
                        }, opt.isPresent() && opt.get() ? 1 : 0));
                break;
            case HP: {
                final List<PosIdentifier> preSignalsList = option.getEntry(PathEntryType.PRESIGNALS)
                        .orElse(new ArrayList<>());
                final UIEntity preSignalEntity = GuiElements
                        .createButton(I18Wrapper.format("property.presignals.name"), e -> {
                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));
                            SignalBoxUIHelper.initializeGrid(screen, gui.container.grid,
                                    (tile, sbt) -> {
                                        final AtomicReference<PosIdentifier> vp = new AtomicReference<>();
                                        sbt.getNode().getModes().forEach((nodeMode, entry) -> {
                                            if (!(nodeMode.mode.equals(EnumGuiMode.VP)
                                                    || nodeMode.mode.equals(EnumGuiMode.ZS3)))
                                                return;
                                            final BlockPos linkedSignal = entry
                                                    .getEntry(PathEntryType.SIGNAL).orElse(null);
                                            if (linkedSignal == null)
                                                return;
                                            vp.set(new PosIdentifier(sbt.getPoint(), nodeMode,
                                                    linkedSignal));
                                        });
                                        final PosIdentifier ident = vp.get();
                                        if (ident == null)
                                            return;
                                        final UIColor color = new UIColor(
                                                GuiSignalBox.SELECTION_COLOR);
                                        tile.add(new UIClickable(e1 -> {
                                            if (preSignalsList.contains(ident)) {
                                                preSignalsList.remove(ident);
                                                tile.remove(color);
                                            } else {
                                                preSignalsList.add(ident);
                                                tile.add(color);
                                            }
                                            if (preSignalsList.isEmpty()) {
                                                option.removeEntry(PathEntryType.PRESIGNALS);
                                                gui.removeEntryFromServer(node, mode, rotation,
                                                        PathEntryType.PRESIGNALS);
                                            } else {
                                                option.setEntry(PathEntryType.PRESIGNALS,
                                                        preSignalsList);
                                                gui.sendPosIdentList(preSignalsList, node, mode,
                                                        rotation, PathEntryType.PRESIGNALS);
                                            }
                                        }));
                                        if (preSignalsList.contains(ident)) {
                                            tile.add(color);
                                        }
                                    });
                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                preSignalEntity.add(new UIToolTip(I18Wrapper.format("property.presignals.desc")));
                parent.add(preSignalEntity);

                final Point selcetedPoint = option.getEntry(PathEntryType.PROTECTIONWAY_END)
                        .orElse(new Point(-1, -1));
                final UIEntity protectionWay = GuiElements
                        .createButton(I18Wrapper.format("property.protectionway.name"), e -> {
                            final UIEntity screen = new UIEntity();
                            screen.setInherits(true);
                            screen.add(new UIBox(UIBox.VBOX, 5));
                            screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                                    e1 -> gui.pop()));
                            final AtomicReference<UIEntity> previous = new AtomicReference<>();
                            SignalBoxUIHelper.initializeGrid(screen, gui.container.grid,
                                    (tile, sbt) -> {
                                        if (sbt.getNode().isEmpty())
                                            return;
                                        final Point point = sbt.getPoint();
                                        final UIColor color = new UIColor(
                                                GuiSignalBox.SELECTION_COLOR);
                                        if (point.equals(selcetedPoint)) {
                                            tile.add(color);
                                            previous.set(tile);
                                        }
                                        tile.add(new UIClickable(e1 -> {
                                            if (previous.get() != null) {
                                                previous.get().findRecursive(UIColor.class)
                                                        .forEach(previous.get()::remove);
                                            }
                                            if (point.equals(selcetedPoint)) {
                                                gui.removeEntryFromServer(node, mode, rotation,
                                                        PathEntryType.PROTECTIONWAY_END);
                                            } else {
                                                tile.add(color);
                                                previous.set(tile);
                                                gui.sendPointEntry(point, node, mode, rotation,
                                                        PathEntryType.PROTECTIONWAY_END);
                                                option.setEntry(PathEntryType.PROTECTIONWAY_END,
                                                        point);
                                            }
                                        }));
                                    });
                            gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                        });
                protectionWay.add(new UIToolTip(I18Wrapper.format("property.protectionway.desc")));
                parent.add(protectionWay);
                gui.selectLink(parent, node, option, entrySet, LinkType.INPUT,
                        PathEntryType.PROTECTIONWAY_RESET, mode, rotation, ".protectionway_reset");
                parent.add(GuiElements.createEnumElement(new SizeIntegerables<>(
                        "reset_protectionway_delay", 60, get -> String.valueOf(get)), i -> {
                            option.setEntry(PathEntryType.DELAY, i);
                            gui.sendIntEntryToServer(i, node, mode, rotation, PathEntryType.DELAY);
                        }, option.getEntry(PathEntryType.DELAY).orElse(0)));
            }
            case RS: {
                parent.add(GuiElements.createBoolElement(BoolIntegerables.of("can_be_overstepped"),
                        e -> {
                            final boolean state = e == 1 ? true : false;
                            option.setEntry(PathEntryType.CAN_BE_OVERSTPEPPED, state);
                            gui.sendBoolEntry(state, node.getPoint(), modeSet,
                                    PathEntryType.CAN_BE_OVERSTPEPPED);
                        },
                        option.getEntry(PathEntryType.CAN_BE_OVERSTPEPPED).orElse(false) ? 1 : 0));
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                break;
            }
            case BUE: {
                parent.add(GuiElements.createEnumElement(
                        new SizeIntegerables<>("delay", 60, get -> String.valueOf(get)), i -> {
                            option.setEntry(PathEntryType.DELAY, i);
                            gui.sendIntEntryToServer(i, node, mode, rotation, PathEntryType.DELAY);
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
                        gui.removeEntryFromServer(node, mode, rotation, PathEntryType.POINT);
                    } else {
                        option.setEntry(PathEntryType.POINT, point);
                        gui.sendPointEntry(point, node, mode, rotation, PathEntryType.POINT);
                    }
                }, option.getEntry(PathEntryType.POINT)
                        .map(point -> validInConnections.indexOf(point)).orElse(-1)));
                break;
            }
            case IN_CONNECTION: {
                final List<Point> validEnds = gui.container.grid.getValidEnds();
                if (validEnds.isEmpty()) {
                    break;
                }
                final IIntegerable<String> integerable = new DisableIntegerable<>(
                        SizeIntegerables.of("inconnection", validEnds.size(), id -> {
                            final Point point = validEnds.get(id);
                            if (point == null)
                                return "Disabled";
                            return point.toShortString();
                        }));
                parent.add(GuiElements.createEnumElement(integerable, e -> {
                    final Point point = e >= 0 ? validEnds.get(e) : null;
                    if (point == null) {
                        option.removeEntry(PathEntryType.POINT);
                        gui.removeEntryFromServer(node, mode, rotation, PathEntryType.POINT);
                    } else {
                        option.setEntry(PathEntryType.POINT, point);
                        gui.sendPointEntry(point, node, mode, rotation, PathEntryType.POINT);
                    }
                }, option.getEntry(PathEntryType.POINT).map(point -> validEnds.indexOf(point))
                        .orElse(-1)));
                break;
            }
            case ZS3: {
                gui.selectLink(parent, node, option, entrySet, LinkType.SIGNAL,
                        PathEntryType.SIGNAL, mode, rotation);
                break;
            }
            default:
                break;
        }
    }

    private void changeShowState() {
        open = !open;
        update.run();
    }

}