package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIComponentEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.UpdateEvent;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class UISignalBoxTile extends UIComponentEntity {

    public static final ResourceLocation ICON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/symbols.png");

    private SignalBoxNode node;
    private final Map<ModeSet, UIEntity> setToEntity = new HashMap<>();

    public UISignalBoxTile(final SignalBoxNode node) {
        super(new UIEntity());
        this.node = node;
        if (this.node != null)
            this.node.forEach(this::localAdd);
    }

    public void setNode(final SignalBoxNode node) {
        if (this.node != null)
            this.node.forEach(this::localRemove);
        this.node = node;
        if (this.node != null)
            this.node.forEach(this::localAdd);
    }

    private void localAdd(final ModeSet modeSet) {
        final UIEntity entity = new UIEntity();
        if (!modeSet.rotation.equals(Rotation.NONE)) {
            final UIRotate rotation = new UIRotate();
            rotation.setRotateZ(modeSet.rotation.ordinal() * ((float) Math.PI / 2.0f));
            entity.add(rotation);
        }
        entity.add(new UIIndependentTranslate(0, 0, 1));
        entity.add((UIComponent) modeSet.mode.consumer.get());
        this.entity.add(entity);
        setToEntity.put(modeSet, entity);
    }

    @Override
    public void update() {
        super.update();
        this.entity.setX(parent.getWidth() / 2.0);
        this.entity.setY(parent.getHeight() / 2.0);
        setToEntity.values().forEach(e -> {
            e.setHeight(entity.getHeight());
            e.setWidth(entity.getWidth());
            e.update();
        });
        this.entity.findRecursive(UIIndependentTranslate.class).forEach(translate -> {
            translate.setX(-this.entity.getX());
            translate.setY(-this.entity.getY());
        });
    }

    @Override
    public void updateEvent(final UpdateEvent event) {
        super.updateEvent(event);
        this.update();
    }

    private void localRemove(final ModeSet modeSet) {
        this.entity.remove(setToEntity.remove(modeSet));
    }

    public void add(final ModeSet modeSet) {
        this.node.add(modeSet);
        localAdd(modeSet);
        this.update();
    }

    public boolean has(final ModeSet modeSet) {
        return this.node.has(modeSet);
    }

    public void remove(final ModeSet modeSet) {
        this.node.remove(modeSet);
        this.localRemove(modeSet);
    }

    public boolean isValidStart() {
        return this.node.isValidStart();
    }

    public Point getPoint() {
        return this.node.getPoint();
    }

    public SignalBoxNode getNode() {
        return this.node;
    }

    public void setColor(final ModeSet mode, final int color) {
        final UIEntity entity = setToEntity.get(mode);
        entity.findRecursive(UIColor.class).forEach(colors -> {
            entity.remove(colors);
            colors.setColor(color);
            entity.add(colors);
        });
    }
}