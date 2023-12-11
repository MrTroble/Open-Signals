package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIComponentEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.UpdateEvent;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.MainSignalIdentifier;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public class UISignalBoxTile extends UIComponentEntity {

    public static final ResourceLocation ICON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/symbols.png");
    public static final ResourceLocation ARROW_ICON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/connection.png");
    public static final ResourceLocation SIGNALS = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/signals.png");

    private SignalBoxNode node;
    private final Map<ModeSet, UIEntity> setToEntity = new HashMap<>();
    private final Map<ModeSet, MainSignalIdentifier> greenSignals = new HashMap<>();

    public UISignalBoxTile(final SignalBoxNode node) {
        this(node, new ArrayList<>());
    }

    public UISignalBoxTile(final SignalBoxNode node, final List<MainSignalIdentifier> list) {
        super(new UIEntity());
        list.forEach(mode -> greenSignals.put(mode.getModeSet(), mode));
        this.node = node;
        if (this.node != null)
            this.node.forEach(this::localAdd);
    }

    public void setGreenSignals(final List<MainSignalIdentifier> list) {
        greenSignals.clear();
        list.forEach(identifier -> {
            greenSignals.put(identifier.getModeSet(), identifier);
            updateModeSet(identifier.getModeSet());
        });
    }

    public void updateModeSet(final ModeSet mode) {
        localRemove(mode);
        localAdd(mode);
        update();
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
            rotation.setRotateZ(modeSet.rotation.ordinal() * ((float) 90));
            entity.add(rotation);
        }
        entity.add(new UIIndependentTranslate(0, 0, modeSet.mode.depth));

        final MainSignalIdentifier identifier = greenSignals.get(modeSet);
        SignalState state = identifier != null ? identifier.state : SignalState.RED;
        entity.add((UIComponent) modeSet.mode.consumer.get(state));
        this.entity.add(entity);
        setToEntity.put(modeSet, entity);
        this.entity.setVisible(!setToEntity.isEmpty());
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
        this.entity.setVisible(!setToEntity.isEmpty());
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

    public boolean isValidEnd() {
        return this.node.isValidEnd();
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
        entity.findRecursive(UILines.class).forEach(lines -> lines.setColor(color));
    }
}