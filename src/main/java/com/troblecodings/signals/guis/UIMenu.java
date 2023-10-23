package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.KeyEvent;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.world.level.block.Rotation;

public class UIMenu extends UIComponent {

    private double mX;
    private double mY;
    private int selection = 0;
    private int rotation = 0;
    private BiConsumer<Integer, Integer> consumer = (i1, i2) -> {
    };

    @Override
    public void draw(final DrawInfo info) {
    }

    @Override
    public void update() {
    }

    @Override
    public void postDraw(final DrawInfo info) {
        if (this.isVisible()) {
            info.stack.pushPose();
            info.stack.translate(0, 0, 5);
            final UIEntity selection = new UIEntity();
            selection.setX(mX);
            selection.setY(mY);
            selection.setHeight(20);
            selection.setWidth(22 * EnumGuiMode.values().length);
            selection.add(new UIBox(UIBox.HBOX, 2));
            for (final EnumGuiMode mode : EnumGuiMode.values()) {
                final UIEntity preview = new UIEntity();
                preview.add(new UIColor(0xFFAFAFAF));
                final SignalBoxNode node = new SignalBoxNode(new Point(-1, -1));
                node.add(new ModeSet(mode, Rotation.values()[this.rotation]));
                final UISignalBoxTile sbt = new UISignalBoxTile(node, new ArrayList<>());
                preview.add(sbt);
                preview.setHeight(20);
                preview.setWidth(20);
                if (mode.ordinal() == this.selection)
                    preview.add(new UIBorder(0xFF00FF00, 1));
                selection.add(preview);
            }
            selection.updateEvent(parent.getLastUpdateEvent());
            selection.draw(info);
            info.stack.popPose();
        }
    }

    public int getSelection() {
        return selection;
    }

    public void setConsumer(final BiConsumer<Integer, Integer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void mouseEvent(final MouseEvent event) {
        switch (event.state) {
            case CLICKED:
                if (event.key != 1)
                    return;
                if (!this.isVisible()) {
                    this.mX = event.x;
                    this.mY = event.y;
                }
                this.selection = Math.max(0, Math.min(EnumGuiMode.values().length - 1,
                        (int) ((event.x - this.mX) / 22.0f)));
                consumer.accept(selection, rotation);
                this.setVisible(true);
                break;
            case RELEASE:
                this.setVisible(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyEvent(final KeyEvent event) {
        super.keyEvent(event);
        if (event.typedChar == 'R' || event.typedChar == 'r') {
            this.rotation++;
            if (this.rotation >= Rotation.values().length)
                this.rotation = 0;
            consumer.accept(selection, rotation);
        }
    }

    public int getRotation() {
        return rotation;
    }
}