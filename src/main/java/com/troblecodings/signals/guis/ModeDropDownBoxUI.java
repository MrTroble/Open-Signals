package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class ModeDropDownBoxUI {

    private final ModeSet modeSet;
    private final Runnable update;
    private final PathOptionEntry option;
    private final GuiSignalBox gui;
    private final SignalBoxNode node;
    private boolean open;

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
        if (!open) {
            return;
        }
        final Set<Entry<BlockPos, LinkType>> entrySet = gui.container.getPositionForTypes()
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

                if (path.equals(EnumPathUsage.BLOCKED)) {
                    final UIEntity layout = new UIEntity();
                    layout.add(new UIBox(UIBox.HBOX, 2));
                    layout.setHeight(20);
                    layout.setInheritWidth(true);
                    final UIEntity inputEntity = new UIEntity();
                    inputEntity.setInheritHeight(true);
                    inputEntity.setWidth(250);
                    final UITextInput input = new UITextInput("");
                    inputEntity.add(input);
                    inputEntity.add(new UIToolTip(I18Wrapper.format("sb.trainnumber.change")));
                    layout.add(inputEntity);
                    final UIEntity save = GuiElements.createButton(I18Wrapper.format("btn.save"),
                            e -> {
                                gui.sendTrainNumber(node.getPoint(), input.getText());
                                input.setText("");
                            });
                    save.add(new UIToolTip(I18Wrapper.format("sb.trainnumber.save")));
                    layout.add(save);
                    final UIEntity remove = GuiElements.createButton("x",
                            e -> gui.deleteTrainNumber(node.getPoint()));
                    remove.add(new UIToolTip(I18Wrapper.format("sb.trainnumber.remove")));
                    layout.add(remove);
                    parent.add(layout);
                }

                final SizeIntegerables<Integer> size = new SizeIntegerables<>("speed", 15, i -> i);
                final UIEntity speedSelection = GuiElements.createEnumElement(size, id -> {
                    final int speed = id > 0 ? id : 127;
                    final Optional<Integer> opt = option.getEntry(PathEntryType.SPEED);
                    if (speed == 127 && opt.isPresent()) {
                        gui.removeEntryFromServer(node, mode, rotation, PathEntryType.SPEED);
                        option.removeEntry(PathEntryType.SPEED);
                    } else if ((opt.isPresent() && opt.get() != speed)
                            || (opt.isEmpty() && speed != 127)) {
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

                if (option.getEntry(PathEntryType.OUTPUT).isPresent()) {
                    final AtomicBoolean canBeManuelChanged = new AtomicBoolean(true);
                    for (final Map.Entry<ModeSet, PathOptionEntry> entry : node.getModes()
                            .entrySet()) {
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
                    final UIEntity manuelButton = GuiElements
                            .createButton(I18Wrapper.format("info.usage.manuel"), e1 -> {
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
                                textureEntity.add(
                                        new UIToolTip(I18Wrapper.format("info.usage.rs.desc")));
                                if (canBeManuelChanged.get()) {
                                    if (node.containsManuellOutput(modeSet)) {
                                        textureEntity.add(new UITexture(GuiSignalBox.REDSTONE_ON));
                                    } else {
                                        textureEntity.add(new UITexture(GuiSignalBox.REDSTONE_OFF));
                                    }
                                } else {
                                    if (usage.isPresent()
                                            && !usage.get().equals(EnumPathUsage.FREE)) {
                                        textureEntity.add(
                                                new UITexture(GuiSignalBox.REDSTONE_ON_BLOCKED));
                                    } else {
                                        textureEntity.add(
                                                new UITexture(GuiSignalBox.REDSTONE_OFF_BLOCKED));
                                    }
                                }
                                info.add(textureEntity);
                                final UILabel outputStatus = new UILabel(((usage.isPresent()
                                        && !usage.get().equals(EnumPathUsage.FREE))
                                        || node.containsManuellOutput(modeSet))
                                                ? I18Wrapper.format("info.usage.rs.true")
                                                : I18Wrapper.format("info.usage.rs.false"));
                                outputStatus.setCenterY(false);
                                outputStatus.setTextColor(new UIEntity().getBasicTextColor());
                                final UIEntity outputEntity = new UIEntity();
                                outputEntity.setInheritWidth(true);
                                outputEntity.setHeight(20);
                                outputEntity.add(outputStatus);
                                info.add(outputEntity);
                                if (canBeManuelChanged.get()) {
                                    info.add(GuiElements.createButton(
                                            I18Wrapper.format("info.usage.change"), i -> {
                                                final boolean turnOff = node
                                                        .containsManuellOutput(modeSet);
                                                textureEntity.clear();
                                                textureEntity.add(new UIToolTip(
                                                        I18Wrapper.format("info.usage.rs.desc")));
                                                if (turnOff) {
                                                    gui.changeRedstoneOutput(node.getPoint(),
                                                            modeSet, false);
                                                    outputStatus.setText(I18Wrapper
                                                            .format("info.usage.rs.false"));
                                                    textureEntity.add(new UITexture(
                                                            GuiSignalBox.REDSTONE_OFF));
                                                } else {
                                                    gui.changeRedstoneOutput(node.getPoint(),
                                                            modeSet, true);
                                                    outputStatus.setText(I18Wrapper
                                                            .format("info.usage.rs.true"));
                                                    textureEntity.add(new UITexture(
                                                            GuiSignalBox.REDSTONE_ON));
                                                }
                                            }));
                                }
                                final UIEntity screen = GuiElements.createScreen(e -> e.add(info));
                                gui.push(screen);
                            });
                    manuelButton.add(new UIToolTip(I18Wrapper.format("info.usage.manuel.desc")));
                    parent.add(manuelButton);

                }
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
                parent.add(GuiElements.createButton(I18Wrapper.format("btn.presignals"), e -> {
                    final UIEntity screen = new UIEntity();
                    screen.setInherits(true);
                    screen.add(new UIBox(UIBox.VBOX, 5));
                    screen.add(GuiElements.createButton(I18Wrapper.format("btn.return"),
                            e1 -> gui.pop()));
                    SignalBoxUIHelper.initializeGrid(screen, gui.container.grid, (tile, sbt) -> {
                        final AtomicReference<PosIdentifier> vp = new AtomicReference<>();
                        sbt.getNode().getModes().forEach((nodeMode, entry) -> {
                            if (!nodeMode.mode.equals(EnumGuiMode.VP))
                                return;
                            final BlockPos linkedSignal = entry.getEntry(PathEntryType.SIGNAL)
                                    .orElse(null);
                            if (linkedSignal == null)
                                return;
                            vp.set(new PosIdentifier(sbt.getPoint(), nodeMode, linkedSignal));
                        });
                        final PosIdentifier ident = vp.get();
                        if (ident == null)
                            return;
                        final UIColor color = new UIColor(GuiSignalBox.SELECTION_COLOR);
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
                                option.setEntry(PathEntryType.PRESIGNALS, preSignalsList);
                                gui.sendPosIdentList(preSignalsList, node, mode, rotation,
                                        PathEntryType.PRESIGNALS);
                            }
                        }));
                        if (preSignalsList.contains(ident)) {
                            tile.add(color);
                        }
                    });
                    gui.push(GuiElements.createScreen(e1 -> e1.add(screen)));
                }));
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
                if (!boxPos.isPresent())
                    break;
                final List<Point> validInConnections = gui.container.validInConnections
                        .getOrDefault(boxPos.get(), new ArrayList<>());
                if (validInConnections.isEmpty())
                    break;
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
                if (validEnds.isEmpty())
                    break;
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
            default:
                break;
        }
    }

    private void changeShowState() {
        open = !open;
        update.run();
    }

}
