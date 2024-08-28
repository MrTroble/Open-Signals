package com.troblecodings.signals.guis;

import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.TrainNumber;

import net.minecraft.client.renderer.GlStateManager;

public class UITrainNumber extends UIComponent {

    public static final int TRAIN_NUMBER_TEXT = ConfigHandler.signalboxTrainnumberColor;

    private TrainNumber number;

    public void setTrainNumber(final TrainNumber number) {
        this.number = number;
    }

    @Override
    public void draw(final DrawInfo info) {
    }

    @Override
    public void update() {
    }

    @Override
    public void postDraw(final DrawInfo info) {
        if (this.isVisible() && !number.trainNumber.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 5);
            final UIEntity trainNumberEntity = new UIEntity();
            trainNumberEntity.setX(parent.getWorldX());
            trainNumberEntity.setY(parent.getWorldY() + 1);
            trainNumberEntity.setHeight(20);
            trainNumberEntity.add(new UIScale(0.45f, 0.45f, 0.45f));
            final UILabel label = new UILabel(number.trainNumber);
            label.setTextColor(TRAIN_NUMBER_TEXT);
            trainNumberEntity.add(label);
            trainNumberEntity.updateEvent(parent.getLastUpdateEvent());
            trainNumberEntity.draw(info);
            GlStateManager.popMatrix();
        }
    }
}