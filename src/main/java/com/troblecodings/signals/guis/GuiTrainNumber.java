package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;

public class GuiTrainNumber extends GuiBase {

    private final ContainerTrainNumber container;

    public GuiTrainNumber(final GuiInfo info) {
        super(info);
        this.container = (ContainerTrainNumber) info.base;
        this.entity.clear();
        this.entity.add(new UILabel(I18Wrapper.format("gui.notconnected")));
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

        final UIEntity label = GuiElements.createLabel(
                I18Wrapper.format("block." + OpenSignalsMain.MODID + ".trainnumberblock"),
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
        textInput.setOnTextUpdate(container::sendNewTrainNumber);
        final UIEntity input = new UIEntity();
        input.add(textInput);
        input.setHeight(20);
        input.setWidth(150);
        inputEntity.add(input);
        inputEntity.add(GuiElements.createSpacerH(5));
        inputEntity.add(
                GuiElements.createButton(I18Wrapper.format("gui.trainnumber.change_set"), e -> {
                    container.setTrainNumber();
                    textInput.setText("");
                }));
        inputEntity.add(new UIToolTip(I18Wrapper.format("gui.trainnumber.info.change")));
        inner.add(inputEntity);

        final UIEntity changeButton =
                GuiElements.createButton(I18Wrapper.format("gui.trainnumber.setpoint"),
                        e -> push(GuiElements.createScreen(screen -> {
                            final BoxEntity entitys = UISignalBoxRendering.createSignalBoxEntity(
                                    container.grid, false, (rendering, point, mouseKey) -> {
                                        if (mouseKey != MouseEvent.LEFT_MOUSE)
                                            return;
                                        container.grid.getNodeChecked(point).ifPresent(node -> {
                                            if (node.isEmpty())
                                                return;
                                            rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
                                                    point, SelectionType.FIRST);
                                            container.selectedPoint = point;
                                            container.sendNewPoint();
                                        });
                                    });
                            if (container.selectedPoint != null) {
                                entitys.rendering.addSelection(GuiSignalBox.SELECTION_COLOR,
                                        container.selectedPoint, SelectionType.FIRST);
                            }
                            screen.add(entitys.entity);

                            final UIEntity lowerEntity = new UIEntity();
                            lowerEntity.add(new UIBox(UIBox.HBOX, 5));
                            lowerEntity.setHeight(15);
                            lowerEntity.setInheritWidth(true);

                            final UIEntity backButtonEntity = new UIEntity();
                            backButtonEntity.setHeight(15);
                            backButtonEntity.setWidth(20);
                            backButtonEntity.add(GuiElements.createButton("<", 20,
                                    u -> u.getLastUpdateEvent().base.pop()));

                            lowerEntity.add(backButtonEntity);

                            final UIEntity labelEntity = new UIEntity();
                            labelEntity.setHeight(15);
                            labelEntity.setInheritWidth(true);
                            labelEntity.add(new UILabel(
                                    I18Wrapper.format("gui.trainnumber.info.select_point"),
                                    lowerEntity.getInfoTextColor()));

                            lowerEntity.add(labelEntity);

                            screen.add(lowerEntity);
                        })));
        changeButton.add(new UIToolTip(I18Wrapper.format("gui.trainnumber.setpoint.desc")));
        inner.add(changeButton);

        inner.add(GuiElements.createSpacerV(5));

        inner.add(GuiElements
                .createLabel("Linked SignalBox: " + (container.linkedPos == null ? "Not linked!"
                        : container.linkedPos.toShortString()), 1.2f));

        entity.add(inner);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

}
