package com.troblecodings.signals.enums;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.guis.UISignalBoxTile;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.SignalBoxUtil;

import net.minecraft.resources.ResourceLocation;

public enum EnumGuiMode {
	STRAIGHT(new float[] { 0, 0.5f, 1, 0.5f }), CORNER(new float[] { 0, 0.5f, 0.5f, 1 }),
	END(new float[] { 0.9f, 0.2f, 0.9f, 0.8f }, PathwayModeType.END, 0),
	PLATFORM(new float[] { 0, 0.15f, 1, 0.15f }, PathwayModeType.NONE, 0, SignalBoxUtil.FREE_COLOR, 3),
	BUE(new float[] { 0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1 }), HP(0, true, PathwayModeType.START_END, 2),
	VP(1, true, PathwayModeType.NONE, 1), RS(2, true, PathwayModeType.START_END, 1), RA10(3, PathwayModeType.END, 1),
	SH2(4, PathwayModeType.NONE, 1), IN_CONNECTION(UISignalBoxTile.INCOMING_ICON, PathwayModeType.START, 1),
	OUT_CONNECTION(UISignalBoxTile.OUTGOING_ICON, PathwayModeType.END, 1),
	ARROW(UISignalBoxTile.ARROW_ICON, PathwayModeType.END, 1),
	NE1(UISignalBoxTile.NE1_ICON, PathwayModeType.START_END, 1),
	NE5(UISignalBoxTile.NE5_ICON, PathwayModeType.START_END, 1), ZS3(UISignalBoxTile.ZS3_ICON, PathwayModeType.NONE, 1),
	TRAIN_NUMBER(new float[] { 0, 0.5f, 2, 0.5f }, PathwayModeType.NONE, 2, 6, GuiSignalBox.TRAIN_NUMBER_BACKGROUND_COLOR),
	CROSSING(new float[] { 0.5f, 0, 0.5f, 1, 0, 0.5f, 1, 0.5f });

	/**
	 * Naming
	 */

	public final Function<SignalState, Consumer<DrawInfo>> consumer;
	public final int translation;
	private final PathwayModeType type;

	private EnumGuiMode(final int id, final PathwayModeType type, final int translation) {
		this((_u) -> ((info) -> UITexture.drawTexture(info, UISignalBoxTile.ICON, UISignalBoxRendering.TILE_WIDTH,
				UISignalBoxRendering.TILE_WIDTH, id * 0.2, 0, id * 0.2 + 0.2, 0.5)), type, translation);
	}

	private EnumGuiMode(final int id, final boolean unused, final PathwayModeType type, final int translation) {
		this((state) -> {
			final int factor = state.ordinal() < 3 ? (state.ordinal() * 3) : (6 + state.ordinal());
			return (info) -> UITexture.drawTexture(info, UISignalBoxTile.SIGNALS, UISignalBoxRendering.TILE_WIDTH,
					UISignalBoxRendering.TILE_WIDTH, (id + factor) * 0.0666667f, 0.0f,
					(id + factor) * 0.066667f + 0.06f, 1.0f);
		}, type, translation);
	}

	private EnumGuiMode(final float[] array) {
		this(array, PathwayModeType.NONE, 0);
	}

	private EnumGuiMode(final float[] array, final PathwayModeType type, final int translation) {
		this(array, type, translation, SignalBoxUtil.FREE_COLOR, 2);
	}

	private EnumGuiMode(final float[] array, final PathwayModeType type, final int translation, final int color,
			final int width) {
		this((_u) -> {
			float[] currentArray = Arrays.copyOf(array, array.length);
			for (int i = 0; i < array.length; i++) {
				currentArray[i] *= UISignalBoxRendering.TILE_WIDTH;
			}
			return (info) -> info.lines(color, width, currentArray);
		}, type, translation);
	}

	private EnumGuiMode(final ResourceLocation location, final PathwayModeType type, final int translation) {
		this.consumer = (state) -> ((info) -> UITexture.drawTexture(info, location, UISignalBoxRendering.TILE_WIDTH,
				UISignalBoxRendering.TILE_WIDTH, 0, 0, 1, 1));
		this.type = type;
		this.translation = translation;
	}

	private EnumGuiMode(final Function<SignalState, Consumer<DrawInfo>> consumer, final PathwayModeType type,
			final int translation) {
		this.consumer = consumer;
		this.type = type;
		this.translation = translation;
	}

	public PathwayModeType getModeType() {
		return type;
	}

	public static EnumGuiMode of(final ReadBuffer buffer) {
		return values()[buffer.getByteToUnsignedInt()];
	}
}
