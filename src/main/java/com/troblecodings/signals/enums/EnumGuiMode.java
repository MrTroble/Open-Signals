package com.troblecodings.signals.enums;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.enums.SignalBoxIcons.SignalBoxSigns;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public enum EnumGuiMode {

    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), //
    CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), //
    END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }, PathwayModeType.END, 0), //
    PLATFORM(new float[] {
            0, 0.15f, 1, 0.15f
    }, PathwayModeType.NONE, 0, ConfigHandler.CLIENT.signalboxFreeColor.get(), 3), //
    BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), //
    HP(SignalBoxIcons.SIGNALS, 0, true, PathwayModeType.START_END, 2),
    VP(SignalBoxIcons.SIGNALS, 1, true, PathwayModeType.NONE, 1),
    RS(SignalBoxIcons.SIGNALS, 2, PathwayModeType.START_END,
            (state) -> state.equals(SignalState.RED) ? 1 : 3),
    RA10(SignalBoxIcons.SIGNS, SignalBoxSigns.RA10.ordinal(), PathwayModeType.END, 1),
    SH2(SignalBoxIcons.SIGNS, SignalBoxSigns.SH2.ordinal(), PathwayModeType.NONE, 3),
    IN_CONNECTION(SignalBoxIcons.SIGNS, SignalBoxSigns.ARROW_IN.ordinal(), PathwayModeType.START,
            1),
    OUT_CONNECTION(SignalBoxIcons.SIGNS, SignalBoxSigns.ARROW_OUT.ordinal(), PathwayModeType.END,
            1),
    ARROW(SignalBoxIcons.SIGNS, SignalBoxSigns.ARROW.ordinal(), PathwayModeType.END, 1),
    NE1(SignalBoxIcons.SIGNS, SignalBoxSigns.NE1.ordinal(), PathwayModeType.START_END, 1),
    NE5(SignalBoxIcons.SIGNS, SignalBoxSigns.NE5.ordinal(), PathwayModeType.START_END, 1),
    ZS3(SignalBoxIcons.SIGNS, SignalBoxSigns.ZS3.ordinal(), PathwayModeType.NONE, 1),
    TRAIN_NUMBER(new float[] {
            0, 0.5f, 2, 0.5f
    }, PathwayModeType.NONE, 2, ConfigHandler.CLIENT.signalboxTrainnumberBackgroundColor.get(), 6), //
    CROSSING(new float[] {
            0.5f, 0, 0.5f, 1, 0, 0.5f, 1, 0.5f
    });

    /**
     * Naming
     */

    public final Function<SignalState, BiConsumer<DrawInfo, Integer>> consumer;
    public final Function<SignalState, Integer> depthFunc;
    private int defaultColor;
    private final PathwayModeType type;

    private EnumGuiMode(final SignalBoxIcons icon, final int id, final PathwayModeType type,
            final int depth) {
        this((_u) -> ((info, c) -> info.drawTexture(icon.getResourceLocation(),
                UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH, icon.getX(id), 0,
                icon.getMX(id), 1)), type, (_u) -> depth);
    }

    private EnumGuiMode(final SignalBoxIcons icon, final int id, final PathwayModeType type,
            final Function<SignalState, Integer> depthFunc) {
        this((state) -> {
            final int factor = state.ordinal() < 3 ? (state.ordinal() * 3) : (6 + state.ordinal());
            return (info, c) -> info.drawTexture(icon.getResourceLocation(),
                    UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH,
                    icon.getX(id + factor), 0, icon.getMX(id + factor), 1);
        }, type, depthFunc);
    }

    private EnumGuiMode(final SignalBoxIcons icon, final int id, final boolean unused,
            final PathwayModeType type, final int depth) {
        this((state) -> {
            final int factor = state.ordinal() < 3 ? (state.ordinal() * 3) : (6 + state.ordinal());
            return (info, c) -> info.drawTexture(icon.getResourceLocation(),
                    UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH,
                    icon.getX(id + factor), 0, icon.getMX(id + factor), 1);
        }, type, (_u) -> depth);
    }

    private EnumGuiMode(final float[] array) {
        this(array, PathwayModeType.NONE, 0);
    }

    private EnumGuiMode(final float[] array, final PathwayModeType type, final int depth) {
        this(array, type, depth, ConfigHandler.CLIENT.signalboxFreeColor.get(), 2);
    }

    private EnumGuiMode(final float[] array, final PathwayModeType type, final int depth,
            final int color, final int width) {
        this((_u) -> {
            float[] currentArray = Arrays.copyOf(array, array.length);
            for (int i = 0; i < array.length; i++) {
                currentArray[i] *= UISignalBoxRendering.TILE_WIDTH;
            }
            return (info, c) -> info.lines(c, width, currentArray);
        }, type, (_u) -> depth);
        this.defaultColor = color;
    }

    private EnumGuiMode(final ResourceLocation location, final PathwayModeType type,
            final int depth) {
        this.consumer = (state) -> ((info, color) -> info.drawTexture(location,
                UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH, 0, 0, 1, 1));
        this.type = type;
        this.depthFunc = (_u) -> depth;
    }

    private EnumGuiMode(final Function<SignalState, BiConsumer<DrawInfo, Integer>> consumer,
            final PathwayModeType type, final Function<SignalState, Integer> depthFunc) {
        this.consumer = consumer;
        this.type = type;
        this.depthFunc = depthFunc;
    }

    public PathwayModeType getModeType() {
        return type;
    }

    public static EnumGuiMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public Rotation getLocalRotation(final Rotation rot) {
        if (!(this.equals(STRAIGHT) || this.equals(BUE)))
            return rot;
        return Rotation.values()[rot.ordinal() % 2];
    }
}