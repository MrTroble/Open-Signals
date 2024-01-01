package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.QuaternionWrapper;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.models.ModelInfoWrapper;

public class PreviewSideBar {
    public static final float MODIFIER = 0.1f;

    private final UIBlockRender blockRender;
    private final Map<SEProperty, String> properties = new HashMap<>();
    private final UIEntity blockRenderEntity = new UIEntity();

    public PreviewSideBar(final float height) {
        blockRender = new UIBlockRender(20, height);
        blockRenderEntity.setInheritHeight(true);
        blockRenderEntity.setWidth(60);

        blockRenderEntity.add(new UIDrag((x, y) -> blockRender
                .updateRotation(QuaternionWrapper.fromXYZ(0, (float) x * MODIFIER, 0))));

        blockRenderEntity.add(new UIScissor());
        blockRenderEntity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        blockRenderEntity.add(blockRender);
    }

    public UIEntity get() {
        return blockRenderEntity;
    }

    public void addToRenderList(final SEProperty property, final int valueId) {
        if (valueId < 0) {
            properties.remove(property);
            return;
        }
        if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
            properties.put(property, property.getObjFromID(valueId));
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
            if (valueId > 0) {
                properties.put(property, property.getDefault());
            } else {
                properties.remove(property);
            }
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
            properties.put(property, property.getDefault());
        }
    }

    public void addToRenderNormal(final SEProperty property, final int valueId) {
        if (valueId < 0) {
            properties.put(property, property.getDefault());
            return;
        }
        properties.put(property, property.getObjFromID(valueId));
    }

    public void clear() {
        properties.clear();
    }

    public void update(final Signal signal) {
        blockRender.setBlockState(signal.defaultBlockState(), new ModelInfoWrapper(properties));
    }
}
