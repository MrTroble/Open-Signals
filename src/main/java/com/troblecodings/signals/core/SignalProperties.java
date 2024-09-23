package com.troblecodings.signals.core;

import java.util.List;

import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.properties.PredicatedPropertyBase.PredicateProperty;
import com.troblecodings.signals.properties.SoundProperty;

public class SignalProperties {

    public final Placementtool placementtool;
    public final float customNameRenderHeight;
    public final int defaultHeight;
    public final List<PredicateProperty<Integer>> signalHeights;
    public final List<PredicateProperty<Float>> customRenderHeights;
    public final float signWidth;
    public final float offsetX;
    public final float offsetY;
    public final float signScale;
    public final boolean autoscale;
    public final List<PredicateProperty<Boolean>> doubleSidedText;
    public final int textColor;
    public final boolean canLink;
    public final List<Integer> colors;
    public final List<SoundProperty> sounds;
    public final List<ValuePack> redstoneOutputs;
    public final List<ValuePack> redstoneOutputPacks;
    public final int defaultItemDamage;
    public final boolean isBridgeSignal;

    public SignalProperties(final Placementtool placementtool, final float customNameRenderHeight,
            final int height, final List<PredicateProperty<Integer>> signalHeights,
            final float signWidth, final float offsetX, final float offsetY, final float signScale,
            final boolean autoscale, final List<PredicateProperty<Boolean>> doubleSidedText,
            final int textColor, final boolean canLink, final List<Integer> colors,
            final List<PredicateProperty<Float>> renderheights, final List<SoundProperty> sounds,
            final List<ValuePack> redstoneOutputs, final int defaultItemDamage,
            final List<ValuePack> redstoneOutputPacks, final boolean isBridgeSignal) {
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
        this.redstoneOutputPacks = redstoneOutputPacks;
        this.isBridgeSignal = isBridgeSignal;
    }
}