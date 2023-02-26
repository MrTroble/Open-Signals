package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.guilib.ecs.entitys.UIComponentEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
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
            rotation.setRotateZ(modeSet.rotation.ordinal() * 90);
            entity.add(rotation);
        }
        entity.add(modeSet.mode.consumer.get());
        this.entity.add(entity);
        setToEntity.put(modeSet, entity);
    }

    private void localRemove(final ModeSet modeSet) {
        this.entity.remove(setToEntity.remove(modeSet));
    }

    public void add(final ModeSet modeSet) {
        this.node.add(modeSet);
        localAdd(modeSet);
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
}
