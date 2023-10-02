package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.world.entity.player.Player;

public class PathwayRequesterGui extends GuiBase {

    private final PathwayRequesterContainer container;
    private final Player player;

    public PathwayRequesterGui(GuiInfo info) {
        super(info);
        this.container = (PathwayRequesterContainer) info.base;
        this.player = info.player;
    }

    private void initOwn() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setInheritHeight(true);
        inner.setInheritWidth(true);
        inner.add(new UIBox(UIBox.VBOX, 2));
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(inner);
        this.entity.add(GuiElements.createSpacerH(10));

        final UITextInput startX = new UITextInput(container.start.getX() + "");
        final UITextInput startY = new UITextInput(container.start.getY() + "");
        final UITextInput endX = new UITextInput(container.end.getX() + "");
        final UITextInput endY = new UITextInput(container.end.getY() + "");

        final UIEntity startXEntity = new UIEntity();
        startXEntity.setHeight(20);
        startXEntity.setInheritWidth(true);
        startXEntity.add(startX);

        final UIEntity startYEntity = new UIEntity();
        startYEntity.setHeight(20);
        startYEntity.setInheritWidth(true);
        startYEntity.add(startY);

        final UIEntity endXEntity = new UIEntity();
        endXEntity.setHeight(20);
        endXEntity.setInheritWidth(true);
        endXEntity.add(endX);

        final UIEntity endYEntity = new UIEntity();
        endYEntity.setHeight(20);
        endYEntity.setInheritWidth(true);
        endYEntity.add(endY);

        final UILabel startLabel = new UILabel("Start: " + container.start.toShortString());
        startLabel.setCenterY(false);
        final UILabel endLabel = new UILabel("End: " + container.end.toShortString());
        endLabel.setCenterY(false);

        final UIEntity startHbox = new UIEntity();
        startHbox.add(new UIBox(UIBox.HBOX, 2));
        startHbox.setHeight(25);
        startHbox.setInheritWidth(true);

        startHbox.add(GuiElements.createLabel("Start X:"));
        startHbox.add(startXEntity);
        startHbox.add(GuiElements.createLabel("Start Y:"));
        startHbox.add(startYEntity);
        startHbox.add(GuiElements.createButton(I18Wrapper.format("btn.parse"), e -> {
            try {
                final Point newStart = new Point(Integer.parseInt(startX.getText()),
                        Integer.parseInt(startY.getText()));
                container.start = newStart;
                startLabel.setText("Start: " + newStart.toShortString());
            } catch (Exception e2) {
                startLabel.setText(I18Wrapper.format("btn.invalid"));
            }
        }));
        inner.add(startHbox);

        final UIEntity endHbox = new UIEntity();
        endHbox.add(new UIBox(UIBox.HBOX, 2));
        endHbox.setHeight(25);
        endHbox.setInheritWidth(true);

        endHbox.add(GuiElements.createLabel("End X:"));
        endHbox.add(endXEntity);
        endHbox.add(GuiElements.createLabel("End Y:"));
        endHbox.add(endYEntity);
        endHbox.add(GuiElements.createButton(I18Wrapper.format("btn.parse"), e -> {
            try {
                final Point newEnd = new Point(Integer.parseInt(endX.getText()),
                        Integer.parseInt(endY.getText()));
                container.end = newEnd;
                endLabel.setText("End: " + newEnd.toShortString());
            } catch (Exception e2) {
                endLabel.setText(I18Wrapper.format("btn.invalid"));
            }
        }));
        inner.add(endHbox);
        inner.add(GuiElements.createSpacerV(5));

        final UIEntity startLabelEntity = new UIEntity();
        startLabelEntity.setHeight(20);
        startLabelEntity.setInheritWidth(true);
        startLabelEntity.add(startLabel);

        final UIEntity endLabelEntity = new UIEntity();
        endLabelEntity.setHeight(20);
        endLabelEntity.setInheritWidth(true);
        endLabelEntity.add(endLabel);

        inner.add(startLabelEntity);
        inner.add(endLabelEntity);

        inner.add(GuiElements.createButton(I18Wrapper.format("btn.save"), e -> sendToServer()));
        inner.add(GuiElements.createSpacerV(5));

        final UILabel label = new UILabel("Linked SignalBox: "
                + (container.linkedPos == null ? "Not linked!" : container.linkedPos.toString()));
        label.setCenterY(false);

        final UIEntity posLabel = new UIEntity();
        posLabel.setHeight(20);
        posLabel.setInheritWidth(true);
        posLabel.add(label);

        inner.add(posLabel);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

    private void sendToServer() {
        final WriteBuffer buffer = new WriteBuffer();
        container.start.writeNetwork(buffer);
        container.end.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

}
