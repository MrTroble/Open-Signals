package com.troblecodings.signals.guis;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Quaternion;
import com.troblecodings.guilib.ecs.entitys.BufferWrapper;
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
	private final SignalBoxConsumer consumer;
	private final UIEntity gridParent;
	private final ColorPoint[] colorSelections = new ColorPoint[SelectionType.values().length];

	public UISignalBoxRendering(final SignalBoxGrid grid, boolean showLines, SignalBoxConsumer consumer, UIEntity gridParent) {
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
	
	public void addSelection(int c, Point point, SelectionType type){
		colorSelections[type.ordinal()] = new ColorPoint(point, c);
	}
	
	public void clearSelection() {
		for (int i = 0; i < colorSelections.length; i++) {
			colorSelections[i] = null;
		}
	}
	
	@Override
	public void mouseEvent(MouseEvent event) {
		if(!this.visible) return;
		if(!this.gridParent.isHovered()) return;
		double x = event.x - parent.getLevelX();
		double y = event.y - parent.getLevelY();
		final double actualWidth = TILE_WIDTH * parent.getScaleX();
		final Point point = new Point((int)(x / actualWidth), (int)(y / actualWidth));
		if(event.state == EnumMouseState.RELEASE) {
			this.consumer.accept(this, point, event.key);
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
		for(ColorPoint c : colorSelections) {
			if(c != null) {
				info.push();
				info.translate(c.point.getX() * TILE_WIDTH, c.point.getY() * TILE_WIDTH, 0);
				info.alphaOn();
				info.blendOn();
	            info.applyColor();
	            final BufferWrapper wrapper = info.builder(Mode.QUADS,
	                    DefaultVertexFormat.POSITION_COLOR);
	            wrapper.quad(0, (int) TILE_WIDTH, 0,
	                    (int) TILE_WIDTH, c.color);
	            info.end();
	    		info.pop();
			}
		}
	}

	@Override
	public void update() {
	}

	public static class BoxEntity {
		UIEntity entity;
		UISignalBoxRendering rendering;
		public BoxEntity(UIEntity entity, UISignalBoxRendering rendering) {
			super();
			this.entity = entity;
			this.rendering = rendering;
		}
	}
	
	public static BoxEntity createSignalBoxEntity(final SignalBoxGrid sigGrid, boolean showLines, SignalBoxConsumer consumer) {
		final UIEntity grid = new UIEntity();
		grid.setInherits(true);
		grid.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
		grid.add(new UIBorder(0xFF000000, 4));
		grid.add(new UIScissor());

		final UIEntity entity = new UIEntity();
		entity.setWidth(TILE_WIDTH * TILE_COUNT);
		entity.setHeight(entity.getHeight());
		final UISignalBoxRendering rendering = new UISignalBoxRendering(sigGrid, showLines, consumer, grid);
		entity.add(rendering);

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
		return new BoxEntity(grid, rendering);
	}

	public void setColor(final Point point, final Function<ModeSet, Integer> color) {
		gridRender.computeIfPresent(point, (p, map) -> {
			map.forEach((set, info) -> info.color = color.apply(set));
			return map;
		});
	}
	
	public void setColor(final Point point, final ModeSet set, final int color) {
		gridRender.computeIfPresent(point, (p, map) -> {
			map.computeIfPresent(set, (u, m) -> { m.color = color; return m;});
			return map;
		});
	}


	private class ModeRenderInfo {
		public int color;
		public final Consumer<DrawInfo> component;

		public ModeRenderInfo(EnumGuiMode mode, SignalState state) {
			this.color = mode.getDefaultColor();
			final BiConsumer<DrawInfo, Integer> component = mode.consumer.apply(state);
			this.component = (info) -> component.accept(info, color);
		}

	}
	
	public static enum SelectionType {
		FIRST, SECOND;
	}
	
	private static class ColorPoint {
		public Point point;
		public int color;
		
		public ColorPoint(Point point, int color) {
			super();
			this.point = point;
			this.color = color;
		}
	}
	
	public static interface SignalBoxConsumer extends TriConsumer<UISignalBoxRendering, Point, Integer> {}

}
