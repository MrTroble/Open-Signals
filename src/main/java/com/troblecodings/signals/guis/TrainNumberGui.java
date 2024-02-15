package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.guis.TrainNumberContainer.TrainNumberNetwork;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.world.entity.player.Player;

public class TrainNumberGui extends GuiBase {

    private final TrainNumberContainer container;
    private final Player player;

    public TrainNumberGui(final GuiInfo info) {
        super(info);
        this.container = (TrainNumberContainer) info.base;
        this.player = info.player;
        this.entity.clear();
        this.entity.add(new UILabel("Not connected"));
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
                    initializeGrid(grid);
                    push(GuiElements.createScreen(screen -> screen.add(grid)));
                });
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

    private void initializeGrid(final UIEntity lowerEntity) {
        final UIEntity splitter = new UIEntity();
        splitter.setInherits(true);
        final UIEntity plane = new UIEntity();
        plane.setWidth(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
        plane.setHeight(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
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
        plane.add(new UIBox(UIBox.VBOX, 0).setPageable(false));
        for (int x = 0; x < GuiSignalBox.TILE_COUNT; x++) {
            final UIEntity row = new UIEntity();
            row.add(new UIBox(UIBox.HBOX, 0).setPageable(false));
            row.setHeight(GuiSignalBox.TILE_WIDTH);
            row.setWidth(GuiSignalBox.TILE_WIDTH);
            for (int y = 0; y < GuiSignalBox.TILE_COUNT; y++) {
                final UIEntity tile = new UIEntity();
                tile.setHeight(GuiSignalBox.TILE_WIDTH);
                tile.setWidth(GuiSignalBox.TILE_WIDTH);
                final Point name = new Point(y, x);
                SignalBoxNode node = container.grid.getNode(name);
                if (node == null) {
                    node = new SignalBoxNode(name);
                }
                final UISignalBoxTile sbt = new UISignalBoxTile(node);
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
                final SignalBoxNode finalNode = node;
                tile.add(new UIClickable(e -> {
                    if (finalNode.isEmpty())
                        return;
                    container.setPoint = name;
                    sendNewPoint();
                    pop();
                }));
                if (name.equals(container.setPoint)) {
                    tile.add(new UIColor(GuiSignalBox.SELECTION_COLOR));
                }
                row.add(tile);
            }
            plane.add(row);
        }
        splitter.add(new UIScissor());
        splitter.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        splitter.add(new UIBorder(0xFF000000, 4));
        splitter.add(plane);
        lowerEntity.add(new UIBox(UIBox.HBOX, 2));
        lowerEntity.add(splitter);
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
