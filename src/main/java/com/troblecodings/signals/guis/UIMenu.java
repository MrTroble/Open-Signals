package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIComponentEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.KeyEvent;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.util.Rotation;

public class UIMenu extends UIComponentEntity {

    private final Map<EnumGuiMode, UIEntity> modeForEntity = new HashMap<>();

    private int selection = 0;
    private int rotation = 0;
    private BiConsumer<Integer, Integer> consumer = (i1, i2) -> {
    };

    public UIMenu() {
        super(new UIEntity());
        entity.setHeight(20);
        entity.setWidth(22 * EnumGuiMode.values().length);
        entity.setX(2);
        entity.setY(2);
        entity.add(new UIBox(UIBox.HBOX, 2));
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
                preview.add(new UIBorder(0xFF00FF00, 1));
            preview.add(new UIClickable(e -> updateSelection(mode)));
            entity.add(preview);
            modeForEntity.put(mode, preview);
        }
    }

    private void updateSelection(final EnumGuiMode newMode) {
        final UIEntity previousEntity = modeForEntity.get(EnumGuiMode.values()[selection]);
        if (previousEntity != null) {
            previousEntity.findRecursive(UIBorder.class).forEach(previousEntity::remove);
        }
        final UIEntity newEntity = modeForEntity.get(newMode);
        newEntity.add(new UIBorder(0xFF00FF00, 1));
        this.selection = newMode.ordinal();
        consumer.accept(selection, rotation);
    }

    @Override
    public void update() {
        this.entity.onAdd(this.getParent());
        super.update();
    }

    public int getSelection() {
        return selection;
    }

    public void setConsumer(final BiConsumer<Integer, Integer> consumer) {
        this.consumer = consumer;
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
