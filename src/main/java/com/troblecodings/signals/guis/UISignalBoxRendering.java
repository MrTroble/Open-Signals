package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.EnumMouseState;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;

public class UISignalBoxRendering extends UIComponent {

	public static final int TILE_WIDTH = 10;
	public static final int TILE_COUNT = 100;
	public static final int GRID_COLOR = 0xFF5B5B5B;
	private static final float[] ALL_LINES = getLines();

	private static float[] getLines() {
		final float[] lines = new float[2 * (TILE_COUNT + 1) * 4];
		final float step = TILE_WIDTH;
		final float max = TILE_WIDTH * TILE_COUNT;
		for (int i = 0; i <= TILE_COUNT; i++) {
			final int offset = i * 4;
			final float pos = i * step;
			lines[offset] = pos;
			lines[offset + 1] = 0;
			lines[offset + 2] = pos;
			lines[offset + 3] = max;

			final int offset2 = (i + TILE_COUNT + 1) * 4;
			lines[offset2] = 0;
			lines[offset2 + 1] = pos;
			lines[offset2 + 2] = max;
			lines[offset2 + 3] = pos;
		}
		return lines;
	}

	private boolean showLines = false;
	private Map<Point, Map<ModeSet, ModeRenderInfo>> gridRender;
	private final BiConsumer<UISignalBoxRendering, Point> consumer;
	private final UIEntity gridParent;

	public UISignalBoxRendering(final SignalBoxGrid grid, boolean showLines, BiConsumer<UISignalBoxRendering, Point> consumer, UIEntity gridParent) {
		super();
		this.showLines = showLines;
		this.consumer = consumer;
		this.gridParent = gridParent;
		gridRender = Maps.newHashMap();
		List<SignalBoxNode> nodes = grid.getNodes();
		nodes.forEach(node -> addNode(node, SignalState.RED));
		// TODO Signal State
	}

	private void addNode(SignalBoxNode node, SignalState state) {
		Map<ModeSet, ModeRenderInfo> modesets = gridRender.computeIfAbsent(node.getPoint(), k -> Maps.newHashMap());
		node.forEach(modeSet -> modesets.put(modeSet, new ModeRenderInfo(modeSet.mode, state)));
		gridRender.put(node.getPoint(), modesets);
	}
	
	public void removeMode(Point point, ModeSet modeSet) {
		gridRender.computeIfPresent(point, (p, f) -> {
			f.computeIfPresent(modeSet, (a, b) -> null);
			return f.isEmpty() ? null:f;
		});
	}
	
	public void addMode(Point point, ModeSet modeSet) {
		gridRender.computeIfAbsent(point, k -> Maps.newHashMap()).put(modeSet, new ModeRenderInfo(modeSet.mode, SignalState.RED));
	}
	
	public boolean has(Point point, ModeSet modeSet) {
		return gridRender.containsKey(point) && gridRender.get(point).containsKey(modeSet);
	}
	
	private void drawModeSets(DrawInfo info, Map<ModeSet, ModeRenderInfo> render) {
		render.forEach((set, rInfo) -> {
			info.push();
			info.translate(TILE_WIDTH / 2, TILE_WIDTH / 2, 0);
			info.rotate(Quaternion.fromXYZ(0, 0, set.rotation.ordinal() * UIRotate.PERPENDICULAR_ANGLE));
			info.translate(-TILE_WIDTH / 2, -TILE_WIDTH / 2, set.mode.translation);
			rInfo.component.accept(info);
			info.pop();
		});
	}
	
	@Override
	public void mouseEvent(MouseEvent event) {
		if(!this.visible) return;
		if(!this.gridParent.isHovered()) return;
		double x = event.x - parent.getLevelX();
		double y = event.y - parent.getLevelY();
		final Point point = new Point((int)(x) / TILE_WIDTH, (int)(y)/ TILE_WIDTH);
		if(event.state == EnumMouseState.RELEASE && event.key == MouseEvent.LEFT_MOUSE) {
			this.consumer.accept(this, point);
		}
	}

	@Override
	public void draw(DrawInfo info) {
		if (showLines)
			info.lines(GRID_COLOR, 0.5f, ALL_LINES);
		gridRender.forEach((point, modelist) -> {
			info.push();
			info.translate(TILE_WIDTH * point.getX(), TILE_WIDTH * point.getY(), 0);
			drawModeSets(info, modelist);
			info.pop();
		});
	}

	@Override
	public void update() {
	}

	public static UIEntity createSignalBoxEntity(final SignalBoxGrid sigGrid, boolean showLines, BiConsumer<UISignalBoxRendering, Point> consumer) {
		final UIEntity grid = new UIEntity();
		grid.setInherits(true);
		grid.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
		grid.add(new UIBorder(0xFF000000, 4));
		grid.add(new UIScissor());

		final UIEntity entity = new UIEntity();
		entity.setWidth(TILE_WIDTH * TILE_COUNT);
		entity.setHeight(entity.getHeight());
		entity.add(new UISignalBoxRendering(sigGrid, showLines, consumer, grid));

		grid.add(new UIScroll(s -> {
			final float newScale = (float) (entity.getScaleX() + s * 0.01f);
			if (newScale <= 0)
				return;
			entity.setScaleX(newScale);
			entity.setScaleY(newScale);
			entity.update();
		}));
		grid.add(new UIDrag((x, y) -> {
			entity.setX(entity.getX() + x);
			entity.setY(entity.getY() + y);
			entity.update();
		}, 2));

		grid.add(entity);
		return grid;
	}

	public void setColor(final Point point, final ModeSet mode, final int color) {
		final ModeRenderInfo entity = gridRender.get(point).get(mode);
		// TODO value
	}

	private class ModeRenderInfo {
		public final Consumer<DrawInfo> component;

		public ModeRenderInfo(EnumGuiMode mode, SignalState state) {
			final Consumer<DrawInfo> component = mode.consumer.apply(state);
			this.component = component;
		}

	}

}
