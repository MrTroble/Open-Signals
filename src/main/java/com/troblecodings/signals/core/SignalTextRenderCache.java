package com.troblecodings.signals.core;

public class SignalTextRenderCache {
    private String lastName = "";
    private float customRenderHeight;
    private boolean doubleSidedText;
    private String[] splitNames;
    private float[] nameWidths;
    private int textColor;
    private float scale;

    public SignalTextRenderCache() {
    }

    public void update(final String currentName, final float customRenderHeight,
            final boolean doubleSidedText, final String[] splitNames, final float[] nameWidths,
            final int textColor, final float scale) {
        this.lastName = currentName;
        this.customRenderHeight = customRenderHeight;
        this.doubleSidedText = doubleSidedText;
        this.splitNames = splitNames;
        this.nameWidths = nameWidths;
        this.scale = scale;
        this.textColor = textColor;
    }

    public String getLastName() {
        return lastName;
    }

    public float getCustomRenderHeight() {
        return customRenderHeight;
    }

    public boolean getDoubleSidedText() {
        return doubleSidedText;
    }

    public String[] getSplitNames() {
        return splitNames;
    }

    public float[] getNameWidth() {
        return nameWidths;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getScale() {
        return scale;
    }

}
