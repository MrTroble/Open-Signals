package com.troblecodings.signals.core;

import java.util.List;

import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.properties.BooleanProperty;
import com.troblecodings.signals.properties.FloatProperty;
import com.troblecodings.signals.properties.HeightProperty;
import com.troblecodings.signals.properties.SoundProperty;

public class SignalProperties {

    public final Placementtool placementtool;
    public final float customNameRenderHeight;
    public final int defaultHeight;
    public final List<HeightProperty> signalHeights;
    public final List<FloatProperty> customRenderHeights;
    public final float signWidth;
    public final float offsetX;
    public final float offsetY;
    public final float signScale;
    public final boolean autoscale;
    public final List<BooleanProperty> doubleSidedText;
    public final int textColor;
    public final boolean canLink;
    public final List<Integer> colors;
    public final List<SoundProperty> sounds;
    public final List<Object> redstoneOutputs;
    public final int defaultItemDamage;

    public SignalProperties(final Placementtool placementtool, final float customNameRenderHeight,
            final int height, final List<HeightProperty> signalHeights, final float signWidth,
            final float offsetX, final float offsetY, final float signScale,
            final boolean autoscale, final List<BooleanProperty> doubleSidedText,
            final int textColor, final boolean canLink, final List<Integer> colors,
            final List<FloatProperty> renderheights, final List<SoundProperty> sounds,
            final List<Object> redstoneOutputs, final int defaultItemDamage) {
        this.placementtool = placementtool;
        this.customNameRenderHeight = customNameRenderHeight;
        this.defaultHeight = height;
        this.signWidth = signWidth;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.signScale = signScale;
        this.autoscale = autoscale;
        this.doubleSidedText = doubleSidedText;
        this.textColor = textColor;
        this.canLink = canLink;
        this.colors = colors;
        this.signalHeights = signalHeights;
        this.customRenderHeights = renderheights;
        this.sounds = sounds;
        this.redstoneOutputs = redstoneOutputs;
        this.defaultItemDamage = defaultItemDamage;
    }
}