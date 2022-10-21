package com.troblecodings.signals.guis;

import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIComponent;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UIEntity.KeyEvent;
import eu.gir.guilib.ecs.entitys.UIEntity.MouseEvent;
import eu.gir.guilib.ecs.entitys.render.UIBorder;
import eu.gir.guilib.ecs.entitys.render.UIColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Rotation;

public class UIMenu extends UIComponent {

    private int mX, mY, selection = 0, rotation = 0;

    @Override
    public void draw(final int mouseX, final int mouseY) {
    }

    @Override
    public void update() {
    }

    @Override
    public void postDraw(final int mouseX, final int mouseY) {
        if (this.isVisible()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 5);
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
                final UISignalBoxTile sbt = new UISignalBoxTile(node);
                preview.add(sbt);
                preview.setHeight(20);
                preview.setWidth(20);
                if (mode.ordinal() == this.selection)
                    preview.add(new UIBorder(0xFF00FF00, 4));
                selection.add(preview);
            }
            selection.updateEvent(parent.getLastUpdateEvent());
            selection.draw(mouseX, mouseY);
            GlStateManager.popMatrix();
        }
    }

    public int getSelection() {
        return selection;
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
        if (event.typed == 'r') {
            this.rotation++;
            if (this.rotation >= Rotation.values().length)
                this.rotation = 0;
        }
    }

    public int getRotation() {
        return rotation;
    }
}
