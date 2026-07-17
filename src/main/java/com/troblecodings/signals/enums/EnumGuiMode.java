package com.troblecodings.signals.enums;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.guis.UISignalBoxIcons;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.SignalBoxUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public enum EnumGuiMode {

    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }, PathwayModeType.END, 0), PLATFORM(new float[] {
            0, 0.15f, 1, 0.15f
    }, PathwayModeType.NONE, 0, SignalBoxUtil.FREE_COLOR, 3), BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), HP(0, true, PathwayModeType.START_END, 2), VP(1, true, PathwayModeType.NONE, 1),
    RS(2, true, PathwayModeType.START_END, (state) -> state.equals(SignalState.RED) ? 1 : 3),
    RA10(3, PathwayModeType.END, 1), SH2(4, PathwayModeType.NONE, 3),
    IN_CONNECTION(UISignalBoxIcons.INCOMING_ICON, PathwayModeType.START, 1),
    OUT_CONNECTION(UISignalBoxIcons.OUTGOING_ICON, PathwayModeType.END, 1),
    ARROW(UISignalBoxIcons.ARROW_ICON, PathwayModeType.END, 1),
    NE1(UISignalBoxIcons.NE1_ICON, PathwayModeType.START_END, 1),
    NE5(UISignalBoxIcons.NE5_ICON, PathwayModeType.START_END, 1),
    ZS3(UISignalBoxIcons.ZS3_ICON, PathwayModeType.NONE, 1), TRAIN_NUMBER(new float[] {
            0, 0.5f, 2, 0.5f
    }, PathwayModeType.NONE, 2, ConfigHandler.CLIENT.signalboxTrainnumberBackgroundColor.get(), 6),
    CROSSING(new float[] {
            0.5f, 0, 0.5f, 1, 0, 0.5f, 1, 0.5f
    });

    public final Function<SignalState, BiConsumer<DrawInfo, Integer>> consumer;
    public final Function<SignalState, Integer> depthFunc;
    private int defaultColor;
    private final PathwayModeType type;

    private EnumGuiMode(final int id, final PathwayModeType type, final int depth) {
        this((_u) -> ((info, c) -> info.drawTexture(UISignalBoxIcons.ICON,
                UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH, id * 0.2, 0,
                id * 0.2 + 0.2, 0.5)), type, (_u) -> depth);
    }

    private EnumGuiMode(final int id, final boolean unused, final PathwayModeType type,
            final Function<SignalState, Integer> depthFunc) {
        this((state) -> {
            final int factor = state.ordinal() < 3 ? (state.ordinal() * 3) : (6 + state.ordinal());
            return (info, c) -> info.drawTexture(UISignalBoxIcons.SIGNALS,
                    UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH,
                    (id + factor) * 0.0666667f, 0.0f, (id + factor) * 0.066667f + 0.06f, 1.0f);
        }, type, depthFunc);
    }

    private EnumGuiMode(final int id, final boolean unused, final PathwayModeType type,
            final int depth) {
        this((state) -> {
            final int factor = state.ordinal() < 3 ? (state.ordinal() * 3) : (6 + state.ordinal());
            return (info, c) -> info.drawTexture(UISignalBoxIcons.SIGNALS,
                    UISignalBoxRendering.TILE_WIDTH, UISignalBoxRendering.TILE_WIDTH,
                    (id + factor) * 0.0666667f, 0.0f, (id + factor) * 0.066667f + 0.06f, 1.0f);
        }, type, (_u) -> depth);
    }

    private EnumGuiMode(final float[] array) {
        this(array, PathwayModeType.NONE, 0);
    }

    private EnumGuiMode(final float[] array, final PathwayModeType type, final int depth) {
        this(array, type, depth, SignalBoxUtil.FREE_COLOR, 2);
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