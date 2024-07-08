package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.guis.ContainerTrainNumber.TrainNumberNetwork;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class GuiTrainNumber extends GuiBase {

    private final ContainerTrainNumber container;
    private final EntityPlayer player;

    public GuiTrainNumber(final GuiInfo info) {
        super(info);
        this.container = (ContainerTrainNumber) info.base;
        this.player = info.player;
        this.entity.clear();
        this.entity.add(new UILabel("Not connected"));
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerTrainNumber(info);
    }

    private void initOwn() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.VBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setWidth(200);
        inner.setInheritHeight(true);
        inner.setX(70);
        inner.add(new UIBox(UIBox.VBOX, 5));
        inner.add(GuiElements.createSpacerV(10));

        final UIEntity label = GuiElements.createLabel(I18Wrapper.format("tile.trainnumberchanger"),
                0x7678a0);
        label.setScaleX(1.5f);
        label.setScaleY(1.5f);
        label.setX(-6);
        inner.add(label);
        inner.add(GuiElements.createSpacerV(20));

        final UIEntity inputEntity = new UIEntity();
        inputEntity.setHeight(20);
        inputEntity.setInheritWidth(true);
        inputEntity.add(new UIBox(UIBox.HBOX, 0));
        final UITextInput textInput = new UITextInput(container.number.trainNumber);
        textInput.setOnTextUpdate(this::sendNewTrainNumber);
        final UIEntity input = new UIEntity();
        input.add(textInput);
        input.setHeight(20);
        input.setWidth(150);
        inputEntity.add(input);
        inputEntity.add(GuiElements.createSpacerH(5));
        inputEntity.add(
                GuiElements.createButton(I18Wrapper.format("gui.trainnumber.change_set"), e -> {
                    setTrainNumber();
                    textInput.setText("");
                }));
        inputEntity.add(new UIToolTip(I18Wrapper.format("gui.trainnumber.info.change")));
        inner.add(inputEntity);

        final UIEntity changeButton = GuiElements
                .createButton(I18Wrapper.format("gui.trainnumber.setpoint"), e -> {
                    final UIEntity grid = new UIEntity();
                    grid.setInherits(true);
                    grid.add(new UIBox(UIBox.VBOX, 0));
                    SignalBoxUIHelper.initializeGrid(grid, container.grid, (tile, sbt) -> {
                        final Point name = sbt.getPoint();
                        tile.add(new UIClickable(e1 -> {
                            if (sbt.getNode().isEmpty())
                                return;
                            container.setPoint = name;
                            sendNewPoint();
                            pop();
                        }));
                        if (name.equals(container.setPoint)) {
                            tile.add(new UIColor(GuiSignalBox.SELECTION_COLOR));
                        }
                    });
                    push(GuiElements.createScreen(screen -> screen.add(grid)));
                });
        changeButton.add(new UIToolTip(I18Wrapper.format("gui.trainnumber.setpoint.desc")));
        inner.add(changeButton);

        inner.add(GuiElements.createSpacerV(5));

        final BlockPos pos = container.linkedPos;
        inner.add(
                GuiElements
                        .createLabel(
                                "Linked SignalBox: " + (pos == null ? "Not linked!"
                                        : pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                                1.2f));
        entity.add(inner);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

    private void sendNewPoint() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(TrainNumberNetwork.SEND_NEW_POINT);
        container.setPoint.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendNewTrainNumber(final String number) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(TrainNumberNetwork.SEND_NEW_TRAINNUMBER);
        buffer.putString(number);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void setTrainNumber() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(TrainNumberNetwork.SET_TRAINNUMBER);
        OpenSignalsMain.network.sendTo(player, buffer);
    }
}