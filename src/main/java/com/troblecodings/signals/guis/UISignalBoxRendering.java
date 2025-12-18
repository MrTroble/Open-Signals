package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.troblecodings.core.QuaternionWrapper;
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
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Rotation;

public class UISignalBoxRendering extends UIComponent {

    public static final int TILE_WIDTH = 10;
    public static final int HALF_TILE = UISignalBoxRendering.TILE_WIDTH / 2;
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
    private final Map<Point, Map<ModeSet, ModeRenderInfo>> gridRender;
    private final Map<Point, String> nodeLabeling;
    private final FontRenderer font = Minecraft.getInstance().font;
    private final SignalBoxConsumer consumer;
    private final UIEntity gridParent;
    private final ColorPoint[] colorSelections = new ColorPoint[SelectionType.values().length];
    private final Map<ModeIdentifier, String> trainNumbers = new HashMap<>();
    private final Set<ColorPoint> additionalPoints = new HashSet<>();

    public UISignalBoxRendering(final SignalBoxGrid grid, final boolean showLines,
            final SignalBoxConsumer consumer, final UIEntity gridParent) {
        this.showLines = showLines;
        this.consumer = consumer;
        this.gridParent = gridParent;
        gridRender = Maps.newHashMap();
        nodeLabeling = Maps.newHashMap();
        final List<SignalBoxNode> nodes = grid.getNodes();
        nodes.forEach(this::addNode);
    }

    private void addNode(final SignalBoxNode node) {
        final Map<ModeSet, ModeRenderInfo> modesets =
                gridRender.computeIfAbsent(node.getPoint(), k -> Maps.newHashMap());
        node.forEach(modeSet -> modesets.put(modeSet,
                new ModeRenderInfo(modeSet.mode, node.getState(modeSet))));
        gridRender.put(node.getPoint(), modesets);
        nodeLabeling.put(node.getPoint(), node.getCustomText());
    }

    public void updateNodeLabeling(final Point point, final String labeling) {
        if (labeling.isEmpty()) {
            nodeLabeling.remove(point);
        } else {
            nodeLabeling.put(point, labeling);
        }
    }

    public void removeMode(final Point point, final ModeSet modeSet) {
        gridRender.computeIfPresent(point, (p, f) -> {
            f.computeIfPresent(modeSet, (a, b) -> null);
            return f.isEmpty() ? null : f;
        });
    }

    public void addMode(final Point point, final ModeSet modeSet) {
        gridRender.computeIfAbsent(point, k -> Maps.newHashMap()).put(modeSet,
                new ModeRenderInfo(modeSet.mode, SignalState.RED));
    }

    public boolean has(final Point point, final ModeSet modeSet) {
        return gridRender.containsKey(point) && gridRender.get(point).containsKey(modeSet);
    }

    private void drawModeSets(final DrawInfo info, final Map<ModeSet, ModeRenderInfo> render) {
        render.forEach((set, rInfo) -> {
            info.push();
            info.depthOn();
            info.translate(HALF_TILE, HALF_TILE, 0);
            info.rotate(QuaternionWrapper.fromXYZ(0, 0,
                    set.rotation.ordinal() * UIRotate.PERPENDICULAR_ANGLE));
            info.translate(-HALF_TILE, -HALF_TILE, set.mode.depthFunc.apply(rInfo.state));
            rInfo.component.accept(info);
            info.pop();
        });
    }

    public void putTrainNumber(final ModeIdentifier modeIdent, final String text) {
        trainNumbers.put(modeIdent, text);
    }

    public void removeTrainNumber(final ModeIdentifier modeIdent) {
        trainNumbers.remove(modeIdent);
    }

    public void clearTrainNumbers() {
        trainNumbers.clear();
    }

    public boolean hasSelection(final int c, final Point point, final SelectionType type) {
        final ColorPoint colorPoint = colorSelections[type.ordinal()];
        return colorPoint == null ? false : colorPoint.equals(new ColorPoint(point, c));
    }

    public void addSelection(final int c, final Point point, final SelectionType type) {
        final ColorPoint colorPoint = new ColorPoint(point, c);
        if (colorSelections[type.ordinal()] == colorPoint) {
            colorSelections[type.ordinal()] = null;
        } else {
            colorSelections[type.ordinal()] = colorPoint;
        }
    }

    public void removeSelection(final SelectionType type) {
        colorSelections[type.ordinal()] = null;
    }

    public void clearSelection() {
        for (int i = 0; i < colorSelections.length; i++) {
            colorSelections[i] = null;
        }
    }

    public void addColoredPoint(final int c, final Point point) {
        additionalPoints.add(new ColorPoint(point, c));
    }

    public void removeColoredPoint(final int c, final Point point) {
        additionalPoints.remove(new ColorPoint(point, c));
    }

    @Override
    public void mouseEvent(final MouseEvent event) {
        if (!this.visible)
            return;
        if (!this.gridParent.isHovered())
            return;
        final double x = event.x - parent.getLevelX();
        final double y = event.y - parent.getLevelY();
        final double actualWidth = TILE_WIDTH * parent.getScaleX();
        final Point point = new Point((int) (x / actualWidth), (int) (y / actualWidth));
        if (event.state == EnumMouseState.RELEASE) {
            this.consumer.accept(this, point, event.key);
        }
    }

    @Override
    public void draw(final DrawInfo info) {
        if (showLines) {
            info.lines(GRID_COLOR, 0.5f, ALL_LINES);
        }
        gridRender.forEach((point, modelist) -> {
            info.push();
            info.translate(TILE_WIDTH * point.getX(), TILE_WIDTH * point.getY(), 0);
            drawModeSets(info, modelist);
            info.pop();
        });
        for (final ColorPoint c : colorSelections) {
            if (c != null) {
                renderColorPoint(info, c);
            }
        }
        for (final ColorPoint c : additionalPoints) {
            renderColorPoint(info, c);
        }
        final int signalBoxTrainNumberColor = ConfigHandler.CLIENT.signalboxTrainNumberColor.get();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        trainNumbers.forEach((point, number) -> renderText(info, point.point, point.mode.rotation,
                number, (int) 6.5f, (4 * TILE_WIDTH - font.width(number)) / 2,
                signalBoxTrainNumberColor, 0.5f));
        nodeLabeling.forEach((point, label) -> renderText(info, point, Rotation.NONE, label,
                (TILE_WIDTH - font.lineHeight) / 2 - 5, (TILE_WIDTH - font.width(label) + 4) / 2,
                0xFFFFFFFF, 0.7f));
        RenderSystem.blendColor(1, 1, 1, 1);
    }

    private void renderText(final DrawInfo info, final Point point, final Rotation rot,
            final String str, final int restHeight, final int restWidth, final int color,
            final float scale) {
        info.push();
        info.translate(TILE_WIDTH * point.getX(), TILE_WIDTH * point.getY(), 0);
        if (!rot.equals(Rotation.NONE)) {
            info.translate(HALF_TILE, HALF_TILE, 0);
            info.rotate(
                    QuaternionWrapper.fromXYZ(0, 0, rot.ordinal() * UIRotate.PERPENDICULAR_ANGLE));
            info.translate(-HALF_TILE, -HALF_TILE, 0);
        }
        info.scale(scale, scale, scale);
        font.draw(info.stack, str, restWidth, restHeight, color);
        info.pop();
    }

    private void renderColorPoint(final DrawInfo info, final ColorPoint c) {
        info.push();
        info.translate(c.point.getX() * TILE_WIDTH, c.point.getY() * TILE_WIDTH, 0);
        info.alphaOn();
        info.blendOn();
        info.applyColor();
        final BufferWrapper wrapper =
                info.builder(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wrapper.quad(0, TILE_WIDTH, 0, TILE_WIDTH, c.color);
        info.end();
        info.pop();
    }

    @Override
    public void update() {
    }

    public static class BoxEntity {

        public final UIEntity entity;
        public final UISignalBoxRendering rendering;

        public BoxEntity(final UIEntity entity, final UISignalBoxRendering rendering) {
            this.entity = entity;
            this.rendering = rendering;
        }
    }

    public static BoxEntity createSignalBoxEntity(final SignalBoxGrid sigGrid,
            final boolean showLines, final SignalBoxConsumer consumer) {
        final UIEntity grid = new UIEntity();
        grid.setInherits(true);
        grid.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        grid.add(new UIBorder(0xFF000000, 4));
        grid.add(new UIScissor());

        final UIEntity entity = new UIEntity();
        entity.setWidth(TILE_WIDTH * TILE_COUNT);
        entity.setHeight(entity.getHeight());
        final UISignalBoxRendering rendering =
                new UISignalBoxRendering(sigGrid, showLines, consumer, grid);
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
            map.computeIfPresent(set, (u, m) -> {
                m.color = color;
                return m;
            });
            return map;
        });
    }

    public void updateSignalState(final Point point, final ModeSet set, final SignalState state) {
        gridRender.computeIfPresent(point, (p, map) -> {
            map.computeIfPresent(set, (u, m) -> new ModeRenderInfo(m, state));
            return map;
        });
    }

    private class ModeRenderInfo {

        public final SignalState state;
        public int color;
        private final EnumGuiMode mode;
        public final Consumer<DrawInfo> component;

        public ModeRenderInfo(final EnumGuiMode mode, final SignalState state) {
            this.color = mode.getDefaultColor();
            this.state = state;
            final BiConsumer<DrawInfo, Integer> component = mode.consumer.apply(state);
            this.mode = mode;
            this.component = (info) -> component.accept(info, color);
        }

        public ModeRenderInfo(final ModeRenderInfo old, final SignalState state) {
            this.mode = old.mode;
            this.color = old.color;
            this.state = state;
            final BiConsumer<DrawInfo, Integer> component = mode.consumer.apply(state);
            this.component = (info) -> component.accept(info, color);
        }

    }

    public static enum SelectionType {
        FIRST, SECOND;
    }

    private static class ColorPoint {

        public final Point point;
        public final int color;

        public ColorPoint(final Point point, final int color) {
            this.point = point;
            this.color = color;
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, point);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ColorPoint other = (ColorPoint) obj;
            return color == other.color && Objects.equals(point, other.point);
        }

    }

    public static interface SignalBoxConsumer
            extends TriConsumer<UISignalBoxRendering, Point, Integer> {
    }

}
