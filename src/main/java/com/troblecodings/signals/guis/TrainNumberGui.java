package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.guis.TrainNumberContainer.TrainNumberNetwork;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.entity.player.EntityPlayer;

public class TrainNumberGui extends GuiBase {

    private final TrainNumberContainer container;
    private final EntityPlayer player;

    public TrainNumberGui(final GuiInfo info) {
        super(info);
        this.container = (TrainNumberContainer) info.base;
        this.player = info.player;
        this.entity.clear();
        this.entity.add(new UILabel("Not connected"));
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new TrainNumberContainer(info);
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

        final IIntegerable<String> points = SizeIntegerables.of(I18Wrapper.format("gui.setpoint"),
                container.validPoints.size(), e -> {
                    if (e == -1)
                        return "Disabled";
                    return container.validPoints.get(e).toShortString();
                });
        inner.add(GuiElements.createEnumElement(new DisableIntegerable<>(points), e -> {
            if (e == -1) {
                container.setPoint = new Point(-1, -1);
                sendNewPoint();
                return;
            }
            container.setPoint = container.validPoints.get(e);
            sendNewPoint();
        }, container.setPoint != null ? container.setPoint.equals(new Point(-1, -1)) ? -1
                : container.validPoints.indexOf(container.setPoint) : -1));

        inner.add(GuiElements.createSpacerV(5));

        final UILabel linkedlabel = new UILabel("Linked SignalBox: "
                + (container.linkedPos == null ? "Not linked!" : container.linkedPos.toString()));
        linkedlabel.setCenterY(false);

        final UIEntity posLabel = new UIEntity();
        posLabel.setHeight(20);
        posLabel.setInheritWidth(true);
        posLabel.add(linkedlabel);

        inner.add(posLabel);

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