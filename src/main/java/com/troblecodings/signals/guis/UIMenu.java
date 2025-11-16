package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIComponentEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.KeyEvent;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UIReentrantScissor;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.signals.enums.EnumGuiMode;

import net.minecraft.util.Rotation;

public class UIMenu extends UIComponentEntity {

    public static final int BACKGROUND_COLOR = 0xFFAFAFAF;
    public static final int HIGHLIGHT_COLOR = 0x45339933;

    private final Map<EnumGuiMode, UIEntity> modeForEntity = new HashMap<>();

    private int selection = 0;
    private int rotation = 0;
    private BiConsumer<Integer, Integer> consumer = (i1, i2) -> {
    };

    public UIMenu() {
        super(new UIEntity());
        entity.setInheritWidth(true);
        entity.setX(2);
        entity.setY(2);
        entity.add(new UIBox(UIBox.VBOX, 0));
        entity.add(new UIScissor());

        final UIEntity list = new UIEntity();
        entity.add(list);
        list.setInheritWidth(true);
        list.setHeight(21);

        final UIScrollBox scrollbox = new UIScrollBox(UIBox.HBOX, 2);
        list.add(scrollbox);
        for (final EnumGuiMode mode : EnumGuiMode.values()) {
            final UIEntity preview = new UIEntity();
            preview.add(new UIColor(BACKGROUND_COLOR));
            preview.add(new UIReentrantScissor());

            final UIComponent sbt = SidePanel.fromEnum(mode.ordinal(), rotation, 1.95f);
            preview.add(sbt);
            preview.setHeight(20);
            preview.setWidth(20);
            preview.add(new UIClickable(e -> updateSelection(mode)));
            if (mode.ordinal() == this.selection) {
                preview.add(new UIColor(HIGHLIGHT_COLOR));
            }
            list.add(preview);
            modeForEntity.put(mode, preview);
        }
        final UIScroll scroll = new UIScroll();
        final UIEntity scrollBar = GuiElements.createScrollBar(scrollbox, 10, scroll);
        scrollbox.setConsumer(i -> {
        });
        entity.add(scroll);
        entity.add(scrollBar);
    }

    private void updateSelection(final EnumGuiMode newMode) {
        final UIEntity previousEntity = modeForEntity.get(EnumGuiMode.values()[selection]);
        if (previousEntity != null) {
            previousEntity.findRecursive(UIColor.class).forEach(c -> {
                if (c.getColor() == HIGHLIGHT_COLOR)
                    previousEntity.remove(c);
            });
        }
        final UIEntity newEntity = modeForEntity.get(newMode);
        newEntity.add(new UIColor(HIGHLIGHT_COLOR));
        this.selection = newMode.ordinal();
        consumer.accept(selection, rotation);
    }

    public int getSelection() {
        return selection;
    }

    public void setConsumer(final BiConsumer<Integer, Integer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void update() {
        this.entity.setHeight(this.parent.getHeight());
        this.entity.setWidth(this.parent.getWidth() - 4);
        this.entity.update();
    }

    @Override
    public void onAdd(UIEntity entity) {
        super.onAdd(entity);
        this.entity.onAdd(entity);
        this.entity.updateEvent(entity.getLastUpdateEvent());
    }

    @Override
    public void keyEvent(final KeyEvent event) {
        super.keyEvent(event);
        if (event.typedChar == 'R' || event.typedChar == 'r') {
            this.rotation++;
            if (this.rotation >= Rotation.values().length) {
                this.rotation = 0;
            }
            consumer.accept(selection, rotation);
        }
    }

    public int getRotation() {
        return rotation;
    }
}
